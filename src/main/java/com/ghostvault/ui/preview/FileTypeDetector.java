package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced file type detection utility using Apache Tika and file extension analysis
 * Provides accurate MIME type detection and file categorization
 */
public class FileTypeDetector {
    
    private static final Tika tika = new Tika();
    
    // MIME type to FileType mapping
    private static final Map<String, FileType> MIME_TYPE_MAP = new HashMap<>();
    
    static {
        // Audio MIME types
        MIME_TYPE_MAP.put("audio/mpeg", FileType.AUDIO_MP3);
        MIME_TYPE_MAP.put("audio/mp3", FileType.AUDIO_MP3);
        MIME_TYPE_MAP.put("audio/wav", FileType.AUDIO_WAV);
        MIME_TYPE_MAP.put("audio/wave", FileType.AUDIO_WAV);
        MIME_TYPE_MAP.put("audio/x-wav", FileType.AUDIO_WAV);
        MIME_TYPE_MAP.put("audio/aac", FileType.AUDIO_AAC);
        MIME_TYPE_MAP.put("audio/mp4", FileType.AUDIO_M4A);
        MIME_TYPE_MAP.put("audio/x-m4a", FileType.AUDIO_M4A);
        MIME_TYPE_MAP.put("audio/flac", FileType.AUDIO_FLAC);
        MIME_TYPE_MAP.put("audio/x-flac", FileType.AUDIO_FLAC);
        
        // Video MIME types
        MIME_TYPE_MAP.put("video/mp4", FileType.VIDEO_MP4);
        MIME_TYPE_MAP.put("video/avi", FileType.VIDEO_AVI);
        MIME_TYPE_MAP.put("video/x-msvideo", FileType.VIDEO_AVI);
        MIME_TYPE_MAP.put("video/quicktime", FileType.VIDEO_MOV);
        MIME_TYPE_MAP.put("video/x-matroska", FileType.VIDEO_MKV);
        MIME_TYPE_MAP.put("video/x-m4v", FileType.VIDEO_M4V);
        
        // Text MIME types
        MIME_TYPE_MAP.put("text/plain", FileType.TEXT_PLAIN);
        MIME_TYPE_MAP.put("text/markdown", FileType.TEXT_MARKDOWN);
        MIME_TYPE_MAP.put("text/x-markdown", FileType.TEXT_MARKDOWN);
        MIME_TYPE_MAP.put("application/json", FileType.CODE_JSON);
        MIME_TYPE_MAP.put("text/json", FileType.CODE_JSON);
        MIME_TYPE_MAP.put("application/xml", FileType.CODE_XML);
        MIME_TYPE_MAP.put("text/xml", FileType.CODE_XML);
        MIME_TYPE_MAP.put("application/x-yaml", FileType.CODE_YAML);
        MIME_TYPE_MAP.put("text/yaml", FileType.CODE_YAML);
        MIME_TYPE_MAP.put("text/x-yaml", FileType.CODE_YAML);
        
        // Code MIME types
        MIME_TYPE_MAP.put("text/x-java-source", FileType.CODE_JAVA);
        MIME_TYPE_MAP.put("text/x-python", FileType.CODE_PYTHON);
        MIME_TYPE_MAP.put("application/javascript", FileType.CODE_JAVASCRIPT);
        MIME_TYPE_MAP.put("text/javascript", FileType.CODE_JAVASCRIPT);
        MIME_TYPE_MAP.put("application/typescript", FileType.CODE_TYPESCRIPT);
        MIME_TYPE_MAP.put("text/html", FileType.CODE_HTML);
        MIME_TYPE_MAP.put("text/css", FileType.CODE_CSS);
        MIME_TYPE_MAP.put("application/sql", FileType.CODE_SQL);
        MIME_TYPE_MAP.put("text/x-sql", FileType.CODE_SQL);
        MIME_TYPE_MAP.put("application/x-sh", FileType.CODE_SHELL);
        MIME_TYPE_MAP.put("text/x-shellscript", FileType.CODE_SHELL);
        
        // Image MIME types (existing support)
        MIME_TYPE_MAP.put("image/png", FileType.IMAGE_PNG);
        MIME_TYPE_MAP.put("image/jpeg", FileType.IMAGE_JPEG);
        MIME_TYPE_MAP.put("image/jpg", FileType.IMAGE_JPG);
        MIME_TYPE_MAP.put("image/gif", FileType.IMAGE_GIF);
        MIME_TYPE_MAP.put("image/bmp", FileType.IMAGE_BMP);
        MIME_TYPE_MAP.put("image/x-ms-bmp", FileType.IMAGE_BMP);
        
        // Document MIME types (existing support)
        MIME_TYPE_MAP.put("application/pdf", FileType.DOCUMENT_PDF);
    }
    
    /**
     * Detect file type from VaultFile using extension and content analysis
     */
    public static FileType detectFileType(VaultFile vaultFile) {
        if (vaultFile == null) {
            return FileType.UNKNOWN;
        }
        
        // First try extension-based detection
        FileType extensionType = FileType.fromExtension(vaultFile.getExtension());
        if (extensionType != FileType.UNKNOWN) {
            return extensionType;
        }
        
        // Fallback to filename analysis
        return detectFromFilename(vaultFile.getOriginalName());
    }
    
