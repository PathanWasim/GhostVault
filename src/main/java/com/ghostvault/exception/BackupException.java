package com.ghostvault.exception;

/**
 * Exception thrown during backup and restore operations
 */
public class BackupException extends Exception {
    
    public BackupException(String message) {
        super(message);
    }
    
    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BackupException(Throwable cause) {
        super(cause);
    }
}