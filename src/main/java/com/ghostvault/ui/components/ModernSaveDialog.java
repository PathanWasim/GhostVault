package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern save dialog with enhanced features and conflict resolution
 */
public class ModernSaveDialog {
    
    // Dialog components
    private Stage dialogStage;
    private VBox mainContainer;
    private HBox headerBar;
    private Label titleLabel;
    private Button closeButton;
    
    // File browser components
    private HBox browserContainer;
    private VBox leftPanel;
    private VBox centerPanel;
    private TreeView<File> directoryTree;
    private ListView<File> fileListView;
    
    // Save controls
    private HBox saveControlsBar;
    private TextField fileNameField;
    private ComboBox<FileTypeFilter> fileTypeFilter;
    private CheckBox createBackup;
    private CheckBox overwriteExisting;
    
    // Action buttons
    private HBox buttonBar;
    private Button saveButton;
    private Button cancelButton;
    
    // Progress and status
    private VBox progressContainer;
    private ProgressBar saveProgress;
    private Label statusLabel;
    
    // Conflict resolution
    private VBox conflictPanel;
    private Label conflictMessage;
    private HBox conflictButtons;
    private Button replaceButton;
    private Button keepBothButton;
    private Button skipButton;
    
    // Configuration
    private File initialDirectory;
    private String initialFileName;
    private List<FileTypeFilter> fileFilters = new ArrayList<>();
    private boolean allowOverwrite = true;
    private boolean showBackupOption = true;
    
    // Results
    private File selectedFile;
    private Consumer<File> onFileSelected;
    private Consumer<SaveOptions> onSaveWithOptions;
    private Runnable onCancelled;
    
    // Current state
    private File currentDirectory;
    private boolean conflictResolutionMode = false;
    
    public ModernSaveDialog() {
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
        dialogStage.setTitle("Save File");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(800);
        dialogStage.setHeight(600);
        
        // Main container
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("modern-save-dialog");
        
        // Header bar
        headerBar = new HBox(8);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getStyleClass().add("save-dialog-header");
        
        titleLabel = new Label("Save File");
        titleLabel.getStyleClass().add("save-dialog-title");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "icon", "close-button");
        
        headerBar.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        
        // Browser container
        browserContainer = new HBox(8);
        browserContainer.getStyleClass().add("save-browser-container");
        
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
        centerPanel = new VBox(8);
        centerPanel.getStyleClass().add("file-list-panel");
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        
        Label filesLabel = new Label("Files");
        filesLabel.getStyleClass().add("panel-title");
        
        fileListView = new ListView<>();
        fileListView.getStyleClass().add("save-file-list");
        fileListView.setCellFactory(listView -> new SaveFileListCell());
        VBox.setVgrow(fileListView, Priority.ALWAYS);
        
        centerPanel.getChildren().addAll(filesLabel, fileListView);
        
        browserContainer.getChildren().addAll(leftPanel, centerPanel);
        
        // Save controls bar
        saveControlsBar = new HBox(8);
        saveControlsBar.setAlignment(Pos.CENTER_LEFT);
        saveControlsBar.getStyleClass().add("save-controls-bar");
        
        Label fileNameLabel = new Label("File name:");
        fileNameLabel.getStyleClass().add("control-label");
        
        fileNameField = new TextField();
        fileNameField.getStyleClass().add("file-name-field");
        HBox.setHgrow(fileNameField, Priority.ALWAYS);
        
        Label filterLabel = new Label("Save as type:");
        filterLabel.getStyleClass().add("control-label");
        
        fileTypeFilter = new ComboBox<>();
        fileTypeFilter.getStyleClass().add("file-type-filter");
        fileTypeFilter.setPrefWidth(200);
        
        saveControlsBar.getChildren().addAll(
            fileNameLabel, fileNameField,
            filterLabel, fileTypeFilter
        );
        
        // Options
        HBox optionsBar = new HBox(16);
        optionsBar.setAlignment(Pos.CENTER_LEFT);
        optionsBar.getStyleClass().add("save-options-bar");
        
        createBackup = new CheckBox("Create backup of existing file");
        createBackup.getStyleClass().add("save-option-checkbox");
        
