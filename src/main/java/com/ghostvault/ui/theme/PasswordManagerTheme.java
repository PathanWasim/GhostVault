package com.ghostvault.ui.theme;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

/**
 * Professional Password Manager Theme for GhostVault
 * Inspired by modern password managers like Bitwarden, 1Password, etc.
 */
public class PasswordManagerTheme {
    
    // Password Manager Color Palette
    public static final String PRIMARY_BLUE = "#175DDC";
    public static final String PRIMARY_BLUE_HOVER = "#1348B8";
    public static final String PRIMARY_BLUE_PRESSED = "#0F3A94";
    
    public static final String SECONDARY_BLUE = "#3B82F6";
    public static final String ACCENT_GREEN = "#10B981";
    public static final String ACCENT_RED = "#EF4444";
    public static final String ACCENT_ORANGE = "#F59E0B";
    
    // Background colors - Professional dark theme
    public static final String BACKGROUND_PRIMARY = "#0F172A";      // Very dark blue-gray
    public static final String BACKGROUND_SECONDARY = "#1E293B";    // Dark blue-gray
    public static final String SURFACE_COLOR = "#334155";           // Medium blue-gray
    public static final String CARD_COLOR = "#475569";              // Light blue-gray
    public static final String SIDEBAR_COLOR = "#1E293B";           // Sidebar background
    
    // Text colors
    public static final String TEXT_PRIMARY = "#F8FAFC";            // Almost white
    public static final String TEXT_SECONDARY = "#CBD5E1";          // Light gray
    public static final String TEXT_MUTED = "#94A3B8";              // Muted gray
    public static final String TEXT_DISABLED = "#64748B";           // Disabled gray
    
    // Border and divider colors
    public static final String BORDER_COLOR = "#475569";
    public static final String BORDER_LIGHT = "#64748B";
    public static final String DIVIDER_COLOR = "#334155";
    
    // Status colors
    public static final String SUCCESS_COLOR = "#10B981";
    public static final String WARNING_COLOR = "#F59E0B";
    public static final String ERROR_COLOR = "#EF4444";
    public static final String INFO_COLOR = "#3B82F6";
    
    // Fonts
    public static final String FONT_FAMILY = "'Inter', 'Segoe UI', 'Roboto', sans-serif";
    public static final String MONO_FONT_FAMILY = "'JetBrains Mono', 'Fira Code', 'Consolas', monospace";
    
