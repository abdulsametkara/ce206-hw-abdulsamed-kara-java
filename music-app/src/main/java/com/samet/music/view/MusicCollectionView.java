package com.samet.music.view;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.controller.AlbumController;
import com.samet.music.controller.ArtistController;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Song;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.util.TimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View class for the music collection management
 */
public class MusicCollectionView extends MenuView {
    private static final Logger logger = LoggerFactory.getLogger(MusicCollectionView.class);
    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    private AlbumController albumController;
    private ArtistController artistController;
    
    /**
     * Constructor
     * @param scanner shared scanner for user input
     * @param userController the user controller
     * @param songController the song controller
     * @param playlistController the playlist controller
     */
    public MusicCollectionView(Scanner scanner, UserController userController, SongController songController, PlaylistController playlistController) {
        super(scanner);
        this.userController = userController;
        this.songController = songController;
        this.playlistController = playlistController;
        
        // Yeni controller'ları mevcut userController ile oluştur
        this.albumController = new AlbumController(new AlbumDAO(), new SongDAO(), userController);
        this.artistController = new ArtistController(new ArtistDAO(), new SongDAO(), new AlbumDAO(), userController);
    }
    
    @Override
    public MenuView display() {
        if (!userController.isLoggedIn()) {
            System.out.println("You must be logged in to access your music collection.");
            System.out.println("Redirecting to login menu...");
            waitForEnter();
            return new LoginMenuView(scanner, userController);
        }
        
        displayHeader("MUSIC COLLECTION MENU");
        
        displayOption("1", "Add Song");
        displayOption("2", "Add Album");
        displayOption("3", "Add Artist");
        displayOption("4", "View Songs");
        displayOption("5", "View Albums");
        displayOption("6", "View Artists");
        displayOption("7", "Delete Song");
        displayOption("8", "Delete Album");
        displayOption("9", "Delete Artist");
        displayOption("10", "Add Song to Album");
        displayOption("0", "Back to Main Menu");
        
        displayFooter();
        
        System.out.print("Please enter your choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                addSong();
                return this;
            case "2":
                addAlbum();
                return this;
            case "3":
                addArtist();
                return this;
            case "4":
                viewSongs();
                return this;
            case "5":
                viewAlbums();
                return this;
            case "6":
                viewArtists();
                return this;
            case "7":
                deleteSong();
                return this;
            case "8":
                // Albüm silme - albüm listesini göster ve isim olarak seçim yap
                List<Album> albums = albumController.getAlbumsByUserId(userController.getCurrentUser().getId());
                if (albums.isEmpty()) {
                    displayInfo("No albums in your library.");
                    waitForEnter();
                    return this;
                }
                
                System.out.println("\nYour albums:");
                int index = 1;
                for (Album album : albums) {
                    System.out.printf("  %d. %s - %s%n", 
                        index++, 
                        album.getTitle(), 
                        album.getArtist());
                }
                
                String albumTitle = getStringInput("Enter album title to delete");
                String albumArtist = getStringInput("Enter album artist");
                
                // Find the album with matching title and artist
                Album selectedAlbum = null;
                for (Album album : albums) {
                    if (album.getTitle().equalsIgnoreCase(albumTitle) && 
                        album.getArtist().equalsIgnoreCase(albumArtist)) {
                        selectedAlbum = album;
                        break;
                    }
                }
                
                if (selectedAlbum == null) {
                    displayError("Album not found: " + albumTitle + " by " + albumArtist);
                    waitForEnter();
                    return this;
                }
                
                deleteAlbum(selectedAlbum);
                return this;
            case "9":
                deleteArtistMenu();
                return this;
            case "10":
                addSongToAlbumMenu();
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
        System.out.println("=== ADD NEW SONG ===");
        
        System.out.print("Enter song title: ");
        String title = scanner.nextLine();
        
        System.out.print("Enter artist name: ");
        String artist = scanner.nextLine();
        
        // Check if artist exists
        if (!songController.addArtist(artist)) {
            System.out.println("Failed to add artist. Song cannot be added.");
            return;
        }
        
        System.out.print("Enter album name: ");
        String album = scanner.nextLine();
        
        System.out.print("Enter genre: ");
        String genre = scanner.nextLine();
        
        System.out.print("Enter release year: ");
        int year = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Enter duration (MM:SS): ");
        String durationStr = scanner.nextLine();
        int duration = TimeFormatter.parseDuration(durationStr);
        
        // Generate file path based on artist and title
        String filePath = "music/" + artist + " - " + title + ".mp3";
        
        Song addedSong = songController.addSong(title, artist, album, genre, year, duration, filePath);
        
        if (addedSong != null) {
            System.out.println("Song added successfully!");
        } else {
            System.out.println("Failed to add the song.");
        }
    }
    
    /**
     * Sanatçının var olup olmadığını kontrol eder
     */
    private boolean artistExists(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            return false;
        }
        
        return artistController.artistExists(artist);
    }
    
