package com.samet.music.dao;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

/**
 * Test class for PlaylistDAO that mocks database access but tests actual code paths
 * This approach provides better code coverage without database access issues
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class PlaylistDAOMockTest {
    
    private PlaylistDAO playlistDAO;
    
    @Mock
    private SongDAO songDAO;
    
    @Mock
    private Connection mockConn;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Before
    public void setUp() throws Exception {
        // Create a real DAO with the mocked dependencies
        playlistDAO = new PlaylistDAO(songDAO);
    }
    
    private void setupMockConnection() throws SQLException {
        // Setup the mock connection
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConn.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    }
    
    @Test
    public void testAddPlaylistValidInput() throws SQLException {
        // Test with valid input data
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = playlistDAO.addPlaylist("Test Playlist", "Test Description", 1);
            
            // Verify
            assertTrue("Should return true for valid input", result);
            verify(mockPreparedStatement).setString(1, "Test Playlist");
            verify(mockPreparedStatement).setString(2, "Test Description");
            verify(mockPreparedStatement).setInt(3, 1);
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testAddPlaylistInvalidInput() {
        // Test with invalid input data
        boolean nullNameResult = playlistDAO.addPlaylist(null, "Description", 1);
        assertFalse("Should return false for null name", nullNameResult);
        
        boolean emptyNameResult = playlistDAO.addPlaylist("", "Description", 1);
        assertFalse("Should return false for empty name", emptyNameResult);
        
        boolean invalidUserIdResult = playlistDAO.addPlaylist("Test", "Description", -1);
        assertFalse("Should return false for invalid user ID", invalidUserIdResult);
    }
    
    @Test
    public void testAddPlaylistDeprecated() {
        // Test deprecated method
        boolean result = playlistDAO.addPlaylist("Test", "Description");
        assertFalse("Deprecated method should return false", result);
    }
    
    @Test
    public void testUpdatePlaylistValidInput() throws SQLException {
        // Test with valid input data
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = playlistDAO.updatePlaylist("Old Name", "New Name");
            
            // Verify
            assertTrue("Should return true for valid input", result);
            verify(mockPreparedStatement).setString(1, "New Name");
            verify(mockPreparedStatement).setString(2, "Old Name");
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testUpdatePlaylistInvalidInput() {
        // Test with invalid input data
        boolean nullNamesResult = playlistDAO.updatePlaylist(null, "New Name");
        assertFalse("Should return false for null old name", nullNamesResult);
        
        boolean emptyOldNameResult = playlistDAO.updatePlaylist("", "New Name");
        assertFalse("Should return false for empty old name", emptyOldNameResult);
        
        boolean nullNewNameResult = playlistDAO.updatePlaylist("Old Name", null);
        assertFalse("Should return false for null new name", nullNewNameResult);
        
        boolean emptyNewNameResult = playlistDAO.updatePlaylist("Old Name", "");
        assertFalse("Should return false for empty new name", emptyNewNameResult);
    }
    
    @Test
    public void testDeletePlaylistValidInput() throws SQLException {
        // Test with valid input data
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = playlistDAO.deletePlaylist("Test Playlist");
            
            // Verify
            assertTrue("Should return true for valid input", result);
            verify(mockPreparedStatement).setString(1, "Test Playlist");
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testDeletePlaylistInvalidInput() {
        // Test with invalid input data
        boolean nullNameResult = playlistDAO.deletePlaylist(null);
        assertFalse("Should return false for null name", nullNameResult);
        
        boolean emptyNameResult = playlistDAO.deletePlaylist("");
        assertFalse("Should return false for empty name", emptyNameResult);
    }
    
    @Test
    public void testGetAllPlaylists() throws SQLException {
        // Setup mock data for getAllPlaylists
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString("name")).thenReturn("Test Playlist");
            when(mockResultSet.getInt("song_count")).thenReturn(5);
            when(mockResultSet.getString("created_at")).thenReturn("2023-01-01");
            
            // Execute method under test
            List<String[]> result = playlistDAO.getAllPlaylists();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 result", 1, result.size());
            assertEquals("Should return playlist name", "Test Playlist", result.get(0)[0]);
            assertEquals("Should return song count", "5", result.get(0)[1]);
            assertEquals("Should return created date", "2023-01-01", result.get(0)[2]);
        }
    }
    
    @Test
    public void testCreatePlaylist() throws SQLException {
        // Test creating a playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Setup mock ResultSet to return a generated key
            ResultSet mockKeysResultSet = mock(ResultSet.class);
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockKeysResultSet);
            when(mockKeysResultSet.next()).thenReturn(true);
            when(mockKeysResultSet.getInt(1)).thenReturn(1);
            
            // Create test playlist
            Playlist playlist = new Playlist("Test", "Description", 1);
            
            // Execute method under test
            Playlist result = playlistDAO.create(playlist);
            
            // Verify
            assertNotNull("Should return non-null playlist", result);
            assertEquals("Should have ID 1", 1, result.getId());
            verify(mockPreparedStatement).setString(1, playlist.getName());
            verify(mockPreparedStatement).setString(2, playlist.getDescription());
            verify(mockPreparedStatement).setInt(3, playlist.getUserId());
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testCreatePlaylistWithSongs() throws SQLException {
        // Test creating a playlist with songs
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});
            
            // Setup mock ResultSet to return a generated key
            ResultSet mockKeysResultSet = mock(ResultSet.class);
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockKeysResultSet);
            when(mockKeysResultSet.next()).thenReturn(true);
            when(mockKeysResultSet.getInt(1)).thenReturn(1);
            
            // Create test playlist with songs
            Playlist playlist = new Playlist("Test", "Description", 1);
            Song song = new Song();
            song.setId(1);
            playlist.addSong(song);
            
            // Execute method under test
            Playlist result = playlistDAO.create(playlist);
            
            // Verify
            assertNotNull("Should return non-null playlist", result);
            assertEquals("Should have ID 1", 1, result.getId());
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testFindById() throws SQLException {
        // Test finding a playlist by ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with playlist data
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("name")).thenReturn("Test Playlist");
            when(mockResultSet.getString("description")).thenReturn("Test Description");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Setup empty songs list
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(songDAO.mapResultSetToSong(any(ResultSet.class))).thenReturn(new Song());
            
            // Execute method under test
            Optional<Playlist> result = playlistDAO.findById(1);
            
            // Verify
            assertTrue("Should return present Optional", result.isPresent());
            assertEquals("Should have ID 1", 1, result.get().getId());
            assertEquals("Should have name 'Test Playlist'", "Test Playlist", result.get().getName());
            
            // The method is called twice - once in findById and once in getSongsByPlaylistId
            verify(mockPreparedStatement, times(2)).setInt(1, 1);
        }
    }
    
    @Test
    public void testFindByIdInvalid() throws SQLException {
        // Test finding a playlist with invalid ID
        Optional<Playlist> result = playlistDAO.findById(-1);
        assertFalse("Should return empty Optional for invalid ID", result.isPresent());
        
        // Test finding a non-existent playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            Optional<Playlist> notFoundResult = playlistDAO.findById(999);
            
            // Verify
            assertFalse("Should return empty Optional for non-existent ID", notFoundResult.isPresent());
            verify(mockPreparedStatement).setInt(1, 999);
        }
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Test finding playlists by user ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with playlist data
            when(mockResultSet.next()).thenReturn(true, false, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("name")).thenReturn("Test Playlist");
            when(mockResultSet.getString("description")).thenReturn("Test Description");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Setup empty songs list
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(songDAO.mapResultSetToSong(any(ResultSet.class))).thenReturn(new Song());
            
            // Execute method under test
            List<Playlist> result = playlistDAO.findByUserId(1);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 result", 1, result.size());
            assertEquals("Should have name 'Test Playlist'", "Test Playlist", result.get(0).getName());
            
            // The method is called twice - once in findByUserId and once in getSongsByPlaylistId
            verify(mockPreparedStatement, times(2)).setInt(1, 1);
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        // Test finding all playlists
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with playlist data
            when(mockResultSet.next()).thenReturn(true, false, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("name")).thenReturn("Test Playlist");
            when(mockResultSet.getString("description")).thenReturn("Test Description");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Setup empty songs list
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(songDAO.mapResultSetToSong(any(ResultSet.class))).thenReturn(new Song());
            
            // Execute method under test
            List<Playlist> result = playlistDAO.findAll();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 result", 1, result.size());
            assertEquals("Should have name 'Test Playlist'", "Test Playlist", result.get(0).getName());
        }
    }
    
    @Test
    public void testUpdate() throws SQLException {
        // Test updating a playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Create test playlist
            Playlist playlist = new Playlist(1, "Updated Name", "Updated Description", 1, LocalDateTime.now());
            
            // Execute method under test
            boolean result = playlistDAO.update(playlist);
            
            // Verify
            assertTrue("Should return true for successful update", result);
            verify(mockPreparedStatement).setString(1, playlist.getName());
            verify(mockPreparedStatement).setString(2, playlist.getDescription());
            verify(mockPreparedStatement).setInt(3, playlist.getId());
            // executeUpdate is called twice, once in update and once in removeSongsFromPlaylist
            verify(mockPreparedStatement, times(2)).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testUpdateWithSongs() throws SQLException {
        // Test updating a playlist with songs
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});
            
            // Create test playlist with songs
            Playlist playlist = new Playlist(1, "Updated Name", "Updated Description", 1, LocalDateTime.now());
            Song song = new Song();
            song.setId(1);
            playlist.addSong(song);
            
            // Execute method under test
            boolean result = playlistDAO.update(playlist);
            
            // Verify
            assertTrue("Should return true for successful update", result);
            verify(mockPreparedStatement).setString(1, playlist.getName());
            verify(mockPreparedStatement).setString(2, playlist.getDescription());
            verify(mockPreparedStatement).setInt(3, playlist.getId());
            // executeUpdate is called twice, once in update and once in removeSongsFromPlaylist
            verify(mockPreparedStatement, times(2)).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testUpdateInvalidInput() {
        // Test updating with invalid input data
        Playlist nullNamePlaylist = new Playlist(1, null, "Description", 1, LocalDateTime.now());
        boolean nullNameResult = playlistDAO.update(nullNamePlaylist);
        assertFalse("Should return false for null name", nullNameResult);
        
        Playlist emptyNamePlaylist = new Playlist(1, "", "Description", 1, LocalDateTime.now());
        boolean emptyNameResult = playlistDAO.update(emptyNamePlaylist);
        assertFalse("Should return false for empty name", emptyNameResult);
        
        Playlist invalidIdPlaylist = new Playlist(0, "Name", "Description", 1, LocalDateTime.now());
        boolean invalidIdResult = playlistDAO.update(invalidIdPlaylist);
        assertFalse("Should return false for invalid ID", invalidIdResult);
    }
    
    @Test
    public void testDelete() throws SQLException {
        // Test deleting a playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = playlistDAO.delete(1);
            
            // Verify
            assertTrue("Should return true for successful deletion", result);
            // setInt is called twice, once in removeSongsFromPlaylist and once in delete
            verify(mockPreparedStatement, times(2)).setInt(1, 1);
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testDeleteInvalidInput() {
        // Test deleting with invalid ID
        boolean invalidIdResult = playlistDAO.delete(0);
        assertFalse("Should return false for invalid ID", invalidIdResult);
        
        boolean negativeIdResult = playlistDAO.delete(-1);
        assertFalse("Should return false for negative ID", negativeIdResult);
    }
    
    @Test
    public void testAddSongsToPlaylist() throws SQLException {
        // Test adding songs to a playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1});
            
            // Create test songs
            List<Song> songs = new ArrayList<>();
            Song song = new Song();
            song.setId(1);
            songs.add(song);
            
            // Execute method under test
            boolean result = playlistDAO.addSongsToPlaylist(1, songs);
            
            // Verify
            assertTrue("Should return true for successful addition", result);
            verify(mockPreparedStatement).setInt(1, 1); // playlist_id
            verify(mockPreparedStatement).setInt(2, 1); // song_id
            verify(mockPreparedStatement).setInt(3, 0); // position
            verify(mockPreparedStatement).addBatch();
            verify(mockPreparedStatement).executeBatch();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testAddSongsToPlaylistInvalidInput() {
        // Test adding songs with invalid input
        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        song.setId(1);
        songs.add(song);
        
        boolean invalidIdResult = playlistDAO.addSongsToPlaylist(0, songs);
        assertFalse("Should return false for invalid ID", invalidIdResult);
        
        boolean nullSongsResult = playlistDAO.addSongsToPlaylist(1, null);
        assertFalse("Should return false for null songs list", nullSongsResult);
        
        boolean emptySongsResult = playlistDAO.addSongsToPlaylist(1, Collections.emptyList());
        assertFalse("Should return false for empty songs list", emptySongsResult);
    }
    
    @Test
    public void testRemoveSongsFromPlaylist() throws SQLException {
        // Test removing songs from a playlist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = playlistDAO.removeSongsFromPlaylist(1);
            
            // Verify
            assertTrue("Should return true for successful removal", result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testSqlExceptionHandling() throws SQLException {
        // Test handling SQLException in addPlaylist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            // Set up to throw exception and then no exception for rollback
            when(mockPreparedStatement.executeUpdate())
                .thenThrow(new SQLException("Test exception"))
                .thenReturn(0); // For rollback call
            
            // Execute method under test
            boolean result = playlistDAO.addPlaylist("Test", "Description", 1);
            
            // Verify
            assertFalse("Should return false when SQLException occurs", result);
        }
        
        // Test handling SQLException in updatePlaylist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate())
                .thenThrow(new SQLException("Test exception"))
                .thenReturn(0); // For rollback call
            
            // Execute method under test
            boolean result = playlistDAO.updatePlaylist("Old Name", "New Name");
            
            // Verify
            assertFalse("Should return false when SQLException occurs", result);
        }
        
        // Test handling SQLException in deletePlaylist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate())
                .thenThrow(new SQLException("Test exception"))
                .thenReturn(0); // For rollback call
            
            // Execute method under test
            boolean result = playlistDAO.deletePlaylist("Test");
            
            // Verify
            assertFalse("Should return false when SQLException occurs", result);
        }
    }
} 