package com.samet.music.dao;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.samet.music.model.User;
import com.samet.music.util.DatabaseUtil;

/**
 * Test class for UserDAO that mocks database access
 * This approach provides better code coverage without database access issues
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class UserDAOMockTest {

    private UserDAO userDAO;
    
    @Mock
    private Connection mockConn;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Before
    public void setUp() throws Exception {
        // Create a real DAO
        userDAO = new UserDAO();
    }
    
    private void setupMockConnection() throws SQLException {
        // Setup the mock connection
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockConn.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    }
    
    private User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    @Test
    public void testCreate() throws SQLException {
        // Test creating a user
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Setup mock ResultSet for last_insert_rowid
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            
            // Create test user
            User user = createTestUser();
            user.setId(0); // Reset ID as it would be when creating new user
            
            // Execute method under test
            User result = userDAO.create(user);
            
            // Verify
            assertNotNull("Should return non-null user", result);
            assertEquals("Should have ID 1", 1, result.getId());
            verify(mockPreparedStatement).setString(1, user.getUsername());
            verify(mockPreparedStatement).setString(2, user.getPassword());
            verify(mockPreparedStatement).setString(3, user.getEmail());
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConn).commit();
        }
    }
    
    @Test
    public void testCreateFailure() throws SQLException {
        // Test failure to create a user (no rows affected)
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
            
            // Create test user
            User user = createTestUser();
            
            // Execute method under test
            User result = userDAO.create(user);
            
            // Verify
            assertNull("Should return null on failure", result);
            verify(mockConn).rollback();
        }
    }
    
    @Test
    public void testCreateSQLException() throws SQLException {
        // Test SQLexception during creation
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockConn.getAutoCommit()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test exception"));
            
            // Create test user
            User user = createTestUser();
            
            // Execute method under test
            User result = userDAO.create(user);
            
            // Verify
            assertNull("Should return null on exception", result);
            verify(mockConn).rollback();
        }
    }
    
    @Test
    public void testFindById() throws SQLException {
        // Test finding user by ID
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with user data
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("username")).thenReturn("testuser");
            when(mockResultSet.getString("password")).thenReturn("password123");
            when(mockResultSet.getString("email")).thenReturn("test@example.com");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            Optional<User> result = userDAO.findById(1);
            
            // Verify
            assertTrue("Should return present Optional", result.isPresent());
            assertEquals("Should have ID 1", 1, result.get().getId());
            assertEquals("Should have username 'testuser'", "testuser", result.get().getUsername());
            
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }
    
    @Test
    public void testFindByIdNotFound() throws SQLException {
        // Test finding user that doesn't exist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            Optional<User> result = userDAO.findById(999);
            
            // Verify
            assertFalse("Should return empty Optional", result.isPresent());
            verify(mockPreparedStatement).setInt(1, 999);
        }
    }
    
    @Test
    public void testFindByUsername() throws SQLException {
        // Test finding user by username
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with user data
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("username")).thenReturn("testuser");
            when(mockResultSet.getString("password")).thenReturn("password123");
            when(mockResultSet.getString("email")).thenReturn("test@example.com");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            Optional<User> result = userDAO.findByUsername("testuser");
            
            // Verify
            assertTrue("Should return present Optional", result.isPresent());
            assertEquals("Should have username 'testuser'", "testuser", result.get().getUsername());
            
            verify(mockPreparedStatement).setString(1, "testuser");
        }
    }
    
    @Test
    public void testFindByUsernameNotFound() throws SQLException {
        // Test finding user by username that doesn't exist
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            Optional<User> result = userDAO.findByUsername("nonexistent");
            
            // Verify
            assertFalse("Should return empty Optional", result.isPresent());
            verify(mockPreparedStatement).setString(1, "nonexistent");
        }
    }
    
    @Test
    public void testFindAll() throws SQLException {
        // Test finding all users
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with multiple users
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("username")).thenReturn("user1", "user2");
            when(mockResultSet.getString("password")).thenReturn("pass1", "pass2");
            when(mockResultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
            
            // Execute method under test
            List<User> result = userDAO.findAll();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertEquals("Should return 2 users", 2, result.size());
            assertEquals("First user should have username 'user1'", "user1", result.get(0).getUsername());
            assertEquals("Second user should have username 'user2'", "user2", result.get(1).getUsername());
        }
    }
    
    @Test
    public void testFindAllEmpty() throws SQLException {
        // Test finding all users when there are none
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            List<User> result = userDAO.findAll();
            
            // Verify
            assertNotNull("Should return non-null list", result);
            assertTrue("Should return empty list", result.isEmpty());
        }
    }
    
    @Test
    public void testUpdate() throws SQLException {
        // Test updating a user
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Create test user with updated values
            User user = createTestUser();
            user.setUsername("updateduser");
            user.setEmail("updated@example.com");
            
            // Execute method under test
            boolean result = userDAO.update(user);
            
            // Verify
            assertTrue("Should return true for successful update", result);
            verify(mockPreparedStatement).setString(1, user.getUsername());
            verify(mockPreparedStatement).setString(2, user.getPassword());
            verify(mockPreparedStatement).setString(3, user.getEmail());
            verify(mockPreparedStatement).setInt(4, user.getId());
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testUpdateFailed() throws SQLException {
        // Test failed update (no rows affected)
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
            
            // Execute method under test
            boolean result = userDAO.update(createTestUser());
            
            // Verify
            assertFalse("Should return false for failed update", result);
        }
    }
    
    @Test
    public void testUpdateException() throws SQLException {
        // Test update with SQLException
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test exception"));
            
            // Execute method under test
            boolean result = userDAO.update(createTestUser());
            
            // Verify
            assertFalse("Should return false when exception occurs", result);
        }
    }
    
    @Test
    public void testDelete() throws SQLException {
        // Test deleting a user
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Execute method under test
            boolean result = userDAO.delete(1);
            
            // Verify
            assertTrue("Should return true for successful deletion", result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }
    
    @Test
    public void testDeleteFailed() throws SQLException {
        // Test failed deletion (no rows affected)
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected
            
            // Execute method under test
            boolean result = userDAO.delete(999);
            
            // Verify
            assertFalse("Should return false for failed deletion", result);
            verify(mockPreparedStatement).setInt(1, 999);
        }
    }
    
    @Test
    public void testDeleteException() throws SQLException {
        // Test delete with SQLException
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test exception"));
            
            // Execute method under test
            boolean result = userDAO.delete(1);
            
            // Verify
            assertFalse("Should return false when exception occurs", result);
        }
    }
    
    @Test
    public void testAuthenticate() throws SQLException {
        // Test successful authentication
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            
            // Setup mock ResultSet with user data
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getString("username")).thenReturn("testuser");
            when(mockResultSet.getString("password")).thenReturn("password123");
            when(mockResultSet.getString("email")).thenReturn("test@example.com");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Execute method under test
            Optional<User> result = userDAO.authenticate("testuser", "password123");
            
            // Verify
            assertTrue("Should return present Optional for successful auth", result.isPresent());
            assertEquals("Should have username 'testuser'", "testuser", result.get().getUsername());
            
            verify(mockPreparedStatement).setString(1, "testuser");
            verify(mockPreparedStatement).setString(2, "password123");
        }
    }
    
    @Test
    public void testAuthenticateFailed() throws SQLException {
        // Test failed authentication
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockResultSet.next()).thenReturn(false);
            
            // Execute method under test
            Optional<User> result = userDAO.authenticate("testuser", "wrongpassword");
            
            // Verify
            assertFalse("Should return empty Optional for failed auth", result.isPresent());
            verify(mockPreparedStatement).setString(1, "testuser");
            verify(mockPreparedStatement).setString(2, "wrongpassword");
        }
    }
    
    @Test
    public void testAuthenticateException() throws SQLException {
        // Test authentication with SQLException
        try (MockedStatic<DatabaseUtil> dbUtilMock = Mockito.mockStatic(DatabaseUtil.class)) {
            dbUtilMock.when(DatabaseUtil::getConnection).thenReturn(mockConn);
            
            setupMockConnection();
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test exception"));
            
            // Execute method under test
            Optional<User> result = userDAO.authenticate("testuser", "password123");
            
            // Verify
            assertFalse("Should return empty Optional when exception occurs", result.isPresent());
        }
    }
} 