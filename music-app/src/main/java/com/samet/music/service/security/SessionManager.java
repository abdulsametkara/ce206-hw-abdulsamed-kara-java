package com.samet.music.service.security;


import com.samet.music.model.security.User;
import com.samet.music.service.security.KeycloakService;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String token;
    private KeycloakService keycloakService;

    private SessionManager() {
        keycloakService = new KeycloakService();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        // Basit kullanıcı doğrulama kullanın
        if ((username.equals("admin") && password.equals("admin")) ||
                (username.equals("user") && password.equals("password"))) {

            // Test kullanıcısı oluştur
            User testUser = new User();
            testUser.setId("test-user-id-" + System.currentTimeMillis());
            testUser.setUsername(username);
            testUser.setEmail(username + "@example.com");
            testUser.setFirstName(username.substring(0, 1).toUpperCase() + username.substring(1));
            testUser.setLastName("User");

            Set<String> roles = new HashSet<>();
            if (username.equals("admin")) {
                roles.add("admin");
            } else {
                roles.add("user");
            }
            testUser.setRoles(roles);

            currentUser = testUser;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
        token = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getToken() {
        return token;
    }

    public boolean hasRole(String role) {
        if (currentUser == null || currentUser.getRoles() == null) {
            return false;
        }
        return currentUser.getRoles().contains(role);
    }
}
