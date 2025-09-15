package com.ghostvault.integration;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.security.SecurityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complete vault workflows
 * Tests end-to-end functionality including upload, download, delete operations
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VaultWorkflowIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private SecurityManager securityManager;
    private SecretKey masterKey;
    
    // Test data
    private static final String MASTER_PASSWORD = "MasterPass123!@#";
    private static final String PANIC_PASSWORD = "PanicPass456$%^";
    private static final String DECOY_PASSWORD = "DecoyPass789&*()";
    private static final String TEST_FILE_CONTENT = "This is a test file for integration testing.";
    
    @BeforeEach
    void setUp() throws Exception {
        // Set up temporary vault directory
        AppConfig.VAULT_DIR = tempDir.resolve("vault").toString();
        Files.createDirectories(Path.of(AppConfig.VAULT_DIR));
        
        // Initialize components
        cryptoManager = new CryptoManager();
        passwordManager = new PasswordManager(cryptoManager);
        auditManager = new AuditManager();
        fileManager = new FileManager(cryptoManager, auditManager);
        metadataManager = new MetadataManager(cryptoManager);
        securityManager = new SecurityManager(passwordManager, cryptoManager, auditManager);
        
        // Initialize vault with passwords
        securityManager.initializeVault(MASTER_PASSWORD, PANIC_PASSWORD, DECOY_PASSWORD);
        
        // Derive master key for operations
        masterKey = passwordManager.deriveKey(MASTER_PASSWORD);
    }
    
    @Test
    @Order(1)
    @DisplayName("Complete file upload workflow")
    void testCompleteFileUploadWorkflow() throws Exception {
        // Create test file
        File testFile = createTestFile("test_upload.txt", TEST_FILE_CONTENT);
        
        // Upload file
        String fileId = fileManager.storeFile(testFile, masterKey);
        assertNotNull(fileId);
        assertFalse(fileId.isEmpty());
        
        // Verify file was encrypted and stored
        Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileId + ".enc");
        assertTrue(Files.exists(encryptedFile));
        
        // Verify encrypted content is different from original
        byte[] encryptedContent = Files.readAllBytes(encryptedFile);
        assertNotEquals(TEST_FILE_CONTENT, new String(encryptedContent));
        
        // Verify metadata was created
        MetadataManager.FileMetadata metadata = metadataManager.getFileMetadata("test_upload.txt");
        assertNotNull(metadata);
        assertEquals("test_upload.txt", metadata.getOriginalName());
        assertEquals(fileId, metadata.getFileId());
        
        // Verify audit log entry
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_UPLOADED") && 
            event.getDescription().contains("test_upload.txt")));
    }
    
    @Test
    @Order(2)
    @DisplayName("Complete file download workflow")
    void testCompleteFileDownloadWorkflow() throws Exception {
        // First upload a file
        File originalFile = createTestFile("test_download.txt", TEST_FILE_CONTENT);
        String fileId = fileManager.storeFile(originalFile, masterKey);
        
        // Download file
        File downloadedFile = tempDir.resolve("downloaded_test.txt").toFile();
        fileManager.retrieveFile(fileId, downloadedFile, masterKey);
        
        // Verify downloaded file exists and has correct content
        assertTrue(downloadedFile.exists());
        String downloadedContent = Files.readString(downloadedFile.toPath());
        assertEquals(TEST_FILE_CONTENT, downloadedContent);
        
        // Verify audit log entry
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_DOWNLOADED") && 
            event.getDescription().contains("test_download.txt")));
    }
    
    @Test
    @Order(3)
    @DisplayName("Complete file deletion workflow")
    void testCompleteFileDeletionWorkflow() throws Exception {
        // First upload a file
        File testFile = createTestFile("test_delete.txt", TEST_FILE_CONTENT);
        String fileId = fileManager.storeFile(testFile, masterKey);
        
        // Verify file exists
        Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileId + ".enc");
        assertTrue(Files.exists(encryptedFile));
        
        // Delete file
        fileManager.secureDelete(fileId);
        
        // Verify encrypted file is deleted
        assertFalse(Files.exists(encryptedFile));
        
        // Verify metadata is removed
        assertThrows(Exception.class, () -> {
            metadataManager.getFileMetadata("test_delete.txt");
        });
        
        // Verify audit log entry
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_DELETED") && 
            event.getDescription().contains("test_delete.txt")));
    }
    
    @Test
    @Order(4)
    @DisplayName("Multiple file operations workflow")
    void testMultipleFileOperationsWorkflow() throws Exception {
        // Upload multiple files
        String[] fileNames = {"file1.txt", "file2.txt", "file3.txt"};
        String[] fileIds = new String[fileNames.length];
        
        for (int i = 0; i < fileNames.length; i++) {
            File testFile = createTestFile(fileNames[i], "Content of " + fileNames[i]);
            fileIds[i] = fileManager.storeFile(testFile, masterKey);
        }
        
        // Verify all files are stored
        for (String fileId : fileIds) {
            Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileId + ".enc");
            assertTrue(Files.exists(encryptedFile));
        }
        
        // Search for files
        List<MetadataManager.FileMetadata> searchResults = metadataManager.searchFiles("file");
        assertEquals(3, searchResults.size());
        
        // Download one file
        File downloadedFile = tempDir.resolve("downloaded_file1.txt").toFile();
        fileManager.retrieveFile(fileIds[0], downloadedFile, masterKey);
        assertTrue(downloadedFile.exists());
        
        // Delete one file
        fileManager.secureDelete(fileIds[1]);
        
        // Verify remaining files
        List<MetadataManager.FileMetadata> remainingFiles = metadataManager.searchFiles("file");
        assertEquals(2, remainingFiles.size());
    }
    
    @Test
    @Order(5)
    @DisplayName("Password authentication workflow")
    void testPasswordAuthenticationWorkflow() throws Exception {
        // Test master password
        SecurityManager.PasswordType masterType = securityManager.validatePassword(MASTER_PASSWORD);
        assertEquals(SecurityManager.PasswordType.MASTER, masterType);
        
        // Test panic password
        SecurityManager.PasswordType panicType = securityManager.validatePassword(PANIC_PASSWORD);
        assertEquals(SecurityManager.PasswordType.PANIC, panicType);
        
        // Test decoy password
        SecurityManager.PasswordType decoyType = securityManager.validatePassword(DECOY_PASSWORD);
        assertEquals(SecurityManager.PasswordType.DECOY, decoyType);
        
        // Test invalid password
        SecurityManager.PasswordType invalidType = securityManager.validatePassword("WrongPassword123");
        assertEquals(SecurityManager.PasswordType.INVALID, invalidType);
        
        // Verify audit logs for authentication attempts
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("LOGIN_SUCCESS")));
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("LOGIN_FAILED")));
    }
    
    @Test
    @Order(6)
    @DisplayName("File integrity verification workflow")
    void testFileIntegrityVerificationWorkflow() throws Exception {
        // Upload file
        File testFile = createTestFile("integrity_test.txt", TEST_FILE_CONTENT);
        String fileId = fileManager.storeFile(testFile, masterKey);
        
        // Verify integrity immediately after upload
        assertTrue(fileManager.verifyFileIntegrity(fileId, masterKey));
        
        // Corrupt the encrypted file
        Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileId + ".enc");
        byte[] originalContent = Files.readAllBytes(encryptedFile);
        byte[] corruptedContent = originalContent.clone();
        corruptedContent[corruptedContent.length / 2] = (byte) ~corruptedContent[corruptedContent.length / 2];
        Files.write(encryptedFile, corruptedContent);
        
        // Verify integrity detection
        assertFalse(fileManager.verifyFileIntegrity(fileId, masterKey));
        
        // Verify audit log for integrity failure
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("INTEGRITY_CHECK_FAILED")));
    }
    
    @Test
    @Order(7)
    @DisplayName("Large file handling workflow")
    void testLargeFileHandlingWorkflow() throws Exception {
        // Create large test file (1MB)
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("This is line ").append(i).append(" of the large test file.\n");
        }
        
        File largeFile = createTestFile("large_file.txt", largeContent.toString());
        assertTrue(largeFile.length() > 500000); // At least 500KB
        
        // Upload large file
        String fileId = fileManager.storeFile(largeFile, masterKey);
        assertNotNull(fileId);
        
        // Verify encrypted file exists
        Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileId + ".enc");
        assertTrue(Files.exists(encryptedFile));
        
        // Download large file
        File downloadedFile = tempDir.resolve("downloaded_large.txt").toFile();
        fileManager.retrieveFile(fileId, downloadedFile, masterKey);
        
        // Verify content integrity
        String downloadedContent = Files.readString(downloadedFile.toPath());
        assertEquals(largeContent.toString(), downloadedContent);
        
        // Verify metadata
        MetadataManager.FileMetadata metadata = metadataManager.getFileMetadata("large_file.txt");
        assertEquals(largeFile.length(), metadata.getOriginalSize());
    }
    
    @Test
    @Order(8)
    @DisplayName("Concurrent file operations workflow")
    void testConcurrentFileOperationsWorkflow() throws Exception {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        String[] fileIds = new String[threadCount];
        
        // Concurrent file uploads
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    File testFile = createTestFile("concurrent_" + threadIndex + ".txt", 
                        "Content from thread " + threadIndex);
                    fileIds[threadIndex] = fileManager.storeFile(testFile, masterKey);
                } catch (Exception e) {
                    fail("Concurrent upload failed: " + e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeout
        }
        
        // Verify all files were uploaded
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(fileIds[i], "File ID should not be null for thread " + i);
            Path encryptedFile = Path.of(AppConfig.VAULT_DIR, "files", fileIds[i] + ".enc");
            assertTrue(Files.exists(encryptedFile), "Encrypted file should exist for thread " + i);
        }
        
        // Verify metadata for all files
        List<MetadataManager.FileMetadata> allFiles = metadataManager.searchFiles("concurrent");
        assertEquals(threadCount, allFiles.size());
    }
    
    @Test
    @Order(9)
    @DisplayName("Error recovery workflow")
    void testErrorRecoveryWorkflow() throws Exception {
        // Test recovery from file not found
        assertThrows(Exception.class, () -> {
            fileManager.retrieveFile("nonexistent_file_id", 
                tempDir.resolve("nonexistent.txt").toFile(), masterKey);
        });
        
        // Test recovery from invalid key
        File testFile = createTestFile("key_test.txt", TEST_FILE_CONTENT);
        String fileId = fileManager.storeFile(testFile, masterKey);
        
        SecretKey wrongKey = CryptoManager.generateKey();
        assertThrows(Exception.class, () -> {
            fileManager.retrieveFile(fileId, tempDir.resolve("wrong_key.txt").toFile(), wrongKey);
        });
        
        // Verify original file can still be retrieved with correct key
        File correctDownload = tempDir.resolve("correct_key.txt").toFile();
        assertDoesNotThrow(() -> {
            fileManager.retrieveFile(fileId, correctDownload, masterKey);
        });
        
        String content = Files.readString(correctDownload.toPath());
        assertEquals(TEST_FILE_CONTENT, content);
    }
    
    @Test
    @Order(10)
    @DisplayName("Audit trail completeness workflow")
    void testAuditTrailCompletenessWorkflow() throws Exception {
        // Perform various operations
        File testFile = createTestFile("audit_test.txt", TEST_FILE_CONTENT);
        String fileId = fileManager.storeFile(testFile, masterKey);
        
        File downloadFile = tempDir.resolve("audit_download.txt").toFile();
        fileManager.retrieveFile(fileId, downloadFile, masterKey);
        
        fileManager.secureDelete(fileId);
        
        // Verify comprehensive audit trail
        List<AuditManager.AuditEvent> events = auditManager.getAuditEvents();
        
        // Should have upload, download, and delete events
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_UPLOADED")));
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_DOWNLOADED")));
        assertTrue(events.stream().anyMatch(event -> 
            event.getEventType().equals("FILE_DELETED")));
        
        // Verify event ordering and timestamps
        AuditManager.AuditEvent uploadEvent = events.stream()
            .filter(event -> event.getEventType().equals("FILE_UPLOADED"))
            .findFirst().orElse(null);
        
        AuditManager.AuditEvent deleteEvent = events.stream()
            .filter(event -> event.getEventType().equals("FILE_DELETED"))
            .findFirst().orElse(null);
        
        assertNotNull(uploadEvent);
        assertNotNull(deleteEvent);
        assertTrue(uploadEvent.getTimestamp().isBefore(deleteEvent.getTimestamp()));
    }
    
    /**
     * Helper method to create test files
     */
    private File createTestFile(String fileName, String content) throws IOException {
        File testFile = tempDir.resolve(fileName).toFile();
        Files.writeString(testFile.toPath(), content);
        return testFile;
    }
    
    @AfterEach
    void tearDown() {
        // Clean up resources
        if (auditManager != null) {
            auditManager.cleanup();
        }
    }
}