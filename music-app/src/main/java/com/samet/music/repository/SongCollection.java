package com.samet.music.repository;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.SongDAO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongCollection extends MusicCollectionBase<Song> {
    private static final Logger logger = LoggerFactory.getLogger(SongCollection.class);
    private static SongCollection instance;
    private final SongDAO songDAO;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;

    private SongCollection() {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.songDAO = daoFactory.getSongDAO();
        this.artistDAO = daoFactory.getArtistDAO();
        this.albumDAO = daoFactory.getAlbumDAO();
        logger.info("SongCollection initialized");
    }

    public static synchronized SongCollection getInstance() {
        if (instance == null) {
            instance = new SongCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Song item) {
        return item.getId();
    }

    @Override
    public void add(Song song) {
        if (song == null) {
            logger.warn("Attempted to add null song");
            return;
        }

        logger.debug("Adding song: {}", song.getName());
        super.add(song);

        try {
            // Get DAO instance using the correct way
            SongDAO songDAO = DAOFactory.getInstance().getSongDAO();

            // Insert or update the song in the database
            boolean success = songDAO.insert(song);

            if (success) {
                logger.info("Successfully added song: {}", song.getName());
                // Set the loaded flag to true since we just modified the collection
                isLoaded = true;
            } else {
                logger.warn("Failed to add song to database: {}", song.getName());
                // Remove from memory if database insert failed
                super.remove(song.getId());
            }
        } catch (Exception e) {
            logger.error("Error adding song to database: {}", e.getMessage(), e);
            // Remove from memory if an exception occurred
            super.remove(song.getId());
        }
    }

    @Override
    public Song getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Attempted to get song with null/empty ID");
            return null;
        }

        logger.debug("Getting song by ID: {}", id);

        // First check memory cache
        Song song = super.getById(id);

        if (song == null) {
            logger.debug("Song not found in memory cache, checking database");
            try {
                // Get from database
                song = songDAO.getById(id);

                if (song != null) {
                    // Add to memory cache
                    super.add(song);
                    logger.debug("Song found in database and added to cache: {}", song.getName());
                } else {
                    logger.debug("Song not found in database either");
                }
            } catch (Exception e) {
                logger.error("Error fetching song from database: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Song found in memory cache: {}", song.getName());
        }

        return song;
    }

    @Override
    public List<Song> getAll() {
        logger.debug("Getting all songs");

        // Always reload from database to get the latest data
        loadFromDatabase();
        isLoaded = true;

        return super.getAll();
    }

    @Override
    protected void loadFromDatabase() {
        logger.info("Loading songs from database...");
        clear(); // Clear existing items

        try {
            List<Song> songs = songDAO.getAll();

            for (Song song : songs) {
                super.add(song);
            }

            logger.info("Loaded {} songs from database", songs.size());

        } catch (Exception e) {
            logger.error("Failed to load songs from database: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean remove(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Attempted to remove song with null/empty ID");
            return false;
        }

        logger.info("Removing song with ID: {}", id);

        // First remove from memory
        boolean removed = super.remove(id);

        // Then remove from database
        try {
            songDAO.delete(id);
            logger.info("Song deleted from database");
        } catch (Exception e) {
            logger.error("Error deleting song from database: {}", e.getMessage(), e);
        }

        return removed;
    }

    /**
     * Search songs by name
     *
     * @param name Name to search for
     * @return List of matching songs
     */
    public List<Song> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempted to search songs with null/empty name");
            return new ArrayList<>();
        }

        logger.debug("Searching songs by name: {}", name);

        List<Song> results = new ArrayList<>();
        List<Song> allSongs = getAll();
        String searchTerm = name.toLowerCase();

        for (Song song : allSongs) {
            if (song.getName().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        logger.debug("Found {} songs matching '{}'", results.size(), name);
        return results;
    }

    /**
     * Get songs by artist
     *
     * @param artist Artist to filter by
     * @return List of songs by the artist
     */
    public List<Song> getByArtist(Artist artist) {
        if (artist == null) {
            logger.warn("Attempted to get songs with null artist");
            return new ArrayList<>();
        }

        logger.debug("Getting songs by artist: {}", artist.getName());

        // Ensure data is loaded
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        List<Song> result = new ArrayList<>();
        String artistId = artist.getId();

        for (Song song : items.values()) {
            if (song.getArtist() != null && song.getArtist().getId().equals(artistId)) {
                result.add(song);
            }
        }

        logger.debug("Found {} songs by artist {}", result.size(), artist.getName());
        return result;
    }

    /**
     * Get songs by album
     *
     * @param album Album to filter by
     * @return List of songs in the album
     */
    public List<Song> getByAlbum(Album album) {
        if (album == null) {
            logger.warn("Attempted to get songs with null album");
            return new ArrayList<>();
        }

        logger.debug("Getting songs by album: {}", album.getName());

        // Ensure data is loaded
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        List<Song> result = new ArrayList<>();
        String albumId = album.getId();

        for (Song song : items.values()) {
            if (song.getAlbum() != null && song.getAlbum().getId().equals(albumId)) {
                result.add(song);
            }
        }

        logger.debug("Found {} songs in album {}", result.size(), album.getName());
        return result;
    }

    /**
     * Get songs by genre
     *
     * @param genre Genre to filter by
     * @return List of songs with the genre
     */
    public List<Song> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            logger.warn("Attempted to get songs with null/empty genre");
            return new ArrayList<>();
        }

        logger.debug("Getting songs by genre: {}", genre);

        List<Song> results = new ArrayList<>();
        List<Song> allSongs = getAll();
        String searchTerm = genre.toLowerCase();

        for (Song song : allSongs) {
            if (song.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        logger.debug("Found {} songs with genre '{}'", results.size(), genre);
        return results;
    }

    @Override
    public boolean saveToFile(String filePath) {
        logger.info("Saving songs to file: {}", filePath);
        // No need to implement as we're using SQLite
        return true;
    }

    @Override
    public boolean loadFromFile(String filePath) {
        logger.info("Loading songs from file: {}", filePath);
        // Load from database instead
        loadFromDatabase();
        isLoaded = true;
        return true;
    }
}