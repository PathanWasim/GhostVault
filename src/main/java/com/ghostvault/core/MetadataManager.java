package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple MetadataManager stub for compilation
 */
public class MetadataManager {
    private SecretKey encryptionKey;
    private List<VaultFile> files = new ArrayList<>();
    private boolean initialized = false;
    private String metadataPath;
    private PersistentStorageManager storageManager;
    
    public MetadataManager() {
        String vaultPath = System.getProperty("user.home") + "/.ghostvault";
        this.metadataPath = vaultPath + "/metadata/metadata.json";
        this.storageManager = new PersistentStorageManager(vaultPath);
        initializeMetadata();
    }
    
    public MetadataManager(String metadataPath) {
        this.metadataPath = metadataPath;
        // Extract vault path from metadata path
        String vaultPath = metadataPath.substring(0, metadataPath.lastIndexOf("/"));
        this.storageManager = new PersistentStorageManager(vaultPath);
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
    
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
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
        // Load metadata from disk if it exists
        loadMetadataFromDisk();
        initialized = true;
    }
    
    private void saveMetadata() throws Exception {
        try {
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
            
            System.out.println("💾 Metadata saved and verified: " + files.size() + " files (" + savedSize + " bytes)");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to save metadata: " + e.getMessage());
            throw e; // Re-throw to notify caller of failure
        }
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
}