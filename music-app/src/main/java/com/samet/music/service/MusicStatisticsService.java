package com.samet.music.service;

import com.samet.music.dao.SongDAO;
import com.samet.music.dao.UserSongStatisticsDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.model.Song;
import com.samet.music.model.User;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating music listening statistics and reports
 */
public class MusicStatisticsService {
    private final SongDAO songDAO;
    private final UserSongStatisticsDAO userSongStatisticsDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    
    /**
     * Constructor
     */
    public MusicStatisticsService() {
        this.songDAO = new SongDAO();
        this.userSongStatisticsDAO = new UserSongStatisticsDAO();
        this.albumDAO = new AlbumDAO();
        this.artistDAO = new ArtistDAO();
    }
    
    /**
     * Get a summary of user's listening activity
     * @param userId the user ID
     * @return a map with listening statistics
     */
    public Map<String, Object> getUserListeningSummary(int userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Get basic statistics
        Map<String, Object> basicStats = userSongStatisticsDAO.getUserStatistics(userId);
        summary.putAll(basicStats);
        
        // Get favorite songs
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(userId);
        List<Song> favoriteSongs = new ArrayList<>();
        
        for (Integer songId : favoriteSongIds) {
            songDAO.findById(songId).ifPresent(favoriteSongs::add);
        }
        
        // Get top 3 favorite songs titles and artists
        List<Map<String, Object>> topFavoriteSongs = favoriteSongs.stream()
                .limit(3)
                .map(song -> {
                    Map<String, Object> songInfo = new HashMap<>();
                    songInfo.put("id", song.getId());
                    songInfo.put("title", song.getTitle());
                    songInfo.put("artist", song.getArtist());
                    return songInfo;
                })
                .collect(Collectors.toList());
                
        summary.put("top_favorites", topFavoriteSongs);
        
        // Get most played songs
        List<Integer> mostPlayedIds = userSongStatisticsDAO.getMostPlayedSongs(userId, 3);
        List<Map<String, Object>> topPlayedSongs = new ArrayList<>();
        
        for (Integer songId : mostPlayedIds) {
            songDAO.findById(songId).ifPresent(song -> {
                Map<String, Object> songInfo = new HashMap<>();
                songInfo.put("id", song.getId());
                songInfo.put("title", song.getTitle());
                songInfo.put("artist", song.getArtist());
                songInfo.put("play_count", userSongStatisticsDAO.getPlayCount(userId, song.getId()));
                topPlayedSongs.add(songInfo);
            });
        }
        
        summary.put("top_played", topPlayedSongs);
        
        // Get favorite genres
        Map<String, Integer> genreCounts = new HashMap<>();
        List<Song> userSongs = favoriteSongs;
        
        // Add most played songs to the analysis if they're not already in favorites
        for (Integer songId : mostPlayedIds) {
            if (!favoriteSongIds.contains(songId)) {
                songDAO.findById(songId).ifPresent(userSongs::add);
            }
        }
        
        // Count genres
        for (Song song : userSongs) {
            if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                genreCounts.put(song.getGenre(), genreCounts.getOrDefault(song.getGenre(), 0) + 1);
            }
        }
        
        // Get top 3 genres
        List<Map.Entry<String, Integer>> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
                
        summary.put("top_genres", topGenres.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
        
        return summary;
    }
    
