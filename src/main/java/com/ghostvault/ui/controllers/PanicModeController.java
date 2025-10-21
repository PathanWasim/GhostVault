package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for Panic Mode - emergency data destruction
 */
public class PanicModeController extends ModeController {
    
    // UI Components
    private VBox centerContainer;
    private Label titleLabel;
    private Label warningLabel;
    private ProgressBar destructionProgress;
    private Label statusLabel;
    private Label countdownLabel;
    private HBox buttonContainer;
    private Button confirmButton;
    private Button cancelButton;
    private Button emergencyButton;
    
    // Destruction configuration
    private List<Path> targetPaths = new ArrayList<>();
    private boolean destructionInProgress = false;
    private boolean emergencyMode = false;
    private int countdownSeconds = 10;
    private Timeline countdownTimeline;
    private Timeline destructionTimeline;
    
    // Statistics
    private AtomicInteger filesDestroyed = new AtomicInteger(0);
    private AtomicInteger totalFiles = new AtomicInteger(0);
    
    public PanicModeController(Stage primaryStage) {
        super(primaryStage, VaultMode.PANIC);
    }
    
    @Override
    public void initialize() {
        if (initialized) return;
        
        try {
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            setupDestructionTargets();
            
            initialized = true;
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Failed to initialize Panic Mode", e);
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        centerContainer = new VBox(20);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.getStyleClass().add("panic-mode-container");
        
        // Title
        titleLabel = new Label("🚨 PANIC MODE ACTIVATED 🚨");
        titleLabel.getStyleClass().add("panic-title");
        
        // Warning message
        warningLabel = new Label(
            "WARNING: This will permanently destroy all vault data and cannot be undone!\\n\\n" +
            "This emergency feature is designed to protect your privacy in critical situations.\\n" +
            "All encrypted files, backups, and metadata will be securely overwritten."
        );
        warningLabel.getStyleClass().add("panic-warning");
        warningLabel.setWrapText(true);
        warningLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Progress bar
        destructionProgress = new ProgressBar(0);
        destructionProgress.getStyleClass().add("destruction-progress");
        destructionProgress.setPrefWidth(400);
        destructionProgress.setVisible(false);
        
        // Status label
        statusLabel = new Label("Ready to initiate emergency destruction");
        statusLabel.getStyleClass().add("panic-status");
        
        // Countdown label
        countdownLabel = new Label("");
        countdownLabel.getStyleClass().add("countdown-label");
        countdownLabel.setVisible(false);
        
        // Buttons
        buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        
        confirmButton = new Button("CONFIRM DESTRUCTION");
        confirmButton.getStyleClass().addAll("button", "danger", "panic-confirm-button");
        confirmButton.setPrefWidth(200);
        
        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "ghost", "panic-cancel-button");
        cancelButton.setPrefWidth(100);
        
        emergencyButton = new Button("EMERGENCY - DESTROY NOW");
        emergencyButton.getStyleClass().addAll("button", "critical", "emergency-button");
        emergencyButton.setPrefWidth(250);
        emergencyButton.setVisible(false);
        
        buttonContainer.getChildren().addAll(cancelButton, confirmButton);
        
        centerContainer.getChildren().addAll(
            titleLabel, warningLabel, destructionProgress, 
            statusLabel, countdownLabel, buttonContainer, emergencyButton
        );
        
        rootContainer.getChildren().add(centerContainer);
        VBox.setVgrow(centerContainer, Priority.ALWAYS);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        rootContainer.setAlignment(Pos.CENTER);
        rootContainer.setPadding(new Insets(40));
        centerContainer.setMaxWidth(600);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        confirmButton.setOnAction(e -> startCountdown());
        cancelButton.setOnAction(e -> cancelPanicMode());
        emergencyButton.setOnAction(e -> emergencyDestruction());
        
        // Prevent window closing during destruction
        primaryStage.setOnCloseRequest(e -> {
            if (destructionInProgress) {
                e.consume();
                showDestructionInProgressWarning();
            } else {
                cancelPanicMode();
            }
        });
    }
    
    /**
     * Setup destruction targets
     */
    private void setupDestructionTargets() {
        // Add vault directories and files to destruction list
        String userHome = System.getProperty("user.home");
        
        // Vault data directories
        targetPaths.add(Paths.get(userHome, ".ghostvault"));
        targetPaths.add(Paths.get(userHome, "GhostVault_Data"));
        targetPaths.add(Paths.get(userHome, "Documents", "GhostVault"));
        
        // Backup directories
        targetPaths.add(Paths.get(userHome, "GhostVault_Backups"));
        
        // Temporary files
        targetPaths.add(Paths.get(System.getProperty("java.io.tmpdir"), "ghostvault_temp"));
        
        // Count total files for progress tracking
        countTotalFiles();
    }
    
