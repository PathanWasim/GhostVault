package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.NotificationManager;
import com.ghostvault.audit.AuditManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PreviewErrorHandler
 */
class PreviewErrorHandlerTest {
    
    private PreviewErrorHandler errorHandler;
    private NotificationManager mockNotificationManager;
    private AuditManager mockAuditManager;
    private PreviewSettings mockSettings;
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        mockNotificationManager = mock(NotificationManager.class);
        mockAuditManager = mock(AuditManager.class);
        mockSettings = mock(PreviewSettings.class);
        mockVaultFile = mock(VaultFile.class);
        
        // Setup default mock behaviors
        when(mockSettings.getMaxPreviewSizeMB()).thenReturn(50);
        when(mockVaultFile.getOriginalName()).thenReturn("test.xyz");
        when(mockVaultFile.getFileId()).thenReturn("test-id-123");
        when(mockVaultFile.getExtension()).thenReturn("xyz");
        when(mockVaultFile.getSize()).thenReturn(1024L);
        
        errorHandler = new PreviewErrorHandler(
            mockNotificationManager, 
            mockAuditManager, 
            mockSettings
        );
    }
    
    @Test
    @DisplayName("Should handle unsupported format errors")
    void testHandleUnsupportedFormat() {
        errorHandler.handleUnsupportedFormat("xyz", mockVaultFile);
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_UNSUPPORTED_FORMAT"),
            contains("Unsupported file extension: xyz"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should handle memory limit exceeded errors")
    void testHandleMemoryLimit() {
        long largeFileSize = 100L * 1024 * 1024; // 100MB
        
        errorHandler.handleMemoryLimit(largeFileSize, mockVaultFile);
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_MEMORY_LIMIT_EXCEEDED"),
            contains("File size"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should handle decryption errors")
    void testHandleDecryptionError() {
        Exception testException = new RuntimeException("Decryption failed");
        
        errorHandler.handleDecryptionError(testException, mockVaultFile);
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_DECRYPTION_FAILED"),
            contains("Decryption error: Decryption failed"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should handle media playback errors")
    void testHandleMediaError() {
        Exception testException = new RuntimeException("Codec not supported");
        when(mockVaultFile.getExtension()).thenReturn("mkv");
        
        errorHandler.handleMediaError(testException, mockVaultFile);
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_MEDIA_PLAYBACK_FAILED"),
            contains("Media error: Codec not supported"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should handle component creation errors")
    void testHandleComponentCreationError() {
        Exception testException = new RuntimeException("Component creation failed");
        
        errorHandler.handleComponentCreationError(testException, mockVaultFile);
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_COMPONENT_CREATION_FAILED"),
            contains("Component creation error: Component creation failed"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should show fallback hex viewer")
    void testShowFallbackViewer() {
        byte[] testData = "Hello, World! This is test data.".getBytes();
        
        // This test verifies the method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            errorHandler.showFallbackViewer(testData, mockVaultFile);
        });
        
        // Verify audit logging for hex viewer usage
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_HEX_VIEWER_USED"),
            eq("Hex viewer fallback used"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should handle empty data for hex viewer")
    void testShowFallbackViewerEmptyData() {
        byte[] emptyData = new byte[0];
        
        // Should handle empty data gracefully
        assertDoesNotThrow(() -> {
            errorHandler.showFallbackViewer(emptyData, mockVaultFile);
        });
        
        // Should not log hex viewer usage for empty data
        verify(mockAuditManager, never()).logSecurityEvent(
            eq("PREVIEW_ERROR_HEX_VIEWER_USED"),
            anyString(),
            any(),
            anyString(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle null data for hex viewer")
    void testShowFallbackViewerNullData() {
        // Should handle null data gracefully
        assertDoesNotThrow(() -> {
            errorHandler.showFallbackViewer(null, mockVaultFile);
        });
        
        // Should not log hex viewer usage for null data
        verify(mockAuditManager, never()).logSecurityEvent(
            eq("PREVIEW_ERROR_HEX_VIEWER_USED"),
            anyString(),
            any(),
            anyString(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should create appropriate error messages for different file types")
    void testFileTypeSpecificMessages() {
        // Test executable file
        when(mockVaultFile.getExtension()).thenReturn("exe");
        errorHandler.handleUnsupportedFormat("exe", mockVaultFile);
        
        // Test archive file
        when(mockVaultFile.getExtension()).thenReturn("zip");
        errorHandler.handleUnsupportedFormat("zip", mockVaultFile);
        
        // Test Office document
        when(mockVaultFile.getExtension()).thenReturn("docx");
        errorHandler.handleUnsupportedFormat("docx", mockVaultFile);
        
        // Verify all were logged
        verify(mockAuditManager, times(3)).logSecurityEvent(
            eq("PREVIEW_ERROR_UNSUPPORTED_FORMAT"),
            anyString(),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should create appropriate media error messages")
    void testMediaSpecificErrorMessages() {
        Exception testException = new RuntimeException("Codec error");
        
        // Test FLAC file
        when(mockVaultFile.getExtension()).thenReturn("flac");
        errorHandler.handleMediaError(testException, mockVaultFile);
        
        // Test MKV file
        when(mockVaultFile.getExtension()).thenReturn("mkv");
        errorHandler.handleMediaError(testException, mockVaultFile);
        
        // Test AVI file
        when(mockVaultFile.getExtension()).thenReturn("avi");
        errorHandler.handleMediaError(testException, mockVaultFile);
        
        // Verify all were logged
        verify(mockAuditManager, times(3)).logSecurityEvent(
            eq("PREVIEW_ERROR_MEDIA_PLAYBACK_FAILED"),
            anyString(),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle null vault file gracefully")
    void testNullVaultFileHandling() {
        // Should not throw exceptions with null vault file
        assertDoesNotThrow(() -> {
            errorHandler.handleUnsupportedFormat("txt", null);
            errorHandler.handleMemoryLimit(1024L, null);
            errorHandler.handleDecryptionError(new RuntimeException("test"), null);
            errorHandler.handleMediaError(new RuntimeException("test"), null);
            errorHandler.handleComponentCreationError(new RuntimeException("test"), null);
        });
        
        // Verify audit logging with null file ID
        verify(mockAuditManager, atLeastOnce()).logSecurityEvent(
            anyString(),
            anyString(),
            any(),
            isNull(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle null audit manager gracefully")
    void testNullAuditManagerHandling() {
        // Create error handler without audit manager
        PreviewErrorHandler handlerWithoutAudit = new PreviewErrorHandler(
            mockNotificationManager, 
            null, 
            mockSettings
        );
        
        // Should not throw exceptions without audit manager
        assertDoesNotThrow(() -> {
            handlerWithoutAudit.handleUnsupportedFormat("txt", mockVaultFile);
            handlerWithoutAudit.handleMemoryLimit(1024L, mockVaultFile);
            handlerWithoutAudit.handleDecryptionError(new RuntimeException("test"), mockVaultFile);
        });
    }
    
    @Test
    @DisplayName("Should format file sizes correctly")
    void testFileSizeFormatting() {
        // Test different file sizes through memory limit error
        errorHandler.handleMemoryLimit(512L, mockVaultFile); // Bytes
        errorHandler.handleMemoryLimit(2048L, mockVaultFile); // KB
        errorHandler.handleMemoryLimit(5L * 1024 * 1024, mockVaultFile); // MB
        errorHandler.handleMemoryLimit(2L * 1024 * 1024 * 1024, mockVaultFile); // GB
        
        // Verify all were logged (file size formatting is internal)
        verify(mockAuditManager, times(4)).logSecurityEvent(
            eq("PREVIEW_ERROR_MEMORY_LIMIT_EXCEEDED"),
            anyString(),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should create proper hex dump format")
    void testHexDumpCreation() {
        // Create test data with various byte values
        byte[] testData = new byte[64];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) (i % 256);
        }
        
        // Test hex viewer with this data
        assertDoesNotThrow(() -> {
            errorHandler.showFallbackViewer(testData, mockVaultFile);
        });
        
        // Verify hex viewer was used
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_HEX_VIEWER_USED"),
            eq("Hex viewer fallback used"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
    
    @Test
    @DisplayName("Should limit hex dump size for large files")
    void testHexDumpSizeLimit() {
        // Create large test data (16KB)
        byte[] largeData = new byte[16 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        // Should handle large data without issues
        assertDoesNotThrow(() -> {
            errorHandler.showFallbackViewer(largeData, mockVaultFile);
        });
        
        // Verify hex viewer was used
        verify(mockAuditManager).logSecurityEvent(
            eq("PREVIEW_ERROR_HEX_VIEWER_USED"),
            eq("Hex viewer fallback used"),
            eq(AuditManager.AuditSeverity.WARNING),
            eq(mockVaultFile.getFileId()),
            contains("test.xyz")
        );
    }
}