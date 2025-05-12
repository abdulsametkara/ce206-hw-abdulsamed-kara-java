package com.samet.music.dao;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for UserSongStatisticsDAO that mocks database access
 * This approach provides better code coverage without database access issues
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class UserSongStatisticsDAOMockTest {

    private UserSongStatisticsDAO userSongStatisticsDAO;
    
    @Mock
    private Connection mockConn;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    private final static int TEST_USER_ID = 1;
    private final static int TEST_SONG_ID = 10;
    
    @Before
    public void setUp() throws Exception {
        // We'll create the DAO object in each test since we need to mock DriverManager
        // which requires a MockedStatic resource that must be closed
    }
    
    private void setupMockConnection() throws SQLException {
        // Setup the mock connection
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConn.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockStatement.execute(anyString())).thenReturn(true);
    }
    
    @Test
    public void testIncrementPlayCount() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.incrementPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertTrue("Should return true for successful increment", result);
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, TEST_SONG_ID);
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testIncrementPlayCountFailed() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.incrementPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertFalse("Should return false when no rows affected", result);
        }
    }
    
    @Test
    public void testIncrementPlayCountException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeUpdate();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.incrementPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertFalse("Should return false when an exception occurs", result);
        }
    }
    
    @Test
    public void testSetFavorite() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.setFavorite(TEST_USER_ID, TEST_SONG_ID, true);
            
            // Verify
            assertTrue("Should return true for successful favorite setting", result);
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, TEST_SONG_ID);
            verify(mockPreparedStatement).setBoolean(3, true);
            verify(mockPreparedStatement).setBoolean(4, true);
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testSetFavoriteFailed() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.setFavorite(TEST_USER_ID, TEST_SONG_ID, false);
            
            // Verify
            assertFalse("Should return false when no rows affected", result);
        }
    }
    
    @Test
    public void testSetFavoriteException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeUpdate();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.setFavorite(TEST_USER_ID, TEST_SONG_ID, true);
            
            // Verify
            assertFalse("Should return false when an exception occurs", result);
        }
    }
    
    @Test
    public void testIsFavoriteTrue() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getBoolean("favorite")).thenReturn(true);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.isFavorite(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertTrue("Should return true for favorite song", result);
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, TEST_SONG_ID);
        }
    }
    
    @Test
    public void testIsFavoriteFalse() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getBoolean("favorite")).thenReturn(false);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.isFavorite(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertFalse("Should return false for non-favorite song", result);
        }
    }
    
    @Test
    public void testIsFavoriteNotFound() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No record found
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.isFavorite(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertFalse("Should return false when record not found", result);
        }
    }
    
    @Test
    public void testIsFavoriteException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            boolean result = userSongStatisticsDAO.isFavorite(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertFalse("Should return false when an exception occurs", result);
        }
    }
    
    @Test
    public void testGetPlayCount() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("play_count")).thenReturn(42);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            int result = userSongStatisticsDAO.getPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertEquals("Should return correct play count", 42, result);
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, TEST_SONG_ID);
        }
    }
    
    @Test
    public void testGetPlayCountNotFound() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No record found
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            int result = userSongStatisticsDAO.getPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertEquals("Should return 0 when record not found", 0, result);
        }
    }
    
    @Test
    public void testGetPlayCountException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            int result = userSongStatisticsDAO.getPlayCount(TEST_USER_ID, TEST_SONG_ID);
            
            // Verify
            assertEquals("Should return 0 when an exception occurs", 0, result);
        }
    }
    
    @Test
    public void testGetMostPlayedSongs() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true, true, true, false); // 3 songs
            when(mockResultSet.getInt("song_id")).thenReturn(10, 20, 30);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 5);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 3 songs", 3, result.size());
            assertEquals("First song ID should match", Integer.valueOf(10), result.get(0));
            assertEquals("Second song ID should match", Integer.valueOf(20), result.get(1));
            assertEquals("Third song ID should match", Integer.valueOf(30), result.get(2));
            
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, 5); // Limit
        }
    }
    
    @Test
    public void testGetMostPlayedSongsEmpty() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No songs
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 5);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list", result.isEmpty());
        }
    }
    
    @Test
    public void testGetMostPlayedSongsException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getMostPlayedSongs(TEST_USER_ID, 5);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list when an exception occurs", result.isEmpty());
        }
    }
    
    @Test
    public void testGetFavoriteSongs() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true, true, false); // 2 favorite songs
            when(mockResultSet.getInt("song_id")).thenReturn(10, 20);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 songs", 2, result.size());
            assertEquals("First song ID should match", Integer.valueOf(10), result.get(0));
            assertEquals("Second song ID should match", Integer.valueOf(20), result.get(1));
            
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
        }
    }
    
    @Test
    public void testGetFavoriteSongsEmpty() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No favorite songs
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list", result.isEmpty());
        }
    }
    
    @Test
    public void testGetFavoriteSongsException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getFavoriteSongs(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list when an exception occurs", result.isEmpty());
        }
    }
    
    @Test
    public void testGetRecentlyPlayedSongs() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true, true, false); // 2 recently played songs
            when(mockResultSet.getInt("song_id")).thenReturn(10, 20);
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getRecentlyPlayedSongs(TEST_USER_ID, 10);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 songs", 2, result.size());
            assertEquals("First song ID should match", Integer.valueOf(10), result.get(0));
            assertEquals("Second song ID should match", Integer.valueOf(20), result.get(1));
            
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
            verify(mockPreparedStatement).setInt(2, 10); // Limit
        }
    }
    
    @Test
    public void testGetRecentlyPlayedSongsEmpty() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No recently played songs
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getRecentlyPlayedSongs(TEST_USER_ID, 10);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list", result.isEmpty());
        }
    }
    
    @Test
    public void testGetRecentlyPlayedSongsException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            List<Integer> result = userSongStatisticsDAO.getRecentlyPlayedSongs(TEST_USER_ID, 10);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list when an exception occurs", result.isEmpty());
        }
    }
    
    @Test
    public void testGetUserStatistics() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true);
            
            // Setup statistics data
            when(mockResultSet.getInt("total_songs")).thenReturn(10);
            when(mockResultSet.getInt("total_plays")).thenReturn(150);
            when(mockResultSet.getInt("favorite_count")).thenReturn(5);
            LocalDateTime lastPlayed = LocalDateTime.now().minusHours(2);
            when(mockResultSet.getTimestamp("last_played")).thenReturn(Timestamp.valueOf(lastPlayed));
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            Map<String, Object> result = userSongStatisticsDAO.getUserStatistics(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null map", result);
            assertEquals("Should have correct total songs", 10, result.get("total_songs"));
            assertEquals("Should have correct total plays", 150, result.get("total_plays"));
            assertEquals("Should have correct favorite count", 5, result.get("favorite_count"));
            assertEquals("Should have last played time", lastPlayed, result.get("last_played"));
            
            verify(mockPreparedStatement).setInt(1, TEST_USER_ID);
        }
    }
    
    @Test
    public void testGetUserStatisticsNullLastPlayed() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true);
            
            // Setup statistics data
            when(mockResultSet.getInt("total_songs")).thenReturn(10);
            when(mockResultSet.getInt("total_plays")).thenReturn(150);
            when(mockResultSet.getInt("favorite_count")).thenReturn(5);
            when(mockResultSet.getTimestamp("last_played")).thenReturn(null); // Null last played
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            Map<String, Object> result = userSongStatisticsDAO.getUserStatistics(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null map", result);
            assertEquals("Should have correct total songs", 10, result.get("total_songs"));
            assertEquals("Should have correct total plays", 150, result.get("total_plays"));
            assertEquals("Should have correct favorite count", 5, result.get("favorite_count"));
            assertFalse("Should not have last_played key", result.containsKey("last_played"));
        }
    }
    
    @Test
    public void testGetUserStatisticsNoData() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false); // No data found
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            Map<String, Object> result = userSongStatisticsDAO.getUserStatistics(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null map", result);
            assertTrue("Should return empty map", result.isEmpty());
        }
    }
    
    @Test
    public void testGetUserStatisticsException() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConn);
            
            setupMockConnection();
            doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeQuery();
            
            // Create DAO instance
            userSongStatisticsDAO = new UserSongStatisticsDAO();
            
            // Execute method under test
            Map<String, Object> result = userSongStatisticsDAO.getUserStatistics(TEST_USER_ID);
            
            // Verify
            assertNotNull("Should return non-null map", result);
            assertTrue("Should return empty map when an exception occurs", result.isEmpty());
        }
    }
} 