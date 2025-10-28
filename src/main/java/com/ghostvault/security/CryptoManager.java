package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Handles all cryptographic operations for the vault using AEAD (AES-GCM)
 * 
 * SECURITY IMPROVEMENTS:
 * - Uses AES-GCM (Authenticated Encryption with Associated Data) instead of AES-CBC+HMAC
 * - Eliminates padding oracle vulnerabilities
 * - Provides built-in authentication without separate HMAC
 * - Simpler and more secure API
 * 
 * @version 2.0.0 - AEAD Implementation
 */
public class CryptoManager {
    
    // AES-GCM configuration
    private static final String AEAD_ALGORITHM = "AES";
    private static final String AEAD_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits (recommended for GCM)
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag
    
    private SecretKey masterKey;
    private final SecureRandom secureRandom;
    
    public CryptoManager() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Initialize crypto manager with a key
     */
    public void initializeWithKey(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        this.masterKey = key;
    }
    
    /**
     * Create a SecretKey from raw key bytes
     */
    public SecretKey keyFromBytes(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) { // 256 bits
            throw new IllegalArgumentException("Key must be 32 bytes (256 bits)");
        }
        return new SecretKeySpec(keyBytes, AEAD_ALGORITHM);
    }
    
    /**
     * Encrypt data using AES-GCM AEAD
     * 
     * @param plaintext Data to encrypt
     * @param key Encryption key
     * @param aad Additional Authenticated Data (can be null)
     * @return IV (12 bytes) || ciphertext+tag
     */
    public byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad) throws GeneralSecurityException {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Generate random IV (12 bytes for GCM)
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AEAD_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        // Add AAD if provided
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
        
        // Encrypt (includes authentication tag)
        byte[] ciphertextWithTag = cipher.doFinal(plaintext);
        
        // Combine IV + ciphertext+tag
        byte[] result = new byte[GCM_IV_LENGTH + ciphertextWithTag.length];
        System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertextWithTag, 0, result, GCM_IV_LENGTH, ciphertextWithTag.length);
        
        return result;
    }
    
    /**
     * Encrypt data using AES-GCM AEAD (no AAD)
     */
    public byte[] encrypt(byte[] plaintext, SecretKey key) throws GeneralSecurityException {
        return encrypt(plaintext, key, null);
    }
    
    /**
     * Encrypt data using master key
     */
    public EncryptedData encrypt(byte[] plaintext) throws GeneralSecurityException {
        if (masterKey == null) {
            throw new IllegalStateException("Crypto manager not initialized with master key");
        }
        
        byte[] combined = encrypt(plaintext, masterKey, null);
        
        // Split into IV and ciphertext for EncryptedData compatibility
        byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
        byte[] ciphertextWithTag = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);
        
        return new EncryptedData(ciphertextWithTag, iv, null); // No separate HMAC in GCM
    }
    
    /**
     * Decrypt data using AES-GCM AEAD
     * 
     * @param ivAndCiphertext IV (12 bytes) || ciphertext+tag
     * @param key Decryption key
     * @param aad Additional Authenticated Data (must match encryption AAD)
     * @return Decrypted plaintext
     * @throws GeneralSecurityException if authentication fails or decryption error
     */
    public byte[] decrypt(byte[] ivAndCiphertext, SecretKey key, byte[] aad) throws GeneralSecurityException {
        if (ivAndCiphertext == null || ivAndCiphertext.length < GCM_IV_LENGTH + 16) {
            throw new IllegalArgumentException("Invalid ciphertext format");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Extract IV and ciphertext+tag
        byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, GCM_IV_LENGTH);
        byte[] ciphertextWithTag = Arrays.copyOfRange(ivAndCiphertext, GCM_IV_LENGTH, ivAndCiphertext.length);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AEAD_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        // Add AAD if provided
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
        
        // Decrypt and verify authentication tag
        // Will throw AEADBadTagException if authentication fails
        try {
            return cipher.doFinal(ciphertextWithTag);
        } catch (javax.crypto.AEADBadTagException e) {
            throw new GeneralSecurityException("Decryption failed: Invalid key or corrupted data. " +
                "This usually indicates the wrong password was used or the data has been tampered with.", e);
        }
    }
    
    /**
     * Decrypt data using AES-GCM AEAD (no AAD)
     */
    public byte[] decrypt(byte[] ivAndCiphertext, SecretKey key) throws GeneralSecurityException {
        return decrypt(ivAndCiphertext, key, null);
    }
    
    /**
     * Decrypt data using master key (EncryptedData format)
     */
    public byte[] decrypt(EncryptedData encryptedData) throws GeneralSecurityException {
        if (masterKey == null) {
            throw new IllegalStateException("Crypto manager not initialized with master key");
        }
        
        // Combine IV and ciphertext
        byte[] combined = new byte[encryptedData.getIv().length + encryptedData.getCiphertext().length];
        System.arraycopy(encryptedData.getIv(), 0, combined, 0, encryptedData.getIv().length);
        System.arraycopy(encryptedData.getCiphertext(), 0, combined, encryptedData.getIv().length, encryptedData.getCiphertext().length);
        
        return decrypt(combined, masterKey, null);
    }
    
    /**
     * Decrypt data with provided key (EncryptedData format)
     */
    public byte[] decrypt(EncryptedData encryptedData, SecretKey key) throws GeneralSecurityException {
        // Combine IV and ciphertext
        byte[] combined = new byte[encryptedData.getIv().length + encryptedData.getCiphertext().length];
        System.arraycopy(encryptedData.getIv(), 0, combined, 0, encryptedData.getIv().length);
        System.arraycopy(encryptedData.getCiphertext(), 0, combined, encryptedData.getIv().length, encryptedData.getCiphertext().length);
        
        return decrypt(combined, key, null);
    }
    
    /**
     * Calculate SHA-256 hash of data for integrity verification
     */
    public String calculateSHA256(byte[] data) throws GeneralSecurityException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new GeneralSecurityException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Generate cryptographically secure salt
     */
    public byte[] generateSalt() {
        return generateSecureRandom(AppConfig.SALT_SIZE);
    }
    
    /**
     * Generate secure random bytes
     */
    public byte[] generateSecureRandom(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Securely wipe sensitive data from memory (zeroize)
     */
    public void zeroize(byte[] data) {
        if (data != null) {
            MemoryUtils.secureWipe(data);
        }
    }
    
    /**
     * Clear sensitive key material from memory
     */
    public void clearKeys() {
        if (masterKey != null) {
            byte[] keyBytes = masterKey.getEncoded();
            zeroize(keyBytes);
            masterKey = null;
        }
    }
    
    /**
     * Get master key for other components (use with caution)
     */
    public SecretKey getMasterKey() {
        return masterKey;
    }
    
    /**
     * Derive key from password using deterministic key derivation
     * Uses EnhancedKeyManager for consistent key generation across sessions
     */
    public SecretKey deriveKeyFromPassword(char[] password) throws GeneralSecurityException {
        try {
            String passwordString = new String(password);
            EnhancedKeyManager keyManager = new EnhancedKeyManager();
            return keyManager.deriveKey(passwordString);
        } finally {
            // Clear password from memory
            Arrays.fill(password, '\0');
        }
    }
    
    /**
     * Derive key from password using PBKDF2 with salt (legacy method)
     * @deprecated Use deriveKeyFromPassword(char[]) for deterministic key generation
     */
    @Deprecated
    public SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws GeneralSecurityException {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, 100000, 256);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            // Clear password from memory
            Arrays.fill(password, '\0');
        }
    }
    
    /**
     * Derive key from password string (convenience method)
     */
    public SecretKey deriveKeyFromPassword(String password) throws GeneralSecurityException {
        EnhancedKeyManager keyManager = new EnhancedKeyManager();
        return keyManager.deriveKey(password);
    }
    
    /**
     * Get enhanced key manager instance for advanced key operations
     */
    public EnhancedKeyManager getEnhancedKeyManager() {
        return new EnhancedKeyManager();
    }
    
    /**
     * Container for encrypted data (backward compatibility)
     * Note: HMAC field is deprecated and unused in GCM mode
     */
    public static class EncryptedData {
        private final byte[] ciphertext;
        private final byte[] iv;
        private final byte[] hmac; // Deprecated - kept for compatibility
        
        public EncryptedData(byte[] ciphertext, byte[] iv) {
            this(ciphertext, iv, null);
        }
        
        public EncryptedData(byte[] ciphertext, byte[] iv, byte[] hmac) {
            this.ciphertext = ciphertext.clone();
            this.iv = iv.clone();
            this.hmac = hmac != null ? hmac.clone() : null;
        }
        
        public byte[] getCiphertext() {
            return ciphertext.clone();
        }
        
        public byte[] getIv() {
            return iv.clone();
        }
        
        @Deprecated
        public byte[] getHmac() {
            return hmac != null ? hmac.clone() : null;
        }
        
        /**
         * Get combined data (IV + ciphertext) for storage
         */
        public byte[] getCombinedData() {
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return combined;
        }
        
        /**
         * Create EncryptedData from combined data (IV + ciphertext)
         */
        public static EncryptedData fromCombinedData(byte[] combinedData) {
            if (combinedData == null || combinedData.length < GCM_IV_LENGTH + 16) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }
            
            byte[] iv = Arrays.copyOfRange(combinedData, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combinedData, GCM_IV_LENGTH, combinedData.length);
            
            return new EncryptedData(ciphertext, iv, null);
        }
    }
}
