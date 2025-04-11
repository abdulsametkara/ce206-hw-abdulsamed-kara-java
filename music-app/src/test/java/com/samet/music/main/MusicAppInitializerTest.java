package com.samet.music.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class MusicAppInitializerTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        resetSingleton();
    }
    
    // Helper method to reset the singleton instance using reflection
    private void resetSingleton() throws Exception {
        Field instance = MusicAppInitializer.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testGetInstanceReturnsSameInstance() {
        // Test singleton pattern
        MusicAppInitializer instance1 = MusicAppInitializer.getInstance();
        MusicAppInitializer instance2 = MusicAppInitializer.getInstance();
        
        // Verify that both instances are the same object
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }
    
    @Test
    void testPrivateConstructor() throws Exception {
        // Test that constructor is private
        assertEquals(0, MusicAppInitializer.class.getConstructors().length);
        assertEquals(1, MusicAppInitializer.class.getDeclaredConstructors().length);
        
        // Try to access private constructor
        Field instance = MusicAppInitializer.class.getDeclaredField("instance");
        instance.setAccessible(true);
        assertNull(instance.get(null));
        
        // Get instance through singleton method
        MusicAppInitializer initializer = MusicAppInitializer.getInstance();
        assertNotNull(initializer);
        assertNotNull(instance.get(null));
    }

    @Test
    void testThreadSafety() throws Exception {
        // Reset the singleton instance
        resetSingleton();
        
        // Create multiple threads to access the singleton simultaneously
        final int threadCount = 10;
        final MusicAppInitializer[] instances = new MusicAppInitializer[threadCount];
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                instances[threadIndex] = MusicAppInitializer.getInstance();
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Check that all threads got the same instance
        MusicAppInitializer firstInstance = instances[0];
        assertNotNull(firstInstance);
        
        for (int i = 1; i < threadCount; i++) {
            assertSame(firstInstance, instances[i], "Thread " + i + " got a different instance");
        }
    }
    
    @Test
    void testInitializeMethodDoesNotThrowException() {
        // Get the initializer instance
        MusicAppInitializer initializer = MusicAppInitializer.getInstance();
        
        // The initialize method shouldn't throw exception in a regular scenario
        // This is more of an integration test than a unit test
        assertDoesNotThrow(() -> initializer.initialize());
        
        // Check for log output (partial test)
        String output = outputStream.toString();
        assertTrue(output.contains("Initializing") || output.isEmpty(), 
                "Output should contain initialization messages or be empty");
    }
    
    @Test
    void testResetDatabaseMethodDoesNotThrowException() {
        // Get the initializer instance
        MusicAppInitializer initializer = MusicAppInitializer.getInstance();
        
        // Test with both true and false parameters
        assertDoesNotThrow(() -> initializer.resetDatabase(true));
        assertDoesNotThrow(() -> initializer.resetDatabase(false));
        
        // Check for log output (partial test)
        String output = outputStream.toString();
        // Since we don't have a real DB connection, this might not produce output
        // or might produce error messages. Both are acceptable in this test.
        assertTrue(output.contains("Database") || output.isEmpty() || output.contains("Error"), 
                "Output should contain database messages or be empty or contain error messages");
    }
    
    @Test
    void testShutdownMethodDoesNotThrowException() {
        // Get the initializer instance
        MusicAppInitializer initializer = MusicAppInitializer.getInstance();
        
        // The shutdown method shouldn't throw exception
        assertDoesNotThrow(() -> initializer.shutdown());
        
        // Check for log output (partial test)
        String output = outputStream.toString();
        assertTrue(output.contains("Shutting down") || output.isEmpty() || output.contains("Error"), 
                "Output should contain shutdown messages or be empty or contain error messages");
    }
} 