package com.ghostvault.ui.preview;

/**
 * Settings class for preview components
 * Contains configuration options for media and text preview behavior
 */
public class PreviewSettings {
    
    // Media settings
    private double defaultVolume = 0.7;
    private boolean autoPlayMedia = false;
    private boolean showMediaControls = true;
    private boolean enableFullscreen = true;
    
    // Text settings
    private String fontFamily = "Consolas";
    private int fontSize = 12;
    private boolean enableSyntaxHighlighting = true;
    private boolean enableLineNumbers = true;
    private boolean enableWordWrap = true;
    
    // General settings
    private boolean enableDarkTheme = true;
    private boolean enableSecurityWarnings = true;
    
    // Advanced settings
    private boolean enableMemoryMonitoring = true;
    private boolean logPreviewActivity = false;
    private int maxPreviewSizeMB = 100;
    private int cleanupDelaySeconds = 30;
    private int maxConcurrentPreviews = 5;
    private boolean showLineNumbers = true;
    private boolean wordWrap = true;
    private boolean enableCodeFolding = true;
    
    // Default constructor
    public PreviewSettings() {
        // Use default values
    }
    
    // Media settings getters/setters
    public double getDefaultVolume() { return defaultVolume; }
    public void setDefaultVolume(double defaultVolume) { 
        this.defaultVolume = Math.max(0.0, Math.min(1.0, defaultVolume)); 
    }
    
    public boolean isAutoPlayMedia() { return autoPlayMedia; }
    public void setAutoPlayMedia(boolean autoPlayMedia) { this.autoPlayMedia = autoPlayMedia; }
    
    public boolean isShowMediaControls() { return showMediaControls; }
    public void setShowMediaControls(boolean showMediaControls) { this.showMediaControls = showMediaControls; }
    
    public boolean isEnableFullscreen() { return enableFullscreen; }
    public void setEnableFullscreen(boolean enableFullscreen) { this.enableFullscreen = enableFullscreen; }
    
    // Text settings getters/setters
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { 
        this.fontSize = Math.max(8, Math.min(72, fontSize)); 
    }
    
    public boolean isEnableSyntaxHighlighting() { return enableSyntaxHighlighting; }
    public void setEnableSyntaxHighlighting(boolean enableSyntaxHighlighting) { 
        this.enableSyntaxHighlighting = enableSyntaxHighlighting; 
    }
    
    public boolean isEnableLineNumbers() { return enableLineNumbers; }
    public void setEnableLineNumbers(boolean enableLineNumbers) { this.enableLineNumbers = enableLineNumbers; }
    
    public boolean isEnableWordWrap() { return enableWordWrap; }
    public void setEnableWordWrap(boolean enableWordWrap) { this.enableWordWrap = enableWordWrap; }
    
    // General settings getters/setters
    public boolean isEnableDarkTheme() { return enableDarkTheme; }
    public void setEnableDarkTheme(boolean enableDarkTheme) { this.enableDarkTheme = enableDarkTheme; }
    
    public boolean isEnableSecurityWarnings() { return enableSecurityWarnings; }
    public void setEnableSecurityWarnings(boolean enableSecurityWarnings) { 
        this.enableSecurityWarnings = enableSecurityWarnings; 
    }
    
    // Advanced settings getters/setters
    public boolean isEnableMemoryMonitoring() { return enableMemoryMonitoring; }
    public void setEnableMemoryMonitoring(boolean enableMemoryMonitoring) { 
        this.enableMemoryMonitoring = enableMemoryMonitoring; 
    }
    
    public boolean isLogPreviewActivity() { return logPreviewActivity; }
    public void setLogPreviewActivity(boolean logPreviewActivity) { 
        this.logPreviewActivity = logPreviewActivity; 
    }
    
    public int getMaxPreviewSizeMB() { return maxPreviewSizeMB; }
    public void setMaxPreviewSizeMB(int maxPreviewSizeMB) { 
        this.maxPreviewSizeMB = Math.max(1, Math.min(1000, maxPreviewSizeMB)); 
    }
    
    public int getCleanupDelaySeconds() { return cleanupDelaySeconds; }
    public void setCleanupDelaySeconds(int cleanupDelaySeconds) { 
        this.cleanupDelaySeconds = Math.max(1, Math.min(300, cleanupDelaySeconds)); 
    }
    
    public int getMaxConcurrentPreviews() { return maxConcurrentPreviews; }
    public void setMaxConcurrentPreviews(int maxConcurrentPreviews) { 
        this.maxConcurrentPreviews = Math.max(1, Math.min(20, maxConcurrentPreviews)); 
    }
    
    public boolean isShowLineNumbers() { return showLineNumbers; }
    public void setShowLineNumbers(boolean showLineNumbers) { 
        this.showLineNumbers = showLineNumbers; 
    }
    
    public boolean isWordWrap() { return wordWrap; }
    public void setWordWrap(boolean wordWrap) { 
        this.wordWrap = wordWrap; 
    }
    
    public boolean isEnableCodeFolding() { return enableCodeFolding; }
    public void setEnableCodeFolding(boolean enableCodeFolding) { 
        this.enableCodeFolding = enableCodeFolding; 
    }
    
    /**
     * Validate settings and fix any invalid values
     */
    public void validate() {
        setDefaultVolume(defaultVolume);
        setFontSize(fontSize);
        setMaxPreviewSizeMB(maxPreviewSizeMB);
        setCleanupDelaySeconds(cleanupDelaySeconds);
        setMaxConcurrentPreviews(maxConcurrentPreviews);
    }
    
    /**
     * Create settings optimized for media preview
     */
    public static PreviewSettings forMedia() {
        PreviewSettings settings = new PreviewSettings();
        settings.setAutoPlayMedia(false);
        settings.setShowMediaControls(true);
        settings.setEnableFullscreen(true);
        settings.setDefaultVolume(0.5);
        return settings;
    }
    
    /**
     * Create settings optimized for text preview
     */
    public static PreviewSettings forText() {
        PreviewSettings settings = new PreviewSettings();
        settings.setEnableSyntaxHighlighting(true);
        settings.setEnableLineNumbers(true);
        settings.setEnableWordWrap(true);
        settings.setFontFamily("Consolas");
        settings.setFontSize(12);
        return settings;
    }
    
    /**
     * Create settings optimized for code preview
     */
    public static PreviewSettings forCode() {
        PreviewSettings settings = new PreviewSettings();
        settings.setEnableSyntaxHighlighting(true);
        settings.setEnableLineNumbers(true);
        settings.setEnableWordWrap(false);
        settings.setFontFamily("JetBrains Mono");
        settings.setFontSize(11);
        return settings;
    }
}