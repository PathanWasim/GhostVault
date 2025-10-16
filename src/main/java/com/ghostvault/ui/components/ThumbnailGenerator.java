package com.ghostvault.ui.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates thumbnails for various file types
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
            } else if (isVideoFile(extension)) {
                thumbnail = generateVideoThumbnail(file, size);
            } else if (isDocumentFile(extension)) {
                thumbnail = generateDocumentThumbnail(file, size);
            } else {
                thumbnail = generateGenericThumbnail(file, size);
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
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                return null;
            }
            
            BufferedImage thumbnail = createThumbnail(originalImage, size);
            return SwingFXUtils.toFXImage(thumbnail, null);
            
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Generate thumbnail for video files
     */
    private static Image generateVideoThumbnail(File file, int size) {
        // For now, return a video icon
        // In a full implementation, you would extract a frame from the video
        return createIconThumbnail("🎬", size, "#F5A623");
    }
    
    /**
     * Generate thumbnail for document files
     */
    private static Image generateDocumentThumbnail(File file, int size) {
        String extension = getFileExtension(file.getName()).toLowerCase();
        String icon;
        String color;
        
        switch (extension) {
            case "pdf":
                icon = "📄";
                color = "#D0021B";
                break;
            case "doc":
            case "docx":
                icon = "📝";
                color = "#2B579A";
                break;
            case "xls":
            case "xlsx":
                icon = "📊";
                color = "#217346";
                break;
            case "ppt":
            case "pptx":
                icon = "📈";
                color = "#D24726";
                break;
            default:
                icon = "📄";
                color = "#9B9B9B";
        }
        
        return createIconThumbnail(icon, size, color);
    }
    
    /**
     * Generate thumbnail for folder
     */
    private static Image generateFolderThumbnail(File folder, int size) {
        // Count items in folder
        File[] children = folder.listFiles();
        int itemCount = children != null ? children.length : 0;
        
        return createFolderThumbnail(size, itemCount);
    }
    
    /**
     * Generate generic thumbnail for unknown file types
     */
    private static Image generateGenericThumbnail(File file, int size) {
        String extension = getFileExtension(file.getName()).toLowerCase();
        String color = FileIconProvider.getFileTypeColor(file);
        
        if (extension.isEmpty()) {
            return createIconThumbnail("📄", size, color);
        } else {
            return createTextThumbnail(extension.toUpperCase(), size, color);
        }
    }
    
    /**
     * Create thumbnail from BufferedImage
     */
    private static BufferedImage createThumbnail(BufferedImage original, int size) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Calculate dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int thumbnailWidth, thumbnailHeight;
        
        if (aspectRatio > 1) {
            thumbnailWidth = size;
            thumbnailHeight = (int) (size / aspectRatio);
        } else {
            thumbnailWidth = (int) (size * aspectRatio);
            thumbnailHeight = size;
        }
        
        // Create thumbnail
        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, size, size);
        
        // Center the image
        int x = (size - thumbnailWidth) / 2;
        int y = (size - thumbnailHeight) / 2;
        
        // Draw the scaled image
        g2d.drawImage(original.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH), x, y, null);
        
        g2d.dispose();
        return thumbnail;
    }
    
    /**
     * Create icon-based thumbnail
     */
    private static Image createIconThumbnail(String icon, int size, String colorHex) {
        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background
        Color bgColor = Color.decode(colorHex);
        Color lightBg = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 30);
        g2d.setColor(lightBg);
        g2d.fillRoundRect(0, 0, size, size, size / 8, size / 8);
        
        // Draw border
        g2d.setColor(bgColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, size - 2, size - 2, size / 8, size / 8);
        
        // Draw icon
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, size / 2);
        g2d.setFont(font);
        g2d.setColor(bgColor);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(icon);
        int textHeight = fm.getAscent();
        
        int x = (size - textWidth) / 2;
        int y = (size + textHeight) / 2;
        
        g2d.drawString(icon, x, y);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(thumbnail, null);
    }
    
    /**
     * Create text-based thumbnail
     */
    private static Image createTextThumbnail(String text, int size, String colorHex) {
        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background
        Color bgColor = Color.decode(colorHex);
        Color lightBg = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 30);
        g2d.setColor(lightBg);
        g2d.fillRoundRect(0, 0, size, size, size / 8, size / 8);
        
        // Draw border
        g2d.setColor(bgColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, size - 2, size - 2, size / 8, size / 8);
        
        // Draw text
        Font font = new Font("Arial", Font.BOLD, Math.max(8, size / 6));
        g2d.setFont(font);
        g2d.setColor(bgColor);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        
        int x = (size - textWidth) / 2;
        int y = (size + textHeight) / 2;
        
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(thumbnail, null);
    }
    
    /**
     * Create folder thumbnail
     */
    private static Image createFolderThumbnail(int size, int itemCount) {
        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw folder icon
        Color folderColor = Color.decode("#4A90E2");
        Color lightBg = new Color(folderColor.getRed(), folderColor.getGreen(), folderColor.getBlue(), 30);
        
        g2d.setColor(lightBg);
        g2d.fillRoundRect(0, 0, size, size, size / 8, size / 8);
        
        g2d.setColor(folderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, size - 2, size - 2, size / 8, size / 8);
        
        // Draw folder icon
        String folderIcon = "📁";
        Font iconFont = new Font("Segoe UI Emoji", Font.PLAIN, size / 2);
        g2d.setFont(iconFont);
        
        FontMetrics fm = g2d.getFontMetrics();
        int iconWidth = fm.stringWidth(folderIcon);
        int iconHeight = fm.getAscent();
        
        int iconX = (size - iconWidth) / 2;
        int iconY = (size + iconHeight) / 2 - size / 8;
        
        g2d.drawString(folderIcon, iconX, iconY);
        
        // Draw item count if > 0
        if (itemCount > 0) {
            String countText = String.valueOf(itemCount);
            Font countFont = new Font("Arial", Font.BOLD, Math.max(8, size / 8));
            g2d.setFont(countFont);
            
            FontMetrics countFm = g2d.getFontMetrics();
            int countWidth = countFm.stringWidth(countText);
            int countHeight = countFm.getAscent();
            
            int countX = (size - countWidth) / 2;
            int countY = size - size / 6;
            
            g2d.drawString(countText, countX, countY);
        }
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(thumbnail, null);
    }
    
    /**
     * Cache thumbnail with size limit
     */
    private static void cacheThumbnail(String key, Image thumbnail) {
        if (thumbnailCache.size() >= MAX_CACHE_SIZE) {
            // Remove oldest entries (simple FIFO)
            String firstKey = thumbnailCache.keySet().iterator().next();
            thumbnailCache.remove(firstKey);
        }
        
        thumbnailCache.put(key, thumbnail);
    }
    
    // Helper methods
    
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0 && lastDot < fileName.length() - 1) ? 
               fileName.substring(lastDot + 1) : "";
    }
    
    private static boolean isImageFile(String extension) {
        return extension.matches("jpg|jpeg|png|gif|bmp|svg|webp|tiff|tif");
    }
    
    private static boolean isVideoFile(String extension) {
        return extension.matches("mp4|avi|mkv|mov|wmv|flv|webm|m4v");
    }
    
    private static boolean isDocumentFile(String extension) {
        return extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt");
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