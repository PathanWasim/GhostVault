package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
// Use java.lang.Runnable (no import required)

/**
 * Manages user sessions with configurable timeout, activity tracking, and security monitoring
 */
public class SessionManager {
    
    private final int sessionTimeoutMinutes;
    private final AtomicLong lastActivityTime;
    private final AtomicInteger failedLoginAttempts;
    private final List<LoginAttempt> loginHistory;
    private final List<Runnable> timeoutListeners;
    private final List<Runnable> warningListeners;
    
    // Integration with SecurityAttemptManager
    private SecurityAttemptManager securityAttemptManager;
    
    private Timeline sessionTimer;
    private Timeline warningTimer;
    private boolean sessionActive;
    private boolean duressDetected;
    private LocalDateTime sessionStartTime;
    private Stage primaryStage;
    private Scene currentScene;
    
    // Security monitoring
    private final AtomicInteger suspiciousActivityCount;
    private final AtomicLong lastMouseActivity;
    private final AtomicLong lastKeyboardActivity;
    private boolean activityMonitoringEnabled;
    
    public SessionManager() {
        this(AppConfig.SESSION_TIMEOUT_MINUTES);
    }
    
    public SessionManager(int timeoutMinutes) {
        this.sessionTimeoutMinutes = timeoutMinutes;
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
    }
    
    /**
     * Start a new session
     */
    public void startSession(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionActive = true;
        this.sessionStartTime = LocalDateTime.now();
        this.lastActivityTime.set(System.currentTimeMillis());
        this.duressDetected = false;
        
        // Reset security counters
        this.suspiciousActivityCount.set(0);
        this.failedLoginAttempts.set(0);
        
        // Reset security attempt manager on successful session start
        if (securityAttemptManager != null) {
            securityAttemptManager.resetAttempts();
        }
        
        // Start session timer
        startSessionTimer();
        
        // Setup activity monitoring
        setupActivityMonitoring();
        
        logSessionEvent("Session started");
    }
    
    /**
     * Start a new session (compatibility method)
     */
    public void startSession() {
        startSession(null);
    }
    
    /**
     * End the current session
     */
    public void endSession() {
        this.sessionActive = false;
        
        // Stop timers
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        if (warningTimer != null) {
            warningTimer.stop();
        }
        
        // Clear activity monitoring
        clearActivityMonitoring();
        
        logSessionEvent("Session ended");
    }
    
