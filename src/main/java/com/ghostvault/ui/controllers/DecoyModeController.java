package com.ghostvault.ui.controllers;

import com.ghostvault.core.DecoyManager;
import com.ghostvault.ui.components.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller for Decoy Mode - fake vault with convincing dummy data
 */
public class DecoyModeController extends ModeController {
    
    // UI Components (similar to Master Mode but with fake data)
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
    
    // Decoy data management
    private DecoyManager decoyManager;
    private List<File> fakeFiles = new ArrayList<>();
    private File fakeCurrentDirectory;
    private Random random = new Random();
    
    // Decoy behavior configuration
    private boolean simulateSlowOperations = true;
    private boolean showFakeErrors = true;
    private int fakeFileCount = 50;
    
    public DecoyModeController(Stage primaryStage) {
        super(primaryStage, VaultMode.DECOY);
        // Initialize DecoyManager with default paths
        java.nio.file.Path realVaultPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault", "real");
        java.nio.file.Path decoyVaultPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault", "decoy");
        this.decoyManager = new DecoyManager(realVaultPath, decoyVaultPath);
    }
    
    @Override
    public void initialize() {
        if (initialized) return;
        
        try {
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            generateFakeData();
            
            initialized = true;
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to initialize Decoy Mode", e);
        }
    }
    
    /**
     * Initialize UI components (similar to Master Mode)
     */
    private void initializeComponents() {
        // Header with fake information
        headerComponent = new ProfessionalHeader();
        headerComponent.setTitle("GhostVault - Personal Files");
        headerComponent.setUserInfo("User");
        headerComponent.setSessionInfo("Standard Session");
        
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
        
        // Setup components with fake data
        setupLeftSidebar();
        setupCenterContent();
        setupRightSidebar();
        
        mainContentArea.getChildren().addAll(leftSidebar, centerContent, rightSidebar);
        
        // Add to root container
        rootContainer.getChildren().addAll(headerComponent, mainContentArea);
        VBox.setVgrow(mainContentArea, Priority.ALWAYS);
    }
    
    /**
     * Setup left sidebar with fake navigation and statistics
     */
    private void setupLeftSidebar() {
        Label navLabel = new Label("Navigation");
        navLabel.getStyleClass().add("sidebar-section-title");
        
        // Fake navigation buttons
        VBox navButtons = new VBox(4);
        navButtons.getStyleClass().add("nav-buttons");
        
        Button homeButton = createFakeNavButton("🏠 Home", "Navigate to home directory");
        Button documentsButton = createFakeNavButton("📁 Documents", "Navigate to documents");
        Button picturesButton = createFakeNavButton("🖼️ Pictures", "Navigate to pictures");
        Button musicButton = createFakeNavButton("🎵 Music", "Navigate to music");
        
        navButtons.getChildren().addAll(homeButton, documentsButton, picturesButton, musicButton);
        
        // Fake quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.getStyleClass().add("sidebar-section-title");
        
        VBox actionButtons = new VBox(4);
        actionButtons.getStyleClass().add("action-buttons");
        
        Button uploadButton = createFakeActionButton("📤 Upload Files", "Upload files");
        Button createFolderButton = createFakeActionButton("📁 New Folder", "Create new folder");
        Button shareButton = createFakeActionButton("🔗 Share", "Share files");
        Button syncButton = createFakeActionButton("🔄 Sync", "Sync with cloud");
        
        actionButtons.getChildren().addAll(uploadButton, createFolderButton, shareButton, syncButton);
        
        // Fake statistics
        Label statsLabel = new Label("Storage Statistics");
        statsLabel.getStyleClass().add("sidebar-section-title");
        
        VBox statsContainer = new VBox(4);
        statsContainer.getStyleClass().add("stats-container");
        
        Label totalFilesLabel = new Label("Total Files: " + fakeFileCount);
        totalFilesLabel.getStyleClass().add("stat-item");
        
        Label totalSizeLabel = new Label("Total Size: " + generateFakeSize());
        totalSizeLabel.getStyleClass().add("stat-item");
        
        Label freeSpaceLabel = new Label("Free Space: " + generateFakeFreeSpace());
        freeSpaceLabel.getStyleClass().add("stat-item");
        
        statsContainer.getChildren().addAll(totalFilesLabel, totalSizeLabel, freeSpaceLabel);
        
        leftSidebar.getChildren().addAll(
            navLabel, navButtons,
            new Separator(),
            actionsLabel, actionButtons,
            new Separator(),
            statsLabel, statsContainer
        );
    }
    
