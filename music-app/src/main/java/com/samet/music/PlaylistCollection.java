package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection for managing playlists
 * Implemented using Singleton pattern
 */
public class PlaylistCollection extends MusicCollectionManager<Playlist> {
    private static PlaylistCollection instance;

    private PlaylistCollection() {
        // Private constructor for Singleton pattern
    }

    public static synchronized PlaylistCollection getInstance() {
        if (instance == null) {
            instance = new PlaylistCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Playlist item) {
        return item.getId();
    }


    public List<Playlist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase();
        List<Playlist> results = new ArrayList<>();

        for (Playlist playlist : items.values()) {
            if (playlist.getName().toLowerCase().contains(searchTerm)) {
                results.add(playlist);
            }
        }

        return results;
    }

    public List<Playlist> getPlaylistsContainingSong(Song song) {
        if (song == null) {
            return new ArrayList<>();
        }

        List<Playlist> results = new ArrayList<>();

        for (Playlist playlist : items.values()) {
            if (playlist.getSongs().contains(song)) {
                results.add(playlist);
            }
        }

        return results;
    }
}