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
 * View class for the search menu
 */
public class SearchMenuView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(SearchMenuView.class);
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
    public SearchMenuView(Scanner scanner, UserController userController, SongController songController, PlaylistController playlistController) {
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
        
        displayHeader("SEARCH MUSIC");
        
        System.out.println("\nSearch your music library for songs, artists, albums, or genres.");
        System.out.print("Enter search query (or leave blank to go back): ");
        
        String query = scanner.nextLine().trim();
        
        if (query.isEmpty()) {
            return new MainMenuView(scanner, userController);
        }
        
        List<Song> searchResults = songController.searchSongs(query);
        
        displaySearchResults(query, searchResults);
        
        if (!searchResults.isEmpty()) {
            displayOption("1", "Add Songs to Playlist");
            displayOption("2", "Edit Song");
            displayOption("3", "Delete Song");
        }
        
        displayOption("4", "New Search");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                if (!searchResults.isEmpty()) {
                    addSongsToPlaylist(searchResults);
                } else {
                    displayInfo("No search results to add to playlist.");
                    waitForEnter();
                }
                return this;
            case "2":
                if (!searchResults.isEmpty()) {
                    editSong(searchResults);
                } else {
                    displayInfo("No search results to edit.");
                    waitForEnter();
                }
                return this;
            case "3":
                if (!searchResults.isEmpty()) {
                    deleteSong(searchResults);
                } else {
                    displayInfo("No search results to delete.");
                    waitForEnter();
                }
                return this;
            case "4":
                return this; // New search
            case "0":
                return new MainMenuView(scanner, userController);
            default:
                System.out.println("Invalid choice. Please try again.");
                return this;
        }
    }
    
    /**
     * Display search results
     * @param query the search query
     * @param searchResults the list of search results
     */
    private void displaySearchResults(String query, List<Song> searchResults) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" SEARCH RESULTS FOR: " + query);
        System.out.println("=".repeat(50));
        
        if (searchResults.isEmpty()) {
            System.out.println("\nNo results found for your search query.");
            return;
        }
        
        System.out.println("\nFound " + searchResults.size() + " matching songs:");
        
        int index = 1;
        for (Song song : searchResults) {
            System.out.printf("  %d. %s - %s (%s) [%s]%n", 
                index++, 
                song.getTitle(), 
                song.getArtist(), 
                song.getAlbum(), 
                song.getFormattedDuration());
        }
    }
    
    /**
     * Add songs from search results to a playlist
     * @param searchResults the list of search results
     */
    private void addSongsToPlaylist(List<Song> searchResults) {
        List<Playlist> playlists = playlistController.getUserPlaylists();
        
        if (playlists.isEmpty()) {
            displayInfo("You don't have any playlists yet. Please create a playlist first.");
            
            if (getYesNoInput("Would you like to create a new playlist now?")) {
                String name = getStringInput("Playlist name");
                String description = getOptionalStringInput("Description");
                
                Playlist createdPlaylist = playlistController.createPlaylist(name, description);
                
                if (createdPlaylist != null) {
                    displaySuccess("Playlist created successfully!");
                    playlists = playlistController.getUserPlaylists(); // Refresh list
                } else {
                    displayError("Failed to create playlist.");
                    waitForEnter();
                    return;
                }
            } else {
                waitForEnter();
                return;
            }
        }
        
        displayHeader("ADD TO PLAYLIST");
        
        System.out.println("\nYour playlists:");
        int index = 1;
        for (Playlist playlist : playlists) {
            System.out.printf("  %d. %s (%d songs)%n", 
                index++, 
                playlist.getName(), 
                playlist.getSongCount());
        }
        
        int playlistChoice = getIntInput("Select a playlist to add songs to", 1, playlists.size());
        Playlist selectedPlaylist = playlists.get(playlistChoice - 1);
        
        System.out.println("\nYour search results:");
        index = 1;
        for (Song song : searchResults) {
            System.out.printf("  %d. %s - %s%n", 
                index++, 
                song.getTitle(), 
                song.getArtist());
        }
        
        System.out.println("\nEnter song numbers to add (comma-separated, e.g. 1,3,5), or 'all' for all songs:");
        String input = scanner.nextLine().trim();
        
        if (input.equalsIgnoreCase("all")) {
            // Add all songs to playlist
            for (Song song : searchResults) {
                playlistController.addSongToPlaylist(selectedPlaylist.getId(), song.getId());
            }
            displaySuccess("Added all songs to playlist: " + selectedPlaylist.getName());
        } else {
            // Add selected songs
            String[] selections = input.split(",");
            int addedCount = 0;
            
            for (String selection : selections) {
                try {
                    int songIndex = Integer.parseInt(selection.trim()) - 1;
                    if (songIndex >= 0 && songIndex < searchResults.size()) {
                        Song song = searchResults.get(songIndex);
                        boolean added = playlistController.addSongToPlaylist(selectedPlaylist.getId(), song.getId());
                        if (added) {
                            addedCount++;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Just skip invalid input
                }
            }
            
            displaySuccess("Added " + addedCount + " song(s) to playlist: " + selectedPlaylist.getName());
        }
        
        waitForEnter();
    }
    
    /**
     * Edit a song from search results
     * @param searchResults the list of search results
     */
    private void editSong(List<Song> searchResults) {
        displayHeader("EDIT SONG");
        
        int choice = getIntInput("Enter the number of the song to edit", 1, searchResults.size());
        Song selectedSong = searchResults.get(choice - 1);
        
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
     * Delete a song from search results
     * @param searchResults the list of search results
     */
    private void deleteSong(List<Song> searchResults) {
        displayHeader("DELETE SONG");
        
        int choice = getIntInput("Enter the number of the song to delete", 1, searchResults.size());
        Song selectedSong = searchResults.get(choice - 1);
        
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