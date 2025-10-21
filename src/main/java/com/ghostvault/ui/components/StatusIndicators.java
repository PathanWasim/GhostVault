package com.ghostvault.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Professional status indicators and badges for GhostVault
 */
public class StatusIndicators {
    
    // Status Types
    public enum StatusType {
        SUCCESS, ERROR, WARNING, INFO, LOADING, OFFLINE, ONLINE, 
        SECURE, INSECURE, ENCRYPTED, DECRYPTED, SYNCING, COMPLETE
    }
    
    // Badge Types
    public enum BadgeType {
        PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, INFO, LIGHT, DARK
    }
    
    // Size Types
    public enum Size {
        SMALL, MEDIUM, LARGE
    }
    
    /**
     * Create a status indicator with icon and text
     */
    public static HBox createStatusIndicator(StatusType status, String text, Size size) {
        HBox container = new HBox(6);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("status-indicator");
        
        // Create status icon
        Label iconLabel = createStatusIcon(status, size);
        
        // Create status text
        Label textLabel = new Label(text);
        textLabel.getStyleClass().addAll("status-text", getSizeClass(size));
        textLabel.setTextFill(getStatusColor(status));
        
        container.getChildren().addAll(iconLabel, textLabel);
        
        // Add tooltip if needed
        if (text != null && !text.isEmpty()) {
            Tooltip tooltip = new Tooltip(getStatusDescription(status));
            tooltip.getStyleClass().add("status-tooltip");
            Tooltip.install(container, tooltip);
        }
        
        return container;
    }
    
    /**
     * Create a simple status icon
     */
    public static Label createStatusIcon(StatusType status, Size size) {
        String icon = getStatusIcon(status);
        double iconSize = getIconSize(size);
        Color color = getStatusColor(status);
        
        // TODO: Add createIcon method to ModernIcons
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: " + iconSize + "px; -fx-text-fill: " + color.toString().replace("0x", "#") + ";");
        iconLabel.getStyleClass().addAll("status-icon", getSizeClass(size));
        
        // Add animation for loading status
        if (status == StatusType.LOADING || status == StatusType.SYNCING) {
            addLoadingAnimation(iconLabel);
        }
        
        return iconLabel;
    }
    
    /**
     * Create a status dot indicator
     */
    public static StackPane createStatusDot(StatusType status, Size size) {
        double dotSize = getDotSize(size);
        
        Circle dot = new Circle(dotSize / 2);
        dot.setFill(getStatusColor(status));
        dot.getStyleClass().add("status-dot");
        
        StackPane container = new StackPane(dot);
        container.getStyleClass().addAll("status-dot-container", getSizeClass(size));
        
        // Add pulsing animation for active states
        if (status == StatusType.LOADING || status == StatusType.SYNCING || status == StatusType.ONLINE) {
            addPulseAnimation(dot);
        }
        
        return container;
    }
    
    /**
     * Create a badge with text
     */
    public static Label createBadge(String text, BadgeType type, Size size) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", getBadgeTypeClass(type), getSizeClass(size));
        
        // Set colors based on type
        setBadgeColors(badge, type);
        
