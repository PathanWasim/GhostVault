package com.ghostvault.ui;

import com.ghostvault.core.DecoyManager;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Decoy vault interface that mirrors real vault functionality
 * Shows fake files to hide the existence of real encrypted content
 */
public class DecoyVaultInterface {
    
    private Stage primaryStage;
    private DecoyManager decoyManager;
    
    private TableView<VaultFile> fileTable;
    private ObservableList<VaultFile> fileList;
    private TextField searchField;
    private Label statusLabel;
    private Label statsLabel;
    
    public DecoyVaultInterface(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.decoyManager = new DecoyManager();
        this.fileList = FXCollections.observableArrayList();
        
        // Ensure minimum decoy files exist
        try {
            decoyManager.ensureMinimumDecoyFiles(8);
        } catch (IOException e) {
            showAlert("Warning", "Could not create decoy files: " + e.getMessage(), Alert.AlertType.WARNING);
        }
        
        refreshFileList();
    }
    
    /**
     * Create and show the decoy vault interface
     */
    public Scene createDecoyVaultScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Title bar
        HBox titleBar = createTitleBar();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // File table
        VBox tableSection = createFileTableSection();
        
        // Status bar
        HBox statusBar = createStatusBar();
        
        root.getChildren().addAll(titleBar, toolbar, tableSection, statusBar);
        
