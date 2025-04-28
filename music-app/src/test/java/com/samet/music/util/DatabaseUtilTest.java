package com.samet.music.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for DatabaseUtil
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseUtilTest {

    private Connection mockConnection;
    private Statement mockStatement;

    @Before
    public void setUp() throws SQLException {
        // Create mock objects
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        
        // Setup connection to return mock statement
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    /**
     * Test the initializeDatabase method
     */
    @Test
    public void testInitializeDatabase() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            // Setup
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            
            // Make sure the connection field is reset
            setConnectionField(null);
            
            // Execute
            DatabaseUtil.initializeDatabase();
            
            // Verify a connection was established
            driverManagerMock.verify(() -> DriverManager.getConnection(anyString()));
            
            // Verify createStatement was called to create tables
            verify(mockConnection).createStatement();
            
            // Verify execute was called for creating tables (multiple times)
            verify(mockStatement, atLeast(5)).execute(anyString());
        }
    }

    /**
     * Test getConnection when connection is null
     */
    @Test
    public void testGetConnectionWhenNull() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            // Setup
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            
            // Reset connection field
            setConnectionField(null);
            
            // Execute
            Connection result = DatabaseUtil.getConnection();
            
            // Verify a new connection was created
            driverManagerMock.verify(() -> DriverManager.getConnection(anyString()));
            
            // Verify the result is our mock connection
            assertSame(mockConnection, result);
        }
    }

    /**
     * Test getConnection when connection is closed
     */
    @Test
    public void testGetConnectionWhenClosed() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            // Setup
            driverManagerMock.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            
            // Set connection field
            setConnectionField(mockConnection);
            
            // Setup connection to report as closed
            when(mockConnection.isClosed()).thenReturn(true);
            
            // Execute
            Connection result = DatabaseUtil.getConnection();
            
            // Verify a new connection was created
            driverManagerMock.verify(() -> DriverManager.getConnection(anyString()));
            
            // Verify the result is our mock connection
            assertSame(mockConnection, result);
        }
    }

    /**
     * Test closeConnection method
     */
    @Test
    public void testCloseConnection() throws SQLException {
        // Make sure connection is set
        setConnectionField(mockConnection);
        
        // Setup expected behavior
        when(mockConnection.isClosed()).thenReturn(false);
        
        // Execute
        DatabaseUtil.closeConnection();
        
        // Verify isClosed was called to check connection status
        verify(mockConnection).isClosed();
        
        // Verify close was called
        verify(mockConnection).close();
    }

    /**
     * Test closeConnection when connection is null
     */
    @Test
    public void testCloseConnectionWhenNull() {
        // Reset the static connection
        setConnectionField(null);
        
        // Execute - should not throw exception
        DatabaseUtil.closeConnection();
    }

    /**
     * Test closeConnection when connection is already closed
     */
    @Test
    public void testCloseConnectionWhenAlreadyClosed() throws SQLException {
        // Make sure connection is set
        setConnectionField(mockConnection);
        
        // Setup connection to report as closed
        when(mockConnection.isClosed()).thenReturn(true);
        
        // Execute
        DatabaseUtil.closeConnection();
        
        // Verify close was not called
        verify(mockConnection, never()).close();
    }

    /**
     * Test exception handling in closeConnection
     */
    @Test
    public void testCloseConnectionWithException() throws SQLException {
        // Make sure connection is set
        setConnectionField(mockConnection);
        
        // Setup connection to be open but throw exception on close
        when(mockConnection.isClosed()).thenReturn(false);
        doThrow(new SQLException("Close error")).when(mockConnection).close();
        
        // Execute - should not throw exception
        DatabaseUtil.closeConnection();
        
        // Verify close was called despite exception
        verify(mockConnection).close();
    }
    
    /**
     * Helper method to set the connection field via reflection
     */
    private void setConnectionField(Connection connection) {
        try {
            java.lang.reflect.Field connectionField = DatabaseUtil.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(null, connection);
        } catch (Exception e) {
            fail("Failed to set connection field: " + e.getMessage());
        }
    }
} 