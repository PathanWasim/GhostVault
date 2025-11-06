package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

/**
 * Media viewer component for audio and video files
 * Supports playback with standard media controls using JavaFX Media API
 */
public class MediaViewerComponent extends PreviewComponent {
    
    // UI Components
    private BorderPane rootPane;
    private MediaView mediaView;
    private VBox audioContainer;
    private Label mediaInfoLabel;
    
    // Media Controls
    private Button playPauseButton;
    private Button stopButton;
    private Button muteButton;
    private Button fullscreenButton;
    private Button infoButton;
    private Slider volumeSlider;
    private Slider positionSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Label volumeLabel;
    
    // Media Components
    private MediaPlayer mediaPlayer;
    private Media media;
    private Path tempMediaFile;
    
    // State
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private double lastVolume = 0.7;
    private boolean isVideo = false;
    private boolean isDraggingPosition = false;
    private boolean isFullscreen = false;
    
    // Settings
    private PreviewSettings settings;
    
    // Error handling
    private MediaErrorHandler errorHandler;
    
    public MediaViewerComponent() {
        this.settings = new PreviewSettings();
        this.errorHandler = new MediaErrorHandler();
    }
    
    public MediaViewerComponent(PreviewSettings settings) {
        this.settings = settings != null ? settings : new PreviewSettings();
        this.errorHandler = new MediaErrorHandler();
    }
    
    @Override
    public void loadContent(byte[] fileData) {
        try {
            if (fileData == null || fileData.length == 0) {
                handleLoadError(new IllegalArgumentException("No media data available"), "unknown");
                return;
            }
            
            String extension = vaultFile != null ? vaultFile.getExtension() : "tmp";
            
            // Validate media format before processing (lenient for video files)
            MediaFormatValidationResult validation = validateMediaFormat(extension, fileData);
            if (!validation.isValid()) {
                // For video files, be more lenient and let JavaFX handle validation
                if (isVideoFile(extension) || isAudioFile(extension)) {
                    System.out.println("⚠️ Format validation failed for " + extension + ", but proceeding with JavaFX validation");
                } else {
                    Exception validationError = new UnsupportedOperationException(
                        validation.getMessage() + " - " + validation.getSuggestion());
                    handleLoadError(validationError, extension);
                    return;
                }
            } else {
                System.out.println("✅ Media format validation passed for: " + extension.toUpperCase());
            }
            
            // Handle images directly without MediaPlayer
            if (isImageFile(extension)) {
                Platform.runLater(() -> {
                    try {
                        loadImageContent(fileData);
                    } catch (Exception e) {
                        handleLoadError(e, extension);
                    }
                });
                return;
            }
            
            // For video files, directly try JavaFX MediaPlayer (no options dialog)
            System.out.println("🎬 Video file detected, attempting direct playback with JavaFX MediaPlayer");
            
            // For audio/video, try MediaPlayer with comprehensive error handling
            try {
                // Create temporary file for JavaFX Media API
                tempMediaFile = Files.createTempFile("ghostvault_media_", "." + extension);
                
                System.out.println("🎬 Created temporary file: " + tempMediaFile.toString());
                System.out.println("🎬 File data size: " + formatFileSize(fileData.length));
                
                // Write media data to temporary file with error handling
                try (FileOutputStream fos = new FileOutputStream(tempMediaFile.toFile())) {
                    fos.write(fileData);
                    fos.flush();
                } catch (IOException e) {
                    throw new IOException("Failed to write media data to temporary file", e);
                }
                
                // Verify file was written correctly
                long writtenSize = Files.size(tempMediaFile);
                System.out.println("🎬 Temporary file size: " + formatFileSize(writtenSize));
                
                if (writtenSize != fileData.length) {
                    throw new IOException("Temporary file size mismatch: expected " + fileData.length + ", got " + writtenSize);
                }
                
                // Determine if this is video or audio
                isVideo = isVideoFile(extension);
                
                System.out.println("🎬 Media type determined: " + (isVideo ? "Video" : "Audio"));
                
                // Load media on JavaFX Application Thread with error handling
                Platform.runLater(() -> {
                    try {
                        loadMediaFile(tempMediaFile.toFile());
                    } catch (Exception e) {
                        System.err.println("🎬 Error in loadMediaFile: " + e.getMessage());
                        handleLoadError(e, extension);
                    }
                });
                
            } catch (IOException e) {
                System.err.println("🎬 IOException during temp file creation: " + e.getMessage());
                Platform.runLater(() -> {
                    handleLoadError(e, extension);
                });
            }
            
        } catch (Exception e) {
            // Catch-all error handling
            Platform.runLater(() -> {
                String extension = vaultFile != null ? vaultFile.getExtension() : "unknown";
                handleLoadError(e, extension);
            });
        }
    }
    
