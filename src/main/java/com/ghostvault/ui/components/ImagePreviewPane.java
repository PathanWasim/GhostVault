package com.ghostvault.ui.components;

/**
 * Alias for ImagePreviewComponent to maintain compatibility
 */
public class ImagePreviewPane extends ImagePreviewComponent {
    
    public ImagePreviewPane() {
        super();
    }
    
    public double getZoomFactor() {
        // Delegate to parent class if it has this method, otherwise return default
        return 1.0;
    }
    
    public double getRotation() {
        // Delegate to parent class if it has this method, otherwise return default
        return 0.0;
    }
}