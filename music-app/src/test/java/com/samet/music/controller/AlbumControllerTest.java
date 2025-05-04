package com.samet.music.controller;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * AlbumController için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class AlbumControllerTest {
    
    @Mock private AlbumDAO mockAlbumDAO;
    @Mock private SongDAO mockSongDAO;
    
    // Test edilecek nesne
    private AlbumController albumController;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        albumController = new AlbumController(mockAlbumDAO, mockSongDAO);
    }
    
    /**
     * Album oluşturma metodunu test eder
     */
    @Test
    public void testCreateAlbum() {
        // Test verileri
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setUserId(1);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.create(album)).thenReturn(true);
        
        // Metodu çağır
        boolean result = albumController.createAlbum(album);
        
        // Sonuçları doğrula
        assertTrue("Album oluşturma başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).create(album);
    }
    
    /**
     * Album oluşturma başarısız durumunu test eder
     */
    @Test
    public void testCreateAlbumFailure() {
        // Test verileri
        Album album = new Album();
        album.setTitle("Test Album");
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.create(album)).thenReturn(false);
        
        // Metodu çağır
        boolean result = albumController.createAlbum(album);
        
        // Sonuçları doğrula
        assertFalse("Album oluşturma başarısız olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).create(album);
    }
    
    /**
     * ID ile album getirme metodunu test eder
     */
    @Test
    public void testGetAlbumById() {
        // Test verileri
        int albumId = 1;
        Album expectedAlbum = new Album();
        expectedAlbum.setId(albumId);
        expectedAlbum.setTitle("Test Album");
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findById(albumId)).thenReturn(expectedAlbum);
        
        // Metodu çağır
        Album result = albumController.getAlbumById(albumId);
        
        // Sonuçları doğrula
        assertNotNull("Album bulunmalı", result);
        assertEquals("Album ID eşleşmeli", albumId, result.getId());
        assertEquals("Album başlığı eşleşmeli", "Test Album", result.getTitle());
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findById(albumId);
    }
    
    /**
     * Tüm albumleri getirme metodunu test eder
     */
    @Test
    public void testGetAllAlbums() {
        // Test verileri
        Album album1 = new Album();
        album1.setId(1);
        album1.setTitle("Album 1");
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setTitle("Album 2");
        
        List<Album> expectedAlbums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findAll()).thenReturn(expectedAlbums);
        
        // Metodu çağır
        List<Album> result = albumController.getAllAlbums();
        
        // Sonuçları doğrula
        assertEquals("2 album dönmeli", 2, result.size());
        assertEquals("Album 1 eşleşmeli", "Album 1", result.get(0).getTitle());
        assertEquals("Album 2 eşleşmeli", "Album 2", result.get(1).getTitle());
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * Kullanıcı ID'sine göre album getirme metodunu test eder
     */
    @Test
    public void testGetAlbumsByUserId() {
        // Test verileri
        int userId = 1;
        
        Album album1 = new Album();
        album1.setId(1);
        album1.setTitle("Album 1");
        album1.setUserId(userId);
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setTitle("Album 2");
        album2.setUserId(userId);
        
        List<Album> expectedAlbums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findByUserId(userId)).thenReturn(expectedAlbums);
        
        // Metodu çağır
        List<Album> result = albumController.getAlbumsByUserId(userId);
        
        // Sonuçları doğrula
        assertEquals("2 album dönmeli", 2, result.size());
        assertEquals("Album 1 eşleşmeli", "Album 1", result.get(0).getTitle());
        assertEquals("Album 2 eşleşmeli", "Album 2", result.get(1).getTitle());
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findByUserId(userId);
    }
    
    /**
     * Sanatçıya göre album getirme metodunu test eder
     */
    @Test
    public void testGetAlbumsByArtist() {
        // Test verileri
        String artist = "Test Artist";
        
        Album album1 = new Album();
        album1.setId(1);
        album1.setTitle("Album 1");
        album1.setArtist(artist);
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setTitle("Album 2");
        album2.setArtist(artist);
        
        List<Album> expectedAlbums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findByArtist(artist)).thenReturn(expectedAlbums);
        
        // Metodu çağır
        List<Album> result = albumController.getAlbumsByArtist(artist);
        
        // Sonuçları doğrula
        assertEquals("2 album dönmeli", 2, result.size());
        assertEquals("Album 1 eşleşmeli", "Album 1", result.get(0).getTitle());
        assertEquals("Album 2 eşleşmeli", "Album 2", result.get(1).getTitle());
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findByArtist(artist);
    }
    
    /**
     * Album güncelleme metodunu test eder
     */
    @Test
    public void testUpdateAlbum() {
        // Test verileri
        Album album = new Album();
        album.setId(1);
        album.setTitle("Updated Album");
        album.setArtist("Updated Artist");
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.update(album)).thenReturn(true);
        
        // Metodu çağır
        boolean result = albumController.updateAlbum(album);
        
        // Sonuçları doğrula
        assertTrue("Album güncelleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).update(album);
    }
    
    /**
     * Album silme metodunu test eder
     */
    @Test
    public void testDeleteAlbum() {
        // Test verileri
        int albumId = 1;
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.delete(albumId)).thenReturn(true);
        
        // Metodu çağır
        boolean result = albumController.deleteAlbum(albumId);
        
        // Sonuçları doğrula
        assertTrue("Album silme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).delete(albumId);
    }
    
    /**
     * Albüme şarkı ekleme metodunu test eder
     */
    @Test
    public void testAddSongsToAlbum() {
        // Test verileri
        int albumId = 1;
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Song 1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Song 2");
        
        songs.add(song1);
        songs.add(song2);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.addSongsToAlbum(albumId, songs)).thenReturn(true);
        
        // Metodu çağır
        boolean result = albumController.addSongsToAlbum(albumId, songs);
        
        // Sonuçları doğrula
        assertTrue("Şarkı ekleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).addSongsToAlbum(albumId, songs);
    }
    
    /**
     * Albümden şarkı kaldırma metodunu test eder
     */
    @Test
    public void testRemoveSongsFromAlbum() {
        // Test verileri
        int albumId = 1;
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.removeSongsFromAlbum(albumId)).thenReturn(true);
        
        // Metodu çağır
        boolean result = albumController.removeSongsFromAlbum(albumId);
        
        // Sonuçları doğrula
        assertTrue("Şarkı kaldırma başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).removeSongsFromAlbum(albumId);
    }
    
    /**
     * Album var mı kontrolünü test eder - var olma durumu
     */
    @Test
    public void testExistsPositive() {
        // Test verileri
        String title = "Test Album";
        String artist = "Test Artist";
        
        Album album = new Album();
        album.setId(1);
        album.setTitle(title);
        album.setArtist(artist);
        
        List<Album> albums = Arrays.asList(album);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findByArtist(artist)).thenReturn(albums);
        
        // Metodu çağır
        boolean result = albumController.exists(title, artist);
        
        // Sonuçları doğrula
        assertTrue("Album mevcut olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findByArtist(artist);
    }
    
    /**
     * Album var mı kontrolünü test eder - olmama durumu
     */
    @Test
    public void testExistsNegative() {
        // Test verileri
        String title = "Test Album";
        String artist = "Test Artist";
        
        Album album = new Album();
        album.setId(1);
        album.setTitle("Different Title");
        album.setArtist(artist);
        
        List<Album> albums = Arrays.asList(album);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findByArtist(artist)).thenReturn(albums);
        
        // Metodu çağır
        boolean result = albumController.exists(title, artist);
        
        // Sonuçları doğrula
        assertFalse("Album mevcut olmamalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findByArtist(artist);
    }
    
    /**
     * Album var mı kontrolünü test eder - albüm listesi boş olma durumu
     */
    @Test
    public void testExistsEmptyList() {
        // Test verileri
        String title = "Test Album";
        String artist = "Test Artist";
        
        List<Album> albums = new ArrayList<>();
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findByArtist(artist)).thenReturn(albums);
        
        // Metodu çağır
        boolean result = albumController.exists(title, artist);
        
        // Sonuçları doğrula
        assertFalse("Album mevcut olmamalı", result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findByArtist(artist);
    }
} 