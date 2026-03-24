package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Client;
import service.DataService;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientsController {

    @FXML private TextField nomField;
    @FXML private TextField telField;
    @FXML private TextField emailField;

    @FXML private TableView<Client> clientsTable;

    @FXML private TableColumn<Client, String> nomColumn;
    @FXML private TableColumn<Client, String> telColumn;
    @FXML private TableColumn<Client, String> emailColumn;

    @FXML
    public void initialize() {
        // 1. Configuration des colonnes

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        telColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 2. Chargement initial
        loadClients();

        // 3. Remplissage automatique des champs quand on clique sur une ligne
        clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            remplirFormulaire(newVal);
        });
    }

    // --- CHARGEMENT AVEC FILTRE ---
    private void loadClients() {
        List<Client> tous = DataService.getInstance().getClients();
        
        // FILTRE : On exclut le client ID 1 (Passage)
        List<Client> vraisClients = tous.stream()
                .filter(c -> c.getId() != 1)
                .collect(Collectors.toList());

        clientsTable.setItems(FXCollections.observableArrayList(vraisClients));
    }

    // --- AJOUTER ---
    @FXML
    private void ajouterClient() {
        if (nomField.getText().isEmpty()) {
            afficherAlerte("Erreur", "Le nom est obligatoire.");
            return;
        }

        Client c = new Client(0, nomField.getText(), telField.getText(), emailField.getText());
        DataService.getInstance().addClient(c);
        
        viderFormulaire();
        loadClients();
    }

    // --- MODIFIER ---
    @FXML
    private void modifierClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Sélectionnez un client à modifier.");
            return;
        }

        // Mise à jour de l'objet
        selected.setNom(nomField.getText());
        selected.setTelephone(telField.getText());
        selected.setEmail(emailField.getText());

        DataService.getInstance().updateClient(selected);
        
        viderFormulaire();
        loadClients();
    }

    // --- SUPPRIMER ---
    @FXML
    private void supprimerClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Sélectionnez un client à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            DataService.getInstance().deleteClient(selected.getId());
            viderFormulaire();
            loadClients();
        }
    }

    // --- UTILITAIRES ---
    private void remplirFormulaire(Client c) {
        if (c != null) {
            nomField.setText(c.getNom());
            telField.setText(c.getTelephone());
            emailField.setText(c.getEmail());
        }
    }

    private void viderFormulaire() {
        nomField.clear();
        telField.clear();
        emailField.clear();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}