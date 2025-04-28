package com.samet.music.controller;

import com.samet.music.dao.SongDAO;
import com.samet.music.dao.AlbumDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArtistController {
    private static final Logger logger = LoggerFactory.getLogger(ArtistController.class);
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;

    public ArtistController() {
        this.songDAO = new SongDAO();
        this.albumDAO = new AlbumDAO();
    }

    public ArtistController(SongDAO songDAO, AlbumDAO albumDAO) {
        this.songDAO = songDAO;
        this.albumDAO = albumDAO;
    }

    public Set<String> getAllArtists() {
        logger.info("Getting all artists");
        Set<String> artists = new HashSet<>();
        
        try {
            // Get artists from songs
            artists.addAll(songDAO.findAll().stream()
                    .map(song -> song.getArtist())
                    .filter(artist -> artist != null && !artist.isEmpty())
                    .collect(Collectors.toSet()));
            
            // Get artists from albums
            try {
                artists.addAll(albumDAO.findAll().stream()
                        .map(album -> album.getArtist())
                        .filter(artist -> artist != null && !artist.isEmpty())
                        .collect(Collectors.toSet()));
            } catch (Exception e) {
                logger.error("Error getting artists from albums: {}", e.getMessage());
                // Continue with just the artists from songs
            }
        } catch (Exception e) {
            logger.error("Error getting all artists: {}", e.getMessage());
            // Return an empty set in case of error
        }
        
        return artists;
    }
    
    public boolean artistExists(String artistName) {
        logger.info("Checking if artist exists: {}", artistName);
        if (artistName == null || artistName.trim().isEmpty()) {
            return false;
        }
        
        try {
            return getAllArtists().stream()
                    .anyMatch(artist -> artist.equalsIgnoreCase(artistName.trim()));
        } catch (Exception e) {
            logger.error("Error checking if artist exists: {}", e.getMessage());
            // Return false in case of error to allow creating a new artist
            return false;
        }
    }
    
    public int getArtistSongCount(String artistName) {
        logger.info("Getting song count for artist: {}", artistName);
        if (artistName == null || artistName.trim().isEmpty()) {
            return 0;
        }
        
        return (int) songDAO.findAll().stream()
                .filter(song -> artistName.equalsIgnoreCase(song.getArtist()))
                .count();
    }
    
    public int getArtistAlbumCount(String artistName) {
        logger.info("Getting album count for artist: {}", artistName);
        if (artistName == null || artistName.trim().isEmpty()) {
            return 0;
        }
        
        return (int) albumDAO.findAll().stream()
                .filter(album -> artistName.equalsIgnoreCase(album.getArtist()))
                .count();
    }
} 