package com.samet.music.ui;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

/**
 * UI class for playlist operations
 */
public class PlaylistUI {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistUI.class);
    private final MusicCollectionService service;
    private final Scanner scanner;

    /**
     * Constructor
     */
    public PlaylistUI(MusicCollectionService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    /**
     * Creates a new playlist
     */
    public void createPlaylist() {
        System.out.println("\n=== Create Playlist ===");
        System.out.print("Enter playlist name: ");
        String name = scanner.nextLine();

        System.out.print("Enter playlist description: ");
        String description = scanner.nextLine();

        if (service.createPlaylist(name, description)) {
            System.out.println("Playlist created successfully!");
        } else {
            System.out.println("Failed to create playlist.");
        }
    }

    /**
     * Views all playlists
     */
    public void viewPlaylists() {
        System.out.println("\n=== All Playlists ===");
        List<Playlist> playlists = service.getAllPlaylists();

        if (playlists.isEmpty()) {
            System.out.println("No playlists found.");
            return;
        }

        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            System.out.printf("%d. %s (%d songs)\n",
                    i + 1,
                    playlist.getName(),
                    playlist.getSongs().size());
        }
    }

    /**
     * Edits an existing playlist
     */
    public void editPlaylist() {
        List<Playlist> playlists = service.getAllPlaylists();
        if (playlists.isEmpty()) {
            System.out.println("No playlists found.");
            return;
        }

        viewPlaylists();
        System.out.print("Enter playlist number to edit: ");
        int index = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (index < 1 || index > playlists.size()) {
            System.out.println("Invalid playlist number.");
            return;
        }

        Playlist playlist = playlists.get(index - 1);

        System.out.println("\n=== Edit Playlist: " + playlist.getName() + " ===");
        System.out.println("1. Rename playlist");
        System.out.println("2. Change description");
        System.out.println("3. Add songs");
        System.out.println("4. Remove songs");
        System.out.println("0. Back");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                renamePlaylist(playlist);
                break;
            case 2:
                changeDescription(playlist);
                break;
            case 3:
                addSongsToPlaylist(playlist);
                break;
            case 4:
                removeSongsFromPlaylist(playlist);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    /**
     * Renames a playlist
     */
    private void renamePlaylist(Playlist playlist) {
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();

        String oldName = playlist.getName();
        playlist.setName(newName);

        if (service.updatePlaylist(playlist)) {
            System.out.println("Playlist renamed from '" + oldName + "' to '" + newName + "'");
        } else {
            System.out.println("Failed to rename playlist.");
            playlist.setName(oldName); // Restore old name
        }
    }

    /**
     * Changes playlist description
     */
    private void changeDescription(Playlist playlist) {
        System.out.print("Enter new description: ");
        String newDescription = scanner.nextLine();

        String oldDescription = playlist.getDescription();
        playlist.setDescription(newDescription);

        if (service.updatePlaylist(playlist)) {
            System.out.println("Playlist description updated.");
        } else {
            System.out.println("Failed to update playlist description.");
            playlist.setDescription(oldDescription); // Restore old description
        }
    }

    /**
     * Adds songs to a playlist
     */
    private void addSongsToPlaylist(Playlist playlist) {
        List<Song> allSongs = service.getAllSongs();
        if (allSongs.isEmpty()) {
            System.out.println("No songs available to add.");
            return;
        }

        System.out.println("\n=== Available Songs ===");
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            System.out.printf("%d. %s - %s\n",
                    i + 1,
                    song.getName(),
                    song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
        }

        System.out.print("Enter song number to add (0 to finish): ");
        int index = scanner.nextInt();
        scanner.nextLine(); // consume newline

        while (index > 0 && index <= allSongs.size()) {
            Song song = allSongs.get(index - 1);

            if (service.addSongToPlaylist(song.getId(), playlist.getId())) {
                System.out.println("Added: " + song.getName());
            } else {
                System.out.println("Failed to add: " + song.getName());
            }

            System.out.print("Enter another song number (0 to finish): ");
            index = scanner.nextInt();
            scanner.nextLine(); // consume newline
        }
    }

    /**
     * Removes songs from a playlist
     */
    private void removeSongsFromPlaylist(Playlist playlist) {
        List<Song> playlistSongs = service.getSongsInPlaylist(playlist.getId());
        if (playlistSongs.isEmpty()) {
            System.out.println("No songs in this playlist.");
            return;
        }

        System.out.println("\n=== Songs in Playlist ===");
        for (int i = 0; i < playlistSongs.size(); i++) {
            Song song = playlistSongs.get(i);
            System.out.printf("%d. %s - %s\n",
                    i + 1,
                    song.getName(),
                    song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
        }

        System.out.print("Enter song number to remove (0 to finish): ");
        int index = scanner.nextInt();
        scanner.nextLine(); // consume newline

        while (index > 0 && index <= playlistSongs.size()) {
            Song song = playlistSongs.get(index - 1);

            if (service.removeSongFromPlaylist(song.getId(), playlist.getId())) {
                System.out.println("Removed: " + song.getName());
                // Update the list
                playlistSongs = service.getSongsInPlaylist(playlist.getId());
                if (playlistSongs.isEmpty()) {
                    System.out.println("No more songs in this playlist.");
                    return;
                }

                // Show updated list
                System.out.println("\n=== Remaining Songs ===");
                for (int i = 0; i < playlistSongs.size(); i++) {
                    Song remainingSong = playlistSongs.get(i);
                    System.out.printf("%d. %s - %s\n",
                            i + 1,
                            remainingSong.getName(),
                            remainingSong.getArtist() != null ? remainingSong.getArtist().getName() : "Unknown Artist");
                }
            } else {
                System.out.println("Failed to remove: " + song.getName());
            }

            System.out.print("Enter another song number (0 to finish): ");
            index = scanner.nextInt();
            scanner.nextLine(); // consume newline
        }
    }

    /**
     * Method to add a playlist to the collection
     * @param playlist Playlist to add
     * @return true if successful
     */
    public boolean addPlaylist(com.samet.music.model.Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot add null playlist");
            return false;
        }

        return service.addPlaylist(playlist);
    }

    /**
     * Method to update a playlist in the service
     * This method needs to be added to MusicCollectionService
     * @param playlist Playlist to update
     * @return true if successful
     */
    public boolean updatePlaylist(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Cannot update null playlist");
            return false;
        }

        return service.updatePlaylist(playlist);
    }
}