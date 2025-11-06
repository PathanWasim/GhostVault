package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LanguageDetector
 */
class LanguageDetectorTest {
    
    @Test
    @DisplayName("Should detect Java from extension")
    void testJavaExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromExtension("Main.java"));
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromExtension("test.JAVA"));
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromExtension("path/to/File.java"));
    }
    
    @Test
    @DisplayName("Should detect Python from extension")
    void testPythonExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromExtension("script.py"));
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromExtension("module.pyw"));
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromExtension("types.pyi"));
    }
    
    @Test
    @DisplayName("Should detect JavaScript from extension")
    void testJavaScriptExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, LanguageDetector.detectFromExtension("app.js"));
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, LanguageDetector.detectFromExtension("component.jsx"));
        assertEquals(VaultFile.CodeLanguage.TYPESCRIPT, LanguageDetector.detectFromExtension("app.ts"));
        assertEquals(VaultFile.CodeLanguage.TYPESCRIPT, LanguageDetector.detectFromExtension("component.tsx"));
    }
    
    @Test
    @DisplayName("Should detect web languages from extension")
    void testWebLanguageExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.HTML, LanguageDetector.detectFromExtension("index.html"));
        assertEquals(VaultFile.CodeLanguage.HTML, LanguageDetector.detectFromExtension("page.htm"));
        assertEquals(VaultFile.CodeLanguage.CSS, LanguageDetector.detectFromExtension("style.css"));
        assertEquals(VaultFile.CodeLanguage.CSS, LanguageDetector.detectFromExtension("main.scss"));
    }
    
    @Test
    @DisplayName("Should detect data formats from extension")
    void testDataFormatExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.JSON, LanguageDetector.detectFromExtension("config.json"));
        assertEquals(VaultFile.CodeLanguage.XML, LanguageDetector.detectFromExtension("data.xml"));
        assertEquals(VaultFile.CodeLanguage.YAML, LanguageDetector.detectFromExtension("config.yaml"));
        assertEquals(VaultFile.CodeLanguage.YAML, LanguageDetector.detectFromExtension("docker.yml"));
    }
    
    @Test
    @DisplayName("Should detect script languages from extension")
    void testScriptLanguageExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.SQL, LanguageDetector.detectFromExtension("query.sql"));
        assertEquals(VaultFile.CodeLanguage.SHELL, LanguageDetector.detectFromExtension("script.sh"));
        assertEquals(VaultFile.CodeLanguage.SHELL, LanguageDetector.detectFromExtension("install.bash"));
        assertEquals(VaultFile.CodeLanguage.BATCH, LanguageDetector.detectFromExtension("build.bat"));
        assertEquals(VaultFile.CodeLanguage.POWERSHELL, LanguageDetector.detectFromExtension("deploy.ps1"));
    }
    
    @Test
    @DisplayName("Should detect text formats from extension")
    void testTextFormatExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, LanguageDetector.detectFromExtension("README.md"));
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, LanguageDetector.detectFromExtension("doc.markdown"));
        assertEquals(VaultFile.CodeLanguage.PLAIN_TEXT, LanguageDetector.detectFromExtension("notes.txt"));
        assertEquals(VaultFile.CodeLanguage.PLAIN_TEXT, LanguageDetector.detectFromExtension("config.ini"));
    }
    
    @Test
    @DisplayName("Should handle unknown extensions")
    void testUnknownExtensionDetection() {
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromExtension("file.xyz"));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromExtension("README"));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromExtension(""));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromExtension(null));
    }
    
    @Test
    @DisplayName("Should detect Java from content")
    void testJavaContentDetection() {
        String javaContent = """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;
        
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromContent(javaContent));
    }
    
    @Test
    @DisplayName("Should detect Python from content")
    void testPythonContentDetection() {
        String pythonContent = """
            def hello_world():
                print("Hello, World!")
            
            if __name__ == "__main__":
                hello_world()
            """;
        
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromContent(pythonContent));
    }
    
    @Test
    @DisplayName("Should detect JavaScript from content")
    void testJavaScriptContentDetection() {
        String jsContent = """
            function helloWorld() {
                console.log("Hello, World!");
            }
            
            let message = "Hello";
            helloWorld();
            """;
        
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, LanguageDetector.detectFromContent(jsContent));
    }
    
    @Test
    @DisplayName("Should detect HTML from content")
    void testHtmlContentDetection() {
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Page</title>
            </head>
            <body>
                <h1>Hello, World!</h1>
            </body>
            </html>
            """;
        
        assertEquals(VaultFile.CodeLanguage.HTML, LanguageDetector.detectFromContent(htmlContent));
    }
    
    @Test
    @DisplayName("Should detect CSS from content")
    void testCssContentDetection() {
        String cssContent = """
            body {
                font-family: Arial, sans-serif;
                margin: 0;
                padding: 20px;
            }
            
            .header {
                color: blue;
                font-size: 24px;
            }
            """;
        
        assertEquals(VaultFile.CodeLanguage.CSS, LanguageDetector.detectFromContent(cssContent));
    }
    
    @Test
    @DisplayName("Should detect JSON from content")
    void testJsonContentDetection() {
        String jsonContent = """
            {
                "name": "test",
                "version": "1.0.0",
                "dependencies": {
                    "lodash": "^4.17.21"
                }
            }
            """;
        
        assertEquals(VaultFile.CodeLanguage.JSON, LanguageDetector.detectFromContent(jsonContent));
    }
    
    @Test
    @DisplayName("Should detect XML from content")
    void testXmlContentDetection() {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <item id="1">
                    <name>Test Item</name>
                    <value>123</value>
                </item>
            </root>
            """;
        
        assertEquals(VaultFile.CodeLanguage.XML, LanguageDetector.detectFromContent(xmlContent));
    }
    
    @Test
    @DisplayName("Should detect YAML from content")
    void testYamlContentDetection() {
        String yamlContent = """
            ---
            name: test-app
            version: 1.0.0
            dependencies:
              - lodash
              - express
            config:
              port: 3000
              debug: true
            """;
        
        assertEquals(VaultFile.CodeLanguage.YAML, LanguageDetector.detectFromContent(yamlContent));
    }
    
    @Test
    @DisplayName("Should detect SQL from content")
    void testSqlContentDetection() {
        String sqlContent = """
            SELECT u.name, u.email, p.title
            FROM users u
            JOIN posts p ON u.id = p.user_id
            WHERE u.active = 1
            ORDER BY u.name;
            """;
        
        assertEquals(VaultFile.CodeLanguage.SQL, LanguageDetector.detectFromContent(sqlContent));
    }
    
    @Test
    @DisplayName("Should detect Shell script from content")
    void testShellContentDetection() {
        String shellContent = """
            #!/bin/bash
            
            echo "Starting deployment..."
            
            if [ -f "deploy.sh" ]; then
                echo "Deploy script found"
                ./deploy.sh
            fi
            """;
        
        assertEquals(VaultFile.CodeLanguage.SHELL, LanguageDetector.detectFromContent(shellContent));
    }
    
    @Test
    @DisplayName("Should detect Markdown from content")
    void testMarkdownContentDetection() {
        String markdownContent = """
            # Project Title
            
            This is a **bold** statement and this is *italic*.
            
            ## Installation
            
            ```bash
            npm install
            ```
            
            [Link to documentation](https://example.com)
            """;
        
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, LanguageDetector.detectFromContent(markdownContent));
    }
    
    @Test
    @DisplayName("Should detect from shebang lines")
    void testShebangDetection() {
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromContent("#!/usr/bin/python\nprint('hello')"));
        assertEquals(VaultFile.CodeLanguage.SHELL, LanguageDetector.detectFromContent("#!/bin/bash\necho 'hello'"));
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, LanguageDetector.detectFromContent("#!/usr/bin/node\nconsole.log('hello')"));
    }
    
    @Test
    @DisplayName("Should handle empty or null content")
    void testEmptyContentDetection() {
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromContent(""));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromContent("   "));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, LanguageDetector.detectFromContent(null));
    }
    
    @Test
    @DisplayName("Should combine extension and content detection")
    void testCombinedDetection() {
        String javaContent = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
            """;
        
        // Should detect Java from both extension and content
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectLanguage("Test.java", javaContent));
        
        // Should detect from content when extension is missing
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectLanguage("Test", javaContent));
        
        // Should prefer content detection over ambiguous extension
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectLanguage("Test.txt", javaContent));
    }
    
    @Test
    @DisplayName("Should calculate confidence scores")
    void testConfidenceScores() {
        String javaContent = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
            """;
        
        // High confidence when both extension and content match
        double highConfidence = LanguageDetector.getConfidenceScore("Test.java", javaContent, VaultFile.CodeLanguage.JAVA);
        assertTrue(highConfidence > 0.8);
        
        // Medium confidence when only extension matches
        double mediumConfidence = LanguageDetector.getConfidenceScore("Test.java", "", VaultFile.CodeLanguage.JAVA);
        assertTrue(mediumConfidence >= 0.4 && mediumConfidence <= 0.6);
        
        // Zero confidence for unknown language
        double zeroConfidence = LanguageDetector.getConfidenceScore("Test.java", javaContent, VaultFile.CodeLanguage.UNKNOWN);
        assertEquals(0.0, zeroConfidence);
    }
    
    @Test
    @DisplayName("Should handle case insensitive extensions")
    void testCaseInsensitiveExtensions() {
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromExtension("Test.JAVA"));
        assertEquals(VaultFile.CodeLanguage.PYTHON, LanguageDetector.detectFromExtension("script.PY"));
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, LanguageDetector.detectFromExtension("app.JS"));
    }
    
    @Test
    @DisplayName("Should handle files with multiple dots")
    void testMultipleDots() {
        assertEquals(VaultFile.CodeLanguage.JAVA, LanguageDetector.detectFromExtension("com.example.Test.java"));
        assertEquals(VaultFile.CodeLanguage.JSON, LanguageDetector.detectFromExtension("package-lock.json"));
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, LanguageDetector.detectFromExtension("CHANGELOG.md"));
    }
}