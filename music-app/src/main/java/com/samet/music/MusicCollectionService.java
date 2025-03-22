package com.samet.music;

import java.sql.*;
import java.util.Collections;
import java.util.List;

/**
 * Service class for music collection operations
 * Implements Singleton pattern
 */
public class MusicCollectionService {
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
    }

    public static synchronized MusicCollectionService getInstance() {
        if (instance == null) {
            instance = new MusicCollectionService();
        }
        return instance;
    }

    // Artist operations
    public boolean addArtist(String name, String biography) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Artist artist = musicFactory.createArtist(name, biography);
        artistCollection.add(artist);
        return true;
    }

    public List<Artist> getAllArtists() {
        return artistCollection.getAll();
    }

    public Artist getArtistById(String id) {
        return artistCollection.getById(id);
    }


    public List<Artist> searchArtistsByName(String name) {
        return artistCollection.searchByName(name);
    }

    // Album operations
    public boolean addAlbum(String name, String artistId, int releaseYear, String genre) {
        if (name == null || name.trim().isEmpty() || releaseYear <= 0) {
            return false;
        }

        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return false;
        }

        Album album = musicFactory.createAlbum(name, artist, releaseYear, genre);
        albumCollection.add(album);
        return true;
    }

    public List<Album> getAllAlbums() {
        return albumCollection.getAll();
    }

    public Album getAlbumById(String id) {
        return albumCollection.getById(id);
    }

    public boolean removeAlbum(String id) {
        return albumCollection.remove(id);
    }

    public List<Album> searchAlbumsByName(String name) {
        return albumCollection.searchByName(name);
    }

    public List<Album> getAlbumsByArtist(String artistId) {
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return Collections.emptyList();
        }
        return albumCollection.getByArtist(artist);
    }

    public List<Album> getAlbumsByGenre(String genre) {
        return albumCollection.getByGenre(genre);
    }

    public List<Playlist> getPlaylistsContainingSong(String songId) {
        if (songId == null || songId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return playlistCollection.getPlaylistsContainingSong(songDAO.getById(songId));
    }

    // Song operations
    public boolean addSong(String name, String artistId, int duration, String genre) {
        try {
            if (name == null || name.trim().isEmpty() || duration <= 0) {
                System.err.println("Invalid song data. Name cannot be empty and duration must be positive.");
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

                    System.out.println("Artist found directly in database: " + artist.getName() + ", ID: " + artist.getId());
                } else {
                    System.err.println("Artist with ID " + artistId + " not found in database.");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Error checking artist in database: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            if (artist == null) {
                System.err.println("Artist with ID " + artistId + " not found.");
                return false;
            }

            System.out.println("Creating song with name: " + name + ", artist: " + artist.getName() +
                    ", duration: " + duration + ", genre: " + genre);

            Song song = musicFactory.createSong(name, artist, duration, genre);
            songCollection.add(song);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding song: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSongToAlbum(String songId, String albumId) {
        Song song = songCollection.getById(songId);
        Album album = albumCollection.getById(albumId);

        if (song == null || album == null) {
            return false;
        }

        song.setAlbum(album);
        return true;
    }

    public List<Song> getAllSongs() {
        return songCollection.getAll();
    }

    public Song getSongById(String id) {
        return songCollection.getById(id);
    }


    public List<Song> searchSongsByName(String name) {
        return songCollection.searchByName(name);
    }

    public List<Song> getSongsByArtist(String artistId) {
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return Collections.emptyList();
        }
        return songCollection.getByArtist(artist);
    }

    public List<Song> getSongsByAlbum(String albumId) {
        Album album = albumCollection.getById(albumId);
        if (album == null) {
            return Collections.emptyList();
        }
        return songCollection.getByAlbum(album);
    }

    public List<Song> getSongsByGenre(String genre) {
        return songCollection.getByGenre(genre);
    }

    public boolean saveData(String artistFile, String albumFile, String songFile) {
        boolean artistSaved = artistCollection.saveToFile(artistFile);
        boolean albumSaved = albumCollection.saveToFile(albumFile);
        boolean songSaved = songCollection.saveToFile(songFile);

        return artistSaved && albumSaved && songSaved;
    }

    public boolean loadData(String artistFile, String albumFile, String songFile, String playlistFile) {
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
            System.err.println("Error checking database: " + e.getMessage());
        }

        // Eğer veritabanında veri yoksa dosyalardan yükle
        if (!hasData) {
            boolean artistLoaded = artistCollection.loadFromFile(artistFile);
            boolean albumLoaded = albumCollection.loadFromFile(albumFile);
            boolean songLoaded = songCollection.loadFromFile(songFile);
            boolean playlistLoaded = playlistCollection.loadFromFile(playlistFile);

            return artistLoaded || albumLoaded || songLoaded || playlistLoaded;
        }

        // Zaten veritabanında veri var
        return true;
    }

    public boolean createPlaylist(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Playlist playlist = new Playlist(name, description);
        playlistCollection.add(playlist);
        return true;
    }

    public List<Playlist> getAllPlaylists() {
        return playlistCollection.getAll();
    }

    public Playlist getPlaylistById(String id) {
        return playlistCollection.getById(id);
    }

    public boolean removePlaylist(String id) {
        return playlistCollection.remove(id);
    }

    public List<Playlist> searchPlaylistsByName(String name) {
        return playlistCollection.searchByName(name);
    }

    public boolean addSongToPlaylist(String songId, String playlistId) {
        Song song = songCollection.getById(songId);
        Playlist playlist = playlistCollection.getById(playlistId);

        if (song == null || playlist == null) {
            return false;
        }

        playlist.addSong(song);
        return true;
    }

    public boolean removeSongFromPlaylist(String songId, String playlistId) {
        Song song = songCollection.getById(songId);
        Playlist playlist = playlistCollection.getById(playlistId);

        if (song == null || playlist == null) {
            return false;
        }

        playlist.removeSong(song);
        return true;
    }

    public List<Song> getSongsInPlaylist(String playlistId) {
        Playlist playlist = playlistCollection.getById(playlistId);
        if (playlist == null) {
            return Collections.emptyList();
        }

        return playlist.getSongs();
    }

    public boolean saveData(String artistFile, String albumFile, String songFile, String playlistFile) {
        boolean artistSaved = artistCollection.saveToFile(artistFile);
        boolean albumSaved = albumCollection.saveToFile(albumFile);
        boolean songSaved = songCollection.saveToFile(songFile);
        boolean playlistSaved = playlistCollection.saveToFile(playlistFile);

        return artistSaved && albumSaved && songSaved && playlistSaved;
    }

    /**
     * Removes an artist and all related albums and songs
     *
     * @param artistId ID of the artist to remove
     * @return true if the artist was removed successfully
     */
    public boolean removeArtist(String artistId) {
        if (artistId == null || artistId.trim().isEmpty()) {
            return false;
        }

        try {
            // Get artist for validation
            Artist artist = artistCollection.getById(artistId);
            if (artist == null) {
                return false;
            }

            Connection conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                // 1. Sanatçının şarkılarını çalma listelerinden kaldır
                PreparedStatement removeSongsFromPlaylists = conn.prepareStatement(
                        "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE artist_id = ?)");
                removeSongsFromPlaylists.setString(1, artistId);
                removeSongsFromPlaylists.executeUpdate();
                removeSongsFromPlaylists.close();

                // 2. Sanatçıya ait şarkıları sil
                PreparedStatement deleteSongs = conn.prepareStatement(
                        "DELETE FROM songs WHERE artist_id = ?");
                deleteSongs.setString(1, artistId);
                deleteSongs.executeUpdate();
                deleteSongs.close();

                // 3. Sanatçıya ait albümleri sil
                PreparedStatement deleteAlbums = conn.prepareStatement(
                        "DELETE FROM albums WHERE artist_id = ?");
                deleteAlbums.setString(1, artistId);
                deleteAlbums.executeUpdate();
                deleteAlbums.close();

                // 4. Sanatçıyı sil
                PreparedStatement deleteArtist = conn.prepareStatement(
                        "DELETE FROM artists WHERE id = ?");
                deleteArtist.setString(1, artistId);
                deleteArtist.executeUpdate();
                deleteArtist.close();

                // İşlemi tamamla
                conn.commit();

                // Memory koleksiyonlarından da kaldır
                artistCollection.remove(artistId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.err.println("Error removing artist: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error removing artist: " + e.getMessage());
            e.printStackTrace();
            return false;
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
        if (albumId == null || albumId.trim().isEmpty()) {
            return false;
        }

        try {
            // Get album for validation
            Album album = albumCollection.getById(albumId);
            if (album == null) {
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
                    removeFromPlaylists.executeUpdate();
                    removeFromPlaylists.close();

                    // 2. Albümdeki tüm şarkıları sil
                    PreparedStatement deleteSongsStmt = conn.prepareStatement(
                            "DELETE FROM songs WHERE album_id = ?");
                    deleteSongsStmt.setString(1, albumId);
                    deleteSongsStmt.executeUpdate();
                    deleteSongsStmt.close();
                } else {
                    // Şarkıları silmeden albüm ilişkisini kaldır
                    PreparedStatement updateSongs = conn.prepareStatement(
                            "UPDATE songs SET album_id = NULL WHERE album_id = ?");
                    updateSongs.setString(1, albumId);
                    updateSongs.executeUpdate();
                    updateSongs.close();
                }

                // 3. Albümü sil
                PreparedStatement deleteAlbum = conn.prepareStatement(
                        "DELETE FROM albums WHERE id = ?");
                deleteAlbum.setString(1, albumId);
                deleteAlbum.executeUpdate();
                deleteAlbum.close();

                // İşlemi tamamla
                conn.commit();

                // Memory koleksiyondan da kaldır
                albumCollection.remove(albumId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.err.println("Error removing album: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error removing album: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a song and its references from playlists
     *
     * @param songId ID of the song to remove
     * @return true if the song was removed successfully
     */
    public boolean removeSong(String songId) {
        if (songId == null || songId.trim().isEmpty()) {
            return false;
        }

        try {
            // Get song for validation
            Song song = songCollection.getById(songId);
            if (song == null) {
                return false;
            }

            Connection conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            try {
                // 1. Şarkıyı çalma listelerinden kaldır
                PreparedStatement removeFromPlaylists = conn.prepareStatement(
                        "DELETE FROM playlist_songs WHERE song_id = ?");
                removeFromPlaylists.setString(1, songId);
                removeFromPlaylists.executeUpdate();
                removeFromPlaylists.close();

                // 2. Şarkıyı sil
                PreparedStatement deleteSong = conn.prepareStatement(
                        "DELETE FROM songs WHERE id = ?");
                deleteSong.setString(1, songId);
                deleteSong.executeUpdate();
                deleteSong.close();

                // İşlemi tamamla
                conn.commit();

                // Memory koleksiyondan da kaldır
                songCollection.remove(songId);

                return true;
            } catch (SQLException e) {
                // Hata durumunda geri al
                conn.rollback();
                System.err.println("Error removing song: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error removing song: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}