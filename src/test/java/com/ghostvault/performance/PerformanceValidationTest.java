package com.ghostvault.performance;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.PasswordManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance validation tests for large files and high file counts
 * Ensures the system performs adequately under load
 */
class PerformanceValidationTest {
    
    @TempDir
    Path tempDir;
    
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private SecretKey testKey;
    
    // Performance thresholds
    private static final long MAX_ENCRYPTION_TIME_MS_PER_MB = 1000; // 1 second per MB
    private static final long MAX_FILE_OPERATION_TIME_MS = 5000; // 5 seconds for file operations
    private static final int LARGE_FILE_SIZE_MB = 10; // 10 MB test file
    private static final int HIGH_FILE_COUNT = 1000; // 1000 files
    private static final int CONCURRENT_OPERATIONS = 10; // 10 concurrent operations
    
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
        
        // Generate test key
        testKey = CryptoManager.generateKey();
        
        // Initialize passwords
        passwordManager.initializePasswords("MasterPass123!@#", "PanicPass456$%^", "DecoyPass789&*()");
    }
    
    @Test
    @DisplayName("Large file encryption should complete within performance threshold")
    void testLargeFileEncryptionPerformance() throws Exception {
        // Create large test file (10 MB)
        byte[] largeFileData = generateTestData(LARGE_FILE_SIZE_MB * 1024 * 1024);
        
        long startTime = System.currentTimeMillis();
        
        // Encrypt large file
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(largeFileData, testKey);
        
        long encryptionTime = System.currentTimeMillis() - startTime;
        
        // Verify performance threshold
        long maxAllowedTime = LARGE_FILE_SIZE_MB * MAX_ENCRYPTION_TIME_MS_PER_MB;
        assertTrue(encryptionTime <= maxAllowedTime, 
            String.format("Encryption took %dms, expected <= %dms for %dMB file", 
                encryptionTime, maxAllowedTime, LARGE_FILE_SIZE_MB));
        
        // Verify encryption worked correctly
        assertNotNull(encrypted);
        assertNotNull(encrypted.getCiphertext());
        assertNotNull(encrypted.getIv());
        
        // Test decryption performance
        startTime = System.currentTimeMillis();
        byte[] decrypted = cryptoManager.decrypt(encrypted, testKey);
        long decryptionTime = System.currentTimeMillis() - startTime;
        
        assertTrue(decryptionTime <= maxAllowedTime, 
            String.format("Decryption took %dms, expected <= %dms for %dMB file", 
                decryptionTime, maxAllowedTime, LARGE_FILE_SIZE_MB));
        
        // Verify data integrity
        assertArrayEquals(largeFileData, decrypted, "Decrypted data should match original");
        
        System.out.printf("Large file (%dMB) - Encryption: %dms, Decryption: %dms%n", 
            LARGE_FILE_SIZE_MB, encryptionTime, decryptionTime);
    }
    
    @Test
    @DisplayName("Large file storage and retrieval should be performant")
    void testLargeFileStoragePerformance() throws Exception {
        String fileName = "large_test_file.bin";
        byte[] largeFileData = generateTestData(LARGE_FILE_SIZE_MB * 1024 * 1024);
        
        // Test file storage performance
        long startTime = System.currentTimeMillis();
        String fileId = fileManager.storeFile(fileName, largeFileData, testKey);
        long storageTime = System.currentTimeMillis() - startTime;
        
        assertTrue(storageTime <= MAX_FILE_OPERATION_TIME_MS * LARGE_FILE_SIZE_MB, 
            String.format("File storage took %dms, expected <= %dms", 
                storageTime, MAX_FILE_OPERATION_TIME_MS * LARGE_FILE_SIZE_MB));
        
        // Test file retrieval performance
        startTime = System.currentTimeMillis();
        byte[] retrievedData = fileManager.retrieveFile(fileId, testKey);
        long retrievalTime = System.currentTimeMillis() - startTime;
        
        assertTrue(retrievalTime <= MAX_FILE_OPERATION_TIME_MS * LARGE_FILE_SIZE_MB, 
            String.format("File retrieval took %dms, expected <= %dms", 
                retrievalTime, MAX_FILE_OPERATION_TIME_MS * LARGE_FILE_SIZE_MB));
        
        // Verify data integrity
        assertArrayEquals(largeFileData, retrievedData, "Retrieved data should match original");
        
        System.out.printf("Large file storage (%dMB) - Store: %dms, Retrieve: %dms%n", 
            LARGE_FILE_SIZE_MB, storageTime, retrievalTime);
    }
    
    @Test
    @DisplayName("High file count operations should be performant")
    void testHighFileCountPerformance() throws Exception {
        List<String> fileIds = new ArrayList<>();
        
        // Test storing many files
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < HIGH_FILE_COUNT; i++) {
            String fileName = "file_" + i + ".txt";
            String content = "Content for file " + i + " with some additional data to make it realistic";
            
            String fileId = fileManager.storeFile(fileName, content.getBytes(), testKey);
            fileIds.add(fileId);
            
            // Add metadata
            String checksum = cryptoManager.calculateSHA256(content.getBytes());
            metadataManager.addFileMetadata(fileName, "txt", content.getBytes().length, checksum);
        }
        
        long totalStorageTime = System.currentTimeMillis() - startTime;
        double avgStorageTimePerFile = (double) totalStorageTime / HIGH_FILE_COUNT;
        
        // Should average less than 50ms per file
        assertTrue(avgStorageTimePerFile <= 50, 
            String.format("Average storage time per file: %.2fms, expected <= 50ms", avgStorageTimePerFile));
        
        // Test listing files performance
        startTime = System.currentTimeMillis();
        List<String> allFiles = fileManager.listFiles();
        long listingTime = System.currentTimeMillis() - startTime;
        
        assertTrue(listingTime <= 1000, // Should list files in under 1 second
            String.format("File listing took %dms, expected <= 1000ms", listingTime));
        
        assertEquals(HIGH_FILE_COUNT, allFiles.size(), "Should list all stored files");
        
        // Test retrieving random files
        Random random = new Random();
        int sampleSize = Math.min(100, HIGH_FILE_COUNT / 10); // Test 10% of files or 100, whichever is smaller
        
        startTime = System.currentTimeMillis();
        for (int i = 0; i < sampleSize; i++) {
            int randomIndex = random.nextInt(fileIds.size());
            String randomFileId = fileIds.get(randomIndex);
            
            byte[] retrievedData = fileManager.retrieveFile(randomFileId, testKey);
            assertNotNull(retrievedData);
            assertTrue(retrievedData.length > 0);
        }
        long sampleRetrievalTime = System.currentTimeMillis() - startTime;
        double avgRetrievalTimePerFile = (double) sampleRetrievalTime / sampleSize;
        
        assertTrue(avgRetrievalTimePerFile <= 100, // Should average less than 100ms per file
            String.format("Average retrieval time per file: %.2fms, expected <= 100ms", avgRetrievalTimePerFile));
        
        System.out.printf("High file count (%d files) - Avg store: %.2fms, List: %dms, Avg retrieve: %.2fms%n", 
            HIGH_FILE_COUNT, avgStorageTimePerFile, listingTime, avgRetrievalTimePerFile);
    }
    
    @Test
    @DisplayName("Concurrent file operations should be performant and thread-safe")
    void testConcurrentOperationsPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_OPERATIONS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(CONCURRENT_OPERATIONS);
        
        List<Future<OperationResult>> futures = new ArrayList<>();
        
        // Submit concurrent operations
        for (int i = 0; i < CONCURRENT_OPERATIONS; i++) {
            final int operationId = i;
            
            Future<OperationResult> future = executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    long operationStart = System.currentTimeMillis();
                    
                    // Perform file operations
                    String fileName = "concurrent_file_" + operationId + ".txt";
                    String content = "Content for concurrent operation " + operationId + 
                                   " with additional data to simulate realistic file size";
                    
                    // Store file
                    String fileId = fileManager.storeFile(fileName, content.getBytes(), testKey);
                    
                    // Retrieve file
                    byte[] retrievedData = fileManager.retrieveFile(fileId, testKey);
                    
                    // Verify integrity
                    assertArrayEquals(content.getBytes(), retrievedData);
                    
                    // Add metadata
                    String checksum = cryptoManager.calculateSHA256(content.getBytes());
                    metadataManager.addFileMetadata(fileName, "txt", content.getBytes().length, checksum);
                    
                    long operationTime = System.currentTimeMillis() - operationStart;
                    
                    return new OperationResult(operationId, operationTime, true, null);
                    
                } catch (Exception e) {
                    return new OperationResult(operationId, 0, false, e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            });
            
            futures.add(future);
        }
        
        // Start all operations simultaneously
        long totalStartTime = System.currentTimeMillis();
        startLatch.countDown();
        
        // Wait for all operations to complete
        assertTrue(completionLatch.await(30, TimeUnit.SECONDS), 
            "All concurrent operations should complete within 30 seconds");
        
        long totalTime = System.currentTimeMillis() - totalStartTime;
        
        // Collect results
        List<OperationResult> results = new ArrayList<>();
        for (Future<OperationResult> future : futures) {
            results.add(future.get());
        }
        
        // Verify all operations succeeded
        long successCount = results.stream().mapToLong(r -> r.success ? 1 : 0).sum();
        assertEquals(CONCURRENT_OPERATIONS, successCount, "All concurrent operations should succeed");
        
        // Check performance
        double avgOperationTime = results.stream().mapToLong(r -> r.operationTime).average().orElse(0);
        assertTrue(avgOperationTime <= MAX_FILE_OPERATION_TIME_MS, 
            String.format("Average concurrent operation time: %.2fms, expected <= %dms", 
                avgOperationTime, MAX_FILE_OPERATION_TIME_MS));
        
        // Verify no data corruption occurred
        List<String> allFiles = fileManager.listFiles();
        assertTrue(allFiles.size() >= CONCURRENT_OPERATIONS, 
            "All concurrent files should be stored");
        
        executor.shutdown();
        
        System.out.printf("Concurrent operations (%d threads) - Total: %dms, Avg per operation: %.2fms%n", 
            CONCURRENT_OPERATIONS, totalTime, avgOperationTime);
    }
    
    @Test
    @DisplayName("Password validation should be performant under load")
    void testPasswordValidationPerformance() throws Exception {
        String masterPassword = "MasterPass123!@#";
        String wrongPassword = "WrongPassword456";
        
        int validationCount = 1000;
        
        // Test correct password validation performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < validationCount; i++) {
            PasswordManager.PasswordType result = passwordManager.validatePassword(masterPassword);
            assertEquals(PasswordManager.PasswordType.MASTER, result);
        }
        long correctPasswordTime = System.currentTimeMillis() - startTime;
        
        // Test incorrect password validation performance
        startTime = System.currentTimeMillis();
        for (int i = 0; i < validationCount; i++) {
            PasswordManager.PasswordType result = passwordManager.validatePassword(wrongPassword);
            assertEquals(PasswordManager.PasswordType.INVALID, result);
        }
        long incorrectPasswordTime = System.currentTimeMillis() - startTime;
        
        // Both should complete in reasonable time (less than 10 seconds for 1000 validations)
        assertTrue(correctPasswordTime <= 10000, 
            String.format("Correct password validation took %dms for %d attempts", 
                correctPasswordTime, validationCount));
        
        assertTrue(incorrectPasswordTime <= 10000, 
            String.format("Incorrect password validation took %dms for %d attempts", 
                incorrectPasswordTime, validationCount));
        
        // Timing should be similar (within 50% to prevent timing attacks)
        double timingDifference = Math.abs(correctPasswordTime - incorrectPasswordTime) / 
                                (double) Math.max(correctPasswordTime, incorrectPasswordTime);
        assertTrue(timingDifference <= 0.5, 
            String.format("Password validation timing difference too large: %.2f%%", timingDifference * 100));
        
        System.out.printf("Password validation (%d attempts) - Correct: %dms, Incorrect: %dms%n", 
            validationCount, correctPasswordTime, incorrectPasswordTime);
    }
    
    @Test
    @DisplayName("Memory usage should remain reasonable under load")
    void testMemoryUsageUnderLoad() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection and get baseline
        System.gc();
        Thread.sleep(100);
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform memory-intensive operations
        List<String> fileIds = new ArrayList<>();
        int fileCount = 100;
        int fileSizeKB = 100; // 100KB per file
        
        for (int i = 0; i < fileCount; i++) {
            String fileName = "memory_test_" + i + ".bin";
            byte[] fileData = generateTestData(fileSizeKB * 1024);
            
            String fileId = fileManager.storeFile(fileName, fileData, testKey);
            fileIds.add(fileId);
            
            // Periodically check memory usage
            if (i % 20 == 0) {
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryIncrease = currentMemory - baselineMemory;
                
                // Memory increase should be reasonable (less than 100MB for this test)
                assertTrue(memoryIncrease <= 100 * 1024 * 1024, 
                    String.format("Memory usage increased by %d bytes after %d files", memoryIncrease, i + 1));
            }
        }
        
        // Force garbage collection
        System.gc();
        Thread.sleep(100);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalMemoryIncrease = finalMemory - baselineMemory;
        
        // Final memory increase should be reasonable
        assertTrue(totalMemoryIncrease <= 200 * 1024 * 1024, // Less than 200MB
            String.format("Total memory increase: %d bytes for %d files of %dKB each", 
                totalMemoryIncrease, fileCount, fileSizeKB));
        
        System.out.printf("Memory usage - Baseline: %dMB, Final: %dMB, Increase: %dMB%n", 
            baselineMemory / (1024 * 1024), finalMemory / (1024 * 1024), 
            totalMemoryIncrease / (1024 * 1024));
    }
    
    @Test
    @DisplayName("Metadata operations should scale with file count")
    void testMetadataScalability() throws Exception {
        int[] fileCounts = {100, 500, 1000, 2000};
        
        for (int fileCount : fileCounts) {
            // Clear metadata for clean test
            metadataManager = new MetadataManager(cryptoManager);
            
            // Add metadata entries
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < fileCount; i++) {
                String fileName = "scale_test_" + i + ".txt";
                metadataManager.addFileMetadata(fileName, "txt", 1000, "checksum" + i);
            }
            long addTime = System.currentTimeMillis() - startTime;
            
            // Search metadata
            startTime = System.currentTimeMillis();
            for (int i = 0; i < Math.min(100, fileCount); i++) {
                String searchTerm = "scale_test_" + i;
                boolean found = metadataManager.hasMetadata(searchTerm + ".txt");
                assertTrue(found, "Should find metadata for " + searchTerm);
            }
            long searchTime = System.currentTimeMillis() - startTime;
            
            // Performance should scale reasonably
            double avgAddTime = (double) addTime / fileCount;
            assertTrue(avgAddTime <= 10, // Less than 10ms per metadata entry
                String.format("Average metadata add time for %d files: %.2fms", fileCount, avgAddTime));
            
            assertTrue(searchTime <= 1000, // Search should complete in under 1 second
                String.format("Metadata search time for %d files: %dms", fileCount, searchTime));
            
            System.out.printf("Metadata scalability (%d files) - Add avg: %.2fms, Search: %dms%n", 
                fileCount, avgAddTime, searchTime);
        }
    }
    
    /**
     * Generate test data of specified size
     */
    private byte[] generateTestData(int size) {
        byte[] data = new byte[size];
        Random random = new Random(42); // Use fixed seed for reproducible tests
        random.nextBytes(data);
        return data;
    }
    
    /**
     * Result of a concurrent operation
     */
    private static class OperationResult {
        final int operationId;
        final long operationTime;
        final boolean success;
        final String errorMessage;
        
        OperationResult(int operationId, long operationTime, boolean success, String errorMessage) {
            this.operationId = operationId;
            this.operationTime = operationTime;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }
}