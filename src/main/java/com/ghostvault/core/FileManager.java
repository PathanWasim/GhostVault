package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import com.ghostvault.logging.PersistenceLogger;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.SecurityConfiguration;
import com.ghostvault.security.SecureMemoryManager;
import javax.crypto.SecretKey;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced FileManager with AES-256-GCM encryption
 * Provides secure file storage with password-derived encryption
 */
public class FileManager {
    private final CryptoManager cryptoManager;
    private String currentPassword; // For encryption/decryption
    private java.util.Map<String, byte[]> fileStorage = new java.util.HashMap<>();
    private String vaultPath;
    private PersistentStorageManager storageManager;
    private boolean encryptionEnabled = true;
    
    public FileManager() {
        this.vaultPath = System.getProperty("user.home") + "/.ghostvault";
        this.cryptoManager = new CryptoManager();
        this.storageManager = new PersistentStorageManager(vaultPath);
        initializeStorage();
    }
    
    public FileManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.storageManager = new PersistentStorageManager(vaultPath);
        initializeStorage();
    }
    
    /**
     * Initialize storage and ensure vault structure exists
     */
    private void initializeStorage() {
        System.out.println("🏗️ Initializing FileManager storage...");
        
        if (!storageManager.initializeVaultStructure()) {
            System.err.println("❌ Failed to initialize vault structure");
            return;
        }
        
        StorageVerification verification = storageManager.verifyStorageIntegrity();
        if (!verification.isStorageHealthy()) {
            System.err.println("⚠️ Storage verification failed, attempting recovery...");
            if (!storageManager.recoverVaultStructure()) {
                System.err.println("❌ Storage recovery failed");
            }
        }
        
        System.out.println("✅ FileManager storage initialized");
        
        // Debug: Check for existing files in storage
        try {
            java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
            if (java.nio.file.Files.exists(filesDir)) {
                long fileCount = java.nio.file.Files.list(filesDir).count();
                System.out.println("📁 Found " + fileCount + " existing files in storage");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error checking existing files: " + e.getMessage());
        }
    }
    
    /**
     * Set the password for encryption/decryption operations
     * @param password The user's password
     */
    public void setPassword(String password) {
        if (password == null) {
            System.err.println("⚠️ FileManager: Password is null - encryption/decryption will not work");
        } else if (password.isEmpty()) {
            System.err.println("⚠️ FileManager: Password is empty - encryption/decryption may fail");
        } else {
            System.out.println("🔐 FileManager: Password set successfully for encryption operations");
        }
        this.currentPassword = password;
    }
    
    /**
     * Legacy method for backward compatibility
     * @param key The encryption key (not used in new implementation)
     */
    @Deprecated
    public void setEncryptionKey(SecretKey key) {
        // Legacy method - encryption now uses password-derived keys
        System.out.println("⚠️ setEncryptionKey is deprecated - use setPassword instead");
    }
    
    /**
     * Enable or disable encryption for file operations
     * @param enabled true to enable encryption
     */
    public void setEncryptionEnabled(boolean enabled) {
        this.encryptionEnabled = enabled;
        System.out.println("🔐 File encryption " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if encryption is enabled
     * @return true if encryption is enabled
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    /**
     * Check if password is properly set for encryption operations
     * @return true if password is available and not empty
     */
    public boolean isPasswordAvailable() {
        return currentPassword != null && !currentPassword.isEmpty();
    }
    
    /**
     * Validate that FileManager is ready for encrypted operations
     * @return true if both encryption is enabled and password is available
     */
    public boolean isReadyForEncryptedOperations() {
        boolean ready = encryptionEnabled && isPasswordAvailable();
        if (!ready) {
            System.err.println("⚠️ FileManager not ready for encrypted operations: encryption=" + encryptionEnabled + ", password=" + isPasswordAvailable());
        }
        return ready;
    }
    
    public VaultFile storeFile(File file) throws Exception {
        // Read the actual file content
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
        
        // Determine MIME type
        String mimeType = determineMimeType(file.getName());
        
        VaultFile vaultFile = new VaultFile(file.getName(), file.length(), mimeType);
        
        // Store file content in memory (unencrypted for fast access)
        fileStorage.put(vaultFile.getFileId(), fileContent);
        
        // Save to disk with encryption if enabled
        try {
            if (encryptionEnabled && currentPassword != null) {
                saveEncryptedFileToDisk(vaultFile.getFileId(), fileContent);
                System.out.println("🔐 File encrypted and saved: " + file.getName());
            } else {
                saveFileToDisk(vaultFile.getFileId(), fileContent);
                if (!encryptionEnabled) {
                    System.out.println("⚠️ File saved without encryption: " + file.getName());
                }
            }
            PersistenceLogger.logFileSave(file.getName(), vaultFile.getFileId(), fileContent.length, true, null);
        } catch (Exception e) {
            PersistenceLogger.logFileSave(file.getName(), vaultFile.getFileId(), fileContent.length, false, e.getMessage());
            throw e;
        }
        
        System.out.println("📁 Stored file: " + file.getName() + " (" + fileContent.length + " bytes)");
        return vaultFile;
    }
    
    /**
     * Save encrypted file to disk using AES-256-GCM
     */
    private void saveEncryptedFileToDisk(String fileId, byte[] content) throws Exception {
        if (currentPassword == null) {
            throw new IllegalStateException("Password not set for encryption");
        }
        
        // Use files subdirectory for better organization
        java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
        java.nio.file.Files.createDirectories(filesDir);
        
        java.nio.file.Path filePath = filesDir.resolve(fileId + ".dat");
        
        // Encrypt the file content
        CryptoManager.EncryptedData cryptoData = cryptoManager.encryptWithPassword(content, currentPassword);
        
        // Create properly formatted encrypted file data
        EncryptedFileData encryptedFileData = new EncryptedFileData(
            cryptoData.getSalt(),
            cryptoData.getIv(),
            cryptoData.getCiphertext()
        );
        
        if (!encryptedFileData.isValid()) {
            throw new Exception("Invalid encrypted file data format");
        }
        
        // Save encrypted data with proper format
        byte[] serializedData = encryptedFileData.toByteArray();
        java.nio.file.Files.write(filePath, serializedData);
        
        // Verify file was saved successfully
        if (!java.nio.file.Files.exists(filePath)) {
            throw new Exception("Encrypted file save verification failed: " + fileId);
        }
        
        long savedSize = java.nio.file.Files.size(filePath);
        System.out.println("🔐 Encrypted file saved to disk: " + fileId + " (" + savedSize + " bytes encrypted)");
    }
    
    /**
     * Legacy method for saving unencrypted files
     */
    private void saveFileToDisk(String fileId, byte[] content) throws Exception {
        // Use files subdirectory for better organization
        java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
        java.nio.file.Files.createDirectories(filesDir);
        
        java.nio.file.Path filePath = filesDir.resolve(fileId + ".dat");
        java.nio.file.Files.write(filePath, content);
        
        // Verify file was saved successfully
        if (!java.nio.file.Files.exists(filePath)) {
            throw new Exception("File save verification failed: " + fileId);
        }
        
        long savedSize = java.nio.file.Files.size(filePath);
        if (savedSize != content.length) {
            throw new Exception("File size mismatch after save: expected " + content.length + ", got " + savedSize);
        }
        
        System.out.println("💾 File saved to disk (unencrypted): " + fileId + " (" + savedSize + " bytes)");
    }
    
    /**
     * Load and decrypt file from disk
     */
    private byte[] loadFileFromDisk(String fileId) throws Exception {
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, "files", fileId + ".dat");
        if (!java.nio.file.Files.exists(filePath)) {
            return null;
        }
        
        byte[] fileData = java.nio.file.Files.readAllBytes(filePath);
        
        // Try to detect if file is encrypted by checking if it has the expected format
        if (isEncryptedFile(fileData)) {
            return loadEncryptedFileFromDisk(fileId, fileData);
        } else {
            // Legacy unencrypted file
            System.out.println("📁 Loaded unencrypted file from disk: " + fileId + " (" + fileData.length + " bytes)");
            return fileData;
        }
    }
    
    /**
     * Load and decrypt an encrypted file
     */
    private byte[] loadEncryptedFileFromDisk(String fileId, byte[] encryptedData) throws Exception {
        if (currentPassword == null) {
            throw new IllegalStateException("Password not set for decryption");
        }
        
        try {
            // Deserialize encrypted file data with format validation
            EncryptedFileData encryptedFileData = EncryptedFileData.fromByteArray(encryptedData);
            
            if (!encryptedFileData.isValid()) {
                throw new Exception("Invalid encrypted file format");
            }
            
            // Convert to CryptoManager format for decryption
            CryptoManager.EncryptedData cryptoData = new CryptoManager.EncryptedData(
                encryptedFileData.getSalt(),
                encryptedFileData.getIv(),
                encryptedFileData.getCiphertext()
            );
            
            // Decrypt the content
            byte[] decryptedContent = cryptoManager.decryptWithPassword(cryptoData, currentPassword);
            
            System.out.println("🔓 Decrypted file from disk: " + fileId + " (" + decryptedContent.length + " bytes)");
            return decryptedContent;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to decrypt file " + fileId + ": " + e.getMessage());
            throw new Exception("Failed to decrypt file - incorrect password or corrupted data", e);
        }
    }
    
    /**
     * Check if file data appears to be encrypted using proper format validation
     */
    private boolean isEncryptedFile(byte[] data) {
        return EncryptedFileData.isEncryptedFileFormat(data);
    }
    
    public byte[] retrieveFile(VaultFile vaultFile) throws Exception {
        // First try memory cache
        byte[] content = fileStorage.get(vaultFile.getFileId());
        if (content != null) {
            System.out.println("📁 Retrieved file from memory: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), content.length, true, "memory");
            return content;
        }
        
        // Then try disk storage (with decryption if needed)
        content = loadFileFromDisk(vaultFile.getFileId());
        if (content != null) {
            // Cache in memory for faster access
            fileStorage.put(vaultFile.getFileId(), content);
            System.out.println("📁 Retrieved file from disk: " + vaultFile.getFileName() + " (" + content.length + " bytes)");
            PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), content.length, true, "disk");
            return content;
        }
        
        PersistenceLogger.logFileLoad(vaultFile.getFileName(), vaultFile.getFileId(), 0, false, "not_found");
        throw new Exception("File not found: " + vaultFile.getFileName());
    }
    
    private String determineMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "txt": return "text/plain";
            case "java": return "text/x-java-source";
            case "py": return "text/x-python";
            case "js": return "text/javascript";
            case "html": return "text/html";
            case "css": return "text/css";
            case "xml": return "text/xml";
            case "json": return "application/json";
            case "mp4": return "video/mp4";
            case "avi": return "video/avi";
            case "mov": return "video/quicktime";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "ogg": return "audio/ogg";
            default: return "application/octet-stream";
        }
    }
    
    public void deleteFile(VaultFile vaultFile) throws Exception {
        // Remove from memory cache
        fileStorage.remove(vaultFile.getFileId());
        
        // Remove from disk storage (files subdirectory)
        java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, "files", vaultFile.getFileId() + ".dat");
        if (java.nio.file.Files.exists(filePath)) {
            java.nio.file.Files.delete(filePath);
            System.out.println("🗑️ Deleted file: " + vaultFile.getFileName());
        }
    }
    
    /**
     * Get storage verification status
     */
    public StorageVerification getStorageVerification() {
        return storageManager.verifyStorageIntegrity();
    }
    
    /**
     * Check if storage is properly initialized
     */
    public boolean isStorageInitialized() {
        return storageManager.isVaultInitialized();
    }
    
    public List<VaultFile> getAllFiles() {
        return new ArrayList<>();
    }
    
    /**
     * Check if a specific file is encrypted on disk
     * @param fileId The file ID to check
     * @return true if file is encrypted
     */
    public boolean isFileEncrypted(String fileId) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(vaultPath, "files", fileId + ".dat");
            if (!java.nio.file.Files.exists(filePath)) {
                return false;
            }
            
            byte[] fileData = java.nio.file.Files.readAllBytes(filePath);
            return isEncryptedFile(fileData);
            
        } catch (Exception e) {
            System.err.println("❌ Error checking encryption status for " + fileId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get encryption status for all files
     * @return map of file ID to encryption status
     */
    public java.util.Map<String, Boolean> getEncryptionStatus() {
        java.util.Map<String, Boolean> status = new java.util.HashMap<>();
        
        try {
            java.nio.file.Path filesDir = java.nio.file.Paths.get(vaultPath, "files");
            if (java.nio.file.Files.exists(filesDir)) {
                java.nio.file.Files.list(filesDir)
                    .filter(path -> path.toString().endsWith(".dat"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String fileId = fileName.substring(0, fileName.length() - 4); // Remove .dat
                        status.put(fileId, isFileEncrypted(fileId));
                    });
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting encryption status: " + e.getMessage());
        }
        
        return status;
    }
    
    /**
     * Clear sensitive data from memory using SecureMemoryManager
     */
    public void clearSensitiveData() {
        if (currentPassword != null) {
            // Clear password from memory (best effort)
            currentPassword = null;
        }
        
        // Clear file cache
        fileStorage.clear();
        
        // Trigger secure memory cleanup
        SecureMemoryManager.getInstance().cleanupAllTrackedData();
        
        System.out.println("🧹 Cleared sensitive data from memory");
    }
}