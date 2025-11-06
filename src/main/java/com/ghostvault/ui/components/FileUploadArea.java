package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dedicated file upload area with drag-and-drop and browse functionality
 */
public class FileUploadArea extends VBox {
    
    private DragDropFileUploader dragDropUploader;
    private Consumer<List<File>> onFilesSelected;
    private Stage parentStage;
    private boolean allowMultipleFiles = true;
    private List<String> allowedExtensions;
    
    public FileUploadArea() {
        super();
        initializeComponents();
        setupLayout();
        setupDragAndDrop();
        applyStyles();
    }
    
    public FileUploadArea(Stage parentStage) {
        this();
        this.parentStage = parentStage;
    }
    
    private void initializeComponents() {
        dragDropUploader = new DragDropFileUploader();
    }
    
    private void setupLayout() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setPadding(new Insets(40));
        this.setPrefHeight(200);
        
        // Create main drop zone
        VBox dropZone = dragDropUploader.createDropZone();
        
        // Add browse button
        Button browseButton = new Button("Browse Files");
        browseButton.getStyleClass().add("browse-button");
        browseButton.setOnAction(e -> showFileChooser());
        
        // Add instruction text
        Label instructionLabel = new Label("Drag files here or click browse to select files");
        instructionLabel.setFont(Font.font(instructionLabel.getFont().getFamily(), FontWeight.NORMAL, 14));
        instructionLabel.setStyle("-fx-text-fill: #666;");
        
        this.getChildren().addAll(dropZone, browseButton, instructionLabel);
    }
    
    private void setupDragAndDrop() {
        // Enable drag and drop on this component
        dragDropUploader.enableDragAndDrop(this);
        
        // Configure allowed extensions if set
        if (allowedExtensions != null) {
            dragDropUploader.setAllowedExtensions(allowedExtensions);
        }
        
        // Handle file drops
        dragDropUploader.setOnFilesDropped(files -> {
            if (onFilesSelected != null) {
                onFilesSelected.accept(files);
            }
        });
        
        // Visual feedback
        dragDropUploader.setOnDragEntered(event -> {
            this.getStyleClass().add("drag-over");
        });
        
        dragDropUploader.setOnDragExited(event -> {
            this.getStyleClass().remove("drag-over");
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("file-upload-area");
        
        this.setStyle(
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 2px;" +
            "-fx-border-style: dashed;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-color: #fafafa;" +
            "-fx-background-radius: 12px;"
        );
    }
    
    /**
     * Show file chooser dialog
     */
    private void showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Upload");
        
        // Set allowed extensions if specified
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            StringBuilder description = new StringBuilder("Allowed files (");
            for (int i = 0; i < allowedExtensions.size(); i++) {
                if (i > 0) description.append(", ");
                description.append("*.").append(allowedExtensions.get(i));
            }
            description.append(")");
            
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                description.toString(),
                allowedExtensions.stream().map(ext -> "*." + ext).toArray(String[]::new)
            );
            fileChooser.getExtensionFilters().add(filter);
        }
        
        List<File> selectedFiles;
        if (allowMultipleFiles) {
            selectedFiles = fileChooser.showOpenMultipleDialog(parentStage);
        } else {
            File singleFile = fileChooser.showOpenDialog(parentStage);
            selectedFiles = singleFile != null ? List.of(singleFile) : null;
        }
        
        if (selectedFiles != null && !selectedFiles.isEmpty() && onFilesSelected != null) {
            onFilesSelected.accept(selectedFiles);
        }
    }
    
    /**
     * Create a compact upload area for smaller spaces
     */
    public static FileUploadArea createCompact(Stage parentStage) {
        FileUploadArea uploadArea = new FileUploadArea(parentStage);
        uploadArea.setPrefHeight(120);
        uploadArea.setPadding(new Insets(20));
        uploadArea.setSpacing(10);
        
        // Replace the drop zone with a compact version
        uploadArea.getChildren().clear();
        
        HBox compactZone = uploadArea.dragDropUploader.createCompactDropZone();
        
        Label instructionLabel = new Label("Drop files or click to browse");
        instructionLabel.setFont(Font.font(12));
        instructionLabel.setStyle("-fx-text-fill: #666;");
        
        uploadArea.getChildren().addAll(compactZone, instructionLabel);
        
        return uploadArea;
    }
    
    /**
     * Show upload progress overlay
     */
    public void showUploadProgress() {
        VBox progressOverlay = dragDropUploader.createUploadProgressOverlay();
        
        // Add overlay to the upload area
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(this, progressOverlay);
        
        // Replace this component with the stack pane in parent
        if (this.getParent() instanceof Pane) {
            Pane parent = (Pane) this.getParent();
            int index = parent.getChildren().indexOf(this);
            parent.getChildren().set(index, stackPane);
        }
    }
    
    /**
     * Show validation message
     */
    public void showValidationMessage(String message, boolean isError) {
        Label validationLabel = dragDropUploader.createValidationMessage(message, isError);
        
        // Add validation message to the upload area
        if (!this.getChildren().contains(validationLabel)) {
            this.getChildren().add(validationLabel);
        }
        
        // Remove after 3 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                this.getChildren().remove(validationLabel);
            })
        );
        timeline.play();
    }
    
    // Configuration methods
    public void setOnFilesSelected(Consumer<List<File>> callback) {
        this.onFilesSelected = callback;
    }
    
    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
    
    public void setAllowMultipleFiles(boolean allowMultiple) {
        this.allowMultipleFiles = allowMultiple;
    }
    
    public void setAllowedExtensions(List<String> extensions) {
        this.allowedExtensions = extensions;
        if (dragDropUploader != null) {
            dragDropUploader.setAllowedExtensions(extensions);
        }
    }
    
    public void setAllowDirectories(boolean allowDirectories) {
        if (dragDropUploader != null) {
            dragDropUploader.setAllowDirectories(allowDirectories);
        }
    }
    
    /**
     * Get the underlying drag-drop uploader
     */
    public DragDropFileUploader getDragDropUploader() {
        return dragDropUploader;
    }
}