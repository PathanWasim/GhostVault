package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog window for media viewer component
 * Provides a dedicated window for playing audio and video files
 */
public class MediaViewerDialog extends Stage {
    
    private MediaViewerComponent mediaViewer;
    private VaultFile vaultFile;
    
    public MediaViewerDialog(VaultFile file, byte[] fileData) {
        this.vaultFile = file;
        
        // Initialize media viewer component
        mediaViewer = new MediaViewerComponent();
        mediaViewer.initialize(file);
        
        // Setup dialog properties
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Media Player - " + (file != null ? file.getOriginalName() : "Unknown"));
        setResizable(true);
        
        // Create and set scene using the component's createScene method
        Scene scene = mediaViewer.createScene();
        if (scene != null) {
            setScene(scene);
        } else {
            // Fallback scene if createScene fails
            Scene fallbackScene = new Scene(new javafx.scene.control.Label("Failed to create media viewer"), 400, 300);
            setScene(fallbackScene);
        }
        
        // Set minimum size
        setMinWidth(600);
        setMinHeight(400);
        
        // Center on screen
        centerOnScreen();
        
        // Load content
        try {
            mediaViewer.loadContent(fileData);
        } catch (Exception e) {
            System.err.println("Error loading media content: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Setup close behavior
        setOnCloseRequest(event -> {
            try {
                mediaViewer.cleanup();
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get the media viewer component
     */
    public MediaViewerComponent getMediaViewer() {
        return mediaViewer;
    }
    
    /**
     * Get the vault file being viewed
     */
    public VaultFile getVaultFile() {
        return vaultFile;
    }
}