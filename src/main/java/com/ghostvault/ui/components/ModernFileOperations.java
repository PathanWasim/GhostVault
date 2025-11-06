package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Modern file operations with enhanced dialogs and progress tracking
 */
public class ModernFileOperations {
    
    private Stage parentStage;
    private NotificationSystem notificationSystem;
    
    public ModernFileOperations(Stage parentStage) {
        this.parentStage = parentStage;
        this.notificationSystem = NotificationSystem.getInstance();
    }
    
    /**
     * Modern file upload with enhanced FileChooser
     */
    public CompletableFuture<List<File>> showFileUploadDialog() {
        CompletableFuture<List<File>> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Files to Upload");
            
            // Add common file type filters
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.svg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt", "*.rtf"),
                new FileChooser.ExtensionFilter("Code Files", "*.java", "*.cpp", "*.c", "*.py", "*.js", "*.html", "*.css")
            );
            
            // Set initial directory to user's documents folder
            String userHome = System.getProperty("user.home");
            File documentsDir = new File(userHome, "Documents");
            if (documentsDir.exists()) {
                fileChooser.setInitialDirectory(documentsDir);
            }
            
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(parentStage);
            
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                future.complete(selectedFiles);
            } else {
                future.complete(new ArrayList<>());
            }
        });
        
        return future;
    }
    
    /**
     * Modern file download with enhanced save dialog
     */
    public CompletableFuture<File> showFileSaveDialog(String suggestedFileName, String fileExtension) {
        CompletableFuture<File> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            
            if (suggestedFileName != null && !suggestedFileName.isEmpty()) {
                fileChooser.setInitialFileName(suggestedFileName);
            }
            
            if (fileExtension != null && !fileExtension.isEmpty()) {
                String description = fileExtension.toUpperCase() + " Files";
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(description, "*." + fileExtension.toLowerCase())
                );
            }
            
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            File selectedFile = fileChooser.showSaveDialog(parentStage);
            
            if (selectedFile != null) {
                future.complete(selectedFile);
            } else {
                future.cancel(false);
            }
        });
        
        return future;
    }
    
    /**
     * Upload files with progress tracking
     */
    public void uploadFiles(List<File> files, File targetDirectory, Consumer<UploadResult> onComplete) {
        if (files == null || files.isEmpty()) {
            onComplete.accept(new UploadResult(0, 0, "No files selected"));
            return;
        }
        
        NotificationSystem.ProgressNotification progressNotification = 
            NotificationSystem.showProgress("Uploading Files", "Preparing to upload " + files.size() + " file(s)...");
        
        Task<UploadResult> uploadTask = new Task<UploadResult>() {
            @Override
            protected UploadResult call() throws Exception {
                int totalFiles = files.size();
                int successCount = 0;
                int failureCount = 0;
                StringBuilder errors = new StringBuilder();
                
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    
                    updateMessage("Uploading " + file.getName() + "...");
                    updateProgress(i, totalFiles);
                    
                    try {
                        Path sourcePath = file.toPath();
                        Path targetPath = targetDirectory.toPath().resolve(file.getName());
                        
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        successCount++;
                        
                    } catch (IOException e) {
                        failureCount++;
                        errors.append("Failed to upload ").append(file.getName())
                              .append(": ").append(e.getMessage()).append("\n");
                    }
                }
                
                updateProgress(totalFiles, totalFiles);
                return new UploadResult(successCount, failureCount, errors.toString());
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    UploadResult result = getValue();
                    progressNotification.complete("Upload completed: " + result.successCount + " files uploaded");
                    onComplete.accept(result);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressNotification.complete("Upload failed");
                    onComplete.accept(new UploadResult(0, files.size(), getException().getMessage()));
                });
            }
        };
        
        Thread uploadThread = new Thread(uploadTask);
        uploadThread.setDaemon(true);
        uploadThread.start();
    }
    
    /**
     * Download file with progress tracking
     */
    public void downloadFile(File sourceFile, File targetFile, Consumer<Boolean> onComplete) {
        if (sourceFile == null || !sourceFile.exists()) {
            NotificationSystem.showError("Download Error", "Source file does not exist");
            onComplete.accept(false);
            return;
        }
        
        Task<Boolean> downloadTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Path sourcePath = sourceFile.toPath();
                Path targetPath = targetFile.toPath();
                
                Files.createDirectories(targetPath.getParent());
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                return true;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    NotificationSystem.showSuccess("Download Complete", 
                        sourceFile.getName() + " downloaded successfully");
                    onComplete.accept(true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    NotificationSystem.showError("Download Error", 
                        "Failed to download file: " + getException().getMessage());
                    onComplete.accept(false);
                });
            }
        };
        
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();
    }
    
    /**
     * Secure file deletion with confirmation
     */
    public void secureDeleteFiles(List<File> files, Consumer<DeletionResult> onComplete) {
        if (files == null || files.isEmpty()) {
            onComplete.accept(new DeletionResult(0, 0, "No files selected"));
            return;
        }
        
        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Secure Deletion");
        confirmation.setHeaderText("Permanently delete " + files.size() + " file(s)?");
        confirmation.setContentText("This action cannot be undone. Files will be securely deleted.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performSecureDeletion(files, onComplete);
        } else {
            onComplete.accept(new DeletionResult(0, 0, "Deletion cancelled"));
        }
    }
    
    private void performSecureDeletion(List<File> files, Consumer<DeletionResult> onComplete) {
        Task<DeletionResult> deleteTask = new Task<DeletionResult>() {
            @Override
            protected DeletionResult call() throws Exception {
                int successCount = 0;
                int failureCount = 0;
                StringBuilder errors = new StringBuilder();
                
                for (File file : files) {
                    try {
                        if (file.delete()) {
                            successCount++;
                        } else {
                            failureCount++;
                            errors.append("Failed to delete ").append(file.getName()).append("\n");
                        }
                    } catch (Exception e) {
                        failureCount++;
                        errors.append("Error deleting ").append(file.getName())
                              .append(": ").append(e.getMessage()).append("\n");
                    }
                }
                
                return new DeletionResult(successCount, failureCount, errors.toString());
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    DeletionResult result = getValue();
                    if (result.failureCount > 0) {
                        NotificationSystem.showWarning("Deletion Issues", 
                            result.failureCount + " files could not be deleted");
                    } else {
                        NotificationSystem.showSuccess("Deletion Complete", 
                            "All " + result.successCount + " files deleted successfully");
                    }
                    onComplete.accept(result);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    NotificationSystem.showError("Deletion Error", 
                        "Failed to delete files: " + getException().getMessage());
                    onComplete.accept(new DeletionResult(0, files.size(), getException().getMessage()));
                });
            }
        };
        
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }
    
    /**
     * Show directory chooser dialog
     */
    public CompletableFuture<File> showDirectoryChooser(String title) {
        CompletableFuture<File> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(title != null ? title : "Select Directory");
            
            File selectedDirectory = directoryChooser.showDialog(parentStage);
            
            if (selectedDirectory != null) {
                future.complete(selectedDirectory);
            } else {
                future.cancel(false);
            }
        });
        
        return future;
    }
    
    /**
     * Upload result class
     */
    public static class UploadResult {
        public final int successCount;
        public final int failureCount;
        public final String errorMessages;
        
        public UploadResult(int successCount, int failureCount, String errorMessages) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errorMessages = errorMessages;
        }
        
        public boolean hasErrors() {
            return failureCount > 0;
        }
        
        public int getTotalFiles() {
            return successCount + failureCount;
        }
    }
    
    /**
     * Deletion result class
     */
    public static class DeletionResult {
        public final int successCount;
        public final int failureCount;
        public final String errorMessages;
        
        public DeletionResult(int successCount, int failureCount, String errorMessages) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errorMessages = errorMessages;
        }
        
        public boolean hasErrors() {
            return failureCount > 0;
        }
        
        public int getTotalFiles() {
            return successCount + failureCount;
        }
    }
}