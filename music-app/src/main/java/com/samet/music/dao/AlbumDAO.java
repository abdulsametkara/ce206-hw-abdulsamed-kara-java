package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlbumDAO.class);
    private final Connection connection;
    private final SongDAO songDAO;

    public AlbumDAO() {
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            logger.error("Error establishing database connection", e);
            throw new RuntimeException("Failed to connect to database", e);
        }
        this.songDAO = new SongDAO();
    }

    public AlbumDAO(Connection connection, SongDAO songDAO) {
        this.connection = connection;
        this.songDAO = songDAO;
    }

    public boolean create(Album album) {
        String sql = "INSERT INTO albums (title, artist, year, genre, user_id) VALUES (?, ?, ?, ?, ?)";
        try {
            // Disable auto-commit to manage transaction
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, album.getTitle());
                statement.setString(2, album.getArtist());
                statement.setInt(3, album.getYear());
                statement.setString(4, album.getGenre());
                statement.setInt(5, album.getUserId());
                
                int affectedRows = statement.executeUpdate();
                
                if (affectedRows == 0) {
                    connection.rollback();
                    logger.error("Creating album failed, no rows affected.");
                    return false;
                }
                
                // Get the generated ID using last_insert_rowid() for SQLite
                try (Statement idStatement = connection.createStatement();
                     ResultSet generatedKeys = idStatement.executeQuery("SELECT last_insert_rowid() as id")) {
                    if (generatedKeys.next()) {
                        album.setId(generatedKeys.getInt("id"));
                        
                        // Add songs to the album if there are any
                        if (album.getSongs() != null && !album.getSongs().isEmpty()) {
                            if (!addSongsToAlbum(album.getId(), album.getSongs())) {
                                connection.rollback();
                                logger.error("Failed to add songs to album: {}", album.getId());
                                return false;
                            }
                        }
                        
                        connection.commit();
                        logger.info("Album created successfully with ID: {}", album.getId());
                        return true;
                    } else {
                        connection.rollback();
                        logger.error("Creating album failed, no ID obtained.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("Failed to rollback transaction", ex);
            }
            logger.error("Error creating album: {}", e.getMessage(), e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Failed to reset auto-commit", e);
            }
        }
    }

    public Album findById(int id) {
        String sql = "SELECT * FROM albums WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Album album = mapResultSetToAlbum(resultSet);
                    album.setSongs(getSongsByAlbumId(id));
                    return album;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding album by ID: {}", e.getMessage(), e);
        }
        return null;
    }

    public List<Album> findAll() {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums ORDER BY created_at DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Album album = mapResultSetToAlbum(resultSet);
                albums.add(album);
            }
        } catch (SQLException e) {
            logger.error("Error finding all albums: {}", e.getMessage(), e);
        }
        return albums;
    }

    public List<Album> findByUserId(int userId) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Album album = mapResultSetToAlbum(resultSet);
                    albums.add(album);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding albums by user ID: {}", e.getMessage(), e);
        }
        return albums;
    }

    public List<Album> findByArtist(String artist) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist LIKE ? ORDER BY created_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + artist + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Album album = mapResultSetToAlbum(resultSet);
                    albums.add(album);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding albums by artist: {}", e.getMessage(), e);
        }
        return albums;
    }

    public boolean update(Album album) {
        String sql = "UPDATE albums SET title = ?, artist = ?, year = ?, genre = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, album.getTitle());
            statement.setString(2, album.getArtist());
            statement.setInt(3, album.getYear());
            statement.setString(4, album.getGenre());
            statement.setInt(5, album.getId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Album updated successfully: {}", album.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating album: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM albums WHERE id = ?";
        try {
            // First remove all songs from the album
            removeSongsFromAlbum(id);
            
            // Then delete the album
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                int affectedRows = statement.executeUpdate();
                if (affectedRows > 0) {
                    logger.info("Album deleted successfully: {}", id);
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Error deleting album: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean addSongsToAlbum(int albumId, List<Song> songs) {
        String sql = "UPDATE songs SET album_id = ? WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Song song : songs) {
                    statement.setInt(1, albumId);
                    statement.setInt(2, song.getId());
                    statement.addBatch();
                }
                int[] results = statement.executeBatch();
                connection.commit();
                
                // Check if all updates were successful
                for (int result : results) {
                    if (result <= 0) {
                        logger.warn("Failed to add a song to album: {}", albumId);
                        return false;
                    }
                }
                logger.info("Successfully added {} songs to album: {}", songs.size(), albumId);
                return true;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("Failed to rollback transaction", ex);
            }
            logger.error("Error adding songs to album: {}", e.getMessage(), e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Failed to reset auto-commit", e);
            }
        }
    }

    public boolean removeSongsFromAlbum(int albumId) {
        String sql = "UPDATE songs SET album_id = NULL WHERE album_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, albumId);
            int affectedRows = statement.executeUpdate();
            logger.info("Removed {} songs from album: {}", affectedRows, albumId);
            return true;
        } catch (SQLException e) {
            logger.error("Error removing songs from album: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<Song> getSongsByAlbumId(int albumId) {
        String sql = "SELECT * FROM songs WHERE album_id = ?";
        List<Song> songs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, albumId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Song song = songDAO.mapResultSetToSong(resultSet);
                    songs.add(song);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding songs by album ID: {}", e.getMessage(), e);
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
        album.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return album;
    }
} 