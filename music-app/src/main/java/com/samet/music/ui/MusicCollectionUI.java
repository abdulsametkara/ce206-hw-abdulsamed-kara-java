package com.samet.music.ui;

import com.samet.music.util.DatabaseManager;
import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.repository.AlbumCollection;
import com.samet.music.service.MusicCollectionService;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UI handler for Music Collection operations
 */
public class MusicCollectionUI {
    private final Scanner scanner;
    private final PrintStream out;
    private final MusicCollectionService service;
    private SongDAO songDAO;
    private AlbumCollection albumCollection;


    public MusicCollectionUI(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
        this.service = MusicCollectionService.getInstance();
        this.songDAO = DAOFactory.getInstance().getSongDAO();
        this.albumCollection = AlbumCollection.getInstance();
    }

    /**
     * Handles adding a new song to the collection
     */
    public void addSong() {
        out.println("\n========== ADD NEW SONG ==========");

        // Get all artists and ensure they are unique
        List<Artist> artists = service.getAllArtists();

        // Sanatçı listesini benzersiz hale getir
        List<Artist> uniqueArtists = new ArrayList<>();
        Map<String, Artist> artistMap = new HashMap<>();

        for (Artist artist : artists) {
            if (!artistMap.containsKey(artist.getId())) {
                artistMap.put(artist.getId(), artist);
                uniqueArtists.add(artist);
            }
        }

        artists = uniqueArtists;

        if (artists.isEmpty()) {
            out.println("No artists available. You need to add an artist first.");
            return;
        }

        // Display available artists
        out.println("\nAvailable artists:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get artist selection
        out.print("\nSelect artist (number): ");
        int artistIndex;
        artistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (artistIndex < 0 || artistIndex >= artists.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);
        out.println("Selected artist: " + selectedArtist.getName());

        // Get song details
        out.print("Enter song name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Song name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter song duration (in seconds): ");
        int duration;
        duration = Integer.parseInt(scanner.nextLine().trim());
        if (duration <= 0) {
            out.println("Duration must be positive. Operation cancelled.");
            return;
        }
        // Makul bir süre sınırı ekle
        if (duration > 3600) { // 1 saat sınırı
            out.println("Duration is too long. Maximum allowed is 3600 seconds (1 hour).");
            return;
        }

        out.print("Enter song genre: ");
        String genre = scanner.nextLine().trim();
        if (genre.isEmpty()) {
            genre = "Unknown";
        }

        // Create the song directly to bypass service layer issues
        try {
            // Doğrudan Song nesnesi oluştur
            Song song = new Song(name, selectedArtist, duration);
            song.setGenre(genre);

            // Doğrudan SongDAO'yu kullan
            songDAO.insert(song);

            out.println("\nSong '" + name + "' added successfully!");
        } catch (Exception e) {
            out.println("\nFailed to add song. Please try again.");
            return;
        }

        // Check if the song should be added to an album
        List<Album> albums = service.getAlbumsByArtist(selectedArtist.getId());
        if (!albums.isEmpty()) {
            out.println("\nDo you want to add this song to an album?");
            out.println("1. Yes");
            out.println("2. No");
            out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                addSongToAlbum(selectedArtist);
            }
        }
    }

