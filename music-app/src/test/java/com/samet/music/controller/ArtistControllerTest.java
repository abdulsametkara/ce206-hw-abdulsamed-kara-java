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
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ArtistController için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtistControllerTest {
    
    @Mock private SongDAO mockSongDAO;
    @Mock private AlbumDAO mockAlbumDAO;
    
    // Test edilecek nesne
    private ArtistController artistController;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        artistController = new ArtistController(mockSongDAO, mockAlbumDAO);
    }
    
    /**
     * Tüm sanatçıları getirme metodunu test eder
     */
    @Test
    public void testGetAllArtists() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Artist 1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setArtist("Artist 2");
        
        List<Song> songs = Arrays.asList(song1, song2);
        
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Artist 2");
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setArtist("Artist 3");
        
        List<Album> albums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        Set<String> result = artistController.getAllArtists();
        
        // Sonuçları doğrula
        assertEquals("3 sanatçı dönmeli", 3, result.size());
        assertTrue("Artist 1 olmalı", result.contains("Artist 1"));
        assertTrue("Artist 2 olmalı", result.contains("Artist 2"));
        assertTrue("Artist 3 olmalı", result.contains("Artist 3"));
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * Boş sanatçı listesi durumunu test eder
     */
    @Test
    public void testGetAllArtistsEmpty() {
        // Test verileri - Boş listeler
        List<Song> songs = new ArrayList<>();
        List<Album> albums = new ArrayList<>();
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        Set<String> result = artistController.getAllArtists();
        
        // Sonuçları doğrula
        assertTrue("Boş set dönmeli", result.isEmpty());
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * Null sanatçıları filtreleme durumunu test eder
     */
    @Test
    public void testGetAllArtistsFilterNull() {
        // Test verileri - Şarkılar (biri null artist)
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Artist 1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setArtist(null);
        
        List<Song> songs = Arrays.asList(song1, song2);
        
        // Test verileri - Albümler (biri boş artist)
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Artist 2");
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setArtist("");
        
        List<Album> albums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        Set<String> result = artistController.getAllArtists();
        
        // Sonuçları doğrula
        assertEquals("2 sanatçı dönmeli", 2, result.size());
        assertTrue("Artist 1 olmalı", result.contains("Artist 1"));
        assertTrue("Artist 2 olmalı", result.contains("Artist 2"));
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * artistExists metodunu test eder - sanatçı varsa
     */
    @Test
    public void testArtistExistsPositive() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Artist 1");
        
        List<Song> songs = Arrays.asList(song1);
        
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Artist 2");
        
        List<Album> albums = Arrays.asList(album1);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        boolean result = artistController.artistExists("Artist 1");
        
        // Sonuçları doğrula
        assertTrue("Sanatçı mevcut olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * artistExists metodunu test eder - sanatçı yoksa
     */
    @Test
    public void testArtistExistsNegative() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Artist 1");
        
        List<Song> songs = Arrays.asList(song1);
        
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Artist 2");
        
        List<Album> albums = Arrays.asList(album1);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        boolean result = artistController.artistExists("Unknown Artist");
        
        // Sonuçları doğrula
        assertFalse("Sanatçı mevcut olmamalı", result);
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verify(mockAlbumDAO).findAll();
    }
    
    /**
     * artistExists metodunu test eder - null sanatçı adı
     */
    @Test
    public void testArtistExistsNullName() {
        // Metodu çağır
        boolean result = artistController.artistExists(null);
        
        // Sonuçları doğrula
        assertFalse("Null sanatçı adı için false dönmeli", result);
        
        // Etkileşim olmamalı
        verifyZeroInteractions(mockSongDAO);
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * artistExists metodunu test eder - boş sanatçı adı
     */
    @Test
    public void testArtistExistsEmptyName() {
        // Metodu çağır
        boolean result = artistController.artistExists("");
        
        // Sonuçları doğrula
        assertFalse("Boş sanatçı adı için false dönmeli", result);
        
        // Etkileşim olmamalı
        verifyZeroInteractions(mockSongDAO);
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * getArtistSongCount metodunu test eder - normal durum
     */
    @Test
    public void testGetArtistSongCount() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Test Artist");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setArtist("Test Artist");
        
        Song song3 = new Song();
        song3.setId(3);
        song3.setArtist("Other Artist");
        
        List<Song> songs = Arrays.asList(song1, song2, song3);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        
        // Metodu çağır
        int result = artistController.getArtistSongCount("Test Artist");
        
        // Sonuçları doğrula
        assertEquals("İki şarkı olmalı", 2, result);
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * getArtistSongCount metodunu test eder - büyük-küçük harf duyarsız
     */
    @Test
    public void testGetArtistSongCountCaseInsensitive() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Test Artist");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setArtist("TEST ARTIST");
        
        List<Song> songs = Arrays.asList(song1, song2);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        
        // Metodu çağır
        int result = artistController.getArtistSongCount("test artist");
        
        // Sonuçları doğrula
        assertEquals("İki şarkı olmalı", 2, result);
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * getArtistSongCount metodunu test eder - sanatçı bulunamama durumu
     */
    @Test
    public void testGetArtistSongCountArtistNotFound() {
        // Test verileri - Şarkılar
        Song song1 = new Song();
        song1.setId(1);
        song1.setArtist("Test Artist");
        
        List<Song> songs = Arrays.asList(song1);
        
        // Mock davranışlarını ayarla
        when(mockSongDAO.findAll()).thenReturn(songs);
        
        // Metodu çağır
        int result = artistController.getArtistSongCount("Unknown Artist");
        
        // Sonuçları doğrula
        assertEquals("Şarkı sayısı sıfır olmalı", 0, result);
        
        // Etkileşimleri doğrula
        verify(mockSongDAO).findAll();
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * getArtistSongCount metodunu test eder - null sanatçı adı
     */
    @Test
    public void testGetArtistSongCountNullName() {
        // Metodu çağır
        int result = artistController.getArtistSongCount(null);
        
        // Sonuçları doğrula
        assertEquals("Null sanatçı adı için sıfır dönmeli", 0, result);
        
        // Etkileşim olmamalı
        verifyZeroInteractions(mockSongDAO);
        verifyZeroInteractions(mockAlbumDAO);
    }
    
    /**
     * getArtistAlbumCount metodunu test eder - normal durum
     */
    @Test
    public void testGetArtistAlbumCount() {
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Test Artist");
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setArtist("Test Artist");
        
        Album album3 = new Album();
        album3.setId(3);
        album3.setArtist("Other Artist");
        
        List<Album> albums = Arrays.asList(album1, album2, album3);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        int result = artistController.getArtistAlbumCount("Test Artist");
        
        // Sonuçları doğrula
        assertEquals("İki albüm olmalı", 2, result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findAll();
        verifyZeroInteractions(mockSongDAO);
    }
    
    /**
     * getArtistAlbumCount metodunu test eder - büyük-küçük harf duyarsız
     */
    @Test
    public void testGetArtistAlbumCountCaseInsensitive() {
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Test Artist");
        
        Album album2 = new Album();
        album2.setId(2);
        album2.setArtist("TEST ARTIST");
        
        List<Album> albums = Arrays.asList(album1, album2);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        int result = artistController.getArtistAlbumCount("test artist");
        
        // Sonuçları doğrula
        assertEquals("İki albüm olmalı", 2, result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findAll();
        verifyZeroInteractions(mockSongDAO);
    }
    
    /**
     * getArtistAlbumCount metodunu test eder - sanatçı bulunamama durumu
     */
    @Test
    public void testGetArtistAlbumCountArtistNotFound() {
        // Test verileri - Albümler
        Album album1 = new Album();
        album1.setId(1);
        album1.setArtist("Test Artist");
        
        List<Album> albums = Arrays.asList(album1);
        
        // Mock davranışlarını ayarla
        when(mockAlbumDAO.findAll()).thenReturn(albums);
        
        // Metodu çağır
        int result = artistController.getArtistAlbumCount("Unknown Artist");
        
        // Sonuçları doğrula
        assertEquals("Albüm sayısı sıfır olmalı", 0, result);
        
        // Etkileşimleri doğrula
        verify(mockAlbumDAO).findAll();
        verifyZeroInteractions(mockSongDAO);
    }
    
    /**
     * getArtistAlbumCount metodunu test eder - null sanatçı adı
     */
    @Test
    public void testGetArtistAlbumCountNullName() {
        // Metodu çağır
        int result = artistController.getArtistAlbumCount(null);
        
        // Sonuçları doğrula
        assertEquals("Null sanatçı adı için sıfır dönmeli", 0, result);
        
        // Etkileşim olmamalı
        verifyZeroInteractions(mockSongDAO);
        verifyZeroInteractions(mockAlbumDAO);
    }
} 