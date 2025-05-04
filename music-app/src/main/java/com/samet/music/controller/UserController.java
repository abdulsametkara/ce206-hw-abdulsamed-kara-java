package com.samet.music.controller;

import com.samet.music.dao.UserDAO;
import com.samet.music.model.User;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for user operations
 */
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserDAO userDAO;
    private User currentUser;

    /**
     * Constructor
     */
    public UserController() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new user
     * @param username the username
     * @param password the password
     * @param email the email
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email) {
        // Check if username already exists
        if (userDAO.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            return false;
        }
        
        // Create new user
        User user = new User(username, password, email);
        User createdUser = userDAO.create(user);
        
        return createdUser != null;
    }

    /**
     * Login user
     * @param username the username
     * @param password the password
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        Optional<User> user = userDAO.authenticate(username, password);
        
        if (user.isPresent()) {
            this.currentUser = user.get();
            logger.info("User logged in: {}", username);
            return true;
        }
        
        logger.warn("Login failed for user: {}", username);
        return false;
    }

    /**
     * Logout current user
     */
    public void logoutUser() {
        if (this.currentUser != null) {
            logger.info("User logged out: {}", this.currentUser.getUsername());
            this.currentUser = null;
        }
    }

    /**
     * Update user profile
     * @param email the new email
     * @param password the new password (null if not changing)
     * @return true if update successful, false otherwise
     */
    public boolean updateUserProfile(String email, String password) {
        if (this.currentUser == null) {
            logger.warn("No user logged in to update profile");
            return false;
        }
        
        // Update user data
        if (email != null && !email.isEmpty()) {
            this.currentUser.setEmail(email);
        }
        
        if (password != null && !password.isEmpty()) {
            this.currentUser.setPassword(password);
        }
        
        boolean updated = userDAO.update(this.currentUser);
        
        if (updated) {
            logger.info("User profile updated: {}", this.currentUser.getUsername());
        } else {
            logger.warn("Failed to update user profile: {}", this.currentUser.getUsername());
        }
        
        return updated;
    }

    /**
     * Delete current user account
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAccount() {
        if (this.currentUser == null) {
            logger.warn("No user logged in to delete account");
            return false;
        }
        
        int userId = this.currentUser.getId();
        boolean deleted = userDAO.delete(userId);
        
        if (deleted) {
            logger.info("User account deleted: {}", this.currentUser.getUsername());
            this.currentUser = null;
        } else {
            logger.warn("Failed to delete user account: {}", this.currentUser.getUsername());
        }
        
        return deleted;
    }

    /**
     * Get all users (admin function)
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Get current logged in user
     * @return the current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
} 