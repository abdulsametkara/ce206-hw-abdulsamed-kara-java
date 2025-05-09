package com.samet.music.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.dao.UserDAO;
import com.samet.music.model.User;

/**
 * UserController için test sınıfı
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserDAO mockUserDAO;
    
    private UserController userController;
    private User testUser;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // Test kullanıcısı oluştur
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        
        // UserController'ı oluştur ve private userDAO alanını mock ile değiştir
        userController = new UserController();
        
        // Reflection kullanarak private userDAO alanını erişilebilir yap ve mock ile değiştir
        java.lang.reflect.Field userDAOField = UserController.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(userController, mockUserDAO);
    }
    
    @Test
    public void testRegisterUser_Success() {
        // Test verileri
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        
        // Mock davranışları
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.empty());
        when(mockUserDAO.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2);
            return user;
        });
        
        // Test
        boolean result = userController.registerUser(username, password, email);
        
        // Doğrulama
        assertTrue(result);
        verify(mockUserDAO).findByUsername(username);
        verify(mockUserDAO).create(any(User.class));
    }
    
    @Test
    public void testRegisterUser_UsernameExists() {
        // Test verileri
        String username = "existinguser";
        String password = "password123";
        String email = "existing@example.com";
        
        // Mock davranışları
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.of(new User()));
        
        // Test
        boolean result = userController.registerUser(username, password, email);
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserDAO).findByUsername(username);
        verify(mockUserDAO, never()).create(any(User.class));
    }
    
    @Test
    public void testRegisterUser_CreationFailed() {
        // Test verileri
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        
        // Mock davranışları
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.empty());
        when(mockUserDAO.create(any(User.class))).thenReturn(null);
        
        // Test
        boolean result = userController.registerUser(username, password, email);
        
        // Doğrulama
        assertFalse(result);
        verify(mockUserDAO).findByUsername(username);
        verify(mockUserDAO).create(any(User.class));
    }
    
    @Test
    public void testLoginUser_Success() {
        // Test verileri
        String username = "testuser";
        String password = "password123";
        
        // Mock davranışları
        when(mockUserDAO.authenticate(username, password)).thenReturn(Optional.of(testUser));
        
        // Test
        boolean result = userController.loginUser(username, password);
        
        // Doğrulama
        assertTrue(result);
        assertEquals(testUser, userController.getCurrentUser());
        verify(mockUserDAO).authenticate(username, password);
    }
    
    @Test
    public void testLoginUser_Failed() {
        // Test verileri
        String username = "testuser";
        String password = "wrongpassword";
        
        // Mock davranışları
        when(mockUserDAO.authenticate(username, password)).thenReturn(Optional.empty());
        
        // Test
        boolean result = userController.loginUser(username, password);
        
        // Doğrulama
        assertFalse(result);
        assertNull(userController.getCurrentUser());
        verify(mockUserDAO).authenticate(username, password);
    }
    
    @Test
    public void testLogoutUser() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Kullanıcının giriş yaptığını doğrula
        assertNotNull(userController.getCurrentUser());
        
        // Test
        userController.logoutUser();
        
        // Doğrulama
        assertNull(userController.getCurrentUser());
    }
    
    @Test
    public void testLogoutUser_NoUserLoggedIn() {
        // Başlangıçta kullanıcı giriş yapmamış durumda
        assertNull(userController.getCurrentUser());
        
        // Test
        userController.logoutUser();
        
        // Doğrulama
        assertNull(userController.getCurrentUser());
    }
    
    @Test
    public void testUpdateUserProfile_Success() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test verileri
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword123";
        
        // Mock davranışları
        when(mockUserDAO.update(any(User.class))).thenReturn(true);
        
        // Test
        boolean result = userController.updateUserProfile(newEmail, newPassword);
        
        // Doğrulama
        assertTrue(result);
        assertEquals(newEmail, userController.getCurrentUser().getEmail());
        assertEquals(newPassword, userController.getCurrentUser().getPassword());
        verify(mockUserDAO).update(testUser);
    }
    
    @Test
    public void testUpdateUserProfile_NoUserLoggedIn() {
        // Test verileri
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword123";
        
        // Test
        boolean result = userController.updateUserProfile(newEmail, newPassword);
        
        // Doğrulama
        assertFalse(result);
        verifyNoInteractions(mockUserDAO);
    }
    
    @Test
    public void testUpdateUserProfile_UpdateFailed() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test verileri
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword123";
        
        // Mock davranışları
        when(mockUserDAO.update(any(User.class))).thenReturn(false);
        
        // Test
        boolean result = userController.updateUserProfile(newEmail, newPassword);
        
        // Doğrulama
        assertFalse(result);
        assertEquals(newEmail, userController.getCurrentUser().getEmail());
        assertEquals(newPassword, userController.getCurrentUser().getPassword());
        verify(mockUserDAO).update(testUser);
    }
    
    @Test
    public void testUpdateUserProfile_OnlyEmail() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test verileri
        String newEmail = "newemail@example.com";
        String originalPassword = testUser.getPassword();
        
        // Mock davranışları
        when(mockUserDAO.update(any(User.class))).thenReturn(true);
        
        // Test
        boolean result = userController.updateUserProfile(newEmail, null);
        
        // Doğrulama
        assertTrue(result);
        assertEquals(newEmail, userController.getCurrentUser().getEmail());
        assertEquals(originalPassword, userController.getCurrentUser().getPassword());
        verify(mockUserDAO).update(testUser);
    }
    
    @Test
    public void testUpdateUserProfile_OnlyPassword() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test verileri
        String originalEmail = testUser.getEmail();
        String newPassword = "newpassword123";
        
        // Mock davranışları
        when(mockUserDAO.update(any(User.class))).thenReturn(true);
        
        // Test
        boolean result = userController.updateUserProfile(null, newPassword);
        
        // Doğrulama
        assertTrue(result);
        assertEquals(originalEmail, userController.getCurrentUser().getEmail());
        assertEquals(newPassword, userController.getCurrentUser().getPassword());
        verify(mockUserDAO).update(testUser);
    }
    
    @Test
    public void testUpdateUserProfile_EmptyValues() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test verileri
        String originalEmail = testUser.getEmail();
        String originalPassword = testUser.getPassword();
        
        // Mock davranışları
        when(mockUserDAO.update(any(User.class))).thenReturn(true);
        
        // Test
        boolean result = userController.updateUserProfile("", "");
        
        // Doğrulama
        assertTrue(result);
        assertEquals(originalEmail, userController.getCurrentUser().getEmail());
        assertEquals(originalPassword, userController.getCurrentUser().getPassword());
        verify(mockUserDAO).update(testUser);
    }
    
    @Test
    public void testDeleteAccount_Success() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Mock davranışları
        when(mockUserDAO.delete(testUser.getId())).thenReturn(true);
        
        // Test
        boolean result = userController.deleteAccount();
        
        // Doğrulama
        assertTrue(result);
        assertNull(userController.getCurrentUser());
        verify(mockUserDAO).delete(testUser.getId());
    }
    
    @Test
    public void testDeleteAccount_NoUserLoggedIn() {
        // Test
        boolean result = userController.deleteAccount();
        
        // Doğrulama
        assertFalse(result);
        verifyNoInteractions(mockUserDAO);
    }
    
    @Test
    public void testDeleteAccount_DeleteFailed() {
        // Önce kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Mock davranışları
        when(mockUserDAO.delete(testUser.getId())).thenReturn(false);
        
        // Test
        boolean result = userController.deleteAccount();
        
        // Doğrulama
        assertFalse(result);
        assertNotNull(userController.getCurrentUser());
        verify(mockUserDAO).delete(testUser.getId());
    }
    
    @Test
    public void testGetAllUsers() {
        // Test verileri
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(testUser);
        expectedUsers.add(new User("user2", "pass2", "user2@example.com"));
        
        // Mock davranışları
        when(mockUserDAO.findAll()).thenReturn(expectedUsers);
        
        // Test
        List<User> result = userController.getAllUsers();
        
        // Doğrulama
        assertEquals(expectedUsers, result);
        verify(mockUserDAO).findAll();
    }
    
    @Test
    public void testGetCurrentUser() {
        // Başlangıçta kullanıcı giriş yapmamış durumda
        assertNull(userController.getCurrentUser());
        
        // Kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test
        User result = userController.getCurrentUser();
        
        // Doğrulama
        assertEquals(testUser, result);
    }
    
    @Test
    public void testIsLoggedIn() {
        // Başlangıçta kullanıcı giriş yapmamış durumda
        assertFalse(userController.isLoggedIn());
        
        // Kullanıcıyı giriş yaptır
        when(mockUserDAO.authenticate("testuser", "password123")).thenReturn(Optional.of(testUser));
        userController.loginUser("testuser", "password123");
        
        // Test
        boolean result = userController.isLoggedIn();
        
        // Doğrulama
        assertTrue(result);
    }
} 