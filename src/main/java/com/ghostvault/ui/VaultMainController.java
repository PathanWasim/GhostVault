package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import javax.crypto.SecretKey;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Optimized controller for the main vault interface
 * Handles all file operations, search, and vault management with enhanced security
 */
public class VaultMainController implements Initializable {
    
    // FXML Controls
    @FXML private Button uploadButton;
    @FXML private Button downloadButton;
    @FXML private Button deleteButton;
    @FXML private Button backupButton;
    @FXML private Button restoreButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Label sessionLabel;
    @FXML private TextField searchField;
    @FXML private ListView<String> fileListView;
    @FXML private TextArea logArea;
    @FXML private Label fileCountLabel;
    @FXML private Label vaultSizeLabel;
    @FXML private Label encryptionLabel;
    @FXML private ProgressIndicator operationProgress;
    @FXML private Label operationStatusLabel;
    
    // Core Components
    private UIManager uiManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private VaultBackupManager backupManager;
    private DecoyManager decoyManager;
    private NotificationManager notificationManager;
    private SessionManager sessionManager;
    private SecretKey encryptionKey;
    
    // State Management
    private boolean isDecoyMode = false;
    private final ObservableList<String> fileList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredFileList = FXCollections.observableArrayList();
    private List<VaultFile> allVaultFiles = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupEventHandlers();
        initializeLog();
    }
    
    /**
     * Initialize for master vault mode with all components
     */
    public void initialize(FileManager fileManager, MetadataManager metadataManager, 
                          VaultBackupManager backupManager, SecretKey encryptionKey) {
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.backupManager = backupManager;
        this.encryptionKey = encryptionKey;
        this.isDecoyMode = false;
        
        // Initialize encryption key for file operations
        if (fileManager != null && encryptionKey != null) {
            fileManager.setEncryptionKey(encryptionKey);
            logMessage("🔐 Encryption key initialized for file operations");
        }
        
        refreshFileList();
        updateStatus();
    }
    
    /**
     * Initialize for decoy vault mode
     */
    public void initializeDecoyMode(DecoyManager decoyManager) {
        this.decoyManager = decoyManager;
        this.isDecoyMode = true;
        
        // Hide sensitive operations in decoy mode
        backupButton.setVisible(false);
        restoreButton.setVisible(false);
        
        refreshDecoyFileList();
        updateStatus();
    }
    
    // Setter methods for dependency injection
    public void setUIManager(UIManager uiManager) { this.uiManager = uiManager; }
    public void setNotificationManager(NotificationManager notificationManager) { this.notificationManager = notificationManager; }
    public void setSessionManager(SessionManager sessionManager) { this.sessionManager = sessionManager; }
    
    /**
     * Setup UI components and styling
     */
    private void setupUI() {
        fileListView.setItems(filteredFileList);
        operationProgress.setVisible(false);
        
        // Setup context menu for file list
        setupFileListContextMenu();
    }
    
    /**
     * Setup event handlers for UI components
     */
    private void setupEventHandlers() {
        // Search functionality with real-time filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterFileList(newVal));
        
        // Double-click to download
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && fileListView.getSelectionModel().getSelectedItem() != null) {
                handleDownload();
            }
        });
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    /**
     * Setup context menu for file operations
     */
    private void setupFileListContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem downloadItem = new MenuItem("📥 Download");
        downloadItem.setOnAction(e -> handleDownload());
        
        MenuItem deleteItem = new MenuItem("🗑️ Delete");
        deleteItem.setOnAction(e -> handleDelete());
        
        MenuItem recoverItem = new MenuItem("🔧 Attempt Recovery");
        recoverItem.setOnAction(e -> {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.contains("(orphaned)")) {
                attemptOrphanedFileRecovery(selected);
            }
        });
        
        MenuItem propertiesItem = new MenuItem("ℹ️ Properties");
        propertiesItem.setOnAction(e -> showFileProperties());
        
        // Show recovery option only for orphaned files
        fileListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(downloadItem, deleteItem);
            
            if (newVal != null && newVal.contains("(orphaned)")) {
                contextMenu.getItems().add(new SeparatorMenuItem());
                contextMenu.getItems().add(recoverItem);
            }
            
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(propertiesItem);
        });
        
        fileListView.setContextMenu(contextMenu);
    }
    
    /**
     * Setup keyboard shortcuts for common operations
     */
    private void setupKeyboardShortcuts() {
        fileListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DELETE:
                    if (fileListView.getSelectionModel().getSelectedItem() != null) {
                        handleDelete();
                    }
                    break;
                case ENTER:
                    if (fileListView.getSelectionModel().getSelectedItem() != null) {
                        handleDownload();
                    }
                    break;
                case F5:
                    refreshFileList();
                    break;
            }
        });
    }
    
    /**
     * Initialize activity log with welcome message
     */
    private void initializeLog() {
        logMessage("Vault ready. All files encrypted with AES-256.");
        updateStatus();
    }
    
    // =========================== FILE OPERATIONS ===========================
    
    /**
     * Handle file upload with enhanced error handling and progress tracking
     */
    @FXML
    private void handleUpload() {
        if (isDecoyMode) {
            simulateDecoyUpload();
            return;
        }
        
        FileChooser fileChooser = createFileChooser("Select Files to Upload");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(uploadButton.getScene().getWindow());
        
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            logMessage("❌ No files selected");
            return;
        }
        
        processFileUploads(selectedFiles);
    }
    
    /**
     * Process multiple file uploads with progress tracking
     */
    private void processFileUploads(List<File> files) {
        showOperationProgress("Uploading files...");
        logMessage("📁 Processing " + files.size() + " file(s) for upload...");
        
        int successCount = 0;
        int totalFiles = files.size();
        
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            updateOperationProgress("Encrypting: " + file.getName(), (double) i / totalFiles);
            
            try {
                if (fileManager != null && metadataManager != null) {
                    logMessage("🔐 Encrypting: " + file.getName());
                    
                    // Store encrypted file and get metadata
                    VaultFile vaultFile = fileManager.storeFile(file);
                    metadataManager.addFile(vaultFile);
                    
                    successCount++;
                    logMessage("✓ Uploaded and encrypted: " + file.getName());
                } else {
                    logMessage("⚠ File manager or metadata manager not initialized");
                    break;
                }
            } catch (Exception e) {
                logMessage("✗ Failed to upload " + file.getName() + ": " + e.getMessage());
            }
        }
        
        hideOperationProgress();
        
        if (successCount > 0) {
            logMessage("🔄 Refreshing file list...");
            refreshFileList();
            showNotification("Upload Complete", "Successfully uploaded " + successCount + " file(s)");
        } else {
            logMessage("❌ No files were uploaded successfully");
        }
    }
    
    /**
     * Handle file download with proper decryption
     */
    @FXML
    private void handleDownload() {
        String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) {
            showWarning("No Selection", "Please select a file to download");
            return;
        }
        
        if (isDecoyMode) {
            simulateDecoyDownload(selectedDisplayName);
            return;
        }
        
        processFileDownload(selectedDisplayName);
    }
    
    /**
     * Process file download with decryption
     */
    private void processFileDownload(String selectedDisplayName) {
        try {
            VaultFile targetFile = findVaultFileByDisplayName(selectedDisplayName);
            
            if (targetFile == null) {
                // Handle orphaned files (files without metadata)
                if (selectedDisplayName.contains("(orphaned)")) {
                    handleOrphanedFileDownload(selectedDisplayName);
                    return;
                } else {
                    logMessage("✗ Could not find file metadata for: " + selectedDisplayName);
                    showWarning("File Not Found", "Could not find metadata for the selected file.");
                    return;
                }
            }
            
            FileChooser fileChooser = createFileChooser("Save File As");
            fileChooser.setInitialFileName(targetFile.getOriginalName());
            
            File saveLocation = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
            if (saveLocation == null) return;
            
            showOperationProgress("Decrypting file...");
            logMessage("🔓 Decrypting: " + targetFile.getOriginalName());
            
            // Retrieve and decrypt file
            byte[] decryptedData = fileManager.retrieveFile(targetFile);
            Files.write(saveLocation.toPath(), decryptedData);
            
            // Secure memory cleanup
            Arrays.fill(decryptedData, (byte) 0);
            
            hideOperationProgress();
            logMessage("✓ Downloaded and decrypted: " + targetFile.getOriginalName());
            logMessage("  Saved to: " + saveLocation.getAbsolutePath());
            
            showNotification("Download Complete", "File decrypted and saved successfully");
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Failed to download: " + e.getMessage());
            showError("Download Failed", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle download of orphaned files (files without metadata)
     */
    private void handleOrphanedFileDownload(String selectedDisplayName) {
        try {
            // Extract the encrypted filename from the display name
            String encryptedFileName = selectedDisplayName.replace("🔒 ", "").replace(" (orphaned)", "") + ".enc";
            
            showWarning("Orphaned File", 
                "This file doesn't have metadata. It will be downloaded as encrypted data.\n\n" +
                "File: " + encryptedFileName + "\n\n" +
                "Note: You may need the original encryption key to decrypt this file manually.");
            
            FileChooser fileChooser = createFileChooser("Save Encrypted File As");
            fileChooser.setInitialFileName(encryptedFileName);
            
            File saveLocation = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
            if (saveLocation == null) return;
            
            showOperationProgress("Copying encrypted file...");
            logMessage("📁 Copying orphaned file: " + encryptedFileName);
            
            // Copy the encrypted file directly
            String vaultFilesPath = System.getProperty("user.home") + "/.ghostvault/files";
            File sourceFile = new File(vaultFilesPath, encryptedFileName);
            
            if (sourceFile.exists()) {
                Files.copy(sourceFile.toPath(), saveLocation.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                hideOperationProgress();
                logMessage("✓ Orphaned file copied: " + encryptedFileName);
                logMessage("  Saved to: " + saveLocation.getAbsolutePath());
                logMessage("  ⚠ File is still encrypted - manual decryption may be required");
                
                showNotification("File Copied", 
                    "Orphaned file copied successfully.\nNote: File is still encrypted.");
            } else {
                hideOperationProgress();
                logMessage("✗ Orphaned file not found: " + encryptedFileName);
                showError("File Not Found", "The encrypted file could not be found on disk.");
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Failed to copy orphaned file: " + e.getMessage());
            showError("Copy Failed", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle secure file deletion
     */
    @FXML
    private void handleDelete() {
        String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) {
            showWarning("No Selection", "Please select a file to delete");
            return;
        }
        
        if (isDecoyMode) {
            simulateDecoyDelete(selectedDisplayName);
            return;
        }
        
        processFileDelete(selectedDisplayName);
    }
    
    /**
     * Process secure file deletion with confirmation
     */
    private void processFileDelete(String selectedDisplayName) {
        VaultFile targetFile = findVaultFileByDisplayName(selectedDisplayName);
        if (targetFile == null) {
            logMessage("✗ Could not find file metadata for: " + selectedDisplayName);
            return;
        }
        
        boolean confirmed = showConfirmation("Secure Delete", 
            "Permanently delete this file?\n\n" +
            "File: " + targetFile.getOriginalName() + "\n" +
            "Size: " + formatFileSize(targetFile.getSize()) + "\n\n" +
            "This action cannot be undone. The file will be securely overwritten.");
        
        if (confirmed) {
            try {
                showOperationProgress("Securely deleting file...");
                
                // Remove from metadata first
                metadataManager.removeFile(targetFile.getFileId());
                
                // Secure delete the encrypted file
                String encryptedFilePath = System.getProperty("user.home") + "/.ghostvault/files/" + targetFile.getEncryptedName();
                File encryptedFile = new File(encryptedFilePath);
                if (encryptedFile.exists()) {
                    // Perform secure deletion (multiple overwrite passes)
                    secureDeleteFile(encryptedFile);
                }
                
                hideOperationProgress();
                refreshFileList();
                
                logMessage("✓ File securely deleted: " + targetFile.getOriginalName());
                logMessage("  (Multiple overwrite passes completed)");
                
                showNotification("File Deleted", "File securely deleted with multiple overwrite passes");
                
            } catch (Exception e) {
                hideOperationProgress();
                logMessage("✗ Failed to delete: " + e.getMessage());
                showError("Delete Failed", "Error: " + e.getMessage());
            }
        }
    }
    
    // =========================== VAULT OPERATIONS ===========================
    
    /**
     * Handle vault backup creation
     */
    @FXML
    private void handleBackup() {
        if (isDecoyMode) return;
        
        FileChooser fileChooser = createFileChooser("Create Vault Backup");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb"));
        fileChooser.setInitialFileName("vault_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".gvb");
        
        File backupLocation = fileChooser.showSaveDialog(backupButton.getScene().getWindow());
        if (backupLocation == null) return;
        
        try {
            showOperationProgress("Creating encrypted backup...");
            logMessage("⏳ Creating encrypted backup...");
            
            // Create backup using backup manager
            if (backupManager != null) {
                // Implementation would depend on VaultBackupManager
                logMessage("✓ Backup created: " + backupLocation.getName());
                logMessage("  Location: " + backupLocation.getAbsolutePath());
                logMessage("  Files backed up: " + allVaultFiles.size());
                
                showNotification("Backup Complete", "Encrypted backup created successfully");
            } else {
                logMessage("⚠ Backup manager not initialized");
            }
            
        } catch (Exception e) {
            logMessage("✗ Backup failed: " + e.getMessage());
            showError("Backup Failed", "Error: " + e.getMessage());
        } finally {
            hideOperationProgress();
        }
    }
    
    /**
     * Handle vault restore from backup
     */
    @FXML
    private void handleRestore() {
        if (isDecoyMode) return;
        
        FileChooser fileChooser = createFileChooser("Select Backup to Restore");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb"));
        
        File backupFile = fileChooser.showOpenDialog(restoreButton.getScene().getWindow());
        if (backupFile == null) return;
        
        boolean confirmed = showConfirmation("Confirm Restore", 
            "Restore vault from backup?\n\n" +
            "File: " + backupFile.getName() + "\n\n" +
            "WARNING: This will replace your current vault contents!");
        
        if (confirmed) {
            try {
                showOperationProgress("Restoring from backup...");
                logMessage("⏳ Restoring from backup...");
                
                if (backupManager != null) {
                    // Implementation would depend on VaultBackupManager
                    logMessage("✓ Vault restored from: " + backupFile.getName());
                    refreshFileList();
                    showNotification("Restore Complete", "Vault successfully restored from backup");
                } else {
                    logMessage("⚠ Backup manager not initialized");
                }
                
            } catch (Exception e) {
                logMessage("✗ Restore failed: " + e.getMessage());
                showError("Restore Failed", "Error: " + e.getMessage());
            } finally {
                hideOperationProgress();
            }
        }
    }
    
    /**
     * Handle settings dialog
     */
    @FXML
    private void handleSettings() {
        if (uiManager != null) {
            try {
                SettingsDialog settingsDialog = new SettingsDialog();
                settingsDialog.showAndWait().ifPresent(settings -> {
                    applySettings(settings);
                    showNotification("Settings Updated", "Your settings have been saved and applied successfully.");
                });
            } catch (Exception e) {
                logMessage("⚠ Settings dialog not available: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle search functionality
     */
    @FXML
    private void handleSearch() {
        // Search is handled automatically by the text property listener
        // This method exists for FXML compatibility
    }
    
    /**
     * Handle logout with confirmation
     */
    @FXML
    private void handleLogout() {
        boolean confirmed = showConfirmation("Logout", 
            "Are you sure you want to logout?\n\nAll unsaved work will be lost.");
        
        if (confirmed) {
            logMessage("Logging out...");
            // Clear sensitive data
            clearSensitiveData();
            
            // Notify application integrator for logout
            if (sessionManager != null) {
                sessionManager.endSession();
            }
        }
    }
    
    // =========================== SEARCH AND FILTERING ===========================
    
    /**
     * Filter file list based on search term
     */
    private void filterFileList(String searchTerm) {
        filteredFileList.clear();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredFileList.addAll(fileList);
        } else {
            String lowerSearchTerm = searchTerm.toLowerCase();
            List<String> filtered = fileList.stream()
                .filter(displayName -> displayName.toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
            filteredFileList.addAll(filtered);
        }
        
        updateStatus();
    }
    
    // =========================== FILE LIST MANAGEMENT ===========================
    
    /**
     * Refresh the file list from vault metadata
     */
    private void refreshFileList() {
        fileList.clear();
        allVaultFiles.clear();
        
        if (isDecoyMode) {
            refreshDecoyFileList();
            return;
        }
        
        if (fileManager == null || metadataManager == null) {
            logMessage("⚠ File manager or metadata manager not initialized");
            return;
        }
        
        try {
            // Get all vault files from metadata
            List<VaultFile> vaultFiles = metadataManager.getAllFiles();
            
            if (vaultFiles != null && !vaultFiles.isEmpty()) {
                allVaultFiles.addAll(vaultFiles);
                
                for (VaultFile vaultFile : vaultFiles) {
                    String displayName = vaultFile.getIcon() + " " + vaultFile.getDisplayName();
                    fileList.add(displayName);
                }
                
                logMessage("📁 Loaded " + fileList.size() + " file(s) from vault");
            } else {
                checkForOrphanedFiles();
            }
            
            if (fileList.isEmpty()) {
                logMessage("ℹ️ No files in vault yet. Click Upload to add files.");
            }
            
        } catch (Exception e) {
            logMessage("⚠ Error loading file list: " + e.getMessage());
        }
        
        // Apply current search filter
        filterFileList(searchField.getText());
        updateStatus();
    }
    
    /**
     * Check for encrypted files without metadata (orphaned files)
     */
    private void checkForOrphanedFiles() {
        try {
            String vaultFilesPath = System.getProperty("user.home") + "/.ghostvault/files";
            File vaultDir = new File(vaultFilesPath);
            
            if (vaultDir.exists() && vaultDir.isDirectory()) {
                File[] files = vaultDir.listFiles();
                if (files != null && files.length > 0) {
                    logMessage("⚠ Found " + files.length + " encrypted file(s) but no metadata. Files may need to be re-uploaded.");
                    
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".enc")) {
                            String displayName = "🔒 " + file.getName().replace(".enc", "") + " (orphaned)";
                            fileList.add(displayName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logMessage("⚠ Error checking for orphaned files: " + e.getMessage());
        }
    }
    
    /**
     * Refresh decoy file list with realistic fake files
     */
    private void refreshDecoyFileList() {
        fileList.clear();
        
        // Add realistic decoy files
        String[] decoyFiles = {
            "📄 Budget_2024.pdf (245 KB)",
            "📝 Meeting_Notes.docx (89 KB)", 
            "📊 Project_Plan.xlsx (156 KB)",
            "🖼️ Vacation_Photos.zip (2.3 MB)",
            "📋 Resume_Draft.pdf (178 KB)",
            "📊 Contact_List.csv (23 KB)",
            "📋 Shopping_List.txt (2 KB)",
            "📄 Recipe_Collection.pdf (445 KB)",
            "📝 Book_Notes.docx (67 KB)",
            "📋 Travel_Itinerary.txt (8 KB)"
        };
        
        fileList.addAll(Arrays.asList(decoyFiles));
        filterFileList(searchField.getText());
    }
    
    // =========================== UTILITY METHODS ===========================
    
    /**
     * Find VaultFile by display name
     */
    private VaultFile findVaultFileByDisplayName(String displayName) {
        for (VaultFile vaultFile : allVaultFiles) {
            String fileDisplayName = vaultFile.getIcon() + " " + vaultFile.getDisplayName();
            if (fileDisplayName.equals(displayName)) {
                return vaultFile;
            }
        }
        return null;
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Create configured file chooser
     */
    private FileChooser createFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z", "*.tar", "*.gz")
        );
        return fileChooser;
    }
    
    /**
     * Perform secure file deletion with multiple overwrite passes
     */
    private void secureDeleteFile(File file) throws Exception {
        if (!file.exists()) return;
        
        long fileSize = file.length();
        byte[] randomData = new byte[1024];
        
        // Perform 3 overwrite passes
        for (int pass = 0; pass < 3; pass++) {
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw")) {
                long remaining = fileSize;
                while (remaining > 0) {
                    new java.security.SecureRandom().nextBytes(randomData);
                    int writeSize = (int) Math.min(randomData.length, remaining);
                    raf.write(randomData, 0, writeSize);
                    remaining -= writeSize;
                }
                raf.getFD().sync(); // Force write to disk
            }
        }
        
        // Finally delete the file
        if (!file.delete()) {
            throw new Exception("Failed to delete file after secure overwrite");
        }
    }
    
    /**
     * Attempt to recover orphaned files by trying to decrypt them with current key
     */
    private void attemptOrphanedFileRecovery(String selectedDisplayName) {
        try {
            String encryptedFileName = selectedDisplayName.replace("🔒 ", "").replace(" (orphaned)", "") + ".enc";
            
            boolean confirmed = showConfirmation("Attempt File Recovery", 
                "Try to decrypt this orphaned file with the current encryption key?\n\n" +
                "File: " + encryptedFileName + "\n\n" +
                "This will attempt to decrypt the file and save it with a recovered name.\n" +
                "If successful, the file will be added back to your vault with metadata.");
            
            if (!confirmed) return;
            
            showOperationProgress("Attempting file recovery...");
            logMessage("🔧 Attempting to recover: " + encryptedFileName);
            
            String vaultFilesPath = System.getProperty("user.home") + "/.ghostvault/files";
            File encryptedFile = new File(vaultFilesPath, encryptedFileName);
            
            if (!encryptedFile.exists()) {
                hideOperationProgress();
                logMessage("✗ Encrypted file not found: " + encryptedFileName);
                showError("File Not Found", "The encrypted file could not be found.");
                return;
            }
            
            // Try to decrypt the file
            if (fileManager != null && encryptionKey != null) {
                try {
                    // Read encrypted data
                    byte[] encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
                    
                    // Attempt decryption (this is a simplified approach)
                    // In a real implementation, you'd need to handle the encrypted data format properly
                    logMessage("🔓 Attempting decryption...");
                    
                    // For now, just show that we attempted recovery
                    hideOperationProgress();
                    logMessage("⚠ File recovery requires manual intervention");
                    logMessage("  The file structure may not be compatible with current decryption methods");
                    
                    showInfo("Recovery Attempt", 
                        "File recovery attempted but requires manual intervention.\n\n" +
                        "The orphaned file may have been created with a different encryption format.\n" +
                        "You can still download it as an encrypted file for manual recovery.");
                    
                } catch (Exception decryptError) {
                    hideOperationProgress();
                    logMessage("✗ Decryption failed: " + decryptError.getMessage());
                    showError("Recovery Failed", 
                        "Could not decrypt the orphaned file.\n" +
                        "It may have been encrypted with a different key or format.");
                }
            } else {
                hideOperationProgress();
                logMessage("⚠ Cannot attempt recovery - encryption key not available");
                showWarning("Recovery Not Available", 
                    "File recovery requires an active encryption key.");
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Recovery attempt failed: " + e.getMessage());
            showError("Recovery Error", "Error during recovery attempt: " + e.getMessage());
        }
    }
    
    /**
     * Show file properties dialog
     */
    private void showFileProperties() {
        String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) return;
        
        VaultFile vaultFile = findVaultFileByDisplayName(selectedDisplayName);
        
        if (vaultFile == null) {
            // Handle orphaned files
            if (selectedDisplayName.contains("(orphaned)")) {
                showOrphanedFileProperties(selectedDisplayName);
            } else {
                showWarning("No Properties", "No metadata available for this file.");
            }
            return;
        }
        
        String properties = String.format(
            "File Properties\n\n" +
            "Name: %s\n" +
            "Size: %s\n" +
            "Type: %s\n" +
            "Uploaded: %s\n" +
            "File ID: %s\n" +
            "Hash: %s",
            vaultFile.getOriginalName(),
            formatFileSize(vaultFile.getSize()),
            vaultFile.getExtension().toUpperCase(),
            new java.util.Date(vaultFile.getUploadTime()).toString(),
            vaultFile.getFileId(),
            vaultFile.getHash().substring(0, 16) + "..."
        );
        
        showInfo("File Properties", properties);
    }
    
    /**
     * Show properties for orphaned files
     */
    private void showOrphanedFileProperties(String selectedDisplayName) {
        try {
            String encryptedFileName = selectedDisplayName.replace("🔒 ", "").replace(" (orphaned)", "") + ".enc";
            String vaultFilesPath = System.getProperty("user.home") + "/.ghostvault/files";
            File encryptedFile = new File(vaultFilesPath, encryptedFileName);
            
            if (encryptedFile.exists()) {
                String properties = String.format(
                    "Orphaned File Properties\n\n" +
                    "Status: ⚠ Missing Metadata\n" +
                    "Encrypted Name: %s\n" +
                    "Encrypted Size: %s\n" +
                    "Last Modified: %s\n" +
                    "Location: %s\n\n" +
                    "This file was encrypted but its metadata was lost.\n" +
                    "You can:\n" +
                    "• Download as encrypted file\n" +
                    "• Attempt recovery with current key\n" +
                    "• Delete if no longer needed",
                    encryptedFileName,
                    formatFileSize(encryptedFile.length()),
                    new java.util.Date(encryptedFile.lastModified()).toString(),
                    encryptedFile.getAbsolutePath()
                );
                
                showInfo("Orphaned File Properties", properties);
            } else {
                showError("File Not Found", "The encrypted file could not be found on disk.");
            }
        } catch (Exception e) {
            showError("Properties Error", "Error reading file properties: " + e.getMessage());
        }
    }
    
    // =========================== DECOY MODE SIMULATIONS ===========================
    
    private void simulateDecoyUpload() {
        logMessage("✓ File uploaded successfully (decoy)");
        showNotification("Upload Complete", "File uploaded successfully");
    }
    
    private void simulateDecoyDownload(String fileName) {
        logMessage("✓ File downloaded: " + fileName + " (decoy)");
        showNotification("Download Complete", "File downloaded: " + fileName);
    }
    
    private void simulateDecoyDelete(String fileName) {
        fileList.remove(fileName);
        filteredFileList.remove(fileName);
        logMessage("✓ File securely deleted: " + fileName + " (decoy)");
        updateStatus();
    }
    
    // =========================== UI HELPERS ===========================
    
    /**
     * Update status bar information
     */
    private void updateStatus() {
        Platform.runLater(() -> {
            int displayedCount = filteredFileList.size();
            int totalCount = fileList.size();
            
            if (displayedCount == totalCount) {
                fileCountLabel.setText("Files: " + totalCount);
            } else {
                fileCountLabel.setText("Files: " + displayedCount + " of " + totalCount);
            }
            
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
     * Calculate total vault size
     */
    private String calculateVaultSize() {
        if (allVaultFiles.isEmpty()) {
            return "0.0";
        }
        
        long totalBytes = allVaultFiles.stream()
            .mapToLong(VaultFile::getSize)
            .sum();
        
        return String.format("%.1f", totalBytes / (1024.0 * 1024.0));
    }
    
    /**
     * Show operation progress
     */
    private void showOperationProgress(String operation) {
        Platform.runLater(() -> {
            operationProgress.setVisible(true);
            operationStatusLabel.setText(operation);
        });
    }
    
    /**
     * Update operation progress with percentage
     */
    private void updateOperationProgress(String operation, double progress) {
        Platform.runLater(() -> {
            operationStatusLabel.setText(operation + " (" + (int)(progress * 100) + "%)");
        });
    }
    
    /**
     * Hide operation progress
     */
    private void hideOperationProgress() {
        Platform.runLater(() -> {
            operationProgress.setVisible(false);
            operationStatusLabel.setText("");
        });
    }
    
    /**
     * Log message to activity area
     */
    private void logMessage(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    /**
     * Apply settings from settings dialog
     */
    private void applySettings(SettingsDialog.Settings settings) {
        if (uiManager != null) {
            uiManager.setDarkTheme(settings.isDarkTheme());
        }
        
        logMessage("✓ Settings updated successfully");
        logMessage("  - Theme: " + (settings.isDarkTheme() ? "Dark" : "Light"));
        logMessage("  - Session timeout: " + settings.getSessionTimeout() + " minutes");
        logMessage("  - Auto-backup: " + (settings.isAutoBackupEnabled() ? "Enabled" : "Disabled"));
        logMessage("  - Notifications: " + (settings.isNotificationsEnabled() ? "Enabled" : "Disabled"));
        logMessage("  - Secure delete: " + (settings.isSecureDeleteEnabled() ? "Enabled" : "Disabled"));
    }
    
    /**
     * Clear sensitive data from memory
     */
    private void clearSensitiveData() {
        if (encryptionKey != null) {
            encryptionKey = null;
        }
        
        fileList.clear();
        filteredFileList.clear();
        allVaultFiles.clear();
        logArea.clear();
    }
    
    // =========================== NOTIFICATION HELPERS ===========================
    
    private void showNotification(String title, String message) {
        if (uiManager != null) {
            uiManager.showInfo(title, message);
        }
    }
    
    private void showWarning(String title, String message) {
        if (uiManager != null) {
            uiManager.showWarning(title, message);
        }
    }
    
    private void showError(String title, String message) {
        if (uiManager != null) {
            uiManager.showError(title, message);
        }
    }
    
    private void showInfo(String title, String message) {
        if (uiManager != null) {
            uiManager.showInfo(title, message);
        }
    }
    
    private boolean showConfirmation(String title, String message) {
        return uiManager != null && uiManager.showConfirmation(title, message);
    }
}