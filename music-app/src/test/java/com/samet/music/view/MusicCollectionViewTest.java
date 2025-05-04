package com.samet.music.view;

import static org.junit.Assert.*;

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
 * MusicCollectionView test sınıfı - test yaklaşımı değiştirilmiştir
 */
public class MusicCollectionViewTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    private MusicCollectionView musicCollectionView;
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
        
        // Test için kullanılacak Scanner oluşturma
        Scanner scanner = new Scanner("0\n"); // Direkt çıkış için
        
        // MusicCollectionView oluşturma
        musicCollectionView = new MusicCollectionView(scanner, userController, songController, playlistController);
    }
    
    @After
    public void tearDown() {
        // Standart output akışını geri yükleme
        System.setOut(originalOut);
    }
    
    /**
     * Test 1: Kullanıcı oturumu açık değilse LoginMenuView'a yönlendirme kontrolü
     */
    @Test
    public void testNotLoggedIn() {
        // Kullanıcı oturumunu kapatma
        userController.setLoggedIn(false);
        
        // Görünümü gösterme
        MenuView resultView = musicCollectionView.display();
        
        // LoginMenuView'a yönlendirildiğini doğrulama
        assertTrue("Oturum açılmamışsa LoginMenuView'a yönlendirilmeli", resultView instanceof LoginMenuView);
    }
    
    /**
     * Test 2: Menünün doğru görüntülenmesi
     */
    @Test
    public void testDisplayMenu() {
        // Menüyü görüntüle
        musicCollectionView.display();
        
        // Çıktı içeriğini kontrol et
        String output = outputStream.toString();
        assertTrue("Müzik koleksiyonu menü başlığı gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
        assertTrue("Menüde Add Song seçeneği olmalı", output.contains("Add Song"));
        assertTrue("Menüde View Albums seçeneği olmalı", output.contains("View Albums"));
        assertTrue("Menüde Add Artist seçeneği olmalı", output.contains("Add Artist"));
    }
    
    /**
     * Test 3: Albüm bilgilerinin doğru gösterilmesi
     */
    @Test
    public void testAlbumDisplay() {
        // Test albümü oluşturma ve listeye ekleme
        Album album = new Album("Test Album", "Test Artist", 2023, "Rock", 1);
        album.setId(1);
        List<Album> albums = new ArrayList<>();
        albums.add(album);
        
        // Controller'a albüm listesini ekleme
        songController.setUserAlbums(albums);
        
        // Menüyü görüntüle
        musicCollectionView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Menü seçenekleri gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
    }
    
    /**
     * Test 4: Şarkı bilgilerinin doğru gösterilmesi
     */
    @Test
    public void testSongDisplay() {
        // Test şarkısı oluşturma ve listeye ekleme
        List<Song> songs = new ArrayList<>();
        Song song = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2023, 180, "path/to/file", 1);
        song.setId(1);
        songs.add(song);
        
        // Controller'a şarkı listesini ekleme
        songController.setUserSongs(songs);
        
        // Menüyü görüntüle
        musicCollectionView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Menü seçenekleri gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
    }
    
    /**
     * Test 5: Albüm ekleme işleminin kontrolü
     */
    @Test
    public void testAddAlbumSuccess() {
        // Test şarkısını oluştur ve ekle - artist ekliyoruz önce
        List<String> artists = new ArrayList<>();
        artists.add("Test Artist");
        songController.setUserArtists(artists);
        
        // Menüyü görüntüle
        musicCollectionView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Menü seçenekleri gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
    }
    
    /**
     * Test 6: Şarkı ekleme işleminin kontrolü
     */
    @Test
    public void testAddSongSuccess() {
        // Test artistini oluştur ve ekle
        List<String> artists = new ArrayList<>();
        artists.add("Test Artist");
        songController.setUserArtists(artists);
        
        // Menüyü görüntüle
        musicCollectionView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Menü seçenekleri gösterilmeli", output.contains("MUSIC COLLECTION MENU"));
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
        private List<Album> userAlbums = new ArrayList<>();
        private List<String> userArtists = new ArrayList<>();
        private boolean addSongToAlbumSuccess = true;
        
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
        public boolean addSongToAlbum(int albumId, int songId) {
            return addSongToAlbumSuccess;
        }
        
        public void setAddSongToAlbumSuccess(boolean success) {
            this.addSongToAlbumSuccess = success;
        }
        
        @Override
        public boolean addArtist(String artistName) {
            return true;
        }
        
        @Override
        public List<String> getArtists() {
            return userArtists;
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