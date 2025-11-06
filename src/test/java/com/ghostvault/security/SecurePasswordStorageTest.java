package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for SecurePasswordStorage
 */
public class SecurePasswordStorageTest {
    
    @TempDir
    Path tempDir;
    
    private SecurePasswordStorage passwordStorage;
    private final String masterPassword = "MasterPass123!";
    private final String decoyPassword = "DecoyPass456@";
    private final String panicPassword = "PanicPass789#";
    
    @BeforeEach
    void setUp() {
        passwordStorage = new SecurePasswordStorage(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should store and verify password hashes correctly")
    void shouldStoreAndVerifyPasswordHashes() throws Exception {
        // Store passwords
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        
        // Verify correct passwords
        assertTrue(passwordStorage.verifyPassword(masterPassword, SecurePasswordStorage.PasswordType.MASTER));
        assertTrue(passwordStorage.verifyPassword(decoyPassword, SecurePasswordStorage.PasswordType.DECOY));
        assertTrue(passwordStorage.verifyPassword(panicPassword, SecurePasswordStorage.PasswordType.PANIC));
        
        // Verify incorrect passwords fail
        assertFalse(passwordStorage.verifyPassword("WrongPassword", SecurePasswordStorage.PasswordType.MASTER));
        assertFalse(passwordStorage.verifyPassword(masterPassword, SecurePasswordStorage.PasswordType.DECOY));
        assertFalse(passwordStorage.verifyPassword(decoyPassword, SecurePasswordStorage.PasswordType.PANIC));
    }
    
    @Test
    @DisplayName("Should not store plain text passwords")
    void shouldNotStorePlainTextPasswords() throws Exception {
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        
        Path encryptedFile = tempDir.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        assertTrue(Files.exists(encryptedFile));
        
        // Read file content and verify it doesn't contain plain text passwords
        byte[] fileContent = Files.readAllBytes(encryptedFile);
        String fileContentStr = new String(fileContent);
        
        assertFalse(fileContentStr.contains(masterPassword));
        assertFalse(fileContentStr.contains(decoyPassword));
        assertFalse(fileContentStr.contains(panicPassword));
    }
    
    @Test
    @DisplayName("Should load password data correctly")
    void shouldLoadPasswordDataCorrectly() throws Exception {
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        
        SecurePasswordStorage.PasswordData data = passwordStorage.loadPasswordData();
        assertNotNull(data);
        assertNotNull(data.getMasterSalt());
        assertNotNull(data.getMasterHash());
        assertNotNull(data.getDecoySalt());
        assertNotNull(data.getDecoyHash());
        assertNotNull(data.getPanicSalt());
        assertNotNull(data.getPanicHash());
        assertEquals(SecurityConfiguration.PBKDF2_ITERATIONS, data.getIterations());
        
        // Verify salt lengths
        assertEquals(SecurityConfiguration.SALT_LENGTH, data.getMasterSalt().length);
        assertEquals(SecurityConfiguration.SALT_LENGTH, data.getDecoySalt().length);
        assertEquals(SecurityConfiguration.SALT_LENGTH, data.getPanicSalt().length);
        
        // Verify hash lengths
        assertEquals(SecurityConfiguration.KEY_LENGTH / 8, data.getMasterHash().length);
        assertEquals(SecurityConfiguration.KEY_LENGTH / 8, data.getDecoyHash().length);
        assertEquals(SecurityConfiguration.KEY_LENGTH / 8, data.getPanicHash().length);
    }
    
    @Test
    @DisplayName("Should detect secure storage status correctly")
    void shouldDetectSecureStorageStatus() throws Exception {
        // Initially no secure storage
        assertFalse(passwordStorage.isUsingSecureStorage());
        
        // After storing passwords, should use secure storage
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        assertTrue(passwordStorage.isUsingSecureStorage());
    }
    
    @Test
    @DisplayName("Should detect migration needs correctly")
    void shouldDetectMigrationNeeds() throws Exception {
        // Initially no migration needed
        assertFalse(passwordStorage.needsMigration());
        
        // Create plain text password file
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        String plainContent = masterPassword + "\n" + decoyPassword + "\n" + panicPassword;
        Files.write(plainFile, plainContent.getBytes());
        
        // Now migration should be needed
        assertTrue(passwordStorage.needsMigration());
        
        // After migration, should not need migration
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        assertFalse(passwordStorage.needsMigration());
    }
    
    @Test
    @DisplayName("Should get salt for key derivation")
    void shouldGetSaltForKeyDerivation() throws Exception {
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
        
        byte[] masterSalt = passwordStorage.getSaltForKeyDerivation(SecurePasswordStorage.PasswordType.MASTER);
        byte[] decoySalt = passwordStorage.getSaltForKeyDerivation(SecurePasswordStorage.PasswordType.DECOY);
        byte[] panicSalt = passwordStorage.getSaltForKeyDerivation(SecurePasswordStorage.PasswordType.PANIC);
        
        assertNotNull(masterSalt);
        assertNotNull(decoySalt);
        assertNotNull(panicSalt);
        
        assertEquals(SecurityConfiguration.SALT_LENGTH, masterSalt.length);
        assertEquals(SecurityConfiguration.SALT_LENGTH, decoySalt.length);
        assertEquals(SecurityConfiguration.SALT_LENGTH, panicSalt.length);
        
        // Salts should be different
        assertFalse(java.util.Arrays.equals(masterSalt, decoySalt));
        assertFalse(java.util.Arrays.equals(masterSalt, panicSalt));
        assertFalse(java.util.Arrays.equals(decoySalt, panicSalt));
    }
    
    @Test
    @DisplayName("Should handle invalid inputs gracefully")
    void shouldHandleInvalidInputsGracefully() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordStorage.storePasswordHashes(null, decoyPassword, panicPassword);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordStorage.storePasswordHashes(masterPassword, null, panicPassword);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordStorage.storePasswordHashes(masterPassword, decoyPassword, null);
        });
    }
    
    @Test
    @DisplayName("Should return null for non-existent password data")
    void shouldReturnNullForNonExistentPasswordData() throws Exception {
        SecurePasswordStorage.PasswordData data = passwordStorage.loadPasswordData();
        assertNull(data);
        
        assertFalse(passwordStorage.verifyPassword(masterPassword, SecurePasswordStorage.PasswordType.MASTER));
    }
    
    @Test
    @DisplayName("Should handle file format validation")
    void shouldHandleFileFormatValidation() throws Exception {
        // Create invalid password file
        Path encryptedFile = tempDir.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        Files.write(encryptedFile, "invalid data".getBytes());
        
        assertThrows(Exception.class, () -> {
            passwordStorage.loadPasswordData();
        });
    }
}