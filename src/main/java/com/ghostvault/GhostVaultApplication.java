package com.ghostvault;

import com.ghostvault.integration.UIBackendIntegrator;
import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main GhostVault Application - Complete Integrated System
 * Connects modern UI with all backend services
 */
public class GhostVaultApplication extends Application {
    
    private UIBackendIntegrator integrator;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Set system properties for optimal performance
            System.setProperty("javafx.animation.fullspeed", "true");
            System.setProperty("javafx.animation.pulse", "60");
            System.setProperty("prism.lcdtext", "false");
            System.setProperty("prism.subpixeltext", "false");
            
            // Create and initialize the complete integrated system
            integrator = new UIBackendIntegrator(primaryStage);
            
            // Start the integrated application
            integrator.startApplication();
            
        } catch (Exception e) {
            e.printStackTrace();
            ErrorHandlingSystem.handleError("Application startup failed", e, 
                ErrorHandlingSystem.ErrorSeverity.CRITICAL);
            
            // Show error dialog and exit
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Startup Error");
                alert.setHeaderText("GhostVault failed to start");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                Platform.exit();
            });
        }
    }
    
    @Override
    public void stop() {
        try {
            if (integrator != null) {
                integrator.shutdown();
            }
        } catch (Exception e) {
            // Force exit if shutdown fails
            System.exit(1);
        }
    }
    
    /**
     * Emergency shutdown hook
     */
    public void emergencyShutdown() {
        if (integrator != null) {
            integrator.emergencyShutdown();
        } else {
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        // Add shutdown hook for emergency situations
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("GhostVault shutdown hook executed");
        }));
        
        // Handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            
            // Try emergency shutdown
            Platform.runLater(() -> {
                try {
                    Platform.exit();
                } catch (Exception e) {
                    System.exit(1);
                }
            });
        });
        
        // Launch the application
        launch(args);
    }
}