package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.*;
import service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Important pour filtrer

public class OrderController {

    @FXML private TextField clientNameField;
    @FXML private ComboBox<Plat> platComboBox;
    @FXML private Spinner<Integer> quantiteSpinner;
    @FXML private ListView<String> ligneCommandeListView;
    @FXML private Label totalLabel;

    // Éléments spécifiques au Mode Serveur (Staff)
    @FXML private ComboBox<TableRestaurant> tableComboBox;
    @FXML private Label fixedTableLabel;
    @FXML private HBox staffControlsBox;

    private ObservableList<LigneCommande> lignesCommandeCurrent;
    private double totalCurrent = 0.0;
    private boolean isStaffMode = false;

    // IDs par défaut
    private final int DEFAULT_TABLE_ID = 1;
    private final int DEFAULT_CLIENT_ID = 1;
    private final int DEFAULT_SERVER_ID = 1; 

    @FXML
    public void initialize() {
        DataService data = DataService.getInstance();

        platComboBox.setItems(FXCollections.observableArrayList(data.getMenu()));
        
        // On ne charge pas les tables ici, mais dans refresh()
        
        lignesCommandeCurrent = FXCollections.observableArrayList();
        quantiteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Démarrage
        refresh(); 
    }
    

    // --- MISE A JOUR DES DONNÉES ---
    public void refresh() {
        if (UserSession.getInstance().isStaffLoggedIn()) {
            setStaffMode(true);
            // Si on est Staff, on charge UNIQUEMENT les tables libres
            chargerTablesLibresUniquement(); 
        } else {
            setStaffMode(false);
        }
    }

    // Méthode de filtrage (Libre uniquement)
    private void chargerTablesLibresUniquement() {
        List<TableRestaurant> toutesLesTables = DataService.getInstance().getTables();
        
        // On garde seulement celles qui sont "Libre"
        List<TableRestaurant> tablesLibres = toutesLesTables.stream()
                .filter(t -> "Libre".equalsIgnoreCase(t.getStatut()))
                .collect(Collectors.toList());

        tableComboBox.setItems(FXCollections.observableArrayList(tablesLibres));
        
        if (tablesLibres.isEmpty()) {
            tableComboBox.setPromptText("Aucune table libre !");
        } else {
            tableComboBox.setPromptText("-- Choisir Table --");
        }
    }

    public void setStaffMode(boolean isStaff) {
        this.isStaffMode = isStaff;

        if (staffControlsBox != null) {
            staffControlsBox.setVisible(isStaff);
            staffControlsBox.setManaged(isStaff);
        }
        if (fixedTableLabel != null) {
            fixedTableLabel.setVisible(!isStaff);
            fixedTableLabel.setManaged(!isStaff);
        }
    }

    @FXML
    private void ajouterPlat() {
        Plat plat = platComboBox.getValue();
        Integer qte = quantiteSpinner.getValue();

        if (plat != null && qte != null) {
            LigneCommande ligne = new LigneCommande(plat, qte);
            lignesCommandeCurrent.add(ligne);
            ligneCommandeListView.getItems().add(String.format("%s (x%d) - %.2f dh", plat.getNom(), qte, ligne.getSousTotal()));
            totalCurrent += ligne.getSousTotal();
            totalLabel.setText(String.format("%.2f dh", totalCurrent));
        } else {
            afficherAlerte(Alert.AlertType.WARNING, "Veuillez sélectionner un plat.");
        }
    }

    @FXML
    private void validerCommande() {
        String clientName = clientNameField.getText();

        if (clientName == null || clientName.trim().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Veuillez entrer un nom.");
            return;
        }
        if (lignesCommandeCurrent.isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Votre panier est vide.");
            return;
        }

        DataService data = DataService.getInstance();
        Client client = new Client(DEFAULT_CLIENT_ID, clientName, "0000", "temp@mail.com");

        TableRestaurant table = null;
        if (isStaffMode) {
            table = tableComboBox.getValue();
            if (table == null) {
                afficherAlerte(Alert.AlertType.WARNING, "En tant que serveur, vous devez choisir une table !");
                return;
            }
        } else {
            table = data.getTables().stream().filter(t -> t.getId() == DEFAULT_TABLE_ID).findFirst().orElse(null);
        }

        if (table == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur configuration : Table introuvable.");
            return;
        }

        Serveur serveur = null;
        if (UserSession.getInstance().isStaffLoggedIn()) {
            serveur = UserSession.getInstance().getServeur();
        } else {
            serveur = data.getServeurs().stream().filter(s -> s.getId() == DEFAULT_SERVER_ID).findFirst().orElse(null);
        }

        try {
            Commande commande = new Commande(0, LocalDateTime.now(), client, serveur, table);
            for (LigneCommande ligne : lignesCommandeCurrent) {
                commande.ajouterLigne(ligne);
            }
            data.addCommande(commande);
            
            // OPTIONNEL : Passer la table en "Occupée" immédiatement
            if (isStaffMode) {
                data.updateTableStatut(table.getId(), "Occupée");
                chargerTablesLibresUniquement(); // Mise à jour immédiate de la liste
            }
            
            afficherAlerte(Alert.AlertType.INFORMATION, "Commande validée pour la table " + table.getNumero() + " !");
            resetForm();
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void annulerCommande() {
        resetForm();
    }

    private void resetForm() {
        clientNameField.clear();
        platComboBox.getSelectionModel().clearSelection();
        quantiteSpinner.getValueFactory().setValue(1);
        ligneCommandeListView.getItems().clear();
        lignesCommandeCurrent.clear();
        totalCurrent = 0.0;
        totalLabel.setText("0.00 dh");
        if(tableComboBox != null) tableComboBox.getSelectionModel().clearSelection();
    }

    private void afficherAlerte(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}