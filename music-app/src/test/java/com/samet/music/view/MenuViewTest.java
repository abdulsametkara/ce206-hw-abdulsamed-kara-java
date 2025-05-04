package com.samet.music.view;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * MenuView için test sınıfı - Kapsanmayan alanların testini içerir
 */
public class MenuViewTest {
    
    private ByteArrayOutputStream outContent;
    private final PrintStream originalOut = System.out;
    private TestMenuView menuView;
    
    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }
    
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testInputCannotBeEmpty() {
        // Boş girişten sonra geçerli giriş verildiğinde
        String input = "\nvalidInput\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        menuView = new TestMenuView(new Scanner(System.in));
        String result = menuView.testGetStringInput("Test prompt");
        
        assertEquals("validInput", result);
        assertTrue(outContent.toString().contains("Input cannot be empty. Please try again."));
    }
    
    @Test
    public void testInvalidNumberInput() {
        // Sayı olmayan giriş verildiğinde
        String input = "not-a-number\n42\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        menuView = new TestMenuView(new Scanner(System.in));
        int result = menuView.testGetIntInput("Test prompt");
        
        assertEquals(42, result);
        assertTrue(outContent.toString().contains("Invalid input. Please enter a number."));
    }
    
    @Test
    public void testNumberBelowMinimum() {
        // Alt sınırın altında giriş verildiğinde
        String input = "5\n15\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        menuView = new TestMenuView(new Scanner(System.in));
        int result = menuView.testGetIntInputWithMin("Test prompt", 10);
        
        assertEquals(15, result);
        assertTrue(outContent.toString().contains("Please enter a number greater than or equal to 10"));
    }
    
    @Test
    public void testNumberOutsideRange() {
        // Aralık dışında giriş verildiğinde
        String input = "1\n100\n25\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        menuView = new TestMenuView(new Scanner(System.in));
        int result = menuView.testGetIntInputWithRange("Test prompt", 10, 50);
        
        assertEquals(25, result);
        assertTrue(outContent.toString().contains("Please enter a number between 10 and 50"));
    }
    
    @Test
    public void testInvalidYesNoInput() {
        // Geçersiz y/n giriş verildiğinde
        String input = "maybe\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        menuView = new TestMenuView(new Scanner(System.in));
        boolean result = menuView.testGetYesNoInput("Test prompt");
        
        assertTrue(result);
        assertTrue(outContent.toString().contains("Please answer with 'y' or 'n'"));
    }
    
    @Test
    public void testDisplayInfoMessage() {
        // Bilgi mesajı gösterme
        menuView = new TestMenuView(new Scanner(System.in));
        menuView.testDisplayInfo("Test info message");
        
        assertTrue(outContent.toString().contains("[INFO] Test info message"));
    }
    
    @Test
    public void testClearScreen() {
        // Ekranı temizleme 
        menuView = new TestMenuView(new Scanner(System.in));
        menuView.testClearScreen();
        
        // ANSI escape kodlarının çıktıda olduğunu kontrol et
        assertTrue(outContent.toString().contains("\033[H\033[2J"));
    }
    
    /**
     * MenuView'in protected metodlarına erişmek için test alt sınıfı
     */
    private static class TestMenuView extends MenuView {
        
        public TestMenuView(Scanner scanner) {
            super(scanner);
        }
        
        @Override
        public MenuView display() {
            return null; // Test için değil
        }
        
        public String testGetStringInput(String prompt) {
            return getStringInput(prompt);
        }
        
        public int testGetIntInput(String prompt) {
            return getIntInput(prompt);
        }
        
        public int testGetIntInputWithMin(String prompt, int min) {
            return getIntInput(prompt, min);
        }
        
        public int testGetIntInputWithRange(String prompt, int min, int max) {
            return getIntInput(prompt, min, max);
        }
        
        public boolean testGetYesNoInput(String prompt) {
            return getYesNoInput(prompt);
        }
        
        public void testDisplayInfo(String message) {
            displayInfo(message);
        }
        
        public void testClearScreen() {
            clearScreen();
        }
    }
} 