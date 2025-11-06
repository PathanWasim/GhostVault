package com.ghostvault.util;

import com.ghostvault.ui.preview.FileTypeDetector;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MIME type detection utility using Apache Tika
 * Provides accurate MIME type detection for various file formats
 */
public class MimeTypeDetector {
    
    private static final Tika tika = new Tika();
    
    // Common MIME type mappings for quick lookup
    private static final Map<String, String> EXTENSION_MIME_MAP = new HashMap<>();
    
    static {
        // Audio MIME types
        EXTENSION_MIME_MAP.put("mp3", "audio/mpeg");
        EXTENSION_MIME_MAP.put("wav", "audio/wav");
        EXTENSION_MIME_MAP.put("aac", "audio/aac");
        EXTENSION_MIME_MAP.put("m4a", "audio/mp4");
        EXTENSION_MIME_MAP.put("flac", "audio/flac");
        EXTENSION_MIME_MAP.put("ogg", "audio/ogg");
        
        // Video MIME types
        EXTENSION_MIME_MAP.put("mp4", "video/mp4");
        EXTENSION_MIME_MAP.put("avi", "video/avi");
        EXTENSION_MIME_MAP.put("mov", "video/quicktime");
        EXTENSION_MIME_MAP.put("mkv", "video/x-matroska");
        EXTENSION_MIME_MAP.put("m4v", "video/x-m4v");
        EXTENSION_MIME_MAP.put("wmv", "video/x-ms-wmv");
        EXTENSION_MIME_MAP.put("flv", "video/x-flv");
        
        // Image MIME types
        EXTENSION_MIME_MAP.put("jpg", "image/jpeg");
        EXTENSION_MIME_MAP.put("jpeg", "image/jpeg");
        EXTENSION_MIME_MAP.put("png", "image/png");
        EXTENSION_MIME_MAP.put("gif", "image/gif");
        EXTENSION_MIME_MAP.put("bmp", "image/bmp");
        EXTENSION_MIME_MAP.put("webp", "image/webp");
        EXTENSION_MIME_MAP.put("svg", "image/svg+xml");
        EXTENSION_MIME_MAP.put("ico", "image/x-icon");
        
        // Document MIME types
        EXTENSION_MIME_MAP.put("pdf", "application/pdf");
        EXTENSION_MIME_MAP.put("doc", "application/msword");
        EXTENSION_MIME_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_MIME_MAP.put("xls", "application/vnd.ms-excel");
        EXTENSION_MIME_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_MIME_MAP.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_MIME_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        
        // Text MIME types
        EXTENSION_MIME_MAP.put("txt", "text/plain");
        EXTENSION_MIME_MAP.put("md", "text/markdown");
        EXTENSION_MIME_MAP.put("json", "application/json");
        EXTENSION_MIME_MAP.put("xml", "application/xml");
        EXTENSION_MIME_MAP.put("yaml", "application/x-yaml");
        EXTENSION_MIME_MAP.put("yml", "application/x-yaml");
        EXTENSION_MIME_MAP.put("csv", "text/csv");
        EXTENSION_MIME_MAP.put("ini", "text/plain");
        EXTENSION_MIME_MAP.put("cfg", "text/plain");
        EXTENSION_MIME_MAP.put("conf", "text/plain");
        EXTENSION_MIME_MAP.put("properties", "text/plain");
        EXTENSION_MIME_MAP.put("toml", "text/plain");
        
        // Code MIME types
        EXTENSION_MIME_MAP.put("java", "text/x-java-source");
        EXTENSION_MIME_MAP.put("py", "text/x-python");
        EXTENSION_MIME_MAP.put("js", "application/javascript");
        EXTENSION_MIME_MAP.put("ts", "application/typescript");
        EXTENSION_MIME_MAP.put("html", "text/html");
        EXTENSION_MIME_MAP.put("htm", "text/html");
        EXTENSION_MIME_MAP.put("css", "text/css");
        EXTENSION_MIME_MAP.put("sql", "application/sql");
        EXTENSION_MIME_MAP.put("sh", "application/x-sh");
        EXTENSION_MIME_MAP.put("bat", "application/x-bat");
        EXTENSION_MIME_MAP.put("ps1", "application/x-powershell");
        EXTENSION_MIME_MAP.put("c", "text/x-c");
        EXTENSION_MIME_MAP.put("cpp", "text/x-c++");
        EXTENSION_MIME_MAP.put("h", "text/x-c");
        EXTENSION_MIME_MAP.put("hpp", "text/x-c++");
        EXTENSION_MIME_MAP.put("cs", "text/x-csharp");
        EXTENSION_MIME_MAP.put("php", "application/x-httpd-php");
        EXTENSION_MIME_MAP.put("rb", "application/x-ruby");
        EXTENSION_MIME_MAP.put("go", "text/x-go");
        EXTENSION_MIME_MAP.put("rs", "text/x-rust");
        
        // Archive MIME types
        EXTENSION_MIME_MAP.put("zip", "application/zip");
        EXTENSION_MIME_MAP.put("rar", "application/x-rar-compressed");
        EXTENSION_MIME_MAP.put("7z", "application/x-7z-compressed");
        EXTENSION_MIME_MAP.put("tar", "application/x-tar");
        EXTENSION_MIME_MAP.put("gz", "application/gzip");
        EXTENSION_MIME_MAP.put("bz2", "application/x-bzip2");
    }
    
