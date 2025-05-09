package com.samet.music.service;

import com.samet.music.dao.SongDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.UserSongStatisticsDAO;
import com.samet.music.model.Song;
import com.samet.music.model.Album;
import com.samet.music.model.User;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating music recommendations
 */
public class RecommendationService {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/musiclibrary.db";
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    private final UserSongStatisticsDAO userSongStatisticsDAO;
    
    /**
     * Constructor
     */
    public RecommendationService() {
        this.songDAO = new SongDAO();
        this.albumDAO = new AlbumDAO();
        this.artistDAO = new ArtistDAO();
        this.userSongStatisticsDAO = new UserSongStatisticsDAO();
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create recommendations table
            String sql = "CREATE TABLE IF NOT EXISTS recommendations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "song_id INTEGER," +
                    "album_id INTEGER," +
                    "artist_name TEXT," +
                    "reason TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (song_id) REFERENCES songs(id)," +
                    "FOREIGN KEY (album_id) REFERENCES albums(id)" +
                    ")";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get song recommendations for a user
     * @param user the user
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs
     */
    public List<Song> getSongRecommendations(User user, int limit) {
        if (user == null) {
            return List.of();
        }
        
        // First check if we have stored recommendations for this user
        List<Song> storedRecommendations = getStoredSongRecommendations(user.getId(), limit);
        if (!storedRecommendations.isEmpty()) {
            return storedRecommendations;
        }
        
        // Generate new recommendations
        List<Song> userSongs = songDAO.findByUserId(user.getId());
        
        // If user has no songs, return empty list
        if (userSongs.isEmpty()) {
            return List.of();
        }
        
        // Calculate genre preferences
        Map<String, Integer> genrePreferences = new HashMap<>();
        Map<String, Integer> artistPreferences = new HashMap<>();
        
        // Consider favorites and play counts for stronger weighting
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(user.getId());
        List<Integer> mostPlayedSongIds = userSongStatisticsDAO.getMostPlayedSongs(user.getId(), 10);
        
        for (Song song : userSongs) {
            int weight = 1;
            
            // Increase weight for favorite songs
            if (favoriteSongIds.contains(song.getId())) {
                weight += 3;
            }
            
            // Increase weight for frequently played songs
            if (mostPlayedSongIds.contains(song.getId())) {
                weight += 2;
            }
            
            // Count genres with weight
            String genre = song.getGenre();
            if (genre != null && !genre.isEmpty()) {
                genrePreferences.put(genre, genrePreferences.getOrDefault(genre, 0) + weight);
            }
            
            // Count artists with weight
            String artist = song.getArtist();
            if (artist != null && !artist.isEmpty()) {
                artistPreferences.put(artist, artistPreferences.getOrDefault(artist, 0) + weight);
            }
        }
        
        // Get top genres and artists
        List<String> topGenres = genrePreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<String> topArtists = artistPreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Get all songs
        List<Song> allSongs = songDAO.findAll();
        
        // Filter to songs not owned by user but matching preferences
        List<Song> recommendations = allSongs.stream()
                .filter(song -> song.getUserId() != user.getId())
                .filter(song -> 
                    (song.getGenre() != null && topGenres.contains(song.getGenre())) ||
                    (song.getArtist() != null && topArtists.contains(song.getArtist()))
                )
                .limit(limit)
                .collect(Collectors.toList());
        
        // Store these recommendations for future use
        storeSongRecommendations(user.getId(), recommendations);
        
        return recommendations;
    }
    
    /**
     * Get enhanced song recommendations based on user listening history
     * @param user the user
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs with reasons
     */
    public Map<Song, String> getEnhancedSongRecommendations(User user, int limit) {
        if (user == null) {
            return Map.of();
        }
        
        Map<Song, String> recommendationsWithReasons = new LinkedHashMap<>();
        
        // Get favorite songs for this user
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(user.getId());
        
        // Get songs by the same artists as user's favorites
        Set<String> favoriteArtists = new HashSet<>();
        for (Integer songId : favoriteSongIds) {
            songDAO.findById(songId).ifPresent(song -> {
                if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                    favoriteArtists.add(song.getArtist());
                }
            });
        }
        
        // Get recently played songs
        List<Integer> recentSongIds = userSongStatisticsDAO.getRecentlyPlayedSongs(user.getId(), 5);
        Set<String> recentGenres = new HashSet<>();
        for (Integer songId : recentSongIds) {
            songDAO.findById(songId).ifPresent(song -> {
                if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                    recentGenres.add(song.getGenre());
                }
            });
        }
        
