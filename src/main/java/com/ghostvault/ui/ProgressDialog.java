package com.ghostvault.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Progress dialog for long-running operations with responsive feedback
 */
public class ProgressDialog {
    
    private Stage dialogStage;
    private ProgressBar progressBar;
    private ProgressIndicator progressIndicator;
    private Label messageLabel;
    private Label detailLabel;
    private Button cancelButton;
    private Task<?> currentTask;
    private boolean cancellable;
    
    public ProgressDialog(Stage owner, String title, String message) {
        this.cancellable = false;
        createDialog(owner, title, message);
    }
    
    public ProgressDialog(Stage owner, String title, String message, boolean cancellable) {
        this.cancellable = cancellable;
        createDialog(owner, title, message);
    }
    
    /**
     * Create the progress dialog
     */
    private void createDialog(Stage owner, String title, String message) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle(title);
        dialogStage.setResizable(false);
        
        VBox root = createContent(message);
        Scene scene = new Scene(root, 400, 150);
        
        // Theme application is handled by UIManager on scenes globally
        
        dialogStage.setScene(scene);
        
        // Prevent closing with X button unless cancellable
        dialogStage.setOnCloseRequest(e -> {
            if (!cancellable) {
                e.consume();
            } else {
                cancel();
            }
        });
    }
    
    /**
     * Create dialog content
     */
    private VBox createContent(String message) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        // Message label
        messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setWrapText(true);
        
        // Progress indicators container
        VBox progressContainer = new VBox(10);
        progressContainer.setAlignment(Pos.CENTER);
        
        // Progress bar (for determinate progress)
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        
        // Progress indicator (for indeterminate progress)
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        progressIndicator.setProgress(-1); // Indeterminate
        
        progressContainer.getChildren().addAll(progressBar, progressIndicator);
        
        // Detail label (for additional information)
        detailLabel = new Label("");
        detailLabel.setFont(Font.font("System", 11));
        detailLabel.setWrapText(true);
        detailLabel.setVisible(false);
        
        // Button container
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        
        if (cancellable) {
            cancelButton = new Button("Cancel");
            cancelButton.setPrefWidth(80);
            cancelButton.setOnAction(e -> cancel());
            buttonContainer.getChildren().add(cancelButton);
        }
        
        root.getChildren().addAll(messageLabel, progressContainer, detailLabel, buttonContainer);
        
        return root;
    }
    
    /**
     * Show the dialog
     */
    public void show() {
        Platform.runLater(() -> {
            dialogStage.show();
            dialogStage.centerOnScreen();
        });
    }
    
    /**
     * Hide the dialog
     */
    public void hide() {
        Platform.runLater(() -> {
            if (dialogStage.isShowing()) {
                dialogStage.hide();
            }
        });
    }
    
    /**
     * Update progress (0.0 to 1.0)
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            if (progress >= 0 && progress <= 1.0) {
                // Switch to determinate progress
                progressIndicator.setVisible(false);
                progressBar.setVisible(true);
                progressBar.setProgress(progress);
            } else {
                // Switch to indeterminate progress
                progressBar.setVisible(false);
                progressIndicator.setVisible(true);
                progressIndicator.setProgress(-1);
            }
        });
    }
    
    /**
     * Update message
     */
    public void updateMessage(String message) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
        });
    }
    
    /**
     * Update detail text
     */
    public void updateDetail(String detail) {
        Platform.runLater(() -> {
            if (detail != null && !detail.trim().isEmpty()) {
                detailLabel.setText(detail);
                detailLabel.setVisible(true);
            } else {
                detailLabel.setVisible(false);
            }
        });
    }
    
    /**
     * Set the current task (for cancellation)
     */
    public void setTask(Task<?> task) {
        this.currentTask = task;
        
        if (task != null) {
            // Bind progress and message to task
            task.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                updateProgress(newProgress.doubleValue());
            });
            
            task.messageProperty().addListener((obs, oldMessage, newMessage) -> {
                if (newMessage != null && !newMessage.trim().isEmpty()) {
                    updateDetail(newMessage);
                }
            });
            
            // Auto-hide when task completes
            task.setOnSucceeded(e -> hide());
            task.setOnFailed(e -> hide());
            task.setOnCancelled(e -> hide());
        }
    }
    
    /**
     * Cancel the current operation
     */
    public void cancel() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
        }
        hide();
    }
    
    /**
     * Check if dialog is showing
     */
    public boolean isShowing() {
        return dialogStage.isShowing();
    }
    
    /**
     * Set cancellable state
     */
    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
        Platform.runLater(() -> {
            if (cancelButton != null) {
                cancelButton.setVisible(cancellable);
            }
        });
    }
    
    /**
     * Show progress dialog for a task
     */
    public static ProgressDialog showForTask(Stage owner, String title, String message, Task<?> task) {
        ProgressDialog dialog = new ProgressDialog(owner, title, message, true);
        dialog.setTask(task);
        dialog.show();
        return dialog;
    }
    
    /**
     * Show simple progress dialog
     */
    public static ProgressDialog showIndeterminate(Stage owner, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(owner, title, message, false);
        dialog.show();
        return dialog;
    }
    
    /**
     * Show progress dialog with cancel option
     */
    public static ProgressDialog showCancellable(Stage owner, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(owner, title, message, true);
        dialog.show();
        return dialog;
    }
}