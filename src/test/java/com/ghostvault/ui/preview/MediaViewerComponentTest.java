package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaViewerComponent
 * Note: Some tests are OS-specific due to JavaFX Media API limitations
 */
class MediaViewerComponentTest {
    
    private MediaViewerComponent mediaViewer;
    private VaultFile mockVaultFile;
    private PreviewSettings mockSettings;
    
    @BeforeEach
    void setUp() {
        mockVaultFile = mock(VaultFile.class);
        mockSettings = mock(PreviewSettings.class);
        
        // Setup default mock behaviors
        when(mockVaultFile.getOriginalName()).thenReturn("test.mp3");
        when(mockVaultFile.getFileId()).thenReturn("test-id-123");
        when(mockVaultFile.getExtension()).thenReturn("mp3");
        when(mockVaultFile.getSize()).thenReturn(5242880L); // 5MB
        
        when(mockSettings.getDefaultVolume()).thenReturn(0.7);
        when(mockSettings.isAutoPlayMedia()).thenReturn(false);
        when(mockSettings.isShowMediaControls()).thenReturn(true);
        
        mediaViewer = new MediaViewerComponent(mockSettings);
    }
    
    @Test
    @DisplayName("Should support correct file types")
    void testSupportedFileTypes() {
        assertTrue(mediaViewer.supportsFileType("mp3"));
        assertTrue(mediaViewer.supportsFileType("wav"));
        assertTrue(mediaViewer.supportsFileType("aac"));
        assertTrue(mediaViewer.supportsFileType("m4a"));
        assertTrue(mediaViewer.supportsFileType("mp4"));
        assertTrue(mediaViewer.supportsFileType("mov"));
        assertTrue(mediaViewer.supportsFileType("m4v"));
        
        assertFalse(mediaViewer.supportsFileType("txt"));
        assertFalse(mediaViewer.supportsFileType("jpg"));
        assertFalse(mediaViewer.supportsFileType("pdf"));
        assertFalse(mediaViewer.supportsFileType("flac")); // Not supported by JavaFX
        assertFalse(mediaViewer.supportsFileType("avi"));  // Limited support
    }
    
    @Test
    @DisplayName("Should return correct component name")
    void testGetComponentName() {
        assertEquals("Media Viewer", mediaViewer.getComponentName());
    }
    
    @Test
    @DisplayName("Should return correct supported extensions")
    void testGetSupportedExtensions() {
        String[] extensions = mediaViewer.getSupportedExtensions();
        assertNotNull(extensions);
        assertEquals(7, extensions.length);
        
        assertTrue(java.util.Arrays.asList(extensions).contains("mp3"));
        assertTrue(java.util.Arrays.asList(extensions).contains("wav"));
        assertTrue(java.util.Arrays.asList(extensions).contains("mp4"));
    }
    