    /**
     * Count total files to be destroyed
     */
    private void countTotalFiles() {
        new Thread(() -> {
            int count = 0;
            for (Path path : targetPaths) {
                if (Files.exists(path)) {
                    count += countFilesRecursive(path);
                }
            }
            totalFiles.set(count);
            
            Platform.runLater(() -> {
                statusLabel.setText(String.format("Ready to destroy %d files and directories", count));
            });
        }).start();
    }
    
    /**
     * Count files recursively
     */
    private int countFilesRecursive(Path path) {
        try {
            if (Files.isDirectory(path)) {
                return (int) Files.walk(path)
                    .mapToInt(p -> Files.isRegularFile(p) ? 1 : 0)
                    .sum();
            } else if (Files.isRegularFile(path)) {
                return 1;
            }
        } catch (IOException e) {
            // Ignore errors during counting
        }
        return 0;
    }
    
    /**
     * Start countdown before destruction
     */
    private void startCountdown() {
        confirmButton.setDisable(true);
        countdownLabel.setVisible(true);
        emergencyButton.setVisible(true);
        
        countdownTimeline = new Timeline();
        
        for (int i = 0; i <= countdownSeconds; i++) {
            final int secondsLeft = countdownSeconds - i;
            
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(i),
                e -> {
                    if (secondsLeft > 0) {
                        countdownLabel.setText(String.format("Destruction starts in %d seconds...", secondsLeft));
                        statusLabel.setText("Click EMERGENCY button to start immediately");
                    } else {
                        startDestruction();
                    }
                }
            );
            
            countdownTimeline.getKeyFrames().add(keyFrame);
        }
        
