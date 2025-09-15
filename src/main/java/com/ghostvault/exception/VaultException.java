package com.ghostvault.exception;

/**
 * Exception for vault-specific operations failures
 */
public class VaultException extends GhostVaultException {
    
    public VaultException(ErrorCode errorCode, String message) {
        super(errorCode, ErrorSeverity.HIGH, true, message, null, null);
    }
    
    public VaultException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, ErrorSeverity.HIGH, true, message, null, cause);
    }
    
    public VaultException(ErrorCode errorCode, ErrorSeverity severity, String message) {
        super(errorCode, severity, true, message, null, null);
    }
    
    public VaultException(ErrorCode errorCode, ErrorSeverity severity, boolean recoverable, 
                         String message, String technicalDetails) {
        super(errorCode, severity, recoverable, message, technicalDetails, null);
    }
    
    /**
     * Create exception for vault not initialized
     */
    public static VaultException notInitialized() {
        return new VaultException(ErrorCode.VAULT_NOT_INITIALIZED, 
            "Vault has not been initialized. Please run initial setup first.");
    }
    
    /**
     * Create exception for corrupted vault
     */
    public static VaultException corrupted(String details) {
        return new VaultException(ErrorCode.VAULT_CORRUPTED, ErrorSeverity.CRITICAL, false,
            "Vault data is corrupted and cannot be accessed", details);
    }
    
    /**
     * Create exception for locked vault
     */
    public static VaultException locked(String reason) {
        return new VaultException(ErrorCode.VAULT_LOCKED, 
            "Vault is currently locked: " + reason);
    }
    
    /**
     * Create exception for metadata corruption
     */
    public static VaultException metadataCorrupted(String details) {
        return new VaultException(ErrorCode.METADATA_CORRUPTED, ErrorSeverity.HIGH, true,
            "Vault metadata is corrupted", details);
    }
    
    /**
     * Create exception for integrity check failure
     */
    public static VaultException integrityCheckFailed(String fileName, String details) {
        return new VaultException(ErrorCode.INTEGRITY_CHECK_FAILED, ErrorSeverity.HIGH, false,
            "File integrity check failed for: " + fileName, details);
    }
}