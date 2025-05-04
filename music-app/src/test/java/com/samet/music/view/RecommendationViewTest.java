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
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;

/**
 * RecommendationView için test sınıfı - Mockito kullanmadan
 */
public class RecommendationViewTest {
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    private RecommendationView recommendationView;
    private TestUserController userController;
    private TestSongController songController;
    private TestPlaylistController playlistController;
    private User testUser;
    
    @Before
    public void setUp() {
        // Çıktıyı yakalamak için System.out yönlendirmesi
        System.setOut(new PrintStream(outContent));
        
        // Test kullanıcı nesnesi
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Test kontrol sınıfları
        userController = new TestUserController();
        songController = new TestSongController();
        playlistController = new TestPlaylistController();
        
        // Kullanıcı girişi
        userController.setCurrentUser(testUser);
        
        // Scanner mocklanması
        String simulatedUserInput = "0\n"; // Çıkış yapmak için kullanıcı girdisi
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        // Test edilen sınıf
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
    }
    
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testDisplayWhenNoUser() {
        // Kullanıcı girişi yok
        userController.setCurrentUser(null);
        
        // Menü gösterme
        MenuView resultView = recommendationView.display();
        
        // Kontrolü
        assertTrue("Kullanıcı girişi yokken login menüsüne dönmeli", resultView instanceof LoginMenuView);
    }
    
