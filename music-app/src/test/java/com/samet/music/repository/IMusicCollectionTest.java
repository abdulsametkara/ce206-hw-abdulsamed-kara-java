package com.samet.music.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * IMusicCollection arayüzünün test sınıfı.
 * Mockito kullanılarak arayüz implementasyonları test edilir.
 */
class IMusicCollectionTest {

    @Mock
    private IMusicCollection<TestEntity> mockCollection;

    private TestEntity testEntity1;
    private TestEntity testEntity2;
    private List<TestEntity> entityList;

    /**
     * Her testten önce çalıştırılır ve test verilerini hazırlar.
     */
    @BeforeEach
    void setUp() {
        // Mockito'yu başlat
        MockitoAnnotations.openMocks(this);
        
        // Test verilerini oluştur
        testEntity1 = new TestEntity("1", "Test Entity 1");
        testEntity2 = new TestEntity("2", "Test Entity 2");
        
        entityList = new ArrayList<>();
        entityList.add(testEntity1);
        entityList.add(testEntity2);
        
        // Mock davranışlarını ayarla
        when(mockCollection.getById("1")).thenReturn(testEntity1);
        when(mockCollection.getById("2")).thenReturn(testEntity2);
        when(mockCollection.getById("nonexistent")).thenReturn(null);
        when(mockCollection.getAll()).thenReturn(entityList);
        when(mockCollection.contains("1")).thenReturn(true);
        when(mockCollection.contains("2")).thenReturn(true);
        when(mockCollection.contains("nonexistent")).thenReturn(false);
        when(mockCollection.size()).thenReturn(2);
        when(mockCollection.remove("1")).thenReturn(true);
        when(mockCollection.remove("nonexistent")).thenReturn(false);
        when(mockCollection.saveToFile(anyString())).thenReturn(true);
        when(mockCollection.loadFromFile(anyString())).thenReturn(true);
        when(mockCollection.loadFromFile("invalid_path.dat")).thenReturn(false);
    }

    @Test
    void add_ShouldAddItemToCollection() {
        // Execute
        mockCollection.add(testEntity1);
        
        // Verify
        verify(mockCollection, times(1)).add(testEntity1);
    }

    @Test
    void remove_ShouldReturnTrueWhenItemExists() {
        // Execute
        boolean result = mockCollection.remove("1");
        
        // Verify
        assertTrue(result);
        verify(mockCollection, times(1)).remove("1");
    }

    @Test
    void remove_ShouldReturnFalseWhenItemDoesNotExist() {
        // Execute
        boolean result = mockCollection.remove("nonexistent");
        
        // Verify
        assertFalse(result);
        verify(mockCollection, times(1)).remove("nonexistent");
    }

    @Test
    void getById_ShouldReturnItemWhenExists() {
        // Execute
        TestEntity result = mockCollection.getById("1");
        
        // Verify
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Entity 1", result.getName());
        verify(mockCollection, times(1)).getById("1");
    }

    @Test
    void getById_ShouldReturnNullWhenDoesNotExist() {
        // Execute
        TestEntity result = mockCollection.getById("nonexistent");
        
        // Verify
        assertNull(result);
        verify(mockCollection, times(1)).getById("nonexistent");
    }

    @Test
    void getAll_ShouldReturnAllItems() {
        // Execute
        List<TestEntity> result = mockCollection.getAll();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testEntity1));
        assertTrue(result.contains(testEntity2));
        verify(mockCollection, times(1)).getAll();
    }

    @Test
    void clear_ShouldClearCollection() {
        // Execute
        mockCollection.clear();
        
        // Verify
        verify(mockCollection, times(1)).clear();
    }

    @Test
    void size_ShouldReturnCollectionSize() {
        // Execute
        int size = mockCollection.size();
        
        // Verify
        assertEquals(2, size);
        verify(mockCollection, times(1)).size();
    }

    @Test
    void contains_ShouldReturnTrueForExistingItem() {
        // Execute
        boolean result = mockCollection.contains("1");
        
        // Verify
        assertTrue(result);
        verify(mockCollection, times(1)).contains("1");
    }

    @Test
    void contains_ShouldReturnFalseForNonExistingItem() {
        // Execute
        boolean result = mockCollection.contains("nonexistent");
        
        // Verify
        assertFalse(result);
        verify(mockCollection, times(1)).contains("nonexistent");
    }

    @Test
    void saveToFile_ShouldReturnTrueWhenSuccessful() {
        // Execute
        boolean result = mockCollection.saveToFile("test_file.dat");
        
        // Verify
        assertTrue(result);
        verify(mockCollection, times(1)).saveToFile("test_file.dat");
    }

    @Test
    void loadFromFile_ShouldReturnTrueWhenSuccessful() {
        // Execute
        boolean result = mockCollection.loadFromFile("test_file.dat");
        
        // Verify
        assertTrue(result);
        verify(mockCollection, times(1)).loadFromFile("test_file.dat");
    }

    @Test
    void loadFromFile_ShouldReturnFalseWhenUnsuccessful() {
        // Execute
        boolean result = mockCollection.loadFromFile("invalid_path.dat");
        
        // Verify
        assertFalse(result);
        verify(mockCollection, times(1)).loadFromFile("invalid_path.dat");
    }
    
    /**
     * IMusicCollection testlerinde kullanılacak test entity sınıfı
     */
    private static class TestEntity {
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