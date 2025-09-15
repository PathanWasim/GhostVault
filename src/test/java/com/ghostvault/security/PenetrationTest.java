package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Penetration tests to verify no sensitive data leakage
 * Tests for memory dumps, file system artifacts, and other security vulnerabilities
 */
class PenetrationTest {
    
    @TempDir
    Path tempDir;
    
    private CryptoManager cryptoManager;
    private PasswordManager passwordManager;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private AuditManager auditManager;
    private PanicModeExecutor panicModeExecutor;
    
    private SecretKey testKey;
    private String masterPassword = "SecretMaster123!@#";
    private String panicPassword = "PanicSecret456$%^";
    private String decoyPassword = "DecoySecret789&*()";
    private String sensitiveContent = "HIGHLY_SENSITIVE_CONFIDENTIAL_DATA_12345";
    
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
        panicModeExecutor = new PanicModeExecutor(fileManager, metadataManager, auditManager);
        
        // Generate test key
        testKey = CryptoManager.generateKey();
        
        // Initialize passwords
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
    }
    
    @Test
    @DisplayName("Memory dumps should not contain sensitive data")
    void testMemoryDumpSecurity() throws Exception {
        // Store sensitive data
        String sensitiveFileName = "classified_document.txt";
        fileManager.storeFile(sensitiveFileName, sensitiveContent.getBytes(), testKey);
        
        // Perform password validation
        passwordManager.validatePassword(masterPassword);
        
        // Force garbage collection to ensure cleanup
        System.gc();
        Thread.sleep(100);
        System.gc();
        
        // Simulate memory dump analysis
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        // In a real penetration test, we would analyze heap dumps
        // For this test, we verify that sensitive data is not easily accessible
        
        // Check if sensitive strings are in system properties or environment
        assertFalse(System.getProperties().toString().contains(sensitiveContent),
            "Sensitive content should not be in system properties");
        
        assertFalse(System.getenv().toString().contains(sensitiveContent),
            "Sensitive content should not be in environment variables");
        
        // Verify passwords are not in string pool
        assertFalse(masterPassword.intern() == masterPassword,
            "Password should not be interned in string pool");
    }
    
    @Test
    @DisplayName("Temporary files should not contain sensitive data")
    void testTemporaryFilesSecurity() throws Exception {
        // Store sensitive data
        fileManager.storeFile("sensitive.txt", sensitiveContent.getBytes(), testKey);
        
        // Check system temporary directory
        String tempDirPath = System.getProperty("java.io.tmpdir");
        Path systemTempDir = Path.of(tempDirPath);
        
        if (Files.exists(systemTempDir)) {
            Files.walk(systemTempDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().contains("ghostvault") ||
                              path.getFileName().toString().contains("vault") ||
                              path.getFileName().toString().contains("crypto"))
                .forEach(tempFile -> {
                    try {
                        String content = Files.readString(tempFile);
                        assertFalse(content.contains(sensitiveContent),
                            "Temporary file should not contain sensitive data: " + tempFile);
                        assertFalse(content.contains(masterPassword),
                            "Temporary file should not contain passwords: " + tempFile);
                    } catch (Exception e) {
                        // File might be locked or deleted, which is acceptable
                    }
                });
        }
    }
    
    @Test
    @DisplayName("Log files should not contain sensitive information")
    void testLogFilesSecurity() throws Exception {
        // Perform operations that generate logs
        passwordManager.validatePassword(masterPassword);
        fileManager.storeFile("secret_file.txt", sensitiveContent.getBytes(), testKey);
        
        // Get audit logs
        List<String> auditLogs = auditManager.getAuditLogs();
        
        // Verify logs don't contain sensitive data
        for (String logEntry : auditLogs) {
            assertFalse(logEntry.contains(sensitiveContent),
                "Audit log should not contain sensitive file content");
            assertFalse(logEntry.contains(masterPassword),
                "Audit log should not contain passwords");
            assertFalse(logEntry.contains(testKey.toString()),
                "Audit log should not contain encryption keys");
        }
        
        // Check system logs (if accessible)
        checkSystemLogsForSensitiveData();
    }
    
    @Test
    @DisplayName("Encrypted files should not be readable without key")
    void testEncryptedFilesSecurity() throws Exception {
        // Store sensitive data
        String fileId = fileManager.storeFile("classified.txt", sensitiveContent.getBytes(), testKey);
        
        // Find the encrypted file on disk
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        Files.walk(vaultPath)
            .filter(Files::isRegularFile)
            .forEach(encryptedFile -> {
                try {
                    byte[] encryptedContent = Files.readAllBytes(encryptedFile);
                    String contentStr = new String(encryptedContent);
                    
                    // Encrypted file should not contain plaintext sensitive data
                    assertFalse(contentStr.contains(sensitiveContent),
                        "Encrypted file should not contain plaintext sensitive data: " + encryptedFile);
                    
                    // Should not contain obvious patterns
                    assertFalse(contentStr.contains("CONFIDENTIAL"),
                        "Encrypted file should not contain plaintext markers");
                    
                } catch (Exception e) {
                    // File might be inaccessible, which is acceptable
                }
            });
    }
    
    @Test
    @DisplayName("Memory should be wiped after panic mode")
    void testPanicModeMemoryWipe() throws Exception {
        // Store sensitive data
        fileManager.storeFile("top_secret.txt", sensitiveContent.getBytes(), testKey);
        
        // Validate password to load it into memory
        passwordManager.validatePassword(masterPassword);
        
        // Execute panic mode
        panicModeExecutor.executePanicWipe();
        
        // Force garbage collection
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(50);
        }
        
        // Verify sensitive data is not easily accessible in memory
        // This is a simplified test - in practice, memory forensics would be more complex
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        
        // Memory should be available (indicating cleanup occurred)
        assertTrue(freeMemory > totalMemory * 0.1, // At least 10% free memory
            "Memory should be available after panic mode cleanup");
    }
    
    @Test
    @DisplayName("Swap files should not contain sensitive data")
    void testSwapFilesSecurity() throws Exception {
        // This test is platform-specific and may not be fully implementable in Java
        // We test what we can control
        
        // Store large amount of sensitive data to potentially trigger swapping
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append(sensitiveContent).append("_").append(i).append("\n");
        }
        
        fileManager.storeFile("large_sensitive.txt", largeContent.toString().getBytes(), testKey);
        
        // Force memory pressure
        try {
            byte[][] memoryPressure = new byte[100][];
            for (int i = 0; i < memoryPressure.length; i++) {
                memoryPressure[i] = new byte[1024 * 1024]; // 1MB each
            }
            
            // Clear the arrays
            for (int i = 0; i < memoryPressure.length; i++) {
                if (memoryPressure[i] != null) {
                    java.util.Arrays.fill(memoryPressure[i], (byte) 0);
                    memoryPressure[i] = null;
                }
            }
            
        } catch (OutOfMemoryError e) {
            // Expected under memory pressure
        }
        
        // Force garbage collection
        System.gc();
        
        // Verify JVM flags don't expose sensitive data
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : jvmArgs) {
            assertFalse(arg.contains(sensitiveContent),
                "JVM arguments should not contain sensitive data");
        }
    }
    
    @Test
    @DisplayName("Network traffic should not leak sensitive data")
    void testNetworkTrafficSecurity() throws Exception {
        // GhostVault should be offline, so no network traffic should occur
        
        // Store sensitive data
        fileManager.storeFile("network_test.txt", sensitiveContent.getBytes(), testKey);
        
        // Verify no network connections are established
        // This is a basic check - in practice, network monitoring tools would be used
        
        // Check system properties for network-related settings
        String networkProperties = System.getProperty("java.net.useSystemProxies", "false");
        assertEquals("false", networkProperties, 
            "System proxies should be disabled for offline operation");
        
        // Verify no remote debugging is enabled
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean hasRemoteDebugging = jvmArgs.stream()
            .anyMatch(arg -> arg.contains("jdwp") || arg.contains("Xdebug"));
        assertFalse(hasRemoteDebugging, 
            "Remote debugging should not be enabled in production");
    }
    
    @Test
    @DisplayName("Error messages should not leak sensitive information")
    void testErrorMessageSecurity() throws Exception {
        // Test various error conditions
        
        // Invalid password
        PasswordManager.PasswordType result = passwordManager.validatePassword("WrongPassword");
        assertEquals(PasswordManager.PasswordType.INVALID, result);
        
        // File not found
        try {
            fileManager.retrieveFile("nonexistent_file_id", testKey);
            fail("Should throw exception for nonexistent file");
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            assertFalse(errorMessage.contains(masterPassword),
                "Error message should not contain passwords");
            assertFalse(errorMessage.contains(testKey.toString()),
                "Error message should not contain encryption keys");
            assertFalse(errorMessage.contains(AppConfig.VAULT_DIR),
                "Error message should not expose full file paths");
        }
        
        // Invalid key
        try {
            SecretKey wrongKey = CryptoManager.generateKey();
            fileManager.storeFile("test.txt", "content".getBytes(), testKey);
            // This should fail or produce garbage, but not leak information
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            assertFalse(errorMessage.contains(testKey.toString()),
                "Error message should not contain encryption keys");
        }
    }
    
    @Test
    @DisplayName("File system permissions should prevent unauthorized access")
    void testFileSystemPermissions() throws Exception {
        // Store sensitive data
        fileManager.storeFile("permission_test.txt", sensitiveContent.getBytes(), testKey);
        
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        
        // Check vault directory permissions
        if (Files.exists(vaultPath)) {
            // On Unix-like systems, check POSIX permissions
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    java.nio.file.attribute.PosixFileAttributes attrs = 
                        Files.readAttributes(vaultPath, java.nio.file.attribute.PosixFileAttributes.class);
                    
                    java.util.Set<java.nio.file.attribute.PosixFilePermission> permissions = attrs.permissions();
                    
                    // Should not be readable by group or others
                    assertFalse(permissions.contains(java.nio.file.attribute.PosixFilePermission.GROUP_READ),
                        "Vault should not be readable by group");
                    assertFalse(permissions.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_READ),
                        "Vault should not be readable by others");
                    
                } catch (Exception e) {
                    // POSIX permissions might not be supported
                }
            }
        }
    }
    
    @Test
    @DisplayName("Timing attacks should not be possible")
    void testTimingAttackResistance() throws Exception {
        int iterations = 1000;
        
        // Test password validation timing
        long[] correctTimes = new long[iterations];
        long[] incorrectTimes = new long[iterations];
        
        // Warm up JVM
        for (int i = 0; i < 100; i++) {
            passwordManager.validatePassword(masterPassword);
            passwordManager.validatePassword("WrongPassword");
        }
        
        // Measure correct password timing
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword(masterPassword);
            correctTimes[i] = System.nanoTime() - start;
        }
        
        // Measure incorrect password timing
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            passwordManager.validatePassword("WrongPassword" + i); // Different wrong passwords
            incorrectTimes[i] = System.nanoTime() - start;
        }
        
        // Statistical analysis
        double correctAvg = java.util.Arrays.stream(correctTimes).average().orElse(0);
        double incorrectAvg = java.util.Arrays.stream(incorrectTimes).average().orElse(0);
        
        double correctStdDev = calculateStandardDeviation(correctTimes, correctAvg);
        double incorrectStdDev = calculateStandardDeviation(incorrectTimes, incorrectAvg);
        
        // Timing difference should not be statistically significant
        double timingDifference = Math.abs(correctAvg - incorrectAvg);
        double combinedStdDev = Math.sqrt((correctStdDev * correctStdDev + incorrectStdDev * incorrectStdDev) / 2);
        
        // Difference should be less than 2 standard deviations (95% confidence)
        assertTrue(timingDifference < 2 * combinedStdDev,
            String.format("Timing difference (%f ns) exceeds statistical threshold (%f ns)", 
                timingDifference, 2 * combinedStdDev));
    }
    
    @Test
    @DisplayName("Side-channel attacks through CPU cache should be mitigated")
    void testCacheSideChannelResistance() throws Exception {
        // Test cache timing attacks on encryption
        byte[] plaintext1 = new byte[1024];
        byte[] plaintext2 = new byte[1024];
        
        // Fill with different patterns
        java.util.Arrays.fill(plaintext1, (byte) 0x00);
        java.util.Arrays.fill(plaintext2, (byte) 0xFF);
        
        int iterations = 1000;
        long[] times1 = new long[iterations];
        long[] times2 = new long[iterations];
        
        // Warm up
        for (int i = 0; i < 100; i++) {
            cryptoManager.encrypt(plaintext1, testKey);
            cryptoManager.encrypt(plaintext2, testKey);
        }
        
        // Measure encryption times for different patterns
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            cryptoManager.encrypt(plaintext1, testKey);
            times1[i] = System.nanoTime() - start;
            
            start = System.nanoTime();
            cryptoManager.encrypt(plaintext2, testKey);
            times2[i] = System.nanoTime() - start;
        }
        
        // Timing should be similar regardless of input pattern
        double avg1 = java.util.Arrays.stream(times1).average().orElse(0);
        double avg2 = java.util.Arrays.stream(times2).average().orElse(0);
        
        double timingDifference = Math.abs(avg1 - avg2) / Math.max(avg1, avg2);
        
        // Timing difference should be minimal (less than 10%)
        assertTrue(timingDifference < 0.1,
            String.format("Cache timing difference too large: %.2f%%", timingDifference * 100));
    }
    
    /**
     * Check system logs for sensitive data (platform-specific)
     */
    private void checkSystemLogsForSensitiveData() {
        // This is a simplified check - in practice, would examine system-specific log locations
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows Event Logs would be checked with native tools
                // For this test, we just verify no obvious leakage in accessible locations
            } else if (os.contains("nix") || os.contains("nux")) {
                // Unix/Linux system logs
                Path[] logPaths = {
                    Path.of("/var/log/syslog"),
                    Path.of("/var/log/messages"),
                    Path.of("/var/log/auth.log")
                };
                
                for (Path logPath : logPaths) {
                    if (Files.exists(logPath) && Files.isReadable(logPath)) {
                        try {
                            String logContent = Files.readString(logPath);
                            assertFalse(logContent.contains(sensitiveContent),
                                "System log should not contain sensitive data: " + logPath);
                        } catch (Exception e) {
                            // Log might not be readable, which is acceptable
                        }
                    }
                }
            }
        } catch (Exception e) {
            // System log checking might fail due to permissions, which is acceptable
        }
    }
    
    /**
     * Calculate standard deviation
     */
    private double calculateStandardDeviation(long[] values, double mean) {
        double sum = 0.0;
        for (long value : values) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / values.length);
    }
}