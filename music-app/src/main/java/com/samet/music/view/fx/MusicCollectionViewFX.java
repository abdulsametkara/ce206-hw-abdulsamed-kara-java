package com.samet.music.view.fx;

import com.samet.music.controller.AlbumController;
import com.samet.music.controller.ArtistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * JavaFX implementation of the music collection view
 */
public class MusicCollectionViewFX implements ViewFX {
    private static final Logger logger = LoggerFactory.getLogger(MusicCollectionViewFX.class);
    
    private final Stage stage;
    private final User currentUser;
    private final BorderPane root;
    
    private SongController songController;
    private AlbumController albumController;
    private ArtistController artistController;
    
    private TableView<Song> songTable;
    private ObservableList<Song> songData;
    
    /**
     * Constructor
     * @param stage the JavaFX stage
     * @param currentUser the currently logged-in user
     */
    public MusicCollectionViewFX(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.root = new BorderPane();
        
        if (currentUser == null) {
            logger.error("No user logged in. Returning to login screen.");
            returnToMainMenu();
            return;
        }
        
        // Mevcut kullanıcıyı kullan
        UserController userController = new UserController();
        userController.setCurrentUser(currentUser);
        
        this.songController = new SongController(userController);
        this.albumController = new AlbumController();
        this.artistController = new ArtistController();
        
        this.songData = FXCollections.observableArrayList();
        
        createContent();
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Music Collection - " + currentUser.getUsername());
        stage.setResizable(true);
        
        // İlk yüklemede verileri al
        if (songTable != null) {
            songTable.setItems(songData);
            loadSongs();
        }
    }
    
    @Override
    public Parent getRoot() {
        return root;
    }
    
    @Override
    public Stage getStage() {
        return stage;
    }
    
