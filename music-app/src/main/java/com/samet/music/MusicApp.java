package com.samet.music;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samet.music.controller.UserController;
import com.samet.music.util.DatabaseUtil;
import com.samet.music.view.LoginMenuView;
import com.samet.music.view.MenuView;

/**
 * Main application class
 */
public class MusicApp {
    private static final Logger logger = LoggerFactory.getLogger(MusicApp.class);

    public static void main(String[] args) {
        logger.info("Music Library Organizer starting...");
        
        // Initialize database
        DatabaseUtil.initializeDatabase();
        logger.info("Database initialized");
        
        // Initialize controllers
        UserController userController = new UserController();
        
        // Create scanner for user input
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Start with login menu
            MenuView currentMenu = new LoginMenuView(scanner, userController);
            
            // Menu navigation loop
            while (currentMenu != null) {
                currentMenu = currentMenu.display();
            }
            
            logger.info("Application exiting...");
            System.out.println("Thank you for using Music Library Organizer. Goodbye!");
        } catch (Exception e) {
            logger.error("Unexpected error in application", e);
            System.out.println("An unexpected error occurred. Please check the logs.");
        } finally {
            // Close scanner and database connection
            scanner.close();
            DatabaseUtil.closeConnection();
            logger.info("Resources closed");
        }
    }
}
