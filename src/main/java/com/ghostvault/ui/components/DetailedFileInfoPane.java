package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Detailed file information display with thumbnails and metadata
 */
public class DetailedFileInfoPane extends VBox {
    
    // UI Components
    private ImageView thumbnailView;
    private Label fileNameLabel;
    private Label fileSizeLabel;
    private Label fileTypeLabel;
    private Label lastModifiedLabel;
    private Label filePathLabel;
    private VBox metadataContainer;
    private ProgressIndicator loadingIndicator;
    
    // State
    private File currentFile;
    private static final int THUMBNAIL_SIZE = 64;
    
    public DetailedFileInfoPane() {
        initializeComponents();
        setupLayout();
        setupStyling();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Thumbnail
        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(THUMBNAIL_SIZE);
        thumbnailView.setFitHeight(THUMBNAIL_SIZE);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.getStyleClass().add("file-thumbnail");
        
        // File information labels
        fileNameLabel = new Label("No file selected");
        fileNameLabel.getStyleClass().add("file-name-label");
        
        fileSizeLabel = new Label("");
        fileSizeLabel.getStyleClass().add("file-size-label");
        
        fileTypeLabel = new Label("");
        fileTypeLabel.getStyleClass().add("file-type-label");   
     
        lastModifiedLabel = new Label("");
        lastModifiedLabel.getStyleClass().add("last-modified-label");
        
        filePathLabel = new Label("");
        filePathLabel.getStyleClass().add("file-path-label");
        
        // Metadata container
        metadataContainer = new VBox(4);
        metadataContainer.getStyleClass().add("metadata-container");
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("thumbnail-loading");
        loadingIndicator.setPrefSize(24, 24);
        loadingIndicator.setVisible(false);
        
        this.getChildren().addAll(
            thumbnailView, fileNameLabel, fileSizeLabel, 
            fileTypeLabel, lastModifiedLabel, filePathLabel, 
            metadataContainer, loadingIndicator
        );
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        this.setSpacing(8);
        this.setPadding(new Insets(12));
        this.setAlignment(Pos.TOP_LEFT);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        this.getStyleClass().add("detailed-file-info-pane");
    }
    
    /**
     * Display file information
     */
    public void displayFile(File file) {
        if (file == null) {
            clearDisplay();
            return;
        }
        
        currentFile = file;
        
        // Update basic information
        fileNameLabel.setText(file.getName());
        fileSizeLabel.setText("Size: " + formatFileSize(file.length()));
        fileTypeLabel.setText("Type: " + getFileType(file));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        lastModifiedLabel.setText("Modified: " + dateFormat.format(new Date(file.lastModified())));
        filePathLabel.setText("Path: " + file.getAbsolutePath());
        
        // Load thumbnail
        loadThumbnail(file);
        
        // Update metadata
        updateMetadata(file);
    }
    
    /**
     * Load thumbnail for file
     */
    private void loadThumbnail(File file) {
        // Show loading
        loadingIndicator.setVisible(true);
        thumbnailView.setImage(null);
        
        Task<Image> thumbnailTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return ThumbnailGenerator.generateThumbnail(file, THUMBNAIL_SIZE);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Image thumbnail = getValue();
                    if (thumbnail != null) {
                        thumbnailView.setImage(thumbnail);
                    } else {
                        setDefaultIcon(file);
                    }
                    loadingIndicator.setVisible(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setDefaultIcon(file);
                    loadingIndicator.setVisible(false);
                });
            }
        };
        
        Thread thumbnailThread = new Thread(thumbnailTask);
        thumbnailThread.setDaemon(true);
        thumbnailThread.start();
    }
    
    /**
     * Set default icon based on file type
     */
    private void setDefaultIcon(File file) {
        String iconPath = FileIconProvider.getIconPath(file);
        if (iconPath != null) {
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                thumbnailView.setImage(icon);
            } catch (Exception e) {
                // Use fallback icon
                thumbnailView.setImage(null);
            }
        }
    }
    
    /**
     * Update metadata display
     */
    private void updateMetadata(File file) {
        metadataContainer.getChildren().clear();
        
        if (file.isDirectory()) {
            // Directory metadata
            addMetadataRow("Type", "Folder");
            
            // Count items in directory
            File[] children = file.listFiles();
            if (children != null) {
                addMetadataRow("Items", String.valueOf(children.length));
            }
        } else {
            // File metadata
            String extension = getFileExtension(file.getName());
            if (!extension.isEmpty()) {
                addMetadataRow("Extension", extension.toUpperCase());
            }
            
            // Additional metadata based on file type
            if (isImageFile(file)) {
                addImageMetadata(file);
            } else if (isVideoFile(file)) {
                addVideoMetadata(file);
            } else if (isAudioFile(file)) {
                addAudioMetadata(file);
            }
        }
        
        // Permissions
        addMetadataRow("Readable", file.canRead() ? "Yes" : "No");
        addMetadataRow("Writable", file.canWrite() ? "Yes" : "No");
        addMetadataRow("Executable", file.canExecute() ? "Yes" : "No");
    }
    
    /**
     * Add metadata row
     */
    private void addMetadataRow(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelControl = new Label(label + ":");
        labelControl.getStyleClass().add("metadata-label");
        labelControl.setPrefWidth(80);
        
        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("metadata-value");
        
        row.getChildren().addAll(labelControl, valueControl);
        metadataContainer.getChildren().add(row);
    }
    
    /**
     * Clear display
     */
    private void clearDisplay() {
        currentFile = null;
        fileNameLabel.setText("No file selected");
        fileSizeLabel.setText("");
        fileTypeLabel.setText("");
        lastModifiedLabel.setText("");
        filePathLabel.setText("");
        thumbnailView.setImage(null);
        metadataContainer.getChildren().clear();
        loadingIndicator.setVisible(false);
    }
    
    // Helper methods for file type detection and formatting
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private String getFileType(File file) {
        if (file.isDirectory()) return "Folder";
        String extension = getFileExtension(file.getName());
        return extension.isEmpty() ? "File" : extension.toUpperCase() + " File";
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0 && lastDot < fileName.length() - 1) ? 
               fileName.substring(lastDot + 1) : "";
    }
    
    private boolean isImageFile(File file) {
        String ext = getFileExtension(file.getName()).toLowerCase();
        return ext.matches("jpg|jpeg|png|gif|bmp|svg|webp|tiff|tif");
    }
    
    private boolean isVideoFile(File file) {
        String ext = getFileExtension(file.getName()).toLowerCase();
        return ext.matches("mp4|avi|mkv|mov|wmv|flv|webm|m4v");
    }
    
    private boolean isAudioFile(File file) {
        String ext = getFileExtension(file.getName()).toLowerCase();
        return ext.matches("mp3|wav|flac|aac|ogg|wma|m4a");
    }
    
    private void addImageMetadata(File file) {
        // Placeholder for image metadata
        addMetadataRow("Category", "Image");
    }
    
    private void addVideoMetadata(File file) {
        // Placeholder for video metadata
        addMetadataRow("Category", "Video");
    }
    
    private void addAudioMetadata(File file) {
        // Placeholder for audio metadata
        addMetadataRow("Category", "Audio");
    }
    
    public File getCurrentFile() {
        return currentFile;
    }
}