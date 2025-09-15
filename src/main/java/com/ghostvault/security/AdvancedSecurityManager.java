package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.exception.SecurityException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced security features and system hardening for GhostVault
 * Implements memory security, clipboard protection, screen security, and side-channel attack resistance
 */
public class AdvancedSecurityManager {
    
    private final ScheduledExecutorService securityScheduler;
    private final AtomicBoolean securityHardeningActive;
    private final AtomicLong lastClipboardClear;
    private final SecureRandom secureRandom;
    
    // Security monitoring
    private final AtomicBoolean memoryProtectionActive;
    private final AtomicBoolean clipboardProtectionActive;
    private final AtomicBoolean screenProtectionActive;
    private final AtomicBoolean fileSystemProtectionActive;
    
    // Timing attack resistance
    private final AtomicLong baseOperationTime;
    private static final long TARGET_OPERATION_TIME_MS = 100;
    
    public AdvancedSecurityManager() {
        this.securityScheduler = Executors.newScheduledThreadPool(3);
        this.securityHardeningActive = new AtomicBoolean(false);
        this.lastClipboardClear = new AtomicLong(0);
        this.secureRandom = new SecureRandom();
        
        this.memoryProtectionActive = new AtomicBoolean(false);
        this.clipboardProtectionActive = new AtomicBoolean(false);
        this.screenProtectionActive = new AtomicBoolean(false);
        this.fileSystemProtectionActive = new AtomicBoolean(false);
        
        this.baseOperationTime = new AtomicLong(TARGET_OPERATION_TIME_MS);
    }
    
    /**
     * Activate all security hardening features
     */
    public void activateSecurityHardening() throws Exception {
        if (securityHardeningActive.get()) {
            return; // Already active
        }
        
        securityHardeningActive.set(true);
        
        // Enable memory protection
        enableMemoryProtection();
        
        // Enable clipboard protection
        enableClipboardProtection();
        
        // Enable screen protection
        enableScreenProtection();
        
        // Enable file system protection
        enableFileSystemProtection();
        
        // Start security monitoring tasks
        startSecurityMonitoring();
        
        System.out.println("🛡️ Advanced security hardening activated");
    }
    
