package com.samet.music.service;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.monitoring.MetricsCollector;
import com.samet.music.repository.AlbumCollection;
import com.samet.music.repository.ArtistCollection;
import com.samet.music.repository.PlaylistCollection;
import com.samet.music.repository.SongCollection;
import com.samet.music.util.DatabaseManager;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for music collection operations
 * Implements Singleton pattern
 */
public class MusicCollectionService {
    private static final Logger logger = LoggerFactory.getLogger(MusicCollectionService.class);

    // Collection managers
    private final ArtistCollection artistCollection;
    private final AlbumCollection albumCollection;
    private final SongCollection songCollection;
    private final PlaylistCollection playlistCollection;
    private final MusicFactory musicFactory;

    // DAO objects - using centralized access through DAOFactory
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    private final PlaylistDAO playlistDAO;

    // Singleton implementation
    private static MusicCollectionService instance;

    /**
     * Private constructor
     */
    private MusicCollectionService() throws SQLException {
        // Initialize collection objects
        this.artistCollection = ArtistCollection.getInstance();
        this.albumCollection = AlbumCollection.getInstance();
        this.songCollection = SongCollection.getInstance();
        this.playlistCollection = PlaylistCollection.getInstance();
        this.musicFactory = MusicFactory.getInstance();

        // Get DAO objects from DAOFactory
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.songDAO = daoFactory.getSongDAO();
        this.albumDAO = daoFactory.getAlbumDAO();
        this.artistDAO = daoFactory.getArtistDAO();
        this.playlistDAO = daoFactory.getPlaylistDAO();

        logger.info("MusicCollectionService initialized");
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized MusicCollectionService getInstance() {
        if (instance == null) {
            try {
                instance = new MusicCollectionService();
            } catch (SQLException e) {
                logger.error("Error initializing MusicCollectionService: {}", e.getMessage(), e);
            }
        }
        return instance;
    }

    // === ARTIST OPERATIONS ===

    /**
     * Adds an artist
     * @param name Artist name
     * @param biography Artist biography
     * @return true if successful
     */
    public boolean addArtist(String name, String biography) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_artist");
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Invalid artist data: name is empty or null");
                return false;
            }

            logger.info("Adding artist: {}", name);
            Artist artist = musicFactory.createArtist(name, biography);
            artistCollection.add(artist);

