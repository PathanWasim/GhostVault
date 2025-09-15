package com.ghostvault.core;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Configuration options for backup operations
 */
public class BackupOptions {
    
    public boolean includeConfiguration = true;
    public boolean continueOnError = false;
    public Set<String> fileExtensionFilter = null; // null means include all
    public LocalDateTime dateFilter = null; // null means include all dates
    public boolean compressBackup = true;
    public int compressionLevel = 6; // 0-9, 6 is default
    
    public BackupOptions() {
        // Default constructor with default values
    }
    
    public BackupOptions(boolean includeConfiguration, boolean continueOnError) {
        this.includeConfiguration = includeConfiguration;
        this.continueOnError = continueOnError;
    }
    
    /**
     * Create options for full backup
     */
    public static BackupOptions fullBackup() {
        BackupOptions options = new BackupOptions();
        options.includeConfiguration = true;
        options.continueOnError = false;
        return options;
    }
    
    /**
     * Create options for files-only backup
     */
    public static BackupOptions filesOnly() {
        BackupOptions options = new BackupOptions();
        options.includeConfiguration = false;
        options.continueOnError = false;
        return options;
    }
    
    /**
     * Create options for incremental backup (files modified after date)
     */
    public static BackupOptions incrementalBackup(LocalDateTime since) {
        BackupOptions options = new BackupOptions();
        options.includeConfiguration = false;
        options.dateFilter = since;
        options.continueOnError = true;
        return options;
    }
    
    @Override
    public String toString() {
        return String.format("BackupOptions{config=%s, continueOnError=%s, extensions=%s, dateFilter=%s}", 
            includeConfiguration, continueOnError, fileExtensionFilter, dateFilter);
    }
}