package com.samet.music.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Playlist entity representing a collection of songs
 */
@Entity
@Table(name = "playlists")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "playlist_songs",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> songs = new HashSet<>();
    
    // Constructors
    public Playlist() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Playlist(Integer id, String name, String description, User user, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.user = user;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    public Playlist(String name, String description, User user) {
        this(null, name, description, user, LocalDateTime.now());
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    
    public Set<Song> getSongs() {
        return songs;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }
    
    // Helper methods
    public void addSong(Song song) {
        songs.add(song);
        song.getPlaylists().add(this);
    }
    
    public void removeSong(Song song) {
        songs.remove(song);
        song.getPlaylists().remove(this);
    }
    
    /**
     * Get the number of songs in the playlist
     * @return the song count
     */
    public int getSongCount() {
        return songs.size();
    }
    
    @Override
    public String toString() {
        return "Playlist [id=" + id + ", name=" + name + ", songCount=" + songs.size() + "]";
    }
} 