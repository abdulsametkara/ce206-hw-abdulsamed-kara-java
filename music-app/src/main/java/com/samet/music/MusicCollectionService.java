package com.samet.music;

import java.util.Collections;
import java.util.List;

/**
 * Service class for music collection operations
 * Implements Singleton pattern
 */
public class MusicCollectionService {
    private final ArtistCollection artistCollection;
    private final AlbumCollection albumCollection;
    private final SongCollection songCollection;
    private final MusicFactory musicFactory;
    private final PlaylistCollection playlistCollection;



    // Singleton implementation
    private static MusicCollectionService instance;

    private MusicCollectionService() {
        this.artistCollection = ArtistCollection.getInstance();
        this.albumCollection = AlbumCollection.getInstance();
        this.songCollection = SongCollection.getInstance();
        this.musicFactory = MusicFactory.getInstance();
        this.playlistCollection = PlaylistCollection.getInstance();


    }

    public static synchronized MusicCollectionService getInstance() {
        if (instance == null) {
            instance = new MusicCollectionService();
        }
        return instance;
    }

    // Artist operations
    public boolean addArtist(String name, String biography) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Artist artist = musicFactory.createArtist(name, biography);
        artistCollection.add(artist);
        return true;
    }

    public List<Artist> getAllArtists() {
        return artistCollection.getAll();
    }

    public Artist getArtistById(String id) {
        return artistCollection.getById(id);
    }

    public boolean removeArtist(String id) {
        return artistCollection.remove(id);
    }

    public List<Artist> searchArtistsByName(String name) {
        return artistCollection.searchByName(name);
    }

    // Album operations
    public boolean addAlbum(String name, String artistId, int releaseYear, String genre) {
        if (name == null || name.trim().isEmpty() || releaseYear <= 0) {
            return false;
        }

        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return false;
        }

        Album album = musicFactory.createAlbum(name, artist, releaseYear, genre);
        albumCollection.add(album);
        return true;
    }

    public List<Album> getAllAlbums() {
        return albumCollection.getAll();
    }

    public Album getAlbumById(String id) {
        return albumCollection.getById(id);
    }

    public boolean removeAlbum(String id) {
        return albumCollection.remove(id);
    }

    public List<Album> searchAlbumsByName(String name) {
        return albumCollection.searchByName(name);
    }

    public List<Album> getAlbumsByArtist(String artistId) {
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return Collections.emptyList();
        }
        return albumCollection.getByArtist(artist);
    }

    public List<Album> getAlbumsByGenre(String genre) {
        return albumCollection.getByGenre(genre);
    }

    // Song operations
    public boolean addSong(String name, String artistId, int duration, String genre) {
        if (name == null || name.trim().isEmpty() || duration <= 0) {
            return false;
        }

        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return false;
        }

        Song song = musicFactory.createSong(name, artist, duration, genre);
        songCollection.add(song);
        return true;
    }

    public boolean addSongToAlbum(String songId, String albumId) {
        Song song = songCollection.getById(songId);
        Album album = albumCollection.getById(albumId);

        if (song == null || album == null) {
            return false;
        }

        song.setAlbum(album);
        return true;
    }

    public List<Song> getAllSongs() {
        return songCollection.getAll();
    }

    public Song getSongById(String id) {
        return songCollection.getById(id);
    }

    public boolean removeSong(String id) {
        return songCollection.remove(id);
    }

    public List<Song> searchSongsByName(String name) {
        return songCollection.searchByName(name);
    }

    public List<Song> getSongsByArtist(String artistId) {
        Artist artist = artistCollection.getById(artistId);
        if (artist == null) {
            return Collections.emptyList();
        }
        return songCollection.getByArtist(artist);
    }

    public List<Song> getSongsByAlbum(String albumId) {
        Album album = albumCollection.getById(albumId);
        if (album == null) {
            return Collections.emptyList();
        }
        return songCollection.getByAlbum(album);
    }

    public List<Song> getSongsByGenre(String genre) {
        return songCollection.getByGenre(genre);
    }

    public boolean saveData(String artistFile, String albumFile, String songFile) {
        boolean artistSaved = artistCollection.saveToFile(artistFile);
        boolean albumSaved = albumCollection.saveToFile(albumFile);
        boolean songSaved = songCollection.saveToFile(songFile);

        return artistSaved && albumSaved && songSaved;
    }

    public boolean loadData(String artistFile, String albumFile, String songFile) {
        boolean artistLoaded = artistCollection.loadFromFile(artistFile);
        boolean albumLoaded = albumCollection.loadFromFile(albumFile);
        boolean songLoaded = songCollection.loadFromFile(songFile);

        return artistLoaded || albumLoaded || songLoaded;
    }

    public boolean createPlaylist(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Playlist playlist = new Playlist(name, description);
        playlistCollection.add(playlist);
        return true;
    }

    public List<Playlist> getAllPlaylists() {
        return playlistCollection.getAll();
    }

    public Playlist getPlaylistById(String id) {
        return playlistCollection.getById(id);
    }

    public boolean removePlaylist(String id) {
        return playlistCollection.remove(id);
    }

    public List<Playlist> searchPlaylistsByName(String name) {
        return playlistCollection.searchByName(name);
    }

    public boolean addSongToPlaylist(String songId, String playlistId) {
        Song song = songCollection.getById(songId);
        Playlist playlist = playlistCollection.getById(playlistId);

        if (song == null || playlist == null) {
            return false;
        }

        playlist.addSong(song);
        return true;
    }

    public boolean removeSongFromPlaylist(String songId, String playlistId) {
        Song song = songCollection.getById(songId);
        Playlist playlist = playlistCollection.getById(playlistId);

        if (song == null || playlist == null) {
            return false;
        }

        playlist.removeSong(song);
        return true;
    }

    public List<Song> getSongsInPlaylist(String playlistId) {
        Playlist playlist = playlistCollection.getById(playlistId);
        if (playlist == null) {
            return Collections.emptyList();
        }

        return playlist.getSongs();
    }

    public List<Playlist> getPlaylistsContainingSong(String songId) {
        Song song = songCollection.getById(songId);
        if (song == null) {
            return Collections.emptyList();
        }

        return playlistCollection.getPlaylistsContainingSong(song);
    }

    // Güncellenmiş saveData ve loadData metotları
    public boolean saveData(String artistFile, String albumFile, String songFile, String playlistFile) {
        boolean artistSaved = artistCollection.saveToFile(artistFile);
        boolean albumSaved = albumCollection.saveToFile(albumFile);
        boolean songSaved = songCollection.saveToFile(songFile);
        boolean playlistSaved = playlistCollection.saveToFile(playlistFile);

        return artistSaved && albumSaved && songSaved && playlistSaved;
    }

    public boolean loadData(String artistFile, String albumFile, String songFile, String playlistFile) {
        boolean artistLoaded = artistCollection.loadFromFile(artistFile);
        boolean albumLoaded = albumCollection.loadFromFile(albumFile);
        boolean songLoaded = songCollection.loadFromFile(songFile);
        boolean playlistLoaded = playlistCollection.loadFromFile(playlistFile);

        return artistLoaded || albumLoaded || songLoaded || playlistLoaded;
    }
}