package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.SQLQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for playlist database operations
 */
public class PlaylistDAO extends BaseDAO<Playlist> {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistDAO.class);
    
    // Thread safety lock
    private static final Object LOCK = new Object();
    
    // Database connection
    private final DatabaseConnection dbConnection;

    // SQL queries from centralized SQLQueries class
    private static final String SQL_INSERT = SQLQueries.Playlist.INSERT;
    private static final String SQL_SELECT_BY_ID = SQLQueries.Playlist.SELECT_BY_ID;
    private static final String SQL_SELECT_ALL = SQLQueries.Playlist.SELECT_ALL;
    private static final String SQL_UPDATE = SQLQueries.Playlist.UPDATE;
    private static final String SQL_DELETE = SQLQueries.Playlist.DELETE;
    private static final String SQL_ADD_SONG = SQLQueries.Playlist.ADD_SONG;
    private static final String SQL_REMOVE_SONG = SQLQueries.Playlist.REMOVE_SONG;
    private static final String SQL_SELECT_SONGS = SQLQueries.Playlist.SELECT_SONGS;
    private static final String SQL_SELECT_PLAYLISTS_FOR_SONG = SQLQueries.Playlist.SELECT_PLAYLISTS_FOR_SONG;
    private static final String SQL_DELETE_ALL_SONGS = SQLQueries.Playlist.DELETE_ALL_SONGS;
    private static final String SQL_SEARCH_BY_NAME = SQLQueries.Playlist.SEARCH_BY_NAME;

    // Singleton instance
    private static volatile PlaylistDAO instance;

    // Song DAO reference
    private SongDAO songDAO;

    /**
     * Constructor
     */
    public PlaylistDAO(DatabaseConnection dbConnection) throws SQLException {
        super("playlists");
        this.dbConnection = dbConnection;
        this.songDAO = new SongDAO(dbConnection);
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized PlaylistDAO getInstance(DatabaseConnection dbConnection) throws SQLException {
        if (instance == null) {
            instance = new PlaylistDAO(dbConnection);
        }
        return instance;
    }

    /**
     * Sets the SongDAO instance (for testing)
     */
    public void setSongDAO(SongDAO songDAO) {
        this.songDAO = songDAO;
    }

    @Override
    public boolean insert(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot insert null playlist");
            return false;
        }

        logger.debug("Inserting playlist: {} (ID: {})", playlist.getName(), playlist.getId());

        synchronized (LOCK) {
            return executeTransaction(conn -> {
                try {
                    // Insert the playlist
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
                        stmt.setString(1, playlist.getId());
                        stmt.setString(2, playlist.getName());
                        stmt.setString(3, playlist.getDescription());
                        int result = stmt.executeUpdate();

                        if (result <= 0) {
                            logger.warn("Failed to insert playlist: {}", playlist.getName());
                            return false;
                        }
                    }

                    // Add songs to playlist if there are any
                    List<Song> songs = playlist.getSongs();
                    if (!songs.isEmpty()) {
                        try (PreparedStatement stmt = conn.prepareStatement(SQL_ADD_SONG)) {
                            for (Song song : songs) {
                                stmt.setString(1, playlist.getId());
                                stmt.setString(2, song.getId());
                                stmt.addBatch();
                            }
                            stmt.executeBatch();
                        }
                    }

                    logger.info("Playlist successfully inserted: {}", playlist.getName());
                    return true;
                } catch (SQLException e) {
                    return false;
                }
            });
        }
    }

    @Override
    public Playlist getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid playlist ID");
            return null;
        }

        logger.debug("Getting playlist by ID: {}", id);

        synchronized (LOCK) {
            // First get the playlist basic info
            Playlist playlist = querySingle(SQL_SELECT_BY_ID, this::mapPlaylist, id);

            if (playlist != null) {
                // Then get the songs in this playlist
                List<Song> songs = getPlaylistSongs(id);
                for (Song song : songs) {
                    playlist.addSong(song);
                }

                logger.debug("Playlist found: {} ({} songs)", playlist.getName(), songs.size());
            } else {
                logger.debug("Playlist not found. ID: {}", id);
            }

            return playlist;
        }
    }

    @Override
    public List<Playlist> getAll() {
        logger.debug("Getting all playlists");

        synchronized (LOCK) {
            List<Playlist> playlists = queryList(SQL_SELECT_ALL, this::mapPlaylist);

            // Load songs for each playlist
            for (Playlist playlist : playlists) {
                List<Song> songs = getPlaylistSongs(playlist.getId());
                for (Song song : songs) {
                    playlist.addSong(song);
                }
            }

            logger.debug("Found {} playlists", playlists.size());
            return playlists;
        }
    }

    @Override
    public boolean update(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot update null playlist");
            return false;
        }

        logger.debug("Updating playlist: {} (ID: {})", playlist.getName(), playlist.getId());

        synchronized (LOCK) {
            int result = executeUpdate(SQL_UPDATE,
                    playlist.getName(),
                    playlist.getDescription(),
                    playlist.getId());

            boolean success = result > 0;
            if (success) {
                logger.info("Playlist successfully updated: {}", playlist.getName());
            } else {
                logger.warn("Failed to update playlist: {}", playlist.getName());
            }

            return success;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid playlist ID");
            return false;
        }

        logger.debug("Deleting playlist. ID: {}", id);

        synchronized (LOCK) {
            return executeTransaction(conn -> {
                try {
                    // First remove playlist-song relationships
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_ALL_SONGS)) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }

                    // Then delete the playlist
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
                        stmt.setString(1, id);
                        int result = stmt.executeUpdate();

                        boolean success = result > 0;
                        if (success) {
                            logger.info("Playlist successfully deleted. ID: {}", id);
                        } else {
                            logger.warn("Failed to delete playlist. ID: {}", id);
                        }

                        return success;
                    }
                } catch (SQLException e) {
                    return false;
                }
            });
        }
    }

    /**
     * Adds a song to a playlist
     *
     * @param playlistId Playlist ID
     * @param songId Song ID
     * @return true if successful
     */
    public boolean addSongToPlaylist(String playlistId, String songId) {
        if (playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
            logger.warn("Invalid playlist or song ID");
            return false;
        }

        logger.debug("Adding song to playlist. Playlist ID: {}, Song ID: {}", playlistId, songId);

        synchronized (LOCK) {
            int result = executeUpdate(SQL_ADD_SONG, playlistId, songId);

            boolean success = result > 0;
            if (success) {
                logger.info("Song added to playlist. Playlist ID: {}, Song ID: {}", playlistId, songId);
            } else {
                logger.warn("Failed to add song to playlist (might already exist). Playlist ID: {}, Song ID: {}", playlistId, songId);
            }

            return success;
        }
    }

    /**
     * Removes a song from a playlist
     *
     * @param playlistId Playlist ID
     * @param songId Song ID
     * @return true if successful
     */
    public boolean removeSongFromPlaylist(String playlistId, String songId) {
        if (playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
            logger.warn("Invalid playlist or song ID");
            return false;
        }

        logger.debug("Removing song from playlist. Playlist ID: {}, Song ID: {}", playlistId, songId);

        synchronized (LOCK) {
            int result = executeUpdate(SQL_REMOVE_SONG, playlistId, songId);

            boolean success = result > 0;
            if (success) {
                logger.info("Song removed from playlist. Playlist ID: {}, Song ID: {}", playlistId, songId);
            } else {
                logger.warn("Failed to remove song from playlist. Playlist ID: {}, Song ID: {}", playlistId, songId);
            }

            return success;
        }
    }

    /**
     * Gets songs in a playlist
     *
     * @param playlistId Playlist ID
     * @return List of songs
     */
    public List<Song> getPlaylistSongs(String playlistId) {
        if (playlistId == null || playlistId.isEmpty()) {
            logger.warn("Invalid playlist ID");
            return new ArrayList<>();
        }

        logger.debug("Getting songs for playlist. Playlist ID: {}", playlistId);

        synchronized (LOCK) {
            List<Song> songs = new ArrayList<>();

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = dbConnection.getConnection();
                stmt = conn.prepareStatement(SQL_SELECT_SONGS);
                stmt.setString(1, playlistId);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String songId = rs.getString("song_id");
                    Song song = songDAO.getById(songId);

                    if (song != null) {
                        songs.add(song);
                    } else {
                        logger.warn("Found non-existent song in playlist: {}", songId);
                    }
                }

                logger.debug("Found {} songs for playlist", songs.size());
            } catch (SQLException e) {
                logger.error("Error getting playlist songs: {}", e.getMessage(), e);
            } finally {
                closeResources(rs, stmt, conn);
            }

            return songs;
        }
    }

    /**
     * Gets playlists containing a song
     *
     * @param songId Song ID
     * @return List of playlists
     */
    public List<Playlist> getPlaylistsContainingSong(String songId) {
        if (songId == null || songId.isEmpty()) {
            logger.warn("Invalid song ID");
            return new ArrayList<>();
        }

        logger.debug("Getting playlists containing song. Song ID: {}", songId);

        synchronized (LOCK) {
            List<Playlist> playlists = queryList(SQL_SELECT_PLAYLISTS_FOR_SONG, this::mapPlaylist, songId);

            // Load songs for each playlist
            for (Playlist playlist : playlists) {
                List<Song> songs = getPlaylistSongs(playlist.getId());
                for (Song song : songs) {
                    playlist.addSong(song);
                }
            }

            logger.debug("Found {} playlists containing song", playlists.size());
            return playlists;
        }
    }

    /**
     * Searches playlists by name
     *
     * @param name Name to search for (partial match)
     * @return List of matching playlists
     */
    public List<Playlist> searchByName(String name) {
        if (name == null || name.isEmpty()) {
            logger.warn("Invalid playlist name");
            return List.of();
        }

        logger.debug("Searching playlists by name: {}", name);

        synchronized (LOCK) {
            List<Playlist> playlists = queryList(SQL_SEARCH_BY_NAME, this::mapPlaylist, "%" + name + "%");

            // Load songs for each playlist
            for (Playlist playlist : playlists) {
                List<Song> songs = getPlaylistSongs(playlist.getId());
                for (Song song : songs) {
                    playlist.addSong(song);
                }
            }

            logger.debug("Found {} playlists for search", playlists.size());
            return playlists;
        }
    }

    /**
     * Maps ResultSet to Playlist object
     *
     * @param rs ResultSet
     * @return Playlist object
     */
    private Playlist mapPlaylist(ResultSet rs) throws SQLException {
        // Get basic data
        String id = rs.getString("id");
        String name = rs.getString("name");
        String description = rs.getString("description");

        // Create playlist object with preserved ID
        Playlist playlist = new Playlist(name, description) {
            @Override
            public String getId() {
                return id;
            }
        };

        return playlist;
    }

    /**
     * Creates the playlists and playlist_songs tables if they don't exist
     */
    public void createTable() {
        logger.debug("Creating playlists tables...");

        String playlistSql = "CREATE TABLE IF NOT EXISTS playlists (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "description TEXT)";

        String playlistSongsSql = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                "playlist_id TEXT, " +
                "song_id TEXT, " +
                "PRIMARY KEY (playlist_id, song_id), " +
                "FOREIGN KEY (playlist_id) REFERENCES playlists(id), " +
                "FOREIGN KEY (song_id) REFERENCES songs(id))";

        try {
            // Create playlists table
            dbConnection.executeUpdate(playlistSql, stmt -> {
                try {
                    stmt.execute();
                    logger.info("Playlists table created or already exists");
                    return true;
                } catch (SQLException e) {
                    logger.error("Error creating playlists table: {}", e.getMessage());
                    return false;
                }
            });

            // Create playlist_songs table
            dbConnection.executeUpdate(playlistSongsSql, stmt -> {
                try {
                    stmt.execute();
                    logger.info("Playlist_songs table created or already exists");
                    return true;
                } catch (SQLException e) {
                    logger.error("Error creating playlist_songs table: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing createTable: {}", e.getMessage());
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Error closing ResultSet: {}", e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing PreparedStatement: {}", e.getMessage());
            }
        }
        try {
            DatabaseConnection.closeConnection();
        } catch (SQLException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }

    @Override
    protected Playlist mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapPlaylist(rs);
    }

    @Override
    protected void setCreateStatementParameters(PreparedStatement stmt, Playlist playlist) throws SQLException {
        stmt.setString(1, playlist.getId());
        stmt.setString(2, playlist.getName());
        stmt.setString(3, playlist.getDescription());
    }

    @Override
    protected void setUpdateStatementParameters(PreparedStatement stmt, Playlist playlist) throws SQLException {
        stmt.setString(1, playlist.getName());
        stmt.setString(2, playlist.getDescription());
        stmt.setString(3, playlist.getId());
    }
}