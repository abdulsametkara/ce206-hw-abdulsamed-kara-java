package com.samet.music.service.security;

import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.TokenUtil;


import com.samet.music.model.security.User;

import javax.ws.rs.core.Response;
import java.util.*;

public class KeycloakService {

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String adminUsername;
    private final String adminPassword;

    private Keycloak keycloakAdminClient;

    public KeycloakService() {
        // Çevre değişkenlerinden değerleri al, yoksa varsayılan değerleri kullan
        this.serverUrl = System.getenv("KEYCLOAK_URL") != null ? System.getenv("KEYCLOAK_URL") : "http://localhost:8080";
        this.realm = System.getenv("KEYCLOAK_REALM") != null ? System.getenv("KEYCLOAK_REALM") : "music-realm";
        this.clientId = System.getenv("KEYCLOAK_CLIENT_ID") != null ? System.getenv("KEYCLOAK_CLIENT_ID") : "music-app";
        this.clientSecret = System.getenv("KEYCLOAK_CLIENT_SECRET") != null ? System.getenv("KEYCLOAK_CLIENT_SECRET") : "client-secret";
        this.adminUsername = "admin";
        this.adminPassword = "admin";

        initKeycloakAdminClient();
    }

    private void initKeycloakAdminClient() {
        try {
            keycloakAdminClient = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .username(adminUsername)
                    .password(adminPassword)
                    .build();

            System.out.println("Keycloak admin client initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing Keycloak admin client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String authenticate(String username, String password) {
        try {
            System.out.println("Authenticating user: " + username);
            System.out.println("Server URL: " + serverUrl);
            System.out.println("Realm: " + realm);
            System.out.println("Client ID: " + clientId);

            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .build();

            // Token alınıyor
            String token = keycloak.tokenManager().getAccessTokenString();
            System.out.println("Token received: " + (token != null ? "Yes" : "No"));
            return token;
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User getUserInfo(String token) {
        try {
            System.out.println("Getting user info for token: " + token.substring(0, Math.min(10, token.length())) + "...");

            // Token'ı decode et
            AccessToken accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
            String userId = accessToken.getSubject();

            System.out.println("User ID: " + userId);

            // Keycloak'tan kullanıcı bilgilerini al
            UserRepresentation userRep = keycloakAdminClient.realm(realm).users().get(userId).toRepresentation();

            // Kullanıcı rollerini al
            List<RoleRepresentation> roles = keycloakAdminClient.realm(realm).users().get(userId).roles().realmLevel().listAll();

            Set<String> userRoles = new HashSet<>();
            for (RoleRepresentation role : roles) {
                userRoles.add(role.getName());
            }

            User user = new User();
            user.setId(userId);
            user.setUsername(userRep.getUsername());
            user.setEmail(userRep.getEmail());
            user.setFirstName(userRep.getFirstName());
            user.setLastName(userRep.getLastName());
            user.setRoles(userRoles);

            System.out.println("User info retrieved: " + user.toString());

            return user;
        } catch (Exception e) {
            System.err.println("Error getting user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String username, String email, String password, String firstName, String lastName) {
        try {
            System.out.println("Registering user: " + username);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setCredentials(Arrays.asList(credential));

            RealmResource realmResource = keycloakAdminClient.realm(realm);
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);
            int status = response.getStatus();
            response.close();

            if (status == 201) {
                // Başarılı
                System.out.println("User registered successfully");

                // Kullanıcıya user rolünü ata
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                RoleRepresentation userRole = realmResource.roles().get("user").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(userRole));

                return true;
            } else {
                System.out.println("Failed to register user, status: " + status);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}