    /**
     * Detect file type from file content using Apache Tika
     */
    public static FileType detectFileType(byte[] fileData, String filename) {
        if (fileData == null || fileData.length == 0) {
            return detectFromFilename(filename);
        }
        
        try {
            // Use Tika to detect MIME type from content
            Metadata metadata = new Metadata();
            if (filename != null) {
                // metadata.set(Metadata.RESOURCE_NAME_KEY, filename); // Constant not available
            }
            
            String mimeType = tika.detect(new ByteArrayInputStream(fileData), metadata);
            
            // Map MIME type to FileType
            FileType detectedType = MIME_TYPE_MAP.get(mimeType);
            if (detectedType != null) {
                return detectedType;
            }
            
            // Fallback to extension-based detection
            if (filename != null) {
                return detectFromFilename(filename);
            }
            
        } catch (IOException e) {
            // If Tika fails, fall back to filename detection
            if (filename != null) {
                return detectFromFilename(filename);
            }
        }
        
        return FileType.UNKNOWN;
    }
    
    /**
     * Detect file type from filename/extension only
     */
    public static FileType detectFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return FileType.UNKNOWN;
        }
        
        // Extract extension
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            String extension = filename.substring(lastDot + 1).toLowerCase();
            return FileType.fromExtension(extension);
        }
        
        return FileType.UNKNOWN;
    }
    
    /**
     * Get MIME type for a file using Tika
     */
    public static String getMimeType(byte[] fileData, String filename) {
        if (fileData == null || fileData.length == 0) {
            return getMimeTypeFromFilename(filename);
        }
        
        try {
            Metadata metadata = new Metadata();
            if (filename != null) {
                // metadata.set(Metadata.RESOURCE_NAME_KEY, filename); // Constant not available
            }
            
            return tika.detect(new ByteArrayInputStream(fileData), metadata);
        } catch (IOException e) {
            return getMimeTypeFromFilename(filename);
        }
    }
    
    /**
     * Get MIME type from filename only
     */
    public static String getMimeTypeFromFilename(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        FileType fileType = detectFromFilename(filename);
        return getMimeTypeForFileType(fileType);
    }
    
    /**
     * Get MIME type for a FileType
     */
    public static String getMimeTypeForFileType(FileType fileType) {
        switch (fileType) {
            // Audio
            case AUDIO_MP3: return "audio/mpeg";
            case AUDIO_WAV: return "audio/wav";
            case AUDIO_AAC: return "audio/aac";
            case AUDIO_M4A: return "audio/mp4";
            case AUDIO_FLAC: return "audio/flac";
            
            // Video
            case VIDEO_MP4: return "video/mp4";
            case VIDEO_AVI: return "video/avi";
            case VIDEO_MOV: return "video/quicktime";
            case VIDEO_MKV: return "video/x-matroska";
            case VIDEO_M4V: return "video/x-m4v";
            
            // Code
            case CODE_JAVA: return "text/x-java-source";
            case CODE_PYTHON: return "text/x-python";
            case CODE_JAVASCRIPT: return "application/javascript";
            case CODE_TYPESCRIPT: return "application/typescript";
            case CODE_HTML: return "text/html";
            case CODE_CSS: return "text/css";
            case CODE_XML: return "application/xml";
            case CODE_JSON: return "application/json";
            case CODE_YAML:
            case CODE_YML: return "application/x-yaml";
            case CODE_SQL: return "application/sql";
            case CODE_SHELL: return "application/x-sh";
            case CODE_BATCH: return "application/x-bat";
            case CODE_POWERSHELL: return "application/x-powershell";
            
            // Text
            case TEXT_PLAIN: return "text/plain";
            case TEXT_MARKDOWN: return "text/markdown";
            case TEXT_LOG: return "text/plain";
            case TEXT_INI: return "text/plain";
            case TEXT_TOML: return "text/plain";
            case TEXT_PROPERTIES: return "text/plain";
            
            // Images
            case IMAGE_PNG: return "image/png";
            case IMAGE_JPG:
            case IMAGE_JPEG: return "image/jpeg";
            case IMAGE_GIF: return "image/gif";
            case IMAGE_BMP: return "image/bmp";
            
            // Documents
            case DOCUMENT_PDF: return "application/pdf";
            
            default: return "application/octet-stream";
        }
    }
    
    /**
     * Check if a file type is supported for preview
     */
    public static boolean isPreviewSupported(FileType fileType) {
        return fileType != FileType.UNKNOWN && 
               (fileType.isMediaType() || fileType.isCodeType() || 
                fileType.isTextType() || fileType.isImageType() || 
                fileType.isDocumentType());
    }
    
    /**
     * Check if a file extension is supported for preview
     */
    public static boolean isPreviewSupported(String extension) {
        FileType fileType = FileType.fromExtension(extension);
        return isPreviewSupported(fileType);
    }
    
    /**
     * Get file category for organization
     */
    public static FileCategory getFileCategory(FileType fileType) {
        if (fileType.isMediaType()) {
            return FileCategory.MEDIA;
        } else if (fileType.isCodeType()) {
            return FileCategory.CODE;
        } else if (fileType.isTextType()) {
            return FileCategory.TEXT;
        } else if (fileType.isImageType()) {
            return FileCategory.IMAGE;
        } else if (fileType.isDocumentType()) {
            return FileCategory.DOCUMENT;
        } else {
            return FileCategory.OTHER;
        }
    }
    
    /**
     * File category enumeration for organization
     */
    public enum FileCategory {
        MEDIA("Media Files", "🎵"),
        CODE("Code Files", "💻"),
        TEXT("Text Files", "📝"),
        IMAGE("Image Files", "🖼️"),
        DOCUMENT("Document Files", "📄"),
        OTHER("Other Files", "📁");
        
        private final String displayName;
        private final String icon;
        
        FileCategory(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getIcon() {
            return icon;
        }
    }
}