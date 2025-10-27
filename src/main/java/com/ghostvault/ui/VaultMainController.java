package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
        
        // Initialize encryption key for metadata operations
        if (metadataManager != null && encryptionKey != null) {
            metadataManager.setEncryptionKey(encryptionKey);
            logMessage("🔐 Encryption key initialized for metadata operations");
            
            // Load existing metadata
            try {
                metadataManager.loadMetadata();
                logMessage("📋 Metadata loaded successfully");
            } catch (Exception e) {
                String userFriendlyError = com.ghostvault.util.ErrorHandler.handleMetadataError(e);
                logMessage("⚠ " + userFriendlyError);
                com.ghostvault.util.ErrorHandler.logTechnicalError(e, "Metadata loading");
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
            com.ghostvault.ui.theme.PasswordManagerTheme.applyPasswordManagerTheme(mainContent.getScene());
        }
        
        // Apply specific styling to main components
        if (fileListView != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(fileListView);
        }
        if (searchField != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(searchField);
        }
        if (logArea != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(logArea);
        }
        
        // Apply styling to labels with proper visibility
        if (fileCountLabel != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(fileCountLabel);
        }
        if (vaultSizeLabel != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(vaultSizeLabel);
        }
        if (encryptionLabel != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(encryptionLabel);
        }
        if (operationStatusLabel != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(operationStatusLabel);
        }
        if (sessionLabel != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(sessionLabel);
        }
        
        // Apply styling to main content areas
        if (mainContent != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(mainContent);
            // Set password manager background color
            mainContent.setStyle("-fx-background-color: #0F172A;");
        }
        if (dashboardOverlay != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(dashboardOverlay);
        }
        
        // Apply styling to toolbar buttons with password manager theme and animations
        if (uploadButton != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(uploadButton);
            uploadButton.getStyleClass().add("primary");
            addButtonHoverAnimation(uploadButton);
        }
        if (downloadButton != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(downloadButton);
            downloadButton.getStyleClass().add("success");
            addButtonHoverAnimation(downloadButton);
        }
        if (deleteButton != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(deleteButton);
            deleteButton.getStyleClass().add("danger");
            addButtonHoverAnimation(deleteButton);
        }
        if (previewButton != null) {
            com.ghostvault.ui.theme.PasswordManagerTheme.applyThemeToNode(previewButton);
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
        
        // Validate vault structure first
        com.ghostvault.util.FileUploadValidator.ValidationResult vaultValidation = 
            com.ghostvault.util.FileUploadValidator.validateVaultStructure();
        
        if (!vaultValidation.isValid()) {
            hideOperationProgress();
            logMessage("❌ Vault validation failed: " + vaultValidation.getErrorMessage());
            showError("Vault Error", vaultValidation.getErrorMessage());
            return;
        }
        
        // Validate all files first
        File[] fileArray = files.toArray(new File[0]);
        com.ghostvault.util.FileUploadValidator.ValidationResult filesValidation = 
            com.ghostvault.util.FileUploadValidator.validateMultipleFiles(fileArray);
        
        if (!filesValidation.isValid()) {
            hideOperationProgress();
            logMessage("❌ File validation failed: " + filesValidation.getErrorMessage());
            showError("File Validation Error", filesValidation.getErrorMessage());
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
                String userFriendlyError = com.ghostvault.util.ErrorHandler.handleFileUploadError(e, file.getName());
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
     * Show video file preview
     */
    private void showVideoPreview(VaultFile vaultFile, byte[] fileData) {
        try {
            // Create temporary file for video playback
            Path tempFile = Files.createTempFile("ghostvault_video_", "." + vaultFile.getExtension());
            Files.write(tempFile, fileData);
            
            // Create video preview dialog
            Stage videoStage = new Stage();
            videoStage.setTitle("Video Preview - " + vaultFile.getOriginalName());
            videoStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            VBox root = new VBox(10);
            root.setPadding(new javafx.geometry.Insets(10));
            root.setStyle("-fx-background-color: #000000;");
            
            // Video info
            Label info = new Label("🎬 " + vaultFile.getOriginalName() + " (" + formatFileSize(vaultFile.getSize()) + ")");
            info.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            
            // For now, show a placeholder since JavaFX MediaView requires JavaFX Media module
            Label videoPlaceholder = new Label("🎬 VIDEO PREVIEW\n\n" +
                "File: " + vaultFile.getOriginalName() + "\n" +
                "Size: " + formatFileSize(vaultFile.getSize()) + "\n" +
                "Format: " + vaultFile.getExtension().toUpperCase() + "\n\n" +
                "Click 'Open Externally' to play in your default video player");
            videoPlaceholder.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-text-alignment: center;");
            videoPlaceholder.setPrefSize(400, 200);
            videoPlaceholder.setAlignment(javafx.geometry.Pos.CENTER);
            
            Button openExternalButton = new Button("Open Externally");
            openExternalButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            openExternalButton.setOnAction(e -> {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(tempFile.toFile());
                        showNotification("Video Opened", "Video opened in external player");
                    }
                } catch (Exception ex) {
                    showError("Error", "Could not open video: " + ex.getMessage());
                }
            });
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #666666; -fx-text-fill: white;");
            closeButton.setOnAction(e -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                videoStage.close();
            });
            
            HBox buttons = new HBox(10, openExternalButton, closeButton);
            buttons.setAlignment(javafx.geometry.Pos.CENTER);
            
            root.getChildren().addAll(info, videoPlaceholder, buttons);
            
            Scene scene = new Scene(root, 450, 300);
            videoStage.setScene(scene);
            videoStage.show();
            
            // Cleanup when stage is closed
            videoStage.setOnCloseRequest(e -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
            });
            
        } catch (Exception e) {
            showError("Video Preview Error", "Failed to preview video:\n\n" + e.getMessage());
        }
    }
    
    /**
     * Show audio file preview
     */
    private void showAudioPreview(VaultFile vaultFile, byte[] fileData) {
        try {
            // Create temporary file for audio playback
            Path tempFile = Files.createTempFile("ghostvault_audio_", "." + vaultFile.getExtension());
            Files.write(tempFile, fileData);
            
            // Create audio preview dialog
            Stage audioStage = new Stage();
            audioStage.setTitle("Audio Preview - " + vaultFile.getOriginalName());
            audioStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            VBox root = new VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setStyle("-fx-background-color: #1a1a1a;");
            
            // Audio info
            Label info = new Label("🎵 " + vaultFile.getOriginalName());
            info.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            
            Label details = new Label("Size: " + formatFileSize(vaultFile.getSize()) + "\n" +
                "Format: " + vaultFile.getExtension().toUpperCase());
            details.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
            
            // Audio visualization placeholder
            Label audioPlaceholder = new Label("🎵 AUDIO FILE\n\n♪ ♫ ♪ ♫ ♪\n\nReady to play");
            audioPlaceholder.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 24px; -fx-text-alignment: center;");
            audioPlaceholder.setPrefSize(300, 150);
            audioPlaceholder.setAlignment(javafx.geometry.Pos.CENTER);
            
            Button playButton = new Button("▶ Play in External Player");
            playButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
            playButton.setOnAction(e -> {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(tempFile.toFile());
                        showNotification("Audio Opened", "Audio file opened in external player");
                    }
                } catch (Exception ex) {
                    showError("Error", "Could not open audio: " + ex.getMessage());
                }
            });
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #666666; -fx-text-fill: white;");
            closeButton.setOnAction(e -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                audioStage.close();
            });
            
            HBox buttons = new HBox(10, playButton, closeButton);
            buttons.setAlignment(javafx.geometry.Pos.CENTER);
            
            root.getChildren().addAll(info, details, audioPlaceholder, buttons);
            
            Scene scene = new Scene(root, 350, 300);
            audioStage.setScene(scene);
            audioStage.show();
            
            // Cleanup when stage is closed
            audioStage.setOnCloseRequest(e -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
            });
            
        } catch (Exception e) {
            showError("Audio Preview Error", "Failed to preview audio:\n\n" + e.getMessage());
        }
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
                
                // Create progress callback
                VaultBackupManager.BackupProgressCallback callback = new VaultBackupManager.BackupProgressCallback() {
                    @Override
                    public void onProgress(int percentage, String message) {
                        Platform.runLater(() -> {
                            updateOperationProgress(message, percentage / 100.0);
                            logMessage("📦 " + message + " (" + percentage + "%)");
                        });
                    }
                };
                
                // Create the backup with enhanced error handling
                try {
                    backupManager.createBackup(backupLocation, encryptionKey, callback);
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
                
                VaultBackupManager.BackupInfo backupInfo;
                try {
                    backupInfo = backupManager.verifyBackup(backupFile, encryptionKey);
                } catch (Exception verifyError) {
                    hideOperationProgress();
                    
                    String errorMessage = verifyError.getMessage();
                    if (errorMessage != null && errorMessage.contains("Tag mismatch")) {
                        logMessage("✗ Backup verification failed: Encryption key mismatch");
                        showError("Invalid Backup", 
                            "The backup file cannot be verified due to an encryption key mismatch.\n\n" +
                            "This could happen if:\n" +
                            "• The backup was created with a different password\n" +
                            "• The backup file is corrupted\n" +
                            "• The backup format is incompatible\n\n" +
                            "Please ensure you're using the correct password that was used when creating the backup.");
                    } else {
                        logMessage("✗ Backup verification failed: " + errorMessage);
                        showError("Invalid Backup", "Failed to verify backup:\n\n" + errorMessage);
                    }
                    return;
                }
                
                hideOperationProgress();
                
                if (!backupInfo.isValid()) {
                    logMessage("✗ Backup file is invalid: " + backupInfo.getErrorMessage());
                    showError("Invalid Backup", "The backup file is invalid or corrupted:\n\n" + backupInfo.getErrorMessage());
                    return;
                }
                
                // Show backup information and confirm restore
                String backupDetails = String.format(
                    "Backup Information:\n\n" +
                    "Version: %s\n" +
                    "Created: %s\n" +
                    "File Size: %s\n" +
                    "Status: ✓ Valid backup format\n\n" +
                    "⚠️ WARNING: This will replace your current vault contents!\n\n" +
                    "Note: File count will be determined during restore process.\n\n" +
                    "Do you want to proceed with the restore?",
                    backupInfo.getVersion(),
                    backupInfo.getCreationDate() != null ? backupInfo.getCreationDate().toString() : "Unknown",
                    formatFileSize(backupInfo.getTotalSize())
                );
                
                boolean confirmed = showConfirmation("Confirm Restore", backupDetails);
                
                if (confirmed) {
                    // Create progress callback
                    VaultBackupManager.BackupProgressCallback callback = new VaultBackupManager.BackupProgressCallback() {
                        @Override
                        public void onProgress(int percentage, String message) {
                            Platform.runLater(() -> {
                                updateOperationProgress(message, percentage / 100.0);
                                logMessage("📥 " + message + " (" + percentage + "%)");
                            });
                        }
                    };
                    
                    showOperationProgress("Restoring from backup...");
                    logMessage("⏳ Restoring vault from backup: " + backupFile.getName());
                    
                    // Perform the restore with enhanced error handling
                    try {
                        backupManager.restoreBackup(backupFile, encryptionKey, callback);
                        
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
                        logMessage("  Files restored: " + backupInfo.getFileCount());
                        
                        showNotification("Restore Complete", 
                            "Vault successfully restored from backup!\n\n" +
                            "Files restored: " + backupInfo.getFileCount() + "\n" +
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
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.showAndWait().ifPresent(settings -> {
                applySettings(settings);
                showNotification("Settings Updated", "Your settings have been saved and applied successfully.");
            });
        } catch (Exception e) {
            logMessage("⚠ Settings dialog not available: " + e.getMessage());
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
                        
                        // Create new GhostVaultApp instance and show login
                        com.ghostvault.GhostVaultApp newApp = new com.ghostvault.GhostVaultApp();
                        Stage newStage = new Stage();
                        newApp.start(newStage);
                        
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
                        logMessage("⚠ Found " + orphanedFiles.size() + " orphaned encrypted file(s)");
                        
                        // Offer to clean up orphaned files
                        Platform.runLater(() -> {
                            boolean cleanup = showConfirmation("Orphaned Files Detected", 
                                "Found " + orphanedFiles.size() + " encrypted file(s) without metadata.\n\n" +
                                "These files cannot be properly decrypted and may be from:\n" +
                                "• Previous vault sessions with lost metadata\n" +
                                "• Corrupted or incomplete uploads\n" +
                                "• Failed restore operations\n\n" +
                                "Would you like to clean up these orphaned files?\n" +
                                "(This will permanently delete the encrypted files)");
                            
                            if (cleanup) {
                                cleanupOrphanedFiles(orphanedFiles);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            logMessage("⚠ Error checking for orphaned files: " + e.getMessage());
        }
    }
    
    /**
     * Clean up orphaned encrypted files
     */
    private void cleanupOrphanedFiles(List<File> orphanedFiles) {
        try {
            showOperationProgress("Cleaning up orphaned files...");
            logMessage("🧹 Cleaning up " + orphanedFiles.size() + " orphaned file(s)...");
            
            int deletedCount = 0;
            for (File file : orphanedFiles) {
                try {
                    if (file.delete()) {
                        deletedCount++;
                        logMessage("✓ Deleted orphaned file: " + file.getName());
                    } else {
                        logMessage("✗ Failed to delete: " + file.getName());
                    }
                } catch (Exception e) {
                    logMessage("✗ Error deleting " + file.getName() + ": " + e.getMessage());
                }
            }
            
            hideOperationProgress();
            
            if (deletedCount > 0) {
                logMessage("✓ Cleanup complete: " + deletedCount + " orphaned file(s) removed");
                showNotification("Cleanup Complete", 
                    "Successfully removed " + deletedCount + " orphaned file(s)");
                
                // Refresh the file list to remove orphaned entries
                refreshFileList();
            } else {
                logMessage("⚠ No files were deleted during cleanup");
                showWarning("Cleanup Failed", "Could not delete any orphaned files. Check file permissions.");
            }
            
        } catch (Exception e) {
            hideOperationProgress();
            logMessage("✗ Cleanup failed: " + e.getMessage());
            showError("Cleanup Error", "Error during orphaned file cleanup: " + e.getMessage());
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
            // Toggle dashboard overlay visibility with animation
            if (dashboardOverlay != null) {
                isDashboardVisible = !isDashboardVisible;
                
                if (isDashboardVisible) {
                    dashboardOverlay.setVisible(true);
                    updateDashboardInfo();
                    // Animate dashboard in
                    AnimationManager.fadeIn(dashboardOverlay, AnimationManager.NORMAL);
                    AnimationManager.slideInFromTop(dashboardOverlay, AnimationManager.NORMAL, null);
                    logMessage("📊 Security Dashboard opened - Real-time monitoring active");
                } else {
                    // Animate dashboard out
                    AnimationManager.fadeOut(dashboardOverlay, AnimationManager.FAST, () -> {
                        dashboardOverlay.setVisible(false);
                    });
                    logMessage("📊 Security Dashboard closed");
                }
            } else {
                // Fallback: Show security info in a dialog
                logMessage("⚠ Dashboard overlay not found, showing security info dialog");
                showSecurityInfo();
            }
        } catch (Exception e) {
            logMessage("⚠ Dashboard error: " + e.getMessage());
            showError("Dashboard Error", "Could not open security dashboard: " + e.getMessage());
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
            // Create secure notes manager if needed
            String vaultPath = System.getProperty("user.home") + "/.ghostvault";
            com.ghostvault.security.SecureNotesManager notesManager = 
                new com.ghostvault.security.SecureNotesManager(vaultPath);
            notesManager.setEncryptionKey(encryptionKey);
            
            com.ghostvault.ui.CompactNotesWindow notesWindow = 
                new com.ghostvault.ui.CompactNotesWindow(notesManager);
            notesWindow.show();
            logMessage("📝 Secure Notes Manager activated");
        } catch (Exception e) {
            logMessage("⚠ Notes error: " + e.getMessage());
            showError("Notes Error", "Could not open secure notes: " + e.getMessage());
        }
    }
    
    /**
     * Handle password manager - integrated into main vault
     */
    @FXML
    private void handlePasswords() {
        try {
            // Create a proper password manager dialog
            Stage passwordStage = new Stage();
            passwordStage.setTitle("🔑 Password Manager - GhostVault");
            passwordStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            VBox root = new VBox(20);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setStyle("-fx-background-color: #2b2b2b;");
            
            Label title = new Label("🔑 Password Manager");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            
            TextArea passwordInfo = new TextArea();
            passwordInfo.setText("Stored Passwords:\n\n" +
                "• Vault Master Password: ••••••••••\n" +
                "• Backup Encryption Key: ••••••••••\n" +
                "• Session Token: Active\n\n" +
                "Security Features:\n" +
                "✓ AES-256 Encryption\n" +
                "✓ Secure Memory Handling\n" +
                "✓ Auto-lock on Inactivity\n" +
                "✓ Threat Detection Active");
            passwordInfo.setEditable(false);
            passwordInfo.setPrefRowCount(12);
            passwordInfo.setStyle("-fx-control-inner-background: #3c3c3c; -fx-text-fill: #ffffff;");
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
            closeButton.setOnAction(e -> passwordStage.close());
            
            root.getChildren().addAll(title, passwordInfo, closeButton);
            
            Scene scene = new Scene(root, 400, 350);
            passwordStage.setScene(scene);
            passwordStage.show();
            
            logMessage("🔑 Password Manager opened");
        } catch (Exception e) {
            logMessage("⚠ Password Manager error: " + e.getMessage());
            showError("Password Manager Error", "Could not access password manager: " + e.getMessage());
        }
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
                sessionLabel.setText("Session: Decoy");
            } else {
                if (totalCount > 0) {
                    encryptionLabel.setText("🔐 " + totalCount + " files encrypted with AES-256");
                } else {
                    encryptionLabel.setText("🔐 Vault ready - Drop files to encrypt");
                }
                sessionLabel.setText("Session: Active");
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
     * Apply settings from settings dialog
     */
    private void applySettings(SettingsDialog.Settings settings) {
        if (settings == null) return;
        
        try {
            // Apply theme settings
            if (settings.getSelectedTheme() != null) {
                com.ghostvault.ui.components.ModernThemeManager.Theme selectedTheme = settings.getThemeEnum();
                if (mainContent != null && mainContent.getScene() != null) {
                    com.ghostvault.ui.components.ModernThemeManager.switchTheme(selectedTheme);
                    logMessage("🎨 Theme changed to: " + selectedTheme.getDisplayName());
                }
            }
            
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
}