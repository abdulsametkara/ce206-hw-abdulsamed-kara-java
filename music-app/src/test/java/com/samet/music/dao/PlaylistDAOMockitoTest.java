package com.samet.music.dao;

import com.samet.music.model.Playlist;
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
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * PlaylistDAO için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class PlaylistDAOMockitoTest {
    
    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private Statement mockStatement;
    @Mock private ResultSet mockResultSet;
    @Mock private SongDAO mockSongDAO;
    
    // Test edilecek nesne
    private PlaylistDAO playlistDAO;
    private MockedStatic<DatabaseUtil> mockedDatabaseUtil;
    
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        
        // Use constructor with only SongDAO parameter
        playlistDAO = new PlaylistDAO(mockSongDAO);
        
        // Mock DatabaseUtil.getConnection()
        mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        mockedDatabaseUtil.when(DatabaseUtil::getConnection).thenReturn(mockConnection);
        
        // Mock getSongsByPlaylistId için kullanılacak ikinci bir PreparedStatement
        PreparedStatement mockPreparedStatement2 = mock(PreparedStatement.class);
        ResultSet mockResultSet2 = mock(ResultSet.class);
        
        // İkinci preparedStatement ve resultSet için ayarlar
        when(mockConnection.prepareStatement(contains("playlist_songs"))).thenReturn(mockPreparedStatement2);
        when(mockPreparedStatement2.executeQuery()).thenReturn(mockResultSet2);
        when(mockResultSet2.next()).thenReturn(false); // Şarkı yok
    }
    
    @org.junit.After
    public void tearDown() {
        // Close the mocked static after each test
        if (mockedDatabaseUtil != null) {
            mockedDatabaseUtil.close();
        }
    }
    
    /**
     * Playlist oluşturma metodunu test eder
     */
    @Test
    public void testCreate() throws SQLException {
        // Test verileri
        Playlist playlist = new Playlist();
        playlist.setName("Test Playlist");
        playlist.setUserId(1);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("last_insert_rowid()"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        
        // Metodu çağır
        Playlist result = playlistDAO.create(playlist);
        
        // Sonuçları doğrula
        assertNotNull("Oluşturulan playlist null olmamalı", result);
        assertEquals("Playlist ID doğru ayarlanmalı", 1, result.getId());
        assertEquals("Playlist adı korunmalı", "Test Playlist", result.getName());
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, playlist.getName());
        verify(mockPreparedStatement).setInt(2, playlist.getUserId());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Playlist oluşturma metodunun SQLException durumunu test eder
     */
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Test verileri
        Playlist playlist = new Playlist();
        playlist.setName("Test Playlist");
        playlist.setUserId(1);
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Playlist result = playlistDAO.create(playlist);
        
        // Sonuçları doğrula
        assertNull("Hata durumunda null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * ID ile playlist bulma metodunu test eder
     */
    @Test
    public void testFindById() throws SQLException {
        // Test verileri
        int playlistId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock davranışlarını ayarla - iki farklı preparedStatement için query ayırımı
        when(mockConnection.prepareStatement(contains("playlists WHERE id"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // İlk çağrıda true, sonra false
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(playlistId);
        when(mockResultSet.getString("name")).thenReturn("Test Playlist");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // getSongsByPlaylistId için mock çağrıyı kurulmayacak, setUp'ta yapıldı
        
        // Metodu çağır
        Optional<Playlist> result = playlistDAO.findById(playlistId);
        
        // Sonuçları doğrula
        assertTrue("Playlist bulunmalı", result.isPresent());
        assertEquals("Playlist ID eşleşmeli", playlistId, result.get().getId());
        assertEquals("Playlist adı eşleşmeli", "Test Playlist", result.get().getName());
        
        // Etkileşimleri doğrula - artık mockPreparedStatement.setInt sadece bir kez çağrılmalı
        verify(mockPreparedStatement).setInt(1, playlistId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * ID ile playlist bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindByIdWithSQLException() throws SQLException {
        // Test verileri
        int playlistId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Optional<Playlist> result = playlistDAO.findById(playlistId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda boş Optional dönmeli", result.isPresent());
    }
    
    /**
     * Kullanıcı ID'sine göre playlist bulma metodunu test eder
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(contains("playlists WHERE user_id"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // Sadece bir sonuç dönecek şekilde ayarla
        when(mockResultSet.next()).thenReturn(true, false); // Tek kayıt var
        
        // İlk kayıt için
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Playlist 1");
        when(mockResultSet.getInt("user_id")).thenReturn(userId);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Metodu çağır
        List<Playlist> result = playlistDAO.findByUserId(userId);
        
        // Sonuçları doğrula - bir playlist dönecek şekilde düzeltildi
        assertEquals("1 playlist dönmeli", 1, result.size());
        assertEquals("Playlist adı doğru olmalı", "Playlist 1", result.get(0).getName());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Kullanıcı ID'sine göre playlist bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindByUserIdWithSQLException() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        List<Playlist> result = playlistDAO.findByUserId(userId);
        
        // Sonuçları doğrula
        assertTrue("Hata durumunda boş liste dönmeli", result.isEmpty());
    }
    
    /**
     * Tüm playlistleri bulma metodunu test eder
     */
    @Test
    public void testFindAll() throws SQLException {
        // Mock davranışlarını ayarla
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Tek kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Playlist 1");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Metodu çağır
        List<Playlist> result = playlistDAO.findAll();
        
        // Sonuçları doğrula - tek playlist dönecek şekilde düzeltildi
        assertEquals("1 playlist dönmeli", 1, result.size());
        assertEquals("Playlist adı doğru olmalı", "Playlist 1", result.get(0).getName());
    }
    
    /**
     * Tüm playlistleri bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindAllWithSQLException() throws SQLException {
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.createStatement()).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        List<Playlist> result = playlistDAO.findAll();
        
        // Sonuçları doğrula
        assertTrue("Hata durumunda boş liste dönmeli", result.isEmpty());
    }
    
    /**
     * Playlist güncelleme metodunu test eder
     */
    @Test
    public void testUpdate() throws SQLException {
        // Test verileri
        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setName("Updated Playlist");
        playlist.setUserId(2);
        playlist.setSongs(null); // Şarkı eklemek istemiyoruz
        
        // Mock davranışlarını ayarla - UPDATE için
        when(mockConnection.prepareStatement(contains("UPDATE playlists"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = playlistDAO.update(playlist);
        
        // Sonuçları doğrula
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, playlist.getName());
        verify(mockPreparedStatement).setInt(2, playlist.getUserId());
        verify(mockPreparedStatement).setInt(3, playlist.getId());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Playlist güncelleme metodunun SQLException durumunu test eder
     */
    @Test
    public void testUpdateWithSQLException() throws SQLException {
        // Test verileri
        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setName("Updated Playlist");
        playlist.setUserId(2);
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = playlistDAO.update(playlist);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Playlist silme metodunu test eder
     */
    @Test
    public void testDelete() throws SQLException {
        // Test verileri
        int playlistId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = playlistDAO.delete(playlistId);
        
        // Sonuçları doğrula
        assertTrue("Silme işlemi başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement, times(2)).setInt(1, playlistId); // İki kez çağrılır (hem şarkıları silme hem playlist silme)
        verify(mockPreparedStatement, times(2)).executeUpdate(); // İki kez çağrılır
    }
    
    /**
     * Playlist silme metodunun SQLException durumunu test eder
     */
    @Test
    public void testDeleteWithSQLException() throws SQLException {
        // Test verileri
        int playlistId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = playlistDAO.delete(playlistId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Playliste şarkı ekleme metodunu test eder
     */
    @Test
    public void testAddSongsToPlaylist() throws SQLException {
        // Test verileri
        int playlistId = 1;
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song();
        song1.setId(1);
        Song song2 = new Song();
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1}); // İki şarkı da başarıyla eklendi
        
        // Metodu çağır
        boolean result = playlistDAO.addSongsToPlaylist(playlistId, songs);
        
        // Sonuçları doğrula
        assertTrue("Şarkı ekleme işlemi başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement, times(2)).setInt(1, playlistId);
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setInt(2, 2);
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement).executeBatch();
    }
    
    /**
     * Playlistten şarkı silme metodunu test eder
     */
    @Test
    public void testRemoveSongsFromPlaylist() throws SQLException {
        // Test verileri
        int playlistId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(2); // 2 şarkı silindi
        
        // Metodu çağır
        boolean result = playlistDAO.removeSongsFromPlaylist(playlistId);
        
        // Sonuçları doğrula
        assertTrue("Şarkı silme işlemi başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, playlistId);
        verify(mockPreparedStatement).executeUpdate();
    }
} 