package com.ghostvault.core;

/**
 * Result of a restore operation
 */
public class RestoreResult {
    
    private final boolean success;
    private final String errorMessage;
    private final RestoreStats stats;
    
    public RestoreResult(boolean success, String errorMessage, RestoreStats stats) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.stats = stats;
    }
    
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public RestoreStats getStats() { return stats; }
    
    @Override
    public String toString() {
        return String.format("RestoreResult{success=%s, error='%s', stats=%s}", 
            success, errorMessage, stats);
    }
}

/**
 * Statistics for restore operations
 */
class RestoreStats {
    
    private int filesRestored = 0;
    private int failedFiles = 0;
    private int skippedFiles = 0;
    private long bytesRestored = 0;
    private long restoreDuration = 0;
    private long backupTimestamp = 0;
    private boolean integrityVerified = false;
    private boolean metadataRestored = false;
    private boolean metadataSkipped = false;
    private boolean configurationRestored = false;
    
    public void incrementFilesRestored() { filesRestored++; }
    public void incrementFailedFiles() { failedFiles++; }
    public void incrementSkippedFiles() { skippedFiles++; }
    public void addBytesRestored(long bytes) { bytesRestored += bytes; }
    public void setRestoreDuration(long duration) { this.restoreDuration = duration; }
    public void setBackupTimestamp(long timestamp) { this.backupTimestamp = timestamp; }
    public void setIntegrityVerified(boolean verified) { this.integrityVerified = verified; }
    public void setMetadataRestored(boolean restored) { this.metadataRestored = restored; }
    public void setMetadataSkipped(boolean skipped) { this.metadataSkipped = skipped; }
    public void setConfigurationRestored(boolean restored) { this.configurationRestored = restored; }
    
    public int getFilesRestored() { return filesRestored; }
    public int getFailedFiles() { return failedFiles; }
    public int getSkippedFiles() { return skippedFiles; }
    public long getBytesRestored() { return bytesRestored; }
    public long getRestoreDuration() { return restoreDuration; }
    public long getBackupTimestamp() { return backupTimestamp; }
    public boolean isIntegrityVerified() { return integrityVerified; }
    public boolean isMetadataRestored() { return metadataRestored; }
    public boolean isMetadataSkipped() { return metadataSkipped; }
    public boolean isConfigurationRestored() { return configurationRestored; }
    
    public int getTotalFiles() { return filesRestored + failedFiles + skippedFiles; }
    
    @Override
    public String toString() {
        return String.format("RestoreStats{restored=%d, failed=%d, skipped=%d, bytes=%d, duration=%dms, integrity=%s}", 
            filesRestored, failedFiles, skippedFiles, bytesRestored, restoreDuration, integrityVerified);
    }
}