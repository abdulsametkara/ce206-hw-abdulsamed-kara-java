package com.samet.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
import com.samet.music.util.DatabaseUtil;
import com.samet.music.view.fx.LoginViewFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application for the Music Library
 */
public class MusicAppFX extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MusicAppFX.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseUtil.initializeDatabase();
            logger.info("Database initialized");
            
            // Initialize controller - keeping the old controller for now
            // Later should be replaced with a proper Spring integration or DI
            UserController userController = new UserController();
            
            // Setup primary stage
            primaryStage.setTitle("Music Library Organizer");
            
            // Create login view
            LoginViewFX loginView = new LoginViewFX(primaryStage, userController);
            
            // Setup and display scene
            Scene scene = new Scene(loginView.getRoot(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            
            logger.info("JavaFX application started");
        } catch (Exception e) {
            logger.error("Error starting JavaFX application", e);
        }
    }
    
    @Override
    public void stop() {
        // Close database connection
        DatabaseUtil.closeConnection();
        logger.info("JavaFX application stopped, resources released");
    }
} 