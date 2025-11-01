package com.ghostvault.ui.animations;

import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Simple AnimationManager stub for compilation
 */
public class AnimationManager {
    public static final Duration FAST = Duration.millis(200);
    public static final Duration NORMAL = Duration.millis(400);
    public static final Duration SLOW = Duration.millis(600);
    
    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        // Stub implementation - just run the callback
        if (onFinished != null) {
            onFinished.run();
        }
    }
    
    public static void fadeIn(Node node, Duration duration) {
        // Stub implementation
    }
    
    public static void fadeIn(Node node, Duration duration, Runnable onFinished) {
        // Stub implementation with callback
        if (onFinished != null) {
            onFinished.run();
        }
    }
    
    public static void pulse(Node node, double scale, Duration duration) {
        // Stub implementation
    }
    
    public static void scaleIn(Node node) {
        // Stub implementation
    }
    
    public static void pulse(Node node) {
        // Stub implementation for single parameter version
    }
    
    public static void successGlow(Node node) {
        // Stub implementation
    }
    
    public static void shake(Node node) {
        // Stub implementation
    }
    
    public static void errorGlow(Node node) {
        // Stub implementation
    }
}