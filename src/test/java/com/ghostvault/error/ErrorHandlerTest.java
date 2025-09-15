package com.ghostvault.error;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import com.ghostvault.core.VaultInitializer;
import com.ghostvault.exception.CryptographicException;
import com.ghostvault.exception.GhostVaultException;
import com.ghostvault.exception.SecurityException;
import com.ghostvault.exception.VaultException;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.security.PanicModeExecutor;

import java.io.IOException;

/**
 * Comprehensive test for ErrorHandler functionality
 */
public class ErrorHandlerTest {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("         ErrorHandler Comprehensive Test");
        System.out.println("=================================================");
        
        try {
            runAllTests();
            System.out.println("\n✅ All ErrorHandler tests passed!");
            
        } catch (Exception e) {
            System.err.println("\n❌ ErrorHandler test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void runAllTests() throws Exception {
        testBasicErrorHandling();
        testExceptionConversion();
        testRecoveryActions();
        testErrorCounting();
        testCriticalConditions();
        testCustomRecoveryStrategies();
        testErrorStatistics();
    }
    
    /**
     * Test basic error handling functionality
     */
    private static void testBasicErrorHandling() throws Exception {
        System.out.println("\n🧪 Testing basic error handling...");
        
        // Set up error handler
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Test handling a simple exception
        IOException testException = new IOException("Test file not found");
        ErrorHandlingResult result = errorHandler.handleException(testException, "file_operation");
        
        assert result != null : "Should return error handling result";
        assert result.getException() != null : "Should have converted exception";
        assert result.getRecoveryAction() != null : "Should have recovery action";
        
        System.out.println("   ✓ Basic error handling: " + result.getRecoveryAction());
        
        // Test with GhostVaultException
        GhostVaultException gvException = new GhostVaultException(
            GhostVaultException.ErrorCode.FILE_NOT_FOUND, "Test file missing");
        
        ErrorHandlingResult gvResult = errorHandler.handleException(gvException, "vault_operation");
        
        assert gvResult.getException().getErrorCode() == GhostVaultException.ErrorCode.FILE_NOT_FOUND : 
            "Should preserve error code";
        
        System.out.println("   ✓ GhostVault exception handling: " + gvResult.getErrorCode());
        
        System.out.println("   ✅ Basic error handling test passed");
    }
    
    /**
     * Test exception conversion
     */
    private static void testExceptionConversion() throws Exception {
        System.out.println("\n🧪 Testing exception conversion...");
        
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Test various exception types
        Exception[] testExceptions = {
            new IOException("IO error"),
            new java.security.GeneralSecurityException("Crypto error"),
            new OutOfMemoryError("Memory error"),
            new SecurityException("Security violation"),
            new RuntimeException("Generic runtime error")
        };
        
        for (Exception ex : testExceptions) {
            ErrorHandlingResult result = errorHandler.handleException(ex, "test_context");
            
            assert result.getException() instanceof GhostVaultException : 
                "Should convert to GhostVaultException: " + ex.getClass().getSimpleName();
            
            System.out.println("   ✓ Converted " + ex.getClass().getSimpleName() + 
                " to " + result.getException().getErrorCode());
        }
        
        System.out.println("   ✅ Exception conversion test passed");
    }
    
    /**
     * Test recovery actions
     */
    private static void testRecoveryActions() throws Exception {
        System.out.println("\n🧪 Testing recovery actions...");
        
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Test different severity levels
        GhostVaultException[] testExceptions = {
            new GhostVaultException(GhostVaultException.ErrorCode.FILE_NOT_FOUND, 
                GhostVaultException.ErrorSeverity.LOW, true, "Low severity", null, null),
            new GhostVaultException(GhostVaultException.ErrorCode.IO_ERROR, 
                GhostVaultException.ErrorSeverity.MEDIUM, true, "Medium severity", null, null),
            new GhostVaultException(GhostVaultException.ErrorCode.VAULT_CORRUPTED, 
                GhostVaultException.ErrorSeverity.HIGH, false, "High severity", null, null),
            new GhostVaultException(GhostVaultException.ErrorCode.TAMPERING_DETECTED, 
                GhostVaultException.ErrorSeverity.CRITICAL, false, "Critical severity", null, null)
        };
        
        for (GhostVaultException ex : testExceptions) {
            ErrorHandlingResult result = errorHandler.handleException(ex, "test_context");
            
            System.out.println("   ✓ " + ex.getSeverity() + " -> " + result.getRecoveryAction());
            
            // Verify appropriate recovery actions
            switch (ex.getSeverity()) {
                case LOW:
                    assert result.getRecoveryAction() == ErrorHandler.RecoveryAction.IGNORE : 
                        "Low severity should be ignored";
                    break;
                case CRITICAL:
                    assert result.getRecoveryAction() == ErrorHandler.RecoveryAction.PANIC_MODE ||
                           result.getRecoveryAction() == ErrorHandler.RecoveryAction.RESTART_APPLICATION : 
                        "Critical severity should trigger panic or restart";
                    break;
            }
        }
        
        System.out.println("   ✅ Recovery actions test passed");
    }
    
    /**
     * Test error counting and thresholds
     */
    private static void testErrorCounting() throws Exception {
        System.out.println("\n🧪 Testing error counting...");
        
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Generate multiple crypto errors
        for (int i = 0; i < 3; i++) {
            CryptographicException cryptoEx = CryptographicException.encryptionFailed("Test " + i, null);
            errorHandler.handleException(cryptoEx, "crypto_test");
        }
        
        // Generate file errors
        for (int i = 0; i < 5; i++) {
            IOException fileEx = new IOException("File error " + i);
            errorHandler.handleException(fileEx, "file_test");
        }
        
        // Check statistics
        ErrorStatistics stats = errorHandler.getErrorStatistics();
        
        assert stats.getTotalErrors() >= 8 : "Should have at least 8 errors";
        assert stats.getCryptographicErrors() >= 3 : "Should have at least 3 crypto errors";
        
        System.out.println("   ✓ Error counts: " + stats.getTotalErrors() + " total, " + 
            stats.getCryptographicErrors() + " crypto");
        System.out.println("   ✓ Health score: " + stats.getHealthScore() + "/100");
        
        System.out.println("   ✅ Error counting test passed");
    }
    
    /**
     * Test critical conditions detection
     */
    private static void testCriticalConditions() throws Exception {
        System.out.println("\n🧪 Testing critical conditions...");
        
        // Create error handler without panic executor to avoid actual panic
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Test tampering detection
        SecurityException tamperingEx = SecurityException.tamperingDetected("Test tampering");
        ErrorHandlingResult result = errorHandler.handleException(tamperingEx, "security_test");
        
        assert result.getRecoveryAction() == ErrorHandler.RecoveryAction.PANIC_MODE : 
            "Tampering should trigger panic mode";
        
        System.out.println("   ✓ Tampering detection triggers panic mode");
        
        // Test intrusion detection
        SecurityException intrusionEx = SecurityException.intrusionDetected("192.168.1.100", "Brute force");
        ErrorHandlingResult intrusionResult = errorHandler.handleException(intrusionEx, "auth_test");
        
        assert intrusionResult.getRecoveryAction() == ErrorHandler.RecoveryAction.PANIC_MODE : 
            "Intrusion should trigger panic mode";
        
        System.out.println("   ✓ Intrusion detection triggers panic mode");
        
        System.out.println("   ✅ Critical conditions test passed");
    }
    
    /**
     * Test custom recovery strategies
     */
    private static void testCustomRecoveryStrategies() throws Exception {
        System.out.println("\n🧪 Testing custom recovery strategies...");
        
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Add custom recovery strategy
        errorHandler.addRecoveryStrategy(new ErrorHandler.RecoveryStrategy() {
            @Override
            public boolean canHandle(GhostVaultException exception, String context) {
                return exception.getErrorCode() == GhostVaultException.ErrorCode.NETWORK_ERROR;
            }
            
            @Override
            public ErrorHandler.RecoveryAction getRecoveryAction(GhostVaultException exception, String context) {
                return ErrorHandler.RecoveryAction.FALLBACK;
            }
        });
        
        // Test custom strategy
        GhostVaultException networkEx = new GhostVaultException(
            GhostVaultException.ErrorCode.NETWORK_ERROR, "Network timeout");
        
        ErrorHandlingResult result = errorHandler.handleException(networkEx, "network_test");
        
        assert result.getRecoveryAction() == ErrorHandler.RecoveryAction.FALLBACK : 
            "Custom strategy should return FALLBACK";
        
        System.out.println("   ✓ Custom recovery strategy applied");
        
        System.out.println("   ✅ Custom recovery strategies test passed");
    }
    
    /**
     * Test error statistics
     */
    private static void testErrorStatistics() throws Exception {
        System.out.println("\n🧪 Testing error statistics...");
        
        ErrorHandler errorHandler = new ErrorHandler(null, null);
        
        // Generate various errors
        errorHandler.handleException(new IOException("File error 1"), "test");
        errorHandler.handleException(CryptographicException.encryptionFailed("Crypto error", null), "test");
        errorHandler.handleException(SecurityException.accessDenied("resource"), "test");
        errorHandler.handleException(VaultException.corrupted("Vault corrupted"), "test");
        
        ErrorStatistics stats = errorHandler.getErrorStatistics();
        
        assert stats.getTotalErrors() > 0 : "Should have errors";
        assert stats.getHealthScore() >= 0 && stats.getHealthScore() <= 100 : 
            "Health score should be 0-100";
        
        String summary = stats.getSummary();
        assert summary.contains("Error Statistics") : "Summary should contain statistics";
        
        System.out.println("   ✓ Statistics generated:");
        System.out.println("     Total errors: " + stats.getTotalErrors());
        System.out.println("     Health: " + stats.getHealthStatus() + " (" + stats.getHealthScore() + "/100)");
        System.out.println("     Stability: " + (stats.indicatesInstability() ? "Unstable" : "Stable"));
        
        System.out.println("   ✅ Error statistics test passed");
    }
}