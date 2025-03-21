package com.samet.music;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * UI handler for metadata editing operations
 */
public class MetadataEditingUI {
    private final Scanner scanner;
    private final PrintStream out;
    private final MusicCollectionService service;

    public MetadataEditingUI(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
        this.service = MusicCollectionService.getInstance();
    }

    /**
     * Handles editing artist metadata
     */
    public void editArtist() {
        out.println("\n========== EDIT ARTIST ==========");

        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists found in the collection.");
            return;
        }

        // Display available artists
        out.println("\nSelect an artist to edit:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get user selection
        out.print("\nEnter artist number (or 0 to cancel): ");
        int artistIndex;
        try {
            artistIndex = Integer.parseInt(scanner.nextLine().trim());
            if (artistIndex == 0) {
                return; // User cancelled
            }
            artistIndex--; // Convert to 0-based index

            if (artistIndex < 0 || artistIndex >= artists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);

        // Display edit options
        out.println("\nEditing artist: " + selectedArtist.getName());
        out.println("1. Edit name");
        out.println("2. Edit biography");
        out.println("0. Cancel");
        out.print("Your choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        switch (choice) {
            case 0:
                return; // User cancelled
            case 1:
                editArtistName(selectedArtist);
                break;
            case 2:
                editArtistBiography(selectedArtist);
                break;
            default:
                out.println("Invalid choice. Operation cancelled.");
                break;
        }
    }

    /**
     * Edit artist name
     */
    private void editArtistName(Artist artist) {
        out.println("\nCurrent name: " + artist.getName());
        out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            out.println("Artist name cannot be empty. Operation cancelled.");
            return;
        }

        // Check if name is unique
        List<Artist> existingArtists = service.searchArtistsByName(newName);
        if (!existingArtists.isEmpty() && !existingArtists.get(0).getId().equals(artist.getId())) {
            out.println("An artist with this name already exists. Please choose a different name.");
            return;
        }

        artist.setName(newName);
        out.println("Artist name updated successfully to '" + newName + "'.");
    }

    /**
     * Edit artist biography
     */
    private void editArtistBiography(Artist artist) {
        out.println("\nCurrent biography: " +
                (artist.getBiography().isEmpty() ? "[No biography]" : artist.getBiography()));
        out.println("Enter new biography (or leave empty to clear):");
        String newBiography = scanner.nextLine().trim();

        artist.setBiography(newBiography);

        if (newBiography.isEmpty()) {
            out.println("Artist biography cleared.");
        } else {
            out.println("Artist biography updated successfully.");
        }
    }

