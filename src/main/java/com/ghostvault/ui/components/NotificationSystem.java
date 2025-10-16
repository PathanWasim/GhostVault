package com.ghostvault.ui.components;

import com.ghostvault.ui.animations.AnimationManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Modern notification system with toast messages, progress notifications, and action notifications
 */
public class NotificationSystem {
    
    private static NotificationSystem instance;
    private ConcurrentLinkedQueue<NotificationPopup> activeNotifications = new ConcurrentLinkedQueue<>();
    private VBox notificationContainer;
    private Window parentWindow;
    
    // Configuration
    private NotificationPosition position = NotificationPosition.TOP_RIGHT;
    private Duration defaultDuration = Duration.seconds(4);
    private int maxNotifications = 5;
    private double notificationSpacing = 8;
    
    private NotificationSystem() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static NotificationSystem getInstance() {
        if (instance == null) {
            instance = new NotificationSystem();
        }
        return instance;
    }
    
    /**
     * Initialize notification system with parent window
     */
    public static void initialize(Window parentWindow) {
        getInstance().setParentWindow(parentWindow);
    }
    
    /**
     * Show success notification
     */
    public static void showSuccess(String title, String message) {
        getInstance().show(title, message, NotificationType.SUCCESS, getInstance().defaultDuration, null);
    }
    
    public static void showSuccess(String title, String message, Duration duration) {
        getInstance().show(title, message, NotificationType.SUCCESS, duration, null);
    }
    
    /**
     * Show info notification
     */
    public static void showInfo(String title, String message) {
        getInstance().show(title, message, NotificationType.INFO, getInstance().defaultDuration, null);
    }
    
    public static void showInfo(String title, String message, Duration duration) {
        getInstance().show(title, message, NotificationType.INFO, duration, null);
    }
    
    /**
     * Show warning notification
     */
    public static void showWarning(String title, String message) {
        getInstance().show(title, message, NotificationType.WARNING, getInstance().defaultDuration, null);
    }
    
    public static void showWarning(String title, String message, Duration duration) {
        getInstance().show(title, message, NotificationType.WARNING, duration, null);
    }
    
    /**
     * Show error notification
     */
    public static void showError(String title, String message) {
        getInstance().show(title, message, NotificationType.ERROR, Duration.seconds(6), null);
    }
    
    public static void showError(String title, String message, Duration duration) {
        getInstance().show(title, message, NotificationType.ERROR, duration, null);
    }
    
    /**
     * Show notification with action button
     */
    public static void showWithAction(String title, String message, String actionText, Runnable action) {
        NotificationAction notificationAction = new NotificationAction(actionText, action);
        getInstance().show(title, message, NotificationType.INFO, getInstance().defaultDuration, notificationAction);
    }
    
    public static void showWithAction(String title, String message, NotificationType type, 
                                    String actionText, Runnable action) {
        NotificationAction notificationAction = new NotificationAction(actionText, action);
        getInstance().show(title, message, type, getInstance().defaultDuration, notificationAction);
    }
    
    /**
     * Show progress notification
     */
    public static ProgressNotification showProgress(String title, String message) {
        return getInstance().showProgressNotification(title, message);
    }
    
    /**
     * Clear all notifications
     */
    public static void clearAll() {
        getInstance().clearAllNotifications();
    }
    
    // Private implementation methods
    
    private void setParentWindow(Window window) {
        this.parentWindow = window;
    }
    
    private void show(String title, String message, NotificationType type, Duration duration, NotificationAction action) {
        Platform.runLater(() -> {
            NotificationPopup notification = new NotificationPopup(title, message, type, duration, action);
            showNotification(notification);
        });
    }
    
    private ProgressNotification showProgressNotification(String title, String message) {
        ProgressNotificationPopup popup = new ProgressNotificationPopup(title, message);
        Platform.runLater(() -> showNotification(popup));
        return popup;
    }
    
