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
import java.io.IOException;
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
    }\n    \n    private void applyStyles() {\n        this.getStyleClass().add(\"image-preview-component\");\n        imageView.getStyleClass().add(\"image-view\");\n        scrollPane.getStyleClass().add(\"image-scroll-pane\");\n        controlsPane.getStyleClass().add(\"image-controls\");\n        statusLabel.getStyleClass().add(\"status-label\");\n        imageInfoLabel.getStyleClass().add(\"image-info-label\");\n        \n        // Button styling\n        zoomInButton.getStyleClass().addAll(\"zoom-button\", \"zoom-in\");\n        zoomOutButton.getStyleClass().addAll(\"zoom-button\", \"zoom-out\");\n        resetZoomButton.getStyleClass().addAll(\"zoom-button\", \"reset-zoom\");\n        fitToWindowButton.getStyleClass().addAll(\"zoom-button\", \"fit-window\");\n        \n        // Set button sizes\n        zoomInButton.setPrefSize(30, 30);\n        zoomOutButton.setPrefSize(30, 30);\n    }\n    \n    /**\n     * Load and display an image file\n     */\n    public void loadImage(File file) {\n        if (file == null || !file.exists() || !file.isFile()) {\n            showError(\"Invalid image file selected\");\n            return;\n        }\n        \n        String extension = getFileExtension(file).toLowerCase();\n        if (!SUPPORTED_FORMATS.contains(extension)) {\n            showError(\"Unsupported image format: \" + extension.toUpperCase());\n            return;\n        }\n        \n        this.currentFile = file;\n        showLoading(true);\n        statusLabel.setText(\"Loading \" + file.getName() + \"...\");\n        \n        Task<Image> loadTask = new Task<Image>() {\n            @Override\n            protected Image call() throws Exception {\n                try (FileInputStream fis = new FileInputStream(file)) {\n                    return new Image(fis);\n                }\n            }\n            \n            @Override\n            protected void succeeded() {\n                Platform.runLater(() -> {\n                    currentImage = getValue();\n                    displayImage();\n                    showLoading(false);\n                    updateImageInfo();\n                });\n            }\n            \n            @Override\n            protected void failed() {\n                Platform.runLater(() -> {\n                    showError(\"Failed to load image: \" + getException().getMessage());\n                    showLoading(false);\n                });\n            }\n        };\n        \n        Thread loadThread = new Thread(loadTask);\n        loadThread.setDaemon(true);\n        loadThread.start();\n    }\n    \n    private void displayImage() {\n        if (currentImage == null) {\n            return;\n        }\n        \n        imageView.setImage(currentImage);\n        resetZoom();\n        \n        statusLabel.setText(currentFile.getName() + \" loaded successfully\");\n    }\n    \n    private void updateImageInfo() {\n        if (currentImage == null || currentFile == null) {\n            imageInfoLabel.setText(\"\");\n            return;\n        }\n        \n        double width = currentImage.getWidth();\n        double height = currentImage.getHeight();\n        long fileSize = currentFile.length();\n        \n        String sizeText = formatFileSize(fileSize);\n        String dimensionsText = String.format(\"%.0f × %.0f\", width, height);\n        String zoomText = String.format(\"%.0f%%\", currentZoom * 100);\n        \n        imageInfoLabel.setText(String.format(\"%s | %s | %s | %s\", \n            dimensionsText, sizeText, getFileExtension(currentFile).toUpperCase(), zoomText));\n    }\n    \n    private void zoomIn() {\n        double newZoom = Math.min(currentZoom * 1.2, MAX_ZOOM);\n        setZoom(newZoom);\n    }\n    \n    private void zoomOut() {\n        double newZoom = Math.max(currentZoom / 1.2, MIN_ZOOM);\n        setZoom(newZoom);\n    }\n    \n    private void resetZoom() {\n        setZoom(1.0);\n    }\n    \n    private void fitToWindow() {\n        if (currentImage == null) {\n            return;\n        }\n        \n        double imageWidth = currentImage.getWidth();\n        double imageHeight = currentImage.getHeight();\n        double paneWidth = scrollPane.getWidth() - 20; // Account for padding\n        double paneHeight = scrollPane.getHeight() - 20;\n        \n        if (paneWidth <= 0 || paneHeight <= 0) {\n            return;\n        }\n        \n        double scaleX = paneWidth / imageWidth;\n        double scaleY = paneHeight / imageHeight;\n        double scale = Math.min(scaleX, scaleY);\n        \n        setZoom(Math.max(scale, MIN_ZOOM));\n    }\n    \n    private void setZoom(double zoom) {\n        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));\n        currentZoom = zoom;\n        \n        if (currentImage != null) {\n            imageView.setFitWidth(currentImage.getWidth() * zoom);\n            imageView.setFitHeight(currentImage.getHeight() * zoom);\n        }\n        \n        // Update slider without triggering listener\n        zoomSlider.setValue(zoom);\n        \n        updateImageInfo();\n    }\n    \n    private void showLoading(boolean show) {\n        loadingIndicator.setVisible(show);\n        controlsPane.setDisable(show);\n    }\n    \n    private void showError(String message) {\n        imageView.setImage(null);\n        currentImage = null;\n        currentFile = null;\n        \n        statusLabel.setText(\"Error: \" + message);\n        imageInfoLabel.setText(\"\");\n        \n        // Show error in image area\n        VBox errorBox = new VBox(10);\n        errorBox.setAlignment(Pos.CENTER);\n        errorBox.setPadding(new Insets(50));\n        \n        Text errorIcon = new Text(\"⚠\");\n        errorIcon.setStyle(\"-fx-font-size: 48px; -fx-fill: #ff6b6b;\");\n        \n        Label errorLabel = new Label(message);\n        errorLabel.setStyle(\"-fx-font-size: 14px; -fx-text-fill: #666;\");\n        \n        errorBox.getChildren().addAll(errorIcon, errorLabel);\n        scrollPane.setContent(errorBox);\n    }\n    \n    private String getFileExtension(File file) {\n        String name = file.getName();\n        int lastDot = name.lastIndexOf('.');\n        return lastDot > 0 ? name.substring(lastDot + 1) : \"\";\n    }\n    \n    private String formatFileSize(long bytes) {\n        if (bytes < 1024) return bytes + \" B\";\n        if (bytes < 1024 * 1024) return new DecimalFormat(\"#.#\").format(bytes / 1024.0) + \" KB\";\n        if (bytes < 1024 * 1024 * 1024) return new DecimalFormat(\"#.#\").format(bytes / (1024.0 * 1024.0)) + \" MB\";\n        return new DecimalFormat(\"#.#\").format(bytes / (1024.0 * 1024.0 * 1024.0)) + \" GB\";\n    }\n    \n    /**\n     * Check if file format is supported\n     */\n    public static boolean isSupported(File file) {\n        if (file == null || !file.isFile()) {\n            return false;\n        }\n        \n        String extension = \"\";\n        String name = file.getName();\n        int lastDot = name.lastIndexOf('.');\n        if (lastDot > 0) {\n            extension = name.substring(lastDot + 1).toLowerCase();\n        }\n        \n        return SUPPORTED_FORMATS.contains(extension);\n    }\n    \n    /**\n     * Get current image\n     */\n    public Image getCurrentImage() {\n        return currentImage;\n    }\n    \n    /**\n     * Get current file\n     */\n    public File getCurrentFile() {\n        return currentFile;\n    }\n    \n    /**\n     * Get current zoom level\n     */\n    public double getCurrentZoom() {\n        return currentZoom;\n    }\n    \n    /**\n     * Clear the preview\n     */\n    public void clear() {\n        imageView.setImage(null);\n        currentImage = null;\n        currentFile = null;\n        currentZoom = 1.0;\n        statusLabel.setText(\"No image selected\");\n        imageInfoLabel.setText(\"\");\n        \n        // Reset scroll pane content\n        scrollPane.setContent(imageView);\n    }\n}"