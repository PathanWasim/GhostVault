package com.ghostvault.ui.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Typography manager for consistent font hierarchy throughout the application
 */
public class TypographyManager {
    
    // Font families
    public static final String PRIMARY_FONT = "Segoe UI";
    public static final String SECONDARY_FONT = "Arial";
    public static final String MONOSPACE_FONT = "Consolas";
    
    // Font sizes
    public static final double FONT_SIZE_H1 = 28.0;
    public static final double FONT_SIZE_H2 = 24.0;
    public static final double FONT_SIZE_H3 = 20.0;
    public static final double FONT_SIZE_H4 = 18.0;
    public static final double FONT_SIZE_H5 = 16.0;
    public static final double FONT_SIZE_H6 = 14.0;
    public static final double FONT_SIZE_BODY = 13.0;
    public static final double FONT_SIZE_SMALL = 11.0;
    public static final double FONT_SIZE_CAPTION = 10.0;
    
    // Line heights
    public static final double LINE_HEIGHT_TIGHT = 1.2;
    public static final double LINE_HEIGHT_NORMAL = 1.4;
    public static final double LINE_HEIGHT_RELAXED = 1.6;
    
    /**
     * Typography styles enumeration
     */
    public enum TypographyStyle {
        H1("typography-h1", FONT_SIZE_H1, FontWeight.BOLD, LINE_HEIGHT_TIGHT),
        H2("typography-h2", FONT_SIZE_H2, FontWeight.BOLD, LINE_HEIGHT_TIGHT),
        H3("typography-h3", FONT_SIZE_H3, FontWeight.SEMI_BOLD, LINE_HEIGHT_TIGHT),
        H4("typography-h4", FONT_SIZE_H4, FontWeight.SEMI_BOLD, LINE_HEIGHT_NORMAL),
        H5("typography-h5", FONT_SIZE_H5, FontWeight.MEDIUM, LINE_HEIGHT_NORMAL),
        H6("typography-h6", FONT_SIZE_H6, FontWeight.MEDIUM, LINE_HEIGHT_NORMAL),
        BODY("typography-body", FONT_SIZE_BODY, FontWeight.NORMAL, LINE_HEIGHT_NORMAL),
        BODY_BOLD("typography-body-bold", FONT_SIZE_BODY, FontWeight.BOLD, LINE_HEIGHT_NORMAL),
        SMALL("typography-small", FONT_SIZE_SMALL, FontWeight.NORMAL, LINE_HEIGHT_NORMAL),
        CAPTION("typography-caption", FONT_SIZE_CAPTION, FontWeight.NORMAL, LINE_HEIGHT_NORMAL),
        BUTTON("typography-button", FONT_SIZE_BODY, FontWeight.MEDIUM, LINE_HEIGHT_TIGHT),
        MONOSPACE("typography-monospace", FONT_SIZE_BODY, FontWeight.NORMAL, LINE_HEIGHT_NORMAL);
        
        private final String styleClass;
        private final double fontSize;
        private final FontWeight fontWeight;
        private final double lineHeight;
        
        TypographyStyle(String styleClass, double fontSize, FontWeight fontWeight, double lineHeight) {
            this.styleClass = styleClass;
            this.fontSize = fontSize;
            this.fontWeight = fontWeight;
            this.lineHeight = lineHeight;
        }
        
        public String getStyleClass() { return styleClass; }
        public double getFontSize() { return fontSize; }
        public FontWeight getFontWeight() { return fontWeight; }
        public double getLineHeight() { return lineHeight; }
    }
    
    /**
     * Apply typography style to a node
     */
    public static void applyStyle(Node node, TypographyStyle style) {
        if (node == null || style == null) return;
        
        // Remove existing typography classes
        node.getStyleClass().removeIf(cls -> cls.startsWith("typography-"));
        
        // Add new style class
        node.getStyleClass().add(style.getStyleClass());
        
        // Apply font properties directly for immediate effect
        String fontFamily = style == TypographyStyle.MONOSPACE ? MONOSPACE_FONT : PRIMARY_FONT;
        Font font = Font.font(fontFamily, style.getFontWeight(), style.getFontSize());
        
        if (node instanceof Label) {
            Label label = (Label) node;
            label.setFont(font);
            label.setLineSpacing(style.getLineHeight() - 1.0);
        } else if (node instanceof Text) {
            Text text = (Text) node;
            text.setFont(font);
        }
    }
    
    /**
     * Create a styled label with typography
     */
    public static Label createLabel(String text, TypographyStyle style) {
        Label label = new Label(text);
        applyStyle(label, style);
        return label;
    }
    
    /**
     * Create a styled text node with typography
     */
    public static Text createText(String text, TypographyStyle style) {
        Text textNode = new Text(text);
        applyStyle(textNode, style);
        return textNode;
    }
    
    /**
     * Apply heading style (H1-H6)
     */
    public static void applyHeading(Node node, int level) {
        TypographyStyle style;
        switch (level) {
            case 1: style = TypographyStyle.H1; break;
            case 2: style = TypographyStyle.H2; break;
            case 3: style = TypographyStyle.H3; break;
            case 4: style = TypographyStyle.H4; break;
            case 5: style = TypographyStyle.H5; break;
            case 6: style = TypographyStyle.H6; break;
            default: style = TypographyStyle.BODY; break;
        }
        applyStyle(node, style);
    }
    
