package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advanced security monitoring system for GhostVault
 * Monitors for suspicious activities, intrusion attempts, and security threats
 */
public class SecurityMonitor {
    
    private final ScheduledExecutorService scheduler;
    private final List<SecurityEvent> securityEvents;
    private final List<SecurityAlertListener> alertListeners;
    
    // Threat detection counters
    private final AtomicInteger rapidLoginAttempts;
    private final AtomicInteger suspiciousFileAccess;
    private final AtomicInteger memoryAccessAttempts;
    private final AtomicInteger networkActivityDetected;
    
    // Timing analysis for attack detection
    private final AtomicLong lastLoginAttempt;
    private final AtomicLong lastFileAccess;
    private final AtomicLong lastMemoryAccess;
    
    // Security thresholds
    private static final int MAX_RAPID_LOGINS = 10;
    private static final int MAX_SUSPICIOUS_FILE_ACCESS = 5;
    private static final int MAX_MEMORY_ACCESS_ATTEMPTS = 3;
    private static final long RAPID_ACCESS_THRESHOLD_MS = 1000; // 1 second
    
    private boolean monitoringActive;
    private SecurityLevel currentSecurityLevel;
    
    public SecurityMonitor() {
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.securityEvents = new ArrayList<>();
        this.alertListeners = new ArrayList<>();
        
        this.rapidLoginAttempts = new AtomicInteger(0);
        this.suspiciousFileAccess = new AtomicInteger(0);
        this.memoryAccessAttempts = new AtomicInteger(0);
        this.networkActivityDetected = new AtomicInteger(0);
        
        this.lastLoginAttempt = new AtomicLong(0);
        this.lastFileAccess = new AtomicLong(0);
        this.lastMemoryAccess = new AtomicLong(0);
        
        this.monitoringActive = false;
        this.currentSecurityLevel = SecurityLevel.NORMAL;
    }
    
    /**
     * Start security monitoring
     */
    public void startMonitoring() {
        monitoringActive = true;
        currentSecurityLevel = SecurityLevel.NORMAL;
        
        // Start periodic security checks
        scheduler.scheduleAtFixedRate(this::performSecurityScan, 0, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::analyzeSecurityTrends, 0, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::cleanupOldEvents, 0, 1, TimeUnit.HOURS);
        
        logSecurityEvent(SecurityEventType.MONITORING_STARTED, "Security monitoring activated", SecurityLevel.NORMAL);
    }
    
    /**
     * Stop security monitoring
     */
    public void stopMonitoring() {
        monitoringActive = false;
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        
        logSecurityEvent(SecurityEventType.MONITORING_STOPPED, "Security monitoring deactivated", SecurityLevel.NORMAL);
    }
    
    /**
     * Record login attempt for analysis
     */
    public void recordLoginAttempt(boolean successful, String source) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastLogin = currentTime - lastLoginAttempt.get();
        
        lastLoginAttempt.set(currentTime);
        
