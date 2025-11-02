package com.ghostvault.core;

import com.ghostvault.logging.PersistenceLogger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

/**
 * Manages persistent storage verification and recovery for vault files and metadata
 */
public class PersistentStorageManager {
    
    private final String vaultPath;
    private final String metadataPath;
    
    public PersistentStorageManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.metadataPath = vaultPath + "/metadata.json";
    }
    
    /**
     * Initialize vault directory structure with proper permissions
     */
    public boolean initializeVaultStructure() {
        try {
            System.out.println("🏗️ Initializing vault directory structure...");
            
            // Create main vault directory
            Path vaultDir = Paths.get(vaultPath);
            Files.createDirectories(vaultDir);
            System.out.println("✅ Created vault directory: " + vaultPath);
            
            // Create files subdirectory
            Path filesDir = vaultDir.resolve("files");
            Files.createDirectories(filesDir);
            System.out.println("✅ Created files directory: " + filesDir);
            
            // Create metadata directory
            Path metadataDir = vaultDir.resolve("metadata");
            Files.createDirectories(metadataDir);
            System.out.println("✅ Created metadata directory: " + metadataDir);
            
            // Create backup directory
            Path backupDir = vaultDir.resolve("backups");
            Files.createDirectories(backupDir);
            System.out.println("✅ Created backup directory: " + backupDir);
            
            // Verify directory permissions
            if (!Files.isWritable(vaultDir)) {
                System.err.println("❌ Vault directory is not writable: " + vaultPath);
                return false;
            }
            
            System.out.println("✅ Vault directory structure initialized successfully");
            PersistenceLogger.logVaultInitialization(vaultPath, true, null);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize vault structure: " + e.getMessage());
            e.printStackTrace();
            PersistenceLogger.logVaultInitialization(vaultPath, false, e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify that all required directories exist and are accessible
     */
    public StorageVerification verifyStorageIntegrity() {
        StorageVerification verification = new StorageVerification();
        
        try {
            // Check main vault directory
            Path vaultDir = Paths.get(vaultPath);
            verification.setVaultDirectoryExists(Files.exists(vaultDir) && Files.isDirectory(vaultDir));
            verification.setVaultDirectoryWritable(Files.isWritable(vaultDir));
            
            // Check files directory
            Path filesDir = vaultDir.resolve("files");
            verification.setFilesDirectoryExists(Files.exists(filesDir) && Files.isDirectory(filesDir));
            
            // Check metadata file
            Path metadataFile = Paths.get(metadataPath);
            verification.setMetadataExists(Files.exists(metadataFile));
            if (verification.isMetadataExists()) {
                verification.setMetadataSize(Files.size(metadataFile));
            }
            
            // Count stored files
            if (verification.isFilesDirectoryExists()) {
                long fileCount = Files.list(filesDir).count();
                verification.setStoredFileCount((int) fileCount);
            }
            
            verification.setLastVerified(java.time.LocalDateTime.now());
            
            System.out.println("📋 Storage verification complete: " + verification);
            
            // Log verification results
            PersistenceLogger.logStorageVerification(
                verification.isVaultDirectoryExists(),
                verification.isVaultDirectoryWritable(),
                verification.isFilesDirectoryExists(),
                verification.isMetadataExists(),
                verification.getStoredFileCount(),
                null
            );
            
        } catch (Exception e) {
            System.err.println("❌ Storage verification failed: " + e.getMessage());
            verification.setVerificationError(e.getMessage());
            
            // Log verification failure
            PersistenceLogger.logStorageVerification(false, false, false, false, 0, e.getMessage());
        }
        
        return verification;
    }
    
    /**
     * Attempt to recover corrupted or missing vault directories
     */
    public boolean recoverVaultStructure() {
        System.out.println("🔧 Attempting vault structure recovery...");
        
        try {
            // Re-initialize directory structure
            if (!initializeVaultStructure()) {
                return false;
            }
            
            // Verify recovery was successful
            StorageVerification verification = verifyStorageIntegrity();
            boolean recovered = verification.isVaultDirectoryExists() && 
                              verification.isVaultDirectoryWritable() && 
                              verification.isFilesDirectoryExists();
            
            if (recovered) {
                System.out.println("✅ Vault structure recovery successful");
            } else {
                System.err.println("❌ Vault structure recovery failed");
            }
            
            return recovered;
            
        } catch (Exception e) {
            System.err.println("❌ Vault recovery error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get vault directory path
     */
    public String getVaultPath() {
        return vaultPath;
    }
    
    /**
     * Get metadata file path
     */
    public String getMetadataPath() {
        return metadataPath;
    }
    
    /**
     * Check if vault structure is properly initialized
     */
    public boolean isVaultInitialized() {
        StorageVerification verification = verifyStorageIntegrity();
        return verification.isVaultDirectoryExists() && 
               verification.isVaultDirectoryWritable() && 
               verification.isFilesDirectoryExists();
    }
}