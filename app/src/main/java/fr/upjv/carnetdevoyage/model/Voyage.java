package fr.upjv.carnetdevoyage.model;

public class Voyage {
    private String nomVoyage;
    private long dateDebutVoyage;
    private long dateFinVoyage;

    public Voyage() {}

    public Voyage(String nomVoyage, long dateDebutVoyage, long dateFinVoyage) {
        this.nomVoyage = nomVoyage;
        this.dateDebutVoyage = dateDebutVoyage;
        this.dateFinVoyage = dateFinVoyage;
    }

    public String getNomVoyage() {
        return nomVoyage;
    }

    public void setNomVoyage(String nomVoyage) {
        this.nomVoyage = nomVoyage;
    }

    public long getDateDebutVoyage() {
        return dateDebutVoyage;
    }

    public void setDateDebutVoyage(long dateDebutVoyage) {
        this.dateDebutVoyage = dateDebutVoyage;
    }

    public long getDateFinVoyage() {
        return dateFinVoyage;
    }

    public void setDateFinVoyage(long dateFinVoyage) {
        this.dateFinVoyage = dateFinVoyage;
    }
}
