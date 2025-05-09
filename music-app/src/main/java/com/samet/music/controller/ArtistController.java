package com.samet.music.controller;

import com.samet.music.dao.SongDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.model.Album;
import com.samet.music.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for artist-related operations
 */
public class ArtistController {
    private final ArtistDAO artistDAO;
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final UserController userController;

    /**
     * Constructor
     */
    public ArtistController() {
        this.artistDAO = new ArtistDAO();
        this.songDAO = new SongDAO();
        this.albumDAO = new AlbumDAO();
        this.userController = new UserController();
    }

    /**
     * Constructor with dependencies
     */
    public ArtistController(ArtistDAO artistDAO, SongDAO songDAO, AlbumDAO albumDAO, UserController userController) {
        this.artistDAO = artistDAO;
        this.songDAO = songDAO;
        this.albumDAO = albumDAO;
        this.userController = userController;
    }

    /**
     * Get all artists in the system
     * @return a set of artist names
     */
    public Set<String> getAllArtists() {
        return artistDAO.getAllArtistNames();
    }
    
    /**
     * Check if an artist exists in the system
     * @param artistName the artist name to check
     * @return true if the artist exists, false otherwise
     */
    public boolean artistExists(String artistName) {
        return artistDAO.artistExists(artistName);
    }
    
    /**
     * Get the number of songs for an artist
     * @param artistName the artist name
     * @return the number of songs
     */
    public int getArtistSongCount(String artistName) {
        return artistDAO.getArtistSongCount(artistName);
    }
    
    /**
     * Get the number of albums for an artist
     * @param artistName the artist name
     * @return the number of albums
     */
    public int getArtistAlbumCount(String artistName) {
        return artistDAO.getArtistAlbumCount(artistName);
    }
    
    /**
     * Add a new artist
     * @param name the artist name
     * @param bio the artist bio
     * @return the created artist, or null if unsuccessful
     */
    public Artist addArtist(String name, String bio) {
        User currentUser = userController.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        // Check if name is valid
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // Create artist object
        Artist artist = new Artist(name, bio != null ? bio : "", currentUser.getId());
        
        // Add to database
        return artistDAO.create(artist);
    }
    
    /**
     * Get an artist by name
     * @param name the artist name
     * @return the artist, or null if not found
     */
    public Artist getArtistByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        return artistDAO.findByName(name);
    }
    
    /**
     * Get all artists for the current user
     * @return a list of artists
     */
    public List<Artist> getUserArtists() {
        User currentUser = userController.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        
        return artistDAO.findByUserId(currentUser.getId());
    }
    
    /**
     * Update an artist
     * @param artist the artist to update
     * @return true if successful, false otherwise
     */
    public boolean updateArtist(Artist artist) {
        User currentUser = userController.getCurrentUser();
        if (currentUser == null || artist == null) {
            return false;
        }
        
        // Check if the user owns this artist
        if (artist.getUserId() != currentUser.getId()) {
            return false;
        }
        
        return artistDAO.update(artist);
    }
    
    /**
     * Delete an artist
     * @param artistId the artist ID
     * @return true if successful, false otherwise
     */
    public boolean deleteArtist(int artistId) {
        User currentUser = userController.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Check if the user owns this artist
        Artist artist = artistDAO.findById(artistId);
        if (artist == null || artist.getUserId() != currentUser.getId()) {
            return false;
        }
        
        return artistDAO.delete(artistId);
    }
    
    /**
     * Get songs by an artist
     * @param artistName the artist name
     * @return a list of songs
     */
    public List<Song> getSongsByArtist(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return List.of();
        }
        
        return songDAO.findByArtist(artistName);
    }
    
    /**
     * Get albums by an artist
     * @param artistName the artist name
     * @return a list of albums
     */
    public List<Album> getAlbumsByArtist(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return List.of();
        }
        
        return albumDAO.findByArtist(artistName);
    }
} 