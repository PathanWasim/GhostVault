package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.DecoyManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Basic session manager without JavaFX dependencies for core functionality and testing
 */
public class BasicSessionManager {
    
    private final int sessionTimeoutMinutes;
    private final AtomicLong lastActivityTime;
    private final AtomicInteger failedLoginAttempts;
    private final List<LoginAttempt> loginHistory;
    private final List<Runnable> timeoutListeners;
    private final List<Runnable> warningListeners;
    
    private ScheduledExecutorService scheduler;
    private boolean sessionActive;
    private boolean duressDetected;
    private LocalDateTime sessionStartTime;
    
    // Security monitoring
    private final AtomicInteger suspiciousActivityCount;
    private final AtomicLong lastMouseActivity;
    private final AtomicLong lastKeyboardActivity;
    private boolean activityMonitoringEnabled;
    
    // CRITICAL: Decoy management for vault protection
    private DecoyManager decoyManager;
    private String vaultPath;
    
    public BasicSessionManager() {
        this(AppConfig.SESSION_TIMEOUT_MINUTES);
    }
    
    public BasicSessionManager(int timeoutMinutes) {
        this(timeoutMinutes, null);
    }
    
    public BasicSessionManager(int timeoutMinutes, String vaultPath) {
        this.sessionTimeoutMinutes = timeoutMinutes;
        this.vaultPath = vaultPath;
        this.lastActivityTime = new AtomicLong(System.currentTimeMillis());
        this.failedLoginAttempts = new AtomicInteger(0);
        this.loginHistory = new ArrayList<>();
        this.timeoutListeners = new ArrayList<>();
        this.warningListeners = new ArrayList<>();
        this.suspiciousActivityCount = new AtomicInteger(0);
        this.lastMouseActivity = new AtomicLong(System.currentTimeMillis());
        this.lastKeyboardActivity = new AtomicLong(System.currentTimeMillis());
        this.sessionActive = false;
        this.duressDetected = false;
        this.activityMonitoringEnabled = true;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Initialize DecoyManager if vault path is provided
        if (vaultPath != null) {
            initializeDecoyManager(vaultPath);
        }
    }
    
    /**
     * Initialize DecoyManager with proper vault paths
     */
    private void initializeDecoyManager(String vaultPath) {
        try {
            Path realVaultPath = Paths.get(vaultPath);
            Path decoyVaultPath = Paths.get(vaultPath + "_decoy");
            this.decoyManager = new DecoyManager(realVaultPath, decoyVaultPath);
            System.out.println("✓ DecoyManager initialized for session");
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not initialize DecoyManager: " + e.getMessage());
            this.decoyManager = null;
        }
    }
    
    /**
     * Start a new session
     */
    public void startSession() {
        this.sessionActive = true;
        this.sessionStartTime = LocalDateTime.now();
        this.lastActivityTime.set(System.currentTimeMillis());
        this.duressDetected = false;
        
        // Reset security counters
        this.suspiciousActivityCount.set(0);
        this.failedLoginAttempts.set(0);
        
        // Start session timer
        startSessionTimer();
        
        logSessionEvent("Session started");
    }
    
    /**
     * End the current session
     */
    public void endSession() {
        this.sessionActive = false;
        
        // Stop scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            scheduler = Executors.newScheduledThreadPool(2);
        }
        
