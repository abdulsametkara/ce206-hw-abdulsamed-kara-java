package com.samet.music.view;

import java.util.List;
import java.util.Scanner;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for the playlist management menu
 */
public class PlaylistMenuView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistMenuView.class);
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
    public PlaylistMenuView(Scanner scanner, UserController userController, SongController songController, PlaylistController playlistController) {
        super(scanner);
        this.userController = userController;
        this.songController = songController;
        this.playlistController = playlistController;
    }
    
    @Override
    public MenuView display() {
        if (!userController.isLoggedIn()) {
            return new LoginMenuView(scanner, userController);
        }
        
        displayHeader("PLAYLISTS MENU");
        
        displayOption("1", "Create Playlist");
        displayOption("2", "View Playlists");
        displayOption("3", "Edit Playlist");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    createPlaylist();
                    return this;
                case "2":
                    viewPlaylists();
                    return this;
                case "3":
                    editPlaylist();
                    return this;
                case "0":
                    return new MainMenuView(scanner, userController);
                default:
                    System.out.println("Invalid choice. Please try again.");
                    return this;
            }
        } catch (Exception e) {
            // Handle exceptions silently without showing error to user
            System.out.println("An operation could not be completed. Please try again.");
            return this;
        }
    }
    
    /**
     * Create a new playlist
     */
    private void createPlaylist() {
        displayHeader("CREATE PLAYLIST");
        
        String name = getStringInput("Playlist name");
        String description = getOptionalStringInput("Description");
        
        try {
            Playlist createdPlaylist = playlistController.createPlaylist(name, description);
            
            if (createdPlaylist != null) {
                displaySuccess("Playlist created successfully!");
                
                // Ask if user wants to add songs to the playlist
                if (getYesNoInput("Would you like to add songs to this playlist now?")) {
                    addSongsToPlaylist(createdPlaylist);
                }
            } else {
                displayError("Failed to create playlist.");
            }
        } catch (Exception e) {
            displayError("Failed to create playlist due to a system error.");
        }
        
        waitForEnter();
    }
    
    /**
     * View all playlists
     */
    private void viewPlaylists() {
        List<Playlist> playlists = null;
        try {
            playlists = playlistController.getUserPlaylists();
        } catch (Exception e) {
            // Silently catch any exceptions and show empty list
            playlists = List.of();
        }
        
        displayHeader("YOUR PLAYLISTS");
        
        if (playlists.isEmpty()) {
            System.out.println("\nYou don't have any playlists yet.");
            waitForEnter();
            return;
        }
        
        // Display all playlists without detailed information
        System.out.println("\nYour playlists:");
        int index = 1;
        for (Playlist playlist : playlists) {
            System.out.printf("  %d. %s%n", index++, playlist.getName());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Edit a playlist
     */
    private void editPlaylist() {
        List<Playlist> playlists = null;
        try {
            playlists = playlistController.getUserPlaylists();
        } catch (Exception e) {
            // Silently catch any exceptions and show empty list
            playlists = List.of();
        }
        
        if (playlists.isEmpty()) {
            displayInfo("You don't have any playlists to edit.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDIT PLAYLIST");
        
        // Display all playlists
        System.out.println("\nYour playlists:");
        for (int i = 0; i < playlists.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, playlists.get(i).getName());
        }
        
        // Ask for playlist name instead of number
        System.out.print("\nEnter the name of the playlist to edit (or '0' to go back): ");
        String playlistName = scanner.nextLine().trim();
        
        if (playlistName.equals("0")) {
            return;
        }
        
        // Find playlist by name
        Playlist selectedPlaylist = null;
        for (Playlist p : playlists) {
            if (p.getName().equalsIgnoreCase(playlistName)) {
                selectedPlaylist = p;
                break;
            }
        }
        
        if (selectedPlaylist == null) {
            System.out.println("Playlist not found. Please try again.");
            waitForEnter();
            return;
        }
        
        displayHeader("EDITING PLAYLIST: " + selectedPlaylist.getName());
        
        displayOption("1", "Change Name/Description");
        displayOption("2", "Add Songs");
        displayOption("3", "Remove Songs");
        displayOption("4", "Delete Playlist");
        displayOption("0", "Back to Playlists Menu");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String menuChoice = scanner.nextLine().trim();
        
        try {
            switch (menuChoice) {
                case "1":
                    changePlaylistDetails(selectedPlaylist);
                    break;
                case "2":
                    addSongsToPlaylist(selectedPlaylist);
                    break;
                case "3":
                    removeSongsFromPlaylist(selectedPlaylist);
                    break;
                case "4":
                    deletePlaylist(selectedPlaylist);
                    break;
                case "0":
                default:
                    // Just return to playlist menu
                    break;
            }
        } catch (Exception e) {
            displayError("Operation could not be completed. Please try again.");
            waitForEnter();
        }
    }
    
    /**
     * Change playlist name and description
     * @param playlist the playlist to change
     */
    private void changePlaylistDetails(Playlist playlist) {
        displayHeader("CHANGE PLAYLIST DETAILS");
        
        System.out.println("\nCurrent name: " + playlist.getName());
        System.out.println("Current description: " + (playlist.getDescription() != null ? playlist.getDescription() : ""));
        
        System.out.println("\n(Leave fields blank to keep current values)");
        
        System.out.print("New name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("New description: ");
        String description = scanner.nextLine().trim();
        
        boolean updated = playlistController.updatePlaylist(
            playlist.getId(),
            name.isEmpty() ? null : name,
            description.isEmpty() ? null : description
        );
        
        if (updated) {
            displaySuccess("Playlist details updated successfully!");
        } else {
            displayError("Failed to update playlist details.");
        }
        
        waitForEnter();
    }
    
    /**
     * Delete a playlist
     * @param playlist the playlist to delete
     */
    private void deletePlaylist(Playlist playlist) {
        displayHeader("DELETE PLAYLIST");
        
        System.out.println("\nYou are about to delete playlist: " + playlist.getName());
        System.out.println("This playlist contains " + playlist.getSongCount() + " songs.");
        System.out.println("Note: The songs themselves will not be deleted from your library.");
        
        if (getYesNoInput("Are you sure you want to delete this playlist?")) {
            boolean deleted = playlistController.deletePlaylist(playlist.getId());
            
            if (deleted) {
                displaySuccess("Playlist deleted successfully!");
            } else {
                displayError("Failed to delete playlist.");
            }
        } else {
            displayInfo("Deletion cancelled.");
        }
        
        waitForEnter();
    }
    
    /**
     * Add songs to a playlist
     * @param playlist the playlist to add songs to
     */
    private void addSongsToPlaylist(Playlist playlist) {
        List<Song> userSongs = songController.getUserSongs();
        
        if (userSongs.isEmpty()) {
            displayInfo("You don't have any songs in your library to add to the playlist.");
            waitForEnter();
            return;
        }
        
        displayHeader("ADD SONGS TO PLAYLIST: " + playlist.getName());
        
        System.out.println("\nYour songs:");
        for (int i = 0; i < userSongs.size(); i++) {
            Song song = userSongs.get(i);
            System.out.printf("  %d. %s - %s%n", 
                i + 1, 
                song.getTitle(), 
                song.getArtist());
        }
        
        System.out.print("\nEnter song title to add (or 'all' for all songs): ");
        String input = scanner.nextLine().trim();
        
        if (input.equalsIgnoreCase("all")) {
            // Add all songs to playlist
            for (Song song : userSongs) {
                playlistController.addSongToPlaylist(playlist.getId(), song.getId());
            }
            displaySuccess("Added all songs to playlist!");
        } else {
            // Find song by title
            int addedCount = 0;
            for (Song song : userSongs) {
                if (song.getTitle().equalsIgnoreCase(input)) {
                    boolean added = playlistController.addSongToPlaylist(playlist.getId(), song.getId());
                    if (added) {
                        addedCount++;
                        displaySuccess("Song \"" + song.getTitle() + "\" added to playlist!");
                        break;
                    }
                }
            }
            
            if (addedCount == 0) {
                displayError("Song not found. Please try again.");
            }
        }
        
        waitForEnter();
    }
    
    /**
     * Remove songs from a playlist
     * @param playlist the playlist to remove songs from
     */
    private void removeSongsFromPlaylist(Playlist playlist) {
        List<Song> playlistSongs = playlist.getSongs();
        
        if (playlistSongs.isEmpty()) {
            displayInfo("This playlist doesn't have any songs to remove.");
            waitForEnter();
            return;
        }
        
        displayHeader("REMOVE SONGS FROM PLAYLIST: " + playlist.getName());
        
        System.out.println("\nSongs in this playlist:");
        for (int i = 0; i < playlistSongs.size(); i++) {
            Song song = playlistSongs.get(i);
            System.out.printf("  %d. %s - %s%n", 
                i + 1, 
                song.getTitle(), 
                song.getArtist());
        }
        
        System.out.print("\nEnter song title to remove (or 'all' to clear playlist): ");
        String input = scanner.nextLine().trim();
        
        if (input.equalsIgnoreCase("all")) {
            // Clear the playlist
            for (Song song : playlistSongs) {
                playlistController.removeSongFromPlaylist(playlist.getId(), song.getId());
            }
            displaySuccess("Removed all songs from playlist!");
        } else {
            // Find song by title
            int removedCount = 0;
            for (Song song : playlistSongs) {
                if (song.getTitle().equalsIgnoreCase(input)) {
                    boolean removed = playlistController.removeSongFromPlaylist(playlist.getId(), song.getId());
                    if (removed) {
                        removedCount++;
                        displaySuccess("Song \"" + song.getTitle() + "\" removed from playlist!");
                        break;
                    }
                }
            }
            
            if (removedCount == 0) {
                displayError("Song not found in playlist. Please try again.");
            }
        }
        
        waitForEnter();
    }
} 