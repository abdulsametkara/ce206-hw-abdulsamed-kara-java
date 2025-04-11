package com.samet.music.ui;

import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PlaylistUITest {

    private PlaylistUI playlistUI;

    @Mock
    private MusicCollectionService mockService;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        System.setOut(new PrintStream(outputStream));
        
        // Test instance with empty input
        Scanner emptyScanner = new Scanner("");
        playlistUI = new PlaylistUI(mockService, emptyScanner);
    }

    /**
     * Scanner oluşturup belirtilen girişi atayan yardımcı metot
     */
    private Scanner createScannerWithInput(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        return new Scanner(inputStream);
    }

    /**
     * Test sonrası temizleme
     */
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testCreatePlaylistSuccess() {
        // Test verileri
        String playlistName = "Test Playlist";
        String description = "Test Description";
        Scanner inputScanner = createScannerWithInput(playlistName + "\n" + description + "\n");
        
        // Mock ayarları
        when(mockService.createPlaylist(playlistName, description)).thenReturn(true);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.createPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Playlist created successfully"));
        verify(mockService).createPlaylist(playlistName, description);
    }

    @Test
    public void testCreatePlaylistFailure() {
        // Test verileri
        String playlistName = "Failed Playlist";
        String description = "Failed Description";
        Scanner inputScanner = createScannerWithInput(playlistName + "\n" + description + "\n");
        
        // Mock ayarları
        when(mockService.createPlaylist(playlistName, description)).thenReturn(false);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.createPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Failed to create playlist"));
        verify(mockService).createPlaylist(playlistName, description);
    }

    @Test
    public void testViewPlaylistsEmpty() {
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(Collections.emptyList());
        
        // Metot çağrısı
        playlistUI.viewPlaylists();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("No playlists found"));
        verify(mockService).getAllPlaylists();
    }

    @Test
    public void testViewPlaylistsWithData() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist1 = new Playlist("Playlist 1", "Description 1");
        playlist1.setId("1");
        Playlist playlist2 = new Playlist("Playlist 2", "Description 2");
        playlist2.setId("2");
        playlists.add(playlist1);
        playlists.add(playlist2);
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        
        // Metot çağrısı
        playlistUI.viewPlaylists();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Playlist 1"));
        assertTrue(output.contains("Playlist 2"));
        verify(mockService).getAllPlaylists();
    }

    @Test
    public void testEditPlaylistNoPlaylists() {
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(Collections.emptyList());
        
        // Metot çağrısı
        playlistUI.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("No playlists found"));
        verify(mockService).getAllPlaylists();
    }

    @Test
    public void testEditPlaylistRenameSuccess() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Old Name", "Description");
        playlist.setId("1");
        playlists.add(playlist);
        
        // Input: 1 (seçilen çalma listesi) + 1 (yeniden adlandırma seçeneği) + "New Name" (yeni ad)
        Scanner inputScanner = createScannerWithInput("1\n1\nNew Name\n");
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        when(mockService.updatePlaylist(playlist)).thenReturn(true);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Playlist renamed from 'Old Name' to 'New Name'"));
        verify(mockService).updatePlaylist(playlist);
        assertEquals("New Name", playlist.getName());
    }

    @Test
    public void testEditPlaylistRenameFailure() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Old Name", "Description");
        playlist.setId("1");
        playlists.add(playlist);
        
        // Input: 1 (seçilen çalma listesi) + 1 (yeniden adlandırma seçeneği) + "New Name" (yeni ad)
        Scanner inputScanner = createScannerWithInput("1\n1\nNew Name\n");
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        when(mockService.updatePlaylist(playlist)).thenReturn(false);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Failed to rename playlist"));
        verify(mockService).updatePlaylist(playlist);
        assertEquals("Old Name", playlist.getName()); // İsim değişmemiş olmalı
    }

    @Test
    public void testEditPlaylistChangeDescriptionSuccess() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Playlist", "Old Description");
        playlist.setId("1");
        playlists.add(playlist);
        
        // Input: 1 (seçilen çalma listesi) + 2 (açıklama değiştirme seçeneği) + "New Description" (yeni açıklama)
        Scanner inputScanner = createScannerWithInput("1\n2\nNew Description\n");
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        when(mockService.updatePlaylist(playlist)).thenReturn(true);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Playlist description updated"));
        verify(mockService).updatePlaylist(playlist);
        assertEquals("New Description", playlist.getDescription());
    }

    @Test
    public void testAddSongsToPlaylist() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Playlist", "Description");
        playlist.setId("1");
        playlists.add(playlist);
        
        List<Song> allSongs = new ArrayList<>();
        Artist artist = new Artist("Test Artist");
        Song song1 = new Song("Song 1", artist, 180);
        song1.setId("song1");
        Song song2 = new Song("Song 2", artist, 210);
        song2.setId("song2");
        allSongs.add(song1);
        allSongs.add(song2);
        
        // Input: 1 (seçilen çalma listesi) + 3 (şarkı ekleme seçeneği) + 1 ve 2 (seçilen şarkılar) + 0 (bitir)
        Scanner inputScanner = createScannerWithInput("1\n3\n1\n2\n0\n");
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        when(mockService.getAllSongs()).thenReturn(allSongs);
        when(mockService.addSongToPlaylist("song1", "1")).thenReturn(true);
        when(mockService.addSongToPlaylist("song2", "1")).thenReturn(true);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Added: Song 1"));
        assertTrue(output.contains("Added: Song 2"));
        verify(mockService).addSongToPlaylist("song1", "1");
        verify(mockService).addSongToPlaylist("song2", "1");
    }

    @Test
    public void testRemoveSongsFromPlaylist() {
        // Test verileri
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist("Playlist", "Description");
        playlist.setId("1");
        playlists.add(playlist);
        
        List<Song> playlistSongs = new ArrayList<>();
        Artist artist = new Artist("Test Artist");
        Song song1 = new Song("Song 1", artist, 180);
        song1.setId("song1");
        playlistSongs.add(song1);
        
        // Input: 1 (seçilen çalma listesi) + 4 (şarkı çıkarma seçeneği) + 1 (seçilen şarkı) + 0 (bitir)
        Scanner inputScanner = createScannerWithInput("1\n4\n1\n0\n");
        
        // Mock ayarları
        when(mockService.getAllPlaylists()).thenReturn(playlists);
        when(mockService.getSongsInPlaylist("1")).thenReturn(playlistSongs).thenReturn(Collections.emptyList());
        when(mockService.removeSongFromPlaylist("song1", "1")).thenReturn(true);
        
        // Test için UI oluşturma
        PlaylistUI ui = new PlaylistUI(mockService, inputScanner);
        
        // Metot çağrısı
        ui.editPlaylist();
        
        // Doğrulama
        String output = outputStream.toString();
        assertTrue(output.contains("Removed: Song 1"));
        assertTrue(output.contains("No more songs in this playlist"));
        verify(mockService).removeSongFromPlaylist("song1", "1");
    }

    @Test
    public void testAddPlaylistSuccess() {
        // Test verileri
        Playlist playlist = new Playlist("New Playlist", "New Description");
        
        // Mock ayarları
        when(mockService.addPlaylist(playlist)).thenReturn(true);
        
        // Metot çağrısı
        boolean result = playlistUI.addPlaylist(playlist);
        
        // Doğrulama
        assertTrue(result);
        verify(mockService).addPlaylist(playlist);
    }

    @Test
    public void testAddPlaylistNull() {
        // Metot çağrısı
        boolean result = playlistUI.addPlaylist(null);
        
        // Doğrulama
        assertFalse(result);
        verify(mockService, never()).addPlaylist(any());
    }

    @Test
    public void testUpdatePlaylistSuccess() {
        // Test verileri
        Playlist playlist = new Playlist("Updated Playlist", "Updated Description");
        
        // Mock ayarları
        when(mockService.updatePlaylist(playlist)).thenReturn(true);
        
        // Metot çağrısı
        boolean result = playlistUI.updatePlaylist(playlist);
        
        // Doğrulama
        assertTrue(result);
        verify(mockService).updatePlaylist(playlist);
    }

    @Test
    public void testUpdatePlaylistNull() {
        // Metot çağrısı
        boolean result = playlistUI.updatePlaylist(null);
        
        // Doğrulama
        assertFalse(result);
        verify(mockService, never()).updatePlaylist(any());
    }
} 