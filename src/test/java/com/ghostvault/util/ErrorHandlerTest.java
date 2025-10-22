package com.ghostvault.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;

/**
 * Tests for ErrorHandler utility class
 */
public class ErrorHandlerTest {
    
    @Test
    public void testFileUploadError_NoSuchFileException() {
        NoSuchFileException exception = new NoSuchFileException("test.txt");
        String result = ErrorHandler.handleFileUploadError(exception, "test.txt");
        
        assertTrue(result.contains("could not be found"));
        assertTrue(result.contains("test.txt"));
    }
    
    @Test
    public void testFileUploadError_AccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("test.txt");
        String result = ErrorHandler.handleFileUploadError(exception, "test.txt");
        
        assertTrue(result.contains("Permission denied"));
        assertTrue(result.contains("test.txt"));
    }
    
    @Test
    public void testFileUploadError_GeneralSecurityException() {
        GeneralSecurityException exception = new GeneralSecurityException("Encryption failed");
        String result = ErrorHandler.handleFileUploadError(exception, "test.txt");
        
        assertTrue(result.contains("Encryption failed"));
        assertTrue(result.contains("test.txt"));
    }
    
    @Test
    public void testFileUploadError_IllegalStateException() {
        IllegalStateException exception = new IllegalStateException("Encryption key not set");
        String result = ErrorHandler.handleFileUploadError(exception, "test.txt");
        
        assertTrue(result.contains("not properly initialized"));
    }
    
    @Test
    public void testDirectoryError_AccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("/test/path");
        String result = ErrorHandler.handleDirectoryError(exception, "/test/path");
        
        assertTrue(result.contains("Permission denied"));
        assertTrue(result.contains("/test/path"));
    }
    
    @Test
    public void testMetadataError_GeneralSecurityException() {
        GeneralSecurityException exception = new GeneralSecurityException("Decryption failed");
        String result = ErrorHandler.handleMetadataError(exception);
        
        assertTrue(result.contains("decrypt vault metadata"));
        assertTrue(result.contains("password"));
    }
    
    @Test
    public void testMetadataError_IOException() {
        IOException exception = new IOException("File not found");
        String result = ErrorHandler.handleMetadataError(exception);
        
        assertTrue(result.contains("read vault metadata"));
        assertTrue(result.contains("corrupted"));
    }
    
    @Test
    public void testVaultInitError_IOException() {
        IOException exception = new IOException("Cannot create directory");
        String result = ErrorHandler.handleVaultInitError(exception, "/vault/path");
        
        assertTrue(result.contains("initialize vault"));
        assertTrue(result.contains("/vault/path"));
    }
    
    @Test
    public void testIsRecoverableError_SecurityException() {
        SecurityException exception = new SecurityException("Access denied");
        boolean result = ErrorHandler.isRecoverableError(exception);
        
        assertFalse(result); // Security exceptions are not recoverable
    }
    
    @Test
    public void testIsRecoverableError_IOException() {
        IOException exception = new IOException("File not found");
        boolean result = ErrorHandler.isRecoverableError(exception);
        
        assertTrue(result); // Regular IO exceptions are recoverable
    }
    
    @Test
    public void testIsRecoverableError_CorruptedData() {
        IOException exception = new IOException("Data is corrupted");
        boolean result = ErrorHandler.isRecoverableError(exception);
        
        assertFalse(result); // Corrupted data is not recoverable
    }
    
    @Test
    public void testGenericErrorMessage() {
        String result = ErrorHandler.getGenericErrorMessage("file upload");
        
        assertTrue(result.contains("file upload"));
        assertTrue(result.contains("try again"));
    }
}