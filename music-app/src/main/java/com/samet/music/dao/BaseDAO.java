package com.samet.music.dao;

import com.samet.music.db.DatabaseConnection;
import com.samet.music.model.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base DAO class for all data access objects
 * @param <T> Entity type
 */
public abstract class BaseDAO<T extends BaseEntity> {
    protected static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);

    protected final String tableName;
    protected final DatabaseConnection dbConnection;

    /**
     * Constructor with tableName
     */
    protected BaseDAO(String tableName) {
        this.tableName = tableName;
        this.dbConnection = null;
    }

    /**
     * Constructor with DatabaseConnection
     */
    protected BaseDAO(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.tableName = getClass().getSimpleName().replace("DAO", "s").toLowerCase();
    }

    /**
     * Inserts a new entity
     * @param entity Entity to insert
     * @return true if successful
     */
    public abstract boolean insert(T entity);

    /**
     * Gets entity by ID
     * @param id Entity ID
     * @return Found entity or null
     */
    public T getById(String id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("Error getting entity by ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets all entities
     * @return List of entities
     */
    public List<T> getAll() {
        String sql = "SELECT * FROM " + tableName;
        List<T> entities = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all entities: {}", e.getMessage());
        }

        return entities;
    }

    /**
     * Updates an entity
     * @param entity Entity to update
     * @return true if successful
     */
    public abstract boolean update(T entity);

    /**
     * Deletes an entity
     * @param id Entity ID to delete
     * @return true if successful
     */
    public abstract boolean delete(String id);

    /**
     * Creates the table for this entity if it doesn't exist
     */
    public abstract void createTable();

    /**
     * Executes a query that returns a single result
     * @param sql SQL query
     * @param mapper Result mapper function
     * @param params Query parameters
     * @return Query result
     */
    protected <R> R querySingle(String sql, ResultMapper<R> mapper, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("Error executing single query: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Executes a query that returns a list of results
     * @param sql SQL query
     * @param mapper Result mapper function
     * @param params Query parameters
     * @return Query result list
     */
    protected List<T> queryList(String sql, ResultMapper<T> mapper, Object... params) {
        List<T> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing list query: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE)
     * @param sql SQL query
     * @param params Query parameters
     * @return Number of affected rows
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error executing update: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Executes a transaction
     * @param action Transaction action to execute
     * @return true if successful
     */
    protected boolean executeTransaction(TransactionAction action) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean result = action.execute(conn);
                if (result) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction: {}", rollbackEx.getMessage());
                }
                logger.error("Error executing transaction: {}", e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error getting connection for transaction: {}", e.getMessage());
            return false;
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    protected interface ResultMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected interface TransactionAction {
        boolean execute(Connection conn) throws SQLException;
    }

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    protected abstract void setCreateStatementParameters(PreparedStatement stmt, T entity) throws SQLException;

    protected abstract void setUpdateStatementParameters(PreparedStatement stmt, T entity) throws SQLException;
}