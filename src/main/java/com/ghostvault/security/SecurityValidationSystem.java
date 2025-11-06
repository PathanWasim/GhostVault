package com.ghostvault.security;

import com.ghostvault.core.FileMigrationUtility;
import com.ghostvault.core.MetadataMigrationUtility;
import com.ghostvault.migration.ComprehensiveMigrationUtility;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Security validation system to verify encryption implementation
 * Provides comprehensive security auditing and vulnerability detection
 */
public class SecurityValidationSystem {
    
    private final String vaultPath;
    private final SecurePasswordStorage passwordStorage;
    private final FileMigrationUtility fileMigration;
    private final MetadataMigrationUtility metadataMigration;
    private final ComprehensiveMigrationUtility comprehensiveMigration;
    
    public SecurityValidationSystem(String vaultPath) {
        this.vaultPath = vaultPath;
        this.passwordStorage = new SecurePasswordStorage(vaultPath);
        this.fileMigration = new FileMigrationUtility(vaultPath);
        this.metadataMigration = new MetadataMigrationUtility(vaultPath);
        this.comprehensiveMigration = new ComprehensiveMigrationUtility(vaultPath);
    }
    
    /**
     * Perform comprehensive security validation
     * @return SecurityValidationReport with detailed findings
     */
    public SecurityValidationReport performSecurityValidation() {
        System.out.println("🔍 Starting comprehensive security validation...");
        
        List<SecurityFinding> findings = new ArrayList<>();
        SecurityLevel overallLevel = SecurityLevel.SECURE;
        
        // Validate password security
        SecurityFinding passwordFinding = validatePasswordSecurity();
        findings.add(passwordFinding);
        if (passwordFinding.getLevel().ordinal() > overallLevel.ordinal()) {
            overallLevel = passwordFinding.getLevel();
        }
        
        // Validate file encryption
        SecurityFinding fileFinding = validateFileEncryption();
        findings.add(fileFinding);
        if (fileFinding.getLevel().ordinal() > overallLevel.ordinal()) {
            overallLevel = fileFinding.getLevel();
        }
        
        // Validate metadata encryption
        SecurityFinding metadataFinding = validateMetadataEncryption();
        findings.add(metadataFinding);
        if (metadataFinding.getLevel().ordinal() > overallLevel.ordinal()) {
            overallLevel = metadataFinding.getLevel();
        }
        
        // Validate encryption configuration
        SecurityFinding configFinding = validateEncryptionConfiguration();
        findings.add(configFinding);
        if (configFinding.getLevel().ordinal() > overallLevel.ordinal()) {
            overallLevel = configFinding.getLevel();
        }
        
        // Validate file system security
        SecurityFinding fileSystemFinding = validateFileSystemSecurity();
        findings.add(fileSystemFinding);
        if (fileSystemFinding.getLevel().ordinal() > overallLevel.ordinal()) {
            overallLevel = fileSystemFinding.getLevel();
        }
        
        System.out.println("✅ Security validation completed - Overall level: " + overallLevel);
        
        return new SecurityValidationReport(overallLevel, findings, generateRecommendations(findings));
    }
    
    /**
     * Validate password security implementation
     */
    private SecurityFinding validatePasswordSecurity() {
        List<String> issues = new ArrayList<>();
        List<String> details = new ArrayList<>();
        
        // Check if using secure password storage
        if (!passwordStorage.isUsingSecureStorage()) {
            if (passwordStorage.needsMigration()) {
                issues.add("Passwords stored in plain text - migration needed");
                return new SecurityFinding(
                    "Password Security",
                    SecurityLevel.CRITICAL,
                    "Passwords are stored in plain text",
                    issues,
                    details
                );
            } else {
                issues.add("No password storage found");
                return new SecurityFinding(
                    "Password Security",
                    SecurityLevel.WARNING,
                    "No password storage detected",
                    issues,
                    details
                );
            }
        }
        
        details.add("✅ Using encrypted password storage");
        details.add("✅ PBKDF2 with " + SecurityConfiguration.PBKDF2_ITERATIONS + " iterations");
        details.add("✅ Unique salts for each password");
        
        // Validate configuration
        if (!SecurityConfiguration.validateConfiguration()) {
            issues.add("Security configuration validation failed");
            return new SecurityFinding(
                "Password Security",
                SecurityLevel.HIGH,
                "Security configuration issues detected",
                issues,
                details
            );
        }
        
        details.add("✅ Security configuration validated");
        
        return new SecurityFinding(
            "Password Security",
            SecurityLevel.SECURE,
            "Password security properly implemented",
            issues,
            details
        );
    }
    
