package com.samet.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(-1) // En yüksek öncelik
public class ActuatorSecurityConfig {
    
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .antMatcher("/api/actuator/**")
            .authorizeRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .cors().disable() 
            .anonymous();
        
        try {
            // Keycloak özelinde ayarlar
            http.oauth2ResourceServer(AbstractHttpConfigurer::disable);
        } catch (Exception e) {
            // ignore
        }
        
        return http.build();
    }
} 