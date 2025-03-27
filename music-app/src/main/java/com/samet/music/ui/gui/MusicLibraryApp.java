package com.samet.music.ui.gui;

import com.samet.music.monitoring.MetricsCollector;
import com.samet.music.util.DatabaseUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicLibraryApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MusicLibraryApp.class);
    private MetricsCollector metricsCollector;

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Music Library Application");

        // Prometheus metrik toplayıcısını başlat
        metricsCollector = MetricsCollector.getInstance();

        try {
            // Veritabanını başlat
            DatabaseUtil.initializeDatabase();
            logger.info("Database initialized successfully");

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            primaryStage.setTitle("Music Library - Login");
            primaryStage.setScene(new Scene(root, 400, 300));
            primaryStage.show();
            logger.info("Application UI initialized and shown");
        } catch (Exception e) {
            logger.error("Error during application startup", e);

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
            logger.info("Database reset and reinitialized");

            try {
                // Tekrar dene
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
                primaryStage.setTitle("Music Library");
                primaryStage.setScene(new Scene(root, 800, 600));
                primaryStage.show();
            } catch (Exception ex) {
                logger.error("Fatal error during application startup", ex);
                Alert fatalAlert = new Alert(Alert.AlertType.ERROR);
                fatalAlert.setTitle("Fatal Error");
                fatalAlert.setHeaderText("Application Could Not Start");
                fatalAlert.setContentText("A fatal error occurred while starting the application. Please reinstall the application.");
                fatalAlert.showAndWait();
                Platform.exit();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.info("Shutting down application");

        // Prometheus HTTP sunucusunu kapat
        if (metricsCollector != null) {
            metricsCollector.shutdown();
        }
    }
}