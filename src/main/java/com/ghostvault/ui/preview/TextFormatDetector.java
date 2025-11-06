package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import java.util.regex.Pattern;

/**
 * Advanced text format detection utility
 * Detects various text formats including JSON, XML, YAML, CSV, etc.
 */
public class TextFormatDetector {
    
    // Regex patterns for format detection
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "^\\s*[\\[\\{].*[\\]\\}]\\s*$", Pattern.DOTALL);
    
    private static final Pattern XML_PATTERN = Pattern.compile(
        "^\\s*<\\?xml.*\\?>.*|^\\s*<[a-zA-Z][^>]*>.*</[a-zA-Z][^>]*>\\s*$", 
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    
    private static final Pattern YAML_PATTERN = Pattern.compile(
        "^\\s*---\\s*$|^\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*:\\s*.*$", 
        Pattern.MULTILINE);
    
    private static final Pattern INI_PATTERN = Pattern.compile(
        "^\\s*\\[[^\\]]+\\]\\s*$|^\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*=.*$", 
        Pattern.MULTILINE);
    
    private static final Pattern TOML_PATTERN = Pattern.compile(
        "^\\s*\\[\\[?[^\\]]+\\]\\]?\\s*$|^\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*=.*$", 
        Pattern.MULTILINE);
    
    private static final Pattern PROPERTIES_PATTERN = Pattern.compile(
        "^\\s*[a-zA-Z_][a-zA-Z0-9_.]*\\s*[=:].*$|^\\s*#.*$", 
        Pattern.MULTILINE);
    
    private static final Pattern CSV_PATTERN = Pattern.compile(
        "^[^,\\n]*,[^,\\n]*", Pattern.MULTILINE);
    
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile(
        "^\\s*#{1,6}\\s+.*$|^\\s*\\*\\s+.*$|^\\s*-\\s+.*$|^\\s*\\d+\\.\\s+.*$|" +
        "^\\s*>\\s+.*$|^\\s*```.*$|^\\s*\\[.*\\]\\(.*\\).*$", 
        Pattern.MULTILINE);
    
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}|^\\d{2}/\\d{2}/\\d{4}|" +
        "\\b(ERROR|WARN|INFO|DEBUG|TRACE|FATAL)\\b|" +
        "\\b\\d{2}:\\d{2}:\\d{2}\\b", 
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    
    private static final Pattern SQL_PATTERN = Pattern.compile(
        "\\b(SELECT|INSERT|UPDATE|DELETE|CREATE|DROP|ALTER|FROM|WHERE|JOIN)\\b", 
        Pattern.CASE_INSENSITIVE);
    
    private static final Pattern SHELL_PATTERN = Pattern.compile(
        "^\\s*#!/bin/(bash|sh)|^\\s*#.*$|\\$\\{?[A-Z_][A-Z0-9_]*\\}?|" +
        "\\b(if|then|else|fi|for|while|do|done|case|esac|function)\\b", 
        Pattern.MULTILINE);
    
    private static final Pattern BATCH_PATTERN = Pattern.compile(
        "^\\s*@echo\\s+(off|on)|^\\s*rem\\s+.*$|^\\s*::\\s*.*$|" +
        "\\b(if|else|endif|for|goto|call|set)\\b|%[A-Z_][A-Z0-9_]*%", 
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    
    /**
     * Detect text format from content and file information
     */
    public static TextFormat detectFormat(String content, VaultFile file) {
        if (content == null || content.trim().isEmpty()) {
            return TextFormat.PLAIN_TEXT;
        }
        
        // First try extension-based detection
        TextFormat extensionFormat = detectByExtension(file);
        if (extensionFormat != TextFormat.UNKNOWN) {
            // Verify with content analysis
            if (isFormatConsistent(content, extensionFormat)) {
                return extensionFormat;
            }
        }
        
        // Content-based detection
        return detectByContent(content);
    }
    
    /**
     * Detect format by file extension
     */
    private static TextFormat detectByExtension(VaultFile file) {
        if (file == null) {
            return TextFormat.UNKNOWN;
        }
        
        String extension = file.getExtension().toLowerCase();
        switch (extension) {
            case "json":
                return TextFormat.JSON;
            case "xml":
            case "xsd":
            case "xsl":
            case "xslt":
                return TextFormat.XML;
            case "yaml":
            case "yml":
                return TextFormat.YAML;
            case "ini":
            case "cfg":
            case "conf":
                return TextFormat.INI;
            case "toml":
                return TextFormat.TOML;
            case "properties":
                return TextFormat.PROPERTIES;
            case "csv":
                return TextFormat.CSV;
            case "tsv":
                return TextFormat.TSV;
            case "md":
            case "markdown":
                return TextFormat.MARKDOWN;
            case "log":
                return TextFormat.LOG;
            case "sql":
                return TextFormat.SQL;
            case "sh":
            case "bash":
                return TextFormat.SHELL;
            case "bat":
            case "cmd":
                return TextFormat.BATCH;
            case "txt":
            case "text":
                return TextFormat.PLAIN_TEXT;
            default:
                return TextFormat.UNKNOWN;
        }
    }
    
    /**
     * Detect format by analyzing content
     */
    private static TextFormat detectByContent(String content) {
        String trimmed = content.trim();
        
        // JSON detection
        if (isJsonFormat(trimmed)) {
            return TextFormat.JSON;
        }
        
        // XML detection
        if (isXmlFormat(trimmed)) {
            return TextFormat.XML;
        }
        
        // YAML detection
        if (isYamlFormat(content)) {
            return TextFormat.YAML;
        }
        
        // INI detection
        if (isIniFormat(content)) {
            return TextFormat.INI;
        }
        
        // TOML detection
        if (isTomlFormat(content)) {
            return TextFormat.TOML;
        }
        
        // Properties detection
        if (isPropertiesFormat(content)) {
            return TextFormat.PROPERTIES;
        }
        
        // CSV detection
        if (isCsvFormat(content)) {
            return TextFormat.CSV;
        }
        
        // Markdown detection
        if (isMarkdownFormat(content)) {
            return TextFormat.MARKDOWN;
        }
        
        // Log detection
        if (isLogFormat(content)) {
            return TextFormat.LOG;
        }
        
        // SQL detection
        if (isSqlFormat(content)) {
            return TextFormat.SQL;
        }
        
        // Shell script detection
        if (isShellFormat(content)) {
            return TextFormat.SHELL;
        }
        
        // Batch file detection
        if (isBatchFormat(content)) {
            return TextFormat.BATCH;
        }
        
        return TextFormat.PLAIN_TEXT;
    }
    
    /**
     * Check if format is consistent with content
     */
    private static boolean isFormatConsistent(String content, TextFormat format) {
        switch (format) {
            case JSON:
                return isJsonFormat(content.trim());
            case XML:
                return isXmlFormat(content.trim());
            case YAML:
                return isYamlFormat(content);
            case INI:
                return isIniFormat(content);
            case TOML:
                return isTomlFormat(content);
            case PROPERTIES:
                return isPropertiesFormat(content);
            case CSV:
                return isCsvFormat(content);
            case MARKDOWN:
                return isMarkdownFormat(content);
            case LOG:
                return isLogFormat(content);
            case SQL:
                return isSqlFormat(content);
            case SHELL:
                return isShellFormat(content);
            case BATCH:
                return isBatchFormat(content);
            default:
                return true; // Always consistent for plain text and unknown
        }
    }
    
    // Format detection methods
    
    private static boolean isJsonFormat(String content) {
        if (content.isEmpty()) return false;
        
        // Must start with { or [ and end with } or ]
        if (!JSON_PATTERN.matcher(content).matches()) {
            return false;
        }
        
        // Try to validate JSON structure
        try {
            return isValidJsonStructure(content);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isValidJsonStructure(String content) {
        // Simple JSON validation - count brackets and braces
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        boolean escaped = false;
        
        for (char c : content.toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                switch (c) {
                    case '{':
                        braceCount++;
                        break;
                    case '}':
                        braceCount--;
                        break;
                    case '[':
                        bracketCount++;
                        break;
                    case ']':
                        bracketCount--;
                        break;
                }
            }
        }
        
        return braceCount == 0 && bracketCount == 0;
    }
    
    private static boolean isXmlFormat(String content) {
        return XML_PATTERN.matcher(content).find();
    }
    
    private static boolean isYamlFormat(String content) {
        return YAML_PATTERN.matcher(content).find();
    }
    
    private static boolean isIniFormat(String content) {
        return INI_PATTERN.matcher(content).find();
    }
    
    private static boolean isTomlFormat(String content) {
        return TOML_PATTERN.matcher(content).find();
    }
    
    private static boolean isPropertiesFormat(String content) {
        return PROPERTIES_PATTERN.matcher(content).find();
    }
    
    private static boolean isCsvFormat(String content) {
        // Check for comma-separated values
        if (!CSV_PATTERN.matcher(content).find()) {
            return false;
        }
        
        // Additional heuristics
        String[] lines = content.split("\n", 5); // Check first few lines
        if (lines.length < 2) return false;
        
        int firstLineCommas = countCommas(lines[0]);
        if (firstLineCommas == 0) return false;
        
        // Check if other lines have similar comma count
        for (int i = 1; i < Math.min(lines.length, 3); i++) {
            int commas = countCommas(lines[i]);
            if (Math.abs(commas - firstLineCommas) > 1) {
                return false;
            }
        }
        
        return true;
    }
    
    private static int countCommas(String line) {
        int count = 0;
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                count++;
            }
        }
        
        return count;
    }
    
    private static boolean isMarkdownFormat(String content) {
        return MARKDOWN_PATTERN.matcher(content).find();
    }
    
    private static boolean isLogFormat(String content) {
        return LOG_PATTERN.matcher(content).find();
    }
    
    private static boolean isSqlFormat(String content) {
        return SQL_PATTERN.matcher(content).find();
    }
    
    private static boolean isShellFormat(String content) {
        return SHELL_PATTERN.matcher(content).find();
    }
    
    private static boolean isBatchFormat(String content) {
        return BATCH_PATTERN.matcher(content).find();
    }
    
    /**
     * Get format confidence score (0.0 to 1.0)
     */
    public static double getFormatConfidence(String content, TextFormat format, VaultFile file) {
        if (content == null || format == null) {
            return 0.0;
        }
        
        double extensionScore = 0.0;
        double contentScore = 0.0;
        
        // Extension-based confidence
        if (file != null) {
            TextFormat extensionFormat = detectByExtension(file);
            if (extensionFormat == format) {
                extensionScore = 0.8; // High confidence for extension match
            }
        }
        
        // Content-based confidence
        TextFormat contentFormat = detectByContent(content);
        if (contentFormat == format) {
            contentScore = 0.6; // Moderate confidence for content match
        }
        
        // Combine scores
        return Math.max(extensionScore, contentScore);
    }
    
    /**
     * Text format enumeration
     */
    public enum TextFormat {
        UNKNOWN("Unknown", "text/plain"),
        PLAIN_TEXT("Plain Text", "text/plain"),
        JSON("JSON", "application/json"),
        XML("XML", "application/xml"),
        YAML("YAML", "application/x-yaml"),
        INI("INI Configuration", "text/plain"),
        TOML("TOML Configuration", "application/toml"),
        PROPERTIES("Properties", "text/plain"),
        CSV("CSV", "text/csv"),
        TSV("TSV", "text/tab-separated-values"),
        MARKDOWN("Markdown", "text/markdown"),
        LOG("Log File", "text/plain"),
        SQL("SQL", "application/sql"),
        SHELL("Shell Script", "application/x-sh"),
        BATCH("Batch File", "application/x-bat");
        
        private final String displayName;
        private final String mimeType;
        
        TextFormat(String displayName, String mimeType) {
            this.displayName = displayName;
            this.mimeType = mimeType;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public boolean isStructuredFormat() {
            return this == JSON || this == XML || this == YAML || 
                   this == INI || this == TOML || this == PROPERTIES || 
                   this == CSV || this == TSV;
        }
        
        public boolean isCodeFormat() {
            return this == SQL || this == SHELL || this == BATCH;
        }
        
        public boolean isMarkupFormat() {
            return this == MARKDOWN || this == XML;
        }
    }
}