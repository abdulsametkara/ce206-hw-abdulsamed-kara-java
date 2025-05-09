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

import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Song;
import com.samet.music.model.User;

/**
 * MetadataEditingView için test sınıfı - Mockito kullanmadan
 */
public class MetadataEditingViewTest {
    
    private ByteArrayOutputStream outContent;
    private final PrintStream originalOut = System.out;
    
    private MetadataEditingView metadataEditingView;
    private TestUserController userController;
    private TestSongController songController;
    private User testUser;
    
    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        // Test kullanıcısı oluştur
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Test kontrolcüleri oluştur
        userController = new TestUserController();
        songController = new TestSongController();
        
        // Test kullanıcısını ayarla
        userController.setCurrentUser(testUser);
    }
    
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testDisplayWhenNoUserLoggedIn() {
        // Kullanıcı oturumu yok
        userController.setCurrentUser(null);
        
        // Kullanıcı girişini simüle et
        String input = "0\n"; // Geri dön
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        MenuView resultView = metadataEditingView.display();
        
        // Doğrulama
        assertTrue("Kullanıcı girişi olmadığında login menüsüne dönmeli", resultView instanceof LoginMenuView);
        assertTrue(outContent.toString().contains("No user logged in"));
    }
    
    @Test
    public void testBackToMainMenu() {
        // Ana menüye dönme durumunu ayrı test et
        String input = "0\n"; // Ana menüye dön
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        MenuView resultView = metadataEditingView.display();
        
        // Özel bir sınıf yerine genel davranışı kontrol et
        assertNotNull("Dönüş değeri null olmamalı", resultView);
        assertFalse("Metadata menüsüne dönmemeli", resultView instanceof MetadataEditingView);
    }
    
    @Test
    public void testInvalidMenuChoice() {
        // Kullanıcı girdisini simüle et - geçersiz seçim
        String input = "9\n0\n"; // Geçersiz seçim, sonra ana menüye dön
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        MenuView resultView = metadataEditingView.display();
        
        // Doğrulama
        assertTrue("Geçersiz seçimde kendi menüsüne dönmeli", resultView instanceof MetadataEditingView);
        assertTrue(outContent.toString().contains("Invalid choice. Please try again"));
    }
    
    @Test
    public void testEditArtistWhenNoSongs() {
        // Kullanıcının hiç şarkısı yok
        songController.setUserSongs(new ArrayList<>());
        
        // Kullanıcı girdisini simüle et
        String input = "1\n"; // Artist düzenleme seçeneği
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Şarkı yok mesajı gösterilmeli", output.contains("You don't have any songs to edit"));
    }
    
    @Test
    public void testEditArtistCancel() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - iptal et
        String input = "1\n0\n"; // Artist düzenleme seçeneği, sonra iptal
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Şarkı listesi gösterilmeli", output.contains("Test Song 1"));
        assertFalse("Şarkı güncelleme yapılmamalı", songController.isUpdateCalled());
    }
    
    @Test
    public void testEditArtistSongNotFound() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - var olmayan şarkı adı
        String input = "1\nNon-existent Song\n"; // Artist düzenleme seçeneği, var olmayan şarkı
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Şarkı bulunamadı mesajı gösterilmeli", output.contains("Song not found"));
        assertFalse("Şarkı güncelleme yapılmamalı", songController.isUpdateCalled());
    }
    
    @Test
    public void testEditArtistWithEnter() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - şarkı adı, Enter için bekleme, sonra geri dönme
        String input = "1\nTest Song 1\nNew Artist\n\n"; 
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter() çağrıldığını kontrol ediyoruz
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    @Test
    public void testEditArtistSuccess() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(true);
        
        // Kullanıcı girdisini simüle et - başarılı güncelleme
        String input = "1\nTest Song 1\nNew Artist\n"; // Artist düzenleme, şarkı adı, yeni artist
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Artist updated successfully"));
        assertTrue("Şarkı güncelleme çağrılmalı", songController.isUpdateCalled());
        assertEquals("Doğru şarkı ID'si ile güncellenmeli", 1, songController.getLastUpdatedSongId());
        assertEquals("Doğru artist adı ile güncellenmeli", "New Artist", songController.getLastUpdatedArtist());
    }
    
    @Test
    public void testEditArtistFailure() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(false);
        
        // Kullanıcı girdisini simüle et - başarısız güncelleme
        String input = "1\nTest Song 1\nNew Artist\n"; // Artist düzenleme, şarkı adı, yeni artist
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarısız güncelleme mesajı gösterilmeli", output.contains("Failed to update artist"));
    }
    
    @Test
    public void testEditAlbumSuccess() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(true);
        
        // Kullanıcı girdisini simüle et - başarılı güncelleme
        String input = "2\nTest Song 1\nNew Album\n"; // Album düzenleme, şarkı adı, yeni album
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarılı güncelleme mesajı gösterilmeli", output.contains("Album updated successfully"));
        assertTrue("Şarkı güncelleme çağrılmalı", songController.isUpdateCalled());
        assertEquals("Doğru şarkı ID'si güncellenmeli", 1, songController.getLastUpdatedSongId());
        assertEquals("Doğru album güncellenmeli", "New Album", songController.getLastUpdatedAlbum());
    }
    
    @Test
    public void testEditAlbumCancel() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - iptal et
        String input = "2\nTest Song 1\n0\n"; // Album düzenleme, şarkı adı, iptal
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("İptal mesajı gösterilmeli", output.contains("Edit cancelled"));
        assertFalse("Şarkı güncelleme yapılmamalı", songController.isUpdateCalled());
    }
    
    @Test
    public void testEditGenreSuccess() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(true);
        
        // Kullanıcı girdisini simüle et - başarılı güncelleme
        String input = "3\nTest Song 1\nNew Genre\n"; // Genre düzenleme, şarkı adı, yeni tür
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarılı güncelleme mesajı gösterilmeli", output.contains("Genre updated successfully"));
        assertTrue("Şarkı güncelleme çağrılmalı", songController.isUpdateCalled());
        assertEquals("Doğru şarkı ID'si güncellenmeli", 1, songController.getLastUpdatedSongId());
        assertEquals("Doğru tür güncellenmeli", "New Genre", songController.getLastUpdatedGenre());
    }
    
    @Test
    public void testExceptionHandling() {
        // Hata fırlatacak kontrolcü
        songController.setThrowException(true);
        
        // Kullanıcı girdisini simüle et
        String input = "1\n"; // Artist düzenleme seçeneği
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        MenuView resultView = metadataEditingView.display();
        
        // Doğrulama
        assertTrue("Hata durumunda kendi menüsüne dönmeli", resultView instanceof MetadataEditingView);
        assertTrue(outContent.toString().contains("An error occurred"));
    }
    
    // YENİ TESTLER - Kırmızı alanlar için ek testler
    
    @Test
    public void testDirectEditAlbumMethodCall() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Direkt olarak menü seçimini simüle et
        String input = "2\n0\n"; // Album düzenleme seçimi, iptal
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test - display metodunu çağır, bu editAlbum metodunu çağıracak
        metadataEditingView.display();
        
        // Doğrulama - editAlbum metodunun çağrıldığını kontrol et
        String output = outContent.toString();
        assertTrue("Album düzenleme başlığı görüntülenmeli", output.contains("EDIT ALBUM"));
    }
    
    @Test
    public void testDirectEditSongGenreMethodCall() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Direkt olarak menü seçimini simüle et
        String input = "3\n0\n"; // Genre düzenleme seçimi, iptal
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test - display metodunu çağır, bu editSongGenre metodunu çağıracak
        metadataEditingView.display();
        
        // Doğrulama - editSongGenre metodunun çağrıldığını kontrol et
        String output = outContent.toString();
        assertTrue("Genre düzenleme başlığı görüntülenmeli", output.contains("EDIT SONG GENRE"));
    }
    
    @Test
    public void testEditArtistWithWaitForEnter() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - Şarkı bulunamadı ve Enter'a basma
        String input = "1\nNon-existent Song\n\n"; // Artist düzenleme, var olmayan şarkı adı, Enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter metodunun çağrıldığını kontrol et
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    @Test
    public void testEditAlbumWithWaitForEnter() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - Şarkı bulunamadı ve Enter'a basma
        String input = "2\nNon-existent Song\n\n"; // Album düzenleme, var olmayan şarkı adı, Enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter metodunun çağrıldığını kontrol et
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    @Test
    public void testEditAlbumFailure() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(false);
        
        // Kullanıcı girdisini simüle et - Album güncelleme başarısız
        String input = "2\nTest Song 1\nNew Album\n\n"; // Album düzenleme, şarkı adı, yeni album
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarısız güncelleme mesajı gösterilmeli", output.contains("Failed to update album"));
    }
    
    @Test
    public void testEditGenreFailure() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(false);
        
        // Kullanıcı girdisini simüle et - Genre güncelleme başarısız
        String input = "3\nTest Song 1\nNew Genre\n\n"; // Genre düzenleme, şarkı adı, yeni tür
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("Başarısız güncelleme mesajı gösterilmeli", output.contains("Failed to update genre"));
    }
    
    @Test
    public void testEditGenreCancel() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - iptal et
        String input = "3\nTest Song 1\n0\n"; // Genre düzenleme, şarkı adı, iptal
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama
        String output = outContent.toString();
        assertTrue("İptal mesajı gösterilmeli", output.contains("Edit cancelled"));
        assertFalse("Şarkı güncelleme yapılmamalı", songController.isUpdateCalled());
    }
    
    @Test
    public void testEditAlbumWithWaitForEnterAfterSuccess() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(true);
        
        // Kullanıcı girdisini simüle et - başarılı güncelleme ve Enter
        String input = "2\nTest Song 1\nNew Album\n\n"; // Album düzenleme, şarkı adı, yeni album, Enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter metodunun çağrıldığını kontrol et
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    @Test
    public void testEditGenreWithWaitForEnterAfterSuccess() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        songController.setUpdateResult(true);
        
        // Kullanıcı girdisini simüle et - başarılı güncelleme ve Enter
        String input = "3\nTest Song 1\nNew Genre\n\n"; // Genre düzenleme, şarkı adı, yeni tür, Enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter metodunun çağrıldığını kontrol et
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    @Test
    public void testEditGenreWithWaitForEnter() {
        // Test şarkıları
        List<Song> testSongs = createTestSongs();
        songController.setUserSongs(testSongs);
        
        // Kullanıcı girdisini simüle et - Şarkı bulunamadı ve Enter'a basma
        String input = "3\nNon-existent Song\n\n"; // Genre düzenleme, var olmayan şarkı adı, Enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        metadataEditingView = new MetadataEditingView(
                new Scanner(System.in),
                userController,
                songController);
                
        // Test
        metadataEditingView.display();
        
        // Doğrulama - waitForEnter metodunun çağrıldığını kontrol et
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }
    
    // Test verileri oluşturma yardımcı metodu
    private List<Song> createTestSongs() {
        List<Song> songs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setArtist("Test Artist 1");
        song1.setAlbum("Test Album 1");
        song1.setGenre("Rock");
        song1.setUserId(testUser.getId());
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Test Song 2");
        song2.setArtist("Test Artist 2");
        song2.setAlbum("Test Album 2");
        song2.setGenre("Pop");
        song2.setUserId(testUser.getId());
        
        songs.add(song1);
        songs.add(song2);
        
        return songs;
    }
    
    /**
     * Test sınıfları
     */
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
        private boolean updateCalled = false;
        private boolean updateResult = false;
        private boolean throwException = false;
        
        private int lastUpdatedSongId;
        private String lastUpdatedTitle;
        private String lastUpdatedArtist;
        private String lastUpdatedAlbum;
        private String lastUpdatedGenre;
        private int lastUpdatedYear;
        
        public TestSongController() {
            super(null);
        }
        
        @Override
        public List<Song> getUserSongs() {
            if (throwException) {
                throw new RuntimeException("Test exception");
            }
            return userSongs;
        }
        
        public void setUserSongs(List<Song> songs) {
            this.userSongs = songs;
        }
        
        @Override
        public boolean updateSong(int songId, String title, String artist, String album, String genre, int year) {
            updateCalled = true;
            lastUpdatedSongId = songId;
            lastUpdatedTitle = title;
            lastUpdatedArtist = artist;
            lastUpdatedAlbum = album;
            lastUpdatedGenre = genre;
            lastUpdatedYear = year;
            return updateResult;
        }
        
        public void setUpdateResult(boolean result) {
            this.updateResult = result;
        }
        
        public boolean isUpdateCalled() {
            return updateCalled;
        }
        
        public int getLastUpdatedSongId() {
            return lastUpdatedSongId;
        }
        
        public String getLastUpdatedArtist() {
            return lastUpdatedArtist;
        }
        
        public String getLastUpdatedAlbum() {
            return lastUpdatedAlbum;
        }
        
        public String getLastUpdatedGenre() {
            return lastUpdatedGenre;
        }
        
        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }
    }
} 