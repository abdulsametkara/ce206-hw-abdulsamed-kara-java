package com.samet.music;

import java.util.List;
import java.util.ArrayList;

public class SongCollection extends MusicCollectionManager<Song> {
    private static SongCollection instance;
    private SongDAO songDAO;
    private ArtistDAO artistDAO;
    private AlbumDAO albumDAO;

    private SongCollection() {
        songDAO = new SongDAO();
        artistDAO = new ArtistDAO();
        albumDAO = new AlbumDAO();
    }

    public static synchronized SongCollection getInstance() {
        if (instance == null) {
            instance = new SongCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Song item) {
        return item.getId();
    }

    @Override
    public void add(Song song) {
        super.add(song);
        songDAO.insert(song);
    }

    @Override
    public Song getById(String id) {
        Song song = super.getById(id);

        if (song == null) {
            song = songDAO.getById(id);
            if (song != null) {
                super.add(song);
            }
        }

        return song;
    }

    @Override
    public List<Song> getAll() {
        return songDAO.getAll();
    }

    @Override
    public boolean remove(String id) {
        boolean removed = super.remove(id);
        songDAO.delete(id);
        return removed;
    }

    public List<Song> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Song> results = new ArrayList<>();
        List<Song> allSongs = getAll();

        String searchTerm = name.toLowerCase();
        for (Song song : allSongs) {
            if (song.getName().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        return results;
    }

    public List<Song> getByArtist(Artist artist) {
        return songDAO.getByArtist(artist);
    }

    public List<Song> getByAlbum(Album album) {
        return songDAO.getByAlbum(album);
    }

    public List<Song> getByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Song> results = new ArrayList<>();
        List<Song> allSongs = getAll();

        String searchTerm = genre.toLowerCase();
        for (Song song : allSongs) {
            if (song.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(song);
            }
        }

        return results;
    }

    public boolean saveToFile(String filePath) {
        return true;
    }

    public boolean loadFromFile(String filePath) {
        List<Song> songs = songDAO.getAll();

        clear();

        for (Song song : songs) {
            add(song);
        }

        return !songs.isEmpty();
    }
}