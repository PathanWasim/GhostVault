package com.ghostvault.audit;

/**
 * Simple stub for AuditManager to maintain compatibility
 * Enterprise audit features removed for simplicity
 */
public class AuditManager {
    
    public enum AuditSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    public AuditManager() {
        // Simple stub constructor
    }
    
    public void logSecurityEvent(String eventType, String description, AuditSeverity severity, 
                                String userId, String details) {
        // Stub implementation - no actual logging
        System.out.println("[AUDIT] " + severity + ": " + eventType + " - " + description);
    }
    
    public void logSystemEvent(String eventType, String description) {
        // Stub implementation
        System.out.println("[SYSTEM] " + eventType + ": " + description);
    }
    
    public void logSystemOperation(String operation, String details) {
        // Stub implementation
        System.out.println("[OPERATION] " + operation + ": " + details);
    }
    
    public java.util.List<String> getAuditLogs() {
        // Return empty list for compatibility
        return new java.util.ArrayList<>();
    }
}