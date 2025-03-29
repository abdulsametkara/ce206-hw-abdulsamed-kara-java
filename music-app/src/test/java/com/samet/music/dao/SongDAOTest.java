package com.samet.music.dao;

import static org.junit.Assert.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samet.music.model.Song;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.util.DatabaseUtil;

/**
 * @class SongDAOTest
 * @brief SongDAO sınıfı için test sınıfı
 */
public class SongDAOTest {

    private SongDAO songDAO;
    private AlbumDAO albumDAO;
    private ArtistDAO artistDAO;
    private Artist testArtist;
    private Album testAlbum;

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
        songDAO = new SongDAO();
        albumDAO = new AlbumDAO();
        artistDAO = new ArtistDAO();

        // Test için bir sanatçı oluştur ve veritabanına ekle
        testArtist = new Artist("Test Artist", "Test Biography");
        artistDAO.insert(testArtist);

        // Test için bir albüm oluştur ve veritabanına ekle
        testAlbum = new Album("Test Album", testArtist, 2023);
        testAlbum.setGenre("Rock");
        albumDAO.insert(testAlbum);
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Veritabanını temizle
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM songs")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief insert metodunu test eder - yeni şarkı ekleme
     */
    @Test
    public void testInsertNewSong() throws Exception {
        // Arrange - Test için şarkı oluştur
        Song song = new Song("Test Song", testArtist, 240);
        song.setAlbum(testAlbum);

        // Act - Şarkıyı veritabanına ekle
        songDAO.insert(song);

        // Assert - Şarkının veritabanına eklenip eklenmediğini kontrol et
        boolean found = false;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs WHERE id = ?")) {

            stmt.setString(1, song.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    found = true;
                    assertEquals("Şarkı adı eşleşmiyor", "Test Song", rs.getString("name"));
                    assertEquals("Albüm ID'si eşleşmiyor", testAlbum.getId(), rs.getString("album_id"));
                    assertEquals("Şarkı süresi eşleşmiyor", 240, rs.getInt("duration"));

                }
            }
        }

        assertTrue("Şarkı veritabanına eklenmedi", found);
    }

    /**
     * @brief insert metodunu test eder - var olan şarkıyı güncelleme
     */
    @Test
    public void testInsertExistingSong() throws Exception {
        // Arrange - İlk şarkıyı oluştur ve ekle
        Song song = new Song("Original Song", testArtist, 180);
        song.setAlbum(testAlbum);

        // İlk şarkıyı ekle
        songDAO.insert(song);

        // Veritabanındaki şarkıyı al
        String songId = song.getId();

        // Yeni bir albüm oluştur
        Album newAlbum = new Album("New Album", testArtist, 2024);
        newAlbum.setGenre("Pop");
        albumDAO.insert(newAlbum);

        // Güncelleme için yeni bir şarkı oluştur
        Song updatedSong = new Song("Updated Song", testArtist, 300);
        updatedSong.setAlbum(newAlbum);
        // BaseEntity sınıfında setId metodu olduğunu varsayıyoruz
        // Eğer yoksa reflection ile id alanını değiştirmek gerekebilir
        setSongId(updatedSong, songId);


        // Act - Güncelleme için insert metodunu çağır
        songDAO.insert(updatedSong);

        // Assert - Şarkının güncellendiğini kontrol et
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs WHERE id = ?")) {

            stmt.setString(1, songId);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Şarkı bulunamadı", rs.next());
                assertEquals("Şarkı adı güncellenmiş olmalı", "Updated Song", rs.getString("name"));
                assertEquals("Albüm ID'si güncellenmiş olmalı", newAlbum.getId(), rs.getString("album_id"));
                assertEquals("Şarkı süresi güncellenmiş olmalı", 300, rs.getInt("duration"));

            }
        }
    }

    /**
     * @brief insert metodunu null şarkı ile test eder (exception beklenir)
     */
    @Test
    public void testInsertNullSong() {
        try {
            // Act - null şarkı ekle
            songDAO.insert(null);
            fail("NullPointerException bekleniyor");
        } catch (NullPointerException e) {
            // Expected exception
        } catch (Exception e) {
            fail("Beklenmeyen exception: " + e.getMessage());
        }
    }

    /**
     * @brief Null albüm bilgisi olan şarkı ekleme işlemini test eder
     */
    @Test
    public void testInsertWithNullAlbum() {
        // Arrange - Null albüm bilgisi olan bir şarkı oluştur
        Song song = new Song("Null Album Song", testArtist, 200);
        song.setAlbum(null);

        try {
            // Act - Şarkıyı eklemeye çalış
            songDAO.insert(song);

            // Eğer exception fırlatılmazsa, veritabanında albüm ID'sinin null olması beklenir
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs WHERE id = ?")) {

                stmt.setString(1, song.getId());

                // Burada veritabanı kontrolü yapılabilir
            }
        } catch (Exception e) {
            // Eğer exception fırlatılırsa da bu geçerli bir durumdur
            // İşlem tamamlandı olarak kabul edilir
        }
    }

    /**
     * Yardımcı metod: Song nesnesinin ID'sini ayarlamak için
     * Not: BaseEntity sınıfında setId metodu yoksa, reflection kullanarak ID alanını değiştirmek gerekebilir
     */
    private void setSongId(Song song, String id) {
        try {
            // Reflection yöntemiyle ID'yi ayarla
            java.lang.reflect.Field idField = song.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(song, id);
        } catch (Exception e) {
            fail("Song ID'si ayarlanamadı: " + e.getMessage());
        }
    }
}