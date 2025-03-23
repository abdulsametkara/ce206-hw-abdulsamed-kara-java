package com.samet.music.ui.gui;

import com.samet.music.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicLibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Veritabanını başlat
        DatabaseUtil.initializeDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        primaryStage.setTitle("Music Library");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}