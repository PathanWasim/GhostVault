package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Modern file uploader with drag-and-drop support and progress tracking
 */
public class ModernFileUploader extends VBox {
    
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
    private static final String[] SUPPORTED_EXTENSIONS = {
        "*.txt", "*.pdf", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.ppt", "*.pptx",
        "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.svg",
        "*.mp3", "*.wav", "*.aac", "*.flac", "*.ogg", "*.m4a",
        "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv", "*.flv",
        "*.zip", "*.rar", "*.7z", "*.tar", "*.gz",
        "*.java", "*.py", "*.cpp", "*.js", "*.html", "*.css", "*.xml", "*.json"
    };
    
    // UI Components
    private final VBox dropZone;
    private final Button browseButton;
    private final Label statusLabel;
    private final ProgressBar uploadProgress;
    private final VBox uploadQueue;
    private final ScrollPane queueScrollPane;
    
    // Callbacks
    private Consumer<File> onFileSelected;
    private Consumer<List<File>> onFilesSelected;
    private Consumer<UploadResult> onUploadComplete;
    private Consumer<String> onError;
    
    // State
    private boolean isUploading = false;
    private Stage parentStage;
    
    public ModernFileUploader(Stage parentStage) {
        super(10);
        this.parentStage = parentStage;
        setPadding(new Insets(20));
        getStyleClass().add("professional-panel");
        
        // Create drop zone
        dropZone = createDropZone();
        
        // Create browse button
        browseButton = createBrowseButton();
        
        // Create status section
        HBox statusSection = createStatusSection();
        
        // Create upload queue
        uploadQueue = new VBox(5);
        uploadQueue.setPadding(new Insets(10));
        
        queueScrollPane = new ScrollPane(uploadQueue);
        queueScrollPane.setFitToWidth(true);
        queueScrollPane.setPrefHeight(200);
        queueScrollPane.setVisible(false);
        queueScrollPane.getStyleClass().add("upload-queue-scroll");
        
        VBox.setVgrow(queueScrollPane, Priority.ALWAYS);
        
        getChildren().addAll(dropZone, browseButton, statusSection, queueScrollPane);
        
        // Setup drag and drop
        setupDragAndDrop();
        
        // Apply styling
        setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1px; -fx-border-radius: 8px;");
    }
    
    /**
     * Create the drag and drop zone
     */
    private VBox createDropZone() {
        VBox zone = new VBox(15);
        zone.setAlignment(Pos.CENTER);
        zone.setPadding(new Insets(40));
        zone.setMinHeight(200);
        zone.getStyleClass().add("drop-zone");
        
        // Drop icon
        Label dropIcon = new Label("📁");
        dropIcon.setStyle("-fx-font-size: 48px;");
        
        // Main message
        Label mainMessage = new Label("Drag & Drop Files Here");
        mainMessage.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #ffffff;
            """);
        
        // Sub message
        Label subMessage = new Label("or click browse to select files");
        subMessage.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #cccccc;
            """);
        
