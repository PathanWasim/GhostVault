package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Controller for Master Mode - full vault functionality
 */
public class MasterModeController extends ModeController {
    
    // UI Components
    private ProfessionalHeader headerComponent;
    private HBox mainContentArea;
    private VBox leftSidebar;
    private VBox centerContent;
    private VBox rightSidebar;
    
    // File management components
    private VirtualizedFileListView fileListView;
    private FileSearchAndFilterSystem searchSystem;
    private ResizablePreviewPane previewPane;
    private DetailedFileInfoPane fileInfoPane;
    
    // Action components
    private BatchOperationsBar batchOperationsBar;
    private ModernContextMenu contextMenu;
    
    // Current state
    private File currentDirectory;
    private List<File> selectedFiles;
    
    public MasterModeController(Stage primaryStage) {
        super(primaryStage, VaultMode.MASTER);
    }
    
    @Override
    public void initialize() {
        if (initialized) return;
        
        try {
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            loadInitialData();
            
            initialized = true;
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to initialize Master Mode", e);
        }
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Header
        headerComponent = new ProfessionalHeader();
        headerComponent.setTitle("GhostVault - Master Mode");
        headerComponent.setUserInfo("Administrator");
        headerComponent.setSessionInfo("Secure Session Active");
        
        // Main content area
        mainContentArea = new HBox();
        mainContentArea.getStyleClass().add("main-content-area");
        
        // Left sidebar
        leftSidebar = new VBox(8);
        leftSidebar.getStyleClass().add("left-sidebar");
        leftSidebar.setPrefWidth(250);
        leftSidebar.setMinWidth(200);
        
        // Center content
        centerContent = new VBox();
        centerContent.getStyleClass().add("center-content");
        HBox.setHgrow(centerContent, Priority.ALWAYS);
        
        // Right sidebar
        rightSidebar = new VBox(8);
        rightSidebar.getStyleClass().add("right-sidebar");
        rightSidebar.setPrefWidth(300);
        rightSidebar.setMinWidth(250);
        
        // File management components
        searchSystem = new FileSearchAndFilterSystem();
        fileListView = new VirtualizedFileListView();
        previewPane = new ResizablePreviewPane();
        fileInfoPane = new DetailedFileInfoPane();
        
        // Batch operations
        batchOperationsBar = new BatchOperationsBar();
        
        // Setup sidebar content
        setupLeftSidebar();
        setupCenterContent();
        setupRightSidebar();
        
        mainContentArea.getChildren().addAll(leftSidebar, centerContent, rightSidebar);
        
        // Add to root container
        rootContainer.getChildren().addAll(headerComponent, mainContentArea);
        VBox.setVgrow(mainContentArea, Priority.ALWAYS);
    }
    
    /**
     * Setup left sidebar with navigation and quick actions
     */
    private void setupLeftSidebar() {
        Label navLabel = new Label("Navigation");
        navLabel.getStyleClass().add("sidebar-section-title");
        
        // Quick navigation buttons
        VBox navButtons = new VBox(4);
        navButtons.getStyleClass().add("nav-buttons");
        
        Button homeButton = createNavButton("🏠 Home", "Navigate to home directory");
        Button documentsButton = createNavButton("📁 Documents", "Navigate to documents");
        Button downloadsButton = createNavButton("⬇️ Downloads", "Navigate to downloads");
        Button desktopButton = createNavButton("🖥️ Desktop", "Navigate to desktop");
        
        navButtons.getChildren().addAll(homeButton, documentsButton, downloadsButton, desktopButton);
        
        // Quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.getStyleClass().add("sidebar-section-title");
        
        VBox actionButtons = new VBox(4);
        actionButtons.getStyleClass().add("action-buttons");
        
        Button uploadButton = createActionButton("📤 Upload Files", "Upload files to vault");
        Button createFolderButton = createActionButton("📁 New Folder", "Create new folder");
        Button backupButton = createActionButton("💾 Create Backup", "Create encrypted backup");
        Button restoreButton = createActionButton("📥 Restore Backup", "Restore from backup");
        
        actionButtons.getChildren().addAll(uploadButton, createFolderButton, backupButton, restoreButton);
        
        // Vault statistics
        Label statsLabel = new Label("Vault Statistics");
        statsLabel.getStyleClass().add("sidebar-section-title");
        
        VBox statsContainer = new VBox(4);
        statsContainer.getStyleClass().add("stats-container");
        
        Label totalFilesLabel = new Label("Total Files: 0");
        totalFilesLabel.getStyleClass().add("stat-item");
        
        Label totalSizeLabel = new Label("Total Size: 0 MB");
        totalSizeLabel.getStyleClass().add("stat-item");
        
        Label lastBackupLabel = new Label("Last Backup: Never");
        lastBackupLabel.getStyleClass().add("stat-item");
        
        statsContainer.getChildren().addAll(totalFilesLabel, totalSizeLabel, lastBackupLabel);
        
        leftSidebar.getChildren().addAll(
            navLabel, navButtons,
            new Separator(),
            actionsLabel, actionButtons,
            new Separator(),
            statsLabel, statsContainer
        );
    }
    
