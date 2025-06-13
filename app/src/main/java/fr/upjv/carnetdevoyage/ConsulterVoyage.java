package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_consultation_voyage_map);

        nomVoyage = getIntent().getStringExtra("nom_voyage");
        firestore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Toast.makeText(this, "page consulter voyage : " , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        firestore.collection("utilisateur_1")
                .document(nomVoyage)
                .collection("positions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat != null && lng != null) {
                            listePoints.add(new LatLng(lat, lng));
                        }
                    }

                    if (!listePoints.isEmpty()) {
                        mMap.addPolyline(new PolylineOptions().addAll(listePoints));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(listePoints.get(0), 12));
                    }
                })
                .addOnFailureListener(e -> Log.e("MAP_VOYAGE", "Erreur chargement positions", e));
    }
}


