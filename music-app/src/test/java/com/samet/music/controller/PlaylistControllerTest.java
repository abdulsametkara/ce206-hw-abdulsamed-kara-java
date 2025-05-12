package com.samet.music.controller;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * PlaylistController için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class PlaylistControllerTest {
    
    @Mock private PlaylistDAO mockPlaylistDAO;
    @Mock private SongDAO mockSongDAO;
    @Mock private UserController mockUserController;
    
    // Test edilecek nesne
    private PlaylistController playlistController;
    
    // Test verileri
    private User testUser;
    private Playlist testPlaylist;
    private Song testSong;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // Test kullanıcısı oluştur
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Test çalma listesi oluştur
        testPlaylist = new Playlist();
        testPlaylist.setId(1);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setDescription("Test Description");
        testPlaylist.setUserId(testUser.getId());
        
        // Test şarkısı oluştur
        testSong = new Song();
        testSong.setId(1);
        testSong.setTitle("Test Song");
        testSong.setArtist("Test Artist");
        
        // PlaylistController oluştur
        playlistController = new PlaylistController(mockUserController);
        
        // Reflection kullanarak PlaylistDAO ve SongDAO alanlarını mocklar ile değiştir
        setPrivateField(playlistController, "playlistDAO", mockPlaylistDAO);
        setPrivateField(playlistController, "songDAO", mockSongDAO);
    }
    
    // Reflection yardımcı metodu - private alanlara erişim için
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    /**
     * Çalma listesi oluşturma metodunu test eder - başarılı durum
     */
    @Test
    public void testCreatePlaylistSuccess() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.create(any(Playlist.class))).thenReturn(testPlaylist);
        
        // Metodu çağır
        Playlist result = playlistController.createPlaylist("Test Playlist", "Test Description");
        
        // Sonuçları doğrula
        assertNotNull("Oluşturulan çalma listesi null olmamalı", result);
        assertEquals("Çalma listesi ID'si doğru olmalı", 1, result.getId());
        assertEquals("Çalma listesi adı doğru olmalı", "Test Playlist", result.getName());
        assertEquals("Çalma listesi açıklaması doğru olmalı", "Test Description", result.getDescription());
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).create(any(Playlist.class));
    }
    
    /**
     * Çalma listesi oluşturma metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testCreatePlaylistNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        Playlist result = playlistController.createPlaylist("Test Playlist", "Test Description");
        
        // Sonuçları doğrula
        assertNull("Kullanıcı giriş yapmamışsa null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Çalma listesi oluşturma metodunu test eder - veritabanı hatası
     */
    @Test
    public void testCreatePlaylistDatabaseError() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.create(any(Playlist.class))).thenReturn(null);
        
        // Metodu çağır
        Playlist result = playlistController.createPlaylist("Test Playlist", "Test Description");
        
        // Sonuçları doğrula
        assertNull("Veritabanı hatası durumunda null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).create(any(Playlist.class));
    }
    
    /**
     * Çalma listesi güncelleme metodunu test eder - başarılı durum
     */
    @Test
    public void testUpdatePlaylistSuccess() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(mockPlaylistDAO.update(any(Playlist.class))).thenReturn(true);
        
        // Metodu çağır
        boolean result = playlistController.updatePlaylist(
            testPlaylist.getId(), "Updated Playlist", "Updated Description");
        
        // Sonuçları doğrula
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
        verify(mockPlaylistDAO).update(any(Playlist.class));
    }
    
    /**
     * Çalma listesi güncelleme metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testUpdatePlaylistNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        boolean result = playlistController.updatePlaylist(
            testPlaylist.getId(), "Updated Playlist", "Updated Description");
        
        // Sonuçları doğrula
        assertFalse("Kullanıcı giriş yapmamışsa false dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Çalma listesi güncelleme metodunu test eder - çalma listesi bulunamadı
     */
    @Test
    public void testUpdatePlaylistNotFound() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.empty());
        
        // Metodu çağır
        boolean result = playlistController.updatePlaylist(
            testPlaylist.getId(), "Updated Playlist", "Updated Description");
        
        // Sonuçları doğrula
        assertFalse("Çalma listesi bulunamadıysa false dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
        verify(mockPlaylistDAO, never()).update(any(Playlist.class));
    }
    
    /**
     * Çalma listesi güncelleme metodunu test eder - kullanıcı yetkisiz
     */
    @Test
    public void testUpdatePlaylistUnauthorized() {
        // Farklı kullanıcı ID'sine sahip çalma listesi oluştur
        Playlist unauthorizedPlaylist = new Playlist();
        unauthorizedPlaylist.setId(testPlaylist.getId());
        unauthorizedPlaylist.setUserId(999); // Farklı kullanıcı ID'si
        
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(unauthorizedPlaylist));
        
        // Metodu çağır
        boolean result = playlistController.updatePlaylist(
            testPlaylist.getId(), "Updated Playlist", "Updated Description");
        
        // Sonuçları doğrula
        assertFalse("Yetkisiz kullanıcı için false dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
        verify(mockPlaylistDAO, never()).update(any(Playlist.class));
    }
    
    /**
     * Çalma listesi silme metodunu test eder - başarılı durum
     */
    @Test
    public void testDeletePlaylistSuccess() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(mockPlaylistDAO.delete(testPlaylist.getId())).thenReturn(true);
        
        // Metodu çağır
        boolean result = playlistController.deletePlaylist(testPlaylist.getId());
        
        // Sonuçları doğrula
        assertTrue("Silme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
        verify(mockPlaylistDAO).delete(testPlaylist.getId());
    }
    
    /**
     * Çalma listesine şarkı ekleme metodunu test eder - başarılı durum
     */
    @Test
    public void testAddSongToPlaylistSuccess() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(mockSongDAO.findById(testSong.getId())).thenReturn(Optional.of(testSong));
        when(mockPlaylistDAO.addSongsToPlaylist(eq(testPlaylist.getId()), any())).thenReturn(true);
        
        // Metodu çağır
        boolean result = playlistController.addSongToPlaylist(testPlaylist.getId(), testSong.getId());
        
        // Sonuçları doğrula
        assertTrue("Şarkı ekleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula - metodlar birden fazla kez çağrılabilir
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO, atLeastOnce()).findById(testPlaylist.getId());
        verify(mockSongDAO, atLeastOnce()).findById(testSong.getId());
        verify(mockPlaylistDAO).addSongsToPlaylist(eq(testPlaylist.getId()), any());
    }
    
    /**
     * Çalma listesine şarkı ekleme metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testAddSongToPlaylistNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        boolean result = playlistController.addSongToPlaylist(testPlaylist.getId(), testSong.getId());
        
        // Sonuçları doğrula
        assertFalse("Kullanıcı giriş yapmamışsa false dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Çalma listesinden şarkı kaldırma metodunu test eder - başarılı durum
     */
    @Test
    public void testRemoveSongFromPlaylistSuccess() {
        // Test çalma listesine şarkı ekle
        List<Song> songs = new ArrayList<>();
        songs.add(testSong);
        testPlaylist.setSongs(songs);
        
        // Güncellenen çalma listesi
        Playlist updatedPlaylist = new Playlist();
        updatedPlaylist.setId(testPlaylist.getId());
        updatedPlaylist.setName(testPlaylist.getName());
        updatedPlaylist.setUserId(testPlaylist.getUserId());
        updatedPlaylist.setSongs(new ArrayList<>()); // Şarkı kaldırıldıktan sonra boş liste
        
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(mockPlaylistDAO.update(any(Playlist.class))).thenReturn(true);
        
        // Metodu çağır
        boolean result = playlistController.removeSongFromPlaylist(testPlaylist.getId(), testSong.getId());
        
        // Sonuçları doğrula
        assertTrue("Şarkı kaldırma başarılı olmalı", result);
        
        // Etkileşimleri doğrula - metodlar birden fazla kez çağrılabilir
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO, atLeastOnce()).findById(testPlaylist.getId());
        verify(mockPlaylistDAO).update(any(Playlist.class));
    }
    
    /**
     * Çalma listesinden şarkı çıkarma metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testRemoveSongFromPlaylistNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        boolean result = playlistController.removeSongFromPlaylist(testPlaylist.getId(), testSong.getId());
        
        // Sonuçları doğrula
        assertFalse("Kullanıcı giriş yapmamışsa false dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Kullanıcının çalma listelerini getirme metodunu test eder - başarılı durum
     */
    @Test
    public void testGetUserPlaylistsSuccess() {
        // Çalma listeleri oluştur
        List<Playlist> playlists = Arrays.asList(testPlaylist);
        
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findByUserId(testUser.getId())).thenReturn(playlists);
        
        // Metodu çağır
        List<Playlist> result = playlistController.getUserPlaylists();
        
        // Sonuçları doğrula
        assertNotNull("Sonuç null olmamalı", result);
        assertEquals("1 çalma listesi dönmeli", 1, result.size());
        assertEquals("Çalma listesi ID'si doğru olmalı", testPlaylist.getId(), result.get(0).getId());
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findByUserId(testUser.getId());
    }
    
    /**
     * Kullanıcının çalma listelerini getirme metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testGetUserPlaylistsNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        List<Playlist> result = playlistController.getUserPlaylists();
        
        // Sonuçları doğrula
        assertNotNull("Sonuç null olmamalı", result);
        assertTrue("Boş liste dönmeli", result.isEmpty());
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Belirli bir çalma listesini getirme metodunu test eder - başarılı durum
     */
    @Test
    public void testGetPlaylistSuccess() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        
        // Metodu çağır
        Playlist result = playlistController.getPlaylist(testPlaylist.getId());
        
        // Sonuçları doğrula
        assertNotNull("Sonuç null olmamalı", result);
        assertEquals("Çalma listesi ID'si doğru olmalı", testPlaylist.getId(), result.getId());
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
    }
    
    /**
     * Belirli bir çalma listesini getirme metodunu test eder - kullanıcı giriş yapmamış
     */
    @Test
    public void testGetPlaylistNoUser() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Metodu çağır
        Playlist result = playlistController.getPlaylist(testPlaylist.getId());
        
        // Sonuçları doğrula
        assertNull("Kullanıcı giriş yapmamışsa null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockPlaylistDAO);
    }
    
    /**
     * Belirli bir çalma listesini getirme metodunu test eder - çalma listesi bulunamadı
     */
    @Test
    public void testGetPlaylistNotFound() {
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.empty());
        
        // Metodu çağır
        Playlist result = playlistController.getPlaylist(testPlaylist.getId());
        
        // Sonuçları doğrula
        assertNull("Çalma listesi bulunamadıysa null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
    }
    
    /**
     * Belirli bir çalma listesini getirme metodunu test eder - kullanıcı yetkisiz
     */
    @Test
    public void testGetPlaylistUnauthorized() {
        // Farklı kullanıcı ID'sine sahip çalma listesi oluştur
        Playlist unauthorizedPlaylist = new Playlist();
        unauthorizedPlaylist.setId(testPlaylist.getId());
        unauthorizedPlaylist.setUserId(999); // Farklı kullanıcı ID'si
        
        // Mock davranışlarını ayarla
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockPlaylistDAO.findById(testPlaylist.getId())).thenReturn(Optional.of(unauthorizedPlaylist));
        
        // Metodu çağır
        Playlist result = playlistController.getPlaylist(testPlaylist.getId());
        
        // Sonuçları doğrula
        assertNull("Yetkisiz kullanıcı için null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockUserController).getCurrentUser();
        verify(mockPlaylistDAO).findById(testPlaylist.getId());
    }
} 