package com.ghostvault.exception;

/**
 * Exception for security-related failures and violations
 */
public class SecurityException extends GhostVaultException {
    
    public SecurityException(ErrorCode errorCode, String message) {
        super(errorCode, ErrorSeverity.CRITICAL, false, message, null, null);
    }
    
    public SecurityException(ErrorCode errorCode, String message, String technicalDetails) {
        super(errorCode, ErrorSeverity.CRITICAL, false, message, technicalDetails, null);
    }
    
    public SecurityException(ErrorCode errorCode, ErrorSeverity severity, String message) {
        super(errorCode, severity, false, message, null, null);
    }
    
    /**
     * Create exception for authentication failure
     */
    public static SecurityException authenticationFailed(String details) {
        return new SecurityException(ErrorCode.AUTHENTICATION_FAILED, 
            "Authentication failed", details);
    }
    
    /**
     * Create exception for access denied
     */
    public static SecurityException accessDenied(String resource) {
        return new SecurityException(ErrorCode.ACCESS_DENIED, 
            "Access denied to resource: " + resource);
    }
    
    /**
     * Create exception for security violation
     */
    public static SecurityException securityViolation(String violation, String details) {
        return new SecurityException(ErrorCode.SECURITY_VIOLATION, 
            "Security policy violation: " + violation, details);
    }
    
    /**
     * Create exception for tampering detection
     */
    public static SecurityException tamperingDetected(String details) {
        return new SecurityException(ErrorCode.TAMPERING_DETECTED, 
            "Data tampering has been detected", details);
    }
    
    /**
     * Create exception for intrusion detection
     */
    public static SecurityException intrusionDetected(String source, String details) {
        return new SecurityException(ErrorCode.INTRUSION_DETECTED, 
            "Intrusion attempt detected from: " + source, details);
    }
    
    /**
     * Create exception for panic mode trigger
     */
    public static SecurityException panicModeTriggered(String reason) {
        return new SecurityException(ErrorCode.PANIC_MODE_TRIGGERED, 
            "Panic mode has been triggered: " + reason);
    }
    
    /**
     * Create exception for session expiry
     */
    public static SecurityException sessionExpired() {
        return new SecurityException(ErrorCode.SESSION_EXPIRED, ErrorSeverity.MEDIUM,
            "Your session has expired. Please log in again.");
    }
}