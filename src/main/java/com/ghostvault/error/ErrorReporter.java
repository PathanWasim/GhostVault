package com.ghostvault.error;

import com.ghostvault.exception.GhostVaultException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Error reporting and debugging utility
 * Provides detailed error information for debugging while maintaining security
 */
public class ErrorReporter {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final Map<String, ErrorReport> errorReports;
    private final boolean debugMode;
    private final PrintWriter debugWriter;
    
    public ErrorReporter(boolean debugMode) {
        this.debugMode = debugMode;
        this.errorReports = new ConcurrentHashMap<>();
        this.debugWriter = debugMode ? new PrintWriter(System.err, true) : null;
    }
    
    public ErrorReporter(boolean debugMode, PrintWriter debugWriter) {
        this.debugMode = debugMode;
        this.errorReports = new ConcurrentHashMap<>();
        this.debugWriter = debugWriter;
    }
    
    /**
     * Report an error with full context
     */
    public String reportError(String operation, Exception exception, Map<String, Object> context) {
        String errorId = generateErrorId();
        
        ErrorReport report = new ErrorReport(
            errorId,
            operation,
            exception,
            context,
            LocalDateTime.now(),
            Thread.currentThread().getName(),
            getStackTraceContext()
        );
        
        errorReports.put(errorId, report);
        
        if (debugMode && debugWriter != null) {
            writeDebugReport(report);
        }
        
        return errorId;
    }
    
    /**
     * Report an error with minimal context
     */
    public String reportError(String operation, Exception exception) {
        return reportError(operation, exception, Collections.emptyMap());
    }
    
    /**
     * Get error report by ID
     */
    public ErrorReport getErrorReport(String errorId) {
        return errorReports.get(errorId);
    }
    
    /**
     * Get all error reports
     */
    public Collection<ErrorReport> getAllErrorReports() {
        return new ArrayList<>(errorReports.values());
    }
    
