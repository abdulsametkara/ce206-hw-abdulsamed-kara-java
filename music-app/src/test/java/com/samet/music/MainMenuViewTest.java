package com.samet.music;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.User;
import com.samet.music.view.MainMenuView;
import com.samet.music.view.MenuView;
import com.samet.music.view.MusicCollectionView;
import com.samet.music.view.PlaylistMenuView;
import com.samet.music.view.MetadataEditingView;
import com.samet.music.view.RecommendationView;
import com.samet.music.view.LoginMenuView;

/**
 * Test class for MainMenuView
 */
public class MainMenuViewTest {

    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @Before
    public void setUp() {
        userController = createTestUserController();
        songController = createTestSongController();
        playlistController = createTestPlaylistController();
        
        // Redirect System.out to capture output
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    private UserController createTestUserController() {
        return new UserController() {
            private boolean isUserLoggedIn = true;
            private User currentUser = null;
            
            @Override
            public boolean isLoggedIn() {
                return isUserLoggedIn;
            }
            
            @Override
            public User getCurrentUser() {
                User user = new User();
                user.setId(1);
                user.setUsername("testuser");
                return user;
            }
            
            @Override
            public boolean loginUser(String username, String password) {
                return true;
            }
            
            @Override
            public boolean registerUser(String username, String password, String email) {
                return true;
            }
            
            @Override
            public void logoutUser() {
                isUserLoggedIn = false;
                currentUser = null;
            }
        };
    }
    
    private SongController createTestSongController() {
        return new SongController(userController) {
            // Mock implementation
        };
    }
    
    private PlaylistController createTestPlaylistController() {
        return new PlaylistController(userController) {
            // Mock implementation
        };
    }
    
    /**
     * Test displaying the main menu
     */
    @Test
    public void testDisplayMainMenu() {
        // Setup
        String input = "5\n"; // Exit/Logout option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be LoginMenuView", nextView instanceof LoginMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain MAIN MENU header", output.contains("MAIN MENU - MUSIC LIBRARY"));
        assertTrue("Output should contain Music Collection option", output.contains("Music Collection"));
        assertTrue("Output should contain Playlists option", output.contains("Playlists"));
        assertTrue("Output should contain Metadata Editing option", output.contains("Metadata Editing"));
        assertTrue("Output should contain Recommendations option", output.contains("Recommendations"));
        assertTrue("Output should contain Logout option", output.contains("Logout"));
    }
    
    /**
     * Test navigation to Music Collection menu
     */
    @Test
    public void testNavigateToMusicCollection() {
        // Setup
        String input = "1\n"; // Music Collection option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be MusicCollectionView", nextView instanceof MusicCollectionView);
    }
    
    /**
     * Test navigation to Playlists menu
     */
    @Test
    public void testNavigateToPlaylists() {
        // Setup
        String input = "2\n"; // Playlists option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be PlaylistMenuView", nextView instanceof PlaylistMenuView);
    }
    
    /**
     * Test navigation to Metadata Editing menu
     */
    @Test
    public void testNavigateToMetadataEditing() {
        // Setup
        String input = "3\n"; // Metadata Editing option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be MetadataEditingView", nextView instanceof MetadataEditingView);
    }
    
    /**
     * Test navigation to Recommendations menu
     */
    @Test
    public void testNavigateToRecommendations() {
        // Setup
        String input = "4\n"; // Recommendations option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be RecommendationView", nextView instanceof RecommendationView);
    }
    
    /**
     * Test logout functionality
     */
    @Test
    public void testLogout() {
        // Setup
        String input = "5\n"; // Logout option
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be LoginMenuView", nextView instanceof LoginMenuView);
        assertFalse("User should be logged out", userController.isLoggedIn());
    }
    
    /**
     * Test handling of invalid input
     */
    @Test
    public void testInvalidInput() {
        // Setup - Enter invalid option
        String input = "99\n"; // Invalid option only
        Scanner scanner = new Scanner(input);
        
        // Execute
        MainMenuView view = new MainMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify - Invalid input should return the same view (MainMenuView)
        assertTrue("Next view should be MainMenuView for invalid input", nextView instanceof MainMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain invalid input message", 
                output.contains("Invalid choice. Please try again."));
    }
} 