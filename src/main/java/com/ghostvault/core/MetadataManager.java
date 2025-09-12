package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.CryptoManager;
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
 * Manages encrypted metadata for vault files
 */
public class MetadataManager {
    
    private final String metadataFilePath;
    private final Map<String, VaultFile> fileRegistry;
    private CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    public MetadataManager(String metadataFilePath) {
        this.metadataFilePath = metadataFilePath;
        this.fileRegistry = new ConcurrentHashMap<>();
        this.cryptoManager = new CryptoManager();
    }
    
    /**
     * Set encryption key for metadata operations
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
        // Initialize crypto manager with the key
        try {
            // We need to create a temporary crypto manager instance
            // In a real implementation, we'd refactor this
        } catch (Exception e) {
            throw new RuntimeException("Failed to set encryption key", e);
        }
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
     * Get file by ID
     */
    public VaultFile getFile(String fileId) {
        return fileRegistry.get(fileId);
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
     * Save encrypted metadata to file
     */
    private void saveMetadata() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        // Serialize metadata to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new HashMap<>(fileRegistry));
        }
        
        // Encrypt metadata
        CryptoManager.EncryptedData encrypted = encryptData(baos.toByteArray());
        
        // Write to file
        FileUtils.writeEncryptedFile(Paths.get(metadataFilePath), encrypted);
    }
    
    /**
     * Load encrypted metadata from file
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
        
        try {
            // Read encrypted data
            CryptoManager.EncryptedData encrypted = FileUtils.readEncryptedFile(metadataPath);
            
            // Decrypt metadata
            byte[] decryptedData = decryptData(encrypted);
            
            // Deserialize metadata
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                @SuppressWarnings("unchecked")
                Map<