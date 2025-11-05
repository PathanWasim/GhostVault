package com.ghostvault.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for FileMigrationUtility
 */
public class FileMigrationUtilityTest {
    
    @TempDir
    Path tempDir;
    
    private FileMigrationUtility migrationUtility;
    private Path filesDir;
    private final String testPassword = "TestPassword123!";
    private final String testFileContent = "This is test file content for encryption testing.";
    
    @BeforeEach
    void setUp() throws Exception {
        migrationUtility = new FileMigrationUtility(tempDir.toString());
        filesDir = tempDir.resolve("files");
        Files.createDirectories(filesDir);
    }
    
    @Test
    @DisplayName("Should detect unencrypted files")
    void shouldDetectUnencryptedFiles() throws Exception {
        // Create unencrypted test files
        createUnencryptedFile("test1.dat");
        createUnencryptedFile("test2.dat");
        
        List<Path> unencryptedFiles = migrationUtility.scanForUnencryptedFiles();
        assertEquals(2, unencryptedFiles.size());
        assertTrue(migrationUtility.isMigrationNeeded());
    }
    
    @Test
    @DisplayName("Should not detect encrypted files as needing migration")
    void shouldNotDetectEncryptedFilesAsNeedingMigration() throws Exception {
        // Create encrypted test file
        createEncryptedFile("encrypted.dat");
        
        List<Path> unencryptedFiles = migrationUtility.scanForUnencryptedFiles();
        assertEquals(0, unencryptedFiles.size());
        assertFalse(migrationUtility.isMigrationNeeded());
    }
    
    @Test
    @DisplayName("Should perform successful migration")
    void shouldPerformSuccessfulMigration() throws Exception {
        // Create unencrypted test files
        createUnencryptedFile("test1.dat");
        createUnencryptedFile("test2.dat");
        
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessCount());
        assertEquals(2, result.getTotalCount());
        assertNotNull(result.getBackupPath());
        
        // Verify files are now encrypted
        assertFalse(migrationUtility.isMigrationNeeded());
        
        FileMigrationUtility.FileEncryptionStatus status = migrationUtility.getEncryptionStatus();
        assertEquals(2, status.getEncryptedCount());
        assertEquals(0, status.getUnencryptedCount());
        assertTrue(status.isFullyEncrypted());
    }
    
    @Test
    @DisplayName("Should create backup during migration")
    void shouldCreateBackupDuringMigration() throws Exception {
        createUnencryptedFile("test.dat");
        
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBackupPath());
        
        // Verify backup exists and contains original data
        Path backupDir = Path.of(result.getBackupPath());
        assertTrue(Files.exists(backupDir));
        
        Path backupFile = backupDir.resolve("test.dat");
        assertTrue(Files.exists(backupFile));
        
        String backupContent = new String(Files.readAllBytes(backupFile));
        assertEquals(testFileContent, backupContent);
    }
    
    @Test
    @DisplayName("Should handle migration with no files")
    void shouldHandleMigrationWithNoFiles() {
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getMessage().contains("No files need migration"));
    }
    
    @Test
    @DisplayName("Should handle invalid password")
    void shouldHandleInvalidPassword() throws Exception {
        createUnencryptedFile("test.dat");
        
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Password is required"));
        
        result = migrationUtility.performMigration("");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Password is required"));
    }
    
    @Test
    @DisplayName("Should rollback migration on failure")
    void shouldRollbackMigrationOnFailure() throws Exception {
        createUnencryptedFile("test.dat");
        
        // Perform successful migration first
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        assertTrue(result.isSuccess());
        
        // Test rollback
        boolean rollbackSuccess = migrationUtility.rollbackMigration(result.getBackupPath());
        assertTrue(rollbackSuccess);
        
        // Verify file is back to unencrypted
        Path testFile = filesDir.resolve("test.dat");
        byte[] fileData = Files.readAllBytes(testFile);
        assertFalse(EncryptedFileData.isEncryptedFileFormat(fileData));
        assertEquals(testFileContent, new String(fileData));
    }
    
    @Test
    @DisplayName("Should handle rollback with invalid backup path")
    void shouldHandleRollbackWithInvalidBackupPath() {
        assertFalse(migrationUtility.rollbackMigration(null));
        assertFalse(migrationUtility.rollbackMigration("nonexistent/path"));
    }
    
    @Test
    @DisplayName("Should provide encryption status")
    void shouldProvideEncryptionStatus() throws Exception {
        // Create mixed files
        createUnencryptedFile("unencrypted.dat");
        createEncryptedFile("encrypted.dat");
        
        FileMigrationUtility.FileEncryptionStatus status = migrationUtility.getEncryptionStatus();
        
        assertEquals(2, status.getTotalCount());
        assertEquals(1, status.getEncryptedCount());
        assertEquals(1, status.getUnencryptedCount());
        assertEquals(50.0, status.getEncryptionPercentage(), 0.1);
        assertFalse(status.isFullyEncrypted());
        assertTrue(status.hasUnencryptedFiles());
    }
    
    @Test
    @DisplayName("Should handle empty vault")
    void shouldHandleEmptyVault() {
        FileMigrationUtility.FileEncryptionStatus status = migrationUtility.getEncryptionStatus();
        
        assertEquals(0, status.getTotalCount());
        assertEquals(0, status.getEncryptedCount());
        assertEquals(0, status.getUnencryptedCount());
        assertEquals(100.0, status.getEncryptionPercentage(), 0.1); // Empty vault is "fully encrypted"
        assertFalse(status.isFullyEncrypted()); // But not really since no files
        assertFalse(status.hasUnencryptedFiles());
    }
    
    @Test
    @DisplayName("Should handle mixed file types during migration")
    void shouldHandleMixedFileTypesDuringMigration() throws Exception {
        // Create files with different content types
        createUnencryptedFile("text.dat", "Text file content");
        createUnencryptedFile("binary.dat", new byte[]{0x00, 0x01, 0x02, (byte)0xFF});
        createEncryptedFile("already_encrypted.dat");
        
        FileMigrationUtility.MigrationResult result = migrationUtility.performMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessCount()); // Only 2 unencrypted files
        assertEquals(2, result.getTotalCount());
        
        // Verify all files are now encrypted
        FileMigrationUtility.FileEncryptionStatus status = migrationUtility.getEncryptionStatus();
        assertEquals(3, status.getTotalCount());
        assertEquals(3, status.getEncryptedCount());
        assertEquals(0, status.getUnencryptedCount());
        assertTrue(status.isFullyEncrypted());
    }
    
    private void createUnencryptedFile(String fileName) throws Exception {
        createUnencryptedFile(fileName, testFileContent);
    }
    
    private void createUnencryptedFile(String fileName, String content) throws Exception {
        Path filePath = filesDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
    }
    
    private void createUnencryptedFile(String fileName, byte[] content) throws Exception {
        Path filePath = filesDir.resolve(fileName);
        Files.write(filePath, content);
    }
    
    private void createEncryptedFile(String fileName) throws Exception {
        // Create a properly formatted encrypted file
        byte[] salt = new byte[32];
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[48]; // Includes auth tag
        
        new java.security.SecureRandom().nextBytes(salt);
        new java.security.SecureRandom().nextBytes(iv);
        new java.security.SecureRandom().nextBytes(ciphertext);
        
        EncryptedFileData encryptedData = new EncryptedFileData(salt, iv, ciphertext);
        Path filePath = filesDir.resolve(fileName);
        Files.write(filePath, encryptedData.toByteArray());
    }
}