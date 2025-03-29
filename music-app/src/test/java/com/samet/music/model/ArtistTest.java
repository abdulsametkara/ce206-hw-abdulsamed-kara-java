package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.List;

/**
 * @class ArtistTest
 * @brief Artist sınıfı için test sınıfı
 */
public class ArtistTest {

    private Artist artist;
    private Album album1;
    private Album album2;

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Test için nesneleri oluştur
        artist = new Artist("Test Artist", "Test Biography");
        // Album constructor'ı otomatik olarak Artist'e albümü ekleyecek
        album1 = new Album("Test Album 1", artist, 2021);
        album2 = new Album("Test Album 2", artist, 2023);
    }

    /**
     * @brief Sadece isim parametreli constructor'ı test eder
     */
    @Test
    public void testConstructorWithNameOnly() {
        // Arrange & Act
        Artist nameOnlyArtist = new Artist("Name Only Artist");

        // Assert
        assertEquals("Sanatçı adı doğru ayarlanmalı", "Name Only Artist", nameOnlyArtist.getName());
        assertEquals("Biyografi boş olmalı", "", nameOnlyArtist.getBiography());
        assertEquals("Albüm listesi boş olarak başlatılmalı", 0, nameOnlyArtist.getAlbums().size());
    }

    /**
     * @brief İsim ve biyografi parametreli constructor'ı test eder
     */
    @Test
    public void testConstructorWithNameAndBiography() {
        // Assert
        assertEquals("Sanatçı adı doğru ayarlanmalı", "Test Artist", artist.getName());
        assertEquals("Biyografi doğru ayarlanmalı", "Test Biography", artist.getBiography());

        // setUp'ta iki albüm oluşturduğumuz için, bu albümler artist'e eklenmiş olmalı
        assertEquals("Sanatçı 2 albüm içermeli", 2, artist.getAlbums().size());
        assertTrue("Sanatçı album1'i içermeli", artist.getAlbums().contains(album1));
        assertTrue("Sanatçı album2'yi içermeli", artist.getAlbums().contains(album2));
    }

    /**
     * @brief ID, isim ve biyografi parametreli constructor'ı test eder
     */
    @Test
    public void testConstructorWithIdNameAndBiography() {
        // Arrange & Act
        String customId = "custom-id-123";
        Artist idArtist = new Artist(customId, "ID Artist", "Custom ID Biography");

        // Assert
        assertEquals("Sanatçı adı doğru ayarlanmalı", "ID Artist", idArtist.getName());
        assertEquals("Biyografi doğru ayarlanmalı", "Custom ID Biography", idArtist.getBiography());
        assertEquals("Özel ID doğru ayarlanmalı", customId, idArtist.getId());
        assertEquals("Albüm listesi boş olarak başlatılmalı", 0, idArtist.getAlbums().size());
    }

    /**
     * @brief setBiography metodunu test eder
     */
    @Test
    public void testSetBiography() {
        // Act
        artist.setBiography("Updated Biography");

        // Assert
        assertEquals("Biyografi güncellenmiş olmalı", "Updated Biography", artist.getBiography());
    }

    /**
     * @brief addAlbum metodunu test eder
     */
    @Test
    public void testAddAlbum() {
        // Arrange
        Artist newArtist = new Artist("New Artist");
        Album newAlbum = new Album("New Album", null, 2022);

        // Act
        newArtist.addAlbum(newAlbum);

        // Assert
        assertEquals("Sanatçı 1 albüm içermeli", 1, newArtist.getAlbums().size());
        assertTrue("Sanatçı yeni albümü içermeli", newArtist.getAlbums().contains(newAlbum));
    }

    /**
     * @brief addAlbum metodunu aynı albümü iki kez ekleyerek test eder
     */
    @Test
    public void testAddAlbumTwice() {
        // Arrange
        Artist newArtist = new Artist("New Artist");
        Album newAlbum = new Album("New Album", null, 2022);

        // Act
        newArtist.addAlbum(newAlbum);
        newArtist.addAlbum(newAlbum);

        // Assert
        assertEquals("Sanatçı sadece 1 albüm içermeli", 1, newArtist.getAlbums().size());
    }

    /**
     * @brief removeAlbum metodunu test eder
     */
    @Test
    public void testRemoveAlbum() {
        // Act
        artist.removeAlbum(album1);

        // Assert
        assertEquals("Sanatçı 1 albüm içermeli", 1, artist.getAlbums().size());
        assertFalse("Sanatçı album1'i içermemeli", artist.getAlbums().contains(album1));
        assertTrue("Sanatçı album2'yi içermeli", artist.getAlbums().contains(album2));
    }

    /**
     * @brief removeAlbum metodunu sanatçıya ait olmayan bir albümle test eder
     */
    @Test
    public void testRemoveNonExistingAlbum() {
        // Arrange
        Artist otherArtist = new Artist("Other Artist");
        Album otherAlbum = new Album("Other Album", otherArtist, 2020);

        // Act - Remove an album that doesn't belong to the artist
        artist.removeAlbum(otherAlbum);

        // Assert - The artist should still have its original albums
        assertEquals("Sanatçı hala 2 albüm içermeli", 2, artist.getAlbums().size());
        assertTrue("Sanatçı album1'i içermeli", artist.getAlbums().contains(album1));
        assertTrue("Sanatçı album2'yi içermeli", artist.getAlbums().contains(album2));
    }

    /**
     * @brief getAlbums metodunun kopyalama özelliğini test eder
     * (Dış değişikliklerden korunma)
     */
    @Test
    public void testGetAlbumsReturnsCopy() {
        // Arrange
        Artist newArtist = new Artist("New Artist");
        Album newAlbum = new Album("New Album", null, 2022);
        newArtist.addAlbum(newAlbum);

        // Act - Dönen listeyi değiştirmeye çalış
        List<Album> albums = newArtist.getAlbums();
        albums.clear();

        // Assert
        assertEquals("Orijinal albüm listesi değişmemeli", 1, newArtist.getAlbums().size());
        assertTrue("Orijinal albüm listesi newAlbum'ü içermeli", newArtist.getAlbums().contains(newAlbum));
    }

    /**
     * @brief getId metodunu test eder - özel ID olmadığında
     */
    @Test
    public void testGetIdWithoutOriginalId() {
        // Assert
        assertNotNull("ID null olmamalı", artist.getId());
        // BaseEntity'den gelen ID genellikle UUID formatında olacaktır
    }

    /**
     * @brief getId metodunu test eder - özel ID olduğunda
     */
    @Test
    public void testGetIdWithOriginalId() {
        // Arrange
        String customId = "db-id-456";
        artist.setOriginalId(customId);

        // Assert
        assertEquals("Özel ID dönmeli", customId, artist.getId());
    }

    /**
     * @brief setOriginalId metodunu test eder
     */
    @Test
    public void testSetOriginalId() {
        // Arrange
        String customId = "custom-original-id";

        // Act
        artist.setOriginalId(customId);

        // Assert
        assertEquals("Özel ID doğru ayarlanmalı", customId, artist.getId());
    }

    /**
     * @brief toString metodunu test eder
     */
    @Test
    public void testToString() {
        // Act
        String artistString = artist.toString();

        // Assert
        assertEquals("toString doğru formatlanmalı",
                "Test Artist (2 albums)", artistString);
    }

    /**
     * @brief equals metodunu test eder - aynı ID'ye sahip sanatçılar
     */
    @Test
    public void testEqualsWithSameId() {
        // Arrange
        String customId = "same-id";
        Artist artist1 = new Artist(customId, "Artist 1", "Bio 1");
        Artist artist2 = new Artist(customId, "Artist 2", "Bio 2");

        // Assert
        assertTrue("Aynı ID'ye sahip sanatçılar eşit olmalı", artist1.equals(artist2));
        assertTrue("equals metodu simetrik olmalı", artist2.equals(artist1));
    }

    /**
     * @brief equals metodunu test eder - farklı ID'ye sahip sanatçılar
     */
    @Test
    public void testEqualsWithDifferentId() {
        // Arrange
        Artist artist1 = new Artist("id1", "Artist 1", "Bio 1");
        Artist artist2 = new Artist("id2", "Artist 1", "Bio 1");

        // Assert
        assertFalse("Farklı ID'ye sahip sanatçılar eşit olmamalı", artist1.equals(artist2));
    }

    /**
     * @brief equals metodunu test eder - aynı nesne
     */
    @Test
    public void testEqualsWithSameObject() {
        // Assert
        assertTrue("Aynı nesne kendisine eşit olmalı", artist.equals(artist));
    }

    /**
     * @brief equals metodunu test eder - null ile
     */
    @Test
    public void testEqualsWithNull() {
        // Assert
        assertFalse("Herhangi bir nesne null'a eşit olmamalı", artist.equals(null));
    }

    /**
     * @brief equals metodunu test eder - farklı sınıftan nesne ile
     */
    @Test
    public void testEqualsWithDifferentClass() {
        // Assert
        assertFalse("Farklı sınıftan nesneler eşit olmamalı", artist.equals(new Object()));
    }

    /**
     * @brief hashCode metodunu test eder
     */
    @Test
    public void testHashCode() {
        // Arrange
        String customId = "hash-id";
        Artist artist1 = new Artist(customId, "Artist 1", "Bio 1");
        Artist artist2 = new Artist(customId, "Artist 2", "Bio 2");

        // Assert
        assertEquals("Aynı ID'ye sahip sanatçıların hashCode'ları eşit olmalı",
                artist1.hashCode(), artist2.hashCode());

        // ID değişirse hashCode da değişmeli
        artist1.setOriginalId("different-id");
        assertNotEquals("Farklı ID'ye sahip sanatçıların hashCode'ları farklı olmalı",
                artist1.hashCode(), artist2.hashCode());
    }
}