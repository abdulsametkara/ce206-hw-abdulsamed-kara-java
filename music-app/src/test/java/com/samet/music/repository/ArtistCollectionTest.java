//package com.samet.music.repository;
//
//import static org.junit.Assert.*;
//import org.junit.*;
//
//import com.samet.music.model.Artist;
//import com.samet.music.util.DatabaseManager;
//
//import java.lang.reflect.Field;
//import java.util.List;
//
///**
// * @class ArtistCollectionTest
// * @brief ArtistCollection sınıfı için test sınıfı
// */
//public class ArtistCollectionTest {
//
//    private ArtistCollection artistCollection;
//    private Artist testArtist;
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
//        // Singleton örneğini sıfırla
//        resetSingleton(ArtistCollection.class, "instance");
//
//        // ArtistCollection örneğini al
//        artistCollection = ArtistCollection.getInstance();
//
//        // Test verilerini oluştur
//        testArtist = new Artist("Test Artist", "Test Biography");
//    }
//
//    /**
//     * @brief Her testten sonra çalıştırılır
//     */
//    @After
//    public void tearDown() throws Exception {
//        // Koleksiyonu temizle
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
//        ArtistCollection instance1 = ArtistCollection.getInstance();
//        ArtistCollection instance2 = ArtistCollection.getInstance();
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
//        Artist artist = new Artist("Add Test Artist", "Test Biography");
//
//        // Act
//        artistCollection.add(artist);
//
//        // Assert
//        Artist retrieved = artistCollection.getById(artist.getId());
//        assertNotNull("Eklenen sanatçı getById ile alınabilmeli", retrieved);
//        assertEquals("Eklenen sanatçı adı doğru olmalı", "Add Test Artist", retrieved.getName());
//        assertEquals("Eklenen sanatçı biyografisi doğru olmalı", "Test Biography", retrieved.getBiography());
//    }
//
//    /**
//     * @brief getById metodunu test eder - sanatçı koleksiyonda varken
//     */
//    @Test
//    public void testGetByIdWhenInCollection() {
//        // Arrange
//        artistCollection.add(testArtist);
//
//        // Act
//        Artist result = artistCollection.getById(testArtist.getId());
//
//        // Assert
//        assertNotNull("Sanatçı bulunmalı", result);
//        assertEquals("Bulunan sanatçı ID'si eşleşmeli", testArtist.getId(), result.getId());
//        assertEquals("Bulunan sanatçı adı eşleşmeli", testArtist.getName(), result.getName());
//    }
//
//    /**
//     * @brief getAll metodunu test eder
//     */
//    @Test
//    public void testGetAll() {
//        // Arrange
//        Artist artist1 = new Artist("Artist 1", "Biography 1");
//        Artist artist2 = new Artist("Artist 2", "Biography 2");
//
//        artistCollection.add(artist1);
//        artistCollection.add(artist2);
//
//        // Act
//        List<Artist> allArtists = artistCollection.getAll();
//
//        // Assert
//        assertNotNull("getAll null döndürmemeli", allArtists);
//        assertTrue("Koleksiyonda en az 2 sanatçı olmalı", allArtists.size() >= 2);
//
//        // ID'lere göre sanatçıların varlığını kontrol et
//        boolean foundArtist1 = false;
//        boolean foundArtist2 = false;
//
//        for (Artist artist : allArtists) {
//            if (artist.getId().equals(artist1.getId())) foundArtist1 = true;
//            if (artist.getId().equals(artist2.getId())) foundArtist2 = true;
//        }
//
//
//    }
//
//    /**
//     * @brief remove metodunu test eder
//     */
//    @Test
//    public void testRemove() {
//        // Arrange
//        artistCollection.add(testArtist);
//        String artistId = testArtist.getId();
//
//        // Act
//        boolean result = artistCollection.remove(artistId);
//
//        // Assert
//        assertTrue("Silme işlemi başarılı olmalı", result);
//        assertNull("Silinen sanatçı getById ile alınamamalı", artistCollection.getById(artistId));
//    }
//
//    /**
//     * @brief searchByName metodunu test eder
//     */
//    @Test
//    public void testSearchByName() {
//        // Arrange
//        Artist artist1 = new Artist("Rock Star", "Rock Biography");
//        Artist artist2 = new Artist("Pop Star", "Pop Biography");
//        Artist artist3 = new Artist("Jazz Musician", "Jazz Biography");
//
//        artistCollection.add(artist1);
//        artistCollection.add(artist2);
//        artistCollection.add(artist3);
//
//        // Act
//        List<Artist> results = artistCollection.searchByName("Rock");
//
//        // Assert
//        assertNotNull("searchByName null döndürmemeli", results);
//        assertEquals("1 sanatçı bulunmalı", 1, results.size());
//        assertEquals("Doğru sanatçı bulunmalı", artist1.getId(), results.get(0).getId());
//
//        // Case insensitive arama
//        List<Artist> caseInsensitiveResults = artistCollection.searchByName("jazz");
//        assertEquals("Büyük/küçük harf duyarsız arama 1 sanatçı bulmalı", 1, caseInsensitiveResults.size());
//        assertEquals("Doğru sanatçı bulunmalı", artist3.getId(), caseInsensitiveResults.get(0).getId());
//    }
//
//    /**
//     * @brief searchByName metodunu null ve boş string ile test eder
//     */
//    @Test
//    public void testSearchByNameWithNullAndEmpty() {
//        // Arrange
//        artistCollection.add(testArtist);
//
//        // Act & Assert
//        assertTrue("Null ile arama boş liste döndürmeli", artistCollection.searchByName(null).isEmpty());
//        assertTrue("Boş string ile arama boş liste döndürmeli", artistCollection.searchByName("").isEmpty());
//        assertTrue("Sadece boşluk içeren string ile arama boş liste döndürmeli", artistCollection.searchByName("  ").isEmpty());
//    }
//
//    /**
//     * @brief loadFromDatabase metodunu test eder
//     */
//    @Test
//    public void testLoadFromDatabase() throws Exception {
//        // Arrange
//        artistCollection.add(testArtist);
//        String artistId = testArtist.getId();
//
//        // Mevcut koleksiyonu temizle
//        artistCollection.clear();
//
//        // Act - loadFromDatabase metodunu çağır (private olduğu için reflection kullanıyoruz)
//        java.lang.reflect.Method method = ArtistCollection.class.getDeclaredMethod("loadFromDatabase");
//        method.setAccessible(true);
//        method.invoke(artistCollection);
//
//
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
//        artistCollection.add(testArtist);
//
//        // Act & Assert
//        assertTrue("saveToFile başarılı olmalı", artistCollection.saveToFile("test_file.dat"));
//
//        // Koleksiyonu temizle ve tekrar yükle
//        artistCollection.clear();
//        assertTrue("loadFromFile başarılı olmalı", artistCollection.loadFromFile("test_file.dat"));
//
//        // Sanatçı tekrar yüklenmiş olmalı
//        Artist loaded = artistCollection.getById(testArtist.getId());
//        assertNotNull("Sanatçı dosyadan yüklenmeli", loaded);
//        assertEquals("Yüklenen sanatçı ID'si doğru olmalı", testArtist.getId(), loaded.getId());
//    }
//}