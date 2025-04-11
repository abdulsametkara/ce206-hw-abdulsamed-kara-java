package com.samet.music.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

/**
 * Database connection manager
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:music.db";
    private static Connection connection;
    private final String url;

    public DatabaseConnection() {
        this(DEFAULT_DB_URL);
    }

    public DatabaseConnection(String dbUrl) {
        this.url = dbUrl;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DEFAULT_DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed();
    }

    public boolean executeUpdate(String sql, Function<PreparedStatement, Boolean> statementHandler) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        try {
            return statementHandler.apply(stmt);
        } finally {
            stmt.close();
        }
    }

    public ResultSet executeQuery(String sql, Function<PreparedStatement, ResultSet> statementHandler) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        try {
            ResultSet rs = statementHandler.apply(stmt);
            return rs;
        } catch (Exception e) {
            stmt.close();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException("Sorgu çalıştırılırken hata oluştu", e);
            }
        }
    }

    public Boolean executeTransaction(Function<Connection, Boolean> transaction) throws SQLException {
        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);
            
            Boolean result = transaction.apply(conn);
            
            if (result != null && result) {
                conn.commit();
                logger.info("Transaction committed successfully");
            } else {
                conn.rollback();
                logger.info("Transaction rolled back");
            }
            
            return result;
        } catch (SQLException e) {
            try {
                logger.warn("Transaction error, performing rollback: {}", e.getMessage());
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Transaction rollback failed: {}", rollbackEx.getMessage());
            }
            logger.error("Transaction failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ex) {
                logger.error("Failed to restore autoCommit setting: {}", ex.getMessage());
            }
        }
    }

    /**
     * Closes the database connection.
     * 
     * @throws SQLException if a database error occurs
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            logger.info("Database connection closed");
        }
    }
} 