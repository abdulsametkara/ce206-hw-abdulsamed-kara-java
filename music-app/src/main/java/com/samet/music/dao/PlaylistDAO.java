package com.samet.music.dao;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Playlist entities
 */
public class PlaylistDAO {
    private final SongDAO songDAO;

    public PlaylistDAO() {
        this.songDAO = new SongDAO();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Create playlists table
            String sql = "CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            conn.createStatement().execute(sql);
            
            // Create playlist_songs table to map songs to playlists
            sql = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id INTEGER," +
                    "song_id INTEGER," +
                    "position INTEGER," +
                    "PRIMARY KEY (playlist_id, song_id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id)" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("PlaylistDAO: playlists tables check/creation completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public PlaylistDAO(SongDAO songDAO) {
        this.songDAO = songDAO;
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Create playlists table
            String sql = "CREATE TABLE IF NOT EXISTS playlists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            conn.createStatement().execute(sql);
            
            // Create playlist_songs table to map songs to playlists
            sql = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                    "playlist_id INTEGER," +
                    "song_id INTEGER," +
                    "position INTEGER," +
                    "PRIMARY KEY (playlist_id, song_id)," +
                    "FOREIGN KEY (playlist_id) REFERENCES playlists(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id)" +
                    ")";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new playlist with basic information (KULLANICI ID'Sİ GEREKLİ)
     * @param name Playlist name
     * @param description Playlist description
     * @param userId Kullanıcı ID'si
     * @return true if added successfully
     */
    public boolean addPlaylist(String name, String description, int userId) {
        String sql = "INSERT INTO playlists (name, description, user_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, userId);
            int affectedRows = pstmt.executeUpdate();
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Eski fonksiyon, yeni fonksiyona yönlendirildi. KULLANIMDAN KALDIRILMASI ÖNERİLİR.
     */
    public boolean addPlaylist(String name, String description) {
        // TODO: Kullanıcı ID'si gereklidir! Lütfen yeni fonksiyonu kullanın.
        return false;
    }
    
    /**
     * Update an existing playlist
     * @param oldName Original name for identifying the playlist
     * @param newName New name
     * @return true if update was successful
     */
    public boolean updatePlaylist(String oldName, String newName) {
        String sql = "UPDATE playlists SET name = ? WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);
            
            int affectedRows = pstmt.executeUpdate();
            
            // Only commit if we're not in auto-commit mode
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a playlist by name
     * @param name Playlist name
     * @return true if deleted successfully
     */
    public boolean deletePlaylist(String name) {
        String sql = "DELETE FROM playlists WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            int affectedRows = pstmt.executeUpdate();
            
            // Only commit if we're not in auto-commit mode
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all playlists as string arrays for display
     * @return List of string arrays containing [name, songCount, created]
     */
    public List<String[]> getAllPlaylists() {
        List<String[]> playlists = new ArrayList<>();
        String sql = "SELECT p.name, COUNT(ps.song_id) as song_count, p.created_at " +
                    "FROM playlists p " +
                    "LEFT JOIN playlist_songs ps ON p.id = ps.playlist_id " +
                    "GROUP BY p.id, p.name, p.created_at";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                playlists.add(new String[]{
                    rs.getString("name"),
                    String.valueOf(rs.getInt("song_count")),
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    /**
     * Create a new playlist in the database
     * @param playlist the playlist to create
     * @return the created playlist with id
     */
    public Playlist create(Playlist playlist) {
        String sql = "INSERT INTO playlists (name, description, user_id) VALUES (?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean previousAutoCommit = true;
        
        try {
            conn = DatabaseUtil.getConnection();
            
            // Save the current auto-commit state
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, playlist.getName());
            pstmt.setString(2, playlist.getDescription());
            pstmt.setInt(3, playlist.getUserId());
                
            int affectedRows = pstmt.executeUpdate();
                
            if (affectedRows > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        playlist.setId(generatedKeys.getInt(1));
                            
                        // Add songs to playlist_songs table if any
                        if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
                            if (!addSongsToPlaylist(conn, playlist.getId(), playlist.getSongs())) {
                                conn.rollback();
                                return null;
                            }
                        }
                        
                        conn.commit();
                        return playlist;
                    }
                }
            }
                
            conn.rollback();
            return null;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            // Restore original auto-commit state
            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                    if (pstmt != null) pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get a playlist by id
     * @param id the playlist id
     * @return an Optional containing the playlist if found
     */
    public Optional<Playlist> findById(int id) {
        String sql = "SELECT * FROM playlists WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Playlist playlist = mapResultSetToPlaylist(rs);
                    
                    // Get songs for this playlist
                    List<Song> songs = getSongsByPlaylistId(id);
                    playlist.setSongs(songs);
                    
                    return Optional.of(playlist);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Get all playlists by user id
     * @param userId the user id
     * @return a list of all playlists for the specified user
     */
    public List<Playlist> findByUserId(int userId) {
        String sql = "SELECT * FROM playlists WHERE user_id = ? ORDER BY created_at DESC";
        List<Playlist> playlists = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        Playlist playlist = mapResultSetToPlaylist(rs);
                        
                    // Get songs for this playlist
                    List<Song> songs = getSongsByPlaylistId(playlist.getId());
                        playlist.setSongs(songs);
                        
                        playlists.add(playlist);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return playlists;
    }

    /**
     * Get all playlists
     * @return a list of all playlists
     */
    public List<Playlist> findAll() {
        String sql = "SELECT * FROM playlists";
        List<Playlist> playlists = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Playlist playlist = mapResultSetToPlaylist(rs);
                
                // Get songs for this playlist
                List<Song> songs = getSongsByPlaylistId(playlist.getId());
                playlist.setSongs(songs);
                
                playlists.add(playlist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return playlists;
    }

    /**
     * Update a playlist in the database
     * @param playlist the playlist to update
     * @return true if the update was successful, false otherwise
     */
    public boolean update(Playlist playlist) {
        String sql = "UPDATE playlists SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            pstmt.setString(1, playlist.getName());
            pstmt.setString(2, playlist.getDescription());
            pstmt.setInt(3, playlist.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update the songs in the playlist
                if (playlist.getSongs() != null) {
                    // First remove all existing songs
                    removeSongsFromPlaylist(conn, playlist.getId());
                    
                    // Then add the current songs if any
                    if (!playlist.getSongs().isEmpty() && !addSongsToPlaylist(conn, playlist.getId(), playlist.getSongs())) {
                        conn.rollback();
                        return false;
                    }
                }
                
                conn.commit();
                return true;
            }
            
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Delete a playlist from the database
     * @param id the id of the playlist to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
        // First remove all songs from the playlist
            removeSongsFromPlaylist(conn, id);
        
            // Then delete the playlist
        String sql = "DELETE FROM playlists WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                    conn.commit();
                return true;
                }
                
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Add songs to a playlist
     * @param playlistId the playlist id
     * @param songs the songs to add
     * @return true if the addition was successful, false otherwise
     */
    public boolean addSongsToPlaylist(int playlistId, List<Song> songs) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            boolean success = addSongsToPlaylist(conn, playlistId, songs);
            
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Add songs to a playlist (internal method with transaction support)
     */
    private boolean addSongsToPlaylist(Connection conn, int playlistId, List<Song> songs) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int position = 0;
            
            for (Song song : songs) {
                pstmt.setInt(1, playlistId);
                pstmt.setInt(2, song.getId());
                pstmt.setInt(3, position++);
                
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
        }
        
            return true;
        }
    }

    /**
     * Remove songs from a playlist
     * @param playlistId the playlist id
     * @return true if the removal was successful, false otherwise
     */
    public boolean removeSongsFromPlaylist(int playlistId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            boolean success = removeSongsFromPlaylist(conn, playlistId);
            
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Remove songs from a playlist (internal method with transaction support)
     */
    private boolean removeSongsFromPlaylist(Connection conn, int playlistId) throws SQLException {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
            return true;
        }
    }

    /**
     * Get songs by playlist id
     * @param playlistId the playlist id
     * @return a list of songs in the playlist
     */
    private List<Song> getSongsByPlaylistId(int playlistId) {
        String sql = "SELECT s.* FROM songs s " +
                    "JOIN playlist_songs ps ON s.id = ps.song_id " +
                    "WHERE ps.playlist_id = ? " +
                    "ORDER BY ps.position";
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, playlistId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(songDAO.mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }

    /**
     * Map a ResultSet to a Playlist object
     * @param rs the ResultSet
     * @return the Playlist object
     * @throws SQLException if a database access error occurs
     */
    private Playlist mapResultSetToPlaylist(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int userId = rs.getInt("user_id");
        
        // Convert timestamp to LocalDateTime
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
        
        return new Playlist(id, name, description, userId, createdAt);
    }
} 