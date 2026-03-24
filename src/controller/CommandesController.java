package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Commande;
import model.LigneCommande;
import service.DataService;

import java.time.format.DateTimeFormatter;

public class CommandesController {

    // --- TABLEAU DES COMMANDES (GAUCHE) ---
    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> colId;
    @FXML private TableColumn<Commande, String> colDate;
    @FXML private TableColumn<Commande, String> colClient;
    @FXML private TableColumn<Commande, String> colTable;
    @FXML private TableColumn<Commande, Double> colTotal;
    @FXML private TableColumn<Commande, String> colStatut;

    // --- DÉTAILS DE LA COMMANDE (DROITE) ---
    @FXML private ListView<String> detailsListView;
    @FXML private Label totalDetailLabel;
    @FXML private Button btnServie;
    @FXML private Button btnPayee;

    private DataService dataService;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();

        // 1. Configuration des Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Affichage du nom du client
        colClient.setCellValueFactory(cell -> {
            if (cell.getValue().getClient() != null) {
                return new SimpleStringProperty(cell.getValue().getClient().getNom());
            }
            return new SimpleStringProperty("Inconnu");
        });
        
        // Affichage "Table X" (avec sécurité si la table est null)
        colTable.setCellValueFactory(cell -> {
            if (cell.getValue().getTable() != null) {
                return new SimpleStringProperty("Table " + cell.getValue().getTable().getNumero());
            }
            return new SimpleStringProperty("À emporter");
        });
        
        // Formatage de la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDateHeure().format(formatter)));

        // 2. Charger les données
        refreshTable();

        // 3. Gestion du clic
        commandesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            afficherDetails(newVal);
        });
    }

    @FXML
    public void refreshTable() {
        commandesTable.setItems(FXCollections.observableArrayList(dataService.getCommandes()));
        
        // Réinitialiser l'affichage de droite
        detailsListView.getItems().clear();
        totalDetailLabel.setText("-");
        btnServie.setDisable(true);
        btnPayee.setDisable(true);
    }

    private void afficherDetails(Commande cmd) {
        if (cmd == null) return;

        detailsListView.getItems().clear();
        
        for (LigneCommande ligne : cmd.getLignesCommande()) {
            String detail = String.format("%dx %s (%.2f dh)", 
                ligne.getQuantite(), 
                ligne.getPlat().getNom(), 
                ligne.getSousTotal());
            detailsListView.getItems().add(detail);
        }

        totalDetailLabel.setText(String.format("%.2f dh", cmd.getTotal()));
        
        // Si c'est payé, on désactive les boutons
        boolean estPayee = "Payée".equals(cmd.getStatut());
        btnServie.setDisable(estPayee);
        btnPayee.setDisable(estPayee);
    }

    @FXML
    private void marquerCommeServie() {
        changerStatut("Servie");
    }

    @FXML
    private void marquerCommePayee() {
        changerStatut("Payée");
    }

    // --- C'EST ICI QUE LA LOGIQUE DE TABLE S'APPLIQUE ---
    private void changerStatut(String nouveauStatut) {
        Commande selected = commandesTable.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            // 1. Mettre à jour le statut de la commande (ex: Payée)
            dataService.updateStatutCommande(selected.getId(), nouveauStatut);

            // 2. GESTION AUTOMATIQUE DE LA TABLE
            if ("Payée".equals(nouveauStatut)) {
                // Si la commande est payée, on LIBÈRE la table
                if (selected.getTable() != null) {
                    dataService.updateTableStatut(selected.getTable().getId(), "Libre");
                    System.out.println("La Table " + selected.getTable().getNumero() + " est maintenant LIBRE.");
                }
            } 
            // Note : Si le statut est "Servie", on ne fait rien, la table reste "Occupée" (ce qui est correct).

            // 3. Rafraîchir l'affichage
            refreshTable(); 
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Commande marquée comme " + nouveauStatut);
            alert.showAndWait();
            
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une commande.");
            alert.showAndWait();
        }
    }
}