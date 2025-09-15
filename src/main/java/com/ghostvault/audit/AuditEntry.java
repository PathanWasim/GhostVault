package com.ghostvault.audit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Represents a single audit log entry
 */
public class AuditEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long id;
    private final LocalDateTime timestamp;
    private final AuditManager.AuditCategory category;
    private final AuditManager.AuditSeverity severity;
    private final String eventType;
    private final String description;
    private final AuditDetails details;
    private final String threadName;
    private final String userContext;
    
    public AuditEntry(long id, LocalDateTime timestamp, AuditManager.AuditCategory category,
                     AuditManager.AuditSeverity severity, String eventType, String description,
                     AuditDetails details, String threadName, String userContext) {
        this.id = id;
        this.timestamp = timestamp;
        this.category = category;
        this.severity = severity;
        this.eventType = eventType;
        this.description = description;
        this.details = details;
        this.threadName = threadName;
        this.userContext = userContext;
    }
    
    // Getters
    public long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public AuditManager.AuditCategory getCategory() { return category; }
    public AuditManager.AuditSeverity getSeverity() { return severity; }
    public String getEventType() { return eventType; }
    public String getDescription() { return description; }
    public AuditDetails getDetails() { return details; }
    public String getThreadName() { return threadName; }
    public String getUserContext() { return userContext; }
    
    /**
     * Get formatted timestamp
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Get severity icon
     */
    public String getSeverityIcon() {
        switch (severity) {
            case INFO: return "ℹ️";
            case WARNING: return "⚠️";
            case ERROR: return "❌";
            case CRITICAL: return "🚨";
            default: return "📝";
        }
    }
    
    /**
     * Get category icon
     */
    public String getCategoryIcon() {
        switch (category) {
            case AUTHENTICATION: return "🔐";
            case FILE_OPERATIONS: return "📁";
            case SECURITY_EVENTS: return "🛡️";
            case SYSTEM_EVENTS: return "⚙️";
            case CONFIGURATION: return "🔧";
            case BACKUP_RESTORE: return "💾";
            case SESSION_MANAGEMENT: return "👤";
            case ERROR_EVENTS: return "🐛";
            default: return "📋";
        }
    }
    
    /**
     * Get human-readable summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getSeverityIcon()).append(" ");
        summary.append(getCategoryIcon()).append(" ");
        summary.append(eventType).append(": ").append(description);
        
        if (details != null && !details.isEmpty()) {
            summary.append(" (");
            boolean first = true;
            for (Map.Entry<String, String> detail : details.entrySet()) {
                if (!first) summary.append(", ");
                summary.append(detail.getKey()).append("=").append(detail.getValue());
                first = false;
            }
            summary.append(")");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if entry matches search criteria
     */
    public boolean matches(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        
        String term = searchTerm.toLowerCase();
        
        return eventType.toLowerCase().contains(term) ||
               description.toLowerCase().contains(term) ||
               category.toString().toLowerCase().contains(term) ||
               severity.toString().toLowerCase().contains(term) ||
               (details != null && details.toString().toLowerCase().contains(term));
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s %s: %s - %s", 
            getFormattedTimestamp(), 
            getSeverityIcon(), 
            getCategoryIcon(), 
            severity, 
            eventType, 
            description);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AuditEntry that = (AuditEntry) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}