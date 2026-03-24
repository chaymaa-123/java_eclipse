package model;

public class UserSession {
    private static UserSession instance;
    private Serveur serveurConnecte; // Stocke le serveur/gérant s'il est connecté

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setServeur(Serveur serveur) {
        this.serveurConnecte = serveur;
    }

    public Serveur getServeur() {
        return serveurConnecte;
    }

    // Est-ce que quelqu'un du staff est connecté ?
    public boolean isStaffLoggedIn() {
        return serveurConnecte != null;
    }
    
    public void logout() {
        this.serveurConnecte = null;
    }
}