package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comprehensive error handling system with user-friendly dialogs and logging
 */
public class ErrorHandlingSystem {
    
    private static final Logger logger = Logger.getLogger(ErrorHandlingSystem.class.getName());
    private static ErrorHandlingSystem instance;
    
    // Error tracking
    private List<ErrorRecord> errorHistory = new ArrayList<>();
    private Consumer<ErrorRecord> onErrorLogged;
    
    // Configuration
    private boolean showStackTraces = false;
    private boolean logToConsole = true;
    private ErrorSeverity minimumSeverity = ErrorSeverity.WARNING;
    
    private ErrorHandlingSystem() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static ErrorHandlingSystem getInstance() {
        if (instance == null) {
            instance = new ErrorHandlingSystem();
        }
        return instance;
    }
    
    /**
     * Handle an error with automatic severity detection
     */
    public static void handleError(String message, Throwable throwable) {
        getInstance().logError(message, throwable, ErrorSeverity.ERROR, null);
    }
    
    /**
     * Handle an error with specified severity
     */
    public static void handleError(String message, Throwable throwable, ErrorSeverity severity) {
        getInstance().logError(message, throwable, severity, null);
    }
    
    /**
     * Handle an error with context information
     */
    public static void handleError(String message, Throwable throwable, ErrorSeverity severity, String context) {
        getInstance().logError(message, throwable, severity, context);
    }
    
    /**
     * Show a user-friendly error dialog
     */
    public static void showErrorDialog(String title, String message) {
        getInstance().displayErrorDialog(title, message, null, ErrorSeverity.ERROR, null);
    }
    
    /**
     * Show a user-friendly error dialog with details
     */
    public static void showErrorDialog(String title, String message, Throwable throwable) {
        getInstance().displayErrorDialog(title, message, throwable, ErrorSeverity.ERROR, null);
    }
    
    /**
     * Show a warning dialog
     */
    public static void showWarningDialog(String title, String message) {
        getInstance().displayErrorDialog(title, message, null, ErrorSeverity.WARNING, null);
    }
    
    /**
     * Show an info dialog
     */
    public static void showInfoDialog(String title, String message) {
        getInstance().displayErrorDialog(title, message, null, ErrorSeverity.INFO, null);
    }
    
    /**
     * Log an error with full details
     */
    private void logError(String message, Throwable throwable, ErrorSeverity severity, String context) {
        // Create error record
        ErrorRecord record = new ErrorRecord(message, throwable, severity, context);
        
        // Add to history
        errorHistory.add(record);
        
        // Keep only last 1000 errors
        if (errorHistory.size() > 1000) {
            errorHistory.remove(0);
        }
        
        // Log to console if enabled
        if (logToConsole && severity.ordinal() >= minimumSeverity.ordinal()) {
            logToConsole(record);
        }
        
        // Notify listeners
        if (onErrorLogged != null) {
            onErrorLogged.accept(record);
        }
        
        // Show dialog for severe errors
        if (severity == ErrorSeverity.CRITICAL || severity == ErrorSeverity.ERROR) {
            Platform.runLater(() -> {
                displayErrorDialog("Application Error", message, throwable, severity, context);
            });
        }
    }
    
    /**
     * Log error to console
     */
    private void logToConsole(ErrorRecord record) {
        Level logLevel = mapSeverityToLogLevel(record.getSeverity());
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[").append(record.getSeverity()).append("] ");
        logMessage.append(record.getMessage());
        
        if (record.getContext() != null) {
            logMessage.append(" (Context: ").append(record.getContext()).append(")");
        }
        
        if (record.getThrowable() != null) {
            logger.log(logLevel, logMessage.toString(), record.getThrowable());
        } else {
            logger.log(logLevel, logMessage.toString());
        }
    }
    
    /**
     * Map error severity to Java logging level
     */
    private Level mapSeverityToLogLevel(ErrorSeverity severity) {
        switch (severity) {
            case CRITICAL:
                return Level.SEVERE;
            case ERROR:
                return Level.SEVERE;
            case WARNING:
                return Level.WARNING;
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.FINE;
            default:
                return Level.INFO;
        }
    }
    