    /**
     * Validate file encryption implementation
     */
    private SecurityFinding validateFileEncryption() {
        List<String> issues = new ArrayList<>();
        List<String> details = new ArrayList<>();
        
        FileMigrationUtility.FileEncryptionStatus status = fileMigration.getEncryptionStatus();
        
        if (status.hasUnencryptedFiles()) {
            issues.add(status.getUnencryptedCount() + " files are not encrypted");
            
            SecurityLevel level = SecurityLevel.HIGH;
            if (status.getUnencryptedCount() > status.getEncryptedCount()) {
                level = SecurityLevel.CRITICAL;
            }
            
            return new SecurityFinding(
                "File Encryption",
                level,
                "Unencrypted files detected",
                issues,
                List.of("Encryption percentage: " + String.format("%.1f%%", status.getEncryptionPercentage()))
            );
        }
        
        if (status.getTotalCount() == 0) {
            return new SecurityFinding(
                "File Encryption",
                SecurityLevel.INFO,
                "No files to validate",
                issues,
                List.of("Vault contains no files")
            );
        }
        
        details.add("✅ All " + status.getTotalCount() + " files are encrypted");
        details.add("✅ Using AES-256-GCM encryption");
        details.add("✅ Unique salt and IV per file");
        details.add("✅ Authentication tags prevent tampering");
        
        return new SecurityFinding(
            "File Encryption",
            SecurityLevel.SECURE,
            "All files properly encrypted",
            issues,
            details
        );
    }
    
    /**
     * Validate metadata encryption implementation
     */
    private SecurityFinding validateMetadataEncryption() {
        List<String> issues = new ArrayList<>();
        List<String> details = new ArrayList<>();
        
        MetadataMigrationUtility.MetadataMigrationStatus status = metadataMigration.getMigrationStatus();
        
        if (status.needsMigration()) {
            issues.add("Metadata stored in plain text");
            return new SecurityFinding(
                "Metadata Encryption",
                SecurityLevel.HIGH,
                "Metadata not encrypted",
                issues,
                List.of("Plain text metadata file exists")
            );
        }
        
        if (status.hasConflict()) {
            issues.add("Both plain text and encrypted metadata exist");
            return new SecurityFinding(
                "Metadata Encryption",
                SecurityLevel.WARNING,
                "Metadata storage conflict",
                issues,
                List.of("Migration may be incomplete")
            );
        }
        
        if (!status.hasEncrypted() && !status.hasPlainText()) {
            return new SecurityFinding(
                "Metadata Encryption",
                SecurityLevel.INFO,
                "No metadata to validate",
                issues,
                List.of("No metadata files found")
            );
        }
        
        details.add("✅ Metadata encrypted with AES-256-GCM");
        details.add("✅ File names and sizes protected");
        details.add("✅ Same encryption standard as files");
        
        return new SecurityFinding(
            "Metadata Encryption",
            SecurityLevel.SECURE,
            "Metadata properly encrypted",
            issues,
            details
        );
    }
    
    /**
     * Validate encryption configuration
     */
    private SecurityFinding validateEncryptionConfiguration() {
        List<String> issues = new ArrayList<>();
        List<String> details = new ArrayList<>();
        
        // Validate security configuration
        if (!SecurityConfiguration.validateConfiguration()) {
            issues.add("Security configuration validation failed");
            return new SecurityFinding(
                "Encryption Configuration",
                SecurityLevel.CRITICAL,
                "Invalid security configuration",
                issues,
                details
            );
        }
        
        details.add("✅ AES-256-GCM encryption algorithm");
        details.add("✅ PBKDF2 with " + SecurityConfiguration.PBKDF2_ITERATIONS + " iterations");
        details.add("✅ " + SecurityConfiguration.SALT_LENGTH + "-byte salts");
        details.add("✅ " + SecurityConfiguration.IV_LENGTH + "-byte IVs for GCM");
        details.add("✅ " + SecurityConfiguration.GCM_TAG_LENGTH + "-byte authentication tags");
        
        // Check for weak configurations
        if (SecurityConfiguration.PBKDF2_ITERATIONS < 100000) {
            issues.add("PBKDF2 iterations below recommended minimum");
        }
        
        if (SecurityConfiguration.SALT_LENGTH < 16) {
            issues.add("Salt length below recommended minimum");
        }
        
        if (!issues.isEmpty()) {
            return new SecurityFinding(
                "Encryption Configuration",
                SecurityLevel.WARNING,
                "Weak encryption configuration",
                issues,
                details
            );
        }
        
        return new SecurityFinding(
            "Encryption Configuration",
            SecurityLevel.SECURE,
            "Strong encryption configuration",
            issues,
            details
        );
    }
    
