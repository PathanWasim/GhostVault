package com.ghostvault.core;

/**
 * Result of backup verification operation
 */
public class BackupVerificationResult {
    
    private final boolean valid;
    private final String errorMessage;
    private final BackupManifest manifest;
    private final RestoreStats verificationStats;
    
    public BackupVerificationResult(boolean valid, String errorMessage, 
                                  BackupManifest manifest, RestoreStats verificationStats) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.manifest = manifest;
        this.verificationStats = verificationStats;
    }
    
    public boolean isValid() { return valid; }
    public String getErrorMessage() { return errorMessage; }
    public BackupManifest getManifest() { return manifest; }
    public RestoreStats getVerificationStats() { return verificationStats; }
    
    /**
     * Get backup information summary
     */
    public String getBackupInfo() {
        if (manifest == null) {
            return "No backup information available";
        }
        
        return String.format("Backup created: %s, Files: %d, Size: %s", 
            manifest.getBackupTimestamp(),
            manifest.getFileCount(),
            com.ghostvault.util.FileUtils.formatFileSize(manifest.getTotalSize()));
    }
    
    @Override
    public String toString() {
        return String.format("BackupVerificationResult{valid=%s, error='%s', manifest=%s}", 
            valid, errorMessage, manifest);
    }
}