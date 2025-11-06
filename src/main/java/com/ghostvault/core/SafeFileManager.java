package com.ghostvault.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Safe File Manager for protecting files from accidental deletion
 * Provides quarantine system and multi-step confirmation for deletions
 */
public class SafeFileManager {
    
    private static final Logger logger = Logger.getLogger(SafeFileManager.class.getName());
    
    private final String vaultPath;
    private final String quarantinePath;
    
    public SafeFileManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.quarantinePath = vaultPath + "/quarantine";
        
        // Ensure quarantine directory exists
        try {
            Files.createDirectories(Paths.get(quarantinePath));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create quarantine directory", e);
        }
    }
    
    /**
     * Quarantine orphaned files instead of deleting them
     * 
     * @param files List of files to quarantine
     * @return QuarantineResult with operation status
     */
    public QuarantineResult quarantineOrphanedFiles(List<File> files) {
        QuarantineResult result = new QuarantineResult();
        
        logger.info("Quarantining " + files.size() + " orphaned files");
        
        for (File file : files) {
            try {
                if (quarantineFile(file)) {
                    result.addQuarantinedFile(file);
                    logger.info("Quarantined: " + file.getName());
                } else {
                    result.addFailedFile(file, "Failed to move to quarantine");
                    logger.warning("Failed to quarantine: " + file.getName());
                }
            } catch (Exception e) {
                result.addFailedFile(file, e.getMessage());
                logger.log(Level.WARNING, "Error quarantining " + file.getName(), e);
            }
        }
        
        result.setSuccessful(!result.getQuarantinedFiles().isEmpty());
        
        logger.info("Quarantine complete: " + result.getQuarantinedFiles().size() + " quarantined, " + 
                   result.getFailedFiles().size() + " failed");
        
        return result;
    }
    
    /**
     * Move a single file to quarantine
     */
    private boolean quarantineFile(File file) throws IOException {
        if (!file.exists()) {
            return false;
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String quarantinedName = timestamp + "_" + file.getName();
        Path quarantinedPath = Paths.get(quarantinePath, quarantinedName);
        
        Files.move(file.toPath(), quarantinedPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create metadata file for the quarantined file
        createQuarantineMetadata(quarantinedPath, file);
        
        return true;
    }
    
    /**
     * Create metadata file for quarantined file
     */
    private void createQuarantineMetadata(Path quarantinedFile, File originalFile) {
        try {
            String metadataFileName = quarantinedFile.getFileName().toString() + ".meta";
            Path metadataPath = Paths.get(quarantinePath, metadataFileName);
            
            Properties metadata = new Properties();
            metadata.setProperty("original.name", originalFile.getName());
            metadata.setProperty("original.path", originalFile.getAbsolutePath());
            metadata.setProperty("quarantine.date", String.valueOf(System.currentTimeMillis()));
            metadata.setProperty("quarantine.reason", "Orphaned file - no metadata entry");
            metadata.setProperty("file.size", String.valueOf(originalFile.length()));
            metadata.setProperty("file.lastModified", String.valueOf(originalFile.lastModified()));
            
            try (var output = Files.newOutputStream(metadataPath)) {
                metadata.store(output, "Quarantined file metadata");
            }
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create quarantine metadata", e);
        }
    }
    
    /**
     * Get recovery options for a file
     */
    public RecoveryOptions getRecoveryOptions(File file) {
        RecoveryOptions options = new RecoveryOptions();
        
        // Check if file is in quarantine
        if (isFileInQuarantine(file)) {
            options.addOption("restore", "Restore from quarantine", 
                "Move the file back to the main vault area");
            options.addOption("delete_permanent", "Delete permanently", 
                "Permanently delete the file (cannot be undone)");
        } else {
            options.addOption("quarantine", "Move to quarantine", 
                "Safely move file to quarantine for later recovery");
            options.addOption("backup_delete", "Backup and delete", 
                "Create backup before deletion");
        }
        
        options.addOption("metadata_recovery", "Attempt metadata recovery", 
            "Try to rebuild metadata for this file");
        options.addOption("manual_inspection", "Manual inspection", 
            "Examine file contents to determine recovery method");
        
        return options;
    }
    
    /**
     * Check if file is in quarantine
     */
    private boolean isFileInQuarantine(File file) {
        return file.getAbsolutePath().contains(quarantinePath);
    }
    
    /**
     * Confirm deletion with multi-step process
     */
    public boolean confirmDeletion(List<File> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        
        // This method should be called from UI code with proper dialogs
        // For now, we'll return false to prevent any accidental deletions
        logger.warning("Deletion confirmation requested for " + files.size() + " files - denied for safety");
        return false;
    }
    
    /**
     * Get list of quarantined files
     */
    public List<QuarantinedFile> getQuarantinedFiles() {
        List<QuarantinedFile> quarantinedFiles = new ArrayList<>();
        
        try {
            Path quarantineDir = Paths.get(quarantinePath);
            if (!Files.exists(quarantineDir)) {
                return quarantinedFiles;
            }
            
            Files.list(quarantineDir)
                .filter(path -> !path.getFileName().toString().endsWith(".meta"))
                .forEach(path -> {
                    try {
                        QuarantinedFile qFile = loadQuarantinedFile(path);
                        if (qFile != null) {
                            quarantinedFiles.add(qFile);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to load quarantined file info", e);
                    }
                });
                
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to list quarantined files", e);
        }
        
        return quarantinedFiles;
    }
    
    /**
     * Load quarantined file information
     */
    private QuarantinedFile loadQuarantinedFile(Path quarantinedPath) {
        try {
            String metadataFileName = quarantinedPath.getFileName().toString() + ".meta";
            Path metadataPath = Paths.get(quarantinePath, metadataFileName);
            
            if (!Files.exists(metadataPath)) {
                // Create basic info without metadata
                return new QuarantinedFile(
                    quarantinedPath.getFileName().toString(),
                    quarantinedPath.toString(),
                    "Unknown",
                    Files.size(quarantinedPath),
                    Files.getLastModifiedTime(quarantinedPath).toMillis(),
                    System.currentTimeMillis(),
                    "No metadata available"
                );
            }
            
            Properties metadata = new Properties();
            try (var input = Files.newInputStream(metadataPath)) {
                metadata.load(input);
            }
            
            return new QuarantinedFile(
                quarantinedPath.getFileName().toString(),
                quarantinedPath.toString(),
                metadata.getProperty("original.name", "Unknown"),
                Long.parseLong(metadata.getProperty("file.size", "0")),
                Long.parseLong(metadata.getProperty("file.lastModified", "0")),
                Long.parseLong(metadata.getProperty("quarantine.date", "0")),
                metadata.getProperty("quarantine.reason", "Unknown")
            );
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load quarantined file metadata", e);
            return null;
        }
    }
    
    /**
     * Restore file from quarantine
     */
    public boolean restoreFromQuarantine(String quarantinedFileName) {
        try {
            Path quarantinedPath = Paths.get(quarantinePath, quarantinedFileName);
            if (!Files.exists(quarantinedPath)) {
                logger.warning("Quarantined file not found: " + quarantinedFileName);
                return false;
            }
            
            // Load original name from metadata
            String metadataFileName = quarantinedFileName + ".meta";
            Path metadataPath = Paths.get(quarantinePath, metadataFileName);
            
            String originalName = quarantinedFileName;
            if (Files.exists(metadataPath)) {
                Properties metadata = new Properties();
                try (var input = Files.newInputStream(metadataPath)) {
                    metadata.load(input);
                    originalName = metadata.getProperty("original.name", quarantinedFileName);
                }
            }
            
            // Restore to main vault area
            Path restoredPath = Paths.get(vaultPath, originalName);
            
            // Ensure we don't overwrite existing files
            int counter = 1;
            while (Files.exists(restoredPath)) {
                String baseName = originalName;
                String extension = "";
                int lastDot = originalName.lastIndexOf('.');
                if (lastDot > 0) {
                    baseName = originalName.substring(0, lastDot);
                    extension = originalName.substring(lastDot);
                }
                restoredPath = Paths.get(vaultPath, baseName + "_restored_" + counter + extension);
                counter++;
            }
            
            Files.move(quarantinedPath, restoredPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Clean up metadata
            if (Files.exists(metadataPath)) {
                Files.delete(metadataPath);
            }
            
            logger.info("Restored from quarantine: " + originalName + " -> " + restoredPath.getFileName());
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to restore from quarantine", e);
            return false;
        }
    }
    
    /**
     * Permanently delete quarantined file
     */
    public boolean permanentlyDeleteQuarantined(String quarantinedFileName) {
        try {
            Path quarantinedPath = Paths.get(quarantinePath, quarantinedFileName);
            String metadataFileName = quarantinedFileName + ".meta";
            Path metadataPath = Paths.get(quarantinePath, metadataFileName);
            
            boolean deleted = false;
            
            if (Files.exists(quarantinedPath)) {
                Files.delete(quarantinedPath);
                deleted = true;
            }
            
            if (Files.exists(metadataPath)) {
                Files.delete(metadataPath);
            }
            
            if (deleted) {
                logger.info("Permanently deleted quarantined file: " + quarantinedFileName);
            }
            
            return deleted;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to permanently delete quarantined file", e);
            return false;
        }
    }
    
    /**
     * Clean up old quarantined files (older than specified days)
     */
    public void cleanupOldQuarantinedFiles(int daysOld) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
            
            List<QuarantinedFile> quarantinedFiles = getQuarantinedFiles();
            int deletedCount = 0;
            
            for (QuarantinedFile qFile : quarantinedFiles) {
                if (qFile.getQuarantineDate() < cutoffTime) {
                    if (permanentlyDeleteQuarantined(qFile.getQuarantinedName())) {
                        deletedCount++;
                    }
                }
            }
            
            if (deletedCount > 0) {
                logger.info("Cleaned up " + deletedCount + " old quarantined files");
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to cleanup old quarantined files", e);
        }
    }
    
    /**
     * Quarantine result container
     */
    public static class QuarantineResult {
        private boolean successful;
        private List<File> quarantinedFiles;
        private Map<File, String> failedFiles;
        
        public QuarantineResult() {
            this.quarantinedFiles = new ArrayList<>();
            this.failedFiles = new HashMap<>();
        }
        
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
        
        public List<File> getQuarantinedFiles() { return quarantinedFiles; }
        public void addQuarantinedFile(File file) { this.quarantinedFiles.add(file); }
        
        public Map<File, String> getFailedFiles() { return failedFiles; }
        public void addFailedFile(File file, String reason) { this.failedFiles.put(file, reason); }
        
        public int getTotalFiles() {
            return quarantinedFiles.size() + failedFiles.size();
        }
    }
    
    /**
     * Recovery options container
     */
    public static class RecoveryOptions {
        private Map<String, RecoveryOption> options;
        
        public RecoveryOptions() {
            this.options = new HashMap<>();
        }
        
        public void addOption(String id, String name, String description) {
            options.put(id, new RecoveryOption(id, name, description));
        }
        
        public Map<String, RecoveryOption> getOptions() { return options; }
        
        public static class RecoveryOption {
            private final String id;
            private final String name;
            private final String description;
            
            public RecoveryOption(String id, String name, String description) {
                this.id = id;
                this.name = name;
                this.description = description;
            }
            
            public String getId() { return id; }
            public String getName() { return name; }
            public String getDescription() { return description; }
        }
    }
    
    /**
     * Quarantined file information
     */
    public static class QuarantinedFile {
        private final String quarantinedName;
        private final String quarantinedPath;
        private final String originalName;
        private final long size;
        private final long lastModified;
        private final long quarantineDate;
        private final String quarantineReason;
        
        public QuarantinedFile(String quarantinedName, String quarantinedPath, String originalName,
                              long size, long lastModified, long quarantineDate, String quarantineReason) {
            this.quarantinedName = quarantinedName;
            this.quarantinedPath = quarantinedPath;
            this.originalName = originalName;
            this.size = size;
            this.lastModified = lastModified;
            this.quarantineDate = quarantineDate;
            this.quarantineReason = quarantineReason;
        }
        
        // Getters
        public String getQuarantinedName() { return quarantinedName; }
        public String getQuarantinedPath() { return quarantinedPath; }
        public String getOriginalName() { return originalName; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
        public long getQuarantineDate() { return quarantineDate; }
        public String getQuarantineReason() { return quarantineReason; }
    }
}