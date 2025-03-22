package com.samet.music.exception;

public class DatabaseOperationException extends MusicLibraryException {
    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
