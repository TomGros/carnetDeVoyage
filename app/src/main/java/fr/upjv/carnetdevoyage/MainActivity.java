package fr.upjv.carnetdevoyage;

import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.upjv.carnetdevoyage.model.GPSPosition;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    // Stocke toutes les positions du trajet pour pouvoir l'enregister en base par la suite
    private List<GPSPosition> positionList = new ArrayList<>();
    // Permet de gérer l'attente de 30s entre chaque géolocalisation
    private Handler handler;
    private Runnable runnable;
    // Si = true alors on est en train de créer un voyage
    private boolean tracking = false;
    // serivce google permettant de géolocaliser
    private FusedLocationProviderClient fusedLocationClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connexionFirebase();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void connexionFirebase(){
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
    }

    public void onClickDebuterTrajet(View view) {
        Toast.makeText(this, "Début du trajet", Toast.LENGTH_SHORT).show();
        tracking = true;
        positionList.clear();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        startLocationUpdates();
    }

    public void onClickTerminerTrajet(View view) {
        tracking = false;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        saveTripToFirestore("voyage1");
        Toast.makeText(this, "Fin du trajet !", Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (tracking) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Demander la permission si elle n'est pas accordée
                        return;
                    }

                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        GPSPosition position = new GPSPosition(
                                                location.getLatitude(),
                                                location.getLongitude(),
                                                System.currentTimeMillis()
                                        );
                                        positionList.add(position);
                                    }
                                }
                            });

                    handler.postDelayed(this, 30000); // 30s
                }
            }
        };
        handler.post(runnable);
    }

    private void saveTripToFirestore(String voyageName) {

        DocumentReference newDocument = this.firebaseFirestore.collection("utilisateur_1").document("voyage");

        Map<String, Object> map = new HashMap<>();
        map.put("nom_voyage", voyageName);
        map.put("date_debut_voyage", positionList.get(0).timestamp);
        map.put("date_fin_voyage", positionList.get(positionList.size() - 1).timestamp);

        newDocument.set(map);
        int i = 0;
        for (GPSPosition position : positionList) {
            i++;
            Map<String, Object> listePositions = new HashMap<>();
            listePositions.put("latitude", position.latitude);
            listePositions.put("longitude", position.longitude);
            listePositions.put("timestamp", position.timestamp);
            newDocument.collection("positions").document("position"+i).set(listePositions);

            Toast.makeText(MainActivity.this, "Voyage enregistré !", Toast.LENGTH_SHORT).show();
        }

    }
}