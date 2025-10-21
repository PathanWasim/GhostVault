package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Enhanced file manager component
 */
public class EnhancedFileManager extends VBox {
    
    private File currentDirectory;
    private EnhancedFileListView fileListView;
    private FileSortingToolbar sortingToolbar;
    private BulkOperationsBar bulkOperationsBar;
    private DragDropFileUploader dragDropUploader;
    private TextField pathField;
    private Button backButton;
    private Button forwardButton;
    private Button upButton;
    private Button refreshButton;
    private Consumer<File> onFileSelected;
    private Consumer<File> onDirectoryChanged;
    
    public EnhancedFileManager() {
        super();
        initialize();
    }
    
    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        
        createToolbar();
        createSortingToolbar();
        createBulkOperationsBar();
        createFileList();
        setupDragAndDrop();
        setupEventHandlers();
        
        // Set initial directory to user home
        setCurrentDirectory(new File(System.getProperty("user.home")));
    }
    
    private void createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5));
        
        // Navigation buttons
        backButton = new Button("←");
        backButton.setDisable(true);
        
        forwardButton = new Button("→");
        forwardButton.setDisable(true);
        
        upButton = new Button("↑");
        
        refreshButton = new Button("⟳");
        
        // Path field
        pathField = new TextField();
        pathField.setEditable(false);
        HBox.setHgrow(pathField, Priority.ALWAYS);
        
        // Browse button
        Button browseButton = new Button("Browse...");
        
        toolbar.getChildren().addAll(
            backButton, forwardButton, upButton, refreshButton,
            new Separator(), pathField, browseButton
        );
        
        this.getChildren().add(toolbar);
    }
    
    private void createSortingToolbar() {
        sortingToolbar = new FileSortingToolbar();
        this.getChildren().add(sortingToolbar);
    }
    
    private void createBulkOperationsBar() {
        bulkOperationsBar = new BulkOperationsBar();
        this.getChildren().add(bulkOperationsBar);
    }
    
    private void createFileList() {
        fileListView = new EnhancedFileListView();
        fileListView.setPrefHeight(400);
        
        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(fileListView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        this.getChildren().add(scrollPane);
    }
    
    private void setupDragAndDrop() {
        dragDropUploader = new DragDropFileUploader();
        
        // Enable drag and drop on the entire file manager
        dragDropUploader.enableDragAndDrop(this);
        
        // Handle file drops
        dragDropUploader.setOnFilesDropped(files -> {
            handleFileUpload(files);
        });
        
        // Visual feedback for drag operations
        dragDropUploader.setOnDragEntered(event -> {
            this.setStyle(this.getStyle() + "; -fx-background-color: rgba(76, 175, 80, 0.1);");
        });
        
        dragDropUploader.setOnDragExited(event -> {
            this.setStyle(this.getStyle().replace("; -fx-background-color: rgba(76, 175, 80, 0.1);", ""));
        });
    }
    
    private void setupEventHandlers() {
        // Up button
        upButton.setOnAction(e -> navigateUp());
        
        // Refresh button
        refreshButton.setOnAction(e -> refreshCurrentDirectory());
        
        // File selection
        fileListView.setOnFileSelected(file -> {
            if (onFileSelected != null) {
                onFileSelected.accept(file);
            }
        });
        
        // File double click
        fileListView.setOnFileDoubleClicked(file -> {
            if (file.isDirectory()) {
                setCurrentDirectory(file);
            } else {
                // Open file
                if (onFileSelected != null) {
                    onFileSelected.accept(file);
                }
            }
        });
        
        // Context menu actions
        FileContextMenuManager contextManager = fileListView.getContextMenuManager();
        
        contextManager.setOnOpenFile(file -> {
            if (file.isDirectory()) {
                setCurrentDirectory(file);
            } else {
                // Open file with default application
                try {
                    java.awt.Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    NotificationSystem.showError("Open Failed", "Could not open file: " + e.getMessage());
                }
            }
        });
        
        contextManager.setOnPreviewFile(file -> {
            NotificationSystem.showInfo("Preview", "Preview functionality not yet implemented");
        });
        
        contextManager.setOnEditFile(file -> {
            NotificationSystem.showInfo("Edit", "Edit functionality not yet implemented");
        });
        
        contextManager.setOnDeleteFile(file -> {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete File");
            alert.setHeaderText("Are you sure you want to delete this file?");
            alert.setContentText(file.getName());
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (file.delete()) {
                        refreshCurrentDirectory();
                        NotificationSystem.showSuccess("File Deleted", "File deleted successfully");
                    } else {
                        NotificationSystem.showError("Delete Failed", "Could not delete file");
                    }
                }
            });
        });
        
        contextManager.setOnRenameFile(file -> {
            showRenameDialog(file);
        });
        
        contextManager.setOnCopyFile(file -> {
            NotificationSystem.showInfo("Copy", "Copy functionality not yet implemented");
        });
        
        contextManager.setOnMoveFile(file -> {
            NotificationSystem.showInfo("Move", "Move functionality not yet implemented");
        });
        
        contextManager.setOnShowProperties(file -> {
            showFilePropertiesDialog(file);
        });
        
        contextManager.setOnShowInExplorer(file -> {
            try {
                java.awt.Desktop.getDesktop().open(file.getParentFile());
            } catch (Exception e) {
                NotificationSystem.showError("Explorer Failed", "Could not open file location");
            }
        });
        
        contextManager.setOnCompressFile(file -> {
            NotificationSystem.showInfo("Compress", "Compression functionality not yet implemented");
        });
        
        contextManager.setOnEncryptFile(file -> {
            NotificationSystem.showInfo("Encrypt", "Encryption functionality not yet implemented");
        });
        
        contextManager.setOnShareFile(file -> {
            NotificationSystem.showInfo("Share", "Share functionality not yet implemented");
        });
        
        // Setup sorting toolbar
        sortingToolbar.setOnSortCriteriaChanged(criteria -> {
            fileListView.sortBy(criteria);
            sortingToolbar.updateFromFileList(fileListView);
        });
        
        sortingToolbar.setOnSortOrderToggled(() -> {
            // The file list view handles the toggle internally
            sortingToolbar.updateFromFileList(fileListView);
        });
        
        // Setup bulk operations bar
        bulkOperationsBar.setOnSelectAll(v -> fileListView.selectAll());
        bulkOperationsBar.setOnSelectNone(v -> fileListView.selectNone());
        
        bulkOperationsBar.setOnDeleteSelected(files -> {
            deleteMultipleFiles(files);
        });
        
        bulkOperationsBar.setOnCopySelected(files -> {
            NotificationSystem.showInfo("Copy Files", "Copy functionality not yet implemented");
        });
        
        bulkOperationsBar.setOnMoveSelected(files -> {
            NotificationSystem.showInfo("Move Files", "Move functionality not yet implemented");
        });
        
        // Handle selection changes in file list
        fileListView.setOnSelectionChanged(selectedFiles -> {
            bulkOperationsBar.updateSelection(selectedFiles);
        });
    }
    
    /**
     * Set the current directory
     */
    public void setCurrentDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            this.currentDirectory = directory;
            pathField.setText(directory.getAbsolutePath());
            
            // Update file list
            File[] files = directory.listFiles();
            if (files != null) {
                List<File> fileList = Arrays.asList(files);
                fileListView.updateFiles(fileList);
            }
            
            // Update navigation buttons
            upButton.setDisable(directory.getParentFile() == null);
            
            // Notify listeners
            if (onDirectoryChanged != null) {
                onDirectoryChanged.accept(directory);
            }
        }
    }
    
    /**
     * Navigate to parent directory
     */
    public void navigateUp() {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            setCurrentDirectory(currentDirectory.getParentFile());
        }
    }
    
    /**
     * Refresh current directory
     */
    public void refreshCurrentDirectory() {
        if (currentDirectory != null) {
            setCurrentDirectory(currentDirectory);
        }
    }
    
    /**
     * Show directory chooser
     */
    public void showDirectoryChooser(Stage parentStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        
        if (currentDirectory != null) {
            directoryChooser.setInitialDirectory(currentDirectory);
        }
        
        File selectedDirectory = directoryChooser.showDialog(parentStage);
        if (selectedDirectory != null) {
            setCurrentDirectory(selectedDirectory);
        }
    }
    
    // Getters and setters
    public File getCurrentDirectory() {
        return currentDirectory;
    }
    
    public void setOnFileSelected(Consumer<File> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }
    
    public void setOnDirectoryChanged(Consumer<File> onDirectoryChanged) {
        this.onDirectoryChanged = onDirectoryChanged;
    }
    
    public EnhancedFileListView getFileListView() {
        return fileListView;
    }
    
    /**
     * Delete multiple files
     */
    private void deleteMultipleFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (File file : files) {
            if (file.delete()) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        // Clear selection after deletion
        fileListView.selectNone();
        
        // Refresh directory
        refreshCurrentDirectory();
        
        // Show result notification
        if (failCount == 0) {
            NotificationSystem.showSuccess("Files Deleted", 
                String.format("Successfully deleted %d file(s)", successCount));
        } else if (successCount == 0) {
            NotificationSystem.showError("Delete Failed", 
                String.format("Failed to delete %d file(s)", failCount));
        } else {
            NotificationSystem.showWarning("Partial Success", 
                String.format("Deleted %d file(s), failed to delete %d file(s)", successCount, failCount));
        }
    }
    
    /**
     * Get the bulk operations bar
     */
    public BulkOperationsBar getBulkOperationsBar() {
        return bulkOperationsBar;
    }
    
    /**
     * Handle file upload from drag and drop
     */
    private void handleFileUpload(List<File> files) {
        if (currentDirectory == null || files == null || files.isEmpty()) {
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (File sourceFile : files) {
            try {
                File targetFile = new File(currentDirectory, sourceFile.getName());
                
                // Check if file already exists
                if (targetFile.exists()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("File Exists");
                    alert.setHeaderText("File already exists");
                    alert.setContentText("Do you want to replace " + sourceFile.getName() + "?");
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        continue; // Skip this file
                    }
                }
                
                // Copy file to current directory
                if (copyFile(sourceFile, targetFile)) {
                    successCount++;
                } else {
                    failCount++;
                }
                
            } catch (Exception e) {
                failCount++;
            }
        }
        
        // Refresh directory to show new files
        refreshCurrentDirectory();
        
        // Show result notification
        if (failCount == 0) {
            NotificationSystem.showSuccess("Files Uploaded", 
                String.format("Successfully uploaded %d file(s)", successCount));
        } else if (successCount == 0) {
            NotificationSystem.showError("Upload Failed", 
                String.format("Failed to upload %d file(s)", failCount));
        } else {
            NotificationSystem.showWarning("Partial Success", 
                String.format("Uploaded %d file(s), failed to upload %d file(s)", successCount, failCount));
        }
    }
    
    /**
     * Copy a file from source to target location
     */
    private boolean copyFile(File source, File target) {
        try {
            java.nio.file.Files.copy(source.toPath(), target.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the drag and drop uploader
     */
    public DragDropFileUploader getDragDropUploader() {
        return dragDropUploader;
    }
    
    /**
     * Show rename dialog for a file
     */
    private void showRenameDialog(File file) {
        TextInputDialog dialog = new TextInputDialog(file.getName());
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Rename " + (file.isDirectory() ? "folder" : "file"));
        dialog.setContentText("New name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(file.getName())) {
                File newFile = new File(file.getParent(), newName.trim());
                if (file.renameTo(newFile)) {
                    refreshCurrentDirectory();
                    NotificationSystem.showSuccess("Renamed", "Successfully renamed to " + newName);
                } else {
                    NotificationSystem.showError("Rename Failed", "Could not rename file");
                }
            }
        });
    }
    
    /**
     * Show file properties dialog
     */
    private void showFilePropertiesDialog(File file) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Properties");
        dialog.setHeaderText(file.getName());
        
        // Create detailed file info pane
        DetailedFileInfoPane infoPane = new DetailedFileInfoPane();
        infoPane.showFileInfo(file);
        
        dialog.getDialogPane().setContent(infoPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Load directory contents
     */
    public void loadDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            currentDirectory = directory;
            refreshCurrentDirectory();
        }
    }
    
    /**
     * Get selected files
     */
    public java.util.List<File> getSelectedFiles() {
        if (fileListView != null) {
            return fileListView.getSelectionModel().getSelectedItems();
        }
        return new java.util.ArrayList<>();
    }
}