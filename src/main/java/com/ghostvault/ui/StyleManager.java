package com.ghostvault.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

/**
 * Manages consistent text styling and visibility across UI components
 */
public class StyleManager {
    
    // Color constants for high contrast
    private static final String LIGHT_TEXT = "#FFFFFF";
    private static final String DARK_BACKGROUND = "#2b2b2b";
    private static final String DARKER_BACKGROUND = "#1a1a1a";
    private static final String INPUT_BACKGROUND = "#3a3a3a";
    private static final String PLACEHOLDER_TEXT = "#888888";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String ERROR_COLOR = "#f44336";
    private static final String WARNING_COLOR = "#ff9800";
    
    // Font constants
    private static final String MONO_FONT = "'Consolas', 'Monaco', 'Courier New', monospace";
    private static final String UI_FONT = "'Segoe UI', 'Arial', sans-serif";
    
    /**
     * Apply consistent text styling to any UI component
     */
    public static void applyTextStyles(Node component) {
        if (component == null) return;
        
        if (component instanceof TextArea) {
            applyTextAreaStyles((TextArea) component);
        } else if (component instanceof TextField) {
            applyTextFieldStyles((TextField) component);
        } else if (component instanceof ListView) {
            applyListViewStyles((ListView<?>) component);
        } else if (component instanceof Label) {
            applyLabelStyles((Label) component);
        } else if (component instanceof Button) {
            applyButtonStyles((Button) component);
        } else {
            // Apply general text styling
            applyGeneralTextStyles(component);
        }
    }
    
    /**
     * Ensure text area visibility with proper styling
     */
    public static void ensureTextVisibility(TextArea textArea) {
        if (textArea == null) return;
        
        String style = String.format(
            "-fx-text-fill: %s; " +
            "-fx-background-color: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 12px; " +
            "-fx-border-color: #555555; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8px;",
            LIGHT_TEXT, DARK_BACKGROUND, MONO_FONT
        );
        
        textArea.setStyle(style);
        
        // Ensure scroll pane content is also styled
        textArea.applyCss();
        textArea.layout();
    }
    
    /**
     * Apply high contrast colors for better visibility
     */
    public static void setContrastColors(Node node) {
        if (node == null) return;
        
        String baseStyle = String.format(
            "-fx-text-fill: %s; " +
            "-fx-background-color: %s;",
            LIGHT_TEXT, DARK_BACKGROUND
        );
        
        // Preserve existing styles and add contrast
        String existingStyle = node.getStyle();
        if (existingStyle != null && !existingStyle.isEmpty()) {
            node.setStyle(existingStyle + " " + baseStyle);
        } else {
            node.setStyle(baseStyle);
        }
    }
    
    /**
     * Apply styling to text areas (like log areas)
     */
    private static void applyTextAreaStyles(TextArea textArea) {
        String style = String.format(
            "-fx-text-fill: %s; " +
            "-fx-background-color: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 12px; " +
            "-fx-border-color: #555555; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 8px; " +
            "-fx-control-inner-background: %s;",
            LIGHT_TEXT, DARK_BACKGROUND, MONO_FONT, DARK_BACKGROUND
        );
        
        textArea.setStyle(style);
    }
    
    /**
     * Apply styling to text fields (like search fields)
     */
    private static void applyTextFieldStyles(TextField textField) {
        String style = String.format(
            "-fx-text-fill: %s; " +
            "-fx-background-color: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-prompt-text-fill: %s; " +
            "-fx-border-color: #555555; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; " +
            "-fx-padding: 6px;",
            LIGHT_TEXT, INPUT_BACKGROUND, UI_FONT, PLACEHOLDER_TEXT
        );
        
        textField.setStyle(style);
    }
    
    /**
     * Apply styling to list views (like file lists)
     */
    @SuppressWarnings("unchecked")
    private static void applyListViewStyles(ListView<?> listView) {
        String style = String.format(
            "-fx-background-color: %s; " +
            "-fx-control-inner-background: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-border-color: #555555; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            DARK_BACKGROUND, DARK_BACKGROUND, LIGHT_TEXT, UI_FONT
        );
        
        listView.setStyle(style);
        
        // Apply cell styling
        listView.setCellFactory(lv -> {
            ListCell cell = new ListCell() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        setStyle(String.format(
                            "-fx-text-fill: %s; " +
                            "-fx-background-color: transparent; " +
                            "-fx-padding: 4px 8px;",
                            LIGHT_TEXT
                        ));
                    }
                }
            };
            
            // Hover effect
            cell.setOnMouseEntered(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(String.format(
                        "-fx-text-fill: %s; " +
                        "-fx-background-color: #404040; " +
                        "-fx-padding: 4px 8px;",
                        LIGHT_TEXT
                    ));
                }
            });
            
            cell.setOnMouseExited(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(String.format(
                        "-fx-text-fill: %s; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 4px 8px;",
                        LIGHT_TEXT
                    ));
                }
            });
            
            return cell;
        });
    }
    
    /**
     * Apply styling to labels
     */
    private static void applyLabelStyles(Label label) {
        String style = String.format(
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px;",
            LIGHT_TEXT, UI_FONT
        );
        
        label.setStyle(style);
    }
    
    /**
     * Apply styling to buttons
     */
    private static void applyButtonStyles(Button button) {
        // Don't override button styles as they have their own theming
        // Just ensure text is visible
        if (button.getTextFill() == null || button.getTextFill().toString().equals("0x000000ff")) {
            button.setTextFill(javafx.scene.paint.Color.WHITE);
        }
    }
    
    /**
     * Apply general text styling to any node
     */
    private static void applyGeneralTextStyles(Node node) {
        String style = String.format(
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s;",
            LIGHT_TEXT, UI_FONT
        );
        
        String existingStyle = node.getStyle();
        if (existingStyle != null && !existingStyle.isEmpty()) {
            node.setStyle(existingStyle + " " + style);
        } else {
            node.setStyle(style);
        }
    }
    
    /**
     * Get success message styling
     */
    public static String getSuccessStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", SUCCESS_COLOR);
    }
    
    /**
     * Get error message styling
     */
    public static String getErrorStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", ERROR_COLOR);
    }
    
    /**
     * Get warning message styling
     */
    public static String getWarningStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", WARNING_COLOR);
    }
    
    /**
     * Apply styling to all child nodes recursively
     */
    public static void applyStylesToChildren(Node parent) {
        if (parent == null) return;
        
        applyTextStyles(parent);
        
        if (parent instanceof Region) {
            Region region = (Region) parent;
            region.getChildrenUnmodifiable().forEach(StyleManager::applyStylesToChildren);
        }
    }
}