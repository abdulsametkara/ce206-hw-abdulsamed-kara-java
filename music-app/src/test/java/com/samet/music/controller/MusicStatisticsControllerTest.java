package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.service.MusicStatisticsService;

public class MusicStatisticsControllerTest {

    private MusicStatisticsController musicStatisticsController;
    
    @Mock
    private UserController userController;
    
    @Mock
    private MusicStatisticsService musicStatisticsService;
    
    private User testUser;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // Test kullanıcısı oluştur
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        
        // Controller'ı oluştur
        musicStatisticsController = new MusicStatisticsController(userController);
        
        // MusicStatisticsService'i private field olarak erişilemediği için
        // reflection kullanarak test sınıfındaki mock ile değiştiriyoruz
        java.lang.reflect.Field serviceField = MusicStatisticsController.class.getDeclaredField("musicStatisticsService");
        serviceField.setAccessible(true);
        serviceField.set(musicStatisticsController, musicStatisticsService);
    }

    @Test
    public void testGetUserListeningSummary_UserLoggedIn() {
        // Test verisi
        Map<String, Object> expectedSummary = new HashMap<>();
        expectedSummary.put("total_songs", 100);
        expectedSummary.put("total_time", 5000);
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getUserListeningSummary(testUser.getId())).thenReturn(expectedSummary);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getUserListeningSummary();
        
        // Doğrulama
        assertEquals(expectedSummary, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getUserListeningSummary(testUser.getId());
    }
    
    @Test
    public void testGetUserListeningSummary_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getUserListeningSummary();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetListeningTrendReport_UserLoggedIn() {
        // Test verisi
        int days = 30;
        Map<String, Object> expectedReport = new HashMap<>();
        expectedReport.put("trend_data", "some data");
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getListeningTrendReport(eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedReport);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getListeningTrendReport(days);
        
        // Doğrulama
        assertEquals(expectedReport, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getListeningTrendReport(eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    public void testGetListeningTrendReport_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getListeningTrendReport(30);
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetMusicTasteProfile_UserLoggedIn() {
        // Test verisi
        Map<String, Object> expectedProfile = new HashMap<>();
        expectedProfile.put("profile_data", "some data");
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getMusicTasteProfile(testUser.getId())).thenReturn(expectedProfile);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getMusicTasteProfile();
        
        // Doğrulama
        assertEquals(expectedProfile, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getMusicTasteProfile(testUser.getId());
    }
    
    @Test
    public void testGetMusicTasteProfile_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        Map<String, Object> result = musicStatisticsController.getMusicTasteProfile();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetSimilarUserRecommendations_UserLoggedIn() {
        // Test verisi
        int limit = 5;
        List<Song> expectedRecommendations = new ArrayList<>();
        expectedRecommendations.add(new Song("Test Song", "Test Artist", "Test Album", "Rock", 2023, 180, "path/to/file", 1));
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getSimilarUserRecommendations(testUser.getId(), limit)).thenReturn(expectedRecommendations);
        
        // Test
        List<Song> result = musicStatisticsController.getSimilarUserRecommendations(limit);
        
        // Doğrulama
        assertEquals(expectedRecommendations, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getSimilarUserRecommendations(testUser.getId(), limit);
    }
    
    @Test
    public void testGetSimilarUserRecommendations_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        List<Song> result = musicStatisticsController.getSimilarUserRecommendations(5);
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetTimeOfDayListeningHabits_UserLoggedIn() {
        // Test verisi
        Map<String, Integer> expectedHabits = new HashMap<>();
        expectedHabits.put("Morning", 20);
        expectedHabits.put("Afternoon", 35);
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getTimeOfDayListeningHabits(testUser.getId())).thenReturn(expectedHabits);
        
        // Test
        Map<String, Integer> result = musicStatisticsController.getTimeOfDayListeningHabits();
        
        // Doğrulama
        assertEquals(expectedHabits, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getTimeOfDayListeningHabits(testUser.getId());
    }
    
    @Test
    public void testGetTimeOfDayListeningHabits_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        Map<String, Integer> result = musicStatisticsController.getTimeOfDayListeningHabits();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetDayOfWeekListeningActivity_UserLoggedIn() {
        // Test verisi
        Map<String, Integer> expectedActivity = new HashMap<>();
        expectedActivity.put("Monday", 15);
        expectedActivity.put("Friday", 30);
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getDayOfWeekListeningActivity(testUser.getId())).thenReturn(expectedActivity);
        
        // Test
        Map<String, Integer> result = musicStatisticsController.getDayOfWeekListeningActivity();
        
        // Doğrulama
        assertEquals(expectedActivity, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getDayOfWeekListeningActivity(testUser.getId());
    }
    
    @Test
    public void testGetDayOfWeekListeningActivity_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        Map<String, Integer> result = musicStatisticsController.getDayOfWeekListeningActivity();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testGetAverageSongDurationPreference_UserLoggedIn() {
        // Test verisi
        int expectedDuration = 240;
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getAverageSongDurationPreference(testUser.getId())).thenReturn(expectedDuration);
        
        // Test
        int result = musicStatisticsController.getAverageSongDurationPreference();
        
        // Doğrulama
        assertEquals(expectedDuration, result);
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getAverageSongDurationPreference(testUser.getId());
    }
    
    @Test
    public void testGetAverageSongDurationPreference_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        int result = musicStatisticsController.getAverageSongDurationPreference();
        
        // Doğrulama
        assertEquals(0, result);
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
    
    @Test
    public void testFormatSongDuration() {
        // Test
        assertEquals("00:00", musicStatisticsController.formatSongDuration(0));
        assertEquals("00:00", musicStatisticsController.formatSongDuration(-10));
        assertEquals("01:30", musicStatisticsController.formatSongDuration(90));
        assertEquals("05:05", musicStatisticsController.formatSongDuration(305));
        assertEquals("10:00", musicStatisticsController.formatSongDuration(600));
    }
    
    @Test
    public void testGetMusicInsights_UserLoggedIn() {
        // Test verisi
        Map<String, Object> testProfile = new HashMap<>();
        List<String> dominantGenres = new ArrayList<>();
        dominantGenres.add("Rock");
        dominantGenres.add("Pop");
        testProfile.put("dominant_genres", dominantGenres);
        testProfile.put("era_preference", "Modern");
        
        List<String> descriptors = new ArrayList<>();
        descriptors.add("Eclectic");
        testProfile.put("taste_descriptors", descriptors);
        
        Map<String, Integer> timeHabits = new HashMap<>();
        timeHabits.put("Evening", 50);
        timeHabits.put("Morning", 20);
        
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(testUser);
        when(musicStatisticsService.getMusicTasteProfile(testUser.getId())).thenReturn(testProfile);
        when(musicStatisticsService.getTimeOfDayListeningHabits(testUser.getId())).thenReturn(timeHabits);
        when(musicStatisticsService.getAverageSongDurationPreference(testUser.getId())).thenReturn(350);
        
        // Test
        List<String> insights = musicStatisticsController.getMusicInsights();
        
        // Doğrulama
        assertFalse(insights.isEmpty());
        verify(userController).getCurrentUser();
        verify(musicStatisticsService).getMusicTasteProfile(testUser.getId());
        verify(musicStatisticsService).getTimeOfDayListeningHabits(testUser.getId());
        verify(musicStatisticsService).getAverageSongDurationPreference(testUser.getId());
    }
    
    @Test
    public void testGetMusicInsights_UserNotLoggedIn() {
        // Mock davranışları
        when(userController.getCurrentUser()).thenReturn(null);
        
        // Test
        List<String> result = musicStatisticsController.getMusicInsights();
        
        // Doğrulama
        assertTrue(result.isEmpty());
        verify(userController).getCurrentUser();
        verifyNoInteractions(musicStatisticsService);
    }
} 