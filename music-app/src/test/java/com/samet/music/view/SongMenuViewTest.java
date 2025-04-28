package com.samet.music.view;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.User;

/**
 * SongMenuView için test sınıfı
 */
public class SongMenuViewTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    private SongMenuView songMenuView;
    private MockUserController userController;
    private MockSongController songController;
    private MockPlaylistController playlistController;
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
        
        // Mock controller'lar oluşturma
        userController = new MockUserController();
        songController = new MockSongController();
        playlistController = new MockPlaylistController();
        
        // Kullanıcı oturumunu ayarlama
        userController.setLoggedIn(true);
        userController.setCurrentUser(testUser);
    }
    
    @After
    public void tearDown() {
        // Standart output akışını geri yükleme
        System.setOut(originalOut);
    }
    
    /**
     * Oturum açılmamış durumu test eder
     */
    @Test
    public void testNotLoggedIn() {
        // Kullanıcı oturumunu kapatma
        userController.setLoggedIn(false);
        
        // Test için scanner oluşturma
        Scanner scanner = new Scanner("");
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        MenuView resultView = songMenuView.display();
        
        // LoginMenuView'a yönlendirildiğini doğrulama
        assertTrue("Oturum açılmamışsa LoginMenuView'a yönlendirilmeli", resultView instanceof LoginMenuView);
    }
    
    /**
     * Boş şarkı listesi durumunu test eder
     */
    @Test
    public void testEmptySongList() {
        // Boş şarkı listesi
        songController.setUserSongs(new ArrayList<>());
        
        // Ana menüye dönüş
        String input = "0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Boş liste mesajı gösterilmeli", output.contains("You don't have any songs in your library yet"));
    }
    
    /**
     * Şarkı listesinin doğru gösterilmesini test eder
     */
    @Test
    public void testDisplayUserSongs() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song 1", "Test Artist 1", "Test Album 1", "Rock", 2021, 180, "path/to/file1", songUser);
        song1.setId(1);
        songs.add(song1);
        
        Song song2 = new Song("Test Song 2", "Test Artist 2", "Test Album 2", "Pop", 2022, 240, "path/to/file2", songUser);
        song2.setId(2);
        songs.add(song2);
        
        songController.setUserSongs(songs);
        
        // Ana menüye dönüş
        String input = "0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("İlk şarkı gösterilmeli", output.contains("Test Song 1"));
        assertTrue("İkinci şarkı gösterilmeli", output.contains("Test Song 2"));
        assertTrue("İlk sanatçı gösterilmeli", output.contains("Test Artist 1"));
        assertTrue("İkinci sanatçı gösterilmeli", output.contains("Test Artist 2"));
    }
    
    /**
     * Geçersiz menü seçimi test eder
     */
    @Test
    public void testInvalidMenuChoice() {
        // Geçersiz seçim, ana menüye dönüş
        String input = "99\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz seçim mesajı gösterilmeli", output.contains("Invalid choice"));
    }
    
    /**
     * Yeni şarkı ekleme işlemini test eder
     */
    @Test
    public void testAddSong() {
        // Şarkı ekle (1), gerekli bilgileri gir, ana menüye dönüş
        String input = "1\nNew Song\nNew Artist\nNew Album\nPop\n2023\n3:30\nmock\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        songController.setUserSongs(songs);
        
        // Şarkı ekleme başarılı olacak
        songController.setAddSongSuccess(true);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı ekleme başlığı gösterilmeli", output.contains("ADD NEW SONG"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Song added to your library"));
    }
    
    /**
     * Geçersiz süre formatı ile şarkı ekleme işlemini test eder
     */
    @Test
    public void testAddSongInvalidDuration() {
        // Şarkı ekle (1), geçersiz süre formatı, ana menüye dönüş
        String input = "1\nNew Song\nNew Artist\nNew Album\nPop\n2023\ninvalid\nmock\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        songController.setUserSongs(songs);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz süre formatı hatası gösterilmeli", output.contains("Invalid duration format"));
    }
    
    /**
     * Şarkı ekleme başarısız olma durumunu test eder
     */
    @Test
    public void testAddSongFailure() {
        // Şarkı ekle (1), gerekli bilgileri gir, ana menüye dönüş
        String input = "1\nNew Song\nNew Artist\nNew Album\nPop\n2023\n3:30\nmock\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Şarkı ekleme başarısız olacak
        songController.setAddSongSuccess(false);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı ekleme hatası gösterilmeli", output.contains("Failed to add song"));
    }
    
    /**
     * Şarkı düzenleme işlemini test eder
     */
    @Test
    public void testEditSong() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı düzenle (2), şarkı seç (1), yeni değerler
        String input = "2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\n2022\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı düzenleme başlığı gösterilmeli", output.contains("EDIT SONG"));
        assertTrue("Mevcut şarkı bilgileri gösterilmeli", output.contains("Test Song"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Song updated successfully"));
    }
    
    /**
     * Şarkı düzenleme başarısız olma durumunu test eder
     */
    @Test
    public void testEditSongFailure() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı güncellemeyi başarısız yap
        songController.setUpdateSongSuccess(false);
        
        // Şarkı düzenle (2), şarkı seç (1), yeni değerler
        String input = "2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\n2022\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı güncelleme hatası gösterilmeli", output.contains("Failed to update song"));
    }
    
    /**
     * Geçersiz yıl formatı ile şarkı düzenleme işlemini test eder
     */
    @Test
    public void testEditSongInvalidYear() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı düzenle (2), şarkı seç (1), geçersiz yıl formatı
        String input = "2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\ninvalid\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz yıl formatı hatası gösterilmeli", output.contains("Invalid year format"));
    }
    
    /**
     * Boş şarkı listesi ile düzenleme yapmayı test eder
     */
    @Test
    public void testEditSongEmptyList() {
        // Boş şarkı listesi
        songController.setUserSongs(new ArrayList<>());
        
        // Şarkı düzenle (2), ana menüye dönüş
        String input = "2\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Boş liste uyarısı gösterilmeli", output.contains("You don't have any songs to edit"));
    }
    
    /**
     * Şarkı silme işlemini test eder
     */
    @Test
    public void testDeleteSong() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı sil (3), şarkı seç (1), onaylama (y)
        String input = "3\n1\ny\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı silme başlığı gösterilmeli", output.contains("DELETE SONG"));
        assertTrue("Onay mesajı gösterilmeli", output.contains("Are you sure"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Song deleted successfully"));
    }
    
    /**
     * Şarkı silme başarısız olma durumunu test eder
     */
    @Test
    public void testDeleteSongFailure() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı silmeyi başarısız yap
        songController.setDeleteSongSuccess(false);
        
        // Şarkı sil (3), şarkı seç (1), onaylama (y)
        String input = "3\n1\ny\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı silme hatası gösterilmeli", output.contains("Failed to delete song"));
    }
    
    /**
     * Şarkı silme iptal işlemini test eder
     */
    @Test
    public void testDeleteSongCancel() {
        // Test için şarkılar oluşturma
        List<Song> songs = new ArrayList<>();
        User songUser = new User();
        songUser.setId(1);
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", songUser);
        song1.setId(1);
        songs.add(song1);
        
        songController.setUserSongs(songs);
        
        // Şarkı sil (3), şarkı seç (1), iptal (n)
        String input = "3\n1\nn\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("İptal mesajı gösterilmeli", output.contains("Deletion cancelled"));
    }
    
    /**
     * Boş şarkı listesi ile silme yapmayı test eder
     */
    @Test
    public void testDeleteSongEmptyList() {
        // Boş şarkı listesi
        songController.setUserSongs(new ArrayList<>());
        
        // Şarkı sil (3), ana menüye dönüş
        String input = "3\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        songMenuView = new SongMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        songMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Boş liste uyarısı gösterilmeli", output.contains("You don't have any songs to delete"));
    }
    
    // Mock Controller Sınıfları
    
    /**
     * Mock UserController sınıfı
     */
    private class MockUserController extends UserController {
        private boolean loggedIn = true;
        private User currentUser;
        
        @Override
        public boolean isLoggedIn() {
            return loggedIn;
        }
        
        public void setLoggedIn(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }
        
        @Override
        public User getCurrentUser() {
            return currentUser;
        }
        
        public void setCurrentUser(User user) {
            this.currentUser = user;
        }
    }
    
    /**
     * Mock SongController sınıfı
     */
    private class MockSongController extends SongController {
        private List<Song> userSongs = new ArrayList<>();
        private boolean addSongSuccess = true;
        private boolean updateSongSuccess = true;
        private boolean deleteSongSuccess = true;
        
        public MockSongController() {
            super(null);
        }
        
        @Override
        public List<Song> getUserSongs() {
            return userSongs;
        }
        
        public void setUserSongs(List<Song> songs) {
            this.userSongs = songs;
        }
        
        @Override
        public Song addSong(String title, String artist, String album, String genre, int year, int duration, String filePath) {
            if (!addSongSuccess) {
                return null;
            }
            
            User songUser = new User();
            songUser.setId(1);
            Song song = new Song(title, artist, album, genre, year, duration, filePath, songUser);
            song.setId(userSongs.size() + 1);
            userSongs.add(song);
            return song;
        }
        
        public void setAddSongSuccess(boolean success) {
            this.addSongSuccess = success;
        }
        
        @Override
        public boolean updateSong(int songId, String title, String artist, String album, String genre, int year) {
            return updateSongSuccess;
        }
        
        public void setUpdateSongSuccess(boolean success) {
            this.updateSongSuccess = success;
        }
        
        @Override
        public boolean deleteSong(int songId) {
            return deleteSongSuccess;
        }
        
        public void setDeleteSongSuccess(boolean success) {
            this.deleteSongSuccess = success;
        }
    }
    
    /**
     * Mock PlaylistController sınıfı
     */
    private class MockPlaylistController extends PlaylistController {
        public MockPlaylistController() {
            super(null);
        }
    }
} 