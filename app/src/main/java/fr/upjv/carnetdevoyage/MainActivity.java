package fr.upjv.carnetdevoyage;

import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.upjv.carnetdevoyage.model.GPSPosition;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;

    // Stocke toutes les positions du trajet dans une liste pour pouvoir l'enregistrer en base par la suite
    private List<GPSPosition> listePositions = new ArrayList<>();
    // Si = true alors on est en train de créer un voyage
    private boolean tracking = false;


    // serivce google permettant de géolocaliser
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connexionFirebase();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        demanderLocalisation();

        // Configurer le callback de localisation
        locationCallback = new LocationCallback() {

            // Méthode présente dans la class LocationCallBack
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || !tracking) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GPSPosition position = new GPSPosition(
                            location.getLatitude(),
                            location.getLongitude(),
                            System.currentTimeMillis()
                    );
                    listePositions.add(position);
                    Toast.makeText(MainActivity.this,
                            "Position ajoutée: " + position.latitude + ", " + position.longitude,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    private void demanderLocalisation() {
        locationRequest = new LocationRequest.Builder(30000) // 30 secondes
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(5) // 5 mètres minimum entre les mises à jour
                .build();
    }

    public void connexionFirebase(){
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
    }

    public void onClickDebuterTrajet(View view) {
        Toast.makeText(this, "Début du trajet", Toast.LENGTH_SHORT).show();
        tracking = true;
        listePositions.clear();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        debuterObtentionDesPositions();
    }

    public void onClickTerminerTrajet(View view) {
        tracking = false;
        terminerObtentionDesPositions();
        if (!listePositions.isEmpty()) {
            enregistrerVoyage("voyage2");
            Toast.makeText(this, "Fin du trajet !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Aucune position enregistrée", Toast.LENGTH_SHORT).show();
        }
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

    private void enregistrerVoyage(String nomVoyage) {

        DocumentReference documentReference = this.firebaseFirestore.collection("utilisateur_1").document("voyage");

        Map<String, Object> map = new HashMap<>();
        map.put("nom_voyage", nomVoyage);
        map.put("date_debut_voyage", listePositions.get(0).timestamp);
        map.put("date_fin_voyage", listePositions.get(listePositions.size() - 1).timestamp);

        documentReference.set(map);
        int i = 0;
        for (GPSPosition position : listePositions) {
            i++;
            Map<String, Object> listePositions = new HashMap<>();
            listePositions.put("latitude", position.latitude);
            listePositions.put("longitude", position.longitude);
            listePositions.put("timestamp", position.timestamp);
            documentReference.collection("positions").document("position"+i).set(listePositions);

            Toast.makeText(MainActivity.this, "Voyage enregistré !", Toast.LENGTH_SHORT).show();
        }

    }
}