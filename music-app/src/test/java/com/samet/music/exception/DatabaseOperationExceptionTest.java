package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class DatabaseOperationExceptionTest
 * @brief DatabaseOperationException sınıfı için test sınıfı
 */
public class DatabaseOperationExceptionTest {

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
     * @brief İlk constructor'ı test eder - sadece mesaj içeren
     */
    @Test
    public void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Test error message";

        // Act
        DatabaseOperationException exception = new DatabaseOperationException(errorMessage);

        // Assert
        assertEquals("Hata mesajı eşleşmiyor", errorMessage, exception.getMessage());
        assertNull("Sebep istisnası null olmalı", exception.getCause());
        assertTrue("MusicLibraryException sınıfından türemiş olmalı", exception instanceof MusicLibraryException);
    }

    /**
     * @brief İkinci constructor'ı test eder - mesaj ve sebep içeren
     */
    @Test
    public void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Test error message with cause";
        Throwable cause = new NullPointerException("Test cause");

        // Act
        DatabaseOperationException exception = new DatabaseOperationException(errorMessage, cause);

        // Assert
        assertEquals("Hata mesajı eşleşmiyor", errorMessage, exception.getMessage());
        assertEquals("Sebep istisnası eşleşmiyor", cause, exception.getCause());
        assertTrue("MusicLibraryException sınıfından türemiş olmalı", exception instanceof MusicLibraryException);
    }

    /**
     * @brief Kalıtım hiyerarşisini test eder
     */
    @Test
    public void testInheritanceHierarchy() {
        // Arrange
        DatabaseOperationException exception = new DatabaseOperationException("Test");

        // Assert
        assertTrue("DatabaseOperationException, MusicLibraryException'dan türemiş olmalı",
                exception instanceof MusicLibraryException);
        assertTrue("DatabaseOperationException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("DatabaseOperationException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }

    /**
     * @brief Exception'ın stack trace bilgisi taşıyabilmesini test eder
     */
    @Test
    public void testStackTracePreservation() {
        // Arrange
        Throwable cause = new NullPointerException("Original cause");
        StackTraceElement[] originalStackTrace = cause.getStackTrace();

        // Act
        DatabaseOperationException exception = new DatabaseOperationException("Test with stack trace", cause);

        // Assert
        assertNotNull("Stack trace null olmamalı", exception.getStackTrace());
        StackTraceElement[] causeStackTrace = exception.getCause().getStackTrace();
        assertEquals("Orijinal stack trace'i korumalı", originalStackTrace.length, causeStackTrace.length);

        // İlk birkaç stack trace elemanının aynı olup olmadığını kontrol et
        int elementsToCheck = Math.min(3, originalStackTrace.length);
        for (int i = 0; i < elementsToCheck; i++) {
            assertEquals("Stack trace elemanı " + i + " korunmamış",
                    originalStackTrace[i], causeStackTrace[i]);
        }
    }
}