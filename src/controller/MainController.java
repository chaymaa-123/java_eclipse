package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.Serveur;
import model.UserSession; 

public class MainController {

    @FXML private TabPane mainTabPane;
    @FXML private Button logoutButton; 

    // Onglets
    @FXML private Tab tablesTab;
    @FXML private Tab ordersTab;
    @FXML private Tab reservationsTab;
    @FXML private Tab menuTab;
    @FXML private Tab clientsTab;
    @FXML private Tab loginTab;
    @FXML private Tab suiviTab; 

    // Contrôleurs injectés (Le nom doit être fx:id + "Controller")
    @FXML private LoginController loginViewController;
    @FXML private OrderController ordersViewController; 
    @FXML private TablesController tablesViewController; // <-- C'est lui qu'on vient d'ajouter

    @FXML
    public void initialize() {
        if (loginViewController != null) {
            loginViewController.setMainController(this);
        }

        updateTabsForRole(null);

        // --- ECOUTEUR DE CHANGEMENT D'ONGLET (AUTO-REFRESH) ---
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            
            // 1. Si on va sur l'onglet TABLES -> On rafraîchit les cartes
            if (newTab == tablesTab && tablesViewController != null) {
                System.out.println("Onglet Tables actif -> Mise à jour visuelle...");
                tablesViewController.refresh(); 
            }

            // 2. Si on va sur l'onglet COMMANDER -> On rafraîchit la liste des tables libres
            if (newTab == ordersTab && ordersViewController != null) {
                ordersViewController.refresh();
            }
        });
    }

    public void onLoginSuccess(Serveur user) {
        updateTabsForRole(user.getRole());

        // Refresh des vues importantes
        if (ordersViewController != null) ordersViewController.refresh(); 
        if (tablesViewController != null) tablesViewController.refresh(); 

        // Redirection
        if (tablesTab != null) {
            mainTabPane.getSelectionModel().select(tablesTab);  
        }
    }

    @FXML 
    public void logout() {
        UserSession.getInstance().logout();
        updateTabsForRole(null);

        if (ordersViewController != null) ordersViewController.refresh();
        mainTabPane.getSelectionModel().select(ordersTab);
    }

    private void updateTabsForRole(String role) {
        mainTabPane.getTabs().clear();
        mainTabPane.getTabs().add(ordersTab);

        if (role == null) {
            if (loginTab != null) mainTabPane.getTabs().add(loginTab);
            if (logoutButton != null) logoutButton.setVisible(false);
        } else {
            if (logoutButton != null) logoutButton.setVisible(true);
            if (tablesTab != null) mainTabPane.getTabs().add(tablesTab);
            if (suiviTab != null) mainTabPane.getTabs().add(suiviTab);
            if (reservationsTab != null) mainTabPane.getTabs().add(reservationsTab);

            if ("Gérant".equalsIgnoreCase(role)) {
                if (menuTab != null) mainTabPane.getTabs().add(menuTab);
                if (clientsTab != null) mainTabPane.getTabs().add(clientsTab);
            }
        }
    }
}