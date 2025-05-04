package com.samet.music.dao;

import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * SongDAO için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class SongDAOMockitoTest {
    
    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private Statement mockStatement;
    @Mock private ResultSet mockResultSet;
    
    // Test edilecek nesne
    private SongDAO songDAO;
    private MockedStatic<DatabaseUtil> mockedDatabaseUtil;
    
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        
        songDAO = new SongDAO();
        
        // Mock DatabaseUtil.getConnection()
        mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        mockedDatabaseUtil.when(DatabaseUtil::getConnection).thenReturn(mockConnection);
    }
    
    @org.junit.After
    public void tearDown() {
        // Close the mocked static after each test
        if (mockedDatabaseUtil != null) {
            mockedDatabaseUtil.close();
        }
    }
    
    /**
     * Şarkı oluşturma metodunu test eder
     */
    @Test
    public void testCreate() throws SQLException {
        // Test verileri
        Song song = new Song();
        song.setTitle("Test Song");
        song.setArtist("Test Artist");
        song.setAlbum("Test Album");
        song.setGenre("Rock");
        song.setYear(2023);
        song.setDuration(180);
        song.setFilePath("/path/to/song.mp3");
        song.setUserId(1);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("last_insert_rowid()"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        
        // Metodu çağır
        Song result = songDAO.create(song);
        
        // Sonuçları doğrula
        assertNotNull("Oluşturulan şarkı null olmamalı", result);
        assertEquals("Şarkı ID doğru ayarlanmalı", 1, result.getId());
        assertEquals("Şarkı adı korunmalı", "Test Song", result.getTitle());
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, song.getTitle());
        verify(mockPreparedStatement).setString(2, song.getArtist());
        verify(mockPreparedStatement).setString(3, song.getAlbum());
        verify(mockPreparedStatement).setString(4, song.getGenre());
        verify(mockPreparedStatement).setInt(5, song.getYear());
        verify(mockPreparedStatement).setInt(6, song.getDuration());
        verify(mockPreparedStatement).setString(7, song.getFilePath());
        verify(mockPreparedStatement).setInt(8, song.getUserId());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Şarkı oluşturma metodunun SQLException durumunu test eder
     */
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Test verileri
        Song song = new Song();
        song.setTitle("Test Song");
        song.setArtist("Test Artist");
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Song result = songDAO.create(song);
        
        // Sonuçları doğrula
        assertNull("Hata durumunda null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        // Rollback ve auto-commit reset doğrulaması yapılmaz çünkü exception mockConnection.prepareStatement'ten fırlatılır
    }
    
    /**
     * ID ile şarkı bulma metodunu test eder
     */
    @Test
    public void testFindById() throws SQLException {
        // Test verileri
        int songId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(songId);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/song.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        
        // Metodu çağır
        Optional<Song> result = songDAO.findById(songId);
        
        // Sonuçları doğrula
        assertTrue("Şarkı bulunmalı", result.isPresent());
        assertEquals("Şarkı ID eşleşmeli", songId, result.get().getId());
        assertEquals("Şarkı adı eşleşmeli", "Test Song", result.get().getTitle());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, songId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * ID ile şarkı bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindByIdWithSQLException() throws SQLException {
        // Test verileri
        int songId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Optional<Song> result = songDAO.findById(songId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda boş Optional dönmeli", result.isPresent());
    }
    
    /**
     * Tüm şarkıları bulma metodunu test eder
     */
    @Test
    public void testFindAll() throws SQLException {
        // Mock davranışlarını ayarla
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Bir kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/song.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        
        // Metodu çağır
        List<Song> result = songDAO.findAll();
        
        // Sonuçları doğrula
        assertEquals("1 şarkı dönmeli", 1, result.size());
        assertEquals("Şarkı adı doğru olmalı", "Test Song", result.get(0).getTitle());
    }
    
    /**
     * Tüm şarkıları bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindAllWithSQLException() throws SQLException {
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.createStatement()).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        List<Song> result = songDAO.findAll();
        
        // Sonuçları doğrula
        assertTrue("Hata durumunda boş liste dönmeli", result.isEmpty());
    }
    
    /**
     * Kullanıcı ID'sine göre şarkı bulma metodunu test eder
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Bir kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/song.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(userId);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        
        // Metodu çağır
        List<Song> result = songDAO.findByUserId(userId);
        
        // Sonuçları doğrula
        assertEquals("1 şarkı dönmeli", 1, result.size());
        assertEquals("Şarkı adı doğru olmalı", "Test Song", result.get(0).getTitle());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Şarkı arama metodunu test eder
     */
    @Test
    public void testSearch() throws SQLException {
        // Test verileri
        String title = "Test";
        String artist = "Artist";
        String album = "Album";
        String genre = "Rock";
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Bir kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/song.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        
        // Metodu çağır
        List<Song> result = songDAO.search(title, artist, album, genre);
        
        // Sonuçları doğrula
        assertEquals("1 şarkı dönmeli", 1, result.size());
        assertEquals("Şarkı adı doğru olmalı", "Test Song", result.get(0).getTitle());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).executeQuery();
        // Parametre sayısı doğrulaması yapılabilir ama SQL dinamik olduğu için tam sayıyı belirlemek zor
    }
    
    /**
     * Şarkı güncelleme metodunu test eder
     */
    @Test
    public void testUpdate() throws SQLException {
        // Test verileri
        Song song = new Song();
        song.setId(1);
        song.setTitle("Updated Song");
        song.setArtist("Updated Artist");
        song.setAlbum("Updated Album");
        song.setGenre("Pop");
        song.setYear(2024);
        song.setDuration(200);
        song.setFilePath("/path/to/updated.mp3");
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = songDAO.update(song);
        
        // Sonuçları doğrula
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, song.getTitle());
        verify(mockPreparedStatement).setString(2, song.getArtist());
        verify(mockPreparedStatement).setString(3, song.getAlbum());
        verify(mockPreparedStatement).setString(4, song.getGenre());
        verify(mockPreparedStatement).setInt(5, song.getYear());
        verify(mockPreparedStatement).setInt(6, song.getDuration());
        verify(mockPreparedStatement).setString(7, song.getFilePath());
        verify(mockPreparedStatement).setInt(8, song.getId());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Şarkı güncelleme metodunun SQLException durumunu test eder
     */
    @Test
    public void testUpdateWithSQLException() throws SQLException {
        // Test verileri
        Song song = new Song();
        song.setId(1);
        song.setTitle("Updated Song");
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = songDAO.update(song);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Şarkı silme metodunu test eder
     */
    @Test
    public void testDelete() throws SQLException {
        // Test verileri
        int songId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = songDAO.delete(songId);
        
        // Sonuçları doğrula
        assertTrue("Silme işlemi başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, songId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Şarkı silme metodunun SQLException durumunu test eder
     */
    @Test
    public void testDeleteWithSQLException() throws SQLException {
        // Test verileri
        int songId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = songDAO.delete(songId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Sanatçıya göre şarkı bulma metodunu test eder
     */
    @Test
    public void testFindByArtist() throws SQLException {
        // Test verileri
        String artist = "Test Artist";
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Bir kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn(artist);
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/song.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        
        // Metodu çağır
        List<Song> result = songDAO.findByArtist(artist);
        
        // Sonuçları doğrula
        assertEquals("1 şarkı dönmeli", 1, result.size());
        assertEquals("Şarkı sanatçısı doğru olmalı", artist, result.get(0).getArtist());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, artist);
        verify(mockPreparedStatement).executeQuery();
    }
} 