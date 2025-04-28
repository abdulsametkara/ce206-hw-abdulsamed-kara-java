package com.samet.music.controller;

import com.samet.music.model.User;
import com.samet.music.service.UserService;
import com.samet.music.dao.UserDAO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * REST controller for user operations
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "API for user management")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private User currentUser;
    private UserDAO userDAO;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    public UserController() {
        this.userService = null;
        this.userDAO = new UserDAO();
    }
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponse(responseCode = "200", description = "Users found")
    public ResponseEntity<List<User>> getAllUsersRest() {
        logger.debug("REST request to get all users");
        List<User> users = getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieves a user by their ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        logger.debug("REST request to get user with id: {}", id);
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.debug("REST request to create user: {}", user);
        
        // Check if username already exists
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Check if email already exists (if email is provided)
        if (user.getEmail() != null && !user.getEmail().isEmpty() && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        
        User createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates an existing user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @Valid @RequestBody User user) {
        logger.debug("REST request to update user: {}", user);
        
        // Check if user exists
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Set the id to ensure we're updating the correct user
        user.setId(id);
        
        User updatedUser = userService.update(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        logger.debug("REST request to delete user with id: {}", id);
        
        // Check if user exists
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate a user", description = "Authenticates a user with username and password")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<User> authenticate(@RequestBody User credentials) {
        logger.debug("REST request to authenticate user: {}", credentials.getUsername());
        
        Optional<User> authenticatedUser = userService.authenticate(
                credentials.getUsername(), credentials.getPassword());
        
        return authenticatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    public boolean loginUser(String username, String password) {
        if (userService != null) {
            Optional<User> user = userService.authenticate(username, password);
            if (user.isPresent()) {
                this.currentUser = user.get();
                return true;
            }
            return false;
        } else {
            Optional<User> user = userDAO.authenticate(username, password);
            if (user.isPresent()) {
                this.currentUser = user.get();
                return true;
            }
            return false;
        }
    }
    
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
    
    /**
     * Set the current user
     * @param user the user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Logout the current user
     */
    public void logoutUser() {
        this.currentUser = null;
    }
    
    /**
     * Update user profile
     * @param email new email (null if not changing)
     * @param password new password (null if not changing)
     * @return true if update successful, false otherwise
     */
    public boolean updateUserProfile(String email, String password) {
        if (currentUser == null) {
            return false;
        }
        
        if (email != null) {
            currentUser.setEmail(email);
        }
        
        if (password != null) {
            currentUser.setPassword(password);
        }
        
        return true;
    }
    
    /**
     * Delete current user account
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAccount() {
        if (currentUser == null) {
            return false;
        }
        
        this.currentUser = null;
        return true;
    }
    
    /**
     * Get all users (for admin purposes)
     * @return list of all users
     */
    public List<User> getAllUsers() {
        if (userService != null) {
            return userService.findAll();
        } else if (userDAO != null) {
            return userDAO.findAll();
        }
        return new ArrayList<>();
    }
    
    /**
     * Create a UserDAO instance
     * @return the created UserDAO instance
     */
    protected UserDAO createUserDAO() {
        return new UserDAO();
    }
    
    /**
     * Register a new user - JavaFX version
     * @param username the username
     * @param password the password
     * @param email the email
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email) {
        if (userService != null) {
            // Spring version
            if (userService.existsByUsername(username)) {
                return false;
            }
            User user = new User(username, password, email);
            User createdUser = userService.create(user);
            return createdUser != null;
        } else {
            // JavaFX version using DAO
            Optional<User> existingUser = userDAO.findByUsername(username);
            if (existingUser.isPresent()) {
                return false;
            }
            User user = new User(username, password, email);
            User createdUser = userDAO.create(user);
            return createdUser != null;
        }
    }
} 