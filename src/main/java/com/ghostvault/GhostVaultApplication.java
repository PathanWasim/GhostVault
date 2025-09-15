package com.ghostvault;

import com.ghostvault.integration.ApplicationIntegrator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main application class for GhostVault
 * Coordinates application startup and integrates all components
 */
public class GhostVaultApplication extends Application {
    
    private ApplicationIntegrator applicationIntegrator;
    
    public static void main(String[] args) {
        // Set system properties for better security
        System.setProperty("java.awt.headless", "false");
        System.setProperty("javafx.preloader", "com.ghostvault.ui.SplashScreenPreloader");
        
        // Launch JavaFX application
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize application integrator
            applicationIntegrator = new ApplicationIntegrator();
            
            // Set up global exception handling
            setupGlobalExceptionHandling();
            
            // Initialize all components
            applicationIntegrator.initialize(primaryStage);
            
            System.out.println("🚀 GhostVault started successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start GhostVault: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog and exit
            showStartupError(e);
            Platform.exit();
        }
    }
    
    @Override
    public void stop() throws Exception {
        try {
            if (applicationIntegrator != null) {
                applicationIntegrator.shutdown();
            }
            System.out.println("👋 GhostVault shutdown complete");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
        super.stop();
    }
    
    /**
     * Set up global exception handling
     */
    private void setupGlobalExceptionHandling() {
        // Handle JavaFX thread exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            
            // If we have an integrator, use its error handler
            if (applicationIntegrator != null && applicationIntegrator.getErrorHandler() != null) {
                applicationIntegrator.getErrorHandler().handleError(
                    "Uncaught exception in " + thread.getName(), 
                    new RuntimeException(exception)
                );
            }
        });
    }
    
    /**
     * Show startup error dialog
     */
    private void showStartupError(Exception e) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("GhostVault Startup Error");
            alert.setHeaderText("Failed to start GhostVault");
            alert.setContentText("An error occurred during startup:\n\n" + e.getMessage() + 
                "\n\nPlease check your system configuration and try again.");
            alert.showAndWait();
        });
    }
}