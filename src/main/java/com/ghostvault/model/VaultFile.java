package com.ghostvault.model;

import com.ghostvault.ui.preview.FileTypeDetector;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import java.io.Serializable;

/**
 * Represents a file stored in the vault with its metadata
 */
public class VaultFile implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String originalName;
    private final String fileId;
    private final String encryptedName;
    private final long size;
    private final String hash;
    private final long uploadTime;
    private String tags;
    
    public VaultFile(String originalName, String fileId, String encryptedName, 
                     long size, String hash, long uploadTime) {
        this.originalName = originalName;
        this.fileId = fileId;
        this.encryptedName = encryptedName;
        this.size = size;
        this.hash = hash;
        this.uploadTime = uploadTime;
        this.tags = "";
    }
    
    // Getters
    public String getOriginalName() { return originalName; }
    public String getFileId() { return fileId; }
    public String getEncryptedName() { return encryptedName; }
    public long getSize() { return size; }
    public String getHash() { return hash; }
    public long getUploadTime() { return uploadTime; }
    public String getTags() { return tags; }
    
    // Setters
    public void setTags(String tags) { 
        this.tags = tags != null ? tags : ""; 
    }
    
    /**
     * Get file extension from original name
     */
    public String getExtension() {
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalName.length() - 1) {
            return originalName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Check if file matches search query
     */
    public boolean matchesSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        return originalName.toLowerCase().contains(lowerQuery) ||
               tags.toLowerCase().contains(lowerQuery);
    }
    
    /**
     * Get display name with size for UI
     */
    public String getDisplayName() {
        return originalName + " (" + formatFileSize(size) + ")";
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Get file category based on enhanced file type detection
     */
    public FileCategory getCategory() {
        FileType fileType = FileTypeDetector.detectFileType(this);
        FileTypeDetector.FileCategory detectedCategory = FileTypeDetector.getFileCategory(fileType);
        
        // Convert FileTypeDetector.FileCategory to VaultFile.FileCategory
        switch (detectedCategory) {
            case MEDIA: return FileCategory.MEDIA;
            case CODE: return FileCategory.CODE;
            case TEXT: return FileCategory.TEXT;
            case IMAGE: return FileCategory.IMAGE;
            case DOCUMENT: return FileCategory.DOCUMENT;
            default: return FileCategory.OTHER;
        }
    }
    
    /**
     * Get detected file type
     */
    public FileType getFileType() {
        return FileTypeDetector.detectFileType(this);
    }
    
    /**
     * Get code language if this is a code file
     */
    public CodeLanguage getCodeLanguage() {
        FileType fileType = getFileType();
        return CodeLanguage.fromFileType(fileType);
    }
    
    /**
     * Check if this is a media file (audio or video)
     */
    public boolean isMediaFile() {
        FileType fileType = getFileType();
        return fileType.isMediaType();
    }
    
    /**
     * Check if this is a code file
     */
    public boolean isCodeFile() {
        FileType fileType = getFileType();
        return fileType.isCodeType();
    }
    
    /**
     * Check if this is a text file
     */
    public boolean isTextFile() {
        FileType fileType = getFileType();
        return fileType.isTextType();
    }
    
    /**
     * Check if this is an image file
     */
    public boolean isImageFile() {
        FileType fileType = getFileType();
        return fileType.isImageType();
    }
    
    /**
     * Check if this is a document file
     */
    public boolean isDocumentFile() {
        FileType fileType = getFileType();
        return fileType.isDocumentType();
    }
    
    /**
     * Get MIME type for this file
     */
    public String getMimeType() {
        FileType fileType = getFileType();
        return FileTypeDetector.getMimeTypeForFileType(fileType);
    }
    
    /**
     * Check if this file supports preview
     */
    public boolean isPreviewSupported() {
        return FileTypeDetector.isPreviewSupported(getFileType());
    }
    
    /**
     * Get file icon based on enhanced file type detection
     */
    public String getIcon() {
        FileCategory category = getCategory();
        FileType fileType = getFileType();
        
        // Use category-based icons with specific overrides
        switch (category) {
            case MEDIA:
                if (fileType.name().startsWith("AUDIO_")) {
                    return "🎵";
                } else if (fileType.name().startsWith("VIDEO_")) {
                    return "🎬";
                }
                return "🎵"; // Default media icon
                
            case CODE:
                return "💻";
                
            case TEXT:
                if (fileType == FileType.TEXT_MARKDOWN) {
                    return "📝";
                }
                return "📋";
                
            case IMAGE:
                return "🖼️";
                
            case DOCUMENT:
                return "📄";
                
            default:
                // Legacy icon mapping for backward compatibility
                switch (getExtension()) {
                    case "doc": case "docx": return "📝";
                    case "xls": case "xlsx": return "📊";
                    case "ppt": case "pptx": return "📊";
                    case "zip": case "rar": case "7z": case "tar": case "gz": return "📦";
                    case "exe": case "msi": case "dmg": return "⚙️";
                    default: return "📁";
                }
        }
    }
    
    @Override
    public String toString() {
        return "VaultFile{" +
                "originalName='" + originalName + '\'' +
                ", fileId='" + fileId + '\'' +
                ", size=" + size +
                ", uploadTime=" + uploadTime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VaultFile vaultFile = (VaultFile) obj;
        return fileId.equals(vaultFile.fileId);
    }
    
    @Override
    public int hashCode() {
        return fileId.hashCode();
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
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    /**
     * Code language enumeration for syntax highlighting
     */
    public enum CodeLanguage {
        JAVA("Java", "java"),
        PYTHON("Python", "py"),
        JAVASCRIPT("JavaScript", "js"),
        TYPESCRIPT("TypeScript", "ts"),
        HTML("HTML", "html"),
        CSS("CSS", "css"),
        XML("XML", "xml"),
        JSON("JSON", "json"),
        YAML("YAML", "yaml", "yml"),
        SQL("SQL", "sql"),
        SHELL("Shell Script", "sh"),
        BATCH("Batch File", "bat"),
        POWERSHELL("PowerShell", "ps1"),
        MARKDOWN("Markdown", "md"),
        PLAIN_TEXT("Plain Text", "txt"),
        UNKNOWN("Unknown", "");
        
        private final String displayName;
        private final String[] extensions;
        
        CodeLanguage(String displayName, String... extensions) {
            this.displayName = displayName;
            this.extensions = extensions;
        }
        
        public String getDisplayName() { return displayName; }
        public String[] getExtensions() { return extensions; }
        
        /**
         * Get CodeLanguage from FileType
         */
        public static CodeLanguage fromFileType(FileType fileType) {
            switch (fileType) {
                case CODE_JAVA: return JAVA;
                case CODE_PYTHON: return PYTHON;
                case CODE_JAVASCRIPT: return JAVASCRIPT;
                case CODE_TYPESCRIPT: return TYPESCRIPT;
                case CODE_HTML: return HTML;
                case CODE_CSS: return CSS;
                case CODE_XML: return XML;
                case CODE_JSON: return JSON;
                case CODE_YAML:
                case CODE_YML: return YAML;
                case CODE_SQL: return SQL;
                case CODE_SHELL: return SHELL;
                case CODE_BATCH: return BATCH;
                case CODE_POWERSHELL: return POWERSHELL;
                case TEXT_MARKDOWN: return MARKDOWN;
                case TEXT_PLAIN: return PLAIN_TEXT;
                default: return UNKNOWN;
            }
        }
        
        /**
         * Get CodeLanguage from file extension
         */
        public static CodeLanguage fromExtension(String extension) {
            if (extension == null || extension.isEmpty()) {
                return UNKNOWN;
            }
            
            String ext = extension.toLowerCase();
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            
            for (CodeLanguage lang : values()) {
                for (String langExt : lang.extensions) {
                    if (langExt.equals(ext)) {
                        return lang;
                    }
                }
            }
            
            return UNKNOWN;
        }
    }
}