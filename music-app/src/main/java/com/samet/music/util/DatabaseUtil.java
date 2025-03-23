package com.samet.music.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Database utilities class
 */
public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:music_library.db";
    // Varsayılan veritabanı konumunu kullanıcının ev dizinine taşıyalım
    private static final String DB_FILE_PATH = System.getProperty("user.home") + "/music_library.db";
    private static final String DB_NEW_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    private static boolean isInitialized = false;
    private static final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOL_SIZE = 10;

    // Flag for database reset
    private static boolean shouldResetDatabase = false;

    public static Connection getConnection() throws SQLException {
        try {
            // Yeni bağlantı URL'sini kullan
            Connection conn = DriverManager.getConnection(DB_NEW_URL);

            // Kilit zaman aşımını ve kilit modunu ayarla
            try (Statement stmt = conn.createStatement()) {
                // Eşzamanlı veritabanı erişimi için ayarlar
                stmt.execute("PRAGMA journal_mode=WAL");    // Write-Ahead Logging modunu etkinleştir
                stmt.execute("PRAGMA synchronous=NORMAL");  // Normal senkronizasyon (performans/güvenlik dengesi)
                stmt.execute("PRAGMA busy_timeout=5000");   // Kilit zaman aşımını 5 saniye olarak ayarla
                stmt.execute("PRAGMA foreign_keys=ON");     // Yabancı anahtar kısıtlamalarını etkinleştir
            }

            return conn;
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantısı oluşturulamadı: " + e.getMessage());
            throw e;
        }
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && connectionPool.size() < MAX_POOL_SIZE) {
                    connectionPool.offer(conn);
                } else {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
                try {
                    conn.close();
                } catch (SQLException ex) {
                    // Sessizce kapat
                }
            }
        }
    }

    /**
     * Initialize database and tables
     */
    public static void initializeDatabase() {
        // Avoid re-initialization if already done
        if (isInitialized) {
            return;
        }

        try {
            // Veritabanı dosyasının olup olmadığını kontrol et
            File dbFile = new File(DB_FILE_PATH);
            if (!dbFile.exists()) {
                // Dosya yoksa, dizini oluştur
                dbFile.getParentFile().mkdirs();
            } else if (shouldResetDatabase || dbFile.length() == 0) {
                // Eğer dosya sıfırlanacaksa veya boşsa, dosyayı sil ve yeniden oluştur
                dbFile.delete();
            }

            // Yeni bir bağlantı al ve tabloları oluştur
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // Create tables if they don't exist
                setupTables(stmt);

                isInitialized = true;
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Set up database tables
     * @param stmt Statement object
     * @throws SQLException SQL error
     */
    private static void setupTables(Statement stmt) throws SQLException {
        // Artists table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS artists (" +
                        "    id TEXT PRIMARY KEY," +
                        "    name TEXT NOT NULL," +
                        "    biography TEXT" +
                        ")"
        );

        // Albums table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS albums (" +
                        "    id TEXT PRIMARY KEY," +
                        "    name TEXT NOT NULL," +
                        "    artist_id TEXT," +
                        "    release_year INTEGER," +
                        "    genre TEXT," +
                        "    FOREIGN KEY(artist_id) REFERENCES artists(id)" +
                        ")"
        );

        // Songs table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS songs (" +
                        "    id TEXT PRIMARY KEY," +
                        "    name TEXT NOT NULL," +
                        "    artist_id TEXT," +
                        "    album_id TEXT," +
                        "    duration INTEGER," +
                        "    genre TEXT," +
                        "    FOREIGN KEY(artist_id) REFERENCES artists(id)," +
                        "    FOREIGN KEY(album_id) REFERENCES albums(id)" +
                        ")"
        );

        // Playlists table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS playlists (" +
                        "    id TEXT PRIMARY KEY," +
                        "    name TEXT NOT NULL," +
                        "    description TEXT" +
                        ")"
        );

        // Playlist Songs table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                        "    playlist_id TEXT," +
                        "    song_id TEXT," +
                        "    PRIMARY KEY(playlist_id, song_id)," +
                        "    FOREIGN KEY(playlist_id) REFERENCES playlists(id)," +
                        "    FOREIGN KEY(song_id) REFERENCES songs(id)" +
                        ")"
        );

        System.out.println("Database tables created successfully.");
    }

    /**
     * Check if there is any data in the database
     * @param conn Database connection
     * @return True if data exists, false otherwise
     */
    private static boolean checkIfDataExists(Connection conn) {
        try {
            // Check for any data
            String[] tables = {"artists", "albums", "songs", "playlists"};

            for (String table : tables) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    rs.close();
                    checkStmt.close();
                    return true; // Data found
                }

                rs.close();
                checkStmt.close();
            }

            // No data found in any table
            return false;

        } catch (SQLException e) {
            System.err.println("Error checking if data exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear test data (records with 'Test' in name)
     * @param conn Database connection
     */
    private static void clearTestData(Connection conn) {
        try {
            // First clear dependent tables
            PreparedStatement deletePlaylistSongs = conn.prepareStatement(
                    "DELETE FROM playlist_songs WHERE playlist_id IN " +
                            "(SELECT id FROM playlists WHERE name LIKE '%Test%')");
            deletePlaylistSongs.executeUpdate();
            deletePlaylistSongs.close();

            // Then clear main tables
            String[] tables = {"songs", "albums", "playlists", "artists"};
            for (String table : tables) {
                PreparedStatement deleteTestData = conn.prepareStatement(
                        "DELETE FROM " + table + " WHERE name LIKE '%Test%'");
                deleteTestData.executeUpdate();
                deleteTestData.close();
            }

            System.out.println("Test data cleared successfully.");

        } catch (SQLException e) {
            System.err.println("Error clearing test data: " + e.getMessage());
        }
    }

    /**
     * Clear all data from the database
     * @param conn Database connection
     */
    private static void clearAllData(Connection conn) {
        try {
            // First clear related tables
            Statement stmt = conn.createStatement();

            stmt.execute("DELETE FROM playlist_songs");
            stmt.execute("DELETE FROM songs");
            stmt.execute("DELETE FROM albums");
            stmt.execute("DELETE FROM playlists");
            stmt.execute("DELETE FROM artists");

            stmt.close();
            System.out.println("All data cleared successfully.");

        } catch (SQLException e) {
            System.err.println("Error clearing all data: " + e.getMessage());
        }
    }

    /**
     * Set flag to reset database
     * @param shouldReset True to reset database
     */
    public static void setShouldResetDatabase(boolean shouldReset) {
        shouldResetDatabase = shouldReset;
    }
}