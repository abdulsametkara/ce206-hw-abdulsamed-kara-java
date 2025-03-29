package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class ValidationExceptionTest
 * @brief ValidationException sınıfı için test sınıfı
 */
public class ValidationExceptionTest {

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
        String errorMessage = "Song name cannot be empty";

        // Act
        ValidationException exception = new ValidationException(errorMessage);

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
        ValidationException exception = new ValidationException(emptyMessage);

        // Assert
        assertEquals("Boş mesaj korunmalı", emptyMessage, exception.getMessage());
    }

    /**
     * @brief null mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithNullMessage() {
        // Arrange & Act
        ValidationException exception = new ValidationException(null);

        // Assert
        assertNull("null mesaj korunmalı", exception.getMessage());
    }

    /**
     * @brief Farklı doğrulama hata mesajları için exception oluşturmayı test eder
     */
    @Test
    public void testWithDifferentValidationMessages() {
        // Test various validation error messages
        String[] errorMessages = {
                "Album year must be a positive number",
                "Artist name is required",
                "Song duration must be greater than zero",
                "Playlist name must be between 3 and 50 characters"
        };

        for (String message : errorMessages) {
            ValidationException exception = new ValidationException(message);
            assertEquals(
                    "Doğrulama hata mesajı korunmalı",
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
        ValidationException exception = new ValidationException("Test message");

        // Assert
        assertTrue("ValidationException, MusicLibraryException'dan türemiş olmalı",
                exception instanceof MusicLibraryException);
        assertTrue("ValidationException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("ValidationException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }

    /**
     * @brief Exception'ın stack trace bilgisini kontrol eder
     */
    @Test
    public void testStackTraceInfo() {
        // Arrange
        ValidationException exception = new ValidationException("Test message");

        // Act
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Assert
        assertNotNull("Stack trace null olmamalı", stackTrace);
        assertTrue("Stack trace en az bir eleman içermeli", stackTrace.length > 0);

        // Stack trace'in ilk elemanı bu test metodu olmalı
        StackTraceElement firstElement = stackTrace[0];
        assertTrue("Stack trace bu test sınıfını içermeli",
                firstElement.getClassName().contains("ValidationExceptionTest"));
    }

    /**
     * @brief Exception'ı try-catch bloğunda kullanma durumunu test eder
     */
    @Test
    public void testExceptionInTryCatch() {
        // Arrange
        String errorMessage = "Invalid data format";
        boolean exceptionCaught = false;

        // Act
        try {
            // Bir exception oluştur ve fırlat
            throw new ValidationException(errorMessage);
        } catch (ValidationException e) {
            // Exception yakalandı
            exceptionCaught = true;
            assertEquals("Yakalanan exception'ın mesajı doğru olmalı", errorMessage, e.getMessage());
        } catch (Exception e) {
            fail("ValidationException dışında bir exception yakalandı: " + e.getClass().getName());
        }

        // Assert
        assertTrue("ValidationException yakalanmış olmalı", exceptionCaught);
    }

    /**
     * @brief MusicLibraryException olarak yakalama durumunu test eder
     */
    @Test
    public void testCatchingAsMusicLibraryException() {
        // Arrange
        String errorMessage = "Invalid input";
        boolean exceptionCaught = false;

        // Act
        try {
            throw new ValidationException(errorMessage);
        } catch (MusicLibraryException e) {
            // Üst sınıf olarak yakalandı
            exceptionCaught = true;
            assertTrue("Exception doğru türde olmalı", e instanceof ValidationException);
            assertEquals("Exception mesajı korunmalı", errorMessage, e.getMessage());
        }

        // Assert
        assertTrue("Exception MusicLibraryException olarak yakalanmış olmalı", exceptionCaught);
    }
}