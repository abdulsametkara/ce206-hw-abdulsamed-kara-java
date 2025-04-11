package com.samet.music.ui;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * UI handler for metadata editing operations
 */
public class MetadataEditingUI {
    private static final Logger logger = LoggerFactory.getLogger(MetadataEditingUI.class);

    Scanner scanner;
    public final PrintStream out;
    public MusicCollectionService service;

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

    void editArtistName(Artist artist) {
        out.println("\nCurrent name: " + artist.getName());
        out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            out.println("Artist name cannot be empty. Operation cancelled.");
            return;
        }

        // Güncelleme öncesi artist ID'sini kaydet
        String artistId = artist.getId();
        System.out.println("Updating artist with ID: " + artistId);

        // Sanatçı adını değiştir
        artist.setName(newName);

        // ArtistDAO'yu kullanarak veritabanını güncelle
        ArtistDAO artistDAO = DAOFactory.getInstance().getArtistDAO();
        artistDAO.update(artist);

        out.println("Artist name updated successfully to '" + newName + "'.");

        // Değişikliğin etkili olup olmadığını kontrol et
        Artist updatedArtist = artistDAO.getById(artistId);
        if (updatedArtist != null) {
            out.println("Verified: Artist with ID " + artistId + " now has name: " + updatedArtist.getName());
        }
    }

    /**
     * Edit artist biography
     */
    void editArtistBiography(Artist artist) {
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

    public void editSongGenre() {
        System.out.println("\n========== EDIT SONG GENRE ==========");
        List<Song> songs = service.getAllSongs();

        if (songs.isEmpty()) {
            System.out.println("No songs in the collection.");
            return;
        }

        System.out.println("Select a song to edit:");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            String artist = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            int minutes = song.getDuration() / 60;
            int seconds = song.getDuration() % 60;
            System.out.printf("%d. %s by %s (%d:%02d) - Genre: %s\n",
                    i + 1, song.getName(), artist, minutes, seconds, song.getGenre());
        }

        System.out.print("Enter song number (or 0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice <= 0 || choice > songs.size()) {
            return;
        }

        Song selectedSong = songs.get(choice - 1);
        System.out.println("Current genre: " + selectedSong.getGenre());
        System.out.print("Enter new genre: ");
        String newGenre = scanner.nextLine();

        // Update genre
        selectedSong.setGenre(newGenre);

        // Update in database - Burada doğrudan DAO'yu kullanıyoruz
        boolean success = DAOFactory.getInstance().getSongDAO().update(selectedSong);

        if (success) {
            System.out.println("Song genre updated successfully to '" + newGenre + "'.");
        } else {
            System.out.println("Failed to update song genre.");
        }
    }

    private void updateAlbumGenre(Album album) {
        System.out.println("Current genre: " + album.getGenre());
        System.out.print("Enter new genre: ");
        String newGenre = scanner.nextLine();

        // Update genre
        album.setGenre(newGenre);

        // Update in database - Burada doğrudan DAO'yu kullanıyoruz
        boolean success = DAOFactory.getInstance().getAlbumDAO().update(album);

        if (success) {
            System.out.println("Album genre updated successfully to '" + newGenre + "'.");
        } else {
            System.out.println("Failed to update album genre.");
        }
    }
}