    /**
     * Apply the complete password manager theme to a scene
     */
    public static void applyPasswordManagerTheme(Scene scene) {
        if (scene == null) return;
        
        // Apply global CSS
        scene.getStylesheets().clear();
        scene.getStylesheets().add(PasswordManagerTheme.class.getResource("/css/password-manager-theme.css").toExternalForm());
        
        // Apply theme to root
        Parent root = scene.getRoot();
        if (root != null) {
            root.setStyle("-fx-background-color: " + BACKGROUND_PRIMARY + ";");
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
            applyPasswordManagerButtonStyles((Button) node);
        } else if (node instanceof TextField) {
            applyPasswordManagerTextFieldStyles((TextField) node);
        } else if (node instanceof PasswordField) {
            applyPasswordManagerPasswordFieldStyles((PasswordField) node);
        } else if (node instanceof TextArea) {
            applyPasswordManagerTextAreaStyles((TextArea) node);
        } else if (node instanceof Label) {
            applyPasswordManagerLabelStyles((Label) node);
        } else if (node instanceof ListView) {
            applyPasswordManagerListViewStyles((ListView<?>) node);
        } else if (node instanceof TableView) {
            applyPasswordManagerTableViewStyles((TableView<?>) node);
        } else if (node instanceof ProgressBar) {
            applyPasswordManagerProgressBarStyles((ProgressBar) node);
        } else if (node instanceof MenuBar) {
            applyPasswordManagerMenuBarStyles((MenuBar) node);
        } else if (node instanceof ToolBar) {
            applyPasswordManagerToolBarStyles((ToolBar) node);
        }
        
        // Apply to children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            parent.getChildrenUnmodifiable().forEach(PasswordManagerTheme::applyThemeToNode);
        }
    }
    
    /**
     * Apply base styles to any node
     */
    private static void applyBaseStyles(Node node) {
        String baseStyle = String.format(
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px;",
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
     * Apply modern password manager button styling
     */
    private static void applyPasswordManagerButtonStyles(Button button) {
        // Determine button type based on style classes or text
        String buttonType = determineButtonType(button);
        
        String buttonStyle;
        switch (buttonType) {
            case "primary":
                buttonStyle = getPrimaryButtonStyle();
                break;
            case "secondary":
                buttonStyle = getSecondaryButtonStyle();
                break;
            case "danger":
                buttonStyle = getDangerButtonStyle();
                break;
            case "success":
                buttonStyle = getSuccessButtonStyle();
                break;
            default:
                buttonStyle = getDefaultButtonStyle();
                break;
        }
        
        button.setStyle(buttonStyle);
        
        // Add hover and pressed effects
        addButtonInteractionEffects(button, buttonType);
    }
    
    private static String determineButtonType(Button button) {
        String text = button.getText().toLowerCase();
        
        if (text.contains("upload") || text.contains("save") || text.contains("add") || 
            text.contains("create") || text.contains("complete") || text.contains("login")) {
            return "primary";
        } else if (text.contains("delete") || text.contains("remove") || text.contains("panic")) {
            return "danger";
        } else if (text.contains("download") || text.contains("export") || text.contains("backup")) {
            return "success";
        } else if (text.contains("cancel") || text.contains("close") || text.contains("settings")) {
            return "secondary";
        }
        
        return "default";
    }
    
    private static String getPrimaryButtonStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(23, 93, 220, 0.3), 4, 0, 0, 2);",
            PRIMARY_BLUE, TEXT_PRIMARY, FONT_FAMILY
        );
    }
    
    private static String getSecondaryButtonStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand;",
            SURFACE_COLOR, TEXT_PRIMARY, FONT_FAMILY, BORDER_COLOR
        );
    }
    
    private static String getDangerButtonStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.3), 4, 0, 0, 2);",
            ERROR_COLOR, TEXT_PRIMARY, FONT_FAMILY
        );
    }
    
    private static String getSuccessButtonStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);",
            SUCCESS_COLOR, TEXT_PRIMARY, FONT_FAMILY
        );
    }
    
    private static String getDefaultButtonStyle() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10px 20px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand;",
            CARD_COLOR, TEXT_PRIMARY, FONT_FAMILY
        );
    }
    
    private static void addButtonInteractionEffects(Button button, String buttonType) {
        String originalStyle = button.getStyle();
        
        button.setOnMouseEntered(e -> {
            String hoverColor;
            switch (buttonType) {
                case "primary":
                    hoverColor = PRIMARY_BLUE_HOVER;
                    break;
                case "danger":
                    hoverColor = "#DC2626";
                    break;
                case "success":
                    hoverColor = "#059669";
                    break;
                default:
                    hoverColor = "#64748B";
                    break;
            }
            button.setStyle(originalStyle.replaceFirst("-fx-background-color: [^;]+", "-fx-background-color: " + hoverColor));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
        
        button.setOnMousePressed(e -> {
            button.setStyle(originalStyle + " -fx-scale-x: 0.98; -fx-scale-y: 0.98;");
        });
        
        button.setOnMouseReleased(e -> {
            button.setStyle(originalStyle);
        });
    }
    
    /**
     * Apply password manager text field styling
     */
    private static void applyPasswordManagerTextFieldStyles(TextField textField) {
        String textFieldStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-prompt-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            SURFACE_COLOR, TEXT_PRIMARY, TEXT_MUTED, FONT_FAMILY, BORDER_COLOR
        );
        
        textField.setStyle(textFieldStyle);
        
        // Focus effects
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle(textFieldStyle.replace(BORDER_COLOR, PRIMARY_BLUE) + 
                    " -fx-effect: dropshadow(gaussian, rgba(23, 93, 220, 0.2), 4, 0, 0, 0);");
            } else {
                textField.setStyle(textFieldStyle);
            }
        });
    }
    
    /**
     * Apply password manager password field styling
     */
    private static void applyPasswordManagerPasswordFieldStyles(PasswordField passwordField) {
        String passwordFieldStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-prompt-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            SURFACE_COLOR, TEXT_PRIMARY, TEXT_MUTED, MONO_FONT_FAMILY, BORDER_COLOR
        );
        
        passwordField.setStyle(passwordFieldStyle);
        
        // Focus effects
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle(passwordFieldStyle.replace(BORDER_COLOR, PRIMARY_BLUE) + 
                    " -fx-effect: dropshadow(gaussian, rgba(23, 93, 220, 0.2), 4, 0, 0, 0);");
            } else {
                passwordField.setStyle(passwordFieldStyle);
            }
        });
    }
    
    /**
     * Apply password manager text area styling
     */
    private static void applyPasswordManagerTextAreaStyles(TextArea textArea) {
        String textAreaStyle = String.format(
            "-fx-control-inner-background: %s; " +
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 12px; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            SURFACE_COLOR, SURFACE_COLOR, TEXT_PRIMARY, MONO_FONT_FAMILY, BORDER_COLOR
        );
        
        textArea.setStyle(textAreaStyle);
    }
    
    /**
     * Apply password manager label styling
     */
    private static void applyPasswordManagerLabelStyles(Label label) {
        String labelStyle = String.format(
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px;",
            TEXT_PRIMARY, FONT_FAMILY
        );
        
        // Check for special label types
        String text = label.getText().toLowerCase();
        if (text.contains("error") || text.contains("failed") || text.contains("✗")) {
            labelStyle += " -fx-text-fill: " + ERROR_COLOR + "; -fx-font-weight: 600;";
        } else if (text.contains("success") || text.contains("complete") || text.contains("✓")) {
            labelStyle += " -fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: 600;";
        } else if (text.contains("warning") || text.contains("⚠")) {
            labelStyle += " -fx-text-fill: " + WARNING_COLOR + "; -fx-font-weight: 600;";
        }
        
        label.setStyle(labelStyle);
    }
    
    /**
     * Apply password manager list view styling
     */
    @SuppressWarnings("unchecked")
    private static void applyPasswordManagerListViewStyles(ListView<?> listView) {
        String listViewStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-control-inner-background: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            SURFACE_COLOR, SURFACE_COLOR, BORDER_COLOR
        );
        
        listView.setStyle(listViewStyle);
        
        // Custom cell factory for password manager styling
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
                            "-fx-padding: 12px 16px; " +
                            "-fx-font-family: %s; " +
                            "-fx-font-size: 14px; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-width: 0 0 1px 0; " +
                            "-fx-border-color: %s;",
                            TEXT_PRIMARY, FONT_FAMILY, DIVIDER_COLOR
                        ));
                    }
                }
            };
            
            // Hover effects
            cell.setOnMouseEntered(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(cell.getStyle() + String.format(" -fx-background-color: %s;", CARD_COLOR));
                }
            });
            
            cell.setOnMouseExited(e -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(cell.getStyle().replace(String.format(" -fx-background-color: %s;", CARD_COLOR), ""));
                }
            });
            
            return cell;
        });
    }
    
    /**
     * Apply password manager table view styling
     */
    private static void applyPasswordManagerTableViewStyles(TableView<?> tableView) {
        String tableStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-control-inner-background: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            SURFACE_COLOR, SURFACE_COLOR, TEXT_PRIMARY, BORDER_COLOR
        );
        
        tableView.setStyle(tableStyle);
    }
    
    /**
     * Apply password manager progress bar styling
     */
    private static void applyPasswordManagerProgressBarStyles(ProgressBar progressBar) {
        String progressStyle = String.format(
            "-fx-accent: %s; " +
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;",
            PRIMARY_BLUE, SURFACE_COLOR, BORDER_COLOR
        );
        
        progressBar.setStyle(progressStyle);
    }
    
    /**
     * Apply password manager menu bar styling
     */
    private static void applyPasswordManagerMenuBarStyles(MenuBar menuBar) {
        String menuStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-font-family: %s; " +
            "-fx-font-size: 14px;",
            SIDEBAR_COLOR, TEXT_PRIMARY, FONT_FAMILY
        );
        
        menuBar.setStyle(menuStyle);
    }
    
    /**
     * Apply password manager toolbar styling
     */
    private static void applyPasswordManagerToolBarStyles(ToolBar toolBar) {
        String toolBarStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-padding: 12px 16px;",
            SIDEBAR_COLOR, BORDER_COLOR
        );
        
        toolBar.setStyle(toolBarStyle);
    }
    
    /**
     * Get styled success message
     */
    public static String getSuccessStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: 600;", SUCCESS_COLOR);
    }
    
    /**
     * Get styled error message
     */
    public static String getErrorStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: 600;", ERROR_COLOR);
    }
    
    /**
     * Get styled warning message
     */
    public static String getWarningStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: 600;", WARNING_COLOR);
    }
    
    /**
     * Get styled info message
     */
    public static String getInfoStyle() {
        return String.format("-fx-text-fill: %s; -fx-font-weight: 600;", INFO_COLOR);
    }
}