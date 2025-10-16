package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Vault restore dialog for restoring encrypted backups
 */
public class VaultRestoreDialog {
    
    // Dialog components
    private Stage dialogStage;
    private VBox mainContainer;
    private HBox headerBar;
    private Label titleLabel;
    private Button closeButton;
    
    // Backup selection area
    private VBox backupSelectionArea;
    private TextField backupFileField;
    private Button browseBackupButton;
    private VBox backupInfoPanel;
    private Label backupInfoLabel;
    private ListView<BackupEntry> backupContentsListView;
    
    // Restore options area
    private VBox restoreOptionsArea;
    private TextField restoreLocationField;
    private Button browseRestoreLocationButton;
    private PasswordField passwordField;
    private CheckBox overwriteExisting;
    private CheckBox restorePermissions;
    private CheckBox restoreTimestamps;
    private ComboBox<RestoreMode> restoreModeSelector;
    
    // Progress area
    private VBox progressArea;
    private ProgressBar overallProgress;
    private ProgressBar currentFileProgress;
    private Label statusLabel;
    private Label currentFileLabel;
    private Label speedLabel;
    
    // Button area
    private HBox buttonArea;
    private Button restoreButton;
    private Button cancelButton;
    
    // Configuration
    private File selectedBackupFile;
    private File restoreLocation;
    private BackupMetadata backupMetadata;
    
    // Callbacks
    private Consumer<File> onRestoreCompleted;
    private Runnable onCancelled;
    
    // State
    private boolean restoreInProgress = false;
    private Task<Void> restoreTask;
    
    public VaultRestoreDialog() {
        initializeComponents();
        setupLayout();
        setupStyling();
        setupEventHandlers();
        initializeDefaults();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Dialog stage
        dialogStage = new Stage();
        dialogStage.setTitle("Restore Vault Backup");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(700);
        dialogStage.setHeight(600);
        
        // Main container
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("vault-restore-dialog");
        
        // Header bar
        headerBar = new HBox(8);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getStyleClass().add("restore-dialog-header");
        
        titleLabel = new Label("Restore Vault Backup");
        titleLabel.getStyleClass().add("restore-dialog-title");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "icon", "close-button");
        
        headerBar.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        
        // Backup selection area
        backupSelectionArea = new VBox(12);
        backupSelectionArea.getStyleClass().add("backup-selection-area");
        
        Label backupLabel = new Label("Select Backup File:");
        backupLabel.getStyleClass().add("section-label");
        
        HBox backupFileRow = new HBox(8);
        backupFileRow.setAlignment(Pos.CENTER_LEFT);
        
        backupFileField = new TextField();
        backupFileField.getStyleClass().add("backup-file-field");
        backupFileField.setEditable(false);
        backupFileField.setPromptText("Select a .gvb backup file...");
        HBox.setHgrow(backupFileField, Priority.ALWAYS);
        
        browseBackupButton = new Button("Browse...");
        browseBackupButton.getStyleClass().addAll("button", "ghost", "browse-button");
        
        backupFileRow.getChildren().addAll(backupFileField, browseBackupButton);
        
        // Backup info panel
        backupInfoPanel = new VBox(8);
        backupInfoPanel.getStyleClass().add("backup-info-panel");
        backupInfoPanel.setVisible(false);
        
        backupInfoLabel = new Label();
        backupInfoLabel.getStyleClass().add("backup-info-label");
        backupInfoLabel.setWrapText(true);
        
        Label contentsLabel = new Label("Backup Contents:");
        contentsLabel.getStyleClass().add("subsection-label");
        
        backupContentsListView = new ListView<>();
        backupContentsListView.getStyleClass().add("backup-contents-list");
        backupContentsListView.setCellFactory(listView -> new BackupEntryListCell());
        backupContentsListView.setPrefHeight(150);
        
        backupInfoPanel.getChildren().addAll(backupInfoLabel, contentsLabel, backupContentsListView);
        
        backupSelectionArea.getChildren().addAll(backupLabel, backupFileRow, backupInfoPanel);
        
        // Restore options area
        restoreOptionsArea = new VBox(12);
        restoreOptionsArea.getStyleClass().add("restore-options-area");
        
        Label optionsLabel = new Label("Restore Options:");
        optionsLabel.getStyleClass().add("section-label");
        
        // Restore location
        Label locationLabel = new Label("Restore to:");
        locationLabel.getStyleClass().add("subsection-label");
        
        HBox locationRow = new HBox(8);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        
        restoreLocationField = new TextField();
        restoreLocationField.getStyleClass().add("restore-location-field");
        restoreLocationField.setEditable(false);
        HBox.setHgrow(restoreLocationField, Priority.ALWAYS);
        
