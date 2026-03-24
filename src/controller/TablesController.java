package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import model.TableRestaurant;
import service.DataService;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

public class TablesController {

    @FXML private FlowPane tablesContainer;
    @FXML private TextField tfNumero;
    @FXML private TextField tfCapacite;

    @FXML
    public void initialize() {
        refresh(); // Charge les tables au démarrage
    }

    // --- MÉTHODE PUBLIQUE POUR RAFRAÎCHIR L'AFFICHAGE ---
    public void refresh() {
        tablesContainer.getChildren().clear();
        List<TableRestaurant> tables = DataService.getInstance().getTables();

        for (TableRestaurant table : tables) {
            VBox tableCard = createTableCard(table);
            tablesContainer.getChildren().add(tableCard);
        }
    }

    // --- AJOUTER UNE NOUVELLE TABLE ---
    @FXML
    private void ajouterTable() {
        try {
            if (tfNumero.getText().isEmpty() || tfCapacite.getText().isEmpty()) return;

            int num = Integer.parseInt(tfNumero.getText());
            int cap = Integer.parseInt(tfCapacite.getText());

            TableRestaurant t = new TableRestaurant(0, num, cap, "Libre");
            DataService.getInstance().addTable(t);
            
            // On vide le formulaire et on rafraîchit
            tfNumero.clear();
            tfCapacite.clear();
            refresh();
            
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Numéro et capacité doivent être des chiffres !");
            alert.showAndWait();
        }
    }

    // --- CRÉATION VISUELLE D'UNE CARTE TABLE ---
    private VBox createTableCard(TableRestaurant table) {
        VBox card = new VBox(5);
        
        // Couleur selon le statut
        String color = "#2ecc71"; // Vert (Libre)
        if ("Occupée".equalsIgnoreCase(table.getStatut())) color = "#e74c3c"; // Rouge
        if ("Réservée".equalsIgnoreCase(table.getStatut())) color = "#f39c12"; // Orange

        card.setStyle(
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 5; " +
                "-fx-padding: 10; -fx-background-color: white; -fx-min-width: 140; -fx-min-height: 100; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );
        card.setAlignment(Pos.CENTER);

        Label lblNum = new Label("Table " + table.getNumero());
        lblNum.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblCap = new Label(table.getCapacite() + " pers.");
        
        Label lblStatut = new Label(table.getStatut());
        lblStatut.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Button btnAction = new Button("Changer État");
        btnAction.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
        btnAction.setOnAction(e -> changerStatut(table));

        card.getChildren().addAll(lblNum, lblCap, lblStatut, btnAction);
        return card;
    }

    // --- MODIFIER LE STATUT VIA UNE POPUP ---
    private void changerStatut(TableRestaurant table) {
        List<String> choix = Arrays.asList("Libre", "Occupée", "Réservée");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(table.getStatut(), choix);
        dialog.setTitle("État Table");
        dialog.setHeaderText("Statut de la Table " + table.getNumero());
        dialog.setContentText("Nouveau statut :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauStatut -> {
            DataService.getInstance().updateTableStatut(table.getId(), nouveauStatut);
            refresh(); // Recharge l'affichage immédiatement
        });
    }
}