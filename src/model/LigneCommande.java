package model;

public class LigneCommande {
    private Plat plat;
    private int quantite;
    private double prixUnit; // Prix au moment de la commande

    public LigneCommande(Plat plat, int quantite) {
        this.plat = plat;
        this.quantite = quantite;
        this.prixUnit = plat.getPrix();
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnit() {
        return prixUnit;
    }

    public void setPrixUnit(double prixUnit) {
        this.prixUnit = prixUnit;
    }

    public double getSousTotal() {
        return quantite * prixUnit;
    }

    @Override
    public String toString() {
        return plat.getNom() + " x" + quantite;
    }
}
