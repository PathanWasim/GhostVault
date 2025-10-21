package com.ghostvault;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.security.AdvancedSecurityManager;
import com.ghostvault.security.SessionManager;
import com.ghostvault.ui.controllers.MainApplicationController;
import com.ghostvault.ui.components.*;
import com.ghostvault.ui.utils.UIUtils;
import com.ghostvault.error.ErrorHandler;
import com.ghostvault.audit.AuditManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Complete Integrated GhostVault Application
 * Combines all backend systems with the modern UI
 */
public class GhostVaultIntegratedApplication extends Application {
    
    // Core system components
    private AppConfig appConfig;
    private FileManager fileManager;
    private AdvancedSecurityManager securityManager;
    private SessionManager sessionManager;
    private AuditManager auditManager;
    private ErrorHandler errorHandler;
    
    // UI components
    private MainApplicationController mainController;
    private Stage primaryStage;
    
    @Override
    public void init() throws Exception {
        super.init();
        initializeBackendSystems();
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Initialize UI systems
            initializeUISystem();
            
            // Create and configure main controller
            mainController = new MainApplicationController(primaryStage);
            
            // Integrate backend with UI
            integrateBackendWithUI();
            
            // Setup window properties
            setupWindow();
            
            // Create and show scene
            Scene scene = mainController.createScene();
            primaryStage.setScene(scene);
            
            // Apply theme and show
            ModernThemeManager.applyTheme(scene);
            primaryStage.show();
            
            // Post-startup initialization
            postStartupInitialization();
            
        } catch (Exception e) {
            handleStartupError(e);
        }
    }
    
    /**
     * Initialize all backend systems
     */
    private void initializeBackendSystems() {
        try {
            // Initialize configuration - AppConfig is a constants class
            // Configuration is loaded from constants
            
            // Initialize error handling first - TODO: Fix constructor parameters
            // errorHandler = new ErrorHandler();
            // ErrorHandlingSystem.getInstance().setBackendErrorHandler(errorHandler);
            
            // Initialize audit system
            try {
                auditManager = new AuditManager();
                auditManager.initialize();
            } catch (Exception e) {
                System.err.println("Failed to initialize audit manager: " + e.getMessage());
            }
            
            // Initialize security manager
            securityManager = new AdvancedSecurityManager();
            securityManager.initializeAdvancedSecurity();
            
            // Initialize session manager
            sessionManager = new SessionManager();
            
            // Initialize file manager
            try {
                fileManager = new FileManager(AppConfig.VAULT_DIR);
            } catch (Exception e) {
                System.err.println("Failed to initialize file manager: " + e.getMessage());
            }
            
            // Log successful initialization
            if (auditManager != null) {
                auditManager.logSystemEvent("SYSTEM_INIT", "Backend systems initialized successfully");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to initialize backend systems: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Backend initialization failed", e);
        }
    }
    
    /**
     * Initialize UI system components
     */
    private void initializeUISystem() {
        try {
            // Initialize theme system
            ModernThemeManager.initialize();
            
            // Initialize notification system (will be done by main controller)
            // NotificationSystem will be initialized when main controller creates scene
            
            // Set system properties for better performance
            System.setProperty("javafx.animation.fullspeed", "true");
            System.setProperty("javafx.animation.pulse", "60");
            System.setProperty("prism.lcdtext", "false");
            System.setProperty("prism.subpixeltext", "false");
            
        } catch (Exception e) {
            throw new RuntimeException("UI system initialization failed", e);
        }
    }
    
    /**
     * Integrate backend systems with UI components
     */
    private void integrateBackendWithUI() {
        try {
            // Integrate file operations
            integrateFileOperations();
            
            // Integrate security systems
            integrateSecuritySystems();
            
            // Integrate session management
            integrateSessionManagement();
            
            // Integrate audit logging
            integrateAuditLogging();
            
            // Setup error handling integration
            integrateErrorHandling();
            
        } catch (Exception e) {
            throw new RuntimeException("Backend-UI integration failed", e);
        }
    }
    
    /**
     * Integrate file operations with backend FileManager
     */
    private void integrateFileOperations() {
        // Get the file operations component from main controller
        // This would be done through proper dependency injection in a real system
        
        // For now, we'll set up the integration points
        fileManager.setProgressCallback((operation, progress) -> {
            Platform.runLater(() -> {
                NotificationSystem.showProgress(operation, "Progress: " + (int)(progress * 100) + "%");
            });
        });
        
        fileManager.setCompletionCallback((operation, success, message) -> {
            Platform.runLater(() -> {
                if (success) {
                    NotificationSystem.showSuccess(operation + " Complete", message);
                } else {
                    NotificationSystem.showError(operation + " Failed", message);
                }
            });
        });
    }
    
    /**
     * Integrate security systems with UI
     */
    private void integrateSecuritySystems() {
        // Integrate security manager with mode controllers
        securityManager.setModeChangeCallback((oldMode, newMode) -> {
            Platform.runLater(() -> {
                mainController.switchMode(newMode);
                auditManager.logEvent("MODE_CHANGE", 
                    String.format("Mode changed from %s to %s", oldMode, newMode));
            });
        });
        
        // Integrate panic mode with security manager
        securityManager.setPanicModeCallback(() -> {
            Platform.runLater(() -> {
                mainController.emergencyShutdown();
            });
        });
        
        // Setup security level monitoring
        securityManager.setSecurityLevelCallback((level) -> {
            Platform.runLater(() -> {
                // Update security indicators in UI
                // This would be passed to the header component
            });
        });
    }
    
    /**
     * Integrate session management
     */
    private void integrateSessionManagement() {
        sessionManager.setSessionTimeoutCallback(() -> {
            Platform.runLater(() -> {
                NotificationSystem.showWarning("Session Timeout", 
                    "Your session has expired. Please authenticate again.");
                // Trigger re-authentication
                mainController.authenticate("");
            });
        });
        
        sessionManager.setSessionActivityCallback((activity) -> {
            // Log user activity for audit purposes
            auditManager.logEvent("USER_ACTIVITY", activity);
        });
    }
    
    /**
     * Integrate audit logging with UI actions
     */
    private void integrateAuditLogging() {
        // This would integrate audit logging with all UI actions
        // For now, we'll set up basic integration
        
        auditManager.setAuditEventCallback((event) -> {
            // Could show audit events in a dedicated UI panel
            System.out.println("Audit Event: " + event.getAction() + " - " + event.getDetails());
        });
    }
    
    /**
     * Integrate error handling between backend and UI
     */
    private void integrateErrorHandling() {
        errorHandler.setErrorCallback((error) -> {
            Platform.runLater(() -> {
                ErrorHandlingSystem.handleError(error.getMessage(), 
                    error.getException(), 
                    ErrorHandlingSystem.ErrorSeverity.valueOf(error.getSeverity().name()));
            });
        });
    }
    
    /**
     * Setup window properties
     */
    private void setupWindow() {
        primaryStage.setTitle("GhostVault - Secure File Management System");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Set window icon if available
        try {
            primaryStage.getIcons().add(UIUtils.loadImageFromResources("/icons/ghostvault-icon.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Setup close request handler
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Prevent immediate close
            handleApplicationExit();
        });
        
        // Center window on screen
        UIUtils.centerStage(primaryStage);
    }
    
    /**
     * Post-startup initialization
     */
    private void postStartupInitialization() {
        Platform.runLater(() -> {
            // Show welcome notification
            NotificationSystem.showSuccess("Welcome to GhostVault", 
                "Secure file management system initialized successfully");
            
            // Start session
            sessionManager.startSession("default_user");
            
            // Log application start
            auditManager.logEvent("APPLICATION_START", "GhostVault application started successfully");
            
            // Initialize keyboard shortcuts
            setupGlobalKeyboardShortcuts();
        });
    }
    
    /**
     * Setup global keyboard shortcuts
     */
    private void setupGlobalKeyboardShortcuts() {
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            KeyboardShortcutManager.initialize(scene);
            
            // Emergency shortcuts
            KeyboardShortcutManager.register("emergency_panic", 
                javafx.scene.input.KeyCode.F12, 
                () -> {
                    mainController.switchMode(com.ghostvault.ui.controllers.ModeController.VaultMode.PANIC);
                });
            
            KeyboardShortcutManager.register("emergency_exit", 
                javafx.scene.input.KeyCode.ESCAPE, 
                () -> {
                    if (javafx.scene.input.KeyEvent.getModifier() != null) {
                        handleApplicationExit();
                    }
                });
        }
    }
    
    /**
     * Handle application exit
     */
    private void handleApplicationExit() {
        try {
            // Show confirmation dialog
            UIUtils.showConfirmationDialog(
                "Exit GhostVault", 
                "Are you sure you want to exit?", 
                "All unsaved work will be lost."
            ).thenAccept(confirmed -> {
                if (confirmed) {
                    performGracefulShutdown();
                }
            });
            
        } catch (Exception e) {
            // Force shutdown if graceful shutdown fails
            performForceShutdown();
        }
    }
    
    /**
     * Perform graceful shutdown
     */
    private void performGracefulShutdown() {
        try {
            // Log shutdown
            auditManager.logEvent("APPLICATION_SHUTDOWN", "Graceful shutdown initiated");
            
            // Cleanup UI components
            if (mainController != null) {
                mainController.shutdown();
            }
            
            // Cleanup backend systems
            cleanupBackendSystems();
            
            // Close application
            Platform.exit();
            
        } catch (Exception e) {
            System.err.println("Error during graceful shutdown: " + e.getMessage());
            performForceShutdown();
        }
    }
    
    /**
     * Perform force shutdown
     */
    private void performForceShutdown() {
        try {
            // Force cleanup
            if (sessionManager != null) {
                sessionManager.endSession();
            }
            
            // Force exit
            Platform.exit();
            System.exit(0);
            
        } catch (Exception e) {
            // Last resort
            System.exit(1);
        }
    }
    
    /**
     * Cleanup backend systems
     */
    private void cleanupBackendSystems() {
        try {
            if (sessionManager != null) {
                sessionManager.endSession();
            }
            
            if (fileManager != null) {
                fileManager.cleanup();
            }
            
            if (securityManager != null) {
                securityManager.cleanup();
            }
            
            if (auditManager != null) {
                auditManager.cleanup();
            }
            
        } catch (Exception e) {
            System.err.println("Error during backend cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Handle startup errors
     */
    private void handleStartupError(Exception e) {
        System.err.println("Application startup failed: " + e.getMessage());
        e.printStackTrace();
        
        Platform.runLater(() -> {
            UIUtils.showErrorDialog(
                "Startup Error", 
                "Failed to start GhostVault", 
                "Error: " + e.getMessage() + "\n\nThe application will now exit."
            );
            
            Platform.exit();
        });
    }
    
    @Override
    public void stop() throws Exception {
        cleanupBackendSystems();
        super.stop();
    }
    
    /**
     * Get main controller (for testing or external access)
     */
    public MainApplicationController getMainController() {
        return mainController;
    }
    
    /**
     * Get file manager (for testing or external access)
     */
    public FileManager getFileManager() {
        return fileManager;
    }
    
    /**
     * Get security manager (for testing or external access)
     */
    public AdvancedSecurityManager getSecurityManager() {
        return securityManager;
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        // Set system properties for better JavaFX performance
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.subpixeltext", "false");
        
        // Launch application
        launch(args);
    }
}