    /**
     * Detect MIME type from file content using Apache Tika
     * @param fileData The file content bytes
     * @param filename The filename (optional, can be null)
     * @return The detected MIME type
     */
    public static String detectMimeType(byte[] fileData, String filename) {
        if (fileData == null || fileData.length == 0) {
            return getMimeTypeFromFilename(filename);
        }
        
        try {
            Metadata metadata = new Metadata();
            if (filename != null && !filename.isEmpty()) {
                // metadata.set(Metadata.RESOURCE_NAME_KEY, filename); // Constant not available
                // Using alternative approach without the constant
            }
            
            String mimeType = tika.detect(new ByteArrayInputStream(fileData), metadata);
            
            // If Tika returns a generic type, try filename-based detection
            if ("application/octet-stream".equals(mimeType) || "text/plain".equals(mimeType)) {
                String filenameMimeType = getMimeTypeFromFilename(filename);
                if (!"application/octet-stream".equals(filenameMimeType)) {
                    return filenameMimeType;
                }
            }
            
            return mimeType;
            
        } catch (IOException e) {
            // Fallback to filename-based detection
            return getMimeTypeFromFilename(filename);
        }
    }
    
    /**
     * Get MIME type from filename extension only
     * @param filename The filename
     * @return The MIME type based on file extension
     */
    public static String getMimeTypeFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }
        
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            return "application/octet-stream";
        }
        
        String mimeType = EXTENSION_MIME_MAP.get(extension.toLowerCase());
        return mimeType != null ? mimeType : "application/octet-stream";
    }
    
    /**
     * Extract file extension from filename
     * @param filename The filename
     * @return The file extension (without dot)
     */
    private static String extractExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        
        return "";
    }
    
    /**
     * Check if a MIME type represents a media file
     * @param mimeType The MIME type
     * @return true if it's a media file
     */
    public static boolean isMediaMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return mimeType.startsWith("audio/") || mimeType.startsWith("video/");
    }
    
    /**
     * Check if a MIME type represents an image file
     * @param mimeType The MIME type
     * @return true if it's an image file
     */
    public static boolean isImageMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return mimeType.startsWith("image/");
    }
    
    /**
     * Check if a MIME type represents a text file
     * @param mimeType The MIME type
     * @return true if it's a text file
     */
    public static boolean isTextMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return mimeType.startsWith("text/") || 
               mimeType.equals("application/json") ||
               mimeType.equals("application/xml") ||
               mimeType.equals("application/x-yaml") ||
               mimeType.equals("application/javascript") ||
               mimeType.equals("application/typescript");
    }
    
    /**
     * Check if a MIME type represents a document file
     * @param mimeType The MIME type
     * @return true if it's a document file
     */
    public static boolean isDocumentMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return mimeType.equals("application/pdf") ||
               mimeType.startsWith("application/msword") ||
               mimeType.startsWith("application/vnd.ms-") ||
               mimeType.startsWith("application/vnd.openxmlformats-");
    }
    
    /**
     * Get a human-readable description for a MIME type
     * @param mimeType The MIME type
     * @return A human-readable description
     */
    public static String getMimeTypeDescription(String mimeType) {
        if (mimeType == null) {
            return "Unknown file type";
        }
        
        switch (mimeType) {
            // Audio
            case "audio/mpeg": return "MP3 Audio";
            case "audio/wav": return "WAV Audio";
            case "audio/aac": return "AAC Audio";
            case "audio/mp4": return "M4A Audio";
            case "audio/flac": return "FLAC Audio";
            case "audio/ogg": return "OGG Audio";
            
            // Video
            case "video/mp4": return "MP4 Video";
            case "video/avi": return "AVI Video";
            case "video/quicktime": return "QuickTime Video";
            case "video/x-matroska": return "MKV Video";
            case "video/x-m4v": return "M4V Video";
            
            // Images
            case "image/jpeg": return "JPEG Image";
            case "image/png": return "PNG Image";
            case "image/gif": return "GIF Image";
            case "image/bmp": return "BMP Image";
            case "image/webp": return "WebP Image";
            case "image/svg+xml": return "SVG Image";
            
            // Documents
            case "application/pdf": return "PDF Document";
            case "application/msword": return "Word Document";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "Word Document";
            case "application/vnd.ms-excel": return "Excel Spreadsheet";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "Excel Spreadsheet";
            
            // Text/Code
            case "text/plain": return "Plain Text";
            case "text/markdown": return "Markdown";
            case "application/json": return "JSON";
            case "application/xml": return "XML";
            case "application/x-yaml": return "YAML";
            case "text/x-java-source": return "Java Source";
            case "text/x-python": return "Python Source";
            case "application/javascript": return "JavaScript";
            case "text/html": return "HTML";
            case "text/css": return "CSS";
            
            default:
                // Try to create a description from the MIME type
                if (mimeType.startsWith("audio/")) {
                    return "Audio File";
                } else if (mimeType.startsWith("video/")) {
                    return "Video File";
                } else if (mimeType.startsWith("image/")) {
                    return "Image File";
                } else if (mimeType.startsWith("text/")) {
                    return "Text File";
                } else {
                    return "Binary File";
                }
        }
    }
    
    /**
     * Check if a file is likely to be previewable based on its MIME type
     * @param mimeType The MIME type
     * @return true if the file is likely previewable
     */
    public static boolean isPreviewableMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return isMediaMimeType(mimeType) || 
               isImageMimeType(mimeType) || 
               isTextMimeType(mimeType) || 
               mimeType.equals("application/pdf");
    }
}