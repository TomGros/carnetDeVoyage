package fr.upjv.carnetdevoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.upjv.carnetdevoyage.R;
import fr.upjv.carnetdevoyage.model.Voyage;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    private List<Voyage> listeVoyages;
    private OnVoyageClickListener listener;

    public interface OnVoyageClickListener {
        void onVoyageClick(Voyage voyage);
    }

    public VoyageAdapter(List<Voyage> listeVoyages, OnVoyageClickListener listener) {
        this.listeVoyages = listeVoyages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voyage, parent, false);
        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = listeVoyages.get(position);
        holder.bind(voyage);
    }

    @Override
    public int getItemCount() {
        return listeVoyages.size();
    }

    public class VoyageViewHolder extends RecyclerView.ViewHolder {

        private TextView nomVoyage;
        private TextView duree;
        private SimpleDateFormat dateFormat;

        public VoyageViewHolder(@NonNull View itemView) {
            super(itemView);
            nomVoyage = itemView.findViewById(R.id.id_nomVoyage_textview);
            duree = itemView.findViewById(R.id.id_dureeVoyage_textview);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onVoyageClick(listeVoyages.get(position));
                    }
                }
            });
        }

        public void bind(Voyage voyage) {
            nomVoyage.setText(voyage.getNomVoyage());

            long debut = voyage.getDateDebutVoyage();
            long fin = voyage.getDateFinVoyage();

            if (fin < debut) {
                duree.setText("Durée invalide");
                return;
            }

            long diff = fin - debut;
            long totalMinutes = diff / (1000 * 60);
            long heures = totalMinutes / 60;
            long minutes = totalMinutes % 60;

            String affichageDuree = String.format(Locale.getDefault(), "%dh%02d", heures, minutes);
            duree.setText("Durée : " + affichageDuree);
        }
    }
}