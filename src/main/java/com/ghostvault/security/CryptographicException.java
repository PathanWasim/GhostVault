package com.ghostvault.security;

/**
 * Exception thrown when cryptographic operations fail
 */
public class CryptographicException extends GhostVaultSecurityException {
    
    public CryptographicException(String message) {
        super(message);
    }
    
    public CryptographicException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CryptographicException(Throwable cause) {
        super(cause);
    }
}