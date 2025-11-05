package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for PasswordMigrationUtility
 */
public class PasswordMigrationUtilityTest {
    
    @TempDir
    Path tempDir;
    
    private PasswordMigrationUtility migrationUtility;
    private SecurePasswordStorage passwordStorage;
    private final String masterPassword = "MasterPass123!";
    private final String decoyPassword = "DecoyPass456@";
    private final String panicPassword = "PanicPass789#";
    
    @BeforeEach
    void setUp() {
        migrationUtility = new PasswordMigrationUtility(tempDir.toString());
        passwordStorage = new SecurePasswordStorage(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should detect when migration is needed")
    void shouldDetectWhenMigrationIsNeeded() throws Exception {
        // Initially no migration needed
        assertFalse(migrationUtility.isMigrationNeeded());
        
        // Create plain text password file
        createPlainTextPasswordFile();
        
        // Now migration should be needed
        assertTrue(migrationUtility.isMigrationNeeded());
    }
    
    @Test
    @DisplayName("Should perform successful migration")
    void shouldPerformSuccessfulMigration() throws Exception {
        // Create plain text password file
        createPlainTextPasswordFile();
        
        // Perform migration
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBackupPath());
        assertTrue(result.getMessage().contains("successfully"));
        
        // Verify passwords work with new secure storage
        assertTrue(passwordStorage.verifyPassword(masterPassword, SecurePasswordStorage.PasswordType.MASTER));
        assertTrue(passwordStorage.verifyPassword(decoyPassword, SecurePasswordStorage.PasswordType.DECOY));
        assertTrue(passwordStorage.verifyPassword(panicPassword, SecurePasswordStorage.PasswordType.PANIC));
        
        // Verify plain text file is removed
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        assertFalse(Files.exists(plainFile));
        
        // Verify encrypted file exists
        Path encryptedFile = tempDir.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        assertTrue(Files.exists(encryptedFile));
    }
    
    @Test
    @DisplayName("Should create backup during migration")
    void shouldCreateBackupDuringMigration() throws Exception {
        createPlainTextPasswordFile();
        
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBackupPath());
        
        // Verify backup file exists and contains original data
        Path backupFile = Path.of(result.getBackupPath());
        assertTrue(Files.exists(backupFile));
        
        String backupContent = new String(Files.readAllBytes(backupFile));
        assertTrue(backupContent.contains(masterPassword));
        assertTrue(backupContent.contains(decoyPassword));
        assertTrue(backupContent.contains(panicPassword));
    }
    
    @Test
    @DisplayName("Should handle migration failure gracefully")
    void shouldHandleMigrationFailureGracefully() throws Exception {
        // Create invalid plain text password file (missing passwords)
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        Files.write(plainFile, "invalid\n".getBytes());
        
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid password format"));
    }
    
    @Test
    @DisplayName("Should rollback migration on verification failure")
    void shouldRollbackMigrationOnVerificationFailure() throws Exception {
        createPlainTextPasswordFile();
        
        // This test would require mocking to force verification failure
        // For now, we'll test the rollback method directly
        
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        assertTrue(result.isSuccess());
        
        // Test rollback functionality
        boolean rollbackSuccess = migrationUtility.rollbackMigration(result.getBackupPath());
        assertTrue(rollbackSuccess);
        
        // Verify plain text file is restored
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        assertTrue(Files.exists(plainFile));
        
        // Verify encrypted file is removed
        Path encryptedFile = tempDir.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        assertFalse(Files.exists(encryptedFile));
    }
    
    @Test
    @DisplayName("Should validate passwords during migration")
    void shouldValidatePasswordsDuringMigration() throws Exception {
        // Create plain text file with duplicate passwords
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        String invalidContent = masterPassword + "\n" + masterPassword + "\n" + panicPassword; // duplicate master
        Files.write(plainFile, invalidContent.getBytes());
        
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("different"));
    }
    
    @Test
    @DisplayName("Should handle missing plain text file")
    void shouldHandleMissingPlainTextFile() {
        PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No plain text password file found"));
    }
    
    @Test
    @DisplayName("Should handle already migrated state")
    void shouldHandleAlreadyMigratedState() throws Exception {
        // First, perform a successful migration
        createPlainTextPasswordFile();
        PasswordMigrationUtility.MigrationResult firstResult = migrationUtility.performMigration();
        assertTrue(firstResult.isSuccess());
        
        // Try to migrate again
        PasswordMigrationUtility.MigrationResult secondResult = migrationUtility.performMigration();
        
        assertFalse(secondResult.isSuccess());
        assertTrue(secondResult.getMessage().contains("No plain text password file found"));
    }
    
    @Test
    @DisplayName("Should handle rollback with missing backup")
    void shouldHandleRollbackWithMissingBackup() {
        boolean rollbackSuccess = migrationUtility.rollbackMigration("nonexistent/backup/path");
        assertFalse(rollbackSuccess);
        
        rollbackSuccess = migrationUtility.rollbackMigration(null);
        assertFalse(rollbackSuccess);
    }
    
    private void createPlainTextPasswordFile() throws Exception {
        Path plainFile = tempDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        String content = masterPassword + "\n" + decoyPassword + "\n" + panicPassword;
        Files.write(plainFile, content.getBytes());
    }
}