package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for test database setup
 */
public class DatabaseTestSetup {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestSetup.class);
    
    /**
     * Setup a clean test database
     * @param dbConnection Database connection
     * @return true if setup was successful
     */
    public static boolean setupTestDatabase(DatabaseConnection dbConnection) {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // SQLite ayarlarını yap
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = OFF");
                pragmaStmt.execute("PRAGMA journal_mode = DELETE");
                pragmaStmt.execute("PRAGMA synchronous = OFF");
                pragmaStmt.execute("PRAGMA busy_timeout = 30000");
            }
            
            stmt = conn.createStatement();
            
            // Mevcut tabloları temizle
            stmt.execute("DROP TABLE IF EXISTS playlist_songs");
            stmt.execute("DROP TABLE IF EXISTS playlists");
            stmt.execute("DROP TABLE IF EXISTS songs");
            stmt.execute("DROP TABLE IF EXISTS albums");
            stmt.execute("DROP TABLE IF EXISTS artists");
            stmt.execute("DROP TABLE IF EXISTS users");
            
            // Tabloları oluştur
            createArtistsTable(dbConnection);
            createAlbumsTable(dbConnection);
            createSongsTable(dbConnection);
            createPlaylistsTable(dbConnection);
            createUsersTable(dbConnection);
            
            // SQLite ayarlarını geri yükle - Test için DELETE modunda kalalım
            try (Statement pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON");
                pragmaStmt.execute("PRAGMA journal_mode = DELETE"); // WAL yerine DELETE kullanıyoruz
                pragmaStmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            logger.info("Test database setup completed successfully");
            return true;
        } catch (SQLException e) {
            logger.error("Failed to setup test database: {}", e.getMessage(), e);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                // Connection kapatma işlemini yapma, çünkü DatabaseConnection sınıfı zaten yönetiyor
            } catch (SQLException e) {
                logger.error("Error closing database resources", e);
            }
        }
    }
    
    private static void createArtistsTable(DatabaseConnection dbConnection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS artists (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL)";
        
        dbConnection.executeUpdate(sql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating artists table: {}", e.getMessage());
                return false;
            }
        });
    }
    
    private static void createAlbumsTable(DatabaseConnection dbConnection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS albums (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "artist_id TEXT," +
                    "release_year INTEGER," +
                    "genre TEXT," +
                    "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE)";
        
        dbConnection.executeUpdate(sql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating albums table: {}", e.getMessage());
                return false;
            }
        });
    }
    
    private static void createSongsTable(DatabaseConnection dbConnection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "artist_id TEXT," +
                    "album_id TEXT," +
                    "duration INTEGER," +
                    "genre TEXT," +
                    "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE)";
        
        dbConnection.executeUpdate(sql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating songs table: {}", e.getMessage());
                return false;
            }
        });
    }
    
    private static void createPlaylistsTable(DatabaseConnection dbConnection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS playlists (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "description TEXT)";
        
        dbConnection.executeUpdate(sql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating playlists table: {}", e.getMessage());
                return false;
            }
        });
        
        String playlistSongsSql = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id TEXT," +
                    "song_id TEXT," +
                    "PRIMARY KEY (playlist_id, song_id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE)";
        
        dbConnection.executeUpdate(playlistSongsSql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating playlist_songs table: {}", e.getMessage());
                return false;
            }
        });
    }
    
    private static void createUsersTable(DatabaseConnection dbConnection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)";
        
        dbConnection.executeUpdate(sql, stmt -> {
            try {
                stmt.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Error creating users table: {}", e.getMessage());
                return false;
            }
        });
    }

    public void setupTestDatabase() {
        Connection connection = null;
        Statement stmt = null;
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (!success && retryCount < maxRetries) {
            try {
                connection = DatabaseConnection.getConnection();
                stmt = connection.createStatement();

                // Disable foreign key checks and set journal mode to DELETE
                stmt.execute("PRAGMA foreign_keys = OFF");
                stmt.execute("PRAGMA journal_mode = DELETE");
                stmt.execute("PRAGMA synchronous = OFF");
                stmt.execute("PRAGMA busy_timeout = 30000");

                // Drop existing tables
                stmt.execute("DROP TABLE IF EXISTS playlist_songs");
                stmt.execute("DROP TABLE IF EXISTS playlists");
                stmt.execute("DROP TABLE IF EXISTS songs");
                stmt.execute("DROP TABLE IF EXISTS albums");
                stmt.execute("DROP TABLE IF EXISTS artists");
                stmt.execute("DROP TABLE IF EXISTS users");

                // Re-enable foreign key checks but keep journal mode as DELETE
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = DELETE"); // WAL yerine DELETE kullanıyoruz
                stmt.execute("PRAGMA synchronous = NORMAL");

                // Create tables
                stmt.execute("CREATE TABLE IF NOT EXISTS artists (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL" +
                        ")");

                stmt.execute("CREATE TABLE IF NOT EXISTS albums (" +
                        "id TEXT PRIMARY KEY," +
                        "title TEXT NOT NULL," +
                        "artist_id TEXT," +
                        "release_year INTEGER," +
                        "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE" +
                        ")");

                stmt.execute("CREATE TABLE IF NOT EXISTS songs (" +
                        "id TEXT PRIMARY KEY," +
                        "title TEXT NOT NULL," +
                        "artist_id TEXT," +
                        "album_id TEXT," +
                        "duration INTEGER," +
                        "genre TEXT," +
                        "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE" +
                        ")");

                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id TEXT PRIMARY KEY," +
                        "username TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL" +
                        ")");

                stmt.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "user_id TEXT," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")");

                stmt.execute("CREATE TABLE IF NOT EXISTS playlist_songs (" +
                        "playlist_id TEXT," +
                        "song_id TEXT," +
                        "FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE," +
                        "PRIMARY KEY (playlist_id, song_id)" +
                        ")");

                success = true;
                logger.info("Test database setup completed successfully");

            } catch (SQLException e) {
                retryCount++;
                logger.error("Failed to setup test database (attempt " + retryCount + " of " + maxRetries + "): " + e.getMessage());
                if (retryCount == maxRetries) {
                    logger.error("Failed to setup test database after " + maxRetries + " attempts", e);
                    throw new RuntimeException("Failed to setup test database", e);
                }
                try {
                    Thread.sleep(1000 * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Test database setup interrupted", ie);
                }
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    // Connection'ı kapatma, çünkü muhtemelen daha sonra kullanılacak
                } catch (SQLException e) {
                    logger.error("Error closing database resources", e);
                }
            }
        }
    }
} 