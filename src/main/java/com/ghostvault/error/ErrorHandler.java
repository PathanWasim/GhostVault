package com.ghostvault.error;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.exception.GhostVaultException;
import com.ghostvault.exception.SecurityException;
import com.ghostvault.security.PanicModeExecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Comprehensive error handling and recovery system for GhostVault
 * Provides centralized error processing, logging, and recovery mechanisms
 */
public class ErrorHandler {
    
    private final AuditManager auditManager;
    private final PanicModeExecutor panicModeExecutor;
    private final ConcurrentHashMap<String, AtomicInteger> errorCounts;
    private final List<ErrorListener> errorListeners;
    private final List<RecoveryStrategy> recoveryStrategies;
    
    // Error thresholds
    private static final int MAX_CRYPTO_ERRORS = 5;
    private static final int MAX_SECURITY_ERRORS = 3;
    private static final int MAX_VAULT_ERRORS = 10;
    
    // Recovery options
    public enum RecoveryAction {
        RETRY,
        IGNORE,
        FALLBACK,
        RESTART_COMPONENT,
        RESTART_APPLICATION,
        PANIC_MODE,
        USER_INTERVENTION
    }
    
    public ErrorHandler(AuditManager auditManager, PanicModeExecutor panicModeExecutor) {
        this.auditManager = auditManager;
        this.panicModeExecutor = panicModeExecutor;
        this.errorCounts = new ConcurrentHashMap<>();
        this.errorListeners = new ArrayList<>();
        this.recoveryStrategies = new ArrayList<>();
        
        // Initialize default recovery strategies
        initializeDefaultRecoveryStrategies();
    }
    
    /**
     * Handle an exception with automatic recovery
     */
    public ErrorHandlingResult handleException(Throwable throwable, String context) {
        return handleException(throwable, context, null);
    }
    
    /**
     * Handle an exception with custom recovery callback
     */
    public ErrorHandlingResult handleException(Throwable throwable, String context, 
                                             Consumer<RecoveryAction> recoveryCallback) {
        try {
            // Convert to GhostVaultException if needed
            GhostVaultException gvException = convertToGhostVaultException(throwable);
            
            // Log the error
            logError(gvException, context);
            
            // Update error counts
            updateErrorCounts(gvException);
            
            // Check for critical conditions
            checkCriticalConditions(gvException);
            
            // Determine recovery action
            RecoveryAction action = determineRecoveryAction(gvException, context);
            
            // Execute recovery if callback provided
            if (recoveryCallback != null) {
                recoveryCallback.accept(action);
            }
            
            // Notify listeners
            notifyErrorListeners(gvException, context, action);
            
            // Execute automatic recovery
            boolean recovered = executeRecovery(gvException, action, context);
            
            return new ErrorHandlingResult(gvException, action, recovered, getRecoveryMessage(action));
            
        } catch (Exception e) {
            // Error in error handling - log to console as fallback
            System.err.println("CRITICAL: Error in error handler: " + e.getMessage());
            e.printStackTrace();
            
            return new ErrorHandlingResult(
                convertToGhostVaultException(throwable), 
                RecoveryAction.USER_INTERVENTION, 
                false, 
                "Error handling failed - manual intervention required"
            );
        }
    }
    
    /**
     * Convert any throwable to GhostVaultException
     */
    private GhostVaultException convertToGhostVaultException(Throwable throwable) {
        if (throwable instanceof GhostVaultException) {
            return (GhostVaultException) throwable;
        }
        
        // Map common exceptions to appropriate error codes
        if (throwable instanceof java.io.FileNotFoundException) {
            return new GhostVaultException(GhostVaultException.ErrorCode.FILE_NOT_FOUND, 
                throwable.getMessage(), throwable);
        }
        
        if (throwable instanceof java.io.IOException) {
            return new GhostVaultException(GhostVaultException.ErrorCode.IO_ERROR, 
                throwable.getMessage(), throwable);
        }
        
        if (throwable instanceof java.security.GeneralSecurityException) {
            return new com.ghostvault.exception.CryptographicException(
                GhostVaultException.ErrorCode.CRYPTO_ALGORITHM_ERROR, 
                throwable.getMessage(), throwable);
        }
        
        if (throwable instanceof OutOfMemoryError) {
            return new GhostVaultException(GhostVaultException.ErrorCode.INSUFFICIENT_MEMORY, 
                GhostVaultException.ErrorSeverity.CRITICAL, false,
                "Application is out of memory", null, throwable);
        }
        
        if (throwable instanceof SecurityException) {
            return new com.ghostvault.exception.SecurityException(
                GhostVaultException.ErrorCode.SECURITY_VIOLATION, 
                throwable.getMessage());
        }
        
        // Default to internal error
        return new GhostVaultException(GhostVaultException.ErrorCode.INTERNAL_ERROR, 
            GhostVaultException.ErrorSeverity.MEDIUM, true,
            "An unexpected error occurred: " + throwable.getMessage(), 
            getStackTrace(throwable), throwable);
    }
    
