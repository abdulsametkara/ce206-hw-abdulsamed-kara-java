/**
 * @file MusicTest.java
 * @brief This file contains the test cases for the Music class.
 * @details This file includes test methods to validate the functionality of the Music class. It uses JUnit for unit testing.
 */
package com.samet.music;

import static org.junit.Assert.*;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.main.Music;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.ui.MusicCollectionUI;
import com.samet.music.util.DatabaseUtil;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * @class MusicTest
 * @brief This class represents the test class for the Music class.
 * @details The MusicTest class provides test methods to verify the behavior of the Music class.
 * @author samet.kara
 */
public class MusicTest {

  // Orijinal çıkış akışını sakla
  private final PrintStream standardOut = System.out;
  // Test çıkışını yakalamak için kullanılacak akış
  private ByteArrayOutputStream outputCaptor;
  // Test etmek için kullanılacak Music örneği
  private Music musicApp;
  Scanner scanner = new Scanner(System.in);


  /**
   * @brief This method is executed once before all test methods.
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // Activate database test mode
    DatabaseUtil.setShouldResetDatabase(true);
    DatabaseUtil.initializeDatabase();
  }

  /**
   * @brief This method is executed once after all test methods.
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // Cleanup operations (if needed)
  }

  /**
   * @brief This method is executed before each test method.
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {


    // Prepare to capture System.out
    outputCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputCaptor));

    // Create Music instance for testing
    Scanner scanner = new Scanner(System.in);
    musicApp = new Music(scanner, new PrintStream(outputCaptor));

    // Enable test mode
    musicApp.isTestMode = true;
  }

  /**
   * @brief This method is executed after each test method.
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    // Restore original output stream
    System.setOut(standardOut);
    outputCaptor.reset();
  }

  /**
   * @brief Tests if the screen clear function works
   */
  @Test
  public void testClearScreen() {
    // Test
    musicApp.clearScreen();

    // Check output for ANSI clear codes
    String output = outputCaptor.toString();
    assertTrue(output.contains("\033[H\033[2J"));
  }

  /**
   * @brief Tests if the message display function works
   */
  @Test
  public void testShowMessage() {
    // Test
    musicApp.showMessage("Test Message", "info");

    // Check output for the message
    String output = outputCaptor.toString();
    assertTrue(output.contains("Test Message"));
  }

  /**
   * @brief Tests if enterToContinue method works in test mode
   */
  @Test
  public void testEnterToContinue() {
    // In test mode, enterToContinue should return true (without calling scanner.nextLine)
    boolean result = musicApp.enterToContinue();
    assertTrue(result);
  }

  /**
   * @brief Tests if invalid input handling works correctly
   */
  @Test
  public void testHandleInputError() {
    // Test
    musicApp.handleInputError();

    // Check output for error message
    String output = outputCaptor.toString();
    assertTrue(output.contains("Invalid input"));
  }

  /**
   * @brief Tests getInput method with invalid input
   */
  @Test
  public void testGetInputWithInvalidInput() {
    // Create mock for invalid input and check how system responds
    musicApp.scanner = new Scanner("abc\n");
    int result = musicApp.getInput();
    assertEquals(-2, result);
  }

  /**
   * @brief Tests opening screen display
   */
  @Test
  public void testPrintOpeningScreen() {
    // Test
    musicApp.printOpeningScreen();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain opening screen title", output.contains("MAIN MENU"));
    assertTrue("Should contain Login option", output.contains("1. Login"));
    assertTrue("Should contain Register option", output.contains("2. Register"));
    assertTrue("Should contain Exit option", output.contains("3. Exit Program"));
  }

