package com.samet.music.ui.gui;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddSongDialogController {

    @FXML private TextField nameField;
    @FXML private ComboBox<Artist> artistComboBox;
    @FXML private TextField durationField;
    @FXML private TextField genreField;
    @FXML private ComboBox<Album> albumComboBox;

    private Stage dialogStage;
    private boolean saved = false;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML
    private void initialize() {
        // Load artists for combo box
        List<Artist> artists = service.getAllArtists();
        artistComboBox.setItems(FXCollections.observableArrayList(artists));
        artistComboBox.setCellFactory(listView -> new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist artist, boolean empty) {
                super.updateItem(artist, empty);
                if (empty || artist == null) {
                    setText(null);
                } else {
                    setText(artist.getName());
                }
            }
        });
        artistComboBox.setButtonCell(new ListCell<Artist>() {
            @Override
            protected void updateItem(Artist artist, boolean empty) {
                super.updateItem(artist, empty);
                if (empty || artist == null) {
                    setText(null);
                } else {
                    setText(artist.getName());
                }
            }
        });

        // Configure artist selection listener to update albums
        artistComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadAlbumsForArtist(newValue);
            } else {
                albumComboBox.getItems().clear();
            }
        });

        // Set up album combo box
        albumComboBox.setCellFactory(listView -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    setText(album.getName() + " (" + album.getReleaseYear() + ")");
                }
            }
        });
        albumComboBox.setButtonCell(new ListCell<Album>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    setText(album.getName() + " (" + album.getReleaseYear() + ")");
                }
            }
        });
    }

    private void loadAlbumsForArtist(Artist artist) {
        List<Album> albums = service.getAlbumsByArtist(artist.getId());
        albumComboBox.setItems(FXCollections.observableArrayList(albums));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            String name = nameField.getText();
            Artist artist = artistComboBox.getValue();
            int duration = Integer.parseInt(durationField.getText());
            String genre = genreField.getText();

            boolean success = service.addSong(name, artist.getId(), duration, genre);

            if (success) {
                Album selectedAlbum = albumComboBox.getValue();
                if (selectedAlbum != null) {
                    // Get the newly created song by searching for it
                    List<Song> songs = service.searchSongsByName(name);
                    for (Song song : songs) {
                        if (song.getArtist().getId().equals(artist.getId())) {
                            service.addSongToAlbum(song.getId(), selectedAlbum.getId());
                            break;
                        }
                    }
                }

                saved = true;
                dialogStage.close();
            } else {
                showErrorAlert("Error", "Failed to save the song");
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Song name cannot be empty\n";
        }

        if (artistComboBox.getValue() == null) {
            errorMessage += "Please select an artist\n";
        }

        if (durationField.getText() == null || durationField.getText().trim().isEmpty()) {
            errorMessage += "Duration cannot be empty\n";
        } else {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                if (duration <= 0 || duration > 3600) {
                    errorMessage += "Duration must be between 1 and 3600 seconds\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Duration must be a number\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showErrorAlert("Validation Error", errorMessage);
            return false;
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}