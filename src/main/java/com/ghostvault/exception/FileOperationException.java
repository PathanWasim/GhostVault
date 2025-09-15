package com.ghostvault.exception;

import java.io.File;

/**
 * Exception for file system operations errors
 */
public class FileOperationException extends GhostVaultException {
    
    public enum FileErrorType {
        FILE_NOT_FOUND("File not found"),
        ACCESS_DENIED("Access denied"),
        DISK_FULL("Disk space insufficient"),
        FILE_LOCKED("File is locked by another process"),
        INVALID_PATH("Invalid file path"),
        CORRUPTION_DETECTED("File corruption detected"),
        READ_ERROR("Error reading file"),
        WRITE_ERROR("Error writing file"),
        DELETE_ERROR("Error deleting file"),
        PERMISSION_ERROR("Insufficient permissions"),
        NETWORK_ERROR("Network file access error"),
        TIMEOUT("File operation timeout");
        
        private final String description;
        
        FileErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final FileErrorType fileErrorType;
    private final String filePath;
    private final String operation;
    
    public FileOperationException(String message, FileErrorType errorType, String filePath, String operation) {
        super(message, ErrorCategory.FILE_SYSTEM, determineSeverity(errorType), isRecoverable(errorType));
        this.fileErrorType = errorType;
        this.filePath = filePath;
        this.operation = operation;
    }
    
    public FileOperationException(String message, Throwable cause, FileErrorType errorType, String filePath, String operation) {
        super(message, cause, ErrorCategory.FILE_SYSTEM, determineSeverity(errorType), 
              generateUserMessage(errorType, operation), null, isRecoverable(errorType));
        this.fileErrorType = errorType;
        this.filePath = filePath;
        this.operation = operation;
    }
    
    public FileOperationException(String message, FileErrorType errorType) {
        this(message, errorType, null, null);
    }
    
    private static ErrorSeverity determineSeverity(FileErrorType errorType) {
        switch (errorType) {
            case FILE_NOT_FOUND:
            case INVALID_PATH:
                return ErrorSeverity.MEDIUM;
            case ACCESS_DENIED:
            case PERMISSION_ERROR:
            case FILE_LOCKED:
                return ErrorSeverity.HIGH;
            case DISK_FULL:
            case CORRUPTION_DETECTED:
                return ErrorSeverity.CRITICAL;
            default:
                return ErrorSeverity.MEDIUM;
        }
    }
    
    private static boolean isRecoverable(FileErrorType errorType) {
        switch (errorType) {
            case FILE_LOCKED:
            case TIMEOUT:
            case NETWORK_ERROR:
                return true;
            case CORRUPTION_DETECTED:
            case DISK_FULL:
                return false;
            default:
                return true;
        }
    }
    
    private static String generateUserMessage(FileErrorType errorType, String operation) {
        String baseMessage = "File operation failed";
        if (operation != null) {
            baseMessage = operation + " operation failed";
        }
        
        switch (errorType) {
            case FILE_NOT_FOUND:
                return "The requested file could not be found.";
            case ACCESS_DENIED:
            case PERMISSION_ERROR:
                return "Access to the file was denied. Please check permissions.";
            case DISK_FULL:
                return "Insufficient disk space to complete the operation.";
            case FILE_LOCKED:
                return "The file is currently in use by another application.";
            case CORRUPTION_DETECTED:
                return "File corruption was detected. The file may be damaged.";
            case NETWORK_ERROR:
                return "Network error occurred while accessing the file.";
            case TIMEOUT:
                return "The file operation timed out. Please try again.";
            default:
                return baseMessage + ". Please try again.";
        }
    }
    
    public FileErrorType getFileErrorType() {
        return fileErrorType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getOperation() {
        return operation;
    }
    
    @Override
    public String getTechnicalDetails() {
        StringBuilder details = new StringBuilder();
        if (operation != null) {
            details.append("Operation: ").append(operation).append("; ");
        }
        if (filePath != null) {
            details.append("File: ").append(new File(filePath).getName()).append("; ");
        }
        details.append("Error Type: ").append(fileErrorType.name());
        return details.toString();
    }
}