    /**
     * Handles editing album metadata
     */
    public void editAlbum() {
        out.println("\n========== EDIT ALBUM ==========");

        List<Album> albums = service.getAllAlbums();
        if (albums.isEmpty()) {
            out.println("No albums found in the collection.");
            return;
        }

        // Display available albums
        out.println("\nSelect an album to edit:");
        for (int i = 0; i < albums.size(); i++) {
            Album album = albums.get(i);
            String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";
            out.println((i + 1) + ". " + album.getName() + " by " + artistName + " (" + album.getReleaseYear() + ")");
        }

        // Get user selection
        out.print("\nEnter album number (or 0 to cancel): ");
        int albumIndex;
        try {
            albumIndex = Integer.parseInt(scanner.nextLine().trim());
            if (albumIndex == 0) {
                return; // User cancelled
            }
            albumIndex--; // Convert to 0-based index

            if (albumIndex < 0 || albumIndex >= albums.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Album selectedAlbum = albums.get(albumIndex);

        // Display edit options
        out.println("\nEditing album: " + selectedAlbum.getName());
        out.println("1. Edit name");
        out.println("2. Edit release year");
        out.println("3. Edit genre");
        out.println("4. Change artist");
        out.println("0. Cancel");
        out.print("Your choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        switch (choice) {
            case 0:
                return; // User cancelled
            case 1:
                editAlbumName(selectedAlbum);
                break;
            case 2:
                editAlbumReleaseYear(selectedAlbum);
                break;
            case 3:
                editAlbumGenre(selectedAlbum);
                break;
            case 4:
                changeAlbumArtist(selectedAlbum);
                break;
            default:
                out.println("Invalid choice. Operation cancelled.");
                break;
        }
    }

    /**
     * Edit album name
     */
    private void editAlbumName(Album album) {
        out.println("\nCurrent name: " + album.getName());
        out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            out.println("Album name cannot be empty. Operation cancelled.");
            return;
        }

        album.setName(newName);
        out.println("Album name updated successfully to '" + newName + "'.");
    }

    /**
     * Edit album release year
     */
    private void editAlbumReleaseYear(Album album) {
        out.println("\nCurrent release year: " + album.getReleaseYear());
        out.print("Enter new release year: ");

        try {
            int newYear = Integer.parseInt(scanner.nextLine().trim());
            if (newYear <= 0) {
                out.println("Release year must be a positive number. Operation cancelled.");
                return;
            }

            album.setReleaseYear(newYear);
            out.println("Album release year updated successfully to " + newYear + ".");
        } catch (NumberFormatException e) {
            out.println("Invalid input. Please enter a valid number. Operation cancelled.");
        }
    }

    /**
     * Edit album genre
     */
    private void editAlbumGenre(Album album) {
        out.println("\nCurrent genre: " + album.getGenre());
        out.print("Enter new genre: ");
        String newGenre = scanner.nextLine().trim();

        if (newGenre.isEmpty()) {
            newGenre = "Unknown";
        }

        album.setGenre(newGenre);
        out.println("Album genre updated successfully to '" + newGenre + "'.");
    }

    /**
     * Change album's artist
     */
    private void changeAlbumArtist(Album album) {
        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists found in the collection. Please add an artist first.");
            return;
        }

        out.println("\nCurrent artist: " + (album.getArtist() != null ? album.getArtist().getName() : "Unknown"));
        out.println("\nSelect a new artist:");

        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        out.print("\nEnter artist number (or 0 to cancel): ");
        int artistIndex;
        try {
            artistIndex = Integer.parseInt(scanner.nextLine().trim());
            if (artistIndex == 0) {
                return; // User cancelled
            }
            artistIndex--; // Convert to 0-based index

            if (artistIndex < 0 || artistIndex >= artists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);

        // Update album's artist
        album.setArtist(selectedArtist);
        out.println("Album artist updated successfully to '" + selectedArtist.getName() + "'.");
    }

    /**
     * Handles editing song metadata (genre)
     */
    public void editSongGenre() {
        out.println("\n========== EDIT SONG GENRE ==========");

        List<Song> songs = service.getAllSongs();
        if (songs.isEmpty()) {
            out.println("No songs found in the collection.");
            return;
        }

        // Display available songs
        out.println("\nSelect a song to edit:");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            out.println((i + 1) + ". " + song.getName() + " by " + artistName +
                    " (" + song.getFormattedDuration() + ") - Genre: " + song.getGenre());
        }

        // Get user selection
        out.print("\nEnter song number (or 0 to cancel): ");
        int songIndex;
        try {
            songIndex = Integer.parseInt(scanner.nextLine().trim());
            if (songIndex == 0) {
                return; // User cancelled
            }
            songIndex--; // Convert to 0-based index

            if (songIndex < 0 || songIndex >= songs.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Song selectedSong = songs.get(songIndex);

        // Edit genre
        out.println("\nCurrent genre: " + selectedSong.getGenre());
        out.print("Enter new genre: ");
        String newGenre = scanner.nextLine().trim();

        if (newGenre.isEmpty()) {
            newGenre = "Unknown";
        }

        selectedSong.setGenre(newGenre);
        out.println("Song genre updated successfully to '" + newGenre + "'.");

        // Ask if user wants to apply the same genre to all songs in the album
        if (selectedSong.getAlbum() != null) {
            out.println("\nDo you want to apply this genre to all songs in the album '" +
                    selectedSong.getAlbum().getName() + "'?");
            out.println("1. Yes");
            out.println("2. No");
            out.print("Your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 1) {
                    List<Song> albumSongs = service.getSongsByAlbum(selectedSong.getAlbum().getId());
                    for (Song song : albumSongs) {
                        song.setGenre(newGenre);
                    }
                    out.println("Genre updated for all songs in the album.");
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input, do nothing
            }
        }
    }
}