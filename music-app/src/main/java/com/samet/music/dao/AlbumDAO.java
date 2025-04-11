package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.db.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Album database operations DAO
 */
public class AlbumDAO extends BaseDAO<Album> {
    private static final Logger logger = LoggerFactory.getLogger(AlbumDAO.class);

    // Singleton instance
    private static volatile AlbumDAO instance;

    // Artist DAO reference
    private final ArtistDAO artistDAO;

    private final Connection connection;

    /**
     * Constructor
     */
    public AlbumDAO(DatabaseConnection dbConnection) throws SQLException {
        super("albums");
        this.artistDAO = new ArtistDAO(dbConnection);
        this.connection = dbConnection.getConnection();
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized AlbumDAO getInstance(DatabaseConnection dbConnection) throws SQLException {
        if (instance == null) {
            instance = new AlbumDAO(dbConnection);
        }
        return instance;
    }

    @Override
    public boolean insert(Album album) {
        if (album == null) {
            logger.warn("Cannot insert null album");
            return false;
        }

        logger.debug("Inserting album: {} (ID: {})", album.getName(), album.getId());

        String sql = "INSERT INTO albums (id, name, artist_id, release_year, genre) VALUES (?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                album.getId(),
                album.getName(),
                album.getArtist() != null ? album.getArtist().getId() : null,
                album.getReleaseYear(),
                album.getGenre());

        boolean success = result > 0;
        if (success) {
            logger.info("Album successfully inserted: {}", album.getName());
        } else {
            logger.warn("Failed to insert album: {}", album.getName());
        }

        return success;
    }