    /**
     * Setup center content with file list and search
     */
    private void setupCenterContent() {
        // Search and filter system
        searchSystem.setOnResultsChanged(this::updateFileList);
        
        // Batch operations bar
        batchOperationsBar.setOnSelectAll(() -> fileListView.selectAll());
        batchOperationsBar.setOnDeselectAll(() -> fileListView.clearSelection());
        batchOperationsBar.setOnDownload(this::downloadFiles);
        batchOperationsBar.setOnDelete(this::deleteFiles);
        batchOperationsBar.setOnMove(this::moveFiles);
        batchOperationsBar.setOnCopy(this::copyFiles);
        
        // File list view
        fileListView.setOnFileSelected(this::selectFile);
        fileListView.setOnFileDoubleClicked(this::openFile);
        fileListView.setOnSelectionChanged(this::updateSelection);
        
        centerContent.getChildren().addAll(searchSystem, batchOperationsBar, fileListView);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
    }
    
    /**
     * Setup right sidebar with preview and file info
     */
    private void setupRightSidebar() {
        Label previewLabel = new Label("Preview");
        previewLabel.getStyleClass().add("sidebar-section-title");
        
        Label infoLabel = new Label("File Information");
        infoLabel.getStyleClass().add("sidebar-section-title");
        
        rightSidebar.getChildren().addAll(
            previewLabel, previewPane,
            new Separator(),
            infoLabel, fileInfoPane
        );
        
        VBox.setVgrow(previewPane, Priority.ALWAYS);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        rootContainer.setSpacing(0);
        rootContainer.setPadding(new Insets(0));
        
        leftSidebar.setPadding(new Insets(12));
        centerContent.setPadding(new Insets(12));
        rightSidebar.setPadding(new Insets(12));
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Header events - TODO: Add these methods to ProfessionalHeader
        // headerComponent.setOnMenuAction(this::showMainMenu);
        // headerComponent.setOnSettingsAction(this::showSettings);
        // headerComponent.setOnLogoutAction(this::logout);
        // headerComponent.setOnExitAction(this::exitApplication);
    }
    
    /**
     * Load initial data
     */
    private void loadInitialData() {
        // Set initial directory
        currentDirectory = new File(System.getProperty("user.home"));
        navigateToDirectory(currentDirectory);
    }
    
    /**
     * Navigate to a directory
     */
    private void navigateToDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        currentDirectory = directory;
        
        // Load files in directory
        File[] files = directory.listFiles();
        if (files != null) {
            searchSystem.setFiles(List.of(files));
        }
        
