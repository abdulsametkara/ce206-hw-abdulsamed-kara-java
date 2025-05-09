package com.samet.music.dao;

import com.samet.music.model.Artist;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Artist entities
 */
public class ArtistDAO {
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;

    public ArtistDAO() {
        this.songDAO = new SongDAO();
        this.albumDAO = new AlbumDAO();
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Create artists table
            String sql = "CREATE TABLE IF NOT EXISTS artists (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "bio TEXT," +
                    "country TEXT," +
                    "genre TEXT," +
                    "user_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("ArtistDAO: artists table check/creation completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new artist with basic information (KULLANICI ID'Sİ GEREKLİ)
     * @param name Artist name
     * @param country Country of origin
     * @param genre Primary genre
     * @param userId Kullanıcı ID'si
     * @return true if added successfully
     */
    public boolean addArtist(String name, String country, String genre, int userId) {
        String sql = "INSERT INTO artists (name, country, genre, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, country);
            pstmt.setString(3, genre);
            pstmt.setInt(4, userId);
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
     * Update an existing artist
     * @param oldName Original name for identifying the artist
     * @param newName New name
     * @param newCountry New country
     * @param newGenre New genre
     * @return true if update was successful
     */
    public boolean updateArtist(String oldName, String newName, String newCountry, String newGenre) {
        String sql = "UPDATE artists SET name = ?, country = ?, genre = ? WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setString(2, newCountry);
            pstmt.setString(3, newGenre);
            pstmt.setString(4, oldName);
            
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
     * Delete an artist by name
     * @param name Artist name
     * @return true if deleted successfully
     */
    public boolean deleteArtist(String name) {
        String sql = "DELETE FROM artists WHERE name = ?";
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
     * Get all artists as string arrays for display
     * @return List of string arrays containing [name, country, genre]
     */
    public List<String[]> getAllArtists() {
        List<String[]> artists = new ArrayList<>();
        String sql = "SELECT name, country, genre FROM artists";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                artists.add(new String[]{
                    rs.getString("name"),
                    rs.getString("country"),
                    rs.getString("genre")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }

    /**
     * Create a new artist in the database
     * @param artist the artist to create
     * @return the created artist with ID
     */
    public Artist create(Artist artist) {
        String sql = "INSERT INTO artists (name, bio, user_id) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            boolean previousAutoCommit = conn.getAutoCommit();
            if (previousAutoCommit) {
                conn.setAutoCommit(false);
            }
            
            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getBio());
            pstmt.setInt(3, artist.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        artist.setId(generatedKeys.getInt(1));
                        if (!previousAutoCommit) {
                            conn.commit();
                        }
                        return artist;
                    }
                }
            }
            
            if (!previousAutoCommit) {
                conn.rollback();
            }
            
            // Restore auto-commit mode if we changed it
            if (previousAutoCommit) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find an artist by ID
     * @param id the artist ID
     * @return the artist, or null if not found
     */
    public Artist findById(int id) {
        String sql = "SELECT * FROM artists WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Artist artist = mapResultSetToArtist(rs);
                    loadRelatedData(artist);
                    return artist;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find an artist by name
     * @param name the artist name
     * @return the artist, or null if not found
     */
    public Artist findByName(String name) {
        String sql = "SELECT * FROM artists WHERE name = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Artist artist = mapResultSetToArtist(rs);
                    loadRelatedData(artist);
                    return artist;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find all artists
     * @return a list of all artists
     */
    public List<Artist> findAll() {
        String sql = "SELECT * FROM artists ORDER BY name";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Artist artist = mapResultSetToArtist(rs);
                artists.add(artist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return artists;
    }

    /**
     * Find all artists for a specific user
     * @param userId the user ID
     * @return a list of artists for the user
     */
    public List<Artist> findByUserId(int userId) {
        String sql = "SELECT * FROM artists WHERE user_id = ? ORDER BY name";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artist artist = mapResultSetToArtist(rs);
                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return artists;
    }

    /**
     * Update an artist's information
     * @param artist the artist to update
     * @return true if the update was successful, false otherwise
     */
    public boolean update(Artist artist) {
        String sql = "UPDATE artists SET name = ?, bio = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getBio());
            pstmt.setInt(3, artist.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Delete an artist
     * @param id the artist ID
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM artists WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all available artist names
     * @return a set of artist names
     */
    public Set<String> getAllArtistNames() {
        Set<String> artists = new HashSet<>();
        
        // Add artists from the artists table
        String sql = "SELECT DISTINCT name FROM artists";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                artists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Also get artists from songs and albums (they might not be in the artists table)
        try {
            for (Song song : songDAO.findAll()) {
                if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                    artists.add(song.getArtist());
                }
            }
            
            for (Album album : albumDAO.findAll()) {
                if (album.getArtist() != null && !album.getArtist().isEmpty()) {
                    artists.add(album.getArtist());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return artists;
    }

    /**
     * Check if an artist exists
     * @param name the artist name
     * @return true if the artist exists, false otherwise
     */
    public boolean artistExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        return getAllArtistNames().stream()
                .anyMatch(artist -> artist.equalsIgnoreCase(name.trim()));
    }

    /**
     * Get the number of songs for an artist
     * @param artistName the artist name
     * @return the number of songs
     */
    public int getArtistSongCount(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return 0;
        }
        
        List<Song> songs = songDAO.findByArtist(artistName);
        return songs.size();
    }

    /**
     * Get the number of albums for an artist
     * @param artistName the artist name
     * @return the number of albums
     */
    public int getArtistAlbumCount(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return 0;
        }
        
        List<Album> albums = albumDAO.findByArtist(artistName);
        return albums.size();
    }

    /**
     * Map a ResultSet to an Artist object
     * @param rs the ResultSet
     * @return the Artist object
     * @throws SQLException if a database access error occurs
     */
    private Artist mapResultSetToArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setId(rs.getInt("id"));
        artist.setName(rs.getString("name"));
        artist.setBio(rs.getString("bio"));
        artist.setUserId(rs.getInt("user_id"));
        
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            artist.setCreatedAt(timestamp.toLocalDateTime());
        } else {
            artist.setCreatedAt(LocalDateTime.now());
        }
        
        return artist;
    }

    /**
     * Load related data (songs and albums) for an artist
     * @param artist the artist to load data for
     */
    private void loadRelatedData(Artist artist) {
        // Load songs
        List<Song> songs = songDAO.findByArtist(artist.getName());
        artist.setSongs(songs);
        
        // Load albums
        List<Album> albums = albumDAO.findByArtist(artist.getName());
        artist.setAlbums(albums);
    }
} 