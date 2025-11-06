package com.ghostvault.ui.preview;

// import com.ghostvault.core.CryptoManager; // Commented out - class not available
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.SessionManager;
import com.ghostvault.audit.AuditManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.Arrays;

/**
 * Secure media manager for handling encrypted media files in memory
 * Provides secure streaming without temporary files and memory cleanup
 */
public class SecureMediaManager {
    
    // private final CryptoManager cryptoManager; // Commented out - class not available
    private final SessionManager sessionManager;
    private final AuditManager auditManager;
    private final PreviewSettings settings;
    
    // Memory management
    private final MemoryMXBean memoryBean;
    private final AtomicLong totalAllocatedMemory;
    private final Map<String, SecureMediaStream> activeStreams;
    private final ScheduledExecutorService cleanupScheduler;
    
    // Security and monitoring
    private volatile boolean isShutdown = false;
    private final Object memoryLock = new Object();
    
    public SecureMediaManager(SessionManager sessionManager,
                             AuditManager auditManager,
                             PreviewSettings settings) {
        // this.cryptoManager = cryptoManager; // Commented out - class not available
        this.sessionManager = sessionManager;
        this.auditManager = auditManager;
        this.settings = settings != null ? settings : new PreviewSettings();
        
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.totalAllocatedMemory = new AtomicLong(0);
        this.activeStreams = new ConcurrentHashMap<>();
        this.cleanupScheduler = Executors.newScheduledThreadPool(2);
        
        // Start periodic cleanup
        startPeriodicCleanup();
        
        // Start memory monitoring
        if (this.settings.isEnableMemoryMonitoring()) {
            startMemoryMonitoring();
        }
    }
    
    /**
     * Create secure stream for media playback without temporary files
     */
    public SecureMediaStream createSecureStream(VaultFile vaultFile, byte[] encryptedData) {
        if (isShutdown) {
            throw new IllegalStateException("SecureMediaManager has been shutdown");
        }
        
        if (sessionManager != null && !isSessionValid(sessionManager)) {
            auditManager.logSecurityEvent("SECURE_MEDIA_SESSION_INVALID", 
                "Attempt to create media stream with invalid session", 
                AuditManager.AuditSeverity.WARNING, 
                vaultFile.getFileId(), 
                "File: " + vaultFile.getOriginalName());
            throw new SecurityException("Session is not valid");
        }
        
        try {
            // Check memory limits before creating stream
            checkMemoryLimits(encryptedData.length);
            
            // Decrypt media data (mock implementation)
            byte[] decryptedData = encryptedData; // TODO: Implement actual decryption
            
            // Create secure stream
            SecureMediaStream stream = new SecureMediaStream(
                vaultFile.getFileId(),
                decryptedData,
                vaultFile.getOriginalName(),
                this
            );
            
            // Track the stream
            activeStreams.put(vaultFile.getFileId(), stream);
            totalAllocatedMemory.addAndGet(decryptedData.length);
            
            // Log creation
            if (settings.isLogPreviewActivity()) {
                auditManager.logSecurityEvent("SECURE_MEDIA_STREAM_CREATED", 
                    "Secure media stream created for preview", 
                    AuditManager.AuditSeverity.INFO, 
                    vaultFile.getFileId(), 
                    "File: " + vaultFile.getOriginalName() + ", Size: " + decryptedData.length);
            }
            
            return stream;
            
        } catch (Exception e) {
            auditManager.logSecurityEvent("SECURE_MEDIA_STREAM_FAILED", 
                "Failed to create secure media stream: " + e.getMessage(), 
                AuditManager.AuditSeverity.ERROR, 
                vaultFile.getFileId(), 
                "File: " + vaultFile.getOriginalName());
            throw new RuntimeException("Failed to create secure media stream", e);
        }
    }
    
    /**
     * Check memory limits before allocation
     */
    private void checkMemoryLimits(long additionalBytes) {
        synchronized (memoryLock) {
            long maxMemoryMB = settings.getMaxPreviewSizeMB();
            long maxMemoryBytes = maxMemoryMB * 1024 * 1024;
            
            long currentAllocated = totalAllocatedMemory.get();
            long projectedTotal = currentAllocated + additionalBytes;
            
            if (projectedTotal > maxMemoryBytes) {
                // Try to free some memory first
                performEmergencyCleanup();
                
                // Check again after cleanup
                currentAllocated = totalAllocatedMemory.get();
                projectedTotal = currentAllocated + additionalBytes;
                
                if (projectedTotal > maxMemoryBytes) {
                    throw new OutOfMemoryError(
                        String.format("Media memory limit exceeded. Current: %d MB, Requested: %d MB, Limit: %d MB",
                            currentAllocated / (1024 * 1024),
                            additionalBytes / (1024 * 1024),
                            maxMemoryMB)
                    );
                }
            }
        }
    }
    
