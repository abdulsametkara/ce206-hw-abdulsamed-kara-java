package com.samet.music;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.view.MusicCollectionView;
import com.samet.music.view.MenuView;
import com.samet.music.view.LoginMenuView;
import com.samet.music.view.MainMenuView;

/**
 * Test class for MusicCollectionView
 */
public class MusicCollectionViewTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private UserController userController;
    private SongController songController;
    private PlaylistController playlistController;
    private User testUser;

    @Before
    public void setUp() {
        // Standart output akışını yakalama
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        // Test kullanıcısı oluşturma
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Controller'ları oluşturma
        userController = createUserController();
        songController = createSongController();
        playlistController = createPlaylistController();
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * Test UserController oluşturur
     */
    private UserController createUserController() {
        return new UserController() {
            @Override
            public boolean isLoggedIn() {
                return true;
            }

            @Override
            public User getCurrentUser() {
                return testUser;
            }
        };
    }
    
    /**
     * Test SongController oluşturur
     */
    private SongController createSongController() {
        return new SongController(userController) {
            @Override
            public List<String> getArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2", "Test Artist 3");
            }
            
            @Override
            public List<Song> getUserSongs() {
                List<Song> songs = new ArrayList<>();
                Song song1 = new Song("Test Song 1", "Test Artist 1", "Test Album 1", "Rock", 2020, 180, "music/Test.mp3", 1);
                song1.setId(1);
                songs.add(song1);
                
                Song song2 = new Song("Test Song 2", "Test Artist 2", "Test Album 2", "Pop", 2021, 240, "music/Test2.mp3", 1);
                song2.setId(2);
                songs.add(song2);
                
                return songs;
            }
            
            @Override
            public List<String> getUserArtists() {
                return Arrays.asList("Test Artist 1", "Test Artist 2", "Test Artist 3");
            }
            
            @Override
            public boolean addArtist(String artistName) {
                return !"FailArtist".equals(artistName);
            }
            
            @Override
            public Song addSong(String title, String artist, String album, String genre, int year, int duration, String filePath) {
                if ("FailSong".equals(title)) {
                    return null;
                }
                Song song = new Song(title, artist, album, genre, year, duration, filePath, 1);
                song.setId(3);
                return song;
            }
            
            @Override
            public boolean deleteSong(int songId) {
                return songId != 999;
            }
            
            @Override
            public List<Album> getUserAlbums() {
                List<Album> albums = new ArrayList<>();
                Album album = new Album("Test Album 1", "Test Artist 1", 2020, "Rock", 1);
                album.setId(1);
                
                List<Song> albumSongs = new ArrayList<>();
                Song song1 = new Song("Album Song 1", "Test Artist 1", "Test Album 1", "Rock", 2020, 180, "music/Test.mp3", 1);
                song1.setId(1);
                albumSongs.add(song1);
                album.setSongs(albumSongs);
                
                albums.add(album);
                return albums;
            }
            
            @Override
            public Album addAlbum(String title, String artist, int year, String genre) {
                if ("FailAlbum".equals(title)) {
                    return null;
                }
                Album album = new Album(title, artist, year, genre, 1);
                album.setId(2);
                return album;
            }
            
            @Override
            public boolean addSongToAlbum(int albumId, int songId) {
                return !(albumId == 999 || songId == 999);
            }

            @Override
            public List<Song> getSongsByArtist(String artistName) {
                if ("Test Artist 1".equals(artistName)) {
                    List<Song> songs = new ArrayList<>();
                    Song song1 = new Song("Artist Song 1", artistName, "Test Album", "Rock", 2020, 180, "music/Test.mp3", 1);
                    song1.setId(1);
                    Song song2 = new Song("Artist Song 2", artistName, "Test Album", "Rock", 2020, 180, "music/Test2.mp3", 1);
                    song2.setId(2);
                    songs.add(song1);
                    songs.add(song2);
                    return songs;
                }
                return new ArrayList<>();
            }
        };
    }
    
    /**
     * Özel başarısız SongController oluştur
     */
    private SongController createEmptySongController() {
        return new SongController(userController) {
            @Override
            public List<String> getArtists() {
                return new ArrayList<>();
            }
            
            @Override
            public List<Song> getUserSongs() {
                return new ArrayList<>();
            }
            
            @Override
            public List<Album> getUserAlbums() {
                return new ArrayList<>();
            }
            
            @Override
            public List<String> getUserArtists() {
                return new ArrayList<>();
            }
            
            @Override
            public List<Song> getSongsByArtist(String artist) {
                return new ArrayList<>();
            }
        };
    }
    
    /**
     * Test PlaylistController oluşturur
     */
    private PlaylistController createPlaylistController() {
        return new PlaylistController(userController);
    }
    
    @Test
    public void testDisplayMusicCollectionMenu() {
        // Çıkış için sadece "0" giriyoruz
        String input = "0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        assertTrue("Menü başlığı gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
        assertTrue("Add Song seçeneği gösterilmeli", output.contains("Add Song"));
        assertTrue("Back to Main Menu seçeneği gösterilmeli", output.contains("Back to Main Menu"));
    }
    
    @Test
    public void testAddSong() {
        // Normal girdi ve çıkış için "0"
        String input = "1\nNew Song\nTest Artist 1\nTest Album\nRock\n2022\n3:00\n0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        assertTrue("Şarkı başarıyla eklendiğine dair mesaj gösterilmeli", output.contains("Song added successfully"));
    }
    
    @Test
    public void testAddSongFail() {
        // Başarısız şarkı ekleme testi
        String input = "1\nFailSong\nTest Artist 1\nTest Album\nRock\n2022\n3:00\n0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        assertTrue("Şarkı eklenemedi mesajı gösterilmeli", output.contains("Failed to add the song"));
    }
    
    @Test
    public void testViewSongs() {
        // Şarkıları görüntüleme testi
        String input = "4\n0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        assertTrue("Şarkı listesi başlığı gösterilmeli", output.contains("YOUR SONGS"));
    }
    
    @Test
    public void testDeleteSong() {
        // Şarkı silme testi - Test Song 1 siliniyor (id=1)
        String input = "7\nTest Song 1\nTest Artist 1\ny\n0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        // Metni tamamen doğru şekilde kontrolü
        assertTrue("Şarkı silindi mesajı gösterilmeli", output.contains("Song deleted successfully!"));
    }
    
    @Test
    public void testDeleteSongCancel() {
        // Şarkı silme iptal testi
        String input = "7\nTest Song 1\nTest Artist 1\nn\n0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        view.display();
        
        String output = outputStream.toString();
        // Metni tamamen doğru şekilde kontrolü
        assertTrue("Silme işlemi iptal edildi mesajı gösterilmeli", output.contains("Deletion cancelled."));
    }
    
    @Test
    public void testBackToMainMenu() {
        // Ana menüye dönüş testi
        String input = "0";
        Scanner scanner = new Scanner(input);
        MusicCollectionView view = new MusicCollectionView(scanner, userController, songController, playlistController);
        MenuView result = view.display();
        assertTrue("MainMenuView'a yönlendirilmeli", result instanceof MainMenuView);
    }
} 