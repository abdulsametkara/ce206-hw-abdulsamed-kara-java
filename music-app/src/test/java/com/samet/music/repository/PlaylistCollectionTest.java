package com.samet.music.repository;

import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaylistCollectionTest {

    private PlaylistCollection playlistCollection;

    @Mock
    private PlaylistDAO playlistDAO;
    
    @Mock
    private SongDAO songDAO;
    
    @Mock
    private DAOFactory daoFactory;

    private Playlist testPlaylist1;
    private Playlist testPlaylist2;
    private Song testSong1;
    private Song testSong2;

    @BeforeEach
    void setUp() throws Exception {
        // Mock nesnelerini başlat
        MockitoAnnotations.openMocks(this);
        
        // Test verilerini hazırla
        testPlaylist1 = new Playlist("Test Playlist 1");
        testPlaylist1.setId("1"); // Açıkça ID atama
        
        testPlaylist2 = new Playlist("Test Playlist 2");
        testPlaylist2.setId("2"); // Açıkça ID atama
        
        Artist testArtist = new Artist("Test Artist");
        testSong1 = new Song("Test Song 1", testArtist, 180);
        testSong1.setId("song1"); // Açıkça ID atama
        
        testSong2 = new Song("Test Song 2", testArtist, 240);
        testSong2.setId("song2"); // Açıkça ID atama
        
        // Mock DAO davranışlarını ayarla
        when(daoFactory.getPlaylistDAO()).thenReturn(playlistDAO);
        when(daoFactory.getSongDAO()).thenReturn(songDAO);
        
        // PlaylistCollection singleton örneğini sıfırla
        resetSingleton();
        
        // Test için PlaylistCollection örneğini oluştur
        setMockDaoFactory();
        playlistCollection = PlaylistCollection.getInstance();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Testler arasında singleton örneğini sıfırla
        resetSingleton();
    }
    
    /**
     * PlaylistCollection sınıfındaki static singleton örneğini sıfırlayan yardımcı metod
     */
    private void resetSingleton() throws Exception {
        Field instance = PlaylistCollection.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Mock DAOFactory'yi PlaylistCollection sınıfına enjekte eden yardımcı metod
     */
    private void setMockDaoFactory() throws Exception {
        Field factory = DAOFactory.class.getDeclaredField("instance");
        factory.setAccessible(true);
        factory.set(null, daoFactory);
    }

    @Test
    void getInstance_ShouldReturnSameInstance() {
        // Execute
        PlaylistCollection instance1 = PlaylistCollection.getInstance();
        PlaylistCollection instance2 = PlaylistCollection.getInstance();
        
        // Verify
        assertSame(instance1, instance2);
    }

    @Test
    void add_ShouldAddPlaylistToCollectionAndDatabase() {
        // Setup
        when(playlistDAO.insert(any(Playlist.class))).thenReturn(true);
        
        // Execute
        playlistCollection.add(testPlaylist1);
        
        // Verify
        verify(playlistDAO).insert(testPlaylist1);
        assertTrue(playlistCollection.contains(testPlaylist1.getId()));
    }
    
    @Test
    void add_ShouldNotAddNullPlaylist() {
        // Execute
        playlistCollection.add(null);
        
        // Verify
        verify(playlistDAO, never()).insert(any(Playlist.class));
    }

    @Test
    void getById_ShouldReturnPlaylistFromCache() {
        // Setup
        playlistCollection.add(testPlaylist1);
        when(playlistDAO.getById("1")).thenReturn(testPlaylist1);
        
        // Execute
        Playlist result = playlistCollection.getById("1");
        
        // Verify
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Playlist 1", result.getName());
        verify(playlistDAO, never()).getById("1"); // Çünkü önbellekten gelmeli
    }
    
    @Test
    void getById_ShouldFetchFromDatabaseWhenNotInCache() {
        // Setup
        when(playlistDAO.getById("1")).thenReturn(testPlaylist1);
        
        // Execute
        Playlist result = playlistCollection.getById("1");
        
        // Verify
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Playlist 1", result.getName());
        verify(playlistDAO).getById("1"); // Veritabanından alınmalı
    }
    
    @Test
    void getById_ShouldReturnNullForInvalidId() {
        // Execute
        Playlist result = playlistCollection.getById(null);
        
        // Verify
        assertNull(result);
        verify(playlistDAO, never()).getById(any());
    }
    
    @Test
    void getById_ShouldReturnNullWhenNotFound() {
        // Setup
        when(playlistDAO.getById("nonexistent")).thenReturn(null);
        
        // Execute
        Playlist result = playlistCollection.getById("nonexistent");
        
        // Verify
        assertNull(result);
        verify(playlistDAO).getById("nonexistent");
    }

    @Test
    void getAll_ShouldReturnAllPlaylistsFromDatabase() {
        // Setup
        List<Playlist> playlists = Arrays.asList(testPlaylist1, testPlaylist2);
        when(playlistDAO.getAll()).thenReturn(playlists);
        
        // Execute
        List<Playlist> result = playlistCollection.getAll();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testPlaylist1));
        assertTrue(result.contains(testPlaylist2));
        verify(playlistDAO).getAll();
    }

    @Test
    void remove_ShouldRemovePlaylistCompletely() {
        // Setup
        playlistCollection.add(testPlaylist1);
        when(playlistDAO.delete(anyString())).thenReturn(true);
        
        // Execute
        boolean result = playlistCollection.remove("1");
        
        // Verify
        assertTrue(result);
        assertFalse(playlistCollection.contains("1"));
        verify(playlistDAO).delete("1");
    }
    
    @Test
    void remove_ShouldReturnFalseForInvalidId() {
        // Execute
        boolean result = playlistCollection.remove(null);
        
        // Verify
        assertFalse(result);
        verify(playlistDAO, never()).delete(any());
    }

    @Test
    void loadFromDatabase_ShouldLoadAllPlaylists() throws Exception {
        // Setup
        List<Playlist> playlists = Arrays.asList(testPlaylist1, testPlaylist2);
        when(playlistDAO.getAll()).thenReturn(playlists);
        
        // loadFromDatabase metodunu direkt çağırmak için reflection kullanıyoruz
        java.lang.reflect.Method method = PlaylistCollection.class.getDeclaredMethod("loadFromDatabase");
        method.setAccessible(true);
        method.invoke(playlistCollection);
        
        // Verify - PlaylistCollection.contains() metodunu kullanıyoruz
        // BaseEntity, Playlist nesnesi oluşturulunca rastgele ID ürettiği için, 
        // test içinde açıkça ayarladığımız ID'leri kullanıyoruz
        assertTrue(playlistCollection.contains("1"));
        assertTrue(playlistCollection.contains("2"));
        verify(playlistDAO).getAll();
    }

    @Test
    void searchByName_ShouldReturnMatchingPlaylists() {
        // Setup
        List<Playlist> allPlaylists = Arrays.asList(
            testPlaylist1,
            testPlaylist2,
            new Playlist("Another Playlist"),
            new Playlist("Special List")
        );
        // "Test" kelimesini içeren çalma listeleri için sonuçları filtreleyelim
        List<Playlist> expectedResults = Arrays.asList(testPlaylist1, testPlaylist2);
        
        when(playlistDAO.getAll()).thenReturn(allPlaylists);
        
        // Execute
        List<Playlist> result = playlistCollection.searchByName("Test");
        
        // Verify
        assertEquals(2, result.size());
        assertEquals(expectedResults.get(0).getId(), result.get(0).getId());
        assertEquals(expectedResults.get(1).getId(), result.get(1).getId());
    }
    
    @Test
    void searchByName_ShouldReturnEmptyListForInvalidSearchTerm() {
        // Execute
        List<Playlist> result = playlistCollection.searchByName(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(playlistDAO, never()).getAll();
    }

    @Test
    void getPlaylistsContainingSong_ShouldReturnMatchingPlaylists() {
        // Setup
        List<Playlist> matchingPlaylists = Arrays.asList(testPlaylist1);
        when(playlistDAO.getPlaylistsContainingSong(String.valueOf(testSong1))).thenReturn(matchingPlaylists);
        
        // Execute
        List<Playlist> result = playlistCollection.getPlaylistsContainingSong(testSong1);
        
        // Verify
        assertEquals(1, result.size());
        assertEquals(testPlaylist1, result.get(0));
        verify(playlistDAO).getPlaylistsContainingSong(String.valueOf(testSong1));
    }
    
    @Test
    void getPlaylistsContainingSong_ShouldReturnEmptyListForNullSong() {
        // Execute
        List<Playlist> result = playlistCollection.getPlaylistsContainingSong(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(playlistDAO, never()).getPlaylistsContainingSong(any());
    }

    @Test
    void addSongToPlaylist_ShouldCallDAOMethod() {
        // Execute
        playlistCollection.addSongToPlaylist("1", "song1");
        
        // Verify
        verify(playlistDAO).addSongToPlaylist("1", "song1");
    }
    
    @Test
    void addSongToPlaylist_ShouldNotCallDAOForInvalidParameters() {
        // Execute - null playlistId
        playlistCollection.addSongToPlaylist(null, "song1");
        
        // Verify
        verify(playlistDAO, never()).addSongToPlaylist(any(), any());
        
        // Execute - null songId
        playlistCollection.addSongToPlaylist("1", null);
        
        // Verify
        verify(playlistDAO, never()).addSongToPlaylist(any(), any());
    }

    @Test
    void removeSongFromPlaylist_ShouldCallDAOMethod() {
        // Execute
        playlistCollection.removeSongFromPlaylist("1", "song1");
        
        // Verify
        verify(playlistDAO).removeSongFromPlaylist("1", "song1");
    }
    
    @Test
    void removeSongFromPlaylist_ShouldNotCallDAOForInvalidParameters() {
        // Execute - null playlistId
        playlistCollection.removeSongFromPlaylist(null, "song1");
        
        // Verify
        verify(playlistDAO, never()).removeSongFromPlaylist(any(), any());
        
        // Execute - null songId
        playlistCollection.removeSongFromPlaylist("1", null);
        
        // Verify
        verify(playlistDAO, never()).removeSongFromPlaylist(any(), any());
    }

    @Test
    void saveToFile_ShouldAlwaysReturnTrue() {
        // Execute
        boolean result = playlistCollection.saveToFile("dummy.file");
        
        // Verify
        assertTrue(result);
    }

    @Test
    void loadFromFile_ShouldLoadFromDatabaseAndReturnSuccess() {
        // Setup
        List<Playlist> playlists = Arrays.asList(testPlaylist1, testPlaylist2);
        when(playlistDAO.getAll()).thenReturn(playlists);
        
        // Execute
        boolean result = playlistCollection.loadFromFile("dummy.file");
        
        // Verify
        assertTrue(result);
        verify(playlistDAO).getAll();
    }
    
    @Test
    void loadFromFile_ShouldReturnFalseWhenNoPlaylistsFound() {
        // Setup
        when(playlistDAO.getAll()).thenReturn(new ArrayList<>());
        
        // Execute
        boolean result = playlistCollection.loadFromFile("dummy.file");
        
        // Verify
        assertFalse(result);
        verify(playlistDAO).getAll();
    }
    
    @Test
    void getItemId_ShouldReturnPlaylistId() throws Exception {
        // Execute - private metodu çağırmak için reflection kullanıyoruz
        java.lang.reflect.Method method = PlaylistCollection.class.getDeclaredMethod("getItemId", Playlist.class);
        method.setAccessible(true);
        String result = (String) method.invoke(playlistCollection, testPlaylist1);
        
        // Verify
        assertEquals("1", result);
    }
}