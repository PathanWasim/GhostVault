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
            
            // Set up global exception handler
            Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
                System.err.println("Uncaught exception in " + thread.getName() + ": " + exception.getMessage());
                exception.printStackTrace();
                
                Platform.runLater(() -> {
                    if (applicationIntegrator != null && applicationIntegrator.getErrorHandler() != null) {
                        applicationIntegrator.getErrorHandler().handleError(
                            "Uncaught exception in " + thread.getName(), 
                            new RuntimeException(exception)
                        );
                    }
                });
            });
            
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
            // Show confirmation dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit GhostVault");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("All unsaved work will be lost.");
            
            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                // Perform graceful shutdown through integrator
                if (applicationIntegrator != null) {
                    applicationIntegrator.shutdown();
                } else {
                    Platform.exit();
                }
            }
        } catch (Exception e) {
            System.err.println("Error during application exit: " + e.getMessage());
            Platform.exit();
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