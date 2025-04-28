package com.samet.music.model;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Album sınıfı için test sınıfı
 */
public class AlbumTest {

    private Album album;
    private Song song1;
    private Song song2;
    private Song song3;
    private LocalDateTime testTime;

    @Before
    public void setUp() {
        testTime = LocalDateTime.now();
        album = new Album(1, "Test Album", "Test Artist", 2023, "Rock", 1, testTime);
        
        // Test şarkıları oluştur
        song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        song1.setDuration(180); // 3 dakika
        
        song2 = new Song();
        song2.setId(2);
        song2.setTitle("Test Song 2");
        song2.setDuration(240); // 4 dakika
        
        song3 = new Song();
        song3.setId(3);
        song3.setTitle("Test Song 3");
        song3.setDuration(300); // 5 dakika
    }
    
    @Test
    public void testDefaultConstructor() {
        Album defaultAlbum = new Album();
        
        // Doğrulama
        assertNotNull("songs listesi null olmamalı", defaultAlbum.getSongs());
        assertTrue("songs listesi boş olmalı", defaultAlbum.getSongs().isEmpty());
    }
    
    @Test
    public void testConstructorWithoutId() {
        String testTitle = "New Album";
        String testArtist = "New Artist";
        int testYear = 2022;
        String testGenre = "Pop";
        int testUserId = 2;
        
        Album newAlbum = new Album(testTitle, testArtist, testYear, testGenre, testUserId);
        
        // Doğrulama
        assertEquals("Başlık doğru olmalı", testTitle, newAlbum.getTitle());
        assertEquals("Sanatçı doğru olmalı", testArtist, newAlbum.getArtist());
        assertEquals("Yıl doğru olmalı", Integer.valueOf(testYear), newAlbum.getYear());
        assertEquals("Tür doğru olmalı", testGenre, newAlbum.getGenre());
        assertEquals("Kullanıcı ID doğru olmalı", Integer.valueOf(testUserId), newAlbum.getUserId());
        assertNotNull("songs listesi null olmamalı", newAlbum.getSongs());
        assertTrue("songs listesi boş olmalı", newAlbum.getSongs().isEmpty());
    }
    
    @Test
    public void testFullConstructor() {
        // Doğrulama
        assertEquals("ID doğru olmalı", Integer.valueOf(1), album.getId());
        assertEquals("Başlık doğru olmalı", "Test Album", album.getTitle());
        assertEquals("Sanatçı doğru olmalı", "Test Artist", album.getArtist());
        assertEquals("Yıl doğru olmalı", Integer.valueOf(2023), album.getYear());
        assertEquals("Tür doğru olmalı", "Rock", album.getGenre());
        assertEquals("Kullanıcı ID doğru olmalı", Integer.valueOf(1), album.getUserId());
        assertEquals("Oluşturma zamanı doğru olmalı", testTime, album.getCreatedAt());
        assertNotNull("songs listesi null olmamalı", album.getSongs());
        assertTrue("songs listesi boş olmalı", album.getSongs().isEmpty());
    }
    
    @Test
    public void testSetAndGetId() {
        album.setId(10);
        assertEquals("ID değeri doğru olmalı", Integer.valueOf(10), album.getId());
    }
    
    @Test
    public void testSetAndGetTitle() {
        album.setTitle("Changed Title");
        assertEquals("Başlık değeri doğru olmalı", "Changed Title", album.getTitle());
    }
    
    @Test
    public void testSetAndGetArtist() {
        album.setArtist("Changed Artist");
        assertEquals("Sanatçı değeri doğru olmalı", "Changed Artist", album.getArtist());
    }
    
    @Test
    public void testSetAndGetYear() {
        album.setYear(2024);
        assertEquals("Yıl değeri doğru olmalı", Integer.valueOf(2024), album.getYear());
    }
    
    @Test
    public void testSetAndGetGenre() {
        album.setGenre("Jazz");
        assertEquals("Tür değeri doğru olmalı", "Jazz", album.getGenre());
    }
    
    @Test
    public void testSetAndGetUserId() {
        album.setUserId(5);
        assertEquals("Kullanıcı ID değeri doğru olmalı", Integer.valueOf(5), album.getUserId());
    }
    
