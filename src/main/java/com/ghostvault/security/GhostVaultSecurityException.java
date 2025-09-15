package com.ghostvault.security;

/**
 * Base exception for all GhostVault security-related errors
 */
public class GhostVaultSecurityException extends Exception {
    
    public GhostVaultSecurityException(String message) {
        super(message);
    }
    
    public GhostVaultSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public GhostVaultSecurityException(Throwable cause) {
        super(cause);
    }
}