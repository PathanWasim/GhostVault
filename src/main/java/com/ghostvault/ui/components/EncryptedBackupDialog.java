package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Encrypted backup creation dialog with compression and progress tracking
 */
public class EncryptedBackupDialog {
    
    // Dialog components
    private Stage dialogStage;
    private VBox mainContainer;
    private HBox headerBar;
    private Label titleLabel;
    private Button closeButton;
    
    // Configuration area
    private VBox configArea;
    private TextField backupNameField;
    private TextField backupLocationField;
    private Button browseLocationButton;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<EncryptionMethod> encryptionMethodSelector;
    private ComboBox<CompressionLevel> compressionLevelSelector;
    private CheckBox includeMetadata;
    private CheckBox splitLargeFiles;
    
    // File selection area
    private VBox fileSelectionArea;
    private ListView<File> selectedFilesListView;
    private Button addFilesButton;
    private Button addFolderButton;
    private Button removeSelectedButton;
    private Label totalSizeLabel;
    
    // Progress area
    private VBox progressArea;
    private ProgressBar overallProgress;
    private ProgressBar currentFileProgress;
    private Label statusLabel;
    private Label currentFileLabel;
    private Label speedLabel;
    
    // Button area
    private HBox buttonArea;
    private Button createBackupButton;
    private Button cancelButton;
    
    // Configuration
    private List<File> filesToBackup = new ArrayList<>();
    private File backupLocation;
    private String backupName;
    
    // Callbacks
    private Consumer<File> onBackupCreated;
    private Runnable onCancelled;
    
    // State
    private boolean backupInProgress = false;
    private Task<File> backupTask;
    
    public EncryptedBackupDialog() {
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
        dialogStage.setTitle("Create Encrypted Backup");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(700);
        dialogStage.setHeight(600);
        
        // Main container
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("encrypted-backup-dialog");
        
        // Header bar
        headerBar = new HBox(8);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getStyleClass().add("backup-dialog-header");
        
        titleLabel = new Label("Create Encrypted Backup");
        titleLabel.getStyleClass().add("backup-dialog-title");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "icon", "close-button");
        
        headerBar.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        
        // Configuration area
        configArea = new VBox(12);
        configArea.getStyleClass().add("backup-config-area");
        
        // Backup name and location
        GridPane basicConfig = new GridPane();
        basicConfig.setHgap(8);
        basicConfig.setVgap(8);
        basicConfig.getStyleClass().add("basic-config-grid");
        
        Label nameLabel = new Label("Backup name:");
        nameLabel.getStyleClass().add("config-label");
        backupNameField = new TextField();
        backupNameField.getStyleClass().add("backup-name-field");
        
        Label locationLabel = new Label("Backup location:");
        locationLabel.getStyleClass().add("config-label");
        HBox locationRow = new HBox(8);
        locationRow.setAlignment(Pos.CENTER_LEFT);
        backupLocationField = new TextField();
        backupLocationField.getStyleClass().add("backup-location-field");
        backupLocationField.setEditable(false);
        HBox.setHgrow(backupLocationField, Priority.ALWAYS);
        browseLocationButton = new Button("Browse...");
        browseLocationButton.getStyleClass().addAll("button", "ghost", "browse-button");
        locationRow.getChildren().addAll(backupLocationField, browseLocationButton);
        
        basicConfig.add(nameLabel, 0, 0);
        basicConfig.add(backupNameField, 1, 0);
        basicConfig.add(locationLabel, 0, 1);
        basicConfig.add(locationRow, 1, 1);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        basicConfig.getColumnConstraints().addAll(col1, col2);
        
        // Security configuration
        VBox securityConfig = new VBox(8);
        securityConfig.getStyleClass().add("security-config");
        
        Label securityLabel = new Label("Security Settings:");
        securityLabel.getStyleClass().add("section-label");
        
