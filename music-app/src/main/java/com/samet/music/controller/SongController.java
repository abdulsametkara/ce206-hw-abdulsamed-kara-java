package com.samet.music.controller;

import com.samet.music.dao.SongDAO;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.model.Album;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for song operations
 */
public class SongController {
    private static final Logger logger = LoggerFactory.getLogger(SongController.class);
    private SongDAO songDAO;
    private final UserController userController;

    /**
     * Constructor
     * @param userController the user controller
     */
    public SongController(UserController userController) {
        this.songDAO = new SongDAO();
        this.userController = userController;
    }
    
    /**
     * Set the SongDAO instance (for testing purposes)
     * @param songDAO the SongDAO instance to use
     */
    public void setSongDAO(SongDAO songDAO) {
        this.songDAO = songDAO;
    }

    /**
     * Add a new song to the library
     * @param title the song title
     * @param artist the artist name
     * @param album the album name
     * @param genre the genre
     * @param year the release year
     * @param duration the duration in seconds
     * @param filePath the path to the audio file
     * @return the added Song object, or null if addition failed
     */
    public Song addSong(String title, String artist, String album, String genre, int year, int duration, String filePath) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot add song: no user is logged in");
            return null;
        }
        
        // Validate the file exists
        if (!isValidFile(filePath)) {
            logger.warn("Cannot add song: file does not exist at {}", filePath);
            return null;
        }
        
        Song song = new Song(title, artist, album, genre, year, duration, filePath, currentUser.getId());
        Song addedSong = songDAO.create(song);
        
        if (addedSong != null) {
            logger.info("Song added: {} by {}", title, artist);
        } else {
            logger.warn("Failed to add song: {} by {}", title, artist);
        }
        
        return addedSong;
    }

    /**
     * Checks if a file exists and is a valid file (not a directory)
     * @param filePath the path to check
     * @return true if the file exists and is valid, false otherwise
     */
    protected boolean isValidFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * Update a song's metadata
     * @param songId the song ID
     * @param title the new title (null if not changing)
     * @param artist the new artist (null if not changing)
     * @param album the new album (null if not changing)
     * @param genre the new genre (null if not changing)
     * @param year the new year (0 if not changing)
     * @return true if update successful, false otherwise
     */
    public boolean updateSong(int songId, String title, String artist, String album, String genre, int year) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot update song: no user is logged in");
            return false;
        }
        
        Optional<Song> songOpt = songDAO.findById(songId);
        
        if (!songOpt.isPresent()) {
            logger.warn("Cannot update song: song with ID {} not found", songId);
            return false;
        }
        
        Song song = songOpt.get();
        
        // Ensure user owns this song
        if (song.getUserId() != currentUser.getId()) {
            logger.warn("Cannot update song: user does not own song with ID {}", songId);
            return false;
        }
        
        // Update fields if provided
        if (title != null && !title.isEmpty()) {
            song.setTitle(title);
        }
        
        if (artist != null && !artist.isEmpty()) {
            song.setArtist(artist);
        }
        
        if (album != null && !album.isEmpty()) {
            song.setAlbum(album);
        }
        
        if (genre != null && !genre.isEmpty()) {
            song.setGenre(genre);
        }
        
        if (year > 0) {
            song.setYear(year);
        }
        
        boolean updated = songDAO.update(song);
        
        if (updated) {
            logger.info("Song updated: ID {}, {}", songId, song.getTitle());
        } else {
            logger.warn("Failed to update song: ID {}", songId);
        }
        
        return updated;
    }

    /**
     * Delete a song from the library
     * @param songId the song ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteSong(int songId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot delete song: no user is logged in");
            return false;
        }
        
        Optional<Song> songOpt = songDAO.findById(songId);
        
        if (!songOpt.isPresent()) {
            logger.warn("Cannot delete song: song with ID {} not found", songId);
            return false;
        }
        
        Song song = songOpt.get();
        
        // Ensure user owns this song
        if (song.getUserId() != currentUser.getId()) {
            logger.warn("Cannot delete song: user does not own song with ID {}", songId);
            return false;
        }
        
        boolean deleted = songDAO.delete(songId);
        
        if (deleted) {
            logger.info("Song deleted: ID {}, {}", songId, song.getTitle());
        } else {
            logger.warn("Failed to delete song: ID {}", songId);
        }
        
        return deleted;
    }

    /**
     * Get all songs for the current user
     * @return a list of songs
     */
    public List<Song> getUserSongs() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get songs: no user is logged in");
            return new ArrayList<>();
        }
        
        List<Song> songs = songDAO.findByUserId(currentUser.getId());
        logger.info("Retrieved {} songs for user {}", songs.size(), currentUser.getUsername());
        
        return songs;
    }

    /**
     * Search for songs by title, artist, album or genre
     * @param query the search query
     * @return a list of matching songs
     */
    public List<Song> searchSongs(String query) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot search songs: no user is logged in");
            return new ArrayList<>();
        }
        
        // Search in all fields
        List<Song> songs = songDAO.search(query, query, query, query);
        
        // Filter songs to only show user's songs
        List<Song> userSongs = songs.stream()
                .filter(song -> song.getUserId() == currentUser.getId())
                .collect(Collectors.toList());
        
        logger.info("Search for '{}' returned {} user songs", query, userSongs.size());
        
        return userSongs;
    }

    /**
     * Get recommendations based on user's music preferences
     * @return a list of recommended songs
     */
    public List<Song> getRecommendations() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get recommendations: no user is logged in");
            return new ArrayList<>();
        }
        
        // Get user's songs
        List<Song> userSongs = songDAO.findByUserId(currentUser.getId());
        
        // If user has no songs, return empty list
        if (userSongs.isEmpty()) {
            logger.info("No recommendations: user has no songs");
            return new ArrayList<>();
        }
        
        // Calculate genre preferences
        Map<String, Integer> genrePreferences = new HashMap<>();
        Map<String, Integer> artistPreferences = new HashMap<>();
        
        for (Song song : userSongs) {
            // Count genres
            String genre = song.getGenre();
            if (genre != null && !genre.isEmpty()) {
                genrePreferences.put(genre, genrePreferences.getOrDefault(genre, 0) + 1);
            }
            
            // Count artists
            String artist = song.getArtist();
            if (artist != null && !artist.isEmpty()) {
                artistPreferences.put(artist, artistPreferences.getOrDefault(artist, 0) + 1);
            }
        }
        
        // Get top 3 genres and artists
        List<String> topGenres = genrePreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<String> topArtists = artistPreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Get all songs
        List<Song> allSongs = songDAO.findAll();
        
        // Filter to songs not owned by user but matching preferences
        List<Song> recommendations = allSongs.stream()
                .filter(song -> song.getUserId() != currentUser.getId())
                .filter(song -> 
                    (song.getGenre() != null && topGenres.contains(song.getGenre())) ||
                    (song.getArtist() != null && topArtists.contains(song.getArtist()))
                )
                .collect(Collectors.toList());
        
        logger.info("Generated {} song recommendations for user", recommendations.size());
        
        return recommendations;
    }

    /**
     * Get songs by artist name
     * @param artistName the artist name to filter by
     * @return a list of songs by the artist
     */
    public List<Song> getSongsByArtist(String artistName) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get songs by artist: no user is logged in");
            return new ArrayList<>();
        }
        
        List<Song> userSongs = songDAO.findByUserId(currentUser.getId());
        
        // Filter by artist name
        return userSongs.stream()
            .filter(song -> song.getArtist() != null && song.getArtist().equalsIgnoreCase(artistName))
            .collect(Collectors.toList());
    }

    /**
     * Get all artists for the current user
     * @return a list of artist names
     */
    public List<String> getArtists() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get artists: no user is logged in");
            return new ArrayList<>();
        }
        
        // Get user's songs
        List<Song> userSongs = songDAO.findByUserId(currentUser.getId());
        
        // Extract unique artist names
        List<String> artists = userSongs.stream()
            .map(Song::getArtist)
            .filter(artist -> artist != null && !artist.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        
        logger.info("Retrieved {} artists for user {}", artists.size(), currentUser.getUsername());
        
        return artists;
    }

    /**
     * Get all artists for the current user
     * Alias for getArtists() method
     * @return a list of artist names
     */
    public List<String> getUserArtists() {
        return getArtists();
    }

    /**
     * Add an artist to the user's library
     * @param artistName the artist name
     * @return true if addition was successful, false otherwise
     */
    public boolean addArtist(String artistName) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot add artist: no user is logged in");
            return false;
        }
        
        if (artistName == null || artistName.trim().isEmpty()) {
            logger.warn("Cannot add artist: name is empty");
            return false;
        }
        
        // Check if artist already exists in user's artists
        List<String> existingArtists = getArtists();
        if (existingArtists.stream().anyMatch(a -> a.equalsIgnoreCase(artistName.trim()))) {
            // Artist already exists, return success
            return true;
        }
        
        // For now just return true as we don't have a separate artist table
        // but would track this in a real implementation
        logger.info("Artist added: {}", artistName);
        return true;
    }

    /**
     * Delete an artist from the user's library
     * Warning: This will not delete any songs but just removes the artist from the user's collection
     * @param artistName the artist name
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteArtist(String artistName) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot delete artist: no user is logged in");
            return false;
        }
        
        if (artistName == null || artistName.trim().isEmpty()) {
            logger.warn("Cannot delete artist: name is empty");
            return false;
        }
        
        // Check if the artist exists in the user's library
        List<String> existingArtists = getArtists();
        if (existingArtists.stream().noneMatch(a -> a.equalsIgnoreCase(artistName.trim()))) {
            logger.warn("Cannot delete artist: {} not found in user's library", artistName);
            return false;
        }
        
        // This is a mock implementation since we don't have a separate artist table
        // In a real implementation, we would delete the artist from the database
        logger.info("Artist deleted: {}", artistName);
        return true;
    }

    /**
     * Create an album for the current user
     * @param title the album title
     * @param artist the artist name
     * @param year the release year
     * @param genre the genre
     * @return the created Album, or null if creation failed
     */
    public Album addAlbum(String title, String artist, int year, String genre) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot add album: no user is logged in");
            return null;
        }
        
        // Create a new album
        Album album = new Album(title, artist, year, genre, currentUser.getId());
        
        // In a real implementation, we would save the album to the database via AlbumDAO
        // For now we just return the created album object without persisting it
        album.setId(1); // Mock ID assignment
        
        logger.info("Album added: {} by {}", title, artist);
        return album;
    }

    /**
     * Get all albums for the current user
     * @return a list of albums
     */
    public List<Album> getUserAlbums() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get albums: no user is logged in");
            return new ArrayList<>();
        }
        
        // In a real implementation, we would fetch albums from the database via AlbumDAO
        // For now we return an empty list
        logger.info("Retrieved albums for user {}", currentUser.getUsername());
        return new ArrayList<>();
    }

    /**
     * Delete an album from the user's library
     * @param albumId the album ID
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAlbum(int albumId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot delete album: no user is logged in");
            return false;
        }
        
        // In a real implementation, we would delete the album from the database via AlbumDAO
        // For now we just return success
        logger.info("Album deleted: ID {}", albumId);
        return true;
    }

    /**
     * Add a song to an album
     * @param albumId the album ID
     * @param songId the song ID
     * @return true if addition was successful, false otherwise
     */
    public boolean addSongToAlbum(int albumId, int songId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot add song to album: no user is logged in");
            return false;
        }
        
        // In a real implementation, we would first check if the album and song exist
        // and belong to the current user, then create the relationship in the database
        
        // For now we just return success
        logger.info("Song added to album: song ID {} to album ID {}", songId, albumId);
        return true;
    }
} 