        // Get all songs
        List<Song> allSongs = songDAO.findAll();
        
        // Filter to songs not owned by user
        List<Song> potentialRecommendations = allSongs.stream()
                .filter(song -> song.getUserId() != user.getId())
                .collect(Collectors.toList());
        
        // Add recommendations based on favorite artists
        if (!favoriteArtists.isEmpty()) {
            for (Song song : potentialRecommendations) {
                if (favoriteArtists.contains(song.getArtist()) && 
                    recommendationsWithReasons.size() < limit) {
                    recommendationsWithReasons.put(song, 
                        "Based on your favorite artist: " + song.getArtist());
                }
            }
        }
        
        // Add recommendations based on recent genres
        if (!recentGenres.isEmpty() && recommendationsWithReasons.size() < limit) {
            for (Song song : potentialRecommendations) {
                if (recentGenres.contains(song.getGenre()) && 
                    !recommendationsWithReasons.containsKey(song) &&
                    recommendationsWithReasons.size() < limit) {
                    recommendationsWithReasons.put(song, 
                        "Because you recently listened to " + song.getGenre() + " music");
                }
            }
        }
        
        // If we still need more recommendations, add some based on overall popularity
        if (recommendationsWithReasons.size() < limit) {
            // This would typically be based on global play counts, for now just add remaining songs
            for (Song song : potentialRecommendations) {
                if (!recommendationsWithReasons.containsKey(song) &&
                    recommendationsWithReasons.size() < limit) {
                    recommendationsWithReasons.put(song, "Popular with other listeners");
                }
            }
        }
        