    @Test
    public void testGetSongRecommendationsWithNoSongs() {
        // Hiç şarkı yok
        songController.setUserSongs(new ArrayList<>());
        
        // Test metodu çağırma
        assertSimulateMenuOption("1"); // "Get Song Recommendations" seçeneği
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Şarkı bulunamadı mesajı gösterilmeli", 
            output.contains("No songs found in your library"));
    }
    
    @Test
    public void testGetSongRecommendationsWithSongs() {
        // Test şarkıları
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Test Song 2");
        song2.setArtist("Test Artist 2");
        song2.setGenre("Pop");
        song2.setUserId(testUser.getId());
        
        testSongs.add(song1);
        testSongs.add(song2);
        
        // Test verisini ayarla
        songController.setUserSongs(testSongs);
        
        // 'n' ile çalma listesine eklemeyi reddetme
        String simulatedUserInput = "1\nn\n"; // 1: Song Recommendations, n: No to playlist
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Test
        MenuView result = recommendationView.display();
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Test Song 1 şarkısı görüntülenmeli", output.contains("Test Song 1"));
        assertTrue("Test Artist 1 sanatçısı görüntülenmeli", output.contains("Test Artist 1"));
        assertTrue("Test Song 2 şarkısı görüntülenmeli", output.contains("Test Song 2"));
        assertTrue("Tür gruplandırması görüntülenmeli", output.contains("Songs by genre"));
    }
    
    @Test
    public void testGetAlbumRecommendationsWithNoAlbums() {
        // Hiç albüm yok
        songController.setUserAlbums(new ArrayList<>());
        
        // Test metodu çağırma
        assertSimulateMenuOption("2"); // "Get Album Recommendations" seçeneği
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Albüm bulunamadı mesajı gösterilmeli", 
            output.contains("No albums found in your library"));
    }
    
    @Test
    public void testGetAlbumRecommendationsWithAlbums() {
        // Test albümleri
        List<Album> testAlbums = new ArrayList<>();
        
        Album album1 = new Album();
        album1.setId(1);
        album1.setTitle("Test Album 1");
        album1.setArtist("Test Artist 1");
        album1.setGenre("Rock");
        album1.setUserId(testUser.getId());
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setTitle("Test Album 2");
        album2.setArtist("Test Artist 2");
        album2.setGenre("Pop");
        album2.setUserId(testUser.getId());
        
        testAlbums.add(album1);
        testAlbums.add(album2);
        
        // Test verisini ayarla
        songController.setUserAlbums(testAlbums);
        
        // Test
        assertSimulateMenuOption("2"); // "Get Album Recommendations" seçeneği
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Test Album 1 albümü görüntülenmeli", output.contains("Test Album 1"));
        assertTrue("Test Artist 1 sanatçısı görüntülenmeli", output.contains("Test Artist 1"));
        assertTrue("Test Album 2 albümü görüntülenmeli", output.contains("Test Album 2"));
    }
    
    @Test
    public void testGetArtistRecommendationsWithNoArtists() {
        // Hiç sanatçı yok
        songController.setUserArtists(new ArrayList<>());
        
        // Test metodu çağırma
        assertSimulateMenuOption("3"); // "Get Artist Recommendations" seçeneği
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Sanatçı bulunamadı mesajı gösterilmeli", 
            output.contains("No artists found in your library"));
    }
    
    @Test
    public void testGetArtistRecommendationsWithArtists() {
        // Test sanatçıları
        List<String> testArtists = new ArrayList<>();
        testArtists.add("Test Artist 1");
        testArtists.add("Test Artist 2");
        
        // Test şarkıları
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Test Song 2");
        song2.setArtist("Test Artist 1");
        
        testSongs.add(song1);
        testSongs.add(song2);
        
        // Test verisini ayarla
        songController.setUserArtists(testArtists);
        songController.setArtistSongs(testSongs);
        
        // Test metodu çağırma
        assertSimulateMenuOption("3"); // "Get Artist Recommendations" seçeneği
        
        // Çıktı kontrolü
        String output = outContent.toString();
        assertTrue("Test Artist 1 sanatçısı görüntülenmeli", output.contains("Test Artist 1"));
        assertTrue("Test Artist 2 sanatçısı görüntülenmeli", output.contains("Test Artist 2"));
        assertTrue("Şarkılar bölümü görüntülenmeli", output.contains("Songs by these artists"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithNoPlaylists() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Boş playlist listesi ayarla
        playlistController.setUserPlaylists(new ArrayList<>());
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), sonra yeni playlist oluşturmayı reddet (n)
        String simulatedUserInput = "1\ny\nn\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Playlist bulunamadı mesajı gösterilmeli", 
            output.contains("You don't have any playlists yet"));
        assertTrue("Yeni playlist oluşturma seçeneği gösterilmeli", 
            output.contains("Would you like to create a new playlist now?"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithNewPlaylistSuccess() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Boş playlist listesi ayarla
        playlistController.setUserPlaylists(new ArrayList<>());
        playlistController.setCreationSuccess(true);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // yeni playlist oluştur (y), playlist adı (Test Playlist), açıklama (Description), 
        // şarkıyı seç (Test Song 1)
        String simulatedUserInput = "1\ny\ny\nTest Playlist\nDescription\nTest Song 1\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Playlist başarıyla oluşturuldu mesajı gösterilmeli", 
            output.contains("Playlist created successfully"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithNewPlaylistFailure() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Boş playlist listesi ayarla
        playlistController.setUserPlaylists(new ArrayList<>());
        playlistController.setCreationSuccess(false);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // yeni playlist oluştur (y), playlist adı (Test Playlist), açıklama (Description)
        String simulatedUserInput = "1\ny\ny\nTest Playlist\nDescription\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Playlist oluşturma hatası gösterilmeli", 
            output.contains("Failed to create playlist"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithExistingPlaylistsAllSongs() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Test playlist oluştur
        com.samet.music.model.Playlist testPlaylist = new com.samet.music.model.Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setUserId(testUser.getId());
        
        List<com.samet.music.model.Playlist> playlists = new ArrayList<>();
        playlists.add(testPlaylist);
        
        // Playlist listesi ayarla
        playlistController.setUserPlaylists(playlists);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // playlist seç (Test Playlist), tüm şarkıları ekle (all)
        String simulatedUserInput = "1\ny\nTest Playlist\nall\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Şarkıların playlist'e eklenmesi başarı mesajı gösterilmeli", 
            output.contains("Added"));
        assertTrue("Playlist adı gösterilmeli", 
            output.contains("Test Playlist"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithExistingPlaylistsOneSong() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Test playlist oluştur
        com.samet.music.model.Playlist testPlaylist = new com.samet.music.model.Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setUserId(testUser.getId());
        
        List<com.samet.music.model.Playlist> playlists = new ArrayList<>();
        playlists.add(testPlaylist);
        
        // Playlist listesi ayarla
        playlistController.setUserPlaylists(playlists);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // playlist seç (Test Playlist), belirli bir şarkıyı ekle (Test Song 1)
        String simulatedUserInput = "1\ny\nTest Playlist\nTest Song 1\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Şarkı başarı mesajı gösterilmeli", 
            output.contains("Song"));
        assertTrue("Şarkı adı gösterilmeli", 
            output.contains("Test Song 1"));
        assertTrue("Playlist adı gösterilmeli", 
            output.contains("Test Playlist"));
    }
    
    @Test
    public void testAddSongsToPlaylistWithNonExistentSong() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Test playlist oluştur
        com.samet.music.model.Playlist testPlaylist = new com.samet.music.model.Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setUserId(testUser.getId());
        
        List<com.samet.music.model.Playlist> playlists = new ArrayList<>();
        playlists.add(testPlaylist);
        
        // Playlist listesi ayarla
        playlistController.setUserPlaylists(playlists);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // playlist seç (Test Playlist), var olmayan bir şarkıyı ekle (Nonexistent Song)
        String simulatedUserInput = "1\ny\nTest Playlist\nNonexistent Song\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Şarkı bulunamadı hata mesajı gösterilmeli", 
            output.contains("Song not found"));
    }
    
    @Test
    public void testCancelAddSongsToPlaylist() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Test playlist oluştur
        com.samet.music.model.Playlist testPlaylist = new com.samet.music.model.Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setUserId(testUser.getId());
        
        List<com.samet.music.model.Playlist> playlists = new ArrayList<>();
        playlists.add(testPlaylist);
        
        // Playlist listesi ayarla
        playlistController.setUserPlaylists(playlists);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // playlist seç (Test Playlist), işlemi iptal et (0)
        String simulatedUserInput = "1\ny\nTest Playlist\n0\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("İşlem iptal edildi mesajı gösterilmeli", 
            output.contains("Operation cancelled"));
    }
    
    @Test
    public void testPlaylistNotFound() {
        // Test için şarkılar ayarla
        List<Song> testSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        testSongs.add(song1);
        
        // Test playlist oluştur
        com.samet.music.model.Playlist testPlaylist = new com.samet.music.model.Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setUserId(testUser.getId());
        
        List<com.samet.music.model.Playlist> playlists = new ArrayList<>();
        playlists.add(testPlaylist);
        
        // Playlist listesi ayarla
        playlistController.setUserPlaylists(playlists);
        
        // Kullanıcı girişini simüle et - şarkı önerilerini görüntüle, playlist'e eklemek iste (y), 
        // yanlış playlist adı (Wrong Playlist)
        String simulatedUserInput = "1\ny\nWrong Playlist\n\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Şarkıları ayarla
        songController.setUserSongs(testSongs);
        
        // Test
        recommendationView.display();
        
        // Output kontrolü
        String output = outContent.toString();
        assertTrue("Playlist bulunamadı mesajı gösterilmeli", 
            output.contains("Playlist not found"));
    }
    
    // Simüle menu seçimi yardımcı metodu
    private void assertSimulateMenuOption(String option) {
        // Kullanıcı girişini simüle et
        String simulatedUserInput = option + "\n\n"; // option seçimi ve Enter tuşları
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes()));
        
        recommendationView = new RecommendationView(
                new Scanner(System.in),
                userController,
                songController,
                playlistController);
        
        // Menü gösterme
        recommendationView.display();
    }
    
    // Test sınıfları
    
    private static class TestUserController extends UserController {
        private User currentUser;
        
        @Override
        public User getCurrentUser() {
            return currentUser;
        }
        
        public void setCurrentUser(User user) {
            this.currentUser = user;
        }
        
        @Override
        public boolean isLoggedIn() {
            return currentUser != null;
        }
    }
    
    private static class TestSongController extends SongController {
        private List<Song> userSongs = new ArrayList<>();
        private List<Album> userAlbums = new ArrayList<>();
        private List<String> userArtists = new ArrayList<>();
        private List<Song> artistSongs = new ArrayList<>();
        
        public TestSongController() {
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
        public List<Album> getUserAlbums() {
            return userAlbums;
        }
        
        public void setUserAlbums(List<Album> albums) {
            this.userAlbums = albums;
        }
        
        @Override
        public List<String> getUserArtists() {
            return userArtists;
        }
        
        public void setUserArtists(List<String> artists) {
            this.userArtists = artists;
        }
        
        @Override
        public List<String> getArtists() {
            return userArtists;
        }
        
        @Override
        public List<Song> getSongsByArtist(String artistName) {
            return artistSongs;
        }
        
        public void setArtistSongs(List<Song> songs) {
            this.artistSongs = songs;
        }
    }
    
    private static class TestPlaylistController extends PlaylistController {
        private List<com.samet.music.model.Playlist> userPlaylists = new ArrayList<>();
        private boolean creationSuccess = true;
        
        public TestPlaylistController() {
            super(null);
        }
        
        @Override
        public List<com.samet.music.model.Playlist> getUserPlaylists() {
            return userPlaylists;
        }
        
        public void setUserPlaylists(List<com.samet.music.model.Playlist> playlists) {
            this.userPlaylists = playlists;
        }
        
        @Override
        public com.samet.music.model.Playlist createPlaylist(String name, String description) {
            if (creationSuccess) {
                com.samet.music.model.Playlist playlist = new com.samet.music.model.Playlist();
                playlist.setId(userPlaylists.size() + 1);
                playlist.setName(name);
                playlist.setDescription(description);
                playlist.setUserId(1); // Test user ID
                userPlaylists.add(playlist);
                return playlist;
            }
            return null;
        }
        
        public void setCreationSuccess(boolean success) {
            this.creationSuccess = success;
        }
        
        @Override
        public boolean addSongToPlaylist(int playlistId, int songId) {
            return true;
        }
    }
} 