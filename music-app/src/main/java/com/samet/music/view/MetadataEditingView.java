package com.samet.music.view;

import java.util.List;
import java.util.Scanner;

import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for metadata editing operations
 */
public class MetadataEditingView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(MetadataEditingView.class);
    private final UserController userController;
    private final SongController songController;
    
    /**
     * Constructor
     * @param scanner Scanner object to read user input
     * @param userController User controller
     * @param songController Song controller
     */
    public MetadataEditingView(Scanner scanner, UserController userController, SongController songController) {
        super(scanner);
        this.userController = userController;
        this.songController = songController;
    }
    
    @Override
    public MenuView display() {
        if (!userController.isLoggedIn()) {
            logger.warn("No user logged in, returning to login menu");
            return new LoginMenuView(scanner, userController);
        }
        
        displayHeader("METADATA EDITING MENU");
        
        displayOption("1", "Edit Artist");
        displayOption("2", "Edit Album");
        displayOption("3", "Edit Song Genre");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    editArtist();
                    return this;
                case "2":
                    editAlbum();
                    return this;
                case "3":
                    editSongGenre();
                    return this;
                case "0":
                    return new MainMenuView(scanner, userController);
                default:
                    System.out.println("Invalid choice. Please try again.");
                    return this;
            }
        } catch (Exception e) {
            System.out.println("An error occurred. Please try again.");
            return this;
        }
    }
    
    /**
     * Edit artist for a song
     */
    private void editArtist() {
        List<Song> userSongs = songController.getUserSongs();
        
        if (userSongs.isEmpty()) {
            displayInfo("You don't have any songs to edit.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDIT ARTIST");
        
        // List songs
        System.out.println("\nYour songs:");
        int index = 1;
        for (Song song : userSongs) {
            System.out.printf("  %d. %s - %s%n", index++, song.getTitle(), song.getArtist());
        }
        
        // Ask for song title instead of number
        System.out.print("\nEnter the title of the song to edit artist (or '0' to cancel): ");
        String songTitle = scanner.nextLine().trim();
        
        if (songTitle.equals("0")) {
            return;
        }
        
        // Find the song by title
        Song selectedSong = null;
        for (Song song : userSongs) {
            if (song.getTitle().equalsIgnoreCase(songTitle)) {
                selectedSong = song;
                break;
            }
        }
        
        if (selectedSong == null) {
            System.out.println("Song not found. Please try again.");
            waitForEnter();
            return;
        }
        
        // Ask for new artist
        System.out.printf("\nCurrent artist: %s%n", selectedSong.getArtist());
        System.out.print("Enter new artist name (or '0' to cancel): ");
        String newArtist = scanner.nextLine().trim();
        
        if (newArtist.equals("0")) {
            System.out.println("Edit cancelled.");
            waitForEnter();
            return;
        }
        
        // Update the artist
        boolean updated = songController.updateSong(
            selectedSong.getId(),
            null,  // title (not changing)
            newArtist,
            null,  // album (not changing)
            null,  // genre (not changing)
            0      // year (not changing)
        );
        
        if (updated) {
            displaySuccess(String.format("Artist updated successfully. %s is now by %s", selectedSong.getTitle(), newArtist));
        } else {
            displayError("Failed to update artist. Please try again.");
        }
        
        waitForEnter();
    }
    
    /**
     * Edit album for a song
     */
    private void editAlbum() {
        List<Song> userSongs = songController.getUserSongs();
        
        if (userSongs.isEmpty()) {
            displayInfo("You don't have any songs to edit.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDIT ALBUM");
        
        // List songs
        System.out.println("\nYour songs:");
        int index = 1;
        for (Song song : userSongs) {
            System.out.printf("  %d. %s - %s (%s)%n", index++, song.getTitle(), song.getArtist(), song.getAlbum());
        }
        
        // Ask for song title instead of number
        System.out.print("\nEnter the title of the song to edit album (or '0' to cancel): ");
        String songTitle = scanner.nextLine().trim();
        
        if (songTitle.equals("0")) {
            return;
        }
        
        // Find the song by title
        Song selectedSong = null;
        for (Song song : userSongs) {
            if (song.getTitle().equalsIgnoreCase(songTitle)) {
                selectedSong = song;
                break;
            }
        }
        
        if (selectedSong == null) {
            System.out.println("Song not found. Please try again.");
            waitForEnter();
            return;
        }
        
        // Ask for new album
        System.out.printf("\nCurrent album: %s%n", selectedSong.getAlbum());
        System.out.print("Enter new album name (or '0' to cancel): ");
        String newAlbum = scanner.nextLine().trim();
        
        if (newAlbum.equals("0")) {
            System.out.println("Edit cancelled.");
            waitForEnter();
            return;
        }
        
        // Update the album
        boolean updated = songController.updateSong(
            selectedSong.getId(),
            null,  // title (not changing)
            null,  // artist (not changing)
            newAlbum,
            null,  // genre (not changing)
            0      // year (not changing)
        );
        
        if (updated) {
            displaySuccess(String.format("Album updated successfully. %s is now in the album %s", selectedSong.getTitle(), newAlbum));
        } else {
            displayError("Failed to update album. Please try again.");
        }
        
        waitForEnter();
    }
    
    /**
     * Edit genre for a song
     */
    private void editSongGenre() {
        List<Song> userSongs = songController.getUserSongs();
        
        if (userSongs.isEmpty()) {
            displayInfo("You don't have any songs to edit.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDIT SONG GENRE");
        
        // List songs
        System.out.println("\nYour songs:");
        int index = 1;
        for (Song song : userSongs) {
            System.out.printf("  %d. %s - %s (%s)%n", index++, song.getTitle(), song.getArtist(), song.getGenre());
        }
        
        // Ask for song title instead of number
        System.out.print("\nEnter the title of the song to edit genre (or '0' to cancel): ");
        String songTitle = scanner.nextLine().trim();
        
        if (songTitle.equals("0")) {
            return;
        }
        
        // Find the song by title
        Song selectedSong = null;
        for (Song song : userSongs) {
            if (song.getTitle().equalsIgnoreCase(songTitle)) {
                selectedSong = song;
                break;
            }
        }
        
        if (selectedSong == null) {
            System.out.println("Song not found. Please try again.");
            waitForEnter();
            return;
        }
        
        // Ask for new genre
        System.out.printf("\nCurrent genre: %s%n", selectedSong.getGenre());
        System.out.print("Enter new genre (or '0' to cancel): ");
        String newGenre = scanner.nextLine().trim();
        
        if (newGenre.equals("0")) {
            System.out.println("Edit cancelled.");
            waitForEnter();
            return;
        }
        
        // Update the genre
        boolean updated = songController.updateSong(
            selectedSong.getId(),
            null,  // title (not changing)
            null,  // artist (not changing)
            null,  // album (not changing)
            newGenre,
            0      // year (not changing)
        );
        
        if (updated) {
            displaySuccess(String.format("Genre updated successfully. %s genre is now %s", selectedSong.getTitle(), newGenre));
        } else {
            displayError("Failed to update genre. Please try again.");
        }
        
        waitForEnter();
    }
} 