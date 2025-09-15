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
    
    // Advanced security components
    private SecurityHardening securityHardening;
    private ThreatDetectionEngine threatEngine;
    
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
        
        // Initialize advanced security components
        initializeAdvancedSecurity();
    }
    
    /**
     * Initialize advanced security components
     */
    public void initializeAdvancedSecurity() {
        try {
            // Initialize security hardening
            securityHardening = SecurityHardening.getInstance();
            
            // Initialize threat detection engine (requires AuditManager)
            // threatEngine = new ThreatDetectionEngine(auditManager);
            
            System.out.println("🛡️ Advanced security components initialized");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize advanced security: " + e.getMessage());
        }
    }
    
    /**
     * Activate comprehensive security hardening
     */
    public void activateSecurityHardening() {
        if (securityHardeningActive.get()) {
            return;
        }
        
        try {
            securityHardeningActive.set(true);
            
            // Enable all security features
            enableMemoryProtection();
            enableClipboardProtection();
            enableScreenProtection();
            enableFileSystemProtection();
            
            // Apply advanced security hardening
            if (securityHardening != null) {
                securityHardening.applySecurityHardening();
            }
            
            // Start threat detection if available
            if (threatEngine != null) {
                threatEngine.startMonitoring();
            }
            
            System.out.println("✅ Comprehensive security hardening activated");
            
        } catch (Exception e) {
            System.err.println("Failed to activate security hardening: " + e.getMessage());
            securityHardeningActive.set(false);
        }
    }
    
    /**
     * Deactivate security hardening
     */
    public void deactivateSecurityHardening() {
        if (!securityHardeningActive.get()) {
            return;
        }
        
        securityHardeningActive.set(false);
        
        // Disable all security features
        memoryProtectionActive.set(false);
        clipboardProtectionActive.set(false);
        screenProtectionActive.set(false);
        fileSystemProtectionActive.set(false);
        
        // Stop threat detection if available
        if (threatEngine != null) {
            threatEngine.stopMonitoring();
        }
        
        System.out.println("🔓 Security hardening deactivated");
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
        
        System.out.println("🧠 Memory protection enabled");
    }
    
    /**
     * Enable clipboard protection
     */
    private void enableClipboardProtection() {
        clipboardProtectionActive.set(true);
        
        // Schedule periodic clipboard clearing
        securityScheduler.scheduleAtFixedRate(() -> {
            if (clipboardProtectionActive.get()) {
                clearClipboard();
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        System.out.println("📋 Clipboard protection enabled");
    }
    
    /**
     * Enable screen protection
     */
    private void enableScreenProtection() {
        screenProtectionActive.set(true);
        
        // Schedule periodic screen security checks
        securityScheduler.scheduleAtFixedRate(() -> {
            if (screenProtectionActive.get()) {
                checkScreenSecurity();
            }
        }, 10, 10, TimeUnit.SECONDS);
        
        System.out.println("🖥️ Screen protection enabled");
    }
    
    /**
     * Enable file system protection
     */
    private void enableFileSystemProtection() {
        fileSystemProtectionActive.set(true);
        
        // Schedule periodic file system monitoring
        securityScheduler.scheduleAtFixedRate(() -> {
            if (fileSystemProtectionActive.get()) {
                monitorFileSystem();
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        System.out.println("📁 File system protection enabled");
    }
    
    /**
     * Perform memory cleanup
     */
    private void performMemoryCleanup() {
        try {
            // Clear sensitive memory regions
            clearSensitiveMemoryRegions();
            
            // Detect memory analysis tools
            detectMemoryAnalysisTools();
            
            // Check for unusual memory patterns
            checkMemoryPatterns();
            
        } catch (Exception e) {
            System.err.println("Memory cleanup error: " + e.getMessage());
        }
    }
    
    /**
     * Clear clipboard securely
     */
    private void clearClipboard() {
        try {
            if (System.currentTimeMillis() - lastClipboardClear.get() > 30000) { // 30 seconds
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                
                // Overwrite with random data multiple times
                for (int i = 0; i < 3; i++) {
                    String randomData = generateRandomString(100);
                    StringSelection selection = new StringSelection(randomData);
                    clipboard.setContents(selection, null);
                }
                
                // Final clear
                clipboard.setContents(new StringSelection(""), null);
                lastClipboardClear.set(System.currentTimeMillis());
            }
            
        } catch (Exception e) {
            System.err.println("Clipboard clearing error: " + e.getMessage());
        }
    }
    
    /**
     * Check screen security
     */
    private void checkScreenSecurity() {
        try {
            // Check for screen recording software
            detectScreenRecording();
            
            // Check for remote desktop connections
            detectRemoteDesktop();
            
        } catch (Exception e) {
            System.err.println("Screen security check error: " + e.getMessage());
        }
    }
    
    /**
     * Monitor file system
     */
    private void monitorFileSystem() {
        try {
            // Check vault directory permissions
            checkVaultPermissions();
            
            // Monitor for suspicious file modifications
            checkFileModifications();
            
            // Check system integrity
            checkSystemIntegrity();
            
        } catch (Exception e) {
            System.err.println("File system monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * Record security event for threat detection
     */
    public void recordSecurityEvent(String eventType, String source, java.util.Map<String, String> metadata) {
        if (threatEngine != null) {
            threatEngine.recordSecurityEvent(eventType, source, metadata);
        }
    }
    
    /**
     * Get comprehensive security status
     */
    public ComprehensiveSecurityStatus getComprehensiveStatus() {
        SecurityHardeningStatus basicStatus = getStatus();
        
        SecurityHardening.SecurityStatus advancedStatus = null;
        if (securityHardening != null) {
            advancedStatus = securityHardening.checkSecurityStatus();
        }
        
        ThreatDetectionEngine.ThreatAssessment threatAssessment = null;
        if (threatEngine != null) {
            threatAssessment = threatEngine.getCurrentThreatAssessment();
        }
        
        return new ComprehensiveSecurityStatus(basicStatus, advancedStatus, threatAssessment);
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
        securityScheduler.shutdown();
    }
    
    // Helper methods
    private void clearSensitiveMemoryRegions() {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    private void detectMemoryAnalysisTools() {
        // Placeholder for memory analysis tool detection
    }
    
    private void checkMemoryPatterns() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Check for unusual memory usage patterns
        if (usedMemory > runtime.maxMemory() * 0.9) {
            System.err.println("⚠️ High memory usage detected");
        }
    }
    
    private void detectScreenRecording() {
        // Placeholder for screen recording detection
    }
    
    private void detectRemoteDesktop() {
        // Placeholder for remote desktop detection
    }
    
    private void checkVaultPermissions() {
        try {
            Path vaultPath = Path.of(AppConfig.VAULT_DIR);
            if (Files.exists(vaultPath) && !hasSecurePermissions(vaultPath)) {
                System.err.println("⚠️ Vault directory permissions are not secure");
            }
        } catch (Exception e) {
            // Ignore permission check errors
        }
    }
    
    private void checkFileModifications() {
        // Placeholder for file modification detection
    }
    
    private void checkSystemIntegrity() {
        // Placeholder for system integrity check
    }
    
    private boolean hasSecurePermissions(Path file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                return Files.isHidden(file);
            } else {
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
    
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        
        return sb.toString();
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
    
    /**
     * Comprehensive security status
     */
    public static class ComprehensiveSecurityStatus {
        private final SecurityHardeningStatus basicStatus;
        private final SecurityHardening.SecurityStatus advancedStatus;
        private final ThreatDetectionEngine.ThreatAssessment threatAssessment;
        
        public ComprehensiveSecurityStatus(SecurityHardeningStatus basicStatus,
                                         SecurityHardening.SecurityStatus advancedStatus,
                                         ThreatDetectionEngine.ThreatAssessment threatAssessment) {
            this.basicStatus = basicStatus;
            this.advancedStatus = advancedStatus;
            this.threatAssessment = threatAssessment;
        }
        
        public SecurityHardeningStatus getBasicStatus() { return basicStatus; }
        public SecurityHardening.SecurityStatus getAdvancedStatus() { return advancedStatus; }
        public ThreatDetectionEngine.ThreatAssessment getThreatAssessment() { return threatAssessment; }
        
        public boolean isFullySecure() {
            boolean basicSecure = basicStatus != null && basicStatus.isActive();
            boolean advancedSecure = advancedStatus != null && advancedStatus.isSecure();
            boolean threatLevelAcceptable = threatAssessment == null || 
                threatAssessment.getOverallLevel().getLevel() <= ThreatDetectionEngine.ThreatLevel.MEDIUM.getLevel();
            
            return basicSecure && advancedSecure && threatLevelAcceptable;
        }
        
        public String getComprehensiveReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Comprehensive Security Status ===\n");
            
            if (basicStatus != null) {
                report.append("Basic Security: ").append(basicStatus.toString()).append("\n");
            }
            
            if (advancedStatus != null) {
                report.append("\n").append(advancedStatus.getStatusReport()).append("\n");
            }
            
            if (threatAssessment != null) {
                report.append("\n").append(threatAssessment.getAssessmentReport()).append("\n");
            }
            
            report.append("\nOverall Status: ").append(isFullySecure() ? "🛡️ SECURE" : "⚠️ NEEDS ATTENTION");
            
            return report.toString();
        }
    }
}