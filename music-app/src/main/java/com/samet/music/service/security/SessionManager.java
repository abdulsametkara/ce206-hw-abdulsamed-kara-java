package com.samet.music.service.security;

import com.samet.music.model.security.User;
import com.samet.music.monitoring.MetricsCollector;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    private User currentUser;
    private String token;
    private KeycloakService keycloakService;
    private final Counter loginCounter;
    private final Counter loginFailureCounter;

    private SessionManager() {
        keycloakService = new KeycloakService();

        // Login counter'ları oluştur
        loginCounter = Counter.build()
                .name("music_app_logins_total")
                .help("Toplam başarılı giriş sayısı")
                .register();

        loginFailureCounter = Counter.build()
                .name("music_app_login_failures_total")
                .help("Toplam başarısız giriş girişimi sayısı")
                .register();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        try {
            logger.info("Login attempt for user: {}", username);

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
                    logger.info("User {} logged in with admin role", username);
                } else {
                    roles.add("user");
                    logger.info("User {} logged in with user role", username);
                }
                testUser.setRoles(roles);

                currentUser = testUser;

                // Login counter'ı artır
                loginCounter.inc();

                // Aktif kullanıcı sayısını güncelle
                MetricsCollector.getInstance().setActiveUsers(1);

                return true;
            }

            logger.warn("Login failed for user: {}", username);

            // Login failure counter'ı artır
            loginFailureCounter.inc();

            return false;
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            loginFailureCounter.inc();
            return false;
        }
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("User {} logged out", currentUser.getUsername());
        }

        currentUser = null;
        token = null;

        // Aktif kullanıcı sayısını güncelle
        MetricsCollector.getInstance().setActiveUsers(0);
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