package com.ghostvault.ui.components;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates thumbnails for various file types (simplified JavaFX-only version)
 */
public class ThumbnailGenerator {
    
    // Thumbnail cache
    private static final Map<String, Image> thumbnailCache = new HashMap<>();
    
    // Maximum cache size
    private static final int MAX_CACHE_SIZE = 1000;
    
    /**
     * Generate thumbnail for a file
     */
    public static Image generateThumbnail(File file, int size) {
        if (file == null || !file.exists()) {
            return null;
        }
        
        String cacheKey = file.getAbsolutePath() + "_" + size + "_" + file.lastModified();
        
        // Check cache first
        if (thumbnailCache.containsKey(cacheKey)) {
            return thumbnailCache.get(cacheKey);
        }
        
        Image thumbnail = null;
        
        if (file.isDirectory()) {
            thumbnail = generateFolderThumbnail(file, size);
        } else {
            String extension = getFileExtension(file.getName()).toLowerCase();
            
            if (isImageFile(extension)) {
                thumbnail = generateImageThumbnail(file, size);
            } else {
                // For non-image files, return null to use default icons
                thumbnail = null;
            }
        }
        
        // Cache the thumbnail
        if (thumbnail != null) {
            cacheThumbnail(cacheKey, thumbnail);
        }
        
        return thumbnail;
    }
    
    /**
     * Generate thumbnail for image files
     */
    private static Image generateImageThumbnail(File file, int size) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Image originalImage = new Image(fis, size, size, true, true);
            fis.close();
            return originalImage;
            
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Generate thumbnail for folders
     */
    private static Image generateFolderThumbnail(File folder, int size) {
        // Return null to use default folder icon
        return null;
    }
    
    /**
     * Check if file is an image
     */
    private static boolean isImageFile(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("bmp") || extension.equals("webp");
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    /**
     * Cache thumbnail with size limit
     */
    private static void cacheThumbnail(String key, Image thumbnail) {
        if (thumbnailCache.size() >= MAX_CACHE_SIZE) {
            // Simple cache eviction - remove first entry
            String firstKey = thumbnailCache.keySet().iterator().next();
            thumbnailCache.remove(firstKey);
        }
        thumbnailCache.put(key, thumbnail);
    }
    
    /**
     * Clear thumbnail cache
     */
    public static void clearCache() {
        thumbnailCache.clear();
    }
    
    /**
     * Get cache size
     */
    public static int getCacheSize() {
        return thumbnailCache.size();
    }
}