    /**
     * Get error reports for specific operation
     */
    public List<ErrorReport> getErrorReportsForOperation(String operation) {
        return errorReports.values().stream()
            .filter(report -> operation.equals(report.getOperation()))
            .sorted(Comparator.comparing(ErrorReport::getTimestamp).reversed())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Generate error summary report
     */
    public String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Error Summary Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n");
        report.append("Total Errors: ").append(errorReports.size()).append("\n\n");
        
        // Group errors by operation
        Map<String, List<ErrorReport>> errorsByOperation = new HashMap<>();
        for (ErrorReport errorReport : errorReports.values()) {
            errorsByOperation.computeIfAbsent(errorReport.getOperation(), k -> new ArrayList<>())
                .add(errorReport);
        }
        
        // Report by operation
        for (Map.Entry<String, List<ErrorReport>> entry : errorsByOperation.entrySet()) {
            String operation = entry.getKey();
            List<ErrorReport> reports = entry.getValue();
            
            report.append("Operation: ").append(operation).append("\n");
            report.append("  Error Count: ").append(reports.size()).append("\n");
            
            // Group by exception type
            Map<String, Integer> exceptionCounts = new HashMap<>();
            for (ErrorReport errorReport : reports) {
                String exceptionType = errorReport.getException().getClass().getSimpleName();
                exceptionCounts.merge(exceptionType, 1, Integer::sum);
            }
            
            for (Map.Entry<String, Integer> exEntry : exceptionCounts.entrySet()) {
                report.append("    ").append(exEntry.getKey()).append(": ").append(exEntry.getValue()).append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate detailed error report
     */
    public String generateDetailedReport(String errorId) {
        ErrorReport report = errorReports.get(errorId);
        if (report == null) {
            return "Error report not found: " + errorId;
        }
        
        StringBuilder detailed = new StringBuilder();
        detailed.append("=== Detailed Error Report ===\n");
        detailed.append("Error ID: ").append(report.getErrorId()).append("\n");
        detailed.append("Timestamp: ").append(report.getTimestamp().format(TIMESTAMP_FORMAT)).append("\n");
        detailed.append("Operation: ").append(report.getOperation()).append("\n");
        detailed.append("Thread: ").append(report.getThreadName()).append("\n");
        detailed.append("\n");
        
        // Exception details
        Exception exception = report.getException();
        detailed.append("Exception Type: ").append(exception.getClass().getName()).append("\n");
        detailed.append("Exception Message: ").append(exception.getMessage()).append("\n");
        
        if (exception instanceof GhostVaultException) {
            GhostVaultException gve = (GhostVaultException) exception;
            detailed.append("Category: ").append(gve.getCategory()).append("\n");
            detailed.append("Severity: ").append(gve.getSeverity()).append("\n");
            detailed.append("Recoverable: ").append(gve.isRecoverable()).append("\n");
            detailed.append("User Message: ").append(gve.getUserMessage()).append("\n");
            if (gve.getTechnicalDetails() != null) {
                detailed.append("Technical Details: ").append(gve.getTechnicalDetails()).append("\n");
            }
        }
        
        detailed.append("\n");
        
        // Context information
        if (!report.getContext().isEmpty()) {
            detailed.append("Context:\n");
            for (Map.Entry<String, Object> entry : report.getContext().entrySet()) {
                detailed.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            detailed.append("\n");
        }
        
        // Stack trace (if debug mode)
        if (debugMode) {
            detailed.append("Stack Trace:\n");
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            detailed.append(sw.toString()).append("\n");
            
            if (!report.getStackContext().isEmpty()) {
                detailed.append("Stack Context:\n");
                for (String context : report.getStackContext()) {
                    detailed.append("  ").append(context).append("\n");
                }
            }
        }
        
        return detailed.toString();
    }
    
    /**
     * Clear old error reports
     */
    public void clearOldReports(int maxAge) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(maxAge);
        
        errorReports.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(cutoff));
    }
    
    /**
     * Export error reports to file
     */
    public void exportReports(File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println(generateSummaryReport());
            writer.println("\n" + "=".repeat(50) + "\n");
            
            for (ErrorReport report : errorReports.values()) {
                writer.println(generateDetailedReport(report.getErrorId()));
                writer.println("\n" + "-".repeat(30) + "\n");
            }
        }
    }
    
    /**
     * Get error statistics
     */
    public ErrorStatistics getStatistics() {
        Map<String, Integer> errorsByType = new HashMap<>();
        Map<String, Integer> errorsByOperation = new HashMap<>();
        LocalDateTime oldestError = null;
        LocalDateTime newestError = null;
        
        for (ErrorReport report : errorReports.values()) {
            // Count by exception type
            String exceptionType = report.getException().getClass().getSimpleName();
            errorsByType.merge(exceptionType, 1, Integer::sum);
            
            // Count by operation
            errorsByOperation.merge(report.getOperation(), 1, Integer::sum);
            
            // Track time range
            LocalDateTime timestamp = report.getTimestamp();
            if (oldestError == null || timestamp.isBefore(oldestError)) {
                oldestError = timestamp;
            }
            if (newestError == null || timestamp.isAfter(newestError)) {
                newestError = timestamp;
            }
        }
        
        return new ErrorStatistics(
            errorReports.size(),
            errorsByType,
            errorsByOperation,
            oldestError,
            newestError
        );
    }
    
    /**
     * Write debug report to debug writer
     */
    private void writeDebugReport(ErrorReport report) {
        if (debugWriter == null) return;
        
        debugWriter.println("=== DEBUG ERROR REPORT ===");
        debugWriter.println("ID: " + report.getErrorId());
        debugWriter.println("Time: " + report.getTimestamp().format(TIMESTAMP_FORMAT));
        debugWriter.println("Operation: " + report.getOperation());
        debugWriter.println("Thread: " + report.getThreadName());
        debugWriter.println("Exception: " + report.getException().getClass().getSimpleName());
        debugWriter.println("Message: " + report.getException().getMessage());
        
        if (!report.getContext().isEmpty()) {
            debugWriter.println("Context: " + report.getContext());
        }
        
        debugWriter.println("Stack trace:");
        report.getException().printStackTrace(debugWriter);
        debugWriter.println("=== END DEBUG REPORT ===\n");
        debugWriter.flush();
    }
    
    /**
     * Generate unique error ID
     */
    private String generateErrorId() {
        return "ERR_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString(Thread.currentThread().hashCode());
    }
    
    /**
     * Get stack trace context for debugging
     */
    private List<String> getStackTraceContext() {
        if (!debugMode) {
            return Collections.emptyList();
        }
        
        List<String> context = new ArrayList<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // Skip the first few frames (getStackTrace, this method, etc.)
        for (int i = 3; i < Math.min(stackTrace.length, 10); i++) {
            StackTraceElement element = stackTrace[i];
            if (element.getClassName().startsWith("com.ghostvault")) {
                context.add(element.getClassName() + "." + element.getMethodName() + 
                           "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
            }
        }
        
        return context;
    }
    
    /**
     * Error report data class
     */
    public static class ErrorReport {
        private final String errorId;
        private final String operation;
        private final Exception exception;
        private final Map<String, Object> context;
        private final LocalDateTime timestamp;
        private final String threadName;
        private final List<String> stackContext;
        
        public ErrorReport(String errorId, String operation, Exception exception, 
                          Map<String, Object> context, LocalDateTime timestamp, 
                          String threadName, List<String> stackContext) {
            this.errorId = errorId;
            this.operation = operation;
            this.exception = exception;
            this.context = new HashMap<>(context);
            this.timestamp = timestamp;
            this.threadName = threadName;
            this.stackContext = new ArrayList<>(stackContext);
        }
        
        // Getters
        public String getErrorId() { return errorId; }
        public String getOperation() { return operation; }
        public Exception getException() { return exception; }
        public Map<String, Object> getContext() { return new HashMap<>(context); }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getThreadName() { return threadName; }
        public List<String> getStackContext() { return new ArrayList<>(stackContext); }
    }
    
    /**
     * Error statistics data class
     */
    public static class ErrorStatistics {
        private final int totalErrors;
        private final Map<String, Integer> errorsByType;
        private final Map<String, Integer> errorsByOperation;
        private final LocalDateTime oldestError;
        private final LocalDateTime newestError;
        
        public ErrorStatistics(int totalErrors, Map<String, Integer> errorsByType, 
                             Map<String, Integer> errorsByOperation, 
                             LocalDateTime oldestError, LocalDateTime newestError) {
            this.totalErrors = totalErrors;
            this.errorsByType = new HashMap<>(errorsByType);
            this.errorsByOperation = new HashMap<>(errorsByOperation);
            this.oldestError = oldestError;
            this.newestError = newestError;
        }
        
        // Getters
        public int getTotalErrors() { return totalErrors; }
        public Map<String, Integer> getErrorsByType() { return new HashMap<>(errorsByType); }
        public Map<String, Integer> getErrorsByOperation() { return new HashMap<>(errorsByOperation); }
        public LocalDateTime getOldestError() { return oldestError; }
        public LocalDateTime getNewestError() { return newestError; }
        
        public String getMostCommonErrorType() {
            return errorsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        }
        
        public String getMostProblematicOperation() {
            return errorsByOperation.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        }
    }
}