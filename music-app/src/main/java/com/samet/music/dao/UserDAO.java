package com.samet.music.dao;

import com.samet.music.model.User;
import com.samet.music.db.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.PreparedStatement;

/**
 * Kullanıcı veritabanı işlemleri için DAO
 */
public class UserDAO extends BaseDAO<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // SQL queries
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM users";
    private static final String SQL_INSERT = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE users SET username = ?, password = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM users WHERE id = ?";

    // Singleton instance
    private static volatile UserDAO instance;

    // Ensure dbConnection is defined
    private final DatabaseConnection dbConnection;

    /**
     * Constructor
     */
    public UserDAO(DatabaseConnection dbConnection) {
        super(dbConnection);
        this.dbConnection = dbConnection;
    }

    /**
     * Returns the singleton instance
     */
    public static synchronized UserDAO getInstance(DatabaseConnection dbConnection) {
        if (instance == null) {
            instance = new UserDAO(dbConnection);
        }
        return instance;
    }

    @Override
    public boolean insert(User user) {
        if (user == null) {
            logger.warn("Cannot insert null user");
            return false;
        }

        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        try {
            return dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.setString(1, user.getId());
                    stmt.setString(2, user.getUsername());
                    stmt.setString(3, user.getPassword());
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    logger.error("Error inserting user: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing insert: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public User getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Cannot get user with null or empty ID");
            return null;
        }
        
        try {
            return querySingle(SQL_SELECT_BY_ID, rs -> {
                try {
                    if (rs.next()) {
                        return mapUser(rs);
                    }
                    return null;
                } catch (SQLException e) {
                    logger.error("Error mapping user: {}", e.getMessage());
                    return null;
                }
            }, id);
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<User> getAll() {
        return queryList(SQL_SELECT_ALL, this::mapUser);
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try {
            return dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.setString(1, user.getUsername());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getId());
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    logger.error("Error updating user: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing update: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try {
            return dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.setString(1, id);
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    logger.error("Error deleting user: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing delete: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcı tablosunun varlığını kontrol eder, yoksa oluşturur
     */
    @Override
    public void createTable() {
        logger.debug("Kullanıcı tablosu oluşturuluyor...");

        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)";

        try {
            dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.execute();
                    logger.info("Kullanıcı tablosu oluşturuldu veya zaten var");
                    return true;
                } catch (SQLException e) {
                    logger.error("Error creating users table: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing createTable: {}", e.getMessage());
            throw new RuntimeException("Failed to create users table", e);
        }
    }

    /**
     * Kullanıcı ekler veya günceller
     * @param username Kullanıcı adı
     * @param password Şifre
     * @return İşlem başarılı ise true
     */
    public boolean saveUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Geçersiz kullanıcı adı veya şifre");
            return false;
        }

        if (userExists(username)) {
            logger.warn("Bu kullanıcı adı zaten kullanımda: {}", username);
            return false;
        }

        logger.debug("Kullanıcı kaydediliyor: {}", username);

        // Generate a random ID for the user
        String id = java.util.UUID.randomUUID().toString();
        
        try {
            return insert(new User(id, username, password));
        } catch (Exception e) {
            logger.error("Error executing saveUser: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcının şifresini getirir
     * @param username Kullanıcı adı
     * @return Şifre, kullanıcı bulunamazsa null
     */
    public String getPassword(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Geçersiz kullanıcı adı");
            return null;
        }

        logger.debug("Kullanıcı şifresi getiriliyor: {}", username);

        String sql = "SELECT password FROM users WHERE username = ?";
        try {
            ResultSet rs = dbConnection.executeQuery(sql, stmt -> {
                try {
                    stmt.setString(1, username);
                    return stmt.executeQuery();
                } catch (SQLException e) {
                    logger.error("Error executing query: {}", e.getMessage());
                    return null;
                }
            });
            
            if (rs != null) {
                try {
                    return rs.next() ? rs.getString("password") : null;
                } catch (SQLException e) {
                    logger.error("Error reading result set: {}", e.getMessage());
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error executing getPassword: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kullanıcının var olup olmadığını kontrol eder
     * @param username Kullanıcı adı
     * @return Kullanıcı varsa true
     */
    public boolean userExists(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Geçersiz kullanıcı adı");
            return false;
        }

        return getPassword(username) != null;
    }

    /**
     * Kullanıcıyı siler
     * @param username Kullanıcı adı
     * @return İşlem başarılı ise true
     */
    public boolean deleteUser(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Geçersiz kullanıcı adı");
            return false;
        }

        logger.debug("Kullanıcı siliniyor: {}", username);

        String sql = "DELETE FROM users WHERE username = ?";
        try {
            return dbConnection.executeUpdate(sql, stmt -> {
                try {
                    stmt.setString(1, username);
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    logger.error("Error deleting user: {}", e.getMessage());
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error executing deleteUser: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Tüm kullanıcıları getirir
     * @return Kullanıcı adı -> şifre eşleşmesi
     */
    public Map<String, String> getAllUsers() {
        logger.debug("Tüm kullanıcılar getiriliyor");
        Map<String, String> users = new HashMap<>();

        String sql = "SELECT username, password FROM users";
        try {
            ResultSet rs = dbConnection.executeQuery(sql, stmt -> {
                try {
                    return stmt.executeQuery();
                } catch (SQLException e) {
                    logger.error("Error executing query: {}", e.getMessage());
                    return null;
                }
            });
            
            if (rs != null) {
                try {
                    while (rs.next()) {
                        users.put(rs.getString("username"), rs.getString("password"));
                    }
                } catch (SQLException e) {
                    logger.error("Error reading results: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error executing getAllUsers: {}", e.getMessage());
        }
        return users;
    }

    /**
     * Maps ResultSet to User object
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password")
        );
    }

    @Override
    protected void setCreateStatementParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getId());
        stmt.setString(2, user.getUsername());
        stmt.setString(3, user.getPassword());
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapUser(rs);
    }

    @Override
    protected void setUpdateStatementParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getPassword());
        stmt.setString(3, user.getId());
    }
}