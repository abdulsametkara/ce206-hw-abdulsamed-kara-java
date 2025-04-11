package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Song entities
 */
public class SongDAO extends BaseDAO<Song> {
    private static final Logger logger = LoggerFactory.getLogger(SongDAO.class);

    // SQL queries
    private static final String SQL_INSERT =
            "INSERT INTO songs (id, name, artist_id, album_id, duration, genre) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM songs WHERE id = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT * FROM songs";
    private static final String SQL_UPDATE =
            "UPDATE songs SET name = ?, artist_id = ?, album_id = ?, duration = ?, genre = ? WHERE id = ?";
    private static final String SQL_DELETE =
            "DELETE FROM songs WHERE id = ?";
    private static final String SQL_DELETE_FROM_PLAYLISTS =
            "DELETE FROM playlist_songs WHERE song_id = ?";
    private static final String SQL_SELECT_BY_ARTIST =
            "SELECT * FROM songs WHERE artist_id = ?";
    private static final String SQL_SELECT_BY_ALBUM =
            "SELECT * FROM songs WHERE album_id = ?";
    private static final String SQL_SELECT_BY_GENRE =
            "SELECT * FROM songs WHERE genre LIKE ?";

    // Singleton instance
    private static SongDAO instance;
    private final Connection connection;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;
    private final DatabaseConnection dbConnection;

    private static final Object LOCK = new Object();

    /**
     * Default constructor
     */
    public SongDAO(DatabaseConnection dbConnection) throws SQLException {
        super(dbConnection);
        this.dbConnection = dbConnection;
        this.connection = dbConnection.getConnection();
        this.artistDAO = ArtistDAO.getInstance(dbConnection);
        this.albumDAO = AlbumDAO.getInstance(dbConnection);
        createTable();
    }

