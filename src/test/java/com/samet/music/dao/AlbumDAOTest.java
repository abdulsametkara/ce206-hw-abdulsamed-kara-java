package com.samet.music.dao;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlbumDAOTest {

    private static AlbumDAO albumDAO;
    private static Connection connection;
    
    @Mock
    private SongDAO mockSongDAO;

    @BeforeClass
    public static void setUp() throws SQLException {
        connection = DatabaseUtil.getConnection();
        // Ensure tables exist
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS albums (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "artist TEXT NOT NULL," +
            "year INTEGER," +
            "genre TEXT," +
            "user_id INTEGER," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS album_songs (" +
            "album_id INTEGER," +
            "song_id INTEGER," +
            "PRIMARY KEY (album_id, song_id)," +
            "FOREIGN KEY (album_id) REFERENCES albums(id)," +
            "FOREIGN KEY (song_id) REFERENCES songs(id)" +
            ")"
        );
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        albumDAO = new AlbumDAO(connection, mockSongDAO);
    }

    @After
    public void cleanup() throws SQLException {
        // Clean up test data
        connection.createStatement().execute("DELETE FROM album_songs");
        connection.createStatement().execute("DELETE FROM albums");
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testCreateAlbum() {
        // Arrange
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2024);
        album.setGenre("Rock");
        album.setUserId(1);

        // Act
        boolean result = albumDAO.create(album);

        // Assert
        assertTrue(result);
        assertTrue(album.getId() > 0);
    }

    @Test
    public void testCreateAlbumWithSongs() {
        // Arrange
        Album album = new Album();
        album.setTitle("Test Album with Songs");
        album.setArtist("Test Artist");
        album.setYear(2024);
        album.setGenre("Rock");
        album.setUserId(1);

        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        song.setId(1);
        songs.add(song);
        album.setSongs(songs);

        // Act
        boolean result = albumDAO.create(album);

        // Assert
        assertTrue(result);
        assertTrue(album.getId() > 0);
    }

    @Test
    public void testFindById() {
        // Arrange
        Album album = createTestAlbum();
        albumDAO.create(album);

        // Act
        Album foundAlbum = albumDAO.findById(album.getId());

        // Assert
        assertNotNull(foundAlbum);
        assertEquals(album.getTitle(), foundAlbum.getTitle());
        assertEquals(album.getArtist(), foundAlbum.getArtist());
    }

    @Test
    public void testFindAll() {
        // Arrange
        createTestAlbum();
        createTestAlbum();

        // Act
        List<Album> albums = albumDAO.findAll();

        // Assert
        assertFalse(albums.isEmpty());
        assertEquals(2, albums.size());
    }

    @Test
    public void testFindByUserId() {
        // Arrange
        Album album = createTestAlbum();
        album.setUserId(999);
        albumDAO.create(album);

        // Act
        List<Album> albums = albumDAO.findByUserId(999);

        // Assert
        assertFalse(albums.isEmpty());
        assertEquals(1, albums.size());
        assertEquals(999, albums.get(0).getUserId());
    }

    @Test
    public void testFindByArtist() {
        // Arrange
        Album album = createTestAlbum();
        album.setArtist("UniqueArtistName");
        albumDAO.create(album);

        // Act
        List<Album> albums = albumDAO.findByArtist("UniqueArtistName");

        // Assert
        assertFalse(albums.isEmpty());
        assertEquals(1, albums.size());
        assertEquals("UniqueArtistName", albums.get(0).getArtist());
    }

    @Test
    public void testUpdate() {
        // Arrange
        Album album = createTestAlbum();
        albumDAO.create(album);

        // Act
        album.setTitle("Updated Title");
        boolean result = albumDAO.update(album);

        // Assert
        assertTrue(result);
        Album updated = albumDAO.findById(album.getId());
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    public void testDelete() {
        // Arrange
        Album album = createTestAlbum();
        albumDAO.create(album);

        // Act
        boolean result = albumDAO.delete(album.getId());

        // Assert
        assertTrue(result);
        assertNull(albumDAO.findById(album.getId()));
    }

    @Test
    public void testAddAlbumLegacy() {
        // Act
        boolean result = albumDAO.addAlbum("Test Album", "Test Artist", "2024", "Rock", 1);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testUpdateAlbumLegacy() {
        // Arrange
        albumDAO.addAlbum("Old Title", "Old Artist", "2020", "Rock", 1);

        // Act
        boolean result = albumDAO.updateAlbum(
            "Old Title", "Old Artist",
            "New Title", "New Artist", "2024", "Pop"
        );

        // Assert
        assertTrue(result);
    }

    @Test
    public void testDeleteAlbumLegacy() {
        // Arrange
        albumDAO.addAlbum("Test Title", "Test Artist", "2024", "Rock", 1);

        // Act
        boolean result = albumDAO.deleteAlbum("Test Title", "Test Artist");

        // Assert
        assertTrue(result);
    }

    @Test
    public void testGetAllAlbums() {
        // Arrange
        albumDAO.addAlbum("Album 1", "Artist 1", "2024", "Rock", 1);
        albumDAO.addAlbum("Album 2", "Artist 2", "2024", "Pop", 1);

        // Act
        List<String[]> albums = albumDAO.getAllAlbums();

        // Assert
        assertEquals(2, albums.size());
    }

    @Test
    public void testAddSongsToAlbum() {
        // Arrange
        Album album = createTestAlbum();
        albumDAO.create(album);

        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        song.setId(1);
        songs.add(song);

        // Act
        boolean result = albumDAO.addSongsToAlbum(album.getId(), songs);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testRemoveSongsFromAlbum() {
        // Arrange
        Album album = createTestAlbum();
        albumDAO.create(album);

        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        song.setId(1);
        songs.add(song);
        albumDAO.addSongsToAlbum(album.getId(), songs);

        // Act
        boolean result = albumDAO.removeSongsFromAlbum(album.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    public void testCreateAlbumTransactionRollback() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simulate failure to get generated keys

        AlbumDAO localAlbumDAO = new AlbumDAO(mockConnection, mockSongDAO);

        // Act
        boolean result = localAlbumDAO.create(createTestAlbum());

        // Assert
        assertFalse(result);
        verify(mockConnection).rollback();
    }

    @Test
    public void testGetSongsByAlbumIdWithSQLException() throws SQLException {
        // Setup mock objects
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        
        // Configure mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test exception"));
        
        // Create AlbumDAO with mock connection and songDAO
        albumDAO = new AlbumDAO(mockConnection, mockSongDAO);
        
        // Execute test
        List<Song> songs = albumDAO.getSongsByAlbumId(1);
        
        // Verify behavior
        verify(mockPreparedStatement).setInt(1, 1);
        assertNotNull("Songs list should not be null", songs);
        assertTrue("Songs list should be empty", songs.isEmpty());
    }

    @Test
    public void testAddSongsToAlbumWithConnectionFailure() {
        // Create AlbumDAO with null connection and songDAO
        albumDAO = new AlbumDAO(null, songDAO);
        
        // Create test data
        List<Song> songs = new ArrayList<>();
        songs.add(new Song(1, "Test Song", "Test Artist", "Test Album", 180));
        
        // Execute test
        boolean result = albumDAO.addSongsToAlbum(1, songs);
        
        // Verify
        assertFalse("Should return false when connection fails", result);
    }

    @Test
    public void testRemoveSongsFromAlbumWithConnectionFailure() {
        // Create AlbumDAO with null connection and songDAO
        albumDAO = new AlbumDAO(null, songDAO);
        
        // Execute test
        boolean result = albumDAO.removeSongsFromAlbum(1);
        
        // Verify
        assertFalse("Should return false when connection fails", result);
    }

    @Test
    public void testAlbumSongsRetrievalWithSQLException() throws SQLException {
        // Setup mock objects
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        
        // Configure mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Album");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getInt("year")).thenReturn(2024);
        when(mockResultSet.getString("genre")).thenReturn("Test Genre");
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        
        // Create AlbumDAO with mock connection and songDAO
        albumDAO = new AlbumDAO(mockConnection, songDAO);
        
        // Execute test
        Album retrievedAlbum = albumDAO.findById(1);
        
        // Verify behavior
        assertNotNull("Album should not be null", retrievedAlbum);
        assertEquals("Album ID should match", 1, retrievedAlbum.getId());
        assertEquals("Album title should match", "Test Album", retrievedAlbum.getTitle());
        assertEquals("Album artist should match", "Test Artist", retrievedAlbum.getArtist());
        assertEquals("Album year should match", 2024, retrievedAlbum.getYear());
        assertEquals("Album genre should match", "Test Genre", retrievedAlbum.getGenre());
        assertNotNull("Songs list should not be null", retrievedAlbum.getSongs());
        assertTrue("Songs list should be empty", retrievedAlbum.getSongs().isEmpty());
    }

    @Test
    public void testRemoveSongsFromAlbumWithSQLException() throws SQLException {
        // Setup mock objects
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        
        // Configure mock behavior
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doThrow(new SQLException("Test exception")).when(mockPreparedStatement).executeUpdate();
        
        // Create AlbumDAO with mock connection and songDAO
        albumDAO = new AlbumDAO(mockConnection, mockSongDAO);
        
        // Execute test
        boolean result = albumDAO.removeSongsFromAlbum(1);
        
        // Verify behavior
        verify(mockPreparedStatement).setInt(1, 1);
        assertFalse("Should return false when SQL exception occurs", result);
    }

    @Test
    public void testAddSongsToAlbumWithEmptyList() {
        // Setup
        Connection mockConnection = mock(Connection.class);
        albumDAO = new AlbumDAO(mockConnection, mockSongDAO);
        List<Song> emptySongs = new ArrayList<>();
        
        // Test execution
        boolean result = albumDAO.addSongsToAlbum(1, emptySongs);
        
        // Verify
        assertTrue("Should return true for empty song list", result);
    }

    private Album createTestAlbum() {
        Album album = new Album();
        album.setTitle("Test Album");
        album.setArtist("Test Artist");
        album.setYear(2024);
        album.setGenre("Rock");
        album.setUserId(1);
        album.setCreatedAt(LocalDateTime.now());
        return album;
    }
} 