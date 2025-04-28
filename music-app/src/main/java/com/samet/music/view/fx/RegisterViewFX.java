package com.samet.music.view.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
import com.samet.music.controller.UserFXController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the registration view
 */
public class RegisterViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(RegisterViewFX.class);
    private final Stage stage;
    private final BorderPane root;
    
    // Controllers can be either type
    private UserController userController;
    private UserFXController userFXController;

    // Constructor for UserController
    public RegisterViewFX(Stage stage, UserController userController) {
        this.stage = stage;
        this.userController = userController;
        this.userFXController = null;
        this.root = createContent();
    }
    
    // Constructor for UserFXController
    public RegisterViewFX(Stage stage, UserFXController userFXController) {
        this.stage = stage;
        this.userController = null;
        this.userFXController = userFXController;
        this.root = createContent();
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    private BorderPane createContent() {
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("background");
        
        // Header
        Text headerText = new Text("Music Library Organizer");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 28));
        headerText.getStyleClass().add("header-text");
        StackPane header = new StackPane(headerText);
        header.setPadding(new Insets(20));
        header.getStyleClass().add("header");
        borderPane.setTop(header);
        
        // Registration form
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25));
        
        Text registerTitle = new Text("Create New Account");
        registerTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        gridPane.add(registerTitle, 0, 0, 2, 1);
        
        Label usernameLabel = new Label("Username:");
        gridPane.add(usernameLabel, 0, 1);
        
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(250);
        gridPane.add(usernameField, 1, 1);
        
        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 2);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(250);
        gridPane.add(passwordField, 1, 2);
        
        Label confirmPasswordLabel = new Label("Confirm Password:");
        gridPane.add(confirmPasswordLabel, 0, 3);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPrefWidth(250);
        gridPane.add(confirmPasswordField, 1, 3);
        
        Label emailLabel = new Label("Email (optional):");
        gridPane.add(emailLabel, 0, 4);
        
        TextField emailField = new TextField();
        emailField.setPrefWidth(250);
        gridPane.add(emailField, 1, 4);
        
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("register-button");
        
        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("login-button");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(backButton, registerButton);
        gridPane.add(buttonBox, 1, 6);
        
        // Add some space
        gridPane.add(new Label(""), 0, 5);
        
        // Event handlers
        registerButton.setOnAction(e -> handleRegister(
                usernameField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                emailField.getText()));
        
        backButton.setOnAction(e -> returnToLogin());
        
        VBox centerVBox = new VBox(20);
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.getChildren().add(gridPane);
        borderPane.setCenter(centerVBox);
        
        // Footer
        Label footerLabel = new Label("© 2023 Music Library Organizer");
        StackPane footer = new StackPane(footerLabel);
        footer.setPadding(new Insets(15));
        footer.getStyleClass().add("footer");
        borderPane.setBottom(footer);
        
        return borderPane;
    }
    
    private void handleRegister(String username, String password, String confirmPassword, String email) {
        try {
            // Input validation
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(AlertType.ERROR, "Registration Error", "Please fill all required fields.");
                return;
            }
            
            if (username.length() < 3) {
                showAlert(AlertType.ERROR, "Registration Error", "Username must be at least 3 characters.");
                return;
            }
            
            if (password.length() < 4) {
                showAlert(AlertType.ERROR, "Registration Error", "Password must be at least 4 characters.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Registration Error", "Passwords do not match.");
                return;
            }
            
            boolean success = false;
            
            // Decide which controller to use
            if (userController != null) {
                success = userController.registerUser(username, password, email);
            } else if (userFXController != null) {
                success = userFXController.registerUser(username, password, email);
            }
            
            if (success) {
                logger.info("User registered: {}", username);
                showAlert(AlertType.INFORMATION, "Registration Success", 
                        "Account created successfully. You can now login.");
                returnToLogin();
            } else {
                logger.warn("Registration failed for username: {}", username);
                showAlert(AlertType.ERROR, "Registration Failed", 
                        "This username is already taken. Please choose another one.");
            }
        } catch (Exception e) {
            logger.error("Error during registration", e);
            showAlert(AlertType.ERROR, "System Error", "An error occurred: " + e.getMessage());
        }
    }
    
    private void returnToLogin() {
        try {
            LoginViewFX loginView;
            
            // Decide which controller to use
            if (userController != null) {
                loginView = new LoginViewFX(stage, userController);
            } else {
                loginView = new LoginViewFX(stage, userFXController);
            }
            
            Scene scene = new Scene(loginView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Returned to login page");
        } catch (Exception e) {
            logger.error("Error returning to login page", e);
            showAlert(AlertType.ERROR, "Navigation Error", "Could not return to login page: " + e.getMessage());
        }
    }
    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 