    /**
     * Setup center content with fake file operations
     */
    private void setupCenterContent() {
        // Search system with fake results
        searchSystem.setOnResultsChanged(this::updateFakeFileList);
        
        // Batch operations with fake handlers
        batchOperationsBar.setOnSelectAll(() -> fileListView.getSelectionModel().selectAll());
        batchOperationsBar.setOnDeselectAll(() -> fileListView.getSelectionModel().clearSelection());
        batchOperationsBar.setOnDownload(this::simulateDownload);
        batchOperationsBar.setOnDelete(this::simulateDelete);
        batchOperationsBar.setOnMove(this::simulateMove);
        batchOperationsBar.setOnCopy(this::simulateCopy);
        
        // File list with fake selection handlers
        fileListView.setOnFileSelected(this::selectFakeFile);
        fileListView.setOnFileDoubleClicked(this::openFakeFile);
        fileListView.setOnSelectionChanged(this::updateFakeSelection);
        
        centerContent.getChildren().addAll(searchSystem, batchOperationsBar, fileListView);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
    }
    
    /**
     * Setup right sidebar with fake preview
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
     * Setup event handlers with fake responses
     */
    private void setupEventHandlers() {
        // Header events with fake responses - TODO: Add these methods to ProfessionalHeader
        // headerComponent.setOnMenuAction(this::showFakeMenu);
        // headerComponent.setOnSettingsAction(this::showFakeSettings);
        // headerComponent.setOnLogoutAction(this::fakeLogout);
        // headerComponent.setOnExitAction(this::exitApplication);
    }
    
    /**
     * Generate fake data that looks convincing
     */
    private void generateFakeData() {
        fakeFiles.clear();
        
        // Create fake directory structure
        fakeCurrentDirectory = new File(System.getProperty("user.home"), "Documents");
        
        // Generate fake files with realistic names
        String[] fakeFileNames = {
            "Resume_2024.pdf", "Budget_Spreadsheet.xlsx", "Vacation_Photos.zip",
            "Meeting_Notes.docx", "Project_Proposal.pptx", "Tax_Documents_2023.pdf",
            "Family_Pictures", "Work_Files", "Downloads", "Music_Collection",
            "Recipe_Collection.txt", "Book_List.txt", "Shopping_List.docx",
            "Insurance_Papers.pdf", "Bank_Statements", "Medical_Records",
            "Backup_Files", "Old_Photos", "Software_Installers", "Temp_Files"
        };
        
        String[] fakeExtensions = {".txt", ".pdf", ".docx", ".xlsx", ".jpg", ".png", ".mp3", ".mp4", ".zip"};
        
        for (int i = 0; i < fakeFileCount; i++) {
            String fileName;
            if (i < fakeFileNames.length) {
                fileName = fakeFileNames[i];
            } else {
                fileName = "Document_" + (i - fakeFileNames.length + 1) + 
                          fakeExtensions[random.nextInt(fakeExtensions.length)];
            }
            
            // Create fake file objects (these don't actually exist on disk)
            File fakeFile = decoyManager.createFakeFile(fakeCurrentDirectory, fileName);
            fakeFiles.add(fakeFile);
        }
    }
    
