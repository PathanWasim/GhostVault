package com.ghostvault.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification system for user feedback and status updates
 */
public class NotificationManager {
    
    private static NotificationManager instance;
    private Stage primaryStage;
    private final List<NotificationPopup> activeNotifications;
    private static final int MAX_NOTIFICATIONS = 5;
    private static final Duration DEFAULT_DURATION = Duration.seconds(4);
    
    public enum NotificationType {
        INFO("ℹ️", "#3498db", "#ffffff"),
        SUCCESS("✅", "#27ae60", "#ffffff"),
        WARNING("⚠️", "#f39c12", "#ffffff"),
        ERROR("❌", "#e74c3c", "#ffffff"),
        SECURITY("🛡️", "#9b59b6", "#ffffff");
        
        private final String icon;
        private final String backgroundColor;
        private final String textColor;
        
        NotificationType(String icon, String backgroundColor, String textColor) {
            this.icon = icon;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }
        
        public String getIcon() { return icon; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getTextColor() { return textColor; }
    }
    
    private NotificationManager() {
        this.activeNotifications = new ArrayList<>();
    }
    
    /**
     * Get singleton instance
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    /**
     * Initialize with primary stage
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Show notification
     */
    public void showNotification(NotificationType type, String title, String message) {
        showNotification(type, title, message, DEFAULT_DURATION);
    }
    
    /**
     * Show notification with custom duration
     */
    public void showNotification(NotificationType type, String title, String message, Duration duration) {
        Platform.runLater(() -> {
            // Remove oldest notification if at max capacity
            if (activeNotifications.size() >= MAX_NOTIFICATIONS) {
                NotificationPopup oldest = activeNotifications.get(0);
                oldest.hide();
            }
            
            NotificationPopup notification = new NotificationPopup(type, title, message, duration);
            activeNotifications.add(notification);
            notification.show();
        });
    }
    
    /**
     * Show info notification
     */
    public void showInfo(String title, String message) {
        showNotification(NotificationType.INFO, title, message);
    }
    
    /**
     * Show success notification
     */
    public void showSuccess(String title, String message) {
        showNotification(NotificationType.SUCCESS, title, message);
    }
    
    /**
     * Show warning notification
     */
    public void showWarning(String title, String message) {
        showNotification(NotificationType.WARNING, title, message);
    }
    
    /**
     * Show error notification
     */
    public void showError(String title, String message) {
        showNotification(NotificationType.ERROR, title, message, Duration.seconds(6)); // Longer for errors
    }
    
    /**
     * Show security notification
     */
    public void showSecurity(String title, String message) {
        showNotification(NotificationType.SECURITY, title, message, Duration.seconds(8)); // Longer for security
    }
    
    /**
     * Clear all notifications
     */
    public void clearAll() {
        Platform.runLater(() -> {
            for (NotificationPopup notification : new ArrayList<>(activeNotifications)) {
                notification.hide();
            }
        });
    }
    
    /**
     * Individual notification popup
     */
    private class NotificationPopup {
        
        private final Popup popup;
        private final NotificationType type;
        private final Duration duration;
        
        public NotificationPopup(NotificationType type, String title, String message, Duration duration) {
            this.type = type;
            this.duration = duration;
            this.popup = createPopup(title, message);
        }
        
        /**
         * Create popup content
         */
        private Popup createPopup(String title, String message) {
            Popup popup = new Popup();
            popup.setAutoHide(false);
            popup.setHideOnEscape(false);
            
            // Main container
            VBox container = new VBox(5);
            container.setPadding(new Insets(12, 16, 12, 16));
            container.setAlignment(Pos.CENTER_LEFT);
            container.setMaxWidth(350);
            container.setStyle(
                "-fx-background-color: " + type.getBackgroundColor() + ";" +
                "-fx-background-radius: 8px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
            );
            
            // Header with icon and title
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label iconLabel = new Label(type.getIcon());
            iconLabel.setFont(Font.font(16));
            
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
            titleLabel.setTextFill(Color.web(type.getTextColor()));
            titleLabel.setWrapText(true);
            
            header.getChildren().addAll(iconLabel, titleLabel);
            
            // Message
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System", 12));
            messageLabel.setTextFill(Color.web(type.getTextColor()));
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(320);
            
            container.getChildren().addAll(header, messageLabel);
            
            // Make clickable to dismiss
            container.setOnMouseClicked(e -> hide());
            
            popup.getContent().add(container);
            
            return popup;
        }
        
        /**
         * Show the notification
         */
        public void show() {
            if (primaryStage == null) return;
            
            // Position at top-right of primary stage
            double x = primaryStage.getX() + primaryStage.getWidth() - 370;
            double y = primaryStage.getY() + 50 + (activeNotifications.size() * 80);
            
            popup.show(primaryStage, x, y);
            
            // Animate in
            animateIn();
            
            // Auto-hide after duration
            PauseTransition pause = new PauseTransition(duration);
            pause.setOnFinished(e -> hide());
            pause.play();
        }
        
        /**
         * Hide the notification
         */
        public void hide() {
            animateOut(() -> {
                popup.hide();
                activeNotifications.remove(this);
                repositionNotifications();
            });
        }
        
        /**
         * Animate notification in
         */
        private void animateIn() {
            Node content = popup.getContent().get(0);
            
            // Slide in from right
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), content);
            slideIn.setFromX(350);
            slideIn.setToX(0);
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            slideIn.play();
            fadeIn.play();
        }
        
        /**
         * Animate notification out
         */
        private void animateOut(Runnable onComplete) {
            Node content = popup.getContent().get(0);
            
            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), content);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            // Slide out to right
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), content);
            slideOut.setFromX(0);
            slideOut.setToX(350);
            
            SequentialTransition sequence = new SequentialTransition(fadeOut, slideOut);
            sequence.setOnFinished(e -> onComplete.run());
            sequence.play();
        }
    }
    
    /**
     * Reposition remaining notifications
     */
    private void repositionNotifications() {
        Platform.runLater(() -> {
            for (int i = 0; i < activeNotifications.size(); i++) {
                NotificationPopup notification = activeNotifications.get(i);
                
                double x = primaryStage.getX() + primaryStage.getWidth() - 370;
                double y = primaryStage.getY() + 50 + (i * 80);
                
                // Animate to new position
                TranslateTransition reposition = new TranslateTransition(Duration.millis(200), 
                    (StackPane) notification.popup.getContent().get(0));
                reposition.setToY(y - notification.popup.getY());
                reposition.play();
            }
        });
    }
    
    /**
     * Show toast notification (simple message at bottom)
     */
    public void showToast(String message) {
        showToast(message, Duration.seconds(2));
    }
    
    /**
     * Show toast notification with custom duration
     */
    public void showToast(String message, Duration duration) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            
            Label toastLabel = new Label(message);
            toastLabel.setFont(Font.font("System", 12));
            toastLabel.setTextFill(Color.WHITE);
            toastLabel.setPadding(new Insets(8, 12, 8, 12));
            toastLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.8);" +
                "-fx-background-radius: 15px;"
            );
            
            StackPane root = new StackPane(toastLabel);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);
            
            // Position at bottom center of primary stage
            toastStage.show();
            double x = primaryStage.getX() + (primaryStage.getWidth() - toastStage.getWidth()) / 2;
            double y = primaryStage.getY() + primaryStage.getHeight() - 100;
            toastStage.setX(x);
            toastStage.setY(y);
            
            // Animate in and out
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            PauseTransition pause = new PauseTransition(duration);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> toastStage.close());
            
            SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
            sequence.play();
        });
    }
}