    private void showNotification(NotificationPopup notification) {
        // Remove oldest notification if at max capacity
        if (activeNotifications.size() >= maxNotifications) {
            NotificationPopup oldest = activeNotifications.poll();
            if (oldest != null) {
                oldest.hide();
            }
        }
        
        // Add to active notifications
        activeNotifications.offer(notification);
        
        // Position and show notification
        positionNotification(notification);
        notification.show();
        
        // Auto-hide after duration (if not persistent)
        if (notification.getDuration() != null && !notification.getDuration().equals(Duration.INDEFINITE)) {
            Timeline autoHide = new Timeline(new KeyFrame(notification.getDuration(), e -> {
                hideNotification(notification);
            }));
            autoHide.play();
        }
    }
    
    private void hideNotification(NotificationPopup notification) {
        activeNotifications.remove(notification);
        notification.hide();
        repositionNotifications();
    }
    
    private void positionNotification(NotificationPopup notification) {
        if (parentWindow == null) return;
        
        double windowX = parentWindow.getX();
        double windowY = parentWindow.getY();
        double windowWidth = parentWindow.getWidth();
        double windowHeight = parentWindow.getHeight();
        
        double notificationWidth = 350;
        double notificationHeight = 80;
        
        double x, y;
        
        switch (position) {
            case TOP_RIGHT:
                x = windowX + windowWidth - notificationWidth - 20;
                y = windowY + 20 + (activeNotifications.size() * (notificationHeight + notificationSpacing));
                break;
            case TOP_LEFT:
                x = windowX + 20;
                y = windowY + 20 + (activeNotifications.size() * (notificationHeight + notificationSpacing));
                break;
            case BOTTOM_RIGHT:
                x = windowX + windowWidth - notificationWidth - 20;
                y = windowY + windowHeight - 20 - notificationHeight - 
                    (activeNotifications.size() * (notificationHeight + notificationSpacing));
                break;
            case BOTTOM_LEFT:
                x = windowX + 20;
                y = windowY + windowHeight - 20 - notificationHeight - 
                    (activeNotifications.size() * (notificationHeight + notificationSpacing));
                break;
            case CENTER:
                x = windowX + (windowWidth - notificationWidth) / 2;
                y = windowY + (windowHeight - notificationHeight) / 2;
                break;
            default:
                x = windowX + windowWidth - notificationWidth - 20;
                y = windowY + 20;
        }
        
        notification.setPosition(x, y);
    }
    
    private void repositionNotifications() {
        Platform.runLater(() -> {
            int index = 0;
            for (NotificationPopup notification : activeNotifications) {
                if (notification.isShowing()) {
                    double newY = calculateYPosition(index);
                    notification.animateToPosition(notification.getX(), newY);
                    index++;
                }
            }
        });
    }
    
    private double calculateYPosition(int index) {
        if (parentWindow == null) return 20;
        
        double windowY = parentWindow.getY();
        double windowHeight = parentWindow.getHeight();
        double notificationHeight = 80;
        
        switch (position) {
            case TOP_RIGHT:
            case TOP_LEFT:
                return windowY + 20 + (index * (notificationHeight + notificationSpacing));
            case BOTTOM_RIGHT:
            case BOTTOM_LEFT:
                return windowY + windowHeight - 20 - notificationHeight - 
                       (index * (notificationHeight + notificationSpacing));
            default:
                return windowY + 20 + (index * (notificationHeight + notificationSpacing));
        }
    }
    
    private void clearAllNotifications() {
        Platform.runLater(() -> {
            for (NotificationPopup notification : activeNotifications) {
                notification.hide();
            }
            activeNotifications.clear();
        });
    }
    
    // Configuration methods
    
    public void setPosition(NotificationPosition position) {
        this.position = position;
    }
    
    public void setDefaultDuration(Duration duration) {
        this.defaultDuration = duration;
    }
    
    public void setMaxNotifications(int max) {
        this.maxNotifications = max;
    }
    
    /**
     * Base notification popup
     */
    private class NotificationPopup extends Popup {
        
        protected VBox content;
        protected Label titleLabel;
        protected Label messageLabel;
        protected Button closeButton;
        protected NotificationType type;
        protected Duration duration;
        
