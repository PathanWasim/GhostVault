package com.ghostvault.core;

/**
 * Result of restore operation
 */
public class RestoreResult {
    private final boolean success;
    private final String message;
    private final RestoreStats stats;
    
    public RestoreResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.stats = null;
    }
    
    public RestoreResult(boolean success, String message, RestoreStats stats) {
        this.success = success;
        this.message = message;
        this.stats = stats;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public RestoreStats getStats() {
        return stats;
    }
}