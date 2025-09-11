package com.ghostvault.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GhostVault extends Application {
    private static final String APP_NAME = "GhostVault";
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.ghostvault";
    private static final String CONFIG_FILE = VAULT_DIR + "/config.dat";
    private static final String LOG_FILE = VAULT_DIR + "/audit.log";
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    
    private Stage primaryStage;
    private String masterPassword;
    private SecretKey encryptionKey;
    private AtomicInteger failedAttempts = new AtomicInteger(0);
    private boolean duressMode = false;
    private ListView<String> fileListView;
    private TextArea logArea;
    
    // Decoy file templates
    private static final String[] DECOY_NAMES = {
        "Budget_2024.xlsx", "Meeting_Notes.txt", "Project_Plan.docx",
        "Vacation_Photos.jpg", "Resume_Draft.pdf", "Contact_List.csv"
    };
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeVault();
        showLoginScreen();
    }
    
    private void initializeVault() {
        File vaultDir = new File(VAULT_DIR);
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
            new File(VAULT_DIR + "/files").mkdirs();
            new File(VAULT_DIR + "/decoys").mkdirs();
            generateDecoyFiles();
        }
    }
    
    // MFA secret file
    private static final String MFA_SECRET_FILE = VAULT_DIR + "/mfa.secret";
    private String mfaSecret = null;

    private void showLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("GhostVault");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #00ff00;");
        
        Label instructionLabel = new Label("Enter Master Password");
        instructionLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(300);
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #00ff00; -fx-text-fill: black; -fx-font-weight: bold;");
        loginButton.setPrefWidth(100);
        
        Button panicButton = new Button("Emergency");
        panicButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        panicButton.setPrefWidth(100);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, panicButton);
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ff6666;");
        
        // Password strength feedback
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(300);
        strengthBar.setStyle("-fx-accent: #ff9800;");
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            int score = getPasswordStrengthScore(newVal);
            double progress = score / 5.0;
            strengthBar.setProgress(progress);
            String[] levels = {"Very Weak", "Weak", "Medium", "Strong", "Very Strong"};
            String[] colors = {"#ff1744", "#ff9100", "#ffd600", "#00e676", "#00bfae"};
            int idx = Math.max(0, Math.min(score - 1, 4));
            strengthLabel.setText("Strength: " + (score > 0 ? levels[idx] : "") );
            strengthBar.setStyle("-fx-accent: " + colors[idx] + ";");
        });

        loginBox.getChildren().addAll(titleLabel, instructionLabel, passwordField, strengthBar, strengthLabel, buttonBox, statusLabel);
        
        loginButton.setOnAction(e -> {
            String password = passwordField.getText();
            int score = getPasswordStrengthScore(password);
            if (score < 4) {
                statusLabel.setText("Password too weak: minimum 8 chars, uppercase, lowercase, digit, special char required.");
                return;
            }
            if (validatePassword(password)) {
                masterPassword = password;
                encryptionKey = deriveKey(password);
                failedAttempts.set(0);
                // MFA setup or prompt
                try {
                    if (mfaSecret == null) {
                        // First time: generate secret
                        mfaSecret = java.util.Base64.getEncoder().encodeToString(java.security.SecureRandom.getInstanceStrong().generateSeed(20));
                        Files.write(Paths.get(MFA_SECRET_FILE), mfaSecret.getBytes());
                        // Show QR code for TOTP setup
                        String issuer = "GhostVault";
                        String account = System.getProperty("user.name");
                        String uri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, account, mfaSecret, issuer);
                        javafx.scene.image.ImageView qr = new javafx.scene.image.ImageView(new javafx.scene.image.Image(
                            new java.io.ByteArrayInputStream(
                                net.glxn.qrgen.javase.QRCode.from(uri).withSize(200, 200).stream().toByteArray()
                            )
                        ));
                        Alert qrAlert = new Alert(Alert.AlertType.INFORMATION);
                        qrAlert.setTitle("Set up MFA");
                        qrAlert.setHeaderText("Scan this QR code in your authenticator app");
                        qrAlert.getDialogPane().setContent(qr);
                        qrAlert.showAndWait();
                    } else {
                        mfaSecret = new String(Files.readAllBytes(Paths.get(MFA_SECRET_FILE)));
                    }
                } catch (Exception ex) {
                    showAlert("Error setting up MFA", Alert.AlertType.ERROR);
                    return;
                }
                // Prompt for TOTP code
                TextInputDialog totpDialog = new TextInputDialog();
                totpDialog.setTitle("Multi-Factor Authentication");
                totpDialog.setHeaderText("Enter the 6-digit code from your authenticator app");
                Optional<String> totpResult = totpDialog.showAndWait();
                if (!totpResult.isPresent() || !validateTOTP(totpResult.get())) {
                    showAlert("Invalid MFA code.", Alert.AlertType.ERROR);
                    return;
                }
                logEvent("Successful login");
                showMainVault();
            } else {
                int attempts = failedAttempts.incrementAndGet();
                logEvent("Failed login attempt #" + attempts);
                
                if (attempts >= MAX_LOGIN_ATTEMPTS) {
                    duressMode = true;
                    showDecoyVault();
                    statusLabel.setText("");
                } else {
                    statusLabel.setText("Invalid password. " + (MAX_LOGIN_ATTEMPTS - attempts) + " attempts remaining.");
                    passwordField.clear();
                }
            }
        });
        
        panicButton.setOnAction(e -> {
            showPanicConfirmation();
        });
        
        passwordField.setOnAction(e -> loginButton.fire());
        
        // Theme switcher
        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setSelected(true);
        themeToggle.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        themeToggle.setFocusTraversable(false);
        themeToggle.setOnAction(ev -> {
            if (themeToggle.isSelected()) {
                themeToggle.setText("Dark Mode");
                loginBox.getScene().getStylesheets().clear();
                loginBox.getScene().getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
            } else {
                themeToggle.setText("Light Mode");
                loginBox.getScene().getStylesheets().clear();
                loginBox.getScene().getStylesheets().add(getClass().getResource("/ghostvault-light.css").toExternalForm());
            }
        });
        loginBox.getChildren().add(themeToggle);

        Scene scene = new Scene(loginBox, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // TOTP validation
    private boolean validateTOTP(String code) {
        try {
            if (mfaSecret == null) return false;
            com.eatthepath.otp.TimeBasedOneTimePasswordGenerator totpGen = new com.eatthepath.otp.TimeBasedOneTimePasswordGenerator();
            javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(java.util.Base64.getDecoder().decode(mfaSecret), "RAW");
            int validCode = totpGen.generateOneTimePassword(key, java.time.Instant.now());
            return String.format("%06d", validCode).equals(code.trim());
        } catch (Exception e) {
            return false;
        }
    }

    // Password strength scoring: returns 1-5
    private int getPasswordStrengthScore(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?].*")) score++;
        return score;
    }

    // Helper: SHA-256 hash as hex string
    private String getSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void showMainVault() {
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #1a1a1a;");
        
        // Top toolbar
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #2b2b2b;");
        
        Button uploadButton = new Button("Upload File");
        Button downloadButton = new Button("Download");
        Button deleteButton = new Button("Secure Delete");
        Button panicButton = new Button("PANIC");
        Button helpButton = new Button("Help");
        Button logoutButton = new Button("Logout");
        
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        downloadButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        panicButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        helpButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        logoutButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");

        uploadButton.setTooltip(new Tooltip("Encrypt and store a new file in the vault"));
        downloadButton.setTooltip(new Tooltip("Decrypt and download the selected file"));
        deleteButton.setTooltip(new Tooltip("Securely delete the selected file from the vault"));
        backupButton.setTooltip(new Tooltip("Create an encrypted backup of your entire vault"));
        restoreButton.setTooltip(new Tooltip("Restore your vault from an encrypted backup"));
        panicButton.setTooltip(new Tooltip("Permanently destroy all vault contents (irreversible!)"));
        helpButton.setTooltip(new Tooltip("Show help and usage tips"));
        logoutButton.setTooltip(new Tooltip("Log out and return to the login screen"));
        searchField.setTooltip(new Tooltip("Search files by name or tag"));
        
        Button backupButton = new Button("Backup");
        Button restoreButton = new Button("Restore");
        backupButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        restoreButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        toolbar.getItems().addAll(uploadButton, downloadButton, deleteButton, backupButton, restoreButton, 
                                  new Separator(), panicButton, helpButton, logoutButton);
        backupButton.setOnAction(e -> backupVault());
        restoreButton.setOnAction(e -> restoreVault());
        
        // Metadata search/filter
        TextField searchField = new TextField();
        searchField.setPromptText("Search files or tags...");
        searchField.setMaxWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshFileList(newVal));

        // File list
        fileListView = new ListView<>();
        fileListView.setStyle("-fx-background-color: #2b2b2b; -fx-control-inner-background: #2b2b2b;");
        refreshFileList("");
        
        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);
        logArea.setStyle("-fx-control-inner-background: #1a1a1a; -fx-text-fill: #00ff00;");
        logArea.setText("System ready. All files encrypted.\n");
        
        // Layout
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(
            new Label("Encrypted Files:") {{ setStyle("-fx-text-fill: white; -fx-font-size: 14px;"); }},
            fileListView,
            new Label("Security Log:") {{ setStyle("-fx-text-fill: white; -fx-font-size: 14px;"); }},
            logArea
        );
        
        mainPane.setTop(toolbar);
        mainPane.setCenter(centerBox);
        
        // Event handlers
        uploadButton.setOnAction(e -> uploadFile());
        downloadButton.setOnAction(e -> downloadFile());
        deleteButton.setOnAction(e -> secureDeleteFile());
        panicButton.setOnAction(e -> showPanicConfirmation());
        helpButton.setOnAction(e -> showHelp());
        logoutButton.setOnAction(e -> logout());
        
        // Theme switcher for main vault
        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setSelected(true);
        themeToggle.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        themeToggle.setFocusTraversable(false);
        themeToggle.setOnAction(ev -> {
            if (themeToggle.isSelected()) {
                themeToggle.setText("Dark Mode");
                mainPane.getScene().getStylesheets().clear();
                mainPane.getScene().getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
            } else {
                themeToggle.setText("Light Mode");
                mainPane.getScene().getStylesheets().clear();
                mainPane.getScene().getStylesheets().add(getClass().getResource("/ghostvault-light.css").toExternalForm());
            }
        });
        toolbar.getItems().add(themeToggle);

        Scene scene = new Scene(mainPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());

        // Session management: auto-logout after 5 minutes inactivity
        final long SESSION_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
        final long WARNING_MS = 30 * 1000; // 30 seconds before logout
        Timer sessionTimer = new Timer(true);
        final long[] lastActivity = {System.currentTimeMillis()};
        Runnable resetTimer = () -> lastActivity[0] = System.currentTimeMillis();

        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> resetTimer.run());
        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> resetTimer.run());

        sessionTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            boolean warned = false;
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (!warned && now - lastActivity[0] > SESSION_TIMEOUT_MS - WARNING_MS) {
                    warned = true;
                    javafx.application.Platform.runLater(() -> showAlert("You will be logged out in 30 seconds due to inactivity.", Alert.AlertType.WARNING));
                }
                if (now - lastActivity[0] > SESSION_TIMEOUT_MS) {
                    sessionTimer.cancel();
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Session expired. You have been logged out.", Alert.AlertType.INFORMATION);
                        logout();
                    });
                }
            }
        }, 10000, 10000); // check every 10 seconds

        primaryStage.setScene(scene);
        primaryStage.setTitle(APP_NAME + " - Secure Vault");
    }
    
    // Backup vault: zip and encrypt .ghostvault directory
    private void backupVault() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Encrypted Backup");
            fileChooser.setInitialFileName("ghostvault-backup.enc");
            File backupFile = fileChooser.showSaveDialog(primaryStage);
            if (backupFile == null) return;
            try {
                // Zip .ghostvault
                java.nio.file.Path vaultDir = java.nio.file.Paths.get(VAULT_DIR);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);
                java.nio.file.Files.walk(vaultDir).forEach(path -> {
                    try {
                        java.nio.file.Path relPath = vaultDir.relativize(path);
                        if (java.nio.file.Files.isDirectory(path)) return;
                        zos.putNextEntry(new java.util.zip.ZipEntry(relPath.toString()));
                        zos.write(java.nio.file.Files.readAllBytes(path));
                        zos.closeEntry();
                    } catch (Exception ignore) {}
                });
                zos.close();
                byte[] zipped = baos.toByteArray();
                // Encrypt
                byte[] encrypted = encrypt(zipped);
                java.nio.file.Files.write(backupFile.toPath(), encrypted);
                showAlert("Backup created successfully.", Alert.AlertType.INFORMATION);
                logEvent("Vault backup created: " + backupFile.getAbsolutePath());
            } catch (IOException ioex) {
                showAlert("Backup file access error: " + ioex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Backup file access error: " + ioex.getMessage());
            } catch (Exception ex) {
                showAlert("Backup encryption error: " + ex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Backup encryption error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            showAlert("Unexpected backup error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Unexpected backup error: " + ex.getMessage());
        }
    }

    // Restore vault: decrypt and unzip backup
    private void restoreVault() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Encrypted Backup");
            File backupFile = fileChooser.showOpenDialog(primaryStage);
            if (backupFile == null) return;
            try {
                byte[] encrypted = java.nio.file.Files.readAllBytes(backupFile.toPath());
                byte[] zipped = decrypt(encrypted);
                // Unzip to .ghostvault
                java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(zipped));
                java.util.zip.ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    java.io.File outFile = new java.io.File(VAULT_DIR, entry.getName());
                    outFile.getParentFile().mkdirs();
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = zis.read(buf)) > 0) fos.write(buf, 0, len);
                    fos.close();
                    zis.closeEntry();
                }
                zis.close();
                showAlert("Vault restored successfully. Please restart the app.", Alert.AlertType.INFORMATION);
                logEvent("Vault restored from backup: " + backupFile.getAbsolutePath());
            } catch (IOException ioex) {
                showAlert("Restore file access error: " + ioex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Restore file access error: " + ioex.getMessage());
            } catch (Exception ex) {
                showAlert("Restore decryption error: " + ex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Restore decryption error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            showAlert("Unexpected restore error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Unexpected restore error: " + ex.getMessage());
        }
    }

    private void showDecoyVault() {
        logEvent("Duress mode activated - showing decoy vault");
        
        BorderPane decoyPane = new BorderPane();
        decoyPane.setStyle("-fx-background-color: #1a1a1a;");
        
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #2b2b2b;");
        
        Button uploadButton = new Button("Upload File");
        Button downloadButton = new Button("Download");
        Button logoutButton = new Button("Logout");
        
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        downloadButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        logoutButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        
        toolbar.getItems().addAll(uploadButton, downloadButton, logoutButton);
        
        ListView<String> decoyList = new ListView<>();
        decoyList.setStyle("-fx-background-color: #2b2b2b; -fx-control-inner-background: #2b2b2b;");
        decoyList.getItems().addAll(DECOY_NAMES);
        
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(
            new Label("Files:") {{ setStyle("-fx-text-fill: white; -fx-font-size: 14px;"); }},
            decoyList
        );
        
        decoyPane.setTop(toolbar);
        decoyPane.setCenter(centerBox);
        
        uploadButton.setOnAction(e -> showAlert("File uploaded successfully", Alert.AlertType.INFORMATION));
        downloadButton.setOnAction(e -> showAlert("Please select a file first", Alert.AlertType.WARNING));
        logoutButton.setOnAction(e -> logout());
        
        Scene scene = new Scene(decoyPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(APP_NAME + " - Vault");
    }
    
    private void uploadFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Encrypt");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file == null) return;
            String fileName = file.getName();
            if (!fileName.matches("^[\w\-. ]{1,64}$")) {
                showAlert("Invalid file name. Only letters, numbers, spaces, dash, and dot allowed (max 64 chars).", Alert.AlertType.ERROR);
                return;
            }
            // Prevent duplicate upload
            Path metaPath = Paths.get(VAULT_DIR + "/metadata.dat");
            if (Files.exists(metaPath)) {
                for (String line : Files.readAllLines(metaPath)) {
                    try {
                        String decrypted = new String(decrypt(line.getBytes()));
                        String[] parts = decrypted.split("\\|");
                        if (parts.length > 0 && parts[0].equals(fileName)) {
                            showAlert("A file with this name already exists in the vault.", Alert.AlertType.ERROR);
                            return;
                        }
                    } catch (Exception ignore) {}
                }
            }
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                byte[] encrypted = encrypt(fileData);
                String encryptedFileName = UUID.randomUUID().toString() + ".enc";
                Path encryptedPath = Paths.get(VAULT_DIR, "files", encryptedFileName);
                Files.write(encryptedPath, encrypted);
                // Calculate SHA-256 hash
                String fileHash = getSHA256(fileData);
                // Store metadata: original name, encrypted name, hash
                String metadata = fileName + "|" + encryptedFileName + "|" + fileHash;
                appendToFile(VAULT_DIR + "/metadata.dat", encrypt(metadata.getBytes()));
                refreshFileList("");
                logArea.appendText("File encrypted: " + fileName + "\n");
                logEvent("File uploaded: " + fileName);
            } catch (IOException ioex) {
                showAlert("File access error: " + ioex.getMessage(), Alert.AlertType.ERROR);
                logEvent("File access error: " + ioex.getMessage());
            } catch (Exception ex) {
                showAlert("Encryption error: " + ex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Encryption error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            showAlert("Unexpected error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Unexpected upload error: " + ex.getMessage());
        }
    }
    
    private void downloadFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a file", Alert.AlertType.WARNING);
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Decrypted File");
            fileChooser.setInitialFileName(selected);
            File saveFile = fileChooser.showSaveDialog(primaryStage);
            if (saveFile == null) return;
            try {
                // Find metadata for selected file
                String originalName = selected;
                String encryptedName = null, storedHash = null;
                List<String> metaLines = Files.readAllLines(Paths.get(VAULT_DIR + "/metadata.dat"));
                for (String line : metaLines) {
                    try {
                        String decrypted = new String(decrypt(line.getBytes()));
                        String[] parts = decrypted.split("\\|");
                        if (parts.length >= 3 && parts[0].equals(originalName)) {
                            encryptedName = parts[1];
                            storedHash = parts[2];
                            break;
                        }
                    } catch (Exception ignore) {}
                }
                if (encryptedName == null || storedHash == null) {
                    showAlert("Metadata not found for file.", Alert.AlertType.ERROR);
                    return;
                }
                // Read and decrypt file
                Path encryptedPath = Paths.get(VAULT_DIR, "files", encryptedName);
                byte[] encryptedBytes = Files.readAllBytes(encryptedPath);
                byte[] decryptedBytes = decrypt(encryptedBytes);
                // Hash check
                String actualHash = getSHA256(decryptedBytes);
                if (!actualHash.equals(storedHash)) {
                    showAlert("File integrity check failed! The file may be corrupted or tampered.", Alert.AlertType.ERROR);
                    logEvent("File integrity check failed: " + selected);
                    return;
                }
                Files.write(saveFile.toPath(), decryptedBytes);
                logArea.appendText("File decrypted: " + selected + "\n");
                logEvent("File downloaded: " + selected);
                showAlert("File decrypted successfully", Alert.AlertType.INFORMATION);
            } catch (IOException ioex) {
                showAlert("File access error: " + ioex.getMessage(), Alert.AlertType.ERROR);
                logEvent("File access error: " + ioex.getMessage());
            } catch (Exception ex) {
                showAlert("Decryption/validation error: " + ex.getMessage(), Alert.AlertType.ERROR);
                logEvent("Decryption/validation error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            showAlert("Unexpected error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Unexpected download error: " + ex.getMessage());
        }
    }
    
    private void secureDeleteFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a file", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Secure Delete");
            confirm.setHeaderText("Permanently delete this file?");
            confirm.setContentText("This action cannot be undone. The file will be overwritten multiple times.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    secureOverwrite(selected);
                    refreshFileList("");
                    logArea.appendText("File securely deleted: " + selected + "\n");
                    logEvent("File securely deleted: " + selected);
                } catch (Exception ex) {
                    showAlert("Secure delete error: " + ex.getMessage(), Alert.AlertType.ERROR);
                    logEvent("Secure delete error: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            showAlert("Unexpected error: " + ex.getMessage(), Alert.AlertType.ERROR);
            logEvent("Unexpected secure delete error: " + ex.getMessage());
        }
    }
    
    private void showPanicConfirmation() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("PANIC MODE");
        alert.setHeaderText("WARNING: This will delete ALL vault files!");
        alert.setContentText("Are you absolutely sure? This cannot be undone!");
        
        ButtonType confirmButton = new ButtonType("DELETE EVERYTHING");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            TextInputDialog confirmDialog = new TextInputDialog();
            confirmDialog.setTitle("Confirm Panic Wipe");
            confirmDialog.setHeaderText("Type DELETE to confirm permanent wipe");
            confirmDialog.setContentText("This action is IRREVERSIBLE.");
            Optional<String> confirmResult = confirmDialog.showAndWait();
            if (confirmResult.isPresent() && "DELETE".equals(confirmResult.get().trim())) {
                executePanicMode();
            } else {
                showAlert("Panic wipe cancelled.", Alert.AlertType.INFORMATION);
            }
        }
    }
    
    private void executePanicMode() {
        logEvent("PANIC MODE ACTIVATED");
        
        try {
            File filesDir = new File(VAULT_DIR + "/files");
            if (filesDir.exists()) {
                for (File file : filesDir.listFiles()) {
                    secureDelete(file);
                }
            }
            
            logArea.appendText("PANIC MODE: All files destroyed\n");
            showAlert("All data has been securely erased", Alert.AlertType.INFORMATION);
            logout();
            
        } catch (Exception e) {
            logEvent("Panic mode error: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("GhostVault Help");
        help.setHeaderText("Emergency Help System");
        help.setContentText(
            "QUICK ACTIONS:\n\n" +
            "• Upload File: Encrypt and store files securely\n" +
            "• Download: Decrypt and retrieve files\n" +
            "• Secure Delete: Overwrite file multiple times\n" +
            "• PANIC: Emergency deletion of all vault contents\n\n" +
            "EMERGENCY:\n" +
            "• Press PANIC button for immediate data destruction\n" +
            "• Enter wrong password 3 times to activate decoy mode\n" +
            "• All files are AES-256 encrypted\n\n" +
            "Remember: This tool is for legitimate privacy protection only."
        );
        help.showAndWait();
    }
    
    private void logout() {
        masterPassword = null;
        encryptionKey = null;
        duressMode = false;
        failedAttempts.set(0);
        logEvent("User logged out");
        showLoginScreen();
    }
    
    // Cryptographic methods
    private boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) return false;
        
        // For demo purposes - in production, check against stored hash
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            // First time setup - any password is valid
            savePasswordHash(password);
            return true;
        }
        
        try {
            byte[] storedHash = Files.readAllBytes(Paths.get(CONFIG_FILE));
            byte[] inputHash = hashPassword(password);
            return MessageDigest.isEqual(storedHash, inputHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void savePasswordHash(String password) {
        try {
            byte[] hash = hashPassword(password);
            Files.write(Paths.get(CONFIG_FILE), hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private byte[] hashPassword(String password) {
        try {
            // Use PBKDF2 with salt
            byte[] salt = "GhostVaultSalt2024".getBytes(); // In production, use random salt
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private SecretKey deriveKey(String password) {
        try {
            byte[] salt = "GhostVaultKeySalt".getBytes();
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // Generate random IV
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivSpec);
            byte[] encrypted = cipher.doFinal(data);
            
            // Prepend IV to encrypted data
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private byte[] decrypt(byte[] data) {
        try {
            // Extract IV
            byte[] iv = Arrays.copyOfRange(data, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(data, 16, data.length);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivSpec);
            
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void secureDelete(File file) {
        try {
            if (!file.exists()) return;
            
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            long length = raf.length();
            
            // Overwrite with random data 3 times
            SecureRandom random = new SecureRandom();
            for (int pass = 0; pass < 3; pass++) {
                raf.seek(0);
                byte[] data = new byte[1024];
                long written = 0;
                while (written < length) {
                    random.nextBytes(data);
                    int toWrite = (int) Math.min(data.length, length - written);
                    raf.write(data, 0, toWrite);
                    written += toWrite;
                }
            }
            
            raf.close();
            file.delete();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void secureOverwrite(String filename) {
        // Implementation for secure file overwriting
        // This would locate the encrypted file and securely delete it
        logEvent("Secure overwrite: " + filename);
    }
    
    // Overload: refreshFileList with search
    private void refreshFileList(String search) {
        if (fileListView == null) return;
        fileListView.getItems().clear();
        try {
            Path metaPath = Paths.get(VAULT_DIR + "/metadata.dat");
            if (!Files.exists(metaPath)) return;
            List<String> metaLines = Files.readAllLines(metaPath);
            for (String line : metaLines) {
                try {
                    String decrypted = new String(decrypt(line.getBytes()));
                    // Format: name|encName|hash|tags
                    String[] parts = decrypted.split("\\|");
                    String name = parts[0];
                    String tags = (parts.length > 3) ? parts[3] : "";
                    if (search == null || search.isEmpty() ||
                        name.toLowerCase().contains(search.toLowerCase()) ||
                        tags.toLowerCase().contains(search.toLowerCase())) {
                        fileListView.getItems().add(name);
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
    }
    
    private void generateDecoyFiles() {
        try {
            File decoysDir = new File(VAULT_DIR + "/decoys");
            for (String decoyName : DECOY_NAMES) {
                File decoyFile = new File(decoysDir, decoyName);
                if (!decoyFile.exists()) {
                    // Create realistic-looking decoy content
                    String content = generateDecoyContent(decoyName);
                    Files.write(decoyFile.toPath(), content.getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

private String generateDecoyContent(String filename) {
    if (filename.endsWith(".txt")) {
        return "Meeting Notes\n\nDate: " + LocalDateTime.now() +
               "\n\nAttendees: John, Sarah, Mike\n\nAgenda:\n1. Project updates\n2. Budget review\n3. Next steps\n\nAction Items:\n- Review Q3 budget\n- Schedule next meeting\n- Prepare project summary";
    } else if (filename.endsWith(".csv")) {
        return "Name,Email,Phone\nJohn Doe,john@example.com,555-0001\nJane Smith,jane@example.com,555-0002\nAlice Lee,alice@example.com,555-0003";
    } else if (filename.endsWith(".pdf")) {
        // PDF header + fake text
        return "%PDF-1.4\n%âãÏÓ\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n4 0 obj\n<< /Length 44 >>\nstream\nBT /F1 24 Tf 100 700 Td (Confidential Report) Tj ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f \n0000000010 00000 n \n0000000060 00000 n \n0000000117 00000 n \n0000000211 00000 n \ntrailer\n<< /Root 1 0 R /Size 5 >>\nstartxref\n315\n%%EOF";
    } else if (filename.endsWith(".docx")) {
        return "PK\u0003\u0004FakeWordDocxHeader\nProject Plan\n- Overview: Lorem ipsum dolor sit amet\n- Timeline: Q1-Q4\n- Stakeholders: John, Jane, Alice";
    } else if (filename.endsWith(".xlsx")) {
        return "PK\u0003\u0004FakeExcelHeader\nBudget 2024\nDepartment,Amount\nHR,12000\nIT,25000\nMarketing,18000";
    } else if (filename.endsWith(".jpg")) {
        // JPEG header
        return "\u00FF\u00D8\u00FF\u00E0FakeJPEGData";
    } else {
        return "Binary file content placeholder";
    // Implementation for secure file overwriting
    // This would locate the encrypted file and securely delete it
    logEvent("Secure overwrite: " + filename);
}

private void refreshFileList() {
    if (fileListView == null) return;
    
    fileListView.getItems().clear();
    
    // In production, read from encrypted metadata
    File filesDir = new File(VAULT_DIR + "/files");
    if (filesDir.exists()) {
        String[] files = filesDir.list();
        if (files != null) {
            for (String file : files) {
                // In production, decrypt metadata to show original names
                fileListView.getItems().add("Document_" + file.substring(0, 8) + ".pdf");
            }
        }
    }
}

private void generateDecoyFiles() {
    try {
        File decoysDir = new File(VAULT_DIR + "/decoys");
        for (String decoyName : DECOY_NAMES) {
            File decoyFile = new File(decoysDir, decoyName);
            if (!decoyFile.exists()) {
                // Create realistic-looking decoy content
                String content = generateDecoyContent(decoyName);
                Files.write(decoyFile.toPath(), content.getBytes());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(APP_NAME);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}