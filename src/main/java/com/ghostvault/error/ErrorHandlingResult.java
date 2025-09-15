package com.ghostvault.error;

import com.ghostvault.exception.GhostVaultException;

/**
 * Result of error handling operation
 */
public class ErrorHandlingResult {
    
    private final GhostVaultException exception;
    private final ErrorHandler.RecoveryAction recoveryAction;
    private final boolean recovered;
    private final String recoveryMessage;
    private final long timestamp;
    
    public ErrorHandlingResult(GhostVaultException exception, ErrorHandler.RecoveryAction recoveryAction,
                              boolean recovered, String recoveryMessage) {
        this.exception = exception;
        this.recoveryAction = recoveryAction;
        this.recovered = recovered;
        this.recoveryMessage = recoveryMessage;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public GhostVaultException getException() { return exception; }
    public ErrorHandler.RecoveryAction getRecoveryAction() { return recoveryAction; }
    public boolean isRecovered() { return recovered; }
    public String getRecoveryMessage() { return recoveryMessage; }
    public long getTimestamp() { return timestamp; }
    
    /**
     * Check if the error was successfully handled
     */
    public boolean isSuccessfullyHandled() {
        return recovered || recoveryAction == ErrorHandler.RecoveryAction.IGNORE;
    }
    
    /**
     * Check if user intervention is required
     */
    public boolean requiresUserIntervention() {
        return recoveryAction == ErrorHandler.RecoveryAction.USER_INTERVENTION ||
               recoveryAction == ErrorHandler.RecoveryAction.RESTART_APPLICATION;
    }
    
    /**
     * Get user-friendly error message
     */
    public String getUserMessage() {
        if (exception != null) {
            return exception.getUserMessage();
        }
        return "An error occurred";
    }
    
    /**
     * Get recovery suggestion for user
     */
    public String getRecoverySuggestion() {
        if (exception != null) {
            return exception.getRecoveryAction();
        }
        return recoveryMessage;
    }
    
    /**
     * Get severity level
     */
    public GhostVaultException.ErrorSeverity getSeverity() {
        return exception != null ? exception.getSeverity() : GhostVaultException.ErrorSeverity.MEDIUM;
    }
    
    /**
     * Get error code
     */
    public int getErrorCode() {
        return exception != null ? exception.getErrorCodeValue() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("ErrorHandlingResult{code=%d, action=%s, recovered=%s, message='%s'}", 
            getErrorCode(), recoveryAction, recovered, recoveryMessage);
    }
}