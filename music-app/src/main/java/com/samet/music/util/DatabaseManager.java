package com.samet.music.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Database connection management class
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:music.db";
    private static final int MAX_POOL_SIZE = 10;

    // Singleton instance
    private static volatile DatabaseManager instance;

    // Database reset flag
    private boolean shouldResetDatabase = false;

    // Connection pool - static to avoid non-static reference error
    private static final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();

    // Used connections tracking - static to avoid non-static reference error
    private static final List<Connection> usedConnections = new ArrayList<>();

    /**
     * Private constructor
     */
    private DatabaseManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            logger.info("SQLite JDBC driver loaded successfully");

            // Initialize connections
            for (int i = 0; i < MAX_POOL_SIZE / 2; i++) {
                connectionPool.add(createConnection());
            }

            // Initialize database schema
            initializeDatabase();

        } catch (ClassNotFoundException e) {
            logger.error("SQLite JDBC driver not found: {}", e.getMessage(), e);
            throw new RuntimeException("Database driver not found", e);
        } catch (SQLException e) {
            logger.error("Error initializing database: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initializes the database schema
     */
    public void initializeDatabase() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();

            // Create database tables
            createTables(conn);

            logger.info("Database schema successfully initialized");
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }

    /**
     * Creates a new connection
     */
    private static Connection createConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

        Connection connection = DriverManager.getConnection(DB_URL, config.toProperties());
        logger.debug("New database connection created");
        return connection;
    }

    /**
     * Gets a connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();

        if (connection == null || connection.isClosed()) {
            // No connection available or connection closed, create a new one
            connection = createConnection();
        }

        // Track used connection
        usedConnections.add(connection);

        return connection;
    }

    /**
     * Releases a connection back to the pool
     */
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                // Reset auto-commit
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }

                // Clear warnings
                connection.clearWarnings();

                // Remove from used connections and add back to pool
                usedConnections.remove(connection);

                // Only add back if we have capacity and connection is valid
                if (connectionPool.size() < MAX_POOL_SIZE && connection.isValid(1)) {
                    connectionPool.add(connection);
                } else {
                    // Close if too many connections or invalid
                    closeConnection(connection);
                }
            } catch (SQLException e) {
                logger.error("Error releasing connection: {}", e.getMessage(), e);
                closeConnection(connection);
            }
        }
    }

    /**
     * Closes a connection completely
     */
    private static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.debug("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection: {}", e.getMessage(), e);
        }
    }

    /**
     * Closes all connections
     */
    public void closeAllConnections() {
        try {
            // Close used connections
            List<Connection> usedConnectionsCopy = new ArrayList<>(usedConnections);
            for (Connection conn : usedConnectionsCopy) {
                closeConnection(conn);
            }
            usedConnections.clear();

            // Close pool connections
            Connection conn;
            while ((conn = connectionPool.poll()) != null) {
                closeConnection(conn);
            }

            logger.info("All database connections closed");
        } catch (Exception e) {
            logger.error("Error closing connections: {}", e.getMessage(), e);
        }
    }

    /**
     * Sets the database reset flag
     */
    public void setShouldResetDatabase(boolean shouldReset) {
        this.shouldResetDatabase = shouldReset;
    }

    /**
     * Creates database tables
     */
    private static void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Artists table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS artists (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "biography TEXT)"
            );

            // Albums table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS albums (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "artist_id TEXT, " +
                            "release_year INTEGER, " +
                            "genre TEXT, " +
                            "FOREIGN KEY (artist_id) REFERENCES artists(id))"
            );

            // Songs table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS songs (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "artist_id TEXT, " +
                            "album_id TEXT, " +
                            "duration INTEGER, " +
                            "genre TEXT, " +
                            "FOREIGN KEY (artist_id) REFERENCES artists(id), " +
                            "FOREIGN KEY (album_id) REFERENCES albums(id))"
            );

            // Playlists table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                            "id TEXT PRIMARY KEY, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT)"
            );

            // Playlist Songs table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                            "playlist_id TEXT, " +
                            "song_id TEXT, " +
                            "PRIMARY KEY (playlist_id, song_id), " +
                            "FOREIGN KEY (playlist_id) REFERENCES playlists(id), " +
                            "FOREIGN KEY (song_id) REFERENCES songs(id))"
            );

            // Users table (if needed)
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "username TEXT PRIMARY KEY, " +
                            "password TEXT NOT NULL)"
            );
        }
    }
}