package com.samet.music.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;
    
    // Database connection properties
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "musicapp";
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";
    private static final int MAX_POOL_SIZE = 10;

    /**
     * Initialize the database connection and create tables if they don't exist
     */
    public static void initializeDatabase() {
        try {
            // Configure Hikari connection pool
            setupConnectionPool();
            logger.info("Connected to PostgreSQL database");
            
            // Create tables
            createTables();
        } catch (Exception e) {
            logger.error("Database initialization error", e);
        }
    }

    /**
     * Setup the connection pool using HikariCP
     */
    private static void setupConnectionPool() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            
            // Set JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            
            // Connection pool settings
            config.setMaximumPoolSize(MAX_POOL_SIZE);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(30000);
            config.setPoolName("MusicAppConnectionPool");
            
            // Add health check properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            logger.info("Connection pool initialized");
        }
    }

    /**
     * Get database connection from pool
     * @return Connection object
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            setupConnectionPool();
        }
        return dataSource.getConnection();
    }

    /**
     * Close the data source and connection pool
     */
    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private static void createTables() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            // Users table
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Songs table
            statement.execute("CREATE TABLE IF NOT EXISTS songs (" +
                    "id SERIAL PRIMARY KEY," +
                    "title VARCHAR(100) NOT NULL," +
                    "artist VARCHAR(100)," +
                    "album VARCHAR(100)," +
                    "genre VARCHAR(50)," +
                    "year INTEGER," +
                    "duration INTEGER," +
                    "file_path VARCHAR(500)," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Playlists table
            statement.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
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
                    "genre VARCHAR(50)," +
                    "artist VARCHAR(100)," +
                    "weight INTEGER," +
                    "PRIMARY KEY (user_id, genre, artist)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Error creating database tables", e);
        }
    }
} 