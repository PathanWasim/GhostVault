package com.ghostvault.exception;

/**
 * Base exception class for all GhostVault-specific exceptions
 * Provides common functionality and error categorization
 */
public class GhostVaultException extends Exception {
    
    private final ErrorCategory category;
    private final ErrorSeverity severity;
    private final String userMessage;
    private final String technicalDetails;
    private final boolean recoverable;
    
    public enum ErrorCategory {
        CRYPTOGRAPHIC("Cryptographic Error"),
        FILE_SYSTEM("File System Error"),
        SECURITY("Security Error"),
        AUTHENTICATION("Authentication Error"),
        CONFIGURATION("Configuration Error"),
        NETWORK("Network Error"),
        VALIDATION("Validation Error"),
        SYSTEM("System Error"),
        USER_INPUT("User Input Error"),
        BACKUP("Backup Error");
        
        private final String displayName;
        
        ErrorCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ErrorSeverity {
        LOW(1, "Low", "Minor issue that doesn't affect core functionality"),
        MEDIUM(2, "Medium", "Issue that may affect some functionality"),
        HIGH(3, "High", "Serious issue that affects core functionality"),
        CRITICAL(4, "Critical", "Critical issue that prevents normal operation");
        
        private final int level;
        private final String name;
        private final String description;
        
        ErrorSeverity(int level, String name, String description) {
            this.level = level;
            this.name = name;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * Create exception with full details
     */
    public GhostVaultException(String message, Throwable cause, ErrorCategory category, 
                              ErrorSeverity severity, String userMessage, 
                              String technicalDetails, boolean recoverable) {
        super(message, cause);
        this.category = category;
        this.severity = severity;
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
        this.recoverable = recoverable;
    }
    
    /**
     * Create exception with basic details
     */
    public GhostVaultException(String message, ErrorCategory category, ErrorSeverity severity) {
        this(message, null, category, severity, message, null, false);
    }
    
    /**
     * Create exception with cause
     */
    public GhostVaultException(String message, Throwable cause, ErrorCategory category, ErrorSeverity severity) {
        this(message, cause, category, severity, message, null, false);
    }
    
    /**
     * Create recoverable exception
     */
    public GhostVaultException(String message, ErrorCategory category, ErrorSeverity severity, boolean recoverable) {
        this(message, null, category, severity, message, null, recoverable);
    }
    
    // Getters
    public ErrorCategory getCategory() { return category; }
    public ErrorSeverity getSeverity() { return severity; }
    public String getUserMessage() { return userMessage != null ? userMessage : getMessage(); }
    public String getTechnicalDetails() { return technicalDetails; }
    public boolean isRecoverable() { return recoverable; }
    
    /**
     * Get formatted error information
     */
    public String getFormattedError() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(category.getDisplayName()).append("] ");
        sb.append(severity.getName()).append(": ");
        sb.append(getUserMessage());
        
        if (technicalDetails != null && !technicalDetails.trim().isEmpty()) {
            sb.append(" (").append(technicalDetails).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Get error code for logging and tracking
     */
    public String getErrorCode() {
        return String.format("%s_%s_%d", 
            category.name(), 
            severity.name(), 
            Math.abs(getMessage().hashCode()) % 10000);
    }
    
    @Override
    public String toString() {
        return String.format("GhostVaultException{category=%s, severity=%s, recoverable=%s, message='%s'}", 
            category, severity, recoverable, getMessage());
    }
}