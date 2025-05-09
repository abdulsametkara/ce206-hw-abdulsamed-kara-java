package com.samet.music.controller;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.Artist;
import com.samet.music.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ArtistController için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtistControllerTest {
    
    @Mock
    private SongDAO mockSongDAO;
    
    @Mock
    private AlbumDAO mockAlbumDAO;
    
    @Mock
    private ArtistDAO mockArtistDAO;
    
    @Mock
    private UserController mockUserController;
    
    // Test edilecek nesne
    private ArtistController artistController;
    
    private User testUser;
    private Artist testArtist;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // Test kullanıcısı oluştur
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Test sanatçısı oluştur
        testArtist = new Artist("Test Artist", "Test Bio", testUser.getId());
        testArtist.setId(1);
        
        // Controller'ı oluştur
        artistController = new ArtistController(mockArtistDAO, mockSongDAO, mockAlbumDAO, mockUserController);
    }
    
    /**
     * Tüm sanatçıları getirme metodunu test eder
     */
    @Test
    public void testGetAllArtists() {
        // Test verisi
        Set<String> expectedArtists = new HashSet<>();
        expectedArtists.add("Artist 1");
        expectedArtists.add("Artist 2");
        
        // Mock davranışı
        when(mockArtistDAO.getAllArtistNames()).thenReturn(expectedArtists);
        
        // Test
        Set<String> result = artistController.getAllArtists();
        
        // Doğrulama
        assertEquals(expectedArtists, result);
        verify(mockArtistDAO).getAllArtistNames();
    }
    
    /**
     * artistExists metodunu test eder - sanatçı varsa
     */
    @Test
    public void testArtistExists() {
        // Test verisi
        String artistName = "Test Artist";
        
        // Mock davranışı
        when(mockArtistDAO.artistExists(artistName)).thenReturn(true);
        when(mockArtistDAO.artistExists("Unknown Artist")).thenReturn(false);
        
        // Test
        boolean result1 = artistController.artistExists(artistName);
        boolean result2 = artistController.artistExists("Unknown Artist");
        
        // Doğrulama
        assertTrue(result1);
        assertFalse(result2);
        verify(mockArtistDAO).artistExists(artistName);
        verify(mockArtistDAO).artistExists("Unknown Artist");
    }
    
    /**
     * getArtistSongCount metodunu test eder - normal durum
     */
    @Test
    public void testGetArtistSongCount() {
        // Test verisi
        String artistName = "Test Artist";
        int expectedCount = 10;
        
        // Mock davranışı
        when(mockArtistDAO.getArtistSongCount(artistName)).thenReturn(expectedCount);
        
        // Test
        int result = artistController.getArtistSongCount(artistName);
        
        // Doğrulama
        assertEquals(expectedCount, result);
        verify(mockArtistDAO).getArtistSongCount(artistName);
    }
    
    /**
     * getArtistAlbumCount metodunu test eder - normal durum
     */
    @Test
    public void testGetArtistAlbumCount() {
        // Test verisi
        String artistName = "Test Artist";
        int expectedCount = 3;
        
        // Mock davranışı
        when(mockArtistDAO.getArtistAlbumCount(artistName)).thenReturn(expectedCount);
        
        // Test
        int result = artistController.getArtistAlbumCount(artistName);
        
        // Doğrulama
        assertEquals(expectedCount, result);
        verify(mockArtistDAO).getArtistAlbumCount(artistName);
    }
    
    @Test
    public void testAddArtist_Success() {
        // Test verisi
        String name = "New Artist";
        String bio = "Artist Bio";
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.create(any(Artist.class))).thenAnswer(invocation -> {
            Artist artist = invocation.getArgument(0);
            artist.setId(2);
            return artist;
        });
        
        // Test
        Artist result = artistController.addArtist(name, bio);
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(bio, result.getBio());
        assertEquals(testUser.getId(), result.getUserId());
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).create(any(Artist.class));
    }
    
    @Test
    public void testAddArtist_NullBio() {
        // Test verisi
        String name = "New Artist";
        String bio = null;
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.create(any(Artist.class))).thenAnswer(invocation -> {
            Artist artist = invocation.getArgument(0);
            artist.setId(2);
            return artist;
        });
        
        // Test
        Artist result = artistController.addArtist(name, bio);
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals("", result.getBio());  // Bio should be empty string, not null
        assertEquals(testUser.getId(), result.getUserId());
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).create(any(Artist.class));
    }
    
    @Test
    public void testAddArtist_UserNotLoggedIn() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Test
        Artist result = artistController.addArtist("New Artist", "Bio");
        
        // Doğrulama
        assertNull(result);
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testAddArtist_InvalidName() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        
        // Test
        Artist result1 = artistController.addArtist(null, "Bio");
        Artist result2 = artistController.addArtist("", "Bio");
        Artist result3 = artistController.addArtist("   ", "Bio");
        
        // Doğrulama
        assertNull(result1);
        assertNull(result2);
        assertNull(result3);
        verify(mockUserController, times(3)).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testGetArtistByName_Success() {
        // Test verisi
        String artistName = "Test Artist";
        
        // Mock davranışları
        when(mockArtistDAO.findByName(artistName)).thenReturn(testArtist);
        
        // Test
        Artist result = artistController.getArtistByName(artistName);
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(testArtist, result);
        verify(mockArtistDAO).findByName(artistName);
    }
    
    @Test
    public void testGetArtistByName_InvalidName() {
        // Test
        Artist result1 = artistController.getArtistByName(null);
        Artist result2 = artistController.getArtistByName("");
        Artist result3 = artistController.getArtistByName("   ");
        
        // Doğrulama
        assertNull(result1);
        assertNull(result2);
        assertNull(result3);
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testGetUserArtists_Success() {
        // Test verisi
        List<Artist> expectedArtists = new ArrayList<>();
        expectedArtists.add(testArtist);
        expectedArtists.add(new Artist("Another Artist", "Another Bio", testUser.getId()));
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.findByUserId(testUser.getId())).thenReturn(expectedArtists);
        
        // Test
        List<Artist> result = artistController.getUserArtists();
        
        // Doğrulama
        assertEquals(expectedArtists, result);
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).findByUserId(testUser.getId());
    }
    
    @Test
    public void testGetUserArtists_UserNotLoggedIn() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Test
        List<Artist> result = artistController.getUserArtists();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testUpdateArtist_Success() {
        // Test verisi
        testArtist.setBio("Updated Bio");
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.update(testArtist)).thenReturn(true);
        
        // Test
        boolean result = artistController.updateArtist(testArtist);
        
        // Doğrulama
        assertTrue(result);
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).update(testArtist);
    }
    
    @Test
    public void testUpdateArtist_UserNotLoggedIn() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Test
        boolean result = artistController.updateArtist(testArtist);
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testUpdateArtist_NullArtist() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        
        // Test
        boolean result = artistController.updateArtist(null);
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testUpdateArtist_NotOwner() {
        // Test verisi
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(otherUser);
        
        // Test
        boolean result = artistController.updateArtist(testArtist);
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testDeleteArtist_Success() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.findById(testArtist.getId())).thenReturn(testArtist);
        when(mockArtistDAO.delete(testArtist.getId())).thenReturn(true);
        
        // Test
        boolean result = artistController.deleteArtist(testArtist.getId());
        
        // Doğrulama
        assertTrue(result);
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).findById(testArtist.getId());
        verify(mockArtistDAO).delete(testArtist.getId());
    }
    
    @Test
    public void testDeleteArtist_UserNotLoggedIn() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(null);
        
        // Test
        boolean result = artistController.deleteArtist(testArtist.getId());
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verifyNoInteractions(mockArtistDAO);
    }
    
    @Test
    public void testDeleteArtist_ArtistNotFound() {
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(testUser);
        when(mockArtistDAO.findById(testArtist.getId())).thenReturn(null);
        
        // Test
        boolean result = artistController.deleteArtist(testArtist.getId());
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).findById(testArtist.getId());
        verify(mockArtistDAO, never()).delete(anyInt());
    }
    
    @Test
    public void testDeleteArtist_NotOwner() {
        // Test verisi
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");
        
        // Mock davranışları
        when(mockUserController.getCurrentUser()).thenReturn(otherUser);
        when(mockArtistDAO.findById(testArtist.getId())).thenReturn(testArtist);
        
        // Test
        boolean result = artistController.deleteArtist(testArtist.getId());
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserController).getCurrentUser();
        verify(mockArtistDAO).findById(testArtist.getId());
        verify(mockArtistDAO, never()).delete(anyInt());
    }
    
    @Test
    public void testGetSongsByArtist_Success() {
        // Test verisi
        String artistName = "Test Artist";
        List<Song> expectedSongs = new ArrayList<>();
        expectedSongs.add(new Song("Song 1", artistName, "Album 1", "Rock", 2023, 180, "path/to/file", testUser.getId()));
        expectedSongs.add(new Song("Song 2", artistName, "Album 1", "Rock", 2023, 200, "path/to/file", testUser.getId()));
        
        // Mock davranışları
        when(mockSongDAO.findByArtist(artistName)).thenReturn(expectedSongs);
        
        // Test
        List<Song> result = artistController.getSongsByArtist(artistName);
        
        // Doğrulama
        assertEquals(expectedSongs, result);
        verify(mockSongDAO).findByArtist(artistName);
    }
    
    @Test
    public void testGetSongsByArtist_InvalidName() {
        // Test
        List<Song> result1 = artistController.getSongsByArtist(null);
        List<Song> result2 = artistController.getSongsByArtist("");
        List<Song> result3 = artistController.getSongsByArtist("   ");
        
        // Doğrulama
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
        verifyNoInteractions(mockSongDAO);
    }
    
    @Test
    public void testGetAlbumsByArtist_Success() {
        // Test verisi
        String artistName = "Test Artist";
        List<Album> expectedAlbums = new ArrayList<>();
        expectedAlbums.add(new Album("Album 1", artistName, 2023, "Rock", testUser.getId()));
        expectedAlbums.add(new Album("Album 2", artistName, 2022, "Pop", testUser.getId()));
        
        // Mock davranışları
        when(mockAlbumDAO.findByArtist(artistName)).thenReturn(expectedAlbums);
        
        // Test
        List<Album> result = artistController.getAlbumsByArtist(artistName);
        
        // Doğrulama
        assertEquals(expectedAlbums, result);
        verify(mockAlbumDAO).findByArtist(artistName);
    }
    
    @Test
    public void testGetAlbumsByArtist_InvalidName() {
        // Test
        List<Album> result1 = artistController.getAlbumsByArtist(null);
        List<Album> result2 = artistController.getAlbumsByArtist("");
        List<Album> result3 = artistController.getAlbumsByArtist("   ");
        
        // Doğrulama
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
        verifyNoInteractions(mockAlbumDAO);
    }
} 