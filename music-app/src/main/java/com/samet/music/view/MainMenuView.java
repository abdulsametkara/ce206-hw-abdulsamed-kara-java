package com.samet.music.view;

import java.util.Scanner;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for the main menu
 */
public class MainMenuView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuView.class);
    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     * @param userController the user controller
     */
    public MainMenuView(Scanner scanner, UserController userController) {
        super(scanner);
        this.userController = userController;
        this.songController = new SongController(userController);
        this.playlistController = new PlaylistController(userController);
    }
    
    @Override
    public MenuView display() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("No user logged in, returning to login menu");
            return new LoginMenuView(scanner, userController);
        }
        
        displayHeader("MAIN MENU - MUSIC LIBRARY");
        
        System.out.println("\nLogged in as: " + currentUser.getUsername());
        
        displayOption("1", "Music Collection");
        displayOption("2", "Playlists");
        displayOption("3", "Metadata Editing");
        displayOption("4", "Recommendations");
        displayOption("5", "Logout");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                return new MusicCollectionView(scanner, userController, songController, playlistController);
            case "2":
                return new PlaylistMenuView(scanner, userController, songController, playlistController);
            case "3":
                return new MetadataEditingView(scanner, userController, songController);
            case "4":
                return new RecommendationView(scanner, userController, songController, playlistController);
            case "5":
                userController.logoutUser();
                logger.info("User logged out");
                return new LoginMenuView(scanner, userController);
            default:
                System.out.println("Invalid choice. Please try again.");
                return this;
        }
    }
} 