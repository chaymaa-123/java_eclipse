package model;

public class Serveur {
    private int id;
    private String nom;
    private String role;
    private String telephone;
    private String email;
    private String password;

    public Serveur(int id, String nom, String role, String telephone, String email ,String password) {
        this.id = id;
        this.nom = nom;
        this.role = role;
        this.telephone = telephone;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return this.password;
    }
}
