package com.ghostvault.audit;

/**
 * Audit manager for security logging
 */
public class AuditManager {
    
    public enum AuditSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    public void logEvent(String message, AuditSeverity severity, String source, String details) {
        System.out.println("[AUDIT] " + severity + " - " + message + " (Source: " + source + ")");
        if (details != null && !details.isEmpty()) {
            System.out.println("  Details: " + details);
        }
    }
    
    public void logSecurityEvent(String event, String details) {
        logEvent(event, AuditSeverity.WARNING, "Security", details);
    }
    
    public void logSecurityEvent(String event, String details, AuditSeverity severity, String source, String additionalDetails) {
        logEvent(event + " - " + details, severity, source != null ? source : "Security", additionalDetails);
    }
    
    public void logSystemEvent(String event) {
        logEvent(event, AuditSeverity.INFO, "System", null);
    }
}