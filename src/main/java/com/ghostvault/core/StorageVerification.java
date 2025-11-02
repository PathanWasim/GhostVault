package com.ghostvault.core;

import java.time.LocalDateTime;

/**
 * Model for storage verification results
 */
public class StorageVerification {
    private boolean vaultDirectoryExists;
    private boolean vaultDirectoryWritable;
    private boolean filesDirectoryExists;
    private boolean metadataExists;
    private long metadataSize;
    private int storedFileCount;
    private LocalDateTime lastVerified;
    private String verificationError;
    
    public StorageVerification() {
        this.lastVerified = LocalDateTime.now();
    }
    
    public boolean isVaultDirectoryExists() {
        return vaultDirectoryExists;
    }
    
    public void setVaultDirectoryExists(boolean vaultDirectoryExists) {
        this.vaultDirectoryExists = vaultDirectoryExists;
    }
    
    public boolean isVaultDirectoryWritable() {
        return vaultDirectoryWritable;
    }
    
    public void setVaultDirectoryWritable(boolean vaultDirectoryWritable) {
        this.vaultDirectoryWritable = vaultDirectoryWritable;
    }
    
    public boolean isFilesDirectoryExists() {
        return filesDirectoryExists;
    }
    
    public void setFilesDirectoryExists(boolean filesDirectoryExists) {
        this.filesDirectoryExists = filesDirectoryExists;
    }
    
    public boolean isMetadataExists() {
        return metadataExists;
    }
    
    public void setMetadataExists(boolean metadataExists) {
        this.metadataExists = metadataExists;
    }
    
    public long getMetadataSize() {
        return metadataSize;
    }
    
    public void setMetadataSize(long metadataSize) {
        this.metadataSize = metadataSize;
    }
    
    public int getStoredFileCount() {
        return storedFileCount;
    }
    
    public void setStoredFileCount(int storedFileCount) {
        this.storedFileCount = storedFileCount;
    }
    
    public LocalDateTime getLastVerified() {
        return lastVerified;
    }
    
    public void setLastVerified(LocalDateTime lastVerified) {
        this.lastVerified = lastVerified;
    }
    
    public String getVerificationError() {
        return verificationError;
    }
    
    public void setVerificationError(String verificationError) {
        this.verificationError = verificationError;
    }
    
    /**
     * Check if storage is healthy (all critical components exist and are accessible)
     */
    public boolean isStorageHealthy() {
        return vaultDirectoryExists && vaultDirectoryWritable && filesDirectoryExists && verificationError == null;
    }
    
    @Override
    public String toString() {
        return "StorageVerification{" +
                "vaultDir=" + vaultDirectoryExists +
                ", writable=" + vaultDirectoryWritable +
                ", filesDir=" + filesDirectoryExists +
                ", metadata=" + metadataExists +
                ", fileCount=" + storedFileCount +
                ", error='" + verificationError + '\'' +
                '}';
    }
}