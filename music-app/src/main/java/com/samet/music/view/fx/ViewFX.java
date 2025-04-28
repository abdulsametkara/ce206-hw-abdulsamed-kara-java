package com.samet.music.view.fx;

import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Base interface for all JavaFX views in the application
 */
public interface ViewFX {
    
    /**
     * Get the root node of this view
     * 
     * @return The root node
     */
    Parent getRoot();
    
    /**
     * Get the primary stage
     * 
     * @return The primary stage
     */
    Stage getStage();
} 