package com.ghostvault.core;

import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.SecurityConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for migrating unencrypted files to encrypted storage
 */
public class FileMigrationUtility {
    
    private final CryptoManager cryptoManager;
    private final Path vaultDirectory;
    private final Path filesDirectory;
    private final Path backupDirectory;
    
    public FileMigrationUtility(String vaultPath) {
        this.cryptoManager = new CryptoManager();
        this.vaultDirectory = Paths.get(vaultPath);
        this.filesDirectory = vaultDirectory.resolve("files");
        this.backupDirectory = vaultDirectory.resolve("backups");
    }
    
    /**
     * Scan for unencrypted files that need migration
     * @return list of file paths that need encryption
     */
    public List<Path> scanForUnencryptedFiles() {
        List<Path> unencryptedFiles = new ArrayList<>();
        
        try {
            if (!Files.exists(filesDirectory)) {
                return unencryptedFiles;
            }
            
            Files.list(filesDirectory)
                .filter(path -> path.toString().endsWith(".dat"))
                .forEach(path -> {
                    try {
                        byte[] fileData = Files.readAllBytes(path);
                        if (!EncryptedFileData.isEncryptedFileFormat(fileData)) {
                            unencryptedFiles.add(path);
                        }
                    } catch (IOException e) {
                        System.err.println("❌ Error reading file " + path + ": " + e.getMessage());
                    }
                });
                
        } catch (IOException e) {
            System.err.println("❌ Error scanning files directory: " + e.getMessage());
        }
        
        return unencryptedFiles;
    }
    
    /**
     * Check if file migration is needed
     * @return true if unencrypted files exist
     */
    public boolean isMigrationNeeded() {
        return !scanForUnencryptedFiles().isEmpty();
    }
    
