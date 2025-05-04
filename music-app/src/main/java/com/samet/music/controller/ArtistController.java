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
        
        // Get artists from songs
        artists.addAll(songDAO.findAll().stream()
                .map(song -> song.getArtist())
                .filter(artist -> artist != null && !artist.isEmpty())
                .collect(Collectors.toSet()));
        
        // Get artists from albums
        artists.addAll(albumDAO.findAll().stream()
                .map(album -> album.getArtist())
                .filter(artist -> artist != null && !artist.isEmpty())
                .collect(Collectors.toSet()));
        
        return artists;
    }
    
    public boolean artistExists(String artistName) {
        logger.info("Checking if artist exists: {}", artistName);
        if (artistName == null || artistName.trim().isEmpty()) {
            return false;
        }
        
        return getAllArtists().stream()
                .anyMatch(artist -> artist.equalsIgnoreCase(artistName.trim()));
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