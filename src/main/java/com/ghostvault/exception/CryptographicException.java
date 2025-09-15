package com.ghostvault.exception;

/**
 * Exception for cryptographic operations errors
 */
public class CryptographicException extends GhostVaultException {
    
    public enum CryptoErrorType {
        ENCRYPTION_FAILED("Encryption operation failed"),
        DECRYPTION_FAILED("Decryption operation failed"),
        KEY_GENERATION_FAILED("Key generation failed"),
        KEY_DERIVATION_FAILED("Key derivation failed"),
        INVALID_KEY("Invalid encryption key"),
        INVALID_IV("Invalid initialization vector"),
        HASH_CALCULATION_FAILED("Hash calculation failed"),
        SIGNATURE_VERIFICATION_FAILED("Signature verification failed"),
        RANDOM_GENERATION_FAILED("Random number generation failed"),
        ALGORITHM_NOT_AVAILABLE("Cryptographic algorithm not available");
        
        private final String description;
        
        CryptoErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final CryptoErrorType cryptoErrorType;
    
    public CryptographicException(String message, CryptoErrorType errorType) {
        super(message, ErrorCategory.CRYPTOGRAPHIC, ErrorSeverity.HIGH);
        this.cryptoErrorType = errorType;
    }
    
    public CryptographicException(String message, Throwable cause, CryptoErrorType errorType) {
        super(message, cause, ErrorCategory.CRYPTOGRAPHIC, ErrorSeverity.HIGH);
        this.cryptoErrorType = errorType;
    }
    
    public CryptographicException(String message, CryptoErrorType errorType, ErrorSeverity severity) {
        super(message, ErrorCategory.CRYPTOGRAPHIC, severity);
        this.cryptoErrorType = errorType;
    }
    
    public CryptoErrorType getCryptoErrorType() {
        return cryptoErrorType;
    }
    
    @Override
    public String getUserMessage() {
        return "A security error occurred. Please try again or contact support if the problem persists.";
    }
}