package com.samet.music.ui.gui;

import com.samet.music.service.security.KeycloakService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterViewController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private KeycloakService keycloakService = new KeycloakService();

    @FXML
    private void handleRegister() {
        if (!validateForm()) {
            return;
        }

        String username = usernameField.getText().trim();
        // Basit kayıt işlemi - gerçek bir kayıt yapmaz, sadece başarılı görünür
        messageLabel.setText("Registration successful! You can now login.");

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(this::navigateToLogin);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean validateForm() {
        if (usernameField.getText().trim().isEmpty()) {
            messageLabel.setText("Username cannot be empty");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            messageLabel.setText("Email cannot be empty");
            return false;
        }

        if (passwordField.getText().trim().isEmpty()) {
            messageLabel.setText("Password cannot be empty");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            messageLabel.setText("Passwords do not match");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error navigating to login screen");
        }
    }
}