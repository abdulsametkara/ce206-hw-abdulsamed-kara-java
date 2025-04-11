package com.samet.music.dao;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.samet.music.util.DatabaseManager;
import com.samet.music.db.DatabaseConnection;

public class UserDAOTest {

    private UserDAO userDAO;
    private DatabaseConnection dbConnection;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPass123";

    @Before
    public void setup() {
        dbConnection = new DatabaseConnection("jdbc:sqlite:test.db");
        // Setup test database with all necessary tables
        DatabaseTestSetup.setupTestDatabase(dbConnection);
        userDAO = UserDAO.getInstance(dbConnection);
        // Test öncesi temizlik
        cleanupTestUser();
    }

    private void cleanupTestUser() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
            stmt.setString(1, TEST_USERNAME);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Test kullanıcısı temizlenirken hata: " + e.getMessage());
        }
    }

    @Test
    public void testGetInstance() {
        UserDAO instance1 = UserDAO.getInstance(dbConnection);
        UserDAO instance2 = UserDAO.getInstance(dbConnection);
        
        assertNotNull("getInstance null döndürmemeli", instance1);
        assertSame("getInstance her zaman aynı instance'ı döndürmeli", instance1, instance2);
    }

    @Test
    public void testSaveUser_ValidInput() {
        boolean result = userDAO.saveUser(TEST_USERNAME, TEST_PASSWORD);
        assertTrue("Geçerli kullanıcı kaydı başarılı olmalı", result);
        
        // Kullanıcının kaydedildiğini doğrula
        assertTrue("Kaydedilen kullanıcı var olmalı", userDAO.userExists(TEST_USERNAME));
    }

    @Test
    public void testSaveUser_InvalidInput() {
        // Null username
        assertFalse("Null username kabul edilmemeli", 
            userDAO.saveUser(null, TEST_PASSWORD));

        // Boş username
        assertFalse("Boş username kabul edilmemeli", 
            userDAO.saveUser("", TEST_PASSWORD));

        // Null password
        assertFalse("Null password kabul edilmemeli", 
            userDAO.saveUser(TEST_USERNAME, null));

        // Boş password
        assertFalse("Boş password kabul edilmemeli", 
            userDAO.saveUser(TEST_USERNAME, ""));
    }

    @Test
    public void testSaveUser_DuplicateUsername() {
        // İlk kayıt
        assertTrue("İlk kullanıcı kaydı başarılı olmalı",
            userDAO.saveUser(TEST_USERNAME, TEST_PASSWORD));

        // Aynı username ile ikinci kayıt
        assertFalse("Aynı username ile ikinci kayıt başarısız olmalı",
            userDAO.saveUser(TEST_USERNAME, "differentPassword"));
    }

    @Test
    public void testGetPassword_ValidUser() {
        // Önce kullanıcıyı kaydet
        userDAO.saveUser(TEST_USERNAME, TEST_PASSWORD);

        // Şifreyi getir ve kontrol et
        String retrievedPassword = userDAO.getPassword(TEST_USERNAME);
        assertNotNull("Var olan kullanıcı için şifre null olmamalı", retrievedPassword);
        assertEquals("Getirilen şifre kaydedilen ile aynı olmalı", 
            TEST_PASSWORD, retrievedPassword);
    }

    @Test
    public void testGetPassword_InvalidUser() {
        // Null username
        assertNull("Null username için null dönmeli", 
            userDAO.getPassword(null));

        // Boş username
        assertNull("Boş username için null dönmeli", 
            userDAO.getPassword(""));

        // Var olmayan kullanıcı
        assertNull("Var olmayan kullanıcı için null dönmeli", 
            userDAO.getPassword("nonexistentUser"));
    }

    @Test
    public void testUserExists_ValidUser() {
        // Önce kullanıcıyı kaydet
        userDAO.saveUser(TEST_USERNAME, TEST_PASSWORD);

        // Varlık kontrolü
        assertTrue("Kaydedilen kullanıcı var olmalı", 
            userDAO.userExists(TEST_USERNAME));
    }

    @Test
    public void testUserExists_InvalidUser() {
        // Null username
        assertFalse("Null username için false dönmeli", 
            userDAO.userExists(null));

        // Boş username
        assertFalse("Boş username için false dönmeli", 
            userDAO.userExists(""));

        // Var olmayan kullanıcı
        assertFalse("Var olmayan kullanıcı için false dönmeli", 
            userDAO.userExists("nonexistentUser"));
    }

    @Test
    public void testDeleteUser_ValidUser() {
        // Önce kullanıcıyı kaydet
        userDAO.saveUser(TEST_USERNAME, TEST_PASSWORD);

        // Kullanıcıyı sil
        boolean deleteResult = userDAO.deleteUser(TEST_USERNAME);
        assertTrue("Var olan kullanıcı silinebilmeli", deleteResult);

        // Silinen kullanıcının artık var olmadığını kontrol et
        assertFalse("Silinen kullanıcı artık var olmamalı", 
            userDAO.userExists(TEST_USERNAME));
    }

    @Test
    public void testDeleteUser_InvalidUser() {
        // Null username
        assertFalse("Null username için false dönmeli", 
            userDAO.deleteUser(null));

        // Boş username
        assertFalse("Boş username için false dönmeli", 
            userDAO.deleteUser(""));

        // Var olmayan kullanıcı
        assertFalse("Var olmayan kullanıcı için false dönmeli", 
            userDAO.deleteUser("nonexistentUser"));
    }
}