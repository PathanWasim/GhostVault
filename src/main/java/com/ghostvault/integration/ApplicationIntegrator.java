package com.ghostvault.integration;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.error.ErrorHandler;
import com.ghostvault.security.*;
import com.ghostvault.ui.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central application integrator that coordinates all components
 * Manages application lifecycle, state transitions, and component interactions
 */
public class ApplicationIntegrator {
    
    // Core components
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private SessionManager sessionManager;
    private DecoyManager decoyManager;
    private PanicModeExecutor panicModeExecutor;
    private VaultBackupManager backupManager;
    private AdvancedSecurityManager advancedSecurityManager;
    private ThreatDetectionEngine threatDetectionEngine;
    private SecurityAttemptManager securityAttemptManager;
    
    // UI components
    private UIManager uiManager;
    private NotificationManager notificationManager;
    private ErrorDialog errorDialog;
    private SystemTrayManager systemTrayManager;
    
    // Error handling
    private ErrorHandler errorHandler;
    
    // Application state
    private ApplicationState currentState;
    private SecretKey currentKey;
    private Stage primaryStage;
    private ExecutorService backgroundExecutor;
    
    // Security context
    private SecurityContext securityContext;
    
    public enum ApplicationState {
        INITIALIZING,
        FIRST_RUN_SETUP,
        LOGIN,
        MASTER_VAULT,
        DECOY_VAULT,
        PANIC_MODE,
        LOCKED,
        SHUTTING_DOWN
    }
    
