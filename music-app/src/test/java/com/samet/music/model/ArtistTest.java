package com.samet.music.model;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Artist sınıfı için test sınıfı
 */
public class ArtistTest {

    private Artist artist;
    private Song song1;
    private Song song2;
    private Album album1;
    private Album album2;
    private LocalDateTime testTime;

    @Before
    public void setUp() {
        testTime = LocalDateTime.now();
        artist = new Artist(1, "Test Artist", "Test Bio", 1, testTime);
        
        // Test verileri oluşturma
        song1 = new Song();
        song1.setId(1);
        song1.setTitle("Test Song 1");
        
        song2 = new Song();
        song2.setId(2);
        song2.setTitle("Test Song 2");
        
        album1 = new Album();
        album1.setId(1);
        album1.setTitle("Test Album 1");
        
        album2 = new Album();
        album2.setId(2);
        album2.setTitle("Test Album 2");
    }
    
    @Test
    public void testDefaultConstructor() {
        Artist defaultArtist = new Artist();
        
        // Doğrulama
        assertNotNull("songs listesi null olmamalı", defaultArtist.getSongs());
        assertTrue("songs listesi boş olmalı", defaultArtist.getSongs().isEmpty());
        assertNotNull("albums listesi null olmamalı", defaultArtist.getAlbums());
        assertTrue("albums listesi boş olmalı", defaultArtist.getAlbums().isEmpty());
    }
    
    @Test
    public void testConstructorWithoutId() {
        String testName = "New Artist";
        String testBio = "New Bio";
        int testUserId = 2;
        
        Artist newArtist = new Artist(testName, testBio, testUserId);
        
        // Doğrulama
        assertEquals("İsim doğru olmalı", testName, newArtist.getName());
        assertEquals("Bio doğru olmalı", testBio, newArtist.getBio());
        assertEquals("Kullanıcı ID doğru olmalı", testUserId, newArtist.getUserId());
        assertNotNull("createdAt null olmamalı", newArtist.getCreatedAt());
        assertNotNull("songs listesi null olmamalı", newArtist.getSongs());
        assertNotNull("albums listesi null olmamalı", newArtist.getAlbums());
    }
    
    @Test
    public void testFullConstructor() {
        // Doğrulama
        assertEquals("ID doğru olmalı", 1, artist.getId());
        assertEquals("İsim doğru olmalı", "Test Artist", artist.getName());
        assertEquals("Bio doğru olmalı", "Test Bio", artist.getBio());
        assertEquals("Kullanıcı ID doğru olmalı", 1, artist.getUserId());
        assertEquals("Oluşturma zamanı doğru olmalı", testTime, artist.getCreatedAt());
        assertNotNull("songs listesi null olmamalı", artist.getSongs());
        assertNotNull("albums listesi null olmamalı", artist.getAlbums());
    }
    
    @Test
    public void testSetAndGetId() {
        artist.setId(10);
        assertEquals("ID değeri doğru olmalı", 10, artist.getId());
    }
    
    @Test
    public void testSetAndGetName() {
        artist.setName("Changed Name");
        assertEquals("İsim değeri doğru olmalı", "Changed Name", artist.getName());
    }
    
    @Test
    public void testSetAndGetBio() {
        artist.setBio("Changed Bio");
        assertEquals("Bio değeri doğru olmalı", "Changed Bio", artist.getBio());
    }
    
    @Test
    public void testSetAndGetUserId() {
        artist.setUserId(5);
        assertEquals("Kullanıcı ID değeri doğru olmalı", 5, artist.getUserId());
    }
    
    @Test
    public void testSetAndGetCreatedAt() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        artist.setCreatedAt(newTime);
        assertEquals("Oluşturma zamanı değeri doğru olmalı", newTime, artist.getCreatedAt());
    }
    
    @Test
    public void testSetAndGetSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        
        artist.setSongs(songs);
        
        // Doğrulama
        assertEquals("songs listesi doğru olmalı", songs, artist.getSongs());
        assertEquals("songs listesinde 2 eleman olmalı", 2, artist.getSongs().size());
    }
    
    @Test
    public void testSetAndGetAlbums() {
        List<Album> albums = new ArrayList<>();
        albums.add(album1);
        albums.add(album2);
        
        artist.setAlbums(albums);
        
        // Doğrulama
        assertEquals("albums listesi doğru olmalı", albums, artist.getAlbums());
        assertEquals("albums listesinde 2 eleman olmalı", 2, artist.getAlbums().size());
    }
    
    @Test
    public void testAddSong() {
        // Başlangıçta songs listesi boş
        assertEquals("Başlangıçta songs listesi boş olmalı", 0, artist.getSongs().size());
        
        // Şarkı ekle
        artist.addSong(song1);
        
        // Doğrulama
        assertEquals("songs listesinde 1 eleman olmalı", 1, artist.getSongs().size());
        assertEquals("Eklenen şarkı doğru olmalı", song1, artist.getSongs().get(0));
        
        // İkinci şarkıyı ekle
        artist.addSong(song2);
        
        // Doğrulama
        assertEquals("songs listesinde 2 eleman olmalı", 2, artist.getSongs().size());
        assertEquals("İkinci eklenen şarkı doğru olmalı", song2, artist.getSongs().get(1));
    }
    
    @Test
    public void testAddAlbum() {
        // Başlangıçta albums listesi boş
        assertEquals("Başlangıçta albums listesi boş olmalı", 0, artist.getAlbums().size());
        
        // Albüm ekle
        artist.addAlbum(album1);
        
        // Doğrulama
        assertEquals("albums listesinde 1 eleman olmalı", 1, artist.getAlbums().size());
        assertEquals("Eklenen albüm doğru olmalı", album1, artist.getAlbums().get(0));
        
        // İkinci albümü ekle
        artist.addAlbum(album2);
        
        // Doğrulama
        assertEquals("albums listesinde 2 eleman olmalı", 2, artist.getAlbums().size());
        assertEquals("İkinci eklenen albüm doğru olmalı", album2, artist.getAlbums().get(1));
    }
    
    @Test
    public void testGetSongCount() {
        // Başlangıçta şarkı yok
        assertEquals("Başlangıçta şarkı sayısı 0 olmalı", 0, artist.getSongCount());
        
        // Şarkı ekle
        artist.addSong(song1);
        assertEquals("Şarkı ekledikten sonra sayı 1 olmalı", 1, artist.getSongCount());
        
        // İkinci şarkıyı ekle
        artist.addSong(song2);
        assertEquals("İki şarkı ekledikten sonra sayı 2 olmalı", 2, artist.getSongCount());
    }
    
    @Test
    public void testGetAlbumCount() {
        // Başlangıçta albüm yok
        assertEquals("Başlangıçta albüm sayısı 0 olmalı", 0, artist.getAlbumCount());
        
        // Albüm ekle
        artist.addAlbum(album1);
        assertEquals("Albüm ekledikten sonra sayı 1 olmalı", 1, artist.getAlbumCount());
        
        // İkinci albümü ekle
        artist.addAlbum(album2);
        assertEquals("İki albüm ekledikten sonra sayı 2 olmalı", 2, artist.getAlbumCount());
    }
    
    @Test
    public void testToString() {
        // Test için şarkı ve albüm ekleyelim
        artist.addSong(song1);
        artist.addAlbum(album1);
        
        String expected = "Artist{id=1, name='Test Artist', songCount=1, albumCount=1}";
        assertEquals("toString metodu doğru çıktı vermeli", expected, artist.toString());
    }
} 