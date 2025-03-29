package com.samet.music.repository;

import static org.junit.Assert.*;
import org.junit.*;

import com.samet.music.model.BaseEntity;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @class MusicCollectionManagerTest
 * @brief MusicCollectionManager sınıfı için test sınıfı
 */
public class MusicCollectionManagerTest {

    // Test için basit bir entity sınıfı
    private static class TestEntity extends BaseEntity {
        public TestEntity(String name) {
            super(name);
        }
    }

    // MusicCollectionManager sınıfını extend eden test sınıfı
    private static class TestCollection extends MusicCollectionManager<TestEntity> {
        private boolean loadFromDatabaseCalled = false;

        @Override
        protected void loadFromDatabase() {
            loadFromDatabaseCalled = true;
            // Test için sadece bu metodun çağrıldığını işaretliyoruz
        }

        @Override
        protected String getItemId(TestEntity item) {
            return item.getId();
        }

        // Test için yardımcı metod
        public boolean isLoadFromDatabaseCalled() {
            return loadFromDatabaseCalled;
        }

        // Test için yardımcı metod
        public void resetLoadFromDatabaseCalled() {
            loadFromDatabaseCalled = false;
        }
    }

    private TestCollection collection;
    private TestEntity testEntity;
    private final String TEST_FILE_PATH = "test_collection.dat";

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        collection = new TestCollection();
        testEntity = new TestEntity("Test Entity");

        // Test dosyasını temizle
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        collection.clear();

        // Test dosyasını temizle
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    /**
     * @brief add metodunu test eder
     */
    @Test
    public void testAdd() {
        // Act
        collection.add(testEntity);

        // Assert
        assertEquals("Koleksiyonun boyutu 1 olmalı", 1, collection.size());
        assertTrue("Koleksiyon test entity ID'sini içermeli", collection.contains(testEntity.getId()));
        assertSame("getById doğru entity'yi döndürmeli", testEntity, collection.getById(testEntity.getId()));
    }

    /**
     * @brief add metodunu null ile test eder
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithNull() {
        // Act - exception bekleniyor
        collection.add(null);
    }

    /**
     * @brief remove metodunu test eder
     */
    @Test
    public void testRemove() {
        // Arrange
        collection.add(testEntity);

        // Act
        boolean result = collection.remove(testEntity.getId());

        // Assert
        assertTrue("Silme işlemi başarılı olmalı", result);
        assertEquals("Koleksiyonun boyutu 0 olmalı", 0, collection.size());
        assertFalse("Koleksiyon artık test entity ID'sini içermemeli", collection.contains(testEntity.getId()));
    }

    /**
     * @brief remove metodunu olmayan ID ile test eder
     */
    @Test
    public void testRemoveWithNonExistingId() {
        // Act
        boolean result = collection.remove("non-existing-id");

        // Assert
        assertFalse("Var olmayan ID'yi silme işlemi false döndürmeli", result);
    }

    /**
     * @brief getById metodunu test eder
     */
    @Test
    public void testGetById() {
        // Arrange
        collection.add(testEntity);

        // Act
        TestEntity result = collection.getById(testEntity.getId());

        // Assert
        assertSame("Bulunan entity orijinal entity ile aynı olmalı", testEntity, result);
    }

    /**
     * @brief getById metodunu olmayan ID ile test eder
     */
    @Test
    public void testGetByIdWithNonExistingId() {
        // Act
        TestEntity result = collection.getById("non-existing-id");

        // Assert
        assertNull("Var olmayan ID için null döndürmeli", result);
    }

    /**
     * @brief getAll metodunu test eder
     */
    @Test
    public void testGetAll() {
        // Arrange
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 2");

        collection.add(entity1);
        collection.add(entity2);

        // Reset the flag to test if loadFromDatabase is called
        collection.resetLoadFromDatabaseCalled();
        collection.isLoaded = false;

        // Act
        List<TestEntity> result = collection.getAll();

        // Assert
        assertTrue("loadFromDatabase metodu çağrılmalı", collection.isLoadFromDatabaseCalled());
        assertEquals("Liste 2 element içermeli", 2, result.size());
        assertTrue("Liste entity1'i içermeli", result.contains(entity1));
        assertTrue("Liste entity2'yi içermeli", result.contains(entity2));
    }