        // Update header with current path - TODO: Add setCurrentPath method to ProfessionalHeader
        // headerComponent.setCurrentPath(directory.getAbsolutePath());
    }
    
    /**
     * Update file list with search results
     */
    private void updateFileList(List<File> files) {
        fileListView.setFiles(files);
    }
    
    /**
     * Handle file selection
     */
    private void selectFile(File file) {
        if (file != null) {
            // Update preview - TODO: Implement proper file preview logic
            // previewPane.previewFile(file);
            
            // Update file info - TODO: Check correct method name
            // fileInfoPane.displayFile(file);
        }
    }
    
    /**
     * Handle file double-click (open)
     */
    private void openFile(File file) {
        if (file.isDirectory()) {
            navigateToDirectory(file);
        } else {
            // Open file with system default application
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
                ErrorHandlingSystem.handleError("Failed to open file", e);
            }
        }
    }
    
    /**
     * Update selection state
     */
    private void updateSelection() {
        File selectedFile = fileListView.getSelectedFile();
        selectedFiles = selectedFile != null ? List.of(selectedFile) : List.of();
        
        // Update batch operations bar
        batchOperationsBar.updateSelection(selectedFiles);
    }
    
    // Action handlers
    
    private Button createNavButton(String text, String tooltip) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "ghost", "nav-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
    
    private Button createActionButton(String text, String tooltip) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "primary", "action-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
    
    private void showMainMenu() {
        // Show main application menu
    }
    
    private void showSettings() {
        // Show settings dialog
    }
    
    private void logout() {
        // Switch to authentication mode
        switchMode(VaultMode.MASTER); // This would trigger re-authentication
    }
    
    private void downloadFiles(List<Object> files) {
        // Handle file download - TODO: Implement proper download functionality
        System.out.println("Download files: " + files.size() + " files");
    }
    
    private void deleteFiles(List<Object> files) {
        // Handle secure file deletion
        List<File> fileList = files.stream()
            .filter(File.class::isInstance)
            .map(File.class::cast)
            .toList();
        
        SecureDeleteDialog deleteDialog = new SecureDeleteDialog();
        deleteDialog.showDialog(primaryStage, fileList);
    }
    
    private void moveFiles(List<Object> files) {
        // Handle file move operation
    }
    
    private void copyFiles(List<Object> files) {
        // Handle file copy operation
    }
    
    @Override
    public void activate() {
        if (!initialized) {
            initialize();
        }
        
        // Update security indicators
        updateSecurityIndicators();
        
        // Refresh file list
        if (currentDirectory != null) {
            navigateToDirectory(currentDirectory);
        }
        
        show();
    }
    
    @Override
    public void deactivate() {
        // Clear sensitive data from UI
        fileListView.clear();
        previewPane.clear();
        // fileInfoPane.displayFile(null); // TODO: Check correct method name
    }
    
    @Override
    public void emergencyShutdown() {
        try {
            // Clear all sensitive data immediately
            deactivate();
            
            // Clear search history
            searchSystem.clearAllFilters();
            
            // Force garbage collection
            System.gc();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Error during emergency shutdown", e);
        }
    }
    
    @Override
    protected void updateSecurityIndicators() {
        if (headerComponent != null) {
            // TODO: Add setSecurityLevel and setEncryptionStatus methods to ProfessionalHeader
            // headerComponent.setSecurityLevel(secureMode ? "High Security" : "Standard");
            // headerComponent.setEncryptionStatus(secureMode ? "AES-256 Enabled" : "Basic Protection");
        }
    }
    
    @Override
    protected void cleanup() {
        try {
            // Save any pending changes
            // Clear caches
            if (previewPane != null) {
                previewPane.clear();
            }
            
            // Clear file list
            if (fileListView != null) {
                fileListView.clear();
            }
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Error during cleanup", e);
        }
    }
    
    @Override
    protected String getWindowTitle() {
        return "GhostVault - Master Mode";
    }
    
    @Override
    public void onAuthenticationSuccess(String password) {
        // Enable full functionality
        setSecureMode(true);
        
        // Load user's vault data
        loadInitialData();
    }
    
    @Override
    public void onAuthenticationFailure() {
        // Handle authentication failure
        ErrorHandlingSystem.showWarningDialog("Authentication Failed", 
            "Invalid credentials for Master Mode access.");
    }
    
    // Event handler methods
    private void showMainMenu() {
        // TODO: Implement main menu
        System.out.println("Show main menu");
    }
    
    private void showSettings() {
        // TODO: Implement settings dialog
        System.out.println("Show settings");
    }
    
    private void logout() {
        // TODO: Implement logout functionality
        System.out.println("Logout");
    }
    
    private void exitApplication() {
        // TODO: Implement exit functionality
        System.out.println("Exit application");
    }
}