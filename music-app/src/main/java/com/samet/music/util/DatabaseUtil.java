package com.samet.music.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String DB_URL = "jdbc:sqlite:musiclibrary.db";
    private static Connection connection;

    /**
     * Initialize the database connection and create tables if they don't exist
     */
    public static void initializeDatabase() {
        try {
            // Create database connection
            connection = DriverManager.getConnection(DB_URL);
            logger.info("Connected to SQLite database");
            
            // Create tables
            createTables();
        } catch (SQLException e) {
            logger.error("Database initialization error", e);
        }
    }

    /**
     * Get database connection
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            logger.error("Error getting database connection", e);
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private static void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Users table
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL," +
                    "email TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Songs table
            statement.execute("CREATE TABLE IF NOT EXISTS songs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "artist TEXT," +
                    "album TEXT," +
                    "genre TEXT," +
                    "year INTEGER," +
                    "duration INTEGER," +
                    "file_path TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Playlists table
            statement.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Playlist_songs (many-to-many relationship)
            statement.execute("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id INTEGER," +
                    "song_id INTEGER," +
                    "position INTEGER," +
                    "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (playlist_id, song_id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id))");
            
            // User_preferences for recommendations
            statement.execute("CREATE TABLE IF NOT EXISTS user_preferences (" +
                    "user_id INTEGER," +
                    "genre TEXT," +
                    "artist TEXT," +
                    "weight INTEGER," +
                    "PRIMARY KEY (user_id, genre, artist)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Error creating database tables", e);
        }
    }
} 