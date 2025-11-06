package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.fxmisc.richtext.CodeArea;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SyntaxHighlighter
 */
class SyntaxHighlighterTest {
    
    private SyntaxHighlighter highlighter;
    private CodeArea codeArea;
    
    @BeforeEach
    void setUp() {
        highlighter = new SyntaxHighlighter();
        codeArea = new CodeArea();
    }
    
    @Test
    @DisplayName("Should create syntax highlighter with default theme")
    void testSyntaxHighlighterCreation() {
        assertNotNull(highlighter);
        assertEquals(SyntaxTheme.DARK, highlighter.getTheme());
        assertNotNull(highlighter.getThemeStylesheet());
    }
    
    @Test
    @DisplayName("Should set and get themes correctly")
    void testThemeManagement() {
        // Test setting different themes
        highlighter.setTheme(SyntaxTheme.LIGHT);
        assertEquals(SyntaxTheme.LIGHT, highlighter.getTheme());
        
        highlighter.setTheme(SyntaxTheme.MONOKAI);
        assertEquals(SyntaxTheme.MONOKAI, highlighter.getTheme());
        
        // Test null theme handling
        highlighter.setTheme(null);
        assertEquals(SyntaxTheme.DARK, highlighter.getTheme()); // Should default to DARK
    }
    
    @Test
    @DisplayName("Should generate theme stylesheets")
    void testThemeStylesheets() {
        // Test each theme has a stylesheet
        for (SyntaxTheme theme : SyntaxTheme.values()) {
            highlighter.setTheme(theme);
            String stylesheet = highlighter.getThemeStylesheet();
            assertNotNull(stylesheet);
            assertFalse(stylesheet.isEmpty());
            assertTrue(stylesheet.contains(".code-area"));
        }
    }
    
