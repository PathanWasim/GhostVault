package com.ghostvault.ui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Manages UI animations and transitions for GhostVault
 * Provides smooth, professional animations for better user experience
 */
public class AnimationManager {
    
    private static final Duration FAST_DURATION = Duration.millis(150);
    private static final Duration NORMAL_DURATION = Duration.millis(300);
    private static final Duration SLOW_DURATION = Duration.millis(500);
    
    /**
     * Fade in animation
     */
    public static FadeTransition fadeIn(Node node) {
        return fadeIn(node, NORMAL_DURATION);
    }
    
    public static FadeTransition fadeIn(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        return fade;
    }
    
    /**
     * Fade out animation
     */
    public static FadeTransition fadeOut(Node node) {
        return fadeOut(node, NORMAL_DURATION);
    }
    
    public static FadeTransition fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        return fade;
    }
    
    /**
     * Slide in from left animation
     */
    public static TranslateTransition slideInFromLeft(Node node) {
        TranslateTransition slide = new TranslateTransition(NORMAL_DURATION, node);
        slide.setFromX(-node.getBoundsInParent().getWidth());
        slide.setToX(0);
        
        // Combine with fade in
        FadeTransition fade = fadeIn(node, NORMAL_DURATION);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        return slide; // Return slide transition, but parallel will play
    }
    
    /**
     * Slide in from right animation
     */
    public static TranslateTransition slideInFromRight(Node node) {
        TranslateTransition slide = new TranslateTransition(NORMAL_DURATION, node);
        slide.setFromX(node.getBoundsInParent().getWidth());
        slide.setToX(0);
        
        // Combine with fade in
        FadeTransition fade = fadeIn(node, NORMAL_DURATION);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        return slide;
    }
    
    /**
     * Slide up animation
     */
    public static TranslateTransition slideUp(Node node) {
        TranslateTransition slide = new TranslateTransition(NORMAL_DURATION, node);
        slide.setFromY(node.getBoundsInParent().getHeight());
        slide.setToY(0);
        
        // Combine with fade in
        FadeTransition fade = fadeIn(node, NORMAL_DURATION);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        return slide;
    }
    
    /**
     * Scale in animation (zoom in)
     */
    public static ScaleTransition scaleIn(Node node) {
        ScaleTransition scale = new ScaleTransition(NORMAL_DURATION, node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        // Combine with fade in
        FadeTransition fade = fadeIn(node, NORMAL_DURATION);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
        return scale;
    }
    
    /**
     * Pulse animation for attention
     */
    public static ScaleTransition pulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        return pulse;
    }
    
    /**
     * Shake animation for errors
     */
    public static TranslateTransition shake(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        return shake;
    }
    
    /**
     * Glow effect animation
     */
    public static Timeline glow(Node node, Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(0);
        
        node.setEffect(glow);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(300), new KeyValue(glow.radiusProperty(), 20)),
            new KeyFrame(Duration.millis(600), new KeyValue(glow.radiusProperty(), 0))
        );
        
        timeline.setOnFinished(e -> node.setEffect(null));
        return timeline;
    }
    
    /**
     * Success glow (green)
     */
    public static Timeline successGlow(Node node) {
        return glow(node, Color.GREEN);
    }
    
    /**
     * Error glow (red)
     */
    public static Timeline errorGlow(Node node) {
        return glow(node, Color.RED);
    }
    
    /**
     * Warning glow (orange)
     */
    public static Timeline warningGlow(Node node) {
        return glow(node, Color.ORANGE);
    }
    
    /**
     * Rotate animation
     */
    public static RotateTransition rotate(Node node, double angle) {
        RotateTransition rotate = new RotateTransition(NORMAL_DURATION, node);
        rotate.setFromAngle(0);
        rotate.setToAngle(angle);
        return rotate;
    }
    
    /**
     * Continuous rotation (for loading indicators)
     */
    public static RotateTransition continuousRotate(Node node) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), node);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        return rotate;
    }
    
    /**
     * Typewriter effect for text
     */
    public static Timeline typewriter(javafx.scene.control.Label label, String text) {
        label.setText("");
        
        Timeline timeline = new Timeline();
        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 50),
                e -> label.setText(text.substring(0, index))
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        return timeline;
    }
    
    /**
     * Progress bar animation
     */
    public static Timeline animateProgress(javafx.scene.control.ProgressBar progressBar, 
                                         double fromValue, double toValue) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), fromValue)),
            new KeyFrame(Duration.seconds(1), new KeyValue(progressBar.progressProperty(), toValue))
        );
        return timeline;
    }
    
    /**
     * Smooth scene transition
     */
    public static void smoothSceneTransition(javafx.stage.Stage stage, 
                                           javafx.scene.Scene newScene, 
                                           Runnable onComplete) {
        if (stage.getScene() == null) {
            stage.setScene(newScene);
            if (onComplete != null) onComplete.run();
            return;
        }
        
        // Fade out current scene
        FadeTransition fadeOut = fadeOut(stage.getScene().getRoot(), FAST_DURATION);
        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            
            // Fade in new scene
            FadeTransition fadeIn = fadeIn(newScene.getRoot(), FAST_DURATION);
            fadeIn.setOnFinished(e2 -> {
                if (onComplete != null) onComplete.run();
            });
            fadeIn.play();
        });
        fadeOut.play();
    }
    
    /**
     * Attention-grabbing bounce animation
     */
    public static SequentialTransition bounce(Node node) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), node);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        return new SequentialTransition(scaleUp, scaleDown);
    }
    
    /**
     * Slide out animation
     */
    public static TranslateTransition slideOut(Node node, Direction direction) {
        TranslateTransition slide = new TranslateTransition(NORMAL_DURATION, node);
        
        switch (direction) {
            case LEFT:
                slide.setToX(-node.getBoundsInParent().getWidth());
                break;
            case RIGHT:
                slide.setToX(node.getBoundsInParent().getWidth());
                break;
            case UP:
                slide.setToY(-node.getBoundsInParent().getHeight());
                break;
            case DOWN:
                slide.setToY(node.getBoundsInParent().getHeight());
                break;
        }
        
        // Combine with fade out
        FadeTransition fade = fadeOut(node, NORMAL_DURATION);
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
        
        return slide;
    }
    
    /**
     * Direction enum for slide animations
     */
    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }
    
    /**
     * Create a staggered animation for multiple nodes
     */
    public static SequentialTransition staggeredFadeIn(Node... nodes) {
        SequentialTransition sequence = new SequentialTransition();
        
        for (Node node : nodes) {
            FadeTransition fade = fadeIn(node, FAST_DURATION);
            sequence.getChildren().add(fade);
        }
        
        return sequence;
    }
    
    /**
     * Create loading dots animation
     */
    public static Timeline loadingDots(javafx.scene.control.Label label, String baseText) {
        Timeline timeline = new Timeline();
        
        for (int i = 0; i <= 3; i++) {
            final String dots = ".".repeat(i);
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 500),
                e -> label.setText(baseText + dots)
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }
}