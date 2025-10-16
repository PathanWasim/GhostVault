package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Detailed file information display with thumbnails
 */
public class FileInfoPanel extends VBox {
    
    private ImageView thumbnailView;
    private Label fileNameLabel;
    private Label fileSizeLabel;
    private Label fileTypeLabel;
    private Label createdDateLabel;
    private Label modifiedDateLabel;
    private Label permissionsLabel;
    private VBox propertiesBox;
    
    private File currentFile;
    
    public FileInfoPanel() {
        initializeComponents();
        setupLayout();
        setupStyling();
    }
    
    private void initializeComponents() {
        // Thumbnail
        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(120);
        thumbnailView.setFitHeight(120);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setSmooth(true);
        thumbnailView.getStyleClass().add("file-thumbnail");
        
        // File name
        fileNameLabel = new Label();
        fileNameLabel.getStyleClass().add("file-name-large");
        fileNameLabel.setWrapText(true);
        
        // File details
        fileSizeLabel = new Label();
        fileSizeLabel.getStyleClass().add("file-detail");
        
        fileTypeLabel = new Label();
        fileTypeLabel.getStyleClass().add("file-detail");
        
        createdDateLabel = new Label();
        createdDateLabel.getStyleClass().add("file-detail");
        
        modifiedDateLabel = new Label();
        modifiedDateLabel.getStyleClass().add("file-detail");
        
        permissionsLabel = new Label();
        permissionsLabel.getStyleClass().add("file-detail");
        
        // Properties container
        propertiesBox = new VBox(8);
        propertiesBox.getStyleClass().add("file-properties");
    }
    
    private void setupLayout() {
        this.setSpacing(16);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);
        
        // Thumbnail container
        StackPane thumbnailContainer = new StackPane(thumbnailView);
        thumbnailContainer.getStyleClass().add("thumbnail-container");
        
        // Info container
        VBox infoContainer = new VBox(8);
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.getChildren().addAll(
            fileNameLabel,
            fileSizeLabel,
            fileTypeLabel
        );
        
        // Details container
        VBox detailsContainer = new VBox(6);
        detailsContainer.getChildren().addAll(
            createdDateLabel,
            modifiedDateLabel,
            permissionsLabel
        );
        