        if (!successful) {
            // Check for rapid failed login attempts
            if (timeSinceLastLogin < RAPID_ACCESS_THRESHOLD_MS) {
                int rapidAttempts = rapidLoginAttempts.incrementAndGet();
                
                if (rapidAttempts > MAX_RAPID_LOGINS) {
                    escalateSecurityLevel(SecurityLevel.HIGH);
                    logSecurityEvent(SecurityEventType.BRUTE_FORCE_DETECTED, 
                        "Rapid login attempts detected from: " + source, SecurityLevel.HIGH);
                    notifySecurityAlert(SecurityAlertType.BRUTE_FORCE_ATTACK, 
                        "Multiple rapid login attempts detected");
                }
            }
        } else {
            // Reset counters on successful login
            rapidLoginAttempts.set(0);
        }
    }
    
    /**
     * Record file access for analysis
     */
    public void recordFileAccess(String fileName, String operation) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAccess = currentTime - lastFileAccess.get();
        
        lastFileAccess.set(currentTime);
        
        // Check for suspicious file access patterns
        if (timeSinceLastAccess < RAPID_ACCESS_THRESHOLD_MS) {
            int suspiciousAccess = suspiciousFileAccess.incrementAndGet();
            
            if (suspiciousAccess > MAX_SUSPICIOUS_FILE_ACCESS) {
                escalateSecurityLevel(SecurityLevel.MEDIUM);
                logSecurityEvent(SecurityEventType.SUSPICIOUS_FILE_ACCESS, 
                    "Rapid file access detected: " + fileName + " (" + operation + ")", SecurityLevel.MEDIUM);
            }
        }
        
        // Check for access to sensitive files
        if (isSensitiveFile(fileName)) {
            logSecurityEvent(SecurityEventType.SENSITIVE_FILE_ACCESS, 
                "Access to sensitive file: " + fileName, SecurityLevel.MEDIUM);
        }
    }
    
    /**
     * Record memory access attempt
     */
    public void recordMemoryAccessAttempt(String source) {
        long currentTime = System.currentTimeMillis();
        lastMemoryAccess.set(currentTime);
        
        int attempts = memoryAccessAttempts.incrementAndGet();
        
        if (attempts > MAX_MEMORY_ACCESS_ATTEMPTS) {
            escalateSecurityLevel(SecurityLevel.CRITICAL);
            logSecurityEvent(SecurityEventType.MEMORY_ACCESS_VIOLATION, 
                "Multiple memory access attempts from: " + source, SecurityLevel.CRITICAL);
            notifySecurityAlert(SecurityAlertType.MEMORY_ATTACK, 
                "Potential memory attack detected");
        }
    }
    
    /**
     * Record network activity
     */
    public void recordNetworkActivity(String destination, String protocol) {
        networkActivityDetected.incrementAndGet();
        
        // Log unexpected network activity
        if (!isExpectedNetworkActivity(destination, protocol)) {
            logSecurityEvent(SecurityEventType.UNEXPECTED_NETWORK_ACTIVITY, 
                "Unexpected network activity to: " + destination + " (" + protocol + ")", SecurityLevel.MEDIUM);
        }
    }
    
    /**
     * Perform periodic security scan
     */
    private void performSecurityScan() {
        if (!monitoringActive) {
            return;
        }
        
        // Check system integrity
        checkSystemIntegrity();
        
        // Check for running processes
        checkRunningProcesses();
        
        // Check file system changes
        checkFileSystemChanges();
        
        // Reset rapid access counters periodically
        resetRapidAccessCounters();
    }
    
    /**
     * Analyze security trends
     */
    private void analyzeSecurityTrends() {
        if (!monitoringActive) {
            return;
        }
        
        // Analyze recent security events for patterns
        List<SecurityEvent> recentEvents = getRecentEvents(5); // Last 5 minutes
        
        // Check for escalating threat patterns
        long highSeverityEvents = recentEvents.stream()
            .filter(event -> event.getSecurityLevel().ordinal() >= SecurityLevel.HIGH.ordinal())
            .count();
        
        if (highSeverityEvents > 3) {
            escalateSecurityLevel(SecurityLevel.CRITICAL);
            notifySecurityAlert(SecurityAlertType.ESCALATING_THREATS, 
                "Multiple high-severity security events detected");
        }
        
        // Check for coordinated attack patterns
        if (detectCoordinatedAttack(recentEvents)) {
            escalateSecurityLevel(SecurityLevel.CRITICAL);
            notifySecurityAlert(SecurityAlertType.COORDINATED_ATTACK, 
                "Coordinated attack pattern detected");
        }
    }
    
    /**
     * Check system integrity
     */
    private void checkSystemIntegrity() {
        // Check if critical files have been modified
        if (checkCriticalFileIntegrity()) {
            logSecurityEvent(SecurityEventType.SYSTEM_INTEGRITY_VIOLATION, 
                "Critical system files modified", SecurityLevel.HIGH);
        }
        
        // Check memory integrity
        if (checkMemoryIntegrity()) {
            logSecurityEvent(SecurityEventType.MEMORY_INTEGRITY_VIOLATION, 
                "Memory integrity check failed", SecurityLevel.HIGH);
        }
    }
    
    /**
     * Check running processes for suspicious activity
     */
    private void checkRunningProcesses() {
        // In a real implementation, this would check for:
        // - Debuggers attached to the process
        // - Memory scanners
        // - Network monitoring tools
        // - Suspicious processes accessing vault files
        
        // Placeholder implementation
        if (detectSuspiciousProcesses()) {
            logSecurityEvent(SecurityEventType.SUSPICIOUS_PROCESS_DETECTED, 
                "Suspicious process detected", SecurityLevel.HIGH);
        }
    }
    
    /**
     * Check file system changes
     */
    private void checkFileSystemChanges() {
        // Check for unauthorized changes to vault files
        if (detectUnauthorizedFileChanges()) {
            logSecurityEvent(SecurityEventType.UNAUTHORIZED_FILE_CHANGE, 
                "Unauthorized file system changes detected", SecurityLevel.HIGH);
        }
    }
    
    /**
     * Reset rapid access counters
     */
    private void resetRapidAccessCounters() {
        long currentTime = System.currentTimeMillis();
        
        // Reset counters if enough time has passed
        if (currentTime - lastLoginAttempt.get() > 60000) { // 1 minute
            rapidLoginAttempts.set(0);
        }
        
        if (currentTime - lastFileAccess.get() > 30000) { // 30 seconds
            suspiciousFileAccess.set(0);
        }
        
        if (currentTime - lastMemoryAccess.get() > 120000) { // 2 minutes
            memoryAccessAttempts.set(0);
        }
    }
    
    /**
     * Clean up old security events
     */
    private void cleanupOldEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        synchronized (securityEvents) {
            securityEvents.removeIf(event -> event.getTimestamp().isBefore(cutoff));
        }
    }
    
    /**
     * Escalate security level
     */
    private void escalateSecurityLevel(SecurityLevel newLevel) {
        if (newLevel.ordinal() > currentSecurityLevel.ordinal()) {
            SecurityLevel previousLevel = currentSecurityLevel;
            currentSecurityLevel = newLevel;
            
            logSecurityEvent(SecurityEventType.SECURITY_LEVEL_ESCALATED, 
                "Security level escalated from " + previousLevel + " to " + newLevel, newLevel);
            
            // Take appropriate action based on security level
            handleSecurityLevelEscalation(newLevel);
        }
    }
    
    /**
     * Handle security level escalation
     */
    private void handleSecurityLevelEscalation(SecurityLevel level) {
        switch (level) {
            case MEDIUM:
                // Increase monitoring frequency
                logSecurityEvent(SecurityEventType.MONITORING_ENHANCED, 
                    "Enhanced monitoring activated", level);
                break;
                
            case HIGH:
                // Lock down non-essential features
                logSecurityEvent(SecurityEventType.LOCKDOWN_INITIATED, 
                    "Security lockdown initiated", level);
                break;
                
            case CRITICAL:
                // Prepare for potential panic mode
                logSecurityEvent(SecurityEventType.CRITICAL_THREAT_DETECTED, 
                    "Critical security threat - prepare for emergency response", level);
                notifySecurityAlert(SecurityAlertType.CRITICAL_THREAT, 
                    "Critical security threat detected - consider panic mode");
                break;
        }
    }
    
    /**
     * Check if file is sensitive
     */
    private boolean isSensitiveFile(String fileName) {
        return fileName.contains("config") || 
               fileName.contains("password") || 
               fileName.contains("key") ||
               fileName.contains("salt") ||
               fileName.endsWith(".enc");
    }
    
    /**
     * Check if network activity is expected
     */
    private boolean isExpectedNetworkActivity(String destination, String protocol) {
        // In a real implementation, this would check against a whitelist
        // For now, assume all network activity is unexpected for a secure vault
        return false;
    }
    
    /**
     * Detect coordinated attack patterns
     */
    private boolean detectCoordinatedAttack(List<SecurityEvent> events) {
        // Look for multiple attack vectors in a short time period
        long bruteForceEvents = events.stream()
            .filter(e -> e.getEventType() == SecurityEventType.BRUTE_FORCE_DETECTED)
            .count();
        
        long memoryAttacks = events.stream()
            .filter(e -> e.getEventType() == SecurityEventType.MEMORY_ACCESS_VIOLATION)
            .count();
        
        long fileAccess = events.stream()
            .filter(e -> e.getEventType() == SecurityEventType.SUSPICIOUS_FILE_ACCESS)
            .count();
        
        // Coordinated attack if multiple attack types detected
        return (bruteForceEvents > 0 && memoryAttacks > 0) || 
               (bruteForceEvents > 0 && fileAccess > 2) ||
               (memoryAttacks > 0 && fileAccess > 2);
    }
    
    /**
     * Get recent security events
     */
    private List<SecurityEvent> getRecentEvents(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        
        synchronized (securityEvents) {
            return securityEvents.stream()
                .filter(event -> event.getTimestamp().isAfter(cutoff))
                .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Placeholder methods for system checks
     */
    private boolean checkCriticalFileIntegrity() {
        // In a real implementation, this would verify checksums of critical files
        return false;
    }
    
    private boolean checkMemoryIntegrity() {
        // In a real implementation, this would check for memory corruption
        return false;
    }
    
    private boolean detectSuspiciousProcesses() {
        // In a real implementation, this would check running processes
        return false;
    }
    
    private boolean detectUnauthorizedFileChanges() {
        // In a real implementation, this would monitor file system changes
        return false;
    }
    
    /**
     * Log security event
     */
    private void logSecurityEvent(SecurityEventType eventType, String description, SecurityLevel level) {
        SecurityEvent event = new SecurityEvent(eventType, description, level, LocalDateTime.now());
        
        synchronized (securityEvents) {
            securityEvents.add(event);
        }
        
        // Also log to console for immediate visibility
        System.out.println("[SECURITY-MONITOR] " + event);
    }
    
    /**
     * Notify security alert listeners
     */
    private void notifySecurityAlert(SecurityAlertType alertType, String message) {
        SecurityAlert alert = new SecurityAlert(alertType, message, currentSecurityLevel, LocalDateTime.now());
        
        for (SecurityAlertListener listener : alertListeners) {
            try {
                listener.onSecurityAlert(alert);
            } catch (Exception e) {
                // Continue with other listeners
            }
        }
    }
    
    /**
     * Add security alert listener
     */
    public void addSecurityAlertListener(SecurityAlertListener listener) {
        alertListeners.add(listener);
    }
    
    /**
     * Remove security alert listener
     */
    public void removeSecurityAlertListener(SecurityAlertListener listener) {
        alertListeners.remove(listener);
    }
    
    /**
     * Get current security level
     */
    public SecurityLevel getCurrentSecurityLevel() {
        return currentSecurityLevel;
    }
    
    /**
     * Get security statistics
     */
    public SecurityStats getSecurityStats() {
        return new SecurityStats(
            monitoringActive,
            currentSecurityLevel,
            securityEvents.size(),
            rapidLoginAttempts.get(),
            suspiciousFileAccess.get(),
            memoryAccessAttempts.get(),
            networkActivityDetected.get()
        );
    }
    
    /**
     * Get all security events
     */
    public List<SecurityEvent> getAllSecurityEvents() {
        synchronized (securityEvents) {
            return new ArrayList<>(securityEvents);
        }
    }
    
    /**
     * Security event types
     */
    public enum SecurityEventType {
        MONITORING_STARTED,
        MONITORING_STOPPED,
        MONITORING_ENHANCED,
        BRUTE_FORCE_DETECTED,
        SUSPICIOUS_FILE_ACCESS,
        SENSITIVE_FILE_ACCESS,
        MEMORY_ACCESS_VIOLATION,
        MEMORY_INTEGRITY_VIOLATION,
        UNEXPECTED_NETWORK_ACTIVITY,
        SYSTEM_INTEGRITY_VIOLATION,
        SUSPICIOUS_PROCESS_DETECTED,
        UNAUTHORIZED_FILE_CHANGE,
        SECURITY_LEVEL_ESCALATED,
        LOCKDOWN_INITIATED,
        CRITICAL_THREAT_DETECTED
    }
    
    /**
     * Security levels
     */
    public enum SecurityLevel {
        NORMAL,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Security alert types
     */
    public enum SecurityAlertType {
        BRUTE_FORCE_ATTACK,
        MEMORY_ATTACK,
        ESCALATING_THREATS,
        COORDINATED_ATTACK,
        CRITICAL_THREAT
    }
    
    /**
     * Security event data class
     */
    public static class SecurityEvent {
        private final SecurityEventType eventType;
        private final String description;
        private final SecurityLevel securityLevel;
        private final LocalDateTime timestamp;
        
        public SecurityEvent(SecurityEventType eventType, String description, 
                           SecurityLevel securityLevel, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.description = description;
            this.securityLevel = securityLevel;
            this.timestamp = timestamp;
        }
        
        public SecurityEventType getEventType() { return eventType; }
        public String getDescription() { return description; }
        public SecurityLevel getSecurityLevel() { return securityLevel; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (%s)", 
                securityLevel, eventType, description, timestamp);
        }
    }
    
    /**
     * Security alert data class
     */
    public static class SecurityAlert {
        private final SecurityAlertType alertType;
        private final String message;
        private final SecurityLevel securityLevel;
        private final LocalDateTime timestamp;
        
        public SecurityAlert(SecurityAlertType alertType, String message, 
                           SecurityLevel securityLevel, LocalDateTime timestamp) {
            this.alertType = alertType;
            this.message = message;
            this.securityLevel = securityLevel;
            this.timestamp = timestamp;
        }
        
        public SecurityAlertType getAlertType() { return alertType; }
        public String getMessage() { return message; }
        public SecurityLevel getSecurityLevel() { return securityLevel; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("ALERT [%s] %s: %s (%s)", 
                securityLevel, alertType, message, timestamp);
        }
    }
    
    /**
     * Security statistics data class
     */
    public static class SecurityStats {
        private final boolean monitoringActive;
        private final SecurityLevel currentLevel;
        private final int totalEvents;
        private final int rapidLoginAttempts;
        private final int suspiciousFileAccess;
        private final int memoryAccessAttempts;
        private final int networkActivity;
        
        public SecurityStats(boolean monitoringActive, SecurityLevel currentLevel, int totalEvents,
                           int rapidLoginAttempts, int suspiciousFileAccess, 
                           int memoryAccessAttempts, int networkActivity) {
            this.monitoringActive = monitoringActive;
            this.currentLevel = currentLevel;
            this.totalEvents = totalEvents;
            this.rapidLoginAttempts = rapidLoginAttempts;
            this.suspiciousFileAccess = suspiciousFileAccess;
            this.memoryAccessAttempts = memoryAccessAttempts;
            this.networkActivity = networkActivity;
        }
        
        public boolean isMonitoringActive() { return monitoringActive; }
        public SecurityLevel getCurrentLevel() { return currentLevel; }
        public int getTotalEvents() { return totalEvents; }
        public int getRapidLoginAttempts() { return rapidLoginAttempts; }
        public int getSuspiciousFileAccess() { return suspiciousFileAccess; }
        public int getMemoryAccessAttempts() { return memoryAccessAttempts; }
        public int getNetworkActivity() { return networkActivity; }
        
        @Override
        public String toString() {
            return String.format("SecurityStats{active=%s, level=%s, events=%d, logins=%d, files=%d, memory=%d, network=%d}", 
                monitoringActive, currentLevel, totalEvents, rapidLoginAttempts, 
                suspiciousFileAccess, memoryAccessAttempts, networkActivity);
        }
    }
    
    /**
     * Security alert listener interface
     */
    public interface SecurityAlertListener {
        void onSecurityAlert(SecurityAlert alert);
    }
}