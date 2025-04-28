package com.samet.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Main Spring Boot Application Class
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Music Library API",
        version = "1.0",
        description = "RESTful API for Music Library Application"
    )
)
public class MusicAppSpring {
    private static final Logger logger = LoggerFactory.getLogger(MusicAppSpring.class);

    public static void main(String[] args) {
        logger.info("Starting Music Library Spring Boot Application...");
        SpringApplication.run(MusicAppSpring.class, args);
        logger.info("Music Library Spring Boot Application is running!");
    }
} 