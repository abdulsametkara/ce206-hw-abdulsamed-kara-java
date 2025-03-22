package com.samet.music.exception;

/**
 * Exception thrown when attempting to create an entity with invalid data
 */
public class InvalidEntityDataException extends MusicLibraryException {
    public InvalidEntityDataException(String message) {
        super(message);
    }
}