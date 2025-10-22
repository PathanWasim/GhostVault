package com.ghostvault.ui.theme;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Comprehensive theme management system for GhostVault
 */
public class ThemeManager {
    
    // Modern color palette
    public static final String PRIMARY_COLOR = "#2196F3";
    public static final String PRIMARY_DARK = "#1976D2";
    public static final String ACCENT_COLOR = "#FF4081";
    
    // Background colors
    public static final String BACKGROUND_PRIMARY = "#121212";
    public static final String BACKGROUND_SECONDARY = "#1E1E1E";
    public static final String SURFACE_COLOR = "#2D2D2D";
    public static final String CARD_COLOR = "#383838";
    
    // Text colors
    public static final String TEXT_PRIMARY = "#FFFFFF";
    public static final String TEXT_SECONDARY = "#B3B3B3";
    public static final String TEXT_DISABLED = "#666666";
    
    // Status colors
    public static final String SUCCESS_COLOR = "#4CAF50";
    public static final String WARNING_COLOR = "#FF9800";
    public static final String ERROR_COLOR = "#F44336";
    public static final String INFO_COLOR = "#2196F3";
    
    // Border and divider colors
    public static final String BORDER_COLOR = "#404040";
    public static final String DIVIDER_COLOR = "#2D2D2D";
    
    // Fonts
    public static final String FONT_FAMILY = "'Segoe UI', 'Roboto', 'Arial', sans-serif";
    public static final String MONO_FONT_FAMILY = "'Consolas', 'Monaco', 'Courier New', monospace";
    
