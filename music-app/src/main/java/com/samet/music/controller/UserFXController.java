package com.samet.music.controller;

import com.samet.music.model.User;
import com.samet.music.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Controller for user operations in JavaFX UI
 */
@Component
public class UserFXController {
    private static final Logger logger = LoggerFactory.getLogger(UserFXController.class);
    
    private final UserService userService;
    private User currentUser;
    
    @Autowired
    public UserFXController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Login user
     * @param username the username
     * @param password the password
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        Optional<User> user = userService.authenticate(username, password);
        
        if (user.isPresent()) {
            this.currentUser = user.get();
            logger.info("User logged in: {}", username);
            return true;
        }
        
        logger.warn("Login failed for user: {}", username);
        return false;
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
        if (userService.existsByUsername(username)) {
            logger.warn("Username already exists: {}", username);
            return false;
        }
        
        // Create new user
        User user = new User(username, password, email);
        User createdUser = userService.create(user);
        
        return createdUser != null;
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