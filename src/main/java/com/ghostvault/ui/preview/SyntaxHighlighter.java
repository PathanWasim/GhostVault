package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced syntax highlighting engine for code files
 * Provides language-specific highlighting rules and themes
 */
public class SyntaxHighlighter {
    
    private final Map<VaultFile.CodeLanguage, LanguageDefinition> languageDefinitions;
    private SyntaxTheme currentTheme;
    
    public SyntaxHighlighter() {
        this.languageDefinitions = new HashMap<>();
        this.currentTheme = SyntaxTheme.DARK;
        initializeLanguageDefinitions();
    }
    
    /**
     * Apply syntax highlighting to code area
     */
    public void applySyntaxHighlighting(CodeArea codeArea, VaultFile.CodeLanguage language) {
        if (codeArea == null || language == VaultFile.CodeLanguage.UNKNOWN) {
            return;
        }
        
        String text = codeArea.getText();
        if (text.isEmpty()) {
            return;
        }
        
        LanguageDefinition langDef = languageDefinitions.get(language);
        if (langDef == null) {
            return;
        }
        
        StyleSpans<Collection<String>> highlighting = computeHighlighting(text, langDef);
        codeArea.setStyleSpans(0, highlighting);
    }
    
    /**
     * Compute syntax highlighting for text
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text, LanguageDefinition langDef) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        Pattern pattern = langDef.getPattern();
        Matcher matcher = pattern.matcher(text);
        
        int lastKwEnd = 0;
        while (matcher.find()) {
            String styleClass = getStyleClass(matcher, langDef);
            
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        
        return spansBuilder.create();
    }
    
    /**
     * Get style class for matched group
     */
    private String getStyleClass(Matcher matcher, LanguageDefinition langDef) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
            if (matcher.group(i) != null) {
                return langDef.getStyleClasses().get(i - 1);
            }
        }
        return "default";
    }
    
    /**
     * Initialize language definitions
     */
    private void initializeLanguageDefinitions() {
        // Java
        languageDefinitions.put(VaultFile.CodeLanguage.JAVA, createJavaDefinition());
        
        // Python  
        languageDefinitions.put(VaultFile.CodeLanguage.PYTHON, createPythonDefinition());
        
        // JavaScript
        languageDefinitions.put(VaultFile.CodeLanguage.JAVASCRIPT, createJavaScriptDefinition());
        
        // HTML
        languageDefinitions.put(VaultFile.CodeLanguage.HTML, createHtmlDefinition());
        
        // CSS
        languageDefinitions.put(VaultFile.CodeLanguage.CSS, createCssDefinition());
        
        // Additional languages
        languageDefinitions.put(VaultFile.CodeLanguage.JSON, createJsonDefinition());
        languageDefinitions.put(VaultFile.CodeLanguage.XML, createXmlDefinition());
        languageDefinitions.put(VaultFile.CodeLanguage.YAML, createYamlDefinition());
        languageDefinitions.put(VaultFile.CodeLanguage.SQL, createSqlDefinition());
        languageDefinitions.put(VaultFile.CodeLanguage.SHELL, createShellDefinition());
        languageDefinitions.put(VaultFile.CodeLanguage.MARKDOWN, createMarkdownDefinition());
    }
    
    /**
     * Create Java language definition
     */
    private LanguageDefinition createJavaDefinition() {
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null"
        };
        
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String stringPattern = "\"([^\"\\\\]|\\\\.)*\"";
        String commentPattern = "//[^\r\n]*" + "|" + "/\\*[\\s\\S]*?\\*/";
        String numberPattern = "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fFdD]?\\b";
        String annotationPattern = "@\\w+";
        
        Pattern pattern = Pattern.compile(
            "(?<KEYWORD>" + keywordPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<NUMBER>" + numberPattern + ")" +
            "|(?<ANNOTATION>" + annotationPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("keyword", "string", "comment", "number", "annotation");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create Python language definition
     */
    private LanguageDefinition createPythonDefinition() {
        String[] keywords = {
            "and", "as", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except",
            "exec", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "not",
            "or", "pass", "print", "raise", "return", "try", "while", "with", "yield", "True", "False", "None"
        };
        
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String stringPattern = "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
        String commentPattern = "#[^\r\n]*";
        String numberPattern = "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b";
        String decoratorPattern = "@\\w+";
        
        Pattern pattern = Pattern.compile(
            "(?<KEYWORD>" + keywordPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<NUMBER>" + numberPattern + ")" +
            "|(?<DECORATOR>" + decoratorPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("keyword", "string", "comment", "number", "decorator");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create JavaScript language definition
     */
    private LanguageDefinition createJavaScriptDefinition() {
        String[] keywords = {
            "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do",
            "else", "export", "extends", "finally", "for", "function", "if", "import", "in", "instanceof",
            "let", "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void",
            "while", "with", "yield", "true", "false", "null", "undefined"
        };
        
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String stringPattern = "`[^`]*`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
        String commentPattern = "//[^\r\n]*" + "|" + "/\\*[\\s\\S]*?\\*/";
        String numberPattern = "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b";
        String regexPattern = "/[^/\\n]+/[gimuy]*";
        
        Pattern pattern = Pattern.compile(
            "(?<KEYWORD>" + keywordPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<NUMBER>" + numberPattern + ")" +
            "|(?<REGEX>" + regexPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("keyword", "string", "comment", "number", "regex");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create HTML language definition
     */
    private LanguageDefinition createHtmlDefinition() {
        String tagPattern = "</?\\b[a-zA-Z][a-zA-Z0-9]*\\b[^>]*>";
        String attributePattern = "\\b[a-zA-Z-]+(?=\\s*=)";
        String stringPattern = "\"[^\"]*\"|'[^']*'";
        String commentPattern = "<!--[\\s\\S]*?-->";
        String doctypePattern = "<!DOCTYPE[^>]*>";
        
        Pattern pattern = Pattern.compile(
            "(?<TAG>" + tagPattern + ")" +
            "|(?<ATTRIBUTE>" + attributePattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<DOCTYPE>" + doctypePattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("tag", "attribute", "string", "comment", "doctype");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create CSS language definition
     */
    private LanguageDefinition createCssDefinition() {
        String selectorPattern = "[.#]?[a-zA-Z][a-zA-Z0-9-]*(?=\\s*\\{)|[a-zA-Z][a-zA-Z0-9-]*(?=\\s*,)|[a-zA-Z][a-zA-Z0-9-]*(?=\\s*\\{)";
        String propertyPattern = "\\b[a-zA-Z-]+(?=\\s*:)";
        String valuePattern = ":\\s*[^;]+";
        String commentPattern = "/\\*[\\s\\S]*?\\*/";
        String colorPattern = "#[0-9a-fA-F]{3,6}\\b";
        
        Pattern pattern = Pattern.compile(
            "(?<SELECTOR>" + selectorPattern + ")" +
            "|(?<PROPERTY>" + propertyPattern + ")" +
            "|(?<VALUE>" + valuePattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<COLOR>" + colorPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("selector", "property", "value", "comment", "color");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create JSON language definition
     */
    private LanguageDefinition createJsonDefinition() {
        String keyPattern = "\"[^\"]+\"(?=\\s*:)";
        String stringPattern = "\"[^\"]*\"";
        String numberPattern = "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b";
        String booleanPattern = "\\b(true|false|null)\\b";
        
        Pattern pattern = Pattern.compile(
            "(?<KEY>" + keyPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<NUMBER>" + numberPattern + ")" +
            "|(?<BOOLEAN>" + booleanPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("key", "string", "number", "boolean");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create XML language definition
     */
    private LanguageDefinition createXmlDefinition() {
        String tagPattern = "</?\\b[a-zA-Z][a-zA-Z0-9]*\\b[^>]*>";
        String attributePattern = "\\b[a-zA-Z-]+(?=\\s*=)";
        String stringPattern = "\"[^\"]*\"|'[^']*'";
        String commentPattern = "<!--[\\s\\S]*?-->";
        String cdataPattern = "<!\\[CDATA\\[[\\s\\S]*?\\]\\]>";
        
        Pattern pattern = Pattern.compile(
            "(?<TAG>" + tagPattern + ")" +
            "|(?<ATTRIBUTE>" + attributePattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<CDATA>" + cdataPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("tag", "attribute", "string", "comment", "cdata");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create YAML language definition
     */
    private LanguageDefinition createYamlDefinition() {
        String keyPattern = "^\\s*\\w+(?=\\s*:)";
        String stringPattern = "\"[^\"]*\"|'[^']*'";
        String commentPattern = "#[^\r\n]*";
        String separatorPattern = "^---\\s*$";
        String listPattern = "^\\s*-\\s+";
        
        Pattern pattern = Pattern.compile(
            "(?<KEY>" + keyPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<SEPARATOR>" + separatorPattern + ")" +
            "|(?<LIST>" + listPattern + ")",
            Pattern.MULTILINE
        );
        
        List<String> styleClasses = Arrays.asList("key", "string", "comment", "separator", "list");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create SQL language definition
     */
    private LanguageDefinition createSqlDefinition() {
        String[] keywords = {
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
            "TABLE", "INDEX", "VIEW", "DATABASE", "SCHEMA", "JOIN", "INNER", "LEFT", "RIGHT",
            "OUTER", "ON", "GROUP", "BY", "ORDER", "HAVING", "UNION", "DISTINCT", "AS", "AND",
            "OR", "NOT", "NULL", "TRUE", "FALSE", "LIKE", "IN", "EXISTS", "BETWEEN"
        };
        
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String stringPattern = "'[^']*'";
        String commentPattern = "--[^\r\n]*" + "|" + "/\\*[\\s\\S]*?\\*/";
        String numberPattern = "\\b\\d+(\\.\\d+)?\\b";
        String identifierPattern = "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b";
        
        Pattern pattern = Pattern.compile(
            "(?<KEYWORD>" + keywordPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<NUMBER>" + numberPattern + ")" +
            "|(?<IDENTIFIER>" + identifierPattern + ")",
            Pattern.CASE_INSENSITIVE
        );
        
        List<String> styleClasses = Arrays.asList("keyword", "string", "comment", "number", "identifier");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create Shell script language definition
     */
    private LanguageDefinition createShellDefinition() {
        String[] keywords = {
            "if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac",
            "function", "return", "exit", "break", "continue", "local", "export", "readonly",
            "declare", "typeset", "unset", "shift", "eval", "exec", "source", "alias"
        };
        
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String stringPattern = "\"[^\"]*\"|'[^']*'";
        String commentPattern = "#[^\r\n]*";
        String variablePattern = "\\$\\{?[a-zA-Z_][a-zA-Z0-9_]*\\}?";
        String commandPattern = "\\b[a-zA-Z_][a-zA-Z0-9_-]*(?=\\s|$)";
        
        Pattern pattern = Pattern.compile(
            "(?<KEYWORD>" + keywordPattern + ")" +
            "|(?<STRING>" + stringPattern + ")" +
            "|(?<COMMENT>" + commentPattern + ")" +
            "|(?<VARIABLE>" + variablePattern + ")" +
            "|(?<COMMAND>" + commandPattern + ")"
        );
        
        List<String> styleClasses = Arrays.asList("keyword", "string", "comment", "variable", "command");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Create Markdown language definition
     */
    private LanguageDefinition createMarkdownDefinition() {
        String headerPattern = "^#{1,6}\\s+.*$";
        String boldPattern = "\\*\\*[^*]+\\*\\*";
        String italicPattern = "\\*[^*]+\\*";
        String codePattern = "`[^`]+`";
        String linkPattern = "\\[[^\\]]+\\]\\([^)]+\\)";
        String codeBlockPattern = "^```[\\s\\S]*?^```";
        
        Pattern pattern = Pattern.compile(
            "(?<HEADER>" + headerPattern + ")" +
            "|(?<BOLD>" + boldPattern + ")" +
            "|(?<ITALIC>" + italicPattern + ")" +
            "|(?<CODE>" + codePattern + ")" +
            "|(?<LINK>" + linkPattern + ")" +
            "|(?<CODEBLOCK>" + codeBlockPattern + ")",
            Pattern.MULTILINE
        );
        
        List<String> styleClasses = Arrays.asList("header", "bold", "italic", "code", "link", "codeblock");
        
        return new LanguageDefinition(pattern, styleClasses);
    }
    
    /**
     * Set syntax theme
     */
    public void setTheme(SyntaxTheme theme) {
        this.currentTheme = theme != null ? theme : SyntaxTheme.DARK;
    }
    
    /**
     * Get current theme
     */
    public SyntaxTheme getTheme() {
        return currentTheme;
    }
    
    /**
     * Get CSS stylesheet for current theme
     */
    public String getThemeStylesheet() {
        return currentTheme.getStylesheet();
    }
    
    /**
     * Language definition class
     */
    private static class LanguageDefinition {
        private final Pattern pattern;
        private final List<String> styleClasses;
        
        public LanguageDefinition(Pattern pattern, List<String> styleClasses) {
            this.pattern = pattern;
            this.styleClasses = styleClasses;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public List<String> getStyleClasses() {
            return styleClasses;
        }
    }
}