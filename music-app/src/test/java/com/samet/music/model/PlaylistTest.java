package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.List;

/**
 * @class PlaylistTest
 * @brief Playlist sınıfı için test sınıfı
 */
public class PlaylistTest {

    private Playlist playlist;
    private Song song1;
    private Song song2;
    private Song song3;
    private Artist artist;

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Test için nesneleri oluştur
        artist = new Artist("Test Artist", "Test Biography");
        playlist = new Playlist("Test Playlist", "Test Description");
        song1 = new Song("Test Song 1", artist, 180); // 3 dakika
        song2 = new Song("Test Song 2", artist, 240); // 4 dakika
        song3 = new Song("Test Song 3", artist, 300); // 5 dakika
    }

    /**
     * @brief Sadece isim parametreli constructor'ı test eder
     */
    @Test
    public void testConstructorWithNameOnly() {
        // Arrange & Act
        Playlist nameOnlyPlaylist = new Playlist("Name Only Playlist");

        // Assert
        assertEquals("Playlist adı doğru ayarlanmalı", "Name Only Playlist", nameOnlyPlaylist.getName());
        assertEquals("Açıklama boş olmalı", "", nameOnlyPlaylist.getDescription());
        assertEquals("Şarkı listesi boş olarak başlatılmalı", 0, nameOnlyPlaylist.getSongs().size());
    }

    /**
     * @brief İsim ve açıklama parametreli constructor'ı test eder
     */
    @Test
    public void testConstructorWithNameAndDescription() {
        // Assert
        assertEquals("Playlist adı doğru ayarlanmalı", "Test Playlist", playlist.getName());
        assertEquals("Açıklama doğru ayarlanmalı", "Test Description", playlist.getDescription());
        assertEquals("Şarkı listesi boş olarak başlatılmalı", 0, playlist.getSongs().size());
    }

    /**
     * @brief setDescription metodunu test eder
     */
    @Test
    public void testSetDescription() {
        // Act
        playlist.setDescription("Updated Description");

        // Assert
        assertEquals("Açıklama güncellenmiş olmalı", "Updated Description", playlist.getDescription());
    }

    /**
     * @brief addSong metodunu test eder
     */
    @Test
    public void testAddSong() {
        // Act
        playlist.addSong(song1);

        // Assert
        assertEquals("Playlist 1 şarkı içermeli", 1, playlist.getSongs().size());
        assertTrue("Playlist song1'i içermeli", playlist.getSongs().contains(song1));
    }

    /**
     * @brief addSong metodunu null ile test eder
     */
    @Test
    public void testAddNullSong() {
        // Act
        playlist.addSong(null);

        // Assert
        assertEquals("Playlist boş olmalı", 0, playlist.getSongs().size());
    }

    /**
     * @brief addSong metodunu aynı şarkıyı iki kez ekleyerek test eder
     */
    @Test
    public void testAddSongTwice() {
        // Act
        playlist.addSong(song1);
        playlist.addSong(song1);

        // Assert
        assertEquals("Playlist sadece 1 şarkı içermeli", 1, playlist.getSongs().size());
    }

    /**
     * @brief removeSong metodunu test eder
     */
    @Test
    public void testRemoveSong() {
        // Arrange
        playlist.addSong(song1);
        playlist.addSong(song2);

        // Act
        playlist.removeSong(song1);

        // Assert
        assertEquals("Playlist 1 şarkı içermeli", 1, playlist.getSongs().size());
        assertFalse("Playlist song1'i içermemeli", playlist.getSongs().contains(song1));
        assertTrue("Playlist song2'yi içermeli", playlist.getSongs().contains(song2));
    }

    /**
     * @brief removeSong metodunu playlist'te olmayan bir şarkı ile test eder
     */
    @Test
    public void testRemoveNonExistingSong() {
        // Arrange
        playlist.addSong(song1);

        // Act
        playlist.removeSong(song3); // song3 playlist'te yok

        // Assert
        assertEquals("Playlist hala 1 şarkı içermeli", 1, playlist.getSongs().size());
        assertTrue("Playlist song1'i içermeli", playlist.getSongs().contains(song1));
    }

    /**
     * @brief getSongs metodunun kopyalama özelliğini test eder
     * (Dış değişikliklerden korunma)
     */
    @Test
    public void testGetSongsReturnsCopy() {
        // Arrange
        playlist.addSong(song1);

        // Act - Dönen listeyi değiştirmeye çalış
        List<Song> songs = playlist.getSongs();
        songs.add(song2);

        // Assert
        assertEquals("Orijinal şarkı listesi değişmemeli", 1, playlist.getSongs().size());
        assertFalse("Orijinal şarkı listesi song2'yi içermemeli", playlist.getSongs().contains(song2));
    }

    /**
     * @brief getSongCount metodunu test eder
     */
    @Test
    public void testGetSongCount() {
        // Arrange
        playlist.addSong(song1);
        playlist.addSong(song2);

        // Act & Assert
        assertEquals("Şarkı sayısı 2 olmalı", 2, playlist.getSongCount());

        // Bir şarkı daha ekle
        playlist.addSong(song3);
        assertEquals("Şarkı sayısı 3 olmalı", 3, playlist.getSongCount());

        // Bir şarkı çıkar
        playlist.removeSong(song2);
        assertEquals("Şarkı sayısı 2 olmalı", 2, playlist.getSongCount());
    }

    /**
     * @brief getTotalDuration metodunu test eder
     */
    @Test
    public void testGetTotalDuration() {
        // Arrange
        playlist.addSong(song1); // 180 saniye
        playlist.addSong(song2); // 240 saniye

        // Act & Assert
        assertEquals("Toplam süre doğru hesaplanmalı", 420, playlist.getTotalDuration()); // 180 + 240 = 420

        // Bir şarkı daha ekle
        playlist.addSong(song3); // 300 saniye
        assertEquals("Toplam süre doğru güncellenmiş olmalı", 720, playlist.getTotalDuration()); // 180 + 240 + 300 = 720
    }

    /**
     * @brief getFormattedTotalDuration metodunu test eder - dakika ve saniye formatı
     */
    @Test
    public void testGetFormattedTotalDurationMinutesAndSeconds() {
        // Arrange
        playlist.addSong(song1); // 180 saniye = 3:00

        // Act & Assert
        assertEquals("Formatlı süre doğru olmalı", "3:00", playlist.getFormattedTotalDuration());

        // Düzensiz süreli şarkı ekle (3:45)
        Song irregularSong = new Song("Irregular Song", artist, 225); // 3 dakika 45 saniye
        playlist.addSong(irregularSong);
        assertEquals("Formatlı süre doğru olmalı", "6:45", playlist.getFormattedTotalDuration()); // 3:00 + 3:45 = 6:45
    }

    /**
     * @brief getFormattedTotalDuration metodunu test eder - saat, dakika ve saniye formatı
     */
    @Test
    public void testGetFormattedTotalDurationHoursMinutesSeconds() {
        // Arrange - 3600 saniye = 1 saat
        for (int i = 0; i < 10; i++) {
            playlist.addSong(new Song("Long Song " + i, artist, 360)); // Her biri 6 dakika, 10 şarkı = 60 dakika = 1 saat
        }

        // Act & Assert
        assertEquals("Formatlı süre saat formatında olmalı", "1:00:00", playlist.getFormattedTotalDuration());

        // Ekstra şarkı ekle
        playlist.addSong(new Song("Extra Song", artist, 90)); // 1:30
        assertEquals("Formatlı süre doğru olmalı", "1:01:30", playlist.getFormattedTotalDuration());
    }

    /**
     * @brief toString metodunu test eder
     */
    @Test
    public void testToString() {
        // Arrange
        playlist.addSong(song1); // 180 saniye
        playlist.addSong(song2); // 240 saniye
        // Toplam: 420 saniye = 7:00

        // Act
        String playlistString = playlist.toString();

        // Assert
        assertEquals("toString doğru formatlanmalı",
                "Test Playlist (2 songs, 7:00)", playlistString);
    }
}