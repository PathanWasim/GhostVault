package com.ghostvault.ui.preview;

import com.ghostvault.ui.preview.PreviewSettings.SyntaxTheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PreviewSettings
 */
class PreviewSettingsTest {
    
    @Test
    @DisplayName("Should create PreviewSettings with default values")
    void testDefaultSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Media settings defaults
        assertFalse(settings.isAutoPlayMedia());
        assertEquals(0.7, settings.getDefaultVolume(), 0.001);
        assertTrue(settings.isShowMediaControls());
        assertTrue(settings.isEnableFullscreenVideo());
        
        // Code viewer settings defaults
        assertTrue(settings.isEnableSyntaxHighlighting());
        assertEquals(SyntaxTheme.DARK, settings.getCodeTheme());
        assertTrue(settings.isShowLineNumbers());
        assertEquals(12, settings.getFontSize());
        assertEquals("Consolas, Monaco, 'Courier New', monospace", settings.getFontFamily());
        assertTrue(settings.isEnableCodeFolding());
        assertTrue(settings.isHighlightCurrentLine());
        
        // Text viewer settings defaults
        assertTrue(settings.isEnableMarkdownRendering());
        assertTrue(settings.isWordWrap());
        assertFalse(settings.isShowTextStatistics());
        assertEquals("UTF-8", settings.getTextEncoding());
        
        // Performance settings defaults
        assertEquals(50, settings.getMaxPreviewSizeMB());
        assertEquals(3, settings.getMaxConcurrentPreviews());
        assertTrue(settings.isEnableMemoryMonitoring());
        assertEquals(30, settings.getCleanupDelaySeconds());
        
        // Security settings defaults
        assertTrue(settings.isEnableSecureCleanup());
        assertTrue(settings.isLogPreviewActivity());
        assertTrue(settings.isRespectSessionTimeout());
        
