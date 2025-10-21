package com.ghostvault.ui.components;

import com.ghostvault.security.PasswordManager;
import com.ghostvault.ui.controllers.ModeController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * Mode-aware file manager that displays different content based on vault mode
 * while maintaining identical UI appearance across all modes
 */
public class ModeAwareFileManager extends VBox {
    
    private ModeController.VaultMode currentMode;
    private PasswordManager passwordManager;
    private File currentDirectory;
    
    // UI Components - identical across all modes
    private ListView<FileItem> fileListView;
    private ObservableList<FileItem> fileItems;
    private Label directoryLabel;
    private Label fileCountLabel;
    
    // Event handlers
    private Consumer<File> onFileSelected;
    private Consumer<File> onFileDoubleClicked;
    private Consumer<Set<File>> onSelectionChanged;
    
    // File icons
    private static final Map<String, String> FILE_ICONS = new HashMap<>();
    static {
        FILE_ICONS.put("folder", "📁");
        FILE_ICONS.put("txt", "📄");
        FILE_ICONS.put("doc", "📄");
        FILE_ICONS.put("docx", "📄");
        FILE_ICONS.put("pdf", "📕");
        FILE_ICONS.put("jpg", "🖼️");
        FILE_ICONS.put("jpeg", "🖼️");
        FILE_ICONS.put("png", "🖼️");
        FILE_ICONS.put("gif", "🖼️");
        FILE_ICONS.put("mp3", "🎵");
        FILE_ICONS.put("mp4", "🎬");
        FILE_ICONS.put("avi", "🎬");
        FILE_ICONS.put("zip", "📦");
        FILE_ICONS.put("rar", "📦");
        FILE_ICONS.put("exe", "⚙️");
        FILE_ICONS.put("default", "📄");
    }
    
    public ModeAwareFileManager() {
        this.passwordManager = new PasswordManager();
        this.fileItems = FXCollections.observableArrayList();
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        getStyleClass().add("mode-aware-file-manager");
        setStyle("-fx-background-color: #2b2b2b;");
        
        // Directory navigation bar
        HBox navigationBar = createNavigationBar();
        
        // File list view
        fileListView = createFileListView();
        
        // Status bar
        HBox statusBar = createStatusBar();
        
        getChildren().addAll(navigationBar, fileListView, statusBar);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
    }
    
    private HBox createNavigationBar() {
        HBox navBar = new HBox(10);
        navBar.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 8;");
        
        Button upButton = new Button("↑");
        upButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        upButton.setOnAction(e -> navigateUp());
        
        directoryLabel = new Label("Loading...");
        directoryLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("🔄");
        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshButton.setOnAction(e -> refreshDirectory());
        
        navBar.getChildren().addAll(upButton, directoryLabel, spacer, refreshButton);
        return navBar;
    }
    
    private ListView<FileItem> createFileListView() {
        ListView<FileItem> listView = new ListView<>(fileItems);
        listView.getStyleClass().add("file-list-view");
        listView.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        
        // Custom cell factory for consistent appearance
        listView.setCellFactory(param -> new FileItemCell());
        
        // Selection model for multi-select
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        return listView;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 5;");
        
        fileCountLabel = new Label("0 items");
        fileCountLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label securityLabel = new Label("🔒 Secure");
        securityLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        statusBar.getChildren().addAll(fileCountLabel, spacer, securityLabel);
        return statusBar;
    }
    
    private void setupEventHandlers() {
        // File selection handling
        fileListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onFileSelected != null) {
                onFileSelected.accept(newSelection.getFile());
            }
            
