package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.carnetdevoyage.adapter.VoyageAdapter;
import fr.upjv.carnetdevoyage.model.Voyage;

public class ActiviteListeTrajetUtilisateur extends AppCompatActivity implements VoyageAdapter.OnVoyageClickListener {

    private ActivityResultLauncher<Intent> formulaireVoyageLauncher;
    private RecyclerView recyclerViewVoyages;
    private VoyageAdapter voyageAdapter;
    private List<Voyage> listeVoyages;
    private FirebaseFirestore firebaseFirestore;

    private String nomUtilisateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_liste_trajet_utilisateur);

        nomUtilisateur = getIntent().getStringExtra("nomUtilisateur");
        if (nomUtilisateur == null || nomUtilisateur.isEmpty()) {
            Toast.makeText(this, "Aucun utilisateur fourni", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerViewVoyages = findViewById(R.id.id_liste_trajet_utilisateur_rv);
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialiser la recyclerView
        listeVoyages = new ArrayList<>();
        voyageAdapter = new VoyageAdapter(listeVoyages, this);
        recyclerViewVoyages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVoyages.setAdapter(voyageAdapter);

        // Permet de lancer une autre activité
        formulaireVoyageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Recharger la liste des voyages après avoir créé un nouveau trajet
                    chargerVoyages();
                }
        );
        chargerVoyages();
    }

    @Override
    public void onVoyageClick(Voyage voyage) {
        Intent intent = new Intent(this, ConsulterVoyage.class);
        intent.putExtra("nom_voyage", voyage.getNomVoyage());
        intent.putExtra("nomUtilisateur", nomUtilisateur);
        startActivity(intent);
    }

    private void chargerVoyages() {
        firebaseFirestore.collection(nomUtilisateur)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Voyage> nouveauxVoyages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String nomVoyage = document.getString("nom_voyage");
                                Long dateDebut = document.getLong("date_debut_voyage");
                                Long dateFin = document.getLong("date_fin_voyage");

                                if (nomVoyage != null && dateDebut != null && dateFin != null) {
                                    Voyage voyage = new Voyage(nomVoyage, dateDebut, dateFin);
                                    nouveauxVoyages.add(voyage);
                                }
                            } catch (Exception e) {}
                        }
                        listeVoyages.clear();
                        listeVoyages.addAll(nouveauxVoyages);
                        listeVoyages.sort((v1, v2) -> Long.compare(v2.getDateDebutVoyage(), v1.getDateDebutVoyage()));
                        // Actualise la liste des voyages
                        voyageAdapter.notifyDataSetChanged();
                        if (listeVoyages.isEmpty()) {
                            Toast.makeText(this, "Aucun voyage trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Erreur lors du chargement des voyages", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}