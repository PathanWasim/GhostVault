package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Image preview component for various image formats
 */
public class ImagePreviewComponent extends VBox {
    
    private ImageView imageView;
    private ScrollPane scrollPane;
    private Label statusLabel;
    private Label imageInfoLabel;
    private ProgressIndicator loadingIndicator;
    private HBox controlsPane;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button resetZoomButton;
    private Button fitToWindowButton;
    private Slider zoomSlider;
    
    private File currentFile;
    private Image currentImage;
    private double currentZoom = 1.0;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;
    
    // Supported image formats
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "tiff", "tif", "webp"
    );
    
    public ImagePreviewComponent() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        applyStyles();
    }
    
    private void initializeComponents() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        scrollPane = new ScrollPane();
        scrollPane.setContent(imageView);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        
        statusLabel = new Label("No image selected");
        imageInfoLabel = new Label("");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(50, 50);
        
        // Zoom controls
        zoomInButton = new Button("+");
        zoomOutButton = new Button("-");
        resetZoomButton = new Button("100%");
        fitToWindowButton = new Button("Fit");
        
        zoomSlider = new Slider(MIN_ZOOM, MAX_ZOOM, 1.0);
        zoomSlider.setShowTickLabels(false);
        zoomSlider.setShowTickMarks(false);
        zoomSlider.setPrefWidth(150);
        
        controlsPane = new HBox(10);
        controlsPane.setAlignment(Pos.CENTER);
        controlsPane.setPadding(new Insets(5));
    }
    
    private void setupLayout() {
        // Controls pane
        controlsPane.getChildren().addAll(
            zoomOutButton, zoomSlider, zoomInButton,
            new Separator(), resetZoomButton, fitToWindowButton
        );
        
        // Status bar
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getChildren().addAll(statusLabel, loadingIndicator);
        
        // Info bar
        HBox infoBar = new HBox();
        infoBar.setPadding(new Insets(5));
        infoBar.setAlignment(Pos.CENTER);
        infoBar.getChildren().add(imageInfoLabel);
        
        this.getChildren().addAll(controlsPane, scrollPane, statusBar, infoBar);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        resetZoomButton.setOnAction(e -> resetZoom());
        fitToWindowButton.setOnAction(e -> fitToWindow());
        
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!zoomSlider.isValueChanging()) {
                setZoom(newVal.doubleValue());
            }
        });
        
        // Mouse wheel zoom
        scrollPane.setOnScroll(e -> {
            if (e.isControlDown()) {
                e.consume();
                double deltaY = e.getDeltaY();
                if (deltaY > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("image-preview-component");
        imageView.getStyleClass().add("image-view");
        scrollPane.getStyleClass().add("image-scroll-pane");
        controlsPane.getStyleClass().add("image-controls");
        statusLabel.getStyleClass().add("status-label");
        imageInfoLabel.getStyleClass().add("image-info-label");
        
        // Button styling
        zoomInButton.getStyleClass().addAll("zoom-button", "zoom-in");
        zoomOutButton.getStyleClass().addAll("zoom-button", "zoom-out");
        resetZoomButton.getStyleClass().addAll("zoom-button", "reset-zoom");
        fitToWindowButton.getStyleClass().addAll("zoom-button", "fit-window");
        
        // Set button sizes
        zoomInButton.setPrefSize(30, 30);
        zoomOutButton.setPrefSize(30, 30);
    }
    
    /**
     * Load and display an image file
     */
    public void loadImage(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            showError("Invalid image file selected");
            return;
        }
        
        String extension = getFileExtension(file).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            showUnsupportedFormatError(extension);
            return;
        }
        
        // Check file size (limit to 50MB for images)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.length() > maxSize) {
            showError("Image file too large (max 50MB): " + formatFileSize(file.length()));
            return;
        }
        
        this.currentFile = file;
        showLoading(true);
        statusLabel.setText("Loading " + file.getName() + "...");
        
        Task<Image> loadTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Image image = new Image(fis);
                    
                    // Check if image loaded successfully
                    if (image.isError()) {
                        throw new Exception("Image format not supported or file corrupted");
                    }
                    
                    // Check image dimensions (prevent extremely large images)
                    if (image.getWidth() > 10000 || image.getHeight() > 10000) {
                        throw new Exception("Image dimensions too large (max 10000x10000)");
                    }
                    
                    return image;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    currentImage = getValue();
                    if (currentImage != null && !currentImage.isError()) {
                        displayImage();
                        showLoading(false);
                        updateImageInfo();
                    } else {
                        showError("Failed to load image: Image format not supported");
                        showLoading(false);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable exception = getException();
                    String errorMessage = "Failed to load image";
                    
                    if (exception != null) {
                        String exceptionMessage = exception.getMessage();
                        if (exceptionMessage != null) {
                            if (exceptionMessage.contains("format not supported")) {
                                errorMessage = "Unsupported image format or corrupted file";
                            } else if (exceptionMessage.contains("dimensions too large")) {
                                errorMessage = "Image too large to display";
                            } else if (exceptionMessage.contains("OutOfMemoryError")) {
                                errorMessage = "Image too large for available memory";
                            } else {
                                errorMessage = "Failed to load image: " + exceptionMessage;
                            }
                        }
                    }
                    
                    showError(errorMessage);
                    showLoading(false);
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void displayImage() {
        if (currentImage == null) {
            return;
        }
        
        imageView.setImage(currentImage);
        resetZoom();
        
        statusLabel.setText(currentFile.getName() + " loaded successfully");
    }
    
    private void updateImageInfo() {
        if (currentImage == null || currentFile == null) {
            imageInfoLabel.setText("");
            return;
        }
        
        double width = currentImage.getWidth();
        double height = currentImage.getHeight();
        long fileSize = currentFile.length();
        
        String sizeText = formatFileSize(fileSize);
        String dimensionsText = String.format("%.0f × %.0f", width, height);
        String zoomText = String.format("%.0f%%", currentZoom * 100);
        
        imageInfoLabel.setText(String.format("%s | %s | %s | %s", 
            dimensionsText, sizeText, getFileExtension(currentFile).toUpperCase(), zoomText));
    }
    
    private void zoomIn() {
        double newZoom = Math.min(currentZoom * 1.2, MAX_ZOOM);
        setZoom(newZoom);
    }
    
    private void zoomOut() {
        double newZoom = Math.max(currentZoom / 1.2, MIN_ZOOM);
        setZoom(newZoom);
    }
    
    private void resetZoom() {
        setZoom(1.0);
    }
    
    private void fitToWindow() {
        if (currentImage == null) {
            return;
        }
        
        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();
        double paneWidth = scrollPane.getWidth() - 20; // Account for padding
        double paneHeight = scrollPane.getHeight() - 20;
        
        if (paneWidth <= 0 || paneHeight <= 0) {
            return;
        }
        
        double scaleX = paneWidth / imageWidth;
        double scaleY = paneHeight / imageHeight;
        double scale = Math.min(scaleX, scaleY);
        
        setZoom(Math.max(scale, MIN_ZOOM));
    }
    
    private void setZoom(double zoom) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        currentZoom = zoom;
        
        if (currentImage != null) {
            imageView.setFitWidth(currentImage.getWidth() * zoom);
            imageView.setFitHeight(currentImage.getHeight() * zoom);
        }
        
        // Update slider without triggering listener
        zoomSlider.setValue(zoom);
        
        updateImageInfo();
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        controlsPane.setDisable(show);
    }
    
    private void showError(String message) {
        imageView.setImage(null);
        currentImage = null;
        currentFile = null;
        
        statusLabel.setText("Error: " + message);
        imageInfoLabel.setText("");
        
        // Show error in image area
        VBox errorBox = new VBox(10);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));
        
        Text errorIcon = new Text("⚠");
        errorIcon.setStyle("-fx-font-size: 48px; -fx-fill: #ff6b6b;");
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);
        
        errorBox.getChildren().addAll(errorIcon, errorLabel);
        scrollPane.setContent(errorBox);
    }
    
    private void showUnsupportedFormatError(String extension) {
        imageView.setImage(null);
        currentImage = null;
        currentFile = null;
        
        statusLabel.setText("Unsupported format: " + extension.toUpperCase());
        imageInfoLabel.setText("");
        
        // Show unsupported format message
        VBox errorBox = new VBox(15);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));
        
        Text errorIcon = new Text("🚫");
        errorIcon.setStyle("-fx-font-size: 48px;");
        
        Label titleLabel = new Label("Unsupported Image Format");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label messageLabel = new Label("Cannot preview ." + extension.toUpperCase() + " files");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Label supportedLabel = new Label("Supported formats: " + String.join(", ", SUPPORTED_FORMATS));
        supportedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        supportedLabel.setWrapText(true);
        supportedLabel.setMaxWidth(400);
        
        errorBox.getChildren().addAll(errorIcon, titleLabel, messageLabel, supportedLabel);
        scrollPane.setContent(errorBox);
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
     * Get current image
     */
    public Image getCurrentImage() {
        return currentImage;
    }
    
    /**
     * Get current file
     */
    public File getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Get current zoom level
     */
    public double getCurrentZoom() {
        return currentZoom;
    }
    
    /**
     * Clear the preview
     */
    public void clear() {
        // Stop any ongoing loading tasks
        stopCurrentTask();
        
        imageView.setImage(null);
        currentImage = null;
        currentFile = null;
        currentZoom = 1.0;
        statusLabel.setText("No image selected");
        imageInfoLabel.setText("");
        
        // Reset scroll pane content
        scrollPane.setContent(imageView);
        
        // Hide loading indicator
        showLoading(false);
    }
    
    /**
     * Stop current loading task if any
     */
    private void stopCurrentTask() {
        // This would be implemented if we kept references to running tasks
        // For now, we rely on daemon threads being cleaned up automatically
    }
    
    /**
     * Cleanup resources when component is being disposed
     */
    public void dispose() {
        clear();
        
        // Clear any cached images to free memory
        if (currentImage != null) {
            currentImage = null;
        }
        
        // Remove event handlers to prevent memory leaks
        zoomSlider.valueProperty().removeListener((obs, oldVal, newVal) -> {
            if (!zoomSlider.isValueChanging()) {
                setZoom(newVal.doubleValue());
            }
        });
        
        scrollPane.setOnScroll(null);
    }
}