package com.samet.music.view.fx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.model.Song;
import com.samet.music.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the recommendation view
 */
public class RecommendationViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    public RecommendationViewFX(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.root = createContent();
        
        // Load recommendations when view is created
        loadRecommendations();
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
        Text headerText = new Text("Music Recommendations");
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
        
        // Center content with tabs for different recommendation types
        TabPane tabPane = new TabPane();
        
        // For You tab - based on listening history
        Tab forYouTab = new Tab("For You");
        forYouTab.setClosable(false);
        VBox forYouContent = createRecommendationTab("Based on Your Listening History", 
                "Recommendations are based on songs you've listened to frequently.");
        forYouTab.setContent(forYouContent);
        
        // Discover tab - new releases and trending
        Tab discoverTab = new Tab("Discover");
        discoverTab.setClosable(false);
        VBox discoverContent = createRecommendationTab("New Releases & Trending", 
                "Discover new music that's trending or recently released.");
        discoverTab.setContent(discoverContent);
        
        // Similar Artists tab
        Tab similarArtistsTab = new Tab("Similar Artists");
        similarArtistsTab.setClosable(false);
        VBox similarArtistsContent = createRecommendationTab("Artists You Might Like", 
                "Based on artists you already enjoy.");
        similarArtistsTab.setContent(similarArtistsContent);
        
        // Genres tab
        Tab genresTab = new Tab("By Genre");
        genresTab.setClosable(false);
        VBox genresContent = createRecommendationTab("Explore Genres", 
                "Recommendations by your favorite genres.");
        genresTab.setContent(genresContent);
        
        tabPane.getTabs().addAll(forYouTab, discoverTab, similarArtistsTab, genresTab);
        
        borderPane.setCenter(tabPane);
        
        // Footer
        Label footerLabel = new Label("© 2023 Music Library Organizer");
        StackPane footer = new StackPane(footerLabel);
        footer.setPadding(new Insets(15));
        footer.getStyleClass().add("footer");
        borderPane.setBottom(footer);
        
        return borderPane;
    }
    
    private VBox createRecommendationTab(String title, String description) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("info-label");
        
        // Table of recommended songs
        TableView<Song> table = createSongTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh Recommendations");
        refreshButton.setOnAction(e -> loadRecommendations());
        
        container.getChildren().addAll(titleLabel, descriptionLabel, table, refreshButton);
        
        return container;
    }
    
    private TableView<Song> createSongTable() {
        TableView<Song> table = new TableView<>();
        
        // Create columns
        TableColumn<Song, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        artistCol.setPrefWidth(150);
        
        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<>("albumName"));
        albumCol.setPrefWidth(150);
        
        TableColumn<Song, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreCol.setPrefWidth(100);
        
        TableColumn<Song, String> recommendedCol = new TableColumn<>("Why Recommended");
        recommendedCol.setPrefWidth(200);
        
        // Action buttons column
        TableColumn<Song, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        
        // Add the columns
        table.getColumns().addAll(titleCol, artistCol, albumCol, genreCol, recommendedCol, actionsCol);
        
        // Initialize data
        ObservableList<Song> songData = FXCollections.observableArrayList();
        table.setItems(songData);
        
        // Set placeholder text when table is empty
        table.setPlaceholder(new Label("No recommendations available yet. Listen to more music to get recommendations!"));
        
        return table;
    }
    
    /**
     * Placeholder method for loading recommendations
     * In a real implementation, this would call controller methods to get recommendations
     */
    private void loadRecommendations() {
        try {
            logger.info("Loading recommendations for user: {}", currentUser.getUsername());
            
            // This would typically call a controller method to get real recommendations
            // For now, we'll just log the attempt
            
            // Example of how it might be implemented:
            // List<Song> forYouSongs = recommendationController.getPersonalizedRecommendations(currentUser.getId());
            // List<Song> discoverSongs = recommendationController.getNewReleases();
            // List<Song> similarArtistsSongs = recommendationController.getSimilarArtistsRecommendations(currentUser.getId());
            // List<Song> genreSongs = recommendationController.getGenreRecommendations(currentUser.getId());
            
            // For demonstration, we're not actually setting any data
            
        } catch (Exception e) {
            logger.error("Error loading recommendations", e);
            showAlert(AlertType.ERROR, "Error", "Could not load recommendations: " + e.getMessage());
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