        browseRestoreLocationButton = new Button("Browse...");
        browseRestoreLocationButton.getStyleClass().addAll("button", "ghost", "browse-button");
        
        locationRow.getChildren().addAll(restoreLocationField, browseRestoreLocationButton);
        
        // Password
        Label passwordLabel = new Label("Backup password:");
        passwordLabel.getStyleClass().add("subsection-label");
        
        passwordField = new PasswordField();
        passwordField.getStyleClass().add("restore-password-field");
        passwordField.setPromptText("Enter backup password...");
        
        // Restore mode
        Label modeLabel = new Label("Restore mode:");
        modeLabel.getStyleClass().add("subsection-label");
        
        restoreModeSelector = new ComboBox<>();
        restoreModeSelector.getItems().addAll(RestoreMode.values());
        restoreModeSelector.setValue(RestoreMode.RESTORE_ALL);
        restoreModeSelector.getStyleClass().add("restore-mode-selector");
        
        // Options checkboxes
        VBox optionsCheckboxes = new VBox(4);
        optionsCheckboxes.getStyleClass().add("restore-options-checkboxes");
        
        overwriteExisting = new CheckBox("Overwrite existing files");
        overwriteExisting.getStyleClass().add("restore-option-checkbox");
        overwriteExisting.setSelected(false);
        
        restorePermissions = new CheckBox("Restore file permissions");
        restorePermissions.getStyleClass().add("restore-option-checkbox");
        restorePermissions.setSelected(true);
        
        restoreTimestamps = new CheckBox("Restore original timestamps");
        restoreTimestamps.getStyleClass().add("restore-option-checkbox");
        restoreTimestamps.setSelected(true);
        
        optionsCheckboxes.getChildren().addAll(overwriteExisting, restorePermissions, restoreTimestamps);
        
        restoreOptionsArea.getChildren().addAll(
            optionsLabel, locationLabel, locationRow, passwordLabel, passwordField,
            modeLabel, restoreModeSelector, optionsCheckboxes
        );
        
        // Progress area
        progressArea = new VBox(8);
        progressArea.getStyleClass().add("restore-progress-area");
        progressArea.setVisible(false);
        
        Label progressLabel = new Label("Restore Progress:");
        progressLabel.getStyleClass().add("section-label");
        
        overallProgress = new ProgressBar();
        overallProgress.getStyleClass().add("overall-progress");
        overallProgress.setPrefWidth(Double.MAX_VALUE);
        
        currentFileProgress = new ProgressBar();
        currentFileProgress.getStyleClass().add("current-file-progress");
        currentFileProgress.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready to restore");
        statusLabel.getStyleClass().add("restore-status-label");
        
        currentFileLabel = new Label("");
        currentFileLabel.getStyleClass().add("current-file-label");
        
        speedLabel = new Label("");
        speedLabel.getStyleClass().add("speed-label");
        
        progressArea.getChildren().addAll(
            progressLabel, overallProgress, currentFileProgress,
            statusLabel, currentFileLabel, speedLabel
        );
        
