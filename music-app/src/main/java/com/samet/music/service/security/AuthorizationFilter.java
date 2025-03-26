package com.samet.music.service.security;


import java.util.Set;
import java.util.function.Predicate;

public class AuthorizationFilter {

    private SessionManager sessionManager;

    public AuthorizationFilter() {
        this.sessionManager = SessionManager.getInstance();
    }

    public boolean checkAccess(Set<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            // Eğer gerekli rol yoksa herkes erişebilir
            return true;
        }

        // Kullanıcı oturum açmamış veya rol bilgisi yoksa erişim engellenir
        if (sessionManager.getCurrentUser() == null ||
                sessionManager.getCurrentUser().getRoles() == null) {
            return false;
        }

        // Kullanıcının rollerinden herhangi biri gerekli rollerden birine eşitse erişim verilir
        return sessionManager.getCurrentUser().getRoles().stream()
                .anyMatch(userRole -> requiredRoles.contains(userRole));
    }

    // Belirli işlemler için predicate döndüren yardımcı metot
    public Predicate<Object> hasRole(String role) {
        return obj -> sessionManager.hasRole(role);
    }
}