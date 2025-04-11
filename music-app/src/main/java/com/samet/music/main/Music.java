package com.samet.music.main;
import com.samet.music.dao.UserDAO;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.service.MusicRecommendationSystem;
import com.samet.music.ui.MetadataEditingUI;
import com.samet.music.ui.MusicCollectionUI;
import com.samet.music.ui.PlaylistUI;
import com.samet.music.ui.RecommendationUI;
import com.samet.music.util.DatabaseManager;
import com.samet.music.db.DatabaseConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

import java.io.PrintStream;

public class Music {


    /** Scanner object for user input. */
    public Scanner scanner;
    /** PrintStream object for output. */
    public PrintStream out;
    private boolean isLoggedIn = false;
    private String currentUser = null;
    private Map<String, String> userCredentials = new HashMap<>(); // Kullanıcı adı -> şifre eşlemesi
    private MusicCollectionUI musicCollectionUI;
    private MusicCollectionService musicService;
    private static final String DATA_DIR = "data/";
    private PlaylistUI playlistUI;
    private MetadataEditingUI metadataEditingUI;
    private RecommendationUI recommendationUI;
    private MusicRecommendationSystem recommendationSystem;
    private UserDAO userDAO;

    /**
     * @brief Test mode flag.
     *
     * Used to control specific behaviors during testing scenarios.
     */
    public boolean isTestMode = false;

    public Music(Scanner inputScanner, PrintStream out) {
        this.scanner = inputScanner;
        this.out = out;

        // Initialize service and UI components
        this.musicService = MusicCollectionService.getInstance();
        this.musicCollectionUI = new MusicCollectionUI(inputScanner, out);
        this.playlistUI = new PlaylistUI(musicService, inputScanner);
        this.metadataEditingUI = new MetadataEditingUI(inputScanner, out);
        this.recommendationUI = new RecommendationUI(inputScanner, out);
        this.recommendationSystem = MusicRecommendationSystem.getInstance();

        // Initialize user DAO
        this.userDAO = new UserDAO(new DatabaseConnection());
        this.userDAO.createTable();  // Kullanıcı tablosunu oluştur veya kontrol et

        // Create data directory if it doesn't exist
        createDataDirectory();
    }
    private void createDataDirectory() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void clearScreen() {
        out.print("\033[H\033[2J");
        out.flush();
    }

    /**
     * Displays a stylish message box
     * @param message The message to display
     * @param type The type of message (success, error, info)
     */
    public void showMessage(String message, String type) {
        String color;
        String prefix;

        if (type.equalsIgnoreCase("success")) {
            color = "\033[32m"; // Green
            prefix = "✓ ";
        } else if (type.equalsIgnoreCase("error")) {
            color = "\033[31m"; // Red
            prefix = "✗ ";
        } else {
            color = "\033[36m"; // Cyan
            prefix = "ℹ ";
        }

        String reset = "\033[0m";

        int width = message.length() + 6;
        String border = "+" + "=".repeat(width) + "+";
        String empty = "|" + " ".repeat(width) + "|";

        out.println();
        out.println(color + border + reset);
        out.println(color + empty + reset);
        out.println(color + "|   " + prefix + message + "   |" + reset);
        out.println(color + empty + reset);
        out.println(color + border + reset);
        out.println();
    }

    /**
     * Displays a stylish title for a screen
     * @param title The title to display
     */
    public void showTitle(String title) {
        int width = title.length() + 4;
        String border = "=" + "=".repeat(width) + "=";

        out.println("\n" + border);
        out.println("| " + title + " |");
        out.println(border + "\n");
    }

    public int getInput() {
        try {
            int input = scanner.nextInt();
            scanner.nextLine(); // Satır sonunu temizle
            return input;
        } catch (Exception e) {
            scanner.nextLine(); // Hatalı girişleri temizle
            handleInputError(); // Hata mesajını yazdır
            return -2; // Hata durumunda -2 döner
        }
    }