    /**
     * Apply the complete theme to a scene
     */
    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        
        // Apply global CSS
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource("/css/ghostvault-theme.css").toExternalForm());
        
        // Apply theme to root
        Parent root = scene.getRoot();
        if (root != null) {
            applyThemeToNode(root);
        }
    }
    
    /**
     * Apply theme to any node and its children
     */
    public static void applyThemeToNode(Node node) {
        if (node == null) return;
        
        // Apply base styling
        applyBaseStyles(node);
        
        // Apply specific component styling
        if (node instanceof Button) {
            applyButtonStyles((Button) node);
        } else if (node instanceof TextField) {
            applyTextFieldStyles((TextField) node);
        } else if (node instanceof TextArea) {
            applyTextAreaStyles((TextArea) node);
        } else if (node instanceof Label) {
            applyLabelStyles((Label) node);
        } else if (node instanceof ListView) {
            applyListViewStyles((ListView<?>) node);
        } else if (node instanceof TableView) {
            applyTableViewStyles((TableView<?>) node);
        } else if (node instanceof ProgressBar) {
            applyProgressBarStyles((ProgressBar) node);
        } else if (node instanceof MenuBar) {
            applyMenuBarStyles((MenuBar) node);
        } else if (node instanceof ToolBar) {
            applyToolBarStyles((ToolBar) node);
        }
        
        // Apply to children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            parent.getChildrenUnmodifiable().forEach(ThemeManager::applyThemeToNode);
        }
    }
    
    /**
     * Apply base styles to any node
     */
    private static void applyBaseStyles(Node node) {
        String baseStyle = String.format(
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px;",
            FONT_FAMILY
        );
        
        String existingStyle = node.getStyle();
        if (existingStyle != null && !existingStyle.isEmpty()) {
            node.setStyle(existingStyle + " " + baseStyle);
        } else {
            node.setStyle(baseStyle);
        }
    }
    
    /**
     * Apply modern button styling
     */
    private static void applyButtonStyles(Button button) {
        String buttonStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 8px 16px; " +
            "-fx-background-radius: 4px; " +
            "-fx-border-radius: 4px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);",
            PRIMARY_COLOR, TEXT_PRIMARY, FONT_FAMILY
        );
        
        button.setStyle(buttonStyle);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(buttonStyle.replace(PRIMARY_COLOR, PRIMARY_DARK));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(buttonStyle);
        });
    }
    
    /**
     * Apply text field styling
     */
    private static void applyTextFieldStyles(TextField textField) {
        String textFieldStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-prompt-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8px 12px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            SURFACE_COLOR, TEXT_PRIMARY, TEXT_SECONDARY, FONT_FAMILY, BORDER_COLOR
        );
        
        textField.setStyle(textFieldStyle);
        
        // Focus effects
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle(textFieldStyle.replace(BORDER_COLOR, PRIMARY_COLOR));
            } else {
                textField.setStyle(textFieldStyle);
            }
        });
    }
    
    /**
     * Apply text area styling
     */
    private static void applyTextAreaStyles(TextArea textArea) {
        String textAreaStyle = String.format(
            "-fx-control-inner-background: %s; " +
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 8px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            SURFACE_COLOR, SURFACE_COLOR, TEXT_PRIMARY, MONO_FONT_FAMILY, BORDER_COLOR
        );
        
        textArea.setStyle(textAreaStyle);
    }
    
    /**
     * Apply label styling
     */
    private static void applyLabelStyles(Label label) {
        String labelStyle = String.format(
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px;",
            TEXT_PRIMARY, FONT_FAMILY
        );
        
        label.setStyle(labelStyle);
    }
    
    /**
     * Apply list view styling
     */
    @SuppressWarnings("unchecked")
    private static void applyListViewStyles(ListView<?> listView) {
        String listViewStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-control-inner-background: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            SURFACE_COLOR, SURFACE_COLOR, BORDER_COLOR
        );
        
        listView.setStyle(listViewStyle);
        
        // Custom cell factory for proper styling
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
                            "-fx-padding: 8px 12px; " +
                            "-fx-font-family: %s; " +
                            "-fx-font-size: 13px;",
                            TEXT_PRIMARY, FONT_FAMILY
                        ));
                    }
                }
            };
            
            // Hover effects
            cell.setOnMouseEntered(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(cell.getStyle() + String.format("-fx-background-color: %s;", CARD_COLOR));
                }
            });
            
            cell.setOnMouseExited(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(cell.getStyle().replace(String.format("-fx-background-color: %s;", CARD_COLOR), "-fx-background-color: transparent;"));
                }
            });
            
            return cell;
        });
    }
    
    /**
     * Apply table view styling
     */
    private static void applyTableViewStyles(TableView<?> tableView) {
        String tableStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-control-inner-background: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            SURFACE_COLOR, SURFACE_COLOR, TEXT_PRIMARY, BORDER_COLOR
        );
        
        tableView.setStyle(tableStyle);
    }
    
    /**
     * Apply progress bar styling
     */
    private static void applyProgressBarStyles(ProgressBar progressBar) {
        String progressStyle = String.format(
            "-fx-accent: %s; " +
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px; " +
            "-fx-background-radius: 4px;",
            PRIMARY_COLOR, SURFACE_COLOR, BORDER_COLOR
        );
        
        progressBar.setStyle(progressStyle);
    }
    
    /**
     * Apply menu bar styling
     */
    private static void applyMenuBarStyles(MenuBar menuBar) {
        String menuStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px;",
            BACKGROUND_SECONDARY, TEXT_PRIMARY, FONT_FAMILY
        );
        
        menuBar.setStyle(menuStyle);
    }
    
    /**
     * Apply toolbar styling
     */
    private static void applyToolBarStyles(ToolBar toolBar) {
        String toolBarStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-padding: 8px;",
            BACKGROUND_SECONDARY, BORDER_COLOR
        );
        
        toolBar.setStyle(toolBarStyle);
    }
    
    /**
     * Get styled success message
     */
    public static String getSuccessStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", SUCCESS_COLOR);
    }
    
    /**
     * Get styled error message
     */
    public static String getErrorStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", ERROR_COLOR);
    }
    
    /**
     * Get styled warning message
     */
    public static String getWarningStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", WARNING_COLOR);
    }
    
    /**
     * Get styled info message
     */
    public static String getInfoStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: bold;", INFO_COLOR);
    }
}