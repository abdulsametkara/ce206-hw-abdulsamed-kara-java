package com.samet.music.view.fx;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * JavaFX implementation of the playlist menu view
 */
public class PlaylistMenuViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistMenuViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    private final PlaylistController playlistController;
    private final SongController songController;
    
    private ListView<Playlist> playlistListView;
    private TableView<Song> songTable;
    private ObservableList<Playlist> playlistData;
    private ObservableList<Song> songData;
    
    private Playlist selectedPlaylist;
    
    public PlaylistMenuViewFX(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        
        // Initialize controllers
        UserController userController = new UserController();
        // Manually simulating a login since we already have the user object
        userController.loginUser(currentUser.getUsername(), currentUser.getPassword());
        this.playlistController = new PlaylistController(userController);
        this.songController = new SongController(userController);
        
        this.root = createContent();
        loadPlaylists();
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
        Text headerText = new Text("Playlists");
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
        
        // Center content with playlists and songs
        HBox centerContent = new HBox(20);
        centerContent.setPadding(new Insets(20));
        
        // Playlists section
        VBox playlistsSection = new VBox(15);
        playlistsSection.setPrefWidth(300);
        
        Label playlistsLabel = new Label("Your Playlists");
        playlistsLabel.getStyleClass().add("section-title");
        
        // New playlist form
        GridPane newPlaylistForm = new GridPane();
        newPlaylistForm.setHgap(10);
        newPlaylistForm.setVgap(10);
        newPlaylistForm.setPadding(new Insets(10));
        
        Label newPlaylistLabel = new Label("Create New Playlist");
        newPlaylistLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        newPlaylistForm.add(newPlaylistLabel, 0, 0, 2, 1);
        
        TextField playlistNameField = new TextField();
        playlistNameField.setPromptText("Playlist name");
        newPlaylistForm.add(playlistNameField, 0, 1);
        
        Button createButton = new Button("Create");
        createButton.setPrefWidth(80);
        createButton.setOnAction(e -> createPlaylist(playlistNameField.getText()));
        newPlaylistForm.add(createButton, 1, 1);
        
        // Playlist list
        playlistListView = new ListView<>();
        playlistListView.setPrefHeight(400);
        VBox.setVgrow(playlistListView, Priority.ALWAYS);
        
        // Selection listener
        playlistListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedPlaylist = newValue;
                        loadPlaylistSongs(selectedPlaylist);
                    }
                });
        
        playlistsSection.getChildren().addAll(playlistsLabel, newPlaylistForm, playlistListView);
        
        // Songs section
        VBox songsSection = new VBox(15);
        HBox.setHgrow(songsSection, Priority.ALWAYS);
        
        Label selectedPlaylistLabel = new Label("Songs in Playlist");
        selectedPlaylistLabel.getStyleClass().add("section-title");
        
        // Song table
        songTable = createSongTable();
        VBox.setVgrow(songTable, Priority.ALWAYS);
        
        // Buttons for song management
        HBox songManagementButtons = new HBox(10);
        songManagementButtons.setAlignment(Pos.CENTER);
        
        Button addSongButton = new Button("Add Songs");
        addSongButton.setOnAction(e -> showAddSongDialog());
        
        Button removeSongButton = new Button("Remove Selected");
        removeSongButton.setOnAction(e -> removeSelectedSong());
        removeSongButton.setDisable(true);
        
        // Enable remove button when a song is selected
        songTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> removeSongButton.setDisable(newValue == null));
        
        songManagementButtons.getChildren().addAll(addSongButton, removeSongButton);
        
        songsSection.getChildren().addAll(selectedPlaylistLabel, songTable, songManagementButtons);
        
        centerContent.getChildren().addAll(playlistsSection, songsSection);
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
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistCol.setPrefWidth(150);
        
        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
        albumCol.setPrefWidth(150);
        
        TableColumn<Song, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreCol.setPrefWidth(100);
        
        TableColumn<Song, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(70);
        
        // Add the columns
        table.getColumns().addAll(titleCol, artistCol, albumCol, genreCol, yearCol);
        
        // Initialize data
        songData = FXCollections.observableArrayList();
        table.setItems(songData);
        
        // Set placeholder text when table is empty
        table.setPlaceholder(new Label("No songs in this playlist. Add some songs!"));
        
        return table;
    }
    
    private void loadPlaylists() {
        try {
            List<Playlist> playlists = playlistController.getUserPlaylists();
            playlistData = FXCollections.observableArrayList(playlists);
            playlistListView.setItems(playlistData);
            
            if (!playlists.isEmpty()) {
                playlistListView.getSelectionModel().select(0);
                selectedPlaylist = playlists.get(0);
                loadPlaylistSongs(selectedPlaylist);
            }
            
            logger.info("Loaded {} playlists for user {}", playlists.size(), currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error loading playlists", e);
            showAlert(AlertType.ERROR, "Error", "Could not load playlists: " + e.getMessage());
        }
    }
    
    private void loadPlaylistSongs(Playlist playlist) {
        try {
            Playlist fullPlaylist = playlistController.getPlaylist(playlist.getId());
            List<Song> songs = fullPlaylist != null ? new ArrayList<>(fullPlaylist.getSongs()) : new ArrayList<>();
            songData.clear();
            songData.addAll(songs);
            logger.info("Loaded {} songs for playlist '{}'", songs.size(), playlist.getName());
        } catch (Exception e) {
            logger.error("Error loading songs for playlist", e);
            showAlert(AlertType.ERROR, "Error", "Could not load songs: " + e.getMessage());
        }
    }
    
    private void createPlaylist(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "Please enter a playlist name.");
                return;
            }
            
            Playlist playlist = playlistController.createPlaylist(name, "");
            
            if (playlist != null) {
                showAlert(AlertType.INFORMATION, "Success", "Playlist created successfully.");
                loadPlaylists();
                // Select the newly created playlist
                for (int i = 0; i < playlistData.size(); i++) {
                    if (playlistData.get(i).getId() == playlist.getId()) {
                        playlistListView.getSelectionModel().select(i);
                        break;
                    }
                }
                logger.info("Created new playlist: {}", name);
            } else {
                showAlert(AlertType.ERROR, "Error", "Could not create playlist. It may already exist.");
                logger.warn("Failed to create playlist: {}", name);
            }
        } catch (Exception e) {
            logger.error("Error creating playlist", e);
            showAlert(AlertType.ERROR, "Error", "Could not create playlist: " + e.getMessage());
        }
    }
    
    private void showAddSongDialog() {
        if (selectedPlaylist == null) {
            showAlert(AlertType.WARNING, "Warning", "Please select a playlist first.");
            return;
        }
        
        try {
            // Create a dialog for selecting songs
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Songs to Playlist");
            dialog.setHeaderText("Select songs to add to playlist: " + selectedPlaylist.getName());
            
            // Set the button types
            ButtonType addButtonType = new ButtonType("Add Selected", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
            
            // Create a VBox to hold the content
            VBox content = new VBox(10);
            content.setPrefWidth(800);
            content.setPrefHeight(500);
            content.setPadding(new Insets(20));
            
            // Create a TableView for songs
            TableView<Song> songSelectionTable = new TableView<>();
            
            // Create columns
            TableColumn<Song, String> titleCol = new TableColumn<>("Title");
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleCol.setPrefWidth(200);
            
            TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
            artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
            artistCol.setPrefWidth(150);
            
            TableColumn<Song, String> albumCol = new TableColumn<>("Album");
            albumCol.setCellValueFactory(new PropertyValueFactory<>("album"));
            albumCol.setPrefWidth(150);
            
            TableColumn<Song, String> genreCol = new TableColumn<>("Genre");
            genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
            genreCol.setPrefWidth(100);
            
            // Add the columns
            songSelectionTable.getColumns().addAll(titleCol, artistCol, albumCol, genreCol);
            
            // Allow multiple selection
            songSelectionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            
            // Set up the table data
            List<Song> userSongs = songController.getUserSongs();
            ObservableList<Song> availableSongs = FXCollections.observableArrayList(userSongs);
            songSelectionTable.setItems(availableSongs);
            
            // Add the table to the content
            content.getChildren().add(songSelectionTable);
            VBox.setVgrow(songSelectionTable, Priority.ALWAYS);
            
            // Set the content to the dialog
            dialog.getDialogPane().setContent(content);
            
            // Request focus on the table
            Platform.runLater(() -> songSelectionTable.requestFocus());
            
            // Show the dialog and handle the result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == addButtonType) {
                List<Song> selectedSongs = new ArrayList<>(songSelectionTable.getSelectionModel().getSelectedItems());
                
                if (selectedSongs.isEmpty()) {
                    showAlert(AlertType.WARNING, "Warning", "No songs selected.");
                    return;
                }
                
                // Add selected songs to the playlist
                int successCount = 0;
                for (Song song : selectedSongs) {
                    boolean success = playlistController.addSongToPlaylist(selectedPlaylist.getId(), song.getId());
                    if (success) {
                        successCount++;
                    }
                }
                
                if (successCount > 0) {
                    showAlert(AlertType.INFORMATION, "Success", 
                            String.format("Added %d song(s) to playlist successfully.", successCount));
                    loadPlaylistSongs(selectedPlaylist);
                    logger.info("Added {} songs to playlist '{}'", successCount, selectedPlaylist.getName());
                } else {
                    showAlert(AlertType.ERROR, "Error", "Could not add songs to playlist.");
                    logger.warn("Failed to add songs to playlist '{}'", selectedPlaylist.getName());
                }
            }
        } catch (Exception e) {
            logger.error("Error showing add song dialog", e);
            showAlert(AlertType.ERROR, "Error", "Could not open add song dialog: " + e.getMessage());
        }
    }
    
    private void removeSelectedSong() {
        try {
            Song selectedSong = songTable.getSelectionModel().getSelectedItem();
            
            if (selectedSong == null || selectedPlaylist == null) {
                return;
            }
            
            boolean success = playlistController.removeSongFromPlaylist(selectedPlaylist.getId(), selectedSong.getId());
            
            if (success) {
                songData.remove(selectedSong);
                logger.info("Removed song '{}' from playlist '{}'", selectedSong.getTitle(), selectedPlaylist.getName());
            } else {
                showAlert(AlertType.ERROR, "Error", "Could not remove song from playlist.");
                logger.warn("Failed to remove song '{}' from playlist '{}'", selectedSong.getTitle(), selectedPlaylist.getName());
            }
        } catch (Exception e) {
            logger.error("Error removing song from playlist", e);
            showAlert(AlertType.ERROR, "Error", "Could not remove song: " + e.getMessage());
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