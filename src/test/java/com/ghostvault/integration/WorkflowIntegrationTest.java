package com.ghostvault.integration;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.decoy.DecoyManager;
import com.ghostvault.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complete workflows
 * Tests end-to-end functionality including upload, download, delete operations
 */
class WorkflowIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private DecoyManager decoyManager;
    private SessionManager sessionManager;
    
    private SecretKey masterKey;
    private String masterPassword = "MasterPass123!@#";
    private String panicPassword = "PanicPass456$%^";
    private String decoyPassword = "DecoyPass789&*()";
    
    @BeforeEach
    void setUp() throws Exception {
        // Set up temporary vault directory
        AppConfig.VAULT_DIR = tempDir.resolve("vault").toString();
        Files.createDirectories(Path.of(AppConfig.VAULT_DIR));
        
        // Initialize components
        cryptoManager = new CryptoManager();
        passwordManager = new PasswordManager(cryptoManager);
        auditManager = new AuditManager();
        fileManager = new FileManager(cryptoManager);
        metadataManager = new MetadataManager(cryptoManager);
        decoyManager = new DecoyManager(cryptoManager, fileManager, metadataManager);
        sessionManager = new BasicSessionManager();
        
        // Initialize passwords and derive master key
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
        masterKey = passwordManager.deriveKey(masterPassword);
    }
    
    @Test
    @DisplayName("Complete file upload workflow should work end-to-end")
    void testCompleteFileUploadWorkflow() throws Exception {
        // 1. Authenticate user
        PasswordManager.PasswordType authResult = passwordManager.validatePassword(masterPassword);
        assertEquals(PasswordManager.PasswordType.MASTER, authResult);
        
        // 2. Start session
        sessionManager.startSession();
        assertTrue(sessionManager.isSessionActive());
        
        // 3. Prepare test file
        String fileName = "test_document.txt";
        String fileContent = "This is a test document with sensitive information.";
        byte[] fileData = fileContent.getBytes();
        
        // 4. Upload file
        String fileId = fileManager.storeFile(fileName, fileData, masterKey);
        assertNotNull(fileId);
        assertFalse(fileId.isEmpty());
        
        // 5. Verify file is stored
        assertTrue(fileManager.fileExists(fileId));
        
        // 6. Add metadata
        String checksum = cryptoManager.calculateSHA256(fileData);
        metadataManager.addFileMetadata(fileName, "txt", fileData.length, checksum);
        
        // 7. Verify metadata is stored
        assertTrue(metadataManager.hasMetadata(fileName));
        
        // 8. Verify audit log entry
        List<String> auditLogs = auditManager.getAuditLogs();
        boolean hasUploadLog = auditLogs.stream()
            .anyMatch(log -> log.contains("FILE_UPLOAD") || log.contains("STORE"));
        assertTrue(hasUploadLog, "Audit log should contain file upload entry");
        
        // 9. End session
        sessionManager.endSession();
        assertFalse(sessionManager.isSessionActive());
    }
    
    @Test
    @DisplayName("Complete file download workflow should work end-to-end")
    void testCompleteFileDownloadWorkflow() throws Exception {
        // Setup: Upload a file first
        String fileName = "download_test.pdf";
        String fileContent = "PDF content for download test";
        byte[] originalData = fileContent.getBytes();
        
        String fileId = fileManager.storeFile(fileName, originalData, masterKey);
        String checksum = cryptoManager.calculateSHA256(originalData);
        metadataManager.addFileMetadata(fileName, "pdf", originalData.length, checksum);
        
        // 1. Authenticate user
        PasswordManager.PasswordType authResult = passwordManager.validatePassword(masterPassword);
        assertEquals(PasswordManager.PasswordType.MASTER, authResult);
        
        // 2. Start session
        sessionManager.startSession();
        
        // 3. List files
        List<String> fileList = fileManager.listFiles();
        assertTrue(fileList.contains(fileId), "File should be in the list");
        
        // 4. Get file metadata
        assertTrue(metadataManager.hasMetadata(fileName));
        
        // 5. Download file
        byte[] downloadedData = fileManager.retrieveFile(fileId, masterKey);
        assertNotNull(downloadedData);
        
        // 6. Verify integrity
        String downloadedChecksum = cryptoManager.calculateSHA256(downloadedData);
        assertEquals(checksum, downloadedChecksum, "File integrity should be maintained");
        
        // 7. Verify content
        assertArrayEquals(originalData, downloadedData, "Downloaded content should match original");
        
        // 8. Verify audit log
        List<String> auditLogs = auditManager.getAuditLogs();
        boolean hasDownloadLog = auditLogs.stream()
            .anyMatch(log -> log.contains("FILE_DOWNLOAD") || log.contains("RETRIEVE"));
        assertTrue(hasDownloadLog, "Audit log should contain file download entry");
        
        sessionManager.endSession();
    }
    
    @Test
    @DisplayName("Complete file deletion workflow should work end-to-end")
    void testCompleteFileDeletionWorkflow() throws Exception {
        // Setup: Upload a file first
        String fileName = "delete_test.doc";
        String fileContent = "Document to be deleted";
        byte[] fileData = fileContent.getBytes();
        
        String fileId = fileManager.storeFile(fileName, fileData, masterKey);
        metadataManager.addFileMetadata(fileName, "doc", fileData.length, "checksum");
        
        // Verify file exists
        assertTrue(fileManager.fileExists(fileId));
        assertTrue(metadataManager.hasMetadata(fileName));
        
        // 1. Authenticate user
        passwordManager.validatePassword(masterPassword);
        sessionManager.startSession();
        
        // 2. Delete file
        fileManager.deleteFile(fileId);
        
        // 3. Remove metadata
        metadataManager.removeFileMetadata(fileName);
        
        // 4. Verify file is deleted
        assertFalse(fileManager.fileExists(fileId), "File should not exist after deletion");
        assertFalse(metadataManager.hasMetadata(fileName), "Metadata should not exist after deletion");
        
        // 5. Verify secure deletion (file should not be recoverable)
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        Files.walk(vaultPath)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    // Check if any file contains the original content
                    byte[] content = Files.readAllBytes(file);
                    String contentStr = new String(content);
                    assertFalse(contentStr.contains(fileContent), 
                        "Deleted file content should not be recoverable");
                } catch (Exception e) {
                    // File might be inaccessible, which is acceptable
                }
            });
        
        // 6. Verify audit log
        List<String> auditLogs = auditManager.getAuditLogs();
        boolean hasDeleteLog = auditLogs.stream()
            .anyMatch(log -> log.contains("FILE_DELETE") || log.contains("DELETE"));
        assertTrue(hasDeleteLog, "Audit log should contain file deletion entry");
        
        sessionManager.endSession();
    }
    
    @Test
    @DisplayName("Complete panic mode workflow should work end-to-end")
    void testCompletePanicModeWorkflow() throws Exception {
        // Setup: Create vault with data
        setupVaultWithTestData();
        
        // Verify data exists
        assertTrue(Files.exists(Path.of(AppConfig.VAULT_DIR)));
        List<String> filesBefore = fileManager.listFiles();
        assertFalse(filesBefore.isEmpty(), "Vault should contain files before panic mode");
        
        // 1. Trigger panic mode with panic password
        PasswordManager.PasswordType authResult = passwordManager.validatePassword(panicPassword);
        assertEquals(PasswordManager.PasswordType.PANIC, authResult);
        
        // 2. Execute panic wipe
        PanicModeExecutor panicExecutor = new PanicModeExecutor(fileManager, metadataManager, auditManager);
        panicExecutor.executePanicWipe();
        
        // 3. Verify all data is destroyed
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        if (Files.exists(vaultPath)) {
            // Directory might exist but should be empty
            assertEquals(0, Files.list(vaultPath).count(), "Vault should be empty after panic wipe");
        }
        
        // 4. Verify no recoverable data
        assertNoRecoverableData(vaultPath);
        
        // 5. Verify audit logs are destroyed
        List<String> auditLogsAfter = auditManager.getAuditLogs();
        assertTrue(auditLogsAfter.isEmpty() || auditLogsAfter.size() < 5, 
            "Audit logs should be minimal or destroyed after panic mode");
    }
    
    @Test
    @DisplayName("Complete decoy mode workflow should work end-to-end")
    void testCompleteDecoyModeWorkflow() throws Exception {
        // Setup: Create real vault with data
        setupVaultWithTestData();
        List<String> realFiles = fileManager.listFiles();
        
        // 1. Trigger decoy mode with decoy password
        PasswordManager.PasswordType authResult = passwordManager.validatePassword(decoyPassword);
        assertEquals(PasswordManager.PasswordType.DECOY, authResult);
        
        // 2. Initialize decoy vault
        decoyManager.initializeDecoyVault(decoyPassword);
        
        // 3. Access decoy files
        List<String> decoyFiles = decoyManager.getDecoyFileList();
        assertFalse(decoyFiles.isEmpty(), "Decoy vault should contain files");
        
        // 4. Verify decoy files are different from real files
        for (String decoyFile : decoyFiles) {
            assertFalse(realFiles.contains(decoyFile), 
                "Decoy files should not overlap with real files");
        }
        
        // 5. Test decoy file operations
        String firstDecoyFile = decoyFiles.get(0);
        byte[] decoyContent = decoyManager.getDecoyFileContent(firstDecoyFile);
        assertNotNull(decoyContent);
        assertTrue(decoyContent.length > 0, "Decoy file should have content");
        
        // 6. Verify real vault remains intact
        List<String> realFilesAfter = fileManager.listFiles();
        assertEquals(realFiles.size(), realFilesAfter.size(), 
            "Real vault should remain unchanged during decoy mode");
        
        // 7. Verify decoy content is realistic
        String contentStr = new String(decoyContent);
        assertTrue(contentStr.length() > 50, "Decoy content should be substantial");
        assertFalse(contentStr.contains("DECOY") || contentStr.contains("FAKE"), 
            "Decoy content should not obviously indicate it's fake");
    }
    
    @Test
    @DisplayName("Session timeout workflow should work correctly")
    void testSessionTimeoutWorkflow() throws Exception {
        // 1. Set short timeout for testing
        sessionManager.setSessionTimeout(2); // 2 seconds
        
        // 2. Authenticate and start session
        passwordManager.validatePassword(masterPassword);
        sessionManager.startSession();
        assertTrue(sessionManager.isSessionActive());
        
        // 3. Perform operation while session is active
        String fileId = fileManager.storeFile("timeout_test.txt", "test content".getBytes(), masterKey);
        assertTrue(fileManager.fileExists(fileId));
        
        // 4. Wait for session timeout
        Thread.sleep(3000); // Wait 3 seconds
        
        // 5. Verify session is expired
        assertFalse(sessionManager.isSessionActive(), "Session should be expired");
        
        // 6. Attempt operation after timeout (should fail or require re-authentication)
        // This depends on implementation - session manager should prevent operations
        // or require re-authentication
    }
    
    @Test
    @DisplayName("Multiple user sessions should be handled correctly")
    void testMultipleUserSessions() throws Exception {
        int sessionCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(sessionCount);
        
        // Create multiple concurrent sessions
        for (int i = 0; i < sessionCount; i++) {
            final int sessionId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    
                    // Each thread simulates a user session
                    SessionManager localSession = new BasicSessionManager();
                    
                    // Authenticate
                    PasswordManager.PasswordType authResult = passwordManager.validatePassword(masterPassword);
                    assertEquals(PasswordManager.PasswordType.MASTER, authResult);
                    
                    // Start session
                    localSession.startSession();
                    assertTrue(localSession.isSessionActive());
                    
                    // Perform file operations
                    String fileName = "session_" + sessionId + "_file.txt";
                    String content = "Content from session " + sessionId;
                    String fileId = fileManager.storeFile(fileName, content.getBytes(), masterKey);
                    
                    // Verify operation
                    byte[] retrieved = fileManager.retrieveFile(fileId, masterKey);
                    assertArrayEquals(content.getBytes(), retrieved);
                    
                    // End session
                    localSession.endSession();
                    assertFalse(localSession.isSessionActive());
                    
                } catch (Exception e) {
                    fail("Session " + sessionId + " failed: " + e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            }).start();
        }
        
        // Start all sessions
        startLatch.countDown();
        
        // Wait for completion
        assertTrue(completionLatch.await(30, TimeUnit.SECONDS), 
            "All sessions should complete within timeout");
        
        // Verify all files were created
        List<String> allFiles = fileManager.listFiles();
        assertTrue(allFiles.size() >= sessionCount, 
            "All session files should be created");
    }
    
    @Test
    @DisplayName("Error recovery workflow should work correctly")
    void testErrorRecoveryWorkflow() throws Exception {
        // 1. Authenticate
        passwordManager.validatePassword(masterPassword);
        sessionManager.startSession();
        
        // 2. Attempt operation that might fail
        String fileName = "recovery_test.txt";
        String content = "Test content for recovery";
        
        // Simulate temporary failure and recovery
        boolean operationSucceeded = false;
        int maxAttempts = 3;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String fileId = fileManager.storeFile(fileName, content.getBytes(), masterKey);
                
                // Verify storage
                assertTrue(fileManager.fileExists(fileId));
                byte[] retrieved = fileManager.retrieveFile(fileId, masterKey);
                assertArrayEquals(content.getBytes(), retrieved);
                
                operationSucceeded = true;
                break;
                
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e; // Re-throw on final attempt
                }
                
                // Wait before retry
                Thread.sleep(100 * attempt);
            }
        }
        
        assertTrue(operationSucceeded, "Operation should succeed with retry");
        sessionManager.endSession();
    }
    
    /**
     * Helper method to set up vault with test data
     */
    private void setupVaultWithTestData() throws Exception {
        // Create multiple test files
        String[] testFiles = {
            "document1.txt:Important document content",
            "report.pdf:Quarterly report data",
            "image.jpg:Image binary data",
            "spreadsheet.xlsx:Financial data",
            "presentation.pptx:Presentation content"
        };
        
        for (String fileSpec : testFiles) {
            String[] parts = fileSpec.split(":");
            String fileName = parts[0];
            String content = parts[1];
            
            String fileId = fileManager.storeFile(fileName, content.getBytes(), masterKey);
            String checksum = cryptoManager.calculateSHA256(content.getBytes());
            metadataManager.addFileMetadata(fileName, getFileExtension(fileName), 
                content.getBytes().length, checksum);
        }
    }
    
    /**
     * Helper method to get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    /**
     * Helper method to verify no recoverable data remains
     */
    private void assertNoRecoverableData(Path vaultPath) throws Exception {
        if (Files.exists(vaultPath)) {
            Files.walk(vaultPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        byte[] content = Files.readAllBytes(file);
                        // File should be empty or contain only zeros
                        for (byte b : content) {
                            assertEquals(0, b, "File should contain only zeros after panic wipe: " + file);
                        }
                    } catch (Exception e) {
                        // File might be deleted during check, which is acceptable
                    }
                });
        }
    }
}