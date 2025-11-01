package com.ghostvault.core;

import com.ghostvault.security.EnhancedKeyManager;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.model.VaultFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handles metadata recovery operations for orphaned encrypted files
 * Attempts multiple recovery strategies to restore file metadata
 */
public class MetadataRecoveryManager {
    
    private static final Logger logger = Logger.getLogger(MetadataRecoveryManager.class.getName());
    
    private final String vaultPath;
    private final CryptoManager cryptoManager;
    private final EnhancedKeyManager keyManager;
    
    public MetadataRecoveryManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.keyManager = new EnhancedKeyManager();
    }
    
    /**
     * Attempt to recover metadata for orphaned files
     * 
     * @param orphanedFiles List of encrypted files without metadata
     * @param password User's password for key derivation
     * @return RecoveryResult with recovered files and status
     */
    public RecoveryResult attemptRecovery(List<File> orphanedFiles, String password) {
        logger.info("Starting recovery attempt for " + orphanedFiles.size() + " orphaned files");
        
        RecoveryResult result = new RecoveryResult();
        
        // Generate key variants for recovery attempts
        SecretKey[] keyVariants;
        try {
            keyVariants = keyManager.generateKeyVariants(password);
        } catch (Exception e) {
            logger.severe("Failed to generate key variants: " + e.getMessage());
            return result;
        }
        
        for (File orphanedFile : orphanedFiles) {
            try {
                VaultFile recoveredFile = attemptFileRecovery(orphanedFile, keyVariants);
                if (recoveredFile != null) {
                    result.addRecoveredFile(recoveredFile);
                    logger.info("Successfully recovered: " + orphanedFile.getName());
                } else {
                    result.addUnrecoverableFile(orphanedFile);
                    logger.warning("Could not recover: " + orphanedFile.getName());
                }
            } catch (Exception e) {
                result.addUnrecoverableFile(orphanedFile);
                result.addWarning("Recovery failed for " + orphanedFile.getName() + ": " + e.getMessage());
                logger.log(Level.WARNING, "Recovery error for " + orphanedFile.getName(), e);
            }
        }
        
        result.setSuccessful(!result.getRecoveredFiles().isEmpty());
        result.setRecoveryMethod("Multi-key variant recovery");
        
        logger.info("Recovery complete: " + result.getRecoveredFiles().size() + " recovered, " + 
                   result.getUnrecoverableFiles().size() + " unrecoverable");
        
        return result;
    }
    
    /**
     * Attempt to recover a single file using multiple key variants
     */
    private VaultFile attemptFileRecovery(File encryptedFile, SecretKey[] keyVariants) {
        for (int i = 0; i < keyVariants.length; i++) {
            try {
                VaultFile recovered = rebuildMetadata(encryptedFile, keyVariants[i]);
                if (recovered != null) {
                    logger.info("File recovered using key variant " + i + ": " + encryptedFile.getName());
                    return recovered;
                }
            } catch (Exception e) {
                logger.fine("Key variant " + i + " failed for " + encryptedFile.getName() + ": " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Rebuild metadata from encrypted file headers
     * 
     * @param encryptedFile The orphaned encrypted file
     * @param key The key to try for decryption
     * @return VaultFile with reconstructed metadata, or null if recovery fails
     */
    public boolean rebuildMetadata(File encryptedFile, SecretKey[] keyVariants) {
        for (SecretKey key : keyVariants) {
            VaultFile recovered = rebuildMetadata(encryptedFile, key);
            if (recovered != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Rebuild metadata from encrypted file using a specific key
     */
    private VaultFile rebuildMetadata(File encryptedFile, SecretKey key) {
        try {
            // Read the encrypted file
            byte[] encryptedData = Files.readAllBytes(encryptedFile.toPath());
            
            if (encryptedData.length < 32) { // Minimum size check
                return null;
            }
            
            // Try to decrypt the file to validate the key
            CryptoManager.EncryptedData encData = new CryptoManager.EncryptedData(encryptedData, new byte[16]);
            byte[] decryptedData = cryptoManager.decrypt(encData, key);
            
            if (decryptedData == null || decryptedData.length == 0) {
                return null;
            }
            
            // If decryption succeeds, create VaultFile metadata
            String originalName = extractOriginalName(encryptedFile.getName());
            String fileId = UUID.randomUUID().toString();
            String checksum = cryptoManager.calculateSHA256(decryptedData);
            long uploadTime = encryptedFile.lastModified();
            
            VaultFile vaultFile = new VaultFile(
                originalName,
                decryptedData.length,
                "application/octet-stream"
            );
            
            // Add recovery tag to indicate this file was recovered
            vaultFile.setTags("recovered,metadata-reconstruction");
            
            logger.info("Successfully rebuilt metadata for: " + originalName);
            return vaultFile;
            
        } catch (Exception e) {
            logger.fine("Failed to rebuild metadata for " + encryptedFile.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract original filename from encrypted filename
     * Attempts to reverse common naming patterns
     */
    private String extractOriginalName(String encryptedName) {
        // Remove .enc extension if present
        if (encryptedName.endsWith(".enc")) {
            encryptedName = encryptedName.substring(0, encryptedName.length() - 4);
        }
        
        // If it's a UUID, generate a generic name
        if (isUUID(encryptedName)) {
            return "recovered_file_" + encryptedName.substring(0, 8);
        }
        
        return encryptedName;
    }
    
    /**
     * Extract file extension from filename
     */
    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }
    
    /**
     * Check if a string is a UUID format
     */
    private boolean isUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Create backup of current metadata
     */
    public void createMetadataBackup() {
        try {
            Path metadataPath = Paths.get(vaultPath, "metadata.enc");
            if (!Files.exists(metadataPath)) {
                logger.info("No metadata file to backup");
                return;
            }
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path backupPath = Paths.get(vaultPath, "metadata_backup_" + timestamp + ".enc");
            
            Files.copy(metadataPath, backupPath);
            
            logger.info("Metadata backup created: " + backupPath.getFileName());
            
            // Keep only the 5 most recent backups
            cleanupOldBackups();
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create metadata backup", e);
        }
    }
    
    /**
     * Restore metadata from backup
     */
    public boolean restoreMetadataFromBackup(String backupFileName) {
        try {
            Path backupPath = Paths.get(vaultPath, backupFileName);
            Path metadataPath = Paths.get(vaultPath, "metadata.enc");
            
            if (!Files.exists(backupPath)) {
                logger.warning("Backup file not found: " + backupFileName);
                return false;
            }
            
            // Create backup of current metadata before restore
            if (Files.exists(metadataPath)) {
                Path currentBackup = Paths.get(vaultPath, "metadata_pre_restore_" + System.currentTimeMillis() + ".enc");
                Files.copy(metadataPath, currentBackup);
            }
            
            // Restore from backup
            Files.copy(backupPath, metadataPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Metadata restored from backup: " + backupFileName);
            return true;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to restore metadata from backup", e);
            return false;
        }
    }
    
    /**
     * Get list of available metadata backups
     */
    public List<String> getAvailableBackups() {
        List<String> backups = new ArrayList<>();
        
        try {
            Path vaultDir = Paths.get(vaultPath);
            if (!Files.exists(vaultDir)) {
                return backups;
            }
            
            Files.list(vaultDir)
                .filter(path -> path.getFileName().toString().startsWith("metadata_backup_"))
                .filter(path -> path.getFileName().toString().endsWith(".enc"))
                .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
                .forEach(path -> backups.add(path.getFileName().toString()));
                
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to list metadata backups", e);
        }
        
        return backups;
    }
    
    /**
     * Clean up old metadata backups, keeping only the 5 most recent
     */
    private void cleanupOldBackups() {
        try {
            Path vaultDir = Paths.get(vaultPath);
            List<Path> backups = new ArrayList<>();
            
            Files.list(vaultDir)
                .filter(path -> path.getFileName().toString().startsWith("metadata_backup_"))
                .filter(path -> path.getFileName().toString().endsWith(".enc"))
                .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
                .forEach(backups::add);
            
            // Keep only the 5 most recent backups
            if (backups.size() > 5) {
                for (int i = 5; i < backups.size(); i++) {
                    try {
                        Files.delete(backups.get(i));
                        logger.fine("Deleted old backup: " + backups.get(i).getFileName());
                    } catch (IOException e) {
                        logger.warning("Failed to delete old backup: " + backups.get(i).getFileName());
                    }
                }
            }
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to cleanup old backups", e);
        }
    }
    
    /**
     * Validate metadata integrity
     */
    public boolean validateMetadata(String metadataPath, SecretKey key) {
        try {
            Path path = Paths.get(metadataPath);
            if (!Files.exists(path)) {
                return false;
            }
            
            byte[] encryptedMetadata = Files.readAllBytes(path);
            CryptoManager.EncryptedData encData = new CryptoManager.EncryptedData(encryptedMetadata, new byte[16]);
            byte[] decryptedMetadata = cryptoManager.decrypt(encData, key);
            
            // Basic validation - check if decryption succeeded and data is not empty
            return decryptedMetadata != null && decryptedMetadata.length > 0;
            
        } catch (Exception e) {
            logger.fine("Metadata validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Recovery result container
     */
    public static class RecoveryResult {
        private boolean successful;
        private List<VaultFile> recoveredFiles;
        private List<File> unrecoverableFiles;
        private String recoveryMethod;
        private List<String> warnings;
        
        public RecoveryResult() {
            this.recoveredFiles = new ArrayList<>();
            this.unrecoverableFiles = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
        
        // Getters and setters
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
        
        public List<VaultFile> getRecoveredFiles() { return recoveredFiles; }
        public void addRecoveredFile(VaultFile file) { this.recoveredFiles.add(file); }
        
        public List<File> getUnrecoverableFiles() { return unrecoverableFiles; }
        public void addUnrecoverableFile(File file) { this.unrecoverableFiles.add(file); }
        
        public String getRecoveryMethod() { return recoveryMethod; }
        public void setRecoveryMethod(String recoveryMethod) { this.recoveryMethod = recoveryMethod; }
        
        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }
        
        public int getTotalFiles() {
            return recoveredFiles.size() + unrecoverableFiles.size();
        }
        
        public double getSuccessRate() {
            int total = getTotalFiles();
            return total > 0 ? (double) recoveredFiles.size() / total : 0.0;
        }
    }
}