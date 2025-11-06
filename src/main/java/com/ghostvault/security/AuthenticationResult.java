package com.ghostvault.security;

/**
 * Result of authentication attempt containing mode and status information
 */
public class AuthenticationResult {
    private final boolean success;
    private final VaultMode mode;
    private final String errorMessage;
    private final boolean isPanicMode;
    private final int remainingAttempts;
    
    /**
     * Create authentication result
     */
    public AuthenticationResult(boolean success, VaultMode mode, String errorMessage, 
                              boolean isPanicMode, int remainingAttempts) {
        this.success = success;
        this.mode = mode;
        this.errorMessage = errorMessage;
        this.isPanicMode = isPanicMode;
        this.remainingAttempts = remainingAttempts;
    }
    
    /**
     * Check if authentication was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the vault mode (null if authentication failed)
     */
    public VaultMode getMode() {
        return mode;
    }
    
    /**
     * Get error message (null if successful)
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Check if this is panic mode activation
     */
    public boolean isPanicMode() {
        return isPanicMode;
    }
    
    /**
     * Get remaining authentication attempts before lockout
     */
    public int getRemainingAttempts() {
        return remainingAttempts;
    }
    
    @Override
    public String toString() {
        return "AuthenticationResult{" +
                "success=" + success +
                ", mode=" + mode +
                ", errorMessage='" + errorMessage + '\'' +
                ", isPanicMode=" + isPanicMode +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}