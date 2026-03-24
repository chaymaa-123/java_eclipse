package service;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class DataService {
    private static DataService instance;

    // Constructeur privé
    private DataService() {
        // Test de connexion au lancement
        try {
            DatabaseConnection.getConnection();
            System.out.println("--- Connexion Base de Données OK ---");
        } catch (SQLException e) {
            System.err.println("!!! Erreur Connexion BDD !!! : " + e.getMessage());
        }
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    // ============================================================
    // PARTIE 1 : LECTURE DES DONNÉES (READ)
    // ============================================================

    public List<Serveur> getServeurs() {
        List<Serveur> list = new ArrayList<>();
        String sql = "SELECT * FROM serveur";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Serveur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("role"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getString("password")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Client> getClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Client(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("telephone"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<TableRestaurant> getTables() {
        List<TableRestaurant> list = new ArrayList<>();
        String sql = "SELECT * FROM table_restaurant";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new TableRestaurant(
                    rs.getInt("id"),
                    rs.getInt("numero"),
                    rs.getInt("capacite"),
                    rs.getString("statut")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Plat> getMenu() {
        List<Plat> list = new ArrayList<>();
        String sql = "SELECT * FROM plat";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Plat(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("categorie"),
                    rs.getDouble("prix"),
                    rs.getBoolean("disponible")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Commande> getCommandes() {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT * FROM commande ORDER BY date_heure DESC"; // Tri par date

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client c = getClientById(rs.getInt("client_id"));
                Serveur s = getServeurById(rs.getInt("serveur_id"));
                TableRestaurant t = getTableById(rs.getInt("table_id"));
                
                Timestamp ts = rs.getTimestamp("date_heure");
                LocalDateTime dt = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                Commande cmd = new Commande(rs.getInt("id"), dt, c, s, t);
                cmd.setStatut(rs.getString("statut"));
                
                // Récupération des lignes de commande (détails)
                List<LigneCommande> lignes = getLignesByCommandeId(cmd.getId());
                for(LigneCommande lc : lignes) {
                    cmd.ajouterLigne(lc); 
                }

                list.add(cmd);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> getReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client c = getClientById(rs.getInt("client_id"));
                TableRestaurant t = getTableById(rs.getInt("table_id"));
                Timestamp ts = rs.getTimestamp("date_heure");
                LocalDateTime dt = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                list.add(new Reservation(
                    rs.getInt("id"),
                    dt,
                    rs.getString("jours"),
                    rs.getInt("nombre_de_personnes"),
                    c, t
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ============================================================
    // PARTIE 2 : AJOUT DES DONNÉES (CREATE)
    // ============================================================

    public void addClient(Client client) {
        String sql = "INSERT INTO client (nom, telephone, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getNom());
            pstmt.setString(2, client.getTelephone());
            pstmt.setString(3, client.getEmail());
            pstmt.executeUpdate();
            System.out.println("Client ajouté en BDD");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Transaction complexe : Commande + Lignes
    public void addCommande(Commande commande) {
        String sqlCmd = "INSERT INTO commande (date_heure, statut, client_id, serveur_id, table_id, total) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlLigne = "INSERT INTO ligne_commande (commande_id, plat_id, quantite, prix_unit) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Démarrer Transaction

            try {
                // 1. Insert Commande
                PreparedStatement pstmt = conn.prepareStatement(sqlCmd, Statement.RETURN_GENERATED_KEYS);
                pstmt.setTimestamp(1, Timestamp.valueOf(commande.getDateHeure()));
                pstmt.setString(2, commande.getStatut());
                pstmt.setObject(3, (commande.getClient() != null) ? commande.getClient().getId() : null);
                pstmt.setObject(4, (commande.getServeur() != null) ? commande.getServeur().getId() : null);
                pstmt.setObject(5, (commande.getTable() != null) ? commande.getTable().getId() : null);
                pstmt.setDouble(6, commande.getTotal());
                pstmt.executeUpdate();

                // Récupérer ID généré
                ResultSet rsKeys = pstmt.getGeneratedKeys();
                int commandeId = 0;
                if (rsKeys.next()) {
                    commandeId = rsKeys.getInt(1);
                    commande.setId(commandeId);
                }

                // 2. Insert Lignes
                PreparedStatement pstmtLigne = conn.prepareStatement(sqlLigne);
                for (LigneCommande ligne : commande.getLignesCommande()) {
                    pstmtLigne.setInt(1, commandeId);
                    pstmtLigne.setInt(2, ligne.getPlat().getId());
                    pstmtLigne.setInt(3, ligne.getQuantite());
                    pstmtLigne.setDouble(4, ligne.getPrixUnit());
                    pstmtLigne.addBatch();
                }
                pstmtLigne.executeBatch();

                conn.commit(); // Valider
                System.out.println("Commande sauvegardée en BDD");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addReservation(Reservation res) {
        String sql = "INSERT INTO reservation (date_heure, jours, nombre_de_personnes, client_id, table_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(res.getDateHeure()));
            pstmt.setString(2, res.getJours());
            pstmt.setInt(3, res.getNombreDePersonnes());
            pstmt.setInt(4, res.getClient().getId());
            pstmt.setInt(5, res.getTable().getId());
            pstmt.executeUpdate();
            System.out.println("Réservation ajoutée en BDD");
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // Ajout d'une table (Gestion Salle)
    public void addTable(TableRestaurant table) {
        String sql = "INSERT INTO table_restaurant (numero, capacite, statut) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, table.getNumero());
            pstmt.setInt(2, table.getCapacite());
            pstmt.setString(3, "Libre");
            pstmt.executeUpdate();
            System.out.println("Table " + table.getNumero() + " ajoutée !");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Ajout d'un plat (Gestion Menu)
    public void addPlat(Plat plat) {
        String sql = "INSERT INTO plat (nom, categorie, prix, disponible) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plat.getNom());
            pstmt.setString(2, plat.getCategorie());
            pstmt.setDouble(3, plat.getPrix());
            pstmt.setBoolean(4, plat.isDisponible());
            pstmt.executeUpdate();
            System.out.println("Plat ajouté : " + plat.getNom());
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ============================================================
    // PARTIE 3 : MISE A JOUR (UPDATE)
    // ============================================================

    public void updateStatutCommande(int commandeId, String nouveauStatut) {
        String sql = "UPDATE commande SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, commandeId);
            pstmt.executeUpdate();
            System.out.println("Commande " + commandeId + " mise à jour : " + nouveauStatut);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateTableStatut(int id, String nouveauStatut) {
        String sql = "UPDATE table_restaurant SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Statut mis à jour pour la table ID " + id);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePlat(Plat plat) {
        String sql = "UPDATE plat SET nom = ?, categorie = ?, prix = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plat.getNom());
            pstmt.setString(2, plat.getCategorie());
            pstmt.setDouble(3, plat.getPrix());
            pstmt.setInt(4, plat.getId());
            pstmt.executeUpdate();
            System.out.println("Plat ID " + plat.getId() + " mis à jour.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateReservation(Reservation res) {
        String sql = "UPDATE reservation SET date_heure = ?, nombre_de_personnes = ?, client_id = ?, table_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(res.getDateHeure()));
            pstmt.setInt(2, res.getNombreDePersonnes());
            pstmt.setInt(3, res.getClient().getId());
            pstmt.setInt(4, res.getTable().getId());
            pstmt.setInt(5, res.getId());
            
            pstmt.executeUpdate();
            System.out.println("Réservation modifiée ID : " + res.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateClient(Client client) {
        String sql = "UPDATE client SET nom = ?, telephone = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getNom());
            pstmt.setString(2, client.getTelephone());
            pstmt.setString(3, client.getEmail());
            pstmt.setInt(4, client.getId());
            
            pstmt.executeUpdate();
            System.out.println("Client mis à jour ID : " + client.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // PARTIE 4 : SUPPRESSION (DELETE)
    // ============================================================

    public void deletePlat(int id) {
        String sql = "DELETE FROM plat WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Plat supprimé ID : " + id);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteReservation(int id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Réservation supprimée ID : " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClient(int id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Client supprimé ID : " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // PARTIE 5 : HELPERS (Récupération par ID)
    // ============================================================

    private Client getClientById(int id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Client(rs.getInt("id"), rs.getString("nom"), rs.getString("telephone"), rs.getString("email"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null; 
    }

    private Serveur getServeurById(int id) {
        String sql = "SELECT * FROM serveur WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Serveur(rs.getInt("id"), rs.getString("nom"), rs.getString("role"), rs.getString("telephone"), rs.getString("email"), rs.getString("password"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private TableRestaurant getTableById(int id) {
        String sql = "SELECT * FROM table_restaurant WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new TableRestaurant(rs.getInt("id"), rs.getInt("numero"), rs.getInt("capacite"), rs.getString("statut"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Plat getPlatById(int id) {
        String sql = "SELECT * FROM plat WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Plat(rs.getInt("id"), rs.getString("nom"), rs.getString("categorie"), rs.getDouble("prix"), rs.getBoolean("disponible"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private List<LigneCommande> getLignesByCommandeId(int commandeId) {
        List<LigneCommande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_commande WHERE commande_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commandeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Plat p = getPlatById(rs.getInt("plat_id"));
                if (p != null) {
                    LigneCommande lc = new LigneCommande(p, rs.getInt("quantite"));
                    lignes.add(lc);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lignes;
    }
}