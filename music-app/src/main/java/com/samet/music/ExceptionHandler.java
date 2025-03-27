package com.samet.music;

import com.samet.music.exception.EntityNotFoundException;
import com.samet.music.exception.FeatureNotImplementedException;
import com.samet.music.exception.InvalidEntityDataException;
import com.samet.music.exception.MusicLibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler utility class
 */
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Handles an exception and returns a user-friendly message
     * @param exception The exception to handle
     * @return A user-friendly error message
     */
    public static String handleException(Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            logger.warn("Entity not found: {}", exception.getMessage());
            return exception.getMessage();
        } else if (exception instanceof InvalidEntityDataException) {
            logger.warn("Invalid data: {}", exception.getMessage());
            return "Invalid data: " + exception.getMessage();
        } else if (exception instanceof FeatureNotImplementedException) {
            logger.info("Feature not implemented: {}", exception.getMessage());
            return exception.getMessage();
        } else if (exception instanceof MusicLibraryException) {
            logger.error("Application error: {}", exception.getMessage());
            return "Application error: " + exception.getMessage();
        } else {
            logger.error("Unexpected error", exception);
            return "An unexpected error occurred. Please try again.";
        }
    }
}