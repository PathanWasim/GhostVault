package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Encrypted backup and restore functionality
 */
public class EncryptedBackupManager {
    
    private Stage parentStage;
    private NotificationSystem notificationSystem;
    
    private static final String BACKUP_EXTENSION = ".gvbackup";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    
    public EncryptedBackupManager(Stage parentStage) {
        this.parentStage = parentStage;
        this.notificationSystem = NotificationSystem.getInstance();
    }
    
    /**
     * Create encrypted backup of vault directory
     */
    public void createBackup(File vaultDirectory, String password, Consumer<BackupResult> onComplete) {
        if (vaultDirectory == null || !vaultDirectory.exists() || !vaultDirectory.isDirectory()) {
            onComplete.accept(new BackupResult(false, "Invalid vault directory"));
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            onComplete.accept(new BackupResult(false, "Password is required for backup encryption"));
            return;
        }
        
        // Show save dialog for backup file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Encrypted Backup");
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultName = "GhostVault_Backup_" + timestamp + BACKUP_EXTENSION;
        fileChooser.setInitialFileName(defaultName);
        
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup Files", "*" + BACKUP_EXTENSION)
        );
        
        File backupFile = fileChooser.showSaveDialog(parentStage);
        if (backupFile == null) {
            onComplete.accept(new BackupResult(false, "Backup cancelled"));
            return;
        }
        
        // Create backup task
        Task<BackupResult> backupTask = new Task<BackupResult>() {
            @Override
            protected BackupResult call() throws Exception {
                updateMessage("Scanning vault directory...");
                
                // Get list of all files to backup
                List<File> filesToBackup = getAllFiles(vaultDirectory);
                updateMessage("Found " + filesToBackup.size() + " files to backup");
                
                // Create temporary zip file
                File tempZip = File.createTempFile("vault_backup_", ".zip");
                tempZip.deleteOnExit();
                
                try {
                    // Create zip archive
                    updateMessage("Creating archive...");
                    createZipArchive(vaultDirectory, filesToBackup, tempZip, this);
                    
                    // Encrypt the zip file
                    updateMessage("Encrypting backup...");
                    encryptFile(tempZip, backupFile, password);
                    
                    // Clean up temp file
                    tempZip.delete();
                    
                    updateProgress(1, 1);
                    return new BackupResult(true, "Backup created successfully: " + backupFile.getName());
                    
                } catch (Exception e) {
                    // Clean up on error
                    if (tempZip.exists()) {
                        tempZip.delete();
                    }
                    if (backupFile.exists()) {
                        backupFile.delete();
                    }
                    throw e;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    BackupResult result = getValue();
                    onComplete.accept(result);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    String errorMsg = "Backup failed: " + getException().getMessage();
                    onComplete.accept(new BackupResult(false, errorMsg));
                });
            }
        };
        
        Thread backupThread = new Thread(backupTask);
        backupThread.setDaemon(true);
        backupThread.start();
    }
    
    /**
     * Show backup creation dialog
     */
    public void showBackupDialog(File vaultDirectory) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create Encrypted Backup");
        dialog.setHeaderText("Create an encrypted backup of your vault");
        
        // Create dialog content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Enter a strong password to encrypt your backup:");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Backup password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        
        Label warningLabel = new Label("Keep this password safe! You will need it to restore your backup.");
        warningLabel.setStyle("-fx-text-fill: #ff6b35; -fx-font-weight: bold;");
        
        content.getChildren().addAll(infoLabel, passwordField, confirmPasswordField, warningLabel);
        
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType createButton = new ButtonType("Create Backup", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, cancelButton);
        
        // Enable/disable create button based on password validation
        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createButton);
        createBtn.setDisable(true);
        
        Runnable validatePasswords = () -> {
            String password = passwordField.getText();
            String confirm = confirmPasswordField.getText();
            
            boolean valid = !password.isEmpty() && password.equals(confirm) && password.length() >= 8;
            createBtn.setDisable(!valid);
        };
        
        passwordField.textProperty().addListener((obs, oldText, newText) -> validatePasswords.run());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> validatePasswords.run());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            createBackup(vaultDirectory, password, backupResult -> {
                // Backup completion is handled by the notification system
            });
        });
    }
    
    private List<File> getAllFiles(File directory) {
        List<File> files = new ArrayList<>();
        getAllFilesRecursive(directory, files);
        return files;
    }
    
    private void getAllFilesRecursive(File directory, List<File> files) {
        File[] children = directory.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    getAllFilesRecursive(child, files);
                } else {
                    files.add(child);
                }
            }
        }
    }
    
    private void createZipArchive(File baseDirectory, List<File> files, File zipFile, Task<?> task) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[8192];
            
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                
                // Progress updates must be called from within the Task's call() method
                // TODO: Implement progress callback mechanism
                
                // Get relative path
                String relativePath = baseDirectory.toPath().relativize(file.toPath()).toString();
                
                ZipEntry entry = new ZipEntry(relativePath);
                zos.putNextEntry(entry);
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                
                zos.closeEntry();
            }
        }
    }
    
    private void encryptFile(File inputFile, File outputFile, String password) throws Exception {
        // Generate salt and derive key
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        
        SecretKeySpec key = deriveKeyFromPassword(password, salt);
        
        // Generate IV
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            // Write salt and IV to the beginning of the file
            fos.write(salt);
            fos.write(iv);
            
            // Encrypt and write data
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    fos.write(encrypted);
                }
            }
            
            // Write final block
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                fos.write(finalBlock);
            }
        }
    }
    
    private SecretKeySpec deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        // Simple key derivation - in production, use PBKDF2 or similar
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes("UTF-8"));
        md.update(salt);
        byte[] keyBytes = md.digest();
        
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * Backup result class
     */
    public static class BackupResult {
        public final boolean success;
        public final String message;
        
        public BackupResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean hasErrors() {
            return !success;
        }
    }
    
    /**
     * Show restore dialog for selecting backup file
     */
    public void showRestoreDialog(File defaultDirectory) {
        // TODO: Implement restore dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File to Restore");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup Files", "*.gvbackup")
        );
        if (defaultDirectory != null) {
            fileChooser.setInitialDirectory(defaultDirectory);
        }
        
        Stage stage = (Stage) parentStage;
        File backupFile = fileChooser.showOpenDialog(stage);
        if (backupFile != null) {
            // TODO: Implement restore functionality
            System.out.println("Selected backup file: " + backupFile.getAbsolutePath());
        }
    }
}