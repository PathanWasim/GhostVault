package com.ghostvault.error;

/**
 * Recovery actions for error handling
 * Defines possible recovery strategies for different error types
 */
public enum RecoveryAction {
    
    RETRY("Retry the operation"),
    SKIP("Skip the operation and continue"),
    ABORT("Abort the current operation"),
    RESTART("Restart the application"),
    RESET("Reset to default state"),
    BACKUP("Create backup before proceeding"),
    ROLLBACK("Rollback to previous state"),
    IGNORE("Ignore the error and continue"),
    PROMPT_USER("Prompt user for action"),
    LOG_AND_CONTINUE("Log error and continue");
    
    private final String description;
    
    RecoveryAction(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}