package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection for managing artists
 * Implemented using Singleton pattern
 */
public class ArtistCollection extends MusicCollectionManager<Artist> {
    private static ArtistCollection instance;

    private ArtistCollection() {
        // Private constructor for Singleton pattern
    }

    public static synchronized ArtistCollection getInstance() {
        if (instance == null) {
            instance = new ArtistCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Artist item) {
        return item.getId();
    }

    public List<Artist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase();
        List<Artist> results = new ArrayList<>();

        for (Artist artist : items.values()) {
            if (artist.getName().toLowerCase().contains(searchTerm)) {
                results.add(artist);
            }
        }

        return results;
    }
}