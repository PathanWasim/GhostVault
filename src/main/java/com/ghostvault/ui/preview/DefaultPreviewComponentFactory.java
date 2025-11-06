package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Default implementation of PreviewComponentFactory
 * Manages registration and creation of preview components
 */
public class DefaultPreviewComponentFactory implements PreviewComponentFactory {
    
    // Component registry mapping file types to component classes
    private final Map<FileType, Class<? extends PreviewComponent>> componentRegistry;
    
    // Cached instances for reuse (if components support it)
    private final Map<FileType, PreviewComponent> componentCache;
    
    public DefaultPreviewComponentFactory() {
        this.componentRegistry = new ConcurrentHashMap<>();
        this.componentCache = new ConcurrentHashMap<>();
        
        // Register default component mappings
        registerDefaultComponents();
    }
    
    /**
     * Register default preview components
     */
    private void registerDefaultComponents() {
        // Note: These will be implemented in subsequent tasks
        // For now, we'll register placeholders that will be replaced
        
        // Media components (implemented in task 5)
        registerComponent(FileType.AUDIO_MP3, MediaViewerComponent.class);
        registerComponent(FileType.AUDIO_WAV, MediaViewerComponent.class);
        registerComponent(FileType.AUDIO_AAC, MediaViewerComponent.class);
        registerComponent(FileType.AUDIO_M4A, MediaViewerComponent.class);
        // Note: FLAC not supported by JavaFX Media API
        
        registerComponent(FileType.VIDEO_MP4, MediaViewerComponent.class);
        registerComponent(FileType.VIDEO_MOV, MediaViewerComponent.class);
        registerComponent(FileType.VIDEO_M4V, MediaViewerComponent.class);
        // Note: AVI and MKV have limited support in JavaFX Media API
        
        // Code components (implemented in task 3)
        registerComponent(FileType.CODE_JAVA, CodeViewerComponent.class);
        registerComponent(FileType.CODE_PYTHON, CodeViewerComponent.class);
        registerComponent(FileType.CODE_JAVASCRIPT, CodeViewerComponent.class);
        registerComponent(FileType.CODE_TYPESCRIPT, CodeViewerComponent.class);
        registerComponent(FileType.CODE_HTML, CodeViewerComponent.class);
        registerComponent(FileType.CODE_CSS, CodeViewerComponent.class);
        registerComponent(FileType.CODE_XML, CodeViewerComponent.class);
        registerComponent(FileType.CODE_JSON, CodeViewerComponent.class);
        registerComponent(FileType.CODE_YAML, CodeViewerComponent.class);
        registerComponent(FileType.CODE_YML, CodeViewerComponent.class);
        registerComponent(FileType.CODE_SQL, CodeViewerComponent.class);
        registerComponent(FileType.CODE_SHELL, CodeViewerComponent.class);
        registerComponent(FileType.CODE_BATCH, CodeViewerComponent.class);
        registerComponent(FileType.CODE_POWERSHELL, CodeViewerComponent.class);
        
        // Text components (implemented in task 4)
        registerComponent(FileType.TEXT_PLAIN, EnhancedTextViewerComponent.class);
        registerComponent(FileType.TEXT_MARKDOWN, EnhancedTextViewerComponent.class);
        registerComponent(FileType.TEXT_LOG, EnhancedTextViewerComponent.class);
        registerComponent(FileType.TEXT_INI, EnhancedTextViewerComponent.class);
        registerComponent(FileType.TEXT_TOML, EnhancedTextViewerComponent.class);
        registerComponent(FileType.TEXT_PROPERTIES, EnhancedTextViewerComponent.class);
        
        // Existing components (already implemented in GhostVault)
        // These would map to existing preview functionality
        // registerComponent(FileType.IMAGE_PNG, ExistingImageViewerComponent.class);
        // registerComponent(FileType.IMAGE_JPG, ExistingImageViewerComponent.class);
        // registerComponent(FileType.IMAGE_JPEG, ExistingImageViewerComponent.class);
        // registerComponent(FileType.IMAGE_GIF, ExistingImageViewerComponent.class);
        // registerComponent(FileType.IMAGE_BMP, ExistingImageViewerComponent.class);
        
        // registerComponent(FileType.DOCUMENT_PDF, ExistingPdfViewerComponent.class);
    }
    
