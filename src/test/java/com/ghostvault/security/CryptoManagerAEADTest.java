package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AES-GCM AEAD CryptoManager implementation
 */
@DisplayName("CryptoManager AEAD Tests")
class CryptoManagerAEADTest {
    
    private CryptoManager cryptoManager;
    private SecretKey testKey;
    
    @BeforeEach
    void setUp() {
        cryptoManager = new CryptoManager();
        // Generate a test key
        byte[] keyBytes = cryptoManager.generateSecureRandom(32);
        testKey = cryptoManager.keyFromBytes(keyBytes);
    }
    
    @Test
    @DisplayName("Should encrypt and decrypt data successfully (round-trip)")
    void testEncryptDecryptRoundTrip() throws GeneralSecurityException {
        // Arrange
        String plaintext = "This is a secret message that needs protection!";
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        
        // Act
        byte[] ciphertext = cryptoManager.encrypt(plaintextBytes, testKey, null);
        byte[] decrypted = cryptoManager.decrypt(ciphertext, testKey, null);
        String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
        
        // Assert
        assertEquals(plaintext, decryptedText);
        assertFalse(Arrays.equals(plaintextBytes, ciphertext));
    }
    
    @Test
    @DisplayName("Should detect tampering with authentication tag")
    void testTamperDetection() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        
        // Act - Tamper with the ciphertext (flip a bit in the middle)
        byte[] tamperedCiphertext = ciphertext.clone();
        int tamperIndex = ciphertext.length / 2;
        tamperedCiphertext[tamperIndex] ^= 0x01; // Flip one bit
        
        // Assert - Should throw exception on tampered data
        assertThrows(AEADBadTagException.class, () -> {
            cryptoManager.decrypt(tamperedCiphertext, testKey, null);
        });
    }
    
    @Test
    @DisplayName("Should detect tampering with IV")
    void testIVTamperDetection() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        
        // Act - Tamper with the IV (first 12 bytes)
        byte[] tamperedCiphertext = ciphertext.clone();
        tamperedCiphertext[5] ^= 0x01; // Flip bit in IV
        
        // Assert - Should fail authentication
        assertThrows(GeneralSecurityException.class, () -> {
            cryptoManager.decrypt(tamperedCiphertext, testKey, null);
        });
    }
    
    @Test
    @DisplayName("Should use unique IV for each encryption")
    void testUniqueIVs() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Same message".getBytes(StandardCharsets.UTF_8);
        
        // Act - Encrypt same message twice
        byte[] ciphertext1 = cryptoManager.encrypt(plaintext, testKey, null);
        byte[] ciphertext2 = cryptoManager.encrypt(plaintext, testKey, null);
        
        // Assert - Ciphertexts should be different (due to different IVs)
        assertFalse(Arrays.equals(ciphertext1, ciphertext2));
        
        // But both should decrypt to same plaintext
        byte[] decrypted1 = cryptoManager.decrypt(ciphertext1, testKey, null);
        byte[] decrypted2 = cryptoManager.decrypt(ciphertext2, testKey, null);
        assertArrayEquals(decrypted1, decrypted2);
    }
    
    @Test
    @DisplayName("Should support Additional Authenticated Data (AAD)")
    void testAAD() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Secret message".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "user123@example.com".getBytes(StandardCharsets.UTF_8);
        
        // Act
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, aad);
        byte[] decrypted = cryptoManager.decrypt(ciphertext, testKey, aad);
        
        // Assert
        assertArrayEquals(plaintext, decrypted);
    }
    
    @Test
    @DisplayName("Should fail decryption with wrong AAD")
    void testWrongAAD() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Secret message".getBytes(StandardCharsets.UTF_8);
        byte[] correctAAD = "user123@example.com".getBytes(StandardCharsets.UTF_8);
        byte[] wrongAAD = "user456@example.com".getBytes(StandardCharsets.UTF_8);
        
        // Act
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, correctAAD);
        
        // Assert - Should fail with wrong AAD
        assertThrows(AEADBadTagException.class, () -> {
            cryptoManager.decrypt(ciphertext, testKey, wrongAAD);
        });
    }
    
    @Test
    @DisplayName("Should fail decryption with wrong key")
    void testWrongKey() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Secret message".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        
        // Create different key
        byte[] wrongKeyBytes = cryptoManager.generateSecureRandom(32);
        SecretKey wrongKey = cryptoManager.keyFromBytes(wrongKeyBytes);
        
        // Assert - Should fail with wrong key
        assertThrows(AEADBadTagException.class, () -> {
            cryptoManager.decrypt(ciphertext, wrongKey, null);
        });
    }
    
    @Test
    @DisplayName("Should handle empty plaintext")
    void testEmptyPlaintext() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = new byte[0];
        
        // Act
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        byte[] decrypted = cryptoManager.decrypt(ciphertext, testKey, null);
        
        // Assert
        assertArrayEquals(plaintext, decrypted);
        assertEquals(0, decrypted.length);
    }
    
    @Test
    @DisplayName("Should handle large data")
    void testLargeData() throws GeneralSecurityException {
        // Arrange - 1MB of data
        byte[] plaintext = new byte[1024 * 1024];
        Arrays.fill(plaintext, (byte) 0x42);
        
        // Act
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        byte[] decrypted = cryptoManager.decrypt(ciphertext, testKey, null);
        
        // Assert
        assertArrayEquals(plaintext, decrypted);
    }
    
    @Test
    @DisplayName("Should properly zeroize sensitive data")
    void testZeroize() {
        // Arrange
        byte[] sensitiveData = "password123".getBytes(StandardCharsets.UTF_8);
        byte[] copy = sensitiveData.clone();
        
        // Act
        cryptoManager.zeroize(sensitiveData);
        
        // Assert
        assertFalse(Arrays.equals(copy, sensitiveData));
        for (byte b : sensitiveData) {
            assertEquals(0, b);
        }
    }
    
    @Test
    @DisplayName("Should calculate SHA-256 hash correctly")
    void testSHA256() throws GeneralSecurityException {
        // Arrange
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        // Act
        String hash1 = cryptoManager.calculateSHA256(data);
        String hash2 = cryptoManager.calculateSHA256(data);
        
        // Assert
        assertEquals(hash1, hash2); // Same input = same hash
        assertEquals(64, hash1.length()); // SHA-256 = 64 hex chars
        assertTrue(hash1.matches("[0-9a-f]{64}")); // Valid hex
    }
    
    @Test
    @DisplayName("Should work with EncryptedData wrapper")
    void testEncryptedDataWrapper() throws GeneralSecurityException {
        // Arrange
        cryptoManager.initializeWithKey(testKey);
        byte[] plaintext = "Test message".getBytes(StandardCharsets.UTF_8);
        
        // Act
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(plaintext);
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        // Assert
        assertArrayEquals(plaintext, decrypted);
    }
    
    @Test
    @DisplayName("Should serialize and deserialize EncryptedData")
    void testEncryptedDataSerialization() throws GeneralSecurityException {
        // Arrange
        byte[] plaintext = "Test message".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cryptoManager.encrypt(plaintext, testKey, null);
        
        // Act - Convert to EncryptedData and back
        CryptoManager.EncryptedData encData = CryptoManager.EncryptedData.fromCombinedData(ciphertext);
        byte[] combined = encData.getCombinedData();
        
        // Assert
        assertArrayEquals(ciphertext, combined);
        
        // Should still decrypt correctly
        byte[] decrypted = cryptoManager.decrypt(combined, testKey, null);
        assertArrayEquals(plaintext, decrypted);
    }
}
