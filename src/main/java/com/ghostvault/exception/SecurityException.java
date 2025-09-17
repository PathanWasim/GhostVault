package com.ghostvault.exception;

/**
 * Exception for security-related failures and violations
 */
public class SecurityException extends GhostVaultException {
    
    public SecurityException(String message) {
        super(message, ErrorCategory.SECURITY, ErrorSeverity.CRITICAL);
    }
    
    public SecurityException(String message, String technicalDetails) {
        super(message, null, ErrorCategory.SECURITY, ErrorSeverity.CRITICAL);
    }
    
    public SecurityException(String message, ErrorSeverity severity) {
        super(message, ErrorCategory.SECURITY, severity);
    }
    
    /**
     * Create exception for authentication failure
     */
    public static SecurityException authenticationFailed(String details) {
        return new SecurityException("Authentication failed", details);
    }
    
    /**
     * Create exception for access denied
     */
    public static SecurityException accessDenied(String resource) {
        return new SecurityException("Access denied to resource: " + resource, ErrorSeverity.HIGH);
    }
    
    /**
     * Create exception for security violation
     */
    public static SecurityException securityViolation(String violation, String details) {
        return new SecurityException("Security policy violation: " + violation + (details != null ? (": " + details) : ""), ErrorSeverity.CRITICAL);
    }
    
    /**
     * Create exception for tampering detection
     */
    public static SecurityException tamperingDetected(String details) {
        return new SecurityException("Data tampering has been detected" + (details != null ? (": " + details) : ""), ErrorSeverity.CRITICAL);
    }
    
    /**
     * Create exception for intrusion detection
     */
    public static SecurityException intrusionDetected(String source, String details) {
        return new SecurityException("Intrusion attempt detected from: " + source + (details != null ? (": " + details) : ""), ErrorSeverity.CRITICAL);
    }
    
    /**
     * Create exception for panic mode trigger
     */
    public static SecurityException panicModeTriggered(String reason) {
        return new SecurityException("Panic mode has been triggered: " + reason, ErrorSeverity.CRITICAL);
    }
    
    /**
     * Create exception for session expiry
     */
    public static SecurityException sessionExpired() {
        return new SecurityException("Your session has expired. Please log in again.", ErrorSeverity.MEDIUM);
    }
}