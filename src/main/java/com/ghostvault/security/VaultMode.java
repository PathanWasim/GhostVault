package com.ghostvault.security;

/**
 * Enumeration of vault operation modes
 */
public enum VaultMode {
    MASTER("Master Mode", "Full access to all vault features"),
    PANIC("Panic Mode", "Emergency mode with data protection"),
    DECOY("Decoy Mode", "Decoy vault to hide real data");
    
    private final String displayName;
    private final String description;
    
    VaultMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}