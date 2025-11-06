package com.ghostvault.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for FileUploadValidator utility class
 */
public class FileUploadValidatorTest {
    
    @TempDir
    Path tempDir;
    
    private File testFile;
    private File nonExistentFile;
    private File directoryFile;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create a test file
        testFile = tempDir.resolve("test.txt").toFile();
        Files.write(testFile.toPath(), "Test content".getBytes());
        
        // Create a non-existent file reference
        nonExistentFile = tempDir.resolve("nonexistent.txt").toFile();
        
        // Create a directory
        directoryFile = tempDir.resolve("testdir").toFile();
        directoryFile.mkdir();
    }
    
    @Test
    public void testValidateFileAccess_ValidFile() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateFileAccess(testFile);
        
        assertTrue(result.isValid());
        assertEquals("File validation passed", result.getMessage());
    }
    
    @Test
    public void testValidateFileAccess_NullFile() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateFileAccess(null);
        
        assertFalse(result.isValid());
        assertEquals("No file selected", result.getErrorMessage());
    }
    
    @Test
    public void testValidateFileAccess_NonExistentFile() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateFileAccess(nonExistentFile);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("does not exist"));
    }
    
    @Test
    public void testValidateFileAccess_Directory() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateFileAccess(directoryFile);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("not a file"));
    }
    
    @Test
    public void testValidateMultipleFiles_ValidFiles() throws IOException {
        File secondFile = tempDir.resolve("test2.txt").toFile();
        Files.write(secondFile.toPath(), "Test content 2".getBytes());
        
        File[] files = {testFile, secondFile};
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateMultipleFiles(files);
        
        assertTrue(result.isValid());
        assertTrue(result.getMessage().contains("validated successfully"));
    }
    
    @Test
    public void testValidateMultipleFiles_EmptyArray() {
        File[] files = {};
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateMultipleFiles(files);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("No files selected"));
    }
    
    @Test
    public void testValidateMultipleFiles_NullArray() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateMultipleFiles(null);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("No files selected"));
    }
    
    @Test
    public void testValidateMultipleFiles_MixedValidInvalid() {
        File[] files = {testFile, nonExistentFile};
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateMultipleFiles(files);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("validation errors"));
    }
    
    @Test
    public void testValidationResult_Success() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.ValidationResult.success("Test message");
        
        assertTrue(result.isValid());
        assertEquals("Test message", result.getMessage());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testValidationResult_Failure() {
        FileUploadValidator.ValidationResult result = FileUploadValidator.ValidationResult.failure("Error message");
        
        assertFalse(result.isValid());
        assertEquals("Error message", result.getMessage());
        assertEquals("Error message", result.getErrorMessage());
    }
    
    @Test
    public void testValidateDiskSpace_ValidFile() {
        // Test with a small file size that should always pass
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateDiskSpace(1024); // 1KB
        
        // This should typically pass unless disk is completely full
        assertTrue(result.isValid() || result.getErrorMessage().contains("disk space"));
    }
}