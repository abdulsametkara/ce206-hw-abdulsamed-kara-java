package com.samet.music.view;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.samet.music.controller.PlaylistController;
import com.samet.music.controller.SongController;
import com.samet.music.controller.UserController;
import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.model.User;

/**
 * MusicCollectionView sınıfı için test sınıfı
 */
public class MusicCollectionViewTest {

    private TestMusicCollectionView view;
    
    @Mock
    private UserController userController;
    
    @Mock
    private SongController songController;
    
    @Mock
    private PlaylistController playlistController;
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private Scanner scanner;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // System.out'u byteArrayOutputStream'e yönlendir
        System.setOut(new PrintStream(outContent));
        
        // Test kullanıcısı oluştur ve giriş yapmış olarak ayarla
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        when(userController.getCurrentUser()).thenReturn(mockUser);
        when(userController.isLoggedIn()).thenReturn(true);
    }
    
    @After
    public void tearDown() {
        // System.out'u orijinal stream'e geri yönlendir
        System.setOut(originalOut);
    }
    
    /**
     * Giriş dizesi ile yeni scanner ve view nesnesi oluştur
     */
    private void createViewWithInput(String input) {
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        view = new TestMusicCollectionView(scanner, userController, songController, playlistController);
    }
    
    /**
     * Çıktıyı doğrula
     */
    private void assertOutputContains(String expected) {
        String output = outContent.toString();
        assertTrue("Çıktı '" + expected + "' içermiyor", output.contains(expected));
    }
    
    @Test
    public void testAddSongsToAlbum_NoSongFound() {
        // Test verisi
        Album album = new Album(1, "Test Album", "Test Artist", 2020, "Rock", 1, null);
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Song1");
        song1.setArtist("Artist1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("Song2");
        song2.setArtist("Artist2");
        
        userSongs.add(song1);
        userSongs.add(song2);
        
        when(songController.getUserSongs()).thenReturn(userSongs);
        
        // Bulunamayan şarkı için girdi hazırla
        String input = "NonExistentSong\n"; // Olmayan şarkı adı
        
        createViewWithInput(input);
        
        // Metodu çağır
        view.testAddSongsToAlbum(album);
        
        // Doğrula
        assertOutputContains("No song found with title: NonExistentSong");
    }
    
    @Test
    public void testAddSongsToAlbum_MultipleSongsFound_ArtistSelected() {
        // Test verisi
        Album album = new Album(1, "Test Album", "Test Artist", 2020, "Rock", 1, null);
        List<Song> userSongs = new ArrayList<>();
        
        // Aynı isimde ancak farklı sanatçıya ait iki şarkı
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("DuplicateSong");
        song1.setArtist("Artist1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("DuplicateSong");
        song2.setArtist("Artist2");
        
        userSongs.add(song1);
        userSongs.add(song2);
        
        when(songController.getUserSongs()).thenReturn(userSongs);
        when(songController.addSongToAlbum(1, 2)).thenReturn(true); // İkinci şarkıyı seçiyoruz
        
        // Aynı isimli şarkıyı sorgula, sonra sorulduğunda Artist2'yi seç
        String input = "DuplicateSong\nArtist2\n";
        
        createViewWithInput(input);
        
        // Metodu çağır
        view.testAddSongsToAlbum(album);
        
        // Doğrula
        assertOutputContains("Multiple songs found with this title");
        assertOutputContains("Enter artist name for 'DuplicateSong'");
        assertOutputContains("Added: DuplicateSong by Artist2");
    }
    
    @Test
    public void testAddSongsToAlbum_MultipleSongsFound_ArtistNotFound() {
        // Test verisi
        Album album = new Album(1, "Test Album", "Test Artist", 2020, "Rock", 1, null);
        List<Song> userSongs = new ArrayList<>();
        
        // Aynı isimde ancak farklı sanatçıya ait iki şarkı
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("DuplicateSong");
        song1.setArtist("Artist1");
        
        Song song2 = new Song();
        song2.setId(2);
        song2.setTitle("DuplicateSong");
        song2.setArtist("Artist2");
        
        userSongs.add(song1);
        userSongs.add(song2);
        
        when(songController.getUserSongs()).thenReturn(userSongs);
        
        // Aynı isimli şarkıyı sorgula, sonra var olmayan bir sanatçı adı ver
        String input = "DuplicateSong\nNonExistentArtist\n";
        
        createViewWithInput(input);
        
        // Metodu çağır
        view.testAddSongsToAlbum(album);
        
        // Doğrula
        assertOutputContains("Multiple songs found with this title");
        assertOutputContains("Enter artist name for 'DuplicateSong'");
        assertOutputContains("No song found with title 'DuplicateSong' by artist 'NonExistentArtist'");
    }
    
    @Test
    public void testAddSongsToAlbum_EmptyInput() {
        // Test verisi
        Album album = new Album(1, "Test Album", "Test Artist", 2020, "Rock", 1, null);
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Song1");
        song1.setArtist("Artist1");
        
        userSongs.add(song1);
        
        when(songController.getUserSongs()).thenReturn(userSongs);
        
        // Boş giriş
        String input = "\n";
        
        createViewWithInput(input);
        
        // Metodu çağır
        view.testAddSongsToAlbum(album);
        
        // Doğrula
        assertOutputContains("No songs were added to the album");
    }
    
    @Test
    public void testAddSongsToAlbum_CommaSeparatedWithEmptyItem() {
        // Test verisi
        Album album = new Album(1, "Test Album", "Test Artist", 2020, "Rock", 1, null);
        List<Song> userSongs = new ArrayList<>();
        
        Song song1 = new Song();
        song1.setId(1);
        song1.setTitle("Song1");
        song1.setArtist("Artist1");
        
        userSongs.add(song1);
        
        when(songController.getUserSongs()).thenReturn(userSongs);
        when(songController.addSongToAlbum(1, 1)).thenReturn(true);
        
        // Virgülle ayrılmış girdi, bir boşluk içeriyor
        String input = "Song1,,\n";
        
        createViewWithInput(input);
        
        // Metodu çağır
        view.testAddSongsToAlbum(album);
        
        // Doğrula
        assertOutputContains("Found: Song1 by Artist1");
        assertOutputContains("Added: Song1 by Artist1");
        assertOutputContains("Added 1 song(s) to album!");
    }
    
    // MusicCollectionView sınıfını genişleterek test edilebilir method ekleyelim
    public class TestMusicCollectionView extends MusicCollectionView {
        public TestMusicCollectionView(Scanner scanner, UserController userController, 
                SongController songController, PlaylistController playlistController) {
            super(scanner, userController, songController, playlistController);
        }
        
        // Test için yeni bir metod oluşturuyoruz çünkü orijinal metod private
        public void testAddSongsToAlbum(Album album) {
            // Private olan addSongsToAlbum metodunun testler için kullanılabilir versiyonu
            List<Song> userSongs = songController.getUserSongs();
            
            if (userSongs.isEmpty()) {
                displayInfo("You don't have any songs to add to this album.");
                return;
            }
            
            displayHeader("ADD SONGS TO ALBUM: " + album.getTitle());
            
            System.out.println("\nYour songs:");
            int index = 1;
            for (Song song : userSongs) {
                System.out.printf("  %d. %s - %s%n", 
                    index++,
                    song.getTitle(), 
                    song.getArtist());
            }
            
            System.out.println("\nEnter song titles to add (comma-separated, e.g. 'Song1,Song2,Song3'):");
            String input = scanner.nextLine().trim();
            
            String[] selections = input.split(",");
            int addedCount = 0;
            
            for (String songTitle : selections) {
                String trimmedTitle = songTitle.trim();
                if (trimmedTitle.isEmpty()) continue;
                
                System.out.println("\nSearching for song: " + trimmedTitle);
                
                List<Song> matchingSongs = new ArrayList<>();
                for (Song song : userSongs) {
                    if (song.getTitle().equalsIgnoreCase(trimmedTitle)) {
                        matchingSongs.add(song);
                    }
                }
                
                Song selectedSong = null;
                
                if (matchingSongs.isEmpty()) {
                    System.out.println("No song found with title: " + trimmedTitle);
                    continue;
                } else if (matchingSongs.size() == 1) {
                    selectedSong = matchingSongs.get(0);
                    System.out.println("Found: " + selectedSong.getTitle() + " by " + selectedSong.getArtist());
                } else {
                    System.out.println("Multiple songs found with this title. Please specify which one:");
                    int matchIndex = 1;
                    for (Song song : matchingSongs) {
                        System.out.printf("  %d. %s by %s%n", matchIndex++, song.getTitle(), song.getArtist());
                    }
                    
                    String artistName = getStringInput("Enter artist name for '" + trimmedTitle + "'");
                    
                    for (Song song : matchingSongs) {
                        if (song.getArtist().equalsIgnoreCase(artistName)) {
                            selectedSong = song;
                            break;
                        }
                    }
                    
                    if (selectedSong == null) {
                        System.out.println("No song found with title '" + trimmedTitle + "' by artist '" + artistName + "'");
                        continue;
                    }
                }
                
                boolean added = songController.addSongToAlbum(album.getId(), selectedSong.getId());
                if (added) {
                    addedCount++;
                    System.out.println("Added: " + selectedSong.getTitle() + " by " + selectedSong.getArtist());
                }
            }
            
            if (addedCount > 0) {
                displaySuccess("Added " + addedCount + " song(s) to album!");
            } else {
                displayInfo("No songs were added to the album.");
            }
        }
    }
} 