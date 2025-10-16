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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Detailed file information display with thumbnails
 */
public class DetailedFileInfoDisplay extends VBox {
    
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
    private VBox contentPane;
    
    private File currentFile;
    private static final int THUMBNAIL_SIZE = 120;
    
    // Cache for thumbnails
    private static final Map<String, Image> thumbnailCache = new HashMap<>();
    
    public DetailedFileInfoDisplay() {
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
        fileSizeLabel = new Label();
        fileTypeLabel = new Label();
        createdDateLabel = new Label();
        modifiedDateLabel = new Label();
        accessedDateLabel = new Label();
        pathLabel = new Label();
        permissionsLabel = new Label();
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(30, 30);
        
        contentPane = new VBox(10);
    }
    
    private void setupLayout() {
        // Thumbnail section
        VBox thumbnailSection = new VBox(5);
        thumbnailSection.setAlignment(Pos.CENTER);
        thumbnailSection.getChildren().addAll(thumbnailView, loadingIndicator);
        
        // File info section
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(10));
        
        // Add labels with descriptions
        addInfoRow(infoGrid, 0, "Name:", fileNameLabel);
        addInfoRow(infoGrid, 1, "Size:", fileSizeLabel);
        addInfoRow(infoGrid, 2, "Type:", fileTypeLabel);
        addInfoRow(infoGrid, 3, "Created:", createdDateLabel);
        addInfoRow(infoGrid, 4, "Modified:", modifiedDateLabel);
        addInfoRow(infoGrid, 5, "Accessed:", accessedDateLabel);
        addInfoRow(infoGrid, 6, "Location:", pathLabel);
        addInfoRow(infoGrid, 7, "Permissions:", permissionsLabel);
        
        // Main layout
        contentPane.getChildren().addAll(thumbnailSection, new Separator(), infoGrid);
        contentPane.setPadding(new Insets(15));
        
