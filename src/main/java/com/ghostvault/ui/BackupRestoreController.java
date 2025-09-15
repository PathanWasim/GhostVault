package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.exception.BackupException;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.audit.AuditManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Controller for backup and restore operations UI
 */
public class BackupRestoreController {
    
    @FXML private Button createBackupButton;
    @FXML private Button restoreBackupButton;
    @FXML private Button verifyBackupButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private TextArea backupInfoArea;
    
    private VaultBackupManager backupManager;
    private SecretKey currentKey;
    private Stage stage;
    
    /**
     * Initialize the controller
     */
    public void initialize() {
        // Initialize UI components
        progressBar.setVisible(false);
        statusLabel.setText("Ready");
        backupInfoArea.setEditable(false);
        
        // Set button actions
        createBackupButton.setOnAction(e -> createBackup());
        restoreBackupButton.setOnAction(e -> restoreBackup());
        verifyBackupButton.setOnAction(e -> verifyBackup());
    }
    
    /**
     * Set the backup manager and encryption key
     */
    public void setBackupManager(VaultBackupManager backupManager, SecretKey key) {
        this.backupManager = backupManager;
        this.currentKey = key;
    }
    
    /**
     * Set the stage for file dialogs
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Create a new backup
     */
    private void createBackup() {
        if (backupManager == null || currentKey == null) {
            showError("Backup manager not initialized");
            return;
        }
        
        // Show file chooser for backup location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup As");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvbackup")
        );
        
        // Set default filename with timestamp
        String defaultName = "vault_backup_" + 
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".gvbackup";
        fileChooser.setInitialFileName(defaultName);
        
        File backupFile = fileChooser.showSaveDialog(stage);
        if (backupFile == null) {
            return; // User cancelled
        }
        
        // Create backup task
        Task<Void> backupTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                backupManager.createBackup(backupFile, currentKey, (percentage, message) -> {
                    Platform.runLater(() -> {
                        progressBar.setProgress(percentage / 100.0);
                        statusLabel.setText(message);
                    });
                });
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Backup created successfully");
                    backupInfoArea.setText("Backup saved to: " + backupFile.getAbsolutePath());
                    showSuccess("Backup created successfully!");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Backup failed");
                    Throwable exception = getException();
                    String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                    backupInfoArea.setText("Backup failed: " + errorMessage);
                    showError("Backup failed: " + errorMessage);
                });
            }
        };
        
        // Show progress and start task
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        statusLabel.setText("Starting backup...");
        
        Thread backupThread = new Thread(backupTask);
        backupThread.setDaemon(true);
        backupThread.start();
    }
    
    /**
     * Restore from backup
     */
    private void restoreBackup() {
        if (backupManager == null || currentKey == null) {
            showError("Backup manager not initialized");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Restore");
        confirmAlert.setHeaderText("Restore Vault from Backup");
        confirmAlert.setContentText("This will replace your current vault with the backup data. " +
                                   "Your current vault will be backed up first. Continue?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        // Show file chooser for backup file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvbackup")
        );
        
        File backupFile = fileChooser.showOpenDialog(stage);
        if (backupFile == null) {
            return; // User cancelled
        }
        
        // Create restore task
        Task<Void> restoreTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                backupManager.restoreBackup(backupFile, currentKey, (percentage, message) -> {
                    Platform.runLater(() -> {
                        progressBar.setProgress(percentage / 100.0);
                        statusLabel.setText(message);
                    });
                });
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Restore completed successfully");
                    backupInfoArea.setText("Vault restored from: " + backupFile.getAbsolutePath());
                    showSuccess("Vault restored successfully! Please restart the application.");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Restore failed");
                    Throwable exception = getException();
                    String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                    backupInfoArea.setText("Restore failed: " + errorMessage);
                    showError("Restore failed: " + errorMessage);
                });
            }
        };
        
        // Show progress and start task
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        statusLabel.setText("Starting restore...");
        
        Thread restoreThread = new Thread(restoreTask);
        restoreThread.setDaemon(true);
        restoreThread.start();
    }
    
    /**
     * Verify backup integrity
     */
    private void verifyBackup() {
        if (backupManager == null || currentKey == null) {
            showError("Backup manager not initialized");
            return;
        }
        
        // Show file chooser for backup file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File to Verify");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvbackup")
        );
        
        File backupFile = fileChooser.showOpenDialog(stage);
        if (backupFile == null) {
            return; // User cancelled
        }
        
        // Create verification task
        Task<VaultBackupManager.BackupInfo> verifyTask = new Task<VaultBackupManager.BackupInfo>() {
            @Override
            protected VaultBackupManager.BackupInfo call() throws Exception {
                Platform.runLater(() -> {
                    statusLabel.setText("Verifying backup...");
                    progressBar.setVisible(true);
                    progressBar.setProgress(-1); // Indeterminate progress
                });
                
                return backupManager.verifyBackup(backupFile, currentKey);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    VaultBackupManager.BackupInfo info = getValue();
                    
                    if (info.isValid()) {
                        statusLabel.setText("Backup verification successful");
                        
                        StringBuilder infoText = new StringBuilder();
                        infoText.append("Backup File: ").append(backupFile.getName()).append("\n");
                        infoText.append("Status: Valid\n");
                        infoText.append("Version: ").append(info.getVersion()).append("\n");
                        infoText.append("Created: ").append(
                            info.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        ).append("\n");
                        infoText.append("File Count: ").append(info.getFileCount()).append("\n");
                        infoText.append("Total Size: ").append(formatFileSize(info.getTotalSize())).append("\n");
                        
                        backupInfoArea.setText(infoText.toString());
                        showSuccess("Backup verification successful!");
                        
                    } else {
                        statusLabel.setText("Backup verification failed");
                        backupInfoArea.setText("Backup verification failed: " + info.getErrorMessage());
                        showError("Backup verification failed: " + info.getErrorMessage());
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Verification failed");
                    Throwable exception = getException();
                    String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                    backupInfoArea.setText("Verification failed: " + errorMessage);
                    showError("Verification failed: " + errorMessage);
                });
            }
        };
        
        Thread verifyThread = new Thread(verifyTask);
        verifyThread.setDaemon(true);
        verifyThread.start();
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}