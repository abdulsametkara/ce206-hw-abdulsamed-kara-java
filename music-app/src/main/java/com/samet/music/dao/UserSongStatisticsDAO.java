package com.samet.music.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for tracking user statistics with songs
 */
public class UserSongStatisticsDAO {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/musiclibrary.db";
    
    public UserSongStatisticsDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create user_song_statistics table
            String sql = "CREATE TABLE IF NOT EXISTS user_song_statistics (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "song_id INTEGER NOT NULL," +
                    "play_count INTEGER DEFAULT 0," +
                    "last_played TIMESTAMP," +
                    "favorite BOOLEAN DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id)," +
                    "UNIQUE(user_id, song_id)" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("UserSongStatisticsDAO: user_song_statistics table check/creation completed");
        } catch (SQLException e) {
            System.err.println("Error initializing user song statistics database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Increment play count for a song by a user
     * @param userId user ID
     * @param songId song ID
     * @return true if successful, false otherwise
     */
    public boolean incrementPlayCount(int userId, int songId) {
        String sql = "INSERT INTO user_song_statistics (user_id, song_id, play_count, last_played) " +
                "VALUES (?, ?, 1, CURRENT_TIMESTAMP) " +
                "ON CONFLICT(user_id, song_id) DO UPDATE SET " +
                "play_count = play_count + 1, " +
                "last_played = CURRENT_TIMESTAMP";
                
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Set a song as favorite for a user
     * @param userId user ID
     * @param songId song ID
     * @param favorite true to mark as favorite, false to unmark
     * @return true if successful, false otherwise
     */
    public boolean setFavorite(int userId, int songId, boolean favorite) {
        String sql = "INSERT INTO user_song_statistics (user_id, song_id, favorite) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(user_id, song_id) DO UPDATE SET " +
                "favorite = ?";
                
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.setBoolean(3, favorite);
            pstmt.setBoolean(4, favorite);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a song is marked as favorite by a user
     * @param userId user ID
     * @param songId song ID
     * @return true if the song is a favorite, false otherwise
     */
    public boolean isFavorite(int userId, int songId) {
        String sql = "SELECT favorite FROM user_song_statistics WHERE user_id = ? AND song_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getBoolean("favorite");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get play count for a song by a user
     * @param userId user ID
     * @param songId song ID
     * @return play count or 0 if not found
     */
    public int getPlayCount(int userId, int songId) {
        String sql = "SELECT play_count FROM user_song_statistics WHERE user_id = ? AND song_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("play_count") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get most played songs for a user
     * @param userId user ID
     * @param limit maximum number of songs to return
     * @return a list of song IDs ordered by play count (descending)
     */
    public List<Integer> getMostPlayedSongs(int userId, int limit) {
        List<Integer> songIds = new ArrayList<>();
        String sql = "SELECT song_id FROM user_song_statistics WHERE user_id = ? " +
                    "ORDER BY play_count DESC LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songIds.add(rs.getInt("song_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songIds;
    }
    
    /**
     * Get favorite songs for a user
     * @param userId user ID
     * @return a list of song IDs marked as favorite
     */
    public List<Integer> getFavoriteSongs(int userId) {
        List<Integer> songIds = new ArrayList<>();
        String sql = "SELECT song_id FROM user_song_statistics WHERE user_id = ? AND favorite = 1";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songIds.add(rs.getInt("song_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songIds;
    }
    
    /**
     * Get recently played songs for a user
     * @param userId user ID
     * @param limit maximum number of songs to return
     * @return a list of song IDs ordered by last played date (descending)
     */
    public List<Integer> getRecentlyPlayedSongs(int userId, int limit) {
        List<Integer> songIds = new ArrayList<>();
        String sql = "SELECT song_id FROM user_song_statistics WHERE user_id = ? AND last_played IS NOT NULL " +
                    "ORDER BY last_played DESC LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songIds.add(rs.getInt("song_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songIds;
    }
    
    /**
     * Get listening statistics for a user
     * @param userId user ID
     * @return a map with statistics (total_plays, favorite_count, etc.)
     */
    public Map<String, Object> getUserStatistics(int userId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                    "COUNT(*) as total_songs, " +
                    "SUM(play_count) as total_plays, " +
                    "SUM(CASE WHEN favorite = 1 THEN 1 ELSE 0 END) as favorite_count, " +
                    "MAX(last_played) as last_played " +
                    "FROM user_song_statistics WHERE user_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_songs", rs.getInt("total_songs"));
                    stats.put("total_plays", rs.getInt("total_plays"));
                    stats.put("favorite_count", rs.getInt("favorite_count"));
                    
                    Timestamp lastPlayed = rs.getTimestamp("last_played");
                    if (lastPlayed != null) {
                        stats.put("last_played", lastPlayed.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
} 