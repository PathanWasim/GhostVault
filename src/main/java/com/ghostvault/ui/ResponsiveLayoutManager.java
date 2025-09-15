package com.ghostvault.ui;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsive layout manager that adapts UI elements based on window size
 */
public class ResponsiveLayoutManager {
    
    private Stage stage;
    private Scene scene;
    private final List<ResponsiveElement> responsiveElements;
    private ChangeListener<Number> sizeListener;
    
    // Breakpoints for responsive design
    public enum Breakpoint {
        SMALL(600),    // Mobile/small screens
        MEDIUM(900),   // Tablets
        LARGE(1200),   // Desktop
        XLARGE(1600);  // Large desktop
        
        private final double width;
        
        Breakpoint(double width) {
            this.width = width;
        }
        
        public double getWidth() { return width; }
    }
    
    public ResponsiveLayoutManager(Stage stage) {
        this.stage = stage;
        this.scene = stage.getScene();
        this.responsiveElements = new ArrayList<>();
        
        setupResponsiveListeners();
    }
    
    /**
     * Setup listeners for window size changes
     */
    private void setupResponsiveListeners() {
        sizeListener = (obs, oldVal, newVal) -> updateLayout();
        
        if (scene != null) {
            scene.widthProperty().addListener(sizeListener);
            scene.heightProperty().addListener(sizeListener);
        }
        
        stage.widthProperty().addListener(sizeListener);
        stage.heightProperty().addListener(sizeListener);
    }
    
    /**
     * Add responsive element
     */
    public void addResponsiveElement(Node node, ResponsiveConfig config) {
        responsiveElements.add(new ResponsiveElement(node, config));
        updateLayout();
    }
    
    /**
     * Remove responsive element
     */
    public void removeResponsiveElement(Node node) {
        responsiveElements.removeIf(element -> element.node == node);
    }
    
    /**
     * Update layout based on current window size
     */
    private void updateLayout() {
        double width = stage.getWidth();
        double height = stage.getHeight();
        
        Breakpoint currentBreakpoint = getCurrentBreakpoint(width);
        
        for (ResponsiveElement element : responsiveElements) {
            applyResponsiveConfig(element, currentBreakpoint, width, height);
        }
    }
    
    /**
     * Get current breakpoint based on width
     */
    private Breakpoint getCurrentBreakpoint(double width) {
        if (width < Breakpoint.SMALL.getWidth()) {
            return Breakpoint.SMALL;
        } else if (width < Breakpoint.MEDIUM.getWidth()) {
            return Breakpoint.MEDIUM;
        } else if (width < Breakpoint.LARGE.getWidth()) {
            return Breakpoint.LARGE;
        } else {
            return Breakpoint.XLARGE;
        }
    }
    
    /**
     * Apply responsive configuration to element
     */
    private void applyResponsiveConfig(ResponsiveElement element, Breakpoint breakpoint, 
                                     double windowWidth, double windowHeight) {
        Node node = element.node;
        ResponsiveConfig config = element.config;
        
        // Apply font size scaling
        if (config.scaleFontSize && node instanceof Label) {
            Label label = (Label) node;
            double baseFontSize = config.baseFontSize > 0 ? config.baseFontSize : 12;
            double scaledSize = calculateScaledFontSize(baseFontSize, breakpoint);
            label.setFont(Font.font(label.getFont().getFamily(), scaledSize));
        }
        
        // Apply width scaling
        if (config.scaleWidth) {
            double scaledWidth = calculateScaledWidth(config.baseWidth, windowWidth, breakpoint);
            if (node instanceof Button) {
                ((Button) node).setPrefWidth(scaledWidth);
            } else if (node instanceof TextField) {
                ((TextField) node).setPrefWidth(scaledWidth);
            } else if (node instanceof TableView) {
                ((TableView<?>) node).setPrefWidth(scaledWidth);
            }
        }
        
        // Apply height scaling
        if (config.scaleHeight) {
            double scaledHeight = calculateScaledHeight(config.baseHeight, windowHeight, breakpoint);
            if (node instanceof TableView) {
                ((TableView<?>) node).setPrefHeight(scaledHeight);
            }
        }
        
        // Apply visibility rules
        if (config.hideOnSmallScreens && breakpoint == Breakpoint.SMALL) {
            node.setVisible(false);
            node.setManaged(false);
        } else {
            node.setVisible(true);
            node.setManaged(true);
        }
        
        // Apply layout priority changes
        if (config.adjustLayoutPriority) {
            adjustLayoutPriority(node, breakpoint);
        }
    }
    
