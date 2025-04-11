package com.samet.music.ui;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.ui.MetadataEditingUI;

public class aaa {

    private MetadataEditingUI metadataEditingUI;

    @Mock
    private MusicCollectionService service;

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private Scanner scanner;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);

        // Test için service mock'unu hazırla
        metadataEditingUI = new MetadataEditingUI(null, printStream);
        metadataEditingUI.service = service;
    }

    @Test
    public void testEditAlbumWithEmptyList() {
        // Boş albüm listesi için test
        when(service.getAllAlbums()).thenReturn(new ArrayList<>());

        // Metodu çağır
        metadataEditingUI.editAlbum();

        // Çıktıyı kontrol et
        String output = outputStream.toString();
        assertTrue(output.contains("No albums found in the collection."));
    }

    @Test
    public void testEditAlbumUserCancels() {
        // Kullanıcı iptal ederse test
        List<Album> albums = createSampleAlbums();
        when(service.getAllAlbums()).thenReturn(albums);

        // Kullanıcı girişi: 0 (iptal)
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        scanner = new Scanner(System.in);
        metadataEditingUI.scanner = scanner;

        metadataEditingUI.editAlbum();

        String output = outputStream.toString();
        assertTrue(output.contains("Select an album to edit:"));
        // İptal edildiğinde metodun sessizce bittiğini kontrol et
    }

    @Test
    public void testEditAlbumInvalidSelection() {
        // Geçersiz seçim için test
        List<Album> albums = createSampleAlbums();
        when(service.getAllAlbums()).thenReturn(albums);

        // Geçersiz giriş: 999
        String input = "999\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        scanner = new Scanner(System.in);
        metadataEditingUI.scanner = scanner;

        metadataEditingUI.editAlbum();

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid selection. Operation cancelled."));
    }

    @Test
    public void testEditAlbumNonNumericInput() {
        // Sayısal olmayan giriş için test
        List<Album> albums = createSampleAlbums();
        when(service.getAllAlbums()).thenReturn(albums);

        // Geçersiz giriş: abc
        String input = "abc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        scanner = new Scanner(System.in);
        metadataEditingUI.scanner = scanner;

        metadataEditingUI.editAlbum();

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid input. Operation cancelled."));
    }

    // Yardımcı metodlar
    private List<Album> createSampleAlbums() {
        List<Album> albums = new ArrayList<>();

        // Test için sanatçı nesneleri oluştur
        Artist artist1 = new Artist("Artist 1");
        Artist artist2 = new Artist("Artist 2");

        // Test için albüm nesneleri oluştur
        Album album1 = new Album("Album 1", artist1, 2020);
        album1.setGenre("Rock");

        Album album2 = new Album("Album 2", artist2, 2021);
        album2.setGenre("Pop");

        Album album3 = new Album("Album 3", null, 2022);
        album3.setGenre("Jazz");

        albums.add(album1);
        albums.add(album2);
        albums.add(album3);

        return albums;
    }
}