package com.samet.music.util;

/**
 * Centralized SQL queries for better management
 */
public final class SQLQueries {

    // Prevent instantiation
    private SQLQueries() {}

    // Artist queries
    public static final class Artist {
        public static final String INSERT =
                "INSERT INTO artists (id, name, biography) VALUES (?, ?, ?)";

        public static final String SELECT_BY_ID =
                "SELECT * FROM artists WHERE id = ?";

        public static final String SELECT_ALL =
                "SELECT * FROM artists";

        public static final String UPDATE =
                "UPDATE artists SET name = ?, biography = ? WHERE id = ?";

        public static final String DELETE =
                "DELETE FROM artists WHERE id = ?";

        public static final String SEARCH_BY_NAME =
                "SELECT * FROM artists WHERE name LIKE ?";

        public static final String DELETE_CASCADE_PLAYLIST_SONGS =
                "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE artist_id = ?)";

        public static final String DELETE_CASCADE_SONGS =
                "DELETE FROM songs WHERE artist_id = ?";

        public static final String DELETE_CASCADE_ALBUMS =
                "DELETE FROM albums WHERE artist_id = ?";

        public static final String MERGE_UPDATE_SONGS =
                "UPDATE songs SET artist_id = ? WHERE artist_id = ?";

        public static final String MERGE_UPDATE_ALBUMS =
                "UPDATE albums SET artist_id = ? WHERE artist_id = ?";
    }

    // Album queries
    public static final class Album {
        public static final String INSERT =
                "INSERT INTO albums (id, name, artist_id, release_year, genre) VALUES (?, ?, ?, ?, ?)";

        public static final String SELECT_BY_ID =
                "SELECT * FROM albums WHERE id = ?";

        public static final String SELECT_ALL =
                "SELECT * FROM albums";

        public static final String UPDATE =
                "UPDATE albums SET name = ?, artist_id = ?, release_year = ?, genre = ? WHERE id = ?";

        public static final String DELETE =
                "DELETE FROM albums WHERE id = ?";

        public static final String SEARCH_BY_NAME =
                "SELECT * FROM albums WHERE name LIKE ?";

        public static final String SELECT_BY_ARTIST =
                "SELECT * FROM albums WHERE artist_id = ?";

        public static final String SELECT_BY_GENRE =
                "SELECT * FROM albums WHERE genre LIKE ?";

        public static final String DELETE_CASCADE_PLAYLIST_SONGS =
                "DELETE FROM playlist_songs WHERE song_id IN (SELECT id FROM songs WHERE album_id = ?)";

        public static final String DELETE_CASCADE_SONGS =
                "DELETE FROM songs WHERE album_id = ?";

        public static final String UPDATE_SONGS_REMOVE_ALBUM =
                "UPDATE songs SET album_id = NULL WHERE album_id = ?";
    }

    // Song queries
    public static final class Song {
        public static final String INSERT =
                "INSERT INTO songs (id, name, artist_id, album_id, duration, genre) VALUES (?, ?, ?, ?, ?, ?)";

        public static final String SELECT_BY_ID =
                "SELECT * FROM songs WHERE id = ?";

        public static final String SELECT_ALL =
                "SELECT * FROM songs";

        public static final String UPDATE =
                "UPDATE songs SET name = ?, artist_id = ?, album_id = ?, duration = ?, genre = ? WHERE id = ?";

        public static final String DELETE =
                "DELETE FROM songs WHERE id = ?";

        public static final String SEARCH_BY_NAME =
                "SELECT * FROM songs WHERE name LIKE ?";

        public static final String SELECT_BY_ARTIST =
                "SELECT * FROM songs WHERE artist_id = ?";

        public static final String SELECT_BY_ALBUM =
                "SELECT * FROM songs WHERE album_id = ?";

        public static final String SELECT_BY_GENRE =
                "SELECT * FROM songs WHERE genre LIKE ?";

        public static final String DELETE_FROM_PLAYLISTS =
                "DELETE FROM playlist_songs WHERE song_id = ?";
    }

    // Playlist queries
    public static final class Playlist {
        public static final String INSERT =
                "INSERT INTO playlists (id, name, description) VALUES (?, ?, ?)";

        public static final String SELECT_BY_ID =
                "SELECT * FROM playlists WHERE id = ?";

        public static final String SELECT_ALL =
                "SELECT * FROM playlists";

        public static final String UPDATE =
                "UPDATE playlists SET name = ?, description = ? WHERE id = ?";

        public static final String DELETE =
                "DELETE FROM playlists WHERE id = ?";

        public static final String SEARCH_BY_NAME =
                "SELECT * FROM playlists WHERE name LIKE ?";

        public static final String ADD_SONG =
                "INSERT OR IGNORE INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";

        public static final String REMOVE_SONG =
                "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

        public static final String SELECT_SONGS =
                "SELECT song_id FROM playlist_songs WHERE playlist_id = ?";

        public static final String SELECT_PLAYLISTS_FOR_SONG =
                "SELECT p.* FROM playlists p JOIN playlist_songs ps ON p.id = ps.playlist_id WHERE ps.song_id = ?";

        public static final String DELETE_ALL_SONGS =
                "DELETE FROM playlist_songs WHERE playlist_id = ?";
    }

    // User queries
    public static final class User {
        public static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT NOT NULL)";

        public static final String INSERT_OR_UPDATE =
                "INSERT OR REPLACE INTO users (username, password) VALUES (?, ?)";

        public static final String SELECT_PASSWORD =
                "SELECT password FROM users WHERE username = ?";

        public static final String DELETE =
                "DELETE FROM users WHERE username = ?";

        public static final String SELECT_ALL =
                "SELECT username, password FROM users";
    }

    // Schema creation queries
    public static final class Schema {
        public static final String CREATE_ARTISTS =
                "CREATE TABLE IF NOT EXISTS artists (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "biography TEXT)";

        public static final String CREATE_ALBUMS =
                "CREATE TABLE IF NOT EXISTS albums (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "artist_id TEXT, " +
                        "release_year INTEGER, " +
                        "genre TEXT, " +
                        "FOREIGN KEY (artist_id) REFERENCES artists(id))";

        public static final String CREATE_SONGS =
                "CREATE TABLE IF NOT EXISTS songs (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "artist_id TEXT, " +
                        "album_id TEXT, " +
                        "duration INTEGER, " +
                        "genre TEXT, " +
                        "FOREIGN KEY (artist_id) REFERENCES artists(id), " +
                        "FOREIGN KEY (album_id) REFERENCES albums(id))";

        public static final String CREATE_PLAYLISTS =
                "CREATE TABLE IF NOT EXISTS playlists (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT)";

        public static final String CREATE_PLAYLIST_SONGS =
                "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                        "playlist_id TEXT, " +
                        "song_id TEXT, " +
                        "PRIMARY KEY (playlist_id, song_id), " +
                        "FOREIGN KEY (playlist_id) REFERENCES playlists(id), " +
                        "FOREIGN KEY (song_id) REFERENCES songs(id))";
    }
}