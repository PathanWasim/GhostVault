package com.ghostvault.util;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced error handler that provides user-friendly error messages
 * while logging technical details for debugging
 */
public class ErrorHandler {
    
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Handle file upload errors with user-friendly messages
     */
    public static String handleFileUploadError(Exception e, String fileName) {
        // Log technical details for debugging
        logger.log(Level.WARNING, "File upload failed for: " + fileName, e);
        
        // Return user-friendly message based on exception type
        if (e instanceof NoSuchFileException) {
            return "The file '" + fileName + "' could not be found. Please check if it still exists.";
        } else if (e instanceof AccessDeniedException) {
            return "Permission denied accessing '" + fileName + "'. Please check file permissions and try again.";
        } else if (e instanceof IOException && e.getMessage().contains("disk")) {
            return "Not enough disk space to upload '" + fileName + "'. Please free up space and try again.";
        } else if (e instanceof IOException && e.getMessage().contains("directory")) {
            return "Could not create vault directory. Please check disk space and permissions.";
        } else if (e instanceof IllegalStateException && e.getMessage().contains("key")) {
            return "Vault encryption not properly initialized. Please restart the application.";
        } else if (e instanceof GeneralSecurityException) {
            return "Encryption failed for '" + fileName + "'. Please try again or restart the application.";
        } else if (e instanceof IOException) {
            return "Failed to read or write '" + fileName + "'. Please check file permissions and disk space.";
        } else {
            return "Unexpected error uploading '" + fileName + "'. Please try again.";
        }
    }
    
    /**
     * Handle directory creation errors
     */
    public static String handleDirectoryError(IOException e, String path) {
        logger.log(Level.WARNING, "Directory creation failed for: " + path, e);
        
        if (e instanceof AccessDeniedException) {
            return "Permission denied creating directory: " + path + ". Please check folder permissions.";
        } else if (e instanceof FileAlreadyExistsException) {
            return "Directory already exists: " + path;
        } else if (e.getMessage() != null && e.getMessage().contains("space")) {
            return "Not enough disk space to create directory: " + path;
        } else {
            return "Failed to create directory: " + path + ". Please check permissions and disk space.";
        }
    }
    
    /**
     * Handle vault initialization errors
     */
    public static String handleVaultInitError(Exception e, String vaultPath) {
        logger.log(Level.SEVERE, "Vault initialization failed for: " + vaultPath, e);
        
        if (e instanceof IOException) {
            return "Could not initialize vault at: " + vaultPath + ". Please check permissions and disk space.";
        } else if (e instanceof SecurityException) {
            return "Security error initializing vault. Please check application permissions.";
        } else {
            return "Failed to initialize vault. Please check the vault location and try again.";
        }
    }
    
    /**
     * Handle metadata loading errors
     */
    public static String handleMetadataError(Exception e) {
        logger.log(Level.WARNING, "Metadata operation failed", e);
        
        if (e instanceof GeneralSecurityException) {
            return "Could not decrypt vault metadata. Please check your password.";
        } else if (e instanceof IOException) {
            return "Could not read vault metadata. The vault may be corrupted or inaccessible.";
        } else {
            return "Metadata operation failed. Please try again.";
        }
    }
    
    /**
     * Log technical error details for debugging
     */
    public static void logTechnicalError(Exception e, String operation) {
        logger.log(Level.SEVERE, "Technical error in operation: " + operation, e);
    }
    
    /**
     * Get a generic user-friendly error message
     */
    public static String getGenericErrorMessage(String operation) {
        return "An error occurred during " + operation + ". Please try again or restart the application.";
    }
    
    /**
     * Check if an error is recoverable
     */
    public static boolean isRecoverableError(Exception e) {
        return !(e instanceof SecurityException || 
                 (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("corrupted")));
    }
    
    /**
     * Check if an error is recoverable (overload for Throwable)
     */
    public static boolean isRecoverableError(Throwable e) {
        return !(e instanceof SecurityException || 
                 e instanceof OutOfMemoryError ||
                 (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("corrupted")));
    }
}