    /**
     * Add a new artist to the library
     */
    private void addArtist() {
        displayHeader("ADD NEW ARTIST");
        
        String artistName = getStringInput("Artist name");
        if (artistName == null || artistName.trim().isEmpty()) {
            displayError("Artist name cannot be empty.");
            waitForEnter();
            return;
        }
        
        // Bio is optional, test only provides artist name
        String bio = "";
        
        Artist artist = artistController.addArtist(artistName, bio);
        
        if (artist != null) {
            displaySuccess("Artist '" + artistName + "' added to your library!");
        } else {
            displayError("Failed to add artist.");
        }
        
        waitForEnter();
    }
    
    /**
     * Belirtilen sanatçıyı sessiz modda ekle (log çıktısı olmadan)
     */
    private void addArtist(String artistName) {
        try {
        Artist artist = artistController.addArtist(artistName, "");
        
        if (artist != null) {
            displaySuccess("Artist '" + artistName + "' added to your library!");
        } else {
            displayError("Failed to add artist.");
        }
        
        waitForEnter();
        } catch (Exception e) {
            displayError("Failed to add artist: " + e.getMessage());
            waitForEnter();
        }
    }
    
    /**
     * View all songs in the library
     */
    private void viewSongs() {
        // Display all user songs
        List<Song> songs = songController.getUserSongs();
        
        displayHeader("YOUR SONGS");
        
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
        
        waitForEnter();
    }
    
