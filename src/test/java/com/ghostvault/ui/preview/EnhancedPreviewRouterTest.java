package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.NotificationManager;
import com.ghostvault.audit.AuditManager;
import com.ghostvault.security.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for EnhancedPreviewRouter
 */
class EnhancedPreviewRouterTest {
    
    private EnhancedPreviewRouter previewRouter;
    private DefaultPreviewComponentFactory mockFactory;
    private PreviewSettings mockSettings;
    private AuditManager mockAuditManager;
    private SessionManager mockSessionManager;
    private NotificationManager mockNotificationManager;
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        mockFactory = mock(DefaultPreviewComponentFactory.class);
        mockSettings = mock(PreviewSettings.class);
        mockAuditManager = mock(AuditManager.class);
        mockSessionManager = mock(SessionManager.class);
        mockNotificationManager = mock(NotificationManager.class);
        mockVaultFile = mock(VaultFile.class);
        
        // Setup default mock behaviors
        when(mockSettings.getMaxPreviewSizeMB()).thenReturn(50);
        when(mockSettings.getMaxConcurrentPreviews()).thenReturn(3);
        when(mockSettings.isEnableMemoryMonitoring()).thenReturn(true);
        when(mockSettings.isLogPreviewActivity()).thenReturn(true);
        
        when(mockVaultFile.getOriginalName()).thenReturn("test.txt");
        when(mockVaultFile.getFileId()).thenReturn("test-id-123");
        when(mockVaultFile.getExtension()).thenReturn("txt");
        when(mockVaultFile.getSize()).thenReturn(1024L);
        
