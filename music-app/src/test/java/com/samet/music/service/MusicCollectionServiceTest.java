package com.samet.music.service;

import com.samet.music.dao.*;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.repository.AlbumCollection;
import com.samet.music.repository.ArtistCollection;
import com.samet.music.repository.PlaylistCollection;
import com.samet.music.repository.SongCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MusicCollectionServiceTest {

    // Mock bağımlılıklar
    @Mock
    private SongDAO songDAO;
    
    @Mock
    private AlbumDAO albumDAO;
    
    @Mock
    private ArtistDAO artistDAO;
    
    @Mock
    private PlaylistDAO playlistDAO;
    
    @Mock
    private ArtistCollection artistCollection;
    
    @Mock
    private AlbumCollection albumCollection;
    
    @Mock
    private SongCollection songCollection;
    
    @Mock
    private PlaylistCollection playlistCollection;
    
    @Mock
    private MusicFactory musicFactory;
    
    // Test edilen sınıf
    private MusicCollectionService musicCollectionService;
    
    // Test verileri
    private Artist testArtist;
    private Album testAlbum;
    private Song testSong;
    private Playlist testPlaylist;
    
    @Before
    public void setUp() throws Exception {
        // Test verileri hazırlama
        testArtist = new Artist("Test Artist");
        testArtist.setId("artist1");
        testArtist.setBiography("Test biography");
        
        testAlbum = new Album("Test Album", testArtist, 2023);
        testAlbum.setId("album1");
        testAlbum.setGenre("Rock");
        
        testSong = new Song("Test Song", testArtist, 240);
        testSong.setId("song1");
        testSong.setGenre("Rock");
        testSong.setAlbum(testAlbum);
        
        testPlaylist = new Playlist("Test Playlist", "Test description");
        testPlaylist.setId("playlist1");
        
        // Private constructor'a erişim
        Constructor<MusicCollectionService> constructor = MusicCollectionService.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        musicCollectionService = constructor.newInstance();
        
        // Bağımlılıkları enjekte et
        injectDependencies();
    }
    
    private void injectDependencies() throws Exception {
        setField("artistCollection", artistCollection);
        setField("albumCollection", albumCollection);
        setField("songCollection", songCollection);
        setField("playlistCollection", playlistCollection);
        setField("musicFactory", musicFactory);
        setField("songDAO", songDAO);
        setField("albumDAO", albumDAO);
        setField("artistDAO", artistDAO);
        setField("playlistDAO", playlistDAO);
    }
    
    private void setField(String fieldName, Object value) throws Exception {
        Field field = MusicCollectionService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(musicCollectionService, value);
    }

    // === ARTIST TEST METHODS ===
    
    @Test
    public void testAddArtist_Success() {
        // Mock ayarları
        when(musicFactory.createArtist("New Artist", "New artist biography"))
            .thenReturn(testArtist);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addArtist("New Artist", "New artist biography");
        
        // Doğrulama
        assertTrue(result);
        verify(artistCollection).add(testArtist);
    }
    
    @Test
    public void testAddArtist_InvalidName() {
        // Test metodu çağrısı
        boolean result = musicCollectionService.addArtist("", "New artist biography");
        
        // Doğrulama
        assertFalse(result);
        verify(artistCollection, never()).add(any(Artist.class));
    }
    
    @Test
    public void testGetArtistById_Success() {
        // Mock ayarları
        when(artistCollection.getById("artist1")).thenReturn(testArtist);
        
        // Test metodu çağrısı
        Artist result = musicCollectionService.getArtistById("artist1");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(testArtist, result);
        verify(artistCollection).getById("artist1");
    }
    
    @Test
    public void testGetArtistById_NotFound() {
        // Mock ayarları
        when(artistCollection.getById("nonexistent")).thenReturn(null);
        
        // Test metodu çağrısı
        Artist result = musicCollectionService.getArtistById("nonexistent");
        
        // Doğrulama
        assertNull(result);
        verify(artistCollection).getById("nonexistent");
    }
    
    @Test
    public void testSearchArtistsByName_Success() {
        // Test verisi
        List<Artist> artistList = Collections.singletonList(testArtist);
        
        // Mock ayarları
        when(artistCollection.searchByName("Test")).thenReturn(artistList);
        
        // Test metodu çağrısı
        List<Artist> result = musicCollectionService.searchArtistsByName("Test");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(artistList, result);
        verify(artistCollection).searchByName("Test");
    }
    
    @Test
    public void testGetAllArtists_Success() {
        // Test verisi
        List<Artist> artistList = Collections.singletonList(testArtist);
        
        // Mock ayarları
        when(artistCollection.getAll()).thenReturn(artistList);
        
        // Test metodu çağrısı
        List<Artist> result = musicCollectionService.getAllArtists();
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(artistList, result);
        verify(artistCollection).getAll();
    }
    
    @Test
    public void testRemoveArtist_Success() {
        // Mock ayarları
        when(artistCollection.getById("artist1")).thenReturn(testArtist);
        when(artistDAO.delete("artist1")).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeArtist("artist1");
        
        // Doğrulama
        assertTrue(result);
        verify(artistDAO).delete("artist1");
        verify(artistCollection).remove("artist1");
    }
    
    @Test
    public void testRemoveArtist_NotFound() {
        // Mock ayarları
        when(artistCollection.getById("nonexistent")).thenReturn(null);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeArtist("nonexistent");
        
        // Doğrulama
        assertFalse(result);
        verify(artistDAO, never()).delete(anyString());
        verify(artistCollection, never()).remove(anyString());
    }

    // === ALBUM TEST METHODS ===
    
    @Test
    public void testAddAlbum_Success() {
        // Test verisi
        String name = "New Album";
        String artistId = "artist1";
        int releaseYear = 2024;
        String genre = "Pop";
        
        // Mock ayarları
        when(artistCollection.getById(artistId)).thenReturn(testArtist);
        when(musicFactory.createAlbum(name, testArtist, releaseYear, genre)).thenReturn(testAlbum);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addAlbum(name, artistId, releaseYear, genre);
        
        // Doğrulama
        assertTrue(result);
        verify(albumCollection).add(testAlbum);
    }
    
    @Test
    public void testAddAlbum_InvalidName() {
        // Test verisi
        String name = "";
        String artistId = "artist1";
        int releaseYear = 2024;
        String genre = "Pop";
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addAlbum(name, artistId, releaseYear, genre);
        
        // Doğrulama
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testAddAlbum_ArtistNotFound() {
        // Test verisi
        String name = "New Album";
        String artistId = "nonexistent";
        int releaseYear = 2024;
        String genre = "Pop";
        
        // Mock ayarları
        when(artistCollection.getById(artistId)).thenReturn(null);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addAlbum(name, artistId, releaseYear, genre);
        
        // Doğrulama
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testGetAlbumById_Success() {
        // Mock ayarları
        when(albumCollection.getById("album1")).thenReturn(testAlbum);
        
        // Test metodu çağrısı
        Album result = musicCollectionService.getAlbumById("album1");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(testAlbum, result);
        verify(albumCollection).getById("album1");
    }
    
    @Test
    public void testRemoveAlbumWithSongs_Success() {
        // Mock ayarları
        when(albumCollection.getById("album1")).thenReturn(testAlbum);
        when(albumDAO.delete("album1")).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeAlbum("album1", true);
        
        // Doğrulama
        assertTrue(result);
        verify(albumDAO).delete("album1");
        verify(albumCollection).remove("album1");
    }
    
    @Test
    public void testRemoveAlbumWithoutSongs_Success() {
        // Mock ayarları
        when(albumCollection.getById("album1")).thenReturn(testAlbum);
        when(albumDAO.deleteWithoutSongs("album1")).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeAlbum("album1", false);
        
        // Doğrulama
        assertTrue(result);
        verify(albumDAO).deleteWithoutSongs("album1");
        verify(albumCollection).remove("album1");
    }

    // === SONG TEST METHODS ===
    
    @Test
    public void testAddSong_Success() {
        // Test verisi
        String name = "New Song";
        String artistId = "artist1";
        int duration = 180;
        String genre = "Rock";
        
        // Mock ayarları
        when(artistCollection.getById(artistId)).thenReturn(testArtist);
        when(musicFactory.createSong(name, testArtist, duration, genre)).thenReturn(testSong);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addSong(name, artistId, duration, genre);
        
        // Doğrulama
        assertTrue(result);
        verify(songCollection).add(testSong);
    }
    
    @Test
    public void testAddSongToAlbum_Success() {
        // Mock ayarları
        when(songCollection.getById("song1")).thenReturn(testSong);
        when(albumCollection.getById("album1")).thenReturn(testAlbum);
        when(songDAO.update(testSong)).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addSongToAlbum("song1", "album1");
        
        // Doğrulama
        assertTrue(result);
        assertEquals(testAlbum, testSong.getAlbum());
        verify(songDAO).update(testSong);
    }
    
    @Test
    public void testGetSongById_Success() {
        // Mock ayarları
        when(songCollection.getById("song1")).thenReturn(testSong);
        
        // Test metodu çağrısı
        Song result = musicCollectionService.getSongById("song1");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(testSong, result);
        verify(songCollection).getById("song1");
    }
    
    @Test
    public void testRemoveSong_Success() {
        // Mock ayarları
        when(songCollection.getById("song1")).thenReturn(testSong);
        when(songDAO.delete("song1")).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeSong("song1");
        
        // Doğrulama
        assertTrue(result);
        verify(songDAO).delete("song1");
        verify(songCollection).remove("song1");
    }
    
    @Test
    public void testSearchSongsByName_Success() {
        // Test verisi
        List<Song> songList = Collections.singletonList(testSong);
        
        // Mock ayarları
        when(songCollection.searchByName("Test")).thenReturn(songList);
        
        // Test metodu çağrısı
        List<Song> result = musicCollectionService.searchSongsByName("Test");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(songList, result);
        verify(songCollection).searchByName("Test");
    }

    // === PLAYLIST TEST METHODS ===
    
    @Test
    public void testCreatePlaylist_Success() {
        // Test verisi
        String name = "New Playlist";
        String description = "New playlist description";
        
        // Capture the playlist for verification
        doAnswer(invocation -> {
            Playlist p = invocation.getArgument(0);
            assertEquals(name, p.getName());
            assertEquals(description, p.getDescription());
            return null;
        }).when(playlistCollection).add(any(Playlist.class));
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.createPlaylist(name, description);
        
        // Doğrulama
        assertTrue(result);
        verify(playlistCollection).add(any(Playlist.class));
    }
    
    @Test
    public void testCreatePlaylist_InvalidName() {
        // Test verisi - boş isim
        String name = "";
        String description = "New playlist description";
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.createPlaylist(name, description);
        
        // Doğrulama
        assertFalse(result);
        verify(playlistCollection, never()).add(any(Playlist.class));
    }
    
    @Test
    public void testGetPlaylistById_Success() {
        // Mock ayarları
        when(playlistCollection.getById("playlist1")).thenReturn(testPlaylist);
        
        // Test metodu çağrısı
        Playlist result = musicCollectionService.getPlaylistById("playlist1");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(testPlaylist, result);
        verify(playlistCollection).getById("playlist1");
    }
    
    @Test
    public void testAddSongToPlaylist_Success() {
        // Mock ayarları
        when(songCollection.getById("song1")).thenReturn(testSong);
        when(playlistCollection.getById("playlist1")).thenReturn(testPlaylist);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.addSongToPlaylist("song1", "playlist1");
        
        // Doğrulama
        assertTrue(result);
        verify(playlistCollection).addSongToPlaylist("playlist1", "song1");
    }
    
    @Test
    public void testRemoveSongFromPlaylist_Success() {
        // Mock ayarları
        when(songCollection.getById("song1")).thenReturn(testSong);
        when(playlistCollection.getById("playlist1")).thenReturn(testPlaylist);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.removeSongFromPlaylist("song1", "playlist1");
        
        // Doğrulama
        assertTrue(result);
        verify(playlistCollection).removeSongFromPlaylist("playlist1", "song1");
    }
    
    @Test
    public void testGetSongsInPlaylist_Success() {
        // Test verisi
        List<Song> songList = Collections.singletonList(testSong);
        
        // Mock ayarları
        when(playlistCollection.getById("playlist1")).thenReturn(testPlaylist);
        
        // testPlaylist bir mock değil, bu yüzden doğrudan when().thenReturn() kullanamayız
        // Bunun yerine şarkıyı ekleyelim
        testPlaylist.addSong(testSong);
        
        // Test metodu çağrısı
        List<Song> result = musicCollectionService.getSongsInPlaylist("playlist1");
        
        // Doğrulama
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSong, result.get(0));
        verify(playlistCollection).getById("playlist1");
    }
    
    @Test
    public void testUpdatePlaylist_Success() {
        // Mock ayarları
        when(playlistCollection.getById("playlist1")).thenReturn(testPlaylist);
        when(playlistDAO.update(testPlaylist)).thenReturn(true);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.updatePlaylist(testPlaylist);
        
        // Doğrulama
        assertTrue(result);
        verify(playlistDAO).update(testPlaylist);
    }
    
    @Test
    public void testUpdatePlaylist_NotFound() {
        // Mock ayarları
        when(playlistCollection.getById("playlist1")).thenReturn(null);
        
        // Test metodu çağrısı
        boolean result = musicCollectionService.updatePlaylist(testPlaylist);
        
        // Doğrulama
        assertFalse(result);
        verify(playlistDAO, never()).update(any(Playlist.class));
    }
    
    @Test
    public void testAddPlaylist_Success() {
        // Test metodu çağrısı
        boolean result = musicCollectionService.addPlaylist(testPlaylist);
        
        // Doğrulama
        assertTrue(result);
        verify(playlistCollection).add(testPlaylist);
    }
    
    @Test
    public void testAddPlaylist_Null() {
        // Test metodu çağrısı
        boolean result = musicCollectionService.addPlaylist(null);
        
        // Doğrulama
        assertFalse(result);
        verify(playlistCollection, never()).add(any(Playlist.class));
    }
} 