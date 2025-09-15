package com.ghostvault.performance;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.core.VaultInitializer;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.PasswordManager;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance testing framework for GhostVault
 * Tests performance of critical operations under various conditions
 */
public class PerformanceTestFramework {
    
    private static final List<PerformanceResult> performanceResults = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       GhostVault Performance Test Framework");
        System.out.println("==================================================");
        
        try {
            runAllPerformanceTests();
            generatePerformanceReport();
            
        } catch (Exception e) {
            System.err.println("❌ Performance testing failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Run all performance tests
     */
    private static void runAllPerformanceTests() {
        System.out.println("\n⚡ Running Performance Tests...");
        System.out.println("--------------------------------------------------");
        
        // Encryption Performance Tests
        runPerformanceTest("Small File Encryption (1KB)", () -> testEncryptionPerformance(1024));
        runPerformanceTest("Medium File Encryption (1MB)", () -> testEncryptionPerformance(1024 * 1024));
        runPerformanceTest("Large File Encryption (10MB)", () -> testEncryptionPerformance(10 * 1024 * 1024));
        
        // Key Derivation Performance Tests
        runPerformanceTest("PBKDF2 Key Derivation", PerformanceTestFramework::testKeyDerivationPerformance);
        runPerformanceTest("Password Validation", PerformanceTestFramework::testPasswordValidationPerformance);
        
        // File Operations Performance Tests
        runPerformanceTest("File Storage Performance", PerformanceTestFramework::testFileStoragePerformance);
        runPerformanceTest("File Retrieval Performance", PerformanceTestFramework::testFileRetrievalPerformance);
        runPerformanceTest("Metadata Operations", PerformanceTestFramework::testMetadataPerformance);
        
        // Bulk Operations Performance Tests
        runPerformanceTest("Bulk File Upload (10 files)", () -> testBulkFileOperations(10));
        runPerformanceTest("Bulk File Upload (50 files)", () -> testBulkFileOperations(50));
        
        // Memory Performance Tests
        runPerformanceTest("Memory Usage Under Load", PerformanceTestFramework::testMemoryUsageUnderLoad);
        runPerformanceTest("Garbage Collection Impact", PerformanceTestFramework::testGarbageCollectionImpact);
        
        // Concurrent Operations Tests
        runPerformanceTest("Concurrent Encryption", PerformanceTestFramework::testConcurrentEncryption);
        
        // Startup Performance Tests
        runPerformanceTest("Vault Initialization Time", PerformanceTestFramework::testVaultInitializationTime);
        runPerformanceTest("Application Startup Time", PerformanceTestFramework::testApplicationStartupTime);
    }
    
    /**
     * Test encryption performance for different file sizes
     */
    private static PerformanceMetrics testEncryptionPerformance(int dataSize) throws Exception {
        CryptoManager cryptoManager = new CryptoManager();
        SecretKey key = cryptoManager.deriveKey("TestPassword123!", "testsalt".getBytes());
        
        byte[] testData = new byte[dataSize];
        java.util.Arrays.fill(testData, (byte) 0x42);
        
        int iterations = Math.max(1, 100 * 1024 * 1024 / dataSize); // Scale iterations based on data size
        
        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();
        
        for (int i = 0; i < iterations; i++) {
            CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData, key);
            byte[] decrypted = cryptoManager.decrypt(encrypted, key);
            
            if (i == 0) {
                assert java.util.Arrays.equals(testData, decrypted) : "Decryption should produce original data";
            }
        }
        
        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();
        
        long totalTime = endTime - startTime;
        long totalBytes = (long) dataSize * iterations * 2; // Encrypt + decrypt
        double throughputMBps = (totalBytes / 1024.0 / 1024.0) / (totalTime / 1000.0);
        
        return new PerformanceMetrics(
            totalTime,
            throughputMBps,
            endMemory - startMemory,
            iterations,
            String.format("%.2f MB/s", throughputMBps)
        );
    }
    
    /**
     * Test key derivation performance
     */
    private static PerformanceMetrics testKeyDerivationPerformance() throws Exception {
        CryptoManager cryptoManager = new CryptoManager();
        
        int iterations = 10;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            cryptoManager.deriveKey("TestPassword" + i, ("salt" + i).getBytes());
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgTimePerDerivation = totalTime / (double) iterations;
        
        return new PerformanceMetrics(
            totalTime,
            1000.0 / avgTimePerDerivation, // Derivations per second
            0,
            iterations,
            String.format("%.1f ms/derivation", avgTimePerDerivation)
        );
    }
    
    /**
     * Test password validation performance
     */
    private static PerformanceMetrics testPasswordValidationPerformance() throws Exception {
        // Clean up and initialize vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        VaultInitializer.initializeVault("MasterPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        String[] testPasswords = {
            "MasterPassword123!",
            "PanicPassword456@",
            "DecoyPassword789#",
            "WrongPassword123!"
        };
        
        int iterations = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            for (String password : testPasswords) {
                passwordManager.validatePassword(password);
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        int totalValidations = iterations * testPasswords.length;
        double avgTimePerValidation = totalTime / (double) totalValidations;
        
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            1000.0 / avgTimePerValidation, // Validations per second
            0,
            totalValidations,
            String.format("%.2f ms/validation", avgTimePerValidation)
        );
    }
    
