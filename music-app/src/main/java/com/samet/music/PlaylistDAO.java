package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    private SongDAO songDAO = new SongDAO();
    private static final Object LOCK = new Object();


    public void insert(Playlist playlist) {
        synchronized (LOCK) {
            String sql = "INSERT INTO playlists (id, name, description) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlist.getId());
                pstmt.setString(2, playlist.getName());
                pstmt.setString(3, playlist.getDescription());

                pstmt.executeUpdate();

                // Şarkıları playlist_songs tablosuna ekle
                for (Song song : playlist.getSongs()) {
                    insertPlaylistSong(playlist.getId(), song.getId());
                }
            } catch (SQLException e) {
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

            String sql = "SELECT * FROM playlists WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Playlist playlist = new Playlist(rs.getString("name"));
                        playlist.setDescription(rs.getString("description"));

                        // Playlistin şarkılarını getir
                        List<Song> songs = getPlaylistSongs(id);
                        for (Song song : songs) {
                            playlist.addSong(song);
                        }

                        return playlist;
                    }
                }
            } catch (SQLException e) {
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
                    Playlist playlist = new Playlist(rs.getString("name"));
                    playlist.setDescription(rs.getString("description"));

                    // Playlistin şarkılarını getir
                    List<Song> songs = getPlaylistSongs(rs.getString("id"));
                    for (Song song : songs) {
                        playlist.addSong(song);
                    }

                    playlists.add(playlist);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return playlists;
        }
    }

    public void update(Playlist playlist) {
        synchronized (LOCK) {

            String sql = "UPDATE playlists SET name = ?, description = ? WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, playlist.getName());
                pstmt.setString(2, playlist.getDescription());
                pstmt.setString(3, playlist.getId());

                pstmt.executeUpdate();

                // Önce mevcut şarkıları sil
                deletePlaylistSongs(playlist.getId());

                // Yeni şarkıları ekle
                for (Song song : playlist.getSongs()) {
                    insertPlaylistSong(playlist.getId(), song.getId());
                }
            } catch (SQLException e) {
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

            // Önce playlist_songs tablosundan şarkıları sil
            deletePlaylistSongs(id);

            // Sonra playlist'i sil
            String sql = "DELETE FROM playlists WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addSongToPlaylist(String playlistId, String songId) {
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