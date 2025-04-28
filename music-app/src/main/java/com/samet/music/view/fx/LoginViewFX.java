package com.samet.music.view.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
import com.samet.music.controller.UserFXController;
import com.samet.music.model.User;

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
 * JavaFX implementation of the login view
 */
public class LoginViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewFX.class);
    private final Stage stage;
    private final BorderPane root;
    
    // Controller can be either UserController or UserFXController
    private UserController userController;
    private UserFXController userFXController;
    
    // Constructor for UserController
    public LoginViewFX(Stage stage, UserController userController) {
        this.stage = stage;
        this.userController = userController;
        this.userFXController = null;
        this.root = createContent();
    }
    
    // Constructor for UserFXController
    public LoginViewFX(Stage stage, UserFXController userFXController) {
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
        
        // Login form
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25));
        
        Text loginTitle = new Text("Login");
        loginTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        gridPane.add(loginTitle, 0, 0, 2, 1);
        
        Label usernameLabel = new Label("Username:");
        gridPane.add(usernameLabel, 0, 1);
        
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(200);
        gridPane.add(usernameField, 1, 1);
        
        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 2);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        gridPane.add(passwordField, 1, 2);
        
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("register-button");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        gridPane.add(buttonBox, 1, 4);
        
        // Event handlers
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText()));
        
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
    
    public void handleLogin(String username, String password) {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                showAlert(AlertType.ERROR, "Login Error", "Please enter both username and password.");
                return;
            }
            
            boolean success = false;
            User user = null;
            
            // Decide which controller to use
            if (userController != null) {
                success = userController.loginUser(username, password);
                if (success) {
                    user = userController.getCurrentUser();
                }
            } else if (userFXController != null) {
                success = userFXController.loginUser(username, password);
            if (success) {
                    user = userFXController.getCurrentUser();
                }
            }
            
            if (success && user != null) {
                logger.info("User logged in: {}", username);
                openMainMenu(user);
            } else {
                logger.warn("Login failed for username: {}", username);
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } catch (Exception e) {
            logger.error("Error during login", e);
            showAlert(AlertType.ERROR, "System Error", "An error occurred: " + e.getMessage());
        }
    }
    
    public void handleRegister(String username, String password) {
        try {
            RegisterViewFX registerView;
            
            // Decide which controller to use
            if (userController != null) {
                registerView = new RegisterViewFX(stage, userController);
            } else {
                registerView = new RegisterViewFX(stage, userFXController);
            }
            
            Scene scene = new Scene(registerView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened registration page");
        } catch (Exception e) {
            logger.error("Error opening registration page", e);
            showAlert(AlertType.ERROR, "Navigation Error", "Could not open the registration page: " + e.getMessage());
        }
    }
    
    private void openMainMenu(User user) {
        try {
            MainMenuViewFX mainMenuView = new MainMenuViewFX(stage, user);
            Scene scene = new Scene(mainMenuView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened main menu for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error opening main menu", e);
            showAlert(AlertType.ERROR, "Navigation Error", "Could not open the main menu: " + e.getMessage());
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