    /**
     * Log error to audit system
     */
    private void logError(GhostVaultException exception, String context) {
        if (auditManager != null) {
            auditManager.logError(
                exception.getErrorCode().toString(),
                exception.getUserMessage(),
                exception.getTechnicalDetails(),
                context
            );
        } else {
            // Fallback to console logging
            System.err.println(String.format("[%s] ERROR %s in %s: %s", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                exception.getErrorCode(),
                context,
                exception.getFormattedMessage()));
        }
    }
    
    /**
     * Update error counts for pattern detection
     */
    private void updateErrorCounts(GhostVaultException exception) {
        String errorKey = exception.getErrorCode().toString();
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicInteger(0)).incrementAndGet();
        
        // Also track by category
        String categoryKey = getCategoryKey(exception.getErrorCode());
        errorCounts.computeIfAbsent(categoryKey, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * Get category key for error code
     */
    private String getCategoryKey(GhostVaultException.ErrorCode errorCode) {
        int code = errorCode.getCode();
        
        if (code >= 1000 && code < 1100) return "AUTH_ERRORS";
        if (code >= 1100 && code < 1200) return "CRYPTO_ERRORS";
        if (code >= 1200 && code < 1300) return "FILE_ERRORS";
        if (code >= 1300 && code < 1400) return "VAULT_ERRORS";
        if (code >= 1700 && code < 1800) return "SECURITY_ERRORS";
        
        return "OTHER_ERRORS";
    }
    
    /**
     * Check for critical conditions that require immediate action
     */
    private void checkCriticalConditions(GhostVaultException exception) {
        // Check if panic mode should be triggered
        if (exception.shouldTriggerPanicMode()) {
            triggerPanicMode("Critical security exception: " + exception.getErrorCode());
            return;
        }
        
        // Check error count thresholds
        String categoryKey = getCategoryKey(exception.getErrorCode());
        int categoryCount = errorCounts.getOrDefault(categoryKey, new AtomicInteger(0)).get();
        
        switch (categoryKey) {
            case "CRYPTO_ERRORS":
                if (categoryCount >= MAX_CRYPTO_ERRORS) {
                    triggerPanicMode("Too many cryptographic errors detected");
                }
                break;
                
            case "SECURITY_ERRORS":
                if (categoryCount >= MAX_SECURITY_ERRORS) {
                    triggerPanicMode("Multiple security violations detected");
                }
                break;
                
            case "VAULT_ERRORS":
                if (categoryCount >= MAX_VAULT_ERRORS) {
                    // Don't panic, but log critical alert
                    if (auditManager != null) {
                        auditManager.logSecurityEvent("VAULT_INSTABILITY", 
                            "Multiple vault errors detected", 
                            AuditManager.AuditSeverity.CRITICAL, null, 
                            "Error count: " + categoryCount);
                    }
                }
                break;
        }
    }
    
    /**
     * Trigger panic mode
     */
    private void triggerPanicMode(String reason) {
        try {
            if (auditManager != null) {
                auditManager.logPanicMode("ERROR_HANDLER", reason);
            }
            
            if (panicModeExecutor != null) {
                panicModeExecutor.executePanicMode();
            }
            
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to execute panic mode: " + e.getMessage());
        }
    }
    
    /**
     * Determine appropriate recovery action
     */
    private RecoveryAction determineRecoveryAction(GhostVaultException exception, String context) {
        // Check custom recovery strategies first
        for (RecoveryStrategy strategy : recoveryStrategies) {
            if (strategy.canHandle(exception, context)) {
                return strategy.getRecoveryAction(exception, context);
            }
        }
        
        // Default recovery logic based on error type and severity
        switch (exception.getSeverity()) {
            case CRITICAL:
                if (exception.isSecurityError()) {
                    return RecoveryAction.PANIC_MODE;
                }
                return RecoveryAction.RESTART_APPLICATION;
                
            case HIGH:
                if (exception.isRecoverable()) {
                    return RecoveryAction.RESTART_COMPONENT;
                }
                return RecoveryAction.USER_INTERVENTION;
                
            case MEDIUM:
                if (exception.isRecoverable()) {
                    return RecoveryAction.RETRY;
                }
                return RecoveryAction.FALLBACK;
                
            case LOW:
                return RecoveryAction.IGNORE;
                
            default:
                return RecoveryAction.USER_INTERVENTION;
        }
    }
    
    /**
     * Execute recovery action
     */
    private boolean executeRecovery(GhostVaultException exception, RecoveryAction action, String context) {
        try {
            switch (action) {
                case RETRY:
                    // Automatic retry is handled by the caller
                    return true;
                    
                case IGNORE:
                    // Log and continue
                    return true;
                    
                case FALLBACK:
                    // Execute fallback logic if available
                    return executeFallback(exception, context);
                    
                case RESTART_COMPONENT:
                    // Component restart is handled by the caller
                    return false;
                    
                case RESTART_APPLICATION:
                    // Application restart requires user action
                    return false;
                    
                case PANIC_MODE:
                    triggerPanicMode("Recovery action: " + exception.getErrorCode());
                    return true;
                    
                case USER_INTERVENTION:
                    // Requires manual intervention
                    return false;
                    
                default:
                    return false;
            }
            
        } catch (Exception e) {
            System.err.println("Recovery execution failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute fallback logic
     */
    private boolean executeFallback(GhostVaultException exception, String context) {
        // Implement specific fallback strategies based on error type
        switch (exception.getErrorCode()) {
            case FILE_NOT_FOUND:
                // Could try alternative file locations
                return false;
                
            case NETWORK_ERROR:
                // Could switch to offline mode
                return false;
                
            case INSUFFICIENT_MEMORY:
                // Could trigger garbage collection
                System.gc();
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Get recovery message for user display
     */
    private String getRecoveryMessage(RecoveryAction action) {
        switch (action) {
            case RETRY:
                return "The operation will be retried automatically.";
            case IGNORE:
                return "The error has been logged and will be ignored.";
            case FALLBACK:
                return "Attempting alternative approach.";
            case RESTART_COMPONENT:
                return "The affected component needs to be restarted.";
            case RESTART_APPLICATION:
                return "The application needs to be restarted to recover.";
            case PANIC_MODE:
                return "Critical security issue detected - emergency procedures activated.";
            case USER_INTERVENTION:
                return "Manual intervention is required to resolve this issue.";
            default:
                return "Recovery action determined.";
        }
    }
    
    /**
     * Initialize default recovery strategies
     */
    private void initializeDefaultRecoveryStrategies() {
        // Add default strategies for common scenarios
        
        // File system recovery
        recoveryStrategies.add(new RecoveryStrategy() {
            @Override
            public boolean canHandle(GhostVaultException exception, String context) {
                return exception.getErrorCode() == GhostVaultException.ErrorCode.DISK_FULL;
            }
            
            @Override
            public RecoveryAction getRecoveryAction(GhostVaultException exception, String context) {
                return RecoveryAction.USER_INTERVENTION;
            }
        });
        
        // Authentication recovery
        recoveryStrategies.add(new RecoveryStrategy() {
            @Override
            public boolean canHandle(GhostVaultException exception, String context) {
                return exception.getErrorCode() == GhostVaultException.ErrorCode.INVALID_PASSWORD;
            }
            
            @Override
            public RecoveryAction getRecoveryAction(GhostVaultException exception, String context) {
                return RecoveryAction.USER_INTERVENTION;
            }
        });
    }
    
    /**
     * Add custom recovery strategy
     */
    public void addRecoveryStrategy(RecoveryStrategy strategy) {
        recoveryStrategies.add(strategy);
    }
    
    /**
     * Add error listener
     */
    public void addErrorListener(ErrorListener listener) {
        errorListeners.add(listener);
    }
    
    /**
     * Notify error listeners
     */
    private void notifyErrorListeners(GhostVaultException exception, String context, RecoveryAction action) {
        for (ErrorListener listener : errorListeners) {
            try {
                listener.onError(exception, context, action);
            } catch (Exception e) {
                System.err.println("Error listener failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get error statistics
     */
    public ErrorStatistics getErrorStatistics() {
        return new ErrorStatistics(errorCounts);
    }
    
    /**
     * Reset error counts
     */
    public void resetErrorCounts() {
        errorCounts.clear();
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Interface for recovery strategies
     */
    public interface RecoveryStrategy {
        boolean canHandle(GhostVaultException exception, String context);
        RecoveryAction getRecoveryAction(GhostVaultException exception, String context);
    }
    
    /**
     * Interface for error listeners
     */
    public interface ErrorListener {
        void onError(GhostVaultException exception, String context, RecoveryAction action);
    }
}