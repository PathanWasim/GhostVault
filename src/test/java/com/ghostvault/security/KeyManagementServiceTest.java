package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
import java.nio.file.Path;

/**
 * Integration tests for KeyManagementService
 */
public class KeyManagementServiceTest {
    
    @TempDir
    Path tempDir;
    
    private KeyManagementService keyManager;
    private SecurePasswordStorage passwordStorage;
    private final String masterPassword = "MasterPass123!";
    private final String decoyPassword = "DecoyPass456@";
    private final String panicPassword = "PanicPass789#";
    
    @BeforeEach
    void setUp() throws Exception {
        keyManager = new KeyManagementService(tempDir.toString());
        passwordStorage = new SecurePasswordStorage(tempDir.toString());
        
        // Set up test passwords
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
    }
    
    @Test
    @DisplayName("Should derive and cache encryption keys")
    void shouldDeriveAndCacheEncryptionKeys() throws Exception {
        // Derive key for master password
        SecretKey masterKey = keyManager.deriveAndCacheKey(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        assertNotNull(masterKey);
        assertEquals("AES", masterKey.getAlgorithm());
        
        // Key should be cached
        SecretKey cachedKey = keyManager.getCachedKey(SecurePasswordStorage.PasswordType.MASTER);
        assertNotNull(cachedKey);
        assertArrayEquals(masterKey.getEncoded(), cachedKey.getEncoded());
    }
    
    @Test
    @DisplayName("Should authenticate passwords correctly")
    void shouldAuthenticatePasswordsCorrectly() throws Exception {
        assertTrue(keyManager.authenticatePassword(masterPassword, SecurePasswordStorage.PasswordType.MASTER));
        assertTrue(keyManager.authenticatePassword(decoyPassword, SecurePasswordStorage.PasswordType.DECOY));
        assertTrue(keyManager.authenticatePassword(panicPassword, SecurePasswordStorage.PasswordType.PANIC));
        
        assertFalse(keyManager.authenticatePassword("WrongPassword", SecurePasswordStorage.PasswordType.MASTER));
        assertFalse(keyManager.authenticatePassword(masterPassword, SecurePasswordStorage.PasswordType.DECOY));
    }
    
    @Test
    @DisplayName("Should initialize keys for session")
    void shouldInitializeKeysForSession() throws Exception {
        assertFalse(keyManager.areKeysLoaded());
        
        keyManager.initializeKeysForSession(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        assertTrue(keyManager.areKeysLoaded());
        assertEquals(SecurePasswordStorage.PasswordType.MASTER, keyManager.getAuthenticatedType());
        
        SecretKey primaryKey = keyManager.getPrimaryEncryptionKey();
        assertNotNull(primaryKey);
    }
    
    @Test
    @DisplayName("Should get salt for key derivation")
    void shouldGetSaltForKeyDerivation() throws Exception {
        keyManager.deriveAndCacheKey(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        byte[] salt = keyManager.getSalt(SecurePasswordStorage.PasswordType.MASTER);
        assertNotNull(salt);
        assertEquals(SecurityConfiguration.SALT_LENGTH, salt.length);
        
        // Salt should be consistent
        byte[] salt2 = keyManager.getSalt(SecurePasswordStorage.PasswordType.MASTER);
        assertArrayEquals(salt, salt2);
    }
    
    @Test
    @DisplayName("Should clear keys securely")
    void shouldClearKeysSecurely() throws Exception {
        keyManager.initializeKeysForSession(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        assertTrue(keyManager.areKeysLoaded());
        assertNotNull(keyManager.getPrimaryEncryptionKey());
        
        keyManager.clearKeys();
        
        assertFalse(keyManager.areKeysLoaded());
        assertNull(keyManager.getAuthenticatedType());
        
        assertThrows(IllegalStateException.class, () -> {
            keyManager.getPrimaryEncryptionKey();
        });
    }
    
    @Test
    @DisplayName("Should validate key integrity")
    void shouldValidateKeyIntegrity() throws Exception {
        keyManager.deriveAndCacheKey(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        assertTrue(keyManager.validateKeyIntegrity(SecurePasswordStorage.PasswordType.MASTER));
        
        // Non-existent key should fail validation
        assertFalse(keyManager.validateKeyIntegrity(SecurePasswordStorage.PasswordType.DECOY));
    }
    
    @Test
    @DisplayName("Should handle multiple password types")
    void shouldHandleMultiplePasswordTypes() throws Exception {
        // Derive keys for different password types
        keyManager.deriveAndCacheKey(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        keyManager.deriveAndCacheKey(decoyPassword, SecurePasswordStorage.PasswordType.DECOY);
        
        // Both keys should be available
        assertNotNull(keyManager.getCachedKey(SecurePasswordStorage.PasswordType.MASTER));
        assertNotNull(keyManager.getCachedKey(SecurePasswordStorage.PasswordType.DECOY));
        
        // Keys should be different
        SecretKey masterKey = keyManager.getCachedKey(SecurePasswordStorage.PasswordType.MASTER);
        SecretKey decoyKey = keyManager.getCachedKey(SecurePasswordStorage.PasswordType.DECOY);
        assertFalse(java.util.Arrays.equals(masterKey.getEncoded(), decoyKey.getEncoded()));
    }
    
    @Test
    @DisplayName("Should provide key management statistics")
    void shouldProvideKeyManagementStatistics() throws Exception {
        KeyManagementService.KeyManagementStats stats = keyManager.getStats();
        
        assertFalse(stats.areKeysLoaded());
        assertEquals(0, stats.getCachedKeyCount());
        assertNull(stats.getAuthenticatedType());
        
        keyManager.initializeKeysForSession(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        stats = keyManager.getStats();
        assertTrue(stats.areKeysLoaded());
        assertTrue(stats.getCachedKeyCount() > 0);
        assertEquals(SecurePasswordStorage.PasswordType.MASTER, stats.getAuthenticatedType());
    }
    
    @Test
    @DisplayName("Should handle invalid inputs gracefully")
    void shouldHandleInvalidInputsGracefully() {
        assertThrows(IllegalArgumentException.class, () -> {
            keyManager.deriveAndCacheKey(null, SecurePasswordStorage.PasswordType.MASTER);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            keyManager.deriveAndCacheKey("", SecurePasswordStorage.PasswordType.MASTER);
        });
        
        assertThrows(IllegalStateException.class, () -> {
            keyManager.getPrimaryEncryptionKey();
        });
    }
    
    @Test
    @DisplayName("Should handle password updates")
    void shouldHandlePasswordUpdates() throws Exception {
        keyManager.deriveAndCacheKey(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        assertNotNull(keyManager.getCachedKey(SecurePasswordStorage.PasswordType.MASTER));
        
        // Update password (this would clear the cached key)
        keyManager.updatePasswordAndKey(masterPassword, "NewPassword123!", SecurePasswordStorage.PasswordType.MASTER);
        
        // Key should be cleared
        assertNull(keyManager.getCachedKey(SecurePasswordStorage.PasswordType.MASTER));
    }
    
    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccessSafely() throws Exception {
        // This test would be more comprehensive in a real scenario
        // For now, just test basic thread safety
        
        keyManager.initializeKeysForSession(masterPassword, SecurePasswordStorage.PasswordType.MASTER);
        
        // Multiple threads accessing keys
        Thread t1 = new Thread(() -> {
            try {
                assertNotNull(keyManager.getPrimaryEncryptionKey());
            } catch (Exception e) {
                fail("Thread 1 failed: " + e.getMessage());
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                assertTrue(keyManager.validateKeyIntegrity(SecurePasswordStorage.PasswordType.MASTER));
            } catch (Exception e) {
                fail("Thread 2 failed: " + e.getMessage());
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
    }
}