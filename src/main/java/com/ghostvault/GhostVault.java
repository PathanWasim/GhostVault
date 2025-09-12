package com.ghostvault;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.ghostvault.core.*;
import com.ghostvault.ui.*;
import com.ghostvault.security.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GhostVault extends Application {
    private static final String APP_NAME = "GhostVault";
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.ghostvault";
    private static final String CONFIG_FILE = VAULT_DIR + "/config.enc";
    private static final String METADATA_FILE = VAULT_DIR + "/metadata.enc";
    private static final String SALT_FILE = VAULT_DIR + "/.salt";
    private static final String LOG_FILE = VAULT_DIR + "/audit.log.enc";
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int SESSION_TIMEOUT_MINUTES = 15;
    
    // Core components
    private Stage primaryStage;
    private SecurityManager securityManager;
    private MetadataManager metadataManager;
    private FileManager fileManager;
    private UIManager uiManager;
    private SessionManager sessionManager;
    
    // State
    private SecretKey encryptionKey;
    private AtomicInteger failedAttempts = new AtomicInteger(0);
    private boolean isFirstRun = false;
    private boolean isDarkTheme = true;
    
    // UI Components
    private ListView<String> fileListView;
    private TextArea logArea;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize components
        initializeComponents();
        
        // Initialize vault directory and check if first run
        initializeVault();
        
        if (isFirstRun) {
            showInitialSetup();
        } else {
            showLoginScreen();
        }
    }
    
    private void initializeComponents() {
        securityManager = new SecurityManager();
        metadataManager = new MetadataManager(METADATA_FILE);
        fileManager = new FileManager(VAULT_DIR + "/files");
        uiManager = new UIManager();
        sessionManager = new SessionManager(SESSION_TIMEOUT_MINUTES);
        
        // Set up session timeout callback
        sessionManager.setTimeoutCallback(() -> {
            Platform.runLater(() -> {
                showAlert("Session expired
            vaultDir.mkdirs();
            new File(VAULT_DIR + "/files").mkdirs();
            new File(VAULT_DIR + "/decoys").mkdirs();
            isFirstRun = true;
        } else {
            isFirstRun = !new File(CONFIG_FILE).exists();
        }
        
        if (isFirstRun) {
            generateDecoyFiles();
        }
    }
    
    private void showInitialSetup() {
        VBox setupBox = new VBox(20);
        setupBox.setAlignment(Pos.CENTER);
        setupBox.setPadding(new Insets(40));
        
        Label titleLabel = new Label("GhostVault - Initial Setup");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label instructionLabel = new Label("Set up your vault passwords. All three are required for security.");
        instructionLabel.setStyle("-fx-font-size: 14px;");
        instructionLabel.setWrapText(true);
        
        // Master password setup
        VBox masterBox = createPasswordSetupBox("Master Password", "Full access to your vault");
        PasswordField masterField = (PasswordField) masterBox.getChildren().get(1);
        ProgressBar masterStrength = (ProgressBar) masterBox.getChildren().get(2);
        Label masterStrengthLabel = (Label) masterBox.getChildren().get(3);
        
        // Panic password setup
        VBox panicBox = createPasswordSetupBox("Panic Password", "Emergency destruction of all data");
        PasswordField panicField = (PasswordField) panicBox.getChildren().get(1);
        ProgressBar panicStrength = (ProgressBar) panicBox.getChildren().get(2);
        Label panicStrengthLabel = (Label) panicBox.getChildren().get(3);
        
        // Decoy password setup
        VBox decoyBox = createPasswordSetupBox("Decoy Password", "Shows fake files to mislead attackers");
        PasswordField decoyField = (PasswordField) decoyBox.getChildren().get(1);
        ProgressBar decoyStrength = (ProgressBar) decoyBox.getChildren().get(2);
        Label decoyStrengthLabel = (Label) decoyBox.getChildren().get(3);
        
        // Password strength listeners
        masterField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, masterStrength, masterStrengthLabel));
        panicField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, panicStrength, panicStrengthLabel));
        decoyField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, decoyStrength, decoyStrengthLabel));
        
        Button createVaultButton = new Button("Create Vault");
        createVaultButton.setPrefWidth(150);
        createVaultButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        setupBox.getChildren().addAll(titleLabel, instructionLabel, masterBox, panicBox, decoyBox, 
                                     createVaultButton, statusLabel);
        
        createVaultButton.setOnAction(e -> {
            String master = masterField.getText();
            String panic = panicField.getText();
            String decoy = decoyField.getText();
            
            if (validateSetupPasswords(master, panic, decoy, statusLabel)) {
                securityManager.createVaultConfiguration(master, panic, decoy);
                metadataManager.initialize(securityManager.getEncryptionKey());
                showLoginScreen();
            }
        });
        
        Scene scene = new Scene(new ScrollPane(setupBox), 600, 700);
        applyTheme(scene);
        primaryStage.setTitle(APP_NAME + " - Setup");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createPasswordSetupBox(String title, String description) {
        VBox box = new VBox(8);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter " + title.toLowerCase());
        passwordField.setPrefWidth(400);
        
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(400);
        
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-font-size: 12px;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        
        box.getChildren().addAll(titleLabel, passwordField, strengthBar, strengthLabel, descLabel);
        return box;
    }
    
    private boolean validateSetupPasswords(String master, String panic, String decoy, Label statusLabel) {
        if (master.isEmpty() || panic.isEmpty() || decoy.isEmpty()) {
            statusLabel.setText("All password fields are required.");
            return false;
        }
        
        if (getPasswordStrengthScore(master) < 4) {
            statusLabel.setText("Master password is too weak. Minimum requirements not met.");
            return false;
        }
        
        if (getPasswordStrengthScore(panic) < 3) {
            statusLabel.setText("Panic password is too weak. Minimum 3/5 strength required.");
            return false;
        }
        
        if (getPasswordStrengthScore(decoy) < 3) {
            statusLabel.setText("Decoy password is too weak. Minimum 3/5 strength required.");
            return false;
        }
        
        if (master.equals(panic) || master.equals(decoy) || panic.equals(decoy)) {
            statusLabel.setText("All passwords must be different from each other.");
            return false;
        }
        
        return true;
    }
    
    private void showLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        
        Label titleLabel = new Label("GhostVault");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label instructionLabel = new Label("Enter Password");
        instructionLabel.setStyle("-fx-font-size: 14px;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(300);
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        
        Button loginButton = new Button("Access Vault");
        loginButton.setPrefWidth(120);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        // Theme toggle
        ToggleButton themeToggle = new ToggleButton("🌙");
        themeToggle.setSelected(isDarkTheme);
        themeToggle.setStyle("-fx-background-color: transparent; -fx-border-color: gray; -fx-font-size: 16px;");
        
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(themeToggle);
        
        loginBox.getChildren().addAll(titleLabel, instructionLabel, passwordField, loginButton, statusLabel);
        
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(topBar, loginBox);
        VBox.setVgrow(loginBox, Priority.ALWAYS);
        
        loginButton.setOnAction(e -> handleLogin(passwordField.getText(), statusLabel, passwordField));
        passwordField.setOnAction(e -> loginButton.fire());
        
        themeToggle.setOnAction(e -> {
            isDarkTheme = themeToggle.isSelected();
            applyTheme(primaryStage.getScene());
        });
        
        Scene scene = new Scene(mainContainer, 500, 400);
        applyTheme(scene);
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Focus on password field
        Platform.runLater(() -> passwordField.requestFocus());
    }
    
    private void handleLogin(String password, Label statusLabel, PasswordField passwordField) {
        if (password.isEmpty()) {
            statusLabel.setText("Please enter a password.");
            return;
        }
        
        PasswordType type = securityManager.validatePassword(password);
        
        switch (type) {
            case MASTER:
                failedAttempts.set(0);
                metadataManager.initialize(securityManager.getEncryptionKey());
                logEvent("Successful master login");
                showMainVault();
                break;
                
            case PANIC:
                // Silent panic mode - no UI indication
                executePanicWipe();
                break;
                
            case DECOY:
                failedAttempts.set(0);
                logEvent("Decoy mode accessed");
                showDecoyVault();
                break;
                
            case INVALID:
                int attempts = failedAttempts.incrementAndGet();
                logEvent("Failed login attempt #" + attempts);
                
                if (attempts >= MAX_LOGIN_ATTEMPTS) {
                    // Automatic decoy mode after max attempts
                    showDecoyVault();
                    statusLabel.setText("");
                } else {
                    statusLabel.setText("Invalid password. " + (MAX_LOGIN_ATTEMPTS - attempts) + " attempts remaining.");
                    passwordField.clear();
                }
                break;
        }
        
        // Clear password from memory
        Arrays.fill(password.toCharArray(), '\0');
    }
    
    private void executePanicWipe() {
        try {
            logEvent("PANIC MODE ACTIVATED - Initiating secure wipe");
            
            // Secure delete all files
            secureDeleteDirectory(new File(VAULT_DIR + "/files"));
            secureDeleteDirectory(new File(VAULT_DIR + "/decoys"));
            
            // Secure delete configuration and metadata
            secureDeleteFile(new File(CONFIG_FILE));
            secureDeleteFile(new File(metadataManager.getMetadataFile()));
            secureDeleteFile(new File(SALT_FILE));
            secureDeleteFile(new File(LOG_FILE));
            
            // Show normal exit without indication
            Platform.exit();
            System.exit(0);
            
        } catch (Exception e) {
            // Silent failure - still exit
            Platform.exit();
            System.exit(0);
        }
    }
    
    private void showMainVault() {
        BorderPane mainPane = new BorderPane();
        
        // Create session timer
        startSessionTimer();
        
        // Top toolbar
        ToolBar toolbar = new ToolBar();
        
        Button uploadButton = new Button("📁 Upload");
        Button downloadButton = new Button("💾 Download");
        Button deleteButton = new Button("🗑️ Delete");
        Button backupButton = new Button("📦 Backup");
        Button restoreButton = new Button("📥 Restore");
        Button settingsButton = new Button("⚙️ Settings");
        Button logoutButton = new Button("🚪 Logout");
        
        // Style buttons
        styleButton(uploadButton, "#4CAF50");
        styleButton(downloadButton, "#2196F3");
        styleButton(deleteButton, "#ff9800");
        styleButton(backupButton, "#607D8B");
        styleButton(restoreButton, "#607D8B");
        styleButton(settingsButton, "#9C27B0");
        styleButton(logoutButton, "#f44336");
        
        // Add tooltips
        uploadButton.setTooltip(new Tooltip("Encrypt and store a new file"));
        downloadButton.setTooltip(new Tooltip("Decrypt and download selected file"));
        deleteButton.setTooltip(new Tooltip("Securely delete selected file"));
        backupButton.setTooltip(new Tooltip("Create encrypted vault backup"));
        restoreButton.setTooltip(new Tooltip("Restore vault from backup"));
        settingsButton.setTooltip(new Tooltip("Vault settings and preferences"));
        logoutButton.setTooltip(new Tooltip("Logout and lock vault"));
        
        toolbar.getItems().addAll(uploadButton, downloadButton, deleteButton, 
                                  new Separator(), backupButton, restoreButton,
                                  new Separator(), settingsButton, logoutButton);
        
        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search files...");
        searchField.setMaxWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshFileList(newVal);
            updateActivity();
        });
        
        // File list with enhanced display
        fileListView = new ListView<>();
        fileListView.setCellFactory(listView -> new FileListCell());
        refreshFileList("");
        
        // Status and log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120);
        logArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        logArea.setText("Vault ready. All files encrypted with AES-256.\n");
        
        // Layout
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(15));
        
        Label searchLabel = new Label("Search Files:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        Label filesLabel = new Label("Encrypted Files:");
        filesLabel.setStyle("-fx-font-weight: bold;");
        
        Label logLabel = new Label("Activity Log:");
        logLabel.setStyle("-fx-font-weight: bold;");
        
        centerBox.getChildren().addAll(searchLabel, searchField, filesLabel, fileListView, logLabel, logArea);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        
        mainPane.setTop(toolbar);
        mainPane.setCenter(centerBox);
        
        // Event handlers
        uploadButton.setOnAction(e -> { uploadFile(); updateActivity(); });
        downloadButton.setOnAction(e -> { downloadFile(); updateActivity(); });
        deleteButton.setOnAction(e -> { secureDeleteFile(); updateActivity(); });
        backupButton.setOnAction(e -> { backupVault(); updateActivity(); });
        restoreButton.setOnAction(e -> { restoreVault(); updateActivity(); });
        settingsButton.setOnAction(e -> { showSettings(); updateActivity(); });
        logoutButton.setOnAction(e -> logout());
        
        // Mouse and keyboard activity tracking
        Scene scene = new Scene(mainPane, 900, 700);
        scene.setOnMouseMoved(e -> updateActivity());
        scene.setOnKeyPressed(e -> updateActivity());
        
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle(APP_NAME + " - Secure Vault");
    }
    
    private void showDecoyVault() {
        BorderPane decoyPane = new BorderPane();
        
        // Create session timer for decoy mode too
        startSessionTimer();
        
        ToolBar toolbar = new ToolBar();
        
        Button uploadButton = new Button("📁 Upload");
        Button downloadButton = new Button("💾 Download");
        Button logoutButton = new Button("🚪 Logout");
        
        styleButton(uploadButton, "#4CAF50");
        styleButton(downloadButton, "#2196F3");
        styleButton(logoutButton, "#f44336");
        
        toolbar.getItems().addAll(uploadButton, downloadButton, new Separator(), logoutButton);
        
        ListView<String> decoyList = new ListView<>();
        decoyList.getItems().addAll(DECOY_NAMES);
        decoyList.setCellFactory(listView -> new DecoyFileListCell());
        
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(15));
        
        Label filesLabel = new Label("Files:");
        filesLabel.setStyle("-fx-font-weight: bold;");
        
        centerBox.getChildren().addAll(filesLabel, decoyList);
        VBox.setVgrow(decoyList, Priority.ALWAYS);
        
        decoyPane.setTop(toolbar);
        decoyPane.setCenter(centerBox);
        
        // Decoy interactions
        uploadButton.setOnAction(e -> {
            showAlert("File uploaded successfully", Alert.AlertType.INFORMATION);
            updateActivity();
        });
        
        downloadButton.setOnAction(e -> {
            String selected = decoyList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a file first", Alert.AlertType.WARNING);
            } else {
                showAlert("File downloaded: " + selected, Alert.AlertType.INFORMATION);
            }
            updateActivity();
        });
        
        logoutButton.setOnAction(e -> logout());
        
        Scene scene = new Scene(decoyPane, 900, 700);
        scene.setOnMouseMoved(e -> updateActivity());
        scene.setOnKeyPressed(e -> updateActivity());
        
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle(APP_NAME + " - Files");
    }  
  
    // ==================== FILE OPERATIONS ====================
    
    private void uploadFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Encrypt");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file == null) return;
            
            String fileName = file.getName();
            if (!isValidFileName(fileName)) {
                showAlert("Invalid file name. Use only letters, numbers, spaces, dots, and dashes (max 100 chars).", 
                         Alert.AlertType.ERROR);
                return;
            }
            
            // Check for duplicates
            if (metadataManager.fileExists(fileName)) {
                showAlert("A file with this name already exists in the vault.", Alert.AlertType.ERROR);
                return;
            }
            
            // Show progress
            ProgressDialog progressDialog = new ProgressDialog("Encrypting file...");
            progressDialog.show();
            
            // Encrypt file in background thread
            new Thread(() -> {
                try {
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    byte[] encrypted = securityManager.encrypt(fileData);
                    String encryptedFileName = UUID.randomUUID().toString() + ".enc";
                    Path encryptedPath = Paths.get(VAULT_DIR, "files", encryptedFileName);
                    Files.write(encryptedPath, encrypted);
                    
                    // Calculate hash and store metadata
                    String fileHash = securityManager.calculateSHA256(fileData);
                    long fileSize = file.length();
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    
                    MetadataManager.FileMetadata metadata = new MetadataManager.FileMetadata(
                        fileName, encryptedFileName, fileHash, fileSize, timestamp);
                    metadataManager.addFileMetadata(metadata);
                    
                    Platform.runLater(() -> {
                        progressDialog.close();
                        refreshFileList("");
                        logArea.appendText("✓ File encrypted: " + fileName + " (" + formatFileSize(fileSize) + ")\n");
                        logEvent("File uploaded: " + fileName);
                        showAlert("File encrypted and stored successfully", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progressDialog.close();
                        showAlert("Encryption failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                        logEvent("Upload failed: " + ex.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception ex) {
            showAlert("Upload error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Upload error: " + ex.getMessage());
        }
    }
    
    private void downloadFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a file to download", Alert.AlertType.WARNING);
                return;
            }
            
            String fileName = extractFileName(selected);
            MetadataManager.FileMetadata metadata = metadataManager.getFileMetadata(fileName);
            if (metadata == null) {
                showAlert("File metadata not found", Alert.AlertType.ERROR);
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Decrypted File");
            fileChooser.setInitialFileName(fileName);
            File saveFile = fileChooser.showSaveDialog(primaryStage);
            if (saveFile == null) return;
            
            // Show progress
            ProgressDialog progressDialog = new ProgressDialog("Decrypting file...");
            progressDialog.show();
            
            // Decrypt file in background thread
            new Thread(() -> {
                try {
                    Path encryptedPath = Paths.get(VAULT_DIR, "files", metadata.encryptedName);
                    byte[] encryptedBytes = Files.readAllBytes(encryptedPath);
                    byte[] decryptedBytes = securityManager.decrypt(encryptedBytes);
                    
                    // Verify integrity
                    String actualHash = securityManager.calculateSHA256(decryptedBytes);
                    if (!actualHash.equals(metadata.hash)) {
                        Platform.runLater(() -> {
                            progressDialog.close();
                            showAlert("File integrity check failed! File may be corrupted.", Alert.AlertType.ERROR);
                            logEvent("Integrity check failed: " + fileName);
                        });
                        return;
                    }
                    
                    Files.write(saveFile.toPath(), decryptedBytes);
                    
                    Platform.runLater(() -> {
                        progressDialog.close();
                        logArea.appendText("✓ File decrypted: " + fileName + "\n");
                        logEvent("File downloaded: " + fileName);
                        showAlert("File decrypted successfully", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progressDialog.close();
                        showAlert("Decryption failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                        logEvent("Download failed: " + ex.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception ex) {
            showAlert("Download error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Download error: " + ex.getMessage());
        }
    }
    
    private void secureDeleteFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a file to delete", Alert.AlertType.WARNING);
                return;
            }
            
            String fileName = extractFileName(selected);
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Secure Delete");
            confirm.setHeaderText("Permanently delete this file?");
            confirm.setContentText("File: " + fileName + "\n\nThis action cannot be undone. The file will be securely overwritten.");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                // Show progress
                ProgressDialog progressDialog = new ProgressDialog("Securely deleting file...");
                progressDialog.show();
                
                new Thread(() -> {
                    try {
                        MetadataManager.FileMetadata metadata = metadataManager.getFileMetadata(fileName);
                        if (metadata != null) {
                            // Secure delete the encrypted file
                            File encryptedFile = new File(VAULT_DIR + "/files/" + metadata.encryptedName);
                            secureDeleteFile(encryptedFile);
                            
                            // Remove from metadata
                            metadataManager.removeFileMetadata(fileName);
                        }
                        
                        Platform.runLater(() -> {
                            progressDialog.close();
                            refreshFileList("");
                            logArea.appendText("✓ File securely deleted: " + fileName + "\n");
                            logEvent("File securely deleted: " + fileName);
                            showAlert("File securely deleted", Alert.AlertType.INFORMATION);
                        });
                        
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            progressDialog.close();
                            showAlert("Secure delete failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                            logEvent("Secure delete failed: " + ex.getMessage());
                        });
                    }
                }).start();
            }
        } catch (Exception ex) {
            showAlert("Delete error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Delete error: " + ex.getMessage());
        }
    }
    
    private void backupVault() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Encrypted Backup");
            fileChooser.setInitialFileName("ghostvault-backup-" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")) + ".gvb");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb"));
            
            File backupFile = fileChooser.showSaveDialog(primaryStage);
            if (backupFile == null) return;
            
            ProgressDialog progressDialog = new ProgressDialog("Creating encrypted backup...");
            progressDialog.show();
            
            new Thread(() -> {
                try {
                    // Create backup data
                    BackupData backup = createBackupData();
                    byte[] backupBytes = serializeBackup(backup);
                    byte[] encryptedBackup = securityManager.encrypt(backupBytes);
                    
                    Files.write(backupFile.toPath(), encryptedBackup);
                    
                    Platform.runLater(() -> {
                        progressDialog.close();
                        logArea.appendText("✓ Backup created: " + backupFile.getName() + "\n");
                        logEvent("Vault backup created");
                        showAlert("Backup created successfully", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progressDialog.close();
                        showAlert("Backup failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                        logEvent("Backup failed: " + ex.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception ex) {
            showAlert("Backup error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void restoreVault() {
        try {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Restore Vault");
            warning.setHeaderText("This will replace all current vault data");
            warning.setContentText("Are you sure you want to restore from backup? This cannot be undone.");
            
            Optional<ButtonType> result = warning.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) return;
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Backup File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GhostVault Backup", "*.gvb"));
            
            File backupFile = fileChooser.showOpenDialog(primaryStage);
            if (backupFile == null) return;
            
            ProgressDialog progressDialog = new ProgressDialog("Restoring from backup...");
            progressDialog.show();
            
            new Thread(() -> {
                try {
                    byte[] encryptedBackup = Files.readAllBytes(backupFile.toPath());
                    byte[] backupBytes = securityManager.decrypt(encryptedBackup);
                    BackupData backup = deserializeBackup(backupBytes);
                    
                    restoreFromBackup(backup);
                    
                    Platform.runLater(() -> {
                        progressDialog.close();
                        refreshFileList("");
                        logArea.appendText("✓ Vault restored from backup\n");
                        logEvent("Vault restored from backup");
                        showAlert("Vault restored successfully", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progressDialog.close();
                        showAlert("Restore failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                        logEvent("Restore failed: " + ex.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception ex) {
            showAlert("Restore error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showSettings() {
        Dialog<Void> settingsDialog = new Dialog<>();
        settingsDialog.setTitle("Vault Settings");
        settingsDialog.setHeaderText("Configure vault preferences");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Theme selection
        Label themeLabel = new Label("Theme:");
        themeLabel.setStyle("-fx-font-weight: bold;");
        
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton darkTheme = new RadioButton("Dark Theme");
        RadioButton lightTheme = new RadioButton("Light Theme");
        darkTheme.setToggleGroup(themeGroup);
        lightTheme.setToggleGroup(themeGroup);
        
        if (isDarkTheme) {
            darkTheme.setSelected(true);
        } else {
            lightTheme.setSelected(true);
        }
        
        // Session timeout
        Label timeoutLabel = new Label("Session Timeout (minutes):");
        timeoutLabel.setStyle("-fx-font-weight: bold;");
        
        Spinner<Integer> timeoutSpinner = new Spinner<>(5, 60, SESSION_TIMEOUT_MINUTES);
        timeoutSpinner.setEditable(true);
        
        // Auto-lock on inactivity
        CheckBox autoLockCheck = new CheckBox("Auto-lock on inactivity");
        autoLockCheck.setSelected(true);
        
        content.getChildren().addAll(
            themeLabel, darkTheme, lightTheme,
            new Separator(),
            timeoutLabel, timeoutSpinner,
            autoLockCheck
        );
        
        settingsDialog.getDialogPane().setContent(content);
        settingsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<Void> result = settingsDialog.showAndWait();
        if (result.isPresent()) {
            // Apply theme change
            boolean newDarkTheme = darkTheme.isSelected();
            if (newDarkTheme != isDarkTheme) {
                isDarkTheme = newDarkTheme;
                applyTheme(primaryStage.getScene());
            }
            
            logEvent("Settings updated");
        }
    }
    
    private void logout() {
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
        
        // Clear sensitive data
        securityManager.clearSensitiveData();
        failedAttempts.set(0);
        logEvent("User logged out");
        
        showLoginScreen();
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void startSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
        
        sessionTimer = new Timer(true);
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            private boolean warningShown = false;
            
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long inactiveTime = now - lastActivity;
                long timeoutMs = SESSION_TIMEOUT_MINUTES * 60 * 1000;
                long warningMs = 30 * 1000; // 30 seconds warning
                
                if (!warningShown && inactiveTime > (timeoutMs - warningMs)) {
                    warningShown = true;
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Session Timeout");
                        alert.setHeaderText("Session will expire soon");
                        alert.setContentText("You will be logged out in 30 seconds due to inactivity.");
                        alert.show();
                    });
                }
                
                if (inactiveTime > timeoutMs) {
                    Platform.runLater(() -> {
                        showAlert("Session expired due to inactivity", Alert.AlertType.INFORMATION);
                        logout();
                    });
                    cancel();
                }
            }
        }, 10000, 10000); // Check every 10 seconds
    }
    
    private void updateActivity() {
        lastActivity = System.currentTimeMillis();
    }
    
    private void updatePasswordStrength(String password, ProgressBar bar, Label label) {
        int score = getPasswordStrengthScore(password);
        double progress = score / 5.0;
        bar.setProgress(progress);
        
        String[] levels = {"Very Weak", "Weak", "Fair", "Strong", "Very Strong"};
        String[] colors = {"#f44336", "#ff9800", "#ffeb3b", "#8bc34a", "#4caf50"};
        
        if (score > 0) {
            String level = levels[score - 1];
            String color = colors[score - 1];
            label.setText("Strength: " + level);
            label.setStyle("-fx-text-fill: " + color + ";");
            bar.setStyle("-fx-accent: " + color + ";");
        } else {
            label.setText("");
            bar.setStyle("");
        }
    }
    
    private int getPasswordStrengthScore(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        return score;
    }
    
    private boolean isValidFileName(String fileName) {
        return fileName != null && 
               fileName.length() <= 100 && 
               fileName.matches("^[\\w\\-. ]+$") &&
               !fileName.startsWith(".") &&
               !fileName.endsWith(".");
    }
    
    private String extractFileName(String displayText) {
        // Extract filename from display format "filename (size) - date"
        int parenIndex = displayText.indexOf(" (");
        return parenIndex > 0 ? displayText.substring(0, parenIndex) : displayText;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    private void styleButton(Button button, String color) {
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 12px; " +
            "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px;", 
            color));
    }
    
    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        try {
            String cssFile = isDarkTheme ? "/ghostvault-dark.css" : "/ghostvault-light.css";
            scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        } catch (Exception e) {
            // CSS not found, continue without styling
        }
    }
    
    private void refreshFileList(String searchTerm) {
        if (fileListView == null) return;
        
        fileListView.getItems().clear();
        
        try {
            List<MetadataManager.FileMetadata> metadata = metadataManager.loadAllMetadata();
            for (MetadataManager.FileMetadata m : metadata) {
                if (searchTerm == null || searchTerm.isEmpty() || 
                    m.originalName.toLowerCase().contains(searchTerm.toLowerCase())) {
                    
                    String displayText = String.format("%s (%s) - %s", 
                        m.originalName, 
                        formatFileSize(m.size),
                        m.timestamp.substring(0, 16).replace("T", " "));
                    
                    fileListView.getItems().add(displayText);
                }
            }
        } catch (Exception e) {
            // Handle error silently
        }
    }
    
    // ==================== SECURE DELETION METHODS ====================
    
    private void secureDeleteFile(File file) {
        if (!file.exists()) return;
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long length = raf.length();
            SecureRandom random = new SecureRandom();
            
            // Multiple overwrite passes
            for (int pass = 0; pass < 3; pass++) {
                raf.seek(0);
                byte[] buffer = new byte[4096];
                long written = 0;
                
                while (written < length) {
                    random.nextBytes(buffer);
                    int toWrite = (int) Math.min(buffer.length, length - written);
                    raf.write(buffer, 0, toWrite);
                    written += toWrite;
                }
                raf.getFD().sync(); // Force write to disk
            }
        } catch (Exception e) {
            // Continue with deletion even if secure overwrite fails
        }
        
        file.delete();
    }
    
    private void secureDeleteDirectory(File dir) {
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    secureDeleteDirectory(file);
                } else {
                    secureDeleteFile(file);
                }
            }
        }
        dir.delete();
    }
    
    // ==================== BACKUP/RESTORE METHODS ====================
    
    private BackupData createBackupData() {
        try {
            BackupData backup = new BackupData();
            backup.metadata = metadataManager.loadAllMetadata();
            backup.files = new HashMap<>();
            
            // Read all encrypted files
            File filesDir = new File(VAULT_DIR + "/files");
            if (filesDir.exists()) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            backup.files.put(file.getName(), Files.readAllBytes(file.toPath()));
                        }
                    }
                }
            }
            
            return backup;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create backup data", e);
        }
    }
    
    private byte[] serializeBackup(BackupData backup) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("GHOSTVAULT_BACKUP_V1\n");
            
            // Serialize metadata
            sb.append("METADATA_START\n");
            sb.append(metadataManager.serializeMetadataJson(backup.metadata));
            sb.append("\nMETADATA_END\n");
            
            // Serialize files
            sb.append("FILES_START\n");
            for (Map.Entry<String, byte[]> entry : backup.files.entrySet()) {
                sb.append("FILE:").append(entry.getKey()).append(":").append(entry.getValue().length).append("\n");
                sb.append(Base64.getEncoder().encodeToString(entry.getValue())).append("\n");
            }
            sb.append("FILES_END\n");
            
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize backup", e);
        }
    }
    
    private BackupData deserializeBackup(byte[] data) {
        try {
            String content = new String(data, StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            
            BackupData backup = new BackupData();
            backup.files = new HashMap<>();
            
            boolean inMetadata = false;
            boolean inFiles = false;
            StringBuilder metadataBuilder = new StringBuilder();
            String currentFileName = null;
            
            for (String line : lines) {
                if (line.equals("METADATA_START")) {
                    inMetadata = true;
                } else if (line.equals("METADATA_END")) {
                    inMetadata = false;
                    backup.metadata = metadataManager.parseMetadataJson(metadataBuilder.toString());
                } else if (line.equals("FILES_START")) {
                    inFiles = true;
                } else if (line.equals("FILES_END")) {
                    inFiles = false;
                } else if (inMetadata) {
                    metadataBuilder.append(line);
                } else if (inFiles && line.startsWith("FILE:")) {
                    String[] parts = line.split(":");
                    currentFileName = parts[1];
                } else if (inFiles && currentFileName != null) {
                    byte[] fileData = Base64.getDecoder().decode(line);
                    backup.files.put(currentFileName, fileData);
                    currentFileName = null;
                }
            }
            
            return backup;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize backup", e);
        }
    }
    
    private void restoreFromBackup(BackupData backup) {
        try {
            // Clear existing files
            secureDeleteDirectory(new File(VAULT_DIR + "/files"));
            new File(VAULT_DIR + "/files").mkdirs();
            
            // Restore files
            for (Map.Entry<String, byte[]> entry : backup.files.entrySet()) {
                Path filePath = Paths.get(VAULT_DIR, "files", entry.getKey());
                Files.write(filePath, entry.getValue());
            }
            
            // Restore metadata
            metadataManager.saveAllMetadata(backup.metadata);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore from backup", e);
        }
    }
    
    // ==================== DECOY FILE GENERATION ====================
    
    private void generateDecoyFiles() {
        try {
            File decoysDir = new File(VAULT_DIR + "/decoys");
            for (String decoyName : DECOY_NAMES) {
                File decoyFile = new File(decoysDir, decoyName);
                if (!decoyFile.exists()) {
                    String content = generateDecoyContent(decoyName);
                    Files.write(decoyFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            // Ignore errors in decoy generation
        }
    }
    
    private String generateDecoyContent(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "txt":
                return "Meeting Notes\n\nDate: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                       "\n\nAttendees: John Smith, Sarah Johnson, Mike Wilson\n\n" +
                       "Agenda:\n1. Project status update\n2. Budget review for Q4\n3. Team assignments\n4. Next meeting schedule\n\n" +
                       "Action Items:\n- Complete budget analysis by Friday\n- Schedule client presentation\n- Update project timeline\n- Prepare quarterly report";
                       
            case "csv":
                return "Name,Email,Phone,Department\n" +
                       "John Smith,john.smith@company.com,555-0101,Engineering\n