    @Override
    public PreviewComponent createPreviewComponent(FileType fileType) {
        if (fileType == null || fileType == FileType.UNKNOWN) {
            return null;
        }
        
        Class<? extends PreviewComponent> componentClass = componentRegistry.get(fileType);
        if (componentClass == null) {
            return null;
        }
        
        try {
            // Create new instance using reflection
            return componentClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Log error and return null
            System.err.println("Failed to create preview component for " + fileType + ": " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public PreviewComponent createPreviewComponent(VaultFile vaultFile) {
        if (vaultFile == null) {
            return null;
        }
        
        FileType fileType = vaultFile.getFileType();
        return createPreviewComponent(fileType);
    }
    
    @Override
    public boolean isSupported(FileType fileType) {
        return fileType != null && 
               fileType != FileType.UNKNOWN && 
               componentRegistry.containsKey(fileType);
    }
    
    @Override
    public boolean isSupported(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return false;
        }
        
        FileType fileType = FileType.fromExtension(fileExtension);
        return isSupported(fileType);
    }
    
    @Override
    public FileType[] getSupportedFileTypes() {
        Set<FileType> supportedTypes = componentRegistry.keySet();
        return supportedTypes.toArray(new FileType[0]);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        Set<String> extensions = new HashSet<>();
        
        for (FileType fileType : componentRegistry.keySet()) {
            if (fileType != FileType.UNKNOWN) {
                extensions.add(fileType.getExtension());
            }
        }
        
        return extensions.toArray(new String[0]);
    }
    
    @Override
    public void registerComponent(FileType fileType, Class<? extends PreviewComponent> componentClass) {
        if (fileType == null || fileType == FileType.UNKNOWN || componentClass == null) {
            throw new IllegalArgumentException("FileType and component class cannot be null or unknown");
        }
        
        // Validate that the class can be instantiated
        try {
            componentClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Component class must have a default constructor", e);
        }
        
        componentRegistry.put(fileType, componentClass);
        
        // Clear any cached instance
        componentCache.remove(fileType);
    }
    
    @Override
    public void unregisterComponent(FileType fileType) {
        if (fileType != null) {
            componentRegistry.remove(fileType);
            componentCache.remove(fileType);
        }
    }
    
    /**
     * Get all registered file types
     * @return Set of registered file types
     */
    public Set<FileType> getRegisteredFileTypes() {
        return new HashSet<>(componentRegistry.keySet());
    }
    
    /**
     * Get the component class for a file type
     * @param fileType The file type
     * @return The component class or null if not registered
     */
    public Class<? extends PreviewComponent> getComponentClass(FileType fileType) {
        return componentRegistry.get(fileType);
    }
    
    /**
     * Check if a component class is registered
     * @param componentClass The component class
     * @return true if the class is registered for any file type
     */
    public boolean isComponentRegistered(Class<? extends PreviewComponent> componentClass) {
        return componentRegistry.containsValue(componentClass);
    }
    
    /**
     * Get statistics about registered components
     * @return FactoryStats object
     */
    public FactoryStats getStatistics() {
        int totalRegistered = componentRegistry.size();
        int mediaComponents = 0;
        int codeComponents = 0;
        int textComponents = 0;
        int imageComponents = 0;
        int documentComponents = 0;
        
        for (FileType fileType : componentRegistry.keySet()) {
            if (fileType.isMediaType()) {
                mediaComponents++;
            } else if (fileType.isCodeType()) {
                codeComponents++;
            } else if (fileType.isTextType()) {
                textComponents++;
            } else if (fileType.isImageType()) {
                imageComponents++;
            } else if (fileType.isDocumentType()) {
                documentComponents++;
            }
        }
        
        return new FactoryStats(totalRegistered, mediaComponents, codeComponents, 
                               textComponents, imageComponents, documentComponents);
    }
    
    /**
     * Clear all registered components
     */
    public void clearAll() {
        componentRegistry.clear();
        componentCache.clear();
    }
    
    /**
     * Factory statistics data class
     */
    public static class FactoryStats {
        private final int totalComponents;
        private final int mediaComponents;
        private final int codeComponents;
        private final int textComponents;
        private final int imageComponents;
        private final int documentComponents;
        
        public FactoryStats(int totalComponents, int mediaComponents, int codeComponents,
                           int textComponents, int imageComponents, int documentComponents) {
            this.totalComponents = totalComponents;
            this.mediaComponents = mediaComponents;
            this.codeComponents = codeComponents;
            this.textComponents = textComponents;
            this.imageComponents = imageComponents;
            this.documentComponents = documentComponents;
        }
        
        public int getTotalComponents() { return totalComponents; }
        public int getMediaComponents() { return mediaComponents; }
        public int getCodeComponents() { return codeComponents; }
        public int getTextComponents() { return textComponents; }
        public int getImageComponents() { return imageComponents; }
        public int getDocumentComponents() { return documentComponents; }
        
        @Override
        public String toString() {
            return String.format("FactoryStats{total=%d, media=%d, code=%d, text=%d, image=%d, document=%d}",
                totalComponents, mediaComponents, codeComponents, textComponents, imageComponents, documentComponents);
        }
    }
}