        this.getChildren().addAll(
            thumbnailContainer,
            infoContainer,
            detailsContainer,
            propertiesBox
        );
    }
    
    private void setupStyling() {
        this.getStyleClass().add("file-info-panel");
    }
    
    public void displayFileInfo(File file) {
        if (file == null) {
            clearInfo();
            return;
        }
        
        this.currentFile = file;
        
        // Set file name
        fileNameLabel.setText(file.getName());
        
        // Set thumbnail
        setThumbnail(file);
        
        // Set file size
        if (file.isFile()) {
            fileSizeLabel.setText("Size: " + formatFileSize(file.length()));
        } else {
            fileSizeLabel.setText("Type: Directory");
        }
        
        // Set file type
        String extension = getFileExtension(file.getName());
        if (!extension.isEmpty()) {
            fileTypeLabel.setText("Type: " + extension.toUpperCase() + " File");
        } else {
            fileTypeLabel.setText("Type: " + (file.isDirectory() ? "Folder" : "File"));
        }
        
        // Set dates
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            
            LocalDateTime created = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime modified = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            
            createdDateLabel.setText("Created: " + created.format(formatter));
            modifiedDateLabel.setText("Modified: " + modified.format(formatter));
            
        } catch (Exception e) {
            createdDateLabel.setText("Created: Unknown");
            modifiedDateLabel.setText("Modified: Unknown");
        }
        
        // Set permissions
        StringBuilder permissions = new StringBuilder("Permissions: ");
        if (file.canRead()) permissions.append("R");
        if (file.canWrite()) permissions.append("W");
        if (file.canExecute()) permissions.append("X");
        permissionsLabel.setText(permissions.toString());
        
        // Add additional properties
        updateProperties(file);
    }
    
    private void setThumbnail(File file) {
        String fileName = file.getName().toLowerCase();
        
        // Try to load actual image thumbnail for image files
        if (isImageFile(fileName) && file.length() < 10 * 1024 * 1024) { // Max 10MB for thumbnail
            try {
                Image image = new Image(file.toURI().toString(), 120, 120, true, true);
                thumbnailView.setImage(image);
                return;
            } catch (Exception e) {
                // Fall back to icon
            }
        }
        
        // Use icon based on file type
        String icon = ModernIcons.getFileIcon(fileName);
        Label iconLabel = ModernIcons.createIcon(icon, 64);
        
        // Create a simple thumbnail with the icon
        StackPane iconContainer = new StackPane(iconLabel);
        iconContainer.setPrefSize(120, 120);
        iconContainer.getStyleClass().add("file-icon-thumbnail");
        
        // Clear image and show icon instead
        thumbnailView.setImage(null);
        
        // Replace thumbnail with icon (this is a simplified approach)
        if (this.getChildren().size() > 0 && this.getChildren().get(0) instanceof StackPane) {
            StackPane container = (StackPane) this.getChildren().get(0);
            container.getChildren().clear();
            container.getChildren().add(iconContainer);
        }
    }
    
    private void updateProperties(File file) {
        propertiesBox.getChildren().clear();
        
        if (file.isDirectory()) {
            // Count files in directory
            try {
                File[] files = file.listFiles();
                if (files != null) {
                    int fileCount = 0;
                    int folderCount = 0;
                    
                    for (File f : files) {
                        if (f.isDirectory()) {
                            folderCount++;
                        } else {
                            fileCount++;
                        }
                    }
                    
                    Label itemsLabel = new Label(String.format("Contains: %d files, %d folders", fileCount, folderCount));
                    itemsLabel.getStyleClass().add("file-property");
                    propertiesBox.getChildren().add(itemsLabel);
                }
            } catch (Exception e) {
                // Ignore
            }
        } else {
            // File-specific properties
            String extension = getFileExtension(file.getName()).toLowerCase();
            
            if (isImageFile(file.getName())) {
                addImageProperties(file);
            } else if (isVideoFile(file.getName())) {
                addVideoProperties(file);
            } else if (isAudioFile(file.getName())) {
                addAudioProperties(file);
            }
        }
    }
    
    private void addImageProperties(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            if (!image.isError()) {
                Label dimensionsLabel = new Label(String.format("Dimensions: %.0f × %.0f", image.getWidth(), image.getHeight()));
                dimensionsLabel.getStyleClass().add("file-property");
                propertiesBox.getChildren().add(dimensionsLabel);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void addVideoProperties(File file) {
        // Placeholder for video properties
        Label videoLabel = new Label("Video file - Use media player for details");
        videoLabel.getStyleClass().add("file-property");
        propertiesBox.getChildren().add(videoLabel);
    }
    
    private void addAudioProperties(File file) {
        // Placeholder for audio properties
        Label audioLabel = new Label("Audio file - Use media player for details");
        audioLabel.getStyleClass().add("file-property");
        propertiesBox.getChildren().add(audioLabel);
    }
    
    private void clearInfo() {
        fileNameLabel.setText("");
        fileSizeLabel.setText("");
        fileTypeLabel.setText("");
        createdDateLabel.setText("");
        modifiedDateLabel.setText("");
        permissionsLabel.setText("");
        thumbnailView.setImage(null);
        propertiesBox.getChildren().clear();
        currentFile = null;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot);
    }
    
    private boolean isImageFile(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return ext.matches("\\.(jpg|jpeg|png|gif|bmp|tiff|svg|webp)");
    }
    
    private boolean isVideoFile(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return ext.matches("\\.(mp4|avi|mkv|mov|wmv|flv|webm|m4v)");
    }
    
    private boolean isAudioFile(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return ext.matches("\\.(mp3|wav|aac|flac|ogg|m4a|wma)");
    }
    
    public File getCurrentFile() {
        return currentFile;
    }
}