        // File info
        Label fileInfo = new Label("Supports documents, images, audio, video, and code files");
        fileInfo.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #888888;
            -fx-font-style: italic;
            """);
        
        // Size limit info
        Label sizeInfo = new Label("Maximum file size: 100MB");
        sizeInfo.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #666666;
            """);
        
        zone.getChildren().addAll(dropIcon, mainMessage, subMessage, fileInfo, sizeInfo);
        
        // Styling for drop zone states
        zone.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.02);
            -fx-border-color: #505050;
            -fx-border-width: 2px;
            -fx-border-style: dashed;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);
        
        return zone;
    }
    
    /**
     * Create the browse button
     */
    private Button createBrowseButton() {
        Button button = new Button("📂 Browse Files");
        button.getStyleClass().addAll("professional-button", "button-primary", "button-large");
        button.setPrefWidth(200);
        
        button.setOnAction(e -> openFileChooser());
        
        return button;
    }
    
    /**
     * Create status section
     */
    private HBox createStatusSection() {
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10, 0, 0, 0));
        
        statusLabel = new Label("Ready to upload files");
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        
        uploadProgress = new ProgressBar();
        uploadProgress.setPrefWidth(200);
        uploadProgress.setVisible(false);
        uploadProgress.getStyleClass().add("upload-progress");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBox.getChildren().addAll(statusLabel, spacer, uploadProgress);
        
        return statusBox;
    }
    
    /**
     * Setup drag and drop functionality
     */
    private void setupDragAndDrop() {
        // Drag over
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                
                // Visual feedback
                dropZone.setStyle("""
                    -fx-background-color: rgba(0, 120, 212, 0.1);
                    -fx-border-color: #0078d4;
                    -fx-border-width: 2px;
                    -fx-border-style: dashed;
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                    """);
            }
            event.consume();
        });
        
        // Drag exited
        dropZone.setOnDragExited(event -> {
            // Reset visual feedback
            dropZone.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.02);
                -fx-border-color: #505050;
                -fx-border-width: 2px;
                -fx-border-style: dashed;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
                """);
            event.consume();
        });
        
        // Drag dropped
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                handleFileSelection(files);
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
            
            // Reset visual feedback
            dropZone.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.02);
                -fx-border-color: #505050;
                -fx-border-width: 2px;
                -fx-border-style: dashed;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
                """);
        });
    }
    
    /**
     * Open file chooser dialog
     */
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Upload");
        
        // Set extension filters
        FileChooser.ExtensionFilter allSupported = new FileChooser.ExtensionFilter(
            "All Supported Files", SUPPORTED_EXTENSIONS
        );
        
        FileChooser.ExtensionFilter documents = new FileChooser.ExtensionFilter(
            "Documents", "*.txt", "*.pdf", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.ppt", "*.pptx"
        );
        
        FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter(
            "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.svg"
        );
        
        FileChooser.ExtensionFilter audio = new FileChooser.ExtensionFilter(
            "Audio", "*.mp3", "*.wav", "*.aac", "*.flac", "*.ogg", "*.m4a"
        );
        
        FileChooser.ExtensionFilter video = new FileChooser.ExtensionFilter(
            "Video", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv", "*.flv"
        );
        
        FileChooser.ExtensionFilter code = new FileChooser.ExtensionFilter(
            "Code Files", "*.java", "*.py", "*.cpp", "*.js", "*.html", "*.css", "*.xml", "*.json"
        );
        
        FileChooser.ExtensionFilter archives = new FileChooser.ExtensionFilter(
            "Archives", "*.zip", "*.rar", "*.7z", "*.tar", "*.gz"
        );
        
        FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter(
            "All Files", "*.*"
        );
        
        fileChooser.getExtensionFilters().addAll(
            allSupported, documents, images, audio, video, code, archives, allFiles
        );
        
        fileChooser.setSelectedExtensionFilter(allSupported);
        
        // Show dialog
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(parentStage);
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            handleFileSelection(selectedFiles);
        }
    }
    
    /**
     * Handle file selection from drag-drop or file chooser
     */
    private void handleFileSelection(List<File> files) {
        if (isUploading) {
            showStatus("Upload in progress. Please wait...", false);
            return;
        }
        
        // Validate files
        List<File> validFiles = validateFiles(files);
        
        if (validFiles.isEmpty()) {
            showStatus("No valid files selected", false);
            return;
        }
        
        // Show upload queue
        displayUploadQueue(validFiles);
        
        // Trigger callbacks
        if (onFilesSelected != null) {
            onFilesSelected.accept(validFiles);
        }
        
        if (validFiles.size() == 1 && onFileSelected != null) {
            onFileSelected.accept(validFiles.get(0));
        }
        
        // Start upload process
        startUpload(validFiles);
    }
    
    /**
     * Validate selected files
     */
    private List<File> validateFiles(List<File> files) {
        List<File> validFiles = new java.util.ArrayList<>();
        
        for (File file : files) {
            if (!file.exists()) {
                showError("File does not exist: " + file.getName());
                continue;
            }
            
            if (!file.isFile()) {
                showError("Not a file: " + file.getName());
                continue;
            }
            
            if (file.length() > MAX_FILE_SIZE) {
                showError("File too large (max 100MB): " + file.getName());
                continue;
            }
            
            if (file.length() == 0) {
                showError("Empty file: " + file.getName());
                continue;
            }
            
            validFiles.add(file);
        }
        
        return validFiles;
    }
    
    /**
     * Display upload queue
     */
    private void displayUploadQueue(List<File> files) {
        uploadQueue.getChildren().clear();
        
        Label queueHeader = new Label("Upload Queue (" + files.size() + " files)");
        queueHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");
        uploadQueue.getChildren().add(queueHeader);
        
        for (File file : files) {
            HBox fileItem = createFileQueueItem(file);
            uploadQueue.getChildren().add(fileItem);
        }
        
        queueScrollPane.setVisible(true);
    }
    
    /**
     * Create file queue item
     */
    private HBox createFileQueueItem(File file) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5));
        item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-radius: 4px; -fx-background-radius: 4px;");
        
        // File icon
        Label icon = new Label(getFileIcon(file.getName()));
        icon.setStyle("-fx-font-size: 16px;");
        
        // File info
        VBox fileInfo = new VBox(2);
        
        Label fileName = new Label(file.getName());
        fileName.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 500;");
        
        Label fileSize = new Label(formatFileSize(file.length()));
        fileSize.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        
        fileInfo.getChildren().addAll(fileName, fileSize);
        
        // Progress indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(20, 20);
        progress.setVisible(false);
        
        // Status label
        Label status = new Label("Pending");
        status.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(icon, fileInfo, spacer, progress, status);
        
        return item;
    }
    
    /**
     * Start upload process
     */
    private void startUpload(List<File> files) {
        isUploading = true;
        browseButton.setDisable(true);
        uploadProgress.setVisible(true);
        uploadProgress.setProgress(0);
        
        showStatus("Uploading " + files.size() + " file(s)...", true);
        
        Task<Void> uploadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int totalFiles = files.size();
                int completedFiles = 0;
                
                for (File file : files) {
                    try {
                        // Update UI for current file
                        Platform.runLater(() -> {
                            updateFileStatus(file, "Uploading...", true);
                        });
                        
                        // Simulate upload process (replace with actual upload logic)
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        
                        // Simulate processing time
                        Thread.sleep(1000 + (file.length() / 1024)); // Simulate based on file size
                        
                        // Create upload result
                        UploadResult result = new UploadResult(file, true, "Upload successful", fileData);
                        
                        Platform.runLater(() -> {
                            updateFileStatus(file, "Completed", false);
                            if (onUploadComplete != null) {
                                onUploadComplete.accept(result);
                            }
                        });
                        
                        completedFiles++;
                        final double progress = (double) completedFiles / totalFiles;
                        
                        Platform.runLater(() -> {
                            uploadProgress.setProgress(progress);
                        });
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            updateFileStatus(file, "Failed: " + e.getMessage(), false);
                            if (onError != null) {
                                onError.accept("Failed to upload " + file.getName() + ": " + e.getMessage());
                            }
                        });
                    }
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    isUploading = false;
                    browseButton.setDisable(false);
                    uploadProgress.setVisible(false);
                    showStatus("Upload completed successfully", true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    isUploading = false;
                    browseButton.setDisable(false);
                    uploadProgress.setVisible(false);
                    showStatus("Upload failed", false);
                });
            }
        };
        
        Thread uploadThread = new Thread(uploadTask);
        uploadThread.setDaemon(true);
        uploadThread.start();
    }
    
    /**
     * Update file status in queue
     */
    private void updateFileStatus(File file, String status, boolean showProgress) {
        for (javafx.scene.Node node : uploadQueue.getChildren()) {
            if (node instanceof HBox) {
                HBox item = (HBox) node;
                VBox fileInfo = (VBox) item.getChildren().get(1);
                Label fileName = (Label) fileInfo.getChildren().get(0);
                
                if (fileName.getText().equals(file.getName())) {
                    ProgressIndicator progress = (ProgressIndicator) item.getChildren().get(3);
                    Label statusLabel = (Label) item.getChildren().get(4);
                    
                    progress.setVisible(showProgress);
                    statusLabel.setText(status);
                    
                    if (status.startsWith("Failed")) {
                        statusLabel.setStyle("-fx-text-fill: #d13438; -fx-font-size: 11px;");
                    } else if (status.equals("Completed")) {
                        statusLabel.setStyle("-fx-text-fill: #107c10; -fx-font-size: 11px;");
                    } else {
                        statusLabel.setStyle("-fx-text-fill: #0078d4; -fx-font-size: 11px;");
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        if (isSuccess) {
            statusLabel.setStyle("-fx-text-fill: #107c10; -fx-font-size: 13px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #d13438; -fx-font-size: 13px;");
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        showStatus("Error: " + message, false);
        if (onError != null) {
            onError.accept(message);
        }
    }
    
    /**
     * Get file icon based on extension
     */
    private String getFileIcon(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        if (extension.matches("\\.(jpg|jpeg|png|gif|bmp|tiff|svg)")) return "🖼️";
        if (extension.matches("\\.(mp4|avi|mkv|mov|wmv|flv)")) return "🎬";
        if (extension.matches("\\.(mp3|wav|aac|flac|ogg|m4a)")) return "🎵";
        if (extension.matches("\\.(java|py|cpp|js|html|css|xml|json)")) return "💻";
        if (extension.matches("\\.(pdf|doc|docx|txt)")) return "📄";
        if (extension.matches("\\.(zip|rar|7z|tar|gz)")) return "📦";
        if (extension.matches("\\.(xls|xlsx)")) return "📊";
        if (extension.matches("\\.(ppt|pptx)")) return "📋";
        
        return "📄";
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot);
    }
    
    /**
     * Format file size
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Callback setters
    public void setOnFileSelected(Consumer<File> callback) {
        this.onFileSelected = callback;
    }
    
    public void setOnFilesSelected(Consumer<List<File>> callback) {
        this.onFilesSelected = callback;
    }
    
    public void setOnUploadComplete(Consumer<UploadResult> callback) {
        this.onUploadComplete = callback;
    }
    
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }
    
    /**
     * Clear upload queue
     */
    public void clearQueue() {
        uploadQueue.getChildren().clear();
        queueScrollPane.setVisible(false);
        showStatus("Ready to upload files", true);
    }
    
    /**
     * Upload result data class
     */
    public static class UploadResult {
        private final File file;
        private final boolean success;
        private final String message;
        private final byte[] data;
        
        public UploadResult(File file, boolean success, String message, byte[] data) {
            this.file = file;
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public File getFile() { return file; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public byte[] getData() { return data; }
    }
}