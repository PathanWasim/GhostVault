package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog window for enhanced text viewer component
 * Provides a dedicated window for viewing text and code files with syntax highlighting
 */
public class EnhancedTextViewerDialog extends Stage {
    
    private EnhancedTextViewerComponent textViewer;
    private VaultFile vaultFile;
    
    public EnhancedTextViewerDialog(VaultFile file, byte[] fileData) {
        this.vaultFile = file;
        
        // Initialize text viewer component
        textViewer = new EnhancedTextViewerComponent();
        textViewer.initialize(file);
        
        // Setup dialog properties
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Text Viewer - " + (file != null ? file.getOriginalName() : "Unknown"));
        setResizable(true);
        
        // Create and set scene using the component's createScene method
        Scene scene = textViewer.createScene();
        if (scene != null) {
            setScene(scene);
        } else {
            // Fallback scene if createScene fails
            Scene fallbackScene = new Scene(new javafx.scene.control.Label("Failed to create text viewer"), 400, 300);
            setScene(fallbackScene);
        }
        
        // Set minimum size
        setMinWidth(600);
        setMinHeight(400);
        
        // Center on screen
        centerOnScreen();
        
        // Load content
        try {
            textViewer.loadContent(fileData);
        } catch (Exception e) {
            System.err.println("Error loading text content: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Setup close behavior
        setOnCloseRequest(event -> {
            try {
                textViewer.cleanup();
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get the text viewer component
     */
    public EnhancedTextViewerComponent getTextViewer() {
        return textViewer;
    }
    
    /**
     * Get the vault file being viewed
     */
    public VaultFile getVaultFile() {
        return vaultFile;
    }
}