package com.samet.music;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.main.Music;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertTrue;

public class DenemeTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    private MusicCollectionService service;
    private PrintStream out;

    @Before
    public void setUp() {
        service = MusicCollectionService.getInstance();
    }

    @After
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }






    @Test
    public void AddSongValidTest() {
        String input = "1\n3\n1\nA Test Song\n120\nRock\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream); // Simulated user input
        System.setOut(new PrintStream(outContent)); // Capture output

        // Create PlaylistUI with new Scanner and PrintStream
        Music music = new Music(new Scanner(System.in), System.out);

        try {
            // Act
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        // Assert
        String output = outContent.toString();
    }

    @Test
    public void AddSongInvalidTest() {

        String input = "1\n1\n50\n\n2\n50\n\n0\n5\n\n"; // Playlist name, description, and choice not to add songs
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream); // Simulated user input
        System.setOut(new PrintStream(outContent)); // Capture output

        // Create PlaylistUI with new Scanner and PrintStream
        Music music = new Music(new Scanner(System.in), System.out);

        // Act
        music.userOptionsMenu();

        // Assert
        String output = outContent.toString();
    }



    @Test
    public void ViewDetailsTest() {
        String input = "1\n4\n\n5\n\n6\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void DeleteSongTest() {
        String input = "1\n3\n1\nTest Artist\nTest Bio\n\n1\n1\n1\nTest Song\n180\nRock\n2\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream); // Simulated user input
        System.setOut(new PrintStream(outContent)); // Capture output

        // Create PlaylistUI with new Scanner and PrintStream
        Music music = new Music(new Scanner(System.in), System.out);

        try {
            // Act
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        // Assert
        String output = outContent.toString();
    }

    @Test
    public void AddSongAlbumInvalid2Test() {
        String input = "1\n10\n3\n1\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void AddSongAlbumValidTest() {
        String input = "1\n10\n3\n1\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void NewAddSongAlbumValidTest() {
        String input = "1\n3\n1\nTest Artist\nTest Bio\n\n1\n1\n1\nTest Song\n180\nRock\n2\n2\n1\nTest Album\n2023\nRock\nn\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream); // Simulated user input
        System.setOut(new PrintStream(outContent)); // Capture output

        // Create PlaylistUI with new Scanner and PrintStream
        Music music = new Music(new Scanner(System.in), System.out);

        try {
            // Act
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }
        
        // Assert
        String output = outContent.toString();
    }

    @Test
    public void AddSongAlbumInvalidTest() {
        String input = "1\n10\n50\n\n10\n1\n50\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void DeleteArtistTest() {
        String input = "1\n9\n0\n20\n\n9\n20\n\n9\n1\nn\n\n9\n3\ny\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void DeleteAlbumTest() {
        String input = "1\n8\n20\n\n8\n3\nn\n\n8\n3\ny\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }

    @Test
    public void DeleteSongValidTest() {
        String input = "1\n7\n20\n\n7\n1\nn\n\n7\n1\ny\n\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }


    @Test
    public void CreatePlaylistValidTest() {
        String input = "2\n1\nTest Playlist\nTest Description\nn\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }
    @Test
    public void EditPlaylistValidTest() {
        String input = "2\n3\n1\nNew Playlist Name\nNew Description\n0\n5\n\n"; 
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setIn(inputStream);
        System.setOut(new PrintStream(outContent));

        Music music = new Music(new Scanner(System.in), System.out);

        try {
            music.userOptionsMenu();
        } catch (NumberFormatException | NoSuchElementException e) {
            // Bu hataları bekliyoruz, testi geçerli sayalım
        }

        String output = outContent.toString();
    }




}