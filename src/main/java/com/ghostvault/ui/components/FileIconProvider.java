package com.ghostvault.ui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides file type icons and preview thumbnails for different file types
 */
public class FileIconProvider {
    
    // Icon cache
    private static final Map<String, Image> iconCache = new HashMap<>();
    
    // Icon paths
    private static final String ICON_BASE_PATH = "/icons/";
    
    // File type mappings
    private static final Map<String, String> FILE_TYPE_ICONS = new HashMap<>();
    
    static {
        initializeFileTypeIcons();
    }
    
    /**
     * Initialize file type to icon mappings
     */
    private static void initializeFileTypeIcons() {
        // Folder
        FILE_TYPE_ICONS.put("folder", "folder.png");
        
        // Images
        FILE_TYPE_ICONS.put("jpg", "image.png");
        FILE_TYPE_ICONS.put("jpeg", "image.png");
        FILE_TYPE_ICONS.put("png", "image.png");
        FILE_TYPE_ICONS.put("gif", "image.png");
        FILE_TYPE_ICONS.put("bmp", "image.png");
        FILE_TYPE_ICONS.put("svg", "image.png");
        FILE_TYPE_ICONS.put("webp", "image.png");
        FILE_TYPE_ICONS.put("tiff", "image.png");
        FILE_TYPE_ICONS.put("tif", "image.png");
        
        // Videos
        FILE_TYPE_ICONS.put("mp4", "video.png");
        FILE_TYPE_ICONS.put("avi", "video.png");
        FILE_TYPE_ICONS.put("mkv", "video.png");
        FILE_TYPE_ICONS.put("mov", "video.png");
        FILE_TYPE_ICONS.put("wmv", "video.png");
        FILE_TYPE_ICONS.put("flv", "video.png");
        FILE_TYPE_ICONS.put("webm", "video.png");
        FILE_TYPE_ICONS.put("m4v", "video.png");
        
        // Audio
        FILE_TYPE_ICONS.put("mp3", "audio.png");
        FILE_TYPE_ICONS.put("wav", "audio.png");
        FILE_TYPE_ICONS.put("flac", "audio.png");
        FILE_TYPE_ICONS.put("aac", "audio.png");
        FILE_TYPE_ICONS.put("ogg", "audio.png");
        FILE_TYPE_ICONS.put("wma", "audio.png");
        FILE_TYPE_ICONS.put("m4a", "audio.png");
        
        // Documents
        FILE_TYPE_ICONS.put("pdf", "pdf.png");
        FILE_TYPE_ICONS.put("doc", "word.png");
        FILE_TYPE_ICONS.put("docx", "word.png");
        FILE_TYPE_ICONS.put("xls", "excel.png");
        FILE_TYPE_ICONS.put("xlsx", "excel.png");
        FILE_TYPE_ICONS.put("ppt", "powerpoint.png");
        FILE_TYPE_ICONS.put("pptx", "powerpoint.png");
        FILE_TYPE_ICONS.put("txt", "text.png");
        FILE_TYPE_ICONS.put("rtf", "text.png");
        FILE_TYPE_ICONS.put("odt", "text.png");
        
        // Archives
        FILE_TYPE_ICONS.put("zip", "archive.png");
        FILE_TYPE_ICONS.put("rar", "archive.png");
        FILE_TYPE_ICONS.put("7z", "archive.png");
        FILE_TYPE_ICONS.put("tar", "archive.png");
        FILE_TYPE_ICONS.put("gz", "archive.png");
        FILE_TYPE_ICONS.put("bz2", "archive.png");
        
        // Code files
        FILE_TYPE_ICONS.put("java", "code.png");
        FILE_TYPE_ICONS.put("py", "python.png");
        FILE_TYPE_ICONS.put("js", "javascript.png");
        FILE_TYPE_ICONS.put("html", "html.png");
        FILE_TYPE_ICONS.put("css", "css.png");
        FILE_TYPE_ICONS.put("cpp", "code.png");
        FILE_TYPE_ICONS.put("c", "code.png");
        FILE_TYPE_ICONS.put("h", "code.png");
        FILE_TYPE_ICONS.put("php", "php.png");
        FILE_TYPE_ICONS.put("rb", "ruby.png");
        FILE_TYPE_ICONS.put("go", "go.png");
        FILE_TYPE_ICONS.put("rs", "rust.png");
        
        // Configuration and data files
        FILE_TYPE_ICONS.put("json", "json.png");
        FILE_TYPE_ICONS.put("xml", "xml.png");
        FILE_TYPE_ICONS.put("yaml", "config.png");
        FILE_TYPE_ICONS.put("yml", "config.png");
        FILE_TYPE_ICONS.put("ini", "config.png");
        FILE_TYPE_ICONS.put("cfg", "config.png");
        FILE_TYPE_ICONS.put("conf", "config.png");
        FILE_TYPE_ICONS.put("log", "log.png");
        FILE_TYPE_ICONS.put("md", "markdown.png");
        
        // Executables
        FILE_TYPE_ICONS.put("exe", "executable.png");
        FILE_TYPE_ICONS.put("msi", "executable.png");
        FILE_TYPE_ICONS.put("app", "executable.png");
        FILE_TYPE_ICONS.put("deb", "executable.png");
        FILE_TYPE_ICONS.put("rpm", "executable.png");
        FILE_TYPE_ICONS.put("dmg", "executable.png");
        
        // Default
        FILE_TYPE_ICONS.put("default", "file.png");
    }
    
