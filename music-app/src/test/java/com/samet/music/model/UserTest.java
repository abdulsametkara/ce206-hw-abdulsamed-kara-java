package com.samet.music.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class UserTest {
    
    private User user;
    private final int TEST_ID = 1;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_EMAIL = "test@example.com";
    private final LocalDateTime TEST_DATE = LocalDateTime.now();
    
    @Before
    public void setUp() {
        // Create a new user for each test
        user = new User(TEST_ID, TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_DATE);
    }
    
    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull(defaultUser);
    }
    
    @Test
    public void testConstructorWithoutId() {
        User newUser = new User(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        assertNotNull(newUser);
        assertEquals(TEST_USERNAME, newUser.getUsername());
        assertEquals(TEST_PASSWORD, newUser.getPassword());
        assertEquals(TEST_EMAIL, newUser.getEmail());
        assertNotNull(newUser.getCreatedAt());
    }
    
    @Test
    public void testFullConstructor() {
        assertNotNull(user);
        assertEquals(TEST_ID, user.getId());
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_PASSWORD, user.getPassword());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(TEST_DATE, user.getCreatedAt());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test setId and getId
        user.setId(2);
        assertEquals(2, user.getId());
        
        // Test setUsername and getUsername
        user.setUsername("newusername");
        assertEquals("newusername", user.getUsername());
        
        // Test setPassword and getPassword
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());
        
        // Test setEmail and getEmail
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
        
        // Test setCreatedAt and getCreatedAt
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        user.setCreatedAt(newDate);
        assertEquals(newDate, user.getCreatedAt());
    }
    
    @Test
    public void testToString() {
        String expected = "User{" +
                "id=" + TEST_ID +
                ", username='" + TEST_USERNAME + '\'' +
                ", email='" + TEST_EMAIL + '\'' +
                ", createdAt=" + TEST_DATE +
                '}';
        
        assertEquals(expected, user.toString());
    }
    
    @Test
    public void testToStringDoesNotIncludePassword() {
        // Ensure that the toString method doesn't include the sensitive password field
        String toStringResult = user.toString();
        assertFalse("toString should not contain the password", 
                toStringResult.contains(TEST_PASSWORD));
    }
} 