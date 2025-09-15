package com.ghostvault.error;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statistics about error occurrences and patterns
 */
public class ErrorStatistics {
    
    private final Map<String, Integer> errorCounts;
    private final long generatedAt;
    
    public ErrorStatistics(ConcurrentHashMap<String, AtomicInteger> errorCountsMap) {
        this.errorCounts = new HashMap<>();
        this.generatedAt = System.currentTimeMillis();
        
        // Convert AtomicInteger map to regular map for immutability
        for (Map.Entry<String, AtomicInteger> entry : errorCountsMap.entrySet()) {
            errorCounts.put(entry.getKey(), entry.getValue().get());
        }
    }
    
    /**
     * Get total number of errors
     */
    public int getTotalErrors() {
        return errorCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Get count for specific error type
     */
    public int getErrorCount(String errorType) {
        return errorCounts.getOrDefault(errorType, 0);
    }
    
    /**
     * Get all error counts
     */
    public Map<String, Integer> getAllErrorCounts() {
        return new HashMap<>(errorCounts);
    }
    
    /**
     * Get most frequent error type
     */
    public String getMostFrequentError() {
        return errorCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Get cryptographic error count
     */
    public int getCryptographicErrors() {
        return getErrorCount("CRYPTO_ERRORS");
    }
    
    /**
     * Get security error count
     */
    public int getSecurityErrors() {
        return getErrorCount("SECURITY_ERRORS");
    }
    
    /**
     * Get vault error count
     */
    public int getVaultErrors() {
        return getErrorCount("VAULT_ERRORS");
    }
    
    /**
     * Get file system error count
     */
    public int getFileSystemErrors() {
        return getErrorCount("FILE_ERRORS");
    }
    
    /**
     * Check if error pattern indicates system instability
     */
    public boolean indicatesInstability() {
        int totalErrors = getTotalErrors();
        int criticalErrors = getCryptographicErrors() + getSecurityErrors();
        
        // Consider unstable if more than 20% of errors are critical
        return totalErrors > 10 && (criticalErrors * 100 / totalErrors) > 20;
    }
    
    /**
     * Get system health score (0-100)
     */
    public int getHealthScore() {
        int totalErrors = getTotalErrors();
        
        if (totalErrors == 0) {
            return 100;
        }
        
        int criticalErrors = getCryptographicErrors() + getSecurityErrors();
        int majorErrors = getVaultErrors();
        
        // Calculate penalty based on error types
        int criticalPenalty = criticalErrors * 20;
        int majorPenalty = majorErrors * 10;
        int minorPenalty = (totalErrors - criticalErrors - majorErrors) * 2;
        
        int score = 100 - criticalPenalty - majorPenalty - minorPenalty;
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Get health status description
     */
    public String getHealthStatus() {
        int score = getHealthScore();
        
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 60) return "Fair";
        if (score >= 40) return "Poor";
        return "Critical";
    }
    
    /**
     * Get error rate (errors per hour, assuming 1 hour window)
     */
    public double getErrorRate() {
        // This is a simplified calculation
        // In a real implementation, you'd track time windows
        return getTotalErrors(); // errors per hour
    }
    
    /**
     * Get statistics summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== Error Statistics ===\n");
        summary.append("Total Errors: ").append(getTotalErrors()).append("\n");
        summary.append("Health Score: ").append(getHealthScore()).append("/100 (").append(getHealthStatus()).append(")\n");
        summary.append("Most Frequent: ").append(getMostFrequentError()).append("\n");
        summary.append("System Stability: ").append(indicatesInstability() ? "Unstable" : "Stable").append("\n");
        
        summary.append("\n=== By Category ===\n");
        summary.append("Cryptographic: ").append(getCryptographicErrors()).append("\n");
        summary.append("Security: ").append(getSecurityErrors()).append("\n");
        summary.append("Vault: ").append(getVaultErrors()).append("\n");
        summary.append("File System: ").append(getFileSystemErrors()).append("\n");
        
        if (!errorCounts.isEmpty()) {
            summary.append("\n=== Detailed Counts ===\n");
            errorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        }
        
        return summary.toString();
    }
    
    /**
     * Get timestamp when statistics were generated
     */
    public long getGeneratedAt() {
        return generatedAt;
    }
    
    @Override
    public String toString() {
        return String.format("ErrorStatistics{total=%d, health=%s, stability=%s}", 
            getTotalErrors(), getHealthStatus(), indicatesInstability() ? "Unstable" : "Stable");
    }
}