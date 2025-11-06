package com.ghostvault.ui.preview;

import com.ghostvault.core.CryptoManager;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.SessionManager;
import com.ghostvault.audit.AuditManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;

/**
 * Security tests for SecureMediaManager
 */
class SecureMediaManagerTest {
    
    private SecureMediaManager mediaManager;
    private CryptoManager mockCryptoManager;
    private SessionManager mockSessionManager;
    private AuditManager mockAuditManager;
    private PreviewSettings mockSettings;
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        mockCryptoManager = mock(CryptoManager.class);
        mockSessionManager = mock(SessionManager.class);
        mockAuditManager = mock(AuditManager.class);
        mockSettings = mock(PreviewSettings.class);
        mockVaultFile = mock(VaultFile.class);
        
        // Setup default mock behaviors
        when(mockSessionManager.isSessionValid()).thenReturn(true);
        when(mockSettings.getMaxPreviewSizeMB()).thenReturn(50);
        when(mockSettings.getCleanupDelaySeconds()).thenReturn(30);
        when(mockSettings.isEnableMemoryMonitoring()).thenReturn(true);
        when(mockSettings.isLogPreviewActivity()).thenReturn(true);
        
        when(mockVaultFile.getFileId()).thenReturn("test-file-123");
        when(mockVaultFile.getOriginalName()).thenReturn("test.mp3");
        
