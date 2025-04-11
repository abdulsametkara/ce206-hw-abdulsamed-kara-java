package com.samet.music.repository;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SongCollectionTest {

    private SongCollection songCollection;

    @Mock
    private SongDAO songDAO;
    
    @Mock
    private ArtistDAO artistDAO;
    
    @Mock
    private AlbumDAO albumDAO;
    
    @Mock
    private DAOFactory daoFactory;

    private Song testSong1;
    private Song testSong2;
    private Artist testArtist;
    private Album testAlbum;

    @BeforeEach
    void setUp() throws Exception {
        // Mock nesnelerini başlat
        MockitoAnnotations.openMocks(this);
        
        // Test verilerini hazırla
        testArtist = new Artist("Test Artist");
        testArtist.setId("artist1");
        
        testAlbum = new Album("Test Album", testArtist, 2023);
        testAlbum.setId("album1");
        
        testSong1 = new Song("Test Song 1", testArtist, 180);
        testSong1.setId("song1");
        testSong1.setAlbum(testAlbum);
        testSong1.setGenre("Rock");
        
        testSong2 = new Song("Test Song 2", testArtist, 240);
        testSong2.setId("song2");
        testSong2.setAlbum(testAlbum);
        testSong2.setGenre("Pop");
        
        // Mock DAO davranışlarını ayarla
        when(daoFactory.getSongDAO()).thenReturn(songDAO);
        when(daoFactory.getArtistDAO()).thenReturn(artistDAO);
        when(daoFactory.getAlbumDAO()).thenReturn(albumDAO);
        
        // SongCollection singleton örneğini sıfırla
        resetSingleton();
        
        // Test için SongCollection örneğini oluştur
        setMockDaoFactory();
        songCollection = SongCollection.getInstance();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Testler arasında singleton örneğini sıfırla
        resetSingleton();
    }
    
    /**
     * SongCollection sınıfındaki static singleton örneğini sıfırlayan yardımcı metod
     */
    private void resetSingleton() throws Exception {
        Field instance = SongCollection.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Mock DAOFactory'yi SongCollection sınıfına enjekte eden yardımcı metod
     */
    private void setMockDaoFactory() throws Exception {
        Field factory = DAOFactory.class.getDeclaredField("instance");
        factory.setAccessible(true);
        factory.set(null, daoFactory);
    }

    @Test
    void getInstance_ShouldReturnSameInstance() {
        // Execute
        SongCollection instance1 = SongCollection.getInstance();
        SongCollection instance2 = SongCollection.getInstance();
        
        // Verify
        assertSame(instance1, instance2);
    }

    @Test
    void add_ShouldAddSongToCollectionAndDatabase() {
        // Setup
        when(songDAO.insert(any(Song.class))).thenReturn(true);
        
        // Execute
        songCollection.add(testSong1);
        
        // Verify
        verify(songDAO).insert(testSong1);
        assertTrue(songCollection.contains(testSong1.getId()));
    }
    
    @Test
    void add_ShouldNotAddNullSong() {
        // Execute
        songCollection.add(null);
        
        // Verify
        verify(songDAO, never()).insert(any(Song.class));
    }
    
    @Test
    void add_ShouldRemoveFromMemoryIfDatabaseInsertFails() {
        // Setup
        when(songDAO.insert(any(Song.class))).thenReturn(false);
        
        // Execute
        songCollection.add(testSong1);
        
        // Verify
        verify(songDAO).insert(testSong1);
        assertFalse(songCollection.contains(testSong1.getId()));
    }

    @Test
    void getById_ShouldReturnSongFromCache() throws Exception {
        // Setup
        // Önce songCollection nesnesinin items map'ine testSong1'i direkt ekleyelim
        // Böylece songDAO.getById çağrılmadan önbellekten gelecek
        Field itemsField = MusicCollectionBase.class.getDeclaredField("items");
        itemsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Song> items = (java.util.Map<String, Song>) itemsField.get(songCollection);
        items.put(testSong1.getId(), testSong1);
        
        // Execute
        Song result = songCollection.getById("song1");
        
        // Verify
        assertNotNull(result);
        assertEquals("song1", result.getId());
        assertEquals("Test Song 1", result.getName());
        verify(songDAO, never()).getById("song1"); // Çünkü önbellekten gelmeli
    }
    
    @Test
    void getById_ShouldFetchFromDatabaseWhenNotInCache() {
        // Setup
        when(songDAO.getById("song1")).thenReturn(testSong1);
        
        // Execute
        Song result = songCollection.getById("song1");
        
        // Verify
        assertNotNull(result);
        assertEquals("song1", result.getId());
        assertEquals("Test Song 1", result.getName());
        verify(songDAO).getById("song1"); // Veritabanından alınmalı
    }
    
    @Test
    void getById_ShouldReturnNullForInvalidId() {
        // Execute
        Song result = songCollection.getById(null);
        
        // Verify
        assertNull(result);
        verify(songDAO, never()).getById(any());
    }
    
    @Test
    void getById_ShouldReturnNullWhenNotFound() {
        // Setup
        when(songDAO.getById("nonexistent")).thenReturn(null);
        
        // Execute
        Song result = songCollection.getById("nonexistent");
        
        // Verify
        assertNull(result);
        verify(songDAO).getById("nonexistent");
    }

    @Test
    void getAll_ShouldReturnAllSongsFromDatabase() {
        // Setup
        List<Song> songs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(songs);
        
        // Execute
        List<Song> result = songCollection.getAll();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSong1));
        assertTrue(result.contains(testSong2));
        verify(songDAO).getAll();
    }

    @Test
    void remove_ShouldRemoveSongCompletely() throws Exception {
        // Setup - items map'e direkt olarak şarkıyı ekleyelim
        when(songDAO.insert(any(Song.class))).thenReturn(true);
        Field itemsField = MusicCollectionBase.class.getDeclaredField("items");
        itemsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Song> items = (java.util.Map<String, Song>) itemsField.get(songCollection);
        items.put(testSong1.getId(), testSong1);
        
        // Execute
        boolean result = songCollection.remove("song1");
        
        // Verify
        assertTrue(result);
        assertFalse(songCollection.contains("song1"));
        verify(songDAO).delete("song1");
    }
    
    @Test
    void remove_ShouldReturnFalseForInvalidId() {
        // Execute
        boolean result = songCollection.remove(null);
        
        // Verify
        assertFalse(result);
        verify(songDAO, never()).delete(any());
    }

    @Test
    void loadFromDatabase_ShouldLoadAllSongs() throws Exception {
        // Setup
        List<Song> songs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(songs);
        
        // loadFromDatabase metodunu direkt çağırmak için reflection kullanıyoruz
        java.lang.reflect.Method method = SongCollection.class.getDeclaredMethod("loadFromDatabase");
        method.setAccessible(true);
        method.invoke(songCollection);
        
        // Verify
        assertTrue(songCollection.contains("song1"));
        assertTrue(songCollection.contains("song2"));
        verify(songDAO).getAll();
    }

    @Test
    void searchByName_ShouldReturnMatchingSongs() {
        // Setup
        List<Song> allSongs = Arrays.asList(
            testSong1,
            testSong2,
            new Song("Another Song", testArtist, 300),
            new Song("Special Track", testArtist, 180)
        );
        
        // "Test" kelimesini içeren şarkılar için sonuçları filtreleyelim
        when(songDAO.getAll()).thenReturn(allSongs);
        
        // Execute
        List<Song> result = songCollection.searchByName("Test");
        
        // Verify
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Test Song 1")));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Test Song 2")));
    }
    
    @Test
    void searchByName_ShouldReturnEmptyListForInvalidSearchTerm() {
        // Execute
        List<Song> result = songCollection.searchByName(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getByArtist_ShouldReturnSongsByArtist() {
        // Setup
        List<Song> allSongs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(allSongs);
        
        // Önce verileri yükleyelim
        songCollection.getAll();
        
        // Execute
        List<Song> result = songCollection.getByArtist(testArtist);
        
        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains(testSong1));
        assertTrue(result.contains(testSong2));
    }
    
    @Test
    void getByArtist_ShouldReturnEmptyListForNullArtist() {
        // Execute
        List<Song> result = songCollection.getByArtist(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getByAlbum_ShouldReturnSongsByAlbum() {
        // Setup
        List<Song> allSongs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(allSongs);
        
        // Önce verileri yükleyelim
        songCollection.getAll();
        
        // Execute
        List<Song> result = songCollection.getByAlbum(testAlbum);
        
        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains(testSong1));
        assertTrue(result.contains(testSong2));
    }
    
    @Test
    void getByAlbum_ShouldReturnEmptyListForNullAlbum() {
        // Execute
        List<Song> result = songCollection.getByAlbum(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getByGenre_ShouldReturnSongsByGenre() {
        // Setup
        List<Song> allSongs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(allSongs);
        
        // Önce verileri yükleyelim
        songCollection.getAll();
        
        // Execute
        List<Song> result = songCollection.getByGenre("Rock");
        
        // Verify
        assertEquals(1, result.size());
        assertEquals(testSong1, result.get(0));
    }
    
    @Test
    void getByGenre_ShouldReturnEmptyListForNullGenre() {
        // Execute
        List<Song> result = songCollection.getByGenre(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getByGenre_ShouldReturnEmptyListForNonExistingGenre() {
        // Setup
        List<Song> allSongs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(allSongs);
        
        // Önce verileri yükleyelim
        songCollection.getAll();
        
        // Execute
        List<Song> result = songCollection.getByGenre("Jazz");
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveToFile_ShouldAlwaysReturnTrue() {
        // Execute
        boolean result = songCollection.saveToFile("dummy.file");
        
        // Verify
        assertTrue(result);
    }

    @Test
    void loadFromFile_ShouldLoadFromDatabaseAndReturnSuccess() {
        // Setup
        List<Song> songs = Arrays.asList(testSong1, testSong2);
        when(songDAO.getAll()).thenReturn(songs);
        
        // Execute
        boolean result = songCollection.loadFromFile("dummy.file");
        
        // Verify
        assertTrue(result);
        verify(songDAO).getAll();
    }
    
    @Test
    void loadFromFile_ShouldReturnTrueEvenWhenNoSongsFound() {
        // Setup
        when(songDAO.getAll()).thenReturn(new ArrayList<>());
        
        // Execute
        boolean result = songCollection.loadFromFile("dummy.file");
        
        // Verify - loadFromFile() her zaman true döndüğü için bunu bekliyoruz
        assertTrue(result);
        verify(songDAO).getAll();
    }
    
    @Test
    void getItemId_ShouldReturnSongId() throws Exception {
        // Execute - private metodu çağırmak için reflection kullanıyoruz
        java.lang.reflect.Method method = SongCollection.class.getDeclaredMethod("getItemId", Song.class);
        method.setAccessible(true);
        String result = (String) method.invoke(songCollection, testSong1);
        
        // Verify
        assertEquals("song1", result);
    }
}