package com.ghostvault.exception;

/**
 * Exception for vault-specific operations failures
 */
public class VaultException extends GhostVaultException {
    
    public VaultException(String message) {
        super(message, ErrorCategory.FILE_SYSTEM, ErrorSeverity.HIGH, true);
    }
    
    public VaultException(String message, Throwable cause) {
        super(message, cause, ErrorCategory.FILE_SYSTEM, ErrorSeverity.HIGH);
    }
    
    public VaultException(String message, ErrorSeverity severity, boolean recoverable, String technicalDetails) {
        super(message, null, ErrorCategory.FILE_SYSTEM, severity, message, technicalDetails, recoverable);
    }
    
    /**
     * Create exception for vault not initialized
     */
    public static VaultException notInitialized() {
        return new VaultException("Vault has not been initialized. Please run initial setup first.");
    }
    
    /**
     * Create exception for corrupted vault
     */
    public static VaultException corrupted(String details) {
        return new VaultException("Vault data is corrupted and cannot be accessed", ErrorSeverity.CRITICAL, false, details);
    }
    
    /**
     * Create exception for locked vault
     */
    public static VaultException locked(String reason) {
        return new VaultException("Vault is currently locked: " + reason);
    }
    
    /**
     * Create exception for metadata corruption
     */
    public static VaultException metadataCorrupted(String details) {
        return new VaultException("Vault metadata is corrupted", ErrorSeverity.HIGH, true, details);
    }
    
    /**
     * Create exception for integrity check failure
     */
    public static VaultException integrityCheckFailed(String fileName, String details) {
        return new VaultException("File integrity check failed for: " + fileName, ErrorSeverity.HIGH, false, details);
    }
}