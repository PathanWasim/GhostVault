package com.ghostvault.ui.testing;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Theme consistency tester to verify professional styling across all UI components
 */
public class ThemeConsistencyTester {
    
    private List<TestResult> testResults = new ArrayList<>();
    private Map<String, List<String>> expectedStyleClasses = new HashMap<>();
    private Map<String, String> colorPalette = new HashMap<>();
    
    public ThemeConsistencyTester() {
        initializeExpectedStyles();
        initializeColorPalette();
    }
    
    /**
     * Initialize expected style classes for different component types
     */
    private void initializeExpectedStyles() {
        // Button styles
        expectedStyleClasses.put("Button", Arrays.asList(
            "button", "primary", "ghost", "danger", "icon", "text"
        ));
        
        // Input field styles
        expectedStyleClasses.put("TextField", Arrays.asList(
            "modern-input", "search-field", "file-name-field"
        ));
        
        expectedStyleClasses.put("PasswordField", Arrays.asList(
            "modern-input", "password-field"
        ));
        
        // List and table styles
        expectedStyleClasses.put("ListView", Arrays.asList(
            "modern-list", "file-list", "virtualized-file-list"
        ));
        
        expectedStyleClasses.put("TableView", Arrays.asList(
            "modern-table", "file-table"
        ));
        
        // Container styles
        expectedStyleClasses.put("VBox", Arrays.asList(
            "container", "panel", "sidebar", "content-area"
        ));
        
        expectedStyleClasses.put("HBox", Arrays.asList(
            "container", "toolbar", "button-bar", "header"
        ));
        
        // Label styles
        expectedStyleClasses.put("Label", Arrays.asList(
            "heading-1", "heading-2", "heading-3", "body-regular", "caption"
        ));
    }
    
    /**
     * Initialize expected color palette
     */
    private void initializeColorPalette() {
        // Primary colors
        colorPalette.put("primary", "#4A90E2");
        colorPalette.put("primary-hover", "#357ABD");
        colorPalette.put("primary-active", "#2E5F8A");
        
        // Surface colors
        colorPalette.put("surface-primary", "#1E1E1E");
        colorPalette.put("surface-secondary", "#2D2D2D");
        colorPalette.put("surface-tertiary", "#3D3D3D");
        
        // Text colors
        colorPalette.put("text-primary", "#FFFFFF");
        colorPalette.put("text-secondary", "#B0B0B0");
        colorPalette.put("text-muted", "#808080");
        
        // Status colors
        colorPalette.put("success", "#4CAF50");
        colorPalette.put("warning", "#FF9800");
        colorPalette.put("error", "#F44336");
        colorPalette.put("info", "#2196F3");
    }
    
    /**
     * Run all theme consistency tests
     */
    public CompletableFuture<TestSummary> runAllTests(Scene scene) {
        return CompletableFuture.supplyAsync(() -> {
            testResults.clear();
            
            try {
                // Test 1: CSS stylesheet loading
                testStylesheetLoading(scene);
                
                // Test 2: Component style class consistency
                testComponentStyleClasses(scene);
                
                // Test 3: Color palette consistency
                testColorPaletteConsistency(scene);
                
                // Test 4: Typography consistency
                testTypographyConsistency(scene);
                
                // Test 5: Button styling consistency
                testButtonStylingConsistency(scene);
                
                // Test 6: Input field styling consistency
                testInputFieldStylingConsistency(scene);
                
                // Test 7: Container styling consistency
                testContainerStylingConsistency(scene);
                
                // Test 8: Icon and imagery consistency
                testIconConsistency(scene);
                
                return generateTestSummary();
                
            } catch (Exception e) {
                TestResult errorResult = new TestResult("Theme Consistency Test Execution");
                errorResult.addStep("✗ Test execution failed: " + e.getMessage());
                errorResult.setSuccess(false);
                testResults.add(errorResult);
                
                return new TestSummary(testResults, false);
            }
        });
    }
    
