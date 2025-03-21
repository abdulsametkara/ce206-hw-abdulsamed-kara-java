package com.samet.music;

/**
 * Exception thrown when attempting to access a feature that hasn't been implemented yet
 */
public class FeatureNotImplementedException extends MusicLibraryException {
    public FeatureNotImplementedException(String featureName) {
        super("The " + featureName + " feature has not been implemented yet");
    }
}