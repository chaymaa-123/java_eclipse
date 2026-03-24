package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import model.*;
import service.DataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationController {

    // --- CHAMPS ---
    @FXML private ComboBox<Client> clientComboBox;
    @FXML private ComboBox<TableRestaurant> tableComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField nbPersonnesField;

    // --- TABLEAU ---
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> dateColumn;
    @FXML private TableColumn<Reservation, String> clientColumn;
    @FXML private TableColumn<Reservation, Integer> nbPersColumn;
    @FXML private TableColumn<Reservation, String> tableColumn;

    @FXML
    public void initialize() {
        // 1. Configuration des Colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getDateHeure().format(formatter)));
            
        clientColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getClient().getNom()));
            
        nbPersColumn.setCellValueFactory(new PropertyValueFactory<>("nombreDePersonnes"));
        
        tableColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTable() != null ? "Table " + cell.getValue().getTable().getNumero() : "N/A"));

        // 2. MAGIE ICI : Auto-refresh des listes quand on clique dessus !
        // Dès que tu ouvres la liste Client, elle se recharge
        clientComboBox.setOnShowing(e -> chargerClients());
        
        // Dès que tu ouvres la liste Table, elle recharge les tables libres
        tableComboBox.setOnShowing(e -> chargerTablesLibres());

        // 3. Chargement initial du tableau
        refreshTableau();

        // 4. Sélection pour modification
        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            remplirFormulaire(newVal);
        });
        
        // On charge une première fois les listes pour qu'elles ne soient pas vides visuellement
        chargerClients();
        chargerTablesLibres();
    }

    // --- LOGIQUE DE CHARGEMENT ---

    private void chargerClients() {
        // On garde la sélection actuelle s'il y en a une
        Client selectionActuelle = clientComboBox.getValue();
        
        List<Client> tous = DataService.getInstance().getClients();
        // Filtre : Pas le client "Passage"
        List<Client> vraisClients = tous.stream().filter(c -> c.getId() != 1).collect(Collectors.toList());
        
        clientComboBox.setItems(FXCollections.observableArrayList(vraisClients));
        
        // Configuration de l'affichage (Juste le Nom)
        clientComboBox.setCellFactory(p -> new ListCell<Client>() {
            @Override protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        clientComboBox.setButtonCell(clientComboBox.getCellFactory().call(null));
        
        // Restaure la sélection si elle existe toujours
        if (selectionActuelle != null && clientComboBox.getItems().contains(selectionActuelle)) {
            clientComboBox.setValue(selectionActuelle);
        }
    }

    private void chargerTablesLibres() {
        TableRestaurant selectionActuelle = tableComboBox.getValue();
        
        List<TableRestaurant> tables = DataService.getInstance().getTables();
        // Filtre : Tables Libres uniquement
        List<TableRestaurant> libres = tables.stream()
                .filter(t -> "Libre".equalsIgnoreCase(t.getStatut()))
                .collect(Collectors.toList());
        
        tableComboBox.setItems(FXCollections.observableArrayList(libres));
        
        // Configuration affichage
        tableComboBox.setCellFactory(p -> new ListCell<TableRestaurant>() {
            @Override protected void updateItem(TableRestaurant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Table " + item.getNumero() + " (" + item.getCapacite() + "p)");
            }
        });
        tableComboBox.setButtonCell(tableComboBox.getCellFactory().call(null));
        
        if (selectionActuelle != null && tableComboBox.getItems().contains(selectionActuelle)) {
            tableComboBox.setValue(selectionActuelle);
        }
    }

    private void refreshTableau() {
        reservationTable.setItems(FXCollections.observableArrayList(DataService.getInstance().getReservations()));
    }

    // --- ACTIONS (CRUD) ---

    @FXML
    private void ajouterReservation() {
        try {
            Reservation res = construireReservation();
            if (res != null) {
                DataService.getInstance().addReservation(res);
                
                // On marque la table comme réservée
                if (res.getTable() != null) {
                    DataService.getInstance().updateTableStatut(res.getTable().getId(), "Réservée");
                }
                
                viderFormulaire();
                refreshTableau(); // Le tableau se met à jour immédiatement
            }
        } catch (Exception e) { afficherAlerte("Erreur", "Vérifiez les données (Heure HH:mm)."); }
    }

    @FXML
    private void modifierReservation() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherAlerte("Info", "Sélectionnez une ligne."); return; }
        
        try {
            Reservation temp = construireReservation();
            if (temp != null) {
                // Si la table change, on libère l'ancienne et on réserve la nouvelle
                if (selected.getTable().getId() != temp.getTable().getId()) {
                    DataService.getInstance().updateTableStatut(selected.getTable().getId(), "Libre");
                    DataService.getInstance().updateTableStatut(temp.getTable().getId(), "Réservée");
                }

                selected.setClient(temp.getClient());
                selected.setTable(temp.getTable());
                selected.setDateHeure(temp.getDateHeure());
                selected.setNombreDePersonnes(temp.getNombreDePersonnes());
                
                DataService.getInstance().updateReservation(selected);
                viderFormulaire();
                refreshTableau();
            }
        } catch (Exception e) { afficherAlerte("Erreur", "Modification impossible."); }
    }

    @FXML
    private void supprimerReservation() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherAlerte("Info", "Sélectionnez une ligne."); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            DataService.getInstance().deleteReservation(selected.getId());
            
            // On libère la table
            if (selected.getTable() != null) {
                DataService.getInstance().updateTableStatut(selected.getTable().getId(), "Libre");
            }
            
            viderFormulaire();
            refreshTableau();
        }
    }

    // --- UTILITAIRES ---

    private Reservation construireReservation() {
        Client c = clientComboBox.getValue();
        TableRestaurant t = tableComboBox.getValue();
        LocalDate d = datePicker.getValue();
        String h = heureField.getText();
        String nb = nbPersonnesField.getText();

        if (c == null || t == null || d == null || h.isEmpty() || nb.isEmpty()) {
            afficherAlerte("Attention", "Remplissez tout.");
            return null;
        }

        LocalTime time = LocalTime.parse(h);
        return new Reservation(0, LocalDateTime.of(d, time), d.getDayOfWeek().toString(), Integer.parseInt(nb), c, t);
    }

    private void remplirFormulaire(Reservation res) {
        if (res == null) return;
        
        // On remet le client
        clientComboBox.getItems().stream()
            .filter(c -> c.getId() == res.getClient().getId())
            .findFirst().ifPresent(clientComboBox::setValue);
        
        // On remet la table (même si elle est occupée, on l'ajoute temporairement pour l'afficher)
        TableRestaurant tRes = res.getTable();
        boolean present = tableComboBox.getItems().stream().anyMatch(t -> t.getId() == tRes.getId());
        if (!present) { tableComboBox.getItems().add(tRes); }
        tableComboBox.setValue(tRes);

        datePicker.setValue(res.getDateHeure().toLocalDate());
        heureField.setText(res.getDateHeure().toLocalTime().toString());
        nbPersonnesField.setText(String.valueOf(res.getNombreDePersonnes()));
    }

    private void viderFormulaire() {
        clientComboBox.getSelectionModel().clearSelection();
        tableComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        heureField.clear();
        nbPersonnesField.clear();
    }

    private void afficherAlerte(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
}