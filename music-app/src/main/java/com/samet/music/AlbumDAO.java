package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private ArtistDAO artistDAO = new ArtistDAO();

    public void insert(Album album) {
        String sql = "INSERT INTO albums (id, name, artist_id, release_year, genre) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, album.getId());
            pstmt.setString(2, album.getName());
            pstmt.setString(3, album.getArtist() != null ? album.getArtist().getId() : null);
            pstmt.setInt(4, album.getReleaseYear());
            pstmt.setString(5, album.getGenre());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Album getById(String id) {
        String sql = "SELECT * FROM albums WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Artist artist = artistDAO.getById(rs.getString("artist_id"));
                Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                album.setGenre(rs.getString("genre"));
                return album;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Album> getAll() {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Artist artist = artistDAO.getById(rs.getString("artist_id"));
                Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                album.setGenre(rs.getString("genre"));
                albums.add(album);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return albums;
    }

    public void update(Album album) {
        String sql = "UPDATE albums SET name = ?, artist_id = ?, release_year = ?, genre = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, album.getName());
            pstmt.setString(2, album.getArtist() != null ? album.getArtist().getId() : null);
            pstmt.setInt(3, album.getReleaseYear());
            pstmt.setString(4, album.getGenre());
            pstmt.setString(5, album.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM albums WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Album> getByArtist(Artist artist) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, artist.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                album.setGenre(rs.getString("genre"));
                albums.add(album);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return albums;
    }
}