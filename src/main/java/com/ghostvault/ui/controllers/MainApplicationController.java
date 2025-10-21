package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.*;
import com.ghostvault.ui.utils.UIUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integrated Main Application Controller - Complete UI System
 */
public class MainApplicationController {
    
    private Stage primaryStage;
    private Scene mainScene;
    private BorderPane rootLayout;
    
    // Core UI Components
    private ProfessionalHeader header;
    private EnhancedFileManager fileManager;
    private TabPane previewTabs;
    private CodePreviewComponent codePreview;
    private ImagePreviewComponent imagePreview;
    private DetailedFileInfoDisplay fileInfo;
    private DragDropFileUploader dragDropUploader;
    private ModernFileOperations fileOperations;
    private FileContextMenuManager contextMenuManager;
    
    // Mode controllers
    private Map<ModeController.VaultMode, ModeController> modeControllers;
    private ModeController currentModeController;
    private AuthenticationController authController;
    
    // Application state
    private ModeController.VaultMode currentMode = ModeController.VaultMode.MASTER;
    private boolean isInitialized = false;
    private File currentDirectory;
    
    public MainApplicationController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.currentDirectory = new File(System.getProperty("user.home"));
        initializeControllers();
        initializeComponents();
    }
    
    private void initializeControllers() {
        modeControllers = new HashMap<>();
        
        // Initialize mode controllers
        modeControllers.put(ModeController.VaultMode.MASTER, new MasterModeController(primaryStage));
        modeControllers.put(ModeController.VaultMode.PANIC, new PanicModeController(primaryStage));
        modeControllers.put(ModeController.VaultMode.DECOY, new DecoyModeController(primaryStage));
        
        // Initialize authentication controller
        authController = new AuthenticationController(primaryStage);
        
        // Set current mode controller
        currentModeController = modeControllers.get(currentMode);
    }
    
    private void initializeComponents() {
        // Initialize file operations
        fileOperations = new ModernFileOperations(primaryStage);
        
        // Initialize drag and drop
        dragDropUploader = new DragDropFileUploader();
        
        // Initialize context menu manager
        contextMenuManager = new FileContextMenuManager();
        setupContextMenuHandlers();
    }
    
    /**
     * Create the main integrated scene
     */
    public Scene createScene() {
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("main-application");
        
        // Create header
        header = createHeader();
        rootLayout.setTop(header);
        
        // Create main content with all integrated components
        SplitPane mainContent = createMainContent();
        rootLayout.setCenter(mainContent);
        
        // Create status bar
        HBox statusBar = createStatusBar();
        rootLayout.setBottom(statusBar);
        
        // Create scene
        mainScene = new Scene(rootLayout, 1400, 900);
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
        
        isInitialized = true;
        return mainScene;
    }
    
    private ProfessionalHeader createHeader() {
        ProfessionalHeader header = new ProfessionalHeader();
        header.setTitle("GhostVault");
        header.setUserInfo("Current User");
        header.setSessionInfo(currentMode.toString() + " Mode");
        
        // Add mode switch buttons to header
        Button masterBtn = new Button("Master");
        Button panicBtn = new Button("Panic");
        Button decoyBtn = new Button("Decoy");
        
        masterBtn.setOnAction(e -> switchMode(ModeController.VaultMode.MASTER));
        panicBtn.setOnAction(e -> switchMode(ModeController.VaultMode.PANIC));
        decoyBtn.setOnAction(e -> switchMode(ModeController.VaultMode.DECOY));
        
        masterBtn.getStyleClass().add("mode-button");
        panicBtn.getStyleClass().addAll("mode-button", "danger-button");
        decoyBtn.getStyleClass().addAll("mode-button", "secondary-button");
        
        HBox modeButtons = new HBox(5, masterBtn, panicBtn, decoyBtn);
        header.addToRight(modeButtons);
        
        return header;
    }
    
    private SplitPane createMainContent() {
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        
        // Left panel - File manager with drag/drop
        VBox leftPanel = createFileManagerPanel();
        
        // Center panel - Preview area
        VBox centerPanel = createPreviewPanel();
        
        // Right panel - File info and operations
        VBox rightPanel = createFileInfoPanel();
        
        mainSplitPane.getItems().addAll(leftPanel, centerPanel, rightPanel);
        mainSplitPane.setDividerPositions(0.35, 0.75);
        
        return mainSplitPane;
    }
    
    private VBox createFileManagerPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(400);
        panel.getStyleClass().add("file-manager-panel");
        
        // Panel title
        Label title = new Label("File Manager");
        title.getStyleClass().add("panel-title");
        
        // Drag and drop zone
        VBox dropZone = dragDropUploader.createDropZone();
        dragDropUploader.setOnFilesDropped(this::handleFilesDropped);
        
        // File manager with all features
        fileManager = new EnhancedFileManager();
        fileManager.loadDirectory(currentDirectory);
        
        panel.getChildren().addAll(title, dropZone, fileManager);
        VBox.setVgrow(fileManager, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createPreviewPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(600);
        panel.getStyleClass().add("preview-panel");
        
        // Panel title
        Label title = new Label("File Preview");
        title.getStyleClass().add("panel-title");
        
        // Preview tabs with all preview components
        previewTabs = new TabPane();
        previewTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Code preview tab
        Tab codeTab = new Tab("Code");
        codePreview = new CodePreviewComponent();
        codeTab.setContent(codePreview);
        
        // Image preview tab
        Tab imageTab = new Tab("Image");
        imagePreview = new ImagePreviewComponent();
        imageTab.setContent(imagePreview);
        
        // Media preview tab
        Tab mediaTab = new Tab("Media");
        Label mediaPlaceholder = new Label("Audio/Video preview will be shown here");
        mediaPlaceholder.getStyleClass().add("placeholder-text");
        ScrollPane mediaScroll = new ScrollPane(mediaPlaceholder);
        mediaScroll.setFitToWidth(true);
        mediaScroll.setFitToHeight(true);
        mediaTab.setContent(mediaScroll);
        
        previewTabs.getTabs().addAll(codeTab, imageTab, mediaTab);
        
        panel.getChildren().addAll(title, previewTabs);
        VBox.setVgrow(previewTabs, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createFileInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);
        panel.getStyleClass().add("file-info-panel");
        
        // Panel title
        Label title = new Label("File Information");
        title.getStyleClass().add("panel-title");
        
        // File info display with thumbnails
        fileInfo = new DetailedFileInfoDisplay();
        
        // Operations panel with all file operations
        VBox operationsPanel = createOperationsPanel();
        
        panel.getChildren().addAll(title, fileInfo, operationsPanel);
        VBox.setVgrow(fileInfo, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createOperationsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.getStyleClass().add("operations-panel");
        
        Label title = new Label("Operations");
        title.getStyleClass().add("section-title");
        
        // File operation buttons
        Button uploadBtn = createOperationButton("Upload Files", "upload-icon", this::handleUploadFiles);
        Button downloadBtn = createOperationButton("Download Selected", "download-icon", this::handleDownloadSelected);
        Button deleteBtn = createOperationButton("Delete Selected", "delete-icon", this::handleDeleteSelected);
        deleteBtn.getStyleClass().add("danger-button");
        
        // Backup operation buttons
        Button backupBtn = createOperationButton("Create Backup", "backup-icon", this::handleCreateBackup);
        Button restoreBtn = createOperationButton("Restore Backup", "restore-icon", this::handleRestoreBackup);
        
        // Add tooltips with keyboard shortcuts
        TooltipManager.install(uploadBtn, "Upload files to the vault", "Ctrl+U");
        TooltipManager.install(downloadBtn, "Download selected files", "Ctrl+S");
        TooltipManager.install(deleteBtn, "Delete selected files", "Delete");
        TooltipManager.install(backupBtn, "Create encrypted backup", "Ctrl+B");
        TooltipManager.install(restoreBtn, "Restore from backup", "Ctrl+R");
        
        panel.getChildren().addAll(title, uploadBtn, downloadBtn, deleteBtn, 
            new Separator(), backupBtn, restoreBtn);
        
        return panel;
    }
    
    private Button createOperationButton(String text, String iconClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("operation-button", "primary-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.getStyleClass().add("status-bar");
        
        // Status indicators
        StatusIndicatorBadge modeIndicator = new StatusIndicatorBadge(
            currentMode.toString() + " Mode", StatusIndicatorBadge.BadgeType.SUCCESS);
        StatusIndicatorBadge securityIndicator = new StatusIndicatorBadge(
            "Secure", StatusIndicatorBadge.BadgeType.INFO);
        
        // File count indicator
        Label fileCountLabel = new Label("0 files");
        fileCountLabel.getStyleClass().add("file-count-label");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Memory usage
        Label memoryLabel = new Label(UIUtils.MemoryUtils.formatMemoryUsage());
        memoryLabel.getStyleClass().add("memory-label");
        
        // Version info
        Label versionLabel = new Label("GhostVault v2.0");
        versionLabel.getStyleClass().add("version-label");
        
        statusBar.getChildren().addAll(modeIndicator, securityIndicator, fileCountLabel, 
            spacer, memoryLabel, versionLabel);
        
        return statusBar;
    }
    
    private void setupContextMenuHandlers() {
        contextMenuManager.setOnFilePreview(this::previewFile);
        contextMenuManager.setOnFileOpen(this::openFile);
        contextMenuManager.setOnFilesDelete(this::deleteFiles);
        contextMenuManager.setOnFileRename(this::renameFile);
        contextMenuManager.setOnFileProperties(this::showFileProperties);
        contextMenuManager.setOnRefresh(this::refreshFileManager);
    }
    
    private void setupKeyboardShortcuts() {
        KeyboardShortcutManager.register("upload", javafx.scene.input.KeyCode.U, this::handleUploadFiles);
        KeyboardShortcutManager.register("download", javafx.scene.input.KeyCode.S, this::handleDownloadSelected);
        KeyboardShortcutManager.register("delete", javafx.scene.input.KeyCode.DELETE, this::handleDeleteSelected);
        KeyboardShortcutManager.register("backup", javafx.scene.input.KeyCode.B, this::handleCreateBackup);
        KeyboardShortcutManager.register("restore", javafx.scene.input.KeyCode.R, this::handleRestoreBackup);
        KeyboardShortcutManager.register("refresh", javafx.scene.input.KeyCode.F5, this::refreshFileManager);
        KeyboardShortcutManager.register("search", javafx.scene.input.KeyCode.F, () -> {
            // Focus on search field in file manager
        });
    }
    
    // Event Handlers
    private void handleFilesDropped(java.util.List<File> files) {
        NotificationSystem.showInfo("Files Dropped", 
            "Processing " + files.size() + " dropped files...");
        
        fileOperations.uploadFiles(files, currentDirectory, result -> {
            if (result.hasErrors()) {
                NotificationSystem.showWarning("Upload Issues", 
                    result.failureCount + " files failed to upload");
            } else {
                NotificationSystem.showSuccess("Upload Complete", 
                    "Successfully uploaded " + result.successCount + " files");
            }
            refreshFileManager();
        });
    }
    
    private void handleUploadFiles() {
        fileOperations.showFileUploadDialog().thenAccept(files -> {
            if (!files.isEmpty()) {
                fileOperations.uploadFiles(files, currentDirectory, result -> {
                    refreshFileManager();
                });
            }
        });
    }
    
    private void handleDownloadSelected() {
        var selectedFiles = fileManager.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            NotificationSystem.showWarning("No Selection", "Please select files to download");
            return;
        }
        
        if (selectedFiles.size() == 1) {
            // Single file download
            File file = selectedFiles.iterator().next();
            String extension = UIUtils.getFileExtension(file);
            fileOperations.showFileSaveDialog(file.getName(), extension)
                .thenAccept(targetFile -> {
                    if (targetFile != null) {
                        fileOperations.downloadFile(file, targetFile, success -> {
                            if (success) {
                                NotificationSystem.showSuccess("Download Complete", 
                                    "File downloaded successfully");
                            }
                        });
                    }
                });
        } else {
            // Multiple file download
            fileOperations.showDirectoryChooser("Select Download Location")
                .thenAccept(targetDirectory -> {
                    if (targetDirectory != null) {
                        for (File file : selectedFiles) {
                            File targetFile = new File(targetDirectory, file.getName());
                            fileOperations.downloadFile(file, targetFile, success -> {
                                // Individual file completion handled by notifications
                            });
                        }
                    }
                });
        }
    }
    
    private void handleDeleteSelected() {
        var selectedFiles = fileManager.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            NotificationSystem.showWarning("No Selection", "Please select files to delete");
            return;
        }
        
        java.util.List<File> filesToDelete = new java.util.ArrayList<>(selectedFiles);
        
        fileOperations.secureDeleteFiles(filesToDelete, result -> {
            if (result.hasErrors()) {
                NotificationSystem.showWarning("Deletion Issues", 
                    result.failureCount + " files could not be deleted");
            } else {
                NotificationSystem.showSuccess("Deletion Complete", 
                    "Successfully deleted " + result.successCount + " files");
            }
            refreshFileManager();
        });
    }
    
    private void handleCreateBackup() {
        EncryptedBackupManager backupManager = new EncryptedBackupManager(primaryStage);
        backupManager.showBackupDialog(currentDirectory);
    }
    
    private void handleRestoreBackup() {
        EncryptedBackupManager backupManager = new EncryptedBackupManager(primaryStage);
        backupManager.showRestoreDialog(currentDirectory);
    }
    
    private void previewFile(File file) {
        if (file == null) return;
        
        // Update file info
        fileInfo.displayFileInfo(file);
        
        // Determine preview type and show appropriate tab
        String extension = UIUtils.getFileExtension(file);
        
        if (UIUtils.isCodeFile(file)) {
            codePreview.loadFile(file);
            previewTabs.getSelectionModel().select(0); // Code tab
        } else if (UIUtils.isImageFile(file)) {
            imagePreview.loadImage(file);
            previewTabs.getSelectionModel().select(1); // Image tab
        } else if (UIUtils.isVideoFile(file) || UIUtils.isAudioFile(file)) {
            previewTabs.getSelectionModel().select(2); // Media tab
            NotificationSystem.showInfo("Media Preview", "Media file selected: " + file.getName());
        } else {
            NotificationSystem.showInfo("File Selected", "File type not supported for preview");
        }
    }
    
    private void openFile(File file) {
        previewFile(file);
        if (file.isDirectory()) {
            currentDirectory = file;
            fileManager.loadDirectory(file);
            NotificationSystem.showInfo("Directory Changed", "Opened: " + file.getName());
        }
    }
    
    private void deleteFiles(java.util.List<File> files) {
        fileOperations.secureDeleteFiles(files, result -> {
            refreshFileManager();
        });
    }
    
    private void renameFile(File file) {
        TextInputDialog dialog = new TextInputDialog(file.getName());
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Enter new name:");
        dialog.setContentText("Name:");
        
        dialog.showAndWait().ifPresent(newName -> {
            File newFile = new File(file.getParent(), newName);
            if (file.renameTo(newFile)) {
                NotificationSystem.showSuccess("Rename", "File renamed successfully");
                refreshFileManager();
            } else {
                NotificationSystem.showError("Rename Failed", "Could not rename file");
            }
        });
    }
    
    private void showFileProperties(File file) {
        fileInfo.displayFileInfo(file);
        NotificationSystem.showInfo("Properties", "File properties displayed in info panel");
    }
    
    private void refreshFileManager() {
        if (fileManager != null && currentDirectory != null) {
            fileManager.loadDirectory(currentDirectory);
        }
    }
    
    /**
     * Switch to a different vault mode
     */
    public void switchMode(ModeController.VaultMode newMode) {
        if (newMode == currentMode) return;
        
        try {
            // Cleanup current mode
            if (currentModeController != null) {
                currentModeController.cleanup();
            }
            
            // Switch to new mode
            currentMode = newMode;
            currentModeController = modeControllers.get(newMode);
            
            if (currentModeController != null) {
                currentModeController.initialize();
                
                // Update header
                header.setSessionInfo(newMode.toString() + " Mode");
                
                // Update window title
                primaryStage.setTitle(currentModeController.getWindowTitle());
                
                NotificationSystem.showInfo("Mode Switch", 
                    "Switched to " + newMode.toString().toLowerCase() + " mode");
            }
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to switch mode", e, 
                ErrorHandlingSystem.ErrorSeverity.CRITICAL);
        }
    }
    
    /**
     * Authenticate user and determine mode
     */
    public void authenticate(String password) {
        try {
            ModeController.VaultMode detectedMode = authController.authenticate(password);
            switchMode(detectedMode);
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Authentication failed", e, 
                ErrorHandlingSystem.ErrorSeverity.WARNING);
        }
    }
    
    /**
     * Emergency shutdown
     */
    public void emergencyShutdown() {
        try {
            PanicModeController panicController = 
                (PanicModeController) modeControllers.get(ModeController.VaultMode.PANIC);
            
            if (panicController != null) {
                panicController.emergencyShutdown();
            }
            
            Platform.exit();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Emergency shutdown failed", e, 
                ErrorHandlingSystem.ErrorSeverity.CRITICAL);
            System.exit(1);
        }
    }
    
    /**
     * Graceful shutdown
     */
    public void shutdown() {
        try {
            // Cleanup all mode controllers
            for (ModeController controller : modeControllers.values()) {
                if (controller != null) {
                    controller.cleanup();
                }
            }
            
            // Cleanup authentication controller
            if (authController != null) {
                authController.cleanup();
            }
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Shutdown error", e, 
                ErrorHandlingSystem.ErrorSeverity.WARNING);
        }
    }
    
    // Getters
    public Scene getMainScene() { return mainScene; }
    public Stage getPrimaryStage() { return primaryStage; }
    public ModeController.VaultMode getCurrentMode() { return currentMode; }
    public ModeController getCurrentModeController() { return currentModeController; }
    public AuthenticationController getAuthenticationController() { return authController; }
    public boolean isInitialized() { return isInitialized; }
    
    public ModeController getModeController(ModeController.VaultMode mode) {
        return modeControllers.get(mode);
    }
    
    /**
     * Set mode change handler for backend integration
     */
    public void setModeChangeHandler(ModeChangeHandler handler) {
        // This would be implemented to use the handler for mode changes
    }
}