package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.dao.UserSongStatisticsDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.service.RecommendationService;

/**
 * Test sınıfı - gerekli yerlerde mock kullanarak test eder
 */
@RunWith(MockitoJUnitRunner.class)
public class SongControllerTest {

    private SongController songController;
    private TestUserController userController;
    private TestSongDAO songDAO;
    
    @Mock
    private RecommendationService mockRecommendationService;
    
    @Mock
    private UserSongStatisticsDAO mockUserSongStatisticsDAO;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        userController = new TestUserController();
        songDAO = new TestSongDAO();
        songController = new SongController(userController);
        songController.setSongDAO(songDAO);
        
        // Set the mocked recommendation service using reflection since there's no setter
        try {
            java.lang.reflect.Field recommendationServiceField = SongController.class.getDeclaredField("recommendationService");
            recommendationServiceField.setAccessible(true);
            recommendationServiceField.set(songController, mockRecommendationService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Diğer dependency'leri reflection ile değiştir
        java.lang.reflect.Field userSongStatisticsDAOField = SongController.class.getDeclaredField("userSongStatisticsDAO");
        userSongStatisticsDAOField.setAccessible(true);
        userSongStatisticsDAOField.set(songController, mockUserSongStatisticsDAO);
    }
    
    @Test
    public void testAddSong() {
        // Create a test subclass of SongController that bypasses file validation
        SongController testController = new SongController(userController) {
            @Override
            protected boolean isValidFile(String filePath) {
                return true;
            }
        };
        testController.setSongDAO(songDAO);
        
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test için mock bir şarkı oluştur ve SongDAO içinde kaydet
        Song expectedSong = new Song("TestSong", "TestArtist", "TestAlbum", "Rock", 2023, 180, "test.mp3", 1);
        expectedSong.setId(1);
        songDAO.setTestSongToReturn(expectedSong);
        
        // Test
        Song result = testController.addSong("TestSong", "TestArtist", "TestAlbum", "Rock", 2023, 180, "test.mp3");
        
        // Doğrulama
        assertNotNull("Şarkı nesnesi oluşturulmalı", result);
        assertEquals("Şarkı adı doğru olmalı", "TestSong", result.getTitle());
        assertEquals("Şarkı sanatçısı doğru olmalı", "TestArtist", result.getArtist());
        assertEquals("Şarkı albümü doğru olmalı", "TestAlbum", result.getAlbum());
        assertEquals("Şarkı türü doğru olmalı", "Rock", result.getGenre());
        assertEquals("Şarkı yılı doğru olmalı", 2023, result.getYear());
        assertEquals("Şarkı süresi doğru olmalı", 180, result.getDuration());
        assertEquals("Şarkı dosya yolu doğru olmalı", "test.mp3", result.getFilePath());
        assertEquals("Şarkı kullanıcı ID'si doğru olmalı", 1, result.getUserId());
    }
    
    @Test
    public void testAddSongWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        Song result = songController.addSong("TestSong", "TestArtist", "TestAlbum", "Rock", 2023, 180, "test.mp3");
        
        // Doğrulama
        assertNull("Kullanıcı oturumu yokken şarkı oluşturulmamalı", result);
    }
    
    @Test
    public void testUpdateSong() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkısını ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(1);
        songDAO.addTestSong(testSong);
        
        // Test
        boolean result = songController.updateSong(1, "UpdatedTitle", "UpdatedArtist", "UpdatedAlbum", "UpdatedGenre", 2024);
        
