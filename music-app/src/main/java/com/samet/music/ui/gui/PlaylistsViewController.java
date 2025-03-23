package com.samet.music.ui.gui;

import com.samet.music.dao.PlaylistDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class PlaylistsViewController {

    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML private TableView<Playlist> playlistsTable;
    @FXML private TableColumn<Playlist, String> playlistNameColumn;
    @FXML private TableColumn<Playlist, String> playlistDescriptionColumn;
    @FXML private TableColumn<Playlist, Integer> playlistSongCountColumn;
    @FXML private TableColumn<Playlist, String> playlistDurationColumn;

    @FXML private TableView<Song> playlistSongsTable;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> songArtistColumn;
    @FXML private TableColumn<Song, String> songDurationColumn;
    @FXML private TableColumn<Song, String> songAlbumColumn;

    private ObservableList<Playlist> playlistsList = FXCollections.observableArrayList();
    private ObservableList<Song> playlistSongsList = FXCollections.observableArrayList();

    private BooleanProperty noPlaylistSelected = new SimpleBooleanProperty(true);
    private BooleanProperty noSongSelected = new SimpleBooleanProperty(true);

    public BooleanProperty noPlaylistSelectedProperty() {
        return noPlaylistSelected;
    }

    public BooleanProperty noSongSelectedProperty() {
        return noSongSelected;
    }

    @FXML
    public void initialize() {
        // Playlist tablosu sütunlarını yapılandır
        playlistNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        playlistDescriptionColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getDescription()));
        playlistSongCountColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getSongCount()));
        playlistDurationColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getFormattedTotalDuration()));

        // Şarkı tablosu sütunlarını yapılandır
        songNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        songArtistColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            if (song.getArtist() != null) {
                return new ReadOnlyObjectWrapper<>(song.getArtist().getName());
            }
            return new ReadOnlyObjectWrapper<>("Unknown");
        });
        songDurationColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getFormattedDuration()));
        songAlbumColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            if (song.getAlbum() != null) {
                return new ReadOnlyObjectWrapper<>(song.getAlbum().getName());
            }
            return new ReadOnlyObjectWrapper<>("N/A");
        });

        // Tabloları veri listeleriyle bağla
        playlistsTable.setItems(playlistsList);
        playlistSongsTable.setItems(playlistSongsList);

        // Seçim değişikliklerini dinle
        playlistsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            noPlaylistSelected.set(newSelection == null);
            updateSongList();
        });

        playlistSongsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            noSongSelected.set(newSelection == null);
        });

        // Verileri yükle
        refreshData();
    }

    private void updateSongList() {
        playlistSongsList.clear();
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            List<Song> songs = service.getSongsInPlaylist(selectedPlaylist.getId());
            playlistSongsList.addAll(songs);
        }
    }

    @FXML
    private void handleCreatePlaylist() {
        try {
            // Yeni bir dialog aç
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePlaylistDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create New Playlist");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));

            CreatePlaylistDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Could not open create playlist dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditPlaylist() {
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            try {
                System.out.println("Editing playlist: " + selectedPlaylist.getId() + " - " + selectedPlaylist.getName());

                // Edit dialog aç
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditPlaylistDialog.fxml"));
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Playlist");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(root));

                EditPlaylistDialogController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setPlaylist(selectedPlaylist);

                dialogStage.showAndWait();

                if (controller.isSaved()) {
                    // Görünümü yenile
                    refreshData();

                    // Kullanıcıya bilgi ver
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Playlist updated successfully.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("Error editing playlist: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Error", "Could not edit playlist: " + e.getMessage());
            }
        } else {
            showErrorAlert("No Selection", "Please select a playlist to edit");
        }
    }

    @FXML
    private void handleDeletePlaylist() {
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            try {
                if (confirmDelete("Delete Playlist",
                        "Are you sure you want to delete the playlist '" + selectedPlaylist.getName() + "'?")) {

                    System.out.println("Attempting to delete playlist: " + selectedPlaylist.getId());

                    // PlaylistDAO'yu doğrudan kullanarak silelim
                    PlaylistDAO playlistDAO = new PlaylistDAO();
                    playlistDAO.delete(selectedPlaylist.getId());

                    // Servis metodunu da çağıralım (çift güvenlik)
                    boolean success = service.removePlaylist(selectedPlaylist.getId());

                    // Her halükarda GUI'yi güncelleyelim
                    refreshData();

                    // Kullanıcıya bilgi verelim
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Playlist Deleted");
                    alert.setHeaderText(null);
                    alert.setContentText("Playlist '" + selectedPlaylist.getName() + "' has been deleted.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("Error in handleDeletePlaylist: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Error", "Failed to delete the playlist: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddSongToPlaylist() {
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddSongToPlaylistDialog.fxml"));
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Add Song to Playlist");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(root));

                AddSongToPlaylistDialogController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setPlaylist(selectedPlaylist);

                dialogStage.showAndWait();

                if (controller.isSaved()) {
                    updateSongList();
                }
            } catch (Exception e) {
                showErrorAlert("Error", "Could not open add song dialog: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveSongFromPlaylist() {
        Playlist selectedPlaylist = playlistsTable.getSelectionModel().getSelectedItem();
        Song selectedSong = playlistSongsTable.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null && selectedSong != null) {
            if (confirmDelete("Remove Song",
                    "Are you sure you want to remove '" + selectedSong.getName() + "' from the playlist?")) {
                boolean success = service.removeSongFromPlaylist(selectedSong.getId(), selectedPlaylist.getId());
                if (success) {
                    updateSongList();
                } else {
                    showErrorAlert("Error", "Failed to remove song from playlist");
                }
            }
        }
    }

    private void refreshData() {
        try {
            playlistsList.clear();

            // Veritabanından en güncel playlist listesini alalım
            PlaylistDAO playlistDAO = new PlaylistDAO();
            List<Playlist> playlists = playlistDAO.getAll();

            // UI'ı güncelleyelim
            playlistsList.addAll(playlists);

            // Playlist seçiliyse, şarkı listesini de güncelleyelim
            updateSongList();

            System.out.println("Refreshed playlist data: " + playlists.size() + " playlists loaded");
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