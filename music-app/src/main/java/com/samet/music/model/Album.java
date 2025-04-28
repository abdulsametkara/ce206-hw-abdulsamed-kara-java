package com.samet.music.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for albums
 */
public class Album {
    private Integer id;
    private String title;
    private String artist;
    private Integer year;
    private String genre;
    private Integer userId;
    private LocalDateTime createdAt;
    private List<Song> songs = new ArrayList<>();

    /**
     * Default constructor
     */
    public Album() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with all fields except id
     */
    public Album(String title, String artist, int year, String genre, Integer userId) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.genre = genre;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with all fields
     */
    public Album(Integer id, String title, String artist, Integer year, String genre, Integer userId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.genre = genre;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    /**
     * Get the album id
     * @return the album id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the album id
     * @param id the album id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the album title
     * @return the album title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the album title
     * @param title the album title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the album artist
     * @return the album artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Set the album artist
     * @param artist the album artist
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Get the album year
     * @return the album year
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Set the album year
     * @param year the album year
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Get the album genre
     * @return the album genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Set the album genre
     * @param genre the album genre
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Get the user ID
     * @return the user ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Set the user ID
     * @param userId the user ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Get creation date
     * @return the creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set creation date
     * @param createdAt the creation date
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the songs in the album
     * @return the songs in the album
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Set the songs in the album
     * @param songs the songs in the album
     */
    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    /**
     * Add a song to the album
     * @param song the song to add
     */
    public void addSong(Song song) {
        if (this.songs == null) {
            this.songs = new ArrayList<>();
        }
        this.songs.add(song);
    }

    /**
     * Remove a song from the album
     * @param song the song to remove
     */
    public void removeSong(Song song) {
        if (this.songs != null) {
            this.songs.remove(song);
        }
    }

    /**
     * Remove a song from the album by id
     * @param songId the id of the song to remove
     * @return true if the song was removed, false otherwise
     */
    public boolean removeSongById(int songId) {
        return this.songs.removeIf(song -> song.getId() == songId);
    }

    /**
     * Get the number of songs in the album
     * @return the number of songs
     */
    public int getSongCount() {
        return songs != null ? songs.size() : 0;
    }

    /**
     * Get the total duration of all songs in the album
     * @return the total duration in seconds
     */
    public int getTotalDuration() {
        return songs.stream().mapToInt(Song::getDuration).sum();
    }

    /**
     * Get the formatted total duration of all songs
     * @return the formatted duration (HH:MM:SS)
     */
    public String getFormattedTotalDuration() {
        if (songs == null || songs.isEmpty()) {
            return "0:00";
        }
        
        int totalSeconds = 0;
        for (Song song : songs) {
            if (song.getDuration() != null) {
                totalSeconds += song.getDuration();
            }
        }
        
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", songsCount=" + (songs != null ? songs.size() : 0) +
                '}';
    }
} 