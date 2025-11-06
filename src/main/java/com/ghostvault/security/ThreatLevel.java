package com.ghostvault.security;

/**
 * Enumeration of threat severity levels
 */
public enum ThreatLevel {
    LOW("Low", 1),
    MEDIUM("Medium", 2), 
    HIGH("High", 3),
    CRITICAL("Critical", 4);
    
    private final String displayName;
    private final int severity;
    
    ThreatLevel(String displayName, int severity) {
        this.displayName = displayName;
        this.severity = severity;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}