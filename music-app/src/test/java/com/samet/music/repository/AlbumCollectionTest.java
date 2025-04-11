package com.samet.music.repository;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlbumCollectionTest {

    private AlbumCollection albumCollection;

    @Mock
    private AlbumDAO albumDAO;
    
    @Mock
    private ArtistDAO artistDAO;
    
    @Mock
    private DAOFactory daoFactory;

    private Album testAlbum;
    private Artist testArtist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        testArtist = new Artist("Test Artist", "Test Biography");
        testArtist.setId("artist1");

        testAlbum = new Album("Test Album", testArtist, 2023);
        testAlbum.setId("album1");
        testAlbum.setGenre("Rock");

        // Mock DAOFactory
        when(daoFactory.getAlbumDAO()).thenReturn(albumDAO);
        when(daoFactory.getArtistDAO()).thenReturn(artistDAO);
        
        // Reset singleton instance
        AlbumCollection.resetInstance();
        
        // Create new instance with mocked dependencies
        albumCollection = new AlbumCollection(daoFactory);
    }

    @Test
    void getInstance_ShouldReturnSameInstance() {
        AlbumCollection instance1 = AlbumCollection.getInstance();
        AlbumCollection instance2 = AlbumCollection.getInstance();
        
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    void add_ShouldAddAlbumToCollectionAndDatabase() {
        // Setup
        Album album = new Album("Test Album", testArtist, 2023);
        album.setId("test1");
        
        // Mock DAO to return true for successful insert
        when(albumDAO.insert(album)).thenReturn(true);
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album));
        
        // Execute
        albumCollection.add(album);
        
        // Verify
        assertTrue(albumCollection.getAll().contains(album));
        verify(albumDAO, times(1)).insert(album);
    }

    @Test
    void add_ShouldNotAddNullAlbum() {
        albumCollection.add(null);
        verify(albumDAO, never()).insert(any());
    }

    @Test
    void getById_ShouldReturnNullForInvalidId() {
        Album result = albumCollection.getById("");
        assertNull(result);
        
        result = albumCollection.getById(null);
        assertNull(result);
    }

    @Test
    void getById_ShouldReturnAlbumFromDatabase() {
        // Setup
        Album album = new Album("Test Album", testArtist, 2023);
        album.setId("test1");
        
        // Mock DAO to return album
        when(albumDAO.getById("test1")).thenReturn(album);
        
        // Execute
        Album result = albumCollection.getById("test1");
        
        // Verify
        assertNotNull(result);
        assertEquals(album, result);
        verify(albumDAO, times(1)).getById("test1");
    }

    @Test
    void searchByName_ShouldReturnMatchingAlbums() {
        // Setup
        Album album1 = new Album("Test Album 1", testArtist, 2023);
        album1.setId("test1");
        Album album2 = new Album("Different Album", testArtist, 2023);
        album2.setId("test2");
        
        // Mock getAll to return our test albums
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album1, album2));
        
        // Execute
        List<Album> results = albumCollection.searchByName("Test");
        
        // Verify
        assertEquals(1, results.size());
        assertTrue(results.contains(album1));
        assertFalse(results.contains(album2));
    }

    @Test
    void getByArtist_ShouldReturnArtistAlbums() {
        // Setup
        Album album1 = new Album("Test Album 1", testArtist, 2023);
        album1.setId("test1");
        Album album2 = new Album("Test Album 2", testArtist, 2023);
        album2.setId("test2");
        
        // Mock getAll to return our test albums
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album1, album2));
        
        // Execute
        List<Album> results = albumCollection.getByArtist(testArtist);
        
        // Verify
        assertEquals(2, results.size());
        assertTrue(results.contains(album1));
        assertTrue(results.contains(album2));
    }

    @Test
    void getByGenre_ShouldReturnGenreAlbums() {
        // Setup
        Album album1 = new Album("Rock Album", testArtist, 2023);
        album1.setGenre("Rock");
        Album album2 = new Album("Pop Album", testArtist, 2023);
        album2.setGenre("Pop");
        
        // Mock getAll to return our test albums
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album1, album2));
        
        // Execute
        List<Album> results = albumCollection.getByGenre("Rock");
        
        // Verify
        assertEquals(1, results.size());
        assertEquals("Rock", results.get(0).getGenre());
    }

    @Test
    void deleteWithoutSongs_ShouldRemoveAlbumPreservingSongs() {
        // Setup
        Album album = new Album("Test Album", testArtist, 2023);
        album.setId("test1");
        
        // Mock getAll to return our test album
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album));
        
        // Mock deleteWithoutSongs to return true
        when(albumDAO.deleteWithoutSongs("test1")).thenReturn(true);
        
        // Execute
        boolean result = albumCollection.deleteWithoutSongs("test1");
        
        // Verify
        assertTrue(result);
        verify(albumDAO, times(1)).deleteWithoutSongs("test1");
    }

    @Test
    void remove_ShouldRemoveAlbumCompletely() {
        // Setup
        Album album = new Album("Test Album", testArtist, 2023);
        album.setId("test1");
        
        // Mock getAll to return our test album
        when(albumDAO.getAll()).thenReturn(Arrays.asList(album));
        
        // Mock delete to return true
        when(albumDAO.delete("test1")).thenReturn(true);
        
        // Execute
        boolean result = albumCollection.remove("test1");
        
        // Verify
        assertTrue(result);
        verify(albumDAO, times(1)).delete("test1");
    }
}