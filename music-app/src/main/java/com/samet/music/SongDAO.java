package com.samet.music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongDAO {
    private ArtistDAO artistDAO = new ArtistDAO();
    private AlbumDAO albumDAO = new AlbumDAO();
    private static final Object LOCK = new Object();


    public void insert(Song song) {
        synchronized (LOCK) {

            // Önce bu ID ile bir kayıt var mı kontrol et
            String checkSql = "SELECT count(*) FROM songs WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, song.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Bu ID zaten var, update yapılabilir
                        update(song);
                        return;
                    }
                }

                // ID yoksa insert yap
                String sql = "INSERT INTO songs (id, name, artist_id, album_id, duration, genre) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, song.getId());
                    pstmt.setString(2, song.getName());

                    String artistId = song.getArtist() != null ? song.getArtist().getId() : null;

                    // Sanatçı ID'sini veritabanında doğrula
                    if (artistId != null) {
                        try (PreparedStatement checkArtist = conn.prepareStatement("SELECT * FROM artists WHERE id = ?")) {
                            checkArtist.setString(1, artistId);
                            try (ResultSet artistRs = checkArtist.executeQuery()) {
                                if (!artistRs.next()) {
                                    // Sanatçıyı otomatik olarak veritabanına ekle
                                    try (PreparedStatement insertArtist = conn.prepareStatement(
                                            "INSERT INTO artists (id, name, biography) VALUES (?, ?, ?)")) {
                                        insertArtist.setString(1, artistId);
                                        insertArtist.setString(2, song.getArtist().getName());
                                        insertArtist.setString(3, song.getArtist().getBiography());
                                        insertArtist.executeUpdate();
                                    }
                                }
                            }
                        }
                    }

                    pstmt.setString(3, artistId);

                    String albumId = song.getAlbum() != null ? song.getAlbum().getId() : null;
                    pstmt.setString(4, albumId);

                    pstmt.setInt(5, song.getDuration());
                    pstmt.setString(6, song.getGenre());

                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                // Sessizce loglama
                System.err.println("Error inserting song: " + e.getMessage());
                throw new RuntimeException("Failed to add song to database", e);
            }
        }
    }

    public Song getById(String id) {
        synchronized (LOCK) {

            String sql = "SELECT * FROM songs WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Artist artist = artistDAO.getById(rs.getString("artist_id"));
                        Album album = albumDAO.getById(rs.getString("album_id"));

                        Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                        song.setAlbum(album);
                        song.setGenre(rs.getString("genre"));
                        return song;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving song by ID: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }

    public List<Song> getAll() {
        synchronized (LOCK) {

            List<Song> songs = new ArrayList<>();
            String sql = "SELECT * FROM songs";

            // Şarkı ID'lerini takip etmek için kullanılacak set
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
                    Album album = albumDAO.getById(rs.getString("album_id"));

                    Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                    song.setAlbum(album);
                    song.setGenre(rs.getString("genre"));
                    songs.add(song);

                    // ID'yi işlenmiş olarak işaretle
                    processedIds.add(id);
                }

                System.out.println("Retrieved " + songs.size() + " unique songs from database.");
            } catch (SQLException e) {
                System.err.println("Error retrieving all songs: " + e.getMessage());
                e.printStackTrace();
            }

            return songs;
        }
    }

    public void update(Song song) {
        synchronized (LOCK) {

            String sql = "UPDATE songs SET name = ?, artist_id = ?, album_id = ?, duration = ?, genre = ? WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                System.out.println("Updating song: " + song.getName() + ", ID: " + song.getId());

                pstmt.setString(1, song.getName());
                pstmt.setString(2, song.getArtist() != null ? song.getArtist().getId() : null);
                pstmt.setString(3, song.getAlbum() != null ? song.getAlbum().getId() : null);
                pstmt.setInt(4, song.getDuration());
                pstmt.setString(5, song.getGenre());
                pstmt.setString(6, song.getId());

                int result = pstmt.executeUpdate();
                System.out.println("Update result: " + result + " row(s) affected");

            } catch (SQLException e) {
                System.err.println("Error updating song: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void delete(String id) {
        synchronized (LOCK) {

            String sql = "DELETE FROM songs WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                int result = pstmt.executeUpdate();
                System.out.println("Delete result: " + result + " row(s) affected");

            } catch (SQLException e) {
                System.err.println("Error deleting song: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<Song> getByArtist(Artist artist) {
        synchronized (LOCK) {

            List<Song> songs = new ArrayList<>();
            String sql = "SELECT * FROM songs WHERE artist_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, artist.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Album album = albumDAO.getById(rs.getString("album_id"));

                        Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                        song.setAlbum(album);
                        song.setGenre(rs.getString("genre"));
                        songs.add(song);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving songs by artist: " + e.getMessage());
                e.printStackTrace();
            }

            return songs;
        }
    }

    public List<Song> getByAlbum(Album album) {
        synchronized (LOCK) {

            List<Song> songs = new ArrayList<>();
            String sql = "SELECT * FROM songs WHERE album_id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, album.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Artist artist = artistDAO.getById(rs.getString("artist_id"));

                        Song song = new Song(rs.getString("name"), artist, rs.getInt("duration"));
                        song.setAlbum(album);
                        song.setGenre(rs.getString("genre"));
                        songs.add(song);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving songs by album: " + e.getMessage());
                e.printStackTrace();
            }

            return songs;
        }
    }
}