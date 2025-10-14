package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Professional code preview component with syntax highlighting
 */
public class CodePreviewPane extends VBox {
    
    private static final Font CODE_FONT = Font.font("Consolas", 13);
    private static final int MAX_LINES_PREVIEW = 1000;
    
    // Language detection patterns
    private static final Map<String, String> LANGUAGE_EXTENSIONS = new HashMap<>();
    static {
        LANGUAGE_EXTENSIONS.put(".py", "python");
        LANGUAGE_EXTENSIONS.put(".cpp", "cpp");
        LANGUAGE_EXTENSIONS.put(".cxx", "cpp");
        LANGUAGE_EXTENSIONS.put(".cc", "cpp");
        LANGUAGE_EXTENSIONS.put(".c", "c");
        LANGUAGE_EXTENSIONS.put(".h", "c");
        LANGUAGE_EXTENSIONS.put(".hpp", "cpp");
        LANGUAGE_EXTENSIONS.put(".java", "java");
        LANGUAGE_EXTENSIONS.put(".js", "javascript");
        LANGUAGE_EXTENSIONS.put(".jsx", "javascript");
        LANGUAGE_EXTENSIONS.put(".ts", "typescript");
        LANGUAGE_EXTENSIONS.put(".tsx", "typescript");
        LANGUAGE_EXTENSIONS.put(".html", "html");
        LANGUAGE_EXTENSIONS.put(".htm", "html");
        LANGUAGE_EXTENSIONS.put(".css", "css");
        LANGUAGE_EXTENSIONS.put(".scss", "css");
        LANGUAGE_EXTENSIONS.put(".sass", "css");
        LANGUAGE_EXTENSIONS.put(".xml", "xml");
        LANGUAGE_EXTENSIONS.put(".json", "json");
        LANGUAGE_EXTENSIONS.put(".yaml", "yaml");
        LANGUAGE_EXTENSIONS.put(".yml", "yaml");
        LANGUAGE_EXTENSIONS.put(".md", "markdown");
        LANGUAGE_EXTENSIONS.put(".sql", "sql");
        LANGUAGE_EXTENSIONS.put(".sh", "bash");
        LANGUAGE_EXTENSIONS.put(".bat", "batch");
        LANGUAGE_EXTENSIONS.put(".ps1", "powershell");
    }
    
    // Syntax highlighting colors
    private static final Map<String, Color> SYNTAX_COLORS = new HashMap<>();
    static {
        SYNTAX_COLORS.put("keyword", Color.web("#569cd6"));      // Blue
        SYNTAX_COLORS.put("string", Color.web("#ce9178"));       // Orange
        SYNTAX_COLORS.put("comment", Color.web("#6a9955"));      // Green
        SYNTAX_COLORS.put("number", Color.web("#b5cea8"));       // Light green
        SYNTAX_COLORS.put("operator", Color.web("#d4d4d4"));     // Light gray
        SYNTAX_COLORS.put("function", Color.web("#dcdcaa"));     // Yellow
        SYNTAX_COLORS.put("type", Color.web("#4ec9b0"));         // Cyan
        SYNTAX_COLORS.put("default", Color.web("#d4d4d4"));      // Default text
    }
    
    private final Label headerLabel;
    private final ScrollPane scrollPane;
    private final VBox contentBox;
    private final ProgressIndicator loadingIndicator;
    private final Label errorLabel;
    
    private String currentFileName;
    private String currentLanguage;
    
    public CodePreviewPane() {
        super(5);
        setPadding(new Insets(10));
        getStyleClass().add("professional-panel");
        
        // Header with file info
        headerLabel = new Label("No file selected");
        headerLabel.getStyleClass().addAll("header-subtitle");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);
        
        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        
        // Content area
        contentBox = new VBox(2);
        contentBox.setPadding(new Insets(10));
        
        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("code-scroll-pane");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(headerLabel, scrollPane);
        