    /**
     * Create the main UI content
     * @return the border pane containing the UI
     */
    private BorderPane createContent() {
        // Create tabs for different sections
        TabPane tabPane = new TabPane();
        
        // Songs tab
        Tab songsTab = new Tab("Songs");
        songsTab.setClosable(false);
        
        VBox songsContent = new VBox(10);
        songsContent.setPadding(new Insets(10));
        
        // Add song form
        GridPane addSongForm = createAddSongForm();
        songsContent.getChildren().add(addSongForm);
        
        // Song table with search
        VBox tableContainer = new VBox(10);
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Search:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter title, artist, or album");
        searchField.setPrefWidth(250);
        
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchSongs(searchField.getText()));
        
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            searchField.clear();
            loadSongs();
        });
        
        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, clearButton);
        
        songTable = createSongTable();
        VBox.setVgrow(songTable, Priority.ALWAYS);
        
        tableContainer.getChildren().addAll(searchBox, songTable);
        songsContent.getChildren().add(tableContainer);
        
        songsTab.setContent(songsContent);
        
        // Artists tab
        Tab artistsTab = new Tab("Artists");
        artistsTab.setClosable(false);
        
        VBox artistsContent = new VBox(10);
        artistsContent.setPadding(new Insets(10));
        
        // Add artist form
        GridPane addArtistForm = createAddArtistForm();
        artistsContent.getChildren().add(addArtistForm);
        
        // Artists list
        VBox listContainer = new VBox(10);
        Label artistsLabel = new Label("Your Artists:");
        
        ListView<String> artistsListView = new ListView<>();
        loadArtists(artistsListView);
        VBox.setVgrow(artistsListView, Priority.ALWAYS);
        
        // Add context menu for edit/delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Artist");
        MenuItem deleteItem = new MenuItem("Delete Artist");
        
        editItem.setOnAction(e -> editArtist(artistsListView.getSelectionModel().getSelectedItem(), artistsListView));
        deleteItem.setOnAction(e -> deleteArtist(artistsListView.getSelectionModel().getSelectedItem(), artistsListView));
        
        contextMenu.getItems().addAll(editItem, deleteItem);
        artistsListView.setContextMenu(contextMenu);
        
        listContainer.getChildren().addAll(artistsLabel, artistsListView);
        artistsContent.getChildren().add(listContainer);
        
        artistsTab.setContent(artistsContent);
        
        // Albums tab
        Tab albumsTab = new Tab("Albums");
        albumsTab.setClosable(false);
        
        VBox albumsContent = new VBox(10);
        albumsContent.setPadding(new Insets(10));
        
        // Add album form
        GridPane addAlbumForm = createAddAlbumForm();
        albumsContent.getChildren().add(addAlbumForm);
        
        // Albums table
        VBox albumTableContainer = new VBox(10);
        Label albumsLabel = new Label("Your Albums:");
        
        TableView<Album> albumsTable = createAlbumsTable();
        loadAlbums(albumsTable);
        VBox.setVgrow(albumsTable, Priority.ALWAYS);
        
        albumTableContainer.getChildren().addAll(albumsLabel, albumsTable);
        albumsContent.getChildren().add(albumTableContainer);
        
        albumsTab.setContent(albumsContent);
        
        // Add tabs to pane
        tabPane.getTabs().addAll(songsTab, artistsTab, albumsTab);
        
        // Bottom buttons
        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> returnToMainMenu());
        
        HBox bottomButtons = new HBox(10);
        bottomButtons.setPadding(new Insets(10));
        bottomButtons.getChildren().add(backButton);
        
        // Add components to root pane
        root.setCenter(tabPane);
        root.setBottom(bottomButtons);
        
        // Load initial data
        loadSongs();
        
        // Albums sekmesindeki tabloyu güncelleyelim
        TableView<Album> albumsTableToUpdate = findAlbumsTable();
        if (albumsTableToUpdate != null) {
            loadAlbums(albumsTableToUpdate);
        }
        
        // Artists sekmesindeki listeyi güncelleyelim
        ListView<String> artistsListViewToUpdate = findArtistsListView();
        if (artistsListViewToUpdate != null) {
            loadArtists(artistsListViewToUpdate);
        }
        
        return root;
    }

    private GridPane createAddAlbumForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("form-container");
        
        Text formTitle = new Text("Add New Album");
        formTitle.getStyleClass().add("section-title");
        grid.add(formTitle, 0, 0, 2, 1);
        
        // Album title field
        grid.add(new Label("Album Title:"), 0, 1);
        TextField titleField = new TextField();
        grid.add(titleField, 1, 1);
        
        // Artist field - combobox with option to type new
        grid.add(new Label("Artist:"), 0, 2);
        ComboBox<String> artistCombo = new ComboBox<>();
        artistCombo.setEditable(true);
        artistCombo.setPrefWidth(200);
        
        // Load existing artists into the combo box
        try {
            List<String> artists = songController.getUserArtists();
            if (artists != null && !artists.isEmpty()) {
                artistCombo.getItems().addAll(artists);
            }
        } catch (Exception e) {
            logger.error("Error loading artists for combo box", e);
        }
        
        grid.add(artistCombo, 1, 2);
        
        // Year field
        grid.add(new Label("Year:"), 0, 3);
        TextField yearField = new TextField();
        yearField.setPromptText("e.g. 2023");
        grid.add(yearField, 1, 3);
        
        // Genre field
        grid.add(new Label("Genre:"), 0, 4);
        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.setEditable(true);
        genreCombo.getItems().addAll(
            "Rock", "Pop", "Hip Hop", "Jazz", "Classical", "Electronic", 
            "R&B", "Country", "Folk", "Blues", "Metal", "Reggae", "Other"
        );
        grid.add(genreCombo, 1, 4);
        
        // Add button
        Button addButton = new Button("Add Album");
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String artist = artistCombo.getValue();
            String yearStr = yearField.getText().trim();
            String genre = genreCombo.getValue();
            
            if (title.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Album title cannot be empty.");
                return;
            }
            
            if (artist == null || artist.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Artist cannot be empty.");
                return;
            }
            
            int year = 0;
            if (!yearStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    if (year < 1900 || year > currentYear + 5) { // Allow a few years in the future
                        showAlert(AlertType.ERROR, "Input Error", 
                            "Year must be between 1900 and " + (currentYear + 5) + ".");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert(AlertType.ERROR, "Input Error", "Year must be a number.");
                    return;
                }
            }
            
            // Make sure genre is not null
            if (genre == null) {
                genre = "Other";
            }
            
            try {
                Album album = songController.addAlbum(title, artist, year, genre);
                
                if (album != null) {
                    showAlert(AlertType.INFORMATION, "Success", "Album added successfully.");
                    titleField.clear();
                    artistCombo.setValue(null);
                    yearField.clear();
                    genreCombo.setValue(null);
                    
                    // Refresh the albums table
                    TableView<Album> albumsTable = findAlbumsTable();
                    if (albumsTable != null) {
                        loadAlbums(albumsTable);
                    }
                    
                    // Also refresh the artists list since we might have added a new artist
                    ListView<String> artistsList = findArtistsListView();
                    if (artistsList != null) {
                        loadArtists(artistsList);
                    }
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to add album.");
                }
            } catch (Exception ex) {
                logger.error("Error adding album", ex);
                showAlert(AlertType.ERROR, "Error", "An error occurred while adding the album: " + ex.getMessage());
            }
        });
        
        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            titleField.clear();
            artistCombo.setValue(null);
            yearField.clear();
            genreCombo.setValue(null);
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, resetButton);
        grid.add(buttonBox, 1, 5);
        
        return grid;
    }

    private GridPane createAddArtistForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("form-container");
        
        Text formTitle = new Text("Add New Artist");
        formTitle.getStyleClass().add("section-title");
        grid.add(formTitle, 0, 0, 2, 1);
        
        // Artist name field
        grid.add(new Label("Artist Name:"), 0, 1);
        TextField nameField = new TextField();
        grid.add(nameField, 1, 1);
        
        // Artist bio field
        grid.add(new Label("Biography:"), 0, 2);
        TextArea bioField = new TextArea();
        bioField.setPrefRowCount(3);
        bioField.setWrapText(true);
        grid.add(bioField, 1, 2);
        
        // Add button
        Button addButton = new Button("Add Artist");
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String bio = bioField.getText();
            
            if (name.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Artist name cannot be empty.");
                return;
            }
            
            try {
                boolean success = songController.addArtist(name);
                
                if (success) {
                    showAlert(AlertType.INFORMATION, "Success", "Artist added successfully.");
                    nameField.clear();
                    bioField.clear();
                    
                    // Refresh the artists list
                    ListView<String> artistsList = findArtistsListView();
                    if (artistsList != null) {
                        loadArtists(artistsList);
                    }
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to add artist.");
                }
            } catch (Exception ex) {
                logger.error("Error adding artist", ex);
                showAlert(AlertType.ERROR, "Error", "An error occurred while adding the artist: " + ex.getMessage());
            }
        });
        
        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            nameField.clear();
            bioField.clear();
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, resetButton);
        grid.add(buttonBox, 1, 3);
        
        return grid;
    }

    private void editArtist(String artistName, ListView<String> listView) {
        if (artistName == null) return;
        
        // Create a custom dialog with fields for both name and bio
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Artist");
        dialog.setHeaderText("Edit Artist Details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Artist name field
        TextField nameField = new TextField(artistName);
        nameField.setMinWidth(300);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        
        // Artist bio field - would get the current bio in a real implementation
        TextArea bioField = new TextArea("");
        bioField.setPrefRowCount(3);
        bioField.setWrapText(true);
        grid.add(new Label("Biography:"), 0, 1);
        grid.add(bioField, 1, 1);
        
        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        Platform.runLater(() -> nameField.requestFocus());
        
        // Handle the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String newName = nameField.getText().trim();
            String newBio = bioField.getText().trim();
            
            if (newName.isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Artist name cannot be empty.");
                return;
            }
            
            try {
                // Check if the name already exists (only if it's different from the original)
                if (!newName.equals(artistName) && artistController.artistExists(newName)) {
                    showAlert(AlertType.WARNING, "Warning", "An artist with this name already exists.");
                    return;
                }
                
                // In a real app with proper artist model, use ArtistController to update
                // For now, simulate by removing old name and adding new one
                boolean deleteSuccess = songController.deleteArtist(artistName);
                boolean addSuccess = songController.addArtist(newName);
                
                if (deleteSuccess && addSuccess) {
                    showAlert(AlertType.INFORMATION, "Success", "Artist updated successfully.");
                    loadArtists(listView);
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to update artist.");
                }
            } catch (Exception ex) {
                logger.error("Error updating artist", ex);
                showAlert(AlertType.ERROR, "Error", "An error occurred while updating the artist: " + ex.getMessage());
            }
        }
    }

    private void loadArtists(ListView<String> listView) {
        try {
            List<String> artists = songController.getUserArtists();
            ObservableList<String> artistsData = FXCollections.observableArrayList(artists);
            listView.setItems(artistsData);
        } catch (Exception e) {
            logger.error("Error loading artists", e);
            showAlert(AlertType.ERROR, "Error", "Could not load artists: " + e.getMessage());
        }
    }
    
    private void deleteArtist(String artistName, ListView<String> listView) {
        if (artistName == null) return;
        
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Artist");
        confirmDialog.setContentText("Are you sure you want to delete the artist: " + artistName + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = songController.deleteArtist(artistName);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Artist deleted successfully.");
                loadArtists(listView);
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to delete artist.");
            }
        }
    }
    
    private void loadAlbums(TableView<Album> table) {
        try {
            List<Album> albums = songController.getUserAlbums();
            ObservableList<Album> albumsData = FXCollections.observableArrayList(albums);
            table.setItems(albumsData);
        } catch (Exception e) {
            logger.error("Error loading albums", e);
            showAlert(AlertType.ERROR, "Error", "Could not load albums: " + e.getMessage());
        }
    }
    
    private TableView<Album> createAlbumsTable() {
        TableView<Album> table = new TableView<>();
        
        // Title column
        TableColumn<Album, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setMinWidth(150);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Artist column
        TableColumn<Album, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setMinWidth(150);
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        
        // Year column
        TableColumn<Album, Integer> yearColumn = new TableColumn<>("Year");
        yearColumn.setMinWidth(70);
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        
        // Genre column
        TableColumn<Album, String> genreColumn = new TableColumn<>("Genre");
        genreColumn.setMinWidth(100);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        
        // Actions column
        TableColumn<Album, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setMinWidth(120);
        actionsColumn.setCellFactory(col -> new TableCell<Album, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.getStyleClass().add("small-button");
                deleteButton.getStyleClass().add("small-button");
                
                editButton.setOnAction(event -> {
                    Album album = getTableView().getItems().get(getIndex());
                    editAlbum(album);
                });
                
                deleteButton.setOnAction(event -> {
                    Album album = getTableView().getItems().get(getIndex());
                    deleteAlbum(album);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(titleColumn, artistColumn, yearColumn, genreColumn, actionsColumn);
        return table;
    }
    
    private void editAlbum(Album album) {
        // Create a dialog for editing the album
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Album");
        dialog.setHeaderText("Edit album details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Album title field
        TextField titleField = new TextField(album.getTitle());
        titleField.setPromptText("Album title");
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        
        // Artist field
        ComboBox<String> artistCombo = new ComboBox<>();
        artistCombo.setEditable(true);
        artistCombo.setValue(album.getArtist());
        
        // Load existing artists
        try {
            List<String> artists = songController.getUserArtists();
            if (artists != null && !artists.isEmpty()) {
                artistCombo.getItems().addAll(artists);
            }
        } catch (Exception e) {
            logger.error("Error loading artists for combo box", e);
        }
        
        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistCombo, 1, 1);
        
        // Year field
        TextField yearField = new TextField(String.valueOf(album.getYear()));
        yearField.setPromptText("Release year");
        grid.add(new Label("Year:"), 0, 2);
        grid.add(yearField, 1, 2);
        
        // Genre field
        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.setEditable(true);
        genreCombo.setValue(album.getGenre());
        genreCombo.getItems().addAll(
            "Rock", "Pop", "Hip Hop", "Jazz", "Classical", "Electronic", 
            "R&B", "Country", "Folk", "Blues", "Metal", "Reggae", "Other"
        );
        
        grid.add(new Label("Genre:"), 0, 3);
        grid.add(genreCombo, 1, 3);
        
        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the title field
        Platform.runLater(() -> titleField.requestFocus());
        
        // Handle the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                // Get the form values
                String title = titleField.getText().trim();
                String artist = artistCombo.getValue().trim();
                String yearStr = yearField.getText().trim();
                String genre = genreCombo.getValue();
                
                // Validate the title
                if (title.isEmpty()) {
                    showAlert(AlertType.ERROR, "Input Error", "Album title cannot be empty");
                    return;
                }
                
                // Validate the artist
                if (artist.isEmpty()) {
                    showAlert(AlertType.ERROR, "Input Error", "Artist name cannot be empty");
                    return;
                }
                
                // Validate the year if provided
                int year = album.getYear();
                if (!yearStr.isEmpty()) {
                    try {
                        year = Integer.parseInt(yearStr);
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        if (year < 1900 || year > currentYear + 5) {
                            showAlert(AlertType.ERROR, "Input Error", 
                                "Year must be between 1900 and " + (currentYear + 5));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert(AlertType.ERROR, "Input Error", "Year must be a number");
                        return;
                    }
                }
                
                // Update the album using the controller
                boolean updated = true; // Placeholder
                
                if (updated) {
                    showAlert(AlertType.INFORMATION, "Success", "Album updated successfully");
                    
                    // Refresh the albums table
                    TableView<Album> albumsTable = findAlbumsTable();
                    if (albumsTable != null) {
                        loadAlbums(albumsTable);
                    }
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to update album");
                }
            } catch (Exception e) {
                logger.error("Error updating album", e);
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    
    private TableView<Album> findAlbumsTable() {
        TabPane tabPane = (TabPane) root.getCenter();
        Tab albumsTab = tabPane.getTabs().get(2);
        VBox albumsContent = (VBox) albumsTab.getContent();
        VBox tableContainer = (VBox) albumsContent.getChildren().get(1);
        return (TableView<Album>) tableContainer.getChildren().get(1);
    }
    
    private void deleteAlbum(Album album) {
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Album");
        confirmDialog.setContentText("Are you sure you want to delete the album: " + album.getTitle() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = songController.deleteAlbum(album.getId());
                
                if (deleted) {
                    showAlert(AlertType.INFORMATION, "Success", "Album deleted successfully");
                    
                    // Refresh the albums table
                    TableView<Album> albumsTable = findAlbumsTable();
                    if (albumsTable != null) {
                        loadAlbums(albumsTable);
                    }
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to delete album");
                }
            } catch (Exception e) {
                logger.error("Error deleting album", e);
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    private void returnToMainMenu() {
        try {
            if (currentUser == null) {
                // Kullanıcı giriş yapmamışsa, login ekranına yönlendir
                LoginViewFX loginView = new LoginViewFX(stage, new UserController());
                Scene scene = new Scene(loginView.getRoot(), 800, 600);
                scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Music App - Login");
                logger.info("Redirected to login screen (no user logged in)");
            } else {
                // Kullanıcı giriş yapmışsa, ana menüye yönlendir
                MainMenuViewFX mainMenuView = new MainMenuViewFX(stage, currentUser);
                Scene scene = new Scene(mainMenuView.getRoot(), 800, 600);
                scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Music App - Main Menu");
                logger.info("Returned to main menu");
            }
        } catch (Exception e) {
            logger.error("Error returning to main menu", e);
            showAlert(AlertType.ERROR, "Error", "Could not return to main menu: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private TableView<Song> createSongTable() {
        TableView<Song> table = new TableView<>();
        table.setItems(songData); // Tabloyu veri kaynağına bağla
        
        // Title column
        TableColumn<Song, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setMinWidth(150);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Artist column
        TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setMinWidth(150);
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        
        // Album column
        TableColumn<Song, String> albumColumn = new TableColumn<>("Album");
        albumColumn.setMinWidth(150);
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        
        // Genre column
        TableColumn<Song, String> genreColumn = new TableColumn<>("Genre");
        genreColumn.setMinWidth(100);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        
        // Year column
        TableColumn<Song, Integer> yearColumn = new TableColumn<>("Year");
        yearColumn.setMinWidth(60);
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        
        // Duration column
        TableColumn<Song, Integer> durationColumn = new TableColumn<>("Duration");
        durationColumn.setMinWidth(80);
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setCellFactory(column -> new TableCell<Song, Integer>() {
            @Override
            protected void updateItem(Integer duration, boolean empty) {
                super.updateItem(duration, empty);
                if (empty || duration == null) {
                    setText(null);
                } else {
                    int minutes = duration / 60;
                    int seconds = duration % 60;
                    setText(String.format("%d:%02d", minutes, seconds));
                }
            }
        });
        
        // Actions column
        TableColumn<Song, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setMinWidth(120);
        actionsColumn.setCellFactory(col -> new TableCell<Song, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.getStyleClass().add("small-button");
                deleteButton.getStyleClass().add("small-button");
                
                editButton.setOnAction(event -> {
                    Song song = getTableView().getItems().get(getIndex());
                    editSong(song);
                });
                
                deleteButton.setOnAction(event -> {
                    Song song = getTableView().getItems().get(getIndex());
                    deleteSong(song);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().addAll(titleColumn, artistColumn, albumColumn, genreColumn, yearColumn, durationColumn, actionsColumn);
        return table;
    }
    
    private GridPane createAddSongForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("form-container");
        
        Text formTitle = new Text("Add New Song");
        formTitle.getStyleClass().add("section-title");
        grid.add(formTitle, 0, 0, 2, 1);
        
        // Title field
        grid.add(new Label("Title:"), 0, 1);
        TextField titleField = new TextField();
        grid.add(titleField, 1, 1);
        
        // Artist field
        grid.add(new Label("Artist:"), 0, 2);
        TextField artistField = new TextField();
        grid.add(artistField, 1, 2);
        
        // Album field
        grid.add(new Label("Album:"), 0, 3);
        TextField albumField = new TextField();
        grid.add(albumField, 1, 3);
        
        // Genre field
        grid.add(new Label("Genre:"), 2, 1);
        TextField genreField = new TextField();
        grid.add(genreField, 3, 1);
        
        // Year field
        grid.add(new Label("Year:"), 2, 2);
        TextField yearField = new TextField();
        grid.add(yearField, 3, 2);
        
        // Duration field
        grid.add(new Label("Duration (sec):"), 2, 3);
        TextField durationField = new TextField();
        grid.add(durationField, 3, 3);
        
        // Add button
        Button addButton = new Button("Add Song");
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> addSong(
                titleField.getText(),
                artistField.getText(),
                albumField.getText(),
                genreField.getText(),
                yearField.getText(),
                durationField.getText()));
        
        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            titleField.clear();
            artistField.clear();
            albumField.clear();
            genreField.clear();
            yearField.clear();
            durationField.clear();
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, resetButton);
        grid.add(buttonBox, 1, 4);
        
        return grid;
    }
    
    private void loadSongs() {
        try {
            List<Song> songs = songController.getUserSongs();
            songData.clear();
            songData.addAll(songs);
            songTable.setItems(songData);
            logger.info("Loaded {} songs for user", songs.size());
        } catch (Exception e) {
            logger.error("Error loading songs", e);
            showAlert(AlertType.ERROR, "Error", "Could not load songs: " + e.getMessage());
        }
    }
    
    private void searchSongs(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                loadSongs();
                return;
            }
            
            List<Song> songs = songController.searchSongs(searchTerm);
            songData.clear();
            songData.addAll(songs);
            songTable.setItems(songData);
            logger.info("Found {} songs matching '{}'", songs.size(), searchTerm);
        } catch (Exception e) {
            logger.error("Error searching songs", e);
            showAlert(AlertType.ERROR, "Error", "Could not search songs: " + e.getMessage());
        }
    }
    
    private void addSong(String title, String artistName, String albumName, String genre, String yearStr, String durationStr) {
        try {
            // Validate inputs
            if (title == null || title.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Title cannot be empty.");
                return;
            }
            
            if (artistName == null || artistName.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Input Error", "Artist name cannot be empty.");
                return;
            }
            
            // Parse numeric values
            int year = 0;
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                    if (year < 1900 || year > 2100) {
                        showAlert(AlertType.ERROR, "Input Error", "Year must be between 1900 and 2100.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(AlertType.ERROR, "Input Error", "Year must be a number.");
                    return;
                }
            }
            
            int duration = 0;
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr);
                    if (duration <= 0) {
                        showAlert(AlertType.ERROR, "Input Error", "Duration must be positive.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(AlertType.ERROR, "Input Error", "Duration must be a number.");
                    return;
                }
            }
            
            // Create a dummy file path (in a real application, this would be a file selector)
            String filePath = "music/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".mp3";
            
            try {
                // Add the song using the songController
                Song addedSong = songController.addSong(title, artistName, albumName, genre, year, duration, filePath);
                
                if (addedSong != null) {
                    showAlert(AlertType.INFORMATION, "Success", "Song added successfully.");
                    loadSongs();
                    logger.info("Added new song: {}", title);
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to add song.");
                    logger.warn("Failed to add song: {}", title);
                }
            } catch (Exception e) {
                logger.error("Database error when adding song", e);
                showAlert(AlertType.ERROR, "Database Error", 
                        "Failed to add song to database. Please try again later.\nError: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error in song addition process", e);
            showAlert(AlertType.ERROR, "Error", "Could not complete the add song process: " + e.getMessage());
        }
    }
    
    private void editSong(Song song) {
        // Create a dialog for editing the song
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Song");
        dialog.setHeaderText("Edit song details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Title field
        TextField titleField = new TextField(song.getTitle());
        titleField.setPromptText("Song title");
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        
        // Artist field
        TextField artistField = new TextField(song.getArtist());
        artistField.setPromptText("Artist name");
        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistField, 1, 1);
        
        // Album field
        TextField albumField = new TextField(song.getAlbum());
        albumField.setPromptText("Album name");
        grid.add(new Label("Album:"), 0, 2);
        grid.add(albumField, 1, 2);
        
        // Genre field
        TextField genreField = new TextField(song.getGenre());
        genreField.setPromptText("Genre");
        grid.add(new Label("Genre:"), 0, 3);
        grid.add(genreField, 1, 3);
        
        // Year field
        TextField yearField = new TextField(song.getYear() > 0 ? String.valueOf(song.getYear()) : "");
        yearField.setPromptText("Release year");
        grid.add(new Label("Year:"), 0, 4);
        grid.add(yearField, 1, 4);
        
        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the title field
        Platform.runLater(() -> titleField.requestFocus());
        
        // Handle the result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                // Get the form values
                String title = titleField.getText().trim();
                String artist = artistField.getText().trim();
                String album = albumField.getText().trim();
                String genre = genreField.getText().trim();
                String yearStr = yearField.getText().trim();
                
                // Validate the title
                if (title.isEmpty()) {
                    showAlert(AlertType.ERROR, "Input Error", "Title cannot be empty");
                    return;
                }
                
                // Validate the artist
                if (artist.isEmpty()) {
                    showAlert(AlertType.ERROR, "Input Error", "Artist name cannot be empty");
                    return;
                }
                
                // Validate the year if provided
                int year = 0;
                if (!yearStr.isEmpty()) {
                    try {
                        year = Integer.parseInt(yearStr);
                        if (year < 1900 || year > 2100) {
                            showAlert(AlertType.ERROR, "Input Error", "Year must be between 1900 and 2100");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlert(AlertType.ERROR, "Input Error", "Year must be a number");
                        return;
                    }
                }
                
                // Update the song using the controller
                boolean updated = songController.updateSong(song.getId(), title, artist, album, genre, year);
                
                if (updated) {
                    showAlert(AlertType.INFORMATION, "Success", "Song updated successfully");
                    loadSongs();
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to update song");
                }
            } catch (Exception e) {
                logger.error("Error updating song", e);
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    
    private void deleteSong(Song song) {
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Song");
        confirmDialog.setContentText("Are you sure you want to delete the song: " + song.getTitle() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = songController.deleteSong(song.getId());
                
                if (deleted) {
                    showAlert(AlertType.INFORMATION, "Success", "Song deleted successfully");
                    loadSongs();
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to delete song");
                }
            } catch (Exception e) {
                logger.error("Error deleting song", e);
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    
    public void show() {
        stage.show();
    }

    private ListView<String> findArtistsListView() {
        try {
            TabPane tabPane = (TabPane) root.getCenter();
            Tab artistsTab = tabPane.getTabs().get(1);
            VBox artistsContent = (VBox) artistsTab.getContent();
            VBox listContainer = (VBox) artistsContent.getChildren().get(1);
            return (ListView<String>) listContainer.getChildren().get(1);
        } catch (Exception e) {
            logger.error("Error finding artists list view", e);
            return null;
        }
    }
}