    /**
     * Generate a listening trend report for a given time period
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a map with trend data
     */
    public Map<String, Object> getListeningTrendReport(int userId, LocalDateTime startDate, LocalDateTime endDate) {
        // In a real implementation, we would query the database for listening activity
        // during the specified time period and generate trend data
        
        // For this example, we'll create some mock trend data
        Map<String, Object> report = new HashMap<>();
        
        // Mock daily listening activity
        Map<String, Integer> dailyActivity = new HashMap<>();
        LocalDateTime currentDay = startDate;
        
        while (!currentDay.isAfter(endDate)) {
            String dayName = currentDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            // Generate a random number of plays for this day (between 0 and 20)
            int plays = new Random().nextInt(21);
            dailyActivity.put(dayName, plays);
            
            currentDay = currentDay.plusDays(1);
        }
        
        report.put("daily_activity", dailyActivity);
        
        // Mock genre distribution
        Map<String, Double> genreDistribution = new HashMap<>();
        genreDistribution.put("Rock", 35.0);
        genreDistribution.put("Pop", 25.0);
        genreDistribution.put("Electronic", 15.0);
        genreDistribution.put("Hip-Hop", 10.0);
        genreDistribution.put("Other", 15.0);
        
        report.put("genre_distribution", genreDistribution);
        
        return report;
    }
    
    /**
     * Generate music taste profile for a user
     * @param userId the user ID
     * @return a map with profile data
     */
    public Map<String, Object> getMusicTasteProfile(int userId) {
        Map<String, Object> profile = new HashMap<>();
        
        // Get user's songs and analyze them
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(userId);
        List<Integer> mostPlayedIds = userSongStatisticsDAO.getMostPlayedSongs(userId, 10);
        
        // Combine these for analysis
        Set<Integer> analysisSongIds = new HashSet<>();
        analysisSongIds.addAll(favoriteSongIds);
        analysisSongIds.addAll(mostPlayedIds);
        
        List<Song> analysisSongs = new ArrayList<>();
        for (Integer songId : analysisSongIds) {
            songDAO.findById(songId).ifPresent(analysisSongs::add);
        }
        
        // If the user hasn't listened to anything yet, return empty profile
        if (analysisSongs.isEmpty()) {
            profile.put("taste_profile", "Not enough data to generate a taste profile");
            return profile;
        }
        
        // Analyze genres
        Map<String, Integer> genreCounts = new HashMap<>();
        for (Song song : analysisSongs) {
            if (song.getGenre() != null && !song.getGenre().isEmpty()) {
                int weight = userSongStatisticsDAO.getPlayCount(userId, song.getId());
                weight = Math.max(1, weight); // Ensure at least weight of 1
                genreCounts.put(song.getGenre(), genreCounts.getOrDefault(song.getGenre(), 0) + weight);
            }
        }
        
        // Find dominant genres
        List<String> dominantGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        profile.put("dominant_genres", dominantGenres);
        
        // Analyze release years to determine if user prefers newer or older music
        int totalYears = 0;
        int songCount = 0;
        int currentYear = LocalDateTime.now().getYear();
        
        for (Song song : analysisSongs) {
            if (song.getYear() > 0) {
                totalYears += song.getYear();
                songCount++;
            }
        }
        
        if (songCount > 0) {
            int averageYear = totalYears / songCount;
            profile.put("average_year", averageYear);
            
            String yearPreference;
            if (currentYear - averageYear < 5) {
                yearPreference = "Contemporary";
            } else if (currentYear - averageYear < 15) {
                yearPreference = "Recent";
            } else if (currentYear - averageYear < 30) {
                yearPreference = "Nostalgic";
            } else {
                yearPreference = "Vintage";
            }
            
            profile.put("era_preference", yearPreference);
        }
        
        // Generate taste descriptors based on analysis
        List<String> tasteDescriptors = new ArrayList<>();
        
        // Add genre-based descriptors
        if (!dominantGenres.isEmpty()) {
            tasteDescriptors.add(dominantGenres.get(0) + " enthusiast");
        }
        
        // Add year-based descriptors if available
        if (profile.containsKey("era_preference")) {
            tasteDescriptors.add(profile.get("era_preference") + " music fan");
        }
        
        // Add variety descriptor
        if (genreCounts.size() > 5) {
            tasteDescriptors.add("Eclectic");
        } else if (genreCounts.size() <= 2) {
            tasteDescriptors.add("Focused");
        }
        
        profile.put("taste_descriptors", tasteDescriptors);
        
        return profile;
    }
    
