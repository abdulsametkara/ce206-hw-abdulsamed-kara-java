package com.samet.music.repository;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Artist;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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

    // ArtistCollection.java sınıfında getAll metodunu değiştirelim
    @Override
    public List<Artist> getAll() {
        // Önce veritabanından çekelim
        List<Artist> allArtists = artistDAO.getAll();

        // Benzersiz sanatçıları saklamak için bir Map oluşturalım
        Map<String, Artist> uniqueArtistsById = new HashMap<>();
        for (Artist artist : allArtists) {
            uniqueArtistsById.put(artist.getId(), artist);
        }

        // İsim çakışmalarını kontrol edelim
        Map<String, List<Artist>> artistsByName = new HashMap<>();
        for (Artist artist : uniqueArtistsById.values()) {
            String name = artist.getName().toLowerCase();
            if (!artistsByName.containsKey(name)) {
                artistsByName.put(name, new ArrayList<>());
            }
            artistsByName.get(name).add(artist);
        }

        // İsim çakışması olan sanatçıları birleştirelim
        for (List<Artist> artistsWithSameName : artistsByName.values()) {
            if (artistsWithSameName.size() > 1) {
                // İlk sanatçıyı temel olarak alalım
                Artist primaryArtist = artistsWithSameName.get(0);

                // Diğer sanatçılardan veri birleştirelim
                for (int i = 1; i < artistsWithSameName.size(); i++) {
                    Artist duplicateArtist = artistsWithSameName.get(i);

                    // İlgili albüm ve şarkıları birincil sanatçıya atayalım
                    artistDAO.mergeArtists(primaryArtist.getId(), duplicateArtist.getId());

                    // Eşleme haritasından duplike sanatçıyı çıkaralım
                    uniqueArtistsById.remove(duplicateArtist.getId());
                }
            }
        }

        // Hafızadaki koleksiyonu temizleyip güncelleyelim
        clear();
        for (Artist artist : uniqueArtistsById.values()) {
            add(artist);
        }

        return new ArrayList<>(uniqueArtistsById.values());
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