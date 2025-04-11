package com.samet.music.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artist in the music library
 */
public class Artist extends BaseEntity {
    private String biography;
    private List<Album> albums;
    private String originalId; // To store the original ID from database

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
     * Creates an artist with a specific ID (for ID from database)
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
     * Returns the artist's ID.
     * If a special ID has been assigned from the database, uses that.
     */
    @Override
    public String getId() {
        if (originalId != null) {
            return originalId;
        }
        return super.getId();
    }

    /**
     * Sets the original ID from database
     */
    public void setOriginalId(String id) {
        this.originalId = id;
    }

    @Override
    public String toString() {
        return getName() + " (" + albums.size() + " albums)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Artist that = (Artist) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}