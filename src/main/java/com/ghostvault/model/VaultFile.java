package com.ghostvault.model;

import java.time.LocalDateTime;

/**
 * Simple VaultFile model for compilation
 */
public class VaultFile {
    private String fileName;
    private long size;
    private String mimeType;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String extension;
    
    public VaultFile(String fileName, long size, String mimeType) {
        this.fileName = fileName;
        this.size = size;
        this.mimeType = mimeType;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.extension = extractExtension(fileName);
    }
    
    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public String getOriginalName() { return fileName; }
    public String getDisplayName() { return fileName; }
    public String getIcon() { return "📄"; }
    public String getFileId() { return fileName + "_" + size; }
    public String getEncryptedName() { return fileName + ".enc"; }
    public String getHash() { return "hash_" + fileName.hashCode(); }
    public long getUploadTime() { return System.currentTimeMillis(); }
    public long getSize() { return size; }
    public String getMimeType() { return mimeType; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public String getExtension() { return extension; }
    
    // Setters
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setSize(long size) { this.size = size; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    public void setExtension(String extension) { this.extension = extension; }
    
    @Override
    public String toString() {
        return fileName;
    }
}