package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class EntityNotFoundExceptionTest
 * @brief EntityNotFoundException sınıfı için test sınıfı
 */
public class EntityNotFoundExceptionTest {

    /**
     * @brief Tüm testlerden önce bir kez çalıştırılır
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Test hazırlığı için kullanılabilir (gerekirse)
    }

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Her test öncesi hazırlık (gerekirse)
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Her test sonrası temizlik (gerekirse)
    }

    /**
     * @brief Constructor'ı test eder - entityType ve id parametreleri ile
     */
    @Test
    public void testConstructorWithEntityTypeAndId() {
        // Arrange
        String entityType = "Album";
        String id = "123";
        String expectedMessage = entityType + " with ID " + id + " not found";

        // Act
        EntityNotFoundException exception = new EntityNotFoundException(entityType, id);

        // Assert
        assertEquals("Hata mesajı beklenen formatta olmalı", expectedMessage, exception.getMessage());
        assertNull("Sebep istisnası null olmalı", exception.getCause());
        assertTrue("MusicLibraryException sınıfından türemiş olmalı", exception instanceof MusicLibraryException);
    }

    /**
     * @brief Boş değerlerle constructor'ı test eder
     */
    @Test
    public void testConstructorWithEmptyValues() {
        // Arrange
        String emptyEntityType = "";
        String emptyId = "";
        String expectedMessage = " with ID  not found";

        // Act
        EntityNotFoundException exception = new EntityNotFoundException(emptyEntityType, emptyId);

        // Assert
        assertEquals("Boş değerlerle hata mesajı doğru olmalı", expectedMessage, exception.getMessage());
    }

    /**
     * @brief null değerlerle constructor'ı test eder
     */
    @Test
    public void testConstructorWithNullValues() {
        // Arrange & Act
        EntityNotFoundException exceptionWithNullEntityType = new EntityNotFoundException(null, "123");
        EntityNotFoundException exceptionWithNullId = new EntityNotFoundException("Album", null);

        // Assert
        assertEquals("null değer ile mesaj doğru oluşturulmalı",
                "null with ID 123 not found", exceptionWithNullEntityType.getMessage());
        assertEquals("null değer ile mesaj doğru oluşturulmalı",
                "Album with ID null not found", exceptionWithNullId.getMessage());
    }

    /**
     * @brief Farklı entity tipleri için mesaj oluşturmayı test eder
     */
    @Test
    public void testMessageForDifferentEntityTypes() {
        // Test various entity types and IDs
        String[][] testCases = {
                {"Album", "A123", "Album with ID A123 not found"},
                {"Song", "S456", "Song with ID S456 not found"},
                {"Artist", "AR789", "Artist with ID AR789 not found"},
                {"Playlist", "P101112", "Playlist with ID P101112 not found"}
        };

        for (String[] testCase : testCases) {
            String entityType = testCase[0];
            String id = testCase[1];
            String expectedMessage = testCase[2];

            EntityNotFoundException exception = new EntityNotFoundException(entityType, id);
            assertEquals(
                    entityType + " için hata mesajı doğru oluşturulmalı",
                    expectedMessage,
                    exception.getMessage()
            );
        }
    }

    /**
     * @brief Kalıtım hiyerarşisini test eder
     */
    @Test
    public void testInheritanceHierarchy() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Album", "123");

        // Assert
        assertTrue("EntityNotFoundException, MusicLibraryException'dan türemiş olmalı",
                exception instanceof MusicLibraryException);
        assertTrue("EntityNotFoundException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("EntityNotFoundException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }
}