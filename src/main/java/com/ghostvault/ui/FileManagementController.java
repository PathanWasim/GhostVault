package com.ghostvault.ui;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive file management interface with upload, download, search, and deletion
 */
public class FileManagementController {
    
    private Stage primaryStage;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private SecretKey encryptionKey;
    
    // UI Components
    private TableView<VaultFile> fileTable;
    private TextField searchField;
    private Label statusLabel;
    private Label vaultStatsLabel;
    private ProgressBar operationProgress;
    private Button uploadButton;
    private Button downloadButton;
    private Button deleteButton;
    private Button refreshButton;
    
    public FileManagementController(Stage primaryStage, FileManager fileManager, 
                                  MetadataManager metadataManager, SecretKey encryptionKey) {
        this.primaryStage = primaryStage;
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.encryptionKey = encryptionKey;
    }
    
    /**
     * Show the file management interface
     */
    public void showFileManagement() {
        Stage fileStage = new Stage();
        fileStage.initModality(Modality.NONE);
        fileStage.initOwner(primaryStage);
        fileStage.setTitle("GhostVault - File Management");
        
        VBox root = createFileManagementUI();
        Scene scene = new Scene(root, 1000, 700);
        fileStage.setScene(scene);
        
        // Load initial data
        refreshFileList();
        updateVaultStats();
        
        fileStage.show();
    }
    
    /**
     * Create the file management UI
     */
    private VBox createFileManagementUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Header
        HBox header = createHeader();
        
        // Search and filter section
        HBox searchSection = createSearchSection();
        
        // File table
        VBox tableSection = createTableSection();
        
        // Action buttons
        HBox buttonSection = createButtonSection();
        
        // Status and progress section
        VBox statusSection = createStatusSection();
        
        root.getChildren().addAll(header, searchSection, tableSection, buttonSection, statusSection);
        
