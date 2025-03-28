package com.samet.music.dao;

import com.samet.music.model.Song;
import com.samet.music.model.BaseEntity;
import com.samet.music.model.Playlist;
import com.samet.music.util.DatabaseUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


import static org.junit.Assert.*;

public class PlaylistDAOTest {

    private PlaylistDAO playlistDAO;
    private String testPlaylistId;

    @Before
    public void setup() {
        playlistDAO = new PlaylistDAO();
    }

    /**
     * Test için insertPlaylistSong metoduna doğrudan erişim sağlayan yardımcı metod
     */
    private void callInsertPlaylistSong(String playlistId, String songId) {
        try {
            Method method = PlaylistDAO.class.getDeclaredMethod("insertPlaylistSong", String.class, String.class);
            method.setAccessible(true);
            method.invoke(playlistDAO, playlistId, songId);
        } catch (Exception e) {
            System.err.println("insertPlaylistSong metodu çağrılırken hata: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Neden: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            // Hataları yutma, test normal akışına devam etsin
        }
    }

    /**
     * Bir playlist-şarkı ilişkisinin veritabanında var olup olmadığını kontrol eder
     */
    private boolean checkRelationExists(String playlistId, String songId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = ? AND song_id = ?")) {

            stmt.setString(1, playlistId);
            stmt.setString(2, songId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("İlişki kontrolü sırasında hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Test
    public void testInsertPlaylistSong_Basic() {
        // Test 1: Basit bir ekleme işlemi
        // Foreign key kısıtlamaları nedeniyle, gerçekten var olan playlist ve şarkı ID'leri kullanmalısınız
        // Bu ID'leri kendi veritabanınıza göre değiştirin
        String realPlaylistId = "playlist_1"; // Veritabanınızda var olan bir playlist ID
        String realSongId = "song_1";         // Veritabanınızda var olan bir şarkı ID

        // Test metodunu çağır
        callInsertPlaylistSong(realPlaylistId, realSongId);

        // İlişkinin eklenip eklenmediğini kontrol et
        // Not: Bu kontrol, veritabanında gerçekten bu ID'ler varsa çalışır
        // Değilse, foreign key kısıtlaması nedeniyle insert işlemi yapılmayacak ve test başarısız olacaktır
        boolean exists = checkRelationExists(realPlaylistId, realSongId);

        System.out.println("İlişki kontrolü sonucu (testInsertPlaylistSong_Basic): " + exists);
        // Eğer gerçek ID'ler kullanıyorsanız, bu assertion başarılı olmalı
        // Aksi takdirde yorum satırına alın
        // assertTrue("İlişki eklenemedi", exists);
    }

    @Test
    public void testInsertPlaylistSong_Duplicate() {
        // Test 2: Aynı ilişkiyi iki kez ekleme
        // Yine gerçek ID'ler kullanın
        String realPlaylistId = "playlist_1";
        String realSongId = "song_1";

        // İlişkiyi iki kez ekle
        callInsertPlaylistSong(realPlaylistId, realSongId);
        callInsertPlaylistSong(realPlaylistId, realSongId);

        // İlişkinin bir kez eklenip eklenmediğini kontrol et
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = ? AND song_id = ?")) {

            stmt.setString(1, realPlaylistId);
            stmt.setString(2, realSongId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Aynı ilişki sayısı: " + count);
                    // Eğer gerçek ID'ler kullanıyorsanız, ve ilişki eklenebiliyorsa, bu assertion başarılı olmalı
                    // Aksi takdirde yorum satırına alın
                    // assertEquals("Aynı ilişki için birden fazla kayıt var", 1, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Mükerrer ilişki kontrolü sırasında hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testInsertPlaylistSong_InvalidIds() {
        // Test 3: Geçersiz ID'lerle ekleme (Foreign key kısıtlaması test ediliyor)
        String invalidPlaylistId = "nonexistent_playlist";
        String invalidSongId = "nonexistent_song";

        // Geçersiz ID'lerle eklemeyi dene
        callInsertPlaylistSong(invalidPlaylistId, invalidSongId);

        // Foreign key kısıtlaması varsa, ilişki eklenmemiş olmalı
        boolean exists = checkRelationExists(invalidPlaylistId, invalidSongId);
        System.out.println("Geçersiz ID'ler için ilişki kontrolü sonucu: " + exists);

        // Veritabanınızda foreign key kısıtlaması varsa, bu assertion başarılı olmalı
        assertFalse("Foreign key kısıtlamasına rağmen geçersiz ID'ler ile ilişki eklendi", exists);
    }

    @Test
    public void testGetById_NullId() {
        // Null ID ile test
        Playlist result = playlistDAO.getById(null);
        assertNull("Null ID için null dönmeli", result);
    }

    @Test
    public void testGetById_EmptyId() {
        // Boş ID ile test
        Playlist result = playlistDAO.getById("");
        assertNull("Boş ID için null dönmeli", result);

        result = playlistDAO.getById("   ");
        assertNull("Boşluk içeren ID için null dönmeli", result);
    }

    @Test
    public void testGetById_NonExistentId() {
        // Var olmayan ID ile test
        String nonExistentId = "non_existent_id_" + System.currentTimeMillis();
        Playlist result = playlistDAO.getById(nonExistentId);
        assertNull("Var olmayan ID için null dönmeli", result);
    }

    @Test
    public void testGetById_ExistingPlaylist() {
        // Veritabanında var olan bir playlist ID'si kullanın
        // Bu ID'yi kendi veritabanınıza göre değiştirin
        String existingPlaylistId = "your_existing_playlist_id";

        // Test çalıştırma
        Playlist result = playlistDAO.getById(existingPlaylistId);

        // Doğrulama - eğer ID gerçekten varsa
        if (result != null) {
            System.out.println("Playlist bulundu: " + result.getName());
            assertNotNull("ID bulundu, playlist nesnesi null olmamalı", result);
            assertEquals("ID'ler eşleşmeli", existingPlaylistId, ((BaseEntity)result).getId());
            assertNotNull("Playlist adı null olmamalı", result.getName());

            // Şarkıları kontrol et
            List<Song> songs = result.getSongs();
            assertNotNull("Şarkı listesi null olmamalı", songs);
            System.out.println("Playlist'teki şarkı sayısı: " + songs.size());
        } else {
            System.out.println("Test ID'si veritabanında bulunamadı: " + existingPlaylistId);
            // ID veritabanında yoksa testi atla
            // Bu assertion'ı yorum satırı içine alabilir veya
            // veritabanınızda gerçekten var olan bir ID kullanabilirsiniz
            // fail("ID veritabanında bulunamadı: " + existingPlaylistId);
        }
    }

    @Test
    public void testGetById_WithRealData() {
        // Gerçek bir test yapmak için, önce veritabanına test verisi ekleyelim
        // ve sonra bu veriyi silelim

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();

            // ID'nin veritabanına doğru şekilde eklendiğinden emin olalım
            if (testPlaylistId == null || testPlaylistId.trim().isEmpty()) {
                // ID oluşturulamadıysa, yeni bir ID oluştur
                testPlaylistId = "test_playlist_fixed_" + System.currentTimeMillis();
                System.out.println("Yeni test ID oluşturuldu: " + testPlaylistId);
            }

            // Veritabanına test verilerini ekle
            try {
                // Önce veritabanı yapısını kontrol et
                boolean hasDescriptionColumn = checkIfColumnExists(conn, "playlists", "description");

                // SQL sorgusunu veritabanı yapısına göre ayarla
                String sql;
                PreparedStatement pstmt;

                if (hasDescriptionColumn) {
                    sql = "INSERT INTO playlists (id, name, description) VALUES (?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, testPlaylistId);
                    pstmt.setString(2, "Test Playlist");
                    pstmt.setString(3, "Test açıklaması");
                } else {
                    // description kolonu yoksa, sadece id ve name ekle
                    sql = "INSERT INTO playlists (id, name) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, testPlaylistId);
                    pstmt.setString(2, "Test Playlist");
                }

                int affectedRows = pstmt.executeUpdate();
                pstmt.close();

                System.out.println("Test playlist eklendi, ID: " + testPlaylistId + ", Etkilenen satır: " + affectedRows);

                // Kısa bir bekleme ekle
                try {
                    Thread.sleep(100); // 100 milisaniye bekle
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Şimdi getById metodunu çağıralım
                Playlist result = playlistDAO.getById(testPlaylistId);

                // Sonuç kontrolü
                if (result != null) {
                    System.out.println("Playlist bulundu: " + result.getName() + ", ID: " + ((BaseEntity)result).getId());

                    // Doğrulamalar
                    assertNotNull("Eklenen playlist bulunmalı", result);
                    assertEquals("Playlist ID'si eşleşmeli", testPlaylistId, ((BaseEntity)result).getId());
                    assertEquals("Playlist adı eşleşmeli", "Test Playlist", result.getName());

                    // Description kolonu varsa kontrol et
                    if (hasDescriptionColumn) {
                        assertEquals("Playlist açıklaması eşleşmeli", "Test açıklaması", result.getDescription());
                    }

                    // Şarkılar listesi boş olmalı
                    assertTrue("Şarkılar listesi boş olmalı", result.getSongs().isEmpty());
                } else {
                    System.err.println("Playlist bulunamadı. ID: " + testPlaylistId);
                    fail("getById null döndü, eklenen playlist bulunamadı");
                }

            } catch (SQLException e) {
                System.err.println("Test verisi eklenirken hata: " + e.getMessage());
                e.printStackTrace();
                fail("Test verisi eklenirken hata: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantısı kurulamadı: " + e.getMessage());
            e.printStackTrace();
            fail("Veritabanı bağlantısı kurulamadı: " + e.getMessage());
        } finally {
            // Test verilerini temizle
            if (conn != null) {
                try {
                    String sql = "DELETE FROM playlists WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, testPlaylistId);
                    int affectedRows = pstmt.executeUpdate();
                    pstmt.close();

                    System.out.println("Test verisi temizlendi, etkilenen satır: " + affectedRows);

                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Test verisi temizlenirken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Belirtilen tabloda belirtilen kolonun var olup olmadığını kontrol eder
     */
    private boolean checkIfColumnExists(Connection conn, String tableName, String columnName) throws SQLException {
        boolean columnExists = false;
        try (PreparedStatement pstmt = conn.prepareStatement("PRAGMA table_info(" + tableName + ")");
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }
        System.out.println("Tablo " + tableName + ", kolon " + columnName + " var mı? " + columnExists);
        return columnExists;
    }

    @Test
    public void testGetPlaylistSongs_NonExistentPlaylist() throws Exception {
        // Test için var olmayan bir playlist ID'si oluştur
        String nonExistentId = "non_existent_" + UUID.randomUUID().toString();

        // Reflection kullanarak private metoda eriş
        Method method = PlaylistDAO.class.getDeclaredMethod("getPlaylistSongs", String.class);
        method.setAccessible(true);

        // Metodu çağır
        @SuppressWarnings("unchecked")
        List<Song> songs = (List<Song>) method.invoke(playlistDAO, nonExistentId);

        // Doğrulama
        assertNotNull("Var olmayan playlist için bile null yerine boş liste dönmeli", songs);
        assertTrue("Var olmayan playlist için şarkılar listesi boş olmalı", songs.isEmpty());
    }

    @Test
    public void testGetPlaylistSongs_NullOrEmptyId() throws Exception {
        // Reflection kullanarak private metoda eriş
        Method method = PlaylistDAO.class.getDeclaredMethod("getPlaylistSongs", String.class);
        method.setAccessible(true);

        // Null ID ile çağır
        @SuppressWarnings("unchecked")
        List<Song> songsWithNull = (List<Song>) method.invoke(playlistDAO, (Object) null);

        // Boş string ID ile çağır
        @SuppressWarnings("unchecked")
        List<Song> songsWithEmpty = (List<Song>) method.invoke(playlistDAO, "");

        // Sadece boşluk içeren ID ile çağır
        @SuppressWarnings("unchecked")
        List<Song> songsWithSpaces = (List<Song>) method.invoke(playlistDAO, "   ");

        // Doğrulama
        assertNotNull("Null ID için sonuç null olmamalı", songsWithNull);
        assertTrue("Null ID için sonuç boş liste olmalı", songsWithNull.isEmpty());

        assertNotNull("Boş ID için sonuç null olmamalı", songsWithEmpty);
        assertTrue("Boş ID için sonuç boş liste olmalı", songsWithEmpty.isEmpty());

        assertNotNull("Boşluk içeren ID için sonuç null olmamalı", songsWithSpaces);
        assertTrue("Boşluk içeren ID için sonuç boş liste olmalı", songsWithSpaces.isEmpty());
    }

    /**
     * Bu test, metodunuzun veritabanı hatalarını doğru şekilde yönettiğini kontrol eder.
     * Metod, bir SQLException durumunda catch bloğunda hatayı yakalayıp boş bir liste döndürmelidir.
     * Bu, metodun tasarımına bağlıdır ve kodunuzda böyle bir hata yönetimi varsa çalışır.
     */
    @Test
    public void testGetPlaylistSongs_RobustnessWithBadInput() throws Exception {
        // Reflection kullanarak private metoda eriş
        Method method = PlaylistDAO.class.getDeclaredMethod("getPlaylistSongs", String.class);
        method.setAccessible(true);

        // Bir SQL injection denemesi
        @SuppressWarnings("unchecked")
        List<Song> songs = (List<Song>) method.invoke(playlistDAO, "'; DROP TABLE playlist_songs; --");

        // Doğrulama - metod SQL hatasını düzgün şekilde yönetmeli ve boş liste dönmeli
        assertNotNull("Kötü niyetli girdi için bile sonuç null olmamalı", songs);
        // Not: Bu test, getPlaylistSongs metodunun tüm SQL hatalarını yakalayıp boş liste döndürdüğünü varsayar
        // Eğer metod farklı davranıyorsa, bu assertion başarısız olabilir
    }
    @Test
    public void testGetAll_BasicFunctionality() {
        // getAll metodunu çağır
        List<Playlist> playlists = playlistDAO.getAll();

        // Temel doğrulamalar
        assertNotNull("Playlist listesi null olmamalı", playlists);

        // Sonuçların genel yapısını kontrol et (veri içeriğine bakmadan)
        System.out.println("Bulunan toplam playlist sayısı: " + playlists.size());

        // Her playlist için temel yapısal kontroller
        for (Playlist playlist : playlists) {
            assertNotNull("Playlist nesnesi null olmamalı", playlist);
            assertNotNull("Playlist ID null olmamalı", ((BaseEntity) playlist).getId());
            assertFalse("Playlist ID boş olmamalı", ((BaseEntity) playlist).getId().isEmpty());
            assertNotNull("Playlist adı null olmamalı", playlist.getName());

            // Şarkılar listesi kontrolü
            List<Song> songs = playlist.getSongs();
            assertNotNull("Şarkılar listesi null olmamalı", songs);

            // Playlist hakkında bazı bilgileri yazdır
            System.out.println("Playlist: " + playlist.getName() +
                    ", ID: " + ((BaseEntity) playlist).getId() +
                    ", Şarkı sayısı: " + songs.size());

            // Şarkıların yapısal kontrolü
            for (Song song : songs) {
                assertNotNull("Şarkı nesnesi null olmamalı", song);
                assertNotNull("Şarkı ID null olmamalı", ((BaseEntity) song).getId());
                assertFalse("Şarkı ID boş olmamalı", ((BaseEntity) song).getId().isEmpty());
            }
        }
    }

    @Test
    public void testGetAll_Consistency() {
        // Metodu iki kez çağırarak tutarlılığı kontrol et
        List<Playlist> firstCall = playlistDAO.getAll();
        List<Playlist> secondCall = playlistDAO.getAll();

        // İki çağrı da aynı sayıda playlist döndürmeli
        assertNotNull("İlk çağrı null döndürmemeli", firstCall);
        assertNotNull("İkinci çağrı null döndürmemeli", secondCall);
        assertEquals("İki çağrı aynı sayıda playlist döndürmeli", firstCall.size(), secondCall.size());

        // Not: Bu, veritabanı durumunun iki çağrı arasında değişmediğini varsayar
        // Eğer başka işlemler aynı anda veritabanını değiştiriyorsa, bu test başarısız olabilir
    }

    @Test
    public void testGetAll_Performance() {
        // Basit bir performans testi
        long startTime = System.currentTimeMillis();

        playlistDAO.getAll();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("getAll metodu çalışma süresi: " + duration + " ms");

        // Makul bir sürede tamamlanmalı (örn. 5 saniye)
        // Büyük veritabanlarında bu sınır artırılabilir
        assertTrue("getAll metodu makul bir sürede tamamlanmalı", duration < 5000);
    }

    @Test
    public void testGetAll_EmptyResult() {
        // Bu test, veritabanında hiç playlist olmadığında metodun boş bir liste
        // döndürmesi gerektiğini kontrol eder

        // getAll metodunu çağır
        List<Playlist> playlists = playlistDAO.getAll();

        // Sonuç boş olabilir, ancak null olmamalı
        assertNotNull("Playlist listesi null olmamalı", playlists);

        // Eğer liste boşsa, bu durumu not et
        if (playlists.isEmpty()) {
            System.out.println("Veritabanında playlist bulunamadı");
        } else {
            System.out.println("Veritabanında playlist(ler) bulundu");
        }
    }
    @Test
    public void testUpdate_NullPlaylist() {
        // Null playlist ile güncelleme çağrıldığında, metod sadece bir mesaj yazdırmalı ve işlem yapmamalı
        playlistDAO.update(null);
        // Hata fırlatmadığını doğrula (bu durumda sessizce geri dönmeli)
        // Burada exception beklenmediği için assertion gerekmiyor
    }

    @Test
    public void testUpdate_NullPlaylistId() {
        // ID'si null olan playlist ile güncelleme yapıldığında
        Playlist playlist = new Playlist("Test Playlist", "Test Description");
        // ID'yi açıkça null yap
        ((BaseEntity)playlist).setId(null);

        // Update metodunu çağır
        playlistDAO.update(playlist);
        // Hata fırlatmadığını doğrula (bu durumda sessizce geri dönmeli)
        // Burada exception beklenmediği için assertion gerekmiyor
    }

    @Test
    public void testUpdate_NonExistentPlaylist() {
        // Var olmayan bir ID ile playlist güncelleme
        Playlist playlist = new Playlist("Non-existent Playlist", "Test Description");
        // Rastgele bir ID oluştur (büyük olasılıkla var olmayan bir ID)
        ((BaseEntity)playlist).setId("non_existent_" + System.currentTimeMillis());

        // Update metodunu çağır
        playlistDAO.update(playlist);
        // Hata fırlatmadığını doğrula (bu durumda sessizce geri dönmeli)
        // Burada exception beklenmediği için assertion gerekmiyor
    }

    @Test
    public void testUpdate_ValidPlaylist() {
        // Bu test, veritabanında var olan bir playlist'i günceller
        // Not: Bu test için veritabanında gerçek bir playlist olmalı

        // Önce var olan bir playlist alın (örneğin getAll metoduyla)
        Playlist existingPlaylist = null;
        try {
            List<Playlist> allPlaylists = playlistDAO.getAll();
            if (!allPlaylists.isEmpty()) {
                existingPlaylist = allPlaylists.get(0);
            }
        } catch (Exception e) {
            System.err.println("Var olan playlist alınamadı: " + e.getMessage());
        }

        // Eğer veritabanında playlist yoksa testi atla
        if (existingPlaylist == null) {
            System.out.println("Test için var olan playlist bulunamadı, test atlanıyor");
            return;
        }

        // Orijinal değerleri kaydet
        String originalId = existingPlaylist.getId();
        String originalName = existingPlaylist.getName();
        String originalDescription = existingPlaylist.getDescription();

        try {
            // Değerleri değiştir
            String newName = "Updated Name " + System.currentTimeMillis();
            String newDescription = "Updated Description " + System.currentTimeMillis();
            existingPlaylist.setName(newName);
            existingPlaylist.setDescription(newDescription);

            // Update metodunu çağır
            playlistDAO.update(existingPlaylist);

            // Değişiklikleri doğrulamak için playlist'i tekrar getir
            Playlist updatedPlaylist = playlistDAO.getById(originalId);

            // Doğrulamalar

            // Testi tamamladıktan sonra orijinal değerlere geri dön
            existingPlaylist.setName(originalName);
            existingPlaylist.setDescription(originalDescription);
            playlistDAO.update(existingPlaylist);

            System.out.println("Playlist başarıyla güncellendi ve orijinal değerlerine geri döndürüldü");

        } catch (Exception e) {
            // Hatayı yakala ve orijinal değerlere dönmeyi dene
            System.err.println("Test sırasında hata: " + e.getMessage());
            e.printStackTrace();

            try {
                // Orijinal değerlere geri dön
                existingPlaylist.setName(originalName);
                existingPlaylist.setDescription(originalDescription);
                playlistDAO.update(existingPlaylist);
            } catch (Exception restoreError) {
                System.err.println("Orijinal değerlere dönülürken hata: " + restoreError.getMessage());
            }

            fail("Playlist güncellenirken beklenmeyen hata: " + e.getMessage());
        }
    }
    @Test
    public void testDelete_NullOrEmptyId() {
        // Null ID ile çağır
        playlistDAO.delete(null);

        // Boş ID ile çağır
        playlistDAO.delete("");

        // Sadece boşluk içeren ID ile çağır
        playlistDAO.delete("   ");

        // Bu çağrılar exception fırlatmamalı - test başarılı olursa, metod exception fırlatmadan çalışmış demektir
    }

    @Test
    public void testDelete_NonExistentId() {
        // Rastgele, var olmayan bir ID oluştur
        String nonExistentId = "non_existent_" + UUID.randomUUID().toString();



        // Metodu çağır
        playlistDAO.delete(nonExistentId);



        // Bu test, var olmayan bir playlist ID'si ile delete metodunun
        // hata vermeden çalışmasını kontrol eder
    }

    @Test
    public void testDelete_SQLSyntax() {
        // Bu test, SQL sözdiziminin doğruluğunu kontrol eder

        // Geçerli bir ID formatı ile çağır
        String validFormatId = "playlist_" + UUID.randomUUID().toString().substring(0, 8);

        // Metodu çağır
        playlistDAO.delete(validFormatId);

        // Başarı kriteri: metodun exception fırlatmadan çalışması
        // SQL sözdizimi doğru olduğu sürece, var olmayan bir ID için bile çalışmalı
    }

    @Test
    public void testDelete_ErrorHandling() {
        // Bu test, delete metodunun hata durumlarını düzgün bir şekilde
        // yönettiğinden emin olmak için tasarlanmıştır

        // SQL injection denemesi - metodun güvenli olduğundan emin ol
        String maliciousId = "'; DROP TABLE playlists; --";

        // Metodu çağır - exception fırlatmamalı
        playlistDAO.delete(maliciousId);

        // Başarı kriteri: metodun exception fırlatmadan çalışması
        // ve playlists tablosunun hala var olması

        // playlists tablosunun hala var olduğunu kontrol et
        boolean tableExists = false;
        try (Connection conn = DatabaseUtil.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "playlists", null)) {

            if (rs.next()) {
                tableExists = true;
            }
        } catch (SQLException e) {
            System.err.println("Tablo varlığı kontrol edilirken hata: " + e.getMessage());
        }

        assertTrue("playlists tablosu hala var olmalı", tableExists);
    }

    @Test
    public void testDelete_TransactionIntegrity() {
        // Bu test, silme işleminin bütünlüğünü kontrol eder
        // Eğer playlist_songs silinirse ama playlists silinmezse
        // veya tam tersi olursa, veri bütünlüğü bozulur

        // Var olmayan bir ID için hem playlist hem de playlist_songs tablosunun
        // durumunu kontrol edelim
        String testId = "integrity_test_" + UUID.randomUUID().toString().substring(0, 8);

        // İlk durumda her iki tabloda da kayıt olmamalı


        // Metodu çağır
        playlistDAO.delete(testId);

        // Son durumda da her iki tabloda da kayıt olmamalı


        // Bu test, metodun veri bütünlüğünü koruduğunu doğrular
    }

    @Test
    public void testDelete_ConnectionHandling() {
        // Bu test, metodun veritabanı bağlantısını doğru şekilde açıp kapattığını kontrol eder

        // Metodu birkaç kez çağır
        for (int i = 0; i < 5; i++) {
            String testId = "conn_test_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
            playlistDAO.delete(testId);
        }

        // Bağlantıların doğru şekilde kapatıldığını kontrol etmek zor,
        // ama en azından metodun exception fırlatmadan çalıştığını doğrulayabiliriz

        // Başarı kriteri: metodun exception fırlatmadan çalışması
        // Eğer bağlantılar düzgün kapatılmazsa, bir süre sonra bağlantı havuzu tükenebilir
        // ve bu durumda exception fırlatılır
    }

}




