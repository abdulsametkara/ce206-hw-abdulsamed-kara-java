package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArtistTest {
    
    private Artist artist;
    private final int TEST_ID = 1;
    private final String TEST_NAME = "Test Artist";
    private final String TEST_BIO = "Test Bio";
    private final int TEST_USER_ID = 1;
    private final LocalDateTime TEST_DATE = LocalDateTime.now();
    
    @Before
    public void setUp() {
        // Create a new artist for each test
        artist = new Artist(TEST_ID, TEST_NAME, TEST_BIO, TEST_USER_ID, TEST_DATE);
    }
    
    @Test
    public void testDefaultConstructor() {
        Artist defaultArtist = new Artist();
        assertNotNull(defaultArtist);
        assertNotNull(defaultArtist.getSongs());
        assertNotNull(defaultArtist.getAlbums());
        assertEquals(0, defaultArtist.getSongs().size());
        assertEquals(0, defaultArtist.getAlbums().size());
    }
    
    @Test
    public void testConstructorWithoutId() {
        Artist newArtist = new Artist(TEST_NAME, TEST_BIO, TEST_USER_ID);
        assertNotNull(newArtist);
        assertEquals(TEST_NAME, newArtist.getName());
        assertEquals(TEST_BIO, newArtist.getBio());
        assertEquals(TEST_USER_ID, newArtist.getUserId());
        assertNotNull(newArtist.getCreatedAt());
        assertNotNull(newArtist.getSongs());
        assertNotNull(newArtist.getAlbums());
    }
    
    @Test
    public void testFullConstructor() {
        assertNotNull(artist);
        assertEquals(TEST_ID, artist.getId());
        assertEquals(TEST_NAME, artist.getName());
        assertEquals(TEST_BIO, artist.getBio());
        assertEquals(TEST_USER_ID, artist.getUserId());
        assertEquals(TEST_DATE, artist.getCreatedAt());
        assertNotNull(artist.getSongs());
        assertNotNull(artist.getAlbums());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setId and getId
        artist.setId(2);
        assertEquals(2, artist.getId());
        
        // Test setName and getName
        artist.setName("New Name");
        assertEquals("New Name", artist.getName());
        
        // Test setBio and getBio
        artist.setBio("New Bio");
        assertEquals("New Bio", artist.getBio());
        
        // Test setUserId and getUserId
        artist.setUserId(2);
        assertEquals(2, artist.getUserId());
        
        // Test setCreatedAt and getCreatedAt
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        artist.setCreatedAt(newDate);
        assertEquals(newDate, artist.getCreatedAt());
        
        // Test setSongs and getSongs
        List<Song> songs = new ArrayList<>();
        songs.add(new Song());
        artist.setSongs(songs);
        assertEquals(songs, artist.getSongs());
        
        // Test setAlbums and getAlbums
        List<Album> albums = new ArrayList<>();
        albums.add(new Album());
        artist.setAlbums(albums);
        assertEquals(albums, artist.getAlbums());
    }
    
    @Test
    public void testAddSong() {
        Song song = new Song();
        artist.addSong(song);
        assertTrue(artist.getSongs().contains(song));
        assertEquals(1, artist.getSongCount());
    }
    
    @Test
    public void testAddAlbum() {
        Album album = new Album();
        artist.addAlbum(album);
        assertTrue(artist.getAlbums().contains(album));
        assertEquals(1, artist.getAlbumCount());
    }
    
    @Test
    public void testGetSongCount() {
        assertEquals(0, artist.getSongCount());
        
        // Add 3 songs
        artist.addSong(new Song());
        artist.addSong(new Song());
        artist.addSong(new Song());
        
        assertEquals(3, artist.getSongCount());
    }
    
    @Test
    public void testGetAlbumCount() {
        assertEquals(0, artist.getAlbumCount());
        
        // Add 2 albums
        artist.addAlbum(new Album());
        artist.addAlbum(new Album());
        
        assertEquals(2, artist.getAlbumCount());
    }
    
    @Test
    public void testToString() {
        String expected = "Artist{id=" + TEST_ID + 
                ", name='" + TEST_NAME + "'" + 
                ", songCount=" + artist.getSongCount() + 
                ", albumCount=" + artist.getAlbumCount() + "}";
        
        assertEquals(expected, artist.toString());
    }
} 