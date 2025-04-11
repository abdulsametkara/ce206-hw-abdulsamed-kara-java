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
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid artist data: name is empty or null");
            timer.observeDuration();
            return false;
        }

        logger.info("Adding artist: {}", name);
        Artist artist = musicFactory.createArtist(name, biography);
        artistCollection.add(artist);

        // Increment metric
        MetricsCollector.getInstance().incrementArtistAdded();
        timer.observeDuration();
        return true;
    }

    /**
     * Gets artist by ID
     * @param id Artist ID
     * @return Artist or null
     */
    public Artist getArtistById(String id) {
        logger.debug("Getting artist by ID: {}", id);
        return artistCollection.getById(id);
    }

    /**
     * Searches artists by name
     * @param name Artist name
     * @return List of matching artists
     */
    public List<Artist> searchArtistsByName(String name) {
        logger.debug("Searching artists by name: {}", name);
        return artistCollection.searchByName(name);
    }

    /**
     * Gets all artists
     * @return List of artists
     */
    public List<Artist> getAllArtists() {
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
    }

    /**
     * Removes an artist and all related albums and songs
     * @param artistId Artist ID
     * @return true if successful
     */
    public boolean removeArtist(String artistId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_artist");
        
        if (artistId == null || artistId.trim().isEmpty()) {
            logger.warn("Invalid artist ID for removal");
            timer.observeDuration();
            return false;
        }

        logger.info("Removing artist with ID: {}", artistId);

        // Validate artist
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            logger.warn("Artist with ID {} not found", artistId);
            timer.observeDuration();
            return false;
        }

        // Delete in transaction
        boolean success = artistDAO.delete(artistId);

        if (success) {
            // If successful, also remove from collection
            artistCollection.remove(artistId);
            logger.info("Successfully removed artist: {}", artist.getName());
        }

        timer.observeDuration();
        return success;
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
        
        if (name == null || name.trim().isEmpty() || releaseYear <= 0) {
            logger.warn("Invalid album data: name={}, releaseYear={}", name, releaseYear);
            timer.observeDuration();
            return false;
        }

        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            logger.warn("Artist with ID {} not found for album creation", artistId);
            timer.observeDuration();
            return false;
        }

        logger.info("Adding album: {}, Artist ID: {}, Release Year: {}, Genre: {}",
                name, artist.getId(), releaseYear, genre);

        Album album = musicFactory.createAlbum(name, artist, releaseYear, genre);
        albumCollection.add(album);

        // Increment metric
        MetricsCollector.getInstance().incrementAlbumAdded();
        timer.observeDuration();
        return true;
    }

    /**
     * Gets all albums
     * @return List of albums
     */
    public List<Album> getAllAlbums() {
        logger.debug("Getting all albums");
        return albumCollection.getAll();
    }

    /**
     * Gets album by ID
     * @param id Album ID
     * @return Album or null
     */
    public Album getAlbumById(String id) {
        logger.debug("Getting album by ID: {}", id);
        return albumCollection.getById(id);
    }

    /**
     * Removes an album
     * @param id Album ID
     * @return true if successful
     */
    public boolean removeAlbum(String id) {
        logger.info("Removing album with ID: {}", id);
        return albumCollection.remove(id);
    }

    /**
     * Removes an album and optionally its songs
     * @param albumId Album ID
     * @param deleteSongs true to delete songs, false to keep them
     * @return true if successful
     */
    public boolean removeAlbum(String albumId, boolean deleteSongs) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_album");
        
        if (albumId == null || albumId.trim().isEmpty()) {
            logger.warn("Invalid album ID for removal");
            timer.observeDuration();
            return false;
        }

        logger.info("Removing album with ID: {}, deleteSongs={}", albumId, deleteSongs);

        // Validate album
        Album album = albumCollection.getById(albumId);
        if (album == null) {
            logger.warn("Album with ID {} not found", albumId);
            timer.observeDuration();
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

        timer.observeDuration();
        return success;
    }

    /**
     * Searches albums by name
     * @param name Album name
     * @return List of matching albums
     */
    public List<Album> searchAlbumsByName(String name) {
        logger.debug("Searching albums by name: {}", name);
        return albumCollection.searchByName(name);
    }

    /**
     * Gets albums by artist
     * @param artistId Artist ID
     * @return List of albums
     */
    public List<Album> getAlbumsByArtist(String artistId) {
        logger.debug("Getting albums by artist ID: {}", artistId);
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            logger.warn("Artist with ID {} not found", artistId);
            return Collections.emptyList();
        }
        return albumCollection.getByArtist(artist);
    }

    /**
     * Gets albums by genre
     * @param genre Genre
     * @return List of albums
     */
    public List<Album> getAlbumsByGenre(String genre) {
        logger.debug("Getting albums by genre: {}", genre);
        return albumCollection.getByGenre(genre);
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
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid song data: name={}, duration={}", name, duration);
            timer.observeDuration();
            return false;
        }

        if (duration <= 0) {
            logger.warn("Invalid song data: name={}, duration={}", name, duration);
            timer.observeDuration();
            return false;
        }

        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            logger.warn("Artist with ID {} not found for song creation", artistId);
            timer.observeDuration();
            return false;
        }

        logger.info("Creating song with name: {}, artist: {}, duration: {}, genre: {}",
                name, artist.getName(), duration, genre);

        Song song = musicFactory.createSong(name, artist, duration, genre);
        songCollection.add(song);

        // Increment metric
        MetricsCollector.getInstance().incrementSongAdded();
        timer.observeDuration();
        return true;
    }

    /**
     * Adds a song to an album
     * @param songId Song ID
     * @param albumId Album ID
     * @return true if successful
     */
    public boolean addSongToAlbum(String songId, String albumId) {
        if (songId == null || albumId == null) {
            logger.warn("Cannot add song to album: song={}, album={}", songId, albumId);
            return false;
        }

        Song song = songCollection.getById(songId);
        Album album = albumCollection.getById(albumId);

        if (song == null || album == null) {
            logger.warn("Cannot add song to album: song={}, album={}",
                    (song == null ? "null" : song.getId()),
                    (album == null ? "null" : album.getId()));
            return false;
        }

        song.setAlbum(album);
        boolean success = songDAO.update(song);

        if (success) {
            logger.info("Added song {} to album {}", song.getName(), album.getName());
        }

        return success;
    }

    /**
     * Gets all songs
     * @return List of songs
     */
    public List<Song> getAllSongs() {
        logger.debug("Getting all songs");
        return songCollection.getAll();
    }

    /**
     * Gets song by ID
     * @param id Song ID
     * @return Song or null
     */
    public Song getSongById(String id) {
        logger.debug("Getting song by ID: {}", id);
        return songCollection.getById(id);
    }

    /**
     * Removes a song
     * @param songId Song ID
     * @return true if successful
     */
    public boolean removeSong(String songId) {
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

        // Remove from DAO
        boolean success = songDAO.delete(songId);

        if (success) {
            // If successful, also remove from collection
            songCollection.remove(songId);
            logger.info("Successfully removed song: {}", song.getName());
        }

        return success;
    }

    /**
     * Searches songs by name
     * @param name Song name
     * @return List of matching songs
     */
    public List<Song> searchSongsByName(String name) {
        logger.debug("Searching songs by name: {}", name);
        return songCollection.searchByName(name);
    }

    /**
     * Gets songs by artist
     * @param artistId Artist ID
     * @return List of songs
     */
    public List<Song> getSongsByArtist(String artistId) {
        logger.debug("Getting songs by artist ID: {}", artistId);
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            logger.warn("Artist with ID {} not found", artistId);
            return Collections.emptyList();
        }
        return songCollection.getByArtist(artist);
    }

    /**
     * Gets songs by album
     * @param albumId Album ID
     * @return List of songs
     */
    public List<Song> getSongsByAlbum(String albumId) {
        logger.debug("Getting songs by album ID: {}", albumId);
        Album album = albumCollection.getById(albumId);
        if (album == null) {
            logger.warn("Album with ID {} not found", albumId);
            return Collections.emptyList();
        }
        return songCollection.getByAlbum(album);
    }

    /**
     * Gets songs by genre
     * @param genre Genre
     * @return List of songs
     */
    public List<Song> getSongsByGenre(String genre) {
        logger.debug("Getting songs by genre: {}", genre);
        return songCollection.getByGenre(genre);
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
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid playlist data: name is empty or null");
            timer.observeDuration();
            return false;
        }

        // Create a new playlist
        Playlist playlist = new Playlist(name, description);
        playlistCollection.add(playlist);

        logger.info("Created new playlist: {} with ID: {}", name, playlist.getId());

        // Increment metric
        MetricsCollector.getInstance().incrementPlaylistAdded();
        timer.observeDuration();
        return true;
    }

    /**
     * Gets all playlists
     * @return List of playlists
     */
    public List<Playlist> getAllPlaylists() {
        logger.debug("Getting all playlists");
        return playlistCollection.getAll();
    }

    /**
     * Gets playlist by ID
     * @param id Playlist ID
     * @return Playlist or null
     */
    public Playlist getPlaylistById(String id) {
        logger.debug("Getting playlist by ID: {}", id);
        return playlistCollection.getById(id);
    }

    /**
     * Removes a playlist
     * @param id Playlist ID
     * @return true if successful
     */
    public boolean removePlaylist(String id) {
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

        // Remove from DAO
        boolean success = playlistDAO.delete(id);

        if (success) {
            // If successful, also remove from collection
            playlistCollection.remove(id);
            logger.info("Successfully removed playlist: {}", playlist.getName());
        }

        return success;
    }

    /**
     * Searches playlists by name
     * @param name Playlist name
     * @return List of matching playlists
     */
    public List<Playlist> searchPlaylistsByName(String name) {
        logger.debug("Searching playlists by name: {}", name);
        return playlistCollection.searchByName(name);
    }

    /**
     * Gets playlists containing a song
     * @param songId Song ID
     * @return List of playlists
     */
    public List<Playlist> getPlaylistsContainingSong(String songId) {
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
    }

    /**
     * Adds a song to a playlist
     * @param songId Song ID
     * @param playlistId Playlist ID
     * @return true if successful
     */
    public boolean addSongToPlaylist(String songId, String playlistId) {
        if (songId == null || playlistId == null) {
            logger.warn("Failed to add song to playlist: song={}, playlist={}",
                    songId, playlistId);
            return false;
        }

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
    }

    /**
     * Removes a song from a playlist
     * @param songId Song ID
     * @param playlistId Playlist ID
     * @return true if successful
     */
    public boolean removeSongFromPlaylist(String songId, String playlistId) {
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
    }

    /**
     * Gets songs in a playlist
     * @param playlistId Playlist ID
     * @return List of songs
     */
    public List<Song> getSongsInPlaylist(String playlistId) {
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
        logger.info("Saving data to files: artist={}, album={}, song={}, playlist={}",
                artistFile, albumFile, songFile, playlistFile);

        boolean artistSaved = artistCollection.saveToFile(artistFile);
        boolean albumSaved = albumCollection.saveToFile(albumFile);
        boolean songSaved = songCollection.saveToFile(songFile);
        boolean playlistSaved = playlistCollection.saveToFile(playlistFile);

        return artistSaved && albumSaved && songSaved && playlistSaved;
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
        logger.info("Loading data from files: artist={}, album={}, song={}, playlist={}",
                artistFile, albumFile, songFile, playlistFile);

        // Load in the right order to maintain references
        boolean artistLoaded = artistCollection.loadFromFile(artistFile);
        
        // We need artists to be loaded to maintain album references
        boolean albumLoaded = false;
        if (artistLoaded) {
            albumLoaded = albumCollection.loadFromFile(albumFile);
        }
        
        // We need both artists and albums to maintain song references
        boolean songLoaded = false;
        if (artistLoaded && albumLoaded) {
            songLoaded = songCollection.loadFromFile(songFile);
        }
        
        // We need songs to be loaded to maintain playlist references
        boolean playlistLoaded = false;
        if (songLoaded) {
            playlistLoaded = playlistCollection.loadFromFile(playlistFile);
        }
        
        return artistLoaded && albumLoaded && songLoaded && playlistLoaded;
    }

    /**
     * Reinitializes the database
     */
    public void reinitializeDatabase() {
        logger.info("Reinitializing database...");
        
        try {
            // DatabaseManager sınıfının initializeDatabase metodunu kullanarak 
            // veritabanını sıfırlayalım
            DatabaseManager.getInstance().setShouldResetDatabase(true);
            DatabaseManager.getInstance().initializeDatabase();
            
            // Koleksiyonları da temizleyelim
            artistCollection.clear();
            albumCollection.clear();
            songCollection.clear();
            playlistCollection.clear();
            
            logger.info("Database successfully reinitialized");
        } catch (SQLException e) {
            logger.error("Error reinitializing database: {}", e.getMessage(), e);
        }
    }

    /**
     * Updates a playlist
     * @param playlist Playlist to update
     * @return true if successful
     */
    public boolean updatePlaylist(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot update null playlist");
            return false;
        }

        logger.info("Updating playlist: {} (ID: {})", playlist.getName(), playlist.getId());

        // Verify playlist exists
        Playlist existingPlaylist = playlistCollection.getById(playlist.getId());
        if (existingPlaylist == null) {
            logger.warn("Playlist with ID {} not found", playlist.getId());
            return false;
        }

        // Update in database
        boolean success = playlistDAO.update(playlist);
        
        if (success) {
            // If successful in database, update in memory collection
            // 1. Remove the old playlist
            playlistCollection.remove(playlist.getId());
            // 2. Add the updated playlist
            playlistCollection.add(playlist);
        }

        return success;
    }

    /**
     * Adds a playlist
     * @param playlist Playlist to add
     * @return true if successful
     */
    public boolean addPlaylist(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot add null playlist");
            return false;
        }

        logger.info("Adding playlist with ID: {}, name: {}", playlist.getId(), playlist.getName());
        playlistCollection.add(playlist);
        return true;
    }
}