package com.samet.music.repository;

import com.samet.music.util.DatabaseUtil;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SongCollection extends MusicCollectionManager<Song> {
    private static SongCollection instance;
    private final SongDAO songDAO;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;

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
        // Önce loadFromDatabase metodu çağrılacak (gerekirse)
        return super.getAll();
    }

    @Override
    protected void loadFromDatabase() {
        clear(); // Önce mevcut öğeleri temizle

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs");
             ResultSet rs = stmt.executeQuery()) {

            Set<String> processedIds = new HashSet<>();

            while (rs.next()) {
                String id = rs.getString("id");

                // Eğer bu ID zaten işlendiyse, atla
                if (processedIds.contains(id)) {
                    continue;
                }

                processedIds.add(id);

                String name = rs.getString("name");
                int duration = rs.getInt("duration");
                String genre = rs.getString("genre");
                String artistId = rs.getString("artist_id");
                String albumId = rs.getString("album_id");

                Artist artist = null;
                Album album = null;

                if (artistId != null) {
                    artist = artistDAO.getById(artistId);
                }

                if (albumId != null) {
                    album = albumDAO.getById(albumId);
                }

                // ID'yi koruyacak özel Song nesnesi oluştur
                final String finalId = id;
                Song song = new Song(name, artist, duration) {
                    @Override
                    public String getId() {
                        return finalId;
                    }
                };

                song.setGenre(genre);
                if (album != null) {
                    song.setAlbum(album);
                }

                // Koleksiyona ekle
                add(song);
            }

        } catch (SQLException e) {
            System.err.println("Error loading songs from database: " + e.getMessage());
        }
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
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        List<Song> result = new ArrayList<>();
        String artistId = artist.getId();

        for (Song song : items.values()) {
            if (song.getArtist() != null && song.getArtist().getId().equals(artistId)) {
                result.add(song);
            }
        }

        return result;
    }

    public List<Song> getByAlbum(Album album) {
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }

        List<Song> result = new ArrayList<>();
        String albumId = album.getId();

        for (Song song : items.values()) {
            if (song.getAlbum() != null && song.getAlbum().getId().equals(albumId)) {
                result.add(song);
            }
        }

        return result;
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
        // SQLite kullanıldığından dosyaya kaydetmeye gerek yok
        return true;
    }

    public boolean loadFromFile(String filePath) {
        // Doğrudan veritabanından yükle
        loadFromDatabase();
        isLoaded = true;
        return true;
    }
}