package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.UserDAO;
import com.samet.music.model.User;

/**
 * Test sınıfı - UserController için testler
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    private UserController userController;
    private TestUserDAO userDAO;
    
    @Before
    public void setUp() {
        userDAO = new TestUserDAO();
        userController = new UserController() {
            @Override
            protected UserDAO createUserDAO() {
                return userDAO;
            }
        };
    }
    
    @Test
    public void testRegisterUser() {
        // Test verisi
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.setTestUserToReturn(testUser);
        
        // Test
        boolean result = userController.registerUser("testuser", "password", "test@example.com");
        
        // Doğrulama
        assertTrue("Kullanıcı kaydı başarılı olmalı", result);
    }
    
    @Test
    public void testRegisterUserWithExistingUsername() {
        // Test verisi - önceden var olan kullanıcı
        User existingUser = new User("existinguser", "password", "existing@example.com");
        existingUser.setId(1);
        userDAO.addTestUser(existingUser);
        
        // Test
        boolean result = userController.registerUser("existinguser", "newpassword", "new@example.com");
        
        // Doğrulama
        assertFalse("Var olan kullanıcı adıyla kayıt başarısız olmalı", result);
    }
    
    @Test
    public void testLoginUser() {
        // Test verisi
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.addTestUser(testUser);
        userDAO.setAuthenticatedUser(testUser);
        
        // Test
        boolean result = userController.loginUser("testuser", "password");
        
        // Doğrulama
        assertTrue("Kullanıcı girişi başarılı olmalı", result);
        assertNotNull("Kullanıcı oturumu açık olmalı", userController.getCurrentUser());
        assertEquals("Oturum açan kullanıcı doğru olmalı", "testuser", userController.getCurrentUser().getUsername());
    }
    
    @Test
    public void testLoginUserWithWrongCredentials() {
        // Test verisi
        userDAO.setAuthenticatedUser(null);
        
        // Test
        boolean result = userController.loginUser("wronguser", "wrongpassword");
        
        // Doğrulama
        assertFalse("Hatalı kimlik bilgileriyle giriş başarısız olmalı", result);
        assertNull("Kullanıcı oturumu açık olmamalı", userController.getCurrentUser());
    }
    
    @Test
    public void testLogoutUser() {
        // Test verisi - oturumu açık kullanıcı
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.setAuthenticatedUser(testUser);
        userController.loginUser("testuser", "password");
        
        // Test
        userController.logoutUser();
        
        // Doğrulama
        assertNull("Kullanıcı oturumu kapanmış olmalı", userController.getCurrentUser());
        assertFalse("isLoggedIn false dönmeli", userController.isLoggedIn());
    }
    
    @Test
    public void testUpdateUserProfile() {
        // Test verisi - oturumu açık kullanıcı
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.addTestUser(testUser);
        userDAO.setAuthenticatedUser(testUser);
        userController.loginUser("testuser", "password");
        
        // Test
        boolean result = userController.updateUserProfile("new@example.com", "newpassword");
        
        // Doğrulama
        assertTrue("Profil güncelleme başarılı olmalı", result);
        assertEquals("E-posta güncellenmiş olmalı", "new@example.com", userController.getCurrentUser().getEmail());
        assertEquals("Şifre güncellenmiş olmalı", "newpassword", userController.getCurrentUser().getPassword());
    }
    
    @Test
    public void testUpdateUserProfileWhenNoUserLoggedIn() {
        // Test
        boolean result = userController.updateUserProfile("new@example.com", "newpassword");
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken profil güncellenememeli", result);
    }
    
    @Test
    public void testDeleteAccount() {
        // Test verisi - oturumu açık kullanıcı
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.addTestUser(testUser);
        userDAO.setAuthenticatedUser(testUser);
        userController.loginUser("testuser", "password");
        
        // Test
        boolean result = userController.deleteAccount();
        
        // Doğrulama
        assertTrue("Hesap silme başarılı olmalı", result);
        assertNull("Kullanıcı oturumu kapanmış olmalı", userController.getCurrentUser());
    }
    
    @Test
    public void testDeleteAccountWhenNoUserLoggedIn() {
        // Test
        boolean result = userController.deleteAccount();
        
        // Doğrulama
        assertFalse("Kullanıcı oturumu yokken hesap silinememeli", result);
    }
    
    @Test
    public void testGetAllUsers() {
        // Test verisi
        User user1 = new User("user1", "password1", "user1@example.com");
        user1.setId(1);
        User user2 = new User("user2", "password2", "user2@example.com");
        user2.setId(2);
        
        userDAO.addTestUser(user1);
        userDAO.addTestUser(user2);
        
        // Test
        List<User> result = userController.getAllUsers();
        
        // Doğrulama
        assertNotNull("Kullanıcı listesi null olmamalı", result);
        assertEquals("Kullanıcı listesi doğru sayıda eleman içermeli", 2, result.size());
    }
    
    @Test
    public void testIsLoggedIn() {
        // Test verisi - oturumu açık kullanıcı
        User testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1);
        userDAO.setAuthenticatedUser(testUser);
        userController.loginUser("testuser", "password");
        
        // Test ve doğrulama
        assertTrue("Kullanıcı oturumu açıkken true dönmeli", userController.isLoggedIn());
        
        // Oturumu kapat
        userController.logoutUser();
        
        // Test ve doğrulama
        assertFalse("Kullanıcı oturumu kapalıyken false dönmeli", userController.isLoggedIn());
    }
    
    /**
     * Test amaçlı özel UserDAO uygulaması
     */
    private static class TestUserDAO extends UserDAO {
        private List<User> testUsers = new ArrayList<>();
        private User testUserToReturn = null;
        private User authenticatedUser = null;
        
        public void addTestUser(User user) {
            testUsers.add(user);
        }
        
        public void setTestUserToReturn(User user) {
            this.testUserToReturn = user;
        }
        
        public void setAuthenticatedUser(User user) {
            this.authenticatedUser = user;
        }
        
        @Override
        public User create(User user) {
            if (testUserToReturn != null) {
                return testUserToReturn;
            }
            
            user.setId(testUsers.size() + 1);
            testUsers.add(user);
            return user;
        }
        
        @Override
        public Optional<User> findById(int id) {
            return testUsers.stream()
                    .filter(u -> u.getId() == id)
                    .findFirst();
        }
        
        @Override
        public Optional<User> findByUsername(String username) {
            return testUsers.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst();
        }
        
        @Override
        public List<User> findAll() {
            return new ArrayList<>(testUsers);
        }
        
        @Override
        public boolean update(User user) {
            for (int i = 0; i < testUsers.size(); i++) {
                if (testUsers.get(i).getId() == user.getId()) {
                    testUsers.set(i, user);
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean delete(int id) {
            return testUsers.removeIf(u -> u.getId() == id);
        }
        
        @Override
        public Optional<User> authenticate(String username, String password) {
            if (authenticatedUser != null && 
                authenticatedUser.getUsername().equals(username) && 
                authenticatedUser.getPassword().equals(password)) {
                return Optional.of(authenticatedUser);
            }
            return Optional.empty();
        }
    }
} 