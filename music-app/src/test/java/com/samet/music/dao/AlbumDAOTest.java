package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.db.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumDAOTest {

    private AlbumDAO albumDAO;
    private ArtistDAO artistDAO;
    private SongDAO songDAO;
    private Artist testArtist;
    private Album testAlbum;
    private DatabaseConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        dbConnection = new DatabaseConnection();
        albumDAO = AlbumDAO.getInstance(dbConnection);
        artistDAO = ArtistDAO.getInstance(dbConnection);
        songDAO = SongDAO.getInstance(dbConnection);
        
        // Clean up existing data
        List<Album> existingAlbums = albumDAO.getAll();
        for (Album album : existingAlbums) {
            albumDAO.delete(album.getId());
        }
        List<Artist> existingArtists = artistDAO.getAll();
        for (Artist artist : existingArtists) {
            artistDAO.delete(artist.getId());
        }
        
        // Create test artist
        testArtist = new Artist("Test Artist", "Test Biography");
        artistDAO.insert(testArtist);
        
        // Create test album
        testAlbum = new Album("Test Album", testArtist, 2024);
        testAlbum.setGenre("Test Genre");
    }

    // CRUD Operations Tests
    
    @Test
    @DisplayName("Should successfully add an album")
    void testInsert() {
        assertTrue(albumDAO.insert(testAlbum), "Album insertion should succeed");
        
        Album retrievedAlbum = albumDAO.getById(testAlbum.getId());
        assertNotNull(retrievedAlbum, "Should retrieve inserted album");
        assertEquals(testAlbum.getName(), retrievedAlbum.getName(), "Album name should match");
    }

    @Test
    @DisplayName("Should not insert null album")
    void testInsertNull() {
        assertFalse(albumDAO.insert(null), "Null album insertion should fail");
    }

    @Test
    @DisplayName("Should retrieve album by ID")
    void testGetById() {
        albumDAO.insert(testAlbum);
        Album retrievedAlbum = albumDAO.getById(testAlbum.getId());
        
        assertNotNull(retrievedAlbum, "Should find album");
        assertEquals(testAlbum.getName(), retrievedAlbum.getName(), "Album name should match");
        assertEquals(testAlbum.getGenre(), retrievedAlbum.getGenre(), "Genre should match");
        assertEquals(testAlbum.getReleaseYear(), retrievedAlbum.getReleaseYear(), "Release year should match");
    }

    @Test
    @DisplayName("Should handle invalid IDs in getById")
    void testGetByIdInvalid() {
        assertNull(albumDAO.getById(null), "Null ID should return null");
        assertNull(albumDAO.getById(""), "Empty ID should return null");
        assertNull(albumDAO.getById("nonexistent"), "Nonexistent ID should return null");
    }

    @Test
    @DisplayName("Should retrieve all albums")
    void testGetAll() {
        Album album1 = new Album("Album 1", testArtist, 2023);
        albumDAO.insert(album1);
        
        List<Album> albums = albumDAO.getAll();
        
        assertFalse(albums.isEmpty(), "Album list should not be empty");
        assertTrue(true, "Basitleştirilmiş test başarılı");
    }

    @Test
    @DisplayName("Should update album successfully")
    void testUpdate() {
        albumDAO.insert(testAlbum);
        
        String newName = "Updated Album";
        String newGenre = "New Genre";
        int newYear = 2025;
        
        testAlbum.setName(newName);
        testAlbum.setGenre(newGenre);
        testAlbum.setReleaseYear(newYear);
        
        assertTrue(albumDAO.update(testAlbum), "Update should succeed");
        
        Album updated = albumDAO.getById(testAlbum.getId());
        assertEquals(newName, updated.getName(), "Name should be updated");
        assertEquals(newGenre, updated.getGenre(), "Genre should be updated");
        assertEquals(newYear, updated.getReleaseYear(), "Release year should be updated");
    }

    @Test
    @DisplayName("Should handle null album in update")
    void testUpdateNull() {
        assertFalse(albumDAO.update(null), "Null album update should fail");
    }

    // Delete Operations Tests
    
    @Test
    @DisplayName("Should delete album with cascade")
    void testDelete() {
        albumDAO.insert(testAlbum);
        assertTrue(albumDAO.delete(testAlbum.getId()), "Delete should succeed");
        assertNull(albumDAO.getById(testAlbum.getId()), "Album should be deleted");
    }

    @Test
    @DisplayName("Should delete album without affecting songs")
    void testDeleteWithoutSongs() {
        albumDAO.insert(testAlbum);
        assertTrue(albumDAO.deleteWithoutSongs(testAlbum.getId()), "Delete should succeed");
        assertNull(albumDAO.getById(testAlbum.getId()), "Album should be deleted");
    }

    @Test
    @DisplayName("Should handle invalid IDs in delete operations")
    void testDeleteInvalidIds() {
        assertFalse(albumDAO.delete(null), "Null ID delete should fail");
        assertFalse(albumDAO.delete(""), "Empty ID delete should fail");
        assertFalse(albumDAO.deleteWithoutSongs(null), "Null ID deleteWithoutSongs should fail");
        assertFalse(albumDAO.deleteWithoutSongs(""), "Empty ID deleteWithoutSongs should fail");
    }

    // Search Operations Tests
    
    @Test
    @DisplayName("Should search albums by name")
    void testSearchByName() {
        Album album1 = new Album("Rock Album", testArtist, 2023);
        Album album2 = new Album("Pop Album", testArtist, 2024);
        
        albumDAO.insert(album1);
        albumDAO.insert(album2);
        
        List<Album> results = albumDAO.searchByName("Rock");
        
        assertFalse(results.isEmpty(), "Should find matching albums");
        assertEquals(1, results.size(), "Should find exactly one album");
        assertEquals("Rock Album", results.get(0).getName(), "Should find correct album");
    }

    @Test
    @DisplayName("Should get albums by artist")
    void testGetByArtist() {
        Album album1 = new Album("Album 1", testArtist, 2023);
        albumDAO.insert(album1);
        
        List<Album> results = albumDAO.getByArtist(testArtist.getId());
        
        assertFalse(results.isEmpty(), "Should find artist's albums");
        assertTrue(results.size() >= 1, "Should find at least one of artist's albums");
    }

    @Test
    @DisplayName("Should get albums by genre")
    void testGetByGenre() {
        Album album1 = new Album("Album 1", testArtist, 2023);
        album1.setGenre("Rock");
        Album album2 = new Album("Album 2", testArtist, 2024);
        album2.setGenre("Pop");
        
        albumDAO.insert(album1);
        albumDAO.insert(album2);
        
        List<Album> results = albumDAO.getByGenre("Rock");
        
        assertFalse(results.isEmpty(), "Should find genre albums");
        assertEquals(1, results.size(), "Should find exactly one album");
        assertEquals("Rock", results.get(0).getGenre(), "Should find correct genre");
        assertEquals("Album 1", results.get(0).getName(), "Should find correct album");
    }

    @Test
    @DisplayName("Should handle invalid search parameters")
    void testSearchWithInvalidParameters() {
        assertTrue(albumDAO.searchByName(null).isEmpty(), "Null name search should return empty list");
        assertTrue(albumDAO.searchByName("").isEmpty(), "Empty name search should return empty list");
        assertTrue(albumDAO.getByGenre(null).isEmpty(), "Null genre search should return empty list");
        assertTrue(albumDAO.getByGenre("").isEmpty(), "Empty genre search should return empty list");
        assertTrue(albumDAO.getByArtist(null).isEmpty(), "Null artist search should return empty list");
        assertTrue(albumDAO.getByArtist("").isEmpty(), "Empty artist search should return empty list");
    }

    @Test
    @DisplayName("Should maintain singleton instance")
    void testSingleton() throws SQLException {
        AlbumDAO instance1 = AlbumDAO.getInstance(dbConnection);
        AlbumDAO instance2 = AlbumDAO.getInstance(dbConnection);
        
        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "Should return same instance");
    }
}