package com.samet.music.repository;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.model.Artist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtistCollectionTest {

    private ArtistCollection artistCollection;

    @Mock
    private ArtistDAO artistDAO;
    
    @Mock
    private DAOFactory daoFactory;

    private Artist testArtist1;
    private Artist testArtist2;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        testArtist1 = new Artist("Test Artist 1", "Test Biography 1");
        testArtist1.setId("artist1");
        
        testArtist2 = new Artist("Test Artist 2", "Test Biography 2");
        testArtist2.setId("artist2");

        // Mock DAOFactory
        when(daoFactory.getArtistDAO()).thenReturn(artistDAO);
        
        // Reset singleton instance using reflection
        java.lang.reflect.Field instanceField = ArtistCollection.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        
        // Create test instance with protected constructor using reflection
        java.lang.reflect.Constructor<ArtistCollection> constructor = ArtistCollection.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        artistCollection = constructor.newInstance();
        
        // Set mock DAO using reflection
        java.lang.reflect.Field daoField = ArtistCollection.class.getDeclaredField("artistDAO");
        daoField.setAccessible(true);
        daoField.set(artistCollection, artistDAO);
    }

    @Test
    void getInstance_ShouldReturnSameInstance() {
        ArtistCollection instance1 = ArtistCollection.getInstance();
        ArtistCollection instance2 = ArtistCollection.getInstance();
        
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    void add_ShouldAddArtistToCollectionAndDatabase() {
        // Setup
        when(artistDAO.insert(testArtist1)).thenReturn(true);
        when(artistDAO.getAll()).thenReturn(Arrays.asList(testArtist1));
        
        // Execute
        artistCollection.add(testArtist1);
        
        // Verify
        verify(artistDAO, times(1)).insert(testArtist1);
        
        // Get all data and verify
        List<Artist> artists = artistCollection.getAll();
        assertTrue(artists.contains(testArtist1));
    }

    @Test
    void add_ShouldNotAddNullArtist() {
        // Execute
        artistCollection.add(null);
        
        // Verify
        verify(artistDAO, never()).insert(any());
    }

    @Test
    void getById_ShouldReturnNullForInvalidId() {
        // Execute
        Artist result = artistCollection.getById("");
        
        // Verify
        assertNull(result);
        
        // Execute with null
        result = artistCollection.getById(null);
        
        // Verify
        assertNull(result);
    }

    @Test
    void getById_ShouldReturnArtistFromDatabase() {
        // Setup
        when(artistDAO.getById("artist1")).thenReturn(testArtist1);
        
        // Execute
        Artist result = artistCollection.getById("artist1");
        
        // Verify
        assertNotNull(result);
        assertEquals(testArtist1, result);
        verify(artistDAO, times(1)).getById("artist1");
    }

    @Test
    void getById_ShouldReturnCachedArtist() {
        // Setup - Add artist to cache
        when(artistDAO.insert(testArtist1)).thenReturn(true);
        artistCollection.add(testArtist1);
        
        // Execute
        Artist result = artistCollection.getById("artist1");
        
        // Verify - No database call should happen
        assertNotNull(result);
        assertEquals(testArtist1, result);
        verify(artistDAO, never()).getById("artist1");
    }

    @Test
    void getAll_ShouldReturnAllArtistsFromDatabase() {
        // Setup
        when(artistDAO.getAll()).thenReturn(Arrays.asList(testArtist1, testArtist2));
        
        // Execute
        List<Artist> results = artistCollection.getAll();
        
        // Verify
        assertEquals(2, results.size());
        assertTrue(results.contains(testArtist1));
        assertTrue(results.contains(testArtist2));
        verify(artistDAO, times(1)).getAll();
    }

    @Test
    void searchByName_ShouldReturnMatchingArtists() {
        // Setup
        when(artistDAO.getAll()).thenReturn(Arrays.asList(testArtist1, testArtist2));
        
        // Execute
        List<Artist> results = artistCollection.searchByName("Test Artist 1");
        
        // Verify
        assertEquals(1, results.size());
        assertTrue(results.contains(testArtist1));
        assertFalse(results.contains(testArtist2));
    }

    @Test
    void searchByName_ShouldReturnEmptyListForInvalidSearchTerm() {
        // Execute
        List<Artist> results = artistCollection.searchByName("");
        
        // Verify
        assertTrue(results.isEmpty());
        
        // Execute with null
        results = artistCollection.searchByName(null);
        
        // Verify
        assertTrue(results.isEmpty());
    }

    @Test
    void remove_ShouldRemoveArtistFromCollectionAndDatabase() {
        // Setup
        when(artistDAO.insert(testArtist1)).thenReturn(true);
        when(artistDAO.getAll()).thenReturn(Arrays.asList());
        artistCollection.add(testArtist1);
        
        // Execute
        boolean result = artistCollection.remove("artist1");
        
        // Verify
        assertTrue(result);
        verify(artistDAO, times(1)).delete("artist1");
        assertFalse(artistCollection.getAll().contains(testArtist1));
    }

    @Test
    void remove_ShouldReturnFalseForInvalidId() {
        // Execute
        boolean result = artistCollection.remove("");
        
        // Verify
        assertFalse(result);
        
        // Execute with null
        result = artistCollection.remove(null);
        
        // Verify
        assertFalse(result);
    }

    @Test
    void loadFromFile_ShouldLoadArtistsFromDatabase() {
        // Setup
        when(artistDAO.getAll()).thenReturn(Arrays.asList(testArtist1, testArtist2));
        
        // Execute
        boolean result = artistCollection.loadFromFile("dummy/path");
        
        // Verify
        assertTrue(result);
        verify(artistDAO, times(1)).getAll();
    }
    
    @Test
    void loadFromFile_ShouldReturnFalseForEmptyDatabase() {
        // Setup
        when(artistDAO.getAll()).thenReturn(Arrays.asList());
        
        // Execute
        boolean result = artistCollection.loadFromFile("dummy/path");
        
        // Verify
        assertFalse(result);
        verify(artistDAO, times(1)).getAll();
    }
}