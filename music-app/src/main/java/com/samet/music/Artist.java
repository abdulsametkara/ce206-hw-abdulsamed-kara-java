package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artist in the music library
 */
public class Artist extends BaseEntity {
    private String biography;
    private List<Album> albums;

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

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public List<Album> getAlbums() {
        return new ArrayList<>(albums);  // Return a copy to prevent external modification
    }

    public void addAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    @Override
    public String toString() {
        return getName() + " (" + albums.size() + " albums)";
    }
}