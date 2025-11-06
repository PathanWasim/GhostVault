package com.ghostvault.util;

import com.ghostvault.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates files and vault structure before upload operations
 */
public class FileUploadValidator {
    
    /**
     * Validate file accessibility and properties before upload
     */
    public static ValidationResult validateFileAccess(File file) {
        if (file == null) {
            return ValidationResult.failure("No file selected");
        }
        
        if (!file.exists()) {
            return ValidationResult.failure("File does not exist: " + file.getName());
        }
        
        if (!file.isFile()) {
            return ValidationResult.failure("Selected item is not a file: " + file.getName());
        }
        
        if (!file.canRead()) {
            return ValidationResult.failure("Cannot read file: " + file.getName() + ". Check permissions.");
        }
        
        // Check file size (max 2GB for safety)
        long maxSize = 2L * 1024 * 1024 * 1024; // 2GB
        if (file.length() > maxSize) {
            return ValidationResult.failure("File too large: " + file.getName() + ". Maximum size is 2GB.");
        }
        
        // Check if file is locked or in use
        if (!canWriteToFile(file)) {
            return ValidationResult.failure("File is locked or in use: " + file.getName());
        }
        
        return ValidationResult.success("File validation passed");
    }
    
    /**
     * Validate vault directory structure and create if needed
     */
    public static ValidationResult validateVaultStructure() {
        try {
            // Try alternative vault location if default fails
            String vaultPath = AppConfig.VAULT_DIR;
            Path vaultDir = Paths.get(vaultPath);
            
            // If default location fails, try Documents folder
            if (!createDirectoryWithPermissions(vaultDir)) {
                String documentsPath = System.getProperty("user.home") + "/Documents/GhostVault";
                vaultDir = Paths.get(documentsPath);
                vaultPath = documentsPath;
                
                if (!createDirectoryWithPermissions(vaultDir)) {
                    // Try temp directory as last resort
                    String tempPath = System.getProperty("java.io.tmpdir") + "/GhostVault";
                    vaultDir = Paths.get(tempPath);
                    vaultPath = tempPath;
                    
                    if (!createDirectoryWithPermissions(vaultDir)) {
                        return ValidationResult.failure("Cannot create vault directory in any location. Please run as administrator or check permissions.");
                    }
                }
                
                // Update the vault path for this session
                System.setProperty("ghostvault.vault.path", vaultPath);
            }
            
            // Ensure files directory exists
            Path filesDir = vaultDir.resolve("files");
            if (!createDirectoryWithPermissions(filesDir)) {
                return ValidationResult.failure("Cannot create files directory: " + filesDir);
            }
            
            // Ensure metadata directory exists
            Path metadataDir = vaultDir.resolve("metadata");
            if (!createDirectoryWithPermissions(metadataDir)) {
                return ValidationResult.failure("Cannot create metadata directory: " + metadataDir);
            }
            
            // Test write permissions by creating a test file
            Path testFile = vaultDir.resolve(".test_write_permission");
            try {
                Files.write(testFile, "test".getBytes());
                Files.deleteIfExists(testFile);
            } catch (IOException e) {
                return ValidationResult.failure("No write permission to vault directory: " + vaultDir + ". Error: " + e.getMessage());
            }
            
            return ValidationResult.success("Vault structure validated at: " + vaultDir);
            
        } catch (Exception e) {
            return ValidationResult.failure("Failed to validate vault structure: " + e.getMessage());
        }
    }
    
    /**
     * Create directory with proper permissions handling
     */
    private static boolean createDirectoryWithPermissions(Path directory) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // Test if we can write to the directory
            if (Files.isWritable(directory)) {
                return true;
            }
            
            // Try to set permissions if possible
            try {
                java.nio.file.attribute.PosixFilePermissions.fromString("rwxrwxrwx");
                // This will only work on POSIX systems, but won't hurt on Windows
            } catch (Exception ignored) {
                // Not a POSIX system, continue
            }
            
            return Files.isWritable(directory);
            
        } catch (IOException | SecurityException e) {
            System.err.println("Failed to create directory " + directory + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate available disk space for file upload
     */
    public static ValidationResult validateDiskSpace(long fileSize) {
        try {
            Path vaultDir = Paths.get(AppConfig.VAULT_DIR);
            long availableSpace = Files.getFileStore(vaultDir).getUsableSpace();
            
            // Require at least 100MB free space plus file size
            long requiredSpace = fileSize + (100L * 1024 * 1024);
            
            if (availableSpace < requiredSpace) {
                return ValidationResult.failure("Insufficient disk space. Need " + 
                    FileUtils.formatFileSize(requiredSpace) + ", available " + 
                    FileUtils.formatFileSize(availableSpace));
            }
            
            return ValidationResult.success("Sufficient disk space available");
            
        } catch (IOException e) {
            return ValidationResult.failure("Could not check disk space: " + e.getMessage());
        }
    }
    
    /**
     * Validate multiple files for batch upload
     */
    public static ValidationResult validateMultipleFiles(File[] files) {
        if (files == null || files.length == 0) {
            return ValidationResult.failure("No files selected for upload");
        }
        
        long totalSize = 0;
        StringBuilder errors = new StringBuilder();
        
        for (File file : files) {
            ValidationResult result = validateFileAccess(file);
            if (!result.isValid()) {
                if (errors.length() > 0) {
                    errors.append("\n");
                }
                errors.append(result.getErrorMessage());
            } else {
                totalSize += file.length();
            }
        }
        
        if (errors.length() > 0) {
            return ValidationResult.failure("File validation errors:\n" + errors.toString());
        }
        
        // Check total disk space needed
        ValidationResult spaceResult = validateDiskSpace(totalSize);
        if (!spaceResult.isValid()) {
            return spaceResult;
        }
        
        return ValidationResult.success("All files validated successfully");
    }
    
    /**
     * Check if we can write to a file (not locked)
     */
    private static boolean canWriteToFile(File file) {
        try {
            // Try to open file for reading to check if it's locked
            return file.canRead() && !isFileLocked(file);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if file is locked by another process
     */
    private static boolean isFileLocked(File file) {
        try {
            // Try to rename file to itself (this fails if file is locked)
            return !file.renameTo(file);
        } catch (Exception e) {
            return true; // Assume locked if we can't check
        }
    }
    
    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getErrorMessage() {
            return valid ? null : message;
        }
    }
}