    /**
     * Calculate scaled font size based on breakpoint
     */
    private double calculateScaledFontSize(double baseSize, Breakpoint breakpoint) {
        switch (breakpoint) {
            case SMALL:
                return baseSize * 0.85;
            case MEDIUM:
                return baseSize * 0.95;
            case LARGE:
                return baseSize;
            case XLARGE:
                return baseSize * 1.1;
            default:
                return baseSize;
        }
    }
    
    /**
     * Calculate scaled width based on window width and breakpoint
     */
    private double calculateScaledWidth(double baseWidth, double windowWidth, Breakpoint breakpoint) {
        if (baseWidth <= 0) {
            // Use percentage of window width
            switch (breakpoint) {
                case SMALL:
                    return windowWidth * 0.9;
                case MEDIUM:
                    return windowWidth * 0.7;
                case LARGE:
                    return windowWidth * 0.5;
                case XLARGE:
                    return windowWidth * 0.4;
                default:
                    return windowWidth * 0.5;
            }
        } else {
            // Scale the base width
            switch (breakpoint) {
                case SMALL:
                    return Math.min(baseWidth * 0.8, windowWidth * 0.9);
                case MEDIUM:
                    return Math.min(baseWidth * 0.9, windowWidth * 0.8);
                case LARGE:
                    return baseWidth;
                case XLARGE:
                    return baseWidth * 1.1;
                default:
                    return baseWidth;
            }
        }
    }
    
    /**
     * Calculate scaled height based on window height and breakpoint
     */
    private double calculateScaledHeight(double baseHeight, double windowHeight, Breakpoint breakpoint) {
        if (baseHeight <= 0) {
            // Use percentage of window height
            switch (breakpoint) {
                case SMALL:
                    return windowHeight * 0.6;
                case MEDIUM:
                    return windowHeight * 0.7;
                case LARGE:
                    return windowHeight * 0.8;
                case XLARGE:
                    return windowHeight * 0.8;
                default:
                    return windowHeight * 0.7;
            }
        } else {
            // Scale the base height
            switch (breakpoint) {
                case SMALL:
                    return Math.min(baseHeight * 0.8, windowHeight * 0.6);
                case MEDIUM:
                    return Math.min(baseHeight * 0.9, windowHeight * 0.7);
                case LARGE:
                    return baseHeight;
                case XLARGE:
                    return baseHeight * 1.1;
                default:
                    return baseHeight;
            }
        }
    }
    
    /**
     * Adjust layout priority based on breakpoint
     */
    private void adjustLayoutPriority(Node node, Breakpoint breakpoint) {
        Priority priority;
        
        switch (breakpoint) {
            case SMALL:
                priority = Priority.ALWAYS;
                break;
            case MEDIUM:
                priority = Priority.SOMETIMES;
                break;
            case LARGE:
            case XLARGE:
            default:
                priority = Priority.NEVER;
                break;
        }
        
        // Apply to parent containers
        if (node.getParent() instanceof HBox) {
            HBox.setHgrow(node, priority);
        } else if (node.getParent() instanceof VBox) {
            VBox.setVgrow(node, priority);
        } else if (node.getParent() instanceof GridPane) {
            GridPane.setHgrow(node, priority);
            GridPane.setVgrow(node, priority);
        }
    }
    
