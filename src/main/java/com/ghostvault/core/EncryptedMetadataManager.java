package com.ghostvault.core;

import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.SecurityConfiguration;
import com.ghostvault.security.SecureMemoryManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Encrypted metadata manager for secure storage of file metadata
 * Uses AES-256-GCM encryption with password-derived keys
 */
public class EncryptedMetadataManager {
    
    private final CryptoManager cryptoManager;
    private final ObjectMapper objectMapper;
    private final Path vaultDirectory;
    private final Path encryptedMetadataFile;
    private final Path plainMetadataFile;
    
    public EncryptedMetadataManager(String vaultPath) {
        this.cryptoManager = new CryptoManager();
        this.objectMapper = new ObjectMapper();
        this.vaultDirectory = Paths.get(vaultPath);
        this.encryptedMetadataFile = vaultDirectory.resolve(SecurityConfiguration.ENCRYPTED_METADATA_FILE);
        this.plainMetadataFile = vaultDirectory.resolve(SecurityConfiguration.PLAIN_METADATA_FILE);
    }
    
    /**
     * Save metadata in encrypted format
     * @param metadata The metadata JSON string
     * @param password The password for encryption
     * @throws Exception if encryption or saving fails
     */
    public void saveEncryptedMetadata(String metadata, String password) throws Exception {
        // Enhanced null validation with specific error messages
        if (metadata == null && password == null) {
            String errorMsg = "Both metadata and password are null - cannot proceed with encryption";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (metadata == null) {
            String errorMsg = "Metadata is null - no data to encrypt";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (password == null) {
            String errorMsg = "Password is null - cannot derive encryption key. Check if password was properly passed from VaultMainController.";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            System.err.println("🔍 Debug: This usually indicates that MetadataManager.setPassword() was not called or failed");
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (password.isEmpty()) {
            String errorMsg = "Password is empty - cannot derive encryption key";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (metadata.isEmpty()) {
            String errorMsg = "Metadata is empty - no data to encrypt";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        System.out.println("🔐 EncryptedMetadataManager: Starting metadata encryption process...");
        System.out.println("🔍 Debug: Metadata length: " + metadata.length() + " characters");
        System.out.println("🔍 Debug: Password length: " + password.length() + " characters");
        
        try {
            // Ensure vault directory exists
            System.out.println("🔍 Debug: Ensuring vault directory exists: " + vaultDirectory);
            Files.createDirectories(vaultDirectory);
            
            // Validate metadata is valid JSON before encryption
            if (!isValidJson(metadata)) {
                String errorMsg = "Metadata is not valid JSON format - cannot encrypt invalid data";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            
            // Encrypt the metadata
            System.out.println("🔍 Debug: Converting metadata to bytes...");
            byte[] metadataBytes = metadata.getBytes("UTF-8");
            System.out.println("🔍 Debug: Metadata bytes length: " + metadataBytes.length);
            
            System.out.println("🔍 Debug: Starting encryption with CryptoManager...");
            CryptoManager.EncryptedData encryptedData = cryptoManager.encryptWithPassword(metadataBytes, password);
            
            if (encryptedData == null) {
                String errorMsg = "Encryption failed - CryptoManager returned null";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("🔍 Debug: Encryption successful, serializing data...");
            
            // Create encrypted metadata format with magic bytes
            byte[] serializedData = serializeEncryptedMetadata(encryptedData);
            System.out.println("🔍 Debug: Serialized data length: " + serializedData.length + " bytes");
            
            // Save to encrypted file
            System.out.println("🔍 Debug: Writing encrypted data to file: " + encryptedMetadataFile);
            Files.write(encryptedMetadataFile, serializedData);
            
            System.out.println("✅ Metadata encrypted and saved successfully: " + encryptedMetadataFile);
            
            // Remove plain text metadata file if it exists
            if (Files.exists(plainMetadataFile)) {
                Files.delete(plainMetadataFile);
                System.out.println("🗑️ Removed plain text metadata file: " + plainMetadataFile);
            }
            
        } catch (IOException e) {
            String errorMsg = "Failed to save encrypted metadata to file: " + e.getMessage();
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            System.err.println("🔍 Debug: Target file: " + encryptedMetadataFile);
            System.err.println("🔍 Debug: Vault directory: " + vaultDirectory);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Metadata encryption failed: " + e.getMessage();
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            System.err.println("🔍 Debug: Exception type: " + e.getClass().getSimpleName());
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Load and decrypt metadata
     * @param password The password for decryption
     * @return The decrypted metadata JSON string
     * @throws Exception if decryption or loading fails
     */
    public String loadEncryptedMetadata(String password) throws Exception {
        System.out.println("🔍 Debug: Loading encrypted metadata from: " + encryptedMetadataFile);
        
        if (!Files.exists(encryptedMetadataFile)) {
            System.out.println("ℹ️ No encrypted metadata file found - returning null");
            return null;
        }
        
        if (password == null) {
            String errorMsg = "Password is null - cannot decrypt metadata. Check if password was properly passed from MetadataManager.";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (password.isEmpty()) {
            String errorMsg = "Password is empty - cannot decrypt metadata";
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        System.out.println("🔍 Debug: Password validation passed, proceeding with decryption...");
        
        try {
            // Read encrypted data
            System.out.println("🔍 Debug: Reading encrypted data from file...");
            byte[] encryptedBytes = Files.readAllBytes(encryptedMetadataFile);
            System.out.println("🔍 Debug: Read " + encryptedBytes.length + " bytes from encrypted metadata file");
            
            if (encryptedBytes.length == 0) {
                String errorMsg = "Encrypted metadata file is empty";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Deserialize encrypted metadata
            System.out.println("🔍 Debug: Deserializing encrypted metadata...");
            CryptoManager.EncryptedData encryptedData = deserializeEncryptedMetadata(encryptedBytes);
            
            if (encryptedData == null) {
                String errorMsg = "Failed to deserialize encrypted metadata - data format invalid";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Decrypt the metadata
            System.out.println("🔍 Debug: Decrypting metadata with CryptoManager...");
            byte[] decryptedBytes = cryptoManager.decryptWithPassword(encryptedData, password);
            
            if (decryptedBytes == null || decryptedBytes.length == 0) {
                String errorMsg = "Decryption failed - returned null or empty data. Check if password is correct.";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("🔍 Debug: Decrypted " + decryptedBytes.length + " bytes");
            
            String metadata = new String(decryptedBytes, "UTF-8");
            
            // Validate decrypted metadata is valid JSON
            if (!isValidJson(metadata)) {
                String errorMsg = "Decrypted metadata is not valid JSON - decryption may have failed or data is corrupted";
                System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("✅ Metadata decrypted and loaded successfully from: " + encryptedMetadataFile);
            System.out.println("🔍 Debug: Decrypted metadata length: " + metadata.length() + " characters");
            
            return metadata;
            
        } catch (IOException e) {
            String errorMsg = "Failed to read encrypted metadata file: " + e.getMessage();
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            System.err.println("🔍 Debug: File path: " + encryptedMetadataFile);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Metadata decryption failed: " + e.getMessage();
            System.err.println("❌ EncryptedMetadataManager: " + errorMsg);
            System.err.println("🔍 Debug: Exception type: " + e.getClass().getSimpleName());
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Check if encrypted metadata exists
     * @return true if encrypted metadata file exists
     */
    public boolean hasEncryptedMetadata() {
        return Files.exists(encryptedMetadataFile);
    }
    
    /**
     * Check if plain text metadata exists and needs migration
     * @return true if migration is needed
     */
    public boolean needsMigration() {
        return Files.exists(plainMetadataFile) && !Files.exists(encryptedMetadataFile);
    }
    
    /**
     * Migrate plain text metadata to encrypted format
     * @param password The password for encryption
     * @return true if migration successful
     */
    public boolean migrateFromPlainText(String password) {
        try {
            if (!Files.exists(plainMetadataFile)) {
                return false;
            }
            
            System.out.println("🔄 Migrating metadata from plain text to encrypted format...");
            
            // Read plain text metadata
            String plainMetadata = new String(Files.readAllBytes(plainMetadataFile), "UTF-8");
            
            // Validate JSON format
            if (!isValidJson(plainMetadata)) {
                System.err.println("❌ Invalid JSON format in plain text metadata");
                return false;
            }
            
            // Save as encrypted
            saveEncryptedMetadata(plainMetadata, password);
            
            System.out.println("✅ Metadata migration completed successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Metadata migration failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate JSON format
     * @param json The JSON string to validate
     * @return true if valid JSON
     */
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Serialize encrypted metadata with format validation
     * Format: [MAGIC_BYTES][SALT][IV][CIPHERTEXT]
     */
    private byte[] serializeEncryptedMetadata(CryptoManager.EncryptedData encryptedData) {
        byte[] salt = encryptedData.getSalt();
        byte[] iv = encryptedData.getIv();
        byte[] ciphertext = encryptedData.getCiphertext();
        
        if (salt == null || iv == null || ciphertext == null) {
            throw new IllegalArgumentException("Invalid encrypted data");
        }
        
        // Calculate total size
        int totalSize = SecurityConfiguration.MAGIC_BYTES.length + salt.length + iv.length + ciphertext.length;
        byte[] result = new byte[totalSize];
        int offset = 0;
        
        // Magic bytes for format identification
        System.arraycopy(SecurityConfiguration.MAGIC_BYTES, 0, result, offset, SecurityConfiguration.MAGIC_BYTES.length);
        offset += SecurityConfiguration.MAGIC_BYTES.length;
        
        // Salt
        System.arraycopy(salt, 0, result, offset, salt.length);
        offset += salt.length;
        
        // IV
        System.arraycopy(iv, 0, result, offset, iv.length);
        offset += iv.length;
        
        // Ciphertext
        System.arraycopy(ciphertext, 0, result, offset, ciphertext.length);
        
        return result;
    }
    
    /**
     * Deserialize encrypted metadata with format validation
     */
    private CryptoManager.EncryptedData deserializeEncryptedMetadata(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        
        int minSize = SecurityConfiguration.MAGIC_BYTES.length + 
                     SecurityConfiguration.SALT_LENGTH + 
                     SecurityConfiguration.IV_LENGTH + 
                     SecurityConfiguration.GCM_TAG_LENGTH;
        
        if (data.length < minSize) {
            throw new IllegalArgumentException("Data too small for encrypted metadata format");
        }
        
        int offset = 0;
        
        // Verify magic bytes
        byte[] magic = new byte[SecurityConfiguration.MAGIC_BYTES.length];
        System.arraycopy(data, offset, magic, 0, SecurityConfiguration.MAGIC_BYTES.length);
        if (!Arrays.equals(magic, SecurityConfiguration.MAGIC_BYTES)) {
            throw new IllegalArgumentException("Invalid encrypted metadata format - magic bytes mismatch");
        }
        offset += SecurityConfiguration.MAGIC_BYTES.length;
        
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
        
        return new CryptoManager.EncryptedData(salt, iv, ciphertext);
    }
    
    /**
     * Check if data represents encrypted metadata format
     * @param data The data to check
     * @return true if data appears to be encrypted metadata
     */
    public static boolean isEncryptedMetadataFormat(byte[] data) {
        if (data == null || data.length < SecurityConfiguration.MAGIC_BYTES.length) {
            return false;
        }
        
        // Check magic bytes
        for (int i = 0; i < SecurityConfiguration.MAGIC_BYTES.length; i++) {
            if (data[i] != SecurityConfiguration.MAGIC_BYTES[i]) {
                return false;
            }
        }
        
        // Check minimum size
        int minSize = SecurityConfiguration.MAGIC_BYTES.length + 
                     SecurityConfiguration.SALT_LENGTH + 
                     SecurityConfiguration.IV_LENGTH + 
                     SecurityConfiguration.GCM_TAG_LENGTH;
        
        return data.length >= minSize;
    }
    
    /**
     * Get the size of encrypted metadata file
     * @return file size in bytes, or -1 if file doesn't exist
     */
    public long getEncryptedMetadataSize() {
        try {
            if (Files.exists(encryptedMetadataFile)) {
                return Files.size(encryptedMetadataFile);
            }
        } catch (IOException e) {
            System.err.println("❌ Error getting metadata file size: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Verify metadata integrity by attempting to decrypt
     * @param password The password to test
     * @return true if metadata can be decrypted successfully
     */
    public boolean verifyMetadataIntegrity(String password) {
        try {
            String metadata = loadEncryptedMetadata(password);
            return metadata != null && isValidJson(metadata);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clear sensitive data from memory using SecureMemoryManager
     */
    public void clearSensitiveData() {
        // Trigger secure memory cleanup
        SecureMemoryManager.getInstance().cleanupAllTrackedData();
        
        System.out.println("🧹 Cleared sensitive encrypted metadata from memory");
    }
}