    /**
     * Get singleton instance
     */
    public static synchronized SongDAO getInstance(DatabaseConnection dbConnection) throws SQLException {
        if (instance == null) {
            instance = new SongDAO(dbConnection);
        }
        return instance;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "artist_id TEXT," +
                    "duration INTEGER," +
                    "FOREIGN KEY (artist_id) REFERENCES artists(id))";
        
        try {
            dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    logger.error("Error executing create table statement", e);
                    return false;
                }
            });
            logger.info("Songs table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating songs table", e);
            throw new RuntimeException("Failed to create songs table", e);
        }
    }

    @Override
    public boolean insert(Song song) {
        if (song == null) {
            logger.warn("Attempted to insert null song");
            return false;
        }

        String sql = "INSERT INTO songs (id, name, artist_id, duration) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, song.getId());
            stmt.setString(2, song.getName());
            stmt.setString(3, song.getArtist().getId());
            stmt.setInt(4, song.getDuration());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error inserting song: " + song.getName(), e);
            throw new RuntimeException("Failed to insert song", e);
        }
    }

    @Override
    public Song getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT s.*, a.name as artist_name FROM songs s " +
                    "LEFT JOIN artists a ON s.artist_id = a.id " +
                    "WHERE s.id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSong(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting song by id: {}", id, e);
            throw new RuntimeException("Failed to get song", e);
        }
        return null;
    }

    @Override
    public List<Song> getAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.*, a.name as artist_name FROM songs s " +
                    "JOIN artists a ON s.artist_id = a.id";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                songs.add(mapResultSetToSong(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all songs", e);
            throw new RuntimeException("Failed to get songs", e);
        }
        return songs;
    }

    @Override
    public boolean update(Song song) {
        if (song == null || song.getId() == null) {
            logger.warn("Cannot update null song or song with null ID");
            return false;
        }

        logger.debug("Updating song: {} (ID: {})", song.getName(), song.getId());

        synchronized (LOCK) {
            int result = executeUpdate(SQL_UPDATE,
                    song.getName(),
                    song.getArtist() != null ? song.getArtist().getId() : null,
                    song.getAlbum() != null ? song.getAlbum().getId() : null,
                    song.getDuration(),
                    song.getGenre(),
                    song.getId());

            boolean success = result > 0;
            if (success) {
                logger.info("Song successfully updated: {}", song.getName());
            } else {
                logger.warn("Failed to update song: {}", song.getName());
            }

            return success;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Cannot delete song with null or empty ID");
            return false;
        }

        logger.debug("Deleting song. ID: {}", id);

        synchronized (LOCK) {
            return executeTransaction(conn -> {
                try {
                    // First remove references from playlists
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_FROM_PLAYLISTS)) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }

                    // Then delete the song
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
                        stmt.setString(1, id);
                        int result = stmt.executeUpdate();

                        boolean success = result > 0;
                        if (success) {
                            logger.info("Song successfully deleted. ID: {}", id);
                        } else {
                            logger.warn("Failed to delete song. ID: {}", id);
                        }

                        return success;
                    }
                } catch (SQLException e) {
                    logger.error("Error in delete transaction: {}", e.getMessage(), e);
                    return false;
                }
            });
        }
    }

    /**
     * Gets songs by artist
     * @param artistId Artist ID
     * @return List of songs
     */
    public List<Song> getByArtist(String artistId) {
        if (artistId == null || artistId.isEmpty()) {
            logger.warn("Invalid artist ID");
            return List.of();
        }

        logger.debug("Getting songs by artist. Artist ID: {}", artistId);

        synchronized (LOCK) {
            List<Song> songs = queryList(SQL_SELECT_BY_ARTIST, this::mapSong, artistId);
            logger.debug("Found {} songs for artist", songs.size());
            return songs;
        }
    }

    /**
     * Gets songs by album
     * @param albumId Album ID
     * @return List of songs
     */
    public List<Song> getByAlbum(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            logger.warn("Invalid album ID");
            return List.of();
        }

        logger.debug("Getting songs by album. Album ID: {}", albumId);

        synchronized (LOCK) {
            List<Song> songs = queryList(SQL_SELECT_BY_ALBUM, this::mapSong, albumId);
            logger.debug("Found {} songs for album", songs.size());
            return songs;
        }
    }

    /**
     * Searches songs by name
     * @param name Name to search for (partial match)
     * @return List of matching songs
     */
    public List<Song> searchByName(String name) {
        if (name == null || name.isEmpty()) {
            logger.warn("Invalid song name");
            return List.of();
        }

        logger.debug("Searching songs by name: {}", name);

        synchronized (LOCK) {
            String sql = "SELECT * FROM songs WHERE name LIKE ?";
            List<Song> songs = queryList(sql, this::mapSong, "%" + name + "%");
            logger.debug("Found {} songs for search", songs.size());
            return songs;
        }
    }

    /**
     * Gets songs by genre
     * @param genre Genre to filter by
     * @return List of songs
     */
    public List<Song> getByGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            logger.warn("Invalid genre");
            return List.of();
        }

        logger.debug("Getting songs by genre: {}", genre);

        synchronized (LOCK) {
            List<Song> songs = queryList(SQL_SELECT_BY_GENRE, this::mapSong, "%" + genre + "%");
            logger.debug("Found {} songs for genre", songs.size());
            return songs;
        }
    }

    /**
     * Maps ResultSet to Song object
     * @param rs ResultSet
     * @return Song object
     */
    private Song mapSong(ResultSet rs) throws SQLException {
        // Get basic data
        String id = rs.getString("id");
        String name = rs.getString("name");
        int duration = rs.getInt("duration");
        String genre = rs.getString("genre");
        String artistId = rs.getString("artist_id");
        String albumId = rs.getString("album_id");

        // Get related entities - using lazy loading to avoid circular dependencies
        Artist artist = null;
        Album album = null;

        if (artistId != null) {
            artist = artistDAO.getById(artistId);
        }

        if (albumId != null) {
            album = albumDAO.getById(albumId);
        }

        // Create song object with preserved ID
        Song song = new Song(name, artist, duration) {
            @Override
            public String getId() {
                return id;
            }
        };

        song.setGenre(genre != null ? genre : "Unknown");

        if (album != null) {
            song.setAlbum(album);
        }

        return song;
    }

    private Song mapResultSetToSong(ResultSet rs) throws SQLException {
        Artist artist = artistDAO.getById(rs.getString("artist_id"));
        return new Song(rs.getString("name"), artist, rs.getInt("duration"));
    }

    @Override
    protected void setUpdateStatementParameters(PreparedStatement stmt, Song song) throws SQLException {
        stmt.setString(1, song.getName());
        stmt.setString(2, song.getArtist() != null ? song.getArtist().getId() : null);
        stmt.setString(3, song.getAlbum() != null ? song.getAlbum().getId() : null);
        stmt.setInt(4, song.getDuration());
        stmt.setString(5, song.getGenre());
        stmt.setString(6, song.getId());
    }

    @Override
    protected Song mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapResultSetToSong(rs);
    }

    @Override
    protected void setCreateStatementParameters(PreparedStatement stmt, Song song) throws SQLException {
        stmt.setString(1, song.getId());
        stmt.setString(2, song.getName());
        stmt.setString(3, song.getArtist() != null ? song.getArtist().getId() : null);
        stmt.setString(4, song.getAlbum() != null ? song.getAlbum().getId() : null);
        stmt.setInt(5, song.getDuration());
        stmt.setString(6, song.getGenre());
    }
}