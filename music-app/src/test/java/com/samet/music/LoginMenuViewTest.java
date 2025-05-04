package com.samet.music;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.samet.music.controller.UserController;
import com.samet.music.model.User;
import com.samet.music.view.LoginMenuView;
import com.samet.music.view.MainMenuView;
import com.samet.music.view.MenuView;

/**
 * Test class for LoginMenuView
 */
public class LoginMenuViewTest {

    private UserController userController;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @Before
    public void setUp() {
        userController = createTestUserController();
        
        // Redirect System.out to capture output
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    /**
     * Test UserController oluşturur
     */
    private UserController createTestUserController() {
        return new UserController() {
            private boolean isUserLoggedIn = false;
            private User currentUser = null;
            
            @Override
            public boolean isLoggedIn() {
                return isUserLoggedIn;
            }
            
            @Override
            public User getCurrentUser() {
                return currentUser;
            }
            
            @Override
            public boolean loginUser(String username, String password) {
                if (username.equals("testuser") && password.equals("password")) {
                    isUserLoggedIn = true;
                    currentUser = new User();
                    currentUser.setId(1);
                    currentUser.setUsername(username);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean registerUser(String username, String password, String email) {
                if (username.equals("existinguser")) {
                    return false; // Simulating a username that already exists
                }
                return true;
            }
            
            @Override
            public void logoutUser() {
                isUserLoggedIn = false;
                currentUser = null;
            }
        };
    }
    
    /**
     * Test displaying the login menu
     */
    @Test
    public void testDisplayLoginMenu() {
        // Setup
        String input = "3\n"; // Exit program
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain MAIN MENU header", output.contains("MAIN MENU"));
        assertTrue("Output should contain Login option", output.contains("Login"));
        assertTrue("Output should contain Register option", output.contains("Register"));
        assertTrue("Output should contain Exit Program option", output.contains("Exit Program"));
    }
    
    /**
     * Test successful login
     */
    @Test
    public void testSuccessfulLogin() {
        // Setup - First option is login, then provide credentials
        String input = "1\ntestuser\npassword\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue("Next view should be MainMenuView", nextView instanceof MainMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain LOGIN header", output.contains("LOGIN"));
        assertTrue("Output should contain success message", output.contains("Login successful"));
    }
    
    /**
     * Test failed login
     */
    @Test
    public void testFailedLogin() {
        // Setup - First option is login, then provide incorrect credentials
        String input = "1\ntestuser\nwrongpassword\n3\n"; // Login, then exit after failure
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify the menu returned should still be LoginMenuView since login failed
        assertTrue("Next view should still be LoginMenuView", nextView instanceof LoginMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain LOGIN header", output.contains("LOGIN"));
        assertTrue("Output should contain failure message", output.contains("Login failed"));
    }
    
    /**
     * Test successful registration
     */
    @Test
    public void testSuccessfulRegistration() {
        // Setup - Second option is register, then provide new credentials
        String input = "2\nnewuser\npassword123\ntest@example.com\n3\n"; // Register, then exit
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify the menu returned should still be LoginMenuView after registration
        assertTrue("Next view should still be LoginMenuView", nextView instanceof LoginMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain REGISTER header", output.contains("REGISTER"));
        assertTrue("Output should contain success message", output.contains("Registration successful"));
    }
    
    /**
     * Test failed registration
     */
    @Test
    public void testFailedRegistration() {
        // Setup - Second option is register, then provide existing username
        String input = "2\nexistinguser\npassword123\ntest@example.com\n3\n"; // Register, then exit
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify the menu returned should still be LoginMenuView after failed registration
        assertTrue("Next view should still be LoginMenuView", nextView instanceof LoginMenuView);
        
        String output = outputStream.toString();
        assertTrue("Output should contain REGISTER header", output.contains("REGISTER"));
        assertTrue("Output should contain failure message", output.contains("Registration failed"));
    }
    
    /**
     * Test exit program
     */
    @Test
    public void testExitProgram() {
        // Setup - Third option is exit
        String input = "3\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        LoginMenuView view = new LoginMenuView(scanner, userController);
        MenuView nextView = view.display();
        
        // Verify the menu returned should be null when exiting
        assertNull("Next view should be null when exiting", nextView);
    }
} 