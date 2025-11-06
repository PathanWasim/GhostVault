package com.ghostvault.ui.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced tooltip manager with rich content and animations
 */
public class TooltipManager {
    
    private static TooltipManager instance;
    private Map<Node, EnhancedTooltip> tooltips = new HashMap<>();
    
    // Tooltip configuration
    private Duration showDelay = Duration.millis(500);
    private Duration hideDelay = Duration.millis(100);
    private Duration fadeInDuration = Duration.millis(200);
    private Duration fadeOutDuration = Duration.millis(150);
    
    private TooltipManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static TooltipManager getInstance() {
        if (instance == null) {
            instance = new TooltipManager();
        }
        return instance;
    }
    
    /**
     * Install simple tooltip
     */
    public static void install(Node node, String text) {
        getInstance().installTooltip(node, text, null, null, TooltipType.SIMPLE);
    }
    
    /**
     * Install tooltip with description
     */
    public static void install(Node node, String title, String description) {
        getInstance().installTooltip(node, title, description, null, TooltipType.DETAILED);
    }
    
    /**
     * Install help tooltip with keyboard shortcut
     */
    public static void installHelp(Node node, String title, String description, String shortcut) {
        getInstance().installTooltip(node, title, description, shortcut, TooltipType.HELP);
    }
    
    /**
     * Install warning tooltip
     */
    public static void installWarning(Node node, String title, String warning) {
        getInstance().installTooltip(node, title, warning, null, TooltipType.WARNING);
    }
    
    /**
     * Install error tooltip
     */
    public static void installError(Node node, String title, String error) {
        getInstance().installTooltip(node, title, error, null, TooltipType.ERROR);
    }
    
    /**
     * Install success tooltip
     */
    public static void installSuccess(Node node, String title, String message) {
        getInstance().installTooltip(node, title, message, null, TooltipType.SUCCESS);
    }
    
    /**
     * Remove tooltip from node
     */
    public static void remove(Node node) {
        getInstance().removeTooltip(node);
    }
    
    /**
     * Update tooltip text
     */
    public static void updateText(Node node, String newText) {
        getInstance().updateTooltipText(node, newText, null);
    }
    
    /**
     * Update tooltip with title and description
     */
    public static void updateText(Node node, String newTitle, String newDescription) {
        getInstance().updateTooltipText(node, newTitle, newDescription);
    }
    
    // Private implementation methods
    
    private void installTooltip(Node node, String title, String description, String shortcut, TooltipType type) {
        // Remove existing tooltip if any
        removeTooltip(node);
        
        // Create enhanced tooltip
        EnhancedTooltip tooltip = new EnhancedTooltip(title, description, shortcut, type);
        tooltips.put(node, tooltip);
        
        // Install event handlers
        node.setOnMouseEntered(e -> showTooltip(node, tooltip, e.getScreenX(), e.getScreenY()));
        node.setOnMouseExited(e -> hideTooltip(tooltip));
        node.setOnMouseMoved(e -> updateTooltipPosition(tooltip, e.getScreenX(), e.getScreenY()));
    }
    
    private void removeTooltip(Node node) {
        EnhancedTooltip existing = tooltips.remove(node);
        if (existing != null) {
            existing.hide();
            node.setOnMouseEntered(null);
            node.setOnMouseExited(null);
            node.setOnMouseMoved(null);
        }
    }
    
    private void updateTooltipText(Node node, String newTitle, String newDescription) {
        EnhancedTooltip tooltip = tooltips.get(node);
        if (tooltip != null) {
            tooltip.updateContent(newTitle, newDescription);
        }
    }
    
    private void showTooltip(Node node, EnhancedTooltip tooltip, double x, double y) {
        // Delay showing tooltip
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(showDelay);
        delay.setOnFinished(e -> {
            Window window = node.getScene().getWindow();
            tooltip.show(window, x + 10, y + 10);
        });
        delay.play();
    }
    
    private void hideTooltip(EnhancedTooltip tooltip) {
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(hideDelay);
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }
    
    private void updateTooltipPosition(EnhancedTooltip tooltip, double x, double y) {
        if (tooltip.isShowing()) {
            tooltip.setX(x + 10);
            tooltip.setY(y + 10);
        }
    }
    
    // Configuration methods
    
    public void setShowDelay(Duration delay) {
        this.showDelay = delay;
    }
    
    public void setHideDelay(Duration delay) {
        this.hideDelay = delay;
    }
    
