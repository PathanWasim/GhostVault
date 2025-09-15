package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.decoy.DecoyManager;
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
 * Comprehensive security validation tests
 * Ensures security features work correctly and don't leak sensitive information
 */
class SecurityValidationTest {
    
    @TempDir
    Path tempDir;
    
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private PanicModeExecutor panicModeExecutor;
    private DecoyManager decoyManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private SessionManager sessionManager;
    
    private SecretKey testKey;
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
        panicModeExecutor = new PanicModeExecutor(fileManager, metadataManager, auditManager);
        sessionManager = new BasicSessionManager();
        
        // Generate test key
        testKey = CryptoManager.generateKey();
        
        // Initialize passwords
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
    }
    
    @Test
    @DisplayName("Password validation should correctly identify password types")
    void testPasswordValidation() {
        // Test master password
        PasswordManager.PasswordType masterType = passwordManager.validatePassword(masterPassword);
        assertEquals(PasswordManager.PasswordType.MASTER, masterType);
        
        // Test panic password
        PasswordManager.PasswordType panicType = passwordManager.validatePassword(panicPassword);
        assertEquals(PasswordManager.PasswordType.PANIC, panicType);
        
        // Test decoy password
        PasswordManager.PasswordType decoyType = passwordManager.validatePassword(decoyPassword);
        assertEquals(PasswordManager.PasswordType.DECOY, decoyType);
        
        // Test invalid password
        PasswordManager.PasswordType invalidType = passwordManager.validatePassword("WrongPassword");
        assertEquals(PasswordManager.PasswordType.INVALID, invalidType);
    }
    
    @Test
    @DisplayName("Password validation should not leak timing information")
    void testPasswordTimingAttackResistance() {
        int iterations = 100;
        long[] masterTimes = new long[iterations];
        long[] invalidTimes = new long[iterations];
        
        // Measure timing for valid password
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword(masterPassword);
            masterTimes[i] = System.nanoTime() - start;
        }
        
        // Measure timing for invalid password
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword("InvalidPassword123");
            invalidTimes[i] = System.nanoTime() - start;
        }
        
        // Calculate averages
        long masterAvg = java.util.Arrays.stream(masterTimes).sum() / iterations;
        long invalidAvg = java.util.Arrays.stream(invalidTimes).sum() / iterations;
        
        // Timing difference should be minimal (within 20% to account for JVM variations)
        double timingDifference = Math.abs(masterAvg - invalidAvg) / (double) Math.max(masterAvg, invalidAvg);
        assertTrue(timingDifference < 0.2, 
            String.format("Timing difference too large: %.2f%% (master: %dns, invalid: %dns)", 
                timingDifference * 100, masterAvg, invalidAvg));
    }
    
    @Test
    @DisplayName("Panic mode should completely destroy all sensitive data")
    void testPanicModeDataDestruction() throws Exception {
        // Create test files and metadata
        createTestVaultData();
        
        // Verify data exists before panic mode
        assertTrue(Files.exists(Path.of(AppConfig.VAULT_DIR)));
        assertTrue(Files.list(Path.of(AppConfig.VAULT_DIR)).findAny().isPresent());
        
        // Execute panic mode
        panicModeExecutor.executePanicWipe();
        
        // Verify all data is destroyed
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        if (Files.exists(vaultPath)) {
            // If directory still exists, it should be empty
            assertEquals(0, Files.list(vaultPath).count(), "Vault directory should be empty after panic wipe");
        }
        
        // Verify no recoverable data remains
        assertNoRecoverableData();
    }
    
    @Test
    @DisplayName("Panic mode should operate silently without user indication")
    void testPanicModeSilentOperation() throws Exception {
        createTestVaultData();
        
        // Capture any output during panic mode
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));
        
        try {
            panicModeExecutor.executePanicWipe();
            
            // Check that no user-visible output was produced
            String output = outputStream.toString();
            assertFalse(output.contains("panic"), "Panic mode should not mention 'panic' in output");
            assertFalse(output.contains("destroy"), "Panic mode should not mention 'destroy' in output");
            assertFalse(output.contains("wipe"), "Panic mode should not mention 'wipe' in output");
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("Decoy mode should provide realistic fake content")
    void testDecoyModeRealisticContent() throws Exception {
        // Initialize decoy mode
        decoyManager.initializeDecoyVault(decoyPassword);
        
        // Get decoy files
        List<String> decoyFiles = decoyManager.getDecoyFileList();
        
        // Verify decoy files exist and are realistic
        assertFalse(decoyFiles.isEmpty(), "Decoy vault should contain files");
        assertTrue(decoyFiles.size() >= 3, "Decoy vault should contain multiple files");
        
        // Check for realistic file names and types
        boolean hasDocuments = decoyFiles.stream().anyMatch(name -> 
            name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".txt"));
        assertTrue(hasDocuments, "Decoy vault should contain document files");
        
        // Verify decoy files have realistic content
        for (String fileName : decoyFiles.subList(0, Math.min(3, decoyFiles.size()))) {
            byte[] content = decoyManager.getDecoyFileContent(fileName);
            assertNotNull(content, "Decoy file should have content");
            assertTrue(content.length > 100, "Decoy file should have substantial content");
        }
    }
    
    @Test
    @DisplayName("Decoy mode should be completely separate from real data")
    void testDecoyModeDataSeparation() throws Exception {
        // Create real vault data
        createTestVaultData();
        
        // Initialize decoy mode
        decoyManager.initializeDecoyVault(decoyPassword);
        
        // Get real and decoy file lists
        List<String> realFiles = fileManager.listFiles();
        List<String> decoyFiles = decoyManager.getDecoyFileList();
        
        // Verify no overlap between real and decoy files
        for (String realFile : realFiles) {
            assertFalse(decoyFiles.contains(realFile), 
                "Decoy files should not contain real file: " + realFile);
        }
        
        for (String decoyFile : decoyFiles) {
            assertFalse(realFiles.contains(decoyFile), 
                "Real files should not contain decoy file: " + decoyFile);
        }
    }
    
    @Test
    @DisplayName("Cryptographic operations should use secure random values")
    void testCryptographicRandomness() throws Exception {
        int iterations = 100;
        byte[][] ivs = new byte[iterations][];
        
        // Generate multiple IVs
        for (int i = 0; i < iterations; i++) {
            ivs[i] = cryptoManager.generateSecureRandom(16);
        }
        
        // Verify all IVs are different (extremely high probability)
        for (int i = 0; i < iterations; i++) {
            for (int j = i + 1; j < iterations; j++) {
                assertFalse(java.util.Arrays.equals(ivs[i], ivs[j]), 
                    "Generated IVs should be unique");
            }
        }
        
        // Verify IVs have good entropy (basic check)
        for (byte[] iv : ivs) {
            int zeroCount = 0;
            for (byte b : iv) {
                if (b == 0) zeroCount++;
            }
            // Should not have too many zeros (indicates poor randomness)
            assertTrue(zeroCount < iv.length / 2, "IV should have good entropy");
        }
    }
    
    @Test
    @DisplayName("Encryption should produce different ciphertext for same plaintext")
    void testEncryptionNonDeterministic() throws Exception {
        String plaintext = "This is a test message for encryption";
        byte[] plaintextBytes = plaintext.getBytes();
        
        // Encrypt the same plaintext multiple times
        CryptoManager.EncryptedData[] encrypted = new CryptoManager.EncryptedData[10];
        for (int i = 0; i < encrypted.length; i++) {
            encrypted[i] = cryptoManager.encrypt(plaintextBytes, testKey);
        }
        
        // Verify all ciphertexts are different
        for (int i = 0; i < encrypted.length; i++) {
            for (int j = i + 1; j < encrypted.length; j++) {
                assertFalse(java.util.Arrays.equals(encrypted[i].getCiphertext(), encrypted[j].getCiphertext()),
                    "Ciphertext should be different for each encryption");
                assertFalse(java.util.Arrays.equals(encrypted[i].getIv(), encrypted[j].getIv()),
                    "IV should be different for each encryption");
            }
        }
        
        // Verify all decrypt to the same plaintext
        for (CryptoManager.EncryptedData encData : encrypted) {
            byte[] decrypted = cryptoManager.decrypt(encData, testKey);
            assertArrayEquals(plaintextBytes, decrypted, "Decryption should produce original plaintext");
        }
    }
    
    @Test
    @DisplayName("Session management should enforce timeouts")
    void testSessionTimeoutEnforcement() throws Exception {
        // Set short timeout for testing
        sessionManager.setSessionTimeout(1); // 1 second
        
        // Start session
        sessionManager.startSession();
        assertTrue(sessionManager.isSessionActive(), "Session should be active initially");
        
        // Wait for timeout
        Thread.sleep(1500); // Wait 1.5 seconds
        
        // Session should be expired
        assertFalse(sessionManager.isSessionActive(), "Session should be expired after timeout");
    }
    
    @Test
    @DisplayName("Session management should track failed login attempts")
    void testFailedLoginTracking() throws Exception {
        String testUser = "testuser";
        
        // Simulate failed login attempts
        for (int i = 0; i < 5; i++) {
            sessionManager.recordFailedLogin(testUser);
        }
        
        // Check if account is locked
        assertTrue(sessionManager.isAccountLocked(testUser), 
            "Account should be locked after multiple failed attempts");
        
        // Verify lockout duration
        assertTrue(sessionManager.getLockoutTimeRemaining(testUser) > 0,
            "Lockout should have remaining time");
    }
    
    @Test
    @DisplayName("Memory should be securely wiped after use")
    void testMemorySecureWiping() {
        byte[] sensitiveData = "SensitivePassword123!@#".getBytes();
        byte[] originalData = sensitiveData.clone();
        
        // Verify data is initially correct
        assertArrayEquals(originalData, sensitiveData);
        
        // Wipe memory
        cryptoManager.secureWipe(sensitiveData);
        
        // Verify data is wiped (should be all zeros or random)
        assertFalse(java.util.Arrays.equals(originalData, sensitiveData),
            "Sensitive data should be wiped from memory");
        
        // Verify it's actually wiped (not just different)
        boolean allZeros = true;
        for (byte b : sensitiveData) {
            if (b != 0) {
                allZeros = false;
                break;
            }
        }
        assertTrue(allZeros, "Wiped memory should be all zeros");
    }
    
    @Test
    @DisplayName("File operations should be atomic")
    void testAtomicFileOperations() throws Exception {
        String testFileName = "atomic_test.txt";
        String testContent = "This is test content for atomic operations";
        
        // Create test file
        String fileId = fileManager.storeFile(testFileName, testContent.getBytes(), testKey);
        
        // Verify file exists and has correct content
        assertTrue(fileManager.fileExists(fileId), "File should exist after storage");
        
        byte[] retrievedContent = fileManager.retrieveFile(fileId, testKey);
        assertArrayEquals(testContent.getBytes(), retrievedContent, "Retrieved content should match original");
        
        // Test atomic deletion
        fileManager.deleteFile(fileId);
        assertFalse(fileManager.fileExists(fileId), "File should not exist after deletion");
    }
    
    @Test
    @DisplayName("Audit logs should not contain sensitive information")
    void testAuditLogSecurity() throws Exception {
        // Perform operations that generate audit logs
        passwordManager.validatePassword(masterPassword);
        fileManager.storeFile("test.txt", "sensitive content".getBytes(), testKey);
        
        // Get audit logs
        List<String> auditLogs = auditManager.getAuditLogs();
        
        // Verify logs don't contain sensitive data
        for (String logEntry : auditLogs) {
            assertFalse(logEntry.contains(masterPassword), "Audit log should not contain passwords");
            assertFalse(logEntry.contains("sensitive content"), "Audit log should not contain file content");
            assertFalse(logEntry.contains(testKey.toString()), "Audit log should not contain encryption keys");
        }
        
        // Verify logs contain expected security events
        boolean hasPasswordValidation = auditLogs.stream()
            .anyMatch(log -> log.contains("PASSWORD_VALIDATION") || log.contains("LOGIN"));
        assertTrue(hasPasswordValidation, "Audit logs should contain password validation events");
    }
    
    @Test
    @DisplayName("Concurrent operations should be thread-safe")
    void testConcurrentOperationsSafety() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        
        // Create threads that perform concurrent operations
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Perform various operations
                        String fileName = "thread_" + threadId + "_file_" + j + ".txt";
                        String content = "Content from thread " + threadId + " operation " + j;
                        
                        String fileId = fileManager.storeFile(fileName, content.getBytes(), testKey);
                        byte[] retrieved = fileManager.retrieveFile(fileId, testKey);
                        assertArrayEquals(content.getBytes(), retrieved);
                        
                        // Validate password
                        PasswordManager.PasswordType type = passwordManager.validatePassword(masterPassword);
                        assertEquals(PasswordManager.PasswordType.MASTER, type);
                    }
                    
                } catch (Exception e) {
                    fail("Concurrent operation failed: " + e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            }).start();
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        assertTrue(completionLatch.await(30, TimeUnit.SECONDS), 
            "All concurrent operations should complete within timeout");
    }
    
    @Test
    @DisplayName("System should resist side-channel attacks")
    void testSideChannelAttackResistance() throws Exception {
        // Test constant-time operations
        String correctPassword = masterPassword;
        String incorrectPassword = "WrongPassword123";
        
        int iterations = 1000;
        long[] correctTimes = new long[iterations];
        long[] incorrectTimes = new long[iterations];
        
        // Warm up JVM
        for (int i = 0; i < 100; i++) {
            passwordManager.validatePassword(correctPassword);
            passwordManager.validatePassword(incorrectPassword);
        }
        
        // Measure timing for correct password
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword(correctPassword);
            correctTimes[i] = System.nanoTime() - start;
        }
        
        // Measure timing for incorrect password
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword(incorrectPassword);
            incorrectTimes[i] = System.nanoTime() - start;
        }
        
        // Statistical analysis
        double correctAvg = java.util.Arrays.stream(correctTimes).average().orElse(0);
        double incorrectAvg = java.util.Arrays.stream(incorrectTimes).average().orElse(0);
        
        // Timing difference should be minimal
        double timingDifference = Math.abs(correctAvg - incorrectAvg) / Math.max(correctAvg, incorrectAvg);
        assertTrue(timingDifference < 0.1, 
            String.format("Timing difference suggests side-channel vulnerability: %.2f%%", timingDifference * 100));
    }
    
    /**
     * Helper method to create test vault data
     */
    private void createTestVaultData() throws Exception {
        // Create test files
        fileManager.storeFile("document1.txt", "Test document content 1".getBytes(), testKey);
        fileManager.storeFile("document2.pdf", "Test PDF content".getBytes(), testKey);
        fileManager.storeFile("image.jpg", "Test image data".getBytes(), testKey);
        
        // Create metadata
        metadataManager.addFileMetadata("document1.txt", "txt", 100, "checksum1");
        metadataManager.addFileMetadata("document2.pdf", "pdf", 200, "checksum2");
        metadataManager.addFileMetadata("image.jpg", "jpg", 300, "checksum3");
    }
    
    /**
     * Helper method to verify no recoverable data remains
     */
    private void assertNoRecoverableData() throws Exception {
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        
        if (Files.exists(vaultPath)) {
            // Check if any files remain
            Files.walk(vaultPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // If files exist, they should be empty or contain only zeros
                        byte[] content = Files.readAllBytes(file);
                        for (byte b : content) {
                            assertEquals(0, b, "Remaining file should contain only zeros: " + file);
                        }
                    } catch (Exception e) {
                        // File might be deleted during check, which is acceptable
                    }
                });
        }
    }
}