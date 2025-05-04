package com.samet.music.view;

import java.util.Scanner;

import com.samet.music.controller.UserController;
import com.samet.music.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for user settings
 */
public class UserSettingsView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(UserSettingsView.class);
    private UserController userController;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     * @param userController the user controller
     */
    public UserSettingsView(Scanner scanner, UserController userController) {
        super(scanner);
        this.userController = userController;
    }
    
    @Override
    public MenuView display() {
        if (!userController.isLoggedIn()) {
            logger.warn("No user logged in, returning to login menu");
            return new LoginMenuView(scanner, userController);
        }
        
        User currentUser = userController.getCurrentUser();
        
        displayHeader("USER SETTINGS");
        
        System.out.println("\nCurrent profile:");
        System.out.println("  Username: " + currentUser.getUsername());
        System.out.println("  Email: " + currentUser.getEmail());
        System.out.println("  Account created: " + currentUser.getCreatedAt());
        
        displayOption("1", "Update Profile");
        displayOption("2", "Change Password");
        displayOption("3", "Delete Account");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                updateProfile();
                return this;
            case "2":
                changePassword();
                return this;
            case "3":
                if (deleteAccount()) {
                    return new LoginMenuView(scanner, userController);
                }
                return this;
            case "0":
                return new MainMenuView(scanner, userController);
            default:
                System.out.println("Invalid choice. Please try again.");
                return this;
        }
    }
    
    /**
     * Update user profile
     */
    private void updateProfile() {
        displayHeader("UPDATE PROFILE");
        
        User currentUser = userController.getCurrentUser();
        
        System.out.println("\nCurrent email: " + currentUser.getEmail());
        System.out.print("New email (leave blank to keep current): ");
        String email = scanner.nextLine().trim();
        
        if (email.isEmpty()) {
            displayInfo("No changes made to profile.");
            waitForEnter();
            return;
        }
        
        boolean updated = userController.updateUserProfile(email, null);
        
        if (updated) {
            displaySuccess("Profile updated successfully!");
        } else {
            displayError("Failed to update profile.");
        }
        
        waitForEnter();
    }
    
    /**
     * Change user password
     */
    private void changePassword() {
        displayHeader("CHANGE PASSWORD");
        
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine().trim();
        
        User currentUser = userController.getCurrentUser();
        
        // Verify current password
        if (!currentUser.getPassword().equals(currentPassword)) {
            displayError("Current password is incorrect.");
            waitForEnter();
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine().trim();
        
        if (newPassword.isEmpty()) {
            displayInfo("Password change cancelled. New password cannot be empty.");
            waitForEnter();
            return;
        }
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine().trim();
        
        if (!newPassword.equals(confirmPassword)) {
            displayError("Passwords do not match. Password not changed.");
            waitForEnter();
            return;
        }
        
        boolean updated = userController.updateUserProfile(null, newPassword);
        
        if (updated) {
            displaySuccess("Password changed successfully!");
        } else {
            displayError("Failed to change password.");
        }
        
        waitForEnter();
    }
    
    /**
     * Delete user account
     * @return true if account was deleted, false otherwise
     */
    private boolean deleteAccount() {
        displayHeader("DELETE ACCOUNT");
        
        System.out.println("\nWARNING: This will permanently delete your account and all your data!");
        System.out.println("This action cannot be undone.");
        
        if (!getYesNoInput("Are you sure you want to delete your account?")) {
            displayInfo("Account deletion cancelled.");
            waitForEnter();
            return false;
        }
        
        System.out.print("Enter your password to confirm: ");
        String password = scanner.nextLine().trim();
        
        User currentUser = userController.getCurrentUser();
        
        // Verify password
        if (!currentUser.getPassword().equals(password)) {
            displayError("Password is incorrect. Account not deleted.");
            waitForEnter();
            return false;
        }
        
        boolean deleted = userController.deleteAccount();
        
        if (deleted) {
            displaySuccess("Your account has been deleted. Goodbye!");
            waitForEnter();
            return true;
        } else {
            displayError("Failed to delete account.");
            waitForEnter();
            return false;
        }
    }
} 