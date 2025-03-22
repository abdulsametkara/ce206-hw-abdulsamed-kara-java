package com.samet.music.dao;

import com.samet.music.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for user operations
 */
public class UserDAO {
    private static final Object LOCK = new Object();

    /**
     * Create users table if not exists
     */
    public void createTable() {
        synchronized (LOCK) {

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL)";

            try (Connection conn = DatabaseUtil.getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute(sql);
                System.out.println("Users table created or already exists.");

            } catch (SQLException e) {
                System.err.println("Error creating users table: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean saveUser(String username, String password) {
        synchronized (LOCK) {

            String sql = "INSERT OR REPLACE INTO users (username, password) VALUES (?, ?)";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                pstmt.setString(2, password);

                int affected = pstmt.executeUpdate();
                return affected > 0;

            } catch (SQLException e) {
                System.err.println("Error saving user: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    public String getPassword(String username) {
        synchronized (LOCK) {

            String sql = "SELECT password FROM users WHERE username = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password");
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error getting user: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }

    public boolean userExists(String username) {
        synchronized (LOCK) {

            return getPassword(username) != null;
        }
    }

    public boolean deleteUser(String username) {
        synchronized (LOCK) {

            String sql = "DELETE FROM users WHERE username = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                int affected = pstmt.executeUpdate();
                return affected > 0;

            } catch (SQLException e) {
                System.err.println("Error deleting user: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    public Map<String, String> getAllUsers() {
        synchronized (LOCK) {

            Map<String, String> users = new HashMap<>();
        String sql = "SELECT username, password FROM users";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.put(rs.getString("username"), rs.getString("password"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
        }
    }
}