    public void setFadeInDuration(Duration duration) {
        this.fadeInDuration = duration;
    }
    
    public void setFadeOutDuration(Duration duration) {
        this.fadeOutDuration = duration;
    }
    
    /**
     * Enhanced tooltip with rich content
     */
    private class EnhancedTooltip extends Popup {
        
        private VBox content;
        private Label titleLabel;
        private Label descriptionLabel;
        private Label shortcutLabel;
        private TooltipType type;
        
        public EnhancedTooltip(String title, String description, String shortcut, TooltipType type) {
            this.type = type;
            
            createContent(title, description, shortcut);
            setupStyling();
            setupAnimations();
        }
        
        private void createContent(String title, String description, String shortcut) {
            content = new VBox(4);
            content.getStyleClass().add("enhanced-tooltip");
            content.getStyleClass().add("tooltip-" + type.name().toLowerCase());
            content.setPadding(new Insets(8, 12, 8, 12));
            content.setMaxWidth(300);
            
            // Title
            if (title != null && !title.isEmpty()) {
                titleLabel = new Label(title);
                titleLabel.getStyleClass().add("tooltip-title");
                titleLabel.setWrapText(true);
                content.getChildren().add(titleLabel);
            }
            
            // Description
            if (description != null && !description.isEmpty()) {
                descriptionLabel = new Label(description);
                descriptionLabel.getStyleClass().add("tooltip-description");
                descriptionLabel.setWrapText(true);
                content.getChildren().add(descriptionLabel);
            }
            
            // Keyboard shortcut
            if (shortcut != null && !shortcut.isEmpty()) {
                HBox shortcutContainer = new HBox(4);
                shortcutContainer.setAlignment(Pos.CENTER_LEFT);
                
                Label shortcutPrefix = new Label("Shortcut:");
                shortcutPrefix.getStyleClass().add("tooltip-shortcut-prefix");
                
                shortcutLabel = new Label(shortcut);
                shortcutLabel.getStyleClass().add("tooltip-shortcut");
                
                shortcutContainer.getChildren().addAll(shortcutPrefix, shortcutLabel);
                content.getChildren().add(shortcutContainer);
            }
            
            // Add type-specific icon
            addTypeIcon();
            
            getContent().add(content);
        }
        
        private void addTypeIcon() {
            String icon = getIconForType(type);
            if (icon != null) {
                Label iconLabel = new Label(icon);
                iconLabel.getStyleClass().add("tooltip-icon");
                
                // Insert icon at the beginning
                if (titleLabel != null) {
                    HBox titleContainer = new HBox(6);
                    titleContainer.setAlignment(Pos.CENTER_LEFT);
                    
                    content.getChildren().remove(titleLabel);
                    titleContainer.getChildren().addAll(iconLabel, titleLabel);
                    content.getChildren().add(0, titleContainer);
                } else {
                    content.getChildren().add(0, iconLabel);
                }
            }
        }
        
        private String getIconForType(TooltipType type) {
            switch (type) {
                case HELP:
                    return "❓";
                case WARNING:
                    return "⚠️";
                case ERROR:
                    return "❌";
                case SUCCESS:
                    return "✅";
                case DETAILED:
                    return "ℹ️";
                default:
                    return null;
            }
        }
        
        private void setupStyling() {
            content.setStyle(
                "-fx-background-color: rgba(45, 45, 45, 0.95);" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.1);" +
                "-fx-border-radius: 6px;" +
                "-fx-border-width: 1px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);"
            );
        }
        
        private void setupAnimations() {
            // Fade in animation
            content.setOpacity(0);
            
            setOnShowing(e -> {
                FadeTransition fadeIn = new FadeTransition(fadeInDuration, content);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            
            // Fade out animation
            setOnHiding(e -> {
                FadeTransition fadeOut = new FadeTransition(fadeOutDuration, content);
                fadeOut.setFromValue(content.getOpacity());
                fadeOut.setToValue(0);
                fadeOut.play();
            });
        }
        
        public void updateContent(String newTitle, String newDescription) {
            if (titleLabel != null && newTitle != null) {
                titleLabel.setText(newTitle);
            }
            
            if (descriptionLabel != null && newDescription != null) {
                descriptionLabel.setText(newDescription);
            }
        }
    }
    
    /**
     * Tooltip types for different styling
     */
    public enum TooltipType {
        SIMPLE,
        DETAILED,
        HELP,
        WARNING,
        ERROR,
        SUCCESS
    }
}