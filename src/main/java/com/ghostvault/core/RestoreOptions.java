package com.ghostvault.core;

/**
 * Configuration options for restore operations
 */
public class RestoreOptions {
    
    public boolean overwriteExisting = false;
    public boolean continueOnError = true;
    public boolean restoreConfiguration = true;
    public boolean backupExistingVault = true;
    public String preRestoreBackupPath = System.getProperty("user.home") + "/ghostvault_backup";
    public boolean verifyIntegrity = true;
    
    public RestoreOptions() {
        // Default constructor with default values
    }
    
    public RestoreOptions(boolean overwriteExisting, boolean continueOnError) {
        this.overwriteExisting = overwriteExisting;
        this.continueOnError = continueOnError;
    }
    
    /**
     * Create options for safe restore (with backup)
     */
    public static RestoreOptions safeRestore() {
        RestoreOptions options = new RestoreOptions();
        options.overwriteExisting = false;
        options.continueOnError = true;
        options.restoreConfiguration = true;
        options.backupExistingVault = true;
        options.verifyIntegrity = true;
        return options;
    }
    
    /**
     * Create options for complete restore (overwrite everything)
     */
    public static RestoreOptions completeRestore() {
        RestoreOptions options = new RestoreOptions();
        options.overwriteExisting = true;
        options.continueOnError = false;
        options.restoreConfiguration = true;
        options.backupExistingVault = true;
        options.verifyIntegrity = true;
        return options;
    }
    
    /**
     * Create options for files-only restore
     */
    public static RestoreOptions filesOnly() {
        RestoreOptions options = new RestoreOptions();
        options.overwriteExisting = false;
        options.continueOnError = true;
        options.restoreConfiguration = false;
        options.backupExistingVault = false;
        options.verifyIntegrity = true;
        return options;
    }
    
    @Override
    public String toString() {
        return String.format("RestoreOptions{overwrite=%s, continueOnError=%s, config=%s, backup=%s}", 
            overwriteExisting, continueOnError, restoreConfiguration, backupExistingVault);
    }
}