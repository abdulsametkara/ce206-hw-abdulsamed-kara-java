package com.samet.music.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IMusicCollection arayüzünün gerçek implementasyonunu test eden sınıf.
 * MusicCollectionBase sınıfını genişleterek IMusicCollection arayüzünü uygulamaktadır.
 */
class IMusicCollectionImplementationTest {

    private TestMusicCollection collection;
    private TestEntity testEntity1;
    private TestEntity testEntity2;

    @TempDir
    Path tempDir;

    /**
     * Her testten önce çalıştırılır ve test verilerini hazırlar.
     */
    @BeforeEach
    void setUp() {
        // Test koleksiyonunu oluştur
        collection = new TestMusicCollection();
        
        // Test verilerini oluştur
        testEntity1 = new TestEntity("1", "Test Entity 1");
        testEntity2 = new TestEntity("2", "Test Entity 2");
    }

    @Test
    void add_ShouldAddItemToCollection() {
        // Execute
        collection.add(testEntity1);
        
        // Verify
        assertTrue(collection.contains("1"));
        assertEquals(1, collection.size());
        assertEquals(testEntity1, collection.getById("1"));
    }

    @Test
    void addMultiple_ShouldAddItemsToCollection() {
        // Execute
        collection.add(testEntity1);
        collection.add(testEntity2);
        
        // Verify
        assertTrue(collection.contains("1"));
        assertTrue(collection.contains("2"));
        assertEquals(2, collection.size());
    }

    @Test
    void remove_ShouldRemoveItemFromCollection() {
        // Setup
        collection.add(testEntity1);
        collection.add(testEntity2);
        
        // Execute
        boolean result = collection.remove("1");
        
        // Verify
        assertTrue(result);
        assertFalse(collection.contains("1"));
        assertEquals(1, collection.size());
        assertNull(collection.getById("1"));
    }

    @Test
    void remove_ShouldReturnFalseWhenItemNotFound() {
        // Execute
        boolean result = collection.remove("nonexistent");
        
        // Verify
        assertFalse(result);
    }

    @Test
    void getById_ShouldReturnItemWhenExists() {
        // Setup
        collection.add(testEntity1);
        
        // Execute
        TestEntity result = collection.getById("1");
        
        // Verify
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Entity 1", result.getName());
    }

    @Test
    void getById_ShouldReturnNullWhenDoesNotExist() {
        // Execute
        TestEntity result = collection.getById("nonexistent");
        
        // Verify
        assertNull(result);
    }

    @Test
    void getAll_ShouldReturnAllItems() {
        // Setup
        collection.add(testEntity1);
        collection.add(testEntity2);
        
        // Execute
        List<TestEntity> result = collection.getAll();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testEntity1));
        assertTrue(result.contains(testEntity2));
    }

    @Test
    void getAll_ShouldReturnEmptyListWhenCollectionIsEmpty() {
        // Execute
        List<TestEntity> result = collection.getAll();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void clear_ShouldRemoveAllItems() {
        // Setup
        collection.add(testEntity1);
        collection.add(testEntity2);
        
        // Execute
        collection.clear();
        
        // Verify
        assertEquals(0, collection.size());
        assertFalse(collection.contains("1"));
        assertFalse(collection.contains("2"));
        assertTrue(collection.getAll().isEmpty());
    }

    @Test
    void contains_ShouldReturnTrueWhenItemExists() {
        // Setup
        collection.add(testEntity1);
        
        // Execute & Verify
        assertTrue(collection.contains("1"));
    }

    @Test
    void contains_ShouldReturnFalseWhenItemDoesNotExist() {
        // Execute & Verify
        assertFalse(collection.contains("nonexistent"));
    }

    @Test
    void saveAndLoadFromFile_ShouldPersistCollection() {
        // Setup
        collection.add(testEntity1);
        collection.add(testEntity2);
        
        String filePath = tempDir.resolve("test.dat").toString();
        
        // Execute save
        boolean saveResult = collection.saveToFile(filePath);
        
        // Verify save
        assertTrue(saveResult);
        
        // Create a new collection and load from file
        TestMusicCollection newCollection = new TestMusicCollection();
        boolean loadResult = newCollection.loadFromFile(filePath);
        
        // Verify load
        assertTrue(loadResult);
        assertEquals(2, newCollection.size());
        assertTrue(newCollection.contains("1"));
        assertTrue(newCollection.contains("2"));
    }

    @Test
    void loadFromFile_ShouldReturnFalseWhenFileDoesNotExist() {
        // Execute
        boolean result = collection.loadFromFile("nonexistent_file.dat");
        
        // Verify
        assertFalse(result);
    }
    
    /**
     * IMusicCollection arayüzünü uygulayan test koleksiyonu
     */
    private static class TestMusicCollection extends MusicCollectionBase<TestEntity> {
        @Override
        protected String getItemId(TestEntity item) {
            return item.getId();
        }

        @Override
        protected void loadFromDatabase() {
            // Test için boş implementasyon
        }
        
        @Override
        public boolean saveToFile(String filePath) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(items);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        
        @Override
        public boolean loadFromFile(String filePath) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                @SuppressWarnings("unchecked")
                Map<String, TestEntity> loadedItems = (Map<String, TestEntity>) ois.readObject();
                items.clear();
                items.putAll(loadedItems);
                return true;
            } catch (IOException | ClassNotFoundException e) {
                return false;
            }
        }
    }
    
    /**
     * Test entity sınıfı
     */
    private static class TestEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private final String name;
        
        public TestEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
} 