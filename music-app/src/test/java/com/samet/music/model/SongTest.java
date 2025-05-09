package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

public class SongTest {
    
    private Song song;
    private final int TEST_ID = 1;
    private final String TEST_TITLE = "Test Song";
    private final String TEST_ARTIST = "Test Artist";
    private final String TEST_ALBUM = "Test Album";
    private final String TEST_GENRE = "Rock";
    private final int TEST_YEAR = 2023;
    private final int TEST_DURATION = 240; // 4 minutes
    private final String TEST_FILE_PATH = "/path/to/song.mp3";
    private final int TEST_USER_ID = 1;
    private final Timestamp TEST_DATE = new Timestamp(System.currentTimeMillis());
    
    @Before
    public void setUp() {
        // Create a new song for each test
        song = new Song(TEST_ID, TEST_TITLE, TEST_ARTIST, TEST_ALBUM, TEST_GENRE, TEST_YEAR, 
                        TEST_DURATION, TEST_FILE_PATH, TEST_USER_ID, TEST_DATE);
    }
    
    @Test
    public void testDefaultConstructor() {
        Song defaultSong = new Song();
        assertNotNull(defaultSong);
    }
    
    @Test
    public void testConstructorWithoutId() {
        Song newSong = new Song(TEST_TITLE, TEST_ARTIST, TEST_ALBUM, TEST_GENRE, TEST_YEAR, 
                                TEST_DURATION, TEST_FILE_PATH, TEST_USER_ID);
        assertNotNull(newSong);
        assertEquals(TEST_TITLE, newSong.getTitle());
        assertEquals(TEST_ARTIST, newSong.getArtist());
        assertEquals(TEST_ALBUM, newSong.getAlbum());
        assertEquals(TEST_GENRE, newSong.getGenre());
        assertEquals(TEST_YEAR, newSong.getYear());
        assertEquals(TEST_DURATION, newSong.getDuration());
        assertEquals(TEST_FILE_PATH, newSong.getFilePath());
        assertEquals(TEST_USER_ID, newSong.getUserId());
        assertNotNull(newSong.getCreatedAt());
    }
    
    @Test
    public void testFullConstructor() {
        assertNotNull(song);
        assertEquals(TEST_ID, song.getId());
        assertEquals(TEST_TITLE, song.getTitle());
        assertEquals(TEST_ARTIST, song.getArtist());
        assertEquals(TEST_ALBUM, song.getAlbum());
        assertEquals(TEST_GENRE, song.getGenre());
        assertEquals(TEST_YEAR, song.getYear());
        assertEquals(TEST_DURATION, song.getDuration());
        assertEquals(TEST_FILE_PATH, song.getFilePath());
        assertEquals(TEST_USER_ID, song.getUserId());
        assertEquals(TEST_DATE, song.getCreatedAt());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setId and getId
        song.setId(2);
        assertEquals(2, song.getId());
        
        // Test setTitle and getTitle
        song.setTitle("New Title");
        assertEquals("New Title", song.getTitle());
        
        // Test setArtist and getArtist
        song.setArtist("New Artist");
        assertEquals("New Artist", song.getArtist());
        
        // Test setAlbum and getAlbum
        song.setAlbum("New Album");
        assertEquals("New Album", song.getAlbum());
        
        // Test setGenre and getGenre
        song.setGenre("Pop");
        assertEquals("Pop", song.getGenre());
        
        // Test setYear and getYear
        song.setYear(2024);
        assertEquals(2024, song.getYear());
        
        // Test setDuration and getDuration
        song.setDuration(300);
        assertEquals(300, song.getDuration());
        
        // Test setFilePath and getFilePath
        song.setFilePath("/new/path.mp3");
        assertEquals("/new/path.mp3", song.getFilePath());
        
        // Test setUserId and getUserId
        song.setUserId(2);
        assertEquals(2, song.getUserId());
        
        // Test setCreatedAt and getCreatedAt
        Timestamp newDate = new Timestamp(System.currentTimeMillis() + 86400000); // 1 day later
        song.setCreatedAt(newDate);
        assertEquals(newDate, song.getCreatedAt());
    }
    
    @Test
    public void testGetFormattedDuration() {
        // Test duration formatting for different durations
        
        // 1 minute
        song.setDuration(60);
        assertEquals("01:00", song.getFormattedDuration());
        
        // 1 minute and 30 seconds
        song.setDuration(90);
        assertEquals("01:30", song.getFormattedDuration());
        
        // 10 minutes
        song.setDuration(600);
        assertEquals("10:00", song.getFormattedDuration());
        
        // 1 hour (TimeFormatter likely won't format hours, but testing anyway)
        song.setDuration(3600);
        String result = song.getFormattedDuration();
        assertTrue(result.equals("60:00") || result.equals("01:00:00"));
    }
    
    @Test
    public void testToString() {
        String expected = "Song{" +
                "id=" + TEST_ID +
                ", title='" + TEST_TITLE + '\'' +
                ", artist='" + TEST_ARTIST + '\'' +
                ", album='" + TEST_ALBUM + '\'' +
                ", genre='" + TEST_GENRE + '\'' +
                ", year=" + TEST_YEAR +
                ", duration=" + song.getFormattedDuration() +
                ", filePath='" + TEST_FILE_PATH + '\'' +
                ", createdAt=" + TEST_DATE +
                '}';
        
        assertEquals(expected, song.toString());
    }
} 