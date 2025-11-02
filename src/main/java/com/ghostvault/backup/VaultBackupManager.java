package com.ghostvault.backup;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.model.VaultFile;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Enhanced VaultBackupManager with full backup and restore functionality
 */
public class VaultBackupManager {
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private CryptoManager cryptoManager;
    private com.ghostvault.audit.AuditManager auditManager;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public VaultBackupManager(FileManager fileManager, MetadataManager metadataManager) {
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
    }
    
    public VaultBackupManager(CryptoManager cryptoManager, FileManager fileManager, 
                             MetadataManager metadataManager, com.ghostvault.audit.AuditManager auditManager) {
        this.cryptoManager = cryptoManager;
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.auditManager = auditManager;
    }
    
    /**
     * Create encrypted backup of entire vault
     */
    public void createBackup(File backupFile, SecretKey key) throws Exception {
        System.out.println("🔄 Starting backup creation...");
        
        if (fileManager == null) {
            throw new Exception("FileManager not available for backup");
        }
        
        if (metadataManager == null) {
            throw new Exception("MetadataManager not available for backup");
        }
        
        // Get vault directory
        String vaultDir = com.ghostvault.config.AppConfig.getVaultDir();
        File vaultFolder = new File(vaultDir);
        
        if (!vaultFolder.exists()) {
            throw new Exception("Vault directory does not exist: " + vaultDir);
        }
        
        System.out.println("📁 Vault directory: " + vaultDir);
        
        // Verify vault structure before backup
        verifyVaultStructureForBackup(vaultFolder);
        
        // Create temporary zip file
        File tempZip = File.createTempFile("vault_backup_", ".zip");
        
        try {
            // Create zip archive of vault directory
            createZipArchive(vaultFolder, tempZip);
            
            // Encrypt the zip file
            encryptBackupFile(tempZip, backupFile, key);
            
            System.out.println("✅ Backup created successfully: " + backupFile.getAbsolutePath());
            System.out.println("📊 Backup size: " + formatFileSize(backupFile.length()));
            
        } finally {
            // Clean up temporary file
            if (tempZip.exists()) {
                tempZip.delete();
            }
        }
    }
    
    /**
     * Create zip archive of vault directory
     */
    private void createZipArchive(File sourceDir, File zipFile) throws Exception {
        System.out.println("📦 Creating zip archive...");
        
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            addDirectoryToZip(sourceDir, sourceDir.getName(), zos);
        }
        
