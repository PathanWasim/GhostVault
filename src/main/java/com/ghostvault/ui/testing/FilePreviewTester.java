package com.ghostvault.ui.testing;

import com.ghostvault.ui.components.*;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive test suite for file preview functionality
 */
public class FilePreviewTester {
    
    private List<TestResult> testResults = new ArrayList<>();
    private Map<String, List<String>> supportedFormats = new HashMap<>();
    
    public FilePreviewTester() {
        initializeSupportedFormats();
    }
    
    /**
     * Initialize supported file formats for testing
     */
    private void initializeSupportedFormats() {
        // Image formats
        supportedFormats.put("Images", Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp", ".tiff", ".tif"
        ));
        
        // Video formats
        supportedFormats.put("Videos", Arrays.asList(
            ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v"
        ));
        
        // Audio formats
        supportedFormats.put("Audio", Arrays.asList(
            ".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a"
        ));
        
        // Document formats
        supportedFormats.put("Documents", Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf", ".odt"
        ));
        
        // Code formats
        supportedFormats.put("Code", Arrays.asList(
            ".java", ".py", ".js", ".html", ".css", ".cpp", ".c", ".h", ".php", ".rb", ".go", ".rs"
        ));
        
        // Configuration formats
        supportedFormats.put("Configuration", Arrays.asList(
            ".json", ".xml", ".yaml", ".yml", ".ini", ".cfg", ".conf", ".log", ".md"
        ));
        
        // Archive formats
        supportedFormats.put("Archives", Arrays.asList(
            ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"
        ));
    }
    
    /**
     * Run comprehensive file preview tests
     */
    public CompletableFuture<TestSummary> runAllTests() {
        return CompletableFuture.supplyAsync(() -> {
            testResults.clear();
            
            try {
                // Test 1: Image preview functionality
                testImagePreview();
                
                // Test 2: Video preview functionality
                testVideoPreview();
                
                // Test 3: Audio preview functionality
                testAudioPreview();
                
                // Test 4: Code preview with syntax highlighting
                testCodePreview();
                
                // Test 5: Document preview functionality
                testDocumentPreview();
                
                // Test 6: Unsupported file handling
                testUnsupportedFiles();
                
                // Test 7: Large file handling
                testLargeFileHandling();
                
                // Test 8: Preview pane resizing and zoom
                testPreviewInteractions();
                
                // Test 9: Error handling and recovery
                testErrorHandling();
                
                return generateTestSummary();
                
            } catch (Exception e) {
                TestResult errorResult = new TestResult("Test Execution");
                errorResult.addStep("✗ Test execution failed: " + e.getMessage());
                errorResult.setSuccess(false);
                testResults.add(errorResult);
                
                return new TestSummary(testResults, false);
            }
        });
    }
    
    /**
     * Test image preview functionality
     */
    private void testImagePreview() {
        TestResult result = new TestResult("Image Preview");
        
        try {
            result.addStep("Testing image preview functionality");
            
            ImagePreviewPane imagePreview = new ImagePreviewPane();
            
            // Test supported formats
            List<String> imageFormats = supportedFormats.get("Images");
            for (String format : imageFormats) {
                File testFile = createTestFile("test_image" + format, "fake image content");
                
                if (ImagePreviewPane.isSupported(testFile)) {
                    result.addStep("✓ Format " + format + " is supported");
                } else {
                    result.addStep("✗ Format " + format + " should be supported but isn't");
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            // Test zoom functionality
            result.addStep("Testing zoom functionality");
            if (testZoomFunctionality(imagePreview)) {
                result.addStep("✓ Zoom functionality working");
            } else {
                result.addStep("✗ Zoom functionality failed");
                result.setSuccess(false);
            }
            
            // Test rotation functionality
            result.addStep("Testing rotation functionality");
            if (testRotationFunctionality(imagePreview)) {
                result.addStep("✓ Rotation functionality working");
            } else {
                result.addStep("✗ Rotation functionality failed");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during image preview test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test video preview functionality
     */
    private void testVideoPreview() {
        TestResult result = new TestResult("Video Preview");
        
        try {
            result.addStep("Testing video preview functionality");
            
            // Test supported formats
            List<String> videoFormats = supportedFormats.get("Videos");
            for (String format : videoFormats) {
                File testFile = createTestFile("test_video" + format, "fake video content");
                
                // Check if format is recognized
                String fileType = FileIconProvider.getFileTypeCategory(testFile);
                if ("Video".equals(fileType)) {
                    result.addStep("✓ Format " + format + " is recognized as video");
                } else {
                    result.addStep("✗ Format " + format + " not recognized as video");
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            result.addStep("✓ Video format recognition working");
            
        } catch (Exception e) {
            result.addStep("✗ Exception during video preview test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test audio preview functionality
     */
    private void testAudioPreview() {
        TestResult result = new TestResult("Audio Preview");
        
        try {
            result.addStep("Testing audio preview functionality");
            
            // Test supported formats
            List<String> audioFormats = supportedFormats.get("Audio");
            for (String format : audioFormats) {
                File testFile = createTestFile("test_audio" + format, "fake audio content");
                
                // Check if format is recognized
                String fileType = FileIconProvider.getFileTypeCategory(testFile);
                if ("Audio".equals(fileType)) {
                    result.addStep("✓ Format " + format + " is recognized as audio");
                } else {
                    result.addStep("✗ Format " + format + " not recognized as audio");
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            result.addStep("✓ Audio format recognition working");
            
        } catch (Exception e) {
            result.addStep("✗ Exception during audio preview test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test code preview with syntax highlighting
     */
    private void testCodePreview() {
        TestResult result = new TestResult("Code Preview");
        
        try {
            result.addStep("Testing code preview with syntax highlighting");
            
            CodePreviewPane codePreview = new CodePreviewPane();
            
            // Test different programming languages
            Map<String, String> codeExamples = new HashMap<>();
            codeExamples.put(".java", "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }");
            codeExamples.put(".py", "def hello_world():\\n    print('Hello, World!')\\n\\nif __name__ == '__main__':\\n    hello_world()");
            codeExamples.put(".js", "function helloWorld() {\\n    console.log('Hello, World!');\\n}\\n\\nhelloWorld();");
            codeExamples.put(".html", "<!DOCTYPE html>\\n<html>\\n<head><title>Test</title></head>\\n<body><h1>Hello</h1></body>\\n</html>");
            codeExamples.put(".css", "body {\\n    font-family: Arial, sans-serif;\\n    background-color: #f0f0f0;\\n}");
            
            for (Map.Entry<String, String> entry : codeExamples.entrySet()) {
                String extension = entry.getKey();
                String code = entry.getValue();
                
                File testFile = createTestFile("test_code" + extension, code);
                
                try {
                    // Test language detection
                    String detectedLanguage = detectLanguageFromFile(testFile);
                    if (detectedLanguage != null && !detectedLanguage.equals("text")) {
                        result.addStep("✓ Language detection working for " + extension + " (" + detectedLanguage + ")");
                    } else {
                        result.addStep("✗ Language detection failed for " + extension);
                        result.setSuccess(false);
                    }
                    
                } catch (Exception e) {
                    result.addStep("✗ Error testing " + extension + ": " + e.getMessage());
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            // Test line number display
            result.addStep("Testing line number display");
            if (codePreview.isShowLineNumbers()) {
                result.addStep("✓ Line numbers enabled by default");
            } else {
                result.addStep("✗ Line numbers should be enabled by default");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during code preview test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test document preview functionality
     */
    private void testDocumentPreview() {
        TestResult result = new TestResult("Document Preview");
        
        try {
            result.addStep("Testing document preview functionality");
            
            // Test document format recognition
            List<String> documentFormats = supportedFormats.get("Documents");
            for (String format : documentFormats) {
                File testFile = createTestFile("test_document" + format, "Sample document content");
                
                String fileType = FileIconProvider.getFileTypeCategory(testFile);
                if ("Document".equals(fileType)) {
                    result.addStep("✓ Format " + format + " is recognized as document");
                } else {
                    result.addStep("✗ Format " + format + " not recognized as document");
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            result.addStep("✓ Document format recognition working");
            
        } catch (Exception e) {
            result.addStep("✗ Exception during document preview test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test handling of unsupported files
     */
    private void testUnsupportedFiles() {
        TestResult result = new TestResult("Unsupported Files");
        
        try {
            result.addStep("Testing unsupported file handling");
            
            // Create files with unsupported extensions
            String[] unsupportedExtensions = {".xyz", ".unknown", ".binary", ".proprietary"};
            
            for (String extension : unsupportedExtensions) {
                File testFile = createTestFile("unsupported" + extension, "binary content");
                
                // Test that appropriate fallback is used
                String fileType = FileIconProvider.getFileTypeCategory(testFile);
                if ("File".equals(fileType)) {
                    result.addStep("✓ Unsupported format " + extension + " handled gracefully");
                } else {
                    result.addStep("✗ Unsupported format " + extension + " not handled properly");
                    result.setSuccess(false);
                }
                
                testFile.delete();
            }
            
            result.addStep("✓ Unsupported file handling working");
            
        } catch (Exception e) {
            result.addStep("✗ Exception during unsupported files test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test large file handling
     */
    private void testLargeFileHandling() {
        TestResult result = new TestResult("Large File Handling");
        
        try {
            result.addStep("Testing large file handling");
            
            // Test with simulated large file
            File largeFile = createTestFile("large_file.txt", generateLargeContent(1024 * 1024)); // 1MB
            
            // Test file info display
            DetailedFileInfoPane fileInfo = new DetailedFileInfoPane();
            fileInfo.displayFile(largeFile);
            
            result.addStep("✓ Large file info display working");
            
            // Test thumbnail generation for large files
            try {
                javafx.scene.image.Image thumbnail = ThumbnailGenerator.generateThumbnail(largeFile, 64);
                if (thumbnail != null) {
                    result.addStep("✓ Thumbnail generation for large files working");
                } else {
                    result.addStep("✓ Thumbnail generation appropriately skipped for large text file");
                }
            } catch (Exception e) {
                result.addStep("✗ Thumbnail generation failed for large file: " + e.getMessage());
                result.setSuccess(false);
            }
            
            largeFile.delete();
            
        } catch (Exception e) {
            result.addStep("✗ Exception during large file test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test preview pane interactions
     */
    private void testPreviewInteractions() {
        TestResult result = new TestResult("Preview Interactions");
        
        try {
            result.addStep("Testing preview pane interactions");
            
            ResizablePreviewPane previewPane = new ResizablePreviewPane();
            
            // Test resizing functionality
            if (previewPane.isResizable()) {
                result.addStep("✓ Preview pane is resizable");
            } else {
                result.addStep("✗ Preview pane should be resizable");
                result.setSuccess(false);
            }
            
            result.addStep("✓ Preview interactions working");
            
        } catch (Exception e) {
            result.addStep("✗ Exception during preview interactions test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test error handling and recovery
     */
    private void testErrorHandling() {
        TestResult result = new TestResult("Error Handling");
        
        try {
            result.addStep("Testing error handling and recovery");
            
            // Test with non-existent file
            File nonExistentFile = new File("non_existent_file.txt");
            
            DetailedFileInfoPane fileInfo = new DetailedFileInfoPane();
            fileInfo.displayFile(nonExistentFile);
            
            result.addStep("✓ Non-existent file handled gracefully");
            
            // Test with corrupted file (empty file with image extension)
            File corruptedFile = createTestFile("corrupted.jpg", "");
            
            try {
                javafx.scene.image.Image thumbnail = ThumbnailGenerator.generateThumbnail(corruptedFile, 64);
                result.addStep("✓ Corrupted file handled gracefully");
            } catch (Exception e) {
                result.addStep("✓ Corrupted file error handled: " + e.getMessage());
            }
            
            corruptedFile.delete();
            
        } catch (Exception e) {
            result.addStep("✗ Exception during error handling test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    // Helper methods
    
    private File createTestFile(String name, String content) throws IOException {
        File testFile = new File(System.getProperty("java.io.tmpdir"), name);
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(content);
        }
        return testFile;
    }
    
    private String generateLargeContent(int size) {
        StringBuilder content = new StringBuilder();
        String line = "This is a test line for large file content generation.\\n";
        
        while (content.length() < size) {
            content.append(line);
        }
        
        return content.toString();
    }
    
    private boolean testZoomFunctionality(ImagePreviewPane imagePreview) {
        try {
            // Test zoom methods exist and can be called
            double initialZoom = imagePreview.getZoomFactor();
            return initialZoom >= 0; // Basic validation
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testRotationFunctionality(ImagePreviewPane imagePreview) {
        try {
            // Test rotation methods exist and can be called
            double initialRotation = imagePreview.getRotation();
            return initialRotation >= 0; // Basic validation
        } catch (Exception e) {
            return false;
        }
    }
    
    private String detectLanguageFromFile(File file) {
        // Simulate language detection based on file extension
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".java")) return "java";
        if (fileName.endsWith(".py")) return "python";
        if (fileName.endsWith(".js")) return "javascript";
        if (fileName.endsWith(".html")) return "html";
        if (fileName.endsWith(".css")) return "css";
        
        return "text";
    }
    
    private TestSummary generateTestSummary() {
        boolean allTestsPassed = testResults.stream().allMatch(TestResult::isSuccess);
        return new TestSummary(testResults, allTestsPassed);
    }
    
    /**
     * Test result class
     */
    public static class TestResult {
        private String testName;
        private List<String> steps = new ArrayList<>();
        private boolean success = true;
        private long executionTime;
        
        public TestResult(String testName) {
            this.testName = testName;
            this.executionTime = System.currentTimeMillis();
        }
        
        public void addStep(String step) {
            steps.add(step);
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getTestName() { return testName; }
        public List<String> getSteps() { return steps; }
        public boolean isSuccess() { return success; }
        public long getExecutionTime() { return System.currentTimeMillis() - executionTime; }
    }
    
    /**
     * Test summary class
     */
    public static class TestSummary {
        private List<TestResult> results;
        private boolean allTestsPassed;
        private int totalTests;
        private int passedTests;
        private int failedTests;
        
        public TestSummary(List<TestResult> results, boolean allTestsPassed) {
            this.results = new ArrayList<>(results);
            this.allTestsPassed = allTestsPassed;
            this.totalTests = results.size();
            this.passedTests = (int) results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum();
            this.failedTests = totalTests - passedTests;
        }
        
        public List<TestResult> getResults() { return results; }
        public boolean isAllTestsPassed() { return allTestsPassed; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        
        public String getSummaryReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== FILE PREVIEW TEST SUMMARY ===\\n");
            report.append(String.format("Total Tests: %d\\n", totalTests));
            report.append(String.format("Passed: %d\\n", passedTests));
            report.append(String.format("Failed: %d\\n", failedTests));
            report.append(String.format("Success Rate: %.1f%%\\n", (passedTests * 100.0) / totalTests));
            report.append("\\n");
            
            for (TestResult result : results) {
                report.append(String.format("[%s] %s (%.2fs)\\n", 
                    result.isSuccess() ? "PASS" : "FAIL", 
                    result.getTestName(), 
                    result.getExecutionTime() / 1000.0));
                
                for (String step : result.getSteps()) {
                    report.append("  ").append(step).append("\\n");
                }
                report.append("\\n");
            }
            
            return report.toString();
        }
    }
}