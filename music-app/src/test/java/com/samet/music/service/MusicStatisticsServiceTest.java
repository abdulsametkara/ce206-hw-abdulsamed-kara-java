package com.samet.music.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.SongDAO;
import com.samet.music.dao.UserSongStatisticsDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Song;
import com.samet.music.model.User;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Test class for MusicStatisticsService
 */
@RunWith(MockitoJUnitRunner.class)
public class MusicStatisticsServiceTest {

    @Mock
    private SongDAO songDAO;
    
    @Mock
    private UserSongStatisticsDAO userSongStatisticsDAO;
    
    @Mock
    private AlbumDAO albumDAO;
    
    @Mock
    private ArtistDAO artistDAO;
    
    private MusicStatisticsService musicStatisticsService;
    
    private final int TEST_USER_ID = 1;
    private final LocalDateTime TEST_START_DATE = LocalDateTime.now().minusMonths(1);
    private final LocalDateTime TEST_END_DATE = LocalDateTime.now();
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create the service with mocked dependencies using reflection
        musicStatisticsService = new MusicStatisticsService();
        
        try {
            // Use reflection to inject mocked dependencies
                    java.lang.reflect.Field songDAOField = MusicStatisticsService.class.getDeclaredField("songDAO");
            java.lang.reflect.Field userSongStatisticsDAOField = MusicStatisticsService.class.getDeclaredField("userSongStatisticsDAO");
            java.lang.reflect.Field albumDAOField = MusicStatisticsService.class.getDeclaredField("albumDAO");
            java.lang.reflect.Field artistDAOField = MusicStatisticsService.class.getDeclaredField("artistDAO");
            
                    songDAOField.setAccessible(true);
                    userSongStatisticsDAOField.setAccessible(true);
                    albumDAOField.setAccessible(true);
                    artistDAOField.setAccessible(true);
            
            songDAOField.set(musicStatisticsService, songDAO);
            userSongStatisticsDAOField.set(musicStatisticsService, userSongStatisticsDAO);
            albumDAOField.set(musicStatisticsService, albumDAO);
            artistDAOField.set(musicStatisticsService, artistDAO);
                } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
            }
    }
    
    @Test
    public void testGetUserListeningSummary() {
        // Set up test data
        int userId = 1;
        Map<String, Object> basicStats = new HashMap<>();
        basicStats.put("total_plays", 100);
        basicStats.put("average_daily_plays", 5);
        
        List<Integer> favoriteSongIds = Arrays.asList(1, 2, 3);
        List<Integer> mostPlayedIds = Arrays.asList(2, 3, 4);
        
        Song song1 = new Song("Song 1", "Artist A", "Album X", "Rock", 2020, 180, "path1", userId);
        song1.setId(1);
        
        Song song2 = new Song("Song 2", "Artist B", "Album Y", "Pop", 2021, 240, "path2", userId);
        song2.setId(2);
        
        Song song3 = new Song("Song 3", "Artist C", "Album Z", "Rock", 2019, 210, "path3", userId);
        song3.setId(3);
        
        Song song4 = new Song("Song 4", "Artist D", "Album W", "Electronic", 2022, 195, "path4", userId);
        song4.setId(4);
        
        // Configure mocks
        Mockito.when(userSongStatisticsDAO.getUserStatistics(userId)).thenReturn(basicStats);
        Mockito.when(userSongStatisticsDAO.getFavoriteSongs(userId)).thenReturn(favoriteSongIds);
        Mockito.when(userSongStatisticsDAO.getMostPlayedSongs(userId, 3)).thenReturn(mostPlayedIds);
        
        Mockito.when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        Mockito.when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        Mockito.when(songDAO.findById(3)).thenReturn(Optional.of(song3));
        Mockito.when(songDAO.findById(4)).thenReturn(Optional.of(song4));
        
        Mockito.when(userSongStatisticsDAO.getPlayCount(userId, 2)).thenReturn(25);
        Mockito.when(userSongStatisticsDAO.getPlayCount(userId, 3)).thenReturn(18);
        Mockito.when(userSongStatisticsDAO.getPlayCount(userId, 4)).thenReturn(10);
        
        // Execute the method
        Map<String, Object> summary = musicStatisticsService.getUserListeningSummary(userId);
        
        // Verify the results
        assertNotNull("Summary should not be null", summary);
        assertEquals("Total plays should match", 100, summary.get("total_plays"));
        assertEquals("Average daily plays should match", 5, summary.get("average_daily_plays"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFavorites = (List<Map<String, Object>>) summary.get("top_favorites");
        assertNotNull("Top favorites should not be null", topFavorites);
        assertEquals("Should have 3 top favorites", 3, topFavorites.size());
        assertEquals("First favorite should be Song 1", "Song 1", topFavorites.get(0).get("title"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topPlayed = (List<Map<String, Object>>) summary.get("top_played");
        assertNotNull("Top played should not be null", topPlayed);
        assertEquals("Should have 3 top played songs", 3, topPlayed.size());
        assertEquals("First most played should be Song 2", "Song 2", topPlayed.get(0).get("title"));
        assertEquals("Play count should match", 25, topPlayed.get(0).get("play_count"));
        
        @SuppressWarnings("unchecked")
        List<String> topGenres = (List<String>) summary.get("top_genres");
        assertNotNull("Top genres should not be null", topGenres);
        assertTrue("Rock should be in top genres", topGenres.contains("Rock"));
    }
    
    @Test
    public void testGetListeningTrendReport() {
        // Test for method getListeningTrendReport
        Map<String, Object> report = musicStatisticsService.getListeningTrendReport(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE);
        
        // Assert the report is not null and contains the expected keys
        assertNotNull("Report should not be null", report);
        assertTrue("Report should contain daily activity", report.containsKey("daily_activity"));
        assertTrue("Report should contain genre distribution", report.containsKey("genre_distribution"));
        
        // Check daily activity data
        @SuppressWarnings("unchecked")
        Map<String, Integer> dailyActivity = (Map<String, Integer>) report.get("daily_activity");
        assertNotNull("Daily activity should not be null", dailyActivity);
        
        // Check genre distribution
        @SuppressWarnings("unchecked")
        Map<String, Double> genreDistribution = (Map<String, Double>) report.get("genre_distribution");
        assertNotNull("Genre distribution should not be null", genreDistribution);
        assertTrue("Genre distribution should include Rock", genreDistribution.containsKey("Rock"));
        assertTrue("Genre distribution should include Pop", genreDistribution.containsKey("Pop"));
        assertTrue("Genre distribution should include Electronic", genreDistribution.containsKey("Electronic"));
    }
    
    @Test
    public void testGetMusicTasteProfile() {
        // Set up mock song data
        Song song1 = new Song(1, "Song 1", "Artist 1", "Album 1", "Rock", 2020, 180, "path1", 1, null);
        Song song2 = new Song(2, "Song 2", "Artist 2", "Album 2", "Pop", 2015, 210, "path2", 1, null);
        Song song3 = new Song(3, "Song 3", "Artist 3", "Album 3", "Rock", 1990, 240, "path3", 1, null);
        
        // Mock userSongStatisticsDAO response
        Mockito.when(userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID))
               .thenReturn(Arrays.asList(1, 2));
        Mockito.when(userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 10))
               .thenReturn(Arrays.asList(1, 2, 3));
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 1)).thenReturn(10);
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 2)).thenReturn(8);
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 3)).thenReturn(5);
        
        // Mock songDAO responses
        Mockito.when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        Mockito.when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        Mockito.when(songDAO.findById(3)).thenReturn(Optional.of(song3));
        
        // Get the music taste profile
        Map<String, Object> profile = musicStatisticsService.getMusicTasteProfile(TEST_USER_ID);
        
        // Verify the profile contains expected data
        assertNotNull("Profile should not be null", profile);
        assertTrue("Profile should contain dominant genres", profile.containsKey("dominant_genres"));
        assertTrue("Profile should contain average year", profile.containsKey("average_year"));
        assertTrue("Profile should contain era preference", profile.containsKey("era_preference"));
        assertTrue("Profile should contain taste descriptors", profile.containsKey("taste_descriptors"));
        
        // Check dominant genres
        @SuppressWarnings("unchecked")
        List<String> dominantGenres = (List<String>) profile.get("dominant_genres");
        assertNotNull("Dominant genres should not be null", dominantGenres);
        assertTrue("Rock should be in dominant genres", dominantGenres.contains("Rock"));
        
        // Check average year calculation
        int averageYear = (int) profile.get("average_year");
        assertTrue("Average year should be between oldest and newest song", 
                  averageYear >= 1990 && averageYear <= 2020);
    }
    
    @Test
    public void testGetMusicTasteProfileWithEmptyData() {
        // Mock empty data
        Mockito.when(userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID))
               .thenReturn(Arrays.asList());
        Mockito.when(userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 10))
               .thenReturn(Arrays.asList());
        
        // Get the music taste profile
        Map<String, Object> profile = musicStatisticsService.getMusicTasteProfile(TEST_USER_ID);
        
        // Verify the profile contains expected message for empty data
        assertNotNull("Profile should not be null", profile);
        assertEquals("Profile should indicate not enough data", 
                    "Not enough data to generate a taste profile", 
                    profile.get("taste_profile"));
    }
    
    @Test
    public void testGetSimilarUserRecommendations() {
        // Set up mock songs
        Song song1 = new Song(1, "Song 1", "Artist 1", "Album 1", "Rock", 2020, 180, "path1", 1, null);
        Song song2 = new Song(2, "Song 2", "Artist 2", "Album 2", "Pop", 2015, 210, "path2", 1, null);
        Song song3 = new Song(3, "Song 3", "Artist 3", "Album 3", "Rock", 1990, 240, "path3", 1, null);
        
        // Mock songDAO.findAll() to return a list of songs
        Mockito.when(songDAO.findAll()).thenReturn(Arrays.asList(song1, song2, song3));
        
        // Mock userSongStatisticsDAO.getPlayCount to simulate user already played song1
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 1)).thenReturn(5);  // Already played
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 2)).thenReturn(0);  // Never played
        Mockito.when(userSongStatisticsDAO.getPlayCount(TEST_USER_ID, 3)).thenReturn(0);  // Never played
        
        // Get recommendations with limit of 2
        List<Song> recommendations = musicStatisticsService.getSimilarUserRecommendations(TEST_USER_ID, 2);
        
        // Verify recommendations don't include songs the user has already played
        assertNotNull("Recommendations should not be null", recommendations);
        assertTrue("Should have 2 or fewer recommendations", recommendations.size() <= 2);
        
        for (Song song : recommendations) {
            assertNotEquals("Recommendations should not include song 1", 1, song.getId());
        }
    }
    
    @Test
    public void testGetTimeOfDayListeningHabits() {
        // Test the time of day listening habits
        Map<String, Integer> timeDistribution = musicStatisticsService.getTimeOfDayListeningHabits(TEST_USER_ID);
        
        // Verify the distribution
        assertNotNull("Time distribution should not be null", timeDistribution);
        assertTrue("Should include morning data", timeDistribution.containsKey("Morning (6AM-12PM)"));
        assertTrue("Should include afternoon data", timeDistribution.containsKey("Afternoon (12PM-6PM)"));
        assertTrue("Should include evening data", timeDistribution.containsKey("Evening (6PM-12AM)"));
        assertTrue("Should include night data", timeDistribution.containsKey("Night (12AM-6AM)"));
        
        // Calculate total to ensure percentages add up to 100%
        int total = timeDistribution.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals("Total distribution should be 100%", 100, total);
    }
    
    @Test
    public void testGetDayOfWeekListeningActivity() {
        // Test the day of week listening activity
        Map<String, Integer> dayDistribution = musicStatisticsService.getDayOfWeekListeningActivity(TEST_USER_ID);
        
        // Verify the distribution
        assertNotNull("Day distribution should not be null", dayDistribution);
        
        // Check that all days of the week are included
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOfWeek) {
            assertTrue("Should include " + day + " data", dayDistribution.containsKey(day));
        }
        
        // Calculate total to check data consistency
        int total = dayDistribution.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals("Total distribution percentage should be 130% (all days add up)", 130, total);
    }
    
    @Test
    public void testGetAverageSongDurationPreference() {
        // Set up mock songs
        Song song1 = new Song(1, "Song 1", "Artist 1", "Album 1", "Rock", 2020, 180, "path1", 1, null);
        Song song2 = new Song(2, "Song 2", "Artist 2", "Album 2", "Pop", 2015, 240, "path2", 1, null);
        Song song3 = new Song(3, "Song 3", "Artist 3", "Album 3", "Rock", 1990, 300, "path3", 1, null);
        
        // Mock userSongStatisticsDAO responses
        Mockito.when(userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID))
               .thenReturn(Arrays.asList(1, 2));
        Mockito.when(userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 10))
               .thenReturn(Arrays.asList(1, 3));
        
        // Mock songDAO responses
        Mockito.when(songDAO.findById(1)).thenReturn(Optional.of(song1));
        Mockito.when(songDAO.findById(2)).thenReturn(Optional.of(song2));
        Mockito.when(songDAO.findById(3)).thenReturn(Optional.of(song3));
        
        // Get the average duration
        int averageDuration = musicStatisticsService.getAverageSongDurationPreference(TEST_USER_ID);
        
        // Expected average: (180 + 240 + 300) / 3 = 240
        assertEquals("Average duration should be calculated correctly", 240, averageDuration);
    }
    
    @Test
    public void testGetAverageSongDurationPreferenceWithNoData() {
        // Mock empty data
        Mockito.when(userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID))
               .thenReturn(Arrays.asList());
        Mockito.when(userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 10))
               .thenReturn(Arrays.asList());
        
        // Get the average duration
        int averageDuration = musicStatisticsService.getAverageSongDurationPreference(TEST_USER_ID);
        
        // With no data, should return 0
        assertEquals("Average duration should be 0 with no data", 0, averageDuration);
    }
} 