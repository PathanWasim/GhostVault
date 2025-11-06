package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Manages password attempt limiting and security lockout functionality
 * 
 * SECURITY FEATURES:
 * - Thread-safe attempt tracking with atomic operations
 * - 3-attempt limit with 30-second lockout timer
 * - Comprehensive security event logging
 * - Audit trail for security monitoring
 * 
 * @version 1.0.0 - Security Enhancement
 */
public class SecurityAttemptManager {
    
    // Security constants
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 30000; // 30 seconds
    private static final int MAX_AUDIT_EVENTS = 100;
    
    // Thread-safe attempt tracking
    private final AtomicInteger attemptCount = new AtomicInteger(0);
    private final AtomicLong lockoutStartTime = new AtomicLong(0);
    private final ReentrantLock stateLock = new ReentrantLock();
    
    // Security event tracking
    private final List<SecurityEvent> attemptHistory = Collections.synchronizedList(new ArrayList<>());
    private final AuditManager auditManager;
    private final SecurityLogger securityLogger;
    
    // State tracking
    private volatile boolean isLocked = false;
    private volatile String lastFailureReason = "";
    
    /**
     * Security event data structure
     */
    public static class SecurityEvent {
        private final long timestamp;
        private final String eventType;
        private final String description;
        private final String sourceInfo;
        
        public SecurityEvent(String eventType, String description, String sourceInfo) {
            this.timestamp = System.currentTimeMillis();
            this.eventType = eventType;
            this.description = description;
            this.sourceInfo = sourceInfo;
        }
        
        public long getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getSourceInfo() { return sourceInfo; }
        
        @Override
        public String toString() {
            return String.format("[%d] %s: %s (%s)", timestamp, eventType, description, sourceInfo);
        }
    }
    
    public SecurityAttemptManager(AuditManager auditManager) {
        this.auditManager = auditManager;
        this.securityLogger = new SecurityLogger(auditManager);
        this.securityLogger.start();
        
        logSecurityEvent("SECURITY_MANAGER_INITIALIZED", "SecurityAttemptManager created", "System");
        securityLogger.logSystemSecurityEvent("SECURITY_MANAGER_INITIALIZED", 
            "SecurityAttemptManager initialized with comprehensive logging", "System", null);
    }
    
