package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for FileManager encryption capabilities
 */
public class FileManagerEncryptionTest {
    
    @TempDir
    Path tempDir;
    
    private FileManager fileManager;
    private final String testPassword = "TestPassword123!";
    private final String testFileContent = "This is sensitive test file content that should be encrypted!";
    
    @BeforeEach
    void setUp() {
        fileManager = new FileManager(tempDir.toString());
        fileManager.setPassword(testPassword);
        fileManager.setEncryptionEnabled(true);
    }
    
    @Test
    @DisplayName("Should store and retrieve encrypted files")
    void shouldStoreAndRetrieveEncryptedFiles() throws Exception {
        // Create test file
        File testFile = createTestFile("test.txt", testFileContent);
        
        // Store file with encryption
        VaultFile vaultFile = fileManager.storeFile(testFile);
        assertNotNull(vaultFile);
        
        // Verify file is encrypted on disk
        assertTrue(fileManager.isFileEncrypted(vaultFile.getFileId()));
        
        // Retrieve and verify content
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertEquals(testFileContent, new String(retrievedContent));
    }
    
    @Test
    @DisplayName("Should encrypt files with unique IVs")
    void shouldEncryptFilesWithUniqueIVs() throws Exception {
        File testFile1 = createTestFile("test1.txt", testFileContent);
        File testFile2 = createTestFile("test2.txt", testFileContent);
        
        VaultFile vaultFile1 = fileManager.storeFile(testFile1);
        VaultFile vaultFile2 = fileManager.storeFile(testFile2);
        
        // Read encrypted data from disk
        Path file1Path = tempDir.resolve("files").resolve(vaultFile1.getFileId() + ".dat");
        Path file2Path = tempDir.resolve("files").resolve(vaultFile2.getFileId() + ".dat");
        
        byte[] encrypted1 = Files.readAllBytes(file1Path);
        byte[] encrypted2 = Files.readAllBytes(file2Path);
        
        // Encrypted data should be different even for same content
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2));
        
        // Both should be valid encrypted format
        assertTrue(EncryptedFileData.isEncryptedFileFormat(encrypted1));
        assertTrue(EncryptedFileData.isEncryptedFileFormat(encrypted2));
    }
    
    @Test
    @DisplayName("Should fail decryption with wrong password")
    void shouldFailDecryptionWithWrongPassword() throws Exception {
        File testFile = createTestFile("test.txt", testFileContent);
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Change password
        fileManager.setPassword("WrongPassword");
        
        // Should fail to retrieve
        assertThrows(Exception.class, () -> {
            fileManager.retrieveFile(vaultFile);
        });
    }
    
    @Test
    @DisplayName("Should handle unencrypted files when encryption is disabled")
    void shouldHandleUnencryptedFilesWhenEncryptionIsDisabled() throws Exception {
        fileManager.setEncryptionEnabled(false);
        
        File testFile = createTestFile("test.txt", testFileContent);
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // File should not be encrypted
        assertFalse(fileManager.isFileEncrypted(vaultFile.getFileId()));
        
        // Should still be retrievable
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertEquals(testFileContent, new String(retrievedContent));
    }
    
    @Test
    @DisplayName("Should handle mixed encrypted and unencrypted files")
    void shouldHandleMixedEncryptedAndUnencryptedFiles() throws Exception {
        // Store unencrypted file first
        fileManager.setEncryptionEnabled(false);
        File unencryptedFile = createTestFile("unencrypted.txt", "Unencrypted content");
        VaultFile unencryptedVault = fileManager.storeFile(unencryptedFile);
        
        // Store encrypted file
        fileManager.setEncryptionEnabled(true);
        File encryptedFile = createTestFile("encrypted.txt", "Encrypted content");
        VaultFile encryptedVault = fileManager.storeFile(encryptedFile);
        
        // Verify encryption status
        assertFalse(fileManager.isFileEncrypted(unencryptedVault.getFileId()));
        assertTrue(fileManager.isFileEncrypted(encryptedVault.getFileId()));
        
        // Both should be retrievable
        assertEquals("Unencrypted content", new String(fileManager.retrieveFile(unencryptedVault)));
        assertEquals("Encrypted content", new String(fileManager.retrieveFile(encryptedVault)));
    }
    
    @Test
    @DisplayName("Should get encryption status for all files")
    void shouldGetEncryptionStatusForAllFiles() throws Exception {
        // Create mixed files
        fileManager.setEncryptionEnabled(false);
        File unencryptedFile = createTestFile("unencrypted.txt", "content");
        VaultFile unencryptedVault = fileManager.storeFile(unencryptedFile);
        
        fileManager.setEncryptionEnabled(true);
        File encryptedFile = createTestFile("encrypted.txt", "content");
        VaultFile encryptedVault = fileManager.storeFile(encryptedFile);
        
        java.util.Map<String, Boolean> status = fileManager.getEncryptionStatus();
        
        assertEquals(2, status.size());
        assertFalse(status.get(unencryptedVault.getFileId()));
        assertTrue(status.get(encryptedVault.getFileId()));
    }
    
    @Test
    @DisplayName("Should clear sensitive data from memory")
    void shouldClearSensitiveDataFromMemory() throws Exception {
        File testFile = createTestFile("test.txt", testFileContent);
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Retrieve file to cache it in memory
        fileManager.retrieveFile(vaultFile);
        
        // Clear sensitive data
        fileManager.clearSensitiveData();
        
        // File should still be retrievable from disk (if password is set)
        fileManager.setPassword(testPassword);
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertEquals(testFileContent, new String(retrievedContent));
    }
    
    @Test
    @DisplayName("Should handle large files")
    void shouldHandleLargeFiles() throws Exception {
        // Create large test content (1MB)
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("This is line ").append(i).append(" of large test content.\n");
        }
        
        File largeFile = createTestFile("large.txt", largeContent.toString());
        VaultFile vaultFile = fileManager.storeFile(largeFile);
        
        assertTrue(fileManager.isFileEncrypted(vaultFile.getFileId()));
        
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertEquals(largeContent.toString(), new String(retrievedContent));
    }
    
    @Test
    @DisplayName("Should handle binary files")
    void shouldHandleBinaryFiles() throws Exception {
        // Create binary test data
        byte[] binaryData = new byte[1000];
        new java.security.SecureRandom().nextBytes(binaryData);
        
        File binaryFile = createTestFile("binary.dat", binaryData);
        VaultFile vaultFile = fileManager.storeFile(binaryFile);
        
        assertTrue(fileManager.isFileEncrypted(vaultFile.getFileId()));
        
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertArrayEquals(binaryData, retrievedContent);
    }
    
    @Test
    @DisplayName("Should require password for encryption")
    void shouldRequirePasswordForEncryption() throws Exception {
        fileManager.setPassword(null);
        
        File testFile = createTestFile("test.txt", testFileContent);
        
        assertThrows(IllegalStateException.class, () -> {
            fileManager.storeFile(testFile);
        });
    }
    
    @Test
    @DisplayName("Should require password for decryption")
    void shouldRequirePasswordForDecryption() throws Exception {
        File testFile = createTestFile("test.txt", testFileContent);
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Clear password
        fileManager.clearSensitiveData();
        fileManager.setPassword(null);
        
        assertThrows(IllegalStateException.class, () -> {
            fileManager.retrieveFile(vaultFile);
        });
    }
    
    private File createTestFile(String fileName, String content) throws Exception {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
    
    private File createTestFile(String fileName, byte[] content) throws Exception {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content);
        return filePath.toFile();
    }
}