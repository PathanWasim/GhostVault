package com.ghostvault.ui.components;

import com.ghostvault.ui.animations.AnimationManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern Theme Manager for GhostVault with theme switching capabilities
 */
public class ModernThemeManager {
    
    private static boolean initialized = false;
    private static String currentTheme = "password-manager";
    private static List<Scene> managedScenes = new ArrayList<>();
    
    // Available themes
    public enum Theme {
        PASSWORD_MANAGER("password-manager", "/css/password-manager-theme.css", "Modern Password Manager"),
        DARK("dark", "/ghostvault-dark.css", "High-Tech Dark"),
        LIGHT("light", "/ghostvault-light.css", "Modern Light"),
        HIGH_CONTRAST("high-contrast", "/styles/high_contrast.css", "High Contrast"),
        PROFESSIONAL("professional", "/styles/professional.css", "Professional");
        
        private final String id;
        private final String cssPath;
        private final String displayName;
        
        Theme(String id, String cssPath, String displayName) {
            this.id = id;
            this.cssPath = cssPath;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getCssPath() { return cssPath; }
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Initialize the theme system
     */
    public static void initialize() {
        if (!initialized) {
            initialized = true;
            System.out.println("🎨 Theme Manager initialized with " + Theme.values().length + " available themes");
        }
    }
    
    /**
     * Apply theme to scene
     */
    public static void applyTheme(Scene scene) {
        applyTheme(scene, getCurrentTheme());
    }
    
    /**
     * Apply specific theme to scene
     */
    public static void applyTheme(Scene scene, Theme theme) {
        if (scene == null) return;
        
        try {
            scene.getStylesheets().clear();
            
            // Add the theme CSS
            String themeUrl = ModernThemeManager.class.getResource(theme.getCssPath()).toExternalForm();
            scene.getStylesheets().add(themeUrl);
            
            // Add common styles if they exist
            try {
                String commonUrl = ModernThemeManager.class.getResource("/styles/common.css").toExternalForm();
                scene.getStylesheets().add(commonUrl);
            } catch (Exception e) {
                // Common styles not found, continue without them
            }
            
            currentTheme = theme.getId();
            
            // Track this scene for future theme changes
            if (!managedScenes.contains(scene)) {
                managedScenes.add(scene);
            }
            
            System.out.println("✅ Applied theme: " + theme.getDisplayName() + " to scene");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to apply theme " + theme.getDisplayName() + ": " + e.getMessage());
            // Fallback to password manager theme
            if (theme != Theme.PASSWORD_MANAGER) {
                applyTheme(scene, Theme.PASSWORD_MANAGER);
            }
        }
    }
    
    /**
     * Switch theme for all managed scenes with animation
     */
    public static void switchTheme(Theme newTheme) {
        if (newTheme == null) return;
        
        System.out.println("🔄 Switching to theme: " + newTheme.getDisplayName());
        
        for (Scene scene : managedScenes) {
            if (scene != null && scene.getRoot() != null) {
                // Animate theme transition
                AnimationManager.fadeOut(scene.getRoot(), AnimationManager.FAST, () -> {
                    Platform.runLater(() -> {
                        applyTheme(scene, newTheme);
                        AnimationManager.fadeIn(scene.getRoot(), AnimationManager.FAST, null);
                    });
                });
            }
        }
        
        currentTheme = newTheme.getId();
        
        // Show notification about theme change
        NotificationSystem.showInfo("Theme Changed", 
            "Switched to " + newTheme.getDisplayName() + " theme");
    }
    
    /**
     * Get current theme
     */
    public static Theme getCurrentTheme() {
        for (Theme theme : Theme.values()) {
            if (theme.getId().equals(currentTheme)) {
                return theme;
            }
        }
        return Theme.PASSWORD_MANAGER; // Default fallback
    }
    
    /**
     * Get all available themes
     */
    public static Theme[] getAvailableThemes() {
        return Theme.values();
    }
    
    /**
     * Cycle to next theme
     */
    public static void cycleTheme() {
        Theme[] themes = Theme.values();
        Theme current = getCurrentTheme();
        
        int currentIndex = 0;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i] == current) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % themes.length;
        switchTheme(themes[nextIndex]);
    }
    
    /**
     * Set accent color (for themes that support it)
     */
    public static void setAccentColor(String color) {
        // This would require CSS variable support or dynamic style injection
        System.out.println("🎨 Accent color change requested: " + color);
        // Implementation would depend on the specific theme system
    }
    
    /**
     * Set background color (for themes that support it)
     */
    public static void setBackgroundColor(String color) {
        System.out.println("🎨 Background color change requested: " + color);
        // Implementation would depend on the specific theme system
    }
    
    /**
     * Set text color (for themes that support it)
     */
    public static void setTextColor(String color) {
        System.out.println("🎨 Text color change requested: " + color);
        // Implementation would depend on the specific theme system
    }
    
    /**
     * Register a scene for theme management
     */
    public static void registerScene(Scene scene) {
        if (scene != null && !managedScenes.contains(scene)) {
            managedScenes.add(scene);
            applyTheme(scene);
        }
    }
    
    /**
     * Unregister a scene from theme management
     */
    public static void unregisterScene(Scene scene) {
        managedScenes.remove(scene);
    }
    
    /**
     * Clean up managed scenes (remove null references)
     */
    public static void cleanup() {
        managedScenes.removeIf(scene -> scene == null || scene.getWindow() == null);
    }
    
    /**
     * Apply theme with custom CSS additions
     */
    public static void applyThemeWithCustomCSS(Scene scene, Theme theme, String customCSS) {
        applyTheme(scene, theme);
        
        if (customCSS != null && !customCSS.trim().isEmpty()) {
            // Create a data URL for the custom CSS
            String dataUrl = "data:text/css;base64," + 
                java.util.Base64.getEncoder().encodeToString(customCSS.getBytes());
            scene.getStylesheets().add(dataUrl);
        }
    }
    
    /**
     * Theme enumeration with metadata
     */
    public static class ThemeInfo {
        private final Theme theme;
        private final boolean isDark;
        private final String primaryColor;
        
        public ThemeInfo(Theme theme, boolean isDark, String primaryColor) {
            this.theme = theme;
            this.isDark = isDark;
            this.primaryColor = primaryColor;
        }
        
        public Theme getTheme() { return theme; }
        public boolean isDark() { return isDark; }
        public String getPrimaryColor() { return primaryColor; }
    }
    
    /**
     * Get theme information
     */
    public static ThemeInfo getThemeInfo(Theme theme) {
        switch (theme) {
            case PASSWORD_MANAGER:
                return new ThemeInfo(theme, true, "#175DDC");
            case DARK:
                return new ThemeInfo(theme, true, "#00d4ff");
            case LIGHT:
                return new ThemeInfo(theme, false, "#3b82f6");
            case HIGH_CONTRAST:
                return new ThemeInfo(theme, true, "#ffffff");
            case PROFESSIONAL:
                return new ThemeInfo(theme, false, "#2563eb");
            default:
                return new ThemeInfo(theme, true, "#175DDC");
        }
    }
}