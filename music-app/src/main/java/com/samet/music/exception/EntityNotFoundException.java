package com.samet.music.exception;

/**
 * Exception thrown when an entity is not found
 */
public class EntityNotFoundException extends MusicLibraryException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " with ID " + id + " not found");
    }
}