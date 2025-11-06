package com.ghostvault.security;

import com.ghostvault.core.FileMigrationUtility;
import com.ghostvault.core.MetadataMigrationUtility;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive migration utility that coordinates all security migrations
 * Handles passwords, files, and metadata migration in a coordinated manner
 */
public class ComprehensiveMigrationUtility {
    
    private final PasswordMigrationUtility passwordMigration;
    private final FileMigrationUtility fileMigration;
    private final MetadataMigrationUtility metadataMigration;
    private final String vaultPath;
    
    public ComprehensiveMigrationUtility(String vaultPath) {
        this.vaultPath = vaultPath;
        this.passwordMigration = new PasswordMigrationUtility(vaultPath);
        this.fileMigration = new FileMigrationUtility(vaultPath);
        this.metadataMigration = new MetadataMigrationUtility(vaultPath);
    }
    
    /**
     * Check if any migration is needed
     * @return true if any component needs migration
     */
    public boolean isMigrationNeeded() {
        return passwordMigration.isMigrationNeeded() ||
               fileMigration.isMigrationNeeded() ||
               metadataMigration.isMigrationNeeded();
    }
    
    /**
     * Get comprehensive migration status
     * @return ComprehensiveMigrationStatus with all component states
     */
    public ComprehensiveMigrationStatus getMigrationStatus() {
        return new ComprehensiveMigrationStatus(
            passwordMigration.isMigrationNeeded(),
            fileMigration.isMigrationNeeded(),
            metadataMigration.isMigrationNeeded(),
            fileMigration.getEncryptionStatus()
        );
    }
    
    /**
     * Perform complete system migration with user confirmation
     * @param password The user's password for encryption
     * @param userConfirmed Whether user has confirmed the migration
     * @return ComprehensiveMigrationResult with detailed results
     */
    public ComprehensiveMigrationResult performCompleteMigration(String password, boolean userConfirmed) {
        if (!userConfirmed) {
            return new ComprehensiveMigrationResult(false, "User confirmation required for migration", null, null, null);
        }
        
        if (password == null || password.isEmpty()) {
            return new ComprehensiveMigrationResult(false, "Password is required for encryption", null, null, null);
        }
        
        System.out.println("🔄 Starting comprehensive security migration...");
        
        List<String> migrationSteps = new ArrayList<>();
        List<String> backupPaths = new ArrayList<>();
        boolean overallSuccess = true;
        String overallMessage = "Migration completed successfully";
        
        // Step 1: Password Migration
        PasswordMigrationUtility.MigrationResult passwordResult = null;
        if (passwordMigration.isMigrationNeeded()) {
            System.out.println("🔐 Step 1: Migrating passwords to secure storage...");
            passwordResult = passwordMigration.performMigration();
            migrationSteps.add("Password migration: " + (passwordResult.isSuccess() ? "SUCCESS" : "FAILED"));
            
            if (passwordResult.isSuccess()) {
                if (passwordResult.getBackupPath() != null) {
                    backupPaths.add(passwordResult.getBackupPath());
                }
            } else {
                overallSuccess = false;
                overallMessage = "Password migration failed: " + passwordResult.getMessage();
                return new ComprehensiveMigrationResult(false, overallMessage, passwordResult, null, null);
            }
        } else {
            migrationSteps.add("Password migration: NOT NEEDED");
        }
        
        // Step 2: File Migration
        FileMigrationUtility.MigrationResult fileResult = null;
        if (fileMigration.isMigrationNeeded()) {
            System.out.println("📁 Step 2: Migrating files to encrypted storage...");
            fileResult = fileMigration.performMigration(password);
            migrationSteps.add("File migration: " + (fileResult.isSuccess() ? "SUCCESS" : "FAILED"));
            
            if (fileResult.isSuccess()) {
                if (fileResult.getBackupPath() != null) {
                    backupPaths.add(fileResult.getBackupPath());
                }
            } else {
                overallSuccess = false;
                overallMessage = "File migration failed: " + fileResult.getMessage();
                // Don't return here - continue with metadata migration
            }
        } else {
            migrationSteps.add("File migration: NOT NEEDED");
        }
        
        // Step 3: Metadata Migration
        MetadataMigrationUtility.MigrationResult metadataResult = null;
        if (metadataMigration.isMigrationNeeded()) {
            System.out.println("📋 Step 3: Migrating metadata to encrypted storage...");
            metadataResult = metadataMigration.performMigration(password);
            migrationSteps.add("Metadata migration: " + (metadataResult.isSuccess() ? "SUCCESS" : "FAILED"));
            
            if (metadataResult.isSuccess()) {
                if (metadataResult.getBackupPath() != null) {
                    backupPaths.add(metadataResult.getBackupPath());
                }
            } else {
                overallSuccess = false;
                if (overallSuccess) { // Only update message if not already failed
                    overallMessage = "Metadata migration failed: " + metadataResult.getMessage();
                }
            }
        } else {
            migrationSteps.add("Metadata migration: NOT NEEDED");
        }
        
        // Summary
        if (overallSuccess) {
            System.out.println("✅ Comprehensive migration completed successfully");
            System.out.println("📋 Migration summary:");
            for (String step : migrationSteps) {
                System.out.println("  - " + step);
            }
        } else {
            System.err.println("❌ Comprehensive migration completed with errors");
            System.err.println("📋 Migration summary:");
            for (String step : migrationSteps) {
                System.err.println("  - " + step);
            }
        }
        
        return new ComprehensiveMigrationResult(overallSuccess, overallMessage, passwordResult, fileResult, metadataResult);
    }
    