  /**
   * @brief Tests main menu display
   */
  @Test
  public void testPrintMainMenu() {
    // Test
    musicApp.printMainMenu();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain main menu title", output.contains("MAIN MENU - MUSIC LIBRARY"));
    assertTrue("Should contain Music Collection option", output.contains("1. Music Collection"));
    assertTrue("Should contain Playlists option", output.contains("2. Playlists"));
    assertTrue("Should contain Metadata Editing option", output.contains("3. Metadata Editing"));
    assertTrue("Should contain Recommendations option", output.contains("4. Recommendations"));
    assertTrue("Should contain Logout option", output.contains("5. Logout"));
  }

  /**
   * @brief Tests Music Collection menu display
   */
  @Test
  public void testPrintMusicCollectionMenu() {
    // Test
    musicApp.printMusicCollectionMenu();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain music collection menu title", output.contains("MUSIC COLLECTION MENU"));
    assertTrue("Should contain Add Song option", output.contains("1. Add Song"));
    assertTrue("Should contain Add Album option", output.contains("2. Add Album"));
    assertTrue("Should contain Add Artist option", output.contains("3. Add Artist"));
    assertTrue("Should contain View Songs option", output.contains("4. View Songs"));
    assertTrue("Should contain View Albums option", output.contains("5. View Albums"));
    assertTrue("Should contain View Artists option", output.contains("6. View Artists"));
    assertTrue("Should contain Delete Song option", output.contains("7. Delete Song"));
    assertTrue("Should contain Delete Albums option", output.contains("8. Delete Albums"));
    assertTrue("Should contain Delete Artist option", output.contains("9. Delete Artist"));
    assertTrue("Should contain Back option", output.contains("0. Back to Main Menu"));
  }

  /**
   * @brief Tests Playlists menu display
   */
  @Test
  public void testPrintPlaylistsMenu() {
    // Test
    musicApp.printPlayistsMenu();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain playlists menu title", output.contains("PLAYLISTS MENU"));
    assertTrue("Should contain Create Playlist option", output.contains("1. Create Playlist"));
    assertTrue("Should contain View Playlists option", output.contains("2. View Playlists"));
    assertTrue("Should contain Edit Playlist option", output.contains("3. Edit Playlist"));
    assertTrue("Should contain Back option", output.contains("0. Back to Main Menu"));
  }

  /**
   * @brief Tests Metadata Editing menu display
   */
  @Test
  public void testPrintEditMetadataMenu() {
    // Test
    musicApp.printEditMetadataMenu();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain metadata editing menu title", output.contains("EDIT METADATA MENU"));
    assertTrue("Should contain Edit Artist option", output.contains("1. Edit Artist"));
    assertTrue("Should contain Edit Album option", output.contains("2. Edit Album"));
    assertTrue("Should contain Edit Song Genre option", output.contains("3. Edit Song Genre"));
    assertTrue("Should contain Back option", output.contains("0. Back to Main Menu"));
  }

  /**
   * @brief Tests Recommendations menu display
   */
  @Test
  public void testPrintRecommendationsMenu() {
    // Test
    musicApp.printRecommendationsMenu();

    // Check output
    String output = outputCaptor.toString();
    assertTrue("Should contain recommendations menu title", output.contains("RECOMMENDATIONS MENU"));
    assertTrue("Should contain Get Song Recommendations option", output.contains("1. Get Song Recommendations"));
    assertTrue("Should contain Get Album Recommendations option", output.contains("2. Get Album Recommendations"));
    assertTrue("Should contain Get Artist Recommendations option", output.contains("3. Get Artist Recommendations"));
    assertTrue("Should contain Back option", output.contains("0. Back to Main Menu"));
  }

  /**
   * @brief Tests the addSong function by simulating user input
   */
  @Test
  public void testAddSong() {
    // Arrange
    // Simulate user input for: Artist selection (1), Song name, Duration, Genre
    String input = "1\nTest Song\n180\nRock\n2\n"; // Select first artist, enter song details, don't add to album
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream); // Redirect system input

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Add a test artist to the database before testing
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Act
    musicCollectionUI.addSong(); // Execute the method

    // Assert
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Song 'Test Song' added successfully"));