        return new Scene(root, 900, 600);
    }
    
    /**
     * Create title bar
     */
    private HBox createTitleBar() {
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 10, 0));
        
        Label titleLabel = new Label("📁 Personal Document Vault");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("👤 User: " + System.getProperty("user.name"));
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.GRAY);
        
        titleBar.getChildren().addAll(titleLabel, spacer, userLabel);
        
        return titleBar;
    }
    
    /**
     * Create toolbar with actions
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        
        Button addButton = new Button("📄 Add Document");
        addButton.setOnAction(e -> addDecoyFile());
        
        Button viewButton = new Button("👁 View");
        viewButton.setOnAction(e -> viewSelectedFile());
        
        Button deleteButton = new Button("🗑 Delete");
        deleteButton.setOnAction(e -> deleteSelectedFile());
        
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setOnAction(e -> refreshFileList());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchField = new TextField();
        searchField.setPromptText("🔍 Search documents...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterFiles(newText));
        
        toolbar.getChildren().addAll(addButton, viewButton, deleteButton, refreshButton, spacer, searchField);
        
        return toolbar;
    }
    
    /**
     * Create file table section
     */
    private VBox createFileTableSection() {
        VBox section = new VBox(5);
        
        Label tableLabel = new Label("Documents (" + fileList.size() + " files)");
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        fileTable = new TableView<>();
        fileTable.setItems(fileList);
        
        // File name column
        TableColumn<VaultFile, String> nameColumn = new TableColumn<>("📄 Document Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("originalName"));
        nameColumn.setPrefWidth(300);
        
        // Size column
        TableColumn<VaultFile, String> sizeColumn = new TableColumn<>("📏 Size");
        sizeColumn.setCellValueFactory(cellData -> {
            long size = cellData.getValue().getSize();
            return new javafx.beans.property.SimpleStringProperty(FileUtils.formatFileSize(size));
        });
        sizeColumn.setPrefWidth(100);
        
        // Date column
        TableColumn<VaultFile, String> dateColumn = new TableColumn<>("📅 Date Modified");
        dateColumn.setCellValueFactory(cellData -> {
            long timestamp = cellData.getValue().getUploadTime();
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            return new javafx.beans.property.SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        });
        dateColumn.setPrefWidth(150);
        
        // Type column
        TableColumn<VaultFile, String> typeColumn = new TableColumn<>("📋 Type");
        typeColumn.setCellValueFactory(cellData -> {
            String extension = cellData.getValue().getExtension();
            String type = getFileTypeDescription(extension);
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        typeColumn.setPrefWidth(120);
        
        fileTable.getColumns().addAll(nameColumn, sizeColumn, dateColumn, typeColumn);
        
        // Double-click to view
        fileTable.setRowFactory(tv -> {
            TableRow<VaultFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewSelectedFile();
                }
            });
            return row;
        });
        
        VBox.setVgrow(fileTable, Priority.ALWAYS);
        section.getChildren().addAll(tableLabel, fileTable);
        
        return section;
    }
    
    /**
     * Create status bar
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 0, 0, 0));
        statusBar.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", 10));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statsLabel = new Label();
        statsLabel.setFont(Font.font("System", 10));
        statsLabel.setTextFill(Color.GRAY);
        updateStatsLabel();
        
        statusBar.getChildren().addAll(statusLabel, spacer, statsLabel);
        
        return statusBar;
    }
    
    /**
     * Add a new decoy file
     */
    private void addDecoyFile() {
        try {
            // Generate a new decoy file
            VaultFile newDecoyFile = decoyManager.generateSingleDecoyFile();
            
            // Add to list and refresh
            fileList.add(newDecoyFile);
            updateStatsLabel();
            
            statusLabel.setText("Document added: " + newDecoyFile.getOriginalName());
            
            // Show success message
            showAlert("Success", "Document '" + newDecoyFile.getOriginalName() + "' has been added to your vault.", Alert.AlertType.INFORMATION);
            
        } catch (IOException e) {
            showAlert("Error", "Failed to add document: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * View selected file
     */
    private void viewSelectedFile() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert("No Selection", "Please select a document to view.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            byte[] content = decoyManager.getDecoyFileContent(selectedFile.getOriginalName());
            showFileContent(selectedFile.getOriginalName(), new String(content));
            
            statusLabel.setText("Viewing: " + selectedFile.getOriginalName());
            
        } catch (IOException e) {
            showAlert("Error", "Failed to open document: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Delete selected file
     */
    private void deleteSelectedFile() {
        VaultFile selectedFile = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert("No Selection", "Please select a document to delete.", Alert.AlertType.WARNING);
            return;
        }
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Document");
        confirmAlert.setContentText("Are you sure you want to delete '" + selectedFile.getOriginalName() + "'?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (decoyManager.removeDecoyFile(selectedFile.getOriginalName())) {
                fileList.remove(selectedFile);
                updateStatsLabel();
                statusLabel.setText("Document deleted: " + selectedFile.getOriginalName());
            } else {
                showAlert("Error", "Failed to delete document.", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Refresh file list
     */
    private void refreshFileList() {
        fileList.clear();
        List<VaultFile> decoyFiles = decoyManager.getDecoyFiles();
        fileList.addAll(decoyFiles);
        updateStatsLabel();
        statusLabel.setText("File list refreshed");
    }
    
    /**
     * Filter files based on search text
     */
    private void filterFiles(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshFileList();
            return;
        }
        
        List<VaultFile> filteredFiles = decoyManager.searchDecoyFiles(searchText);
        fileList.clear();
        fileList.addAll(filteredFiles);
        
        statusLabel.setText("Search results: " + filteredFiles.size() + " documents found");
    }
    
    /**
     * Show file content in a dialog
     */
    private void showFileContent(String fileName, String content) {
        Stage contentStage = new Stage();
        contentStage.setTitle("Document Viewer - " + fileName);
        contentStage.initOwner(primaryStage);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("📄 " + fileName);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        TextArea contentArea = new TextArea(content);
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(20);
        contentArea.setPrefColumnCount(60);
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> contentStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(titleLabel, contentArea, buttonBox);
        
        Scene scene = new Scene(root, 600, 500);
        contentStage.setScene(scene);
        contentStage.show();
    }
    
    /**
     * Update statistics label
     */
    private void updateStatsLabel() {
        DecoyManager.DecoyStats stats = decoyManager.getDecoyStats();
        statsLabel.setText(String.format("Total: %d documents, %s", 
            stats.getFileCount(), stats.getFormattedSize()));
    }
    
    /**
     * Get file type description
     */
    private String getFileTypeDescription(String extension) {
        switch (extension.toLowerCase()) {
            case "txt": return "Text Document";
            case "docx": return "Word Document";
            case "pdf": return "PDF Document";
            case "xlsx": return "Excel Spreadsheet";
            case "pptx": return "PowerPoint Presentation";
            case "jpg": case "jpeg": return "JPEG Image";
            case "png": return "PNG Image";
            case "gif": return "GIF Image";
            case "mp3": return "MP3 Audio";
            case "mp4": return "MP4 Video";
            case "zip": return "ZIP Archive";
            default: return extension.toUpperCase() + " File";
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
            alert.initOwner(primaryStage);
            alert.showAndWait();
        });
    }
    
    /**
     * Get current file count
     */
    public int getFileCount() {
        return fileList.size();
    }
    
    /**
     * Check if decoy vault has content
     */
    public boolean hasContent() {
        return !fileList.isEmpty();
    }
}