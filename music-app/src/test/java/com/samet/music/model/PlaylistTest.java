package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlaylistTest {
    
    private Playlist playlist;
    private final int TEST_ID = 1;
    private final String TEST_NAME = "Test Playlist";
    private final String TEST_DESCRIPTION = "Test Description";
    private final int TEST_USER_ID = 1;
    private final LocalDateTime TEST_DATE = LocalDateTime.now();
    
    @Before
    public void setUp() {
        // Create a new playlist for each test
        playlist = new Playlist(TEST_ID, TEST_NAME, TEST_DESCRIPTION, TEST_USER_ID, TEST_DATE);
    }
    
    @Test
    public void testDefaultConstructor() {
        Playlist defaultPlaylist = new Playlist();
        assertNotNull(defaultPlaylist);
        assertNotNull(defaultPlaylist.getSongs());
        assertEquals(0, defaultPlaylist.getSongs().size());
    }
    
    @Test
    public void testConstructorWithoutId() {
        Playlist newPlaylist = new Playlist(TEST_NAME, TEST_DESCRIPTION, TEST_USER_ID);
        assertNotNull(newPlaylist);
        assertEquals(TEST_NAME, newPlaylist.getName());
        assertEquals(TEST_DESCRIPTION, newPlaylist.getDescription());
        assertEquals(TEST_USER_ID, newPlaylist.getUserId());
        assertNotNull(newPlaylist.getCreatedAt());
        assertNotNull(newPlaylist.getSongs());
        assertEquals(0, newPlaylist.getSongs().size());
    }
    
    @Test
    public void testFullConstructor() {
        assertNotNull(playlist);
        assertEquals(TEST_ID, playlist.getId());
        assertEquals(TEST_NAME, playlist.getName());
        assertEquals(TEST_DESCRIPTION, playlist.getDescription());
        assertEquals(TEST_USER_ID, playlist.getUserId());
        assertEquals(TEST_DATE, playlist.getCreatedAt());
        assertNotNull(playlist.getSongs());
        assertEquals(0, playlist.getSongs().size());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setId and getId
        playlist.setId(2);
        assertEquals(2, playlist.getId());
        
        // Test setName and getName
        playlist.setName("New Name");
        assertEquals("New Name", playlist.getName());
        
        // Test setDescription and getDescription
        playlist.setDescription("New Description");
        assertEquals("New Description", playlist.getDescription());
        
        // Test setUserId and getUserId
        playlist.setUserId(2);
        assertEquals(2, playlist.getUserId());
        
        // Test setCreatedAt and getCreatedAt
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        playlist.setCreatedAt(newDate);
        assertEquals(newDate, playlist.getCreatedAt());
        
        // Test setSongs and getSongs
        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        songs.add(song);
        playlist.setSongs(songs);
        assertEquals(songs, playlist.getSongs());
    }
    
    @Test
    public void testAddSong() {
        Song song = new Song();
        playlist.addSong(song);
        assertTrue(playlist.getSongs().contains(song));
        assertEquals(1, playlist.getSongCount());
    }
    
    @Test
    public void testRemoveSong() {
        Song song = new Song();
        playlist.addSong(song);
        assertEquals(1, playlist.getSongCount());
        
        boolean result = playlist.removeSong(song);
        assertTrue(result);
        assertEquals(0, playlist.getSongCount());
        assertFalse(playlist.getSongs().contains(song));
    }
    
    @Test
    public void testRemoveSongUnsuccessful() {
        Song song1 = new Song();
        Song song2 = new Song();
        playlist.addSong(song1);
        
        // Try to remove a song that's not in the playlist
        boolean result = playlist.removeSong(song2);
        assertFalse(result);
        assertEquals(1, playlist.getSongCount());
    }
    
    @Test
    public void testRemoveSongById() {
        Song song1 = new Song();
        song1.setId(1);
        Song song2 = new Song();
        song2.setId(2);
        
        playlist.addSong(song1);
        playlist.addSong(song2);
        assertEquals(2, playlist.getSongCount());
        
        boolean result = playlist.removeSongById(1);
        assertTrue(result);
        assertEquals(1, playlist.getSongCount());
        assertFalse(playlist.getSongs().contains(song1));
        assertTrue(playlist.getSongs().contains(song2));
        
        // Try to remove a song that doesn't exist
        result = playlist.removeSongById(3);
        assertFalse(result);
        assertEquals(1, playlist.getSongCount());
    }
    
    @Test
    public void testGetSongCount() {
        assertEquals(0, playlist.getSongCount());
        
        // Add songs
        playlist.addSong(new Song());
        playlist.addSong(new Song());
        playlist.addSong(new Song());
        
        assertEquals(3, playlist.getSongCount());
    }
    
    @Test
    public void testGetTotalDuration() {
        assertEquals(0, playlist.getTotalDuration());
        
        // Add songs with durations
        Song song1 = new Song();
        song1.setDuration(180); // 3 minutes
        Song song2 = new Song();
        song2.setDuration(240); // 4 minutes
        
        playlist.addSong(song1);
        playlist.addSong(song2);
        
        assertEquals(420, playlist.getTotalDuration()); // 7 minutes total
    }
    
    @Test
    public void testGetFormattedTotalDurationMinutesAndSeconds() {
        // Add songs with durations less than an hour
        Song song1 = new Song();
        song1.setDuration(180); // 3 minutes
        Song song2 = new Song();
        song2.setDuration(45); // 45 seconds
        
        playlist.addSong(song1);
        playlist.addSong(song2);
        
        assertEquals("03:45", playlist.getFormattedTotalDuration());
    }
    
    @Test
    public void testGetFormattedTotalDurationWithHours() {
        // Add songs with durations more than an hour
        Song song1 = new Song();
        song1.setDuration(3600); // 1 hour
        Song song2 = new Song();
        song2.setDuration(120); // 2 minutes
        Song song3 = new Song();
        song3.setDuration(45); // 45 seconds
        
        playlist.addSong(song1);
        playlist.addSong(song2);
        playlist.addSong(song3);
        
        assertEquals("1:02:45", playlist.getFormattedTotalDuration());
    }
    
    @Test
    public void testToString() {
        String expected = "Playlist{" +
                "id=" + TEST_ID +
                ", name='" + TEST_NAME + '\'' +
                ", description='" + TEST_DESCRIPTION + '\'' +
                ", songCount=" + playlist.getSongCount() +
                ", totalDuration=" + playlist.getFormattedTotalDuration() +
                '}';
        
        assertEquals(expected, playlist.toString());
    }
} 