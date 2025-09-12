package com.ghostvault.util;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.CryptoManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility methods for file operations and validation
 */
public class FileUtils {
    
    /**
     * Validate if file is acceptable for vault storage
     */
    public static boolean isValidFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        // Check file name
        String fileName = file.getName();
        if (fileName.length() > AppConfig.MAX_FILENAME_LENGTH) {
            return false;
        }
        
        if (!fileName.matches(AppConfig.VALID_FILENAME_PATTERN)) {
            return false;
        }
        
        // Check if file starts or ends with dot (hidden files)
        if (fileName.startsWith(".") || fileName.endsWith(".")) {
            return false;
        }
        
        // Check file size (max 2GB for safety)
        if (file.length() > 2L * 1024 * 1024 * 1024) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate SHA-256 hash of file data
     */
    public static String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Format file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Write encrypted data to file
     */
    public static void writeEncryptedFile(Path filePath, CryptoManager.EncryptedData encryptedData) throws IOException {
        Files.write(filePath, encryptedData.getCombinedData());
    }
    
    /**
     * Read encrypted data from file
     */
    public static CryptoManager.EncryptedData readEncryptedFile(Path filePath) throws IOException {
        byte[] combinedData = Files.readAllBytes(filePath);
        return CryptoManager.EncryptedData.fromCombinedData(combinedData);
    }
    
    /**
     * Get file extension
     */
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Check if file extension is supported for preview
     */
    public static boolean isPreviewSupported(String extension) {
        switch (extension.toLowerCase()) {
            case "txt":
            case "md":
            case "log":
            case "csv":
            case "json":
            case "xml":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Sanitize filename for safe storage
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed_file";
        }
        
        // Replace invalid characters
        String sanitized = fileName.replaceAll("[^\\w\\-. ]", "_");
        
        // Limit length
        if (sanitized.length() > AppConfig.MAX_FILENAME_LENGTH) {
            String extension = getFileExtension(sanitized);
            int maxNameLength = AppConfig.MAX_FILENAME_LENGTH - extension.length() - 1;
            sanitized = sanitized.substring(0, maxNameLength) + "." + extension;
        }
        
        // Ensure it doesn't start or end with dot
        sanitized = sanitized.replaceAll("^\\.|\\.$", "_");
        
        return sanitized;
    }
    
    /**
     * Create directory if it doesn't exist
     */
    public static void ensureDirectoryExists(String dirPath) throws IOException {
        Path path = Path.of(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
    
    /**
     * Check if path is safe (no directory traversal)
     */
    public static boolean isSafePath(String path) {
        if (path == null) {
            return false;
        }
        
        // Check for directory traversal attempts
        return !path.contains("..") && 
               !path.contains("~") && 
               !path.startsWith("/") &&
               !path.matches("^[A-Za-z]:");
    }
}