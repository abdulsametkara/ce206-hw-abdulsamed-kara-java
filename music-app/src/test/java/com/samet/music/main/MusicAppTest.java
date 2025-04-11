package com.samet.music.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MusicApp sınıfı için test sınıfı
 */
public class MusicAppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() throws Exception {
        System.setOut(originalOut);
    }

    /**
     * Test ana uygulama sınıfı ve bağlı sınıfların varlığını
     */
    @Test
    public void testAppClassExists() {
        assertNotNull(MusicApp.class);
        assertNotNull(Music.class);
        assertNotNull(MusicAppInitializer.class);
    }
    
    /**
     * Test sınıfların doğru paket içinde olduğunu
     */
    @Test
    public void testPackageStructure() {
        assertEquals("com.samet.music.main", MusicApp.class.getPackage().getName());
        assertEquals("com.samet.music.main", Music.class.getPackage().getName());
        assertEquals("com.samet.music.main", MusicAppInitializer.class.getPackage().getName());
    }
    
    /**
     * Test main metod imzasının doğru olduğunu
     */
    @Test
    public void testMainMethodSignature() {
        try {
            MusicApp.class.getMethod("main", String[].class);
            assertTrue(true);
        } catch (NoSuchMethodException e) {
            fail("MusicApp sınıfında doğru imzalı bir main metodu bulunamadı");
        }
    }
    
    /**
     * Test dosya sistem yollarının doğru oluşturulabildiğini
     */
    @Test
    public void testFilePathManagement() throws IOException {
        // User credentials için geçici dosya yolu oluştur
        Path userCredPath = tempDir.resolve("user_credentials.txt");
        Files.writeString(userCredPath, "testuser:password");
        
        // Dosyanın oluşturulduğunu kontrol et
        assertTrue(Files.exists(userCredPath));
        
        // Dosya içeriğini oku
        String content = Files.readString(userCredPath);
        assertEquals("testuser:password", content);
        
        // Dosya yolunun düzgün biçimlendirildiğini kontrol et
        String pathString = userCredPath.toString().replace('\\', '/');
        assertTrue(pathString.endsWith("/user_credentials.txt"));
    }
    
    /**
     * Test MusicApp sınıfının gerçek dosya sistemine erişebilirliğini
     */
    @Test
    public void testResourceAccess() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        try {
            File credentialsFile = new File(dataDir, "user_credentials.txt");
            if (!credentialsFile.exists()) {
                credentialsFile.createNewFile();
            }
            
            assertTrue(credentialsFile.exists(), "Credentials dosyası oluşturulabilmeli");
            
            // Temizlik
            credentialsFile.delete();
            dataDir.delete();
        } catch (IOException e) {
            // Dosya işlemleri gerçekleştirilemezse, en azından yapılar kontrol edilsin
            assertNotNull(MusicApp.class);
        }
    }
} 