package com.samet.music.ui.gui;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.service.MusicRecommendationSystem;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationsViewController {

    private MusicCollectionService service = MusicCollectionService.getInstance();
    private MusicRecommendationSystem recommendationSystem = MusicRecommendationSystem.getInstance();

    // Geçici olarak sabit kullanıcı ID'si kullanıyoruz - gerçek uygulamada oturum açmış kullanıcıdan alınmalı
    private final String currentUserId = "currentUser";

    // Song Recommendations Tab
    @FXML private ListView<String> topGenresListView;
    @FXML private TableView<Song> recommendedSongsTable;
    @FXML private TableColumn<Song, String> songNameColumn;
    @FXML private TableColumn<Song, String> songArtistColumn;
    @FXML private TableColumn<Song, String> songDurationColumn;
    @FXML private TableColumn<Song, String> songGenreColumn;

    // Album Recommendations Tab
    @FXML private ListView<String> topArtistsListView;
    @FXML private TableView<Album> recommendedAlbumsTable;
    @FXML private TableColumn<Album, String> albumNameColumn;
    @FXML private TableColumn<Album, String> albumArtistColumn;
    @FXML private TableColumn<Album, String> albumYearColumn;
    @FXML private TableColumn<Album, String> albumGenreColumn;

    // Artist Recommendations Tab
    @FXML private TableView<Artist> recommendedArtistsTable;
    @FXML private TableColumn<Artist, String> artistNameColumn;
    @FXML private TableColumn<Artist, String> artistBiographyColumn;
    @FXML private TableColumn<Artist, String> artistAlbumCountColumn;

    private ObservableList<Song> recommendedSongs = FXCollections.observableArrayList();
    private ObservableList<Album> recommendedAlbums = FXCollections.observableArrayList();
    private ObservableList<Artist> recommendedArtists = FXCollections.observableArrayList();
    private ObservableList<String> topGenres = FXCollections.observableArrayList();
    private ObservableList<String> topArtists = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Şarkı önerileri tablosunu konfigüre et
        songNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getName()));
        songArtistColumn.setCellValueFactory(cellData -> {
            Song song = cellData.getValue();
            return new ReadOnlyStringWrapper(song.getArtist() != null ? song.getArtist().getName() : "Unknown");
        });
        songDurationColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getFormattedDuration()));
        songGenreColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getGenre()));

        // Albüm önerileri tablosunu konfigüre et
        albumNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getName()));
        albumArtistColumn.setCellValueFactory(cellData -> {
            Album album = cellData.getValue();
            return new ReadOnlyStringWrapper(album.getArtist() != null ? album.getArtist().getName() : "Unknown");
        });
        albumYearColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getReleaseYear())));
        albumGenreColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getGenre()));

        // Sanatçı önerileri tablosunu konfigüre et
        artistNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getName()));
        artistBiographyColumn.setCellValueFactory(cellData -> {
            String bio = cellData.getValue().getBiography();
            if (bio != null && bio.length() > 100) {
                bio = bio.substring(0, 100) + "...";
            }
            return new ReadOnlyStringWrapper(bio);
        });
        artistAlbumCountColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getAlbums().size())));

        // Tabloları veri listeleriyle bağla
        recommendedSongsTable.setItems(recommendedSongs);
        recommendedAlbumsTable.setItems(recommendedAlbums);
        recommendedArtistsTable.setItems(recommendedArtists);

        // Liste görünümlerini bağla
        topGenresListView.setItems(topGenres);
        topArtistsListView.setItems(topArtists);

        // Veri yoksa kullanıcı için dinleme geçmişi simüle et
        simulateListeningHistory();

        // Tüm önerileri yükle
        loadAllRecommendations();
    }

    private void simulateListeningHistory() {
        // Gerçek uygulamada bu zaten kaydedilmiş olacak, ama burası için simüle ediyoruz
        List<Song> allSongs = service.getAllSongs();
        if (!allSongs.isEmpty()) {
            // Bazı şarkıları rastgele sayıda dinle
            for (int i = 0; i < Math.min(allSongs.size(), 5); i++) {
                Song song = allSongs.get(i);
                int playCount = (int) (Math.random() * 5) + 1;
                for (int j = 0; j < playCount; j++) {
                    recommendationSystem.recordSongPlay(currentUserId, song.getId());
                }
            }
        }
    }

    private void loadAllRecommendations() {
        loadSongRecommendations();
        loadAlbumRecommendations();
        loadArtistRecommendations();
        loadUserTopGenres();
        loadUserTopArtists();
    }

    private void loadSongRecommendations() {
        recommendedSongs.clear();
        List<Song> songs = recommendationSystem.recommendSongsByGenre(currentUserId, 10);
        recommendedSongs.addAll(songs);
    }

    private void loadAlbumRecommendations() {
        recommendedAlbums.clear();
        List<Album> albums = recommendationSystem.recommendAlbumsByArtist(currentUserId, 10);
        recommendedAlbums.addAll(albums);
    }

    private void loadArtistRecommendations() {
        recommendedArtists.clear();
        List<Artist> artists = recommendationSystem.recommendArtists(currentUserId, 10);
        recommendedArtists.addAll(artists);
    }

    private void loadUserTopGenres() {
        topGenres.clear();
        Map<String, Integer> genres = recommendationSystem.getUserTopGenres(currentUserId);
        List<String> genreList = genres.entrySet().stream()
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.toList());
        topGenres.addAll(genreList);
    }

    private void loadUserTopArtists() {
        topArtists.clear();
        Map<String, Integer> artistIds = recommendationSystem.getUserTopArtists(currentUserId);

        List<String> artistList = artistIds.entrySet().stream()
                .map(entry -> {
                    String artistId = entry.getKey();
                    int score = entry.getValue();
                    Artist artist = service.getArtistById(artistId);
                    return (artist != null ? artist.getName() : "Unknown") + " (" + score + ")";
                })
                .collect(Collectors.toList());

        topArtists.addAll(artistList);
    }

    @FXML
    private void handleListenToSong() {
        Song selectedSong = recommendedSongsTable.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            // Gerçek uygulamada şarkıyı çalacak bir işlem başlatılır
            // Burada sadece dinleme geçmişini güncelliyoruz
            recommendationSystem.recordSongPlay(currentUserId, selectedSong.getId());

            showInfoAlert("Now Playing",
                    "Now playing: " + selectedSong.getName() + " by " +
                            (selectedSong.getArtist() != null ? selectedSong.getArtist().getName() : "Unknown"));

            // Önerileri yenile
            loadAllRecommendations();
        } else {
            showErrorAlert("No Selection", "Please select a song to listen");
        }
    }

    @FXML
    private void handleRefreshSongRecommendations() {
        loadSongRecommendations();
        loadUserTopGenres();
    }

    @FXML
    private void handleViewAlbumTracks() {
        Album selectedAlbum = recommendedAlbumsTable.getSelectionModel().getSelectedItem();
        if (selectedAlbum != null) {
            try {
                // Albüm detaylarını gösteren dialog aç
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AlbumDetailsDialog.fxml"));
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Album Details: " + selectedAlbum.getName());
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(root));

                AlbumDetailsDialogController controller = loader.getController();
                controller.setAlbum(selectedAlbum);

                dialogStage.showAndWait();
            } catch (Exception e) {
                showErrorAlert("Error", "Could not open album details: " + e.getMessage());
            }
        } else {
            showErrorAlert("No Selection", "Please select an album to view");
        }
    }

    @FXML
    private void handleRefreshAlbumRecommendations() {
        loadAlbumRecommendations();
        loadUserTopArtists();
    }

    @FXML
    private void handleViewArtistDetails() {
        Artist selectedArtist = recommendedArtistsTable.getSelectionModel().getSelectedItem();
        if (selectedArtist != null) {
            try {
                // Sanatçı detaylarını gösteren dialog aç
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ArtistDetailsDialog.fxml"));
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Artist Details: " + selectedArtist.getName());
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(root));

                ArtistDetailsDialogController controller = loader.getController();
                controller.setArtist(selectedArtist);

                dialogStage.showAndWait();
            } catch (Exception e) {
                showErrorAlert("Error", "Could not open artist details: " + e.getMessage());
            }
        } else {
            showErrorAlert("No Selection", "Please select an artist to view");
        }
    }

    @FXML
    private void handleRefreshArtistRecommendations() {
        loadArtistRecommendations();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}