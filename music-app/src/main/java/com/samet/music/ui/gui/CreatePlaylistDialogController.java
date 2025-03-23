package com.samet.music.ui.gui;

import com.samet.music.service.MusicCollectionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreatePlaylistDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;

    private Stage dialogStage;
    private boolean saved = false;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleCreate() {
        if (validateInput()) {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();

            boolean success = service.createPlaylist(name, description);

            if (success) {
                saved = true;
                dialogStage.close();
            } else {
                showErrorAlert("Error", "Could not create playlist. Please try again.");
            }
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