package com.samet.music;

import java.util.List;
import java.util.ArrayList;

public class ArtistCollection extends MusicCollectionManager<Artist> {
    private static ArtistCollection instance;
    private ArtistDAO artistDAO;

    private ArtistCollection() {
        artistDAO = new ArtistDAO();
    }

    public static synchronized ArtistCollection getInstance() {
        if (instance == null) {
            instance = new ArtistCollection();
        }
        return instance;
    }

    @Override
    protected String getItemId(Artist item) {
        return item.getId();
    }

    @Override
    public void add(Artist artist) {
        // Önce hafızadaki koleksiyona ekle
        super.add(artist);

        // Sonra veritabanına kaydet
        artistDAO.insert(artist);
    }

    @Override
    public Artist getById(String id) {
        // Önce hafızadan kontrol et
        Artist artist = super.getById(id);

        // Eğer hafızada yoksa veritabanından çek
        if (artist == null) {
            artist = artistDAO.getById(id);
            if (artist != null) {
                // Hafızaya ekle
                super.add(artist);
            }
        }

        return artist;
    }

    @Override
    public List<Artist> getAll() {
        // Veritabanından tüm sanatçıları çek
        return artistDAO.getAll();
    }

    @Override
    public boolean remove(String id) {
        // Hafızadan sil
        boolean removed = super.remove(id);

        // Veritabanından sil
        artistDAO.delete(id);

        return removed;
    }

    @Override
    protected void loadFromDatabase() {
        clear(); // Önce mevcut öğeleri temizle

        // Veritabanından sanatçıları yükle
        List<Artist> artists = artistDAO.getAll();

        // Koleksiyona ekle
        for (Artist artist : artists) {
            super.add(artist);
        }
    }

    public List<Artist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Artist> results = new ArrayList<>();
        List<Artist> allArtists = getAll();

        String searchTerm = name.toLowerCase();
        for (Artist artist : allArtists) {
            if (artist.getName().toLowerCase().contains(searchTerm)) {
                results.add(artist);
            }
        }

        return results;
    }

    // Veritabanı kaydetme metodu
    public boolean saveToFile(String filePath) {
        // SQLite doğrudan veritabanına kaydettiği için bu metot boş bırakılabilir
        return true;
    }

    // Veritabanından yükleme metodu
    public boolean loadFromFile(String filePath) {
        // Tüm sanatçıları veritabanından çek
        List<Artist> artists = artistDAO.getAll();

        // Hafızadaki koleksiyonu temizle
        clear();

        // Tüm sanatçıları hafızaya ekle
        for (Artist artist : artists) {
            add(artist);
        }

        return !artists.isEmpty();
    }
}