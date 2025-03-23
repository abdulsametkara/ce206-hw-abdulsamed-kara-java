package com.samet.music.repository;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;

import java.util.List;
import java.util.ArrayList;

public class PlaylistCollection extends MusicCollectionManager<Playlist> {
    private static PlaylistCollection instance;
    private PlaylistDAO playlistDAO;
    private SongDAO songDAO;

    private PlaylistCollection() {
        playlistDAO = new PlaylistDAO();
        songDAO = new SongDAO();
    }

    public static synchronized PlaylistCollection getInstance() {
        if (instance == null) {
            instance = new PlaylistCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Playlist item) {
        return item.getId();
    }

    @Override
    public void add(Playlist playlist) {
        super.add(playlist);
        playlistDAO.insert(playlist);
    }

    @Override
    public Playlist getById(String id) {
        Playlist playlist = super.getById(id);

        if (playlist == null) {
            playlist = playlistDAO.getById(id);
            if (playlist != null) {
                super.add(playlist);
            }
        }

        return playlist;
    }

    @Override
    public List<Playlist> getAll() {
        return playlistDAO.getAll();
    }

    @Override
    public boolean remove(String id) {
        try {
            System.out.println("Removing playlist with ID: " + id);

            // Önce ana koleksiyondan kaldır
            boolean removed = super.remove(id);
            System.out.println("Removed from memory collection: " + removed);

            // Sonra DAO üzerinden sil
            playlistDAO.delete(id);
            System.out.println("Deleted from database through DAO");

            return true;
        } catch (Exception e) {
            System.err.println("Error in PlaylistCollection.remove: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @Override
    protected void loadFromDatabase() {
        clear(); // Önce mevcut öğeleri temizle

        // Veritabanından çalma listelerini yükle
        List<Playlist> playlists = playlistDAO.getAll();

        // Koleksiyona ekle
        for (Playlist playlist : playlists) {
            super.add(playlist);
        }
    }

    public List<Playlist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Playlist> results = new ArrayList<>();
        List<Playlist> allPlaylists = getAll();

        String searchTerm = name.toLowerCase();
        for (Playlist playlist : allPlaylists) {
            if (playlist.getName().toLowerCase().contains(searchTerm)) {
                results.add(playlist);
            }
        }

        return results;
    }

    public List<Playlist> getPlaylistsContainingSong(Song song) {
        return playlistDAO.getPlaylistsContainingSong(song);
    }

    public void addSongToPlaylist(String playlistId, String songId) {
        playlistDAO.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(String playlistId, String songId) {
        playlistDAO.removeSongFromPlaylist(playlistId, songId);
    }

    public boolean saveToFile(String filePath) {
        return true;
    }

    public boolean loadFromFile(String filePath) {
        List<Playlist> playlists = playlistDAO.getAll();

        clear();

        for (Playlist playlist : playlists) {
            add(playlist);
        }

        return !playlists.isEmpty();
    }
}