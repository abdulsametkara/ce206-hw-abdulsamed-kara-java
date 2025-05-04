package com.samet.music.view;

import java.util.Scanner;

import com.samet.music.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for the login and registration menu
 */
public class LoginMenuView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(LoginMenuView.class);
    private UserController userController;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     * @param userController the user controller
     */
    public LoginMenuView(Scanner scanner, UserController userController) {
        super(scanner);
        this.userController = userController;
    }
    
    @Override
    public MenuView display() {
        displayHeader("MAIN MENU");
        
        displayOption("1", "Login");
        displayOption("2", "Register");
        displayOption("3", "Exit Program");
        
        displayFooter();
        
        System.out.print("Please enter a number: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                return login();
            case "2":
                return register();
            case "3":
                logger.info("User chose to exit the application");
                return null;
            default:
                System.out.println("Invalid choice. Please try again.");
                return this;
        }
    }
    
    /**
     * Handle user login
     * @return the next menu to display
     */
    private MenuView login() {
        displayHeader("LOGIN");
        
        String username = getStringInput("Username");
        String password = getStringInput("Password");
        
        boolean success = userController.loginUser(username, password);
        
        if (success) {
            displaySuccess("Login successful. Welcome, " + username + "!");
            logger.info("User '{}' logged in successfully", username);
            return new MainMenuView(scanner, userController);
        } else {
            displayError("Login failed. Invalid username or password.");
            waitForEnter();
            return this;
        }
    }
    
    /**
     * Handle user registration
     * @return the next menu to display
     */
    private MenuView register() {
        displayHeader("REGISTER");
        
        String username = getStringInput("Username");
        String password = getStringInput("Password");
        String email = getStringInput("Email");
        
        boolean success = userController.registerUser(username, password, email);
        
        if (success) {
            displaySuccess("Registration successful! You can now login.");
            logger.info("New user '{}' registered successfully", username);
            waitForEnter();
            return this;
        } else {
            displayError("Registration failed. Username may already exist.");
            waitForEnter();
            return this;
        }
    }
} 