package com.samet.music.ui.gui;

import com.samet.music.service.security.KeycloakService;
import com.samet.music.service.security.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginViewController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private KeycloakService keycloakService = new KeycloakService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty");
            return;
        }

        // SessionManager kullanarak login işlemini gerçekleştir
        SessionManager sessionManager = SessionManager.getInstance();
        boolean success = sessionManager.login(username, password);

        if (success) {
            try {
                // Ana uygulamayı başlat
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                Parent root = loader.load();


                Scene scene = new Scene(root);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Music Library - " + sessionManager.getCurrentUser().getUsername());
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Error loading main application");
            }
        } else {
            messageLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading registration form");
        }
    }
}