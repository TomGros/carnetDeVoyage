package fr.upjv.carnetdevoyage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class ConsulterVoyage extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String nomVoyage;
    private String nomUtilisateur;
    private FirebaseFirestore firestore;
    private final List<LatLng> listePoints = new ArrayList<>();

    private TextView nomVoyageTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_consultation_voyage_map);

        nomVoyage = getIntent().getStringExtra("nom_voyage");
        nomUtilisateur = getIntent().getStringExtra("nomUtilisateur");

        nomVoyageTextView = findViewById(R.id.nom_voyage_textview);


        if (nomVoyage != null) {
            nomVoyageTextView.setText(nomVoyage);
        }


        nomVoyage = getIntent().getStringExtra("nom_voyage");
        firestore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        firestore.collection(nomUtilisateur)
                .document(nomVoyage)
                .collection("positions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<QueryDocumentSnapshot> documents = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        documents.add(doc);
                    }

                    documents.sort((d1, d2) -> {
                        Long t1 = d1.getLong("timestamp");
                        Long t2 = d2.getLong("timestamp");
                        return Long.compare(t1 != null ? t1 : 0, t2 != null ? t2 : 0);
                    });

                    for (QueryDocumentSnapshot doc : documents) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat != null && lng != null) {
                            listePoints.add(new LatLng(lat, lng));
                        }
                    }

                    if (!listePoints.isEmpty()) {
                        mMap.addPolyline(new PolylineOptions()
                                .addAll(listePoints)
                                .color(Color.RED)
                                .width(8f));

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                        for (LatLng point : listePoints) {
                            boundsBuilder.include(point);
                        }
                        LatLngBounds bounds = boundsBuilder.build();

                        int padding = 150;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    } else {
                        Toast.makeText(this, "Aucune position à afficher", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors du chargement des positions", Toast.LENGTH_SHORT).show());
    }

    public void onClickPartagerVoyage(View view) {
        if (listePoints.isEmpty()) {
            Toast.makeText(this, "Aucun trajet à partager", Toast.LENGTH_SHORT).show();
            return;
        }
        File gpxFile = genererFichierGPX();
        if (gpxFile != null) {
            envoyerParEmail(gpxFile);
        } else {
            Toast.makeText(this, "Erreur lors de la génération du fichier GPX", Toast.LENGTH_SHORT).show();
        }
    }

    private File genererFichierGPX() {
        try {
            File gpxFile = new File(getCacheDir(), nomVoyage + "_trajet.gpx");
            FileWriter writer = new FileWriter(gpxFile);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<gpx version=\"1.1\" creator=\"Carnet de Voyage\">\n");
            writer.write("  <trk>\n");
            writer.write("    <name>" + nomVoyage + "</name>\n");
            writer.write("    <trkseg>\n");

            for (LatLng point : listePoints) {
                writer.write("      <trkpt lat=\"" + point.latitude + "\" lon=\"" + point.longitude + "\">\n");
                writer.write("      </trkpt>\n");
            }

            writer.write("    </trkseg>\n");
            writer.write("  </trk>\n");
            writer.write("</gpx>");
            writer.close();
            return gpxFile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void envoyerParEmail(File gpxFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    gpxFile
            );
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, nomVoyage);
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Veuillez trouver ci-joint le trajet \"" + nomVoyage + "\" au format GPX.");
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(emailIntent, "Envoyer le trajet par email");
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
                Toast.makeText(this, "Email envoyé avec succès", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Aucune application email trouvée", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'envoi de l'email", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSupprimerVoyage(View view) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer le voyage ?")
                .setPositiveButton("Oui, supprimer", (dialog, which) -> supprimerVoyage())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerVoyage() {
        firestore.collection(nomUtilisateur)
                .document(nomVoyage)
                .collection("positions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    firestore.collection(nomUtilisateur)
                            .document(nomVoyage)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Voyage supprimé avec succès", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur lors de la suppression du voyage", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la suppression des positions", Toast.LENGTH_SHORT).show();
                });
    }
}


