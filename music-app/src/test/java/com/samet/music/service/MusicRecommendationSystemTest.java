package com.samet.music.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.samet.music.model.Song;
import com.samet.music.model.Artist;
import com.samet.music.model.Album;
import com.samet.music.service.MusicCollectionService;

import java.util.Collections;
import java.util.List;

public class MusicRecommendationSystemTest {
    private MusicRecommendationSystem recommendationSystem;

    @Mock
    private MusicCollectionService musicService;

    @BeforeEach
    public void setUp() {
        // Reset Mockito mocks
        MockitoAnnotations.openMocks(this);

        // Reset the singleton instance
        recommendationSystem = MusicRecommendationSystem.getInstance();

        // Configure mock service with empty collections
        when(musicService.getAllSongs()).thenReturn(Collections.emptyList());
        when(musicService.getAllArtists()).thenReturn(Collections.emptyList());
        when(musicService.getAllAlbums()).thenReturn(Collections.emptyList());

        // Customize behavior to prevent NPEs
        when(musicService.getSongById(anyString())).thenReturn(null);
        when(musicService.getSongsByArtist(anyString())).thenReturn(Collections.emptyList());
    }

    @Test
    public void testRecordSongPlay() {
        String userId = "user1";
        String songId = "song1";

        // Record a song play
        recommendationSystem.recordSongPlay(userId, songId);

        // Verify genre and artist preferences are not updated
        // (since song is not found)
        assertTrue(recommendationSystem.getUserTopGenres(userId).isEmpty(),
                "Genre preferences should be empty");

        assertTrue(recommendationSystem.getUserTopArtists(userId).isEmpty(),
                "Artist preferences should be empty");
    }

    @Test
    public void testRecommendSongsByGenre() {
        String userId = "user2";

        // Attempt to record song play
        recommendationSystem.recordSongPlay(userId, "song1");

        // Get recommendations
        List<Song> recommendations = recommendationSystem.recommendSongsByGenre(userId, 5);

        assertTrue(recommendations.isEmpty(),
                "Recommendations should be empty when no preferences exist");
    }

    @Test
    public void testRecommendSongsBySimilarArtist() {
        String userId = "user3";

        // Attempt to record song play
        recommendationSystem.recordSongPlay(userId, "song1");

        // Get recommendations
        List<Song> recommendations = recommendationSystem.recommendSongsBySimilarArtist(userId, 5);

        assertTrue(recommendations.isEmpty(),
                "Recommendations should be empty when no preferences exist");
    }

    @Test
    public void testRecommendAlbumsByArtist() {
        String userId = "user4";

        // Attempt to record song play
        recommendationSystem.recordSongPlay(userId, "song1");

        // Get album recommendations
        List<Album> recommendations = recommendationSystem.recommendAlbumsByArtist(userId, 5);

        assertTrue(recommendations.isEmpty(),
                "Album recommendations should be empty when no preferences exist");
    }

    @Test
    public void testRecommendArtists() {
        String userId = "user5";

        // Attempt to record song play
        recommendationSystem.recordSongPlay(userId, "song1");

        // Get artist recommendations
        List<Artist> recommendations = recommendationSystem.recommendArtists(userId, 5);

        assertTrue(recommendations.isEmpty(),
                "Artist recommendations should be empty when no preferences exist");
    }

    @Test
    public void testSaveAndLoadRecommendationData() {
        String userId = "user6";
        String testFilePath = "test_recommendation_data.ser";

        // Record some listening history
        recommendationSystem.recordSongPlay(userId, "song1");

        // Save recommendation data
        boolean saveResult = recommendationSystem.saveRecommendationData(testFilePath);
        assertTrue(saveResult, "Should successfully save recommendation data");

        // Create a new instance to test loading
        MusicRecommendationSystem newInstance = MusicRecommendationSystem.getInstance();

        // Load recommendation data
        boolean loadResult = newInstance.loadRecommendationData(testFilePath);
        assertTrue(loadResult, "Should successfully load recommendation data");

        // Verify loaded data is empty
        assertTrue(newInstance.getUserTopGenres(userId).isEmpty(),
                "Loaded data should not contain user genre preferences");
    }

    @Test
    public void testNullAndEmptyInputHandling() {
        // Test null inputs
        assertDoesNotThrow(() -> recommendationSystem.recordSongPlay(null, null),
                "Should handle null userId and songId gracefully");

        // Test empty genre and artist preference updates
        assertDoesNotThrow(() -> recommendationSystem.updateGenrePreference("user7", "", 1),
                "Should handle empty genre gracefully");

        assertDoesNotThrow(() -> recommendationSystem.updateArtistPreference("user7", "", 1),
                "Should handle empty artistId gracefully");

        // Test recommendations with no preferences
        List<Song> songRecs = recommendationSystem.recommendSongsByGenre("nonexistent_user", 5);
        assertTrue(songRecs.isEmpty(), "Should return empty list for users with no preferences");

        List<Artist> artistRecs = recommendationSystem.recommendArtists("nonexistent_user", 5);
        assertTrue(artistRecs.isEmpty(), "Should return empty list for users with no preferences");
    }
}