package com.ghostvault.exception;

/**
 * Base exception class for all GhostVault-specific exceptions
 * Provides a hierarchy for different types of errors with appropriate handling
 */
public class GhostVaultException extends Exception {
    
    private final ErrorCode errorCode;
    private final ErrorSeverity severity;
    private final boolean recoverable;
    private final String userMessage;
    private final String technicalDetails;
    
    public enum ErrorCode {
        // Authentication errors (1000-1099)
        INVALID_PASSWORD(1001, "Invalid password provided"),
        AUTHENTICATION_FAILED(1002, "Authentication failed"),
        SESSION_EXPIRED(1003, "Session has expired"),
        ACCESS_DENIED(1004, "Access denied"),
        
        // Cryptographic errors (1100-1199)
        ENCRYPTION_FAILED(1101, "Encryption operation failed"),
        DECRYPTION_FAILED(1102, "Decryption operation failed"),
        KEY_GENERATION_FAILED(1103, "Key generation failed"),
        INVALID_KEY(1104, "Invalid encryption key"),
        CRYPTO_ALGORITHM_ERROR(1105, "Cryptographic algorithm error"),
        
        // File system errors (1200-1299)
        FILE_NOT_FOUND(1201, "File not found"),
        FILE_ACCESS_DENIED(1202, "File access denied"),
        FILE_CORRUPTED(1203, "File is corrupted"),
        DISK_FULL(1204, "Insufficient disk space"),
        IO_ERROR(1205, "Input/output error"),
        FILE_LOCKED(1206, "File is locked by another process"),
        
        // Vault errors (1300-1399)
        VAULT_NOT_INITIALIZED(1301, "Vault is not initialized"),
        VAULT_CORRUPTED(1302, "Vault data is corrupted"),
        VAULT_LOCKED(1303, "Vault is locked"),
        METADATA_CORRUPTED(1304, "Vault metadata is corrupted"),
        INTEGRITY_CHECK_FAILED(1305, "File integrity check failed"),
        
        // Network errors (1400-1499)
        NETWORK_ERROR(1401, "Network communication error"),
        CONNECTION_TIMEOUT(1402, "Connection timeout"),
        INVALID_RESPONSE(1403, "Invalid server response"),
        
        // Configuration errors (1500-1599)
        INVALID_CONFIGURATION(1501, "Invalid configuration"),
        MISSING_CONFIGURATION(1502, "Missing required configuration"),
        CONFIGURATION_CORRUPTED(1503, "Configuration file corrupted"),
        
        // System errors (1600-1699)
        INSUFFICIENT_MEMORY(1601, "Insufficient memory"),
        SYSTEM_ERROR(1602, "System error occurred"),
        PERMISSION_DENIED(1603, "Permission denied"),
        RESOURCE_UNAVAILABLE(1604, "Required resource unavailable"),
        
        // Security errors (1700-1799)
        SECURITY_VIOLATION(1701, "Security policy violation"),
        TAMPERING_DETECTED(1702, "Data tampering detected"),
        INTRUSION_DETECTED(1703, "Intrusion attempt detected"),
        PANIC_MODE_TRIGGERED(1704, "Panic mode has been triggered"),
        
        // Backup/Restore errors (1800-1899)
        BACKUP_FAILED(1801, "Backup operation failed"),
        RESTORE_FAILED(1802, "Restore operation failed"),
        BACKUP_CORRUPTED(1803, "Backup file is corrupted"),
        BACKUP_INCOMPATIBLE(1804, "Backup format incompatible"),
        
        // Unknown/Generic errors (9000-9999)
        UNKNOWN_ERROR(9000, "Unknown error occurred"),
        INTERNAL_ERROR(9001, "Internal application error");
        
        private final int code;
        private final String defaultMessage;
        
        ErrorCode(int code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }
        
        public int getCode() { return code; }
        public String getDefaultMessage() { return defaultMessage; }
    }
    
    public enum ErrorSeverity {
        LOW,        // Minor issues, operation can continue
        MEDIUM,     // Significant issues, some functionality affected
        HIGH,       // Major issues, core functionality affected
        CRITICAL    // Severe issues, application security compromised
    }
    
    public GhostVaultException(ErrorCode errorCode, String userMessage) {
        this(errorCode, ErrorSeverity.MEDIUM, true, userMessage, null, null);
    }
    
    public GhostVaultException(ErrorCode errorCode, String userMessage, Throwable cause) {
        this(errorCode, ErrorSeverity.MEDIUM, true, userMessage, null, cause);
    }
    
    public GhostVaultException(ErrorCode errorCode, ErrorSeverity severity, boolean recoverable,
                              String userMessage, String technicalDetails, Throwable cause) {
        super(userMessage != null ? userMessage : errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.severity = severity;
        this.recoverable = recoverable;
        this.userMessage = userMessage != null ? userMessage : errorCode.getDefaultMessage();
        this.technicalDetails = technicalDetails;
    }
    
    // Getters
    public ErrorCode getErrorCode() { return errorCode; }
    public ErrorSeverity getSeverity() { return severity; }
    public boolean isRecoverable() { return recoverable; }
    public String getUserMessage() { return userMessage; }
    public String getTechnicalDetails() { return technicalDetails; }
    
    /**
     * Get error code as integer
     */
    public int getErrorCodeValue() {
        return errorCode.getCode();
    }
    
    /**
     * Get formatted error message for logging
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode.getCode()).append("] ");
        sb.append(severity).append(" - ");
        sb.append(userMessage);
        
        if (technicalDetails != null) {
            sb.append(" (").append(technicalDetails).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Check if this is a security-related error
     */
    public boolean isSecurityError() {
        int code = errorCode.getCode();
        return (code >= 1000 && code < 1100) ||  // Authentication
               (code >= 1100 && code < 1200) ||  // Cryptographic
               (code >= 1700 && code < 1800);    // Security
    }
    
    /**
     * Check if this error should trigger panic mode
     */
    public boolean shouldTriggerPanicMode() {
        return severity == ErrorSeverity.CRITICAL && 
               (errorCode == ErrorCode.TAMPERING_DETECTED ||
                errorCode == ErrorCode.INTRUSION_DETECTED ||
                errorCode == ErrorCode.SECURITY_VIOLATION);
    }
    
    /**
     * Get suggested recovery action
     */
    public String getRecoveryAction() {
        switch (errorCode) {
            case INVALID_PASSWORD:
                return "Please verify your password and try again";
            case SESSION_EXPIRED:
                return "Please log in again";
            case FILE_NOT_FOUND:
                return "Check if the file exists and try again";
            case DISK_FULL:
                return "Free up disk space and retry the operation";
            case VAULT_CORRUPTED:
                return "Restore from a recent backup";
            case NETWORK_ERROR:
                return "Check your network connection and retry";
            case INSUFFICIENT_MEMORY:
                return "Close other applications and try again";
            case BACKUP_CORRUPTED:
                return "Use a different backup file";
            default:
                return recoverable ? "Please try the operation again" : "Contact support for assistance";
        }
    }
    
    @Override
    public String toString() {
        return String.format("GhostVaultException{code=%s, severity=%s, recoverable=%s, message='%s'}", 
            errorCode, severity, recoverable, userMessage);
    }
}