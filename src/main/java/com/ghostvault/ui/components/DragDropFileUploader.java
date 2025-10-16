package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drag and drop file uploader component
 */
public class DragDropFileUploader {
    
    private Consumer<List<File>> onFilesDropped;
    private Consumer<DragEvent> onDragEntered;
    private Consumer<DragEvent> onDragExited;
    private boolean allowDirectories = true;
    private List<String> allowedExtensions = new ArrayList<>();
    
    /**
     * Enable drag and drop on a node
     */
    public void enableDragAndDrop(Node node) {
        // Set up drag over handler
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                List<File> files = event.getDragboard().getFiles();
                if (areFilesAcceptable(files)) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
            event.consume();
        });
        
        // Set up drag entered handler
        node.setOnDragEntered(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                List<File> files = event.getDragboard().getFiles();
                if (areFilesAcceptable(files)) {
                    addDragOverEffect(node);
                    if (onDragEntered != null) {
                        onDragEntered.accept(event);
                    }
                }
            }
            event.consume();
        });
        
        // Set up drag exited handler
        node.setOnDragExited(event -> {
            removeDragOverEffect(node);
            if (onDragExited != null) {
                onDragExited.accept(event);
            }
            event.consume();
        });
        
        // Set up drag dropped handler
        node.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                if (areFilesAcceptable(files)) {
                    if (onFilesDropped != null) {
                        onFilesDropped.accept(files);
                    }
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            removeDragOverEffect(node);
            event.consume();
        });
    }
    
    /**
     * Create a dedicated drop zone component
     */
    public VBox createDropZone() {
        VBox dropZone = new VBox(20);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(40));
        dropZone.setPrefHeight(200);
        dropZone.getStyleClass().add("drag-drop-zone");
        
        // Drop zone icon (using text for now)
        Label iconLabel = new Label("📁");
        iconLabel.setFont(Font.font(48));
        iconLabel.getStyleClass().add("drop-zone-icon");
        
        // Main message
        Label mainLabel = new Label("Drag and drop files here");
        mainLabel.setFont(Font.font(mainLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        mainLabel.getStyleClass().add("drop-zone-main-text");
        
        // Secondary message
        Label secondaryLabel = new Label("or click to browse");
        secondaryLabel.setFont(Font.font(12));
        secondaryLabel.getStyleClass().add("drop-zone-secondary-text");
        
        dropZone.getChildren().addAll(iconLabel, mainLabel, secondaryLabel);
        
        // Enable drag and drop
        enableDragAndDrop(dropZone);
        
        // Add click handler for file browsing
        dropZone.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                // This would trigger file chooser dialog
                NotificationSystem.showInfo("File Browser", "Opening file browser...");
            }
        });
        
        // Style the drop zone
        dropZone.setStyle(
            "-fx-border-color: #ddd; " +
            "-fx-border-width: 2px; " +
            "-fx-border-style: dashed; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-color: #fafafa; " +
            "-fx-cursor: hand;"
        );
        
        return dropZone;
    }
    
    /**
     * Create a compact drop zone for smaller areas
     */
    public HBox createCompactDropZone() {
        HBox dropZone = new HBox(10);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(15));
        dropZone.getStyleClass().add("compact-drag-drop-zone");
        
        Label iconLabel = new Label("📎");
        iconLabel.setFont(Font.font(16));
        
        Label textLabel = new Label("Drop files here or click to browse");
        textLabel.setFont(Font.font(12));
        
        dropZone.getChildren().addAll(iconLabel, textLabel);
        
        // Enable drag and drop
        enableDragAndDrop(dropZone);
        
        // Add click handler
        dropZone.setOnMouseClicked(event -> {
            NotificationSystem.showInfo("File Browser", "Opening file browser...");
        });
        
        // Style
        dropZone.setStyle(
            "-fx-border-color: #ccc; " +
            "-fx-border-width: 1px; " +
            "-fx-border-style: dashed; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-color: #f8f8f8; " +
            "-fx-cursor: hand;"
        );
        
        return dropZone;
    }
    
    /**
     * Add visual feedback when files are dragged over
     */
    private void addDragOverEffect(Node node) {
        if (node instanceof Region) {
            Region region = (Region) node;
            
            // Store original style
            String originalStyle = region.getStyle();
            region.getProperties().put("original-style", originalStyle);
            
            // Add drag over style
            String dragOverStyle = originalStyle + 
                "; -fx-border-color: #4CAF50" +
                "; -fx-border-width: 3px" +
                "; -fx-background-color: rgba(76, 175, 80, 0.1)" +
                "; -fx-border-style: solid";
            
            region.setStyle(dragOverStyle);
        }
    }
    
    /**
     * Remove visual feedback when drag exits
     */
    private void removeDragOverEffect(Node node) {
        if (node instanceof Region) {
            Region region = (Region) node;
            
            // Restore original style
            String originalStyle = (String) region.getProperties().get("original-style");
            if (originalStyle != null) {
                region.setStyle(originalStyle);
            }
        }
    }
    
    /**
     * Check if the dropped files are acceptable
     */
    private boolean areFilesAcceptable(List<File> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        
        for (File file : files) {
            // Check if directories are allowed
            if (file.isDirectory() && !allowDirectories) {
                return false;
            }
            
            // Check file extensions if specified
            if (!allowedExtensions.isEmpty() && file.isFile()) {
                String extension = getFileExtension(file).toLowerCase();
                if (!allowedExtensions.contains(extension)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    /**
     * Show upload progress overlay
     */
    public VBox createUploadProgressOverlay() {
        VBox overlay = new VBox(15);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(30));
        overlay.getStyleClass().add("upload-progress-overlay");
        
        Label iconLabel = new Label("⬆");
        iconLabel.setFont(Font.font(36));
        iconLabel.setTextFill(Color.BLUE);
        
        Label messageLabel = new Label("Uploading files...");
        messageLabel.setFont(Font.font(messageLabel.getFont().getFamily(), FontWeight.BOLD, 14));
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        
        Label progressLabel = new Label("0% complete");
        progressLabel.setFont(Font.font(12));
        
        overlay.getChildren().addAll(iconLabel, messageLabel, progressBar, progressLabel);
        
        // Style the overlay
        overlay.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-border-color: #ddd; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
        );
        
        return overlay;
    }
    
    /**
     * Create file validation message
     */
    public Label createValidationMessage(String message, boolean isError) {
        Label validationLabel = new Label(message);
        validationLabel.setFont(Font.font(12));
        
        if (isError) {
            validationLabel.setTextFill(Color.RED);
            validationLabel.setText("❌ " + message);
        } else {
            validationLabel.setTextFill(Color.GREEN);
            validationLabel.setText("✅ " + message);
        }
        
        return validationLabel;
    }
    
    // Configuration methods
    public void setOnFilesDropped(Consumer<List<File>> onFilesDropped) {
        this.onFilesDropped = onFilesDropped;
    }
    
    public void setOnDragEntered(Consumer<DragEvent> onDragEntered) {
        this.onDragEntered = onDragEntered;
    }
    
    public void setOnDragExited(Consumer<DragEvent> onDragExited) {
        this.onDragExited = onDragExited;
    }
    
    public void setAllowDirectories(boolean allowDirectories) {
        this.allowDirectories = allowDirectories;
    }
    
    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = new ArrayList<>(allowedExtensions);
    }
    
    public void addAllowedExtension(String extension) {
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            allowedExtensions.add(extension.toLowerCase());
        }
    }
    
    public void clearAllowedExtensions() {
        allowedExtensions.clear();
    }
    
    /**
     * Utility method to format file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Utility method to get file count description
     */
    public static String getFileCountDescription(List<File> files) {
        if (files == null || files.isEmpty()) {
            return "No files";
        }
        
        long totalSize = files.stream().mapToLong(File::length).sum();
        int fileCount = (int) files.stream().filter(File::isFile).count();
        int folderCount = (int) files.stream().filter(File::isDirectory).count();
        
        StringBuilder description = new StringBuilder();
        
        if (fileCount > 0) {
            description.append(fileCount).append(" file");
            if (fileCount > 1) description.append("s");
        }
        
        if (folderCount > 0) {
            if (description.length() > 0) description.append(", ");
            description.append(folderCount).append(" folder");
            if (folderCount > 1) description.append("s");
        }
        
        if (totalSize > 0) {
            description.append(" (").append(formatFileSize(totalSize)).append(")");
        }
        
        return description.toString();
    }
}