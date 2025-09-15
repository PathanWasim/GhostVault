package com.ghostvault.audit;

import com.ghostvault.util.FileUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Statistics and summary information about audit logs
 */
public class AuditStatistics {
    
    private final int totalEntries;
    private final long totalEntriesGenerated;
    private final long totalLogSize;
    private final LocalDateTime oldestEntry;
    private final LocalDateTime newestEntry;
    private final Map<AuditManager.AuditCategory, Long> entriesByCategory;
    private final Map<AuditManager.AuditSeverity, Long> entriesBySeverity;
    
    public AuditStatistics(int totalEntries, long totalEntriesGenerated, long totalLogSize,
                          LocalDateTime oldestEntry, LocalDateTime newestEntry,
                          Map<AuditManager.AuditCategory, Long> entriesByCategory,
                          Map<AuditManager.AuditSeverity, Long> entriesBySeverity) {
        this.totalEntries = totalEntries;
        this.totalEntriesGenerated = totalEntriesGenerated;
        this.totalLogSize = totalLogSize;
        this.oldestEntry = oldestEntry;
        this.newestEntry = newestEntry;
        this.entriesByCategory = entriesByCategory;
        this.entriesBySeverity = entriesBySeverity;
    }
    
    // Getters
    public int getTotalEntries() { return totalEntries; }
    public long getTotalEntriesGenerated() { return totalEntriesGenerated; }
    public long getTotalLogSize() { return totalLogSize; }
    public LocalDateTime getOldestEntry() { return oldestEntry; }
    public LocalDateTime getNewestEntry() { return newestEntry; }
    public Map<AuditManager.AuditCategory, Long> getEntriesByCategory() { return entriesByCategory; }
    public Map<AuditManager.AuditSeverity, Long> getEntriesBySeverity() { return entriesBySeverity; }
    
    /**
     * Get formatted log size
     */
    public String getFormattedLogSize() {
        return FileUtils.formatFileSize(totalLogSize);
    }
    
    /**
     * Get formatted date range
     */
    public String getDateRange() {
        if (oldestEntry == null || newestEntry == null) {
            return "No entries";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return oldestEntry.format(formatter) + " - " + newestEntry.format(formatter);
    }
    
    /**
     * Get log retention period in days
     */
    public long getRetentionDays() {
        if (oldestEntry == null || newestEntry == null) {
            return 0;
        }
        
        return java.time.Duration.between(oldestEntry, newestEntry).toDays();
    }
    
    /**
     * Get entries per day average
     */
    public double getEntriesPerDay() {
        long retentionDays = getRetentionDays();
        if (retentionDays == 0) {
            return totalEntries;
        }
        
        return (double) totalEntries / retentionDays;
    }
    
    /**
     * Get category with most entries
     */
    public AuditManager.AuditCategory getMostActiveCategory() {
        return entriesByCategory.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Get severity with most entries
     */
    public AuditManager.AuditSeverity getMostCommonSeverity() {
        return entriesBySeverity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Get count for specific category
     */
    public long getCategoryCount(AuditManager.AuditCategory category) {
        return entriesByCategory.getOrDefault(category, 0L);
    }
    
    /**
     * Get count for specific severity
     */
    public long getSeverityCount(AuditManager.AuditSeverity severity) {
        return entriesBySeverity.getOrDefault(severity, 0L);
    }
    
    /**
     * Get percentage for category
     */
    public double getCategoryPercentage(AuditManager.AuditCategory category) {
        if (totalEntries == 0) return 0.0;
        return (getCategoryCount(category) * 100.0) / totalEntries;
    }
    
    /**
     * Get percentage for severity
     */
    public double getSeverityPercentage(AuditManager.AuditSeverity severity) {
        if (totalEntries == 0) return 0.0;
        return (getSeverityCount(severity) * 100.0) / totalEntries;
    }
    
    /**
     * Check if there are any critical entries
     */
    public boolean hasCriticalEntries() {
        return getSeverityCount(AuditManager.AuditSeverity.CRITICAL) > 0;
    }
    
    /**
     * Check if there are any error entries
     */
    public boolean hasErrorEntries() {
        return getSeverityCount(AuditManager.AuditSeverity.ERROR) > 0;
    }
    
    /**
     * Get security health score (0-100)
     */
    public int getSecurityHealthScore() {
        if (totalEntries == 0) return 100;
        
        long criticalCount = getSeverityCount(AuditManager.AuditSeverity.CRITICAL);
        long errorCount = getSeverityCount(AuditManager.AuditSeverity.ERROR);
        long warningCount = getSeverityCount(AuditManager.AuditSeverity.WARNING);
        
        // Calculate score based on severity distribution
        double criticalPenalty = (criticalCount * 50.0) / totalEntries;
        double errorPenalty = (errorCount * 25.0) / totalEntries;
        double warningPenalty = (warningCount * 10.0) / totalEntries;
        
        int score = (int) Math.max(0, 100 - criticalPenalty - errorPenalty - warningPenalty);
        return Math.min(100, score);
    }
    
    /**
     * Get health status description
     */
    public String getHealthStatus() {
        int score = getSecurityHealthScore();
        
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 60) return "Fair";
        if (score >= 40) return "Poor";
        return "Critical";
    }
    
    /**
     * Get summary report
     */
    public String getSummaryReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=== Audit Log Statistics ===\n");
        report.append("Total Entries: ").append(totalEntries).append("\n");
        report.append("Log Size: ").append(getFormattedLogSize()).append("\n");
        report.append("Date Range: ").append(getDateRange()).append("\n");
        report.append("Retention: ").append(getRetentionDays()).append(" days\n");
        report.append("Avg Entries/Day: ").append(String.format("%.1f", getEntriesPerDay())).append("\n");
        report.append("Security Health: ").append(getHealthStatus()).append(" (").append(getSecurityHealthScore()).append("/100)\n");
        
        report.append("\n=== By Category ===\n");
        for (AuditManager.AuditCategory category : AuditManager.AuditCategory.values()) {
            long count = getCategoryCount(category);
            if (count > 0) {
                report.append(String.format("%-20s: %6d (%.1f%%)\n", 
                    category, count, getCategoryPercentage(category)));
            }
        }
        
        report.append("\n=== By Severity ===\n");
        for (AuditManager.AuditSeverity severity : AuditManager.AuditSeverity.values()) {
            long count = getSeverityCount(severity);
            if (count > 0) {
                String icon = getIconForSeverity(severity);
                report.append(String.format("%-12s %s: %6d (%.1f%%)\n", 
                    severity, icon, count, getSeverityPercentage(severity)));
            }
        }
        
        return report.toString();
    }
    
    /**
     * Get icon for severity level
     */
    private String getIconForSeverity(AuditManager.AuditSeverity severity) {
        switch (severity) {
            case INFO: return "ℹ️";
            case WARNING: return "⚠️";
            case ERROR: return "❌";
            case CRITICAL: return "🚨";
            default: return "📝";
        }
    }
    
    @Override
    public String toString() {
        return String.format("AuditStatistics{entries=%d, size=%s, health=%s}", 
            totalEntries, getFormattedLogSize(), getHealthStatus());
    }
}