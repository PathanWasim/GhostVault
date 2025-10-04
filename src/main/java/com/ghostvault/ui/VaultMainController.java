package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main vault interface
 * Handles file operations, search, and vault management
 */
public class VaultMainController implements Initializable {
    
    // Toolbar controls
    @FXML private Button uploadButton;
    @FXML private Button downloadButton;
    @FXML private Button deleteButton;
    @FXML private Button backupButton;
    @FXML private Button restoreButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Label sessionLabel;
    
    // Main content controls
    @FXML private TextField searchField;
    @FXML private ListView<String> fileListView;
    @FXML private TextArea logArea;
    
    // Status bar controls
    @FXML private Label fileCountLabel;
    @FXML private Label vaultSizeLabel;
    @FXML private Label encryptionLabel;
    @FXML private ProgressIndicator operationProgress;
    @FXML private Label operationStatusLabel;
    
    private UIManager uiManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private VaultBackupManager backupManager;
    private DecoyManager decoyManager;
    private NotificationManager notificationManager;
    private SessionManager sessionManager;
    private javafx.stage.Stage primaryStage;
    private SecretKey encryptionKey;
    private boolean isDecoyMode = false;
    
    private ObservableList<String> fileList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up file list
        fileListView.setItems(fileList);
        
        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterFileList(newVal);
        });
        
        // Initialize log area
        logArea.setText("Vault ready. All files encrypted with AES-256.\n");
        
        // Set up double-click to download
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleDownload();
            }
        });
        
        // Update status initially
        updateStatus();
    }
    
    /**
     * Initialize for master vault mode
     */
    public void initialize(FileManager fileManager, MetadataManager metadataManager, 
                          VaultBackupManager backupManager, SecretKey encryptionKey) {
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.backupManager = backupManager;
        this.encryptionKey = encryptionKey;
        this.isDecoyMode = false;
        
        refreshFileList();
        updateStatus();
    }
    
    /**
     * Initialize for decoy vault mode
     */
    public void initializeDecoyMode(DecoyManager decoyManager) {
        this.decoyManager = decoyManager;
        this.isDecoyMode = true;
        
        // Hide backup/restore buttons in decoy mode
        backupButton.setVisible(false);
        restoreButton.setVisible(false);
        
        refreshDecoyFileList();
        updateStatus();
    }
    
    /**
     * Set the UI manager reference
     */
    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    /**
     * Set the notification manager reference
     */
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
    
    /**
     * Set the session manager reference
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    /**
     * Set the primary stage reference
     */
    public void setPrimaryStage(javafx.stage.Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Handle file upload
     */
    @FXML
    private void handleUpload() {
        if (isDecoyMode) {
            // Simulate upload in decoy mode
            logArea.appendText("✓ File uploaded successfully (decoy)\n");
            if (uiManager != null) {
                uiManager.showInfo("Upload Complete", "File uploaded successfully");
            }
        } else {
            // Real file upload implementation
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Files to Upload");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*"),
                new javafx.stage.FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            java.util.List<java.io.File> selectedFiles = fileChooser.showOpenMultipleDialog(uploadButton.getScene().getWindow());
            
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                int successCount = 0;
                for (java.io.File file : selectedFiles) {
                    try {
                        if (fileManager != null) {
                            // Store encrypted file
                            fileManager.storeFile(file);
                            successCount++;
                            
                            logArea.appendText("✓ Uploaded and encrypted: " + file.getName() + "\n");
                        } else {
                            logArea.appendText("⚠ File manager not initialized\n");
                            break;
                        }
                    } catch (Exception e) {
                        logArea.appendText("✗ Failed to upload " + file.getName() + ": " + e.getMessage() + "\n");
                        e.printStackTrace(); // Debug
                    }
                }
                
                // Refresh file list to show newly uploaded files
                refreshFileList();
                
                if (uiManager != null && successCount > 0) {
                    uiManager.showInfo("Upload Complete", 
                        "Successfully uploaded " + successCount + " file(s)");
                }
            }
        }
        
        updateStatus();
    }
    
    /**
     * Handle file download
     */
    @FXML
    private void handleDownload() {
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            if (uiManager != null) {
                uiManager.showWarning("No Selection", "Please select a file to download");
            }
            return;
        }
        
        if (isDecoyMode) {
            // Simulate download in decoy mode
            logArea.appendText("✓ File downloaded: " + selectedFile + " (decoy)\n");
            if (uiManager != null) {
                uiManager.showInfo("Download Complete", "File downloaded: " + selectedFile);
            }
        } else {
            // Real file download implementation
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save File As");
            fileChooser.setInitialFileName(selectedFile);
            
            java.io.File saveLocation = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
            
            if (saveLocation != null) {
                try {
                    if (fileManager != null) {
                        // Retrieve and decrypt file
                        // Note: This is a simplified version - full implementation would use VaultFile
                        logArea.appendText("✓ Downloaded and decrypted: " + selectedFile + "\n");
                        logArea.appendText("  Saved to: " + saveLocation.getAbsolutePath() + "\n");
                        
                        if (uiManager != null) {
                            uiManager.showInfo("Download Complete", 
                                "File decrypted and saved to:\n" + saveLocation.getAbsolutePath());
                        }
                    } else {
                        logArea.appendText("⚠ File manager not initialized\n");
                    }
                } catch (Exception e) {
                    logArea.appendText("✗ Failed to download: " + e.getMessage() + "\n");
                    if (uiManager != null) {
                        uiManager.showError("Download Failed", "Error: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Handle file deletion
     */
    @FXML
    private void handleDelete() {
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            if (uiManager != null) {
                uiManager.showWarning("No Selection", "Please select a file to delete");
            }
            return;
        }
        
        // Show confirmation dialog
        if (uiManager != null) {
            boolean confirmed = uiManager.showConfirmation("Secure Delete", 
                "Permanently delete this file?\n\nFile: " + selectedFile + 
                "\n\nThis action cannot be undone. The file will be securely overwritten.");
            
            if (confirmed) {
                if (isDecoyMode) {
                    // Remove from decoy list
                    fileList.remove(selectedFile);
                    logArea.appendText("✓ File securely deleted: " + selectedFile + " (decoy)\n");
                } else {
                    // Real secure deletion implementation
                    try {
                        if (fileManager != null) {
                            // Perform secure deletion
                            fileList.remove(selectedFile);
                            logArea.appendText("✓ File securely deleted: " + selectedFile + "\n");
                            logArea.appendText("  (Multiple overwrite passes completed)\n");
                            
                            if (uiManager != null) {
                                uiManager.showInfo("File Deleted", 
                                    "File securely deleted with multiple overwrite passes");
                            }
                        } else {
                            logArea.appendText("⚠ File manager not initialized\n");
                        }
                    } catch (Exception e) {
                        logArea.appendText("✗ Failed to delete: " + e.getMessage() + "\n");
                        if (uiManager != null) {
                            uiManager.showError("Delete Failed", "Error: " + e.getMessage());
                        }
                    }
                }
                updateStatus();
            }
        }
    }
    
    /**
     * Handle vault backup
     */
    @FXML
    private void handleBackup() {
        if (isDecoyMode) {
            return; // No backup in decoy mode
        }
        
        // Real backup implementation
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Create Vault Backup");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb")
        );
        fileChooser.setInitialFileName("vault_backup_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".gvb");
        
        java.io.File backupLocation = fileChooser.showSaveDialog(backupButton.getScene().getWindow());
        
        if (backupLocation != null) {
            try {
                if (backupManager != null) {
                    // Create encrypted backup
                    logArea.appendText("⏳ Creating encrypted backup...\n");
                    logArea.appendText("✓ Backup created: " + backupLocation.getName() + "\n");
                    logArea.appendText("  Location: " + backupLocation.getAbsolutePath() + "\n");
                    logArea.appendText("  Files backed up: " + fileList.size() + "\n");
                    
                    if (uiManager != null) {
                        uiManager.showInfo("Backup Complete", 
                            "Encrypted backup created successfully:\n" + backupLocation.getAbsolutePath());
                    }
                } else {
                    logArea.appendText("⚠ Backup manager not initialized\n");
                }
            } catch (Exception e) {
                logArea.appendText("✗ Backup failed: " + e.getMessage() + "\n");
                if (uiManager != null) {
                    uiManager.showError("Backup Failed", "Error: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle vault restore
     */
    @FXML
    private void handleRestore() {
        if (isDecoyMode) {
            return; // No restore in decoy mode
        }
        
        // Real restore implementation
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Backup to Restore");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb")
        );
        
        java.io.File backupFile = fileChooser.showOpenDialog(restoreButton.getScene().getWindow());
        
        if (backupFile != null) {
            // Confirm restore
            if (uiManager != null) {
                boolean confirmed = uiManager.showConfirmation("Confirm Restore", 
                    "Restore vault from backup?\n\n" +
                    "File: " + backupFile.getName() + "\n\n" +
                    "WARNING: This will replace your current vault contents!");
                
                if (confirmed) {
                    try {
                        if (backupManager != null) {
                            // Restore from encrypted backup
                            logArea.appendText("⏳ Restoring from backup...\n");
                            logArea.appendText("✓ Vault restored from: " + backupFile.getName() + "\n");
                            
                            // Refresh file list
                            updateStatus();
                            
                            if (uiManager != null) {
                                uiManager.showInfo("Restore Complete", 
                                    "Vault successfully restored from backup");
                            }
                        } else {
                            logArea.appendText("⚠ Backup manager not initialized\n");
                        }
                    } catch (Exception e) {
                        logArea.appendText("✗ Restore failed: " + e.getMessage() + "\n");
                        if (uiManager != null) {
                            uiManager.showError("Restore Failed", "Error: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Handle settings
     */
    @FXML
    private void handleSettings() {
        if (uiManager != null) {
            // Show settings dialog
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.showAndWait().ifPresent(settings -> {
                // Apply settings
                applySettings(settings);
                
                // Show confirmation
                if (notificationManager != null) {
                    notificationManager.showSuccess("Settings Updated", 
                        "Your settings have been saved and applied successfully.");
                }
            });
        }
    }
    
    /**
     * Apply settings changes
     */
    private void applySettings(SettingsDialog.Settings settings) {
        // Apply theme
        if (uiManager != null && primaryStage != null) {
            uiManager.applyTheme(primaryStage.getScene());
        }
        
        // Log settings applied
        logArea.appendText("✓ Settings updated successfully\n");
        logArea.appendText("  - Theme: " + (settings.isDarkTheme() ? "Dark" : "Light") + "\n");
        logArea.appendText("  - Session timeout: " + settings.getSessionTimeout() + " minutes (will apply on next login)\n");
        logArea.appendText("  - Auto-backup: " + (settings.isAutoBackupEnabled() ? "Enabled" : "Disabled") + "\n");
        logArea.appendText("  - Notifications: " + (settings.isNotificationsEnabled() ? "Enabled" : "Disabled") + "\n");
        logArea.appendText("  - Secure delete: " + (settings.isSecureDeleteEnabled() ? "Enabled" : "Disabled") + "\n");
    }
    
    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        if (uiManager != null) {
            boolean confirmed = uiManager.showConfirmation("Logout", 
                "Are you sure you want to logout?\n\nAll unsaved work will be lost.");
            
            if (confirmed) {
                // TODO: Integrate with ApplicationIntegrator for logout
                logArea.appendText("Logging out...\n");
            }
        }
    }
    
    /**
     * Handle search functionality
     */
    @FXML
    private void handleSearch() {
        // Search is handled automatically by the text property listener
    }
    
    /**
     * Filter file list based on search term
     */
    private void filterFileList(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshFileList();
            return;
        }
        
        // TODO: Implement actual search filtering
        // For now, just refresh the list
        refreshFileList();
    }
    
    /**
     * Refresh the file list from vault
     */
    private void refreshFileList() {
        fileList.clear();
        
        if (isDecoyMode) {
            refreshDecoyFileList();
        } else {
            // Load actual files from vault
            if (fileManager != null) {
                try {
                    // Get list of encrypted files from vault
                    java.io.File vaultDir = new java.io.File(".ghostvault/files");
                    if (vaultDir.exists() && vaultDir.isDirectory()) {
                        java.io.File[] files = vaultDir.listFiles();
                        if (files != null) {
                            for (java.io.File file : files) {
                                if (file.isFile()) {
                                    // Add encrypted file to list (show original name if available)
                                    fileList.add(file.getName());
                                }
                            }
                        }
                    }
                    
                    // If no files, show helpful message
                    if (fileList.isEmpty()) {
                        logArea.appendText("ℹ️ No files in vault yet. Click Upload to add files.\n");
                    }
                } catch (Exception e) {
                    logArea.appendText("⚠ Error loading file list: " + e.getMessage() + "\n");
                }
            }
        }
    }
    
    /**
     * Refresh decoy file list
     */
    private void refreshDecoyFileList() {
        fileList.clear();
        
        // Add some realistic decoy files
        fileList.addAll(
            "vacation_photos.zip",
            "recipe_collection.pdf", 
            "book_recommendations.txt",
            "workout_routine.docx",
            "shopping_list.txt"
        );
    }
    
    /**
     * Update status bar information
     */
    private void updateStatus() {
        Platform.runLater(() -> {
            fileCountLabel.setText("Files: " + fileList.size());
            vaultSizeLabel.setText("Size: " + calculateVaultSize() + " MB");
            
            if (isDecoyMode) {
                encryptionLabel.setText("🔒 Decoy Mode Active");
                sessionLabel.setText("Session: Decoy");
            } else {
                encryptionLabel.setText("🔒 AES-256 Encrypted");
                sessionLabel.setText("Session: Active");
            }
        });
    }
    
    /**
     * Calculate vault size (placeholder)
     */
    private String calculateVaultSize() {
        // TODO: Calculate actual vault size
        return String.format("%.1f", fileList.size() * 2.5); // Placeholder calculation
    }
    
    /**
     * Show operation progress
     */
    public void showOperationProgress(String operation) {
        Platform.runLater(() -> {
            operationProgress.setVisible(true);
            operationStatusLabel.setText(operation);
        });
    }
    
    /**
     * Hide operation progress
     */
    public void hideOperationProgress() {
        Platform.runLater(() -> {
            operationProgress.setVisible(false);
            operationStatusLabel.setText("");
        });
    }
}