        // Apply professional styling
        setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1px; -fx-border-radius: 6px;");
    }
    
    /**
     * Preview a code file with syntax highlighting
     */
    public void previewFile(String fileName, String content) {
        this.currentFileName = fileName;
        this.currentLanguage = detectLanguage(fileName);
        
        Platform.runLater(() -> {
            headerLabel.setText(String.format("📄 %s (%s) - %d lines", 
                fileName, currentLanguage.toUpperCase(), content.split("\n").length));
            
            showLoading(true);
            contentBox.getChildren().clear();
        });
        
        // Process syntax highlighting in background
        Task<Void> highlightTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String[] lines = content.split("\n");
                
                Platform.runLater(() -> {
                    try {
                        for (int i = 0; i < Math.min(lines.length, MAX_LINES_PREVIEW); i++) {
                            HBox lineBox = createLineWithHighlighting(i + 1, lines[i]);
                            contentBox.getChildren().add(lineBox);
                        }
                        
                        if (lines.length > MAX_LINES_PREVIEW) {
                            Label truncatedLabel = new Label(String.format("... (%d more lines truncated for performance)", 
                                lines.length - MAX_LINES_PREVIEW));
                            truncatedLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
                            contentBox.getChildren().add(truncatedLabel);
                        }
                        
                        showLoading(false);
                        
                    } catch (Exception e) {
                        showError("Error highlighting code: " + e.getMessage());
                    }
                });
                
                return null;
            }
        };
        
        Thread highlightThread = new Thread(highlightTask);
        highlightThread.setDaemon(true);
        highlightThread.start();
    }
    
    /**
     * Show message for unsupported files
     */
    public void showUnsupportedFile(String fileName, String reason) {
        Platform.runLater(() -> {
            headerLabel.setText("📄 " + fileName);
            contentBox.getChildren().clear();
            
            VBox messageBox = new VBox(10);
            messageBox.setStyle("-fx-alignment: center; -fx-padding: 50px;");
            
            Label iconLabel = new Label("⚠️");
            iconLabel.setStyle("-fx-font-size: 48px;");
            
            Label messageLabel = new Label("Cannot preview this file");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cccccc;");
            
            Label reasonLabel = new Label(reason);
            reasonLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
            reasonLabel.setWrapText(true);
            
            messageBox.getChildren().addAll(iconLabel, messageLabel, reasonLabel);
            contentBox.getChildren().add(messageBox);
            
            showLoading(false);
        });
    }
    
    /**
     * Create a line with syntax highlighting and line numbers
     */
    private HBox createLineWithHighlighting(int lineNumber, String lineContent) {
        HBox lineBox = new HBox(10);
        lineBox.setStyle("-fx-padding: 2px 0;");
        
        // Line number
        Label lineNumLabel = new Label(String.format("%4d", lineNumber));
        lineNumLabel.setFont(CODE_FONT);
        lineNumLabel.setStyle("-fx-text-fill: #858585; -fx-min-width: 40px; -fx-alignment: center-right;");
        
        // Code content with syntax highlighting
        TextFlow codeFlow = new TextFlow();
        codeFlow.getChildren().addAll(highlightSyntax(lineContent, currentLanguage));
        
        lineBox.getChildren().addAll(lineNumLabel, codeFlow);
        
        return lineBox;
    }
    
    /**
     * Apply syntax highlighting to a line of code
     */
    private Text[] highlightSyntax(String line, String language) {
        if (line.trim().isEmpty()) {
            Text emptyText = new Text(" ");
            emptyText.setFont(CODE_FONT);
            return new Text[]{emptyText};
        }
        
        switch (language.toLowerCase()) {
            case "java":
                return highlightJava(line);
            case "python":
                return highlightPython(line);
            case "cpp":
            case "c":
                return highlightCpp(line);
            case "javascript":
            case "typescript":
                return highlightJavaScript(line);
            case "html":
                return highlightHtml(line);
            case "css":
                return highlightCss(line);
            case "json":
                return highlightJson(line);
            case "xml":
                return highlightXml(line);
            default:
                return highlightGeneric(line);
        }
    }
    
    /**
     * Highlight Java syntax
     */
    private Text[] highlightJava(String line) {
        String[] javaKeywords = {
            "public", "private", "protected", "static", "final", "abstract", "class", "interface",
            "extends", "implements", "import", "package", "if", "else", "for", "while", "do",
            "switch", "case", "default", "break", "continue", "return", "try", "catch", "finally",
            "throw", "throws", "new", "this", "super", "null", "true", "false", "void", "int",
            "double", "float", "long", "short", "byte", "char", "boolean", "String"
        };
        
        return highlightWithKeywords(line, javaKeywords);
    }
    
    /**
     * Highlight Python syntax
     */
    private Text[] highlightPython(String line) {
        String[] pythonKeywords = {
            "def", "class", "if", "elif", "else", "for", "while", "try", "except", "finally",
            "import", "from", "as", "return", "yield", "break", "continue", "pass", "lambda",
            "and", "or", "not", "in", "is", "None", "True", "False", "self", "with", "async", "await"
        };
        
        return highlightWithKeywords(line, pythonKeywords);
    }
    
    /**
     * Highlight C/C++ syntax
     */
    private Text[] highlightCpp(String line) {
        String[] cppKeywords = {
            "int", "char", "float", "double", "void", "bool", "long", "short", "unsigned", "signed",
            "const", "static", "extern", "auto", "register", "volatile", "inline", "typedef",
            "struct", "union", "enum", "class", "public", "private", "protected", "virtual",
            "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue",
            "return", "goto", "sizeof", "new", "delete", "this", "true", "false", "nullptr",
            "#include", "#define", "#ifdef", "#ifndef", "#endif", "#pragma"
        };
        
        return highlightWithKeywords(line, cppKeywords);
    }
    
    /**
     * Highlight JavaScript/TypeScript syntax
     */
    private Text[] highlightJavaScript(String line) {
        String[] jsKeywords = {
            "var", "let", "const", "function", "class", "extends", "import", "export", "default",
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "return",
            "try", "catch", "finally", "throw", "new", "this", "super", "null", "undefined",
            "true", "false", "typeof", "instanceof", "in", "of", "async", "await", "yield"
        };
        
        return highlightWithKeywords(line, jsKeywords);
    }
    
    /**
     * Generic syntax highlighting with keywords
     */
    private Text[] highlightWithKeywords(String line, String[] keywords) {
        java.util.List<Text> texts = new java.util.ArrayList<>();
        
        // Check for comments first
        if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
            Text commentText = new Text(line);
            commentText.setFont(CODE_FONT);
            commentText.setFill(SYNTAX_COLORS.get("comment"));
            texts.add(commentText);
            return texts.toArray(new Text[0]);
        }
        
        // Split by whitespace and operators while preserving them
        String[] tokens = line.split("(\\s+|(?=[(){}\\[\\];,.])|(?<=[(){}\\[\\];,.]))");
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            
            Text text = new Text(token);
            text.setFont(CODE_FONT);
            
            // Determine token type and color
            if (isKeyword(token, keywords)) {
                text.setFill(SYNTAX_COLORS.get("keyword"));
            } else if (isString(token)) {
                text.setFill(SYNTAX_COLORS.get("string"));
            } else if (isNumber(token)) {
                text.setFill(SYNTAX_COLORS.get("number"));
            } else if (isOperator(token)) {
                text.setFill(SYNTAX_COLORS.get("operator"));
            } else {
                text.setFill(SYNTAX_COLORS.get("default"));
            }
            
            texts.add(text);
        }
        
        return texts.toArray(new Text[0]);
    }
    
    /**
     * Highlight HTML syntax
     */
    private Text[] highlightHtml(String line) {
        java.util.List<Text> texts = new java.util.ArrayList<>();
        
        // Simple HTML tag highlighting
        Pattern tagPattern = Pattern.compile("<[^>]+>");
        Matcher matcher = tagPattern.matcher(line);
        
        int lastEnd = 0;
        while (matcher.find()) {
            // Add text before tag
            if (matcher.start() > lastEnd) {
                Text beforeText = new Text(line.substring(lastEnd, matcher.start()));
                beforeText.setFont(CODE_FONT);
                beforeText.setFill(SYNTAX_COLORS.get("default"));
                texts.add(beforeText);
            }
            
            // Add tag
            Text tagText = new Text(matcher.group());
            tagText.setFont(CODE_FONT);
            tagText.setFill(SYNTAX_COLORS.get("keyword"));
            texts.add(tagText);
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text
        if (lastEnd < line.length()) {
            Text remainingText = new Text(line.substring(lastEnd));
            remainingText.setFont(CODE_FONT);
            remainingText.setFill(SYNTAX_COLORS.get("default"));
            texts.add(remainingText);
        }
        
        return texts.toArray(new Text[0]);
    }
    
    /**
     * Highlight CSS syntax
     */
    private Text[] highlightCss(String line) {
        return highlightGeneric(line); // Simplified for now
    }
    
    /**
     * Highlight JSON syntax
     */
    private Text[] highlightJson(String line) {
        return highlightGeneric(line); // Simplified for now
    }
    
    /**
     * Highlight XML syntax
     */
    private Text[] highlightXml(String line) {
        return highlightHtml(line); // Similar to HTML
    }
    
    /**
     * Generic highlighting for unknown languages
     */
    private Text[] highlightGeneric(String line) {
        Text text = new Text(line);
        text.setFont(CODE_FONT);
        text.setFill(SYNTAX_COLORS.get("default"));
        return new Text[]{text};
    }
    
    /**
     * Check if token is a keyword
     */
    private boolean isKeyword(String token, String[] keywords) {
        for (String keyword : keywords) {
            if (keyword.equals(token)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if token is a string literal
     */
    private boolean isString(String token) {
        return (token.startsWith("\"") && token.endsWith("\"")) ||
               (token.startsWith("'") && token.endsWith("'")) ||
               (token.startsWith("`") && token.endsWith("`"));
    }
    
    /**
     * Check if token is a number
     */
    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if token is an operator
     */
    private boolean isOperator(String token) {
        return token.matches("[+\\-*/=<>!&|%^~(){}\\[\\];,.]");
    }
    
    /**
     * Detect programming language from file extension
     */
    private String detectLanguage(String fileName) {
        if (fileName == null) return "text";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "text";
        
        String extension = fileName.substring(lastDot).toLowerCase();
        return LANGUAGE_EXTENSIONS.getOrDefault(extension, "text");
    }
    
    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        if (show) {
            if (!getChildren().contains(loadingIndicator)) {
                getChildren().add(1, loadingIndicator);
            }
        } else {
            getChildren().remove(loadingIndicator);
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            showLoading(false);
            
            if (!getChildren().contains(errorLabel)) {
                getChildren().add(errorLabel);
            }
        });
    }
    
    /**
     * Clear the preview
     */
    public void clear() {
        Platform.runLater(() -> {
            headerLabel.setText("No file selected");
            contentBox.getChildren().clear();
            showLoading(false);
            errorLabel.setVisible(false);
        });
    }
    
    /**
     * Check if file can be previewed as code
     */
    public static boolean canPreviewAsCode(String fileName) {
        if (fileName == null) return false;
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return false;
        
        String extension = fileName.substring(lastDot).toLowerCase();
        return LANGUAGE_EXTENSIONS.containsKey(extension);
    }
}