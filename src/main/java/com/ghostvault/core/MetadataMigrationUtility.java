package com.ghostvault.core;

import com.ghostvault.security.SecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for migrating plain text metadata to encrypted storage
 */
public class MetadataMigrationUtility {
    
    private final EncryptedMetadataManager encryptedMetadataManager;
    private final Path vaultDirectory;
    private final Path plainMetadataFile;
    private final Path backupDirectory;
    private final ObjectMapper objectMapper;
    
    public MetadataMigrationUtility(String vaultPath) {
        this.vaultDirectory = Paths.get(vaultPath);
        this.encryptedMetadataManager = new EncryptedMetadataManager(vaultPath);
        this.plainMetadataFile = vaultDirectory.resolve(SecurityConfiguration.PLAIN_METADATA_FILE);
        this.backupDirectory = vaultDirectory.resolve("backups");
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Check if metadata migration is needed
     * @return true if plain text metadata exists and needs migration
     */
    public boolean isMigrationNeeded() {
        return encryptedMetadataManager.needsMigration();
    }
    
    /**
     * Perform migration from plain text to encrypted metadata storage
     * @param password The password for encryption
     * @return MigrationResult containing success status and details
     */
    public MigrationResult performMigration(String password) {
        try {
            System.out.println("🔄 Starting metadata migration from plain text to encrypted storage...");
            
            // Step 1: Validate preconditions
            if (!Files.exists(plainMetadataFile)) {
                return new MigrationResult(false, "No plain text metadata file found", null);
            }
            
            if (encryptedMetadataManager.hasEncryptedMetadata()) {
                return new MigrationResult(false, "Already using encrypted metadata storage", null);
            }
            
            if (password == null || password.isEmpty()) {
                return new MigrationResult(false, "Password is required for encryption", null);
            }
            
            // Step 2: Create backup
            String backupPath = createBackup();
            System.out.println("📦 Created backup: " + backupPath);
            
            // Step 3: Read and validate plain text metadata
            String plainMetadata = readPlainTextMetadata();
            if (plainMetadata == null) {
                return new MigrationResult(false, "Failed to read plain text metadata", backupPath);
            }
            
            // Step 4: Validate JSON format
            if (!validateMetadataFormat(plainMetadata)) {
                return new MigrationResult(false, "Invalid metadata format in plain text file", backupPath);
            }
            
            // Step 5: Migrate to encrypted storage
            encryptedMetadataManager.saveEncryptedMetadata(plainMetadata, password);
            
            // Step 6: Verify migration success
            if (!verifyMigration(plainMetadata, password)) {
                // Rollback on verification failure
                rollbackMigration(backupPath);
                return new MigrationResult(false, "Migration verification failed - rolled back", backupPath);
            }
            
            // Step 7: Clean up plain text file (already done by EncryptedMetadataManager)
            System.out.println("✅ Metadata migration completed successfully");
            
            return new MigrationResult(true, "Migration completed successfully", backupPath);
            
        } catch (Exception e) {
            System.err.println("❌ Metadata migration failed: " + e.getMessage());
            e.printStackTrace();
            return new MigrationResult(false, "Migration failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Rollback migration by restoring from backup
     * @param backupPath The backup file path
     * @return true if rollback successful
     */
    public boolean rollbackMigration(String backupPath) {
        try {
            if (backupPath == null || !Files.exists(Paths.get(backupPath))) {
                System.err.println("❌ Cannot rollback: backup file not found");
                return false;
            }
            
            System.out.println("🔄 Rolling back metadata migration...");
            
            // Restore plain text metadata file
            Files.copy(Paths.get(backupPath), plainMetadataFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Remove encrypted metadata file
            Path encryptedFile = vaultDirectory.resolve(SecurityConfiguration.ENCRYPTED_METADATA_FILE);
            if (Files.exists(encryptedFile)) {
                Files.delete(encryptedFile);
            }
            
            System.out.println("✅ Metadata migration rollback completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Rollback failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get migration status information
     * @return MetadataMigrationStatus with current state
     */
    public MetadataMigrationStatus getMigrationStatus() {
        boolean hasPlainText = Files.exists(plainMetadataFile);
        boolean hasEncrypted = encryptedMetadataManager.hasEncryptedMetadata();
        long plainTextSize = -1;
        long encryptedSize = -1;
        
        try {
            if (hasPlainText) {
                plainTextSize = Files.size(plainMetadataFile);
            }
            if (hasEncrypted) {
                encryptedSize = encryptedMetadataManager.getEncryptedMetadataSize();
            }
        } catch (IOException e) {
            System.err.println("❌ Error getting file sizes: " + e.getMessage());
        }
        
        return new MetadataMigrationStatus(hasPlainText, hasEncrypted, plainTextSize, encryptedSize);
    }
    
    /**
     * Create backup of the plain text metadata file
     * @return backup file path
     * @throws IOException if backup creation fails
     */
    private String createBackup() throws IOException {
        Files.createDirectories(backupDirectory);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupFileName = "metadata_backup_" + timestamp + ".json";
        Path backupFile = backupDirectory.resolve(backupFileName);
        
        Files.copy(plainMetadataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        
        return backupFile.toString();
    }
    
    /**
     * Read plain text metadata from file
     * @return metadata content or null if reading fails
     */
    private String readPlainTextMetadata() {
        try {
            return new String(Files.readAllBytes(plainMetadataFile), "UTF-8");
        } catch (Exception e) {
            System.err.println("❌ Failed to read plain text metadata: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate metadata format
     * @param metadata The metadata content to validate
     * @return true if valid JSON format
     */
    private boolean validateMetadataFormat(String metadata) {
        try {
            JsonNode rootNode = objectMapper.readTree(metadata);
            
            // Basic validation - should be an array or object
            if (!rootNode.isArray() && !rootNode.isObject()) {
                System.err.println("❌ Metadata is not valid JSON array or object");
                return false;
            }
            
            // If it's an array, validate file entries
            if (rootNode.isArray()) {
                for (JsonNode fileNode : rootNode) {
                    if (!validateFileEntry(fileNode)) {
                        return false;
                    }
                }
            }
            
            // If it's an object, check for files array
            if (rootNode.isObject() && rootNode.has("files")) {
                JsonNode filesNode = rootNode.get("files");
                if (filesNode.isArray()) {
                    for (JsonNode fileNode : filesNode) {
                        if (!validateFileEntry(fileNode)) {
                            return false;
                        }
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Invalid JSON format in metadata: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate individual file entry
     * @param fileNode The file node to validate
     * @return true if valid file entry
     */
    private boolean validateFileEntry(JsonNode fileNode) {
        if (!fileNode.isObject()) {
            return false;
        }
        
        // Check required fields
        if (!fileNode.has("fileName") || !fileNode.has("fileId")) {
            System.err.println("❌ File entry missing required fields (fileName, fileId)");
            return false;
        }
        
        // Validate field types
        if (!fileNode.get("fileName").isTextual() || !fileNode.get("fileId").isTextual()) {
            System.err.println("❌ File entry has invalid field types");
            return false;
        }
        
        return true;
    }
    
    /**
     * Verify that migration was successful
     * @param originalMetadata The original plain text metadata
     * @param password The password used for encryption
     * @return true if verification successful
     */
    private boolean verifyMigration(String originalMetadata, String password) {
        try {
            // Test that encrypted metadata can be decrypted
            String decryptedMetadata = encryptedMetadataManager.loadEncryptedMetadata(password);
            
            if (decryptedMetadata == null) {
                System.err.println("❌ Cannot decrypt migrated metadata");
                return false;
            }
            
            // Verify content integrity (basic check)
            JsonNode originalNode = objectMapper.readTree(originalMetadata);
            JsonNode decryptedNode = objectMapper.readTree(decryptedMetadata);
            
            // Compare structure and content
            if (!originalNode.equals(decryptedNode)) {
                System.err.println("❌ Decrypted metadata doesn't match original");
                return false;
            }
            
            // Test with wrong password should fail
            try {
                encryptedMetadataManager.loadEncryptedMetadata("WrongPassword123!");
                System.err.println("❌ Decryption with wrong password should have failed");
                return false;
            } catch (Exception e) {
                // Expected - wrong password should fail
            }
            
            System.out.println("✅ Migration verification successful");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Migration verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Result of metadata migration operation
     */
    public static class MigrationResult {
        private final boolean success;
        private final String message;
        private final String backupPath;
        
        public MigrationResult(boolean success, String message, String backupPath) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupPath() { return backupPath; }
        
        @Override
        public String toString() {
            return String.format("MetadataMigrationResult{success=%s, message='%s', backup='%s'}", 
                               success, message, backupPath);
        }
    }
    
    /**
     * Status of metadata migration
     */
    public static class MetadataMigrationStatus {
        private final boolean hasPlainText;
        private final boolean hasEncrypted;
        private final long plainTextSize;
        private final long encryptedSize;
        
        public MetadataMigrationStatus(boolean hasPlainText, boolean hasEncrypted, long plainTextSize, long encryptedSize) {
            this.hasPlainText = hasPlainText;
            this.hasEncrypted = hasEncrypted;
            this.plainTextSize = plainTextSize;
            this.encryptedSize = encryptedSize;
        }
        
        public boolean hasPlainText() { return hasPlainText; }
        public boolean hasEncrypted() { return hasEncrypted; }
        public long getPlainTextSize() { return plainTextSize; }
        public long getEncryptedSize() { return encryptedSize; }
        
        public boolean needsMigration() { return hasPlainText && !hasEncrypted; }
        public boolean isFullyMigrated() { return !hasPlainText && hasEncrypted; }
        public boolean hasConflict() { return hasPlainText && hasEncrypted; }
        
        @Override
        public String toString() {
            return String.format("MetadataMigrationStatus{plainText=%s(%d bytes), encrypted=%s(%d bytes), needsMigration=%s}",
                               hasPlainText, plainTextSize, hasEncrypted, encryptedSize, needsMigration());
        }
    }
}