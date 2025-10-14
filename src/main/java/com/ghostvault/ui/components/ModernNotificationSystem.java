package com.ghostvault.ui.components;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Modern notification system with toast notifications and in-app alerts
 */
public class ModernNotificationSystem {
    
    public enum NotificationType {
        SUCCESS("✓", "#107c10", "#d4edda"),
        INFO("ℹ", "#0078d4", "#d1ecf1"),
        WARNING("⚠", "#ff8c00", "#fff3cd"),
        ERROR("✗", "#d13438", "#f8d7da"),
        SECURITY("🛡", "#9c27b0", "#e1bee7")
    }
    
    private static ModernNotificationSystem instance;
    private final Stage parentStage;
    private final VBox notificationContainer;
    private final ConcurrentLinkedQueue<NotificationItem> notificationQueue;
    private final AtomicInteger notificationCounter;
    
    // Configuration
    private static final int MAX_NOTIFICATIONS = 5;
    private static final Duration NOTIFICATION_DURATION = Duration.seconds(5);
    private static final Duration FADE_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_WIDTH = 350;
    private static final double NOTIFICATION_SPACING = 10;
    
    private ModernNotificationSystem(Stage parentStage) {
        this.parentStage = parentStage;
        this.notificationContainer = new VBox(NOTIFICATION_SPACING);
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        this.notificationCounter = new AtomicInteger(0);
        
        setupNotificationContainer();
    }
    
    /**
     * Get singleton instance
     */
    public static ModernNotificationSystem getInstance(Stage parentStage) {
        if (instance == null) {
            instance = new ModernNotificationSystem(parentStage);
        }
        return instance;
    }
    
    /**
     * Setup notification container
     */
    private void setupNotificationContainer() {
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(20));
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setPickOnBounds(false);
        
