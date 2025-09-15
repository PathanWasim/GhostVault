package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.decoy.DecoyManager;
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
            // TODO: Implement real file upload
            logArea.appendText("Upload functionality not yet implemented\n");
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
            // TODO: Implement real file download
            logArea.appendText("Download functionality not yet implemented\n");
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
                    // TODO: Implement real secure deletion
                    logArea.appendText("Secure delete functionality not yet implemented\n");
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
        
        // TODO: Implement backup functionality
        logArea.appendText("Backup functionality not yet implemented\n");
    }
    
    /**
     * Handle vault restore
     */
    @FXML
    private void handleRestore() {
        if (isDecoyMode) {
            return; // No restore in decoy mode
        }
        
        // TODO: Implement restore functionality
        logArea.appendText("Restore functionality not yet implemented\n");
    }
    
    /**
     * Handle settings
     */
    @FXML
    private void handleSettings() {
        if (uiManager != null) {
            // Show settings dialog
            Alert settingsAlert = new Alert(Alert.AlertType.INFORMATION);
            settingsAlert.setTitle("Vault Settings");
            settingsAlert.setHeaderText("GhostVault Settings");
            settingsAlert.setContentText(
                "Settings functionality coming soon!\n\n" +
                "Available options will include:\n" +
                "• Session timeout configuration\n" +
                "• Theme preferences\n" +
                "• Security settings\n" +
                "• Backup preferences"
            );
            settingsAlert.showAndWait();
        }
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
            // TODO: Load actual files from vault
            // For now, show placeholder
            fileList.addAll("sample_document.pdf", "important_notes.txt", "financial_data.xlsx");
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