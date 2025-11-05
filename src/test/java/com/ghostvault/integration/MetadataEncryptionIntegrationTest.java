package com.ghostvault.integration;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.core.EncryptedMetadataManager;
import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Integration tests for metadata encryption password flow
 * Tests the complete integration between FileManager and MetadataManager with password-based encryption
 */
public class MetadataEncryptionIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private EncryptedMetadataManager encryptedMetadataManager;
    private String testPassword = "testPassword123";
    private String vaultPath;
    
    @BeforeEach
    void setUp() {
        vaultPath = tempDir.toString();
        fileManager = new FileManager(vaultPath);
        metadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        encryptedMetadataManager = new EncryptedMetadataManager(vaultPath);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up any sensitive data
        if (fileManager != null) {
            try {
                fileManager.clearSensitiveData();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        if (metadataManager != null) {
            try {
                metadataManager.clearSensitiveData();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    @Test
    void testPasswordBasedEncryptionIntegration() throws Exception {
        // Test that both FileManager and MetadataManager can be initialized with passwords
        
        // Set passwords for both managers
        fileManager.setPassword(testPassword);
        fileManager.setEncryptionEnabled(true);
        
        metadataManager.setPassword(testPassword);
        metadataManager.setEncryptionEnabled(true);
        
        // Verify password availability
        assertTrue(fileManager.isPasswordAvailable(), "FileManager should have password available");
        assertTrue(fileManager.isReadyForEncryptedOperations(), "FileManager should be ready for encrypted operations");
        
        // Create a test file
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.write(testFile.toPath(), "Test content".getBytes());
        
        // Store file using FileManager
        VaultFile vaultFile = fileManager.storeFile(testFile);
        assertNotNull(vaultFile, "VaultFile should be created");
        
        // Add metadata using MetadataManager
        assertDoesNotThrow(() -> {
            metadataManager.addFile(vaultFile);
        }, "Adding file metadata should not throw exception");
        
        // Verify file can be retrieved
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertNotNull(retrievedContent, "Retrieved content should not be null");
        assertEquals("Test content", new String(retrievedContent), "Retrieved content should match original");
    }
    
    @Test
    void testEncryptedMetadataManagerPasswordValidation() throws Exception {
        // Test EncryptedMetadataManager password validation
        
        String testMetadata = "{\"files\":[{\"id\":\"test\",\"name\":\"test.txt\"}]}";
        
        // Test null password validation
        Exception nullPasswordException = assertThrows(IllegalArgumentException.class, () -> {
            encryptedMetadataManager.saveEncryptedMetadata(testMetadata, null);
        }, "Should throw exception for null password");
        
        assertTrue(nullPasswordException.getMessage().contains("Password is null"), 
                  "Error message should indicate null password issue");
        
        // Test empty password validation
        Exception emptyPasswordException = assertThrows(IllegalArgumentException.class, () -> {
            encryptedMetadataManager.saveEncryptedMetadata(testMetadata, "");
        }, "Should throw exception for empty password");
        
        assertTrue(emptyPasswordException.getMessage().contains("Password is empty"), 
                  "Error message should indicate empty password issue");
        
        // Test null metadata validation
        Exception nullMetadataException = assertThrows(IllegalArgumentException.class, () -> {
            encryptedMetadataManager.saveEncryptedMetadata(null, testPassword);
        }, "Should throw exception for null metadata");
        
        assertTrue(nullMetadataException.getMessage().contains("Metadata is null"), 
                  "Error message should indicate null metadata issue");
    }
    
    @Test
    void testSuccessfulMetadataEncryptionDecryption() throws Exception {
        // Test successful encryption and decryption cycle
        
        String testMetadata = "{\"files\":[{\"id\":\"test123\",\"name\":\"test.txt\",\"size\":100}]}";
        
        // Save encrypted metadata
        assertDoesNotThrow(() -> {
            encryptedMetadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        }, "Saving encrypted metadata should not throw exception");
        
        // Verify encrypted metadata file exists
        assertTrue(encryptedMetadataManager.hasEncryptedMetadata(), 
                  "Encrypted metadata file should exist");
        
        // Load and decrypt metadata
        String decryptedMetadata = encryptedMetadataManager.loadEncryptedMetadata(testPassword);
        assertNotNull(decryptedMetadata, "Decrypted metadata should not be null");
        assertEquals(testMetadata, decryptedMetadata, "Decrypted metadata should match original");
    }
    
    @Test
    void testPasswordMismatchHandling() throws Exception {
        // Test handling of password mismatches
        
        String testMetadata = "{\"files\":[{\"id\":\"test456\",\"name\":\"test2.txt\"}]}";
        String correctPassword = "correctPassword123";
        String wrongPassword = "wrongPassword456";
        
        // Save with correct password
        encryptedMetadataManager.saveEncryptedMetadata(testMetadata, correctPassword);
        
        // Try to load with wrong password
        Exception wrongPasswordException = assertThrows(RuntimeException.class, () -> {
            encryptedMetadataManager.loadEncryptedMetadata(wrongPassword);
        }, "Should throw exception for wrong password");
        
        assertTrue(wrongPasswordException.getMessage().contains("Metadata decryption failed") ||
                  wrongPasswordException.getMessage().contains("decryption may have failed"), 
                  "Error message should indicate decryption failure");
        
        // Verify correct password still works
        String decryptedMetadata = encryptedMetadataManager.loadEncryptedMetadata(correctPassword);
        assertEquals(testMetadata, decryptedMetadata, "Correct password should decrypt successfully");
    }
    
    @Test
    void testFileManagerPasswordIntegration() throws Exception {
        // Test FileManager password integration specifically
        
        // Test password setting
        assertDoesNotThrow(() -> {
            fileManager.setPassword(testPassword);
        }, "Setting password should not throw exception");
        
        // Test password availability check
        assertTrue(fileManager.isPasswordAvailable(), "Password should be available after setting");
        
        // Test encryption enabled check
        fileManager.setEncryptionEnabled(true);
        assertTrue(fileManager.isEncryptionEnabled(), "Encryption should be enabled");
        assertTrue(fileManager.isReadyForEncryptedOperations(), "Should be ready for encrypted operations");
        
        // Test with null password
        fileManager.setPassword(null);
        assertFalse(fileManager.isPasswordAvailable(), "Password should not be available when set to null");
        assertFalse(fileManager.isReadyForEncryptedOperations(), "Should not be ready with null password");
        
        // Test with empty password
        fileManager.setPassword("");
        assertFalse(fileManager.isPasswordAvailable(), "Password should not be available when empty");
        assertFalse(fileManager.isReadyForEncryptedOperations(), "Should not be ready with empty password");
    }
    
    @Test
    void testCompleteUploadFlow() throws Exception {
        // Test the complete upload flow with password-based encryption
        
        // Initialize both managers with password
        fileManager.setPassword(testPassword);
        fileManager.setEncryptionEnabled(true);
        metadataManager.setPassword(testPassword);
        metadataManager.setEncryptionEnabled(true);
        
        // Create test file
        File testFile = tempDir.resolve("upload_test.txt").toFile();
        String testContent = "This is a test file for upload flow";
        Files.write(testFile.toPath(), testContent.getBytes());
        
        // Simulate upload process
        VaultFile vaultFile = fileManager.storeFile(testFile);
        assertNotNull(vaultFile, "File should be stored successfully");
        
        // Add to metadata
        metadataManager.addFile(vaultFile);
        
        // Verify file is in metadata
        assertTrue(metadataManager.getFiles().contains(vaultFile), 
                  "File should be in metadata manager");
        
        // Verify file can be retrieved with correct content
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertEquals(testContent, new String(retrievedContent), 
                    "Retrieved content should match original");
        
        // Verify metadata persistence (save and reload)
        metadataManager.saveMetadata();
        
        // Create new metadata manager and load
        MetadataManager newMetadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        newMetadataManager.setPassword(testPassword);
        newMetadataManager.setEncryptionEnabled(true);
        newMetadataManager.loadMetadata();
        
        // Verify file is still in metadata after reload
        boolean fileFound = newMetadataManager.getFiles().stream()
                .anyMatch(f -> f.getFileId().equals(vaultFile.getFileId()));
        assertTrue(fileFound, "File should be found in reloaded metadata");
    }
}