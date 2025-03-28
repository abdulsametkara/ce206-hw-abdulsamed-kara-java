package com.samet.music.service;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.monitoring.MetricsCollector;
import com.samet.music.repository.AlbumCollection;
import com.samet.music.repository.ArtistCollection;
import com.samet.music.repository.PlaylistCollection;
import com.samet.music.repository.SongCollection;
import com.samet.music.util.DatabaseUtil;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Service class for music collection operations
 * Implements Singleton pattern
 */
public class MusicCollectionService {
    private static final Logger logger = LoggerFactory.getLogger(MusicCollectionService.class);

    private final ArtistCollection artistCollection;
    private final AlbumCollection albumCollection;
    private final SongCollection songCollection;
    private final MusicFactory musicFactory;
    private final PlaylistCollection playlistCollection;

    // DAO nesneleri
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;

    // Singleton implementation
    private static MusicCollectionService instance;

    private MusicCollectionService() {
        this.artistCollection = ArtistCollection.getInstance();
        this.albumCollection = AlbumCollection.getInstance();
        this.songCollection = SongCollection.getInstance();
        this.musicFactory = MusicFactory.getInstance();
        this.playlistCollection = PlaylistCollection.getInstance();

        // DAO nesnelerini başlat
        this.songDAO = new SongDAO();
        this.albumDAO = new AlbumDAO();
        this.artistDAO = new ArtistDAO();

        logger.info("MusicCollectionService initialized");
    }

    public static synchronized MusicCollectionService getInstance() {
        if (instance == null) {
            instance = new MusicCollectionService();
        }
        return instance;
    }

