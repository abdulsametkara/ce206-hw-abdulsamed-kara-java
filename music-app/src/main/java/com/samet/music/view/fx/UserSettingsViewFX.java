package com.samet.music.view.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the user settings view
 */
public class UserSettingsViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(UserSettingsViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    public UserSettingsViewFX(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
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
        Text headerText = new Text("User Settings");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 28));
        headerText.getStyleClass().add("header-text");
        
        HBox navButtons = new HBox(10);
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> returnToMainMenu());
        navButtons.getChildren().add(backButton);
        navButtons.setAlignment(Pos.CENTER_RIGHT);
        
        HBox headerBox = new HBox(20);
        headerBox.getChildren().addAll(headerText, navButtons);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(navButtons, Priority.ALWAYS);
        
        StackPane header = new StackPane(headerBox);
        header.setPadding(new Insets(20));
        header.getStyleClass().add("header");
        borderPane.setTop(header);
        
        // Center content with tabs
        TabPane tabPane = new TabPane();
        
        // Profile tab
        Tab profileTab = new Tab("Profile");
        profileTab.setClosable(false);
        VBox profileContent = createProfileTab();
        profileTab.setContent(profileContent);
        
        // Security tab
        Tab securityTab = new Tab("Security");
        securityTab.setClosable(false);
        VBox securityContent = createSecurityTab();
        securityTab.setContent(securityContent);
        
        // Preferences tab
        Tab preferencesTab = new Tab("Preferences");
        preferencesTab.setClosable(false);
        VBox preferencesContent = createPreferencesTab();
        preferencesTab.setContent(preferencesContent);
        
        tabPane.getTabs().addAll(profileTab, securityTab, preferencesTab);
        
        borderPane.setCenter(tabPane);
        
        // Footer
        Label footerLabel = new Label("© 2023 Music Library Organizer");
        StackPane footer = new StackPane(footerLabel);
        footer.setPadding(new Insets(15));
        footer.getStyleClass().add("footer");
        borderPane.setBottom(footer);
        
        return borderPane;
    }
    
    private VBox createProfileTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Profile Information");
        titleLabel.getStyleClass().add("section-title");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        
        // Username (non-editable)
        Label usernameLabel = new Label("Username:");
        form.add(usernameLabel, 0, 0);
        
        TextField usernameField = new TextField(currentUser.getUsername());
        usernameField.setEditable(false);
        usernameField.setPrefWidth(300);
        form.add(usernameField, 1, 0);
        
        // Display Name
        Label displayNameLabel = new Label("Display Name:");
        form.add(displayNameLabel, 0, 1);
        
        TextField displayNameField = new TextField(currentUser.getUsername());
        displayNameField.setPrefWidth(300);
        form.add(displayNameField, 1, 1);
        
        // Email
        Label emailLabel = new Label("Email:");
        form.add(emailLabel, 0, 2);
        
        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPrefWidth(300);
        form.add(emailField, 1, 2);
        
        // Bio
        Label bioLabel = new Label("Bio:");
        form.add(bioLabel, 0, 3);
        
        TextField bioField = new TextField("Tell us about yourself...");
        bioField.setPrefWidth(300);
        form.add(bioField, 1, 3);
        
        // Save button
        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("login-button");
        saveButton.setOnAction(e -> saveProfileChanges(displayNameField.getText(), emailField.getText(), bioField.getText()));
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(saveButton);
        form.add(buttonBox, 1, 4);
        
        container.getChildren().addAll(titleLabel, form);
        
        return container;
    }
    
    private VBox createSecurityTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Security Settings");
        titleLabel.getStyleClass().add("section-title");
        
        // Change Password Form
        GridPane changePasswordForm = new GridPane();
        changePasswordForm.setHgap(10);
        changePasswordForm.setVgap(10);
        changePasswordForm.setPadding(new Insets(20));
        
        Label passwordHeaderLabel = new Label("Change Password");
        passwordHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        changePasswordForm.add(passwordHeaderLabel, 0, 0, 2, 1);
        
        // Current Password
        Label currentPasswordLabel = new Label("Current Password:");
        changePasswordForm.add(currentPasswordLabel, 0, 1);
        
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPrefWidth(300);
        changePasswordForm.add(currentPasswordField, 1, 1);
        
        // New Password
        Label newPasswordLabel = new Label("New Password:");
        changePasswordForm.add(newPasswordLabel, 0, 2);
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPrefWidth(300);
        changePasswordForm.add(newPasswordField, 1, 2);
        
        // Confirm New Password
        Label confirmPasswordLabel = new Label("Confirm New Password:");
        changePasswordForm.add(confirmPasswordLabel, 0, 3);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPrefWidth(300);
        changePasswordForm.add(confirmPasswordField, 1, 3);
        
        // Change Password Button
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(e -> changePassword(
                currentPasswordField.getText(), 
                newPasswordField.getText(), 
                confirmPasswordField.getText()));
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(changePasswordButton);
        changePasswordForm.add(buttonBox, 1, 4);
        
        container.getChildren().addAll(titleLabel, changePasswordForm);
        
        return container;
    }
    
    private VBox createPreferencesTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Application Preferences");
        titleLabel.getStyleClass().add("section-title");
        
        Label placeholderLabel = new Label("Preferences will be available in a future update.");
        placeholderLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        container.getChildren().addAll(titleLabel, placeholderLabel);
        
        return container;
    }
    
    private void saveProfileChanges(String displayName, String email, String bio) {
        try {
            // In a real implementation, this would call a controller method
            // For now, we'll just show a success message
            
            logger.info("Saving profile changes for user: {}", currentUser.getUsername());
            
            showAlert(AlertType.INFORMATION, "Success", "Profile changes saved successfully.");
        } catch (Exception e) {
            logger.error("Error saving profile changes", e);
            showAlert(AlertType.ERROR, "Error", "Could not save profile changes: " + e.getMessage());
        }
    }
    
    private void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        try {
            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "All password fields are required.");
                return;
            }
            
            if (newPassword.length() < 4) {
                showAlert(AlertType.ERROR, "Error", "New password must be at least 4 characters.");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Error", "New passwords don't match.");
                return;
            }
            
            // In a real implementation, this would call a controller method
            // For now, we'll just show a success message
            
            logger.info("Changing password for user: {}", currentUser.getUsername());
            
            showAlert(AlertType.INFORMATION, "Success", "Password changed successfully.");
        } catch (Exception e) {
            logger.error("Error changing password", e);
            showAlert(AlertType.ERROR, "Error", "Could not change password: " + e.getMessage());
        }
    }
    
    private void returnToMainMenu() {
        try {
            MainMenuViewFX mainMenuView = new MainMenuViewFX(stage, currentUser);
            Scene scene = new Scene(mainMenuView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Returned to main menu");
        } catch (Exception e) {
            logger.error("Error returning to main menu", e);
            showAlert(AlertType.ERROR, "Navigation Error", "Could not return to main menu: " + e.getMessage());
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