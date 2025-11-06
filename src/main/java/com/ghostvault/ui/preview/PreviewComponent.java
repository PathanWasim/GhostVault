package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Abstract base class for all preview components in GhostVault
 * Provides common functionality for file preview windows
 */
public abstract class PreviewComponent {
    
    protected Stage previewStage;
    protected VaultFile vaultFile;
    protected boolean isSecureMode = true;
    protected Runnable cleanupCallback;
    
    /**
     * Initialize the preview component with a vault file
     */
    public void initialize(VaultFile vaultFile) {
        this.vaultFile = vaultFile;
        setupPreviewStage();
    }
    
    /**
     * Load and display the file content
     * @param fileData The decrypted file data
     */
    public abstract void loadContent(byte[] fileData);
    
    /**
     * Show the preview window
     */
    public void show() {
        if (previewStage != null) {
            previewStage.show();
            previewStage.toFront();
        }
    }
    
    /**
     * Hide the preview window
     */
    public void hide() {
        if (previewStage != null) {
            previewStage.hide();
        }
    }
    
    /**
     * Close the preview and cleanup resources
     */
    public void close() {
        cleanup();
        if (previewStage != null) {
            previewStage.close();
            previewStage = null;
        }
    }
    
    /**
     * Set cleanup callback for secure memory management
     */
    public void setCleanupCallback(Runnable callback) {
        this.cleanupCallback = callback;
    }
    
    /**
     * Get the preview window title
     */
    protected String getWindowTitle() {
        if (vaultFile != null) {
            return "Preview - " + vaultFile.getOriginalName();
        }
        return "File Preview";
    }
    
    /**
     * Setup the preview stage with common properties
     */
    protected void setupPreviewStage() {
        previewStage = new Stage();
        previewStage.setTitle(getWindowTitle());
        previewStage.setOnCloseRequest(event -> {
            event.consume();
            close();
        });
        
        // Apply GhostVault styling
        Scene scene = createScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
            previewStage.setScene(scene);
        }
    }
    
    /**
     * Create the scene for this preview component
     * Must be implemented by subclasses
     */
    protected abstract Scene createScene();
    
    /**
     * Cleanup resources and sensitive data
     * Should be overridden by subclasses for specific cleanup
     */
    protected void cleanup() {
        if (cleanupCallback != null) {
            cleanupCallback.run();
        }
    }
    
    /**
     * Show error message to user
     */
    protected void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Preview Error");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        
        // Apply dark theme if available
        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/ghostvault-dark.css").toExternalForm());
        } catch (Exception e) {
            // Ignore styling errors
        }
        
        alert.showAndWait();
    }
    
    /**
     * Check if the component supports the given file type
     */
    public abstract boolean supportsFileType(String fileExtension);
    
    /**
     * Get the display name for this preview component
     */
    public abstract String getComponentName();
    
    /**
     * Get the supported file extensions
     */
    public abstract String[] getSupportedExtensions();
    
    /**
     * Set secure mode (affects how sensitive data is handled)
     */
    public void setSecureMode(boolean secureMode) {
        this.isSecureMode = secureMode;
    }
    
    /**
     * Check if component is in secure mode
     */
    public boolean isSecureMode() {
        return isSecureMode;
    }
    
    /**
     * Get the vault file being previewed
     */
    public VaultFile getVaultFile() {
        return vaultFile;
    }
    
    /**
     * Get the preview stage
     */
    public Stage getPreviewStage() {
        return previewStage;
    }
}