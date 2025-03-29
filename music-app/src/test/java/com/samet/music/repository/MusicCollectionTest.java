package com.samet.music.repository;

import static org.junit.Assert.*;
import org.junit.*;

import com.samet.music.model.BaseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @class MusicCollectionTest
 * @brief IMusicCollection arayüzü için test sınıfı
 *
 * Not: IMusicCollection bir arayüz olduğu için doğrudan test edilemez.
 * Bu nedenle, test için basit bir implementasyon kullanıyoruz.
 */
public class MusicCollectionTest {

    // Test için basit bir entity sınıfı
    private class TestEntity extends BaseEntity {
        public TestEntity(String name) {
            super(name);
        }
    }

    // IMusicCollection arayüzünü uygulayan test sınıfı
    private class TestCollection implements IMusicCollection<TestEntity> {
        private Map<String, TestEntity> items = new HashMap<>();
        private boolean isLoaded = false;

        @Override
        public void add(TestEntity item) {
            items.put(item.getId(), item);
        }

        @Override
        public boolean remove(String id) {
            return items.remove(id) != null;
        }

        @Override
        public TestEntity getById(String id) {
            return items.get(id);
        }

        @Override
        public List<TestEntity> getAll() {
            if (!isLoaded) {
                loadFromFile("dummy"); // Test için dosya adı önemli değil
                isLoaded = true;
            }
            return new ArrayList<>(items.values());
        }

        @Override
        public void clear() {
            items.clear();
            isLoaded = false;
        }

        @Override
        public int size() {
            return items.size();
        }

        @Override
        public boolean contains(String id) {
            return items.containsKey(id);
        }

        @Override
        public boolean saveToFile(String filePath) {
            // Test için her zaman başarılı döner
            return true;
        }

        @Override
        public boolean loadFromFile(String filePath) {
            // Test için her zaman başarılı döner
            return true;
        }
    }

    private TestCollection collection;
    private TestEntity testEntity;

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        collection = new TestCollection();
        testEntity = new TestEntity("Test Entity");
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        collection.clear();
    }

    /**
     * @brief add metodunu test eder
     */
    @Test
    public void testAdd() {
        // Arrange
        TestEntity entity = new TestEntity("Add Test Entity");

        // Act
        collection.add(entity);

        // Assert
        TestEntity retrieved = collection.getById(entity.getId());
        assertNotNull("Eklenen öğe getById ile alınabilmeli", retrieved);
        assertEquals("Eklenen öğe adı doğru olmalı", "Add Test Entity", retrieved.getName());
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
        assertNotNull("Öğe bulunmalı", result);
        assertEquals("Bulunan öğe ID'si eşleşmeli", testEntity.getId(), result.getId());
        assertEquals("Bulunan öğe adı eşleşmeli", testEntity.getName(), result.getName());
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

        // Act
        List<TestEntity> allEntities = collection.getAll();

        // Assert
        assertNotNull("getAll null döndürmemeli", allEntities);
        assertEquals("Koleksiyonda 2 öğe olmalı", 2, allEntities.size());

        // ID'lere göre öğelerin varlığını kontrol et
        boolean foundEntity1 = false;
        boolean foundEntity2 = false;

        for (TestEntity entity : allEntities) {
            if (entity.getId().equals(entity1.getId())) foundEntity1 = true;
            if (entity.getId().equals(entity2.getId())) foundEntity2 = true;
        }

        assertTrue("Entity1 koleksiyonda olmalı", foundEntity1);
        assertTrue("Entity2 koleksiyonda olmalı", foundEntity2);
    }

    /**
     * @brief remove metodunu test eder
     */
    @Test
    public void testRemove() {
        // Arrange
        collection.add(testEntity);
        String entityId = testEntity.getId();

        // Act
        boolean result = collection.remove(entityId);

        // Assert
        assertTrue("Silme işlemi başarılı olmalı", result);
        assertNull("Silinen öğe getById ile alınamamalı", collection.getById(entityId));
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
        assertNull("Önceki öğeler getById ile alınamamalı", collection.getById(testEntity.getId()));
    }

    /**
     * @brief size metodunu test eder
     */
    @Test
    public void testSize() {
        // Arrange
        assertEquals("Başlangıçta koleksiyon boş olmalı", 0, collection.size());

        // Act
        collection.add(testEntity);

        // Assert
        assertEquals("Bir öğe eklendiğinde size 1 olmalı", 1, collection.size());

        // Act - bir öğe daha ekle
        TestEntity anotherEntity = new TestEntity("Another Entity");
        collection.add(anotherEntity);

        // Assert
        assertEquals("İki öğe eklendiğinde size 2 olmalı", 2, collection.size());
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
     */
    @Test
    public void testSaveAndLoadFromFile() {
        // Arrange
        collection.add(testEntity);

        // Act & Assert
        assertTrue("saveToFile başarılı olmalı", collection.saveToFile("test_file.dat"));

        // Koleksiyonu temizle ve tekrar yükle
        collection.clear();
        assertTrue("loadFromFile başarılı olmalı", collection.loadFromFile("test_file.dat"));
    }
}