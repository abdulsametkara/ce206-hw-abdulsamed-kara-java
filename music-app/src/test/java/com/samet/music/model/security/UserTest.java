package com.samet.music.model.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private String testId;
    private String testUsername;
    private String testEmail;
    private String testFirstName;
    private String testLastName;
    private Set<String> testRoles;

    @BeforeEach
    void setUp() {
        testId = "user123";
        testUsername = "testuser";
        testEmail = "test@example.com";
        testFirstName = "Test";
        testLastName = "User";
        testRoles = new HashSet<>();
        testRoles.add("USER");
        testRoles.add("ADMIN");

        user = new User(testId, testUsername, testEmail, testFirstName, testLastName, testRoles);
    }

    @Test
    void testEmptyConstructor() {
        // Test boş constructor
        User emptyUser = new User();
        
        // Tüm alanların null olduğunu kontrol et
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getFirstName());
        assertNull(emptyUser.getLastName());
        assertNull(emptyUser.getRoles());
    }

    @Test
    void testParameterizedConstructor() {
        // Parametreli constructor'ın değerleri doğru atadığını kontrol et
        assertEquals(testId, user.getId());
        assertEquals(testUsername, user.getUsername());
        assertEquals(testEmail, user.getEmail());
        assertEquals(testFirstName, user.getFirstName());
        assertEquals(testLastName, user.getLastName());
        assertEquals(testRoles, user.getRoles());
    }

    @Test
    void testGetSetId() {
        // Id getter/setter testi
        String newId = "newId456";
        user.setId(newId);
        assertEquals(newId, user.getId());
    }

    @Test
    void testGetSetUsername() {
        // Username getter/setter testi
        String newUsername = "newUsername";
        user.setUsername(newUsername);
        assertEquals(newUsername, user.getUsername());
    }

    @Test
    void testGetSetEmail() {
        // Email getter/setter testi
        String newEmail = "new.email@example.com";
        user.setEmail(newEmail);
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    void testGetSetFirstName() {
        // FirstName getter/setter testi
        String newFirstName = "NewFirstName";
        user.setFirstName(newFirstName);
        assertEquals(newFirstName, user.getFirstName());
    }

    @Test
    void testGetSetLastName() {
        // LastName getter/setter testi
        String newLastName = "NewLastName";
        user.setLastName(newLastName);
        assertEquals(newLastName, user.getLastName());
    }

    @Test
    void testGetSetRoles() {
        // Roles getter/setter testi
        Set<String> newRoles = new HashSet<>();
        newRoles.add("MODERATOR");
        newRoles.add("GUEST");
        
        user.setRoles(newRoles);
        assertEquals(newRoles, user.getRoles());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("MODERATOR"));
        assertTrue(user.getRoles().contains("GUEST"));
    }

    @Test
    void testToString() {
        // toString metodu testi
        String expectedToString = "User{" +
                "id='" + testId + '\'' +
                ", username='" + testUsername + '\'' +
                ", email='" + testEmail + '\'' +
                ", firstName='" + testFirstName + '\'' +
                ", lastName='" + testLastName + '\'' +
                ", roles=" + testRoles +
                '}';
        
        assertEquals(expectedToString, user.toString());
    }

    @Test
    void testRolesAddRemove() {
        // Roller koleksiyonunun doğru çalıştığını kontrol et
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_1");
        
        User testUser = new User();
        testUser.setRoles(roles);
        
        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains("ROLE_1"));
        
        // Rol ekleme
        testUser.getRoles().add("ROLE_2");
        assertEquals(2, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains("ROLE_2"));
        
        // Rol çıkarma
        testUser.getRoles().remove("ROLE_1");
        assertEquals(1, testUser.getRoles().size());
        assertFalse(testUser.getRoles().contains("ROLE_1"));
        assertTrue(testUser.getRoles().contains("ROLE_2"));
    }

    @Test
    void testNullValues() {
        // Null değerlerle user oluşturma
        User nullUser = new User(null, null, null, null, null, null);
        
        assertNull(nullUser.getId());
        assertNull(nullUser.getUsername());
        assertNull(nullUser.getEmail());
        assertNull(nullUser.getFirstName());
        assertNull(nullUser.getLastName());
        assertNull(nullUser.getRoles());
        
        // Null değerleri atama
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setRoles(null);
        
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getRoles());
    }

    @Test
    void testSerializableInterface() {
        // Serializable interface'ini implemente ettiğini kontrol et
        assertTrue(user instanceof java.io.Serializable);
    }
} 