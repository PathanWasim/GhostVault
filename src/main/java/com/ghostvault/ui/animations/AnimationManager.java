package com.ghostvault.ui.animations;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized animation manager for smooth UI transitions and effects
 */
public class AnimationManager {
    
    private static AnimationManager instance;
    private Map<Node, Timeline> activeAnimations = new HashMap<>();
    
    // Animation durations
    public static final Duration FAST = Duration.millis(150);
    public static final Duration NORMAL = Duration.millis(300);
    public static final Duration SLOW = Duration.millis(500);
    public static final Duration VERY_SLOW = Duration.millis(800);
    
    // Easing functions
    public static final Interpolator EASE_OUT = Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94);
    public static final Interpolator EASE_IN = Interpolator.SPLINE(0.55, 0.06, 0.68, 0.19);
    public static final Interpolator EASE_IN_OUT = Interpolator.SPLINE(0.42, 0, 0.58, 1);
    public static final Interpolator BOUNCE = Interpolator.SPLINE(0.68, -0.55, 0.265, 1.55);
    
    private AnimationManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static AnimationManager getInstance() {
        if (instance == null) {
            instance = new AnimationManager();
        }
        return instance;
    }
    
    /**
     * Fade in animation
     */
    public static Timeline fadeIn(Node node) {
        return fadeIn(node, NORMAL, null);
    }
    
    public static Timeline fadeIn(Node node, Duration duration) {
        return fadeIn(node, duration, null);
    }
    
    public static Timeline fadeIn(Node node, Duration duration, Runnable onFinished) {
        node.setOpacity(0);
        
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT);
        
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, fade.getKeyFrames().get(0).getValues().toArray(new KeyValue[0])));
        
        getInstance().trackAnimation(node, timeline);
        fade.play();
        
        return timeline;
    }
    
    /**
     * Fade out animation
     */
    public static Timeline fadeOut(Node node) {
        return fadeOut(node, NORMAL, null);
    }
    
    public static Timeline fadeOut(Node node, Duration duration) {
        return fadeOut(node, duration, null);
    }
    
    public static Timeline fadeOut(Node node, Duration duration, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(node.getOpacity());
        fade.setToValue(0);
        fade.setInterpolator(EASE_IN);
        
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, fade.getKeyFrames().get(0).getValues().toArray(new KeyValue[0])));
        
        getInstance().trackAnimation(node, timeline);
        fade.play();
        
        return timeline;
    }
    
    /**
     * Slide in from left animation
     */
    public static Timeline slideInFromLeft(Node node) {
        return slideInFromLeft(node, NORMAL, null);
    }
    
    public static Timeline slideInFromLeft(Node node, Duration duration, Runnable onFinished) {
        double originalX = node.getTranslateX();
        node.setTranslateX(-node.getBoundsInParent().getWidth());
        
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(-node.getBoundsInParent().getWidth());
        slide.setToX(originalX);
        slide.setInterpolator(EASE_OUT);
        
        if (onFinished != null) {
            slide.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, 
            new KeyValue(node.translateXProperty(), originalX, EASE_OUT)));
        
        getInstance().trackAnimation(node, timeline);
        slide.play();
        
        return timeline;
    }
    
    /**
     * Slide in from right animation
     */
    public static Timeline slideInFromRight(Node node) {
        return slideInFromRight(node, NORMAL, null);
    }
    
    public static Timeline slideInFromRight(Node node, Duration duration, Runnable onFinished) {
        double originalX = node.getTranslateX();
        node.setTranslateX(node.getBoundsInParent().getWidth());
        
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(node.getBoundsInParent().getWidth());
        slide.setToX(originalX);
        slide.setInterpolator(EASE_OUT);
        
        if (onFinished != null) {
            slide.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, 
            new KeyValue(node.translateXProperty(), originalX, EASE_OUT)));
        
        getInstance().trackAnimation(node, timeline);
        slide.play();
        
        return timeline;
    }
    
    /**
     * Slide in from top animation
     */
    public static Timeline slideInFromTop(Node node) {
        return slideInFromTop(node, NORMAL, null);
    }
    
    public static Timeline slideInFromTop(Node node, Duration duration, Runnable onFinished) {
        double originalY = node.getTranslateY();
        node.setTranslateY(-node.getBoundsInParent().getHeight());
        
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(-node.getBoundsInParent().getHeight());
        slide.setToY(originalY);
        slide.setInterpolator(EASE_OUT);
        
        if (onFinished != null) {
            slide.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, 
            new KeyValue(node.translateYProperty(), originalY, EASE_OUT)));
        
        getInstance().trackAnimation(node, timeline);
        slide.play();
        
        return timeline;
    }
    
    /**
     * Slide out to left animation
     */
    public static Timeline slideOutToLeft(Node node) {
        return slideOutToLeft(node, NORMAL, null);
    }
    
    public static Timeline slideOutToLeft(Node node, Duration duration, Runnable onFinished) {
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(node.getTranslateX());
        slide.setToX(-node.getBoundsInParent().getWidth());
        slide.setInterpolator(EASE_IN);
        
        if (onFinished != null) {
            slide.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, 
            new KeyValue(node.translateXProperty(), -node.getBoundsInParent().getWidth(), EASE_IN)));
        
        getInstance().trackAnimation(node, timeline);
        slide.play();
        
        return timeline;
    }
    
    /**
     * Scale in animation (zoom in)
     */
    public static Timeline scaleIn(Node node) {
        return scaleIn(node, NORMAL, null);
    }
    
    public static Timeline scaleIn(Node node, Duration duration, Runnable onFinished) {
        node.setScaleX(0);
        node.setScaleY(0);
        
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(0);
        scale.setFromY(0);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(BOUNCE);
        
        if (onFinished != null) {
            scale.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(duration, new KeyValue(node.scaleXProperty(), 1, BOUNCE)),
            new KeyFrame(duration, new KeyValue(node.scaleYProperty(), 1, BOUNCE))
        );
        
        getInstance().trackAnimation(node, timeline);
        scale.play();
        
        return timeline;
    }
    
    /**
     * Scale out animation (zoom out)
     */
    public static Timeline scaleOut(Node node) {
        return scaleOut(node, NORMAL, null);
    }
    
    public static Timeline scaleOut(Node node, Duration duration, Runnable onFinished) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(node.getScaleX());
        scale.setFromY(node.getScaleY());
        scale.setToX(0);
        scale.setToY(0);
        scale.setInterpolator(EASE_IN);
        
        if (onFinished != null) {
            scale.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(duration, new KeyValue(node.scaleXProperty(), 0, EASE_IN)),
            new KeyFrame(duration, new KeyValue(node.scaleYProperty(), 0, EASE_IN))
        );
        
        getInstance().trackAnimation(node, timeline);
        scale.play();
        
        return timeline;
    }
    
    /**
     * Rotate animation
     */
    public static Timeline rotate(Node node, double fromAngle, double toAngle) {
        return rotate(node, fromAngle, toAngle, NORMAL, null);
    }
    
    public static Timeline rotate(Node node, double fromAngle, double toAngle, Duration duration, Runnable onFinished) {
        RotateTransition rotate = new RotateTransition(duration, node);
        rotate.setFromAngle(fromAngle);
        rotate.setToAngle(toAngle);
        rotate.setInterpolator(EASE_IN_OUT);
        
        if (onFinished != null) {
            rotate.setOnFinished(e -> onFinished.run());
        }
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(duration, 
            new KeyValue(node.rotateProperty(), toAngle, EASE_IN_OUT)));
        
        getInstance().trackAnimation(node, timeline);
        rotate.play();
        
        return timeline;
    }
    
    /**
     * Pulse animation (scale up and down)
     */
    public static Timeline pulse(Node node) {
        return pulse(node, 1.1, FAST);
    }
    
    public static Timeline pulse(Node node, double scale, Duration duration) {
        ScaleTransition scaleUp = new ScaleTransition(duration, node);
        scaleUp.setToX(scale);
        scaleUp.setToY(scale);
        scaleUp.setInterpolator(EASE_OUT);
        
        ScaleTransition scaleDown = new ScaleTransition(duration, node);
        scaleDown.setToX(1);
        scaleDown.setToY(1);
        scaleDown.setInterpolator(EASE_IN);
        
        SequentialTransition pulse = new SequentialTransition(scaleUp, scaleDown);
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(duration, new KeyValue(node.scaleXProperty(), scale, EASE_OUT)),
            new KeyFrame(duration, new KeyValue(node.scaleYProperty(), scale, EASE_OUT)),
            new KeyFrame(duration.multiply(2), new KeyValue(node.scaleXProperty(), 1, EASE_IN)),
            new KeyFrame(duration.multiply(2), new KeyValue(node.scaleYProperty(), 1, EASE_IN))
        );
        
        getInstance().trackAnimation(node, timeline);
        pulse.play();
        
        return timeline;
    }
    
    /**
     * Shake animation (error indication)
     */
    public static Timeline shake(Node node) {
        return shake(node, 10, FAST);
    }
    
    public static Timeline shake(Node node, double distance, Duration duration) {
        double originalX = node.getTranslateX();
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), originalX)),
            new KeyFrame(duration.multiply(0.1), new KeyValue(node.translateXProperty(), originalX + distance)),
            new KeyFrame(duration.multiply(0.2), new KeyValue(node.translateXProperty(), originalX - distance)),
            new KeyFrame(duration.multiply(0.3), new KeyValue(node.translateXProperty(), originalX + distance)),
            new KeyFrame(duration.multiply(0.4), new KeyValue(node.translateXProperty(), originalX - distance)),
            new KeyFrame(duration.multiply(0.5), new KeyValue(node.translateXProperty(), originalX + distance)),
            new KeyFrame(duration.multiply(0.6), new KeyValue(node.translateXProperty(), originalX - distance)),
            new KeyFrame(duration.multiply(0.7), new KeyValue(node.translateXProperty(), originalX + distance)),
            new KeyFrame(duration.multiply(0.8), new KeyValue(node.translateXProperty(), originalX - distance)),
            new KeyFrame(duration.multiply(0.9), new KeyValue(node.translateXProperty(), originalX + distance)),
            new KeyFrame(duration, new KeyValue(node.translateXProperty(), originalX))
        );
        
        getInstance().trackAnimation(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Glow effect animation
     */
    public static Timeline glow(Node node, Color color) {
        return glow(node, color, NORMAL);
    }
    
    public static Timeline glow(Node node, Color color, Duration duration) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(0);
        glow.setSpread(0.5);
        
        node.setEffect(glow);
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 0)),
            new KeyFrame(duration.multiply(0.5), new KeyValue(glow.radiusProperty(), 20, EASE_OUT)),
            new KeyFrame(duration, new KeyValue(glow.radiusProperty(), 0, EASE_IN))
        );
        
        timeline.setOnFinished(e -> node.setEffect(null));
        
        getInstance().trackAnimation(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Blur in animation
     */
    public static Timeline blurIn(Node node) {
        return blurIn(node, NORMAL);
    }
    
    public static Timeline blurIn(Node node, Duration duration) {
        GaussianBlur blur = new GaussianBlur(10);
        node.setEffect(blur);
        node.setOpacity(0);
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(blur.radiusProperty(), 10),
                new KeyValue(node.opacityProperty(), 0)),
            new KeyFrame(duration, 
                new KeyValue(blur.radiusProperty(), 0, EASE_OUT),
                new KeyValue(node.opacityProperty(), 1, EASE_OUT))
        );
        
        timeline.setOnFinished(e -> node.setEffect(null));
        
        getInstance().trackAnimation(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Flip animation
     */
    public static Timeline flip(Node node) {
        return flip(node, NORMAL);
    }
    
    public static Timeline flip(Node node, Duration duration) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1)),
            new KeyFrame(duration.multiply(0.5), new KeyValue(node.scaleXProperty(), 0, EASE_IN)),
            new KeyFrame(duration, new KeyValue(node.scaleXProperty(), 1, EASE_OUT))
        );
        
        getInstance().trackAnimation(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Bounce animation
     */
    public static Timeline bounce(Node node) {
        return bounce(node, NORMAL);
    }
    
    public static Timeline bounce(Node node, Duration duration) {
        double originalY = node.getTranslateY();
        
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), originalY)),
            new KeyFrame(duration.multiply(0.2), new KeyValue(node.translateYProperty(), originalY - 20, EASE_OUT)),
            new KeyFrame(duration.multiply(0.4), new KeyValue(node.translateYProperty(), originalY, EASE_IN)),
            new KeyFrame(duration.multiply(0.6), new KeyValue(node.translateYProperty(), originalY - 10, EASE_OUT)),
            new KeyFrame(duration.multiply(0.8), new KeyValue(node.translateYProperty(), originalY, EASE_IN)),
            new KeyFrame(duration, new KeyValue(node.translateYProperty(), originalY))
        );
        
        getInstance().trackAnimation(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Sequential animation builder
     */
    public static SequentialAnimationBuilder sequential() {
        return new SequentialAnimationBuilder();
    }
    
    /**
     * Parallel animation builder
     */
    public static ParallelAnimationBuilder parallel() {
        return new ParallelAnimationBuilder();
    }
    
    /**
     * Stop animation for a node
     */
    public static void stopAnimation(Node node) {
        getInstance().stopAnimationForNode(node);
    }
    
    /**
     * Stop all animations
     */
    public static void stopAllAnimations() {
        getInstance().stopAllActiveAnimations();
    }
    
    // Private methods
    
    private void trackAnimation(Node node, Timeline timeline) {
        // Stop any existing animation for this node
        stopAnimationForNode(node);
        
        // Track new animation
        activeAnimations.put(node, timeline);
        
        // Remove from tracking when finished
        timeline.setOnFinished(e -> activeAnimations.remove(node));
    }
    
    private void stopAnimationForNode(Node node) {
        Timeline existing = activeAnimations.get(node);
        if (existing != null) {
            existing.stop();
            activeAnimations.remove(node);
        }
    }
    
    private void stopAllActiveAnimations() {
        for (Timeline timeline : activeAnimations.values()) {
            timeline.stop();
        }
        activeAnimations.clear();
    }
    
    /**
     * Sequential animation builder
     */
    public static class SequentialAnimationBuilder {
        private SequentialTransition sequence = new SequentialTransition();
        
        public SequentialAnimationBuilder then(Animation animation) {
            sequence.getChildren().add(animation);
            return this;
        }
        
        public SequentialAnimationBuilder then(Runnable action, Duration delay) {
            PauseTransition pause = new PauseTransition(delay);
            pause.setOnFinished(e -> action.run());
            sequence.getChildren().add(pause);
            return this;
        }
        
        public SequentialTransition build() {
            return sequence;
        }
        
        public void play() {
            sequence.play();
        }
    }
    
    /**
     * Parallel animation builder
     */
    public static class ParallelAnimationBuilder {
        private ParallelTransition parallel = new ParallelTransition();
        
        public ParallelAnimationBuilder with(Animation animation) {
            parallel.getChildren().add(animation);
            return this;
        }
        
        public ParallelTransition build() {
            return parallel;
        }
        
        public void play() {
            parallel.play();
        }
    }
}