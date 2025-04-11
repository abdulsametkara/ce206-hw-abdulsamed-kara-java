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

/**
 * Edge case ve hata durumu testleri için MusicCollectionService test sınıfı
 */
@RunWith(MockitoJUnitRunner.class)
public class MusicCollectionServiceEdgeCaseTest {
    
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
        
        testAlbum = new Album("Test Album", testArtist, 2023);
        testAlbum.setId("album1");
        
        testSong = new Song("Test Song", testArtist, 240);
        testSong.setId("song1");
        
        testPlaylist = new Playlist("Test Playlist", "Test description");
        testPlaylist.setId("playlist1");
        
        // Reflection ile MusicCollectionService private constructor'ını çağır
        Constructor<MusicCollectionService> constructor = MusicCollectionService.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        musicCollectionService = constructor.newInstance();
        
        // Mock bağımlılıkları enjekte et
        injectMockDependencies();
    }
    
    // Mock bağımlılıkları enjekte et
    private void injectMockDependencies() throws Exception {
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
    
    // Reflection ile field ayarlama
    private void setField(String fieldName, Object fieldValue) throws Exception {
        Field field = MusicCollectionService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(musicCollectionService, fieldValue);
    }
    
    // === PARAMETRIK DEĞER TESTLERI ===
    
    @Test
    public void testAddArtist_NullName() {
        // Test with null name
        boolean result = musicCollectionService.addArtist(null, "Bio");
        
        // Verification
        assertFalse(result);
        verify(artistCollection, never()).add(any(Artist.class));
    }
    
    @Test
    public void testAddArtist_EmptyName() {
        // Test with empty name
        boolean result = musicCollectionService.addArtist("", "Bio");
        
        // Verification
        assertFalse(result);
        verify(artistCollection, never()).add(any(Artist.class));
    }
    
    @Test
    public void testAddAlbum_NullName() {
        // Test with null name
        boolean result = musicCollectionService.addAlbum(null, "artist1", 2023, "Rock");
        
        // Verification
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testAddAlbum_EmptyName() {
        // Test with empty name
        boolean result = musicCollectionService.addAlbum("", "artist1", 2023, "Rock");
        
        // Verification
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testAddAlbum_NegativeYear() {
        // Test with negative year
        boolean result = musicCollectionService.addAlbum("Album", "artist1", -1, "Rock");
        
        // Verification
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testAddAlbum_ZeroYear() {
        // Test with zero year
        boolean result = musicCollectionService.addAlbum("Album", "artist1", 0, "Rock");
        
        // Verification
        assertFalse(result);
        verify(albumCollection, never()).add(any(Album.class));
    }
    
    @Test
    public void testAddSong_NullName() {
        // Test with null name
        boolean result = musicCollectionService.addSong(null, "artist1", 180, "Rock");
        
        // Verification
        assertFalse(result);
        verify(songCollection, never()).add(any(Song.class));
    }
    
    @Test
    public void testAddSong_EmptyName() {
        // Test with empty name
        boolean result = musicCollectionService.addSong("", "artist1", 180, "Rock");
        
        // Verification
        assertFalse(result);
        verify(songCollection, never()).add(any(Song.class));
    }
    
    @Test
    public void testAddSong_ZeroDuration() {
        // Test with zero duration
        boolean result = musicCollectionService.addSong("Song", "artist1", 0, "Rock");
        
        // Verification
        assertFalse(result);
        verify(songCollection, never()).add(any(Song.class));
    }
    
    @Test
    public void testAddSong_NegativeDuration() {
        // Test with negative duration
        boolean result = musicCollectionService.addSong("Song", "artist1", -10, "Rock");
        
        // Verification
        assertFalse(result);
        verify(songCollection, never()).add(any(Song.class));
    }
    
    @Test
    public void testAddSongToAlbum_NullSong() {
        // Mock setup
        when(songCollection.getById("song1")).thenReturn(null);
        when(albumCollection.getById("album1")).thenReturn(testAlbum);
        
        // Test call
        boolean result = musicCollectionService.addSongToAlbum("song1", "album1");
        
        // Verification
        assertFalse(result);
        verify(songDAO, never()).update(any(Song.class));
    }
    
    @Test
    public void testAddSongToAlbum_NullAlbum() {
        // Mock setup
        when(songCollection.getById("song1")).thenReturn(testSong);
        when(albumCollection.getById("album1")).thenReturn(null);
        
        // Test call
        boolean result = musicCollectionService.addSongToAlbum("song1", "album1");
        
        // Verification
        assertFalse(result);
        verify(songDAO, never()).update(any(Song.class));
    }
    
    @Test
    public void testCreatePlaylist_NullName() {
        // Test with null name
        boolean result = musicCollectionService.createPlaylist(null, "Description");
        
        // Verification
        assertFalse(result);
        verify(playlistCollection, never()).add(any(Playlist.class));
    }
    
    @Test
    public void testCreatePlaylist_EmptyName() {
        // Test with empty name
        boolean result = musicCollectionService.createPlaylist("", "Description");
        
        // Verification
        assertFalse(result);
        verify(playlistCollection, never()).add(any(Playlist.class));
    }
    
    @Test
    public void testRemoveArtist_NullId() {
        // Test with null ID
        boolean result = musicCollectionService.removeArtist(null);
        
        // Verification
        assertFalse(result);
        verify(artistDAO, never()).delete(anyString());
    }
    
    @Test
    public void testRemoveArtist_EmptyId() {
        // Test with empty ID
        boolean result = musicCollectionService.removeArtist("");
        
        // Verification
        assertFalse(result);
        verify(artistDAO, never()).delete(anyString());
    }
    
    @Test
    public void testRemoveAlbum_NullId() {
        // Test with null ID
        boolean result = musicCollectionService.removeAlbum(null, true);
        
        // Verification
        assertFalse(result);
        verify(albumDAO, never()).delete(anyString());
    }
    
    @Test
    public void testRemoveAlbum_EmptyId() {
        // Test with empty ID
        boolean result = musicCollectionService.removeAlbum("", true);
        
        // Verification
        assertFalse(result);
        verify(albumDAO, never()).delete(anyString());
    }
    
    @Test
    public void testRemoveSong_NullId() {
        // Test with null ID
        boolean result = musicCollectionService.removeSong(null);
        
        // Verification
        assertFalse(result);
        verify(songDAO, never()).delete(anyString());
    }
    
    @Test
    public void testRemoveSong_EmptyId() {
        // Test with empty ID
        boolean result = musicCollectionService.removeSong("");
        
        // Verification
        assertFalse(result);
        verify(songDAO, never()).delete(anyString());
    }
    
    @Test
    public void testAddSongToPlaylist_BothNull() {
        // Mock setup
        when(songCollection.getById("song1")).thenReturn(null);
        when(playlistCollection.getById("playlist1")).thenReturn(null);
        
        // Test call
        boolean result = musicCollectionService.addSongToPlaylist("song1", "playlist1");
        
        // Verification
        assertFalse(result);
        verify(playlistCollection, never()).addSongToPlaylist(anyString(), anyString());
    }
    
    @Test
    public void testGetSongsInPlaylist_NullId() {
        // Test call
        List<Song> result = musicCollectionService.getSongsInPlaylist(null);
        
        // Verification
        assertTrue(result.isEmpty());
        verify(playlistCollection, never()).getById(anyString());
    }
    
    @Test
    public void testGetSongsInPlaylist_EmptyId() {
        // Test call
        List<Song> result = musicCollectionService.getSongsInPlaylist("");
        
        // Verification
        assertTrue(result.isEmpty());
        verify(playlistCollection, never()).getById(anyString());
    }
} 