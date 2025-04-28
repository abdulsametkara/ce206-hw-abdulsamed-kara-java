package com.samet.music.view.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
import com.samet.music.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the main menu view
 */
public class MainMenuViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    public MainMenuViewFX(Stage stage, User currentUser) {
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
        Text headerText = new Text("Music Library Organizer");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 28));
        headerText.getStyleClass().add("header-text");
        
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        VBox headerVBox = new VBox(10);
        headerVBox.setAlignment(Pos.CENTER);
        headerVBox.getChildren().addAll(headerText, welcomeLabel);
        
        StackPane header = new StackPane(headerVBox);
        header.setPadding(new Insets(20));
        header.getStyleClass().add("header");
        borderPane.setTop(header);
        
        // Main menu options
        VBox menuOptions = new VBox(15);
        menuOptions.setAlignment(Pos.CENTER);
        menuOptions.setPadding(new Insets(30));
        
        Button musicCollectionButton = createMenuButton("Music Collection", () -> openMusicCollection());
        Button playlistsButton = createMenuButton("Playlists", () -> openPlaylists());
        Button searchButton = createMenuButton("Search Music", () -> openSearch());
        Button recommendationsButton = createMenuButton("Recommendations", () -> openRecommendations());
        Button settingsButton = createMenuButton("User Settings", () -> openSettings());
        Button logoutButton = createMenuButton("Logout", () -> logout());
        
        menuOptions.getChildren().addAll(
                musicCollectionButton,
                playlistsButton,
                searchButton,
                recommendationsButton,
                settingsButton,
                logoutButton
        );
        
        borderPane.setCenter(menuOptions);
        
        // Footer
        Label footerLabel = new Label("© 2023 Music Library Organizer");
        StackPane footer = new StackPane(footerLabel);
        footer.setPadding(new Insets(15));
        footer.getStyleClass().add("footer");
        borderPane.setBottom(footer);
        
        return borderPane;
    }
    
    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setPrefWidth(250);
        button.setPrefHeight(40);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    private void openMusicCollection() {
        try {
            MusicCollectionViewFX view = new MusicCollectionViewFX(stage, currentUser);
            Scene scene = new Scene(view.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened music collection for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening music collection", e);
            // Show error alert
        }
    }
    
    private void openPlaylists() {
        try {
            PlaylistMenuViewFX view = new PlaylistMenuViewFX(stage, currentUser);
            Scene scene = new Scene(view.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened playlists for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening playlists", e);
            // Show error alert
        }
    }
    
    private void openSearch() {
        try {
            SearchMenuViewFX view = new SearchMenuViewFX(stage, currentUser);
            Scene scene = new Scene(view.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened search for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening search", e);
            // Show error alert
        }
    }
    
    private void openRecommendations() {
        try {
            RecommendationViewFX view = new RecommendationViewFX(stage, currentUser);
            Scene scene = new Scene(view.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened recommendations for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening recommendations", e);
            // Show error alert
        }
    }
    
    private void openSettings() {
        try {
            UserSettingsViewFX view = new UserSettingsViewFX(stage, currentUser);
            Scene scene = new Scene(view.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("Opened settings for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening settings", e);
            // Show error alert
        }
    }
    
    private void logout() {
        try {
            UserController controller = new UserController();
            LoginViewFX loginView = new LoginViewFX(stage, controller);
            Scene scene = new Scene(loginView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            logger.info("User logged out: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error during logout", e);
            // Show error alert
        }
    }
} 