    /**
     * Check if account is currently locked due to failed attempts
     * 
     * @return true if account is locked, false otherwise
     */
    public boolean isLocked() {
        stateLock.lock();
        try {
            if (!isLocked) {
                return false;
            }
            
            // Check if lockout period has expired
            long currentTime = System.currentTimeMillis();
            long lockoutStart = lockoutStartTime.get();
            
            if (currentTime - lockoutStart >= LOCKOUT_DURATION_MS) {
                // Lockout expired, reset state
                resetLockout();
                logSecurityEvent("LOCKOUT_EXPIRED", "Security lockout period expired", "System");
                return false;
            }
            
            return true;
        } finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Record a failed login attempt
     * 
     * @param reason Reason for the failed attempt
     * @param sourceInfo Source information (IP, user agent, etc.)
     */
    public void recordFailedAttempt(String reason, String sourceInfo) {
        stateLock.lock();
        try {
            int currentAttempts = attemptCount.incrementAndGet();
            lastFailureReason = reason;
            
            // Log the failed attempt
            String description = String.format("Failed login attempt %d/%d: %s", 
                currentAttempts, MAX_ATTEMPTS, reason);
            logSecurityEvent("LOGIN_ATTEMPT_FAILED", description, sourceInfo);
            
            // Enhanced security logging
            securityLogger.logAuthenticationEvent("LOGIN_FAILED", description, sourceInfo, 
                String.format("Attempt: %d/%d, Reason: %s", currentAttempts, MAX_ATTEMPTS, reason));
            
            // Check if we've reached the maximum attempts
            if (currentAttempts >= MAX_ATTEMPTS) {
                startLockout(sourceInfo);
            }
            
        } finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Reset attempt counter (called on successful login)
     */
    public void resetAttempts() {
        stateLock.lock();
        try {
            int previousAttempts = attemptCount.get();
            
            if (previousAttempts > 0) {
                attemptCount.set(0);
                lastFailureReason = "";
                
                // If we were locked, clear the lockout
                if (isLocked) {
                    resetLockout();
                }
                
                String description = String.format("Attempt counter reset after %d failed attempts", previousAttempts);
                logSecurityEvent("ATTEMPTS_RESET", description, "System");
                
                // Enhanced security logging
                securityLogger.logAuthenticationEvent("ATTEMPTS_RESET", description, "System", 
                    String.format("Previous attempts: %d, Was locked: %s", previousAttempts, isLocked));
            }
            
        } finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Get remaining lockout time in milliseconds
     * 
     * @return remaining lockout time, or 0 if not locked
     */
    public long getRemainingLockoutTime() {
        if (!isLocked) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long lockoutStart = lockoutStartTime.get();
        long elapsed = currentTime - lockoutStart;
        
        if (elapsed >= LOCKOUT_DURATION_MS) {
            return 0;
        }
        
        return LOCKOUT_DURATION_MS - elapsed;
    }
    
    /**
     * Get remaining lockout time in seconds
     * 
     * @return remaining lockout time in seconds
     */
    public int getRemainingLockoutSeconds() {
        long remainingMs = getRemainingLockoutTime();
        return (int) Math.ceil(remainingMs / 1000.0);
    }
    
    /**
     * Get current attempt count
     * 
     * @return number of failed attempts
     */
    public int getAttemptCount() {
        return attemptCount.get();
    }
    
    /**
     * Get maximum allowed attempts
     * 
     * @return maximum attempts before lockout
     */
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }
    
    /**
     * Get lockout duration in milliseconds
     * 
     * @return lockout duration
     */
    public long getLockoutDuration() {
        return LOCKOUT_DURATION_MS;
    }
    
    /**
     * Get last failure reason
     * 
     * @return description of last failure
     */
    public String getLastFailureReason() {
        return lastFailureReason;
    }
    
    /**
     * Get security event history
     * 
     * @return list of recent security events
     */
    public List<SecurityEvent> getSecurityEventHistory() {
        synchronized (attemptHistory) {
            return new ArrayList<>(attemptHistory);
        }
    }
    
    /**
     * Start security lockout
     */
    private void startLockout(String sourceInfo) {
        lockoutStartTime.set(System.currentTimeMillis());
        isLocked = true;
        
        String description = String.format("Security lockout activated for %d seconds after %d failed attempts", 
            LOCKOUT_DURATION_MS / 1000, MAX_ATTEMPTS);
        logSecurityEvent("SECURITY_LOCKOUT_ACTIVATED", description, sourceInfo);
        
        // Enhanced security logging for critical event
        securityLogger.logAuthenticationEvent("SECURITY_LOCKOUT", description, sourceInfo, 
            String.format("Attempts: %d, Duration: %ds, Timestamp: %d", 
                MAX_ATTEMPTS, LOCKOUT_DURATION_MS / 1000, System.currentTimeMillis()));
        
        // Log to audit manager as critical security event
        if (auditManager != null) {
            auditManager.logSecurityEvent("SECURITY_LOCKOUT", 
                "Account locked due to multiple failed login attempts", 
                AuditManager.AuditSeverity.CRITICAL, null, 
                String.format("Attempts: %d, Duration: %ds, Source: %s", 
                    MAX_ATTEMPTS, LOCKOUT_DURATION_MS / 1000, sourceInfo));
        }
    }
    
    /**
     * Reset lockout state
     */
    private void resetLockout() {
        isLocked = false;
        lockoutStartTime.set(0);
    }
    
    /**
     * Log security event to internal history and audit manager
     */
    private void logSecurityEvent(String eventType, String description, String sourceInfo) {
        // Add to internal history
        SecurityEvent event = new SecurityEvent(eventType, description, sourceInfo);
        
        synchronized (attemptHistory) {
            attemptHistory.add(event);
            
            // Maintain maximum history size
            while (attemptHistory.size() > MAX_AUDIT_EVENTS) {
                attemptHistory.remove(0);
            }
        }
        
        // Log to audit manager
        if (auditManager != null) {
            AuditManager.AuditSeverity severity = getSeverityForEventType(eventType);
            auditManager.logSecurityEvent(eventType, description, severity, null, sourceInfo);
        }
        
        // Console logging for debugging
        System.out.println("🔒 Security Event: " + event);
    }
    
    /**
     * Determine audit severity based on event type
     */
    private AuditManager.AuditSeverity getSeverityForEventType(String eventType) {
        switch (eventType) {
            case "SECURITY_LOCKOUT_ACTIVATED":
                return AuditManager.AuditSeverity.CRITICAL;
            case "LOGIN_ATTEMPT_FAILED":
                return AuditManager.AuditSeverity.WARNING;
            case "ATTEMPTS_RESET":
            case "LOCKOUT_EXPIRED":
                return AuditManager.AuditSeverity.INFO;
            default:
                return AuditManager.AuditSeverity.INFO;
        }
    }
    
    /**
     * Get security status summary
     * 
     * @return formatted security status string
     */
    public String getSecurityStatus() {
        stateLock.lock();
        try {
            if (isLocked()) {
                int remainingSeconds = getRemainingLockoutSeconds();
                return String.format("LOCKED - %d seconds remaining", remainingSeconds);
            } else if (attemptCount.get() > 0) {
                return String.format("ACTIVE - %d/%d attempts", attemptCount.get(), MAX_ATTEMPTS);
            } else {
                return "NORMAL - No failed attempts";
            }
        } finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Force reset all security state (for administrative use)
     */
    public void forceReset(String adminSource) {
        stateLock.lock();
        try {
            int previousAttempts = attemptCount.get();
            boolean wasLocked = isLocked;
            
            attemptCount.set(0);
            resetLockout();
            lastFailureReason = "";
            
            String description = String.format("Security state force reset - Previous attempts: %d, Was locked: %s", 
                previousAttempts, wasLocked);
            logSecurityEvent("SECURITY_FORCE_RESET", description, adminSource);
            
            // Enhanced security logging for administrative action
            securityLogger.logSystemSecurityEvent("SECURITY_FORCE_RESET", description, adminSource, 
                String.format("Admin action - Previous state: attempts=%d, locked=%s", previousAttempts, wasLocked));
            
        } finally {
            stateLock.unlock();
        }
    }
    
    /**
     * Get security logger for external access
     */
    public SecurityLogger getSecurityLogger() {
        return securityLogger;
    }
    
    /**
     * Shutdown security logging
     */
    public void shutdown() {
        if (securityLogger != null) {
            securityLogger.stop();
        }
    }
}