        // UI settings defaults
        assertTrue(settings.isDarkTheme());
        assertTrue(settings.isShowPreviewTooltips());
        assertTrue(settings.isEnableKeyboardShortcuts());
    }
    
    @Test
    @DisplayName("Should set and get media settings correctly")
    void testMediaSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test auto play media
        settings.setAutoPlayMedia(true);
        assertTrue(settings.isAutoPlayMedia());
        
        // Test default volume with clamping
        settings.setDefaultVolume(0.5);
        assertEquals(0.5, settings.getDefaultVolume(), 0.001);
        
        settings.setDefaultVolume(-0.1); // Below minimum
        assertEquals(0.0, settings.getDefaultVolume(), 0.001);
        
        settings.setDefaultVolume(1.5); // Above maximum
        assertEquals(1.0, settings.getDefaultVolume(), 0.001);
        
        // Test media controls
        settings.setShowMediaControls(false);
        assertFalse(settings.isShowMediaControls());
        
        // Test fullscreen video
        settings.setEnableFullscreenVideo(false);
        assertFalse(settings.isEnableFullscreenVideo());
    }
    
    @Test
    @DisplayName("Should set and get code viewer settings correctly")
    void testCodeViewerSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test syntax highlighting
        settings.setEnableSyntaxHighlighting(false);
        assertFalse(settings.isEnableSyntaxHighlighting());
        
        // Test code theme
        settings.setCodeTheme(SyntaxTheme.LIGHT);
        assertEquals(SyntaxTheme.LIGHT, settings.getCodeTheme());
        
        // Test line numbers
        settings.setShowLineNumbers(false);
        assertFalse(settings.isShowLineNumbers());
        
        // Test font size with clamping
        settings.setFontSize(14);
        assertEquals(14, settings.getFontSize());
        
        settings.setFontSize(5); // Below minimum
        assertEquals(8, settings.getFontSize());
        
        settings.setFontSize(100); // Above maximum
        assertEquals(72, settings.getFontSize());
        
        // Test font family
        settings.setFontFamily("Arial");
        assertEquals("Arial", settings.getFontFamily());
        
        // Test code folding
        settings.setEnableCodeFolding(false);
        assertFalse(settings.isEnableCodeFolding());
        
        // Test highlight current line
        settings.setHighlightCurrentLine(false);
        assertFalse(settings.isHighlightCurrentLine());
    }
    
    @Test
    @DisplayName("Should set and get text viewer settings correctly")
    void testTextViewerSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test markdown rendering
        settings.setEnableMarkdownRendering(false);
        assertFalse(settings.isEnableMarkdownRendering());
        
        // Test word wrap
        settings.setWordWrap(false);
        assertFalse(settings.isWordWrap());
        
        // Test text statistics
        settings.setShowTextStatistics(true);
        assertTrue(settings.isShowTextStatistics());
        
        // Test text encoding
        settings.setTextEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", settings.getTextEncoding());
    }
    
    @Test
    @DisplayName("Should set and get performance settings correctly")
    void testPerformanceSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test max preview size with clamping
        settings.setMaxPreviewSizeMB(100);
        assertEquals(100, settings.getMaxPreviewSizeMB());
        
        settings.setMaxPreviewSizeMB(0); // Below minimum
        assertEquals(1, settings.getMaxPreviewSizeMB());
        
        settings.setMaxPreviewSizeMB(1000); // Above maximum
        assertEquals(500, settings.getMaxPreviewSizeMB());
        
        // Test max concurrent previews with clamping
        settings.setMaxConcurrentPreviews(5);
        assertEquals(5, settings.getMaxConcurrentPreviews());
        
        settings.setMaxConcurrentPreviews(0); // Below minimum
        assertEquals(1, settings.getMaxConcurrentPreviews());
        
        settings.setMaxConcurrentPreviews(20); // Above maximum
        assertEquals(10, settings.getMaxConcurrentPreviews());
        
        // Test memory monitoring
        settings.setEnableMemoryMonitoring(false);
        assertFalse(settings.isEnableMemoryMonitoring());
        
        // Test cleanup delay with clamping
        settings.setCleanupDelaySeconds(60);
        assertEquals(60, settings.getCleanupDelaySeconds());
        
        settings.setCleanupDelaySeconds(-10); // Below minimum
        assertEquals(0, settings.getCleanupDelaySeconds());
        
        settings.setCleanupDelaySeconds(500); // Above maximum
        assertEquals(300, settings.getCleanupDelaySeconds());
    }
    
    @Test
    @DisplayName("Should set and get security settings correctly")
    void testSecuritySettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test secure cleanup
        settings.setEnableSecureCleanup(false);
        assertFalse(settings.isEnableSecureCleanup());
        
        // Test preview activity logging
        settings.setLogPreviewActivity(false);
        assertFalse(settings.isLogPreviewActivity());
        
        // Test session timeout respect
        settings.setRespectSessionTimeout(false);
        assertFalse(settings.isRespectSessionTimeout());
    }
    
    @Test
    @DisplayName("Should set and get UI settings correctly")
    void testUISettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Test dark theme
        settings.setDarkTheme(false);
        assertFalse(settings.isDarkTheme());
        
        // Test preview tooltips
        settings.setShowPreviewTooltips(false);
        assertFalse(settings.isShowPreviewTooltips());
        
        // Test keyboard shortcuts
        settings.setEnableKeyboardShortcuts(false);
        assertFalse(settings.isEnableKeyboardShortcuts());
    }
    
    @Test
    @DisplayName("Should copy settings correctly")
    void testCopySettings() {
        PreviewSettings original = new PreviewSettings();
        original.setAutoPlayMedia(true);
        original.setDefaultVolume(0.8);
        original.setCodeTheme(SyntaxTheme.MONOKAI);
        original.setFontSize(16);
        original.setMaxPreviewSizeMB(100);
        
        PreviewSettings copy = original.copy();
        
        // Verify copy has same values
        assertEquals(original.isAutoPlayMedia(), copy.isAutoPlayMedia());
        assertEquals(original.getDefaultVolume(), copy.getDefaultVolume(), 0.001);
        assertEquals(original.getCodeTheme(), copy.getCodeTheme());
        assertEquals(original.getFontSize(), copy.getFontSize());
        assertEquals(original.getMaxPreviewSizeMB(), copy.getMaxPreviewSizeMB());
        
        // Verify they are different objects
        assertNotSame(original, copy);
        
        // Verify modifying copy doesn't affect original
        copy.setAutoPlayMedia(false);
        assertTrue(original.isAutoPlayMedia());
        assertFalse(copy.isAutoPlayMedia());
    }
    
    @Test
    @DisplayName("Should reset to defaults correctly")
    void testResetToDefaults() {
        PreviewSettings settings = new PreviewSettings();
        
        // Modify settings
        settings.setAutoPlayMedia(true);
        settings.setDefaultVolume(0.9);
        settings.setCodeTheme(SyntaxTheme.LIGHT);
        settings.setFontSize(20);
        settings.setDarkTheme(false);
        
        // Reset to defaults
        settings.resetToDefaults();
        
        // Verify defaults are restored
        assertFalse(settings.isAutoPlayMedia());
        assertEquals(0.7, settings.getDefaultVolume(), 0.001);
        assertEquals(SyntaxTheme.DARK, settings.getCodeTheme());
        assertEquals(12, settings.getFontSize());
        assertTrue(settings.isDarkTheme());
    }
    
    @Test
    @DisplayName("Should validate settings correctly")
    void testValidateSettings() {
        PreviewSettings settings = new PreviewSettings();
        
        // Set invalid values
        settings.setDefaultVolume(2.0); // Above max
        settings.setFontSize(100); // Above max
        settings.setMaxPreviewSizeMB(1000); // Above max
        settings.setCodeTheme(null); // Null value
        settings.setFontFamily(null); // Null value
        settings.setTextEncoding(""); // Empty value
        
        // Validate
        settings.validate();
        
        // Check values are corrected
        assertEquals(1.0, settings.getDefaultVolume(), 0.001);
        assertEquals(72, settings.getFontSize());
        assertEquals(500, settings.getMaxPreviewSizeMB());
        assertEquals(SyntaxTheme.DARK, settings.getCodeTheme());
        assertEquals("Consolas, Monaco, 'Courier New', monospace", settings.getFontFamily());
        assertEquals("UTF-8", settings.getTextEncoding());
    }
    
    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        PreviewSettings settings = new PreviewSettings();
        String toString = settings.toString();
        
        assertTrue(toString.contains("PreviewSettings"));
        assertTrue(toString.contains("autoPlayMedia"));
        assertTrue(toString.contains("defaultVolume"));
        assertTrue(toString.contains("enableSyntaxHighlighting"));
        assertTrue(toString.contains("codeTheme"));
        assertTrue(toString.contains("fontSize"));
        assertTrue(toString.contains("maxPreviewSizeMB"));
        assertTrue(toString.contains("enableSecureCleanup"));
    }
    
    @Test
    @DisplayName("Should handle SyntaxTheme enum correctly")
    void testSyntaxThemeEnum() {
        // Test all themes have required properties
        for (SyntaxTheme theme : SyntaxTheme.values()) {
            assertNotNull(theme.getDisplayName());
            assertNotNull(theme.getBackgroundColor());
            assertNotNull(theme.getTextColor());
            assertFalse(theme.getDisplayName().isEmpty());
            assertFalse(theme.getBackgroundColor().isEmpty());
            assertFalse(theme.getTextColor().isEmpty());
        }
        
        // Test specific themes
        assertEquals("Dark Theme", SyntaxTheme.DARK.getDisplayName());
        assertEquals("#2b2b2b", SyntaxTheme.DARK.getBackgroundColor());
        assertEquals("#ffffff", SyntaxTheme.DARK.getTextColor());
        
        assertEquals("Light Theme", SyntaxTheme.LIGHT.getDisplayName());
        assertEquals("#ffffff", SyntaxTheme.LIGHT.getBackgroundColor());
        assertEquals("#000000", SyntaxTheme.LIGHT.getTextColor());
        
        assertEquals("Monokai", SyntaxTheme.MONOKAI.getDisplayName());
        assertEquals("#272822", SyntaxTheme.MONOKAI.getBackgroundColor());
        assertEquals("#f8f8f2", SyntaxTheme.MONOKAI.getTextColor());
    }
}