        return recommendationsWithReasons;
    }
    
    /**
     * Get album recommendations for a user
     * @param user the user
     * @param limit the maximum number of recommendations
     * @return a list of recommended albums
     */
    public List<Album> getAlbumRecommendations(User user, int limit) {
        if (user == null) {
            return List.of();
        }
        
        // First check if we have stored recommendations for this user
        List<Album> storedRecommendations = getStoredAlbumRecommendations(user.getId(), limit);
        if (!storedRecommendations.isEmpty()) {
            return storedRecommendations;
        }
        
        // Get user's songs and albums
        List<Song> userSongs = songDAO.findByUserId(user.getId());
        List<Album> userAlbums = albumDAO.findByUserId(user.getId());
        
        // If user has no music, return empty list
        if (userSongs.isEmpty() && userAlbums.isEmpty()) {
            return List.of();
        }
        
        // Calculate artist and genre preferences
        Map<String, Integer> genrePreferences = new HashMap<>();
        Map<String, Integer> artistPreferences = new HashMap<>();
        
        // Get favorite songs to increase their weight
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(user.getId());
        
        // From songs
        for (Song song : userSongs) {
            int weight = 1;
            
            // Increase weight for favorite songs
            if (favoriteSongIds.contains(song.getId())) {
                weight = 3;
            }
            
            if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                genrePreferences.put(song.getGenre(), genrePreferences.getOrDefault(song.getGenre(), 0) + weight);
            }
            
            if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                artistPreferences.put(song.getArtist(), artistPreferences.getOrDefault(song.getArtist(), 0) + weight);
            }
        }
        
        // From albums
        for (Album album : userAlbums) {
            if (album.getGenre() != null && !album.getGenre().isEmpty()) {
                genrePreferences.put(album.getGenre(), genrePreferences.getOrDefault(album.getGenre(), 0) + 1);
            }
            
            if (album.getArtist() != null && !album.getArtist().isEmpty()) {
                artistPreferences.put(album.getArtist(), artistPreferences.getOrDefault(album.getArtist(), 0) + 1);
            }
        }
        
        // Get top genres and artists
        List<String> topGenres = genrePreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<String> topArtists = artistPreferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Get all albums
        List<Album> allAlbums = albumDAO.findAll();
        
        // Filter to albums not owned by user but matching preferences
        List<Album> recommendations = allAlbums.stream()
                .filter(album -> album.getUserId() != user.getId())
                .filter(album -> 
                    (album.getGenre() != null && topGenres.contains(album.getGenre())) ||
                    (album.getArtist() != null && topArtists.contains(album.getArtist()))
                )
                .limit(limit)
                .collect(Collectors.toList());
        
        // Store these recommendations for future use
        storeAlbumRecommendations(user.getId(), recommendations);
        
        return recommendations;
    }
    
    /**
     * Get artist recommendations for a user
     * @param user the user
     * @param limit the maximum number of recommendations
     * @return a list of recommended artist names
     */
    public List<String> getArtistRecommendations(User user, int limit) {
        if (user == null) {
            return List.of();
        }
        
        // First check if we have stored recommendations for this user
        List<String> storedRecommendations = getStoredArtistRecommendations(user.getId(), limit);
        if (!storedRecommendations.isEmpty()) {
            return storedRecommendations;
        }
        
        // Get artists from user's songs and albums
        Set<String> userArtists = new HashSet<>();
        
        songDAO.findByUserId(user.getId()).stream()
                .map(Song::getArtist)
                .filter(a -> a != null && !a.isEmpty())
                .forEach(userArtists::add);
                
        albumDAO.findByUserId(user.getId()).stream()
                .map(Album::getArtist)
                .filter(a -> a != null && !a.isEmpty())
                .forEach(userArtists::add);
        
        // If user has no artists, return empty list
        if (userArtists.isEmpty()) {
            return List.of();
        }
        
        // Get all artists
        Set<String> allArtists = artistDAO.getAllArtistNames();
        
        // Remove user's artists
        allArtists.removeAll(userArtists);
        
        // Get recommendations (up to limit)
        List<String> recommendations = allArtists.stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        // Store these recommendations for future use
        storeArtistRecommendations(user.getId(), recommendations);
        
        return recommendations;
    }
    
    /**
     * Get most likely to enjoy songs for a user based on their favorites and listening history
     * @param user the user
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs
     */
    public List<Song> getMostLikelyToEnjoySongs(User user, int limit) {
        if (user == null) {
            return List.of();
        }
        
        // Get user listening statistics
        Map<String, Object> userStats = userSongStatisticsDAO.getUserStatistics(user.getId());
        
        // If user has no listening history, fall back to regular recommendations
        if ((int)userStats.getOrDefault("total_plays", 0) == 0) {
            return getSongRecommendations(user, limit);
        }
        
        // Get most played songs, these give us the strongest signal of preference
        List<Integer> mostPlayedIds = userSongStatisticsDAO.getMostPlayedSongs(user.getId(), 5);
        List<Song> mostPlayedSongs = new ArrayList<>();
        
        for (Integer id : mostPlayedIds) {
            songDAO.findById(id).ifPresent(mostPlayedSongs::add);
        }
        
        // Calculate genre and artist weights based on play counts
        Map<String, Double> genreScores = new HashMap<>();
        Map<String, Double> artistScores = new HashMap<>();
        
        for (Song song : mostPlayedSongs) {
            // Get play count
            int playCount = userSongStatisticsDAO.getPlayCount(user.getId(), song.getId());
            double weight = Math.log10(playCount + 1); // Logarithmic scaling to prevent one extremely played song from dominating
            
            if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                genreScores.put(song.getGenre(), genreScores.getOrDefault(song.getGenre(), 0.0) + weight);
            }
            
            if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                artistScores.put(song.getArtist(), artistScores.getOrDefault(song.getArtist(), 0.0) + weight);
            }
        }
        
        // Add favorite songs to the mix
        List<Integer> favoriteIds = userSongStatisticsDAO.getFavoriteSongs(user.getId());
        for (Integer id : favoriteIds) {
            songDAO.findById(id).ifPresent(song -> {
                // Explicitly favorited songs get a high weight
                double weight = 3.0;
                
                if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                    genreScores.put(song.getGenre(), genreScores.getOrDefault(song.getGenre(), 0.0) + weight);
                }
                
                if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                    artistScores.put(song.getArtist(), artistScores.getOrDefault(song.getArtist(), 0.0) + weight);
                }
            });
        }
        
        // Get all songs
        List<Song> allSongs = songDAO.findAll();
        
        // Calculate a score for each potential recommendation based on genre and artist match
        Map<Song, Double> songScores = new HashMap<>();
        
        for (Song song : allSongs) {
            if (song.getUserId() == user.getId()) {
                continue; // Skip songs the user already owns
            }
            
            double score = 0.0;
            
            // Add genre score
            if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                score += genreScores.getOrDefault(song.getGenre(), 0.0);
            }
            
            // Add artist score (weighted higher than genre)
            if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                score += 1.5 * artistScores.getOrDefault(song.getArtist(), 0.0);
            }
            
            if (score > 0) {
                songScores.put(song, score);
            }
        }
        
        // Sort by score and take the top ones
        List<Song> recommendations = songScores.entrySet().stream()
                .sorted(Map.Entry.<Song, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        return recommendations;
    }
    
    /**
     * Store song recommendations for a user
     * @param userId the user ID
     * @param songs the recommended songs
     */
    private void storeSongRecommendations(int userId, List<Song> songs) {
        String sql = "INSERT INTO recommendations (user_id, song_id, reason) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // First delete old recommendations
            deleteUserRecommendations(conn, userId, "song");
            
            // Then add new ones
            for (Song song : songs) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, song.getId());
                pstmt.setString(3, "Based on your music preferences");
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Store album recommendations for a user
     * @param userId the user ID
     * @param albums the recommended albums
     */
    private void storeAlbumRecommendations(int userId, List<Album> albums) {
        String sql = "INSERT INTO recommendations (user_id, album_id, reason) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // First delete old recommendations
            deleteUserRecommendations(conn, userId, "album");
            
            // Then add new ones
            for (Album album : albums) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, album.getId());
                pstmt.setString(3, "Based on your music preferences");
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Store artist recommendations for a user
     * @param userId the user ID
     * @param artists the recommended artist names
     */
    private void storeArtistRecommendations(int userId, List<String> artists) {
        String sql = "INSERT INTO recommendations (user_id, artist_name, reason) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // First delete old recommendations
            deleteUserRecommendations(conn, userId, "artist");
            
            // Then add new ones
            for (String artist : artists) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, artist);
                pstmt.setString(3, "Based on your music preferences");
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Delete a user's recommendations of a specific type
     * @param conn the database connection
     * @param userId the user ID
     * @param type the recommendation type (song, album, or artist)
     */
    private void deleteUserRecommendations(Connection conn, int userId, String type) throws SQLException {
        String sql = "";
        
        switch (type) {
            case "song":
                sql = "DELETE FROM recommendations WHERE user_id = ? AND song_id IS NOT NULL";
                break;
            case "album":
                sql = "DELETE FROM recommendations WHERE user_id = ? AND album_id IS NOT NULL";
                break;
            case "artist":
                sql = "DELETE FROM recommendations WHERE user_id = ? AND artist_name IS NOT NULL";
                break;
            default:
                return;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get stored song recommendations for a user
     * @param userId the user ID
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs
     */
    private List<Song> getStoredSongRecommendations(int userId, int limit) {
        List<Song> recommendations = new ArrayList<>();
        String sql = "SELECT song_id FROM recommendations WHERE user_id = ? AND song_id IS NOT NULL LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int songId = rs.getInt("song_id");
                    songDAO.findById(songId).ifPresent(recommendations::add);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * Get stored album recommendations for a user
     * @param userId the user ID
     * @param limit the maximum number of recommendations
     * @return a list of recommended albums
     */
    private List<Album> getStoredAlbumRecommendations(int userId, int limit) {
        List<Album> recommendations = new ArrayList<>();
        String sql = "SELECT album_id FROM recommendations WHERE user_id = ? AND album_id IS NOT NULL LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int albumId = rs.getInt("album_id");
                    Album album = albumDAO.findById(albumId);
                    if (album != null) {
                        recommendations.add(album);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * Get stored artist recommendations for a user
     * @param userId the user ID
     * @param limit the maximum number of recommendations
     * @return a list of recommended artist names
     */
    private List<String> getStoredArtistRecommendations(int userId, int limit) {
        List<String> recommendations = new ArrayList<>();
        String sql = "SELECT artist_name FROM recommendations WHERE user_id = ? AND artist_name IS NOT NULL LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String artistName = rs.getString("artist_name");
                    if (artistName != null && !artistName.isEmpty()) {
                        recommendations.add(artistName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return recommendations;
    }
} 