    /**
     * Create responsive configuration for buttons
     */
    public static ResponsiveConfig createButtonConfig() {
        return new ResponsiveConfig()
            .setScaleWidth(true)
            .setBaseWidth(120)
            .setScaleFontSize(true)
            .setBaseFontSize(12);
    }
    
    /**
     * Create responsive configuration for text fields
     */
    public static ResponsiveConfig createTextFieldConfig() {
        return new ResponsiveConfig()
            .setScaleWidth(true)
            .setBaseWidth(200)
            .setScaleFontSize(true)
            .setBaseFontSize(12);
    }
    
    /**
     * Create responsive configuration for tables
     */
    public static ResponsiveConfig createTableConfig() {
        return new ResponsiveConfig()
            .setScaleWidth(true)
            .setScaleHeight(true)
            .setBaseWidth(600)
            .setBaseHeight(400)
            .setAdjustLayoutPriority(true);
    }
    
    /**
     * Create responsive configuration for labels
     */
    public static ResponsiveConfig createLabelConfig() {
        return new ResponsiveConfig()
            .setScaleFontSize(true)
            .setBaseFontSize(12);
    }
    
    /**
     * Create responsive configuration for optional elements
     */
    public static ResponsiveConfig createOptionalElementConfig() {
        return new ResponsiveConfig()
            .setHideOnSmallScreens(true)
            .setScaleFontSize(true)
            .setBaseFontSize(11);
    }
    
    /**
     * Cleanup listeners
     */
    public void cleanup() {
        if (scene != null && sizeListener != null) {
            scene.widthProperty().removeListener(sizeListener);
            scene.heightProperty().removeListener(sizeListener);
        }
        
        if (stage != null && sizeListener != null) {
            stage.widthProperty().removeListener(sizeListener);
            stage.heightProperty().removeListener(sizeListener);
        }
        
        responsiveElements.clear();
    }
    
    /**
     * Responsive configuration class
     */
    public static class ResponsiveConfig {
        private boolean scaleFontSize = false;
        private double baseFontSize = 12;
        private boolean scaleWidth = false;
        private double baseWidth = 0;
        private boolean scaleHeight = false;
        private double baseHeight = 0;
        private boolean hideOnSmallScreens = false;
        private boolean adjustLayoutPriority = false;
        
        public ResponsiveConfig setScaleFontSize(boolean scaleFontSize) {
            this.scaleFontSize = scaleFontSize;
            return this;
        }
        
        public ResponsiveConfig setBaseFontSize(double baseFontSize) {
            this.baseFontSize = baseFontSize;
            return this;
        }
        
        public ResponsiveConfig setScaleWidth(boolean scaleWidth) {
            this.scaleWidth = scaleWidth;
            return this;
        }
        
        public ResponsiveConfig setBaseWidth(double baseWidth) {
            this.baseWidth = baseWidth;
            return this;
        }
        
        public ResponsiveConfig setScaleHeight(boolean scaleHeight) {
            this.scaleHeight = scaleHeight;
            return this;
        }
        
        public ResponsiveConfig setBaseHeight(double baseHeight) {
            this.baseHeight = baseHeight;
            return this;
        }
        
        public ResponsiveConfig setHideOnSmallScreens(boolean hideOnSmallScreens) {
            this.hideOnSmallScreens = hideOnSmallScreens;
            return this;
        }
        
        public ResponsiveConfig setAdjustLayoutPriority(boolean adjustLayoutPriority) {
            this.adjustLayoutPriority = adjustLayoutPriority;
            return this;
        }
    }
    
    /**
     * Responsive element wrapper
     */
    private static class ResponsiveElement {
        final Node node;
        final ResponsiveConfig config;
        
        ResponsiveElement(Node node, ResponsiveConfig config) {
            this.node = node;
            this.config = config;
        }
    }
}