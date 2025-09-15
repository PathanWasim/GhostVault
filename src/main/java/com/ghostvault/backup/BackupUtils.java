package com.ghostvault.backup;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for backup operations
 */
public class BackupUtils {
    
    private static final String BACKUP_EXTENSION = ".gvbackup";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Generate default backup filename with timestamp
     */
    public static String generateBackupFilename() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return "vault_backup_" + timestamp + BACKUP_EXTENSION;
    }
    
    /**
     * Generate backup filename with custom prefix
     */
    public static String generateBackupFilename(String prefix) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return prefix + "_" + timestamp + BACKUP_EXTENSION;
    }
    
    /**
     * Ensure file has correct backup extension
     */
    public static File ensureBackupExtension(File file) {
        if (file.getName().endsWith(BACKUP_EXTENSION)) {
            return file;
        }
        
        String newName = file.getName() + BACKUP_EXTENSION;
        return new File(file.getParent(), newName);
    }
    
    /**
     * Check if file is a valid backup file
     */
    public static boolean isBackupFile(File file) {
        return file != null && file.getName().endsWith(BACKUP_EXTENSION);
    }
    
    /**
     * Format file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Validate backup file name
     */
    public static boolean isValidBackupFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // Check for invalid characters
        String invalidChars = "<>:\"/\\|?*";
        for (char c : invalidChars.toCharArray()) {
            if (fileName.indexOf(c) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get backup file info without full verification
     */
    public static BackupFileInfo getBackupFileInfo(File backupFile) {
        BackupFileInfo info = new BackupFileInfo();
        info.setFileName(backupFile.getName());
        info.setFilePath(backupFile.getAbsolutePath());
        info.setFileSize(backupFile.length());
        info.setLastModified(LocalDateTime.ofEpochSecond(
            backupFile.lastModified() / 1000, 0, java.time.ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now())
        ));
        info.setExists(backupFile.exists());
        info.setReadable(backupFile.canRead());
        
        return info;
    }
    
    /**
     * Create backup directory if it doesn't exist
     */
    public static boolean createBackupDirectory(File directory) {
        if (directory.exists()) {
            return directory.isDirectory();
        }
        
        return directory.mkdirs();
    }
    
    /**
     * Basic backup file info
     */
    public static class BackupFileInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime lastModified;
        private boolean exists;
        private boolean readable;
        
        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
        
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
        
        public boolean isReadable() { return readable; }
        public void setReadable(boolean readable) { this.readable = readable; }
        
        public String getFormattedSize() {
            return formatFileSize(fileSize);
        }
        
        @Override
        public String toString() {
            return String.format("BackupFileInfo{name='%s', size=%s, modified=%s, exists=%s}", 
                fileName, getFormattedSize(), lastModified, exists);
        }
    }
}