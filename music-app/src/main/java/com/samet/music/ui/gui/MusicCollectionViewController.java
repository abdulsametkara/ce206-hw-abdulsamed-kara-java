package com.samet.music.ui.gui;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class MusicCollectionViewController {

    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML private TableView<Song> songsTable;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> songArtistColumn;
    @FXML private TableColumn<Song, String> songDurationColumn;
    @FXML private TableColumn<Song, String> songAlbumColumn;
    @FXML private TableColumn<Song, String> songGenreColumn;

    @FXML private TableView<Album> albumsTable;
    @FXML private TableColumn<Album, String> albumNameColumn;
    @FXML private TableColumn<Album, String> albumArtistColumn;
    @FXML private TableColumn<Album, Integer> albumYearColumn;
    @FXML private TableColumn<Album, String> albumGenreColumn;

    @FXML private TableView<Artist> artistsTable;
    @FXML private TableColumn<Artist, String> artistNameColumn;
    @FXML private TableColumn<Artist, Integer> artistAlbumsColumn;
    @FXML private TableColumn<Artist, Integer> artistSongsColumn;

    private ObservableList<Song> songsList = FXCollections.observableArrayList();
    private ObservableList<Album> albumsList = FXCollections.observableArrayList();
    private ObservableList<Artist> artistsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure song table columns
        songNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        songArtistColumn.setCellValueFactory(cellData -> {
            Artist artist = cellData.getValue().getArtist();
            return new SimpleStringProperty(artist != null ? artist.getName() : "Unknown");
        });
        songDurationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDuration()));
        songAlbumColumn.setCellValueFactory(cellData -> {
            Album album = cellData.getValue().getAlbum();
            return new SimpleStringProperty(album != null ? album.getName() : "N/A");
        });
        songGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // Configure album table columns
        albumNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        albumArtistColumn.setCellValueFactory(cellData -> {
            Artist artist = cellData.getValue().getArtist();
            return new SimpleStringProperty(artist != null ? artist.getName() : "Unknown");
        });
        albumYearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        albumGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // Configure artist table columns
        artistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        artistAlbumsColumn.setCellValueFactory(cellData -> {
            List<Album> albums = cellData.getValue().getAlbums();
            return new ReadOnlyObjectWrapper<>(albums.size());
        });
        artistSongsColumn.setCellValueFactory(cellData -> {
            List<Song> songs = service.getSongsByArtist(cellData.getValue().getId());
            return new ReadOnlyObjectWrapper<>(songs.size());
        });

        // Set data
        songsTable.setItems(songsList);
        albumsTable.setItems(albumsList);
        artistsTable.setItems(artistsList);

        // Load data
        refreshData();
    }

    @FXML
    private void handleAddSong() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddSongDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Song");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));

            AddSongDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (Exception e) {
            showErrorAlert("Error opening dialog", e.getMessage());
        }
    }

    @FXML
    private void handleAddAlbum() {
        try {
            // Dialog penceresi aç
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddAlbumDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Album");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));

            AddAlbumDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Could not open add album dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddArtist() {
        try {
            // Dialog penceresi aç
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddArtistDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Artist");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));

            AddArtistDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Could not open add artist dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            TabPane tabPane = (TabPane) songsTable.getParent().getParent();
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

            if (selectedTab == null) {
                showErrorAlert("Error", "No tab selected");
                return;
            }

            String tabText = selectedTab.getText();

            if ("Songs".equals(tabText)) {
                Song selectedSong = songsTable.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    if (confirmDelete("Delete Song", "Are you sure you want to delete the song '" + selectedSong.getName() + "'?")) {
                        boolean success = service.removeSong(selectedSong.getId());
                        if (success) {
                            refreshData();
                        } else {
                            showErrorAlert("Error", "Could not delete the song");
                        }
                    }
                } else {
                    showErrorAlert("No Selection", "Please select a song to delete");
                }
            } else if ("Albums".equals(tabText)) {
                Album selectedAlbum = albumsTable.getSelectionModel().getSelectedItem();
                if (selectedAlbum != null) {
                    if (confirmDelete("Delete Album", "Are you sure you want to delete the album '" + selectedAlbum.getName() + "'? This will remove the album but keep all songs.")) {
                        // Albüm silme işlemi - MusicCollectionService'deki metodu çağır
                        boolean success = service.removeAlbum(selectedAlbum.getId(), false);
                        if (success) {
                            refreshData();
                        } else {
                            showErrorAlert("Error", "Could not delete the album");
                        }
                    }
                } else {
                    showErrorAlert("No Selection", "Please select an album to delete");
                }
            } else if ("Artists".equals(tabText)) {
                Artist selectedArtist = artistsTable.getSelectionModel().getSelectedItem();
                if (selectedArtist != null) {
                    if (confirmDelete("Delete Artist", "Are you sure you want to delete the artist '" + selectedArtist.getName() + "'? This will also delete all albums and songs by this artist.")) {
                        // Sanatçı silme işlemi - MusicCollectionService'deki metodu çağır
                        boolean success = service.removeArtist(selectedArtist.getId());
                        if (success) {
                            refreshData();
                        } else {
                            showErrorAlert("Error", "Could not delete the artist");
                        }
                    }
                } else {
                    showErrorAlert("No Selection", "Please select an artist to delete");
                }
            }
        } catch (Exception e) {
            showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    private void refreshData() {
        try {
            songsList.clear();
            albumsList.clear();
            artistsList.clear();

            // Her zaman veritabanından yeni verileri çek
            List<Song> songs = service.getAllSongs();
            List<Album> albums = service.getAllAlbums();
            List<Artist> artists = service.getAllArtists();

            // Benzersiz sanatçı listesi oluştur
            Map<String, Artist> uniqueArtists = new HashMap<>();
            for (Artist artist : artists) {
                uniqueArtists.put(artist.getId(), artist);
            }

            // Aynı isimli sanatçıları tespit et ve birleştir
            Map<String, List<Artist>> artistsByName = new HashMap<>();
            for (Artist artist : uniqueArtists.values()) {
                String name = artist.getName().toLowerCase();
                artistsByName.computeIfAbsent(name, k -> new ArrayList<>()).add(artist);
            }

            // Her isim için tek bir sanatçı seç
            Map<String, Artist> finalArtists = new HashMap<>();
            for (Map.Entry<String, List<Artist>> entry : artistsByName.entrySet()) {
                List<Artist> sameNameArtists = entry.getValue();

                // Şarkı sayısı en fazla olanı seç
                sameNameArtists.sort((a1, a2) -> {
                    int songs1 = service.getSongsByArtist(a1.getId()).size();
                    int songs2 = service.getSongsByArtist(a2.getId()).size();
                    return Integer.compare(songs2, songs1); // Büyükten küçüğe sırala
                });

                finalArtists.put(entry.getKey(), sameNameArtists.get(0));
            }

            // Listeleri doldur
            songsList.addAll(songs);
            albumsList.addAll(albums);
            artistsList.addAll(finalArtists.values());

            System.out.println("Refreshed data: " + songs.size() + " songs, " +
                    albums.size() + " albums, " +
                    finalArtists.size() + " unique artists");
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean confirmDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}