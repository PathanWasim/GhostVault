package com.ghostvault.ui;

import com.ghostvault.error.ErrorHandlingResult;
import com.ghostvault.exception.GhostVaultException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User-friendly error dialog that doesn't expose technical details
 * Provides clear error messages and recovery suggestions
 */
public class ErrorDialog {
    
    private Stage dialogStage;
    private ErrorHandlingResult errorResult;
    private boolean userWantsRetry = false;
    private boolean userWantsDetails = false;
    
    public ErrorDialog(ErrorHandlingResult errorResult) {
        this.errorResult = errorResult;
    }
    
    /**
     * Show error dialog and return user's choice
     */
    public ErrorDialogResult showDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("GhostVault - Error");
        dialogStage.setResizable(false);
        
        VBox root = createErrorUI();
        Scene scene = new Scene(root, 500, 350);
        dialogStage.setScene(scene);
        
        dialogStage.showAndWait();
        
        return new ErrorDialogResult(userWantsRetry, userWantsDetails);
    }
    
    /**
     * Create error dialog UI
     */
    private VBox createErrorUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        
        // Header with icon and title
        HBox headerBox = createHeaderBox();
        
        // Error message
        VBox messageBox = createMessageBox();
        
        // Recovery suggestion
        VBox recoveryBox = createRecoveryBox();
        
        // Technical details (collapsible)
        VBox detailsBox = createDetailsBox();
        
        // Buttons
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(
            headerBox,
            new Separator(),
            messageBox,
            recoveryBox,
            detailsBox,
            buttonBox
        );
        
        return root;
    }
    
    /**
     * Create header with icon and title
     */
    private HBox createHeaderBox() {
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        // Error icon based on severity
        Label iconLabel = new Label(getErrorIcon());
        iconLabel.setFont(Font.font(32));
        
        // Title
        Label titleLabel = new Label(getErrorTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(getErrorColor());
        
        headerBox.getChildren().addAll(iconLabel, titleLabel);
        
        return headerBox;
    }
    
    /**
     * Create message box
     */
    private VBox createMessageBox() {
        VBox messageBox = new VBox(10);
        
        Label messageLabel = new Label("What happened:");
        messageLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label messageText = new Label(errorResult.getUserMessage());
        messageText.setWrapText(true);
        messageText.setFont(Font.font("System", 12));
        
        messageBox.getChildren().addAll(messageLabel, messageText);
        
        return messageBox;
    }
    
    /**
     * Create recovery suggestion box
     */
    private VBox createRecoveryBox() {
        VBox recoveryBox = new VBox(10);
        
        Label recoveryLabel = new Label("What you can do:");
        recoveryLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label recoveryText = new Label(errorResult.getRecoverySuggestion());
        recoveryText.setWrapText(true);
        recoveryText.setFont(Font.font("System", 12));
        
        // Additional recovery message from error handler
        if (errorResult.getRecoveryMessage() != null && !errorResult.getRecoveryMessage().isEmpty()) {
            Label additionalText = new Label(errorResult.getRecoveryMessage());
            additionalText.setWrapText(true);
            additionalText.setFont(Font.font("System", 11));
            additionalText.setTextFill(Color.GRAY);
            
            recoveryBox.getChildren().addAll(recoveryLabel, recoveryText, additionalText);
        } else {
            recoveryBox.getChildren().addAll(recoveryLabel, recoveryText);
        }
        
        return recoveryBox;
    }
    
    /**
     * Create collapsible technical details box
     */
    private VBox createDetailsBox() {
        VBox detailsBox = new VBox(5);
        
        // Details toggle button
        Button detailsButton = new Button("Show Technical Details");
        detailsButton.setFont(Font.font("System", 10));
        
        // Details area (initially hidden)
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(6);
        detailsArea.setWrapText(true);
        detailsArea.setVisible(false);
        detailsArea.setManaged(false);
        
        // Populate details
        StringBuilder details = new StringBuilder();
        details.append("Error Code: ").append(errorResult.getErrorCode()).append("\n");
        details.append("Severity: ").append(errorResult.getSeverity()).append("\n");
        details.append("Recovery Action: ").append(errorResult.getRecoveryAction()).append("\n");
        details.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        
        if (errorResult.getException() != null && errorResult.getException().getTechnicalDetails() != null) {
            details.append("Technical Details: ").append(errorResult.getException().getTechnicalDetails()).append("\n");
        }
        
        detailsArea.setText(details.toString());
        
        // Toggle functionality
        detailsButton.setOnAction(e -> {
            boolean visible = !detailsArea.isVisible();
            detailsArea.setVisible(visible);
            detailsArea.setManaged(visible);
            detailsButton.setText(visible ? "Hide Technical Details" : "Show Technical Details");
            
            // Resize dialog
            dialogStage.sizeToScene();
            
            userWantsDetails = visible;
        });
        
        detailsBox.getChildren().addAll(detailsButton, detailsArea);
        
        return detailsBox;
    }
    
    /**
     * Create button box
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Retry button (if applicable)
        if (canRetry()) {
            Button retryButton = new Button("Try Again");
            retryButton.setPrefWidth(100);
            retryButton.setOnAction(e -> {
                userWantsRetry = true;
                dialogStage.close();
            });
            buttonBox.getChildren().add(retryButton);
        }
        
        // OK/Close button
        Button okButton = new Button(canRetry() ? "Cancel" : "OK");
        okButton.setPrefWidth(100);
        okButton.setOnAction(e -> {
            userWantsRetry = false;
            dialogStage.close();
        });
        
        buttonBox.getChildren().add(okButton);
        
        return buttonBox;
    }
    
    /**
     * Get error icon based on severity
     */
    private String getErrorIcon() {
        switch (errorResult.getSeverity()) {
            case LOW:
                return "ℹ️";
            case MEDIUM:
                return "⚠️";
            case HIGH:
                return "❌";
            case CRITICAL:
                return "🚨";
            default:
                return "❓";
        }
    }
    
    /**
     * Get error title based on severity
     */
    private String getErrorTitle() {
        switch (errorResult.getSeverity()) {
            case LOW:
                return "Information";
            case MEDIUM:
                return "Warning";
            case HIGH:
                return "Error";
            case CRITICAL:
                return "Critical Error";
            default:
                return "Error";
        }
    }
    
    /**
     * Get error color based on severity
     */
    private Color getErrorColor() {
        switch (errorResult.getSeverity()) {
            case LOW:
                return Color.BLUE;
            case MEDIUM:
                return Color.ORANGE;
            case HIGH:
                return Color.RED;
            case CRITICAL:
                return Color.DARKRED;
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Check if retry is possible
     */
    private boolean canRetry() {
        return errorResult.getException() != null && 
               errorResult.getException().isRecoverable() &&
               errorResult.getRecoveryAction() != com.ghostvault.error.ErrorHandler.RecoveryAction.PANIC_MODE;
    }
    
    /**
     * Show simple error message (static method)
     */
    public static void showError(Stage parentStage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(parentStage);
        alert.setTitle("GhostVault - " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show simple warning message (static method)
     */
    public static void showWarning(Stage parentStage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(parentStage);
        alert.setTitle("GhostVault - " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show simple info message (static method)
     */
    public static void showInfo(Stage parentStage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(parentStage);
        alert.setTitle("GhostVault - " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Result of error dialog interaction
     */
    public static class ErrorDialogResult {
        private final boolean retry;
        private final boolean viewedDetails;
        
        public ErrorDialogResult(boolean retry, boolean viewedDetails) {
            this.retry = retry;
            this.viewedDetails = viewedDetails;
        }
        
        public boolean shouldRetry() { return retry; }
        public boolean viewedDetails() { return viewedDetails; }
    }
}