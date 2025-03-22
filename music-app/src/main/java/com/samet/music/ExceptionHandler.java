package com.samet.music;

import com.samet.music.exception.EntityNotFoundException;
import com.samet.music.exception.FeatureNotImplementedException;
import com.samet.music.exception.InvalidEntityDataException;
import com.samet.music.exception.MusicLibraryException;

/**
 * Exception handler utility class
 */
public class ExceptionHandler {
    /**
     * Handles an exception and returns a user-friendly message
     * @param exception The exception to handle
     * @return A user-friendly error message
     */
    public static String handleException(Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return exception.getMessage();
        } else if (exception instanceof InvalidEntityDataException) {
            return "Invalid data: " + exception.getMessage();
        } else if (exception instanceof FeatureNotImplementedException) {
            return exception.getMessage();
        } else if (exception instanceof MusicLibraryException) {
            return "Application error: " + exception.getMessage();
        } else {
            // For unexpected exceptions, hide implementation details from the user
            return "An unexpected error occurred. Please try again.";
        }
    }
}