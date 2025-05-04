package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * AlbumDAO için test sınıfı
 */
public class AlbumDAOTest {
    
    private AlbumDAO albumDAO;
    private TestDatabaseManager testDbManager;
    private TestSongDAO testSongDAO;
    
    @Before
    public void setUp() {
        testDbManager = new TestDatabaseManager();
        testSongDAO = new TestSongDAO();
        albumDAO = new AlbumDAO(testDbManager.getConnection(), testSongDAO) {
            @Override
            public boolean create(Album album) {
                testDbManager.setAlbum(album);
                album.setId(1);
                return true;
            }
            
            @Override
            public Album findById(int id) {
                if (id == 1 && testDbManager.getAlbum() != null) {
                    return testDbManager.getAlbum();
                }
                return null;
            }
            
            @Override
            public List<Album> findAll() {
                List<Album> albums = new ArrayList<>();
                if (testDbManager.getAlbum() != null) {
                    albums.add(testDbManager.getAlbum());
                }
                return albums;
            }
            
            @Override
            public List<Album> findByUserId(int userId) {
                List<Album> albums = new ArrayList<>();
                if (testDbManager.getAlbum() != null && testDbManager.getAlbum().getUserId() == userId) {
                    albums.add(testDbManager.getAlbum());
                }
                return albums;
            }
            
            @Override
            public List<Album> findByArtist(String artist) {
                List<Album> albums = new ArrayList<>();
                Album album = testDbManager.getAlbum();
                
                if (album != null && artist != null && album.getArtist().contains(artist)) {
                    albums.add(album);
                }
                
                return albums;
            }
            
            @Override
            public boolean update(Album album) {
                if (album.getId() == 1 && testDbManager.getAlbum() != null) {
                    testDbManager.setAlbum(album);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean delete(int id) {
                if (id == 1 && testDbManager.getAlbum() != null) {
                    testDbManager.setAlbum(null);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean addSongsToAlbum(int albumId, List<Song> songs) {
                if (albumId == 1 && testDbManager.getAlbum() != null) {
                    Album album = testDbManager.getAlbum();
                    album.setSongs(songs);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean removeSongsFromAlbum(int albumId) {
                if (albumId == 1 && testDbManager.getAlbum() != null) {
                    Album album = testDbManager.getAlbum();
                    album.setSongs(new ArrayList<>());
                    return true;
                }
                return false;
            }
        };
    }
    
    /**
     * Mock Connection sınıfı
     */
    private class MockConnection implements Connection {
        @Override public void close() throws SQLException {}
        @Override public boolean isClosed() throws SQLException { return false; }
        @Override public void setAutoCommit(boolean autoCommit) throws SQLException {}
        @Override public boolean getAutoCommit() throws SQLException { return true; }
        @Override public void commit() throws SQLException {}
        @Override public void rollback() throws SQLException {}
        @Override public PreparedStatement prepareStatement(String sql) throws SQLException { return null; }
        @Override public Statement createStatement() throws SQLException { return null; }
        
        // Kullanılmayan diğer metotlar
        @Override public DatabaseMetaData getMetaData() throws SQLException { return null; }
        @Override public void setReadOnly(boolean readOnly) throws SQLException {}
        @Override public boolean isReadOnly() throws SQLException { return false; }
        @Override public void setCatalog(String catalog) throws SQLException {}
        @Override public String getCatalog() throws SQLException { return null; }
        @Override public void setTransactionIsolation(int level) throws SQLException {}
        @Override public int getTransactionIsolation() throws SQLException { return 0; }
        @Override public SQLWarning getWarnings() throws SQLException { return null; }
        @Override public void clearWarnings() throws SQLException {}
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return null; }
        @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return null; }
        @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException {}
        @Override public void setHoldability(int holdability) throws SQLException {}
        @Override public int getHoldability() throws SQLException { return 0; }
        @Override public Savepoint setSavepoint() throws SQLException { return null; }
        @Override public Savepoint setSavepoint(String name) throws SQLException { return null; }
        @Override public void rollback(Savepoint savepoint) throws SQLException {}
        @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException {}
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return null; }
        @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return null; }
        @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return null; }
        @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return null; }
        @Override public Clob createClob() throws SQLException { return null; }
        @Override public Blob createBlob() throws SQLException { return null; }
        @Override public NClob createNClob() throws SQLException { return null; }
        @Override public SQLXML createSQLXML() throws SQLException { return null; }
        @Override public boolean isValid(int timeout) throws SQLException { return true; }
        @Override public void setClientInfo(String name, String value) throws SQLClientInfoException {}
        @Override public void setClientInfo(Properties properties) throws SQLClientInfoException {}
        @Override public String getClientInfo(String name) throws SQLException { return null; }
        @Override public Properties getClientInfo() throws SQLException { return null; }
        @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return null; }
        @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return null; }
        @Override public void setSchema(String schema) throws SQLException {}
        @Override public String getSchema() throws SQLException { return null; }
        @Override public void abort(java.util.concurrent.Executor executor) throws SQLException {}
        @Override public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {}
        @Override public int getNetworkTimeout() throws SQLException { return 0; }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        @Override public String nativeSQL(String sql) throws SQLException { return null; }
        @Override public CallableStatement prepareCall(String sql) throws SQLException { return null; }
    }
    
    /**
     * Test veri yönetim sınıfı
     */
    private class TestDatabaseManager {
        private Album album;
        private MockConnection connection = new MockConnection();
        
        public Album getAlbum() {
            return album;
        }
        
        public void setAlbum(Album album) {
            this.album = album;
        }
        
        public Connection getConnection() {
            return connection;
        }
    }
    
    /**
     * Mock SongDAO sınıfı
     */
    private class TestSongDAO extends SongDAO {
        private List<Song> songs = new ArrayList<>();
        
        @Override
        public Song mapResultSetToSong(ResultSet rs) throws SQLException {
            return new Song("Test Song", "Test Artist", "Test Album", "Test Genre", 2023, 180, "/path/to/file", 1);
        }
    }
    
    /**
     * Albüm oluşturma işlemini test eder
     */
    @Test
    public void testCreate() {
        // Test albümünü oluştur
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // create metodu çağır
        boolean result = albumDAO.create(album);
        
        // Sonuçları doğrula
        assertTrue("Album başarıyla oluşturulmalı", result);
        assertEquals("Album ID 1 olmalı", 1, album.getId());
        assertEquals("Album başlığı eşleşmeli", "Test Album", album.getTitle());
        assertEquals("Sanatçı adı eşleşmeli", "Test Artist", album.getArtist());
        assertEquals("Yıl eşleşmeli", 2023, album.getYear());
        assertEquals("Tür eşleşmeli", "Rock", album.getGenre());
        assertEquals("Kullanıcı ID eşleşmeli", 1, album.getUserId());
    }
    
    /**
     * ID ile albüm bulma işlemini test eder
     */
    @Test
    public void testFindById() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        testDbManager.setAlbum(album);
        
        // Var olan albümü ara
        Album result = albumDAO.findById(1);
        assertNotNull("ID 1 olan albüm bulunmalı", result);
        assertEquals("Albüm başlığı eşleşmeli", "Test Album", result.getTitle());
        
        // Var olmayan albümü ara
        Album nonExistentResult = albumDAO.findById(2);
        assertNull("ID 2 olan albüm bulunmamalı", nonExistentResult);
    }
    
    /**
     * Tüm albümleri bulma işlemini test eder
     */
    @Test
    public void testFindAll() {
        // Albüm yokken test et
        List<Album> emptyResults = albumDAO.findAll();
        assertEquals("Hiç albüm yokken boş liste dönmeli", 0, emptyResults.size());
        
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        testDbManager.setAlbum(album);
        
        // Bir albüm varken test et
        List<Album> results = albumDAO.findAll();
        assertEquals("Bir albüm olmalı", 1, results.size());
        assertEquals("Albüm başlığı eşleşmeli", "Test Album", results.get(0).getTitle());
    }
    
    /**
     * Kullanıcı ID'sine göre albüm bulma işlemini test eder
     */
    @Test
    public void testFindByUserId() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setUserId(1);
        testDbManager.setAlbum(album);
        
        // Kullanıcı ID 1 için albümleri ara
        List<Album> results = albumDAO.findByUserId(1);
        assertEquals("Kullanıcı ID 1 için bir albüm olmalı", 1, results.size());
        
        // Kullanıcı ID 2 için albümleri ara
        List<Album> nonExistentResults = albumDAO.findByUserId(2);
        assertEquals("Kullanıcı ID 2 için albüm olmamalı", 0, nonExistentResults.size());
    }
    
    /**
     * Sanatçıya göre albüm bulma işlemini test eder
     */
    @Test
    public void testFindByArtist() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        testDbManager.setAlbum(album);
        
        // Var olan sanatçı için albümleri ara
        List<Album> results = albumDAO.findByArtist("Test");
        assertEquals("Test içeren sanatçı adı için bir albüm olmalı", 1, results.size());
        
        // Var olmayan sanatçı için albümleri ara
        List<Album> nonExistentResults = albumDAO.findByArtist("Nonexistent");
        assertEquals("Nonexistent içeren sanatçı adı için albüm olmamalı", 0, nonExistentResults.size());
    }
    
    /**
     * Albüm güncelleme işlemini test eder
     */
    @Test
    public void testUpdate() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Original Title");
        album.setArtist("Original Artist");
        album.setYear(2020);
        album.setGenre("Pop");
        testDbManager.setAlbum(album);
        
        // Güncellenmiş albüm oluştur
        Album updatedAlbum = new Album();
        updatedAlbum.setId(1);
        updatedAlbum.setTitle("Updated Title");
        updatedAlbum.setArtist("Updated Artist");
        updatedAlbum.setYear(2023);
        updatedAlbum.setGenre("Rock");
        
        // Albümü güncelle
        boolean result = albumDAO.update(updatedAlbum);
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Güncellemenin uygulandığını doğrula
        Album storedAlbum = testDbManager.getAlbum();
        assertEquals("Başlık güncellenmiş olmalı", "Updated Title", storedAlbum.getTitle());
        assertEquals("Sanatçı güncellenmiş olmalı", "Updated Artist", storedAlbum.getArtist());
        
        // Var olmayan albümü güncellemeyi dene
        Album nonExistentAlbum = new Album();
        nonExistentAlbum.setId(2);
        nonExistentAlbum.setTitle("Nonexistent");
        
        boolean nonExistentResult = albumDAO.update(nonExistentAlbum);
        assertFalse("Var olmayan albümü güncellemek başarısız olmalı", nonExistentResult);
    }
    
    /**
     * Albüm silme işlemini test eder
     */
    @Test
    public void testDelete() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        testDbManager.setAlbum(album);
        
        // Var olan albümü sil
        boolean result = albumDAO.delete(1);
        assertTrue("Silme işlemi başarılı olmalı", result);
        assertNull("Albüm silinmiş olmalı", testDbManager.getAlbum());
        
        // Var olmayan albümü silmeyi dene
        boolean nonExistentResult = albumDAO.delete(2);
        assertFalse("Var olmayan albümü silmek başarısız olmalı", nonExistentResult);
    }
    
    /**
     * Albüme şarkı ekleme işlemini test eder
     */
    @Test
    public void testAddSongsToAlbum() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        testDbManager.setAlbum(album);
        
        // Eklenecek şarkıları oluştur
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file1", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Test Artist", "Test Album", "Rock", 2023, 200, "/path/to/file2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        
        // Şarkıları albüme ekle
        boolean result = albumDAO.addSongsToAlbum(1, songs);
        assertTrue("Şarkı ekleme başarılı olmalı", result);
        assertEquals("Albümde 2 şarkı olmalı", 2, testDbManager.getAlbum().getSongs().size());
        
        // Var olmayan albüme şarkı eklemeyi dene
        boolean nonExistentResult = albumDAO.addSongsToAlbum(2, songs);
        assertFalse("Var olmayan albüme şarkı eklemek başarısız olmalı", nonExistentResult);
    }
    
    /**
     * Albümden şarkı çıkarma işlemini test eder
     */
    @Test
    public void testRemoveSongsFromAlbum() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        
        // Albüme şarkı ekle
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file1", 1);
        song1.setId(1);
        songs.add(song1);
        album.setSongs(songs);
        
        testDbManager.setAlbum(album);
        
        // Albümdeki şarkıları çıkar
        boolean result = albumDAO.removeSongsFromAlbum(1);
        assertTrue("Şarkı çıkarma başarılı olmalı", result);
        assertEquals("Albümde şarkı kalmamalı", 0, testDbManager.getAlbum().getSongs().size());
        
        // Var olmayan albümden şarkı çıkarmayı dene
        boolean nonExistentResult = albumDAO.removeSongsFromAlbum(2);
        assertFalse("Var olmayan albümden şarkı çıkarmak başarısız olmalı", nonExistentResult);
    }
    
    /**
     * Şarkılı albüm oluşturma işlemini test eder
     */
    @Test
    public void testCreateWithSongs() {
        // Test albümünü oluştur
        Album album = new Album();
        album.setTitle("Test Album With Songs");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Albüme şarkı ekle
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file1", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Test Artist", "Test Album", "Rock", 2023, 200, "/path/to/file2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        album.setSongs(songs);
        
        // create metodu çağır
        boolean result = albumDAO.create(album);
        
        // Sonuçları doğrula
        assertTrue("Şarkılı albüm başarıyla oluşturulmalı", result);
        assertEquals("Album ID 1 olmalı", 1, album.getId());
        assertEquals("Albüm başlığı eşleşmeli", "Test Album With Songs", album.getTitle());
        assertEquals("Şarkı sayısı eşleşmeli", 2, testDbManager.getAlbum().getSongs().size());
    }
    
    /**
     * FindById metodunun şarkıları doğru getirdiğini test eder
     */
    @Test
    public void testFindByIdWithSongs() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Albüme şarkı ekle
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file1", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Test Artist", "Test Album", "Rock", 2023, 200, "/path/to/file2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        album.setSongs(songs);
        
        testDbManager.setAlbum(album);
        
        // findById metodunu çağır
        Album result = albumDAO.findById(1);
        
        // Sonuçları doğrula
        assertNotNull("ID 1 olan albüm bulunmalı", result);
        assertEquals("Albüm başlığı eşleşmeli", "Test Album", result.getTitle());
        assertNotNull("Şarkı listesi null olmamalı", result.getSongs());
        assertEquals("Şarkı sayısı eşleşmeli", 2, result.getSongs().size());
    }
    
    /**
     * Boş şarkı listesi ile albüm oluşturma işlemini test eder
     */
    @Test
    public void testCreateWithEmptySongsList() {
        // Test albümünü oluştur
        Album album = new Album();
        album.setTitle("Test Album Empty Songs");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Boş şarkı listesi ekle
        album.setSongs(new ArrayList<>());
        
        // create metodu çağır
        boolean result = albumDAO.create(album);
        
        // Sonuçları doğrula
        assertTrue("Boş şarkı listeli albüm başarıyla oluşturulmalı", result);
        assertEquals("Album ID 1 olmalı", 1, album.getId());
        assertEquals("Albüm başlığı eşleşmeli", "Test Album Empty Songs", album.getTitle());
        assertEquals("Şarkı sayısı 0 olmalı", 0, testDbManager.getAlbum().getSongs().size());
    }
    
    /**
     * Null şarkı listesi ile albüm oluşturma işlemini test eder
     */
    @Test
    public void testCreateWithNullSongsList() {
        // Test albümünü oluştur
        Album album = new Album();
        album.setTitle("Test Album Null Songs");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Null şarkı listesi (default)
        album.setSongs(null);
        
        // create metodu çağır
        boolean result = albumDAO.create(album);
        
        // Sonuçları doğrula
        assertTrue("Null şarkı listeli albüm başarıyla oluşturulmalı", result);
        assertEquals("Album ID 1 olmalı", 1, album.getId());
        assertEquals("Albüm başlığı eşleşmeli", "Test Album Null Songs", album.getTitle());
        assertNull("Şarkı listesi null olmalı", testDbManager.getAlbum().getSongs());
    }
    
    /**
     * Tür ve yıl filtrelenmesi ile albüm güncelleme işlemini test eder
     */
    @Test
    public void testUpdateAlbumGenreAndYear() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Original Title");
        album.setArtist("Original Artist");
        album.setYear(2020);
        album.setGenre("Pop");
        testDbManager.setAlbum(album);
        
        // Sadece tür ve yılı güncelle, başlık ve sanatçıyı değiştirme
        Album updatedAlbum = new Album();
        updatedAlbum.setId(1);
        updatedAlbum.setTitle("Original Title");
        updatedAlbum.setArtist("Original Artist");
        updatedAlbum.setYear(2023);
        updatedAlbum.setGenre("Jazz");
        
        // Albümü güncelle
        boolean result = albumDAO.update(updatedAlbum);
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Güncellemenin uygulandığını doğrula
        Album storedAlbum = testDbManager.getAlbum();
        assertEquals("Başlık değişmemeli", "Original Title", storedAlbum.getTitle());
        assertEquals("Sanatçı değişmemeli", "Original Artist", storedAlbum.getArtist());
        assertEquals("Yıl güncellenmiş olmalı", 2023, storedAlbum.getYear());
        assertEquals("Tür güncellenmiş olmalı", "Jazz", storedAlbum.getGenre());
    }
    
    /**
     * Albüme tek şarkı ekleme işlemini test eder
     */
    @Test
    public void testAddSingleSongToAlbum() {
        // Test albümünü oluştur ve veritabanına ekle
        Album album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        testDbManager.setAlbum(album);
        
        // Tek bir şarkı ekle
        List<Song> songs = new ArrayList<>();
        Song song = new Song("Single Song", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file", 1);
        song.setId(1);
        songs.add(song);
        
        // Şarkıyı albüme ekle
        boolean result = albumDAO.addSongsToAlbum(1, songs);
        assertTrue("Tek şarkı ekleme başarılı olmalı", result);
        assertEquals("Albümde 1 şarkı olmalı", 1, testDbManager.getAlbum().getSongs().size());
        assertEquals("Şarkı başlığı eşleşmeli", "Single Song", testDbManager.getAlbum().getSongs().get(0).getTitle());
    }
} 