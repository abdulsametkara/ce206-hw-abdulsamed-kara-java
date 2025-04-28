package com.samet.music.dao;

import com.samet.music.model.User;
import com.samet.music.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for User entities
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Create a new user in the database
     * @param user the user to create
     * @return the created user with id
     */
    public User create(User user) {
        // SQLite desteklemiyor olabilir, alternatif yöntem kullanacağız
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        String idSql = "SELECT last_insert_rowid() as id";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Auto-commit'i devre dışı bırak
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Son eklenen satırın ID'sini al
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(idSql)) {
                        if (rs.next()) {
                            user.setId(rs.getInt("id"));
                            conn.commit();
                            logger.info("User created successfully with ID: {}", user.getId());
                            return user;
                        }
                    }
                }
                
                conn.rollback();
                logger.error("Failed to create user, no ID obtained.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating user", e);
        }
        
        return null;
    }

    /**
     * Get a user by id
     * @param id the user id
     * @return an Optional containing the user if found
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID", e);
        }
        
        return Optional.empty();
    }

    /**
     * Get a user by username
     * @param username the username
     * @return an Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username", e);
        }
        
        return Optional.empty();
    }

    /**
     * Get all users
     * @return a list of all users
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
        }
        
        return users;
    }

    /**
     * Update a user in the database
     * @param user the user to update
     * @return true if the update was successful, false otherwise
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("User updated successfully with ID: {}", user.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating user", e);
        }
        
        return false;
    }

    /**
     * Delete a user from the database
     * @param id the id of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("User deleted successfully with ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
        }
        
        return false;
    }

    /**
     * Authenticate a user with username and password
     * @param username the username
     * @param password the password
     * @return an Optional containing the user if authentication successful
     */
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    logger.info("User authenticated successfully: {}", username);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error authenticating user", e);
        }
        
        logger.info("Authentication failed for user: {}", username);
        return Optional.empty();
    }

    /**
     * Map a ResultSet to a User object
     * @param rs the ResultSet
     * @return the User object
     * @throws SQLException if a database access error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        
        // Convert timestamp to LocalDateTime
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
        
        return new User(id, username, password, email, createdAt);
    }
} 