package com.samet.music.ui.gui;

import com.samet.music.monitoring.MetricsCollector;
import com.samet.music.service.security.KeycloakService;
import com.samet.music.service.security.SessionManager;
import io.prometheus.client.Histogram;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginViewController {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private KeycloakService keycloakService = new KeycloakService();

    @FXML
    private void handleLogin() {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("login");
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            logger.info("Login attempt from UI for user: {}", username);

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username and password cannot be empty");
                logger.warn("Login failed: empty username or password");
                return;
            }

            // SessionManager kullanarak login işlemini gerçekleştir
            SessionManager sessionManager = SessionManager.getInstance();
            boolean success = sessionManager.login(username, password);

            if (success) {
                try {
                    logger.info("User logged in successfully: {}", username);

                    // Ana uygulamayı başlat
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                    Parent root = loader.load();

                    Scene scene = new Scene(root);
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Music Library - " + sessionManager.getCurrentUser().getUsername());
                    stage.show();
                } catch (Exception e) {
                    logger.error("Error loading main application: {}", e.getMessage(), e);
                    messageLabel.setText("Error loading main application");
                }
            } else {
                logger.warn("Login failed: invalid credentials for user {}", username);
                messageLabel.setText("Invalid username or password");
            }
        } finally {
            timer.observeDuration();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            logger.info("Navigating to registration screen");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading registration form: {}", e.getMessage(), e);
            messageLabel.setText("Error loading registration form");
        }
    }
}