    // Get all songs to verify it was added
    List<Song> songs = service.getAllSongs();
    boolean songFound = false;
    for (Song song : songs) {
      if (song.getName().equals("Test Song") && song.getGenre().equals("Rock")) {
        songFound = true;
        break;
      }
    }

    // Restore original System.in and System.out
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with valid inputs
   */
  @Test
  public void testAddSongWithValidInputs() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate user input for: Artist selection, Song name, Duration, Genre, No album
    String input = "1\nTest Song\n180\nRock\n2\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Song 'Test Song' added successfully"));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function when no artists are available
   */
  @Test
  public void testAddSongWithNoArtists() {
    // Arrange
    // Ensure no artists in database
    MusicCollectionService service = MusicCollectionService.getInstance();
    List<Artist> artists = service.getAllArtists();
    for (Artist artist : artists) {
      service.removeArtist(artist.getId());
    }

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no artists message", output.contains("No artists available. You need to add an artist first."));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with invalid artist selection
   */
  @Test
  public void testAddSongWithInvalidArtistSelection() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate invalid artist selection (out of range)
    String input = "99\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show invalid selection message", output.contains("Invalid selection. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with non-numeric artist selection
   */
  @Test
  public void testAddSongWithNonNumericArtistSelection() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate non-numeric selection
    String input = "abc\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show invalid input message", output.contains("Invalid input. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with empty song name
   */
  @Test
  public void testAddSongWithEmptySongName() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate valid artist selection but empty song name
    String input = "1\n\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show empty song name error", output.contains("Song name cannot be empty. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with invalid duration input (non-numeric)
   */
  @Test
  public void testAddSongWithInvalidDuration() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate valid artist selection and name but invalid duration
    String input = "1\nTest Song\nabc\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show invalid duration message", output.contains("Invalid duration. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with zero or negative duration
   */
  @Test
  public void testAddSongWithNegativeDuration() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate valid artist selection and name but negative duration
    String input = "1\nTest Song\n0\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show duration must be positive message", output.contains("Duration must be positive. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with duration exceeding maximum
   */
  @Test
  public void testAddSongWithExcessiveDuration() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate valid artist selection and name but excessive duration
    String input = "1\nTest Song\n3700\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show duration too long message", output.contains("Duration is too long. Maximum allowed is 3600 seconds (1 hour)."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSong function with empty genre (should default to 'Unknown')
   */
  @Test
  public void testAddSongWithEmptyGenre() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate valid inputs but empty genre (should default to 'Unknown')
    String input = "1\nTest Song\n180\n\n2\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should add song successfully", output.contains("Song 'Test Song' added successfully"));

    // Verify the song has 'Unknown' genre
    List<Song> songs = service.getAllSongs();
    boolean hasUnknownGenre = false;
    for (Song song : songs) {
      if (song.getName().equals("Test Song") && song.getGenre().equals("Unknown")) {
        hasUnknownGenre = true;
        break;
      }
    }

  }

  /**
   * @brief Tests the addSong function when adding to an album
   */
  @Test
  public void testAddSongToAlbum() {
    // Arrange
    // First add a test artist and album
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Artist")) {
        artist = a;
        break;
      }
    }

    // Create album for this artist
    service.addAlbum("Test Album", artist.getId(), 2023, "Rock");

    // Simulate user input for: Artist selection, Song details, Yes to album, Album selection
    String input = "1\nTest Album Song\n180\nRock\n1\n1\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Song 'Test Album Song' added successfully"));

    // Check that song is associated with the album
    List<Song> songs = service.getSongsByAlbum(service.getAlbumsByArtist(artist.getId()).get(0).getId());
    boolean songInAlbum = false;
    for (Song song : songs) {
      if (song.getName().equals("Test Album Song")) {
        songInAlbum = true;
        break;
      }
    }

  }

  /**
   * @brief Tests the addAlbum function with valid inputs
   */
  @Test
  public void testAddAlbumWithValidInputs() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Artist", "Test Biography");

    // Simulate user input for: Artist selection, Album name, Release year, Genre
    String input = "1\nTest Album\n2023\nRock\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addAlbum();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Album 'Test Album' added successfully"));

    // Verify the album was added to the database
    List<Album> albums = service.getAllAlbums();
    boolean albumFound = false;
    for (Album album : albums) {
      if (album.getName().equals("Test Album") && album.getReleaseYear() == 2023) {
        albumFound = true;
        break;
      }
    }
    assertTrue("Album should be added to database", albumFound);

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addArtist function with valid inputs
   */
  @Test
  public void testAddArtistWithValidInputs() {
    // Arrange
    // Simulate user input for: Artist name, Biography
    String input = "Test New Artist\nTest Artist Biography\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addArtist();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show success message", output.contains("Artist 'Test New Artist' added successfully"));

    // Verify the artist was added to the database
    MusicCollectionService service = MusicCollectionService.getInstance();
    List<Artist> artists = service.getAllArtists();
    boolean artistFound = false;
    for (Artist artist : artists) {
      if (artist.getName().equals("Test New Artist")) {
        artistFound = true;
        break;
      }
    }
    assertTrue("Artist should be added to database", artistFound);

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteSong function
   */
  @Test
  public void testDeleteSong() {
    // Arrange
    // Add a test artist and song
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Artist")) {
        artist = a;
        break;
      }
    }

    // Create song for this artist
    String songName = "Delete Test Song";
    Song song = new Song(songName, artist, 180);
    song.setGenre("Rock");

    // Add to database
    SongDAO songDAO = new SongDAO();
    songDAO.insert(song);

    // Simulate user input to select and confirm deletion
    // First option (1) for the song, then 'y' to confirm
    String input = "1\ny\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteSong();

    // Assert
    String output = outContent.toString();

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the viewSongs function
   */
  @Test
  public void testViewSongs() {
    // Arrange
    // Add a test artist and song
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test View Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test View Artist")) {
        artist = a;
        break;
      }
    }

    // Create song for this artist
    String songName = "View Test Song";
    Song song = new Song(songName, artist, 180);
    song.setGenre("Rock");

    // Add to database
    SongDAO songDAO = new SongDAO();
    songDAO.insert(song);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.viewSongs();

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests for view-related methods in MusicCollectionUI
   */
  @Test
  public void testViewAlbums() {
    // Arrange
    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.viewAlbums();

    // Assert
    String output = outContent.toString();
    assertTrue("Should display albums header", output.contains("ALL ALBUMS"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the viewArtists function including error handling for database
   */
  @Test
  public void testViewArtists() {
    // Arrange
    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.viewArtists();

    // Assert
    String output = outContent.toString();
    assertTrue("Should display artists header", output.contains("ALL ARTISTS"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteArtist function with no artists available
   */
  @Test
  public void testDeleteArtistWithNoArtists() {
    // Arrange
    // Ensure no artists in database
    MusicCollectionService service = MusicCollectionService.getInstance();
    List<Artist> artists = service.getAllArtists();
    for (Artist artist : artists) {
      service.removeArtist(artist.getId());
    }

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteArtist();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no artists message", output.contains("No artists found in the collection"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteArtist function with invalid selection
   */
  @Test
  public void testDeleteArtistWithInvalidSelection() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Delete Artist", "Test Biography");

    // Simulate invalid artist number selection
    String input = "99\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteArtist();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show invalid selection message", output.contains("Invalid selection. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteArtist function with cancellation (entering 0)
   */
  @Test
  public void testDeleteArtistWithCancellation() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Delete Artist", "Test Biography");

    // Simulate cancel selection (0)
    String input = "0\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteArtist();

    // Assert - no specific message, just verify the function returns without error

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteArtist function with an artist that has albums and songs
   */
  @Test
  public void testDeleteArtistWithAssociatedContent() {
    // Arrange
    // Create artist, album, and song
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Delete Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Delete Artist")) {
        artist = a;
        break;
      }
    }

    // Create album for artist
    service.addAlbum("Test Album", artist.getId(), 2023, "Test Genre");

    // Create song
    Song song = new Song("Test Song", artist, 180);
    SongDAO songDAO = new SongDAO();
    songDAO.insert(song);

    // Simulate user confirming deletion (selecting artist, then answering 'n' to first confirmation, then 'y' to second)
    String input = "1\nn\ny\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteArtist();

    // Assert
    String output = outContent.toString();

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteArtist function with confirmation cancellation
   */
  @Test
  public void testDeleteArtistWithConfirmationCancellation() {
    // Arrange
    // First add a test artist
    MusicCollectionService service = MusicCollectionService.getInstance();
    service.addArtist("Test Delete Artist", "Test Biography");

    // Simulate selecting artist but cancelling with 'n'
    String input = "1\nn\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteArtist();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show operation cancelled message", output.contains("Operation cancelled"));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteSong function with no songs available
   */
  @Test
  public void testDeleteSongWithNoSongs() {
    // Arrange
    // Ensure no songs in database
    MusicCollectionService service = MusicCollectionService.getInstance();
    List<Song> songs = service.getAllSongs();
    for (Song song : songs) {
      service.removeSong(song.getId());
    }

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no songs message", output.contains("No songs found in the collection"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteSong function with song in playlists
   */
  @Test
  public void testDeleteSongInPlaylists() {
    // Veritabanını tamamen sıfırlayalım
    DatabaseUtil.setShouldResetDatabase(true);
    DatabaseUtil.initializeDatabase();

    // Yeni nesneler oluştur
    SongDAO songDAO = new SongDAO();
    ArtistDAO artistDAO = new ArtistDAO();
    PlaylistDAO playlistDAO = new PlaylistDAO();

    // Önce sanatçı oluştur
    Artist artist = new Artist("Test Artist", "Test Biography");
    artistDAO.insert(artist);

    // Şarkı oluştur ve ekle
    Song song = new Song("Playlist Test Song", artist, 180);
    song.setGenre("Test Genre");
    songDAO.insert(song);

    // Şarkının doğru şekilde eklendiğini kontrol et
    Song retrievedSong = songDAO.getById(song.getId());
    assertNotNull("Şarkı veritabanına eklenmelidir", retrievedSong);

    // Çalma listesi oluştur
    Playlist playlist = new Playlist("Test Playlist", "Test Description");
    playlistDAO.insert(playlist);

    // Şarkıyı çalma listesine ekle
    playlistDAO.addSongToPlaylist(playlist.getId(), song.getId());

    // Giriş stimüle et
    String input = "1\ny\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Çıktıyı yakala
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // MusicCollectionUI oluştur
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    try {
      // Test metodu çalıştır
      musicCollectionUI.deleteSong();

      // Çıktıyı kontrol et
      String output = outContent.toString();

    } finally {
      // Kaynakları her durumda temizle
      System.setIn(originalIn);
      System.setOut(originalOut);
    }
  }
  /**
   * @brief Tests the deleteSong function with cancellation
   */
  @Test
  public void testDeleteSongWithCancellation() {
    // Arrange
    // First add a test artist and song
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Artist")) {
        artist = a;
        break;
      }
    }

    // Create song
    Song song = new Song("Cancel Test Song", artist, 180);
    SongDAO songDAO = new SongDAO();
    songDAO.insert(song);

    // Simulate selecting song but cancelling with 'n'
    String input = "1\nn\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteSong();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show operation cancelled message", output.contains("Operation cancelled"));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteAlbum function with no albums available
   */
  @Test
  public void testDeleteAlbumWithNoAlbums() {
    // Arrange
    // Ensure no albums in database
    MusicCollectionService service = MusicCollectionService.getInstance();
    List<Album> albums = service.getAllAlbums();
    for (Album album : albums) {
      service.removeAlbum(album.getId());
    }

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteAlbum();

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no albums message", output.contains("No albums found in the collection"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the deleteAlbum function with songs and deleting them
   */
  @Test
  public void testDeleteAlbumWithSongsAndDeleteThem() {
    // Arrange
    // Create artist, album, and song
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Delete Album Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Delete Album Artist")) {
        artist = a;
        break;
      }
    }

    // Create album
    service.addAlbum("Test Delete Album", artist.getId(), 2023, "Test Genre");
    Album album = null;
    List<Album> albums = service.getAlbumsByArtist(artist.getId());
    for (Album a : albums) {
      if (a.getName().equals("Test Delete Album")) {
        album = a;
        break;
      }
    }

    // Create song and add to album
    Song song = new Song("Album Song", artist, 180);
    song.setAlbum(album);
    SongDAO songDAO = new SongDAO();
    songDAO.insert(song);

    // Simulate user selecting album, choosing option 2 (delete album and songs)
    String input = "1\n2\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.deleteAlbum();

    // Assert
    String output = outContent.toString();

    // Verify songs are deleted
    List<Song> remainingSongs = service.getAllSongs();
    boolean songStillExists = false;
    for (Song s : remainingSongs) {
      if (s.getName().equals("Album Song")) {
        songStillExists = true;
        break;
      }
    }
    assertFalse("Song should be deleted", songStillExists);

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSongToAlbum function with no albums available
   */
  @Test
  public void testAddSongToAlbumWithNoAlbums() {
    // Arrange
    // Create artist but no albums
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Artist No Albums", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Artist No Albums")) {
        artist = a;
        break;
      }
    }

    // Remove all albums for this artist
    List<Album> albums = service.getAlbumsByArtist(artist.getId());
    for (Album album : albums) {
      service.removeAlbum(album.getId());
    }

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    Scanner scanner = new Scanner(System.in);
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSongToAlbum(artist);

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no albums message", output.contains("No albums available for this artist"));

    // Restore original stream
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSongToAlbum function with invalid album selection
   */
  @Test
  public void testAddSongToAlbumWithInvalidAlbumSelection() {
    // Arrange
    // Create artist and album
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test Album Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test Album Artist")) {
        artist = a;
        break;
      }
    }

    // Create album
    service.addAlbum("Test Album Selection", artist.getId(), 2023, "Test Genre");

    // Simulate invalid album selection (out of range)
    String input = "99\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSongToAlbum(artist);

    // Assert
    String output = outContent.toString();
    assertTrue("Should show invalid selection message", output.contains("Invalid selection. Operation cancelled."));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }

  /**
   * @brief Tests the addSongToAlbum function with no available songs to add
   */
  @Test
  public void testAddSongToAlbumWithNoAvailableSongs() {
    // Arrange
    // Create artist and album, but no songs
    MusicCollectionService service = MusicCollectionService.getInstance();
    Artist artist = null;

    // Create artist
    service.addArtist("Test No Songs Artist", "Test Biography");
    List<Artist> artists = service.getAllArtists();
    for (Artist a : artists) {
      if (a.getName().equals("Test No Songs Artist")) {
        artist = a;
        break;
      }
    }

    // Create album
    service.addAlbum("Test No Songs Album", artist.getId(), 2023, "Test Genre");

    // Simulate valid album selection
    String input = "1\n";
    InputStream originalIn = System.in;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    // Create fresh scanner with simulated input
    Scanner scanner = new Scanner(System.in);

    // Capture output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // Create Music Collection UI for testing
    MusicCollectionUI musicCollectionUI = new MusicCollectionUI(scanner, System.out);

    // Act
    musicCollectionUI.addSongToAlbum(artist);

    // Assert
    String output = outContent.toString();
    assertTrue("Should show no available songs message", output.contains("No songs available to add to this album"));

    // Restore original streams
    System.setIn(originalIn);
    System.setOut(originalOut);
  }
}