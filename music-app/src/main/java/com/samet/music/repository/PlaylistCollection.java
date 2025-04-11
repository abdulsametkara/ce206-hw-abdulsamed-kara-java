package com.samet.music.repository;

import com.samet.music.dao.DAOFactory;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.model.Playlist;
import com.samet.music.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

/**
 * Repository for playlist operations
 */
public class PlaylistCollection extends MusicCollectionManager<Playlist> {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistCollection.class);
    private static volatile PlaylistCollection instance;
    private final PlaylistDAO playlistDAO;
    private final SongDAO songDAO;

    private PlaylistCollection() throws SQLException {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.playlistDAO = daoFactory.getPlaylistDAO();
        this.songDAO = daoFactory.getSongDAO();
        logger.info("PlaylistCollection başlatıldı");
    }

    public static synchronized PlaylistCollection getInstance() {
        if (instance == null) {
            try {
                instance = new PlaylistCollection();
            } catch (SQLException e) {
                logger.error("Error initializing PlaylistCollection: {}", e.getMessage(), e);
            }
        }
        return instance;
    }

    @Override
    protected String getItemId(Playlist item) {
        return item.getId();
    }

    @Override
    public void add(Playlist playlist) {
        if (playlist == null) {
            logger.warn("Null çalma listesi eklenemez");
            return;
        }

        logger.debug("Çalma listesi ekleniyor: {}", playlist.getName());

        super.add(playlist);

        try {
            playlistDAO.insert(playlist);
            logger.info("Çalma listesi başarıyla eklendi: {}", playlist.getName());
        } catch (Exception e) {
            logger.error("Çalma listesi veritabanına eklenirken hata: {}", e.getMessage(), e);
        }
    }

    @Override
    public Playlist getById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Geçersiz çalma listesi ID'si");
            return null;
        }

        logger.debug("ID'ye göre çalma listesi getiriliyor: {}", id);

        Playlist playlist = super.getById(id);

        if (playlist == null) {
            logger.debug("Çalma listesi önbellekte bulunamadı, veritabanından alınıyor");
            playlist = playlistDAO.getById(id);
            if (playlist != null) {
                super.add(playlist);
                logger.debug("Çalma listesi veritabanında bulundu ve önbelleğe eklendi: {}", playlist.getName());
            } else {
                logger.debug("Çalma listesi veritabanında da bulunamadı");
            }
        } else {
            logger.debug("Çalma listesi önbellekte bulundu: {}", playlist.getName());
        }

        return playlist;
    }

    @Override
    public List<Playlist> getAll() {
        logger.debug("Tüm çalma listeleri getiriliyor");
        return playlistDAO.getAll();
    }

    @Override
    public boolean remove(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Geçersiz çalma listesi ID'si");
            return false;
        }

        try {
            logger.info("Çalma listesi siliniyor. ID: {}", id);

            // Önce ana koleksiyondan kaldır
            boolean removed = super.remove(id);
            logger.debug("Önbellekten kaldırıldı: {}", removed);

            // Sonra DAO üzerinden sil
            playlistDAO.delete(id);
            logger.info("Çalma listesi veritabanından silindi");

            return true;
        } catch (Exception e) {
            logger.error("Çalma listesi silinirken hata: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void loadFromDatabase() {
        logger.info("Loading playlists from database...");
        clear(); // First clear existing items

        // Load playlists from database
        List<Playlist> playlists = playlistDAO.getAll();

        // Add to collection
        for (Playlist playlist : playlists) {
            super.add(playlist);
        }

        logger.info("{} playlists loaded", playlists.size());
    }

    public List<Playlist> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid search term");
            return new ArrayList<>();
        }

        logger.debug("Searching for playlist: {}", name);

        List<Playlist> results = new ArrayList<>();
        List<Playlist> allPlaylists = getAll();

        String searchTerm = name.toLowerCase();
        for (Playlist playlist : allPlaylists) {
            if (playlist.getName().toLowerCase().contains(searchTerm)) {
                results.add(playlist);
            }
        }

        logger.debug("Search result: {} playlists found", results.size());
        return results;
    }

    public List<Playlist> getPlaylistsContainingSong(Song song) {
        if (song == null) {
            logger.warn("Invalid song");
            return new ArrayList<>();
        }

        logger.debug("Getting playlists containing song: {}", song.getName());
        return playlistDAO.getPlaylistsContainingSong(String.valueOf(song));
    }

    public void addSongToPlaylist(String playlistId, String songId) {
        if (playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
            logger.warn("Geçersiz çalma listesi veya şarkı ID'si");
            return;
        }

        logger.info("Çalma listesine şarkı ekleniyor. Playlist ID: {}, Song ID: {}", playlistId, songId);
        playlistDAO.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(String playlistId, String songId) {
        if (playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
            logger.warn("Geçersiz çalma listesi veya şarkı ID'si");
            return;
        }

        logger.info("Çalma listesinden şarkı kaldırılıyor. Playlist ID: {}, Song ID: {}", playlistId, songId);
        playlistDAO.removeSongFromPlaylist(playlistId, songId);
    }

    public boolean saveToFile(String filePath) {
        logger.debug("SQLite kullanıldığı için dosyaya kaydetmeye gerek yok");
        return true;
    }

    public boolean loadFromFile(String filePath) {
        logger.info("Tüm çalma listeleri veritabanından yükleniyor");

        List<Playlist> playlists = playlistDAO.getAll();

        clear();

        for (Playlist playlist : playlists) {
            add(playlist);
        }

        logger.info("{} çalma listesi yüklendi", playlists.size());
        return !playlists.isEmpty();
    }
}