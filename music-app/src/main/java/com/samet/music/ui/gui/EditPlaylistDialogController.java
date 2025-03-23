package com.samet.music.ui.gui;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.model.Playlist;
import com.samet.music.service.MusicCollectionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditPlaylistDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;

    private Stage dialogStage;
    private Playlist playlist;
    private boolean saved = false;
    private MusicCollectionService service = MusicCollectionService.getInstance();
    private PlaylistDAO playlistDAO = new PlaylistDAO(); // PlaylistDAO ekleyelim

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        // Set field values based on playlist
        nameField.setText(playlist.getName());
        descriptionArea.setText(playlist.getDescription());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();

            // Playlist nesnesini güncelle
            playlist.setName(name);
            playlist.setDescription(description);

            // Veritabanında güncelle - Bu satırı ekledik
            playlistDAO.update(playlist);

            saved = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showErrorAlert("Validation Error", "Playlist name cannot be empty");
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