    // Artist operations
    public boolean addArtist(String name, String biography) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_artist");
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Invalid artist data: name is empty or null");
                return false;
            }

            logger.info("Adding artist: {}", name);
            Artist artist = musicFactory.createArtist(name, biography);
            artistCollection.add(artist);

            // Metriği artır
            MetricsCollector.getInstance().incrementArtistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding artist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public Artist getArtistById(String id) {
        try {
            logger.debug("Getting artist by ID: {}", id);
            return artistCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting artist by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<Artist> searchArtistsByName(String name) {
        try {
            logger.debug("Searching artists by name: {}", name);
            return artistCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching artists by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Album operations
    public boolean addAlbum(String name, String artistId, int releaseYear, String genre) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_album");
        try {
            if (name == null || name.trim().isEmpty() || releaseYear <= 0) {
                logger.warn("Invalid album data: name={}, releaseYear={}", name, releaseYear);
                return false;
            }

            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found for album creation", artistId);
                return false;
            }

            // Log işlem detayları
            logger.info("Adding album: {}, Artist ID: {}, Release Year: {}, Genre: {}",
                    name, artist.getId(), releaseYear, genre);

            Album album = musicFactory.createAlbum(name, artist, releaseYear, genre);
            albumCollection.add(album);

            // Metriği artır
            MetricsCollector.getInstance().incrementAlbumAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public List<Album> getAllAlbums() {
        try {
            logger.debug("Getting all albums");
            return albumCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Album getAlbumById(String id) {
        try {
            logger.debug("Getting album by ID: {}", id);
            return albumCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting album by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean removeAlbum(String id) {
        try {
            logger.info("Removing album with ID: {}", id);
            return albumCollection.remove(id);
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Album> searchAlbumsByName(String name) {
        try {
            logger.debug("Searching albums by name: {}", name);
            return albumCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching albums by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Album> getAlbumsByArtist(String artistId) {
        try {
            logger.debug("Getting albums by artist ID: {}", artistId);
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return Collections.emptyList();
            }
            return albumCollection.getByArtist(artist);
        } catch (Exception e) {
            logger.error("Error getting albums by artist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Album> getAlbumsByGenre(String genre) {
        try {
            logger.debug("Getting albums by genre: {}", genre);
            return albumCollection.getByGenre(genre);
        } catch (Exception e) {
            logger.error("Error getting albums by genre: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Playlist> getPlaylistsContainingSong(String songId) {
        try {
            if (songId == null || songId.trim().isEmpty()) {
                logger.warn("Invalid song ID for getPlaylistsContainingSong");
                return Collections.emptyList();
            }

            logger.debug("Getting playlists containing song: {}", songId);
            return playlistCollection.getPlaylistsContainingSong(songDAO.getById(songId));
        } catch (Exception e) {
            logger.error("Error getting playlists containing song: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Song operations
    public boolean addSong(String name, String artistId, int duration, String genre) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song");
        try {
            if (name == null || name.trim().isEmpty() || duration <= 0) {
                logger.warn("Invalid song data: name={}, duration={}", name, duration);
                return false;
            }

            // Önce sanatçıyı doğrudan veritabanından almayı deneyin
            Artist artist = null;
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE id = ?")) {

                stmt.setString(1, artistId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Sanatçıyı doğrudan oluşturun, koleksiyondan almak yerine
                    artist = new Artist(rs.getString("name"));
                    artist.setBiography(rs.getString("biography"));

                    logger.debug("Artist found directly in database: {}, ID: {}", artist.getName(), artist.getId());
                } else {
                    logger.warn("Artist with ID {} not found in database", artistId);
                    return false;
                }
            } catch (SQLException e) {
                logger.error("Error checking artist in database: {}", e.getMessage(), e);
                return false;
            }

            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return false;
            }

            logger.info("Creating song with name: {}, artist: {}, duration: {}, genre: {}",
                    name, artist.getName(), duration, genre);

            Song song = musicFactory.createSong(name, artist, duration, genre);
            songCollection.add(song);

            // Metriği artır
            MetricsCollector.getInstance().incrementSongAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding song: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    // MusicCollectionService sınıfındaki addSongToAlbum metodunu güncelleyelim
    public boolean addSongToAlbum(String songId, String albumId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song_to_album");
        try {
            Song song = songCollection.getById(songId);
            Album album = albumCollection.getById(albumId);

            if (song == null || album == null) {
                logger.warn("Cannot add song to album: song={}, album={}",
                        (song == null ? "null" : song.getId()),
                        (album == null ? "null" : album.getId()));
                return false;
            }

            // Şarkıyı albüme bağla
            song.setAlbum(album);

            // Veritabanını güncelle
            SongDAO songDAO = new SongDAO();
            songDAO.update(song);

            logger.info("Added song {} to album {}", song.getName(), album.getName());

            return true;
        } catch (Exception e) {
            logger.error("Error adding song to album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public List<Song> getAllSongs() {
        try {
            logger.debug("Getting all songs");
            return songCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all songs: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Song getSongById(String id) {
        try {
            logger.debug("Getting song by ID: {}", id);
            return songCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting song by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<Song> searchSongsByName(String name) {
        try {
            logger.debug("Searching songs by name: {}", name);
            return songCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching songs by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Song> getSongsByArtist(String artistId) {
        try {
            logger.debug("Getting songs by artist ID: {}", artistId);
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return Collections.emptyList();
            }
            return songCollection.getByArtist(artist);
        } catch (Exception e) {
            logger.error("Error getting songs by artist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Song> getSongsByAlbum(String albumId) {
        try {
            logger.debug("Getting songs by album ID: {}", albumId);
            Album album = albumCollection.getById(albumId);
            if (album == null) {
                logger.warn("Album with ID {} not found", albumId);
                return Collections.emptyList();
            }
            return songCollection.getByAlbum(album);
        } catch (Exception e) {
            logger.error("Error getting songs by album: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Song> getSongsByGenre(String genre) {
        try {
            logger.debug("Getting songs by genre: {}", genre);
            return songCollection.getByGenre(genre);
        } catch (Exception e) {
            logger.error("Error getting songs by genre: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public boolean saveData(String artistFile, String albumFile, String songFile) {
        try {
            logger.info("Saving data to files: artist={}, album={}, song={}", artistFile, albumFile, songFile);
            boolean artistSaved = artistCollection.saveToFile(artistFile);
            boolean albumSaved = albumCollection.saveToFile(albumFile);
            boolean songSaved = songCollection.saveToFile(songFile);

            return artistSaved && albumSaved && songSaved;
        } catch (Exception e) {
            logger.error("Error saving data: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean loadData(String artistFile, String albumFile, String songFile, String playlistFile) {
        try {
            logger.info("Loading data from files: artist={}, album={}, song={}, playlist={}",
                    artistFile, albumFile, songFile, playlistFile);

            // Önce veritabanını kontrol et, eğer veriler varsa dosyalardan yükleme
            boolean hasData = false;

            // Basit kontrol: veritabanında kayıt var mı?
            try (Connection conn = DatabaseUtil.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM artists")) {

                if (rs.next() && rs.getInt(1) > 0) {
                    hasData = true;
                }
            } catch (SQLException e) {
                logger.error("Error checking database: {}", e.getMessage(), e);
            }

            // Eğer veritabanında veri yoksa dosyalardan yükle
            if (!hasData) {
                boolean artistLoaded = artistCollection.loadFromFile(artistFile);
                boolean albumLoaded = albumCollection.loadFromFile(albumFile);
                boolean songLoaded = songCollection.loadFromFile(songFile);
                boolean playlistLoaded = playlistCollection.loadFromFile(playlistFile);

                return artistLoaded || albumLoaded || songLoaded || playlistLoaded;
            }

            logger.info("Data already exists in database, skipping file import");
            // Zaten veritabanında veri var
            return true;
        } catch (Exception e) {
            logger.error("Error loading data: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean createPlaylist(String name, String description) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("create_playlist");
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Invalid playlist data: name is empty or null");
                return false;
            }

            Playlist playlist = new Playlist(name, description);
            playlistCollection.add(playlist);

            // Bu satırı ekleyelim - yeni oluşturulan playlist'in ID'sini loglayalım
            logger.info("Created new playlist: {} with ID: {}", name, playlist.getId());

            // Metriği artır
            MetricsCollector.getInstance().incrementPlaylistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error creating playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public List<Playlist> getAllPlaylists() {
        try {
            logger.debug("Getting all playlists");
            return playlistCollection.getAll();
        } catch (Exception e) {
            logger.error("Error getting all playlists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Playlist getPlaylistById(String id) {
        try {
            logger.debug("Getting playlist by ID: {}", id);
            return playlistCollection.getById(id);
        } catch (Exception e) {
            logger.error("Error getting playlist by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean removePlaylist(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                logger.warn("Invalid playlist ID for removal");
                return false;
            }

            logger.info("Removing playlist with ID: {}", id);

            // Get playlist for validation
            Playlist playlist = playlistCollection.getById(id);
            if (playlist == null) {
                logger.warn("Playlist with ID {} not found", id);
                return false;
            }

            // Delete playlist
            playlistCollection.remove(id);
            return true;
        } catch (Exception e) {
            logger.error("Error removing playlist: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Playlist> searchPlaylistsByName(String name) {
        try {
            logger.debug("Searching playlists by name: {}", name);
            return playlistCollection.searchByName(name);
        } catch (Exception e) {
            logger.error("Error searching playlists by name: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public boolean addSongToPlaylist(String songId, String playlistId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_song_to_playlist");
        try {
            Song song = songCollection.getById(songId);
            Playlist playlist = playlistCollection.getById(playlistId);

            if (song == null || playlist == null) {
                logger.warn("Failed to add song to playlist: song={}, playlist={}",
                        (song == null ? "null" : song.getId()),
                        (playlist == null ? "null" : playlist.getId()));
                return false;
            }

            playlist.addSong(song);
            logger.info("Added song {} to playlist {}", song.getName(), playlist.getName());
            return true;
        } catch (Exception e) {
            logger.error("Error adding song to playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public boolean removeSongFromPlaylist(String songId, String playlistId) {
        try {
            logger.info("Removing song {} from playlist {}", songId, playlistId);
            Song song = songCollection.getById(songId);
            Playlist playlist = playlistCollection.getById(playlistId);

            if (song == null || playlist == null) {
                logger.warn("Failed to remove song from playlist: song={}, playlist={}",
                        (song == null ? "null" : song.getId()),
                        (playlist == null ? "null" : playlist.getId()));
                return false;
            }

            playlist.removeSong(song);
            logger.info("Removed song {} from playlist {}", song.getName(), playlist.getName());
            return true;
        } catch (Exception e) {
            logger.error("Error removing song from playlist: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Song> getSongsInPlaylist(String playlistId) {
        try {
            if (playlistId == null || playlistId.trim().isEmpty()) {
                logger.warn("Invalid playlist ID for getSongsInPlaylist");
                return Collections.emptyList();
            }

            logger.debug("Getting songs for playlist {}", playlistId);

            Playlist playlist = playlistCollection.getById(playlistId);
            if (playlist == null) {
                logger.warn("Playlist with ID {} not found", playlistId);
                return Collections.emptyList();
            }

            // Mevcut implementasyonu kullan
            return playlist.getSongs();
        } catch (Exception e) {
            logger.error("Error getting songs in playlist: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public boolean saveData(String artistFile, String albumFile, String songFile, String playlistFile) {
        try {
            logger.info("Saving data to files: artist={}, album={}, song={}, playlist={}",
                    artistFile, albumFile, songFile, playlistFile);

            boolean artistSaved = artistCollection.saveToFile(artistFile);
            boolean albumSaved = albumCollection.saveToFile(albumFile);
            boolean songSaved = songCollection.saveToFile(songFile);
            boolean playlistSaved = playlistCollection.saveToFile(playlistFile);

            return artistSaved && albumSaved && songSaved && playlistSaved;
        } catch (Exception e) {
            logger.error("Error saving data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Removes an artist and all related albums and songs
     *
     * @param artistId ID of the artist to remove
     * @return true if the artist was removed successfully
     */
    public boolean removeArtist(String artistId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_artist");
        try {
            if (artistId == null || artistId.trim().isEmpty()) {
                logger.warn("Invalid artist ID for removal");
                return false;
            }

            logger.info("Removing artist with ID: {}", artistId);

            // Get artist for validation
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                logger.warn("Artist with ID {} not found", artistId);
                return false;
            }

            Connection conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                // 1. Sanatçının şarkılarını çalma listelerinden kaldır
                PreparedStatement removeSongsFromPlaylists = conn.prepareStatement(
                        "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE artist_id = ?)");
                removeSongsFromPlaylists.setString(1, artistId);
                int playlistSongsRemoved = removeSongsFromPlaylists.executeUpdate();
                removeSongsFromPlaylists.close();
                logger.debug("Removed {} songs from playlists", playlistSongsRemoved);

                // 2. Sanatçıya ait şarkıları sil
                PreparedStatement deleteSongs = conn.prepareStatement(
                        "DELETE FROM songs WHERE artist_id = ?");
                deleteSongs.setString(1, artistId);
                int songsRemoved = deleteSongs.executeUpdate();
                deleteSongs.close();
                logger.debug("Removed {} songs", songsRemoved);

                // 3. Sanatçıya ait albümleri sil
                PreparedStatement deleteAlbums = conn.prepareStatement(
                        "DELETE FROM albums WHERE artist_id = ?");
                deleteAlbums.setString(1, artistId);
                int albumsRemoved = deleteAlbums.executeUpdate();
                deleteAlbums.close();
                logger.debug("Removed {} albums", albumsRemoved);

                // 4. Sanatçıyı sil
                PreparedStatement deleteArtist = conn.prepareStatement(
                        "DELETE FROM artists WHERE id = ?");
                deleteArtist.setString(1, artistId);
                int artistRemoved = deleteArtist.executeUpdate();
                deleteArtist.close();
                logger.debug("Removed artist record: {}", artistRemoved > 0);

                // İşlemi tamamla
                conn.commit();
                logger.info("Successfully removed artist {} with {} albums and {} songs",
                        artist.getName(), albumsRemoved, songsRemoved);

                // Memory koleksiyonlarından da kaldır
                artistCollection.remove(artistId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                logger.error("Error removing artist from database: {}", e.getMessage(), e);
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            logger.error("Error removing artist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Removes an album and optionally its songs
     *
     * @param albumId ID of the album to remove
     * @param deleteSongs true to delete the album's songs, false to keep them
     * @return true if the album was removed successfully
     */
    public boolean removeAlbum(String albumId, boolean deleteSongs) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_album");
        try {
            if (albumId == null || albumId.trim().isEmpty()) {
                logger.warn("Invalid album ID for removal");
                return false;
            }

            logger.info("Removing album with ID: {}, deleteSongs={}", albumId, deleteSongs);

            // Get album for validation
            Album album = albumCollection.getById(albumId);
            if (album == null) {
                logger.warn("Album with ID {} not found", albumId);
                return false;
            }

            Connection conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                if (deleteSongs) {
                    // 1. Albümdeki şarkıları çalma listelerinden kaldır
                    PreparedStatement removeFromPlaylists = conn.prepareStatement(
                            "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE album_id = ?)");
                    removeFromPlaylists.setString(1, albumId);
                    int playlistSongsRemoved = removeFromPlaylists.executeUpdate();
                    removeFromPlaylists.close();
                    logger.debug("Removed {} playlist song references", playlistSongsRemoved);

                    // 2. Albümdeki tüm şarkıları sil
                    PreparedStatement deleteSongsStmt = conn.prepareStatement(
                            "DELETE FROM songs WHERE album_id = ?");
                    deleteSongsStmt.setString(1, albumId);
                    int songsRemoved = deleteSongsStmt.executeUpdate();
                    deleteSongsStmt.close();
                    logger.debug("Removed {} songs", songsRemoved);
                } else {
                    // Şarkıları silmeden albüm ilişkisini kaldır
                    PreparedStatement updateSongs = conn.prepareStatement(
                            "UPDATE songs SET album_id = NULL WHERE album_id = ?");
                    updateSongs.setString(1, albumId);
                    int songsUpdated = updateSongs.executeUpdate();
                    updateSongs.close();
                    logger.debug("Updated {} songs (removed album reference)", songsUpdated);
                }

                // 3. Albümü sil
                PreparedStatement deleteAlbum = conn.prepareStatement(
                        "DELETE FROM albums WHERE id = ?");
                deleteAlbum.setString(1, albumId);
                int albumRemoved = deleteAlbum.executeUpdate();
                deleteAlbum.close();
                logger.debug("Removed album record: {}", albumRemoved > 0);

                // İşlemi tamamla
                conn.commit();
                logger.info("Successfully removed album {}", album.getName());

                // Memory koleksiyondan da kaldır
                albumCollection.remove(albumId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                logger.error("Error removing album from database: {}", e.getMessage(), e);
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            logger.error("Error removing album: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Removes a song and its references from playlists
     *
     * @param songId ID of the song to remove
     * @return true if the song was removed successfully
     */
    public boolean removeSong(String songId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("remove_song");
        try {
            if (songId == null || songId.trim().isEmpty()) {
                logger.warn("Invalid song ID for removal");
                return false;
            }

            logger.info("Removing song with ID: {}", songId);

            // Get song for validation
            Song song = songCollection.getById(songId);
            if (song == null) {
                logger.warn("Song with ID {} not found", songId);
                return false;
            }

            Connection conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                // 1. Şarkıyı çalma listelerinden kaldır
                PreparedStatement removeFromPlaylists = conn.prepareStatement(
                        "DELETE FROM playlist_songs WHERE song_id = ?");
                removeFromPlaylists.setString(1, songId);
                int playlistRefsRemoved = removeFromPlaylists.executeUpdate();
                removeFromPlaylists.close();
                logger.debug("Removed {} playlist references", playlistRefsRemoved);

                // 2. Şarkıyı sil
                PreparedStatement deleteSong = conn.prepareStatement(
                        "DELETE FROM songs WHERE id = ?");
                deleteSong.setString(1, songId);
                int songRemoved = deleteSong.executeUpdate();
                deleteSong.close();
                logger.debug("Removed song record: {}", songRemoved > 0);

                // İşlemi tamamla
                conn.commit();
                logger.info("Successfully removed song {}", song.getName());

                // Memory koleksiyondan da kaldır
                songCollection.remove(songId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                logger.error("Error removing song from database: {}", e.getMessage(), e);
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            logger.error("Error removing song: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Sanatçı listesinden duplikasyonları temizler
     *
     * @param artists Duplikasyonları temizlenecek sanatçı listesi
     * @return Duplikasyonları temizlenmiş yeni liste
     */
    private List<Artist> removeDuplicateArtists(List<Artist> artists) {
        try {
            logger.debug("Removing duplicates from artist list with {} entries", artists.size());
            Map<String, Artist> uniqueArtists = new HashMap<>();

            for (Artist artist : artists) {
                uniqueArtists.put(artist.getId(), artist);
            }

            logger.debug("After duplicate removal: {} unique artists", uniqueArtists.size());
            return new ArrayList<>(uniqueArtists.values());
        } catch (Exception e) {
            logger.error("Error removing duplicate artists: {}", e.getMessage(), e);
            return artists; // Hata durumunda orijinal listeyi döndür
        }
    }

    public List<Artist> getAllArtists() {
        try {
            logger.debug("Getting all artists");
            // Sanatçı listesini al ve duplikasyonları temizle
            List<Artist> allArtists = artistCollection.getAll();

            // Unique olan sanatçıları elde etmek için bir Map kullan
            Map<String, Artist> uniqueArtistsById = new HashMap<>();
            for (Artist artist : allArtists) {
                uniqueArtistsById.put(artist.getId(), artist);
            }

            // ID'ye göre filtrelenmiş listeyi döndür
            List<Artist> uniqueArtists = new ArrayList<>(uniqueArtistsById.values());

            logger.info("GetAllArtists returning {} unique artists", uniqueArtists.size());
            return uniqueArtists;
        } catch (Exception e) {
            logger.error("Error getting all artists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public boolean addPlaylist(Playlist playlist) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("add_playlist");
        try {
            if (playlist == null) {
                logger.warn("Cannot add null playlist");
                return false;
            }

            logger.info("Adding playlist with ID: {}, name: {}", playlist.getId(), playlist.getName());
            playlistCollection.add(playlist);

            // Metriği artır
            MetricsCollector.getInstance().incrementPlaylistAdded();
            return true;
        } catch (Exception e) {
            logger.error("Error adding playlist: {}", e.getMessage(), e);
            return false;
        } finally {
            timer.observeDuration();
        }
    }

    public void reinitializeDatabase() {
        try {
            // Veritabanını yeniden başlat
            DatabaseUtil.setShouldResetDatabase(true);
            DatabaseUtil.initializeDatabase();

            // Koleksiyonları temizle ve yeniden yükle
            artistCollection.clear();
            albumCollection.clear();
            songCollection.clear();
            playlistCollection.clear();

            logger.info("Database and collections reinitialized successfully");
        } catch (Exception e) {
            logger.error("Error reinitializing database: {}", e.getMessage(), e);
        }
    }
}