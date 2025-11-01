package com.ghostvault.util;

import com.ghostvault.security.CryptoManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/**
 * Simple file utilities for encrypted file operations
 */
public class FileUtils {
    
    /**
     * Read encrypted file data
     */
    public static CryptoManager.EncryptedData readEncryptedFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return null;
        }
        
        byte[] data = Files.readAllBytes(filePath);
        // Simple implementation - in real version this would parse the encrypted format
        return new CryptoManager.EncryptedData(data, new byte[16]); // dummy iv
    }
    
    /**
     * Write encrypted file data
     */
    public static void writeEncryptedFile(Path filePath, CryptoManager.EncryptedData encryptedData) throws IOException {
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, encryptedData.getCiphertext());
    }
    
    /**
     * Format file size in human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}