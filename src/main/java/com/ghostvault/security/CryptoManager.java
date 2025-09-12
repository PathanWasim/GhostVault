package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Handles all cryptographic operations for the vault
 * Uses AES-256-CBC with PBKDF2 key derivation and HMAC-SHA256 authentication
 */
public class CryptoManager {
    
    private SecretKey masterKey;
    private SecretKey hmacKey;
    private final SecureRandom secureRandom;
    
    // HMAC algorithm for message authentication
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int HMAC_SIZE = 32; // SHA-256 output size
    
    public CryptoManager() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Initialize crypto manager with password and salt
     */
    public void initializeWithPassword(String password, byte[] salt) throws GeneralSecurityException {
        this.masterKey = deriveKey(password, salt, "AES");
        this.hmacKey = deriveKey(password, salt, "HMAC");
    }
    
    /**
     * Derive key from password using PBKDF2 with different info for different purposes
     */
    public SecretKey deriveKey(String password, byte[] salt) throws GeneralSecurityException {
        return deriveKey(password, salt, "AES");
    }
    
    /**
     * Derive encryption key from password using PBKDF2 with purpose-specific salt
     */
    private SecretKey deriveKey(String password, byte[] salt, String purpose) throws GeneralSecurityException {
        // Create purpose-specific salt to derive different keys
        byte[] purposeSalt = new byte[salt.length + purpose.getBytes().length];
        System.arraycopy(salt, 0, purposeSalt, 0, salt.length);
        System.arraycopy(purpose.getBytes(), 0, purposeSalt, salt.length, purpose.getBytes().length);
        
        char[] passwordChars = password.toCharArray();
        try {
            KeySpec spec = new PBEKeySpec(
                passwordChars,
                purposeSalt,
                AppConfig.PBKDF2_ITERATIONS,
                AppConfig.KEY_SIZE
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(AppConfig.KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            
            String algorithm = purpose.equals("HMAC") ? HMAC_ALGORITHM : AppConfig.ENCRYPTION_ALGORITHM;
            return new SecretKeySpec(keyBytes, algorithm);
            
        } finally {
            // Clear the password from memory
            Arrays.fill(passwordChars, '\0');
            secureWipe(purposeSalt);
        }
    }
    
    /**
     * Encrypt data using AES-256-CBC with HMAC authentication
     */
    public EncryptedData encrypt(byte[] plaintext) throws GeneralSecurityException {
        if (masterKey == null || hmacKey == null) {
            throw new IllegalStateException("Crypto manager not initialized");
        }
        
        Cipher cipher = Cipher.getInstance(AppConfig.ENCRYPTION_TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = generateSecureRandom(AppConfig.IV_SIZE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, ivSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // Calculate HMAC over IV + ciphertext
        byte[] dataToAuthenticate = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, dataToAuthenticate, 0, iv.length);
        System.arraycopy(ciphertext, 0, dataToAuthenticate, iv.length, ciphertext.length);
        
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        hmac.init(hmacKey);
        byte[] hmacValue = hmac.doFinal(dataToAuthenticate);
        
        return new EncryptedData(ciphertext, iv, hmacValue);
    }
    
    /**
     * Encrypt data with provided key (for password hashing, etc.)
     */
    public EncryptedData encrypt(byte[] plaintext, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AppConfig.ENCRYPTION_TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = generateSecureRandom(AppConfig.IV_SIZE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // For external key usage, we don't have HMAC key, so skip authentication
        return new EncryptedData(ciphertext, iv, null);
    }
    
    /**
     * Decrypt data using AES-256-CBC with HMAC verification
     */
    public byte[] decrypt(EncryptedData encryptedData) throws GeneralSecurityException {
        if (masterKey == null || hmacKey == null) {
            throw new IllegalStateException("Crypto manager not initialized");
        }
        
        // Verify HMAC if present
        if (encryptedData.getHmac() != null) {
            byte[] dataToAuthenticate = new byte[encryptedData.getIv().length + encryptedData.getCiphertext().length];
            System.arraycopy(encryptedData.getIv(), 0, dataToAuthenticate, 0, encryptedData.getIv().length);
            System.arraycopy(encryptedData.getCiphertext(), 0, dataToAuthenticate, encryptedData.getIv().length, encryptedData.getCiphertext().length);
            
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(hmacKey);
            byte[] calculatedHmac = hmac.doFinal(dataToAuthenticate);
            
            if (!constantTimeEquals(calculatedHmac, encryptedData.getHmac())) {
                throw new GeneralSecurityException("HMAC verification failed - data may be corrupted or tampered");
            }
        }
        
        Cipher cipher = Cipher.getInstance(AppConfig.ENCRYPTION_TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(encryptedData.getIv());
        cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec);
        
        return cipher.doFinal(encryptedData.getCiphertext());
    }
    
    /**
     * Decrypt data with provided key (for password verification, etc.)
     */
    public byte[] decrypt(EncryptedData encryptedData, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AppConfig.ENCRYPTION_TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(encryptedData.getIv());
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        
        return cipher.doFinal(encryptedData.getCiphertext());
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
     * Securely wipe sensitive data from memory
     */
    public void secureWipe(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }
    
    /**
     * Constant-time comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    /**
     * Clear sensitive key material from memory
     */
    public void clearKeys() {
        if (masterKey != null) {
            byte[] keyBytes = masterKey.getEncoded();
            if (keyBytes != null) {
                secureWipe(keyBytes);
            }
            masterKey = null;
        }
        
        if (hmacKey != null) {
            byte[] hmacKeyBytes = hmacKey.getEncoded();
            if (hmacKeyBytes != null) {
                secureWipe(hmacKeyBytes);
            }
            hmacKey = null;
        }
    }
    
    /**
     * Get master key for other components (audit logging, metadata)
     */
    public SecretKey getMasterKey() {
        return masterKey;
    }
    
    /**
     * Container for encrypted data with IV and HMAC authentication
     */
    public static class EncryptedData {
        private final byte[] ciphertext;
        private final byte[] iv;
        private final byte[] hmac;
        
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
        
        public byte[] getHmac() {
            return hmac != null ? hmac.clone() : null;
        }
        
        /**
         * Get combined data (IV + ciphertext + HMAC) for storage
         */
        public byte[] getCombinedData() {
            int totalLength = iv.length + ciphertext.length;
            if (hmac != null) {
                totalLength += hmac.length;
            }
            
            byte[] combined = new byte[totalLength];
            int offset = 0;
            
            System.arraycopy(iv, 0, combined, offset, iv.length);
            offset += iv.length;
            
            System.arraycopy(ciphertext, 0, combined, offset, ciphertext.length);
            offset += ciphertext.length;
            
            if (hmac != null) {
                System.arraycopy(hmac, 0, combined, offset, hmac.length);
            }
            
            return combined;
        }
        
        /**
         * Create EncryptedData from combined data (IV + ciphertext + optional HMAC)
         */
        public static EncryptedData fromCombinedData(byte[] combinedData) {
            if (combinedData.length < AppConfig.IV_SIZE) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }
            
            byte[] iv = Arrays.copyOfRange(combinedData, 0, AppConfig.IV_SIZE);
            
            // Check if HMAC is present (data length indicates this)
            boolean hasHmac = combinedData.length >= AppConfig.IV_SIZE + HMAC_SIZE + 16; // minimum ciphertext size
            
            if (hasHmac && combinedData.length >= AppConfig.IV_SIZE + HMAC_SIZE) {
                int ciphertextLength = combinedData.length - AppConfig.IV_SIZE - HMAC_SIZE;
                byte[] ciphertext = Arrays.copyOfRange(combinedData, AppConfig.IV_SIZE, AppConfig.IV_SIZE + ciphertextLength);
                byte[] hmac = Arrays.copyOfRange(combinedData, AppConfig.IV_SIZE + ciphertextLength, combinedData.length);
                
                return new EncryptedData(ciphertext, iv, hmac);
            } else {
                // Legacy format without HMAC
                byte[] ciphertext = Arrays.copyOfRange(combinedData, AppConfig.IV_SIZE, combinedData.length);
                return new EncryptedData(ciphertext, iv);
            }
        }
    }
}