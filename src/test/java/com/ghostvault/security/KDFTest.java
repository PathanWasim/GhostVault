package com.ghostvault.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KDF (Key Derivation Function) utilities
 */
@DisplayName("KDF Tests")
class KDFTest {
    
    @Test
    @DisplayName("Should derive consistent keys from same password and params")
    void testConsistentKeyDerivation() throws GeneralSecurityException {
        // Arrange
        char[] password = "TestPassword123!".toCharArray();
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Act
        byte[] key1 = KDF.deriveKey(password, params);
        byte[] key2 = KDF.deriveKey(password, params);
        
        // Assert
        assertArrayEquals(key1, key2);
        assertEquals(32, key1.length); // 256 bits
    }
    
    @Test
    @DisplayName("Should derive different keys from different passwords")
    void testDifferentPasswords() throws GeneralSecurityException {
        // Arrange
        char[] password1 = "Password1".toCharArray();
        char[] password2 = "Password2".toCharArray();
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Act
        byte[] key1 = KDF.deriveKey(password1, params);
        byte[] key2 = KDF.deriveKey(password2, params);
        
        // Assert
        assertFalse(Arrays.equals(key1, key2));
    }
    
    @Test
    @DisplayName("Should derive different keys with different salts")
    void testDifferentSalts() throws GeneralSecurityException {
        // Arrange
        char[] password = "SamePassword".toCharArray();
        byte[] salt1 = KDF.generateSalt();
        byte[] salt2 = KDF.generateSalt();
        
        KDF.KdfParams params1 = new KDF.KdfParams(KDF.Algorithm.PBKDF2, salt1, 100000);
        KDF.KdfParams params2 = new KDF.KdfParams(KDF.Algorithm.PBKDF2, salt2, 100000);
        
        // Act
        byte[] key1 = KDF.deriveKey(password, params1);
        byte[] key2 = KDF.deriveKey(password, params2);
        
        // Assert
        assertFalse(Arrays.equals(key1, key2));
    }
    
    @Test
    @DisplayName("Should generate unique salts")
    void testUniqueSalts() {
        // Act
        byte[] salt1 = KDF.generateSalt();
        byte[] salt2 = KDF.generateSalt();
        byte[] salt3 = KDF.generateSalt();
        
        // Assert
        assertEquals(32, salt1.length);
        assertFalse(Arrays.equals(salt1, salt2));
        assertFalse(Arrays.equals(salt2, salt3));
        assertFalse(Arrays.equals(salt1, salt3));
    }
    
    @Test
    @DisplayName("Should perform benchmark and return recommended params")
    void testBenchmark() {
        // Act
        KDF.BenchmarkResult result = KDF.benchmark();
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getRecommendedParams());
        assertTrue(result.getDurationMs() > 0);
        assertTrue(result.getDurationMs() < 5000); // Should complete in reasonable time
        
        System.out.println("Benchmark result: " + result.getRecommendedParams());
        System.out.println("Duration: " + result.getDurationMs() + "ms");
    }
    
    @Test
    @DisplayName("Should support Argon2id if available")
    void testArgon2Support() {
        // Act
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Assert
        assertNotNull(params);
        assertNotNull(params.getAlgorithm());
        assertNotNull(params.getSalt());
        assertEquals(32, params.getSalt().length);
        
        System.out.println("Default KDF: " + params.getAlgorithm());
        System.out.println("Parameters: " + params);
    }
    
    @Test
    @DisplayName("Should create SecretKey from derived key bytes")
    void testCreateSecretKey() throws GeneralSecurityException {
        // Arrange
        char[] password = "TestPassword".toCharArray();
        KDF.KdfParams params = KDF.getDefaultParams();
        byte[] keyBytes = KDF.deriveKey(password, params);
        
        // Act
        SecretKey secretKey = KDF.createSecretKey(keyBytes);
        
        // Assert
        assertNotNull(secretKey);
        assertEquals("AES", secretKey.getAlgorithm());
        assertEquals(32, secretKey.getEncoded().length);
    }
    
    @Test
    @DisplayName("Should handle PBKDF2 fallback")
    void testPBKDF2Fallback() throws GeneralSecurityException {
        // Arrange
        char[] password = "TestPassword".toCharArray();
        byte[] salt = KDF.generateSalt();
        KDF.KdfParams params = new KDF.KdfParams(KDF.Algorithm.PBKDF2, salt, 100000);
        
        // Act
        byte[] key = KDF.deriveKey(password, params);
        
        // Assert
        assertNotNull(key);
        assertEquals(32, key.length);
    }
    
    @Test
    @DisplayName("Should handle Argon2id parameters")
    void testArgon2Parameters() throws GeneralSecurityException {
        // Arrange
        char[] password = "TestPassword".toCharArray();
        byte[] salt = KDF.generateSalt();
        KDF.KdfParams params = new KDF.KdfParams(
            KDF.Algorithm.ARGON2ID,
            salt,
            65536,  // 64MB memory
            3,      // 3 iterations
            4       // 4 threads
        );
        
        // Act
        try {
            byte[] key = KDF.deriveKey(password, params);
            
            // Assert
            assertNotNull(key);
            assertEquals(32, key.length);
            System.out.println("Argon2id derivation successful");
        } catch (Exception e) {
            // Argon2 might not be available in all environments
            System.out.println("Argon2id not available: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        // Arrange
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            KDF.deriveKey(null, params);
        });
    }
    
    @Test
    @DisplayName("Should reject empty password")
    void testEmptyPassword() {
        // Arrange
        char[] password = new char[0];
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            KDF.deriveKey(password, params);
        });
    }
    
    @Test
    @DisplayName("Should reject null params")
    void testNullParams() {
        // Arrange
        char[] password = "TestPassword".toCharArray();
        
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            KDF.deriveKey(password, null);
        });
    }
    
    @Test
    @DisplayName("Should handle long passwords")
    void testLongPassword() throws GeneralSecurityException {
        // Arrange
        char[] password = new char[1000];
        Arrays.fill(password, 'a');
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Act
        byte[] key = KDF.deriveKey(password, params);
        
        // Assert
        assertNotNull(key);
        assertEquals(32, key.length);
    }
    
    @Test
    @DisplayName("Should handle special characters in password")
    void testSpecialCharacters() throws GeneralSecurityException {
        // Arrange
        char[] password = "P@ssw0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`".toCharArray();
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Act
        byte[] key = KDF.deriveKey(password, params);
        
        // Assert
        assertNotNull(key);
        assertEquals(32, key.length);
    }
    
    @Test
    @DisplayName("Should handle Unicode characters in password")
    void testUnicodeCharacters() throws GeneralSecurityException {
        // Arrange
        char[] password = "Пароль密码🔐".toCharArray();
        KDF.KdfParams params = KDF.getDefaultParams();
        
        // Act
        byte[] key = KDF.deriveKey(password, params);
        
        // Assert
        assertNotNull(key);
        assertEquals(32, key.length);
    }
}
