package com.samet.music;

/**
 * Base exception class for Music Library application
 * Implements proper exception handling
 */
public class MusicLibraryException extends Exception {
    public MusicLibraryException(String message) {
        super(message);
    }

    public MusicLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}