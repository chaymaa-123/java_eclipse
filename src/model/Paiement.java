package model;

import java.time.LocalDateTime;

public class Paiement {
    private int id;
    private double montant;
    private String mode;
    private LocalDateTime dateHeure;

    public Paiement(int id, double montant, String mode, LocalDateTime dateHeure) {
        this.id = id;
        this.montant = montant;
        this.mode = mode;
        this.dateHeure = dateHeure;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }
}
