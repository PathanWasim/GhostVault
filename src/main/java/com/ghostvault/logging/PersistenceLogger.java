package com.ghostvault.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Specialized logger for file persistence operations
 */
public class PersistenceLogger {
    
    private static final String LOG_FILE = System.getProperty("user.home") + "/.ghostvault/logs/persistence.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log file save operation
     */
    public static void logFileSave(String fileName, String fileId, long fileSize, boolean success, String errorMessage) {
        String logEntry = String.format("[%s] FILE_SAVE: name=%s, id=%s, size=%d, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            fileName,
            fileId,
            fileSize,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("💾 " + logEntry);
    }
    
    /**
     * Log file load operation
     */
    public static void logFileLoad(String fileName, String fileId, long fileSize, boolean success, String source) {
        String logEntry = String.format("[%s] FILE_LOAD: name=%s, id=%s, size=%d, success=%s, source=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            fileName,
            fileId,
            fileSize,
            success,
            source // "memory", "disk", "failed"
        );
        
        writeLogEntry(logEntry);
        System.out.println("📁 " + logEntry);
    }
    
    /**
     * Log metadata save operation
     */
    public static void logMetadataSave(int fileCount, long metadataSize, boolean success, String errorMessage) {
        String logEntry = String.format("[%s] METADATA_SAVE: file_count=%d, size=%d, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            fileCount,
            metadataSize,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("📋 " + logEntry);
    }
    
    /**
     * Log metadata load operation
     */
    public static void logMetadataLoad(int fileCount, boolean success, String errorMessage) {
        String logEntry = String.format("[%s] METADATA_LOAD: file_count=%d, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            fileCount,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("📋 " + logEntry);
    }
    
    /**
     * Log vault structure initialization
     */
    public static void logVaultInitialization(String vaultPath, boolean success, String errorMessage) {
        String logEntry = String.format("[%s] VAULT_INIT: path=%s, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            vaultPath,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("🏗️ " + logEntry);
    }
    
    /**
     * Log storage verification results
     */
    public static void logStorageVerification(boolean vaultExists, boolean writable, boolean filesDir, 
                                            boolean metadata, int fileCount, String errorMessage) {
        String logEntry = String.format("[%s] STORAGE_VERIFY: vault=%s, writable=%s, files_dir=%s, metadata=%s, file_count=%d, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            vaultExists,
            writable,
            filesDir,
            metadata,
            fileCount,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("🔍 " + logEntry);
    }
    
    /**
     * Write log entry to file
     */
    private static void writeLogEntry(String logEntry) {
        try {
            // Ensure log directory exists
            Files.createDirectories(Paths.get(LOG_FILE).getParent());
            
            // Append to log file
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(logEntry + System.lineSeparator());
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("⚠️ Failed to write persistence log: " + e.getMessage());
        }
    }
}