        public NotificationPopup(String title, String message, NotificationType type, Duration duration, NotificationAction action) {
            this.type = type;
            this.duration = duration;
            
            createContent(title, message, action);
            setupStyling();
            setupEventHandlers();
        }
        
        protected void createContent(String title, String message, NotificationAction action) {
            content = new VBox(4);
            content.getStyleClass().add("notification-popup");
            content.getStyleClass().add("notification-" + type.name().toLowerCase());
            content.setPadding(new Insets(12, 16, 12, 16));
            content.setPrefWidth(350);
            content.setMaxWidth(350);
            
            // Header with title and close button
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            
            // Type icon
            Label iconLabel = new Label(getIconForType(type));
            iconLabel.getStyleClass().add("notification-icon");
            
            // Title
            titleLabel = new Label(title);
            titleLabel.getStyleClass().add("notification-title");
            titleLabel.setWrapText(true);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            
            // Close button
            closeButton = new Button("✕");
            closeButton.getStyleClass().addAll("button", "icon", "notification-close");
            closeButton.setPrefSize(20, 20);
            
            header.getChildren().addAll(iconLabel, titleLabel, closeButton);
            
            // Message
            messageLabel = new Label(message);
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);
            
            content.getChildren().addAll(header, messageLabel);
            
            // Action button if provided
            if (action != null) {
                HBox actionContainer = new HBox();
                actionContainer.setAlignment(Pos.CENTER_RIGHT);
                actionContainer.setPadding(new Insets(8, 0, 0, 0));
                
                Button actionButton = new Button(action.getText());
                actionButton.getStyleClass().addAll("button", "primary", "notification-action");
                actionButton.setOnAction(e -> {
                    action.getAction().run();
                    hideNotification(this);
                });
                
                actionContainer.getChildren().add(actionButton);
                content.getChildren().add(actionContainer);
            }
            
            getContent().add(content);
        }
        
        protected void setupStyling() {
            content.setStyle(
                "-fx-background-color: " + getBackgroundColorForType(type) + ";" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: " + getBorderColorForType(type) + ";" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0, 0, 3);"
            );
        }
        
        protected void setupEventHandlers() {
            closeButton.setOnAction(e -> hideNotification(this));
            
            // Auto-hide on click (except for error notifications)
            if (type != NotificationType.ERROR) {
                content.setOnMouseClicked(e -> hideNotification(this));
            }
        }
        
        public void show() {
            if (parentWindow != null) {
                show(parentWindow);
                AnimationManager.slideInFromRight(content);
            }
        }
        
        public void hide() {
            AnimationManager.slideOutToRight(content, AnimationManager.FAST, () -> {
                if (isShowing()) {
                    super.hide();
                }
            });
        }
        
        public void setPosition(double x, double y) {
            setX(x);
            setY(y);
        }
        
        public void animateToPosition(double x, double y) {
            // Animate to new position
            javafx.animation.Timeline timeline = new javafx.animation.Timeline();
            timeline.getKeyFrames().addAll(
                new javafx.animation.KeyFrame(Duration.millis(300), 
                    new javafx.animation.KeyValue(xProperty(), x, AnimationManager.EASE_OUT),
                    new javafx.animation.KeyValue(yProperty(), y, AnimationManager.EASE_OUT))
            );
            timeline.play();
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        private String getIconForType(NotificationType type) {
            switch (type) {
                case SUCCESS:
                    return "✅";
                case INFO:
                    return "ℹ️";
                case WARNING:
                    return "⚠️";
                case ERROR:
                    return "❌";
                default:
                    return "ℹ️";
            }
        }
        
        private String getBackgroundColorForType(NotificationType type) {
            switch (type) {
                case SUCCESS:
                    return "rgba(76, 175, 80, 0.9)";
                case INFO:
                    return "rgba(33, 150, 243, 0.9)";
                case WARNING:
                    return "rgba(255, 152, 0, 0.9)";
                case ERROR:
                    return "rgba(244, 67, 54, 0.9)";
                default:
                    return "rgba(96, 125, 139, 0.9)";
            }
        }
        
        private String getBorderColorForType(NotificationType type) {
            switch (type) {
                case SUCCESS:
                    return "rgba(76, 175, 80, 1)";
                case INFO:
                    return "rgba(33, 150, 243, 1)";
                case WARNING:
                    return "rgba(255, 152, 0, 1)";
                case ERROR:
                    return "rgba(244, 67, 54, 1)";
                default:
                    return "rgba(96, 125, 139, 1)";
            }
        }
    }
    
