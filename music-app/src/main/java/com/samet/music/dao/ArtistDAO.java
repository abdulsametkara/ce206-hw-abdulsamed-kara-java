package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import com.samet.music.model.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Artist database operations DAO
 */
public class ArtistDAO extends BaseDAO<Artist> {
    private static final Logger logger = LoggerFactory.getLogger(ArtistDAO.class);
    
    private static final String SQL_CREATE_TABLE = 
        "CREATE TABLE IF NOT EXISTS artists (" +
        "id TEXT PRIMARY KEY," +
        "name TEXT NOT NULL" +
        ")";
    
    private static final String SQL_INSERT = "INSERT INTO artists (id, name) VALUES (?, ?)";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM artists WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM artists";
    private static final String SQL_UPDATE = "UPDATE artists SET name = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM artists WHERE id = ?";
    private static final String SQL_SEARCH_BY_NAME = "SELECT * FROM artists WHERE name LIKE ?";
    
    private static ArtistDAO instance;
    
    public ArtistDAO(DatabaseConnection dbConnection) {
        super("artists");
        createTable();
    }
    
    public static synchronized ArtistDAO getInstance(DatabaseConnection dbConnection) {
        if (instance == null) {
            instance = new ArtistDAO(dbConnection);
        }
        return instance;
    }
    
    @Override
    public void createTable() {
        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            dbConnection.executeUpdate(SQL_CREATE_TABLE, stmt -> {
                try {
                    stmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    logger.error("Error executing create table statement", e);
                    return false;
                }
            });
            logger.info("Artists table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating artists table", e);
            throw new RuntimeException("Failed to create artists table", e);
        }
    }
    
    @Override
    public boolean insert(Artist artist) {
        if (artist == null || artist.getId() == null || artist.getId().trim().isEmpty()) {
            logger.warn("Cannot insert artist with null or empty ID");
            return false;
        }
        
        return executeUpdate(SQL_INSERT, artist.getId(), artist.getName()) > 0;
    }
    
    @Override
    public Artist getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Cannot get artist with null or empty ID");
            return null;
        }
        
        return querySingle(SQL_SELECT_BY_ID, this::mapArtist, id);
    }
    
    @Override
    public List<Artist> getAll() {
        return queryList(SQL_SELECT_ALL, this::mapArtist);
    }
    
    @Override
    public boolean update(Artist artist) {
        if (artist == null || artist.getId() == null || artist.getId().trim().isEmpty()) {
            logger.warn("Cannot update artist with null or empty ID");
            return false;
        }
        
        return executeUpdate(SQL_UPDATE, artist.getName(), artist.getId()) > 0;
    }
    
    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Cannot delete artist with null or empty ID");
            return false;
        }
        
        return executeUpdate(SQL_DELETE, id) > 0;
    }
    
    /**
     * Searches for artists by name (partial match)
     * @param name Name to search for
     * @return List of matching artists
     */
    public List<Artist> searchByName(String name) {
        if (name == null || name.isEmpty()) {
            logger.warn("Invalid search name");
            return new ArrayList<>();
        }

        logger.debug("Searching artists by name: {}", name);
        
        return queryList(SQL_SEARCH_BY_NAME, this::mapArtist, "%" + name + "%");
    }
    
    /**
     * Merges two artists by moving all songs from source artist to target artist
     * and then deleting the source artist.
     * 
     * @param sourceId The ID of the artist to merge from
     * @param targetId The ID of the artist to merge into
     * @return true if merge was successful, false otherwise
     */
    public boolean mergeArtists(String sourceId, String targetId) {
        if (sourceId == null || targetId == null || sourceId.equals(targetId)) {
            return false;
        }

        // Check if both artists exist
        Artist source = getById(sourceId);
        Artist target = getById(targetId);
        
        if (source == null || target == null) {
            return false;
        }

        // Update all songs to point to target artist
        String updateSql = "UPDATE songs SET artist_id = ? WHERE artist_id = ?";
        executeUpdate(updateSql, targetId, sourceId);

        // Delete source artist
        delete(sourceId);
        
        return true;
    }

    /**
     * Maps ResultSet to Artist object
     * @param rs ResultSet
     * @return Artist object
     */
    private Artist mapArtist(ResultSet rs) throws SQLException {
        return new Artist(
            rs.getString("id"),
            rs.getString("name")
        );
    }

    @Override
    protected Artist mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapArtist(rs);
    }

    @Override
    protected void setCreateStatementParameters(PreparedStatement stmt, Artist artist) throws SQLException {
        stmt.setString(1, artist.getId());
        stmt.setString(2, artist.getName());
    }

    @Override
    protected void setUpdateStatementParameters(PreparedStatement stmt, Artist artist) throws SQLException {
        stmt.setString(1, artist.getName());
        stmt.setString(2, artist.getId());
    }
}