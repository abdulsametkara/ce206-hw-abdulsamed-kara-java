package com.samet.music;

import com.samet.music.main.Music;
import com.samet.music.model.Artist;
import com.samet.music.repository.ArtistCollection;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.util.DatabaseUtil;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class DenemeTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private MusicCollectionService service;

    @Before
    public void setUp() {
        // Hazırlık işlemleri
        System.setOut(new PrintStream(outContent));
        DatabaseUtil.setShouldResetDatabase(true); // Test için veritabanını sıfırla
        DatabaseUtil.initializeDatabase();
        service = MusicCollectionService.getInstance();
    }

    @After
    public void restoreSystemStreams() {
        // Test sonrası temizlik
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    public void testAddArtist() {
        // Arrange
        String input = "Artist Name\nThis is a test biography for the artist.\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream); // Simulated user input

        Music music = new Music(new Scanner(System.in), System.out);
        music.isTestMode = true; // Enable test mode

        // Act
        // Add an artist using MusicCollectionUI
        boolean result = service.addArtist("Artist Name", "This is a test biography for the artist.");

        // Assert
        assertTrue("Artist was not added successfully", result);

        // Verify the added artist from the database
        List<Artist> artists = service.getAllArtists();
        boolean artistFound = false;

        for (Artist artist : artists) {
            if ("Artist Name".equals(artist.getName())) {
                artistFound = true;
                assertEquals("Artist biography doesn't match",
                        "This is a test biography for the artist.", artist.getBiography());
                break;
            }
        }

        assertTrue("Added artist was not found", artistFound);
        String output = outContent.toString();
        assertTrue("Expected output not found", output.contains("Artist Name"));
    }

    @Test
    public void testAddArtistWithEmptyName() {
        // Arrange
        String input = "\nSome biography\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        // Act
        boolean result = service.addArtist("", "Some biography");

        // Assert
        assertFalse("Empty artist name should not be accepted", result);
    }

    @Test
    public void testSearchArtistByName() {
        // Arrange
        service.addArtist("Test Artist", "Biography");
        service.addArtist("Another Artist", "Biography");
        service.addArtist("Test Singer", "Biography");

        // Act
        List<Artist> results = service.searchArtistsByName("Test");

        // Assert
        assertEquals("Search should return 2 artists", 2, results.size());
        boolean foundArtist1 = false;
        boolean foundArtist2 = false;

        for (Artist artist : results) {
            if ("Test Artist".equals(artist.getName())) foundArtist1 = true;
            if ("Test Singer".equals(artist.getName())) foundArtist2 = true;
        }

        assertTrue("'Test Artist' should be found in search results", foundArtist1);
        assertTrue("'Test Singer' should be found in search results", foundArtist2);
    }

    @Test
    public void testRemoveArtist() {
        // Arrange
        service.addArtist("Artist To Remove", "Biography");

        List<Artist> artists = service.getAllArtists();
        String artistId = null;

        for (Artist artist : artists) {
            if ("Artist To Remove".equals(artist.getName())) {
                artistId = artist.getId();
                break;
            }
        }

        assertNotNull("Artist ID not found", artistId);

        // Act
        boolean result = service.removeArtist(artistId);

        // Assert
        assertTrue("Artist was not removed successfully", result);

        // Verify that the artist is no longer in the database
        artists = service.getAllArtists();
        boolean artistFound = false;

        for (Artist artist : artists) {
            if ("Artist To Remove".equals(artist.getName())) {
                artistFound = true;
                break;
            }
        }

        assertFalse("Deleted artist is still in database", artistFound);
    }

    @Test
    public void testEditArtistName() {
        // Arrange
        String input = "1\nUpdated Artist Name\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        service.addArtist("Original Artist Name", "Biography");

        List<Artist> artists = service.getAllArtists();
        Artist targetArtist = null;

        for (Artist artist : artists) {
            if ("Original Artist Name".equals(artist.getName())) {
                targetArtist = artist;
                break;
            }
        }

        assertNotNull("Artist to edit not found", targetArtist);

        // Act
        targetArtist.setName("Updated Artist Name");
        ArtistCollection.getInstance().add(targetArtist); // Apply the update

        // Assert
        artists = service.getAllArtists();
        boolean updatedNameFound = false;

        for (Artist artist : artists) {
            if (artist.getId().equals(targetArtist.getId())) {
                assertEquals("Artist name was not updated", "Updated Artist Name", artist.getName());
                updatedNameFound = true;
                break;
            }
        }

        assertTrue("Updated artist name not found", updatedNameFound);
    }
}