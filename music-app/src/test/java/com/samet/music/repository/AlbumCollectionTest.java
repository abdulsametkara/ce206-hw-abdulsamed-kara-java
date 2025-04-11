//package com.samet.music.repository;
//
//import static org.junit.Assert.*;
//import org.junit.*;
//
//import com.samet.music.model.Album;
//import com.samet.music.model.Artist;
//import com.samet.music.util.DatabaseManager;
//
//import java.lang.reflect.Field;
//import java.util.List;
//
///**
// * @class AlbumCollectionTest
// * @brief AlbumCollection sınıfı için test sınıfı
// */
//public class AlbumCollectionTest {
//
//    private AlbumCollection albumCollection;
//    private ArtistCollection artistCollection;
//    private Artist testArtist;
//    private Album testAlbum;
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
//        resetSingleton(AlbumCollection.class, "instance");
//        resetSingleton(ArtistCollection.class, "instance");
//
//        // Koleksiyon örneklerini al
//        albumCollection = AlbumCollection.getInstance();
//        artistCollection = ArtistCollection.getInstance();
//
//        // Test verilerini oluştur
//        testArtist = new Artist("Test Artist", "Test Biography");
//        artistCollection.add(testArtist); // Sanatçıyı veritabanına ekle
//
//        testAlbum = new Album("Test Album", testArtist, 2023);
//        testAlbum.setGenre("Rock");
//    }
//
//    /**
//     * @brief Her testten sonra çalıştırılır
//     */
//    @After
//    public void tearDown() throws Exception {
//        // Koleksiyonları temizle
//        if (albumCollection != null) {
//            albumCollection.clear();
//        }
//
//        if (artistCollection != null) {
//            artistCollection.clear();
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
//        AlbumCollection instance1 = AlbumCollection.getInstance();
//        AlbumCollection instance2 = AlbumCollection.getInstance();
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
//        // Arrange
//        Album album = new Album("Add Test Album", testArtist, 2022);
//
//        // Act
//        albumCollection.add(album);
//
//        // Assert
//        Album retrieved = albumCollection.getById(album.getId());
//        assertNotNull("Eklenen albüm getById ile alınabilmeli", retrieved);
//        assertEquals("Eklenen albüm adı doğru olmalı", "Add Test Album", retrieved.getName());
//        assertEquals("Eklenen albüm sanatçısı doğru olmalı", testArtist.getId(), retrieved.getArtist().getId());
//    }
//
//    /**
//     * @brief getById metodunu test eder - albüm koleksiyonda varken
//     */
//    @Test
//    public void testGetByIdWhenInCollection() {
//        // Arrange
//        albumCollection.add(testAlbum);
//
//        // Act
//        Album result = albumCollection.getById(testAlbum.getId());
//
//        // Assert
//        assertNotNull("Albüm bulunmalı", result);
//        assertEquals("Bulunan albüm ID'si eşleşmeli", testAlbum.getId(), result.getId());
//        assertEquals("Bulunan albüm adı eşleşmeli", testAlbum.getName(), result.getName());
//    }
//
//    /**
//     * @brief getAll metodunu test eder
//     */
//    @Test
//    public void testGetAll() {
//        // Arrange
//        Album album1 = new Album("Album 1", testArtist, 2021);
//        Album album2 = new Album("Album 2", testArtist, 2022);
//
//        albumCollection.add(album1);
//        albumCollection.add(album2);
//
//        // Act
//        List<Album> allAlbums = albumCollection.getAll();
//
//        // Assert
//        assertNotNull("getAll null döndürmemeli", allAlbums);
//
//        // ID'lere göre albümlerin varlığını kontrol et
//        boolean foundAlbum1 = false;
//        boolean foundAlbum2 = false;
//
//        for (Album album : allAlbums) {
//            if (album.getId().equals(album1.getId())) foundAlbum1 = true;
//            if (album.getId().equals(album2.getId())) foundAlbum2 = true;
//        }
//
//        assertTrue("Album1 koleksiyonda olmalı", foundAlbum1);
//        assertTrue("Album2 koleksiyonda olmalı", foundAlbum2);
//    }
//
//    /**
//     * @brief remove metodunu test eder
//     */
//    @Test
//    public void testRemove() {
//        // Arrange
//        albumCollection.add(testAlbum);
//        String albumId = testAlbum.getId();
//
//        // Act
//        boolean result = albumCollection.remove(albumId);
//
//        // Assert
//        assertTrue("Silme işlemi başarılı olmalı", result);
//        assertNull("Silinen albüm getById ile alınamamalı", albumCollection.getById(albumId));
//    }
//
//    /**
//     * @brief searchByName metodunu test eder
//     */
//    @Test
//    public void testSearchByName() {
//        // Arrange
//        Album album1 = new Album("Rock Album", testArtist, 2021);
//        Album album2 = new Album("Pop Album", testArtist, 2022);
//        Album album3 = new Album("Jazz Album", testArtist, 2023);
//
//        albumCollection.add(album1);
//        albumCollection.add(album2);
//        albumCollection.add(album3);
//
//        // Act
//        List<Album> results = albumCollection.searchByName("Rock");
//
//        // Assert
//        assertNotNull("searchByName null döndürmemeli", results);
//        assertEquals("1 albüm bulunmalı", 1, results.size());
//        assertEquals("Doğru albüm bulunmalı", album1.getId(), results.get(0).getId());
//
//        // Case insensitive arama
//        List<Album> caseInsensitiveResults = albumCollection.searchByName("jazz");
//        assertEquals("Büyük/küçük harf duyarsız arama 1 albüm bulmalı", 1, caseInsensitiveResults.size());
//        assertEquals("Doğru albüm bulunmalı", album3.getId(), caseInsensitiveResults.get(0).getId());
//    }
//
//    /**
//     * @brief searchByName metodunu null ve boş string ile test eder
//     */
//    @Test
//    public void testSearchByNameWithNullAndEmpty() {
//        // Arrange
//        albumCollection.add(testAlbum);
//
//        // Act & Assert
//        assertTrue("Null ile arama boş liste döndürmeli", albumCollection.searchByName(null).isEmpty());
//        assertTrue("Boş string ile arama boş liste döndürmeli", albumCollection.searchByName("").isEmpty());
//        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", albumCollection.searchByName("  ").isEmpty());
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
//        Album album1 = new Album("Album 1", artist1, 2021);
//        Album album2 = new Album("Album 2", artist1, 2022);
//        Album album3 = new Album("Album 3", artist2, 2023);
//
//        albumCollection.add(album1);
//        albumCollection.add(album2);
//        albumCollection.add(album3);
//
//        // Act
//        List<Album> results = albumCollection.getByArtist(artist1);
//
//        // Assert
//        assertNotNull("getByArtist null döndürmemeli", results);
//        assertEquals("Artist1 için 2 albüm bulunmalı", 2, results.size());
//
//        // ID'lere göre albümlerin varlığını kontrol et
//        boolean foundAlbum1 = false;
//        boolean foundAlbum2 = false;
//
//        for (Album album : results) {
//            if (album.getId().equals(album1.getId())) foundAlbum1 = true;
//            if (album.getId().equals(album2.getId())) foundAlbum2 = true;
//        }
//
//        assertTrue("Album1 sonuçlarda olmalı", foundAlbum1);
//        assertTrue("Album2 sonuçlarda olmalı", foundAlbum2);
//    }
//
//    /**
//     * @brief getByGenre metodunu test eder
//     */
//    @Test
//    public void testGetByGenre() {
//        // Arrange
//        Album album1 = new Album("Album 1", testArtist, 2021);
//        album1.setGenre("Rock");
//
//        Album album2 = new Album("Album 2", testArtist, 2022);
//        album2.setGenre("Pop");
//
//        Album album3 = new Album("Album 3", testArtist, 2023);
//        album3.setGenre("Rock/Metal");
//
//        albumCollection.add(album1);
//        albumCollection.add(album2);
//        albumCollection.add(album3);
//
//        // Act
//        List<Album> results = albumCollection.getByGenre("Rock");
//
//        // Assert
//        assertNotNull("getByGenre null döndürmemeli", results);
//
//
//        // ID'lere göre albümlerin varlığını kontrol et
//        boolean foundAlbum1 = false;
//        boolean foundAlbum3 = false;
//
//        for (Album album : results) {
//            if (album.getId().equals(album1.getId())) foundAlbum1 = true;
//            if (album.getId().equals(album3.getId())) foundAlbum3 = true;
//        }
//
//        assertTrue("Album1 (Rock) sonuçlarda olmalı", foundAlbum1);
//        assertTrue("Album3 (Rock/Metal) sonuçlarda olmalı", foundAlbum3);
//    }
//
//    /**
//     * @brief getByGenre metodunu null ve boş string ile test eder
//     */
//    @Test
//    public void testGetByGenreWithNullAndEmpty() {
//        // Arrange
//        albumCollection.add(testAlbum);
//
//        // Act & Assert
//        assertTrue("Null ile arama boş liste döndürmeli", albumCollection.getByGenre(null).isEmpty());
//        assertTrue("Boş string ile arama boş liste döndürmeli", albumCollection.getByGenre("").isEmpty());
//        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", albumCollection.getByGenre("  ").isEmpty());
//    }
//
//    /**
//     * @brief saveToFile ve loadFromFile metodlarını test eder
//     */
//    @Test
//    public void testSaveAndLoadFromFile() {
//        // Bu metodlar SQLite kullanıldığında sadece true dönüyor
//
//        // Act & Assert
//        assertTrue("saveToFile her zaman true dönmeli", albumCollection.saveToFile("test_file.dat"));
//        assertTrue("loadFromFile her zaman true dönmeli", albumCollection.loadFromFile("test_file.dat"));
//    }
//}