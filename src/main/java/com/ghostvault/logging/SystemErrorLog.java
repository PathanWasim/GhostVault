package com.ghostvault.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced error logging model for detailed error tracking
 */
public class SystemErrorLog {
    private String component;
    private String operation;
    private String errorDetails;
    private LocalDateTime timestamp;
    private String userAction;
    private String severity;
    private String stackTrace;
    
    public SystemErrorLog(String component, String operation, String errorDetails, String userAction) {
        this.component = component;
        this.operation = operation;
        this.errorDetails = errorDetails;
        this.userAction = userAction;
        this.timestamp = LocalDateTime.now();
        this.severity = "ERROR";
    }
    
    public SystemErrorLog(String component, String operation, String errorDetails, String userAction, String severity) {
        this(component, operation, errorDetails, userAction);
        this.severity = severity;
    }
    
    public SystemErrorLog(String component, String operation, Exception exception, String userAction) {
        this(component, operation, exception.getMessage(), userAction);
        this.stackTrace = getStackTraceString(exception);
    }
    
    private String getStackTraceString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    public void logToConsole() {
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("=== SYSTEM ERROR LOG ===");
        System.out.println("Time: " + formattedTime);
        System.out.println("Component: " + component);
        System.out.println("Operation: " + operation);
        System.out.println("Severity: " + severity);
        System.out.println("User Action: " + userAction);
        System.out.println("Error: " + errorDetails);
        if (stackTrace != null) {
            System.out.println("Stack Trace:");
            System.out.println(stackTrace);
        }
        System.out.println("========================");
    }
    
    public void logToFile(String logFilePath) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(logFilePath);
            java.nio.file.Files.createDirectories(path.getParent());
            
            String logEntry = String.format("[%s] %s - %s - %s: %s (User: %s)%n",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                severity,
                component,
                operation,
                errorDetails,
                userAction
            );
            
            java.nio.file.Files.write(path, logEntry.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
                
        } catch (Exception e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    // Getters and setters
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUserAction() { return userAction; }
    public void setUserAction(String userAction) { this.userAction = userAction; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
}