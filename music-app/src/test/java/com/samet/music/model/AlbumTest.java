package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.List;

/**
 * @class AlbumTest
 * @brief Album sınıfı için test sınıfı
 */
public class AlbumTest {

    private Album album;
    private Artist artist;
    private Song song1;
    private Song song2;

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Test için nesneleri oluştur
        artist = new Artist("Test Artist", "Test Biography");
        album = new Album("Test Album", artist, 2023);
        song1 = new Song("Test Song 1", artist, 180);
        song2 = new Song("Test Song 2", artist, 240);
    }

    /**
     * @brief Constructor'ı test eder
     */
    @Test
    public void testConstructor() {
        // Doğrudan erişilebilen özellikleri kontrol et
        assertEquals("Album adı doğru ayarlanmalı", "Test Album", album.getName());
        assertEquals("Sanatçı doğru ayarlanmalı", artist, album.getArtist());
        assertEquals("Yayın yılı doğru ayarlanmalı", 2023, album.getReleaseYear());
        assertEquals("Genre varsayılan değeri 'Unknown' olmalı", "Unknown", album.getGenre());
        assertEquals("Şarkı listesi boş olarak başlatılmalı", 0, album.getSongs().size());

        // Sanatçının albümleri arasında bu albüm olmalı
        assertTrue("Sanatçının albümleri arasında yeni albüm olmalı", artist.getAlbums().contains(album));
    }

    /**
     * @brief Null sanatçı ile constructor'ı test eder
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
     * @brief setArtist metodunu test eder
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
     * @brief setArtist metodunu null değer ile test eder
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
     * @brief setReleaseYear metodunu test eder
     */
    @Test
    public void testSetReleaseYear() {
        // Act
        album.setReleaseYear(2022);

        // Assert
        assertEquals("Yayın yılı değişmeli", 2022, album.getReleaseYear());
    }

    /**
     * @brief setGenre metodunu test eder
     */
    @Test
    public void testSetGenre() {
        // Act
        album.setGenre("Rock");

        // Assert
        assertEquals("Genre değişmeli", "Rock", album.getGenre());
    }

    /**
     * @brief addSong metodunu test eder
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
     * @brief addSong metodunu aynı şarkıyı iki kez ekleyerek test eder
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
     * @brief removeSong metodunu test eder
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
     * @brief removeSong metodunu albümde olmayan bir şarkı ile test eder
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
     * @brief getSongs metodunun kopyalama özelliğini test eder
     * (Dış değişikliklerden korunma)
     */
    @Test
    public void testGetSongsReturnsCopy() {
        // Arrange
        album.addSong(song1);

        // Act - Dönen listeyi değiştirmeye çalış
        List<Song> songs = album.getSongs();
        songs.add(song2);

        // Assert
        assertEquals("Orijinal şarkı listesi değişmemeli", 1, album.getSongs().size());
        assertFalse("Orijinal şarkı listesi song2'yi içermemeli", album.getSongs().contains(song2));
    }

    /**
     * @brief toString metodunu test eder
     */
    @Test
    public void testToString() {
        // Act
        String albumString = album.toString();

        // Assert
        assertEquals("toString doğru formatlanmalı",
                "Test Album (2023) by Test Artist", albumString);
    }

    /**
     * @brief toString metodunu null sanatçı ile test eder
     */
    @Test
    public void testToStringWithNullArtist() {
        // Arrange
        Album nullArtistAlbum = new Album("No Artist Album", null, 2020);

        // Act
        String albumString = nullArtistAlbum.toString();

        // Assert
        assertEquals("toString null sanatçı ile doğru formatlanmalı",
                "No Artist Album (2020) by Unknown Artist", albumString);
    }
}