package com.samet.music.dao;

import com.samet.music.model.BaseEntity;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import com.samet.music.util.DatabaseUtil;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    private SongDAO songDAO = new SongDAO();
    private static final Object LOCK = new Object();


    // PlaylistDAO sınıfında
    public void insert(Playlist playlist) {
        synchronized (LOCK) {
            try {
                // Aynı ID'li playlist var mı kontrol et
                String checkSql = "SELECT COUNT(*) FROM playlists WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setString(1, playlist.getId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            // Bu ID ile playlist var, güncelle
                            update(playlist);
                            return;
                        }
                    }
                }

                // Playlist'i ekle
                String sql = "INSERT INTO playlists (id, name, description) VALUES (?, ?, ?)";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, playlist.getId());
                    pstmt.setString(2, playlist.getName());
                    pstmt.setString(3, playlist.getDescription());
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("Inserted playlist with ID: " + playlist.getId() +
                            ", affected rows: " + affectedRows);
                }

                // Şarkıları ekle
                for (Song song : playlist.getSongs()) {
                    insertPlaylistSong(playlist.getId(), song.getId());
                }
            } catch (SQLException e) {
                System.err.println("Error inserting playlist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void insertPlaylistSong(String playlistId, String songId) {
        synchronized (LOCK) {

            String sql = "INSERT OR IGNORE INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlistId);
                pstmt.setString(2, songId);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Playlist getById(String id) {
        synchronized (LOCK) {
            if (id == null || id.trim().isEmpty()) {
                System.err.println("Cannot get playlist with null or empty ID");
                return null;
            }

            System.out.println("Looking for playlist with ID: " + id);

            String sql = "SELECT * FROM playlists WHERE id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String playlistId = rs.getString("id");
                        String name = rs.getString("name");
                        String description = rs.getString("description");

                        System.out.println("Found playlist: " + name + " with ID: " + playlistId);

                        // ID'si korunan bir Playlist nesnesi oluşturalım
                        Playlist playlist = new Playlist(name, description);
                        // ID'yi açıkça ayarlayalım
                        ((BaseEntity)playlist).setId(playlistId);

                        // Şarkıları getir
                        List<Song> songs = getPlaylistSongs(playlistId);
                        for (Song song : songs) {
                            playlist.addSong(song);
                        }

                        return playlist;
                    } else {
                        System.out.println("No playlist found with ID: " + id);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error getting playlist by ID: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }
    private List<Song> getPlaylistSongs(String playlistId) {
        synchronized (LOCK) {

            List<Song> songs = new ArrayList<>();
            String sql = "SELECT song_id FROM playlist_songs WHERE playlist_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlistId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Song song = songDAO.getById(rs.getString("song_id"));
                        if (song != null) {
                            songs.add(song);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return songs;
        }
    }

    public List<Playlist> getAll() {
        synchronized (LOCK) {
            List<Playlist> playlists = new ArrayList<>();
            String sql = "SELECT * FROM playlists";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");

                    // ID'si korunan Playlist nesnesi oluştur
                    Playlist playlist = new Playlist(name, description);
                    // ID'yi açıkça ayarla
                    ((BaseEntity)playlist).setId(id);

                    // Playlistin şarkılarını getir
                    List<Song> songs = getPlaylistSongs(id);
                    for (Song song : songs) {
                        playlist.addSong(song);
                    }

                    playlists.add(playlist);
                    System.out.println("Loaded playlist: " + name + " with ID: " + id);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return playlists;
        }
    }

    public void update(Playlist playlist) {
        synchronized (LOCK) {
            try {
                if (playlist == null) {
                    System.err.println("Cannot update null playlist");
                    return;
                }

                String playlistId = playlist.getId();
                if (playlistId == null || playlistId.trim().isEmpty()) {
                    System.err.println("Cannot update playlist with null or empty ID");
                    return;
                }

                System.out.println("Updating playlist with ID: " + playlistId + ", new name: " + playlist.getName() +
                        ", new description: " + playlist.getDescription());

                // Playlist bilgilerini güncelle
                String sql = "UPDATE playlists SET name = ?, description = ? WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, playlist.getName());
                    pstmt.setString(2, playlist.getDescription());
                    pstmt.setString(3, playlistId);
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("Updated playlist in database. Affected rows: " + affectedRows);

                    if (affectedRows == 0) {
                        System.err.println("Update failed. No rows affected. Playlist with ID " +
                                playlistId + " might not exist in database.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error updating playlist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void deletePlaylistSongs(String playlistId) {
        synchronized (LOCK) {

            String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlistId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(String id) {
        synchronized (LOCK) {
            try {
                // ID kontrolü yapalım
                if (id == null || id.trim().isEmpty()) {
                    System.err.println("Playlist ID is null or empty");
                    return;
                }

                // Playlist'in var olup olmadığını kontrol edelim
                String checkSql = "SELECT COUNT(*) FROM playlists WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setString(1, id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.err.println("Playlist with ID " + id + " not found");
                            return;
                        }
                    }
                }

                // Önce playlist_songs tablosundan şarkıları silelim
                String deleteSongsSQL = "DELETE FROM playlist_songs WHERE playlist_id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(deleteSongsSQL)) {
                    pstmt.setString(1, id);
                    pstmt.executeUpdate();
                    System.out.println("Removed songs from playlist " + id);
                }

                // Sonra playlist'i silelim
                String deletePlaylistSQL = "DELETE FROM playlists WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(deletePlaylistSQL)) {
                    pstmt.setString(1, id);
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("Deleted playlist " + id + ", affected rows: " + affectedRows);
                }
            } catch (SQLException e) {
                System.err.println("Error deleting playlist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void addSongToPlaylist(String playlistId, String songId) {
        synchronized (LOCK) {
            if (playlistId == null || playlistId.trim().isEmpty() || songId == null || songId.trim().isEmpty()) {
                System.err.println("PlaylistDAO: Invalid playlist ID or song ID");
                return;
            }

            System.out.println("PlaylistDAO: Adding song " + songId + " to playlist " + playlistId);

            try {
                // Playlist'in varlığını kontrol et
                String checkPlaylistSql = "SELECT COUNT(*) FROM playlists WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(checkPlaylistSql)) {
                    pstmt.setString(1, playlistId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.err.println("PlaylistDAO: Playlist with ID " + playlistId + " not found in database");
                            return;
                        }
                    }
                }

                // Şarkının varlığını kontrol et
                String checkSongSql = "SELECT COUNT(*) FROM songs WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(checkSongSql)) {
                    pstmt.setString(1, songId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.err.println("PlaylistDAO: Song with ID " + songId + " not found in database");
                            return;
                        }
                    }
                }

                // Şarkıyı playlist'e ekle
                String sql = "INSERT OR REPLACE INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, playlistId);
                    pstmt.setString(2, songId);
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("PlaylistDAO: Added song " + songId + " to playlist " + playlistId +
                            ", affected rows: " + affectedRows);
                }
            } catch (SQLException e) {
                System.err.println("PlaylistDAO: Error adding song to playlist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void removeSongFromPlaylist(String playlistId, String songId) {
        synchronized (LOCK) {

            String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlistId);
                pstmt.setString(2, songId);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Playlist> getPlaylistsContainingSong(Song song) {
        synchronized (LOCK) {

            List<Playlist> playlists = new ArrayList<>();
            String sql = "SELECT playlist_id FROM playlist_songs WHERE song_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, song.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Playlist playlist = getById(rs.getString("playlist_id"));
                        if (playlist != null) {
                            playlists.add(playlist);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return playlists;
        }
    }
}