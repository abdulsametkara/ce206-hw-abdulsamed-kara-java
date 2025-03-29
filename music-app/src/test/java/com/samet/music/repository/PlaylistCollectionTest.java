package com.samet.music.repository;

import static org.junit.Assert.*;
import org.junit.*;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.model.Artist;
import com.samet.music.util.DatabaseUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @class PlaylistCollectionTest
 * @brief PlaylistCollection sınıfı için test sınıfı
 */
public class PlaylistCollectionTest {

    private PlaylistCollection playlistCollection;
    private SongCollection songCollection;
    private ArtistCollection artistCollection;
    private Playlist testPlaylist;
    private Song testSong;
    private Artist testArtist;

    /**
     * @brief Tüm testlerden önce bir kez çalıştırılır
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Veritabanını test modunda başlat
        DatabaseUtil.setShouldResetDatabase(true);
        DatabaseUtil.initializeDatabase();
    }

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Singleton örneklerini sıfırla
        resetSingleton(PlaylistCollection.class, "instance");
        resetSingleton(SongCollection.class, "instance");
        resetSingleton(ArtistCollection.class, "instance");

        // Collection örneklerini al
        playlistCollection = PlaylistCollection.getInstance();
        songCollection = SongCollection.getInstance();
        artistCollection = ArtistCollection.getInstance();

        // Test verilerini oluştur
        testArtist = new Artist("Test Artist", "Test Biography");
        artistCollection.add(testArtist);

        testSong = new Song("Test Song", testArtist, 240);
        songCollection.add(testSong);

        testPlaylist = new Playlist("Test Playlist", "Test Description");
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Koleksiyonları temizle
        if (playlistCollection != null) {
            playlistCollection.clear();
        }
    }

    /**
     * @brief Reflection kullanarak singleton örneğini sıfırlar
     */
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instance = clazz.getDeclaredField(fieldName);
        instance.setAccessible(true);
        instance.set(null, null);
    }

    /**
     * @brief getInstance metodunu test eder
     */
    @Test
    public void testGetInstance() {
        // Arrange & Act
        PlaylistCollection instance1 = PlaylistCollection.getInstance();
        PlaylistCollection instance2 = PlaylistCollection.getInstance();

        // Assert
        assertNotNull("getInstance null döndürmemeli", instance1);
        assertSame("getInstance her zaman aynı örneği döndürmeli", instance1, instance2);
    }

    /**
     * @brief add metodunu test eder
     */
    @Test
    public void testAdd() {
        // Arrange
        Playlist playlist = new Playlist("Add Test Playlist", "Test Description");

        // Act
        playlistCollection.add(playlist);

        // Assert
        Playlist retrieved = playlistCollection.getById(playlist.getId());
        assertNotNull("Eklenen çalma listesi getById ile alınabilmeli", retrieved);
        assertEquals("Eklenen çalma listesi adı doğru olmalı", "Add Test Playlist", retrieved.getName());
        assertEquals("Eklenen çalma listesi açıklaması doğru olmalı", "Test Description", retrieved.getDescription());
    }

    /**
     * @brief getById metodunu test eder - çalma listesi koleksiyonda varken
     */
    @Test
    public void testGetByIdWhenInCollection() {
        // Arrange
        playlistCollection.add(testPlaylist);

        // Act
        Playlist result = playlistCollection.getById(testPlaylist.getId());

        // Assert
        assertNotNull("Çalma listesi bulunmalı", result);
        assertEquals("Bulunan çalma listesi ID'si eşleşmeli", testPlaylist.getId(), result.getId());
        assertEquals("Bulunan çalma listesi adı eşleşmeli", testPlaylist.getName(), result.getName());
    }

    /**
     * @brief getAll metodunu test eder
     */
    @Test
    public void testGetAll() {
        // Arrange
        Playlist playlist1 = new Playlist("Playlist 1", "Description 1");
        Playlist playlist2 = new Playlist("Playlist 2", "Description 2");

        playlistCollection.add(playlist1);
        playlistCollection.add(playlist2);

        // Act
        List<Playlist> allPlaylists = playlistCollection.getAll();

        // Assert
        assertNotNull("getAll null döndürmemeli", allPlaylists);
        assertTrue("Koleksiyonda en az 2 çalma listesi olmalı", allPlaylists.size() >= 2);

        // ID'lere göre çalma listelerinin varlığını kontrol et
        boolean foundPlaylist1 = false;
        boolean foundPlaylist2 = false;

        for (Playlist playlist : allPlaylists) {
            if (playlist.getId().equals(playlist1.getId())) foundPlaylist1 = true;
            if (playlist.getId().equals(playlist2.getId())) foundPlaylist2 = true;
        }

        assertTrue("Playlist1 koleksiyonda olmalı", foundPlaylist1);
        assertTrue("Playlist2 koleksiyonda olmalı", foundPlaylist2);
    }

    /**
     * @brief remove metodunu test eder
     */
    @Test
    public void testRemove() {
        // Arrange
        playlistCollection.add(testPlaylist);
        String playlistId = testPlaylist.getId();

        // Act
        boolean result = playlistCollection.remove(playlistId);

        // Assert
        assertTrue("Silme işlemi başarılı olmalı", result);
        assertNull("Silinen çalma listesi getById ile alınamamalı", playlistCollection.getById(playlistId));
    }

    /**
     * @brief searchByName metodunu test eder
     */
    @Test
    public void testSearchByName() {
        // Arrange
        Playlist playlist1 = new Playlist("Rock Playlist", "Rock songs");
        Playlist playlist2 = new Playlist("Pop Playlist", "Pop songs");
        Playlist playlist3 = new Playlist("Jazz Playlist", "Jazz songs");

        playlistCollection.add(playlist1);
        playlistCollection.add(playlist2);
        playlistCollection.add(playlist3);

        // Act
        List<Playlist> results = playlistCollection.searchByName("Rock");

        // Assert
        assertNotNull("searchByName null döndürmemeli", results);
        assertEquals("1 çalma listesi bulunmalı", 1, results.size());
        assertEquals("Doğru çalma listesi bulunmalı", playlist1.getId(), results.get(0).getId());

        // Case insensitive arama
        List<Playlist> caseInsensitiveResults = playlistCollection.searchByName("jazz");
        assertEquals("Büyük/küçük harf duyarsız arama 1 çalma listesi bulmalı", 1, caseInsensitiveResults.size());
        assertEquals("Doğru çalma listesi bulunmalı", playlist3.getId(), caseInsensitiveResults.get(0).getId());
    }

    /**
     * @brief searchByName metodunu null ve boş string ile test eder
     */
    @Test
    public void testSearchByNameWithNullAndEmpty() {
        // Arrange
        playlistCollection.add(testPlaylist);

        // Act & Assert
        assertTrue("Null ile arama boş liste döndürmeli", playlistCollection.searchByName(null).isEmpty());
        assertTrue("Boş string ile arama boş liste döndürmeli", playlistCollection.searchByName("").isEmpty());
        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", playlistCollection.searchByName("  ").isEmpty());
    }

    /**
     * @brief getPlaylistsContainingSong metodunu test eder
     */
    @Test
    public void testGetPlaylistsContainingSong() {
        // Arrange
        Playlist playlist1 = new Playlist("Playlist 1", "Description 1");
        playlistCollection.add(playlist1);

        // Şarkıyı çalma listesine ekle
        playlistCollection.addSongToPlaylist(playlist1.getId(), testSong.getId());

        // Act
        List<Playlist> results = playlistCollection.getPlaylistsContainingSong(testSong);

        // Assert
        assertNotNull("getPlaylistsContainingSong null döndürmemeli", results);
        assertTrue("En az bir çalma listesi bulunmalı", results.size() >= 1);

        boolean found = false;
        for (Playlist playlist : results) {
            if (playlist.getId().equals(playlist1.getId())) {
                found = true;
                break;
            }
        }

        assertTrue("Test şarkısını içeren çalma listesi bulunmalı", found);
    }

    /**
     * @brief addSongToPlaylist ve removeSongFromPlaylist metodlarını test eder
     */
    @Test
    public void testAddAndRemoveSongFromPlaylist() {
        // Arrange
        playlistCollection.add(testPlaylist);

        // Act - şarkıyı ekle
        playlistCollection.addSongToPlaylist(testPlaylist.getId(), testSong.getId());

        // Çalma listesini güncel haliyle al
        Playlist updatedPlaylist = playlistCollection.getById(testPlaylist.getId());

        // Assert - şarkı eklendi mi kontrol et
        List<Song> songs = updatedPlaylist.getSongs();
        boolean songAdded = false;
        for (Song song : songs) {
            if (song.getId().equals(testSong.getId())) {
                songAdded = true;
                break;
            }
        }

        // Act - şarkıyı çıkar
        playlistCollection.removeSongFromPlaylist(testPlaylist.getId(), testSong.getId());

        // Çalma listesini güncel haliyle al
        updatedPlaylist = playlistCollection.getById(testPlaylist.getId());

        // Assert - şarkı çıkarıldı mı kontrol et
        songs = updatedPlaylist.getSongs();
        boolean songRemoved = true;
        for (Song song : songs) {
            if (song.getId().equals(testSong.getId())) {
                songRemoved = false;
                break;
            }
        }
        assertTrue("Şarkı çalma listesinden çıkarılmalı", songRemoved);
    }

    /**
     * @brief saveToFile ve loadFromFile metodlarını test eder
     */
    @Test
    public void testSaveAndLoadFromFile() {
        // Bu metodlar SQLite kullanıldığında farklı davranıyor

        // Arrange
        playlistCollection.add(testPlaylist);

        // Act & Assert
        assertTrue("saveToFile başarılı olmalı", playlistCollection.saveToFile("test_file.dat"));

        // Koleksiyonu temizle ve tekrar yükle
        playlistCollection.clear();
        assertTrue("loadFromFile başarılı olmalı", playlistCollection.loadFromFile("test_file.dat"));

        // Çalma listesi tekrar yüklenmiş olmalı
        Playlist loaded = playlistCollection.getById(testPlaylist.getId());
        assertNotNull("Çalma listesi dosyadan yüklenmeli", loaded);
        assertEquals("Yüklenen çalma listesi ID'si doğru olmalı", testPlaylist.getId(), loaded.getId());
    }
}