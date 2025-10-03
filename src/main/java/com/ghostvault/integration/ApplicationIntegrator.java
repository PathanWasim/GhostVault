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
    
    // UI components
    private UIManager uiManager;
    private NotificationManager notificationManager;
    private ErrorDialog errorDialog;
    
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
        
        // UI manager integration with notification manager
        uiManager.setNotificationManager(notificationManager);
        
        // Error handler integration
        // ErrorDialog is constructed per-use with ErrorHandlingResult; no global handler to set
        
        System.out.println("🔗 Component integrations configured");
    }
    
    /**
     * Determine initial application state
     */
    private void determineInitialState() throws Exception {
        if (!passwordManager.arePasswordsConfigured()) {
            transitionToState(ApplicationState.FIRST_RUN_SETUP);
        } else {
            transitionToState(ApplicationState.LOGIN);
        }
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
            
            // Show vault interface
            showVaultInterface(false); // false = not decoy mode
            
        } catch (Exception e) {
            errorHandler.handleError("Master login", e);
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
            
            // Show decoy vault interface
            showVaultInterface(true); // true = decoy mode
            
        } catch (Exception e) {
            errorHandler.handleError("Decoy login", e);
        }
    }
    
    /**
     * Handle invalid password
     */
    private void handleInvalidPassword() {
        // Record failed attempt
        sessionManager.recordFailedLogin("user");
        
        // Log failed login
        auditManager.logSecurityEvent("LOGIN_FAILED", 
            "Invalid password attempt", 
            AuditManager.AuditSeverity.WARNING, null, null);
        
        // Show error message
        showLoginError("Invalid password. Please try again.");
        
        // Check for too many failed attempts
        if (sessionManager.isAccountLocked("user")) {
            showLoginError("Too many failed attempts. Please wait before trying again.");
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