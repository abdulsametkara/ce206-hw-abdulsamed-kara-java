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
 * SearchMenuView için test sınıfı
 */
public class SearchMenuViewTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    private SearchMenuView searchMenuView;
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
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        MenuView resultView = searchMenuView.display();
        
        // LoginMenuView'a yönlendirildiğini doğrulama
        assertTrue("Oturum açılmamışsa LoginMenuView'a yönlendirilmeli", resultView instanceof LoginMenuView);
    }
    
    /**
     * Boş arama sorgusuyla ana menüye dönüşü test eder
     */
    @Test
    public void testEmptySearchQuery() {
        // Boş arama sorgusu için input
        String input = "\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        MenuView resultView = searchMenuView.display();
        
        // MainMenuView'a yönlendirildiğini doğrulama
        assertTrue("Boş arama sorgusunda MainMenuView'a yönlendirilmeli", resultView instanceof MainMenuView);
    }
    
    /**
     * Sonuç olmayan arama sorgusunu test eder
     */
    @Test
    public void testNoSearchResults() {
        // Arama sorgusu
        String input = "nonexistentquery\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Boş sonuç listesi ayarlama
        songController.setSearchResults(new ArrayList<>());
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Sonuç bulunamadı mesajı gösterilmeli", output.contains("No results found"));
    }
    
    /**
     * Başarılı arama sorgusunu test eder
     */
    @Test
    public void testSuccessfulSearch() {
        // Arama sorgusu ve ana menüye dönüş
        String input = "test\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Arama sonuçları gösterilmeli", output.contains("SEARCH RESULTS FOR: test"));
        assertTrue("Şarkı başlığı gösterilmeli", output.contains("Test Song"));
        assertTrue("Sanatçı adı gösterilmeli", output.contains("Test Artist"));
        assertTrue("Albüm adı gösterilmeli", output.contains("Test Album"));
    }
    
    /**
     * Şarkıları playlist'e ekleme işlemini test eder
     */
    @Test
    public void testAddSongsToPlaylist() {
        // Arama sorgusu, playlist'e ekle (1), playlist seç (1), tüm şarkıları ekle (all)
        String input = "test\n1\n1\nall\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // Test için playlist oluşturma
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Test Playlist", "Description", 1);
        playlist.setId(1);
        playlists.add(playlist);
        playlistController.setUserPlaylists(playlists);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Playlist'e ekleme başlığı gösterilmeli", output.contains("ADD TO PLAYLIST"));
        assertTrue("Playlist listesi gösterilmeli", output.contains("Test Playlist"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Added"));
    }
    
    /**
     * Yeni playlist oluşturarak şarkı ekleme işlemini test eder
     */
    @Test
    public void testAddSongsToNewPlaylist() {
        // Arama sorgusu, playlist'e ekle (1), yeni playlist oluştur (y), playlist adı ve açıklama, playlist seç (1), tüm şarkıları ekle (all)
        String input = "test\n1\ny\nNew Playlist\nDescription\n1\nall\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // Boş playlist listesi ayarlama
        playlistController.setUserPlaylists(new ArrayList<>());
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Boş playlist uyarısı gösterilmeli", output.contains("You don't have any playlists yet"));
        assertTrue("Playlist oluşturma mesajı gösterilmeli", output.contains("Would you like to create a new playlist now?"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Playlist created successfully"));
    }
    
    /**
     * Playlist oluşturma başarısız olma durumunu test eder
     */
    @Test
    public void testAddSongsToNewPlaylistFailure() {
        // Arama sorgusu, playlist'e ekle (1), yeni playlist oluştur (y), playlist adı ve açıklama
        String input = "test\n1\ny\nNew Playlist\nDescription\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // Boş playlist listesi ayarlama
        playlistController.setUserPlaylists(new ArrayList<>());
        
        // Playlist oluşturmayı başarısız yap
        playlistController.setCreatePlaylistSuccess(false);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Playlist oluşturma hatası gösterilmeli", output.contains("Failed to create playlist"));
    }
    
    /**
     * Şarkı düzenleme işlemini test eder
     */
    @Test
    public void testEditSong() {
        // Arama sorgusu, şarkı düzenle (2), şarkı seç (1), yeni değerler
        String input = "test\n2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\n2022\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı düzenleme başlığı gösterilmeli", output.contains("EDIT SONG"));
        assertTrue("Mevcut şarkı bilgileri gösterilmeli", output.contains("Test Song"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Song updated successfully"));
    }
    
    /**
     * Şarkı güncelleme başarısız olma durumunu test eder
     */
    @Test
    public void testEditSongFailure() {
        // Arama sorgusu, şarkı düzenle (2), şarkı seç (1), yeni değerler
        String input = "test\n2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\n2022\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // Şarkı güncellemeyi başarısız yap
        songController.setUpdateSongSuccess(false);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı güncelleme hatası gösterilmeli", output.contains("Failed to update song"));
    }
    
    /**
     * Geçersiz yıl formatı ile şarkı düzenleme işlemini test eder
     */
    @Test
    public void testEditSongInvalidYear() {
        // Arama sorgusu, şarkı düzenle (2), şarkı seç (1), yeni değerler, geçersiz yıl formatı
        String input = "test\n2\n1\nNew Title\nNew Artist\nNew Album\nNew Genre\ninvalid\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz yıl formatı hatası gösterilmeli", output.contains("Invalid year format"));
    }
    
    /**
     * Şarkı silme işlemini test eder
     */
    @Test
    public void testDeleteSong() {
        // Arama sorgusu, şarkı sil (3), şarkı seç (1), onaylama (y)
        String input = "test\n3\n1\ny\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
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
        // Arama sorgusu, şarkı sil (3), şarkı seç (1), onaylama (y)
        String input = "test\n3\n1\ny\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // Şarkı silmeyi başarısız yap
        songController.setDeleteSongSuccess(false);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şarkı silme hatası gösterilmeli", output.contains("Failed to delete song"));
    }
    
    /**
     * Şarkı silme iptal işlemini test eder
     */
    @Test
    public void testDeleteSongCancel() {
        // Arama sorgusu, şarkı sil (3), şarkı seç (1), iptal (n)
        String input = "test\n3\n1\nn\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("İptal mesajı gösterilmeli", output.contains("Deletion cancelled"));
    }
    
    /**
     * Yeni arama yapma işlemini test eder
     */
    @Test
    public void testNewSearch() {
        // İlk arama sorgusu, yeni arama (4), ikinci arama sorgusu, ana menüye dönüş
        String input = "test\n4\nanother\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults1 = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults1.add(song1);
        
        List<Song> searchResults2 = new ArrayList<>();
        Song song2 = new Song("Another Song", "Another Artist", "Another Album", "Pop", 2022, 240, "path/to/another", 1);
        song2.setId(2);
        searchResults2.add(song2);
        
        // İlk aramanın sonuçlarını ayarlama
        songController.setSearchResults(searchResults1);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // İlk arama sonuçlarını göster
        MenuView resultView = searchMenuView.display();
        
        // İkinci aramada farklı sonuçları döndürmek için ayarlama
        songController.setSecondSearchResults(searchResults2);
        
        // İkinci aramayı yap (resultView'ın display metodunu çağır)
        ((SearchMenuView)resultView).display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("İlk arama sonuçları gösterilmeli", output.contains("SEARCH RESULTS FOR: test"));
        assertTrue("İkinci arama sonuçları gösterilmeli", output.contains("SEARCH RESULTS FOR: another"));
        assertTrue("İkinci arama sonuç şarkısı gösterilmeli", output.contains("Another Song"));
    }
    
    /**
     * Geçersiz menü seçimi test eder
     */
    @Test
    public void testInvalidMenuChoice() {
        // Arama sorgusu, geçersiz seçim, ana menüye dönüş
        String input = "test\n99\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Test için şarkılar oluşturma
        List<Song> searchResults = new ArrayList<>();
        Song song1 = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2021, 180, "path/to/file", 1);
        song1.setId(1);
        searchResults.add(song1);
        
        // Sonuçları ayarlama
        songController.setSearchResults(searchResults);
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz seçim mesajı gösterilmeli", output.contains("Invalid choice"));
    }
    
    /**
     * Sonuç olmadan işlem yapmaya çalışmayı test eder
     */
    @Test
    public void testActionsWithNoResults() {
        // Arama sorgusu, playlist'e ekle (1), ana menüye dönüş
        String input = "test\n1\n0\n";
        Scanner scanner = new Scanner(input);
        
        // Boş sonuç listesi ayarlama
        songController.setSearchResults(new ArrayList<>());
        
        // View oluşturma
        searchMenuView = new SearchMenuView(scanner, userController, songController, playlistController);
        
        // Görünümü gösterme
        searchMenuView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Sonuç yok uyarısı gösterilmeli", output.contains("No search results to add to playlist"));
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
        private List<Song> searchResults = new ArrayList<>();
        private List<Song> secondSearchResults = null;
        private boolean updateSongSuccess = true;
        private boolean deleteSongSuccess = true;
        private int searchCount = 0;
        
        public MockSongController() {
            super(null);
        }
        
        @Override
        public List<Song> searchSongs(String query) {
            searchCount++;
            if (searchCount > 1 && secondSearchResults != null) {
                if (query.equals("another")) {
                    return secondSearchResults;
                }
            }
            return searchResults;
        }
        
        public void setSearchResults(List<Song> results) {
            this.searchResults = results;
        }
        
        public void setSecondSearchResults(List<Song> results) {
            this.secondSearchResults = results;
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
        private List<Playlist> userPlaylists = new ArrayList<>();
        private boolean createPlaylistSuccess = true;
        
        public MockPlaylistController() {
            super(null);
        }
        
        @Override
        public List<Playlist> getUserPlaylists() {
            return userPlaylists;
        }
        
        public void setUserPlaylists(List<Playlist> playlists) {
            this.userPlaylists = playlists;
        }
        
        @Override
        public Playlist createPlaylist(String name, String description) {
            if (!createPlaylistSuccess) {
                return null;
            }
            
            Playlist playlist = new Playlist(name, description, 1);
            playlist.setId(userPlaylists.size() + 1);
            userPlaylists.add(playlist);
            return playlist;
        }
        
        public void setCreatePlaylistSuccess(boolean success) {
            this.createPlaylistSuccess = success;
        }
        
        @Override
        public boolean addSongToPlaylist(int playlistId, int songId) {
            return true;
        }
    }
}