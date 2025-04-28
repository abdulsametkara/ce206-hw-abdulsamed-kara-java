package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;

/**
 * Test sınıfı - gerekli yerlerde mock kullanarak test eder
 */
@RunWith(MockitoJUnitRunner.class)
public class SongControllerTest {

    private SongController songController;
    private TestUserController userController;
    private TestSongDAO songDAO;
    
    @Before
    public void setUp() {
        userController = new TestUserController();
        songDAO = new TestSongDAO();
        songController = new SongController(userController);
        songController.setSongDAO(songDAO);
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
        User mockUser = new User();
        mockUser.setId(1);
        Song expectedSong = new Song("TestSong", "TestArtist", "TestAlbum", "Rock", 2023, 180, "test.mp3", mockUser);
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
        assertEquals("Şarkı yılı doğru olmalı", Integer.valueOf(2023), result.getYear());
        assertEquals("Şarkı süresi doğru olmalı", Integer.valueOf(180), result.getDuration());
        assertEquals("Şarkı dosya yolu doğru olmalı", "test.mp3", result.getFilePath());
        assertEquals("Şarkı kullanıcı ID'si doğru olmalı", Integer.valueOf(1), result.getUserId());
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
        
        // Kullanıcının şarkılarını ekle
        Song userSong1 = new Song();
        userSong1.setId(1);
        userSong1.setUserId(1);
        userSong1.setTitle("User Rock Song");
        userSong1.setGenre("Rock");
        userSong1.setArtist("Artist1");
        
        Song userSong2 = new Song();
        userSong2.setId(2);
        userSong2.setUserId(1);
        userSong2.setTitle("User Pop Song");
        userSong2.setGenre("Pop");
        userSong2.setArtist("Artist2");
        
        // Diğer kullanıcıların şarkıları (öneriler için)
        Song otherSong1 = new Song();
        otherSong1.setId(3);
        otherSong1.setUserId(2);
        otherSong1.setTitle("Other Rock Song");
        otherSong1.setGenre("Rock");
        otherSong1.setArtist("Artist3");
        
        Song otherSong2 = new Song();
        otherSong2.setId(4);
        otherSong2.setUserId(2);
        otherSong2.setTitle("Other Pop Song");
        otherSong2.setGenre("Pop");
        otherSong2.setArtist("Artist2");
        
        songDAO.addTestSong(userSong1);
        songDAO.addTestSong(userSong2);
        songDAO.addTestSong(otherSong1);
        songDAO.addTestSong(otherSong2);
        
        // Test
        List<Song> result = songController.getRecommendations();
        
        // Doğrulama
        assertNotNull("Öneri listesi null olmamalı", result);
        assertTrue("Öneri listesi boş olmamalı", !result.isEmpty());
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
        assertEquals("Album yılı doğru olmalı", Integer.valueOf(2023), result.getYear());
        assertEquals("Album türü doğru olmalı", "Rock", result.getGenre());
        assertEquals("Album kullanıcı ID'si doğru olmalı", Integer.valueOf(1), result.getUserId());
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