    /**
     * Test file storage performance
     */
    private static PerformanceMetrics testFileStoragePerformance() throws Exception {
        // Clean up and initialize vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        VaultInitializer.initializeVault("TestPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        FileManager fileManager = new FileManager(AppConfig.VAULT_DIR);
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        fileManager.setEncryptionKey(passwordManager.deriveVaultKey("TestPassword123!"));
        
        // Create test files
        int fileCount = 20;
        File[] testFiles = new File[fileCount];
        long totalFileSize = 0;
        
        for (int i = 0; i < fileCount; i++) {
            testFiles[i] = new File("perf-test-" + i + ".txt");
            String content = "Performance test content for file " + i + " ".repeat(100);
            Files.write(testFiles[i].toPath(), content.getBytes());
            totalFileSize += testFiles[i].length();
        }
        
        long startTime = System.currentTimeMillis();
        
        for (File testFile : testFiles) {
            fileManager.storeFile(testFile);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double throughputMBps = (totalFileSize / 1024.0 / 1024.0) / (totalTime / 1000.0);
        
        // Clean up
        for (File file : testFiles) {
            file.delete();
        }
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            throughputMBps,
            0,
            fileCount,
            String.format("%.2f MB/s", throughputMBps)
        );
    }
    
    /**
     * Test file retrieval performance
     */
    private static PerformanceMetrics testFileRetrievalPerformance() throws Exception {
        // Clean up and initialize vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        VaultInitializer.initializeVault("TestPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        FileManager fileManager = new FileManager(AppConfig.VAULT_DIR);
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        fileManager.setEncryptionKey(passwordManager.deriveVaultKey("TestPassword123!"));
        
        // Create and store test files
        int fileCount = 20;
        File[] testFiles = new File[fileCount];
        com.ghostvault.model.VaultFile[] vaultFiles = new com.ghostvault.model.VaultFile[fileCount];
        long totalFileSize = 0;
        
        for (int i = 0; i < fileCount; i++) {
            testFiles[i] = new File("perf-test-" + i + ".txt");
            String content = "Performance test content for file " + i + " ".repeat(100);
            Files.write(testFiles[i].toPath(), content.getBytes());
            vaultFiles[i] = fileManager.storeFile(testFiles[i]);
            totalFileSize += testFiles[i].length();
        }
        
        long startTime = System.currentTimeMillis();
        
        // Retrieve all files
        for (com.ghostvault.model.VaultFile vaultFile : vaultFiles) {
            byte[] data = fileManager.retrieveFile(vaultFile);
            assert data != null : "Retrieved data should not be null";
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double throughputMBps = (totalFileSize / 1024.0 / 1024.0) / (totalTime / 1000.0);
        
        // Clean up
        for (File file : testFiles) {
            file.delete();
        }
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            throughputMBps,
            0,
            fileCount,
            String.format("%.2f MB/s", throughputMBps)
        );
    }
    
    /**
     * Test metadata operations performance
     */
    private static PerformanceMetrics testMetadataPerformance() throws Exception {
        // Clean up and initialize vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        VaultInitializer.initializeVault("TestPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        metadataManager.setEncryptionKey(passwordManager.deriveVaultKey("TestPassword123!"));
        
        int operations = 1000;
        long startTime = System.currentTimeMillis();
        
        // Perform metadata operations
        for (int i = 0; i < operations; i++) {
            metadataManager.getAllFiles();
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double operationsPerSecond = operations / (totalTime / 1000.0);
        
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            operationsPerSecond,
            0,
            operations,
            String.format("%.0f ops/sec", operationsPerSecond)
        );
    }
    
