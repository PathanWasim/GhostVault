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

/**
 * Predefined tooltips for common UI elements
 */
public class CommonTooltips {
    
    // File operations
    public static final String UPLOAD_FILES = "Upload files to the vault";
    public static final String UPLOAD_FILES_DESC = "Select one or more files from your computer to securely store in the vault. Supported formats include documents, images, videos, and archives.";
    public static final String UPLOAD_SHORTCUT = "Ctrl+U";
    
    public static final String DOWNLOAD_FILES = "Download selected files";
    public static final String DOWNLOAD_FILES_DESC = "Download the selected files to your computer. Files will be decrypted during the download process.";
    public static final String DOWNLOAD_SHORTCUT = "Ctrl+D";
    
    public static final String DELETE_FILES = "Delete selected files";
    public static final String DELETE_FILES_DESC = "Permanently delete the selected files from the vault. This action cannot be undone.";
    public static final String DELETE_SHORTCUT = "Delete";
    
    public static final String SECURE_DELETE = "Secure delete";
    public static final String SECURE_DELETE_DESC = "Securely overwrite file data multiple times before deletion to prevent recovery. Use for sensitive files.";
    
    // Search and navigation
    public static final String SEARCH_FILES = "Search files and folders";
    public static final String SEARCH_FILES_DESC = "Search for files by name, content, or metadata. Use filters to narrow down results by file type, size, or date.";
    public static final String SEARCH_SHORTCUT = "Ctrl+F";
    
    public static final String FILTER_FILES = "Filter files";
    public static final String FILTER_FILES_DESC = "Apply filters to show only specific types of files, sizes, or date ranges.";
    
    public static final String SORT_FILES = "Sort files";
    public static final String SORT_FILES_DESC = "Change the sorting order of files by name, size, date modified, or file type.";
    
    // Security features
    public static final String MASTER_MODE = "Master Mode";
    public static final String MASTER_MODE_DESC = "Full access to all vault features and real data. Use your master password to access this mode.";
    
    public static final String PANIC_MODE = "Panic Mode";
    public static final String PANIC_MODE_DESC = "Emergency data destruction mode. Activates when panic password is entered. All vault data will be permanently destroyed.";
    
    public static final String DECOY_MODE = "Decoy Mode";
    public static final String DECOY_MODE_DESC = "Fake vault with dummy data. Provides plausible deniability by showing convincing but fake files and folders.";
    
    public static final String BACKUP_VAULT = "Create encrypted backup";
    public static final String BACKUP_VAULT_DESC = "Create a secure, encrypted backup of your vault data. Backups are compressed and can be restored later.";
    public static final String BACKUP_SHORTCUT = "Ctrl+B";
    
    public static final String RESTORE_VAULT = "Restore from backup";
    public static final String RESTORE_VAULT_DESC = "Restore vault data from a previously created encrypted backup file.";
    public static final String RESTORE_SHORTCUT = "Ctrl+R";
    
    // Preview and viewing
    public static final String PREVIEW_FILE = "Preview file";
    public static final String PREVIEW_FILE_DESC = "View file contents without downloading. Supports images, videos, audio, documents, and code files.";
    public static final String PREVIEW_SHORTCUT = "Space";
    
    public static final String ZOOM_IN = "Zoom in";
    public static final String ZOOM_IN_DESC = "Increase the zoom level of the preview.";
    public static final String ZOOM_IN_SHORTCUT = "Ctrl++";
    
    public static final String ZOOM_OUT = "Zoom out";
    public static final String ZOOM_OUT_DESC = "Decrease the zoom level of the preview.";
    public static final String ZOOM_OUT_SHORTCUT = "Ctrl+-";
    
    public static final String FIT_TO_WINDOW = "Fit to window";
    public static final String FIT_TO_WINDOW_DESC = "Resize the preview to fit within the available space.";
    public static final String FIT_TO_WINDOW_SHORTCUT = "Ctrl+0";
    
