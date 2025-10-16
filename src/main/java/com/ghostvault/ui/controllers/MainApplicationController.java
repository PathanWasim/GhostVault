package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application controller that manages mode switching and authentication
 */
public class MainApplicationController {
    
    private Stage primaryStage;
    
    // Controllers
    private AuthenticationController authController;
    private Map<ModeController.VaultMode, ModeController> modeControllers;
    private ModeController currentModeController;
    
    // State
    private ModeController.VaultMode currentMode;
    private boolean applicationInitialized = false;
    
    public MainApplicationController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.modeControllers = new HashMap<>();
        
        initializeApplication();
    }
    
    /**
     * Initialize the application
     */
    private void initializeApplication() {
        try {
            // Initialize authentication controller
            authController = new AuthenticationController(primaryStage);
            authController.setOnAuthenticationSuccess(this::switchToMode);
            authController.setOnAuthenticationCancelled(this::exitApplication);
            
            // Initialize mode controllers
            initializeModeControllers();
            
            // Setup error handling
            ErrorHandlingSystem.getInstance().setOnErrorLogged(this::handleApplicationError);
            
            applicationInitialized = true;
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to initialize application", e);
            Platform.exit();
        }
    }
    
    /**
     * Initialize all mode controllers
     */
    private void initializeModeControllers() {
        // Master Mode Controller
        MasterModeController masterController = new MasterModeController(primaryStage);
        masterController.setOnModeChanged(this::handleModeChangeRequest);
        masterController.setOnExit(this::exitApplication);
        modeControllers.put(ModeController.VaultMode.MASTER, masterController);
        
        // Panic Mode Controller
        PanicModeController panicController = new PanicModeController(primaryStage);
        panicController.setOnModeChanged(this::handleModeChangeRequest);
        panicController.setOnExit(this::exitApplication);
        modeControllers.put(ModeController.VaultMode.PANIC, panicController);
        
        // Decoy Mode Controller
        DecoyModeController decoyController = new DecoyModeController(primaryStage);
        decoyController.setOnModeChanged(this::handleModeChangeRequest);
        decoyController.setOnExit(this::exitApplication);
        modeControllers.put(ModeController.VaultMode.DECOY, decoyController);
    }
    
    /**
     * Start the application
     */
    public void start() {
        if (!applicationInitialized) {
            ErrorHandlingSystem.showErrorDialog("Initialization Error", 
                "Application not properly initialized");
            return;
        }
        
        try {
            // Show authentication screen
            showAuthentication();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to start application", e);
        }
    }
    
    /**
     * Show authentication screen
     */
    private void showAuthentication() {
        // Deactivate current mode if any
        if (currentModeController != null) {
            currentModeController.deactivate();
            currentModeController = null;
        }
        
        currentMode = null;
        
        // Reset and show authentication
        authController.reset();
        authController.show();
    }
    
    /**
     * Switch to specified mode after successful authentication
     */
    private void switchToMode(ModeController.VaultMode mode) {
        try {
            // Deactivate current mode
            if (currentModeController != null) {
                currentModeController.deactivate();
            }
            
            // Get the target mode controller
            ModeController targetController = modeControllers.get(mode);
            if (targetController == null) {
                throw new IllegalStateException("No controller found for mode: " + mode);
            }
            
            // Special handling for panic mode
            if (mode == ModeController.VaultMode.PANIC) {
                handlePanicModeActivation(targetController);
                return;
            }
            
            // Activate new mode
            currentMode = mode;
            currentModeController = targetController;
            
            // Initialize if needed
            if (!targetController.isInitialized()) {
                targetController.initialize();
            }
            
            // Notify controller of successful authentication
            targetController.onAuthenticationSuccess(authController.getScene() != null ? "authenticated" : "");
            
            // Activate the mode
            targetController.activate();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to switch to " + mode.getDisplayName(), e);
            
            // Fall back to authentication
            showAuthentication();
        }
    }
    
    /**
     * Handle panic mode activation with special security measures
     */
    private void handlePanicModeActivation(ModeController panicController) {
        try {
            // Immediately deactivate all other modes
            for (ModeController controller : modeControllers.values()) {
                if (controller != panicController) {
                    controller.deactivate();
                }
            }
            
            // Clear authentication controller
            authController.reset();
            
            // Activate panic mode
            currentMode = ModeController.VaultMode.PANIC;
            currentModeController = panicController;
            
            if (!panicController.isInitialized()) {
                panicController.initialize();
            }
            
            panicController.onAuthenticationSuccess("panic");
            panicController.activate();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to activate panic mode", e);
            
            // In case of panic mode failure, force emergency shutdown
            emergencyShutdown();
        }
    }
    
    /**
     * Handle mode change requests from controllers
     */
    private void handleModeChangeRequest(ModeController.VaultMode requestedMode) {
        if (requestedMode == currentMode) {
            return; // Already in requested mode
        }
        
        try {
            // Special handling for mode change requests
            switch (requestedMode) {
                case MASTER:
                    // Return to authentication for re-authentication
                    showAuthentication();
                    break;
                    
                case PANIC:
                    // Immediate switch to panic mode
                    switchToMode(ModeController.VaultMode.PANIC);
                    break;
                    
                case DECOY:
                    // Switch to decoy mode
                    switchToMode(ModeController.VaultMode.DECOY);
                    break;
                    
                default:
                    showAuthentication();
            }
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to handle mode change request", e);
            showAuthentication();
        }
    }
    
    /**
     * Handle application errors
     */
    private void handleApplicationError(ErrorHandlingSystem.ErrorRecord error) {
        // Log critical errors
        if (error.getSeverity() == ErrorHandlingSystem.ErrorSeverity.CRITICAL) {
            // For critical errors, consider emergency shutdown
            if (currentMode == ModeController.VaultMode.MASTER) {
                // In master mode, critical errors might warrant panic mode
                Platform.runLater(() -> {
                    boolean shouldPanic = showCriticalErrorDialog(error);
                    if (shouldPanic) {
                        switchToMode(ModeController.VaultMode.PANIC);
                    }
                });
            }
        }
    }
    
    /**
     * Show critical error dialog with panic option
     */
    private boolean showCriticalErrorDialog(ErrorHandlingSystem.ErrorRecord error) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Critical System Error");
        alert.setHeaderText("A critical error has occurred");
        alert.setContentText(
            "Error: " + error.getMessage() + "\\n\\n" +
            "This may indicate a security breach or system compromise.\\n" +
            "Do you want to activate emergency data destruction?"
        );
        
        javafx.scene.control.ButtonType panicButton = new javafx.scene.control.ButtonType(
            "Emergency Destruction", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType continueButton = new javafx.scene.control.ButtonType(
            "Continue", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(panicButton, continueButton);
        
        return alert.showAndWait()
            .map(response -> response == panicButton)
            .orElse(false);
    }
    
    /**
     * Emergency shutdown of the application
     */
    private void emergencyShutdown() {
        try {
            // Trigger emergency shutdown on all controllers
            for (ModeController controller : modeControllers.values()) {
                try {
                    controller.emergencyShutdown();
                } catch (Exception e) {
                    // Continue with other controllers even if one fails
                }
            }
            
            // Clear authentication
            if (authController != null) {
                authController.reset();
            }
            
            // Force garbage collection
            System.gc();
            
            // Exit immediately
            Platform.exit();
            
        } catch (Exception e) {
            // Force exit even if emergency shutdown fails
            System.exit(1);
        }
    }
    
    /**
     * Normal application exit
     */
    private void exitApplication() {
        try {
            // Deactivate current mode
            if (currentModeController != null) {
                currentModeController.deactivate();
            }
            
            // Cleanup all controllers
            for (ModeController controller : modeControllers.values()) {
                try {
                    controller.deactivate();
                } catch (Exception e) {
                    // Continue cleanup even if one fails
                }
            }
            
            // Clear error history
            ErrorHandlingSystem.getInstance().clearErrorHistory();
            
            Platform.exit();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Error during application exit", e);
            Platform.exit();
        }
    }
    
    /**
     * Get current mode
     */
    public ModeController.VaultMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Get current mode controller
     */
    public ModeController getCurrentModeController() {
        return currentModeController;
    }
    
    /**
     * Check if application is initialized
     */
    public boolean isInitialized() {
        return applicationInitialized;
    }
    
    /**
     * Force mode switch (for testing or emergency)
     */
    public void forceModeSwitch(ModeController.VaultMode mode) {
        switchToMode(mode);
    }
    
    /**
     * Get authentication controller
     */
    public AuthenticationController getAuthenticationController() {
        return authController;
    }
    
    /**
     * Get mode controller for specific mode
     */
    public ModeController getModeController(ModeController.VaultMode mode) {
        return modeControllers.get(mode);
    }
}