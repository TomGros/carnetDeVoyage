package fr.upjv.carnetdevoyage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.upjv.carnetdevoyage.model.GPSPosition;

public class AffichageTrajetEnCours extends AppCompatActivity {

    private String nomVoyage;
    private String nomUtilisateur;
    private String delaiActualisation;
    private TextView textViewNombrePositions;
    private final List<GPSPosition> listePositions = new ArrayList<>();
    private boolean tracking = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_affichage_trajet_en_cours);

        nomVoyage = getIntent().getStringExtra("nomVoyage");
        nomUtilisateur = getIntent().getStringExtra("nomUtilisateur");
        delaiActualisation = getIntent().getStringExtra("delaiActualisation");
        TextView textTitreDeLaPage = findViewById(R.id.id_trajet_en_cours_textview);
        textTitreDeLaPage.setText(String.format("%s%s", getString(R.string.titre_page_trajet_en_cours), nomVoyage));

        firebaseFirestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        demanderLocalisation();
        configurerLocationCallback();

        // Début du suivi des positions
        tracking = true;
        listePositions.clear();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        debuterObtentionDesPositions();

        textViewNombrePositions = findViewById(R.id.id_nombre_positions_textview);
    }

    private void demanderLocalisation() {
        locationRequest = new LocationRequest.Builder(Long.parseLong(delaiActualisation)*1000) // selon le temps choisi par l'utilisateur
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
    }

    private void configurerLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!tracking) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GPSPosition position = new GPSPosition(
                            location.getLatitude(),
                            location.getLongitude(),
                            System.currentTimeMillis()
                    );
                    listePositions.add(position);
                    mettreAJourNombrePositions();
                    Toast.makeText(AffichageTrajetEnCours.this,
                            "Position ajoutée : " + position.latitude + ", " + position.longitude,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void debuterObtentionDesPositions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void terminerObtentionDesPositions() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void onClickTerminerTrajet(View view) {
        tracking = false;
        terminerObtentionDesPositions();
        if (!listePositions.isEmpty()) {
            enregistrerVoyage(nomVoyage);
            Toast.makeText(this, "Trajet terminé et enregistré", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Aucune position enregistrée", Toast.LENGTH_SHORT).show();
        }
    }

    private void enregistrerVoyage(String nomVoyage) {
        DocumentReference documentReference = firebaseFirestore.collection(nomUtilisateur).document(nomVoyage);

        Map<String, Object> map = new HashMap<>();
        map.put("nom_voyage", nomVoyage);
        map.put("date_debut_voyage", listePositions.get(0).timestamp);
        map.put("date_fin_voyage", listePositions.get(listePositions.size() - 1).timestamp);

        documentReference.set(map);

        int i = 0;
        for (GPSPosition position : listePositions) {
            i++;
            Map<String, Object> positionMap = new HashMap<>();
            positionMap.put("latitude", position.latitude);
            positionMap.put("longitude", position.longitude);
            positionMap.put("timestamp", position.timestamp);

            documentReference.collection("positions").document("position" + i).set(positionMap);
        }
    }

    public void onClickAjouterPositionManuellement(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this); // On arrête dès qu'on a recupere la position

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GPSPosition position = new GPSPosition(
                            location.getLatitude(),
                            location.getLongitude(),
                            System.currentTimeMillis()
                    );
                    listePositions.add(position);
                    mettreAJourNombrePositions();
                    Toast.makeText(AffichageTrajetEnCours.this,
                            "Position ajoutée manuellement : " + position.latitude + ", " + position.longitude,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AffichageTrajetEnCours.this, "Position actuelle indisponible", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }
    private void mettreAJourNombrePositions() {
        String texte = "" + listePositions.size();
        textViewNombrePositions.setText(String.format("%s%s", getString(R.string.texte_nb_positions), texte));
    }

}