        countdownTimeline.setOnFinished(e -> startDestruction());
        countdownTimeline.play();
    }
    
    /**
     * Start the destruction process
     */
    private void startDestruction() {
        destructionInProgress = true;
        emergencyMode = false;
        
        // Stop countdown if running
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        // Update UI
        countdownLabel.setVisible(false);
        destructionProgress.setVisible(true);
        buttonContainer.setVisible(false);
        emergencyButton.setVisible(false);
        
        statusLabel.setText("DESTROYING DATA - DO NOT POWER OFF");
        
        // Start destruction in background thread
        Thread destructionThread = new Thread(this::performDestruction);
        destructionThread.setDaemon(false); // Ensure it completes
        destructionThread.start();
    }
    
    /**
     * Emergency immediate destruction
     */
    private void emergencyDestruction() {
        emergencyMode = true;
        
        // Stop countdown
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        startDestruction();
    }
    
    /**
     * Perform the actual data destruction
     */
    private void performDestruction() {
        try {
            Platform.runLater(() -> {
                statusLabel.setText("Initializing secure destruction...");
                destructionProgress.setProgress(0.1);
            });
            
            SecureRandom random = new SecureRandom();
            
            for (int i = 0; i < targetPaths.size(); i++) {
                Path targetPath = targetPaths.get(i);
                
                if (Files.exists(targetPath)) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Destroying: " + targetPath.getFileName());
                    });
                    
                    secureDeletePath(targetPath, random);
                }
                
                // Update progress
                final double progress = 0.1 + (0.8 * (i + 1) / targetPaths.size());
                Platform.runLater(() -> destructionProgress.setProgress(progress));
            }
            
            // Clear system caches and temporary data
            Platform.runLater(() -> {
                statusLabel.setText("Clearing system caches...");
                destructionProgress.setProgress(0.9);
            });
            
            clearSystemCaches();
            
            // Final cleanup
            Platform.runLater(() -> {
                statusLabel.setText("Destruction completed successfully");
                destructionProgress.setProgress(1.0);
            });
            
            // Wait a moment then exit
            Thread.sleep(2000);
            
            Platform.runLater(() -> {
                showDestructionCompleteDialog();
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error during destruction: " + e.getMessage());
                ErrorHandlingSystem.handleError("Panic mode destruction failed", e);
            });
        }
    }
    
    /**
     * Securely delete a path (file or directory)
     */
    private void secureDeletePath(Path path, SecureRandom random) throws IOException {
        if (Files.isDirectory(path)) {
            // Delete directory contents first
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                .forEach(p -> {
                    try {
                        if (Files.isRegularFile(p)) {
                            secureDeleteFile(p, random);
                            filesDestroyed.incrementAndGet();
                            
                            // Update progress periodically
                            if (filesDestroyed.get() % 10 == 0) {
                                Platform.runLater(() -> {
                                    double fileProgress = (double) filesDestroyed.get() / totalFiles.get();
                                    destructionProgress.setProgress(0.1 + (0.8 * fileProgress));
                                });
                            }
                        } else if (Files.isDirectory(p) && !p.equals(path)) {
                            Files.deleteIfExists(p);
                        }
                    } catch (IOException e) {
                        // Continue with other files even if one fails
                    }
                });
            
            // Delete the directory itself
            Files.deleteIfExists(path);
            
        } else if (Files.isRegularFile(path)) {
            secureDeleteFile(path, random);
            filesDestroyed.incrementAndGet();
        }
    }
    
    /**
     * Securely delete a single file with multiple overwrite passes
     */
    private void secureDeleteFile(Path filePath, SecureRandom random) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        
        long fileSize = Files.size(filePath);
        if (fileSize == 0) {
            Files.delete(filePath);
            return;
        }
        
        // Perform 3-pass overwrite (DoD standard)
        byte[] buffer = new byte[8192];
        
        for (int pass = 0; pass < 3; pass++) {
            try (var channel = Files.newByteChannel(filePath, 
                    java.nio.file.StandardOpenOption.WRITE)) {
                
                long bytesWritten = 0;
                while (bytesWritten < fileSize) {
                    int bytesToWrite = (int) Math.min(buffer.length, fileSize - bytesWritten);
                    
                    // Fill buffer with pattern based on pass
                    switch (pass) {
                        case 0:
                            // Pass 1: All zeros
                            java.util.Arrays.fill(buffer, 0, bytesToWrite, (byte) 0x00);
                            break;
                        case 1:
                            // Pass 2: All ones
                            java.util.Arrays.fill(buffer, 0, bytesToWrite, (byte) 0xFF);
                            break;
                        case 2:
                            // Pass 3: Random data
                            random.nextBytes(buffer);
                            break;
                    }
                    
                    channel.write(java.nio.ByteBuffer.wrap(buffer, 0, bytesToWrite));
                    bytesWritten += bytesToWrite;
                }
                
                // Force write to disk (if it's a FileChannel)
                if (channel instanceof java.nio.channels.FileChannel) {
                    ((java.nio.channels.FileChannel) channel).force(true);
                }
            }
        }
        
        // Finally delete the file
        Files.delete(filePath);
    }
    
    /**
     * Clear system caches and temporary data
     */
    private void clearSystemCaches() {
        try {
            // Force garbage collection
            System.gc();
            System.runFinalization();
            System.gc();
            
            // Clear Java temporary directory
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempPath = Paths.get(tempDir);
            
            Files.walk(tempPath)
                .filter(p -> p.getFileName().toString().contains("ghostvault"))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // Ignore errors
                    }
                });
            
        } catch (Exception e) {
            // Ignore errors during cache clearing
        }
    }
    
    /**
     * Cancel panic mode
     */
    private void cancelPanicMode() {
        if (destructionInProgress) {
            showDestructionInProgressWarning();
            return;
        }
        
        // Stop countdown if running
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        // Return to authentication or previous mode
        switchMode(VaultMode.MASTER);
    }
    
    /**
     * Show warning when trying to cancel during destruction
     */
    private void showDestructionInProgressWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Destruction In Progress");
        alert.setHeaderText("Cannot Cancel");
        alert.setContentText("Data destruction is currently in progress and cannot be stopped. " +
                           "Please wait for the process to complete.");
        alert.showAndWait();
    }
    
    /**
     * Show destruction complete dialog
     */
    private void showDestructionCompleteDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Destruction Complete");
        alert.setHeaderText("Data Successfully Destroyed");
        alert.setContentText(String.format(
            "Emergency data destruction completed successfully.\\n\\n" +
            "Files destroyed: %d\\n" +
            "The application will now exit.",
            filesDestroyed.get()
        ));
        
        alert.setOnHidden(e -> Platform.exit());
        alert.showAndWait();
    }
    
    @Override
    public void activate() {
        if (!initialized) {
            initialize();
        }
        
        // Reset state
        destructionInProgress = false;
        emergencyMode = false;
        filesDestroyed.set(0);
        
        // Update UI
        confirmButton.setDisable(false);
        countdownLabel.setVisible(false);
        destructionProgress.setVisible(false);
        buttonContainer.setVisible(true);
        emergencyButton.setVisible(false);
        
        // Recount files
        countTotalFiles();
        
        show();
    }
    
    @Override
    public void deactivate() {
        // Stop any running timelines
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (destructionTimeline != null) {
            destructionTimeline.stop();
        }
    }
    
    @Override
    public void emergencyShutdown() {
        // In panic mode, emergency shutdown means immediate destruction
        emergencyDestruction();
    }
    
    @Override
    protected void updateSecurityIndicators() {
        // No security indicators needed in panic mode
    }
    
    @Override
    protected void cleanup() {
        deactivate();
    }
    
    @Override
    protected String getWindowTitle() {
        return "GhostVault - PANIC MODE";
    }
    
    @Override
    public void onAuthenticationSuccess(String password) {
        // Panic mode doesn't require special authentication handling
        activate();
    }
    
    @Override
    public void onAuthenticationFailure() {
        // Return to authentication
        switchMode(VaultMode.MASTER);
    }
}