package com.ghostvault.ui.controllers;

import com.ghostvault.core.DecoyManager;
import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Base controller for different vault modes (Master, Panic, Decoy)
 */
public abstract class ModeController {
    
    protected Stage primaryStage;
    protected Scene scene;
    protected VBox rootContainer;
    protected VaultMode currentMode;
    
    // Callbacks
    protected Consumer<VaultMode> onModeChanged;
    protected Runnable onExit;
    
    // State
    protected boolean initialized = false;
    protected boolean secureMode = false;
    
    public ModeController(Stage primaryStage, VaultMode mode) {
        this.primaryStage = primaryStage;
        this.currentMode = mode;
        this.rootContainer = new VBox();
        this.rootContainer.getStyleClass().add("mode-controller");
        this.rootContainer.getStyleClass().add("mode-" + mode.name().toLowerCase());
    }
    
    /**
     * Initialize the controller
     */
    public abstract void initialize();
    
    /**
     * Activate this mode
     */
    public abstract void activate();
    
    /**
     * Deactivate this mode
     */
    public abstract void deactivate();
    
    /**
     * Handle emergency shutdown
     */
    public abstract void emergencyShutdown();
    
    /**
     * Get the mode type
     */
    public VaultMode getMode() {
        return currentMode;
    }
    
    /**
     * Check if controller is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Set secure mode (affects UI behavior)
     */
    public void setSecureMode(boolean secure) {
        this.secureMode = secure;
        updateSecurityIndicators();
    }
    
    /**
     * Update security indicators in UI
     */
    protected abstract void updateSecurityIndicators();
    
    /**
     * Handle mode switching
     */
    protected void switchMode(VaultMode newMode) {
        try {
            if (onModeChanged != null) {
                onModeChanged.accept(newMode);
            }
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to switch vault mode", e);
        }
    }
    
    /**
     * Handle application exit
     */
    protected void exitApplication() {
        try {
            // Perform cleanup
            cleanup();
            
            if (onExit != null) {
                onExit.run();
            } else {
                Platform.exit();
            }
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Error during application exit", e);
            Platform.exit(); // Force exit on error
        }
    }
    
    /**
     * Cleanup resources
     */
    protected abstract void cleanup();
    
    /**
     * Show the controller's scene
     */
    public void show() {
        if (scene == null) {
            scene = new Scene(rootContainer, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
        }
        
        primaryStage.setScene(scene);
        primaryStage.setTitle(getWindowTitle());
        
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }
    
    /**
     * Get window title for this mode
     */
    protected abstract String getWindowTitle();
    
    /**
     * Handle authentication success
     */
    public abstract void onAuthenticationSuccess(String password);
    
    /**
     * Handle authentication failure
     */
    public abstract void onAuthenticationFailure();
    
    // Getters and Setters
    
    public void setOnModeChanged(Consumer<VaultMode> callback) {
        this.onModeChanged = callback;
    }
    
    public void setOnExit(Runnable callback) {
        this.onExit = callback;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public VBox getRootContainer() {
        return rootContainer;
    }
    
    /**
     * Vault operation modes
     */
    public enum VaultMode {
        MASTER("Master Mode", "Full access to all vault features"),
        PANIC("Panic Mode", "Emergency data destruction mode"),
        DECOY("Decoy Mode", "Fake vault with dummy data");
        
        private final String displayName;
        private final String description;
        
        VaultMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}