    /**
     * Deactivate security hardening
     */
    public void deactivateSecurityHardening() {
        securityHardeningActive.set(false);
        
        memoryProtectionActive.set(false);
        clipboardProtectionActive.set(false);
        screenProtectionActive.set(false);
        fileSystemProtectionActive.set(false);
        
        if (securityScheduler != null && !securityScheduler.isShutdown()) {
            securityScheduler.shutdown();
        }
        
        System.out.println("🛡️ Advanced security hardening deactivated");
    }
} 
   
    /**
     * Enable memory protection features
     */
    private void enableMemoryProtection() {
        memoryProtectionActive.set(true);
        
        // Schedule periodic memory cleanup
        securityScheduler.scheduleAtFixedRate(() -> {
            if (memoryProtectionActive.get()) {
                performMemoryCleanup();
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        // Set up memory monitoring
        securityScheduler.scheduleAtFixedRate(() -> {
            if (memoryProtectionActive.get()) {
                monitorMemoryUsage();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    /**
     * Enable clipboard protection
     */
    private void enableClipboardProtection() {
        clipboardProtectionActive.set(true);
        
        // Schedule automatic clipboard clearing
        securityScheduler.scheduleAtFixedRate(() -> {
            if (clipboardProtectionActive.get()) {
                clearClipboardIfNeeded();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Enable screen protection features
     */
    private void enableScreenProtection() {
        screenProtectionActive.set(true);
        
        // Monitor for screen capture attempts
        securityScheduler.scheduleAtFixedRate(() -> {
            if (screenProtectionActive.get()) {
                detectScreenCaptureAttempts();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Enable file system protection
     */
    private void enableFileSystemProtection() {
        fileSystemProtectionActive.set(true);
        
        // Set secure file permissions
        setSecureFilePermissions();
        
        // Monitor file system changes
        securityScheduler.scheduleAtFixedRate(() -> {
            if (fileSystemProtectionActive.get()) {
                monitorFileSystemChanges();
            }
        }, 15, 15, TimeUnit.SECONDS);
    }
    
    /**
     * Start security monitoring tasks
     */
    private void startSecurityMonitoring() {
        // Monitor for debugging tools
        securityScheduler.scheduleAtFixedRate(() -> {
            if (securityHardeningActive.get()) {
                detectDebuggingTools();
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        // Monitor system integrity
        securityScheduler.scheduleAtFixedRate(() -> {
            if (securityHardeningActive.get()) {
                checkSystemIntegrity();
            }
        }, 60, 60, TimeUnit.SECONDS);
    }
    
    /**
     * Perform memory cleanup and protection
     */
    private void performMemoryCleanup() {
        try {
            // Force garbage collection
            System.gc();
            
            // Clear any cached sensitive data
            clearSensitiveMemoryRegions();
            
            // Check for memory dumps or analysis tools
            detectMemoryAnalysisTools();
            
        } catch (Exception e) {
            System.err.println("Memory cleanup error: " + e.getMessage());
        }
    }
    
    /**
     * Monitor memory usage for anomalies
     */
    private void monitorMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Check for excessive memory usage
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        
        if (memoryUsagePercent > 85) {
            System.err.println("⚠️ High memory usage detected: " + String.format("%.1f%%", memoryUsagePercent));
            
            // Trigger aggressive cleanup
            performMemoryCleanup();
        }
        
        // Check for unusual memory patterns that might indicate attacks
        if (detectUnusualMemoryPatterns(usedMemory)) {
            System.err.println("🚨 Unusual memory patterns detected - possible memory attack");
        }
    }
    
    /**
     * Clear clipboard if it contains sensitive data
     */
    private void clearClipboardIfNeeded() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Clear clipboard after 30 seconds of any clipboard operation
            if (currentTime - lastClipboardClear.get() > 30000) {
                clearClipboard();
                lastClipboardClear.set(currentTime);
            }
            
        } catch (Exception e) {
            // Clipboard operations can fail, ignore silently
        }
    }
    
    /**
     * Clear system clipboard
     */
    public void clearClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            // Overwrite with random data first
            String randomData = generateRandomString(100);
            StringSelection randomSelection = new StringSelection(randomData);
            clipboard.setContents(randomSelection, null);
            
            // Then clear with empty string
            StringSelection emptySelection = new StringSelection("");
            clipboard.setContents(emptySelection, null);
            
        } catch (Exception e) {
            // Clipboard operations can fail on some systems
        }
    }
    
    /**
     * Detect screen capture attempts
     */
    private void detectScreenCaptureAttempts() {
        try {
            // Check for common screen capture tools
            String[] screenCaptureProcesses = {
                "snagit", "greenshot", "lightshot", "gyazo", "puush",
                "screenpresso", "faststone", "picpick", "screenshot",
                "obs", "camtasia", "bandicam", "fraps"
            };
            
            if (detectRunningProcesses(screenCaptureProcesses)) {
                System.err.println("🚨 Screen capture software detected");
                // In a real implementation, might blur the screen or show warning
            }
            
        } catch (Exception e) {
            // Process detection can fail, continue silently
        }
    }
    
    /**
     * Set secure file permissions for vault files
     */
    private void setSecureFilePermissions() {
        try {
            Path vaultPath = Path.of(AppConfig.VAULT_DIR);
            
            if (Files.exists(vaultPath)) {
                // On Unix-like systems, set restrictive permissions
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    Set<PosixFilePermission> permissions = new HashSet<>();
                    permissions.add(PosixFilePermission.OWNER_READ);
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                    
                    Files.setPosixFilePermissions(vaultPath, permissions);
                    
                    // Set permissions for all vault files
                    Files.walk(vaultPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Set<PosixFilePermission> filePerms = new HashSet<>();
                                filePerms.add(PosixFilePermission.OWNER_READ);
                                filePerms.add(PosixFilePermission.OWNER_WRITE);
                                Files.setPosixFilePermissions(file, filePerms);
                            } catch (Exception e) {
                                // Continue with other files
                            }
                        });
                }
                
                // On Windows, set file attributes
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    try {
                        File vaultDir = vaultPath.toFile();
                        Runtime.getRuntime().exec("attrib +H +S \"" + vaultDir.getAbsolutePath() + "\"");
                    } catch (Exception e) {
                        // Ignore if attribute setting fails
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to set secure file permissions: " + e.getMessage());
        }
    }
    
    /**
     * Monitor file system changes
     */
    private void monitorFileSystemChanges() {
        try {
            Path vaultPath = Path.of(AppConfig.VAULT_DIR);
            
            if (Files.exists(vaultPath)) {
                // Check for unauthorized modifications
                Files.walk(vaultPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // Check file permissions
                            if (!hasSecurePermissions(file)) {
                                System.err.println("🚨 Insecure file permissions detected: " + file);
                            }
                            
                            // Check for suspicious file modifications
                            if (detectSuspiciousModification(file)) {
                                System.err.println("🚨 Suspicious file modification detected: " + file);
                            }
                            
                        } catch (Exception e) {
                            // Continue with other files
                        }
                    });
            }
            
        } catch (Exception e) {
            System.err.println("File system monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * Detect debugging tools
     */
    private void detectDebuggingTools() {
        try {
            String[] debuggingTools = {
                "jdb", "jconsole", "jvisualvm", "jprofiler", "yourkit",
                "eclipse", "intellij", "netbeans", "debugger", "profiler",
                "wireshark", "fiddler", "burpsuite", "owasp", "metasploit"
            };
            
            if (detectRunningProcesses(debuggingTools)) {
                System.err.println("🚨 Debugging/analysis tools detected");
                // In a real implementation, might trigger additional security measures
            }
            
        } catch (Exception e) {
            // Process detection can fail, continue silently
        }
    }
    
    /**
     * Check system integrity
     */
    private void checkSystemIntegrity() {
        try {
            // Check if critical files have been modified
            checkCriticalFileIntegrity();
            
            // Check for rootkits or system modifications
            checkSystemModifications();
            
            // Verify cryptographic libraries
            verifyCryptographicLibraries();
            
        } catch (Exception e) {
            System.err.println("System integrity check error: " + e.getMessage());
        }
    }
    
    /**
     * Implement timing attack resistance
     */
    public void executeWithTimingResistance(Runnable operation) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the operation
            operation.run();
            
        } finally {
            // Ensure consistent timing
            long executionTime = System.currentTimeMillis() - startTime;
            long targetTime = baseOperationTime.get();
            
            if (executionTime < targetTime) {
                try {
                    Thread.sleep(targetTime - executionTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Add random jitter to prevent timing analysis
            try {
                Thread.sleep(secureRandom.nextInt(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Secure array handling with automatic wiping
     */
    public static class SecureArray {
        private byte[] data;
        private final boolean autoWipe;
        
        public SecureArray(int size) {
            this(size, true);
        }
        
        public SecureArray(int size, boolean autoWipe) {
            this.data = new byte[size];
            this.autoWipe = autoWipe;
        }
        
        public byte[] getData() {
            return data;
        }
        
        public int length() {
            return data != null ? data.length : 0;
        }
        
        public void wipe() {
            if (data != null) {
                MemoryUtils.secureWipe(data);
                data = null;
            }
        }
        
        @Override
        protected void finalize() throws Throwable {
            if (autoWipe) {
                wipe();
            }
            super.finalize();
        }
    }
    
    /**
     * Secure string handling with automatic clearing
     */
    public static class SecureString {
        private char[] data;
        private final boolean autoWipe;
        
        public SecureString(String value) {
            this(value, true);
        }
        
        public SecureString(String value, boolean autoWipe) {
            this.data = value != null ? value.toCharArray() : new char[0];
            this.autoWipe = autoWipe;
        }
        
        public char[] getData() {
            return data;
        }
        
        public String getString() {
            return data != null ? new String(data) : "";
        }
        
        public int length() {
            return data != null ? data.length : 0;
        }
        
        public void wipe() {
            if (data != null) {
                MemoryUtils.secureWipe(data);
                data = null;
            }
        }
        
        @Override
        protected void finalize() throws Throwable {
            if (autoWipe) {
                wipe();
            }
            super.finalize();
        }
    }
    
    /**
     * Copy sensitive data to clipboard with automatic clearing
     */
    public void copyToClipboardSecurely(String data, int clearAfterSeconds) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(data);
            clipboard.setContents(selection, null);
            
            lastClipboardClear.set(System.currentTimeMillis());
            
            // Schedule automatic clearing
            securityScheduler.schedule(() -> {
                clearClipboard();
            }, clearAfterSeconds, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Secure clipboard operation failed: " + e.getMessage());
        }
    }
    
    /**
     * Perform atomic file operations to prevent corruption
     */
    public void performAtomicFileOperation(Path targetFile, byte[] data) throws Exception {
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        
        try {
            // Write to temporary file first
            Files.write(tempFile, data);
            
            // Verify write was successful
            if (!Files.exists(tempFile) || Files.size(tempFile) != data.length) {
                throw new SecurityException("Atomic file write verification failed");
            }
            
            // Atomically move to target location
            Files.move(tempFile, targetFile, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            
        } catch (Exception e) {
            // Clean up temporary file if operation failed
            try {
                if (Files.exists(tempFile)) {
                    Files.delete(tempFile);
                }
            } catch (Exception cleanupEx) {
                // Ignore cleanup errors
            }
            throw e;
        }
    }
    
    /**
     * Implement side-channel attack resistance for cryptographic operations
     */
    public byte[] performResistantCryptographicOperation(CryptoOperation operation, byte[] input) throws Exception {
        // Add random delays to prevent timing analysis
        long randomDelay = secureRandom.nextInt(50) + 10; // 10-60ms
        Thread.sleep(randomDelay);
        
        // Perform operation with consistent timing
        return executeWithTimingResistance(() -> {
            try {
                return operation.execute(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Execute operation with timing resistance
     */
    private <T> T executeWithTimingResistance(java.util.function.Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        
        try {
            return operation.get();
        } finally {
            // Ensure consistent timing
            long executionTime = System.currentTimeMillis() - startTime;
            long targetTime = baseOperationTime.get();
            
            if (executionTime < targetTime) {
                try {
                    Thread.sleep(targetTime - executionTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Clear sensitive memory regions
     */
    private void clearSensitiveMemoryRegions() {
        // Force finalization of objects that might contain sensitive data
        System.runFinalization();
        
        // Multiple garbage collection passes
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Detect memory analysis tools
     */
    private void detectMemoryAnalysisTools() {
        // Check for common memory analysis tools
        String[] memoryTools = {
            "volatility", "rekall", "memoryze", "dumpit", "winpmem",
            "lime", "fmem", "pmem", "memdump", "procmon"
        };
        
        if (detectRunningProcesses(memoryTools)) {
            System.err.println("🚨 Memory analysis tools detected");
        }
    }
    
    /**
     * Detect running processes by name
     */
    private boolean detectRunningProcesses(String[] processNames) {
        try {
            // Get list of running processes (platform-specific)
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("tasklist /fo csv");
            } else {
                process = Runtime.getRuntime().exec("ps -eo comm");
            }
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String lowerLine = line.toLowerCase();
                    
                    for (String processName : processNames) {
                        if (lowerLine.contains(processName.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // Process detection can fail, continue silently
        }
        
        return false;
    }
    
    /**
     * Check critical file integrity
     */
    private void checkCriticalFileIntegrity() {
        try {
            // Check vault configuration files
            String[] criticalFiles = {
                AppConfig.CONFIG_FILE,
                AppConfig.SALT_FILE,
                AppConfig.METADATA_FILE
            };
            
            for (String filePath : criticalFiles) {
                File file = new File(filePath);
                if (file.exists()) {
                    // Check file size and modification time
                    if (detectSuspiciousFileChanges(file)) {
                        System.err.println("🚨 Critical file modification detected: " + filePath);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Critical file integrity check failed: " + e.getMessage());
        }
    }
    
    /**
     * Check for system modifications
     */
    private void checkSystemModifications() {
        try {
            // Check for common rootkit indicators
            checkRootkitIndicators();
            
            // Check system file integrity
            checkSystemFileIntegrity();
            
            // Check for suspicious network connections
            checkNetworkConnections();
            
        } catch (Exception e) {
            System.err.println("System modification check failed: " + e.getMessage());
        }
    }
    
    /**
     * Verify cryptographic libraries haven't been tampered with
     */
    private void verifyCryptographicLibraries() {
        try {
            // Test basic cryptographic operations to ensure they work correctly
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // Verify AES is working correctly
            byte[] testData = "test".getBytes();
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
            keyGen.init(256);
            javax.crypto.SecretKey testKey = keyGen.generateKey();
            
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, testKey);
            byte[] encrypted = cipher.doFinal(testData);
            
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, testKey, cipher.getParameters());
            byte[] decrypted = cipher.doFinal(encrypted);
            
            if (!java.util.Arrays.equals(testData, decrypted)) {
                throw new SecurityException("Cryptographic library verification failed");
            }
            
        } catch (Exception e) {
            System.err.println("🚨 Cryptographic library verification failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper methods for security checks
     */
    private boolean detectUnusualMemoryPatterns(long currentMemory) {
        // Simple heuristic - in a real implementation, this would be more sophisticated
        return false; // Placeholder
    }
    
    private boolean hasSecurePermissions(Path file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // On Windows, check if file is hidden/system
                return Files.isHidden(file);
            } else {
                // On Unix-like systems, check POSIX permissions
                Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
                return permissions.contains(PosixFilePermission.OWNER_READ) &&
                       permissions.contains(PosixFilePermission.OWNER_WRITE) &&
                       !permissions.contains(PosixFilePermission.GROUP_READ) &&
                       !permissions.contains(PosixFilePermission.OTHERS_READ);
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean detectSuspiciousModification(Path file) {
        // Placeholder for file modification detection
        return false;
    }
    
    private boolean detectSuspiciousFileChanges(File file) {
        // Placeholder for suspicious file change detection
        return false;
    }
    
    private void checkRootkitIndicators() {
        // Placeholder for rootkit detection
    }
    
    private void checkSystemFileIntegrity() {
        // Placeholder for system file integrity check
    }
    
    private void checkNetworkConnections() {
        // Placeholder for network connection monitoring
    }
    
    /**
     * Generate random string for clipboard clearing
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Get security hardening status
     */
    public SecurityHardeningStatus getStatus() {
        return new SecurityHardeningStatus(
            securityHardeningActive.get(),
            memoryProtectionActive.get(),
            clipboardProtectionActive.get(),
            screenProtectionActive.get(),
            fileSystemProtectionActive.get()
        );
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        deactivateSecurityHardening();
    }
    
    /**
     * Functional interface for crypto operations
     */
    @FunctionalInterface
    public interface CryptoOperation {
        byte[] execute(byte[] input) throws Exception;
    }
    
    /**
     * Security hardening status
     */
    public static class SecurityHardeningStatus {
        private final boolean active;
        private final boolean memoryProtection;
        private final boolean clipboardProtection;
        private final boolean screenProtection;
        private final boolean fileSystemProtection;
        
        public SecurityHardeningStatus(boolean active, boolean memoryProtection, 
                                     boolean clipboardProtection, boolean screenProtection, 
                                     boolean fileSystemProtection) {
            this.active = active;
            this.memoryProtection = memoryProtection;
            this.clipboardProtection = clipboardProtection;
            this.screenProtection = screenProtection;
            this.fileSystemProtection = fileSystemProtection;
        }
        
        public boolean isActive() { return active; }
        public boolean isMemoryProtectionActive() { return memoryProtection; }
        public boolean isClipboardProtectionActive() { return clipboardProtection; }
        public boolean isScreenProtectionActive() { return screenProtection; }
        public boolean isFileSystemProtectionActive() { return fileSystemProtection; }
        
        public int getActiveFeatureCount() {
            int count = 0;
            if (memoryProtection) count++;
            if (clipboardProtection) count++;
            if (screenProtection) count++;
            if (fileSystemProtection) count++;
            return count;
        }
        
        @Override
        public String toString() {
            return String.format("SecurityHardening{active=%s, features=%d/4}", 
                active, getActiveFeatureCount());
        }
    }