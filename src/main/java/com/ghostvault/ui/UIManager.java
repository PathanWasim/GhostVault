package com.ghostvault.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Comprehensive UI manager for consistent styling and theme management
 * Provides centralized control over application appearance and user experience
 */
public class UIManager {
    
    private static UIManager instance;
    private Theme currentTheme;
    private final Map<String, String> styleSheets;
    private final Preferences preferences;
    private Stage primaryStage;
    
    // Theme definitions
    public enum Theme {
        LIGHT("Light", "/styles/light-theme.css", "#f4f4f4", "#2c3e50"),
        DARK("Dark", "/styles/dark-theme.css", "#2b2b2b", "#ecf0f1"),
        HIGH_CONTRAST("High Contrast", "/styles/high-contrast-theme.css", "#000000", "#ffffff"),
        STEALTH("Stealth", "/styles/stealth-theme.css", "#1a1a1a", "#00ff00");
        
        private final String displayName;
        private final String cssFile;
        private final String backgroundColor;
        private final String textColor;
        
        Theme(String displayName, String cssFile, String backgroundColor, String textColor) {
            this.displayName = displayName;
            this.cssFile = cssFile;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }
        
        public String getDisplayName() { return displayName; }
        public String getCssFile() { return cssFile; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getTextColor() { return textColor; }
    }
    
    private UIManager() {
        this.styleSheets = new HashMap<>();
        this.preferences = Preferences.userNodeForPackage(UIManager.class);
        this.currentTheme = Theme.valueOf(preferences.get("theme", Theme.LIGHT.name()));
        
        // Initialize CSS resources
        initializeStyleSheets();
    }
    
