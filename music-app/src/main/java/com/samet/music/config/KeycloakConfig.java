package com.samet.music.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.util.Map;

public class KeycloakConfig implements KeycloakConfigResolver {

    private KeycloakDeployment keycloakDeployment;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {
        if (keycloakDeployment != null) {
            return keycloakDeployment;
        }

        // Çevre değişkenlerinden veya sistem özelliklerinden yapılandırma bilgilerini al
        String keycloakUrl = System.getenv("KEYCLOAK_URL");
        String realm = System.getenv("KEYCLOAK_REALM");
        String clientId = System.getenv("KEYCLOAK_CLIENT_ID");
        String clientSecret = System.getenv("KEYCLOAK_CLIENT_SECRET");

        // AdapterConfig nesnesini oluştur
        AdapterConfig config = new AdapterConfig();
        config.setRealm(realm);
        config.setResource(clientId);
        config.setAuthServerUrl(keycloakUrl + "/auth");
        config.setCredentials(Map.of("secret", clientSecret));

        keycloakDeployment = KeycloakDeploymentBuilder.build(config);
        return keycloakDeployment;
    }
}