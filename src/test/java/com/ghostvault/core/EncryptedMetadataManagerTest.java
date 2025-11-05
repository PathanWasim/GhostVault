package com.ghostvault.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for EncryptedMetadataManager
 */
public class EncryptedMetadataManagerTest {
    
    @TempDir
    Path tempDir;
    
    private EncryptedMetadataManager metadataManager;
    private final String testPassword = "TestPassword123!";
    private final String testMetadata = "{\"files\":[{\"fileName\":\"test.txt\",\"fileId\":\"test123\",\"size\":1024,\"mimeType\":\"text/plain\"}],\"version\":\"1.0\"}";
    
    @BeforeEach
    void setUp() {
        metadataManager = new EncryptedMetadataManager(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should save and load encrypted metadata")
    void shouldSaveAndLoadEncryptedMetadata() throws Exception {
        // Save metadata
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        // Verify encrypted file exists
        assertTrue(metadataManager.hasEncryptedMetadata());
        
        // Load and verify metadata
        String loadedMetadata = metadataManager.loadEncryptedMetadata(testPassword);
        assertEquals(testMetadata, loadedMetadata);
    }
    
    @Test
    @DisplayName("Should not store metadata in plain text")
    void shouldNotStoreMetadataInPlainText() throws Exception {
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        Path encryptedFile = tempDir.resolve("metadata.enc");
        assertTrue(Files.exists(encryptedFile));
        
        // Read file content and verify it doesn't contain plain text
        byte[] fileContent = Files.readAllBytes(encryptedFile);
        String fileContentStr = new String(fileContent);
        
        assertFalse(fileContentStr.contains("test.txt"));
        assertFalse(fileContentStr.contains("test123"));
        assertFalse(fileContentStr.contains("text/plain"));
    }
    
    @Test
    @DisplayName("Should fail decryption with wrong password")
    void shouldFailDecryptionWithWrongPassword() throws Exception {
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        assertThrows(Exception.class, () -> {
            metadataManager.loadEncryptedMetadata("WrongPassword");
        });
    }
    
    @Test
    @DisplayName("Should detect encrypted metadata format")
    void shouldDetectEncryptedMetadataFormat() throws Exception {
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        Path encryptedFile = tempDir.resolve("metadata.enc");
        byte[] fileData = Files.readAllBytes(encryptedFile);
        
        assertTrue(EncryptedMetadataManager.isEncryptedMetadataFormat(fileData));
        
        // Test with invalid data
        byte[] invalidData = "This is not encrypted metadata".getBytes();
        assertFalse(EncryptedMetadataManager.isEncryptedMetadataFormat(invalidData));
    }
    
    @Test
    @DisplayName("Should detect migration needs correctly")
    void shouldDetectMigrationNeedsCorrectly() throws Exception {
        // Initially no migration needed
        assertFalse(metadataManager.needsMigration());
        
        // Create plain text metadata file
        Path plainFile = tempDir.resolve("metadata.json");
        Files.write(plainFile, testMetadata.getBytes());
        
        // Now migration should be needed
        assertTrue(metadataManager.needsMigration());
        
        // After migration, should not need migration
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        assertFalse(metadataManager.needsMigration());
    }
    
    @Test
    @DisplayName("Should migrate from plain text successfully")
    void shouldMigrateFromPlainTextSuccessfully() throws Exception {
        // Create plain text metadata file
        Path plainFile = tempDir.resolve("metadata.json");
        Files.write(plainFile, testMetadata.getBytes());
        
        // Perform migration
        boolean migrationSuccess = metadataManager.migrateFromPlainText(testPassword);
        assertTrue(migrationSuccess);
        
        // Verify encrypted metadata exists and is correct
        assertTrue(metadataManager.hasEncryptedMetadata());
        String loadedMetadata = metadataManager.loadEncryptedMetadata(testPassword);
        assertEquals(testMetadata, loadedMetadata);
        
        // Verify plain text file is removed
        assertFalse(Files.exists(plainFile));
    }
    
    @Test
    @DisplayName("Should handle invalid JSON during migration")
    void shouldHandleInvalidJsonDuringMigration() throws Exception {
        // Create invalid JSON file
        Path plainFile = tempDir.resolve("metadata.json");
        Files.write(plainFile, "invalid json content".getBytes());
        
        // Migration should fail
        boolean migrationSuccess = metadataManager.migrateFromPlainText(testPassword);
        assertFalse(migrationSuccess);
    }
    
    @Test
    @DisplayName("Should verify metadata integrity")
    void shouldVerifyMetadataIntegrity() throws Exception {
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        // Correct password should verify successfully
        assertTrue(metadataManager.verifyMetadataIntegrity(testPassword));
        
        // Wrong password should fail verification
        assertFalse(metadataManager.verifyMetadataIntegrity("WrongPassword"));
    }
    
    @Test
    @DisplayName("Should get encrypted metadata size")
    void shouldGetEncryptedMetadataSize() throws Exception {
        // Initially no file
        assertEquals(-1, metadataManager.getEncryptedMetadataSize());
        
        // After saving metadata
        metadataManager.saveEncryptedMetadata(testMetadata, testPassword);
        
        long size = metadataManager.getEncryptedMetadataSize();
        assertTrue(size > 0);
        assertTrue(size > testMetadata.length()); // Should be larger due to encryption overhead
    }
    
    @Test
    @DisplayName("Should handle null and empty inputs gracefully")
    void shouldHandleNullAndEmptyInputsGracefully() {
        assertThrows(IllegalArgumentException.class, () -> {
            metadataManager.saveEncryptedMetadata(null, testPassword);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            metadataManager.saveEncryptedMetadata(testMetadata, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            metadataManager.loadEncryptedMetadata(null);
        });
    }
    
    @Test
    @DisplayName("Should return null for non-existent metadata")
    void shouldReturnNullForNonExistentMetadata() throws Exception {
        String loadedMetadata = metadataManager.loadEncryptedMetadata(testPassword);
        assertNull(loadedMetadata);
        
        assertFalse(metadataManager.hasEncryptedMetadata());
    }
    
    @Test
    @DisplayName("Should handle large metadata")
    void shouldHandleLargeMetadata() throws Exception {
        // Create large metadata (simulate many files)
        StringBuilder largeMetadata = new StringBuilder();
        largeMetadata.append("{\"files\":[");
        
        for (int i = 0; i < 1000; i++) {
            if (i > 0) largeMetadata.append(",");
            largeMetadata.append("{\"fileName\":\"file").append(i).append(".txt\",");
            largeMetadata.append("\"fileId\":\"id").append(i).append("\",");
            largeMetadata.append("\"size\":").append(i * 1024).append(",");
            largeMetadata.append("\"mimeType\":\"text/plain\"}");
        }
        
        largeMetadata.append("],\"version\":\"1.0\"}");
        
        String largeMetadataStr = largeMetadata.toString();
        
        // Save and load large metadata
        metadataManager.saveEncryptedMetadata(largeMetadataStr, testPassword);
        String loadedMetadata = metadataManager.loadEncryptedMetadata(testPassword);
        
        assertEquals(largeMetadataStr, loadedMetadata);
    }
}