package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an album in the music library
 */
public class Album extends BaseEntity {
    private Artist artist;
    private int releaseYear;
    private List<Song> songs;
    private String genre;

    public Album(String name, Artist artist, int releaseYear) {
        super(name);
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.songs = new ArrayList<>();
        this.genre = "Unknown";

        // Add this album to the artist's albums
        if (artist != null) {
            artist.addAlbum(this);
        }
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        if (this.artist != null) {
            this.artist.removeAlbum(this);
        }

        this.artist = artist;

        if (artist != null) {
            artist.addAlbum(this);
        }
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songs);  // Return a copy to prevent external modification
    }

    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
            song.setAlbum(this);
        }
    }

    public void removeSong(Song song) {
        if (songs.remove(song)) {
            if (song.getAlbum() == this) {
                song.setAlbum(null);
            }
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + releaseYear + ") by " + (artist != null ? artist.getName() : "Unknown Artist");
    }
}