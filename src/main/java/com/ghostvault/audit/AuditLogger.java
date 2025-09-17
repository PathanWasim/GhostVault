package com.ghostvault.audit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple audit logger for compatibility
 * Provides basic logging functionality for the audit system
 */
public class AuditLogger {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log an audit event
     */
    public void logEvent(String event, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("[AUDIT] " + timestamp + " - " + event + ": " + details);
    }
    
    /**
     * Log a security event
     */
    public void logSecurityEvent(String event, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("[SECURITY] " + timestamp + " - " + event + ": " + details);
    }
    
    /**
     * Log an error event
     */
    public void logError(String error, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.err.println("[ERROR] " + timestamp + " - " + error + ": " + details);
    }
}