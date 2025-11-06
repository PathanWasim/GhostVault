package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultPreviewComponentFactory
 */
class DefaultPreviewComponentFactoryTest {
    
    private DefaultPreviewComponentFactory factory;
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        factory = new DefaultPreviewComponentFactory();
        mockVaultFile = mock(VaultFile.class);
    }
    
    @Test
    @DisplayName("Should create code viewer components")
    void testCreateCodeViewerComponents() {
        PreviewComponent javaComponent = factory.createPreviewComponent(FileType.CODE_JAVA);
        assertNotNull(javaComponent);
        assertTrue(javaComponent instanceof CodeViewerComponent);
        
        PreviewComponent pythonComponent = factory.createPreviewComponent(FileType.CODE_PYTHON);
        assertNotNull(pythonComponent);
        assertTrue(pythonComponent instanceof CodeViewerComponent);
        
        PreviewComponent jsComponent = factory.createPreviewComponent(FileType.CODE_JAVASCRIPT);
        assertNotNull(jsComponent);
        assertTrue(jsComponent instanceof CodeViewerComponent);
    }
    
    @Test
    @DisplayName("Should create text viewer components")
    void testCreateTextViewerComponents() {
        PreviewComponent textComponent = factory.createPreviewComponent(FileType.TEXT_PLAIN);
        assertNotNull(textComponent);
        assertTrue(textComponent instanceof EnhancedTextViewerComponent);
        
        PreviewComponent markdownComponent = factory.createPreviewComponent(FileType.TEXT_MARKDOWN);
        assertNotNull(markdownComponent);
        assertTrue(markdownComponent instanceof EnhancedTextViewerComponent);
        
        PreviewComponent logComponent = factory.createPreviewComponent(FileType.TEXT_LOG);
        assertNotNull(logComponent);
        assertTrue(logComponent instanceof EnhancedTextViewerComponent);
    }
    
    @Test
    @DisplayName("Should return null for unsupported file types")
    void testUnsupportedFileTypes() {
        // Media components not yet implemented
        assertNull(factory.createPreviewComponent(FileType.AUDIO_MP3));
        assertNull(factory.createPreviewComponent(FileType.VIDEO_MP4));
        
        // Unknown type
        assertNull(factory.createPreviewComponent(FileType.UNKNOWN));
        assertNull(factory.createPreviewComponent(null));
    }
    
    @Test
    @DisplayName("Should create components from VaultFile")
    void testCreateFromVaultFile() {
        when(mockVaultFile.getFileType()).thenReturn(FileType.CODE_JAVA);
        
        PreviewComponent component = factory.createPreviewComponent(mockVaultFile);
        assertNotNull(component);
        assertTrue(component instanceof CodeViewerComponent);
        
        // Test with null VaultFile
        assertNull(factory.createPreviewComponent((VaultFile) null));
    }
    
    @Test
    @DisplayName("Should check support correctly")
    void testIsSupported() {
        // Supported types
        assertTrue(factory.isSupported(FileType.CODE_JAVA));
        assertTrue(factory.isSupported(FileType.TEXT_MARKDOWN));
        assertTrue(factory.isSupported(FileType.CODE_JSON));
        
        // Unsupported types
        assertFalse(factory.isSupported(FileType.AUDIO_MP3));
        assertFalse(factory.isSupported(FileType.VIDEO_MP4));
        assertFalse(factory.isSupported(FileType.UNKNOWN));
        assertFalse(factory.isSupported((FileType) null));
        
        // Test by extension
        assertTrue(factory.isSupported("java"));
        assertTrue(factory.isSupported("md"));
        assertTrue(factory.isSupported("json"));
        assertFalse(factory.isSupported("mp3"));
        assertFalse(factory.isSupported("mp4"));
        assertFalse(factory.isSupported(""));
        assertFalse(factory.isSupported(null));
    }
    
    @Test
    @DisplayName("Should get supported file types")
    void testGetSupportedFileTypes() {
        FileType[] supportedTypes = factory.getSupportedFileTypes();
        assertNotNull(supportedTypes);
        assertTrue(supportedTypes.length > 0);
        
        // Should contain code types
        boolean hasJava = false;
        boolean hasPython = false;
        for (FileType type : supportedTypes) {
            if (type == FileType.CODE_JAVA) hasJava = true;
            if (type == FileType.CODE_PYTHON) hasPython = true;
        }
        assertTrue(hasJava);
        assertTrue(hasPython);
    }
    
    @Test
    @DisplayName("Should get supported extensions")
    void testGetSupportedExtensions() {
        String[] supportedExtensions = factory.getSupportedExtensions();
        assertNotNull(supportedExtensions);
        assertTrue(supportedExtensions.length > 0);
        
        // Should contain common extensions
        boolean hasJava = false;
        boolean hasMd = false;
        for (String ext : supportedExtensions) {
            if ("java".equals(ext)) hasJava = true;
            if ("md".equals(ext)) hasMd = true;
        }
        assertTrue(hasJava);
        assertTrue(hasMd);
    }
    
    @Test
    @DisplayName("Should register and unregister components")
    void testRegisterUnregisterComponents() {
        // Create a mock component class
        class MockPreviewComponent extends PreviewComponent {
            @Override
            public void loadContent(byte[] fileData) {}
            
            @Override
            protected javafx.scene.Scene createScene() {
                return null;
            }
            
            @Override
            public boolean supportsFileType(String fileExtension) {
                return "mock".equals(fileExtension);
            }
            
            @Override
            public String getComponentName() {
                return "Mock Component";
            }
            
            @Override
            public String[] getSupportedExtensions() {
                return new String[]{"mock"};
            }
        }
        
        // Initially not supported
        assertFalse(factory.isSupported("mock"));
        
        // Register the component
        factory.registerComponent(FileType.UNKNOWN, MockPreviewComponent.class);
        
        // Should be able to create it now (though we're using UNKNOWN type for testing)
        PreviewComponent component = factory.createPreviewComponent(FileType.UNKNOWN);
        assertNotNull(component);
        assertTrue(component instanceof MockPreviewComponent);
        
        // Unregister the component
        factory.unregisterComponent(FileType.UNKNOWN);
        
        // Should return null again
        assertNull(factory.createPreviewComponent(FileType.UNKNOWN));
    }
    
    @Test
    @DisplayName("Should handle invalid registrations")
    void testInvalidRegistrations() {
        // Should throw exception for null or invalid parameters
        assertThrows(IllegalArgumentException.class, () -> {
            factory.registerComponent(null, CodeViewerComponent.class);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.registerComponent(FileType.UNKNOWN, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.registerComponent(FileType.UNKNOWN, CodeViewerComponent.class);
        });
        
        // Should handle unregistering null gracefully
        assertDoesNotThrow(() -> {
            factory.unregisterComponent(null);
        });
    }
    
    @Test
    @DisplayName("Should get registered file types")
    void testGetRegisteredFileTypes() {
        var registeredTypes = factory.getRegisteredFileTypes();
        assertNotNull(registeredTypes);
        assertTrue(registeredTypes.size() > 0);
        
        // Should contain registered types
        assertTrue(registeredTypes.contains(FileType.CODE_JAVA));
        assertTrue(registeredTypes.contains(FileType.TEXT_MARKDOWN));
    }
    
    @Test
    @DisplayName("Should get component class for file type")
    void testGetComponentClass() {
        Class<? extends PreviewComponent> javaClass = factory.getComponentClass(FileType.CODE_JAVA);
        assertEquals(CodeViewerComponent.class, javaClass);
        
        Class<? extends PreviewComponent> textClass = factory.getComponentClass(FileType.TEXT_PLAIN);
        assertEquals(EnhancedTextViewerComponent.class, textClass);
        
        // Unsupported type should return null
        assertNull(factory.getComponentClass(FileType.AUDIO_MP3));
    }
    
    @Test
    @DisplayName("Should check if component is registered")
    void testIsComponentRegistered() {
        assertTrue(factory.isComponentRegistered(CodeViewerComponent.class));
        assertTrue(factory.isComponentRegistered(EnhancedTextViewerComponent.class));
        
        // Create a mock class that's not registered
        class UnregisteredComponent extends PreviewComponent {
            @Override
            public void loadContent(byte[] fileData) {}
            @Override
            protected javafx.scene.Scene createScene() { return null; }
            @Override
            public boolean supportsFileType(String fileExtension) { return false; }
            @Override
            public String getComponentName() { return "Unregistered"; }
            @Override
            public String[] getSupportedExtensions() { return new String[0]; }
        }
        
        assertFalse(factory.isComponentRegistered(UnregisteredComponent.class));
    }
    
    @Test
    @DisplayName("Should get factory statistics")
    void testGetStatistics() {
        DefaultPreviewComponentFactory.FactoryStats stats = factory.getStatistics();
        assertNotNull(stats);
        
        assertTrue(stats.getTotalComponents() > 0);
        assertTrue(stats.getCodeComponents() > 0);
        assertTrue(stats.getTextComponents() > 0);
        
        // Media components not yet implemented
        assertEquals(0, stats.getMediaComponents());
        
        // Test toString
        String statsString = stats.toString();
        assertNotNull(statsString);
        assertTrue(statsString.contains("total="));
        assertTrue(statsString.contains("code="));
    }
    
    @Test
    @DisplayName("Should clear all components")
    void testClearAll() {
        // Initially has components
        assertTrue(factory.getSupportedFileTypes().length > 0);
        
        // Clear all
        factory.clearAll();
        
        // Should have no components
        assertEquals(0, factory.getSupportedFileTypes().length);
        assertEquals(0, factory.getSupportedExtensions().length);
        assertFalse(factory.isSupported(FileType.CODE_JAVA));
    }
    
    @Test
    @DisplayName("Should handle component creation failures gracefully")
    void testComponentCreationFailures() {
        // Create a component class without default constructor
        class BadComponent extends PreviewComponent {
            public BadComponent(String param) {} // No default constructor
            
            @Override
            public void loadContent(byte[] fileData) {}
            @Override
            protected javafx.scene.Scene createScene() { return null; }
            @Override
            public boolean supportsFileType(String fileExtension) { return false; }
            @Override
            public String getComponentName() { return "Bad"; }
            @Override
            public String[] getSupportedExtensions() { return new String[0]; }
        }
        
        // Should throw exception when trying to register
        assertThrows(IllegalArgumentException.class, () -> {
            factory.registerComponent(FileType.UNKNOWN, BadComponent.class);
        });
    }
}