    /**
     * Display error dialog to user
     */
    private void displayErrorDialog(String title, String message, Throwable throwable, 
                                  ErrorSeverity severity, String context) {
        
        // Create dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(500);
        dialogStage.setHeight(400);
        
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("error-dialog");
        
        // Header with icon and title
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("error-dialog-header");
        headerBox.setPadding(new Insets(16));
        
        Label iconLabel = new Label(getIconForSeverity(severity));
        iconLabel.getStyleClass().add("error-icon");
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("error-title");
        
        Label severityLabel = new Label(severity.getDisplayName());
        severityLabel.getStyleClass().add("error-severity");
        severityLabel.getStyleClass().add("severity-" + severity.name().toLowerCase());
        
        titleBox.getChildren().addAll(titleLabel, severityLabel);
        
        headerBox.getChildren().addAll(iconLabel, titleBox);
        
        // Message area
        VBox messageArea = new VBox(8);
        messageArea.setPadding(new Insets(0, 16, 16, 16));
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setWrapText(true);
        
        messageArea.getChildren().add(messageLabel);
        
        // Context information
        if (context != null && !context.trim().isEmpty()) {
            Label contextLabel = new Label("Context: " + context);
            contextLabel.getStyleClass().add("error-context");
            contextLabel.setWrapText(true);
            messageArea.getChildren().add(contextLabel);
        }
        
        // Details area (expandable)
        if (throwable != null) {
            VBox detailsArea = new VBox(8);
            detailsArea.setPadding(new Insets(0, 16, 16, 16));
            
            // Details toggle
            CheckBox showDetailsCheckBox = new CheckBox("Show technical details");
            showDetailsCheckBox.getStyleClass().add("show-details-checkbox");
            
            // Stack trace area
            TextArea stackTraceArea = new TextArea();
            stackTraceArea.getStyleClass().add("stack-trace-area");
            stackTraceArea.setEditable(false);
            stackTraceArea.setWrapText(true);
            stackTraceArea.setPrefRowCount(10);
            stackTraceArea.setVisible(false);
            stackTraceArea.setManaged(false);
            
            // Populate stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            stackTraceArea.setText(sw.toString());
            
            // Toggle details visibility
            showDetailsCheckBox.setOnAction(e -> {
                boolean show = showDetailsCheckBox.isSelected();
                stackTraceArea.setVisible(show);
                stackTraceArea.setManaged(show);
                
                if (show) {
                    dialogStage.setHeight(600);
                } else {
                    dialogStage.setHeight(400);
                }
            });
            
            detailsArea.getChildren().addAll(showDetailsCheckBox, stackTraceArea);
            messageArea.getChildren().add(detailsArea);
        }
        
        // Button area
        HBox buttonArea = new HBox(8);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);
        buttonArea.setPadding(new Insets(16));
        buttonArea.getStyleClass().add("error-dialog-buttons");
        
        // Copy to clipboard button
        Button copyButton = new Button("Copy Details");
        copyButton.getStyleClass().addAll("button", "ghost");
        copyButton.setOnAction(e -> copyErrorToClipboard(title, message, throwable, context));
        
        // Report error button (for critical errors)
        Button reportButton = null;
        if (severity == ErrorSeverity.CRITICAL) {
            reportButton = new Button("Report Error");
            reportButton.getStyleClass().addAll("button", "ghost");
            reportButton.setOnAction(e -> showErrorReportDialog(title, message, throwable, context));
        }
        
