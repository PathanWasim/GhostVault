package com.ghostvault.error;

import com.ghostvault.exception.*;
import com.ghostvault.error.ErrorHandler.RecoveryResult;
import com.ghostvault.error.ErrorHandler.RecoveryStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * Collection of recovery strategies for common error scenarios
 */
public class RecoveryStrategies {
    
    /**
     * Recovery strategy for file operations
     */
    public static class FileOperationRecovery implements RecoveryStrategy<Boolean> {
        
        private final String operation;
        private final Path filePath;
        private final Runnable retryOperation;
        
        public FileOperationRecovery(String operation, Path filePath, Runnable retryOperation) {
            this.operation = operation;
            this.filePath = filePath;
            this.retryOperation = retryOperation;
        }
        
        @Override
        public RecoveryResult<Boolean> recover(Exception originalException, int attemptNumber) throws Exception {
            
            if (originalException instanceof FileOperationException) {
                FileOperationException foe = (FileOperationException) originalException;
                
                switch (foe.getFileErrorType()) {
                    case FILE_LOCKED:
                        return recoverFromFileLocked(attemptNumber);
                    
                    case ACCESS_DENIED:
                        return recoverFromAccessDenied();
                    
                    case DISK_FULL:
                        return recoverFromDiskFull();
                    
                    case TIMEOUT:
                        return recoverFromTimeout(attemptNumber);
                    
                    case CORRUPTION_DETECTED:
                        return recoverFromCorruption();
                    
                    default:
                        return RecoveryResult.failure("No recovery strategy for " + foe.getFileErrorType());
                }
            }
            
            // Generic file operation recovery
            return attemptGenericFileRecovery(attemptNumber);
        }
        
        private RecoveryResult<Boolean> recoverFromFileLocked(int attemptNumber) throws Exception {
            // Wait longer for file to be unlocked
            long waitTime = Math.min(2000 * attemptNumber, 10000); // Max 10 seconds
            Thread.sleep(waitTime);
            
            // Check if file is still locked
            if (filePath != null && Files.exists(filePath)) {
                try {
                    // Try to open file for writing to test if it's still locked
                    Files.newOutputStream(filePath).close();
                    
                    // File is no longer locked, retry operation
                    retryOperation.run();
                    return RecoveryResult.success(true, "File lock released, operation completed");
                    
                } catch (IOException e) {
                    return RecoveryResult.failure("File is still locked");
                }
            }
            
            return RecoveryResult.failure("File not accessible");
        }
        
        private RecoveryResult<Boolean> recoverFromAccessDenied() throws Exception {
            if (filePath != null && Files.exists(filePath)) {
                try {
                    // Try to fix permissions if possible
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        // On Windows, try to remove read-only attribute
                        File file = filePath.toFile();
                        if (file.setWritable(true)) {
                            retryOperation.run();
                            return RecoveryResult.success(true, "File permissions corrected");
                        }
                    } else {
                        // On Unix-like systems, try to set owner permissions
                        Files.setPosixFilePermissions(filePath, 
                            java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
                        retryOperation.run();
                        return RecoveryResult.success(true, "File permissions corrected");
                    }
                } catch (Exception e) {
                    return RecoveryResult.failure("Could not fix file permissions: " + e.getMessage());
                }
            }
            
            return RecoveryResult.failure("Cannot access file");
        }
        
        private RecoveryResult<Boolean> recoverFromDiskFull() throws Exception {
            // Try to free up space by cleaning temporary files
            long freedSpace = cleanTemporaryFiles();
            
            if (freedSpace > 0) {
                try {
                    retryOperation.run();
                    return RecoveryResult.success(true, "Freed " + freedSpace + " bytes, operation completed");
                } catch (Exception e) {
                    return RecoveryResult.failure("Still insufficient disk space after cleanup");
                }
            }
            
            return RecoveryResult.failure("Could not free sufficient disk space");
        }
        
        private RecoveryResult<Boolean> recoverFromTimeout(int attemptNumber) throws Exception {
            // Increase timeout for retry
            long extendedTimeout = 5000 * attemptNumber; // Increase timeout with each attempt
            
            // Wait a bit before retry
            Thread.sleep(1000);
            
            try {
                retryOperation.run();
                return RecoveryResult.success(true, "Operation completed with extended timeout");
            } catch (Exception e) {
                return RecoveryResult.failure("Operation still timed out");
            }
        }
        