    @Override
    public Album getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return null;
        }

        logger.debug("Getting album by ID: {}", id);

        String sql = "SELECT * FROM albums WHERE id = ?";
        return querySingle(sql, this::mapAlbum, id);
    }

    @Override
    public List<Album> getAll() {
        logger.debug("Getting all albums");

        String sql = "SELECT * FROM albums";
        List<Album> albums = queryList(sql, this::mapAlbum);

        logger.debug("Found {} albums", albums.size());
        return albums;
    }

    @Override
    public boolean update(Album album) {
        if (album == null) {
            logger.warn("Cannot update null album");
            return false;
        }

        logger.debug("Updating album: {} (ID: {})", album.getName(), album.getId());

        String sql = "UPDATE albums SET name = ?, artist_id = ?, release_year = ?, genre = ? WHERE id = ?";
        int result = executeUpdate(sql,
                album.getName(),
                album.getArtist() != null ? album.getArtist().getId() : null,
                album.getReleaseYear(),
                album.getGenre(),
                album.getId());

        boolean success = result > 0;
        if (success) {
            logger.info("Album successfully updated: {}", album.getName());
        } else {
            logger.warn("Failed to update album: {}", album.getName());
        }

        return success;
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return false;
        }

        logger.debug("Deleting album. ID: {}", id);

        return executeTransaction(conn -> {
            try {
                // First remove references from playlists
                String playlistSql = "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE album_id = ?)";
                var playlistStmt = conn.prepareStatement(playlistSql);
                playlistStmt.setString(1, id);
                playlistStmt.executeUpdate();
                playlistStmt.close();

                // Delete songs in album
                String songsSql = "DELETE FROM songs WHERE album_id = ?";
                var songsStmt = conn.prepareStatement(songsSql);
                songsStmt.setString(1, id);
                songsStmt.executeUpdate();
                songsStmt.close();

                // Delete album
                String albumSql = "DELETE FROM albums WHERE id = ?";
                var albumStmt = conn.prepareStatement(albumSql);
                albumStmt.setString(1, id);
                int result = albumStmt.executeUpdate();
                albumStmt.close();

                boolean success = result > 0;
                if (success) {
                    logger.info("Album and related songs successfully deleted. ID: {}", id);
                } else {
                    logger.warn("Failed to delete album. ID: {}", id);
                }

                return success;
            } catch (SQLException e) {
                logger.error("Error in delete transaction: {}", e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Deletes an album but keeps its songs
     *
     * @param id Album ID
     * @return true if successful
     */
    public boolean deleteWithoutSongs(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return false;
        }

        logger.debug("Removing album while keeping songs. ID: {}", id);

        return executeTransaction(conn -> {
            try {
                // Update songs to remove album reference
                String updateSongsSql = "UPDATE songs SET album_id = NULL WHERE album_id = ?";
                var updateSongsStmt = conn.prepareStatement(updateSongsSql);
                updateSongsStmt.setString(1, id);
                int songsUpdated = updateSongsStmt.executeUpdate();
                updateSongsStmt.close();

                // Delete album
                String albumSql = "DELETE FROM albums WHERE id = ?";
                var albumStmt = conn.prepareStatement(albumSql);
                albumStmt.setString(1, id);
                int result = albumStmt.executeUpdate();
                albumStmt.close();

                boolean success = result > 0;
                if (success) {
                    logger.info("Album deleted, {} songs updated to remove album reference. ID: {}", songsUpdated, id);
                } else {
                    logger.warn("Failed to delete album. ID: {}", id);
                }

                return success;
            } catch (SQLException e) {
                logger.error("Error in delete transaction: {}", e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Gets albums by artist
     *
     * @param artistId Artist ID
     * @return List of albums
     */
    public List<Album> getByArtist(String artistId) {
        if (artistId == null || artistId.isEmpty()) {
            logger.warn("Invalid artist ID");
            return List.of();
        }

        logger.debug("Getting albums by artist. Artist ID: {}", artistId);

        String sql = "SELECT * FROM albums WHERE artist_id = ?";
        List<Album> albums = queryList(sql, this::mapAlbum, artistId);

        logger.debug("Found {} albums for artist", albums.size());
        return albums;
    }

    /**
     * Searches albums by name
     *
     * @param name Name to search for (partial match)
     * @return List of matching albums
     */
    public List<Album> searchByName(String name) {
        if (name == null || name.isEmpty()) {
            logger.warn("Invalid album name");
            return List.of();
        }

        logger.debug("Searching albums by name: {}", name);

        String sql = "SELECT * FROM albums WHERE name LIKE ?";
        List<Album> albums = queryList(sql, this::mapAlbum, "%" + name + "%");

        logger.debug("Found {} albums for search", albums.size());
        return albums;
    }

    /**
     * Gets albums by genre
     *
     * @param genre Genre
     * @return List of albums
     */
    public List<Album> getByGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            logger.warn("Invalid genre");
            return List.of();
        }

        logger.debug("Getting albums by genre: {}", genre);

        String sql = "SELECT * FROM albums WHERE genre LIKE ?";
        List<Album> albums = queryList(sql, this::mapAlbum, "%" + genre + "%");

        logger.debug("Found {} albums for genre", albums.size());
        return albums;
    }

    /**
     * Maps ResultSet to Album object
     *
     * @param rs ResultSet
     * @return Album object
     */
    private Album mapAlbum(ResultSet rs) throws SQLException {
        // Get basic data
        String id = rs.getString("id");
        String name = rs.getString("name");
        int releaseYear = rs.getInt("release_year");
        String genre = rs.getString("genre");
        String artistId = rs.getString("artist_id");

        // Get related artist
        Artist artist = null;
        if (artistId != null) {
            artist = artistDAO.getById(artistId);
        }

        // Create album object with preserved ID
        Album album = new Album(name, artist, releaseYear) {
            @Override
            public String getId() {
                return id;
            }
        };

        album.setGenre(genre != null ? genre : "Unknown");

        return album;
    }

    /**
     * Creates the albums table if it doesn't exist
     */
    public void createTable() {
        logger.debug("Creating albums table...");

        String sql = "CREATE TABLE IF NOT EXISTS albums (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "artist_id TEXT, " +
                "release_year INTEGER, " +
                "genre TEXT, " +
                "FOREIGN KEY (artist_id) REFERENCES artists(id))";

        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.execute();
                    logger.info("Albums table created or already exists");
                    return true;
                } catch (SQLException e) {
                    logger.error("Error creating albums table: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing createTable: {}", e.getMessage());
        }
    }

    @Override
    protected Album mapResultSetToEntity(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String artistId = rs.getString("artist_id");
        int releaseYear = rs.getInt("release_year");
        String genre = rs.getString("genre");

        Artist artist = null;
        if (artistId != null) {
            artist = artistDAO.getById(artistId);
        }

        Album album = new Album(name, artist, releaseYear);
        album.setId(id);
        album.setGenre(genre);
        return album;
    }

    @Override
    protected void setCreateStatementParameters(PreparedStatement stmt, Album album) throws SQLException {
        stmt.setString(1, album.getId());
        stmt.setString(2, album.getName());
        stmt.setString(3, album.getArtist() != null ? album.getArtist().getId() : null);
        stmt.setInt(4, album.getReleaseYear());
        stmt.setString(5, album.getGenre());
    }

    @Override
    protected void setUpdateStatementParameters(PreparedStatement stmt, Album album) throws SQLException {
        stmt.setString(1, album.getName());
        stmt.setString(2, album.getArtist() != null ? album.getArtist().getId() : null);
        stmt.setInt(3, album.getReleaseYear());
        stmt.setString(4, album.getGenre());
        stmt.setString(5, album.getId());
    }
}