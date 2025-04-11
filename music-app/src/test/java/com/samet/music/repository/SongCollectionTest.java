//package com.samet.music.repository;
//
//import static org.junit.Assert.*;
//import org.junit.*;
//
//import com.samet.music.model.Song;
//import com.samet.music.model.Artist;
//import com.samet.music.model.Album;
//import com.samet.music.util.DatabaseManager;
//
//import java.lang.reflect.Field;
//import java.util.List;
//
///**
// * @class SongCollectionTest
// * @brief SongCollection sınıfı için test sınıfı
// */
//public class SongCollectionTest {
//
//    private SongCollection songCollection;
//    private AlbumCollection albumCollection;
//    private ArtistCollection artistCollection;
//    private Artist testArtist;
//    private Album testAlbum;
//    private Song testSong;
//
//    /**
//     * @brief Tüm testlerden önce bir kez çalıştırılır
//     */
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        // Veritabanını test modunda başlat
//        DatabaseManager.setShouldResetDatabase(true);
//        DatabaseManager.initializeDatabase();
//    }
//
//    /**
//     * @brief Her testten önce çalıştırılır
//     */
//    @Before
//    public void setUp() throws Exception {
//        // Singleton örneklerini sıfırla
//        resetSingleton(SongCollection.class, "instance");
//        resetSingleton(AlbumCollection.class, "instance");
//        resetSingleton(ArtistCollection.class, "instance");
//
//        // Koleksiyon örneklerini al
//        songCollection = SongCollection.getInstance();
//        albumCollection = AlbumCollection.getInstance();
//        artistCollection = ArtistCollection.getInstance();
//
//        // Test verilerini oluştur
//        testArtist = new Artist("Test Artist", "Test Biography");
//        artistCollection.add(testArtist);
//
//        testAlbum = new Album("Test Album", testArtist, 2023);
//        testAlbum.setGenre("Rock");
//        albumCollection.add(testAlbum);
//
//        testSong = new Song("Test Song", testArtist, 240);
//        testSong.setGenre("Rock");
//        testSong.setAlbum(testAlbum);
//    }
//
//    /**
//     * @brief Her testten sonra çalıştırılır
//     */
//    @After
//    public void tearDown() throws Exception {
//        // Koleksiyonları temizle
//        if (songCollection != null) {
//            songCollection.clear();
//        }
//    }
//
//    /**
//     * @brief Reflection kullanarak singleton örneğini sıfırlar
//     */
//    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
//        Field instance = clazz.getDeclaredField(fieldName);
//        instance.setAccessible(true);
//        instance.set(null, null);
//    }
//
//    /**
//     * @brief getInstance metodunu test eder
//     */
//    @Test
//    public void testGetInstance() {
//        // Arrange & Act
//        SongCollection instance1 = SongCollection.getInstance();
//        SongCollection instance2 = SongCollection.getInstance();
//
//        // Assert
//        assertNotNull("getInstance null döndürmemeli", instance1);
//        assertSame("getInstance her zaman aynı örneği döndürmeli", instance1, instance2);
//    }
//
//    /**
//     * @brief add metodunu test eder
//     */
//    @Test
//    public void testAdd() {
//        // Act
//        songCollection.add(testSong);
//
//        // Assert
//        Song retrieved = songCollection.getById(testSong.getId());
//        assertNotNull("Eklenen şarkı getById ile alınabilmeli", retrieved);
//        assertEquals("Eklenen şarkı adı doğru olmalı", "Test Song", retrieved.getName());
//        assertEquals("Eklenen şarkı süresi doğru olmalı", 240, retrieved.getDuration());
//        assertEquals("Eklenen şarkı türü doğru olmalı", "Rock", retrieved.getGenre());
//        assertEquals("Eklenen şarkı sanatçısı doğru olmalı", testArtist.getId(), retrieved.getArtist().getId());
//        assertEquals("Eklenen şarkı albümü doğru olmalı", testAlbum.getId(), retrieved.getAlbum().getId());
//    }
//
//    /**
//     * @brief getById metodunu test eder - şarkı koleksiyonda varken
//     */
//    @Test
//    public void testGetByIdWhenInCollection() {
//        // Arrange
//        songCollection.add(testSong);
//
//        // Act
//        Song result = songCollection.getById(testSong.getId());
//
//        // Assert
//        assertNotNull("Şarkı bulunmalı", result);
//        assertEquals("Bulunan şarkı ID'si eşleşmeli", testSong.getId(), result.getId());
//        assertEquals("Bulunan şarkı adı eşleşmeli", testSong.getName(), result.getName());
//    }
//
//    /**
//     * @brief getAll metodunu test eder
//     */
//    @Test
//    public void testGetAll() {
//        // Arrange
//        Song song1 = new Song("Song 1", testArtist, 180);
//        Song song2 = new Song("Song 2", testArtist, 240);
//
//        songCollection.add(song1);
//        songCollection.add(song2);
//
//        // Act
//        List<Song> allSongs = songCollection.getAll();
//
//        // Assert
//        assertNotNull("getAll null döndürmemeli", allSongs);
//        assertTrue("Koleksiyonda en az 2 şarkı olmalı", allSongs.size() >= 2);
//
//        // ID'lere göre şarkıların varlığını kontrol et
//        boolean foundSong1 = false;
//        boolean foundSong2 = false;
//
//        for (Song song : allSongs) {
//            if (song.getId().equals(song1.getId())) foundSong1 = true;
//            if (song.getId().equals(song2.getId())) foundSong2 = true;
//        }
//
//        assertTrue("Song1 koleksiyonda olmalı", foundSong1);
//        assertTrue("Song2 koleksiyonda olmalı", foundSong2);
//    }
//
//    /**
//     * @brief remove metodunu test eder
//     */
//    @Test
//    public void testRemove() {
//        // Arrange
//        songCollection.add(testSong);
//        String songId = testSong.getId();
//
//        // Act
//        boolean result = songCollection.remove(songId);
//
//        // Assert
//        assertTrue("Silme işlemi başarılı olmalı", result);
//        assertNull("Silinen şarkı getById ile alınamamalı", songCollection.getById(songId));
//    }
//
//    /**
//     * @brief searchByName metodunu test eder
//     */
//    @Test
//    public void testSearchByName() {
//        // Arrange
//        Song song1 = new Song("Rock Song", testArtist, 180);
//        song1.setGenre("Rock");
//
//        Song song2 = new Song("Pop Song", testArtist, 200);
//        song2.setGenre("Pop");
//
//        Song song3 = new Song("Jazz Song", testArtist, 300);
//        song3.setGenre("Jazz");
//
//        songCollection.add(song1);
//        songCollection.add(song2);
//        songCollection.add(song3);
//
//        // Act
//        List<Song> results = songCollection.searchByName("Rock");
//
//        // Assert
//        assertNotNull("searchByName null döndürmemeli", results);
//        assertEquals("1 şarkı bulunmalı", 1, results.size());
//        assertEquals("Doğru şarkı bulunmalı", song1.getId(), results.get(0).getId());
//
//        // Case insensitive arama
//        List<Song> caseInsensitiveResults = songCollection.searchByName("jazz");
//        assertEquals("Büyük/küçük harf duyarsız arama 1 şarkı bulmalı", 1, caseInsensitiveResults.size());
//        assertEquals("Doğru şarkı bulunmalı", song3.getId(), caseInsensitiveResults.get(0).getId());
//    }
//
//    /**
//     * @brief searchByName metodunu null ve boş string ile test eder
//     */
//    @Test
//    public void testSearchByNameWithNullAndEmpty() {
//        // Arrange
//        songCollection.add(testSong);
//
//        // Act & Assert
//        assertTrue("Null ile arama boş liste döndürmeli", songCollection.searchByName(null).isEmpty());
//        assertTrue("Boş string ile arama boş liste döndürmeli", songCollection.searchByName("").isEmpty());
//        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", songCollection.searchByName("  ").isEmpty());
//    }
//
//    /**
//     * @brief getByArtist metodunu test eder
//     */
//    @Test
//    public void testGetByArtist() {
//        // Arrange
//        Artist artist1 = new Artist("Artist 1", "Bio 1");
//        Artist artist2 = new Artist("Artist 2", "Bio 2");
//
//        artistCollection.add(artist1);
//        artistCollection.add(artist2);
//
//        Song song1 = new Song("Song 1", artist1, 180);
//        Song song2 = new Song("Song 2", artist1, 200);
//        Song song3 = new Song("Song 3", artist2, 220);
//
//        songCollection.add(song1);
//        songCollection.add(song2);
//        songCollection.add(song3);
//
//        // Act
//        List<Song> results = songCollection.getByArtist(artist1);
//
//        // Assert
//        assertNotNull("getByArtist null döndürmemeli", results);
//        assertEquals("Artist1 için 2 şarkı bulunmalı", 2, results.size());
//
//        // ID'lere göre şarkıların varlığını kontrol et
//        boolean foundSong1 = false;
//        boolean foundSong2 = false;
//
//        for (Song song : results) {
//            if (song.getId().equals(song1.getId())) foundSong1 = true;
//            if (song.getId().equals(song2.getId())) foundSong2 = true;
//        }
//
//        assertTrue("Song1 sonuçlarda olmalı", foundSong1);
//        assertTrue("Song2 sonuçlarda olmalı", foundSong2);
//    }
//
//    /**
//     * @brief getByAlbum metodunu test eder
//     */
//    @Test
//    public void testGetByAlbum() {
//        // Arrange
//        Album album1 = new Album("Album 1", testArtist, 2021);
//        Album album2 = new Album("Album 2", testArtist, 2022);
//
//        albumCollection.add(album1);
//        albumCollection.add(album2);
//
//        Song song1 = new Song("Song 1", testArtist, 180);
//        song1.setAlbum(album1);
//
//        Song song2 = new Song("Song 2", testArtist, 200);
//        song2.setAlbum(album1);
//
//        Song song3 = new Song("Song 3", testArtist, 220);
//        song3.setAlbum(album2);
//
//        songCollection.add(song1);
//        songCollection.add(song2);
//        songCollection.add(song3);
//
//        // Act
//        List<Song> results = songCollection.getByAlbum(album1);
//
//        // Assert
//        assertNotNull("getByAlbum null döndürmemeli", results);
//
//        // ID'lere göre şarkıların varlığını kontrol et
//        boolean foundSong1 = false;
//        boolean foundSong2 = false;
//
//        for (Song song : results) {
//            if (song.getId().equals(song1.getId())) foundSong1 = true;
//            if (song.getId().equals(song2.getId())) foundSong2 = true;
//        }
//
//    }
//
//    /**
//     * @brief getByGenre metodunu test eder
//     */
//    @Test
//    public void testGetByGenre() {
//        // Arrange
//        Song song1 = new Song("Song 1", testArtist, 180);
//        song1.setGenre("Rock");
//
//        Song song2 = new Song("Song 2", testArtist, 200);
//        song2.setGenre("Pop");
//
//        Song song3 = new Song("Song 3", testArtist, 220);
//        song3.setGenre("Rock/Metal");
//
//        songCollection.add(song1);
//        songCollection.add(song2);
//        songCollection.add(song3);
//
//        // Act
//        List<Song> results = songCollection.getByGenre("Rock");
//
//        // Assert
//        assertNotNull("getByGenre null döndürmemeli", results);
//
//        // ID'lere göre şarkıların varlığını kontrol et
//        boolean foundSong1 = false;
//        boolean foundSong3 = false;
//
//        for (Song song : results) {
//            if (song.getId().equals(song1.getId())) foundSong1 = true;
//            if (song.getId().equals(song3.getId())) foundSong3 = true;
//        }
//
//        assertTrue("Song1 (Rock) sonuçlarda olmalı", foundSong1);
//        assertTrue("Song3 (Rock/Metal) sonuçlarda olmalı", foundSong3);
//    }
//
//    /**
//     * @brief getByGenre metodunu null ve boş string ile test eder
//     */
//    @Test
//    public void testGetByGenreWithNullAndEmpty() {
//        // Arrange
//        songCollection.add(testSong);
//
//        // Act & Assert
//        assertTrue("Null ile arama boş liste döndürmeli", songCollection.getByGenre(null).isEmpty());
//        assertTrue("Boş string ile arama boş liste döndürmeli", songCollection.getByGenre("").isEmpty());
//        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", songCollection.getByGenre("  ").isEmpty());
//    }
//
//    /**
//     * @brief saveToFile ve loadFromFile metodlarını test eder
//     */
//    @Test
//    public void testSaveAndLoadFromFile() {
//        // Bu metodlar SQLite kullanıldığında farklı davranıyor
//
//        // Arrange
//        songCollection.add(testSong);
//
//        // Act & Assert
//        assertTrue("saveToFile başarılı olmalı", songCollection.saveToFile("test_file.dat"));
//
//        // Koleksiyonu temizle ve tekrar yükle
//        songCollection.clear();
//        assertTrue("loadFromFile başarılı olmalı", songCollection.loadFromFile("test_file.dat"));
//    }
//}