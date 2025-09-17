package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for CryptoManager
 * Tests AES-256-CBC encryption, PBKDF2 key derivation, SHA-256 hashing, and security features
 */
public class CryptoManagerTest {
    
    private CryptoManager cryptoManager;
    private byte[] testSalt;
    private String testPassword;
    
    @BeforeEach
    public void setUp() {
        cryptoManager = new CryptoManager();
        testSalt = cryptoManager.generateSalt();
        testPassword = "TestPassword123!";
    }
    
    @AfterEach
    public void tearDown() {
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        // Secure wipe test data
        if (testSalt != null) {
            cryptoManager.secureWipe(testSalt);
        }
    }
    
    @Test
    public void testKeyDerivation() throws GeneralSecurityException {
        SecretKey key1 = cryptoManager.deriveKey(testPassword, testSalt);
        SecretKey key2 = cryptoManager.deriveKey(testPassword, testSalt);
        
        assertNotNull(key1, "Derived key should not be null");
        assertNotNull(key2, "Derived key should not be null");
        assertEquals("AES", key1.getAlgorithm(), "Algorithm should be AES");
        assertArrayEquals(key1.getEncoded(), key2.getEncoded(), 
                         "Same password and salt should produce same key");
    }
    
    @Test
    public void testKeyDerivationDifferentPasswords() throws GeneralSecurityException {
        SecretKey key1 = cryptoManager.deriveKey("password1", testSalt);
        SecretKey key2 = cryptoManager.deriveKey("password2", testSalt);
        
        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()),
                   "Different passwords should produce different keys");
    }
    
    @Test
    public void testKeyDerivationDifferentSalts() throws GeneralSecurityException {
        byte[] salt1 = cryptoManager.generateSalt();
        byte[] salt2 = cryptoManager.generateSalt();
        
        SecretKey key1 = cryptoManager.deriveKey(testPassword, salt1);
        SecretKey key2 = cryptoManager.deriveKey(testPassword, salt2);
        
        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()),
                   "Different salts should produce different keys");
    }
    
    @Test
    public void testEncryptionDecryption() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        String testData = "This is sensitive test data that needs encryption!";
        byte[] plaintext = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(plaintext);
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        assertNotNull("Encrypted data should not be null", encrypted);
        assertNotNull("Decrypted data should not be null", decrypted);
        assertArrayEquals("Decrypted data should match original", plaintext, decrypted);
        assertEquals("Decrypted string should match original", testData, new String(decrypted));
    }
    
    @Test
    public void testEncryptionProducesUniqueIVs() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        byte[] testData = "Test data".getBytes();
        
        CryptoManager.EncryptedData encrypted1 = cryptoManager.encrypt(testData);
        CryptoManager.EncryptedData encrypted2 = cryptoManager.encrypt(testData);
        
        assertFalse("Each encryption should produce unique IV",
                   Arrays.equals(encrypted1.getIv(), encrypted2.getIv()));
        assertFalse("Each encryption should produce different ciphertext",
                   Arrays.equals(encrypted1.getCiphertext(), encrypted2.getCiphertext()));
    }
    
    @Test
    public void testHMACAuthentication() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        byte[] testData = "Test data for HMAC".getBytes();
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData);
        
        assertNotNull("HMAC should be present", encrypted.getHmac());
        assertEquals("HMAC should be 32 bytes (SHA-256)", 32, encrypted.getHmac().length);
        
        // Should decrypt successfully with valid HMAC
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        assertArrayEquals("Decryption with valid HMAC should succeed", testData, decrypted);
    }
    
    @Test
    @DisplayName("Test HMAC tamper detection")
    public void testHMACTamperDetection() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        byte[] testData = "Test data for tamper detection".getBytes();
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData);
        
        // Tamper with the ciphertext
        byte[] tamperedCiphertext = encrypted.getCiphertext();
        tamperedCiphertext[0] ^= 1; // Flip one bit
        
        CryptoManager.EncryptedData tamperedData = new CryptoManager.EncryptedData(
            tamperedCiphertext, encrypted.getIv(), encrypted.getHmac());
        
        // Should throw exception due to HMAC mismatch
        assertThrows(GeneralSecurityException.class, () -> {
            cryptoManager.decrypt(tamperedData);
        });
    }
    
    @Test
    public void testSHA256Hashing() throws GeneralSecurityException {
        String testData = "Test data for hashing";
        byte[] data = testData.getBytes();
        
        String hash1 = cryptoManager.calculateSHA256(data);
        String hash2 = cryptoManager.calculateSHA256(data);
        
        assertNotNull("Hash should not be null", hash1);
        assertEquals("Hash should be 64 characters (32 bytes in hex)", 64, hash1.length());
        assertEquals("Same data should produce same hash", hash1, hash2);
        
        // Test different data produces different hash
        String hash3 = cryptoManager.calculateSHA256("Different data".getBytes());
        assertNotEquals("Different data should produce different hash", hash1, hash3);
    }
    
    @Test
    public void testSecureRandomGeneration() {
        byte[] random1 = cryptoManager.generateSecureRandom(32);
        byte[] random2 = cryptoManager.generateSecureRandom(32);
        
        assertNotNull("Random bytes should not be null", random1);
        assertNotNull("Random bytes should not be null", random2);
        assertEquals("Should generate requested length", 32, random1.length);
        assertEquals("Should generate requested length", 32, random2.length);
        assertFalse("Should generate different random values", Arrays.equals(random1, random2));
    }
    
    @Test
    public void testSaltGeneration() {
        byte[] salt1 = cryptoManager.generateSalt();
        byte[] salt2 = cryptoManager.generateSalt();
        
        assertNotNull("Salt should not be null", salt1);
        assertNotNull("Salt should not be null", salt2);
        assertEquals("Salt should be correct size", AppConfig.SALT_SIZE, salt1.length);
        assertEquals("Salt should be correct size", AppConfig.SALT_SIZE, salt2.length);
        assertFalse("Should generate different salts", Arrays.equals(salt1, salt2));
    }
    
    @Test
    public void testSecureWipe() {
        byte[] sensitiveData = "Sensitive information".getBytes();
        byte[] originalData = sensitiveData.clone();
        
        cryptoManager.secureWipe(sensitiveData);
        
        assertFalse("Data should be wiped", Arrays.equals(originalData, sensitiveData));
        
        // Verify all bytes are zero
        for (byte b : sensitiveData) {
            assertEquals("All bytes should be zero after wipe", 0, b);
        }
    }
    
    @Test
    public void testClearKeys() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        // Verify keys are initialized
        assertNotNull("Master key should be available", cryptoManager.getMasterKey());
        
        cryptoManager.clearKeys();
        
        // Keys should be cleared
        assertNull("Master key should be null after clearing", cryptoManager.getMasterKey());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEncryptionWithoutInitialization() throws GeneralSecurityException {
        byte[] testData = "Test data".getBytes();
        cryptoManager.encrypt(testData);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDecryptionWithoutInitialization() throws GeneralSecurityException {
        // Create dummy encrypted data
        CryptoManager.EncryptedData dummyData = new CryptoManager.EncryptedData(
            new byte[16], new byte[16]);
        cryptoManager.decrypt(dummyData);
    }
    
    @Test
    public void testEncryptedDataCombinedFormat() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        byte[] testData = "Test data for combined format".getBytes();
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData);
        
        // Test combined data format
        byte[] combined = encrypted.getCombinedData();
        CryptoManager.EncryptedData restored = CryptoManager.EncryptedData.fromCombinedData(combined);
        
        assertArrayEquals("IV should be preserved", encrypted.getIv(), restored.getIv());
        assertArrayEquals("Ciphertext should be preserved", encrypted.getCiphertext(), restored.getCiphertext());
        assertArrayEquals("HMAC should be preserved", encrypted.getHmac(), restored.getHmac());
        
        // Should decrypt successfully
        byte[] decrypted = cryptoManager.decrypt(restored);
        assertArrayEquals("Decrypted data should match original", testData, decrypted);
    }
    
    @Test
    public void testLargeDataEncryption() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        // Test with 1MB of data
        byte[] largeData = new byte[1024 * 1024];
        Arrays.fill(largeData, (byte) 0x42);
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(largeData);
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        assertArrayEquals("Large data should encrypt/decrypt correctly", largeData, decrypted);
    }
    
    @Test
    public void testEmptyDataEncryption() throws GeneralSecurityException {
        cryptoManager.initializeWithPassword(testPassword, testSalt);
        
        byte[] emptyData = new byte[0];
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(emptyData);
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        assertArrayEquals("Empty data should encrypt/decrypt correctly", emptyData, decrypted);
    }
}