        mediaManager = new SecureMediaManager(
            mockCryptoManager, 
            mockSessionManager, 
            mockAuditManager, 
            mockSettings
        );
    }
    
    @AfterEach
    void tearDown() {
        if (mediaManager != null) {
            mediaManager.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should create secure stream successfully")
    void testCreateSecureStream() throws Exception {
        byte[] encryptedData = "encrypted_media_data".getBytes();
        byte[] decryptedData = "decrypted_media_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        
        assertNotNull(stream);
        assertEquals("test-file-123", stream.getFileId());
        assertEquals("test.mp3", stream.getFileName());
        assertEquals(decryptedData.length, stream.getSize());
        
        verify(mockCryptoManager).decrypt(encryptedData);
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_STREAM_CREATED"),
            anyString(),
            eq(AuditManager.AuditSeverity.INFO),
            eq("test-file-123"),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should reject stream creation with invalid session")
    void testCreateStreamInvalidSession() {
        when(mockSessionManager.isSessionValid()).thenReturn(false);
        
        byte[] encryptedData = "encrypted_data".getBytes();
        
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            mediaManager.createSecureStream(mockVaultFile, encryptedData);
        });
        
        assertEquals("Session is not valid", exception.getMessage());
        
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_SESSION_INVALID"),
            anyString(),
            eq(AuditManager.AuditSeverity.WARNING),
            eq("test-file-123"),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should enforce memory limits")
    void testMemoryLimits() throws Exception {
        // Set low memory limit
        when(mockSettings.getMaxPreviewSizeMB()).thenReturn(1); // 1MB limit
        
        // Create large data that exceeds limit
        byte[] largeEncryptedData = new byte[2 * 1024 * 1024]; // 2MB
        byte[] largeDecryptedData = new byte[2 * 1024 * 1024]; // 2MB
        
        when(mockCryptoManager.decrypt(largeEncryptedData)).thenReturn(largeDecryptedData);
        
        assertThrows(OutOfMemoryError.class, () -> {
            mediaManager.createSecureStream(mockVaultFile, largeEncryptedData);
        });
    }
    
    @Test
    @DisplayName("Should release stream and cleanup memory")
    void testReleaseStream() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        assertNotNull(stream);
        
        // Release the stream
        mediaManager.releaseStream("test-file-123");
        
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_STREAM_RELEASED"),
            anyString(),
            eq(AuditManager.AuditSeverity.INFO),
            eq("test-file-123"),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should perform force cleanup")
    void testForceCleanup() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        // Create multiple streams
        SecureMediaManager.SecureMediaStream stream1 = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        
        VaultFile mockVaultFile2 = mock(VaultFile.class);
        when(mockVaultFile2.getFileId()).thenReturn("test-file-456");
        when(mockVaultFile2.getOriginalName()).thenReturn("test2.mp3");
        
        SecureMediaManager.SecureMediaStream stream2 = mediaManager.createSecureStream(mockVaultFile2, encryptedData);
        
        // Force cleanup
        mediaManager.forceCleanup();
        
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_FORCE_CLEANUP"),
            anyString(),
            eq(AuditManager.AuditSeverity.INFO),
            isNull(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should get memory statistics")
    void testGetMemoryStatistics() {
        SecureMediaManager.MemoryStatistics stats = mediaManager.getMemoryStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.getActiveStreams() >= 0);
        assertTrue(stats.getAllocatedMemory() >= 0);
        assertTrue(stats.getHeapUsed() >= 0);
        assertTrue(stats.getHeapMax() > 0);
        assertTrue(stats.getMemoryLimit() > 0);
        
        // Test percentage calculations
        assertTrue(stats.getHeapUsagePercentage() >= 0);
        assertTrue(stats.getAllocationPercentage() >= 0);
        
        // Test toString
        String statsString = stats.toString();
        assertNotNull(statsString);
        assertTrue(statsString.contains("MemoryStatistics"));
    }
    
    @Test
    @DisplayName("Should handle shutdown gracefully")
    void testShutdown() {
        mediaManager.shutdown();
        
        // Should not be able to create streams after shutdown
        assertThrows(IllegalStateException.class, () -> {
            mediaManager.createSecureStream(mockVaultFile, "data".getBytes());
        });
        
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_MANAGER_SHUTDOWN"),
            anyString(),
            eq(AuditManager.AuditSeverity.INFO),
            isNull(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle decryption errors")
    void testDecryptionError() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenThrow(new RuntimeException("Decryption failed"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mediaManager.createSecureStream(mockVaultFile, encryptedData);
        });
        
        assertEquals("Failed to create secure media stream", exception.getMessage());
        
        verify(mockAuditManager).logSecurityEvent(
            eq("SECURE_MEDIA_STREAM_FAILED"),
            anyString(),
            eq(AuditManager.AuditSeverity.ERROR),
            eq("test-file-123"),
            anyString()
        );
    }
    
    @Test
    @DisplayName("SecureMediaStream should create input stream")
    void testSecureMediaStreamInputStream() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        
        InputStream inputStream = stream.createInputStream();
        assertNotNull(inputStream);
        
        // Read data from stream
        byte[] readData = inputStream.readAllBytes();
        assertArrayEquals(decryptedData, readData);
    }
    
    @Test
    @DisplayName("SecureMediaStream should handle cleanup")
    void testSecureMediaStreamCleanup() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        
        assertTrue(stream.getSize() > 0);
        assertTrue(stream.canBeCleanedUp());
        
        long freedMemory = stream.cleanup();
        assertEquals(decryptedData.length, freedMemory);
        assertEquals(0, stream.getSize());
        
        // Should not be able to create input stream after cleanup
        assertThrows(IllegalStateException.class, () -> {
            stream.createInputStream();
        });
    }
    
    @Test
    @DisplayName("Should handle null settings gracefully")
    void testNullSettings() {
        SecureMediaManager managerWithNullSettings = new SecureMediaManager(
            mockCryptoManager, 
            mockSessionManager, 
            mockAuditManager, 
            null
        );
        
        // Should create default settings internally
        assertNotNull(managerWithNullSettings.getMemoryStatistics());
        
        managerWithNullSettings.shutdown();
    }
    
    @Test
    @DisplayName("Should track last access time")
    void testLastAccessTime() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(mockVaultFile, encryptedData);
        
        long initialAccessTime = stream.getLastAccessTime();
        assertTrue(initialAccessTime > 0);
        
        // Wait a bit and access stream
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stream.createInputStream();
        long newAccessTime = stream.getLastAccessTime();
        
        assertTrue(newAccessTime >= initialAccessTime);
    }
    
    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() throws Exception {
        byte[] encryptedData = "encrypted_data".getBytes();
        byte[] decryptedData = "decrypted_data".getBytes();
        
        when(mockCryptoManager.decrypt(encryptedData)).thenReturn(decryptedData);
        
        // Create multiple streams concurrently
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    VaultFile file = mock(VaultFile.class);
                    when(file.getFileId()).thenReturn("file-" + index);
                    when(file.getOriginalName()).thenReturn("test" + index + ".mp3");
                    
                    SecureMediaManager.SecureMediaStream stream = mediaManager.createSecureStream(file, encryptedData);
                    assertNotNull(stream);
                } catch (Exception e) {
                    fail("Concurrent access failed: " + e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout
        }
        
        // Verify statistics
        SecureMediaManager.MemoryStatistics stats = mediaManager.getMemoryStatistics();
        assertEquals(5, stats.getActiveStreams());
    }
}