            // Notify selection change
            if (onSelectionChanged != null) {
                Set<File> selectedFiles = new HashSet<>();
                for (FileItem item : fileListView.getSelectionModel().getSelectedItems()) {
                    selectedFiles.add(item.getFile());
                }
                onSelectionChanged.accept(selectedFiles);
            }
        });
        
        // Double-click handling
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileItem selectedItem = fileListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    File file = selectedItem.getFile();
                    if (file.isDirectory()) {
                        navigateToDirectory(file);
                    } else if (onFileDoubleClicked != null) {
                        onFileDoubleClicked.accept(file);
                    }
                }
            }
        });
    }
    
    public void setCurrentMode(ModeController.VaultMode mode) {
        this.currentMode = mode;
        loadModeSpecificDirectory();
    }
    
    private void loadModeSpecificDirectory() {
        if (currentMode == null) return;
        
        try {
            Path vaultDirectory = passwordManager.getVaultDirectory(currentMode);
            currentDirectory = vaultDirectory.toFile();
            
            // Ensure directory exists
            if (!currentDirectory.exists()) {
                currentDirectory.mkdirs();
            }
            
            refreshDirectory();
            
        } catch (Exception e) {
            System.err.println("Error loading mode-specific directory: " + e.getMessage());
            showErrorState("Failed to load vault directory");
        }
    }
    
    public void refreshDirectory() {
        if (currentDirectory == null) return;
        
        Platform.runLater(() -> {
            try {
                fileItems.clear();
                
                File[] files = currentDirectory.listFiles();
                if (files != null) {
                    // Sort files: directories first, then by name
                    Arrays.sort(files, (f1, f2) -> {
                        if (f1.isDirectory() && !f2.isDirectory()) return -1;
                        if (!f1.isDirectory() && f2.isDirectory()) return 1;
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    });
                    
                    for (File file : files) {
                        // Skip hidden files and system files
                        if (!file.getName().startsWith(".")) {
                            fileItems.add(new FileItem(file));
                        }
                    }
                }
                
                updateUI();
                
            } catch (Exception e) {
                System.err.println("Error refreshing directory: " + e.getMessage());
                showErrorState("Failed to refresh directory");
            }
        });
    }
    
    private void updateUI() {
        // Update directory label
        if (currentDirectory != null) {
            String dirName = currentDirectory.getName();
            if (dirName.isEmpty()) {
                dirName = currentDirectory.getAbsolutePath();
            }
            directoryLabel.setText(dirName);
        }
        
        // Update file count
        fileCountLabel.setText(fileItems.size() + " items");
    }
    
    private void navigateUp() {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            // Don't allow navigation outside vault directory
            Path vaultRoot = passwordManager.getVaultDirectory(currentMode);
            Path parentPath = currentDirectory.getParentFile().toPath();
            
            if (parentPath.startsWith(vaultRoot.getParent())) {
                currentDirectory = currentDirectory.getParentFile();
                refreshDirectory();
            }
        }
    }
    
    private void navigateToDirectory(File directory) {
        if (directory != null && directory.isDirectory() && directory.canRead()) {
            currentDirectory = directory;
            refreshDirectory();
        }
    }
    
    private void showErrorState(String message) {
        fileItems.clear();
        directoryLabel.setText("Error");
        fileCountLabel.setText(message);
    }
    
    public Set<File> getSelectedFiles() {
        Set<File> selectedFiles = new HashSet<>();
        for (FileItem item : fileListView.getSelectionModel().getSelectedItems()) {
            selectedFiles.add(item.getFile());
        }
        return selectedFiles;
    }
    
    public void clearSelection() {
        fileListView.getSelectionModel().clearSelection();
    }
    
    // Event handler setters
    public void setOnFileSelected(Consumer<File> handler) {
        this.onFileSelected = handler;
    }
    
    public void setOnFileDoubleClicked(Consumer<File> handler) {
        this.onFileDoubleClicked = handler;
    }
    
    public void setOnSelectionChanged(Consumer<Set<File>> handler) {
        this.onSelectionChanged = handler;
    }
    
    /**
     * File item data class
     */
    private static class FileItem {
        private final File file;
        private final String displayName;
        private final String icon;
        private final String sizeText;
        private final String dateText;
        
        public FileItem(File file) {
            this.file = file;
            this.displayName = file.getName();
            this.icon = getFileIcon(file);
            this.sizeText = formatFileSize(file);
            this.dateText = formatDate(file.lastModified());
        }
        
        private String getFileIcon(File file) {
            if (file.isDirectory()) {
                return FILE_ICONS.get("folder");
            }
            
            String extension = getFileExtension(file.getName()).toLowerCase();
            return FILE_ICONS.getOrDefault(extension, FILE_ICONS.get("default"));
        }
        
        private String getFileExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
        }
        
        private String formatFileSize(File file) {
            if (file.isDirectory()) {
                return "Folder";
            }
            
            long size = file.length();
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
        
        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            return sdf.format(new Date(timestamp));
        }
        
        public File getFile() { return file; }
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getSizeText() { return sizeText; }
        public String getDateText() { return dateText; }
    }
    
    /**
     * Custom cell for file list - identical appearance across all modes
     */
    private static class FileItemCell extends ListCell<FileItem> {
        @Override
        protected void updateItem(FileItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox content = new HBox(10);
                content.setStyle("-fx-alignment: center-left; -fx-padding: 5;");
                
                // File icon
                Label iconLabel = new Label(item.getIcon());
                iconLabel.setStyle("-fx-font-size: 16px;");
                
                // File info
                VBox fileInfo = new VBox(2);
                
                Label nameLabel = new Label(item.getDisplayName());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                
                Label detailsLabel = new Label(item.getSizeText() + " • " + item.getDateText());
                detailsLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
                
                fileInfo.getChildren().addAll(nameLabel, detailsLabel);
                
                content.getChildren().addAll(iconLabel, fileInfo);
                
                setGraphic(content);
                setText(null);
                
                // Consistent styling
                setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            }
        }
    }
}