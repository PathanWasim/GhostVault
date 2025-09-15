package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles secure vault initialization with directory structure creation
 */
public class VaultInitializer {
    
    /**
     * Initialize a new vault with the given passwords
     */
    public static void initializeVault(String masterPassword, String panicPassword, String decoyPassword) throws Exception {
        // Validate passwords before proceeding
        validatePasswords(masterPassword, panicPassword, decoyPassword);
        
        // Create vault directory structure
        createVaultDirectories();
        
        // Initialize password manager
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
        
        // Initialize metadata manager with empty metadata
        MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        metadataManager.setEncryptionKey(passwordManager.deriveVaultKey(masterPassword));
        metadataManager.saveMetadata();
        
        // Create initial decoy files
        createInitialDecoyFiles();
        
        // Set appropriate permissions
        setVaultPermissions();
    }
    
    /**
     * Validate passwords meet all requirements
     */
    private static void validatePasswords(String masterPassword, String panicPassword, String decoyPassword) {
        // Check password strength
        if (PasswordManager.getPasswordStrength(masterPassword) < AppConfig.PASSWORD_MIN_STRENGTH) {
            throw new IllegalArgumentException("Master password does not meet strength requirements");
        }
        
        if (PasswordManager.getPasswordStrength(panicPassword) < 3) {
            throw new IllegalArgumentException("Panic password does not meet strength requirements");
        }
        
        if (PasswordManager.getPasswordStrength(decoyPassword) < 3) {
            throw new IllegalArgumentException("Decoy password does not meet strength requirements");
        }
        
        // Check passwords are different
        if (masterPassword.equals(panicPassword) || 
            masterPassword.equals(decoyPassword) || 
            panicPassword.equals(decoyPassword)) {
            throw new IllegalArgumentException("All passwords must be different from each other");
        }
    }
    
    /**
     * Create vault directory structure
     */
    private static void createVaultDirectories() throws IOException {
        // Create main vault directory
        FileUtils.ensureDirectoryExists(AppConfig.VAULT_DIR);
        
        // Create subdirectories
        FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
        FileUtils.ensureDirectoryExists(AppConfig.DECOYS_DIR);
        
        // Create logs directory (for future audit logging)
        String logsDir = AppConfig.VAULT_DIR + "/logs";
        FileUtils.ensureDirectoryExists(logsDir);
        
        // Create temp directory for operations
        String tempDir = AppConfig.VAULT_DIR + "/temp";
        FileUtils.ensureDirectoryExists(tempDir);
    }
    
    /**
     * Create initial decoy files to make decoy mode realistic
     */
    private static void createInitialDecoyFiles() throws IOException {
        Path decoyDir = Paths.get(AppConfig.DECOYS_DIR);
        
        // Create some realistic decoy files
        String[] decoyContents = {
            "Meeting Notes - Q4 Planning\n\nAttendees: John, Sarah, Mike\nDate: " + 
            java.time.LocalDate.now() + "\n\nAgenda:\n1. Budget review\n2. Project timeline\n3. Resource allocation",
            
            "Shopping List\n\n- Milk\n- Bread\n- Eggs\n- Apples\n- Chicken\n- Rice\n- Pasta\n- Cheese",
            
            "Book Notes - Project Management\n\nChapter 1: Introduction\n- Define project scope\n" +
            "- Identify stakeholders\n- Set clear objectives\n\nChapter 2: Planning\n- Create timeline\n- Allocate resources",
            
            "Vacation Itinerary\n\nDay 1: Arrival\n- Check into hotel\n- Explore downtown\n" +
            "Day 2: Sightseeing\n- Museum visit\n- City tour\nDay 3: Departure"
        };
        
        for (int i = 0; i < Math.min(decoyContents.length, AppConfig.DECOY_FILES.length); i++) {
            Path decoyFile = decoyDir.resolve(AppConfig.DECOY_FILES[i]);
            Files.write(decoyFile, decoyContents[i].getBytes());
        }
    }
    
    /**
     * Set appropriate permissions for vault directories
     */
    private static void setVaultPermissions() {
        try {
            File vaultDir = new File(AppConfig.VAULT_DIR);
            
            // Set directory permissions (owner read/write/execute only)
            vaultDir.setReadable(true, true);
            vaultDir.setWritable(true, true);
            vaultDir.setExecutable(true, true);
            
            // Hide vault directory on Windows
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Runtime.getRuntime().exec("attrib +H \"" + vaultDir.getAbsolutePath() + "\"");
                } catch (Exception e) {
                    // Ignore if hiding fails
                }
            }
            
