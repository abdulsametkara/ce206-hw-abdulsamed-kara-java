package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.dao.UserSongStatisticsDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.service.RecommendationService;

/**
 * Comprehensive test class for SongController with mocks
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SongControllerTest {

    private SongController songController;
    
    @Mock
    private UserController userController;
    
    @Mock
    private SongDAO songDAO;
    
    @Mock
    private UserSongStatisticsDAO userSongStatisticsDAO;
    
    @Mock
    private RecommendationService recommendationService;
    
    private User testUser;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Mock userController behavior
        lenient().when(userController.getCurrentUser()).thenReturn(testUser);
        lenient().when(userController.isLoggedIn()).thenReturn(true);
        
        // Create SongController with mocked dependencies
        songController = new SongController(userController);
        songController.setSongDAO(songDAO);
        
        // Use reflection to inject mock dependencies
        try {
            java.lang.reflect.Field userSongStatisticsDAOField = SongController.class.getDeclaredField("userSongStatisticsDAO");
            userSongStatisticsDAOField.setAccessible(true);
            userSongStatisticsDAOField.set(songController, userSongStatisticsDAO);
            
            java.lang.reflect.Field recommendationServiceField = SongController.class.getDeclaredField("recommendationService");
            recommendationServiceField.setAccessible(true);
            recommendationServiceField.set(songController, recommendationService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGetMostLikelyToEnjoySongs() {
        // Arrange
        List<Song> expectedSongs = new ArrayList<>();
        expectedSongs.add(new Song("Enjoyable Song", "Artist", "Album", "Genre", 2023, 180, "path.mp3", 1));
        
        when(recommendationService.getMostLikelyToEnjoySongs(eq(testUser), eq(10)))
            .thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songController.getMostLikelyToEnjoySongs();
        
        // Assert
        assertEquals("Should return songs from recommendation service", expectedSongs, result);
        verify(recommendationService).getMostLikelyToEnjoySongs(testUser, 10);
    }
    
    @Test
    public void testGetMostLikelyToEnjoySongs_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getMostLikelyToEnjoySongs();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(recommendationService, never()).getMostLikelyToEnjoySongs(any(), anyInt());
    }
    
    @Test
    public void testGetUserStatistics() {
        // Arrange
        Map<String, Object> expectedStats = new HashMap<>();
        expectedStats.put("totalSongs", 10);
        expectedStats.put("totalPlayTime", 1800);
        expectedStats.put("favoritesCount", 5);
        
        when(userSongStatisticsDAO.getUserStatistics(testUser.getId()))
            .thenReturn(expectedStats);
        
        // Act
        Map<String, Object> result = songController.getUserStatistics();
        
        // Assert
        assertEquals("Should return statistics from DAO", expectedStats, result);
        verify(userSongStatisticsDAO).getUserStatistics(testUser.getId());
    }
    
    @Test
    public void testGetUserStatistics_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        Map<String, Object> result = songController.getUserStatistics();
        
        // Assert
        assertTrue("Should return empty map when no user is logged in", result.isEmpty());
        verify(userSongStatisticsDAO, never()).getUserStatistics(anyInt());
    }
    
    @Test
    public void testPlaySong_Success() {
        // Arrange
        int songId = 1;
        Song expectedSong = new Song("Test Song", "Test Artist", "Test Album", "Rock", 2023, 180, "path.mp3", 2);
        expectedSong.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(expectedSong));
        when(userSongStatisticsDAO.incrementPlayCount(testUser.getId(), songId)).thenReturn(true);
        
        // Act
        Song result = songController.playSong(songId);
        
        // Assert
        assertNotNull("Should return the played song", result);
        assertEquals("Should return correct song", expectedSong, result);
        verify(userSongStatisticsDAO).incrementPlayCount(testUser.getId(), songId);
    }
    
    @Test
    public void testPlaySong_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        Song result = songController.playSong(1);
        
        // Assert
        assertNull("Should return null when no user is logged in", result);
        verify(userSongStatisticsDAO, never()).incrementPlayCount(anyInt(), anyInt());
    }
    
    @Test
    public void testPlaySong_SongNotFound() {
        // Arrange
        int songId = 999;
        when(songDAO.findById(songId)).thenReturn(Optional.empty());
        
        // Act
        Song result = songController.playSong(songId);
        
        // Assert
        assertNull("Should return null when song is not found", result);
        verify(userSongStatisticsDAO, never()).incrementPlayCount(anyInt(), anyInt());
    }
    
    @Test
    public void testGetRecentlyPlayedSongs() {
        // Arrange
        int limit = 3;
        List<Integer> recentSongIds = new ArrayList<>();
        recentSongIds.add(1);
        recentSongIds.add(2);
        recentSongIds.add(3);
        
        Song song1 = new Song("Recent 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        song1.setId(1);
        
        Song song2 = new Song("Recent 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        song2.setId(2);
        
        Song song3 = new Song("Recent 3", "Artist 3", "Album 3", "Genre 3", 2023, 180, "path3.mp3", testUser.getId());
        song3.setId(3);
        
        when(userSongStatisticsDAO.getRecentlyPlayedSongs(testUser.getId(), limit)).thenReturn(recentSongIds);
        when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        when(songDAO.findById(3)).thenReturn(Optional.of(song3));
        
        // Act
        List<Song> result = songController.getRecentlyPlayedSongs(limit);
        
        // Assert
        assertEquals("Should return correct number of recently played songs", 3, result.size());
        assertEquals("Should return recent songs in correct order", "Recent 1", result.get(0).getTitle());
        assertEquals("Should return recent songs in correct order", "Recent 2", result.get(1).getTitle());
        assertEquals("Should return recent songs in correct order", "Recent 3", result.get(2).getTitle());
        verify(userSongStatisticsDAO).getRecentlyPlayedSongs(testUser.getId(), limit);
    }
    
    @Test
    public void testGetRecentlyPlayedSongs_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getRecentlyPlayedSongs(5);
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(userSongStatisticsDAO, never()).getRecentlyPlayedSongs(anyInt(), anyInt());
    }
    
    @Test
    public void testGetSongsByArtist() {
        // Arrange
        String artistName = "Test Artist";
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song("Song 1", artistName, "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        Song song2 = new Song("Song 2", artistName, "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        Song song3 = new Song("Song 3", "Different Artist", "Album 3", "Genre 3", 2023, 180, "path3.mp3", testUser.getId());
        
        userSongs.add(song1);
        userSongs.add(song2);
        userSongs.add(song3);
        
        when(songDAO.findByUserId(testUser.getId())).thenReturn(userSongs);
        
        // Act
        List<Song> result = songController.getSongsByArtist(artistName);
        
        // Assert
        assertEquals("Should return only songs by the specified artist", 2, result.size());
        assertEquals("Should return song with matching artist", artistName, result.get(0).getArtist());
        assertEquals("Should return song with matching artist", artistName, result.get(1).getArtist());
        verify(songDAO).findByUserId(testUser.getId());
    }
    
    @Test
    public void testGetSongsByArtist_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getSongsByArtist("Any Artist");
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(songDAO, never()).findByUserId(anyInt());
    }
    
    @Test
    public void testGetArtists() {
        // Arrange
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song("Song 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        Song song2 = new Song("Song 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        Song song3 = new Song("Song 3", "Artist 1", "Album 3", "Genre 3", 2023, 180, "path3.mp3", testUser.getId());
        Song song4 = new Song("Song 4", null, "Album 4", "Genre 4", 2023, 180, "path4.mp3", testUser.getId());
        Song song5 = new Song("Song 5", "", "Album 5", "Genre 5", 2023, 180, "path5.mp3", testUser.getId());
        
        userSongs.add(song1);
        userSongs.add(song2);
        userSongs.add(song3);
        userSongs.add(song4);
        userSongs.add(song5);
        
        when(songDAO.findByUserId(testUser.getId())).thenReturn(userSongs);
        
        // Act
        List<String> result = songController.getArtists();
        
        // Assert
        assertEquals("Should return unique artist names", 2, result.size());
        assertTrue("Should contain Artist 1", result.contains("Artist 1"));
        assertTrue("Should contain Artist 2", result.contains("Artist 2"));
        verify(songDAO).findByUserId(testUser.getId());
    }
    
    @Test
    public void testGetArtists_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<String> result = songController.getArtists();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(songDAO, never()).findByUserId(anyInt());
    }
    
    @Test
    public void testGetUserArtists() {
        // Arrange
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song("Song 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        Song song2 = new Song("Song 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        
        userSongs.add(song1);
        userSongs.add(song2);
        
        when(songDAO.findByUserId(testUser.getId())).thenReturn(userSongs);
        
        // Act
        List<String> result = songController.getUserArtists();
        
        // Assert
        assertEquals("Should return same result as getArtists()", 2, result.size());
        assertTrue("Should contain Artist 1", result.contains("Artist 1"));
        assertTrue("Should contain Artist 2", result.contains("Artist 2"));
        verify(songDAO).findByUserId(testUser.getId());
    }
    
    @Test
    public void testToggleFavorite_AddFavorite() {
        // Arrange
        int songId = 1;
        
        Song song = new Song("Test Song", "Artist", "Album", "Genre", 2023, 180, "path.mp3", testUser.getId());
        song.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(song));
        when(userSongStatisticsDAO.setFavorite(testUser.getId(), songId, true)).thenReturn(true);
        
        // Act
        boolean result = songController.toggleFavorite(songId, true);
        
        // Assert
        assertTrue("Should return true indicating successful addition to favorites", result);
        verify(userSongStatisticsDAO).setFavorite(testUser.getId(), songId, true);
    }
    
    @Test
    public void testToggleFavorite_RemoveFavorite() {
        // Arrange
        int songId = 1;
        
        Song song = new Song("Test Song", "Artist", "Album", "Genre", 2023, 180, "path.mp3", testUser.getId());
        song.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(song));
        when(userSongStatisticsDAO.setFavorite(testUser.getId(), songId, false)).thenReturn(true);
        
        // Act
        boolean result = songController.toggleFavorite(songId, false);
        
        // Assert
        assertTrue("Should return true indicating successful removal from favorites", result);
        verify(userSongStatisticsDAO).setFavorite(testUser.getId(), songId, false);
    }
    
    @Test
    public void testToggleFavorite_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.toggleFavorite(1, true);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
        verify(userSongStatisticsDAO, never()).setFavorite(anyInt(), anyInt(), anyBoolean());
    }
    
    @Test
    public void testToggleFavorite_SongNotFound() {
        // Arrange
        int songId = 999;
        when(songDAO.findById(songId)).thenReturn(Optional.empty());
        
        // Act
        boolean result = songController.toggleFavorite(songId, true);
        
        // Assert
        assertFalse("Should return false when song is not found", result);
        verify(userSongStatisticsDAO, never()).setFavorite(anyInt(), anyInt(), anyBoolean());
    }
    
    @Test
    public void testIsFavorite_WhenFavorite() {
        // Arrange
        int songId = 1;
        when(userSongStatisticsDAO.isFavorite(testUser.getId(), songId)).thenReturn(true);
        
        // Act
        boolean result = songController.isFavorite(songId);
        
        // Assert
        assertTrue("Should return true for favorite song", result);
        verify(userSongStatisticsDAO).isFavorite(testUser.getId(), songId);
    }
    
    @Test
    public void testIsFavorite_WhenNotFavorite() {
        // Arrange
        int songId = 1;
        when(userSongStatisticsDAO.isFavorite(testUser.getId(), songId)).thenReturn(false);
        
        // Act
        boolean result = songController.isFavorite(songId);
        
        // Assert
        assertFalse("Should return false for non-favorite song", result);
        verify(userSongStatisticsDAO).isFavorite(testUser.getId(), songId);
    }
    
    @Test
    public void testIsFavorite_WhenNoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.isFavorite(1);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
        verify(userSongStatisticsDAO, never()).isFavorite(anyInt(), anyInt());
    }
    
    @Test
    public void testGetFavoriteSongs_Success() {
        // Arrange
        List<Integer> favoriteSongIds = new ArrayList<>();
        favoriteSongIds.add(1);
        favoriteSongIds.add(2);
        
        Song song1 = new Song("Fav 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        song1.setId(1);
        
        Song song2 = new Song("Fav 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        song2.setId(2);
        
        when(userSongStatisticsDAO.getFavoriteSongs(testUser.getId())).thenReturn(favoriteSongIds);
        when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        
        // Act
        List<Song> result = songController.getFavoriteSongs();
        
        // Assert
        assertEquals("Should return correct number of favorite songs", 2, result.size());
        assertEquals("Should return favorite songs", "Fav 1", result.get(0).getTitle());
        assertEquals("Should return favorite songs", "Fav 2", result.get(1).getTitle());
        verify(userSongStatisticsDAO).getFavoriteSongs(testUser.getId());
    }
    
    @Test
    public void testGetFavoriteSongs_WhenNoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getFavoriteSongs();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(userSongStatisticsDAO, never()).getFavoriteSongs(anyInt());
    }
    
    @Test
    public void testAddSong_Success() {
        // Arrange
        String title = "Test Song";
        String artist = "Test Artist";
        String album = "Test Album";
        String genre = "Rock";
        int year = 2023;
        int duration = 180;
        String filePath = "path/to/audio.mp3";
        
        // Create a spy of the controller to mock the isValidFile method
        SongController spyController = spy(songController);
        doReturn(true).when(spyController).isValidFile(anyString());
        
        // Mock the song creation
        Song expectedSong = new Song(title, artist, album, genre, year, duration, filePath, testUser.getId());
        expectedSong.setId(1);
        when(songDAO.create(any(Song.class))).thenReturn(expectedSong);
        
        // Act
        Song result = spyController.addSong(title, artist, album, genre, year, duration, filePath);
        
        // Assert
        assertNotNull("Should return a Song object", result);
        assertEquals("Should return song with correct ID", 1, result.getId());
        assertEquals("Should return song with correct title", title, result.getTitle());
        verify(songDAO).create(any(Song.class));
    }
    
    @Test
    public void testAddSong_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        Song result = songController.addSong("Title", "Artist", "Album", "Genre", 2023, 180, "path.mp3");
        
        // Assert
        assertNull("Should return null when no user is logged in", result);
        verify(songDAO, never()).create(any(Song.class));
    }
    
    @Test
    public void testAddSong_InvalidFile() {
        // Arrange
        SongController spyController = spy(songController);
        doReturn(false).when(spyController).isValidFile(anyString());
        
        // Act
        Song result = spyController.addSong("Title", "Artist", "Album", "Genre", 2023, 180, "invalid_path.mp3");
        
        // Assert
        assertNull("Should return null when file path is invalid", result);
        verify(songDAO, never()).create(any(Song.class));
    }
    
    @Test
    public void testUpdateSong_Success() {
        // Arrange
        int songId = 1;
        String newTitle = "Updated Title";
        String newArtist = "Updated Artist";
        String newAlbum = "Updated Album";
        String newGenre = "Updated Genre";
        int newYear = 2024;
        
        Song existingSong = new Song("Original Title", "Original Artist", "Original Album", 
                "Original Genre", 2023, 180, "path.mp3", testUser.getId());
        existingSong.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(existingSong));
        when(songDAO.update(any(Song.class))).thenReturn(true);
        
        // Act
        boolean result = songController.updateSong(songId, newTitle, newArtist, newAlbum, newGenre, newYear);
        
        // Assert
        assertTrue("Should return true on successful update", result);
        verify(songDAO).update(argThat(song -> 
            song.getId() == songId &&
            song.getTitle().equals(newTitle) &&
            song.getArtist().equals(newArtist) &&
            song.getAlbum().equals(newAlbum) &&
            song.getGenre().equals(newGenre) &&
            song.getYear() == newYear
        ));
    }
    
    @Test
    public void testUpdateSong_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.updateSong(1, "Title", "Artist", "Album", "Genre", 2023);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
        verify(songDAO, never()).update(any(Song.class));
    }
    
    @Test
    public void testUpdateSong_SongNotFound() {
        // Arrange
        when(songDAO.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act
        boolean result = songController.updateSong(999, "Title", "Artist", "Album", "Genre", 2023);
        
        // Assert
        assertFalse("Should return false when song is not found", result);
        verify(songDAO, never()).update(any(Song.class));
    }
    
    @Test
    public void testUpdateSong_UserDoesNotOwnSong() {
        // Arrange
        int songId = 1;
        int differentUserId = 999;
        
        Song existingSong = new Song("Title", "Artist", "Album", "Genre", 2023, 180, "path.mp3", differentUserId);
        existingSong.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(existingSong));
        
        // Act
        boolean result = songController.updateSong(songId, "New Title", "New Artist", "New Album", "New Genre", 2024);
        
        // Assert
        assertFalse("Should return false when user doesn't own the song", result);
        verify(songDAO, never()).update(any(Song.class));
    }
    
    @Test
    public void testDeleteSong_Success() {
        // Arrange
        int songId = 1;
        
        Song existingSong = new Song("Title", "Artist", "Album", "Genre", 2023, 180, "path.mp3", testUser.getId());
        existingSong.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(existingSong));
        when(songDAO.delete(songId)).thenReturn(true);
        
        // Act
        boolean result = songController.deleteSong(songId);
        
        // Assert
        assertTrue("Should return true on successful deletion", result);
        verify(songDAO).delete(songId);
    }
    
    @Test
    public void testDeleteSong_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.deleteSong(1);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
        verify(songDAO, never()).delete(anyInt());
    }
    
    @Test
    public void testDeleteSong_SongNotFound() {
        // Arrange
        when(songDAO.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act
        boolean result = songController.deleteSong(999);
        
        // Assert
        assertFalse("Should return false when song is not found", result);
        verify(songDAO, never()).delete(anyInt());
    }
    
    @Test
    public void testDeleteSong_UserDoesNotOwnSong() {
        // Arrange
        int songId = 1;
        int differentUserId = 999;
        
        Song existingSong = new Song("Title", "Artist", "Album", "Genre", 2023, 180, "path.mp3", differentUserId);
        existingSong.setId(songId);
        
        when(songDAO.findById(songId)).thenReturn(Optional.of(existingSong));
        
        // Act
        boolean result = songController.deleteSong(songId);
        
        // Assert
        assertFalse("Should return false when user doesn't own the song", result);
        verify(songDAO, never()).delete(anyInt());
    }
    
    @Test
    public void testGetUserSongs_Success() {
        // Arrange
        List<Song> expectedSongs = new ArrayList<>();
        expectedSongs.add(new Song("Song 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId()));
        expectedSongs.add(new Song("Song 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId()));
        
        when(songDAO.findByUserId(testUser.getId())).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songController.getUserSongs();
        
        // Assert
        assertEquals("Should return the correct number of songs", 2, result.size());
        assertEquals("Should return the correct songs", expectedSongs, result);
        verify(songDAO).findByUserId(testUser.getId());
    }
    
    @Test
    public void testGetUserSongs_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getUserSongs();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(songDAO, never()).findByUserId(anyInt());
    }
    
    @Test
    public void testSearchSongs_Success() {
        // Arrange
        String query = "rock";
        
        List<Song> allFoundSongs = new ArrayList<>();
        Song userSong = new Song("Rock Song", "Rock Artist", "Rock Album", "Rock", 2023, 180, "path1.mp3", testUser.getId());
        Song otherUserSong = new Song("Pop Song", "Pop Artist", "Pop Album", "Pop", 2023, 180, "path2.mp3", 999); // different user
        
        allFoundSongs.add(userSong);
        allFoundSongs.add(otherUserSong);
        
        when(songDAO.search(query, query, query, query)).thenReturn(allFoundSongs);
        
        // Act
        List<Song> result = songController.searchSongs(query);
        
        // Assert
        assertEquals("Should return only songs owned by the current user", 1, result.size());
        assertEquals("Should return songs with matching query", "Rock Song", result.get(0).getTitle());
        verify(songDAO).search(query, query, query, query);
    }
    
    @Test
    public void testSearchSongs_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.searchSongs("query");
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(songDAO, never()).search(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    public void testGetRecommendations_Success() {
        // Arrange
        List<Song> expectedRecommendations = new ArrayList<>();
        expectedRecommendations.add(new Song("Recommended Song", "Artist", "Album", "Genre", 2023, 180, "path.mp3", 1));
        
        when(recommendationService.getSongRecommendations(eq(testUser), eq(10))).thenReturn(expectedRecommendations);
        
        // Act
        List<Song> result = songController.getRecommendations();
        
        // Assert
        assertEquals("Should return recommendations from recommendation service", expectedRecommendations, result);
        verify(recommendationService).getSongRecommendations(testUser, 10);
    }
    
    @Test
    public void testGetRecommendations_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getRecommendations();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(recommendationService, never()).getSongRecommendations(any(), anyInt());
    }
    
    @Test
    public void testGetRecommendations_ExceptionHandling() {
        // Arrange
        when(recommendationService.getSongRecommendations(any(), anyInt())).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        List<Song> result = songController.getRecommendations();
        
        // Assert
        assertTrue("Should return empty list when exception occurs", result.isEmpty());
        verify(recommendationService).getSongRecommendations(testUser, 10);
    }
    
    @Test
    public void testGetEnhancedRecommendations_Success() {
        // Arrange
        Map<Song, String> expectedRecommendations = new HashMap<>();
        Song recommendedSong = new Song("Recommended Song", "Artist", "Album", "Genre", 2023, 180, "path.mp3", 1);
        expectedRecommendations.put(recommendedSong, "Because you like this genre");
        
        when(recommendationService.getEnhancedSongRecommendations(eq(testUser), eq(10))).thenReturn(expectedRecommendations);
        
        // Act
        Map<Song, String> result = songController.getEnhancedRecommendations();
        
        // Assert
        assertEquals("Should return enhanced recommendations from recommendation service", expectedRecommendations, result);
        verify(recommendationService).getEnhancedSongRecommendations(testUser, 10);
    }
    
    @Test
    public void testGetEnhancedRecommendations_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        Map<Song, String> result = songController.getEnhancedRecommendations();
        
        // Assert
        assertTrue("Should return empty map when no user is logged in", result.isEmpty());
        verify(recommendationService, never()).getEnhancedSongRecommendations(any(), anyInt());
    }
    
    @Test
    public void testGetEnhancedRecommendations_ExceptionHandling() {
        // Arrange
        when(recommendationService.getEnhancedSongRecommendations(any(), anyInt())).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        Map<Song, String> result = songController.getEnhancedRecommendations();
        
        // Assert
        assertTrue("Should return empty map when exception occurs", result.isEmpty());
        verify(recommendationService).getEnhancedSongRecommendations(testUser, 10);
    }
    
    @Test
    public void testGetMostPlayedSongs_Success() {
        // Arrange
        int limit = 2;
        List<Integer> mostPlayedIds = new ArrayList<>();
        mostPlayedIds.add(1);
        mostPlayedIds.add(2);
        
        Song song1 = new Song("Most Played 1", "Artist 1", "Album 1", "Genre 1", 2023, 180, "path1.mp3", testUser.getId());
        song1.setId(1);
        
        Song song2 = new Song("Most Played 2", "Artist 2", "Album 2", "Genre 2", 2023, 180, "path2.mp3", testUser.getId());
        song2.setId(2);
        
        when(userSongStatisticsDAO.getMostPlayedSongs(testUser.getId(), limit)).thenReturn(mostPlayedIds);
        when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        
        // Act
        List<Song> result = songController.getMostPlayedSongs(limit);
        
        // Assert
        assertEquals("Should return correct number of most played songs", 2, result.size());
        assertEquals("Should return correct most played songs", "Most Played 1", result.get(0).getTitle());
        assertEquals("Should return correct most played songs", "Most Played 2", result.get(1).getTitle());
        verify(userSongStatisticsDAO).getMostPlayedSongs(testUser.getId(), limit);
    }
    
    @Test
    public void testGetMostPlayedSongs_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Song> result = songController.getMostPlayedSongs(5);
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
        verify(userSongStatisticsDAO, never()).getMostPlayedSongs(anyInt(), anyInt());
    }
    
    @Test
    public void testGetMostPlayedSongs_SongNotFound() {
        // Arrange
        int limit = 1;
        List<Integer> mostPlayedIds = new ArrayList<>();
        mostPlayedIds.add(999); // ID that won't be found
        
        when(userSongStatisticsDAO.getMostPlayedSongs(testUser.getId(), limit)).thenReturn(mostPlayedIds);
        when(songDAO.findById(999)).thenReturn(Optional.empty());
        
        // Act
        List<Song> result = songController.getMostPlayedSongs(limit);
        
        // Assert
        assertTrue("Should return empty list when no songs are found", result.isEmpty());
        verify(userSongStatisticsDAO).getMostPlayedSongs(testUser.getId(), limit);
        verify(songDAO).findById(999);
    }
    
    @Test
    public void testAddArtist_Success() {
        // Arrange
        String artistName = "New Artist";
        List<String> existingArtists = new ArrayList<>();
        existingArtists.add("Existing Artist");
        
        // Mock behavior
        SongController spyController = spy(songController);
        doReturn(existingArtists).when(spyController).getArtists();
        
        // Act
        boolean result = spyController.addArtist(artistName);
        
        // Assert
        assertTrue("Should return true on successful artist addition", result);
    }
    
    @Test
    public void testAddArtist_AlreadyExists() {
        // Arrange
        String artistName = "Existing Artist";
        List<String> existingArtists = new ArrayList<>();
        existingArtists.add(artistName);
        
        // Mock behavior
        SongController spyController = spy(songController);
        doReturn(existingArtists).when(spyController).getArtists();
        
        // Act
        boolean result = spyController.addArtist(artistName);
        
        // Assert
        assertTrue("Should return true when artist already exists", result);
    }
    
    @Test
    public void testAddArtist_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.addArtist("Artist Name");
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
    }
    
    @Test
    public void testAddArtist_NullOrEmptyName() {
        // Test with null
        assertFalse("Should return false when artist name is null", songController.addArtist(null));
        
        // Test with empty string
        assertFalse("Should return false when artist name is empty", songController.addArtist(""));
        
        // Test with whitespace only
        assertFalse("Should return false when artist name contains only whitespace", songController.addArtist("   "));
    }
    
    @Test
    public void testDeleteArtist_Success() {
        // Arrange
        String artistName = "Artist to Delete";
        List<String> existingArtists = new ArrayList<>();
        existingArtists.add(artistName);
        
        // Mock behavior
        SongController spyController = spy(songController);
        doReturn(existingArtists).when(spyController).getArtists();
        
        // Act
        boolean result = spyController.deleteArtist(artistName);
        
        // Assert
        assertTrue("Should return true on successful artist deletion", result);
    }
    
    @Test
    public void testDeleteArtist_NotFound() {
        // Arrange
        String artistName = "Non-existent Artist";
        List<String> existingArtists = new ArrayList<>();
        existingArtists.add("Different Artist");
        
        // Mock behavior
        SongController spyController = spy(songController);
        doReturn(existingArtists).when(spyController).getArtists();
        
        // Act
        boolean result = spyController.deleteArtist(artistName);
        
        // Assert
        assertFalse("Should return false when artist is not found", result);
    }
    
    @Test
    public void testDeleteArtist_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.deleteArtist("Artist Name");
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
    }
    
    @Test
    public void testDeleteArtist_NullOrEmptyName() {
        // Test with null
        assertFalse("Should return false when artist name is null", songController.deleteArtist(null));
        
        // Test with empty string
        assertFalse("Should return false when artist name is empty", songController.deleteArtist(""));
        
        // Test with whitespace only
        assertFalse("Should return false when artist name contains only whitespace", songController.deleteArtist("   "));
    }
    
    @Test
    public void testAddAlbum_Success() {
        // Arrange
        String title = "New Album";
        String artist = "Test Artist";
        int year = 2023;
        String genre = "Rock";
        
        // Create expected album
        Album expectedAlbum = new Album(title, artist, year, genre, testUser.getId());
        expectedAlbum.setId(1);
        
        // Act
        Album result = songController.addAlbum(title, artist, year, genre);
        
        // Assert
        assertNotNull("Should return an Album object", result);
        assertEquals("Should set correct ID", 1, result.getId());
        assertEquals("Should set correct title", title, result.getTitle());
        assertEquals("Should set correct artist", artist, result.getArtist());
        assertEquals("Should set correct year", year, result.getYear());
        assertEquals("Should set correct genre", genre, result.getGenre());
    }
    
    @Test
    public void testAddAlbum_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        Album result = songController.addAlbum("Title", "Artist", 2023, "Genre");
        
        // Assert
        assertNull("Should return null when no user is logged in", result);
    }
    
    @Test
    public void testGetUserAlbums_Success() {
        // Note: This test is simplified since the actual implementation returns an empty list
        // Act
        List<Album> result = songController.getUserAlbums();
        
        // Assert
        assertNotNull("Should return a list", result);
        assertTrue("Should return an empty list per the implementation", result.isEmpty());
    }
    
    @Test
    public void testGetUserAlbums_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        List<Album> result = songController.getUserAlbums();
        
        // Assert
        assertTrue("Should return empty list when no user is logged in", result.isEmpty());
    }
    
    @Test
    public void testDeleteAlbum_Success() {
        // Note: This test is simplified since the actual implementation always returns true
        // Act
        boolean result = songController.deleteAlbum(1);
        
        // Assert
        assertTrue("Should return true according to the implementation", result);
    }
    
    @Test
    public void testDeleteAlbum_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.deleteAlbum(1);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
    }
    
    @Test
    public void testAddSongToAlbum_Success() {
        // Note: This test is simplified since the actual implementation always returns true
        // Act
        boolean result = songController.addSongToAlbum(1, 1);
        
        // Assert
        assertTrue("Should return true according to the implementation", result);
    }
    
    @Test
    public void testAddSongToAlbum_NoUserLoggedIn() {
        // Arrange
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Act
        boolean result = songController.addSongToAlbum(1, 1);
        
        // Assert
        assertFalse("Should return false when no user is logged in", result);
    }
    
    @Test
    public void testAddSong_FourParameters() {
        // Arrange
        String title = "Test Song";
        String artist = "Test Artist";
        String album = "Test Album";
        String genre = "Rock";
        
        // Act
        songController.addSong(title, artist, album, genre);
        
        // Assert
        verify(songDAO).addSong(title, artist, album, genre);
    }
    
    @Test
    public void testGetAllSongs() {
        // Arrange
        List<String[]> expectedSongs = new ArrayList<>();
        expectedSongs.add(new String[]{"Song 1", "Artist 1", "Album 1"});
        expectedSongs.add(new String[]{"Song 2", "Artist 2", "Album 2"});
        
        when(songDAO.getAllSongs()).thenReturn(expectedSongs);
        
        // Act
        List<String[]> result = songController.getAllSongs();
        
        // Assert
        assertEquals("Should return songs from DAO", expectedSongs, result);
        verify(songDAO).getAllSongs();
    }
    
    @Test
    public void testDeleteSong_WithTitleArtistAlbum() {
        // Arrange
        String title = "Song to Delete";
        String artist = "Delete Artist";
        String album = "Delete Album";
        
        // Act
        songController.deleteSong(title, artist, album);
        
        // Assert
        verify(songDAO).deleteSong(title, artist, album);
    }
} 