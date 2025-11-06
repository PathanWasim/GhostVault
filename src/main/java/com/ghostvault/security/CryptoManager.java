package com.ghostvault.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Enhanced crypto manager for secure encryption operations
 * Implements AES-256-GCM encryption and PBKDF2 password hashing
 */
public class CryptoManager {
    
    /**
     * Container for encrypted data with salt, IV, and ciphertext
     */
    public static class EncryptedData {
        private final byte[] salt;
        private final byte[] iv;
        private final byte[] ciphertext;
        
        public EncryptedData(byte[] salt, byte[] iv, byte[] ciphertext) {
            this.salt = salt != null ? salt.clone() : null;
            this.iv = iv != null ? iv.clone() : null;
            this.ciphertext = ciphertext != null ? ciphertext.clone() : null;
        }
        
        // Legacy constructor for backward compatibility
        public EncryptedData(byte[] ciphertext, byte[] iv) {
            this(null, iv, ciphertext);
        }
        
        public byte[] getSalt() { return salt != null ? salt.clone() : null; }
        public byte[] getIv() { return iv != null ? iv.clone() : null; }
        public byte[] getCiphertext() { return ciphertext != null ? ciphertext.clone() : null; }
        
        /**
         * Get the total size of the encrypted data
         */
        public int getTotalSize() {
            int size = 0;
            if (salt != null) size += salt.length;
            if (iv != null) size += iv.length;
            if (ciphertext != null) size += ciphertext.length;
            return size;
        }
        
        /**
         * Serialize to byte array for storage
         */
        public byte[] toByteArray() {
            int totalSize = getTotalSize();
            byte[] result = new byte[totalSize];
            int offset = 0;
            
            if (salt != null) {
                System.arraycopy(salt, 0, result, offset, salt.length);
                offset += salt.length;
            }
            if (iv != null) {
                System.arraycopy(iv, 0, result, offset, iv.length);
                offset += iv.length;
            }
            if (ciphertext != null) {
                System.arraycopy(ciphertext, 0, result, offset, ciphertext.length);
            }
            
            return result;
        }
        
        /**
         * Deserialize from byte array
         */
        public static EncryptedData fromByteArray(byte[] data) {
            if (data == null || data.length < SecurityConfiguration.SALT_LENGTH + SecurityConfiguration.IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }
            
            byte[] salt = new byte[SecurityConfiguration.SALT_LENGTH];
            byte[] iv = new byte[SecurityConfiguration.IV_LENGTH];
            byte[] ciphertext = new byte[data.length - SecurityConfiguration.SALT_LENGTH - SecurityConfiguration.IV_LENGTH];
            
            System.arraycopy(data, 0, salt, 0, SecurityConfiguration.SALT_LENGTH);
            System.arraycopy(data, SecurityConfiguration.SALT_LENGTH, iv, 0, SecurityConfiguration.IV_LENGTH);
            System.arraycopy(data, SecurityConfiguration.SALT_LENGTH + SecurityConfiguration.IV_LENGTH, ciphertext, 0, ciphertext.length);
            
            return new EncryptedData(salt, iv, ciphertext);
        }
    }
    
