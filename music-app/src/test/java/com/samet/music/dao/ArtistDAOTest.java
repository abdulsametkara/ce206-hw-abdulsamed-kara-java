package com.samet.music.dao;

import static org.junit.Assert.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samet.music.model.Artist;
import com.samet.music.util.DatabaseUtil;

/**
 * @class ArtistDAOTest
 * @brief ArtistDAO sınıfı için test sınıfı
 */
public class ArtistDAOTest {

    private ArtistDAO artistDAO;

    /**
     * @brief Tüm testlerden önce bir kez çalıştırılır
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Veritabanını test modunda başlat
        DatabaseUtil.setShouldResetDatabase(true);
        DatabaseUtil.initializeDatabase();
    }

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Her test öncesi yeni DAO örneği oluştur
        artistDAO = new ArtistDAO();

        // Testler için veritabanını temizle
        cleanupDatabase();
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Veritabanını temizle
        cleanupDatabase();
    }

    /**
     * @brief Veritabanındaki artists tablosunu temizler
     */
    private void cleanupDatabase() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM artists")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief insert metodunu test eder - yeni sanatçı ekleme
     */
    @Test
    public void testInsertNewArtist() throws Exception {
        // Arrange - Test için sanatçı oluştur
        Artist artist = new Artist("Test Artist", "Test Biography");

        // Act - Sanatçıyı veritabanına ekle
        artistDAO.insert(artist);

        // Assert - Sanatçının veritabanına eklenip eklenmediğini kontrol et
        boolean found = false;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE id = ?")) {

            stmt.setString(1, artist.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    found = true;
                    assertEquals("Sanatçı adı eşleşmiyor", "Test Artist", rs.getString("name"));
                    assertEquals("Biyografi eşleşmiyor", "Test Biography", rs.getString("biography"));
                }
            }
        }

        assertTrue("Sanatçı veritabanına eklenmedi", found);
    }

    /**
     * @brief insert metodunu test eder - mevcut sanatçı güncelleme durumu
     */
    @Test
    public void testInsertExistingArtist() throws Exception {
        // Arrange - Test için sanatçı oluştur ve ekle
        Artist artist = new Artist("Test Artist", "Original Biography");
        artistDAO.insert(artist);

        // Aynı ID ile güncellenen sanatçı bilgileri
        String originalId = artist.getId();
        Artist updatedArtist = new Artist(originalId, "Test Artist Updated", "Updated Biography");

        // Act - Sanatçıyı veritabanına ekle (Bu durumda güncelleme işlemi gerçekleşmeli)
        artistDAO.insert(updatedArtist);

        // Assert - Sanatçı bilgilerinin güncellenip güncellenmediğini kontrol et
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE id = ?")) {

            stmt.setString(1, originalId);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Sanatçı veritabanında bulunamadı", rs.next());
                assertEquals("Sanatçı adı güncellenmemiş", "Test Artist Updated", rs.getString("name"));
                assertEquals("Biyografi güncellenmemiş", "Updated Biography", rs.getString("biography"));
            }
        }
    }

    /**
     * @brief insert metodunun özel ID'li sanatçıları doğru şekilde ekleyip eklemediğini test eder
     */
    @Test
    public void testInsertWithCustomId() throws Exception {
        // Arrange - Özel ID ile test sanatçısı oluştur
        String customId = "custom-id-123";
        Artist artist = new Artist(customId, "Custom ID Artist", "Custom ID Biography");

        // Act - Sanatçıyı veritabanına ekle
        artistDAO.insert(artist);

        // Assert - Özel ID ile sanatçının veritabanına eklenip eklenmediğini kontrol et
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE id = ?")) {

            stmt.setString(1, customId);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Özel ID'li sanatçı veritabanında bulunamadı", rs.next());
                assertEquals("Sanatçı adı eşleşmiyor", "Custom ID Artist", rs.getString("name"));
                assertEquals("Biyografi eşleşmiyor", "Custom ID Biography", rs.getString("biography"));
            }
        }

    }
    /**
     * @brief Var olan bir sanatçıyı ID ile getirme testini yapar
     */
    @Test
    public void testGetByIdExistingArtist() {
        // Arrange - Test sanatçısı oluştur ve veritabanına ekle
        Artist originalArtist = new Artist("Test Artist", "Test Biography");
        artistDAO.insert(originalArtist);
        String artistId = originalArtist.getId();

        // Act - ID ile sanatçıyı getir
        Artist retrievedArtist = artistDAO.getById(artistId);

        // Assert - Sonuçları kontrol et
        assertNotNull("Sanatçı null olmamalı", retrievedArtist);
        assertEquals("Sanatçı ID'si eşleşmiyor", artistId, retrievedArtist.getId());
        assertEquals("Sanatçı adı eşleşmiyor", "Test Artist", retrievedArtist.getName());
        assertEquals("Sanatçı biyografisi eşleşmiyor", "Test Biography", retrievedArtist.getBiography());
    }

    /**
     * @brief Var olmayan bir ID için getById metodunu test eder
     */
    @Test
    public void testGetByIdNonExistingArtist() {
        // Act - Var olmayan bir ID ile sanatçı getirmeye çalış
        Artist retrievedArtist = artistDAO.getById("non_existing_id");

        // Assert - Sonuçları kontrol et
        assertNull("Var olmayan ID için null dönmeli", retrievedArtist);
    }

    /**
     * @brief Birden fazla kez aynı ID ile getById çağrısının önbellek kontrolünü test eder
     */
    @Test
    public void testGetByIdCaching() {
        // Arrange - Test sanatçısı oluştur ve veritabanına ekle
        Artist originalArtist = new Artist("Test Artist", "Test Biography");
        artistDAO.insert(originalArtist);
        String artistId = originalArtist.getId();

        // Act - Aynı ID ile iki kez getById çağrısı
        Artist firstRetrieval = artistDAO.getById(artistId);
        Artist secondRetrieval = artistDAO.getById(artistId);

        // Assert - Sonuçları kontrol et
        assertNotNull("İlk getirme null olmamalı", firstRetrieval);
        assertNotNull("İkinci getirme null olmamalı", secondRetrieval);
        assertSame("Aynı nesne referansı döndürülmeli (önbellek)", firstRetrieval, secondRetrieval);
    }

    /**
     * @brief Null ID için getById metodunu test eder
     */
    @Test
    public void testGetByIdWithNullId() {
        // Act & Assert - Null ID ile getById çağrısı
        Artist retrievedArtist = artistDAO.getById(null);
        assertNull("Null ID için null dönmeli", retrievedArtist);
    }

    /**
     * @brief Boş string ID için getById metodunu test eder
     */
    @Test
    public void testGetByIdWithEmptyStringId() {
        // Act & Assert - Boş string ID ile getById çağrısı
        Artist retrievedArtist = artistDAO.getById("");
        assertNull("Boş ID için null dönmeli", retrievedArtist);
    }
}
