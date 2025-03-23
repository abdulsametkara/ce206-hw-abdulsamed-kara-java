package com.samet.music.ui.gui;

import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class AddSongToPlaylistDialogController {

    @FXML private TableView<Song> availableSongsTable;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> artistNameColumn;
    @FXML private TableColumn<Song, String> durationColumn;
    @FXML private TableColumn<Song, String> albumColumn;

    private Stage dialogStage;
    private Playlist playlist;
    private boolean saved = false;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML
    public void initialize() {
        // Tablo sütunlarını yapılandır
        songNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        artistNameColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            return new SimpleStringProperty(song.getArtist() != null ?
                    song.getArtist().getName() : "Unknown");
        });

        durationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        albumColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            return new SimpleStringProperty(song.getAlbum() != null ?
                    song.getAlbum().getName() : "N/A");
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        loadAvailableSongs();
    }

    public boolean isSaved() {
        return saved;
    }

    private void loadAvailableSongs() {
        // Tüm şarkıları al
        List<Song> allSongs = service.getAllSongs();

        // Çalma listesindeki şarkıları al
        List<Song> playlistSongs = service.getSongsInPlaylist(playlist.getId());

        // Çalma listesinde olmayan şarkıları filtrele
        List<Song> availableSongs = allSongs.stream()
                .filter(song -> !playlistSongs.contains(song))
                .collect(Collectors.toList());

        availableSongsTable.setItems(FXCollections.observableArrayList(availableSongs));
    }

    @FXML
    private void handleAddSelected() {
        Song selectedSong = availableSongsTable.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a song to add to the playlist");
            return;
        }

        boolean success = service.addSongToPlaylist(selectedSong.getId(), playlist.getId());

        if (success) {
            saved = true;
            // Tablodan seçilen şarkıyı kaldır
            availableSongsTable.getItems().remove(selectedSong);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Song added to playlist successfully");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add song to playlist");
        }
    }

    @FXML
    private void handleAddAll() {
        ObservableList<Song> songs = availableSongsTable.getItems();

        if (songs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information",
                    "No songs available to add");
            return;
        }

        int addedCount = 0;

        for (Song song : songs) {
            boolean success = service.addSongToPlaylist(song.getId(), playlist.getId());
            if (success) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            saved = true;
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    addedCount + " songs added to playlist successfully");
            // Tabloyu temizle
            availableSongsTable.getItems().clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add songs to playlist");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}