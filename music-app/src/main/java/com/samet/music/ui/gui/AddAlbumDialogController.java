package com.samet.music.ui.gui;

import com.samet.music.model.Artist;
import com.samet.music.service.MusicCollectionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class AddAlbumDialogController {

    @FXML private TextField nameField;
    @FXML private ComboBox<Artist> artistComboBox;
    @FXML private TextField yearField;
    @FXML private TextField genreField;

    private Stage dialogStage;
    private boolean saved = false;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML
    private void initialize() {
        // Sanatçı ComboBox'ını yapılandır
        artistComboBox.setCellFactory(param -> new ListCell<Artist>() {
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

        artistComboBox.setConverter(new StringConverter<Artist>() {
            @Override
            public String toString(Artist artist) {
                if (artist == null) {
                    return null;
                }
                return artist.getName();
            }

            @Override
            public Artist fromString(String string) {
                return null; // Not used for ComboBox
            }
        });

        // Sanatçıları yükle
        List<Artist> artists = service.getAllArtists();
        artistComboBox.setItems(FXCollections.observableArrayList(artists));
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
            String name = nameField.getText().trim();
            Artist artist = artistComboBox.getValue();
            String yearStr = yearField.getText().trim();
            String genre = genreField.getText().trim();

            try {
                int year = Integer.parseInt(yearStr);

                boolean success = service.addAlbum(name, artist.getId(), year, genre);

                if (success) {
                    saved = true;
                    dialogStage.close();
                } else {
                    showErrorAlert("Error", "Failed to save the album. Please try again.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Validation Error", "Release year must be a valid number");
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        Artist artist = artistComboBox.getValue();
        String yearStr = yearField.getText().trim();

        if (name.isEmpty()) {
            showErrorAlert("Validation Error", "Album name cannot be empty");
            return false;
        }

        if (artist == null) {
            showErrorAlert("Validation Error", "Please select an artist");
            return false;
        }

        if (yearStr.isEmpty()) {
            showErrorAlert("Validation Error", "Release year cannot be empty");
            return false;
        }

        try {
            int year = Integer.parseInt(yearStr);
            if (year <= 0) {
                showErrorAlert("Validation Error", "Release year must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Release year must be a valid number");
            return false;
        }

        return true;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}