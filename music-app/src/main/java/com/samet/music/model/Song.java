package com.samet.music.model;

import java.sql.Timestamp;
import com.samet.music.util.TimeFormatter;

/**
 * Song model class representing a music track in the library
 */
public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int year;
    private int duration; // in seconds
    private String filePath;
    private int userId;
    private Timestamp createdAt;

    // Default constructor
    public Song() {
    }

    // Constructor without id and createdAt (for new songs)
    public Song(String title, String artist, String album, String genre, int year, int duration, String filePath, int userId) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.filePath = filePath;
        this.userId = userId;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Full constructor
    public Song(int id, String title, String artist, String album, String genre, int year, int duration, String filePath, int userId, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.filePath = filePath;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns formatted duration in MM:SS format
     * @return Formatted duration string
     */
    public String getFormattedDuration() {
        return TimeFormatter.formatDuration(duration);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", year=" + year +
                ", duration=" + getFormattedDuration() +
                ", filePath='" + filePath + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 