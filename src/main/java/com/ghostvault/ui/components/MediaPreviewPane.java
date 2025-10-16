package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Modern media preview component with support for images, audio, and video
 */
public class MediaPreviewPane extends VBox {
    
    // Supported file types
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".svg", ".webp"
    );
    
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
        ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v"
    );
    
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
        ".mp3", ".wav", ".aac", ".flac", ".ogg", ".m4a", ".wma"
    );
    
    // UI Components
    private HBox headerPane;
    private Label fileNameLabel;
    private Label fileInfoLabel;
    private StackPane contentPane;
    private HBox controlsPane;
    
    // Media components
    private ImageView imageView;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    
    // Media controls
    private Button playPauseButton;
    private Button stopButton;
    private Button muteButton;
    private Slider timeSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Label durationLabel;
    
    // State
    private File currentFile;
    private MediaType currentMediaType = MediaType.NONE;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    
    public enum MediaType {
        NONE, IMAGE, AUDIO, VIDEO
    }
    
    public MediaPreviewPane() {
        initializeComponents();
        setupLayout();
        setupStyling();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Header
        headerPane = new HBox(10);
        headerPane.setAlignment(Pos.CENTER_LEFT);
        headerPane.setPadding(new Insets(12, 16, 12, 16));
        headerPane.getStyleClass().add("media-preview-header");
        
        fileNameLabel = new Label();
        fileNameLabel.getStyleClass().add("media-file-name");
        
        fileInfoLabel = new Label();
        fileInfoLabel.getStyleClass().add("media-file-info");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button fullscreenButton = new Button("⛶");
        fullscreenButton.getStyleClass().addAll("button", "icon");
        fullscreenButton.setTooltip(new Tooltip("Fullscreen"));
        
        headerPane.getChildren().addAll(fileNameLabel, fileInfoLabel, spacer, fullscreenButton);
        
        // Content pane
        contentPane = new StackPane();
        contentPane.getStyleClass().add("media-content-pane");
        contentPane.setPrefSize(600, 400);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        
        // Image view
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        // Media view for video
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        
        // Controls pane
        controlsPane = new HBox(12);
        controlsPane.setAlignment(Pos.CENTER);
        controlsPane.setPadding(new Insets(12, 16, 12, 16));
        controlsPane.getStyleClass().add("media-controls-pane");
        controlsPane.setVisible(false);
        
        initializeMediaControls();
        
        this.getChildren().addAll(headerPane, contentPane, controlsPane);
    }
    
    /**
     * Initialize media control components
     */
    private void initializeMediaControls() {
        // Play/Pause button\n        playPauseButton = new Button("▶");
        playPauseButton.getStyleClass().addAll("button", "primary", "icon");
        playPauseButton.setTooltip(new Tooltip("Play/Pause"));
        playPauseButton.setOnAction(e -> togglePlayPause());
        
        // Stop button
        stopButton = new Button("⏹");
        stopButton.getStyleClass().addAll("button", "icon");
        stopButton.setTooltip(new Tooltip("Stop"));
        stopButton.setOnAction(e -> stopMedia());
        
        // Time slider
        timeSlider = new Slider();
        timeSlider.getStyleClass().add("time-slider");
        timeSlider.setPrefWidth(300);
        timeSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });
        
        // Time labels
        timeLabel = new Label("00:00");
        timeLabel.getStyleClass().add("time-label");
        
        durationLabel = new Label("00:00");
        durationLabel.getStyleClass().add("time-label");
        
        // Volume controls
        muteButton = new Button("🔊");
        muteButton.getStyleClass().addAll("button", "icon");
        muteButton.setTooltip(new Tooltip("Mute/Unmute"));
        muteButton.setOnAction(e -> toggleMute());
        
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setPrefWidth(80);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
                updateVolumeIcon();
            }
        });
        
        controlsPane.getChildren().addAll(
            playPauseButton,
            stopButton,
            timeLabel,
            timeSlider,
            durationLabel,
            muteButton,
            volumeSlider
        );
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        this.setSpacing(0);
        this.setPrefWidth(600);
        this.setPrefHeight(500);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        this.getStyleClass().add("media-preview-pane");
    }
    
    /**
     * Load and display a media file
     */
    public void loadFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            showError("File not found or invalid");
            return;
        }
        
        // Clean up previous media
        cleanup();
        
        this.currentFile = file;
        this.currentMediaType = detectMediaType(file);
        
        // Update header info
        fileNameLabel.setText(file.getName());
        
        switch (currentMediaType) {
            case IMAGE:
                loadImage(file);
                break;
            case AUDIO:
                loadAudio(file);
                break;
            case VIDEO:
                loadVideo(file);
                break;
            default:
                showUnsupportedMedia(file.getName(), "Unsupported media format");
        }
    }
    
    /**
     * Load and display an image
     */
    private void loadImage(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            
            // Bind image size to content pane
            imageView.fitWidthProperty().bind(
                Bindings.min(contentPane.widthProperty().subtract(20), 
                            Bindings.multiply(image.getWidth(), 
                                            Bindings.min(
                                                Bindings.divide(contentPane.widthProperty().subtract(20), image.getWidth()),
                                                Bindings.divide(contentPane.heightProperty().subtract(20), image.getHeight())
                                            )))
            );
            
            imageView.fitHeightProperty().bind(
                Bindings.min(contentPane.heightProperty().subtract(20),
                            Bindings.multiply(image.getHeight(),
                                            Bindings.min(
                                                Bindings.divide(contentPane.widthProperty().subtract(20), image.getWidth()),
                                                Bindings.divide(contentPane.heightProperty().subtract(20), image.getHeight())
                                            )))
            );
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(imageView);
            
            // Update file info
            fileInfoLabel.setText(String.format("%.0f × %.0f • %s", 
                image.getWidth(), image.getHeight(), formatFileSize(file.length())));
            
            controlsPane.setVisible(false);
            
        } catch (Exception e) {
            showError("Failed to load image: " + e.getMessage());
        }
    }
    
    /**
     * Load and play audio
     */
    private void loadAudio(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            // Setup media player listeners
            setupMediaPlayerListeners();
            
            // Show audio visualization or album art placeholder
            Label audioIcon = new Label("🎵");
            audioIcon.getStyleClass().add("audio-icon");
            audioIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #5865f2;");
            
            VBox audioContainer = new VBox(16);
            audioContainer.setAlignment(Pos.CENTER);
            audioContainer.getChildren().addAll(
                audioIcon,
                new Label(file.getName())
            );
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(audioContainer);
            
            controlsPane.setVisible(true);
            
            // Update file info
            fileInfoLabel.setText(formatFileSize(file.length()));
            
        } catch (Exception e) {
            showError("Failed to load audio: " + e.getMessage());
        }
    }
    
    /**
     * Load and play video
     */
    private void loadVideo(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            // Setup media player listeners
            setupMediaPlayerListeners();
            
            // Bind video size to content pane
            mediaView.fitWidthProperty().bind(contentPane.widthProperty().subtract(20));
            mediaView.fitHeightProperty().bind(contentPane.heightProperty().subtract(20));
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(mediaView);
            
            controlsPane.setVisible(true);
            
            // Update file info when media is ready
            mediaPlayer.setOnReady(() -> {
                Platform.runLater(() -> {
                    fileInfoLabel.setText(String.format("%.0f × %.0f • %s", 
                        mediaView.getMediaPlayer().getMedia().getWidth(),
                        mediaView.getMediaPlayer().getMedia().getHeight(),
                        formatFileSize(file.length())));
                });
            });
            
        } catch (Exception e) {
            showError("Failed to load video: " + e.getMessage());
        }
    }
    
    /**
     * Setup media player event listeners
     */
    private void setupMediaPlayerListeners() {
        if (mediaPlayer == null) return;
        
        // Update duration when ready
        mediaPlayer.setOnReady(() -> {
            Platform.runLater(() -> {
                Duration duration = mediaPlayer.getTotalDuration();
                timeSlider.setMax(duration.toSeconds());
                durationLabel.setText(formatTime(duration));
            });
        });
        
        // Update time slider during playback
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Platform.runLater(() -> {
                if (!timeSlider.isPressed()) {
                    timeSlider.setValue(newTime.toSeconds());
                }
                timeLabel.setText(formatTime(newTime));
            });
        });
        
        // Handle end of media
        mediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(() -> {
                isPlaying = false;
                playPauseButton.setText("▶");
                timeSlider.setValue(0);
                timeLabel.setText("00:00");
            });
        });
        
        // Handle errors
        mediaPlayer.setOnError(() -> {
            Platform.runLater(() -> {
                showError("Media playback error: " + mediaPlayer.getError().getMessage());
            });
        });
    }
    
    /**
     * Toggle play/pause
     */
    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        
        if (isPlaying) {
            mediaPlayer.pause();
            playPauseButton.setText("▶");
            isPlaying = false;
        } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸");
            isPlaying = true;
        }
    }
    
    /**
     * Stop media playback
     */
    private void stopMedia() {
        if (mediaPlayer == null) return;
        
        mediaPlayer.stop();
        playPauseButton.setText("▶");
        isPlaying = false;
        timeSlider.setValue(0);
        timeLabel.setText("00:00");
    }
    
    /**
     * Toggle mute
     */
    private void toggleMute() {
        if (mediaPlayer == null) return;
        
        isMuted = !isMuted;
        mediaPlayer.setMute(isMuted);
        updateVolumeIcon();
    }
    
    /**
     * Update volume icon based on current volume
     */
    private void updateVolumeIcon() {
        if (mediaPlayer == null) return;
        
        if (isMuted || mediaPlayer.getVolume() == 0) {
            muteButton.setText("🔇");
        } else if (mediaPlayer.getVolume() < 0.5) {
            muteButton.setText("🔉");
        } else {
            muteButton.setText("🔊");
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        contentPane.getChildren().clear();
        
        VBox errorContainer = new VBox(16);
        errorContainer.setAlignment(Pos.CENTER);
        
        Label errorIcon = new Label("❌");
        errorIcon.setStyle("-fx-font-size: 48px;");
        
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);
        
        errorContainer.getChildren().addAll(errorIcon, errorLabel);
        contentPane.getChildren().add(errorContainer);
        
        controlsPane.setVisible(false);
        fileNameLabel.setText("Error");
        fileInfoLabel.setText("");
    }
    
    /**
     * Show unsupported media message
     */
    public void showUnsupportedMedia(String fileName, String reason) {
        contentPane.getChildren().clear();
        
        VBox unsupportedContainer = new VBox(16);
        unsupportedContainer.setAlignment(Pos.CENTER);
        
        Label unsupportedIcon = new Label("📄");
        unsupportedIcon.setStyle("-fx-font-size: 48px;");
        
        Label unsupportedLabel = new Label("Preview not supported");
        unsupportedLabel.getStyleClass().add("unsupported-text");
        
        Label reasonLabel = new Label(reason);
        reasonLabel.getStyleClass().add("reason-text");
        reasonLabel.setWrapText(true);
        
        unsupportedContainer.getChildren().addAll(unsupportedIcon, unsupportedLabel, reasonLabel);
        contentPane.getChildren().add(unsupportedContainer);
        
        controlsPane.setVisible(false);
        fileNameLabel.setText(fileName);
        fileInfoLabel.setText("");
    }
    
    /**
     * Detect media type from file extension
     */
    private MediaType detectMediaType(File file) {
        String fileName = file.getName().toLowerCase();
        
        for (String ext : IMAGE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return MediaType.IMAGE;
            }
        }
        
        for (String ext : VIDEO_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return MediaType.VIDEO;
            }
        }
        
        for (String ext : AUDIO_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return MediaType.AUDIO;
            }
        }
        
        return MediaType.NONE;
    }
    
    /**
     * Format time duration for display
     */
    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes >= 60) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Check if file can be previewed as media
     */
    public static boolean canPreview(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        
        // Check for supported extensions
        for (String ext : IMAGE_EXTENSIONS) {
            if (fileName.endsWith(ext)) return true;
        }
        for (String ext : VIDEO_EXTENSIONS) {
            if (fileName.endsWith(ext)) return true;
        }
        for (String ext : AUDIO_EXTENSIONS) {
            if (fileName.endsWith(ext)) return true;
        }
        
        return false;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        
        if (imageView != null) {
            imageView.setImage(null);
        }
        
        contentPane.getChildren().clear();
        controlsPane.setVisible(false);
    }
    
    /**
     * Clear the preview
     */
    public void clear() {
        cleanup();
        fileNameLabel.setText("");
        fileInfoLabel.setText("");
        currentFile = null;
        currentMediaType = MediaType.NONE;
    }
    
    // Getters
    public File getCurrentFile() {
        return currentFile;
    }
    
    public MediaType getCurrentMediaType() {
        return currentMediaType;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
}