    /**
     * Update fake file list
     */
    private void updateFakeFileList(List<File> searchResults) {
        // In decoy mode, always show fake files regardless of search
        fileListView.setFiles(fakeFiles);
    }
    
    /**
     * Handle fake file selection
     */
    private void selectFakeFile(File file) {
        if (file != null) {
            // Show fake preview
            previewPane.showFakePreview(file);
            
            // Show fake file info
            fileInfoPane.displayFile(file);
        }
    }
    
    /**
     * Handle fake file opening
     */
    private void openFakeFile(File file) {
        if (file.getName().contains("Folder") || file.getName().contains("Files")) {
            // Simulate navigating to a folder
            simulateDirectoryNavigation(file);
        } else {
            // Simulate opening a file
            simulateFileOpen(file);
        }
    }
    
    /**
     * Update fake selection
     */
    private void updateFakeSelection() {
        File selectedFile = fileListView.getSelectedFile();
        List<File> selectedFiles = selectedFile != null ? List.of(selectedFile) : List.of();
        
        // Update batch operations bar
        batchOperationsBar.updateSelection(selectedFiles);
    }
    
    /**
     * Simulate directory navigation
     */
    private void simulateDirectoryNavigation(File directory) {
        // Show fake loading
        simulateOperation("Opening folder...", () -> {
            // Generate new fake files for this "directory"
            generateFakeSubdirectory(directory);
            headerComponent.setCurrentPath(directory.getAbsolutePath());
        });
    }
    
    /**
     * Generate fake subdirectory contents
     */
    private void generateFakeSubdirectory(File parentDir) {
        fakeFiles.clear();
        
        String[] subFiles = {
            "Subfolder_1", "Subfolder_2", "Important_Document.pdf",
            "Notes.txt", "Presentation.pptx", "Data_File.xlsx",
            "Image_001.jpg", "Image_002.jpg", "Archive.zip"
        };
        
        for (String fileName : subFiles) {
            File fakeFile = decoyManager.createFakeFile(parentDir, fileName);
            fakeFiles.add(fakeFile);
        }
        
        fileListView.setFiles(fakeFiles);
    }
    
    /**
     * Simulate file opening
     */
    private void simulateFileOpen(File file) {
        simulateOperation("Opening file...", () -> {
            // Show fake "file opened" message
            showFakeSuccessMessage("File opened successfully");
        });
    }
    
    // Fake operation handlers
    
    private void simulateDownload(List<Object> files) {
        simulateOperation("Downloading files...", () -> {
            showFakeSuccessMessage(files.size() + " file(s) downloaded successfully");
        });
    }
    
    private void simulateDelete(List<Object> files) {
        simulateOperation("Deleting files...", () -> {
            // Remove files from fake list
            fakeFiles.removeIf(f -> files.contains(f));
            fileListView.setFiles(fakeFiles);
            showFakeSuccessMessage(files.size() + " file(s) deleted successfully");
        });
    }
    
    private void simulateMove(List<Object> files) {
        simulateOperation("Moving files...", () -> {
            showFakeSuccessMessage(files.size() + " file(s) moved successfully");
        });
    }
    
    private void simulateCopy(List<Object> files) {
        simulateOperation("Copying files...", () -> {
            showFakeSuccessMessage(files.size() + " file(s) copied successfully");
        });
    }
    