    /**
     * Release secure stream and cleanup memory
     */
    public void releaseStream(String fileId) {
        SecureMediaStream stream = activeStreams.remove(fileId);
        if (stream != null) {
            long freedMemory = stream.cleanup();
            totalAllocatedMemory.addAndGet(-freedMemory);
            
            if (settings.isLogPreviewActivity()) {
                auditManager.logSecurityEvent("SECURE_MEDIA_STREAM_RELEASED", 
                    "Secure media stream released and cleaned up", 
                    AuditManager.AuditSeverity.INFO, 
                    fileId, 
                    "Freed memory: " + freedMemory + " bytes");
            }
        }
    }
    
    /**
     * Force cleanup of all streams
     */
    public void forceCleanup() {
        synchronized (memoryLock) {
            long totalFreed = 0;
            int streamCount = activeStreams.size();
            
            for (SecureMediaStream stream : activeStreams.values()) {
                totalFreed += stream.cleanup();
            }
            
            activeStreams.clear();
            totalAllocatedMemory.set(0);
            
            // Force garbage collection
            System.gc();
            
            auditManager.logSecurityEvent("SECURE_MEDIA_FORCE_CLEANUP", 
                "Forced cleanup of all media streams", 
                AuditManager.AuditSeverity.INFO, 
                null, 
                String.format("Streams cleaned: %d, Memory freed: %d bytes", streamCount, totalFreed));
        }
    }
    
    /**
     * Perform emergency cleanup to free memory
     */
    private void performEmergencyCleanup() {
        // Remove oldest streams first (simple LRU-like behavior)
        long targetFreeMemory = settings.getMaxPreviewSizeMB() * 1024 * 1024 / 4; // Free 25% of limit
        long freedMemory = 0;
        
        var streamIterator = activeStreams.entrySet().iterator();
        while (streamIterator.hasNext() && freedMemory < targetFreeMemory) {
            var entry = streamIterator.next();
            SecureMediaStream stream = entry.getValue();
            
            if (stream.canBeCleanedUp()) {
                freedMemory += stream.cleanup();
                streamIterator.remove();
                totalAllocatedMemory.addAndGet(-freedMemory);
            }
        }
        
        if (freedMemory > 0) {
            auditManager.logSecurityEvent("SECURE_MEDIA_EMERGENCY_CLEANUP", 
                "Emergency cleanup performed", 
                AuditManager.AuditSeverity.WARNING, 
                null, 
                "Memory freed: " + freedMemory + " bytes");
        }
    }
    
    /**
     * Start periodic cleanup of unused streams
     */
    private void startPeriodicCleanup() {
        int cleanupDelaySeconds = settings.getCleanupDelaySeconds();
        
        cleanupScheduler.scheduleWithFixedDelay(() -> {
            if (isShutdown) return;
            
            try {
                performPeriodicCleanup();
            } catch (Exception e) {
                auditManager.logSecurityEvent("SECURE_MEDIA_CLEANUP_ERROR", 
                    "Error during periodic cleanup: " + e.getMessage(), 
                    AuditManager.AuditSeverity.ERROR, 
                    null, 
                    "Cleanup error");
            }
        }, cleanupDelaySeconds, cleanupDelaySeconds, TimeUnit.SECONDS);
    }
    
    /**
     * Perform periodic cleanup of unused streams
     */
    private void performPeriodicCleanup() {
        long currentTime = System.currentTimeMillis();
        long maxIdleTime = settings.getCleanupDelaySeconds() * 1000L * 2; // 2x cleanup delay
        
        var streamIterator = activeStreams.entrySet().iterator();
        int cleanedCount = 0;
        long freedMemory = 0;
        
        while (streamIterator.hasNext()) {
            var entry = streamIterator.next();
            SecureMediaStream stream = entry.getValue();
            
            if (stream.getLastAccessTime() + maxIdleTime < currentTime && stream.canBeCleanedUp()) {
                freedMemory += stream.cleanup();
                streamIterator.remove();
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            totalAllocatedMemory.addAndGet(-freedMemory);
            
            if (settings.isLogPreviewActivity()) {
                auditManager.logSecurityEvent("SECURE_MEDIA_PERIODIC_CLEANUP", 
                    "Periodic cleanup completed", 
                    AuditManager.AuditSeverity.INFO, 
                    null, 
                    String.format("Streams cleaned: %d, Memory freed: %d bytes", cleanedCount, freedMemory));
            }
        }
    }
    
    /**
     * Start memory monitoring
     */
    private void startMemoryMonitoring() {
        cleanupScheduler.scheduleWithFixedDelay(() -> {
            if (isShutdown) return;
            
            try {
                monitorMemoryUsage();
            } catch (Exception e) {
                // Log but don't fail
                auditManager.logSecurityEvent("SECURE_MEDIA_MONITOR_ERROR", 
                    "Error during memory monitoring: " + e.getMessage(), 
                    AuditManager.AuditSeverity.WARNING, 
                    null, 
                    "Monitor error");
            }
        }, 30, 30, TimeUnit.SECONDS); // Monitor every 30 seconds
    }
    
    /**
     * Monitor memory usage and trigger cleanup if needed
     */
    private void monitorMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed();
        long maxMemory = heapUsage.getMax();
        
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        
        // If memory usage is high, perform cleanup
        if (usagePercentage > 80.0) { // 80% threshold
            auditManager.logSecurityEvent("SECURE_MEDIA_HIGH_MEMORY", 
                "High memory usage detected, performing cleanup", 
                AuditManager.AuditSeverity.WARNING, 
                null, 
                String.format("Memory usage: %.1f%% (%d/%d bytes)", usagePercentage, usedMemory, maxMemory));
            
            performEmergencyCleanup();
        }
    }
    
