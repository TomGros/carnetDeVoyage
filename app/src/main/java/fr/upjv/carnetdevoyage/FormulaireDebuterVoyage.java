package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class FormulaireDebuterVoyage extends AppCompatActivity {

    private EditText nomVoyageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire_debuter_voyage);

        nomVoyageEditText = findViewById(R.id.id_nom_voyage_edittext);

        Spinner dureeSpinner = findViewById(R.id.id_duree_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.duree_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dureeSpinner.setAdapter(adapter);


    }

    public void onClickValiderVoyage(View view) {
        String nomVoyage = nomVoyageEditText.getText().toString().trim();
        Spinner dureeSpinner = findViewById(R.id.id_duree_spinner);
        String dureeChoisie = dureeSpinner.getSelectedItem().toString();

        if (!nomVoyage.isEmpty() && !dureeChoisie.isEmpty()) {
            Intent intent = new Intent(this, AffichageTrajetEnCours.class);
            intent.putExtra("nomVoyage", nomVoyage);
            intent.putExtra("delaiActualisation", dureeChoisie);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Veuillez entrer un nom de voyage et choisir une durée d'actualisation", Toast.LENGTH_SHORT).show();
        }
    }


    public void onClickAnnuler(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}