package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Plat;
import service.DataService;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MenuController {

    @FXML private TextField nomField;
    @FXML private TextField categorieField;
    @FXML private TextField prixField;
    @FXML private CheckBox dispoCheckbox;

    @FXML private TableView<Plat> menuTable;
    @FXML private TableColumn<Plat, Integer> idColumn;
    @FXML private TableColumn<Plat, String> nomColumn;
    @FXML private TableColumn<Plat, String> categorieColumn;
    @FXML private TableColumn<Plat, Double> prixColumn;
    @FXML private TableColumn<Plat, Boolean> dispoColumn;

    @FXML
    public void initialize() {
        // Configuration des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dispoColumn.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        // Chargement des données
        loadMenu();

        // Remplir les champs quand on clique sur une ligne
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomField.setText(newSelection.getNom());
                categorieField.setText(newSelection.getCategorie());
                prixField.setText(String.valueOf(newSelection.getPrix()));
                dispoCheckbox.setSelected(newSelection.isDisponible());
            }
        });
    }

    private void loadMenu() {
        menuTable.setItems(FXCollections.observableArrayList(DataService.getInstance().getMenu()));
    }

    @FXML
    private void ajouterPlat() {
        try {
            String nom = nomField.getText();
            String cat = categorieField.getText();
            double prix = Double.parseDouble(prixField.getText().replace(",", "."));
            boolean dispo = dispoCheckbox.isSelected();

            Plat p = new Plat(0, nom, cat, prix, dispo);
            DataService.getInstance().addPlat(p);
            
            viderChamps();
            loadMenu();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Vérifiez les champs (Prix doit être un nombre).");
        }
    }

    @FXML
    private void modifierPlat() {
        Plat selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Sélectionnez un plat à modifier.");
            return;
        }

        try {
            selected.setNom(nomField.getText());
            selected.setCategorie(categorieField.getText());
            selected.setPrix(Double.parseDouble(prixField.getText().replace(",", ".")));
            selected.setDisponible(dispoCheckbox.isSelected());

            DataService.getInstance().updatePlat(selected);
            viderChamps();
            loadMenu();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Données invalides.");
        }
    }

    @FXML
    private void supprimerPlat() {
        Plat selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Sélectionnez un plat à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            DataService.getInstance().deletePlat(selected.getId());
            viderChamps();
            loadMenu();
        }
    }

    @FXML
    private void exporterAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le Menu (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(menuTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Utilisation du flux (Stream) pour l'exportation
                menuTable.getItems().stream()
                    .map(p -> String.format("%s;%s;%.2f;%b", 
                         p.getNom(), p.getCategorie(), p.getPrix(), p.isDisponible()))
                    .forEach(writer::println);
                
                afficherAlerte("Succès", "Menu exporté correctement.");
            } catch (Exception e) {
                afficherAlerte("Erreur", "Exportation impossible : " + e.getMessage());
            }
        }
    }

    @FXML
    private void importerAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un Menu (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(menuTable.getScene().getWindow());

        if (file != null) {
            try (Stream<String> lines = Files.lines(file.toPath())) {
                // Utilisation du flux (Stream) pour l'importation
                List<Plat> importes = lines
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length >= 4)
                    .map(parts -> new Plat(0, parts[0], parts[1], 
                         Double.parseDouble(parts[2].replace(",", ".")), 
                         Boolean.parseBoolean(parts[3])))
                    .collect(Collectors.toList());

                importes.forEach(p -> DataService.getInstance().addPlat(p));
                loadMenu();
                afficherAlerte("Succès", importes.size() + " plats ajoutés !");
            } catch (Exception e) {
                afficherAlerte("Erreur", "Importation échouée : " + e.getMessage());
            }
        }
    }

    private void viderChamps() {
        nomField.clear();
        categorieField.clear();
        prixField.clear();
        dispoCheckbox.setSelected(true);
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}