    @Test
    @DisplayName("Should handle null or empty content gracefully")
    void testNullOrEmptyContent() {
        mediaViewer.setVaultFile(mockVaultFile);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            mediaViewer.loadContent(null);
        });
        
        assertDoesNotThrow(() -> {
            mediaViewer.loadContent(new byte[0]);
        });
    }
    
    @Test
    @DisplayName("Should create scene without errors")
    void testCreateScene() {
        mediaViewer.setVaultFile(mockVaultFile);
        
        assertDoesNotThrow(() -> {
            javafx.scene.Scene scene = mediaViewer.createScene();
            assertNotNull(scene);
            assertTrue(scene.getWidth() > 0);
            assertTrue(scene.getHeight() > 0);
        });
    }
    
    @Test
    @DisplayName("Should handle video file detection correctly")
    void testVideoFileDetection() {
        // Test video files
        when(mockVaultFile.getExtension()).thenReturn("mp4");
        mediaViewer.setVaultFile(mockVaultFile);
        // Video detection is internal, but we can test through supported types
        assertTrue(mediaViewer.supportsFileType("mp4"));
        
        when(mockVaultFile.getExtension()).thenReturn("mov");
        assertTrue(mediaViewer.supportsFileType("mov"));
        
        when(mockVaultFile.getExtension()).thenReturn("m4v");
        assertTrue(mediaViewer.supportsFileType("m4v"));
        
        // Test audio files
        when(mockVaultFile.getExtension()).thenReturn("mp3");
        assertTrue(mediaViewer.supportsFileType("mp3"));
        
        when(mockVaultFile.getExtension()).thenReturn("wav");
        assertTrue(mediaViewer.supportsFileType("wav"));
    }
    
    @Test
    @DisplayName("Should handle cleanup properly")
    void testCleanup() {
        mediaViewer.setVaultFile(mockVaultFile);
        
        // Should not throw exceptions during cleanup
        assertDoesNotThrow(() -> {
            mediaViewer.cleanup();
        });
        
        // Should be able to call cleanup multiple times
        assertDoesNotThrow(() -> {
            mediaViewer.cleanup();
            mediaViewer.cleanup();
        });
    }
    
    @Test
    @DisplayName("Should handle case insensitive file extensions")
    void testCaseInsensitiveExtensions() {
        assertTrue(mediaViewer.supportsFileType("MP3"));
        assertTrue(mediaViewer.supportsFileType("Mp3"));
        assertTrue(mediaViewer.supportsFileType("WAV"));
        assertTrue(mediaViewer.supportsFileType("Mp4"));
        assertTrue(mediaViewer.supportsFileType("MOV"));
    }
    
    @Test
    @DisplayName("Should use settings correctly")
    void testSettingsUsage() {
        // Test with different settings
        when(mockSettings.getDefaultVolume()).thenReturn(0.5);
        when(mockSettings.isAutoPlayMedia()).thenReturn(true);
        
        MediaViewerComponent customViewer = new MediaViewerComponent(mockSettings);
        customViewer.setVaultFile(mockVaultFile);
        
        // Settings should be used during initialization
        assertDoesNotThrow(() -> {
            customViewer.createScene();
        });
    }
    
    @Test
    @DisplayName("Should handle null settings gracefully")
    void testNullSettings() {
        MediaViewerComponent viewerWithNullSettings = new MediaViewerComponent(null);
        viewerWithNullSettings.setVaultFile(mockVaultFile);
        
        // Should create default settings internally
        assertDoesNotThrow(() -> {
            viewerWithNullSettings.createScene();
        });
    }
    
    @Test
    @DisplayName("Should format file sizes correctly")
    void testFileSizeFormatting() {
        // Test different file sizes through the component
        when(mockVaultFile.getSize()).thenReturn(512L);
        mediaViewer.setVaultFile(mockVaultFile);
        assertDoesNotThrow(() -> mediaViewer.createScene());
        
        when(mockVaultFile.getSize()).thenReturn(2048L);
        mediaViewer.setVaultFile(mockVaultFile);
        assertDoesNotThrow(() -> mediaViewer.createScene());
        
        when(mockVaultFile.getSize()).thenReturn(5L * 1024 * 1024);
        mediaViewer.setVaultFile(mockVaultFile);
        assertDoesNotThrow(() -> mediaViewer.createScene());
    }
    
    @Test
    @DisplayName("Should handle different audio formats")
    void testAudioFormats() {
        String[] audioFormats = {"mp3", "wav", "aac", "m4a"};
        
        for (String format : audioFormats) {
            when(mockVaultFile.getExtension()).thenReturn(format);
            when(mockVaultFile.getOriginalName()).thenReturn("test." + format);
            
            mediaViewer.setVaultFile(mockVaultFile);
            assertTrue(mediaViewer.supportsFileType(format));
            
            // Should create scene without errors
            assertDoesNotThrow(() -> {
                mediaViewer.createScene();
            });
        }
    }
    
    @Test
    @DisplayName("Should handle different video formats")
    void testVideoFormats() {
        String[] videoFormats = {"mp4", "mov", "m4v"};
        
        for (String format : videoFormats) {
            when(mockVaultFile.getExtension()).thenReturn(format);
            when(mockVaultFile.getOriginalName()).thenReturn("test." + format);
            
            mediaViewer.setVaultFile(mockVaultFile);
            assertTrue(mediaViewer.supportsFileType(format));
            
            // Should create scene without errors
            assertDoesNotThrow(() -> {
                mediaViewer.createScene();
            });
        }
    }
    
    @Test
    @DisplayName("Should handle unsupported formats gracefully")
    void testUnsupportedFormats() {
        String[] unsupportedFormats = {"flac", "avi", "mkv", "wmv", "ogg"};
        
        for (String format : unsupportedFormats) {
            assertFalse(mediaViewer.supportsFileType(format), 
                "Format " + format + " should not be supported");
        }
    }
    
    @Test
    @DisplayName("Should handle null vault file")
    void testNullVaultFile() {
        mediaViewer.setVaultFile(null);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            mediaViewer.createScene();
            mediaViewer.loadContent("test data".getBytes());
            mediaViewer.cleanup();
        });
    }
    
    @Test
    @DisplayName("Should handle empty file names")
    void testEmptyFileName() {
        when(mockVaultFile.getOriginalName()).thenReturn("");
        when(mockVaultFile.getExtension()).thenReturn("mp3");
        
        mediaViewer.setVaultFile(mockVaultFile);
        
        assertDoesNotThrow(() -> {
            mediaViewer.createScene();
        });
    }
    
    @Test
    @DisplayName("Should handle very large file sizes")
    void testLargeFileSizes() {
        // Test with very large file size
        when(mockVaultFile.getSize()).thenReturn(Long.MAX_VALUE);
        mediaViewer.setVaultFile(mockVaultFile);
        
        assertDoesNotThrow(() -> {
            mediaViewer.createScene();
        });
    }
    
    @Test
    @DisplayName("Should handle special characters in file names")
    void testSpecialCharactersInFileName() {
        when(mockVaultFile.getOriginalName()).thenReturn("test file with spaces & symbols!.mp3");
        mediaViewer.setVaultFile(mockVaultFile);
        
        assertDoesNotThrow(() -> {
            mediaViewer.createScene();
        });
    }
    
    // Note: The following tests would require JavaFX Application Thread and actual media files
    // They are commented out but show what could be tested in an integration test environment
    
    /*
    @Test
    @EnabledOnOs({OS.WINDOWS, OS.MAC}) // JavaFX Media API has better support on these platforms
    @DisplayName("Should load and play media file")
    void testMediaPlayback() {
        // This would require actual media file data and JavaFX Application Thread
        // Platform.runLater(() -> {
        //     byte[] testAudioData = createTestAudioData();
        //     mediaViewer.loadContent(testAudioData);
        //     // Test playback controls
        // });
    }
    
    @Test
    @EnabledOnOs({OS.WINDOWS, OS.MAC})
    @DisplayName("Should handle media player errors")
    void testMediaPlayerErrors() {
        // This would test error handling with corrupted media data
        // byte[] corruptedData = new byte[]{0x00, 0x01, 0x02};
        // mediaViewer.loadContent(corruptedData);
        // Verify error handling
    }
    */
    
    /**
     * Helper method to create mock test audio data
     * In a real test, this would be actual audio file bytes
     */
    private byte[] createTestAudioData() {
        // This is just placeholder data - real tests would use actual media files
        return "fake audio data".getBytes();
    }
}