        // Button area
        buttonArea = new HBox(8);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);
        buttonArea.getStyleClass().add("restore-button-area");
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "cancel-button");
        
        restoreButton = new Button("Restore Backup");
        restoreButton.getStyleClass().addAll("button", "primary", "restore-button");
        restoreButton.setDisable(true);
        
        buttonArea.getChildren().addAll(cancelButton, restoreButton);
        
        mainContainer.getChildren().addAll(
            headerBar, backupSelectionArea, restoreOptionsArea, progressArea, buttonArea
        );
        
        // Create scene
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
        dialogStage.setScene(scene);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        mainContainer.setSpacing(0);
        mainContainer.setPadding(new Insets(0));
        
        headerBar.setPadding(new Insets(12));
        backupSelectionArea.setPadding(new Insets(0, 12, 12, 12));
        restoreOptionsArea.setPadding(new Insets(12));
        progressArea.setPadding(new Insets(12));
        buttonArea.setPadding(new Insets(12));
        
        VBox.setVgrow(backupSelectionArea, Priority.ALWAYS);
        VBox.setVgrow(backupContentsListView, Priority.ALWAYS);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        // Styling handled via CSS
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Close button
        closeButton.setOnAction(e -> cancel());
        
        // Dialog close
        dialogStage.setOnCloseRequest(e -> {
            if (restoreInProgress) {
                e.consume();
                showCancelConfirmation();
            } else {
                cancel();
            }
        });
        
        // Browse backup file
        browseBackupButton.setOnAction(e -> browseBackupFile());
        
        // Browse restore location
        browseRestoreLocationButton.setOnAction(e -> browseRestoreLocation());
        
        // Form validation
        passwordField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        
        // Buttons
        restoreButton.setOnAction(e -> startRestore());
        cancelButton.setOnAction(e -> {
            if (restoreInProgress) {
                showCancelConfirmation();
            } else {
                cancel();
            }
        });
    }
    
    /**
     * Initialize default values
     */
    private void initializeDefaults() {
        // Set default restore location
        restoreLocation = new File(System.getProperty("user.home"), "Restored_Files");
        restoreLocationField.setText(restoreLocation.getAbsolutePath());
    }
    
    /**
     * Browse for backup file
     */
    private void browseBackupFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup Files", "*.gvb")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            selectedBackupFile = selectedFile;
            backupFileField.setText(selectedFile.getAbsolutePath());
            loadBackupInfo();
        }
    }
    
    /**
     * Browse for restore location
     */
    private void browseRestoreLocation() {
        ModernFileChooser fileChooser = new ModernFileChooser();
        fileChooser.setDirectorySelection(true);
        fileChooser.setOnFilesSelected(files -> {
            if (!files.isEmpty()) {
                restoreLocation = files.get(0);
                restoreLocationField.setText(restoreLocation.getAbsolutePath());
                validateForm();
            }
        });
        
        fileChooser.showDialog(dialogStage);
    }
    
    /**
     * Load backup file information
     */
    private void loadBackupInfo() {
        if (selectedBackupFile == null || !selectedBackupFile.exists()) {
            return;
        }
        
        Task<BackupMetadata> loadTask = new Task<BackupMetadata>() {
            @Override
            protected BackupMetadata call() throws Exception {
                return readBackupMetadata(selectedBackupFile);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    backupMetadata = getValue();
                    displayBackupInfo();
                    validateForm();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("Error reading backup file: " + getException().getMessage());
                    backupInfoPanel.setVisible(false);
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Read backup metadata from file
     */
    private BackupMetadata readBackupMetadata(File backupFile) throws Exception {
        BackupMetadata metadata = new BackupMetadata();
        
        try (FileInputStream fis = new FileInputStream(backupFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            // Read magic number
            byte[] magic = new byte[4];
            bis.read(magic);
            if (!new String(magic).equals("GVBK")) {
                throw new IllegalArgumentException("Invalid backup file format");
            }
            
            // Read version
            byte[] version = new byte[2];
            bis.read(version);
            metadata.setVersion(version[0] + "." + version[1]);
            
            // Read encryption method
            int encryptionMethod = bis.read();
            metadata.setEncryptionMethod(encryptionMethod);
            
            // Read compression level
            int compressionLevel = bis.read();
            metadata.setCompressionLevel(compressionLevel);
            
            // Read timestamp
            long timestamp = 0;
            for (int i = 0; i < 8; i++) {
                timestamp = (timestamp << 8) | (bis.read() & 0xFF);
            }
            metadata.setCreationTime(timestamp);
            
            // Read IV
            byte[] iv = new byte[16];
            bis.read(iv);
            metadata.setIv(iv);
            
            // For now, we can't read the actual contents without the password
            // We'll populate the contents list when the user provides the password
            
        }
        
        return metadata;
    }
    
    /**
     * Display backup information
     */
    private void displayBackupInfo() {
        if (backupMetadata == null) {
            return;
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Backup Version: ").append(backupMetadata.getVersion()).append("\\n");
        info.append("Created: ").append(new java.util.Date(backupMetadata.getCreationTime())).append("\\n");
        info.append("Encryption: ").append(getEncryptionMethodName(backupMetadata.getEncryptionMethod())).append("\\n");
        info.append("Compression: ").append(getCompressionLevelName(backupMetadata.getCompressionLevel()));
        
        backupInfoLabel.setText(info.toString());
        backupInfoPanel.setVisible(true);
        
        // Clear contents list (will be populated when password is provided)
        backupContentsListView.getItems().clear();
    }
    
    /**
     * Get encryption method name
     */
    private String getEncryptionMethodName(int method) {
        switch (method) {
            case 1: return "AES-128-CBC";
            case 2: return "AES-256-CBC";
            case 3: return "AES-256-GCM";
            default: return "Unknown";
        }
    }
    
    /**
     * Get compression level name
     */
    private String getCompressionLevelName(int level) {
        switch (level) {
            case 0: return "None";
            case 1: return "Fast";
            case 6: return "Standard";
            case 9: return "Maximum";
            default: return "Level " + level;
        }
    }
    
    /**
     * Validate form inputs
     */
    private void validateForm() {
        boolean valid = true;
        
        // Check backup file
        if (selectedBackupFile == null || !selectedBackupFile.exists()) {
            valid = false;
        }
        
        // Check restore location
        if (restoreLocation == null) {
            valid = false;
        }
        
        // Check password
        if (passwordField.getText().isEmpty()) {
            valid = false;
        }
        
        restoreButton.setDisable(!valid);
    }
    
    /**
     * Start restore process
     */
    private void startRestore() {
        if (!validateInputs()) {
            return;
        }
        
        restoreInProgress = true;
        
        // Show progress area and hide config
        backupSelectionArea.setVisible(false);
        restoreOptionsArea.setVisible(false);
        progressArea.setVisible(true);
        
        // Update button states
        restoreButton.setDisable(true);
        cancelButton.setText("Cancel Restore");
        
        // Create restore task
        restoreTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                performRestore();
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    restoreInProgress = false;
                    statusLabel.setText("Restore completed successfully");
                    
                    if (onRestoreCompleted != null) {
                        onRestoreCompleted.accept(restoreLocation);
                    }
                    
                    // Auto-close after delay
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(2000);
                            dialogStage.close();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    restoreInProgress = false;
                    statusLabel.setText("Restore failed: " + getException().getMessage());
                    cancelButton.setText("Close");
                });
            }
            
            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    restoreInProgress = false;
                    statusLabel.setText("Restore cancelled");
                    cancelButton.setText("Close");
                });
            }
        };
        
        Thread restoreThread = new Thread(restoreTask);
        restoreThread.setDaemon(true);
        restoreThread.start();
    }
    
    /**
     * Validate inputs before starting restore
     */
    private boolean validateInputs() {
        // Ensure restore location exists or can be created
        if (!restoreLocation.exists() && !restoreLocation.mkdirs()) {
            showAlert("Cannot create restore location directory.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Perform the actual restore operation
     */
    private void performRestore() throws Exception {
        Platform.runLater(() -> {
            statusLabel.setText("Decrypting backup...");
            overallProgress.setProgress(0);
        });
        
        // Generate decryption key from password
        String password = passwordField.getText();
        SecretKeySpec decryptionKey = generateKeyFromPassword(password);
        
        // Create cipher for decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(backupMetadata.getIv());
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
        
        // Decrypt and extract backup
        try (FileInputStream fis = new FileInputStream(selectedBackupFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            // Skip header (already read during metadata loading)
            bis.skip(4 + 2 + 1 + 1 + 8 + 16); // magic + version + encryption + compression + timestamp + IV
            
            // Create temporary file for decrypted content
            File tempFile = File.createTempFile("restore_temp", ".zip");
            try {
                decryptToFile(bis, tempFile, cipher);
                extractZipFile(tempFile, restoreLocation);
            } finally {
                tempFile.delete();
            }
        }
        
        Platform.runLater(() -> {
            overallProgress.setProgress(1.0);
            currentFileProgress.setProgress(1.0);
        });
    }
    
    /**
     * Generate decryption key from password
     */
    private SecretKeySpec generateKeyFromPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Decrypt backup content to file
     */
    private void decryptToFile(InputStream encryptedInput, File outputFile, Cipher cipher) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            byte[] buffer = new byte[8192];
            int len;
            
            while ((len = encryptedInput.read(buffer)) > 0) {
                byte[] decryptedData = cipher.update(buffer, 0, len);
                if (decryptedData != null) {
                    bos.write(decryptedData);
                }
                
                if (restoreTask.isCancelled()) {
                    break;
                }
            }
            
            // Write final decrypted block
            byte[] finalData = cipher.doFinal();
            if (finalData != null) {
                bos.write(finalData);
            }
        }
    }
    
    /**
     * Extract ZIP file to destination
     */
    private void extractZipFile(File zipFile, File destination) throws Exception {
        Platform.runLater(() -> {
            statusLabel.setText("Extracting files...");
        });
        
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            int extractedFiles = 0;
            
            while ((entry = zis.getNextEntry()) != null) {
                if (restoreTask.isCancelled()) {
                    break;
                }
                
                Platform.runLater(() -> {
                    currentFileLabel.setText("Extracting: " + entry.getName());
                    currentFileProgress.setProgress(0);
                });
                
                File outputFile = new File(destination, entry.getName());
                
                // Check if file already exists
                if (outputFile.exists() && !overwriteExisting.isSelected()) {
                    continue; // Skip existing files
                }
                
                // Create parent directories
                outputFile.getParentFile().mkdirs();
                
                // Extract file
                try (FileOutputStream fos = new FileOutputStream(outputFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    
                    byte[] buffer = new byte[8192];
                    long totalBytes = entry.getSize();
                    long bytesExtracted = 0;
                    int len;
                    
                    while ((len = zis.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                        bytesExtracted += len;
                        
                        if (totalBytes > 0) {
                            final double progress = (double) bytesExtracted / totalBytes;
                            Platform.runLater(() -> currentFileProgress.setProgress(progress));
                        }
                        
                        if (restoreTask.isCancelled()) {
                            break;
                        }
                    }
                }
                
                // Restore timestamps if requested
                if (restoreTimestamps.isSelected()) {
                    outputFile.setLastModified(entry.getTime());
                }
                
                extractedFiles++;
                zis.closeEntry();
            }
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Restore Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show cancellation confirmation
     */
    private void showCancelConfirmation() {
        Alert cancelAlert = new Alert(Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Restore");
        cancelAlert.setHeaderText("Cancel restore operation?");
        cancelAlert.setContentText("The restore process will be stopped and any partially restored files may be incomplete.");
        
        cancelAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (restoreTask != null) {
                    restoreTask.cancel();
                }
            }
        });
    }
    
    /**
     * Cancel and close dialog
     */
    private void cancel() {
        if (onCancelled != null) {
            onCancelled.run();
        }
        dialogStage.close();
    }
    
    /**
     * Custom list cell for backup entries
     */
    private class BackupEntryListCell extends ListCell<BackupEntry> {
        private HBox content;
        private Label nameLabel;
        private Label sizeLabel;
        
        public BackupEntryListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(4, 8, 4, 8));
            
            nameLabel = new Label();
            nameLabel.getStyleClass().add("backup-entry-name");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            sizeLabel = new Label();
            sizeLabel.getStyleClass().add("backup-entry-size");
            
            content.getChildren().addAll(nameLabel, spacer, sizeLabel);
        }
        
        @Override
        protected void updateItem(BackupEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            
            if (empty || entry == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(entry.getName());
                sizeLabel.setText(formatFileSize(entry.getSize()));
                setGraphic(content);
            }
        }
        
        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    // Public API
    
    /**
     * Show the restore dialog
     */
    public void showDialog(Window owner) {
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        
        dialogStage.showAndWait();
    }
    
    /**
     * Show dialog with pre-selected backup file
     */
    public void showDialog(Window owner, File backupFile) {
        if (backupFile != null && backupFile.exists()) {
            selectedBackupFile = backupFile;
            backupFileField.setText(backupFile.getAbsolutePath());
            loadBackupInfo();
        }
        
        showDialog(owner);
    }
    
    // Getters and Setters
    
    public void setOnRestoreCompleted(Consumer<File> callback) {
        this.onRestoreCompleted = callback;
    }
    
    public void setOnCancelled(Runnable callback) {
        this.onCancelled = callback;
    }
    
    // Helper classes
    
    /**
     * Backup metadata information
     */
    public static class BackupMetadata {
        private String version;
        private int encryptionMethod;
        private int compressionLevel;
        private long creationTime;
        private byte[] iv;
        
        // Getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public int getEncryptionMethod() { return encryptionMethod; }
        public void setEncryptionMethod(int encryptionMethod) { this.encryptionMethod = encryptionMethod; }
        
        public int getCompressionLevel() { return compressionLevel; }
        public void setCompressionLevel(int compressionLevel) { this.compressionLevel = compressionLevel; }
        
        public long getCreationTime() { return creationTime; }
        public void setCreationTime(long creationTime) { this.creationTime = creationTime; }
        
        public byte[] getIv() { return iv; }
        public void setIv(byte[] iv) { this.iv = iv; }
    }
    
    /**
     * Backup entry information
     */
    public static class BackupEntry {
        private String name;
        private long size;
        private boolean isDirectory;
        
        public BackupEntry(String name, long size, boolean isDirectory) {
            this.name = name;
            this.size = size;
            this.isDirectory = isDirectory;
        }
        
        public String getName() { return name; }
        public long getSize() { return size; }
        public boolean isDirectory() { return isDirectory; }
    }
    
    /**
     * Restore modes
     */
    public enum RestoreMode {
        RESTORE_ALL("Restore All Files", "Restore all files from the backup"),
        RESTORE_SELECTED("Restore Selected Files", "Choose specific files to restore"),
        RESTORE_NEWER("Restore Newer Files Only", "Only restore files newer than existing ones"),
        RESTORE_MISSING("Restore Missing Files Only", "Only restore files that don't exist");
        
        private final String displayName;
        private final String description;
        
        RestoreMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() { return displayName; }
    }
}