package com.samet.music;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.HashSet;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.view.MusicCollectionView;
import com.samet.music.view.MenuView;
import com.samet.music.view.LoginMenuView;

/**
 * Test class for MusicCollectionView
 * Not: Bu testler JUnit 4 kullanılarak yazılmıştır. Mockito kütüphanesi kullanılmamıştır.
 */
public class MusicCollectionViewTest {

    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @Before
    public void setUp() {
        // Manuel olarak nesneleri oluştur
        userController = createUserController();
        songController = createSongController();
        playlistController = createPlaylistController();
        
        // Redirect System.out to capture output
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    @After
    public void tearDown() {
        // Reset System.out
        System.setOut(originalOut);
    }
    
    /**
     * Test UserController oluşturur
     */
    private UserController createUserController() {
        return new UserController() {
            private User currentUser = new User(); // User sınıfı için varsayılan yapılandırıcı kullan
            
            @Override
            public boolean isLoggedIn() {
                return true; // Varsayılan olarak kullanıcı giriş yapmış
            }
            
            @Override
            public User getCurrentUser() {
                currentUser.setId(1);
                currentUser.setUsername("testuser");
                return currentUser;
            }
            
            // Diğer metotlar gerektiğinde eklenir
        };
    }
    
    /**
     * Test SongController oluşturur
     */
    private SongController createSongController() {
        return new SongController(createUserController()) {
            @Override
            public List<String> getArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2", "Test Artist 3");
            }
            
            @Override
            public List<Song> getUserSongs() {
                List<Song> songs = new ArrayList<>();
                User user = new User();
                user.setId(1);
                Song song = new Song("Test Song 1", "Test Artist 1", "Test Album 1", "Rock", 2020, 180, "music/Test.mp3", user);
                song.setId(1);
                songs.add(song);
                return songs;
            }
            
            @Override
            public List<String> getUserArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2", "Test Artist 3");
            }
            
            @Override
            public boolean addArtist(String artistName) {
                // "FailArtist" için false döndür
                if ("FailArtist".equals(artistName)) {
                    return false;
                }
                return true;
            }
            
            @Override
            public Song addSong(String title, String artist, String album, String genre, int year, int duration, String filePath) {
                // "FailSong" için null döndür
                if ("FailSong".equals(title)) {
                    return null;
                }
                User user = new User();
                user.setId(1);
                Song song = new Song(title, artist, album, genre, year, duration, filePath, user);
                song.setId(3);
                return song;
            }
            
            @Override
            public boolean deleteSong(int songId) {
                // Belirli bir ID için silme işleminin başarısız olmasını simüle et
                if (songId == 999) {
                    return false;
                }
                return true;
            }
            
            @Override
            public List<Album> getUserAlbums() {
                List<Album> albums = new ArrayList<>();
                Album album = new Album("Test Album 1", "Test Artist 1", 2020, "Rock", 1);
                album.setId(1);
                
                // Add songs to album for testing
                List<Song> albumSongs = new ArrayList<>();
                User user = new User();
                user.setId(1);
                Song song1 = new Song("Album Song 1", "Test Artist 1", "Test Album 1", "Rock", 2020, 180, "music/Test.mp3", user);
                song1.setId(1);
                albumSongs.add(song1);
                album.setSongs(albumSongs); // Pass List directly instead of converting to HashSet
                
                albums.add(album);
                return albums;
            }
            
            @Override
            public Album addAlbum(String title, String artist, int year, String genre) {
                // "FailAlbum" için null döndür
                if ("FailAlbum".equals(title)) {
                    return null;
                }
                Album album = new Album(title, artist, year, genre, 1);
                album.setId(1);
                return album;
            }
            
            @Override
            public boolean addSongToAlbum(int albumId, int songId) {
                // Belirli kombinasyonlar için başarısız olmasını simüle et
                if (albumId == 999 || songId == 999) {
                    return false;
                }
                return true;
            }
            
            @Override
            public boolean deleteAlbum(int albumId) {
                // Belirli bir ID için silme işleminin başarısız olmasını simüle et
                if (albumId == 999) {
                    return false;
                }
                return true;
            }
            
            @Override
            public boolean deleteArtist(String artistName) {
                // "Default Artist" için false döndür (silinemiyor)
                if ("Default Artist".equals(artistName)) {
                    return false;
                }
                return true;
            }
            
            @Override
            public List<Song> getSongsByArtist(String artist) {
                List<Song> songs = new ArrayList<>();
                if (artist.equals("Test Artist 1")) {
                    User user = new User();
                    user.setId(1);
                    Song song1 = new Song("Artist Song 1", artist, "Artist Album 1", "Rock", 2020, 180, "music/Test.mp3", user);
                    song1.setId(1);
                    songs.add(song1);
                    
                    User user2 = new User();
                    user2.setId(1);
                    Song song2 = new Song("Artist Song 2", artist, "Artist Album 2", "Rock", 2021, 210, "music/Test2.mp3", user2);
                    song2.setId(2);
                    songs.add(song2);
                }
                return songs;
            }
            
            // artistExists metodu özel olarak bu kontrolleri yapıyor
            private boolean artistExists(String artist) {
                return Arrays.asList("Test Artist 1", "Test Artist 2").contains(artist);
            }
        };
    }
    
    /**
     * Özel başarısız SongController oluştur
     */
    private SongController createEmptySongController() {
        return new SongController(createUserController()) {
            @Override
            public List<String> getArtists() {
                return new ArrayList<>(); // Boş liste
            }
            
            @Override
            public List<Song> getUserSongs() {
                return new ArrayList<>(); // Boş liste
            }
            
            @Override
            public List<Album> getUserAlbums() {
                return new ArrayList<>(); // Boş liste
            }
            
            @Override
            public List<String> getUserArtists() {
                return new ArrayList<>(); // Boş liste
            }
            
            @Override
            public List<Song> getSongsByArtist(String artist) {
                return new ArrayList<>(); // Boş liste
            }
        };
    }
    
    /**
     * Test PlaylistController oluşturur
     */
    private PlaylistController createPlaylistController() {
        return new PlaylistController(createUserController()) {
            // Gerekli metotlar burada override edilecek
        };
    }
    
    /**
     * Test that the view redirects to login menu when no user is logged in
     */
    @Test
    public void testRedirectToLoginWhenNotLoggedIn() {
        // Setup
        UserController notLoggedInController = new UserController() {
            @Override
            public boolean isLoggedIn() {
                return false;
            }
            
            @Override
            public User getCurrentUser() {
                return null;
            }
        };
        
        Scanner scanner = new Scanner("0\n");
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, notLoggedInController, songController, playlistController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue(nextView instanceof LoginMenuView);
    }
    
    /**
     * Test displaying the music collection menu
     */
    @Test
    public void testDisplayMusicCollectionMenu() {
        // Setup
        String input = "0\n";  // Back to Main Menu
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("MUSIC COLLECTION MENU"));
        assertTrue(output.contains("Add Song"));
        assertTrue(output.contains("Add Album"));
        assertTrue(output.contains("Add Artist"));
        assertTrue(output.contains("View Songs"));
        assertTrue(output.contains("View Albums"));
        assertTrue(output.contains("View Artists"));
        assertTrue(output.contains("Delete Song"));
        assertTrue(output.contains("Delete Album"));
        assertTrue(output.contains("Delete Artist"));
        assertTrue(output.contains("Add Song to Album"));
        assertTrue(output.contains("Back to Main Menu"));
    }
    
    /**
     * Test viewing songs
     */
    @Test
    public void testViewSongs() {
        // Setup - option 4 for View Songs
        String input = "4\n\n"; // Select option 4 + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain YOUR SONGS header", output.contains("YOUR SONGS"));
        assertTrue("Output should contain test song 1", output.contains("Test Song 1"));
        assertTrue("Output should contain test song 2", output.contains("Test Song 2"));
    }
    
    /**
     * Test viewing artists
     */
    @Test
    public void testViewArtists() {
        // Setup - option 6 for View Artists
        String input = "6\n\n"; // Select option 6 + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain YOUR ARTISTS header", output.contains("YOUR ARTISTS"));
        assertTrue("Output should contain Test Artist 1", output.contains("Test Artist 1"));
        assertTrue("Output should contain Test Artist 2", output.contains("Test Artist 2"));
        assertTrue("Output should contain Test Artist 3", output.contains("Test Artist 3"));
    }
    
    /**
     * Test adding a new artist
     */
    @Test
    public void testAddArtist() {
        // Setup - option 3 for Add Artist
        String artistName = "New Test Artist";
        String input = "3\n" + artistName + "\n\n"; // Select option 3 + artist name + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ADD NEW ARTIST header", output.contains("ADD NEW ARTIST"));
        assertTrue("Output should show success message", output.contains("Artist '" + artistName + "' added to your library!"));
    }
    
    /**
     * Test adding an album
     */
    @Test
    public void testAddAlbum() {
        // Setup - option 2 for Add Album
        String albumTitle = "New Test Album";
        String albumArtist = "Test Artist 1"; // Existing artist
        String genre = "Rock";
        String year = "2023";
        String addSongs = "n"; // Don't add songs now
        
        // Create input string with all needed inputs
        String input = "2\n" + albumTitle + "\n" + albumArtist + "\n" + genre + "\n" + year + "\n" + addSongs + "\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ADD NEW ALBUM header", output.contains("ADD NEW ALBUM"));
        assertTrue("Output should show success message", output.contains("Album added to your library!"));
    }
    
    /**
     * Test adding a new song
     */
    @Test
    public void testAddSong() {
        // Setup - option 1 for Add Song
        String songTitle = "New Test Song";
        String artistName = "Test Artist 1";
        String albumName = "Test Album 1";
        String genre = "Rock";
        String year = "2023";
        String duration = "03:30";
        
        // Create input string with all needed inputs
        String input = "1\n" + songTitle + "\n" + artistName + "\n" + albumName + "\n" + genre + "\n" + year + "\n" + duration + "\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ADD NEW SONG header", output.contains("ADD NEW SONG"));
        assertTrue("Output should prompt for song title", output.contains("Enter song title"));
        assertTrue("Output should prompt for artist name", output.contains("Enter artist name"));
        assertTrue("Output should prompt for album name", output.contains("Enter album name"));
        assertTrue("Output should prompt for genre", output.contains("Enter genre"));
        assertTrue("Output should prompt for release year", output.contains("Enter release year"));
        assertTrue("Output should prompt for duration", output.contains("Enter duration"));
        assertTrue("Output should show success message", output.contains("Song added successfully!"));
    }
    
    /**
     * Test deleting a song
     */
    @Test
    public void testDeleteSong() {
        // Setup - option 7 for Delete Song
        String input = "7\nTest Song 1\nTest Artist 1\ny\n\n"; // Select option 7 + song title + artist + confirm delete + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain DELETE SONG header", output.contains("DELETE SONG"));
        assertTrue("Output should show success message", output.contains("Song deleted successfully!"));
    }
    
    /**
     * Test viewing albums
     */
    @Test
    public void testViewAlbums() {
        // Setup - option 5 for View Albums
        String input = "5\nn\n\n"; // Select option 5 + don't view details + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain YOUR ALBUMS header", output.contains("YOUR ALBUMS"));
        assertTrue("Output should contain test album", output.contains("Test Album 1"));
        assertTrue("Output should contain album artist", output.contains("Test Artist 1"));
    }
    
    /**
     * Test viewing album details
     */
    @Test
    public void testViewAlbumDetails() {
        // Setup - option 5 for View Albums, then yes to view details
        String input = "5\ny\nTest Album 1\nTest Artist 1\n0\n\n"; // View albums + view details + album title + artist + back option + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ALBUM header", output.contains("ALBUM: Test Album 1"));
        assertTrue("Output should show album artist", output.contains("Artist: Test Artist 1"));
        assertTrue("Output should show album year", output.contains("Year: 2020"));
        assertTrue("Output should show genre", output.contains("Genre: Rock"));
        assertTrue("Output should list album songs", output.contains("Songs in this album"));
        assertTrue("Output should show album song", output.contains("Album Song 1"));
    }
    
    /**
     * Test adding songs to an album
     */
    @Test
    public void testAddSongsToAlbum() {
        // Setup input for adding songs to an album
        String input = "5\ny\nTest Album 1\nTest Artist 1\n1\nTest Song 1\n\n"; // View albums + view details + album info + add songs option + song to add + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ADD SONGS TO ALBUM header", output.contains("ADD SONGS TO ALBUM: Test Album 1"));
        assertTrue("Output should list available songs", output.contains("Your songs:"));
        assertTrue("Output should show song being added", output.contains("Searching for song:"));
    }
    
    /**
     * Test deleting an album
     */
    @Test
    public void testDeleteAlbum() {
        // Setup - option 8 for Delete Album
        String input = "8\nTest Album 1\nTest Artist 1\ny\n\n"; // Select option 8 + album title + artist + confirm + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain DELETE ALBUM header", output.contains("DELETE ALBUM"));
        assertTrue("Output should show delete confirmation", output.contains("You are about to delete album: Test Album 1"));
        assertTrue("Output should show success message", output.contains("Album deleted successfully!"));
    }
    
    /**
     * Test adding a song to an album
     */
    @Test
    public void testAddSongToAlbum() {
        // Setup - option 10 for Add Song to Album
        String input = "10\nTest Album 1\nTest Artist 1\nTest Song 1\nTest Artist 1\n\n"; // Select option 10 + album details + song details + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain ADD SONG TO ALBUM header", output.contains("ADD SONG TO ALBUM"));
        assertTrue("Output should show success message", output.contains("Song added to album successfully!"));
    }
    
    /**
     * Test deleting an artist
     */
    @Test
    public void testDeleteArtist() {
        // Setup - option 9 for Delete Artist
        String input = "9\nTest Artist 1\ny\n\n"; // Select option 9 + artist name + confirm + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain DELETE ARTIST header", output.contains("DELETE ARTIST"));
        assertTrue("Output should show delete confirmation", output.contains("You are about to delete artist: Test Artist 1"));
        assertTrue("Output should show success message", output.contains("Artist deleted successfully!"));
    }
    
    /**
     * Test viewing songs by an artist
     */
    @Test
    public void testViewArtistSongs() {
        // Setup - option 6 for View Artists, then select artist 1
        String input = "6\n1\n\n"; // View artists + select first artist + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain SONGS BY header", output.contains("SONGS BY TEST ARTIST 1"));
        assertTrue("Output should list artist songs", output.contains("Artist Song 1"));
        assertTrue("Output should list artist songs", output.contains("Artist Song 2"));
    }
    
    /**
     * Test adding a song with failure
     */
    @Test
    public void testAddSongFailure() {
        // Setup - option 1 for Add Song with a failing song
        String songTitle = "FailSong";  // SongController'da bu isim için null dönecek
        String artistName = "Test Artist 1";
        String albumName = "Test Album 1";
        String genre = "Rock";
        String year = "2023";
        String duration = "03:30";
        
        // Create input string with all needed inputs
        String input = "1\n" + songTitle + "\n" + artistName + "\n" + albumName + "\n" + genre + "\n" + year + "\n" + duration + "\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show error message", output.contains("Failed to add the song"));
    }
    
    /**
     * Test adding an artist with failure
     */
    @Test
    public void testAddArtistFailure() {
        // Setup - option 3 for Add Artist with a failing artist
        String artistName = "FailArtist";  // SongController'da bu isim için false dönecek
        String input = "3\n" + artistName + "\n\n"; 
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show error message", output.contains("Failed to add artist"));
    }
    
    /**
     * Test adding an album with failure
     */
    @Test
    public void testAddAlbumFailure() {
        // Setup - option 2 for Add Album with a failing album
        String albumTitle = "FailAlbum";  // SongController'da bu isim için null dönecek
        String albumArtist = "Test Artist 1";
        String genre = "Rock";
        String year = "2023";
        
        // Create input string with all needed inputs - add more newlines
        String input = "2\n" + albumTitle + "\n" + albumArtist + "\n" + genre + "\n" + year + "\n\n\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show error message", output.contains("Failed to add album"));
    }
    
    /**
     * Test for trying to add an album with a non-existent artist
     */
    @Test
    public void testAddAlbumWithNonExistentArtist() {
        // Override getArtists for this test
        SongController mockController = new SongController(createUserController()) {
            @Override
            public List<String> getArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2");
            }
            
            @Override
            public boolean addArtist(String artistName) {
                return true;
            }
        };
        
        // Setup - option 2 for Add Album
        String albumTitle = "New Test Album";
        String albumArtist = "Non-existent Artist"; // Sanatçı listede yok
        String addArtist = "n"; // No, don't add artist
        
        // Create input string with all needed inputs - add more newlines
        String input = "2\n" + albumTitle + "\n" + albumArtist + "\n" + addArtist + "\n\n\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, mockController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show artist not exist error", output.contains("does not exist in your library"));
        assertTrue("Output should show cancel message", output.contains("Album creation canceled"));
    }
    
    /**
     * Test deleting a song that doesn't exist
     */
    @Test
    public void testDeleteNonExistentSong() {
        // Setup - option 7 for Delete Song
        String title = "Non-existent Song";
        String artist = "Non-existent Artist";
        String input = "7\n" + title + "\n" + artist + "\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show song not found error", output.contains("Song not found"));
    }
    
    /**
     * Test deleting an album that doesn't exist
     */
    @Test
    public void testDeleteNonExistentAlbum() {
        // Setup - option 8 for Delete Album
        String albumTitle = "Non-existent Album";
        String albumArtist = "Non-existent Artist";
        String input = "8\n" + albumTitle + "\n" + albumArtist + "\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show album not found error", output.contains("Album not found"));
    }
    
    /**
     * Test deletion failure of an existing album
     */
    @Test
    public void testDeleteAlbumFailure() {
        // Modify album ID to trigger failure
        SongController mockController = new SongController(createUserController()) {
            @Override
            public List<Album> getUserAlbums() {
                List<Album> albums = new ArrayList<>();
                Album album = new Album("Test Album 1", "Test Artist 1", 2020, "Rock", 1);
                album.setId(999); // ID that will fail when deleted
                albums.add(album);
                return albums;
            }
            
            @Override
            public boolean deleteAlbum(int albumId) {
                return albumId != 999; // Return false for ID 999
            }
        };
        
        // Setup - option 8 for Delete Album
        String input = "8\nTest Album 1\nTest Artist 1\ny\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, mockController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show failure message", output.contains("Failed to delete album"));
    }
    
    /**
     * Test deleting a non-existent artist
     */
    @Test
    public void testDeleteNonExistentArtist() {
        // Setup - option 9 for Delete Artist
        String artistName = "Non-existent Artist";
        String input = "9\n" + artistName + "\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show artist not found error", output.contains("Artist not found"));
    }
    
    /**
     * Test deletion failure of default artist 
     */
    @Test
    public void testDeleteDefaultArtistFailure() {
        // Setup - option 9 for Delete Artist
        String artistName = "Default Artist"; // This name is set to fail in mock controller
        String input = "9\n" + artistName + "\ny\n\n";
        
        // Create special controller for this test
        SongController mockController = new SongController(createUserController()) {
            @Override
            public List<String> getArtists() {
                return Arrays.asList("Test Artist 1", "Default Artist");
            }
            
            @Override
            public boolean deleteArtist(String artistName) {
                return !"Default Artist".equals(artistName);
            }
        };
        
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, mockController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show delete failure message", output.contains("Failed to delete artist. Default artists cannot be deleted"));
    }
    
    /**
     * Test adding song to album failure
     */
    @Test
    public void testAddSongToAlbumFailure() {
        // Create special controller for this test
        SongController mockController = new SongController(createUserController()) {
            @Override
            public List<Album> getUserAlbums() {
                List<Album> albums = new ArrayList<>();
                Album album = new Album("Test Album 1", "Test Artist 1", 2020, "Rock", 1);
                album.setId(999); // ID that will fail
                albums.add(album);
                return albums;
            }
            
            @Override
            public List<Song> getUserSongs() {
                List<Song> songs = new ArrayList<>();
                User user = new User();
                user.setId(1);
                Song song = new Song("Test Song 1", "Test Artist 1", "Test Album 1", "Rock", 2020, 180, "music/Test.mp3", user);
                song.setId(1);
                songs.add(song);
                return songs;
            }
            
            @Override
            public boolean addSongToAlbum(int albumId, int songId) {
                return albumId != 999; // Fail for album ID 999
            }
        };
        
        // Setup - option 10 for Add Song to Album
        String input = "10\nTest Album 1\nTest Artist 1\nTest Song 1\nTest Artist 1\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, mockController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show failure message", output.contains("Failed to add song to album"));
    }
    
    /**
     * Test adding song to album with non-existent song
     */
    @Test
    public void testAddNonExistentSongToAlbum() {
        // Setup - option 10 for Add Song to Album
        String input = "10\nTest Album 1\nTest Artist 1\nNon-existent Song\nNon-existent Artist\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show song not found error", output.contains("Song not found"));
    }
    
    /**
     * Test view songs with empty library
     */
    @Test
    public void testViewSongsWithEmptyLibrary() {
        // Setup - option 4 for View Songs
        String input = "4\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no songs message", output.contains("You don't have any songs in your library yet"));
    }
    
    /**
     * Test view artists with empty library
     */
    @Test
    public void testViewArtistsWithEmptyLibrary() {
        // Setup - option 6 for View Artists
        String input = "6\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no artists message", output.contains("You don't have any artists in your library yet"));
    }
    
    /**
     * Test view albums with empty library
     */
    @Test
    public void testViewAlbumsWithEmptyLibrary() {
        // Setup - option 5 for View Albums
        String input = "5\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no albums message", output.contains("You don't have any albums in your library yet"));
    }
    
    /**
     * Test delete song with empty library
     */
    @Test
    public void testDeleteSongWithEmptyLibrary() {
        // Setup - option 7 for Delete Song
        String input = "7\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no songs to delete message", output.contains("You don't have any songs to delete"));
    }
    
    /**
     * Test delete album with empty library
     */
    @Test
    public void testDeleteAlbumWithEmptyLibrary() {
        // Setup - option 8 for Delete Album
        String input = "8\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no albums to delete message", output.contains("You don't have any albums to delete"));
    }
    
    /**
     * Test delete artist with empty library
     */
    @Test
    public void testDeleteArtistWithEmptyLibrary() {
        // Setup - option 9 for Delete Artist
        String input = "9\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no artists to delete message", output.contains("No artists to delete"));
    }
    
    /**
     * Test add song to album with empty library
     */
    @Test
    public void testAddSongToAlbumWithEmptyLibrary() {
        // Setup - option 10 for Add Song to Album
        String input = "10\n\n";
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no albums message", output.contains("You don't have any albums to add songs to"));
    }
    
    /**
     * Test view non-existent artist songs
     */
    @Test
    public void testViewNonExistentArtistSongs() {
        // Create special controller that returns empty song list
        SongController mockController = new SongController(createUserController()) {
            @Override
            public List<String> getUserArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2");
            }
            
            @Override
            public List<Song> getSongsByArtist(String artist) {
                return new ArrayList<>(); // Return empty list
            }
        };
        
        // Setup - option 6 for View Artists, then select artist 1
        String input = "6\n1\n\n"; // View artists + select first artist + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        MusicCollectionView view = new MusicCollectionView(scanner, userController, mockController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show no songs found message", output.contains("No songs found for artist"));
    }
} 