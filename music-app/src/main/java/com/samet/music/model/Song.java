package com.samet.music.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Song entity representing a music track
 */
@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String title;
    
    @Size(max = 100, message = "Artist must be less than 100 characters")
    @Column(length = 100)
    private String artist;
    
    @Size(max = 100, message = "Album must be less than 100 characters")
    @Column(length = 100)
    private String album;
    
    @Size(max = 50, message = "Genre must be less than 50 characters")
    @Column(length = 50)
    private String genre;
    
    @Column
    private Integer year;
    
    @Column
    private Integer duration;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToMany(mappedBy = "songs")
    private Set<Playlist> playlists = new HashSet<>();
    
    // Constructors
    public Song() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Song(Integer id, String title, String artist, String album, String genre, Integer year, Integer duration,
            String filePath, User user, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.filePath = filePath;
        this.user = user;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    public Song(String title, String artist, String album, String genre, Integer year, Integer duration, String filePath,
            User user) {
        this(null, title, artist, album, genre, year, duration, filePath, user, LocalDateTime.now());
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Get the user ID
     * @return the user ID
     */
    public Integer getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * Set the user ID by creating a User object
     * @param userId the user ID
     */
    public void setUserId(Integer userId) {
        if (userId != null) {
            User newUser = new User();
            newUser.setId(userId);
            this.user = newUser;
        } else {
            this.user = null;
        }
    }
    
    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }
    
    /**
     * Get the formatted duration in MM:SS format
     * @return formatted duration string
     */
    public String getFormattedDuration() {
        if (duration == null) {
            return "0:00";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    @Override
    public String toString() {
        return "Song [id=" + id + ", title=" + title + ", artist=" + artist + "]";
    }
} 