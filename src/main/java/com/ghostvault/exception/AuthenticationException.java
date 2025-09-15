package com.ghostvault.exception;

/**
 * Exception for authentication and authorization errors
 */
public class AuthenticationException extends GhostVaultException {
    
    public enum AuthErrorType {
        INVALID_PASSWORD("Invalid password"),
        ACCOUNT_LOCKED("Account locked"),
        SESSION_EXPIRED("Session expired"),
        INSUFFICIENT_PRIVILEGES("Insufficient privileges"),
        AUTHENTICATION_FAILED("Authentication failed"),
        PASSWORD_EXPIRED("Password expired"),
        TOO_MANY_ATTEMPTS("Too many failed attempts"),
        INVALID_SESSION("Invalid session"),
        UNAUTHORIZED_ACCESS("Unauthorized access attempt"),
        CREDENTIAL_CORRUPTION("Credential data corruption");
        
        private final String description;
        
        AuthErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final AuthErrorType authErrorType;
    private final int attemptCount;
    private final String username;
    
    public AuthenticationException(String message, AuthErrorType errorType) {
        super(message, ErrorCategory.AUTHENTICATION, determineSeverity(errorType), isRecoverable(errorType));
        this.authErrorType = errorType;
        this.attemptCount = 0;
        this.username = null;
    }
    
    public AuthenticationException(String message, AuthErrorType errorType, int attemptCount) {
        super(message, ErrorCategory.AUTHENTICATION, determineSeverity(errorType), isRecoverable(errorType));
        this.authErrorType = errorType;
        this.attemptCount = attemptCount;
        this.username = null;
    }
    
    public AuthenticationException(String message, AuthErrorType errorType, String username) {
        super(message, ErrorCategory.AUTHENTICATION, determineSeverity(errorType), 
              generateUserMessage(errorType), null, isRecoverable(errorType));
        this.authErrorType = errorType;
        this.attemptCount = 0;
        this.username = username;
    }
    
    public AuthenticationException(String message, Throwable cause, AuthErrorType errorType) {
        super(message, cause, ErrorCategory.AUTHENTICATION, determineSeverity(errorType));
        this.authErrorType = errorType;
        this.attemptCount = 0;
        this.username = null;
    }
    
    private static ErrorSeverity determineSeverity(AuthErrorType errorType) {
        switch (errorType) {
            case INVALID_PASSWORD:
            case SESSION_EXPIRED:
                return ErrorSeverity.MEDIUM;
            case ACCOUNT_LOCKED:
            case TOO_MANY_ATTEMPTS:
            case UNAUTHORIZED_ACCESS:
                return ErrorSeverity.HIGH;
            case CREDENTIAL_CORRUPTION:
                return ErrorSeverity.CRITICAL;
            default:
                return ErrorSeverity.MEDIUM;
        }
    }
    
    private static boolean isRecoverable(AuthErrorType errorType) {
        switch (errorType) {
            case INVALID_PASSWORD:
            case SESSION_EXPIRED:
            case INVALID_SESSION:
                return true;
            case CREDENTIAL_CORRUPTION:
                return false;
            default:
                return true;
        }
    }
    
    private static String generateUserMessage(AuthErrorType errorType) {
        switch (errorType) {
            case INVALID_PASSWORD:
                return "Invalid password. Please try again.";
            case ACCOUNT_LOCKED:
                return "Account is temporarily locked due to security reasons.";
            case SESSION_EXPIRED:
                return "Your session has expired. Please log in again.";
            case TOO_MANY_ATTEMPTS:
                return "Too many failed login attempts. Please wait before trying again.";
            case UNAUTHORIZED_ACCESS:
                return "Access denied. You don't have permission to perform this action.";
            case CREDENTIAL_CORRUPTION:
                return "Authentication data is corrupted. Please contact support.";
            default:
                return "Authentication failed. Please try again.";
        }
    }
    
    public AuthErrorType getAuthErrorType() {
        return authErrorType;
    }
    
    public int getAttemptCount() {
        return attemptCount;
    }
    
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getUserMessage() {
        // Never expose technical details in authentication errors for security
        return generateUserMessage(authErrorType);
    }
    
    @Override
    public String getTechnicalDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Auth Error: ").append(authErrorType.name());
        if (attemptCount > 0) {
            details.append("; Attempts: ").append(attemptCount);
        }
        if (username != null) {
            details.append("; User: ").append(username.substring(0, Math.min(3, username.length()))).append("***");
        }
        return details.toString();
    }
}