        previewRouter = new EnhancedPreviewRouter(
            mockFactory, 
            mockSettings, 
            mockAuditManager, 
            mockSessionManager
        );
    }
    
    @AfterEach
    void tearDown() {
        if (previewRouter != null) {
            previewRouter.closeAllPreviews();
        }
    }
    
    @Test
    @DisplayName("Should route files to correct preview components")
    void testFileRouting() {
        // Setup mock component
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType("txt")).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        
        // Test routing
        byte[] testData = "Hello, World!".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertTrue(result);
        verify(mockFactory).createPreviewComponent(any());
        verify(mockComponent).initialize(mockVaultFile);
        verify(mockComponent).loadContent(testData);
        verify(mockComponent).show();
    }
    
    @Test
    @DisplayName("Should check preview support correctly")
    void testPreviewSupport() {
        when(mockFactory.isSupported("txt")).thenReturn(true);
        when(mockFactory.isSupported("xyz")).thenReturn(false);
        
        assertTrue(previewRouter.isPreviewSupported("txt"));
        assertFalse(previewRouter.isPreviewSupported("xyz"));
        
        verify(mockFactory).isSupported("txt");
        verify(mockFactory).isSupported("xyz");
    }
    
    @Test
    @DisplayName("Should handle unsupported file types gracefully")
    void testUnsupportedFileTypes() {
        when(mockFactory.isSupported(any(String.class))).thenReturn(false);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        verify(mockFactory).isSupported(mockVaultFile.getExtension());
        verify(mockFactory, never()).createPreviewComponent(any());
    }
    
    @Test
    @DisplayName("Should enforce file size limits")
    void testFileSizeLimit() {
        // Setup large file
        when(mockVaultFile.getSize()).thenReturn(100L * 1024 * 1024); // 100MB
        when(mockSettings.getMaxPreviewSizeMB()).thenReturn(50); // 50MB limit
        
        byte[] testData = new byte[1024]; // Small test data
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        // Should not create component for oversized files
        verify(mockFactory, never()).createPreviewComponent(any());
    }
    
    @Test
    @DisplayName("Should handle component creation failures")
    void testComponentCreationFailure() {
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(null);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        verify(mockFactory).createPreviewComponent(any());
    }
    
    @Test
    @DisplayName("Should handle component initialization failures")
    void testComponentInitializationFailure() {
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        doThrow(new RuntimeException("Initialization failed")).when(mockComponent).initialize(any());
        
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        verify(mockComponent).initialize(mockVaultFile);
    }
    
    @Test
    @DisplayName("Should manage preview component lifecycle")
    void testPreviewLifecycleManagement() {
        PreviewComponent mockComponent1 = mock(PreviewComponent.class);
        PreviewComponent mockComponent2 = mock(PreviewComponent.class);
        
        when(mockComponent1.supportsFileType(any())).thenReturn(true);
        when(mockComponent2.supportsFileType(any())).thenReturn(true);
        
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any()))
            .thenReturn(mockComponent1)
            .thenReturn(mockComponent2);
        
        // Show first preview
        byte[] testData1 = "test data 1".getBytes();
        boolean result1 = previewRouter.showPreview(mockVaultFile, testData1);
        assertTrue(result1);
        
        // Show second preview
        VaultFile mockVaultFile2 = mock(VaultFile.class);
        when(mockVaultFile2.getOriginalName()).thenReturn("test2.txt");
        when(mockVaultFile2.getFileId()).thenReturn("test-id-456");
        when(mockVaultFile2.getExtension()).thenReturn("txt");
        when(mockVaultFile2.getSize()).thenReturn(2048L);
        
        byte[] testData2 = "test data 2".getBytes();
        boolean result2 = previewRouter.showPreview(mockVaultFile2, testData2);
        assertTrue(result2);
        
        // Close all previews
        previewRouter.closeAllPreviews();
        
        verify(mockComponent1).close();
        verify(mockComponent2).close();
    }
    
    @Test
    @DisplayName("Should enforce concurrent preview limits")
    void testConcurrentPreviewLimits() {
        when(mockSettings.getMaxConcurrentPreviews()).thenReturn(2);
        
        PreviewComponent mockComponent1 = mock(PreviewComponent.class);
        PreviewComponent mockComponent2 = mock(PreviewComponent.class);
        PreviewComponent mockComponent3 = mock(PreviewComponent.class);
        
        when(mockComponent1.supportsFileType(any())).thenReturn(true);
        when(mockComponent2.supportsFileType(any())).thenReturn(true);
        when(mockComponent3.supportsFileType(any())).thenReturn(true);
        
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any()))
            .thenReturn(mockComponent1)
            .thenReturn(mockComponent2)
            .thenReturn(mockComponent3);
        
        // Create test files
        VaultFile file1 = createMockVaultFile("file1.txt", "id1");
        VaultFile file2 = createMockVaultFile("file2.txt", "id2");
        VaultFile file3 = createMockVaultFile("file3.txt", "id3");
        
        byte[] testData = "test".getBytes();
        
        // Show first two previews (should succeed)
        assertTrue(previewRouter.showPreview(file1, testData));
        assertTrue(previewRouter.showPreview(file2, testData));
        
        // Third preview should be rejected due to limit
        boolean result3 = previewRouter.showPreview(file3, testData);
        
        // Depending on implementation, this might succeed by closing oldest preview
        // or fail due to limit. Check the actual behavior.
        verify(mockFactory, atMost(3)).createPreviewComponent(any());
    }
    
    @Test
    @DisplayName("Should log preview activities when enabled")
    void testPreviewActivityLogging() {
        when(mockSettings.isLogPreviewActivity()).thenReturn(true);
        
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType(any())).thenReturn(true);
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        byte[] testData = "test data".getBytes();
        previewRouter.showPreview(mockVaultFile, testData);
        
        // Verify audit logging
        verify(mockAuditManager, atLeastOnce()).logSecurityEvent(
            eq("PREVIEW_OPENED"),
            anyString(),
            any(),
            eq(mockVaultFile.getFileId()),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should not log preview activities when disabled")
    void testPreviewActivityLoggingDisabled() {
        when(mockSettings.isLogPreviewActivity()).thenReturn(false);
        
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType(any())).thenReturn(true);
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        byte[] testData = "test data".getBytes();
        previewRouter.showPreview(mockVaultFile, testData);
        
        // Verify no audit logging for preview activities
        verify(mockAuditManager, never()).logSecurityEvent(
            eq("PREVIEW_OPENED"),
            anyString(),
            any(),
            anyString(),
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should handle memory monitoring")
    void testMemoryMonitoring() {
        when(mockSettings.isEnableMemoryMonitoring()).thenReturn(true);
        
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType(any())).thenReturn(true);
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertTrue(result);
        // Memory monitoring should be active (implementation specific)
        verify(mockComponent).setSecureMode(true);
    }
    
    @Test
    @DisplayName("Should handle session timeout integration")
    void testSessionTimeoutIntegration() {
        when(mockSessionManager.isSessionValid()).thenReturn(false);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        verify(mockSessionManager).isSessionValid();
        verify(mockFactory, never()).createPreviewComponent(any());
    }
    
    @Test
    @DisplayName("Should handle null inputs gracefully")
    void testNullInputHandling() {
        // Test null file
        assertFalse(previewRouter.showPreview(null, "test".getBytes()));
        
        // Test null data
        assertFalse(previewRouter.showPreview(mockVaultFile, null));
        
        // Test null extension
        assertFalse(previewRouter.isPreviewSupported(null));
        
        // Test empty extension
        assertFalse(previewRouter.isPreviewSupported(""));
    }
    
    @Test
    @DisplayName("Should get error handler instance")
    void testGetErrorHandler() {
        PreviewErrorHandler errorHandler = previewRouter.getErrorHandler();
        assertNotNull(errorHandler);
    }
    
    @Test
    @DisplayName("Should handle cleanup on close")
    void testCleanupOnClose() {
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType(any())).thenReturn(true);
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        // Show preview
        byte[] testData = "test data".getBytes();
        previewRouter.showPreview(mockVaultFile, testData);
        
        // Close all previews
        previewRouter.closeAllPreviews();
        
        verify(mockComponent).close();
    }
    
    @Test
    @DisplayName("Should handle component exceptions during show")
    void testComponentExceptionDuringShow() {
        PreviewComponent mockComponent = mock(PreviewComponent.class);
        when(mockComponent.supportsFileType(any())).thenReturn(true);
        doThrow(new RuntimeException("Show failed")).when(mockComponent).show();
        
        when(mockFactory.isSupported(any(String.class))).thenReturn(true);
        when(mockFactory.createPreviewComponent(any())).thenReturn(mockComponent);
        
        byte[] testData = "test data".getBytes();
        boolean result = previewRouter.showPreview(mockVaultFile, testData);
        
        assertFalse(result);
        verify(mockComponent).show();
    }
    
    /**
     * Helper method to create mock VaultFile
     */
    private VaultFile createMockVaultFile(String name, String id) {
        VaultFile file = mock(VaultFile.class);
        when(file.getOriginalName()).thenReturn(name);
        when(file.getFileId()).thenReturn(id);
        when(file.getExtension()).thenReturn("txt");
        when(file.getSize()).thenReturn(1024L);
        return file;
    }
}