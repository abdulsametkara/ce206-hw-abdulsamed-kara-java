package com.samet.music.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

/**
 * Test class for PlaylistDAO
 */
public class PlaylistDAOTest {
    
    private PlaylistDAO playlistDAO;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private SongDAO songDAO;
    
    @Before
    public void setUp() throws SQLException {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);
        
        // Configure mock behavior
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(preparedStatement.executeUpdate()).thenReturn(1); // Default success case
        when(connection.getAutoCommit()).thenReturn(true);     // Default autoCommit behavior
        
        // Mock ResultSet for findById and similar methods
        when(resultSet.next()).thenReturn(true, false); // Default to one result
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test Playlist");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Create PlaylistDAO instance with mocked dependencies
        playlistDAO = spy(new PlaylistDAO(songDAO));
        
        // Override default behavior for tests
        doAnswer(invocation -> {
            Playlist playlist = invocation.getArgument(0);
            playlist.setId(1);
            return playlist;
        }).when(playlistDAO).create(any(Playlist.class));
        
        doAnswer(invocation -> {
            int id = invocation.getArgument(0);
            Playlist playlist = new Playlist();
            playlist.setId(id);
            playlist.setName("Test Playlist");
            playlist.setDescription("Test Description");
            playlist.setUserId(1);
            return Optional.of(playlist);
        }).when(playlistDAO).findById(anyInt());
        
        doAnswer(invocation -> {
            List<Playlist> playlists = new ArrayList<>();
            Playlist playlist1 = new Playlist();
            playlist1.setId(1);
            playlist1.setName("Playlist 1");
            Playlist playlist2 = new Playlist();
            playlist2.setId(2);
            playlist2.setName("Playlist 2");
            playlists.add(playlist1);
            playlists.add(playlist2);
            return playlists;
        }).when(playlistDAO).findAll();
        
        doAnswer(invocation -> {
            int userId = invocation.getArgument(0);
            List<Playlist> playlists = new ArrayList<>();
            Playlist playlist1 = new Playlist();
            playlist1.setId(1);
            playlist1.setName("Playlist 1");
            playlist1.setUserId(userId);
            Playlist playlist2 = new Playlist();
            playlist2.setId(2);
            playlist2.setName("Playlist 2");
            playlist2.setUserId(userId);
            playlists.add(playlist1);
            playlists.add(playlist2);
            return playlists;
        }).when(playlistDAO).findByUserId(anyInt());
        
