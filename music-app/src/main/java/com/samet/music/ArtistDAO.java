package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtistDAO {
    public void insert(Artist artist) {
        String sql = "INSERT INTO artists (id, name, biography) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, artist.getId());
            pstmt.setString(2, artist.getName());
            pstmt.setString(3, artist.getBiography());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Artist getById(String id) {
        String sql = "SELECT * FROM artists WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Artist artist = new Artist(rs.getString("name"));
                artist.setBiography(rs.getString("biography"));
                return artist;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Artist> getAll() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM artists";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Artist artist = new Artist(rs.getString("name"));
                artist.setBiography(rs.getString("biography"));
                artists.add(artist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return artists;
    }

    public void update(Artist artist) {
        String sql = "UPDATE artists SET name = ?, biography = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getBiography());
            pstmt.setString(3, artist.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM artists WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}