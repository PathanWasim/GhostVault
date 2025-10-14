package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Professional media preview component for images, audio, and video
 */
public class MediaPreviewPane extends VBox {
    
    // Supported file formats
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".webp"
    );
    
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
        ".mp3", ".wav", ".aac", ".m4a", ".ogg", ".flac", ".wma"
    );
    
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
        ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v"
    );
    
    private final Label headerLabel;
    private final ScrollPane scrollPane;
    private final VBox contentBox;
    private final ProgressIndicator loadingIndicator;
    private final Label errorLabel;
    
    // Media components
    private ImageView imageView;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private VBox mediaControls;
    
    private String currentFileName;
    private String currentFileType;
    
    public MediaPreviewPane() {
        super(5);
        setPadding(new Insets(10));
        getStyleClass().add("professional-panel");
        
        // Header with file info
        headerLabel = new Label("No media selected");
        headerLabel.getStyleClass().addAll("header-subtitle");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);
        
        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        
        // Content area
        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.setAlignment(Pos.CENTER);
        
        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("media-scroll-pane");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(headerLabel, scrollPane);
        
        // Apply professional styling
        setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1px; -fx-border-radius: 6px;");
    }
    
    /**
     * Preview an image file
     */
    public void previewImage(String fileName, byte[] imageData) {
        this.currentFileName = fileName;
        this.currentFileType = "image";
        
        Platform.runLater(() -> {
            headerLabel.setText("🖼️ " + fileName);
            showLoading(true);
            contentBox.getChildren().clear();
            stopCurrentMedia();
        });
        
        Task<Void> imageTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                    Image image = new Image(inputStream);
                    
                    Platform.runLater(() -> {
                        if (image.isError()) {
                            showError("Failed to load image: Invalid format or corrupted file");
                            return;
                        }
                        
                        // Create image view
                        imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setCache(true);
                        
                        // Fit image to available space
                        double maxWidth = scrollPane.getWidth() - 40;
                        double maxHeight = scrollPane.getHeight() - 100;
                        
                        if (maxWidth > 0 && maxHeight > 0) {
                            imageView.setFitWidth(Math.min(image.getWidth(), maxWidth));
                            imageView.setFitHeight(Math.min(image.getHeight(), maxHeight));
                        } else {
                            imageView.setFitWidth(Math.min(image.getWidth(), 800));
                            imageView.setFitHeight(Math.min(image.getHeight(), 600));
                        }
                        
                        // Create metadata panel
                        VBox metadataPanel = createImageMetadataPanel(image, imageData.length);
                        
                        contentBox.getChildren().addAll(imageView, metadataPanel);
                        showLoading(false);
                        
                        // Update header with dimensions
                        headerLabel.setText(String.format("🖼️ %s (%dx%d)", 
                            fileName, (int)image.getWidth(), (int)image.getHeight()));
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error loading image: " + e.getMessage()));
                }
                
                return null;
            }
        };
        
        Thread imageThread = new Thread(imageTask);
        imageThread.setDaemon(true);
        imageThread.start();
    }
    
    /**
     * Preview an audio file
     */
    public void previewAudio(String fileName, byte[] audioData) {
        this.currentFileName = fileName;
        this.currentFileType = "audio";
        
        Platform.runLater(() -> {
            headerLabel.setText("🎵 " + fileName);
            showLoading(true);
            contentBox.getChildren().clear();
            stopCurrentMedia();
        });
        
        try {
            // Create temporary file for JavaFX Media
            java.io.File tempFile = java.io.File.createTempFile("ghostvault_audio", getFileExtension(fileName));
            tempFile.deleteOnExit();
            
            java.nio.file.Files.write(tempFile.toPath(), audioData);
            
            Media media = new Media(tempFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            Platform.runLater(() -> {
                // Create audio player UI
                VBox audioPlayerBox = createAudioPlayerUI();
                
                // Create metadata panel
                VBox metadataPanel = createAudioMetadataPanel(audioData.length);
                
                contentBox.getChildren().addAll(audioPlayerBox, metadataPanel);
                showLoading(false);
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> showError("Error loading audio: " + e.getMessage()));
        }
    }
    
    /**
     * Preview a video file
     */
    public void previewVideo(String fileName, byte[] videoData) {
        this.currentFileName = fileName;
        this.currentFileType = "video";
        
        Platform.runLater(() -> {
            headerLabel.setText("🎬 " + fileName);
            showLoading(true);
            contentBox.getChildren().clear();
            stopCurrentMedia();
        });
        
        try {
            // Create temporary file for JavaFX Media
            java.io.File tempFile = java.io.File.createTempFile("ghostvault_video", getFileExtension(fileName));
            tempFile.deleteOnExit();
            
            java.nio.file.Files.write(tempFile.toPath(), videoData);
            
            Media media = new Media(tempFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            
            Platform.runLater(() -> {
                // Create video player UI
                mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(600);
                mediaView.setFitHeight(400);
                mediaView.setPreserveRatio(true);
                
                VBox videoPlayerBox = createVideoPlayerUI();
                
                // Create metadata panel
                VBox metadataPanel = createVideoMetadataPanel(videoData.length);
                
                contentBox.getChildren().addAll(videoPlayerBox, metadataPanel);
                showLoading(false);
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> showError("Error loading video: " + e.getMessage()));
        }
    }
    
    /**
     * Create image metadata panel
     */
    private VBox createImageMetadataPanel(Image image, int fileSize) {
        VBox metadataBox = new VBox(5);
        metadataBox.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        metadataBox.setMaxWidth(400);
        
        Label titleLabel = new Label("Image Information");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        Label dimensionsLabel = new Label(String.format("Dimensions: %dx%d pixels", 
            (int)image.getWidth(), (int)image.getHeight()));
        dimensionsLabel.setStyle("-fx-text-fill: #cccccc;");
        
        Label sizeLabel = new Label("File Size: " + formatFileSize(fileSize));
        sizeLabel.setStyle("-fx-text-fill: #cccccc;");
        
        Label formatLabel = new Label("Format: " + getFileExtension(currentFileName).toUpperCase().substring(1));
        formatLabel.setStyle("-fx-text-fill: #cccccc;");
        
        metadataBox.getChildren().addAll(titleLabel, dimensionsLabel, sizeLabel, formatLabel);
        
        return metadataBox;
    }
    
    /**
     * Create audio player UI
     */
    private VBox createAudioPlayerUI() {
        VBox playerBox = new VBox(10);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 20px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        playerBox.setMaxWidth(400);
        
        // Audio icon
        Label audioIcon = new Label("🎵");
        audioIcon.setStyle("-fx-font-size: 48px;");
        
        // File name
        Label fileNameLabel = new Label(currentFileName);
        fileNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");
        
        // Media controls
        mediaControls = createMediaControls();
        
        playerBox.getChildren().addAll(audioIcon, fileNameLabel, mediaControls);
        
        return playerBox;
    }
    
    /**
     * Create video player UI
     */
    private VBox createVideoPlayerUI() {
        VBox playerBox = new VBox(10);
        playerBox.setAlignment(Pos.CENTER);
        
        // Video view
        VBox videoContainer = new VBox();
        videoContainer.setAlignment(Pos.CENTER);
        videoContainer.setStyle("-fx-background-color: black; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        videoContainer.getChildren().add(mediaView);
        
        // Media controls
        mediaControls = createMediaControls();
        
        playerBox.getChildren().addAll(videoContainer, mediaControls);
        
        return playerBox;
    }
    
    /**
     * Create media controls (play, pause, progress, volume)
     */
    private VBox createMediaControls() {
        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.getStyleClass().add("media-progress");
        
        // Control buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button playButton = new Button("▶️");
        playButton.getStyleClass().addAll("professional-button", "button-primary");
        playButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playButton.setText("▶️");
                } else {
                    mediaPlayer.play();
                    playButton.setText("⏸️");
                }
            }
        });
        
        Button stopButton = new Button("⏹️");
        stopButton.getStyleClass().addAll("professional-button");
        stopButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                playButton.setText("▶️");
                progressBar.setProgress(0);
            }
        });
        
        // Volume control
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
        
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-text-fill: #cccccc;");
        
        HBox volumeBox = new HBox(5, volumeLabel, volumeSlider);
        volumeBox.setAlignment(Pos.CENTER);
        
        buttonBox.getChildren().addAll(playButton, stopButton, volumeBox);
        
        // Update progress bar
        if (mediaPlayer != null) {
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (mediaPlayer.getTotalDuration() != null) {
                    double progress = newTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                    progressBar.setProgress(progress);
                }
            });
        }
        
        controlsBox.getChildren().addAll(progressBar, buttonBox);
        
        return controlsBox;
    }
    
    /**
     * Create audio metadata panel
     */
    private VBox createAudioMetadataPanel(int fileSize) {
        VBox metadataBox = new VBox(5);
        metadataBox.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        metadataBox.setMaxWidth(400);
        
        Label titleLabel = new Label("Audio Information");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        Label sizeLabel = new Label("File Size: " + formatFileSize(fileSize));
        sizeLabel.setStyle("-fx-text-fill: #cccccc;");
        
        Label formatLabel = new Label("Format: " + getFileExtension(currentFileName).toUpperCase().substring(1));
        formatLabel.setStyle("-fx-text-fill: #cccccc;");
        
        metadataBox.getChildren().addAll(titleLabel, sizeLabel, formatLabel);
        
        return metadataBox;
    }
    
    /**
     * Create video metadata panel
     */
    private VBox createVideoMetadataPanel(int fileSize) {
        VBox metadataBox = new VBox(5);
        metadataBox.setStyle("-fx-background-color: #3a3a3a; -fx-padding: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        metadataBox.setMaxWidth(400);
        
        Label titleLabel = new Label("Video Information");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        Label sizeLabel = new Label("File Size: " + formatFileSize(fileSize));
        sizeLabel.setStyle("-fx-text-fill: #cccccc;");
        
        Label formatLabel = new Label("Format: " + getFileExtension(currentFileName).toUpperCase().substring(1));
        formatLabel.setStyle("-fx-text-fill: #cccccc;");
        
        metadataBox.getChildren().addAll(titleLabel, sizeLabel, formatLabel);
        
        return metadataBox;
    }
    
    /**
     * Show unsupported media message
     */
    public void showUnsupportedMedia(String fileName, String reason) {
        Platform.runLater(() -> {
            headerLabel.setText("📄 " + fileName);
            contentBox.getChildren().clear();
            stopCurrentMedia();
            
            VBox messageBox = new VBox(10);
            messageBox.setStyle("-fx-alignment: center; -fx-padding: 50px;");
            
            Label iconLabel = new Label("⚠️");
            iconLabel.setStyle("-fx-font-size: 48px;");
            
            Label messageLabel = new Label("Cannot preview this media file");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cccccc;");
            
            Label reasonLabel = new Label(reason);
            reasonLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
            reasonLabel.setWrapText(true);
            
            messageBox.getChildren().addAll(iconLabel, messageLabel, reasonLabel);
            contentBox.getChildren().add(messageBox);
            
            showLoading(false);
        });
    }
    
    /**
     * Stop current media playback
     */
    private void stopCurrentMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (mediaView != null) {
            mediaView = null;
        }
        if (imageView != null) {
            imageView = null;
        }
    }
    
    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        if (show) {
            if (!getChildren().contains(loadingIndicator)) {
                getChildren().add(1, loadingIndicator);
            }
        } else {
            getChildren().remove(loadingIndicator);
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            showLoading(false);
            
            if (!getChildren().contains(errorLabel)) {
                getChildren().add(errorLabel);
            }
        });
    }
    
    /**
     * Clear the preview
     */
    public void clear() {
        Platform.runLater(() -> {
            headerLabel.setText("No media selected");
            contentBox.getChildren().clear();
            stopCurrentMedia();
            showLoading(false);
            errorLabel.setVisible(false);
        });
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot).toLowerCase();
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
     * Check if file can be previewed as image
     */
    public static boolean canPreviewAsImage(String fileName) {
        if (fileName == null) return false;
        String extension = getExtension(fileName);
        return IMAGE_EXTENSIONS.contains(extension);
    }
    
    /**
     * Check if file can be previewed as audio
     */
    public static boolean canPreviewAsAudio(String fileName) {
        if (fileName == null) return false;
        String extension = getExtension(fileName);
        return AUDIO_EXTENSIONS.contains(extension);
    }
    
    /**
     * Check if file can be previewed as video
     */
    public static boolean canPreviewAsVideo(String fileName) {
        if (fileName == null) return false;
        String extension = getExtension(fileName);
        return VIDEO_EXTENSIONS.contains(extension);
    }
    
    /**
     * Get file extension (static helper)
     */
    private static String getExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot).toLowerCase();
    }
}