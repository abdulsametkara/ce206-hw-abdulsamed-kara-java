package com.samet.music.ui;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.service.MusicRecommendationSystem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RecommendationUITest {

    private RecommendationUI recommendationUI;

    @Mock
    private MusicCollectionService mockCollectionService;

    @Mock
    private MusicRecommendationSystem mockRecommendationSystem;

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);

        // Create a test instance with controlled input
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
        Scanner scanner = new Scanner(inputStream);

        recommendationUI = new RecommendationUI(scanner, printStream);

        // Use reflection to replace the service field
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(recommendationUI, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(recommendationUI, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }
    }

    // Helper method to create a scanner with predefined input
    private Scanner createScannerWithInput(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        return new Scanner(inputStream);
    }

    @Test
    public void testShowSongRecommendationsByGenreEmptyRecommendations() {
        // Setup
        String userId = "user123";
        when(mockRecommendationSystem.recommendSongsByGenre(userId, 10))
                .thenReturn(Collections.emptyList());
        
        Map<String, Integer> genrePrefs = new HashMap<>();
        genrePrefs.put("Rock", 5);
        when(mockRecommendationSystem.getUserTopGenres(userId)).thenReturn(genrePrefs);

        // Execute
        recommendationUI.showSongRecommendationsByGenre(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("No song recommendations found"));
        verify(mockRecommendationSystem).recommendSongsByGenre(userId, 10);
        verify(mockRecommendationSystem).getUserTopGenres(userId);
    }

    @Test
    public void testShowSongRecommendationsByGenreWithRecommendations() {
        // Setup
        String userId = "user123";
        
        // Create artists, albums and songs
        Artist artist1 = new Artist("Artist One");
        artist1.setId("artist1");
        Album album1 = new Album("Album One", artist1, 2020);
        album1.setId("album1");
        album1.setGenre("Rock");
        
        Song song1 = new Song("Song One", artist1, 180);
        song1.setId("song1");
        song1.setGenre("Rock");
        song1.setAlbum(album1);
        
        List<Song> recommendations = new ArrayList<>();
        recommendations.add(song1);
        
        Map<String, Integer> genrePrefs = new HashMap<>();
        genrePrefs.put("Rock", 5);

        // Setup mock responses
        when(mockRecommendationSystem.recommendSongsByGenre(userId, 10)).thenReturn(recommendations);
        when(mockRecommendationSystem.getUserTopGenres(userId)).thenReturn(genrePrefs);
        
        // Use scanner with "2" input (No, don't listen)
        Scanner scanner = createScannerWithInput("2\n");
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showSongRecommendationsByGenre(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Song One"));
        assertTrue(output.contains("Artist One"));
        assertTrue(output.contains("3:00")); // 180 seconds formatted
        verify(mockRecommendationSystem).recommendSongsByGenre(userId, 10);
    }

    @Test
    public void testShowSongRecommendationsAndListenToRecommendedSong() {
        // Setup
        String userId = "user123";
        
        // Create artists, albums and songs
        Artist artist1 = new Artist("Artist One");
        artist1.setId("artist1");
        Album album1 = new Album("Album One", artist1, 2020);
        album1.setId("album1");
        album1.setGenre("Rock");
        
        Song song1 = new Song("Song One", artist1, 180);
        song1.setId("song1");
        song1.setGenre("Rock");
        song1.setAlbum(album1);
        
        List<Song> recommendations = new ArrayList<>();
        recommendations.add(song1);
        
        Map<String, Integer> genrePrefs = new HashMap<>();
        genrePrefs.put("Rock", 5);

        // Setup mock responses
        when(mockRecommendationSystem.recommendSongsByGenre(userId, 10)).thenReturn(recommendations);
        when(mockRecommendationSystem.getUserTopGenres(userId)).thenReturn(genrePrefs);
        
        // Use scanner with input to simulate selecting a song and rating it
        Scanner scanner = createScannerWithInput("1\n1\n1\n"); // Yes listen + Song #1 + Rating "1" (Like)
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showSongRecommendationsByGenre(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Now playing: Song One"));
        assertTrue(output.contains("Song finished playing"));
        verify(mockRecommendationSystem).recordSongPlay(userId, "song1");
        verify(mockRecommendationSystem).updateGenrePreference(userId, "Rock", 3);
        verify(mockRecommendationSystem).updateArtistPreference(userId, "artist1", 3);
    }

    @Test
    public void testShowAlbumRecommendationsEmptyRecommendations() {
        // Setup
        String userId = "user123";
        when(mockRecommendationSystem.recommendAlbumsByArtist(userId, 5))
                .thenReturn(Collections.emptyList());
        
        Map<String, Integer> artistPrefs = new HashMap<>();
        artistPrefs.put("artist1", 5);
        when(mockRecommendationSystem.getUserTopArtists(userId)).thenReturn(artistPrefs);
        
        Artist artist1 = new Artist("Artist One");
        artist1.setId("artist1");
        when(mockCollectionService.getArtistById("artist1")).thenReturn(artist1);

        // Execute
        recommendationUI.showAlbumRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("No album recommendations found"));
        verify(mockRecommendationSystem).recommendAlbumsByArtist(userId, 5);
    }

    @Test
    public void testShowAlbumRecommendationsWithRecommendations() {
        // Setup
        String userId = "user123";
        
        // Create artists and albums
        Artist artist1 = new Artist("Artist One");
        artist1.setId("artist1");
        
        Album album1 = new Album("Album One", artist1, 2020);
        album1.setId("album1");
        album1.setGenre("Rock");
        
        List<Album> recommendations = new ArrayList<>();
        recommendations.add(album1);
        
        Map<String, Integer> artistPrefs = new HashMap<>();
        artistPrefs.put("artist1", 5);
        
        // Setup mock responses
        when(mockRecommendationSystem.recommendAlbumsByArtist(userId, 5)).thenReturn(recommendations);
        when(mockRecommendationSystem.getUserTopArtists(userId)).thenReturn(artistPrefs);
        when(mockCollectionService.getArtistById("artist1")).thenReturn(artist1);
        
        // Use scanner with "2" input (No, don't view)
        Scanner scanner = createScannerWithInput("2\n");
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showAlbumRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Album One"));
        assertTrue(output.contains("Artist One"));
        assertTrue(output.contains("2020"));
        verify(mockRecommendationSystem).recommendAlbumsByArtist(userId, 5);
    }

    @Test
    public void testShowAlbumRecommendationsAndViewAlbumTracks() {
        // Setup
        String userId = "user123";
        
        // Create artists and albums
        Artist artist1 = new Artist("Artist One");
        artist1.setId("artist1");
        
        Album album1 = new Album("Album One", artist1, 2020);
        album1.setId("album1");
        album1.setGenre("Rock");
        
        Song song1 = new Song("Track One", artist1, 180);
        song1.setId("song1");
        
        List<Song> albumSongs = new ArrayList<>();
        albumSongs.add(song1);
        
        List<Album> recommendations = new ArrayList<>();
        recommendations.add(album1);
        
        Map<String, Integer> artistPrefs = new HashMap<>();
        artistPrefs.put("artist1", 5);
        
        // Setup mock responses
        when(mockRecommendationSystem.recommendAlbumsByArtist(userId, 5)).thenReturn(recommendations);
        when(mockRecommendationSystem.getUserTopArtists(userId)).thenReturn(artistPrefs);
        when(mockCollectionService.getArtistById("artist1")).thenReturn(artist1);
        when(mockCollectionService.getSongsByAlbum("album1")).thenReturn(albumSongs);
        
        // Use scanner with "1" input and "1" to select album
        Scanner scanner = createScannerWithInput("1\n1\n");
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showAlbumRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Album: Album One by Artist One"));
        assertTrue(output.contains("Track One"));
        verify(mockCollectionService).getSongsByAlbum("album1");
    }

    @Test
    public void testShowArtistRecommendationsEmptyRecommendations() {
        // Setup
        String userId = "user123";
        when(mockRecommendationSystem.recommendArtists(userId, 5))
                .thenReturn(Collections.emptyList());

        // Execute
        recommendationUI.showArtistRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("No artist recommendations found"));
        verify(mockRecommendationSystem).recommendArtists(userId, 5);
    }

    @Test
    public void testShowArtistRecommendationsWithRecommendations() {
        // Setup
        String userId = "user123";
        
        // Create artists
        Artist artist1 = new Artist("Artist One", "Famous rock artist");
        artist1.setId("artist1");
        
        List<Artist> recommendations = new ArrayList<>();
        recommendations.add(artist1);
        
        // Setup mock responses
        when(mockRecommendationSystem.recommendArtists(userId, 5)).thenReturn(recommendations);
        
        // Use scanner with "2" input (No, don't view)
        Scanner scanner = createScannerWithInput("2\n");
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showArtistRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Artist One"));
        assertTrue(output.contains("Famous rock artist"));
        verify(mockRecommendationSystem).recommendArtists(userId, 5);
    }

    @Test
    public void testShowArtistRecommendationsAndViewArtistDetails() {
        // Setup
        String userId = "user123";
        
        // Create artists, albums and songs
        Artist artist1 = new Artist("Artist One", "Famous rock artist with multiple hit albums");
        artist1.setId("artist1");
        
        Album album1 = new Album("Album One", artist1, 2020);
        album1.setId("album1");
        
        Song song1 = new Song("Popular Song", artist1, 180);
        song1.setId("song1");
        
        List<Album> artistAlbums = new ArrayList<>();
        artistAlbums.add(album1);
        
        List<Song> popularSongs = new ArrayList<>();
        popularSongs.add(song1);
        
        List<Artist> recommendations = new ArrayList<>();
        recommendations.add(artist1);
        
        // Setup mock responses
        when(mockRecommendationSystem.recommendArtists(userId, 5)).thenReturn(recommendations);
        when(mockCollectionService.getAlbumsByArtist("artist1")).thenReturn(artistAlbums);
        when(mockCollectionService.getSongsByArtist("artist1")).thenReturn(popularSongs);
        
        // Use scanner with "1" input and "1" to select artist
        Scanner scanner = createScannerWithInput("1\n1\n");
        RecommendationUI ui = new RecommendationUI(scanner, printStream);
        
        // Inject mocks
        try {
            java.lang.reflect.Field serviceField = RecommendationUI.class.getDeclaredField("service");
            serviceField.setAccessible(true);
            serviceField.set(ui, mockCollectionService);

            java.lang.reflect.Field recommendationSystemField = RecommendationUI.class.getDeclaredField("recommendationSystem");
            recommendationSystemField.setAccessible(true);
            recommendationSystemField.set(ui, mockRecommendationSystem);
        } catch (Exception e) {
            fail("Failed to inject mock services: " + e.getMessage());
        }

        // Execute
        ui.showArtistRecommendations(userId);

        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Artist: Artist One"));
        assertTrue(output.contains("Famous rock artist"));
        assertTrue(output.contains("Albums:"));
        assertTrue(output.contains("Album One"));
        assertTrue(output.contains("Popular Songs:"));
        assertTrue(output.contains("Popular Song"));
        verify(mockCollectionService).getAlbumsByArtist("artist1");
        verify(mockCollectionService).getSongsByArtist("artist1");
    }

    @Test
    public void testTruncateBiography() throws Exception {
        // Setup
        java.lang.reflect.Method truncateMethod = RecommendationUI.class.getDeclaredMethod(
                "truncateBiography", String.class, int.class);
        truncateMethod.setAccessible(true);
        
        // Test cases
        String shortBio = "Short biography";
        String longBio = "This is a very long biography that needs to be truncated to a shorter length for display purposes";

        // Execute and verify
        assertEquals(shortBio, truncateMethod.invoke(recommendationUI, shortBio, 20)); // Short bio unchanged
        assertEquals("This is a very...", truncateMethod.invoke(recommendationUI, longBio, 17)); // Long bio truncated
    }
} 