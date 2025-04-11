package com.samet.music.repository;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
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

public class AlbumCollection extends MusicCollectionBase<Album> {
    private static final Logger logger = LoggerFactory.getLogger(AlbumCollection.class);
    private static AlbumCollection instance;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;

    private AlbumCollection() {
        DAOFactory daoFactory = DAOFactory.getInstance();
        albumDAO = daoFactory.getAlbumDAO();
        artistDAO = daoFactory.getArtistDAO();
        logger.info("AlbumCollection initialized");
    }

    public static synchronized AlbumCollection getInstance() {
        if (instance == null) {
            instance = new AlbumCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Album item) {
        return item.getId();
    }

    @Override
    public void add(Album album) {
        if (album == null) {
            logger.warn("Cannot add null album");
            return;
        }

        logger.debug("Adding album: {}", album.getName());

        super.add(album);

        try {
            albumDAO.insert(album);
            logger.info("Album successfully added: {}", album.getName());
        } catch (Exception e) {
            logger.error("Error adding album to database: {}", e.getMessage(), e);
        }
    }

    @Override
    public Album getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return null;
        }

        logger.debug("Getting album by ID: {}", id);

        Album album = super.getById(id);

        if (album == null) {
            logger.debug("Album not found in cache, getting from database");
            album = albumDAO.getById(id);
            if (album != null) {
                super.add(album);
                logger.debug("Album found in database and added to cache: {}", album.getName());
            } else {
                logger.debug("Album not found in database either");
            }
        } else {
            logger.debug("Album found in cache: {}", album.getName());
        }

        return album;
    }

    @Override
    public List<Album> getAll() {
        logger.debug("Getting all albums");

        // Load from database if needed
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        return super.getAll();
    }

    @Override
    protected void loadFromDatabase() {
        logger.info("Loading albums from database...");
        clear(); // First clear existing items

        try {
            List<Album> albums = albumDAO.getAll();

            for (Album album : albums) {
                super.add(album);
            }

            logger.info("Loaded {} albums", albums.size());

        } catch (Exception e) {
            logger.error("Error loading albums from database: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean remove(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return false;
        }

        logger.info("Removing album. ID: {}", id);

        boolean removed = super.remove(id);

        try {
            albumDAO.delete(id);
            logger.info("Album removed from database");
        } catch (Exception e) {
            logger.error("Error removing album from database: {}", e.getMessage(), e);
        }

        return removed;
    }

    public boolean deleteWithoutSongs(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return false;
        }

        logger.info("Removing album while keeping songs. ID: {}", id);

        // Remove from cache
        boolean removed = super.remove(id);

        try {
            // Use special method in AlbumDAO
            albumDAO.deleteWithoutSongs(id);
            logger.info("Album removed from database, songs preserved");
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        }

        return removed;
    }

    public List<Album> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid search term");
            return new ArrayList<>();
        }

        logger.debug("Searching album: {}", name);

        List<Album> results = new ArrayList<>();
        List<Album> allAlbums = getAll();

        String searchTerm = name.toLowerCase();
        for (Album album : allAlbums) {
            if (album.getName().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        logger.debug("Search result: {} albums found", results.size());
        return results;
    }

    public List<Album> getByArtist(Artist artist) {
        if (artist == null) {
            logger.warn("Invalid artist");
            return new ArrayList<>();
        }

        logger.debug("Getting albums by artist: {}", artist.getName());

        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        List<Album> result = new ArrayList<>();
        String artistId = artist.getId();

        for (Album album : items.values()) {
            if (album.getArtist() != null && album.getArtist().getId().equals(artistId)) {
                result.add(album);
            }
        }

        logger.debug("Found {} albums for artist", result.size());
        return result;
    }

    public List<Album> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            logger.warn("Invalid genre");
            return new ArrayList<>();
        }

        logger.debug("Getting albums by genre: {}", genre);

        List<Album> results = new ArrayList<>();
        List<Album> allAlbums = getAll();

        String searchTerm = genre.toLowerCase();
        for (Album album : allAlbums) {
            if (album.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        logger.debug("Found {} albums for genre", results.size());
        return results;
    }

    @Override
    public boolean loadFromFile(String filePath) {
        logger.info("Loading all albums from database");

        // Load directly from database
        loadFromDatabase();
        isLoaded = true;
        return true;
    }
}