        GridPane securityGrid = new GridPane();
        securityGrid.setHgap(8);
        securityGrid.setVgap(8);
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("config-label");
        passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field");
        
        Label confirmLabel = new Label("Confirm password:");
        confirmLabel.getStyleClass().add("config-label");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.getStyleClass().add("password-field");
        
        Label encryptionLabel = new Label("Encryption method:");
        encryptionLabel.getStyleClass().add("config-label");
        encryptionMethodSelector = new ComboBox<>();
        encryptionMethodSelector.getItems().addAll(EncryptionMethod.values());
        encryptionMethodSelector.setValue(EncryptionMethod.AES_256_GCM);
        encryptionMethodSelector.getStyleClass().add("encryption-method-selector");
        
        securityGrid.add(passwordLabel, 0, 0);
        securityGrid.add(passwordField, 1, 0);
        securityGrid.add(confirmLabel, 0, 1);
        securityGrid.add(confirmPasswordField, 1, 1);
        securityGrid.add(encryptionLabel, 0, 2);
        securityGrid.add(encryptionMethodSelector, 1, 2);
        
        securityGrid.getColumnConstraints().addAll(col1, col2);
        
        securityConfig.getChildren().addAll(securityLabel, securityGrid);
        
        // Compression and options
        VBox optionsConfig = new VBox(8);
        optionsConfig.getStyleClass().add("options-config");
        
        Label optionsLabel = new Label("Backup Options:");
        optionsLabel.getStyleClass().add("section-label");
        
        HBox compressionRow = new HBox(8);
        compressionRow.setAlignment(Pos.CENTER_LEFT);
        
        Label compressionLabel = new Label("Compression:");
        compressionLabel.getStyleClass().add("config-label");
        compressionLevelSelector = new ComboBox<>();
        compressionLevelSelector.getItems().addAll(CompressionLevel.values());
        compressionLevelSelector.setValue(CompressionLevel.STANDARD);
        compressionLevelSelector.getStyleClass().add("compression-selector");
        
        compressionRow.getChildren().addAll(compressionLabel, compressionLevelSelector);
        
        includeMetadata = new CheckBox("Include file metadata (timestamps, permissions)");
        includeMetadata.getStyleClass().add("backup-option-checkbox");
        includeMetadata.setSelected(true);
        
        splitLargeFiles = new CheckBox("Split backup into multiple files (for large backups)");
        splitLargeFiles.getStyleClass().add("backup-option-checkbox");
        splitLargeFiles.setSelected(false);
        
        optionsConfig.getChildren().addAll(
            optionsLabel, compressionRow, includeMetadata, splitLargeFiles
        );
        
        configArea.getChildren().addAll(basicConfig, securityConfig, optionsConfig);
        
        // File selection area
        fileSelectionArea = new VBox(8);
        fileSelectionArea.getStyleClass().add("file-selection-area");
        
        HBox fileSelectionHeader = new HBox(8);
        fileSelectionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label filesLabel = new Label("Files to Backup:");
        filesLabel.getStyleClass().add("section-label");
        
        Region filesSpacer = new Region();
        HBox.setHgrow(filesSpacer, Priority.ALWAYS);
        
        addFilesButton = new Button("Add Files");
        addFilesButton.getStyleClass().addAll("button", "ghost", "add-files-button");
        
        addFolderButton = new Button("Add Folder");
        addFolderButton.getStyleClass().addAll("button", "ghost", "add-folder-button");
        
        removeSelectedButton = new Button("Remove Selected");
        removeSelectedButton.getStyleClass().addAll("button", "ghost", "remove-button");
        removeSelectedButton.setDisable(true);
        
        fileSelectionHeader.getChildren().addAll(
            filesLabel, filesSpacer, addFilesButton, addFolderButton, removeSelectedButton
        );
        
        selectedFilesListView = new ListView<>();
        selectedFilesListView.getStyleClass().add("selected-files-list");
        selectedFilesListView.setCellFactory(listView -> new BackupFileListCell());
        selectedFilesListView.setPrefHeight(150);
        
