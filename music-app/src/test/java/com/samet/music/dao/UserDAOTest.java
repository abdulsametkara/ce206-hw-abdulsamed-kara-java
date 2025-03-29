package com.samet.music.dao;

import static org.junit.Assert.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.samet.music.util.DatabaseUtil;

/**
 * @class UserDAOTest
 * @brief UserDAO sınıfı için test sınıfı
 */
public class UserDAOTest {

    private UserDAO userDAO;

    /**
     * @brief Tüm testlerden önce bir kez çalıştırılır
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Veritabanını test modunda başlat
        DatabaseUtil.setShouldResetDatabase(true);
        DatabaseUtil.initializeDatabase();
    }

    /**
     * @brief Her testten önce çalıştırılır
     */
    @Before
    public void setUp() throws Exception {
        // Her test öncesi yeni DAO örnekleri oluştur
        userDAO = new UserDAO();
        userDAO.createTable();
    }

    /**
     * @brief Her testten sonra çalıştırılır
     */
    @After
    public void tearDown() throws Exception {
        // Veritabanını temizle
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief saveUser metodunu test eder - yeni kullanıcı ekleme
     */
    @Test
    public void testSaveNewUser() throws Exception {
        // Arrange - Test için kullanıcı bilgileri oluştur
        String username = "test_user";
        String password = "test_password";

        // Act - Kullanıcıyı veritabanına ekle
        boolean result = userDAO.saveUser(username, password);

        // Assert - İşlemin başarılı olduğunu ve kullanıcının veritabanına eklendiğini kontrol et
        assertTrue("saveUser metodu başarılı olmalı", result);
        assertTrue("Kullanıcı veritabanında bulunmalı", userDAO.userExists(username));

        // Şifre doğru kaydedilmiş mi?
        String savedPassword = userDAO.getPassword(username);
        assertEquals("Şifre doğru kaydedilmemiş", password, savedPassword);
    }

    /**
     * @brief saveUser metodunu test eder - var olan kullanıcıyı güncelleme
     */
    @Test
    public void testSaveExistingUser() throws Exception {
        // Arrange - Önce bir kullanıcı oluştur ve ekle
        String username = "existing_user";
        String originalPassword = "original_password";
        String updatedPassword = "updated_password";

        // İlk kullanıcıyı ekle
        userDAO.saveUser(username, originalPassword);

        // Act - Aynı kullanıcı adı ile farklı şifre kullanarak güncelleme yap
        boolean result = userDAO.saveUser(username, updatedPassword);

        // Assert - İşlemin başarılı olduğunu ve şifrenin güncellendiğini kontrol et
        assertTrue("saveUser metodu başarılı olmalı", result);
        assertEquals("Şifre güncellenmiş olmalı", updatedPassword, userDAO.getPassword(username));
    }

    /**
     * @brief saveUser metodunu boş kullanıcı adı ve şifre ile test eder
     */
    @Test
    public void testSaveUserWithEmptyCredentials() {
        // Arrange - Boş kullanıcı adı ve şifre
        String emptyUsername = "";
        String emptyPassword = "";

        // Act & Assert - Boş kullanıcı adı ile kayıt
        boolean resultEmptyUsername = userDAO.saveUser(emptyUsername, "password");
        assertTrue("Boş kullanıcı adı kabul edilmeli", resultEmptyUsername);

        // Act & Assert - Boş şifre ile kayıt
        boolean resultEmptyPassword = userDAO.saveUser("username", emptyPassword);
        assertTrue("Boş şifre kabul edilmeli", resultEmptyPassword);
    }

    /**
     * @brief userExists metodunu test eder
     */
    @Test
    public void testUserExists() {
        // Arrange - Bir kullanıcı oluştur ve ekle
        String username = "exists_test";
        String password = "password";
        userDAO.saveUser(username, password);

        // Act & Assert - Var olan kullanıcı kontrolü
        assertTrue("Var olan kullanıcıyı bulmalı", userDAO.userExists(username));

        // Act & Assert - Var olmayan kullanıcı kontrolü
        assertFalse("Var olmayan kullanıcıyı bulmamalı", userDAO.userExists("nonexistent_user"));
    }

    /**
     * @brief deleteUser metodunu test eder
     */
    @Test
    public void testDeleteUser() {
        // Arrange - Bir kullanıcı oluştur ve ekle
        String username = "delete_test";
        String password = "password";
        userDAO.saveUser(username, password);

        // Act - Kullanıcıyı sil
        boolean result = userDAO.deleteUser(username);

        // Assert - İşlemin başarılı olduğunu ve kullanıcının silindiğini kontrol et
        assertTrue("deleteUser metodu başarılı olmalı", result);
        assertFalse("Kullanıcı artık var olmamalı", userDAO.userExists(username));
    }

    /**
     * @brief deleteUser metodunu var olmayan kullanıcı ile test eder
     */
    @Test
    public void testDeleteNonExistingUser() {
        // Act - Var olmayan kullanıcıyı silmeye çalış
        boolean result = userDAO.deleteUser("nonexistent_user");

        // Assert - İşlemin başarısız olduğunu kontrol et
        assertFalse("Var olmayan kullanıcıyı silmek başarısız olmalı", result);
    }

    /**
     * @brief getAllUsers metodunu test eder
     */
    @Test
    public void testGetAllUsers() {
        // Arrange - Birkaç kullanıcı oluştur ve ekle
        userDAO.saveUser("user1", "password1");
        userDAO.saveUser("user2", "password2");
        userDAO.saveUser("user3", "password3");

        // Act - Tüm kullanıcıları al
        Map<String, String> allUsers = userDAO.getAllUsers();

        // Assert - Eklenen tüm kullanıcıların var olduğunu kontrol et
        assertEquals("Kullanıcı sayısı doğru olmalı", 3, allUsers.size());
        assertEquals("user1 için şifre doğru olmalı", "password1", allUsers.get("user1"));
        assertEquals("user2 için şifre doğru olmalı", "password2", allUsers.get("user2"));
        assertEquals("user3 için şifre doğru olmalı", "password3", allUsers.get("user3"));
    }
}