    /**
     * Displays error message for invalid input
     */
    public void handleInputError() {
        clearScreen();
        out.println("Invalid input. Please enter a number.");
    }

    public  boolean enterToContinue() {
        out.println("Press enter to continue...");
        if (!isTestMode) {
            scanner.nextLine();
        }
        return true;
    }

    public void printOpeningScreen() {
        clearScreen();
        out.println("=============== MAIN MENU ===============");
        out.println("1. Login");
        out.println("2. Register");
        out.println("3. Exit Program");
        out.println("=========================================");
        out.print("Please enter a number: ");
    }

    public void printMainMenu() {
        clearScreen();
        out.println("========================================");
        out.println("      MAIN MENU - MUSIC LIBRARY        ");
        out.println("========================================");
        if (currentUser != null) {
            out.println("Logged in as: " + currentUser);
            out.println("========================================");
        }
        out.println("1. Music Collection");
        out.println("2. Playlists");
        out.println("3. Metadata Editing");
        out.println("4. Recommendations");
        out.println("5. Logout");
        out.println("========================================");
        out.print("Please enter your choice: ");
    }

    public void printMusicCollectionMenu() {
        clearScreen();
        out.println("========================================");
        out.println("         MUSIC COLLECTION MENU            ");
        out.println("========================================");
        out.println("1. Add Song");
        out.println("2. Add Album");
        out.println("3. Add Artist");
        out.println("4. View Songs");
        out.println("5. View Albums");
        out.println("6. View Artists");
        out.println("7. Delete Song");
        out.println("8. Delete Albums");
        out.println("9. Delete Artist");
        out.println("10. Add Song to Album"); // Yeni seçenek
        out.println("0. Back to Main Menu");
        out.println("========================================");
        out.print("Please enter your choice: ");
    }

    public void printPlayistsMenu() {
        clearScreen();
        out.println("========================================");
        out.println("         PLAYLISTS MENU                ");
        out.println("========================================");
        out.println("1. Create Playlist");
        out.println("2. View Playlists");
        out.println("3. Edit Playlist");
        out.println("0. Back to Main Menu");
        out.println("========================================");
        out.print("Please enter your choice: ");
    }

    public void printEditMetadataMenu() {
        clearScreen();
        out.println("========================================");
        out.println("         EDIT METADATA MENU            ");
        out.println("========================================");
        out.println("1. Edit Artist");
        out.println("2. Edit Album");
        out.println("3. Edit Song Genre");
        out.println("0. Back to Main Menu");
        out.println("========================================");
        out.print("Please enter your choice: ");
    }

    public void printRecommendationsMenu() {
        clearScreen();
        out.println("========================================");
        out.println("         RECOMMENDATIONS MENU          ");
        out.println("========================================");
        out.println("1. Get Song Recommendations");
        out.println("2. Get Album Recommendations");
        out.println("3. Get Artist Recommendations");
        out.println("0. Back to Main Menu");
        out.println("========================================");
        out.print("Please enter your choice: ");
    }



