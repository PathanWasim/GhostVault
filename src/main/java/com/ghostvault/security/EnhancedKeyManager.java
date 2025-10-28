package com.ghostvault.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced Key Manager for deterministic and consistent key generation
 * Ensures the same password always generates the same encryption key across sessions
 */
public class EnhancedKeyManager {
    
    // Fixed salt for deterministic key derivation
    private static final byte[] FIXED_SALT = {
        (byte) 0x47, (byte) 0x68, (byte) 0x6F, (byte) 0x73, // "Ghos"
        (byte) 0x74, (byte) 0x56, (byte) 0x61, (byte) 0x75, // "tVau"
        (byte) 0x6C, (byte) 0x74, (byte) 0x32, (byte) 0x30, // "lt20"
        (byte) 0x32, (byte) 0x34, (byte) 0x53, (byte) 0x61  // "24Sa"
    };
    
    // Key derivation parameters
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256; // 256 bits for AES-256
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String KEY_ALGORITHM = "AES";
    
    /**
     * Derive a deterministic encryption key from a password
     * Same password will always produce the same key
     * 
     * @param password The user's password
     * @return SecretKey for encryption/decryption
     */
    public SecretKey deriveKey(String password) {
        return deriveKey(password, FIXED_SALT);
    }
    
    /**
     * Derive a deterministic encryption key from a password with custom salt
     * 
     * @param password The user's password
     * @param salt The salt bytes for key derivation
     * @return SecretKey for encryption/decryption
     */
    public SecretKey deriveKey(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                salt, 
                PBKDF2_ITERATIONS, 
                KEY_LENGTH
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            
            // Clear the password from memory
            spec.clearPassword();
            
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to derive encryption key", e);
        }
    }
    
    /**
     * Validate that a key can successfully decrypt test data
     * 
     * @param key The key to validate
     * @param testData Encrypted test data to try decrypting
     * @return true if key is valid for the test data
     */
    public boolean validateKey(SecretKey key, byte[] testData) {
        if (key == null || testData == null || testData.length == 0) {
            return false;
        }
        
        try {
            // Try to decrypt the test data with the key
            CryptoManager cryptoManager = new CryptoManager();
            cryptoManager.decrypt(testData, key);
            return true;
        } catch (Exception e) {
            // If decryption fails, the key is not valid for this data
            return false;
        }
    }
    
    /**
     * Generate multiple key variants for recovery purposes
     * Useful when trying to recover from different key derivation methods
     * 
     * @param password The user's password
     * @return Array of possible key variants
     */
    public SecretKey[] generateKeyVariants(String password) {
        List<SecretKey> variants = new ArrayList<>();
        
        // Primary key with fixed salt
        variants.add(deriveKey(password, FIXED_SALT));
        
        // Legacy variant with different iteration count
        variants.add(deriveKeyWithIterations(password, FIXED_SALT, 50000));
        
        // Variant with password-based salt (for backward compatibility)
        byte[] passwordSalt = generatePasswordBasedSalt(password);
        variants.add(deriveKey(password, passwordSalt));
        
        // Variant with simple hash-based derivation (legacy support)
        variants.add(deriveSimpleKey(password));
        
        return variants.toArray(new SecretKey[0]);
    }
    
    /**
     * Derive key with specific iteration count
     */
    private SecretKey deriveKeyWithIterations(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                salt, 
                iterations, 
                KEY_LENGTH
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            
            spec.clearPassword();
            
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to derive encryption key with custom iterations", e);
        }
    }
    
    /**
     * Generate salt based on password for backward compatibility
     */
    private byte[] generatePasswordBasedSalt(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Use first 16 bytes of hash as salt
            return Arrays.copyOf(hash, 16);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate password-based salt", e);
        }
    }
    
    /**
     * Simple key derivation for legacy compatibility
     */
    private SecretKey deriveSimpleKey(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to derive simple key", e);
        }
    }
    
    /**
     * Get key derivation parameters for storage/verification
     */
    public KeyDerivationParams getKeyDerivationParams() {
        return new KeyDerivationParams(
            PBKDF2_ALGORITHM,
            FIXED_SALT.clone(),
            PBKDF2_ITERATIONS,
            KEY_LENGTH
        );
    }
    
    /**
     * Verify that two keys are identical
     */
    public boolean keysMatch(SecretKey key1, SecretKey key2) {
        if (key1 == null || key2 == null) {
            return false;
        }
        
        return Arrays.equals(key1.getEncoded(), key2.getEncoded());
    }
    
    /**
     * Create a validation hash for a key (for storage/verification)
     */
    public byte[] createKeyValidationHash(SecretKey key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to create key validation hash", e);
        }
    }
    
    /**
     * Data class for key derivation parameters
     */
    public static class KeyDerivationParams {
        private final String algorithm;
        private final byte[] salt;
        private final int iterations;
        private final int keyLength;
        
        public KeyDerivationParams(String algorithm, byte[] salt, int iterations, int keyLength) {
            this.algorithm = algorithm;
            this.salt = salt.clone();
            this.iterations = iterations;
            this.keyLength = keyLength;
        }
        
        public String getAlgorithm() { return algorithm; }
        public byte[] getSalt() { return salt.clone(); }
        public int getIterations() { return iterations; }
        public int getKeyLength() { return keyLength; }
    }
}