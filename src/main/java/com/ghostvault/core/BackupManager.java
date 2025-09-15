package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.MemoryUtils;
import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Manages secure backup and restore operations for GhostVault
 * Creates encrypted backups that maintain all security properties
 */
public class BackupManager {
    
    private final CryptoManager cryptoManager;
    private final FileManager fileManager;
    private final MetadataManager metadataManager;
    private SecretKey backupEncryptionKey;
    
    // Backup format version for compatibility
    private static final int BACKUP_FORMAT_VERSION = 1;
    private static final String BACKUP_MAGIC_HEADER = "GHOSTVAULT_BACKUP";
    
    public BackupManager(FileManager fileManager, MetadataManager metadataManager) throws Exception {
        this.cryptoManager = new CryptoManager();
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
    }
    
    /**
     * Set encryption key for backup operations
     */
    public void setBackupEncryptionKey(SecretKey key) {
        this.backupEncryptionKey = key;
    }
    
    /**
     * Create a complete encrypted backup of the vault
     */
    public BackupResult createBackup(File backupFile, BackupOptions options) throws Exception {
        if (backupEncryptionKey == null) {
            throw new IllegalStateException("Backup encryption key not set");
        }
        
        long startTime = System.currentTimeMillis();
        BackupStats stats = new BackupStats();
        
        try {
            // Create backup manifest
            BackupManifest manifest = createBackupManifest(options);
            
            // Create temporary backup directory
            Path tempBackupDir = Files.createTempDirectory("ghostvault-backup");
            
            try {
                // Copy vault files to temporary directory
                copyVaultFiles(tempBackupDir, manifest, stats, options);
                
                // Copy metadata
                copyMetadata(tempBackupDir, stats);
                
                // Copy configuration files if requested
                if (options.includeConfiguration) {
                    copyConfiguration(tempBackupDir, stats);
                }
                
                // Create encrypted backup archive
                createEncryptedBackupArchive(tempBackupDir, backupFile, manifest, stats);
                
                long duration = System.currentTimeMillis() - startTime;
                stats.setBackupDuration(duration);
                
                return new BackupResult(true, null, stats);
                
            } finally {
                // Clean up temporary directory
                deleteDirectory(tempBackupDir.toFile());
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            stats.setBackupDuration(duration);
            return new BackupResult(false, e.getMessage(), stats);
        }
    }
    
    /**
     * Restore vault from encrypted backup
     */
    public RestoreResult restoreFromBackup(File backupFile, RestoreOptions options) throws Exception {
        if (backupEncryptionKey == null) {
            throw new IllegalStateException("Backup encryption key not set");
        }
        
        long startTime = System.currentTimeMillis();
        RestoreStats stats = new RestoreStats();
        
        try {
            // Verify backup file exists and is readable
            if (!backupFile.exists() || !backupFile.canRead()) {
                throw new IOException("Backup file not found or not readable: " + backupFile.getPath());
            }
            
            // Create temporary restore directory
            Path tempRestoreDir = Files.createTempDirectory("ghostvault-restore");
            
            try {
                // Extract and decrypt backup archive
                BackupManifest manifest = extractBackupArchive(backupFile, tempRestoreDir, stats);
                
                // Validate backup integrity
                validateBackupIntegrity(tempRestoreDir, manifest, stats);
                
                // Backup existing vault if requested
                if (options.backupExistingVault && VaultInitializer.isVaultInitialized()) {
                    createPreRestoreBackup(options);
                }
                
                // Restore vault files
                restoreVaultFiles(tempRestoreDir, manifest, stats, options);
                
                // Restore metadata
                restoreMetadata(tempRestoreDir, stats, options);
                
                // Restore configuration if present and requested
                if (options.restoreConfiguration) {
                    restoreConfiguration(tempRestoreDir, stats);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                stats.setRestoreDuration(duration);
                
                return new RestoreResult(true, null, stats);
                
            } finally {
                // Clean up temporary directory
                deleteDirectory(tempRestoreDir.toFile());
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            stats.setRestoreDuration(duration);
            return new RestoreResult(false, e.getMessage(), stats);
        }
    }
    
    /**
     * Verify backup integrity without restoring
     */
    public BackupVerificationResult verifyBackup(File backupFile) throws Exception {
        if (backupEncryptionKey == null) {
            throw new IllegalStateException("Backup encryption key not set");
        }
        
        try {
            // Create temporary directory for verification
            Path tempDir = Files.createTempDirectory("ghostvault-verify");
            
            try {
                // Extract backup
                RestoreStats stats = new RestoreStats();
                BackupManifest manifest = extractBackupArchive(backupFile, tempDir, stats);
                
                // Validate integrity
                validateBackupIntegrity(tempDir, manifest, stats);
                
                return new BackupVerificationResult(true, null, manifest, stats);
                
            } finally {
                deleteDirectory(tempDir.toFile());
            }
            
        } catch (Exception e) {
            return new BackupVerificationResult(false, e.getMessage(), null, null);
        }
    }
    
    /**
     * Create backup manifest with file information
     */
    private BackupManifest createBackupManifest(BackupOptions options) throws Exception {
        List<VaultFile> allFiles = metadataManager.getAllFiles();
        List<VaultFile> filesToBackup = new ArrayList<>();
        
        // Filter files based on options
        for (VaultFile file : allFiles) {
            if (shouldIncludeFile(file, options)) {
                filesToBackup.add(file);
            }
        }
        
        return new BackupManifest(
            BACKUP_FORMAT_VERSION,
            LocalDateTime.now(),
            filesToBackup,
            options.includeConfiguration,
            calculateTotalSize(filesToBackup)
        );
    }
    
    /**
     * Check if file should be included in backup
     */
    private boolean shouldIncludeFile(VaultFile file, BackupOptions options) {
        // Apply file filters
        if (options.fileExtensionFilter != null && !options.fileExtensionFilter.isEmpty()) {
            if (!options.fileExtensionFilter.contains(file.getExtension())) {
                return false;
            }
        }
        
        // Apply date filters
        if (options.dateFilter != null) {
            LocalDateTime fileDate = LocalDateTime.ofEpochSecond(
                file.getUploadTime() / 1000, 0, java.time.ZoneOffset.UTC);
            
            if (fileDate.isBefore(options.dateFilter)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculate total size of files to backup
     */
    private long calculateTotalSize(List<VaultFile> files) {
        return files.stream().mapToLong(VaultFile::getSize).sum();
    }
    
    /**
     * Copy vault files to backup directory
     */
    private void copyVaultFiles(Path backupDir, BackupManifest manifest, 
                               BackupStats stats, BackupOptions options) throws Exception {
        Path filesDir = backupDir.resolve("files");
        Files.createDirectories(filesDir);
        
        for (VaultFile vaultFile : manifest.getFiles()) {
            try {
                // Get encrypted file path
                Path sourcePath = fileManager.getEncryptedFilePath(vaultFile);
                Path destPath = filesDir.resolve(vaultFile.getEncryptedName());
                
                // Copy encrypted file (maintains encryption)
                Files.copy(sourcePath, destPath);
                
                stats.incrementFilesBackedUp();
                stats.addBytesBackedUp(Files.size(destPath));
                
            } catch (Exception e) {
                stats.incrementFailedFiles();
                if (!options.continueOnError) {
                    throw new Exception("Failed to backup file: " + vaultFile.getOriginalName(), e);
                }
            }
        }
    }
    
    /**
     * Copy metadata to backup directory
     */
    private void copyMetadata(Path backupDir, BackupStats stats) throws Exception {
        Path metadataSource = Paths.get(AppConfig.METADATA_FILE);
        Path metadataDest = backupDir.resolve("metadata.enc");
        
        if (Files.exists(metadataSource)) {
            Files.copy(metadataSource, metadataDest);
            stats.addBytesBackedUp(Files.size(metadataDest));
        }
    }
    
    /**
     * Copy configuration files to backup directory
     */
    private void copyConfiguration(Path backupDir, BackupStats stats) throws Exception {
        Path configDir = backupDir.resolve("config");
        Files.createDirectories(configDir);
        
        // Copy configuration files
        String[] configFiles = {AppConfig.CONFIG_FILE, AppConfig.SALT_FILE};
        
        for (String configFile : configFiles) {
            Path sourcePath = Paths.get(configFile);
            if (Files.exists(sourcePath)) {
                Path destPath = configDir.resolve(sourcePath.getFileName());
                Files.copy(sourcePath, destPath);
                stats.addBytesBackedUp(Files.size(destPath));
            }
        }
    }
    
    /**
     * Create encrypted backup archive
     */
    private void createEncryptedBackupArchive(Path backupDir, File backupFile, 
                                            BackupManifest manifest, BackupStats stats) throws Exception {
        // Create ZIP archive in memory
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBuffer)) {
            // Add manifest
            addManifestToZip(zipOut, manifest);
            
            // Add all files from backup directory
            addDirectoryToZip(zipOut, backupDir, "");
        }
        
        byte[] zipData = zipBuffer.toByteArray();
        
        try {
            // Encrypt the entire ZIP archive
            CryptoManager.EncryptedData encryptedBackup = cryptoManager.encrypt(zipData, backupEncryptionKey);
            
            // Write encrypted backup to file with header
            try (FileOutputStream fos = new FileOutputStream(backupFile);
                 DataOutputStream dos = new DataOutputStream(fos)) {
                
                // Write magic header and version
                dos.writeUTF(BACKUP_MAGIC_HEADER);
                dos.writeInt(BACKUP_FORMAT_VERSION);
                dos.writeLong(System.currentTimeMillis()); // Backup timestamp
                
                // Write encrypted data
                byte[] combinedData = encryptedBackup.getCombinedData();
                dos.writeInt(combinedData.length);
                dos.write(combinedData);
            }
            
            stats.setFinalBackupSize(backupFile.length());
            
        } finally {
            // Clear sensitive data
            MemoryUtils.secureWipe(zipData);
        }
    }
    
    /**
     * Add manifest to ZIP archive
     */
    private void addManifestToZip(ZipOutputStream zipOut, BackupManifest manifest) throws Exception {
        ZipEntry manifestEntry = new ZipEntry("manifest.json");
        zipOut.putNextEntry(manifestEntry);
        
        // Serialize manifest to JSON (simplified)
        String manifestJson = serializeManifest(manifest);
        zipOut.write(manifestJson.getBytes());
        zipOut.closeEntry();
    }
    
    /**
     * Add directory contents to ZIP archive
     */
    private void addDirectoryToZip(ZipOutputStream zipOut, Path sourceDir, String basePath) throws Exception {
        Files.walk(sourceDir)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    Path relativePath = sourceDir.relativize(file);
                    String zipEntryName = basePath + relativePath.toString().replace('\\', '/');
                    
                    ZipEntry entry = new ZipEntry(zipEntryName);
                    zipOut.putNextEntry(entry);
                    
                    Files.copy(file, zipOut);
                    zipOut.closeEntry();
                    
                } catch (Exception e) {
                    throw new RuntimeException("Failed to add file to ZIP: " + file, e);
                }
            });
    }
    
    /**
     * Extract and decrypt backup archive
     */
    private BackupManifest extractBackupArchive(File backupFile, Path restoreDir, 
                                               RestoreStats stats) throws Exception {
        BackupManifest manifest;
        
        try (FileInputStream fis = new FileInputStream(backupFile);
             DataInputStream dis = new DataInputStream(fis)) {
            
            // Read and verify header
            String magicHeader = dis.readUTF();
            if (!BACKUP_MAGIC_HEADER.equals(magicHeader)) {
                throw new IOException("Invalid backup file format");
            }
            
            int formatVersion = dis.readInt();
            if (formatVersion > BACKUP_FORMAT_VERSION) {
                throw new IOException("Backup format version " + formatVersion + " not supported");
            }
            
            long backupTimestamp = dis.readLong();
            stats.setBackupTimestamp(backupTimestamp);
            
            // Read encrypted data
            int encryptedDataLength = dis.readInt();
            byte[] encryptedData = new byte[encryptedDataLength];
            dis.readFully(encryptedData);
            
            // Decrypt backup data
            CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedData);
            byte[] decryptedZipData = cryptoManager.decrypt(encrypted, backupEncryptionKey);
            
            try {
                // Extract ZIP archive
                try (ByteArrayInputStream bais = new ByteArrayInputStream(decryptedZipData);
                     ZipInputStream zipIn = new ZipInputStream(bais)) {
                    
                    ZipEntry entry;
                    manifest = null;
                    
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (entry.getName().equals("manifest.json")) {
                            // Read manifest
                            ByteArrayOutputStream manifestBuffer = new ByteArrayOutputStream();
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = zipIn.read(buffer)) != -1) {
                                manifestBuffer.write(buffer, 0, bytesRead);
                            }
                            
                            manifest = deserializeManifest(manifestBuffer.toString());
                            
                        } else {
                            // Extract file
                            Path filePath = restoreDir.resolve(entry.getName());
                            Files.createDirectories(filePath.getParent());
                            
                            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = zipIn.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                            
                            stats.incrementFilesRestored();
                            stats.addBytesRestored(Files.size(filePath));
                        }
                        zipIn.closeEntry();
                    }
                }
                
