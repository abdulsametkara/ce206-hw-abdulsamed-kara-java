package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import com.samet.music.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;

/**
 * Factory class for creating DAO instances
 */
public class DAOFactory {
    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);
    private static volatile DAOFactory instance;
    private static DatabaseConnection dbConnection;

    private volatile ArtistDAO artistDAO;
    private volatile AlbumDAO albumDAO;
    private volatile SongDAO songDAO;
    private volatile PlaylistDAO playlistDAO;
    private volatile UserDAO userDAO;

    private DAOFactory() {
        try {
            // Veritabanı bağlantısını al
            dbConnection = DatabaseManager.getInstance().getConnection();
            createTables();
        } catch (Exception e) {
            logger.error("Error initializing DAOFactory: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize DAOFactory", e);
        }
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    private void createTables() {
        logger.info("Creating database tables...");

        // Create tables in correct order for foreign key constraints
        try {
            getArtistDAO().createTable();
            getAlbumDAO().createTable();
            getSongDAO().createTable();
            getPlaylistDAO().createTable();
            getUserDAO().createTable();

            logger.info("Database tables created successfully");
        } catch (Exception e) {
            logger.error("Error creating database tables: {}", e.getMessage());
            throw new RuntimeException("Failed to create database tables", e);
        }
    }

    public ArtistDAO getArtistDAO() {
        if (artistDAO == null) {
            synchronized (this) {
                if (artistDAO == null) {
                    try {
                        artistDAO = new ArtistDAO(dbConnection);
                    } catch (Exception e) {
                        logger.error("Error creating ArtistDAO: {}", e.getMessage());
                        throw new RuntimeException("Failed to create ArtistDAO", e);
                    }
                }
            }
        }
        return artistDAO;
    }

    public AlbumDAO getAlbumDAO() {
        if (albumDAO == null) {
            synchronized (this) {
                if (albumDAO == null) {
                    try {
                        albumDAO = new AlbumDAO(dbConnection);
                    } catch (SQLException e) {
                        logger.error("Error creating AlbumDAO: {}", e.getMessage());
                        throw new RuntimeException("Failed to create AlbumDAO", e);
                    }
                }
            }
        }
        return albumDAO;
    }

    public SongDAO getSongDAO() {
        if (songDAO == null) {
            synchronized (this) {
                if (songDAO == null) {
                    try {
                        songDAO = new SongDAO(dbConnection);
                    } catch (SQLException e) {
                        logger.error("Error creating SongDAO: {}", e.getMessage());
                        throw new RuntimeException("Failed to create SongDAO", e);
                    }
                }
            }
        }
        return songDAO;
    }

    public PlaylistDAO getPlaylistDAO() throws SQLException {
        if (playlistDAO == null) {
            synchronized (this) {
                if (playlistDAO == null) {
                    playlistDAO = new PlaylistDAO(dbConnection);
                }
            }
        }
        return playlistDAO;
    }

    public UserDAO getUserDAO() {
        if (userDAO == null) {
            synchronized (this) {
                if (userDAO == null) {
                    userDAO = new UserDAO(dbConnection);
                }
            }
        }
        return userDAO;
    }

    public void closeConnection() {
        if (dbConnection != null) {
            try {
                dbConnection.getConnection().close();
            } catch (Exception e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
}