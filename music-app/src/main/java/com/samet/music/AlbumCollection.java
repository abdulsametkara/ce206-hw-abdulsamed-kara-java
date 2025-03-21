package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection for managing albums
 * Implemented using Singleton pattern
 */
public class AlbumCollection extends MusicCollectionManager<Album> {
    private static AlbumCollection instance;

    private AlbumCollection() {
        // Private constructor for Singleton pattern
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

    public List<Album> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase();
        List<Album> results = new ArrayList<>();

        for (Album album : items.values()) {
            if (album.getName().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        return results;
    }

    public List<Album> getByArtist(Artist artist) {
        if (artist == null) {
            return new ArrayList<>();
        }

        List<Album> results = new ArrayList<>();

        for (Album album : items.values()) {
            if (artist.equals(album.getArtist())) {
                results.add(album);
            }
        }

        return results;
    }

    public List<Album> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = genre.toLowerCase();
        List<Album> results = new ArrayList<>();

        for (Album album : items.values()) {
            if (album.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        return results;
    }
}