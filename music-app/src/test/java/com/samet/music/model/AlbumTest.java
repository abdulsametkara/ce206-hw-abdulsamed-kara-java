package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlbumTest {
    
    private Album album;
    private final int TEST_ID = 1;
    private final String TEST_TITLE = "Test Album";
    private final String TEST_ARTIST = "Test Artist";
    private final int TEST_YEAR = 2023;
    private final String TEST_GENRE = "Rock";
    private final int TEST_USER_ID = 1;
    private final LocalDateTime TEST_DATE = LocalDateTime.now();
    
    @Before
    public void setUp() {
        // Create a new album for each test
        album = new Album(TEST_ID, TEST_TITLE, TEST_ARTIST, TEST_YEAR, TEST_GENRE, TEST_USER_ID, TEST_DATE);
    }
    
    @Test
    public void testDefaultConstructor() {
        Album defaultAlbum = new Album();
        assertNotNull(defaultAlbum);
        assertNotNull(defaultAlbum.getSongs());
        assertEquals(0, defaultAlbum.getSongs().size());
    }
    
    @Test
    public void testConstructorWithoutId() {
        Album newAlbum = new Album(TEST_TITLE, TEST_ARTIST, TEST_YEAR, TEST_GENRE, TEST_USER_ID);
        assertNotNull(newAlbum);
        assertEquals(TEST_TITLE, newAlbum.getTitle());
        assertEquals(TEST_ARTIST, newAlbum.getArtist());
        assertEquals(TEST_YEAR, newAlbum.getYear());
        assertEquals(TEST_GENRE, newAlbum.getGenre());
        assertEquals(TEST_USER_ID, newAlbum.getUserId());
        assertNotNull(newAlbum.getSongs());
        assertEquals(0, newAlbum.getSongs().size());
    }
    
    @Test
    public void testFullConstructor() {
        assertNotNull(album);
        assertEquals(TEST_ID, album.getId());
        assertEquals(TEST_TITLE, album.getTitle());
        assertEquals(TEST_ARTIST, album.getArtist());
        assertEquals(TEST_YEAR, album.getYear());
        assertEquals(TEST_GENRE, album.getGenre());
        assertEquals(TEST_USER_ID, album.getUserId());
        assertEquals(TEST_DATE, album.getCreatedAt());
        assertNotNull(album.getSongs());
        assertEquals(0, album.getSongs().size());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setId and getId
        album.setId(2);
        assertEquals(2, album.getId());
        
        // Test setTitle and getTitle
        album.setTitle("New Title");
        assertEquals("New Title", album.getTitle());
        
        // Test setArtist and getArtist
        album.setArtist("New Artist");
        assertEquals("New Artist", album.getArtist());
        
        // Test setYear and getYear
        album.setYear(2024);
        assertEquals(2024, album.getYear());
        
        // Test setGenre and getGenre
        album.setGenre("Pop");
        assertEquals("Pop", album.getGenre());
        
        // Test setUserId and getUserId
        album.setUserId(2);
        assertEquals(2, album.getUserId());
        
        // Test setCreatedAt and getCreatedAt
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        album.setCreatedAt(newDate);
        assertEquals(newDate, album.getCreatedAt());
        
        // Test setSongs and getSongs
        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        songs.add(song);
        album.setSongs(songs);
        assertEquals(songs, album.getSongs());
    }
    
    @Test
    public void testAddSong() {
        Song song = new Song();
        album.addSong(song);
        assertTrue(album.getSongs().contains(song));
        assertEquals(1, album.getSongCount());
    }
    
    @Test
    public void testAddSongWithNullSongsList() {
        // Setup scenario where songs list is null
        album.setSongs(null);
        
        // Test behavior
        Song song = new Song();
        album.addSong(song);
        
        // Verify that a list was created and song was added
        assertNotNull(album.getSongs());
        assertTrue(album.getSongs().contains(song));
        assertEquals(1, album.getSongCount());
    }
    
    @Test
    public void testRemoveSong() {
        Song song = new Song();
        album.addSong(song);
        assertEquals(1, album.getSongCount());
        
        album.removeSong(song);
        assertEquals(0, album.getSongCount());
        assertFalse(album.getSongs().contains(song));
    }
    
    @Test
    public void testRemoveSongWithNullSongsList() {
        album.setSongs(null);
        
        // This should not throw an exception
        Song song = new Song();
        album.removeSong(song);
        
        // List should be created if it was null
        assertNotNull(album.getSongs());
    }
    
    @Test
    public void testRemoveSongById() {
        Song song1 = new Song();
        song1.setId(1);
        Song song2 = new Song();
        song2.setId(2);
        
        album.addSong(song1);
        album.addSong(song2);
        assertEquals(2, album.getSongCount());
        
        boolean result = album.removeSongById(1);
        assertTrue(result);
        assertEquals(1, album.getSongCount());
        assertFalse(album.getSongs().contains(song1));
        assertTrue(album.getSongs().contains(song2));
        
        // Try to remove a song that doesn't exist
        result = album.removeSongById(3);
        assertFalse(result);
        assertEquals(1, album.getSongCount());
    }
    
    @Test
    public void testGetSongCount() {
        assertEquals(0, album.getSongCount());
        
        // Add songs
        album.addSong(new Song());
        album.addSong(new Song());
        album.addSong(new Song());
        
        assertEquals(3, album.getSongCount());
    }
    
    @Test
    public void testGetTotalDuration() {
        assertEquals(0, album.getTotalDuration());
        
        // Add songs with durations
        Song song1 = new Song();
        song1.setDuration(180); // 3 minutes
        Song song2 = new Song();
        song2.setDuration(240); // 4 minutes
        
        album.addSong(song1);
        album.addSong(song2);
        
        assertEquals(420, album.getTotalDuration()); // 7 minutes total
    }
    
    @Test
    public void testGetFormattedTotalDuration() {
        // Add songs with durations to total more than an hour
        Song song1 = new Song();
        song1.setDuration(3600); // 1 hour
        Song song2 = new Song();
        song2.setDuration(120); // 2 minutes
        Song song3 = new Song();
        song3.setDuration(45); // 45 seconds
        
        album.addSong(song1);
        album.addSong(song2);
        album.addSong(song3);
        
        assertEquals("01:02:45", album.getFormattedTotalDuration());
    }
    
    @Test
    public void testToString() {
        String expected = "Album{" +
                "id=" + TEST_ID +
                ", title='" + TEST_TITLE + '\'' +
                ", artist='" + TEST_ARTIST + '\'' +
                ", year=" + TEST_YEAR +
                ", genre='" + TEST_GENRE + '\'' +
                ", userId=" + TEST_USER_ID +
                ", createdAt=" + TEST_DATE +
                ", songsCount=" + album.getSongs().size() +
                '}';
        
        assertEquals(expected, album.toString());
    }
    
    @Test
    public void testToStringWithNullSongs() {
        // Setup a scenario where songs is null
        album.setSongs(null);
        
        String expected = "Album{" +
                "id=" + TEST_ID +
                ", title='" + TEST_TITLE + '\'' +
                ", artist='" + TEST_ARTIST + '\'' +
                ", year=" + TEST_YEAR +
                ", genre='" + TEST_GENRE + '\'' +
                ", userId=" + TEST_USER_ID +
                ", createdAt=" + TEST_DATE +
                ", songsCount=0" +
                '}';
        
        assertEquals(expected, album.toString());
    }
} 