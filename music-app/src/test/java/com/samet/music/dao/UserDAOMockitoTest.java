package com.samet.music.dao;

import com.samet.music.model.User;
import com.samet.music.util.DatabaseUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * UserDAO için Mockito kullanarak test eden sınıf
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDAOMockitoTest {
    
    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private Statement mockStatement;
    @Mock private ResultSet mockResultSet;
    
    // Test edilecek nesne
    private UserDAO userDAO;
    private MockedStatic<DatabaseUtil> mockedDatabaseUtil;
    
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        
        userDAO = new UserDAO();
        
        // Mock DatabaseUtil.getConnection()
        mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        mockedDatabaseUtil.when(DatabaseUtil::getConnection).thenReturn(mockConnection);
    }
    
    @org.junit.After
    public void tearDown() {
        // Close the mocked static after each test
        if (mockedDatabaseUtil != null) {
            mockedDatabaseUtil.close();
        }
    }
    
    /**
     * Kullanıcı oluşturma metodunu test eder
     */
    @Test
    public void testCreate() throws SQLException {
        // Test verileri
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("last_insert_rowid()"))).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        
        // Metodu çağır
        User result = userDAO.create(user);
        
        // Sonuçları doğrula
        assertNotNull("Oluşturulan kullanıcı null olmamalı", result);
        assertEquals("Kullanıcı ID doğru ayarlanmalı", 1, result.getId());
        assertEquals("Kullanıcı adı korunmalı", "testuser", result.getUsername());
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        verify(mockPreparedStatement).setString(1, user.getUsername());
        verify(mockPreparedStatement).setString(2, user.getPassword());
        verify(mockPreparedStatement).setString(3, user.getEmail());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    /**
     * Kullanıcı oluşturma metodunun SQLException durumunu test eder
     */
    @Test
    public void testCreateWithSQLException() throws SQLException {
        // Test verileri
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        User result = userDAO.create(user);
        
        // Sonuçları doğrula
        assertNull("Hata durumunda null dönmeli", result);
        
        // Etkileşimleri doğrula
        verify(mockConnection).setAutoCommit(false);
        // Rollback ve auto-commit reset doğrulaması yapılmaz çünkü exception mockConnection.prepareStatement'ten fırlatılır
    }
    
    /**
     * ID ile kullanıcı bulma metodunu test eder
     */
    @Test
    public void testFindById() throws SQLException {
        // Test verileri
        int userId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(userId);
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("password")).thenReturn("password123");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Metodu çağır
        Optional<User> result = userDAO.findById(userId);
        
        // Sonuçları doğrula
        assertTrue("Kullanıcı bulunmalı", result.isPresent());
        assertEquals("Kullanıcı ID eşleşmeli", userId, result.get().getId());
        assertEquals("Kullanıcı adı eşleşmeli", "testuser", result.get().getUsername());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * ID ile kullanıcı bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindByIdWithSQLException() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Optional<User> result = userDAO.findById(userId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda boş Optional dönmeli", result.isPresent());
    }
    
    /**
     * Kullanıcı adına göre kullanıcı bulma metodunu test eder
     */
    @Test
    public void testFindByUsername() throws SQLException {
        // Test verileri
        String username = "testuser";
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("password123");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Metodu çağır
        Optional<User> result = userDAO.findByUsername(username);
        
        // Sonuçları doğrula
        assertTrue("Kullanıcı bulunmalı", result.isPresent());
        assertEquals("Kullanıcı adı eşleşmeli", username, result.get().getUsername());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Kullanıcı adına göre kullanıcı bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindByUsernameWithSQLException() throws SQLException {
        // Test verileri
        String username = "testuser";
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Optional<User> result = userDAO.findByUsername(username);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda boş Optional dönmeli", result.isPresent());
    }
    
    /**
     * Tüm kullanıcıları bulma metodunu test eder
     */
    @Test
    public void testFindAll() throws SQLException {
        // Mock davranışlarını ayarla
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Bir kayıt var
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("password")).thenReturn("password123");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Metodu çağır
        List<User> result = userDAO.findAll();
        
        // Sonuçları doğrula
        assertEquals("1 kullanıcı dönmeli", 1, result.size());
        assertEquals("Kullanıcı adı doğru olmalı", "testuser", result.get(0).getUsername());
    }
    
    /**
     * Tüm kullanıcıları bulma metodunun SQLException durumunu test eder
     */
    @Test
    public void testFindAllWithSQLException() throws SQLException {
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.createStatement()).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        List<User> result = userDAO.findAll();
        
        // Sonuçları doğrula
        assertTrue("Hata durumunda boş liste dönmeli", result.isEmpty());
    }
    
    /**
     * Kullanıcı güncelleme metodunu test eder
     */
    @Test
    public void testUpdate() throws SQLException {
        // Test verileri
        User user = new User();
        user.setId(1);
        user.setUsername("updateduser");
        user.setPassword("newpassword");
        user.setEmail("updated@example.com");
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = userDAO.update(user);
        
        // Sonuçları doğrula
        assertTrue("Güncelleme başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, user.getUsername());
        verify(mockPreparedStatement).setString(2, user.getPassword());
        verify(mockPreparedStatement).setString(3, user.getEmail());
        verify(mockPreparedStatement).setInt(4, user.getId());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Kullanıcı güncelleme metodunun SQLException durumunu test eder
     */
    @Test
    public void testUpdateWithSQLException() throws SQLException {
        // Test verileri
        User user = new User();
        user.setId(1);
        user.setUsername("updateduser");
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = userDAO.update(user);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Kullanıcı silme metodunu test eder
     */
    @Test
    public void testDelete() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 satır etkilendi
        
        // Metodu çağır
        boolean result = userDAO.delete(userId);
        
        // Sonuçları doğrula
        assertTrue("Silme işlemi başarılı olmalı", result);
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    /**
     * Kullanıcı silme metodunun SQLException durumunu test eder
     */
    @Test
    public void testDeleteWithSQLException() throws SQLException {
        // Test verileri
        int userId = 1;
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        boolean result = userDAO.delete(userId);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda false dönmeli", result);
    }
    
    /**
     * Kullanıcı kimlik doğrulama metodunu test eder
     */
    @Test
    public void testAuthenticate() throws SQLException {
        // Test verileri
        String username = "testuser";
        String password = "password123";
        LocalDateTime createdAt = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // ResultSet veri dönüşlerini ayarla
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn(password);
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Metodu çağır
        Optional<User> result = userDAO.authenticate(username, password);
        
        // Sonuçları doğrula
        assertTrue("Kullanıcı doğrulanmalı", result.isPresent());
        assertEquals("Kullanıcı adı eşleşmeli", username, result.get().getUsername());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).setString(2, password);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Başarısız kullanıcı kimlik doğrulama metodunu test eder
     */
    @Test
    public void testAuthenticateFailure() throws SQLException {
        // Test verileri
        String username = "testuser";
        String password = "wrongpassword";
        
        // Mock davranışlarını ayarla
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Kimlik doğrulama başarısız
        
        // Metodu çağır
        Optional<User> result = userDAO.authenticate(username, password);
        
        // Sonuçları doğrula
        assertFalse("Kullanıcı doğrulanmamalı", result.isPresent());
        
        // Etkileşimleri doğrula
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).setString(2, password);
        verify(mockPreparedStatement).executeQuery();
    }
    
    /**
     * Kullanıcı kimlik doğrulama metodunun SQLException durumunu test eder
     */
    @Test
    public void testAuthenticateWithSQLException() throws SQLException {
        // Test verileri
        String username = "testuser";
        String password = "password123";
        
        // Mock davranışlarını ayarla - exception fırlat
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        
        // Metodu çağır
        Optional<User> result = userDAO.authenticate(username, password);
        
        // Sonuçları doğrula
        assertFalse("Hata durumunda boş Optional dönmeli", result.isPresent());
    }
} 