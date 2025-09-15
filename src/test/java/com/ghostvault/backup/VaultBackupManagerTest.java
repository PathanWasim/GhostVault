package com.ghostvault.backup;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.exception.BackupException;
import com.ghostvault.security.CryptoManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for VaultBackupManager
 */
class VaultBackupManagerTest {
    
    @TempDir
    Path tempDir;
    
    private VaultBackupManager backupManager;
    private CryptoManager cryptoManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private SecretKey testKey;
    
    @BeforeEach
    void setUp() throws Exception {
        // Set up temporary vault directory
        AppConfig.VAULT_DIR = tempDir.resolve("vault").toString();
        Files.createDirectories(Path.of(AppConfig.VAULT_DIR));
        
        // Create mock dependencies
        cryptoManager = mock(CryptoManager.class);
        fileManager = mock(FileManager.class);
        metadataManager = mock(MetadataManager.class);
        auditManager = mock(AuditManager.class);
        
        // Create test key
        testKey = CryptoManager.generateKey();
        
        // Set up crypto manager mocks
        when(cryptoManager.encrypt(any(byte[].class), eq(testKey)))
            .thenAnswer(invocation -> {
                byte[] data = invocation.getArgument(0);
                byte[] iv = new byte[16];
                return new CryptoManager.EncryptedData(iv, data, null); // Simplified for testing
            });
        
        when(cryptoManager.decrypt(any(CryptoManager.EncryptedData.class), eq(testKey)))
            .thenAnswer(invocation -> {
                CryptoManager.EncryptedData encData = invocation.getArgument(0);
                return encData.getCiphertext(); // Simplified for testing
            });
        
        when(cryptoManager.calculateSHA256(any(byte[].class)))
            .thenReturn("test_checksum");
        
        // Create backup manager
        backupManager = new VaultBackupManager(cryptoManager, fileManager, metadataManager, auditManager);
    }
    
