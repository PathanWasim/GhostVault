package com.ghostvault.ui.components;

import com.ghostvault.ui.animations.AnimationManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drag and drop file upload manager with visual feedback and progress tracking
 */
public class DragDropUploadManager {
    
    private Node targetNode;
    private VBox dropOverlay;
    private Label dropLabel;
    private ProgressBar uploadProgress;
    private Rectangle dropZone;
    
    // Callbacks
    private Consumer<List<File>> onFilesDropped;
    private Consumer<Double> onUploadProgress;
    private Runnable onUploadComplete;
    private Consumer<String> onUploadError;
    
    // Configuration
    private boolean allowMultipleFiles = true;
    private boolean allowDirectories = true;
    private List<String> allowedExtensions = new ArrayList<>();
    private long maxFileSize = Long.MAX_VALUE; // bytes
    private boolean showProgressOverlay = true;
    
    // State
    private boolean dragActive = false;
    private boolean uploadInProgress = false;
    
    public DragDropUploadManager(Node targetNode) {
        this.targetNode = targetNode;
        initializeComponents();
        setupDragAndDrop();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Drop overlay
        dropOverlay = new VBox(16);
        dropOverlay.setAlignment(Pos.CENTER);
        dropOverlay.getStyleClass().add("drag-drop-overlay");
        dropOverlay.setVisible(false);
        dropOverlay.setMouseTransparent(true);
        
        // Drop zone visual
        dropZone = new Rectangle(200, 120);
        dropZone.getStyleClass().add("drop-zone");
        dropZone.setFill(Color.TRANSPARENT);
        dropZone.setStroke(Color.web("#4A90E2"));
        dropZone.setStrokeWidth(3);
        dropZone.setStrokeDashArray(10, 5);
        dropZone.setArcWidth(12);
        dropZone.setArcHeight(12);
        
        // Drop label
        dropLabel = new Label("📁 Drop files here to upload");
        dropLabel.getStyleClass().add("drop-label");
        
        // Upload progress
        uploadProgress = new ProgressBar();
        uploadProgress.getStyleClass().add("upload-progress");
        uploadProgress.setPrefWidth(200);
        uploadProgress.setVisible(false);
        
        dropOverlay.getChildren().addAll(dropZone, dropLabel, uploadProgress);
        
        // Add overlay to target node if it's a Pane
        if (targetNode instanceof Pane) {
            ((Pane) targetNode).getChildren().add(dropOverlay);
            
            // Position overlay to cover the entire target
            dropOverlay.prefWidthProperty().bind(((Pane) targetNode).widthProperty());
            dropOverlay.prefHeightProperty().bind(((Pane) targetNode).heightProperty());
        }
    }
    