    /**
     * Record user activity to reset timeout
     */
    public void recordActivity() {
        if (sessionActive) {
            lastActivityTime.set(System.currentTimeMillis());
            
            // Reset session timer
            if (sessionTimer != null) {
                sessionTimer.stop();
                startSessionTimer();
            }
            
            // Stop warning timer if running
            if (warningTimer != null) {
                warningTimer.stop();
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
        // Warning timer (1 minute before timeout)
        int warningTimeMinutes = Math.max(1, sessionTimeoutMinutes - 1);
        
        warningTimer = new Timeline(new KeyFrame(Duration.minutes(warningTimeMinutes), e -> {
            if (sessionActive) {
                notifyWarningListeners();
            }
        }));
        warningTimer.play();
        
        // Session timeout timer
        sessionTimer = new Timeline(new KeyFrame(Duration.minutes(sessionTimeoutMinutes), e -> {
            if (sessionActive) {
                handleSessionTimeout();
            }
        }));
        sessionTimer.play();
    }
    
    /**
     * Handle session timeout
     */
    private void handleSessionTimeout() {
        logSessionEvent("Session timeout");
        sessionActive = false;
        
        Platform.runLater(() -> {
            notifyTimeoutListeners();
        });
    }
    
    /**
     * Setup activity monitoring for the current scene
     */
    private void setupActivityMonitoring() {
        if (primaryStage != null && activityMonitoringEnabled) {
            currentScene = primaryStage.getScene();
            if (currentScene != null) {
                // Mouse activity monitoring
                currentScene.addEventFilter(MouseEvent.ANY, this::handleMouseEvent);
                
                // Keyboard activity monitoring
                currentScene.addEventFilter(KeyEvent.ANY, this::handleKeyEvent);
            }
        }
    }
    
    /**
     * Clear activity monitoring
     */
    private void clearActivityMonitoring() {
        if (currentScene != null) {
            currentScene.removeEventFilter(MouseEvent.ANY, this::handleMouseEvent);
            currentScene.removeEventFilter(KeyEvent.ANY, this::handleKeyEvent);
        }
    }
    
    /**
     * Handle mouse events for activity monitoring
     */
    private void handleMouseEvent(MouseEvent event) {
        recordMouseActivity();
        
        // Detect suspicious mouse patterns
        if (detectSuspiciousMouseActivity(event)) {
            suspiciousActivityCount.incrementAndGet();
            logSecurityEvent("Suspicious mouse activity detected");
        }
    }
    
    /**
     * Handle keyboard events for activity monitoring
     */
    private void handleKeyEvent(KeyEvent event) {
        recordKeyboardActivity();
        
        // Detect suspicious keyboard patterns
        if (detectSuspiciousKeyboardActivity(event)) {
            suspiciousActivityCount.incrementAndGet();
            logSecurityEvent("Suspicious keyboard activity detected");
        }
    }
    
    /**
     * Detect suspicious mouse activity patterns
     */
    private boolean detectSuspiciousMouseActivity(MouseEvent event) {
        // Simple heuristics for suspicious activity
        // In a real implementation, this would be more sophisticated
        
        // Rapid clicking detection
        long currentTime = System.currentTimeMillis();
        long timeSinceLastMouse = currentTime - lastMouseActivity.get();
        
        if (timeSinceLastMouse < 50 && event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            return true; // Too rapid clicking
        }
        
        return false;
    }
    
    /**
     * Detect suspicious keyboard activity patterns
     */
    private boolean detectSuspiciousKeyboardActivity(KeyEvent event) {
        // Simple heuristics for suspicious activity
        
        // Rapid key pressing detection
        long currentTime = System.currentTimeMillis();
        long timeSinceLastKey = currentTime - lastKeyboardActivity.get();
        
        if (timeSinceLastKey < 20 && event.getEventType() == KeyEvent.KEY_PRESSED) {
            return true; // Too rapid key pressing
        }
        
        return false;
    }
    
    /**
     * Record failed login attempt with SecurityAttemptManager integration
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
        
        // Integrate with SecurityAttemptManager if available
        if (securityAttemptManager != null) {
            String sourceInfo = String.format("User: %s, IP: %s", username, ipAddress);
            securityAttemptManager.recordFailedAttempt("Invalid credentials", sourceInfo);
        }
        
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
        
        // In a real implementation, this might trigger additional security measures
        // For now, we just log it
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
        
        if (!enabled) {
            clearActivityMonitoring();
        } else if (sessionActive) {
            setupActivityMonitoring();
        }
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
    
    /**
     * Set timeout callback (compatibility method)
     */
    public void setTimeoutCallback(Runnable callback) {
        addTimeoutListener(callback);
    }
    

    
    /**
     * Record failed login (compatibility method)
     */
    public void recordFailedLogin(String username) {
        recordFailedLogin(username, "Unknown IP");
    }
    

    
    /**
     * Pause session (compatibility method)
     */
    public void pauseSession() {
        // Pause session timer
        if (sessionTimer != null) {
            sessionTimer.pause();
        }
    }
    
    /**
     * Resume session (compatibility method)
     */
    public void resumeSession() {
        // Resume session timer
        if (sessionTimer != null) {
            sessionTimer.play();
        }
        recordActivity();
    }
    
    /**
     * Set SecurityAttemptManager for integration
     */
    public void setSecurityAttemptManager(SecurityAttemptManager securityAttemptManager) {
        this.securityAttemptManager = securityAttemptManager;
        System.out.println("🔗 SecurityAttemptManager integrated with SessionManager");
    }
    
    /**
     * Check if account is locked via SecurityAttemptManager
     */
    public boolean isAccountLocked(String username) {
        if (securityAttemptManager != null) {
            return securityAttemptManager.isLocked();
        }
        
        // Fallback to legacy logic
        return failedLoginAttempts.get() >= AppConfig.MAX_LOGIN_ATTEMPTS;
    }
    
    /**
     * Get remaining lockout time from SecurityAttemptManager
     */
    public long getRemainingLockoutTime() {
        if (securityAttemptManager != null) {
            return securityAttemptManager.getRemainingLockoutTime();
        }
        return 0;
    }
    
    /**
     * Get security status from SecurityAttemptManager
     */
    public String getSecurityStatus() {
        if (securityAttemptManager != null) {
            return securityAttemptManager.getSecurityStatus();
        }
        
        int attempts = failedLoginAttempts.get();
        if (attempts > 0) {
            return String.format("LEGACY - %d failed attempts", attempts);
        }
        return "NORMAL - No failed attempts";
    }
    
    /**
     * Reset security state through SecurityAttemptManager
     */
    public void resetSecurityState() {
        failedLoginAttempts.set(0);
        
        if (securityAttemptManager != null) {
            securityAttemptManager.resetAttempts();
        }
        
        logSessionEvent("Security state reset");
    }
    
    /**
     * Get integrated security statistics
     */
    public String getIntegratedSecurityStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Session Manager Security Statistics:\n");
        stats.append("Session Active: ").append(sessionActive).append("\n");
        stats.append("Failed Attempts (Legacy): ").append(failedLoginAttempts.get()).append("\n");
        stats.append("Login History Size: ").append(loginHistory.size()).append("\n");
        stats.append("Suspicious Activity Count: ").append(suspiciousActivityCount.get()).append("\n");
        
        if (securityAttemptManager != null) {
            stats.append("\nSecurityAttemptManager Integration:\n");
            stats.append("Status: ").append(securityAttemptManager.getSecurityStatus()).append("\n");
            stats.append("Attempt Count: ").append(securityAttemptManager.getAttemptCount()).append("\n");
            stats.append("Is Locked: ").append(securityAttemptManager.isLocked()).append("\n");
            
            if (securityAttemptManager.isLocked()) {
                stats.append("Remaining Lockout: ").append(securityAttemptManager.getRemainingLockoutSeconds()).append(" seconds\n");
            }
        } else {
            stats.append("\nSecurityAttemptManager: Not integrated\n");
        }
        
        return stats.toString();
    }
}