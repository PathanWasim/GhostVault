package com.ghostvault.security;

import javax.crypto.SecretKey;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Secure memory manager for tracking and cleaning up sensitive data
 * Provides automatic cleanup of encryption keys and sensitive byte arrays
 */
public class SecureMemoryManager {
    
    private static final SecureMemoryManager INSTANCE = new SecureMemoryManager();
    
    private final SecureRandom secureRandom;
    private final Set<WeakReference<byte[]>> trackedByteArrays;
    private final Set<WeakReference<SecretKey>> trackedKeys;
    private final ScheduledExecutorService cleanupScheduler;
    private final CryptoManager cryptoManager;
    
    // Cleanup configuration
    private static final int CLEANUP_INTERVAL_SECONDS = 30;
    private static final int WIPE_PASSES = SecurityConfiguration.SECURE_WIPE_PASSES;
    
    private SecureMemoryManager() {
        this.secureRandom = new SecureRandom();
        this.trackedByteArrays = ConcurrentHashMap.newKeySet();
        this.trackedKeys = ConcurrentHashMap.newKeySet();
        this.cryptoManager = new CryptoManager();
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SecureMemoryCleanup");
            t.setDaemon(true);
            return t;
        });
        
        // Start periodic cleanup
        startPeriodicCleanup();
        
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public static SecureMemoryManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Track a byte array for secure cleanup
     * @param data The byte array to track
     */
    public void trackByteArray(byte[] data) {
        if (data != null) {
            trackedByteArrays.add(new WeakReference<>(data));
        }
    }
    
    /**
     * Track a SecretKey for secure cleanup
     * @param key The key to track
     */
    public void trackSecretKey(SecretKey key) {
        if (key != null) {
            trackedKeys.add(new WeakReference<>(key));
        }
    }
    
    /**
     * Securely wipe a byte array immediately
     * @param data The data to wipe
     */
    public void secureWipe(byte[] data) {
        if (data != null) {
            for (int pass = 0; pass < WIPE_PASSES; pass++) {
                // Fill with random data
                secureRandom.nextBytes(data);
            }
            // Final pass with zeros
            Arrays.fill(data, (byte) 0);
        }
    }
    
    /**
     * Securely wipe a SecretKey immediately (best effort)
     * @param key The key to wipe
     */
    public void secureWipe(SecretKey key) {
        cryptoManager.secureWipe(key);
    }
    
    /**
     * Securely wipe a string by converting to char array and wiping
     * @param str The string to wipe (best effort)
     */
    public void secureWipe(String str) {
        if (str != null) {
            try {
                // Get char array and wipe it
                char[] chars = str.toCharArray();
                secureWipe(chars);
            } catch (Exception e) {
                // Best effort - strings are immutable in Java
                System.err.println("⚠️ Cannot securely wipe string: " + e.getMessage());
            }
        }
    }
    
    /**
     * Securely wipe a char array
     * @param chars The char array to wipe
     */
    public void secureWipe(char[] chars) {
        if (chars != null) {
            for (int pass = 0; pass < WIPE_PASSES; pass++) {
                // Fill with random characters
                for (int i = 0; i < chars.length; i++) {
                    chars[i] = (char) secureRandom.nextInt(65536);
                }
            }
            // Final pass with zeros
            Arrays.fill(chars, '\0');
        }
    }
    
    /**
     * Clean up all tracked sensitive data
     */
    public void cleanupAllTrackedData() {
        System.out.println("🧹 Starting comprehensive memory cleanup...");
        
        int wipedArrays = 0;
        int wipedKeys = 0;
        
        // Clean up tracked byte arrays
        var arrayIterator = trackedByteArrays.iterator();
        while (arrayIterator.hasNext()) {
            WeakReference<byte[]> ref = arrayIterator.next();
            byte[] data = ref.get();
            if (data == null) {
                // Object was garbage collected
                arrayIterator.remove();
            } else {
                secureWipe(data);
                wipedArrays++;
                arrayIterator.remove();
            }
        }
        
        // Clean up tracked keys
        var keyIterator = trackedKeys.iterator();
        while (keyIterator.hasNext()) {
            WeakReference<SecretKey> ref = keyIterator.next();
            SecretKey key = ref.get();
            if (key == null) {
                // Object was garbage collected
                keyIterator.remove();
            } else {
                secureWipe(key);
                wipedKeys++;
                keyIterator.remove();
            }
        }
        
        // Force garbage collection to help clear references
        System.gc();
        
        System.out.println("✅ Memory cleanup complete: " + wipedArrays + " arrays, " + wipedKeys + " keys wiped");
    }
    
    /**
     * Clean up garbage collected references
     */
    private void cleanupGarbageCollectedReferences() {
        // Remove weak references to garbage collected objects
        trackedByteArrays.removeIf(ref -> ref.get() == null);
        trackedKeys.removeIf(ref -> ref.get() == null);
    }
    
    /**
     * Start periodic cleanup of garbage collected references
     */
    private void startPeriodicCleanup() {
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupGarbageCollectedReferences,
            CLEANUP_INTERVAL_SECONDS,
            CLEANUP_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Emergency cleanup for panic mode
     */
    public void emergencyCleanup() {
        System.out.println("🚨 EMERGENCY MEMORY CLEANUP - PANIC MODE");
        
        // Aggressively clean all tracked data
        cleanupAllTrackedData();
        
        // Multiple garbage collection passes
        for (int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("🚨 Emergency cleanup completed");
    }
    
    /**
     * Get memory management statistics
     * @return MemoryStats with current state
     */
    public MemoryStats getStats() {
        cleanupGarbageCollectedReferences();
        return new MemoryStats(trackedByteArrays.size(), trackedKeys.size());
    }
    
    /**
     * Shutdown the secure memory manager
     */
    public void shutdown() {
        System.out.println("🔒 Shutting down SecureMemoryManager...");
        
        // Clean up all tracked data
        cleanupAllTrackedData();
        
        // Shutdown cleanup scheduler
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ SecureMemoryManager shutdown complete");
    }
    
    /**
     * Create a secure copy of byte array with tracking
     * @param original The original array
     * @return A secure copy that will be tracked for cleanup
     */
    public byte[] createSecureCopy(byte[] original) {
        if (original == null) {
            return null;
        }
        
        byte[] copy = original.clone();
        trackByteArray(copy);
        return copy;
    }
    
    /**
     * Create secure random bytes with tracking
     * @param length The number of bytes to generate
     * @return Random bytes that will be tracked for cleanup
     */
    public byte[] createSecureRandomBytes(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        trackByteArray(randomBytes);
        return randomBytes;
    }
    
    /**
     * Statistics about memory management
     */
    public static class MemoryStats {
        private final int trackedArrays;
        private final int trackedKeys;
        
        public MemoryStats(int trackedArrays, int trackedKeys) {
            this.trackedArrays = trackedArrays;
            this.trackedKeys = trackedKeys;
        }
        
        public int getTrackedArrays() { return trackedArrays; }
        public int getTrackedKeys() { return trackedKeys; }
        public int getTotalTracked() { return trackedArrays + trackedKeys; }
        
        @Override
        public String toString() {
            return String.format("MemoryStats{arrays=%d, keys=%d, total=%d}",
                               trackedArrays, trackedKeys, getTotalTracked());
        }
    }
}