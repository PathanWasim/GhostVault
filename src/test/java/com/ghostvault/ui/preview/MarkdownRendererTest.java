package com.ghostvault.ui.preview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MarkdownRenderer
 */
class MarkdownRendererTest {
    
    private MarkdownRenderer darkRenderer;
    private MarkdownRenderer lightRenderer;
    
    @BeforeEach
    void setUp() {
        darkRenderer = new MarkdownRenderer(true);
        lightRenderer = new MarkdownRenderer(false);
    }
    
    @Test
    @DisplayName("Should render basic Markdown to HTML")
    void testBasicMarkdownRendering() {
        String markdown = "# Title\n\nThis is **bold** and *italic* text.";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>italic</em>"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }
    
    @Test
    @DisplayName("Should render HTML fragment without document wrapper")
    void testHtmlFragment() {
        String markdown = "# Title\n\nParagraph text.";
        String fragment = darkRenderer.renderToHtmlFragment(markdown);
        
        assertNotNull(fragment);
        assertTrue(fragment.contains("<h1>Title</h1>"));
        assertTrue(fragment.contains("<p>Paragraph text.</p>"));
        assertFalse(fragment.contains("<!DOCTYPE html>"));
        assertFalse(fragment.contains("<html>"));
    }
    
    @Test
    @DisplayName("Should render tables correctly")
    void testTableRendering() {
        String markdown = "| Name | Age |\n|------|-----|\n| John | 25 |\n| Jane | 30 |";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<th>Name</th>"));
        assertTrue(html.contains("<th>Age</th>"));
        assertTrue(html.contains("<td>John</td>"));
        assertTrue(html.contains("<td>25</td>"));
    }
    
    @Test
    @DisplayName("Should render task lists")
    void testTaskListRendering() {
        String markdown = "- [x] Completed task\n- [ ] Incomplete task";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("task-list"));
    }
    
    @Test
    @DisplayName("Should render code blocks")
    void testCodeBlockRendering() {
        String markdown = "```java\npublic class Test {\n    // comment\n}\n```";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<pre>"));
        assertTrue(html.contains("<code>"));
        assertTrue(html.contains("public class Test"));
    }
    
    @Test
    @DisplayName("Should render inline code")
    void testInlineCodeRendering() {
        String markdown = "Use the `System.out.println()` method.";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<code>System.out.println()</code>"));
    }
    
    @Test
    @DisplayName("Should render links")
    void testLinkRendering() {
        String markdown = "[GitHub](https://github.com)";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<a href=\"https://github.com\">GitHub</a>"));
    }
    
    @Test
    @DisplayName("Should render blockquotes")
    void testBlockquoteRendering() {
        String markdown = "> This is a quote\n> with multiple lines";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<blockquote>"));
        assertTrue(html.contains("This is a quote"));
    }
    
    @Test
    @DisplayName("Should render lists")
    void testListRendering() {
        String markdown = "1. First item\n2. Second item\n\n- Bullet one\n- Bullet two";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<ol>"));
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>First item</li>"));
        assertTrue(html.contains("<li>Bullet one</li>"));
    }
    
    @Test
    @DisplayName("Should handle empty content")
    void testEmptyContent() {
        String html = darkRenderer.renderToHtml("");
        assertNotNull(html);
        assertTrue(html.contains("No content to display"));
        
        html = darkRenderer.renderToHtml(null);
        assertNotNull(html);
        assertTrue(html.contains("No content to display"));
    }
    
    @Test
    @DisplayName("Should handle whitespace-only content")
    void testWhitespaceContent() {
        String html = darkRenderer.renderToHtml("   \n\n   ");
        assertNotNull(html);
        assertTrue(html.contains("No content to display"));
    }
    
    @Test
    @DisplayName("Should include dark theme CSS")
    void testDarkThemeCSS() {
        String html = darkRenderer.renderToHtml("# Test");
        
        assertNotNull(html);
        assertTrue(html.contains("background-color: #1e1e1e"));
        assertTrue(html.contains("color: #d4d4d4"));
    }
    
    @Test
    @DisplayName("Should include light theme CSS")
    void testLightThemeCSS() {
        String html = lightRenderer.renderToHtml("# Test");
        
        assertNotNull(html);
        assertTrue(html.contains("background-color: #ffffff"));
        assertTrue(html.contains("color: #24292e"));
    }
    
    @Test
    @DisplayName("Should report correct theme")
    void testThemeReporting() {
        assertTrue(darkRenderer.isDarkTheme());
        assertFalse(lightRenderer.isDarkTheme());
    }
    
    @Test
    @DisplayName("Should create renderer with different theme")
    void testWithTheme() {
        MarkdownRenderer newLight = darkRenderer.withTheme(false);
        MarkdownRenderer newDark = lightRenderer.withTheme(true);
        
        assertFalse(newLight.isDarkTheme());
        assertTrue(newDark.isDarkTheme());
        
        // Original renderers should be unchanged
        assertTrue(darkRenderer.isDarkTheme());
        assertFalse(lightRenderer.isDarkTheme());
    }
    
    @Test
    @DisplayName("Should handle malformed Markdown gracefully")
    void testMalformedMarkdown() {
        String malformed = "# Unclosed [link\n\n**Unclosed bold\n\n```\nUnclosed code block";
        String html = darkRenderer.renderToHtml(malformed);
        
        assertNotNull(html);
        // Should not crash and should produce some HTML
        assertTrue(html.contains("<h1>"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }
    
    @Test
    @DisplayName("Should render strikethrough text")
    void testStrikethroughRendering() {
        String markdown = "~~strikethrough text~~";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<del>strikethrough text</del>"));
    }
    
    @Test
    @DisplayName("Should auto-link URLs")
    void testAutoLinking() {
        String markdown = "Visit https://example.com for more info.";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<a href=\"https://example.com\">https://example.com</a>"));
    }
    
    @Test
    @DisplayName("Should render horizontal rules")
    void testHorizontalRules() {
        String markdown = "Before\n\n---\n\nAfter";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        assertTrue(html.contains("<hr"));
    }
    
    @Test
    @DisplayName("Should handle special characters in HTML")
    void testSpecialCharacters() {
        String markdown = "Text with <script> and & characters";
        String html = darkRenderer.renderToHtml(markdown);
        
        assertNotNull(html);
        // Should escape HTML characters
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&amp;"));
    }
}