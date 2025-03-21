package com.samet.music;

import java.util.List;
import java.util.ArrayList;

public class AlbumCollection extends MusicCollectionManager<Album> {
    private static AlbumCollection instance;
    private AlbumDAO albumDAO;
    private ArtistDAO artistDAO;

    private AlbumCollection() {
        albumDAO = new AlbumDAO();
        artistDAO = new ArtistDAO();
    }

    public static synchronized AlbumCollection getInstance() {
        if (instance == null) {
            instance = new AlbumCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Album item) {
        return item.getId();
    }

    @Override
    public void add(Album album) {
        super.add(album);
        albumDAO.insert(album);
    }

    @Override
    public Album getById(String id) {
        Album album = super.getById(id);

        if (album == null) {
            album = albumDAO.getById(id);
            if (album != null) {
                super.add(album);
            }
        }

        return album;
    }

    @Override
    public List<Album> getAll() {
        return albumDAO.getAll();
    }

    @Override
    public boolean remove(String id) {
        boolean removed = super.remove(id);
        albumDAO.delete(id);
        return removed;
    }

    public List<Album> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Album> results = new ArrayList<>();
        List<Album> allAlbums = getAll();

        String searchTerm = name.toLowerCase();
        for (Album album : allAlbums) {
            if (album.getName().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        return results;
    }

    public List<Album> getByArtist(Artist artist) {
        return albumDAO.getByArtist(artist);
    }

    public List<Album> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Album> results = new ArrayList<>();
        List<Album> allAlbums = getAll();

        String searchTerm = genre.toLowerCase();
        for (Album album : allAlbums) {
            if (album.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(album);
            }
        }

        return results;
    }

    public boolean saveToFile(String filePath) {
        return true;
    }

    public boolean loadFromFile(String filePath) {
        List<Album> albums = albumDAO.getAll();

        clear();

        for (Album album : albums) {
            add(album);
        }

        return !albums.isEmpty();
    }
}