    private boolean displayOpeningScreen() {
        int choice;
        while (true) {
            printOpeningScreen();
            choice = getInput();

            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }

            switch (choice) {
                case 1: // Login
                    if (loginUser()) {
                        return true;
                    }
                    break;
                case 2: // Register
                    registerUser();
                    break;
                case 3: // Exit
                    return false;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    private boolean loginUser() {
        clearScreen();
        Scanner in = new Scanner(System.in);
        out.println("========== LOGIN ==========");
        out.print("Username: ");
        String username = in.nextLine().trim();
        out.print("Password: ");
        String password = in.nextLine().trim();

        // Kullanıcı adı ve şifre doğrulaması
        if (username.isEmpty() || password.isEmpty()) {
            clearScreen();
            out.println("Login failed. Username and password cannot be empty.");
            enterToContinue();
            return false;
        }

        // Kullanıcı adının kayıtlı olup olmadığını kontrol et
        String savedPassword = userDAO.getPassword(username);
        if (savedPassword == null) {
            clearScreen();
            out.println("Login failed. User not found.");
            enterToContinue();
            return false;
        }

        // Şifre kontrolü
        if (!savedPassword.equals(password)) {
            clearScreen();
            out.println("Login failed. Incorrect password.");
            enterToContinue();
            return false;
        }

        // Başarılı giriş
        clearScreen();
        out.println("Login successful! Welcome, " + username);
        isLoggedIn = true;
        currentUser = username;
        enterToContinue();
        return true;
    }


    private void registerUser() {
        clearScreen();
        Scanner in = new Scanner(System.in);
        out.println("========== REGISTER ==========");
        out.print("Enter new username: ");
        String username = in.nextLine().trim();
        out.print("Enter new password: ");
        String password = in.nextLine().trim();
        out.print("Confirm password: ");
        String confirmPass = in.nextLine().trim();

        // Temel doğrulamalar
        if (username.isEmpty()) {
            clearScreen();
            out.println("Registration failed. Username cannot be empty.");
            enterToContinue();
            return;
        }

        if (password.isEmpty()) {
            clearScreen();
            out.println("Registration failed. Password cannot be empty.");
            enterToContinue();
            return;
        }

        if (!password.equals(confirmPass)) {
            clearScreen();
            out.println("Registration failed. Passwords do not match.");
            enterToContinue();
            return;
        }

        if (userDAO.userExists(username)) {
            clearScreen();
            out.println("Registration failed. Username already exists.");
            enterToContinue();
            return;
        }

        // Kullanıcıyı veritabanına kaydet
        boolean success = userDAO.saveUser(username, password);

        if (success) {
            clearScreen();
            out.println("Registration successful! You can now login.");
        } else {
            clearScreen();
            out.println("Registration failed due to database error. Please try again.");
        }
        enterToContinue();
    }

    public void userOptionsMenu() {
        int choice;
        while (true) {
            printMainMenu();
            choice = getInput();
            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }
            switch (choice) {
                case 1:
                    musicCollectionMenu();
                    break;
                case 2:
                    playlistsMenu();
                    break;
                case 3:
                    editMetadataMenu();
                    break;
                case 4:
                    recommendationsMenu();
                    break;
                case 5:
                    logout();
                    return;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    public void musicCollectionMenu() {
        int choice;

        while (true) {
            printMusicCollectionMenu();
            choice = getInput();

            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }

            switch (choice) {
                case 0: // Return to main menu
                    return;
                case 1: // Add Song
                    musicCollectionUI.addSong();
                    enterToContinue();
                    break;
                case 2: // Add Album
                    musicCollectionUI.addAlbum();
                    enterToContinue();
                    break;
                case 3: // Add Artist
                    musicCollectionUI.addArtist();
                    enterToContinue();
                    break;
                case 4: // View Songs
                    musicCollectionUI.viewSongs();
                    enterToContinue();
                    break;
                case 5: // View Albums
                    musicCollectionUI.viewAlbums();
                    enterToContinue();
                    break;
                case 6: // View Artists
                    musicCollectionUI.viewArtists();
                    enterToContinue();
                    break;
                case 7: // Delete Song
                    musicCollectionUI.deleteSong();
                    enterToContinue();
                    break;
                case 8: // Delete Album
                    musicCollectionUI.deleteAlbum();
                    enterToContinue();
                    break;
                case 9: // Delete Artist
                    musicCollectionUI.deleteArtist();
                    enterToContinue();
                    break;
                case 10: // Add Song to Album
                    musicCollectionUI.addSongToAlbumMenu();
                    enterToContinue();
                    break;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    // PlaylistsMenu metodunu güncelleyin
    public void playlistsMenu() {
        int choice;

        while (true) {
            printPlayistsMenu();
            choice = getInput();

            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }

            switch (choice) {
                case 0: // Return to main menu
                    return;
                case 1: // Create Playlist
                    playlistUI.createPlaylist();
                    enterToContinue();
                    break;
                case 2: // View Playlists
                    playlistUI.viewPlaylists();
                    enterToContinue();
                    break;
                case 3: // Edit Playlist
                    playlistUI.editPlaylist();
                    enterToContinue();
                    break;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    public void editMetadataMenu() {
        int choice;

        while (true) {
            printEditMetadataMenu();
            choice = getInput();

            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }

            switch (choice) {
                case 0: // Return to main menu
                    return;
                case 1: // Edit Artist
                    metadataEditingUI.editArtist();
                    enterToContinue();
                    break;
                case 2: // Edit Album
                    metadataEditingUI.editAlbum();
                    enterToContinue();
                    break;
                case 3: // Edit Song Genre
                    metadataEditingUI.editSongGenre();
                    enterToContinue();
                    break;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    public void recommendationsMenu() {
        int choice;

        while (true) {
            printRecommendationsMenu();
            choice = getInput();

            if (choice == -2) {
                handleInputError();
                enterToContinue();
                continue;
            }

            switch (choice) {
                case 0: // Return to main menu
                    return;
                case 1: // Get Song Recommendations
                    recommendationUI.showSongRecommendationsByGenre(currentUser);
                    enterToContinue();
                    break;
                case 2: // Get Album Recommendations
                    recommendationUI.showAlbumRecommendations(currentUser);
                    enterToContinue();
                    break;
                case 3: // Get Artist Recommendations
                    recommendationUI.showArtistRecommendations(currentUser);
                    enterToContinue();
                    break;
                default:
                    clearScreen();
                    out.println("Invalid choice. Please try again.");
                    enterToContinue();
                    break;
            }
        }
    }

    /**
     * Logs out the current user
     */
    private void logout() {
        clearScreen();
        out.println("Logging out...");
        isLoggedIn = false;
        currentUser = null;
        enterToContinue();
    }

    public void mainMenu(String libraryFilePath) {
        try {
            DatabaseManager.getInstance().initializeDatabase();
        } catch (Exception e) {
            out.println("Error initializing database: " + e.getMessage());
        }

        // Load music collection data
        String artistFile = DATA_DIR + "artists.dat";
        String albumFile = DATA_DIR + "albums.dat";
        String songFile = DATA_DIR + "songs.dat";
        String playlistFile = DATA_DIR + "playlists.dat";
        String recommendationFile = DATA_DIR + "recommendations.dat";

        musicService.loadData(artistFile, albumFile, songFile, playlistFile);
        recommendationSystem.loadRecommendationData(recommendationFile);

        // Display login/register menu first
        if (displayOpeningScreen()) {
            // Only show main menu if login is successful
            userOptionsMenu();
        }

        // Save data before exiting
        musicService.saveData(artistFile, albumFile, songFile, playlistFile);
        recommendationSystem.saveRecommendationData(recommendationFile);
    }

    private void loadLibraryData(String filePath) {
        File userFile = new File(filePath);

        // Eğer dosya yoksa, varsayılan kullanıcıları ekle
        if (!userFile.exists()) {
            userCredentials.put("admin", "admin123");
            userCredentials.put("user", "password");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
            out.println("User data loaded successfully.");
        } catch (IOException e) {
            out.println("Error loading user data: " + e.getMessage());
            // Hata durumunda varsayılan kullanıcıları ekle
            userCredentials.put("admin", "admin123");
            userCredentials.put("user", "password");
        }
    }

    /**
     * Saves library data to file
     *
     * @param filePath Path to the library data file
     */
    private void saveLibraryData(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
            out.println("User data saved successfully.");
        } catch (IOException e) {
            out.println("Error saving user data: " + e.getMessage());
        }
    }


}