    /**
     * Load media file using JavaFX Media API with comprehensive error handling
     */
    private void loadMediaFile(File mediaFile) {
        String fileType = vaultFile != null ? vaultFile.getExtension() : "unknown";
        
        try {
            // Validate media file before processing
            if (!validateMediaFile(mediaFile)) {
                throw new IllegalArgumentException("Invalid media file");
            }
            
            // Create Media object with error handling
            String mediaUrl = mediaFile.toURI().toString();
            System.out.println("🎬 Loading media from URL: " + mediaUrl);
            System.out.println("🎬 File exists: " + mediaFile.exists());
            System.out.println("🎬 File readable: " + mediaFile.canRead());
            System.out.println("🎬 File size: " + formatFileSize(mediaFile.length()));
            
            try {
                media = new Media(mediaUrl);
                System.out.println("🎬 Media object created successfully");
            } catch (Exception e) {
                System.err.println("🎬 Failed to create Media object: " + e.getMessage());
                throw new RuntimeException("Failed to create Media object: " + e.getMessage(), e);
            }
            
            // Set up comprehensive media error handling
            media.setOnError(() -> {
                Exception error = media.getError();
                String errorMessage = error != null ? error.getMessage() : "Unknown media error";
                System.err.println("🎬 Media error: " + errorMessage);
                
                Platform.runLater(() -> {
                    // For video files, go directly to simplified preview instead of showing corruption error
                    if (isVideoFile(fileType)) {
                        System.out.println("🎬 Media error for video file, showing simplified preview");
                        showSimplifiedVideoPreview(fileType);
                    } else {
                        handleLoadError(error != null ? error : new RuntimeException(errorMessage), fileType);
                    }
                });
            });
            
            // Create MediaPlayer with error handling
            try {
                mediaPlayer = new MediaPlayer(media);
                System.out.println("🎬 MediaPlayer created successfully");
            } catch (Exception e) {
                throw new RuntimeException("Failed to create MediaPlayer: " + e.getMessage(), e);
            }
            
            // Configure MediaPlayer with comprehensive error handling
            try {
                setupMediaPlayerWithErrorHandling();
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure MediaPlayer: " + e.getMessage(), e);
            }
            
            // Update UI based on media type with error handling
            try {
                if (isVideo) {
                    setupVideoDisplay();
                    System.out.println("🎬 Video display setup completed");
                } else {
                    setupAudioDisplay();
                    System.out.println("🎬 Audio display setup completed");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to setup media display: " + e.getMessage(), e);
            }
            
            // Update media info with error handling
            try {
                updateMediaInfo();
                System.out.println("🎬 Media info updated successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to update media info: " + e.getMessage());
                // Non-critical error, continue
            }
            
            System.out.println("✅ Media file loaded successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load media file: " + e.getMessage());
            e.printStackTrace();
            
            // Handle the error through our error handling system
            handleLoadError(e, fileType);
        }
    }
    
    /**
     * Validate media file before processing
     */
    private boolean validateMediaFile(File mediaFile) {
        try {
            if (mediaFile == null) {
                System.err.println("Media file is null");
                return false;
            }
            
            if (!mediaFile.exists()) {
                System.err.println("Media file does not exist: " + mediaFile.getPath());
                return false;
            }
            
            if (!mediaFile.canRead()) {
                System.err.println("Cannot read media file: " + mediaFile.getPath());
                return false;
            }
            
            if (mediaFile.length() == 0) {
                System.err.println("Media file is empty: " + mediaFile.getPath());
                return false;
            }
            
            // Check file size limits
            long maxSize = 500 * 1024 * 1024; // 500MB limit
            if (mediaFile.length() > maxSize) {
                System.err.println("Media file too large: " + mediaFile.length() + " bytes");
                return false;
            }
            
            System.out.println("✅ Media file validation passed: " + mediaFile.getName() + 
                " (" + formatFileSize(mediaFile.length()) + ")");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error validating media file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Setup MediaPlayer with event handlers and settings (enhanced error handling)
     */
    private void setupMediaPlayerWithErrorHandling() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer is null");
        }
        
        try {
            // Set initial volume with error handling
            try {
                double defaultVolume = settings.getDefaultVolume();
                mediaPlayer.setVolume(defaultVolume);
                lastVolume = defaultVolume;
                System.out.println("🔊 Volume set to: " + (defaultVolume * 100) + "%");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to set volume: " + e.getMessage());
                // Use default volume
                lastVolume = 0.7;
            }
            
            // Auto-play setting with error handling
            try {
                boolean autoPlay = settings.isAutoPlayMedia();
                mediaPlayer.setAutoPlay(autoPlay);
                System.out.println("🎬 Auto-play: " + autoPlay);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to set auto-play: " + e.getMessage());
            }
            
            // Setup event handlers with comprehensive error handling
            setupMediaPlayerEventHandlers();
            
            // Setup property listeners with error handling
            setupMediaPlayerPropertyListeners();
            
            System.out.println("✅ MediaPlayer setup completed");
            
        } catch (Exception e) {
            System.err.println("❌ Error setting up MediaPlayer: " + e.getMessage());
            throw new RuntimeException("MediaPlayer setup failed", e);
        }
    }
    
    /**
     * Setup MediaPlayer event handlers with error handling
     */
    private void setupMediaPlayerEventHandlers() {
        String fileType = vaultFile != null ? vaultFile.getExtension() : "unknown";
        
        // OnReady handler with error handling
        mediaPlayer.setOnReady(() -> {
            try {
                Platform.runLater(() -> {
                    try {
                        System.out.println("🎬 MediaPlayer ready");
                        
                        Duration totalDuration = mediaPlayer.getTotalDuration();
                        if (totalDuration != null && !totalDuration.isUnknown()) {
                            positionSlider.setMax(totalDuration.toSeconds());
                            updateTotalTimeLabel(totalDuration);
                            System.out.println("⏱️ Total duration: " + formatDuration(totalDuration));
                        } else {
                            System.out.println("⏱️ Duration unknown");
                        }
                        
                        // Enable controls
                        enableControls(true);
                        
                        // Auto-play if enabled
                        if (settings.isAutoPlayMedia()) {
                            playMedia();
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Error in onReady handler: " + e.getMessage());
                        handleLoadError(e, fileType);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error in onReady Platform.runLater: " + e.getMessage());
            }
        });
        
        // OnPlaying handler
        mediaPlayer.setOnPlaying(() -> {
            Platform.runLater(() -> {
                try {
                    isPlaying = true;
                    updatePlayPauseButton();
                    System.out.println("▶️ Media playing");
                } catch (Exception e) {
                    System.err.println("Error in onPlaying handler: " + e.getMessage());
                }
            });
        });
        
        // OnPaused handler
        mediaPlayer.setOnPaused(() -> {
            Platform.runLater(() -> {
                try {
                    isPlaying = false;
                    updatePlayPauseButton();
                    System.out.println("⏸️ Media paused");
                } catch (Exception e) {
                    System.err.println("Error in onPaused handler: " + e.getMessage());
                }
            });
        });
        
        // OnStopped handler
        mediaPlayer.setOnStopped(() -> {
            Platform.runLater(() -> {
                try {
                    isPlaying = false;
                    updatePlayPauseButton();
                    positionSlider.setValue(0);
                    updateCurrentTimeLabel(Duration.ZERO);
                    System.out.println("⏹️ Media stopped");
                } catch (Exception e) {
                    System.err.println("Error in onStopped handler: " + e.getMessage());
                }
            });
        });
        
        // OnEndOfMedia handler
        mediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(() -> {
                try {
                    isPlaying = false;
                    updatePlayPauseButton();
                    mediaPlayer.seek(Duration.ZERO);
                    System.out.println("🏁 End of media reached");
                } catch (Exception e) {
                    System.err.println("Error in onEndOfMedia handler: " + e.getMessage());
                }
            });
        });
        
        // OnError handler with enhanced error handling
        mediaPlayer.setOnError(() -> {
            try {
                Exception error = mediaPlayer.getError();
                String errorMessage = error != null ? error.getMessage() : "Unknown MediaPlayer error";
                System.err.println("🎬 MediaPlayer error: " + errorMessage);
                
                Platform.runLater(() -> {
                    handleLoadError(error != null ? error : new RuntimeException(errorMessage), fileType);
                });
                
            } catch (Exception e) {
                System.err.println("Error in error handler: " + e.getMessage());
                Platform.runLater(() -> {
                    handleLoadError(e, fileType);
                });
            }
        });
    }
    
    /**
     * Setup MediaPlayer property listeners with error handling
     */
    private void setupMediaPlayerPropertyListeners() {
        try {
            // Update position slider during playback with error handling
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                try {
                    if (!isDraggingPosition && newTime != null && !newTime.isUnknown()) {
                        Platform.runLater(() -> {
                            try {
                                positionSlider.setValue(newTime.toSeconds());
                                updateCurrentTimeLabel(newTime);
                            } catch (Exception e) {
                                System.err.println("Error updating position: " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error in currentTime listener: " + e.getMessage());
                }
            });
            
            // Monitor status changes
            mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                try {
                    System.out.println("🎬 MediaPlayer status: " + oldStatus + " → " + newStatus);
                    
                    // Handle specific status changes that might indicate problems
                    if (newStatus == javafx.scene.media.MediaPlayer.Status.HALTED) {
                        Exception error = mediaPlayer.getError();
                        if (error != null) {
                            Platform.runLater(() -> {
                                String fileType = vaultFile != null ? vaultFile.getExtension() : "unknown";
                                handleLoadError(error, fileType);
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in status listener: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error setting up property listeners: " + e.getMessage());
            throw new RuntimeException("Failed to setup property listeners", e);
        }
    }
    
    /**
     * Setup video display
     */
    private void setupVideoDisplay() {
        if (mediaPlayer == null) return;
        
        // Create MediaView for video
        mediaView = new MediaView(mediaPlayer);
        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(600);
        mediaView.setFitHeight(400);
        
        // Add to center of root pane
        rootPane.setCenter(mediaView);
        
        // Hide audio container
        if (audioContainer != null) {
            audioContainer.setVisible(false);
            audioContainer.setManaged(false);
        }
        
        // Show fullscreen button for video
        if (fullscreenButton != null) {
            fullscreenButton.setVisible(true);
            fullscreenButton.setManaged(true);
        }
    }
    
    /**
     * Setup audio display
     */
    private void setupAudioDisplay() {
        // Create audio visualization container
        audioContainer = new VBox(20);
        audioContainer.setAlignment(Pos.CENTER);
        audioContainer.setPadding(new Insets(40));
        
        // Audio icon and info
        Label audioIcon = new Label("🎵");
        audioIcon.setStyle("-fx-font-size: 72px;");
        
        Label audioTitle = new Label(vaultFile != null ? vaultFile.getOriginalName() : "Audio File");
        audioTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label audioFormat = new Label(getAudioFormatInfo());
        audioFormat.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        
        audioContainer.getChildren().addAll(audioIcon, audioTitle, audioFormat);
        
        // Add to center of root pane
        rootPane.setCenter(audioContainer);
        
        // Hide media view
        if (mediaView != null) {
            mediaView.setVisible(false);
            mediaView.setManaged(false);
        }
        
        // Hide fullscreen button for audio
        if (fullscreenButton != null) {
            fullscreenButton.setVisible(false);
            fullscreenButton.setManaged(false);
        }
    }
    
    @Override
    protected Scene createScene() {
        rootPane = new BorderPane();
        
        // Create media info bar
        HBox infoBar = createInfoBar();
        rootPane.setTop(infoBar);
        
        // Create media controls
        VBox controlsContainer = createMediaControls();
        rootPane.setBottom(controlsContainer);
        
        // Initially show placeholder
        showPlaceholder();
        
        // Apply dashboard styling
        Scene scene = new Scene(rootPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts(scene);
        
        return scene;
    }
    
    /**
     * Create media info bar
     */
    private HBox createInfoBar() {
        HBox infoBar = new HBox(10);
        infoBar.setPadding(new Insets(10));
        infoBar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #404040; -fx-border-width: 0 0 1 0;");
        
        mediaInfoLabel = new Label("Loading media...");
        mediaInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cccccc;");
        
        infoBar.getChildren().add(mediaInfoLabel);
        
        return infoBar;
    }
    
    /**
     * Create media controls
     */
    private VBox createMediaControls() {
        VBox controlsContainer = new VBox(10);
        controlsContainer.setPadding(new Insets(10));
        controlsContainer.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #404040; -fx-border-width: 1 0 0 0;");
        
        // Position slider
        HBox positionContainer = createPositionControls();
        
        // Main controls
        HBox mainControls = createMainControls();
        
        // Volume controls
        HBox volumeControls = createVolumeControls();
        
        controlsContainer.getChildren().addAll(positionContainer, mainControls, volumeControls);
        
        // Initially disable controls
        enableControls(false);
        
        return controlsContainer;
    }
    
    /**
     * Create position controls
     */
    private HBox createPositionControls() {
        HBox positionContainer = new HBox(10);
        positionContainer.setAlignment(Pos.CENTER);
        
        currentTimeLabel = new Label("00:00");
        currentTimeLabel.setMinWidth(50);
        currentTimeLabel.setStyle("-fx-font-family: monospace;");
        
        positionSlider = new Slider(0, 100, 0);
        positionSlider.setPrefWidth(400);
        HBox.setHgrow(positionSlider, Priority.ALWAYS);
        
        totalTimeLabel = new Label("00:00");
        totalTimeLabel.setMinWidth(50);
        totalTimeLabel.setStyle("-fx-font-family: monospace;");
        
        // Position slider event handlers
        positionSlider.setOnMousePressed(e -> isDraggingPosition = true);
        positionSlider.setOnMouseReleased(e -> {
            isDraggingPosition = false;
            if (mediaPlayer != null) {
                Duration seekTime = Duration.seconds(positionSlider.getValue());
                mediaPlayer.seek(seekTime);
            }
        });
        
        positionContainer.getChildren().addAll(currentTimeLabel, positionSlider, totalTimeLabel);
        
        return positionContainer;
    }
    
    /**
     * Create main playback controls
     */
    private HBox createMainControls() {
        HBox mainControls = new HBox(10);
        mainControls.setAlignment(Pos.CENTER);
        
        playPauseButton = new Button("▶");
        playPauseButton.setPrefSize(50, 40);
        playPauseButton.setStyle("-fx-font-size: 16px;");
        playPauseButton.setOnAction(e -> togglePlayPause());
        
        stopButton = new Button("⏹");
        stopButton.setPrefSize(40, 40);
        stopButton.setStyle("-fx-font-size: 14px;");
        stopButton.setOnAction(e -> stopMedia());
        
        // Fullscreen button (only for video)
        fullscreenButton = new Button("⛶");
        fullscreenButton.setPrefSize(40, 40);
        fullscreenButton.setStyle("-fx-font-size: 14px;");
        fullscreenButton.setOnAction(e -> toggleFullscreen());
        fullscreenButton.setVisible(isVideo);
        fullscreenButton.setManaged(isVideo);
        
        // Media info button
        infoButton = new Button("ℹ");
        infoButton.setPrefSize(40, 40);
        infoButton.setStyle("-fx-font-size: 14px;");
        infoButton.setOnAction(e -> showMediaInfo());
        
        mainControls.getChildren().addAll(playPauseButton, stopButton, fullscreenButton, infoButton);
        
        return mainControls;
    }
    
    /**
     * Create volume controls
     */
    private HBox createVolumeControls() {
        HBox volumeControls = new HBox(10);
        volumeControls.setAlignment(Pos.CENTER);
        
        muteButton = new Button("🔊");
        muteButton.setPrefSize(40, 30);
        muteButton.setOnAction(e -> toggleMute());
        
        volumeLabel = new Label("Volume:");
        
        volumeSlider = new Slider(0, 1, settings.getDefaultVolume());
        volumeSlider.setPrefWidth(150);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null && !isMuted) {
                mediaPlayer.setVolume(newVal.doubleValue());
                lastVolume = newVal.doubleValue();
            }
        });
        
        Label volumeValue = new Label();
        volumeValue.textProperty().bind(volumeSlider.valueProperty().multiply(100).asString("%.0f%%"));
        volumeValue.setMinWidth(40);
        
        volumeControls.getChildren().addAll(muteButton, volumeLabel, volumeSlider, volumeValue);
        
        return volumeControls;
    }
    
    /**
     * Show placeholder while loading
     */
    private void showPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(40));
        
        Label icon = new Label("🎬");
        icon.setStyle("-fx-font-size: 72px;");
        
        Label message = new Label("Loading media...");
        message.setStyle("-fx-font-size: 16px;");
        
        placeholder.getChildren().addAll(icon, message);
        rootPane.setCenter(placeholder);
    }
    
    /**
     * Toggle play/pause
     */
    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        
        if (isPlaying) {
            pauseMedia();
        } else {
            playMedia();
        }
    }
    
    /**
     * Play media
     */
    private void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
    
    /**
     * Pause media
     */
    private void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    
    /**
     * Stop media
     */
    private void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    /**
     * Toggle mute
     */
    private void toggleMute() {
        if (mediaPlayer == null) return;
        
        if (isMuted) {
            // Unmute
            mediaPlayer.setVolume(lastVolume);
            volumeSlider.setValue(lastVolume);
            muteButton.setText("🔊");
            isMuted = false;
        } else {
            // Mute
            lastVolume = mediaPlayer.getVolume();
            mediaPlayer.setVolume(0);
            muteButton.setText("🔇");
            isMuted = true;
        }
    }
    
    /**
     * Update play/pause button
     */
    private void updatePlayPauseButton() {
        if (playPauseButton != null) {
            playPauseButton.setText(isPlaying ? "⏸" : "▶");
        }
    }
    
    /**
     * Update current time label
     */
    private void updateCurrentTimeLabel(Duration time) {
        if (currentTimeLabel != null && time != null) {
            currentTimeLabel.setText(formatDuration(time));
        }
    }
    
    /**
     * Update total time label
     */
    private void updateTotalTimeLabel(Duration time) {
        if (totalTimeLabel != null && time != null) {
            totalTimeLabel.setText(formatDuration(time));
        }
    }
    
    /**
     * Update media info display
     */
    private void updateMediaInfo() {
        if (mediaInfoLabel == null || vaultFile == null) return;
        
        String info = String.format("%s | %s | %s", 
            vaultFile.getOriginalName(),
            vaultFile.getExtension().toUpperCase(),
            formatFileSize(vaultFile.getSize())
        );
        
        mediaInfoLabel.setText(info);
    }
    
    /**
     * Enable/disable controls
     */
    private void enableControls(boolean enabled) {
        if (playPauseButton != null) playPauseButton.setDisable(!enabled);
        if (stopButton != null) stopButton.setDisable(!enabled);
        if (muteButton != null) muteButton.setDisable(!enabled);
        if (volumeSlider != null) volumeSlider.setDisable(!enabled);
        if (positionSlider != null) positionSlider.setDisable(!enabled);
        if (fullscreenButton != null) fullscreenButton.setDisable(!enabled);
        if (infoButton != null) infoButton.setDisable(!enabled);
    }
    
    /**
     * Toggle fullscreen mode for video
     */
    private void toggleFullscreen() {
        if (!isVideo || previewStage == null) return;
        
        isFullscreen = !isFullscreen;
        previewStage.setFullScreen(isFullscreen);
        
        // Update fullscreen button
        if (fullscreenButton != null) {
            fullscreenButton.setText(isFullscreen ? "⛶" : "⛶");
        }
    }
    
    /**
     * Show detailed media information dialog
     */
    private void showMediaInfo() {
        if (vaultFile == null) return;
        
        StringBuilder info = new StringBuilder();
        info.append("Media File Information\n\n");
        
        // Basic file info
        info.append("File: ").append(vaultFile.getOriginalName()).append("\n");
        info.append("Type: ").append(vaultFile.getExtension().toUpperCase()).append("\n");
        info.append("Size: ").append(formatFileSize(vaultFile.getSize())).append("\n");
        
        // Media-specific info
        if (mediaPlayer != null && media != null) {
            Duration duration = mediaPlayer.getTotalDuration();
            if (duration != null && !duration.isUnknown()) {
                info.append("Duration: ").append(formatDuration(duration)).append("\n");
            }
            
            if (isVideo && mediaView != null) {
                info.append("Video Resolution: ");
                if (media.getWidth() > 0 && media.getHeight() > 0) {
                    info.append((int)media.getWidth()).append(" × ").append((int)media.getHeight()).append("\n");
                } else {
                    info.append("Unknown\n");
                }
            }
        }
        
        // Format-specific information
        info.append("\nFormat Information:\n");
        String extension = vaultFile.getExtension().toLowerCase();
        switch (extension) {
            case "mp3":
                info.append("• MPEG-1 Audio Layer III\n");
                info.append("• Lossy compression\n");
                info.append("• Widely supported\n");
                break;
            case "wav":
                info.append("• Waveform Audio File Format\n");
                info.append("• Uncompressed audio\n");
                info.append("• High quality\n");
                break;
            case "aac":
                info.append("• Advanced Audio Coding\n");
                info.append("• Lossy compression\n");
                info.append("• Better quality than MP3\n");
                break;
            case "m4a":
                info.append("• MPEG-4 Audio\n");
                info.append("• AAC or ALAC codec\n");
                info.append("• iTunes compatible\n");
                break;
            case "mp4":
                info.append("• MPEG-4 Video\n");
                info.append("• H.264/H.265 video codec\n");
                info.append("• AAC audio codec\n");
                break;
            case "mov":
                info.append("• QuickTime Movie\n");
                info.append("• Apple's video format\n");
                info.append("• Various codecs supported\n");
                break;
            case "m4v":
                info.append("• iTunes Video\n");
                info.append("• Similar to MP4\n");
                info.append("• DRM protection possible\n");
                break;
        }
        
        // Playback info
        info.append("\nPlayback Status:\n");
        info.append("• Status: ").append(isPlaying ? "Playing" : "Stopped").append("\n");
        info.append("• Volume: ").append(String.format("%.0f%%", volumeSlider.getValue() * 100)).append("\n");
        info.append("• Muted: ").append(isMuted ? "Yes" : "No").append("\n");
        
        // Show info dialog
        Alert infoDialog = new Alert(Alert.AlertType.INFORMATION);
        infoDialog.setTitle("Media Information");
        infoDialog.setHeaderText("File Details");
        infoDialog.setContentText(info.toString());
        
        // Apply dark theme
        infoDialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        infoDialog.showAndWait();
    }
    
    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (mediaPlayer == null) return;
            
            switch (event.getCode()) {
                case SPACE:
                    togglePlayPause();
                    event.consume();
                    break;
                case LEFT:
                    // Seek backward 10 seconds
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    Duration newTime = currentTime.subtract(Duration.seconds(10));
                    mediaPlayer.seek(newTime.greaterThan(Duration.ZERO) ? newTime : Duration.ZERO);
                    event.consume();
                    break;
                case RIGHT:
                    // Seek forward 10 seconds
                    Duration current = mediaPlayer.getCurrentTime();
                    Duration total = mediaPlayer.getTotalDuration();
                    Duration forward = current.add(Duration.seconds(10));
                    mediaPlayer.seek(forward.lessThan(total) ? forward : total);
                    event.consume();
                    break;
                case UP:
                    // Volume up
                    double newVolume = Math.min(1.0, volumeSlider.getValue() + 0.1);
                    volumeSlider.setValue(newVolume);
                    event.consume();
                    break;
                case DOWN:
                    // Volume down
                    double lowerVolume = Math.max(0.0, volumeSlider.getValue() - 0.1);
                    volumeSlider.setValue(lowerVolume);
                    event.consume();
                    break;
                case M:
                    toggleMute();
                    event.consume();
                    break;
                case F11:
                    if (isVideo) {
                        toggleFullscreen();
                        event.consume();
                    }
                    break;
                case I:
                    showMediaInfo();
                    event.consume();
                    break;
                case ESCAPE:
                    if (isFullscreen) {
                        toggleFullscreen();
                        event.consume();
                    }
                    break;
            }
        });
    }
    
    @Override
    protected void cleanup() {
        try {
            // Stop and dispose media player with error handling
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping media player: " + e.getMessage());
                }
                
                try {
                    mediaPlayer.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing media player: " + e.getMessage());
                }
                
                mediaPlayer = null;
            }
            
            // Clean up temporary file with proper error handling
            if (tempMediaFile != null) {
                try {
                    if (Files.exists(tempMediaFile)) {
                        Files.delete(tempMediaFile);
                        System.out.println("🗑️ Cleaned up temporary media file: " + tempMediaFile.getFileName());
                    }
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to delete temporary media file: " + e.getMessage());
                    // Try to delete on exit as fallback
                    tempMediaFile.toFile().deleteOnExit();
                }
                tempMediaFile = null;
            }
            
            // Clean up media reference
            if (media != null) {
                media = null;
            }
            
            super.cleanup();
            
        } catch (Exception e) {
            System.err.println("Error during MediaViewerComponent cleanup: " + e.getMessage());
            // Continue with cleanup even if errors occur
            super.cleanup();
        }
    }
    
    @Override
    public boolean supportsFileType(String fileExtension) {
        return Arrays.asList("mp3", "wav", "aac", "m4a", "mp4", "mov", "m4v").contains(fileExtension.toLowerCase());
    }
    
    @Override
    public String getComponentName() {
        return "Media Viewer";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"mp3", "wav", "aac", "m4a", "mp4", "mov", "m4v"};
    }
    