        System.out.println("📦 Zip archive created: " + formatFileSize(zipFile.length()));
    }
    
    /**
     * Recursively add directory contents to zip
     */
    private void addDirectoryToZip(File dir, String baseName, ZipOutputStream zos) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            String entryName = baseName + "/" + file.getName();
            
            if (file.isDirectory()) {
                // Add directory entry
                ZipEntry dirEntry = new ZipEntry(entryName + "/");
                zos.putNextEntry(dirEntry);
                zos.closeEntry();
                
                // Recursively add directory contents
                addDirectoryToZip(file, entryName, zos);
            } else {
                // Skip user-specific files that shouldn't be backed up
                if (file.getName().equals("passwords.dat") || file.getName().equals("auth_attempts.dat")) {
                    System.out.println("🔐 Skipped user-specific file: " + file.getName());
                } else {
                    // Add file entry
                    ZipEntry fileEntry = new ZipEntry(entryName);
                    zos.putNextEntry(fileEntry);
                    
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    
                    zos.closeEntry();
                    System.out.println("📄 Added to backup: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
                }
            }
        }
    }
    
    /**
     * Encrypt backup file using AES-GCM
     */
    private void encryptBackupFile(File sourceFile, File encryptedFile, SecretKey key) throws Exception {
        System.out.println("🔐 Encrypting backup file...");
        
        // Generate random IV
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        
        // Setup cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(encryptedFile)) {
            
            // Write IV first
            fos.write(iv);
            
            // Write backup metadata
            String metadata = createBackupMetadata();
            byte[] metadataBytes = metadata.getBytes("UTF-8");
            byte[] encryptedMetadata = cipher.update(metadataBytes);
            
            // Write metadata length and encrypted metadata
            fos.write(intToBytes(encryptedMetadata.length));
            fos.write(encryptedMetadata);
            
            // Encrypt and write file data
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] encryptedChunk = cipher.update(buffer, 0, bytesRead);
                if (encryptedChunk != null) {
                    fos.write(encryptedChunk);
                }
            }
            
            // Write final encrypted block
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                fos.write(finalBlock);
            }
        }
        
        System.out.println("🔐 Backup encrypted successfully");
    }
    
    /**
     * Create backup metadata
     */
    private String createBackupMetadata() {
        return String.format("GhostVault Backup\nVersion: 1.0\nCreated: %s\nType: Full Vault Backup",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * Convert int to byte array
     */
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }
    
    /**
     * Convert byte array to int
     */
    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
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
     * Restore vault from encrypted backup
     */
    public void restoreBackup(File backupFile, SecretKey key) throws Exception {
        System.out.println("🔄 Starting backup restoration...");
        System.out.println("📁 Backup file: " + backupFile.getAbsolutePath());
        System.out.println("📊 Backup size: " + formatFileSize(backupFile.length()));
        
        if (!backupFile.exists()) {
            throw new Exception("Backup file does not exist: " + backupFile.getAbsolutePath());
        }
        
        if (backupFile.length() == 0) {
            throw new Exception("Backup file is empty: " + backupFile.getAbsolutePath());
        }
        
        // Create temporary file for decrypted zip
        File tempZip = File.createTempFile("vault_restore_", ".zip");
        
        try {
            // Clear existing vault directory before restore
            String vaultDir = com.ghostvault.config.AppConfig.getVaultDir();
            File vaultFolder = new File(vaultDir);
            
            System.out.println("🗑️ Target vault directory: " + vaultDir);
            System.out.println("📁 Vault directory exists: " + vaultFolder.exists());
            
            if (vaultFolder.exists()) {
                System.out.println("🗑️ Clearing existing vault contents...");
                deleteDirectoryContents(vaultFolder);
                System.out.println("✅ Vault directory cleared");
            } else {
                System.out.println("📁 Creating vault directory...");
                boolean created = vaultFolder.mkdirs();
                System.out.println("📁 Vault directory created: " + created);
                if (!created) {
                    throw new Exception("Failed to create vault directory: " + vaultDir);
                }
            }
            
            // Decrypt backup file
            System.out.println("🔓 Decrypting backup file...");
            decryptBackupFile(backupFile, tempZip, key);
            System.out.println("✅ Backup decrypted successfully");
            System.out.println("📊 Decrypted size: " + formatFileSize(tempZip.length()));
            
            // Verify decrypted file
            if (!tempZip.exists() || tempZip.length() == 0) {
                throw new Exception("Decryption failed - temporary file is empty or missing");
            }
            
            // Extract zip to vault directory
            System.out.println("📦 Extracting backup archive...");
            extractZipArchive(tempZip, vaultFolder);
            
            // Verify extraction by checking subdirectories
            verifyBackupExtraction(vaultFolder);
            
            // Reload metadata after restore
            System.out.println("🔄 Reloading metadata after restore...");
            reloadMetadataAfterRestore();
            
            System.out.println("✅ Backup restored successfully");
            
        } finally {
            // Clean up temporary file
            if (tempZip.exists()) {
                tempZip.delete();
            }
        }
    }
    
    /**
     * Reload metadata and verify file persistence after restore
     */
    private void reloadMetadataAfterRestore() throws Exception {
        try {
            if (metadataManager != null) {
                // Force reload metadata from disk
                metadataManager.loadMetadata();
                
                // Verify that files are accessible
                List<VaultFile> allFiles = metadataManager.getAllFiles();
                System.out.println("📋 Metadata loaded: " + allFiles.size() + " files");
                
                // Verify file persistence
                int accessibleFiles = 0;
                for (VaultFile vaultFile : allFiles) {
                    try {
                        if (fileManager != null) {
                            byte[] fileData = fileManager.retrieveFile(vaultFile);
                            if (fileData != null && fileData.length > 0) {
                                accessibleFiles++;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ File not accessible after restore: " + vaultFile.getFileName());
                    }
                }
                
                System.out.println("✅ Files accessible after restore: " + accessibleFiles + "/" + allFiles.size());
                
                if (accessibleFiles < allFiles.size()) {
                    System.err.println("⚠️ Warning: Some files may not be accessible after restore");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error reloading metadata after restore: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verify vault structure before backup to ensure all files are included
     */
    private void verifyVaultStructureForBackup(File vaultFolder) throws Exception {
        System.out.println("🔍 Verifying vault structure for backup...");
        
        // Check for files directory
        File filesDir = new File(vaultFolder, "files");
        if (filesDir.exists() && filesDir.isDirectory()) {
            File[] files = filesDir.listFiles();
            int fileCount = (files != null) ? files.length : 0;
            System.out.println("📁 Files directory contains: " + fileCount + " files");
        } else {
            System.out.println("⚠️ Files directory not found or empty");
        }
        
        // Check for metadata
        File metadataDir = new File(vaultFolder, "metadata");
        if (metadataDir.exists() && metadataDir.isDirectory()) {
            File[] metaFiles = metadataDir.listFiles();
            int metaCount = (metaFiles != null) ? metaFiles.length : 0;
            System.out.println("📋 Metadata directory contains: " + metaCount + " files");
        } else {
            System.out.println("⚠️ Metadata directory not found");
        }
        
        // Get total vault size
        long totalSize = calculateDirectorySize(vaultFolder);
        System.out.println("📊 Total vault size: " + formatFileSize(totalSize));
        
        if (totalSize == 0) {
            System.out.println("⚠️ Warning: Vault appears to be empty");
        }
    }
    
    /**
     * Calculate total size of directory and all subdirectories
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * Verify backup extraction by checking all subdirectories
     */
    private void verifyBackupExtraction(File vaultFolder) {
        System.out.println("🔍 Verifying backup extraction...");
        
        int totalFiles = 0;
        long totalSize = 0;
        
        // Check files directory
        File filesDir = new File(vaultFolder, "files");
        if (filesDir.exists() && filesDir.isDirectory()) {
            File[] files = filesDir.listFiles();
            if (files != null) {
                totalFiles += files.length;
                for (File file : files) {
                    totalSize += file.length();
                    System.out.println("📄 Restored file: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
                }
            }
        }
        
        // Check metadata directory
        File metadataDir = new File(vaultFolder, "metadata");
        if (metadataDir.exists() && metadataDir.isDirectory()) {
            File[] metaFiles = metadataDir.listFiles();
            if (metaFiles != null) {
                for (File file : metaFiles) {
                    if (file.isFile()) {
                        System.out.println("📋 Restored metadata: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
                    }
                }
            }
        }
        
        // Check logs directory
        File logsDir = new File(vaultFolder, "logs");
        if (logsDir.exists() && logsDir.isDirectory()) {
            File[] logFiles = logsDir.listFiles();
            if (logFiles != null) {
                for (File file : logFiles) {
                    if (file.isFile()) {
                        System.out.println("📝 Restored log: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
                    }
                }
            }
        }
        
        System.out.println("✅ Backup extraction verified: " + totalFiles + " data files restored (" + formatFileSize(totalSize) + ")");
        
        if (totalFiles == 0) {
            System.out.println("⚠️ Warning: No data files were found in backup");
        }
    }
    
    /**
     * Decrypt backup file
     */
    private void decryptBackupFile(File encryptedFile, File decryptedFile, SecretKey key) throws Exception {
        System.out.println("🔓 Decrypting backup file...");
        
        try (FileInputStream fis = new FileInputStream(encryptedFile);
             FileOutputStream fos = new FileOutputStream(decryptedFile)) {
            
            // Read IV
            byte[] iv = new byte[12];
            if (fis.read(iv) != 12) {
                throw new Exception("Invalid backup file: Cannot read IV");
            }
            
            // Setup cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            // Read metadata length
            byte[] lengthBytes = new byte[4];
            if (fis.read(lengthBytes) != 4) {
                throw new Exception("Invalid backup file: Cannot read metadata length");
            }
            int metadataLength = bytesToInt(lengthBytes);
            
            // Read and decrypt metadata
            byte[] encryptedMetadata = new byte[metadataLength];
            if (fis.read(encryptedMetadata) != metadataLength) {
                throw new Exception("Invalid backup file: Cannot read metadata");
            }
            
            byte[] decryptedMetadata = cipher.update(encryptedMetadata);
            String metadata = new String(decryptedMetadata, "UTF-8");
            System.out.println("📋 Backup metadata: " + metadata.replace("\n", " | "));
            
            // Decrypt file data
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedChunk = cipher.update(buffer, 0, bytesRead);
                if (decryptedChunk != null) {
                    fos.write(decryptedChunk);
                }
            }
            
            // Write final decrypted block
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                fos.write(finalBlock);
            }
        }
        
        System.out.println("🔓 Backup decrypted successfully");
    }
    
    /**
     * Delete all contents of a directory but keep the directory itself
     */
    private void deleteDirectoryContents(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Don't delete password files during restore - they should remain user-specific
                    if (!file.getName().equals("passwords.dat") && !file.getName().equals("auth_attempts.dat")) {
                        if (file.isDirectory()) {
                            deleteDirectoryContents(file);
                            file.delete();
                        } else {
                            file.delete();
                        }
                        System.out.println("🗑️ Deleted: " + file.getName());
                    } else {
                        System.out.println("🔐 Preserved: " + file.getName() + " (user-specific)");
                    }
                }
            }
        }
    }
    
    /**
     * Extract zip archive to target directory
     */
    private void extractZipArchive(File zipFile, File targetDir) throws Exception {
        System.out.println("📦 Extracting backup archive...");
        
        // Create target directory if it doesn't exist
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Skip the top-level vault directory and extract contents directly
                if (entryName.contains("/")) {
                    // Remove the first directory component if it exists
                    String[] parts = entryName.split("/", 2);
                    if (parts.length > 1) {
                        entryName = parts[1];
                    }
                }
                
                // Skip empty entries
                if (entryName.isEmpty()) {
                    zis.closeEntry();
                    continue;
                }
                
                File entryFile = new File(targetDir, entryName);
                
                // Ensure parent directories exist
                File parentDir = entryFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                    System.out.println("📁 Created directory: " + entryName);
                } else {
                    // Extract file
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                    System.out.println("📄 Restored: " + entryName + " (" + formatFileSize(entryFile.length()) + ")");
                }
                
                zis.closeEntry();
            }
        }
        
        System.out.println("📦 Archive extracted successfully");
    }
    
    /**
     * Verify backup file integrity
     */
    public boolean verifyBackup(File backupFile, SecretKey key) throws Exception {
        System.out.println("🔍 Verifying backup file...");
        
        if (!backupFile.exists()) {
            System.out.println("❌ Backup file does not exist");
            return false;
        }
        
        try {
            // Try to decrypt and read metadata
            try (FileInputStream fis = new FileInputStream(backupFile)) {
                // Read IV
                byte[] iv = new byte[12];
                if (fis.read(iv) != 12) {
                    System.out.println("❌ Invalid backup file: Cannot read IV");
                    return false;
                }
                
                // Setup cipher
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
                
                // Read metadata length
                byte[] lengthBytes = new byte[4];
                if (fis.read(lengthBytes) != 4) {
                    System.out.println("❌ Invalid backup file: Cannot read metadata length");
                    return false;
                }
                int metadataLength = bytesToInt(lengthBytes);
                
                // Read and decrypt metadata
                byte[] encryptedMetadata = new byte[metadataLength];
                if (fis.read(encryptedMetadata) != metadataLength) {
                    System.out.println("❌ Invalid backup file: Cannot read metadata");
                    return false;
                }
                
                byte[] decryptedMetadata = cipher.update(encryptedMetadata);
                String metadata = new String(decryptedMetadata, "UTF-8");
                
                if (metadata.startsWith("GhostVault Backup")) {
                    System.out.println("✅ Backup file is valid");
                    System.out.println("📋 " + metadata.replace("\n", " | "));
                    return true;
                } else {
                    System.out.println("❌ Invalid backup metadata");
                    return false;
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Backup verification failed: " + e.getMessage());
            return false;
        }
    }
}