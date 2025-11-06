package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Automatic language detection for code files
 * Uses file extension and content analysis to determine programming language
 */
public class LanguageDetector {
    
    private static final Map<String, VaultFile.CodeLanguage> EXTENSION_MAP = new HashMap<>();
    private static final Map<VaultFile.CodeLanguage, Pattern[]> CONTENT_PATTERNS = new HashMap<>();
    
    static {
        initializeExtensionMap();
        initializeContentPatterns();
    }
    
    /**
     * Detect programming language from file extension and content
     */
    public static VaultFile.CodeLanguage detectLanguage(String filename, String content) {
        // First try extension-based detection
        VaultFile.CodeLanguage extensionLanguage = detectFromExtension(filename);
        
        // If extension detection is successful and not ambiguous, use it
        if (extensionLanguage != VaultFile.CodeLanguage.UNKNOWN && !isAmbiguousExtension(filename)) {
            return extensionLanguage;
        }
        
        // Try content-based detection
        VaultFile.CodeLanguage contentLanguage = detectFromContent(content);
        
        // Return content-based detection if available, otherwise extension-based
        return contentLanguage != VaultFile.CodeLanguage.UNKNOWN ? contentLanguage : extensionLanguage;
    }
    
    /**
     * Detect language from file extension
     */
    public static VaultFile.CodeLanguage detectFromExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return VaultFile.CodeLanguage.UNKNOWN;
        }
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            String extension = filename.substring(lastDot + 1).toLowerCase();
            return EXTENSION_MAP.getOrDefault(extension, VaultFile.CodeLanguage.UNKNOWN);
        }
        
        return VaultFile.CodeLanguage.UNKNOWN;
    }
    
    /**
     * Detect language from file content
     */
    public static VaultFile.CodeLanguage detectFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return VaultFile.CodeLanguage.UNKNOWN;
        }
        
        // Check for shebang line
        VaultFile.CodeLanguage shebangLanguage = detectFromShebang(content);
        if (shebangLanguage != VaultFile.CodeLanguage.UNKNOWN) {
            return shebangLanguage;
        }
        
        // Check content patterns
        for (Map.Entry<VaultFile.CodeLanguage, Pattern[]> entry : CONTENT_PATTERNS.entrySet()) {
            if (matchesContentPatterns(content, entry.getValue())) {
                return entry.getKey();
            }
        }
        
        return VaultFile.CodeLanguage.UNKNOWN;
    }
    
    /**
     * Detect language from shebang line
     */
    private static VaultFile.CodeLanguage detectFromShebang(String content) {
        if (!content.startsWith("#!")) {
            return VaultFile.CodeLanguage.UNKNOWN;
        }
        
        String firstLine = content.split("\n")[0].toLowerCase();
        
        if (firstLine.contains("python")) {
            return VaultFile.CodeLanguage.PYTHON;
        } else if (firstLine.contains("bash") || firstLine.contains("sh")) {
            return VaultFile.CodeLanguage.SHELL;
        } else if (firstLine.contains("node")) {
            return VaultFile.CodeLanguage.JAVASCRIPT;
        }
        
        return VaultFile.CodeLanguage.UNKNOWN;
    }
    
    /**
     * Check if content matches language patterns
     */
    private static boolean matchesContentPatterns(String content, Pattern[] patterns) {
        int matches = 0;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(content).find()) {
                matches++;
            }
        }
        
        // Require at least 2 pattern matches for confidence
        return matches >= Math.min(2, patterns.length);
    }
    
    /**
     * Check if file extension is ambiguous (could be multiple languages)
     */
    private static boolean isAmbiguousExtension(String filename) {
        if (filename == null) return false;
        
        String extension = getExtension(filename);
        
        // Extensions that could be multiple languages
        return "h".equals(extension) || // C or C++
               "m".equals(extension) || // Objective-C or MATLAB
               "pl".equals(extension);  // Perl or Prolog
    }
    
    /**
     * Get file extension
     */
    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Initialize extension to language mapping
     */
    private static void initializeExtensionMap() {
        // Java
        EXTENSION_MAP.put("java", VaultFile.CodeLanguage.JAVA);
        
        // Python
        EXTENSION_MAP.put("py", VaultFile.CodeLanguage.PYTHON);
        EXTENSION_MAP.put("pyw", VaultFile.CodeLanguage.PYTHON);
        EXTENSION_MAP.put("pyi", VaultFile.CodeLanguage.PYTHON);
        
        // JavaScript/TypeScript
        EXTENSION_MAP.put("js", VaultFile.CodeLanguage.JAVASCRIPT);
        EXTENSION_MAP.put("jsx", VaultFile.CodeLanguage.JAVASCRIPT);
        EXTENSION_MAP.put("ts", VaultFile.CodeLanguage.TYPESCRIPT);
        EXTENSION_MAP.put("tsx", VaultFile.CodeLanguage.TYPESCRIPT);
        
        // Web languages
        EXTENSION_MAP.put("html", VaultFile.CodeLanguage.HTML);
        EXTENSION_MAP.put("htm", VaultFile.CodeLanguage.HTML);
        EXTENSION_MAP.put("css", VaultFile.CodeLanguage.CSS);
        EXTENSION_MAP.put("scss", VaultFile.CodeLanguage.CSS);
        EXTENSION_MAP.put("sass", VaultFile.CodeLanguage.CSS);
        EXTENSION_MAP.put("less", VaultFile.CodeLanguage.CSS);
        
        // Data formats
        EXTENSION_MAP.put("json", VaultFile.CodeLanguage.JSON);
        EXTENSION_MAP.put("xml", VaultFile.CodeLanguage.XML);
        EXTENSION_MAP.put("yaml", VaultFile.CodeLanguage.YAML);
        EXTENSION_MAP.put("yml", VaultFile.CodeLanguage.YAML);
        
        // Database
        EXTENSION_MAP.put("sql", VaultFile.CodeLanguage.SQL);
        
        // Shell scripts
        EXTENSION_MAP.put("sh", VaultFile.CodeLanguage.SHELL);
        EXTENSION_MAP.put("bash", VaultFile.CodeLanguage.SHELL);
        EXTENSION_MAP.put("zsh", VaultFile.CodeLanguage.SHELL);
        EXTENSION_MAP.put("fish", VaultFile.CodeLanguage.SHELL);
        
        // Batch files
        EXTENSION_MAP.put("bat", VaultFile.CodeLanguage.BATCH);
        EXTENSION_MAP.put("cmd", VaultFile.CodeLanguage.BATCH);
        
        // PowerShell
        EXTENSION_MAP.put("ps1", VaultFile.CodeLanguage.POWERSHELL);
        EXTENSION_MAP.put("psm1", VaultFile.CodeLanguage.POWERSHELL);
        
        // Markdown
        EXTENSION_MAP.put("md", VaultFile.CodeLanguage.MARKDOWN);
        EXTENSION_MAP.put("markdown", VaultFile.CodeLanguage.MARKDOWN);
        
        // Plain text
        EXTENSION_MAP.put("txt", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("text", VaultFile.CodeLanguage.PLAIN_TEXT);
        
        // Configuration files
        EXTENSION_MAP.put("ini", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("cfg", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("conf", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("config", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("properties", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("toml", VaultFile.CodeLanguage.PLAIN_TEXT);
        
        // Additional languages
        EXTENSION_MAP.put("c", VaultFile.CodeLanguage.PLAIN_TEXT); // Could be enhanced to C language
        EXTENSION_MAP.put("cpp", VaultFile.CodeLanguage.PLAIN_TEXT); // Could be enhanced to C++ language
        EXTENSION_MAP.put("h", VaultFile.CodeLanguage.PLAIN_TEXT); // Header files
        EXTENSION_MAP.put("hpp", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("cs", VaultFile.CodeLanguage.PLAIN_TEXT); // C#
        EXTENSION_MAP.put("php", VaultFile.CodeLanguage.PLAIN_TEXT);
        EXTENSION_MAP.put("rb", VaultFile.CodeLanguage.PLAIN_TEXT); // Ruby
        EXTENSION_MAP.put("go", VaultFile.CodeLanguage.PLAIN_TEXT); // Go
        EXTENSION_MAP.put("rs", VaultFile.CodeLanguage.PLAIN_TEXT); // Rust
    }
    
    /**
     * Initialize content patterns for language detection
     */
    private static void initializeContentPatterns() {
        // Java patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.JAVA, new Pattern[]{
            Pattern.compile("\\bpublic\\s+class\\s+\\w+"),
            Pattern.compile("\\bimport\\s+java\\."),
            Pattern.compile("\\bpublic\\s+static\\s+void\\s+main"),
            Pattern.compile("\\bSystem\\.out\\.print")
        });
        
        // Python patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.PYTHON, new Pattern[]{
            Pattern.compile("\\bdef\\s+\\w+\\s*\\("),
            Pattern.compile("\\bimport\\s+\\w+"),
            Pattern.compile("\\bfrom\\s+\\w+\\s+import"),
            Pattern.compile("\\bif\\s+__name__\\s*==\\s*['\"]__main__['\"]")
        });
        
        // JavaScript patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.JAVASCRIPT, new Pattern[]{
            Pattern.compile("\\bfunction\\s+\\w+\\s*\\("),
            Pattern.compile("\\bvar\\s+\\w+\\s*="),
            Pattern.compile("\\blet\\s+\\w+\\s*="),
            Pattern.compile("\\bconsole\\.log\\s*\\(")
        });
        
        // HTML patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.HTML, new Pattern[]{
            Pattern.compile("<!DOCTYPE\\s+html>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<html[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<head[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<body[^>]*>", Pattern.CASE_INSENSITIVE)
        });
        
        // CSS patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.CSS, new Pattern[]{
            Pattern.compile("[.#]?\\w+\\s*\\{[^}]*\\}"),
            Pattern.compile("\\w+\\s*:\\s*[^;]+;"),
            Pattern.compile("@media\\s+[^{]+\\{"),
            Pattern.compile("@import\\s+['\"][^'\"]+['\"];")
        });
        
        // JSON patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.JSON, new Pattern[]{
            Pattern.compile("^\\s*\\{"),
            Pattern.compile("\"\\w+\"\\s*:\\s*"),
            Pattern.compile("\\[\\s*\\{"),
            Pattern.compile("\\}\\s*,\\s*\\{")
        });
        
        // XML patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.XML, new Pattern[]{
            Pattern.compile("<?xml\\s+version", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\w+[^>]*>[^<]*</\\w+>"),
            Pattern.compile("<\\w+[^>]*/\\s*>"),
            Pattern.compile("<!--[^>]*-->")
        });
        
        // YAML patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.YAML, new Pattern[]{
            Pattern.compile("^\\w+:\\s*$", Pattern.MULTILINE),
            Pattern.compile("^\\s*-\\s+\\w+"),
            Pattern.compile("^---\\s*$", Pattern.MULTILINE),
            Pattern.compile("^\\w+:\\s*[^\\n]+$", Pattern.MULTILINE)
        });
        
        // SQL patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.SQL, new Pattern[]{
            Pattern.compile("\\bSELECT\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bFROM\\s+\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bINSERT\\s+INTO", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bCREATE\\s+TABLE", Pattern.CASE_INSENSITIVE)
        });
        
        // Shell script patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.SHELL, new Pattern[]{
            Pattern.compile("^#!/bin/(ba)?sh"),
            Pattern.compile("\\becho\\s+"),
            Pattern.compile("\\bif\\s+\\["),
            Pattern.compile("\\$\\{?\\w+\\}?")
        });
        
        // Markdown patterns
        CONTENT_PATTERNS.put(VaultFile.CodeLanguage.MARKDOWN, new Pattern[]{
            Pattern.compile("^#{1,6}\\s+", Pattern.MULTILINE),
            Pattern.compile("\\*\\*[^*]+\\*\\*"),
            Pattern.compile("\\[[^\\]]+\\]\\([^)]+\\)"),
            Pattern.compile("^```\\w*$", Pattern.MULTILINE)
        });
    }
    
    /**
     * Get confidence score for language detection
     */
    public static double getConfidenceScore(String filename, String content, VaultFile.CodeLanguage detectedLanguage) {
        if (detectedLanguage == VaultFile.CodeLanguage.UNKNOWN) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // Extension match adds confidence
        VaultFile.CodeLanguage extensionLanguage = detectFromExtension(filename);
        if (extensionLanguage == detectedLanguage) {
            score += 0.5;
        }
        
        // Content pattern matches add confidence
        Pattern[] patterns = CONTENT_PATTERNS.get(detectedLanguage);
        if (patterns != null && content != null) {
            int matches = 0;
            for (Pattern pattern : patterns) {
                if (pattern.matcher(content).find()) {
                    matches++;
                }
            }
            score += (double) matches / patterns.length * 0.5;
        }
        
        return Math.min(1.0, score);
    }
}