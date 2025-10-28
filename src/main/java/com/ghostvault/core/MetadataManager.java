package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.MemoryUtils;
import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages encrypted metadata for vault files with serialization support
 */
public class MetadataManager {
    
    /**
     * File metadata wrapper for compatibility
     */
    public static class FileMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final VaultFile vaultFile;
        
        public FileMetadata(VaultFile vaultFile) {
            this.vaultFile = vaultFile;
        }
        
        public VaultFile getVaultFile() { return vaultFile; }
        public String getFileName() { return vaultFile.getOriginalName(); }
        public String getFileId() { return vaultFile.getFileId(); }
        public long getSize() { return vaultFile.getSize(); }
        public String getHash() { return vaultFile.getHash(); }
        public long getUploadTime() { return vaultFile.getUploadTime(); }
        public String getTags() { return vaultFile.getTags(); }
    }
    
    private final String metadataFilePath;
    private final Map<String, VaultFile> fileRegistry;
    private final CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    public MetadataManager(String metadataFilePath) throws Exception {
        this.metadataFilePath = metadataFilePath;
        this.fileRegistry = new ConcurrentHashMap<>();
        this.cryptoManager = new CryptoManager();
        
        // Ensure metadata directory exists
        Path metadataPath = Paths.get(metadataFilePath);
        if (metadataPath.getParent() != null) {
            Files.createDirectories(metadataPath.getParent());
        }
    }
    
    /**
     * Set encryption key for metadata operations
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    /**
     * Add file to metadata registry
     */
    public void addFile(VaultFile file) throws Exception {
        fileRegistry.put(file.getFileId(), file);
        saveMetadata();
    }
    
    /**
     * Remove file from metadata registry
     */
    public void removeFile(String fileId) throws Exception {
        fileRegistry.remove(fileId);
        saveMetadata();
    }
    
    /**
     * Remove file metadata (compatibility method)
     */
    public void removeFileMetadata(String fileId) throws Exception {
        removeFile(fileId);
    }
    
    /**
     * Get file by ID
     */
    public VaultFile getFile(String fileId) {
        return fileRegistry.get(fileId);
    }
    
    /**
     * Get file metadata (compatibility method)
     */
    public FileMetadata getFileMetadata(String fileId) {
        VaultFile vaultFile = fileRegistry.get(fileId);
        return vaultFile != null ? new FileMetadata(vaultFile) : null;
    }
    
    /**
     * Check if file exists in metadata
     */
    public boolean fileExists(String fileId) {
        return fileRegistry.containsKey(fileId);
    }
    
    /**
     * Get all files
     */
    public List<VaultFile> getAllFiles() {
        return new ArrayList<>(fileRegistry.values());
    }
    
    /**
     * Search files by name or tags
     */
    public List<VaultFile> searchFiles(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllFiles();
        }
        
        String lowerQuery = query.toLowerCase();
        return fileRegistry.values().stream()
                .filter(file -> file.matchesSearch(lowerQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Update file tags
     */
    public void updateFileTags(String fileId, String tags) throws Exception {
        VaultFile file = fileRegistry.get(fileId);
        if (file != null) {
            file.setTags(tags);
            saveMetadata();
        }
    }
    
    /**
     * Find file by name
     */
    public VaultFile findFileByName(String fileName) {
        return fileRegistry.values().stream()
                .filter(file -> file.getOriginalName().equals(fileName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get files by extension
     */
    public List<VaultFile> getFilesByExtension(String extension) {
        return fileRegistry.values().stream()
                .filter(file -> file.getExtension().equals(extension.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get total vault size
     */
    public long getTotalVaultSize() {
        return fileRegistry.values().stream()
                .mapToLong(VaultFile::getSize)
                .sum();
    }
    
    /**
     * Get file count
     */
    public int getFileCount() {
        return fileRegistry.size();
    }
    
    /**
     * Save encrypted metadata to file using serialization with automatic backup
     */
    public void saveMetadata() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        // Create backup before saving (if metadata file exists)
        Path metadataPath = Paths.get(metadataFilePath);
        if (Files.exists(metadataPath)) {
            try {
                createBackup();
            } catch (Exception e) {
                // Don't fail the save operation if backup fails
                System.err.println("Warning: Failed to create metadata backup: " + e.getMessage());
            }
        }
        
        byte[] serializedData = null;
        
        try {
            // Serialize metadata to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new HashMap<>(fileRegistry));
            }
            
            serializedData = baos.toByteArray();
            
            // Encrypt metadata
            byte[] encryptedBytes = cryptoManager.encrypt(serializedData, encryptionKey);
            CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Write to file
            FileUtils.writeEncryptedFile(metadataPath, encrypted);
            
        } finally {
            // Clear sensitive data from memory
            if (serializedData != null) {
                MemoryUtils.secureWipe(serializedData);
            }
        }
    }
    
    /**
     * Save metadata without creating backup (for internal use)
     */
    private void saveMetadataWithoutBackup() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        byte[] serializedData = null;
        
        try {
            // Serialize metadata to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new HashMap<>(fileRegistry));
            }
            
            serializedData = baos.toByteArray();
            
            // Encrypt metadata
            byte[] encryptedBytes = cryptoManager.encrypt(serializedData, encryptionKey);
            CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Write to file
            FileUtils.writeEncryptedFile(Paths.get(metadataFilePath), encrypted);
            
        } finally {
            // Clear sensitive data from memory
            if (serializedData != null) {
                MemoryUtils.secureWipe(serializedData);
            }
        }
    }
    
    /**
     * Load encrypted metadata from file using serialization with recovery capabilities
     */
    public void loadMetadata() throws Exception {
        Path metadataPath = Paths.get(metadataFilePath);
        
        if (!Files.exists(metadataPath)) {
            // No metadata file exists yet
            return;
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        // Try primary load first
        try {
            loadMetadataPrimary();
            return;
        } catch (Exception primaryException) {
            // Primary load failed, attempt recovery
            System.out.println("Primary metadata load failed, attempting recovery...");
            
            try {
                boolean recovered = attemptMetadataRecovery(primaryException);
                if (recovered) {
                    System.out.println("Metadata recovery successful");
                    return;
                }
            } catch (Exception recoveryException) {
                System.err.println("Metadata recovery also failed: " + recoveryException.getMessage());
            }
            
            // If recovery fails, throw the original exception
            throw primaryException;
        }
    }
    
    /**
     * Primary metadata loading method
     */
    private void loadMetadataPrimary() throws Exception {
        Path metadataPath = Paths.get(metadataFilePath);
        byte[] decryptedData = null;
        
        try {
            // Read encrypted data
            CryptoManager.EncryptedData encrypted = FileUtils.readEncryptedFile(metadataPath);
            
            // Decrypt metadata
            decryptedData = cryptoManager.decrypt(encrypted, encryptionKey);
            
            // Deserialize metadata
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                @SuppressWarnings("unchecked")
                Map<String, VaultFile> loadedRegistry = (Map<String, VaultFile>) ois.readObject();
                fileRegistry.clear();
                fileRegistry.putAll(loadedRegistry);
            }
        } finally {
            // Clear sensitive data from memory
            if (decryptedData != null) {
                MemoryUtils.secureWipe(decryptedData);
            }
        }
    }
    
    /**
     * Attempt metadata recovery using alternative key derivation methods
     */
    private boolean attemptMetadataRecovery(Exception originalException) throws Exception {
        // Get the vault directory to find recovery manager
        Path metadataPath = Paths.get(metadataFilePath);
        String vaultPath = metadataPath.getParent().toString();
        
        MetadataRecoveryManager recoveryManager = new MetadataRecoveryManager(vaultPath);
        
        // Try to validate metadata with alternative keys
        com.ghostvault.security.EnhancedKeyManager keyManager = cryptoManager.getEnhancedKeyManager();
        
        // We need the password to generate key variants, but we don't have it here
        // So we'll try to restore from backup first
        if (attemptBackupRestore(recoveryManager)) {
            return true;
        }
        
        // Log detailed recovery attempt information
        System.err.println("Metadata recovery failed. Original error: " + originalException.getMessage());
        System.err.println("Consider using manual recovery tools or restoring from backup.");
        
        return false;
    }
    
    /**
     * Attempt to restore metadata from backup
     */
    private boolean attemptBackupRestore(MetadataRecoveryManager recoveryManager) {
        List<String> backups = recoveryManager.getAvailableBackups();
        
        for (String backup : backups) {
            try {
                if (recoveryManager.restoreMetadataFromBackup(backup)) {
                    // Try to load the restored metadata
                    loadMetadataPrimary();
                    System.out.println("Successfully restored metadata from backup: " + backup);
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Failed to restore from backup " + backup + ": " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Load metadata with alternative key (for recovery)
     */
    public boolean loadMetadataWithKey(SecretKey alternativeKey) {
        SecretKey originalKey = this.encryptionKey;
        
        try {
            this.encryptionKey = alternativeKey;
            loadMetadataPrimary();
            return true;
        } catch (Exception e) {
            // Restore original key if alternative fails
            this.encryptionKey = originalKey;
            return false;
        }
    }
    
    /**
     * Create metadata backup before critical operations
     */
    public void createBackup() {
        try {
            Path metadataPath = Paths.get(metadataFilePath);
            String vaultPath = metadataPath.getParent().toString();
            
            MetadataRecoveryManager recoveryManager = new MetadataRecoveryManager(vaultPath);
            recoveryManager.createMetadataBackup();
        } catch (Exception e) {
            System.err.println("Failed to create metadata backup: " + e.getMessage());
        }
    }
    
    /**
     * Validate backup integrity
     */
    public boolean validateBackup(String backupFileName) {
        try {
            Path metadataPath = Paths.get(metadataFilePath);
            String vaultPath = metadataPath.getParent().toString();
            
            MetadataRecoveryManager recoveryManager = new MetadataRecoveryManager(vaultPath);
            String backupPath = vaultPath + "/" + backupFileName;
            
            return recoveryManager.validateMetadata(backupPath, encryptionKey);
        } catch (Exception e) {
            System.err.println("Failed to validate backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of available backups
     */
    public List<String> getAvailableBackups() {
        try {
            Path metadataPath = Paths.get(metadataFilePath);
            String vaultPath = metadataPath.getParent().toString();
            
            MetadataRecoveryManager recoveryManager = new MetadataRecoveryManager(vaultPath);
            return recoveryManager.getAvailableBackups();
        } catch (Exception e) {
            System.err.println("Failed to list backups: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Restore metadata from specific backup
     */
    public boolean restoreFromBackup(String backupFileName) {
        try {
            Path metadataPath = Paths.get(metadataFilePath);
            String vaultPath = metadataPath.getParent().toString();
            
            MetadataRecoveryManager recoveryManager = new MetadataRecoveryManager(vaultPath);
            
            if (recoveryManager.restoreMetadataFromBackup(backupFileName)) {
                // Reload metadata after restore
                loadMetadataPrimary();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to restore from backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get metadata file path
     */
    public String getMetadataFile() {
        return metadataFilePath;
    }
    
    /**
     * Check if metadata has been initialized (metadata file exists)
     */
    public boolean hasBeenInitialized() {
        Path metadataPath = Paths.get(metadataFilePath);
        return Files.exists(metadataPath) && Files.isRegularFile(metadataPath);
    }
    
    /**
     * Verify metadata integrity
     */
    public boolean verifyMetadataIntegrity() {
        try {
            loadMetadata();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get metadata statistics
     */
    public MetadataStats getMetadataStats() {
        return new MetadataStats(
            fileRegistry.size(),
            fileRegistry.values().stream().mapToLong(VaultFile::getSize).sum(),
            fileRegistry.values().stream().map(VaultFile::getExtension).distinct().count()
        );
    }
    
    /**
     * Metadata statistics data class
     */
    public static class MetadataStats {
        private final int fileCount;
        private final long totalSize;
        private final long uniqueExtensions;
        
        public MetadataStats(int fileCount, long totalSize, long uniqueExtensions) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.uniqueExtensions = uniqueExtensions;
        }
        
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public long getUniqueExtensions() { return uniqueExtensions; }
        
        @Override
        public String toString() {
            return String.format("MetadataStats{files=%d, size=%s, extensions=%d}", 
                fileCount, FileUtils.formatFileSize(totalSize), uniqueExtensions);
        }
    }
    
    /**
     * Clean up resources and clear sensitive data
     */
    public void cleanup() {
        fileRegistry.clear();
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        encryptionKey = null;
    }
}