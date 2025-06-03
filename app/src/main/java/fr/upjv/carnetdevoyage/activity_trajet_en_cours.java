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

public class activity_trajet_en_cours extends AppCompatActivity {

    private String nomVoyage;
    private final List<GPSPosition> listePositions = new ArrayList<>();
    private boolean tracking = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FirebaseFirestore firebaseFirestore;

    private TextView textTitre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet_en_cours);

        nomVoyage = getIntent().getStringExtra("nomVoyage");
        textTitre = findViewById(R.id.id_trajet_en_cours_textview);
        textTitre.setText("Trajet : " + nomVoyage);

        firebaseFirestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        demanderLocalisation();
        configurerLocationCallback();

        // Démarrer le suivi
        tracking = true;
        listePositions.clear();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        debuterObtentionDesPositions();
    }

    private void demanderLocalisation() {
        locationRequest = new LocationRequest.Builder(30000) // 30 secondes
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
                    Toast.makeText(activity_trajet_en_cours.this,
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
        DocumentReference documentReference = firebaseFirestore.collection("utilisateur_1").document(nomVoyage);

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
}