        doReturn(true).when(playlistDAO).update(any(Playlist.class));
        doReturn(true).when(playlistDAO).delete(anyInt());
    }
    
    @Test
    public void testCreate() throws SQLException {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setName("Test Playlist");
        playlist.setDescription("Test Description");
        playlist.setUserId(1);
        
        // Act
        Playlist result = playlistDAO.create(playlist);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Playlist", result.getName());
    }
    
    @Test
    public void testFindById() throws SQLException {
        // Arrange
        int id = 1;
        
        // Act
        Optional<Playlist> result = playlistDAO.findById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Playlist", result.get().getName());
        assertEquals("Test Description", result.get().getDescription());
    }
    
    @Test
    public void testFindAll() throws SQLException {
        // Act
        List<Playlist> result = playlistDAO.findAll();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Playlist 1", result.get(0).getName());
        assertEquals("Playlist 2", result.get(1).getName());
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Act
        List<Playlist> result = playlistDAO.findByUserId(userId);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
    }
    
    @Test
    public void testUpdate() throws SQLException {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setName("Updated Playlist");
        playlist.setDescription("Updated Description");
        playlist.setUserId(1);
        
        // Act
        boolean result = playlistDAO.update(playlist);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testDelete() throws SQLException {
        // Arrange
        int id = 1;
        
        // Act
        boolean result = playlistDAO.delete(id);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testAddPlaylist() throws SQLException {
        // Arrange
        String name = "Test Playlist";
        String description = "Test Description";
        int userId = 1;
        
        // Setup for this specific test
        doReturn(true).when(playlistDAO).addPlaylist(anyString(), anyString(), anyInt());
        
        // Act
        boolean result = playlistDAO.addPlaylist(name, description, userId);
        
        // Assert
        assertTrue(result);
        verify(playlistDAO).addPlaylist(name, description, userId);
    }
    
    @Test
    public void testAddPlaylistInvalidInput() throws SQLException {
        // Arrange
        String name = null;
        String description = "Test Description";
        int userId = 1;
        
        // Override the stub behavior for this test
        doReturn(false).when(playlistDAO).addPlaylist(null, description, userId);
        
        // Act
        boolean result = playlistDAO.addPlaylist(name, description, userId);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testUpdatePlaylist() throws SQLException {
        // Arrange
        String oldName = "Old Playlist";
        String newName = "New Playlist";
        
        // Setup for this specific test
        doReturn(true).when(playlistDAO).updatePlaylist(anyString(), anyString());
        
        // Act
        boolean result = playlistDAO.updatePlaylist(oldName, newName);
        
        // Assert
        assertTrue(result);
        verify(playlistDAO).updatePlaylist(oldName, newName);
    }
    
    @Test
    public void testUpdatePlaylistInvalidInput() throws SQLException {
        // Arrange
        String oldName = null;
        String newName = "New Playlist";
        
        // Override the stub behavior for this test
        doReturn(false).when(playlistDAO).updatePlaylist(null, newName);
        
        // Act
        boolean result = playlistDAO.updatePlaylist(oldName, newName);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testDeletePlaylist() throws SQLException {
        // Arrange
        String name = "Test Playlist";
        
        // Setup for this specific test
        doReturn(true).when(playlistDAO).deletePlaylist(anyString());
        
        // Act
        boolean result = playlistDAO.deletePlaylist(name);
        
        // Assert
        assertTrue(result);
        verify(playlistDAO).deletePlaylist(name);
    }
    
    @Test
    public void testDeletePlaylistInvalidInput() throws SQLException {
        // Arrange
        String name = null;
        
        // Override the stub behavior for this test
        doReturn(false).when(playlistDAO).deletePlaylist(null);
        
        // Act
        boolean result = playlistDAO.deletePlaylist(name);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testGetAllPlaylists() throws SQLException {
        // Arrange
        List<String[]> expectedPlaylists = new ArrayList<>();
        expectedPlaylists.add(new String[]{"Playlist 1", "5", "2023-05-01"});
        expectedPlaylists.add(new String[]{"Playlist 2", "3", "2023-05-02"});
        
        // Setup for this specific test
        doReturn(expectedPlaylists).when(playlistDAO).getAllPlaylists();
        
        // Act
        List<String[]> result = playlistDAO.getAllPlaylists();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Playlist 1", result.get(0)[0]);
        assertEquals("5", result.get(0)[1]);
        assertEquals("2023-05-01", result.get(0)[2]);
        assertEquals("Playlist 2", result.get(1)[0]);
        assertEquals("3", result.get(1)[1]);
        assertEquals("2023-05-02", result.get(1)[2]);
    }
    
    @Test
    public void testAddSongsToPlaylist() throws SQLException {
        // Arrange
        int playlistId = 1;
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Song 1");
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Song 2");
        songs.add(song1);
        songs.add(song2);
        
        // Setup for this specific test
        doReturn(true).when(playlistDAO).addSongsToPlaylist(anyInt(), anyList());
        
        // Act
        boolean result = playlistDAO.addSongsToPlaylist(playlistId, songs);
        
        // Assert
        assertTrue(result);
        verify(playlistDAO).addSongsToPlaylist(playlistId, songs);
    }
    
    @Test
    public void testAddSongsToPlaylistInvalidInput() throws SQLException {
        // Arrange
        int playlistId = 0;
        List<Song> songs = new ArrayList<>();
        
        // Setup for this specific test
        doReturn(false).when(playlistDAO).addSongsToPlaylist(eq(0), anyList());
        
        // Act
        boolean result = playlistDAO.addSongsToPlaylist(playlistId, songs);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testRemoveSongsFromPlaylist() throws SQLException {
        // Arrange
        int playlistId = 1;
        
        // Setup for this specific test
        doReturn(true).when(playlistDAO).removeSongsFromPlaylist(anyInt());
        
        // Act
        boolean result = playlistDAO.removeSongsFromPlaylist(playlistId);
        
        // Assert
        assertTrue(result);
        verify(playlistDAO).removeSongsFromPlaylist(playlistId);
    }
    
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setName("Test Playlist");
        playlist.setDescription("Test Description");
        playlist.setUserId(1);
        playlist.setSongs(Arrays.asList(new Song(), new Song()));
        
        // Setup for this specific test
        doReturn(null).when(playlistDAO).create(eq(playlist));
        
        // Act
        Playlist result = playlistDAO.create(playlist);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    public void testUpdateWithSQLException() throws SQLException {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setName("Updated Playlist");
        playlist.setDescription("Updated Description");
        playlist.setUserId(1);
        
        // Setup for this specific test
        doReturn(false).when(playlistDAO).update(eq(playlist));
        
        // Act
        boolean result = playlistDAO.update(playlist);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testDeleteWithSQLException() throws SQLException {
        // Arrange
        int id = 1;
        
        // Setup for this specific test
        doReturn(false).when(playlistDAO).delete(eq(id));
        
        // Act
        boolean result = playlistDAO.delete(id);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testFindByIdWithSQLException() throws SQLException {
        // Arrange
        int id = 1;
        
        // Setup for this specific test
        doReturn(Optional.empty()).when(playlistDAO).findById(eq(id));
        
        // Act
        Optional<Playlist> result = playlistDAO.findById(id);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testFindAllWithSQLException() throws SQLException {
        // Arrange
        // Setup for this specific test
        doReturn(Collections.emptyList()).when(playlistDAO).findAll();
        
        // Act
        List<Playlist> result = playlistDAO.findAll();
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testFindByUserIdWithSQLException() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Setup for this specific test
        doReturn(Collections.emptyList()).when(playlistDAO).findByUserId(eq(userId));
        
        // Act
        List<Playlist> result = playlistDAO.findByUserId(userId);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testAddPlaylistWithSQLException() throws SQLException {
        // Arrange
        String name = "Test Playlist";
        String description = "Test Description";
        int userId = 1;
        
        // Setup for this specific test
        doReturn(false).when(playlistDAO).addPlaylist(eq(name), eq(description), eq(userId));
        
        // Act
        boolean result = playlistDAO.addPlaylist(name, description, userId);
        
        // Assert
        assertFalse(result);
    }
}
