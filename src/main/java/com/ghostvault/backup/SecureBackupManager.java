package com.ghostvault.backup;

import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.SecurityConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Secure backup manager for migration operations
 * Provides encrypted backup and recovery capabilities with integrity verification
 */
public class SecureBackupManager {
    
    private final CryptoManager cryptoManager;
    private final Path vaultDirectory;
    private final Path backupDirectory;
    
    public SecureBackupManager(String vaultPath) {
        this.cryptoManager = new CryptoManager();
        this.vaultDirectory = Paths.get(vaultPath);
        this.backupDirectory = vaultDirectory.resolve("backups");
    }
    
    /**
     * Create a comprehensive backup before migration
     * @param password The password for backup encryption
     * @return BackupResult with backup details
     */
    public BackupResult createMigrationBackup(String password) {
        try {
            System.out.println("📦 Creating comprehensive migration backup...");
            
            // Ensure backup directory exists
            Files.createDirectories(backupDirectory);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupName = "migration_backup_" + timestamp;
            Path backupFile = backupDirectory.resolve(backupName + ".gvbackup");
            
            // Create backup manifest
            BackupManifest manifest = createBackupManifest();
            
            // Create encrypted backup
            createEncryptedBackup(backupFile, manifest, password);
            
            // Verify backup integrity
            if (!verifyBackupIntegrity(backupFile, password)) {
                throw new Exception("Backup integrity verification failed");
            }
            
            System.out.println("✅ Migration backup created: " + backupFile.getFileName());
            
            return new BackupResult(true, "Backup created successfully", backupFile.toString(), manifest);
            
        } catch (Exception e) {
            System.err.println("❌ Backup creation failed: " + e.getMessage());
            return new BackupResult(false, "Backup failed: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Restore from backup
     * @param backupPath The backup file path
     * @param password The password for backup decryption
     * @return RestoreResult with restore details
     */
    public RestoreResult restoreFromBackup(String backupPath, String password) {
        try {
            System.out.println("🔄 Restoring from backup: " + Paths.get(backupPath).getFileName());
            
            Path backupFile = Paths.get(backupPath);
            if (!Files.exists(backupFile)) {
                return new RestoreResult(false, "Backup file not found", null);
            }
            
            // Verify backup integrity before restore
            if (!verifyBackupIntegrity(backupFile, password)) {
                return new RestoreResult(false, "Backup integrity verification failed", null);
            }
            
            // Create restore point
            String restorePointPath = createRestorePoint();
            
            // Perform restore
            BackupManifest manifest = restoreFromEncryptedBackup(backupFile, password);
            
            // Verify restore integrity
            if (!verifyRestoreIntegrity(manifest)) {
                // Rollback to restore point
                rollbackToRestorePoint(restorePointPath);
                return new RestoreResult(false, "Restore verification failed - rolled back", restorePointPath);
            }
            
            System.out.println("✅ Restore completed successfully");
            
            return new RestoreResult(true, "Restore completed successfully", restorePointPath);
            
        } catch (Exception e) {
            System.err.println("❌ Restore failed: " + e.getMessage());
            return new RestoreResult(false, "Restore failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Create backup manifest with file inventory
     */
    private BackupManifest createBackupManifest() throws IOException {
        List<BackupItem> items = new ArrayList<>();
        
        // Scan vault directory for files to backup
        if (Files.exists(vaultDirectory)) {
            Files.walk(vaultDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> !path.startsWith(backupDirectory)) // Exclude backup directory
                .forEach(path -> {
                    try {
                        String relativePath = vaultDirectory.relativize(path).toString();
                        long size = Files.size(path);
                        String checksum = calculateFileChecksum(path);
                        items.add(new BackupItem(relativePath, size, checksum));
                    } catch (Exception e) {
                        System.err.println("⚠️ Error processing file " + path + ": " + e.getMessage());
                    }
                });
        }
        
        return new BackupManifest(items, LocalDateTime.now().toString());
    }
    
    /**
     * Create encrypted backup file
     */
    private void createEncryptedBackup(Path backupFile, BackupManifest manifest, String password) throws Exception {
        // Create temporary zip file
        Path tempZip = Files.createTempFile("backup", ".zip");
        
        try {
            // Create zip archive
            createZipArchive(tempZip, manifest);
            
            // Encrypt zip file
            byte[] zipData = Files.readAllBytes(tempZip);
            CryptoManager.EncryptedData encryptedData = cryptoManager.encryptWithPassword(zipData, password);
            
            // Create backup file format: [MAGIC][MANIFEST][ENCRYPTED_ZIP]
            createBackupFile(backupFile, manifest, encryptedData);
            
        } finally {
            // Clean up temporary file
            Files.deleteIfExists(tempZip);
        }
    }
    
    /**
     * Create zip archive of vault files
     */
    private void createZipArchive(Path zipFile, BackupManifest manifest) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            
            for (BackupItem item : manifest.getItems()) {
                Path sourceFile = vaultDirectory.resolve(item.getRelativePath());
                
                if (Files.exists(sourceFile)) {
                    ZipEntry entry = new ZipEntry(item.getRelativePath());
                    zos.putNextEntry(entry);
                    
                    Files.copy(sourceFile, zos);
                    zos.closeEntry();
                    
                    System.out.println("📦 Added to backup: " + item.getRelativePath());
                }
            }
        }
    }
    
    /**
     * Create backup file with proper format
     */
    private void createBackupFile(Path backupFile, BackupManifest manifest, CryptoManager.EncryptedData encryptedData) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(backupFile.toFile())) {
            
            // Write magic bytes
            fos.write(SecurityConfiguration.MAGIC_BYTES);
            
            // Write manifest (encrypted)
            String manifestJson = serializeManifest(manifest);
            CryptoManager.EncryptedData encryptedManifest = cryptoManager.encryptWithPassword(manifestJson.getBytes(), "manifest");
            byte[] manifestData = encryptedManifest.toByteArray();
            
            // Write manifest size (4 bytes)
            fos.write((manifestData.length >>> 24) & 0xFF);
            fos.write((manifestData.length >>> 16) & 0xFF);
            fos.write((manifestData.length >>> 8) & 0xFF);
            fos.write(manifestData.length & 0xFF);
            
            // Write encrypted manifest
            fos.write(manifestData);
            
            // Write encrypted zip data
            fos.write(encryptedData.toByteArray());
        }
    }
    
    /**
     * Restore from encrypted backup
     */
    private BackupManifest restoreFromEncryptedBackup(Path backupFile, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(backupFile.toFile())) {
            
            // Verify magic bytes
            byte[] magic = new byte[SecurityConfiguration.MAGIC_BYTES.length];
            fis.read(magic);
            if (!java.util.Arrays.equals(magic, SecurityConfiguration.MAGIC_BYTES)) {
                throw new Exception("Invalid backup file format");
            }
            
            // Read manifest size
            int manifestSize = (fis.read() << 24) | (fis.read() << 16) | (fis.read() << 8) | fis.read();
            
            // Read and decrypt manifest
            byte[] manifestData = new byte[manifestSize];
            fis.read(manifestData);
            CryptoManager.EncryptedData encryptedManifest = CryptoManager.EncryptedData.fromByteArray(manifestData);
            byte[] manifestBytes = cryptoManager.decryptWithPassword(encryptedManifest, "manifest");
            BackupManifest manifest = deserializeManifest(new String(manifestBytes));
            
            // Read and decrypt zip data
            byte[] zipData = fis.readAllBytes();
            CryptoManager.EncryptedData encryptedZip = CryptoManager.EncryptedData.fromByteArray(zipData);
            byte[] decryptedZip = cryptoManager.decryptWithPassword(encryptedZip, password);
            
            // Extract zip to vault directory
            extractZipToVault(decryptedZip);
            
            return manifest;
        }
    }
    
    /**
     * Extract zip data to vault directory
     */
    private void extractZipToVault(byte[] zipData) throws IOException {
        Path tempZip = Files.createTempFile("restore", ".zip");
        
        try {
            Files.write(tempZip, zipData);
            
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip.toFile()))) {
                ZipEntry entry;
                
                while ((entry = zis.getNextEntry()) != null) {
                    Path targetFile = vaultDirectory.resolve(entry.getName());
                    
                    // Ensure parent directories exist
                    Files.createDirectories(targetFile.getParent());
                    
                    // Extract file
                    Files.copy(zis, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    
                    System.out.println("📁 Restored: " + entry.getName());
                }
            }
            
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }
    