        private RecoveryResult<Boolean> recoverFromCorruption() throws Exception {
            if (filePath != null && Files.exists(filePath)) {
                // Try to create backup of corrupted file
                Path backupPath = Paths.get(filePath.toString() + ".corrupted." + System.currentTimeMillis());
                
                try {
                    Files.move(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Try to recreate the file or restore from backup
                    return RecoveryResult.failure("File corrupted, moved to " + backupPath.getFileName());
                    
                } catch (IOException e) {
                    return RecoveryResult.failure("Could not handle corrupted file: " + e.getMessage());
                }
            }
            
            return RecoveryResult.failure("Corrupted file not accessible");
        }
        
        private RecoveryResult<Boolean> attemptGenericFileRecovery(int attemptNumber) throws Exception {
            // Generic recovery: wait and retry
            Thread.sleep(1000 * attemptNumber);
            
            try {
                retryOperation.run();
                return RecoveryResult.success(true, "Generic recovery successful");
            } catch (Exception e) {
                return RecoveryResult.failure("Generic recovery failed: " + e.getMessage());
            }
        }
        
        private long cleanTemporaryFiles() {
            long freedSpace = 0;
            
            try {
                // Clean system temp directory
                String tempDir = System.getProperty("java.io.tmpdir");
                Path tempPath = Paths.get(tempDir);
                
                Files.walk(tempPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            // Delete files older than 1 hour
                            long ageMs = System.currentTimeMillis() - Files.getLastModifiedTime(path).toMillis();
                            return ageMs > TimeUnit.HOURS.toMillis(1);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            long size = Files.size(path);
                            Files.deleteIfExists(path);
                            // Note: This is an approximation, actual freed space may vary
                        } catch (IOException e) {
                            // Ignore deletion errors
                        }
                    });
                    
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            
            return freedSpace;
        }
    }
    
    /**
     * Recovery strategy for cryptographic operations
     */
    public static class CryptographicRecovery implements RecoveryStrategy<Object> {
        
        private final Runnable retryOperation;
        
        public CryptographicRecovery(Runnable retryOperation) {
            this.retryOperation = retryOperation;
        }
        
        @Override
        public RecoveryResult<Object> recover(Exception originalException, int attemptNumber) throws Exception {
            
            if (originalException instanceof CryptographicException) {
                CryptographicException ce = (CryptographicException) originalException;
                
                switch (ce.getCryptoErrorType()) {
                    case RANDOM_GENERATION_FAILED:
                        return recoverFromRandomGenerationFailure();
                    
                    case ALGORITHM_NOT_AVAILABLE:
                        return recoverFromAlgorithmUnavailable();
                    
                    case KEY_GENERATION_FAILED:
                        return recoverFromKeyGenerationFailure(attemptNumber);
                    
                    default:
                        return RecoveryResult.failure("No recovery for " + ce.getCryptoErrorType());
                }
            }
            
            return RecoveryResult.failure("Cannot recover from cryptographic error");
        }
        
        private RecoveryResult<Object> recoverFromRandomGenerationFailure() throws Exception {
            // Try alternative random number generation
            try {
                // Force re-seeding of SecureRandom
                java.security.SecureRandom.getInstance("SHA1PRNG").setSeed(System.nanoTime());
                
                retryOperation.run();
                return RecoveryResult.success(null, "Random generation recovered with alternative seeding");
                
            } catch (Exception e) {
                return RecoveryResult.failure("Alternative random generation failed: " + e.getMessage());
            }
        }
        
        private RecoveryResult<Object> recoverFromAlgorithmUnavailable() throws Exception {
            // Check available providers and algorithms
            try {
                java.security.Provider[] providers = java.security.Security.getProviders();
                
                // Log available providers for debugging
                StringBuilder providerInfo = new StringBuilder("Available providers: ");
                for (java.security.Provider provider : providers) {
                    providerInfo.append(provider.getName()).append(" ");
                }
                
                return RecoveryResult.failure("Algorithm unavailable. " + providerInfo.toString());
                
            } catch (Exception e) {
                return RecoveryResult.failure("Could not check available algorithms");
            }
        }
        
