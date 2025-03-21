package com.samet.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a playlist in the music library
 */
public class Playlist extends BaseEntity {
    private String description;
    private List<Song> songs;

    public Playlist(String name) {
        super(name);
        this.description = "";
        this.songs = new ArrayList<>();
    }

    public Playlist(String name, String description) {
        super(name);
        this.description = description;
        this.songs = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songs); // Return a copy to prevent external modification
    }

    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
        }
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public int getSongCount() {
        return songs.size();
    }

    public int getTotalDuration() {
        int totalDuration = 0;
        for (Song song : songs) {
            totalDuration += song.getDuration();
        }
        return totalDuration;
    }

    public String getFormattedTotalDuration() {
        int totalSeconds = getTotalDuration();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + songs.size() + " songs, " + getFormattedTotalDuration() + ")";
    }
}