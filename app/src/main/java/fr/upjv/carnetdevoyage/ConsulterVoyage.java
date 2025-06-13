package fr.upjv.carnetdevoyage;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class ConsulterVoyage extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String nomVoyage;
    private FirebaseFirestore firestore;
    private final List<LatLng> listePoints = new ArrayList<>();

    private TextView nomVoyageTextView;
    private ImageButton buttonMail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_consultation_voyage_map);

        nomVoyage = getIntent().getStringExtra("nom_voyage");

        nomVoyageTextView = findViewById(R.id.nom_voyage_textview);
        buttonMail = findViewById(R.id.button_mail);

        if (nomVoyage != null) {
            nomVoyageTextView.setText(nomVoyage);
        }

        // Pour gautier : bouton mail
        buttonMail.setOnClickListener(v ->
                Toast.makeText(this, "Fonction mail à venir", Toast.LENGTH_SHORT).show()
        );


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

        firestore.collection("utilisateur_1")
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


}


