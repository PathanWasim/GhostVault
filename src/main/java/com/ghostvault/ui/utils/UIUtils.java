package com.ghostvault.ui.utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for common UI operations and styling
 */
public class UIUtils {
    
    // Color constants
    public static final String PRIMARY_COLOR = "#2c3e50";
    public static final String SECONDARY_COLOR = "#3498db";
    public static final String SUCCESS_COLOR = "#27ae60";
    public static final String WARNING_COLOR = "#f39c12";
    public static final String ERROR_COLOR = "#e74c3c";
    public static final String BACKGROUND_COLOR = "#ecf0f1";
    public static final String CARD_COLOR = "#ffffff";
    public static final String TEXT_COLOR = "#2c3e50";
    public static final String MUTED_TEXT_COLOR = "#7f8c8d";
    
    // Animation durations
    public static final Duration FAST_ANIMATION = Duration.millis(150);
    public static final Duration NORMAL_ANIMATION = Duration.millis(300);
    public static final Duration SLOW_ANIMATION = Duration.millis(500);
    
    /**
     * Apply modern card styling to a node
     */
    public static void applyCardStyle(Node node) {
        node.setStyle(
            "-fx-background-color: " + CARD_COLOR + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
        );
    }
    
    /**
     * Apply primary button styling
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #2980b9;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
    }
    
    /**
     * Apply secondary button styling
     */
    public static void applySecondaryButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + SECONDARY_COLOR + ";" +
            "-fx-border-color: " + SECONDARY_COLOR + ";" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-color: " + SECONDARY_COLOR + ";" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + SECONDARY_COLOR + ";" +
            "-fx-border-color: " + SECONDARY_COLOR + ";" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
    }
    
    /**
     * Apply danger button styling
     */
    public static void applyDangerButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: " + ERROR_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #c0392b;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + ERROR_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        ));
    }
    
    /**
     * Create a modern text field with styling
     */
    public static TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 8px;" +
            "-fx-font-size: 14px;"
        );
        
        // Focus effects
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                textField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: " + SECONDARY_COLOR + ";" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 4px;" +
                    "-fx-background-radius: 4px;" +
                    "-fx-padding: 7px;" +
                    "-fx-font-size: 14px;"
                );
            } else {
                textField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #ddd;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 4px;" +
                    "-fx-background-radius: 4px;" +
                    "-fx-padding: 8px;" +
                    "-fx-font-size: 14px;"
                );
            }
        });
        
        return textField;
    }
    
    /**
     * Create a styled password field
     */
    public static PasswordField createStyledPasswordField(String promptText) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 8px;" +
            "-fx-font-size: 14px;"
        );
        
        // Focus effects
        passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                passwordField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: " + SECONDARY_COLOR + ";" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 4px;" +
                    "-fx-background-radius: 4px;" +
                    "-fx-padding: 7px;" +
                    "-fx-font-size: 14px;"
                );
            } else {
                passwordField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #ddd;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 4px;" +
                    "-fx-background-radius: 4px;" +
                    "-fx-padding: 8px;" +
                    "-fx-font-size: 14px;"
                );
            }
        });
        
        return passwordField;
    }
    
    /**
     * Create a section header label
     */
    public static Label createSectionHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        label.setTextFill(Color.web(PRIMARY_COLOR));
        label.setPadding(new Insets(10, 0, 5, 0));
        return label;
    }
    
    /**
     * Create a subtitle label
     */
    public static Label createSubtitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));
        label.setTextFill(Color.web(MUTED_TEXT_COLOR));
        return label;
    }
    
    /**
     * Create a status badge
     */
    public static Label createStatusBadge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        return badge;
    }
    
    /**
     * Format file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return new DecimalFormat("#.#").format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0)) + " MB";
        return new DecimalFormat("#.#").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }
    
    /**
     * Format timestamp for display
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    /**
     * Get file extension
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show error dialog
     */
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show information dialog
     */
    public static void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Create fade in animation
     */
    public static FadeTransition createFadeIn(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        return fade;
    }
    
    /**
     * Create fade out animation
     */
    public static FadeTransition createFadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        return fade;
    }
    
    /**
     * Create slide in animation
     */
    public static TranslateTransition createSlideIn(Node node, Duration duration, double fromX, double fromY) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(fromX);
        slide.setFromY(fromY);
        slide.setToX(0);
        slide.setToY(0);
        return slide;
    }
    
    /**
     * Create scale animation
     */
    public static ScaleTransition createScaleAnimation(Node node, Duration duration, double fromScale, double toScale) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(fromScale);
        scale.setFromY(fromScale);
        scale.setToX(toScale);
        scale.setToY(toScale);
        return scale;
    }
    
    /**
     * Add hover effect to node
     */
    public static void addHoverEffect(Node node) {
        ScaleTransition scaleUp = createScaleAnimation(node, FAST_ANIMATION, 1.0, 1.05);
        ScaleTransition scaleDown = createScaleAnimation(node, FAST_ANIMATION, 1.05, 1.0);
        
        node.setOnMouseEntered(e -> scaleUp.play());
        node.setOnMouseExited(e -> scaleDown.play());
    }
    
    /**
     * Add click effect to node
     */
    public static void addClickEffect(Node node) {
        ScaleTransition scaleDown = createScaleAnimation(node, Duration.millis(100), 1.0, 0.95);
        ScaleTransition scaleUp = createScaleAnimation(node, Duration.millis(100), 0.95, 1.0);
        
        SequentialTransition clickAnimation = new SequentialTransition(scaleDown, scaleUp);
        
        node.setOnMousePressed(e -> clickAnimation.play());
    }
    
    /**
     * Create loading spinner
     */
    public static ProgressIndicator createLoadingSpinner() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(30, 30);
        spinner.setStyle("-fx-accent: " + SECONDARY_COLOR + ";");
        return spinner;
    }
    
    /**
     * Create separator line
     */
    public static Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e0e0e0;");
        return separator;
    }
    
    /**
     * Center stage on screen
     */
    public static void centerStage(javafx.stage.Stage stage) {
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
    
    /**
     * Memory utilities class
     */
    public static class MemoryUtils {
        
        /**
         * Get used memory in bytes
         */
        public static long getUsedMemory() {
            Runtime runtime = Runtime.getRuntime();
            return runtime.totalMemory() - runtime.freeMemory();
        }
        
        /**
         * Get total memory in bytes
         */
        public static long getTotalMemory() {
            return Runtime.getRuntime().totalMemory();
        }
        
        /**
         * Get memory usage percentage
         */
        public static double getMemoryUsagePercentage() {
            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long total = runtime.totalMemory();
            return (double) used / total * 100.0;
        }
        
        /**
         * Format memory size in human readable format
         */
        public static String formatMemorySize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            String pre = "KMGTPE".charAt(exp - 1) + "";
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
        
        /**
         * Format current memory usage
         */
        public static String formatMemoryUsage() {
            long used = getUsedMemory();
            long total = getTotalMemory();
            double percentage = getMemoryUsagePercentage();
            return String.format("Memory: %s / %s (%.1f%%)", 
                formatMemorySize(used), formatMemorySize(total), percentage);
        }
    }
}