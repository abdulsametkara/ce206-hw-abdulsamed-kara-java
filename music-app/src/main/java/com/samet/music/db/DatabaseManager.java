package com.samet.music.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;

/**
 * Manages database connections for the music application
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile DatabaseConnection dbConnection;
    private static volatile DatabaseManager instance;
    private boolean shouldResetDatabase = false;

    /**
     * Private constructor to prevent direct instantiation
     */
    private DatabaseManager() {
        // Private constructor
    }

    /**
     * Returns the singleton instance of DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Gets a database connection
     * @return DatabaseConnection instance
     * @throws SQLException if connection fails
     */
    public DatabaseConnection getConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            synchronized (DatabaseManager.class) {
                if (dbConnection == null || dbConnection.isClosed()) {
                    dbConnection = new DatabaseConnection("jdbc:sqlite:music.db");
                    logger.info("Database connection established");
                }
            }
        }
        return dbConnection;
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        if (dbConnection != null) {
            try {
                if (!dbConnection.isClosed()) {
                    dbConnection.closeConnection();
                    logger.info("Database connection closed");
                }
                dbConnection = null;
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Sets whether the database should be reset
     * @param shouldReset true if database should be reset
     */
    public void setShouldResetDatabase(boolean shouldReset) {
        this.shouldResetDatabase = shouldReset;
    }

    /**
     * Initializes the database
     * @throws SQLException if initialization fails
     */
    public void initializeDatabase() throws SQLException {
        if (shouldResetDatabase) {
            closeConnection();
            shouldResetDatabase = false;
        }
        getConnection();
    }

    /**
     * Closes all database connections
     */
    public void closeAllConnections() {
        closeConnection();
    }
} 