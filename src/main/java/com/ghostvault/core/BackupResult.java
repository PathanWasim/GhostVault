package com.ghostvault.core;

/**
 * Result of backup operation
 */
public class BackupResult {
    private final boolean success;
    private final String message;
    private final String backupFile;
    
    public BackupResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.backupFile = null;
    }
    
    public BackupResult(boolean success, String message, String backupFile) {
        this.success = success;
        this.message = message;
        this.backupFile = backupFile;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getBackupFile() {
        return backupFile;
    }
}