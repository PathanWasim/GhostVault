package com.ghostvault.security;

/**
 * Security configuration constants for GhostVault encryption
 * Defines industry-standard cryptographic parameters
 */
public final class SecurityConfiguration {
    
    // PBKDF2 Configuration
    public static final int PBKDF2_ITERATIONS = 100_000;
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int KEY_LENGTH = 256; // AES-256 key length in bits
    
    // Salt and IV Configuration
    public static final int SALT_LENGTH = 32; // 256 bits
    public static final int IV_LENGTH = 12;   // 96 bits for GCM (optimal)
    
    // AES-GCM Configuration
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final String AES_ALGORITHM = "AES";
    public static final int GCM_TAG_LENGTH = 16; // 128-bit authentication tag
    
    // File Format Constants
    public static final String ENCRYPTED_PASSWORD_FILE = "passwords.enc";
    public static final String ENCRYPTED_METADATA_FILE = "metadata.enc";
    public static final String PLAIN_PASSWORD_FILE = "passwords.dat";
    public static final String PLAIN_METADATA_FILE = "metadata.json";
    
    // Security Validation
    public static final String SECURITY_MARKER = "GHOSTVAULT_ENCRYPTED";
    public static final byte[] MAGIC_BYTES = {0x47, 0x56, 0x45, 0x4E}; // "GVEN" - GhostVault Encrypted
    
    // Memory Security
    public static final int SECURE_WIPE_PASSES = 3;
    
    // Private constructor to prevent instantiation
    private SecurityConfiguration() {
        throw new AssertionError("SecurityConfiguration should not be instantiated");
    }
    
    /**
     * Validate security configuration parameters
     * @return true if all parameters are within secure ranges
     */
    public static boolean validateConfiguration() {
        return PBKDF2_ITERATIONS >= 100_000 &&
               SALT_LENGTH >= 16 &&
               IV_LENGTH == 12 &&
               KEY_LENGTH == 256 &&
               GCM_TAG_LENGTH == 16;
    }
}