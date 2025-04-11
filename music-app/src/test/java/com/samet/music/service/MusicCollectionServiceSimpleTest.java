package com.samet.music.service;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.*;

/**
 * MusicCollectionService için basit test sınıfı.
 * Bu testler, entegrasyon test yapısında, gerçek veritabanına bağlanmadan 
 * servisin temel işlevselliğini kontrol eder.
 */
@RunWith(JUnit4.class)
public class MusicCollectionServiceSimpleTest {

    /**
     * Parametrik değer kontrolleri
     */
    @Test
    public void testParameterValidation() {
        // Boş veya null değer kontrolü
        assertFalse("Boş sanatçı adı kabul edilmemeli", MusicCollectionService.getInstance().addArtist("", "Bio"));
        assertFalse("Null sanatçı adı kabul edilmemeli", MusicCollectionService.getInstance().addArtist(null, "Bio"));
        
        assertFalse("Boş albüm adı kabul edilmemeli", MusicCollectionService.getInstance().addAlbum("", "artistId", 2023, "Rock"));
        assertFalse("Null albüm adı kabul edilmemeli", MusicCollectionService.getInstance().addAlbum(null, "artistId", 2023, "Rock"));
        assertFalse("Negatif yıl kabul edilmemeli", MusicCollectionService.getInstance().addAlbum("Album", "artistId", -1, "Rock"));
        assertFalse("0 yıl kabul edilmemeli", MusicCollectionService.getInstance().addAlbum("Album", "artistId", 0, "Rock"));
        
        assertFalse("Boş şarkı adı kabul edilmemeli", MusicCollectionService.getInstance().addSong("", "artistId", 180, "Rock"));
        assertFalse("Null şarkı adı kabul edilmemeli", MusicCollectionService.getInstance().addSong(null, "artistId", 180, "Rock"));
        assertFalse("Negatif süre kabul edilmemeli", MusicCollectionService.getInstance().addSong("Song", "artistId", -10, "Rock"));
        assertFalse("Sıfır süre kabul edilmemeli", MusicCollectionService.getInstance().addSong("Song", "artistId", 0, "Rock"));
        
        assertFalse("Boş çalma listesi adı kabul edilmemeli", MusicCollectionService.getInstance().createPlaylist("", "Description"));
        assertFalse("Null çalma listesi adı kabul edilmemeli", MusicCollectionService.getInstance().createPlaylist(null, "Description"));
        
        assertFalse("Null sanatçı ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeArtist(null));
        assertFalse("Boş sanatçı ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeArtist(""));
        
        assertFalse("Null albüm ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeAlbum(null, true));
        assertFalse("Boş albüm ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeAlbum("", true));
        
        assertFalse("Null şarkı ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeSong(null));
        assertFalse("Boş şarkı ID'si kabul edilmemeli", MusicCollectionService.getInstance().removeSong(""));
        
        // ID bulunamayan durum kontrolleri
        List<Song> emptySongList = MusicCollectionService.getInstance().getSongsInPlaylist(null);
        assertTrue("Null çalma listesi ID'si için boş liste dönmeli", emptySongList.isEmpty());
        
        emptySongList = MusicCollectionService.getInstance().getSongsInPlaylist("");
        assertTrue("Boş çalma listesi ID'si için boş liste dönmeli", emptySongList.isEmpty());
        
        assertNull("Var olmayan ID için null sonuç dönmeli", MusicCollectionService.getInstance().getArtistById("nonexistent"));
        assertNull("Var olmayan ID için null sonuç dönmeli", MusicCollectionService.getInstance().getAlbumById("nonexistent"));
        assertNull("Var olmayan ID için null sonuç dönmeli", MusicCollectionService.getInstance().getSongById("nonexistent"));
        assertNull("Var olmayan ID için null sonuç dönmeli", MusicCollectionService.getInstance().getPlaylistById("nonexistent"));
    }
    
    /**
     * Koleksiyon getirme metotlarını test eder
     */
    @Test
    public void testCollectionRetrieval() {
        // Koleksiyon alma metotları
        List<Artist> artists = MusicCollectionService.getInstance().getAllArtists();
        assertNotNull("Sanatçı listesi null olmamalı", artists);
        
        List<Artist> searchedArtists = MusicCollectionService.getInstance().searchArtistsByName("any");
        assertNotNull("Sanatçı arama sonucu null olmamalı", searchedArtists);
        
        List<Album> albums = MusicCollectionService.getInstance().getAllAlbums();
        assertNotNull("Albüm listesi null olmamalı", albums);
        
        List<Album> searchedAlbums = MusicCollectionService.getInstance().searchAlbumsByName("any");
        assertNotNull("Albüm arama sonucu null olmamalı", searchedAlbums);
        
        List<Song> songs = MusicCollectionService.getInstance().getAllSongs();
        assertNotNull("Şarkı listesi null olmamalı", songs);
        
        List<Song> searchedSongs = MusicCollectionService.getInstance().searchSongsByName("any");
        assertNotNull("Şarkı arama sonucu null olmamalı", searchedSongs);
        
        List<Playlist> playlists = MusicCollectionService.getInstance().getAllPlaylists();
        assertNotNull("Çalma listesi null olmamalı", playlists);
    }
    
    /**
     * Null nesne eklenememe durumunu test eder
     */
    @Test
    public void testAddNullObjects() {
        assertFalse("Null çalma listesi eklenememeli", MusicCollectionService.getInstance().addPlaylist(null));
        assertFalse("Null çalma listesi güncellenememeli", MusicCollectionService.getInstance().updatePlaylist(null));
    }
} 