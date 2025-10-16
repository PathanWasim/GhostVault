package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern file chooser dialog with enhanced features and preview capabilities
 */
public class ModernFileChooser {
    
    // Dialog components
    private Stage dialogStage;
    private VBox mainContainer;
    private HBox headerBar;
    private Label titleLabel;
    private Button closeButton;
    
    // File browser components
    private HBox browserContainer;
    private VBox leftPanel;
    private VBox rightPanel;
    private TreeView<File> directoryTree;
    private VirtualizedFileListView fileListView;
    private VBox previewPane;
    private ImageView previewImageView;
    private Label previewLabel;
    
    // Controls
    private HBox controlsBar;
    private TextField fileNameField;
    private ComboBox<FileTypeFilter> fileTypeFilter;
    private CheckBox showHiddenFiles;
    private Button selectButton;
    private Button cancelButton;
    
    // Progress and status
    private ProgressBar uploadProgress;
    private Label statusLabel;
    
    // Configuration
    private boolean multipleSelection = false;
    private boolean directorySelection = false;
    private File initialDirectory;
    private String initialFileName;
    private List<FileTypeFilter> fileFilters = new ArrayList<>();
    
    // Results
    private List<File> selectedFiles = new ArrayList<>();
    private Consumer<List<File>> onFilesSelected;
    private Runnable onCancelled;
    
    // Current state
    private File currentDirectory;
    
    public ModernFileChooser() {
        initializeComponents();
        setupLayout();
        setupStyling();
        setupEventHandlers();
        loadInitialDirectory();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Dialog stage
        dialogStage = new Stage();
        dialogStage.setTitle("Select Files");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(900);
        dialogStage.setHeight(600);
        
        // Main container
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("modern-file-chooser");
        
        // Header bar
        headerBar = new HBox(8);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getStyleClass().add("file-chooser-header");
        
        titleLabel = new Label("Select Files");
        titleLabel.getStyleClass().add("file-chooser-title");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "icon", "close-button");
        
        headerBar.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        
        // Browser container
        browserContainer = new HBox(8);
        browserContainer.getStyleClass().add("file-browser-container");
        
        // Left panel - Directory tree
        leftPanel = new VBox(8);
        leftPanel.getStyleClass().add("directory-panel");
        leftPanel.setPrefWidth(250);
        leftPanel.setMinWidth(200);
        
        Label dirLabel = new Label("Folders");
        dirLabel.getStyleClass().add("panel-title");
        
