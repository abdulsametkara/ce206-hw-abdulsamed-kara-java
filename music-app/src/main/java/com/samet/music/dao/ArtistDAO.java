package com.samet.music.dao;

import com.samet.music.util.DatabaseUtil;
import com.samet.music.model.Artist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ArtistDAO {
    private static final Object LOCK = new Object();
    private static final Map<String, Artist> artistCache = new HashMap<>();

    public void insert(Artist artist) {
        synchronized (LOCK) {
            // Önce bu ID ile bir kayıt var mı kontrol et
            String checkSql = "SELECT count(*) FROM artists WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, artist.getId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    // Bu ID zaten var, update yapılabilir
                    update(artist);
                    return;
                }

                // ID yoksa insert yap
                String sql = "INSERT INTO artists (id, name, biography) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    System.out.println("Inserting artist: " + artist.getName() + ", ID: " + artist.getId());

                    pstmt.setString(1, artist.getId());
                    pstmt.setString(2, artist.getName());
                    pstmt.setString(3, artist.getBiography());

                    int result = pstmt.executeUpdate();
                    System.out.println("Insert result: " + result + " row(s) affected");

                    // Önbelleğe ekle
                    artistCache.put(artist.getId(), artist);
                }
            } catch (SQLException e) {
                System.err.println("Error inserting artist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Artist getById(String id) {
        synchronized (LOCK) {

            // Önce önbellekte kontrol et
            if (artistCache.containsKey(id)) {
                return artistCache.get(id);
            }

            String sql = "SELECT * FROM artists WHERE id = ?";
            System.out.println("Looking for artist with ID: " + id);

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String artistId = rs.getString("id");
                        String name = rs.getString("name");
                        String biography = rs.getString("biography");

                        System.out.println("Found artist: " + name + " with ID: " + artistId);

                        Artist artist = new Artist(name, biography) {
                            @Override
                            public String getId() {
                                return artistId; // ID'yi değiştirme
                            }
                        };

                        // Önbelleğe ekle
                        artistCache.put(artistId, artist);
                        return artist;
                    } else {
                        System.out.println("Artist with ID " + id + " not found in database.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving artist by ID: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }

    // ArtistDAO sınıfında getAll metodunda şu değişikliği yapın:
    public List<Artist> getAll() {
        synchronized (LOCK) {
            List<Artist> artists = new ArrayList<>();
            Map<String, Artist> uniqueArtists = new HashMap<>(); // Benzersiz sanatçıları saklamak için
            String sql = "SELECT * FROM artists";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                // Önbelleği temizle ve yeniden oluştur
                artistCache.clear();

                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String biography = rs.getString("biography");

                    // Bir sanatçıyı sadece bir kez işle
                    if (!uniqueArtists.containsKey(id)) {
                        // Yeni Artist nesnesi oluştur ama ID'yi koru
                        final String finalId = id;
                        Artist artist = new Artist(name, biography) {
                            @Override
                            public String getId() {
                                return finalId; // UUID oluşturma yerine veritabanındaki ID'yi kullan
                            }
                        };

                        // Önbelleğe ve benzersiz sanatçılar map'ine ekle
                        artistCache.put(id, artist);
                        uniqueArtists.put(id, artist);
                    }
                }

                // Benzersiz sanatçıları listeye ekle
                artists.addAll(uniqueArtists.values());

                System.out.println("[DEBUG] Retrieved " + artists.size() + " unique artists from database.");
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to retrieve artists: " + e.getMessage());
            }

            return artists;
        }
    }

    public void update(Artist artist) {
        synchronized (LOCK) {
            String sql = "UPDATE artists SET name = ?, biography = ? WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, artist.getName());
                pstmt.setString(2, artist.getBiography());
                pstmt.setString(3, artist.getId());

                int result = pstmt.executeUpdate();
                System.out.println("Update artist result: " + result + " row(s) affected");

                // Önbelleği güncelle
                artistCache.put(artist.getId(), artist);

            } catch (SQLException e) {
                System.err.println("Error updating artist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void delete(String id) {
        synchronized (LOCK) {

            String sql = "DELETE FROM artists WHERE id = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, id);
                int result = pstmt.executeUpdate();
                System.out.println("Delete result: " + result + " row(s) affected");

                // Önbellekten kaldır
                artistCache.remove(id);

            } catch (SQLException e) {
                System.err.println("Error deleting artist: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void removeDuplicateArtists() {
        synchronized (LOCK) {
            try {
                // Önce tüm sanatçıları al
                List<Artist> allArtists = getAll();

                // İsme göre grupla (ilk ID'yi tut)
                Map<String, String> nameToIdMap = new HashMap<>();

                for (Artist artist : allArtists) {
                    String name = artist.getName();
                    if (!nameToIdMap.containsKey(name)) {
                        nameToIdMap.put(name, artist.getId());
                        System.out.println("Keeping artist: " + name + " with ID: " + artist.getId());
                    } else {
                        String keepId = nameToIdMap.get(name);
                        String duplicateId = artist.getId();

                        System.out.println("Found duplicate artist: " + name +
                                " - keeping ID: " + keepId +
                                ", removing ID: " + duplicateId);

                        // Albüm ve şarkı referanslarını güncelle
                        try (Connection conn = DatabaseUtil.getConnection()) {
                            // Albümleri güncelle
                            PreparedStatement updateAlbums = conn.prepareStatement(
                                    "UPDATE albums SET artist_id = ? WHERE artist_id = ?");
                            updateAlbums.setString(1, keepId);
                            updateAlbums.setString(2, duplicateId);
                            int albumsUpdated = updateAlbums.executeUpdate();
                            System.out.println("Updated " + albumsUpdated + " albums");

                            // Şarkıları güncelle
                            PreparedStatement updateSongs = conn.prepareStatement(
                                    "UPDATE songs SET artist_id = ? WHERE artist_id = ?");
                            updateSongs.setString(1, keepId);
                            updateSongs.setString(2, duplicateId);
                            int songsUpdated = updateSongs.executeUpdate();
                            System.out.println("Updated " + songsUpdated + " songs");

                            // Duplike sanatçıyı sil
                            PreparedStatement deleteArtist = conn.prepareStatement(
                                    "DELETE FROM artists WHERE id = ?");
                            deleteArtist.setString(1, duplicateId);
                            int deleted = deleteArtist.executeUpdate();
                            System.out.println("Deleted " + deleted + " duplicate artist records");
                        }
                    }
                }

                // Önbelleği temizle
                artistCache.clear();

            } catch (SQLException e) {
                System.err.println("Error removing duplicate artists: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void mergeArtists(String primaryArtistId, String duplicateArtistId) {
        synchronized (LOCK) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                // Transaction başlat
                conn.setAutoCommit(false);

                try {
                    // Albümleri güncelle
                    PreparedStatement updateAlbums = conn.prepareStatement(
                            "UPDATE albums SET artist_id = ? WHERE artist_id = ?");
                    updateAlbums.setString(1, primaryArtistId);
                    updateAlbums.setString(2, duplicateArtistId);
                    int albumsUpdated = updateAlbums.executeUpdate();

                    // Şarkıları güncelle
                    PreparedStatement updateSongs = conn.prepareStatement(
                            "UPDATE songs SET artist_id = ? WHERE artist_id = ?");
                    updateSongs.setString(1, primaryArtistId);
                    updateSongs.setString(2, duplicateArtistId);
                    int songsUpdated = updateSongs.executeUpdate();

                    // Duplike sanatçıyı sil
                    PreparedStatement deleteArtist = conn.prepareStatement(
                            "DELETE FROM artists WHERE id = ?");
                    deleteArtist.setString(1, duplicateArtistId);
                    int artistDeleted = deleteArtist.executeUpdate();

                    // Önbellekten kaldır
                    artistCache.remove(duplicateArtistId);

                    // Transaction'ı tamamla
                    conn.commit();

                    System.out.println("Merged artist " + duplicateArtistId + " into " + primaryArtistId +
                            ": Updated " + albumsUpdated + " albums, " +
                            songsUpdated + " songs, deleted " + artistDeleted + " artist record");
                } catch (SQLException e) {
                    // Hata durumunda geri al
                    conn.rollback();
                    throw e;
                } finally {
                    // Otomatik commit'i geri yükle
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error merging artists: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}