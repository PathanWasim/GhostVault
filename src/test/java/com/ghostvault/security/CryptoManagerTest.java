package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Unit tests for CryptoManager
 */
public class CryptoManagerTest {
    
    private CryptoManager cryptoManager;
    private final String testPassword = "TestPassword123!";
    private final String testData = "This is sensitive test data that needs encryption!";
    
    @BeforeEach
    void setUp() {
        cryptoManager = new CryptoManager();
    }
    
    @Test
    @DisplayName("Should generate unique salts")
    void shouldGenerateUniqueSalts() {
        byte[] salt1 = cryptoManager.generateSalt();
        byte[] salt2 = cryptoManager.generateSalt();
        
        assertEquals(SecurityConfiguration.SALT_LENGTH, salt1.length);
        assertEquals(SecurityConfiguration.SALT_LENGTH, salt2.length);
        assertFalse(Arrays.equals(salt1, salt2), "Salts should be unique");
    }
    
    @Test
    @DisplayName("Should hash passwords with different salts producing different hashes")
    void shouldHashPasswordsWithDifferentSalts() throws Exception {
        byte[] salt1 = cryptoManager.generateSalt();
        byte[] salt2 = cryptoManager.generateSalt();
        
        byte[] hash1 = cryptoManager.hashPassword(testPassword, salt1);
        byte[] hash2 = cryptoManager.hashPassword(testPassword, salt2);
        
        assertEquals(SecurityConfiguration.KEY_LENGTH / 8, hash1.length);
        assertEquals(SecurityConfiguration.KEY_LENGTH / 8, hash2.length);
        assertFalse(Arrays.equals(hash1, hash2), "Different salts should produce different hashes");
    }
    
    @Test
    @DisplayName("Should verify passwords correctly")
    void shouldVerifyPasswordsCorrectly() throws Exception {
        byte[] salt = cryptoManager.generateSalt();
        byte[] hash = cryptoManager.hashPassword(testPassword, salt);
        
        assertTrue(cryptoManager.verifyPassword(testPassword, salt, hash));
        assertFalse(cryptoManager.verifyPassword("WrongPassword", salt, hash));
    }
    
    @Test
    @DisplayName("Should derive consistent encryption keys")
    void shouldDeriveConsistentEncryptionKeys() throws Exception {
        byte[] salt = cryptoManager.generateSalt();
        
        SecretKey key1 = cryptoManager.deriveEncryptionKey(testPassword, salt);
        SecretKey key2 = cryptoManager.deriveEncryptionKey(testPassword, salt);
        
        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
        assertEquals(SecurityConfiguration.AES_ALGORITHM, key1.getAlgorithm());
    }
    
    @Test
    @DisplayName("Should encrypt and decrypt data with password")
    void shouldEncryptAndDecryptDataWithPassword() throws Exception {
        byte[] originalData = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encryptWithPassword(originalData, testPassword);
        byte[] decrypted = cryptoManager.decryptWithPassword(encrypted, testPassword);
        
        assertArrayEquals(originalData, decrypted);
        assertNotNull(encrypted.getSalt());
        assertNotNull(encrypted.getIv());
        assertNotNull(encrypted.getCiphertext());
        assertEquals(SecurityConfiguration.SALT_LENGTH, encrypted.getSalt().length);
        assertEquals(SecurityConfiguration.IV_LENGTH, encrypted.getIv().length);
    }
    
    @Test
    @DisplayName("Should fail decryption with wrong password")
    void shouldFailDecryptionWithWrongPassword() throws Exception {
        byte[] originalData = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encryptWithPassword(originalData, testPassword);
        
        assertThrows(Exception.class, () -> {
            cryptoManager.decryptWithPassword(encrypted, "WrongPassword");
        });
    }
    
    @Test
    @DisplayName("Should produce different ciphertext for same data")
    void shouldProduceDifferentCiphertextForSameData() throws Exception {
        byte[] originalData = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted1 = cryptoManager.encryptWithPassword(originalData, testPassword);
        CryptoManager.EncryptedData encrypted2 = cryptoManager.encryptWithPassword(originalData, testPassword);
        
        // Different salts and IVs should produce different ciphertext
        assertFalse(Arrays.equals(encrypted1.getSalt(), encrypted2.getSalt()));
        assertFalse(Arrays.equals(encrypted1.getIv(), encrypted2.getIv()));
        assertFalse(Arrays.equals(encrypted1.getCiphertext(), encrypted2.getCiphertext()));
        
        // But both should decrypt to the same original data
        byte[] decrypted1 = cryptoManager.decryptWithPassword(encrypted1, testPassword);
        byte[] decrypted2 = cryptoManager.decryptWithPassword(encrypted2, testPassword);
        assertArrayEquals(originalData, decrypted1);
        assertArrayEquals(originalData, decrypted2);
    }
    
    @Test
    @DisplayName("Should serialize and deserialize encrypted data")
    void shouldSerializeAndDeserializeEncryptedData() throws Exception {
        byte[] originalData = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encryptWithPassword(originalData, testPassword);
        byte[] serialized = encrypted.toByteArray();
        CryptoManager.EncryptedData deserialized = CryptoManager.EncryptedData.fromByteArray(serialized);
        
        byte[] decrypted = cryptoManager.decryptWithPassword(deserialized, testPassword);
        assertArrayEquals(originalData, decrypted);
    }
    
    @Test
    @DisplayName("Should handle null and empty inputs gracefully")
    void shouldHandleNullAndEmptyInputsGracefully() {
        assertThrows(IllegalArgumentException.class, () -> {
            cryptoManager.hashPassword(null, cryptoManager.generateSalt());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cryptoManager.hashPassword("", cryptoManager.generateSalt());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cryptoManager.hashPassword(testPassword, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cryptoManager.encryptWithPassword(null, testPassword);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cryptoManager.encryptWithPassword(testData.getBytes(), null);
        });
    }
    
    @Test
    @DisplayName("Should securely wipe byte arrays")
    void shouldSecurelyWipeByteArrays() {
        byte[] sensitiveData = testData.getBytes();
        byte[] originalCopy = sensitiveData.clone();
        
        cryptoManager.secureWipe(sensitiveData);
        
        // Data should be different after wiping
        assertFalse(Arrays.equals(originalCopy, sensitiveData));
        
        // Should handle null gracefully
        assertDoesNotThrow(() -> cryptoManager.secureWipe((byte[]) null));
    }
    
    @Test
    @DisplayName("Should securely wipe SecretKeys")
    void shouldSecurelyWipeSecretKeys() throws Exception {
        byte[] salt = cryptoManager.generateSalt();
        SecretKey key = cryptoManager.deriveEncryptionKey(testPassword, salt);
        
        // Should not throw exception
        assertDoesNotThrow(() -> cryptoManager.secureWipe(key));
        assertDoesNotThrow(() -> cryptoManager.secureWipe((SecretKey) null));
    }
    
    @Test
    @DisplayName("Should validate security configuration")
    void shouldValidateSecurityConfiguration() {
        assertTrue(SecurityConfiguration.validateConfiguration());
    }
}