        return badge;
    }
    
    /**
     * Create a notification badge (number)
     */
    public static Label createNotificationBadge(int count, Size size) {
        String text = count > 99 ? "99+" : String.valueOf(count);
        Label badge = createBadge(text, BadgeType.DANGER, size);
        badge.getStyleClass().add("notification-badge");
        
        if (count == 0) {
            badge.setVisible(false);
        }
        
        return badge;
    }
    
    /**
     * Create a security level indicator
     */
    public static HBox createSecurityLevelIndicator(double level, Size size) {
        HBox container = new HBox(4);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("security-level-indicator");
        
        // Create security bars
        int bars = 5;
        int activeBars = (int) Math.ceil(level * bars);
        
        for (int i = 0; i < bars; i++) {
            StackPane bar = new StackPane();
            bar.getStyleClass().add("security-bar");
            
            double barWidth = getBarWidth(size);
            double barHeight = getBarHeight(size);
            bar.setPrefSize(barWidth, barHeight);
            bar.setMaxSize(barWidth, barHeight);
            
            if (i < activeBars) {
                bar.getStyleClass().add("security-bar-active");
                Color barColor = getSecurityColor(level);
                bar.setStyle(String.format("-fx-background-color: %s;", toHexString(barColor)));
            } else {
                bar.getStyleClass().add("security-bar-inactive");
            }
            
            container.getChildren().add(bar);
        }
        
        // Add security level text
        Label levelText = new Label(String.format("%.0f%%", level * 100));
        levelText.getStyleClass().addAll("security-level-text", getSizeClass(size));
        levelText.setTextFill(getSecurityColor(level));
        
        container.getChildren().add(levelText);
        
        return container;
    }
    
    /**
     * Create a progress indicator badge
     */
    public static HBox createProgressIndicator(double progress, String text, Size size) {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("progress-indicator");
        
        // Progress bar
        StackPane progressBar = new StackPane();
        progressBar.getStyleClass().add("progress-bar-container");
        
        double barWidth = getProgressBarWidth(size);
        double barHeight = getProgressBarHeight(size);
        progressBar.setPrefSize(barWidth, barHeight);
        
        // Background
        StackPane background = new StackPane();
        background.getStyleClass().add("progress-bar-background");
        background.setPrefSize(barWidth, barHeight);
        
        // Fill
        StackPane fill = new StackPane();
        fill.getStyleClass().add("progress-bar-fill");
        fill.setPrefWidth(barWidth * progress);
        fill.setPrefHeight(barHeight);
        fill.setAlignment(Pos.CENTER_LEFT);
        
        progressBar.getChildren().addAll(background, fill);
        
        // Progress text
        Label progressText = new Label(text != null ? text : String.format("%.0f%%", progress * 100));
        progressText.getStyleClass().addAll("progress-text", getSizeClass(size));
        
        container.getChildren().addAll(progressBar, progressText);
        
        return container;
    }
    
    /**
     * Create a file type badge
     */
    public static Label createFileTypeBadge(String extension, Size size) {
        String displayText = extension.toUpperCase().replace(".", "");
        Label badge = new Label(displayText);
        badge.getStyleClass().addAll("file-type-badge", getSizeClass(size));
        
        // Set color based on file type
        Color badgeColor = getFileTypeColor(extension);
        badge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white;", 
            toHexString(badgeColor)
        ));
        
        return badge;
    }
    
    /**
     * Create an online/offline indicator
     */
    public static HBox createConnectionIndicator(boolean isOnline, Size size) {
        StatusType status = isOnline ? StatusType.ONLINE : StatusType.OFFLINE;
        String text = isOnline ? "Online" : "Offline";
        
        return createStatusIndicator(status, text, size);
    }
    
    /**
     * Create a sync status indicator
     */
    public static HBox createSyncIndicator(boolean isSyncing, long lastSyncTime, Size size) {
        StatusType status = isSyncing ? StatusType.SYNCING : StatusType.COMPLETE;
        String text;
        
        if (isSyncing) {
            text = "Syncing...";
        } else {
            long timeDiff = System.currentTimeMillis() - lastSyncTime;
            text = formatTimeDifference(timeDiff);
        }
        
        return createStatusIndicator(status, text, size);
    }
    
    // Helper methods
    
    private static String getStatusIcon(StatusType status) {
        switch (status) {
            case SUCCESS: case COMPLETE: return ModernIcons.SUCCESS;
            case ERROR: return ModernIcons.ERROR;
            case WARNING: return ModernIcons.WARNING;
            case INFO: return ModernIcons.INFO;
            case LOADING: case SYNCING: return ModernIcons.SYNCING;
            case OFFLINE: return ModernIcons.DISCONNECTED;
            case ONLINE: return ModernIcons.CONNECTED;
            case SECURE: case ENCRYPTED: return ModernIcons.SECURE;
            case INSECURE: case DECRYPTED: return ModernIcons.VAULT;
            default: return ModernIcons.INFO;
        }
    }
    
    private static Color getStatusColor(StatusType status) {
        switch (status) {
            case SUCCESS: case COMPLETE: case ONLINE: case SECURE: case ENCRYPTED:
                return ModernIcons.ICON_SUCCESS;
            case ERROR: case INSECURE:
                return ModernIcons.ICON_DANGER;
            case WARNING: case DECRYPTED:
                return ModernIcons.ICON_WARNING;
            case INFO:
                return ModernIcons.ICON_INFO;
            case LOADING: case SYNCING:
                return ModernIcons.ICON_PRIMARY;
            case OFFLINE:
                return ModernIcons.ICON_MUTED;
            default:
                return ModernIcons.ICON_MUTED;
        }
    }
    
    private static String getStatusDescription(StatusType status) {
        switch (status) {
            case SUCCESS: return "Operation completed successfully";
            case ERROR: return "An error occurred";
            case WARNING: return "Warning - attention required";
            case INFO: return "Information";
            case LOADING: return "Loading...";
            case SYNCING: return "Synchronizing data";
            case COMPLETE: return "Task completed";
            case OFFLINE: return "Currently offline";
            case ONLINE: return "Connected and online";
            case SECURE: return "Secure connection";
            case INSECURE: return "Insecure connection";
            case ENCRYPTED: return "Data is encrypted";
            case DECRYPTED: return "Data is decrypted";
            default: return "";
        }
    }
    
    private static double getIconSize(Size size) {
        switch (size) {
            case SMALL: return ModernIcons.ICON_SMALL;
            case MEDIUM: return ModernIcons.ICON_MEDIUM;
            case LARGE: return ModernIcons.ICON_LARGE;
            default: return ModernIcons.ICON_MEDIUM;
        }
    }
    
    private static double getDotSize(Size size) {
        switch (size) {
            case SMALL: return 8;
            case MEDIUM: return 12;
            case LARGE: return 16;
            default: return 12;
        }
    }
    
    private static String getSizeClass(Size size) {
        switch (size) {
            case SMALL: return "size-small";
            case MEDIUM: return "size-medium";
            case LARGE: return "size-large";
            default: return "size-medium";
        }
    }
    
    private static String getBadgeTypeClass(BadgeType type) {
        switch (type) {
            case PRIMARY: return "badge-primary";
            case SECONDARY: return "badge-secondary";
            case SUCCESS: return "badge-success";
            case DANGER: return "badge-danger";
            case WARNING: return "badge-warning";
            case INFO: return "badge-info";
            case LIGHT: return "badge-light";
            case DARK: return "badge-dark";
            default: return "badge-primary";
        }
    }
    
    private static void setBadgeColors(Label badge, BadgeType type) {
        String backgroundColor, textColor;
        
        switch (type) {
            case PRIMARY:
                backgroundColor = "#0078d4";
                textColor = "white";
                break;
            case SECONDARY:
                backgroundColor = "#6c757d";
                textColor = "white";
                break;
            case SUCCESS:
                backgroundColor = "#28a745";
                textColor = "white";
                break;
            case DANGER:
                backgroundColor = "#dc3545";
                textColor = "white";
                break;
            case WARNING:
                backgroundColor = "#ffc107";
                textColor = "#212529";
                break;
            case INFO:
                backgroundColor = "#17a2b8";
                textColor = "white";
                break;
            case LIGHT:
                backgroundColor = "#f8f9fa";
                textColor = "#212529";
                break;
            case DARK:
                backgroundColor = "#343a40";
                textColor = "white";
                break;
            default:
                backgroundColor = "#0078d4";
                textColor = "white";
        }
        
        badge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s;",
            backgroundColor, textColor
        ));
    }
    
    private static Color getSecurityColor(double level) {
        if (level >= 0.8) return Color.web("#28a745"); // Green
        if (level >= 0.5) return Color.web("#ffc107"); // Yellow
        return Color.web("#dc3545"); // Red
    }
    
    private static Color getFileTypeColor(String extension) {
        switch (extension.toLowerCase()) {
            case ".jpg": case ".jpeg": case ".png": case ".gif": case ".bmp":
                return Color.web("#4caf50"); // Green for images
            case ".mp4": case ".avi": case ".mkv": case ".mov":
                return Color.web("#f44336"); // Red for videos
            case ".mp3": case ".wav": case ".aac": case ".flac":
                return Color.web("#ff9800"); // Orange for audio
            case ".pdf": case ".doc": case ".docx": case ".txt":
                return Color.web("#9c27b0"); // Purple for documents
            case ".zip": case ".rar": case ".7z":
                return Color.web("#607d8b"); // Blue-grey for archives
            case ".java": case ".py": case ".cpp": case ".js":
                return Color.web("#2196f3"); // Blue for code
            default:
                return Color.web("#6c757d"); // Grey for others
        }
    }
    
    private static double getBarWidth(Size size) {
        switch (size) {
            case SMALL: return 3;
            case MEDIUM: return 4;
            case LARGE: return 5;
            default: return 4;
        }
    }
    
    private static double getBarHeight(Size size) {
        switch (size) {
            case SMALL: return 12;
            case MEDIUM: return 16;
            case LARGE: return 20;
            default: return 16;
        }
    }
    
    private static double getProgressBarWidth(Size size) {
        switch (size) {
            case SMALL: return 60;
            case MEDIUM: return 80;
            case LARGE: return 100;
            default: return 80;
        }
    }
    
    private static double getProgressBarHeight(Size size) {
        switch (size) {
            case SMALL: return 4;
            case MEDIUM: return 6;
            case LARGE: return 8;
            default: return 6;
        }
    }
    
    private static void addLoadingAnimation(Label iconLabel) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), e -> iconLabel.setRotate(0)),
            new KeyFrame(Duration.millis(1000), e -> iconLabel.setRotate(360))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private static void addPulseAnimation(Circle dot) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), dot);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }
    
    private static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    private static String formatTimeDifference(long timeDiff) {
        long seconds = timeDiff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }
    
    /**
     * Create animated status change
     */
    public static void animateStatusChange(Label statusIcon, StatusType newStatus) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), statusIcon);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            statusIcon.setText(getStatusIcon(newStatus));
            statusIcon.setTextFill(getStatusColor(newStatus));
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), statusIcon);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Update notification badge count with animation
     */
    public static void updateNotificationBadge(Label badge, int newCount) {
        if (newCount == 0) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), badge);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> badge.setVisible(false));
            fadeOut.play();
        } else {
            if (!badge.isVisible()) {
                badge.setVisible(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), badge);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
            
            String text = newCount > 99 ? "99+" : String.valueOf(newCount);
            badge.setText(text);
            
            // Bounce animation for new notifications
            ScaleTransition bounce = new ScaleTransition(Duration.millis(200), badge);
            bounce.setFromX(1.0);
            bounce.setFromY(1.0);
            bounce.setToX(1.2);
            bounce.setToY(1.2);
            bounce.setAutoReverse(true);
            bounce.setCycleCount(2);
            bounce.play();
        }
    }
}