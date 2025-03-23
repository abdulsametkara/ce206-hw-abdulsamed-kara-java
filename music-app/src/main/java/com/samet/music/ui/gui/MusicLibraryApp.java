package com.samet.music.ui.gui;

import com.samet.music.util.DatabaseUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MusicLibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Veritabanını başlat
            DatabaseUtil.initializeDatabase();

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            primaryStage.setTitle("Music Library");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        } catch (Exception e) {
            // Hata mesajını göster
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database Connection Error");
            alert.setContentText("Could not connect to the database. The application will now create a new database file.\n\n" +
                    "Error details: " + e.getMessage());
            alert.showAndWait();

            // Veritabanını sıfırla
            DatabaseUtil.setShouldResetDatabase(true);
            DatabaseUtil.initializeDatabase();

            try {
                // Tekrar dene
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
                primaryStage.setTitle("Music Library");
                primaryStage.setScene(new Scene(root, 800, 600));
                primaryStage.show();
            } catch (Exception ex) {
                Alert fatalAlert = new Alert(Alert.AlertType.ERROR);
                fatalAlert.setTitle("Fatal Error");
                fatalAlert.setHeaderText("Application Could Not Start");
                fatalAlert.setContentText("A fatal error occurred while starting the application. Please reinstall the application.");
                fatalAlert.showAndWait();
                Platform.exit();
            }
        }
    }


}