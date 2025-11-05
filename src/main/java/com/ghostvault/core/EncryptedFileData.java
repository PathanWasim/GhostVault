package com.ghostvault.core;

import com.ghostvault.security.SecurityConfiguration;
import java.util.Arrays;

/**
 * Container for encrypted file data with proper format validation
 * Format: [MAGIC_BYTES][SALT_32_BYTES][IV_12_BYTES][ENCRYPTED_CONTENT_WITH_AUTH_TAG]
 */
public class EncryptedFileData {
    
    // Magic bytes to identify encrypted files
    private static final byte[] MAGIC_BYTES = {0x47, 0x56, 0x45, 0x46}; // "GVEF" - GhostVault Encrypted File
    
    private final byte[] salt;
    private final byte[] iv;
    private final byte[] ciphertext;
    private final boolean isValid;
    
    /**
     * Create encrypted file data
     * @param salt The salt used for key derivation
     * @param iv The initialization vector
     * @param ciphertext The encrypted content with authentication tag
     */
    public EncryptedFileData(byte[] salt, byte[] iv, byte[] ciphertext) {
        this.salt = salt != null ? salt.clone() : null;
        this.iv = iv != null ? iv.clone() : null;
        this.ciphertext = ciphertext != null ? ciphertext.clone() : null;
        this.isValid = validateFormat();
    }
    
    /**
     * Validate the encrypted file data format
     * @return true if format is valid
     */
    private boolean validateFormat() {
        if (salt == null || iv == null || ciphertext == null) {
            return false;
        }
        
        if (salt.length != SecurityConfiguration.SALT_LENGTH) {
            return false;
        }
        
        if (iv.length != SecurityConfiguration.IV_LENGTH) {
            return false;
        }
        
        // Ciphertext should include at least the GCM authentication tag
        if (ciphertext.length < SecurityConfiguration.GCM_TAG_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the salt (copy)
     * @return salt bytes
     */
    public byte[] getSalt() {
        return salt != null ? salt.clone() : null;
    }
    
    /**
     * Get the IV (copy)
     * @return IV bytes
     */
    public byte[] getIv() {
        return iv != null ? iv.clone() : null;
    }
    
    /**
     * Get the ciphertext (copy)
     * @return ciphertext bytes
     */
    public byte[] getCiphertext() {
        return ciphertext != null ? ciphertext.clone() : null;
    }
    
    /**
     * Check if the data format is valid
     * @return true if valid
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * Get the total size of the encrypted data when serialized
     * @return total size in bytes
     */
    public int getTotalSize() {
        if (!isValid) {
            return 0;
        }
        return MAGIC_BYTES.length + salt.length + iv.length + ciphertext.length;
    }
    
    /**
     * Serialize to byte array for storage
     * Format: [MAGIC_BYTES][SALT][IV][CIPHERTEXT]
     * @return serialized data
     * @throws IllegalStateException if data is invalid
     */
    public byte[] toByteArray() {
        if (!isValid) {
            throw new IllegalStateException("Cannot serialize invalid encrypted file data");
        }
        
        byte[] result = new byte[getTotalSize()];
        int offset = 0;
        
        // Magic bytes for format identification
        System.arraycopy(MAGIC_BYTES, 0, result, offset, MAGIC_BYTES.length);
        offset += MAGIC_BYTES.length;
        
        // Salt
        System.arraycopy(salt, 0, result, offset, salt.length);
        offset += salt.length;
        
        // IV
        System.arraycopy(iv, 0, result, offset, iv.length);
        offset += iv.length;
        
        // Ciphertext (includes authentication tag)
        System.arraycopy(ciphertext, 0, result, offset, ciphertext.length);
        
        return result;
    }
    
    /**
     * Deserialize from byte array
     * @param data The serialized data
     * @return EncryptedFileData instance
     * @throws IllegalArgumentException if data format is invalid
     */
    public static EncryptedFileData fromByteArray(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        
        int minSize = MAGIC_BYTES.length + SecurityConfiguration.SALT_LENGTH + 
                     SecurityConfiguration.IV_LENGTH + SecurityConfiguration.GCM_TAG_LENGTH;
        
        if (data.length < minSize) {
            throw new IllegalArgumentException("Data too small for encrypted file format");
        }
        
        int offset = 0;
        
        // Verify magic bytes
        byte[] magic = new byte[MAGIC_BYTES.length];
        System.arraycopy(data, offset, magic, 0, MAGIC_BYTES.length);
        if (!Arrays.equals(magic, MAGIC_BYTES)) {
            throw new IllegalArgumentException("Invalid encrypted file format - magic bytes mismatch");
        }
        offset += MAGIC_BYTES.length;
        
        // Extract salt
        byte[] salt = new byte[SecurityConfiguration.SALT_LENGTH];
        System.arraycopy(data, offset, salt, 0, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        
        // Extract IV
        byte[] iv = new byte[SecurityConfiguration.IV_LENGTH];
        System.arraycopy(data, offset, iv, 0, SecurityConfiguration.IV_LENGTH);
        offset += SecurityConfiguration.IV_LENGTH;
        
        // Extract ciphertext (remaining data)
        int ciphertextLength = data.length - offset;
        byte[] ciphertext = new byte[ciphertextLength];
        System.arraycopy(data, offset, ciphertext, 0, ciphertextLength);
        
        return new EncryptedFileData(salt, iv, ciphertext);
    }
    
    /**
     * Check if byte array represents an encrypted file
     * @param data The data to check
     * @return true if data appears to be encrypted file format
     */
    public static boolean isEncryptedFileFormat(byte[] data) {
        if (data == null || data.length < MAGIC_BYTES.length) {
            return false;
        }
        
        // Check magic bytes
        for (int i = 0; i < MAGIC_BYTES.length; i++) {
            if (data[i] != MAGIC_BYTES[i]) {
                return false;
            }
        }
        
        // Check minimum size
        int minSize = MAGIC_BYTES.length + SecurityConfiguration.SALT_LENGTH + 
                     SecurityConfiguration.IV_LENGTH + SecurityConfiguration.GCM_TAG_LENGTH;
        
        return data.length >= minSize;
    }
    
    /**
     * Get format information
     * @return string describing the format
     */
    public String getFormatInfo() {
        if (!isValid) {
            return "Invalid encrypted file data";
        }
        
        return String.format("Encrypted file format: %d bytes salt, %d bytes IV, %d bytes ciphertext (total: %d bytes)",
                           salt.length, iv.length, ciphertext.length, getTotalSize());
    }
    
    @Override
    public String toString() {
        return String.format("EncryptedFileData{valid=%s, saltLen=%d, ivLen=%d, ciphertextLen=%d}",
                           isValid, 
                           salt != null ? salt.length : 0,
                           iv != null ? iv.length : 0,
                           ciphertext != null ? ciphertext.length : 0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EncryptedFileData other = (EncryptedFileData) obj;
        return Arrays.equals(salt, other.salt) &&
               Arrays.equals(iv, other.iv) &&
               Arrays.equals(ciphertext, other.ciphertext);
    }
    
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(salt);
        result = 31 * result + Arrays.hashCode(iv);
        result = 31 * result + Arrays.hashCode(ciphertext);
        return result;
    }
}