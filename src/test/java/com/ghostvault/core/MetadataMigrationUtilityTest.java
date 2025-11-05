package com.ghostvault.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for MetadataMigrationUtility
 */
public class MetadataMigrationUtilityTest {
    
    @TempDir
    Path tempDir;
    
    private MetadataMigrationUtility migrationUtility;
    private final String testPassword = "TestPassword123!";
    private final String validMetadata = "[{\"fileName\":\"test.txt\",\"fileId\":\"test123\",\"size\":1024,\"mimeType\":\"text/plain\"}]";
    
    @BeforeEach
    void setUp() {
        migrationUtility = new MetadataMigrationUtility(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should detect when migration is needed")
    void shouldDetectWhenMigrationIsNeeded() throws Exception {
        // Initially no migration needed
        assertFalse(migrationUtility.isMigrationNeeded());
        
        // Create plain text metadata file
        createPlainTextMetadataFile();
        
        // Now migration should be needed
        assertTrue(migrationUtility.isMigrationNeeded());
    }
    
    @Test
    @DisplayName("Should perform successful migration")
    void shouldPerformSuccessfulMigration() throws Exception {
        // Create plain text metadata file
        createPlainTextMetadataFile();
        
        // Perform migration
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBackupPath());
        assertTrue(result.getMessage().contains("successfully"));
        
        // Verify encrypted metadata exists
        Path encryptedFile = tempDir.resolve("metadata.enc");
        assertTrue(Files.exists(encryptedFile));
        
        // Verify plain text file is removed
        Path plainFile = tempDir.resolve("metadata.json");
        assertFalse(Files.exists(plainFile));
        
        // Verify backup exists
        Path backupFile = Path.of(result.getBackupPath());
        assertTrue(Files.exists(backupFile));
        
        String backupContent = new String(Files.readAllBytes(backupFile));
        assertEquals(validMetadata, backupContent);
    }
    
    @Test
    @DisplayName("Should create backup during migration")
    void shouldCreateBackupDuringMigration() throws Exception {
        createPlainTextMetadataFile();
        
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBackupPath());
        
        // Verify backup file exists and contains original data
        Path backupFile = Path.of(result.getBackupPath());
        assertTrue(Files.exists(backupFile));
        
        String backupContent = new String(Files.readAllBytes(backupFile));
        assertEquals(validMetadata, backupContent);
    }
    
    @Test
    @DisplayName("Should handle migration with no plain text file")
    void shouldHandleMigrationWithNoPlainTextFile() {
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No plain text metadata file found"));
    }
    
    @Test
    @DisplayName("Should handle invalid password")
    void shouldHandleInvalidPassword() throws Exception {
        createPlainTextMetadataFile();
        
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Password is required"));
        
        result = migrationUtility.performMigration("");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Password is required"));
    }
    
    @Test
    @DisplayName("Should handle invalid JSON format")
    void shouldHandleInvalidJsonFormat() throws Exception {
        // Create invalid JSON file
        Path plainFile = tempDir.resolve("metadata.json");
        Files.write(plainFile, "invalid json content".getBytes());
        
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid metadata format"));
    }
    
    @Test
    @DisplayName("Should rollback migration on failure")
    void shouldRollbackMigrationOnFailure() throws Exception {
        createPlainTextMetadataFile();
        
        // Perform successful migration first
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        assertTrue(result.isSuccess());
        
        // Test rollback
        boolean rollbackSuccess = migrationUtility.rollbackMigration(result.getBackupPath());
        assertTrue(rollbackSuccess);
        
        // Verify plain text file is restored
        Path plainFile = tempDir.resolve("metadata.json");
        assertTrue(Files.exists(plainFile));
        
        String restoredContent = new String(Files.readAllBytes(plainFile));
        assertEquals(validMetadata, restoredContent);
        
        // Verify encrypted file is removed
        Path encryptedFile = tempDir.resolve("metadata.enc");
        assertFalse(Files.exists(encryptedFile));
    }
    
    @Test
    @DisplayName("Should handle rollback with invalid backup path")
    void shouldHandleRollbackWithInvalidBackupPath() {
        assertFalse(migrationUtility.rollbackMigration(null));
        assertFalse(migrationUtility.rollbackMigration("nonexistent/path"));
    }
    
    @Test
    @DisplayName("Should provide migration status")
    void shouldProvideMigrationStatus() throws Exception {
        // Initially no files
        MetadataMigrationUtility.MetadataMigrationStatus status = migrationUtility.getMigrationStatus();
        assertFalse(status.hasPlainText());
        assertFalse(status.hasEncrypted());
        assertFalse(status.needsMigration());
        assertTrue(status.isFullyMigrated()); // No files means "migrated"
        
        // Create plain text file
        createPlainTextMetadataFile();
        
        status = migrationUtility.getMigrationStatus();
        assertTrue(status.hasPlainText());
        assertFalse(status.hasEncrypted());
        assertTrue(status.needsMigration());
        assertFalse(status.isFullyMigrated());
        assertTrue(status.getPlainTextSize() > 0);
        
        // Perform migration
        migrationUtility.performMigration(testPassword);
        
        status = migrationUtility.getMigrationStatus();
        assertFalse(status.hasPlainText());
        assertTrue(status.hasEncrypted());
        assertFalse(status.needsMigration());
        assertTrue(status.isFullyMigrated());
        assertTrue(status.getEncryptedSize() > 0);
    }
    
    @Test
    @DisplayName("Should validate metadata format correctly")
    void shouldValidateMetadataFormatCorrectly() throws Exception {
        // Valid array format
        String validArrayMetadata = "[{\"fileName\":\"test.txt\",\"fileId\":\"test123\"}]";
        createPlainTextMetadataFile(validArrayMetadata);
        
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        assertTrue(result.isSuccess());
        
        // Clean up for next test
        migrationUtility.rollbackMigration(result.getBackupPath());
        
        // Valid object format
        String validObjectMetadata = "{\"files\":[{\"fileName\":\"test.txt\",\"fileId\":\"test123\"}]}";
        Files.write(tempDir.resolve("metadata.json"), validObjectMetadata.getBytes());
        
        result = migrationUtility.performMigration(testPassword);
        assertTrue(result.isSuccess());
    }
    
    @Test
    @DisplayName("Should handle missing required fields")
    void shouldHandleMissingRequiredFields() throws Exception {
        // Missing fileId
        String invalidMetadata = "[{\"fileName\":\"test.txt\",\"size\":1024}]";
        createPlainTextMetadataFile(invalidMetadata);
        
        MetadataMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid metadata format"));
    }
    
    @Test
    @DisplayName("Should handle already migrated state")
    void shouldHandleAlreadyMigratedState() throws Exception {
        // First, perform a successful migration
        createPlainTextMetadataFile();
        MetadataMigrationUtility.MigrationResult firstResult = migrationUtility.performMigration(testPassword);
        assertTrue(firstResult.isSuccess());
        
        // Try to migrate again (should detect already migrated)
        MetadataMigrationUtility.MigrationResult secondResult = migrationUtility.performMigration(testPassword);
        
        assertFalse(secondResult.isSuccess());
        assertTrue(secondResult.getMessage().contains("Already using encrypted"));
    }
    
    private void createPlainTextMetadataFile() throws Exception {
        createPlainTextMetadataFile(validMetadata);
    }
    
    private void createPlainTextMetadataFile(String content) throws Exception {
        Path plainFile = tempDir.resolve("metadata.json");
        Files.write(plainFile, content.getBytes());
    }
}