    public ApplicationIntegrator() {
        this.currentState = ApplicationState.INITIALIZING;
        this.backgroundExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "GhostVault-Background");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Initialize all application components
     */
    public void initialize(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        try {
            // Initialize core security components
            initializeCoreComponents();
            
            // Initialize UI components
            initializeUIComponents();
            
            // Initialize error handling
            initializeErrorHandling();
            
            // Initialize advanced security
            initializeAdvancedSecurity();
            
            // Set up component integrations
            setupComponentIntegrations();
            
            // Determine initial state
            determineInitialState();
            
            // Start security monitoring
            startSecurityMonitoring();
            
            System.out.println("✅ GhostVault application initialized successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize GhostVault: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initialize core security and data components
     */
    private void initializeCoreComponents() throws Exception {
        // Core security
        cryptoManager = new CryptoManager();
        passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        // Data management
        fileManager = new FileManager(AppConfig.VAULT_DIR);
        metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        
        // Audit and session management
        auditManager = new AuditManager();
        sessionManager = new SessionManager();
        
        // Special modes
        decoyManager = new DecoyManager();
        panicModeExecutor = new PanicModeExecutor();
        
        // Backup management
        backupManager = new VaultBackupManager(cryptoManager, fileManager, metadataManager, auditManager);
        
        // Security attempt management
        securityAttemptManager = new SecurityAttemptManager(auditManager);
        
        System.out.println("🔐 Core components initialized");
    }
    
    /**
     * Initialize UI components
     */
    private void initializeUIComponents() throws Exception {
        uiManager = new UIManager();
        notificationManager = NotificationManager.getInstance();
        notificationManager.initialize(primaryStage);
        
        // Set up UI manager with primary stage
        uiManager.initialize(primaryStage);
        
        // Initialize system tray
        systemTrayManager = new SystemTrayManager(primaryStage);
        if (systemTrayManager.initializeSystemTray()) {
            // Set up minimize to tray behavior
            primaryStage.setOnCloseRequest(event -> {
                event.consume(); // Prevent default close
                systemTrayManager.minimizeToTray();
            });
        }
        
        // Set this integrator reference in UI manager
        uiManager.setApplicationIntegrator(this);
        
        System.out.println("🎨 UI components initialized");
    }
    
    /**
     * Initialize error handling system
     */
    private void initializeErrorHandling() {
        errorHandler = new ErrorHandler(auditManager, notificationManager);
        
        // Set up global exception handler with loop prevention
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            // Print to console for debugging
            System.err.println("❌ Uncaught exception in " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            
            // Don't try to handle ClassCastException from error handler itself (prevents loops)
            if (exception instanceof ClassCastException) {
                System.err.println("⚠️ ClassCastException detected - skipping error handler to prevent loop");
                return;
            }
            
            try {
                // Convert Throwable to Exception properly
                Exception exceptionToHandle;
                if (exception instanceof Exception) {
                    exceptionToHandle = (Exception) exception;
                } else {
                    exceptionToHandle = new RuntimeException(exception);
                }
                
                errorHandler.handleError("Uncaught exception in " + thread.getName(), exceptionToHandle);
                
                // Log critical error
                if (auditManager != null) {
                    auditManager.logSecurityEvent("CRITICAL_ERROR", 
                        "Uncaught exception: " + exception.getMessage(), 
                        AuditManager.AuditSeverity.CRITICAL, null, 
                        "Thread: " + thread.getName());
                }
            } catch (Exception handlerException) {
                System.err.println("⚠️ Error in exception handler: " + handlerException.getMessage());
                handlerException.printStackTrace();
            }
        });
        
        System.out.println("🛠️ Error handling initialized");
    }
    
    /**
     * Initialize advanced security features
     */
    private void initializeAdvancedSecurity() throws Exception {
        advancedSecurityManager = new AdvancedSecurityManager();
        threatDetectionEngine = new ThreatDetectionEngine(auditManager);
        
        // Initialize advanced security
        advancedSecurityManager.initializeAdvancedSecurity();
        
        System.out.println("🛡️ Advanced security initialized");
    }
    
    /**
     * Set up integrations between components
     */
    private void setupComponentIntegrations() {
        // Session manager integration
        sessionManager.setTimeoutCallback(() -> {
            Platform.runLater(() -> {
                handleSessionTimeout();
            });
        });
        
        // Integrate SecurityAttemptManager with SessionManager
        sessionManager.setSecurityAttemptManager(securityAttemptManager);
        
        // UI manager integration with notification manager
        uiManager.setNotificationManager(notificationManager);
        
        // Error handler integration
        // ErrorDialog is constructed per-use with ErrorHandlingResult; no global handler to set
        
        System.out.println("🔗 Component integrations configured");
    }
    
    /**
     * Determine initial application state with enhanced configuration detection
     */
    private void determineInitialState() throws Exception {
        System.out.println("🔍 Determining initial application state...");
        
        try {
            // Get detailed configuration validation
            var configValidation = passwordManager.getConfigurationValidation();
            
            System.out.println("📋 Configuration Status: " + configValidation.getStatus());
            
            switch (configValidation.getStatus()) {
                case VALID:
                    // Configuration is valid, proceed to login
                    System.out.println("✅ Valid configuration found - proceeding to login");
                    transitionToState(ApplicationState.LOGIN);
                    break;
                    
                case MISSING:
                    // No configuration found, first run setup required
                    System.out.println("📝 No configuration found - first run setup required");
                    transitionToState(ApplicationState.FIRST_RUN_SETUP);
                    break;
                    
                case CORRUPTED:
                case INCOMPLETE:
                    // Configuration has issues, attempt recovery
                    System.out.println("⚠️ Configuration issues detected, attempting recovery...");
                    handleConfigurationRecovery(configValidation);
                    break;
                    
                case BACKUP_AVAILABLE:
                    // Primary config missing but backup available
                    System.out.println("🔄 Primary configuration missing, backup available");
                    handleBackupRecovery(configValidation);
                    break;
                    
                case INACCESSIBLE:
                    // Configuration file cannot be accessed
                    System.err.println("❌ Configuration file inaccessible");
                    handleConfigurationError(configValidation);
                    break;
                    
                default:
                    // Unknown status
                    System.err.println("❓ Unknown configuration status");
                    handleConfigurationError(configValidation);
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error determining initial state: " + e.getMessage());
            
            // Log the error
            if (auditManager != null) {
                auditManager.logSecurityEvent("INITIALIZATION_ERROR", 
                    "Failed to determine initial application state", 
                    AuditManager.AuditSeverity.CRITICAL, null, e.getMessage());
            }
            
            // Default to first run setup on error
            transitionToState(ApplicationState.FIRST_RUN_SETUP);
        }
    }
    
    /**
     * Handle configuration recovery scenarios
     */
    private void handleConfigurationRecovery(com.ghostvault.config.ConfigurationValidator.ValidationResult validation) {
        try {
            System.out.println("🔧 Attempting configuration recovery...");
            
            if (passwordManager.recoverConfiguration()) {
                System.out.println("✅ Configuration recovered successfully");
                
                // Log successful recovery
                auditManager.logSecurityEvent("CONFIG_RECOVERY_SUCCESS", 
                    "Configuration successfully recovered", 
                    AuditManager.AuditSeverity.INFO, null, 
                    "Original status: " + validation.getStatus());
                
                // Proceed to login
                transitionToState(ApplicationState.LOGIN);
                
            } else {
                System.err.println("❌ Configuration recovery failed");
                
                // Log failed recovery
                auditManager.logSecurityEvent("CONFIG_RECOVERY_FAILED", 
                    "Configuration recovery failed", 
                    AuditManager.AuditSeverity.WARNING, null, 
                    "Status: " + validation.getStatus() + ", Error: " + validation.getErrorMessage());
                
                // Show recovery dialog to user
                showConfigurationRecoveryDialog(validation);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception during configuration recovery: " + e.getMessage());
            showConfigurationRecoveryDialog(validation);
        }
    }
    
    /**
     * Handle backup recovery scenarios
     */
    private void handleBackupRecovery(com.ghostvault.config.ConfigurationValidator.ValidationResult validation) {
        try {
            System.out.println("🔄 Attempting backup recovery...");
            
            if (passwordManager.recoverConfiguration()) {
                System.out.println("✅ Configuration restored from backup");
                
                // Log successful backup recovery
                auditManager.logSecurityEvent("BACKUP_RECOVERY_SUCCESS", 
                    "Configuration restored from backup", 
                    AuditManager.AuditSeverity.INFO, null, null);
                
                // Show notification to user
                notificationManager.showInfo("Configuration Restored", 
                    "Your configuration has been restored from backup.");
                
                // Proceed to login
                transitionToState(ApplicationState.LOGIN);
                
            } else {
                System.err.println("❌ Backup recovery failed");
                handleConfigurationError(validation);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception during backup recovery: " + e.getMessage());
            handleConfigurationError(validation);
        }
    }
    
    /**
     * Handle configuration errors
     */
    private void handleConfigurationError(com.ghostvault.config.ConfigurationValidator.ValidationResult validation) {
        System.err.println("❌ Configuration error: " + validation.getErrorMessage());
        
        // Log configuration error
        auditManager.logSecurityEvent("CONFIG_ERROR", 
            "Configuration error encountered", 
            AuditManager.AuditSeverity.CRITICAL, null, 
            "Status: " + validation.getStatus() + ", Error: " + validation.getErrorMessage());
        
        // Show error dialog to user
        Platform.runLater(() -> {
            showConfigurationErrorDialog(validation);
        });
        
        // Default to first run setup
        transitionToState(ApplicationState.FIRST_RUN_SETUP);
    }
    
    /**
     * Show configuration recovery dialog to user
     */
    private void showConfigurationRecoveryDialog(com.ghostvault.config.ConfigurationValidator.ValidationResult validation) {
        Platform.runLater(() -> {
            Alert recoveryDialog = new Alert(Alert.AlertType.WARNING);
            recoveryDialog.setTitle("Configuration Recovery");
            recoveryDialog.setHeaderText("Configuration Issue Detected");
            
            StringBuilder message = new StringBuilder();
            message.append("Status: ").append(validation.getStatus().getDescription()).append("\n\n");
            
            if (validation.getErrorMessage() != null) {
                message.append("Error: ").append(validation.getErrorMessage()).append("\n\n");
            }
            
            if (validation.getRecoveryAction() != null) {
                message.append("Recommended Action: ").append(validation.getRecoveryAction()).append("\n\n");
            }
            
            message.append("You can:\n");
            message.append("• Try automatic recovery (if available)\n");
            message.append("• Proceed with first-time setup (will create new configuration)\n");
            message.append("• Exit and manually fix the configuration");
            
            recoveryDialog.setContentText(message.toString());
            
            // Add custom buttons
            ButtonType recoveryButton = new ButtonType("Try Recovery");
            ButtonType setupButton = new ButtonType("First-Time Setup");
            ButtonType exitButton = new ButtonType("Exit");
            
            recoveryDialog.getButtonTypes().setAll(recoveryButton, setupButton, exitButton);
            
            recoveryDialog.showAndWait().ifPresent(response -> {
                if (response == recoveryButton && validation.canRecover()) {
                    // Try recovery again
                    handleConfigurationRecovery(validation);
                } else if (response == setupButton) {
                    // Proceed with first-time setup
                    transitionToState(ApplicationState.FIRST_RUN_SETUP);
                } else {
                    // Exit application
                    shutdown();
                }
            });
        });
    }
    
    /**
     * Show configuration error dialog to user
     */
    private void showConfigurationErrorDialog(com.ghostvault.config.ConfigurationValidator.ValidationResult validation) {
        Alert errorDialog = new Alert(Alert.AlertType.ERROR);
        errorDialog.setTitle("Configuration Error");
        errorDialog.setHeaderText("Cannot Access Configuration");
        
        StringBuilder message = new StringBuilder();
        message.append("Status: ").append(validation.getStatus().getDescription()).append("\n\n");
        message.append("Error: ").append(validation.getErrorMessage()).append("\n\n");
        message.append("The application cannot start with the current configuration.\n");
        message.append("Please check file permissions and disk space, then restart the application.");
        
        errorDialog.setContentText(message.toString());
        
        ButtonType exitButton = new ButtonType("Exit");
        ButtonType setupButton = new ButtonType("Try First-Time Setup");
        
        errorDialog.getButtonTypes().setAll(setupButton, exitButton);
        
        errorDialog.showAndWait().ifPresent(response -> {
            if (response == setupButton) {
                transitionToState(ApplicationState.FIRST_RUN_SETUP);
            } else {
                shutdown();
            }
        });
    }
    
    /**
     * Start security monitoring
     */
    private void startSecurityMonitoring() {
        // Start advanced security hardening
        CompletableFuture.runAsync(() -> {
            try {
                advancedSecurityManager.activateSecurityHardening();
            } catch (Exception e) {
                errorHandler.handleError("Security hardening activation", e);
            }
        }, backgroundExecutor);
        
        // Start threat detection
        CompletableFuture.runAsync(() -> {
            try {
                threatDetectionEngine.startMonitoring();
            } catch (Exception e) {
                errorHandler.handleError("Threat detection startup", e);
            }
        }, backgroundExecutor);
        
        System.out.println("👁️ Security monitoring started");
    }
    
    /**
     * Handle user authentication
     */
    public void handleAuthentication(String password) {
        // Check if account is locked before attempting authentication
        if (securityAttemptManager.isLocked()) {
            Platform.runLater(() -> {
                int remainingSeconds = securityAttemptManager.getRemainingLockoutSeconds();
                showLoginError(String.format("Account temporarily locked. Please wait %d seconds before trying again.", remainingSeconds));
            });
            return;
        }
        
        CompletableFuture.supplyAsync(() -> {
            return errorHandler.handleWithRecovery("password_validation", 
                () -> passwordManager.detectPassword(password.toCharArray()), 
                null);
        }, backgroundExecutor).thenAccept(passwordType -> {
            Platform.runLater(() -> {
                switch (passwordType) {
                    case MASTER -> handleMasterPasswordLogin(password);
                    case PANIC -> handlePanicPasswordLogin();
                    case DECOY -> handleDecoyPasswordLogin(password);
                    case INVALID -> handleInvalidPassword();
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                errorHandler.handleError("Authentication", (Exception) throwable);
                securityAttemptManager.recordFailedAttempt("Authentication system error", "System");
                showLoginError("Authentication failed. Please try again.");
            });
            return null;
        });
    }
    
    /**
     * Handle master password login
     */
    private void handleMasterPasswordLogin(String password) {
        try {
            // Unwrap Vault Master Key
            currentKey = passwordManager.unwrapVMK(password.toCharArray());
            
            // Reset security attempts on successful login
            securityAttemptManager.resetAttempts();
            
            // Start session
            sessionManager.startSession();
            
            // Initialize security context
            securityContext = new SecurityContext(currentKey, PasswordManager.PasswordType.MASTER);
            
            // Transition to master vault
            transitionToState(ApplicationState.MASTER_VAULT);
            
            // Log successful login
            auditManager.logSecurityEvent("LOGIN_SUCCESS", 
                "Master vault access granted", 
                AuditManager.AuditSeverity.INFO, null, null);
            
            // Enhanced security logging
            securityAttemptManager.getSecurityLogger().logAuthenticationEvent("LOGIN_SUCCESS", 
                "Master vault access granted", "User", "Authentication method: Master password");
            
            // Show vault interface
            showVaultInterface(false); // false = not decoy mode
            
        } catch (Exception e) {
            errorHandler.handleError("Master login", e);
            securityAttemptManager.recordFailedAttempt("Master password validation failed", "System");
        }
    }
    
    /**
     * Handle panic password login
     */
    private void handlePanicPasswordLogin() {
        try {
            // Log panic mode activation (before destruction)
            auditManager.logSecurityEvent("PANIC_MODE_ACTIVATED", 
                "Emergency data destruction initiated", 
                AuditManager.AuditSeverity.CRITICAL, null, null);
            
            // Enhanced security logging for critical event
            securityAttemptManager.getSecurityLogger().logSystemSecurityEvent("PANIC_MODE_ACTIVATED", 
                "Emergency data destruction initiated", "User", "Panic password authentication");
            
            // Transition to panic mode
            transitionToState(ApplicationState.PANIC_MODE);
            
            // Execute panic wipe in background
            CompletableFuture.runAsync(() -> {
                panicModeExecutor.executePanic(java.nio.file.Paths.get(AppConfig.VAULT_DIR), false);
            }, backgroundExecutor).thenRun(() -> {
                // Shutdown application after panic wipe
                Platform.runLater(() -> {
                    shutdown();
                });
            });
            
            // Show normal login interface to maintain cover
            showLoginInterface();
            
        } catch (Exception e) {
            // Silent error handling in panic mode
            shutdown();
        }
    }
    
    /**
     * Handle decoy password login
     */
    private void handleDecoyPasswordLogin(String password) {
        try {
            // Initialize decoy vault with minimum files
            decoyManager.ensureMinimumDecoyFiles(8);
            
            // Reset security attempts on successful login (even decoy)
            securityAttemptManager.resetAttempts();
            
            // Create decoy security context
            securityContext = new SecurityContext(null, PasswordManager.PasswordType.DECOY);
            
            // Start session
            sessionManager.startSession();
            
            // Transition to decoy vault
            transitionToState(ApplicationState.DECOY_VAULT);
            
            // Log decoy access (appears as normal login)
            auditManager.logSecurityEvent("LOGIN_SUCCESS", 
                "Vault access granted", 
                AuditManager.AuditSeverity.INFO, null, null);
            
            // Enhanced security logging (appears as normal login for security)
            securityAttemptManager.getSecurityLogger().logAuthenticationEvent("LOGIN_SUCCESS", 
                "Vault access granted", "User", "Authentication method: Standard password");
            
            // Show decoy vault interface
            showVaultInterface(true); // true = decoy mode
            
        } catch (Exception e) {
            errorHandler.handleError("Decoy login", e);
            securityAttemptManager.recordFailedAttempt("Decoy password validation failed", "System");
        }
    }
    
    /**
     * Handle invalid password
     */
    private void handleInvalidPassword() {
        // Record failed attempt in security manager
        securityAttemptManager.recordFailedAttempt("Invalid password entered", "User");
        
        // Also record in session manager for compatibility
        sessionManager.recordFailedLogin("user");
        
        // Log failed login
        auditManager.logSecurityEvent("LOGIN_FAILED", 
            "Invalid password attempt", 
            AuditManager.AuditSeverity.WARNING, null, null);
        
        // Check if account is now locked
        if (securityAttemptManager.isLocked()) {
            int remainingSeconds = securityAttemptManager.getRemainingLockoutSeconds();
            showLoginError(String.format("Too many failed attempts. Account locked for %d seconds.", remainingSeconds));
        } else {
            int attempts = securityAttemptManager.getAttemptCount();
            int maxAttempts = securityAttemptManager.getMaxAttempts();
            int remaining = maxAttempts - attempts;
            
            if (remaining <= 1) {
                showLoginError(String.format("Invalid password. Warning: %d attempt remaining before lockout.", remaining));
            } else {
                showLoginError(String.format("Invalid password. %d attempts remaining.", remaining));
            }
        }
    }
    
    /**
     * Handle session timeout
     */
    private void handleSessionTimeout() {
        try {
            // Clear sensitive data
            if (currentKey != null) {
                // Secure wipe of key (if possible)
                currentKey = null;
            }
            
            // Clear security context
            securityContext = null;
            
            // Log session timeout
            auditManager.logSecurityEvent("SESSION_TIMEOUT", 
                "Session expired due to inactivity", 
                AuditManager.AuditSeverity.INFO, null, null);
            
            // Transition to locked state
            transitionToState(ApplicationState.LOCKED);
            
            // Show login interface
            showLoginInterface();
            
            // Notify user
            notificationManager.showWarning("Session Expired", 
                "Your session has expired. Please log in again.");
            
        } catch (Exception e) {
            errorHandler.handleError("Session timeout", e);
        }
    }
    
    /**
     * Transition to new application state
     */
    private void transitionToState(ApplicationState newState) {
        ApplicationState oldState = currentState;
        currentState = newState;
        
        System.out.println("🔄 State transition: " + oldState + " → " + newState);
        
        // Log state transition
        if (auditManager != null) {
            auditManager.logSecurityEvent("STATE_TRANSITION", 
                "Application state changed", 
                AuditManager.AuditSeverity.INFO, null, 
                "From: " + oldState + ", To: " + newState);
        }
        
        // Update UI based on new state
        updateUIForState(newState);
    }
    
    /**
     * Update UI for current state
     */
    private void updateUIForState(ApplicationState state) {
        Platform.runLater(() -> {
            try {
                switch (state) {
                    case FIRST_RUN_SETUP:
                        showFirstRunSetup();
                        break;
                    case LOGIN:
                    case LOCKED:
                        showLoginInterface();
                        break;
                    case MASTER_VAULT:
                    case DECOY_VAULT:
                        // UI already shown in login handlers
                        break;
                    case PANIC_MODE:
                        // Maintain normal appearance
                        break;
                    case SHUTTING_DOWN:
                        // No UI updates needed
                        break;
                }
            } catch (Exception e) {
                errorHandler.handleError("UI state update", e);
            }
        });
    }
    
    /**
     * Show first run setup interface
     */
    private void showFirstRunSetup() {
        try {
            Scene setupScene = uiManager.createFirstRunSetupScene(passwordManager);
            primaryStage.setScene(setupScene);
            primaryStage.setTitle("GhostVault - Initial Setup");
            primaryStage.show();
        } catch (Exception e) {
            errorHandler.handleError("First run setup UI", e);
        }
    }
    
    /**
     * Show login interface
     */
    private void showLoginInterface() {
        try {
            Scene loginScene = uiManager.createLoginScene();
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("GhostVault");
            primaryStage.show();
        } catch (Exception e) {
            errorHandler.handleError("Login UI", e);
        }
    }
    
    /**
     * Show vault interface
     */
    private void showVaultInterface(boolean isDecoyMode) {
        Platform.runLater(() -> {
            try {
                System.out.println("🚀 Creating vault scene (decoy: " + isDecoyMode + ")");
                
                Scene vaultScene;
                if (isDecoyMode) {
                    System.out.println("📁 Creating decoy vault scene");
                    vaultScene = uiManager.createDecoyVaultScene(decoyManager);
                } else {
                    System.out.println("🔒 Creating master vault scene");
                    vaultScene = uiManager.createMasterVaultScene(fileManager, metadataManager, 
                        backupManager, currentKey);
                }
                
                System.out.println("🎭 Setting vault scene on stage");
                primaryStage.setScene(vaultScene);
                primaryStage.setTitle("GhostVault - " + (isDecoyMode ? "Decoy" : "Secure") + " Vault");
                primaryStage.show();
                
                System.out.println("✅ Vault interface displayed successfully");
                
            } catch (Exception e) {
                System.err.println("❌ Error showing vault interface: " + e.getMessage());
                e.printStackTrace();
                errorHandler.handleError("Vault interface", e);
                
                // Show error to user and return to login
                Platform.runLater(() -> {
                    try {
                        Scene loginScene = uiManager.createLoginScene();
                        primaryStage.setScene(loginScene);
                        notificationManager.showError("Vault Error", 
                            "Failed to load vault interface: " + e.getMessage());
                    } catch (Exception ex) {
                        System.err.println("Failed to return to login: " + ex.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Show login error
     */
    private void showLoginError(String message) {
        notificationManager.showError("Login Error", message);
        
        // Also update login UI if available
        updateLoginUIStatus(message);
    }
    
    /**
     * Update login UI with status information
     */
    private void updateLoginUIStatus(String message) {
        // Update the login controller with security status
        if (uiManager != null) {
            try {
                // Get the current login controller and update it
                uiManager.updateLoginStatus(message, securityAttemptManager);
            } catch (Exception e) {
                System.err.println("Failed to update login UI: " + e.getMessage());
            }
        }
        System.out.println("🔒 Login Status: " + message);
    }
    
    /**
     * Perform application shutdown
     */
    public void shutdown() {
        try {
            transitionToState(ApplicationState.SHUTTING_DOWN);
            
            // Stop security monitoring
            if (threatDetectionEngine != null) {
                threatDetectionEngine.stopMonitoring();
            }
            
            // Deactivate security hardening
            if (advancedSecurityManager != null) {
                advancedSecurityManager.cleanup();
            }
            
            // End session
            if (sessionManager != null) {
                sessionManager.endSession();
            }
            
            // Clear sensitive data
            if (currentKey != null) {
                currentKey = null;
            }
            securityContext = null;
            
            // Cleanup system tray
            if (systemTrayManager != null) {
                systemTrayManager.cleanup();
            }
            
            // Shutdown security logging
            if (securityAttemptManager != null) {
                securityAttemptManager.shutdown();
            }
            
            // Shutdown background executor
            if (backgroundExecutor != null) {
                backgroundExecutor.shutdown();
            }
            
            // Log shutdown
            if (auditManager != null) {
                auditManager.logSecurityEvent("APPLICATION_SHUTDOWN", 
                    "GhostVault application shutdown", 
                    AuditManager.AuditSeverity.INFO, null, null);
            }
            
            // Close application
            Platform.exit();
            
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Get current application state
     */
    public ApplicationState getCurrentState() {
        return currentState;
    }
    
    /**
     * Get security context
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }
    
    /**
     * Get error handler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    /**
     * Get security attempt manager
     */
    public SecurityAttemptManager getSecurityAttemptManager() {
        return securityAttemptManager;
    }
    
    /**
     * Security context class
     */
    public static class SecurityContext {
        private final SecretKey encryptionKey;
        private final PasswordManager.PasswordType passwordType;
        private final long sessionStartTime;
        
        public SecurityContext(SecretKey encryptionKey, PasswordManager.PasswordType passwordType) {
            this.encryptionKey = encryptionKey;
            this.passwordType = passwordType;
            this.sessionStartTime = System.currentTimeMillis();
        }
        
        public SecretKey getEncryptionKey() { return encryptionKey; }
        public PasswordManager.PasswordType getPasswordType() { return passwordType; }
        public long getSessionStartTime() { return sessionStartTime; }
        
        public boolean isMasterMode() {
            return passwordType == PasswordManager.PasswordType.MASTER;
        }
        
        public boolean isDecoyMode() {
            return passwordType == PasswordManager.PasswordType.DECOY;
        }
    }
}