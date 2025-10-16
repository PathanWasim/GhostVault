package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Video preview component with playback controls
 */
public class VideoPreviewComponent extends VBox {
    
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private File currentFile;
    private Button playPauseButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Label statusLabel;
    private Label videoInfoLabel;
    private ProgressIndicator loadingIndicator;
    private boolean isPlaying = false;
    private boolean isDragging = false;
    
    // Supported video formats
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv"
    );
    
    public VideoPreviewComponent() {
        super();
        initializeComponents();
        setupLayout();
        applyStyles();
    }
    
    private void initializeComponents() {
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(600);
        mediaView.setFitHeight(400);
        
        playPauseButton = new Button("▶");
        playPauseButton.setPrefSize(40, 40);
        playPauseButton.setDisable(true);
        
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setDisable(true);
        
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);
        
        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-font-family: monospace;");
        
        statusLabel = new Label("No video file selected");
        videoInfoLabel = new Label("");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(30, 30);
    }
    
    private void setupLayout() {
        // Video display area
        StackPane videoPane = new StackPane();
        videoPane.getChildren().add(mediaView);
        videoPane.setStyle(
            "-fx-background-color: black;" +
            "-fx-border-color: #333;" +
            "-fx-border-width: 1px;"
        );
        videoPane.setPrefHeight(400);
        
        // Controls row
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10));
        
        Label volumeIcon = new Label("🔊");
        
        controlsBox.getChildren().addAll(
            playPauseButton,
            progressSlider,
            timeLabel,
            volumeIcon,
            volumeSlider
        );
        
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        
        // Status row
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(5, 10, 5, 10));
        statusBox.getChildren().addAll(statusLabel, loadingIndicator);
        
        // Info row
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(5));
        infoBox.getChildren().add(videoInfoLabel);
        
        this.getChildren().addAll(videoPane, controlsBox, statusBox, infoBox);
        VBox.setVgrow(videoPane, Priority.ALWAYS);
        
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        playPauseButton.setOnAction(e -> togglePlayPause());
        
        progressSlider.setOnMousePressed(e -> isDragging = true);
        progressSlider.setOnMouseReleased(e -> {
            isDragging = false;
            if (mediaPlayer != null) {
                Duration seekTime = Duration.seconds(progressSlider.getValue());
                mediaPlayer.seek(seekTime);
            }
        });
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("video-preview-component");
        playPauseButton.getStyleClass().add("play-pause-button");
        progressSlider.getStyleClass().add("progress-slider");
        volumeSlider.getStyleClass().add("volume-slider");
        statusLabel.getStyleClass().add("status-label");
        videoInfoLabel.getStyleClass().add("video-info-label");
        
        this.setStyle(
            "-fx-background-color: #f8f8f8;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
    }
    
    /**
     * Load and prepare video file for playback
     */
    public void loadVideo(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            showError("Invalid video file selected");
            return;
        }
        
        String extension = getFileExtension(file).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            showUnsupportedFormatError(extension);
            return;
        }
        
        // Check file size (limit to 500MB for videos)
        long maxSize = 500 * 1024 * 1024; // 500MB
        if (file.length() > maxSize) {
            showError("Video file too large (max 500MB): " + formatFileSize(file.length()));
            return;
        }
        
        // Stop current playback if any
        stopPlayback();
        
        this.currentFile = file;
        showLoading(true);
        statusLabel.setText("Loading " + file.getName() + "...");
        
        try {
            String fileUri = file.toURI().toString();
            Media media = new Media(fileUri);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            mediaPlayer.setOnReady(() -> Platform.runLater(() -> {
                setupMediaPlayer();
                showLoading(false);
                updateVideoInfo();
                statusLabel.setText("Ready to play: " + file.getName());
            }));
            
            mediaPlayer.setOnError(() -> Platform.runLater(() -> {
                showError("Failed to load video: " + mediaPlayer.getError().getMessage());
                showLoading(false);
            }));
            
        } catch (Exception e) {
            showError("Failed to load video: " + e.getMessage());
            showLoading(false);
        }
    }
    
    private void setupMediaPlayer() {
        if (mediaPlayer == null) return;
        
        Duration totalDuration = mediaPlayer.getTotalDuration();
        progressSlider.setMax(totalDuration.toSeconds());
        progressSlider.setDisable(false);
        playPauseButton.setDisable(false);
        
        // Update progress
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!isDragging) {
                progressSlider.setValue(newTime.toSeconds());
            }
            updateTimeLabel(newTime, totalDuration);
        });
        
        // Handle end of media
        mediaPlayer.setOnEndOfMedia(() -> {
            isPlaying = false;
            playPauseButton.setText("▶");
            progressSlider.setValue(0);
            mediaPlayer.seek(Duration.ZERO);
        });
        
        // Set initial volume
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
    }
    
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
    
    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        
        mediaView.setMediaPlayer(null);
        isPlaying = false;
        playPauseButton.setText("▶");
        playPauseButton.setDisable(true);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        timeLabel.setText("00:00 / 00:00");
    }
    
    private void updateTimeLabel(Duration currentTime, Duration totalDuration) {
        String current = formatDuration(currentTime);
        String total = formatDuration(totalDuration);
        timeLabel.setText(current + " / " + total);
    }
    
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        
        int hours = (int) duration.toHours();
        int minutes = (int) (duration.toMinutes() % 60);
        int seconds = (int) (duration.toSeconds() % 60);
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    private void updateVideoInfo() {
        if (currentFile == null) {
            videoInfoLabel.setText("");
            return;
        }
        
        long fileSize = currentFile.length();
        String sizeText = formatFileSize(fileSize);
        String extension = getFileExtension(currentFile).toUpperCase();
        
        String durationText = "";
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            durationText = formatDuration(mediaPlayer.getTotalDuration());
        }
        
        // Get video dimensions if available
        String dimensionsText = "";
        if (mediaPlayer != null && mediaPlayer.getMedia() != null) {
            int width = (int) mediaPlayer.getMedia().getWidth();
            int height = (int) mediaPlayer.getMedia().getHeight();
            if (width > 0 && height > 0) {
                dimensionsText = width + "×" + height;
            }
        }
        
        StringBuilder info = new StringBuilder();
        info.append(extension).append(" | ").append(sizeText);
        if (!durationText.isEmpty()) {
            info.append(" | ").append(durationText);
        }
        if (!dimensionsText.isEmpty()) {
            info.append(" | ").append(dimensionsText);
        }
        
        videoInfoLabel.setText(info.toString());
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        playPauseButton.setDisable(show);
    }
    
    private void showError(String message) {
        stopPlayback();
        currentFile = null;
        statusLabel.setText("Error: " + message);
        videoInfoLabel.setText("");
    }
    
    private void showUnsupportedFormatError(String extension) {
        stopPlayback();
        currentFile = null;
        statusLabel.setText("Unsupported format: " + extension.toUpperCase());
        videoInfoLabel.setText("Supported formats: " + String.join(", ", SUPPORTED_FORMATS));
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return new DecimalFormat("#.#").format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0)) + " MB";
        return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }
    
    /**
     * Check if file format is supported
     */
    public static boolean isSupported(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        
        String extension = "";
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            extension = name.substring(lastDot + 1).toLowerCase();
        }
        
        return SUPPORTED_FORMATS.contains(extension);
    }
    
    /**
     * Get current file
     */
    public File getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Check if video is currently playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Clear the preview and stop playback
     */
    public void clear() {
        stopPlayback();
        currentFile = null;
        statusLabel.setText("No video file selected");
        videoInfoLabel.setText("");
        showLoading(false);
    }
    
    /**
     * Dispose of resources when component is being destroyed
     */
    public void dispose() {
        clear();
        
        // Remove listeners to prevent memory leaks
        if (volumeSlider != null) {
            volumeSlider.valueProperty().removeListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
                }
            });
        }
    }
}