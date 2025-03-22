package com.samet.music.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artist in the music library
 */
public class Artist extends BaseEntity {
    private String biography;
    private List<Album> albums;
    private String originalId; // Veritabanından gelen orijinal ID'yi saklamak için

    public Artist(String name) {
        super(name);
        this.biography = "";
        this.albums = new ArrayList<>();
    }

    public Artist(String name, String biography) {
        super(name);
        this.biography = biography;
        this.albums = new ArrayList<>();
    }

    /**
     * Özel ID ile sanatçı oluşturur (veritabanından gelen ID için)
     */
    public Artist(String id, String name, String biography) {
        super(name);
        this.originalId = id;
        this.biography = biography;
        this.albums = new ArrayList<>();
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public List<Album> getAlbums() {
        return new ArrayList<>(albums); // Return a copy to prevent external modification
    }

    public void addAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    /**
     * Sanatçının ID'sini döndürür.
     * Eğer veritabanından özel bir ID atanmışsa, onu kullanır.
     */
    @Override
    public String getId() {
        if (originalId != null) {
            return originalId;
        }
        return super.getId();
    }

    /**
     * Veritabanından orijinal ID'yi ayarlar
     */
    public void setOriginalId(String id) {
        this.originalId = id;
    }

    @Override
    public String toString() {
        return getName() + " (" + albums.size() + " albums)";
    }
}