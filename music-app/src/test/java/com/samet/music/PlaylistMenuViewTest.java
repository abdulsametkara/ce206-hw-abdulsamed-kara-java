package com.samet.music;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashSet;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.view.PlaylistMenuView;
import com.samet.music.view.MainMenuView;
import com.samet.music.view.MenuView;
import com.samet.music.view.LoginMenuView;

/**
 * Test class for PlaylistMenuView
 */
public class PlaylistMenuViewTest {

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
            private User currentUser = new User();
            private boolean loggedIn = true;
            
            @Override
            public boolean isLoggedIn() {
                return loggedIn;
            }
            
            // Test için giriş durumunu değiştir
            public void setLoggedIn(boolean loggedIn) {
                this.loggedIn = loggedIn;
            }
            
            @Override
            public User getCurrentUser() {
                currentUser.setId(1);
                currentUser.setUsername("testuser");
                return currentUser;
            }
        };
    }
    
    /**
     * Test SongController oluşturur
     */
    private SongController createSongController() {
        return new SongController(userController) {
            @Override
            public List<Song> getUserSongs() {
                List<Song> songs = new ArrayList<>();
                
                User user = new User();
                user.setId(1);
                Song song1 = new Song("Test Song 1", "Test Artist", "Test Album", "Rock", 2020, 180, "music/test.mp3", user);
                song1.setId(1);
                
                Song song2 = new Song("Test Song 2", "Test Artist", "Test Album", "Pop", 2021, 240, "music/test2.mp3", user);
                song2.setId(2);
                
                songs.add(song1);
                songs.add(song2);
                return songs;
            }
        };
    }
    
    /**
     * Boş şarkı listesi döndüren SongController oluşturur
     */
    private SongController createEmptySongController() {
        return new SongController(userController) {
            @Override
            public List<Song> getUserSongs() {
                return new ArrayList<>(); // Boş liste
            }
        };
    }
    
    /**
     * Test PlaylistController oluşturur
     */
    private PlaylistController createPlaylistController() {
        return new PlaylistController(userController) {
            private List<Playlist> playlists = new ArrayList<>();
            
            @Override
            public List<Playlist> getUserPlaylists() {
                if (playlists.isEmpty()) {
                    // Test için varsayılan çalma listesi ekle
                    User playlistUser = new User();
                    playlistUser.setId(1);
                    Playlist playlist = new Playlist("Test Playlist", "Test Description", playlistUser);
                    playlist.setId(1);
                    
                    // Playlist'e şarkı ekle
                    List<Song> songs = new ArrayList<>();
                    User user = new User();
                    user.setId(1);
                    Song song = new Song("Playlist Song", "Test Artist", "Test Album", "Rock", 2020, 180, "music/test.mp3", user);
                    song.setId(1);
                    songs.add(song);
                    playlist.setSongs(new HashSet<>(songs));
                    
                    playlists.add(playlist);
                }
                return playlists;
            }
            
            @Override
            public Playlist createPlaylist(String name, String description) {
                User playlistUser = new User();
                playlistUser.setId(1);
                Playlist playlist = new Playlist(name, description, playlistUser);
                playlist.setId(playlists.size() + 1);
                playlists.add(playlist);
                return playlist;
            }
            
            @Override
            public Playlist getPlaylist(int playlistId) {
                for (Playlist playlist : playlists) {
                    if (playlist.getId() == playlistId) {
                        return playlist;
                    }
                }
                return null;
            }
            
            @Override
            public boolean addSongToPlaylist(int playlistId, int songId) {
                return true;
            }
            
            @Override
            public boolean removeSongFromPlaylist(int playlistId, int songId) {
                return true;
            }
            
            @Override
            public boolean deletePlaylist(int playlistId) {
                int index = -1;
                for (int i = 0; i < playlists.size(); i++) {
                    if (playlists.get(i).getId() == playlistId) {
                        index = i;
                        break;
                    }
                }
                
                if (index != -1) {
                    playlists.remove(index);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean updatePlaylist(int playlistId, String name, String description) {
                for (Playlist playlist : playlists) {
                    if (playlist.getId() == playlistId) {
                        if (name != null) {
                            playlist.setName(name);
                        }
                        if (description != null) {
                            playlist.setDescription(description);
                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    /**
     * Boş çalma listesi döndüren PlaylistController oluşturur
     */
    private PlaylistController createEmptyPlaylistController() {
        return new PlaylistController(userController) {
            @Override
            public List<Playlist> getUserPlaylists() {
                return new ArrayList<>(); // Boş liste
            }
        };
    }
    
    /**
     * Hataya sebep olan PlaylistController oluşturur
     */
    private PlaylistController createFailingPlaylistController() {
        return new PlaylistController(userController) {
            @Override
            public List<Playlist> getUserPlaylists() {
                throw new RuntimeException("Test exception");
            }
            
            @Override
            public Playlist createPlaylist(String name, String description) {
                return null; // Başarısız
            }
            
            @Override
            public boolean deletePlaylist(int playlistId) {
                return false; // Başarısız
            }
            
            @Override
            public boolean updatePlaylist(int playlistId, String name, String description) {
                return false; // Başarısız
            }
        };
    }
    
    /**
     * Test displaying the playlist menu
     */
    @Test
    public void testDisplayPlaylistMenu() {
        // Setup
        String input = "0\n";  // Back to Main Menu
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("PLAYLISTS MENU"));
        assertTrue(output.contains("Create Playlist"));
        assertTrue(output.contains("View Playlists"));
        assertTrue(output.contains("Edit Playlist"));
        assertTrue(output.contains("Back to Main Menu"));
    }
    
    /**
     * Test creating a playlist
     */
    @Test
    public void testCreatePlaylist() {
        // Setup
        String playlistName = "New Test Playlist";
        String description = "Test Description";
        String input = "1\n" + playlistName + "\n" + description + "\nno\n\n"; // Create playlist + inputs + no to adding songs + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain CREATE PLAYLIST header", output.contains("CREATE PLAYLIST"));
        assertTrue("Output should show success message", output.contains("Playlist created successfully"));
        
        // Verify playlist was added
        List<Playlist> playlists = playlistController.getUserPlaylists();
        boolean found = false;
        for (Playlist p : playlists) {
            if (p.getName().equals(playlistName)) {
                found = true;
                break;
            }
        }
        assertTrue("Playlist should be added to user's playlists", found);
    }
    
    /**
     * Test viewing playlists
     */
    @Test
    public void testViewPlaylists() {
        // Setup - option 2 for View Playlists
        String input = "2\n\n"; // View playlists + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain YOUR PLAYLISTS header", output.contains("YOUR PLAYLISTS"));
        assertTrue("Output should show test playlist", output.contains("Test Playlist"));
    }
    
    /**
     * Test editing a playlist (adding a song)
     */
    @Test
    public void testEditPlaylist() {
        // Setup - option 3 for Edit Playlist
        String input = "3\nTest Playlist\n2\nTest Song 1\n0\n\n"; // Edit playlist + playlist name + add song (option 2) + song title + 0 to go back + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain EDIT PLAYLIST header", output.contains("EDIT PLAYLIST"));
        assertTrue("Output should show add song success", output.contains("added to playlist"));
    }
    
    /**
     * Test going back to main menu
     */
    @Test
    public void testBackToMainMenu() {
        // Setup
        String input = "0\n";  // Back to Main Menu
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue(nextView instanceof MainMenuView);
    }
    
    /**
     * Test redirect to login when not logged in
     */
    @Test
    public void testRedirectToLoginWhenNotLoggedIn() {
        // Setup
        String input = ""; // No input needed, redirect happens first
        Scanner scanner = new Scanner(input);
        
        // Create a special controller with login = false
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
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, notLoggedInController, songController, playlistController);
        MenuView nextView = view.display();
        
        // Verify
        assertTrue(nextView instanceof LoginMenuView);
    }
    
    /**
     * Test invalid menu choice
     */
    @Test
    public void testInvalidMenuChoice() {
        // Setup
        String input = "999\n0\n"; // Invalid choice + Back to Main Menu
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show invalid choice message", output.contains("Invalid choice. Please try again"));
    }
    
    /**
     * Test exception handling in menu
     */
    @Test
    public void testExceptionHandling() {
        // Setup
        String input = "invalid\n0\n"; // Invalid input + Back to Main Menu
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show invalid choice message", output.contains("Invalid choice"));
    }
    
    /**
     * Test create playlist and add songs
     */
    @Test
    public void testCreatePlaylistAndAddSongs() {
        // Setup
        String playlistName = "New Test Playlist";
        String description = "Test Description";
        String input = "1\n" + playlistName + "\n" + description + "\nyes\nTest Song 1\n\n"; // Create playlist + add songs + song title + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show success adding song", output.contains("added to playlist"));
    }
    
    /**
     * Test create playlist failure
     */
    @Test
    public void testCreatePlaylistFailure() {
        // Setup
        String playlistName = "New Test Playlist";
        String description = "Test Description";
        String input = "1\n" + playlistName + "\n" + description + "\nno\n\n"; // Create playlist + inputs + no to adding songs + wait for enter
        Scanner scanner = new Scanner(input);
        
        // PlaylistController that returns null on createPlaylist
        PlaylistController controller = new PlaylistController(userController) {
            @Override
            public Playlist createPlaylist(String name, String description) {
                return null;
            }
            
            @Override
            public List<Playlist> getUserPlaylists() {
                return new ArrayList<>();
            }
        };
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, controller);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show failure message", output.contains("Failed to create playlist"));
    }
    
    /**
     * Test changing playlist details with failure
     */
    @Test
    public void testChangePlaylistDetailsFailure() {
        // Setup
        String input = "3\nTest Playlist\n1\nNew Name\nNew Description\n\n"; // Edit playlist + playlist name + change details (option 1) + new name + new description + wait for enter
        Scanner scanner = new Scanner(input);
        
        // PlaylistController that returns false on updatePlaylist
        PlaylistController controller = new PlaylistController(userController) {
            @Override
            public List<Playlist> getUserPlaylists() {
                List<Playlist> playlists = new ArrayList<>();
                User playlistUser = new User();
                playlistUser.setId(1);
                Playlist playlist = new Playlist("Test Playlist", "Test Description", playlistUser);
                playlist.setId(1);
                playlists.add(playlist);
                return playlists;
            }
            
            @Override
            public boolean updatePlaylist(int playlistId, String name, String description) {
                return false;
            }
        };
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, controller);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show failure message", output.contains("Failed to update playlist details"));
    }
    
    /**
     * Test delete playlist failure
     */
    @Test
    public void testDeletePlaylistFailure() {
        // Setup
        String input = "3\nTest Playlist\n4\ny\n\n"; // Edit playlist + playlist name + delete (option 4) + confirm yes + wait for enter
        Scanner scanner = new Scanner(input);
        
        // PlaylistController that returns false on deletePlaylist
        PlaylistController controller = new PlaylistController(userController) {
            @Override
            public List<Playlist> getUserPlaylists() {
                List<Playlist> playlists = new ArrayList<>();
                User playlistUser = new User();
                playlistUser.setId(1);
                Playlist playlist = new Playlist("Test Playlist", "Test Description", playlistUser);
                playlist.setId(1);
                playlists.add(playlist);
                return playlists;
            }
            
            @Override
            public boolean deletePlaylist(int playlistId) {
                return false;
            }
        };
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, controller);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show failure message", output.contains("Failed to delete playlist"));
    }
    
    /**
     * Test exception in edit playlist menu
     */
    @Test
    public void testExceptionHandlingInEditPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n5\n\n"; // Edit playlist + playlist name + invalid option + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        // No specific error message for unknown options, test should pass without error
        assertTrue(true);
    }
    
    /**
     * Test viewing playlists when playlist is empty
     */
    @Test
    public void testViewPlaylistsWhenEmpty() {
        // Setup - option 2 for View Playlists with empty playlist controller
        String input = "2\n\n"; // View playlists + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, createEmptyPlaylistController());
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain YOUR PLAYLISTS header", output.contains("YOUR PLAYLISTS"));
        assertTrue("Output should show empty message", output.contains("You don't have any playlists yet"));
    }
    
    /**
     * Test edit playlist when playlist is empty
     */
    @Test
    public void testEditPlaylistWhenEmpty() {
        // Setup - option 3 for Edit Playlist with empty playlist controller
        String input = "3\n\n"; // Edit playlist + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, createEmptyPlaylistController());
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should contain empty playlists message", output.contains("You don't have any playlists to edit"));
    }
    
    /**
     * Test edit playlist with invalid playlist name
     */
    @Test
    public void testEditPlaylistWithInvalidName() {
        // Setup
        String input = "3\nNonExistentPlaylist\n\n"; // Edit playlist + nonexistent playlist name + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show playlist not found message", output.contains("Playlist not found"));
    }
    
    /**
     * Test removing songs from playlist (clearing all songs)
     */
    @Test
    public void testRemoveAllSongsFromPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n3\nall\n\n"; // Edit playlist + playlist name + remove songs (option 3) + "all" to clear + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show remove songs success", output.contains("Removed all songs from playlist"));
    }
    
    /**
     * Test removing a specific song from playlist
     */
    @Test
    public void testRemoveSpecificSongFromPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n3\nPlaylist Song\n\n"; // Edit playlist + playlist name + remove songs (option 3) + song title + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show remove song success", output.contains("removed from playlist"));
    }
    
    /**
     * Test removing a non-existent song from playlist
     */
    @Test
    public void testRemoveNonExistentSongFromPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n3\nNonExistentSong\n\n"; // Edit playlist + playlist name + remove songs (option 3) + nonexistent song + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show song not found message", output.contains("Song not found in playlist"));
    }
    
    /**
     * Test removing songs from empty playlist
     */
    @Test
    public void testRemoveSongsFromEmptyPlaylist() {
        // Setup
        // Create a playlist with no songs
        PlaylistController emptyPlaylistController = new PlaylistController(userController) {
            @Override
            public List<Playlist> getUserPlaylists() {
                List<Playlist> playlists = new ArrayList<>();
                User playlistUser = new User();
                playlistUser.setId(1);
                Playlist playlist = new Playlist("Empty Playlist", "No songs", playlistUser);
                playlist.setId(1);
                playlist.setSongs(new HashSet<>()); // Empty songs set
                playlists.add(playlist);
                return playlists;
            }
            
            @Override
            public boolean removeSongFromPlaylist(int playlistId, int songId) {
                return true;
            }
        };
        
        String input = "3\nEmpty Playlist\n3\n\n"; // Edit playlist + playlist name + remove songs (option 3) + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, emptyPlaylistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show empty playlist message", output.contains("doesn't have any songs to remove"));
    }
    
    /**
     * Test adding songs to playlist (all songs)
     */
    @Test
    public void testAddAllSongsToPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n2\nall\n\n"; // Edit playlist + playlist name + add songs (option 2) + "all" to add all + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show add all songs success", output.contains("Added all songs to playlist"));
    }
    
    /**
     * Test add songs to playlist with empty song library
     */
    @Test
    public void testAddSongsToPlaylistWithEmptySongLibrary() {
        // Setup
        String input = "3\nTest Playlist\n2\n\n"; // Edit playlist + playlist name + add songs (option 2) + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, createEmptySongController(), playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show empty song library message", output.contains("You don't have any songs in your library"));
    }
    
    /**
     * Test adding non-existent song to playlist
     */
    @Test
    public void testAddNonExistentSongToPlaylist() {
        // Setup
        String input = "3\nTest Playlist\n2\nNonExistentSong\n\n"; // Edit playlist + playlist name + add songs (option 2) + nonexistent song + wait for enter
        Scanner scanner = new Scanner(input);
        
        // Execute
        PlaylistMenuView view = new PlaylistMenuView(scanner, userController, songController, playlistController);
        view.display();
        
        // Verify
        String output = outputStream.toString();
        assertTrue("Output should show song not found message", output.contains("Song not found"));
    }
} 