    // Batch operations
    public static final String SELECT_ALL = "Select all files";
    public static final String SELECT_ALL_DESC = "Select all visible files in the current folder for batch operations.";
    public static final String SELECT_ALL_SHORTCUT = "Ctrl+A";
    
    public static final String DESELECT_ALL = "Deselect all files";
    public static final String DESELECT_ALL_DESC = "Clear the current file selection.";
    public static final String DESELECT_ALL_SHORTCUT = "Ctrl+D";
    
    public static final String BATCH_DOWNLOAD = "Download selected files";
    public static final String BATCH_DOWNLOAD_DESC = "Download all selected files as a single operation. Large selections may be compressed into an archive.";
    
    public static final String BATCH_DELETE = "Delete selected files";
    public static final String BATCH_DELETE_DESC = "Delete all selected files. You will be prompted for confirmation before deletion.";
    
    // Settings and configuration
    public static final String SETTINGS = "Application settings";
    public static final String SETTINGS_DESC = "Configure vault settings, security options, and user preferences.";
    public static final String SETTINGS_SHORTCUT = "Ctrl+,";
    
    public static final String LOGOUT = "Logout";
    public static final String LOGOUT_DESC = "End the current session and return to the login screen. All cached data will be cleared.";
    public static final String LOGOUT_SHORTCUT = "Ctrl+L";
    
    public static final String EXIT = "Exit application";
    public static final String EXIT_DESC = "Close the application completely. All data will be securely cleared from memory.";
    public static final String EXIT_SHORTCUT = "Alt+F4";
    
    // Help and information
    public static final String HELP = "Help and documentation";
    public static final String HELP_DESC = "Access user guides, keyboard shortcuts, and troubleshooting information.";
    public static final String HELP_SHORTCUT = "F1";
    
    public static final String ABOUT = "About GhostVault";
    public static final String ABOUT_DESC = "View application version, license information, and credits.";
    
    // Error and warning messages
    public static final String NETWORK_ERROR = "Network connection error";
    public static final String NETWORK_ERROR_DESC = "Unable to connect to the server. Check your internet connection and try again.";
    
    public static final String PERMISSION_ERROR = "Permission denied";
    public static final String PERMISSION_ERROR_DESC = "You don't have permission to perform this operation. Contact your administrator if needed.";
    
    public static final String DISK_SPACE_WARNING = "Low disk space";
    public static final String DISK_SPACE_WARNING_DESC = "Available disk space is running low. Consider freeing up space or moving files to another location.";
    
    public static final String UNSUPPORTED_FORMAT = "Unsupported file format";
    public static final String UNSUPPORTED_FORMAT_DESC = "This file format is not supported for preview. You can still download and open it with an appropriate application.";
    
    /**
     * Install common tooltips on UI elements
     */
    public static void installCommonTooltips(Node uploadButton, Node downloadButton, Node deleteButton, 
                                           Node searchField, Node settingsButton) {
        
        if (uploadButton != null) {
            TooltipManager.installHelp(uploadButton, UPLOAD_FILES, UPLOAD_FILES_DESC, UPLOAD_SHORTCUT);
        }
        
        if (downloadButton != null) {
            TooltipManager.installHelp(downloadButton, DOWNLOAD_FILES, DOWNLOAD_FILES_DESC, DOWNLOAD_SHORTCUT);
        }
        
        if (deleteButton != null) {
            TooltipManager.installWarning(deleteButton, DELETE_FILES, DELETE_FILES_DESC);
        }
        
        if (searchField != null) {
            TooltipManager.installHelp(searchField, SEARCH_FILES, SEARCH_FILES_DESC, SEARCH_SHORTCUT);
        }
        
        if (settingsButton != null) {
            TooltipManager.installHelp(settingsButton, SETTINGS, SETTINGS_DESC, SETTINGS_SHORTCUT);
        }
    }
}