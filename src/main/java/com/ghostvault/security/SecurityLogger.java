package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Comprehensive security logging system with audit trail maintenance
 * 
 * FEATURES:
 * - Asynchronous logging to prevent blocking authentication
 * - Configurable log rotation and retention
 * - Security event categorization and filtering
 * - Audit trail maintenance with integrity checking
 * - Thread-safe operations for concurrent access
 * 
 * @version 1.0.0 - Security Enhancement
 */
public class SecurityLogger {
    
    // Configuration constants
    private static final String SECURITY_LOG_DIR = "logs/security";
    private static final String SECURITY_LOG_FILE = "security.log";
    private static final String AUDIT_LOG_FILE = "audit.log";
    private static final long MAX_LOG_SIZE_MB = 10;
    private static final int MAX_LOG_FILES = 5;
    private static final int MAX_RETENTION_DAYS = 30;
    
    // Log levels
    public enum SecurityLevel {
        INFO, WARNING, CRITICAL, AUDIT
    }
    
    // Security event categories
    public enum EventCategory {
        AUTHENTICATION, AUTHORIZATION, DATA_ACCESS, SYSTEM_SECURITY, 
        CONFIGURATION, SESSION_MANAGEMENT, THREAT_DETECTION
    }
    
    /**
     * Security log entry structure
     */
    public static class SecurityLogEntry {
        private final LocalDateTime timestamp;
        private final SecurityLevel level;
        private final EventCategory category;
        private final String eventType;
        private final String description;
        private final String sourceInfo;
        private final String additionalData;
        
        public SecurityLogEntry(SecurityLevel level, EventCategory category, 
                              String eventType, String description, 
                              String sourceInfo, String additionalData) {
            this.timestamp = LocalDateTime.now();
            this.level = level;
            this.category = category;
            this.eventType = eventType;
            this.description = description;
            this.sourceInfo = sourceInfo;
            this.additionalData = additionalData;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public SecurityLevel getLevel() { return level; }
        public EventCategory getCategory() { return category; }
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getSourceInfo() { return sourceInfo; }
        public String getAdditionalData() { return additionalData; }
        
        public String toLogString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            return String.format("[%s] [%s] [%s] %s: %s | Source: %s | Data: %s",
                timestamp.format(formatter), level, category, eventType, 
                description, sourceInfo != null ? sourceInfo : "N/A", 
                additionalData != null ? additionalData : "N/A");
        }
        
        @Override
        public String toString() {
            return toLogString();
        }
    }
    
    // Logging infrastructure
    private final BlockingQueue<SecurityLogEntry> logQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Thread loggerThread;
    private final AuditManager auditManager;
    
    // Log file management
    private final Path logDirectory;
    private final Path securityLogPath;
    private final Path auditLogPath;
    
    // In-memory audit trail
    private final List<SecurityLogEntry> auditTrail = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_AUDIT_TRAIL_SIZE = 1000;
    
