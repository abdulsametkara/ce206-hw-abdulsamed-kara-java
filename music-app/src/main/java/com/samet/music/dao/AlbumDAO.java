package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private final SongDAO songDAO;
    private Connection connection;

    public AlbumDAO() {
        this.songDAO = new SongDAO();
        
        System.out.println("AlbumDAO initializing");
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Database connection established for AlbumDAO");
            
            String sql = "CREATE TABLE IF NOT EXISTS albums (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "artist TEXT NOT NULL," +
                    "year INTEGER," +
                    "genre TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("AlbumDAO: albums table check/creation completed");
            
            // Create album_songs table to map songs to albums
            sql = "CREATE TABLE IF NOT EXISTS album_songs (" +
                    "album_id INTEGER," +
                    "song_id INTEGER," +
                    "PRIMARY KEY (album_id, song_id)," +
                    "FOREIGN KEY (album_id) REFERENCES albums(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id)" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("AlbumDAO: album_songs table check/creation completed");
        } catch (SQLException e) {
            System.err.println("Error initializing album database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Constructor with connection and songDAO for testing
     * @param connection database connection
     * @param songDAO song data access object
     */
    public AlbumDAO(Connection connection, SongDAO songDAO) {
        this.connection = connection;
        this.songDAO = songDAO;
    }
    
    /**
     * Add a new album with basic information (KULLANICI ID'Sİ GEREKLİ)
     * @param title Album title
     * @param artist Artist name
     * @param year Release year
     * @param genre Genre
     * @param userId Kullanıcı ID'si
     * @return true if added successfully
     */
    public boolean addAlbum(String title, String artist, String year, String genre, int userId) {
        String sql = "INSERT INTO albums (title, artist, year, genre, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setInt(3, Integer.parseInt(year));
            pstmt.setString(4, genre);
            pstmt.setInt(5, userId);
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
    public boolean addAlbum(String title, String artist, String year, String genre) {
        // TODO: Kullanıcı ID'si gereklidir! Lütfen yeni fonksiyonu kullanın.
        return false;
    }
    
    /**
     * Update an existing album
     * @param oldTitle original title for identifying the album
     * @param oldArtist original artist for identifying the album
     * @param newTitle new title
     * @param newArtist new artist
     * @param newYear new year
     * @param newGenre new genre
     * @return true if update was successful
     */
    public boolean updateAlbum(String oldTitle, String oldArtist, 
                            String newTitle, String newArtist, String newYear, String newGenre) {
        String sql = "UPDATE albums SET title = ?, artist = ?, year = ?, genre = ? " +
                    "WHERE title = ? AND artist = ?";
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newArtist);
            pstmt.setInt(3, Integer.parseInt(newYear));
            pstmt.setString(4, newGenre);
            pstmt.setString(5, oldTitle);
            pstmt.setString(6, oldArtist);
            
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
     * Delete an album by title and artist
     * @param title Album title
     * @param artist Artist name
     * @return true if deleted successfully
     */
    public boolean deleteAlbum(String title, String artist) {
        String sql = "DELETE FROM albums WHERE title = ? AND artist = ?";
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            
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
     * Get all albums as string arrays for display
     * @return List of string arrays containing [title, artist, year, genre]
     */
    public List<String[]> getAllAlbums() {
        List<String[]> albums = new ArrayList<>();
        String sql = "SELECT title, artist, year, genre FROM albums";
        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                albums.add(new String[]{
                    rs.getString("title"),
                    rs.getString("artist"),
                    String.valueOf(rs.getInt("year")),
                    rs.getString("genre")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public boolean create(Album album) {
        String sql = "INSERT INTO albums (title, artist, year, genre, user_id) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean previousAutoCommit = true;
        
        try {
            conn = DatabaseUtil.getConnection();
            
            // Save the current auto-commit state and set to false for transaction
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, album.getTitle());
            pstmt.setString(2, album.getArtist());
            pstmt.setInt(3, album.getYear());
            pstmt.setString(4, album.getGenre());
            pstmt.setInt(5, album.getUserId());
                
            int affectedRows = pstmt.executeUpdate();
                
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
                
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    album.setId(generatedKeys.getInt(1));
                        
                    // Add songs to the album if any
                    if (album.getSongs() != null && !album.getSongs().isEmpty()) {
                        if (!addSongsToAlbum(conn, album.getId(), album.getSongs())) {
                            conn.rollback();
                            return false;
                        }
                    }
                        
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
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

    public Album findById(int id) {
        if (connection == null) {
            return null;
        }
        
        String sql = "SELECT * FROM albums WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Album album = mapResultSetToAlbum(rs);
                    album.setSongs(getSongsByAlbumId(id));
                    return album;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Album> findAll() {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Album album = mapResultSetToAlbum(rs);
                albums.add(album);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public List<Album> findByUserId(int userId) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Album album = mapResultSetToAlbum(rs);
                    album.setSongs(getSongsByAlbumId(album.getId()));
                    albums.add(album);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public List<Album> findByArtist(String artist) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist LIKE ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + artist + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Album album = mapResultSetToAlbum(rs);
                    album.setSongs(getSongsByAlbumId(album.getId()));
                    albums.add(album);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public boolean update(Album album) {
        String sql = "UPDATE albums SET title = ?, artist = ?, year = ?, genre = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            pstmt.setString(1, album.getTitle());
            pstmt.setString(2, album.getArtist());
            pstmt.setInt(3, album.getYear());
            pstmt.setString(4, album.getGenre());
            pstmt.setInt(5, album.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update songs if provided
                if (album.getSongs() != null) {
                    // Remove existing song associations
                    removeSongsFromAlbum(conn, album.getId());
                    
                    // Add new song associations
                    if (!album.getSongs().isEmpty() && !addSongsToAlbum(conn, album.getId(), album.getSongs())) {
                        conn.rollback();
                        return false;
                    }
                }
                
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            // First remove song associations
            removeSongsFromAlbum(conn, id);
            
            // Then delete the album
            String sql = "DELETE FROM albums WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSongsToAlbum(int albumId, List<Song> songs) {
        if (connection == null) {
            return false;
        }
        try {
            connection.setAutoCommit(false);
            boolean success = addSongsToAlbum(connection, albumId, songs);
            if (success) {
                connection.commit();
            } else {
                connection.rollback();
            }
            return success;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    private boolean addSongsToAlbum(Connection conn, int albumId, List<Song> songs) throws SQLException {
        if (songs == null || songs.isEmpty()) {
            return true;
        }
        
        String sql = "INSERT INTO album_songs (album_id, song_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Song song : songs) {
                pstmt.setInt(1, albumId);
                pstmt.setInt(2, song.getId());
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

    public boolean removeSongsFromAlbum(int albumId) {
        if (connection == null) {
            return false;
        }
        try {
            connection.setAutoCommit(false);
            boolean success = removeSongsFromAlbum(connection, albumId);
            if (success) {
                connection.commit();
            } else {
                connection.rollback();
            }
            return success;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeSongsFromAlbum(Connection conn, int albumId) throws SQLException {
        String sql = "DELETE FROM album_songs WHERE album_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, albumId);
            pstmt.executeUpdate();
            return true;
        }
    }

    public List<Song> getSongsByAlbumId(int albumId) {
        List<Song> songs = new ArrayList<>();
        if (connection == null) {
            return songs;
        }
        
        String sql = "SELECT s.* FROM songs s " +
                "JOIN album_songs as_map ON s.id = as_map.song_id " +
                "WHERE as_map.album_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, albumId);
            
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

    private Album mapResultSetToAlbum(ResultSet rs) throws SQLException {
        Album album = new Album();
        album.setId(rs.getInt("id"));
        album.setTitle(rs.getString("title"));
        album.setArtist(rs.getString("artist"));
        album.setYear(rs.getInt("year"));
        album.setGenre(rs.getString("genre"));
        album.setUserId(rs.getInt("user_id"));
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            album.setCreatedAt(timestamp.toLocalDateTime());
        } else {
            album.setCreatedAt(LocalDateTime.now());
        }
        
        return album;
    }
} 