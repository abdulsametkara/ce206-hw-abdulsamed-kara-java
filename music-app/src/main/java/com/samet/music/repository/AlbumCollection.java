package com.samet.music.repository;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AlbumCollection extends MusicCollectionBase<Album> {
    private static final Logger logger = LoggerFactory.getLogger(AlbumCollection.class);
    private static AlbumCollection instance;
    private AlbumDAO albumDAO;
    private ArtistDAO artistDAO;

    private AlbumCollection() {
        this(DAOFactory.getInstance());
    }

    // Test için constructor
    protected AlbumCollection(DAOFactory daoFactory) {
        this.albumDAO = daoFactory.getAlbumDAO();
        this.artistDAO = daoFactory.getArtistDAO();
        logger.info("AlbumCollection initialized");
    }

    public static synchronized AlbumCollection getInstance() {
        if (instance == null) {
            instance = new AlbumCollection();
        }
        return instance;
    }

    // Test için instance'ı sıfırlama metodu
    protected static void resetInstance() {
        instance = null;
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

        try {
            if (albumDAO.insert(album)) {
                super.add(album);
                logger.info("Album successfully added: {}", album.getName());
            } else {
                logger.error("Failed to add album to database");
            }
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
        loadFromDatabase();
        return super.getAll();
    }

    @Override
    protected void loadFromDatabase() {
        logger.info("Loading albums from database...");
        clear();

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

        try {
            if (albumDAO.delete(id)) {
                super.remove(id);
                logger.info("Album removed from database");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error removing album from database: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteWithoutSongs(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid album ID");
            return false;
        }

        logger.info("Removing album while keeping songs. ID: {}", id);

        try {
            if (albumDAO.deleteWithoutSongs(id)) {
                super.remove(id);
                logger.info("Album removed from database, songs preserved");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        }
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

        List<Album> results = new ArrayList<>();
        List<Album> allAlbums = getAll();

        String artistId = artist.getId();
        for (Album album : allAlbums) {
            if (album.getArtist() != null && album.getArtist().getId().equals(artistId)) {
                results.add(album);
            }
        }

        logger.debug("Found {} albums for artist", results.size());
        return results;
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
            if (album.getGenre() != null && album.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        logger.debug("Found {} albums for genre", results.size());
        return results;
    }

    @Override
    public boolean loadFromFile(String filePath) {
        logger.info("Loading all albums from database");
        loadFromDatabase();
        isLoaded = true;
        return true;
    }
}