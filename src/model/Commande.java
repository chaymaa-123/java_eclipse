package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private LocalDateTime dateHeure;
    private double total;
    private String statut; // "En cours", "Servie", "Payée"

    private Client client;
    private Serveur serveur;
    private TableRestaurant table;
    private List<LigneCommande> lignesCommande;
    private Paiement paiement;

    public Commande(int id, LocalDateTime dateHeure, Client client, Serveur serveur, TableRestaurant table) {
        this.id = id;
        this.dateHeure = dateHeure;
        this.client = client;
        this.serveur = serveur;
        this.table = table;
        this.lignesCommande = new ArrayList<>();
        this.statut = "En cours";
        this.total = 0.0;
    }

    public void ajouterLigne(LigneCommande ligne) {
        lignesCommande.add(ligne);
        calculerTotal();
    }

    public void calculerTotal() {
        this.total = 0.0;
        for (LigneCommande ligne : lignesCommande) {
            this.total += ligne.getSousTotal();
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public double getTotal() {
        return total;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Serveur getServeur() {
        return serveur;
    }

    public void setServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    public TableRestaurant getTable() {
        return table;
    }

    public void setTable(TableRestaurant table) {
        this.table = table;
    }

    public List<LigneCommande> getLignesCommande() {
        return lignesCommande;
    }

    public Paiement getPaiement() {
        return paiement;
    }

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
        if (paiement != null) {
            this.statut = "Payée";
        }
    }
}