    /**
     * Delete a song from the library
     */
    private void deleteSong() {
        List<Song> songs = songController.getUserSongs();
        
        if (songs.isEmpty()) {
            displayInfo("You don't have any songs to delete.");
            waitForEnter();
            return;
        }
        
        displayHeader("DELETE SONG");
        
        // Display all songs first
        System.out.println("\nYour songs:");
        int index = 1;
        for (Song song : songs) {
            System.out.printf("  %d. %s - %s (%s)%n", 
                index++, 
                song.getTitle(), 
                song.getArtist(), 
                song.getAlbum());
        }
        
        String title = getStringInput("Enter the title of the song to delete");
        String artist = getStringInput("Enter the artist of the song");
        
        // Find the song with matching title and artist
        Song selectedSong = null;
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(title) && song.getArtist().equalsIgnoreCase(artist)) {
                selectedSong = song;
                break;
            }
        }
        
        if (selectedSong == null) {
            displayError("Song not found: " + title + " by " + artist);
            waitForEnter();
            return;
        }
        
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
    
    /**
     * Add a new album to the library
     */
    private void addAlbum() {
        displayHeader("ADD NEW ALBUM");
        
        String title = getStringInput("Album title");
        String artist = getStringInput("Artist name");
        
        // Check if artist exists
        if (!artistExists(artist)) {
            displayError("Artist does not exist in your library.");
            System.out.println("Album creation canceled.");
                waitForEnter();
                return;
        }
        
        int year = getIntInput("Release year");
        String genre = getStringInput("Genre");
        
        Album album = new Album(title, artist, year, genre, userController.getCurrentUser().getId());
        boolean success = albumController.createAlbum(album);
        
        if (success) {
            displaySuccess("Album added to your library!");
        } else {
            displayError("Failed to add album.");
        }
        
        waitForEnter();
    }
    
    /**
     * View all albums in the library
     */
    private void viewAlbums() {
        displayHeader("YOUR ALBUMS");
        
        List<Album> albums = albumController.getAlbumsByUserId(userController.getCurrentUser().getId());
        
        if (albums.isEmpty()) {
            displayInfo("No albums in your library.");
            waitForEnter();
            return;
        }
        
            System.out.println("\nYour albums:");
            int index = 1;
            for (Album album : albums) {
            System.out.printf("  %d. %s - %s%n", 
                    index++,
                    album.getTitle(), 
                album.getArtist());
            }
            
        if (getYesNoInput("\nWould you like to view album details?")) {
                String albumTitle = getStringInput("Enter album title");
            String albumArtist = getStringInput("Enter album artist");
                
                // Find the album with matching title and artist
                Album selectedAlbum = null;
                for (Album album : albums) {
                    if (album.getTitle().equalsIgnoreCase(albumTitle) && 
                    album.getArtist().equalsIgnoreCase(albumArtist)) {
                        selectedAlbum = album;
                        break;
                    }
                }
                
            if (selectedAlbum == null) {
                displayError("Album not found: " + albumTitle + " by " + albumArtist);
                waitForEnter();
                return;
        }
        
            viewAlbumDetails(selectedAlbum);
        } else {
        waitForEnter();
        }
    }
    
    /**
     * View album details
     */
    private void viewAlbumDetails(Album album) {
        displayHeader("ALBUM: " + album.getTitle());
        
        System.out.printf("\nTitle: %s%n", album.getTitle());
        System.out.printf("Artist: %s%n", album.getArtist());
        System.out.printf("Year: %d%n", album.getYear());
        System.out.printf("Genre: %s%n", album.getGenre());
        
        List<Song> songs = album.getSongs();
        if (songs != null && !songs.isEmpty()) {
            System.out.println("\nSongs in this album:");
            int index = 1;
            for (Song song : songs) {
                System.out.printf("  %d. %s (%s)%n", 
                    index++,
                    song.getTitle(), 
                    formatDuration(song.getDuration()));
            }
        } else {
            System.out.println("\nNo songs in this album yet.");
        }
        
        waitForEnter();
    }
    
    /**
     * Add songs to an album
     */
    private void addSongsToAlbum(Album album) {
        displayHeader("ADD SONGS TO ALBUM: " + album.getTitle());
        
        List<Song> userSongs = songController.getUserSongs();
        
        if (userSongs.isEmpty()) {
            displayInfo("You don't have any songs to add to this album.");
            return;
        }
        
        System.out.println("\nYour songs:");
        int index = 1;
        for (Song song : userSongs) {
            System.out.printf("  %d. %s - %s%n", 
                index++,
                song.getTitle(), 
                song.getArtist());
        }
        
        System.out.println("\nEnter song titles to add (comma-separated, e.g. 'Song1,Song2,Song3'):");
        String input = scanner.nextLine().trim();
        
        String[] selections = input.split(",");
        int addedCount = 0;
        List<Song> songsToAdd = new ArrayList<>();
        
        for (String songTitle : selections) {
            String trimmedTitle = songTitle.trim();
            if (trimmedTitle.isEmpty()) continue;
            
            // Kullanıcıdan ayrıca sanatçı adı iste
            System.out.println("\nSearching for song: " + trimmedTitle);
            
            // Aynı isimde birden fazla şarkı olup olmadığını kontrol et
            List<Song> matchingSongs = new ArrayList<>();
            for (Song song : userSongs) {
                if (song.getTitle().equalsIgnoreCase(trimmedTitle)) {
                    matchingSongs.add(song);
                }
            }
            
            Song selectedSong = null;
            
            if (matchingSongs.isEmpty()) {
                System.out.println("No song found with title: " + trimmedTitle);
                continue;
            } else if (matchingSongs.size() == 1) {
                // Yalnızca bir eşleşme varsa direkt o şarkıyı seç
                selectedSong = matchingSongs.get(0);
                System.out.println("Found: " + selectedSong.getTitle() + " by " + selectedSong.getArtist());
            } else {
                // Birden fazla eşleşme varsa kullanıcıdan sanatçı adı iste
                System.out.println("Multiple songs found with this title. Please specify which one:");
                int matchIndex = 1;
                for (Song song : matchingSongs) {
                    System.out.printf("  %d. %s by %s%n", matchIndex++, song.getTitle(), song.getArtist());
                }
                
                String artistName = getStringInput("Enter artist name for '" + trimmedTitle + "'");
                
                for (Song song : matchingSongs) {
                    if (song.getArtist().equalsIgnoreCase(artistName)) {
                        selectedSong = song;
                        break;
                    }
                }
                
                if (selectedSong == null) {
                    System.out.println("No song found with title '" + trimmedTitle + "' by artist '" + artistName + "'");
                    continue;
                }
            }
            
            // Şarkı bulundu, albüme ekle
            songsToAdd.add(selectedSong);
            addedCount++;
        }
        
        if (addedCount > 0) {
            boolean success = albumController.addSongsToAlbum(album.getId(), songsToAdd);
            if (success) {
                displaySuccess(addedCount + " song(s) added to album successfully!");
            } else {
                displayError("Failed to add songs to album.");
            }
        } else {
            displayInfo("No songs were added to the album.");
        }
    }
    
    /**
     * Delete an album
     */
    private void deleteAlbum(Album album) {
        displayHeader("DELETE ALBUM");
        
        if (album == null) {
            displayError("Album not found");
            waitForEnter();
            return;
        }
        
        System.out.println("\nYou are about to delete album: " + album.getTitle());
        
        if (!getYesNoInput("Are you sure you want to delete this album?")) {
            displayInfo("Deletion cancelled.");
            waitForEnter();
            return;
        }
        
        boolean success = albumController.deleteAlbum(album.getId());
        
        if (success) {
            displaySuccess("Album deleted successfully!");
        } else {
            displayError("Failed to delete album.");
        }
        
        waitForEnter();
    }
    
    /**
     * View all artists in the library
     */
    private void viewArtists() {
        displayHeader("YOUR ARTISTS");
        
        List<String> artists = songController.getUserArtists();
        
        if (artists.isEmpty()) {
            displayInfo("No artists in your library.");
            waitForEnter();
            return;
        }
        
            System.out.println("\nYour artists:");
            int index = 1;
        for (String artist : artists) {
            System.out.printf("  %d. %s%n", index++, artist);
        }
        
        System.out.println("\nEnter the number of the artist to view their songs (or 0 to go back):");
        try {
            int selection = Integer.parseInt(scanner.nextLine().trim());
            if (selection > 0 && selection <= artists.size()) {
                String selectedArtist = artists.get(selection - 1);
                viewArtistSongs(selectedArtist);
            }
        } catch (NumberFormatException e) {
            displayError("Invalid selection. Please enter a number.");
            waitForEnter();
        }
    }
    
    /**
     * Delete artist menu
     */
    private void deleteArtistMenu() {
        displayHeader("DELETE ARTIST");
        
        List<String> artists = songController.getUserArtists();
        
        if (artists.isEmpty()) {
            displayInfo("No artists to delete.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nYour artists:");
        int index = 1;
        for (String artist : artists) {
            System.out.printf("  %d. %s%n", index++, artist);
        }
        
        String artistName = getStringInput("Enter artist name to delete");
        
        if (!artists.contains(artistName)) {
            displayError("Artist not found: " + artistName);
            waitForEnter();
            return;
        }
        
        System.out.println("\nYou are about to delete artist: " + artistName);
        
        if (!getYesNoInput("Are you sure you want to delete this artist?")) {
            displayInfo("Deletion cancelled.");
            waitForEnter();
            return;
        }
        
        Artist artist = artistController.getArtistByName(artistName);
        
        if (artist == null) {
            displayError("Artist not found: " + artistName);
            waitForEnter();
            return;
        }
        
        boolean success = artistController.deleteArtist(artist.getId());
            
            if (success) {
            displaySuccess("Artist deleted successfully!");
        } else {
            if ("Default Artist".equals(artistName)) {
                displayError("Failed to delete artist. Default artists cannot be deleted.");
            } else {
                displayError("Failed to delete artist.");
            }
        }
        
        waitForEnter();
    }
    
    /**
     * Add a song to an album
     */
    private void addSongToAlbumMenu() {
        displayHeader("ADD SONG TO ALBUM");
        
        List<Album> albums = albumController.getAlbumsByUserId(userController.getCurrentUser().getId());
        
        if (albums.isEmpty()) {
            displayInfo("You don't have any albums to add songs to.");
                waitForEnter();
            return;
        }
        
        System.out.println("\nYour albums:");
        int index = 1;
        for (Album album : albums) {
            System.out.printf("  %d. %s - %s%n", 
                index++, 
                album.getTitle(), 
                album.getArtist());
        }
        
        String albumTitle = getStringInput("Enter album title");
        String albumArtist = getStringInput("Enter album artist");
        
        Album selectedAlbum = null;
        for (Album album : albums) {
            if (album.getTitle().equalsIgnoreCase(albumTitle) && 
                album.getArtist().equalsIgnoreCase(albumArtist)) {
                selectedAlbum = album;
                break;
            }
        }
        
        if (selectedAlbum == null) {
            displayError("Album not found: " + albumTitle + " by " + albumArtist);
            waitForEnter();
            return;
        }
        
        List<Song> songs = songController.getUserSongs();
        
        if (songs.isEmpty()) {
            displayInfo("You don't have any songs to add to the album.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nYour songs:");
        index = 1;
        for (Song song : songs) {
            System.out.printf("  %d. %s - %s%n", 
                index++, 
                song.getTitle(), 
                song.getArtist());
        }
        
        String songTitle = getStringInput("Enter song title");
        String songArtist = getStringInput("Enter song artist");
        
        Song selectedSong = null;
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(songTitle) && 
                song.getArtist().equalsIgnoreCase(songArtist)) {
                selectedSong = song;
                break;
            }
        }
        
        if (selectedSong == null) {
            displayError("Song not found: " + songTitle + " by " + songArtist);
            waitForEnter();
            return;
        }
        
        boolean success = songController.addSongToAlbum(selectedAlbum.getId(), selectedSong.getId());
        
        if (success) {
            displaySuccess("Song added to album successfully!");
        } else {
            displayError("Failed to add song to album.");
        }
        
        waitForEnter();
    }

    /**
     * Format duration in seconds to MM:SS format
     * @param seconds duration in seconds
     * @return formatted duration string
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void viewArtistSongs(String artist) {
        displayHeader("SONGS BY " + artist.toUpperCase());
        
        List<Song> songs = songController.getSongsByArtist(artist);
        
        if (songs.isEmpty()) {
            displayInfo("No songs found for this artist.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nSongs:");
        int index = 1;
        for (Song song : songs) {
            System.out.printf("  %d. %s (%s)%n", 
                index++, 
                song.getTitle(),
                formatDuration(song.getDuration()));
        }
        
        waitForEnter();
    }
} 