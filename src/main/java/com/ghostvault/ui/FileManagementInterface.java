package com.ghostvault.ui;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Comprehensive file management interface for the real vault
 */
public class FileManagementInterface {
    
    private Stage primaryStage;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private SecretKey encryptionKey;
    
    private ListView<String> fileListView;
    private ObservableList<String> fileList;
    private TextField searchField;
    private Label statusLabel;
    private Label statsLabel;
    private ProgressBar operationProgress;
    private Label progressLabel;
    
    public FileManagementInterface(Stage primaryStage, FileManager fileManager, 
                                 MetadataManager metadataManager, SecretKey encryptionKey) {
        this.primaryStage = primaryStage;
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.encryptionKey = encryptionKey;
        this.fileList = FXCollections.observableArrayList();
        
        refreshFileList();
    }
    
    /**
     * Create and show the file management interface
     */
    public Scene createFileManagementScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Title bar
        HBox titleBar = createTitleBar();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Progress section
        VBox progressSection = createProgressSection();
        
        // File list section
        VBox listSection = createFileListSection();
        
        // Status bar
        HBox statusBar = createStatusBar();
        
        root.getChildren().addAll(titleBar, toolbar, progressSection, listSection, statusBar);
        
        return new Scene(root, 800, 600);
    }
    
    /**
     * Create title bar
     */
    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("GhostVault - File Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        titleBar.getChildren().add(titleLabel);
        return titleBar;
    }
    
    /**
     * Create toolbar with action buttons
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button uploadButton = new Button("Upload File");
        Button downloadButton = new Button("Download File");
        Button deleteButton = new Button("Delete File");
        Button refreshButton = new Button("Refresh");
        
        uploadButton.setOnAction(e -> uploadFile());
        downloadButton.setOnAction(e -> downloadFile());
        deleteButton.setOnAction(e -> deleteFile());
        refreshButton.setOnAction(e -> refreshFileList());
        
        toolbar.getChildren().addAll(uploadButton, downloadButton, deleteButton, refreshButton);
        return toolbar;
    }
    
    /**
     * Create progress section
     */
    private VBox createProgressSection() {
        VBox progressSection = new VBox(5);
        
        operationProgress = new ProgressBar();
        operationProgress.setPrefWidth(400);
        operationProgress.setVisible(false);
        
        progressLabel = new Label();
        progressLabel.setVisible(false);
        
        progressSection.getChildren().addAll(operationProgress, progressLabel);
        return progressSection;
    }
    
    /**
     * Create file list section
     */
    private VBox createFileListSection() {
        VBox listSection = new VBox(10);
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search files...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterFiles(newVal));
        
        // File list
        fileListView = new ListView<>(fileList);
        fileListView.setPrefHeight(300);
        
        Label listLabel = new Label("Encrypted Files:");
        listLabel.setStyle("-fx-font-weight: bold;");
        
        listSection.getChildren().addAll(listLabel, searchField, fileListView);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        
        return listSection;
    }
    
    /**
     * Create status bar
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        statsLabel = new Label("Files: 0");
        
        statusBar.getChildren().addAll(statusLabel, statsLabel);
        return statusBar;
    }
    
    /**
     * Refresh the file list
     */
    private void refreshFileList() {
        Platform.runLater(() -> {
            fileList.clear();
            // TODO: Load actual files from vault
            fileList.addAll("sample_document.pdf", "important_notes.txt", "financial_data.xlsx");
            updateStats();
            statusLabel.setText("File list refreshed at " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    
    /**
     * Filter files based on search term
     */
    private void filterFiles(String searchTerm) {
        // TODO: Implement actual file filtering
        statusLabel.setText("Filtering files: " + searchTerm);
    }
    
    /**
     * Upload a file to the vault
     */
    private void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        File file = fileChooser.showOpenDialog(primaryStage);
        
        if (file != null) {
            showProgress("Uploading: " + file.getName());
            
            Task<Void> uploadTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // TODO: Implement actual file upload
                    Thread.sleep(2000); // Simulate upload
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    hideProgress();
                    statusLabel.setText("File uploaded successfully: " + file.getName());
                    refreshFileList();
                }
                
                @Override
                protected void failed() {
                    hideProgress();
                    statusLabel.setText("Upload failed: " + getException().getMessage());
                }
            };
            
            new Thread(uploadTask).start();
        }
    }
    
    /**
     * Download a file from the vault
     */
    private void downloadFile() {
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            statusLabel.setText("Please select a file to download");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.setInitialFileName(selectedFile);
        File saveFile = fileChooser.showSaveDialog(primaryStage);
        
        if (saveFile != null) {
            showProgress("Downloading: " + selectedFile);
            
            Task<Void> downloadTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // TODO: Implement actual file download
                    Thread.sleep(2000); // Simulate download
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    hideProgress();
                    statusLabel.setText("File downloaded successfully: " + selectedFile);
                }
                
                @Override
                protected void failed() {
                    hideProgress();
                    statusLabel.setText("Download failed: " + getException().getMessage());
                }
            };
            
            new Thread(downloadTask).start();
        }
    }
    
    /**
     * Delete a file from the vault
     */
    private void deleteFile() {
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            statusLabel.setText("Please select a file to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Secure Delete");
        confirmAlert.setHeaderText("Permanently delete this file?");
        confirmAlert.setContentText("File: " + selectedFile + 
            "\n\nThis action cannot be undone. The file will be securely overwritten.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showProgress("Securely deleting: " + selectedFile);
            
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // TODO: Implement actual secure file deletion
                    Thread.sleep(1500); // Simulate secure deletion
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    hideProgress();
                    statusLabel.setText("File securely deleted: " + selectedFile);
                    refreshFileList();
                }
                
                @Override
                protected void failed() {
                    hideProgress();
                    statusLabel.setText("Delete failed: " + getException().getMessage());
                }
            };
            
            new Thread(deleteTask).start();
        }
    }
    
    /**
     * Show progress indicator
     */
    private void showProgress(String message) {
        Platform.runLater(() -> {
            progressLabel.setText(message);
            progressLabel.setVisible(true);
            operationProgress.setVisible(true);
        });
    }
    
    /**
     * Hide progress indicator
     */
    private void hideProgress() {
        Platform.runLater(() -> {
            progressLabel.setVisible(false);
            operationProgress.setVisible(false);
        });
    }
    
    /**
     * Update statistics display
     */
    private void updateStats() {
        Platform.runLater(() -> {
            statsLabel.setText("Files: " + fileList.size());
        });
    }
}