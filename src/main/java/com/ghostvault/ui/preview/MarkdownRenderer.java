package com.ghostvault.ui.preview;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;

import java.util.Arrays;

/**
 * Markdown renderer using FlexMark library
 * Converts Markdown text to HTML with GitHub Flavored Markdown support
 */
public class MarkdownRenderer {
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    private final boolean darkTheme;
    
    public MarkdownRenderer() {
        this(true); // Default to dark theme
    }
    
    public MarkdownRenderer(boolean darkTheme) {
        this.darkTheme = darkTheme;
        
        // Configure FlexMark options
        MutableDataSet options = new MutableDataSet();
        
        // Enable GitHub Flavored Markdown extensions
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create(),
            AutolinkExtension.create(),
            AnchorLinkExtension.create(),
            TocExtension.create(),
            FootnoteExtension.create(),
            DefinitionExtension.create(),
            AbbreviationExtension.create()
        ));
        
        // Configure table options
        options.set(TablesExtension.COLUMN_SPANS, false)
               .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
               .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
               .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);
        
        // Configure task list options
        options.set(TaskListExtension.ITEM_DONE_MARKER, "✓")
               .set(TaskListExtension.ITEM_NOT_DONE_MARKER, "○");
        
        // Configure anchor link options
        options.set(AnchorLinkExtension.ANCHORLINKS_SET_ID, true)
               .set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor")
               .set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true);
        
        // Configure TOC options
        options.set(TocExtension.LEVELS, 255)
               .set(TocExtension.IS_TEXT_ONLY, false)
               .set(TocExtension.IS_NUMBERED, false);
        
        // Create parser and renderer
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }
    
    /**
     * Render Markdown text to HTML
     * @param markdownText The Markdown source text
     * @return HTML string
     */
    public String renderToHtml(String markdownText) {
        if (markdownText == null || markdownText.trim().isEmpty()) {
            return "<html><body><p>No content to display</p></body></html>";
        }
        
        try {
            // Parse Markdown
            Node document = parser.parse(markdownText);
            
            // Render to HTML
            String htmlContent = renderer.render(document);
            
            // Wrap in complete HTML document with styling
            return wrapInHtmlDocument(htmlContent);
            
        } catch (Exception e) {
            // Return error message if parsing fails
            return "<html><body><div class='error'><h3>Markdown Parsing Error</h3><p>" + 
                   escapeHtml(e.getMessage()) + "</p><pre>" + 
                   escapeHtml(markdownText) + "</pre></div></body></html>";
        }
    }
    
    /**
     * Render Markdown text to plain HTML (without document wrapper)
     * @param markdownText The Markdown source text
     * @return HTML fragment
     */
    public String renderToHtmlFragment(String markdownText) {
        if (markdownText == null || markdownText.trim().isEmpty()) {
            return "<p>No content to display</p>";
        }
        
        try {
            Node document = parser.parse(markdownText);
            return renderer.render(document);
        } catch (Exception e) {
            return "<div class='error'><h3>Markdown Parsing Error</h3><p>" + 
                   escapeHtml(e.getMessage()) + "</p></div>";
        }
    }
    
    /**
     * Wrap HTML content in a complete document with styling
     */
    private String wrapInHtmlDocument(String htmlContent) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Markdown Preview</title>\n");
        html.append("    <style>\n");
        html.append(getMarkdownCss());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"markdown-body\">\n");
        html.append(htmlContent);
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Get CSS styles for Markdown rendering
     */
    private String getMarkdownCss() {
        if (darkTheme) {
            return getDarkThemeCss();
        } else {
            return getLightThemeCss();
        }
    }
    
    /**
     * Dark theme CSS for Markdown
     */
    private String getDarkThemeCss() {
        return """
            body {
                background-color: #1e1e1e;
                color: #d4d4d4;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                line-height: 1.6;
                margin: 0;
                padding: 20px;
            }
            
            .markdown-body {
                max-width: 900px;
                margin: 0 auto;
            }
            
            h1, h2, h3, h4, h5, h6 {
                color: #ffffff;
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
                line-height: 1.25;
            }
            
            h1 { font-size: 2em; border-bottom: 1px solid #404040; padding-bottom: 0.3em; }
            h2 { font-size: 1.5em; border-bottom: 1px solid #404040; padding-bottom: 0.3em; }
            h3 { font-size: 1.25em; }
            h4 { font-size: 1em; }
            h5 { font-size: 0.875em; }
            h6 { font-size: 0.85em; color: #8c8c8c; }
            
            p {
                margin-bottom: 16px;
            }
            
            a {
                color: #4fc3f7;
                text-decoration: none;
            }
            
            a:hover {
                text-decoration: underline;
            }
            
            code {
                background-color: #2d2d2d;
                color: #f8f8f2;
                padding: 2px 4px;
                border-radius: 3px;
                font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                font-size: 0.9em;
            }
            
            pre {
                background-color: #2d2d2d;
                color: #f8f8f2;
                padding: 16px;
                border-radius: 6px;
                overflow-x: auto;
                font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                font-size: 0.9em;
                line-height: 1.45;
            }
            
            pre code {
                background-color: transparent;
                padding: 0;
            }
            
            blockquote {
                border-left: 4px solid #404040;
                padding-left: 16px;
                margin-left: 0;
                color: #8c8c8c;
                font-style: italic;
            }
            
            table {
                border-collapse: collapse;
                width: 100%;
                margin-bottom: 16px;
            }
            
            th, td {
                border: 1px solid #404040;
                padding: 8px 12px;
                text-align: left;
            }
            
            th {
                background-color: #2d2d2d;
                font-weight: 600;
            }
            
            tr:nth-child(even) {
                background-color: #252525;
            }
            
            ul, ol {
                margin-bottom: 16px;
                padding-left: 30px;
            }
            
            li {
                margin-bottom: 4px;
            }
            
            .task-list-item {
                list-style-type: none;
                margin-left: -20px;
            }
            
            .task-list-item input[type="checkbox"] {
                margin-right: 8px;
            }
            
            hr {
                border: none;
                border-top: 1px solid #404040;
                margin: 24px 0;
            }
            
            img {
                max-width: 100%;
                height: auto;
            }
            
            .anchor {
                color: #4fc3f7;
                text-decoration: none;
                margin-left: 4px;
            }
            
            .error {
                background-color: #3d1a1a;
                border: 1px solid #d32f2f;
                border-radius: 4px;
                padding: 16px;
                margin: 16px 0;
            }
            
            .error h3 {
                color: #f44336;
                margin-top: 0;
            }
            
            .error pre {
                background-color: #2d2d2d;
                border: 1px solid #404040;
                max-height: 200px;
                overflow-y: auto;
            }
            """;
    }
    
    /**
     * Light theme CSS for Markdown
     */
    private String getLightThemeCss() {
        return """
            body {
                background-color: #ffffff;
                color: #24292e;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                line-height: 1.6;
                margin: 0;
                padding: 20px;
            }
            
            .markdown-body {
                max-width: 900px;
                margin: 0 auto;
            }
            
            h1, h2, h3, h4, h5, h6 {
                color: #24292e;
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
                line-height: 1.25;
            }
            
            h1 { font-size: 2em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
            h2 { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
            h3 { font-size: 1.25em; }
            h4 { font-size: 1em; }
            h5 { font-size: 0.875em; }
            h6 { font-size: 0.85em; color: #6a737d; }
            
            p {
                margin-bottom: 16px;
            }
            
            a {
                color: #0366d6;
                text-decoration: none;
            }
            
            a:hover {
                text-decoration: underline;
            }
            
            code {
                background-color: #f6f8fa;
                color: #24292e;
                padding: 2px 4px;
                border-radius: 3px;
                font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                font-size: 0.9em;
            }
            
            pre {
                background-color: #f6f8fa;
                color: #24292e;
                padding: 16px;
                border-radius: 6px;
                overflow-x: auto;
                font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                font-size: 0.9em;
                line-height: 1.45;
            }
            
            pre code {
                background-color: transparent;
                padding: 0;
            }
            
            blockquote {
                border-left: 4px solid #dfe2e5;
                padding-left: 16px;
                margin-left: 0;
                color: #6a737d;
            }
            
            table {
                border-collapse: collapse;
                width: 100%;
                margin-bottom: 16px;
            }
            
            th, td {
                border: 1px solid #dfe2e5;
                padding: 8px 12px;
                text-align: left;
            }
            
            th {
                background-color: #f6f8fa;
                font-weight: 600;
            }
            
            tr:nth-child(even) {
                background-color: #f6f8fa;
            }
            
            ul, ol {
                margin-bottom: 16px;
                padding-left: 30px;
            }
            
            li {
                margin-bottom: 4px;
            }
            
            .task-list-item {
                list-style-type: none;
                margin-left: -20px;
            }
            
            .task-list-item input[type="checkbox"] {
                margin-right: 8px;
            }
            
            hr {
                border: none;
                border-top: 1px solid #eaecef;
                margin: 24px 0;
            }
            
            img {
                max-width: 100%;
                height: auto;
            }
            
            .anchor {
                color: #0366d6;
                text-decoration: none;
                margin-left: 4px;
            }
            
            .error {
                background-color: #ffeaea;
                border: 1px solid #d32f2f;
                border-radius: 4px;
                padding: 16px;
                margin: 16px 0;
            }
            
            .error h3 {
                color: #d32f2f;
                margin-top: 0;
            }
            
            .error pre {
                background-color: #f6f8fa;
                border: 1px solid #dfe2e5;
                max-height: 200px;
                overflow-y: auto;
            }
            """;
    }
    
    /**
     * Escape HTML characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * Check if the renderer is using dark theme
     */
    public boolean isDarkTheme() {
        return darkTheme;
    }
    
    /**
     * Create a new renderer with different theme
     */
    public MarkdownRenderer withTheme(boolean darkTheme) {
        return new MarkdownRenderer(darkTheme);
    }
}