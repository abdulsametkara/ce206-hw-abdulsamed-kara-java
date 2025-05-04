package com.samet.music.dao;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Playlist entities
 */
public class PlaylistDAO {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistDAO.class);
    private final SongDAO songDAO;

    public PlaylistDAO(SongDAO songDAO) {
        this.songDAO = songDAO;
    }

    /**
     * Create a new playlist in the database
     * @param playlist the playlist to create
     * @return the created playlist with id
     */
    public Playlist create(Playlist playlist) {
        String insertSql = "INSERT INTO playlists (name, user_id) VALUES (?, ?)";
        String idSql = "SELECT last_insert_rowid() as id";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Auto-commit'i devre dışı bırak
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, playlist.getName());
                pstmt.setInt(2, playlist.getUserId());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Son eklenen satırın ID'sini al
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(idSql)) {
                        if (rs.next()) {
                            playlist.setId(rs.getInt("id"));
                            conn.commit();
                            
                            // Add songs to playlist_songs table
                            if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
                                addSongsToPlaylist(playlist.getId(), playlist.getSongs());
                            }
                            
                            logger.info("Playlist created successfully with ID: {}", playlist.getId());
                            return playlist;
                        }
                    }
                }
                
                conn.rollback();
                logger.error("Failed to create playlist, no ID obtained.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating playlist", e);
        }
        
        return null;
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
            logger.error("Error finding playlist by ID", e);
        }
        
        return Optional.empty();
    }

    /**
     * Get all playlists by user id
     * @param userId the user id
     * @return a list of all playlists for the specified user
     */
    public List<Playlist> findByUserId(int userId) {
        String sql = "SELECT * FROM playlists WHERE user_id = ?";
        List<Playlist> playlists = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        Playlist playlist = mapResultSetToPlaylist(rs);
                        
                        // Get songs for this playlist - hata durumunda boş liste kullan
                        List<Song> songs = new ArrayList<>();
                        try {
                            songs = getSongsByPlaylistId(playlist.getId());
                        } catch (Exception e) {
                            // Sessizce devam et
                        }
                        playlist.setSongs(songs);
                        
                        playlists.add(playlist);
                    } catch (Exception e) {
                        // Bir kayıt hatalıysa diğerlerine devam et
                        logger.warn("Error mapping playlist, skipping", e);
                    }
                }
            }
        } catch (SQLException e) {
            // Hata logunu bastır ama uygulamanın çökmesine izin verme
            logger.error("Error finding playlists by user ID", e);
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
            logger.error("Error finding all playlists", e);
        }
        
        return playlists;
    }

    /**
     * Update a playlist in the database
     * @param playlist the playlist to update
     * @return true if the update was successful, false otherwise
     */
    public boolean update(Playlist playlist) {
        String sql = "UPDATE playlists SET name = ?, user_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playlist.getName());
            pstmt.setInt(2, playlist.getUserId());
            pstmt.setInt(3, playlist.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update the songs in the playlist
                if (playlist.getSongs() != null) {
                    // First remove all existing songs
                    removeSongsFromPlaylist(playlist.getId());
                    
                    // Then add the current songs
                    addSongsToPlaylist(playlist.getId(), playlist.getSongs());
                }
                
                logger.info("Playlist updated successfully with ID: {}", playlist.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating playlist", e);
        }
        
        return false;
    }

    /**
     * Delete a playlist from the database
     * @param id the id of the playlist to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        // First remove all songs from the playlist
        removeSongsFromPlaylist(id);
        
        String sql = "DELETE FROM playlists WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Playlist deleted successfully with ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting playlist", e);
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
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
        boolean success = true;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Song song : songs) {
                pstmt.setInt(1, playlistId);
                pstmt.setInt(2, song.getId());
                
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            
            for (int result : results) {
                if (result <= 0) {
                    success = false;
                }
            }
            
            if (success) {
                logger.info("Songs added to playlist successfully");
            } else {
                logger.error("Failed to add some songs to playlist");
            }
        } catch (SQLException e) {
            logger.error("Error adding songs to playlist", e);
            success = false;
        }
        
        return success;
    }

    /**
     * Remove songs from a playlist
     * @param playlistId the playlist id
     * @return true if the removal was successful, false otherwise
     */
    public boolean removeSongsFromPlaylist(int playlistId) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, playlistId);
            
            pstmt.executeUpdate();
            logger.info("Songs removed from playlist successfully");
            return true;
        } catch (SQLException e) {
            logger.error("Error removing songs from playlist", e);
        }
        
        return false;
    }

    /**
     * Get songs by playlist id
     * @param playlistId the playlist id
     * @return a list of songs in the playlist
     */
    private List<Song> getSongsByPlaylistId(int playlistId) {
        String sql = "SELECT song_id FROM playlist_songs WHERE playlist_id = ?";
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, playlistId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int songId = rs.getInt("song_id");
                    songDAO.findById(songId).ifPresent(songs::add);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting songs by playlist ID", e);
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
        int userId = rs.getInt("user_id");
        
        // Convert timestamp to LocalDateTime
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
        
        return new Playlist(id, name, "", userId, createdAt);
    }
} 