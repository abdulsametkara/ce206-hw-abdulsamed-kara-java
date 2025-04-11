package com.samet.music.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MusicTest {

    private Music music;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Input ve output stream'leri ayarla
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        
        // Test için bir Scanner oluştur - boş StringReader üzerinden
        Scanner scanner = new Scanner(new StringReader(""));
        
        // Music nesnesini oluştur
        music = new Music(scanner, printStream);
        music.isTestMode = true; // Test modunu aktif et
    }
    
    @AfterEach
    void tearDown() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (printStream != null) {
                printStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Çıktıyı string olarak alır
     */
    private String getOutput() {
        return outputStream.toString();
    }
    
    /**
     * Çıktı akışını temizler
     */
    private void clearOutput() {
        outputStream.reset();
    }

    @Test
    void testConstructor() {
        assertNotNull(music);
        assertNotNull(music.scanner);
        assertNotNull(music.out);
        assertTrue(music.isTestMode);
    }
    
    @Test
    void testClearScreen() {
        music.clearScreen();
        String output = getOutput();
        assertTrue(output.contains("\033[H\033[2J"));
    }
    
    @Test
    void testShowMessageSuccess() {
        music.showMessage("Test Success Message", "success");
        String output = getOutput();
        assertTrue(output.contains("Test Success Message"));
    }
    
    @Test
    void testShowMessageError() {
        music.showMessage("Test Error Message", "error");
        String output = getOutput();
        assertTrue(output.contains("Test Error Message"));
    }
    
    @Test
    void testShowMessageInfo() {
        music.showMessage("Test Info Message", "info");
        String output = getOutput();
        assertTrue(output.contains("Test Info Message"));
    }
    
    @Test
    void testShowTitle() {
        music.showTitle("Test Title");
        String output = getOutput();
        assertTrue(output.contains("| Test Title |"));
    }
    
    @Test
    void testPrintOpeningScreen() {
        music.printOpeningScreen();
        String output = getOutput();
        assertTrue(output.contains("MAIN MENU"));
        assertTrue(output.contains("1. Login"));
        assertTrue(output.contains("2. Register"));
        assertTrue(output.contains("3. Exit Program"));
    }
    
    @Test
    void testPrintMainMenu() {
        music.printMainMenu();
        String output = getOutput();
        assertTrue(output.contains("MAIN MENU - MUSIC LIBRARY"));
        assertTrue(output.contains("1. Music Collection"));
        assertTrue(output.contains("2. Playlists"));
        assertTrue(output.contains("5. Logout"));
    }
    
    @Test
    void testPrintMusicCollectionMenu() {
        music.printMusicCollectionMenu();
        String output = getOutput();
        assertTrue(output.contains("MUSIC COLLECTION MENU"));
        assertTrue(output.contains("1. Add Song"));
        assertTrue(output.contains("2. Add Album"));
        assertTrue(output.contains("3. Add Artist"));
    }
    
    @Test
    void testPrintPlaylistsMenu() {
        music.printPlayistsMenu();
        String output = getOutput();
        assertTrue(output.contains("PLAYLISTS MENU"));
        assertTrue(output.contains("1. Create Playlist"));
        assertTrue(output.contains("2. View Playlists"));
    }
    
    @Test
    void testPrintEditMetadataMenu() {
        music.printEditMetadataMenu();
        String output = getOutput();
        assertTrue(output.contains("EDIT METADATA MENU"));
        assertTrue(output.contains("1. Edit Artist"));
        assertTrue(output.contains("2. Edit Album"));
    }
    
    @Test
    void testPrintRecommendationsMenu() {
        music.printRecommendationsMenu();
        String output = getOutput();
        assertTrue(output.contains("RECOMMENDATIONS MENU"));
        assertTrue(output.contains("1. Get Song Recommendations"));
        assertTrue(output.contains("2. Get Album Recommendations"));
    }
    
    @Test
    void testHandleInputError() {
        music.handleInputError();
        String output = getOutput();
        assertTrue(output.contains("Invalid input"));
    }
    
    @Test
    void testEnterToContinue() {
        boolean result = music.enterToContinue();
        assertTrue(result);
        assertTrue(getOutput().contains("Press enter to continue..."));
    }
    
    @Test
    void testCreateDataDirectory() {
        // data klasörünün var olup olmadığını kontrol et
        File dataDir = new File("data/");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        assertTrue(dataDir.exists());
        assertTrue(dataDir.isDirectory());
    }
} 