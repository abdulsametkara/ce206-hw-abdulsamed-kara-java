package com.samet.music.service;

import com.samet.music.model.Album;
import com.samet.music.model.Artist;
import com.samet.music.model.Song;
import com.samet.music.monitoring.MetricsCollector;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * System for generating music recommendations based on user preferences
 * Implements the Strategy design pattern for different recommendation algorithms
 */
public class MusicRecommendationSystem {
    private static final Logger logger = LoggerFactory.getLogger(MusicRecommendationSystem.class);

    private final MusicCollectionService service;

    // Track user's listening history
    private Map<String, Map<String, Integer>> userListeningHistory; // userId -> (songId -> playCount)

    // Track user's genre preferences
    private Map<String, Map<String, Integer>> userGenrePreferences; // userId -> (genre -> score)

    // Track user's artist preferences
    private Map<String, Map<String, Integer>> userArtistPreferences; // userId -> (artistId -> score)

    // Recommendation counter metriği
    private final Counter recommendationCounter;

    // Singleton implementation
    private static MusicRecommendationSystem instance;

    private MusicRecommendationSystem() {
        this.service = MusicCollectionService.getInstance();
        this.userListeningHistory = new HashMap<>();
        this.userGenrePreferences = new HashMap<>();
        this.userArtistPreferences = new HashMap<>();

        // Recommendation counter oluştur
        this.recommendationCounter = Counter.build()
                .name("music_app_recommendations_total")
                .help("Yapılan toplam öneri sayısı")
                .labelNames("type")
                .register();

        logger.info("MusicRecommendationSystem initialized");
    }

    public static synchronized MusicRecommendationSystem getInstance() {
        if (instance == null) {
            instance = new MusicRecommendationSystem();
        }
        return instance;
    }

