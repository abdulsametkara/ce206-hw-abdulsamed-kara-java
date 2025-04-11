//package com.samet.music.main;
//
//import static org.junit.Assert.*;
//import org.junit.*;
//
//import com.samet.music.util.DatabaseManager;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.Scanner;
//
///**
// * @class MainTest
// * @brief Music sınıfı için test sınıfı
// */
//public class MainTest {
//
//    private Music music;
//    private ByteArrayOutputStream outputStream;
//    private PrintStream printStream;
//    private static final String TEST_DATA_DIR = "test-data/";
//
//    /**
//     * @brief Tüm testlerden önce bir kez çalıştırılır
//     */
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        // Test veritabanını kurma
//        DatabaseManager.setShouldResetDatabase(true);
//        DatabaseManager.initializeDatabase();
//    }
//
//    /**
//     * @brief Her testten önce çalıştırılır
//     */
//    @Before
//    public void setUp() throws Exception {
//        // Çıktıyı yakalamak için ByteArrayOutputStream kullan
//        outputStream = new ByteArrayOutputStream();
//        printStream = new PrintStream(outputStream);
//
//        // Varsayılan Scanner ile Music nesnesi oluştur
//        Scanner scanner = new Scanner(""); // Boş input için
//        music = new Music(scanner, printStream);
//
//        // Test modunu aktifleştir
//        music.isTestMode = true;
//    }
//
//    /**
//     * @brief Her testten sonra çalıştırılır
//     */
//    @After
//    public void tearDown() throws Exception {
//        // Kaynakları temizle
//        printStream.close();
//        outputStream.close();
//    }
//
//    /**
//     * @brief Music constructor'ını test eder
//     */
//    @Test
//    public void testConstructor() {
//        // Assert
//        assertNotNull("Music nesnesi null olmamalı", music);
//        assertNotNull("Scanner null olmamalı", music.scanner);
//        assertNotNull("PrintStream null olmamalı", music.out);
//    }
//
//    /**
//     * @brief clearScreen metodunu test eder
//     */
//    @Test
//    public void testClearScreen() {
//        // Act
//        music.clearScreen();
//        String output = outputStream.toString();
//
//        // Assert
//        assertTrue("Ekran temizleme kaçış dizisi içermeli", output.contains("\033[H\033[2J"));
//    }
//
//    /**
//     * @brief showMessage metodunu test eder
//     */
//    @Test
//    public void testShowMessage() {
//        // Arrange
//        String testMessage = "Test Message";
//
//        // Act - success tipinde bir mesaj göster
//        music.showMessage(testMessage, "success");
//        String successOutput = outputStream.toString();
//        outputStream.reset();
//
//        // Act - error tipinde bir mesaj göster
//        music.showMessage(testMessage, "error");
//        String errorOutput = outputStream.toString();
//        outputStream.reset();
//
//        // Act - info tipinde bir mesaj göster
//        music.showMessage(testMessage, "info");
//        String infoOutput = outputStream.toString();
//
//        // Assert
//        assertTrue("Success mesajı yeşil renk kodu içermeli", successOutput.contains("\033[32m"));
//        assertTrue("Success mesajı test mesajını içermeli", successOutput.contains(testMessage));
//
//        assertTrue("Error mesajı kırmızı renk kodu içermeli", errorOutput.contains("\033[31m"));
//        assertTrue("Error mesajı test mesajını içermeli", errorOutput.contains(testMessage));
//
//        assertTrue("Info mesajı mavi renk kodu içermeli", infoOutput.contains("\033[36m"));
//        assertTrue("Info mesajı test mesajını içermeli", infoOutput.contains(testMessage));
//    }
//
//    /**
//     * @brief showTitle metodunu test eder
//     */
//    @Test
//    public void testShowTitle() {
//        // Arrange
//        String testTitle = "Test Title";
//
//        // Act
//        music.showTitle(testTitle);
//        String output = outputStream.toString();
//
//        // Assert
//        assertTrue("Çıktı başlık metnini içermeli", output.contains(testTitle));
//        assertTrue("Çıktı sınırlayıcı içermeli", output.contains("="));
//    }
//
//    /**
//     * @brief enterToContinue metodunu test eder
//     */
//    @Test
//    public void testEnterToContinue() {
//        // Act
//        boolean result = music.enterToContinue();
//        String output = outputStream.toString();
//
//        // Assert
//        assertTrue("enterToContinue true dönmeli", result);
//        assertTrue("Çıktı beklemek için mesaj içermeli", output.contains("Press enter to continue"));
//    }
//
//    /**
//     * @brief handleInputError metodunu test eder
//     */
//    @Test
//    public void testHandleInputError() {
//        // Act
//        music.handleInputError();
//        String output = outputStream.toString();
//
//        // Assert
//        assertTrue("Çıktı hata mesajı içermeli", output.contains("Invalid input"));
//    }
//
//    /**
//     * @brief getInput metodunu test eder - geçerli giriş
//     */
//    @Test
//    public void testGetInputValid() {
//        // Arrange - Girişi simüle et
//        String input = "5\n";
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
//        Scanner scanner = new Scanner(inputStream);
//        music.scanner = scanner;
//
//        // Act
//        int result = music.getInput();
//
//        // Assert
//        assertEquals("getInput doğru değeri okumalı", 5, result);
//    }
//
//    /**
//     * @brief getInput metodunu test eder - geçersiz giriş
//     */
//    @Test
//    public void testGetInputInvalid() {
//        // Arrange - Geçersiz girişi simüle et
//        String input = "abc\n";
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
//        Scanner scanner = new Scanner(inputStream);
//        music.scanner = scanner;
//
//        // Act
//        int result = music.getInput();
//        String output = outputStream.toString();
//
//        // Assert
//        assertEquals("getInput hata durumunda -2 dönmeli", -2, result);
//        assertTrue("Çıktı hata mesajı içermeli", output.contains("Invalid input"));
//    }
//
//    /**
//     * @brief Çeşitli menü görüntüleme metotlarını test eder
//     */
//    @Test
//    public void testPrintMenus() {
//        // Act & Assert - Ana menüyü kontrol et
//        music.printOpeningScreen();
//        String openingScreen = outputStream.toString();
//        assertTrue("Açılış menüsü başlık içermeli", openingScreen.contains("MAIN MENU"));
//        assertTrue("Açılış menüsü login seçeneği içermeli", openingScreen.contains("1. Login"));
//        outputStream.reset();
//
//        // Main menu
//        music.printMainMenu();
//        String mainMenu = outputStream.toString();
//        assertTrue("Ana menü müzik kitaplığı başlığı içermeli", mainMenu.contains("MUSIC LIBRARY"));
//        assertTrue("Ana menü müzik koleksiyonu seçeneği içermeli", mainMenu.contains("1. Music Collection"));
//        outputStream.reset();
//
//        // Music collection menu
//        music.printMusicCollectionMenu();
//        String musicMenu = outputStream.toString();
//        assertTrue("Müzik koleksiyonu menüsü başlık içermeli", musicMenu.contains("MUSIC COLLECTION MENU"));
//        assertTrue("Müzik koleksiyonu menüsü şarkı ekleme seçeneği içermeli", musicMenu.contains("1. Add Song"));
//        outputStream.reset();
//
//        // Playlists menu
//        music.printPlayistsMenu();
//        String playlistMenu = outputStream.toString();
//        assertTrue("Çalma listesi menüsü başlık içermeli", playlistMenu.contains("PLAYLISTS MENU"));
//        assertTrue("Çalma listesi menüsü liste oluşturma seçeneği içermeli", playlistMenu.contains("1. Create Playlist"));
//        outputStream.reset();
//
//        // Metadata menu
//        music.printEditMetadataMenu();
//        String metadataMenu = outputStream.toString();
//        assertTrue("Metadata düzenleme menüsü başlık içermeli", metadataMenu.contains("EDIT METADATA MENU"));
//        assertTrue("Metadata düzenleme menüsü sanatçı düzenleme seçeneği içermeli", metadataMenu.contains("1. Edit Artist"));
//        outputStream.reset();
//
//        // Recommendations menu
//        music.printRecommendationsMenu();
//        String recommendMenu = outputStream.toString();
//        assertTrue("Öneriler menüsü başlık içermeli", recommendMenu.contains("RECOMMENDATIONS MENU"));
//        assertTrue("Öneriler menüsü şarkı önerisi seçeneği içermeli", recommendMenu.contains("1. Get Song Recommendations"));
//    }
//
//    /**
//     * @brief mainMenu metodunu test eder
//     * Not: Gerçek giriş/çıkış ve veritabanı işlemleri nedeniyle kısmi test
//     */
//    @Test
//    public void testMainMenuInitialization() {
//        try {
//            // Burada gerçek davranışı tamamen test etmek zor olduğundan
//            // sadece metodun hata fırlatmadan çalıştığını kontrol ediyoruz
//            // ve çıktıdaki beklenen metinleri arıyoruz
//
//            // Arrange - Çıkış seçeneğini simüle et
//            String input = "3\n"; // Exit option
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
//            Scanner scanner = new Scanner(inputStream);
//            music.scanner = scanner;
//
//            // Act
//            music.mainMenu("test-library.dat");
//            String output = outputStream.toString();
//
//            // Assert - Temel olarak hata olmadan çalıştığını ve bazı beklenen çıktıları kontrol et
//            assertTrue("Menü görüntülenmeli", output.contains("MAIN MENU"));
//        } catch (Exception e) {
//            fail("mainMenu metodu exception fırlatmamalı: " + e.getMessage());
//        }
//    }
//}