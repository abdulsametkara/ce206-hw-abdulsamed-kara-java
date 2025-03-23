package com.samet.music.ui;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * UI handler for Playlist operations
 */
public class PlaylistUI {
    private final Scanner scanner;
    private final PrintStream out;
    private final MusicCollectionService service;

    public PlaylistUI(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
        this.service = MusicCollectionService.getInstance();
    }

    public void createPlaylist() {
        out.println("\n========== CREATE NEW PLAYLIST ==========");

        out.print("Enter playlist name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Playlist name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter playlist description (optional): ");
        String description = scanner.nextLine().trim();

        // Burada bir Playlist nesnesi oluşturalım ve ID'sini saklayalım
        Playlist newPlaylist = new Playlist(name, description);
        String playlistId = newPlaylist.getId();

        // Playlist'i service üzerinden ekleyelim, ancak direkt newPlaylist nesnesini geçelim
        boolean success = service.addPlaylist(newPlaylist);  // Bu metodu MusicCollectionService'e eklemeliyiz

        if (success) {
            out.println("\nPlaylist '" + name + "' created successfully!");

            // Kullanıcıya şarkı eklemek isteyip istemediğini soralım
            out.println("\nDo you want to add songs to this playlist now?");
            out.println("1. Yes");
            out.println("2. No");
            out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                // Doğrudan oluşturduğumuz playlist ID'sini kullanalım
                out.println("Adding songs to playlist: " + name + " (ID: " + playlistId + ")");
                addSongsToPlaylist(playlistId);
            }
        } else {
            out.println("\nFailed to create playlist. Please try again.");
        }
    }

    /**
     * Displays all playlists
     */
    public void viewPlaylists() {
        out.println("\n========== ALL PLAYLISTS ==========");

        List<Playlist> playlists = service.getAllPlaylists();

        if (playlists.isEmpty()) {
            out.println("No playlists found.");
            return;
        }

        out.println("Total playlists: " + playlists.size());
        out.println("\nID | Name | Description | Songs | Duration");
        out.println("--------------------------------------------");

        for (Playlist playlist : playlists) {
            out.println(playlist.getId() + " | " +
                    playlist.getName() + " | " +
                    (playlist.getDescription().isEmpty() ? "N/A" : playlist.getDescription()) + " | " +
                    playlist.getSongCount() + " | " +
                    playlist.getFormattedTotalDuration());
        }

        // Ask if user wants to view a specific playlist's details
        out.println("\nDo you want to view details of a specific playlist?");
        out.println("1. Yes");
        out.println("2. No");
        out.print("Your choice: ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) {
            viewPlaylistDetails();
        }
    }

    public void viewPlaylistDetails() {
        // En güncel playlist listesini alalım
        PlaylistDAO playlistDAO = new PlaylistDAO();
        List<Playlist> playlists = playlistDAO.getAll();

        if (playlists.isEmpty()) {
            out.println("No playlists available.");
            return;
        }

        out.println("\nSelect a playlist to view:");
        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            out.println((i + 1) + ". " + playlist.getName() + " (" + playlist.getSongCount() + " songs) - ID: " + playlist.getId());
        }

        out.print("\nEnter playlist number: ");
        int playlistIndex;
        try {
            playlistIndex = Integer.parseInt(scanner.nextLine().trim());
            if (playlistIndex < 1 || playlistIndex > playlists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Playlist selectedPlaylist = playlists.get(playlistIndex - 1);

        // Seçilen playlist'in ID'sini gösterelim
        String playlistId = selectedPlaylist.getId();
        System.out.println("Using playlist with ID: " + playlistId);

        // Playlist'in detaylarını gösterelim
        out.println("\n========== PLAYLIST: " + selectedPlaylist.getName() + " ==========");
        out.println("Description: " + (selectedPlaylist.getDescription().isEmpty() ? "N/A" : selectedPlaylist.getDescription()));
        out.println("Total songs: " + selectedPlaylist.getSongCount());
        out.println("Total duration: " + selectedPlaylist.getFormattedTotalDuration());

        List<Song> songs = selectedPlaylist.getSongs();

        if (songs.isEmpty()) {
            out.println("\nThis playlist is empty.");
        } else {
            out.println("\nSongs in this playlist:");
            out.println("No. | Name | Artist | Duration | Album");
            out.println("-----------------------------------------");

            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
                String albumName = song.getAlbum() != null ? song.getAlbum().getName() : "N/A";

                out.println((i + 1) + ". | " +
                        song.getName() + " | " +
                        artistName + " | " +
                        song.getFormattedDuration() + " | " +
                        albumName);
            }
        }
    }

    public void editPlaylist() {
        // En güncel playlist listesini doğrudan DAO'dan alalım
        PlaylistDAO playlistDAO = new PlaylistDAO();
        List<Playlist> playlists = playlistDAO.getAll();

        if (playlists.isEmpty()) {
            out.println("No playlists available to edit.");
            return;
        }

        out.println("\n========== EDIT PLAYLIST ==========");
        out.println("Select a playlist to edit:");

        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            out.println((i + 1) + ". " + playlist.getName() + " - ID: " + playlist.getId());
        }

        out.print("\nEnter playlist number: ");
        int playlistIndex;
        try {
            playlistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (playlistIndex < 0 || playlistIndex >= playlists.size()) {
                out.println("Invalid selection. Operation cancelled.");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input. Operation cancelled.");
            return;
        }

        Playlist selectedPlaylist = playlists.get(playlistIndex);
        String playlistId = selectedPlaylist.getId();

        System.out.println("Using playlist with ID: " + playlistId);

        out.println("\nEditing playlist: " + selectedPlaylist.getName());
        out.println("1. Rename playlist");
        out.println("2. Edit description");
        out.println("3. Add songs");
        out.println("4. Remove songs");
        out.println("5. Delete playlist");
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
                return;
            case 1:
                renamePlaylist(selectedPlaylist);
                break;
            case 2:
                editPlaylistDescription(selectedPlaylist);
                break;
            case 3:
                addSongsToPlaylist(playlistId);
                break;
            case 4:
                removeSongsFromPlaylist(selectedPlaylist);
                break;
            case 5:
                deletePlaylist(selectedPlaylist);
                break;
            default:
                out.println("Invalid choice. Operation cancelled.");
                break;
        }
    }
    /**
     * Handles renaming a playlist
     */
    private void renamePlaylist(Playlist playlist) {
        out.print("\nEnter new name for playlist: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            out.println("Playlist name cannot be empty. Operation cancelled.");
            return;
        }

        playlist.setName(newName);
        out.println("Playlist renamed successfully to '" + newName + "'!");
    }

    /**
     * Handles editing a playlist's description
     */
    private void editPlaylistDescription(Playlist playlist) {
        out.print("\nEnter new description for playlist: ");
        String newDescription = scanner.nextLine().trim();

        playlist.setDescription(newDescription);
        out.println("Playlist description updated successfully!");
    }

    /**
     * Handles adding songs to a playlist
     */
    public void addSongsToPlaylist(String playlistId) {
        if (playlistId == null || playlistId.isEmpty()) {
            out.println("Invalid playlist ID.");
            return;
        }

        // PlaylistDAO'yu doğrudan kullanalım
        PlaylistDAO playlistDAO = new PlaylistDAO();
        Playlist playlist = playlistDAO.getById(playlistId);

        if (playlist == null) {
            out.println("Looking for playlist with ID: " + playlistId);
            out.println("No playlist found with ID: " + playlistId);
            out.println("Playlist not found.");
            return;
        }

        List<Song> allSongs = service.getAllSongs();
        if (allSongs.isEmpty()) {
            out.println("No songs available to add to the playlist.");
            return;
        }

        List<Song> playlistSongs = service.getSongsInPlaylist(playlistId);

        // Filter out songs that are already in the playlist
        List<Song> availableSongs = allSongs.stream()
                .filter(song -> !playlistSongs.contains(song))
                .collect(java.util.stream.Collectors.toList());

        if (availableSongs.isEmpty()) {
            out.println("All available songs are already in this playlist.");
            return;
        }

        out.println("\n========== ADD SONGS TO PLAYLIST: " + playlist.getName() + " ==========");
        out.println("Available songs:");

        for (int i = 0; i < availableSongs.size(); i++) {
            Song song = availableSongs.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";

            out.println((i + 1) + ". " + song.getName() + " - " + artistName +
                    " (" + song.getFormattedDuration() + ")");
        }

        out.println("\nEnter the numbers of songs to add (comma-separated, e.g., 1,3,5)");
        out.println("Or enter 'all' to add all songs");
        out.print("Your selection: ");

        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("all")) {
            // Add all available songs
            for (Song song : availableSongs) {
                playlistDAO.addSongToPlaylist(playlistId, song.getId());
            }
            out.println("Added " + availableSongs.size() + " songs to the playlist.");
            return;
        }

        // Parse user selection
        String[] selections = input.split(",");
        int addedCount = 0;

        for (String selection : selections) {
            try {
                int index = Integer.parseInt(selection.trim()) - 1;
                if (index >= 0 && index < availableSongs.size()) {
                    Song selectedSong = availableSongs.get(index);
                    playlistDAO.addSongToPlaylist(playlistId, selectedSong.getId());
                    addedCount++;
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        }

        out.println("Added " + addedCount + " songs to the playlist.");
    }


    /**
     * Handles removing songs from a playlist
     */
    private void removeSongsFromPlaylist(Playlist playlist) {
        List<Song> playlistSongs = service.getSongsInPlaylist(playlist.getId());

        if (playlistSongs.isEmpty()) {
            out.println("This playlist is empty.");
            return;
        }

        out.println("\n========== REMOVE SONGS FROM PLAYLIST: " + playlist.getName() + " ==========");
        out.println("Songs in this playlist:");

        for (int i = 0; i < playlistSongs.size(); i++) {
            Song song = playlistSongs.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";

            out.println((i + 1) + ". " + song.getName() + " - " + artistName +
                    " (" + song.getFormattedDuration() + ")");
        }

        out.println("\nEnter the numbers of songs to remove (comma-separated, e.g., 1,3,5)");
        out.println("Or enter 'all' to remove all songs");
        out.print("Your selection: ");

        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("all")) {
            // Remove all songs
            for (Song song : playlistSongs) {
                service.removeSongFromPlaylist(song.getId(), playlist.getId());
            }
            out.println("Removed all songs from the playlist.");
            return;
        }

        // Parse user selection
        String[] selections = input.split(",");
        int removedCount = 0;

        for (String selection : selections) {
            try {
                int index = Integer.parseInt(selection.trim()) - 1;
                if (index >= 0 && index < playlistSongs.size()) {
                    Song selectedSong = playlistSongs.get(index);
                    if (service.removeSongFromPlaylist(selectedSong.getId(), playlist.getId())) {
                        removedCount++;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        }

        out.println("Removed " + removedCount + " songs from the playlist.");
    }

    /**
     * Handles deleting a playlist
     */
    private void deletePlaylist(Playlist playlist) {
        out.println("\nAre you sure you want to delete the playlist '" + playlist.getName() + "'?");
        out.println("This action cannot be undone.");
        out.println("1. Yes, delete the playlist");
        out.println("2. No, cancel");
        out.print("Your choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            boolean success = service.removePlaylist(playlist.getId());
            if (success) {
                out.println("Playlist '" + playlist.getName() + "' has been deleted.");
            } else {
                out.println("Failed to delete the playlist. Please try again.");
            }
        } else {
            out.println("Operation cancelled.");
        }
    }


}