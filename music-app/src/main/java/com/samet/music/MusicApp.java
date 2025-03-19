package com.samet.music;
import java.util.Scanner;


public class MusicApp {
  
  public static void main(String[] args) {

    /** @brief Scanner for user input. */
    Scanner inputScanner = new Scanner(System.in);

    /** @brief Create an instance of the Task class. */
    Music music = new Music(inputScanner, System.out);

    // Define a better path for saving data
    String userDataPath = "user_data.txt";

    // Start application
    music.mainMenu(userDataPath);


    System.out.println("Thank you for using Music Library. Goodbye!");
  
  }

}