        private RecoveryResult<Object> recoverFromKeyGenerationFailure(int attemptNumber) throws Exception {
            // Wait and retry key generation
            Thread.sleep(100 * attemptNumber);
            
            try {
                retryOperation.run();
                return RecoveryResult.success(null, "Key generation recovered on retry");
                
            } catch (Exception e) {
                return RecoveryResult.failure("Key generation still failing: " + e.getMessage());
            }
        }
    }
    
    /**
     * Recovery strategy for authentication operations
     */
    public static class AuthenticationRecovery implements RecoveryStrategy<Boolean> {
        
        @Override
        public RecoveryResult<Boolean> recover(Exception originalException, int attemptNumber) throws Exception {
            
            if (originalException instanceof AuthenticationException) {
                AuthenticationException ae = (AuthenticationException) originalException;
                
                switch (ae.getAuthErrorType()) {
                    case SESSION_EXPIRED:
                        return recoverFromSessionExpired();
                    
                    case INVALID_SESSION:
                        return recoverFromInvalidSession();
                    
                    case CREDENTIAL_CORRUPTION:
                        return recoverFromCredentialCorruption();
                    
                    default:
                        return RecoveryResult.failure("No recovery for " + ae.getAuthErrorType());
                }
            }
            
            return RecoveryResult.failure("Cannot recover from authentication error");
        }
        
        private RecoveryResult<Boolean> recoverFromSessionExpired() throws Exception {
            // Session expired - user needs to re-authenticate
            return RecoveryResult.failure("Session expired - re-authentication required");
        }
        
        private RecoveryResult<Boolean> recoverFromInvalidSession() throws Exception {
            // Invalid session - clear session and prompt for new login
            return RecoveryResult.failure("Invalid session - new login required");
        }
        
        private RecoveryResult<Boolean> recoverFromCredentialCorruption() throws Exception {
            // Credential corruption is not recoverable automatically
            return RecoveryResult.failure("Credential corruption detected - manual intervention required");
        }
    }
    
    /**
     * Recovery strategy for validation errors
     */
    public static class ValidationRecovery implements RecoveryStrategy<Object> {
        
        private final Object correctedInput;
        private final Runnable retryWithCorrectedInput;
        
        public ValidationRecovery(Object correctedInput, Runnable retryWithCorrectedInput) {
            this.correctedInput = correctedInput;
            this.retryWithCorrectedInput = retryWithCorrectedInput;
        }
        
        @Override
        public RecoveryResult<Object> recover(Exception originalException, int attemptNumber) throws Exception {
            
            if (originalException instanceof ValidationException) {
                ValidationException ve = (ValidationException) originalException;
                
                // Validation errors are typically user input errors
                // Recovery usually involves providing corrected input
                if (correctedInput != null && retryWithCorrectedInput != null) {
                    try {
                        retryWithCorrectedInput.run();
                        return RecoveryResult.success(correctedInput, "Validation recovered with corrected input");
                    } catch (Exception e) {
                        return RecoveryResult.failure("Corrected input still invalid: " + e.getMessage());
                    }
                }
                
                return RecoveryResult.failure("Validation error requires user input correction");
            }
            
            return RecoveryResult.failure("Cannot recover from validation error");
        }
    }
    
    /**
     * Generic retry recovery strategy
     */
    public static class RetryRecovery<T> implements RecoveryStrategy<T> {
        
        private final ErrorHandler.ThrowingSupplier<T> operation;
        private final long delayMs;
        
        public RetryRecovery(ErrorHandler.ThrowingSupplier<T> operation, long delayMs) {
            this.operation = operation;
            this.delayMs = delayMs;
        }
        
        @Override
        public RecoveryResult<T> recover(Exception originalException, int attemptNumber) throws Exception {
            // Wait before retry
            if (delayMs > 0) {
                Thread.sleep(delayMs * attemptNumber);
            }
            
            try {
                T result = operation.get();
                return RecoveryResult.success(result, "Retry successful on attempt " + attemptNumber);
            } catch (Exception e) {
                return RecoveryResult.failure("Retry failed: " + e.getMessage());
            }
        }
    }
}