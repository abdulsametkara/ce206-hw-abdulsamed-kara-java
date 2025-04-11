package com.samet.music.repository;

import com.samet.music.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all music collection repositories
 * @param <T> Entity type
 */
public abstract class MusicCollectionBase<T> implements IMusicCollection<T> {
    private static final Logger logger = LoggerFactory.getLogger(MusicCollectionBase.class);

    // In-memory collection
    protected final Map<String, T> items = new HashMap<>();

    // Flag to track if data has been loaded
    protected boolean isLoaded = false;

    // Database manager - static reference
    protected static final DatabaseManager dbManager = DatabaseManager.getInstance();

    /**
     * Get ID of the item
     * @param item Item
     * @return ID string
     */
    protected abstract String getItemId(T item);

    /**
     * Load data from database
     */
    protected abstract void loadFromDatabase();

    @Override
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        String id = getItemId(item);
        items.put(id, item);
    }

    @Override
    public boolean remove(String id) {
        return items.remove(id) != null;
    }

    @Override
    public T getById(String id) {
        return items.get(id);
    }

    @Override
    public java.util.List<T> getAll() {
        // Load from database if not loaded yet
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }
        return new java.util.ArrayList<>(items.values());
    }

    @Override
    public void clear() {
        items.clear();
        isLoaded = false;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean contains(String id) {
        return items.containsKey(id);
    }

    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    protected static Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    /**
     * Release a database connection
     * @param conn Connection to release
     */
    protected static void releaseConnection(Connection conn) {
        if (conn != null) {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Default implementation for saving to file - just returns true
     * as we're using SQLite and don't need file saving
     */
    @Override
    public boolean saveToFile(String filePath) {
        logger.debug("Using SQLite - no need to save to file");
        return true;
    }
}