    public SecurityLogger(AuditManager auditManager) {
        this.auditManager = auditManager;
        
        // Initialize log directory
        this.logDirectory = Paths.get(SECURITY_LOG_DIR);
        this.securityLogPath = logDirectory.resolve(SECURITY_LOG_FILE);
        this.auditLogPath = logDirectory.resolve(AUDIT_LOG_FILE);
        
        // Create log directory if it doesn't exist
        try {
            Files.createDirectories(logDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create security log directory: " + e.getMessage());
        }
        
        // Create and start logger thread
        this.loggerThread = new Thread(this::processLogEntries, "SecurityLogger");
        this.loggerThread.setDaemon(true);
        
        // Log initialization
        logSecurityEvent(SecurityLevel.INFO, EventCategory.SYSTEM_SECURITY, 
            "SECURITY_LOGGER_INITIALIZED", "Security logging system started", 
            "System", null);
    }
    
    /**
     * Start the security logger
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            loggerThread.start();
            System.out.println("🔒 Security logger started");
        }
    }
    
    /**
     * Stop the security logger
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            loggerThread.interrupt();
            
            // Process remaining entries
            while (!logQueue.isEmpty()) {
                SecurityLogEntry entry = logQueue.poll();
                if (entry != null) {
                    writeLogEntry(entry);
                }
            }
            
            logSecurityEvent(SecurityLevel.INFO, EventCategory.SYSTEM_SECURITY, 
                "SECURITY_LOGGER_STOPPED", "Security logging system stopped", 
                "System", null);
            
            System.out.println("🔒 Security logger stopped");
        }
    }
    
    /**
     * Log a security event
     */
    public void logSecurityEvent(SecurityLevel level, EventCategory category, 
                                String eventType, String description, 
                                String sourceInfo, String additionalData) {
        SecurityLogEntry entry = new SecurityLogEntry(level, category, eventType, 
            description, sourceInfo, additionalData);
        
        // Add to queue for asynchronous processing
        if (!logQueue.offer(entry)) {
            System.err.println("Security log queue full - dropping entry: " + entry);
        }
        
        // Add to in-memory audit trail
        synchronized (auditTrail) {
            auditTrail.add(entry);
            
            // Maintain maximum size
            while (auditTrail.size() > MAX_AUDIT_TRAIL_SIZE) {
                auditTrail.remove(0);
            }
        }
        
        // Forward critical events to audit manager immediately
        if (level == SecurityLevel.CRITICAL && auditManager != null) {
            AuditManager.AuditSeverity severity = AuditManager.AuditSeverity.CRITICAL;
            auditManager.logSecurityEvent(eventType, description, severity, null, sourceInfo);
        }
    }
    
    /**
     * Log authentication event
     */
    public void logAuthenticationEvent(String eventType, String description, 
                                     String sourceInfo, String additionalData) {
        SecurityLevel level = determineAuthenticationLevel(eventType);
        logSecurityEvent(level, EventCategory.AUTHENTICATION, eventType, 
            description, sourceInfo, additionalData);
    }
    
    /**
     * Log session management event
     */
    public void logSessionEvent(String eventType, String description, 
                              String sourceInfo, String additionalData) {
        SecurityLevel level = determineSessionLevel(eventType);
        logSecurityEvent(level, EventCategory.SESSION_MANAGEMENT, eventType, 
            description, sourceInfo, additionalData);
    }
    
    /**
     * Log system security event
     */
    public void logSystemSecurityEvent(String eventType, String description, 
                                     String sourceInfo, String additionalData) {
        SecurityLevel level = determineSystemSecurityLevel(eventType);
        logSecurityEvent(level, EventCategory.SYSTEM_SECURITY, eventType, 
            description, sourceInfo, additionalData);
    }
    
    /**
     * Get recent audit trail entries
     */
    public List<SecurityLogEntry> getAuditTrail() {
        synchronized (auditTrail) {
            return new ArrayList<>(auditTrail);
        }
    }
    
    /**
     * Get audit trail entries by category
     */
    public List<SecurityLogEntry> getAuditTrailByCategory(EventCategory category) {
        synchronized (auditTrail) {
            return auditTrail.stream()
                .filter(entry -> entry.getCategory() == category)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
    
    /**
     * Get audit trail entries by level
     */
    public List<SecurityLogEntry> getAuditTrailByLevel(SecurityLevel level) {
        synchronized (auditTrail) {
            return auditTrail.stream()
                .filter(entry -> entry.getLevel() == level)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
    
    /**
     * Process log entries in background thread
     */
    private void processLogEntries() {
        while (isRunning.get() || !logQueue.isEmpty()) {
            try {
                SecurityLogEntry entry = logQueue.take();
                writeLogEntry(entry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing security log entry: " + e.getMessage());
            }
        }
    }
    
    /**
     * Write log entry to file
     */
    private void writeLogEntry(SecurityLogEntry entry) {
        try {
            // Check if log rotation is needed
            checkLogRotation();
            
            // Write to security log
            String logLine = entry.toLogString() + System.lineSeparator();
            Files.write(securityLogPath, logLine.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            // Write audit events to separate audit log
            if (entry.getLevel() == SecurityLevel.AUDIT || 
                entry.getLevel() == SecurityLevel.CRITICAL) {
                Files.write(auditLogPath, logLine.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to write security log entry: " + e.getMessage());
        }
    }
    
    /**
     * Check if log rotation is needed and perform rotation
     */
    private void checkLogRotation() throws IOException {
        if (Files.exists(securityLogPath)) {
            long fileSize = Files.size(securityLogPath);
            long maxSizeBytes = MAX_LOG_SIZE_MB * 1024 * 1024;
            
            if (fileSize > maxSizeBytes) {
                rotateLogFiles();
            }
        }
    }
    
    /**
     * Rotate log files
     */
    private void rotateLogFiles() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Rotate security log
        Path rotatedSecurityLog = logDirectory.resolve(
            SECURITY_LOG_FILE + "." + timestamp);
        Files.move(securityLogPath, rotatedSecurityLog);
        
        // Rotate audit log if it exists
        if (Files.exists(auditLogPath)) {
            Path rotatedAuditLog = logDirectory.resolve(
                AUDIT_LOG_FILE + "." + timestamp);
            Files.move(auditLogPath, rotatedAuditLog);
        }
        
        // Clean up old log files
        cleanupOldLogFiles();
        
        System.out.println("🔄 Security logs rotated: " + timestamp);
    }
    
    /**
     * Clean up old log files based on retention policy
     */
    private void cleanupOldLogFiles() {
        try {
            Files.list(logDirectory)
                .filter(path -> path.getFileName().toString().startsWith(SECURITY_LOG_FILE + ".") ||
                               path.getFileName().toString().startsWith(AUDIT_LOG_FILE + "."))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .skip(MAX_LOG_FILES)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        System.out.println("🗑️ Deleted old log file: " + path.getFileName());
                    } catch (IOException e) {
                        System.err.println("Failed to delete old log file: " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("Failed to cleanup old log files: " + e.getMessage());
        }
    }
    
    /**
     * Determine security level for authentication events
     */
    private SecurityLevel determineAuthenticationLevel(String eventType) {
        switch (eventType.toUpperCase()) {
            case "LOGIN_SUCCESS":
            case "LOGOUT":
                return SecurityLevel.INFO;
            case "LOGIN_FAILED":
            case "INVALID_PASSWORD":
                return SecurityLevel.WARNING;
            case "SECURITY_LOCKOUT":
            case "PANIC_MODE_ACTIVATED":
            case "BRUTE_FORCE_DETECTED":
                return SecurityLevel.CRITICAL;
            default:
                return SecurityLevel.INFO;
        }
    }
    
    /**
     * Determine security level for session events
     */
    private SecurityLevel determineSessionLevel(String eventType) {
        switch (eventType.toUpperCase()) {
            case "SESSION_STARTED":
            case "SESSION_ENDED":
                return SecurityLevel.INFO;
            case "SESSION_TIMEOUT":
            case "CONCURRENT_SESSION_DETECTED":
                return SecurityLevel.WARNING;
            case "SESSION_HIJACK_DETECTED":
                return SecurityLevel.CRITICAL;
            default:
                return SecurityLevel.INFO;
        }
    }
    
    /**
     * Determine security level for system security events
     */
    private SecurityLevel determineSystemSecurityLevel(String eventType) {
        switch (eventType.toUpperCase()) {
            case "SYSTEM_STARTUP":
            case "SYSTEM_SHUTDOWN":
                return SecurityLevel.INFO;
            case "CONFIGURATION_CHANGED":
            case "SECURITY_POLICY_UPDATED":
                return SecurityLevel.WARNING;
            case "SECURITY_BREACH_DETECTED":
            case "UNAUTHORIZED_ACCESS_ATTEMPT":
                return SecurityLevel.CRITICAL;
            default:
                return SecurityLevel.INFO;
        }
    }
    
    /**
     * Get security logging statistics
     */
    public String getLoggingStatistics() {
        synchronized (auditTrail) {
            long infoCount = auditTrail.stream().filter(e -> e.getLevel() == SecurityLevel.INFO).count();
            long warningCount = auditTrail.stream().filter(e -> e.getLevel() == SecurityLevel.WARNING).count();
            long criticalCount = auditTrail.stream().filter(e -> e.getLevel() == SecurityLevel.CRITICAL).count();
            long auditCount = auditTrail.stream().filter(e -> e.getLevel() == SecurityLevel.AUDIT).count();
            
            return String.format("Security Log Statistics - Total: %d | Info: %d | Warning: %d | Critical: %d | Audit: %d",
                auditTrail.size(), infoCount, warningCount, criticalCount, auditCount);
        }
    }
}