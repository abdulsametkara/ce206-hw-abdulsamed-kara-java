package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * AlbumDAO için Mockito kullanarak doğrudan implementasyonu test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class AlbumDAOMockitoTest {
    
    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private Statement mockStatement;
    @Mock private ResultSet mockResultSet;
    @Mock private SongDAO mockSongDAO;
    
    private AlbumDAO albumDAO;
    
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        
        // Setup for direct testing of AlbumDAO
        albumDAO = new AlbumDAO(mockConnection, mockSongDAO);
    }
    
    /**
     * Directly tests the create method of AlbumDAO using mocks
     */
    @Test
    public void testCreateDirectImplementation() throws SQLException {
        // Setup
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Mock behavior for prepared statement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // One row affected
        
        // Mock behavior for getting generated ID
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("last_insert_rowid()"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        
        // Execute
        boolean result = albumDAO.create(album);
        
        // Verify
        assertTrue("Album should be created successfully", result);
        assertEquals("Album ID should be set", 1, album.getId());
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, album.getTitle());
        verify(mockPreparedStatement).setString(2, album.getArtist());
        verify(mockPreparedStatement).setInt(3, album.getYear());
        verify(mockPreparedStatement).setString(4, album.getGenre());
        verify(mockPreparedStatement).setInt(5, album.getUserId());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests the create method with songs included
     */
    @Test
    public void testCreateWithSongs() throws SQLException {
        // Setup
        Album album = new Album();
        album.setTitle("Test Album With Songs");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Add songs to the album
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Test Artist", "Test Album", "Rock", 2023, 180, "/path/to/file1", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Test Artist", "Test Album", "Rock", 2023, 200, "/path/to/file2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        album.setSongs(songs);
        
        // Mock behavior for prepared statement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // One row affected
        
        // Mock behavior for getting generated ID
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("last_insert_rowid()"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        
        // Mock behavior for adding songs to album
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[] {1, 1});
        
        // Execute
        boolean result = albumDAO.create(album);
        
        // Verify
        assertTrue("Album with songs should be created successfully", result);
        assertEquals("Album ID should be set", 1, album.getId());
        
        // Verify interactions for adding songs
        verify(mockConnection, atLeastOnce()).setAutoCommit(false);
        verify(mockConnection, atLeastOnce()).commit();
        verify(mockConnection, atLeastOnce()).setAutoCommit(true);
    }
    
    /**
     * Tests the findById method directly
     */
    @Test
    public void testFindByIdDirectImplementation() throws SQLException {
        // Setup
        int albumId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock behavior for the album query
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Album exists, then no more rows
        
        // Mock result set data for the album
        when(mockResultSet.getInt("id")).thenReturn(albumId);
        when(mockResultSet.getString("title")).thenReturn("Test Album");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Mock songs query
        PreparedStatement songStatement = mock(PreparedStatement.class);
        ResultSet songResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT * FROM songs"))).thenReturn(songStatement);
        when(songStatement.executeQuery()).thenReturn(songResultSet);
        when(songResultSet.next()).thenReturn(false); // No songs
        
        // Execute
        Album album = albumDAO.findById(albumId);
        
        // Verify
        assertNotNull("Album should not be null", album);
        assertEquals("Album ID should match", albumId, album.getId());
        assertEquals("Album title should match", "Test Album", album.getTitle());
        assertEquals("Album artist should match", "Test Artist", album.getArtist());
        
        // Verify interactions
        verify(mockPreparedStatement).setInt(1, albumId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Tests the findAll method directly
     */
    @Test
    public void testFindAllDirectImplementation() throws SQLException {
        // Setup
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock behavior
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        
        // Mock album data - return true twice for two albums, then false
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2); // IDs for the two albums
        when(mockResultSet.getString("title")).thenReturn("Album 1", "Album 2");
        when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
        when(mockResultSet.getInt("year")).thenReturn(2021, 2022);
        when(mockResultSet.getString("genre")).thenReturn("Rock", "Pop");
        when(mockResultSet.getInt("user_id")).thenReturn(1, 2);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Execute
        List<Album> albums = albumDAO.findAll();
        
        // Verify
        assertNotNull("Albums list should not be null", albums);
        assertEquals("Should return 2 albums", 2, albums.size());
        assertEquals("First album title should match", "Album 1", albums.get(0).getTitle());
        assertEquals("Second album title should match", "Album 2", albums.get(1).getTitle());
        
        // Verify interactions
        verify(mockStatement).executeQuery(contains("SELECT * FROM albums"));
    }
    
    /**
     * Tests the findByUserId method directly
     */
    @Test
    public void testFindByUserIdDirectImplementation() throws SQLException {
        // Setup
        int userId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Mock result set data for two albums
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("User Album 1", "User Album 2");
        when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
        when(mockResultSet.getInt("year")).thenReturn(2021, 2022);
        when(mockResultSet.getString("genre")).thenReturn("Rock", "Pop");
        when(mockResultSet.getInt("user_id")).thenReturn(userId, userId);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Execute
        List<Album> albums = albumDAO.findByUserId(userId);
        
        // Verify
        assertNotNull("Albums list should not be null", albums);
        assertEquals("Should return 2 albums for user", 2, albums.size());
        assertEquals("First album title should match", "User Album 1", albums.get(0).getTitle());
        
        // Verify interactions
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Tests the findByArtist method directly
     */
    @Test
    public void testFindByArtistDirectImplementation() throws SQLException {
        // Setup
        String artistName = "Test Artist";
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Mock result set data for albums by artist
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Artist Album");
        when(mockResultSet.getString("artist")).thenReturn(artistName);
        when(mockResultSet.getInt("year")).thenReturn(2023);
        when(mockResultSet.getString("genre")).thenReturn("Rock");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Execute
        List<Album> albums = albumDAO.findByArtist(artistName);
        
        // Verify
        assertNotNull("Albums list should not be null", albums);
        assertEquals("Should return 1 album for artist", 1, albums.size());
        assertEquals("Album artist should match", artistName, albums.get(0).getArtist());
        
        // Verify interactions
        verify(mockPreparedStatement).setString(1, "%" + artistName + "%");
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Tests the update method directly
     */
    @Test
    public void testUpdateDirectImplementation() throws SQLException {
        // Setup
        Album album = new Album();
        album.setId(1);
        album.setTitle("Updated Album");
        album.setArtist("Updated Artist");
        album.setYear(2023);
        album.setGenre("Jazz");
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // One row affected
        
        // Execute
        boolean result = albumDAO.update(album);
        
        // Verify
        assertTrue("Album should be updated successfully", result);
        
        // Verify interactions
        verify(mockPreparedStatement).setString(1, album.getTitle());
        verify(mockPreparedStatement).setString(2, album.getArtist());
        verify(mockPreparedStatement).setInt(3, album.getYear());
        verify(mockPreparedStatement).setString(4, album.getGenre());
        verify(mockPreparedStatement).setInt(5, album.getId());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Tests the delete method directly
     */
    @Test
    public void testDeleteDirectImplementation() throws SQLException {
        // Setup
        int albumId = 1;
        
        // Mock behavior for removeSongsFromAlbum part
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(2, 1); // First call removes 2 songs, second call deletes 1 album
        
        // Execute
        boolean result = albumDAO.delete(albumId);
        
        // Verify
        assertTrue("Album should be deleted successfully", result);
        
        // Verify interactions for removing songs
        verify(mockPreparedStatement, times(2)).setInt(1, albumId);
        verify(mockPreparedStatement, times(2)).executeUpdate();
    }
    
    /**
     * Tests the addSongsToAlbum method directly
     */
    @Test
    public void testAddSongsToAlbumDirectImplementation() throws SQLException {
        // Setup
        int albumId = 1;
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Artist", "Album", "Rock", 2023, 180, "/path", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Artist", "Album", "Pop", 2023, 200, "/path2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[] {1, 1}); // Both updates successful
        
        // Execute
        boolean result = albumDAO.addSongsToAlbum(albumId, songs);
        
        // Verify
        assertTrue("Songs should be added to album successfully", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement, times(2)).setInt(1, albumId);
        verify(mockPreparedStatement).setInt(2, song1.getId());
        verify(mockPreparedStatement).setInt(2, song2.getId());
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement).executeBatch();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests the removeSongsFromAlbum method directly
     */
    @Test
    public void testRemoveSongsFromAlbumDirectImplementation() throws SQLException {
        // Setup
        int albumId = 1;
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(3); // 3 songs removed
        
        // Execute
        boolean result = albumDAO.removeSongsFromAlbum(albumId);
        
        // Verify
        assertTrue("Songs should be removed from album successfully", result);
        
        // Verify interactions
        verify(mockPreparedStatement).setInt(1, albumId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Tests exception handling in create method
     */
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Setup
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database connection failed"));
        
        // Execute
        boolean result = albumDAO.create(album);
        
        // Verify
        assertFalse("Creation should fail when SQLException occurs", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests exception handling in commit phase
     */
    @Test
    public void testCreateRollbackException() throws SQLException {
        // Setup
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Mock behavior to throw SQLException during prepared statement
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Make rollback also throw an exception to test that catch block
        doThrow(new SQLException("Rollback failed")).when(mockConnection).rollback();
        
        // Execute
        boolean result = albumDAO.create(album);
        
        // Verify
        assertFalse("Creation should fail when SQLException occurs", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests exception in setAutoCommit
     */
    @Test
    public void testCreateSetAutoCommitException() throws SQLException {
        // Setup
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2023);
        album.setGenre("Rock");
        album.setUserId(1);
        
        // Make prepareStatement throw exception to enter catch block
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Setup mockConnection to first work normally, then throw exception in finally block
        // The first call happens in the catch block after rollback, and the second in finally
        doThrow(new SQLException("Failed to reset auto-commit")).when(mockConnection).setAutoCommit(true);
        
        // Execute
        boolean result = albumDAO.create(album);
        
        // Verify
        assertFalse("Creation should fail when SQLException occurs", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests exception handling in findById method
     */
    @Test
    public void testFindByIdWithSQLException() throws SQLException {
        // Setup
        int albumId = 1;
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Execute
        Album result = albumDAO.findById(albumId);
        
        // Verify
        assertNull("Should return null when SQLException occurs", result);
    }
    
    /**
     * Tests exception handling in delete method
     */
    @Test
    public void testDeleteWithSQLException() throws SQLException {
        // Setup
        int albumId = 1;
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Execute
        boolean result = albumDAO.delete(albumId);
        
        // Verify
        assertFalse("Should return false when SQLException occurs", result);
    }
    
    /**
     * Tests exception handling in update method
     */
    @Test
    public void testUpdateWithSQLException() throws SQLException {
        // Setup
        Album album = new Album();
        album.setId(1);
        album.setTitle("Updated Album");
        album.setArtist("Updated Artist");
        album.setYear(2023);
        album.setGenre("Jazz");
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Execute
        boolean result = albumDAO.update(album);
        
        // Verify
        assertFalse("Should return false when SQLException occurs", result);
    }
    
    /**
     * Tests exception handling in addSongsToAlbum method
     */
    @Test
    public void testAddSongsToAlbumWithSQLException() throws SQLException {
        // Setup
        int albumId = 1;
        List<Song> songs = new ArrayList<>();
        Song song = new Song("Song 1", "Artist", "Album", "Rock", 2023, 180, "/path", 1);
        song.setId(1);
        songs.add(song);
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Execute
        boolean result = albumDAO.addSongsToAlbum(albumId, songs);
        
        // Verify
        assertFalse("Should return false when SQLException occurs", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests execution batch failure in addSongsToAlbum method
     */
    @Test
    public void testAddSongsToAlbumWithBatchFailure() throws SQLException {
        // Setup
        int albumId = 1;
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("Song 1", "Artist", "Album", "Rock", 2023, 180, "/path", 1);
        song1.setId(1);
        Song song2 = new Song("Song 2", "Artist", "Album", "Pop", 2023, 200, "/path2", 1);
        song2.setId(2);
        songs.add(song1);
        songs.add(song2);
        
        // Mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[] {1, -1}); // One success, one failure
        
        // Execute
        boolean result = albumDAO.addSongsToAlbum(albumId, songs);
        
        // Verify
        assertFalse("Should return false when batch update fails", result);
        
        // Verify interactions
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Tests exception handling in removeSongsFromAlbum method
     */
    @Test
    public void testRemoveSongsFromAlbumWithSQLException() throws SQLException {
        // Setup
        int albumId = 1;
        
        // Mock behavior to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Execute
        boolean result = albumDAO.removeSongsFromAlbum(albumId);
        
        // Verify
        assertFalse("Should return false when SQLException occurs", result);
    }
} 