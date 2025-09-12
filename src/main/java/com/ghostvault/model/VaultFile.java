package com.ghostvault.model;

import java.io.Serializable;

/**
 * Represents a file stored in the vault with its metadata
 */
public class VaultFile implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String originalName;
    private final String fileId;
    private final String encryptedName;
    private final long size;
    private final String hash;
    private final long uploadTime;
    private String tags;
    
    public VaultFile(String originalName, String fileId, String encryptedName, 
                     long size, String hash, long uploadTime) {
        this.originalName = originalName;
        this.fileId = fileId;
        this.encryptedName = encryptedName;
        this.size = size;
        this.hash = hash;
        this.uploadTime = uploadTime;
        this.tags = "";
    }
    
    // Getters
    public String getOriginalName() { return originalName; }
    public String getFileId() { return fileId; }
    public String getEncryptedName() { return encryptedName; }
    public long getSize() { return size; }
    public String getHash() { return hash; }
    public long getUploadTime() { return uploadTime; }
    public String getTags() { return tags; }
    
    // Setters
    public void setTags(String tags) { 
        this.tags = tags != null ? tags : ""; 
    }
    
    /**
     * Get file extension from original name
     */
    public String getExtension() {
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalName.length() - 1) {
            return originalName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Check if file matches search query
     */
    public boolean matchesSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        return originalName.toLowerCase().contains(lowerQuery) ||
               tags.toLowerCase().contains(lowerQuery);
    }
    
    /**
     * Get display name with size for UI
     */
    public String getDisplayName() {
        return originalName + " (" + formatFileSize(size) + ")";
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Get file icon based on extension
     */
    public String getIcon() {
        switch (getExtension()) {
            case "pdf": return "📄";
            case "doc": case "docx": return "📝";
            case "xls": case "xlsx": return "📊";
            case "ppt": case "pptx": return "📊";
            case "txt": case "md": return "📋";
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": return "🖼️";
            case "mp3": case "wav": case "flac": case "aac": return "🎵";
            case "mp4": case "avi": case "mkv": case "mov": return "🎬";
            case "zip": case "rar": case "7z": case "tar": case "gz": return "📦";
            case "exe": case "msi": case "dmg": return "⚙️";
            case "java": case "py": case "js": case "html": case "css": return "💻";
            default: return "📁";
        }
    }
    
    @Override
    public String toString() {
        return "VaultFile{" +
                "originalName='" + originalName + '\'' +
                ", fileId='" + fileId + '\'' +
                ", size=" + size +
                ", uploadTime=" + uploadTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VaultFile vaultFile = (VaultFile) obj;
        return fileId.equals(vaultFile.fileId);
    }
    
    @Override
    public int hashCode() {
        return fileId.hashCode();
    }
}