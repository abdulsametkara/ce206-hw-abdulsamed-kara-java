package com.samet.music.view;

import java.util.List;
import java.util.Scanner;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for the song management menu
 */
public class SongMenuView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(SongMenuView.class);
    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     * @param userController the user controller
     * @param songController the song controller
     * @param playlistController the playlist controller
     */
    public SongMenuView(Scanner scanner, UserController userController, SongController songController, PlaylistController playlistController) {
        super(scanner);
        this.userController = userController;
        this.songController = songController;
        this.playlistController = playlistController;
    }
    
    @Override
    public MenuView display() {
        if (!userController.isLoggedIn()) {
            logger.warn("No user logged in, returning to login menu");
            return new LoginMenuView(scanner, userController);
        }
        
        displayHeader("MY SONGS");
        
        // Display all user songs
        List<Song> songs = songController.getUserSongs();
        
        if (songs.isEmpty()) {
            System.out.println("\nYou don't have any songs in your library yet.");
        } else {
            System.out.println("\nYour songs:");
            int index = 1;
            for (Song song : songs) {
                System.out.printf("  %d. %s - %s (%s) [%s]%n", 
                    index++, 
                    song.getTitle(), 
                    song.getArtist(), 
                    song.getAlbum(), 
                    song.getFormattedDuration());
            }
        }
        
        displayOption("1", "Add New Song");
        displayOption("2", "Edit Song");
        displayOption("3", "Delete Song");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                addSong();
                return this;
            case "2":
                editSong(songs);
                return this;
            case "3":
                deleteSong(songs);
                return this;
            case "0":
                return new MainMenuView(scanner, userController);
            default:
                System.out.println("Invalid choice. Please try again.");
                return this;
        }
    }
    
    /**
     * Add a new song to the library
     */
    private void addSong() {
        displayHeader("ADD NEW SONG");
        
        String title = getStringInput("Title");
        String artist = getStringInput("Artist");
        String album = getOptionalStringInput("Album");
        String genre = getOptionalStringInput("Genre");
        int year = getIntInput("Year (e.g. 2023)", 0);
        
        // Duration in format MM:SS
        System.out.println("Enter duration (in format MM:SS):");
        String durationStr = getStringInput("Duration");
        int minutes = 0;
        int seconds = 0;
        
        try {
            String[] parts = durationStr.split(":");
            if (parts.length == 2) {
                minutes = Integer.parseInt(parts[0]);
                seconds = Integer.parseInt(parts[1]);
            } else {
                seconds = Integer.parseInt(durationStr);
            }
        } catch (NumberFormatException e) {
            displayError("Invalid duration format. Using 0:00.");
            minutes = 0;
            seconds = 0;
        }
        
        int totalSeconds = minutes * 60 + seconds;
        
        // In a real application, you'd let the user browse for files
        // For this demo, we'll just ask for a path
        String filePath = getStringInput("File path (or enter 'mock' for a mock path)");
        
        if (filePath.equalsIgnoreCase("mock")) {
            filePath = "C:\\Music\\" + artist + " - " + title + ".mp3";
        }
        
        Song addedSong = songController.addSong(title, artist, album, genre, year, totalSeconds, filePath);
        
        if (addedSong != null) {
            displaySuccess("Song added to your library!");
        } else {
            displayError("Failed to add song.");
        }
        
        waitForEnter();
    }
    
    /**
     * Edit an existing song
     * @param songs the list of user's songs
     */
    private void editSong(List<Song> songs) {
        if (songs.isEmpty()) {
            displayInfo("You don't have any songs to edit.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDIT SONG");
        
        int choice = getIntInput("Enter the number of the song to edit", 1, songs.size());
        Song selectedSong = songs.get(choice - 1);
        
        System.out.println("\nEditing: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());
        System.out.println("(Leave fields blank to keep current values)");
        
        System.out.print("Title [" + selectedSong.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Artist [" + selectedSong.getArtist() + "]: ");
        String artist = scanner.nextLine().trim();
        
        System.out.print("Album [" + (selectedSong.getAlbum() != null ? selectedSong.getAlbum() : "") + "]: ");
        String album = scanner.nextLine().trim();
        
        System.out.print("Genre [" + (selectedSong.getGenre() != null ? selectedSong.getGenre() : "") + "]: ");
        String genre = scanner.nextLine().trim();
        
        System.out.print("Year [" + selectedSong.getYear() + "]: ");
        String yearStr = scanner.nextLine().trim();
        int year = 0;
        
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                displayError("Invalid year format. Keeping original value.");
            }
        }
        
        boolean updated = songController.updateSong(
            selectedSong.getId(),
            title.isEmpty() ? null : title,
            artist.isEmpty() ? null : artist,
            album.isEmpty() ? null : album,
            genre.isEmpty() ? null : genre,
            year
        );
        
        if (updated) {
            displaySuccess("Song updated successfully!");
        } else {
            displayError("Failed to update song.");
        }
        
        waitForEnter();
    }
    
    /**
     * Delete a song
     * @param songs the list of user's songs
     */
    private void deleteSong(List<Song> songs) {
        if (songs.isEmpty()) {
            displayInfo("You don't have any songs to delete.");
            waitForEnter();
            return;
        }
        
        displayHeader("DELETE SONG");
        
        int choice = getIntInput("Enter the number of the song to delete", 1, songs.size());
        Song selectedSong = songs.get(choice - 1);
        
        System.out.println("\nYou are about to delete: " + selectedSong.getTitle() + " - " + selectedSong.getArtist());
        
        if (getYesNoInput("Are you sure you want to delete this song?")) {
            boolean deleted = songController.deleteSong(selectedSong.getId());
            
            if (deleted) {
                displaySuccess("Song deleted successfully!");
            } else {
                displayError("Failed to delete song.");
            }
        } else {
            displayInfo("Deletion cancelled.");
        }
        
        waitForEnter();
    }
} 