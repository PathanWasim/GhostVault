package com.ghostvault;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.ghostvault.integration.ApplicationIntegrator;
import com.ghostvault.ui.HelpSystem;

/**
 * Main GhostVault application entry point
 * Uses ApplicationIntegrator for comprehensive component coordination
 */
public class GhostVault extends Application {
    
    private ApplicationIntegrator applicationIntegrator;
    private HelpSystem helpSystem;
    
    public static void main(String[] args) {
        // Set system properties for better JavaFX performance
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.subpixeltext", "false");
        
        // Launch application
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize help system
            helpSystem = new HelpSystem();
            
            // Initialize application integrator
            applicationIntegrator = new ApplicationIntegrator();
            
            // Note: Global exception handler is set up in ApplicationIntegrator.initializeErrorHandling()
            
            // Set up stage close handler
            primaryStage.setOnCloseRequest(event -> {
                event.consume(); // Prevent default close
                handleApplicationExit();
            });
            
            // Initialize all components through integrator
            applicationIntegrator.initialize(primaryStage);
            
            System.out.println("🚀 GhostVault application started successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start GhostVault: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog and exit
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("GhostVault Startup Error");
                alert.setHeaderText("Failed to start GhostVault");
                alert.setContentText("Error: " + e.getMessage() + 
                    "\n\nPlease check the console for more details.");
                alert.showAndWait();
                Platform.exit();
            });
        }
    }
    
    /**
     * Handle application exit with proper cleanup
     */
    private void handleApplicationExit() {
        try {
            // Perform graceful shutdown through integrator
            if (applicationIntegrator != null) {
                applicationIntegrator.shutdown();
            }
            
            // Force exit to prevent hanging
            Platform.exit();
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("Error during application exit: " + e.getMessage());
            Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Show help system
     */
    public void showHelp(Stage parentStage) {
        if (helpSystem != null) {
            helpSystem.showHelp(parentStage);
        }
    }
    
    /**
     * Get application integrator (for testing or advanced usage)
     */
    public ApplicationIntegrator getApplicationIntegrator() {
        return applicationIntegrator;
    }
}