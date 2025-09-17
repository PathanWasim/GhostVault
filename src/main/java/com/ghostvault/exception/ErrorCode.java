package com.ghostvault.exception;

/**
 * Error codes for GhostVault exceptions
 * Provides standardized error classification
 */
public enum ErrorCode {
    
    // Authentication errors (1000-1999)
    INVALID_PASSWORD(1001, "Invalid password provided"),
    AUTHENTICATION_FAILED(1002, "Authentication failed"),
    SESSION_EXPIRED(1003, "Session has expired"),
    
    // Cryptographic errors (2000-2999)
    ENCRYPTION_FAILED(2001, "Encryption operation failed"),
    DECRYPTION_FAILED(2002, "Decryption operation failed"),
    KEY_GENERATION_FAILED(2003, "Key generation failed"),
    
    // File system errors (3000-3999)
    FILE_NOT_FOUND(3001, "File not found"),
    FILE_ACCESS_DENIED(3002, "File access denied"),
    FILE_CORRUPTION(3003, "File corruption detected"),
    
    // Vault errors (4000-4999)
    VAULT_LOCKED(4001, "Vault is locked"),
    VAULT_CORRUPTED(4002, "Vault data corrupted"),
    VAULT_INITIALIZATION_FAILED(4003, "Vault initialization failed"),
    
    // Security errors (5000-5999)
    SECURITY_VIOLATION(5001, "Security violation detected"),
    THREAT_DETECTED(5002, "Security threat detected"),
    PANIC_MODE_ACTIVATED(5003, "Panic mode activated"),
    
    // General errors (9000-9999)
    UNKNOWN_ERROR(9001, "Unknown error occurred"),
    INTERNAL_ERROR(9002, "Internal system error"),
    CONFIGURATION_ERROR(9003, "Configuration error");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("[%d] %s", code, message);
    }
}