    /**
     * Rollback all migrations using backup paths
     * @param migrationResult The result containing backup paths
     * @return true if rollback successful
     */
    public boolean rollbackAllMigrations(ComprehensiveMigrationResult migrationResult) {
        if (migrationResult == null) {
            return false;
        }
        
        System.out.println("🔄 Rolling back comprehensive migration...");
        
        boolean rollbackSuccess = true;
        
        // Rollback metadata migration
        if (migrationResult.getMetadataResult() != null && migrationResult.getMetadataResult().getBackupPath() != null) {
            if (!metadataMigration.rollbackMigration(migrationResult.getMetadataResult().getBackupPath())) {
                rollbackSuccess = false;
                System.err.println("❌ Metadata rollback failed");
            }
        }
        
        // Rollback file migration
        if (migrationResult.getFileResult() != null && migrationResult.getFileResult().getBackupPath() != null) {
            if (!fileMigration.rollbackMigration(migrationResult.getFileResult().getBackupPath())) {
                rollbackSuccess = false;
                System.err.println("❌ File rollback failed");
            }
        }
        
        // Rollback password migration
        if (migrationResult.getPasswordResult() != null && migrationResult.getPasswordResult().getBackupPath() != null) {
            if (!passwordMigration.rollbackMigration(migrationResult.getPasswordResult().getBackupPath())) {
                rollbackSuccess = false;
                System.err.println("❌ Password rollback failed");
            }
        }
        
        if (rollbackSuccess) {
            System.out.println("✅ Comprehensive rollback completed successfully");
        } else {
            System.err.println("❌ Comprehensive rollback completed with errors");
        }
        
        return rollbackSuccess;
    }
    
    /**
     * Generate migration prompt for user
     * @return user-friendly migration prompt
     */
    public String generateMigrationPrompt() {
        ComprehensiveMigrationStatus status = getMigrationStatus();
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("🔒 Security Enhancement Required\n\n");
        prompt.append("Your GhostVault contains unencrypted data that should be secured:\n\n");
        
        if (status.needsPasswordMigration()) {
            prompt.append("• Passwords: Currently stored in plain text\n");
        }
        
        if (status.needsFileMigration()) {
            FileMigrationUtility.FileEncryptionStatus fileStatus = status.getFileEncryptionStatus();
            prompt.append(String.format("• Files: %d of %d files are unencrypted\n", 
                         fileStatus.getUnencryptedCount(), fileStatus.getTotalCount()));
        }
        
        if (status.needsMetadataMigration()) {
            prompt.append("• Metadata: File information stored in plain text\n");
        }
        
        prompt.append("\nMigration will:\n");
        prompt.append("✓ Encrypt all data with AES-256-GCM\n");
        prompt.append("✓ Use your password for key derivation\n");
        prompt.append("✓ Create backups before migration\n");
        prompt.append("✓ Maintain full compatibility\n\n");
        prompt.append("This process is safe and reversible. Continue with migration?");
        
        return prompt.toString();
    }
    
    /**
     * Status of comprehensive migration
     */
    public static class ComprehensiveMigrationStatus {
        private final boolean needsPasswordMigration;
        private final boolean needsFileMigration;
        private final boolean needsMetadataMigration;
        private final FileMigrationUtility.FileEncryptionStatus fileEncryptionStatus;
        
        public ComprehensiveMigrationStatus(boolean needsPasswordMigration, boolean needsFileMigration, 
                                          boolean needsMetadataMigration, 
                                          FileMigrationUtility.FileEncryptionStatus fileEncryptionStatus) {
            this.needsPasswordMigration = needsPasswordMigration;
            this.needsFileMigration = needsFileMigration;
            this.needsMetadataMigration = needsMetadataMigration;
            this.fileEncryptionStatus = fileEncryptionStatus;
        }
        
        public boolean needsPasswordMigration() { return needsPasswordMigration; }
        public boolean needsFileMigration() { return needsFileMigration; }
        public boolean needsMetadataMigration() { return needsMetadataMigration; }
        public FileMigrationUtility.FileEncryptionStatus getFileEncryptionStatus() { return fileEncryptionStatus; }
        
        public boolean needsAnyMigration() {
            return needsPasswordMigration || needsFileMigration || needsMetadataMigration;
        }
        
        public boolean isFullySecure() {
            return !needsAnyMigration() && 
                   (fileEncryptionStatus == null || fileEncryptionStatus.isFullyEncrypted());
        }
        
        @Override
        public String toString() {
            return String.format("ComprehensiveMigrationStatus{passwords=%s, files=%s, metadata=%s, secure=%s}",
                               needsPasswordMigration, needsFileMigration, needsMetadataMigration, isFullySecure());
        }
    }
    
    /**
     * Result of comprehensive migration
     */
    public static class ComprehensiveMigrationResult {
        private final boolean success;
        private final String message;
        private final PasswordMigrationUtility.MigrationResult passwordResult;
        private final FileMigrationUtility.MigrationResult fileResult;
        private final MetadataMigrationUtility.MigrationResult metadataResult;
        
        public ComprehensiveMigrationResult(boolean success, String message,
                                          PasswordMigrationUtility.MigrationResult passwordResult,
                                          FileMigrationUtility.MigrationResult fileResult,
                                          MetadataMigrationUtility.MigrationResult metadataResult) {
            this.success = success;
            this.message = message;
            this.passwordResult = passwordResult;
            this.fileResult = fileResult;
            this.metadataResult = metadataResult;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public PasswordMigrationUtility.MigrationResult getPasswordResult() { return passwordResult; }
        public FileMigrationUtility.MigrationResult getFileResult() { return fileResult; }
        public MetadataMigrationUtility.MigrationResult getMetadataResult() { return metadataResult; }
        
        @Override
        public String toString() {
            return String.format("ComprehensiveMigrationResult{success=%s, message='%s'}", success, message);
        }
    }
}