package com.ghostvault.ui.preview;

/**
 * Syntax highlighting themes for code viewer
 * Provides color schemes and styling for different code elements
 */
public enum SyntaxTheme {
    DARK("Dark Theme", createDarkStylesheet()),
    LIGHT("Light Theme", createLightStylesheet()),
    MONOKAI("Monokai", createMonokaiStylesheet()),
    SOLARIZED_DARK("Solarized Dark", createSolarizedDarkStylesheet()),
    SOLARIZED_LIGHT("Solarized Light", createSolarizedLightStylesheet());
    
    private final String displayName;
    private final String stylesheet;
    
    SyntaxTheme(String displayName, String stylesheet) {
        this.displayName = displayName;
        this.stylesheet = stylesheet;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getStylesheet() {
        return stylesheet;
    }
    
    /**
     * Create dark theme stylesheet
     */
    private static String createDarkStylesheet() {
        return """
            .code-area {
                -fx-background-color: #1e1e1e;
                -fx-text-fill: #d4d4d4;
            }
            
            .code-area .keyword {
                -fx-fill: #569cd6;
                -fx-font-weight: bold;
            }
            
            .code-area .string {
                -fx-fill: #ce9178;
            }
            
            .code-area .comment {
                -fx-fill: #6a9955;
                -fx-font-style: italic;
            }
            
            .code-area .number {
                -fx-fill: #b5cea8;
            }
            
            .code-area .annotation,
            .code-area .decorator {
                -fx-fill: #dcdcaa;
            }
            
            .code-area .tag {
                -fx-fill: #569cd6;
            }
            
            .code-area .attribute {
                -fx-fill: #92c5f8;
            }
            
            .code-area .selector {
                -fx-fill: #d7ba7d;
            }
            
            .code-area .property {
                -fx-fill: #92c5f8;
            }
            
            .code-area .value {
                -fx-fill: #ce9178;
            }
            
            .code-area .color {
                -fx-fill: #4ec9b0;
            }
            
            .code-area .regex {
                -fx-fill: #d16969;
            }
            
            .code-area .doctype {
                -fx-fill: #808080;
            }
            """;
    }
    
    /**
     * Create light theme stylesheet
     */
    private static String createLightStylesheet() {
        return """
            .code-area {
                -fx-background-color: #ffffff;
                -fx-text-fill: #000000;
            }
            
            .code-area .keyword {
                -fx-fill: #0000ff;
                -fx-font-weight: bold;
            }
            
            .code-area .string {
                -fx-fill: #a31515;
            }
            
            .code-area .comment {
                -fx-fill: #008000;
                -fx-font-style: italic;
            }
            
            .code-area .number {
                -fx-fill: #098658;
            }
            
            .code-area .annotation,
            .code-area .decorator {
                -fx-fill: #795e26;
            }
            
            .code-area .tag {
                -fx-fill: #800000;
            }
            
            .code-area .attribute {
                -fx-fill: #ff0000;
            }
            
            .code-area .selector {
                -fx-fill: #800000;
            }
            
            .code-area .property {
                -fx-fill: #ff0000;
            }
            
            .code-area .value {
                -fx-fill: #0451a5;
            }
            
            .code-area .color {
                -fx-fill: #0451a5;
            }
            
            .code-area .regex {
                -fx-fill: #811f3f;
            }
            
            .code-area .doctype {
                -fx-fill: #808080;
            }
            """;
    }
    
    /**
     * Create Monokai theme stylesheet
     */
    private static String createMonokaiStylesheet() {
        return """
            .code-area {
                -fx-background-color: #272822;
                -fx-text-fill: #f8f8f2;
            }
            
            .code-area .keyword {
                -fx-fill: #f92672;
                -fx-font-weight: bold;
            }
            
            .code-area .string {
                -fx-fill: #e6db74;
            }
            
            .code-area .comment {
                -fx-fill: #75715e;
                -fx-font-style: italic;
            }
            
            .code-area .number {
                -fx-fill: #ae81ff;
            }
            
            .code-area .annotation,
            .code-area .decorator {
                -fx-fill: #a6e22e;
            }
            
            .code-area .tag {
                -fx-fill: #f92672;
            }
            
            .code-area .attribute {
                -fx-fill: #a6e22e;
            }
            
            .code-area .selector {
                -fx-fill: #a6e22e;
            }
            
            .code-area .property {
                -fx-fill: #66d9ef;
            }
            
            .code-area .value {
                -fx-fill: #e6db74;
            }
            
            .code-area .color {
                -fx-fill: #ae81ff;
            }
            
            .code-area .regex {
                -fx-fill: #e6db74;
            }
            
            .code-area .doctype {
                -fx-fill: #75715e;
            }
            """;
    }
    
    /**
     * Create Solarized Dark theme stylesheet
     */
    private static String createSolarizedDarkStylesheet() {
        return """
            .code-area {
                -fx-background-color: #002b36;
                -fx-text-fill: #839496;
            }
            
            .code-area .keyword {
                -fx-fill: #859900;
                -fx-font-weight: bold;
            }
            
            .code-area .string {
                -fx-fill: #2aa198;
            }
            
            .code-area .comment {
                -fx-fill: #586e75;
                -fx-font-style: italic;
            }
            
            .code-area .number {
                -fx-fill: #d33682;
            }
            
            .code-area .annotation,
            .code-area .decorator {
                -fx-fill: #b58900;
            }
            
            .code-area .tag {
                -fx-fill: #268bd2;
            }
            
            .code-area .attribute {
                -fx-fill: #b58900;
            }
            
            .code-area .selector {
                -fx-fill: #268bd2;
            }
            
            .code-area .property {
                -fx-fill: #268bd2;
            }
            
            .code-area .value {
                -fx-fill: #2aa198;
            }
            
            .code-area .color {
                -fx-fill: #d33682;
            }
            
            .code-area .regex {
                -fx-fill: #dc322f;
            }
            
            .code-area .doctype {
                -fx-fill: #586e75;
            }
            """;
    }
    
    /**
     * Create Solarized Light theme stylesheet
     */
    private static String createSolarizedLightStylesheet() {
        return """
            .code-area {
                -fx-background-color: #fdf6e3;
                -fx-text-fill: #657b83;
            }
            
            .code-area .keyword {
                -fx-fill: #859900;
                -fx-font-weight: bold;
            }
            
            .code-area .string {
                -fx-fill: #2aa198;
            }
            
            .code-area .comment {
                -fx-fill: #93a1a1;
                -fx-font-style: italic;
            }
            
            .code-area .number {
                -fx-fill: #d33682;
            }
            
            .code-area .annotation,
            .code-area .decorator {
                -fx-fill: #b58900;
            }
            
            .code-area .tag {
                -fx-fill: #268bd2;
            }
            
            .code-area .attribute {
                -fx-fill: #b58900;
            }
            
            .code-area .selector {
                -fx-fill: #268bd2;
            }
            
            .code-area .property {
                -fx-fill: #268bd2;
            }
            
            .code-area .value {
                -fx-fill: #2aa198;
            }
            
            .code-area .color {
                -fx-fill: #d33682;
            }
            
            .code-area .regex {
                -fx-fill: #dc322f;
            }
            
            .code-area .doctype {
                -fx-fill: #93a1a1;
            }
            """;
    }
}