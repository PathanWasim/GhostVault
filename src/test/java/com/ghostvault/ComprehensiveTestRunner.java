package com.ghostvault;

import com.ghostvault.performance.PerformanceTestFramework;
import com.ghostvault.security.SecurityValidationFramework;

/**
 * Comprehensive test runner that executes all test frameworks
 * This is the main entry point for running all GhostVault tests
 */
public class ComprehensiveTestRunner {
    
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        GhostVault Comprehensive Test Runner");
        System.out.println("==================================================");
        System.out.println("Running all test suites: Unit, Integration, Security, and Performance");
        System.out.println();
        
        boolean allTestsPassed = true;
        long totalStartTime = System.currentTimeMillis();
        
        try {
            // 1. Run Main Test Suite
            System.out.println("🧪 PHASE 1: Running Main Test Suite");
            System.out.println("==================================================");
            try {
                TestSuite.main(new String[]{});
                System.out.println("✅ Main Test Suite completed successfully");
            } catch (Exception e) {
                System.err.println("❌ Main Test Suite failed: " + e.getMessage());
                allTestsPassed = false;
            }
            
            System.out.println("\n");
            
            // 2. Run Security Validation Framework
            System.out.println("🔐 PHASE 2: Running Security Validation Framework");
            System.out.println("==================================================");
            try {
                SecurityValidationFramework.main(new String[]{});
                System.out.println("✅ Security Validation completed successfully");
            } catch (Exception e) {
                System.err.println("❌ Security Validation failed: " + e.getMessage());
                allTestsPassed = false;
            }
            
            System.out.println("\n");
            
            // 3. Run Performance Test Framework
            System.out.println("⚡ PHASE 3: Running Performance Test Framework");
            System.out.println("==================================================");
            try {
                PerformanceTestFramework.main(new String[]{});
                System.out.println("✅ Performance Testing completed successfully");
            } catch (Exception e) {
                System.err.println("❌ Performance Testing failed: " + e.getMessage());
                allTestsPassed = false;
            }
            
            // Generate final comprehensive report
            generateComprehensiveReport(totalStartTime, allTestsPassed);
            
        } catch (Exception e) {
            System.err.println("❌ Comprehensive test execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // Exit with appropriate code
        System.exit(allTestsPassed ? 0 : 1);
    }
    
    /**
     * Generate comprehensive test report
     */
    private static void generateComprehensiveReport(long startTime, boolean allTestsPassed) {
        long totalDuration = System.currentTimeMillis() - startTime;
        
        System.out.println("\n\n==================================================");
        System.out.println("           COMPREHENSIVE TEST REPORT");
        System.out.println("==================================================");
        
        System.out.printf("Total Execution Time: %.2f seconds%n", totalDuration / 1000.0);
        System.out.println();
        
        if (allTestsPassed) {
            System.out.println("🎉 ALL TEST PHASES COMPLETED SUCCESSFULLY!");
            System.out.println();
            System.out.println("✅ Test Coverage Summary:");
            System.out.println("   • Unit Tests: Core functionality validated");
            System.out.println("   • Integration Tests: Component interaction verified");
            System.out.println("   • Security Tests: Cryptographic security validated");
            System.out.println("   • Performance Tests: Performance requirements met");
            System.out.println();
            System.out.println("🚀 GhostVault is ready for deployment!");
            System.out.println();
            System.out.println("📋 Deployment Checklist:");
            System.out.println("   ✅ All unit tests passing");
            System.out.println("   ✅ Security validation complete");
            System.out.println("   ✅ Performance benchmarks met");
            System.out.println("   ✅ Integration tests successful");
            System.out.println("   ✅ Memory management verified");
            System.out.println("   ✅ Encryption standards validated");
            
        } else {
            System.out.println("❌ SOME TEST PHASES FAILED!");
            System.out.println();
            System.out.println("⚠️  Issues detected in one or more test phases.");
            System.out.println("   Please review the detailed output above and fix all issues");
            System.out.println("   before proceeding with deployment.");
            System.out.println();
            System.out.println("🔧 Recommended Actions:");
            System.out.println("   1. Review failed test output for specific issues");
            System.out.println("   2. Fix any security vulnerabilities immediately");
            System.out.println("   3. Address performance bottlenecks");
            System.out.println("   4. Verify all unit tests pass");
            System.out.println("   5. Re-run comprehensive tests after fixes");
        }
        
        System.out.println("\n==================================================");
        System.out.println("         End of Comprehensive Test Report");
        System.out.println("==================================================");
    }
}