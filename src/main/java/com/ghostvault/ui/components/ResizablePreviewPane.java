package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * Resizable preview pane that can contain code or media preview components
 */
public class ResizablePreviewPane extends VBox {
    
    private static final double MIN_PREVIEW_WIDTH = 300;
    private static final double MIN_PREVIEW_HEIGHT = 200;
    private static final double DEFAULT_PREVIEW_WIDTH = 400;
    
    // Components
    private final HBox headerBox;
    private final Label titleLabel;
    private final Button closeButton;
    private final Button resizeButton;
    private final StackPane contentPane;
    private final Region resizeHandle;
    
    // Preview components
    private CodePreviewPane codePreview;
    private MediaPreviewPane mediaPreview;
    
    // State
    private boolean isVisible = false;
    private double lastWidth = DEFAULT_PREVIEW_WIDTH;
    private PreviewType currentPreviewType = PreviewType.NONE;
    
    // Resize handling
    private double dragStartX;
    private double dragStartWidth;
    
    public enum PreviewType {
        NONE, CODE, IMAGE, AUDIO, VIDEO
    }
    
    public ResizablePreviewPane() {
        super();
        setPrefWidth(DEFAULT_PREVIEW_WIDTH);
        setMinWidth(MIN_PREVIEW_WIDTH);
        setMaxWidth(800);
        
        // Create header
        headerBox = createHeader();
        
        // Create content pane
        contentPane = new StackPane();
        contentPane.setMinHeight(MIN_PREVIEW_HEIGHT);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        
        // Create resize handle
        resizeHandle = createResizeHandle();
        
        getChildren().addAll(headerBox, contentPane, resizeHandle);
        
        // Initialize preview components
        initializePreviewComponents();
        
        // Apply styling
        getStyleClass().add("resizable-preview-pane");
        setStyle("""
            -fx-background-color: #2d2d2d;
            -fx-border-color: #404040;
            -fx-border-width: 0 0 0 1px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, -2, 0);
            """);
        
        // Initially hidden
        setVisible(false);
        setManaged(false);
    }
    
