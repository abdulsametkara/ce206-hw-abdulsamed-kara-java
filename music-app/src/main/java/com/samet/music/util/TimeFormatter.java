package com.samet.music.util;

/**
 * Utility class for formatting time durations in various formats
 */
public class TimeFormatter {

    /**
     * Formats seconds into MM:SS format (e.g. 03:45)
     * 
     * @param seconds Total seconds to format
     * @return Formatted string in MM:SS format
     */
    public static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
    
    /**
     * Formats seconds into HH:MM:SS format (e.g. 01:23:45)
     * Only shows hours if duration is 1 hour or longer
     * 
     * @param seconds Total seconds to format
     * @return Formatted string in either MM:SS or HH:MM:SS format
     */
    public static String formatLongDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, remainingSeconds);
        } else {
            return String.format("%02d:%02d", minutes, remainingSeconds);
        }
    }
    
    /**
     * Parses a duration string in MM:SS format into total seconds
     * 
     * @param durationStr Duration string in MM:SS format
     * @return Total seconds, or 0 if parsing fails
     */
    public static int parseDuration(String durationStr) {
        try {
            String[] parts = durationStr.split(":");
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            } else {
                // Try to parse as seconds only
                return Integer.parseInt(durationStr);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
} 