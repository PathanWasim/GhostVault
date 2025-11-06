package com.ghostvault.core;

/**
 * Statistics for restore operation
 */
public class RestoreStats {
    private final int filesRestored;
    private final long totalSize;
    private final long duration;
    
    public RestoreStats(int filesRestored, long totalSize, long duration) {
        this.filesRestored = filesRestored;
        this.totalSize = totalSize;
        this.duration = duration;
    }
    
    public int getFilesRestored() {
        return filesRestored;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public long getDuration() {
        return duration;
    }
}