package com.ghostvault.error;

import com.ghostvault.exception.*;
import com.ghostvault.audit.AuditManager;
import com.ghostvault.ui.NotificationManager;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Centralized error handling and recovery system
 * Provides consistent error processing, logging, and recovery mechanisms
 */
public class ErrorHandler {
    
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());
    
    private final AuditManager auditManager;
    private final NotificationManager notificationManager;
    private final ConcurrentHashMap<String, AtomicInteger> errorCounts;
    private final ConcurrentHashMap<String, LocalDateTime> lastErrorTimes;
    
    // Error handling configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long ERROR_COOLDOWN_MS = 5000; // 5 seconds
    private static final int MAX_ERRORS_PER_MINUTE = 10;
    
    public ErrorHandler(AuditManager auditManager, NotificationManager notificationManager) {
        this.auditManager = auditManager;
        this.notificationManager = notificationManager;
        this.errorCounts = new ConcurrentHashMap<>();
        this.lastErrorTimes = new ConcurrentHashMap<>();
    }
    
    /**
     * Handle exception with automatic recovery attempt
     */
    public <T> T handleWithRecovery(String operation, ThrowingSupplier<T> supplier, 
                                   RecoveryStrategy<T> recoveryStrategy) {
        return handleWithRecovery(operation, supplier, recoveryStrategy, null);
    }
    
    /**
     * Handle exception with automatic recovery attempt and user notification
     */
    public <T> T handleWithRecovery(String operation, ThrowingSupplier<T> supplier, 
                                   RecoveryStrategy<T> recoveryStrategy, 
                                   Consumer<String> userNotification) {
        
        String errorKey = operation + "_" + Thread.currentThread().getId();
        AtomicInteger attemptCount = errorCounts.computeIfAbsent(errorKey, k -> new AtomicInteger(0));
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                T result = supplier.get();
                
                // Reset error count on success
                errorCounts.remove(errorKey);
                lastErrorTimes.remove(errorKey);
                
                return result;
                
            } catch (Exception e) {
                attemptCount.incrementAndGet();
                lastErrorTimes.put(errorKey, LocalDateTime.now());
                
                // Log the error
                logError(operation, e, attempt);
                
                // Check if we should attempt recovery
                if (attempt < MAX_RETRY_ATTEMPTS && shouldAttemptRecovery(e)) {
                    
                    // Wait before retry
                    waitForRetry(attempt);
                    
                    // Attempt recovery if strategy provided
                    if (recoveryStrategy != null) {
                        try {
                            RecoveryResult<T> recoveryResult = recoveryStrategy.recover(e, attempt);
                            
                            if (recoveryResult.isSuccessful()) {
                                // Recovery successful
                                errorCounts.remove(errorKey);
                                lastErrorTimes.remove(errorKey);
                                
                                if (userNotification != null) {
                                    userNotification.accept("Operation recovered successfully");
                                }
                                
                                return recoveryResult.getResult();
                            }
                            
                        } catch (Exception recoveryException) {
                            // Recovery failed, log and continue with original error
                            logError("Recovery for " + operation, recoveryException, attempt);
                        }
                    }
                    
                } else {
                    // Final attempt failed or non-recoverable error
                    handleFinalError(operation, e, userNotification);
                    throw new RuntimeException("Operation failed after " + attempt + " attempts", e);
                }
            }
        }
        
        // Should never reach here
        throw new RuntimeException("Unexpected error in recovery loop for operation: " + operation);
    }
    
    /**
     * Handle exception without recovery
     */
    public void handleError(String operation, Exception e) {
        handleError(operation, e, null);
    }
    
    /**
     * Handle exception without recovery with user notification
     */
    public void handleError(String operation, Exception e, Consumer<String> userNotification) {
        logError(operation, e, 1);
        
        // Notify user if callback provided
        if (userNotification != null) {
            String userMessage = getUserFriendlyMessage(e);
            userNotification.accept(userMessage);
        }
        
        // Show notification if manager available
        if (notificationManager != null) {
            String userMessage = getUserFriendlyMessage(e);
            notificationManager.showError("Error", userMessage);
        }
    }
    
    /**
     * Log error with appropriate severity
     */
    private void logError(String operation, Exception e, int attempt) {
        String errorCode = generateErrorCode(e);
        
        // Determine log level based on exception type and attempt
        Level logLevel = determineLogLevel(e, attempt);
        
        // Log to system logger
        logger.log(logLevel, String.format("Error in operation '%s' (attempt %d): %s [%s]", 
            operation, attempt, e.getMessage(), errorCode), e);
        
        // Log to audit manager if available
        if (auditManager != null) {
            AuditManager.AuditSeverity severity = mapToAuditSeverity(e);
            String details = String.format("Operation: %s; Attempt: %d; Error: %s", 
                operation, attempt, errorCode);
            
            auditManager.logSecurityEvent("ERROR_OCCURRED", e.getMessage(), severity, null, details);
        }
    }
    
    /**
     * Determine if recovery should be attempted for this exception
     */
    private boolean shouldAttemptRecovery(Exception e) {
        if (e instanceof GhostVaultException) {
            GhostVaultException gve = (GhostVaultException) e;
            return gve.isRecoverable();
        }
        
        // Check for specific recoverable exceptions
        if (e instanceof java.io.IOException) {
            return true; // File I/O errors are often recoverable
        }
        
        if (e instanceof java.net.SocketTimeoutException) {
            return true; // Network timeouts are recoverable
        }
        
        if (e instanceof java.util.concurrent.TimeoutException) {
            return true; // General timeouts are recoverable
        }
        
        // Default to non-recoverable for unknown exceptions
        return false;
    }
    
    /**
     * Wait before retry with exponential backoff
     */
    private void waitForRetry(int attempt) {
        try {
            long waitTime = Math.min(1000 * (long) Math.pow(2, attempt - 1), 5000); // Max 5 seconds
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Handle final error after all recovery attempts failed
     */
    private void handleFinalError(String operation, Exception e, Consumer<String> userNotification) {
        String errorCode = generateErrorCode(e);
        
        // Log critical error
        logger.severe(String.format("Final error in operation '%s' after all recovery attempts: %s [%s]", 
            operation, e.getMessage(), errorCode));
        
        // Audit critical failure
        if (auditManager != null) {
            auditManager.logSecurityEvent("CRITICAL_ERROR", 
                "Operation failed after all recovery attempts", 
                AuditManager.AuditSeverity.CRITICAL, null, 
                "Operation: " + operation + "; Error: " + errorCode);
        }
        
        // Notify user
        if (userNotification != null) {
            String userMessage = getUserFriendlyMessage(e);
            userNotification.accept("Critical error: " + userMessage);
        }
    }
    
    /**
     * Generate user-friendly error message
     */
    private String getUserFriendlyMessage(Exception e) {
        if (e instanceof GhostVaultException) {
            return ((GhostVaultException) e).getUserMessage();
        }
        
        // Map common exceptions to user-friendly messages
        if (e instanceof java.io.FileNotFoundException) {
            return "The requested file could not be found.";
        }
        
        if (e instanceof java.io.IOException) {
            return "A file operation failed. Please check file permissions and disk space.";
        }
        
        if (e instanceof java.net.ConnectException) {
            return "Network connection failed. Please check your connection.";
        }
        
        if (e instanceof java.security.GeneralSecurityException) {
            return "A security error occurred. Please try again.";
        }
        
        if (e instanceof IllegalArgumentException) {
            return "Invalid input provided. Please check your data and try again.";
        }
        
        if (e instanceof OutOfMemoryError) {
            return "Insufficient memory to complete the operation. Please close other applications and try again.";
        }
        
        // Generic message for unknown exceptions
        return "An unexpected error occurred. Please try again or contact support.";
    }
    
    /**
     * Generate error code for tracking
     */
    private String generateErrorCode(Exception e) {
        if (e instanceof GhostVaultException) {
            return ((GhostVaultException) e).getErrorCode();
        }
        
        // Generate code based on exception class and message
        String className = e.getClass().getSimpleName();
        int messageHash = e.getMessage() != null ? Math.abs(e.getMessage().hashCode()) % 10000 : 0;
        return String.format("%s_%d", className, messageHash);
    }
    
    /**
     * Determine log level based on exception type and attempt
     */
    private Level determineLogLevel(Exception e, int attempt) {
        if (e instanceof GhostVaultException) {
            GhostVaultException gve = (GhostVaultException) e;
            switch (gve.getSeverity()) {
                case CRITICAL:
                    return Level.SEVERE;
                case HIGH:
                    return Level.WARNING;
                case MEDIUM:
                    return attempt > 1 ? Level.WARNING : Level.INFO;
                case LOW:
                    return Level.INFO;
            }
        }
        
        // Default based on attempt number
        if (attempt > 2) {
            return Level.WARNING;
        } else {
            return Level.INFO;
        }
    }
    
    /**
     * Map exception to audit severity
     */
    private AuditManager.AuditSeverity mapToAuditSeverity(Exception e) {
        if (e instanceof GhostVaultException) {
            GhostVaultException gve = (GhostVaultException) e;
            switch (gve.getSeverity()) {
                case CRITICAL:
                    return AuditManager.AuditSeverity.CRITICAL;
                case HIGH:
                    return AuditManager.AuditSeverity.ERROR;
                case MEDIUM:
                    return AuditManager.AuditSeverity.WARNING;
                case LOW:
                    return AuditManager.AuditSeverity.INFO;
            }
        }
        
        // Default mapping
        if (e instanceof SecurityException || e instanceof java.security.GeneralSecurityException) {
            return AuditManager.AuditSeverity.CRITICAL;
        }
        
        return AuditManager.AuditSeverity.ERROR;
    }
    
    /**
     * Check if error rate is too high
     */
    public boolean isErrorRateTooHigh(String operation) {
        String errorKey = operation + "_rate";
        AtomicInteger count = errorCounts.get(errorKey);
        LocalDateTime lastError = lastErrorTimes.get(errorKey);
        
        if (count == null || lastError == null) {
            return false;
        }
        
        // Check if we have too many errors in the last minute
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return lastError.isAfter(oneMinuteAgo) && count.get() > MAX_ERRORS_PER_MINUTE;
    }
    
    /**
     * Get error statistics for monitoring
     */
    public ErrorStatistics getErrorStatistics() {
        return new ErrorStatistics(errorCounts, lastErrorTimes);
    }
    
    /**
     * Functional interface for operations that can throw exceptions
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
    
    /**
     * Interface for recovery strategies
     */
    @FunctionalInterface
    public interface RecoveryStrategy<T> {
        RecoveryResult<T> recover(Exception originalException, int attemptNumber) throws Exception;
    }
    
    /**
     * Result of a recovery attempt
     */
    public static class RecoveryResult<T> {
        private final boolean successful;
        private final T result;
        private final String message;
        
        private RecoveryResult(boolean successful, T result, String message) {
            this.successful = successful;
            this.result = result;
            this.message = message;
        }
        
        public static <T> RecoveryResult<T> success(T result) {
            return new RecoveryResult<>(true, result, "Recovery successful");
        }
        
        public static <T> RecoveryResult<T> success(T result, String message) {
            return new RecoveryResult<>(true, result, message);
        }
        
        public static <T> RecoveryResult<T> failure(String message) {
            return new RecoveryResult<>(false, null, message);
        }
        
        public boolean isSuccessful() { return successful; }
        public T getResult() { return result; }
        public String getMessage() { return message; }
    }
    
    /**
     * Error statistics for monitoring
     */
    public static class ErrorStatistics {
        private final ConcurrentHashMap<String, AtomicInteger> errorCounts;
        private final ConcurrentHashMap<String, LocalDateTime> lastErrorTimes;
        
        public ErrorStatistics(ConcurrentHashMap<String, AtomicInteger> errorCounts, 
                             ConcurrentHashMap<String, LocalDateTime> lastErrorTimes) {
            this.errorCounts = new ConcurrentHashMap<>(errorCounts);
            this.lastErrorTimes = new ConcurrentHashMap<>(lastErrorTimes);
        }
        
        public int getTotalErrorCount() {
            return errorCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        }
        
        public int getErrorCount(String operation) {
            AtomicInteger count = errorCounts.get(operation);
            return count != null ? count.get() : 0;
        }
        
        public LocalDateTime getLastErrorTime(String operation) {
            return lastErrorTimes.get(operation);
        }
        
        public int getUniqueErrorTypes() {
            return errorCounts.size();
        }
    }
}