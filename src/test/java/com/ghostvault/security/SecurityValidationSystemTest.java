package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Comprehensive tests for SecurityValidationSystem
 */
public class SecurityValidationSystemTest {
    
    @TempDir
    Path tempDir;
    
    private SecurityValidationSystem validationSystem;
    private SecurePasswordStorage passwordStorage;
    private final String testPassword = "TestPassword123!";
    
    @BeforeEach
    void setUp() throws Exception {
        validationSystem = new SecurityValidationSystem(tempDir.toString());
        passwordStorage = new SecurePasswordStorage(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should detect secure vault configuration")
    void shouldDetectSecureVaultConfiguration() throws Exception {
        // Set up secure vault
        passwordStorage.storePasswordHashes(testPassword, "DecoyPass456@", "PanicPass789#");
        
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        assertTrue(report.isSecure());
        assertFalse(report.hasCriticalIssues());
        assertEquals(SecurityValidationSystem.SecurityLevel.SECURE, report.getOverallLevel());
    }
    
    @Test
    @DisplayName("Should detect plain text password vulnerability")
    void shouldDetectPlainTextPasswordVulnerability() throws Exception {
        // Create plain text password file
        Path plainPasswordFile = tempDir.resolve("passwords.dat");
        Files.write(plainPasswordFile, "Master123!\nDecoy456@\nPanic789#".getBytes());
        
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        assertFalse(report.isSecure());
        assertTrue(report.hasCriticalIssues());
        assertEquals(SecurityValidationSystem.SecurityLevel.CRITICAL, report.getOverallLevel());
        
        // Check that password security finding is critical
        boolean foundPasswordIssue = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("Password Security") && 
                          f.getLevel() == SecurityValidationSystem.SecurityLevel.CRITICAL);
        assertTrue(foundPasswordIssue);
    }
    
    @Test
    @DisplayName("Should detect unencrypted files")
    void shouldDetectUnencryptedFiles() throws Exception {
        // Set up secure passwords
        passwordStorage.storePasswordHashes(testPassword, "DecoyPass456@", "PanicPass789#");
        
        // Create unencrypted files
        Path filesDir = tempDir.resolve("files");
        Files.createDirectories(filesDir);
        Files.write(filesDir.resolve("test1.dat"), "unencrypted content 1".getBytes());
        Files.write(filesDir.resolve("test2.dat"), "unencrypted content 2".getBytes());
        
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        assertFalse(report.isSecure());
        assertTrue(report.hasHighIssues());
        
        // Check that file encryption finding shows issues
        boolean foundFileIssue = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("File Encryption") && 
                          f.getLevel().ordinal() >= SecurityValidationSystem.SecurityLevel.HIGH.ordinal());
        assertTrue(foundFileIssue);
    }
    
    @Test
    @DisplayName("Should detect plain text metadata")
    void shouldDetectPlainTextMetadata() throws Exception {
        // Set up secure passwords
        passwordStorage.storePasswordHashes(testPassword, "DecoyPass456@", "PanicPass789#");
        
        // Create plain text metadata file
        Path metadataFile = tempDir.resolve("metadata.json");
        Files.write(metadataFile, "[{\"fileName\":\"test.txt\",\"fileId\":\"test123\"}]".getBytes());
        
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        
        // Check that metadata encryption finding shows issues
        boolean foundMetadataIssue = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("Metadata Encryption") && 
                          f.getLevel() == SecurityValidationSystem.SecurityLevel.HIGH);
        assertTrue(foundMetadataIssue);
    }
    
    @Test
    @DisplayName("Should validate encryption configuration")
    void shouldValidateEncryptionConfiguration() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        // Should always have encryption configuration finding
        boolean foundConfigFinding = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("Encryption Configuration"));
        assertTrue(foundConfigFinding);
        
        // Configuration should be secure with current settings
        SecurityValidationSystem.SecurityFinding configFinding = report.getFindings().stream()
            .filter(f -> f.getCategory().equals("Encryption Configuration"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(configFinding);
        assertEquals(SecurityValidationSystem.SecurityLevel.SECURE, configFinding.getLevel());
    }
    
    @Test
    @DisplayName("Should validate file system security")
    void shouldValidateFileSystemSecurity() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        // Should always have file system security finding
        boolean foundFileSystemFinding = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("File System Security"));
        assertTrue(foundFileSystemFinding);
    }
    
    @Test
    @DisplayName("Should provide security recommendations")
    void shouldProvideSecurityRecommendations() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report.getRecommendations());
        assertFalse(report.getRecommendations().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle empty vault")
    void shouldHandleEmptyVault() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        assertNotNull(report.getFindings());
        assertFalse(report.getFindings().isEmpty());
        
        // Should have findings for all categories
        assertEquals(5, report.getFindings().size()); // Password, File, Metadata, Config, FileSystem
    }
    
    @Test
    @DisplayName("Should detect mixed security states")
    void shouldDetectMixedSecurityStates() throws Exception {
        // Set up partially secure vault
        passwordStorage.storePasswordHashes(testPassword, "DecoyPass456@", "PanicPass789#");
        
        // Create both plain text and encrypted files
        Path plainPasswordFile = tempDir.resolve("passwords.dat");
        Files.write(plainPasswordFile, "old plain text passwords".getBytes());
        
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report);
        
        // Should detect the conflict
        boolean foundFileSystemIssue = report.getFindings().stream()
            .anyMatch(f -> f.getCategory().equals("File System Security") && 
                          f.getLevel() == SecurityValidationSystem.SecurityLevel.HIGH);
        assertTrue(foundFileSystemIssue);
    }
    
    @Test
    @DisplayName("Should provide detailed finding information")
    void shouldProvideDetailedFindingInformation() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        for (SecurityValidationSystem.SecurityFinding finding : report.getFindings()) {
            assertNotNull(finding.getCategory());
            assertNotNull(finding.getLevel());
            assertNotNull(finding.getSummary());
            assertNotNull(finding.getIssues());
            assertNotNull(finding.getDetails());
            
            // toString should work
            assertNotNull(finding.toString());
            assertTrue(finding.toString().contains(finding.getCategory()));
        }
    }
    
    @Test
    @DisplayName("Should include timestamp in report")
    void shouldIncludeTimestampInReport() throws Exception {
        SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
        
        assertNotNull(report.getTimestamp());
        assertFalse(report.getTimestamp().isEmpty());
        
        // toString should work
        assertNotNull(report.toString());
        assertTrue(report.toString().contains("SecurityValidationReport"));
    }
    
    @Test
    @DisplayName("Should handle concurrent validation requests")
    void shouldHandleConcurrentValidationRequests() throws InterruptedException {
        // Set up secure vault
        try {
            passwordStorage.storePasswordHashes(testPassword, "DecoyPass456@", "PanicPass789#");
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
        
        // Run concurrent validations
        Thread t1 = new Thread(() -> {
            try {
                SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
                assertNotNull(report);
            } catch (Exception e) {
                fail("Thread 1 failed: " + e.getMessage());
            }
        });
        
        Thread t2 = new Thread(() -> {
            try {
                SecurityValidationSystem.SecurityValidationReport report = validationSystem.performSecurityValidation();
                assertNotNull(report);
            } catch (Exception e) {
                fail("Thread 2 failed: " + e.getMessage());
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
    }
}