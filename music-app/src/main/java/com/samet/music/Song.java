package com.samet.music;

/**
 * Represents a song in the music library
 */
public class Song extends BaseEntity {
    private Album album;
    private Artist artist;
    private int duration; // in seconds
    private String genre;

    public Song(String name, Artist artist, int duration) {
        super(name);
        this.artist = artist;
        this.duration = duration;
        this.genre = "Unknown";
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        if (this.album != null && this.album != album) {
            this.album.removeSong(this);
        }

        this.album = album;

        if (album != null && !album.getSongs().contains(this)) {
            album.addSong(this);
        }
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return getName() + " - " +
                (artist != null ? artist.getName() : "Unknown Artist") +
                " - " + getFormattedDuration();
    }
}