    /**
     * Get singleton instance
     */
    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager();
        }
        return instance;
    }
    
    /**
     * Initialize the UI manager with primary stage
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        applyTheme(currentTheme);
    }
    
    /**
     * Apply theme to the application
     */
    public void applyTheme(Theme theme) {
        this.currentTheme = theme;
        preferences.put("theme", theme.name());
        
        if (primaryStage != null && primaryStage.getScene() != null) {
            Scene scene = primaryStage.getScene();
            scene.getStylesheets().clear();
            
            // Add base styles
            scene.getStylesheets().add(getClass().getResource("/styles/base.css").toExternalForm());
            
            // Add theme-specific styles
            String themeCSS = styleSheets.get(theme.name());
            if (themeCSS != null) {
                scene.getStylesheets().add(themeCSS);
            }
            
            // Apply theme-specific properties
            scene.getRoot().setStyle(
                "-fx-background-color: " + theme.getBackgroundColor() + ";" +
                "-fx-text-fill: " + theme.getTextColor() + ";"
            );
        }
    }
    
    /**
     * Get current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Initialize CSS stylesheets
     */
    private void initializeStyleSheets() {
        // Create CSS content for each theme
        createLightThemeCSS();
        createDarkThemeCSS();
        createHighContrastThemeCSS();
        createStealthThemeCSS();
        createBaseCSS();
    }
    
    /**
     * Create base CSS styles
     */
    private void createBaseCSS() {
        String baseCSS = 
            "/* Base styles for GhostVault */\n" +
            ".root {\n" +
            "    -fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;\n" +
            "    -fx-font-size: 12px;\n" +
            "}\n" +
            "\n" +
            "/* Button styles */\n" +
            ".button {\n" +
            "    -fx-background-radius: 4px;\n" +
            "    -fx-border-radius: 4px;\n" +
            "    -fx-padding: 8px 16px;\n" +
            "    -fx-cursor: hand;\n" +
            "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);\n" +
            "}\n" +
            "\n" +
            ".button:hover {\n" +
            "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);\n" +
            "}\n" +
            "\n" +
            ".button:pressed {\n" +
            "    -fx-effect: innershadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);\n" +
            "}\n" +
            "\n" +
            "/* Primary button */\n" +
            ".button-primary {\n" +
            "    -fx-background-color: #3498db;\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".button-primary:hover {\n" +
            "    -fx-background-color: #2980b9;\n" +
            "}\n" +
            "\n" +
            "/* Danger button */\n" +
            ".button-danger {\n" +
            "    -fx-background-color: #e74c3c;\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".button-danger:hover {\n" +
            "    -fx-background-color: #c0392b;\n" +
            "}\n" +
            "\n" +
            "/* Success button */\n" +
            ".button-success {\n" +
            "    -fx-background-color: #27ae60;\n" +
            "    -fx-text-fill: white;\n" +
            "    -fx-font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".button-success:hover {\n" +
            "    -fx-background-color: #229954;\n" +
            "}\n" +
            "\n" +
            "/* Text field styles */\n" +
            ".text-field, .password-field {\n" +
            "    -fx-background-radius: 4px;\n" +
            "    -fx-border-radius: 4px;\n" +
            "    -fx-padding: 8px 12px;\n" +
            "    -fx-border-width: 1px;\n" +
            "}\n" +
            "\n" +
            ".text-field:focused, .password-field:focused {\n" +
            "    -fx-border-width: 2px;\n" +
            "    -fx-border-color: #3498db;\n" +
            "}\n" +
            "\n" +
            "/* Progress indicators */\n" +
            ".progress-bar {\n" +
            "    -fx-background-radius: 10px;\n" +
            "}\n" +
            "\n" +
            ".progress-bar .bar {\n" +
            "    -fx-background-radius: 10px;\n" +
            "    -fx-background-color: #3498db;\n" +
            "}\n" +
            "\n" +
            "/* Table styles */\n" +
            ".table-view {\n" +
            "    -fx-background-radius: 4px;\n" +
            "    -fx-border-radius: 4px;\n" +
            "    -fx-border-width: 1px;\n" +
            "}\n" +
            "\n" +
            ".table-row-cell:selected {\n" +
            "    -fx-background-color: #3498db;\n" +
            "    -fx-text-fill: white;\n" +
            "}\n" +
            "\n" +
            "/* Menu styles */\n" +
            ".menu-bar {\n" +
            "    -fx-background-radius: 0;\n" +
            "    -fx-border-width: 0 0 1px 0;\n" +
            "}\n" +
            "\n" +
            "/* Dialog styles */\n" +
            ".dialog-pane {\n" +
            "    -fx-background-radius: 8px;\n" +
            "}\n" +
            "\n" +
            "/* Tooltip styles */\n" +
            ".tooltip {\n" +
            "    -fx-background-radius: 4px;\n" +
            "    -fx-font-size: 11px;\n" +
            "    -fx-padding: 4px 8px;\n" +
            "}";
        
        // Save to temporary file or use in-memory
        styleSheets.put("BASE", "data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(baseCSS.getBytes()));
    }
    
    /**
     * Create light theme CSS
     */
    private void createLightThemeCSS() {
        String lightCSS = 
            "/* Light Theme */\n" +
            ".root {\n" +
            "    -fx-base: #f4f4f4;\n" +
            "    -fx-background: #ffffff;\n" +
            "    -fx-control-inner-background: #ffffff;\n" +
            "    -fx-accent: #3498db;\n" +
            "    -fx-default-button: #3498db;\n" +
            "    -fx-focus-color: #3498db;\n" +
            "    -fx-text-fill: #2c3e50;\n" +
            "}\n" +
            "\n" +
            ".button {\n" +
            "    -fx-background-color: #ecf0f1;\n" +
            "    -fx-text-fill: #2c3e50;\n" +
            "    -fx-border-color: #bdc3c7;\n" +
            "}\n" +
            "\n" +
            ".text-field, .password-field {\n" +
            "    -fx-background-color: #ffffff;\n" +
            "    -fx-text-fill: #2c3e50;\n" +
            "    -fx-border-color: #bdc3c7;\n" +
            "}\n" +
            "\n" +
            ".table-view {\n" +
            "    -fx-background-color: #ffffff;\n" +
            "    -fx-border-color: #bdc3c7;\n" +
            "}\n" +
            "\n" +
            ".menu-bar {\n" +
            "    -fx-background-color: #ecf0f1;\n" +
            "    -fx-border-color: #bdc3c7;\n" +
            "}";
        
        styleSheets.put(Theme.LIGHT.name(), "data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(lightCSS.getBytes()));
    }
    
    /**
     * Create dark theme CSS
     */
    private void createDarkThemeCSS() {
        String darkCSS = 
            "/* Dark Theme */\n" +
            ".root {\n" +
            "    -fx-base: #2b2b2b;\n" +
            "    -fx-background: #1e1e1e;\n" +
            "    -fx-control-inner-background: #3c3c3c;\n" +
            "    -fx-accent: #0078d4;\n" +
            "    -fx-default-button: #0078d4;\n" +
            "    -fx-focus-color: #0078d4;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "}\n" +
            "\n" +
            ".button {\n" +
            "    -fx-background-color: #404040;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "    -fx-border-color: #555555;\n" +
            "}\n" +
            "\n" +
            ".button:hover {\n" +
            "    -fx-background-color: #4a4a4a;\n" +
            "}\n" +
            "\n" +
            ".text-field, .password-field {\n" +
            "    -fx-background-color: #3c3c3c;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "    -fx-border-color: #555555;\n" +
            "}\n" +
            "\n" +
            ".table-view {\n" +
            "    -fx-background-color: #2b2b2b;\n" +
            "    -fx-border-color: #555555;\n" +
            "}\n" +
            "\n" +
            ".table-row-cell {\n" +
            "    -fx-background-color: #2b2b2b;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "}\n" +
            "\n" +
            ".table-row-cell:odd {\n" +
            "    -fx-background-color: #333333;\n" +
            "}\n" +
            "\n" +
            ".menu-bar {\n" +
            "    -fx-background-color: #2b2b2b;\n" +
            "    -fx-border-color: #555555;\n" +
            "}\n" +
            "\n" +
            ".label {\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "}";
        
        styleSheets.put(Theme.DARK.name(), "data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(darkCSS.getBytes()));
    }
    
    /**
     * Create high contrast theme CSS
     */
    private void createHighContrastThemeCSS() {
        String highContrastCSS = 
            "/* High Contrast Theme */\n" +
            ".root {\n" +
            "    -fx-base: #000000;\n" +
            "    -fx-background: #000000;\n" +
            "    -fx-control-inner-background: #000000;\n" +
            "    -fx-accent: #ffffff;\n" +
            "    -fx-default-button: #ffffff;\n" +
            "    -fx-focus-color: #ffff00;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "}\n" +
            "\n" +
            ".button {\n" +
            "    -fx-background-color: #000000;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "    -fx-border-color: #ffffff;\n" +
            "    -fx-border-width: 2px;\n" +
            "}\n" +
            "\n" +
            ".button:hover {\n" +
            "    -fx-background-color: #ffffff;\n" +
            "    -fx-text-fill: #000000;\n" +
            "}\n" +
            "\n" +
            ".button:focused {\n" +
            "    -fx-border-color: #ffff00;\n" +
            "    -fx-border-width: 3px;\n" +
            "}\n" +
            "\n" +
            ".text-field, .password-field {\n" +
            "    -fx-background-color: #000000;\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "    -fx-border-color: #ffffff;\n" +
            "    -fx-border-width: 2px;\n" +
            "}\n" +
            "\n" +
            ".text-field:focused, .password-field:focused {\n" +
            "    -fx-border-color: #ffff00;\n" +
            "    -fx-border-width: 3px;\n" +
            "}\n" +
            "\n" +
            ".table-view {\n" +
            "    -fx-background-color: #000000;\n" +
            "    -fx-border-color: #ffffff;\n" +
            "    -fx-border-width: 2px;\n" +
            "}\n" +
            "\n" +
            ".label {\n" +
            "    -fx-text-fill: #ffffff;\n" +
            "    -fx-font-weight: bold;\n" +
            "}";
        
        styleSheets.put(Theme.HIGH_CONTRAST.name(), "data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(highContrastCSS.getBytes()));
    }
    
    /**
     * Create stealth theme CSS
     */
    private void createStealthThemeCSS() {
        String stealthCSS = 
            "/* Stealth Theme */\n" +
            ".root {\n" +
            "    -fx-base: #0a0a0a;\n" +
            "    -fx-background: #000000;\n" +
            "    -fx-control-inner-background: #1a1a1a;\n" +
            "    -fx-accent: #00ff00;\n" +
            "    -fx-default-button: #00ff00;\n" +
            "    -fx-focus-color: #00ff00;\n" +
            "    -fx-text-fill: #00ff00;\n" +
            "}\n" +
            "\n" +
            ".button {\n" +
            "    -fx-background-color: #1a1a1a;\n" +
            "    -fx-text-fill: #00ff00;\n" +
            "    -fx-border-color: #00ff00;\n" +
            "    -fx-font-family: 'Courier New', monospace;\n" +
            "}\n" +
            "\n" +
            ".button:hover {\n" +
            "    -fx-background-color: #003300;\n" +
            "    -fx-effect: dropshadow(gaussian, #00ff00, 5, 0, 0, 0);\n" +
            "}\n" +
            "\n" +
            ".text-field, .password-field {\n" +
            "    -fx-background-color: #000000;\n" +
            "    -fx-text-fill: #00ff00;\n" +
            "    -fx-border-color: #00ff00;\n" +
            "    -fx-font-family: 'Courier New', monospace;\n" +
            "}\n" +
            "\n" +
            ".label {\n" +
            "    -fx-text-fill: #00ff00;\n" +
            "    -fx-font-family: 'Courier New', monospace;\n" +
            "}\n" +
            "\n" +
            ".table-view {\n" +
            "    -fx-background-color: #000000;\n" +
            "    -fx-border-color: #00ff00;\n" +
            "}\n" +
            "\n" +
            ".progress-bar .bar {\n" +
            "    -fx-background-color: #00ff00;\n" +
            "    -fx-effect: dropshadow(gaussian, #00ff00, 3, 0, 0, 0);\n" +
            "}";
        
        styleSheets.put(Theme.STEALTH.name(), "data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(stealthCSS.getBytes()));
    }
    
    /**
     * Apply smooth theme transition
     */
    public void applyThemeWithTransition(Theme newTheme) {
        if (primaryStage != null && primaryStage.getScene() != null) {
            Scene scene = primaryStage.getScene();
            
            // Create fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), scene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.3);
            
            fadeOut.setOnFinished(e -> {
                applyTheme(newTheme);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), scene.getRoot());
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
            fadeOut.play();
        } else {
            applyTheme(newTheme);
        }
    }
    
    /**
     * Add responsive feedback to a button
     */
    public void addButtonFeedback(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        button.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(50), button);
            scale.setToX(0.95);
            scale.setToY(0.95);
            scale.play();
        });
        
        button.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(50), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
    }
    
    /**
     * Add progress feedback to long-running operations
     */
    public ProgressDialog createProgressDialog(String title, String message) {
        return new ProgressDialog(primaryStage, title, message);
    }
    
    /**
     * Show loading indicator
     */
    public void showLoadingIndicator(Region parent, String message) {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(50, 50);
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("loading-message");
        
        // Add to parent with overlay
        // Implementation would depend on parent layout
    }
    
    /**
     * Apply consistent styling to a node
     */
    public void styleNode(Node node, String... styleClasses) {
        node.getStyleClass().addAll(styleClasses);
    }
    
    /**
     * Create styled button with consistent appearance
     */
    public Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        addButtonFeedback(button);
        return button;
    }
    
    /**
     * Create primary action button
     */
    public Button createPrimaryButton(String text) {
        return createStyledButton(text, "button-primary");
    }
    
    /**
     * Create danger action button
     */
    public Button createDangerButton(String text) {
        return createStyledButton(text, "button-danger");
    }
    
    /**
     * Create success action button
     */
    public Button createSuccessButton(String text) {
        return createStyledButton(text, "button-success");
    }
    
    /**
     * Add fade-in animation to node
     */
    public void fadeIn(Node node) {
        fadeIn(node, Duration.millis(300));
    }
    
    /**
     * Add fade-in animation with custom duration
     */
    public void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    /**
     * Add fade-out animation to node
     */
    public void fadeOut(Node node) {
        fadeOut(node, Duration.millis(300));
    }
    
    /**
     * Add fade-out animation with custom duration
     */
    public void fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();
    }
    
    /**
     * Get available themes
     */
    public Theme[] getAvailableThemes() {
        return Theme.values();
    }
    
    /**
     * Check if current theme is dark
     */
    public boolean isDarkTheme() {
        return currentTheme == Theme.DARK || currentTheme == Theme.STEALTH || currentTheme == Theme.HIGH_CONTRAST;
    }
    
    /**
     * Get theme-appropriate icon color
     */
    public String getIconColor() {
        return isDarkTheme() ? "#ffffff" : "#2c3e50";
    }
    
    /**
     * Get theme-appropriate accent color
     */
    public String getAccentColor() {
        switch (currentTheme) {
            case LIGHT:
            case DARK:
                return "#3498db";
            case HIGH_CONTRAST:
                return "#ffffff";
            case STEALTH:
                return "#00ff00";
            default:
                return "#3498db";
        }
    }
}