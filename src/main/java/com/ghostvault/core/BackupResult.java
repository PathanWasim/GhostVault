package com.ghostvault.core;

/**
 * Result of a backup operation
 */
public class BackupResult {
    
    private final boolean success;
    private final String errorMessage;
    private final BackupStats stats;
    
    public BackupResult(boolean success, String errorMessage, BackupStats stats) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.stats = stats;
    }
    
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public BackupStats getStats() { return stats; }
    
    @Override
    public String toString() {
        return String.format("BackupResult{success=%s, error='%s', stats=%s}", 
            success, errorMessage, stats);
    }
}

/**
 * Statistics for backup operations
 */
class BackupStats {
    
    private int filesBackedUp = 0;
    private int failedFiles = 0;
    private long bytesBackedUp = 0;
    private long backupDuration = 0;
    private long finalBackupSize = 0;
    
    public void incrementFilesBackedUp() { filesBackedUp++; }
    public void incrementFailedFiles() { failedFiles++; }
    public void addBytesBackedUp(long bytes) { bytesBackedUp += bytes; }
    public void setBackupDuration(long duration) { this.backupDuration = duration; }
    public void setFinalBackupSize(long size) { this.finalBackupSize = size; }
    
    public int getFilesBackedUp() { return filesBackedUp; }
    public int getFailedFiles() { return failedFiles; }
    public long getBytesBackedUp() { return bytesBackedUp; }
    public long getBackupDuration() { return backupDuration; }
    public long getFinalBackupSize() { return finalBackupSize; }
    
    public double getCompressionRatio() {
        return bytesBackedUp > 0 ? (double) finalBackupSize / bytesBackedUp : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("BackupStats{files=%d, failed=%d, bytes=%d, duration=%dms, size=%d, ratio=%.2f}", 
            filesBackedUp, failedFiles, bytesBackedUp, backupDuration, finalBackupSize, getCompressionRatio());
    }
}