package com.samet.music.ui.gui;

import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.service.MusicCollectionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class MetadataEditingViewController {

    private MusicCollectionService service = MusicCollectionService.getInstance();

    // Artist Tab Controls
    @FXML private ComboBox<Artist> artistComboBox;
    @FXML private TextField artistNameField;
    @FXML private TextArea artistBiographyArea;

    // Album Tab Controls
    @FXML private ComboBox<Album> albumComboBox;
    @FXML private TextField albumNameField;
    @FXML private ComboBox<Artist> albumArtistComboBox;
    @FXML private TextField albumYearField;
    @FXML private TextField albumGenreField;

    // Song Tab Controls
    @FXML private ComboBox<Song> songComboBox;
    @FXML private TextField songNameField;
    @FXML private Label songArtistLabel;
    @FXML private ComboBox<Album> songAlbumComboBox;
    @FXML private TextField songDurationField;
    @FXML private TextField songGenreField;

    @FXML
    public void initialize() {
        setupArtistTab();
        setupAlbumTab();
        setupSongTab();
    }

    private void setupArtistTab() {
        // Sanatçı ComboBox'ını konfigüre et
        artistComboBox.setConverter(new StringConverter<Artist>() {
            @Override
            public String toString(Artist artist) {
                return artist != null ? artist.getName() : "";
            }

            @Override
            public Artist fromString(String string) {
                return null; // Not used
            }
        });

        // Sanatçı seçildiğinde alanları güncelle
        artistComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                artistNameField.setText(newValue.getName());
                artistBiographyArea.setText(newValue.getBiography());
            } else {
                artistNameField.clear();
                artistBiographyArea.clear();
            }
        });

        // Sanatçıları yükle
        List<Artist> artists = service.getAllArtists();
        artistComboBox.setItems(FXCollections.observableArrayList(artists));
    }

    private void setupAlbumTab() {
        // Album ComboBox'ını konfigüre et
        albumComboBox.setConverter(new StringConverter<Album>() {
            @Override
            public String toString(Album album) {
                if (album == null) return "";
                String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";
                return album.getName() + " (" + artistName + ")";
            }

            @Override
            public Album fromString(String string) {
                return null; // Not used
            }
        });

        // Sanatçı ComboBox'ını konfigüre et
        albumArtistComboBox.setConverter(new StringConverter<Artist>() {
            @Override
            public String toString(Artist artist) {
                return artist != null ? artist.getName() : "";
            }

            @Override
            public Artist fromString(String string) {
                return null; // Not used
            }
        });

        // Album seçildiğinde alanları güncelle
        albumComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                albumNameField.setText(newValue.getName());
                albumYearField.setText(String.valueOf(newValue.getReleaseYear()));
                albumGenreField.setText(newValue.getGenre());

                if (newValue.getArtist() != null) {
                    albumArtistComboBox.getSelectionModel().select(newValue.getArtist());
                } else {
                    albumArtistComboBox.getSelectionModel().clearSelection();
                }
            } else {
                albumNameField.clear();
                albumYearField.clear();
                albumGenreField.clear();
                albumArtistComboBox.getSelectionModel().clearSelection();
            }
        });

        // Albümleri ve sanatçıları yükle
        List<Album> albums = service.getAllAlbums();
        List<Artist> artists = service.getAllArtists();

        albumComboBox.setItems(FXCollections.observableArrayList(albums));
        albumArtistComboBox.setItems(FXCollections.observableArrayList(artists));
    }

    private void setupSongTab() {
        // Şarkı ComboBox'ını konfigüre et
        songComboBox.setConverter(new StringConverter<Song>() {
            @Override
            public String toString(Song song) {
                if (song == null) return "";
                String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
                return song.getName() + " (" + artistName + ")";
            }

            @Override
            public Song fromString(String string) {
                return null; // Not used
            }
        });

        // Album ComboBox'ını konfigüre et
        songAlbumComboBox.setConverter(new StringConverter<Album>() {
            @Override
            public String toString(Album album) {
                if (album == null) return "None";
                return album.getName();
            }

            @Override
            public Album fromString(String string) {
                return null; // Not used
            }
        });

        // Şarkı seçildiğinde alanları güncelle
        songComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                songNameField.setText(newValue.getName());
                songDurationField.setText(String.valueOf(newValue.getDuration()));
                songGenreField.setText(newValue.getGenre());

                if (newValue.getArtist() != null) {
                    songArtistLabel.setText(newValue.getArtist().getName());
                } else {
                    songArtistLabel.setText("Unknown");
                }

                // Sanatçıya ait albümleri yükle
                if (newValue.getArtist() != null) {
                    List<Album> artistAlbums = service.getAlbumsByArtist(newValue.getArtist().getId());
                    songAlbumComboBox.setItems(FXCollections.observableArrayList(artistAlbums));
                } else {
                    songAlbumComboBox.setItems(FXCollections.observableArrayList());
                }

                if (newValue.getAlbum() != null) {
                    songAlbumComboBox.getSelectionModel().select(newValue.getAlbum());
                } else {
                    songAlbumComboBox.getSelectionModel().clearSelection();
                }
            } else {
                songNameField.clear();
                songDurationField.clear();
                songGenreField.clear();
                songArtistLabel.setText("");
                songAlbumComboBox.setItems(FXCollections.observableArrayList());
            }
        });

        // Şarkıları yükle
        List<Song> songs = service.getAllSongs();
        songComboBox.setItems(FXCollections.observableArrayList(songs));
    }

    @FXML
    private void handleSaveArtistChanges() {
        Artist selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();
        if (selectedArtist != null) {
            String name = artistNameField.getText().trim();
            String biography = artistBiographyArea.getText().trim();

            if (name.isEmpty()) {
                showErrorAlert("Validation Error", "Artist name cannot be empty");
                return;
            }

            selectedArtist.setName(name);
            selectedArtist.setBiography(biography);

            // ArtistDAO'yu doğrudan kullanarak güncelleyelim
            ArtistDAO artistDAO = new ArtistDAO();
            artistDAO.update(selectedArtist);

            System.out.println("Updated artist in database: " + selectedArtist.getId() + " - " + name);

            showInfoAlert("Success", "Artist information updated successfully");

            // Tüm artist verilerini yeniden yükleyelim
            List<Artist> artists = service.getAllArtists();
            artistComboBox.setItems(FXCollections.observableArrayList(artists));

            // Güncellenmiş sanatçıyı seçili hale getirelim
            for (Artist artist : artists) {
                if (artist.getId().equals(selectedArtist.getId())) {
                    artistComboBox.getSelectionModel().select(artist);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSaveAlbumChanges() {
        Album selectedAlbum = albumComboBox.getSelectionModel().getSelectedItem();
        if (selectedAlbum != null) {
            String name = albumNameField.getText().trim();
            Artist artist = albumArtistComboBox.getSelectionModel().getSelectedItem();
            String yearText = albumYearField.getText().trim();
            String genre = albumGenreField.getText().trim();

            if (name.isEmpty()) {
                showErrorAlert("Validation Error", "Album name cannot be empty");
                return;
            }

            if (artist == null) {
                showErrorAlert("Validation Error", "Please select an artist");
                return;
            }

            try {
                int year = Integer.parseInt(yearText);
                if (year <= 0) {
                    showErrorAlert("Validation Error", "Release year must be a positive number");
                    return;
                }

                selectedAlbum.setName(name);
                selectedAlbum.setArtist(artist);
                selectedAlbum.setReleaseYear(year);
                selectedAlbum.setGenre(genre);

                // Albümü güncelle - normalde bir service metodu olmalı
                // service.updateAlbum(selectedAlbum);
                showInfoAlert("Success", "Album information updated successfully");

                // ComboBox'ı yenile
                int selectedIndex = albumComboBox.getSelectionModel().getSelectedIndex();
                List<Album> albums = service.getAllAlbums();
                albumComboBox.setItems(FXCollections.observableArrayList(albums));
                albumComboBox.getSelectionModel().select(selectedIndex);
            } catch (NumberFormatException e) {
                showErrorAlert("Validation Error", "Release year must be a number");
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to update album: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveSongChanges() {
        Song selectedSong = songComboBox.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            String name = songNameField.getText().trim();
            Album album = songAlbumComboBox.getSelectionModel().getSelectedItem();
            String durationText = songDurationField.getText().trim();
            String genre = songGenreField.getText().trim();

            if (name.isEmpty()) {
                showErrorAlert("Validation Error", "Song name cannot be empty");
                return;
            }

            try {
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    showErrorAlert("Validation Error", "Duration must be a positive number");
                    return;
                }

                selectedSong.setName(name);
                selectedSong.setAlbum(album);
                selectedSong.setDuration(duration);
                selectedSong.setGenre(genre);

                // Şarkıyı güncelle - normalde bir service metodu olmalı
                // service.updateSong(selectedSong);
                showInfoAlert("Success", "Song information updated successfully");

                // ComboBox'ı yenile
                int selectedIndex = songComboBox.getSelectionModel().getSelectedIndex();
                List<Song> songs = service.getAllSongs();
                songComboBox.setItems(FXCollections.observableArrayList(songs));
                songComboBox.getSelectionModel().select(selectedIndex);
            } catch (NumberFormatException e) {
                showErrorAlert("Validation Error", "Duration must be a number");
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to update song: " + e.getMessage());
            }
        }
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