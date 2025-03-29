package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.UUID;
/**
 * @class BaseEntityTest
 * @brief BaseEntity sınıfı için test sınıfı
 */
public class BaseEntityTest {

    /**
     * BaseEntity soyut sınıfını test etmek için kullanılacak
     * özel bir alt sınıf tanımlıyoruz
     */
    private static class TestEntity extends BaseEntity {
        public TestEntity(String name) {
            super(name);
        }
    }

    private TestEntity entity;
    private final String TEST_NAME = "Test Entity";

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        entity = new TestEntity(TEST_NAME);
    }

    /**
     * @brief Constructor'ı test eder
     */
    @Test
    public void testConstructor() {
        // Assert
        assertNotNull("ID null olmamalı", entity.getId());
        assertEquals("İsim doğru atanmalı", TEST_NAME, entity.getName());
    }

    /**
     * @brief setId metodunu test eder - geçerli ID
     */
    @Test
    public void testSetIdWithValidId() {
        // Arrange
        String newId = "custom-id-123";

        // Act
        entity.setId(newId);

        // Assert
        assertEquals("ID doğru güncellenmeli", newId, entity.getId());
    }

    /**
     * @brief setId metodunu test eder - null ID
     */
    @Test
    public void testSetIdWithNullId() {
        // Arrange
        String originalId = entity.getId();

        // Act
        entity.setId(null);

        // Assert
        assertEquals("ID değişmemeli", originalId, entity.getId());
    }

    /**
     * @brief setId metodunu test eder - boş ID
     */
    @Test
    public void testSetIdWithEmptyId() {
        // Arrange
        String originalId = entity.getId();

        // Act
        entity.setId("");

        // Assert
        assertEquals("ID değişmemeli", originalId, entity.getId());
    }

    /**
     * @brief setName metodunu test eder
     */
    @Test
    public void testSetName() {
        // Arrange
        String newName = "Updated Name";

        // Act
        entity.setName(newName);

        // Assert
        assertEquals("İsim doğru güncellenmeli", newName, entity.getName());
    }

    /**
     * @brief equals metodunu test eder - aynı ID'ye sahip nesneler
     */
    @Test
    public void testEqualsWithSameId() {
        // Arrange
        String sameId = "same-id";
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 2");
        entity1.setId(sameId);
        entity2.setId(sameId);

        // Assert
        assertTrue("Aynı ID'ye sahip nesneler eşit olmalı", entity1.equals(entity2));
        assertTrue("equals metodu simetrik olmalı", entity2.equals(entity1));
    }

    /**
     * @brief equals metodunu test eder - farklı ID'ye sahip nesneler
     */
    @Test
    public void testEqualsWithDifferentId() {
        // Arrange
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 1");

        // Assert
        assertFalse("Farklı ID'ye sahip nesneler eşit olmamalı", entity1.equals(entity2));
    }

    /**
     * @brief equals metodunu test eder - aynı nesne
     */
    @Test
    public void testEqualsWithSameObject() {
        // Assert
        assertTrue("Nesne kendisine eşit olmalı", entity.equals(entity));
    }

    /**
     * @brief equals metodunu test eder - null ile
     */
    @Test
    public void testEqualsWithNull() {
        // Assert
        assertFalse("Nesne null'a eşit olmamalı", entity.equals(null));
    }

    /**
     * @brief equals metodunu test eder - farklı türden nesne ile
     */
    @Test
    public void testEqualsWithDifferentClass() {
        // Arrange
        Object other = new Object();

        // Assert
        assertFalse("Farklı türden nesneler eşit olmamalı", entity.equals(other));
    }

    /**
     * @brief hashCode metodunu test eder
     */
    @Test
    public void testHashCode() {
        // Arrange
        String customId = "hash-id";
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 2");
        entity1.setId(customId);
        entity2.setId(customId);

        // Assert
        assertEquals("Aynı ID'ye sahip nesnelerin hashCode'ları eşit olmalı",
                entity1.hashCode(), entity2.hashCode());

        // ID değişirse hashCode da değişmeli
        entity1.setId("different-id");
        assertNotEquals("Farklı ID'ye sahip nesnelerin hashCode'ları farklı olmalı",
                entity1.hashCode(), entity2.hashCode());
    }

    /**
     * @brief UUID'nin rastgele üretildiğini test eder
     */
    @Test
    public void testRandomUuidGeneration() {
        // Arrange
        TestEntity entity1 = new TestEntity("Entity 1");
        TestEntity entity2 = new TestEntity("Entity 2");

        // Assert
        assertNotEquals("Farklı nesneler için farklı ID'ler üretilmeli",
                entity1.getId(), entity2.getId());
    }

    /**
     * @brief getId metodunu test eder
     */
    @Test
    public void testGetId() {
        // Assert
        assertNotNull("getId null döndürmemeli", entity.getId());
        assertTrue("ID UUID formatında olmalı", isValidUUID(entity.getId()));
    }

    /**
     * @brief getName metodunu test eder
     */
    @Test
    public void testGetName() {
        // Assert
        assertEquals("getName doğru değeri döndürmeli", TEST_NAME, entity.getName());
    }

    /**
     * Verilen string'in geçerli bir UUID formatında olup olmadığını kontrol eder
     */
    private boolean isValidUUID(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}