package com.ghostvault.migration;

import com.ghostvault.security.SecurePasswordStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Integration tests for ComprehensiveMigrationUtility
 */
public class ComprehensiveMigrationUtilityTest {
    
    @TempDir
    Path tempDir;
    
    private ComprehensiveMigrationUtility migrationUtility;
    private final String testPassword = "TestPassword123!";
    
    @BeforeEach
    void setUp() {
        migrationUtility = new ComprehensiveMigrationUtility(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should assess migration needs correctly")
    void shouldAssessMigrationNeedsCorrectly() throws Exception {
        // Initially no migration needed
        ComprehensiveMigrationUtility.MigrationAssessment assessment = migrationUtility.assessMigrationNeeds();
        
        assertFalse(assessment.needsAnyMigration());
        assertFalse(assessment.needsPasswordMigration());
        assertFalse(assessment.needsFileMigration());
        assertFalse(assessment.needsMetadataMigration());
        
        // Create plain text data
        createPlainTextData();
        
        assessment = migrationUtility.assessMigrationNeeds();
        assertTrue(assessment.needsAnyMigration());
        assertTrue(assessment.needsPasswordMigration());
        assertTrue(assessment.needsFileMigration());
        assertTrue(assessment.needsMetadataMigration());
    }
    
    @Test
    @DisplayName("Should perform comprehensive migration successfully")
    void shouldPerformComprehensiveMigrationSuccessfully() throws Exception {
        // Create plain text data
        createPlainTextData();
        
        // Perform migration
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertFalse(result.getBackupPaths().isEmpty());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        
        // Verify migration log
        assertFalse(result.getMigrationLog().isEmpty());
        assertTrue(result.getMigrationLog().stream().anyMatch(log -> log.contains("Password Migration")));
        assertTrue(result.getMigrationLog().stream().anyMatch(log -> log.contains("File Migration")));
        assertTrue(result.getMigrationLog().stream().anyMatch(log -> log.contains("Metadata Migration")));
        
        // Verify no migration needed after completion
        ComprehensiveMigrationUtility.MigrationAssessment postAssessment = migrationUtility.assessMigrationNeeds();
        assertFalse(postAssessment.needsAnyMigration());
    }
    
    @Test
    @DisplayName("Should handle migration failure gracefully")
    void shouldHandleMigrationFailureGracefully() throws Exception {
        // Create invalid plain text data
        createInvalidPlainTextData();
        
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("failed"));
        
        // Should have migration log even on failure
        assertFalse(result.getMigrationLog().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle missing password gracefully")
    void shouldHandleMissingPasswordGracefully() throws Exception {
        createPlainTextData();
        
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Password migration failed"));
    }
    
    @Test
    @DisplayName("Should rollback migrations on request")
    void shouldRollbackMigrationsOnRequest() throws Exception {
        createPlainTextData();
        
        // Perform migration
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertFalse(result.getBackupPaths().isEmpty());
        
        // Rollback
        boolean rollbackSuccess = migrationUtility.rollbackAllMigrations(result.getBackupPaths());
        assertTrue(rollbackSuccess);
        
        // Should need migration again after rollback
        ComprehensiveMigrationUtility.MigrationAssessment assessment = migrationUtility.assessMigrationNeeds();
        assertTrue(assessment.needsAnyMigration());
    }
    
    @Test
    @DisplayName("Should provide detailed migration status")
    void shouldProvideDetailedMigrationStatus() throws Exception {
        ComprehensiveMigrationUtility.MigrationStatus status = migrationUtility.getDetailedMigrationStatus();
        
        assertNotNull(status);
        assertNotNull(status.getAssessment());
        assertNotNull(status.getFileEncryptionStatus());
        assertNotNull(status.getMetadataMigrationStatus());
        
        // Initially should be fully secure (no data to secure)
        assertTrue(status.isFullySecure());
        
        // After adding plain text data
        createPlainTextData();
        
        status = migrationUtility.getDetailedMigrationStatus();
        assertFalse(status.isFullySecure());
    }
    
    @Test
    @DisplayName("Should handle partial migration scenarios")
    void shouldHandlePartialMigrationScenarios() throws Exception {
        // Create only password data (no files or metadata)
        Path plainPasswordFile = tempDir.resolve("passwords.dat");
        Files.write(plainPasswordFile, "Master123!\nDecoy456@\nPanic789#".getBytes());
        
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        assertTrue(result.isSuccess());
        
        // Should have migrated passwords but not files/metadata (none existed)
        assertTrue(result.getMigrationLog().stream().anyMatch(log -> log.contains("Password migration successful")));
    }
    
    @Test
    @DisplayName("Should handle empty vault migration")
    void shouldHandleEmptyVaultMigration() throws Exception {
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("No files need migration") || 
                  result.getMessage().contains("successful"));
    }
    
    @Test
    @DisplayName("Should provide meaningful migration assessment toString")
    void shouldProvideMeaningfulMigrationAssessmentToString() throws Exception {
        ComprehensiveMigrationUtility.MigrationAssessment assessment = migrationUtility.assessMigrationNeeds();
        
        String assessmentStr = assessment.toString();
        assertNotNull(assessmentStr);
        assertTrue(assessmentStr.contains("MigrationAssessment"));
        assertTrue(assessmentStr.contains("passwords"));
        assertTrue(assessmentStr.contains("files"));
        assertTrue(assessmentStr.contains("metadata"));
    }
    
    @Test
    @DisplayName("Should provide meaningful migration result toString")
    void shouldProvideMeaningfulMigrationResultToString() throws Exception {
        ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = 
            migrationUtility.performComprehensiveMigration(testPassword);
        
        String resultStr = result.toString();
        assertNotNull(resultStr);
        assertTrue(resultStr.contains("ComprehensiveMigrationResult"));
        assertTrue(resultStr.contains("success"));
        assertTrue(resultStr.contains("backups"));
    }
    
    @Test
    @DisplayName("Should provide meaningful migration status toString")
    void shouldProvideMeaningfulMigrationStatusToString() throws Exception {
        ComprehensiveMigrationUtility.MigrationStatus status = migrationUtility.getDetailedMigrationStatus();
        
        String statusStr = status.toString();
        assertNotNull(statusStr);
        assertTrue(statusStr.contains("MigrationStatus"));
        assertTrue(statusStr.contains("fullySecure"));
        assertTrue(statusStr.contains("passwords"));
        assertTrue(statusStr.contains("files"));
        assertTrue(statusStr.contains("metadata"));
    }
    
    private void createPlainTextData() throws Exception {
        // Create plain text password file
        Path plainPasswordFile = tempDir.resolve("passwords.dat");
        Files.write(plainPasswordFile, "Master123!\nDecoy456@\nPanic789#".getBytes());
        
        // Create unencrypted files
        Path filesDir = tempDir.resolve("files");
        Files.createDirectories(filesDir);
        Files.write(filesDir.resolve("test1.dat"), "unencrypted content 1".getBytes());
        Files.write(filesDir.resolve("test2.dat"), "unencrypted content 2".getBytes());
        
        // Create plain text metadata
        Path metadataFile = tempDir.resolve("metadata.json");
        Files.write(metadataFile, "[{\"fileName\":\"test1.txt\",\"fileId\":\"test123\"}]".getBytes());
    }
    
    private void createInvalidPlainTextData() throws Exception {
        // Create invalid password file (missing passwords)
        Path plainPasswordFile = tempDir.resolve("passwords.dat");
        Files.write(plainPasswordFile, "invalid\n".getBytes());
        
        // Create invalid metadata file
        Path metadataFile = tempDir.resolve("metadata.json");
        Files.write(metadataFile, "invalid json content".getBytes());
    }
}