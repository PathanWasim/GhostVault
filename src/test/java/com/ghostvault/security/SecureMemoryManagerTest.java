package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Unit tests for SecureMemoryManager
 */
public class SecureMemoryManagerTest {
    
    private SecureMemoryManager memoryManager;
    
    @BeforeEach
    void setUp() {
        memoryManager = SecureMemoryManager.getInstance();
    }
    
    @Test
    @DisplayName("Should be singleton")
    void shouldBeSingleton() {
        SecureMemoryManager instance1 = SecureMemoryManager.getInstance();
        SecureMemoryManager instance2 = SecureMemoryManager.getInstance();
        
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should track byte arrays")
    void shouldTrackByteArrays() {
        SecureMemoryManager.MemoryStats initialStats = memoryManager.getStats();
        int initialArrays = initialStats.getTrackedArrays();
        
        byte[] testData = "sensitive data".getBytes();
        memoryManager.trackByteArray(testData);
        
        SecureMemoryManager.MemoryStats newStats = memoryManager.getStats();
        assertTrue(newStats.getTrackedArrays() >= initialArrays);
    }
    
    @Test
    @DisplayName("Should track secret keys")
    void shouldTrackSecretKeys() throws Exception {
        SecureMemoryManager.MemoryStats initialStats = memoryManager.getStats();
        int initialKeys = initialStats.getTrackedKeys();
        
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey testKey = keyGen.generateKey();
        
        memoryManager.trackSecretKey(testKey);
        
        SecureMemoryManager.MemoryStats newStats = memoryManager.getStats();
        assertTrue(newStats.getTrackedKeys() >= initialKeys);
    }
    
    @Test
    @DisplayName("Should securely wipe byte arrays")
    void shouldSecurelyWipeByteArrays() {
        byte[] sensitiveData = "very sensitive information".getBytes();
        byte[] originalCopy = sensitiveData.clone();
        
        memoryManager.secureWipe(sensitiveData);
        
        // Data should be different after wiping
        assertFalse(Arrays.equals(originalCopy, sensitiveData));
        
        // Should be all zeros after final pass
        boolean allZeros = true;
        for (byte b : sensitiveData) {
            if (b != 0) {
                allZeros = false;
                break;
            }
        }
        assertTrue(allZeros);
    }
    
    @Test
    @DisplayName("Should securely wipe char arrays")
    void shouldSecurelyWipeCharArrays() {
        char[] sensitiveChars = "password123".toCharArray();
        char[] originalCopy = sensitiveChars.clone();
        
        memoryManager.secureWipe(sensitiveChars);
        
        // Data should be different after wiping
        assertFalse(Arrays.equals(originalCopy, sensitiveChars));
        
        // Should be all null chars after final pass
        boolean allNulls = true;
        for (char c : sensitiveChars) {
            if (c != '\0') {
                allNulls = false;
                break;
            }
        }
        assertTrue(allNulls);
    }
    
    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            memoryManager.trackByteArray(null);
            memoryManager.trackSecretKey(null);
            memoryManager.secureWipe((byte[]) null);
            memoryManager.secureWipe((SecretKey) null);
            memoryManager.secureWipe((String) null);
            memoryManager.secureWipe((char[]) null);
        });
    }
    
    @Test
    @DisplayName("Should create secure copies with tracking")
    void shouldCreateSecureCopiesWithTracking() {
        byte[] original = "original data".getBytes();
        
        byte[] secureCopy = memoryManager.createSecureCopy(original);
        
        assertNotNull(secureCopy);
        assertArrayEquals(original, secureCopy);
        assertNotSame(original, secureCopy); // Should be different objects
        
        // Should handle null input
        assertNull(memoryManager.createSecureCopy(null));
    }
    
    @Test
    @DisplayName("Should create secure random bytes with tracking")
    void shouldCreateSecureRandomBytesWithTracking() {
        int length = 32;
        byte[] randomBytes = memoryManager.createSecureRandomBytes(length);
        
        assertNotNull(randomBytes);
        assertEquals(length, randomBytes.length);
        
        // Should be different each time
        byte[] randomBytes2 = memoryManager.createSecureRandomBytes(length);
        assertFalse(Arrays.equals(randomBytes, randomBytes2));
    }
    
    @Test
    @DisplayName("Should cleanup all tracked data")
    void shouldCleanupAllTrackedData() {
        // Track some data
        byte[] testData1 = "test data 1".getBytes();
        byte[] testData2 = "test data 2".getBytes();
        
        memoryManager.trackByteArray(testData1);
        memoryManager.trackByteArray(testData2);
        
        // Cleanup all tracked data
        memoryManager.cleanupAllTrackedData();
        
        // Data should be wiped
        boolean data1Wiped = Arrays.equals(testData1, new byte[testData1.length]);
        boolean data2Wiped = Arrays.equals(testData2, new byte[testData2.length]);
        
        assertTrue(data1Wiped);
        assertTrue(data2Wiped);
    }
    
    @Test
    @DisplayName("Should perform emergency cleanup")
    void shouldPerformEmergencyCleanup() {
        // Track some data
        byte[] emergencyData = "emergency sensitive data".getBytes();
        memoryManager.trackByteArray(emergencyData);
        
        // Perform emergency cleanup
        assertDoesNotThrow(() -> {
            memoryManager.emergencyCleanup();
        });
        
        // Data should be wiped
        boolean dataWiped = Arrays.equals(emergencyData, new byte[emergencyData.length]);
        assertTrue(dataWiped);
    }
    
    @Test
    @DisplayName("Should provide memory statistics")
    void shouldProvideMemoryStatistics() {
        SecureMemoryManager.MemoryStats stats = memoryManager.getStats();
        
        assertNotNull(stats);
        assertTrue(stats.getTrackedArrays() >= 0);
        assertTrue(stats.getTrackedKeys() >= 0);
        assertTrue(stats.getTotalTracked() >= 0);
        assertEquals(stats.getTrackedArrays() + stats.getTrackedKeys(), stats.getTotalTracked());
        
        // Test toString
        assertNotNull(stats.toString());
        assertTrue(stats.toString().contains("arrays"));
        assertTrue(stats.toString().contains("keys"));
    }
    
    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccessSafely() throws InterruptedException {
        // Test basic thread safety
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                byte[] data = ("thread1-data-" + i).getBytes();
                memoryManager.trackByteArray(data);
                memoryManager.secureWipe(data);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                byte[] data = ("thread2-data-" + i).getBytes();
                memoryManager.trackByteArray(data);
                memoryManager.secureWipe(data);
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            memoryManager.getStats();
            memoryManager.cleanupAllTrackedData();
        });
    }
    
    @Test
    @DisplayName("Should handle garbage collected references")
    void shouldHandleGarbageCollectedReferences() {
        // Create some data that will be garbage collected
        for (int i = 0; i < 10; i++) {
            byte[] tempData = ("temp-data-" + i).getBytes();
            memoryManager.trackByteArray(tempData);
        }
        
        // Force garbage collection
        System.gc();
        
        // Get stats (this should clean up garbage collected references)
        SecureMemoryManager.MemoryStats stats = memoryManager.getStats();
        
        // Should not throw exceptions
        assertNotNull(stats);
    }
}