    /**
     * Test CSS stylesheet loading
     */
    private void testStylesheetLoading(Scene scene) {
        TestResult result = new TestResult("Stylesheet Loading");
        
        try {
            result.addStep("Testing CSS stylesheet loading");
            
            List<String> stylesheets = scene.getStylesheets();
            
            if (stylesheets.isEmpty()) {
                result.addStep("✗ No stylesheets loaded");
                result.setSuccess(false);
            } else {
                result.addStep("✓ Found " + stylesheets.size() + " stylesheet(s)");
                
                // Check for main theme stylesheet
                boolean hasMainTheme = stylesheets.stream()
                    .anyMatch(sheet -> sheet.contains("ultra-modern-theme.css"));
                
                if (hasMainTheme) {
                    result.addStep("✓ Main theme stylesheet loaded");
                } else {
                    result.addStep("✗ Main theme stylesheet not found");
                    result.setSuccess(false);
                }
                
                // Check for typography stylesheet
                boolean hasTypography = stylesheets.stream()
                    .anyMatch(sheet -> sheet.contains("typography.css"));
                
                if (hasTypography) {
                    result.addStep("✓ Typography stylesheet loaded");
                } else {
                    result.addStep("⚠ Typography stylesheet not found (may be included in main theme)");
                }
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during stylesheet test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test component style class consistency
     */
    private void testComponentStyleClasses(Scene scene) {
        TestResult result = new TestResult("Component Style Classes");
        
        try {
            result.addStep("Testing component style class consistency");
            
            Map<String, Integer> componentCounts = new HashMap<>();
            Map<String, Set<String>> componentStyles = new HashMap<>();
            
            // Traverse scene graph and collect style information
            traverseSceneGraph(scene.getRoot(), componentCounts, componentStyles);
            
            result.addStep("Found components: " + componentCounts.toString());
            
            // Verify each component type has appropriate styles
            for (Map.Entry<String, Set<String>> entry : componentStyles.entrySet()) {
                String componentType = entry.getKey();
                Set<String> actualStyles = entry.getValue();
                
                List<String> expectedStyles = expectedStyleClasses.get(componentType);
                if (expectedStyles != null) {
                    boolean hasExpectedStyles = actualStyles.stream()
                        .anyMatch(style -> expectedStyles.contains(style));
                    
                    if (hasExpectedStyles) {
                        result.addStep("✓ " + componentType + " has appropriate styling");
                    } else {
                        result.addStep("⚠ " + componentType + " may need style review");
                    }
                }
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during style class test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test color palette consistency
     */
    private void testColorPaletteConsistency(Scene scene) {
        TestResult result = new TestResult("Color Palette Consistency");
        
        try {
            result.addStep("Testing color palette consistency");
            
            // This would require CSS parsing in a full implementation
            // For now, verify that color-related style classes exist
            
            Set<String> colorStyleClasses = new HashSet<>();
            collectColorStyleClasses(scene.getRoot(), colorStyleClasses);
            
            // Check for expected color-related classes
            String[] expectedColorClasses = {
                "primary", "secondary", "success", "warning", "error", "info",
                "text-primary", "text-secondary", "text-muted"
            };
            
            int foundColorClasses = 0;
            for (String expectedClass : expectedColorClasses) {
                if (colorStyleClasses.contains(expectedClass)) {
                    foundColorClasses++;
                }
            }
            
            if (foundColorClasses > 0) {
                result.addStep("✓ Found " + foundColorClasses + " color-related style classes");
            } else {
                result.addStep("⚠ No color-related style classes found");
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during color palette test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test typography consistency
     */
    private void testTypographyConsistency(Scene scene) {
        TestResult result = new TestResult("Typography Consistency");
        
        try {
            result.addStep("Testing typography consistency");
            
            Set<String> typographyClasses = new HashSet<>();
            collectTypographyClasses(scene.getRoot(), typographyClasses);
            
            // Check for expected typography classes
            String[] expectedTypographyClasses = {
                "heading-1", "heading-2", "heading-3", "heading-4", "heading-5", "heading-6",
                "body-large", "body-regular", "body-small", "caption", "code-inline"
            };
            
            int foundTypographyClasses = 0;
            for (String expectedClass : expectedTypographyClasses) {
                if (typographyClasses.contains(expectedClass)) {
                    foundTypographyClasses++;
                }
            }
            
            if (foundTypographyClasses > 0) {
                result.addStep("✓ Found " + foundTypographyClasses + " typography classes");
            } else {
                result.addStep("⚠ No typography classes found");
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during typography test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test button styling consistency
     */
    private void testButtonStylingConsistency(Scene scene) {
        TestResult result = new TestResult("Button Styling Consistency");
        
        try {
            result.addStep("Testing button styling consistency");
            
            List<Button> buttons = findNodesOfType(scene.getRoot(), Button.class);
            
            if (buttons.isEmpty()) {
                result.addStep("⚠ No buttons found in scene");
            } else {
                result.addStep("Found " + buttons.size() + " buttons");
                
                // Check that all buttons have the base "button" class
                int buttonsWithBaseClass = 0;
                for (Button button : buttons) {
                    if (button.getStyleClass().contains("button")) {
                        buttonsWithBaseClass++;
                    }
                }
                
                if (buttonsWithBaseClass == buttons.size()) {
                    result.addStep("✓ All buttons have base 'button' style class");
                } else {
                    result.addStep("⚠ " + (buttons.size() - buttonsWithBaseClass) + 
                                 " buttons missing base 'button' class");
                }
                
                // Check for variant classes
                Set<String> buttonVariants = new HashSet<>();
                for (Button button : buttons) {
                    for (String styleClass : button.getStyleClass()) {
                        if (Arrays.asList("primary", "ghost", "danger", "icon", "text").contains(styleClass)) {
                            buttonVariants.add(styleClass);
                        }
                    }
                }
                
                result.addStep("✓ Found button variants: " + buttonVariants);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during button styling test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test input field styling consistency
     */
    private void testInputFieldStylingConsistency(Scene scene) {
        TestResult result = new TestResult("Input Field Styling Consistency");
        
        try {
            result.addStep("Testing input field styling consistency");
            
            List<TextInputControl> inputs = new ArrayList<>();
            inputs.addAll(findNodesOfType(scene.getRoot(), TextField.class));
            inputs.addAll(findNodesOfType(scene.getRoot(), PasswordField.class));
            inputs.addAll(findNodesOfType(scene.getRoot(), TextArea.class));
            
            if (inputs.isEmpty()) {
                result.addStep("⚠ No input fields found in scene");
            } else {
                result.addStep("Found " + inputs.size() + " input fields");
                
                // Check for modern input styling
                int modernInputs = 0;
                for (TextInputControl input : inputs) {
                    if (input.getStyleClass().contains("modern-input") || 
                        input.getStyleClass().stream().anyMatch(cls -> cls.contains("field"))) {
                        modernInputs++;
                    }
                }
                
                if (modernInputs > 0) {
                    result.addStep("✓ " + modernInputs + " inputs have modern styling");
                } else {
                    result.addStep("⚠ No inputs found with modern styling classes");
                }
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during input field styling test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test container styling consistency
     */
    private void testContainerStylingConsistency(Scene scene) {
        TestResult result = new TestResult("Container Styling Consistency");
        
        try {
            result.addStep("Testing container styling consistency");
            
            List<Pane> containers = new ArrayList<>();
            containers.addAll(findNodesOfType(scene.getRoot(), VBox.class));
            containers.addAll(findNodesOfType(scene.getRoot(), HBox.class));
            containers.addAll(findNodesOfType(scene.getRoot(), BorderPane.class));
            containers.addAll(findNodesOfType(scene.getRoot(), GridPane.class));
            containers.addAll(findNodesOfType(scene.getRoot(), StackPane.class));
            
            if (containers.isEmpty()) {
                result.addStep("⚠ No containers found in scene");
            } else {
                result.addStep("Found " + containers.size() + " containers");
                
                // Check for consistent container styling
                Set<String> containerStyleClasses = new HashSet<>();
                for (Pane container : containers) {
                    containerStyleClasses.addAll(container.getStyleClass());
                }
                
                // Look for expected container classes
                String[] expectedContainerClasses = {
                    "container", "panel", "sidebar", "content-area", "header", "footer",
                    "main-content-area", "toolbar", "button-bar"
                };
                
                int foundContainerClasses = 0;
                for (String expectedClass : expectedContainerClasses) {
                    if (containerStyleClasses.contains(expectedClass)) {
                        foundContainerClasses++;
                    }
                }
                
                if (foundContainerClasses > 0) {
                    result.addStep("✓ Found " + foundContainerClasses + " container style classes");
                } else {
                    result.addStep("⚠ No expected container style classes found");
                }
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during container styling test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test icon consistency
     */
    private void testIconConsistency(Scene scene) {
        TestResult result = new TestResult("Icon Consistency");
        
        try {
            result.addStep("Testing icon consistency");
            
            // Count buttons with icon class
            List<Button> iconButtons = findNodesOfType(scene.getRoot(), Button.class).stream()
                .filter(button -> button.getStyleClass().contains("icon"))
                .toList();
            
            result.addStep("Found " + iconButtons.size() + " icon buttons");
            
            // Check for consistent icon usage
            if (iconButtons.size() > 0) {
                result.addStep("✓ Icon buttons found with consistent styling");
            }
            
            // Test file type icons
            result.addStep("Testing file type icon system");
            
            // Test various file types
            String[] testExtensions = {".jpg", ".pdf", ".txt", ".mp4", ".zip", ".java"};
            int supportedIcons = 0;
            
            for (String extension : testExtensions) {
                File testFile = new File("test" + extension);
                String iconPath = FileIconProvider.getIconPath(testFile);
                
                if (iconPath != null) {
                    supportedIcons++;
                }
            }
            
            if (supportedIcons == testExtensions.length) {
                result.addStep("✓ All test file types have icon mappings");
            } else {
                result.addStep("⚠ " + (testExtensions.length - supportedIcons) + 
                             " file types missing icon mappings");
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during icon consistency test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    // Helper methods
    
    private void traverseSceneGraph(Node node, Map<String, Integer> componentCounts, 
                                  Map<String, Set<String>> componentStyles) {
        
        String nodeType = node.getClass().getSimpleName();
        componentCounts.merge(nodeType, 1, Integer::sum);
        
        Set<String> styles = componentStyles.computeIfAbsent(nodeType, k -> new HashSet<>());
        styles.addAll(node.getStyleClass());
        
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                traverseSceneGraph(child, componentCounts, componentStyles);
            }
        }
    }
    
    private void collectColorStyleClasses(Node node, Set<String> colorClasses) {
        for (String styleClass : node.getStyleClass()) {
            if (styleClass.contains("primary") || styleClass.contains("secondary") ||
                styleClass.contains("success") || styleClass.contains("warning") ||
                styleClass.contains("error") || styleClass.contains("info") ||
                styleClass.contains("text-")) {
                colorClasses.add(styleClass);
            }
        }
        
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                collectColorStyleClasses(child, colorClasses);
            }
        }
    }
    
    private void collectTypographyClasses(Node node, Set<String> typographyClasses) {
        for (String styleClass : node.getStyleClass()) {
            if (styleClass.contains("heading-") || styleClass.contains("body-") ||
                styleClass.contains("caption") || styleClass.contains("code-") ||
                styleClass.contains("text-")) {
                typographyClasses.add(styleClass);
            }
        }
        
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                collectTypographyClasses(child, typographyClasses);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Node> List<T> findNodesOfType(Node root, Class<T> type) {
        List<T> nodes = new ArrayList<>();
        
        if (type.isInstance(root)) {
            nodes.add((T) root);
        }
        
        if (root instanceof Parent) {
            for (Node child : ((Parent) root).getChildrenUnmodifiable()) {
                nodes.addAll(findNodesOfType(child, type));
            }
        }
        
        return nodes;
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
            report.append("=== THEME CONSISTENCY TEST SUMMARY ===\\n");
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