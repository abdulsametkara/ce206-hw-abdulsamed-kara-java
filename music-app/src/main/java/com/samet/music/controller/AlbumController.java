package com.samet.music.controller;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlbumController {
    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);
    private final AlbumDAO albumDAO;
    private final SongDAO songDAO;
    private final UserController userController;

    public AlbumController() {
        this.albumDAO = new AlbumDAO();
        this.songDAO = new SongDAO();
        this.userController = new UserController();
    }

    public AlbumController(AlbumDAO albumDAO, SongDAO songDAO) {
        this.albumDAO = albumDAO;
        this.songDAO = songDAO;
        this.userController = new UserController();
    }
    
    public AlbumController(AlbumDAO albumDAO, SongDAO songDAO, UserController userController) {
        this.albumDAO = albumDAO;
        this.songDAO = songDAO;
        this.userController = userController;
    }

    public boolean createAlbum(Album album) {
        logger.info("Creating album: {}", album.getTitle());
        return albumDAO.create(album);
    }

    public Album getAlbumById(int id) {
        logger.info("Getting album by ID: {}", id);
        return albumDAO.findById(id);
    }

    public List<Album> getAllAlbums() {
        logger.info("Getting all albums");
        return albumDAO.findAll();
    }

    public List<Album> getAlbumsByUserId(int userId) {
        logger.info("Getting albums for user ID: {}", userId);
        return albumDAO.findByUserId(userId);
    }

    public List<Album> getAlbumsByArtist(String artist) {
        logger.info("Getting albums by artist: {}", artist);
        return albumDAO.findByArtist(artist);
    }

    public boolean updateAlbum(Album album) {
        logger.info("Updating album: {}", album.getId());
        return albumDAO.update(album);
    }

    public boolean deleteAlbum(int id) {
        logger.info("Deleting album: {}", id);
        return albumDAO.delete(id);
    }

    public boolean addSongsToAlbum(int albumId, List<Song> songs) {
        logger.info("Adding {} songs to album: {}", songs.size(), albumId);
        return albumDAO.addSongsToAlbum(albumId, songs);
    }

    public boolean removeSongsFromAlbum(int albumId) {
        logger.info("Removing all songs from album: {}", albumId);
        return albumDAO.removeSongsFromAlbum(albumId);
    }

    public boolean exists(String title, String artist) {
        List<Album> albums = albumDAO.findByArtist(artist);
        return albums.stream()
                .anyMatch(album -> album.getTitle().equalsIgnoreCase(title) && 
                           album.getArtist().equalsIgnoreCase(artist));
    }
} 