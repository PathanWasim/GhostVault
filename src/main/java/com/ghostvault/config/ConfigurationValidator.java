package com.ghostvault.config;

import com.ghostvault.security.PasswordManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Validates GhostVault configuration files and ensures system integrity
 * 
 * VALIDATION FEATURES:
 * - Configuration file existence and accessibility checks
 * - File integrity verification using checksums
 * - Configuration completeness validation
 * - Corruption detection and recovery recommendations
 * - Backup configuration management
 * 
 * @version 1.0.0 - Configuration Enhancement
 */
public class ConfigurationValidator {
    
    /**
     * Configuration validation status
     */
    public enum ConfigurationStatus {
        VALID("Configuration is valid and complete"),
        MISSING("Configuration file not found"),
        CORRUPTED("Configuration file is corrupted or invalid"),
        INCOMPLETE("Configuration exists but is incomplete"),
        INACCESSIBLE("Configuration file cannot be accessed"),
        BACKUP_AVAILABLE("Primary config corrupted but backup available"),
        UNKNOWN("Configuration status cannot be determined");
        
        private final String description;
        
        ConfigurationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Configuration validation result
     */
    public static class ValidationResult {
        private final ConfigurationStatus status;
        private final String errorMessage;
        private final String recoveryAction;
        private final boolean canRecover;
        private final long lastModified;
        private final long fileSize;
        
        public ValidationResult(ConfigurationStatus status, String errorMessage, 
                              String recoveryAction, boolean canRecover, 
                              long lastModified, long fileSize) {
            this.status = status;
            this.errorMessage = errorMessage;
            this.recoveryAction = recoveryAction;
            this.canRecover = canRecover;
            this.lastModified = lastModified;
            this.fileSize = fileSize;
        }
        
        public ConfigurationStatus getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public String getRecoveryAction() { return recoveryAction; }
        public boolean canRecover() { return canRecover; }
        public long getLastModified() { return lastModified; }
        public long getFileSize() { return fileSize; }
        
        public boolean isValid() {
            return status == ConfigurationStatus.VALID;
        }
        
        public boolean requiresRecovery() {
            return status == ConfigurationStatus.CORRUPTED || 
                   status == ConfigurationStatus.INCOMPLETE ||
                   status == ConfigurationStatus.BACKUP_AVAILABLE;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{status=%s, error='%s', canRecover=%s}", 
                status, errorMessage, canRecover);
        }
    }
    
    // Configuration file paths
    private final String configFilePath;
    private final String backupConfigPath;
    private final String checksumFilePath;
    
    // Validation settings
    private static final long MIN_CONFIG_SIZE = 100; // Minimum expected config size
    private static final long MAX_CONFIG_SIZE = 10 * 1024 * 1024; // 10MB max
    
    public ConfigurationValidator() {
        this.configFilePath = AppConfig.CONFIG_FILE;
        this.backupConfigPath = AppConfig.CONFIG_FILE + ".backup";
        this.checksumFilePath = AppConfig.CONFIG_FILE + ".checksum";
    }
    
    public ConfigurationValidator(String configFilePath) {
        this.configFilePath = configFilePath;
        this.backupConfigPath = configFilePath + ".backup";
        this.checksumFilePath = configFilePath + ".checksum";
    }
    
