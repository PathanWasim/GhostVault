package com.ghostvault.audit;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.MemoryUtils;
import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive audit logging system for GhostVault
 * Provides encrypted, append-only logging of all vault operations
 */
public class AuditManager {
    
    private final CryptoManager cryptoManager;
    private final BlockingQueue<AuditEntry> auditQueue;
    private final Thread auditWriterThread;
    private final AtomicBoolean isRunning;
    private final AtomicLong entryCounter;
    
    private SecretKey auditEncryptionKey;
    private String auditLogPath;
    private long maxLogFileSize;
    private int maxLogFiles;
    private boolean maskSensitiveData;
    
    // Audit categories
    public enum AuditCategory {
        AUTHENTICATION,
        FILE_OPERATIONS,
        SECURITY_EVENTS,
        SYSTEM_EVENTS,
        CONFIGURATION,
        BACKUP_RESTORE,
        SESSION_MANAGEMENT,
        ERROR_EVENTS
    }
    
    // Audit severity levels
    public enum AuditSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
    
    public AuditManager() throws Exception {
        this.cryptoManager = new CryptoManager();
        this.auditQueue = new LinkedBlockingQueue<>();
        this.isRunning = new AtomicBoolean(false);
        this.entryCounter = new AtomicLong(0);
        
        // Default configuration
        this.auditLogPath = AppConfig.LOG_FILE;
        this.maxLogFileSize = 10 * 1024 * 1024; // 10MB
        this.maxLogFiles = 10;
        this.maskSensitiveData = true;
        
        // Create audit writer thread
        this.auditWriterThread = new Thread(this::auditWriterLoop, "AuditWriter");
        this.auditWriterThread.setDaemon(true);
    }
    
    /**
     * Start audit logging
     */
    public void startAuditLogging(SecretKey encryptionKey) throws Exception {
        this.auditEncryptionKey = encryptionKey;
        
        // Ensure audit log directory exists
        Path logPath = Paths.get(auditLogPath);
        if (logPath.getParent() != null) {
            Files.createDirectories(logPath.getParent());
        }
        
        isRunning.set(true);
        auditWriterThread.start();
        
        // Log audit system startup
        logAuditEvent(AuditCategory.SYSTEM_EVENTS, AuditSeverity.INFO, 
            "AUDIT_SYSTEM_STARTED", "Audit logging system initialized", null);
    }
    
