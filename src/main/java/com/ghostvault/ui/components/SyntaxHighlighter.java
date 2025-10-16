package com.ghostvault.ui.components;

import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple syntax highlighter for code preview
 */
public class SyntaxHighlighter {
    
    private List<SyntaxPattern> patterns;
    
    public SyntaxHighlighter() {
        patterns = new ArrayList<>();
        initializePatterns();
    }
    
    private void initializePatterns() {
        // Comments
        patterns.add(new SyntaxPattern("//.*$", "code-comment"));
        patterns.add(new SyntaxPattern("/\\*[\\s\\S]*?\\*/", "code-comment"));
        
        // Strings
        patterns.add(new SyntaxPattern("\"([^\"]|\\\\.)*\"", "code-string"));
        patterns.add(new SyntaxPattern("'([^']|\\\\.)*'", "code-string"));
        
        // Keywords
        patterns.add(new SyntaxPattern("\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b", "code-keyword"));
        
        // Annotations
        patterns.add(new SyntaxPattern("@\\w+", "code-annotation"));
        
        // Numbers
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b", "code-number"));
    }
    
    /**
     * Highlight a line of code
     */
    public List<Text> highlight(String line) {
        List<Text> result = new ArrayList<>();
        
        if (line == null || line.isEmpty()) {
            return result;
        }
        
        // Simple approach: split by spaces and apply basic highlighting
        String[] tokens = line.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Text textNode = new Text(token);
            
            // Apply styling based on token
            String styleClass = getStyleClass(token);
            textNode.getStyleClass().add(styleClass);
            
            result.add(textNode);
            
            // Add space between tokens (except for last token)
            if (i < tokens.length - 1) {
                Text space = new Text(" ");
                space.getStyleClass().add("code-text");
                result.add(space);
            }
        }
        
        return result;
    }
    
    private String getStyleClass(String token) {
        // Check for keywords
        if (isKeyword(token)) {
            return "code-keyword";
        }
        
        // Check for strings
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return "code-string";
        }
        
        // Check for comments
        if (token.startsWith("//")) {
            return "code-comment";
        }
        
        // Check for numbers
        if (token.matches("\\d+(\\.\\d+)?[fFdDlL]?")) {
            return "code-number";
        }
        
        // Check for annotations
        if (token.startsWith("@")) {
            return "code-annotation";
        }
        
        return "code-text";
    }
    
    private boolean isKeyword(String token) {
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", 
            "class", "const", "continue", "default", "do", "double", "else", "enum", 
            "extends", "final", "finally", "float", "for", "goto", "if", "implements", 
            "import", "instanceof", "int", "interface", "long", "native", "new", 
            "package", "private", "protected", "public", "return", "short", "static", 
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", 
            "transient", "try", "void", "volatile", "while"
        };
        
        for (String keyword : keywords) {
            if (keyword.equals(token)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Syntax pattern class
     */
    private static class SyntaxPattern {
        private Pattern pattern;
        private String styleClass;
        
        public SyntaxPattern(String regex, String styleClass) {
            this.pattern = Pattern.compile(regex);
            this.styleClass = styleClass;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public String getStyleClass() {
            return styleClass;
        }
    }
}