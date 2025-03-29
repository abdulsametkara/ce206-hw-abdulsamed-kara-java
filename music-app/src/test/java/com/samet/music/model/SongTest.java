package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class SongTest
 * @brief Song sınıfı için test sınıfı
 */
public class SongTest {

    private Song song;
    private Artist artist;
    private Album album;

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Test için nesneleri oluştur
        artist = new Artist("Test Artist", "Test Biography");
        song = new Song("Test Song", artist, 240); // 4 dakika
        album = new Album("Test Album", artist, 2023);
    }

    /**
     * @brief Constructor'ı test eder
     */
    @Test
    public void testConstructor() {
        // Assert
        assertEquals("Şarkı adı doğru ayarlanmalı", "Test Song", song.getName());
        assertEquals("Sanatçı doğru ayarlanmalı", artist, song.getArtist());
        assertEquals("Süre doğru ayarlanmalı", 240, song.getDuration());
        assertEquals("Genre varsayılan değeri 'Unknown' olmalı", "Unknown", song.getGenre());
        assertNull("Album başlangıçta null olmalı", song.getAlbum());
    }

    /**
     * @brief setAlbum metodunu test eder
     */
    @Test
    public void testSetAlbum() {
        // Act
        song.setAlbum(album);

        // Assert
        assertEquals("Album doğru ayarlanmalı", album, song.getAlbum());
        assertTrue("Album'ün şarkıları arasında bu şarkı olmalı", album.getSongs().contains(song));
    }

    /**
     * @brief setAlbum metodunu null ile test eder
     */
    @Test
    public void testSetAlbumNull() {
        // Arrange
        song.setAlbum(album); // Önce bir albüm atayalım

        // Act
        song.setAlbum(null);

        // Assert
        assertNull("Album null olmalı", song.getAlbum());
        assertFalse("Önceki album'ün şarkıları arasında bu şarkı olmamalı", album.getSongs().contains(song));
    }

    /**
     * @brief setAlbum metodunu farklı bir album ile test eder
     */
    @Test
    public void testSetDifferentAlbum() {
        // Arrange
        song.setAlbum(album); // İlk albümü ayarla
        Album newAlbum = new Album("New Album", artist, 2024);

        // Act
        song.setAlbum(newAlbum);

        // Assert
        assertEquals("Yeni album ayarlanmış olmalı", newAlbum, song.getAlbum());
        assertFalse("Eski album'ün şarkıları arasında bu şarkı olmamalı", album.getSongs().contains(song));
        assertTrue("Yeni album'ün şarkıları arasında bu şarkı olmalı", newAlbum.getSongs().contains(song));
    }

    /**
     * @brief setArtist metodunu test eder
     */
    @Test
    public void testSetArtist() {
        // Arrange
        Artist newArtist = new Artist("New Artist", "New Biography");

        // Act
        song.setArtist(newArtist);

        // Assert
        assertEquals("Sanatçı doğru değiştirilmeli", newArtist, song.getArtist());
    }

    /**
     * @brief setDuration metodunu test eder
     */
    @Test
    public void testSetDuration() {
        // Act
        song.setDuration(300); // 5 dakika

        // Assert
        assertEquals("Süre doğru güncellenmiş olmalı", 300, song.getDuration());
    }

    /**
     * @brief setGenre metodunu test eder
     */
    @Test
    public void testSetGenre() {
        // Act
        song.setGenre("Rock");

        // Assert
        assertEquals("Genre doğru güncellenmiş olmalı", "Rock", song.getGenre());
    }

    /**
     * @brief getFormattedDuration metodunu test eder - tam dakika değeri
     */
    @Test
    public void testGetFormattedDurationExactMinutes() {
        // Arrange (setUp'ta song 240 saniye = 4:00 olarak ayarlanmış)

        // Act & Assert
        assertEquals("Formatlanmış süre doğru olmalı", "4:00", song.getFormattedDuration());
    }

    /**
     * @brief getFormattedDuration metodunu test eder - saniyeli değer
     */
    @Test
    public void testGetFormattedDurationWithSeconds() {
        // Arrange
        song.setDuration(185); // 3 dakika 5 saniye

        // Act & Assert
        assertEquals("Formatlanmış süre doğru olmalı", "3:05", song.getFormattedDuration());
    }

    /**
     * @brief getFormattedDuration metodunu test eder - tek haneli dakika
     */
    @Test
    public void testGetFormattedDurationSingleDigitMinute() {
        // Arrange
        song.setDuration(65); // 1 dakika 5 saniye

        // Act & Assert
        assertEquals("Formatlanmış süre doğru olmalı", "1:05", song.getFormattedDuration());
    }

    /**
     * @brief toString metodunu test eder
     */
    @Test
    public void testToString() {
        // Act
        String songString = song.toString();

        // Assert
        assertEquals("toString doğru formatlanmalı",
                "Test Song - Test Artist - 4:00", songString);
    }

    /**
     * @brief toString metodunu null artist ile test eder
     */
    @Test
    public void testToStringWithNullArtist() {
        // Arrange
        Song nullArtistSong = new Song("No Artist Song", null, 180);

        // Act
        String songString = nullArtistSong.toString();

        // Assert
        assertEquals("toString null sanatçı ile doğru formatlanmalı",
                "No Artist Song - Unknown Artist - 3:00", songString);
    }
}