package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlbumDAO {
    private ArtistDAO artistDAO = new ArtistDAO();
    private static final Object LOCK = new Object();


    public void insert(Album album) {
        synchronized (LOCK) {
            String checkSql = "SELECT count(*) FROM albums WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, album.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Bu ID zaten var, update yapılabilir
                        update(album);
                        return;
                    }
                }

                // ID yoksa insert yap
                String sql = "INSERT INTO albums (id, name, artist_id, release_year, genre) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, album.getId());
                    pstmt.setString(2, album.getName());
                    pstmt.setString(3, album.getArtist() != null ? album.getArtist().getId() : null);
                    pstmt.setInt(4, album.getReleaseYear());
                    pstmt.setString(5, album.getGenre());

                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Album getById(String id) {
        synchronized (LOCK) {

            String sql = "SELECT * FROM albums WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Artist artist = artistDAO.getById(rs.getString("artist_id"));
                        Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                        album.setGenre(rs.getString("genre"));
                        return album;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public List<Album> getAll() {
        synchronized (LOCK) {

            List<Album> albums = new ArrayList<>();
            String sql = "SELECT * FROM albums";

            // Album ID'lerini takip etmek için kullanılacak set
            Set<String> processedIds = new HashSet<>();

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");

                    // Eğer bu ID daha önce işlendiyse, atla
                    if (processedIds.contains(id)) {
                        continue;
                    }

                    Artist artist = artistDAO.getById(rs.getString("artist_id"));
                    Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                    album.setGenre(rs.getString("genre"));
                    albums.add(album);

                    // ID'yi işlenmiş olarak işaretle
                    processedIds.add(id);
                }

                System.out.println("Retrieved " + albums.size() + " unique albums from database.");
            } catch (SQLException e) {
                System.err.println("Error retrieving all albums: " + e.getMessage());
                e.printStackTrace();
            }

            return albums;
        }
    }

    public void update(Album album) {
        synchronized (LOCK) {
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
    }

    public void delete(String id) {
        synchronized (LOCK) {
            String sql = "DELETE FROM albums WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Album> getByArtist(Artist artist) {
        synchronized (LOCK) {
            List<Album> albums = new ArrayList<>();
            String sql = "SELECT * FROM albums WHERE artist_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, artist.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Album album = new Album(rs.getString("name"), artist, rs.getInt("release_year"));
                        album.setGenre(rs.getString("genre"));
                        albums.add(album);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return albums;
        }
    }
}