    /**
     * @brief getAll metodunu önceden yüklenmiş durumda test eder
     */
    @Test
    public void testGetAllWhenAlreadyLoaded() {
        // Arrange
        TestEntity entity = new TestEntity("Test Entity");
        collection.add(entity);

        // İlk çağrı loadFromDatabase'i çağırır
        collection.getAll();

        // Reset the flag
        collection.resetLoadFromDatabaseCalled();

        // Act - ikinci çağrı
        List<TestEntity> result = collection.getAll();

        // Assert
        assertFalse("loadFromDatabase metodu tekrar çağrılmamalı", collection.isLoadFromDatabaseCalled());
        assertEquals("Liste hala element içermeli", 1, result.size());
        assertTrue("Liste entity'yi içermeli", result.contains(entity));
    }

    /**
     * @brief clear metodunu test eder
     */
    @Test
    public void testClear() {
        // Arrange
        collection.add(testEntity);

        // Act
        collection.clear();

        // Assert
        assertEquals("Koleksiyon boş olmalı", 0, collection.size());
        assertFalse("Koleksiyon temizlendikten sonra element içermemeli", collection.contains(testEntity.getId()));
        assertFalse("isLoaded false olmalı", collection.isLoaded);
    }

    /**
     * @brief size metodunu test eder
     */
    @Test
    public void testSize() {
        // Arrange - başlangıçta boş
        assertEquals("Başlangıçta koleksiyon boş olmalı", 0, collection.size());

        // Act - entity ekle
        collection.add(testEntity);

        // Assert
        assertEquals("1 element ekledikten sonra size 1 olmalı", 1, collection.size());

        // Act - ikinci entity ekle
        TestEntity entity2 = new TestEntity("Entity 2");
        collection.add(entity2);

        // Assert
        assertEquals("2 element ekledikten sonra size 2 olmalı", 2, collection.size());

        // Act - bir entity sil
        collection.remove(testEntity.getId());

        // Assert
        assertEquals("1 element sildikten sonra size 1 olmalı", 1, collection.size());
    }

    /**
     * @brief contains metodunu test eder
     */
    @Test
    public void testContains() {
        // Arrange
        collection.add(testEntity);

        // Act & Assert
        assertTrue("Var olan ID için contains true döndürmeli", collection.contains(testEntity.getId()));
        assertFalse("Var olmayan ID için contains false döndürmeli", collection.contains("non-existing-id"));
    }

    /**
     * @brief saveToFile ve loadFromFile metodlarını test eder
     * Not: Bu test gerçek dosya I/O işlemleri yapar
     */
    @Test
    public void testSaveAndLoadFromFile() {
        // Arrange
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 2");

        collection.add(entity1);
        collection.add(entity2);

        // Dosya henüz oluşturulmadı
        File testFile = new File(TEST_FILE_PATH);
        assertFalse("Test öncesinde dosya olmamalı", testFile.exists());

        // Act - dosyaya kaydet
        boolean saveResult = collection.saveToFile(TEST_FILE_PATH);

        // Assert - kaydetme sonucu
        assertTrue("saveToFile başarılı olmalı", saveResult);
        assertTrue("Dosya oluşturulmalı", testFile.exists());

        // Koleksiyonu temizle
        collection.clear();
        assertEquals("Temizleme sonrası koleksiyon boş olmalı", 0, collection.size());

        // Act - dosyadan yükle
        boolean loadResult = collection.loadFromFile(TEST_FILE_PATH);

        // Assert - yükleme sonucu
        assertTrue("loadFromFile başarılı olmalı", loadResult);
        assertEquals("Yükleme sonrası koleksiyon 2 element içermeli", 2, collection.size());
        assertNotNull("Entity1 ID'si ile element alınabilmeli", collection.getById(entity1.getId()));
        assertNotNull("Entity2 ID'si ile element alınabilmeli", collection.getById(entity2.getId()));
    }

    /**
     * @brief loadFromFile metodunu var olmayan dosya ile test eder
     */
    @Test
    public void testLoadFromFileWithNonExistingFile() {
        // Act
        boolean result = collection.loadFromFile("non-existing-file.dat");

        // Assert
        assertFalse("Var olmayan dosya için false döndürmeli", result);
    }
}