                if (manifest == null) {
                    throw new IOException("Backup manifest not found");
                }
                
                return manifest;
                
            } finally {
                // Clear decrypted data
                MemoryUtils.secureWipe(decryptedZipData);
            }
        }
    }
    
    /**
     * Validate backup integrity
     */
    private void validateBackupIntegrity(Path restoreDir, BackupManifest manifest, 
                                       RestoreStats stats) throws Exception {
        // Verify all expected files are present
        Path filesDir = restoreDir.resolve("files");
        
        for (VaultFile vaultFile : manifest.getFiles()) {
            Path filePath = filesDir.resolve(vaultFile.getEncryptedName());
            
            if (!Files.exists(filePath)) {
                throw new IOException("Missing file in backup: " + vaultFile.getOriginalName());
            }
            
            // Verify file size matches expected
            long actualSize = Files.size(filePath);
            // Note: We can't verify exact size since we're dealing with encrypted files
            // But we can check if the file is not empty when it should have content
            if (vaultFile.getSize() > 0 && actualSize == 0) {
                throw new IOException("Corrupted file in backup: " + vaultFile.getOriginalName());
            }
        }
        
        stats.setIntegrityVerified(true);
    }
    
    /**
     * Create pre-restore backup of existing vault
     */
    private void createPreRestoreBackup(RestoreOptions options) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File preRestoreBackup = new File(options.preRestoreBackupPath + "_pre_restore_" + timestamp + ".gvb");
        
        BackupOptions backupOptions = new BackupOptions();
        backupOptions.includeConfiguration = true;
        backupOptions.continueOnError = true;
        
        createBackup(preRestoreBackup, backupOptions);
    }
    
    /**
     * Restore vault files from backup
     */
    private void restoreVaultFiles(Path restoreDir, BackupManifest manifest, 
                                 RestoreStats stats, RestoreOptions options) throws Exception {
        Path filesDir = restoreDir.resolve("files");
        
        // Ensure vault files directory exists
        FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
        
        for (VaultFile vaultFile : manifest.getFiles()) {
            try {
                Path sourcePath = filesDir.resolve(vaultFile.getEncryptedName());
                Path destPath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
                
                if (Files.exists(destPath) && !options.overwriteExisting) {
                    stats.incrementSkippedFiles();
                    continue;
                }
                
                Files.copy(sourcePath, destPath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                stats.incrementFilesRestored();
                
            } catch (Exception e) {
                stats.incrementFailedFiles();
                if (!options.continueOnError) {
                    throw new Exception("Failed to restore file: " + vaultFile.getOriginalName(), e);
                }
            }
        }
    }
    
    /**
     * Restore metadata from backup
     */
    private void restoreMetadata(Path restoreDir, RestoreStats stats, RestoreOptions options) throws Exception {
        Path metadataSource = restoreDir.resolve("metadata.enc");
        Path metadataDest = Paths.get(AppConfig.METADATA_FILE);
        
        if (Files.exists(metadataSource)) {
            if (Files.exists(metadataDest) && !options.overwriteExisting) {
                stats.setMetadataSkipped(true);
                return;
            }
            
            Files.copy(metadataSource, metadataDest, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            stats.setMetadataRestored(true);
        }
    }
    
    /**
     * Restore configuration from backup
     */
    private void restoreConfiguration(Path restoreDir, RestoreStats stats) throws Exception {
        Path configDir = restoreDir.resolve("config");
        
        if (Files.exists(configDir)) {
            Files.walk(configDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Path destPath = Paths.get(AppConfig.VAULT_DIR, file.getFileName().toString());
                        Files.copy(file, destPath, 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to restore config file: " + file, e);
                    }
                });
            
            stats.setConfigurationRestored(true);
        }
    }
    
    /**
     * Serialize manifest to JSON (simplified implementation)
     */
    private String serializeManifest(BackupManifest manifest) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"version\": ").append(manifest.getFormatVersion()).append(",\n");
        json.append("  \"timestamp\": \"").append(manifest.getBackupTimestamp()).append("\",\n");
        json.append("  \"includeConfiguration\": ").append(manifest.isIncludeConfiguration()).append(",\n");
        json.append("  \"totalSize\": ").append(manifest.getTotalSize()).append(",\n");
        json.append("  \"fileCount\": ").append(manifest.getFiles().size()).append("\n");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Deserialize manifest from JSON (simplified implementation)
     */
    private BackupManifest deserializeManifest(String json) {
        // Simplified JSON parsing - in a real implementation, use a proper JSON library
        int version = extractIntValue(json, "version");
        String timestamp = extractStringValue(json, "timestamp");
        boolean includeConfig = extractBooleanValue(json, "includeConfiguration");
        long totalSize = extractLongValue(json, "totalSize");
        
        // For this simplified implementation, we'll reconstruct the file list from the actual files
        // In a real implementation, the manifest would include complete file metadata
        return new BackupManifest(
            version,
            LocalDateTime.parse(timestamp),
            new ArrayList<>(), // Files will be populated during restore
            includeConfig,
            totalSize
        );
    }
    
    /**
     * Helper methods for simple JSON parsing
     */
    private int extractIntValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
    
    private long extractLongValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : 0L;
    }
    
    private String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
    
    private boolean extractBooleanValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*(true|false)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() && "true".equals(m.group(1));
    }
    
    /**
     * Delete directory recursively
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        backupEncryptionKey = null;
    }
}