            // Increment metric
            MetricsCollector.getInstance().incrementArtistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding artist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Gets artist by ID
     * @param id Artist ID
     * @return Artist or null
     */
    public Artist getArtistById(String id) {
        try {
            logger.debug("Getting artist by ID: {}", id);
            return artistCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting artist by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Searches artists by name
     * @param name Artist name
     * @return List of matching artists
     */
    public List<Artist> searchArtistsByName(String name) {
        try {
            logger.debug("Searching artists by name: {}", name);
            return artistCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching artists by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets all artists
     * @return List of artists
     */
    public List<Artist> getAllArtists() {
        try {
            logger.debug("Getting all artists");
            List<Artist> allArtists = artistCollection.getAll();

            // Filter unique artists
            Map<String, Artist> uniqueArtistsById = new HashMap<>();
            for (Artist artist : allArtists) {
                uniqueArtistsById.put(artist.getId(), artist);
            }

            List<Artist> uniqueArtists = new ArrayList<>(uniqueArtistsById.values());
            logger.info("Returning {} unique artists", uniqueArtists.size());
            return uniqueArtists;
        } catch (Exception e) {
            logger.error("Error getting all artists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Removes an artist and all related albums and songs
     * @param artistId Artist ID
     * @return true if successful
     */
    public boolean removeArtist(String artistId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_artist");
        try {
            if (artistId == null || artistId.trim().isEmpty()) {
                logger.warn("Invalid artist ID for removal");
                return false;
            }

            logger.info("Removing artist with ID: {}", artistId);

            // Validate artist
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return false;
            }

            // Delete in transaction
            boolean success = artistDAO.delete(artistId);

            if (success) {
                // If successful, also remove from collection
                artistCollection.remove(artistId);
                logger.info("Successfully removed artist: {}", artist.getName());
            }

            return success;
        } catch (Exception e) {
            logger.error("Error removing artist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    // === ALBUM OPERATIONS ===

    /**
     * Adds an album
     * @param name Album name
     * @param artistId Artist ID
     * @param releaseYear Release year
     * @param genre Genre
     * @return true if successful
     */
    public boolean addAlbum(String name, String artistId, int releaseYear, String genre) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_album");
        try {
            if (name == null || name.trim().isEmpty() || releaseYear <= 0) {
                logger.warn("Invalid album data: name={}, releaseYear={}", name, releaseYear);
                return false;
            }

            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found for album creation", artistId);
                return false;
            }

            logger.info("Adding album: {}, Artist ID: {}, Release Year: {}, Genre: {}",
                    name, artist.getId(), releaseYear, genre);

            Album album = musicFactory.createAlbum(name, artist, releaseYear, genre);
            albumCollection.add(album);

            // Increment metric
            MetricsCollector.getInstance().incrementAlbumAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Gets all albums
     * @return List of albums
     */
    public List<Album> getAllAlbums() {
        try {
            logger.debug("Getting all albums");
            return albumCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets album by ID
     * @param id Album ID
     * @return Album or null
     */
    public Album getAlbumById(String id) {
        try {
            logger.debug("Getting album by ID: {}", id);
            return albumCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting album by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Removes an album
     * @param id Album ID
     * @return true if successful
     */
    public boolean removeAlbum(String id) {
        try {
            logger.info("Removing album with ID: {}", id);
            return albumCollection.remove(id);
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Removes an album and optionally its songs
     * @param albumId Album ID
     * @param deleteSongs true to delete songs, false to keep them
     * @return true if successful
     */
    public boolean removeAlbum(String albumId, boolean deleteSongs) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_album");
        try {
            if (albumId == null || albumId.trim().isEmpty()) {
                logger.warn("Invalid album ID for removal");
                return false;
            }

            logger.info("Removing album with ID: {}, deleteSongs={}", albumId, deleteSongs);

            // Validate album
            Album album = albumCollection.getById(albumId);
            if (album == null) {
                logger.warn("Album with ID {} not found", albumId);
                return false;
            }

            boolean success;
            if (deleteSongs) {
                // Delete album and its songs
                success = albumDAO.delete(albumId);
            } else {
                // Delete album but keep songs
                success = albumDAO.deleteWithoutSongs(albumId);
            }

            if (success) {
                // Also remove from collection
                albumCollection.remove(albumId);
                logger.info("Successfully removed album: {}", album.getName());
            }

            return success;
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Searches albums by name
     * @param name Album name
     * @return List of matching albums
     */
    public List<Album> searchAlbumsByName(String name) {
        try {
            logger.debug("Searching albums by name: {}", name);
            return albumCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching albums by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets albums by artist
     * @param artistId Artist ID
     * @return List of albums
     */
    public List<Album> getAlbumsByArtist(String artistId) {
        try {
            logger.debug("Getting albums by artist ID: {}", artistId);
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return Collections.emptyList();
            }
            return albumCollection.getByArtist(artist);
        } catch (Exception e) {
            logger.error("Error getting albums by artist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets albums by genre
     * @param genre Genre
     * @return List of albums
     */
    public List<Album> getAlbumsByGenre(String genre) {
        try {
            logger.debug("Getting albums by genre: {}", genre);
            return albumCollection.getByGenre(genre);
        } catch (Exception e) {
            logger.error("Error getting albums by genre: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // === SONG OPERATIONS ===

    /**
     * Adds a song
     * @param name Song name
     * @param artistId Artist ID
     * @param duration Duration in seconds
     * @param genre Genre
     * @return true if successful
     */
    public boolean addSong(String name, String artistId, int duration, String genre) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song");
        try {
            if (name == null || name.trim().isEmpty() || duration <= 0) {
                logger.warn("Invalid song data: name={}, duration={}", name, duration);
                return false;
            }

            // Get artist from collection
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return false;
            }

            logger.info("Creating song with name: {}, artist: {}, duration: {}, genre: {}",
                    name, artist.getName(), duration, genre);

            Song song = musicFactory.createSong(name, artist, duration, genre);
            songCollection.add(song);

            // Increment metric
            MetricsCollector.getInstance().incrementSongAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding song: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Adds a song to an album
     * @param songId Song ID
     * @param albumId Album ID
     * @return true if successful
     */
    public boolean addSongToAlbum(String songId, String albumId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song_to_album");
        try {
            Song song = songCollection.getById(songId);
            Album album = albumCollection.getById(albumId);

            if (song == null || album == null) {
                logger.warn("Cannot add song to album: song={}, album={}",
                        (song == null ? "null" : song.getId()),
                        (album == null ? "null" : album.getId()));
                return false;
            }

            // Set album reference
            song.setAlbum(album);

            // Update in database
            boolean updated = songDAO.update(song);
            if (updated) {
                logger.info("Added song {} to album {}", song.getName(), album.getName());
            }

            return updated;
        } catch (Exception e) {
            logger.error("Error adding song to album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Gets all songs
     * @return List of songs
     */
    public List<Song> getAllSongs() {
        try {
            logger.debug("Getting all songs");
            return songCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all songs: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets song by ID
     * @param id Song ID
     * @return Song or null
     */
    public Song getSongById(String id) {
        try {
            logger.debug("Getting song by ID: {}", id);
            return songCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting song by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Removes a song
     * @param songId Song ID
     * @return true if successful
     */
    public boolean removeSong(String songId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_song");
        try {
            if (songId == null || songId.trim().isEmpty()) {
                logger.warn("Invalid song ID for removal");
                return false;
            }

            logger.info("Removing song with ID: {}", songId);

            // Validate song
            Song song = songCollection.getById(songId);
            if (song == null) {
                logger.warn("Song with ID {} not found", songId);
                return false;
            }

            // Delete from database
            boolean success = songDAO.delete(songId);

            if (success) {
                // Also remove from collection
                songCollection.remove(songId);
                logger.info("Successfully removed song: {}", song.getName());
            }

            return success;
        } catch (Exception e) {
            logger.error("Error removing song: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Searches songs by name
     * @param name Song name
     * @return List of matching songs
     */
    public List<Song> searchSongsByName(String name) {
        try {
            logger.debug("Searching songs by name: {}", name);
            return songCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching songs by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets songs by artist
     * @param artistId Artist ID
     * @return List of songs
     */
    public List<Song> getSongsByArtist(String artistId) {
        try {
            logger.debug("Getting songs by artist ID: {}", artistId);
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return Collections.emptyList();
            }
            return songCollection.getByArtist(artist);
        } catch (Exception e) {
            logger.error("Error getting songs by artist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets songs by album
     * @param albumId Album ID
     * @return List of songs
     */
    public List<Song> getSongsByAlbum(String albumId) {
        try {
            logger.debug("Getting songs by album ID: {}", albumId);
            Album album = albumCollection.getById(albumId);
            if (album == null) {
                logger.warn("Album with ID {} not found", albumId);
                return Collections.emptyList();
            }
            return songCollection.getByAlbum(album);
        } catch (Exception e) {
            logger.error("Error getting songs by album: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets songs by genre
     * @param genre Genre
     * @return List of songs
     */
    public List<Song> getSongsByGenre(String genre) {
        try {
            logger.debug("Getting songs by genre: {}", genre);
            return songCollection.getByGenre(genre);
        } catch (Exception e) {
            logger.error("Error getting songs by genre: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // === PLAYLIST OPERATIONS ===

    /**
     * Creates a playlist
     * @param name Playlist name
     * @param description Playlist description
     * @return true if successful
     */
    public boolean createPlaylist(String name, String description) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("create_playlist");
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Invalid playlist data: name is empty or null");
                return false;
            }

            Playlist playlist = new Playlist(name, description);
            playlistCollection.add(playlist);

            logger.info("Created new playlist: {} with ID: {}", name, playlist.getId());

            // Increment metric
            MetricsCollector.getInstance().incrementPlaylistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error creating playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Gets all playlists
     * @return List of playlists
     */
    public List<Playlist> getAllPlaylists() {
        try {
            logger.debug("Getting all playlists");
            return playlistCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all playlists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets playlist by ID
     * @param id Playlist ID
     * @return Playlist or null
     */
    public Playlist getPlaylistById(String id) {
        try {
            logger.debug("Getting playlist by ID: {}", id);
            return playlistCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting playlist by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Removes a playlist
     * @param id Playlist ID
     * @return true if successful
     */
    public boolean removePlaylist(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                logger.warn("Invalid playlist ID for removal");
                return false;
            }

            logger.info("Removing playlist with ID: {}", id);

            // Validate playlist
            Playlist playlist = playlistCollection.getById(id);
            if (playlist == null) {
                logger.warn("Playlist with ID {} not found", id);
                return false;
            }

            // Delete playlist
            return playlistCollection.remove(id);
        } catch (Exception e) {
            logger.error("Error removing playlist: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Searches playlists by name
     * @param name Playlist name
     * @return List of matching playlists
     */
    public List<Playlist> searchPlaylistsByName(String name) {
        try {
            logger.debug("Searching playlists by name: {}", name);
            return playlistCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching playlists by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets playlists containing a song
     * @param songId Song ID
     * @return List of playlists
     */
    public List<Playlist> getPlaylistsContainingSong(String songId) {
        try {
            if (songId == null || songId.trim().isEmpty()) {
                logger.warn("Invalid song ID for getPlaylistsContainingSong");
                return Collections.emptyList();
            }

            logger.debug("Getting playlists containing song: {}", songId);
            Song song = songCollection.getById(songId);
            if (song == null) {
                logger.warn("Song with ID {} not found", songId);
                return Collections.emptyList();
            }

            return playlistCollection.getPlaylistsContainingSong(song);
        } catch (Exception e) {
            logger.error("Error getting playlists containing song: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Adds a song to a playlist
     * @param songId Song ID
     * @param playlistId Playlist ID
     * @return true if successful
     */
    public boolean addSongToPlaylist(String songId, String playlistId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song_to_playlist");
        try {
            Song song = songCollection.getById(songId);
            Playlist playlist = playlistCollection.getById(playlistId);

            if (song == null || playlist == null) {
                logger.warn("Failed to add song to playlist: song={}, playlist={}",
                        (song == null ? "null" : song.getId()),
                        (playlist == null ? "null" : playlist.getId()));
                return false;
            }

            playlist.addSong(song);
            playlistCollection.addSongToPlaylist(playlistId, songId);

            logger.info("Added song {} to playlist {}", song.getName(), playlist.getName());
            return true;
        } catch (Exception e) {
            logger.error("Error adding song to playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Removes a song from a playlist
     * @param songId Song ID
     * @param playlistId Playlist ID
     * @return true if successful
     */
    public boolean removeSongFromPlaylist(String songId, String playlistId) {
        try {
            logger.info("Removing song {} from playlist {}", songId, playlistId);
            Song song = songCollection.getById(songId);
            Playlist playlist = playlistCollection.getById(playlistId);

            if (song == null || playlist == null) {
                logger.warn("Failed to remove song from playlist: song={}, playlist={}",
                        (song == null ? "null" : song.getId()),
                        (playlist == null ? "null" : playlist.getId()));
                return false;
            }

            playlist.removeSong(song);
            playlistCollection.removeSongFromPlaylist(playlistId, songId);

            logger.info("Removed song {} from playlist {}", song.getName(), playlist.getName());
            return true;
        } catch (Exception e) {
            logger.error("Error removing song from playlist: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets songs in a playlist
     * @param playlistId Playlist ID
     * @return List of songs
     */
    public List<Song> getSongsInPlaylist(String playlistId) {
        try {
            if (playlistId == null || playlistId.trim().isEmpty()) {
                logger.warn("Invalid playlist ID for getSongsInPlaylist");
                return Collections.emptyList();
            }

            logger.debug("Getting songs for playlist {}", playlistId);

            Playlist playlist = playlistCollection.getById(playlistId);
            if (playlist == null) {
                logger.warn("Playlist with ID {} not found", playlistId);
                return Collections.emptyList();
            }

            return playlist.getSongs();
        } catch (Exception e) {
            logger.error("Error getting songs in playlist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Saves data to files
     * @param artistFile Artist file path
     * @param albumFile Album file path
     * @param songFile Song file path
     * @param playlistFile Playlist file path
     * @return true if successful
     */
    public boolean saveData(String artistFile, String albumFile, String songFile, String playlistFile) {
        try {
            logger.info("Saving data to files: artist={}, album={}, song={}, playlist={}",
                    artistFile, albumFile, songFile, playlistFile);

            boolean artistSaved = artistCollection.saveToFile(artistFile);
            boolean albumSaved = albumCollection.saveToFile(albumFile);
            boolean songSaved = songCollection.saveToFile(songFile);
            boolean playlistSaved = playlistCollection.saveToFile(playlistFile);

            return artistSaved && albumSaved && songSaved && playlistSaved;
        } catch (Exception e) {
            logger.error("Error saving data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Loads data from files
     * @param artistFile Artist file path
     * @param albumFile Album file path
     * @param songFile Song file path
     * @param playlistFile Playlist file path
     * @return true if successful
     */
    public boolean loadData(String artistFile, String albumFile, String songFile, String playlistFile) {
        try {
            logger.info("Loading data from files: artist={}, album={}, song={}, playlist={}",
                    artistFile, albumFile, songFile, playlistFile);

            // Check database first
            boolean hasData = false;

            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM artists");

                if (rs.next() && rs.getInt(1) > 0) {
                    hasData = true;
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error checking database: {}", e.getMessage(), e);
            }

            // If no data in database, load from files
            if (!hasData) {
                boolean artistLoaded = artistCollection.loadFromFile(artistFile);
                boolean albumLoaded = albumCollection.loadFromFile(albumFile);
                boolean songLoaded = songCollection.loadFromFile(songFile);
                boolean playlistLoaded = playlistCollection.loadFromFile(playlistFile);

                return artistLoaded || albumLoaded || songLoaded || playlistLoaded;
            }

            logger.info("Data already exists in database, skipping file import");
            return true;
        } catch (Exception e) {
            logger.error("Error loading data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Reinitializes the database
     */
    public void reinitializeDatabase() {
        try {
            // Reset database
            DatabaseManager.getInstance().setShouldResetDatabase(true);
            DatabaseManager.getInstance().initializeDatabase();

            // Clear and reload collections
            artistCollection.clear();
            albumCollection.clear();
            songCollection.clear();
            playlistCollection.clear();

            logger.info("Database and collections reinitialized successfully");
        } catch (Exception e) {
            logger.error("Error reinitializing database: {}", e.getMessage(), e);
        }
    }

    public boolean updatePlaylist(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot update null playlist");
            return false;
        }

        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("update_playlist");
        try {
            logger.info("Updating playlist: {} (ID: {})", playlist.getName(), playlist.getId());

            // Verify playlist exists
            Playlist existingPlaylist = playlistCollection.getById(playlist.getId());
            if (existingPlaylist == null) {
                logger.warn("Playlist with ID {} not found", playlist.getId());
                return false;
            }

            // Update playlist in collection and database
            return playlistDAO.update(playlist);
        } catch (Exception e) {
            logger.error("Error updating playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Method to add to MusicCollectionService class
     * to support adding a pre-existing playlist
     */
    public boolean addPlaylist(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot add null playlist");
            return false;
        }

        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_playlist");
        try {
            logger.info("Adding playlist with ID: {}, name: {}", playlist.getId(), playlist.getName());
            playlistCollection.add(playlist);

            // Increment metric
            MetricsCollector.getInstance().incrementPlaylistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

}