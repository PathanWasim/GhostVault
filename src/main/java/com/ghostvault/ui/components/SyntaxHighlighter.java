package com.ghostvault.ui.components;

import javafx.scene.text.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Base interface for syntax highlighters
 */
public interface SyntaxHighlighter {
    List<Text> highlight(String code);
}

/**
 * Base syntax highlighter with common functionality
 */
abstract class BaseSyntaxHighlighter implements SyntaxHighlighter {
    
    protected static class SyntaxPattern {
        final Pattern pattern;
        final String styleClass;
        
        public SyntaxPattern(String regex, String styleClass) {
            this.pattern = Pattern.compile(regex);
            this.styleClass = styleClass;
        }
    }
    
    protected abstract List<SyntaxPattern> getPatterns();
    
    @Override
    public List<Text> highlight(String code) {
        List<Text> result = new ArrayList<>();
        String[] lines = code.split("\\r?\\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            result.addAll(highlightLine(line));
            
            // Add newline except for last line
            if (i < lines.length - 1) {
                Text newline = new Text("\\n");
                newline.getStyleClass().add("code-plain-text");
                result.add(newline);
            }
        }
        
        return result;
    }
    
    protected List<Text> highlightLine(String line) {
        List<Text> result = new ArrayList<>();
        List<SyntaxPattern> patterns = getPatterns();
        
        int position = 0;
        
        while (position < line.length()) {
            boolean matched = false;
            
            // Try each pattern
            for (SyntaxPattern syntaxPattern : patterns) {
                Matcher matcher = syntaxPattern.pattern.matcher(line);
                matcher.region(position, line.length());
                
                if (matcher.lookingAt()) {
                    String matchedText = matcher.group();
                    Text text = new Text(matchedText);
                    text.getStyleClass().add(syntaxPattern.styleClass);
                    TypographyManager.applyMonospace(text);
                    result.add(text);
                    
                    position = matcher.end();
                    matched = true;
                    break;
                }
            }
            
            // If no pattern matched, add single character as plain text
            if (!matched) {
                Text text = new Text(String.valueOf(line.charAt(position)));
                text.getStyleClass().add("code-plain-text");
                TypographyManager.applyMonospace(text);
                result.add(text);
                position++;
            }
        }
        
        return result;
    }
}

/**
 * Java syntax highlighter
 */
class JavaSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("//.*", "code-comment"));
        patterns.add(new SyntaxPattern("/\\*[\\s\\S]*?\\*/", "code-comment"));
        
        // Strings
        patterns.add(new SyntaxPattern("\\"([^\\"]|\\\\.)*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'([^']|\\\\.)*'", "code-string"));
        
        // Keywords
        patterns.add(new SyntaxPattern("\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b", "code-keyword"));
        
        // Annotations
        patterns.add(new SyntaxPattern("@\\w+", "code-annotation"));
        
        // Numbers
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b", "code-number"));
        patterns.add(new SyntaxPattern("\\b0x[0-9a-fA-F]+[lL]?\\b", "code-number"));
        
        // Types (capitalized words)
        patterns.add(new SyntaxPattern("\\b[A-Z][a-zA-Z0-9_]*\\b", "code-type"));
        
        // Operators
        patterns.add(new SyntaxPattern("[+\\-*/%=<>!&|^~?:;,.]", "code-operator"));
        patterns.add(new SyntaxPattern("[()\\[\\]{}]", "code-operator"));
        
        return patterns;
    }
}

/**
 * Python syntax highlighter
 */
class PythonSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("#.*", "code-comment"));
        
        // Strings
        patterns.add(new SyntaxPattern("\\"\\"\\"[\\s\\S]*?\\"\\"\\"", "code-string"));
        patterns.add(new SyntaxPattern("'''[\\s\\S]*?'''", "code-string"));
        patterns.add(new SyntaxPattern("\\"([^\\"]|\\\\.)*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'([^']|\\\\.)*'", "code-string"));
        
        // Keywords
        patterns.add(new SyntaxPattern("\\b(and|as|assert|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|not|or|pass|print|raise|return|try|while|with|yield|True|False|None)\\b", "code-keyword"));
        
        // Decorators
        patterns.add(new SyntaxPattern("@\\w+", "code-annotation"));
        
        // Numbers
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?[jJ]?\\b", "code-number"));
        patterns.add(new SyntaxPattern("\\b0x[0-9a-fA-F]+\\b", "code-number"));
        
        // Built-in functions
        patterns.add(new SyntaxPattern("\\b(abs|all|any|bin|bool|chr|dict|dir|enumerate|eval|filter|float|hex|int|len|list|map|max|min|oct|ord|range|repr|reversed|round|set|sorted|str|sum|tuple|type|zip)\\b", "code-type"));
        
        // Operators
        patterns.add(new SyntaxPattern("[+\\-*/%=<>!&|^~]", "code-operator"));
        patterns.add(new SyntaxPattern("[()\\[\\]{}:;,.]", "code-operator"));
        
        return patterns;
    }
}

/**
 * JavaScript syntax highlighter
 */
class JavaScriptSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("//.*", "code-comment"));
        patterns.add(new SyntaxPattern("/\\*[\\s\\S]*?\\*/", "code-comment"));
        
        // Strings
        patterns.add(new SyntaxPattern("`([^`]|\\\\.)*`", "code-string"));
        patterns.add(new SyntaxPattern("\\"([^\\"]|\\\\.)*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'([^']|\\\\.)*'", "code-string"));
        
        // Keywords
        patterns.add(new SyntaxPattern("\\b(async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|export|extends|finally|for|function|if|import|in|instanceof|let|new|return|super|switch|this|throw|try|typeof|var|void|while|with|yield)\\b", "code-keyword"));
        
        // Built-in objects
        patterns.add(new SyntaxPattern("\\b(Array|Boolean|Date|Error|Function|JSON|Math|Number|Object|RegExp|String|console|document|window)\\b", "code-type"));
        
        // Numbers
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?\\b", "code-number"));
        patterns.add(new SyntaxPattern("\\b0x[0-9a-fA-F]+\\b", "code-number"));
        
        // Operators
        patterns.add(new SyntaxPattern("[+\\-*/%=<>!&|^~?:;,.]", "code-operator"));
        patterns.add(new SyntaxPattern("[()\\[\\]{}]", "code-operator"));
        
        return patterns;
    }
}

