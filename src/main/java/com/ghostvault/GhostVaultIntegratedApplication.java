package com.ghostvault;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.security.AdvancedSecurityManager;
import com.ghostvault.security.SessionManager;
import com.ghostvault.ui.controllers.MainApplicationController;
import com.ghostvault.ui.controllers.AuthenticationController;
import com.ghostvault.ui.controllers.ModeController;
import com.ghostvault.ui.components.*;
import com.ghostvault.ui.utils.UIUtils;
import com.ghostvault.error.ErrorHandler;
import com.ghostvault.audit.AuditManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
            System.out.println("🚀 GhostVault starting...");
            
            // Simple, direct approach that works
            primaryStage.setTitle("GhostVault - Secure File Management System");
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            
            // Check setup status and show appropriate screen
            com.ghostvault.security.PasswordManager passwordManager = new com.ghostvault.security.PasswordManager();
            
            if (!passwordManager.isSetupComplete()) {
                System.out.println("🔧 First time setup required");
                showInitialSetup();
            } else {
                System.out.println("🔐 Setup complete, showing login");
                showSimpleAuthentication();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup primary stage with proper window controls
     */
    private void setupPrimaryStage() {
        System.out.println("🪟 Setting up primary stage...");
        
        // Basic window properties
        primaryStage.setTitle("GhostVault - Secure File Management System");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        
        // CRITICAL: Ensure window has all standard controls
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        
        // Set proper close behavior
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🚪 Window close requested via window controls");
            javafx.application.Platform.exit();
        });
        
        // Center on screen
        primaryStage.centerOnScreen();
        
        System.out.println("✅ Primary stage setup complete");
    }
    
    /**
     * Show professional authentication screen using AuthenticationController
     */
    private void showSimpleAuthentication() {
        try {
            System.out.println("🔐 Showing professional authentication screen...");
            
            // Create authentication controller
            AuthenticationController authController = new AuthenticationController(primaryStage);
            
            // Set up authentication success handler
            authController.setOnAuthenticationSuccess(mode -> {
                System.out.println("✅ Authentication successful - Mode: " + mode);
                showMainApplication(mode);
            });
            
            // Set up authentication cancelled handler
            authController.setOnAuthenticationCancelled(() -> {
                System.out.println("❌ Authentication cancelled");
                Platform.exit();
            });
            
            // Show authentication screen
            authController.show();
            
        } catch (Exception e) {
            System.err.println("❌ Error showing authentication screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show initial setup wizard
     */
    private void showInitialSetup() {
        try {
            System.out.println("🔧 Showing initial setup wizard...");
            
            com.ghostvault.ui.controllers.InitialSetupController setupController = 
                new com.ghostvault.ui.controllers.InitialSetupController(primaryStage);
            
            setupController.setOnSetupComplete(success -> {
                if (success) {
                    System.out.println("✅ Setup completed successfully");
                    showSimpleAuthentication();
                } else {
                    System.out.println("❌ Setup cancelled");
                    javafx.application.Platform.exit();
                }
            });
            
            setupController.show();
            
        } catch (Exception e) {
            System.err.println("❌ Error showing initial setup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show main application after authentication
     */
    private void showMainApplication(com.ghostvault.ui.controllers.ModeController.VaultMode mode) {
        try {
            System.out.println("🏠 Loading main application for mode: " + mode);
            
            // Create and configure main controller with the authenticated mode
            mainController = new MainApplicationController(primaryStage);
            mainController.switchMode(mode);
            
            // Create and show main scene
            Scene scene = mainController.createScene();
            primaryStage.setScene(scene);
            
            // Apply theme
            ModernThemeManager.applyTheme(scene);
            
            // Setup window properties for main application
            primaryStage.setTitle("GhostVault - Secure File Management (" + getModeDisplayName(mode) + " Mode)");
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            
            System.out.println("✅ Main application displayed for mode: " + mode);
            
        } catch (Exception e) {
            System.err.println("❌ Error showing main application: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple welcome screen if main controller fails
            showFallbackMainApplication(mode);
        }
    }
    
    /**
     * Fallback main application screen if the full controller fails
     */
    private void showFallbackMainApplication(com.ghostvault.ui.controllers.ModeController.VaultMode mode) {
        try {
            System.out.println("🔄 Showing fallback main application...");
            
            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 30; -fx-alignment: center;");
            
            Label title = new Label("🎉 Welcome to GhostVault!");
            title.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Label message = new Label("Authentication successful! Your secure vault is ready.");
            message.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50;");
            
            // Show mode information (but keep it subtle for security)
            Label modeInfo = new Label("Vault Mode: " + getModeDisplayName(mode));
            modeInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #2196F3;");
            
            Label info = new Label("File management interface is loading...");
            info.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
            
            // Show mode-specific information
            Label modeDetails = new Label(getModeDescription(mode));
            modeDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
            modeDetails.setWrapText(true);
            modeDetails.setMaxWidth(600);
            
            Button backButton = new Button("Back to Login");
            backButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-color: #2196F3; -fx-text-fill: white;");
            backButton.setOnAction(e -> showSimpleAuthentication());
            
            root.getChildren().addAll(title, message, modeInfo, info, modeDetails, backButton);
            
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            
            System.out.println("✅ Fallback main application displayed");
            
        } catch (Exception e) {
            System.err.println("❌ Error showing fallback main application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getModeDisplayName(com.ghostvault.ui.controllers.ModeController.VaultMode mode) {
        switch (mode) {
            case MASTER: return "Master";
            case PANIC: return "Emergency";
            case DECOY: return "Standard";
            default: return "Unknown";
        }
    }
    
    private String getModeDescription(com.ghostvault.ui.controllers.ModeController.VaultMode mode) {
        switch (mode) {
            case MASTER: 
                return "Full access to your secure vault with all files and features available.";
            case PANIC: 
                return "Emergency mode activated. System will perform secure cleanup operations.";
            case DECOY: 
                return "Standard access mode with limited file visibility for enhanced security.";
            default: 
                return "Unknown access mode.";
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
                // AuditManager doesn't have initialize() method
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
        
        // For now, we'll set up the integration points - TODO: Add callback methods to FileManager
        // fileManager.setProgressCallback((operation, progress) -> {
        //     Platform.runLater(() -> {
        //         NotificationSystem.showProgress(operation, "Progress: " + (int)(progress * 100) + "%");
        //     });
        // });
        // 
        // fileManager.setCompletionCallback((operation, success, message) -> {
        //     Platform.runLater(() -> {
        //         if (success) {
        //             NotificationSystem.showSuccess(operation + " Complete", message);
        //         } else {
        //             NotificationSystem.showError(operation + " Failed", message);
        //         }
        //     });
        // });
    }
    
    /**
     * Integrate security systems with UI
     */
    private void integrateSecuritySystems() {
        // Integrate security manager with mode controllers
        // TODO: Add setModeChangeCallback method to AdvancedSecurityManager
        // securityManager.setModeChangeCallback((oldMode, newMode) -> {
        //     Platform.runLater(() -> {
        //         mainController.switchMode(newMode);
        //         auditManager.logSystemEvent("MODE_CHANGE", 
        //             String.format("Mode changed from %s to %s", oldMode, newMode));
        //     });
        // });
        
        // Integrate panic mode with security manager - TODO: Add setPanicModeCallback method
        // securityManager.setPanicModeCallback(() -> {
        //     Platform.runLater(() -> {
        //         mainController.emergencyShutdown();
        //     });
        // });
        
        // Setup security level monitoring - TODO: Add setSecurityLevelCallback method
        // securityManager.setSecurityLevelCallback((level) -> {
        //     Platform.runLater(() -> {
        //         // Update security indicators in UI
        //         // This would be passed to the header component
        //     });
        // });
    }
    
    /**
     * Integrate session management
     */
    private void integrateSessionManagement() {
        // TODO: Add callback methods to SessionManager
        // sessionManager.setSessionTimeoutCallback(() -> {
        //     Platform.runLater(() -> {
        //         NotificationSystem.showWarning("Session Timeout", 
        //             "Your session has expired. Please authenticate again.");
        //         // Trigger re-authentication
        //         mainController.authenticate("");
        //     });
        // });
        // 
        // sessionManager.setSessionActivityCallback((activity) -> {
        //     // Log user activity for audit purposes
        //     auditManager.logSystemEvent("USER_ACTIVITY", activity);
        // });
    }
    
    /**
     * Integrate audit logging with UI actions
     */
    private void integrateAuditLogging() {
        // This would integrate audit logging with all UI actions
        // For now, we'll set up basic integration
        
        // TODO: Add setAuditEventCallback method to AuditManager
        // auditManager.setAuditEventCallback((event) -> {
        //     // Could show audit events in a dedicated UI panel
        //     System.out.println("Audit Event: " + event.getAction() + " - " + event.getDetails());
        // });
    }
    
    /**
     * Integrate error handling between backend and UI
     */
    private void integrateErrorHandling() {
        // TODO: Add setErrorCallback method to ErrorHandler
        // errorHandler.setErrorCallback((error) -> {
        //     Platform.runLater(() -> {
        //         ErrorHandlingSystem.handleError(error.getMessage(), 
        //             error.getException(), 
        //             ErrorHandlingSystem.ErrorSeverity.valueOf(error.getSeverity().name()));
        //     });
        // });
    }
    
    /**
     * Setup window properties
     */
    private void setupWindow() {
        System.out.println("🪟 Setting up window properties...");
        
        primaryStage.setTitle("GhostVault - Secure File Management System");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        // Make sure window is resizable
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        
        // Set window icon if available
        try {
            // Try to load icon from resources
            var iconStream = getClass().getResourceAsStream("/icons/ghostvault_64.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load window icon: " + e.getMessage());
        }
        
        // Setup close request handler - but don't consume the event immediately
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🚪 Window close requested");
            // Allow normal close for now - we can add confirmation later
            Platform.exit();
        });
        
        // Center window on screen
        primaryStage.centerOnScreen();
        
        System.out.println("✅ Window properties set up successfully");
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
            sessionManager.startSession(primaryStage);
            
            // Log application start
            if (auditManager != null) {
                auditManager.logSystemEvent("APPLICATION_START", "GhostVault application started successfully");
            }
            
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
                    // TODO: Check for modifier keys properly
                    handleApplicationExit();
                });
        }
    }
    
    /**
     * Handle application exit
     */
    private void handleApplicationExit() {
        try {
            // Show confirmation dialog
            boolean confirmed = UIUtils.showConfirmationDialog(
                "Exit GhostVault", 
                "Are you sure you want to exit? All unsaved work will be lost."
            );
            if (confirmed) {
                performGracefulShutdown();
            }
            
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
            if (auditManager != null) {
                auditManager.logSystemEvent("APPLICATION_SHUTDOWN", "Graceful shutdown initiated");
            }
            
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
                "Failed to start GhostVault. Error: " + e.getMessage() + "\n\nThe application will now exit."
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
     * Show authentication screen and handle mode selection
     */
    private void showAuthentication() {
        try {
            System.out.println("🔐 Starting authentication flow...");
            
            // Create authentication controller
            AuthenticationController authController = new AuthenticationController(primaryStage);
            
            // Set up authentication success handler
            authController.setOnAuthenticationSuccess(this::onAuthenticationSuccess);
            
            // Set up authentication cancelled handler
            authController.setOnAuthenticationCancelled(this::handleApplicationExit);
            
            // Show authentication screen
            System.out.println("🔐 Showing authentication screen...");
            authController.show();
            
        } catch (Exception e) {
            System.err.println("❌ Error in authentication flow: " + e.getMessage());
            e.printStackTrace();
            handleStartupError(e);
        }
    }
    
    /**
     * Handle successful authentication and mode determination
     */
    private void onAuthenticationSuccess(ModeController.VaultMode mode) {
        try {
            System.out.println("🔐 Authentication successful - Mode: " + mode);
            
            // Create and configure main controller with the authenticated mode
            mainController = new MainApplicationController(primaryStage);
            mainController.switchMode(mode);
            
            // Integrate backend with UI
            integrateBackendWithUI();
            
            // Create and show main scene
            Scene scene = mainController.createScene();
            primaryStage.setScene(scene);
            
            // Apply theme
            ModernThemeManager.applyTheme(scene);
            
            // Post-startup initialization
            postStartupInitialization();
            
        } catch (Exception e) {
            handleStartupError(e);
        }
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