        directoryTree = new TreeView<>();
        directoryTree.getStyleClass().add("directory-tree");
        directoryTree.setShowRoot(false);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);
        
        leftPanel.getChildren().addAll(dirLabel, directoryTree);
        
        // Center panel - File list
        VBox centerPanel = new VBox(8);
        centerPanel.getStyleClass().add("file-list-panel");
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        Label filesLabel = new Label("Files");
        filesLabel.getStyleClass().add("panel-title");
        
        fileListView = new VirtualizedFileListView();
        fileListView.setShowPagination(false); // Disable pagination for file chooser
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        
        centerPanel.getChildren().addAll(filesLabel, fileListView);
        
        // Right panel - Preview
        rightPanel = new VBox(8);
        rightPanel.getStyleClass().add("preview-panel");
        rightPanel.setPrefWidth(200);
        rightPanel.setMinWidth(150);
        
        Label previewTitleLabel = new Label("Preview");
        previewTitleLabel.getStyleClass().add("panel-title");
        
        previewPane = new VBox(8);
        previewPane.setAlignment(Pos.CENTER);
        previewPane.getStyleClass().add("preview-content");
        VBox.setVgrow(previewPane, Priority.ALWAYS);
        
        previewImageView = new ImageView();
        previewImageView.setFitWidth(150);
        previewImageView.setFitHeight(150);
        previewImageView.setPreserveRatio(true);
        previewImageView.setSmooth(true);
        
        previewLabel = new Label("No preview available");
        previewLabel.getStyleClass().add("preview-label");
        
        previewPane.getChildren().addAll(previewImageView, previewLabel);
        rightPanel.getChildren().addAll(previewTitleLabel, previewPane);
        
        browserContainer.getChildren().addAll(leftPanel, centerPanel, rightPanel);
        
        // Controls bar
        controlsBar = new HBox(8);
        controlsBar.setAlignment(Pos.CENTER_LEFT);
        controlsBar.getStyleClass().add("controls-bar");
        
        Label fileNameLabel = new Label("File name:");
        fileNameLabel.getStyleClass().add("control-label");
        
        fileNameField = new TextField();
        fileNameField.getStyleClass().add("file-name-field");
        HBox.setHgrow(fileNameField, Priority.ALWAYS);
        
        Label filterLabel = new Label("File type:");
        filterLabel.getStyleClass().add("control-label");
        
        fileTypeFilter = new ComboBox<>();
        fileTypeFilter.getStyleClass().add("file-type-filter");
        fileTypeFilter.setPrefWidth(200);
        
        showHiddenFiles = new CheckBox("Show hidden files");
        showHiddenFiles.getStyleClass().add("show-hidden-checkbox");
        
        Region controlsSpacer = new Region();
        HBox.setHgrow(controlsSpacer, Priority.ALWAYS);
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "cancel-button");
        
        selectButton = new Button("Select");
        selectButton.getStyleClass().addAll("button", "primary", "select-button");
        selectButton.setDefaultButton(true);
        
        controlsBar.getChildren().addAll(
            fileNameLabel, fileNameField,
            filterLabel, fileTypeFilter,
            showHiddenFiles,
            controlsSpacer,
            cancelButton, selectButton
        );
        
        // Progress and status
        uploadProgress = new ProgressBar();
        uploadProgress.getStyleClass().add("upload-progress");
        uploadProgress.setVisible(false);
        uploadProgress.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        VBox statusContainer = new VBox(4);
        statusContainer.getChildren().addAll(uploadProgress, statusLabel);
        
        mainContainer.getChildren().addAll(
            headerBar, browserContainer, controlsBar, statusContainer
        );
        
        // Create scene
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
        dialogStage.setScene(scene);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        mainContainer.setSpacing(0);
        mainContainer.setPadding(new Insets(0));
        
        headerBar.setPadding(new Insets(12));
        browserContainer.setPadding(new Insets(0, 12, 12, 12));
        controlsBar.setPadding(new Insets(12));
        
        VBox.setVgrow(browserContainer, Priority.ALWAYS);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        // Styling is handled via CSS classes
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Close button
        closeButton.setOnAction(e -> cancel());
        
        // Dialog close
        dialogStage.setOnCloseRequest(e -> cancel());
        
        // Directory tree selection
        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                navigateToDirectory(newItem.getValue());
            }
        });
        
        // File list selection
        fileListView.setOnFileSelected(this::selectFile);
        fileListView.setOnFileDoubleClicked(this::handleFileDoubleClick);
        
        // File name field
        fileNameField.textProperty().addListener((obs, oldText, newText) -> {
            updateSelectButtonState();
        });
        
        // File type filter
        fileTypeFilter.setOnAction(e -> applyFileFilter());
        
        // Show hidden files
        showHiddenFiles.setOnAction(e -> refreshFileList());
        
        // Control buttons
        selectButton.setOnAction(e -> selectFiles());
        cancelButton.setOnAction(e -> cancel());
    }
    
    /**
     * Load initial directory
     */
    private void loadInitialDirectory() {
        File startDir = initialDirectory != null ? initialDirectory : new File(System.getProperty("user.home"));
        navigateToDirectory(startDir);
        buildDirectoryTree();
    }
    
    /**
     * Build directory tree
     */
    private void buildDirectoryTree() {
        Task<TreeItem<File>> treeTask = new Task<TreeItem<File>>() {
            @Override
            protected TreeItem<File> call() throws Exception {
                File[] roots = File.listRoots();
                TreeItem<File> rootItem = new TreeItem<>();
                
                for (File root : roots) {
                    TreeItem<File> rootTreeItem = new TreeItem<>(root);
                    rootTreeItem.setExpanded(false);
                    
                    // Add dummy child to make it expandable
                    rootTreeItem.getChildren().add(new TreeItem<>());
                    
                    rootItem.getChildren().add(rootTreeItem);
                }
                
                return rootItem;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    directoryTree.setRoot(getValue());
                    
                    // Set up lazy loading for tree items
                    directoryTree.setOnMouseClicked(event -> {
                        TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();
                        if (selectedItem != null && !selectedItem.isExpanded()) {
                            loadDirectoryChildren(selectedItem);
                        }
                    });
                });
            }
        };
        
        Thread treeThread = new Thread(treeTask);
        treeThread.setDaemon(true);
        treeThread.start();
    }
    
    /**
     * Load children for a directory tree item
     */
    private void loadDirectoryChildren(TreeItem<File> parentItem) {
        if (parentItem.getValue() == null) return;
        
        Task<List<File>> loadTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                File parentDir = parentItem.getValue();
                List<File> children = new ArrayList<>();
                
                File[] files = parentDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory() && (!file.isHidden() || showHiddenFiles.isSelected())) {
                            children.add(file);
                        }
                    }
                }
                
                return children;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    parentItem.getChildren().clear();
                    
                    for (File child : getValue()) {
                        TreeItem<File> childItem = new TreeItem<>(child);
                        
                        // Check if this directory has subdirectories
                        File[] subFiles = child.listFiles();
                        if (subFiles != null) {
                            for (File subFile : subFiles) {
                                if (subFile.isDirectory()) {
                                    childItem.getChildren().add(new TreeItem<>()); // Dummy child
                                    break;
                                }
                            }
                        }
                        
                        parentItem.getChildren().add(childItem);
                    }
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Navigate to a directory
     */
    private void navigateToDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        currentDirectory = directory;
        refreshFileList();
        statusLabel.setText("Current folder: " + directory.getAbsolutePath());
    }
    
    /**
     * Refresh file list for current directory
     */
    private void refreshFileList() {
        if (currentDirectory == null) return;
        
        Task<List<File>> loadTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                List<File> files = new ArrayList<>();
                
                File[] fileArray = currentDirectory.listFiles();
                if (fileArray != null) {
                    for (File file : fileArray) {
                        if (file.isHidden() && !showHiddenFiles.isSelected()) {
                            continue;
                        }
                        
                        if (directorySelection && !file.isDirectory()) {
                            continue;
                        }
                        
                        if (matchesFileFilter(file)) {
                            files.add(file);
                        }
                    }
                }
                
                return files;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    fileListView.setFiles(getValue());
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Check if file matches current filter
     */
    private boolean matchesFileFilter(File file) {
        FileTypeFilter selectedFilter = fileTypeFilter.getValue();
        if (selectedFilter == null || selectedFilter.getExtensions().isEmpty()) {
            return true;
        }
        
        if (file.isDirectory()) {
            return true; // Always show directories
        }
        
        String fileName = file.getName().toLowerCase();
        for (String extension : selectedFilter.getExtensions()) {
            if (fileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Apply file filter
     */
    private void applyFileFilter() {
        refreshFileList();
    }
    
    /**
     * Select a file
     */
    private void selectFile(File file) {
        if (file == null) return;
        
        if (file.isFile()) {
            fileNameField.setText(file.getName());
            showPreview(file);
        }
        
        updateSelectButtonState();
    }
    
    /**
     * Handle file double-click
     */
    private void handleFileDoubleClick(File file) {
        if (file.isDirectory()) {
            navigateToDirectory(file);
        } else {
            selectFiles(); // Select and close
        }
    }
    
    /**
     * Show preview for selected file
     */
    private void showPreview(File file) {
        if (file == null || !file.isFile()) {
            previewImageView.setImage(null);
            previewLabel.setText("No preview available");
            return;
        }
        
        // Show file info
        String info = String.format("%s\n%s\n%s", 
            file.getName(),
            formatFileSize(file.length()),
            formatDate(file.lastModified()));
        previewLabel.setText(info);
        
        // Try to show image preview
        if (ImagePreviewPane.isSupported(file)) {
            Task<javafx.scene.image.Image> previewTask = new Task<javafx.scene.image.Image>() {
                @Override
                protected javafx.scene.image.Image call() throws Exception {
                    return ThumbnailGenerator.generateThumbnail(file, 150);
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        javafx.scene.image.Image preview = getValue();
                        if (preview != null) {
                            previewImageView.setImage(preview);
                        }
                    });
                }
            };
            
            Thread previewThread = new Thread(previewTask);
            previewThread.setDaemon(true);
            previewThread.start();
        } else {
            previewImageView.setImage(FileIconProvider.getFileIconImage(file));
        }
    }
    
    /**
     * Update select button state
     */
    private void updateSelectButtonState() {
        boolean hasSelection = !fileNameField.getText().trim().isEmpty() || 
                              fileListView.getSelectedFile() != null;
        selectButton.setDisable(!hasSelection);
    }
    
    /**
     * Select files and close dialog
     */
    private void selectFiles() {
        selectedFiles.clear();
        
        String fileName = fileNameField.getText().trim();
        if (!fileName.isEmpty()) {
            File selectedFile = new File(currentDirectory, fileName);
            if (selectedFile.exists()) {
                selectedFiles.add(selectedFile);
            }
        } else {
            File listSelection = fileListView.getSelectedFile();
            if (listSelection != null) {
                selectedFiles.add(listSelection);
            }
        }
        
        if (!selectedFiles.isEmpty()) {
            if (onFilesSelected != null) {
                onFilesSelected.accept(new ArrayList<>(selectedFiles));
            }
            dialogStage.close();
        }
    }
    
    /**
     * Cancel and close dialog
     */
    private void cancel() {
        selectedFiles.clear();
        if (onCancelled != null) {
            onCancelled.run();
        }
        dialogStage.close();
    }
    
    // Helper methods
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    // Public API
    
    /**
     * Show the file chooser dialog
     */
    public void showDialog(Window owner) {
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        
        // Initialize file type filters
        if (fileFilters.isEmpty()) {
            fileFilters.add(new FileTypeFilter("All Files", "*.*"));
        }
        fileTypeFilter.getItems().setAll(fileFilters);
        fileTypeFilter.setValue(fileFilters.get(0));
        
        // Set initial file name
        if (initialFileName != null) {
            fileNameField.setText(initialFileName);
        }
        
        updateSelectButtonState();
        dialogStage.showAndWait();
    }
    
    // Getters and Setters
    
    public void setMultipleSelection(boolean multiple) {
        this.multipleSelection = multiple;
        titleLabel.setText(multiple ? "Select Files" : "Select File");
    }
    
    public void setDirectorySelection(boolean directory) {
        this.directorySelection = directory;
        titleLabel.setText(directory ? "Select Folder" : "Select File");
    }
    
    public void setInitialDirectory(File directory) {
        this.initialDirectory = directory;
    }
    
    public void setInitialFileName(String fileName) {
        this.initialFileName = fileName;
    }
    
    public void setFileFilters(List<FileTypeFilter> filters) {
        this.fileFilters = new ArrayList<>(filters);
    }
    
    public void addFileFilter(FileTypeFilter filter) {
        this.fileFilters.add(filter);
    }
    
    public void setOnFilesSelected(Consumer<List<File>> callback) {
        this.onFilesSelected = callback;
    }
    
    public void setOnCancelled(Runnable callback) {
        this.onCancelled = callback;
    }
    
    public List<File> getSelectedFiles() {
        return new ArrayList<>(selectedFiles);
    }
    
    /**
     * File type filter class
     */
    public static class FileTypeFilter {
        private String description;
        private List<String> extensions;
        
        public FileTypeFilter(String description, String... extensions) {
            this.description = description;
            this.extensions = new ArrayList<>();
            for (String ext : extensions) {
                this.extensions.add(ext);
            }
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getExtensions() {
            return extensions;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}