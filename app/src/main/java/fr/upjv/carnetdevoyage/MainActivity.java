package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> formulaireVoyageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        formulaireVoyageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Ici tu peux gérer le retour du formulaire si besoin (facultatif maintenant)
                }
        );
    }

    public void onClickDebuterTrajet(View view) {
        formulaireVoyageLauncher.launch(new Intent(this, FormulaireDebuterVoyage.class));
    }
}