    /**
     * Test bulk file operations performance
     */
    private static PerformanceMetrics testBulkFileOperations(int fileCount) throws Exception {
        // Clean up and initialize vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        VaultInitializer.initializeVault("TestPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        FileManager fileManager = new FileManager(AppConfig.VAULT_DIR);
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        fileManager.setEncryptionKey(passwordManager.deriveVaultKey("TestPassword123!"));
        
        // Create test files
        File[] testFiles = new File[fileCount];
        long totalFileSize = 0;
        
        for (int i = 0; i < fileCount; i++) {
            testFiles[i] = new File("bulk-test-" + i + ".txt");
            String content = "Bulk test content for file " + i + " ".repeat(50);
            Files.write(testFiles[i].toPath(), content.getBytes());
            totalFileSize += testFiles[i].length();
        }
        
        long startTime = System.currentTimeMillis();
        
        // Store all files
        for (File testFile : testFiles) {
            fileManager.storeFile(testFile);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double throughputMBps = (totalFileSize / 1024.0 / 1024.0) / (totalTime / 1000.0);
        double filesPerSecond = fileCount / (totalTime / 1000.0);
        
        // Clean up
        for (File file : testFiles) {
            file.delete();
        }
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            throughputMBps,
            0,
            fileCount,
            String.format("%.1f files/sec", filesPerSecond)
        );
    }
    
    /**
     * Test memory usage under load
     */
    private static PerformanceMetrics testMemoryUsageUnderLoad() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        
        long memoryBefore = getUsedMemory();
        
        // Simulate heavy load
        CryptoManager cryptoManager = new CryptoManager();
        SecretKey key = cryptoManager.deriveKey("TestPassword123!", "testsalt".getBytes());
        
        byte[][] dataArrays = new byte[100][];
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            byte[] data = new byte[1024 * 1024]; // 1MB each
            java.util.Arrays.fill(data, (byte) i);
            
            CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(data, key);
            dataArrays[i] = cryptoManager.decrypt(encrypted, key);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        long memoryAfter = getUsedMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // Clean up references
        for (int i = 0; i < dataArrays.length; i++) {
            dataArrays[i] = null;
        }
        System.gc();
        
