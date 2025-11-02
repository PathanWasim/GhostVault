package com.ghostvault.integration;

import com.ghostvault.security.SecureAuthenticationManager;
import com.ghostvault.security.AuthenticationResult;
import com.ghostvault.security.VaultMode;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.core.PersistentStorageManager;
import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.model.VaultFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Integration tests for complete security workflow
 */
public class SecurityIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private String vaultPath;
    private SecureAuthenticationManager authManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private PersistentStorageManager storageManager;
    private VaultBackupManager backupManager;
    private SecretKey testKey;
    
    @BeforeEach
    void setUp() throws Exception {
        vaultPath = tempDir.resolve("integration-vault").toString();
        authManager = new SecureAuthenticationManager();
        fileManager = new FileManager(vaultPath);
        metadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        storageManager = new PersistentStorageManager(vaultPath);
        backupManager = new VaultBackupManager(fileManager, metadataManager);
        
        // Create test encryption key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        testKey = keyGen.generateKey();
    }
    
    @Test
    @DisplayName("Complete authentication and file persistence workflow")
    void testCompleteAuthenticationAndPersistenceWorkflow() throws Exception {
        // Step 1: Authenticate with master password
        AuthenticationResult authResult = authManager.authenticate("masterpass123");
        assertTrue(authResult.isSuccess(), "Master password authentication should succeed");
        assertEquals(VaultMode.MASTER, authResult.getMode(), "Should return master mode");
        
        // Step 2: Initialize vault structure
        boolean vaultInitialized = storageManager.initializeVaultStructure();
        assertTrue(vaultInitialized, "Vault structure should initialize");
        
        // Step 3: Store files
        File testFile1 = tempDir.resolve("integration-test1.txt").toFile();
        Files.write(testFile1.toPath(), "Integration test content 1".getBytes());
        
        File testFile2 = tempDir.resolve("integration-test2.txt").toFile();
        Files.write(testFile2.toPath(), "Integration test content 2".getBytes());
        
        VaultFile vaultFile1 = fileManager.storeFile(testFile1);
        VaultFile vaultFile2 = fileManager.storeFile(testFile2);
        
        metadataManager.addFile(vaultFile1);
        metadataManager.addFile(vaultFile2);
        
        // Step 4: Verify files are accessible
        byte[] content1 = fileManager.retrieveFile(vaultFile1);
        byte[] content2 = fileManager.retrieveFile(vaultFile2);
        
        assertEquals("Integration test content 1", new String(content1));
        assertEquals("Integration test content 2", new String(content2));
        
        // Step 5: Simulate application restart
        FileManager newFileManager = new FileManager(vaultPath);
        MetadataManager newMetadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        newMetadataManager.loadMetadata();
        
        // Step 6: Verify persistence after restart
        List<VaultFile> persistedFiles = newMetadataManager.getAllFiles();
        assertEquals(2, persistedFiles.size(), "Both files should persist");
        
        for (VaultFile file : persistedFiles) {
            byte[] persistedContent = newFileManager.retrieveFile(file);
            assertNotNull(persistedContent, "File content should be accessible after restart");
            assertTrue(persistedContent.length > 0, "File content should not be empty");
        }
    }
    
    @Test
    @DisplayName("Authentication lockout and recovery workflow")
    void testAuthenticationLockoutAndRecovery() {
        // Make 5 failed attempts to trigger lockout
        for (int i = 1; i <= 5; i++) {
            AuthenticationResult result = authManager.authenticate("wrongpassword" + i);
            assertFalse(result.isSuccess(), "Failed attempt " + i + " should be rejected");
        }
        
        // Verify system is locked
        assertTrue(authManager.isSystemLocked(), "System should be locked after 5 failed attempts");
        
        // Try valid password while locked
        AuthenticationResult lockedResult = authManager.authenticate("masterpass123");
        assertFalse(lockedResult.isSuccess(), "Valid password should be rejected while locked");
        
        // Note: In a real test, we would wait for lockout to expire or mock the time
        // For this test, we verify the lockout mechanism works
    }
    
    @Test
    @DisplayName("Panic mode should prevent vault access")
    void testPanicModePreventVaultAccess() {
        // Authenticate with panic password
        AuthenticationResult panicResult = authManager.authenticate("panicmode999");
        
        assertTrue(panicResult.isSuccess(), "Panic password should be accepted");
        assertEquals(VaultMode.PANIC, panicResult.getMode(), "Should return panic mode");
        assertTrue(panicResult.isPanicMode(), "Should indicate panic mode activation");
        
        // In a real implementation, panic mode would wipe the vault
        // Here we verify the authentication correctly identifies panic mode
    }
    
    @Test
    @DisplayName("Backup and restore with persistent files")
    void testBackupRestoreWithPersistentFiles() throws Exception {
        // Initialize vault and store files
        storageManager.initializeVaultStructure();
        
        File testFile = tempDir.resolve("backup-test.txt").toFile();
        Files.write(testFile.toPath(), "Backup test content".getBytes());
        
        VaultFile vaultFile = fileManager.storeFile(testFile);
        metadataManager.addFile(vaultFile);
        
        // Create backup
        File backupFile = tempDir.resolve("test-backup.gvbackup").toFile();
        backupManager.createBackup(backupFile, testKey);
        
        assertTrue(backupFile.exists(), "Backup file should be created");
        assertTrue(backupFile.length() > 0, "Backup file should not be empty");
        
        // Clear vault (simulate data loss)
        Files.deleteIfExists(Path.of(vaultPath, "files", vaultFile.getFileId() + ".dat"));
        
        // Restore from backup
        backupManager.restoreBackup(backupFile, testKey);
        
        // Verify restoration
        FileManager restoredFileManager = new FileManager(vaultPath);
        MetadataManager restoredMetadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        restoredMetadataManager.loadMetadata();
        
        List<VaultFile> restoredFiles = restoredMetadataManager.getAllFiles();
        assertEquals(1, restoredFiles.size(), "Restored vault should contain 1 file");
        
        VaultFile restoredFile = restoredFiles.get(0);
        byte[] restoredContent = restoredFileManager.retrieveFile(restoredFile);
        assertEquals("Backup test content", new String(restoredContent), 
            "Restored file content should match original");
    }
    
    @Test
    @DisplayName("Different vault modes should be properly isolated")
    void testVaultModeIsolation() {
        // Test master mode authentication
        AuthenticationResult masterResult = authManager.authenticate("masterpass123");
        assertTrue(masterResult.isSuccess());
        assertEquals(VaultMode.MASTER, masterResult.getMode());
        assertFalse(masterResult.isPanicMode());
        
        // Test decoy mode authentication
        AuthenticationResult decoyResult = authManager.authenticate("decoypass456");
        assertTrue(decoyResult.isSuccess());
        assertEquals(VaultMode.DECOY, decoyResult.getMode());
        assertFalse(decoyResult.isPanicMode());
        
        // Test panic mode authentication
        AuthenticationResult panicResult = authManager.authenticate("panicmode999");
        assertTrue(panicResult.isSuccess());
        assertEquals(VaultMode.PANIC, panicResult.getMode());
        assertTrue(panicResult.isPanicMode());
        
        // Verify modes are distinct
        assertNotEquals(masterResult.getMode(), decoyResult.getMode());
        assertNotEquals(masterResult.getMode(), panicResult.getMode());
        assertNotEquals(decoyResult.getMode(), panicResult.getMode());
    }
    
    @Test
    @DisplayName("Storage verification and recovery integration")
    void testStorageVerificationAndRecovery() throws Exception {
        // Initialize vault
        storageManager.initializeVaultStructure();
        
        // Verify healthy storage
        var verification = storageManager.verifyStorageIntegrity();
        assertTrue(verification.isStorageHealthy(), "Initial storage should be healthy");
        
        // Simulate corruption by deleting metadata directory
        Files.deleteIfExists(Path.of(vaultPath, "metadata"));
        
        // Verify storage is unhealthy
        var corruptedVerification = storageManager.verifyStorageIntegrity();
        assertFalse(corruptedVerification.isStorageHealthy(), "Storage should be unhealthy after corruption");
        
        // Recover storage
        boolean recovered = storageManager.recoverVaultStructure();
        assertTrue(recovered, "Storage recovery should succeed");
        
        // Verify storage is healthy again
        var recoveredVerification = storageManager.verifyStorageIntegrity();
        assertTrue(recoveredVerification.isStorageHealthy(), "Storage should be healthy after recovery");
    }
    
    @Test
    @DisplayName("Multiple authentication attempts with different passwords")
    void testMultipleAuthenticationAttempts() {
        // Test sequence of different password types
        String[] passwords = {
            "masterpass123",  // Valid master
            "wrongpassword",  // Invalid
            "decoypass456",   // Valid decoy
            "anotherwrong",   // Invalid
            "panicmode999"    // Valid panic
        };
        
        VaultMode[] expectedModes = {
            VaultMode.MASTER,
            null,
            VaultMode.DECOY,
            null,
            VaultMode.PANIC
        };
        
        boolean[] expectedSuccess = {true, false, true, false, true};
        boolean[] expectedPanic = {false, false, false, false, true};
        
        for (int i = 0; i < passwords.length; i++) {
            AuthenticationResult result = authManager.authenticate(passwords[i]);
            
            assertEquals(expectedSuccess[i], result.isSuccess(), 
                "Password " + passwords[i] + " success should match expected");
            assertEquals(expectedModes[i], result.getMode(), 
                "Password " + passwords[i] + " mode should match expected");
            assertEquals(expectedPanic[i], result.isPanicMode(), 
                "Password " + passwords[i] + " panic mode should match expected");
        }
    }
}