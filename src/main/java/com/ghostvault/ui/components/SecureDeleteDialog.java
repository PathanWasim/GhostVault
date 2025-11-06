package com.ghostvault.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Secure file deletion dialog with multiple overwrite passes and confirmation
 */
public class SecureDeleteDialog {
    
    // Dialog components
    private Stage dialogStage;
    private VBox mainContainer;
    private HBox headerBar;
    private Label titleLabel;
    private Button closeButton;
    
    // Content area
    private VBox contentArea;
    private Label warningLabel;
    private ListView<File> fileListView;
    private VBox optionsPanel;
    private ComboBox<SecureDeleteMethod> methodSelector;
    private CheckBox deleteEmptyFolders;
    private CheckBox confirmEachFile;
    
    // Progress area
    private VBox progressArea;
    private ProgressBar overallProgress;
    private ProgressBar currentFileProgress;
    private Label statusLabel;
    private Label currentFileLabel;
    
    // Button area
    private HBox buttonArea;
    private Button deleteButton;
    private Button cancelButton;
    
    // Configuration
    private List<File> filesToDelete = new ArrayList<>();
    private SecureDeleteMethod selectedMethod = SecureDeleteMethod.DOD_3_PASS;
    
    // Callbacks
    private Consumer<List<File>> onFilesDeleted;
    private Runnable onCancelled;
    
    // State
    private boolean deletionInProgress = false;
    private Task<Void> deletionTask;
    
    public SecureDeleteDialog() {
        initializeComponents();
        setupLayout();
        setupStyling();
        setupEventHandlers();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Dialog stage
        dialogStage = new Stage();
        dialogStage.setTitle("Secure Delete");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(true);
        dialogStage.setWidth(600);
        dialogStage.setHeight(500);
        
        // Main container
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("secure-delete-dialog");
        
        // Header bar
        headerBar = new HBox(8);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getStyleClass().add("secure-delete-header");
        
        titleLabel = new Label("Secure Delete");
        titleLabel.getStyleClass().add("secure-delete-title");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "icon", "close-button");
        
        headerBar.getChildren().addAll(titleLabel, headerSpacer, closeButton);
        
        // Content area
        contentArea = new VBox(12);
        contentArea.getStyleClass().add("secure-delete-content");
        
        // Warning label
        warningLabel = new Label(
            "⚠️ WARNING: Secure deletion permanently destroys files and makes them unrecoverable. " +
            "This operation cannot be undone!"
        );
        warningLabel.getStyleClass().add("warning-label");
        warningLabel.setWrapText(true);
        
        // File list
        Label filesLabel = new Label("Files to be securely deleted:");
        filesLabel.getStyleClass().add("section-label");
        
        fileListView = new ListView<>();
        fileListView.getStyleClass().add("delete-file-list");
        fileListView.setCellFactory(listView -> new DeleteFileListCell());
        fileListView.setPrefHeight(150);
        
        // Options panel
        optionsPanel = new VBox(8);
        optionsPanel.getStyleClass().add("delete-options-panel");
        
        Label optionsLabel = new Label("Deletion Options:");
        optionsLabel.getStyleClass().add("section-label");
        
        HBox methodRow = new HBox(8);
        methodRow.setAlignment(Pos.CENTER_LEFT);
        
        Label methodLabel = new Label("Secure deletion method:");
        methodLabel.getStyleClass().add("option-label");
        
        methodSelector = new ComboBox<>();
        methodSelector.getItems().addAll(SecureDeleteMethod.values());
        methodSelector.setValue(SecureDeleteMethod.DOD_3_PASS);
        methodSelector.getStyleClass().add("method-selector");
        
        methodRow.getChildren().addAll(methodLabel, methodSelector);
        
        deleteEmptyFolders = new CheckBox("Delete empty parent folders");
        deleteEmptyFolders.getStyleClass().add("delete-option-checkbox");
        deleteEmptyFolders.setSelected(true);
        
        confirmEachFile = new CheckBox("Confirm deletion of each file individually");
        confirmEachFile.getStyleClass().add("delete-option-checkbox");
        confirmEachFile.setSelected(false);
        
