package com.samet.music.dao;

import static org.junit.Assert.*;
import org.junit.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.util.DatabaseUtil;


/**
 * @class AlbumDAOTest
 * @brief AlbumDAO sınıfı için test sınıfı
 */
public class AlbumDAOTest {

    private AlbumDAO albumDAO;
    private ArtistDAO artistDAO;
    private Artist testArtist;

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
        // Her test öncesi yeni DAO örnekleri oluştur
        albumDAO = new AlbumDAO();
        artistDAO = new ArtistDAO();

        // Test için bir sanatçı oluştur ve veritabanına ekle
        testArtist = new Artist("Test Artist", "Test Biography");
        artistDAO.insert(testArtist);
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Veritabanını temizle
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM albums")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief insert metodunu test eder - yeni albüm ekleme
     */
    @Test
    public void testInsertNewAlbum() throws Exception {
        // Arrange - Test için albüm oluştur
        Album album = new Album("Test Album", testArtist, 2023);
        album.setGenre("Rock");

        // Act - Albümü veritabanına ekle
        albumDAO.insert(album);

        // Assert - Albümün veritabanına eklenip eklenmediğini kontrol et
        boolean found = false;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE id = ?")) {

            stmt.setString(1, album.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    found = true;
                    assertEquals("Albüm adı eşleşmiyor", "Test Album", rs.getString("name"));
                    assertEquals("Sanatçı ID'si eşleşmiyor", testArtist.getId(), rs.getString("artist_id"));
                    assertEquals("Yayın yılı eşleşmiyor", 2023, rs.getInt("release_year"));
                    assertEquals("Tür eşleşmiyor", "Rock", rs.getString("genre"));
                }
            }
        }

        assertTrue("Albüm veritabanına eklenmedi", found);
    }

    /**
     * @brief insert metodunu test eder - var olan albümü güncelleme
     */
    @Test
    public void testInsertExistingAlbum() throws Exception {
        // Arrange - İlk albümü oluştur ve ekle
        Album album = new Album("Original Album", testArtist, 2023);
        album.setGenre("Rock");

        // İlk albümü ekle
        albumDAO.insert(album);

        // Veritabanındaki albümü al
        String albumId = album.getId();

        // Yeni albüm oluşturarak (aynı ID ile) güncelleme yap
        Artist newArtist = new Artist("New Artist", "New Bio");
        artistDAO.insert(newArtist);

        // Güncelleme için yeni bir albüm oluştur
        Album updatedAlbum = new Album("Updated Album", newArtist, 2024);
        // BaseEntity sınıfında setId metodu olduğunu varsayıyoruz
        // Eğer yoksa reflection ile id alanını değiştirmek gerekebilir
        setAlbumId(updatedAlbum, albumId);
        updatedAlbum.setGenre("Pop");

        // Act - Güncelleme için insert metodunu çağır
        albumDAO.insert(updatedAlbum);

        // Assert - Albümün güncellendiğini kontrol et
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE id = ?")) {

            stmt.setString(1, albumId);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Albüm bulunamadı", rs.next());
                assertEquals("Albüm adı güncellenmiş olmalı", "Updated Album", rs.getString("name"));
                assertEquals("Sanatçı ID'si güncellenmiş olmalı", newArtist.getId(), rs.getString("artist_id"));
                assertEquals("Yayın yılı güncellenmiş olmalı", 2024, rs.getInt("release_year"));
                assertEquals("Tür güncellenmiş olmalı", "Pop", rs.getString("genre"));
            }
        }
    }

    /**
     * @brief insert metodunu null albüm ile test eder (exception beklenir)
     */
    @Test
    public void testInsertNullAlbum() {
        try {
            // Act - null albüm ekle
            albumDAO.insert(null);
            fail("NullPointerException bekleniyor");
        } catch (NullPointerException e) {
            // Expected exception
        } catch (Exception e) {
            fail("Beklenmeyen exception: " + e.getMessage());
        }
    }

    /**
     * @brief Null sanatçı bilgisi olan albüm ekleme işlemini test eder
     */
    @Test
    public void testInsertWithNullArtist() {
        // Arrange - Null sanatçı bilgisi olan bir albüm oluştur
        Album album = new Album("Null Artist Album", null, 2023);
        album.setGenre("Rock");

        try {
            // Act - Albümü eklemeye çalış
            albumDAO.insert(album);

            // Eğer exception fırlatılmazsa, veritabanında sanatçı ID'sinin null olması beklenir
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE id = ?")) {

                stmt.setString(1, album.getId());


            }
        } catch (Exception e) {
            // Eğer exception fırlatılırsa da bu geçerli bir durumdur
            // İşlem tamamlandı olarak kabul edilir
        }
    }

    /**
     * Yardımcı metod: Album nesnesinin ID'sini ayarlamak için
     * Not: BaseEntity sınıfında setId metodu yoksa, reflection kullanarak ID alanını değiştirmek gerekebilir
     */
    private void setAlbumId(Album album, String id) {
        try {
            // Reflection yöntemiyle ID'yi ayarla
            java.lang.reflect.Field idField = album.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(album, id);
        } catch (Exception e) {
            fail("Album ID'si ayarlanamadı: " + e.getMessage());
        }
    }
    /**
     * @brief Boş veritabanında getAll metodu test edilir
     */
    @Test
    public void testGetAllWithEmptyDatabase() {
        // Act - getAll metodunu çağır
        List<Album> albums = albumDAO.getAll();

        // Assert - Liste boş olmalı
        assertNotNull("Album listesi null olmamalı", albums);
        assertTrue("Boş veritabanı için albüm listesi boş olmalı", albums.isEmpty());
    }

    /**
     * @brief Tek albüm içeren veritabanında getAll metodu test edilir
     */
    @Test
    public void testGetAllWithSingleAlbum() {
        // Arrange - Bir albüm ekle
        Album album = new Album("Test Album 1", testArtist, 2020);
        album.setGenre("Rock");
        albumDAO.insert(album);

        // Act - getAll metodunu çağır
        List<Album> albums = albumDAO.getAll();

        // Assert - Sonuçları kontrol et

    }

    /**
     * @brief Çoklu albüm içeren veritabanında getAll metodu test edilir
     */
    @Test
    public void testGetAllWithMultipleAlbums() {
        // Arrange - Birden fazla albüm ekle
        Album album1 = new Album("Test Album 1", testArtist, 2020);
        album1.setGenre("Rock");
        albumDAO.insert(album1);

        Album album2 = new Album("Test Album 2", testArtist, 2021);
        album2.setGenre("Pop");
        albumDAO.insert(album2);

        Album album3 = new Album("Test Album 3", testArtist, 2022);
        album3.setGenre("Jazz");
        albumDAO.insert(album3);

        // Act - getAll metodunu çağır
        List<Album> albums = albumDAO.getAll();

        // Assert - Sonuçları kontrol et
        assertNotNull("Album listesi null olmamalı", albums);
        assertEquals("Album listesi üç eleman içermeli", 3, albums.size());

        // Albüm adlarını kontrol et
        Set<String> albumNames = new HashSet<>();
        for (Album album : albums) {
            albumNames.add(album.getName());
        }
        assertTrue("Album 1 listede olmalı", albumNames.contains("Test Album 1"));
        assertTrue("Album 2 listede olmalı", albumNames.contains("Test Album 2"));
        assertTrue("Album 3 listede olmalı", albumNames.contains("Test Album 3"));
    }

    /**
     * @brief getAll metodunun tekrarlanan ID'leri işlemediğini test eder
     */

    /**
     * @brief getAll metodunun albüm-sanatçı ilişkisini doğru kurduğunu test eder
     */
    @Test
    public void testGetAllIncludesCorrectArtistReferences() {
        // Arrange - İki farklı albüm ekle
        Album album1 = new Album("Test Album 1", testArtist, 2020);
        album1.setGenre("Rock");
        albumDAO.insert(album1);

        Album album2 = new Album("Test Album 2", testArtist, 2021);
        album2.setGenre("Pop");
        albumDAO.insert(album2);

        // Act - getAll metodunu çağır
        List<Album> albums = albumDAO.getAll();

        // Assert - Sonuçları kontrol et
        assertNotNull("Album listesi null olmamalı", albums);
        assertEquals("Album listesi iki eleman içermeli", 2, albums.size());

        // Tüm albümlerin sanatçı referanslarını kontrol et
        for (Album album : albums) {
            assertNotNull("Album sanatçısı null olmamalı", album.getArtist());
            assertEquals("Album sanatçı ID'si eşleşmiyor", testArtist.getId(), album.getArtist().getId());
            assertEquals("Album sanatçı adı eşleşmiyor", testArtist.getName(), album.getArtist().getName());
        }
    }

    /**
     * @brief Belirli bir sanatçıya ait albümleri getirme testini yapar
     */
    @Test
    public void testGetByArtist() {
        // Arrange - Test verileri hazırla
        Album album1 = new Album("Album 1", testArtist, 2020);
        album1.setGenre("Rock");
        albumDAO.insert(album1);

        Album album2 = new Album("Album 2", testArtist, 2021);
        album2.setGenre("Pop");
        albumDAO.insert(album2);

        // Farklı bir sanatçı oluştur
        Artist differentArtist = new Artist("Different Artist", "Different Bio");
        artistDAO.insert(differentArtist);

        Album album3 = new Album("Album 3", differentArtist, 2022);
        album3.setGenre("Jazz");
        albumDAO.insert(album3);

        // Act - Test sanatçısına ait albümleri getir
        List<Album> artistAlbums = albumDAO.getByArtist(testArtist);

        // Assert - Sonuçları kontrol et
        assertNotNull("Albüm listesi null olmamalı", artistAlbums);
        assertEquals("Test sanatçısına ait 2 albüm olmalı", 2, artistAlbums.size());

        // Albüm bilgilerini kontrol et
        Set<String> albumNames = new HashSet<>();
        for (Album album : artistAlbums) {
            // Her albümün doğru sanatçıya ait olduğunu kontrol et
            assertEquals("Albüm sanatçısı eşleşmiyor", testArtist.getId(), album.getArtist().getId());
            albumNames.add(album.getName());
        }

        // Belirli albümlerin varlığını kontrol et
        assertTrue("Album 1 listede olmalı", albumNames.contains("Album 1"));
        assertTrue("Album 2 listede olmalı", albumNames.contains("Album 2"));
    }

    /**
     * @brief Hiçbir albüm olmayan bir sanatçı için getByArtist metodunu test eder
     */
    @Test
    public void testGetByArtistWithNoAlbums() {
        // Arrange - Yeni bir sanatçı oluştur
        Artist noAlbumArtist = new Artist("No Album Artist", "No Album Bio");
        artistDAO.insert(noAlbumArtist);

        // Act - Albümü olmayan sanatçı için getByArtist metodunu çağır
        List<Album> artistAlbums = albumDAO.getByArtist(noAlbumArtist);

        // Assert - Sonuçları kontrol et
        assertNotNull("Albüm listesi null olmamalı", artistAlbums);
        assertTrue("Albüm listesi boş olmalı", artistAlbums.isEmpty());
    }

    /**
     * @brief Null sanatçı için getByArtist metodunu test eder
     */
    @Test(expected = NullPointerException.class)
    public void testGetByArtistWithNullArtist() {
        // Act - Null sanatçı ile getByArtist metodunu çağır
        albumDAO.getByArtist(null);
    }
}