        return new PerformanceMetrics(
            totalTime,
            100.0 / (totalTime / 1000.0), // Operations per second
            memoryUsed,
            100,
            String.format("%.1f MB peak", memoryUsed / 1024.0 / 1024.0)
        );
    }
    
    /**
     * Test garbage collection impact
     */
    private static PerformanceMetrics testGarbageCollectionImpact() throws Exception {
        long startTime = System.currentTimeMillis();
        long memoryBefore = getUsedMemory();
        
        // Create objects that will need garbage collection
        List<byte[]> objects = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            objects.add(new byte[10240]); // 10KB each
        }
        
        // Force garbage collection
        objects.clear();
        System.gc();
        Thread.sleep(100); // Allow GC to complete
        
        long totalTime = System.currentTimeMillis() - startTime;
        long memoryAfter = getUsedMemory();
        
        return new PerformanceMetrics(
            totalTime,
            0,
            memoryAfter - memoryBefore,
            1000,
            String.format("GC completed in %dms", totalTime)
        );
    }
    
    /**
     * Test concurrent encryption performance
     */
    private static PerformanceMetrics testConcurrentEncryption() throws Exception {
        CryptoManager cryptoManager = new CryptoManager();
        SecretKey key = cryptoManager.deriveKey("TestPassword123!", "testsalt".getBytes());
        
        byte[] testData = new byte[1024 * 1024]; // 1MB
        java.util.Arrays.fill(testData, (byte) 0x42);
        
        int threadCount = 4;
        int operationsPerThread = 10;
        
        long startTime = System.currentTimeMillis();
        
        Thread[] threads = new Thread[threadCount];
        final Exception[] exceptions = new Exception[threadCount];
        
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData, key);
                        byte[] decrypted = cryptoManager.decrypt(encrypted, key);
                        
                        if (!java.util.Arrays.equals(testData, decrypted)) {
                            throw new RuntimeException("Decryption failed in thread " + threadIndex);
                        }
                    }
                } catch (Exception e) {
                    exceptions[threadIndex] = e;
                }
            });
            threads[t].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Check for exceptions
        for (Exception e : exceptions) {
            if (e != null) {
                throw e;
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        int totalOperations = threadCount * operationsPerThread;
        double operationsPerSecond = totalOperations / (totalTime / 1000.0);
        
        return new PerformanceMetrics(
            totalTime,
            operationsPerSecond,
            0,
            totalOperations,
            String.format("%.1f ops/sec (%d threads)", operationsPerSecond, threadCount)
        );
    }
    
    /**
     * Test vault initialization time
     */
    private static PerformanceMetrics testVaultInitializationTime() throws Exception {
        // Clean up any existing vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        long startTime = System.currentTimeMillis();
        
        VaultInitializer.initializeVault("TestPassword123!", "PanicPassword456@", "DecoyPassword789#");
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        VaultInitializer.resetVault();
        
        return new PerformanceMetrics(
            totalTime,
            1000.0 / totalTime, // Initializations per second
            0,
            1,
            String.format("Initialized in %dms", totalTime)
        );
    }
    
    /**
     * Test application startup time
     */
    private static PerformanceMetrics testApplicationStartupTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Simulate application startup components
        new CryptoManager();
        new PasswordManager("test-vault");
        
        // Simulate UI component initialization time
        Thread.sleep(50); // Simulate JavaFX initialization
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        return new PerformanceMetrics(
            totalTime,
            1000.0 / totalTime, // Startups per second
            0,
            1,
            String.format("Startup completed in %dms", totalTime)
        );
    }
    
    /**
     * Run a single performance test
     */
    private static void runPerformanceTest(String testName, PerformanceTestRunnable test) {
        long startTime = System.currentTimeMillis();
        
        try {
            PerformanceMetrics metrics = test.run();
            
            performanceResults.add(new PerformanceResult(testName, true, null, metrics));
            
            System.out.printf("✅ %-40s [%4dms] - %s%n", 
                testName, metrics.duration, metrics.description);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            performanceResults.add(new PerformanceResult(testName, false, e.getMessage(), 
                new PerformanceMetrics(duration, 0, 0, 0, "FAILED")));
            
            System.out.printf("❌ %-40s [%4dms] - %s%n", testName, duration, e.getMessage());
        }
    }
    
    /**
     * Generate performance report
     */
    private static void generatePerformanceReport() {
        System.out.println("\n==================================================");
        System.out.println("            PERFORMANCE TEST REPORT");
        System.out.println("==================================================");
        
        int totalTests = performanceResults.size();
        int passedTests = (int) performanceResults.stream().filter(r -> r.passed).count();
        int failedTests = totalTests - passedTests;
        
        System.out.printf("Total Performance Tests: %d%n", totalTests);
        System.out.printf("Passed:                  %d (%.1f%%)%n", passedTests, (passedTests * 100.0 / totalTests));
        System.out.printf("Failed:                  %d (%.1f%%)%n", failedTests, (failedTests * 100.0 / totalTests));
        
        if (passedTests > 0) {
            System.out.println("\n📊 PERFORMANCE METRICS:");
            System.out.println("--------------------------------------------------");
            
            for (PerformanceResult result : performanceResults) {
                if (result.passed) {
                    System.out.printf("%-40s: %s%n", result.testName, result.metrics.description);
                }
            }
            
            // Calculate averages
            double avgDuration = performanceResults.stream()
                .filter(r -> r.passed)
                .mapToLong(r -> r.metrics.duration)
                .average().orElse(0);
            
            System.out.printf("\nAverage test duration: %.1f ms%n", avgDuration);
        }
        
        if (failedTests > 0) {
            System.out.println("\n❌ PERFORMANCE ISSUES:");
            System.out.println("--------------------------------------------------");
            
            for (PerformanceResult result : performanceResults) {
                if (!result.passed) {
                    System.out.printf("⚠️  %s: %s%n", result.testName, result.errorMessage);
                }
            }
        }
        
        System.out.println("\n==================================================");
        
        if (failedTests == 0) {
            System.out.println("🚀 ALL PERFORMANCE TESTS PASSED!");
            System.out.println("   GhostVault meets performance requirements.");
        } else {
            System.out.println("⚠️  Some performance tests failed.");
            System.out.println("   Review and optimize before deployment.");
        }
    }
    
    /**
     * Get current memory usage
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * Functional interface for performance tests
     */
    @FunctionalInterface
    private interface PerformanceTestRunnable {
        PerformanceMetrics run() throws Exception;
    }
    
    /**
     * Performance metrics data class
     */
    private static class PerformanceMetrics {
        final long duration;
        final double throughput;
        final long memoryUsed;
        final int operations;
        final String description;
        
        PerformanceMetrics(long duration, double throughput, long memoryUsed, int operations, String description) {
            this.duration = duration;
            this.throughput = throughput;
            this.memoryUsed = memoryUsed;
            this.operations = operations;
            this.description = description;
        }
    }
    
    /**
     * Performance test result data class
     */
    private static class PerformanceResult {
        final String testName;
        final boolean passed;
        final String errorMessage;
        final PerformanceMetrics metrics;
        
        PerformanceResult(String testName, boolean passed, String errorMessage, PerformanceMetrics metrics) {
            this.testName = testName;
            this.passed = passed;
            this.errorMessage = errorMessage;
            this.metrics = metrics;
        }
    }
}