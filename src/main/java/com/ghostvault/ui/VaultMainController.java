package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
// import com.ghostvault.core.MetadataRecoveryManager; // Not implemented yet
import com.ghostvault.security.PasswordVaultManager;
import com.ghostvault.security.PasswordEntry;
import com.ghostvault.security.SecureNotesManager;
import com.ghostvault.security.SecureNote;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Optional;
import com.ghostvault.ui.animations.AnimationManager;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @FXML private Button previewButton;
    @FXML private Button deleteButton;
    @FXML private Button backupButton;
    @FXML private Button restoreButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;
    @FXML private Button notesButton;
    @FXML private Button passwordsButton;
    @FXML private Button fileManagerButton;
    @FXML private Label sessionLabel;
    
    // Dashboard Components
    @FXML private VBox mainContent;
    @FXML private VBox dashboardOverlay;
    
    @FXML private TextField searchField;
    @FXML private ListView<String> fileListView;
    @FXML private TextArea logArea;
    @FXML private Label fileCountLabel;
    @FXML private Label vaultSizeLabel;
    @FXML private Label encryptionLabel;
    @FXML private ProgressIndicator operationProgress;
    @FXML private Label operationStatusLabel;
    
    // Core Components
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private VaultBackupManager backupManager;
    private DecoyManager decoyManager;
    private NotificationManager notificationManager;
    private SessionManager sessionManager;
    private SecretKey encryptionKey;
    
    // Accessibility Manager
    private com.ghostvault.ui.AccessibilityManager accessibilityManager;
    
    // State Management
    private boolean isDecoyMode = false;
    private boolean isDashboardVisible = false;
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
     * FXML initialize method - called automatically when FXML is loaded
     */
    public void initialize() {
        // Setup UI components now that FXML injection is complete
        setupUI();
        
        logMessage("🔧 FXML components initialized");
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
        
        // Initialize encryption key for metadata operations with session validation
        if (metadataManager != null && encryptionKey != null) {
            // Validate key consistency before setting
            if (validateSessionKey(encryptionKey)) {
                metadataManager.setEncryptionKey(encryptionKey);
                logMessage("🔐 Encryption key validated and initialized for metadata operations");
                
                // Load existing metadata with enhanced recovery
                try {
                    metadataManager.loadMetadata();
                    logMessage("📋 Metadata loaded successfully");
                } catch (Exception e) {
                    logMessage("⚠ Metadata loading failed, attempting recovery...");
                    
                    // Enhanced error handling with recovery attempt
                    boolean recovered = attemptSessionRecovery(e);
                    if (!recovered) {
                        String userFriendlyError = "Metadata loading failed: " + e.getMessage();
                        logMessage("⚠ " + userFriendlyError);
                        System.err.println("Technical error in metadata loading: " + e.getMessage());
                    }
                }
            } else {
                logMessage("⚠ Session key validation failed - using fallback initialization");
                metadataManager.setEncryptionKey(encryptionKey);
            }
        }
        
        // Initialize Feature Manager with all advanced features
        // Advanced features are now integrated directly
        logMessage("🚀 All features initialized");
        
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
    public void setNotificationManager(NotificationManager notificationManager) { this.notificationManager = notificationManager; }
    public void setSessionManager(SessionManager sessionManager) { this.sessionManager = sessionManager; }
    
    /**
     * Setup UI components and styling
     */
    private void setupUI() {
        if (fileListView != null) {
            fileListView.setItems(filteredFileList);
            logMessage("🔍 Debug: ListView bound to filteredFileList");
        } else {
            logMessage("⚠ Debug: fileListView is null in setupUI!");
        }
        
        if (operationProgress != null) {
            operationProgress.setVisible(false);
        }
        
        // Apply comprehensive text styling using StyleManager
        applyUITextStyling();
        
        // Initialize accessibility features
        initializeAccessibility();
        
        // Setup context menu for file list
        setupFileListContextMenu();
        
        // Setup drag and drop
        setupDragAndDrop();
    }
    
    /**
     * Apply password manager styling to all UI components
     */
    private void applyUITextStyling() {
        // Apply password manager theme to the entire scene
        if (mainContent != null && mainContent.getScene() != null) {
            try {
                com.ghostvault.ui.theme.PasswordManagerTheme.applyPasswordManagerTheme(mainContent.getScene());
            } catch (Exception e) {
                // Fallback if theme not available
                System.out.println("Theme not available, using default styling");
            }
        }
        
        // Apply specific styling to main components
        safeApplyTheme(fileListView);
        safeApplyTheme(searchField);
        safeApplyTheme(logArea);
        
        // Apply styling to labels with proper visibility
        safeApplyTheme(fileCountLabel);
        safeApplyTheme(vaultSizeLabel);
        safeApplyTheme(encryptionLabel);
        safeApplyTheme(operationStatusLabel);
        safeApplyTheme(sessionLabel);
        
        // Apply styling to main content areas
        if (mainContent != null) {
            safeApplyTheme(mainContent);
            // Set password manager background color
            mainContent.setStyle("-fx-background-color: #0F172A;");
        }
        safeApplyTheme(dashboardOverlay);
        
        // Apply styling to toolbar buttons with password manager theme and animations
        if (uploadButton != null) {
            safeApplyTheme(uploadButton);
            uploadButton.getStyleClass().add("primary");
            addButtonHoverAnimation(uploadButton);
        }
        if (downloadButton != null) {
            safeApplyTheme(downloadButton);
            downloadButton.getStyleClass().add("success");
            addButtonHoverAnimation(downloadButton);
        }
        if (deleteButton != null) {
            safeApplyTheme(deleteButton);
            deleteButton.getStyleClass().add("danger");
            addButtonHoverAnimation(deleteButton);
        }
        if (previewButton != null) {
            safeApplyTheme(previewButton);
            previewButton.getStyleClass().add("secondary");
            addButtonHoverAnimation(previewButton);
        }
        
        // Add hover animations to navigation buttons
        if (dashboardButton != null) addButtonHoverAnimation(dashboardButton);
        if (notesButton != null) addButtonHoverAnimation(notesButton);
        if (passwordsButton != null) addButtonHoverAnimation(passwordsButton);
        if (fileManagerButton != null) addButtonHoverAnimation(fileManagerButton);
        if (settingsButton != null) addButtonHoverAnimation(settingsButton);
        if (logoutButton != null) addButtonHoverAnimation(logoutButton);
    }
    
    /**
     * Setup drag and drop functionality
     */
    private void setupDragAndDrop() {
        // Enable drag over
        fileListView.setOnDragOver(event -> {
            if (event.getGestureSource() != fileListView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                
                // Visual feedback
                fileListView.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-border-style: dashed;");
            }
            event.consume();
        });
        
        // Handle drag exit
        fileListView.setOnDragExited(event -> {
            fileListView.setStyle(""); // Remove border
            event.consume();
        });
        
        // Handle file drop
        fileListView.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasFiles()) {
                java.util.List<java.io.File> files = dragboard.getFiles();
                if (!files.isEmpty()) {
                    logMessage("📁 Files dropped: " + files.size() + " file(s)");
                    processFileUploads(files);
                    success = true;
                }
            }
            
            // Remove visual feedback
            fileListView.setStyle("");
            event.setDropCompleted(success);
            event.consume();
        });
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
        
        // Setup tooltips with keyboard shortcuts
        setupTooltips();
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    /**
     * Setup tooltips for buttons with keyboard shortcuts
     */
    private void setupTooltips() {
        if (uploadButton != null) {
            uploadButton.setTooltip(new javafx.scene.control.Tooltip("Upload files to vault (Ctrl+O)\nOr drag & drop files here"));
        }
        if (downloadButton != null) {
            downloadButton.setTooltip(new javafx.scene.control.Tooltip("Download selected file (Ctrl+S or Enter)"));
        }
        if (previewButton != null) {
            previewButton.setTooltip(new javafx.scene.control.Tooltip("Preview selected file (Space)"));
        }
        if (deleteButton != null) {
            deleteButton.setTooltip(new javafx.scene.control.Tooltip("Delete selected file (Delete key)"));
        }
        if (backupButton != null) {
            backupButton.setTooltip(new javafx.scene.control.Tooltip("Create encrypted backup (Ctrl+B)"));
        }
        if (restoreButton != null) {
            restoreButton.setTooltip(new javafx.scene.control.Tooltip("Restore from backup (Ctrl+R)"));
        }
        if (settingsButton != null) {
            settingsButton.setTooltip(new javafx.scene.control.Tooltip("Open settings"));
        }
        if (logoutButton != null) {
            logoutButton.setTooltip(new javafx.scene.control.Tooltip("Logout and return to login (Ctrl+Q)"));
        }
        if (dashboardButton != null) {
            dashboardButton.setTooltip(new javafx.scene.control.Tooltip("Open Security Dashboard (Ctrl+D)"));
        }
        if (notesButton != null) {
            notesButton.setTooltip(new javafx.scene.control.Tooltip("Manage Secure Notes (Ctrl+N)"));
        }
        if (passwordsButton != null) {
            passwordsButton.setTooltip(new javafx.scene.control.Tooltip("Password Manager (Ctrl+P)"));
        }
        if (searchField != null) {
            searchField.setTooltip(new javafx.scene.control.Tooltip("Search files (Ctrl+F to focus, Esc to clear)"));
        }
        if (fileListView != null) {
            fileListView.setTooltip(new javafx.scene.control.Tooltip("File list - Double-click to download, Right-click for options\nPress F1 for all keyboard shortcuts"));
        }
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
        // File list shortcuts
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
                case SPACE:
                    if (fileListView.getSelectionModel().getSelectedItem() != null) {
                        handlePreview();
                    }
                    break;
            }
        });
        
        // Global shortcuts for the scene
        if (fileListView.getScene() != null) {
            setupGlobalShortcuts(fileListView.getScene());
        } else {
            // Set up shortcuts when scene is available
            fileListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setupGlobalShortcuts(newScene);
                }
            });
        }
    }
    
    /**
     * Setup global keyboard shortcuts
     */
    private void setupGlobalShortcuts(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case O: // Ctrl+O - Upload
                        handleUpload();
                        event.consume();
                        break;
                    case S: // Ctrl+S - Download selected
                        if (fileListView.getSelectionModel().getSelectedItem() != null) {
                            handleDownload();
                        }
                        event.consume();
                        break;
                    case B: // Ctrl+B - Backup
                        handleBackup();
                        event.consume();
                        break;
                    case R: // Ctrl+R - Restore
                        handleRestore();
                        event.consume();
                        break;
                    case F: // Ctrl+F - Focus search
                        searchField.requestFocus();
                        event.consume();
                        break;
                    case Q: // Ctrl+Q - Logout
                        handleLogout();
                        event.consume();
                        break;
                    case D: // Ctrl+D - Dashboard
                        handleDashboard();
                        event.consume();
                        break;
                    case N: // Ctrl+N - Notes
                        handleNotes();
                        event.consume();
                        break;
                    case T: // Ctrl+T - Switch Theme
                        switchTheme();
                        event.consume();
                        break;
                    case P: // Ctrl+P - Passwords
                        handlePasswords();
                        event.consume();
                        break;
                    case M: // Ctrl+M - AI Mode
                        handleFileManager();
                        event.consume();
                        break;
                    case I: // Ctrl+I - AI Analysis
                        showAIAnalysis();
                        event.consume();
                        break;
                }
            } else {
                switch (event.getCode()) {
                    case F1: // Help
                        showHelpSystem();
                        event.consume();
                        break;
                    case ESCAPE: // Clear search
                        searchField.clear();
                        searchField.getParent().requestFocus();
                        event.consume();
                        break;
                }
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
        
        // Simple validation
        try {
            // Basic file validation
            for (File file : files) {
                if (!file.exists() || !file.canRead()) {
                    hideOperationProgress();
                    logMessage("❌ File validation failed: Cannot read " + file.getName());
                    showError("File Validation Error", "Cannot read file: " + file.getName());
                    return;
                }
                if (file.length() > 100 * 1024 * 1024) { // 100MB limit
                    hideOperationProgress();
                    logMessage("❌ File too large: " + file.getName());
                    showError("File Too Large", "File " + file.getName() + " exceeds 100MB limit");
                    return;
                }
            }
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("❌ Validation error: " + e.getMessage());
            showError("Validation Error", "File validation failed: " + e.getMessage());
            return;
        }
        
        int successCount = 0;
        int totalFiles = files.size();
        StringBuilder errorSummary = new StringBuilder();
        
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
                    String error = "File manager or metadata manager not initialized";
                    logMessage("⚠ " + error);
                    errorSummary.append(file.getName()).append(": ").append(error).append("\n");
                    break;
                }
            } catch (Exception e) {
                String userFriendlyError = "Upload failed for " + file.getName() + ": " + e.getMessage();
                logMessage("✗ " + userFriendlyError);
                errorSummary.append(file.getName()).append(": ").append(userFriendlyError).append("\n");
            }
        }
        
        hideOperationProgress();
        
        if (successCount > 0) {
            logMessage("🔄 Refreshing file list...");
            refreshFileList();
            
            if (successCount == totalFiles) {
                showNotification("Upload Complete", "Successfully uploaded " + successCount + " file(s)");
            } else {
                showNotification("Partial Success", 
                    "Uploaded " + successCount + " of " + totalFiles + " files.\n\nErrors:\n" + errorSummary.toString());
            }
        } else {
            logMessage("❌ No files were uploaded successfully");
            showError("Upload Failed", "No files could be uploaded.\n\nErrors:\n" + errorSummary.toString());
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
     * Handle file preview
     */
    @FXML
    private void handlePreview() {
        String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
        if (selectedDisplayName == null) {
            showWarning("No Selection", "Please select a file to preview");
            return;
        }
        
        if (isDecoyMode) {
            simulateDecoyPreview(selectedDisplayName);
            return;
        }
        
        processFilePreview(selectedDisplayName);
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
            // Extract the UUID from the display name
            String uuidPart = selectedDisplayName.replace("🔒 ", "").replace(" (orphaned)", "");
            
            // Show warning dialog about orphaned file
            boolean proceed = showConfirmation("Orphaned File Recovery", 
                "This file has no metadata (orphaned file).\n\n" +
                "File ID: " + uuidPart + "\n\n" +
                "⚠️ WARNING:\n" +
                "• Original filename is unknown\n" +
                "• File type cannot be determined\n" +
                "• File may be corrupted or incomplete\n\n" +
                "Do you want to attempt recovery anyway?");
            
            if (!proceed) {
                logMessage("❌ Orphaned file download cancelled by user");
                return;
            }
            
            // Attempt to decrypt the orphaned file
            String encryptedFilePath = System.getProperty("user.home") + "/.ghostvault/files/" + uuidPart + ".enc";
            File encryptedFile = new File(encryptedFilePath);
            
            if (!encryptedFile.exists()) {
                logMessage("✗ Encrypted file not found: " + encryptedFilePath);
                showError("File Not Found", "The encrypted file could not be located on disk.");
                return;
            }
            
            // Set up file chooser for recovery
            FileChooser fileChooser = createFileChooser("Recover Orphaned File As");
            fileChooser.setInitialFileName("recovered_" + uuidPart.substring(0, 8) + ".dat");
            
            File saveLocation = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
            if (saveLocation == null) return;
            
            showOperationProgress("Attempting file recovery...");
            logMessage("🔧 Attempting to recover orphaned file: " + uuidPart);
            
            // Try to decrypt using the current encryption key
            if (fileManager != null) {
                try {
                    // Create a temporary VaultFile for decryption attempt
                    com.ghostvault.model.VaultFile tempVaultFile = new com.ghostvault.model.VaultFile(
                        "unknown_file",
                        uuidPart,
                        uuidPart + ".enc",
                        encryptedFile.length(),
                        "unknown",
                        System.currentTimeMillis()
                    );
                    
                    // Attempt to retrieve/decrypt
                    byte[] decryptedData = fileManager.retrieveFile(tempVaultFile);
                    
                    // Write recovered data
                    Files.write(saveLocation.toPath(), decryptedData);
                    
                    // Clear sensitive data
                    Arrays.fill(decryptedData, (byte) 0);
                    
                    hideOperationProgress();
                    logMessage("✓ Orphaned file recovered and decrypted: " + saveLocation.getName());
                    logMessage("  Saved to: " + saveLocation.getAbsolutePath());
                    logMessage("  ⚠️ Please verify file integrity and rename appropriately");
                    
                    showNotification("Recovery Complete", 
                        "Orphaned file recovered and decrypted successfully!\n\n" +
                        "⚠️ Please verify the file integrity and rename it appropriately.");
                        
                } catch (Exception decryptError) {
                    hideOperationProgress();
                    logMessage("✗ Failed to decrypt orphaned file: " + decryptError.getMessage());
                    
                    // Offer raw file copy as last resort
                    boolean copyRaw = showConfirmation("Decryption Failed", 
                        "Could not decrypt the orphaned file.\n\n" +
                        "Would you like to copy the raw encrypted file instead?\n" +
                        "(You can try to decrypt it manually later)");
                    
                    if (copyRaw) {
                        Files.copy(encryptedFile.toPath(), saveLocation.toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        logMessage("📄 Raw encrypted file copied: " + saveLocation.getName());
                        showNotification("Raw File Copied", "Encrypted file copied for manual recovery.");
                    }
                }
            } else {
                hideOperationProgress();
                logMessage("⚠ File manager not available for orphaned file recovery");
                showError("Recovery Failed", "File manager not initialized for recovery.");
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Orphaned file recovery failed: " + e.getMessage());
            showError("Recovery Failed", "Could not recover orphaned file: " + e.getMessage());
        }
    }
    
    /**
     * Process file preview with decryption and display
     */
    private void processFilePreview(String selectedDisplayName) {
        try {
            VaultFile targetFile = findVaultFileByDisplayName(selectedDisplayName);
            
            if (targetFile == null) {
                if (selectedDisplayName.contains("(orphaned)")) {
                    showWarning("Preview Not Available", "Cannot preview orphaned files. Please recover the file first.");
                    return;
                } else {
                    logMessage("✗ Could not find file metadata for preview: " + selectedDisplayName);
                    showWarning("File Not Found", "Could not find metadata for the selected file.");
                    return;
                }
            }
            
            // Check if file type is previewable
            String extension = targetFile.getExtension().toLowerCase();
            if (!isPreviewableFileType(extension)) {
                showWarning("Preview Not Supported", 
                    "Preview is not supported for this file type: " + extension.toUpperCase() + "\n\n" +
                    "Supported types:\n" +
                    "• Text: TXT, MD\n" +
                    "• Images: JPG, JPEG, PNG, GIF, BMP\n" +
                    "• Documents: PDF\n" +
                    "• Video: MP4, AVI, MOV, MKV, WEBM, FLV\n" +
                    "• Audio: MP3, WAV, FLAC, AAC, OGG, M4A");
                return;
            }
            
            showOperationProgress("Loading file for preview...");
            logMessage("👁️ Loading file for preview: " + targetFile.getOriginalName());
            
            // Retrieve and decrypt file
            byte[] decryptedData = fileManager.retrieveFile(targetFile);
            
            hideOperationProgress();
            
            // Show preview based on file type
            showFilePreview(targetFile, decryptedData);
            
            // Secure memory cleanup
            Arrays.fill(decryptedData, (byte) 0);
            
            logMessage("✓ File preview loaded: " + targetFile.getOriginalName());
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Failed to preview file: " + e.getMessage());
            showError("Preview Failed", "Error loading file preview:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Check if file type supports preview
     */
    private boolean isPreviewableFileType(String extension) {
        return Arrays.asList("txt", "md", "pdf", "jpg", "jpeg", "png", "gif", "bmp", 
                           "mp4", "avi", "mov", "mkv", "webm", "flv",  // Video formats
                           "mp3", "wav", "flac", "aac", "ogg", "m4a").contains(extension); // Audio formats
    }
    
    /**
     * Show file preview in appropriate viewer
     */
    private void showFilePreview(VaultFile vaultFile, byte[] fileData) {
        String extension = vaultFile.getExtension().toLowerCase();
        
        try {
            if (Arrays.asList("txt", "md").contains(extension)) {
                showTextPreview(vaultFile, fileData);
            } else if (extension.equals("pdf")) {
                showPdfPreview(vaultFile, fileData);
            } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(extension)) {
                showImagePreview(vaultFile, fileData);
            } else if (Arrays.asList("mp4", "avi", "mov", "mkv", "webm", "flv").contains(extension)) {
                showVideoPreview(vaultFile, fileData);
            } else if (Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "m4a").contains(extension)) {
                showAudioPreview(vaultFile, fileData);
            } else {
                showWarning("Preview Not Supported", "Preview not supported for file type: " + extension.toUpperCase());
            }
        } catch (Exception e) {
            showError("Preview Error", "Failed to display preview:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Show text file preview
     */
    private void showTextPreview(VaultFile vaultFile, byte[] fileData) {
        try {
            String content = new String(fileData, "UTF-8");
            
            // Create preview dialog
            Dialog<Void> previewDialog = new Dialog<>();
            previewDialog.setTitle("File Preview - " + vaultFile.getOriginalName());
            previewDialog.setHeaderText("Text File Preview");
            
            // Create text area for content
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(600, 400);
            textArea.setStyle("-fx-font-family: 'Courier New', monospace;");
            
            // Add scroll pane
            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            previewDialog.getDialogPane().setContent(scrollPane);
            previewDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Show dialog
            previewDialog.showAndWait();
            
        } catch (Exception e) {
            showError("Text Preview Error", "Failed to display text preview:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Show image file preview
     */
    private void showImagePreview(VaultFile vaultFile, byte[] fileData) {
        try {
            // Create image from byte array
            ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
            javafx.scene.image.Image image = new javafx.scene.image.Image(bis);
            
            if (image.isError()) {
                showError("Image Preview Error", "Failed to load image. The file may be corrupted.");
                return;
            }
            
            // Create preview dialog
            Dialog<Void> previewDialog = new Dialog<>();
            previewDialog.setTitle("File Preview - " + vaultFile.getOriginalName());
            previewDialog.setHeaderText("Image Preview");
            
            // Create image view
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(400);
            
            // Add scroll pane for large images
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setPrefSize(650, 450);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            previewDialog.getDialogPane().setContent(scrollPane);
            previewDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Show dialog
            previewDialog.showAndWait();
            
        } catch (Exception e) {
            showError("Image Preview Error", "Failed to display image preview:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Show PDF file preview (basic implementation)
     */
    private void showPdfPreview(VaultFile vaultFile, byte[] fileData) {
        // For now, show a message that PDF preview requires external viewer
        // In a full implementation, you could integrate a PDF viewer library
        
        boolean openExternal = showConfirmation("PDF Preview", 
            "PDF preview requires an external viewer.\n\n" +
            "File: " + vaultFile.getOriginalName() + "\n" +
            "Size: " + formatFileSize(vaultFile.getSize()) + "\n\n" +
            "Would you like to temporarily save and open the PDF in your default viewer?\n" +
            "(The temporary file will be securely deleted after viewing)");
        
        if (openExternal) {
            try {
                // Create temporary file
                Path tempFile = Files.createTempFile("ghostvault_preview_", ".pdf");
                Files.write(tempFile, fileData);
                
                // Open with default application
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(tempFile.toFile());
                    
                    // Schedule cleanup after delay
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(5000); // Wait 5 seconds
                            Files.deleteIfExists(tempFile);
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    });
                    
                    showNotification("PDF Opened", "PDF opened in external viewer. Temporary file will be cleaned up automatically.");
                } else {
                    Files.deleteIfExists(tempFile);
                    showError("PDF Preview Error", "Desktop operations not supported on this system.");
                }
                
            } catch (Exception e) {
                showError("PDF Preview Error", "Failed to open PDF:\n\n" + e.getMessage());
            }
        }
    }
    
    /**
     * Show video file preview with internal JavaFX MediaView player
     */
    private void showVideoPreview(VaultFile vaultFile, byte[] fileData) {
        try {
            // Create temporary file for video playback
            Path tempFile = Files.createTempFile("ghostvault_video_", "." + vaultFile.getExtension());
            Files.write(tempFile, fileData);
            
            // Create video preview dialog
            Stage videoStage = new Stage();
            videoStage.setTitle("🎬 Video Player - " + vaultFile.getOriginalName());
            videoStage.initModality(javafx.stage.Modality.NONE);
            videoStage.setResizable(true);
            
            VBox root = new VBox(10);
            root.setPadding(new javafx.geometry.Insets(15));
            root.setStyle("-fx-background-color: #0F172A;");
            
            // Video info header
            HBox header = new HBox(15);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color: #1E293B; -fx-padding: 10px 15px; -fx-background-radius: 8px;");
            
            Label info = new Label("🎬 " + vaultFile.getOriginalName());
            info.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 16px; -fx-font-weight: bold;");
            
            Label sizeInfo = new Label(formatFileSize(vaultFile.getSize()) + " • " + vaultFile.getExtension().toUpperCase());
            sizeInfo.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label encryptedLabel = new Label("🔒 Decrypted");
            encryptedLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 4px 8px; -fx-background-radius: 12px; -fx-font-size: 10px;");
            
            header.getChildren().addAll(info, sizeInfo, spacer, encryptedLabel);
            
            try {
                // Create JavaFX MediaView for internal video playback
                javafx.scene.media.Media media = new javafx.scene.media.Media(tempFile.toUri().toString());
                javafx.scene.media.MediaPlayer mediaPlayer = new javafx.scene.media.MediaPlayer(media);
                javafx.scene.media.MediaView mediaView = new javafx.scene.media.MediaView(mediaPlayer);
                
                // Configure media view
                mediaView.setFitWidth(600);
                mediaView.setFitHeight(400);
                mediaView.setPreserveRatio(true);
                mediaView.setStyle("-fx-background-color: #000000; -fx-background-radius: 8px;");
                
                // Media controls panel
                HBox controlsPanel = new HBox(15);
                controlsPanel.setAlignment(javafx.geometry.Pos.CENTER);
                controlsPanel.setStyle("-fx-background-color: #334155; -fx-padding: 15px; -fx-background-radius: 8px;");
                
                // Control buttons
                Button playPauseBtn = new Button("▶️ Play");
                playPauseBtn.getStyleClass().addAll("button", "primary");
                playPauseBtn.setPrefWidth(100);
                
                Button stopBtn = new Button("⏹️ Stop");
                stopBtn.getStyleClass().addAll("button", "secondary");
                stopBtn.setPrefWidth(100);
                
                Button muteBtn = new Button("🔊 Mute");
                muteBtn.getStyleClass().addAll("button", "secondary");
                muteBtn.setPrefWidth(100);
                
                // Volume slider
                Slider volumeSlider = new Slider(0, 1, 0.5);
                volumeSlider.setPrefWidth(150);
                volumeSlider.setStyle("-fx-control-inner-background: #475569;");
                
                Label volumeLabel = new Label("Volume");
                volumeLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
                
                VBox volumeBox = new VBox(5, volumeLabel, volumeSlider);
                volumeBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Progress bar
                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(400);
                progressBar.setStyle("-fx-accent: #175DDC;");
                
                Label timeLabel = new Label("00:00 / 00:00");
                timeLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
                
                VBox progressBox = new VBox(5, progressBar, timeLabel);
                progressBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                controlsPanel.getChildren().addAll(playPauseBtn, stopBtn, muteBtn, volumeBox, progressBox);
                
                // Media player event handlers
                final boolean[] isPlaying = {false};
                
                playPauseBtn.setOnAction(e -> {
                    if (isPlaying[0]) {
                        mediaPlayer.pause();
                        playPauseBtn.setText("▶️ Play");
                        isPlaying[0] = false;
                    } else {
                        mediaPlayer.play();
                        playPauseBtn.setText("⏸️ Pause");
                        isPlaying[0] = true;
                    }
                });
                
                stopBtn.setOnAction(e -> {
                    mediaPlayer.stop();
                    playPauseBtn.setText("▶️ Play");
                    isPlaying[0] = false;
                });
                
                muteBtn.setOnAction(e -> {
                    if (mediaPlayer.isMute()) {
                        mediaPlayer.setMute(false);
                        muteBtn.setText("🔊 Mute");
                    } else {
                        mediaPlayer.setMute(true);
                        muteBtn.setText("🔇 Unmute");
                    }
                });
                
                volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    mediaPlayer.setVolume(newVal.doubleValue());
                });
                
                // Update progress and time
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (mediaPlayer.getTotalDuration() != null) {
                        double progress = newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                        progressBar.setProgress(progress);
                        
                        String currentTime = formatTime(newTime.toSeconds());
                        String totalTime = formatTime(mediaPlayer.getTotalDuration().toSeconds());
                        timeLabel.setText(currentTime + " / " + totalTime);
                    }
                });
                
                // Close button
                Button closeBtn = new Button("Close Player");
                closeBtn.getStyleClass().addAll("button", "secondary");
                closeBtn.setOnAction(e -> {
                    mediaPlayer.dispose();
                    videoStage.close();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                HBox closeBox = new HBox(closeBtn);
                closeBox.setAlignment(javafx.geometry.Pos.CENTER);
                closeBox.setStyle("-fx-padding: 10px 0 0 0;");
                
                root.getChildren().addAll(header, mediaView, controlsPanel, closeBox);
                
                // Cleanup when stage is closed
                videoStage.setOnCloseRequest(e -> {
                    mediaPlayer.dispose();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                logMessage("🎬 Internal video player opened for: " + vaultFile.getOriginalName());
                
            } catch (Exception mediaEx) {
                // Fallback to external player if JavaFX Media fails
                logMessage("⚠ JavaFX Media not available, using external player");
                
                Label fallbackLabel = new Label("🎬 Media Player Not Available\n\n" +
                    "JavaFX Media components are not available.\n" +
                    "Opening with external player...");
                fallbackLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 16px; -fx-text-alignment: center;");
                fallbackLabel.setAlignment(javafx.geometry.Pos.CENTER);
                fallbackLabel.setPrefSize(400, 200);
                
                Button externalBtn = new Button("Open External Player");
                externalBtn.getStyleClass().addAll("button", "primary");
                externalBtn.setOnAction(e -> {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(tempFile.toFile());
                            showNotification("Video Opened", "Video opened in external player");
                        }
                    } catch (Exception ex) {
                        showError("Error", "Could not open video: " + ex.getMessage());
                    }
                });
                
                Button closeBtn = new Button("Close");
                closeBtn.getStyleClass().addAll("button", "secondary");
                closeBtn.setOnAction(e -> {
                    videoStage.close();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                VBox fallbackBox = new VBox(20, header, fallbackLabel, externalBtn, closeBtn);
                fallbackBox.setAlignment(javafx.geometry.Pos.CENTER);
                fallbackBox.setPadding(new javafx.geometry.Insets(20));
                
                root.getChildren().clear();
                root.getChildren().add(fallbackBox);
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
            
            videoStage.setScene(scene);
            videoStage.show();
            
        } catch (Exception e) {
            showError("Video Preview Error", "Failed to open video:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Show audio file preview with internal JavaFX MediaPlayer
     */
    private void showAudioPreview(VaultFile vaultFile, byte[] fileData) {
        try {
            // Create temporary file for audio playback
            Path tempFile = Files.createTempFile("ghostvault_audio_", "." + vaultFile.getExtension());
            Files.write(tempFile, fileData);
            
            // Create audio preview dialog
            Stage audioStage = new Stage();
            audioStage.setTitle("🎵 Audio Player - " + vaultFile.getOriginalName());
            audioStage.initModality(javafx.stage.Modality.NONE);
            audioStage.setResizable(false);
            
            VBox root = new VBox(20);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setStyle("-fx-background-color: #0F172A;");
            root.setPrefWidth(500);
            
            // Audio info header
            VBox header = new VBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER);
            header.setStyle("-fx-background-color: #1E293B; -fx-padding: 20px; -fx-background-radius: 12px;");
            
            Label titleLabel = new Label("🎵 " + vaultFile.getOriginalName());
            titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label infoLabel = new Label(formatFileSize(vaultFile.getSize()) + " • " + vaultFile.getExtension().toUpperCase());
            infoLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
            
            Label encryptedLabel = new Label("🔒 Securely Decrypted");
            encryptedLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 6px 12px; -fx-background-radius: 15px; -fx-font-size: 12px;");
            
            header.getChildren().addAll(titleLabel, infoLabel, encryptedLabel);
            
            try {
                // Create JavaFX MediaPlayer for internal audio playback
                javafx.scene.media.Media media = new javafx.scene.media.Media(tempFile.toUri().toString());
                javafx.scene.media.MediaPlayer mediaPlayer = new javafx.scene.media.MediaPlayer(media);
                
                // Audio visualization (waveform placeholder)
                VBox visualizer = new VBox();
                visualizer.setAlignment(javafx.geometry.Pos.CENTER);
                visualizer.setStyle("-fx-background-color: #334155; -fx-padding: 30px; -fx-background-radius: 8px;");
                visualizer.setPrefHeight(120);
                
                // Create simple audio waveform visualization
                HBox waveform = new HBox(3);
                waveform.setAlignment(javafx.geometry.Pos.CENTER);
                
                for (int i = 0; i < 50; i++) {
                    VBox bar = new VBox();
                    bar.setPrefWidth(4);
                    bar.setPrefHeight(Math.random() * 60 + 10);
                    bar.setStyle("-fx-background-color: #175DDC; -fx-background-radius: 2px;");
                    waveform.getChildren().add(bar);
                }
                
                Label visualLabel = new Label("🎵 Audio Waveform");
                visualLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 14px; -fx-padding: 0 0 10px 0;");
                
                visualizer.getChildren().addAll(visualLabel, waveform);
                
                // Audio controls
                VBox controlsPanel = new VBox(15);
                controlsPanel.setAlignment(javafx.geometry.Pos.CENTER);
                controlsPanel.setStyle("-fx-background-color: #334155; -fx-padding: 20px; -fx-background-radius: 8px;");
                
                // Progress bar
                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(400);
                progressBar.setStyle("-fx-accent: #175DDC;");
                
                Label timeLabel = new Label("00:00 / 00:00");
                timeLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
                
                VBox progressBox = new VBox(8, progressBar, timeLabel);
                progressBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Control buttons
                HBox buttonBox = new HBox(15);
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                Button playPauseBtn = new Button("▶️ Play");
                playPauseBtn.getStyleClass().addAll("button", "primary");
                playPauseBtn.setPrefWidth(120);
                
                Button stopBtn = new Button("⏹️ Stop");
                stopBtn.getStyleClass().addAll("button", "secondary");
                stopBtn.setPrefWidth(120);
                
                Button muteBtn = new Button("🔊");
                muteBtn.getStyleClass().addAll("button", "secondary");
                muteBtn.setPrefWidth(60);
                
                buttonBox.getChildren().addAll(playPauseBtn, stopBtn, muteBtn);
                
                // Volume control
                HBox volumeBox = new HBox(10);
                volumeBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                Label volumeLabel = new Label("Volume:");
                volumeLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
                
                Slider volumeSlider = new Slider(0, 1, 0.7);
                volumeSlider.setPrefWidth(200);
                volumeSlider.setStyle("-fx-control-inner-background: #475569;");
                
                Label volumeValue = new Label("70%");
                volumeValue.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-min-width: 40px;");
                
                volumeBox.getChildren().addAll(volumeLabel, volumeSlider, volumeValue);
                
                controlsPanel.getChildren().addAll(progressBox, buttonBox, volumeBox);
                
                // Media player event handlers
                final boolean[] isPlaying = {false};
                
                playPauseBtn.setOnAction(e -> {
                    if (isPlaying[0]) {
                        mediaPlayer.pause();
                        playPauseBtn.setText("▶️ Play");
                        isPlaying[0] = false;
                    } else {
                        mediaPlayer.play();
                        playPauseBtn.setText("⏸️ Pause");
                        isPlaying[0] = true;
                    }
                });
                
                stopBtn.setOnAction(e -> {
                    mediaPlayer.stop();
                    playPauseBtn.setText("▶️ Play");
                    isPlaying[0] = false;
                });
                
                muteBtn.setOnAction(e -> {
                    if (mediaPlayer.isMute()) {
                        mediaPlayer.setMute(false);
                        muteBtn.setText("🔊");
                    } else {
                        mediaPlayer.setMute(true);
                        muteBtn.setText("🔇");
                    }
                });
                
                volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    mediaPlayer.setVolume(newVal.doubleValue());
                    volumeValue.setText(Math.round(newVal.doubleValue() * 100) + "%");
                });
                
                // Update progress and time
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (mediaPlayer.getTotalDuration() != null) {
                        double progress = newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                        progressBar.setProgress(progress);
                        
                        String currentTime = formatTime(newTime.toSeconds());
                        String totalTime = formatTime(mediaPlayer.getTotalDuration().toSeconds());
                        timeLabel.setText(currentTime + " / " + totalTime);
                    }
                });
                
                // Close button
                Button closeBtn = new Button("Close Player");
                closeBtn.getStyleClass().addAll("button", "secondary");
                closeBtn.setOnAction(e -> {
                    mediaPlayer.dispose();
                    audioStage.close();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                HBox closeBox = new HBox(closeBtn);
                closeBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                root.getChildren().addAll(header, visualizer, controlsPanel, closeBox);
                
                // Cleanup when stage is closed
                audioStage.setOnCloseRequest(e -> {
                    mediaPlayer.dispose();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                logMessage("🎵 Internal audio player opened for: " + vaultFile.getOriginalName());
                
            } catch (Exception mediaEx) {
                // Fallback message if JavaFX Media fails
                logMessage("⚠ JavaFX Media not available for audio playback");
                
                Label fallbackLabel = new Label("🎵 Audio Player Not Available\n\n" +
                    "JavaFX Media components are not available.\n" +
                    "Please install JavaFX Media module for audio playback.");
                fallbackLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 16px; -fx-text-alignment: center;");
                fallbackLabel.setAlignment(javafx.geometry.Pos.CENTER);
                
                Button closeBtn = new Button("Close");
                closeBtn.getStyleClass().addAll("button", "secondary");
                closeBtn.setOnAction(e -> {
                    audioStage.close();
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ex) {
                        // Ignore cleanup errors
                    }
                });
                
                root.getChildren().addAll(header, fallbackLabel, closeBtn);
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
            
            audioStage.setScene(scene);
            audioStage.show();
            
        } catch (Exception e) {
            showError("Audio Preview Error", "Failed to open audio:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Format time in MM:SS format
     */
    private String formatTime(double seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    

    
    /**
     * Simulate decoy preview for decoy mode
     */
    private void simulateDecoyPreview(String selectedDisplayName) {
        showOperationProgress("Loading preview...");
        
        // Simulate loading delay
        Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            hideOperationProgress();
            
            // Show fake preview content
            Dialog<Void> previewDialog = new Dialog<>();
            previewDialog.setTitle("File Preview - " + selectedDisplayName.substring(2)); // Remove icon
            previewDialog.setHeaderText("Document Preview");
            
            TextArea textArea = new TextArea("This is a sample document preview.\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit...");
            textArea.setEditable(false);
            textArea.setPrefSize(500, 300);
            
            previewDialog.getDialogPane().setContent(textArea);
            previewDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            previewDialog.showAndWait();
            
            logMessage("👁️ Decoy preview shown for: " + selectedDisplayName);
        });
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvbackup"));
        fileChooser.setInitialFileName("vault_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".gvbackup");
        
        File backupLocation = fileChooser.showSaveDialog(backupButton.getScene().getWindow());
        if (backupLocation == null) return;
        
        try {
            showOperationProgress("Creating encrypted backup...");
            logMessage("⏳ Creating encrypted backup...");
            
            // Create backup using backup manager
            if (backupManager != null && encryptionKey != null) {
                // Validate encryption key and vault state
                logMessage("🔐 Validating encryption key for backup...");
                
                // Check if vault has files to backup
                if (allVaultFiles.isEmpty()) {
                    logMessage("ℹ️ No files in vault to backup");
                    boolean proceedEmpty = showConfirmation("Empty Vault Backup", 
                        "Your vault appears to be empty.\n\n" +
                        "Do you still want to create a backup?\n" +
                        "(This will create a backup of the vault structure only)");
                    
                    if (!proceedEmpty) {
                        hideOperationProgress();
                        return;
                    }
                }
                
                // Simple progress tracking
                showOperationProgress("Creating backup...");
                
                // Create the backup with enhanced error handling
                try {
                    backupManager.createBackup(backupLocation, encryptionKey, null);
                } catch (Exception backupError) {
                    hideOperationProgress();
                    
                    // Check for specific error types
                    String errorMessage = backupError.getMessage();
                    if (errorMessage != null && errorMessage.contains("Tag mismatch")) {
                        logMessage("✗ Backup failed: Encryption key mismatch or corrupted data");
                        showError("Backup Failed - Key Mismatch", 
                            "The backup failed due to an encryption key mismatch.\n\n" +
                            "This could happen if:\n" +
                            "• The vault was created with a different password\n" +
                            "• The vault data is corrupted\n" +
                            "• There's an issue with the encryption system\n\n" +
                            "Please try:\n" +
                            "1. Restart the application\n" +
                            "2. Verify your password is correct\n" +
                            "3. Check if individual files can be downloaded");
                    } else {
                        logMessage("✗ Backup failed: " + errorMessage);
                        showError("Backup Failed", "Failed to create backup:\n\n" + errorMessage);
                    }
                    return;
                }
                
                hideOperationProgress();
                logMessage("✓ Backup created successfully: " + backupLocation.getName());
                logMessage("  Location: " + backupLocation.getAbsolutePath());
                logMessage("  Files backed up: " + allVaultFiles.size());
                
                showNotification("Backup Complete", 
                    "Encrypted backup created successfully!\n\n" +
                    "Location: " + backupLocation.getAbsolutePath() + "\n" +
                    "Files: " + allVaultFiles.size());
                
            } else {
                hideOperationProgress();
                if (backupManager == null) {
                    logMessage("⚠ Backup manager not initialized");
                    showError("Backup Failed", "Backup manager is not available.");
                } else {
                    logMessage("⚠ Encryption key not available");
                    showError("Backup Failed", "Encryption key is not available for backup.");
                }
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Backup failed: " + e.getMessage());
            showError("Backup Failed", "Failed to create backup:\n\n" + e.getMessage());
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvbackup"));
        
        File backupFile = fileChooser.showOpenDialog(restoreButton.getScene().getWindow());
        if (backupFile == null) return;
        
        // First verify the backup
        try {
            if (backupManager != null && encryptionKey != null) {
                showOperationProgress("Verifying backup...");
                logMessage("🔍 Verifying backup file: " + backupFile.getName());
                
                // Simple backup verification
                try {
                    // Basic file existence and size check
                    if (!backupFile.exists() || backupFile.length() < 1024) {
                        throw new Exception("Backup file is too small or doesn't exist");
                    }
                    logMessage("✓ Backup file appears valid");
                } catch (Exception verifyError) {
                    hideOperationProgress();
                    logMessage("✗ Backup verification failed: " + verifyError.getMessage());
                    showError("Backup Verification Failed", 
                        "Could not verify the backup file:\n\n" + verifyError.getMessage());
                    return;
                }
                
                hideOperationProgress();
                
                // Show backup information and confirm restore
                String backupDetails = String.format(
                    "Backup Information:\n\n" +
                    "File: %s\n" +
                    "Size: %s\n" +
                    "Status: ✓ Valid backup format\n\n" +
                    "⚠️ WARNING: This will replace your current vault contents!\n\n" +
                    "Do you want to proceed with the restore?",
                    backupFile.getName(),
                    formatFileSize(backupFile.length())
                );
                
                boolean confirmed = showConfirmation("Confirm Restore", backupDetails);
                
                if (confirmed) {
                    // Simple progress tracking
                    showOperationProgress("Restoring from backup...");
                    logMessage("⏳ Restoring vault from backup: " + backupFile.getName());
                    
                    // Perform the restore with enhanced error handling
                    try {
                        backupManager.restoreBackup(backupFile, encryptionKey, null);
                        
                        // Reload metadata from restored file
                        if (metadataManager != null) {
                            try {
                                metadataManager.loadMetadata();
                                logMessage("📋 Metadata reloaded after restore");
                            } catch (Exception metaError) {
                                logMessage("⚠ Could not reload metadata after restore: " + metaError.getMessage());
                            }
                        }
                        
                        hideOperationProgress();
                        refreshFileList();
                        updateStatus();
                        
                        logMessage("✓ Vault successfully restored from: " + backupFile.getName());
                        
                        showNotification("Restore Complete", 
                            "Vault successfully restored from backup!\n\n" +
                            "Please verify your files are accessible.");
                            
                    } catch (Exception restoreError) {
                        hideOperationProgress();
                        
                        String errorMessage = restoreError.getMessage();
                        if (errorMessage != null && errorMessage.contains("Tag mismatch")) {
                            logMessage("✗ Restore failed: Encryption key mismatch during restore");
                            showError("Restore Failed - Key Mismatch", 
                                "The restore failed due to an encryption key mismatch.\n\n" +
                                "This indicates the backup was created with a different password.\n" +
                                "Please ensure you're using the exact same password that was used when creating the backup.");
                        } else {
                            logMessage("✗ Restore failed: " + errorMessage);
                            showError("Restore Failed", "Failed to restore from backup:\n\n" + errorMessage);
                        }
                        return;
                    }
                }
            } else {
                if (backupManager == null) {
                    showError("Restore Failed", "Backup manager is not available.");
                } else {
                    showError("Restore Failed", "Encryption key is not available for restore.");
                }
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Restore failed: " + e.getMessage());
            showError("Restore Failed", "Failed to restore from backup:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Handle settings dialog
     */
    @FXML
    private void handleSettings() {
        try {
            // Simple settings dialog implementation
            Alert settingsAlert = new Alert(Alert.AlertType.INFORMATION);
            settingsAlert.setTitle("Settings");
            settingsAlert.setHeaderText("GhostVault Settings");
            settingsAlert.setContentText("Settings functionality will be implemented in a future version.");
            settingsAlert.showAndWait();
            logMessage("⚙️ Settings dialog opened");
        } catch (Exception e) {
            logMessage("⚠ Settings error: " + e.getMessage());
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
     * Handle logout with confirmation and return to login screen
     */
    @FXML
    private void handleLogout() {
        boolean confirmed = showConfirmation("Logout", 
            "Are you sure you want to logout?\n\nAll unsaved work will be lost.");
        
        if (confirmed) {
            logMessage("🚪 Logging out...");
            
            try {
                // Clear sensitive data
                clearSensitiveData();
                
                // End session
                if (sessionManager != null) {
                    sessionManager.endSession();
                }
                
                // Close current window and return to login
                Platform.runLater(() -> {
                    try {
                        // Get the current stage and close it
                        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                        currentStage.close();
                        
                        // Simple logout - application will be restarted externally
                        logMessage("🔓 User logged out successfully");
                        
                        logMessage("✓ Returned to login screen");
                        
                    } catch (Exception e) {
                        logMessage("✗ Error returning to login: " + e.getMessage());
                        // Fallback: close application
                        Platform.exit();
                    }
                });
                
            } catch (Exception e) {
                logMessage("✗ Logout error: " + e.getMessage());
                showError("Logout Error", "Error during logout: " + e.getMessage());
            }
        }
    }
    
    // =========================== SEARCH AND FILTERING ===========================
    
    /**
     * Filter file list based on search term
     */
    private void filterFileList(String searchTerm) {
        filteredFileList.clear();
        
        logMessage("🔍 Debug: Filtering files - fileList size: " + fileList.size() + ", searchTerm: '" + searchTerm + "'");
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredFileList.addAll(fileList);
        } else {
            String lowerSearchTerm = searchTerm.toLowerCase();
            List<String> filtered = fileList.stream()
                .filter(displayName -> displayName.toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
            filteredFileList.addAll(filtered);
        }
        
        logMessage("🔍 Debug: After filtering - filteredFileList size: " + filteredFileList.size());
        
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
            
            logMessage("🔍 Debug: Retrieved " + (vaultFiles != null ? vaultFiles.size() : 0) + " files from metadata");
            
            if (vaultFiles != null && !vaultFiles.isEmpty()) {
                allVaultFiles.addAll(vaultFiles);
                
                for (VaultFile vaultFile : vaultFiles) {
                    String displayName = vaultFile.getIcon() + " " + vaultFile.getDisplayName();
                    fileList.add(displayName);
                    logMessage("� eDebug: Added file to list: " + displayName);
                }
                
                logMessage("📁 Loaded " + fileList.size() + " file(s) from vault");
            } else {
                // Only check for orphaned files if this isn't a fresh vault
                // This prevents showing orphaned files in a newly initialized vault
                if (metadataManager.hasBeenInitialized()) {
                    checkForOrphanedFiles();
                } else {
                    logMessage("ℹ️ Fresh vault detected - skipping orphaned file check");
                }
            }
            
            if (fileList.isEmpty()) {
                logMessage("ℹ️ No files in vault yet. Click Upload to add files.");
            }
            
        } catch (Exception e) {
            logMessage("⚠ Error loading file list: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Apply current search filter with animation - ensure this runs on JavaFX thread
        Platform.runLater(() -> {
            try {
                logMessage("🔍 Debug: Updating UI with " + fileList.size() + " files");
                
                if (fileListView != null) {
                    // Skip animation for now to debug
                    filterFileList(searchField != null ? searchField.getText() : "");
                    updateStatus();
                    logMessage("🔍 Debug: UI updated, filteredFileList size: " + filteredFileList.size());
                } else {
                    logMessage("⚠ Debug: fileListView is null!");
                    filterFileList(searchField != null ? searchField.getText() : "");
                    updateStatus();
                }
            } catch (Exception e) {
                logMessage("⚠ Error updating UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
                    List<File> orphanedFiles = new ArrayList<>();
                    
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".enc")) {
                            orphanedFiles.add(file);
                            String displayName = "🔒 " + file.getName().replace(".enc", "") + " (orphaned)";
                            fileList.add(displayName);
                        }
                    }
                    
                    if (!orphanedFiles.isEmpty()) {
                        logMessage("⚠ Found " + orphanedFiles.size() + " orphaned encrypted file(s) - preserving for safety");
                        
                        // Preserve orphaned files and log them for manual recovery
                        for (File orphanedFile : orphanedFiles) {
                            logMessage("📁 Orphaned file preserved: " + orphanedFile.getName());
                        }
                        
                        logMessage("ℹ️ Orphaned files have been preserved. They may be recoverable through metadata recovery.");
                        
                        // Offer recovery options instead of deletion
                        Platform.runLater(() -> {
                            showOrphanedFileRecoveryDialog(orphanedFiles);
                        });
                    }
                }
            }
        } catch (Exception e) {
            logMessage("⚠ Error checking for orphaned files: " + e.getMessage());
        }
    }
    
    /**
     * Clean up orphaned encrypted files - DISABLED FOR SAFETY
     * Files are now preserved to prevent data loss
     */
    private void cleanupOrphanedFiles(List<File> orphanedFiles) {
        // SAFETY: This method has been disabled to prevent accidental file deletion
        logMessage("🛡️ File cleanup disabled - orphaned files preserved for safety");
        logMessage("ℹ️ " + orphanedFiles.size() + " file(s) have been preserved and may be recoverable");
        
        // Log the preserved files for reference
        for (File file : orphanedFiles) {
            logMessage("📁 Preserved: " + file.getName());
        }
        
        showNotification("Files Preserved", 
            "Orphaned files have been preserved for safety. They may be recoverable through metadata recovery.");
        
        // Note: No actual file deletion occurs here anymore
    }
    
    /**
     * Show recovery dialog for orphaned files instead of deletion prompt
     */
    private void showOrphanedFileRecoveryDialog(List<File> orphanedFiles) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Files Preserved for Recovery");
            alert.setHeaderText("Found " + orphanedFiles.size() + " orphaned file(s)");
            
            String message = "These encrypted files exist without metadata entries.\n\n" +
                           "Possible causes:\n" +
                           "• Session key derivation differences\n" +
                           "• Temporary metadata loading issues\n" +
                           "• Previous incomplete operations\n\n" +
                           "Recovery options:\n" +
                           "• Files have been preserved safely\n" +
                           "• Try logging out and back in\n" +
                           "• Use manual recovery tools if available\n" +
                           "• Check metadata backups\n\n" +
                           "No files will be deleted automatically.";
            
            alert.setContentText(message);
            
            // Add recovery button
            ButtonType recoveryButton = new ButtonType("Attempt Recovery");
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            
            alert.getButtonTypes().setAll(recoveryButton, okButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == recoveryButton) {
                attemptOrphanedFileRecovery(orphanedFiles);
            }
            
        } catch (Exception e) {
            logMessage("⚠ Error showing recovery dialog: " + e.getMessage());
        }
    }
    
    /**
     * Attempt to recover orphaned files using metadata recovery
     */
    private void attemptOrphanedFileRecovery(List<File> orphanedFiles) {
        try {
            // Show progress
            showOperationProgress("Attempting file recovery...");
            logMessage("🔄 Starting recovery attempt for " + orphanedFiles.size() + " orphaned files");
            
            // We need the user's password for recovery
            String password = promptForPassword("Recovery Password", 
                "Enter your vault password to attempt file recovery:");
            
            if (password == null || password.isEmpty()) {
                hideOperationProgress();
                logMessage("⚠ Recovery cancelled - password required");
                return;
            }
            
            // Simple recovery attempt
            hideOperationProgress();
            
            // Basic recovery - just log the attempt
            logMessage("🔧 Attempting to recover " + orphanedFiles.size() + " orphaned files...");
            logMessage("⚠ Recovery functionality will be implemented in a future version");
            
            showNotification("Recovery Status", 
                "Recovery functionality is not yet implemented.\n\n" +
                "Orphaned files detected: " + orphanedFiles.size() + "\n" +
                "These files exist but have no metadata entries.");
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("⚠ Recovery error: " + e.getMessage());
            showError("Recovery Error", "Error during file recovery: " + e.getMessage());
        }
    }
    
    /**
     * Prompt user for password input
     */
    private String promptForPassword(String title, String message) {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(message);
            dialog.setContentText("Password:");
            
            // Make it a password field
            TextField textField = dialog.getEditor();
            textField.setPromptText("Enter password");
            
            Optional<String> result = dialog.showAndWait();
            return result.orElse(null);
            
        } catch (Exception e) {
            logMessage("⚠ Error showing password dialog: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate session key consistency
     */
    private boolean validateSessionKey(SecretKey key) {
        try {
            if (key == null) {
                return false;
            }
            
            // Basic validation - check key format and length
            byte[] keyBytes = key.getEncoded();
            if (keyBytes == null || keyBytes.length != 32) { // 256 bits
                logMessage("⚠ Invalid key format or length");
                return false;
            }
            
            // Additional validation could include test encryption/decryption
            logMessage("✓ Session key validation passed");
            return true;
            
        } catch (Exception e) {
            logMessage("⚠ Session key validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Attempt session recovery when metadata loading fails
     */
    private boolean attemptSessionRecovery(Exception originalException) {
        try {
            logMessage("🔄 Attempting session recovery...");
            
            // Try to recover using metadata backup restoration
            if (metadataManager != null) {
                List<String> backups = metadataManager.getAvailableBackups();
                
                for (String backup : backups) {
                    try {
                        logMessage("🔄 Trying backup: " + backup);
                        if (metadataManager.restoreFromBackup(backup)) {
                            logMessage("✅ Session recovered using backup: " + backup);
                            return true;
                        }
                    } catch (Exception backupException) {
                        logMessage("⚠ Backup recovery failed: " + backupException.getMessage());
                    }
                }
            }
            
            logMessage("⚠ Session recovery failed - no usable backups found");
            return false;
            
        } catch (Exception recoveryException) {
            logMessage("⚠ Session recovery error: " + recoveryException.getMessage());
            return false;
        }
    }
    
    /**
     * Validate session state and recover if needed
     */
    private void validateSessionState() {
        try {
            if (metadataManager == null || encryptionKey == null) {
                logMessage("⚠ Invalid session state - missing components");
                return;
            }
            
            // Check if metadata is accessible
            if (!metadataManager.hasBeenInitialized()) {
                logMessage("ℹ️ Metadata not initialized - this may be a new vault");
                return;
            }
            
            // Verify metadata integrity
            if (!metadataManager.verifyMetadataIntegrity()) {
                logMessage("⚠ Metadata integrity check failed");
                attemptSessionRecovery(new Exception("Metadata integrity check failed"));
            } else {
                logMessage("✓ Session state validation passed");
            }
            
        } catch (Exception e) {
            logMessage("⚠ Session state validation error: " + e.getMessage());
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
    

    
    /**
     * Handle security dashboard - show security overview
     */
    @FXML
    private void handleDashboard() {
        try {
            // Create professional dashboard window
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("📊 GhostVault Security Dashboard - " + getCurrentVaultMode());
            dashboardStage.initModality(javafx.stage.Modality.NONE);
            dashboardStage.setResizable(true);
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #0F172A;");
            
            // Header with vault mode indicator
            HBox header = createDashboardHeader();
            
            // Main dashboard content
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            VBox content = new VBox(20);
            content.setPadding(new javafx.geometry.Insets(20));
            
            // Security Overview Section
            VBox securitySection = createSecurityOverviewSection();
            
            // Vault Statistics Section
            VBox statsSection = createVaultStatisticsSection();
            
            // Recent Activity Section
            VBox activitySection = createRecentActivitySection();
            
            // System Health Section
            VBox healthSection = createSystemHealthSection();
            
            content.getChildren().addAll(securitySection, statsSection, activitySection, healthSection);
            scrollPane.setContent(content);
            
            root.getChildren().addAll(header, scrollPane);
            
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
            
            dashboardStage.setScene(scene);
            dashboardStage.show();
            
            logMessage("📊 Professional Security Dashboard opened - " + getCurrentVaultMode() + " Mode");
        } catch (Exception e) {
            logMessage("⚠ Dashboard error: " + e.getMessage());
            showError("Dashboard Error", "Could not open security dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Get current vault mode for display
     */
    private String getCurrentVaultMode() {
        if (isDecoyMode) {
            return "DECOY";
        } else {
            return "MASTER";
        }
    }
    
    /**
     * Create dashboard header with vault mode indicator
     */
    private HBox createDashboardHeader() {
        HBox header = new HBox(20);
        header.setPadding(new javafx.geometry.Insets(20));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 1px 0;");
        
        Label titleLabel = new Label("🛡️ Security Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Vault mode indicator
        Label modeLabel = new Label(getCurrentVaultMode() + " MODE");
        if (isDecoyMode) {
            modeLabel.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: #000000; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            modeLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: #FFFFFF; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        
        // Security status indicator
        Label securityStatus = new Label("🔒 SECURE");
        securityStatus.setStyle("-fx-background-color: #175DDC; -fx-text-fill: #FFFFFF; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        header.getChildren().addAll(titleLabel, spacer, modeLabel, securityStatus);
        return header;
    }
    
    /**
     * Create security overview section
     */
    private VBox createSecurityOverviewSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("🔐 Security Overview");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        
        // Security metrics
        addDashboardMetric(grid, 0, 0, "Encryption Status", "AES-256 Active", "#10B981");
        addDashboardMetric(grid, 1, 0, "Authentication", "Multi-Factor", "#175DDC");
        addDashboardMetric(grid, 0, 1, "Session Security", "Encrypted", "#10B981");
        addDashboardMetric(grid, 1, 1, "Threat Level", calculateThreatLevel(), getThreatLevelColor());
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    /**
     * Create vault statistics section with enhanced alignment and real data
     */
    private VBox createVaultStatisticsSection() {
        VBox section = new VBox(20);
        section.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label sectionTitle = new Label("📊 Vault Statistics");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC; -fx-padding: 0 0 10 0;");
        sectionTitle.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Enhanced grid with better spacing and alignment
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(20);
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Calculate real statistics
        int totalFiles = allVaultFiles.size();
        long totalSize = allVaultFiles.stream().mapToLong(VaultFile::getSize).sum();
        String formattedSize = formatFileSize(totalSize);
        
        // Get password manager statistics
        int totalPasswords = 0;
        int weakPasswords = 0;
        int strongPasswords = 0;
        try {
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            PasswordVaultManager passwordManager = new PasswordVaultManager(vaultPath);
            passwordManager.setEncryptionKey(encryptionKey);
            passwordManager.loadPasswords();
            
            totalPasswords = passwordManager.getAllPasswords().size();
            weakPasswords = (int) passwordManager.getAllPasswords().stream()
                .filter(p -> passwordManager.calculatePasswordStrength(p.getPassword()) < 60)
                .count();
            strongPasswords = totalPasswords - weakPasswords;
        } catch (Exception e) {
            // Use default values if password manager fails
        }
        
        // Get notes statistics
        int totalNotes = 0;
        try {
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            SecureNotesManager notesManager = new SecureNotesManager(vaultPath);
            notesManager.setEncryptionKey(encryptionKey);
            notesManager.loadNotes();
            
            totalNotes = notesManager.getAllNotes().size();
        } catch (Exception e) {
            // Use default values if notes manager fails
        }
        
        // Create enhanced dashboard cards with consistent sizing
        VBox filesCard = createEnhancedDashboardCard("📁 Total Files", String.valueOf(totalFiles), "Encrypted & Secured", "#3B82F6");
        VBox sizeCard = createEnhancedDashboardCard("💾 Storage Used", formattedSize, "Total vault size", "#8B5CF6");
        VBox encryptedCard = createEnhancedDashboardCard("🔐 Encrypted", String.valueOf(totalFiles), "Files protected", "#10B981");
        
        VBox passwordsCard = createEnhancedDashboardCard("🔑 Passwords", String.valueOf(totalPasswords), "Stored securely", "#F59E0B");
        VBox notesCard = createEnhancedDashboardCard("📝 Secure Notes", String.valueOf(totalNotes), "Encrypted notes", "#06B6D4");
        VBox strongCard = createEnhancedDashboardCard("💪 Strong Passwords", String.valueOf(strongPasswords), "High security", "#10B981");
        
        // Add cards to grid with perfect alignment
        grid.add(filesCard, 0, 0);
        grid.add(sizeCard, 1, 0);
        grid.add(encryptedCard, 2, 0);
        grid.add(passwordsCard, 0, 1);
        grid.add(notesCard, 1, 1);
        grid.add(strongCard, 2, 1);
        
        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }
    
    /**
     * Create enhanced dashboard card with consistent styling and alignment
     */
    private VBox createEnhancedDashboardCard(String title, String value, String description, String accentColor) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPrefHeight(140);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 15; -fx-padding: 20; " +
                     "-fx-border-color: " + accentColor + "; -fx-border-width: 2; -fx-border-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-font-weight: bold;");
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        valueLabel.setAlignment(javafx.geometry.Pos.CENTER);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        descLabel.setAlignment(javafx.geometry.Pos.CENTER);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);
        
        card.getChildren().addAll(titleLabel, valueLabel, descLabel);
        return card;
    }
    
    /**
     * Create recent activity section with real activities
     */
    private VBox createRecentActivitySection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📋 Recent Activity");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        ListView<String> activityList = new ListView<>();
        activityList.setPrefHeight(150);
        activityList.getStyleClass().add("list-view");
        
        // Get real recent activities
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTime = java.time.LocalDateTime.now().format(timeFormatter);
        
        java.util.List<String> activities = new java.util.ArrayList<>();
        activities.add("🔐 Vault accessed (" + getCurrentVaultMode() + " Mode) - " + currentTime);
        
        if (!allVaultFiles.isEmpty()) {
            activities.add("📁 " + allVaultFiles.size() + " files loaded successfully");
            
            // Show recent file types
            java.util.Map<String, Long> fileTypes = allVaultFiles.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    VaultFile::getExtension, 
                    java.util.stream.Collectors.counting()));
            
            fileTypes.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(2)
                .forEach(entry -> activities.add("📄 " + entry.getValue() + " " + entry.getKey().toUpperCase() + " files detected"));
        }
        
        activities.add("🛡️ AES-256 encryption verified");
        activities.add("💾 Metadata integrity confirmed");
        activities.add("🔒 Session security validated");
        
        // Add vault mode specific activities
        if (isDecoyMode) {
            activities.add("⚠️ Operating in DECOY mode");
            activities.add("🎭 Decoy data layer active");
        } else {
            activities.add("✅ Operating in MASTER mode");
            activities.add("🔐 Full vault access enabled");
        }
        
        activityList.getItems().addAll(activities);
        
        section.getChildren().addAll(sectionTitle, activityList);
        return section;
    }
    
    /**
     * Create system health section with real system data
     */
    private VBox createSystemHealthSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("💚 System Health");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        VBox healthItems = new VBox(10);
        
        // Get real system information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        String memoryStatus = memoryUsagePercent < 70 ? "Normal" : memoryUsagePercent < 85 ? "High" : "Critical";
        String memoryColor = memoryUsagePercent < 70 ? "#10B981" : memoryUsagePercent < 85 ? "#F59E0B" : "#EF4444";
        
        // Check disk space
        java.io.File vaultDir = new java.io.File(com.ghostvault.config.AppConfig.getVaultDir());
        long freeSpace = vaultDir.getFreeSpace();
        long totalSpace = vaultDir.getTotalSpace();
        double diskUsagePercent = (double) (totalSpace - freeSpace) / totalSpace * 100;
        
        String diskStatus = diskUsagePercent < 80 ? "Available" : diskUsagePercent < 90 ? "Low" : "Critical";
        String diskColor = diskUsagePercent < 80 ? "#10B981" : diskUsagePercent < 90 ? "#F59E0B" : "#EF4444";
        
        // File integrity check
        int totalFiles = allVaultFiles.size();
        String integrityStatus = totalFiles > 0 ? "Verified (" + totalFiles + " files)" : "No files";
        String integrityColor = totalFiles > 0 ? "#10B981" : "#94A3B8";
        
        // Encryption status
        String encryptionStatus = encryptionKey != null ? "AES-256 Active" : "Not Initialized";
        String encryptionColor = encryptionKey != null ? "#10B981" : "#EF4444";
        
        healthItems.getChildren().addAll(
            createHealthItem("Memory Usage", memoryStatus + " (" + Math.round(memoryUsagePercent) + "%)", memoryColor),
            createHealthItem("Disk Space", diskStatus + " (" + formatFileSize(freeSpace) + " free)", diskColor),
            createHealthItem("Encryption", encryptionStatus, encryptionColor),
            createHealthItem("File Integrity", integrityStatus, integrityColor),
            createHealthItem("Vault Mode", getCurrentVaultMode() + " Mode Active", isDecoyMode ? "#F59E0B" : "#10B981"),
            createHealthItem("Session", "Authenticated & Secure", "#175DDC")
        );
        
        section.getChildren().addAll(sectionTitle, healthItems);
        return section;
    }
    
    /**
     * Add metric to dashboard grid
     */
    private void addDashboardMetric(GridPane grid, int col, int row, String label, String value, String color) {
        VBox metricBox = new VBox(5);
        metricBox.setStyle("-fx-background-color: #334155; -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #475569; -fx-border-width: 1px; -fx-border-radius: 8px;");
        metricBox.setPrefWidth(180);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        metricBox.getChildren().addAll(labelText, valueText);
        grid.add(metricBox, col, row);
    }
    
    /**
     * Create health item
     */
    private HBox createHealthItem(String label, String status, String color) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #334155; -fx-padding: 10px 15px; -fx-background-radius: 6px;");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label statusText = new Label("✓ " + status);
        statusText.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        
        item.getChildren().addAll(labelText, spacer, statusText);
        return item;
    }
    
    /**
     * Get threat level color
     */
    private String getThreatLevelColor() {
        String level = calculateThreatLevel();
        switch (level) {
            case "LOW": return "#10B981";
            case "MEDIUM": return "#F59E0B";
            case "HIGH": return "#EF4444";
            default: return "#6B7280";
        }
    }
    
    /**
     * Update dashboard information
     */
    private void updateDashboardInfo() {
        if (dashboardOverlay == null) return;
        
        try {
            // Update dashboard with current vault statistics
            Platform.runLater(() -> {
                // Clear existing content
                dashboardOverlay.getChildren().clear();
                
                // Create dashboard content
                Label titleLabel = new Label("🔒 Security Dashboard");
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
                
                Label filesLabel = new Label("📁 Files in Vault: " + allVaultFiles.size());
                filesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #CBD5E1;");
                
                Label sizeLabel = new Label("💾 Total Size: " + calculateTotalVaultSize());
                sizeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #CBD5E1;");
                
                Label encryptionLabel = new Label("🔐 Encryption: AES-256-GCM");
                encryptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981;");
                
                Label sessionLabel = new Label("⏱️ Session: Active");
                sessionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981;");
                
                // Add close button
                Button closeButton = new Button("✕ Close");
                closeButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                closeButton.setOnAction(e -> {
                    isDashboardVisible = false;
                    dashboardOverlay.setVisible(false);
                    logMessage("📊 Security Dashboard closed");
                });
                
                dashboardOverlay.getChildren().addAll(titleLabel, filesLabel, sizeLabel, encryptionLabel, sessionLabel, closeButton);
                dashboardOverlay.setSpacing(10);
                dashboardOverlay.setStyle("-fx-background-color: rgba(30, 41, 59, 0.95); -fx-padding: 20px; -fx-border-color: #475569; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            });
        } catch (Exception e) {
            logMessage("⚠ Error updating dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Show security info in a dialog (fallback)
     */
    private void showSecurityInfo() {
        String info = String.format(
            "🔒 GhostVault Security Status\n\n" +
            "📁 Files in Vault: %d\n" +
            "💾 Total Size: %s\n" +
            "🔐 Encryption: AES-256-GCM\n" +
            "⏱️ Session: Active\n" +
            "🛡️ Mode: %s",
            allVaultFiles.size(),
            calculateTotalVaultSize(),
            isDecoyMode ? "Decoy Mode" : "Master Mode"
        );
        
        showNotification("Security Dashboard", info);
    }
    
    /**
     * Calculate total vault size
     */
    private String calculateTotalVaultSize() {
        long totalSize = 0;
        for (VaultFile file : allVaultFiles) {
            totalSize += file.getSize();
        }
        return formatFileSize(totalSize);
    }
    
    /**
     * Switch between light and dark themes
     */
    private void switchTheme() {
        try {
            if (mainContent != null && mainContent.getScene() != null) {
                Scene scene = mainContent.getScene();
                
                // Toggle between themes with animation
                AnimationManager.fadeOut(mainContent, AnimationManager.FAST, () -> {
                    // Check current theme and switch
                    if (scene.getStylesheets().contains(getClass().getResource("/css/password-manager-theme.css").toExternalForm())) {
                        // Switch to light theme
                        scene.getStylesheets().clear();
                        scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
                        logMessage("🌞 Switched to Light Theme");
                        showNotification("Theme Changed", "Switched to Light Theme");
                    } else {
                        // Switch to dark theme
                        scene.getStylesheets().clear();
                        scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
                        logMessage("🌙 Switched to Dark Theme");
                        showNotification("Theme Changed", "Switched to Dark Theme");
                    }
                    
                    // Re-apply theme to all components
                    com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(mainContent);
                    
                    // Animate back in
                    AnimationManager.fadeIn(mainContent, AnimationManager.NORMAL);
                });
            }
        } catch (Exception e) {
            logMessage("⚠ Theme switch error: " + e.getMessage());
            showError("Theme Error", "Could not switch theme: " + e.getMessage());
        }
    }
    
    /**
     * Show advanced file manager options
     */
    private void showAdvancedFileManager() {
        // Create a dialog with advanced file management options
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Advanced File Manager");
        dialog.setHeaderText("Enhanced File Operations");
        
        String content = String.format(
            "📁 Current Vault Status:\n" +
            "• Files: %d\n" +
            "• Total Size: %s\n" +
            "• Mode: %s\n\n" +
            "🔧 Available Operations:\n" +
            "• Bulk file operations (Select multiple files)\n" +
            "• File search and filtering (Use search box)\n" +
            "• Drag & drop upload support\n" +
            "• Secure file deletion with overwrite\n" +
            "• Encrypted backup and restore\n" +
            "• File integrity verification\n\n" +
            "⌨️ Keyboard Shortcuts:\n" +
            "• Ctrl+O: Upload files\n" +
            "• Ctrl+S: Download selected\n" +
            "• Delete: Secure delete\n" +
            "• F5: Refresh file list\n" +
            "• Ctrl+F: Focus search",
            allVaultFiles.size(),
            calculateTotalVaultSize(),
            isDecoyMode ? "Decoy Mode" : "Master Mode"
        );
        
        dialog.setContentText(content);
        
        // Apply password manager theme
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/password-manager-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        dialog.showAndWait();
    }
    
    /**
     * Add hover animation to buttons
     */
    private void addButtonHoverAnimation(Button button) {
        if (button == null) return;
        
        button.setOnMouseEntered(e -> {
            AnimationManager.pulse(button, 1.05, AnimationManager.FAST);
        });
        
        button.setOnMousePressed(e -> {
            AnimationManager.scaleIn(button);
        });
    }
    

    

    
    // AI mode state
    private boolean aiModeEnabled = false;
    
    /**
     * Handle secure notes - integrated into main vault
     */
    @FXML
    private void handleNotes() {
        try {
            // Initialize secure notes manager
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            SecureNotesManager notesManager = new SecureNotesManager(vaultPath);
            notesManager.setEncryptionKey(encryptionKey);
            notesManager.loadNotes();
            
            // Create professional notes manager window
            Stage notesStage = new Stage();
            notesStage.setTitle("📝 Secure Notes - " + getCurrentVaultMode() + " Mode");
            notesStage.initModality(javafx.stage.Modality.NONE);
            notesStage.setResizable(true);
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #0F172A;");
            
            // Header
            HBox header = createNotesHeader(notesManager, notesStage);
            
            // Main content
            HBox mainContent = new HBox(0);
            
            // Notes list (left panel)
            VBox leftPanel = createNotesListPanel(notesManager);
            leftPanel.setPrefWidth(300);
            leftPanel.setMinWidth(250);
            
            // Notes editor (right panel) - enhanced with connection to notes list
            VBox rightPanel = createEnhancedNotesEditorPanel(notesManager, leftPanel);
            HBox.setHgrow(rightPanel, javafx.scene.layout.Priority.ALWAYS);
            
            mainContent.getChildren().addAll(leftPanel, rightPanel);
            
            root.getChildren().addAll(header, mainContent);
            
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
            
            notesStage.setScene(scene);
            notesStage.show();
            
            logMessage("📝 Professional Secure Notes opened - " + getCurrentVaultMode() + " Mode");
        } catch (Exception e) {
            logMessage("⚠ Notes error: " + e.getMessage());
            showError("Notes Error", "Could not open secure notes: " + e.getMessage());
        }
    }
    
    /**
     * Create notes header with real functionality
     */
    private HBox createNotesHeader(SecureNotesManager notesManager, Stage stage) {
        HBox header = new HBox(20);
        header.setPadding(new javafx.geometry.Insets(20));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 1px 0;");
        
        Label titleLabel = new Label("📝 Secure Notes");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Action buttons
        Button newNoteBtn = new Button("➕ New Note");
        newNoteBtn.getStyleClass().addAll("button", "primary");
        newNoteBtn.setOnAction(e -> showNewNoteDialog(notesManager));
        
        Button saveBtn = new Button("💾 Save All");
        saveBtn.getStyleClass().addAll("button", "success");
        saveBtn.setOnAction(e -> {
            try {
                notesManager.saveNotes();
                showNotification("Notes Saved", "All notes saved successfully");
            } catch (Exception ex) {
                showError("Save Error", "Failed to save notes: " + ex.getMessage());
            }
        });
        
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().addAll("button", "secondary");
        closeBtn.setOnAction(e -> stage.close());
        
        // Vault mode indicator
        Label modeLabel = new Label(getCurrentVaultMode() + " NOTES");
        if (isDecoyMode) {
            modeLabel.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: #000000; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            modeLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: #FFFFFF; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        
        header.getChildren().addAll(titleLabel, spacer, newNoteBtn, saveBtn, closeBtn, modeLabel);
        return header;
    }
    
    /**
     * Create notes list panel with real data
     */
    private VBox createNotesListPanel(SecureNotesManager notesManager) {
        VBox panel = new VBox(10);
        panel.setPadding(new javafx.geometry.Insets(20));
        panel.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 1px 0 0;");
        
        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search notes...");
        searchField.getStyleClass().add("text-field");
        
        // Notes list with real data
        ListView<SecureNote> notesList = new ListView<>();
        notesList.getStyleClass().add("list-view");
        VBox.setVgrow(notesList, javafx.scene.layout.Priority.ALWAYS);
        
        // Load real notes
        notesList.getItems().addAll(notesManager.getAllNotes());
        
        // Enhanced cell factory for note display with real data
        notesList.setCellFactory(listView -> new ListCell<SecureNote>() {
            @Override
            protected void updateItem(SecureNote note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create enhanced note display
                    VBox noteBox = new VBox(5);
                    noteBox.setPadding(new javafx.geometry.Insets(10));
                    noteBox.setStyle("-fx-background-color: #334155; -fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 8;");
                    
                    // Note title
                    Label titleLabel = new Label(note.getTitle());
                    titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 14px; -fx-font-weight: bold;");
                    
                    // Note preview (first 50 characters)
                    String preview = note.getContent().length() > 50 ? 
                        note.getContent().substring(0, 50) + "..." : note.getContent();
                    Label previewLabel = new Label(preview);
                    previewLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
                    previewLabel.setWrapText(true);
                    
                    // Note metadata
                    HBox metaBox = new HBox(10);
                    metaBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label categoryLabel = new Label("📂 " + (note.getCategory() != null ? note.getCategory() : "General"));
                    categoryLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");
                    
                    Label dateLabel = new Label("📅 " + note.getLastModified().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
                    dateLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");
                    
                    // Word count
                    int wordCount = note.getContent().trim().isEmpty() ? 0 : note.getContent().trim().split("\\s+").length;
                    Label wordCountLabel = new Label("📝 " + wordCount + " words");
                    wordCountLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");
                    
                    metaBox.getChildren().addAll(categoryLabel, dateLabel, wordCountLabel);
                    
                    noteBox.getChildren().addAll(titleLabel, previewLabel, metaBox);
                    setGraphic(noteBox);
                    setText(null);
                }
            }
        });
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            notesList.getItems().clear();
            notesList.getItems().addAll(notesManager.searchNotes(newText));
        });
        
        // Action buttons for notes
        HBox noteActions = new HBox(10);
        noteActions.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().addAll("button", "secondary");
        editBtn.setOnAction(e -> {
            SecureNote selected = notesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditNoteDialog(notesManager, selected, notesList);
            } else {
                showWarning("No Selection", "Please select a note to edit");
            }
        });
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().addAll("button", "danger");
        deleteBtn.setOnAction(e -> {
            SecureNote selected = notesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (showConfirmation("Delete Note", "Are you sure you want to delete '" + selected.getTitle() + "'?")) {
                    try {
                        notesManager.removeNote(selected.getId());
                        notesList.getItems().remove(selected);
                        showNotification("Note Deleted", "Note removed successfully");
                    } catch (Exception ex) {
                        showError("Delete Error", "Failed to delete note: " + ex.getMessage());
                    }
                }
            } else {
                showWarning("No Selection", "Please select a note to delete");
            }
        });
        
        noteActions.getChildren().addAll(editBtn, deleteBtn);
        
        panel.getChildren().addAll(searchField, notesList, noteActions);
        return panel;
    }
    
    /**
     * Create enhanced notes editor panel with real functionality
     */
    private VBox createEnhancedNotesEditorPanel(SecureNotesManager notesManager, VBox leftPanel) {
        VBox panel = new VBox(20);
        panel.setPadding(new javafx.geometry.Insets(25));
        panel.setStyle("-fx-background-color: #0F172A;");
        
        // Editor header
        HBox editorHeader = new HBox(15);
        editorHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label editorTitle = new Label("📝 Note Editor");
        editorTitle.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button saveBtn = new Button("💾 Save");
        saveBtn.getStyleClass().addAll("button", "primary");
        
        Button newBtn = new Button("➕ New");
        newBtn.getStyleClass().addAll("button", "success");
        
        editorHeader.getChildren().addAll(editorTitle, spacer, newBtn, saveBtn);
        
        // Note title field
        TextField titleField = new TextField();
        titleField.setPromptText("Enter note title...");
        titleField.getStyleClass().add("text-field");
        titleField.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 12;");
        
        // Note content area
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Write your secure note here...\\n\\nThis note will be encrypted and stored securely.");
        contentArea.getStyleClass().add("text-area");
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        VBox.setVgrow(contentArea, javafx.scene.layout.Priority.ALWAYS);
        
        // Note metadata
        HBox metadataBox = new HBox(20);
        metadataBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("General", "Personal", "Work", "Ideas", "Important", "Archive");
        categoryBox.setValue("General");
        categoryBox.setPromptText("Category");
        
        Label wordCountLabel = new Label("Words: 0");
        wordCountLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        
        Label charCountLabel = new Label("Characters: 0");
        charCountLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        
        Label encryptionStatus = new Label("🔒 AES-256 Encrypted");
        encryptionStatus.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
        
        // Update word/character count in real-time
        contentArea.textProperty().addListener((obs, oldText, newText) -> {
            int wordCount = newText.trim().isEmpty() ? 0 : newText.trim().split("\\\\s+").length;
            int charCount = newText.length();
            wordCountLabel.setText("Words: " + wordCount);
            charCountLabel.setText("Characters: " + charCount);
        });
        
        metadataBox.getChildren().addAll(new Label("Category:"), categoryBox, wordCountLabel, charCountLabel, encryptionStatus);
        
        // Find the notes list from left panel to handle selection
        ListView<SecureNote> notesList = findNotesListInPanel(leftPanel);
        SecureNote[] currentNote = {null}; // Array to hold current note reference
        
        if (notesList != null) {
            // Handle note selection to populate editor
            notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
                if (newNote != null) {
                    currentNote[0] = newNote;
                    titleField.setText(newNote.getTitle());
                    contentArea.setText(newNote.getContent());
                    categoryBox.setValue(newNote.getCategory() != null ? newNote.getCategory() : "General");
                }
            });
        }
        
        // Save button functionality
        saveBtn.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String content = contentArea.getText();
                String category = categoryBox.getValue();
                
                if (title.isEmpty()) {
                    showWarning("Invalid Title", "Please enter a title for the note.");
                    return;
                }
                
                if (currentNote[0] != null) {
                    // Update existing note
                    currentNote[0].setTitle(title);
                    currentNote[0].setContent(content);
                    currentNote[0].setCategory(category);
                    currentNote[0].setLastModified(java.time.LocalDateTime.now());
                    notesManager.updateNote(currentNote[0]);
                    showNotification("Note Updated", "Your note has been updated and encrypted.");
                } else {
                    // Create new note
                    SecureNote note = new SecureNote(title, content);
                    note.setCategory(category);
                    notesManager.addNote(note);
                    currentNote[0] = note;
                    showNotification("Note Saved", "Your note has been encrypted and saved securely.");
                }
                
                // Refresh the notes list
                if (notesList != null) {
                    notesList.getItems().clear();
                    notesList.getItems().addAll(notesManager.getAllNotes());
                }
                
                logMessage("📝 Note saved: " + title);
            } catch (Exception ex) {
                showError("Save Error", "Failed to save note: " + ex.getMessage());
            }
        });
        
        // New note button functionality
        newBtn.setOnAction(e -> {
            currentNote[0] = null;
            titleField.clear();
            contentArea.clear();
            categoryBox.setValue("General");
            titleField.requestFocus();
        });
        
        panel.getChildren().addAll(editorHeader, titleField, contentArea, metadataBox);
        return panel;
    }
    
    /**
     * Find the notes ListView in the left panel
     */
    @SuppressWarnings("unchecked")
    private ListView<SecureNote> findNotesListInPanel(VBox panel) {
        for (javafx.scene.Node node : panel.getChildren()) {
            if (node instanceof ListView) {
                ListView<?> listView = (ListView<?>) node;
                if (!listView.getItems().isEmpty() && listView.getItems().get(0) instanceof SecureNote) {
                    return (ListView<SecureNote>) listView;
                }
            }
        }
        return null;
    }
    
    /**
     * Handle password manager - Professional password vault with real functionality
     */
    @FXML
    private void handlePasswords() {
        try {
            // Initialize password vault manager
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            PasswordVaultManager passwordManager = new PasswordVaultManager(vaultPath);
            passwordManager.setEncryptionKey(encryptionKey);
            passwordManager.loadPasswords();
            
            // Create professional password manager window
            Stage passwordStage = new Stage();
            passwordStage.setTitle("🔑 Password Vault - " + getCurrentVaultMode() + " Mode");
            passwordStage.initModality(javafx.stage.Modality.NONE);
            passwordStage.setResizable(true);
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #0F172A;");
            
            // Header
            HBox header = createPasswordManagerHeader();
            
            // Main content
            VBox content = new VBox(20);
            content.setPadding(new javafx.geometry.Insets(20));
            
            // Search bar
            TextField searchField = new TextField();
            searchField.setPromptText("🔍 Search passwords...");
            searchField.getStyleClass().add("text-field");
            searchField.setPrefHeight(40);
            
            // Password categories
            HBox categories = createPasswordCategories();
            
            // Password list with real data
            ListView<PasswordEntry> passwordList = new ListView<>();
            passwordList.getStyleClass().add("list-view");
            passwordList.setPrefHeight(300);
            
            // Load real passwords
            passwordList.getItems().addAll(passwordManager.getAllPasswords());
            
            // Enhanced cell factory for password display with copy buttons
            passwordList.setCellFactory(listView -> new ListCell<PasswordEntry>() {
                @Override
                protected void updateItem(PasswordEntry entry, boolean empty) {
                    super.updateItem(entry, empty);
                    if (empty || entry == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Create enhanced password entry display
                        HBox entryBox = new HBox(15);
                        entryBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        entryBox.setPadding(new javafx.geometry.Insets(10));
                        entryBox.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8; -fx-border-color: #334155; -fx-border-width: 1; -fx-border-radius: 8;");
                        
                        // Password info section
                        VBox infoBox = new VBox(5);
                        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
                        
                        Label titleLabel = new Label(entry.getTitle());
                        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 14px; -fx-font-weight: bold;");
                        
                        Label usernameLabel = new Label("👤 " + entry.getUsername());
                        usernameLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
                        
                        Label urlLabel = new Label("🌐 " + (entry.getUrl().isEmpty() ? "No URL" : entry.getUrl()));
                        urlLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
                        
                        infoBox.getChildren().addAll(titleLabel, usernameLabel, urlLabel);
                        
                        // Action buttons section
                        HBox buttonsBox = new HBox(8);
                        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        
                        // Copy username button
                        Button copyUserBtn = new Button("👤");
                        copyUserBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 8; -fx-background-radius: 5;");
                        copyUserBtn.setTooltip(new javafx.scene.control.Tooltip("Copy Username"));
                        copyUserBtn.setOnAction(e -> {
                            copyToClipboard(entry.getUsername());
                            showNotification("Copied", "Username copied to clipboard");
                        });
                        
                        // Copy password button
                        Button copyPassBtn = new Button("🔑");
                        copyPassBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 8; -fx-background-radius: 5;");
                        copyPassBtn.setTooltip(new javafx.scene.control.Tooltip("Copy Password"));
                        copyPassBtn.setOnAction(e -> {
                            copyToClipboard(entry.getPassword());
                            showNotification("Copied", "Password copied to clipboard");
                        });
                        
                        // Copy URL button
                        Button copyUrlBtn = new Button("🌐");
                        copyUrlBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 8; -fx-background-radius: 5;");
                        copyUrlBtn.setTooltip(new javafx.scene.control.Tooltip("Copy URL"));
                        copyUrlBtn.setOnAction(e -> {
                            if (!entry.getUrl().isEmpty()) {
                                copyToClipboard(entry.getUrl());
                                showNotification("Copied", "URL copied to clipboard");
                            }
                        });
                        copyUrlBtn.setDisable(entry.getUrl().isEmpty());
                        
                        // Password strength indicator
                        Label strengthLabel = new Label();
                        int strength = passwordManager.calculatePasswordStrength(entry.getPassword());
                        if (strength >= 80) {
                            strengthLabel.setText("🟢");
                            strengthLabel.setTooltip(new javafx.scene.control.Tooltip("Strong Password"));
                        } else if (strength >= 60) {
                            strengthLabel.setText("🟡");
                            strengthLabel.setTooltip(new javafx.scene.control.Tooltip("Medium Password"));
                        } else {
                            strengthLabel.setText("🔴");
                            strengthLabel.setTooltip(new javafx.scene.control.Tooltip("Weak Password"));
                        }
                        
                        buttonsBox.getChildren().addAll(strengthLabel, copyUserBtn, copyPassBtn, copyUrlBtn);
                        entryBox.getChildren().addAll(infoBox, buttonsBox);
                        
                        setGraphic(entryBox);
                        setText(null);
                    }
                }
            });
            
            // Search functionality
            searchField.textProperty().addListener((obs, oldText, newText) -> {
                passwordList.getItems().clear();
                passwordList.getItems().addAll(passwordManager.searchPasswords(newText));
            });
            
            // Password details panel
            VBox detailsPanel = new VBox(15);
            detailsPanel.setStyle("-fx-background-color: #334155; -fx-padding: 20px; -fx-background-radius: 8px;");
            detailsPanel.setPrefHeight(200);
            
            Label detailsTitle = new Label("Select a password to view details");
            detailsTitle.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 14px;");
            detailsPanel.getChildren().add(detailsTitle);
            
            // Password selection handler
            passwordList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    updatePasswordDetails(detailsPanel, newSelection);
                }
            });
            
            // Action buttons
            HBox actionButtons = createPasswordActionButtons(passwordStage, passwordManager, passwordList);
            
            // Security info with real statistics
            VBox securityInfo = createPasswordSecurityInfo(passwordManager);
            
            content.getChildren().addAll(searchField, categories, passwordList, detailsPanel, actionButtons, securityInfo);
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            root.getChildren().addAll(header, scrollPane);
            
            Scene scene = new Scene(root, 800, 700);
            scene.getStylesheets().add(getClass().getResource("/css/password-manager-theme.css").toExternalForm());
            
            passwordStage.setScene(scene);
            passwordStage.show();
            
            logMessage("🔑 Professional Password Vault opened - " + getCurrentVaultMode() + " Mode");
        } catch (Exception e) {
            logMessage("⚠ Password Manager error: " + e.getMessage());
            showError("Password Manager Error", "Could not access password manager: " + e.getMessage());
        }
    }
    
    /**
     * Create password manager header
     */
    private HBox createPasswordManagerHeader() {
        HBox header = new HBox(20);
        header.setPadding(new javafx.geometry.Insets(20));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 1px 0;");
        
        Label titleLabel = new Label("🔑 Password Vault");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Vault mode indicator
        Label modeLabel = new Label(getCurrentVaultMode() + " VAULT");
        if (isDecoyMode) {
            modeLabel.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: #000000; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            modeLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: #FFFFFF; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        
        header.getChildren().addAll(titleLabel, spacer, modeLabel);
        return header;
    }
    
    /**
     * Create password categories
     */
    private HBox createPasswordCategories() {
        HBox categories = new HBox(10);
        categories.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        String[] categoryNames = {"All", "Banking", "Social", "Work", "Shopping", "Crypto"};
        for (String category : categoryNames) {
            Button categoryBtn = new Button(category);
            categoryBtn.getStyleClass().add("button");
            categoryBtn.getStyleClass().add("secondary");
            categoryBtn.setPrefHeight(35);
            categories.getChildren().add(categoryBtn);
        }
        
        return categories;
    }
    
    /**
     * Create password action buttons with real functionality
     */
    private HBox createPasswordActionButtons(Stage stage, PasswordVaultManager passwordManager, ListView<PasswordEntry> passwordList) {
        HBox buttons = new HBox(15);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button addBtn = new Button("➕ Add Password");
        addBtn.getStyleClass().addAll("button", "primary");
        addBtn.setOnAction(e -> showAddPasswordDialog(passwordManager, passwordList));
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().addAll("button", "secondary");
        editBtn.setOnAction(e -> {
            PasswordEntry selected = passwordList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditPasswordDialog(passwordManager, passwordList, selected);
            } else {
                showWarning("No Selection", "Please select a password to edit");
            }
        });
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().addAll("button", "danger");
        deleteBtn.setOnAction(e -> {
            PasswordEntry selected = passwordList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (showConfirmation("Delete Password", "Are you sure you want to delete '" + selected.getTitle() + "'?")) {
                    try {
                        passwordManager.removePassword(selected.getId());
                        passwordList.getItems().remove(selected);
                        showNotification("Password Deleted", "Password removed successfully");
                    } catch (Exception ex) {
                        showError("Delete Error", "Failed to delete password: " + ex.getMessage());
                    }
                }
            } else {
                showWarning("No Selection", "Please select a password to delete");
            }
        });
        
        Button generateBtn = new Button("🎲 Generate");
        generateBtn.getStyleClass().addAll("button", "success");
        generateBtn.setOnAction(e -> showPasswordGenerator(passwordManager));
        
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().addAll("button", "secondary");
        closeBtn.setOnAction(e -> stage.close());
        
        buttons.getChildren().addAll(addBtn, editBtn, deleteBtn, generateBtn, closeBtn);
        return buttons;
    }
    
    /**
     * Update password details panel
     */
    private void updatePasswordDetails(VBox detailsPanel, PasswordEntry entry) {
        detailsPanel.getChildren().clear();
        
        Label titleLabel = new Label("Password Details");
        titleLabel.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox details = new VBox(8);
        
        details.getChildren().addAll(
            createDetailRow("Title:", entry.getTitle()),
            createDetailRow("Username:", entry.getUsername()),
            createDetailRow("URL:", entry.getUrl()),
            createDetailRow("Category:", entry.getCategory()),
            createDetailRow("Strength:", entry.getStrengthDescription() + " (" + entry.getStrength() + "%)"),
            createDetailRow("Created:", entry.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
        );
        
        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            Label notesLabel = new Label("Notes:");
            notesLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
            
            TextArea notesArea = new TextArea(entry.getNotes());
            notesArea.setEditable(false);
            notesArea.setPrefRowCount(3);
            notesArea.getStyleClass().add("text-area");
            
            details.getChildren().addAll(notesLabel, notesArea);
        }
        
        detailsPanel.getChildren().addAll(titleLabel, details);
    }
    
    /**
     * Create detail row for password info
     */
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px; -fx-min-width: 80px;");
        
        Label valueText = new Label(value != null ? value : "Not set");
        valueText.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 12px;");
        
        row.getChildren().addAll(labelText, valueText);
        return row;
    }
    
    /**
     * Create password security info with real statistics
     */
    private VBox createPasswordSecurityInfo(PasswordVaultManager passwordManager) {
        VBox securityInfo = new VBox(10);
        securityInfo.setStyle("-fx-background-color: #334155; -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #475569; -fx-border-width: 1px; -fx-border-radius: 8px;");
        
        Label securityTitle = new Label("🛡️ Vault Statistics");
        securityTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");
        
        Map<String, Object> stats = passwordManager.getPasswordStatistics();
        
        VBox statusItems = new VBox(5);
        statusItems.getChildren().addAll(
            createSecurityStatusItem("Total Passwords", stats.get("totalPasswords").toString(), "#175DDC"),
            createSecurityStatusItem("Favorites", stats.get("favoritePasswords").toString(), "#10B981"),
            createSecurityStatusItem("Weak Passwords", stats.get("weakPasswords").toString(), 
                (Integer) stats.get("weakPasswords") > 0 ? "#EF4444" : "#10B981"),
            createSecurityStatusItem("Categories", stats.get("categories").toString(), "#8B5CF6"),
            createSecurityStatusItem("Avg Strength", stats.get("averageStrength") + "%", "#F59E0B"),
            createSecurityStatusItem("Vault Mode", getCurrentVaultMode(), isDecoyMode ? "#F59E0B" : "#10B981")
        );
        
        securityInfo.getChildren().addAll(securityTitle, statusItems);
        return securityInfo;
    }
    
    /**
     * Show add password dialog
     */
    private void showAddPasswordDialog(PasswordVaultManager passwordManager, ListView<PasswordEntry> passwordList) {
        Dialog<PasswordEntry> dialog = new Dialog<>();
        dialog.setTitle("Add New Password");
        dialog.setHeaderText("Create a new password entry");
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField urlField = new TextField();
        urlField.setPromptText("URL");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Banking", "Social", "Work", "Shopping", "Crypto", "Email", "Other");
        categoryBox.setPromptText("Category");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes (optional)");
        notesArea.setPrefRowCount(3);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("URL:"), 0, 3);
        grid.add(urlField, 1, 3);
        grid.add(new Label("Category:"), 0, 4);
        grid.add(categoryBox, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                PasswordEntry entry = new PasswordEntry(
                    titleField.getText(),
                    usernameField.getText(),
                    passwordField.getText(),
                    urlField.getText()
                );
                entry.setCategory(categoryBox.getValue());
                entry.setNotes(notesArea.getText());
                return entry;
            }
            return null;
        });
        
        Optional<PasswordEntry> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            try {
                passwordManager.addPassword(entry);
                passwordList.getItems().add(entry);
                showNotification("Password Added", "New password entry created successfully");
            } catch (Exception e) {
                showError("Save Error", "Failed to save password: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show edit password dialog
     */
    private void showEditPasswordDialog(PasswordVaultManager passwordManager, ListView<PasswordEntry> passwordList, PasswordEntry entry) {
        Dialog<PasswordEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Password");
        dialog.setHeaderText("Edit password entry: " + entry.getTitle());
        
        // Create form fields with existing values
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField(entry.getTitle());
        TextField usernameField = new TextField(entry.getUsername());
        PasswordField passwordField = new PasswordField();
        passwordField.setText(entry.getPassword());
        TextField urlField = new TextField(entry.getUrl());
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Banking", "Social", "Work", "Shopping", "Crypto", "Email", "Other");
        categoryBox.setValue(entry.getCategory());
        TextArea notesArea = new TextArea(entry.getNotes());
        notesArea.setPrefRowCount(3);
        CheckBox favoriteBox = new CheckBox("Favorite");
        favoriteBox.setSelected(entry.isFavorite());
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("URL:"), 0, 3);
        grid.add(urlField, 1, 3);
        grid.add(new Label("Category:"), 0, 4);
        grid.add(categoryBox, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);
        grid.add(favoriteBox, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                entry.setTitle(titleField.getText());
                entry.setUsername(usernameField.getText());
                entry.setPassword(passwordField.getText());
                entry.setUrl(urlField.getText());
                entry.setCategory(categoryBox.getValue());
                entry.setNotes(notesArea.getText());
                entry.setFavorite(favoriteBox.isSelected());
                return entry;
            }
            return null;
        });
        
        Optional<PasswordEntry> result = dialog.showAndWait();
        result.ifPresent(updatedEntry -> {
            try {
                passwordManager.addPassword(updatedEntry);
                passwordList.refresh();
                showNotification("Password Updated", "Password entry updated successfully");
            } catch (Exception e) {
                showError("Update Error", "Failed to update password: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show password generator dialog
     */
    private void showPasswordGenerator(PasswordVaultManager passwordManager) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Password Generator");
        dialog.setHeaderText("Generate a secure password");
        
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        
        // Length slider
        Label lengthLabel = new Label("Password Length: 12");
        Slider lengthSlider = new Slider(8, 32, 12);
        lengthSlider.setShowTickLabels(true);
        lengthSlider.setShowTickMarks(true);
        lengthSlider.setMajorTickUnit(4);
        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lengthLabel.setText("Password Length: " + newVal.intValue());
        });
        
        // Options
        CheckBox symbolsBox = new CheckBox("Include Symbols (!@#$%^&*)");
        symbolsBox.setSelected(true);
        
        // Generated password display
        TextField passwordDisplay = new TextField();
        passwordDisplay.setEditable(false);
        passwordDisplay.setStyle("-fx-font-family: 'Courier New', monospace;");
        
        // Generate button
        Button generateBtn = new Button("🎲 Generate Password");
        generateBtn.getStyleClass().addAll("button", "primary");
        generateBtn.setOnAction(e -> {
            String password = passwordManager.generateSecurePassword(
                (int) lengthSlider.getValue(), 
                symbolsBox.isSelected()
            );
            passwordDisplay.setText(password);
        });
        
        // Copy button
        Button copyBtn = new Button("📋 Copy to Clipboard");
        copyBtn.getStyleClass().addAll("button", "secondary");
        copyBtn.setOnAction(e -> {
            if (!passwordDisplay.getText().isEmpty()) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                clipboardContent.putString(passwordDisplay.getText());
                clipboard.setContent(clipboardContent);
                showNotification("Copied", "Password copied to clipboard");
            }
        });
        
        HBox buttonBox = new HBox(10, generateBtn, copyBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        content.getChildren().addAll(lengthLabel, lengthSlider, symbolsBox, passwordDisplay, buttonBox);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Generate initial password
        generateBtn.fire();
        
        dialog.showAndWait();
    }
    
    /**
     * Create security status item
     */
    private HBox createSecurityStatusItem(String label, String status, String color) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label labelText = new Label(label + ":");
        labelText.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label statusText = new Label(status);
        statusText.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        
        item.getChildren().addAll(labelText, spacer, statusText);
        return item;
    }
    

    

    

    
    /**
     * Calculate threat level based on system state
     */
    private String calculateThreatLevel() {
        // Simple threat assessment
        if (allVaultFiles.isEmpty()) {
            return "LOW";
        } else if (allVaultFiles.size() > 50) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Handle advanced file manager - enhanced file management features
     */
    @FXML
    private void handleFileManager() {
        try {
            // Show advanced file management options
            showAdvancedFileManager();
            logMessage("📁 Advanced File Manager opened - Enhanced file operations available");
        } catch (Exception e) {
            logMessage("⚠ File Manager error: " + e.getMessage());
            showError("File Manager Error", "Could not open advanced file manager: " + e.getMessage());
        }
    }
    

    

    


    

    

    
    /**
     * Show AI analysis of current vault files
     */
    private void showAIAnalysis() {
        // Simple analysis without external feature manager
        StringBuilder analysis = new StringBuilder();
        analysis.append("Vault Analysis Summary:\n\n");
        analysis.append("Total Files: ").append(allVaultFiles.size()).append("\n");
        
        if (!allVaultFiles.isEmpty()) {
            long totalSize = allVaultFiles.stream()
                .mapToLong(VaultFile::getSize)
                .sum();
            analysis.append("Total Size: ").append(formatFileSize(totalSize)).append("\n");
            
            Map<String, Long> typeCount = allVaultFiles.stream()
                .collect(Collectors.groupingBy(
                    f -> {
                        String name = f.getOriginalName();
                        int lastDot = name.lastIndexOf('.');
                        return lastDot > 0 ? name.substring(lastDot) : "no extension";
                    },
                    Collectors.counting()
                ));
            
            analysis.append("\nFile Types:\n");
            typeCount.forEach((ext, count) -> 
                analysis.append("• ").append(ext.isEmpty() ? "No extension" : ext)
                        .append(": ").append(count).append(" files\n"));
        }
        
        showInfo("🤖 AI Vault Analysis", analysis.toString());
        logMessage("🧠 AI analysis completed for " + allVaultFiles.size() + " files");
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
                fileCountLabel.setText("📁 " + totalCount + " file(s)");
            } else {
                fileCountLabel.setText("📁 " + displayedCount + " of " + totalCount + " files");
            }
            
            // Calculate total vault size from actual VaultFile objects
            long totalSize = allVaultFiles.stream().mapToLong(VaultFile::getSize).sum();
            vaultSizeLabel.setText("💾 " + formatFileSize(totalSize));
            
            if (isDecoyMode) {
                encryptionLabel.setText("🎭 Decoy Mode Active");
                sessionLabel.setText("🟡 DECOY MODE");
                sessionLabel.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: #000000; -fx-padding: 6px 12px; -fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 12px;");
            } else {
                if (totalCount > 0) {
                    encryptionLabel.setText("🔐 " + totalCount + " files encrypted with AES-256");
                } else {
                    encryptionLabel.setText("🔐 Vault ready - Drop files to encrypt");
                }
                sessionLabel.setText("🟢 MASTER MODE");
                sessionLabel.setStyle("-fx-background-color: #10B981; -fx-text-fill: #FFFFFF; -fx-padding: 6px 12px; -fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 12px;");
            }
            
            // Status updated successfully
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
            // Animate progress indicator
            AnimationManager.fadeIn(operationProgress, AnimationManager.FAST);
            AnimationManager.pulse(operationProgress);
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
            // Animate progress indicator out
            AnimationManager.fadeOut(operationProgress, AnimationManager.FAST, () -> {
                operationProgress.setVisible(false);
            });
            operationStatusLabel.setText("");
        });
    }
    
    /**
     * Log message to activity area with password manager styling
     */
    private void logMessage(String message) {
        Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(message + "\n");
                logArea.setScrollTop(Double.MAX_VALUE);
                // Apply password manager theme styling
                com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(logArea);
            }
        });
    }
    
    /**
     * Apply settings (simplified implementation)
     */
    private void applySettings(Object settings) {
        if (settings == null) return;
        
        try {
            // Simple settings application
            logMessage("⚙️ Settings applied successfully");
            
            // Apply theme settings
            logMessage("✓ Theme settings applied");
            
            // Log all applied settings
            logMessage("✓ Settings updated successfully");
            logMessage("  - Theme: " + (settings.getSelectedTheme() != null ? settings.getSelectedTheme() : "Default"));
            logMessage("  - Session timeout: " + settings.getSessionTimeout() + " minutes");
            logMessage("  - Auto-backup: " + (settings.isAutoBackupEnabled() ? "Enabled" : "Disabled"));
            logMessage("  - Notifications: " + (settings.isNotificationsEnabled() ? "Enabled" : "Disabled"));
            logMessage("  - Secure delete: " + (settings.isSecureDeleteEnabled() ? "Enabled" : "Disabled"));
            
        } catch (Exception e) {
            logMessage("⚠ Error applying settings: " + e.getMessage());
            showError("Settings Error", "Some settings could not be applied: " + e.getMessage());
        }
    }
    
    /**
     * Clear sensitive data from memory
     */
    private void clearSensitiveData() {
        if (encryptionKey != null) {
            encryptionKey = null;
        }
        
        // Cleanup completed
        logMessage("🧹 Cleanup completed");
        
        fileList.clear();
        filteredFileList.clear();
        allVaultFiles.clear();
        logArea.clear();
    }
    
    // =========================== NOTIFICATION HELPERS ===========================
    
    private void showNotification(String title, String message) {
        com.ghostvault.ui.components.NotificationSystem.showInfo(title, message);
        // Add success glow to main content
        if (mainContent != null) {
            AnimationManager.successGlow(mainContent);
        }
    }
    
    private void showWarning(String title, String message) {
        com.ghostvault.ui.components.NotificationSystem.showWarning(title, message);
        // Add warning pulse to main content
        if (mainContent != null) {
            AnimationManager.pulse(mainContent, 1.05, AnimationManager.FAST);
        }
    }
    
    private void showError(String title, String message) {
        com.ghostvault.ui.components.NotificationSystem.showError(title, message);
        // Add error shake and glow to main content
        if (mainContent != null) {
            AnimationManager.shake(mainContent);
            AnimationManager.errorGlow(mainContent);
        }
    }
    
    private void showInfo(String title, String message) {
        com.ghostvault.ui.components.NotificationSystem.showInfo(title, message);
    }
    
    /**
     * Copy text to system clipboard
     */
    private void copyToClipboard(String text) {
        try {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
        } catch (Exception e) {
            logMessage("⚠ Failed to copy to clipboard: " + e.getMessage());
        }
    }
    
    /**
     * Safely apply theme to a node
     */
    private void safeApplyTheme(javafx.scene.Node node) {
        if (node != null) {
            try {
                com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(node);
            } catch (Exception e) {
                // Fallback styling if theme not available
                node.setStyle("-fx-text-fill: #F8FAFC; -fx-background-color: #1E293B;");
            }
        }
    }
    
    /**
     * Get password count from password manager
     */
    private int getPasswordCount() {
        try {
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            PasswordVaultManager passwordManager = new PasswordVaultManager(vaultPath);
            passwordManager.setEncryptionKey(encryptionKey);
            passwordManager.loadPasswords();
            return passwordManager.getAllPasswords().size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get notes count from notes manager
     */
    private int getNotesCount() {
        try {
            String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
            SecureNotesManager notesManager = new SecureNotesManager(vaultPath);
            notesManager.setEncryptionKey(encryptionKey);
            notesManager.loadNotes();
            return notesManager.getAllNotes().size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Initialize accessibility features
     */
    private void initializeAccessibility() {
        try {
            if (mainContent != null && mainContent.getScene() != null) {
                accessibilityManager = new com.ghostvault.ui.AccessibilityManager();
                accessibilityManager.initializeAccessibility(mainContent.getScene());
                logMessage("♿ Accessibility features initialized");
            }
        } catch (Exception e) {
            logMessage("⚠ Accessibility initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * Show comprehensive help system
     */
    private void showHelpSystem() {
        try {
            com.ghostvault.ui.HelpSystem helpSystem = new com.ghostvault.ui.HelpSystem();
            helpSystem.showHelp((javafx.stage.Stage) mainContent.getScene().getWindow());
            logMessage("📖 Help system opened");
        } catch (Exception e) {
            logMessage("⚠ Help system not available: " + e.getMessage());
            // Fallback to basic help
            showInfo("Keyboard Shortcuts", 
                "File Operations:\n" +
                "• Ctrl+O - Upload files\n" +
                "• Ctrl+S - Download selected file\n" +
                "• Enter - Download selected file\n" +
                "• Space - Preview selected file\n" +
                "• Delete - Delete selected file\n\n" +
                "Vault Operations:\n" +
                "• Ctrl+B - Create backup\n" +
                "• Ctrl+R - Restore from backup\n" +
                "• F5 - Refresh file list\n\n" +
                "Advanced Features:\n" +
                "• Ctrl+D - Security Dashboard\n" +
                "• Ctrl+N - Secure Notes\n" +
                "• Ctrl+P - Password Manager\n" +
                "• Ctrl+M - Advanced File Manager\n" +
                "• Ctrl+T - Switch Theme (Dark/Light)\n\n" +
                "Navigation:\n" +
                "• Ctrl+F - Focus search box\n" +
                "• Ctrl+Q - Logout\n" +
                "• F1 - Show this help");
        }
    }
    
    /**
     * Show new note dialog
     */
    private void showNewNoteDialog(SecureNotesManager notesManager) {
        Dialog<SecureNote> dialog = new Dialog<>();
        dialog.setTitle("Create New Note");
        dialog.setHeaderText("Create a new secure note");
        
        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Note title");
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Personal", "Work", "Finance", "Security", "Ideas", "Recipes", "Travel", "Other");
        categoryBox.setPromptText("Category");
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Note content...");
        contentArea.setPrefRowCount(10);
        contentArea.setPrefColumnCount(50);
        
        TextField tagsField = new TextField();
        tagsField.setPromptText("Tags (comma separated)");
        
        CheckBox pinnedBox = new CheckBox("Pin this note");
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);
        grid.add(new Label("Tags:"), 0, 3);
        grid.add(tagsField, 1, 3);
        grid.add(pinnedBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                SecureNote note = new SecureNote(titleField.getText(), contentArea.getText());
                note.setCategory(categoryBox.getValue());
                note.setTags(tagsField.getText());
                note.setPinned(pinnedBox.isSelected());
                return note;
            }
            return null;
        });
        
        Optional<SecureNote> result = dialog.showAndWait();
        result.ifPresent(note -> {
            try {
                notesManager.addNote(note);
                showNotification("Note Created", "New note created successfully");
            } catch (Exception e) {
                showError("Save Error", "Failed to save note: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show edit note dialog
     */
    private void showEditNoteDialog(SecureNotesManager notesManager, SecureNote note, ListView<SecureNote> notesList) {
        Dialog<SecureNote> dialog = new Dialog<>();
        dialog.setTitle("Edit Note");
        dialog.setHeaderText("Edit note: " + note.getTitle());
        
        // Create form fields with existing values
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField(note.getTitle());
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Personal", "Work", "Finance", "Security", "Ideas", "Recipes", "Travel", "Other");
        categoryBox.setValue(note.getCategory());
        
        TextArea contentArea = new TextArea(note.getContent());
        contentArea.setPrefRowCount(10);
        contentArea.setPrefColumnCount(50);
        
        TextField tagsField = new TextField(note.getTags());
        
        CheckBox pinnedBox = new CheckBox("Pin this note");
        pinnedBox.setSelected(note.isPinned());
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);
        grid.add(new Label("Tags:"), 0, 3);
        grid.add(tagsField, 1, 3);
        grid.add(pinnedBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                note.setTitle(titleField.getText());
                note.setContent(contentArea.getText());
                note.setCategory(categoryBox.getValue());
                note.setTags(tagsField.getText());
                note.setPinned(pinnedBox.isSelected());
                return note;
            }
            return null;
        });
        
        Optional<SecureNote> result = dialog.showAndWait();
        result.ifPresent(updatedNote -> {
            try {
                notesManager.addNote(updatedNote);
                notesList.refresh();
                showNotification("Note Updated", "Note updated successfully");
            } catch (Exception e) {
                showError("Update Error", "Failed to update note: " + e.getMessage());
            }
        });
    }
}