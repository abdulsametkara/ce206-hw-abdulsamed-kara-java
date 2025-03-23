package com.samet.music.ui.gui;

import com.samet.music.model.Album;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

public class AlbumDetailsDialogController {

    @FXML private Label albumNameLabel;
    @FXML private Label artistNameLabel;
    @FXML private Label releaseYearLabel;
    @FXML private Label genreLabel;

    @FXML private TableView<Song> songsTable;
    @FXML private TableColumn<Song, String> trackNumberColumn;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> durationColumn;

    private Album album;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML
    public void initialize() {
        // Configure table columns
        songNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        durationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        trackNumberColumn.setCellValueFactory(cellData -> {
            // This would ideally come from a track number field on Song
            int index = songsTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
    }

    public void setAlbum(Album album) {
        this.album = album;

        // Update UI with album details
        albumNameLabel.setText(album.getName());
        artistNameLabel.setText(album.getArtist() != null ? album.getArtist().getName() : "Unknown");
        releaseYearLabel.setText(String.valueOf(album.getReleaseYear()));
        genreLabel.setText(album.getGenre());

        // Load songs for this album
        List<Song> songs = service.getSongsByAlbum(album.getId());
        songsTable.setItems(FXCollections.observableArrayList(songs));
    }
}