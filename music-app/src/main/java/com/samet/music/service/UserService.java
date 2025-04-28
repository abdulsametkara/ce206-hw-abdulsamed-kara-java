package com.samet.music.service;

import com.samet.music.model.User;
import com.samet.music.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for user operations
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Find all users
     * @return a list of all users
     */
    public List<User> findAll() {
        logger.debug("Finding all users");
        return userRepository.findAll();
    }
    
    /**
     * Find a user by id
     * @param id the user id
     * @return an Optional containing the user if found
     */
    public Optional<User> findById(Integer id) {
        logger.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Find a user by username
     * @param username the username
     * @return an Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }
    
    /**
     * Find a user by email
     * @param email the email
     * @return an Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Check if a username exists
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if an email exists
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     */
    @Transactional
    public User create(User user) {
        logger.debug("Creating new user: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * Update an existing user
     * @param user the user to update
     * @return the updated user
     */
    @Transactional
    public User update(User user) {
        logger.debug("Updating user: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * Delete a user by id
     * @param id the id of the user to delete
     */
    @Transactional
    public void delete(Integer id) {
        logger.debug("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }
    
    /**
     * Authenticate a user with username and password
     * @param username the username
     * @param password the password
     * @return an Optional containing the user if authentication successful
     */
    public Optional<User> authenticate(String username, String password) {
        logger.debug("Authenticating user: {}", username);
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }
} 