    @Test
    @DisplayName("Should handle Java syntax highlighting")
    void testJavaSyntaxHighlighting() {
        String javaCode = """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                    int number = 42;
                    boolean flag = true;
                }
            }
            """;
        
        codeArea.replaceText(javaCode);
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JAVA);
        });
        
        // Code area should still contain the original text
        assertEquals(javaCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle Python syntax highlighting")
    void testPythonSyntaxHighlighting() {
        String pythonCode = """
            def hello_world():
                print("Hello, World!")
                number = 42
                flag = True
                return None
            
            if __name__ == "__main__":
                hello_world()
            """;
        
        codeArea.replaceText(pythonCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.PYTHON);
        });
        
        assertEquals(pythonCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle JavaScript syntax highlighting")
    void testJavaScriptSyntaxHighlighting() {
        String jsCode = """
            function helloWorld() {
                console.log("Hello, World!");
                let number = 42;
                const flag = true;
                return null;
            }
            
            helloWorld();
            """;
        
        codeArea.replaceText(jsCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JAVASCRIPT);
        });
        
        assertEquals(jsCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle HTML syntax highlighting")
    void testHtmlSyntaxHighlighting() {
        String htmlCode = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Test Page</title>
            </head>
            <body>
                <h1 class="header">Hello, World!</h1>
                <!-- This is a comment -->
            </body>
            </html>
            """;
        
        codeArea.replaceText(htmlCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.HTML);
        });
        
        assertEquals(htmlCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle CSS syntax highlighting")
    void testCssSyntaxHighlighting() {
        String cssCode = """
            body {
                font-family: Arial, sans-serif;
                margin: 0;
                padding: 20px;
                background-color: #ffffff;
            }
            
            .header {
                color: #333;
                font-size: 24px;
            }
            
            /* This is a comment */
            #main {
                width: 100%;
            }
            """;
        
        codeArea.replaceText(cssCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.CSS);
        });
        
        assertEquals(cssCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle JSON syntax highlighting")
    void testJsonSyntaxHighlighting() {
        String jsonCode = """
            {
                "name": "test-project",
                "version": "1.0.0",
                "dependencies": {
                    "lodash": "^4.17.21"
                },
                "scripts": {
                    "start": "node index.js"
                },
                "active": true,
                "count": 42,
                "data": null
            }
            """;
        
        codeArea.replaceText(jsonCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JSON);
        });
        
        assertEquals(jsonCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle XML syntax highlighting")
    void testXmlSyntaxHighlighting() {
        String xmlCode = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <item id="1" type="test">
                    <name>Test Item</name>
                    <value>123</value>
                    <active>true</active>
                </item>
                <!-- This is a comment -->
                <![CDATA[Some raw data here]]>
            </root>
            """;
        
        codeArea.replaceText(xmlCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.XML);
        });
        
        assertEquals(xmlCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle YAML syntax highlighting")
    void testYamlSyntaxHighlighting() {
        String yamlCode = """
            ---
            name: test-app
            version: 1.0.0
            dependencies:
              - lodash
              - express
            config:
              port: 3000
              debug: true
              database:
                host: localhost
                port: 5432
            # This is a comment
            """;
        
        codeArea.replaceText(yamlCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.YAML);
        });
        
        assertEquals(yamlCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle SQL syntax highlighting")
    void testSqlSyntaxHighlighting() {
        String sqlCode = """
            SELECT u.name, u.email, p.title
            FROM users u
            INNER JOIN posts p ON u.id = p.user_id
            WHERE u.active = TRUE
              AND p.published_date > '2023-01-01'
            ORDER BY u.name ASC, p.created_date DESC
            LIMIT 100;
            
            -- This is a comment
            /* Multi-line comment */
            """;
        
        codeArea.replaceText(sqlCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.SQL);
        });
        
        assertEquals(sqlCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle Shell script syntax highlighting")
    void testShellSyntaxHighlighting() {
        String shellCode = """
            #!/bin/bash
            
            # This is a comment
            function deploy() {
                local env=$1
                echo "Deploying to $env environment"
                
                if [ "$env" = "production" ]; then
                    echo "Production deployment"
                    export NODE_ENV=production
                fi
                
                npm run build
                return 0
            }
            
            deploy "staging"
            """;
        
        codeArea.replaceText(shellCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.SHELL);
        });
        
        assertEquals(shellCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle Markdown syntax highlighting")
    void testMarkdownSyntaxHighlighting() {
        String markdownCode = """
            # Project Title
            
            This is a **bold** statement and this is *italic*.
            
            ## Installation
            
            ```bash
            npm install
            npm start
            ```
            
            ### Features
            
            - Feature 1
            - Feature 2
            - Feature 3
            
            [Link to documentation](https://example.com)
            
            `inline code` example
            """;
        
        codeArea.replaceText(markdownCode);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.MARKDOWN);
        });
        
        assertEquals(markdownCode, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle unknown language gracefully")
    void testUnknownLanguageHandling() {
        String code = "Some random text that doesn't match any language";
        codeArea.replaceText(code);
        
        // Should not throw exception for unknown language
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.UNKNOWN);
        });
        
        // Text should remain unchanged
        assertEquals(code, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        // Should not throw with null code area
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(null, VaultFile.CodeLanguage.JAVA);
        });
        
        // Should not throw with null language
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, null);
        });
    }
    
    @Test
    @DisplayName("Should handle empty code area")
    void testEmptyCodeArea() {
        codeArea.replaceText("");
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JAVA);
        });
        
        assertEquals("", codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle very large code content")
    void testLargeCodeContent() {
        // Create large content
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("public class Test").append(i).append(" {\n");
            largeContent.append("    public void method").append(i).append("() {\n");
            largeContent.append("        System.out.println(\"Test ").append(i).append("\");\n");
            largeContent.append("    }\n");
            largeContent.append("}\n\n");
        }
        
        String code = largeContent.toString();
        codeArea.replaceText(code);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JAVA);
        });
        
        assertEquals(code, codeArea.getText());
    }
    
    @Test
    @DisplayName("Should handle special characters in code")
    void testSpecialCharacters() {
        String codeWithSpecialChars = """
            // Special characters: àáâãäåæçèéêë
            public class Test {
                String unicode = "Hello 世界! 🌍";
                String escaped = "Line 1\\nLine 2\\tTabbed";
                String regex = "\\d+\\.\\d+";
            }
            """;
        
        codeArea.replaceText(codeWithSpecialChars);
        
        assertDoesNotThrow(() -> {
            highlighter.applySyntaxHighlighting(codeArea, VaultFile.CodeLanguage.JAVA);
        });
        
        assertEquals(codeWithSpecialChars, codeArea.getText());
    }
}