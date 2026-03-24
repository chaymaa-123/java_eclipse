package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Serveur;
import model.UserSession; // IMPORTANT : On importe la session
import service.DataService;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // On cherche le serveur dans la liste chargée depuis la BDD
        Serveur user = DataService.getInstance().getServeurs().stream()
                .filter(s -> s.getEmail().equals(email) && s.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            // --- C'EST ICI QUE TOUT SE JOUE ---
            // On enregistre l'utilisateur dans la session globale
            UserSession.getInstance().setServeur(user);
            System.out.println("Login réussi : " + user.getNom() + " est connecté.");

            // On notifie le contrôleur principal pour changer de vue
            if (mainController != null) {
                mainController.onLoginSuccess(user);
            }
            
            // Si c'est une fenêtre popup, on peut la fermer ici
            // ((Stage) emailField.getScene().getWindow()).close();

        } else {
            errorLabel.setText("Email ou mot de passe incorrect.");
        }
    }
}