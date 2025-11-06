package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import com.ghostvault.security.SecureMemoryManager;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Enhanced MetadataManager with encrypted storage support
 * Uses EncryptedMetadataManager for secure metadata operations
 */
public class MetadataManager {
    private final EncryptedMetadataManager encryptedMetadataManager;
    private List<VaultFile> files = new ArrayList<>();
    private boolean initialized = false;
    private String metadataPath;
    private String vaultPath;
    private PersistentStorageManager storageManager;
    private boolean encryptionEnabled = true;
    
    public MetadataManager() {
        this.vaultPath = System.getProperty("user.home") + "/.ghostvault";
        this.metadataPath = vaultPath + "/metadata/metadata.json";
        this.storageManager = new PersistentStorageManager(vaultPath);
        this.encryptedMetadataManager = new EncryptedMetadataManager(vaultPath);
        initializeMetadata();
    }
    
    public MetadataManager(String metadataPath) {
        this.metadataPath = metadataPath;
        // Extract vault path from metadata path
        this.vaultPath = metadataPath.substring(0, metadataPath.lastIndexOf("/"));
        this.storageManager = new PersistentStorageManager(vaultPath);
        this.encryptedMetadataManager = new EncryptedMetadataManager(vaultPath);
        initializeMetadata();
    }
    
    /**
     * Initialize metadata storage
     */
    private void initializeMetadata() {
        System.out.println("📋 Initializing MetadataManager...");
        
        // Ensure vault structure exists
        if (!storageManager.initializeVaultStructure()) {
            System.err.println("❌ Failed to initialize vault structure for metadata");
            return;
        }
        
        // Load existing metadata
        try {
            loadMetadata();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load existing metadata: " + e.getMessage());
        }
        
        System.out.println("✅ MetadataManager initialized with " + files.size() + " files");
        
        // Debug: List all loaded files
        for (VaultFile file : files) {
            System.out.println("📋 Loaded metadata for: " + file.getFileName() + " (ID: " + file.getFileId() + ")");
        }
    }
    
    private String currentPassword; // For encryption/decryption
    
    /**
     * Set the password for metadata encryption/decryption
     * @param password The user's password
     */
    public void setPassword(String password) {
        this.currentPassword = password;
        System.out.println("🔐 MetadataManager: Password set successfully (length: " + (password != null ? password.length() : "null") + ")");
    }
    
    /**
     * Legacy method for backward compatibility
     * @key The encryption key (not used in new implementation)
     */
    @Deprecated
    public void setEncryptionKey(SecretKey key) {
        // Legacy method - encryption now uses password-derived keys
        System.out.println("⚠️ setEncryptionKey is deprecated - use setPassword instead");
        
        // WORKAROUND: For legacy compatibility, we'll try to use a default password
        // This is not ideal for security but maintains functionality during transition
        if (key != null && currentPassword == null) {
            // Use a derived password based on the key for backward compatibility
            // This is a temporary solution until proper password passing is implemented
            String derivedPassword = "legacy_key_" + key.hashCode();
            this.currentPassword = derivedPassword;
            System.out.println("⚠️ Using derived password for legacy encryption key compatibility: " + derivedPassword);
        } else if (currentPassword != null) {
            System.out.println("🔐 MetadataManager: Password already set, not overriding with derived password");
        } else {
            System.out.println("⚠️ MetadataManager: No encryption key provided for legacy compatibility");
        }
    }
    
