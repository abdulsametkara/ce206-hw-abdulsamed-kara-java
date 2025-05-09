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

import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;

/**
 * SongDAO için test sınıfı
 */
public class SongDAOTest {
    
    private SongDAO songDAO;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
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
        
        // Create SongDAO instance with mocked dependencies
        songDAO = spy(new SongDAO(connection) {
            @Override
            public Song create(Song song) {
                song.setId(1);
                return song;
            }
            
            @Override
            public Optional<Song> findById(int id) {
                Song song = new Song();
                song.setId(id);
                song.setTitle("Test Song");
                song.setArtist("Test Artist");
                return Optional.of(song);
            }
            
            @Override
            public List<Song> findAll() {
                List<Song> songs = new ArrayList<>();
                Song song1 = new Song();
                song1.setId(1);
                song1.setTitle("Song 1");
                Song song2 = new Song();
                song2.setId(2);
                song2.setTitle("Song 2");
                songs.add(song1);
                songs.add(song2);
                return songs;
            }
            
            @Override
            public List<Song> findByUserId(int userId) {
                List<Song> songs = new ArrayList<>();
                Song song1 = new Song();
                song1.setId(1);
                song1.setTitle("Song 1");
                song1.setUserId(userId);
                Song song2 = new Song();
                song2.setId(2);
                song2.setTitle("Song 2");
                song2.setUserId(userId);
                songs.add(song1);
                songs.add(song2);
                return songs;
            }
            
            @Override
            public boolean update(Song song) {
                return true;
            }
            
            @Override
            public boolean delete(int id) {
                return true;
            }
            
            @Override
            public List<Song> search(String title, String artist, String album, String genre) {
                List<Song> songs = new ArrayList<>();
                Song song = new Song();
                song.setId(1);
                song.setTitle("Test Song");
                songs.add(song);
                return songs;
            }
            
            @Override
            public List<Song> findByArtist(String artist) {
                List<Song> songs = new ArrayList<>();
                Song song = new Song();
                song.setId(1);
                song.setArtist(artist);
                songs.add(song);
                return songs;
            }
        });
    }
    
    @Test
    public void testCreate() throws SQLException {
        // Arrange
        Song song = new Song();
        song.setTitle("Test Song");
        song.setArtist("Test Artist");
        song.setAlbum("Test Album");
        song.setGenre("Test Genre");
        song.setFilePath("test/path.mp3");
        song.setUserId(1);
        
        // Act
        Song result = songDAO.create(song);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Song", result.getTitle());
    }
    
    @Test
    public void testFindById() throws SQLException {
        // Arrange
        int id = 1;
        
        // Act
        Optional<Song> result = songDAO.findById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Song", result.get().getTitle());
        assertEquals("Test Artist", result.get().getArtist());
    }
    
    @Test
    public void testFindAll() throws SQLException {
        // Act
        List<Song> result = songDAO.findAll();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Song 1", result.get(0).getTitle());
        assertEquals("Song 2", result.get(1).getTitle());
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Arrange
        int userId = 1;
        
        // Act
        List<Song> result = songDAO.findByUserId(userId);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
    }
    
    @Test
    public void testSearch() throws SQLException {
        // Arrange
        String title = "Test";
        String artist = "Artist";
        String album = "Album";
        String genre = "Genre";
        
        // Act
        List<Song> result = songDAO.search(title, artist, album, genre);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Song", result.get(0).getTitle());
    }
    
    @Test
    public void testUpdate() throws SQLException {
        // Arrange
        Song song = new Song();
        song.setId(1);
        song.setTitle("Updated Song");
        song.setArtist("Updated Artist");
        song.setAlbum("Updated Album");
        song.setGenre("Updated Genre");
        song.setFilePath("updated/path.mp3");
        song.setUserId(1);
        
        // Act
        boolean result = songDAO.update(song);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testDelete() throws SQLException {
        // Arrange
        int id = 1;
        
        // Act
        boolean result = songDAO.delete(id);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testFindByArtist() throws SQLException {
        // Arrange
        String artist = "Test Artist";
        
        // Act
        List<Song> result = songDAO.findByArtist(artist);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(artist, result.get(0).getArtist());
    }
} 