        overwriteExisting = new CheckBox("Overwrite existing files");
        overwriteExisting.getStyleClass().add("save-option-checkbox");
        overwriteExisting.setSelected(false);
        
        optionsBar.getChildren().addAll(createBackup, overwriteExisting);
        
        // Button bar
        buttonBar = new HBox(8);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.getStyleClass().add("save-button-bar");
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "cancel-button");
        
        saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("button", "primary", "save-button");
        saveButton.setDefaultButton(true);
        
        buttonBar.getChildren().addAll(cancelButton, saveButton);
        
        // Progress container
        progressContainer = new VBox(4);
        progressContainer.getStyleClass().add("progress-container");
        progressContainer.setVisible(false);
        
        saveProgress = new ProgressBar();
        saveProgress.getStyleClass().add("save-progress");
        saveProgress.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready to save");
        statusLabel.getStyleClass().add("save-status-label");
        
        progressContainer.getChildren().addAll(saveProgress, statusLabel);
        
        // Conflict resolution panel
        conflictPanel = new VBox(8);
        conflictPanel.getStyleClass().add("conflict-panel");
        conflictPanel.setVisible(false);
        conflictPanel.setAlignment(Pos.CENTER);
        
        conflictMessage = new Label();
        conflictMessage.getStyleClass().add("conflict-message");
        conflictMessage.setWrapText(true);
        
        conflictButtons = new HBox(8);
        conflictButtons.setAlignment(Pos.CENTER);
        conflictButtons.getStyleClass().add("conflict-buttons");
        
        replaceButton = new Button("Replace");
        replaceButton.getStyleClass().addAll("button", "danger", "replace-button");
        
        keepBothButton = new Button("Keep Both");
        keepBothButton.getStyleClass().addAll("button", "primary", "keep-both-button");
        
        skipButton = new Button("Skip");
        skipButton.getStyleClass().addAll("button", "ghost", "skip-button");
        
        conflictButtons.getChildren().addAll(replaceButton, keepBothButton, skipButton);
        conflictPanel.getChildren().addAll(conflictMessage, conflictButtons);
        
        mainContainer.getChildren().addAll(
            headerBar, browserContainer, saveControlsBar, optionsBar,
            buttonBar, progressContainer, conflictPanel
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
        saveControlsBar.setPadding(new Insets(12));
        buttonBar.setPadding(new Insets(12));
        
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
        fileListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null && newFile.isFile()) {
                fileNameField.setText(newFile.getName());
                updateSaveButtonState();
            }
        });
        
        // File name field
        fileNameField.textProperty().addListener((obs, oldText, newText) -> {
            updateSaveButtonState();
        });
        
        // File type filter
        fileTypeFilter.setOnAction(e -> {
            updateFileExtension();
            refreshFileList();
        });
        
        // Control buttons
        saveButton.setOnAction(e -> saveFile());
        cancelButton.setOnAction(e -> cancel());
        
        // Conflict resolution buttons
        replaceButton.setOnAction(e -> resolveConflict(ConflictResolution.REPLACE));
        keepBothButton.setOnAction(e -> resolveConflict(ConflictResolution.KEEP_BOTH));
        skipButton.setOnAction(e -> resolveConflict(ConflictResolution.SKIP));
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
     * Build directory tree (similar to ModernFileChooser)
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
                    rootTreeItem.getChildren().add(new TreeItem<>());
                    rootItem.getChildren().add(rootTreeItem);
                }
                
                return rootItem;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    directoryTree.setRoot(getValue());
                    setupTreeLazyLoading();
                });
            }
        };
        
        Thread treeThread = new Thread(treeTask);
        treeThread.setDaemon(true);
        treeThread.start();
    }
    
    /**
     * Setup lazy loading for tree
     */
    private void setupTreeLazyLoading() {
        directoryTree.setOnMouseClicked(event -> {
            TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null && !selectedItem.isExpanded()) {
                loadDirectoryChildren(selectedItem);
            }
        });
    }
    
    /**
     * Load children for directory tree item
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
                        if (file.isDirectory() && !file.isHidden()) {
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
                        
                        // Check for subdirectories
                        File[] subFiles = child.listFiles();
                        if (subFiles != null) {
                            for (File subFile : subFiles) {
                                if (subFile.isDirectory()) {
                                    childItem.getChildren().add(new TreeItem<>());
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
     * Navigate to directory
     */
    private void navigateToDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        currentDirectory = directory;
        refreshFileList();
        statusLabel.setText("Save location: " + directory.getAbsolutePath());
    }
    
    /**
     * Refresh file list
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
                        if (!file.isHidden() && (file.isDirectory() || matchesFileFilter(file))) {
                            files.add(file);
                        }
                    }
                }
                
                return files;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    fileListView.getItems().setAll(getValue());
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Check if file matches filter
     */
    private boolean matchesFileFilter(File file) {
        FileTypeFilter selectedFilter = fileTypeFilter.getValue();
        if (selectedFilter == null || selectedFilter.getExtensions().isEmpty()) {
            return true;
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
     * Update file extension based on selected filter
     */
    private void updateFileExtension() {
        FileTypeFilter selectedFilter = fileTypeFilter.getValue();
        if (selectedFilter == null || selectedFilter.getExtensions().isEmpty()) {
            return;
        }
        
        String currentName = fileNameField.getText().trim();
        if (currentName.isEmpty()) {
            return;
        }
        
        // Remove existing extension
        int lastDot = currentName.lastIndexOf('.');
        String baseName = lastDot > 0 ? currentName.substring(0, lastDot) : currentName;
        
        // Add new extension
        String newExtension = selectedFilter.getExtensions().get(0);
        if (!newExtension.startsWith(".")) {
            newExtension = "." + newExtension;
        }
        
        fileNameField.setText(baseName + newExtension);
    }
    
    /**
     * Update save button state
     */
    private void updateSaveButtonState() {
        boolean hasFileName = !fileNameField.getText().trim().isEmpty();
        saveButton.setDisable(!hasFileName);
    }
    
    /**
     * Save file
     */
    private void saveFile() {
        String fileName = fileNameField.getText().trim();
        if (fileName.isEmpty()) {
            return;
        }
        
        File targetFile = new File(currentDirectory, fileName);
        
        // Check for conflicts
        if (targetFile.exists() && !overwriteExisting.isSelected()) {
            showConflictResolution(targetFile);
            return;
        }
        
        // Proceed with save
        proceedWithSave(targetFile);
    }
    
    /**
     * Show conflict resolution dialog
     */
    private void showConflictResolution(File conflictingFile) {
        conflictResolutionMode = true;
        selectedFile = conflictingFile;
        
        conflictMessage.setText(String.format(
            "A file named '%s' already exists in this location.\\n\\n" +
            "Do you want to replace it with the file you're saving?",
            conflictingFile.getName()
        ));
        
        // Hide main controls and show conflict panel
        browserContainer.setVisible(false);
        saveControlsBar.setVisible(false);
        buttonBar.setVisible(false);
        conflictPanel.setVisible(true);
    }
    
    /**
     * Resolve file conflict
     */
    private void resolveConflict(ConflictResolution resolution) {
        switch (resolution) {
            case REPLACE:
                proceedWithSave(selectedFile);
                break;
            case KEEP_BOTH:
                File newFile = generateUniqueFileName(selectedFile);
                proceedWithSave(newFile);
                break;
            case SKIP:
                hideConflictResolution();
                break;
        }
    }
    
    /**
     * Generate unique file name
     */
    private File generateUniqueFileName(File originalFile) {
        String baseName = originalFile.getName();
        String extension = "";
        
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = baseName.substring(lastDot);
            baseName = baseName.substring(0, lastDot);
        }
        
        int counter = 1;
        File newFile;
        do {
            String newName = baseName + " (" + counter + ")" + extension;
            newFile = new File(originalFile.getParent(), newName);
            counter++;
        } while (newFile.exists());
        
        return newFile;
    }
    
    /**
     * Hide conflict resolution panel
     */
    private void hideConflictResolution() {
        conflictResolutionMode = false;
        conflictPanel.setVisible(false);
        browserContainer.setVisible(true);
        saveControlsBar.setVisible(true);
        buttonBar.setVisible(true);
    }
    
    /**
     * Proceed with save operation
     */
    private void proceedWithSave(File targetFile) {
        selectedFile = targetFile;
        
        SaveOptions options = new SaveOptions();
        options.setTargetFile(targetFile);
        options.setCreateBackup(createBackup.isSelected());
        options.setOverwriteExisting(overwriteExisting.isSelected());
        
        if (onSaveWithOptions != null) {
            onSaveWithOptions.accept(options);
        } else if (onFileSelected != null) {
            onFileSelected.accept(targetFile);
        }
        
        dialogStage.close();
    }
    
    /**
     * Show save progress
     */
    public void showProgress(String message, double progress) {
        Platform.runLater(() -> {
            progressContainer.setVisible(true);
            statusLabel.setText(message);
            saveProgress.setProgress(progress);
        });
    }
    
    /**
     * Hide save progress
     */
    public void hideProgress() {
        Platform.runLater(() -> {
            progressContainer.setVisible(false);
        });
    }
    
    /**
     * Cancel and close dialog
     */
    private void cancel() {
        selectedFile = null;
        if (onCancelled != null) {
            onCancelled.run();
        }
        dialogStage.close();
    }
    
    /**
     * Custom list cell for save dialog
     */
    private class SaveFileListCell extends ListCell<File> {
        private HBox content;
        private Label nameLabel;
        private Label typeLabel;
        
        public SaveFileListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(4, 8, 4, 8));
            
            nameLabel = new Label();
            nameLabel.getStyleClass().add("save-file-name");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            typeLabel = new Label();
            typeLabel.getStyleClass().add("save-file-type");
            
            content.getChildren().addAll(nameLabel, spacer, typeLabel);
        }
        
        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(file.getName());
                
                if (file.isDirectory()) {
                    typeLabel.setText("Folder");
                    getStyleClass().add("directory-cell");
                } else {
                    String extension = getFileExtension(file.getName());
                    typeLabel.setText(extension.isEmpty() ? "File" : extension.toUpperCase() + " File");
                    getStyleClass().remove("directory-cell");
                }
                
                setGraphic(content);
            }
        }
        
        private String getFileExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return (lastDot > 0 && lastDot < fileName.length() - 1) ? 
                   fileName.substring(lastDot + 1) : "";
        }
    }
    
    // Public API
    
    /**
     * Show the save dialog
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
        
        // Configure options visibility
        createBackup.setVisible(showBackupOption);
        
        updateSaveButtonState();
        dialogStage.showAndWait();
    }
    
    // Getters and Setters
    
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
    
    public void setOnFileSelected(Consumer<File> callback) {
        this.onFileSelected = callback;
    }
    
    public void setOnSaveWithOptions(Consumer<SaveOptions> callback) {
        this.onSaveWithOptions = callback;
    }
    
    public void setOnCancelled(Runnable callback) {
        this.onCancelled = callback;
    }
    
    public File getSelectedFile() {
        return selectedFile;
    }
    
    public void setAllowOverwrite(boolean allow) {
        this.allowOverwrite = allow;
        overwriteExisting.setVisible(allow);
    }
    
    public void setShowBackupOption(boolean show) {
        this.showBackupOption = show;
    }
    
    // Helper classes
    
    /**
     * Save options configuration
     */
    public static class SaveOptions {
        private File targetFile;
        private boolean createBackup;
        private boolean overwriteExisting;
        
        public File getTargetFile() { return targetFile; }
        public void setTargetFile(File targetFile) { this.targetFile = targetFile; }
        
        public boolean isCreateBackup() { return createBackup; }
        public void setCreateBackup(boolean createBackup) { this.createBackup = createBackup; }
        
        public boolean isOverwriteExisting() { return overwriteExisting; }
        public void setOverwriteExisting(boolean overwriteExisting) { this.overwriteExisting = overwriteExisting; }
    }
    
    /**
     * File type filter
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
        
        public String getDescription() { return description; }
        public List<String> getExtensions() { return extensions; }
        
        @Override
        public String toString() { return description; }
    }
    
    /**
     * Conflict resolution options
     */
    private enum ConflictResolution {
        REPLACE, KEEP_BOTH, SKIP
    }
}