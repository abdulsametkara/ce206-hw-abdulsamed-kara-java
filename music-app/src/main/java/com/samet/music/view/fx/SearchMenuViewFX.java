package com.samet.music.view.fx;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.SongController;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
 * JavaFX implementation of the search menu view
 */
public class SearchMenuViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(SearchMenuViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    private TableView<Song> songTable;
    private ObservableList<Song> songData;
    
    private TextField searchField;
    private ComboBox<String> searchTypeComboBox;
    
    public SearchMenuViewFX(Stage stage, User currentUser) {
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
        Text headerText = new Text("Search Music");
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
        
        // Center content with search form and results
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        
        // Search form
        VBox searchFormContainer = new VBox(15);
        searchFormContainer.setPadding(new Insets(10));
        searchFormContainer.getStyleClass().add("form-container");
        
        Label searchLabel = new Label("Search for Music");
        searchLabel.getStyleClass().add("section-title");
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Enter search term...");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("search-box");
        
        searchTypeComboBox = new ComboBox<>();
        searchTypeComboBox.getItems().addAll("All", "Title", "Artist", "Album", "Genre");
        searchTypeComboBox.setValue("All");
        
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(e -> performSearch());
        
        searchBox.getChildren().addAll(searchField, searchTypeComboBox, searchButton);
        
        Label searchTipsLabel = new Label("Search Tips: Enter a title, artist, album, or genre to find matching songs.");
        searchTipsLabel.getStyleClass().add("info-label");
        
        searchFormContainer.getChildren().addAll(searchLabel, searchBox, searchTipsLabel);
        
        // Search results
        VBox resultsContainer = new VBox(15);
        Label resultsLabel = new Label("Search Results");
        resultsLabel.getStyleClass().add("section-title");
        
        songTable = createSongTable();
        
        resultsContainer.getChildren().addAll(resultsLabel, songTable);
        VBox.setVgrow(songTable, Priority.ALWAYS);
        
        centerContent.getChildren().addAll(searchFormContainer, resultsContainer);
        VBox.setVgrow(resultsContainer, Priority.ALWAYS);
        
        borderPane.setCenter(centerContent);
        
        // Footer
        Label footerLabel = new Label("© 2023 Music Library Organizer");
        StackPane footer = new StackPane(footerLabel);
        footer.setPadding(new Insets(15));
        footer.getStyleClass().add("footer");
        borderPane.setBottom(footer);
        
        return borderPane;
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
        
        TableColumn<Song, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(70);
        
        // Add the columns
        table.getColumns().addAll(titleCol, artistCol, albumCol, genreCol, yearCol);
        
        // Add actions column for adding to playlist, etc.
        TableColumn<Song, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        
        table.getColumns().add(actionsCol);
        
        // Initialize data
        songData = FXCollections.observableArrayList();
        table.setItems(songData);
        
        // Set placeholder text when table is empty
        table.setPlaceholder(new Label("No songs found. Try another search term."));
        
        return table;
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String searchType = searchTypeComboBox.getValue();
        
        if (searchTerm.isEmpty()) {
            showAlert(AlertType.WARNING, "Search Error", "Please enter a search term.");
            return;
        }
        
        try {
            // This is a placeholder. In the actual application, you would call a method
            // from a controller to search for songs
            List<Song> results = searchSongs(searchTerm, searchType);
            
            songData.clear();
            if (results != null) {
                songData.addAll(results);
                logger.info("Found {} songs matching '{}'", results.size(), searchTerm);
            }
            
            if (songData.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Search Results", 
                        "No songs found matching '" + searchTerm + "'.");
            }
        } catch (Exception e) {
            logger.error("Error searching songs", e);
            showAlert(AlertType.ERROR, "Search Error", "An error occurred: " + e.getMessage());
        }
    }
    
    // Placeholder method - would be replaced with actual controller call
    private List<Song> searchSongs(String searchTerm, String searchType) {
        // This is just a placeholder. In the actual implementation,
        // this would call the appropriate controller method
        
        // Example implementation:
        // if ("Title".equals(searchType)) {
        //     return songController.searchByTitle(searchTerm);
        // } else if ("Artist".equals(searchType)) {
        //     return songController.searchByArtist(searchTerm);
        // } ...
        
        // For now, we'll just return an empty list
        return FXCollections.observableArrayList();
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