        return root;
    }
    
    /**
     * Create header section
     */
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("File Management");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        vaultStatsLabel = new Label("Loading vault statistics...");
        vaultStatsLabel.setFont(Font.font("System", 12));
        vaultStatsLabel.setTextFill(Color.GRAY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshFileList());
        
        header.getChildren().addAll(titleLabel, vaultStatsLabel, spacer, refreshButton);
        
        return header;
    }
    
    /**
     * Create search section
     */
    private HBox createSearchSection() {
        HBox searchSection = new HBox(10);
        searchSection.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search files by name or tags...");
        searchField.setPrefWidth(300);
        
        // Real-time search
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            performSearch(newText);
        });
        
        Button clearSearchButton = new Button("Clear");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            refreshFileList();
        });
        
        searchSection.getChildren().addAll(searchLabel, searchField, clearSearchButton);
        
        return searchSection;
    }
    
    /**
     * Create table section
     */
    private VBox createTableSection() {
        VBox tableSection = new VBox(5);
        
        Label tableLabel = new Label("Vault Files");
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        fileTable = new TableView<>();
        fileTable.setPrefHeight(400);
        
        // Create columns
        TableColumn<VaultFile, String> iconColumn = new TableColumn<>("");
        iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
        iconColumn.setPrefWidth(40);
        iconColumn.setResizable(false);
        
        TableColumn<VaultFile, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("originalName"));
        nameColumn.setPrefWidth(300);
        
        TableColumn<VaultFile, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(cellData -> {
            VaultFile file = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(FileUtils.formatFileSize(file.getSize()));
        });
        sizeColumn.setPrefWidth(100);
        
        TableColumn<VaultFile, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        typeColumn.setPrefWidth(80);
        
        TableColumn<VaultFile, String> dateColumn = new TableColumn<>("Upload Date");
        dateColumn.setCellValueFactory(cellData -> {
            VaultFile file = cellData.getValue();
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.getUploadTime()), 
                ZoneId.systemDefault()
            );
            return new javafx.beans.property.SimpleStringProperty(
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
        });
        dateColumn.setPrefWidth(150);
        
        TableColumn<VaultFile, String> tagsColumn = new TableColumn<>("Tags");
        tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tags"));
        tagsColumn.setPrefWidth(200);
        
        fileTable.getColumns().addAll(iconColumn, nameColumn, sizeColumn, typeColumn, dateColumn, tagsColumn);
        
        // Enable row selection
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        fileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates();
        });
        
        // Double-click to download
        fileTable.setRowFactory(tv -> {
            TableRow<VaultFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    downloadSelectedFile();
                }
            });
            return row;
        });
        
        tableSection.getChildren().addAll(tableLabel, fileTable);
        
        return tableSection;
    }
    
    /**
     * Create button section
     */
    private HBox createButtonSection() {
        HBox buttonSection = new HBox(15);
        buttonSection.setAlignment(Pos.CENTER);
        
        uploadButton = new Button("Upload File");
        uploadButton.setPrefWidth(120);
        uploadButton.setOnAction(e -> uploadFile());
        
        downloadButton = new Button("Download");
        downloadButton.setPrefWidth(120);
        downloadButton.setDisable(true);
        downloadButton.setOnAction(e -> downloadSelectedFile());
        
        deleteButton = new Button("Delete");
        deleteButton.setPrefWidth(120);
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedFile());
        
        Button editTagsButton = new Button("Edit Tags");
        editTagsButton.setPrefWidth(120);
        editTagsButton.setDisable(true);
        editTagsButton.setOnAction(e -> editSelectedFileTags());
        
        // Update button states when selection changes
        fileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            downloadButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            editTagsButton.setDisable(!hasSelection);
        });
        
        buttonSection.getChildren().addAll(uploadButton, downloadButton, deleteButton, editTagsButton);
        
        return buttonSection;
    }
    
    /**
     * Create status section
     */
    private VBox createStatusSection() {
        VBox statusSection = new VBox(5);
        
        operationProgress = new ProgressBar();
        operationProgress.setPrefWidth(400);
        operationProgress.setVisible(false);
        
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", 12));
        
        statusSection.getChildren().addAll(operationProgress, statusLabel);
        statusSection.setAlignment(Pos.CENTER);
        
        return statusSection;
    }
    
    /**
     * Upload file with encryption and progress indication
     */
    private void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null) {
            return;
        }
        
        // Validate file
        if (!FileUtils.isValidFile(selectedFile)) {
            showAlert("Invalid File", "The selected file is not valid for vault storage.", Alert.AlertType.ERROR);
            return;
        }
        
        // Show progress and disable buttons
        showProgress("Uploading file...");
        setButtonsEnabled(false);
        
        Task<VaultFile> uploadTask = new Task<VaultFile>() {
            @Override
            protected VaultFile call() throws Exception {
                updateMessage("Encrypting file...");
                updateProgress(0.3, 1.0);
                
                // Store file in vault
                VaultFile vaultFile = fileManager.storeFile(selectedFile);
                
                updateMessage("Updating metadata...");
                updateProgress(0.7, 1.0);
                
                // Add to metadata
                metadataManager.addFile(vaultFile);
                
                updateProgress(1.0, 1.0);
                return vaultFile;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    refreshFileList();
                    updateVaultStats();
                    setStatus("File uploaded successfully: " + selectedFile.getName());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    setStatus("Upload failed: " + getException().getMessage());
                    showAlert("Upload Failed", "Failed to upload file: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        // Bind progress
        operationProgress.progressProperty().bind(uploadTask.progressProperty());
        statusLabel.textProperty().bind(uploadTask.messageProperty());
        
        Thread uploadThread = new Thread(uploadTask);
        uploadThread.setDaemon(true);
        uploadThread.start();
    }
    
    /**
     * Download selected file with decryption and integrity verification
     */
    private void downloadSelectedFile() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.setInitialFileName(selectedFile.getOriginalName());
        
        File saveLocation = fileChooser.showSaveDialog(primaryStage);
        if (saveLocation == null) {
            return;
        }
        
        // Show progress and disable buttons
        showProgress("Downloading file...");
        setButtonsEnabled(false);
        
        Task<Void> downloadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Decrypting file...");
                updateProgress(0.3, 1.0);
                
                // Verify file integrity
                updateMessage("Verifying integrity...");
                updateProgress(0.5, 1.0);
                
                if (!fileManager.verifyFileIntegrity(selectedFile)) {
                    throw new SecurityException("File integrity verification failed");
                }
                
                updateMessage("Exporting file...");
                updateProgress(0.8, 1.0);
                
                // Export file
                fileManager.exportFile(selectedFile, saveLocation);
                
                updateProgress(1.0, 1.0);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    setStatus("File downloaded successfully: " + selectedFile.getOriginalName());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    setStatus("Download failed: " + getException().getMessage());
                    showAlert("Download Failed", "Failed to download file: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        // Bind progress
        operationProgress.progressProperty().bind(downloadTask.progressProperty());
        statusLabel.textProperty().bind(downloadTask.messageProperty());
        
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();
    }
    
    /**
     * Delete selected file with user confirmation and progress feedback
     */
    private void deleteSelectedFile() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            return;
        }
        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete File");
        confirmAlert.setContentText("Are you sure you want to permanently delete '" + 
            selectedFile.getOriginalName() + "'?\n\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        
        // Show progress and disable buttons
        showProgress("Deleting file...");
        setButtonsEnabled(false);
        
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Securely deleting file...");
                updateProgress(0.3, 1.0);
                
                // Secure delete from vault
                fileManager.secureDeleteFile(selectedFile);
                
                updateMessage("Updating metadata...");
                updateProgress(0.8, 1.0);
                
                // Remove from metadata
                metadataManager.removeFile(selectedFile.getFileId());
                
                updateProgress(1.0, 1.0);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    refreshFileList();
                    updateVaultStats();
                    setStatus("File deleted successfully: " + selectedFile.getOriginalName());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setButtonsEnabled(true);
                    setStatus("Deletion failed: " + getException().getMessage());
                    showAlert("Deletion Failed", "Failed to delete file: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };
        
        // Bind progress
        operationProgress.progressProperty().bind(deleteTask.progressProperty());
        statusLabel.textProperty().bind(deleteTask.messageProperty());
        
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }
    
    /**
     * Edit tags for selected file
     */
    private void editSelectedFileTags() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedFile.getTags());
        dialog.setTitle("Edit Tags");
        dialog.setHeaderText("Edit tags for: " + selectedFile.getOriginalName());
        dialog.setContentText("Tags (comma-separated):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                metadataManager.updateFileTags(selectedFile.getFileId(), result.get());
                refreshFileList();
                setStatus("Tags updated for: " + selectedFile.getOriginalName());
            } catch (Exception e) {
                showAlert("Update Failed", "Failed to update tags: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Perform real-time search
     */
    private void performSearch(String query) {
        try {
            List<VaultFile> searchResults = metadataManager.searchFiles(query);
            Platform.runLater(() -> {
                fileTable.getItems().clear();
                fileTable.getItems().addAll(searchResults);
                setStatus("Found " + searchResults.size() + " files matching: " + query);
            });
        } catch (Exception e) {
            setStatus("Search failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh file list
     */
    private void refreshFileList() {
        try {
            List<VaultFile> allFiles = metadataManager.getAllFiles();
            Platform.runLater(() -> {
                fileTable.getItems().clear();
                fileTable.getItems().addAll(allFiles);
                setStatus("Loaded " + allFiles.size() + " files");
            });
        } catch (Exception e) {
            setStatus("Failed to load files: " + e.getMessage());
        }
    }
    
    /**
     * Update vault statistics
     */
    private void updateVaultStats() {
        try {
            FileManager.VaultStats vaultStats = fileManager.getVaultStats();
            MetadataManager.MetadataStats metadataStats = metadataManager.getMetadataStats();
            
            Platform.runLater(() -> {
                vaultStatsLabel.setText(String.format("Files: %d | Size: %s | Types: %d", 
                    metadataStats.getFileCount(), 
                    vaultStats.getFormattedSize(),
                    metadataStats.getUniqueExtensions()));
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                vaultStatsLabel.setText("Stats unavailable");
            });
        }
    }
    
    /**
     * Update button states
     */
    private void updateButtonStates() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selectedFile != null;
        
        downloadButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }
    
    /**
     * Show progress indicator
     */
    private void showProgress(String message) {
        operationProgress.setVisible(true);
        setStatus(message);
    }
    
    /**
     * Hide progress indicator
     */
    private void hideProgress() {
        operationProgress.setVisible(false);
        operationProgress.progressProperty().unbind();
        statusLabel.textProperty().unbind();
    }
    
    /**
     * Set status message
     */
    private void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    /**
     * Enable/disable buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        uploadButton.setDisable(!enabled);
        refreshButton.setDisable(!enabled);
        
        if (enabled) {
            updateButtonStates();
        } else {
            downloadButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}