package com.samet.music.util;

import org.junit.Test;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * SQLQueries sınıfı için test sınıfı.
 * Bu testler SQL sorgularının geçerli olduğunu ve veritabanı tarafından
 * hatasız bir şekilde hazırlanabildiğini test eder.
 */
public class SQLQueriesTest {

    /**
     * SQLQueries sınıfının private constructor'ının test edilmesi
     * Bu test, private constructor'ın kapsama (coverage) dahil olması için bulunuyor
     */
    @Test
    public void testPrivateConstructor() throws Exception {
        // Private constructor'ı reflection ile çağırma
        java.lang.reflect.Constructor<SQLQueries> constructor = SQLQueries.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        // Constructor'ı çağırıp nesne oluşturmayı deniyoruz - exception fırlatmamalı
        SQLQueries instance = constructor.newInstance();
        assertNotNull(instance);
    }

    /**
     * Artist sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testArtistQueries() throws SQLException {
        // In-memory veritabanında sorguları test et
        try (Connection conn = createInMemoryDatabase()) {
            // Tabloları oluştur
            createTables(conn);
            
            // INSERT sorgusu
            testQuery(conn, SQLQueries.Artist.INSERT);
            
            // SELECT sorguları
            testQuery(conn, SQLQueries.Artist.SELECT_BY_ID);
            testQuery(conn, SQLQueries.Artist.SELECT_ALL);
            testQuery(conn, SQLQueries.Artist.SEARCH_BY_NAME);
            
            // UPDATE sorgusu
            testQuery(conn, SQLQueries.Artist.UPDATE);
            
            // DELETE sorguları
            testQuery(conn, SQLQueries.Artist.DELETE);
            testQuery(conn, SQLQueries.Artist.DELETE_CASCADE_PLAYLIST_SONGS);
            testQuery(conn, SQLQueries.Artist.DELETE_CASCADE_SONGS);
            testQuery(conn, SQLQueries.Artist.DELETE_CASCADE_ALBUMS);
            
            // MERGE sorguları
            testQuery(conn, SQLQueries.Artist.MERGE_UPDATE_SONGS);
            testQuery(conn, SQLQueries.Artist.MERGE_UPDATE_ALBUMS);
        }
    }
    
    /**
     * Album sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testAlbumQueries() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // Tabloları oluştur
            createTables(conn);
            
            // INSERT sorgusu
            testQuery(conn, SQLQueries.Album.INSERT);
            
            // SELECT sorguları
            testQuery(conn, SQLQueries.Album.SELECT_BY_ID);
            testQuery(conn, SQLQueries.Album.SELECT_ALL);
            testQuery(conn, SQLQueries.Album.SEARCH_BY_NAME);
            testQuery(conn, SQLQueries.Album.SELECT_BY_ARTIST);
            testQuery(conn, SQLQueries.Album.SELECT_BY_GENRE);
            
            // UPDATE sorguları
            testQuery(conn, SQLQueries.Album.UPDATE);
            testQuery(conn, SQLQueries.Album.UPDATE_SONGS_REMOVE_ALBUM);
            
            // DELETE sorguları
            testQuery(conn, SQLQueries.Album.DELETE);
            testQuery(conn, SQLQueries.Album.DELETE_CASCADE_PLAYLIST_SONGS);
            testQuery(conn, SQLQueries.Album.DELETE_CASCADE_SONGS);
        }
    }
    
    /**
     * Song sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testSongQueries() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // Tabloları oluştur
            createTables(conn);
            
            // INSERT sorgusu
            testQuery(conn, SQLQueries.Song.INSERT);
            
            // SELECT sorguları
            testQuery(conn, SQLQueries.Song.SELECT_BY_ID);
            testQuery(conn, SQLQueries.Song.SELECT_ALL);
            testQuery(conn, SQLQueries.Song.SEARCH_BY_NAME);
            testQuery(conn, SQLQueries.Song.SELECT_BY_ARTIST);
            testQuery(conn, SQLQueries.Song.SELECT_BY_ALBUM);
            testQuery(conn, SQLQueries.Song.SELECT_BY_GENRE);
            
            // UPDATE sorguları
            testQuery(conn, SQLQueries.Song.UPDATE);
            
            // DELETE sorguları
            testQuery(conn, SQLQueries.Song.DELETE);
            testQuery(conn, SQLQueries.Song.DELETE_FROM_PLAYLISTS);
        }
    }
    
    /**
     * Playlist sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testPlaylistQueries() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // Tabloları oluştur
            createTables(conn);
            
            // INSERT sorguları
            testQuery(conn, SQLQueries.Playlist.INSERT);
            testQuery(conn, SQLQueries.Playlist.ADD_SONG);
            
            // SELECT sorguları
            testQuery(conn, SQLQueries.Playlist.SELECT_BY_ID);
            testQuery(conn, SQLQueries.Playlist.SELECT_ALL);
            testQuery(conn, SQLQueries.Playlist.SEARCH_BY_NAME);
            testQuery(conn, SQLQueries.Playlist.SELECT_SONGS);
            testQuery(conn, SQLQueries.Playlist.SELECT_PLAYLISTS_FOR_SONG);
            
            // UPDATE sorguları
            testQuery(conn, SQLQueries.Playlist.UPDATE);
            
            // DELETE sorguları
            testQuery(conn, SQLQueries.Playlist.DELETE);
            testQuery(conn, SQLQueries.Playlist.REMOVE_SONG);
            testQuery(conn, SQLQueries.Playlist.DELETE_ALL_SONGS);
        }
    }
    
    /**
     * User sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testUserQueries() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // Kullanıcı tablosunu oluştur
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(SQLQueries.User.CREATE_TABLE);
            }
            
            // INSERT sorguları
            testQuery(conn, SQLQueries.User.INSERT_OR_UPDATE);
            
            // SELECT sorguları
            testQuery(conn, SQLQueries.User.SELECT_PASSWORD);
            testQuery(conn, SQLQueries.User.SELECT_ALL);
            
            // DELETE sorguları
            testQuery(conn, SQLQueries.User.DELETE);
        }
    }
    
    /**
     * Schema sınıfı içindeki SQL sorgularının geçerliliğini test eder
     */
    @Test
    public void testSchemaQueries() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // CREATE TABLE sorguları
            try (Statement stmt = conn.createStatement()) {
                // Her bir tabloyu oluştur
                stmt.execute(SQLQueries.Schema.CREATE_ARTISTS);
                stmt.execute(SQLQueries.Schema.CREATE_ALBUMS);
                stmt.execute(SQLQueries.Schema.CREATE_SONGS);
                stmt.execute(SQLQueries.Schema.CREATE_PLAYLISTS);
                stmt.execute(SQLQueries.Schema.CREATE_PLAYLIST_SONGS);
            }
        }
    }
    
    /**
     * Schema CREATE ifadelerinin doğru syntax'a sahip olduğunu test eder
     */
    @Test
    public void testSchemaCreateSyntax() {
        // Schema CREATE ifadelerinin doğru syntax'a sahip olduğunu kontrol et
        assertTrue(SQLQueries.Schema.CREATE_ARTISTS.contains("CREATE TABLE"));
        assertTrue(SQLQueries.Schema.CREATE_ARTISTS.contains("id TEXT PRIMARY KEY"));
        
        assertTrue(SQLQueries.Schema.CREATE_ALBUMS.contains("CREATE TABLE"));
        assertTrue(SQLQueries.Schema.CREATE_ALBUMS.contains("id TEXT PRIMARY KEY"));
        assertTrue(SQLQueries.Schema.CREATE_ALBUMS.contains("FOREIGN KEY"));
        
        assertTrue(SQLQueries.Schema.CREATE_SONGS.contains("CREATE TABLE"));
        assertTrue(SQLQueries.Schema.CREATE_SONGS.contains("id TEXT PRIMARY KEY"));
        assertTrue(SQLQueries.Schema.CREATE_SONGS.contains("FOREIGN KEY"));
        
        assertTrue(SQLQueries.Schema.CREATE_PLAYLISTS.contains("CREATE TABLE"));
        assertTrue(SQLQueries.Schema.CREATE_PLAYLISTS.contains("id TEXT PRIMARY KEY"));
        
        assertTrue(SQLQueries.Schema.CREATE_PLAYLIST_SONGS.contains("CREATE TABLE"));
        assertTrue(SQLQueries.Schema.CREATE_PLAYLIST_SONGS.contains("PRIMARY KEY"));
        assertTrue(SQLQueries.Schema.CREATE_PLAYLIST_SONGS.contains("FOREIGN KEY"));
    }
    
    /**
     * Tamamen şemayı oluşturup tüm sorguları çalıştırarak
     * entegrasyon testini gerçekleştirir
     */
    @Test
    public void testIntegratedSchema() throws SQLException {
        try (Connection conn = createInMemoryDatabase()) {
            // Şema oluşturma
            createTablesWithUsers(conn);
            
            // Şimdi tüm sorgular çalışmalı
            
            // Artist sorguları
            testArtistIntegration(conn);
            
            // Album sorguları
            testAlbumIntegration(conn);
            
            // Song sorguları
            testSongIntegration(conn);
            
            // Playlist sorguları
            testPlaylistIntegration(conn);
            
            // User sorguları
            testUserIntegration(conn);
        }
    }
    
    /**
     * Artist tablolarıyla ilgili sorgular entegrasyon testi
     */
    private void testArtistIntegration(Connection conn) throws SQLException {
        // Örnek bir artist ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Artist.INSERT)) {
            stmt.setString(1, "art1");
            stmt.setString(2, "Test Artist");
            stmt.setString(3, "Biography");
            stmt.executeUpdate();
        }
        
        // Artistle ilgili diğer sorguları test et
        testQuery(conn, SQLQueries.Artist.SELECT_BY_ID);
        testQuery(conn, SQLQueries.Artist.SELECT_ALL);
        testQuery(conn, SQLQueries.Artist.UPDATE);
    }
    
    /**
     * Album tablolarıyla ilgili sorgular entegrasyon testi
     */
    private void testAlbumIntegration(Connection conn) throws SQLException {
        // Örnek bir album ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Album.INSERT)) {
            stmt.setString(1, "alb1");
            stmt.setString(2, "Test Album");
            stmt.setString(3, "art1"); // Artist ID referansı
            stmt.setInt(4, 2023);
            stmt.setString(5, "Rock");
            stmt.executeUpdate();
        }
        
        // Albumle ilgili diğer sorguları test et
        testQuery(conn, SQLQueries.Album.SELECT_BY_ID);
        testQuery(conn, SQLQueries.Album.SELECT_ALL);
        testQuery(conn, SQLQueries.Album.UPDATE);
    }
    
    /**
     * Song tablolarıyla ilgili sorgular entegrasyon testi
     */
    private void testSongIntegration(Connection conn) throws SQLException {
        // Örnek bir şarkı ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Song.INSERT)) {
            stmt.setString(1, "s1");
            stmt.setString(2, "Test Song");
            stmt.setString(3, "art1"); // Artist ID referansı
            stmt.setString(4, "alb1"); // Album ID referansı
            stmt.setInt(5, 180); // 3 dakika
            stmt.setString(6, "Rock");
            stmt.executeUpdate();
        }
        
        // Şarkıyla ilgili diğer sorguları test et
        testQuery(conn, SQLQueries.Song.SELECT_BY_ID);
        testQuery(conn, SQLQueries.Song.SELECT_ALL);
        testQuery(conn, SQLQueries.Song.UPDATE);
    }
    
    /**
     * Playlist tablolarıyla ilgili sorgular entegrasyon testi
     */
    private void testPlaylistIntegration(Connection conn) throws SQLException {
        // Örnek bir playlist ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Playlist.INSERT)) {
            stmt.setString(1, "p1");
            stmt.setString(2, "Test Playlist");
            stmt.setString(3, "A test playlist");
            stmt.executeUpdate();
        }
        
        // Playliste şarkı ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Playlist.ADD_SONG)) {
            stmt.setString(1, "p1"); // Playlist ID
            stmt.setString(2, "s1"); // Song ID
            stmt.executeUpdate();
        }
        
        // Playlistle ilgili diğer sorguları test et
        testQuery(conn, SQLQueries.Playlist.SELECT_BY_ID);
        testQuery(conn, SQLQueries.Playlist.SELECT_ALL);
        testQuery(conn, SQLQueries.Playlist.UPDATE);
    }
    
    /**
     * User tablolarıyla ilgili sorgular entegrasyon testi
     */
    private void testUserIntegration(Connection conn) throws SQLException {
        // Örnek bir kullanıcı ekle
        try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.User.INSERT_OR_UPDATE)) {
            stmt.setString(1, "testuser");
            stmt.setString(2, "password123");
            stmt.executeUpdate();
        }
        
        // Kullanıcıyla ilgili diğer sorguları test et
        testQuery(conn, SQLQueries.User.SELECT_PASSWORD);
        testQuery(conn, SQLQueries.User.SELECT_ALL);
        testQuery(conn, SQLQueries.User.DELETE);
    }
    
    /**
     * Veritabanındaki gerekli tabloları oluşturur
     * @param conn Veritabanı bağlantısı
     * @throws SQLException Tablo oluşturma hatası durumunda
     */
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Tabloları sırasıyla oluşturuyoruz (foreign key referans sırası önemli)
            stmt.execute(SQLQueries.Schema.CREATE_ARTISTS);
            stmt.execute(SQLQueries.Schema.CREATE_ALBUMS);
            stmt.execute(SQLQueries.Schema.CREATE_SONGS);
            stmt.execute(SQLQueries.Schema.CREATE_PLAYLISTS);
            stmt.execute(SQLQueries.Schema.CREATE_PLAYLIST_SONGS);
        }
    }
    
    /**
     * Kullanıcı tablosu dahil tüm tabloları oluşturur
     * @param conn Veritabanı bağlantısı
     * @throws SQLException Tablo oluşturma hatası durumunda
     */
    private void createTablesWithUsers(Connection conn) throws SQLException {
        createTables(conn);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(SQLQueries.User.CREATE_TABLE);
        }
    }
    
    /**
     * Bir sorgunun geçerli olup olmadığını test eder
     * @param conn Veritabanı bağlantısı
     * @param query Test edilecek SQL sorgusu
     * @throws SQLException Sorgu geçersizse fırlatılır
     */
    private void testQuery(Connection conn, String query) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Sorgunun sadece geçerli olup olmadığını kontrol ediyoruz
            // PreparedStatement oluşturulabilirse sorgu geçerlidir
            assertNotNull(stmt);
        }
    }
    
    /**
     * Test için in-memory SQLite veritabanı oluşturur
     * @return Veritabanı bağlantısı
     * @throws SQLException Bağlantı kurulmazsa fırlatılır
     */
    private Connection createInMemoryDatabase() throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        // Foreign key desteğini aktif et
        Connection conn = dataSource.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
} 