        // Position container in top-right corner
        if (parentStage != null) {
            parentStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                updateContainerPosition();
            });
            
            parentStage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                updateContainerPosition();
            });
        }
    }
    
    /**
     * Update container position
     */
    private void updateContainerPosition() {
        if (parentStage != null && parentStage.getScene() != null) {
            double stageWidth = parentStage.getWidth();
            double stageHeight = parentStage.getHeight();
            
            notificationContainer.setLayoutX(stageWidth - NOTIFICATION_WIDTH - 20);
            notificationContainer.setLayoutY(20);
        }
    }
    
    /**
     * Show success notification
     */
    public void showSuccess(String title, String message) {
        showNotification(NotificationType.SUCCESS, title, message, NOTIFICATION_DURATION);
    }
    
    /**
     * Show info notification
     */
    public void showInfo(String title, String message) {
        showNotification(NotificationType.INFO, title, message, NOTIFICATION_DURATION);
    }
    
    /**
     * Show warning notification
     */
    public void showWarning(String title, String message) {
        showNotification(NotificationType.WARNING, title, message, NOTIFICATION_DURATION);
    }
    
    /**
     * Show error notification
     */
    public void showError(String title, String message) {
        showNotification(NotificationType.ERROR, title, message, Duration.seconds(8)); // Longer for errors
    }
    
    /**
     * Show security notification
     */
    public void showSecurity(String title, String message) {
        showNotification(NotificationType.SECURITY, title, message, Duration.seconds(10)); // Longer for security
    }
    
    /**
     * Show notification with custom duration
     */
    public void showNotification(NotificationType type, String title, String message, Duration duration) {
        Platform.runLater(() -> {
            NotificationItem notification = createNotification(type, title, message, duration);
            addNotification(notification);
        });
    }
    
    /**
     * Show persistent notification (no auto-dismiss)
     */
    public void showPersistent(NotificationType type, String title, String message) {
        Platform.runLater(() -> {
            NotificationItem notification = createNotification(type, title, message, null);
            addNotification(notification);
        });
    }
    
    /**
     * Create notification item
     */
    private NotificationItem createNotification(NotificationType type, String title, String message, Duration duration) {
        int id = notificationCounter.incrementAndGet();
        
        // Main container
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(16));
        container.setPrefWidth(NOTIFICATION_WIDTH);
        container.setMaxWidth(NOTIFICATION_WIDTH);
        
        // Icon
        Label icon = new Label(type.icon);
        icon.setStyle(String.format("""
            -fx-font-size: 20px;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            """, type.iconColor));
        
        // Content
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-text-fill: #ffffff;
            -fx-wrap-text: true;
            """);
        titleLabel.setMaxWidth(NOTIFICATION_WIDTH - 100);
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #cccccc;
            -fx-wrap-text: true;
            """);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 100);
        
        content.getChildren().addAll(titleLabel, messageLabel);
        
        // Close button
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("notification-close");
        closeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #888888;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: 20px;
            -fx-min-height: 20px;
            -fx-max-width: 20px;
            -fx-max-height: 20px;
            -fx-cursor: hand;
            """);
        
        container.getChildren().addAll(icon, content, closeButton);
        
        // Styling based on type
        container.setStyle(String.format("""
            -fx-background-color: linear-gradient(to right, %s, #2d2d2d);
            -fx-border-color: %s;
            -fx-border-width: 0 0 0 4px;
            -fx-border-radius: 0 6px 6px 0;
            -fx-background-radius: 0 6px 6px 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 4);
            """, 
            Color.web(type.iconColor).deriveColor(0, 1, 1, 0.1).toString().replace("0x", "#"),
            type.iconColor));
        
        // Create notification item
        NotificationItem notification = new NotificationItem(id, container, duration);
        
        // Setup close button
        closeButton.setOnAction(e -> removeNotification(notification));
        
        // Setup hover effects
        container.setOnMouseEntered(e -> {
            container.setStyle(container.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        
        container.setOnMouseExited(e -> {
            container.setStyle(container.getStyle().replace("-fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
        });
        
        return notification;
    }
    
    /**
     * Add notification to display
     */
    private void addNotification(NotificationItem notification) {
        // Remove oldest notifications if at limit
        while (notificationQueue.size() >= MAX_NOTIFICATIONS) {
            NotificationItem oldest = notificationQueue.poll();
            if (oldest != null) {
                removeNotificationFromUI(oldest);
            }
        }
        
        notificationQueue.offer(notification);
        
        // Add to UI with animation
        addNotificationToUI(notification);
        
        // Setup auto-dismiss if duration is specified
        if (notification.duration != null) {
            Timeline dismissTimer = new Timeline(
                new KeyFrame(notification.duration, e -> removeNotification(notification))
            );
            dismissTimer.play();
            notification.dismissTimer = dismissTimer;
        }
    }
    
    /**
     * Add notification to UI with slide-in animation
     */
    private void addNotificationToUI(NotificationItem notification) {
        HBox container = notification.container;
        
        // Initial state (off-screen)
        container.setTranslateX(NOTIFICATION_WIDTH + 50);
        container.setOpacity(0);
        
        // Add to container
        notificationContainer.getChildren().add(0, container);
        
        // Ensure container is in scene
        if (!isContainerInScene()) {
            addContainerToScene();
        }
        
        // Slide-in animation
        TranslateTransition slideIn = new TranslateTransition(FADE_DURATION, container);
        slideIn.setFromX(NOTIFICATION_WIDTH + 50);
        slideIn.setToX(0);
        
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showAnimation = new ParallelTransition(slideIn, fadeIn);
        showAnimation.play();
    }
    
    /**
     * Remove notification
     */
    private void removeNotification(NotificationItem notification) {
        if (notification.dismissTimer != null) {
            notification.dismissTimer.stop();
        }
        
        notificationQueue.remove(notification);
        removeNotificationFromUI(notification);
    }
    
    /**
     * Remove notification from UI with slide-out animation
     */
    private void removeNotificationFromUI(NotificationItem notification) {
        HBox container = notification.container;
        
        if (!notificationContainer.getChildren().contains(container)) {
            return;
        }
        
        // Slide-out animation
        TranslateTransition slideOut = new TranslateTransition(FADE_DURATION, container);
        slideOut.setFromX(0);
        slideOut.setToX(NOTIFICATION_WIDTH + 50);
        
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hideAnimation = new ParallelTransition(slideOut, fadeOut);
        hideAnimation.setOnFinished(e -> {
            notificationContainer.getChildren().remove(container);
            
            // Remove container from scene if no notifications
            if (notificationContainer.getChildren().isEmpty()) {
                removeContainerFromScene();
            }
        });
        
        hideAnimation.play();
    }
    
    /**
     * Check if container is in scene
     */
    private boolean isContainerInScene() {
        return parentStage != null && 
               parentStage.getScene() != null && 
               parentStage.getScene().getRoot() instanceof Pane &&
               ((Pane) parentStage.getScene().getRoot()).getChildren().contains(notificationContainer);
    }
    
    /**
     * Add container to scene
     */
    private void addContainerToScene() {
        if (parentStage != null && parentStage.getScene() != null && parentStage.getScene().getRoot() instanceof Pane) {
            Pane root = (Pane) parentStage.getScene().getRoot();
            root.getChildren().add(notificationContainer);
            updateContainerPosition();
        }
    }
    
    /**
     * Remove container from scene
     */
    private void removeContainerFromScene() {
        if (parentStage != null && parentStage.getScene() != null && parentStage.getScene().getRoot() instanceof Pane) {
            Pane root = (Pane) parentStage.getScene().getRoot();
            root.getChildren().remove(notificationContainer);
        }
    }
    
    /**
     * Clear all notifications
     */
    public void clearAll() {
        Platform.runLater(() -> {
            for (NotificationItem notification : notificationQueue) {
                if (notification.dismissTimer != null) {
                    notification.dismissTimer.stop();
                }
            }
            notificationQueue.clear();
            notificationContainer.getChildren().clear();
            removeContainerFromScene();
        });
    }
    
    /**
     * Show progress notification
     */
    public ProgressNotification showProgress(String title, String message) {
        return new ProgressNotification(this, title, message);
    }
    
    /**
     * Notification item data class
     */
    private static class NotificationItem {
        final int id;
        final HBox container;
        final Duration duration;
        Timeline dismissTimer;
        
        NotificationItem(int id, HBox container, Duration duration) {
            this.id = id;
            this.container = container;
            this.duration = duration;
        }
    }
    
    /**
     * Progress notification for long-running operations
     */
    public static class ProgressNotification {
        private final ModernNotificationSystem system;
        private final NotificationItem notification;
        private final javafx.scene.control.ProgressBar progressBar;
        private final Label statusLabel;
        
        ProgressNotification(ModernNotificationSystem system, String title, String message) {
            this.system = system;
            
            // Create progress notification
            HBox container = new HBox(12);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(16));
            container.setPrefWidth(NOTIFICATION_WIDTH);
            container.setMaxWidth(NOTIFICATION_WIDTH);
            
            // Icon
            Label icon = new Label("⏳");
            icon.setStyle("""
                -fx-font-size: 20px;
                -fx-text-fill: #0078d4;
                -fx-font-weight: bold;
                """);
            
            // Content
            VBox content = new VBox(6);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content, Priority.ALWAYS);
            
            Label titleLabel = new Label(title);
            titleLabel.setStyle("""
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #ffffff;
                """);
            
            statusLabel = new Label(message);
            statusLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-text-fill: #cccccc;
                """);
            
            progressBar = new javafx.scene.control.ProgressBar();
            progressBar.setPrefWidth(NOTIFICATION_WIDTH - 100);
            progressBar.getStyleClass().add("progress-notification");
            
            content.getChildren().addAll(titleLabel, statusLabel, progressBar);
            container.getChildren().addAll(icon, content);
            
            // Styling
            container.setStyle("""
                -fx-background-color: linear-gradient(to right, rgba(0, 120, 212, 0.1), #2d2d2d);
                -fx-border-color: #0078d4;
                -fx-border-width: 0 0 0 4px;
                -fx-border-radius: 0 6px 6px 0;
                -fx-background-radius: 0 6px 6px 0;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 4);
                """);
            
            this.notification = new NotificationItem(system.notificationCounter.incrementAndGet(), container, null);
            
            Platform.runLater(() -> {
                system.addNotificationToUI(notification);
            });
        }
        
        /**
         * Update progress (0.0 to 1.0)
         */
        public void updateProgress(double progress) {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
            });
        }
        
        /**
         * Update status message
         */
        public void updateStatus(String status) {
            Platform.runLater(() -> {
                statusLabel.setText(status);
            });
        }
        
        /**
         * Complete the progress notification
         */
        public void complete(String message) {
            Platform.runLater(() -> {
                progressBar.setProgress(1.0);
                statusLabel.setText(message);
                
                // Auto-dismiss after 2 seconds
                Timeline dismissTimer = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> system.removeNotification(notification))
                );
                dismissTimer.play();
            });
        }
        
        /**
         * Fail the progress notification
         */
        public void fail(String errorMessage) {
            Platform.runLater(() -> {
                statusLabel.setText(errorMessage);
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d13438;");
                
                // Change icon to error
                HBox container = notification.container;
                Label icon = (Label) container.getChildren().get(0);
                icon.setText("✗");
                icon.setStyle("""
                    -fx-font-size: 20px;
                    -fx-text-fill: #d13438;
                    -fx-font-weight: bold;
                    """);
                
                // Auto-dismiss after 5 seconds
                Timeline dismissTimer = new Timeline(
                    new KeyFrame(Duration.seconds(5), e -> system.removeNotification(notification))
                );
                dismissTimer.play();
            });
        }
        
        /**
         * Dismiss the progress notification
         */
        public void dismiss() {
            Platform.runLater(() -> {
                system.removeNotification(notification);
            });
        }
    }
}