            // On Unix-like systems, set more restrictive permissions
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Runtime.getRuntime().exec("chmod 700 \"" + vaultDir.getAbsolutePath() + "\"");
                } catch (Exception e) {
                    // Ignore if chmod fails
                }
            }
            
        } catch (Exception e) {
            // Permissions setting is best-effort, don't fail if it doesn't work
        }
    }
    
    /**
     * Check if vault is already initialized
     */
    public static boolean isVaultInitialized() {
        File configFile = new File(AppConfig.CONFIG_FILE);
        File saltFile = new File(AppConfig.SALT_FILE);
        File vaultDir = new File(AppConfig.VAULT_DIR);
        File filesDir = new File(AppConfig.FILES_DIR);
        
        return configFile.exists() && 
               saltFile.exists() && 
               vaultDir.exists() && 
               vaultDir.isDirectory() &&
               filesDir.exists() && 
               filesDir.isDirectory();
    }
    
    /**
     * Get vault initialization status
     */
    public static VaultStatus getVaultStatus() {
        if (!isVaultInitialized()) {
            return VaultStatus.NOT_INITIALIZED;
        }
        
        // Check if vault appears to be corrupted
        try {
            File configFile = new File(AppConfig.CONFIG_FILE);
            File metadataFile = new File(AppConfig.METADATA_FILE);
            
            if (configFile.exists() && configFile.length() == 0) {
                return VaultStatus.CORRUPTED;
            }
            
            if (metadataFile.exists() && metadataFile.length() == 0) {
                return VaultStatus.CORRUPTED;
            }
            
            return VaultStatus.INITIALIZED;
            
        } catch (Exception e) {
            return VaultStatus.CORRUPTED;
        }
    }
    
    /**
     * Reset vault (for testing or recovery)
     */
    public static void resetVault() throws IOException {
        // Delete all vault files and directories
        deleteDirectory(new File(AppConfig.VAULT_DIR));
    }
    
    /**
     * Recursively delete directory
     */
    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        
        if (!directory.delete()) {
            throw new IOException("Failed to delete: " + directory.getAbsolutePath());
        }
    }
    
    /**
     * Vault status enumeration
     */
    public enum VaultStatus {
        NOT_INITIALIZED,
        INITIALIZED,
        CORRUPTED
    }
    
    /**
     * Get vault statistics
     */
    public static VaultInfo getVaultInfo() {
        if (!isVaultInitialized()) {
            return new VaultInfo(false, 0, 0, 0);
        }
        
        try {
            File vaultDir = new File(AppConfig.VAULT_DIR);
            File filesDir = new File(AppConfig.FILES_DIR);
            File decoysDir = new File(AppConfig.DECOYS_DIR);
            
            long vaultSize = calculateDirectorySize(vaultDir);
            int fileCount = countFiles(filesDir);
            int decoyCount = countFiles(decoysDir);
            
            return new VaultInfo(true, vaultSize, fileCount, decoyCount);
            
        } catch (Exception e) {
            return new VaultInfo(true, 0, 0, 0);
        }
    }
    
    /**
     * Calculate directory size recursively
     */
    private static long calculateDirectorySize(File directory) {
        long size = 0;
        
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        
        return size;
    }
    
    /**
     * Count files in directory
     */
    private static int countFiles(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        File[] files = directory.listFiles();
        return files != null ? files.length : 0;
    }
    
    /**
     * Vault information data class
     */
    public static class VaultInfo {
        private final boolean initialized;
        private final long totalSize;
        private final int fileCount;
        private final int decoyCount;
        
        public VaultInfo(boolean initialized, long totalSize, int fileCount, int decoyCount) {
            this.initialized = initialized;
            this.totalSize = totalSize;
            this.fileCount = fileCount;
            this.decoyCount = decoyCount;
        }
        
        public boolean isInitialized() { return initialized; }
        public long getTotalSize() { return totalSize; }
        public int getFileCount() { return fileCount; }
        public int getDecoyCount() { return decoyCount; }
        
        public String getFormattedSize() {
            return FileUtils.formatFileSize(totalSize);
        }
        
        @Override
        public String toString() {
            return String.format("VaultInfo{initialized=%s, size=%s, files=%d, decoys=%d}", 
                initialized, getFormattedSize(), fileCount, decoyCount);
        }
    }
}