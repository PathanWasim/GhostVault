package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.decoy.DecoyManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.crypto.SecretKey;

/**
 * Controller for the main vault interface
 * Handles both master vault and decoy vault modes
 */
public class VaultMainController {
    
    @FXML private ToolBar mainToolbar;
    @FXML private TextField searchField;
    @FXML private ListView<String> fileListView;
    @FXML private TextArea activityLog;
    @FXML private VBox mainContainer;
    
    // Components
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private VaultBackupManager backupManager;
    private DecoyManager decoyManager;
    private SecretKey encryptionKey;
    private UIManager uiManager;
    
    // State
    private boolean isDecoyMode = false;
    
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
        
        setupUI();
        refreshFileList();
    }
    
    /**
     * Initialize for decoy vault mode
     */
    public void initializeDecoyMode(DecoyManager decoyManager) {
        this.decoyManager = decoyManager;
        this.isDecoyMode = true;
        
        setupUI();
        refreshDecoyFileList();
    }
    
    /**
     * Set UI manager reference
     */
    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    /**
     * Set up UI components
     */
    private void setupUI() {
        // Set up search functionality
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (isDecoyMode) {
                    filterDecoyFiles(newVal);
                } else {
                    filterFiles(newVal);
                }
            });
        }
        
        // Set up file list
        if (fileListView != null) {
            fileListView.setCellFactory(listView -> new FileListCell());
        }
        
        // Initialize activity log
        if (activityLog != null) {
            activityLog.setEditable(false);
            logActivity("Vault interface initialized");
        }
    }
    
    /**
     * Refresh file list for master vault
     */
    private void refreshFileList() {
        if (fileManager != null && fileListView != null) {
            try {
                fileListView.getItems().clear();
                fileListView.getItems().addAll(fileManager.listFiles());
            } catch (Exception e) {
                logActivity("Error refreshing file list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Refresh file list for decoy vault
     */
    private void refreshDecoyFileList() {
        if (decoyManager != null && fileListView != null) {
            try {
                fileListView.getItems().clear();
                fileListView.getItems().addAll(decoyManager.getDecoyFileList());
            } catch (Exception e) {
                logActivity("Error refreshing decoy file list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Filter files based on search term
     */
    private void filterFiles(String searchTerm) {
        if (fileManager != null && fileListView != null) {
            try {
                fileListView.getItems().clear();
                fileManager.listFiles().stream()
                    .filter(fileName -> fileName.toLowerCase().contains(searchTerm.toLowerCase()))
                    .forEach(fileName -> fileListView.getItems().add(fileName));
            } catch (Exception e) {
                logActivity("Error filtering files: " + e.getMessage());
            }
        }
    }
    
    /**
     * Filter decoy files based on search term
     */
    private void filterDecoyFiles(String searchTerm) {
        if (decoyManager != null && fileListView != null) {
            try {
                fileListView.getItems().clear();
                decoyManager.getDecoyFileList().stream()
                    .filter(fileName -> fileName.toLowerCase().contains(searchTerm.toLowerCase()))
                    .forEach(fileName -> fileListView.getItems().add(fileName));
            } catch (Exception e) {
                logActivity("Error filtering decoy files: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle file upload
     */
    @FXML
    private void handleUpload() {
        if (isDecoyMode) {
            // Simulate upload in decoy mode
            logActivity("File uploaded (decoy mode)");
            if (uiManager != null) {
                uiManager.showInfo("Upload Complete", "File uploaded successfully");
            }
        } else {
            // Real file upload
            // Implementation would use FileChooser and FileManager
            logActivity("File upload initiated");
        }
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
            logActivity("File downloaded: " + selectedFile + " (decoy mode)");
            if (uiManager != null) {
                uiManager.showInfo("Download Complete", "File downloaded: " + selectedFile);
            }
        } else {
            // Real file download
            logActivity("File download initiated: " + selectedFile);
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
        
        if (uiManager != null) {
            boolean confirmed = uiManager.showConfirmation("Confirm Delete", 
                "Are you sure you want to permanently delete: " + selectedFile + "?");
            
            if (confirmed) {
                if (isDecoyMode) {
                    // Simulate deletion in decoy mode
                    logActivity("File deleted: " + selectedFile + " (decoy mode)");
                    uiManager.showInfo("Delete Complete", "File deleted successfully");
                } else {
                    // Real file deletion
                    logActivity("File deletion initiated: " + selectedFile);
                }
            }
        }
    }
    
    /**
     * Handle backup creation
     */
    @FXML
    private void handleBackup() {
        if (isDecoyMode) {
            // Don't allow backup in decoy mode
            if (uiManager != null) {
                uiManager.showInfo("Backup", "Backup feature not available");
            }
            return;
        }
        
        try {
            // Open backup/restore interface
            if (uiManager != null && backupManager != null) {
                uiManager.switchToScene(uiManager.createBackupRestoreScene(backupManager, encryptionKey));
            }
        } catch (Exception e) {
            logActivity("Error opening backup interface: " + e.getMessage());
        }
    }
    
    /**
     * Handle settings
     */
    @FXML
    private void handleSettings() {
        logActivity("Settings opened");
        
        if (uiManager != null) {
            // Toggle theme as a simple setting
            uiManager.toggleTheme();
        }
    }
    
    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        if (uiManager != null) {
            boolean confirmed = uiManager.showConfirmation("Logout", 
                "Are you sure you want to logout?");
            
            if (confirmed) {
                logActivity("User logged out");
                try {
                    uiManager.switchToScene(uiManager.createLoginScene());
                } catch (Exception e) {
                    logActivity("Error returning to login: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Log activity to the activity log
     */
    private void logActivity(String message) {
        if (activityLog != null) {
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            activityLog.appendText("[" + timestamp + "] " + message + "\n");
        }
    }
    
    /**
     * Custom cell factory for file list
     */
    private static class FileListCell extends ListCell<String> {
        @Override
        protected void updateItem(String fileName, boolean empty) {
            super.updateItem(fileName, empty);
            
            if (empty || fileName == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText("📄 " + fileName);
                setTooltip(new Tooltip("File: " + fileName));
            }
        }
    }
}