    /**
     * Apply body text style
     */
    public static void applyBody(Node node) {
        applyStyle(node, TypographyStyle.BODY);
    }
    
    /**
     * Apply body bold text style
     */
    public static void applyBodyBold(Node node) {
        applyStyle(node, TypographyStyle.BODY_BOLD);
    }
    
    /**
     * Apply small text style
     */
    public static void applySmall(Node node) {
        applyStyle(node, TypographyStyle.SMALL);
    }
    
    /**
     * Apply caption text style
     */
    public static void applyCaption(Node node) {
        applyStyle(node, TypographyStyle.CAPTION);
    }
    
    /**
     * Apply button text style
     */
    public static void applyButton(Node node) {
        applyStyle(node, TypographyStyle.BUTTON);
    }
    
    /**
     * Apply monospace text style
     */
    public static void applyMonospace(Node node) {
        applyStyle(node, TypographyStyle.MONOSPACE);
    }
    
    /**
     * Get CSS for typography styles
     */
    public static String getTypographyCSS() {
        return """
            /* Typography Styles */
            .typography-h1 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: bold;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-h2 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: bold;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-h3 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: 600;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-h4 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: 600;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-h5 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: 500;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-h6 {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: 500;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-body {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: normal;
                -fx-text-fill: #e0e0e0;
                -fx-line-spacing: %.1f;
            }
            
            .typography-body-bold {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: bold;
                -fx-text-fill: #e0e0e0;
                -fx-line-spacing: %.1f;
            }
            
            .typography-small {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: normal;
                -fx-text-fill: #b0b0b0;
                -fx-line-spacing: %.1f;
            }
            
            .typography-caption {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: normal;
                -fx-text-fill: #888888;
                -fx-line-spacing: %.1f;
            }
            
            .typography-button {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: 500;
                -fx-text-fill: #ffffff;
                -fx-line-spacing: %.1f;
            }
            
            .typography-monospace {
                -fx-font-family: '%s';
                -fx-font-size: %.1fpx;
                -fx-font-weight: normal;
                -fx-text-fill: #e0e0e0;
                -fx-line-spacing: %.1f;
            }
            
            /* Text selection styles */
            .typography-h1:selected,
            .typography-h2:selected,
            .typography-h3:selected,
            .typography-h4:selected,
            .typography-h5:selected,
            .typography-h6:selected,
            .typography-body:selected,
            .typography-body-bold:selected,
            .typography-small:selected,
            .typography-caption:selected,
            .typography-button:selected,
            .typography-monospace:selected {
                -fx-highlight-fill: #3d5afe;
                -fx-highlight-text-fill: #ffffff;
            }
            
            /* Responsive typography for different screen sizes */
            @media screen and (max-width: 1024px) {
                .typography-h1 { -fx-font-size: %.1fpx; }
                .typography-h2 { -fx-font-size: %.1fpx; }
                .typography-h3 { -fx-font-size: %.1fpx; }
                .typography-h4 { -fx-font-size: %.1fpx; }
                .typography-h5 { -fx-font-size: %.1fpx; }
                .typography-h6 { -fx-font-size: %.1fpx; }
                .typography-body { -fx-font-size: %.1fpx; }
                .typography-body-bold { -fx-font-size: %.1fpx; }
                .typography-small { -fx-font-size: %.1fpx; }
                .typography-caption { -fx-font-size: %.1fpx; }
                .typography-button { -fx-font-size: %.1fpx; }
                .typography-monospace { -fx-font-size: %.1fpx; }
            }
            """.formatted(
                // Regular styles
                PRIMARY_FONT, FONT_SIZE_H1, LINE_HEIGHT_TIGHT,
                PRIMARY_FONT, FONT_SIZE_H2, LINE_HEIGHT_TIGHT,
                PRIMARY_FONT, FONT_SIZE_H3, LINE_HEIGHT_TIGHT,
                PRIMARY_FONT, FONT_SIZE_H4, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_H5, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_H6, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_BODY, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_BODY, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_SMALL, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_CAPTION, LINE_HEIGHT_NORMAL,
                PRIMARY_FONT, FONT_SIZE_BODY, LINE_HEIGHT_TIGHT,
                MONOSPACE_FONT, FONT_SIZE_BODY, LINE_HEIGHT_NORMAL,
                // Responsive styles (smaller)
                FONT_SIZE_H1 - 2, FONT_SIZE_H2 - 2, FONT_SIZE_H3 - 2,
                FONT_SIZE_H4 - 1, FONT_SIZE_H5 - 1, FONT_SIZE_H6 - 1,
                FONT_SIZE_BODY - 1, FONT_SIZE_BODY - 1, FONT_SIZE_SMALL - 1,
                FONT_SIZE_CAPTION - 1, FONT_SIZE_BODY - 1, FONT_SIZE_BODY - 1
            );
    }
    
    /**
     * Initialize typography system
     */
    public static void initialize() {
        // Load custom fonts if available
        try {
            Font.loadFont(TypographyManager.class.getResourceAsStream("/fonts/SegoeUI.ttf"), 12);
            Font.loadFont(TypographyManager.class.getResourceAsStream("/fonts/Consolas.ttf"), 12);
        } catch (Exception e) {
            // Fall back to system fonts
            System.out.println("Custom fonts not available, using system fonts");
        }
    }
}