        this.getChildren().add(contentPane);
    }
    
    private void addInfoRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label descLabel = new Label(labelText);
        descLabel.setFont(Font.font(descLabel.getFont().getFamily(), FontWeight.BOLD, 12));
        
        grid.add(descLabel, 0, row);
        grid.add(valueLabel, 1, row);
        
        // Make value label wrap text
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(250);
    }
    
    private void applyStyles() {
        this.getStyleClass().add("detailed-file-info");
        thumbnailView.getStyleClass().add("file-thumbnail");
        contentPane.getStyleClass().add("file-info-content");
        
        // Style labels
        fileNameLabel.getStyleClass().add("file-name-label");
        fileSizeLabel.getStyleClass().add("file-info-label");
        fileTypeLabel.getStyleClass().add("file-info-label");
        createdDateLabel.getStyleClass().add("file-info-label");
        modifiedDateLabel.getStyleClass().add("file-info-label");
        accessedDateLabel.getStyleClass().add("file-info-label");
        pathLabel.getStyleClass().add("file-path-label");
        permissionsLabel.getStyleClass().add("file-info-label");
    }
    
    /**
     * Display detailed information for a file
     */
    public void displayFileInfo(File file) {
        if (file == null) {
            clearDisplay();
            return;
        }
        
        this.currentFile = file;
        
        // Show loading for thumbnail
        showThumbnailLoading(true);
        
        // Load basic file information immediately
        loadBasicFileInfo(file);
        
        // Load thumbnail asynchronously
        loadThumbnailAsync(file);
    }
    
    private void loadBasicFileInfo(File file) {
        try {
            // Basic file information
            fileNameLabel.setText(file.getName());
            fileSizeLabel.setText(formatFileSize(file.length()));
            fileTypeLabel.setText(getFileTypeDescription(file));
            pathLabel.setText(file.getParent() != null ? file.getParent() : "Root");
            
            // File attributes
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            
            LocalDateTime created = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime modified = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime accessed = LocalDateTime.ofInstant(attrs.lastAccessTime().toInstant(), ZoneId.systemDefault());
            
            createdDateLabel.setText(created.format(formatter));
            modifiedDateLabel.setText(modified.format(formatter));
            accessedDateLabel.setText(accessed.format(formatter));
            
            // Permissions
            StringBuilder permissions = new StringBuilder();
            if (file.canRead()) permissions.append("Read ");
            if (file.canWrite()) permissions.append("Write ");
            if (file.canExecute()) permissions.append("Execute");
            if (permissions.length() == 0) permissions.append("None");
            
            permissionsLabel.setText(permissions.toString().trim());
            
        } catch (Exception e) {
            // Handle errors gracefully
            createdDateLabel.setText("Unknown");
            modifiedDateLabel.setText("Unknown");
            accessedDateLabel.setText("Unknown");
            permissionsLabel.setText("Unknown");
        }
    }
    
    private void loadThumbnailAsync(File file) {
        String cacheKey = file.getAbsolutePath() + "_" + file.lastModified();
        
        // Check cache first
        if (thumbnailCache.containsKey(cacheKey)) {
            Platform.runLater(() -> {
                thumbnailView.setImage(thumbnailCache.get(cacheKey));
                showThumbnailLoading(false);
            });
            return;
        }
        
        Task<Image> thumbnailTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return generateThumbnail(file);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Image thumbnail = getValue();
                    if (thumbnail != null) {
                        thumbnailView.setImage(thumbnail);
                        thumbnailCache.put(cacheKey, thumbnail);
                    } else {
                        setDefaultThumbnail(file);
                    }
                    showThumbnailLoading(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setDefaultThumbnail(file);
                    showThumbnailLoading(false);
                });
            }
        };
        
        Thread thumbnailThread = new Thread(thumbnailTask);
        thumbnailThread.setDaemon(true);
        thumbnailThread.start();
    }
    
    private Image generateThumbnail(File file) {
        try {
            String extension = getFileExtension(file).toLowerCase();
            
            // Generate thumbnail for images
            if (isImageFile(extension)) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Image image = new Image(fis, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);
                    if (!image.isError()) {
                        return image;
                    }
                }
            }
            
            // For other file types, return null to use default icon
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private void setDefaultThumbnail(File file) {
        // Clear thumbnail for now
        thumbnailView.setImage(null);
    }
    
    private boolean isImageFile(String extension) {
        return extension.matches("jpg|jpeg|png|gif|bmp|svg|tiff|tif|webp");
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    private String getFileTypeDescription(File file) {
        if (file.isDirectory()) {
            return "Folder";
        }
        
        String extension = getFileExtension(file).toLowerCase();
        
        // Common file type descriptions
        switch (extension) {
            case "txt": return "Text Document";
            case "pdf": return "PDF Document";
            case "jpg": case "jpeg": return "JPEG Image";
            case "png": return "PNG Image";
            case "java": return "Java Source File";
            case "py": return "Python Script";
            case "js": return "JavaScript File";
            default:
                if (extension.isEmpty()) {
                    return "File";
                }
                return extension.toUpperCase() + " File";
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return new DecimalFormat("#.# KB").format(bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return new DecimalFormat("#.# MB").format(bytes / (1024.0 * 1024.0));
        return new DecimalFormat("#.# GB").format(bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private void showThumbnailLoading(boolean show) {
        loadingIndicator.setVisible(show);
        if (show) {
            thumbnailView.setImage(null);
        }
    }
    
    /**
     * Clear the display
     */
    public void clearDisplay() {
        currentFile = null;
        thumbnailView.setImage(null);
        fileNameLabel.setText("");
        fileSizeLabel.setText("");
        fileTypeLabel.setText("");
        createdDateLabel.setText("");
        modifiedDateLabel.setText("");
        accessedDateLabel.setText("");
        pathLabel.setText("");
        permissionsLabel.setText("");
        showThumbnailLoading(false);
    }
    
    /**
     * Get current file
     */
    public File getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Clear thumbnail cache
     */
    public static void clearThumbnailCache() {
        thumbnailCache.clear();
    }
}