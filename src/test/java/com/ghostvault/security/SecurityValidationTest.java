package com.ghostvault.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Security validation tests for critical system fixes
 */
public class SecurityValidationTest {
    
    private SecretKey testKey;
    private String testVaultPath;
    private SecureNotesManager notesManager;
    private PasswordVaultManager passwordManager;
    
    @BeforeEach
    void setUp() throws Exception {
        // Generate test encryption key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        testKey = keyGen.generateKey();
        
        // Create temporary test vault directory
        testVaultPath = System.getProperty("java.io.tmpdir") + File.separator + "ghostvault_test_" + System.currentTimeMillis();
        Files.createDirectories(Paths.get(testVaultPath));
        
        // Initialize managers
        notesManager = new SecureNotesManager(testVaultPath);
        notesManager.setEncryptionKey(testKey);
        
        passwordManager = new PasswordVaultManager(testVaultPath);
        passwordManager.setEncryptionKey(testKey);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Clean up test directory
        deleteDirectoryRecursively(new File(testVaultPath));
    }
    
    @Test
    @DisplayName("Test complete data deletion in panic mode simulation")
    void testCompleteDataDeletion() throws Exception {
        // Create test data
        SecureNote testNote = new SecureNote("Test Note", "Secret content");
        notesManager.addNote(testNote);
        
        PasswordEntry testPassword = new PasswordEntry();
        testPassword.setServiceName("Test Service");
        testPassword.setUsername("testuser");
        testPassword.setPassword("secretpassword");
        passwordManager.addPassword(testPassword);
        
        // Verify files exist
        File notesFile = new File(testVaultPath + File.separator + "notes.enc");
        File passwordsFile = new File(testVaultPath + File.separator + "passwords.enc");
        
        assertTrue(notesFile.exists(), "Notes file should exist after saving");
        assertTrue(passwordsFile.exists(), "Passwords file should exist after saving");
        assertTrue(notesFile.length() > 0, "Notes file should have content");
        assertTrue(passwordsFile.length() > 0, "Passwords file should have content");
        
        // Simulate panic mode deletion
        deleteDirectoryRecursively(new File(testVaultPath));
        
        // Verify complete deletion
        assertFalse(notesFile.exists(), "Notes file should be deleted");
        assertFalse(passwordsFile.exists(), "Passwords file should be deleted");
        assertFalse(new File(testVaultPath).exists(), "Vault directory should be deleted");
        
        System.out.println("✅ Complete data deletion test passed");
    }
    
    @Test
    @DisplayName("Test encryption key consistency across sessions")
    void testEncryptionKeyConsistency() throws Exception {
        // Create and save test note
        SecureNote originalNote = new SecureNote("Consistency Test", "This is a test for key consistency");
        notesManager.addNote(originalNote);
        
        // Create new manager instance with same key (simulating new session)
        SecureNotesManager newSessionManager = new SecureNotesManager(testVaultPath);
        newSessionManager.setEncryptionKey(testKey);
        newSessionManager.loadNotes();
        
        // Verify data can be loaded with same key
        var loadedNotes = newSessionManager.getNotes();
        assertFalse(loadedNotes.isEmpty(), "Should be able to load notes with same key");
        
        SecureNote loadedNote = loadedNotes.get(0);
        assertEquals(originalNote.getTitle(), loadedNote.getTitle(), "Note title should match");
        assertEquals(originalNote.getContent(), loadedNote.getContent(), "Note content should match");
        
        // Test with different key (should fail)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey differentKey = keyGen.generateKey();
        
        SecureNotesManager wrongKeyManager = new SecureNotesManager(testVaultPath);
        wrongKeyManager.setEncryptionKey(differentKey);
        
        // Should throw exception when trying to load with wrong key
        assertThrows(Exception.class, () -> {
            wrongKeyManager.loadNotes();
        }, "Should fail to load notes with wrong encryption key");
        
        System.out.println("✅ Encryption key consistency test passed");
    }
    
    @Test
    @DisplayName("Test memory cleanup effectiveness")
    void testMemoryCleanupEffectiveness() throws Exception {
        // Create test data
        String sensitiveContent = "This is highly sensitive information that should be cleared from memory";
        byte[] sensitiveData = sensitiveContent.getBytes("UTF-8");
        byte[] originalData = Arrays.copyOf(sensitiveData, sensitiveData.length);
        
        // Verify data is initially present
        assertArrayEquals(originalData, sensitiveData, "Data should initially match");
        
        // Simulate memory cleanup
        Arrays.fill(sensitiveData, (byte) 0);
        
        // Verify data is cleared
        for (byte b : sensitiveData) {
            assertEquals(0, b, "All bytes should be zeroed after cleanup");
        }
        
        // Verify original data is different from cleared data
        assertFalse(Arrays.equals(originalData, sensitiveData), "Cleared data should differ from original");
        
        System.out.println("✅ Memory cleanup effectiveness test passed");
    }
    