    @Test
    public void testSetAndGetCreatedAt() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        album.setCreatedAt(newTime);
        assertEquals("Oluşturma zamanı değeri doğru olmalı", newTime, album.getCreatedAt());
    }
    
    @Test
    public void testSetAndGetSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        
        album.setSongs(songs);
        
        // Doğrulama
        assertEquals("songs listesi doğru olmalı", songs, album.getSongs());
        assertEquals("songs listesinde 2 eleman olmalı", 2, album.getSongs().size());
    }
    
    @Test
    public void testAddSong() {
        // Başlangıçta songs listesi boş
        assertEquals("Başlangıçta songs listesi boş olmalı", 0, album.getSongs().size());
        
        // Şarkı ekle
        album.addSong(song1);
        
        // Doğrulama
        assertEquals("songs listesinde 1 eleman olmalı", 1, album.getSongs().size());
        assertEquals("Eklenen şarkı doğru olmalı", song1, album.getSongs().get(0));
        
        // İkinci şarkıyı ekle
        album.addSong(song2);
        
        // Doğrulama
        assertEquals("songs listesinde 2 eleman olmalı", 2, album.getSongs().size());
        assertEquals("İkinci eklenen şarkı doğru olmalı", song2, album.getSongs().get(1));
    }
    
    @Test
    public void testAddSongWithNullList() {
        // songs listesini null yap
        album.setSongs(null);
        
        // Şarkı ekle
        album.addSong(song1);
        
        // Doğrulama - yeni liste oluşturulmalı ve şarkı eklenmiş olmalı
        assertNotNull("songs listesi null olmamalı", album.getSongs());
        assertEquals("songs listesinde 1 eleman olmalı", 1, album.getSongs().size());
        assertEquals("Eklenen şarkı doğru olmalı", song1, album.getSongs().get(0));
    }
    
    @Test
    public void testRemoveSong() {
        // Şarkıları ekle
        album.addSong(song1);
        album.addSong(song2);
        assertEquals("Başlangıçta 2 şarkı olmalı", 2, album.getSongs().size());
        
        // Şarkı kaldır
        album.removeSong(song1);
        
        // Doğrulama
        assertEquals("songs listesinde 1 eleman olmalı", 1, album.getSongs().size());
        assertEquals("Kalan şarkı doğru olmalı", song2, album.getSongs().get(0));
    }
    
    @Test
    public void testRemoveSongWithNullList() {
        // songs listesini null yap
        album.setSongs(null);
        
        // Şarkı kaldırmayı dene - hata vermemeli
        album.removeSong(song1);
    }
    
    @Test
    public void testRemoveSongById() {
        // Şarkıları ekle
        album.addSong(song1);
        album.addSong(song2);
        album.addSong(song3);
        assertEquals("Başlangıçta 3 şarkı olmalı", 3, album.getSongs().size());
        
        // ID ile şarkı kaldır
        boolean result = album.removeSongById(2); // song2'yi kaldır
        
        // Doğrulama
        assertTrue("Kaldırma işlemi başarılı olmalı", result);
        assertEquals("songs listesinde 2 eleman olmalı", 2, album.getSongs().size());
        assertEquals("İlk kalan şarkı doğru olmalı", song1, album.getSongs().get(0));
        assertEquals("İkinci kalan şarkı doğru olmalı", song3, album.getSongs().get(1));
    }
    
    @Test
    public void testRemoveSongByIdNotFound() {
        // Şarkıları ekle
        album.addSong(song1);
        album.addSong(song2);
        
        // Olmayan bir ID ile şarkı kaldırmayı dene
        boolean result = album.removeSongById(999);
        
        // Doğrulama
        assertFalse("Olmayan şarkıyı kaldırma başarısız olmalı", result);
        assertEquals("songs listesinde hala 2 eleman olmalı", 2, album.getSongs().size());
    }
    
    @Test
    public void testGetSongCount() {
        // Başlangıçta şarkı yok
        assertEquals("Başlangıçta şarkı sayısı 0 olmalı", 0, album.getSongCount());
        
        // Şarkı ekle
        album.addSong(song1);
        assertEquals("Şarkı ekledikten sonra sayı 1 olmalı", 1, album.getSongCount());
        
        // İkinci şarkıyı ekle
        album.addSong(song2);
        assertEquals("İki şarkı ekledikten sonra sayı 2 olmalı", 2, album.getSongCount());
    }
    
    @Test
    public void testGetTotalDuration() {
        // Başlangıçta şarkı yok, toplam süre 0 olmalı
        assertEquals("Başlangıçta toplam süre 0 olmalı", 0, album.getTotalDuration());
        
        // Şarkıları ekle
        album.addSong(song1); // 180 saniye
        album.addSong(song2); // 240 saniye
        album.addSong(song3); // 300 saniye
        
        // Doğrulama
        int expectedDuration = 180 + 240 + 300; // 720 saniye
        assertEquals("Toplam süre doğru hesaplanmalı", expectedDuration, album.getTotalDuration());
    }
    
    @Test
    public void testGetFormattedTotalDuration() {
        // Şarkıları ekle
        album.addSong(song1); // 180 saniye (3 dakika)
        album.addSong(song2); // 240 saniye (4 dakika)
        album.addSong(song3); // 300 saniye (5 dakika)
        
        // Toplam: 720 saniye = 12 dakika = 00:12:00
        String expected = "00:12:00";
        assertEquals("Biçimlendirilmiş süre doğru olmalı", expected, album.getFormattedTotalDuration());
    }
    
    @Test
    public void testGetFormattedTotalDurationWithHours() {
        // Uzun süreli bir şarkı oluştur
        Song longSong = new Song();
        longSong.setId(4);
        longSong.setTitle("Long Song");
        longSong.setDuration(3665); // 1 saat 1 dakika 5 saniye
        
        // Şarkıyı ekle
        album.addSong(longSong);
        
        // Doğrulama
        String expected = "01:01:05";
        assertEquals("Biçimlendirilmiş süre saatleri doğru göstermeli", expected, album.getFormattedTotalDuration());
    }
    
    @Test
    public void testToString() {
        // Test için şarkı ekleyelim
        album.addSong(song1);
        album.addSong(song2);
        
        String expected = "Album{id=1, title='Test Album', artist='Test Artist', year=2023, genre='Rock', userId=1, createdAt=" + testTime + ", songsCount=2}";
        assertEquals("toString metodu doğru çıktı vermeli", expected, album.toString());
    }
    
    @Test
    public void testToStringWithNullSongs() {
        // songs listesini null yap
        album.setSongs(null);
        
        String expected = "Album{id=1, title='Test Album', artist='Test Artist', year=2023, genre='Rock', userId=1, createdAt=" + testTime + ", songsCount=0}";
        assertEquals("toString metodu null songs ile doğru çalışmalı", expected, album.toString());
    }
} 