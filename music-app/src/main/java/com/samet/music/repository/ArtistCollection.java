package com.samet.music.repository;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtistCollection extends MusicCollectionBase<Artist> {
    private static final Logger logger = LoggerFactory.getLogger(ArtistCollection.class);
    private static ArtistCollection instance;
    private ArtistDAO artistDAO;

    private ArtistCollection() {
        artistDAO = DAOFactory.getInstance().getArtistDAO();
        logger.info("ArtistCollection initialized");
    }

    public static synchronized ArtistCollection getInstance() {
        if (instance == null) {
            instance = new ArtistCollection();
        }
        return instance;
    }
    
    // Test için instance'ı sıfırlama metodu
    protected static void resetInstance() {
        instance = null;
    }

    @Override
    protected String getItemId(Artist item) {
        return item.getId();
    }

    @Override
    public void add(Artist artist) {
        if (artist == null) {
            logger.warn("Cannot add null artist");
            return;
        }

        logger.debug("Adding artist: {}", artist.getName());

        // Add to memory collection
        super.add(artist);

        // Save to database
        try {
            artistDAO.insert(artist);
            logger.info("Artist successfully added: {}", artist.getName());
        } catch (Exception e) {
            logger.error("Error adding artist to database: {}", e.getMessage(), e);
        }
    }

    @Override
    public Artist getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid artist ID");
            return null;
        }

        logger.debug("Getting artist by ID: {}", id);

        // Check memory cache first
        Artist artist = super.getById(id);

        // If not in memory, get from database
        if (artist == null) {
            logger.debug("Artist not found in cache, getting from database");
            artist = artistDAO.getById(id);
            if (artist != null) {
                // Add to memory
                super.add(artist);
                logger.debug("Artist found in database and added to cache: {}", artist.getName());
            } else {
                logger.debug("Artist not found in database either");
            }
        } else {
            logger.debug("Artist found in cache: {}", artist.getName());
        }

        return artist;
    }

    @Override
    public List<Artist> getAll() {
        logger.debug("Getting all artists");

        // Load from database
        List<Artist> allArtists = artistDAO.getAll();
        logger.debug("Loaded {} artists from database", allArtists.size());

        // Create map of unique artists by ID
        Map<String, Artist> uniqueArtistsById = new HashMap<>();
        for (Artist artist : allArtists) {
            uniqueArtistsById.put(artist.getId(), artist);
        }

        // Check for name collisions
        Map<String, List<Artist>> artistsByName = new HashMap<>();
        for (Artist artist : uniqueArtistsById.values()) {
            String name = artist.getName().toLowerCase();
            if (!artistsByName.containsKey(name)) {
                artistsByName.put(name, new ArrayList<>());
            }
            artistsByName.get(name).add(artist);
        }

        // Merge artists with the same name
        for (List<Artist> artistsWithSameName : artistsByName.values()) {
            if (artistsWithSameName.size() > 1) {
                // Use first artist as primary
                Artist primaryArtist = artistsWithSameName.get(0);
                logger.info("Found artists with same name: {}, primary ID: {}",
                        primaryArtist.getName(), primaryArtist.getId());

                // Merge other duplicates
                for (int i = 1; i < artistsWithSameName.size(); i++) {
                    Artist duplicateArtist = artistsWithSameName.get(i);
                    logger.info("Merging duplicate artist - ID: {}", duplicateArtist.getId());

                    // Merge in database
                    artistDAO.mergeArtists(primaryArtist.getId(), duplicateArtist.getId());

                    // Remove from unique artists map
                    uniqueArtistsById.remove(duplicateArtist.getId());
                }
            }
        }

        // Clear and update memory collection
        clear();
        for (Artist artist : uniqueArtistsById.values()) {
            super.add(artist);
        }

        logger.info("Returning {} unique artists", uniqueArtistsById.size());
        return new ArrayList<>(uniqueArtistsById.values());
    }

    @Override
    public boolean remove(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Invalid artist ID");
            return false;
        }

        logger.info("Removing artist. ID: {}", id);

        // Remove from memory
        boolean removed = super.remove(id);

        // Remove from database
        try {
            artistDAO.delete(id);
            logger.info("Artist removed from database");
        } catch (Exception e) {
            logger.error("Error removing artist from database: {}", e.getMessage(), e);
        }

        return removed;
    }

    @Override
    protected void loadFromDatabase() {
        logger.info("Loading artists from database...");
        clear(); // Clear existing items

        // Get all artists from database
        List<Artist> artists = artistDAO.getAll();

        // Add to collection
        for (Artist artist : artists) {
            super.add(artist);
        }

        logger.info("Loaded {} artists", artists.size());
    }

    public List<Artist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid search term");
            return new ArrayList<>();
        }

        logger.debug("Searching artist: {}", name);

        List<Artist> results = new ArrayList<>();
        List<Artist> allArtists = getAll();

        String searchTerm = name.toLowerCase();
        for (Artist artist : allArtists) {
            if (artist.getName().toLowerCase().contains(searchTerm)) {
                results.add(artist);
            }
        }

        logger.debug("Search result: {} artists found", results.size());
        return results;
    }

    @Override
    public boolean loadFromFile(String filePath) {
        logger.info("Loading all artists from database");

        // Load all artists from database
        List<Artist> artists = artistDAO.getAll();

        // Clear memory collection
        clear();

        // Add all artists to memory
        for (Artist artist : artists) {
            super.add(artist);
        }

        logger.info("Loaded {} artists", artists.size());
        return !artists.isEmpty();
    }
}