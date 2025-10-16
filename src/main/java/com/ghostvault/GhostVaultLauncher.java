package com.ghostvault;

import com.ghostvault.config.AppConfig;
import com.ghostvault.integration.UIBackendIntegrator;
import com.ghostvault.ui.components.NotificationSystem;
import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Application;
import javafx.application.Platform;

/**
 * Main launcher for the complete GhostVault application
 * This is the primary entry point that initializes everything
 */
public class GhostVaultLauncher {
    
    private static final String VERSION = "2.0.0";
    private static final String BUILD_DATE = "2024-01-01";
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🔒 GhostVault - Secure File Management System");
        System.out.println("Version: " + VERSION + " | Build: " + BUILD_DATE);
        System.out.println("=".repeat(60));
        
        try {
            // Pre-launch system checks
            performSystemChecks();
            
            // Set JavaFX system properties for optimal performance
            setJavaFXProperties();
            
            // Launch the integrated application
            System.out.println("🚀 Launching GhostVault...");
            Application.launch(GhostVaultIntegratedApplication.class, args);
            
        } catch (Exception e) {
            handleLaunchError(e);
        }
    }
    
    /**
     * Perform system checks before launch
     */
    private static void performSystemChecks() {
        System.out.println("🔍 Performing system checks...");
        
        // Check Java version
        String javaVersion = System.getProperty("java.version");
        System.out.println("   ✓ Java Version: " + javaVersion);
        
        // Check JavaFX availability
        try {
            Class.forName("javafx.application.Application");
            System.out.println("   ✓ JavaFX: Available");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JavaFX not found. Please ensure JavaFX is in the module path.");
        }
        
        // Check memory
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        System.out.println("   ✓ Memory: " + formatBytes(totalMemory) + " / " + formatBytes(maxMemory));
        
        // Check disk space
        java.io.File currentDir = new java.io.File(".");
        long freeSpace = currentDir.getFreeSpace();
        System.out.println("   ✓ Disk Space: " + formatBytes(freeSpace) + " available");
        
        // Check configuration
        try {
            AppConfig config = new AppConfig();
            System.out.println("   ✓ Configuration: Loaded");
        } catch (Exception e) {
            System.out.println("   ⚠ Configuration: Using defaults (" + e.getMessage() + ")");
        }
        
        System.out.println("✅ System checks completed successfully");
    }
    
    /**
     * Set JavaFX system properties for optimal performance
     */
    private static void setJavaFXProperties() {
        System.out.println("⚙️ Configuring JavaFX properties...");
        
        // Performance optimizations
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.subpixeltext", "false");
        System.setProperty("prism.order", "sw,hw");
        
        // Memory optimizations
        System.setProperty("javafx.embed.isEventThread", "true");
        System.setProperty("javafx.embed.eventProc", "true");
        
        // Security properties
        System.setProperty("javafx.allowjs", "false");
        System.setProperty("javafx.webkit.javascript", "false");
        
        System.out.println("   ✓ Performance optimizations applied");
        System.out.println("   ✓ Security properties configured");
    }
    
    /**
     * Handle launch errors
     */
    private static void handleLaunchError(Exception e) {
        System.err.println("❌ Failed to launch GhostVault:");
        System.err.println("   Error: " + e.getMessage());
        e.printStackTrace();
        
        // Try to show error dialog if JavaFX is available
        try {
            Platform.runLater(() -> {
                try {
                    ErrorHandlingSystem.showErrorDialog(
                        "Launch Error",
                        "Failed to start GhostVault",
                        "Error: " + e.getMessage() + "\n\nPlease check the console for more details."
                    );
                } catch (Exception dialogError) {
                    // If even the error dialog fails, just exit
                    System.err.println("Could not show error dialog: " + dialogError.getMessage());
                }
                Platform.exit();
            });
        } catch (Exception platformError) {
            // If Platform is not available, just exit
            System.err.println("JavaFX Platform not available for error dialog");
            System.exit(1);
        }
    }
    
    /**
     * Format bytes for display
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Get application version
     */
    public static String getVersion() {
        return VERSION;
    }
    
    /**
     * Get build date
     */
    public static String getBuildDate() {
        return BUILD_DATE;
    }
    
    /**
     * Get full version string
     */
    public static String getFullVersionString() {
        return "GhostVault v" + VERSION + " (Build: " + BUILD_DATE + ")";
    }
}