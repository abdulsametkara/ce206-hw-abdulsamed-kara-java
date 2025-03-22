package com.samet.music.main;
import com.samet.music.util.DatabaseUtil;

import java.util.Scanner;


public class MusicApp {

  public static void main(String[] args) {
    // Veritabanını sıfırlamak istiyorsanız bu değeri true yapın
    DatabaseUtil.setShouldResetDatabase(false);

    // Create scanner for user input
    Scanner inputScanner = new Scanner(System.in);

    // Veritabanını başlat
    DatabaseUtil.initializeDatabase();

    // Create an instance of the Music class
    Music music = new Music(inputScanner, System.out);

    // Define path for saving user data
    String userDataPath = "user_data.txt";

    // Start the application
    music.mainMenu(userDataPath);

    // Final message
    System.out.println("Thank you for using Music Library. Goodbye!");

    // Close scanner
    inputScanner.close();
  }
}
