package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class InvalidEntityDataExceptionTest
 * @brief InvalidEntityDataException sınıfı için test sınıfı
 */
public class InvalidEntityDataExceptionTest {

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
     * @brief Constructor'ı test eder - mesaj parametresi ile
     */
    @Test
    public void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Invalid album data: title cannot be empty";

        // Act
        InvalidEntityDataException exception = new InvalidEntityDataException(errorMessage);

        // Assert
        assertEquals("Hata mesajı eşleşmiyor", errorMessage, exception.getMessage());
        assertNull("Sebep istisnası null olmalı", exception.getCause());
        assertTrue("MusicLibraryException sınıfından türemiş olmalı", exception instanceof MusicLibraryException);
    }

    /**
     * @brief Boş mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        InvalidEntityDataException exception = new InvalidEntityDataException(emptyMessage);

        // Assert
        assertEquals("Boş mesaj korunmalı", emptyMessage, exception.getMessage());
    }

    /**
     * @brief null mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithNullMessage() {
        // Arrange & Act
        InvalidEntityDataException exception = new InvalidEntityDataException(null);

        // Assert
        assertNull("null mesaj korunmalı", exception.getMessage());
    }

    /**
     * @brief Farklı hata mesajları için exception oluşturmayı test eder
     */
    @Test
    public void testWithDifferentErrorMessages() {
        // Test various error messages
        String[] errorMessages = {
                "Invalid song data: duration must be positive",
                "Invalid artist data: name cannot be empty",
                "Invalid album data: release year must be valid",
                "Invalid playlist data: must contain at least one song"
        };

        for (String message : errorMessages) {
            InvalidEntityDataException exception = new InvalidEntityDataException(message);
            assertEquals(
                    "Hata mesajı korunmalı",
                    message,
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
        InvalidEntityDataException exception = new InvalidEntityDataException("Test message");

        // Assert
        assertTrue("InvalidEntityDataException, MusicLibraryException'dan türemiş olmalı",
                exception instanceof MusicLibraryException);
        assertTrue("InvalidEntityDataException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("InvalidEntityDataException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }

    /**
     * @brief Exception'ın stack trace bilgisini kontrol eder
     */
    @Test
    public void testStackTraceInfo() {
        // Arrange
        InvalidEntityDataException exception = new InvalidEntityDataException("Test message");

        // Act
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Assert
        assertNotNull("Stack trace null olmamalı", stackTrace);
        assertTrue("Stack trace en az bir eleman içermeli", stackTrace.length > 0);

        // Stack trace'in ilk elemanı bu test metodu olmalı
        StackTraceElement firstElement = stackTrace[0];
        assertTrue("Stack trace bu test sınıfını içermeli",
                firstElement.getClassName().contains("InvalidEntityDataExceptionTest"));
    }
}