    @Test
    @DisplayName("Should create backup successfully with valid vault")
    void testCreateBackupSuccess() throws Exception {
        // Set up test vault structure
        createTestVaultStructure();
        
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        AtomicInteger progressCalls = new AtomicInteger(0);
        AtomicReference<String> lastMessage = new AtomicReference<>();
        
        // Create backup with progress callback
        assertDoesNotThrow(() -> {
            backupManager.createBackup(backupFile, testKey, (percentage, message) -> {
                progressCalls.incrementAndGet();
                lastMessage.set(message);
                assertTrue(percentage >= 0 && percentage <= 100);
                assertNotNull(message);
            });
        });
        
        // Verify backup file was created
        assertTrue(backupFile.exists());
        assertTrue(backupFile.length() > 0);
        
        // Verify progress callback was called
        assertTrue(progressCalls.get() > 0);
        assertEquals("Backup completed successfully", lastMessage.get());
        
        // Verify audit logging
        verify(auditManager).logSecurityEvent(
            eq("BACKUP_CREATED"), 
            anyString(), 
            eq(AuditManager.AuditSeverity.INFO), 
            isNull(), 
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should fail backup with null parameters")
    void testCreateBackupWithNullParameters() {
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        
        // Test null backup file
        BackupException exception1 = assertThrows(BackupException.class, () -> {
            backupManager.createBackup(null, testKey, null);
        });
        assertTrue(exception1.getMessage().contains("required"));
        
        // Test null key
        BackupException exception2 = assertThrows(BackupException.class, () -> {
            backupManager.createBackup(backupFile, null, null);
        });
        assertTrue(exception2.getMessage().contains("required"));
    }
    
    @Test
    @DisplayName("Should restore backup successfully")
    void testRestoreBackupSuccess() throws Exception {
        // Create a backup first
        createTestVaultStructure();
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        
        backupManager.createBackup(backupFile, testKey, null);
        
        // Clear vault directory to simulate restore scenario
        deleteDirectory(Path.of(AppConfig.VAULT_DIR));
        
        AtomicInteger progressCalls = new AtomicInteger(0);
        AtomicReference<String> lastMessage = new AtomicReference<>();
        
        // Restore backup
        assertDoesNotThrow(() -> {
            backupManager.restoreBackup(backupFile, testKey, (percentage, message) -> {
                progressCalls.incrementAndGet();
                lastMessage.set(message);
                assertTrue(percentage >= 0 && percentage <= 100);
                assertNotNull(message);
            });
        });
        
        // Verify vault was restored
        assertTrue(Files.exists(Path.of(AppConfig.VAULT_DIR)));
        
        // Verify progress callback was called
        assertTrue(progressCalls.get() > 0);
        assertEquals("Restore completed successfully", lastMessage.get());
        
        // Verify audit logging
        verify(auditManager).logSecurityEvent(
            eq("BACKUP_RESTORED"), 
            anyString(), 
            eq(AuditManager.AuditSeverity.INFO), 
            isNull(), 
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should fail restore with non-existent backup file")
    void testRestoreBackupWithNonExistentFile() {
        File nonExistentFile = tempDir.resolve("non_existent.gvbackup").toFile();
        
        BackupException exception = assertThrows(BackupException.class, () -> {
            backupManager.restoreBackup(nonExistentFile, testKey, null);
        });
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    @DisplayName("Should fail restore with null key")
    void testRestoreBackupWithNullKey() throws Exception {
        // Create a backup first
        createTestVaultStructure();
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        backupManager.createBackup(backupFile, testKey, null);
        
        BackupException exception = assertThrows(BackupException.class, () -> {
            backupManager.restoreBackup(backupFile, null, null);
        });
        
        assertTrue(exception.getMessage().contains("required"));
    }
    
    @Test
    @DisplayName("Should verify backup successfully")
    void testVerifyBackupSuccess() throws Exception {
        // Create a backup first
        createTestVaultStructure();
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        backupManager.createBackup(backupFile, testKey, null);
        
        // Verify backup
        VaultBackupManager.BackupInfo info = backupManager.verifyBackup(backupFile, testKey);
        
        assertNotNull(info);
        assertTrue(info.isValid());
        assertEquals("1.0", info.getVersion());
        assertNotNull(info.getCreationDate());
        assertTrue(info.getFileCount() >= 0);
        assertTrue(info.getTotalSize() >= 0);
    }
    
    @Test
    @DisplayName("Should fail verification with invalid backup file")
    void testVerifyBackupWithInvalidFile() throws Exception {
        // Create a fake backup file with invalid content
        File fakeBackupFile = tempDir.resolve("fake_backup.gvbackup").toFile();
        Files.write(fakeBackupFile.toPath(), "invalid backup content".getBytes());
        
        VaultBackupManager.BackupInfo info = backupManager.verifyBackup(fakeBackupFile, testKey);
        
        assertNotNull(info);
        assertFalse(info.isValid());
        assertNotNull(info.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should fail verification with non-existent file")
    void testVerifyBackupWithNonExistentFile() {
        File nonExistentFile = tempDir.resolve("non_existent.gvbackup").toFile();
        
        BackupException exception = assertThrows(BackupException.class, () -> {
            backupManager.verifyBackup(nonExistentFile, testKey);
        });
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    @DisplayName("Should handle backup with empty vault")
    void testBackupEmptyVault() throws Exception {
        // Ensure vault directory exists but is empty
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        Files.createDirectories(vaultPath);
        
        File backupFile = tempDir.resolve("empty_vault_backup.gvbackup").toFile();
        
        // Should not throw exception for empty vault
        assertDoesNotThrow(() -> {
            backupManager.createBackup(backupFile, testKey, null);
        });
        
        assertTrue(backupFile.exists());
        
        // Verify backup info
        VaultBackupManager.BackupInfo info = backupManager.verifyBackup(backupFile, testKey);
        assertTrue(info.isValid());
        assertEquals(0, info.getFileCount());
    }
    
    @Test
    @DisplayName("Should handle restore with corrupted backup")
    void testRestoreCorruptedBackup() throws Exception {
        // Create a backup first
        createTestVaultStructure();
        File backupFile = tempDir.resolve("test_backup.gvbackup").toFile();
        backupManager.createBackup(backupFile, testKey, null);
        
        // Corrupt the backup file
        byte[] originalData = Files.readAllBytes(backupFile.toPath());
        byte[] corruptedData = new byte[originalData.length];
        System.arraycopy(originalData, 0, corruptedData, 0, originalData.length);
        // Corrupt some bytes in the middle
        for (int i = 100; i < 200 && i < corruptedData.length; i++) {
            corruptedData[i] = (byte) ~corruptedData[i];
        }
        Files.write(backupFile.toPath(), corruptedData);
        
        // Should fail to restore corrupted backup
        assertThrows(BackupException.class, () -> {
            backupManager.restoreBackup(backupFile, testKey, null);
        });
        
        // Verify audit logging for failure
        verify(auditManager).logSecurityEvent(
            eq("BACKUP_RESTORE_FAILED"), 
            anyString(), 
            eq(AuditManager.AuditSeverity.ERROR), 
            isNull(), 
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle backup with large vault")
    void testBackupLargeVault() throws Exception {
        // Create vault with multiple files and directories
        createLargeTestVaultStructure();
        
        File backupFile = tempDir.resolve("large_vault_backup.gvbackup").toFile();
        
        assertDoesNotThrow(() -> {
            backupManager.createBackup(backupFile, testKey, (percentage, message) -> {
                // Verify progress is reported
                assertTrue(percentage >= 0 && percentage <= 100);
                assertNotNull(message);
            });
        });
        
        assertTrue(backupFile.exists());
        assertTrue(backupFile.length() > 1000); // Should be reasonably large
        
        // Verify backup
        VaultBackupManager.BackupInfo info = backupManager.verifyBackup(backupFile, testKey);
        assertTrue(info.isValid());
        assertTrue(info.getFileCount() > 1);
    }
    
    @Test
    @DisplayName("Should add correct file extension to backup")
    void testBackupFileExtension() throws Exception {
        createTestVaultStructure();
        
        // Test without extension
        File backupFileWithoutExt = tempDir.resolve("test_backup").toFile();
        
        backupManager.createBackup(backupFileWithoutExt, testKey, null);
        
        // Should create file with .gvbackup extension
        File expectedFile = tempDir.resolve("test_backup.gvbackup").toFile();
        assertTrue(expectedFile.exists());
        
        // Test with extension already present
        File backupFileWithExt = tempDir.resolve("test_backup2.gvbackup").toFile();
        
        backupManager.createBackup(backupFileWithExt, testKey, null);
        
        assertTrue(backupFileWithExt.exists());
        // Should not double the extension
        assertFalse(tempDir.resolve("test_backup2.gvbackup.gvbackup").toFile().exists());
    }
    
    @Test
    @DisplayName("Should handle concurrent backup operations")
    void testConcurrentBackupOperations() throws Exception {
        createTestVaultStructure();
        
        File backupFile1 = tempDir.resolve("concurrent_backup1.gvbackup").toFile();
        File backupFile2 = tempDir.resolve("concurrent_backup2.gvbackup").toFile();
        
        // Create two backup managers for concurrent operations
        VaultBackupManager backupManager2 = new VaultBackupManager(
            cryptoManager, fileManager, metadataManager, auditManager);
        
        // Run backups concurrently
        Thread thread1 = new Thread(() -> {
            try {
                backupManager.createBackup(backupFile1, testKey, null);
            } catch (Exception e) {
                fail("Concurrent backup 1 failed: " + e.getMessage());
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                backupManager2.createBackup(backupFile2, testKey, null);
            } catch (Exception e) {
                fail("Concurrent backup 2 failed: " + e.getMessage());
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join(10000); // Wait up to 10 seconds
        thread2.join(10000);
        
        // Both backups should succeed
        assertTrue(backupFile1.exists());
        assertTrue(backupFile2.exists());
    }
    
    /**
     * Create test vault structure
     */
    private void createTestVaultStructure() throws Exception {
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        
        // Create directories
        Files.createDirectories(vaultPath.resolve("files"));
        Files.createDirectories(vaultPath.resolve("config"));
        
        // Create test files
        Files.write(vaultPath.resolve("files/test1.enc"), "encrypted file 1".getBytes());
        Files.write(vaultPath.resolve("files/test2.enc"), "encrypted file 2".getBytes());
        Files.write(vaultPath.resolve("metadata.enc"), "encrypted metadata".getBytes());
        Files.write(vaultPath.resolve("config/passwords.enc"), "encrypted passwords".getBytes());
        Files.write(vaultPath.resolve("config/.salt"), "salt data".getBytes());
    }
    
    /**
     * Create large test vault structure
     */
    private void createLargeTestVaultStructure() throws Exception {
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        
        // Create directories
        Files.createDirectories(vaultPath.resolve("files"));
        Files.createDirectories(vaultPath.resolve("config"));
        
        // Create multiple test files
        for (int i = 0; i < 10; i++) {
            String content = "encrypted file content " + i + " ".repeat(100); // Make files larger
            Files.write(vaultPath.resolve("files/test" + i + ".enc"), content.getBytes());
        }
        
        Files.write(vaultPath.resolve("metadata.enc"), "large encrypted metadata".getBytes());
        Files.write(vaultPath.resolve("config/passwords.enc"), "encrypted passwords".getBytes());
        Files.write(vaultPath.resolve("config/.salt"), "salt data".getBytes());
    }
    
    /**
     * Delete directory recursively
     */
    private void deleteDirectory(Path directory) throws Exception {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order for deletion
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (Exception e) {
                        // Ignore deletion errors in tests
                    }
                });
        }
    }
}