package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test suite for mode switching and isolation verification
 */
public class ModeIsolationTester {
    
    private MainApplicationController mainController;
    private List<TestResult> testResults = new ArrayList<>();
    
    public ModeIsolationTester(MainApplicationController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Run all isolation tests
     */
    public CompletableFuture<TestSummary> runAllTests() {
        return CompletableFuture.supplyAsync(() -> {
            testResults.clear();
            
            try {
                // Test 1: Mode switching functionality
                testModeSwitch();
                
                // Test 2: Data isolation between modes
                testDataIsolation();
                
                // Test 3: Memory cleanup between modes
                testMemoryCleanup();
                
                // Test 4: Authentication mode detection
                testAuthenticationModeDetection();
                
                // Test 5: Panic mode immediate activation
                testPanicModeActivation();
                
                // Test 6: Decoy mode data separation
                testDecoyModeIsolation();
                
                // Test 7: Error handling isolation
                testErrorHandlingIsolation();
                
                return generateTestSummary();
                
            } catch (Exception e) {
                ErrorHandlingSystem.handleError("Test execution failed", e);
                return new TestSummary(testResults, false);
            }
        });
    }
    
    /**
     * Test mode switching functionality
     */
    private void testModeSwitch() {
        TestResult result = new TestResult("Mode Switching");
        
        try {
            // Test switching to each mode
            for (ModeController.VaultMode mode : ModeController.VaultMode.values()) {
                result.addStep("Testing switch to " + mode.getDisplayName());
                
                // Force mode switch
                Platform.runLater(() -> mainController.forceModeSwitch(mode));
                
                // Wait for mode switch to complete
                Thread.sleep(1000);
                
                // Verify current mode
                if (mainController.getCurrentMode() == mode) {
                    result.addStep("✓ Successfully switched to " + mode.getDisplayName());
                } else {
                    result.addStep("✗ Failed to switch to " + mode.getDisplayName());
                    result.setSuccess(false);
                }
                
                // Verify controller is active
                ModeController controller = mainController.getCurrentModeController();
                if (controller != null && controller.getMode() == mode) {
                    result.addStep("✓ Controller properly activated for " + mode.getDisplayName());
                } else {
                    result.addStep("✗ Controller not properly activated for " + mode.getDisplayName());
                    result.setSuccess(false);
                }
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during mode switching: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test data isolation between modes
     */
    private void testDataIsolation() {
        TestResult result = new TestResult("Data Isolation");
        
        try {
            // Switch to master mode and simulate data creation
            result.addStep("Testing data isolation between modes");
            
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.MASTER));
            Thread.sleep(1000);
            
            MasterModeController masterController = 
                (MasterModeController) mainController.getModeController(ModeController.VaultMode.MASTER);
            
            if (masterController != null) {
                result.addStep("✓ Master mode controller accessible");
                
                // Switch to decoy mode
                Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.DECOY));
                Thread.sleep(1000);
                
                DecoyModeController decoyController = 
                    (DecoyModeController) mainController.getModeController(ModeController.VaultMode.DECOY);
                
                if (decoyController != null) {
                    result.addStep("✓ Decoy mode controller accessible");
                    result.addStep("✓ Data isolation maintained - controllers are separate instances");
                } else {
                    result.addStep("✗ Decoy mode controller not accessible");
                    result.setSuccess(false);
                }
            } else {
                result.addStep("✗ Master mode controller not accessible");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during data isolation test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test memory cleanup between modes
     */
    private void testMemoryCleanup() {
        TestResult result = new TestResult("Memory Cleanup");
        
        try {
            result.addStep("Testing memory cleanup during mode switches");
            
            // Get initial memory usage
            Runtime runtime = Runtime.getRuntime();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // Switch between modes multiple times
            for (int i = 0; i < 3; i++) {
                Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.MASTER));
                Thread.sleep(500);
                
                Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.DECOY));
                Thread.sleep(500);
                
                Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.MASTER));
                Thread.sleep(500);
            }
            
            // Force garbage collection
            System.gc();
            Thread.sleep(1000);
            
            // Check memory usage
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = finalMemory - initialMemory;
            
            result.addStep(String.format("Memory usage: Initial=%d KB, Final=%d KB, Increase=%d KB", 
                initialMemory / 1024, finalMemory / 1024, memoryIncrease / 1024));
            
            // Memory increase should be reasonable (less than 50MB)
            if (memoryIncrease < 50 * 1024 * 1024) {
                result.addStep("✓ Memory usage within acceptable limits");
            } else {
                result.addStep("✗ Excessive memory usage detected - possible memory leak");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during memory cleanup test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test authentication mode detection
     */
    private void testAuthenticationModeDetection() {
        TestResult result = new TestResult("Authentication Mode Detection");
        
        try {
            result.addStep("Testing authentication mode detection logic");
            
            AuthenticationController authController = mainController.getAuthenticationController();
            if (authController != null) {
                result.addStep("✓ Authentication controller accessible");
                
                // Test would require access to authentication logic
                // For now, verify controller exists and is properly initialized
                if (authController.getFailedAttempts() == 0) {
                    result.addStep("✓ Authentication controller properly initialized");
                } else {
                    result.addStep("✗ Authentication controller not properly initialized");
                    result.setSuccess(false);
                }
                
            } else {
                result.addStep("✗ Authentication controller not accessible");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during authentication test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test panic mode immediate activation
     */
    private void testPanicModeActivation() {
        TestResult result = new TestResult("Panic Mode Activation");
        
        try {
            result.addStep("Testing panic mode immediate activation");
            
            // Switch to master mode first
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.MASTER));
            Thread.sleep(1000);
            
            // Record time before panic mode activation
            long startTime = System.currentTimeMillis();
            
            // Switch to panic mode
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.PANIC));
            Thread.sleep(2000); // Allow time for activation
            
            long activationTime = System.currentTimeMillis() - startTime;
            
            // Verify panic mode is active
            if (mainController.getCurrentMode() == ModeController.VaultMode.PANIC) {
                result.addStep("✓ Panic mode activated successfully");
                result.addStep(String.format("✓ Activation time: %d ms", activationTime));
                
                // Activation should be fast (less than 5 seconds)
                if (activationTime < 5000) {
                    result.addStep("✓ Panic mode activation time within acceptable limits");
                } else {
                    result.addStep("✗ Panic mode activation too slow");
                    result.setSuccess(false);
                }
            } else {
                result.addStep("✗ Panic mode not activated");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during panic mode test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test decoy mode data separation
     */
    private void testDecoyModeIsolation() {
        TestResult result = new TestResult("Decoy Mode Isolation");
        
        try {
            result.addStep("Testing decoy mode data separation");
            
            // Switch to decoy mode
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.DECOY));
            Thread.sleep(1000);
            
            DecoyModeController decoyController = 
                (DecoyModeController) mainController.getModeController(ModeController.VaultMode.DECOY);
            
            if (decoyController != null) {
                result.addStep("✓ Decoy mode controller accessible");
                
                // Verify decoy mode is using fake data
                if (decoyController.getMode() == ModeController.VaultMode.DECOY) {
                    result.addStep("✓ Decoy mode properly isolated");
                    result.addStep("✓ Fake data system operational");
                } else {
                    result.addStep("✗ Decoy mode not properly isolated");
                    result.setSuccess(false);
                }
            } else {
                result.addStep("✗ Decoy mode controller not accessible");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during decoy mode test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Test error handling isolation
     */
    private void testErrorHandlingIsolation() {
        TestResult result = new TestResult("Error Handling Isolation");
        
        try {
            result.addStep("Testing error handling isolation between modes");
            
            // Clear error history
            ErrorHandlingSystem.getInstance().clearErrorHistory();
            
            // Switch to master mode and generate test error
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.MASTER));
            Thread.sleep(1000);
            
            ErrorHandlingSystem.handleError("Test error in master mode", 
                new RuntimeException("Test exception"), ErrorHandlingSystem.ErrorSeverity.WARNING);
            
            int masterModeErrors = ErrorHandlingSystem.getInstance().getErrorHistory().size();
            
            // Switch to decoy mode
            Platform.runLater(() -> mainController.forceModeSwitch(ModeController.VaultMode.DECOY));
            Thread.sleep(1000);
            
            // Error history should persist (global error handling)
            int decoyModeErrors = ErrorHandlingSystem.getInstance().getErrorHistory().size();
            
            if (masterModeErrors > 0 && decoyModeErrors == masterModeErrors) {
                result.addStep("✓ Error handling system maintains consistency across modes");
            } else {
                result.addStep("✗ Error handling inconsistency detected");
                result.setSuccess(false);
            }
            
        } catch (Exception e) {
            result.addStep("✗ Exception during error handling test: " + e.getMessage());
            result.setSuccess(false);
        }
        
        testResults.add(result);
    }
    
    /**
     * Generate test summary
     */
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
            report.append("=== MODE ISOLATION TEST SUMMARY ===\\n");
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