    /**
     * Encrypt data using AES-256-GCM with a password-derived key
     * @param data The data to encrypt
     * @param password The password to derive the key from
     * @return EncryptedData containing salt, IV, and ciphertext with auth tag
     * @throws Exception if encryption fails
     */
    public EncryptedData encryptWithPassword(byte[] data, String password) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Generate unique salt and IV for this encryption
        byte[] salt = generateSalt();
        byte[] iv = new byte[SecurityConfiguration.IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // Derive key from password and salt
        SecretKey key = deriveEncryptionKey(password, salt);
        
        try {
            // Encrypt with AES-GCM
            Cipher cipher = Cipher.getInstance(SecurityConfiguration.ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(SecurityConfiguration.GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(data);
            
            return new EncryptedData(salt, iv, ciphertext);
        } finally {
            // Clear key from memory
            secureWipe(key);
        }
    }
    
    /**
     * Decrypt data using AES-256-GCM with a password-derived key
     * @param encryptedData The encrypted data containing salt, IV, and ciphertext
     * @param password The password to derive the key from
     * @return The decrypted data
     * @throws Exception if decryption fails or authentication fails
     */
    public byte[] decryptWithPassword(EncryptedData encryptedData, String password) throws Exception {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        byte[] salt = encryptedData.getSalt();
        byte[] iv = encryptedData.getIv();
        byte[] ciphertext = encryptedData.getCiphertext();
        
        if (salt == null || iv == null || ciphertext == null) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }
        
        // Derive key from password and salt
        SecretKey key = deriveEncryptionKey(password, salt);
        
        try {
            // Decrypt with AES-GCM
            Cipher cipher = Cipher.getInstance(SecurityConfiguration.ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(SecurityConfiguration.GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            return cipher.doFinal(ciphertext);
        } finally {
            // Clear key from memory
            secureWipe(key);
        }
    }
    
    /**
     * Legacy encrypt method for backward compatibility
     */
    public EncryptedData encrypt(byte[] data, SecretKey key) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Generate IV
        byte[] iv = new byte[SecurityConfiguration.IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // Encrypt with AES-GCM
        Cipher cipher = Cipher.getInstance(SecurityConfiguration.ENCRYPTION_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(SecurityConfiguration.GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        byte[] ciphertext = cipher.doFinal(data);
        
        return new EncryptedData(null, iv, ciphertext);
    }
    
    /**
     * Legacy decrypt method for backward compatibility
     */
    public byte[] decrypt(EncryptedData encryptedData, SecretKey key) throws Exception {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        byte[] iv = encryptedData.getIv();
        byte[] ciphertext = encryptedData.getCiphertext();
        
        if (iv == null || ciphertext == null) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }
        
        // Decrypt with AES-GCM
        Cipher cipher = Cipher.getInstance(SecurityConfiguration.ENCRYPTION_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(SecurityConfiguration.GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        return cipher.doFinal(ciphertext);
    }
    
    private final SecureRandom secureRandom;
    
    public CryptoManager() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Generate a cryptographically secure random salt
     * @return 32-byte salt
     */
    public byte[] generateSalt() {
        byte[] salt = new byte[SecurityConfiguration.SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    /**
     * Hash a password using PBKDF2 with SHA-256
     * @param password The password to hash
     * @param salt The salt to use
     * @return The password hash
     * @throws Exception if hashing fails
     */
    public byte[] hashPassword(String password, byte[] salt) throws Exception {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (salt == null || salt.length != SecurityConfiguration.SALT_LENGTH) {
            throw new IllegalArgumentException("Salt must be " + SecurityConfiguration.SALT_LENGTH + " bytes");
        }
        
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(), 
            salt, 
            SecurityConfiguration.PBKDF2_ITERATIONS, 
            SecurityConfiguration.KEY_LENGTH
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SecurityConfiguration.KEY_DERIVATION_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        
        // Clear the password from memory
        ((PBEKeySpec) spec).clearPassword();
        
        return hash;
    }
    
    /**
     * Verify a password against a stored hash
     * @param password The password to verify
     * @param salt The salt used for hashing
     * @param storedHash The stored password hash
     * @return true if password matches
     * @throws Exception if verification fails
     */
    public boolean verifyPassword(String password, byte[] salt, byte[] storedHash) throws Exception {
        byte[] computedHash = hashPassword(password, salt);
        boolean matches = Arrays.equals(computedHash, storedHash);
        
        // Clear computed hash from memory
        secureWipe(computedHash);
        
        return matches;
    }
    
    /**
     * Derive an encryption key from a password and salt
     * @param password The password
     * @param salt The salt
     * @return SecretKey for AES encryption
     * @throws Exception if key derivation fails
     */
    public SecretKey deriveEncryptionKey(String password, byte[] salt) throws Exception {
        byte[] keyBytes = hashPassword(password, salt);
        SecretKey key = new SecretKeySpec(keyBytes, SecurityConfiguration.AES_ALGORITHM);
        
        // Clear key bytes from memory
        secureWipe(keyBytes);
        
        return key;
    }
    
    /**
     * Securely wipe sensitive data from memory using SecureMemoryManager
     * @param data The data to wipe
     */
    public void secureWipe(byte[] data) {
        SecureMemoryManager.getInstance().secureWipe(data);
    }
    
    /**
     * Securely wipe a SecretKey from memory
     * @param key The key to wipe
     */
    public void secureWipe(SecretKey key) {
        if (key != null) {
            try {
                // Get the encoded key and wipe it
                byte[] encoded = key.getEncoded();
                if (encoded != null) {
                    // Overwrite the key data with zeros
                    java.util.Arrays.fill(encoded, (byte) 0);
                }
            } catch (Exception e) {
                // Best effort - some keys might not support getEncoded()
                System.err.println("⚠️ Could not securely wipe key: " + e.getMessage());
            }
        }
    }
    
    public String calculateSHA256(byte[] data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}