    /**
     * Stop audit logging
     */
    public void stopAuditLogging() {
        if (isRunning.get()) {
            // Log audit system shutdown
            logAuditEvent(AuditCategory.SYSTEM_EVENTS, AuditSeverity.INFO, 
                "AUDIT_SYSTEM_STOPPING", "Audit logging system shutting down", null);
            
            isRunning.set(false);
            
            // Wait for queue to drain
            try {
                auditWriterThread.interrupt();
                auditWriterThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Log authentication event
     */
    public void logAuthentication(String event, String username, String ipAddress, boolean success) {
        AuditDetails details = new AuditDetails();
        details.addDetail("username", maskSensitive(username));
        details.addDetail("ip_address", ipAddress);
        details.addDetail("success", String.valueOf(success));
        
        AuditSeverity severity = success ? AuditSeverity.INFO : AuditSeverity.WARNING;
        logAuditEvent(AuditCategory.AUTHENTICATION, severity, event, 
            "Authentication attempt", details);
    }
    
    /**
     * Log file operation
     */
    public void logFileOperation(String operation, String fileName, String fileId, 
                                long fileSize, boolean success, String errorMessage) {
        AuditDetails details = new AuditDetails();
        details.addDetail("operation", operation);
        details.addDetail("file_name", maskSensitive(fileName));
        details.addDetail("file_id", fileId);
        details.addDetail("file_size", String.valueOf(fileSize));
        details.addDetail("success", String.valueOf(success));
        if (errorMessage != null) {
            details.addDetail("error", errorMessage);
        }
        
        AuditSeverity severity = success ? AuditSeverity.INFO : AuditSeverity.ERROR;
        logAuditEvent(AuditCategory.FILE_OPERATIONS, severity, 
            "FILE_" + operation.toUpperCase(), "File operation", details);
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String event, String description, AuditSeverity severity, 
                                String sourceIP, String additionalInfo) {
        AuditDetails details = new AuditDetails();
        if (sourceIP != null) {
            details.addDetail("source_ip", sourceIP);
        }
        if (additionalInfo != null) {
            details.addDetail("additional_info", additionalInfo);
        }
        
        logAuditEvent(AuditCategory.SECURITY_EVENTS, severity, event, description, details);
    }
    
    /**
     * Log session event
     */
    public void logSessionEvent(String event, String sessionId, long duration, String reason) {
        AuditDetails details = new AuditDetails();
        details.addDetail("session_id", sessionId);
        if (duration > 0) {
            details.addDetail("duration_ms", String.valueOf(duration));
        }
        if (reason != null) {
            details.addDetail("reason", reason);
        }
        
        logAuditEvent(AuditCategory.SESSION_MANAGEMENT, AuditSeverity.INFO, 
            event, "Session event", details);
    }
    
    /**
     * Log backup/restore operation
     */
    public void logBackupRestore(String operation, String backupFile, int fileCount, 
                                long totalSize, boolean success, String errorMessage) {
        AuditDetails details = new AuditDetails();
        details.addDetail("operation", operation);
        details.addDetail("backup_file", maskSensitive(backupFile));
        details.addDetail("file_count", String.valueOf(fileCount));
        details.addDetail("total_size", String.valueOf(totalSize));
        details.addDetail("success", String.valueOf(success));
        if (errorMessage != null) {
            details.addDetail("error", errorMessage);
        }
        
        AuditSeverity severity = success ? AuditSeverity.INFO : AuditSeverity.ERROR;
        logAuditEvent(AuditCategory.BACKUP_RESTORE, severity, 
            operation.toUpperCase(), "Backup/restore operation", details);
    }
    
    /**
     * Log configuration change
     */
    public void logConfigurationChange(String setting, String oldValue, String newValue, String changedBy) {
        AuditDetails details = new AuditDetails();
        details.addDetail("setting", setting);
        details.addDetail("old_value", maskSensitive(oldValue));
        details.addDetail("new_value", maskSensitive(newValue));
        details.addDetail("changed_by", maskSensitive(changedBy));
        
        logAuditEvent(AuditCategory.CONFIGURATION, AuditSeverity.INFO, 
            "CONFIG_CHANGE", "Configuration changed", details);
    }
    
    /**
     * Log error event
     */
    public void logError(String errorType, String errorMessage, String stackTrace, String context) {
        AuditDetails details = new AuditDetails();
        details.addDetail("error_type", errorType);
        details.addDetail("error_message", errorMessage);
        if (stackTrace != null) {
            details.addDetail("stack_trace", stackTrace);
        }
        if (context != null) {
            details.addDetail("context", context);
        }
        
        logAuditEvent(AuditCategory.ERROR_EVENTS, AuditSeverity.ERROR, 
            "ERROR_OCCURRED", "System error", details);
    }
    
    /**
     * Log panic mode activation
     */
    public void logPanicMode(String trigger, String reason) {
        AuditDetails details = new AuditDetails();
        details.addDetail("trigger", trigger);
        details.addDetail("reason", reason);
        details.addDetail("data_destruction", "INITIATED");
        
        logAuditEvent(AuditCategory.SECURITY_EVENTS, AuditSeverity.CRITICAL, 
            "PANIC_MODE_ACTIVATED", "Emergency data destruction initiated", details);
    }
    
    /**
     * Log system event
     */
    public void logSystemEvent(String event, String description) {
        logAuditEvent(AuditCategory.SYSTEM_EVENTS, AuditSeverity.INFO, 
            event, description, null);
    }
    
    /**
     * Log system event with severity
     */
    public void logSystemEvent(String event, String description, String severity) {
        AuditSeverity auditSeverity;
        try {
            auditSeverity = AuditSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            auditSeverity = AuditSeverity.INFO;
        }
        logAuditEvent(AuditCategory.SYSTEM_EVENTS, auditSeverity, 
            event, description, null);
    }
    
    /**
     * Log system operation
     */
    public void logSystemOperation(String operation, String description) {
        logAuditEvent(AuditCategory.SYSTEM_EVENTS, AuditSeverity.INFO, 
            operation, description, null);
    }
    
    /**
     * Core audit logging method
     */
    private void logAuditEvent(AuditCategory category, AuditSeverity severity, 
                              String eventType, String description, AuditDetails details) {
        if (!isRunning.get()) {
            return; // Audit system not running
        }
        
        try {
            AuditEntry entry = new AuditEntry(
                entryCounter.incrementAndGet(),
                LocalDateTime.now(),
                category,
                severity,
                eventType,
                description,
                details,
                Thread.currentThread().getName(),
                getCurrentUser()
            );
            
            // Add to queue for async processing
            if (!auditQueue.offer(entry)) {
                // Queue is full, log to console as fallback
                System.err.println("AUDIT QUEUE FULL: " + entry.toString());
            }
            
        } catch (Exception e) {
            // Audit logging should never fail the main operation
            System.err.println("AUDIT LOGGING ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Audit writer thread loop
     */
    private void auditWriterLoop() {
        while (isRunning.get() || !auditQueue.isEmpty()) {
            try {
                AuditEntry entry = auditQueue.take();
                writeAuditEntry(entry);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("AUDIT WRITE ERROR: " + e.getMessage());
            }
        }
    }
    
    /**
     * Write audit entry to encrypted log file
     */
    private void writeAuditEntry(AuditEntry entry) throws Exception {
        if (auditEncryptionKey == null) {
            return; // No encryption key available
        }
        
        // Check if log rotation is needed
        rotateLogIfNeeded();
        
        // Serialize audit entry
        String entryJson = serializeAuditEntry(entry);
        byte[] entryData = entryJson.getBytes("UTF-8");
        
        try {
            // Encrypt audit entry
            byte[] encryptedBytes = cryptoManager.encrypt(entryData, auditEncryptionKey);
            CryptoManager.EncryptedData encryptedEntry = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Append to log file
            appendToLogFile(encryptedEntry);
            
        } finally {
            // Clear sensitive data
            MemoryUtils.secureWipe(entryData);
        }
    }
    
    /**
     * Append encrypted entry to log file
     */
    private void appendToLogFile(CryptoManager.EncryptedData encryptedEntry) throws Exception {
        Path logPath = Paths.get(auditLogPath);
        
        // Create log file if it doesn't exist
        if (!Files.exists(logPath)) {
            Files.createFile(logPath);
        }
        
        // Write entry length and data
        try (FileOutputStream fos = new FileOutputStream(logPath.toFile(), true);
             DataOutputStream dos = new DataOutputStream(fos)) {
            
            byte[] combinedData = encryptedEntry.getCombinedData();
            dos.writeInt(combinedData.length);
            dos.write(combinedData);
            dos.flush();
        }
    }
    
    /**
     * Rotate log file if it exceeds size limit
     */
    private void rotateLogIfNeeded() throws Exception {
        Path logPath = Paths.get(auditLogPath);
        
        if (Files.exists(logPath) && Files.size(logPath) > maxLogFileSize) {
            rotateLogFiles();
        }
    }
    
    /**
     * Rotate log files
     */
    private void rotateLogFiles() throws Exception {
        Path logPath = Paths.get(auditLogPath);
        String baseName = logPath.getFileName().toString();
        Path logDir = logPath.getParent();
        
        // Remove oldest log file if we have too many
        Path oldestLog = logDir.resolve(baseName + "." + maxLogFiles);
        if (Files.exists(oldestLog)) {
            Files.delete(oldestLog);
        }
        
        // Rotate existing log files
        for (int i = maxLogFiles - 1; i >= 1; i--) {
            Path currentLog = logDir.resolve(baseName + "." + i);
            Path nextLog = logDir.resolve(baseName + "." + (i + 1));
            
            if (Files.exists(currentLog)) {
                Files.move(currentLog, nextLog);
            }
        }
        
        // Move current log to .1
        Path rotatedLog = logDir.resolve(baseName + ".1");
        Files.move(logPath, rotatedLog);
        
        // Log rotation event
        logAuditEvent(AuditCategory.SYSTEM_EVENTS, AuditSeverity.INFO, 
            "LOG_ROTATED", "Audit log file rotated", null);
    }
    
    /**
     * Read audit log entries
     */
    public List<AuditEntry> readAuditLog(int maxEntries, AuditCategory categoryFilter, 
                                        LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
        List<AuditEntry> entries = new ArrayList<>();
        
        if (auditEncryptionKey == null) {
            throw new IllegalStateException("Audit encryption key not set");
        }
        
        // Read from current log file and rotated files
        List<Path> logFiles = getLogFiles();
        
        for (Path logFile : logFiles) {
            if (!Files.exists(logFile)) {
                continue;
            }
            
            try (FileInputStream fis = new FileInputStream(logFile.toFile());
                 DataInputStream dis = new DataInputStream(fis)) {
                
                while (dis.available() > 0 && entries.size() < maxEntries) {
                    try {
                        // Read entry length and data
                        int entryLength = dis.readInt();
                        byte[] encryptedData = new byte[entryLength];
                        dis.readFully(encryptedData);
                        
                        // Decrypt entry
                        CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedData);
                        byte[] decryptedData = cryptoManager.decrypt(encrypted, auditEncryptionKey);
                        
                        try {
                            // Deserialize entry
                            String entryJson = new String(decryptedData, "UTF-8");
                            AuditEntry entry = deserializeAuditEntry(entryJson);
                            
                            // Apply filters
                            if (matchesFilters(entry, categoryFilter, fromDate, toDate)) {
                                entries.add(entry);
                            }
                            
                        } finally {
                            MemoryUtils.secureWipe(decryptedData);
                        }
                        
                    } catch (Exception e) {
                        // Skip corrupted entries
                        System.err.println("Skipping corrupted audit entry: " + e.getMessage());
                    }
                }
            }
        }
        
        return entries;
    }
    
    /**
     * Get list of log files (current and rotated)
     */
    private List<Path> getLogFiles() {
        List<Path> logFiles = new ArrayList<>();
        Path logPath = Paths.get(auditLogPath);
        
        // Add current log file
        if (Files.exists(logPath)) {
            logFiles.add(logPath);
        }
        
        // Add rotated log files
        Path logDir = logPath.getParent();
        String baseName = logPath.getFileName().toString();
        
        for (int i = 1; i <= maxLogFiles; i++) {
            Path rotatedLog = logDir.resolve(baseName + "." + i);
            if (Files.exists(rotatedLog)) {
                logFiles.add(rotatedLog);
            }
        }
        
        return logFiles;
    }
    
    /**
     * Check if entry matches filters
     */
    private boolean matchesFilters(AuditEntry entry, AuditCategory categoryFilter, 
                                  LocalDateTime fromDate, LocalDateTime toDate) {
        if (categoryFilter != null && entry.getCategory() != categoryFilter) {
            return false;
        }
        
        if (fromDate != null && entry.getTimestamp().isBefore(fromDate)) {
            return false;
        }
        
        if (toDate != null && entry.getTimestamp().isAfter(toDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Serialize audit entry to JSON
     */
    private String serializeAuditEntry(AuditEntry entry) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"id\": ").append(entry.getId()).append(",\n");
        json.append("  \"timestamp\": \"").append(entry.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("  \"category\": \"").append(entry.getCategory()).append("\",\n");
        json.append("  \"severity\": \"").append(entry.getSeverity()).append("\",\n");
        json.append("  \"event_type\": \"").append(escapeJson(entry.getEventType())).append("\",\n");
        json.append("  \"description\": \"").append(escapeJson(entry.getDescription())).append("\",\n");
        json.append("  \"thread\": \"").append(escapeJson(entry.getThreadName())).append("\",\n");
        json.append("  \"user\": \"").append(escapeJson(entry.getUserContext())).append("\"");
        
        if (entry.getDetails() != null && !entry.getDetails().isEmpty()) {
            json.append(",\n  \"details\": {\n");
            boolean first = true;
            for (java.util.Map.Entry<String, String> detail : entry.getDetails().entrySet()) {
                if (!first) json.append(",\n");
                json.append("    \"").append(escapeJson(detail.getKey())).append("\": \"")
                   .append(escapeJson(detail.getValue())).append("\"");
                first = false;
            }
            json.append("\n  }");
        }
        
        json.append("\n}");
        return json.toString();
    }
    
    /**
     * Deserialize audit entry from JSON (simplified)
     */
    private AuditEntry deserializeAuditEntry(String json) {
        // Simplified JSON parsing - in production, use a proper JSON library
        long id = extractLongValue(json, "id");
        String timestamp = extractStringValue(json, "timestamp");
        String category = extractStringValue(json, "category");
        String severity = extractStringValue(json, "severity");
        String eventType = extractStringValue(json, "event_type");
        String description = extractStringValue(json, "description");
        String thread = extractStringValue(json, "thread");
        String user = extractStringValue(json, "user");
        
        AuditDetails details = new AuditDetails();
        // Note: Details parsing would be more complex in a real implementation
        
        return new AuditEntry(
            id,
            LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            AuditCategory.valueOf(category),
            AuditSeverity.valueOf(severity),
            eventType,
            description,
            details,
            thread,
            user
        );
    }
    
    /**
     * Helper methods for JSON processing
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private long extractLongValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : 0L;
    }
    
    private String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
    
    /**
     * Mask sensitive data if enabled
     */
    private String maskSensitive(String value) {
        if (!maskSensitiveData || value == null || value.length() <= 4) {
            return value;
        }
        
        // Show first 2 and last 2 characters, mask the rest
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }
    
    /**
     * Get current user context
     */
    private String getCurrentUser() {
        // In a real implementation, this would get the current authenticated user
        return System.getProperty("user.name", "system");
    }
    
    /**
     * Securely delete all audit logs (for panic mode)
     */
    public void secureDeleteAuditLogs() throws Exception {
        List<Path> logFiles = getLogFiles();
        
        for (Path logFile : logFiles) {
            if (Files.exists(logFile)) {
                com.ghostvault.core.FileManager.secureDeleteFile(logFile);
            }
        }
        
        // Log the deletion (this will be the last entry before logs are destroyed)
        logAuditEvent(AuditCategory.SECURITY_EVENTS, AuditSeverity.CRITICAL, 
            "AUDIT_LOGS_DESTROYED", "All audit logs securely deleted", null);
    }
    
    /**
     * Get audit statistics
     */
    public AuditStatistics getAuditStatistics() throws Exception {
        List<AuditEntry> allEntries = readAuditLog(Integer.MAX_VALUE, null, null, null);
        
        return new AuditStatistics(
            allEntries.size(),
            entryCounter.get(),
            calculateLogFileSize(),
            getOldestEntryDate(allEntries),
            getNewestEntryDate(allEntries),
            countByCategory(allEntries),
            countBySeverity(allEntries)
        );
    }
    
    /**
     * Calculate total log file size
     */
    private long calculateLogFileSize() {
        return getLogFiles().stream()
            .mapToLong(path -> {
                try {
                    return Files.exists(path) ? Files.size(path) : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
    }
    
    /**
     * Get oldest entry date
     */
    private LocalDateTime getOldestEntryDate(List<AuditEntry> entries) {
        return entries.stream()
            .map(AuditEntry::getTimestamp)
            .min(LocalDateTime::compareTo)
            .orElse(null);
    }
    
    /**
     * Get newest entry date
     */
    private LocalDateTime getNewestEntryDate(List<AuditEntry> entries) {
        return entries.stream()
            .map(AuditEntry::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
    
    /**
     * Count entries by category
     */
    private java.util.Map<AuditCategory, Long> countByCategory(List<AuditEntry> entries) {
        return entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                AuditEntry::getCategory,
                java.util.stream.Collectors.counting()
            ));
    }
    
    /**
     * Count entries by severity
     */
    private java.util.Map<AuditSeverity, Long> countBySeverity(List<AuditEntry> entries) {
        return entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                AuditEntry::getSeverity,
                java.util.stream.Collectors.counting()
            ));
    }
    
    /**
     * Configuration setters
     */
    public void setMaxLogFileSize(long maxSize) { this.maxLogFileSize = maxSize; }
    public void setMaxLogFiles(int maxFiles) { this.maxLogFiles = maxFiles; }
    public void setMaskSensitiveData(boolean mask) { this.maskSensitiveData = mask; }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        stopAuditLogging();
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        auditEncryptionKey = null;
    }
}