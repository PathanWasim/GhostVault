package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import com.ghostvault.logging.PersistenceLogger;
import javax.crypto.SecretKey;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple FileManager stub for compilation
 */
public class FileManager {
    private SecretKey encryptionKey;
    private java.util.Map<String, byte[]> fileStorage = new java.util.HashMap<>();
    private String vaultPath;
    private PersistentStorageManager storageManager;
    
    public FileManager() {
        this.vaultPath = System.getProperty("user.home") + "/.ghostvault";
        this.storageManager = new PersistentStorageManager(vaultPath);
        initializeStorage();
    }
    
    public FileManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.storageManager = new PersistentStorageManager(vaultPath);
        initializeStorage();
    }
    
    /**
     * Initialize storage and ensure vault structure exists
     */
    private void initializeStorage() {
        System.out.println("🏗️ Initializing FileManager storage...");
        
        if (!storageManager.initializeVaultStructure()) {
            System.err.println("❌ Failed to initialize vault structure");
            return;
        }
        
        StorageVerification verification = storageManager.verifyStorageIntegrity();
        if (!verification.isStorageHealthy()) {
            System.err.println("⚠️ Storage verification failed, attempting recovery...");
            if (!storageManager.recoverVaultStructure()) {
                System.err.println("❌ Storage recovery failed");
            }
        }
        
        System.out.println("✅ FileManager storage initialized");
        
        // Debug: Check for existing files in storage
        try {
            java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
            if (java.nio.file.Files.exists(filesDir)) {
                long fileCount = java.nio.file.Files.list(filesDir).count();
                System.out.println("📁 Found " + fileCount + " existing files in storage");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error checking existing files: " + e.getMessage());
        }
    }
    
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    public VaultFile storeFile(File file) throws Exception {
        // Read the actual file content
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
        
        // Determine MIME type
        String mimeType = determineMimeType(file.getName());
        
        VaultFile vaultFile = new VaultFile(file.getName(), file.length(), mimeType);
        
        // Store file content both in memory AND on disk for persistence
        fileStorage.put(vaultFile.getFileId(), fileContent);
        
        // Also save to disk for persistence across sessions
        try {
            saveFileToDisk(vaultFile.getFileId(), fileContent);
            PersistenceLogger.logFileSave(file.getName(), vaultFile.getFileId(), fileContent.length, true, null);
        } catch (Exception e) {
            PersistenceLogger.logFileSave(file.getName(), vaultFile.getFileId(), fileContent.length, false, e.getMessage());
            throw e;
        }
        
        System.out.println("📁 Stored file: " + file.getName() + " (" + fileContent.length + " bytes)");
        return vaultFile;
    }
    
    private void saveFileToDisk(String fileId, byte[] content) throws Exception {
        // Use files subdirectory for better organization
        java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
        java.nio.file.Files.createDirectories(filesDir);
        
        java.nio.file.Path filePath = filesDir.resolve(fileId + ".dat");
        java.nio.file.Files.write(filePath, content);
        
        // Verify file was saved successfully
        if (!java.nio.file.Files.exists(filePath)) {
            throw new Exception("File save verification failed: " + fileId);
        }
        
        long savedSize = java.nio.file.Files.size(filePath);
        if (savedSize != content.length) {
            throw new Exception("File size mismatch after save: expected " + content.length + ", got " + savedSize);
        }
        
        System.out.println("💾 File saved to disk: " + fileId + " (" + savedSize + " bytes)");
    }
    
    private byte[] loadFileFromDisk(String fileId) throws Exception {
        // Load from files subdirectory
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, "files", fileId + ".dat");
        if (java.nio.file.Files.exists(filePath)) {
            byte[] content = java.nio.file.Files.readAllBytes(filePath);
            System.out.println("📁 Loaded file from disk: " + fileId + " (" + content.length + " bytes)");
            return content;
        }
        return null;
    }
    
    public byte[] retrieveFile(VaultFile vaultFile) throws Exception {
        // First try memory cache
        byte[] content = fileStorage.get(vaultFile.getFileId());
        if (content != null) {
            System.out.println("📁 Retrieved file from memory: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), content.length, true, "memory");
            return content;
        }
        
        // Then try disk storage (for persistence across sessions)
        content = loadFileFromDisk(vaultFile.getFileId());
        if (content != null) {
            // Cache in memory for faster access
            fileStorage.put(vaultFile.getFileId(), content);
            System.out.println("📁 Retrieved file from disk: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), content.length, true, "disk");
            return content;
        }
        
        PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), 0, false, "not_found");
        throw new Exception("File not found: " + vaultFile.getFileName());
    }
    
    private String determineMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "txt": return "text/plain";
            case "java": return "text/x-java-source";
            case "py": return "text/x-python";
            case "js": return "text/javascript";
            case "html": return "text/html";
            case "css": return "text/css";
            case "xml": return "text/xml";
            case "json": return "application/json";
            case "mp4": return "video/mp4";
            case "avi": return "video/avi";
            case "mov": return "video/quicktime";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "ogg": return "audio/ogg";
            default: return "application/octet-stream";
        }
    }
    
    public void deleteFile(VaultFile vaultFile) throws Exception {
        // Remove from memory cache
        fileStorage.remove(vaultFile.getFileId());
        
        // Remove from disk storage (files subdirectory)
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, "files", vaultFile.getFileId() + ".dat");
        if (java.nio.file.Files.exists(filePath)) {
            java.nio.file.Files.delete(filePath);
            System.out.println("🗑️ Deleted file: " + vaultFile.getFileName());
        }
    }
    
    /**
     * Get storage verification status
     */
    public StorageVerification getStorageVerification() {
        return storageManager.verifyStorageIntegrity();
    }
    
    /**
     * Check if storage is properly initialized
     */
    public boolean isStorageInitialized() {
        return storageManager.isVaultInitialized();
    }
    
    public List<VaultFile> getAllFiles() {
        return new ArrayList<>();
    }
}