    /**
     * Progress notification popup
     */
    private class ProgressNotificationPopup extends NotificationPopup implements ProgressNotification {
        
        private ProgressBar progressBar;
        private Label progressLabel;
        private Button cancelButton;
        private Runnable onCancel;
        
        public ProgressNotificationPopup(String title, String message) {
            super(title, message, NotificationType.INFO, Duration.INDEFINITE, null);
        }
        
        @Override
        protected void createContent(String title, String message, NotificationAction action) {
            super.createContent(title, message, action);
            
            // Add progress bar
            progressBar = new ProgressBar(0);
            progressBar.getStyleClass().add("notification-progress");
            progressBar.setPrefWidth(Double.MAX_VALUE);
            
            // Progress label
            progressLabel = new Label("0%");
            progressLabel.getStyleClass().add("notification-progress-label");
            
            HBox progressContainer = new HBox(8);
            progressContainer.setAlignment(Pos.CENTER_LEFT);
            progressContainer.getChildren().addAll(progressBar, progressLabel);
            
            // Cancel button
            HBox buttonContainer = new HBox();
            buttonContainer.setAlignment(Pos.CENTER_RIGHT);
            buttonContainer.setPadding(new Insets(8, 0, 0, 0));
            
            cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().addAll("button", "ghost", "notification-cancel");
            cancelButton.setOnAction(e -> {
                if (onCancel != null) {
                    onCancel.run();
                }
                hideNotification(this);
            });
            
            buttonContainer.getChildren().add(cancelButton);
            
            content.getChildren().addAll(progressContainer, buttonContainer);
        }
        
        @Override
        public void updateProgress(double progress) {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
                progressLabel.setText(String.format("%.0f%%", progress * 100));
            });
        }
        
        @Override
        public void updateMessage(String message) {
            Platform.runLater(() -> {
                messageLabel.setText(message);
            });
        }
        
        @Override
        public void complete(String message) {
            Platform.runLater(() -> {
                updateProgress(1.0);
                updateMessage(message);
                
                // Change to success type
                content.getStyleClass().removeIf(cls -> cls.startsWith("notification-"));
                content.getStyleClass().add("notification-success");
                
                // Hide cancel button
                cancelButton.setVisible(false);
                
                // Auto-hide after 2 seconds
                Timeline autoHide = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                    hideNotification(this);
                }));
                autoHide.play();
            });
        }
        
        @Override
        public void error(String errorMessage) {
            Platform.runLater(() -> {
                updateMessage(errorMessage);
                
                // Change to error type
                content.getStyleClass().removeIf(cls -> cls.startsWith("notification-"));
                content.getStyleClass().add("notification-error");
                
                // Change cancel to close
                cancelButton.setText("Close");
                cancelButton.setOnAction(e -> hideNotification(this));
            });
        }
        
        @Override
        public void setOnCancel(Runnable onCancel) {
            this.onCancel = onCancel;
        }
        
        @Override
        public void dismiss() {
            hideNotification(this);
        }
    }
    
    /**
     * Notification types
     */
    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
    
    /**
     * Notification positions
     */
    public enum NotificationPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }
    
    /**
     * Notification action
     */
    public static class NotificationAction {
        private String text;
        private Runnable action;
        
        public NotificationAction(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
        
        public String getText() { return text; }
        public Runnable getAction() { return action; }
    }
    
    /**
     * Progress notification interface
     */
    public interface ProgressNotification {
        void updateProgress(double progress);
        void updateMessage(String message);
        void complete(String message);
        void error(String errorMessage);
        void setOnCancel(Runnable onCancel);
        void dismiss();
    }
}