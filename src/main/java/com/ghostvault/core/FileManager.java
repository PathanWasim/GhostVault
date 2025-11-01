package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
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
    
    public FileManager() {
        this.vaultPath = System.getProperty("user.home") + "/.ghostvault/files";
    }
    
    public FileManager(String vaultPath) {
        this.vaultPath = vaultPath;
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
        saveFileToDisk(vaultFile.getFileId(), fileContent);
        
        System.out.println("📁 Stored file: " + file.getName() + " (" + fileContent.length + " bytes)");
        return vaultFile;
    }
    
    private void saveFileToDisk(String fileId, byte[] content) throws Exception {
        java.nio.file.Path vaultDir = java.nio.file.Paths.get(vaultPath);
        java.nio.file.Files.createDirectories(vaultDir);
        
        java.nio.file.Path filePath = vaultDir.resolve(fileId + ".dat");
        java.nio.file.Files.write(filePath, content);
    }
    
    private byte[] loadFileFromDisk(String fileId) throws Exception {
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, fileId + ".dat");
        if (java.nio.file.Files.exists(filePath)) {
            return java.nio.file.Files.readAllBytes(filePath);
        }
        return null;
    }
    
    public byte[] retrieveFile(VaultFile vaultFile) throws Exception {
        // First try memory cache
        byte[] content = fileStorage.get(vaultFile.getFileId());
        if (content != null) {
            System.out.println("📁 Retrieved file from memory: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            return content;
        }
        
        // Then try disk storage (for persistence across sessions)
        content = loadFileFromDisk(vaultFile.getFileId());
        if (content != null) {
            // Cache in memory for faster access
            fileStorage.put(vaultFile.getFileId(), content);
            System.out.println("📁 Retrieved file from disk: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            return content;
        }
        
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
        
        // Remove from disk storage
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, vaultFile.getFileId() + ".dat");
        if (java.nio.file.Files.exists(filePath)) {
            java.nio.file.Files.delete(filePath);
            System.out.println("🗑️ Deleted file: " + vaultFile.getFileName());
        }
    }
    
    public List<VaultFile> getAllFiles() {
        return new ArrayList<>();
    }
}