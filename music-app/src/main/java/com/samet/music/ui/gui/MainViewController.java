package com.samet.music.ui.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainViewController {

    @FXML
    private Label statusLabel;

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Music Library");
        alert.setHeaderText("Music Library Application");
        alert.setContentText("Created by Samet\nVersion 1.0");
        alert.showAndWait();
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }
}