package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Detailed file information display with thumbnails
 */
public class DetailedFileInfoPane extends VBox {
    
    private ImageView thumbnailView;
    private Label fileNameLabel;
    private Label fileSizeLabel;
    private Label fileTypeLabel;
    private Label createdDateLabel;
    private Label modifiedDateLabel;
    private Label accessedDateLabel;
    private Label pathLabel;
    private Label permissionsLabel;
    private ProgressIndicator loadingIndicator;
    private VBox propertiesBox;
    
    private File currentFile;
    private static final int THUMBNAIL_SIZE = 120;
    
    // Image formats that can have thumbnails
    private static final List<String> IMAGE_FORMATS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "tiff", "tif", "webp"
    );
    
    public DetailedFileInfoPane() {
        super();
        initializeComponents();
        setupLayout();
        applyStyles();
    }
    
    private void initializeComponents() {
        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(THUMBNAIL_SIZE);
        thumbnailView.setFitHeight(THUMBNAIL_SIZE);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setSmooth(true);
        
        fileNameLabel = new Label();
        fileNameLabel.setFont(Font.font(fileNameLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        fileNameLabel.setWrapText(true);
        
        fileSizeLabel = new Label();
        fileTypeLabel = new Label();
        createdDateLabel = new Label();
        modifiedDateLabel = new Label();
        accessedDateLabel = new Label();
        pathLabel = new Label();
        pathLabel.setWrapText(true);
        permissionsLabel = new Label();
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(30, 30);
        
        propertiesBox = new VBox(8);
    }
    
    private void setupLayout() {
        // Thumbnail section
        VBox thumbnailSection = new VBox(10);
        thumbnailSection.setAlignment(Pos.CENTER);
        thumbnailSection.setPadding(new Insets(10));
        
        StackPane thumbnailPane = new StackPane();
        thumbnailPane.getChildren().addAll(thumbnailView, loadingIndicator);
        thumbnailPane.setStyle(
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-color: #f9f9f9;"
        );
        thumbnailPane.setPrefSize(THUMBNAIL_SIZE + 10, THUMBNAIL_SIZE + 10);
        
        thumbnailSection.getChildren().addAll(thumbnailPane, fileNameLabel);
        
        // Properties section
        propertiesBox.setPadding(new Insets(10));
        
        this.getChildren().addAll(thumbnailSection, new Separator(), propertiesBox);
        this.setSpacing(10);
    }
    
    private void applyStyles() {
        this.getStyleClass().add("detailed-file-info-pane");
        
        this.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        
        fileNameLabel.getStyleClass().add("file-name-label");
        fileSizeLabel.getStyleClass().add("file-property-label");
        fileTypeLabel.getStyleClass().add("file-property-label");
        createdDateLabel.getStyleClass().add("file-property-label");
        modifiedDateLabel.getStyleClass().add("file-property-label");
        accessedDateLabel.getStyleClass().add("file-property-label");
        pathLabel.getStyleClass().add("file-property-label");
        permissionsLabel.getStyleClass().add("file-property-label");
    }
    
    /**
     * Display detailed information for a file
     */
    public void showFileInfo(File file) {
        if (file == null) {
            clear();
            return;
        }
        
        this.currentFile = file;
        
        // Set basic info immediately
        fileNameLabel.setText(file.getName());
        
        // Load thumbnail if it's an image
        loadThumbnail(file);
        
        // Load detailed file attributes
        loadFileAttributes(file);
    }
    
    private void loadThumbnail(File file) {
        // Clear previous thumbnail
        thumbnailView.setImage(null);
        
        if (file.isDirectory()) {
            // Show folder icon
            setDefaultIcon("📁", "#4CAF50");
            return;
        }
        
        String extension = getFileExtension(file).toLowerCase();
        
        if (IMAGE_FORMATS.contains(extension)) {
            // Load image thumbnail
            loadImageThumbnail(file);
        } else {
            // Show file type icon
            setFileTypeIcon(extension);
        }
    }
    
    private void loadImageThumbnail(File file) {
        loadingIndicator.setVisible(true);
        
        Task<Image> thumbnailTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                try (FileInputStream fis = new FileInputStream(file)) {
                    // Load image with size constraints for thumbnail
                    return new Image(fis, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Image thumbnail = getValue();
                    if (thumbnail != null && !thumbnail.isError()) {
                        thumbnailView.setImage(thumbnail);
                    } else {
                        setDefaultIcon("🖼", "#2196F3");
                    }
                    loadingIndicator.setVisible(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setDefaultIcon("🖼", "#2196F3");
                    loadingIndicator.setVisible(false);
                });
            }
        };
        
        Thread thumbnailThread = new Thread(thumbnailTask);
        thumbnailThread.setDaemon(true);
        thumbnailThread.start();
    }
    
    private void setFileTypeIcon(String extension) {
        String icon;
        String color;
        
        switch (extension) {
            case "pdf":
                icon = "📄";
                color = "#F44336";
                break;
            case "doc":
            case "docx":
                icon = "📝";
                color = "#2196F3";
                break;
            case "xls":
            case "xlsx":
                icon = "📊";
                color = "#4CAF50";
                break;
            case "ppt":
            case "pptx":
                icon = "📈";
                color = "#FF9800";
                break;
            case "txt":
                icon = "📃";
                color = "#9E9E9E";
                break;
            case "zip":
            case "rar":
            case "7z":
                icon = "🗜";
                color = "#795548";
                break;
            case "mp3":
            case "wav":
            case "flac":
                icon = "🎵";
                color = "#9C27B0";
                break;
            case "mp4":
            case "avi":
            case "mov":
                icon = "🎬";
                color = "#E91E63";
                break;
            case "exe":
                icon = "⚙";
                color = "#607D8B";
                break;
            default:
                icon = "📄";
                color = "#757575";
                break;
        }
        
        setDefaultIcon(icon, color);
    }
    
    private void setDefaultIcon(String icon, String color) {
        // Create a simple text-based icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(48));
        iconLabel.setStyle("-fx-text-fill: " + color + ";");
        
        // Convert label to image (simplified approach)
        thumbnailView.setImage(null);
        
        // For now, we'll just clear the thumbnail for non-image files
        // In a full implementation, you might want to create actual icon images
    }
    
    private void loadFileAttributes(File file) {
        Task<Void> attributesTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    
                    Platform.runLater(() -> {
                        updateFileProperties(file, attrs);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        updateBasicFileProperties(file);
                    });
                }
                return null;
            }
        };
        
        Thread attributesThread = new Thread(attributesTask);
        attributesThread.setDaemon(true);
        attributesThread.start();
    }
    
    private void updateFileProperties(File file, BasicFileAttributes attrs) {
        propertiesBox.getChildren().clear();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
        
        // File size
        String sizeText;
        if (file.isDirectory()) {
            sizeText = "Directory";
        } else {
            sizeText = formatFileSize(file.length());
        }
        addProperty("Size:", sizeText);
        
        // File type
        String typeText;
        if (file.isDirectory()) {
            typeText = "Folder";
        } else {
            String extension = getFileExtension(file);
            typeText = extension.isEmpty() ? "File" : extension.toUpperCase() + " File";
        }
        addProperty("Type:", typeText);
        
        // Dates
        addProperty("Created:", dateFormat.format(new Date(attrs.creationTime().toMillis())));
        addProperty("Modified:", dateFormat.format(new Date(attrs.lastModifiedTime().toMillis())));
        addProperty("Accessed:", dateFormat.format(new Date(attrs.lastAccessTime().toMillis())));
        
        // Path
        addProperty("Location:", file.getParent() != null ? file.getParent() : "");
        
        // Permissions
        StringBuilder permissions = new StringBuilder();
        if (file.canRead()) permissions.append("Read ");
        if (file.canWrite()) permissions.append("Write ");
        if (file.canExecute()) permissions.append("Execute");
        addProperty("Permissions:", permissions.toString().trim());
        
        // Additional properties for files
        if (file.isFile()) {
            addProperty("Full Path:", file.getAbsolutePath());
            
            // File extension
            String extension = getFileExtension(file);
            if (!extension.isEmpty()) {
                addProperty("Extension:", "." + extension);
            }
        }
    }
    
    private void updateBasicFileProperties(File file) {
        propertiesBox.getChildren().clear();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
        
        // Basic properties using File API
        String sizeText = file.isDirectory() ? "Directory" : formatFileSize(file.length());
        addProperty("Size:", sizeText);
        
        String typeText = file.isDirectory() ? "Folder" : "File";
        addProperty("Type:", typeText);
        
        addProperty("Modified:", dateFormat.format(new Date(file.lastModified())));
        addProperty("Location:", file.getParent() != null ? file.getParent() : "");
        
        StringBuilder permissions = new StringBuilder();
        if (file.canRead()) permissions.append("Read ");
        if (file.canWrite()) permissions.append("Write ");
        if (file.canExecute()) permissions.append("Execute");
        addProperty("Permissions:", permissions.toString().trim());
    }
    
    private void addProperty(String label, String value) {
        HBox propertyRow = new HBox(10);
        propertyRow.setAlignment(Pos.CENTER_LEFT);
        
        Label labelControl = new Label(label);
        labelControl.setFont(Font.font(labelControl.getFont().getFamily(), FontWeight.BOLD, 12));
        labelControl.setMinWidth(80);
        labelControl.setStyle("-fx-text-fill: #666;");
        
        Label valueControl = new Label(value);
        valueControl.setFont(Font.font(12));
        valueControl.setWrapText(true);
        valueControl.setStyle("-fx-text-fill: #333;");
        
        propertyRow.getChildren().addAll(labelControl, valueControl);
        HBox.setHgrow(valueControl, Priority.ALWAYS);
        
        propertiesBox.getChildren().add(propertyRow);
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return new DecimalFormat("#.#").format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0)) + " MB";
        return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }
    
    /**
     * Clear the file information display
     */
    public void clear() {
        currentFile = null;
        thumbnailView.setImage(null);
        fileNameLabel.setText("No file selected");
        propertiesBox.getChildren().clear();
        loadingIndicator.setVisible(false);
    }
    
    /**
     * Get the currently displayed file
     */
    public File getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Refresh the display for the current file
     */
    public void refresh() {
        if (currentFile != null) {
            showFileInfo(currentFile);
        }
    }
}