    /**
     * Get current memory statistics
     */
    public MemoryStatistics getMemoryStatistics() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        return new MemoryStatistics(
            activeStreams.size(),
            totalAllocatedMemory.get(),
            heapUsage.getUsed(),
            heapUsage.getMax(),
            settings.getMaxPreviewSizeMB() * 1024 * 1024
        );
    }
    
    /**
     * Check if session is valid (mock implementation)
     */
    private boolean isSessionValid(SessionManager sessionManager) {
        // TODO: Implement actual session validation
        return true; // Mock implementation
    }
    
    /**
     * Shutdown the secure media manager
     */
    public void shutdown() {
        isShutdown = true;
        
        // Cleanup all streams
        forceCleanup();
        
        // Shutdown scheduler
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        auditManager.logSecurityEvent("SECURE_MEDIA_MANAGER_SHUTDOWN", 
            "SecureMediaManager shutdown completed", 
            AuditManager.AuditSeverity.INFO, 
            null, 
            "Manager shutdown");
    }
    
    /**
     * Memory statistics data class
     */
    public static class MemoryStatistics {
        private final int activeStreams;
        private final long allocatedMemory;
        private final long heapUsed;
        private final long heapMax;
        private final long memoryLimit;
        
        public MemoryStatistics(int activeStreams, long allocatedMemory, 
                               long heapUsed, long heapMax, long memoryLimit) {
            this.activeStreams = activeStreams;
            this.allocatedMemory = allocatedMemory;
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.memoryLimit = memoryLimit;
        }
        
        public int getActiveStreams() { return activeStreams; }
        public long getAllocatedMemory() { return allocatedMemory; }
        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getMemoryLimit() { return memoryLimit; }
        
        public double getHeapUsagePercentage() {
            return heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0;
        }
        
        public double getAllocationPercentage() {
            return memoryLimit > 0 ? (double) allocatedMemory / memoryLimit * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "MemoryStatistics{streams=%d, allocated=%d bytes (%.1f%%), heap=%d/%d bytes (%.1f%%)}",
                activeStreams, allocatedMemory, getAllocationPercentage(),
                heapUsed, heapMax, getHeapUsagePercentage()
            );
        }
    }
    
    /**
     * Secure media stream for in-memory media handling
     */
    public static class SecureMediaStream {
        private final String fileId;
        private volatile byte[] data;
        private final String fileName;
        private final SecureMediaManager manager;
        private volatile long lastAccessTime;
        private volatile boolean isActive;
        
        public SecureMediaStream(String fileId, byte[] data, String fileName, SecureMediaManager manager) {
            this.fileId = fileId;
            this.data = data.clone(); // Defensive copy
            this.fileName = fileName;
            this.manager = manager;
            this.lastAccessTime = System.currentTimeMillis();
            this.isActive = true;
        }
        
        /**
         * Create input stream for media playback
         */
        public InputStream createInputStream() {
            if (!isActive || data == null) {
                throw new IllegalStateException("Stream has been cleaned up");
            }
            
            lastAccessTime = System.currentTimeMillis();
            return new ByteArrayInputStream(data);
        }
        
        /**
         * Get stream size
         */
        public long getSize() {
            return data != null ? data.length : 0;
        }
        
        /**
         * Get file ID
         */
        public String getFileId() {
            return fileId;
        }
        
        /**
         * Get file name
         */
        public String getFileName() {
            return fileName;
        }
        
        /**
         * Get last access time
         */
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        /**
         * Check if stream can be cleaned up
         */
        public boolean canBeCleanedUp() {
            // Stream can be cleaned up if it's not actively being used
            // This is a simple implementation - in practice, you might want to track active readers
            return !isActive || (System.currentTimeMillis() - lastAccessTime > 60000); // 1 minute idle
        }
        
        /**
         * Cleanup stream and return freed memory size
         */
        public long cleanup() {
            long freedSize = 0;
            if (data != null) {
                freedSize = data.length;
                // Securely clear the data
                Arrays.fill(data, (byte) 0);
                data = null;
            }
            
            isActive = false;
            return freedSize;
        }
        
        /**
         * Mark stream as inactive
         */
        public void markInactive() {
            isActive = false;
        }
    }
}