package com.samet.music;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class AlbumCollection extends MusicCollectionManager<Album> {
    private static AlbumCollection instance;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;

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
        // Önce loadFromDatabase metodu çağrılacak (gerekirse)
        return super.getAll();
    }

    @Override
    protected void loadFromDatabase() {
        clear(); // Önce mevcut öğeleri temizle

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums");
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
                int releaseYear = rs.getInt("release_year");
                String genre = rs.getString("genre");
                String artistId = rs.getString("artist_id");

                Artist artist = null;

                if (artistId != null) {
                    artist = artistDAO.getById(artistId);
                }

                // ID'yi koruyacak özel Album nesnesi oluştur
                final String finalId = id;
                Album album = new Album(name, artist, releaseYear) {
                    @Override
                    public String getId() {
                        return finalId;
                    }
                };

                album.setGenre(genre);

                // Koleksiyona ekle
                add(album);
            }

        } catch (SQLException e) {
            System.err.println("Error loading albums from database: " + e.getMessage());
        }
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
        if (!isLoaded) {
            try {
                loadFromDatabase();
                isLoaded = true;
            } catch (Exception e) {
                System.err.println("Error loading database in getByArtist: " + e.getMessage());
                return new ArrayList<>(); // Hata durumunda boş liste döndür
            }
        }

        List<Album> result = new ArrayList<>();
        String artistId = artist.getId();

        for (Album album : items.values()) {
            if (album.getArtist() != null && album.getArtist().getId().equals(artistId)) {
                result.add(album);
            }
        }

        return result;
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