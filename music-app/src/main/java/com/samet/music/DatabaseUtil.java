package com.samet.music;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:music_library.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Artists tablosu
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS artists (" +
                            "    id TEXT PRIMARY KEY," +
                            "    name TEXT NOT NULL," +
                            "    biography TEXT" +
                            ")"
            );

            // Albums tablosu
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS albums (" +
                            "    id TEXT PRIMARY KEY," +
                            "    name TEXT NOT NULL," +
                            "    artist_id TEXT," +
                            "    release_year INTEGER," +
                            "    genre TEXT," +
                            "    FOREIGN KEY(artist_id) REFERENCES artists(id)" +
                            ")"
            );

            // Songs tablosu
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS songs (" +
                            "    id TEXT PRIMARY KEY," +
                            "    name TEXT NOT NULL," +
                            "    artist_id TEXT," +
                            "    album_id TEXT," +
                            "    duration INTEGER," +
                            "    genre TEXT," +
                            "    FOREIGN KEY(artist_id) REFERENCES artists(id)," +
                            "    FOREIGN KEY(album_id) REFERENCES albums(id)" +
                            ")"
            );

            // Playlists tablosu
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                            "    id TEXT PRIMARY KEY," +
                            "    name TEXT NOT NULL," +
                            "    description TEXT" +
                            ")"
            );

            // Playlist Songs tablosu
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                            "    playlist_id TEXT," +
                            "    song_id TEXT," +
                            "    PRIMARY KEY(playlist_id, song_id)," +
                            "    FOREIGN KEY(playlist_id) REFERENCES playlists(id)," +
                            "    FOREIGN KEY(song_id) REFERENCES songs(id)" +
                            ")"
            );

            System.out.println("Database tables created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}