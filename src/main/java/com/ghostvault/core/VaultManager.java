package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.security.SecureDeletion;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;
import com.ghostvault.audit.AuditLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/**
 * Core vault management functionality
 * Coordinates between security, file operations, and audit logging
 */
public class VaultManager {
    
    private final String vaultPath;
    private final CryptoManager cryptoManager;
    private final PasswordManager passwordManager;
    private final AuditLogger auditLogger;
    private final MetadataManager metadataManager;
    private final DecoyManager decoyManager;
    
    private boolean isInitialized = false;
    private boolean isFirstRun = false;
    
    public VaultManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.passwordManager = new PasswordManager(vaultPath);
        this.auditLogger = new AuditLogger(AppConfig.LOG_FILE);
        this.metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        this.decoyManager = new DecoyManager(AppConfig.DECOYS_DIR);
        
        initializeVault();
    }
    
    /**
     * Initialize vault directory structure
     */
    private void initializeVault() throws IOException {
        File vaultDir = new File(vaultPath);
        
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
            new File(AppConfig.FILES_DIR).mkdirs();
            new File(AppConfig.DECOYS_DIR).mkdirs();
            isFirstRun = true;
            
            // Generate decoy files
            decoyManager.generateDecoyFiles();
            auditLogger.log("Vault directory structure created");
        } else {
            isFirstRun = !new File(AppConfig.CONFIG_FILE).exists();
        }
    }
    
    /**
     * Check if this is the first run (setup required)
     */
    public boolean isFirstRun() {
        return isFirstRun;
    }
    
    /**
     * Initialize vault with master, panic, and decoy passwords
     */
    public void initializePasswords(String masterPassword, String panicPassword, String decoyPassword) throws Exception {
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
        cryptoManager.initializeWithPassword(masterPassword, passwordManager.getSalt());
        auditLogger.setEncryptionKey(cryptoManager.getMasterKey());
        metadataManager.setEncryptionKey(cryptoManager.getMasterKey());
        
        isInitialized = true;
        isFirstRun = false;
        
        auditLogger.log("Vault initialized with new passwords");
    }
    
    /**
     * Authenticate user and determine access mode
     */
    public AuthResult authenticate(String password) throws Exception {
        PasswordManager.PasswordType type = passwordManager.validatePassword(password);
        
        switch (type) {
            case MASTER:
                cryptoManager.initializeWithPassword(password, passwordManager.getSalt());
                auditLogger.setEncryptionKey(cryptoManager.getMasterKey());
                metadataManager.setEncryptionKey(cryptoManager.getMasterKey());
                metadataManager.loadMetadata();
                isInitialized = true;
                auditLogger.log("Master authentication successful");
                return new AuthResult(AuthResult.Type.MASTER, true);
                
            case PANIC:
                auditLogger.log("PANIC MODE ACTIVATED");
                executePanicWipe();
                return new AuthResult(AuthResult.Type.PANIC, true);
                
            case DECOY:
                // Initialize with decoy mode (limited functionality)
                isInitialized = true;
                auditLogger.log("Decoy mode accessed");
                return new AuthResult(AuthResult.Type.DECOY, true);
                
            default:
                auditLogger.log("Authentication failed");
                return new AuthResult(AuthResult.Type.INVALID, false);
        }
    }
    
    /**
     * Upload and encrypt a file to the vault
     */
    public String uploadFile(File sourceFile) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        // Validate file
        if (!FileUtils.isValidFile(sourceFile)) {
            throw new IllegalArgumentException("Invalid file");
        }
        
        // Read file data
        byte[] fileData = Files.readAllBytes(sourceFile.toPath());
        
        // Encrypt file
        CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(fileData);
        
        // Generate unique file ID
        String fileId = java.util.UUID.randomUUID().toString();
        String encryptedFileName = fileId + ".enc";
        
        // Save encrypted file
        Path encryptedPath = Paths.get(AppConfig.FILES_DIR, encryptedFileName);
        FileUtils.writeEncryptedFile(encryptedPath, encrypted);
        
        // Create metadata
        VaultFile vaultFile = new VaultFile(
            sourceFile.getName(),
            fileId,
            encryptedFileName,
            sourceFile.length(),
            FileUtils.calculateSHA256(fileData),
            System.currentTimeMillis()
        );
        
        // Store metadata
        metadataManager.addFile(vaultFile);
        
        auditLogger.log("File uploaded: " + sourceFile.getName() + " (" + FileUtils.formatFileSize(sourceFile.length()) + ")");
        
        return fileId;
    }
    
    /**
     * Download and decrypt a file from the vault
     */
    public byte[] downloadFile(String fileId) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        VaultFile vaultFile = metadataManager.getFile(fileId);
        if (vaultFile == null) {
            throw new IllegalArgumentException("File not found");
        }
        
        // Read encrypted file
        Path encryptedPath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
        CryptoManager.EncryptedData encrypted = FileUtils.readEncryptedFile(encryptedPath);
        
        // Decrypt
        byte[] decrypted = cryptoManager.decrypt(encrypted);
        
        // Verify integrity
        String currentHash = FileUtils.calculateSHA256(decrypted);
        if (!currentHash.equals(vaultFile.getHash())) {
            auditLogger.log("WARNING: File integrity check failed for " + vaultFile.getOriginalName());
            throw new SecurityException("File integrity check failed");
        }
        
        auditLogger.log("File downloaded: " + vaultFile.getOriginalName());
        
        return decrypted;
    }
    
    /**
     * Securely delete a file from the vault
     */
    public void deleteFile(String fileId) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        VaultFile vaultFile = metadataManager.getFile(fileId);
        if (vaultFile == null) {
            throw new IllegalArgumentException("File not found");
        }
        
        // Securely delete encrypted file
        Path encryptedPath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
        SecureDeletion.secureDelete(encryptedPath.toFile());
        
        // Remove from metadata
        metadataManager.removeFile(fileId);
        
        auditLogger.log("File securely deleted: " + vaultFile.getOriginalName());
    }
    
    /**
     * Get list of all files in the vault
     */
    public List<VaultFile> listFiles() {
        if (!isInitialized) {
            return new ArrayList<>();
        }
        
        return metadataManager.getAllFiles();
    }
    
    /**
     * Search files by name
     */
    public List<VaultFile> searchFiles(String query) {
        if (!isInitialized || query == null || query.trim().isEmpty()) {
            return listFiles();
        }
        
        return metadataManager.searchFiles(query.toLowerCase());
    }
    
    /**
     * Create encrypted backup of the vault
     */
    public void createBackup(File backupFile) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        BackupManager backupManager = new BackupManager(cryptoManager);
        backupManager.createBackup(vaultPath, backupFile);
        
        auditLogger.log("Backup created: " + backupFile.getName());
    }
    
    /**
     * Restore vault from encrypted backup
     */
    public void restoreBackup(File backupFile) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        BackupManager backupManager = new BackupManager(cryptoManager);
        backupManager.restoreBackup(backupFile, vaultPath);
        
        // Reload metadata
        metadataManager.loadMetadata();
        
        auditLogger.log("Backup restored: " + backupFile.getName());
    }
    
    /**
     * Execute panic wipe - destroy all vault contents
     */
    private void executePanicWipe() {
        try {
            auditLogger.log("PANIC WIPE INITIATED");
            
            // Securely delete all files
            File filesDir = new File(AppConfig.FILES_DIR);
            if (filesDir.exists()) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        SecureDeletion.secureDelete(file);
                    }
                }
            }
            
            // Delete configuration and metadata
            SecureDeletion.secureDelete(new File(AppConfig.CONFIG_FILE));
            SecureDeletion.secureDelete(new File(AppConfig.METADATA_FILE));
            SecureDeletion.secureDelete(new File(AppConfig.SALT_FILE));
            
            auditLogger.log("PANIC WIPE COMPLETED");
            
            // Exit application silently
            System.exit(0);
            
        } catch (Exception e) {
            // Silent failure - still exit
            System.exit(0);
        }
    }
    
    /**
     * Get decoy files for decoy mode
     */
    public List<String> getDecoyFiles() {
        return decoyManager.getDecoyFileList();
    }
    
    /**
     * Cleanup and logout
     */
    public void logout() {
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        
        isInitialized = false;
        auditLogger.log("User logged out");
    }
    
    /**
     * Authentication result
     */
    public static class AuthResult {
        public enum Type { MASTER, PANIC, DECOY, INVALID }
        
        private final Type type;
        private final boolean success;
        
        public AuthResult(Type type, boolean success) {
            this.type = type;
            this.success = success;
        }
        
        public Type getType() { return type; }
        public boolean isSuccess() { return success; }
    }
}