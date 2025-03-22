package com.samet.music.service;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;

/**
 * Factory for creating music entities
 * Implements Factory Method Pattern
 */
public class MusicFactory {
    // Singleton pattern implementation
    private static MusicFactory instance;

    private MusicFactory() {
        // Private constructor for Singleton
    }

    public static synchronized MusicFactory getInstance() {
        if (instance == null) {
            instance = new MusicFactory();
        }
        return instance;
    }

    public Artist createArtist(String name, String biography) {
        return new Artist(name, biography);
    }

    public Album createAlbum(String name, Artist artist, int releaseYear, String genre) {
        Album album = new Album(name, artist, releaseYear);
        album.setGenre(genre);
        return album;
    }

    public Song createSong(String name, Artist artist, int duration, String genre) {
        Song song = new Song(name, artist, duration);
        song.setGenre(genre);
        return song;
    }
}