    /**
     * Setup drag and drop event handlers
     */
    private void setupDragAndDrop() {
        // Drag over event
        targetNode.setOnDragOver(event -> {
            if (event.getGestureSource() != targetNode && hasFiles(event.getDragboard())) {
                event.acceptTransferModes(TransferMode.COPY);
                
                if (!dragActive) {
                    showDropOverlay();
                }
            }
            event.consume();
        });
        
        // Drag entered event
        targetNode.setOnDragEntered(event -> {
            if (event.getGestureSource() != targetNode && hasFiles(event.getDragboard())) {
                showDropOverlay();
            }
            event.consume();
        });
        
        // Drag exited event
        targetNode.setOnDragExited(event -> {
            hideDropOverlay();
            event.consume();
        });
        
        // Drag dropped event
        targetNode.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (hasFiles(dragboard)) {
                List<File> files = dragboard.getFiles();
                if (validateFiles(files)) {
                    handleFileDrop(files);
                    success = true;
                } else {
                    showValidationError();
                }
            }
            
            event.setDropCompleted(success);
            hideDropOverlay();
            event.consume();
        });
    }
    
    /**
     * Check if dragboard has files
     */
    private boolean hasFiles(Dragboard dragboard) {
        return dragboard.hasFiles() && !uploadInProgress;
    }
    
    /**
     * Validate dropped files
     */
    private boolean validateFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        
        // Check multiple files restriction
        if (!allowMultipleFiles && files.size() > 1) {
            return false;
        }
        
        for (File file : files) {
            // Check if directories are allowed
            if (file.isDirectory() && !allowDirectories) {
                return false;
            }
            
            // Check file size
            if (file.isFile() && file.length() > maxFileSize) {
                return false;
            }
            
            // Check file extensions
            if (!allowedExtensions.isEmpty() && file.isFile()) {
                String fileName = file.getName().toLowerCase();
                boolean validExtension = false;
                
                for (String ext : allowedExtensions) {
                    if (fileName.endsWith(ext.toLowerCase())) {
                        validExtension = true;
                        break;
                    }
                }
                
                if (!validExtension) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Handle file drop
     */
    private void handleFileDrop(List<File> files) {
        if (onFilesDropped != null) {
            // Show upload progress if enabled
            if (showProgressOverlay) {
                showUploadProgress();
            }
            
            // Process files
            onFilesDropped.accept(files);
        }
    }
    
    /**
     * Show drop overlay with animation
     */
    private void showDropOverlay() {
        if (dragActive) return;
        
        dragActive = true;
        dropOverlay.setVisible(true);
        
        // Update drop label based on configuration
        updateDropLabel();
        
        // Animate overlay appearance
        AnimationManager.fadeIn(dropOverlay, AnimationManager.FAST);
        AnimationManager.pulse(dropZone, 1.05, AnimationManager.FAST);
        
        // Animate drop zone
        dropZone.setStrokeWidth(3);
        javafx.animation.Timeline strokeAnimation = new javafx.animation.Timeline();
        strokeAnimation.getKeyFrames().addAll(
            new javafx.animation.KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(dropZone.strokeWidthProperty(), 3)),
            new javafx.animation.KeyFrame(Duration.millis(500), 
                new javafx.animation.KeyValue(dropZone.strokeWidthProperty(), 5)),
            new javafx.animation.KeyFrame(Duration.millis(1000), 
                new javafx.animation.KeyValue(dropZone.strokeWidthProperty(), 3))
        );
        strokeAnimation.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        strokeAnimation.play();
    }
    
    /**
     * Hide drop overlay with animation
     */
    private void hideDropOverlay() {
        if (!dragActive) return;
        
        dragActive = false;
        
        AnimationManager.fadeOut(dropOverlay, AnimationManager.FAST, () -> {
            dropOverlay.setVisible(false);
        });
    }
    
    /**
     * Show upload progress
     */
    private void showUploadProgress() {
        uploadInProgress = true;
        
        dropLabel.setText("📤 Uploading files...");
        uploadProgress.setVisible(true);
        uploadProgress.setProgress(0);
        
        dropOverlay.setVisible(true);
        AnimationManager.fadeIn(dropOverlay, AnimationManager.NORMAL);
    }
    
    /**
     * Update upload progress
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            if (uploadProgress.isVisible()) {
                uploadProgress.setProgress(progress);
                
                if (progress >= 1.0) {
                    completeUpload();
                }
            }
            
            if (onUploadProgress != null) {
                onUploadProgress.accept(progress);
            }
        });
    }
    
    /**
     * Complete upload process
     */
    private void completeUpload() {
        uploadInProgress = false;
        
        dropLabel.setText("✅ Upload completed!");
        
        // Hide progress after delay
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            AnimationManager.fadeOut(dropOverlay, AnimationManager.NORMAL, () -> {
                dropOverlay.setVisible(false);
                uploadProgress.setVisible(false);
                resetDropLabel();
            });
        });
        delay.play();
        
        if (onUploadComplete != null) {
            onUploadComplete.run();
        }
    }
    
    /**
     * Show upload error
     */
    public void showError(String errorMessage) {
        Platform.runLater(() -> {
            uploadInProgress = false;
            
            dropLabel.setText("❌ Upload failed: " + errorMessage);
            dropLabel.getStyleClass().add("error");
            uploadProgress.setVisible(false);
            
            // Hide error after delay
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {
                AnimationManager.fadeOut(dropOverlay, AnimationManager.NORMAL, () -> {
                    dropOverlay.setVisible(false);
                    resetDropLabel();
                });
            });
            delay.play();
            
            if (onUploadError != null) {
                onUploadError.accept(errorMessage);
            }
        });
    }
    
    /**
     * Show validation error
     */
    private void showValidationError() {
        String errorMessage = buildValidationErrorMessage();
        
        dropLabel.setText("❌ " + errorMessage);
        dropLabel.getStyleClass().add("error");
        
        // Shake animation for error feedback
        AnimationManager.shake(dropOverlay);
        
        // Hide error after delay
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> resetDropLabel());
        delay.play();
    }
    
    /**
     * Build validation error message
     */
    private String buildValidationErrorMessage() {
        StringBuilder message = new StringBuilder("Invalid files: ");
        
        if (!allowMultipleFiles) {
            message.append("Only single file allowed. ");
        }
        
        if (!allowDirectories) {
            message.append("Folders not allowed. ");
        }
        
        if (!allowedExtensions.isEmpty()) {
            message.append("Allowed types: ").append(String.join(", ", allowedExtensions)).append(". ");
        }
        
        if (maxFileSize < Long.MAX_VALUE) {
            message.append("Max size: ").append(formatFileSize(maxFileSize)).append(". ");
        }
        
        return message.toString().trim();
    }
    
    /**
     * Reset drop label to default state
     */
    private void resetDropLabel() {
        dropLabel.getStyleClass().removeIf(cls -> cls.equals("error"));
        updateDropLabel();
    }
    
    /**
     * Update drop label based on current configuration
     */
    private void updateDropLabel() {
        StringBuilder labelText = new StringBuilder("📁 Drop ");
        
        if (allowMultipleFiles) {
            labelText.append("files");
        } else {
            labelText.append("file");
        }
        
        if (allowDirectories) {
            labelText.append(" or folders");
        }
        
        labelText.append(" here to upload");
        
        if (!allowedExtensions.isEmpty()) {
            labelText.append("\\n(").append(String.join(", ", allowedExtensions)).append(")");
        }
        
        dropLabel.setText(labelText.toString());
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Configuration methods
    
    public void setAllowMultipleFiles(boolean allow) {
        this.allowMultipleFiles = allow;
        updateDropLabel();
    }
    
    public void setAllowDirectories(boolean allow) {
        this.allowDirectories = allow;
        updateDropLabel();
    }
    
    public void setAllowedExtensions(List<String> extensions) {
        this.allowedExtensions = new ArrayList<>(extensions);
        updateDropLabel();
    }
    
    public void setMaxFileSize(long maxSize) {
        this.maxFileSize = maxSize;
    }
    
    public void setShowProgressOverlay(boolean show) {
        this.showProgressOverlay = show;
    }
    
    // Callback setters
    
    public void setOnFilesDropped(Consumer<List<File>> callback) {
        this.onFilesDropped = callback;
    }
    
    public void setOnUploadProgress(Consumer<Double> callback) {
        this.onUploadProgress = callback;
    }
    
    public void setOnUploadComplete(Runnable callback) {
        this.onUploadComplete = callback;
    }
    
    public void setOnUploadError(Consumer<String> callback) {
        this.onUploadError = callback;
    }
    
    // State getters
    
    public boolean isDragActive() {
        return dragActive;
    }
    
    public boolean isUploadInProgress() {
        return uploadInProgress;
    }
    
    /**
     * Manually trigger upload completion (for external upload processes)
     */
    public void triggerUploadComplete() {
        completeUpload();
    }
    
    /**
     * Manually trigger upload error (for external upload processes)
     */
    public void triggerUploadError(String errorMessage) {
        showError(errorMessage);
    }
    
    /**
     * Enable/disable drag and drop functionality
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            setupDragAndDrop();
        } else {
            targetNode.setOnDragOver(null);
            targetNode.setOnDragEntered(null);
            targetNode.setOnDragExited(null);
            targetNode.setOnDragDropped(null);
        }
    }
}

/**
 * Utility class for creating common drag-drop configurations
 */
public class DragDropConfigurations {
    
    /**
     * Configuration for image files only
     */
    public static DragDropUploadManager createImageUploader(Node targetNode) {
        DragDropUploadManager manager = new DragDropUploadManager(targetNode);
        manager.setAllowedExtensions(List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp"));
        manager.setAllowDirectories(false);
        manager.setMaxFileSize(50 * 1024 * 1024); // 50MB
        return manager;
    }
    
    /**
     * Configuration for document files
     */
    public static DragDropUploadManager createDocumentUploader(Node targetNode) {
        DragDropUploadManager manager = new DragDropUploadManager(targetNode);
        manager.setAllowedExtensions(List.of(".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt"));
        manager.setAllowDirectories(false);
        manager.setMaxFileSize(100 * 1024 * 1024); // 100MB
        return manager;
    }
    
    /**
     * Configuration for any file type
     */
    public static DragDropUploadManager createUniversalUploader(Node targetNode) {
        DragDropUploadManager manager = new DragDropUploadManager(targetNode);
        manager.setAllowMultipleFiles(true);
        manager.setAllowDirectories(true);
        manager.setMaxFileSize(1024 * 1024 * 1024); // 1GB
        return manager;
    }
    
    /**
     * Configuration for backup files
     */
    public static DragDropUploadManager createBackupUploader(Node targetNode) {
        DragDropUploadManager manager = new DragDropUploadManager(targetNode);
        manager.setAllowedExtensions(List.of(".gvb", ".zip", ".7z", ".tar", ".gz"));
        manager.setAllowDirectories(false);
        manager.setAllowMultipleFiles(false);
        manager.setMaxFileSize(5L * 1024 * 1024 * 1024); // 5GB
        return manager;
    }
}