package com.samet.music.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for database maintenance and fixing common issues
 */
public class DatabaseMaintenance {

    /**
     * Fix foreign key constraint issues in the database
     * This method looks for orphaned references and fixes them
     */
    public static void fixForeignKeyConstraints() {
        System.out.println("Starting database maintenance to fix foreign key constraint issues...");

        try (Connection conn = DatabaseManager.getConnection()) {
            // Temporarily disable foreign key constraints for maintenance
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = OFF");
            }

            // Start a transaction
            conn.setAutoCommit(false);

            try {
                // Fix songs with non-existent album references
                fixOrphanedSongAlbumReferences(conn);

                // Fix songs with non-existent artist references
                fixOrphanedSongArtistReferences(conn);

                // Fix albums with non-existent artist references
                fixOrphanedAlbumArtistReferences(conn);

                // Fix playlist_songs with non-existent song references
                fixOrphanedPlaylistSongReferences(conn);

                // Commit the transaction
                conn.commit();
                System.out.println("Database maintenance completed successfully");
            } catch (SQLException e) {
                // Rollback in case of error
                conn.rollback();
                System.err.println("Error during database maintenance: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                // Re-enable foreign key constraints
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }

                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Failed to perform database maintenance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fix songs that reference non-existent albums
     */
    private static void fixOrphanedSongAlbumReferences(Connection conn) throws SQLException {
        System.out.println("Fixing orphaned song-album references...");

        // Find songs with album_id that doesn't exist in the albums table
        String findSql = "SELECT s.id, s.name, s.album_id FROM songs s " +
                "LEFT JOIN albums a ON s.album_id = a.id " +
                "WHERE s.album_id IS NOT NULL AND a.id IS NULL";

        // Set album_id to NULL for orphaned references
        String updateSql = "UPDATE songs SET album_id = NULL WHERE id = ?";

        int fixedCount = 0;

        try (PreparedStatement findStmt = conn.prepareStatement(findSql);
             ResultSet rs = findStmt.executeQuery();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            while (rs.next()) {
                String songId = rs.getString("id");
                String songName = rs.getString("name");
                String invalidAlbumId = rs.getString("album_id");

                updateStmt.setString(1, songId);
                updateStmt.executeUpdate();

                System.out.println("Fixed song '" + songName + "' (ID: " + songId +
                        ") - removed invalid album reference: " + invalidAlbumId);
                fixedCount++;
            }
        }

        System.out.println("Fixed " + fixedCount + " orphaned song-album references");
    }

    /**
     * Fix songs that reference non-existent artists
     */
    private static void fixOrphanedSongArtistReferences(Connection conn) throws SQLException {
        System.out.println("Fixing orphaned song-artist references...");

        // Find songs with artist_id that doesn't exist in the artists table
        String findSql = "SELECT s.id, s.name, s.artist_id FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "WHERE s.artist_id IS NOT NULL AND a.id IS NULL";

        // Create a default "Unknown Artist" if needed
        String checkDefaultArtistSql = "SELECT id FROM artists WHERE name = 'Unknown Artist' LIMIT 1";
        String createDefaultArtistSql = "INSERT INTO artists (id, name, biography) VALUES (?, 'Unknown Artist', 'Automatically created for orphaned songs')";

        String defaultArtistId = null;

        // Check if default artist exists, create if not
        try (PreparedStatement checkStmt = conn.prepareStatement(checkDefaultArtistSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next()) {
                defaultArtistId = rs.getString("id");
                System.out.println("Using existing 'Unknown Artist' with ID: " + defaultArtistId);
            } else {
                defaultArtistId = java.util.UUID.randomUUID().toString();
                try (PreparedStatement createStmt = conn.prepareStatement(createDefaultArtistSql)) {
                    createStmt.setString(1, defaultArtistId);
                    createStmt.executeUpdate();
                    System.out.println("Created 'Unknown Artist' with ID: " + defaultArtistId);
                }
            }
        }

        // Update orphaned songs to use the default artist
        String updateSql = "UPDATE songs SET artist_id = ? WHERE id = ?";

        int fixedCount = 0;

        try (PreparedStatement findStmt = conn.prepareStatement(findSql);
             ResultSet rs = findStmt.executeQuery();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            while (rs.next()) {
                String songId = rs.getString("id");
                String songName = rs.getString("name");
                String invalidArtistId = rs.getString("artist_id");

                updateStmt.setString(1, defaultArtistId);
                updateStmt.setString(2, songId);
                updateStmt.executeUpdate();

                System.out.println("Fixed song '" + songName + "' (ID: " + songId +
                        ") - replaced invalid artist reference: " +
                        invalidArtistId + " with Unknown Artist");
                fixedCount++;
            }
        }

        System.out.println("Fixed " + fixedCount + " orphaned song-artist references");
    }

    /**
     * Fix albums that reference non-existent artists
     */
    private static void fixOrphanedAlbumArtistReferences(Connection conn) throws SQLException {
        System.out.println("Fixing orphaned album-artist references...");

        // Find albums with artist_id that doesn't exist in the artists table
        String findSql = "SELECT a.id, a.name, a.artist_id FROM albums a " +
                "LEFT JOIN artists ar ON a.artist_id = ar.id " +
                "WHERE a.artist_id IS NOT NULL AND ar.id IS NULL";

        // Check if default "Unknown Artist" exists
        String checkDefaultArtistSql = "SELECT id FROM artists WHERE name = 'Unknown Artist' LIMIT 1";
        String createDefaultArtistSql = "INSERT INTO artists (id, name, biography) VALUES (?, 'Unknown Artist', 'Automatically created for orphaned albums')";

        String defaultArtistId = null;

        // Check if default artist exists, create if not
        try (PreparedStatement checkStmt = conn.prepareStatement(checkDefaultArtistSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next()) {
                defaultArtistId = rs.getString("id");
            } else {
                defaultArtistId = java.util.UUID.randomUUID().toString();
                try (PreparedStatement createStmt = conn.prepareStatement(createDefaultArtistSql)) {
                    createStmt.setString(1, defaultArtistId);
                    createStmt.executeUpdate();
                }
            }
        }

        // Update orphaned albums to use the default artist
        String updateSql = "UPDATE albums SET artist_id = ? WHERE id = ?";

        int fixedCount = 0;

        try (PreparedStatement findStmt = conn.prepareStatement(findSql);
             ResultSet rs = findStmt.executeQuery();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            while (rs.next()) {
                String albumId = rs.getString("id");
                String albumName = rs.getString("name");
                String invalidArtistId = rs.getString("artist_id");

                updateStmt.setString(1, defaultArtistId);
                updateStmt.setString(2, albumId);
                updateStmt.executeUpdate();

                System.out.println("Fixed album '" + albumName + "' (ID: " + albumId +
                        ") - replaced invalid artist reference: " +
                        invalidArtistId + " with Unknown Artist");
                fixedCount++;
            }
        }

        System.out.println("Fixed " + fixedCount + " orphaned album-artist references");
    }

    /**
     * Fix playlist_songs entries that reference non-existent songs
     */
    private static void fixOrphanedPlaylistSongReferences(Connection conn) throws SQLException {
        System.out.println("Fixing orphaned playlist-song references...");

        // Find playlist_songs entries with song_id that doesn't exist in the songs table
        String findSql = "SELECT ps.playlist_id, ps.song_id FROM playlist_songs ps " +
                "LEFT JOIN songs s ON ps.song_id = s.id " +
                "WHERE s.id IS NULL";

        // Delete orphaned playlist_songs entries
        String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

        int fixedCount = 0;

        try (PreparedStatement findStmt = conn.prepareStatement(findSql);
             ResultSet rs = findStmt.executeQuery();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            while (rs.next()) {
                String playlistId = rs.getString("playlist_id");
                String songId = rs.getString("song_id");

                deleteStmt.setString(1, playlistId);
                deleteStmt.setString(2, songId);
                deleteStmt.executeUpdate();

                System.out.println("Removed orphaned playlist-song reference: playlist ID " +
                        playlistId + " - song ID " + songId);
                fixedCount++;
            }
        }

        System.out.println("Fixed " + fixedCount + " orphaned playlist-song references");
    }

    /**
     * Optimize database performance
     */
    public static void optimizeDatabasePerformance() {
        System.out.println("Optimizing database performance...");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // VACUUM operation - reduces database size and defragments
            stmt.execute("VACUUM");

            // Rebuild indexes
            stmt.execute("REINDEX");

            // Analyze statistics
            stmt.execute("ANALYZE");

            System.out.println("Database optimization completed");
        } catch (SQLException e) {
            System.err.println("Error during database optimization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create database backup
     */
    public static boolean backupDatabase(String backupPath) {
        System.out.println("Creating database backup to: " + backupPath);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("BACKUP DATABASE TO '" + backupPath + "'");
            System.out.println("Database backup completed successfully");
            return true;
        } catch (SQLException e) {
            System.err.println("Error during database backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}