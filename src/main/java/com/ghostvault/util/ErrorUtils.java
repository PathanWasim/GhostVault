package com.ghostvault.util;

import com.ghostvault.error.ErrorHandler;
import com.ghostvault.error.ErrorHandlingResult;
import com.ghostvault.exception.GhostVaultException;
import com.ghostvault.ui.ErrorDialog;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for consistent error handling throughout the application
 */
public class ErrorUtils {
    
    private static ErrorHandler globalErrorHandler;
    private static Stage primaryStage;
    
    /**
     * Initialize global error handler
     */
    public static void initialize(ErrorHandler errorHandler, Stage stage) {
        globalErrorHandler = errorHandler;
        primaryStage = stage;
    }
    
    /**
     * Execute operation with automatic error handling
     */
    public static <T> T executeWithErrorHandling(Supplier<T> operation, String context) {
        return executeWithErrorHandling(operation, context, null);
    }
    
    /**
     * Execute operation with automatic error handling and custom fallback
     */
    public static <T> T executeWithErrorHandling(Supplier<T> operation, String context, T fallbackValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleError(e, context);
            return fallbackValue;
        }
    }
    
    /**
     * Execute operation with error handling and user interaction
     */
    public static <T> T executeWithUserErrorHandling(Supplier<T> operation, String context) {
        int maxRetries = 3;
        int attempts = 0;
        
        while (attempts < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                attempts++;
                
                ErrorHandlingResult result = handleError(e, context);
                
                if (result.requiresUserIntervention() && primaryStage != null) {
                    // Show error dialog on JavaFX thread
                    CompletableFuture<Boolean> userChoice = new CompletableFuture<>();
                    
                    Platform.runLater(() -> {
                        ErrorDialog dialog = new ErrorDialog(result);
                        ErrorDialog.ErrorDialogResult dialogResult = dialog.showDialog(primaryStage);
                        userChoice.complete(dialogResult.shouldRetry());
                    });
                    
                    try {
                        boolean shouldRetry = userChoice.get();
                        if (!shouldRetry || attempts >= maxRetries) {
                            break;
                        }
                    } catch (Exception ex) {
                        break;
                    }
                } else if (!result.isRecovered()) {
                    break;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Handle error using global error handler
     */
    public static ErrorHandlingResult handleError(Throwable throwable, String context) {
        if (globalErrorHandler != null) {
            return globalErrorHandler.handleException(throwable, context);
        } else {
            // Fallback error handling
            System.err.println("ERROR in " + context + ": " + throwable.getMessage());
            throwable.printStackTrace();
            
            GhostVaultException gvException = new GhostVaultException(
                GhostVaultException.ErrorCode.INTERNAL_ERROR, 
                throwable.getMessage(), throwable);
            
            return new ErrorHandlingResult(gvException, ErrorHandler.RecoveryAction.USER_INTERVENTION, 
                false, "Please contact support");
        }
    }
    
    /**
     * Show error to user (UI thread safe)
     */
    public static void showErrorToUser(String title, String message) {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                ErrorDialog.showError(primaryStage, title, message);
            });
        } else {
            System.err.println("ERROR - " + title + ": " + message);
        }
    }
    
    /**
     * Show warning to user (UI thread safe)
     */
    public static void showWarningToUser(String title, String message) {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                ErrorDialog.showWarning(primaryStage, title, message);
            });
        } else {
            System.out.println("WARNING - " + title + ": " + message);
        }
    }
    
    /**
     * Show info to user (UI thread safe)
     */
    public static void showInfoToUser(String title, String message) {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                ErrorDialog.showInfo(primaryStage, title, message);
            });
        } else {
            System.out.println("INFO - " + title + ": " + message);
        }
    }
    
    /**
     * Validate parameter and throw appropriate exception if invalid
     */
    public static void validateNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
    
    /**
     * Validate string parameter
     */
    public static void validateNotEmpty(String parameter, String parameterName) {
        if (parameter == null || parameter.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    /**
     * Validate numeric parameter
     */
    public static void validatePositive(int parameter, String parameterName) {
        if (parameter <= 0) {
            throw new IllegalArgumentException(parameterName + " must be positive");
        }
    }
    
    /**
     * Validate numeric parameter
     */
    public static void validateNonNegative(long parameter, String parameterName) {
        if (parameter < 0) {
            throw new IllegalArgumentException(parameterName + " cannot be negative");
        }
    }
    
    /**
     * Validate array parameter
     */
    public static void validateNotEmpty(byte[] parameter, String parameterName) {
        if (parameter == null || parameter.length == 0) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    /**
     * Create user-friendly error message
     */
    public static String createUserFriendlyMessage(Throwable throwable) {
        if (throwable instanceof GhostVaultException) {
            return ((GhostVaultException) throwable).getUserMessage();
        }
        
        // Map common exceptions to user-friendly messages
        if (throwable instanceof java.io.FileNotFoundException) {
            return "The requested file could not be found.";
        }
        
        if (throwable instanceof java.io.IOException) {
            return "A file operation failed. Please check file permissions and disk space.";
        }
        
        if (throwable instanceof java.security.GeneralSecurityException) {
            return "A security operation failed. Please check your password and try again.";
        }
        
        if (throwable instanceof OutOfMemoryError) {
            return "The application is running out of memory. Please close other applications and try again.";
        }
        
        if (throwable instanceof IllegalArgumentException) {
            return "Invalid input provided: " + throwable.getMessage();
        }
        
        // Generic message for unknown exceptions
        return "An unexpected error occurred. Please try again or contact support if the problem persists.";
    }
    
    /**
     * Check if error is recoverable
     */
    public static boolean isRecoverable(Throwable throwable) {
        if (throwable instanceof GhostVaultException) {
            return ((GhostVaultException) throwable).isRecoverable();
        }
        
        // Most common exceptions are recoverable
        return !(throwable instanceof OutOfMemoryError ||
                throwable instanceof SecurityException ||
                throwable instanceof Error);
    }
    
    /**
     * Get error severity
     */
    public static GhostVaultException.ErrorSeverity getErrorSeverity(Throwable throwable) {
        if (throwable instanceof GhostVaultException) {
            return ((GhostVaultException) throwable).getSeverity();
        }
        
        if (throwable instanceof OutOfMemoryError || throwable instanceof SecurityException) {
            return GhostVaultException.ErrorSeverity.CRITICAL;
        }
        
        if (throwable instanceof java.security.GeneralSecurityException) {
            return GhostVaultException.ErrorSeverity.HIGH;
        }
        
        if (throwable instanceof java.io.IOException) {
            return GhostVaultException.ErrorSeverity.MEDIUM;
        }
        
        return GhostVaultException.ErrorSeverity.LOW;
    }
    
    /**
     * Log error for debugging (development mode)
     */
    public static void logErrorForDebugging(Throwable throwable, String context) {
        if (Boolean.getBoolean("com.ghostvault.debug")) {
            System.err.println("DEBUG ERROR in " + context + ":");
            System.err.println("  Type: " + throwable.getClass().getSimpleName());
            System.err.println("  Message: " + throwable.getMessage());
            System.err.println("  Severity: " + getErrorSeverity(throwable));
            System.err.println("  Recoverable: " + isRecoverable(throwable));
            
            if (throwable.getCause() != null) {
                System.err.println("  Caused by: " + throwable.getCause().getMessage());
            }
        }
    }
}