        // Doğrulama
        assertTrue("Şarkı güncelleme başarılı olmalı", result);
    }
    
    @Test
    public void testUpdateSongWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.updateSong(1, "UpdatedTitle", "UpdatedArtist", "UpdatedAlbum", "UpdatedGenre", 2024);
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken şarkı güncellenememeli", result);
    }
    
    @Test
    public void testUpdateSongWithWrongUser() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Farklı kullanıcı ID'sine sahip test şarkısı ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(2); // farklı kullanıcı ID'si
        songDAO.addTestSong(testSong);
        
        // Test
        boolean result = songController.updateSong(1, "UpdatedTitle", "UpdatedArtist", "UpdatedAlbum", "UpdatedGenre", 2024);
        
        // Doğrulama
        assertFalse("Başka kullanıcının şarkısı güncellenememeli", result);
    }
    
    @Test
    public void testDeleteSong() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkısını ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(1);
        songDAO.addTestSong(testSong);
        
        // Test
        boolean result = songController.deleteSong(1);
        
        // Doğrulama
        assertTrue("Şarkı silme başarılı olmalı", result);
    }
    
    @Test
    public void testDeleteSongWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.deleteSong(1);
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken şarkı silinememeli", result);
    }
    
    @Test
    public void testDeleteSongWithWrongUser() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Farklı kullanıcı ID'sine sahip test şarkısı ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(2); // farklı kullanıcı ID'si
        songDAO.addTestSong(testSong);
        
        // Test
        boolean result = songController.deleteSong(1);
        
        // Doğrulama
        assertFalse("Başka kullanıcının şarkısı silinememeli", result);
    }
    
    @Test
    public void testGetUserSongs() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkılarını ekle
        Song song1 = new Song();
        song1.setId(1);
        song1.setUserId(1);
        song1.setTitle("Song1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setUserId(1);
        song2.setTitle("Song2");
        
        songDAO.addTestSong(song1);
        songDAO.addTestSong(song2);
        
        // Test
        List<Song> result = songController.getUserSongs();
        
        // Doğrulama
        assertNotNull("Şarkı listesi null olmamalı", result);
        assertEquals("Şarkı listesi 2 şarkı içermeli", 2, result.size());
    }
    
    @Test
    public void testGetUserSongsWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Song> result = songController.getUserSongs();
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken şarkı listesi boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testSearchSongs() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkılarını ekle
        Song song1 = new Song();
        song1.setId(1);
        song1.setUserId(1);
        song1.setTitle("Rock Song");
        song1.setGenre("Rock");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setUserId(1);
        song2.setTitle("Pop Song");
        song2.setGenre("Pop");
        
        songDAO.addTestSong(song1);
        songDAO.addTestSong(song2);
        songDAO.setSearchResults(List.of(song1, song2));
        
        // Test
        List<Song> result = songController.searchSongs("Rock");
        
        // Doğrulama
        assertNotNull("Arama sonucu null olmamalı", result);
        assertEquals("Arama sonucu doğru sayıda şarkı içermeli", 2, result.size());
    }
    
    @Test
    public void testSearchSongsWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Song> result = songController.searchSongs("Rock");
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken arama sonucu boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testGetRecommendations() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Setup mock recommendations
        List<Song> mockRecommendations = new ArrayList<>();
        Song recommendedSong = new Song();
        recommendedSong.setId(100);
        recommendedSong.setTitle("Recommended Song");
        recommendedSong.setArtist("Recommended Artist");
        mockRecommendations.add(recommendedSong);
        
        // Configure the mock to return these recommendations
        when(mockRecommendationService.getSongRecommendations(eq(testUser), anyInt())).thenReturn(mockRecommendations);
        
        // Test
        List<Song> result = songController.getRecommendations();
        
        // Doğrulama
        assertNotNull("Öneri listesi null olmamalı", result);
        assertTrue("Öneri listesi boş olmamalı", !result.isEmpty());
        assertEquals("Recommended Song", result.get(0).getTitle());
    }
    
    @Test
    public void testGetRecommendationsWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Song> result = songController.getRecommendations();
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken öneri listesi boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testGetSongsByArtist() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkılarını ekle
        Song song1 = new Song();
        song1.setId(1);
        song1.setUserId(1);
        song1.setTitle("Song1");
        song1.setArtist("Artist1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setUserId(1);
        song2.setTitle("Song2");
        song2.setArtist("Artist1");
        
        Song song3 = new Song();
        song3.setId(3);
        song3.setUserId(1);
        song3.setTitle("Song3");
        song3.setArtist("Artist2");
        
        songDAO.addTestSong(song1);
        songDAO.addTestSong(song2);
        songDAO.addTestSong(song3);
        
        // Test
        List<Song> result = songController.getSongsByArtist("Artist1");
        
        // Doğrulama
        assertNotNull("Şarkı listesi null olmamalı", result);
        assertEquals("Artist1 için 2 şarkı olmalı", 2, result.size());
    }
    
    @Test
    public void testGetSongsByArtistWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Song> result = songController.getSongsByArtist("Artist1");
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken şarkı listesi boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testAddArtist() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        boolean result = songController.addArtist("NewArtist");
        
        // Doğrulama
        assertTrue("Artist ekleme başarılı olmalı", result);
    }
    
    @Test
    public void testAddArtistWithEmptyName() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        boolean result = songController.addArtist("");
        
        // Doğrulama
        assertFalse("Boş artist adı eklenememeli", result);
    }
    
    @Test
    public void testAddArtistWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.addArtist("Artist");
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken artist eklenememeli", result);
    }
    
    @Test
    public void testAddAlbum() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        Album result = songController.addAlbum("AlbumTitle", "AlbumArtist", 2023, "Rock");
        
        // Doğrulama
        assertNotNull("Album nesnesi oluşturulmalı", result);
        assertEquals("Album başlığı doğru olmalı", "AlbumTitle", result.getTitle());
        assertEquals("Album sanatçısı doğru olmalı", "AlbumArtist", result.getArtist());
        assertEquals("Album yılı doğru olmalı", 2023, result.getYear());
        assertEquals("Album türü doğru olmalı", "Rock", result.getGenre());
        assertEquals("Album kullanıcı ID'si doğru olmalı", 1, result.getUserId());
    }
    
    @Test
    public void testAddAlbumWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        Album result = songController.addAlbum("AlbumTitle", "AlbumArtist", 2023, "Rock");
        
        // Doğrulama
        assertNull("Kullanıcı oturumu yokken album oluşturulmamalı", result);
    }
    
    @Test
    public void testGetUserAlbums() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        List<Album> result = songController.getUserAlbums();
        
        // Doğrulama
        assertNotNull("Album listesi null olmamalı", result);
        assertTrue("Album listesi başlangıçta boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testGetUserAlbumsWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Album> result = songController.getUserAlbums();
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken album listesi boş olmalı", result.isEmpty());
    }
    
    @Test
    public void testDeleteAlbum() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        boolean result = songController.deleteAlbum(1);
        
        // Doğrulama
        assertTrue("Album silme başarılı olmalı", result);
    }
    
    @Test
    public void testDeleteAlbumWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.deleteAlbum(1);
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken album silinememeli", result);
    }
    
    @Test
    public void testAddSongToAlbum() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        boolean result = songController.addSongToAlbum(1, 1);
        
        // Doğrulama
        assertTrue("Albüme şarkı ekleme başarılı olmalı", result);
    }
    
    @Test
    public void testAddSongToAlbumWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.addSongToAlbum(1, 1);
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken albüme şarkı eklenememeli", result);
    }
    
    @Test
    public void testGetEnhancedRecommendations_Success() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test verisi
        Map<Song, String> expectedRecommendations = new HashMap<>();
        expectedRecommendations.put(
            new Song("Recommended Song", "Recommended Artist", "Recommended Album", "Jazz", 2023, 210, "path/to/file", 3),
            "Because you like Jazz music"
        );
        
        // Mock davranışları
        when(mockRecommendationService.getEnhancedSongRecommendations(eq(testUser), anyInt())).thenReturn(expectedRecommendations);
        
        // Test
        Map<Song, String> result = songController.getEnhancedRecommendations();
        
        // Doğrulama
        assertEquals(expectedRecommendations, result);
        verify(mockRecommendationService).getEnhancedSongRecommendations(eq(testUser), anyInt());
    }
    
    @Test
    public void testGetEnhancedRecommendations_NoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        Map<Song, String> result = songController.getEnhancedRecommendations();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verifyNoInteractions(mockRecommendationService);
    }
    
    @Test
    public void testPlaySong_Success() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkısını ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(1);
        testSong.setTitle("Test Song");
        songDAO.addTestSong(testSong);
        
        // Mock davranışları
        when(mockUserSongStatisticsDAO.incrementPlayCount(testUser.getId(), 1)).thenReturn(true);
        
        // Test
        Song result = songController.playSong(1);
        
        // Doğrulama
        assertNotNull("Çalınan şarkı null olmamalı", result);
        assertEquals("Çalınan şarkı ID'si doğru olmalı", 1, result.getId());
        verify(mockUserSongStatisticsDAO).incrementPlayCount(testUser.getId(), 1);
    }
    
    @Test
    public void testPlaySong_NoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        Song result = songController.playSong(1);
        
        // Doğrulama
        assertNull("Kullanıcı oturumu yokken şarkı çalınamamalı", result);
        verifyNoInteractions(mockUserSongStatisticsDAO);
    }
    
    @Test
    public void testPlaySong_SongNotFound() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test
        Song result = songController.playSong(999); // Var olmayan şarkı ID'si
        
        // Doğrulama
        assertNull("Var olmayan şarkı çalınamamalı", result);
        verifyNoInteractions(mockUserSongStatisticsDAO);
    }
    
    @Test
    public void testToggleFavorite_Success() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test şarkısını ekle
        Song testSong = new Song();
        testSong.setId(1);
        testSong.setUserId(1);
        songDAO.addTestSong(testSong);
        
        // Mock davranışları
        when(mockUserSongStatisticsDAO.setFavorite(testUser.getId(), 1, true)).thenReturn(true);
        
        // Test
        boolean result = songController.toggleFavorite(1, true);
        
        // Doğrulama
        assertTrue("Favoriye ekleme başarılı olmalı", result);
        verify(mockUserSongStatisticsDAO).setFavorite(testUser.getId(), 1, true);
    }
    
    @Test
    public void testToggleFavorite_NoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        boolean result = songController.toggleFavorite(1, true);
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken favori eklenememeli", result);
        verifyNoInteractions(mockUserSongStatisticsDAO);
    }
    
    @Test
    public void testGetFavoriteSongs_Success() {
        // Test kullanıcısını ayarla
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        userController.setCurrentUser(testUser);
        
        // Test verisi
        List<Integer> favoriteSongIds = new ArrayList<>();
        favoriteSongIds.add(1);
        favoriteSongIds.add(2);
        
        Song song1 = new Song("Favorite 1", "Artist 1", "Album 1", "Rock", 2023, 180, "path/to/file1", testUser.getId());
        song1.setId(1);
        Song song2 = new Song("Favorite 2", "Artist 2", "Album 2", "Pop", 2022, 200, "path/to/file2", testUser.getId());
        song2.setId(2);
        
        songDAO.addTestSong(song1);
        songDAO.addTestSong(song2);
        
        // Mock davranışları
        when(mockUserSongStatisticsDAO.getFavoriteSongs(testUser.getId())).thenReturn(favoriteSongIds);
        
        // Test
        List<Song> result = songController.getFavoriteSongs();
        
        // Doğrulama
        assertEquals("Favori şarkı sayısı doğru olmalı", 2, result.size());
        assertEquals("İlk favori şarkı doğru olmalı", 1, result.get(0).getId());
        assertEquals("İkinci favori şarkı doğru olmalı", 2, result.get(1).getId());
        verify(mockUserSongStatisticsDAO).getFavoriteSongs(testUser.getId());
    }
    
    @Test
    public void testGetFavoriteSongs_NoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Test
        List<Song> result = songController.getFavoriteSongs();
        
        // Doğrulama
        assertTrue("Kullanıcı oturumu yokken favori şarkı listesi boş olmalı", result.isEmpty());
        verifyNoInteractions(mockUserSongStatisticsDAO);
    }
    
    /**
     * Test amaçlı basit UserController uygulaması
     */
    private static class TestUserController extends UserController {
        private User currentUser;
        
        public TestUserController() {
            super();
        }
        
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
    
    /**
     * Test amaçlı basit SongDAO uygulaması
     */
    private static class TestSongDAO extends SongDAO {
        private List<Song> testSongs = new ArrayList<>();
        private List<Song> searchResults = new ArrayList<>();
        private Song testSongToReturn = null;
        
        public TestSongDAO() {
            super();
        }
        
        public void addTestSong(Song song) {
            testSongs.add(song);
        }
        
        public void setSearchResults(List<Song> results) {
            this.searchResults = results;
        }
        
        public void setTestSongToReturn(Song song) {
            this.testSongToReturn = song;
        }
        
        @Override
        public Song create(Song song) {
            // SongController'da dosya kontrolü yapılıyor, kullanıcıya özel şarkı döndürme
            if (testSongToReturn != null) {
                return testSongToReturn;
            }
            
            song.setId(testSongs.size() + 1);
            testSongs.add(song);
            return song;
        }
        
        @Override
        public Optional<Song> findById(int id) {
            return testSongs.stream()
                    .filter(s -> s.getId() == id)
                    .findFirst();
        }
        
        @Override
        public List<Song> findByUserId(int userId) {
            return testSongs.stream()
                    .filter(s -> s.getUserId() == userId)
                    .collect(Collectors.toList());
        }
        
        @Override
        public List<Song> findAll() {
            return new ArrayList<>(testSongs);
        }
        
        @Override
        public boolean update(Song song) {
            for (int i = 0; i < testSongs.size(); i++) {
                if (testSongs.get(i).getId() == song.getId()) {
                    testSongs.set(i, song);
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean delete(int id) {
            return testSongs.removeIf(s -> s.getId() == id);
        }
        
        @Override
        public List<Song> search(String title, String artist, String album, String genre) {
            return searchResults;
        }
    }
} 