package com.samet.music.controller;

import com.samet.music.model.Song;
import com.samet.music.model.User;
import com.samet.music.service.MusicStatisticsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for accessing music statistics and insights
 */
public class MusicStatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(MusicStatisticsController.class);
    private final UserController userController;
    private final MusicStatisticsService musicStatisticsService;
    
    /**
     * Constructor
     * @param userController the user controller
     */
    public MusicStatisticsController(UserController userController) {
        this.userController = userController;
        this.musicStatisticsService = new MusicStatisticsService();
    }
    
    /**
     * Get a summary of the current user's listening activity
     * @return a map with listening statistics
     */
    public Map<String, Object> getUserListeningSummary() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get listening summary: no user is logged in");
            return new HashMap<>();
        }
        
        Map<String, Object> summary = musicStatisticsService.getUserListeningSummary(currentUser.getId());
        logger.info("Retrieved listening summary for user {}", currentUser.getUsername());
        
        return summary;
    }
    
    /**
     * Generate a listening trend report for a given time period
     * @param days the number of days to include in the report (counting back from today)
     * @return a map with trend data
     */
    public Map<String, Object> getListeningTrendReport(int days) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get listening trend report: no user is logged in");
            return new HashMap<>();
        }
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, Object> report = musicStatisticsService.getListeningTrendReport(
                currentUser.getId(), startDate, endDate);
        
        logger.info("Generated listening trend report for user {} covering {} days", 
                currentUser.getUsername(), days);
        
        return report;
    }
    
    /**
     * Generate music taste profile for the current user
     * @return a map with profile data
     */
    public Map<String, Object> getMusicTasteProfile() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get music taste profile: no user is logged in");
            return new HashMap<>();
        }
        
        Map<String, Object> profile = musicStatisticsService.getMusicTasteProfile(currentUser.getId());
        logger.info("Generated music taste profile for user {}", currentUser.getUsername());
        
        return profile;
    }
    
    /**
     * Get song recommendations based on similar users' listening habits
     * @param limit the maximum number of recommendations
     * @return a list of recommended songs
     */
    public List<Song> getSimilarUserRecommendations(int limit) {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get similar user recommendations: no user is logged in");
            return new ArrayList<>();
        }
        
        List<Song> recommendations = musicStatisticsService.getSimilarUserRecommendations(
                currentUser.getId(), limit);
        
        logger.info("Generated {} similar user recommendations for user {}", 
                recommendations.size(), currentUser.getUsername());
        
        return recommendations;
    }
    
    /**
     * Generate a report of listening habits by time of day
     * @return a map with time-based listening data
     */
    public Map<String, Integer> getTimeOfDayListeningHabits() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get time of day listening habits: no user is logged in");
            return new HashMap<>();
        }
        
        Map<String, Integer> habits = musicStatisticsService.getTimeOfDayListeningHabits(currentUser.getId());
        logger.info("Retrieved time of day listening habits for user {}", currentUser.getUsername());
        
        return habits;
    }
    
    /**
     * Get listening activity by day of week
     * @return a map with day-based listening data
     */
    public Map<String, Integer> getDayOfWeekListeningActivity() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get day of week listening activity: no user is logged in");
            return new HashMap<>();
        }
        
        Map<String, Integer> activity = musicStatisticsService.getDayOfWeekListeningActivity(currentUser.getId());
        logger.info("Retrieved day of week listening activity for user {}", currentUser.getUsername());
        
        return activity;
    }
    
    /**
     * Get average song duration preference for the current user
     * @return average duration in seconds, or 0 if no data available
     */
    public int getAverageSongDurationPreference() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get average song duration preference: no user is logged in");
            return 0;
        }
        
        int averageDuration = musicStatisticsService.getAverageSongDurationPreference(currentUser.getId());
        logger.info("Retrieved average song duration preference for user {}: {} seconds", 
                currentUser.getUsername(), averageDuration);
        
        return averageDuration;
    }
    
    /**
     * Format song duration from seconds to a human-readable string (mm:ss)
     * @param durationInSeconds the duration in seconds
     * @return formatted duration string
     */
    public String formatSongDuration(int durationInSeconds) {
        if (durationInSeconds <= 0) {
            return "00:00";
        }
        
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Get insights about the current user's music habits
     * @return a list of insight messages
     */
    public List<String> getMusicInsights() {
        User currentUser = userController.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Cannot get music insights: no user is logged in");
            return new ArrayList<>();
        }
        
        List<String> insights = new ArrayList<>();
        
        // Get taste profile to generate insights
        Map<String, Object> profile = musicStatisticsService.getMusicTasteProfile(currentUser.getId());
        
        // Generate insights based on taste profile
        if (profile.containsKey("dominant_genres") && !((List<?>)profile.get("dominant_genres")).isEmpty()) {
            List<String> genres = (List<String>) profile.get("dominant_genres");
            insights.add("You seem to enjoy " + genres.get(0) + " music the most.");
            
            if (genres.size() > 1) {
                insights.add("You also have a strong preference for " + genres.get(1) + " music.");
            }
        }
        
        if (profile.containsKey("era_preference")) {
            String era = (String) profile.get("era_preference");
            insights.add("You tend to listen to " + era.toLowerCase() + " music.");
        }
        
        if (profile.containsKey("taste_descriptors") && !((List<?>)profile.get("taste_descriptors")).isEmpty()) {
            List<String> descriptors = (List<String>) profile.get("taste_descriptors");
            if (descriptors.contains("Eclectic")) {
                insights.add("You have diverse music tastes across many genres.");
            } else if (descriptors.contains("Focused")) {
                insights.add("You have focused music tastes in specific genres.");
            }
        }
        
        // Add insights based on listening times
        Map<String, Integer> timeHabits = musicStatisticsService.getTimeOfDayListeningHabits(currentUser.getId());
        if (!timeHabits.isEmpty()) {
            String peakTime = timeHabits.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
                    
            if (!peakTime.isEmpty()) {
                insights.add("You tend to listen to music most during the " + peakTime.toLowerCase() + ".");
            }
        }
        
        // Add insights based on song duration
        int avgDuration = musicStatisticsService.getAverageSongDurationPreference(currentUser.getId());
        if (avgDuration > 0) {
            if (avgDuration < 180) { // Less than 3 minutes
                insights.add("You prefer shorter songs compared to the average listener.");
            } else if (avgDuration > 300) { // More than 5 minutes
                insights.add("You enjoy longer songs compared to the average listener.");
            }
        }
        
        logger.info("Generated {} music insights for user {}", insights.size(), currentUser.getUsername());
        
        return insights;
    }
} 