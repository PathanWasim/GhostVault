package com.ghostvault.migration;

import com.ghostvault.security.PasswordMigrationUtility;
import com.ghostvault.core.FileMigrationUtility;
import com.ghostvault.core.MetadataMigrationUtility;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive migration utility that coordinates all security migrations
 * Handles passwords, files, and metadata migration in the correct order
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
     * @return MigrationAssessment with details of what needs migration
     */
    public MigrationAssessment assessMigrationNeeds() {
        boolean passwordsNeedMigration = passwordMigration.isMigrationNeeded();
        boolean filesNeedMigration = fileMigration.isMigrationNeeded();
        boolean metadataNeedsMigration = metadataMigration.isMigrationNeeded();
        
        FileMigrationUtility.FileEncryptionStatus fileStatus = fileMigration.getEncryptionStatus();
        MetadataMigrationUtility.MetadataMigrationStatus metadataStatus = metadataMigration.getMigrationStatus();
        
        return new MigrationAssessment(
            passwordsNeedMigration,
            filesNeedMigration,
            metadataNeedsMigration,
            fileStatus,
            metadataStatus
        );
    }
    
    /**
     * Perform comprehensive migration of all components
     * @param password The user's password for encryption
     * @return ComprehensiveMigrationResult with details of all migrations
     */
    public ComprehensiveMigrationResult performComprehensiveMigration(String password) {
        System.out.println("🔄 Starting comprehensive security migration...");
        
        List<String> migrationLog = new ArrayList<>();
        List<String> backupPaths = new ArrayList<>();
        boolean overallSuccess = true;
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try {
            // Step 1: Migrate passwords first (required for other migrations)
            migrationLog.add("Step 1: Password Migration");
            PasswordMigrationUtility.MigrationResult passwordResult = passwordMigration.performMigration();
            
            if (passwordResult.isSuccess()) {
                migrationLog.add("✅ Password migration successful");
                if (passwordResult.getBackupPath() != null) {
                    backupPaths.add(passwordResult.getBackupPath());
                }
            } else {
                migrationLog.add("❌ Password migration failed: " + passwordResult.getMessage());
                overallSuccess = false;
                
                // If password migration fails, we can't proceed with encrypted storage
                return new ComprehensiveMigrationResult(
                    false, 
                    "Password migration failed - cannot proceed with encryption", 
                    migrationLog, 
                    backupPaths,
                    startTime,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            }
            
            // Step 2: Migrate files
            migrationLog.add("Step 2: File Migration");
            FileMigrationUtility.MigrationResult fileResult = fileMigration.performMigration(password);
            
            if (fileResult.isSuccess()) {
                migrationLog.add("✅ File migration successful: " + fileResult.getSuccessCount() + "/" + fileResult.getTotalCount() + " files");
                if (fileResult.getBackupPath() != null) {
                    backupPaths.add(fileResult.getBackupPath());
                }
            } else {
                migrationLog.add("⚠️ File migration failed: " + fileResult.getMessage());
                // File migration failure is not critical - continue with metadata
            }
            
            // Step 3: Migrate metadata
            migrationLog.add("Step 3: Metadata Migration");
            MetadataMigrationUtility.MigrationResult metadataResult = metadataMigration.performMigration(password);
            
            if (metadataResult.isSuccess()) {
                migrationLog.add("✅ Metadata migration successful");
                if (metadataResult.getBackupPath() != null) {
                    backupPaths.add(metadataResult.getBackupPath());
                }
            } else {
                migrationLog.add("⚠️ Metadata migration failed: " + metadataResult.getMessage());
                // Metadata migration failure is not critical
            }
            
            // Step 4: Final verification
            migrationLog.add("Step 4: Migration Verification");
            MigrationAssessment postMigrationAssessment = assessMigrationNeeds();
            
            if (!postMigrationAssessment.needsAnyMigration()) {
                migrationLog.add("✅ All migrations completed successfully");
            } else {
                migrationLog.add("⚠️ Some migrations may not have completed fully");
            }
            
            String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            return new ComprehensiveMigrationResult(
                overallSuccess,
                overallSuccess ? "Comprehensive migration completed successfully" : "Migration completed with some issues",
                migrationLog,
                backupPaths,
                startTime,
                endTime
            );
            
        } catch (Exception e) {
            migrationLog.add("❌ Migration failed with exception: " + e.getMessage());
            System.err.println("❌ Comprehensive migration failed: " + e.getMessage());
            e.printStackTrace();
            
            return new ComprehensiveMigrationResult(
                false,
                "Migration failed: " + e.getMessage(),
                migrationLog,
                backupPaths,
                startTime,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }
    }
    
    /**
     * Rollback all migrations using backup paths
     * @param backupPaths List of backup paths from migration result
     * @return true if rollback successful
     */
    public boolean rollbackAllMigrations(List<String> backupPaths) {
        System.out.println("🔄 Rolling back all migrations...");
        
        boolean rollbackSuccess = true;
        
        // Rollback in reverse order
        for (String backupPath : backupPaths) {
            try {
                if (backupPath.contains("password")) {
                    if (!passwordMigration.rollbackMigration(backupPath)) {
                        rollbackSuccess = false;
                    }
                } else if (backupPath.contains("file")) {
                    if (!fileMigration.rollbackMigration(backupPath)) {
                        rollbackSuccess = false;
                    }
                } else if (backupPath.contains("metadata")) {
                    if (!metadataMigration.rollbackMigration(backupPath)) {
                        rollbackSuccess = false;
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Rollback failed for " + backupPath + ": " + e.getMessage());
                rollbackSuccess = false;
            }
        }
        
        if (rollbackSuccess) {
            System.out.println("✅ All migrations rolled back successfully");
        } else {
            System.err.println("⚠️ Some rollbacks may have failed");
        }
        
        return rollbackSuccess;
    }
    
    /**
     * Get detailed migration status
     * @return MigrationStatus with comprehensive information
     */
    public MigrationStatus getDetailedMigrationStatus() {
        MigrationAssessment assessment = assessMigrationNeeds();
        
        return new MigrationStatus(
            assessment,
            passwordMigration.isMigrationNeeded(),
            fileMigration.getEncryptionStatus(),
            metadataMigration.getMigrationStatus()
        );
    }
    
    /**
     * Assessment of what needs migration
     */
    public static class MigrationAssessment {
        private final boolean passwordsNeedMigration;
        private final boolean filesNeedMigration;
        private final boolean metadataNeedsMigration;
        private final FileMigrationUtility.FileEncryptionStatus fileStatus;
        private final MetadataMigrationUtility.MetadataMigrationStatus metadataStatus;
        
        public MigrationAssessment(boolean passwordsNeedMigration, boolean filesNeedMigration, 
                                 boolean metadataNeedsMigration, 
                                 FileMigrationUtility.FileEncryptionStatus fileStatus,
                                 MetadataMigrationUtility.MetadataMigrationStatus metadataStatus) {
            this.passwordsNeedMigration = passwordsNeedMigration;
            this.filesNeedMigration = filesNeedMigration;
            this.metadataNeedsMigration = metadataNeedsMigration;
            this.fileStatus = fileStatus;
            this.metadataStatus = metadataStatus;
        }
        
        public boolean needsPasswordMigration() { return passwordsNeedMigration; }
        public boolean needsFileMigration() { return filesNeedMigration; }
        public boolean needsMetadataMigration() { return metadataNeedsMigration; }
        public boolean needsAnyMigration() { return passwordsNeedMigration || filesNeedMigration || metadataNeedsMigration; }
        
        public FileMigrationUtility.FileEncryptionStatus getFileStatus() { return fileStatus; }
        public MetadataMigrationUtility.MetadataMigrationStatus getMetadataStatus() { return metadataStatus; }
        
        @Override
        public String toString() {
            return String.format("MigrationAssessment{passwords=%s, files=%s, metadata=%s}",
                               passwordsNeedMigration, filesNeedMigration, metadataNeedsMigration);
        }
    }
    
    /**
     * Result of comprehensive migration
     */
    public static class ComprehensiveMigrationResult {
        private final boolean success;
        private final String message;
        private final List<String> migrationLog;
        private final List<String> backupPaths;
        private final String startTime;
        private final String endTime;
        
        public ComprehensiveMigrationResult(boolean success, String message, List<String> migrationLog,
                                          List<String> backupPaths, String startTime, String endTime) {
            this.success = success;
            this.message = message;
            this.migrationLog = new ArrayList<>(migrationLog);
            this.backupPaths = new ArrayList<>(backupPaths);
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<String> getMigrationLog() { return new ArrayList<>(migrationLog); }
        public List<String> getBackupPaths() { return new ArrayList<>(backupPaths); }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        
        @Override
        public String toString() {
            return String.format("ComprehensiveMigrationResult{success=%s, message='%s', backups=%d, duration=%s to %s}",
                               success, message, backupPaths.size(), startTime, endTime);
        }
    }
    
    /**
     * Detailed migration status
     */
    public static class MigrationStatus {
        private final MigrationAssessment assessment;
        private final boolean passwordMigrationNeeded;
        private final FileMigrationUtility.FileEncryptionStatus fileEncryptionStatus;
        private final MetadataMigrationUtility.MetadataMigrationStatus metadataMigrationStatus;
        
        public MigrationStatus(MigrationAssessment assessment, boolean passwordMigrationNeeded,
                             FileMigrationUtility.FileEncryptionStatus fileEncryptionStatus,
                             MetadataMigrationUtility.MetadataMigrationStatus metadataMigrationStatus) {
            this.assessment = assessment;
            this.passwordMigrationNeeded = passwordMigrationNeeded;
            this.fileEncryptionStatus = fileEncryptionStatus;
            this.metadataMigrationStatus = metadataMigrationStatus;
        }
        
        public MigrationAssessment getAssessment() { return assessment; }
        public boolean isPasswordMigrationNeeded() { return passwordMigrationNeeded; }
        public FileMigrationUtility.FileEncryptionStatus getFileEncryptionStatus() { return fileEncryptionStatus; }
        public MetadataMigrationUtility.MetadataMigrationStatus getMetadataMigrationStatus() { return metadataMigrationStatus; }
        
        public boolean isFullySecure() {
            return !passwordMigrationNeeded && 
                   fileEncryptionStatus.isFullyEncrypted() && 
                   metadataMigrationStatus.isFullyMigrated();
        }
        
        @Override
        public String toString() {
            return String.format("MigrationStatus{fullySecure=%s, passwords=%s, files=%s, metadata=%s}",
                               isFullySecure(), !passwordMigrationNeeded, 
                               fileEncryptionStatus.isFullyEncrypted(), 
                               metadataMigrationStatus.isFullyMigrated());
        }
    }
}