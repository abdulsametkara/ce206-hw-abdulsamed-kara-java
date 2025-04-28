package com.samet.music.view;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.Album;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for music recommendations
 */
public class RecommendationView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationView.class);
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
    public RecommendationView(Scanner scanner, UserController userController, SongController songController, PlaylistController playlistController) {
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
        
        displayHeader("RECOMMENDATIONS MENU");
        
        displayOption("1", "Get Song Recommendations");
        displayOption("2", "Get Album Recommendations");
        displayOption("3", "Get Artist Recommendations");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    getSongRecommendations();
                    return this;
                case "2":
                    getAlbumRecommendations();
                    return this;
                case "3":
                    getArtistRecommendations();
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
     * Get song recommendations
     */
    private void getSongRecommendations() {
        displayHeader("SONG RECOMMENDATIONS");
        
        List<Song> userSongs = null;
        try {
            userSongs = songController.getUserSongs();
        } catch (Exception e) {
            userSongs = new ArrayList<>();
        }
        
        if (userSongs.isEmpty()) {
            System.out.println("\nNo songs found in your library. Add some songs to get recommendations.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nSongs in your collection:");
        
        int index = 1;
        for (Song song : userSongs) {
            System.out.printf("  %d. %s - %s (%s)%n", 
                index++, 
                song.getTitle(), 
                song.getArtist(), 
                song.getGenre() != null ? song.getGenre() : "Unknown");
        }
        
        // Group songs by genre
        Map<String, List<Song>> songsByGenre = userSongs.stream()
            .filter(song -> song.getGenre() != null && !song.getGenre().isEmpty())
            .collect(Collectors.groupingBy(Song::getGenre));
        
        if (!songsByGenre.isEmpty()) {
            System.out.println("\nSongs by genre:");
            for (Map.Entry<String, List<Song>> entry : songsByGenre.entrySet()) {
                System.out.println("  • " + entry.getKey() + ": " + 
                    entry.getValue().stream()
                        .map(Song::getTitle)
                        .limit(3) // Show only first 3 songs for each genre
                        .collect(Collectors.joining(", ")));
            }
        }
        
        if (getYesNoInput("\nWould you like to add any of these songs to a playlist?")) {
            addSongsToPlaylist(userSongs);
        }
        
        waitForEnter();
    }
    
    /**
     * Get album recommendations
     */
    private void getAlbumRecommendations() {
        displayHeader("ALBUM RECOMMENDATIONS");
        
        List<Album> userAlbums = null;
        try {
            userAlbums = songController.getUserAlbums();
        } catch (Exception e) {
            userAlbums = new ArrayList<>();
        }
        
        if (userAlbums.isEmpty()) {
            System.out.println("\nNo albums found in your library. Add some albums to get recommendations.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nBased on your music collection:");
        
        int index = 1;
        for (Album album : userAlbums) {
            System.out.printf("  %d. %s by %s (%s)%n", 
                index++, 
                album.getTitle(), 
                album.getArtist(), 
                album.getGenre() != null ? album.getGenre() : "Unknown");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generate album recommendations based on user's existing albums
     * @param userAlbums the user's existing albums
     * @return a list of recommended albums
     */
    private List<Album> generateAlbumRecommendations(List<Album> userAlbums) {
        // This method is no longer used since we're showing actual user content
        return new ArrayList<>();
    }
    
    /**
     * Get artist recommendations
     */
    private void getArtistRecommendations() {
        displayHeader("ARTIST RECOMMENDATIONS");
        
        List<String> userArtists = null;
        try {
            userArtists = songController.getArtists();
        } catch (Exception e) {
            userArtists = new ArrayList<>();
        }
        
        if (userArtists.isEmpty()) {
            System.out.println("\nNo artists found in your library. Add some artists to get recommendations.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nArtists in your collection:");
        
        int index = 1;
        for (String artist : userArtists) {
            System.out.printf("  %d. %s%n", index++, artist);
        }
        
        // Show songs by each artist
        System.out.println("\nSongs by these artists:");
        for (String artist : userArtists) {
            List<Song> artistSongs = songController.getSongsByArtist(artist);
            if (!artistSongs.isEmpty()) {
                System.out.println("  • " + artist + " - " + 
                    artistSongs.stream()
                        .map(Song::getTitle)
                        .limit(2) // Show only first 2 songs for each artist
                        .collect(Collectors.joining(", ")));
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generate artist recommendations based on user's existing artists
     * @param userArtists the user's existing artists
     * @return a list of recommended artists
     */
    private List<String> generateArtistRecommendations(List<String> userArtists) {
        // This method is no longer used since we're showing actual user content
        return new ArrayList<>();
    }
    
    /**
     * Add recommended songs to a playlist
     * @param recommendations the list of recommended songs
     */
    private void addSongsToPlaylist(List<Song> recommendations) {
        List<Playlist> playlists = null;
        try {
            playlists = playlistController.getUserPlaylists();
        } catch (Exception e) {
            playlists = new ArrayList<>();
        }
        
        if (playlists.isEmpty()) {
            displayInfo("You don't have any playlists yet. Please create a playlist first.");
            
            if (getYesNoInput("Would you like to create a new playlist now?")) {
                String name = getStringInput("Playlist name");
                String description = getOptionalStringInput("Description");
                
                Playlist createdPlaylist = null;
                try {
                    createdPlaylist = playlistController.createPlaylist(name, description);
                } catch (Exception e) {
                    displayError("Failed to create playlist due to a system error.");
                    waitForEnter();
                    return;
                }
                
                if (createdPlaylist != null) {
                    displaySuccess("Playlist created successfully!");
                    try {
                        playlists = playlistController.getUserPlaylists(); // Refresh list
                    } catch (Exception e) {
                        playlists = new ArrayList<>();
                        playlists.add(createdPlaylist);
                    }
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
        
        displayHeader("ADD RECOMMENDATIONS TO PLAYLIST");
        
        System.out.println("\nYour playlists:");
        int index = 1;
        for (Playlist playlist : playlists) {
            System.out.printf("  %d. %s%n", index++, playlist.getName());
        }
        
        // Ask for playlist name instead of number
        System.out.print("\nEnter the name of the playlist to add songs to (or '0' to cancel): ");
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
        
        System.out.println("\nRecommended songs:");
        index = 1;
        for (Song song : recommendations) {
            System.out.printf("  %d. %s - %s%n", index++, song.getTitle(), song.getArtist());
        }
        
        // Ask for song title instead of number
        System.out.print("\nEnter the title of a song to add (or 'all' for all songs, '0' to cancel): ");
        String input = scanner.nextLine().trim();
        
        if (input.equals("0")) {
            System.out.println("Operation cancelled.");
            waitForEnter();
            return;
        }
        
        if (input.equalsIgnoreCase("all")) {
            // Add all recommendations to playlist
            int addedCount = 0;
            for (Song song : recommendations) {
                boolean added = playlistController.addSongToPlaylist(selectedPlaylist.getId(), song.getId());
                if (added) {
                    addedCount++;
                }
            }
            displaySuccess("Added " + addedCount + " recommended songs to playlist: " + selectedPlaylist.getName());
        } else {
            // Find song by title
            boolean songFound = false;
            for (Song song : recommendations) {
                if (song.getTitle().equalsIgnoreCase(input)) {
                    boolean added = playlistController.addSongToPlaylist(selectedPlaylist.getId(), song.getId());
                    if (added) {
                        displaySuccess("Song \"" + song.getTitle() + "\" added to playlist: " + selectedPlaylist.getName());
                        songFound = true;
                        break;
                    }
                }
            }
            
            if (!songFound) {
                displayError("Song not found. Please try again.");
            }
        }
        
        waitForEnter();
    }
} 