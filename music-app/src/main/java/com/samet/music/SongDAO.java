package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private ArtistDAO artistDAO = new ArtistDAO();
    private AlbumDAO albumDAO = new AlbumDAO();

    public void insert(Song song) {
        String sql = "INSERT INTO songs (id, name, artist_id, album_id, duration, genre) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getId());
            pstmt.setString(2, song.getName());
            pstmt.setString(3, song.getArtist() != null ? song.getArtist().getId() : null);
            pstmt.setString(4, song.getAlbum() != null ? song.getAlbum().getId() : null);
            pstmt.setInt(5, song.getDuration());
            pstmt.setString(6, song.getGenre());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Song getById(String id) {
        String sql = "SELECT * FROM songs WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Artist artist = artistDAO.getById(rs.getString("artist_id"));
                Album album = albumDAO.getById(rs.getString("album_id"));

                Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                song.setAlbum(album);
                song.setGenre(rs.getString("genre"));
                return song;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Song> getAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Artist artist = artistDAO.getById(rs.getString("artist_id"));
                Album album = albumDAO.getById(rs.getString("album_id"));

                Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                song.setAlbum(album);
                song.setGenre(rs.getString("genre"));
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public void update(Song song) {
        String sql = "UPDATE songs SET name = ?, artist_id = ?, album_id = ?, duration = ?, genre = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getName());
            pstmt.setString(2, song.getArtist() != null ? song.getArtist().getId() : null);
            pstmt.setString(3, song.getAlbum() != null ? song.getAlbum().getId() : null);
            pstmt.setInt(4, song.getDuration());
            pstmt.setString(5, song.getGenre());
            pstmt.setString(6, song.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM songs WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Song> getByArtist(Artist artist) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE artist_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, artist.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Album album = albumDAO.getById(rs.getString("album_id"));

                Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                song.setAlbum(album);
                song.setGenre(rs.getString("genre"));
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public List<Song> getByAlbum(Album album) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE album_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, album.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Artist artist = artistDAO.getById(rs.getString("artist_id"));

                Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                song.setAlbum(album);
                song.setGenre(rs.getString("genre"));
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }
}