        // OK button
        Button okButton = new Button("OK");
        okButton.getStyleClass().addAll("button", "primary");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> dialogStage.close());
        
        if (reportButton != null) {
            buttonArea.getChildren().addAll(copyButton, reportButton, okButton);
        } else {
            buttonArea.getChildren().addAll(copyButton, okButton);
        }
        
        mainContainer.getChildren().addAll(headerBox, messageArea, buttonArea);
        
        // Create scene and show
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
        dialogStage.setScene(scene);
        
        // Apply severity-specific styling
        mainContainer.getStyleClass().add("severity-" + severity.name().toLowerCase());
        
        dialogStage.showAndWait();
    }
    
    /**
     * Get icon for error severity
     */
    private String getIconForSeverity(ErrorSeverity severity) {
        switch (severity) {
            case CRITICAL:
                return "🚨";
            case ERROR:
                return "❌";
            case WARNING:
                return "⚠️";
            case INFO:
                return "ℹ️";
            case DEBUG:
                return "🐛";
            default:
                return "❓";
        }
    }
    
    /**
     * Copy error details to clipboard
     */
    private void copyErrorToClipboard(String title, String message, Throwable throwable, String context) {
        StringBuilder details = new StringBuilder();
        details.append("Error Report\\n");
        details.append("=============\\n\\n");
        details.append("Title: ").append(title).append("\\n");
        details.append("Message: ").append(message).append("\\n");
        details.append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\\n");
        
        if (context != null) {
            details.append("Context: ").append(context).append("\\n");
        }
        
        if (throwable != null) {
            details.append("\\nStack Trace:\\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            details.append(sw.toString());
        }
        
        // Copy to system clipboard
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(details.toString());
        clipboard.setContent(content);
        
        // Show confirmation
        showInfoDialog("Copied", "Error details copied to clipboard.");
    }
    
    /**
     * Show error report dialog
     */
    private void showErrorReportDialog(String title, String message, Throwable throwable, String context) {
        Alert reportDialog = new Alert(Alert.AlertType.INFORMATION);
        reportDialog.setTitle("Report Error");
        reportDialog.setHeaderText("Error Report");
        reportDialog.setContentText(
            "This error has been logged for analysis. If the problem persists, " +
            "please contact support with the error details (use 'Copy Details' button)."
        );
        
        reportDialog.showAndWait();
    }
    
    /**
     * Show error history dialog
     */
    public void showErrorHistoryDialog(Window owner) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Error History");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        dialogStage.setWidth(700);
        dialogStage.setHeight(500);
        
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("error-history-dialog");
        
        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(16));
        headerBox.getStyleClass().add("error-history-header");
        
        Label titleLabel = new Label("Error History");
        titleLabel.getStyleClass().add("error-history-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearButton = new Button("Clear History");
        clearButton.getStyleClass().addAll("button", "ghost");
        clearButton.setOnAction(e -> {
            errorHistory.clear();
            dialogStage.close();
        });
        
        headerBox.getChildren().addAll(titleLabel, spacer, clearButton);
        
        // Error list
        ListView<ErrorRecord> errorListView = new ListView<>();
        errorListView.getStyleClass().add("error-history-list");
        errorListView.setCellFactory(listView -> new ErrorHistoryListCell());
        errorListView.getItems().setAll(errorHistory);
        VBox.setVgrow(errorListView, Priority.ALWAYS);
        
        // Button area
        HBox buttonArea = new HBox(8);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);
        buttonArea.setPadding(new Insets(16));
        
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("button", "primary");
        closeButton.setOnAction(e -> dialogStage.close());
        
        buttonArea.getChildren().add(closeButton);
        
        mainContainer.getChildren().addAll(headerBox, errorListView, buttonArea);
        
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    /**
     * Custom list cell for error history
     */
    private class ErrorHistoryListCell extends ListCell<ErrorRecord> {
        private VBox content;
        private Label timeLabel;
        private Label severityLabel;
        private Label messageLabel;
        private Label contextLabel;
        
        public ErrorHistoryListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new VBox(4);
            content.setPadding(new Insets(8));
            content.getStyleClass().add("error-history-cell");
            
            HBox headerRow = new HBox(8);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            
            timeLabel = new Label();
            timeLabel.getStyleClass().add("error-time");
            
            severityLabel = new Label();
            severityLabel.getStyleClass().add("error-severity-badge");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            headerRow.getChildren().addAll(timeLabel, severityLabel, spacer);
            
            messageLabel = new Label();
            messageLabel.getStyleClass().add("error-message-summary");
            messageLabel.setWrapText(true);
            
            contextLabel = new Label();
            contextLabel.getStyleClass().add("error-context-summary");
            contextLabel.setWrapText(true);
            
            content.getChildren().addAll(headerRow, messageLabel, contextLabel);
        }
        
        @Override
        protected void updateItem(ErrorRecord record, boolean empty) {
            super.updateItem(record, empty);
            
            if (empty || record == null) {
                setGraphic(null);
            } else {
                timeLabel.setText(record.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss")));
                severityLabel.setText(record.getSeverity().getDisplayName());
                severityLabel.getStyleClass().removeIf(cls -> cls.startsWith("severity-"));
                severityLabel.getStyleClass().add("severity-" + record.getSeverity().name().toLowerCase());
                
                messageLabel.setText(record.getMessage());
                
                if (record.getContext() != null) {
                    contextLabel.setText("Context: " + record.getContext());
                    contextLabel.setVisible(true);
                    contextLabel.setManaged(true);
                } else {
                    contextLabel.setVisible(false);
                    contextLabel.setManaged(false);
                }
                
                setGraphic(content);
            }
        }
    }
    
    // Configuration methods
    
    public void setShowStackTraces(boolean show) {
        this.showStackTraces = show;
    }
    
    public void setLogToConsole(boolean log) {
        this.logToConsole = log;
    }
    
    public void setMinimumSeverity(ErrorSeverity severity) {
        this.minimumSeverity = severity;
    }
    
    public void setOnErrorLogged(Consumer<ErrorRecord> callback) {
        this.onErrorLogged = callback;
    }
    
    public List<ErrorRecord> getErrorHistory() {
        return new ArrayList<>(errorHistory);
    }
    
    public void clearErrorHistory() {
        errorHistory.clear();
    }
    
    // Helper classes
    
    /**
     * Error severity levels
     */
    public enum ErrorSeverity {
        DEBUG("Debug", "Debugging information"),
        INFO("Information", "General information"),
        WARNING("Warning", "Potential issue that doesn't prevent operation"),
        ERROR("Error", "Error that affects functionality"),
        CRITICAL("Critical", "Severe error that may cause application instability");
        
        private final String displayName;
        private final String description;
        
        ErrorSeverity(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Error record for tracking
     */
    public static class ErrorRecord {
        private final LocalDateTime timestamp;
        private final String message;
        private final Throwable throwable;
        private final ErrorSeverity severity;
        private final String context;
        
        public ErrorRecord(String message, Throwable throwable, ErrorSeverity severity, String context) {
            this.timestamp = LocalDateTime.now();
            this.message = message;
            this.throwable = throwable;
            this.severity = severity;
            this.context = context;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public Throwable getThrowable() { return throwable; }
        public ErrorSeverity getSeverity() { return severity; }
        public String getContext() { return context; }
    }
}