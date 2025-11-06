package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;

/**
 * Factory interface for creating preview components
 * Implements the Factory pattern for preview component creation
 */
public interface PreviewComponentFactory {
    
    /**
     * Create a preview component for the given file type
     * @param fileType The type of file to preview
     * @return A preview component capable of handling the file type, or null if unsupported
     */
    PreviewComponent createPreviewComponent(FileType fileType);
    
    /**
     * Create a preview component for the given vault file
     * @param vaultFile The vault file to preview
     * @return A preview component capable of handling the file, or null if unsupported
     */
    PreviewComponent createPreviewComponent(VaultFile vaultFile);
    
    /**
     * Check if a preview component exists for the given file type
     * @param fileType The file type to check
     * @return true if a preview component is available
     */
    boolean isSupported(FileType fileType);
    
    /**
     * Check if a preview component exists for the given file extension
     * @param fileExtension The file extension to check
     * @return true if a preview component is available
     */
    boolean isSupported(String fileExtension);
    
    /**
     * Get all supported file types
     * @return Array of supported file types
     */
    FileType[] getSupportedFileTypes();
    
    /**
     * Get all supported file extensions
     * @return Array of supported file extensions
     */
    String[] getSupportedExtensions();
    
    /**
     * Register a new preview component type
     * @param fileType The file type this component handles
     * @param componentClass The preview component class
     */
    void registerComponent(FileType fileType, Class<? extends PreviewComponent> componentClass);
    
    /**
     * Unregister a preview component type
     * @param fileType The file type to unregister
     */
    void unregisterComponent(FileType fileType);
    
    /**
     * Enumeration of supported file types for preview
     */
    enum FileType {
        // Media types
        AUDIO_MP3("mp3", "Audio MP3"),
        AUDIO_WAV("wav", "Audio WAV"),
        AUDIO_AAC("aac", "Audio AAC"),
        AUDIO_M4A("m4a", "Audio M4A"),
        AUDIO_FLAC("flac", "Audio FLAC"),
        
        VIDEO_MP4("mp4", "Video MP4"),
        VIDEO_AVI("avi", "Video AVI"),
        VIDEO_MOV("mov", "Video MOV"),
        VIDEO_MKV("mkv", "Video MKV"),
        VIDEO_M4V("m4v", "Video M4V"),
        
        // Code types
        CODE_JAVA("java", "Java Source"),
        CODE_PYTHON("py", "Python Source"),
        CODE_JAVASCRIPT("js", "JavaScript"),
        CODE_TYPESCRIPT("ts", "TypeScript"),
        CODE_HTML("html", "HTML"),
        CODE_CSS("css", "CSS"),
        CODE_XML("xml", "XML"),
        CODE_JSON("json", "JSON"),
        CODE_YAML("yaml", "YAML"),
        CODE_YML("yml", "YAML"),
        CODE_SQL("sql", "SQL"),
        CODE_SHELL("sh", "Shell Script"),
        CODE_BATCH("bat", "Batch File"),
        CODE_POWERSHELL("ps1", "PowerShell"),
        
        // Text types
        TEXT_PLAIN("txt", "Plain Text"),
        TEXT_MARKDOWN("md", "Markdown"),
        TEXT_LOG("log", "Log File"),
        TEXT_INI("ini", "Configuration"),
        TEXT_TOML("toml", "TOML Configuration"),
        TEXT_PROPERTIES("properties", "Properties File"),
        
        // Existing types (for compatibility)
        IMAGE_PNG("png", "PNG Image"),
        IMAGE_JPG("jpg", "JPEG Image"),
        IMAGE_JPEG("jpeg", "JPEG Image"),
        IMAGE_GIF("gif", "GIF Image"),
        IMAGE_BMP("bmp", "BMP Image"),
        
        DOCUMENT_PDF("pdf", "PDF Document"),
        
        // Fallback
        UNKNOWN("", "Unknown File Type");
        
        private final String extension;
        private final String displayName;
        
        FileType(String extension, String displayName) {
            this.extension = extension;
            this.displayName = displayName;
        }
        
        public String getExtension() {
            return extension;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Get FileType from file extension
         */
        public static FileType fromExtension(String extension) {
            if (extension == null || extension.isEmpty()) {
                return UNKNOWN;
            }
            
            String ext = extension.toLowerCase();
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            
            for (FileType type : values()) {
                if (type.extension.equals(ext)) {
                    return type;
                }
            }
            
            return UNKNOWN;
        }
        
        /**
         * Check if this is a media file type
         */
        public boolean isMediaType() {
            return name().startsWith("AUDIO_") || name().startsWith("VIDEO_");
        }
        
        /**
         * Check if this is a code file type
         */
        public boolean isCodeType() {
            return name().startsWith("CODE_");
        }
        
        /**
         * Check if this is a text file type
         */
        public boolean isTextType() {
            return name().startsWith("TEXT_");
        }
        
        /**
         * Check if this is an image file type
         */
        public boolean isImageType() {
            return name().startsWith("IMAGE_");
        }
        
        /**
         * Check if this is a document file type
         */
        public boolean isDocumentType() {
            return name().startsWith("DOCUMENT_");
        }
    }
}