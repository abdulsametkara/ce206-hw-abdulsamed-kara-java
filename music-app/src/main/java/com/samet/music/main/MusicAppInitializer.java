package com.samet.music.main;

import com.samet.music.dao.DAOFactory;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application initializer class
 * Responsible for starting up application components
 */
public class MusicAppInitializer {
    private static final Logger logger = LoggerFactory.getLogger(MusicAppInitializer.class);

    private static volatile MusicAppInitializer instance;

    /**
     * Private constructor to prevent direct instantiation
     */
    private MusicAppInitializer() {
        // Private constructor
    }

    /**
     * Returns the singleton instance
     */
    public static MusicAppInitializer getInstance() {
        if (instance == null) {
            synchronized (MusicAppInitializer.class) {
                if (instance == null) {
                    instance = new MusicAppInitializer();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the application
     */
    public void initialize() {
        logger.info("Initializing music application...");

        try {
            // Initialize database manager
            DatabaseManager.getInstance();
            logger.info("Database manager initialized successfully");

            // Initialize DAO factory
            DAOFactory.getInstance();
            logger.info("DAO factory initialized successfully");

            // Initialize service layer
            MusicCollectionService.getInstance();
            logger.info("Service layer initialized successfully");

            logger.info("Application initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize application: {}", e.getMessage(), e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    /**
     * Resets the database (for testing/development)
     * @param resetDatabase flag to reset database
     */
    public void resetDatabase(boolean resetDatabase) {
        if (resetDatabase) {
            try {
                logger.info("Resetting database...");
                DatabaseManager.getInstance().setShouldResetDatabase(true);
                DatabaseManager.getInstance().initializeDatabase();
                MusicCollectionService.getInstance().reinitializeDatabase();
                logger.info("Database reset successfully");
            } catch (Exception e) {
                logger.error("Failed to reset database: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Shuts down the application
     */
    public void shutdown() {
        logger.info("Shutting down application...");

        try {
            // Close database connections
            DatabaseManager.getInstance().closeAllConnections();
            logger.info("Database connections closed");

            logger.info("Application shutdown complete");
        } catch (Exception e) {
            logger.error("Error during application shutdown: {}", e.getMessage(), e);
        }
    }
}