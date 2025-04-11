package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.List;

/**
 * @class AlbumTest
 * @brief Test class for Album class
 */
public class AlbumTest {

    private Album album;
    private Artist artist;
    private Song song1;
    private Song song2;

    /**
     * @brief Runs before each test
     */
    @Before
    public void setUp() throws Exception {
        // Create objects for testing
        artist = new Artist("Test Artist", "Test Biography");
        album = new Album("Test Album", artist, 2023);
        song1 = new Song("Test Song 1", artist, 180);
        song2 = new Song("Test Song 2", artist, 240);
    }

    /**
     * @brief Tests the constructor
     */
    @Test
    public void testConstructor() {
        // Check directly accessible properties
        assertEquals("Album name should be set correctly", "Test Album", album.getName());
        assertEquals("Artist should be set correctly", artist, album.getArtist());
        assertEquals("Release year should be set correctly", 2023, album.getReleaseYear());
        assertEquals("Genre default value should be 'Unknown'", "Unknown", album.getGenre());
        assertEquals("Song list should be initialized empty", 0, album.getSongs().size());

        // The album should be in the artist's albums
        assertTrue("New album should be in artist's albums", artist.getAlbums().contains(album));
    }

    /**
     * @brief Tests constructor with null artist
     */
    @Test
    public void testConstructorWithNullArtist() {
        // Arrange & Act
        Album nullArtistAlbum = new Album("No Artist Album", null, 2020);

        // Assert
        assertNull("Sanatçı null olmalı", nullArtistAlbum.getArtist());
        assertEquals("Album adı doğru ayarlanmalı", "No Artist Album", nullArtistAlbum.getName());
        assertEquals("Yayın yılı doğru ayarlanmalı", 2020, nullArtistAlbum.getReleaseYear());
    }

    /**
     * @brief Tests setArtist method
     */
    @Test
    public void testSetArtist() {
        // Arrange
        Artist newArtist = new Artist("New Artist", "New Biography");

        // Act
        album.setArtist(newArtist);

        // Assert
        assertEquals("Yeni sanatçı ayarlanmalı", newArtist, album.getArtist());
        assertFalse("Eski sanatçının albümleri arasında bu albüm olmamalı", artist.getAlbums().contains(album));
        assertTrue("Yeni sanatçının albümleri arasında bu albüm olmalı", newArtist.getAlbums().contains(album));
    }

    /**
     * @brief Tests setArtist method with null value
     */
    @Test
    public void testSetArtistNull() {
        // Act
        album.setArtist(null);

        // Assert
        assertNull("Sanatçı null olmalı", album.getArtist());
        assertFalse("Eski sanatçının albümleri arasında bu albüm olmamalı", artist.getAlbums().contains(album));
    }

    /**
     * @brief Tests setReleaseYear method
     */
    @Test
    public void testSetReleaseYear() {
        // Act
        album.setReleaseYear(2022);

        // Assert
        assertEquals("Yayın yılı değişmeli", 2022, album.getReleaseYear());
    }

    /**
     * @brief Tests setGenre method
     */
    @Test
    public void testSetGenre() {
        // Act
        album.setGenre("Rock");

        // Assert
        assertEquals("Genre değişmeli", "Rock", album.getGenre());
    }

    /**
     * @brief Tests addSong method
     */
    @Test
    public void testAddSong() {
        // Act
        album.addSong(song1);

        // Assert
        assertEquals("Albüm 1 şarkı içermeli", 1, album.getSongs().size());
        assertTrue("Albüm song1'i içermeli", album.getSongs().contains(song1));
        assertEquals("Song1'in albümü bu albüm olmalı", album, song1.getAlbum());
    }

    /**
     * @brief Tests addSong method with the same song twice
     */
    @Test
    public void testAddSongTwice() {
        // Act
        album.addSong(song1);
        album.addSong(song1);

        // Assert
        assertEquals("Albüm sadece 1 şarkı içermeli", 1, album.getSongs().size());
    }

    /**
     * @brief Tests removeSong method
     */
    @Test
    public void testRemoveSong() {
        // Arrange
        album.addSong(song1);
        album.addSong(song2);

        // Act
        album.removeSong(song1);

        // Assert
        assertEquals("Albüm 1 şarkı içermeli", 1, album.getSongs().size());
        assertFalse("Albüm song1'i içermemeli", album.getSongs().contains(song1));
        assertTrue("Albüm song2'yi içermeli", album.getSongs().contains(song2));
        assertNull("Song1'in albümü null olmalı", song1.getAlbum());
        assertEquals("Song2'nin albümü bu albüm olmalı", album, song2.getAlbum());
    }

    /**
     * @brief Tests removeSong method with a song that is not in the album
     */
    @Test
    public void testRemoveNonExistingSong() {
        // Arrange
        album.addSong(song1);

        // Act
        album.removeSong(song2);

        // Assert
        assertEquals("Albüm hala 1 şarkı içermeli", 1, album.getSongs().size());
        assertTrue("Albüm song1'i içermeli", album.getSongs().contains(song1));
    }

    /**
     * @brief Tests getSongs method's copy feature
     * (Protection from external modifications)
     */
    @Test
    public void testGetSongsReturnsCopy() {
        // Arrange
        album.addSong(song1);

        // Act - Try to modify the returned list
        List<Song> songs = album.getSongs();
        songs.add(song2);

        // Assert
        assertEquals("Original song list should not change", 1, album.getSongs().size());
        assertFalse("Original song list should not contain song2", album.getSongs().contains(song2));
    }

    /**
     * @brief Tests toString method
     */
    @Test
    public void testToString() {
        // Act
        String albumString = album.toString();

        // Assert
        assertEquals("toString should be formatted correctly",
                "Test Album (2023) by Test Artist", albumString);
    }

    /**
     * @brief Tests toString method with null artist
     */
    @Test
    public void testToStringWithNullArtist() {
        // Arrange
        Album nullArtistAlbum = new Album("No Artist Album", null, 2020);

        // Act
        String albumString = nullArtistAlbum.toString();

        // Assert
        assertEquals("toString should be formatted correctly with null artist",
                "No Artist Album (2020) by Unknown Artist", albumString);
    }
}