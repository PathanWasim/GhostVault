package com.ghostvault.exception;

/**
 * Exception for cryptographic operations failures
 */
public class CryptographicException extends GhostVaultException {
    
    public CryptographicException(String message) {
        super(ErrorCode.ENCRYPTION_FAILED, ErrorSeverity.HIGH, false, message, null, null);
    }
    
    public CryptographicException(String message, Throwable cause) {
        super(ErrorCode.ENCRYPTION_FAILED, ErrorSeverity.HIGH, false, message, null, cause);
    }
    
    public CryptographicException(ErrorCode errorCode, String message) {
        super(errorCode, ErrorSeverity.HIGH, false, message, null, null);
    }
    
    public CryptographicException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, ErrorSeverity.HIGH, false, message, null, cause);
    }
    
    public CryptographicException(ErrorCode errorCode, String message, String technicalDetails) {
        super(errorCode, ErrorSeverity.HIGH, false, message, technicalDetails, null);
    }
    
    /**
     * Create exception for encryption failure
     */
    public static CryptographicException encryptionFailed(String details, Throwable cause) {
        return new CryptographicException(ErrorCode.ENCRYPTION_FAILED, 
            "Failed to encrypt data", cause);
    }
    
    /**
     * Create exception for decryption failure
     */
    public static CryptographicException decryptionFailed(String details, Throwable cause) {
        return new CryptographicException(ErrorCode.DECRYPTION_FAILED, 
            "Failed to decrypt data", cause);
    }
    
    /**
     * Create exception for key generation failure
     */
    public static CryptographicException keyGenerationFailed(Throwable cause) {
        return new CryptographicException(ErrorCode.KEY_GENERATION_FAILED, 
            "Failed to generate encryption key", cause);
    }
    
    /**
     * Create exception for invalid key
     */
    public static CryptographicException invalidKey(String details) {
        return new CryptographicException(ErrorCode.INVALID_KEY, 
            "Invalid encryption key provided", details);
    }
}