    /**
     * Validate configuration file comprehensively
     * 
     * @return ValidationResult with detailed status and recovery information
     */
    public ValidationResult validateConfiguration() {
        try {
            // Check if primary config file exists
            Path configPath = Paths.get(configFilePath);
            
            if (!Files.exists(configPath)) {
                // Check for backup configuration
                Path backupPath = Paths.get(backupConfigPath);
                if (Files.exists(backupPath)) {
                    return new ValidationResult(ConfigurationStatus.BACKUP_AVAILABLE, 
                        "Primary configuration missing but backup available", 
                        "Restore from backup configuration", true, 
                        getLastModified(backupPath), getFileSize(backupPath));
                } else {
                    return new ValidationResult(ConfigurationStatus.MISSING, 
                        "Configuration file not found", 
                        "Run initial setup to create configuration", false, 
                        0, 0);
                }
            }
            
            // Check file accessibility
            if (!Files.isReadable(configPath)) {
                return new ValidationResult(ConfigurationStatus.INACCESSIBLE, 
                    "Configuration file cannot be read", 
                    "Check file permissions", false, 
                    getLastModified(configPath), getFileSize(configPath));
            }
            
            // Check file size
            long fileSize = Files.size(configPath);
            if (fileSize < MIN_CONFIG_SIZE) {
                return new ValidationResult(ConfigurationStatus.INCOMPLETE, 
                    "Configuration file is too small", 
                    "Restore from backup or recreate configuration", true, 
                    getLastModified(configPath), fileSize);
            }
            
            if (fileSize > MAX_CONFIG_SIZE) {
                return new ValidationResult(ConfigurationStatus.CORRUPTED, 
                    "Configuration file is unusually large", 
                    "Restore from backup or recreate configuration", true, 
                    getLastModified(configPath), fileSize);
            }
            
            // Validate file integrity using checksum
            if (!validateChecksum(configPath)) {
                return new ValidationResult(ConfigurationStatus.CORRUPTED, 
                    "Configuration file checksum validation failed", 
                    "Restore from backup or recreate configuration", true, 
                    getLastModified(configPath), fileSize);
            }
            
            // Validate configuration content structure
            ValidationResult contentValidation = validateConfigurationContent(configPath);
            if (!contentValidation.isValid()) {
                return contentValidation;
            }
            
            // All validations passed
            return new ValidationResult(ConfigurationStatus.VALID, 
                null, null, false, 
                getLastModified(configPath), fileSize);
            
        } catch (IOException e) {
            return new ValidationResult(ConfigurationStatus.INACCESSIBLE, 
                "Error accessing configuration file: " + e.getMessage(), 
                "Check file system and permissions", false, 0, 0);
        } catch (Exception e) {
            return new ValidationResult(ConfigurationStatus.UNKNOWN, 
                "Unexpected error during validation: " + e.getMessage(), 
                "Contact support or recreate configuration", false, 0, 0);
        }
    }
    
    /**
     * Validate configuration file content structure
     */
    private ValidationResult validateConfigurationContent(Path configPath) {
        try {
            // Read and deserialize configuration
            byte[] configData = Files.readAllBytes(configPath);
            
            // Try to deserialize as PasswordConfiguration
            try (ByteArrayInputStream bais = new ByteArrayInputStream(configData);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                
                PasswordManager.PasswordConfiguration config = 
                    (PasswordManager.PasswordConfiguration) ois.readObject();
                
                // Validate configuration completeness
                if (config.getKdfParams() == null || config.getKdfParams().length == 0) {
                    return new ValidationResult(ConfigurationStatus.INCOMPLETE, 
                        "KDF parameters missing from configuration", 
                        "Recreate configuration", true, 
                        getLastModified(configPath), getFileSize(configPath));
                }
                
                if (config.getMasterVerifier() == null || config.getMasterVerifier().length == 0) {
                    return new ValidationResult(ConfigurationStatus.INCOMPLETE, 
                        "Master password verifier missing", 
                        "Recreate configuration", true, 
                        getLastModified(configPath), getFileSize(configPath));
                }
                
                if (config.getWrappedVMK() == null || config.getWrappedVMK().length == 0) {
                    return new ValidationResult(ConfigurationStatus.INCOMPLETE, 
                        "Wrapped vault master key missing", 
                        "Recreate configuration", true, 
                        getLastModified(configPath), getFileSize(configPath));
                }
                
                // Configuration appears complete and valid
                return new ValidationResult(ConfigurationStatus.VALID, 
                    null, null, false, 
                    getLastModified(configPath), getFileSize(configPath));
                
            } catch (ClassNotFoundException | InvalidClassException e) {
                return new ValidationResult(ConfigurationStatus.CORRUPTED, 
                    "Configuration file format is invalid: " + e.getMessage(), 
                    "Restore from backup or recreate configuration", true, 
                    getLastModified(configPath), getFileSize(configPath));
            } catch (StreamCorruptedException e) {
                return new ValidationResult(ConfigurationStatus.CORRUPTED, 
                    "Configuration file is corrupted: " + e.getMessage(), 
                    "Restore from backup or recreate configuration", true, 
                    getLastModified(configPath), getFileSize(configPath));
            }
            
        } catch (IOException e) {
            return new ValidationResult(ConfigurationStatus.INACCESSIBLE, 
                "Cannot read configuration content: " + e.getMessage(), 
                "Check file permissions and disk space", false, 
                getLastModified(configPath), getFileSize(configPath));
        }
    }
    