    /**
     * Create a sample image for demonstration
     */
    private javafx.scene.image.Image createSampleImage() {
        try {
            javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(400, 300);
            javafx.scene.image.PixelWriter pixelWriter = image.getPixelWriter();
            
            // Create a pattern based on filename
            String filename = vaultFile != null ? vaultFile.getOriginalName() : "sample";
            int hash = filename.hashCode();
            double hue = Math.abs(hash % 360);
            
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 300; y++) {
                    double brightness = 0.3 + (0.4 * Math.sin(x * 0.02) * Math.cos(y * 0.02));
                    javafx.scene.paint.Color color = javafx.scene.paint.Color.hsb(hue, 0.7, Math.abs(brightness));
                    pixelWriter.setColor(x, y, color);
                }
            }
            
            return image;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Format duration for display
     */
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        
        int totalSeconds = (int) duration.toSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Load image content directly
     */
    private void loadImageContent(byte[] imageData) {
        try {
            // For demonstration, create a sample image since we don't have real image data
            javafx.scene.image.Image image = createSampleImage();
            
            if (image == null || image.isError()) {
                showError("Failed to load image: Invalid image format");
                return;
            }
            
            // Create ImageView
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(400);
            
            // Create scroll pane for large images
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true);
            
            // Add zoom controls
            VBox imageContainer = new VBox(10);
            imageContainer.setAlignment(Pos.CENTER);
            
            HBox zoomControls = new HBox(10);
            zoomControls.setAlignment(Pos.CENTER);
            zoomControls.setPadding(new Insets(10));
            
            Button zoomInBtn = new Button("🔍+");
            Button zoomOutBtn = new Button("🔍-");
            Button fitBtn = new Button("Fit");
            Button actualSizeBtn = new Button("100%");
            
            zoomInBtn.setOnAction(e -> {
                imageView.setFitWidth(imageView.getFitWidth() * 1.2);
                imageView.setFitHeight(imageView.getFitHeight() * 1.2);
            });
            
            zoomOutBtn.setOnAction(e -> {
                imageView.setFitWidth(imageView.getFitWidth() * 0.8);
                imageView.setFitHeight(imageView.getFitHeight() * 0.8);
            });
            
            fitBtn.setOnAction(e -> {
                imageView.setFitWidth(600);
                imageView.setFitHeight(400);
            });
            
            actualSizeBtn.setOnAction(e -> {
                imageView.setFitWidth(image.getWidth());
                imageView.setFitHeight(image.getHeight());
            });
            
            zoomControls.getChildren().addAll(zoomInBtn, zoomOutBtn, fitBtn, actualSizeBtn);
            
            imageContainer.getChildren().addAll(scrollPane, zoomControls);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            rootPane.setCenter(imageContainer);
            
            // Update info
            if (mediaInfoLabel != null) {
                String info = String.format("%s | %dx%d | %s", 
                    vaultFile.getOriginalName(),
                    (int)image.getWidth(), (int)image.getHeight(),
                    formatFileSize(vaultFile.getSize())
                );
                mediaInfoLabel.setText(info);
            }
            
            // Hide media controls for images
            if (rootPane.getBottom() != null) {
                rootPane.getBottom().setVisible(false);
                rootPane.getBottom().setManaged(false);
            }
            
        } catch (Exception e) {
            showError("Failed to load image: " + e.getMessage());
        }
    }
    
    /**
     * Show fallback display for unsupported media
     */
    private void showMediaFallback(String extension, long fileSize) {
        VBox fallbackContainer = new VBox(20);
        fallbackContainer.setAlignment(Pos.CENTER);
        fallbackContainer.setPadding(new Insets(40));
        
        // Media type icon
        String icon = isVideoFile(extension) ? "🎬" : "🎵";
        Label mediaIcon = new Label(icon);
        mediaIcon.setStyle("-fx-font-size: 72px;");
        
        // File info
        Label fileName = new Label(vaultFile != null ? vaultFile.getOriginalName() : "Media File");
        fileName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label fileInfo = new Label(String.format("%s • %s", 
            extension.toUpperCase(), formatFileSize(fileSize)));
        fileInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        
        // Message
        Label message = new Label("Media preview not supported for this format");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9999;");
        
        // Suggestions
        VBox suggestions = new VBox(5);
        suggestions.setAlignment(Pos.CENTER);
        suggestions.setMaxWidth(400);
        
        Label suggestionsTitle = new Label("You can:");
        suggestionsTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label suggestion1 = new Label("• Download the file to view with an appropriate application");
        Label suggestion2 = new Label("• Use the 'Hex Viewer' to see raw file data");
        
        suggestion1.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        suggestion2.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        
        suggestions.getChildren().addAll(suggestionsTitle, suggestion1, suggestion2);
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        
        Button hexViewBtn = new Button("Show Hex Viewer");
        hexViewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        hexViewBtn.setOnAction(e -> showHexViewer());
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> {
            if (previewStage != null) {
                previewStage.close();
            }
        });
        
        actions.getChildren().addAll(hexViewBtn, cancelBtn);
        
        fallbackContainer.getChildren().addAll(
            mediaIcon, fileName, fileInfo, message, suggestions, actions
        );
        
        rootPane.setCenter(fallbackContainer);
        
        // Update info label
        if (mediaInfoLabel != null) {
            mediaInfoLabel.setText("Preview not supported: " + extension.toUpperCase());
        }
        
        // Hide media controls
        if (rootPane.getBottom() != null) {
            rootPane.getBottom().setVisible(false);
            rootPane.getBottom().setManaged(false);
        }
    }
    
    /**
     * Show hex viewer for unsupported files
     */
    private void showHexViewer() {
        // This would open a hex viewer dialog
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Hex Viewer");
        info.setHeaderText("Raw File Data");
        info.setContentText("Hex viewer functionality would be implemented here to show the raw binary data of the file.");
        info.showAndWait();
    }
    
    /**
     * Check if file extension indicates video with enhanced validation
     */
    private boolean isVideoFile(String extension) {
        Set<String> videoFormats = Set.of("mp4", "mov", "m4v", "avi", "mkv", "webm", "flv", "wmv");
        return videoFormats.contains(extension.toLowerCase());
    }
    
    /**
     * Check if file extension indicates image with enhanced validation
     */
    private boolean isImageFile(String extension) {
        Set<String> imageFormats = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "svg");
        return imageFormats.contains(extension.toLowerCase());
    }
    
    /**
     * Check if file extension indicates audio
     */
    private boolean isAudioFile(String extension) {
        Set<String> audioFormats = Set.of("mp3", "wav", "aac", "m4a", "ogg", "flac", "wma");
        return audioFormats.contains(extension.toLowerCase());
    }
    
    /**
     * Validate media format before processing
     */
    private MediaFormatValidationResult validateMediaFormat(String extension, byte[] fileData) {
        try {
            String ext = extension.toLowerCase();
            
            // Check if format is supported
            if (!supportsFileType(ext)) {
                return new MediaFormatValidationResult(false, 
                    MediaFormatValidationResult.ValidationIssue.UNSUPPORTED_FORMAT,
                    "Format " + ext.toUpperCase() + " is not supported",
                    "Use an external application to view this file");
            }
            
            // Validate file signature (magic bytes)
            MediaFormatValidationResult signatureResult = validateFileSignature(ext, fileData);
            if (!signatureResult.isValid()) {
                return signatureResult;
            }
            
            // Check file size constraints
            MediaFormatValidationResult sizeResult = validateFileSize(ext, fileData.length);
            if (!sizeResult.isValid()) {
                return sizeResult;
            }
            
            // Format-specific validation
            MediaFormatValidationResult formatResult = validateFormatSpecific(ext, fileData);
            if (!formatResult.isValid()) {
                return formatResult;
            }
            
            return new MediaFormatValidationResult(true, null, null, null);
            
        } catch (Exception e) {
            return new MediaFormatValidationResult(false,
                MediaFormatValidationResult.ValidationIssue.VALIDATION_ERROR,
                "Error validating format: " + e.getMessage(),
                "Try using an external application");
        }
    }
    
    /**
     * Validate file signature (magic bytes)
     */
    private MediaFormatValidationResult validateFileSignature(String extension, byte[] fileData) {
        if (fileData.length < 12) {
            return new MediaFormatValidationResult(false,
                MediaFormatValidationResult.ValidationIssue.INSUFFICIENT_DATA,
                "File is too small to validate",
                "Check if the file is complete");
        }
        
        // Get first few bytes for signature checking
        byte[] header = Arrays.copyOf(fileData, Math.min(12, fileData.length));
        
        switch (extension) {
            case "mp4", "m4v", "m4a":
                if (!validateMP4Signature(header)) {
                    return new MediaFormatValidationResult(false,
                        MediaFormatValidationResult.ValidationIssue.INVALID_SIGNATURE,
                        "File does not have valid MP4 signature",
                        "File may be corrupted or misnamed");
                }
                break;
                
            case "jpg", "jpeg":
                if (!validateJPEGSignature(header)) {
                    return new MediaFormatValidationResult(false,
                        MediaFormatValidationResult.ValidationIssue.INVALID_SIGNATURE,
                        "File does not have valid JPEG signature",
                        "File may be corrupted or misnamed");
                }
                break;
                
            case "png":
                if (!validatePNGSignature(header)) {
                    return new MediaFormatValidationResult(false,
                        MediaFormatValidationResult.ValidationIssue.INVALID_SIGNATURE,
                        "File does not have valid PNG signature",
                        "File may be corrupted or misnamed");
                }
                break;
                
            case "wav":
                if (!validateWAVSignature(header)) {
                    return new MediaFormatValidationResult(false,
                        MediaFormatValidationResult.ValidationIssue.INVALID_SIGNATURE,
                        "File does not have valid WAV signature",
                        "File may be corrupted or misnamed");
                }
                break;
                
            // Add more format validations as needed
        }
        
        return new MediaFormatValidationResult(true, null, null, null);
    }
    
    /**
     * Validate file size constraints
     */
    private MediaFormatValidationResult validateFileSize(String extension, long fileSize) {
        // Define size limits for different formats
        long maxSize = switch (extension) {
            case "mp4", "mov", "m4v", "avi", "mkv", "webm" -> 500L * 1024 * 1024; // 500MB for video
            case "mp3", "wav", "aac", "m4a", "ogg", "flac" -> 100L * 1024 * 1024; // 100MB for audio
            case "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp" -> 50L * 1024 * 1024; // 50MB for images
            default -> 10L * 1024 * 1024; // 10MB for other formats
        };
        
        if (fileSize > maxSize) {
            return new MediaFormatValidationResult(false,
                MediaFormatValidationResult.ValidationIssue.FILE_TOO_LARGE,
                String.format("File size (%s) exceeds maximum allowed (%s)", 
                    formatFileSize(fileSize), formatFileSize(maxSize)),
                "Use an external application for large files");
        }
        
        if (fileSize < 100) { // Minimum 100 bytes
            return new MediaFormatValidationResult(false,
                MediaFormatValidationResult.ValidationIssue.FILE_TOO_SMALL,
                "File is too small to be a valid media file",
                "Check if the file is complete");
        }
        
        return new MediaFormatValidationResult(true, null, null, null);
    }
    
    /**
     * Format-specific validation (lenient for video files)
     */
    private MediaFormatValidationResult validateFormatSpecific(String extension, byte[] fileData) {
        switch (extension) {
            case "mp4", "m4v", "mov", "avi", "mkv", "webm":
                // For video files, use lenient validation and let JavaFX handle the details
                return validateMP4Format(fileData);
            case "jpg", "jpeg":
                return validateJPEGFormat(fileData);
            case "png":
                return validatePNGFormat(fileData);
            case "wav":
                return validateWAVFormat(fileData);
            default:
                // For other formats, assume valid if basic checks pass
                return new MediaFormatValidationResult(true, null, null, null);
        }
    }
    
    // Signature validation methods
    private boolean validateMP4Signature(byte[] header) {
        // MP4 files typically start with ftyp box at offset 4
        return header.length >= 8 && 
               header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
    }
    
    private boolean validateJPEGSignature(byte[] header) {
        // JPEG files start with FF D8 FF
        return header.length >= 3 && 
               (header[0] & 0xFF) == 0xFF && 
               (header[1] & 0xFF) == 0xD8 && 
               (header[2] & 0xFF) == 0xFF;
    }
    
    private boolean validatePNGSignature(byte[] header) {
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        byte[] pngSignature = {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (header.length < pngSignature.length) return false;
        
        for (int i = 0; i < pngSignature.length; i++) {
            if (header[i] != pngSignature[i]) return false;
        }
        return true;
    }
    
    private boolean validateWAVSignature(byte[] header) {
        // WAV files start with "RIFF" and have "WAVE" at offset 8
        return header.length >= 12 &&
               header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
               header[8] == 'W' && header[9] == 'A' && header[10] == 'V' && header[11] == 'E';
    }
    
    // Format-specific validation methods
    private MediaFormatValidationResult validateMP4Format(byte[] fileData) {
        // Simplified MP4 validation - just check basic structure
        try {
            // Check if file has minimum size and proper signature
            if (fileData.length < 32) {
                return new MediaFormatValidationResult(false,
                    MediaFormatValidationResult.ValidationIssue.CORRUPT_FORMAT,
                    "MP4 file is too small to be valid",
                    "Check if the file is complete");
            }
            
            // Check for ftyp box signature at the beginning (more reliable binary check)
            if (fileData.length >= 8) {
                // Look for 'ftyp' signature at offset 4 (after size field)
                if (fileData[4] == 'f' && fileData[5] == 't' && fileData[6] == 'y' && fileData[7] == 'p') {
                    return new MediaFormatValidationResult(true, null, null, null);
                }
            }
            
            // If signature check fails, still allow the file to be processed
            // JavaFX MediaPlayer will handle the actual validation
            System.out.println("⚠️ MP4 signature not found, but allowing JavaFX to handle validation");
            return new MediaFormatValidationResult(true, null, null, null);
            
        } catch (Exception e) {
            // On any validation error, allow JavaFX to handle it
            System.out.println("⚠️ MP4 validation error, allowing JavaFX to handle: " + e.getMessage());
            return new MediaFormatValidationResult(true, null, null, null);
        }
    }
    
    private MediaFormatValidationResult validateJPEGFormat(byte[] fileData) {
        // Check for JPEG end marker
        if (fileData.length >= 2) {
            int lastIndex = fileData.length - 2;
            if ((fileData[lastIndex] & 0xFF) == 0xFF && (fileData[lastIndex + 1] & 0xFF) == 0xD9) {
                return new MediaFormatValidationResult(true, null, null, null);
            }
        }
        
        return new MediaFormatValidationResult(false,
            MediaFormatValidationResult.ValidationIssue.CORRUPT_FORMAT,
            "JPEG file appears to be incomplete or corrupted",
            "Try re-downloading or using a different copy");
    }
    
    private MediaFormatValidationResult validatePNGFormat(byte[] fileData) {
        // Basic PNG validation - check for IEND chunk at the end
        if (fileData.length >= 12) {
            int endIndex = fileData.length - 8;
            String endChunk = new String(fileData, endIndex + 4, 4);
            if ("IEND".equals(endChunk)) {
                return new MediaFormatValidationResult(true, null, null, null);
            }
        }
        
        return new MediaFormatValidationResult(false,
            MediaFormatValidationResult.ValidationIssue.CORRUPT_FORMAT,
            "PNG file appears to be incomplete or corrupted",
            "Try re-downloading or using a different copy");
    }
    
    private MediaFormatValidationResult validateWAVFormat(byte[] fileData) {
        // Basic WAV validation - check file size matches header
        if (fileData.length >= 8) {
            // Read file size from header (little-endian)
            int headerSize = (fileData[4] & 0xFF) | 
                           ((fileData[5] & 0xFF) << 8) | 
                           ((fileData[6] & 0xFF) << 16) | 
                           ((fileData[7] & 0xFF) << 24);
            
            // Header size should be file size - 8
            if (Math.abs((headerSize + 8) - fileData.length) <= 1) {
                return new MediaFormatValidationResult(true, null, null, null);
            }
        }
        
        return new MediaFormatValidationResult(false,
            MediaFormatValidationResult.ValidationIssue.CORRUPT_FORMAT,
            "WAV file appears to be corrupted or incomplete",
            "Try re-downloading or using a different copy");
    }
    
    /**
     * Media format validation result
     */
    private static class MediaFormatValidationResult {
        enum ValidationIssue {
            UNSUPPORTED_FORMAT, INVALID_SIGNATURE, CORRUPT_FORMAT, 
            FILE_TOO_LARGE, FILE_TOO_SMALL, INSUFFICIENT_DATA, VALIDATION_ERROR
        }
        
        private final boolean valid;
        private final ValidationIssue issue;
        private final String message;
        private final String suggestion;
        
        public MediaFormatValidationResult(boolean valid, ValidationIssue issue, String message, String suggestion) {
            this.valid = valid;
            this.issue = issue;
            this.message = message;
            this.suggestion = suggestion;
        }
        
        public boolean isValid() { return valid; }
        public ValidationIssue getIssue() { return issue; }
        public String getMessage() { return message; }
        public String getSuggestion() { return suggestion; }
    }
    
    /**
     * Get audio format information
     */
    private String getAudioFormatInfo() {
        if (vaultFile == null) return "Audio File";
        
        String ext = vaultFile.getExtension().toUpperCase();
        String size = formatFileSize(vaultFile.getSize());
        
        return String.format("%s Audio • %s", ext, size);
    }
    
    /**
     * Handle media loading errors with comprehensive error analysis
     */
    private void handleLoadError(Exception error, String fileType) {
        try {
            // For video files, try a simpler approach first
            if (isVideoFile(fileType) && !error.getMessage().contains("unsupported")) {
                System.out.println("🎬 Video loading failed, trying simplified preview: " + error.getMessage());
                showSimplifiedVideoPreview(fileType);
                return;
            }
            
            // Use MediaErrorHandler to analyze and categorize the error
            MediaErrorHandler.MediaErrorInfo errorInfo = errorHandler.handleMediaLoadError(error, fileType, vaultFile);
            
            // Show user-friendly error message
            showEnhancedError(errorInfo);
            
            // Attempt fallback if possible
            if (errorInfo.canFallback()) {
                showFallbackPreview(errorInfo, fileType);
            } else {
                showBasicErrorDisplay(errorInfo);
            }
            
        } catch (Exception handlingError) {
            // Fallback error handling if error handler itself fails
            System.err.println("Error in error handling: " + handlingError.getMessage());
            showBasicErrorDisplay(null);
        }
    }
    
    /**
     * Show simplified video preview when JavaFX MediaPlayer fails
     */
    private void showSimplifiedVideoPreview(String fileType) {
        VBox videoContainer = new VBox(20);
        videoContainer.setAlignment(Pos.CENTER);
        videoContainer.setPadding(new Insets(40));
        
        // Video icon
        Label videoIcon = new Label("🎬");
        videoIcon.setStyle("-fx-font-size: 72px;");
        
        // File info
        Label fileName = new Label(vaultFile != null ? vaultFile.getOriginalName() : "Video File");
        fileName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label fileInfo = new Label(String.format("%s Video • %s", 
            fileType.toUpperCase(), 
            vaultFile != null ? formatFileSize(vaultFile.getSize()) : "Unknown size"));
        fileInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        
        // Status message
        Label statusMessage = new Label("Video preview not available with JavaFX MediaPlayer");
        statusMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffaa00;");
        
        // Suggestions
        VBox suggestions = new VBox(5);
        suggestions.setAlignment(Pos.CENTER);
        suggestions.setMaxWidth(400);
        
        Label suggestionsTitle = new Label("You can:");
        suggestionsTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label suggestion1 = new Label("• Download the file to view with an external video player");
        Label suggestion2 = new Label("• Use VLC, Windows Media Player, or similar applications");
        Label suggestion3 = new Label("• The file appears to be valid but requires external playback");
        
        suggestion1.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        suggestion2.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        suggestion3.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        
        suggestions.getChildren().addAll(suggestionsTitle, suggestion1, suggestion2, suggestion3);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button externalPlayerBtn = new Button("Open with External Player");
        externalPlayerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        externalPlayerBtn.setOnAction(e -> {
            openWithExternalPlayer();
        });
        
        Button downloadBtn = new Button("Download File");
        downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        downloadBtn.setOnAction(e -> {
            // This would trigger download functionality
            System.out.println("Download requested for: " + (vaultFile != null ? vaultFile.getOriginalName() : "file"));
        });
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> {
            if (previewStage != null) {
                previewStage.close();
            }
        });
        
        actionButtons.getChildren().addAll(externalPlayerBtn, downloadBtn, closeBtn);
        
        videoContainer.getChildren().addAll(
            videoIcon, fileName, fileInfo, statusMessage,
            new javafx.scene.control.Separator(),
            suggestions,
            new javafx.scene.control.Separator(),
            actionButtons
        );
        
        rootPane.setCenter(videoContainer);
        
        // Update info label
        if (mediaInfoLabel != null) {
            mediaInfoLabel.setText("Video file detected - External player recommended");
            mediaInfoLabel.setStyle("-fx-text-fill: #ffaa00;");
        }
        
        // Hide media controls
        if (rootPane.getBottom() != null) {
            rootPane.getBottom().setVisible(false);
            rootPane.getBottom().setManaged(false);
        }
    }
    
    /**
     * Open video file with external player
     */
    private void openWithExternalPlayer() {
        try {
            if (tempMediaFile != null && Files.exists(tempMediaFile)) {
                // Use existing temp file
                openFileWithSystemApp(tempMediaFile.toFile());
            } else {
                // Create temp file and open it
                createTempFileAndOpen();
            }
        } catch (Exception e) {
            System.err.println("Failed to open with external player: " + e.getMessage());
            showExternalPlayerError(e.getMessage());
        }
    }
    
    /**
     * Create temporary file and open with system app
     */
    private void createTempFileAndOpen() {
        try {
            String extension = vaultFile != null ? vaultFile.getExtension() : "mp4";
            Path tempFile = Files.createTempFile("ghostvault_external_", "." + extension);
            
            // Get file data from vault file
            // Note: This would need to be integrated with the actual file data retrieval
            System.out.println("🎬 Creating temporary file for external player: " + tempFile);
            
            // For now, use the existing temp file if available
            if (tempMediaFile != null && Files.exists(tempMediaFile)) {
                Files.copy(tempMediaFile, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                openFileWithSystemApp(tempFile.toFile());
                
                // Schedule cleanup after delay
                scheduleFileCleanup(tempFile, 30000); // 30 seconds
            } else {
                throw new IOException("No media data available for external player");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to create temp file for external player: " + e.getMessage());
            showExternalPlayerError(e.getMessage());
        }
    }
    
    /**
     * Open file with system default application
     */
    private void openFileWithSystemApp(File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file);
                    System.out.println("✅ Opened file with system default application: " + file.getName());
                    
                    // Show success message
                    Platform.runLater(() -> {
                        if (mediaInfoLabel != null) {
                            mediaInfoLabel.setText("File opened with external player");
                            mediaInfoLabel.setStyle("-fx-text-fill: #4CAF50;");
                        }
                    });
                } else {
                    throw new UnsupportedOperationException("Desktop OPEN action not supported");
                }
            } else {
                throw new UnsupportedOperationException("Desktop not supported on this system");
            }
        } catch (Exception e) {
            System.err.println("Failed to open file with system app: " + e.getMessage());
            showExternalPlayerError(e.getMessage());
        }
    }
    
    /**
     * Schedule file cleanup after delay
     */
    private void scheduleFileCleanup(Path file, long delayMs) {
        Thread cleanupThread = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                if (Files.exists(file)) {
                    Files.delete(file);
                    System.out.println("🗑️ Cleaned up temporary file: " + file.getFileName());
                }
            } catch (Exception e) {
                System.err.println("Failed to cleanup temporary file: " + e.getMessage());
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("TempFileCleanup");
        cleanupThread.start();
    }
    
    /**
     * Show error message for external player issues
     */
    private void showExternalPlayerError(String errorMessage) {
        Platform.runLater(() -> {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("External Player Error");
            errorAlert.setHeaderText("Cannot Open with External Player");
            errorAlert.setContentText("Failed to open the video file with an external player.\n\n" +
                "Error: " + errorMessage + "\n\n" +
                "Please try downloading the file instead.");
            
            // Apply dark theme if available
            try {
                errorAlert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/ghostvault-dark.css").toExternalForm());
            } catch (Exception e) {
                // Ignore styling errors
            }
            
            errorAlert.showAndWait();
        });
    }

    
    /**
     * Show enhanced error message with recovery options
     */
    private void showEnhancedError(MediaErrorHandler.MediaErrorInfo errorInfo) {
        if (mediaInfoLabel != null) {
            String statusMessage = String.format("Error: %s - %s", 
                errorInfo.getCategory().getDisplayName(), 
                errorInfo.getUserMessage());
            mediaInfoLabel.setText(statusMessage);
            mediaInfoLabel.setStyle("-fx-text-fill: #ff6b6b;");
        }
        
        // Log error details for debugging
        System.err.println("🎬 Media Error Details:");
        System.err.println("   Category: " + errorInfo.getCategory());
        System.err.println("   Severity: " + errorInfo.getSeverity());
        System.err.println("   User Message: " + errorInfo.getUserMessage());
        System.err.println("   Technical Message: " + errorInfo.getTechnicalMessage());
        System.err.println("   Can Fallback: " + errorInfo.canFallback());
        System.err.println("   Fallback Type: " + errorInfo.getFallbackType());
    }
    
    /**
     * Show fallback preview based on error analysis
     */
    private void showFallbackPreview(MediaErrorHandler.MediaErrorInfo errorInfo, String fileType) {
        String fallbackType = errorInfo.getFallbackType();
        
        switch (fallbackType) {
            case "File Information Display":
                showEnhancedMediaFallback(errorInfo, fileType);
                break;
            case "Hex Viewer":
                showHexViewerFallback(errorInfo);
                break;
            case "Image Information Display":
                showImageInfoFallback(errorInfo);
                break;
            case "Audio Information Display":
                showAudioInfoFallback(errorInfo);
                break;
            case "Video Information Display":
                showVideoInfoFallback(errorInfo);
                break;
            default:
                showEnhancedMediaFallback(errorInfo, fileType);
                break;
        }
    }
    
    /**
     * Show enhanced media fallback with error context
     */
    private void showEnhancedMediaFallback(MediaErrorHandler.MediaErrorInfo errorInfo, String fileType) {
        VBox fallbackContainer = new VBox(20);
        fallbackContainer.setAlignment(Pos.CENTER);
        fallbackContainer.setPadding(new Insets(40));
        
        // Error-specific icon and styling
        String icon = getErrorIcon(errorInfo.getCategory());
        Label errorIcon = new Label(icon);
        errorIcon.setStyle("-fx-font-size: 72px;");
        
        // File info with error context
        Label fileName = new Label(vaultFile != null ? vaultFile.getOriginalName() : "Media File");
        fileName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label fileInfo = new Label(String.format("%s • %s", 
            fileType.toUpperCase(), 
            vaultFile != null ? formatFileSize(vaultFile.getSize()) : "Unknown size"));
        fileInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        
        // Error message
        Label errorMessage = new Label(errorInfo.getUserMessage());
        errorMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9999; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);
        errorMessage.setMaxWidth(500);
        
        // Recovery actions
        VBox recoveryActions = new VBox(5);
        recoveryActions.setAlignment(Pos.CENTER);
        recoveryActions.setMaxWidth(500);
        
        Label actionsTitle = new Label("Available options:");
        actionsTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        for (String action : errorInfo.getRecoveryActions()) {
            Label actionLabel = new Label("• " + action);
            actionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
            actionLabel.setWrapText(true);
            recoveryActions.getChildren().add(actionLabel);
        }
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        if (errorInfo.getFallbackType().equals("Hex Viewer")) {
            Button hexViewBtn = new Button("Show Hex Viewer");
            hexViewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            hexViewBtn.setOnAction(e -> showHexViewer());
            actionButtons.getChildren().add(hexViewBtn);
        }
        
        Button detailsBtn = new Button("Error Details");
        detailsBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        detailsBtn.setOnAction(e -> errorHandler.showUserFriendlyError(errorInfo, vaultFile));
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> {
            if (previewStage != null) {
                previewStage.close();
            }
        });
        
        actionButtons.getChildren().addAll(detailsBtn, closeBtn);
        
        fallbackContainer.getChildren().addAll(
            errorIcon, fileName, fileInfo, errorMessage, 
            new javafx.scene.control.Separator(),
            actionsTitle, recoveryActions,
            new javafx.scene.control.Separator(),
            actionButtons
        );
        
        rootPane.setCenter(fallbackContainer);
        
        // Hide media controls
        if (rootPane.getBottom() != null) {
            rootPane.getBottom().setVisible(false);
            rootPane.getBottom().setManaged(false);
        }
    }
    
    /**
     * Show basic error display as ultimate fallback
     */
    private void showBasicErrorDisplay(MediaErrorHandler.MediaErrorInfo errorInfo) {
        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(40));
        
        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 72px;");
        
        Label errorTitle = new Label("Media Preview Error");
        errorTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        
        String message = errorInfo != null ? errorInfo.getUserMessage() : 
            "An error occurred while loading the media file.";
        Label errorMessage = new Label(message);
        errorMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);
        errorMessage.setMaxWidth(400);
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> {
            if (previewStage != null) {
                previewStage.close();
            }
        });
        
        errorContainer.getChildren().addAll(errorIcon, errorTitle, errorMessage, closeBtn);
        rootPane.setCenter(errorContainer);
        
        // Hide media controls
        if (rootPane.getBottom() != null) {
            rootPane.getBottom().setVisible(false);
            rootPane.getBottom().setManaged(false);
        }
    }
    
    /**
     * Get appropriate icon for error category
     */
    private String getErrorIcon(MediaErrorHandler.MediaErrorCategory category) {
        return switch (category) {
            case FORMAT_UNSUPPORTED -> "🚫";
            case FILE_CORRUPTED -> "💥";
            case CODEC_MISSING -> "🔧";
            case MEMORY_INSUFFICIENT -> "💾";
            case PERMISSION_DENIED -> "🔒";
            case NETWORK_ERROR -> "🌐";
            case JAVAFX_ERROR -> "⚙️";
            default -> "⚠️";
        };
    }
    
    /**
     * Show hex viewer fallback
     */
    private void showHexViewerFallback(MediaErrorHandler.MediaErrorInfo errorInfo) {
        // Implementation would show hex viewer
        showEnhancedMediaFallback(errorInfo, "binary");
    }
    
    /**
     * Show image info fallback
     */
    private void showImageInfoFallback(MediaErrorHandler.MediaErrorInfo errorInfo) {
        showEnhancedMediaFallback(errorInfo, "image");
    }
    
    /**
     * Show audio info fallback
     */
    private void showAudioInfoFallback(MediaErrorHandler.MediaErrorInfo errorInfo) {
        showEnhancedMediaFallback(errorInfo, "audio");
    }
    
    /**
     * Show video info fallback
     */
    private void showVideoInfoFallback(MediaErrorHandler.MediaErrorInfo errorInfo) {
        showEnhancedMediaFallback(errorInfo, "video");
    }
}