    /**
     * Get song recommendations based on similar users' listening habits
     * @param userId the user ID
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs
     */
    public List<Song> getSimilarUserRecommendations(int userId, int limit) {
        // In a real implementation, we would:
        // 1. Find users with similar listening patterns 
        // 2. Get songs they enjoy that the current user hasn't heard
        // 3. Return those as recommendations
        
        // For this example, we'll return some mock recommendations
        List<Song> mockRecommendations = new ArrayList<>();
        
        // Get some songs the user hasn't played
        List<Song> allSongs = songDAO.findAll();
        List<Integer> userSongIds = new ArrayList<>();
        
        // Get IDs of songs the user has played
        for (Song song : allSongs) {
            if (userSongStatisticsDAO.getPlayCount(userId, song.getId()) > 0) {
                userSongIds.add(song.getId());
            }
        }
        
        // Filter to songs the user hasn't played
        List<Song> unplayedSongs = allSongs.stream()
                .filter(song -> !userSongIds.contains(song.getId()))
                .collect(Collectors.toList());
                
        // Return a random subset of unplayed songs
        Collections.shuffle(unplayedSongs);
        int count = Math.min(limit, unplayedSongs.size());
        
        for (int i = 0; i < count; i++) {
            mockRecommendations.add(unplayedSongs.get(i));
        }
        
        return mockRecommendations;
    }
    
    /**
     * Generate a report of listening habits by time of day
     * @param userId the user ID
     * @return a map with time-based listening data
     */
    public Map<String, Integer> getTimeOfDayListeningHabits(int userId) {
        // In a real implementation, we would analyze when users listen to music
        // and group their activity by time of day
        
        // For this example, we'll return mock data
        Map<String, Integer> timeDistribution = new HashMap<>();
        timeDistribution.put("Morning (6AM-12PM)", 15);
        timeDistribution.put("Afternoon (12PM-6PM)", 25);
        timeDistribution.put("Evening (6PM-12AM)", 45);
        timeDistribution.put("Night (12AM-6AM)", 15);
        
        return timeDistribution;
    }
    
    /**
     * Get listening activity by day of week
     * @param userId the user ID
     * @return a map with day-based listening data
     */
    public Map<String, Integer> getDayOfWeekListeningActivity(int userId) {
        // In a real implementation, we would analyze which days users listen to music most
        
        // For this example, we'll return mock data
        Map<String, Integer> dayDistribution = new HashMap<>();
        dayDistribution.put("Monday", 10);
        dayDistribution.put("Tuesday", 12);
        dayDistribution.put("Wednesday", 15);
        dayDistribution.put("Thursday", 18);
        dayDistribution.put("Friday", 25);
        dayDistribution.put("Saturday", 30);
        dayDistribution.put("Sunday", 20);
        
        return dayDistribution;
    }
    
    /**
     * Get average song duration preferences for a user
     * @param userId the user ID
     * @return average duration in seconds
     */
    public int getAverageSongDurationPreference(int userId) {
        // Get most played and favorite songs
        List<Integer> favoriteSongIds = userSongStatisticsDAO.getFavoriteSongs(userId);
        List<Integer> mostPlayedIds = userSongStatisticsDAO.getMostPlayedSongs(userId, 10);
        
        // Combine these for analysis
        Set<Integer> analysisSongIds = new HashSet<>();
        analysisSongIds.addAll(favoriteSongIds);
        analysisSongIds.addAll(mostPlayedIds);
        
        if (analysisSongIds.isEmpty()) {
            return 0; // No data available
        }
        
        int totalDuration = 0;
        int songCount = 0;
        
        for (Integer songId : analysisSongIds) {
            Optional<Song> songOpt = songDAO.findById(songId);
            if (songOpt.isPresent()) {
                Song song = songOpt.get();
                if (song.getDuration() > 0) {
                    totalDuration += song.getDuration();
                    songCount++;
                }
            }
        }
        
        return songCount > 0 ? totalDuration / songCount : 0;
    }
} 