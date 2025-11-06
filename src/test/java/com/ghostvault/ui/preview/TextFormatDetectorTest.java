package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TextFormatDetector
 */
class TextFormatDetectorTest {
    
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        mockVaultFile = mock(VaultFile.class);
    }
    
    @Test
    @DisplayName("Should detect JSON format by content")
    void testDetectJSONByContent() {
        String jsonContent = "{\n  \"name\": \"test\",\n  \"value\": 123\n}";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(jsonContent, null);
        assertEquals(TextFormatDetector.TextFormat.JSON, format);
    }
    
    @Test
    @DisplayName("Should detect JSON array format")
    void testDetectJSONArray() {
        String jsonContent = "[\n  {\"id\": 1},\n  {\"id\": 2}\n]";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(jsonContent, null);
        assertEquals(TextFormatDetector.TextFormat.JSON, format);
    }
    
    @Test
    @DisplayName("Should detect XML format by content")
    void testDetectXMLByContent() {
        String xmlContent = "<?xml version=\"1.0\"?>\n<root>\n  <item>test</item>\n</root>";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(xmlContent, null);
        assertEquals(TextFormatDetector.TextFormat.XML, format);
    }
    
    @Test
    @DisplayName("Should detect XML without declaration")
    void testDetectXMLWithoutDeclaration() {
        String xmlContent = "<root>\n  <item>test</item>\n</root>";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(xmlContent, null);
        assertEquals(TextFormatDetector.TextFormat.XML, format);
    }
    
    @Test
    @DisplayName("Should detect YAML format by content")
    void testDetectYAMLByContent() {
        String yamlContent = "---\nname: test\nvalue: 123\nitems:\n  - item1\n  - item2";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(yamlContent, null);
        assertEquals(TextFormatDetector.TextFormat.YAML, format);
    }
    
    @Test
    @DisplayName("Should detect YAML without document separator")
    void testDetectYAMLWithoutSeparator() {
        String yamlContent = "name: test\nvalue: 123";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(yamlContent, null);
        assertEquals(TextFormatDetector.TextFormat.YAML, format);
    }
    
    @Test
    @DisplayName("Should detect Markdown format")
    void testDetectMarkdown() {
        String markdownContent = "# Title\n\nThis is a **bold** text with [link](http://example.com).\n\n- Item 1\n- Item 2";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(markdownContent, null);
        assertEquals(TextFormatDetector.TextFormat.MARKDOWN, format);
    }
    
    @Test
    @DisplayName("Should detect CSV format")
    void testDetectCSV() {
        String csvContent = "Name,Age,City\nJohn,25,New York\nJane,30,London";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(csvContent, null);
        assertEquals(TextFormatDetector.TextFormat.CSV, format);
    }
    
    @Test
    @DisplayName("Should detect INI format")
    void testDetectINI() {
        String iniContent = "[section1]\nkey1=value1\nkey2=value2\n\n[section2]\nkey3=value3";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(iniContent, null);
        assertEquals(TextFormatDetector.TextFormat.INI, format);
    }
    
    @Test
    @DisplayName("Should detect log format")
    void testDetectLog() {
        String logContent = "2023-12-01 10:30:45 INFO Starting application\n2023-12-01 10:30:46 ERROR Connection failed";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(logContent, null);
        assertEquals(TextFormatDetector.TextFormat.LOG, format);
    }
    
    @Test
    @DisplayName("Should detect SQL format")
    void testDetectSQL() {
        String sqlContent = "SELECT * FROM users WHERE age > 18 ORDER BY name;";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(sqlContent, null);
        assertEquals(TextFormatDetector.TextFormat.SQL, format);
    }
    
    @Test
    @DisplayName("Should detect shell script format")
    void testDetectShellScript() {
        String shellContent = "#!/bin/bash\necho \"Hello World\"\nif [ $? -eq 0 ]; then\n  echo \"Success\"\nfi";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(shellContent, null);
        assertEquals(TextFormatDetector.TextFormat.SHELL, format);
    }
    
    @Test
    @DisplayName("Should detect batch file format")
    void testDetectBatchFile() {
        String batchContent = "@echo off\nrem This is a comment\nset VAR=value\nif %VAR%==value goto end\n:end";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(batchContent, null);
        assertEquals(TextFormatDetector.TextFormat.BATCH, format);
    }
    
    @Test
    @DisplayName("Should detect format by file extension")
    void testDetectByExtension() {
        when(mockVaultFile.getFileExtension()).thenReturn("json");
        String content = "not really json content";
        
        // Should still try content detection if extension doesn't match
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(content, mockVaultFile);
        assertEquals(TextFormatDetector.TextFormat.PLAIN_TEXT, format);
    }
    
    @Test
    @DisplayName("Should detect consistent format")
    void testDetectConsistentFormat() {
        when(mockVaultFile.getFileExtension()).thenReturn("json");
        String jsonContent = "{\"test\": true}";
        
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(jsonContent, mockVaultFile);
        assertEquals(TextFormatDetector.TextFormat.JSON, format);
    }
    
    @Test
    @DisplayName("Should handle empty content")
    void testEmptyContent() {
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat("", null);
        assertEquals(TextFormatDetector.TextFormat.PLAIN_TEXT, format);
        
        format = TextFormatDetector.detectFormat("   ", null);
        assertEquals(TextFormatDetector.TextFormat.PLAIN_TEXT, format);
    }
    
    @Test
    @DisplayName("Should handle null content")
    void testNullContent() {
        TextFormatDetector.TextFormat format = TextFormatDetector.detectFormat(null, null);
        assertEquals(TextFormatDetector.TextFormat.PLAIN_TEXT, format);
    }
    
    @Test
    @DisplayName("Should calculate confidence scores")
    void testGetFormatConfidence() {
        when(mockVaultFile.getFileExtension()).thenReturn("json");
        String jsonContent = "{\"test\": true}";
        
        double confidence = TextFormatDetector.getFormatConfidence(jsonContent, TextFormatDetector.TextFormat.JSON, mockVaultFile);
        assertTrue(confidence > 0.0);
        assertTrue(confidence <= 1.0);
        
        // Wrong format should have lower confidence
        double wrongConfidence = TextFormatDetector.getFormatConfidence(jsonContent, TextFormatDetector.TextFormat.XML, mockVaultFile);
        assertTrue(wrongConfidence < confidence);
    }
    
    @Test
    @DisplayName("Should identify structured formats")
    void testStructuredFormats() {
        assertTrue(TextFormatDetector.TextFormat.JSON.isStructuredFormat());
        assertTrue(TextFormatDetector.TextFormat.XML.isStructuredFormat());
        assertTrue(TextFormatDetector.TextFormat.YAML.isStructuredFormat());
        assertTrue(TextFormatDetector.TextFormat.CSV.isStructuredFormat());
        
        assertFalse(TextFormatDetector.TextFormat.PLAIN_TEXT.isStructuredFormat());
        assertFalse(TextFormatDetector.TextFormat.MARKDOWN.isStructuredFormat());
    }
    
    @Test
    @DisplayName("Should identify code formats")
    void testCodeFormats() {
        assertTrue(TextFormatDetector.TextFormat.SQL.isCodeFormat());
        assertTrue(TextFormatDetector.TextFormat.SHELL.isCodeFormat());
        assertTrue(TextFormatDetector.TextFormat.BATCH.isCodeFormat());
        
        assertFalse(TextFormatDetector.TextFormat.PLAIN_TEXT.isCodeFormat());
        assertFalse(TextFormatDetector.TextFormat.JSON.isCodeFormat());
    }
    
    @Test
    @DisplayName("Should identify markup formats")
    void testMarkupFormats() {
        assertTrue(TextFormatDetector.TextFormat.MARKDOWN.isMarkupFormat());
        assertTrue(TextFormatDetector.TextFormat.XML.isMarkupFormat());
        
        assertFalse(TextFormatDetector.TextFormat.PLAIN_TEXT.isMarkupFormat());
        assertFalse(TextFormatDetector.TextFormat.JSON.isMarkupFormat());
    }
    
    @Test
    @DisplayName("Should have correct display names and MIME types")
    void testDisplayNamesAndMimeTypes() {
        assertEquals("JSON", TextFormatDetector.TextFormat.JSON.getDisplayName());
        assertEquals("application/json", TextFormatDetector.TextFormat.JSON.getMimeType());
        
        assertEquals("Plain Text", TextFormatDetector.TextFormat.PLAIN_TEXT.getDisplayName());
        assertEquals("text/plain", TextFormatDetector.TextFormat.PLAIN_TEXT.getMimeType());
        
        assertEquals("Markdown", TextFormatDetector.TextFormat.MARKDOWN.getDisplayName());
        assertEquals("text/markdown", TextFormatDetector.TextFormat.MARKDOWN.getMimeType());
    }
}