    /**
     * Get icon for a file
     */
    public static ImageView getFileIcon(File file, int size) {
        Image icon = getFileIconImage(file);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    
    /**
     * Get icon image for a file
     */
    public static Image getFileIconImage(File file) {
        String iconKey = getIconKey(file);
        
        // Check cache first
        if (iconCache.containsKey(iconKey)) {
            return iconCache.get(iconKey);
        }
        
        // Load icon
        String iconPath = getIconPath(file);
        Image icon = loadIcon(iconPath);
        
        // Cache the icon
        iconCache.put(iconKey, icon);
        
        return icon;
    }
    
    /**
     * Get icon path for a file
     */
    public static String getIconPath(File file) {
        if (file.isDirectory()) {
            return ICON_BASE_PATH + FILE_TYPE_ICONS.get("folder");
        }
        
        String extension = getFileExtension(file.getName()).toLowerCase();
        String iconFileName = FILE_TYPE_ICONS.getOrDefault(extension, FILE_TYPE_ICONS.get("default"));
        
        return ICON_BASE_PATH + iconFileName;
    }
    
    /**
     * Load icon from resources
     */
    private static Image loadIcon(String iconPath) {
        try {
            return new Image(FileIconProvider.class.getResourceAsStream(iconPath));
        } catch (Exception e) {
            // Return default icon if specific icon not found
            try {
                return new Image(FileIconProvider.class.getResourceAsStream(ICON_BASE_PATH + "file.png"));
            } catch (Exception ex) {
                // Create a simple colored rectangle as fallback
                return createFallbackIcon();
            }
        }
    }
    
    /**
     * Create a simple fallback icon
     */
    private static Image createFallbackIcon() {
        // This would create a simple colored rectangle
        // For now, return null and handle in the UI
        return null;
    }
    
    /**
     * Get cache key for a file
     */
    private static String getIconKey(File file) {
        if (file.isDirectory()) {
            return "folder";
        }
        
        String extension = getFileExtension(file.getName()).toLowerCase();
        return extension.isEmpty() ? "default" : extension;
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0 && lastDot < fileName.length() - 1) ? 
               fileName.substring(lastDot + 1) : "";
    }
    
    /**
     * Get file type category
     */
    public static String getFileTypeCategory(File file) {
        if (file.isDirectory()) {
            return "Folder";
        }
        
        String extension = getFileExtension(file.getName()).toLowerCase();
        
        // Images
        if (extension.matches("jpg|jpeg|png|gif|bmp|svg|webp|tiff|tif")) {
            return "Image";
        }
        
        // Videos
        if (extension.matches("mp4|avi|mkv|mov|wmv|flv|webm|m4v")) {
            return "Video";
        }
        
        // Audio
        if (extension.matches("mp3|wav|flac|aac|ogg|wma|m4a")) {
            return "Audio";
        }
        
        // Documents
        if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt")) {
            return "Document";
        }
        
        // Archives
        if (extension.matches("zip|rar|7z|tar|gz|bz2")) {
            return "Archive";
        }
        
        // Code files
        if (extension.matches("java|py|js|html|css|cpp|c|h|php|rb|go|rs")) {
            return "Code";
        }
        
        // Configuration
        if (extension.matches("json|xml|yaml|yml|ini|cfg|conf|log|md")) {
            return "Configuration";
        }
        
        // Executables
        if (extension.matches("exe|msi|app|deb|rpm|dmg")) {
            return "Executable";
        }
        
        return "File";
    }
    
    /**
     * Check if file type supports thumbnails
     */
    public static boolean supportsThumbnails(File file) {
        if (file.isDirectory()) {
            return false;
        }
        
        String extension = getFileExtension(file.getName()).toLowerCase();
        
        // Images support thumbnails
        return extension.matches("jpg|jpeg|png|gif|bmp|svg|webp|tiff|tif");
    }
    
    /**
     * Get color for file type
     */
    public static String getFileTypeColor(File file) {
        String category = getFileTypeCategory(file);
        
        switch (category) {
            case "Folder":
                return "#4A90E2"; // Blue
            case "Image":
                return "#7ED321"; // Green
            case "Video":
                return "#F5A623"; // Orange
            case "Audio":
                return "#9013FE"; // Purple
            case "Document":
                return "#D0021B"; // Red
            case "Archive":
                return "#50E3C2"; // Teal
            case "Code":
                return "#BD10E0"; // Magenta
            case "Configuration":
                return "#B8E986"; // Light Green
            case "Executable":
                return "#417505"; // Dark Green
            default:
                return "#9B9B9B"; // Gray
        }
    }
    
    /**
     * Clear icon cache
     */
    public static void clearCache() {
        iconCache.clear();
    }
    
    /**
     * Get cache size
     */
    public static int getCacheSize() {
        return iconCache.size();
    }
}