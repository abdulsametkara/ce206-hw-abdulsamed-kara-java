package com.samet.music.dao;

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
 * Data Access Object for Song entities
 */
public class SongDAO {
    private static final Logger logger = LoggerFactory.getLogger(SongDAO.class);

    /**
     * Create a new song in the database
     * @param song the song to create
     * @return the created song with id
     */
    public Song create(Song song) {
        // SQLite desteklemiyor olabilir, alternatif yöntem kullanacağız
        String insertSql = "INSERT INTO songs (title, artist, album, genre, year, duration, file_path, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        String idSql = "SELECT last_insert_rowid() as id";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Auto-commit'i devre dışı bırak
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, song.getTitle());
                pstmt.setString(2, song.getArtist());
                pstmt.setString(3, song.getAlbum());
                pstmt.setString(4, song.getGenre());
                pstmt.setInt(5, song.getYear());
                pstmt.setInt(6, song.getDuration());
                pstmt.setString(7, song.getFilePath());
                pstmt.setInt(8, song.getUser().getId());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Son eklenen satırın ID'sini al
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(idSql)) {
                        if (rs.next()) {
                            song.setId(rs.getInt("id"));
                            conn.commit();
                            logger.info("Song created successfully with ID: {}", song.getId());
                            return song;
                        }
                    }
                }
                
                conn.rollback();
                logger.error("Failed to create song, no ID obtained.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating song", e);
        }
        
        return null;
    }

    /**
     * Get a song by id
     * @param id the song id
     * @return an Optional containing the song if found
     */
    public Optional<Song> findById(int id) {
        String sql = "SELECT * FROM songs WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    return Optional.of(song);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding song by ID", e);
        }
        
        return Optional.empty();
    }

    /**
     * Get all songs
     * @return a list of all songs
     */
    public List<Song> findAll() {
        String sql = "SELECT * FROM songs";
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Song song = mapResultSetToSong(rs);
                songs.add(song);
            }
        } catch (SQLException e) {
            logger.error("Error finding all songs", e);
        }
        
        return songs;
    }

    /**
     * Get all songs for a user
     * @param userId the user id
     * @return a list of all songs for the user
     */
    public List<Song> findByUserId(int userId) {
        String sql = "SELECT * FROM songs WHERE user_id = ?";
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    songs.add(song);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding songs by user ID", e);
        }
        
        return songs;
    }

    /**
     * Search for songs by various criteria
     * @param title the title to search for (can be partial)
     * @param artist the artist to search for (can be partial)
     * @param album the album to search for (can be partial)
     * @param genre the genre to search for (can be partial)
     * @return a list of songs matching the criteria
     */
    public List<Song> search(String title, String artist, String album, String genre) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM songs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (title != null && !title.isEmpty()) {
            sqlBuilder.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        
        if (artist != null && !artist.isEmpty()) {
            sqlBuilder.append(" AND artist LIKE ?");
            params.add("%" + artist + "%");
        }
        
        if (album != null && !album.isEmpty()) {
            sqlBuilder.append(" AND album LIKE ?");
            params.add("%" + album + "%");
        }
        
        if (genre != null && !genre.isEmpty()) {
            sqlBuilder.append(" AND genre LIKE ?");
            params.add("%" + genre + "%");
        }
        
        String sql = sqlBuilder.toString();
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    songs.add(song);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching songs", e);
        }
        
        return songs;
    }

    /**
     * Update a song in the database
     * @param song the song to update
     * @return true if the update was successful, false otherwise
     */
    public boolean update(Song song) {
        String sql = "UPDATE songs SET title = ?, artist = ?, album = ?, genre = ?, " +
                     "year = ?, duration = ?, file_path = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getAlbum());
            pstmt.setString(4, song.getGenre());
            pstmt.setInt(5, song.getYear());
            pstmt.setInt(6, song.getDuration());
            pstmt.setString(7, song.getFilePath());
            pstmt.setInt(8, song.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Song updated successfully with ID: {}", song.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating song", e);
        }
        
        return false;
    }

    /**
     * Delete a song from the database
     * @param id the id of the song to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM songs WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Song deleted successfully with ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting song", e);
        }
        
        return false;
    }

    /**
     * Map a ResultSet to a Song object
     * @param rs the ResultSet
     * @return the Song object
     * @throws SQLException if a database access error occurs
     */
    public Song mapResultSetToSong(ResultSet rs) throws SQLException {
        Song song = new Song();
        song.setId(rs.getInt("id"));
        song.setTitle(rs.getString("title"));
        song.setArtist(rs.getString("artist"));
        song.setAlbum(rs.getString("album"));
        song.setGenre(rs.getString("genre"));
        song.setYear(rs.getInt("year"));
        song.setDuration(rs.getInt("duration"));
        song.setFilePath(rs.getString("file_path"));
        
        // Create a User object with just the ID
        com.samet.music.model.User user = new com.samet.music.model.User();
        user.setId(rs.getInt("user_id"));
        song.setUser(user);
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            song.setCreatedAt(timestamp.toLocalDateTime());
        }
        
        return song;
    }
    
    /**
     * Find songs by artist name
     * @param artist the artist name to search for (exact match)
     * @return a list of songs by the specified artist
     */
    public List<Song> findByArtist(String artist) {
        String sql = "SELECT * FROM songs WHERE artist = ?";
        List<Song> songs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artist);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Song song = mapResultSetToSong(rs);
                    songs.add(song);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding songs by artist", e);
        }
        
        return songs;
    }
} 