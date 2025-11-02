package com.ghostvault.ai;

/**
 * File size categories for AI analysis
 */
public enum SizeCategory {
    SMALL(0, 1024 * 1024, "Small", "< 1MB"),           // < 1MB
    MEDIUM(1024 * 1024, 50 * 1024 * 1024, "Medium", "1MB - 50MB"),  // 1MB - 50MB
    LARGE(50 * 1024 * 1024, Long.MAX_VALUE, "Large", "> 50MB"); // > 50MB
    
    private final long minSize;
    private final long maxSize;
    private final String displayName;
    private final String description;
    
    SizeCategory(long minSize, long maxSize, String displayName, String description) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.displayName = displayName;
        this.description = description;
    }
    
    public long getMinSize() {
        return minSize;
    }
    
    public long getMaxSize() {
        return maxSize;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
}