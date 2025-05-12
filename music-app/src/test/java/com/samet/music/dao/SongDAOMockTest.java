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
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

/**
 * Test class for SongDAO that mocks database access
 * This approach provides better code coverage without database access issues
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SongDAOMockTest {

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
        songDAO = new SongDAO(mockConn);
    }
    
    private void setupMockConnection() throws SQLException {
        // Setup the mock connection
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConn.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    }
    
    private Song createTestSong() {
        Song song = new Song();
        song.setId(1);
        song.setTitle("Test Song");
        song.setArtist("Test Artist");
        song.setAlbum("Test Album");
        song.setGenre("Test Genre");
        song.setYear(2023);
        song.setDuration(180);
        song.setFilePath("/path/to/test.mp3");
        song.setUserId(1);
        song.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return song;
    }
    
    @Test
    public void testAddSong() throws SQLException {
        // Test adding a song using the addSong method
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            songDAO.addSong("Test Song", "Test Artist", "Test Album", "Test Genre");
            
            // Verify
            verify(mockPreparedStatement).setString(1, "Test Song");
            verify(mockPreparedStatement).setString(2, "Test Artist");
            verify(mockPreparedStatement).setString(3, "Test Album");
            verify(mockPreparedStatement).setString(4, "Test Genre");
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testGetAllSongs() throws SQLException {
        // Test retrieving all songs
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(true, true, false); // Return two songs
            when(mockResultSet.getString("title")).thenReturn("Song 1", "Song 2");
            when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
            when(mockResultSet.getString("album")).thenReturn("Album 1", "Album 2");
            when(mockResultSet.getString("genre")).thenReturn("Genre 1", "Genre 2");
            
            // Execute method under test
            List<String[]> result = songDAO.getAllSongs();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 songs", 2, result.size());
            assertEquals("First song title should match", "Song 1", result.get(0)[0]);
            assertEquals("Second song artist should match", "Artist 2", result.get(1)[1]);
        }
    }
    
    @Test
    public void testDeleteSong() throws SQLException {
        // Test deleting a song
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            songDAO.deleteSong("Test Song", "Test Artist", "Test Album");
            
            // Verify
            verify(mockPreparedStatement).setString(1, "Test Song");
            verify(mockPreparedStatement).setString(2, "Test Artist");
            verify(mockPreparedStatement).setString(3, "Test Album");
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testCreate() throws SQLException {
        // Test creating a song
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
            
            // Create test song
            Song song = createTestSong();
            
            // Execute method under test
            Song result = songDAO.create(song);
            
            // Verify
            assertNotNull("Should return non-null song", result);
            assertEquals("Should have ID 1", 1, result.getId());
            verify(mockPreparedStatement).setString(1, song.getTitle());
            verify(mockPreparedStatement).setString(2, song.getArtist());
            verify(mockPreparedStatement).setString(3, song.getAlbum());
            verify(mockPreparedStatement).setString(4, song.getGenre());
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testFindById() throws SQLException {
        // Test finding a song by ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("title")).thenReturn("Test Song");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist");
            when(mockResultSet.getString("album")).thenReturn("Test Album");
            when(mockResultSet.getString("genre")).thenReturn("Test Genre");
            when(mockResultSet.getInt("year")).thenReturn(2023);
            when(mockResultSet.getInt("duration")).thenReturn(180);
            when(mockResultSet.getString("file_path")).thenReturn("/path/to/test.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            Optional<Song> result = songDAO.findById(1);
            
            // Verify
            assertTrue("Should return present Optional", result.isPresent());
            assertEquals("Should have ID 1", 1, result.get().getId());
            assertEquals("Should have title 'Test Song'", "Test Song", result.get().getTitle());
            
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }
    
    @Test
    public void testFindByIdNotFound() throws SQLException {
        // Test finding a song that doesn't exist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            Optional<Song> result = songDAO.findById(999);
            
            // Verify
            assertFalse("Should return empty Optional", result.isPresent());
            verify(mockPreparedStatement).setInt(1, 999);
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        // Test finding all songs
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("title")).thenReturn("Test Song 1", "Test Song 2");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist 1", "Test Artist 2");
            when(mockResultSet.getString("album")).thenReturn("Test Album 1", "Test Album 2");
            when(mockResultSet.getString("genre")).thenReturn("Test Genre 1", "Test Genre 2");
            when(mockResultSet.getInt("year")).thenReturn(2023, 2022);
            when(mockResultSet.getInt("duration")).thenReturn(180, 240);
            when(mockResultSet.getString("file_path")).thenReturn("/path/to/test1.mp3", "/path/to/test2.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1, 2);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(
                    Timestamp.valueOf(LocalDateTime.now()), 
                    Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
            
            // Execute method under test
            List<Song> result = songDAO.findAll();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 songs", 2, result.size());
            assertEquals("First song title should match", "Test Song 1", result.get(0).getTitle());
            assertEquals("Second song artist should match", "Test Artist 2", result.get(1).getArtist());
        }
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Test finding songs by user ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("title")).thenReturn("Test Song");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist");
            when(mockResultSet.getString("album")).thenReturn("Test Album");
            when(mockResultSet.getString("genre")).thenReturn("Test Genre");
            when(mockResultSet.getInt("year")).thenReturn(2023);
            when(mockResultSet.getInt("duration")).thenReturn(180);
            when(mockResultSet.getString("file_path")).thenReturn("/path/to/test.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            List<Song> result = songDAO.findByUserId(1);
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 song", 1, result.size());
            assertEquals("Song title should match", "Test Song", result.get(0).getTitle());
            assertEquals("User ID should match", 1, result.get(0).getUserId());
            
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }
    
    @Test
    public void testUpdate() throws SQLException {
        // Test updating a song
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Create test song
            Song song = createTestSong();
            song.setTitle("Updated Song");
            
            // Execute method under test
            boolean result = songDAO.update(song);
            
            // Verify
            assertTrue("Should return true for successful update", result);
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
    }
    
    @Test
    public void testUpdateFailed() throws SQLException {
        // Test failing to update a song
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);
            
            // Create test song
            Song song = createTestSong();
            
            // Execute method under test
            boolean result = songDAO.update(song);
            
            // Verify
            assertFalse("Should return false for failed update", result);
        }
    }
    
    @Test
    public void testUpdateThrowsException() throws SQLException {
        // Test exception during update
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test exception"));
            
            // Create test song
            Song song = createTestSong();
            
            // Execute method under test
            boolean result = songDAO.update(song);
            
            // Verify
            assertFalse("Should return false when SQLException occurs", result);
        }
    }
    
    @Test
    public void testDelete() throws SQLException {
        // Test deleting a song by ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = songDAO.delete(1);
            
            // Verify
            assertTrue("Should return true for successful deletion", result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testDeleteFailed() throws SQLException {
        // Test failing to delete a song
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);
            
            // Execute method under test
            boolean result = songDAO.delete(999);
            
            // Verify
            assertFalse("Should return false for failed deletion", result);
            verify(mockPreparedStatement).setInt(1, 999);
        }
    }
    
    @Test
    public void testSearch() throws SQLException {
        // Test searching for songs
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("title")).thenReturn("Test Song");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist");
            when(mockResultSet.getString("album")).thenReturn("Test Album");
            when(mockResultSet.getString("genre")).thenReturn("Test Genre");
            when(mockResultSet.getInt("year")).thenReturn(2023);
            when(mockResultSet.getInt("duration")).thenReturn(180);
            when(mockResultSet.getString("file_path")).thenReturn("/path/to/test.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            List<Song> result = songDAO.search("Test", "Artist", "Album", "Genre");
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 song", 1, result.size());
            assertEquals("Song title should match", "Test Song", result.get(0).getTitle());
            
            verify(mockPreparedStatement).setObject(1, "%Test%");
            verify(mockPreparedStatement).setObject(2, "%Artist%");
            verify(mockPreparedStatement).setObject(3, "%Album%");
            verify(mockPreparedStatement).setObject(4, "%Genre%");
        }
    }
    
    @Test
    public void testSearchWithNullParams() throws SQLException {
        // Test searching with null parameters
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("title")).thenReturn("Test Song");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist");
            when(mockResultSet.getString("album")).thenReturn("Test Album");
            when(mockResultSet.getString("genre")).thenReturn("Test Genre");
            when(mockResultSet.getInt("year")).thenReturn(2023);
            when(mockResultSet.getInt("duration")).thenReturn(180);
            when(mockResultSet.getString("file_path")).thenReturn("/path/to/test.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test with null parameters
            List<Song> result = songDAO.search(null, "Artist", null, "Genre");
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 1 song", 1, result.size());
            
            // Should only set parameters for non-null values
            verify(mockPreparedStatement).setObject(1, "%Artist%");
            verify(mockPreparedStatement).setObject(2, "%Genre%");
        }
    }
    
    @Test
    public void testFindByArtist() throws SQLException {
        // Test finding songs by artist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with song data
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("title")).thenReturn("Song 1", "Song 2");
            when(mockResultSet.getString("artist")).thenReturn("Test Artist", "Test Artist");
            when(mockResultSet.getString("album")).thenReturn("Album 1", "Album 2");
            when(mockResultSet.getString("genre")).thenReturn("Genre 1", "Genre 2");
            when(mockResultSet.getInt("year")).thenReturn(2023, 2022);
            when(mockResultSet.getInt("duration")).thenReturn(180, 240);
            when(mockResultSet.getString("file_path")).thenReturn("/path/1.mp3", "/path/2.mp3");
            when(mockResultSet.getInt("user_id")).thenReturn(1, 1);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(
                    Timestamp.valueOf(LocalDateTime.now()), 
                    Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            List<Song> result = songDAO.findByArtist("Test Artist");
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 songs", 2, result.size());
            assertEquals("All songs should have same artist", "Test Artist", result.get(0).getArtist());
            assertEquals("All songs should have same artist", "Test Artist", result.get(1).getArtist());
            
            verify(mockPreparedStatement).setString(1, "Test Artist");
        }
    }
    
    @Test
    public void testUpdateSong() throws SQLException {
        // Test updating a song information
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = songDAO.updateSong("Old Title", "Old Artist", "Old Album", 
                                               "New Title", "New Artist", "New Album", "New Genre");
            
            // Verify
            assertTrue("Should return true for successful update", result);
            verify(mockPreparedStatement).setString(1, "New Title");
            verify(mockPreparedStatement).setString(2, "New Artist");
            verify(mockPreparedStatement).setString(3, "New Album");
            verify(mockPreparedStatement).setString(4, "New Genre");
            verify(mockPreparedStatement).setString(5, "Old Title");
            verify(mockPreparedStatement).setString(6, "Old Artist");
            verify(mockPreparedStatement).setString(7, "Old Album");
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testMapResultSetToSong() throws SQLException {
        // Test mapping ResultSet to Song object
        // Setup mock ResultSet with song data
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Song");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("album")).thenReturn("Test Album");
        when(mockResultSet.getString("genre")).thenReturn("Test Genre");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getInt("duration")).thenReturn(180);
        when(mockResultSet.getString("file_path")).thenReturn("/path/to/test.mp3");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Execute method under test
        Song song = songDAO.mapResultSetToSong(mockResultSet);
        
        // Verify
        assertNotNull("Should return non-null song", song);
        assertEquals("ID should match", 1, song.getId());
        assertEquals("Title should match", "Test Song", song.getTitle());
        assertEquals("Artist should match", "Test Artist", song.getArtist());
        assertEquals("Album should match", "Test Album", song.getAlbum());
        assertEquals("Genre should match", "Test Genre", song.getGenre());
        assertEquals("Year should match", 2023, song.getYear());
        assertEquals("Duration should match", 180, song.getDuration());
        assertEquals("File path should match", "/path/to/test.mp3", song.getFilePath());
        assertEquals("User ID should match", 1, song.getUserId());
        assertEquals("Created at should match", timestamp, song.getCreatedAt());
    }
    
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Test exception during create
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test exception"));
            when(mockConn.getAutoCommit()).thenReturn(true);
            
            // Create test song
            Song song = createTestSong();
            
            // Execute method under test
            Song result = songDAO.create(song);
            
            // Verify
            assertNull("Should return null when SQLException occurs", result);
            verify(mockConn).rollback();
        }
    }
} 