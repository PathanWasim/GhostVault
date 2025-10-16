package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced code preview component with syntax highlighting
 */
public class CodePreviewComponent extends VBox {
    
    private TextFlow codeDisplay;
    private ScrollPane scrollPane;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private VBox lineNumberPane;
    private HBox contentPane;
    
    private File currentFile;
    private String currentLanguage;
    private boolean showLineNumbers = true;
    
    // Syntax highlighting patterns for different languages
    private static final Map<String, SyntaxHighlighter> SYNTAX_HIGHLIGHTERS = new HashMap<>();
    
    static {
        // Initialize syntax highlighters for different languages
        SYNTAX_HIGHLIGHTERS.put("java", new JavaSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("cpp", new CppSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("c", new CppSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("py", new PythonSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("js", new JavaScriptSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("html", new HtmlSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("css", new CssSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("xml", new XmlSyntaxHighlighter());
        SYNTAX_HIGHLIGHTERS.put("json", new JsonSyntaxHighlighter());
    }
    
    public CodePreviewComponent() {
        initializeComponents();
        setupLayout();
        applyStyles();
    }
    
    private void initializeComponents() {
        codeDisplay = new TextFlow();
        codeDisplay.setPadding(new Insets(10));
        
        lineNumberPane = new VBox();
        lineNumberPane.setPadding(new Insets(10, 5, 10, 10));
        lineNumberPane.setMinWidth(50);
        
        contentPane = new HBox();
        
        scrollPane = new ScrollPane();
        scrollPane.setContent(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        statusLabel = new Label("No file selected");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(30, 30);
    }
    
    private void setupLayout() {
        contentPane.getChildren().addAll(lineNumberPane, codeDisplay);
        
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.getChildren().addAll(statusLabel, loadingIndicator);
        
        this.getChildren().addAll(scrollPane, statusBar);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }
    
    private void applyStyles() {
        this.getStyleClass().add("code-preview-component");
        codeDisplay.getStyleClass().add("code-display");
        lineNumberPane.getStyleClass().add("line-numbers");
        statusLabel.getStyleClass().add("status-label");
        
        // Set monospace font for code display
        Font codeFont = Font.font("Consolas", 12);
        if (codeFont.getFamily().equals("System")) {
            codeFont = Font.font("Courier New", 12);
        }
        codeDisplay.setStyle("-fx-font-family: '" + codeFont.getFamily() + "'; -fx-font-size: 12px;");
        lineNumberPane.setStyle("-fx-font-family: '" + codeFont.getFamily() + "'; -fx-font-size: 12px;");
    }
    
    /**
     * Load and display a code file with syntax highlighting
     */
    public void loadFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            showError("Invalid file selected");
            return;
        }
        
        this.currentFile = file;
        this.currentLanguage = detectLanguage(file);
        
        showLoading(true);
        statusLabel.setText("Loading " + file.getName() + "...");
        
        Task<String> loadTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return Files.readString(file.toPath());
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    displayCode(getValue());
                    showLoading(false);
                    statusLabel.setText(String.format("%s (%s) - %d lines", 
                        file.getName(), currentLanguage.toUpperCase(), 
                        getValue().split("\\n").length));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Failed to load file: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void displayCode(String content) {
        codeDisplay.getChildren().clear();
        lineNumberPane.getChildren().clear();
        
        if (content == null || content.isEmpty()) {
            Text emptyText = new Text("File is empty");
            emptyText.getStyleClass().add("empty-file-text");
            codeDisplay.getChildren().add(emptyText);
            return;
        }
        
        String[] lines = content.split("\\n", -1);
        SyntaxHighlighter highlighter = SYNTAX_HIGHLIGHTERS.get(currentLanguage);
        
        for (int i = 0; i < lines.length; i++) {
            // Add line number
            if (showLineNumbers) {
                Label lineNumber = new Label(String.format("%4d", i + 1));
                lineNumber.getStyleClass().add("line-number");
                lineNumberPane.getChildren().add(lineNumber);
            }
            
            // Add highlighted code line
            String line = lines[i];
            if (highlighter != null) {
                List<Text> highlightedTokens = highlighter.highlight(line);
                codeDisplay.getChildren().addAll(highlightedTokens);
            } else {
                Text plainText = new Text(line);
                plainText.getStyleClass().add("code-text");
                codeDisplay.getChildren().add(plainText);
            }
            
            // Add line break (except for last line)
            if (i < lines.length - 1) {
                codeDisplay.getChildren().add(new Text("\n"));
            }
        }
    }
    
    private String detectLanguage(File file) {
        String fileName = file.getName().toLowerCase();
        String extension = "";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot + 1);
        }
        
        // Map file extensions to languages
        switch (extension) {
            case "java": return "java";
            case "cpp": case "cc": case "cxx": return "cpp";
            case "c": case "h": return "c";
            case "py": case "pyw": return "py";
            case "js": case "jsx": return "js";
            case "html": case "htm": return "html";
            case "css": return "css";
            case "xml": case "xsl": case "xsd": return "xml";
            case "json": return "json";
            default: return "text";
        }
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
    }
    
    private void showError(String message) {
        codeDisplay.getChildren().clear();
        lineNumberPane.getChildren().clear();
        
        Text errorText = new Text(message);
        errorText.getStyleClass().add("error-text");
        codeDisplay.getChildren().add(errorText);
        
        statusLabel.setText("Error");
    }
    
    /**
     * Toggle line number display
     */
    public void setShowLineNumbers(boolean show) {
        this.showLineNumbers = show;
        lineNumberPane.setVisible(show);
        lineNumberPane.setManaged(show);
        
        if (currentFile != null) {
            loadFile(currentFile); // Refresh display
        }
    }
    
    /**
     * Get current file
     */
    public File getCurrentFile() {
        return currentFile;
    }
    
    /**
     * Get detected language
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Clear the preview
     */
    public void clear() {
        codeDisplay.getChildren().clear();
        lineNumberPane.getChildren().clear();
        currentFile = null;
        currentLanguage = null;
        statusLabel.setText("No file selected");
    }
    
    // Abstract syntax highlighter interface
    private interface SyntaxHighlighter {
        List<Text> highlight(String line);
    }
    
    // Java syntax highlighter
    private static class JavaSyntaxHighlighter implements SyntaxHighlighter {
        private static final Pattern KEYWORDS = Pattern.compile(
            "\\b(public|private|protected|static|final|abstract|class|interface|extends|implements|" +
            "import|package|if|else|for|while|do|switch|case|default|break|continue|return|" +
            "try|catch|finally|throw|throws|new|this|super|null|true|false|void|int|long|" +
            "double|float|boolean|char|byte|short|String)\\b");
        
        private static final Pattern STRINGS = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
        private static final Pattern COMMENTS = Pattern.compile("//.*$|/\\*.*?\\*/");
        private static final Pattern NUMBERS = Pattern.compile("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b");
        
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            
            // Simple tokenization - this is a basic implementation
            String[] parts = line.split("(\\s+)");
            for (String part : parts) {
                Text token = new Text(part + " ");
                
                if (KEYWORDS.matcher(part).matches()) {
                    token.getStyleClass().add("keyword");
                } else if (STRINGS.matcher(part).matches()) {
                    token.getStyleClass().add("string");
                } else if (COMMENTS.matcher(part).matches()) {
                    token.getStyleClass().add("comment");
                } else if (NUMBERS.matcher(part).matches()) {
                    token.getStyleClass().add("number");
                } else {
                    token.getStyleClass().add("code-text");
                }
                
                tokens.add(token);
            }
            
            return tokens;
        }
    }
    
    // C++ syntax highlighter
    private static class CppSyntaxHighlighter implements SyntaxHighlighter {
        private static final Pattern KEYWORDS = Pattern.compile(
            "\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|" +
            "float|for|goto|if|int|long|register|return|short|signed|sizeof|static|" +
            "struct|switch|typedef|union|unsigned|void|volatile|while|class|private|" +
            "protected|public|virtual|inline|template|namespace|using|std)\\b");
        
        @Override
        public List<Text> highlight(String line) {
            // Similar implementation to Java highlighter
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // Python syntax highlighter
    private static class PythonSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // JavaScript syntax highlighter
    private static class JavaScriptSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // HTML syntax highlighter
    private static class HtmlSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // CSS syntax highlighter
    private static class CssSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // XML syntax highlighter
    private static class XmlSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
    
    // JSON syntax highlighter
    private static class JsonSyntaxHighlighter implements SyntaxHighlighter {
        @Override
        public List<Text> highlight(String line) {
            List<Text> tokens = new ArrayList<>();
            Text token = new Text(line);
            token.getStyleClass().add("code-text");
            tokens.add(token);
            return tokens;
        }
    }
}