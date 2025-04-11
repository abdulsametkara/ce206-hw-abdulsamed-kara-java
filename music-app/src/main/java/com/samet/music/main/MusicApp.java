package com.samet.music.main;

import com.samet.music.service.MusicCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main application class
 */
public class MusicApp {
  private static final Logger logger = LoggerFactory.getLogger(MusicApp.class);

  /**
   * Application entry point
   */
  public static void main(String[] args) {
    logger.info("Starting music application...");

    try {
      // Initialize application
      MusicAppInitializer initializer = MusicAppInitializer.getInstance();
      initializer.initialize();

      // Create and run the Music UI with the library path
      Scanner scanner = new Scanner(System.in);
      Music music = new Music(scanner, System.out);

      // Run the main menu - NOTE: Calling mainMenu with actual library path
      music.mainMenu("data/user_credentials.txt");

      // Shutdown application
      initializer.shutdown();

      // Close resources
      scanner.close();

      logger.info("Application terminated successfully");
    } catch (Exception e) {
      logger.error("Application terminated with error: {}", e.getMessage(), e);
      System.exit(1);
    }
  }
}