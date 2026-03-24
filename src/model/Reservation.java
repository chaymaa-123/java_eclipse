package model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private LocalDateTime dateHeure;
    private String jours; // e.g., "Lundi", "Mardi" - maybe redundant with dateHeure but in UML
    private int nombreDePersonnes;
    private Client client;
    private TableRestaurant table;

    public Reservation(int id, LocalDateTime dateHeure, String jours, int nombreDePersonnes, Client client,
            TableRestaurant table) {
        this.id = id;
        this.dateHeure = dateHeure;
        this.jours = jours;
        this.nombreDePersonnes = nombreDePersonnes;
        this.client = client;
        this.table = table;
    }

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

    public String getJours() {
        return jours;
    }

    public void setJours(String jours) {
        this.jours = jours;
    }

    public int getNombreDePersonnes() {
        return nombreDePersonnes;
    }

    public void setNombreDePersonnes(int nombreDePersonnes) {
        this.nombreDePersonnes = nombreDePersonnes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public TableRestaurant getTable() {
        return table;
    }

    public void setTable(TableRestaurant table) {
        this.table = table;
    }
}