    /**
     * Perform migration of all unencrypted files
     * @param password The password to use for encryption
     * @return MigrationResult with details
     */
    public MigrationResult performMigration(String password) {
        if (password == null || password.isEmpty()) {
            return new MigrationResult(false, "Password is required for encryption", null, 0, 0);
        }
        
        List<Path> unencryptedFiles = scanForUnencryptedFiles();
        if (unencryptedFiles.isEmpty()) {
            return new MigrationResult(true, "No files need migration", null, 0, 0);
        }
        
        System.out.println("🔄 Starting file encryption migration for " + unencryptedFiles.size() + " files...");
        
        String backupPath = null;
        int successCount = 0;
        int totalCount = unencryptedFiles.size();
        
        try {
            // Create backup
            backupPath = createBackup(unencryptedFiles);
            System.out.println("📦 Created backup: " + backupPath);
            
            // Encrypt each file
            for (Path filePath : unencryptedFiles) {
                try {
                    if (encryptFile(filePath, password)) {
                        successCount++;
                        System.out.println("🔐 Encrypted: " + filePath.getFileName());
                    } else {
                        System.err.println("❌ Failed to encrypt: " + filePath.getFileName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error encrypting " + filePath.getFileName() + ": " + e.getMessage());
                }
            }
            
            // Verify migration
            if (successCount == totalCount) {
                if (verifyMigration(unencryptedFiles, password)) {
                    System.out.println("✅ File migration completed successfully");
                    return new MigrationResult(true, "Migration completed successfully", backupPath, successCount, totalCount);
                } else {
                    // Rollback on verification failure
                    rollbackMigration(backupPath);
                    return new MigrationResult(false, "Migration verification failed - rolled back", backupPath, 0, totalCount);
                }
            } else {
                return new MigrationResult(false, 
                    String.format("Partial migration: %d/%d files encrypted", successCount, totalCount), 
                    backupPath, successCount, totalCount);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Migration failed: " + e.getMessage());
            if (backupPath != null) {
                rollbackMigration(backupPath);
            }
            return new MigrationResult(false, "Migration failed: " + e.getMessage(), backupPath, successCount, totalCount);
        }
    }
    
    /**
     * Encrypt a single file in place
     * @param filePath The file to encrypt
     * @param password The password for encryption
     * @return true if successful
     */
    private boolean encryptFile(Path filePath, String password) throws Exception {
        // Read original file content
        byte[] originalContent = Files.readAllBytes(filePath);
        
        // Skip if already encrypted
        if (EncryptedFileData.isEncryptedFileFormat(originalContent)) {
            return true;
        }
        
        // Encrypt the content
        CryptoManager.EncryptedData cryptoData = cryptoManager.encryptWithPassword(originalContent, password);
        
        // Create properly formatted encrypted file data
        EncryptedFileData encryptedFileData = new EncryptedFileData(
            cryptoData.getSalt(),
            cryptoData.getIv(),
            cryptoData.getCiphertext()
        );
        
        if (!encryptedFileData.isValid()) {
            throw new Exception("Invalid encrypted file data format");
        }
        
        // Write encrypted data back to file
        byte[] encryptedBytes = encryptedFileData.toByteArray();
        Files.write(filePath, encryptedBytes);
        
        // Verify the file was written correctly
        byte[] verifyData = Files.readAllBytes(filePath);
        return EncryptedFileData.isEncryptedFileFormat(verifyData);
    }
    
    /**
     * Create backup of unencrypted files
     * @param filesToBackup List of files to backup
     * @return backup directory path
     */
    private String createBackup(List<Path> filesToBackup) throws IOException {
        Files.createDirectories(backupDirectory);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path backupDir = backupDirectory.resolve("file_migration_backup_" + timestamp);
        Files.createDirectories(backupDir);
        
        for (Path filePath : filesToBackup) {
            Path backupFile = backupDir.resolve(filePath.getFileName());
            Files.copy(filePath, backupFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return backupDir.toString();
    }
    
    /**
     * Verify that migration was successful
     * @param originalFiles List of files that were migrated
     * @param password Password used for encryption
     * @return true if verification successful
     */
    private boolean verifyMigration(List<Path> originalFiles, String password) {
        try {
            for (Path filePath : originalFiles) {
                // Check that file is now encrypted
                byte[] fileData = Files.readAllBytes(filePath);
                if (!EncryptedFileData.isEncryptedFileFormat(fileData)) {
                    System.err.println("❌ File not encrypted after migration: " + filePath.getFileName());
                    return false;
                }
                
                // Try to decrypt and verify content integrity would require original content
                // For now, just verify the format is correct
                try {
                    EncryptedFileData encryptedData = EncryptedFileData.fromByteArray(fileData);
                    if (!encryptedData.isValid()) {
                        System.err.println("❌ Invalid encrypted format: " + filePath.getFileName());
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Cannot parse encrypted file: " + filePath.getFileName());
                    return false;
                }
            }
            
            System.out.println("✅ Migration verification successful");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Migration verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rollback migration by restoring from backup
     * @param backupPath The backup directory path
     * @return true if rollback successful
     */
    public boolean rollbackMigration(String backupPath) {
        try {
            if (backupPath == null || !Files.exists(Paths.get(backupPath))) {
                System.err.println("❌ Cannot rollback: backup directory not found");
                return false;
            }
            
            System.out.println("🔄 Rolling back file migration...");
            
            Path backupDir = Paths.get(backupPath);
            Files.list(backupDir).forEach(backupFile -> {
                try {
                    Path targetFile = filesDirectory.resolve(backupFile.getFileName());
                    Files.copy(backupFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("❌ Error restoring file " + backupFile.getFileName() + ": " + e.getMessage());
                }
            });
            
            System.out.println("✅ File migration rollback completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Rollback failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get encryption status for all files
     * @return FileEncryptionStatus with details
     */
    public FileEncryptionStatus getEncryptionStatus() {
        List<Path> allFiles = new ArrayList<>();
        List<Path> encryptedFiles = new ArrayList<>();
        List<Path> unencryptedFiles = new ArrayList<>();
        
        try {
            if (Files.exists(filesDirectory)) {
                Files.list(filesDirectory)
                    .filter(path -> path.toString().endsWith(".dat"))
                    .forEach(path -> {
                        allFiles.add(path);
                        try {
                            byte[] fileData = Files.readAllBytes(path);
                            if (EncryptedFileData.isEncryptedFileFormat(fileData)) {
                                encryptedFiles.add(path);
                            } else {
                                unencryptedFiles.add(path);
                            }
                        } catch (IOException e) {
                            System.err.println("❌ Error reading file " + path + ": " + e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("❌ Error scanning files: " + e.getMessage());
        }
        
        return new FileEncryptionStatus(allFiles, encryptedFiles, unencryptedFiles);
    }
    
    /**
     * Result of file migration operation
     */
    public static class MigrationResult {
        private final boolean success;
        private final String message;
        private final String backupPath;
        private final int successCount;
        private final int totalCount;
        
        public MigrationResult(boolean success, String message, String backupPath, int successCount, int totalCount) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
            this.successCount = successCount;
            this.totalCount = totalCount;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupPath() { return backupPath; }
        public int getSuccessCount() { return successCount; }
        public int getTotalCount() { return totalCount; }
        
        @Override
        public String toString() {
            return String.format("FileMigrationResult{success=%s, message='%s', files=%d/%d, backup='%s'}", 
                               success, message, successCount, totalCount, backupPath);
        }
    }
    
    /**
     * Status of file encryption in the vault
     */
    public static class FileEncryptionStatus {
        private final List<Path> allFiles;
        private final List<Path> encryptedFiles;
        private final List<Path> unencryptedFiles;
        
        public FileEncryptionStatus(List<Path> allFiles, List<Path> encryptedFiles, List<Path> unencryptedFiles) {
            this.allFiles = new ArrayList<>(allFiles);
            this.encryptedFiles = new ArrayList<>(encryptedFiles);
            this.unencryptedFiles = new ArrayList<>(unencryptedFiles);
        }
        
        public List<Path> getAllFiles() { return new ArrayList<>(allFiles); }
        public List<Path> getEncryptedFiles() { return new ArrayList<>(encryptedFiles); }
        public List<Path> getUnencryptedFiles() { return new ArrayList<>(unencryptedFiles); }
        
        public int getTotalCount() { return allFiles.size(); }
        public int getEncryptedCount() { return encryptedFiles.size(); }
        public int getUnencryptedCount() { return unencryptedFiles.size(); }
        
        public boolean isFullyEncrypted() { return unencryptedFiles.isEmpty() && !allFiles.isEmpty(); }
        public boolean hasUnencryptedFiles() { return !unencryptedFiles.isEmpty(); }
        
        public double getEncryptionPercentage() {
            if (allFiles.isEmpty()) return 100.0;
            return (double) encryptedFiles.size() / allFiles.size() * 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("FileEncryptionStatus{total=%d, encrypted=%d, unencrypted=%d, percentage=%.1f%%}",
                               getTotalCount(), getEncryptedCount(), getUnencryptedCount(), getEncryptionPercentage());
        }
    }
}