        optionsPanel.getChildren().addAll(
            optionsLabel, methodRow, deleteEmptyFolders, confirmEachFile
        );
        
        contentArea.getChildren().addAll(
            warningLabel, filesLabel, fileListView, optionsPanel
        );
        
        // Progress area
        progressArea = new VBox(8);
        progressArea.getStyleClass().add("delete-progress-area");
        progressArea.setVisible(false);
        
        Label progressLabel = new Label("Deletion Progress:");
        progressLabel.getStyleClass().add("section-label");
        
        overallProgress = new ProgressBar();
        overallProgress.getStyleClass().add("overall-progress");
        overallProgress.setPrefWidth(Double.MAX_VALUE);
        
        currentFileProgress = new ProgressBar();
        currentFileProgress.getStyleClass().add("current-file-progress");
        currentFileProgress.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready to delete");
        statusLabel.getStyleClass().add("delete-status-label");
        
        currentFileLabel = new Label("");
        currentFileLabel.getStyleClass().add("current-file-label");
        
        progressArea.getChildren().addAll(
            progressLabel, overallProgress, currentFileProgress, 
            statusLabel, currentFileLabel
        );
        
        // Button area
        buttonArea = new HBox(8);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);
        buttonArea.getStyleClass().add("delete-button-area");
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "cancel-button");
        
        deleteButton = new Button("Secure Delete");
        deleteButton.getStyleClass().addAll("button", "danger", "delete-button");
        
        buttonArea.getChildren().addAll(cancelButton, deleteButton);
        
        mainContainer.getChildren().addAll(
            headerBar, contentArea, progressArea, buttonArea
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
        contentArea.setPadding(new Insets(0, 12, 12, 12));
        progressArea.setPadding(new Insets(12));
        buttonArea.setPadding(new Insets(12));
        
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        VBox.setVgrow(fileListView, Priority.ALWAYS);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        // Styling handled via CSS
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Close button
        closeButton.setOnAction(e -> cancel());
        
        // Dialog close
        dialogStage.setOnCloseRequest(e -> {
            if (deletionInProgress) {
                e.consume(); // Prevent closing during deletion
                showCancelConfirmation();
            } else {
                cancel();
            }
        });
        
        // Method selector
        methodSelector.setOnAction(e -> {
            selectedMethod = methodSelector.getValue();
            updateDeleteButtonText();
        });
        
        // Buttons
        deleteButton.setOnAction(e -> startSecureDeletion());
        cancelButton.setOnAction(e -> {
            if (deletionInProgress) {
                showCancelConfirmation();
            } else {
                cancel();
            }
        });
    }
    
    /**
     * Update delete button text based on method
     */
    private void updateDeleteButtonText() {
        if (selectedMethod != null) {
            deleteButton.setText("Secure Delete (" + selectedMethod.getDisplayName() + ")");
        }
    }
    
    /**
     * Start secure deletion process
     */
    private void startSecureDeletion() {
        if (filesToDelete.isEmpty()) {
            return;
        }
        
        // Show final confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Secure Deletion");
        confirmAlert.setHeaderText("Are you absolutely sure?");
        confirmAlert.setContentText(
            String.format("This will permanently delete %d file(s) using %s method.\\n\\n" +
                         "This operation CANNOT be undone!",
                         filesToDelete.size(), selectedMethod.getDisplayName())
        );
        
        ButtonType proceedButton = new ButtonType("Proceed with Deletion", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(proceedButton, cancelButton);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == proceedButton) {
                performSecureDeletion();
            }
        });
    }
    
    /**
     * Perform the actual secure deletion
     */
    private void performSecureDeletion() {
        deletionInProgress = true;
        
        // Show progress area and hide content
        contentArea.setVisible(false);
        progressArea.setVisible(true);
        
        // Update button states
        deleteButton.setDisable(true);
        cancelButton.setText("Cancel Deletion");
        
        // Create deletion task
        deletionTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int totalFiles = filesToDelete.size();
                
                for (int i = 0; i < totalFiles; i++) {
                    File file = filesToDelete.get(i);
                    final File currentFile = file;
                    final int currentIndex = i;
                    
                    Platform.runLater(() -> {
                        currentFileLabel.setText("Deleting: " + currentFile.getName());
                        overallProgress.setProgress((double) currentIndex / totalFiles);
                    });
                    
                    // Check for cancellation
                    if (isCancelled()) {
                        break;
                    }
                    
                    // Perform secure deletion
                    secureDeleteFile(currentFile);
                    
                    Platform.runLater(() -> {
                        overallProgress.setProgress((double) (currentIndex + 1) / totalFiles);
                    });
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    deletionInProgress = false;
                    statusLabel.setText("Secure deletion completed successfully");
                    currentFileLabel.setText("");
                    
                    if (onFilesDeleted != null) {
                        onFilesDeleted.accept(new ArrayList<>(filesToDelete));
                    }
                    
                    // Auto-close after a delay
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> dialogStage.close()));
                    timeline.play();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    deletionInProgress = false;
                    statusLabel.setText("Deletion failed: " + getException().getMessage());
                    cancelButton.setText("Close");
                });
            }
            
            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    deletionInProgress = false;
                    statusLabel.setText("Deletion cancelled by user");
                    cancelButton.setText("Close");
                });
            }
        };
        
        Thread deletionThread = new Thread(deletionTask);
        deletionThread.setDaemon(true);
        deletionThread.start();
    }
    
    /**
     * Securely delete a single file
     */
    private void secureDeleteFile(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        
        if (file.isDirectory()) {
            secureDeleteDirectory(file);
        } else {
            secureDeleteRegularFile(file);
        }
    }
    
    /**
     * Securely delete a regular file
     */
    private void secureDeleteRegularFile(File file) throws IOException {
        long fileSize = file.length();
        
        if (fileSize == 0) {
            // Empty file, just delete normally
            Files.delete(file.toPath());
            return;
        }
        
        Platform.runLater(() -> {
            statusLabel.setText("Overwriting file data...");
            currentFileProgress.setProgress(0);
        });
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
            SecureRandom random = new SecureRandom();
            byte[] buffer = new byte[8192]; // 8KB buffer
            
            int passes = selectedMethod.getPasses();
            
            for (int pass = 0; pass < passes; pass++) {
                final int currentPass = pass;
                final int totalPasses = passes;
                Platform.runLater(() -> {
                    statusLabel.setText(String.format("Pass %d of %d", currentPass + 1, totalPasses));
                });
                
                raf.seek(0);
                long bytesWritten = 0;
                
                while (bytesWritten < fileSize) {
                    // Check for cancellation
                    if (deletionTask.isCancelled()) {
                        return;
                    }
                    
                    int bytesToWrite = (int) Math.min(buffer.length, fileSize - bytesWritten);
                    
                    // Fill buffer with pattern based on method and pass
                    fillBufferWithPattern(buffer, bytesToWrite, selectedMethod, pass, random);
                    
                    raf.write(buffer, 0, bytesToWrite);
                    bytesWritten += bytesToWrite;
                    
                    // Update progress
                    final double progress = (double) bytesWritten / fileSize;
                    Platform.runLater(() -> currentFileProgress.setProgress(progress));
                }
                
                // Force write to disk
                raf.getFD().sync();
            }
        }
        
        Platform.runLater(() -> {
            statusLabel.setText("Removing file entry...");
            currentFileProgress.setProgress(1.0);
        });
        
        // Finally delete the file
        Files.delete(file.toPath());
    }
    
    /**
     * Fill buffer with overwrite pattern
     */
    private void fillBufferWithPattern(byte[] buffer, int length, SecureDeleteMethod method, 
                                     int pass, SecureRandom random) {
        switch (method) {
            case SIMPLE_OVERWRITE:
                // Single pass with zeros
                for (int i = 0; i < length; i++) {
                    buffer[i] = 0;
                }
                break;
                
            case DOD_3_PASS:
                // DoD 5220.22-M 3-pass
                switch (pass) {
                    case 0:
                        // Pass 1: All zeros
                        for (int i = 0; i < length; i++) {
                            buffer[i] = 0;
                        }
                        break;
                    case 1:
                        // Pass 2: All ones
                        for (int i = 0; i < length; i++) {
                            buffer[i] = (byte) 0xFF;
                        }
                        break;
                    case 2:
                        // Pass 3: Random data
                        random.nextBytes(buffer);
                        break;
                }
                break;
                
            case DOD_7_PASS:
                // DoD 5220.22-M 7-pass
                switch (pass) {
                    case 0:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0x35;
                        break;
                    case 1:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0xCA;
                        break;
                    case 2:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0x97;
                        break;
                    case 3:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0x68;
                        break;
                    case 4:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0x92;
                        break;
                    case 5:
                        for (int i = 0; i < length; i++) buffer[i] = (byte) 0x6D;
                        break;
                    case 6:
                        random.nextBytes(buffer);
                        break;
                }
                break;
                
            case GUTMANN_35_PASS:
                // Simplified Gutmann method (would need full implementation)
                if (pass < 34) {
                    // Use various patterns
                    byte pattern = (byte) (pass * 7 + 42); // Simple pattern generation
                    for (int i = 0; i < length; i++) {
                        buffer[i] = pattern;
                    }
                } else {
                    // Final pass with random data
                    random.nextBytes(buffer);
                }
                break;
        }
    }
    
    /**
     * Securely delete a directory
     */
    private void secureDeleteDirectory(File directory) throws IOException {
        File[] children = directory.listFiles();
        if (children != null) {
            for (File child : children) {
                secureDeleteFile(child);
            }
        }
        
        // Delete the empty directory
        Files.delete(directory.toPath());
    }
    
    /**
     * Show cancellation confirmation
     */
    private void showCancelConfirmation() {
        Alert cancelAlert = new Alert(Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Deletion");
        cancelAlert.setHeaderText("Cancel secure deletion?");
        cancelAlert.setContentText("Cancelling now may leave some files partially overwritten but still present.");
        
        cancelAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deletionTask != null) {
                    deletionTask.cancel();
                }
            }
        });
    }
    
    /**
     * Cancel and close dialog
     */
    private void cancel() {
        if (onCancelled != null) {
            onCancelled.run();
        }
        dialogStage.close();
    }
    
    /**
     * Custom list cell for files to delete
     */
    private class DeleteFileListCell extends ListCell<File> {
        private HBox content;
        private Label nameLabel;
        private Label sizeLabel;
        
        public DeleteFileListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(4, 8, 4, 8));
            
            nameLabel = new Label();
            nameLabel.getStyleClass().add("delete-file-name");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            sizeLabel = new Label();
            sizeLabel.getStyleClass().add("delete-file-size");
            
            content.getChildren().addAll(nameLabel, spacer, sizeLabel);
        }
        
        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(file.getAbsolutePath());
                
                if (file.isDirectory()) {
                    sizeLabel.setText("Folder");
                } else {
                    sizeLabel.setText(formatFileSize(file.length()));
                }
                
                setGraphic(content);
            }
        }
        
        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    // Public API
    
    /**
     * Show the secure delete dialog
     */
    public void showDialog(Window owner, List<File> files) {
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        
        this.filesToDelete = new ArrayList<>(files);
        fileListView.getItems().setAll(files);
        
        updateDeleteButtonText();
        dialogStage.showAndWait();
    }
    
    // Getters and Setters
    
    public void setOnFilesDeleted(Consumer<List<File>> callback) {
        this.onFilesDeleted = callback;
    }
    
    public void setOnCancelled(Runnable callback) {
        this.onCancelled = callback;
    }
    
    /**
     * Secure deletion methods
     */
    public enum SecureDeleteMethod {
        SIMPLE_OVERWRITE("Simple Overwrite", 1, "Single pass with zeros"),
        DOD_3_PASS("DoD 3-Pass", 3, "US Department of Defense 3-pass method"),
        DOD_7_PASS("DoD 7-Pass", 7, "US Department of Defense 7-pass method"),
        GUTMANN_35_PASS("Gutmann 35-Pass", 35, "Peter Gutmann's 35-pass method");
        
        private final String displayName;
        private final int passes;
        private final String description;
        
        SecureDeleteMethod(String displayName, int passes, String description) {
            this.displayName = displayName;
            this.passes = passes;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public int getPasses() { return passes; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() { return displayName; }
    }
}