package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.security.SecureDeletion;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;
import com.ghostvault.audit.AuditManager;

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
    private final AuditManager auditManager;
    private final MetadataManager metadataManager;
    private final DecoyManager decoyManager;
    private final FileManager fileManager;
    
    private String masterPassword; // Store for backup operations
    private boolean isInitialized = false;
    private boolean isFirstRun = false;
    
    public VaultManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.passwordManager = new PasswordManager(vaultPath);
        this.auditManager = new AuditManager();
        this.metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        this.decoyManager = new DecoyManager();
        this.fileManager = new FileManager(vaultPath);
        
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
            decoyManager.generateDecoyFiles(10);
            auditManager.logSecurityEvent("VAULT_INIT", "Vault directory structure created", AuditManager.AuditSeverity.INFO, null, null);
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
        this.masterPassword = masterPassword; // Store for backup operations
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
        cryptoManager.initializeWithPassword(masterPassword, passwordManager.getSalt());
        metadataManager.setEncryptionKey(cryptoManager.getMasterKey());
        fileManager.setEncryptionKey(cryptoManager.getMasterKey());
        
        isInitialized = true;
        isFirstRun = false;
        
        auditManager.logSecurityEvent("VAULT_INIT", "Vault initialized with new passwords", AuditManager.AuditSeverity.INFO, null, null);
    }
    
    /**
     * Authenticate user and determine access mode
     */
    public AuthResult authenticate(String password) throws Exception {
        PasswordManager.PasswordType type = passwordManager.validatePassword(password);
        
        switch (type) {
            case MASTER:
                this.masterPassword = password; // Store for backup operations
                cryptoManager.initializeWithPassword(password, passwordManager.getSalt());
                metadataManager.setEncryptionKey(cryptoManager.getMasterKey());
                fileManager.setEncryptionKey(cryptoManager.getMasterKey());
                metadataManager.loadMetadata();
                isInitialized = true;
                auditManager.logSecurityEvent("LOGIN_SUCCESS", "Master authentication successful", AuditManager.AuditSeverity.INFO, null, null);
                return new AuthResult(AuthResult.Type.MASTER, true);
                
            case PANIC:
                auditManager.logSecurityEvent("PANIC_MODE", "Panic mode activated", AuditManager.AuditSeverity.CRITICAL, null, null);
                executePanicWipe();
                return new AuthResult(AuthResult.Type.PANIC, true);
                
            case DECOY:
                // Initialize with decoy mode (limited functionality)
                isInitialized = true;
                auditManager.logSecurityEvent("DECOY_MODE", "Decoy mode accessed", AuditManager.AuditSeverity.INFO, null, null);
                return new AuthResult(AuthResult.Type.DECOY, true);
                
            default:
                auditManager.logSecurityEvent("LOGIN_FAILED", "Authentication failed", AuditManager.AuditSeverity.WARNING, null, null);
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
        
        auditManager.logSecurityEvent("FILE_UPLOAD", "File uploaded: " + sourceFile.getName(), AuditManager.AuditSeverity.INFO, null, null);
        
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
            auditManager.logSecurityEvent("INTEGRITY_FAIL", "File integrity check failed for " + vaultFile.getOriginalName(), AuditManager.AuditSeverity.ERROR, null, null);
            throw new SecurityException("File integrity check failed");
        }
        
        auditManager.logSecurityEvent("FILE_DOWNLOAD", "File downloaded: " + vaultFile.getOriginalName(), AuditManager.AuditSeverity.INFO, null, null);
        
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
        
        auditManager.logSecurityEvent("FILE_DELETE", "File securely deleted: " + vaultFile.getOriginalName(), AuditManager.AuditSeverity.INFO, null, null);
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
        
        com.ghostvault.core.BackupManager backupManager = new com.ghostvault.core.BackupManager(fileManager, metadataManager);
        backupManager.setBackupEncryptionKey(cryptoManager.deriveKey(masterPassword, passwordManager.getSalt()));
        backupManager.createBackup(backupFile, new BackupOptions());
        
        auditManager.logSecurityEvent("BACKUP_CREATED", "Backup created: " + backupFile.getName(), AuditManager.AuditSeverity.INFO, null, null);
    }
    
    /**
     * Restore vault from encrypted backup
     */
    public void restoreBackup(File backupFile) throws Exception {
        if (!isInitialized) {
            throw new IllegalStateException("Vault not initialized");
        }
        
        com.ghostvault.core.BackupManager backupManager = new com.ghostvault.core.BackupManager(fileManager, metadataManager);
        backupManager.setBackupEncryptionKey(cryptoManager.deriveKey(masterPassword, passwordManager.getSalt()));
        backupManager.restoreFromBackup(backupFile, new RestoreOptions());
        
        // Reload metadata
        metadataManager.loadMetadata();
        
        auditManager.logSecurityEvent("BACKUP_RESTORED", "Backup restored: " + backupFile.getName(), AuditManager.AuditSeverity.INFO, null, null);
    }
    
    /**
     * Execute panic wipe - destroy all vault contents
     */
    private void executePanicWipe() {
        try {
            auditManager.logSecurityEvent("PANIC_WIPE", "Panic wipe initiated", AuditManager.AuditSeverity.CRITICAL, null, null);
            
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
            
            auditManager.logSecurityEvent("PANIC_WIPE", "Panic wipe completed", AuditManager.AuditSeverity.CRITICAL, null, null);
            
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
        List<VaultFile> files = decoyManager.getDecoyFiles();
        List<String> names = new ArrayList<>();
        for (VaultFile vf : files) names.add(vf.getOriginalName());
        return names;
    }
    
    /**
     * Cleanup and logout
     */
    public void logout() {
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        
        isInitialized = false;
        auditManager.logSecurityEvent("LOGOUT", "User logged out", AuditManager.AuditSeverity.INFO, null, null);
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