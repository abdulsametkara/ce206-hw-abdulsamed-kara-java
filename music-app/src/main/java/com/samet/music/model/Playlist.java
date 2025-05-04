package com.samet.music.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Playlist model class representing a collection of songs
 */
public class Playlist {
    private int id;
    private String name;
    private String description;
    private int userId;
    private LocalDateTime createdAt;
    private List<Song> songs;

    // Default constructor
    public Playlist() {
        this.songs = new ArrayList<>();
    }

    // Constructor without id and createdAt (for new playlists)
    public Playlist(String name, String description, int userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.songs = new ArrayList<>();
    }

    // Full constructor
    public Playlist(int id, String name, String description, int userId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.createdAt = createdAt;
        this.songs = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Add a song to the playlist
     * @param song the song to add
     */
    public void addSong(Song song) {
        this.songs.add(song);
    }

    /**
     * Remove a song from the playlist
     * @param song the song to remove
     * @return true if the song was removed, false otherwise
     */
    public boolean removeSong(Song song) {
        return this.songs.remove(song);
    }

    /**
     * Remove a song from the playlist by id
     * @param songId the id of the song to remove
     * @return true if the song was removed, false otherwise
     */
    public boolean removeSongById(int songId) {
        return this.songs.removeIf(song -> song.getId() == songId);
    }

    /**
     * Get the total duration of the playlist in seconds
     * @return the total duration
     */
    public int getTotalDuration() {
        return this.songs.stream().mapToInt(Song::getDuration).sum();
    }

    /**
     * Get the formatted total duration of the playlist as hh:mm:ss
     * @return the formatted total duration
     */
    public String getFormattedTotalDuration() {
        int totalSeconds = getTotalDuration();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Get the number of songs in the playlist
     * @return the number of songs
     */
    public int getSongCount() {
        return this.songs.size();
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", songCount=" + getSongCount() +
                ", totalDuration=" + getFormattedTotalDuration() +
                '}';
    }
} 