    /**
     * Record that a user has played a song
     * @param userId ID of the user
     * @param songId ID of the song
     */
    public void recordSongPlay(String userId, String songId) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("record_song_play");
        try {
            if (userId == null || songId == null) {
                logger.warn("Cannot record song play with null userId or songId");
                return;
            }

            logger.info("Recording song play for user: {}, song: {}", userId, songId);

            // Initialize user's history if not exists
            userListeningHistory.putIfAbsent(userId, new HashMap<>());

            // Update play count for this song
            Map<String, Integer> userHistory = userListeningHistory.get(userId);
            int newCount = userHistory.getOrDefault(songId, 0) + 1;
            userHistory.put(songId, newCount);
            logger.debug("Updated play count for song {} to {}", songId, newCount);

            // Update genre preference
            Song song = service.getSongById(songId);
            if (song != null) {
                updateGenrePreference(userId, song.getGenre(), 1);

                // Şarkı oynatıldı metriği
                if (song.getArtist() != null) {
                    MetricsCollector.getInstance().incrementSongPlay(
                            song.getArtist().getName(),
                            song.getGenre()
                    );
                }

                // Update artist preference
                if (song.getArtist() != null) {
                    updateArtistPreference(userId, song.getArtist().getId(), 1);
                }
            } else {
                logger.warn("Could not find song with ID {} for recording play", songId);
            }
        } catch (Exception e) {
            logger.error("Error recording song play: {}", e.getMessage(), e);
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Update a user's preference for a genre
     * @param userId ID of the user
     * @param genre Genre to update preference for
     * @param score Score to add to the preference
     */
    public void updateGenrePreference(String userId, String genre, int score) {
        if (genre == null || genre.isEmpty()) {
            logger.debug("Skipping genre preference update due to null/empty genre");
            return;
        }

        logger.debug("Updating genre preference for user {}, genre {}, score {}", userId, genre, score);

        // Initialize user's genre preferences if not exists
        userGenrePreferences.putIfAbsent(userId, new HashMap<>());

        // Update genre preference score
        Map<String, Integer> genrePrefs = userGenrePreferences.get(userId);
        int newScore = genrePrefs.getOrDefault(genre, 0) + score;
        genrePrefs.put(genre, newScore);
        logger.debug("Updated genre preference for {} to {}", genre, newScore);
    }

    /**
     * Update a user's preference for an artist
     * @param userId ID of the user
     * @param artistId ID of the artist
     * @param score Score to add to the preference
     */
    public void updateArtistPreference(String userId, String artistId, int score) {
        if (artistId == null || artistId.isEmpty()) {
            logger.debug("Skipping artist preference update due to null/empty artistId");
            return;
        }

        logger.debug("Updating artist preference for user {}, artist {}, score {}", userId, artistId, score);

        // Initialize user's artist preferences if not exists
        userArtistPreferences.putIfAbsent(userId, new HashMap<>());

        // Update artist preference score
        Map<String, Integer> artistPrefs = userArtistPreferences.get(userId);
        int newScore = artistPrefs.getOrDefault(artistId, 0) + score;
        artistPrefs.put(artistId, newScore);
        logger.debug("Updated artist preference for {} to {}", artistId, newScore);
    }

    /**
     * Get a map of the user's top genres by preference score
     * @param userId ID of the user
     * @return Map of genre -> score, sorted by score in descending order
     */
    public Map<String, Integer> getUserTopGenres(String userId) {
        try {
            logger.debug("Getting top genres for user: {}", userId);

            if (!userGenrePreferences.containsKey(userId)) {
                logger.debug("No genre preferences found for user {}", userId);
                return Collections.emptyMap();
            }

            // Sort genres by preference score
            Map<String, Integer> sortedGenres = userGenrePreferences.get(userId).entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            logger.debug("Found {} genre preferences for user {}", sortedGenres.size(), userId);
            return sortedGenres;
        } catch (Exception e) {
            logger.error("Error getting user top genres: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Get a map of the user's top artists by preference score
     * @param userId ID of the user
     * @return Map of artistId -> score, sorted by score in descending order
     */
    public Map<String, Integer> getUserTopArtists(String userId) {
        try {
            logger.debug("Getting top artists for user: {}", userId);

            if (!userArtistPreferences.containsKey(userId)) {
                logger.debug("No artist preferences found for user {}", userId);
                return Collections.emptyMap();
            }

            // Sort artists by preference score
            Map<String, Integer> sortedArtists = userArtistPreferences.get(userId).entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            logger.debug("Found {} artist preferences for user {}", sortedArtists.size(), userId);
            return sortedArtists;
        } catch (Exception e) {
            logger.error("Error getting user top artists: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Recommend songs based on the user's genre preferences
     * @param userId ID of the user
     * @param limit Maximum number of recommendations to return
     * @return List of recommended songs
     */
    public List<Song> recommendSongsByGenre(String userId, int limit) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("recommend_songs");
        try {
            logger.info("Generating song recommendations for user: {}, limit: {}", userId, limit);

            Map<String, Integer> topGenres = getUserTopGenres(userId);
            if (topGenres.isEmpty()) {
                logger.info("No genre preferences found for user: {}", userId);
                return Collections.emptyList();
            }

            // Get songs the user has already listened to
            Set<String> listenedSongIds = userListeningHistory.getOrDefault(userId, Collections.emptyMap()).keySet();

            // Get all songs
            List<Song> allSongs = service.getAllSongs();
            logger.debug("Found {} total songs for recommendation", allSongs.size());

            // Score songs based on genre match with user preferences
            List<ScoredSong> scoredSongs = new ArrayList<>();
            for (Song song : allSongs) {
                // Skip songs the user has already listened to
                if (listenedSongIds.contains(song.getId())) {
                    continue;
                }

                // Calculate score based on genre match
                String genre = song.getGenre();
                int score = topGenres.getOrDefault(genre, 0);

                if (score > 0) {
                    scoredSongs.add(new ScoredSong(song, score));
                }
            }

            // Sort by score (descending) and return the top N songs
            List<Song> recommendations = scoredSongs.stream()
                    .sorted(Comparator.comparingInt(ScoredSong::getScore).reversed())
                    .limit(limit)
                    .map(ScoredSong::getSong)
                    .collect(Collectors.toList());

            logger.info("Generated {} song recommendations for user: {}", recommendations.size(), userId);

            // Recommendation counter'ı artır
            recommendationCounter.labels("song").inc();

            return recommendations;
        } catch (Exception e) {
            logger.error("Error generating song recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Recommend songs by similar artists based on the user's artist preferences
     * @param userId ID of the user
     * @param limit Maximum number of recommendations to return
     * @return List of recommended songs
     */
    public List<Song> recommendSongsBySimilarArtist(String userId, int limit) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("recommend_songs_by_artist");
        try {
            logger.info("Generating similar artist song recommendations for user: {}, limit: {}", userId, limit);

            Map<String, Integer> topArtists = getUserTopArtists(userId);
            if (topArtists.isEmpty()) {
                logger.info("No artist preferences found for user: {}", userId);
                return Collections.emptyList();
            }

            // Get songs the user has already listened to
            Set<String> listenedSongIds = userListeningHistory.getOrDefault(userId, Collections.emptyMap()).keySet();

            // Get all songs
            List<Song> allSongs = service.getAllSongs();
            logger.debug("Found {} total songs for recommendation", allSongs.size());

            // Score songs based on artist match with user preferences
            List<ScoredSong> scoredSongs = new ArrayList<>();
            for (Song song : allSongs) {
                // Skip songs the user has already listened to
                if (listenedSongIds.contains(song.getId())) {
                    continue;
                }

                // Calculate score based on artist match
                if (song.getArtist() != null) {
                    int score = topArtists.getOrDefault(song.getArtist().getId(), 0);

                    if (score > 0) {
                        scoredSongs.add(new ScoredSong(song, score));
                    }
                }
            }

            // Sort by score (descending) and return the top N songs
            List<Song> recommendations = scoredSongs.stream()
                    .sorted(Comparator.comparingInt(ScoredSong::getScore).reversed())
                    .limit(limit)
                    .map(ScoredSong::getSong)
                    .collect(Collectors.toList());

            logger.info("Generated {} similar artist song recommendations for user: {}", recommendations.size(), userId);

            // Recommendation counter'ı artır
            recommendationCounter.labels("similar_artist_song").inc();

            return recommendations;
        } catch (Exception e) {
            logger.error("Error generating similar artist song recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Recommend new albums based on the user's artist preferences
     * @param userId ID of the user
     * @param limit Maximum number of recommendations to return
     * @return List of recommended albums
     */
    public List<Album> recommendAlbumsByArtist(String userId, int limit) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("recommend_albums");
        try {
            logger.info("Generating album recommendations for user: {}, limit: {}", userId, limit);

            Map<String, Integer> topArtists = getUserTopArtists(userId);
            if (topArtists.isEmpty()) {
                logger.info("No artist preferences found for user: {}", userId);
                return Collections.emptyList();
            }

            // Get all songs the user has listened to
            Map<String, Integer> userHistory = userListeningHistory.getOrDefault(userId, Collections.emptyMap());
            logger.debug("User has listened to {} songs", userHistory.size());

            // Build set of albums from songs the user has listened to
            Set<String> listenedAlbumIds = new HashSet<>();
            for (String songId : userHistory.keySet()) {
                Song song = service.getSongById(songId);
                if (song != null && song.getAlbum() != null) {
                    listenedAlbumIds.add(song.getAlbum().getId());
                }
            }
            logger.debug("User has listened to songs from {} albums", listenedAlbumIds.size());

            // Get all albums
            List<Album> allAlbums = service.getAllAlbums();
            logger.debug("Found {} total albums for recommendation", allAlbums.size());

            // Score albums based on artist match with user preferences
            List<ScoredAlbum> scoredAlbums = new ArrayList<>();
            for (Album album : allAlbums) {
                // Skip albums the user has already listened to
                if (listenedAlbumIds.contains(album.getId())) {
                    continue;
                }

                // Calculate score based on artist match
                if (album.getArtist() != null) {
                    int score = topArtists.getOrDefault(album.getArtist().getId(), 0);

                    if (score > 0) {
                        scoredAlbums.add(new ScoredAlbum(album, score));
                    }
                }
            }

            // Sort by score (descending) and return the top N albums
            List<Album> recommendations = scoredAlbums.stream()
                    .sorted(Comparator.comparingInt(ScoredAlbum::getScore).reversed())
                    .limit(limit)
                    .map(ScoredAlbum::getAlbum)
                    .collect(Collectors.toList());

            logger.info("Generated {} album recommendations for user: {}", recommendations.size(), userId);

            // Recommendation counter'ı artır
            recommendationCounter.labels("album").inc();

            return recommendations;
        } catch (Exception e) {
            logger.error("Error generating album recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Recommend artists based on the user's listening preferences
     * @param userId ID of the user
     * @param limit Maximum number of recommendations to return
     * @return List of recommended artists
     */
    public List<Artist> recommendArtists(String userId, int limit) {
        Histogram.Timer timer = MetricsCollector.getInstance().startRequestTimer("recommend_artists");
        try {
            logger.info("Generating artist recommendations for user: {}, limit: {}", userId, limit);

            // Get the user's top genres
            Map<String, Integer> topGenres = getUserTopGenres(userId);
            if (topGenres.isEmpty()) {
                logger.info("No genre preferences found for user: {}", userId);
                return Collections.emptyList();
            }

            // Get artists the user has already listened to
            Set<String> listenedArtistIds = userArtistPreferences.getOrDefault(userId, Collections.emptyMap()).keySet();
            logger.debug("User has listened to {} artists", listenedArtistIds.size());

            // Get all artists
            List<Artist> allArtists = service.getAllArtists();
            logger.debug("Found {} total artists for recommendation", allArtists.size());

            // Score artists based on genre match with user preferences
            List<ScoredArtist> scoredArtists = new ArrayList<>();
            for (Artist artist : allArtists) {
                // Skip artists the user already knows
                if (listenedArtistIds.contains(artist.getId())) {
                    continue;
                }

                // Calculate score based on genre match of artist's songs
                List<Song> artistSongs = service.getSongsByArtist(artist.getId());

                // Count genres for this artist's songs
                Map<String, Integer> artistGenreCounts = new HashMap<>();
                for (Song song : artistSongs) {
                    String genre = song.getGenre();
                    artistGenreCounts.put(genre, artistGenreCounts.getOrDefault(genre, 0) + 1);
                }

                // Calculate score based on overlap with user's preferred genres
                int score = 0;
                for (Map.Entry<String, Integer> entry : artistGenreCounts.entrySet()) {
                    String genre = entry.getKey();
                    int count = entry.getValue();
                    int genreScore = topGenres.getOrDefault(genre, 0);
                    score += count * genreScore;
                }

                if (score > 0) {
                    scoredArtists.add(new ScoredArtist(artist, score));
                }
            }

            // Sort by score (descending) and return the top N artists
            List<Artist> recommendations = scoredArtists.stream()
                    .sorted(Comparator.comparingInt(ScoredArtist::getScore).reversed())
                    .limit(limit)
                    .map(ScoredArtist::getArtist)
                    .collect(Collectors.toList());

            logger.info("Generated {} artist recommendations for user: {}", recommendations.size(), userId);

            // Recommendation counter'ı artır
            recommendationCounter.labels("artist").inc();

            return recommendations;
        } catch (Exception e) {
            logger.error("Error generating artist recommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            timer.observeDuration();
        }
    }

    /**
     * Save the recommendation system data
     * @param filepath Path to save the data to
     * @return True if successful, false otherwise
     */
    public boolean saveRecommendationData(String filepath) {
        try {
            logger.info("Saving recommendation data to: {}", filepath);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
                oos.writeObject(userListeningHistory);
                oos.writeObject(userGenrePreferences);
                oos.writeObject(userArtistPreferences);

                logger.info("Successfully saved recommendation data");
                return true;
            }
        } catch (Exception e) {
            logger.error("Error saving recommendation data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Load the recommendation system data
     * @param filepath Path to load the data from
     * @return True if successful, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean loadRecommendationData(String filepath) {
        try {
            logger.info("Loading recommendation data from: {}", filepath);

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
                userListeningHistory = (Map<String, Map<String, Integer>>) ois.readObject();
                userGenrePreferences = (Map<String, Map<String, Integer>>) ois.readObject();
                userArtistPreferences = (Map<String, Map<String, Integer>>) ois.readObject();

                logger.info("Successfully loaded recommendation data for {} users", userListeningHistory.size());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error loading recommendation data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Helper class for scoring songs for recommendation
     */
    private static class ScoredSong {
        private final Song song;
        private final int score;

        public ScoredSong(Song song, int score) {
            this.song = song;
            this.score = score;
        }

        public Song getSong() {
            return song;
        }

        public int getScore() {
            return score;
        }
    }

    /**
     * Helper class for scoring albums for recommendation
     */
    private static class ScoredAlbum {
        private final Album album;
        private final int score;

        public ScoredAlbum(Album album, int score) {
            this.album = album;
            this.score = score;
        }

        public Album getAlbum() {
            return album;
        }

        public int getScore() {
            return score;
        }
    }

    /**
     * Helper class for scoring artists for recommendation
     */
    private static class ScoredArtist {
        private final Artist artist;
        private final int score;

        public ScoredArtist(Artist artist, int score) {
            this.artist = artist;
            this.score = score;
        }

        public Artist getArtist() {
            return artist;
        }

        public int getScore() {
            return score;
        }
    }
}