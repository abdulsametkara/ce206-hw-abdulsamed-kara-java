package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection for managing songs
 * Implemented using Singleton pattern
 */
public class SongCollection extends MusicCollectionManager<Song> {
    private static SongCollection instance;

    private SongCollection() {
        // Private constructor for Singleton pattern
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

    public List<Song> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase();
        List<Song> results = new ArrayList<>();

        for (Song song : items.values()) {
            if (song.getName().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        return results;
    }

    public List<Song> getByArtist(Artist artist) {
        if (artist == null) {
            return new ArrayList<>();
        }

        List<Song> results = new ArrayList<>();

        for (Song song : items.values()) {
            if (artist.equals(song.getArtist())) {
                results.add(song);
            }
        }

        return results;
    }

    public List<Song> getByAlbum(Album album) {
        if (album == null) {
            return new ArrayList<>();
        }

        List<Song> results = new ArrayList<>();

        for (Song song : items.values()) {
            if (album.equals(song.getAlbum())) {
                results.add(song);
            }
        }

        return results;
    }

    public List<Song> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = genre.toLowerCase();
        List<Song> results = new ArrayList<>();

        for (Song song : items.values()) {
            if (song.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        return results;
    }
}