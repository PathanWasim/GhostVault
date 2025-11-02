package com.ghostvault.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Specialized logger for UI interactions and context menu events
 */
public class UILogger {
    
    private static final String LOG_FILE = System.getProperty("user.home") + "/.ghostvault/logs/ui.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log context menu positioning event
     */
    public static void logContextMenuEvent(double clickX, double clickY, int calculatedIndex, 
                                         int totalItems, boolean success, String selectedItem) {
        String logEntry = String.format("[%s] CONTEXT_MENU: click_pos=(%.1f,%.1f), calculated_index=%d, total_items=%d, success=%s, selected=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            clickX,
            clickY,
            calculatedIndex,
            totalItems,
            success,
            selectedItem != null ? selectedItem : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("🖱️ " + logEntry);
    }
    
    /**
     * Log context menu positioning failure
     */
    public static void logContextMenuFailure(double clickY, int calculatedIndex, int totalItems, String reason) {
        String logEntry = String.format("[%s] CONTEXT_MENU_FAIL: click_y=%.1f, calculated_index=%d, total_items=%d, reason=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            clickY,
            calculatedIndex,
            totalItems,
            reason
        );
        
        writeLogEntry(logEntry);
        System.out.println("❌ " + logEntry);
    }
    
    /**
     * Log file list interaction
     */
    public static void logFileListInteraction(String action, String fileName, boolean success, String details) {
        String logEntry = String.format("[%s] FILE_LIST: action=%s, file=%s, success=%s, details=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            action, // "select", "double_click", "right_click", "preview", "download", "delete"
            fileName != null ? fileName : "none",
            success,
            details != null ? details : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("📋 " + logEntry);
    }
    
    /**
     * Log backup/restore UI operation
     */
    public static void logBackupRestoreOperation(String operation, String filePath, long fileSize, 
                                               boolean success, String errorMessage) {
        String logEntry = String.format("[%s] BACKUP_RESTORE: operation=%s, file=%s, size=%d, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            operation, // "backup_create", "backup_restore"
            filePath,
            fileSize,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("💼 " + logEntry);
    }
    
    /**
     * Log UI component initialization
     */
    public static void logUIInitialization(String component, boolean success, String errorMessage) {
        String logEntry = String.format("[%s] UI_INIT: component=%s, success=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            component,
            success,
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("🖥️ " + logEntry);
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
            System.err.println("⚠️ Failed to write UI log: " + e.getMessage());
        }
    }
}