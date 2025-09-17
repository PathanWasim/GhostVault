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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Simplified GhostVault application demonstrating core functionality
 * This is a working version that can be compiled and run
 */
public class SimpleGhostVault extends Application {
    
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.ghostvault-simple";
    private static final String PASSWORD_FILE = VAULT_DIR + "/password.txt";
    
    private Stage primaryStage;
    private SecretKey encryptionKey;
    private String masterPassword = "demo123"; // Default password for demo
    
    // UI Components
    private ListView<String> fileListView;
    private TextArea logArea;
    private boolean isDarkTheme = true;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize vault directory
        initializeVault();
        
        // Show login screen
        showLoginScreen();
    }
    
    /**
     * Initialize vault directory
     */
    private void initializeVault() {
        try {
            File vaultDir = new File(VAULT_DIR);
            if (!vaultDir.exists()) {
                vaultDir.mkdirs();
                new File(VAULT_DIR + "/files").mkdirs();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to initialize vault: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Show login screen
     */
    private void showLoginScreen() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        
        Label titleLabel = new Label("🔒 GhostVault");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Secure File Vault - Demo Version");
        subtitleLabel.setStyle("-fx-font-size: 14px;");
        
        Label instructionLabel = new Label("Enter Password (demo: demo123)");
        instructionLabel.setStyle("-fx-font-size: 12px;");
        
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
        
        loginBox.getChildren().addAll(titleLabel, subtitleLabel, instructionLabel, passwordField, loginButton, statusLabel);
        
        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(topBar, loginBox);
        VBox.setVgrow(loginBox, Priority.ALWAYS);
        
        loginButton.setOnAction(e -> handleLogin(passwordField.getText(), statusLabel));
        passwordField.setOnAction(e -> loginButton.fire());
        
        themeToggle.setOnAction(e -> {
            isDarkTheme = themeToggle.isSelected();
            applyTheme(primaryStage.getScene());
        });
        
        Scene scene = new Scene(mainContainer, 500, 400);
        applyTheme(scene);
        primaryStage.setTitle("GhostVault - Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Focus on password field
        Platform.runLater(() -> passwordField.requestFocus());
    }
    
    /**
     * Handle login
     */
    private void handleLogin(String password, Label statusLabel) {
        if (password.isEmpty()) {
            statusLabel.setText("Please enter a password.");
            return;
        }
        
        if (password.equals(masterPassword)) {
            try {
                // Generate encryption key from password
                encryptionKey = generateKeyFromPassword(password);
                statusLabel.setText("");
                showMainVault();
            } catch (Exception e) {
                statusLabel.setText("Login failed: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Invalid password. Try: demo123");
        }
    }
    
    /**
     * Show main vault interface
     */
    private void showMainVault() {
        BorderPane mainPane = new BorderPane();
        
        // Top toolbar
        ToolBar toolbar = new ToolBar();
        
        Button uploadButton = new Button("📁 Upload");
        Button downloadButton = new Button("💾 Download");
        Button deleteButton = new Button("🗑️ Delete");
        Button logoutButton = new Button("🚪 Logout");
        Button helpButton = new Button("❓ Help");
        
        // Style buttons
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        downloadButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        helpButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        toolbar.getItems().addAll(uploadButton, downloadButton, deleteButton, 
                                  new Separator(), helpButton, logoutButton);
        
        // File list
        fileListView = new ListView<>();
        refreshFileList();
        
        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120);
        logArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        logArea.setText("🔒 GhostVault Demo Ready - All files encrypted with AES-256\n");
        
        // Layout
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(15));
        
        Label filesLabel = new Label("Encrypted Files:");
        filesLabel.setStyle("-fx-font-weight: bold;");
        
        Label logLabel = new Label("Activity Log:");
        logLabel.setStyle("-fx-font-weight: bold;");
        
        centerBox.getChildren().addAll(filesLabel, fileListView, logLabel, logArea);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        
        mainPane.setTop(toolbar);
        mainPane.setCenter(centerBox);
        
        // Event handlers
        uploadButton.setOnAction(e -> uploadFile());
        downloadButton.setOnAction(e -> downloadFile());
        deleteButton.setOnAction(e -> deleteFile());
        logoutButton.setOnAction(e -> showLoginScreen());
        helpButton.setOnAction(e -> showHelp());
        
        Scene scene = new Scene(mainPane, 800, 600);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle("GhostVault - Secure Vault (Demo)");
    }
    
    /**
     * Upload and encrypt a file
     */
    private void uploadFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Encrypt");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file == null) return;
            
            String fileName = file.getName();
            
            // Read file data
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // Encrypt file
            byte[] encryptedData = encrypt(fileData);
            
            // Save encrypted file
            String encryptedFileName = fileName + ".enc";
            Path encryptedPath = Paths.get(VAULT_DIR, "files", encryptedFileName);
            Files.write(encryptedPath, encryptedData);
            
            // Update UI
            refreshFileList();
            logArea.appendText("✅ File encrypted: " + fileName + " (" + formatFileSize(file.length()) + ")\n");
            showAlert("Success", "File encrypted and stored successfully!", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            logArea.appendText("❌ Upload failed: " + e.getMessage() + "\n");
            showAlert("Error", "Upload failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Download and decrypt a file
     */
    private void downloadFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Warning", "Please select a file to download", Alert.AlertType.WARNING);
                return;
            }
            
            String originalName = selected.replace(".enc", "");
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Decrypted File");
            fileChooser.setInitialFileName(originalName);
            File saveFile = fileChooser.showSaveDialog(primaryStage);
            if (saveFile == null) return;
            
            // Read encrypted file
            Path encryptedPath = Paths.get(VAULT_DIR, "files", selected);
            byte[] encryptedData = Files.readAllBytes(encryptedPath);
            
            // Decrypt file
            byte[] decryptedData = decrypt(encryptedData);
            
            // Save decrypted file
            Files.write(saveFile.toPath(), decryptedData);
            
            // Update UI
            logArea.appendText("✅ File decrypted: " + originalName + "\n");
            showAlert("Success", "File decrypted successfully!", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            logArea.appendText("❌ Download failed: " + e.getMessage() + "\n");
            showAlert("Error", "Download failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Delete a file
     */
    private void deleteFile() {
        try {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Warning", "Please select a file to delete", Alert.AlertType.WARNING);
                return;
            }
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete this file?");
            confirm.setContentText("File: " + selected.replace(".enc", "") + "\n\nThis action cannot be undone.");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Delete file
                Path filePath = Paths.get(VAULT_DIR, "files", selected);
                Files.deleteIfExists(filePath);
                
                // Update UI
                refreshFileList();
                logArea.appendText("🗑️ File deleted: " + selected.replace(".enc", "") + "\n");
                showAlert("Success", "File deleted successfully!", Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            logArea.appendText("❌ Delete failed: " + e.getMessage() + "\n");
            showAlert("Error", "Delete failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Show help dialog
     */
    private void showHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("GhostVault Help");
        helpAlert.setHeaderText("GhostVault Demo - Help");
        helpAlert.setContentText(
            "Welcome to GhostVault Demo!\n\n" +
            "Features:\n" +
            "• Upload: Encrypt and store files securely\n" +
            "• Download: Decrypt and save files\n" +
            "• Delete: Remove files from vault\n" +
            "• Theme: Toggle dark/light theme on login screen\n\n" +
            "Security:\n" +
            "• All files encrypted with AES-256\n" +
            "• Password: demo123 (for demonstration)\n" +
            "• Files stored in: " + VAULT_DIR + "\n\n" +
            "This is a simplified demo version showcasing\n" +
            "the core encryption functionality of GhostVault."
        );
        helpAlert.getDialogPane().setPrefWidth(400);
        helpAlert.showAndWait();
    }
    
    /**
     * Refresh file list
     */
    private void refreshFileList() {
        try {
            fileListView.getItems().clear();
            File filesDir = new File(VAULT_DIR + "/files");
            if (filesDir.exists()) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            fileListView.getItems().add(file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logArea.appendText("❌ Failed to refresh file list: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Generate encryption key from password
     */
    private SecretKey generateKeyFromPassword(String password) throws Exception {
        // Simple key derivation for demo (not production-ready)
        byte[] key = new byte[32]; // 256 bits
        byte[] passwordBytes = password.getBytes("UTF-8");
        
        for (int i = 0; i < key.length; i++) {
            key[i] = passwordBytes[i % passwordBytes.length];
        }
        
        return new SecretKeySpec(key, "AES");
    }
    
    /**
     * Encrypt data
     */
    private byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        return cipher.doFinal(data);
    }
    
    /**
     * Decrypt data
     */
    private byte[] decrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * Apply theme to scene
     */
    private void applyTheme(Scene scene) {
        if (isDarkTheme) {
            scene.getRoot().setStyle(
                "-fx-background-color: #2b2b2b; " +
                "-fx-text-fill: white;"
            );
        } else {
            scene.getRoot().setStyle(
                "-fx-background-color: #f0f0f0; " +
                "-fx-text-fill: black;"
            );
        }
    }
    
    /**
     * Format file size
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}