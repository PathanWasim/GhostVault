package com.ghostvault.core;

import com.ghostvault.model.VaultFile;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Backup manifest containing metadata about backup contents
 */
public class BackupManifest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int formatVersion;
    private final LocalDateTime backupTimestamp;
    private final List<VaultFile> files;
    private final boolean includeConfiguration;
    private final long totalSize;
    
    public BackupManifest(int formatVersion, LocalDateTime backupTimestamp, 
                         List<VaultFile> files, boolean includeConfiguration, long totalSize) {
        this.formatVersion = formatVersion;
        this.backupTimestamp = backupTimestamp;
        this.files = files;
        this.includeConfiguration = includeConfiguration;
        this.totalSize = totalSize;
    }
    
    public int getFormatVersion() { return formatVersion; }
    public LocalDateTime getBackupTimestamp() { return backupTimestamp; }
    public List<VaultFile> getFiles() { return files; }
    public boolean isIncludeConfiguration() { return includeConfiguration; }
    public long getTotalSize() { return totalSize; }
    
    public int getFileCount() { return files.size(); }
    
    @Override
    public String toString() {
        return String.format("BackupManifest{version=%d, timestamp=%s, files=%d, size=%d, config=%s}", 
            formatVersion, backupTimestamp, files.size(), totalSize, includeConfiguration);
    }
}