    /**
     * Create header with title and controls
     */
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #3a3a3a, #2d2d2d);
            -fx-border-color: #404040;
            -fx-border-width: 0 0 1px 0;
            """);
        
        // Title
        titleLabel = new Label("Preview");
        titleLabel.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            """);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Resize button
        resizeButton = new Button("⤢");
        resizeButton.getStyleClass().addAll("icon-button");
        resizeButton.setTooltip(new Tooltip("Toggle size"));
        resizeButton.setOnAction(e -> toggleSize());
        
        // Close button
        closeButton = new Button("×");
        closeButton.getStyleClass().addAll("icon-button");
        closeButton.setTooltip(new Tooltip("Close preview"));
        closeButton.setOnAction(e -> hidePreview());
        
        header.getChildren().addAll(titleLabel, spacer, resizeButton, closeButton);
        
        return header;
    }
    
    /**
     * Create resize handle
     */
    private Region createResizeHandle() {
        Region handle = new Region();
        handle.setPrefHeight(5);
        handle.setStyle("""
            -fx-background-color: #404040;
            -fx-cursor: h-resize;
            """);
        
        handle.setOnMouseEntered(e -> {
            handle.setStyle("""
                -fx-background-color: #0078d4;
                -fx-cursor: h-resize;
                """);
        });
        
        handle.setOnMouseExited(e -> {
            handle.setStyle("""
                -fx-background-color: #404040;
                -fx-cursor: h-resize;
                """);
        });
        
        // Resize functionality
        handle.setOnMousePressed(this::handleResizeStart);
        handle.setOnMouseDragged(this::handleResizeDrag);
        handle.setOnMouseReleased(this::handleResizeEnd);
        
        return handle;
    }
    
    /**
     * Initialize preview components
     */
    private void initializePreviewComponents() {
        codePreview = new CodePreviewPane();
        mediaPreview = new MediaPreviewPane();
        
        // Initially hide both
        codePreview.setVisible(false);
        codePreview.setManaged(false);
        mediaPreview.setVisible(false);
        mediaPreview.setManaged(false);
        
        contentPane.getChildren().addAll(codePreview, mediaPreview);
    }
    
    /**
     * Show code preview
     */
    public void showCodePreview(String fileName, String content) {
        Platform.runLater(() -> {
            currentPreviewType = PreviewType.CODE;
            titleLabel.setText("📄 Code Preview - " + fileName);
            
            // Show code preview, hide media preview
            showPreviewComponent(codePreview, mediaPreview);
            
            // Load content
            codePreview.previewFile(fileName, content);
            
            showPreview();
        });
    }
    
    /**
     * Show image preview
     */
    public void showImagePreview(String fileName, byte[] imageData) {
        Platform.runLater(() -> {
            currentPreviewType = PreviewType.IMAGE;
            titleLabel.setText("🖼️ Image Preview - " + fileName);
            
            // Show media preview, hide code preview
            showPreviewComponent(mediaPreview, codePreview);
            
            // Load content
            mediaPreview.previewImage(fileName, imageData);
            
            showPreview();
        });
    }
    
    /**
     * Show audio preview
     */
    public void showAudioPreview(String fileName, byte[] audioData) {
        Platform.runLater(() -> {
            currentPreviewType = PreviewType.AUDIO;
            titleLabel.setText("🎵 Audio Preview - " + fileName);
            
            // Show media preview, hide code preview
            showPreviewComponent(mediaPreview, codePreview);
            
            // Load content
            mediaPreview.previewAudio(fileName, audioData);
            
            showPreview();
        });
    }
    
    /**
     * Show video preview
     */
    public void showVideoPreview(String fileName, byte[] videoData) {
        Platform.runLater(() -> {
            currentPreviewType = PreviewType.VIDEO;
            titleLabel.setText("🎬 Video Preview - " + fileName);
            
            // Show media preview, hide code preview
            showPreviewComponent(mediaPreview, codePreview);
            
            // Load content
            mediaPreview.previewVideo(fileName, videoData);
            
            showPreview();
        });
    }
    
    /**
     * Show unsupported file message
     */
    public void showUnsupportedFile(String fileName, String reason) {
        Platform.runLater(() -> {
            currentPreviewType = PreviewType.NONE;
            titleLabel.setText("📄 " + fileName);
            
            if (CodePreviewPane.canPreviewAsCode(fileName)) {
                showPreviewComponent(codePreview, mediaPreview);
                codePreview.showUnsupportedFile(fileName, reason);
            } else {
                showPreviewComponent(mediaPreview, codePreview);
                mediaPreview.showUnsupportedMedia(fileName, reason);
            }
            
            showPreview();
        });
    }
    
    /**
     * Show specific preview component and hide others
     */
    private void showPreviewComponent(Region showComponent, Region hideComponent) {
        showComponent.setVisible(true);
        showComponent.setManaged(true);
        hideComponent.setVisible(false);
        hideComponent.setManaged(false);
    }
    
    /**
     * Show the preview pane
     */
    private void showPreview() {
        if (!isVisible) {
            isVisible = true;
            setVisible(true);
            setManaged(true);
            
            // Animate in
            setOpacity(0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), this
            );
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }
    
    /**
     * Hide the preview pane
     */
    public void hidePreview() {
        if (isVisible) {
            // Animate out
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), this
            );
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                isVisible = false;
                setVisible(false);
                setManaged(false);
                
                // Clear content
                codePreview.clear();
                mediaPreview.clear();
                currentPreviewType = PreviewType.NONE;
            });
            fadeOut.play();
        }
    }
    
    /**
     * Toggle preview pane size
     */
    private void toggleSize() {
        double currentWidth = getPrefWidth();
        
        if (currentWidth <= MIN_PREVIEW_WIDTH + 50) {
            // Expand to large size
            setPrefWidth(600);
            resizeButton.setText("⤡");
            resizeButton.setTooltip(new Tooltip("Minimize"));
        } else if (currentWidth >= 550) {
            // Minimize to small size
            setPrefWidth(MIN_PREVIEW_WIDTH);
            resizeButton.setText("⤢");
            resizeButton.setTooltip(new Tooltip("Expand"));
        } else {
            // Go to medium size
            setPrefWidth(DEFAULT_PREVIEW_WIDTH);
            resizeButton.setText("⤢");
            resizeButton.setTooltip(new Tooltip("Maximize"));
        }
    }
    
    /**
     * Handle resize start
     */
    private void handleResizeStart(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartWidth = getPrefWidth();
        setCursor(Cursor.H_RESIZE);
    }
    
    /**
     * Handle resize drag
     */
    private void handleResizeDrag(MouseEvent event) {
        double deltaX = dragStartX - event.getSceneX(); // Negative because we're resizing from right edge
        double newWidth = dragStartWidth + deltaX;
        
        // Constrain to min/max width
        newWidth = Math.max(MIN_PREVIEW_WIDTH, Math.min(800, newWidth));
        
        setPrefWidth(newWidth);
    }
    
    /**
     * Handle resize end
     */
    private void handleResizeEnd(MouseEvent event) {
        setCursor(Cursor.DEFAULT);
        lastWidth = getPrefWidth();
    }
    
    /**
     * Get current preview type
     */
    public PreviewType getCurrentPreviewType() {
        return currentPreviewType;
    }
    
    /**
     * Check if preview is visible
     */
    public boolean isPreviewVisible() {
        return isVisible;
    }
    
    /**
     * Get preferred width for layout calculations
     */
    public double getPreferredWidth() {
        return isVisible ? getPrefWidth() : 0;
    }
}