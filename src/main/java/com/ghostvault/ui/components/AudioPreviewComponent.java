package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Audio preview component with playback controls
 */
public class AudioPreviewComponent extends VBox {
    
    private MediaPlayer mediaPlayer;
    private File currentFile;
    private Button playPauseButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Label statusLabel;
    private Label audioInfoLabel;
    private ProgressIndicator loadingIndicator;
    private boolean isPlaying = false;
    private boolean isDragging = false;
    
    // Supported audio formats
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "mp3", "wav", "aac", "m4a", "flac", "ogg"
    );
    
    public AudioPreviewComponent() {
        super();
        initializeComponents();
        setupLayout();
        applyStyles();
    }
    
    private void initializeComponents() {
        playPauseButton = new Button("▶");
        playPauseButton.setPrefSize(50, 50);
        playPauseButton.setDisable(true);
        
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setDisable(true);
        
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);
        
        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-font-family: monospace;");
        
        statusLabel = new Label("No audio file selected");
        audioInfoLabel = new Label("");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(30, 30);
    }
    
    private void setupLayout() {
        // Controls row
        HBox controlsBox = new HBox(15);
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
        infoBox.getChildren().add(audioInfoLabel);
        
        this.getChildren().addAll(controlsBox, statusBox, infoBox);
        
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
        this.getStyleClass().add("audio-preview-component");
        playPauseButton.getStyleClass().add("play-pause-button");
        progressSlider.getStyleClass().add("progress-slider");
        volumeSlider.getStyleClass().add("volume-slider");
        statusLabel.getStyleClass().add("status-label");
        audioInfoLabel.getStyleClass().add("audio-info-label");
        
        this.setStyle(
            "-fx-background-color: #f8f8f8;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
    }
    
    /**
     * Load and prepare audio file for playback
     */
    public void loadAudio(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            showError("Invalid audio file selected");
            return;
        }
        
        String extension = getFileExtension(file).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            showUnsupportedFormatError(extension);
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
            
            mediaPlayer.setOnReady(() -> Platform.runLater(() -> {
                setupMediaPlayer();
                showLoading(false);
                updateAudioInfo();
                statusLabel.setText("Ready to play: " + file.getName());
            }));
            
            mediaPlayer.setOnError(() -> Platform.runLater(() -> {
                showError("Failed to load audio: " + mediaPlayer.getError().getMessage());
                showLoading(false);
            }));
            
        } catch (Exception e) {
            showError("Failed to load audio: " + e.getMessage());
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
        
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void updateAudioInfo() {
        if (currentFile == null) {
            audioInfoLabel.setText("");
            return;
        }
        
        long fileSize = currentFile.length();
        String sizeText = formatFileSize(fileSize);
        String extension = getFileExtension(currentFile).toUpperCase();
        
        String durationText = "";
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            durationText = formatDuration(mediaPlayer.getTotalDuration());
        }
        
        audioInfoLabel.setText(String.format("%s | %s | %s", 
            extension, sizeText, durationText));
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        playPauseButton.setDisable(show);
    }
    
    private void showError(String message) {
        stopPlayback();
        currentFile = null;
        statusLabel.setText("Error: " + message);
        audioInfoLabel.setText("");
    }
    
    private void showUnsupportedFormatError(String extension) {
        stopPlayback();
        currentFile = null;
        statusLabel.setText("Unsupported format: " + extension.toUpperCase());
        audioInfoLabel.setText("Supported formats: " + String.join(", ", SUPPORTED_FORMATS));
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
     * Check if audio is currently playing
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
        statusLabel.setText("No audio file selected");
        audioInfoLabel.setText("");
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