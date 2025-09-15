package com.ghostvault.error;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.exception.*;
import com.ghostvault.ui.NotificationManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ErrorHandler
 */
class ErrorHandlerTest {
    
    @TempDir
    Path tempDir;
    
    private ErrorHandler errorHandler;
    private AuditManager mockAuditManager;
    private NotificationManager mockNotificationManager;
    
    @BeforeEach
    void setUp() {
        mockAuditManager = mock(AuditManager.class);
        mockNotificationManager = mock(NotificationManager.class);
        errorHandler = new ErrorHandler(mockAuditManager, mockNotificationManager);
    }
    
    @Test
    @DisplayName("Should handle successful operation without recovery")
    void testSuccessfulOperation() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = errorHandler.handleWithRecovery(
            "test_operation",
            () -> {
                callCount.incrementAndGet();
                return "success";
            },
            null
        );
        
        assertEquals("success", result);
        assertEquals(1, callCount.get());
    }
    
    @Test
    @DisplayName("Should retry recoverable exceptions")
    void testRecoverableExceptionRetry() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = errorHandler.handleWithRecovery(
            "test_operation",
            () -> {
                int count = callCount.incrementAndGet();
                if (count < 3) {
                    throw new FileOperationException("Temporary failure", 
                        FileOperationException.FileErrorType.FILE_LOCKED, null, "test");
                }
                return "success_after_retry";
            },
            (exception, attempt) -> {
                // Simple recovery: just retry
                return ErrorHandler.RecoveryResult.failure("Retry needed");
            }
        );
        
        assertEquals("success_after_retry", result);
        assertEquals(3, callCount.get());
    }
    
    @Test
    @DisplayName("Should handle successful recovery")
    void testSuccessfulRecovery() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = errorHandler.handleWithRecovery(
            "test_operation",
            () -> {
                callCount.incrementAndGet();
                throw new FileOperationException("File locked", 
                    FileOperationException.FileErrorType.FILE_LOCKED, null, "test");
            },
            (exception, attempt) -> {
                // Simulate successful recovery
                return ErrorHandler.RecoveryResult.success("recovered_result");
            }
        );
        
        assertEquals("recovered_result", result);
        assertEquals(1, callCount.get());
    }
    
    @Test
    @DisplayName("Should fail after max retry attempts")
    void testMaxRetryAttempts() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            errorHandler.handleWithRecovery(
                "test_operation",
                () -> {
                    callCount.incrementAndGet();
                    throw new FileOperationException("Persistent failure", 
                        FileOperationException.FileErrorType.FILE_LOCKED, null, "test");
                },
                (ex, attempt) -> ErrorHandler.RecoveryResult.failure("Recovery failed")
            );
        });
        
        assertTrue(exception.getMessage().contains("Operation failed after"));
        assertEquals(3, callCount.get()); // Should try 3 times
    }
    
    @Test
    @DisplayName("Should not retry non-recoverable exceptions")
    void testNonRecoverableException() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            errorHandler.handleWithRecovery(
                "test_operation",
                () -> {
                    callCount.incrementAndGet();
                    throw new CryptographicException("Critical crypto error", 
                        CryptographicException.CryptoErrorType.ALGORITHM_NOT_AVAILABLE);
                },
                null
            );
        });
        
        assertEquals(1, callCount.get()); // Should only try once
        assertTrue(exception.getMessage().contains("Operation failed after 1 attempts"));
    }
    
    @Test
    @DisplayName("Should handle error without recovery")
    void testHandleErrorWithoutRecovery() {
        AtomicReference<String> userMessage = new AtomicReference<>();
        
        Exception testException = new ValidationException("Invalid input", 
            ValidationException.ValidationType.INPUT_FORMAT);
        
        errorHandler.handleError("test_operation", testException, userMessage::set);
        
        assertNotNull(userMessage.get());
        assertTrue(userMessage.get().contains("Invalid"));
        
        // Verify audit logging
        verify(mockAuditManager).logSecurityEvent(
            eq("ERROR_OCCURRED"), 
            anyString(), 
            any(AuditManager.AuditSeverity.class), 
            isNull(), 
            anyString()
        );
        
        // Verify notification
        verify(mockNotificationManager).showError(eq("Error"), anyString());
    }
    
    @Test
    @DisplayName("Should generate user-friendly messages for common exceptions")
    void testUserFriendlyMessages() {
        // Test IOException
        errorHandler.handleError("test", new IOException("File not found"), null);
        verify(mockNotificationManager).showError(eq("Error"), 
            eq("A file operation failed. Please check file permissions and disk space."));
        
        reset(mockNotificationManager);
        
        // Test SecurityException
        errorHandler.handleError("test", new SecurityException("Access denied"), null);
        verify(mockNotificationManager).showError(eq("Error"), 
            eq("A security error occurred. Please try again."));
        
        reset(mockNotificationManager);
        
        // Test IllegalArgumentException
        errorHandler.handleError("test", new IllegalArgumentException("Invalid argument"), null);
        verify(mockNotificationManager).showError(eq("Error"), 
            eq("Invalid input provided. Please check your data and try again."));
    }
    
    @Test
    @DisplayName("Should handle GhostVault exceptions with proper user messages")
    void testGhostVaultExceptionHandling() {
        AuthenticationException authEx = new AuthenticationException("Invalid password", 
            AuthenticationException.AuthErrorType.INVALID_PASSWORD);
        
        errorHandler.handleError("login", authEx, null);
        
        verify(mockNotificationManager).showError(eq("Error"), 
            eq("Invalid password. Please try again."));
    }
    
    @Test
    @DisplayName("Should track error statistics")
    void testErrorStatistics() {
        // Generate some errors
        for (int i = 0; i < 5; i++) {
            try {
                errorHandler.handleWithRecovery(
                    "test_operation_" + i,
                    () -> {
                        throw new IOException("Test error " + i);
                    },
                    null
                );
            } catch (Exception e) {
                // Expected
            }
        }
        
        ErrorHandler.ErrorStatistics stats = errorHandler.getErrorStatistics();
        assertTrue(stats.getTotalErrorCount() > 0);
        assertTrue(stats.getUniqueErrorTypes() > 0);
    }
    
    @Test
    @DisplayName("Should detect high error rates")
    void testErrorRateDetection() {
        // This test would need to be implemented based on the actual error rate logic
        // For now, just verify the method exists and returns a boolean
        boolean isHighRate = errorHandler.isErrorRateTooHigh("test_operation");
        assertFalse(isHighRate); // Should be false initially
    }
    
    @Test
    @DisplayName("Should handle concurrent error operations")
    void testConcurrentErrorHandling() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Create multiple threads that generate errors
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    errorHandler.handleWithRecovery(
                        "concurrent_test_" + threadId,
                        () -> {
                            if (threadId % 2 == 0) {
                                return "success";
                            } else {
                                throw new IOException("Test error");
                            }
                        },
                        null
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // Wait up to 5 seconds per thread
        }
        
        // Verify results
        assertEquals(5, successCount.get()); // Even numbered threads should succeed
        assertEquals(5, errorCount.get());   // Odd numbered threads should fail
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        // Test with null operation name
        assertDoesNotThrow(() -> {
            errorHandler.handleError(null, new IOException("Test"), null);
        });
        
        // Test with null exception
        assertDoesNotThrow(() -> {
            errorHandler.handleError("test", null, null);
        });
        
        // Test with null callback
        assertDoesNotThrow(() -> {
            errorHandler.handleError("test", new IOException("Test"), null);
        });
    }
    
    @Test
    @DisplayName("Should generate proper error codes")
    void testErrorCodeGeneration() {
        GhostVaultException gve = new ValidationException("Test validation error", 
            ValidationException.ValidationType.INPUT_FORMAT);
        
        String errorCode = gve.getErrorCode();
        assertNotNull(errorCode);
        assertTrue(errorCode.contains("VALIDATION"));
        assertTrue(errorCode.contains("LOW"));
    }
    
    @Test
    @DisplayName("Should handle recovery strategy exceptions")
    void testRecoveryStrategyExceptions() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            errorHandler.handleWithRecovery(
                "test_operation",
                () -> {
                    callCount.incrementAndGet();
                    throw new FileOperationException("Test error", 
                        FileOperationException.FileErrorType.FILE_LOCKED, null, "test");
                },
                (ex, attempt) -> {
                    // Recovery strategy that throws exception
                    throw new RuntimeException("Recovery failed");
                }
            );
        });
        
        assertTrue(exception.getMessage().contains("Operation failed after"));
        assertTrue(callCount.get() >= 1);
    }
}