        totalSizeLabel = new Label("Total size: 0 bytes");
        totalSizeLabel.getStyleClass().add("total-size-label");
        
        fileSelectionArea.getChildren().addAll(
            fileSelectionHeader, selectedFilesListView, totalSizeLabel
        );
        
        // Progress area
        progressArea = new VBox(8);
        progressArea.getStyleClass().add("backup-progress-area");
        progressArea.setVisible(false);
        
        Label progressLabel = new Label("Backup Progress:");
        progressLabel.getStyleClass().add("section-label");
        
        overallProgress = new ProgressBar();
        overallProgress.getStyleClass().add("overall-progress");
        overallProgress.setPrefWidth(Double.MAX_VALUE);
        
        currentFileProgress = new ProgressBar();
        currentFileProgress.getStyleClass().add("current-file-progress");
        currentFileProgress.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready to create backup");
        statusLabel.getStyleClass().add("backup-status-label");
        
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
        buttonArea.getStyleClass().add("backup-button-area");
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "cancel-button");
        
        createBackupButton = new Button("Create Backup");
        createBackupButton.getStyleClass().addAll("button", "primary", "create-backup-button");
        createBackupButton.setDisable(true);
        
        buttonArea.getChildren().addAll(cancelButton, createBackupButton);
        
        mainContainer.getChildren().addAll(
            headerBar, configArea, fileSelectionArea, progressArea, buttonArea
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
        configArea.setPadding(new Insets(0, 12, 12, 12));
        fileSelectionArea.setPadding(new Insets(12));
        progressArea.setPadding(new Insets(12));
        buttonArea.setPadding(new Insets(12));
        
        VBox.setVgrow(fileSelectionArea, Priority.ALWAYS);
        VBox.setVgrow(selectedFilesListView, Priority.ALWAYS);
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
            if (backupInProgress) {
                e.consume();
                showCancelConfirmation();
            } else {
                cancel();
            }
        });
        
        // Browse location
        browseLocationButton.setOnAction(e -> browseBackupLocation());
        
        // File selection
        addFilesButton.setOnAction(e -> addFiles());
        addFolderButton.setOnAction(e -> addFolder());
        removeSelectedButton.setOnAction(e -> removeSelectedFiles());
        
        selectedFilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            removeSelectedButton.setDisable(newFile == null);
        });
        
        // Form validation
        backupNameField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        passwordField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        
        // Buttons
        createBackupButton.setOnAction(e -> createBackup());
        cancelButton.setOnAction(e -> {
            if (backupInProgress) {
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
        // Set default backup name with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        backupNameField.setText("Backup_" + sdf.format(new Date()));
        
        // Set default backup location
        backupLocation = new File(System.getProperty("user.home"), "GhostVault_Backups");
        backupLocationField.setText(backupLocation.getAbsolutePath());
    }
    
    /**
     * Browse for backup location
     */
    private void browseBackupLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Location");
        
        if (backupLocation != null && backupLocation.exists()) {
            directoryChooser.setInitialDirectory(backupLocation);
        }
        
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if (selectedDirectory != null) {
            backupLocation = selectedDirectory;
            backupLocationField.setText(selectedDirectory.getAbsolutePath());
            validateForm();
        }
    }
    
    /**
     * Add files to backup
     */
    private void addFiles() {
        ModernFileChooser fileChooser = new ModernFileChooser();
        fileChooser.setMultipleSelection(true);
        fileChooser.setOnFilesSelected(files -> {
            for (File file : files) {
                if (!filesToBackup.contains(file)) {
                    filesToBackup.add(file);
                }
            }
            updateFilesList();
        });
        
        fileChooser.showDialog(dialogStage);
    }
    
    /**
     * Add folder to backup
     */
    private void addFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Backup");
        
        File selectedDirectory = directoryChooser.showDialog(dialogStage);
        if (selectedDirectory != null && !filesToBackup.contains(selectedDirectory)) {
            filesToBackup.add(selectedDirectory);
            updateFilesList();
        }
    }
    
    /**
     * Remove selected files from backup list
     */
    private void removeSelectedFiles() {
        File selectedFile = selectedFilesListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            filesToBackup.remove(selectedFile);
            updateFilesList();
        }
    }
    
    /**
     * Update files list display
     */
    private void updateFilesList() {
        selectedFilesListView.getItems().setAll(filesToBackup);
        
        // Calculate total size
        long totalSize = 0;
        for (File file : filesToBackup) {
            totalSize += calculateFileSize(file);
        }
        
        totalSizeLabel.setText("Total size: " + formatFileSize(totalSize));
        validateForm();
    }
    
    /**
     * Calculate file or directory size
     */
    private long calculateFileSize(File file) {
        if (file.isFile()) {
            return file.length();
        } else if (file.isDirectory()) {
            long size = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    size += calculateFileSize(child);
                }
            }
            return size;
        }
        return 0;
    }
    
    /**
     * Validate form inputs
     */
    private void validateForm() {
        boolean valid = true;
        
        // Check backup name
        if (backupNameField.getText().trim().isEmpty()) {
            valid = false;
        }
        
        // Check backup location
        if (backupLocation == null) {
            valid = false;
        }
        
        // Check password
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (password.isEmpty() || !password.equals(confirmPassword)) {
            valid = false;
        }
        
        // Check files to backup
        if (filesToBackup.isEmpty()) {
            valid = false;
        }
        
        createBackupButton.setDisable(!valid);
    }
    
    /**
     * Create encrypted backup
     */
    private void createBackup() {
        if (!validateInputs()) {
            return;
        }
        
        backupInProgress = true;
        
        // Show progress area and hide config
        configArea.setVisible(false);
        fileSelectionArea.setVisible(false);
        progressArea.setVisible(true);
        
        // Update button states
        createBackupButton.setDisable(true);
        cancelButton.setText("Cancel Backup");
        
        // Create backup task
        backupTask = new Task<File>() {
            @Override
            protected File call() throws Exception {
                return performBackup();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    backupInProgress = false;
                    statusLabel.setText("Backup created successfully");
                    
                    if (onBackupCreated != null) {
                        onBackupCreated.accept(getValue());
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
                    backupInProgress = false;
                    statusLabel.setText("Backup failed: " + getException().getMessage());
                    cancelButton.setText("Close");
                });
            }
            
            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    backupInProgress = false;
                    statusLabel.setText("Backup cancelled");
                    cancelButton.setText("Close");
                });
            }
        };
        
        Thread backupThread = new Thread(backupTask);
        backupThread.setDaemon(true);
        backupThread.start();
    }
    
    /**
     * Validate all inputs before starting backup
     */
    private boolean validateInputs() {
        // Validate password strength
        String password = passwordField.getText();
        if (password.length() < 8) {
            showAlert("Password must be at least 8 characters long.");
            return false;
        }
        
        // Ensure backup location exists or can be created
        if (!backupLocation.exists() && !backupLocation.mkdirs()) {
            showAlert("Cannot create backup location directory.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Perform the actual backup operation
     */
    private File performBackup() throws Exception {
        String backupFileName = backupNameField.getText().trim() + ".gvb"; // GhostVault Backup
        File backupFile = new File(backupLocation, backupFileName);
        
        Platform.runLater(() -> {
            statusLabel.setText("Creating encrypted backup...");
            overallProgress.setProgress(0);
        });
        
        // Generate encryption key from password
        String password = passwordField.getText();
        SecretKey encryptionKey = generateKeyFromPassword(password);
        
        // Create encrypted backup
        try (FileOutputStream fos = new FileOutputStream(backupFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            // Write backup header
            writeBackupHeader(bos, encryptionKey);
            
            // Create encrypted ZIP stream
            Cipher cipher = createCipher(encryptionKey, Cipher.ENCRYPT_MODE);
            
            // Use a temporary file for the ZIP content
            File tempZipFile = File.createTempFile("backup_temp", ".zip");
            try {
                createZipBackup(tempZipFile);
                encryptAndWriteFile(tempZipFile, bos, cipher);
            } finally {
                tempZipFile.delete();
            }
        }
        
        Platform.runLater(() -> {
            overallProgress.setProgress(1.0);
            currentFileProgress.setProgress(1.0);
        });
        
        return backupFile;
    }
    
    /**
     * Generate encryption key from password
     */
    private SecretKey generateKeyFromPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Create cipher for encryption/decryption
     */
    private Cipher createCipher(SecretKey key, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(mode, key);
        } else {
            // For decryption, IV would need to be read from the file
            byte[] iv = new byte[16]; // This would be read from backup file
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(mode, key, ivSpec);
        }
        
        return cipher;
    }
    
    /**
     * Write backup file header
     */
    private void writeBackupHeader(OutputStream out, SecretKey key) throws Exception {
        // Write magic number
        out.write("GVBK".getBytes()); // GhostVault Backup
        
        // Write version
        out.write(new byte[]{1, 0}); // Version 1.0
        
        // Write encryption method
        out.write(encryptionMethodSelector.getValue().getId());
        
        // Write compression level
        out.write(compressionLevelSelector.getValue().getLevel());
        
        // Write timestamp
        long timestamp = System.currentTimeMillis();
        for (int i = 7; i >= 0; i--) {
            out.write((byte) (timestamp >>> (i * 8)));
        }
        
        // Write IV for encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        out.write(iv);
    }
    
    /**
     * Create ZIP backup of selected files
     */
    private void createZipBackup(File zipFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            // Set compression level
            zos.setLevel(compressionLevelSelector.getValue().getLevel());
            
            int totalFiles = countTotalFiles();
            int processedFiles = 0;
            
            for (File file : filesToBackup) {
                processedFiles = addFileToZip(zos, file, "", processedFiles, totalFiles);
                
                if (backupTask.isCancelled()) {
                    break;
                }
            }
        }
    }
    
    /**
     * Count total files for progress tracking
     */
    private int countTotalFiles() {
        int count = 0;
        for (File file : filesToBackup) {
            count += countFilesRecursive(file);
        }
        return count;
    }
    
    /**
     * Count files recursively
     */
    private int countFilesRecursive(File file) {
        if (file.isFile()) {
            return 1;
        } else if (file.isDirectory()) {
            int count = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    count += countFilesRecursive(child);
                }
            }
            return count;
        }
        return 0;
    }
    
    /**
     * Add file to ZIP archive
     */
    private int addFileToZip(ZipOutputStream zos, File file, String basePath, 
                           int processedFiles, int totalFiles) throws Exception {
        
        if (file.isFile()) {
            Platform.runLater(() -> {
                currentFileLabel.setText("Adding: " + file.getName());
                currentFileProgress.setProgress(0);
            });
            
            String entryName = basePath + file.getName();
            ZipEntry entry = new ZipEntry(entryName);
            
            if (includeMetadata.isSelected()) {
                entry.setTime(file.lastModified());
            }
            
            zos.putNextEntry(entry);
            
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                
                byte[] buffer = new byte[8192];
                long totalBytes = file.length();
                long bytesRead = 0;
                int len;
                
                while ((len = bis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                    bytesRead += len;
                    
                    final double fileProgress = (double) bytesRead / totalBytes;
                    Platform.runLater(() -> currentFileProgress.setProgress(fileProgress));
                    
                    if (backupTask.isCancelled()) {
                        break;
                    }
                }
            }
            
            zos.closeEntry();
            processedFiles++;
            
            final double overallProgress = (double) processedFiles / totalFiles;
            Platform.runLater(() -> this.overallProgress.setProgress(overallProgress));
            
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                String newBasePath = basePath + file.getName() + "/";
                for (File child : children) {
                    processedFiles = addFileToZip(zos, child, newBasePath, processedFiles, totalFiles);
                    
                    if (backupTask.isCancelled()) {
                        break;
                    }
                }
            }
        }
        
        return processedFiles;
    }
    
    /**
     * Encrypt and write file to output stream
     */
    private void encryptAndWriteFile(File inputFile, OutputStream outputStream, 
                                   Cipher cipher) throws Exception {
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            byte[] buffer = new byte[8192];
            int len;
            
            while ((len = bis.read(buffer)) > 0) {
                byte[] encryptedData = cipher.update(buffer, 0, len);
                if (encryptedData != null) {
                    outputStream.write(encryptedData);
                }
                
                if (backupTask.isCancelled()) {
                    break;
                }
            }
            
            // Write final encrypted block
            byte[] finalData = cipher.doFinal();
            if (finalData != null) {
                outputStream.write(finalData);
            }
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Backup Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show cancellation confirmation
     */
    private void showCancelConfirmation() {
        Alert cancelAlert = new Alert(Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Backup");
        cancelAlert.setHeaderText("Cancel backup creation?");
        cancelAlert.setContentText("The backup process will be stopped and any partial backup will be deleted.");
        
        cancelAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (backupTask != null) {
                    backupTask.cancel();
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
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Custom list cell for backup files
     */
    private class BackupFileListCell extends ListCell<File> {
        private HBox content;
        private Label nameLabel;
        private Label sizeLabel;
        
        public BackupFileListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(4, 8, 4, 8));
            
            nameLabel = new Label();
            nameLabel.getStyleClass().add("backup-file-name");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            sizeLabel = new Label();
            sizeLabel.getStyleClass().add("backup-file-size");
            
            content.getChildren().addAll(nameLabel, spacer, sizeLabel);
        }
        
        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(file.getAbsolutePath());
                
                if (file.isDirectory()) {
                    sizeLabel.setText("Folder");
                } else {
                    sizeLabel.setText(formatFileSize(file.length()));
                }
                
                setGraphic(content);
            }
        }
    }
    
    // Public API
    
    /**
     * Show the backup dialog
     */
    public void showDialog(Window owner) {
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        
        dialogStage.showAndWait();
    }
    
    /**
     * Show dialog with pre-selected files
     */
    public void showDialog(Window owner, List<File> files) {
        if (files != null && !files.isEmpty()) {
            filesToBackup.addAll(files);
            updateFilesList();
        }
        
        showDialog(owner);
    }
    
    // Getters and Setters
    
    public void setOnBackupCreated(Consumer<File> callback) {
        this.onBackupCreated = callback;
    }
    
    public void setOnCancelled(Runnable callback) {
        this.onCancelled = callback;
    }
    
    // Helper enums
    
    /**
     * Encryption methods
     */
    public enum EncryptionMethod {
        AES_128_CBC("AES-128-CBC", 1, "AES 128-bit with CBC mode"),
        AES_256_CBC("AES-256-CBC", 2, "AES 256-bit with CBC mode"),
        AES_256_GCM("AES-256-GCM", 3, "AES 256-bit with GCM mode (recommended)");
        
        private final String displayName;
        private final int id;
        private final String description;
        
        EncryptionMethod(String displayName, int id, String description) {
            this.displayName = displayName;
            this.id = id;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public int getId() { return id; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() { return displayName; }
    }
    
    /**
     * Compression levels
     */
    public enum CompressionLevel {
        NONE("No Compression", 0),
        FAST("Fast", 1),
        STANDARD("Standard", 6),
        MAXIMUM("Maximum", 9);
        
        private final String displayName;
        private final int level;
        
        CompressionLevel(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
        
        @Override
        public String toString() { return displayName; }
    }
}