    /**
     * Simulate an operation with fake delay and progress
     */
    private void simulateOperation(String message, Runnable onComplete) {
        if (!simulateSlowOperations) {
            onComplete.run();
            return;
        }
        
        // Show fake progress
        Platform.runLater(() -> {
            headerComponent.setSessionInfo(message);
        });
        
        // Simulate delay
        new Thread(() -> {
            try {
                Thread.sleep(1000 + random.nextInt(2000)); // 1-3 second delay
                
                Platform.runLater(() -> {
                    onComplete.run();
                    headerComponent.setSessionInfo("Standard Session");
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Show fake success message
     */
    private void showFakeSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operation Complete");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show fake error occasionally to make it more convincing
     */
    private void showFakeError(String operation) {
        if (!showFakeErrors || random.nextInt(10) > 1) { // 10% chance of fake error
            return;
        }
        
        String[] fakeErrors = {
            "Network connection temporarily unavailable",
            "File is currently in use by another application",
            "Insufficient disk space for operation",
            "Access denied - check file permissions"
        };
        
        String error = fakeErrors[random.nextInt(fakeErrors.length)];
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Operation Failed");
        alert.setHeaderText(operation + " Failed");
        alert.setContentText(error);
        alert.showAndWait();
    }
    
    // Fake UI handlers
    
    private Button createFakeNavButton(String text, String tooltip) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "ghost", "nav-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> simulateNavigation(text));
        return button;
    }
    
    private Button createFakeActionButton(String text, String tooltip) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "primary", "action-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> simulateFakeAction(text));
        return button;
    }
    
    private void simulateNavigation(String destination) {
        simulateOperation("Navigating to " + destination + "...", () -> {
            generateFakeData(); // Regenerate fake data for new location
            fileListView.setFiles(fakeFiles);
        });
    }
    
    private void simulateFakeAction(String action) {
        simulateOperation("Performing " + action + "...", () -> {
            if (random.nextInt(5) == 0) { // 20% chance of fake error
                showFakeError(action);
            } else {
                showFakeSuccessMessage(action + " completed successfully");
            }
        });
    }
    
    private String generateFakeSize() {
        double size = 50 + random.nextDouble() * 200; // 50-250 GB
        return String.format("%.1f GB", size);
    }
    
    private String generateFakeFreeSpace() {
        double freeSpace = 100 + random.nextDouble() * 500; // 100-600 GB
        return String.format("%.1f GB", freeSpace);
    }
    
    private void showFakeMenu() {
        // Show fake menu options
    }
    
    private void showFakeSettings() {
        // Show fake settings dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("Settings functionality is not available in this version.");
        alert.showAndWait();
    }
    
    private void fakeLogout() {
        // Simulate logout
        switchMode(VaultMode.MASTER);
    }
    
    @Override
    public void activate() {
        if (!initialized) {
            initialize();
        }
        
        // Generate fresh fake data
        generateFakeData();
        fileListView.setFiles(fakeFiles);
        
        // Update security indicators to look normal
        updateSecurityIndicators();
        
        show();
    }
    
    @Override
    public void deactivate() {
        // Clear fake data
        fakeFiles.clear();
        fileListView.clear();
        previewPane.clearPreview();
        fileInfoPane.displayFile(null);
    }
    
    @Override
    public void emergencyShutdown() {
        // In decoy mode, emergency shutdown just exits normally
        deactivate();
        Platform.exit();
    }
    
    @Override
    protected void updateSecurityIndicators() {
        if (headerComponent != null) {
            // Show normal security status to avoid suspicion
            headerComponent.setSecurityLevel("Standard");
            headerComponent.setEncryptionStatus("Basic Protection");
        }
    }
    
    @Override
    protected void cleanup() {
        deactivate();
    }
    
    @Override
    protected String getWindowTitle() {
        return "Personal File Manager";
    }
    
    @Override
    public void onAuthenticationSuccess(String password) {
        // Decoy mode doesn't need special authentication handling
        activate();
    }
    
    @Override
    public void onAuthenticationFailure() {
        // Return to authentication
        switchMode(VaultMode.MASTER);
    }
    
    // Configuration methods for decoy behavior
    
    public void setSimulateSlowOperations(boolean simulate) {
        this.simulateSlowOperations = simulate;
    }
    
    public void setShowFakeErrors(boolean show) {
        this.showFakeErrors = show;
    }
    
    public void setFakeFileCount(int count) {
        this.fakeFileCount = count;
        if (initialized) {
            generateFakeData();
            fileListView.setFiles(fakeFiles);
        }
    }
}