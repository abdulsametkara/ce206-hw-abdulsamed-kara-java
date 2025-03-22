package com.samet.music;

import com.samet.music.exception.DatabaseOperationException;
import com.samet.music.exception.ResourceNotFoundException;
import com.samet.music.exception.ValidationException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    public static void logError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logWarning(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    public static void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static String getUserFriendlyErrorMessage(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            return "The information entered is invalid: " + throwable.getMessage();
        } else if (throwable instanceof ResourceNotFoundException) {
            return "The requested resource was not found: " + throwable.getMessage();
        } else if (throwable instanceof DatabaseOperationException) {
            return "An error occurred in the database operation: " + throwable.getMessage();
        } else {
            return "An unexpected error occurred. Please try again later.";
        }
    }

    public static void handleException(Throwable throwable) {
        logError("An error occurred", throwable);
    }
}