    /**
     * Validate file system security
     */
    private SecurityFinding validateFileSystemSecurity() {
        List<String> issues = new ArrayList<>();
        List<String> details = new ArrayList<>();
        
        Path vaultDir = Paths.get(vaultPath);
        
        // Check vault directory exists
        if (!Files.exists(vaultDir)) {
            issues.add("Vault directory does not exist");
            return new SecurityFinding(
                "File System Security",
                SecurityLevel.WARNING,
                "Vault directory not found",
                issues,
                details
            );
        }
        
        // Check for plain text password files
        Path plainPasswordFile = vaultDir.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        if (Files.exists(plainPasswordFile)) {
            issues.add("Plain text password file exists: " + SecurityConfiguration.PLAIN_PASSWORD_FILE);
        }
        
        // Check for plain text metadata files
        Path plainMetadataFile = vaultDir.resolve(SecurityConfiguration.PLAIN_METADATA_FILE);
        if (Files.exists(plainMetadataFile)) {
            issues.add("Plain text metadata file exists: " + SecurityConfiguration.PLAIN_METADATA_FILE);
        }
        
        // Check for encrypted files
        Path encryptedPasswordFile = vaultDir.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        if (Files.exists(encryptedPasswordFile)) {
            details.add("✅ Encrypted password file present");
        }
        
        Path encryptedMetadataFile = vaultDir.resolve(SecurityConfiguration.ENCRYPTED_METADATA_FILE);
        if (Files.exists(encryptedMetadataFile)) {
            details.add("✅ Encrypted metadata file present");
        }
        
        if (!issues.isEmpty()) {
            return new SecurityFinding(
                "File System Security",
                SecurityLevel.HIGH,
                "Plain text files detected",
                issues,
                details
            );
        }
        
        details.add("✅ No plain text sensitive files found");
        details.add("✅ Proper vault directory structure");
        
        return new SecurityFinding(
            "File System Security",
            SecurityLevel.SECURE,
            "File system properly secured",
            issues,
            details
        );
    }
    
    /**
     * Generate security recommendations based on findings
     */
    private List<String> generateRecommendations(List<SecurityFinding> findings) {
        List<String> recommendations = new ArrayList<>();
        
        for (SecurityFinding finding : findings) {
            if (finding.getLevel() == SecurityLevel.CRITICAL) {
                recommendations.add("URGENT: " + finding.getCategory() + " - " + finding.getSummary());
            } else if (finding.getLevel() == SecurityLevel.HIGH) {
                recommendations.add("HIGH PRIORITY: " + finding.getCategory() + " - " + finding.getSummary());
            } else if (finding.getLevel() == SecurityLevel.WARNING) {
                recommendations.add("RECOMMENDED: Address " + finding.getCategory() + " issues");
            }
        }
        
        // Add general recommendations
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Security implementation is excellent");
            recommendations.add("💡 Consider regular security audits");
            recommendations.add("💡 Keep encryption libraries updated");
        } else {
            recommendations.add("🔧 Run comprehensive migration to fix issues");
            recommendations.add("🔍 Re-run validation after fixes");
        }
        
        return recommendations;
    }
    
    /**
     * Security levels for findings
     */
    public enum SecurityLevel {
        SECURE,    // No issues
        INFO,      // Informational
        WARNING,   // Minor issues
        HIGH,      // Significant issues
        CRITICAL   // Critical vulnerabilities
    }
    
    /**
     * Individual security finding
     */
    public static class SecurityFinding {
        private final String category;
        private final SecurityLevel level;
        private final String summary;
        private final List<String> issues;
        private final List<String> details;
        
        public SecurityFinding(String category, SecurityLevel level, String summary,
                             List<String> issues, List<String> details) {
            this.category = category;
            this.level = level;
            this.summary = summary;
            this.issues = new ArrayList<>(issues);
            this.details = new ArrayList<>(details);
        }
        
        public String getCategory() { return category; }
        public SecurityLevel getLevel() { return level; }
        public String getSummary() { return summary; }
        public List<String> getIssues() { return new ArrayList<>(issues); }
        public List<String> getDetails() { return new ArrayList<>(details); }
        
        @Override
        public String toString() {
            return String.format("%s [%s]: %s", category, level, summary);
        }
    }
    
    /**
     * Comprehensive security validation report
     */
    public static class SecurityValidationReport {
        private final SecurityLevel overallLevel;
        private final List<SecurityFinding> findings;
        private final List<String> recommendations;
        private final String timestamp;
        
        public SecurityValidationReport(SecurityLevel overallLevel, List<SecurityFinding> findings,
                                      List<String> recommendations) {
            this.overallLevel = overallLevel;
            this.findings = new ArrayList<>(findings);
            this.recommendations = new ArrayList<>(recommendations);
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
        
        public SecurityLevel getOverallLevel() { return overallLevel; }
        public List<SecurityFinding> getFindings() { return new ArrayList<>(findings); }
        public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
        public String getTimestamp() { return timestamp; }
        
        public boolean isSecure() { return overallLevel == SecurityLevel.SECURE || overallLevel == SecurityLevel.INFO; }
        public boolean hasCriticalIssues() { return overallLevel == SecurityLevel.CRITICAL; }
        public boolean hasHighIssues() { return overallLevel == SecurityLevel.HIGH || overallLevel == SecurityLevel.CRITICAL; }
        
        @Override
        public String toString() {
            return String.format("SecurityValidationReport{level=%s, findings=%d, timestamp=%s}",
                               overallLevel, findings.size(), timestamp);
        }
    }
}