        logSessionEvent("Session ended");
    }
    
    /**
     * Record user activity to reset timeout
     */
    public void recordActivity() {
        if (sessionActive) {
            lastActivityTime.set(System.currentTimeMillis());
            
            // Restart session timer
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                scheduler = Executors.newScheduledThreadPool(2);
                startSessionTimer();
            }
        }
    }
    
    /**
     * Record mouse activity
     */
    public void recordMouseActivity() {
        lastMouseActivity.set(System.currentTimeMillis());
        recordActivity();
    }
    
    /**
     * Record keyboard activity
     */
    public void recordKeyboardActivity() {
        lastKeyboardActivity.set(System.currentTimeMillis());
        recordActivity();
    }
    
    /**
     * Start session timeout timer
     */
    private void startSessionTimer() {
        if (scheduler == null || scheduler.isShutdown()) {
            return;
        }
        
        // Warning timer (1 minute before timeout)
        int warningTimeMinutes = Math.max(1, sessionTimeoutMinutes - 1);
        
        scheduler.schedule(() -> {
            if (sessionActive) {
                notifyWarningListeners();
            }
        }, warningTimeMinutes, TimeUnit.MINUTES);
        
        // Session timeout timer
        scheduler.schedule(() -> {
            if (sessionActive) {
                handleSessionTimeout();
            }
        }, sessionTimeoutMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * Handle session timeout
     */
    private void handleSessionTimeout() {
        logSessionEvent("Session timeout");
        sessionActive = false;
        notifyTimeoutListeners();
    }
    
    /**
     * Record failed login attempt
     */
    public void recordFailedLogin(String username, String ipAddress) {
        int attempts = failedLoginAttempts.incrementAndGet();
        
        LoginAttempt attempt = new LoginAttempt(
            username, 
            ipAddress, 
            LocalDateTime.now(), 
            false
        );
        
        loginHistory.add(attempt);
        
        logSecurityEvent("Failed login attempt #" + attempts + " for user: " + username);
        
        // Check for duress conditions
        if (attempts >= AppConfig.MAX_LOGIN_ATTEMPTS) {
            detectDuress();
        }
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String username, String ipAddress) {
        failedLoginAttempts.set(0); // Reset failed attempts
        
        LoginAttempt attempt = new LoginAttempt(
            username, 
            ipAddress, 
            LocalDateTime.now(), 
            true
        );
        
        loginHistory.add(attempt);
        
        logSecurityEvent("Successful login for user: " + username);
    }
    
    /**
     * Detect duress conditions
     */
    private void detectDuress() {
        duressDetected = true;
        logSecurityEvent("DURESS DETECTED - Multiple failed login attempts");
    }
    
    /**
     * Check if session is active
     */
    public boolean isSessionActive() {
        return sessionActive;
    }
    
    /**
     * Check if duress has been detected
     */
    public boolean isDuressDetected() {
        return duressDetected;
    }
    
    /**
     * Get session duration in minutes
     */
    public long getSessionDurationMinutes() {
        if (sessionStartTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Get time until session timeout in minutes
     */
    public long getTimeUntilTimeoutMinutes() {
        if (!sessionActive) {
            return 0;
        }
        
        long timeSinceActivity = System.currentTimeMillis() - lastActivityTime.get();
        long timeoutMillis = sessionTimeoutMinutes * 60 * 1000;
        long remainingMillis = timeoutMillis - timeSinceActivity;
        
        return Math.max(0, remainingMillis / (60 * 1000));
    }
    
    /**
     * Get failed login attempts count
     */
    public int getFailedLoginAttempts() {
        return failedLoginAttempts.get();
    }
    
    /**
     * Get suspicious activity count
     */
    public int getSuspiciousActivityCount() {
        return suspiciousActivityCount.get();
    }
    
    /**
     * Get login history
     */
    public List<LoginAttempt> getLoginHistory() {
        return new ArrayList<>(loginHistory);
    }
    
    /**
     * Add timeout listener
     */
    public void addTimeoutListener(Runnable listener) {
        timeoutListeners.add(listener);
    }
    
    /**
     * Add warning listener
     */
    public void addWarningListener(Runnable listener) {
        warningListeners.add(listener);
    }
    
    /**
     * Remove timeout listener
     */
    public void removeTimeoutListener(Runnable listener) {
        timeoutListeners.remove(listener);
    }
    
    /**
     * Remove warning listener
     */
    public void removeWarningListener(Runnable listener) {
        warningListeners.remove(listener);
    }
    
    /**
     * Notify timeout listeners
     */
    private void notifyTimeoutListeners() {
        for (Runnable listener : timeoutListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                // Continue with other listeners
            }
        }
    }
    
    /**
     * Notify warning listeners
     */
    private void notifyWarningListeners() {
        for (Runnable listener : warningListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                // Continue with other listeners
            }
        }
    }
    
    /**
     * Enable or disable activity monitoring
     */
    public void setActivityMonitoringEnabled(boolean enabled) {
        this.activityMonitoringEnabled = enabled;
    }
    
    /**
     * Force session timeout (for testing or manual logout)
     */
    public void forceTimeout() {
        handleSessionTimeout();
    }
    
    /**
     * Extend session timeout
     */
    public void extendSession(int additionalMinutes) {
        if (sessionActive) {
            lastActivityTime.set(System.currentTimeMillis() + (additionalMinutes * 60 * 1000));
            logSessionEvent("Session extended by " + additionalMinutes + " minutes");
        }
    }
    
    /**
     * Get session statistics
     */
    public SessionStats getSessionStats() {
        return new SessionStats(
            sessionActive,
            getSessionDurationMinutes(),
            getTimeUntilTimeoutMinutes(),
            failedLoginAttempts.get(),
            suspiciousActivityCount.get(),
            loginHistory.size(),
            duressDetected
        );
    }
    
    /**
     * Log session event
     */
    private void logSessionEvent(String event) {
        // In a real implementation, this would write to an audit log
        System.out.println("[SESSION] " + LocalDateTime.now() + ": " + event);
    }
    
    /**
     * Log security event
     */
    private void logSecurityEvent(String event) {
        // In a real implementation, this would write to a security log
        System.out.println("[SECURITY] " + LocalDateTime.now() + ": " + event);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        endSession();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    // ========================= DECOY MODE METHODS =========================
    
    /**
     * Check if currently in decoy mode
     */
    public boolean isDecoyMode() {
        return decoyManager != null && decoyManager.isDecoyMode();
    }
    
    /**
     * Activate decoy mode safely
     */
    public void activateDecoyMode() {
        if (decoyManager == null) {
            System.err.println("⚠️ Cannot activate decoy mode - DecoyManager not initialized");
            return;
        }
        
        try {
            // Verify vault integrity before switching
            if (!decoyManager.verifyRealVaultIntegrity()) {
                System.err.println("⚠️ Cannot activate decoy mode - vault integrity check failed");
                return;
            }
            
            decoyManager.switchToDecoyMode();
            decoyManager.autoGenerateForNewDevice();
            decoyManager.ensureMinimumDecoyFiles(8); // Ensure at least 8 decoy files
            
            logSecurityEvent("DECOY MODE ACTIVATED - Real vault protected");
            System.out.println("🎭 Decoy mode activated successfully");
            System.out.println("🔒 Real vault protected and hidden");
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to activate decoy mode: " + e.getMessage());
            logSecurityEvent("DECOY MODE ACTIVATION FAILED: " + e.getMessage());
        }
    }
    
    /**
     * Deactivate decoy mode and return to real vault
     */
    public void deactivateDecoyMode() {
        if (decoyManager == null || !decoyManager.isDecoyMode()) {
            System.out.println("Not in decoy mode");
            return;
        }
        
        try {
            decoyManager.switchToRealMode();
            logSecurityEvent("DECOY MODE DEACTIVATED - Returned to real vault");
            System.out.println("✓ Returned to real vault mode");
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to deactivate decoy mode: " + e.getMessage());
            logSecurityEvent("DECOY MODE DEACTIVATION FAILED: " + e.getMessage());
            // Emergency fallback
            decoyManager.emergencySwitchToRealVault();
        }
    }
    
    /**
     * Refresh decoy vault with new fake data
     */
    public void refreshDecoyData() {
        if (decoyManager == null || !decoyManager.isDecoyMode()) {
            return;
        }
        
        try {
            decoyManager.refreshDecoyVault();
            logSecurityEvent("DECOY DATA REFRESHED");
            System.out.println("✓ Decoy data refreshed");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to refresh decoy data: " + e.getMessage());
            logSecurityEvent("DECOY DATA REFRESH FAILED: " + e.getMessage());
        }
    }
    
    /**
     * Get current vault path (real or decoy)
     */
    public String getCurrentVaultPath() {
        if (decoyManager != null) {
            return decoyManager.getCurrentVaultPath().toString();
        }
        return vaultPath;
    }
    
    /**
     * Get DecoyManager instance (for advanced operations)
     */
    public DecoyManager getDecoyManager() {
        return decoyManager;
    }
    
    /**
     * Emergency switch to real vault (bypasses all checks)
     */
    public void emergencyRealVaultAccess() {
        if (decoyManager != null) {
            decoyManager.emergencySwitchToRealVault();
            logSecurityEvent("EMERGENCY REAL VAULT ACCESS");
        }
    }
    
    /**
     * Login attempt data class
     */
    public static class LoginAttempt {
        private final String username;
        private final String ipAddress;
        private final LocalDateTime timestamp;
        private final boolean successful;
        
        public LoginAttempt(String username, String ipAddress, LocalDateTime timestamp, boolean successful) {
            this.username = username;
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
            this.successful = successful;
        }
        
        public String getUsername() { return username; }
        public String getIpAddress() { return ipAddress; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isSuccessful() { return successful; }
        
        @Override
        public String toString() {
            return String.format("LoginAttempt{user='%s', ip='%s', time=%s, success=%s}", 
                username, ipAddress, timestamp, successful);
        }
    }
    
    /**
     * Session statistics data class
     */
    public static class SessionStats {
        private final boolean active;
        private final long durationMinutes;
        private final long timeUntilTimeoutMinutes;
        private final int failedLogins;
        private final int suspiciousActivity;
        private final int totalLogins;
        private final boolean duressDetected;
        
        public SessionStats(boolean active, long durationMinutes, long timeUntilTimeoutMinutes,
                          int failedLogins, int suspiciousActivity, int totalLogins, boolean duressDetected) {
            this.active = active;
            this.durationMinutes = durationMinutes;
            this.timeUntilTimeoutMinutes = timeUntilTimeoutMinutes;
            this.failedLogins = failedLogins;
            this.suspiciousActivity = suspiciousActivity;
            this.totalLogins = totalLogins;
            this.duressDetected = duressDetected;
        }
        
        public boolean isActive() { return active; }
        public long getDurationMinutes() { return durationMinutes; }
        public long getTimeUntilTimeoutMinutes() { return timeUntilTimeoutMinutes; }
        public int getFailedLogins() { return failedLogins; }
        public int getSuspiciousActivity() { return suspiciousActivity; }
        public int getTotalLogins() { return totalLogins; }
        public boolean isDuressDetected() { return duressDetected; }
        
        @Override
        public String toString() {
            return String.format("SessionStats{active=%s, duration=%dm, timeout=%dm, failed=%d, suspicious=%d, total=%d, duress=%s}", 
                active, durationMinutes, timeUntilTimeoutMinutes, failedLogins, suspiciousActivity, totalLogins, duressDetected);
        }
    }
}