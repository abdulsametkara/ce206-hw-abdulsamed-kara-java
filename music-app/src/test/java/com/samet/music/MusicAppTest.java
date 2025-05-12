package com.samet.music;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for MusicApp
 */
public class MusicAppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * Test that MusicApp class exists
     */
    @Test
    public void testMusicAppClassExists() {
        // Just verify that the MusicApp class can be loaded
        MusicApp app = new MusicApp();
        assertNotNull(app);
    }
    
    /**
     * Test that main method exists
     */
    @Test
    public void testMainMethodExists() {
        boolean hasMainMethod = false;
        try {
            MusicApp.class.getMethod("main", String[].class);
            hasMainMethod = true;
        } catch (NoSuchMethodException e) {
            fail("Main method not found in MusicApp class");
        }
        
        assertTrue("The MusicApp class should have a main method", hasMainMethod);
    }
    
    /**
     * Test that MusicApp class has required private methods
     */
    @Test
    public void testRunGuiAppMethodExists() {
        boolean hasRunGuiApp = false;
        try {
            Method method = MusicApp.class.getDeclaredMethod("runGuiApp");
            hasRunGuiApp = true;
        } catch (NoSuchMethodException e) {
            // Method doesn't exist
        }
        
        assertTrue("The MusicApp class should have a runGuiApp method", hasRunGuiApp);
    }
    
    /**
     * Test that runConsoleApp method exists
     */
    @Test
    public void testRunConsoleAppMethodExists() {
        boolean hasRunConsoleApp = false;
        try {
            Method method = MusicApp.class.getDeclaredMethod("runConsoleApp");
            hasRunConsoleApp = true;
        } catch (NoSuchMethodException e) {
            // Method doesn't exist
        }
        
        assertTrue("The MusicApp class should have a runConsoleApp method", hasRunConsoleApp);
    }
    
    /**
     * Test basic initialization of the app
     */
    @Test
    public void testBasicInitialization() {
        // Simple test to verify constructor and static initialization
        MusicApp app = new MusicApp();
        assertNotNull("App should be created without exceptions", app);
    }
}
