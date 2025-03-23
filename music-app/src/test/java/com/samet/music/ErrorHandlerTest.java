/**
 * @file ErrorHandlerTest.java
 * @brief This file contains the test cases for the ErrorHandler class.
 * @details This file includes test methods to validate the functionality of the ErrorHandler class. It uses JUnit for unit testing.
 */
package com.samet.music;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.samet.music.exception.ValidationException;
import com.samet.music.exception.ResourceNotFoundException;
import com.samet.music.exception.DatabaseOperationException;

import org.junit.*;

/**
 * @class ErrorHandlerTest
 * @brief This class represents the test class for the ErrorHandler class.
 * @details The ErrorHandlerTest class provides test methods to verify the behavior of the ErrorHandler class.
 * @author samet.kara
 */
public class ErrorHandlerTest {
    // Logger'ı test etmek için kullanılacak değişkenler
    private static Logger logger;
    private static TestLogHandler testLogHandler;

    /**
     * @brief This method is executed once before all test methods.
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // ErrorHandler sınıfı ile aynı Logger'ı al
        logger = Logger.getLogger(ErrorHandler.class.getName());

        // Test için özel log handler oluştur
        testLogHandler = new TestLogHandler();
        logger.addHandler(testLogHandler);

        // Parent handler'ların müdahale etmemesi için
        logger.setUseParentHandlers(false);
    }

    /**
     * @brief This method is executed once after all test methods.
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Eklenen handler'ı kaldır
        logger.removeHandler(testLogHandler);

        // Parent handler'ları geri aç
        logger.setUseParentHandlers(true);
    }

    /**
     * @brief This method is executed before each test method.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Her test öncesinde log kayıtlarını temizle
        testLogHandler.clear();
    }

    /**
     * @brief This method is executed after each test method.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        // Gerekirse temizlik işlemleri buraya
    }


    @Test
    public void testLoggerInitialization() {
        // Logger'ın aynı isimle oluşturulduğunu doğrula
        assertEquals("com.samet.music.ErrorHandler", logger.getName());

        // Logger'ın null olmadığını kontrol et
        assertNotNull(logger);
    }

    /**
     * @brief Tests if the logger logs error messages correctly
     */
    @Test
    public void testLogError() {
        // Test için değişkenler
        String errorMessage = "Test error message";
        Exception testException = new RuntimeException("Test exception");

        // ErrorHandler.logError metodunu çağır
        ErrorHandler.logError(errorMessage, testException);

        // Log seviyesinin SEVERE olduğunu kontrol et
        assertEquals(Level.SEVERE, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals(errorMessage, testLogHandler.getLastLogRecord().getMessage());

        // Hata nesnesinin (throwable) doğru şekilde kaydedildiğini kontrol et
        assertSame(testException, testLogHandler.getLastLogRecord().getThrown());
    }

    /**
     * @brief Tests if the logError method properly handles null throwable
     */
    @Test
    public void testLogErrorWithNullThrowable() {
        // Test için değişkenler
        String errorMessage = "Error with null throwable";

        // ErrorHandler.logError metodunu null throwable ile çağır
        ErrorHandler.logError(errorMessage, null);

        // Log seviyesinin SEVERE olduğunu kontrol et
        assertEquals(Level.SEVERE, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals(errorMessage, testLogHandler.getLastLogRecord().getMessage());

        // Throwable'ın null olduğunu kontrol et
        assertNull(testLogHandler.getLastLogRecord().getThrown());
    }

    /**
     * @brief Tests if the logger logs warning messages correctly
     */
    @Test
    public void testLogWarning() {
        // Test için değişkenler
        String warningMessage = "Test warning message";

        // ErrorHandler.logWarning metodunu çağır
        ErrorHandler.logWarning(warningMessage);

        // Log seviyesinin WARNING olduğunu kontrol et
        assertEquals(Level.WARNING, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals(warningMessage, testLogHandler.getLastLogRecord().getMessage());

        // Throwable'ın null olduğunu kontrol et (warning'de throwable gönderilmiyor)
        assertNull(testLogHandler.getLastLogRecord().getThrown());
    }

    /**
     * @brief Tests if the logWarning method handles empty message
     */
    @Test
    public void testLogWarningWithEmptyMessage() {
        // Boş mesajla test
        String emptyMessage = "";

        // ErrorHandler.logWarning metodunu boş mesajla çağır
        ErrorHandler.logWarning(emptyMessage);

        // Log seviyesinin WARNING olduğunu kontrol et
        assertEquals(Level.WARNING, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının boş olduğunu kontrol et
        assertEquals(emptyMessage, testLogHandler.getLastLogRecord().getMessage());
    }

    /**
     * @brief Tests if the logger logs info messages correctly
     */
    @Test
    public void testLogInfo() {
        // Test için değişkenler
        String infoMessage = "Test info message";

        // ErrorHandler.logInfo metodunu çağır
        ErrorHandler.logInfo(infoMessage);

        // Log seviyesinin INFO olduğunu kontrol et
        assertEquals(Level.INFO, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals(infoMessage, testLogHandler.getLastLogRecord().getMessage());

        // Throwable'ın null olduğunu kontrol et (info'da throwable gönderilmiyor)
        assertNull(testLogHandler.getLastLogRecord().getThrown());
    }

    /**
     * @brief Tests if the logger correctly handles special characters in info messages
     */
    @Test
    public void testLogInfoWithSpecialCharacters() {
        // Özel karakterlerle test
        String specialMessage = "Test message with special chars: !@#$%^&*()_+";

        // ErrorHandler.logInfo metodunu özel karakterli mesajla çağır
        ErrorHandler.logInfo(specialMessage);

        // Log seviyesinin INFO olduğunu kontrol et
        assertEquals(Level.INFO, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru şekilde kaydedildiğini kontrol et
        assertEquals(specialMessage, testLogHandler.getLastLogRecord().getMessage());
    }

    /**
     * @brief Simple custom log handler for testing purposes
     */
    private static class TestLogHandler extends Handler {
        private LogRecord lastLogRecord;

        @Override
        public void publish(LogRecord record) {
            lastLogRecord = record;
        }

        @Override
        public void flush() {
            // No implementation needed for testing
        }

        @Override
        public void close() throws SecurityException {
            // No implementation needed for testing
        }

        public LogRecord getLastLogRecord() {
            return lastLogRecord;
        }

        public void clear() {
            lastLogRecord = null;
        }
    }
    /**
     * @brief Tests for the getUserFriendlyErrorMessage method
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithValidationException() {
        // Arrange
        ValidationException testException = new ValidationException("Invalid username format");

        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(testException);

        // Assert
        assertEquals("The information entered is invalid: Invalid username format", result);
    }

    /**
     * @brief Tests getUserFriendlyErrorMessage with ResourceNotFoundException
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException testException = new ResourceNotFoundException("Artist with ID 123 not found");

        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(testException);

        // Assert
        assertEquals("The requested resource was not found: Artist with ID 123 not found", result);
    }

    /**
     * @brief Tests getUserFriendlyErrorMessage with DatabaseOperationException
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithDatabaseOperationException() {
        // Arrange
        DatabaseOperationException testException = new DatabaseOperationException("Failed to update song record");

        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(testException);

        // Assert
        assertEquals("An error occurred in the database operation: Failed to update song record", result);
    }

    /**
     * @brief Tests getUserFriendlyErrorMessage with generic exception
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithGenericException() {
        // Arrange
        Exception testException = new Exception("Unknown error");

        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(testException);

        // Assert
        assertEquals("An unexpected error occurred. Please try again later.", result);
    }

    /**
     * @brief Tests getUserFriendlyErrorMessage with null exception
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithNullException() {
        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(null);

        // Assert
        assertEquals("An unexpected error occurred. Please try again later.", result);
    }

    /**
     * @brief Tests getUserFriendlyErrorMessage with exception that has null message
     */
    @Test
    public void testGetUserFriendlyErrorMessageWithNullExceptionMessage() {
        // Arrange
        ValidationException testException = new ValidationException(null);

        // Act
        String result = ErrorHandler.getUserFriendlyErrorMessage(testException);

        // Assert
        assertEquals("The information entered is invalid: null", result);
    }

    /**
     * @brief Tests the handleException method
     */
    @Test
    public void testHandleException() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");

        // Testi çalıştırmadan önce TestLogHandler'ı temizleyelim
        testLogHandler.clear();

        // Act
        ErrorHandler.handleException(testException);

        // Assert
        // Log kaydının SEVERE seviyesinde olduğunu kontrol et
        assertEquals(Level.SEVERE, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals("An error occurred", testLogHandler.getLastLogRecord().getMessage());

        // İletilen exception nesnesinin doğru olduğunu kontrol et
        assertSame(testException, testLogHandler.getLastLogRecord().getThrown());
    }

    /**
     * @brief Tests the handleException method with null exception
     */
    @Test
    public void testHandleExceptionWithNullThrowable() {
        // Testi çalıştırmadan önce TestLogHandler'ı temizleyelim
        testLogHandler.clear();

        // Act
        ErrorHandler.handleException(null);

        // Assert
        // Log kaydının SEVERE seviyesinde olduğunu kontrol et
        assertEquals(Level.SEVERE, testLogHandler.getLastLogRecord().getLevel());

        // Log mesajının doğru olduğunu kontrol et
        assertEquals("An error occurred", testLogHandler.getLastLogRecord().getMessage());

        // Throwable'ın null olduğunu kontrol et
        assertNull(testLogHandler.getLastLogRecord().getThrown());
    }
}
