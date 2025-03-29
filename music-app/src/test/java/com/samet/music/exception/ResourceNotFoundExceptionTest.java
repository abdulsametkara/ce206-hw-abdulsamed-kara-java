package com.samet.music.exception;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * @class ResourceNotFoundExceptionTest
 * @brief ResourceNotFoundException sınıfı için test sınıfı
 */
public class ResourceNotFoundExceptionTest {

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
        String errorMessage = "Resource file 'songs.xml' not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

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
        ResourceNotFoundException exception = new ResourceNotFoundException(emptyMessage);

        // Assert
        assertEquals("Boş mesaj korunmalı", emptyMessage, exception.getMessage());
    }

    /**
     * @brief null mesaj ile constructor'ı test eder
     */
    @Test
    public void testConstructorWithNullMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

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
                "Album cover image not found",
                "Song lyrics file not found",
                "Artist photo not found",
                "Configuration file 'settings.json' not found"
        };

        for (String message : errorMessages) {
            ResourceNotFoundException exception = new ResourceNotFoundException(message);
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
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

        // Assert
        assertTrue("ResourceNotFoundException, MusicLibraryException'dan türemiş olmalı",
                exception instanceof MusicLibraryException);
        assertTrue("ResourceNotFoundException, Exception'dan türemiş olmalı",
                exception instanceof Exception);
        assertTrue("ResourceNotFoundException, Throwable'dan türemiş olmalı",
                exception instanceof Throwable);
    }

    /**
     * @brief Exception'ın stack trace bilgisini kontrol eder
     */
    @Test
    public void testStackTraceInfo() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

        // Act
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Assert
        assertNotNull("Stack trace null olmamalı", stackTrace);
        assertTrue("Stack trace en az bir eleman içermeli", stackTrace.length > 0);

        // Stack trace'in ilk elemanı bu test metodu olmalı
        StackTraceElement firstElement = stackTrace[0];
        assertTrue("Stack trace bu test sınıfını içermeli",
                firstElement.getClassName().contains("ResourceNotFoundExceptionTest"));
    }

    /**
     * @brief Exception'ı try-catch bloğunda kullanma durumunu test eder
     */
    @Test
    public void testExceptionInTryCatch() {
        // Arrange
        String errorMessage = "Resource file not found";
        boolean exceptionCaught = false;

        // Act
        try {
            // Bir exception oluştur ve fırlat
            throw new ResourceNotFoundException(errorMessage);
        } catch (ResourceNotFoundException e) {
            // Exception yakalandı
            exceptionCaught = true;
            assertEquals("Yakalanan exception'ın mesajı doğru olmalı", errorMessage, e.getMessage());
        } catch (Exception e) {
            fail("ResourceNotFoundException dışında bir exception yakalandı: " + e.getClass().getName());
        }

        // Assert
        assertTrue("ResourceNotFoundException yakalanmış olmalı", exceptionCaught);
    }
}