package com.ghostvault.security;

/**
 * Enumeration of vault access modes
 */
public enum VaultMode {
    /**
     * Master mode - Full access to real vault with all data
     */
    MASTER,
    
    /**
     * Decoy mode - Shows fake vault to hide real data under coercion
     */
    DECOY,
    
    /**
     * Panic mode - Emergency system wipe, destroys all data
     */
    PANIC
}