    /**
     * Handles adding a song to an album
     */
    public void addSongToAlbum(Artist artist) {
        List<Album> albums = service.getAlbumsByArtist(artist.getId());

        if (albums.isEmpty()) {
            out.println("No albums available for this artist. You need to add an album first.");
            return;
        }

        // Display available albums
        out.println("\nAvailable albums for " + artist.getName() + ":");
        for (int i = 0; i < albums.size(); i++) {
            out.println((i + 1) + ". " + albums.get(i).getName() + " (" + albums.get(i).getReleaseYear() + ")");
        }

        // Get album selection
        out.print("\nSelect album (number): ");
        int albumIndex;
        albumIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (albumIndex < 0 || albumIndex >= albums.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Album selectedAlbum = albums.get(albumIndex);
        out.println("Selected album: " + selectedAlbum.getName() + ", ID: " + selectedAlbum.getId());

        // Get songs not in this album
        List<Song> songs = service.getSongsByArtist(artist.getId());
        List<Song> albumSongs = service.getSongsByAlbum(selectedAlbum.getId());

        List<Song> availableSongs = songs.stream()
                .filter(song -> !albumSongs.contains(song))
                .collect(Collectors.toList());

        if (availableSongs.isEmpty()) {
            return;
        }

        // Display available songs
        out.println("\nAvailable songs to add to " + selectedAlbum.getName() + ":");
        for (int i = 0; i < availableSongs.size(); i++) {
            out.println((i + 1) + ". " + availableSongs.get(i).getName() + " (" + availableSongs.get(i).getFormattedDuration() + ")");
        }

        // Get song selection
        out.print("\nSelect song (number): ");
        int songIndex;
        songIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (songIndex < 0 || songIndex >= availableSongs.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Song selectedSong = availableSongs.get(songIndex);
        out.println("Selected song: " + selectedSong.getName() + ", ID: " + selectedSong.getId());

        boolean success = service.addSongToAlbum(selectedSong.getId(), selectedAlbum.getId());

        if (success) {
            out.println("\nSong '" + selectedSong.getName() + "' added to album '" + selectedAlbum.getName() + "' successfully!");
        } else {
            out.println("\nFailed to add song to album. Please try again.");
        }
    }

    /**
     * Handles adding a new album to the collection
     */
    public void addAlbum() {
        out.println("\n========== ADD NEW ALBUM ==========");

        // If there are no artists, we need to create one first
        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists available. You need to add an artist first.");
            return;
        }

        // Display available artists
        out.println("\nAvailable artists:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get artist selection
        out.print("\nSelect artist (number): ");
        int artistIndex;
        artistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (artistIndex < 0 || artistIndex >= artists.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);
        out.println("Selected artist: " + selectedArtist.getName() + ", ID: " + selectedArtist.getId());

        // Get album details
        out.print("Enter album name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Album name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter release year: ");
        int releaseYear;
        releaseYear = Integer.parseInt(scanner.nextLine().trim());
        if (releaseYear <= 0) {
            out.println("Release year must be positive. Operation cancelled.");
            return;
        }

        out.print("Enter album genre: ");
        String genre = scanner.nextLine().trim();
        if (genre.isEmpty()) {
            genre = "Unknown";
        }

        // Log işlem detayları
        out.println("Trying to add album: " + name + ", Artist ID: " + selectedArtist.getId() +
                ", Release Year: " + releaseYear + ", Genre: " + genre);

        boolean success = service.addAlbum(name, selectedArtist.getId(), releaseYear, genre);

        if (success) {
            out.println("\nAlbum '" + name + "' added successfully!");

            // Albüm oluşturulduktan sonra şarkı eklemek isteyip istemediğini sor
            out.print("Do you want to add songs to this album? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("y") || answer.equals("yes")) {
                // Yeni oluşturulan albümü bul
                List<Album> albums = service.getAlbumsByArtist(selectedArtist.getId());
                Album newAlbum = null;

                for (Album album : albums) {
                    if (album.getName().equals(name)) {
                        newAlbum = album;
                        break;
                    }
                }

                if (newAlbum != null) {
                    // Şarkı ekleme işlemini sanatçı ile çağır
                    addSongToAlbum(selectedArtist);
                } else {
                    out.println("Could not locate the newly created album. Please add songs from the menu.");
                }
            }
        } else {
            out.println("\nFailed to add album. Please try again.");
        }
    }

    public void addSongToAlbumMenu() {
        out.println("\n========== ADD SONG TO ALBUM ==========");

        // Önce sanatçıları listele
        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists available. You need to add an artist first.");
            return;
        }

        // Sanatçıları göster
        out.println("\nSelect an artist:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Sanatçı seçimini al
        out.print("\nEnter artist number (0 to cancel): ");
        int artistIndex;
        artistIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (artistIndex < 0 || artistIndex >= artists.size()) {
            out.println("Invalid selection or cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);
        // Seçilen sanatçıyla şarkı ekleme metodunu çağır
        addSongToAlbum(selectedArtist);
    }

    /**
     * Handles adding a new artist to the collection
     */
    public void addArtist() {
        out.println("\n========== ADD NEW ARTIST ==========");

        out.print("Enter artist name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            out.println("Artist name cannot be empty. Operation cancelled.");
            return;
        }

        out.print("Enter artist biography (optional): ");
        String biography = scanner.nextLine().trim();

        // Log işlem detayları
        out.println("Trying to add artist: " + name);

        boolean success = service.addArtist(name, biography);

        if (success) {
            out.println("\nArtist '" + name + "' added successfully!");
        } else {
            out.println("\nFailed to add artist. Please try again.");
        }
    }

    /**
     * Displays all songs in the collection
     */
    public void viewSongs() {
        out.println("\n========== ALL SONGS ==========");

        List<Song> songs = service.getAllSongs();

        if (songs.isEmpty()) {
            out.println("No songs in the collection.");
            return;
        }

        out.println("Total songs: " + songs.size());
        out.println("\nID | Name | Artist | Duration | Album | Genre");
        out.println("------------------------------------------------");

        for (Song song : songs) {
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            String albumName = song.getAlbum() != null ? song.getAlbum().getName() : "N/A";

            out.println(song.getId() + " | " +
                    song.getName() + " | " +
                    artistName + " | " +
                    song.getFormattedDuration() + " | " +
                    albumName + " | " +
                    song.getGenre());
        }
    }

    /**
     * Displays all albums in the collection
     */
    public void viewAlbums() {
        out.println("\n========== ALL ALBUMS ==========");

        List<Album> albums = service.getAllAlbums();

        if (albums.isEmpty()) {
            out.println("No albums in the collection.");
            return;
        }

        out.println("Total albums: " + albums.size());
        out.println("\nID | Name | Artist | Release Year | Genre | Songs");
        out.println("--------------------------------------------------");

        for (Album album : albums) {
            String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";
            List<Song> songs = service.getSongsByAlbum(album.getId());

            out.println(album.getId() + " | " +
                    album.getName() + " | " +
                    artistName + " | " +
                    album.getReleaseYear() + " | " +
                    album.getGenre() + " | " +
                    songs.size());
        }
    }

    /**
     * Displays all artists in the collection
     */
    public void viewArtists() {
        out.println("\n========== ALL ARTISTS ==========");

        List<Artist> artists = service.getAllArtists();

        if (artists.isEmpty()) {
            out.println("No artists in the collection.");
            return;
        }

        out.println("Total artists: " + artists.size());
        out.println("\nID | Name | Albums | Songs");
        out.println("---------------------------");

        for (Artist artist : artists) {
            List<Album> albums = service.getAlbumsByArtist(artist.getId());
            List<Song> songs = service.getSongsByArtist(artist.getId());

            out.println(artist.getId() + " | " +
                    artist.getName() + " | " +
                    albums.size() + " | " +
                    songs.size());
        }
    }

    /**
     * Handles deleting an artist
     */
    public void deleteArtist() {
        out.println("\n========== DELETE ARTIST ==========");

        List<Artist> artists = service.getAllArtists();
        if (artists.isEmpty()) {
            out.println("No artists found in the collection.");
            return;
        }

        // Display available artists
        out.println("\nSelect an artist to delete:");
        for (int i = 0; i < artists.size(); i++) {
            out.println((i + 1) + ". " + artists.get(i).getName());
        }

        // Get user selection
        out.print("\nEnter artist number (or 0 to cancel): ");
        int artistIndex;
        artistIndex = Integer.parseInt(scanner.nextLine().trim());
        if (artistIndex == 0) {
            return; // User cancelled
        }
        artistIndex--; // Convert to 0-based index

        if (artistIndex < 0 || artistIndex >= artists.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Artist selectedArtist = artists.get(artistIndex);

        // Check if artist has albums or songs
        List<Album> artistAlbums = service.getAlbumsByArtist(selectedArtist.getId());
        List<Song> artistSongs = service.getSongsByArtist(selectedArtist.getId());

        if (!artistAlbums.isEmpty() || !artistSongs.isEmpty()) {
            out.println("\nWarning: This artist has associated albums and/or songs.");
            out.println("Deleting this artist will also delete:");

            if (!artistAlbums.isEmpty()) {
                out.println("- " + artistAlbums.size() + " album(s)");
            }

            if (!artistSongs.isEmpty()) {
                out.println("- " + artistSongs.size() + " song(s)");
            }

            out.println("\nAre you sure you want to delete this artist? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                out.println("Operation cancelled.");
                return;
            }
        } else {
            out.println("\nAre you sure you want to delete artist '" + selectedArtist.getName() + "'? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                out.println("Operation cancelled.");
                return;
            }
        }

        // Delete the artist
        boolean success = service.removeArtist(selectedArtist.getId());

        if (success) {
            out.println("\nArtist '" + selectedArtist.getName() + "' has been deleted successfully.");
        } else {
            out.println("\nFailed to delete the artist. Please try again.");
        }
    }

    /**
     * Handles deleting a song
     */
    public void deleteSong() {
        out.println("\n========== DELETE SONG ==========");

        List<Song> songs = service.getAllSongs();
        if (songs.isEmpty()) {
            out.println("No songs found in the collection.");
            return;
        }

        // Display available songs
        out.println("\nSelect a song to delete:");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            String albumName = song.getAlbum() != null ? song.getAlbum().getName() : "N/A";

            out.println((i + 1) + ". " + song.getName() + " - " + artistName +
                    " (" + song.getFormattedDuration() + ") - " + albumName);
        }

        // Get user selection
        out.print("\nEnter song number (or 0 to cancel): ");
        int songIndex;
        songIndex = Integer.parseInt(scanner.nextLine().trim());
        if (songIndex == 0) {
            return; // User cancelled
        }
        songIndex--; // Convert to 0-based index

        if (songIndex < 0 || songIndex >= songs.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Song selectedSong = songs.get(songIndex);

        // Check if song is in any playlists
        List<Playlist> playlists = service.getPlaylistsContainingSong(selectedSong.getId());
        if (!playlists.isEmpty()) {
            out.println("\nWarning: This song is used in the following playlists:");
            for (Playlist playlist : playlists) {
                out.println("- " + playlist.getName());
            }
            out.println("Deleting this song will remove it from these playlists.");

            out.println("\nAre you sure you want to delete this song? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                out.println("Operation cancelled.");
                return;
            }
        } else {
            out.println("\nAre you sure you want to delete song '" + selectedSong.getName() + "'? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                out.println("Operation cancelled.");
                return;
            }
        }

        // Delete the song
        boolean success = service.removeSong(selectedSong.getId());

        if (success) {
            out.println("\nSong '" + selectedSong.getName() + "' has been deleted successfully.");
        } else {
            out.println("\nFailed to delete the song. Please try again.");
        }
    }

    /**
     * Removes a song and its references from playlists
     *
     * @param songId ID of the song to remove
     * @return true if the song was removed successfully
     */
    public boolean removeSong(String songId) {
        if (songId == null || songId.trim().isEmpty()) {
            return false;
        }

        try {
            // Get song for validation
            Song song = songDAO.getById(songId);
            if (song == null) {
                return false;
            }

            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                // 1. Şarkıyı çalma listelerinden kaldır
                PreparedStatement removeFromPlaylists = conn.prepareStatement(
                        "DELETE FROM playlist_songs WHERE song_id = ?");
                removeFromPlaylists.setString(1, songId);
                removeFromPlaylists.executeUpdate();
                removeFromPlaylists.close();

                // 2. Şarkıyı sil
                PreparedStatement deleteSong = conn.prepareStatement(
                        "DELETE FROM songs WHERE id = ?");
                deleteSong.setString(1, songId);
                deleteSong.executeUpdate();
                deleteSong.close();

                // İşlemi tamamla
                conn.commit();

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.err.println("Error removing song: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error removing song: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles deleting an album
     */
    public void deleteAlbum() {
        out.println("\n========== DELETE ALBUM ==========");

        List<Album> albums = service.getAllAlbums();
        if (albums.isEmpty()) {
            out.println("No albums found in the collection.");
            return;
        }

        // Display available albums
        out.println("\nSelect an album to delete:");
        for (int i = 0; i < albums.size(); i++) {
            Album album = albums.get(i);
            String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";

            out.println((i + 1) + ". " + album.getName() + " - " + artistName +
                    " (" + album.getReleaseYear() + ") - " + album.getGenre());
        }

        // Get user selection
        out.print("\nEnter album number (or 0 to cancel): ");
        int albumIndex;
        albumIndex = Integer.parseInt(scanner.nextLine().trim());
        if (albumIndex == 0) {
            return; // User cancelled
        }
        albumIndex--; // Convert to 0-based index

        if (albumIndex < 0 || albumIndex >= albums.size()) {
            out.println("Invalid selection. Operation cancelled.");
            return;
        }

        Album selectedAlbum = albums.get(albumIndex);

        // Check if album has songs
        List<Song> albumSongs = service.getSongsByAlbum(selectedAlbum.getId());
        if (!albumSongs.isEmpty()) {
            out.println("\nWarning: This album contains " + albumSongs.size() + " songs.");
            out.println("You have two options:");
            out.println("1. Delete the album and keep the songs (songs will no longer be associated with this album)");
            out.println("2. Delete the album and all its songs");
            out.println("3. Cancel operation");
            out.print("\nYour choice: ");

            int choice;
            choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 3) {
                out.println("Operation cancelled.");
                return;
            } else if (choice != 1 && choice != 2) {
                out.println("Invalid choice. Operation cancelled.");
                return;
            }

            boolean deleteSongs = (choice == 2);

            // Delete the album
            boolean success = service.removeAlbum(selectedAlbum.getId(), deleteSongs);

            if (success) {
                if (deleteSongs) {
                    out.println("\nAlbum '" + selectedAlbum.getName() + "' and its songs have been deleted successfully.");
                } else {
                    out.println("\nAlbum '" + selectedAlbum.getName() + "' has been deleted successfully. Songs are kept.");
                }
            } else {
                out.println("\nFailed to delete the album. Please try again.");
            }
        } else {
            // No songs, just confirm and delete album
            out.println("\nAre you sure you want to delete album '" + selectedAlbum.getName() + "'? (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                out.println("Operation cancelled.");
                return;
            }

            boolean success = service.removeAlbum(selectedAlbum.getId(), false);

            if (success) {
                out.println("\nAlbum '" + selectedAlbum.getName() + "' has been deleted successfully.");
            } else {
                out.println("\nFailed to delete the album. Please try again.");
            }
        }
    }

    /**
     * Removes an album and optionally its songs
     *
     * @param albumId ID of the album to remove
     * @param deleteSongs true to delete the album's songs, false to keep them
     * @return true if the album was removed successfully
     */
    public boolean removeAlbum(String albumId, boolean deleteSongs) {
        if (albumId == null || albumId.trim().isEmpty()) {
            return false;
        }

        try {
            // Get album for validation
            Album album = albumCollection.getById(albumId);
            if (album == null) {
                return false;
            }

            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                if (deleteSongs) {
                    // 1. Albümdeki şarkıları çalma listelerinden kaldır
                    PreparedStatement removeFromPlaylists = conn.prepareStatement(
                            "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE album_id = ?)");
                    removeFromPlaylists.setString(1, albumId);
                    removeFromPlaylists.executeUpdate();
                    removeFromPlaylists.close();

                    // 2. Albümdeki tüm şarkıları sil
                    PreparedStatement deleteSongsStmt = conn.prepareStatement(
                            "DELETE FROM songs WHERE album_id = ?");
                    deleteSongsStmt.setString(1, albumId);
                    deleteSongsStmt.executeUpdate();
                    deleteSongsStmt.close();
                } else {
                    // Şarkıları silmeden albüm ilişkisini kaldır
                    PreparedStatement updateSongs = conn.prepareStatement(
                            "UPDATE songs SET album_id = NULL WHERE album_id = ?");
                    updateSongs.setString(1, albumId);
                    updateSongs.executeUpdate();
                    updateSongs.close();
                }

                // 3. Albümü sil
                PreparedStatement deleteAlbum = conn.prepareStatement(
                        "DELETE FROM albums WHERE id = ?");
                deleteAlbum.setString(1, albumId);
                deleteAlbum.executeUpdate();
                deleteAlbum.close();

                // İşlemi tamamla
                conn.commit();

                // Memory koleksiyondan da kaldır
                albumCollection.remove(albumId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.err.println("Error removing album: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error removing album: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}