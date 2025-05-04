package com.samet.music.view;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for menu views with common functionality
 */
public abstract class MenuView {
    private static final Logger logger = LoggerFactory.getLogger(MenuView.class);
    protected Scanner scanner;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     */
    public MenuView(Scanner scanner) {
        this.scanner = scanner;
    }
    
    /**
     * Display this menu and handle user interaction
     * @return the next menu to display, or null to exit
     */
    public abstract MenuView display();
    
    /**
     * Display a menu header
     * @param title the menu title
     */
    protected void displayHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" " + title);
        System.out.println("=".repeat(50));
    }
    
    /**
     * Display a menu option
     * @param key the key to press
     * @param description the option description
     */
    protected void displayOption(String key, String description) {
        System.out.printf("  %-4s %s%n", key + ")", description);
    }
    
    /**
     * Display a menu footer
     */
    protected void displayFooter() {
        System.out.println("-".repeat(50));
    }
    
    /**
     * Wait for user to press Enter
     */
    protected void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Get a non-empty string input from the user
     * @param prompt the prompt to display
     * @return the user input
     */
    protected String getStringInput(String prompt) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        }
        return input;
    }
    
    /**
     * Get an optional string input from the user (can be empty)
     * @param prompt the prompt to display
     * @return the user input or empty string
     */
    protected String getOptionalStringInput(String prompt) {
        System.out.print(prompt + " (optional): ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Get an integer input from the user
     * @param prompt the prompt to display
     * @return the user input as an integer
     */
    protected int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    /**
     * Get an integer input from the user with validation for minimum value
     * @param prompt the prompt to display
     * @param min the minimum allowed value
     * @return the user input as an integer
     */
    protected int getIntInput(String prompt, int min) {
        while (true) {
            int input = getIntInput(prompt);
            if (input >= min) {
                return input;
            }
            System.out.printf("Please enter a number greater than or equal to %d%n", min);
        }
    }
    
    /**
     * Get an integer input from the user with validation for range
     * @param prompt the prompt to display
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the user input as an integer
     */
    protected int getIntInput(String prompt, int min, int max) {
        while (true) {
            int input = getIntInput(prompt);
            if (input >= min && input <= max) {
                return input;
            }
            System.out.printf("Please enter a number between %d and %d%n", min, max);
        }
    }
    
    /**
     * Get a yes/no input from the user
     * @param prompt the prompt to display
     * @return true for yes, false for no
     */
    protected boolean getYesNoInput(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("Please answer with 'y' or 'n'");
        }
    }
    
    /**
     * Display an error message
     * @param message the error message
     */
    protected void displayError(String message) {
        System.out.println("\n[ERROR] " + message);
        logger.error(message);
    }
    
    /**
     * Display a success message
     * @param message the success message
     */
    protected void displaySuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
        logger.info(message);
    }
    
    /**
     * Display information message
     * @param message the info message
     */
    protected void displayInfo(String message) {
        System.out.println("\n[INFO] " + message);
    }
    
    /**
     * Clear the console screen
     */
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
} 