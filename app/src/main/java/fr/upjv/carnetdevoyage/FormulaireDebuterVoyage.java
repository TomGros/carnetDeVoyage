package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FormulaireDebuterVoyage extends AppCompatActivity {

    private EditText nomVoyageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire_debuter_voyage);

        nomVoyageEditText = findViewById(R.id.id_nom_voyage_edittext);
    }

    public void onClickValiderVoyage(View view) {
        String nomVoyage = nomVoyageEditText.getText().toString().trim();

        if (!nomVoyage.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("nomVoyage", nomVoyage);
            setResult(RESULT_OK, resultIntent);
            finish(); // on retourne à MainActivity
        } else {
            Toast.makeText(this, "Veuillez entrer un nom de voyage", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickAnnuler(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}