package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitePrincipale extends AppCompatActivity {

    private ActivityResultLauncher<Intent> formulaireVoyageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_principale);

        formulaireVoyageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // pour gerer le retour du formualaire
                }
        );
    }

    public void onClickDebuterTrajet(View view) {
        formulaireVoyageLauncher.launch(new Intent(this, FormulaireDebuterVoyage.class));
    }
}
