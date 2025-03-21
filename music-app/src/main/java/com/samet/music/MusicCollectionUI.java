package com.samet.music;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * UI handler for Music Collection operations
 */
public class MusicCollectionUI {
    private final Scanner scanner;
    private final PrintStream out;
    private final MusicCollectionService service;

    public MusicCollectionUI(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
        this.service = MusicCollectionService.getInstance();
    }

    /**
     * Handles adding a new song to the collection
     */
    public void addSong() {
        out.println("\n========== ADD NEW SONG ==========");

        // If there are no artists, we need to create one first
        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists available. You need to add an artist first.");
            return;
        }

        // Display available artists
        out.println("\nAvailable artists:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get artist selection
        out.print("\nSelect artist (number): ");
        int artistIndex;
        try {
            artistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (artistIndex < 0 || artistIndex >= artists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);

        // Get song details
        out.print("Enter song name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Song name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter song duration (in seconds): ");
        int duration;
        try {
            duration = Integer.parseInt(scanner.nextLine().trim());
            if (duration <= 0) {
                out.println("Duration must be positive. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid duration. Operation cancelled.");
            return;
        }

        out.print("Enter song genre: ");
        String genre = scanner.nextLine().trim();
        if (genre.isEmpty()) {
            genre = "Unknown";
        }

        // Create the song
        boolean success = service.addSong(name, selectedArtist.getId(), duration, genre);

        if (success) {
            out.println("\nSong '" + name + "' added successfully!");

            // Check if the song should be added to an album
            List<Album> albums = service.getAlbumsByArtist(selectedArtist.getId());
            if (!albums.isEmpty()) {
                out.println("\nDo you want to add this song to an album?");
                out.println("1. Yes");
                out.println("2. No");
                out.print("Your choice: ");

                String choice = scanner.nextLine().trim();
                if (choice.equals("1")) {
                    addSongToAlbum(selectedArtist);
                }
            }
        } else {
            out.println("\nFailed to add song. Please try again.");
        }
    }

    /**
     * Handles adding a song to an album
     */
    public void addSongToAlbum(Artist artist) {
        List<Album> albums = service.getAlbumsByArtist(artist.getId());

        if (albums.isEmpty()) {
            out.println("No albums available for this artist. You need to add an album first.");
            return;
        }

        // Display available albums
        out.println("\nAvailable albums for " + artist.getName() + ":");
        for (int i = 0; i < albums.size(); i++) {
            out.println((i + 1) + ". " + albums.get(i).getName() + " (" + albums.get(i).getReleaseYear() + ")");
        }

        // Get album selection
        out.print("\nSelect album (number): ");
        int albumIndex;
        try {
            albumIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (albumIndex < 0 || albumIndex >= albums.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Album selectedAlbum = albums.get(albumIndex);

        // Get songs not in this album
        List<Song> songs = service.getSongsByArtist(artist.getId());
        List<Song> albumSongs = service.getSongsByAlbum(selectedAlbum.getId());

        List<Song> availableSongs = songs.stream()
                .filter(song -> !albumSongs.contains(song))
                .collect(Collectors.toList());

        if (availableSongs.isEmpty()) {
            out.println("No songs available to add to this album.");
            return;
        }

        // Display available songs
        out.println("\nAvailable songs to add to " + selectedAlbum.getName() + ":");
        for (int i = 0; i < availableSongs.size(); i++) {
            out.println((i + 1) + ". " + availableSongs.get(i).getName() + " (" + availableSongs.get(i).getFormattedDuration() + ")");
        }

        // Get song selection
        out.print("\nSelect song (number): ");
        int songIndex;
        try {
            songIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (songIndex < 0 || songIndex >= availableSongs.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Song selectedSong = availableSongs.get(songIndex);

        boolean success = service.addSongToAlbum(selectedSong.getId(), selectedAlbum.getId());

        if (success) {
            out.println("\nSong '" + selectedSong.getName() + "' added to album '" + selectedAlbum.getName() + "' successfully!");
        } else {
            out.println("\nFailed to add song to album. Please try again.");
        }
    }

    /**
     * Handles adding a new album to the collection
     */
    public void addAlbum() {
        out.println("\n========== ADD NEW ALBUM ==========");

        // If there are no artists, we need to create one first
        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists available. You need to add an artist first.");
            return;
        }

        // Display available artists
        out.println("\nAvailable artists:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get artist selection
        out.print("\nSelect artist (number): ");
        int artistIndex;
        try {
            artistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (artistIndex < 0 || artistIndex >= artists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);

        // Get album details
        out.print("Enter album name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Album name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter release year: ");
        int releaseYear;
        try {
            releaseYear = Integer.parseInt(scanner.nextLine().trim());
            if (releaseYear <= 0) {
                out.println("Release year must be positive. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid release year. Operation cancelled.");
            return;
        }

        out.print("Enter album genre: ");
        String genre = scanner.nextLine().trim();
        if (genre.isEmpty()) {
            genre = "Unknown";
        }

        // Create the album
        boolean success = service.addAlbum(name, selectedArtist.getId(), releaseYear, genre);

        if (success) {
            out.println("\nAlbum '" + name + "' added successfully!");
        } else {
            out.println("\nFailed to add album. Please try again.");
        }
    }

    /**
     * Handles adding a new artist to the collection
     */
    public void addArtist() {
        out.println("\n========== ADD NEW ARTIST ==========");

        out.print("Enter artist name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Artist name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter artist biography (optional): ");
        String biography = scanner.nextLine().trim();

        boolean success = service.addArtist(name, biography);

        if (success) {
            out.println("\nArtist '" + name + "' added successfully!");
        } else {
            out.println("\nFailed to add artist. Please try again.");
        }
    }

    /**
     * Displays all songs in the collection
     */
    public void viewSongs() {
        out.println("\n========== ALL SONGS ==========");

        List<Song> songs = service.getAllSongs();

        if (songs.isEmpty()) {
            out.println("No songs in the collection.");
            return;
        }

        out.println("Total songs: " + songs.size());
        out.println("\nID | Name | Artist | Duration | Album | Genre");
        out.println("------------------------------------------------");

        for (Song song : songs) {
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            String albumName = song.getAlbum() != null ? song.getAlbum().getName() : "N/A";

            out.println(song.getId() + " | " +
                    song.getName() + " | " +
                    artistName + " | " +
                    song.getFormattedDuration() + " | " +
                    albumName + " | " +
                    song.getGenre());
        }
    }

    /**
     * Displays all albums in the collection
     */
    public void viewAlbums() {
        out.println("\n========== ALL ALBUMS ==========");

        List<Album> albums = service.getAllAlbums();

        if (albums.isEmpty()) {
            out.println("No albums in the collection.");
            return;
        }

        out.println("Total albums: " + albums.size());
        out.println("\nID | Name | Artist | Release Year | Genre | Songs");
        out.println("--------------------------------------------------");

        for (Album album : albums) {
            String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";
            List<Song> songs = service.getSongsByAlbum(album.getId());

            out.println(album.getId() + " | " +
                    album.getName() + " | " +
                    artistName + " | " +
                    album.getReleaseYear() + " | " +
                    album.getGenre() + " | " +
                    songs.size());
        }
    }

    /**
     * Displays all artists in the collection
     */
    public void viewArtists() {
        out.println("\n========== ALL ARTISTS ==========");

        List<Artist> artists = service.getAllArtists();

        if (artists.isEmpty()) {
            out.println("No artists in the collection.");
            return;
        }

        out.println("Total artists: " + artists.size());
        out.println("\nID | Name | Albums | Songs");
        out.println("---------------------------");

        for (Artist artist : artists) {
            List<Album> albums = service.getAlbumsByArtist(artist.getId());
            List<Song> songs = service.getSongsByArtist(artist.getId());

            out.println(artist.getId() + " | " +
                    artist.getName() + " | " +
                    albums.size() + " | " +
                    songs.size());
        }
    }
}