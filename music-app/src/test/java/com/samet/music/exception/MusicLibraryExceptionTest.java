package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class MusicLibraryExceptionTest
 * @brief MusicLibraryException sınıfı için test sınıfı
 */
public class MusicLibraryExceptionTest {

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
     * @brief İlk constructor'ı test eder - sadece mesaj ile
     */
    @Test
    public void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Test error message";

        // Act
        MusicLibraryException exception = new MusicLibraryException(errorMessage);

        // Assert
        assertEquals("Hata mesajı eşleşmiyor", errorMessage, exception.getMessage());
        assertNull("Sebep istisnası null olmalı", exception.getCause());
        assertTrue("Exception sınıfından türemiş olmalı", exception instanceof Exception);
    }

    /**
     * @brief İkinci constructor'ı test eder - mesaj ve sebep ile
     */
    @Test
    public void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Test error message with cause";
        Throwable cause = new NullPointerException("Test cause");

        // Act
        MusicLibraryException exception = new MusicLibraryException(errorMessage, cause);

        // Assert
        assertEquals("Hata mesajı eşleşmiyor", errorMessage, exception.getMessage());
        assertEquals("Sebep istisnası eşleşmiyor", cause, exception.getCause());
        assertEquals("Orijinal sebebin mesajı korunmuş olmalı", "Test cause", exception.getCause().getMessage());
    }

    /**
     * @brief Boş mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        MusicLibraryException exception = new MusicLibraryException(emptyMessage);

        // Assert
        assertEquals("Boş mesaj korunmalı", emptyMessage, exception.getMessage());
    }

    /**
     * @brief null mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithNullMessage() {
        // Arrange & Act
        MusicLibraryException exception = new MusicLibraryException(null);

        // Assert
        assertNull("null mesaj korunmalı", exception.getMessage());
    }

    /**
     * @brief Zincirleme istisna davranışını test eder
     */
    @Test
    public void testExceptionChaining() {
        // Arrange - İç içe istisnalar oluştur
        IllegalArgumentException rootCause = new IllegalArgumentException("Root cause");
        SQLException middleCause = new SQLException("SQL error", rootCause);
        MusicLibraryException exception = new MusicLibraryException("Top level error", middleCause);

        // Act & Assert
        assertEquals("Doğrudan sebep doğru olmalı", middleCause, exception.getCause());
        assertEquals("Köke kadar istisnalar zinciri doğru olmalı", rootCause, exception.getCause().getCause());
    }

    /**
     * @brief Kalıtım hiyerarşisini test eder
     */
    @Test
    public void testInheritanceHierarchy() {
        // Arrange
        MusicLibraryException exception = new MusicLibraryException("Test");

        // Assert
        assertTrue("MusicLibraryException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("MusicLibraryException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }

    /**
     * @brief Exception'ın stack trace bilgisini kontrol eder
     */
    @Test
    public void testStackTraceInfo() {
        // Arrange
        MusicLibraryException exception = new MusicLibraryException("Test message");

        // Act
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Assert
        assertNotNull("Stack trace null olmamalı", stackTrace);
        assertTrue("Stack trace en az bir eleman içermeli", stackTrace.length > 0);

        // Stack trace'in ilk elemanı bu test metodu olmalı
        StackTraceElement firstElement = stackTrace[0];
        assertTrue("Stack trace bu test sınıfını içermeli",
                firstElement.getClassName().contains("MusicLibraryExceptionTest"));
    }

    /**
     * Inner class to test SQLException since it might not be directly accessible
     */
    private static class SQLException extends Exception {
        public SQLException(String message) {
            super(message);
        }

        public SQLException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}