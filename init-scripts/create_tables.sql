-- Mevcut SQLite tablolarınızı PostgreSQL formatına dönüştürün
CREATE TABLE IF NOT EXISTS artists (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    biography TEXT
);

CREATE TABLE IF NOT EXISTS albums (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    artist_id TEXT REFERENCES artists(id),
    release_year INTEGER,
    genre TEXT
);

CREATE TABLE IF NOT EXISTS songs (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    artist_id TEXT REFERENCES artists(id),
    album_id TEXT REFERENCES albums(id),
    duration INTEGER,
    genre TEXT
);

CREATE TABLE IF NOT EXISTS playlists (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS playlist_songs (
    playlist_id TEXT REFERENCES playlists(id),
    song_id TEXT REFERENCES songs(id),
    PRIMARY KEY(playlist_id, song_id)
);

CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL
);