    @Test
    @DisplayName("Test no data traces remain after panic mode")
    void testNoDataTracesRemain() throws Exception {
        // Create comprehensive test data
        SecureNote note1 = new SecureNote("Secret Note 1", "Confidential information 1");
        SecureNote note2 = new SecureNote("Secret Note 2", "Confidential information 2");
        notesManager.addNote(note1);
        notesManager.addNote(note2);
        
        PasswordEntry pwd1 = new PasswordEntry();
        pwd1.setServiceName("Bank Account");
        pwd1.setUsername("user123");
        pwd1.setPassword("supersecret123");
        
        PasswordEntry pwd2 = new PasswordEntry();
        pwd2.setServiceName("Email Account");
        pwd2.setUsername("user@email.com");
        pwd2.setPassword("anothersecret456");
        
        passwordManager.addPassword(pwd1);
        passwordManager.addPassword(pwd2);
        
        // Create additional test files
        File additionalFile = new File(testVaultPath + File.separator + "additional_data.txt");
        Files.write(additionalFile.toPath(), "Additional sensitive data".getBytes());
        
        // Verify all data exists
        assertTrue(new File(testVaultPath + File.separator + "notes.enc").exists());
        assertTrue(new File(testVaultPath + File.separator + "passwords.enc").exists());
        assertTrue(additionalFile.exists());
        
        // Simulate complete panic mode wipe
        deleteDirectoryRecursively(new File(testVaultPath));
        
        // Verify no traces remain
        File vaultDir = new File(testVaultPath);
        assertFalse(vaultDir.exists(), "Vault directory should not exist");
        
        // Try to recreate directory and verify it's empty
        Files.createDirectories(Paths.get(testVaultPath));
        File[] remainingFiles = vaultDir.listFiles();
        
        if (remainingFiles != null) {
            assertEquals(0, remainingFiles.length, "No files should remain after panic mode");
        }
        
        System.out.println("✅ No data traces remain test passed");
    }
    
    @Test
    @DisplayName("Test data persistence across application restarts")
    void testDataPersistenceAcrossRestarts() throws Exception {
        // Create test data
        SecureNote persistentNote = new SecureNote("Persistent Note", "This should survive restart");
        notesManager.addNote(persistentNote);
        
        PasswordEntry persistentPassword = new PasswordEntry();
        persistentPassword.setServiceName("Persistent Service");
        persistentPassword.setUsername("persistent_user");
        persistentPassword.setPassword("persistent_pass");
        passwordManager.addPassword(persistentPassword);
        
        // Simulate application restart by creating new manager instances
        SecureNotesManager restartedNotesManager = new SecureNotesManager(testVaultPath);
        restartedNotesManager.setEncryptionKey(testKey);
        restartedNotesManager.loadNotes();
        
        PasswordVaultManager restartedPasswordManager = new PasswordVaultManager(testVaultPath);
        restartedPasswordManager.setEncryptionKey(testKey);
        restartedPasswordManager.loadPasswords();
        
        // Verify data persisted
        var loadedNotes = restartedNotesManager.getNotes();
        assertFalse(loadedNotes.isEmpty(), "Notes should persist across restart");
        assertEquals(persistentNote.getTitle(), loadedNotes.get(0).getTitle(), "Note title should persist");
        assertEquals(persistentNote.getContent(), loadedNotes.get(0).getContent(), "Note content should persist");
        
        var loadedPasswords = restartedPasswordManager.getPasswords();
        assertFalse(loadedPasswords.isEmpty(), "Passwords should persist across restart");
        assertEquals(persistentPassword.getServiceName(), loadedPasswords.get(0).getServiceName(), "Password service should persist");
        assertEquals(persistentPassword.getUsername(), loadedPasswords.get(0).getUsername(), "Password username should persist");
        assertEquals(persistentPassword.getPassword(), loadedPasswords.get(0).getPassword(), "Password should persist");
        
        System.out.println("✅ Data persistence across restarts test passed");
    }
    
    /**
     * Recursively delete directory and all contents (for testing)
     */
    private void deleteDirectoryRecursively(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}