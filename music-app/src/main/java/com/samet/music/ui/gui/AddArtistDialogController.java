package com.samet.music.ui.gui;

import com.samet.music.service.MusicCollectionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddArtistDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea biographyArea;

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
    private void handleSave() {
        if (validateInput()) {
            String name = nameField.getText().trim();
            String biography = biographyArea.getText().trim();

            boolean success = service.addArtist(name, biography);

            if (success) {
                saved = true;
                dialogStage.close();
            } else {
                showErrorAlert("Error", "Failed to save the artist. Please try again.");
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
            showErrorAlert("Validation Error", "Artist name cannot be empty");
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