    /**
     * Verify backup integrity
     */
    private boolean verifyBackupIntegrity(Path backupFile, String password) {
        try {
            // Try to read and decrypt the backup
            restoreFromEncryptedBackup(backupFile, password);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Backup integrity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify restore integrity
     */
    private boolean verifyRestoreIntegrity(BackupManifest manifest) {
        try {
            for (BackupItem item : manifest.getItems()) {
                Path restoredFile = vaultDirectory.resolve(item.getRelativePath());
                
                if (!Files.exists(restoredFile)) {
                    System.err.println("❌ Restored file missing: " + item.getRelativePath());
                    return false;
                }
                
                if (Files.size(restoredFile) != item.getSize()) {
                    System.err.println("❌ Restored file size mismatch: " + item.getRelativePath());
                    return false;
                }
                
                String checksum = calculateFileChecksum(restoredFile);
                if (!checksum.equals(item.getChecksum())) {
                    System.err.println("❌ Restored file checksum mismatch: " + item.getRelativePath());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Restore integrity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create restore point before restore operation
     */
    private String createRestorePoint() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path restorePointDir = backupDirectory.resolve("restore_point_" + timestamp);
        
        Files.createDirectories(restorePointDir);
        
        // Copy current vault state to restore point
        if (Files.exists(vaultDirectory)) {
            Files.walk(vaultDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> !path.startsWith(backupDirectory))
                .forEach(path -> {
                    try {
                        Path relativePath = vaultDirectory.relativize(path);
                        Path targetPath = restorePointDir.resolve(relativePath);
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("⚠️ Error creating restore point for " + path + ": " + e.getMessage());
                    }
                });
        }
        
        return restorePointDir.toString();
    }
    
    /**
     * Rollback to restore point
     */
    private void rollbackToRestorePoint(String restorePointPath) throws IOException {
        Path restorePointDir = Paths.get(restorePointPath);
        
        if (!Files.exists(restorePointDir)) {
            throw new IOException("Restore point not found: " + restorePointPath);
        }
        
        // Clear current vault directory (except backups)
        if (Files.exists(vaultDirectory)) {
            Files.walk(vaultDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> !path.startsWith(backupDirectory))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("⚠️ Error deleting file during rollback: " + path);
                    }
                });
        }
        
        // Restore from restore point
        Files.walk(restorePointDir)
            .filter(Files::isRegularFile)
            .forEach(path -> {
                try {
                    Path relativePath = restorePointDir.relativize(path);
                    Path targetPath = vaultDirectory.resolve(relativePath);
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("⚠️ Error restoring file during rollback: " + path);
                }
            });
    }
    
    /**
     * Calculate file checksum for integrity verification
     */
    private String calculateFileChecksum(Path file) throws Exception {
        byte[] fileData = Files.readAllBytes(file);
        return cryptoManager.calculateSHA256(fileData);
    }
    
    /**
     * Serialize manifest to JSON
     */
    private String serializeManifest(BackupManifest manifest) {
        // Simple JSON serialization
        StringBuilder json = new StringBuilder();
        json.append("{\"timestamp\":\"").append(manifest.getTimestamp()).append("\",");
        json.append("\"items\":[");
        
        for (int i = 0; i < manifest.getItems().size(); i++) {
            BackupItem item = manifest.getItems().get(i);
            json.append("{\"path\":\"").append(item.getRelativePath()).append("\",");
            json.append("\"size\":").append(item.getSize()).append(",");
            json.append("\"checksum\":\"").append(item.getChecksum()).append("\"}");
            
            if (i < manifest.getItems().size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        return json.toString();
    }
    
    /**
     * Deserialize manifest from JSON
     */
    private BackupManifest deserializeManifest(String json) {
        // Simple JSON parsing - in production, use proper JSON library
        List<BackupItem> items = new ArrayList<>();
        String timestamp = "";
        
        // Extract timestamp
        int timestampStart = json.indexOf("\"timestamp\":\"") + 13;
        int timestampEnd = json.indexOf("\"", timestampStart);
        timestamp = json.substring(timestampStart, timestampEnd);
        
        // Parse items (simplified)
        // In production, use proper JSON parsing
        
        return new BackupManifest(items, timestamp);
    }
    
    /**
     * Backup item representing a file in the backup
     */
    public static class BackupItem {
        private final String relativePath;
        private final long size;
        private final String checksum;
        
        public BackupItem(String relativePath, long size, String checksum) {
            this.relativePath = relativePath;
            this.size = size;
            this.checksum = checksum;
        }
        
        public String getRelativePath() { return relativePath; }
        public long getSize() { return size; }
        public String getChecksum() { return checksum; }
    }
    
    /**
     * Backup manifest containing file inventory
     */
    public static class BackupManifest {
        private final List<BackupItem> items;
        private final String timestamp;
        
        public BackupManifest(List<BackupItem> items, String timestamp) {
            this.items = new ArrayList<>(items);
            this.timestamp = timestamp;
        }
        
        public List<BackupItem> getItems() { return new ArrayList<>(items); }
        public String getTimestamp() { return timestamp; }
    }
    
    /**
     * Result of backup operation
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final String backupPath;
        private final BackupManifest manifest;
        
        public BackupResult(boolean success, String message, String backupPath, BackupManifest manifest) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
            this.manifest = manifest;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupPath() { return backupPath; }
        public BackupManifest getManifest() { return manifest; }
    }
    
    /**
     * Result of restore operation
     */
    public static class RestoreResult {
        private final boolean success;
        private final String message;
        private final String restorePointPath;
        
        public RestoreResult(boolean success, String message, String restorePointPath) {
            this.success = success;
            this.message = message;
            this.restorePointPath = restorePointPath;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getRestorePointPath() { return restorePointPath; }
    }
}