package com.samet.music.ui.gui;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;

public class ArtistDetailsDialogController {

    @FXML private Label artistNameLabel;
    @FXML private TextArea biographyTextArea;

    @FXML private TableView<Album> albumsTable;
    @FXML private TableColumn<Album, String> albumNameColumn;
    @FXML private TableColumn<Album, String> albumYearColumn;
    @FXML private TableColumn<Album, String> albumGenreColumn;

    @FXML private TableView<Song> popularSongsTable;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> songDurationColumn;
    @FXML private TableColumn<Song, String> songAlbumColumn;

    private Artist artist;
    private MusicCollectionService service = MusicCollectionService.getInstance();

    @FXML
    public void initialize() {
        // Album tablosu sütunlarını yapılandır
        albumNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        albumYearColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getReleaseYear())));

        albumGenreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGenre()));

        // Şarkı tablosu sütunlarını yapılandır
        songNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        songDurationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        songAlbumColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            return new SimpleStringProperty(song.getAlbum() != null ?
                    song.getAlbum().getName() : "N/A");
        });
    }

    public void setArtist(Artist artist) {
        this.artist = artist;

        // UI'ı sanatçı detaylarıyla güncelle
        artistNameLabel.setText(artist.getName());
        biographyTextArea.setText(artist.getBiography());

        // Sanatçının albümlerini yükle
        List<Album> albums = service.getAlbumsByArtist(artist.getId());
        albumsTable.setItems(FXCollections.observableArrayList(albums));

        // Sanatçının en popüler şarkılarını yükle
        List<Song> songs = service.getSongsByArtist(artist.getId());
        // Normalde bu şarkılar, oynatma sayısı gibi bir metriğe göre sıralanabilir
        // Burada sadece ilk 5 şarkıyı alıyoruz
        if (songs.size() > 5) {
            songs = songs.subList(0, 5);
        }
        popularSongsTable.setItems(FXCollections.observableArrayList(songs));
    }

    @FXML
    private void handleClose() {
        ((Stage) artistNameLabel.getScene().getWindow()).close();
    }
}