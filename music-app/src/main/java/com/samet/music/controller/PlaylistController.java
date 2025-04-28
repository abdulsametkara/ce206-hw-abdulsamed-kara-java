package com.samet.music.controller;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for playlist operations
 */
public class PlaylistController {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private final PlaylistDAO playlistDAO;
    private final SongDAO songDAO;
    private final UserController userController;

    /**
     * Constructor
     * @param userController the user controller
     */
    public PlaylistController(UserController userController) {
        this.songDAO = new SongDAO();
        this.playlistDAO = new PlaylistDAO(this.songDAO);
        this.userController = userController;
    }
    
    /**
     * Get the playlist DAO
     * @return the playlist DAO
     */
    protected PlaylistDAO getPlaylistDAO() {
        return playlistDAO;
    }
    
    /**
     * Get the song DAO
     * @return the song DAO
     */
    protected SongDAO getSongDAO() {
        return songDAO;
    }

    /**
     * Create a new playlist
     * @param name the playlist name
     * @param description the playlist description
     * @return the created Playlist object, or null if creation failed
     */
    public Playlist createPlaylist(String name, String description) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot create playlist: no user is logged in");
            return null;
        }
        
        Playlist playlist = new Playlist(name, description, currentUser);
        Playlist createdPlaylist = playlistDAO.create(playlist);
        
        if (createdPlaylist != null) {
            logger.info("Playlist created: {} (ID: {})", name, createdPlaylist.getId());
        } else {
            logger.warn("Failed to create playlist: {}", name);
        }
        
        return createdPlaylist;
    }

    /**
     * Update a playlist
     * @param playlistId the playlist ID
     * @param name the new name (null if not changing)
     * @param description the new description (null if not changing)
     * @return true if update successful, false otherwise
     */
    public boolean updatePlaylist(int playlistId, String name, String description) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot update playlist: no user is logged in");
            return false;
        }
        
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        
        if (!playlistOpt.isPresent()) {
            logger.warn("Cannot update playlist: playlist with ID {} not found", playlistId);
            return false;
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Ensure user owns this playlist
        if (playlist.getUser().getId().equals(currentUser.getId())) {
            // Update fields if provided
            if (name != null && !name.isEmpty()) {
                playlist.setName(name);
            }
            
            if (description != null) {
                playlist.setDescription(description);
            }
            
            boolean updated = playlistDAO.update(playlist);
            
            if (updated) {
                logger.info("Playlist updated: ID {}, {}", playlistId, playlist.getName());
            } else {
                logger.warn("Failed to update playlist: ID {}", playlistId);
            }
            
            return updated;
        } else {
            logger.warn("Cannot update playlist: user does not own playlist with ID {}", playlistId);
            return false;
        }
    }

    /**
     * Delete a playlist
     * @param playlistId the playlist ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deletePlaylist(int playlistId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot delete playlist: no user is logged in");
            return false;
        }
        
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        
        if (!playlistOpt.isPresent()) {
            logger.warn("Cannot delete playlist: playlist with ID {} not found", playlistId);
            return false;
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Ensure user owns this playlist
        if (playlist.getUser().getId().equals(currentUser.getId())) {
            boolean deleted = playlistDAO.delete(playlistId);
            
            if (deleted) {
                logger.info("Playlist deleted: ID {}, {}", playlistId, playlist.getName());
            } else {
                logger.warn("Failed to delete playlist: ID {}", playlistId);
            }
            
            return deleted;
        } else {
            logger.warn("Cannot delete playlist: user does not own playlist with ID {}", playlistId);
            return false;
        }
    }

    /**
     * Add a song to a playlist
     * @param playlistId the playlist ID
     * @param songId the song ID
     * @return true if addition successful, false otherwise
     */
    public boolean addSongToPlaylist(int playlistId, int songId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot add song to playlist: no user is logged in");
            return false;
        }
        
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        Optional<Song> songOpt = songDAO.findById(songId);
        
        if (!playlistOpt.isPresent()) {
            logger.warn("Cannot add song to playlist: playlist with ID {} not found", playlistId);
            return false;
        }
        
        if (!songOpt.isPresent()) {
            logger.warn("Cannot add song to playlist: song with ID {} not found", songId);
            return false;
        }
        
        Playlist playlist = playlistOpt.get();
        Song song = songOpt.get();
        
        // Ensure user owns this playlist
        if (playlist.getUser().getId().equals(currentUser.getId())) {
            boolean added = addSingleSongToPlaylist(playlistId, songId);
            
            if (added) {
                logger.info("Song added to playlist: song {} added to playlist {}", song.getTitle(), playlist.getName());
            } else {
                logger.warn("Failed to add song to playlist: song {} to playlist {}", song.getTitle(), playlist.getName());
            }
            
            return added;
        } else {
            logger.warn("Cannot add song to playlist: user does not own playlist with ID {}", playlistId);
            return false;
        }
    }

    /**
     * Helper method to add a single song to a playlist
     */
    private boolean addSingleSongToPlaylist(int playlistId, int songId) {
        Optional<Song> songOpt = songDAO.findById(songId);
        if (!songOpt.isPresent()) {
            return false;
        }
        
        List<Song> songs = new ArrayList<>();
        songs.add(songOpt.get());
        return playlistDAO.addSongsToPlaylist(playlistId, songs);
    }

    /**
     * Remove a song from a playlist
     * @param playlistId the playlist ID
     * @param songId the song ID
     * @return true if removal successful, false otherwise
     */
    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot remove song from playlist: no user is logged in");
            return false;
        }
        
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        
        if (!playlistOpt.isPresent()) {
            logger.warn("Cannot remove song from playlist: playlist with ID {} not found", playlistId);
            return false;
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Ensure user owns this playlist
        if (playlist.getUser().getId().equals(currentUser.getId())) {
            boolean removed = removeSingleSongFromPlaylist(playlistId, songId);
            
            if (removed) {
                logger.info("Song removed from playlist: song ID {} removed from playlist {}", songId, playlist.getName());
            } else {
                logger.warn("Failed to remove song from playlist: song ID {} from playlist {}", songId, playlist.getName());
            }
            
            return removed;
        } else {
            logger.warn("Cannot remove song from playlist: user does not own playlist with ID {}", playlistId);
            return false;
        }
    }
    
    /**
     * Helper method to remove a single song from a playlist
     */
    private boolean removeSingleSongFromPlaylist(int playlistId, int songId) {
        // Get current songs, remove the specified one, then update
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        if (!playlistOpt.isPresent()) {
            return false;
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Find the song to remove
        Optional<Song> songOpt = songDAO.findById(songId);
        if (!songOpt.isPresent()) {
            return false;
        }
        
        Song song = songOpt.get();
        playlist.removeSong(song);
        
        return playlistDAO.update(playlist);
    }

    /**
     * Get all playlists for the current user
     * @return a list of playlists
     */
    public List<Playlist> getUserPlaylists() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            // Sessizce boş bir liste döndür
            return new ArrayList<>();
        }
        
        List<Playlist> playlists = new ArrayList<>();
        try {
            playlists = playlistDAO.findByUserId(currentUser.getId());
            logger.info("Retrieved {} playlists for user {}", playlists.size(), currentUser.getUsername());
        } catch (Exception e) {
            // Exception'ı sessizce yut ve boş bir liste döndür
            logger.error("Error retrieving playlists", e);
        }
        
        return playlists;
    }

    /**
     * Get a playlist by ID
     * @param playlistId the playlist ID
     * @return the Playlist object, or null if not found or not owned by the current user
     */
    public Playlist getPlaylist(int playlistId) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get playlist: no user is logged in");
            return null;
        }
        
        Optional<Playlist> playlistOpt = playlistDAO.findById(playlistId);
        
        if (!playlistOpt.isPresent()) {
            logger.warn("Cannot get playlist: playlist with ID {} not found", playlistId);
            return null;
        }
        
        Playlist playlist = playlistOpt.get();
        
        // Ensure user owns this playlist
        if (playlist.getUser().getId().equals(currentUser.getId())) {
            return playlist;
        } else {
            logger.warn("Cannot get playlist: user does not own playlist with ID {}", playlistId);
            return null;
        }
    }
} 