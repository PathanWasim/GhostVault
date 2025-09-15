package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Basic test for CryptoManager functionality without JUnit dependencies
 */
public class CryptoManagerBasicTest {
    
    public static void main(String[] args) {
        CryptoManagerBasicTest test = new CryptoManagerBasicTest();
        
        try {
            test.testKeyDerivation();
            test.testEncryptionDecryption();
            test.testSHA256Hashing();
            test.testSecureRandom();
            test.testSecureWipe();
            test.testHMACAuthentication();
            
            System.out.println("✅ All CryptoManager tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void testKeyDerivation() throws GeneralSecurityException {
        System.out.println("Testing key derivation...");
        
        CryptoManager cryptoManager = new CryptoManager();
        byte[] salt = cryptoManager.generateSalt();
        String password = "TestPassword123!";
        
        SecretKey key1 = cryptoManager.deriveKey(password, salt);
        SecretKey key2 = cryptoManager.deriveKey(password, salt);
        
        assert key1 != null : "Derived key should not be null";
        assert key2 != null : "Derived key should not be null";
        assert "AES".equals(key1.getAlgorithm()) : "Algorithm should be AES";
        assert Arrays.equals(key1.getEncoded(), key2.getEncoded()) : "Same password and salt should produce same key";
        
        // Test different passwords produce different keys
        SecretKey key3 = cryptoManager.deriveKey("DifferentPassword", salt);
        assert !Arrays.equals(key1.getEncoded(), key3.getEncoded()) : "Different passwords should produce different keys";
        
        System.out.println("✓ Key derivation test passed");
    }
    
    private void testEncryptionDecryption() throws GeneralSecurityException {
        System.out.println("Testing encryption/decryption...");
        
        CryptoManager cryptoManager = new CryptoManager();
        byte[] salt = cryptoManager.generateSalt();
        String password = "TestPassword123!";
        
        cryptoManager.initializeWithPassword(password, salt);
        
        String testData = "This is sensitive test data that needs encryption!";
        byte[] plaintext = testData.getBytes();
        
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(plaintext);
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        assert encrypted != null : "Encrypted data should not be null";
        assert decrypted != null : "Decrypted data should not be null";
        assert Arrays.equals(plaintext, decrypted) : "Decrypted data should match original";
        assert testData.equals(new String(decrypted)) : "Decrypted string should match original";
        
        // Test that each encryption produces unique IVs
        CryptoManager.EncryptedData encrypted2 = cryptoManager.encrypt(plaintext);
        assert !Arrays.equals(encrypted.getIv(), encrypted2.getIv()) : "Each encryption should produce unique IV";
        
        cryptoManager.clearKeys();
        System.out.println("✓ Encryption/decryption test passed");
    }
    
    private void testSHA256Hashing() throws GeneralSecurityException {
        System.out.println("Testing SHA-256 hashing...");
        
        CryptoManager cryptoManager = new CryptoManager();
        String testData = "Test data for hashing";
        byte[] data = testData.getBytes();
        
        String hash1 = cryptoManager.calculateSHA256(data);
        String hash2 = cryptoManager.calculateSHA256(data);
        
        assert hash1 != null : "Hash should not be null";
        assert hash1.length() == 64 : "Hash should be 64 characters (32 bytes in hex)";
        assert hash1.equals(hash2) : "Same data should produce same hash";
        
        // Test different data produces different hash
        String hash3 = cryptoManager.calculateSHA256("Different data".getBytes());
        assert !hash1.equals(hash3) : "Different data should produce different hash";
        
        System.out.println("✓ SHA-256 hashing test passed");
    }
    
    private void testSecureRandom() {
        System.out.println("Testing secure random generation...");
        
        CryptoManager cryptoManager = new CryptoManager();
        
        byte[] random1 = cryptoManager.generateSecureRandom(32);
        byte[] random2 = cryptoManager.generateSecureRandom(32);
        
        assert random1 != null : "Random bytes should not be null";
        assert random2 != null : "Random bytes should not be null";
        assert random1.length == 32 : "Should generate requested length";
        assert random2.length == 32 : "Should generate requested length";
        assert !Arrays.equals(random1, random2) : "Should generate different random values";
        
        // Test salt generation
        byte[] salt1 = cryptoManager.generateSalt();
        byte[] salt2 = cryptoManager.generateSalt();
        
        assert salt1 != null : "Salt should not be null";
        assert salt2 != null : "Salt should not be null";
        assert salt1.length == AppConfig.SALT_SIZE : "Salt should be correct size";
        assert salt2.length == AppConfig.SALT_SIZE : "Salt should be correct size";
        assert !Arrays.equals(salt1, salt2) : "Should generate different salts";
        
        System.out.println("✓ Secure random generation test passed");
    }
    
    private void testSecureWipe() {
        System.out.println("Testing secure wipe...");
        
        CryptoManager cryptoManager = new CryptoManager();
        
        byte[] sensitiveData = "Sensitive information".getBytes();
        byte[] originalData = sensitiveData.clone();
        
        cryptoManager.secureWipe(sensitiveData);
        
        assert !Arrays.equals(originalData, sensitiveData) : "Data should be wiped";
        
        // Verify all bytes are zero
        for (byte b : sensitiveData) {
            assert b == 0 : "All bytes should be zero after wipe";
        }
        
        System.out.println("✓ Secure wipe test passed");
    }
    
    private void testHMACAuthentication() throws GeneralSecurityException {
        System.out.println("Testing HMAC authentication...");
        
        CryptoManager cryptoManager = new CryptoManager();
        byte[] salt = cryptoManager.generateSalt();
        String password = "TestPassword123!";
        
        cryptoManager.initializeWithPassword(password, salt);
        
        byte[] testData = "Test data for HMAC".getBytes();
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData);
        
        assert encrypted.getHmac() != null : "HMAC should be present";
        assert encrypted.getHmac().length == 32 : "HMAC should be 32 bytes (SHA-256)";
        
        // Should decrypt successfully with valid HMAC
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        assert Arrays.equals(testData, decrypted) : "Decryption with valid HMAC should succeed";
        
        // Test combined data format
        byte[] combined = encrypted.getCombinedData();
        CryptoManager.EncryptedData restored = CryptoManager.EncryptedData.fromCombinedData(combined);
        
        assert Arrays.equals(encrypted.getIv(), restored.getIv()) : "IV should be preserved";
        assert Arrays.equals(encrypted.getCiphertext(), restored.getCiphertext()) : "Ciphertext should be preserved";
        assert Arrays.equals(encrypted.getHmac(), restored.getHmac()) : "HMAC should be preserved";
        
        // Should decrypt successfully
        byte[] decrypted2 = cryptoManager.decrypt(restored);
        assert Arrays.equals(testData, decrypted2) : "Decrypted data should match original";
        
        cryptoManager.clearKeys();
        System.out.println("✓ HMAC authentication test passed");
    }
}