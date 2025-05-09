package com.samet.music.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

public class SongDAO {
    
    private Connection connection;
    
    public SongDAO() {
        System.out.println("SongDAO initializing");
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Database connection established successfully");
            
            String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "artist TEXT NOT NULL," +
                    "album TEXT NOT NULL," +
                    "genre TEXT NOT NULL," +
                    "year INTEGER," +
                    "duration INTEGER," +
                    "file_path TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                    
            conn.createStatement().execute(sql);
            System.out.println("SongDAO: songs table check/creation completed");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor with connection parameter for testing
     * @param connection database connection
     */
    public SongDAO(Connection connection) {
        this.connection = connection;
        System.out.println("SongDAO initializing with provided connection");
    }

    public void addSong(String title, String artist, String album, String genre) {
        String sql = "INSERT INTO songs(title, artist, album, genre) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setString(3, album);
            pstmt.setString(4, genre);
            pstmt.executeUpdate();
            
            // Only commit if we're not in auto-commit mode
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAllSongs() {
        List<String[]> songs = new ArrayList<>();
        String sql = "SELECT title, artist, album, genre FROM songs";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(new String[]{
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getString("genre")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public void deleteSong(String title, String artist, String album) {
        String sql = "DELETE FROM songs WHERE title=? AND artist=? AND album=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setString(3, album);
            pstmt.executeUpdate();
            
            // Only commit if we're not in auto-commit mode
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new song
     * @param song song to create
     * @return created song
     */
    public Song create(Song song) {
        String sql = "INSERT INTO songs(title, artist, album, genre, year, duration, file_path, user_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean previousAutoCommit = true;
        
        try {
            conn = DatabaseUtil.getConnection();
            
            // Save the current auto-commit state and set to false for transaction
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getAlbum());
            pstmt.setString(4, song.getGenre());
            pstmt.setInt(5, song.getYear());
            pstmt.setInt(6, song.getDuration());
            pstmt.setString(7, song.getFilePath());
            pstmt.setInt(8, song.getUserId());
            pstmt.setTimestamp(9, song.getCreatedAt());
                
            int affectedRows = pstmt.executeUpdate();
                
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    song.setId(generatedKeys.getInt(1));
                    
                    // Commit transaction
                    conn.commit();
                    return song;
                }
            }
            
            // If we get here, something went wrong
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
     * Find song by ID
     * @param id song ID
     * @return song
     */
    public Optional<Song> findById(int id) {
        String sql = "SELECT * FROM songs WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Find all songs
     * @return list of songs
     */
    public List<Song> findAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(mapResultSetToSong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }

    /**
     * Find songs by user ID
     * @param userId user ID
     * @return list of songs
     */
    public List<Song> findByUserId(int userId) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }

    /**
     * Song update
     * @param song song to update
     * @return update successful or not
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
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete song by ID
     * @param id song ID to delete
     * @return deletion successful or not
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM songs WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search songs
     * @param title title
     * @param artist artist
     * @param album album
     * @param genre genre
     * @return search results
     */
    public List<Song> search(String title, String artist, String album, String genre) {
        List<Song> songs = new ArrayList<>();
        
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
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }

    /**
     * Find songs by artist
     * @param artist artist name
     * @return list of songs
     */
    public List<Song> findByArtist(String artist) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE artist = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artist);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapResultSetToSong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }

    /**
     * Map ResultSet to Song object
     * @param rs ResultSet
     * @return Song object
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
        song.setUserId(rs.getInt("user_id"));
        song.setCreatedAt(rs.getTimestamp("created_at"));
        return song;
    }

    /**
     * Update song information
     * @param oldTitle original title for identifying the song
     * @param oldArtist original artist for identifying the song
     * @param oldAlbum original album for identifying the song
     * @param newTitle new title
     * @param newArtist new artist
     * @param newAlbum new album
     * @param newGenre new genre
     * @return true if update was successful
     */
    public boolean updateSong(String oldTitle, String oldArtist, String oldAlbum, 
                             String newTitle, String newArtist, String newAlbum, String newGenre) {
        String sql = "UPDATE songs SET title = ?, artist = ?, album = ?, genre = ? " +
                     "WHERE title = ? AND artist = ? AND album = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newArtist);
            pstmt.setString(3, newAlbum);
            pstmt.setString(4, newGenre);
            pstmt.setString(5, oldTitle);
            pstmt.setString(6, oldArtist);
            pstmt.setString(7, oldAlbum);
            
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
} 