/**
 * C++ syntax highlighter
 */
class CppSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("//.*", "code-comment"));
        patterns.add(new SyntaxPattern("/\\*[\\s\\S]*?\\*/", "code-comment"));
        
        // Strings
        patterns.add(new SyntaxPattern("\\"([^\\"]|\\\\.)*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'([^']|\\\\.)*'", "code-string"));
        
        // Preprocessor directives
        patterns.add(new SyntaxPattern("#\\w+", "code-annotation"));
        
        // Keywords
        patterns.add(new SyntaxPattern("\\b(auto|bool|break|case|catch|char|class|const|continue|default|delete|do|double|else|enum|explicit|extern|false|float|for|friend|goto|if|inline|int|long|mutable|namespace|new|operator|private|protected|public|register|return|short|signed|sizeof|static|struct|switch|template|this|throw|true|try|typedef|typename|union|unsigned|using|virtual|void|volatile|while)\\b", "code-keyword"));
        
        // Numbers
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?[fFlL]?\\b", "code-number"));
        patterns.add(new SyntaxPattern("\\b0x[0-9a-fA-F]+[lL]?\\b", "code-number"));
        
        // Types
        patterns.add(new SyntaxPattern("\\b[A-Z][a-zA-Z0-9_]*\\b", "code-type"));
        
        // Operators
        patterns.add(new SyntaxPattern("[+\\-*/%=<>!&|^~?:;,.]", "code-operator"));
        patterns.add(new SyntaxPattern("[()\\[\\]{}]", "code-operator"));
        
        return patterns;
    }
}

/**
 * HTML syntax highlighter
 */
class HtmlSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("<!--[\\s\\S]*?-->", "code-comment"));
        
        // Tags
        patterns.add(new SyntaxPattern("</?\\w+", "code-keyword"));
        patterns.add(new SyntaxPattern("/>|>", "code-keyword"));
        
        // Attributes
        patterns.add(new SyntaxPattern("\\w+(?==)", "code-type"));
        
        // Strings (attribute values)
        patterns.add(new SyntaxPattern("\\"[^\\\"]*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'[^']*'", "code-string"));
        
        // Operators
        patterns.add(new SyntaxPattern("=", "code-operator"));
        
        return patterns;
    }
}

/**
 * CSS syntax highlighter
 */
class CssSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("/\\*[\\s\\S]*?\\*/", "code-comment"));
        
        // Selectors
        patterns.add(new SyntaxPattern("[.#]?[a-zA-Z][a-zA-Z0-9_-]*(?=\\s*[{,])", "code-keyword"));
        
        // Properties
        patterns.add(new SyntaxPattern("[a-zA-Z-]+(?=\\s*:)", "code-type"));
        
        // Strings
        patterns.add(new SyntaxPattern("\\"[^\\\"]*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'[^']*'", "code-string"));
        
        // Numbers and units
        patterns.add(new SyntaxPattern("\\b\\d+(\\.\\d+)?(px|em|rem|%|vh|vw|pt|pc|in|cm|mm)?\\b", "code-number"));
        
        // Colors
        patterns.add(new SyntaxPattern("#[0-9a-fA-F]{3,6}\\b", "code-number"));
        
        // Operators
        patterns.add(new SyntaxPattern("[{}:;,]", "code-operator"));
        
        return patterns;
    }
}

/**
 * XML syntax highlighter
 */
class XmlSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Comments
        patterns.add(new SyntaxPattern("<!--[\\s\\S]*?-->", "code-comment"));
        
        // CDATA
        patterns.add(new SyntaxPattern("<!\\[CDATA\\[[\\s\\S]*?\\]\\]>", "code-string"));
        
        // Processing instructions
        patterns.add(new SyntaxPattern("<\\?[\\s\\S]*?\\?>", "code-annotation"));
        
        // Tags
        patterns.add(new SyntaxPattern("</?\\w+", "code-keyword"));
        patterns.add(new SyntaxPattern("/>|>", "code-keyword"));
        
        // Attributes
        patterns.add(new SyntaxPattern("\\w+(?==)", "code-type"));
        
        // Strings
        patterns.add(new SyntaxPattern("\\"[^\\\"]*\\"", "code-string"));
        patterns.add(new SyntaxPattern("'[^']*'", "code-string"));
        
        // Operators
        patterns.add(new SyntaxPattern("=", "code-operator"));
        
        return patterns;
    }
}

/**
 * JSON syntax highlighter
 */
class JsonSyntaxHighlighter extends BaseSyntaxHighlighter {
    
    @Override
    protected List<SyntaxPattern> getPatterns() {
        List<SyntaxPattern> patterns = new ArrayList<>();
        
        // Strings (keys and values)
        patterns.add(new SyntaxPattern("\\"[^\\\"]*\\"", "code-string"));
        
        // Numbers
        patterns.add(new SyntaxPattern("-?\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b", "code-number"));
        
        // Keywords (boolean and null)
        patterns.add(new SyntaxPattern("\\b(true|false|null)\\b", "code-keyword"));
        
        // Operators
        patterns.add(new SyntaxPattern("[{}\\[\\]:,]", "code-operator"));
        
        return patterns;
    }
}