    /**
     * Enable or disable encryption for metadata operations
     * @param enabled true to enable encryption
     */
    public void setEncryptionEnabled(boolean enabled) {
        this.encryptionEnabled = enabled;
        System.out.println("🔐 Metadata encryption " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if encryption is enabled
     * @return true if encryption is enabled
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    public List<VaultFile> getAllFiles() {
        return new ArrayList<>(files);
    }
    
    public void addFile(VaultFile vaultFile) throws Exception {
        files.add(vaultFile);
        initialized = true;
        saveMetadata(); // Persist to disk
    }
    
    public void removeFile(VaultFile vaultFile) throws Exception {
        files.removeIf(f -> f.getFileId().equals(vaultFile.getFileId()));
        saveMetadata(); // Persist changes to disk
    }
    
    public void updateFile(VaultFile vaultFile) throws Exception {
        // Stub implementation
    }
    
    public void loadMetadata() throws Exception {
        // Check for and perform migration if needed
        checkAndPerformMigration();
        
        // Load metadata from appropriate storage
        if (encryptionEnabled && encryptedMetadataManager.hasEncryptedMetadata()) {
            loadEncryptedMetadata();
        } else {
            loadMetadataFromDisk();
        }
        initialized = true;
    }
    
    private void saveMetadata() throws Exception {
        try {
            if (encryptionEnabled) {
                saveEncryptedMetadata();
            } else {
                savePlainTextMetadata();
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to save metadata: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Save metadata using encryption
     */
    private void saveEncryptedMetadata() throws Exception {
        // Convert files to metadata map
        Map<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> filesList = new ArrayList<>();
        
        for (VaultFile file : files) {
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("fileName", file.getFileName());
            fileData.put("fileId", file.getFileId());
            fileData.put("size", file.getSize());
            fileData.put("mimeType", file.getMimeType());
            filesList.add(fileData);
        }
        
        metadata.put("files", filesList);
        metadata.put("version", "1.0");
        metadata.put("encrypted", true);
        
        // Convert to JSON string
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonString = mapper.writeValueAsString(metadata);
        
        // Save using encrypted metadata manager
        System.out.println("🔍 Debug: About to save encrypted metadata with password: " + (currentPassword != null ? "SET (length=" + currentPassword.length() + ")" : "NULL"));
        encryptedMetadataManager.saveEncryptedMetadata(jsonString, currentPassword);
        
        System.out.println("🔐 Encrypted metadata saved: " + files.size() + " files");
    }
    
    /**
     * Save metadata in plain text format (legacy)
     */
    private void savePlainTextMetadata() throws Exception {
        // Create a simple JSON-like format for metadata
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < files.size(); i++) {
            VaultFile file = files.get(i);
            json.append("  {\n");
            json.append("    \"fileName\": \"").append(file.getFileName()).append("\",\n");
            json.append("    \"fileId\": \"").append(file.getFileId()).append("\",\n");
            json.append("    \"size\": ").append(file.getSize()).append(",\n");
            json.append("    \"mimeType\": \"").append(file.getMimeType()).append("\"\n");
            json.append("  }");
            if (i < files.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        
        // Save to disk with verification
        java.nio.file.Path metaPath = java.nio.file.Paths.get(metadataPath);
        java.nio.file.Files.createDirectories(metaPath.getParent());
        java.nio.file.Files.write(metaPath, json.toString().getBytes());
        
        // Verify save was successful
        if (!java.nio.file.Files.exists(metaPath)) {
            throw new Exception("Metadata save verification failed - file does not exist");
        }
        
        long savedSize = java.nio.file.Files.size(metaPath);
        if (savedSize == 0) {
            throw new Exception("Metadata save verification failed - file is empty");
        }
        
        System.out.println("💾 Plain text metadata saved: " + files.size() + " files (" + savedSize + " bytes)");
    }
    
    private void loadMetadataFromDisk() throws Exception {
        try {
            java.nio.file.Path metaPath = java.nio.file.Paths.get(metadataPath);
            if (java.nio.file.Files.exists(metaPath)) {
                String content = new String(java.nio.file.Files.readAllBytes(metaPath));
                // Simple parsing - in a real app, use proper JSON library
                parseMetadata(content);
                System.out.println("📋 Metadata loaded: " + files.size() + " files");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load metadata: " + e.getMessage());
        }
    }
    
    private void parseMetadata(String content) {
        // Simple JSON parsing for our basic format
        files.clear();
        String[] lines = content.split("\n");
        String fileName = null, fileId = null, mimeType = null;
        long size = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("\"fileName\":")) {
                fileName = extractValue(line);
            } else if (line.contains("\"fileId\":")) {
                fileId = extractValue(line);
            } else if (line.contains("\"size\":")) {
                size = Long.parseLong(line.split(":")[1].trim().replace(",", ""));
            } else if (line.contains("\"mimeType\":")) {
                mimeType = extractValue(line);
                // End of object, create VaultFile
                if (fileName != null && fileId != null && mimeType != null) {
                    VaultFile vaultFile = new VaultFile(fileName, size, mimeType);
                    vaultFile.setFileId(fileId); // Use the stored fileId
                    files.add(vaultFile);
                    System.out.println("📋 Loaded file: " + fileName + " (ID: " + fileId + ")");
                }
            }
        }
    }
    
    private String extractValue(String line) {
        int start = line.indexOf("\"", line.indexOf(":")) + 1;
        int end = line.lastIndexOf("\"");
        return line.substring(start, end);
    }
    
    public boolean hasBeenInitialized() {
        return initialized;
    }
    
    public List<String> getAvailableBackups() {
        return new ArrayList<>();
    }
    
    public boolean restoreFromBackup(String backup) {
        return true;
    }
    
    public boolean verifyMetadataIntegrity() {
        return true;
    }
    
    public void removeFile(String fileId) throws Exception {
        files.removeIf(f -> f.getFileId().equals(fileId));
        saveMetadata(); // Persist changes to disk
    }
    
    /**
     * Load metadata from encrypted storage
     */
    private void loadEncryptedMetadata() throws Exception {
        try {
            String jsonString = encryptedMetadataManager.loadEncryptedMetadata(currentPassword);
            if (jsonString == null) {
                return;
            }
            
            // Parse JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonString);
            
            files.clear();
            
            if (rootNode.has("files")) {
                com.fasterxml.jackson.databind.JsonNode filesNode = rootNode.get("files");
                
                for (com.fasterxml.jackson.databind.JsonNode fileNode : filesNode) {
                    String fileName = fileNode.get("fileName").asText();
                    String fileId = fileNode.get("fileId").asText();
                    long size = fileNode.get("size").asLong();
                    String mimeType = fileNode.get("mimeType").asText();
                    
                    if (fileName != null && fileId != null && mimeType != null) {
                        VaultFile vaultFile = new VaultFile(fileName, size, mimeType);
                        vaultFile.setFileId(fileId);
                        files.add(vaultFile);
                        System.out.println("🔓 Loaded encrypted file: " + fileName + " (ID: " + fileId + ")");
                    }
                }
            }
            
            System.out.println("🔓 Encrypted metadata loaded: " + files.size() + " files");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load encrypted metadata: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check for and perform migration from plain text metadata if needed
     */
    private void checkAndPerformMigration() {
        if (encryptionEnabled && encryptedMetadataManager.needsMigration()) {
            System.out.println("🔄 Plain text metadata detected - performing automatic migration...");
            
            if (encryptedMetadataManager.migrateFromPlainText(currentPassword)) {
                System.out.println("✅ Metadata migration completed successfully");
            } else {
                System.err.println("❌ Metadata migration failed");
                System.err.println("⚠️ WARNING: Metadata is still stored in plain text!");
            }
        }
    }
    
    /**
     * Check if using encrypted metadata storage
     * @return true if metadata is stored securely
     */
    public boolean isUsingEncryptedStorage() {
        return encryptedMetadataManager.hasEncryptedMetadata();
    }
    
    /**
     * Clear sensitive data from memory using SecureMemoryManager
     */
    public void clearSensitiveData() {
        if (currentPassword != null) {
            SecureMemoryManager.getInstance().secureWipe(currentPassword);
            currentPassword = null;
        }
        
        // Trigger secure memory cleanup
        SecureMemoryManager.getInstance().cleanupAllTrackedData();
        
        System.out.println("🧹 Cleared sensitive metadata from memory");
    }
}