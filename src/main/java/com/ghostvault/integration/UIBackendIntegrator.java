package com.ghostvault.integration;

import com.ghostvault.core.*;
import com.ghostvault.security.*;
import com.ghostvault.backup.*;
import com.ghostvault.audit.*;
import com.ghostvault.ui.controllers.MainApplicationController;
import com.ghostvault.ui.components.*;
import com.ghostvault.model.VaultFile;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Complete UI-Backend Integration System
 * Connects the modern UI with all existing GhostVault backend services
 */
public class UIBackendIntegrator {
    
    // Backend Services
    private FileManager fileManager;
    private CryptoManager cryptoManager;
    private SessionManager sessionManager;
    private VaultBackupManager backupManager;
    private AuditManager auditManager;
    private SecurityMonitor securityMonitor;
    private PanicModeExecutor panicModeExecutor;
    private DecoyManager decoyManager;
    
    // UI Components
    private MainApplicationController uiController;
    private Stage primaryStage;
    
    // Integration State
    private boolean isInitialized = false;
    private String currentVaultPath;
    
    public UIBackendIntegrator(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeBackendServices();
    }
    
    /**
     * Initialize all backend services
     */
    private void initializeBackendServices() {
        try {
            // Core services
            fileManager = new FileManager();
            cryptoManager = new CryptoManager();
            sessionManager = new SessionManager();
            
            // Security services
            securityMonitor = new SecurityMonitor();
            panicModeExecutor = new PanicModeExecutor();
            decoyManager = new DecoyManager();
            
            // Backup and audit services
            backupManager = new VaultBackupManager();
            auditManager = new AuditManager();
            
            // Initialize UI controller
            uiController = new MainApplicationController(primaryStage);
            
            // Setup integration hooks
            setupIntegrationHooks();
            
            isInitialized = true;
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to initialize backend services", e, 
                ErrorHandlingSystem.ErrorSeverity.CRITICAL);
        }
    }
    
    /**
     * Setup integration hooks between UI and backend
     */
    private void setupIntegrationHooks() {
        // File operations integration
        setupFileOperationsIntegration();
        
        // Security integration
        setupSecurityIntegration();
        
        // Backup integration
        setupBackupIntegration();
        
        // Audit integration
        setupAuditIntegration();
        
        // Mode switching integration
        setupModeIntegration();
    }
    
    /**
     * Integrate file operations with backend FileManager
     */
    private void setupFileOperationsIntegration() {
        // Override UI file operations to use backend services
        ModernFileOperations fileOps = new ModernFileOperations(primaryStage) {
            @Override
            public void uploadFiles(List<File> files, File targetDirectory, 
                    Consumer<UploadResult> onComplete) {
                
                CompletableFuture.runAsync(() -> {
                    try {
                        int successCount = 0;
                        int failureCount = 0;
                        StringBuilder errors = new StringBuilder();
                        
                        for (File file : files) {
                            try {
                                // Use backend FileManager for actual upload
                                VaultFile vaultFile = fileManager.addFile(file, targetDirectory.getPath());
                                
                                // Encrypt file if needed
                                if (sessionManager.getCurrentMode() == SessionManager.VaultMode.MASTER) {
                                    cryptoManager.encryptFile(vaultFile);
                                }
                                
                                // Log audit entry
                                auditManager.logFileOperation("UPLOAD", file.getName(), 
                                    sessionManager.getCurrentUser());
                                
                                successCount++;
                                
                            } catch (Exception e) {
                                failureCount++;
                                errors.append("Failed to upload ").append(file.getName())
                                      .append(": ").append(e.getMessage()).append("\\n");
                            }
                        }
                        
                        Platform.runLater(() -> {
                            onComplete.accept(new UploadResult(successCount, failureCount, errors.toString()));
                        });
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            onComplete.accept(new UploadResult(0, files.size(), e.getMessage()));
                        });
                    }
                });
            }
            
            @Override
            public void downloadFile(File sourceFile, File targetFile, Consumer<Boolean> onComplete) {
                CompletableFuture.runAsync(() -> {
                    try {
                        // Use backend FileManager for download
                        VaultFile vaultFile = fileManager.getFile(sourceFile.getPath());
                        
                        // Decrypt if needed
                        if (vaultFile.isEncrypted()) {
                            cryptoManager.decryptFile(vaultFile);
                        }
                        
                        // Copy to target location
                        fileManager.exportFile(vaultFile, targetFile);
                        
                        // Log audit entry
                        auditManager.logFileOperation("DOWNLOAD", sourceFile.getName(), 
                            sessionManager.getCurrentUser());
                        
                        Platform.runLater(() -> onComplete.accept(true));
                        
                    } catch (Exception e) {
                        ErrorHandlingSystem.handleError("Download failed", e, 
                            ErrorHandlingSystem.ErrorSeverity.WARNING);
                        Platform.runLater(() -> onComplete.accept(false));
                    }
                });
            }
        };
        
        // Integrate with UI controller
        // This would require modifying the UI controller to accept the integrated file operations
    }
    
    /**
     * Integrate security features with backend security services
     */
    private void setupSecurityIntegration() {
        // Authentication integration
        uiController.getAuthenticationController().setAuthenticationProvider(
            (password) -> {
                try {
                    // Use backend session manager for authentication
                    SessionManager.VaultMode mode = sessionManager.authenticate(password);
                    
                    // Start security monitoring
                    securityMonitor.startMonitoring();
                    
                    // Log audit entry
                    auditManager.logSecurityEvent("LOGIN", "User authenticated in " + mode + " mode");
                    
                    // Convert to UI mode enum
                    return convertToUIMode(mode);
                    
                } catch (Exception e) {
                    auditManager.logSecurityEvent("LOGIN_FAILED", "Authentication failed: " + e.getMessage());
                    throw e;
                }
            }
        );
        
        // Security monitoring integration
        securityMonitor.setOnThreatDetected((threat) -> {
            Platform.runLater(() -> {
                NotificationSystem.showError("Security Alert", 
                    "Threat detected: " + threat.getDescription());
                
                // Offer panic mode activation
                if (threat.getSeverity() == ThreatLevel.CRITICAL) {
                    showPanicModeDialog();
                }
            });
        });
    }
    
    /**
     * Integrate backup operations with backend backup manager
     */
    private void setupBackupIntegration() {
        EncryptedBackupManager uiBackupManager = new EncryptedBackupManager(primaryStage) {
            @Override
            public void createBackup(File vaultDirectory, String password, Consumer<BackupResult> onComplete) {
                CompletableFuture.runAsync(() -> {
                    try {
                        // Use backend backup manager
                        BackupOptions options = new BackupOptions();
                        options.setEncryptionPassword(password);
                        options.setCompressionEnabled(true);
                        options.setVerificationEnabled(true);
                        
                        com.ghostvault.core.BackupResult result = backupManager.createBackup(
                            vaultDirectory.getPath(), options);
                        
                        // Log audit entry
                        auditManager.logSystemOperation("BACKUP_CREATED", 
                            "Backup created: " + result.getBackupFile());
                        
                        Platform.runLater(() -> {
                            onComplete.accept(new BackupResult(result.isSuccess(), result.getMessage()));
                        });
                        
                    } catch (Exception e) {
                        auditManager.logSystemOperation("BACKUP_FAILED", "Backup failed: " + e.getMessage());
                        Platform.runLater(() -> {
                            onComplete.accept(new BackupResult(false, e.getMessage()));
                        });
                    }
                });
            }
            
            @Override
            public void restoreBackup(File backupFile, String password, File targetDirectory, 
                    Consumer<RestoreResult> onComplete) {
                CompletableFuture.runAsync(() -> {
                    try {
                        // Use backend backup manager
                        RestoreOptions options = new RestoreOptions();
                        options.setDecryptionPassword(password);
                        options.setVerificationEnabled(true);
                        
                        com.ghostvault.core.RestoreResult result = backupManager.restoreBackup(
                            backupFile.getPath(), targetDirectory.getPath(), options);
                        
                        // Log audit entry
                        auditManager.logSystemOperation("BACKUP_RESTORED", 
                            "Backup restored from: " + backupFile.getName());
                        
                        Platform.runLater(() -> {
                            onComplete.accept(new RestoreResult(result.isSuccess(), result.getMessage()));
                        });
                        
                    } catch (Exception e) {
                        auditManager.logSystemOperation("RESTORE_FAILED", "Restore failed: " + e.getMessage());
                        Platform.runLater(() -> {
                            onComplete.accept(new RestoreResult(false, e.getMessage()));
                        });
                    }
                });
            }
        };
    }
    
    /**
     * Integrate audit logging with UI operations
     */
    private void setupAuditIntegration() {
        // Hook into UI operations for audit logging
        ErrorHandlingSystem.getInstance().setOnErrorLogged((error) -> {
            auditManager.logSystemEvent("ERROR", error.getMessage(), error.getSeverity().toString());
        });
        
        // Log UI events
        NotificationSystem.getInstance().setOnNotificationShown((notification) -> {
            auditManager.logUserActivity("NOTIFICATION", notification.getTitle() + ": " + notification.getMessage());
        });
    }
    
    /**
     * Integrate mode switching with backend services
     */
    private void setupModeIntegration() {
        // Override mode switching to use backend services
        uiController.setModeChangeHandler((newMode) -> {
            try {
                SessionManager.VaultMode backendMode = convertToBackendMode(newMode);
                
                // Switch mode in backend
                sessionManager.switchMode(backendMode);
                
                // Handle mode-specific operations
                switch (backendMode) {
                    case PANIC:
                        // Activate panic mode
                        panicModeExecutor.activatePanicMode();
                        auditManager.logSecurityEvent("PANIC_MODE_ACTIVATED", "Emergency mode activated");
                        break;
                        
                    case DECOY:
                        // Activate decoy mode
                        decoyManager.activateDecoyMode();
                        auditManager.logSecurityEvent("DECOY_MODE_ACTIVATED", "Decoy mode activated");
                        break;
                        
                    case MASTER:
                        // Activate master mode
                        auditManager.logSecurityEvent("MASTER_MODE_ACTIVATED", "Master mode activated");
                        break;
                }
                
                return true;
                
            } catch (Exception e) {
                ErrorHandlingSystem.handleError("Mode switch failed", e, 
                    ErrorHandlingSystem.ErrorSeverity.CRITICAL);
                return false;
            }
        });
    }
    
    /**
     * Start the integrated application
     */
    public void startApplication() {
        if (!isInitialized) {
            throw new IllegalStateException("Integration not initialized");
        }
        
        try {
            // Initialize UI with backend integration
            uiController.createScene();
            
            // Apply theme
            ModernThemeManager.initialize();
            ModernThemeManager.applyTheme(uiController.getMainScene());
            
            // Setup window
            primaryStage.setTitle("GhostVault - Secure File Manager");
            primaryStage.setScene(uiController.getMainScene());
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            
            // Center window
            com.ghostvault.ui.utils.UIUtils.centerStage(primaryStage);
            
            // Setup shutdown handler
            primaryStage.setOnCloseRequest(event -> {
                shutdown();
            });
            
            // Show application
            primaryStage.show();
            
            // Initialize notification system
            NotificationSystem.getInstance().initialize(primaryStage);
            
            // Show startup notification
            NotificationSystem.showSuccess("GhostVault Started", 
                "All systems initialized and ready");
            
            // Log startup
            auditManager.logSystemEvent("APPLICATION_STARTED", "GhostVault application started successfully");
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to start application", e, 
                ErrorHandlingSystem.ErrorSeverity.CRITICAL);
            throw new RuntimeException("Application startup failed", e);
        }
    }
    
    /**
     * Shutdown the integrated application
     */
    public void shutdown() {
        try {
            // Log shutdown
            auditManager.logSystemEvent("APPLICATION_SHUTDOWN", "GhostVault application shutting down");
            
            // Stop security monitoring
            if (securityMonitor != null) {
                securityMonitor.stopMonitoring();
            }
            
            // Clear session
            if (sessionManager != null) {
                sessionManager.clearSession();
            }
            
            // Cleanup UI
            if (uiController != null) {
                uiController.shutdown();
            }
            
            // Force garbage collection
            System.gc();
            
            Platform.exit();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Shutdown error", e, 
                ErrorHandlingSystem.ErrorSeverity.WARNING);
            System.exit(1);
        }
    }
    
    /**
     * Emergency shutdown with panic mode activation
     */
    public void emergencyShutdown() {
        try {
            auditManager.logSecurityEvent("EMERGENCY_SHUTDOWN", "Emergency shutdown initiated");
            
            // Activate panic mode
            panicModeExecutor.activatePanicMode();
            
            // Force shutdown
            System.exit(0);
            
        } catch (Exception e) {
            // Force exit even if panic mode fails
            System.exit(1);
        }
    }
    
    /**
     * Show panic mode activation dialog
     */
    private void showPanicModeDialog() {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Security Threat Detected");
            alert.setHeaderText("Critical security threat detected!");
            alert.setContentText("A critical security threat has been detected. " +
                "Do you want to activate emergency data destruction?");
            
            javafx.scene.control.ButtonType panicButton = new javafx.scene.control.ButtonType(
                "Activate Panic Mode", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType continueButton = new javafx.scene.control.ButtonType(
                "Continue", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(panicButton, continueButton);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == panicButton) {
                    uiController.switchMode(com.ghostvault.ui.controllers.ModeController.VaultMode.PANIC);
                }
            });
        });
    }
    
    // Utility methods for mode conversion
    private com.ghostvault.ui.controllers.ModeController.VaultMode convertToUIMode(SessionManager.VaultMode backendMode) {
        switch (backendMode) {
            case MASTER: return com.ghostvault.ui.controllers.ModeController.VaultMode.MASTER;
            case PANIC: return com.ghostvault.ui.controllers.ModeController.VaultMode.PANIC;
            case DECOY: return com.ghostvault.ui.controllers.ModeController.VaultMode.DECOY;
            default: return com.ghostvault.ui.controllers.ModeController.VaultMode.MASTER;
        }
    }
    
    private SessionManager.VaultMode convertToBackendMode(com.ghostvault.ui.controllers.ModeController.VaultMode uiMode) {
        switch (uiMode) {
            case MASTER: return SessionManager.VaultMode.MASTER;
            case PANIC: return SessionManager.VaultMode.PANIC;
            case DECOY: return SessionManager.VaultMode.DECOY;
            default: return SessionManager.VaultMode.MASTER;
        }
    }
    
    // Getters
    public MainApplicationController getUIController() { return uiController; }
    public FileManager getFileManager() { return fileManager; }
    public CryptoManager getCryptoManager() { return cryptoManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public AuditManager getAuditManager() { return auditManager; }
    public boolean isInitialized() { return isInitialized; }
}