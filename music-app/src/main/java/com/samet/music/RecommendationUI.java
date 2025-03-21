package com.samet.music;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * UI handler for recommendation features
 */
public class RecommendationUI {
    private final Scanner scanner;
    private final PrintStream out;
    private final MusicCollectionService service;
    private final MusicRecommendationSystem recommendationSystem;

    public RecommendationUI(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
        this.service = MusicCollectionService.getInstance();
        this.recommendationSystem = MusicRecommendationSystem.getInstance();
    }

    /**
     * Handles user interaction for song recommendations by genre
     * @param userId ID of the current user
     */
    public void showSongRecommendationsByGenre(String userId) {
        out.println("\n========== SONG RECOMMENDATIONS BY GENRE ==========");

        // Check if the user has listened to any songs
        if (!hasListeningHistory(userId)) {
            return;
        }

        // Show the user's top genres
        showUserTopGenres(userId);

        // Get recommendations
        List<Song> recommendations = recommendationSystem.recommendSongsByGenre(userId, 10);

        if (recommendations.isEmpty()) {
            out.println("\nNo song recommendations found based on your genre preferences.");
            out.println("Try listening to more songs or exploring different genres.");
            return;
        }

        // Display recommendations
        out.println("\nBased on your genre preferences, you might like these songs:");
        out.println("------------------------------------------------------------");

        for (int i = 0; i < recommendations.size(); i++) {
            Song song = recommendations.get(i);
            String artistName = song.getArtist() != null ? song.getArtist().getName() : "Unknown";
            String albumName = song.getAlbum() != null ? song.getAlbum().getName() : "Single";

            out.println((i + 1) + ". " + song.getName() + " by " + artistName +
                    " (" + song.getFormattedDuration() + ") - " + albumName);
        }

        // Ask if user wants to listen to a recommended song
        out.println("\nWould you like to listen to one of these songs?");
        out.println("1. Yes");
        out.println("2. No");
        out.print("Your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 1) {
                listenToRecommendedSong(userId, recommendations);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Handles user interaction for album recommendations
     * @param userId ID of the current user
     */
    public void showAlbumRecommendations(String userId) {
        out.println("\n========== ALBUM RECOMMENDATIONS ==========");

        // Check if the user has listened to any songs
        if (!hasListeningHistory(userId)) {
            return;
        }

        // Show the user's top artists
        showUserTopArtists(userId);

        // Get recommendations
        List<Album> recommendations = recommendationSystem.recommendAlbumsByArtist(userId, 5);

        if (recommendations.isEmpty()) {
            out.println("\nNo album recommendations found based on your artist preferences.");
            out.println("Try listening to more songs by different artists.");
            return;
        }

        // Display recommendations
        out.println("\nBased on your artist preferences, you might like these albums:");
        out.println("------------------------------------------------------------");

        for (int i = 0; i < recommendations.size(); i++) {
            Album album = recommendations.get(i);
            String artistName = album.getArtist() != null ? album.getArtist().getName() : "Unknown";

            out.println((i + 1) + ". " + album.getName() + " by " + artistName +
                    " (" + album.getReleaseYear() + ") - " + album.getGenre());
        }

        // Ask if user wants to see the tracks of a recommended album
        out.println("\nWould you like to see the tracks of one of these albums?");
        out.println("1. Yes");
        out.println("2. No");
        out.print("Your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 1) {
                viewRecommendedAlbumTracks(recommendations);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Handles user interaction for artist recommendations
     * @param userId ID of the current user
     */
    public void showArtistRecommendations(String userId) {
        out.println("\n========== ARTIST RECOMMENDATIONS ==========");

        // Check if the user has listened to any songs
        if (!hasListeningHistory(userId)) {
            return;
        }

        // Get recommendations
        List<Artist> recommendations = recommendationSystem.recommendArtists(userId, 5);

        if (recommendations.isEmpty()) {
            out.println("\nNo artist recommendations found based on your listening preferences.");
            out.println("Try listening to more songs by different artists and genres.");
            return;
        }

        // Display recommendations
        out.println("\nBased on your listening preferences, you might like these artists:");
        out.println("------------------------------------------------------------");

        for (int i = 0; i < recommendations.size(); i++) {
            Artist artist = recommendations.get(i);

            out.println((i + 1) + ". " + artist.getName());
            if (!artist.getBiography().isEmpty()) {
                out.println("   " + truncateBiography(artist.getBiography(), 100));
            }
        }

        // Ask if user wants to see more info about a recommended artist
        out.println("\nWould you like to see more info about one of these artists?");
        out.println("1. Yes");
        out.println("2. No");
        out.print("Your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 1) {
                viewRecommendedArtistDetails(recommendations);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Simulates listening to a song and updates user preferences
     * @param userId ID of the user
     * @param recommendations List of recommended songs
     */
    private void listenToRecommendedSong(String userId, List<Song> recommendations) {
        out.print("\nEnter the number of the song you want to listen to: ");

        try {
            int songNumber = Integer.parseInt(scanner.nextLine().trim());

            if (songNumber >= 1 && songNumber <= recommendations.size()) {
                Song selectedSong = recommendations.get(songNumber - 1);
                String artistName = selectedSong.getArtist() != null ? selectedSong.getArtist().getName() : "Unknown";

                out.println("\nNow playing: " + selectedSong.getName() + " by " + artistName);
                out.println("Duration: " + selectedSong.getFormattedDuration());

                // Simulate listening and update preferences
                recommendationSystem.recordSongPlay(userId, selectedSong.getId());

                out.println("\n[Song finished playing]");
                out.println("Do you like this song?");
                out.println("1. Yes, it's great!");
                out.println("2. It's okay");
                out.println("3. Not really");
                out.print("Your rating: ");

                try {
                    int rating = Integer.parseInt(scanner.nextLine().trim());

                    if (rating == 1) {
                        // User really liked it - boost genre and artist preference
                        recommendationSystem.updateGenrePreference(userId, selectedSong.getGenre(), 3);
                        if (selectedSong.getArtist() != null) {
                            recommendationSystem.updateArtistPreference(userId, selectedSong.getArtist().getId(), 3);
                        }
                        out.println("Great! We'll recommend more songs like this.");
                    } else if (rating == 3) {
                        // User didn't like it - reduce genre and artist preference
                        recommendationSystem.updateGenrePreference(userId, selectedSong.getGenre(), -1);
                        if (selectedSong.getArtist() != null) {
                            recommendationSystem.updateArtistPreference(userId, selectedSong.getArtist().getId(), -1);
                        }
                        out.println("Thanks for the feedback. We'll adjust our recommendations.");
                    } else {
                        out.println("Thanks for the feedback!");
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            } else {
                out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input.");
        }
    }

    /**
     * Shows tracks of a recommended album
     * @param recommendations List of recommended albums
     */
    private void viewRecommendedAlbumTracks(List<Album> recommendations) {
        out.print("\nEnter the number of the album you want to explore: ");

        try {
            int albumNumber = Integer.parseInt(scanner.nextLine().trim());

            if (albumNumber >= 1 && albumNumber <= recommendations.size()) {
                Album selectedAlbum = recommendations.get(albumNumber - 1);
                String artistName = selectedAlbum.getArtist() != null ? selectedAlbum.getArtist().getName() : "Unknown";

                out.println("\nAlbum: " + selectedAlbum.getName() + " by " + artistName);
                out.println("Released: " + selectedAlbum.getReleaseYear());
                out.println("Genre: " + selectedAlbum.getGenre());

                List<Song> albumSongs = service.getSongsByAlbum(selectedAlbum.getId());

                if (albumSongs.isEmpty()) {
                    out.println("\nNo tracks found for this album.");
                } else {
                    out.println("\nTracks:");
                    for (int i = 0; i < albumSongs.size(); i++) {
                        Song song = albumSongs.get(i);
                        out.println((i + 1) + ". " + song.getName() + " (" + song.getFormattedDuration() + ")");
                    }
                }
            } else {
                out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input.");
        }
    }

    /**
     * Shows details of a recommended artist
     * @param recommendations List of recommended artists
     */
    private void viewRecommendedArtistDetails(List<Artist> recommendations) {
        out.print("\nEnter the number of the artist you want to learn more about: ");

        try {
            int artistNumber = Integer.parseInt(scanner.nextLine().trim());

            if (artistNumber >= 1 && artistNumber <= recommendations.size()) {
                Artist selectedArtist = recommendations.get(artistNumber - 1);

                out.println("\nArtist: " + selectedArtist.getName());

                if (!selectedArtist.getBiography().isEmpty()) {
                    out.println("\nBiography:");
                    out.println(selectedArtist.getBiography());
                }

                List<Album> artistAlbums = service.getAlbumsByArtist(selectedArtist.getId());

                if (!artistAlbums.isEmpty()) {
                    out.println("\nAlbums:");
                    for (int i = 0; i < artistAlbums.size(); i++) {
                        Album album = artistAlbums.get(i);
                        out.println((i + 1) + ". " + album.getName() + " (" + album.getReleaseYear() + ")");
                    }
                }

                List<Song> popularSongs = service.getSongsByArtist(selectedArtist.getId());

                if (!popularSongs.isEmpty()) {
                    out.println("\nPopular Songs:");
                    int limit = Math.min(5, popularSongs.size());
                    for (int i = 0; i < limit; i++) {
                        Song song = popularSongs.get(i);
                        out.println((i + 1) + ". " + song.getName() + " (" + song.getFormattedDuration() + ")");
                    }
                }
            } else {
                out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid input.");
        }
    }

    /**
     * Checks if the user has any listening history
     * @param userId ID of the user
     * @return True if the user has listening history, false otherwise
     */
    private boolean hasListeningHistory(String userId) {
        // For demonstration, we'll simulate some listening history if none exists
        simulateListeningHistory(userId);

        return true;
    }

    /**
     * Simulates listening history for a new user
     * @param userId ID of the user
     */
    private void simulateListeningHistory(String userId) {
        // This is only for demonstration purposes
        // In a real application, this would be based on actual user activity

        List<Song> allSongs = service.getAllSongs();
        if (allSongs.isEmpty()) {
            out.println("There are no songs in your library yet.");
            out.println("Add some songs first to get recommendations.");
            return;
        }

        // Simulate listening to some random songs
        int listenedCount = 0;
        for (Song song : allSongs) {
            // Simulate listening to about 30% of songs in the library
            if (Math.random() < 0.3) {
                // Simulate listening to this song 1-5 times
                int playCount = (int) (Math.random() * 5) + 1;
                for (int i = 0; i < playCount; i++) {
                    recommendationSystem.recordSongPlay(userId, song.getId());
                }
                listenedCount++;
            }
        }

        if (listenedCount == 0 && !allSongs.isEmpty()) {
            // Ensure at least one song is "listened to"
            Song firstSong = allSongs.get(0);
            recommendationSystem.recordSongPlay(userId, firstSong.getId());
        }
    }

    /**
     * Shows the user's top genres based on listening history
     * @param userId ID of the user
     */
    private void showUserTopGenres(String userId) {
        Map<String, Integer> topGenres = recommendationSystem.getUserTopGenres(userId);

        if (topGenres.isEmpty()) {
            out.println("\nYou haven't established any genre preferences yet.");
            return;
        }

        out.println("\nYour top genres:");
        int count = 0;
        for (Map.Entry<String, Integer> entry : topGenres.entrySet()) {
            out.println("- " + entry.getKey());
            count++;
            if (count >= 3) break; // Show top 3 genres
        }
    }

    /**
     * Shows the user's top artists based on listening history
     * @param userId ID of the user
     */
    private void showUserTopArtists(String userId) {
        Map<String, Integer> topArtistIds = recommendationSystem.getUserTopArtists(userId);

        if (topArtistIds.isEmpty()) {
            out.println("\nYou haven't established any artist preferences yet.");
            return;
        }

        out.println("\nYour favorite artists:");
        int count = 0;
        for (Map.Entry<String, Integer> entry : topArtistIds.entrySet()) {
            String artistId = entry.getKey();
            Artist artist = service.getArtistById(artistId);
            if (artist != null) {
                out.println("- " + artist.getName());
                count++;
                if (count >= 3) break; // Show top 3 artists
            }
        }
    }

    /**
     * Helper method to truncate a long text
     * @param text Text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    private String truncateBiography(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }
}