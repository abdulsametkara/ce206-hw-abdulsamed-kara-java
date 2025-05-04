package com.samet.music.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Artist model class representing a music artist in the library
 */
public class Artist {
    private int id;
    private String name;
    private String bio;
    private int userId;
    private LocalDateTime createdAt;
    private List<Song> songs;
    private List<Album> albums;

    // Default constructor
    public Artist() {
        this.songs = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    // Constructor without id and createdAt (for new artists)
    public Artist(String name, String bio, int userId) {
        this.name = name;
        this.bio = bio;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.songs = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    // Full constructor
    public Artist(int id, String name, String bio, int userId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.userId = userId;
        this.createdAt = createdAt;
        this.songs = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    /**
     * Add a song to the artist
     * @param song the song to add
     */
    public void addSong(Song song) {
        this.songs.add(song);
    }

    /**
     * Add an album to the artist
     * @param album the album to add
     */
    public void addAlbum(Album album) {
        this.albums.add(album);
    }

    /**
     * Get the total number of songs by this artist
     * @return the number of songs
     */
    public int getSongCount() {
        return this.songs.size();
    }

    /**
     * Get the total number of albums by this artist
     * @return the number of albums
     */
    public int getAlbumCount() {
        return this.albums.size();
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songCount=" + getSongCount() +
                ", albumCount=" + getAlbumCount() +
                '}';
    }
} 