    /**
     * Validate configuration file checksum
     */
    private boolean validateChecksum(Path configPath) {
        try {
            Path checksumPath = Paths.get(checksumFilePath);
            
            // If no checksum file exists, create one for future validation
            if (!Files.exists(checksumPath)) {
                createChecksum(configPath);
                return true; // Assume valid for first-time creation
            }
            
            // Read stored checksum
            String storedChecksum = new String(Files.readAllBytes(checksumPath)).trim();
            
            // Calculate current checksum
            String currentChecksum = calculateChecksum(configPath);
            
            return storedChecksum.equals(currentChecksum);
            
        } catch (Exception e) {
            System.err.println("Checksum validation failed: " + e.getMessage());
            return false; // Assume corrupted if checksum validation fails
        }
    }
    
    /**
     * Calculate SHA-256 checksum of configuration file
     */
    private String calculateChecksum(Path configPath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] configData = Files.readAllBytes(configPath);
        byte[] hash = digest.digest(configData);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Create checksum file for configuration
     */
    public void createChecksum(Path configPath) throws IOException, NoSuchAlgorithmException {
        String checksum = calculateChecksum(configPath);
        Files.write(Paths.get(checksumFilePath), checksum.getBytes());
    }
    
    /**
     * Create backup of configuration file
     */
    public boolean createBackup() {
        try {
            Path configPath = Paths.get(configFilePath);
            Path backupPath = Paths.get(backupConfigPath);
            
            if (Files.exists(configPath)) {
                Files.copy(configPath, backupPath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Also backup checksum if it exists
                Path checksumPath = Paths.get(checksumFilePath);
                if (Files.exists(checksumPath)) {
                    Path backupChecksumPath = Paths.get(checksumFilePath + ".backup");
                    Files.copy(checksumPath, backupChecksumPath, 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                
                System.out.println("✅ Configuration backup created");
                return true;
            }
            
            return false;
            
        } catch (IOException e) {
            System.err.println("Failed to create configuration backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Restore configuration from backup
     */
    public boolean restoreFromBackup() {
        try {
            Path backupPath = Paths.get(backupConfigPath);
            Path configPath = Paths.get(configFilePath);
            
            if (Files.exists(backupPath)) {
                Files.copy(backupPath, configPath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Also restore checksum if backup exists
                Path backupChecksumPath = Paths.get(checksumFilePath + ".backup");
                if (Files.exists(backupChecksumPath)) {
                    Path checksumPath = Paths.get(checksumFilePath);
                    Files.copy(backupChecksumPath, checksumPath, 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                
                System.out.println("✅ Configuration restored from backup");
                return true;
            }
            
            return false;
            
        } catch (IOException e) {
            System.err.println("Failed to restore configuration from backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if configuration is complete and valid
     */
    public boolean isConfigurationComplete() {
        ValidationResult result = validateConfiguration();
        return result.isValid();
    }
    
    /**
     * Get configuration status
     */
    public ConfigurationStatus getConfigurationStatus() {
        return validateConfiguration().getStatus();
    }
    
    /**
     * Get detailed configuration information
     */
    public String getConfigurationInfo() {
        ValidationResult result = validateConfiguration();
        
        StringBuilder info = new StringBuilder();
        info.append("Configuration Status: ").append(result.getStatus()).append("\n");
        info.append("Description: ").append(result.getStatus().getDescription()).append("\n");
        
        if (result.getErrorMessage() != null) {
            info.append("Error: ").append(result.getErrorMessage()).append("\n");
        }
        
        if (result.getRecoveryAction() != null) {
            info.append("Recovery Action: ").append(result.getRecoveryAction()).append("\n");
        }
        
        info.append("File Size: ").append(result.getFileSize()).append(" bytes\n");
        info.append("Last Modified: ").append(new java.util.Date(result.getLastModified())).append("\n");
        info.append("Can Recover: ").append(result.canRecover()).append("\n");
        
        return info.toString();
    }
    
    /**
     * Perform configuration recovery if possible
     */
    public boolean performRecovery() {
        ValidationResult result = validateConfiguration();
        
        if (!result.canRecover()) {
            return false;
        }
        
        switch (result.getStatus()) {
            case BACKUP_AVAILABLE:
            case CORRUPTED:
                return restoreFromBackup();
            case INCOMPLETE:
                // For incomplete configurations, backup might help
                return restoreFromBackup();
            default:
                return false;
        }
    }
    
    /**
     * Helper method to get file last modified time
     */
    private long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Helper method to get file size
     */
    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
}