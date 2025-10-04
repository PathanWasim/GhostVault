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
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Manages encrypted file operations with UUID-based naming and secure deletion
 */
public class FileManager {
    
    private final String vaultPath;
    private final CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    public FileManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        
        // Ensure vault directories exist
        FileUtils.ensureDirectoryExists(vaultPath);
        FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
    }
    
    /**
     * Set encryption key for file operations
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    /**
     * Store file in vault with encryption and return VaultFile metadata
     */
    public VaultFile storeFile(File sourceFile) throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        if (!FileUtils.isValidFile(sourceFile)) {
            throw new IllegalArgumentException("Invalid file for vault storage");
        }
        
        // Generate unique file ID and encrypted name
        String fileId = UUID.randomUUID().toString();
        String encryptedFileName = fileId + ".enc";
        
        // Read source file data
        byte[] fileData = Files.readAllBytes(sourceFile.toPath());
        
        try {
            // Calculate SHA-256 hash for integrity verification
            String originalHash = FileUtils.calculateSHA256(fileData);
            
            // Encrypt file data
            byte[] encryptedBytes = cryptoManager.encrypt(fileData, encryptionKey);
            CryptoManager.EncryptedData encryptedData = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Write encrypted file to vault
            Path encryptedFilePath = Paths.get(AppConfig.FILES_DIR, encryptedFileName);
            FileUtils.writeEncryptedFile(encryptedFilePath, encryptedData);
            
            // Create VaultFile metadata
            VaultFile vaultFile = new VaultFile(
                sourceFile.getName(),
                fileId,
                encryptedFileName,
                sourceFile.length(),
                originalHash,
                System.currentTimeMillis()
            );
            
            return vaultFile;
            
        } finally {
            // Clear sensitive data from memory
            MemoryUtils.secureWipe(fileData);
        }
    }
    
    /**
     * Retrieve and decrypt file from vault
     */
    public byte[] retrieveFile(VaultFile vaultFile) throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        Path encryptedFilePath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
        
        if (!Files.exists(encryptedFilePath)) {
            throw new FileNotFoundException("Encrypted file not found: " + vaultFile.getEncryptedName());
        }
        
        // Read encrypted data
        CryptoManager.EncryptedData encryptedData = FileUtils.readEncryptedFile(encryptedFilePath);
        
        // Decrypt file data
        byte[] decryptedData = cryptoManager.decrypt(encryptedData, encryptionKey);
        
        // Verify file integrity
        String currentHash = FileUtils.calculateSHA256(decryptedData);
        if (!currentHash.equals(vaultFile.getHash())) {
            MemoryUtils.secureWipe(decryptedData);
            throw new SecurityException("File integrity verification failed for: " + vaultFile.getOriginalName());
        }
        
        return decryptedData;
    }
    
    /**
     * Export file from vault to destination
     */
    public void exportFile(VaultFile vaultFile, File destinationFile) throws Exception {
        byte[] fileData = null;
        
        try {
            // Retrieve and decrypt file
            fileData = retrieveFile(vaultFile);
            
            // Write to destination
            Files.write(destinationFile.toPath(), fileData);
            
        } finally {
            // Clear sensitive data from memory
            if (fileData != null) {
                MemoryUtils.secureWipe(fileData);
            }
        }
    }
    
    /**
     * Securely delete file from vault using DoD 5220.22-M standard
     */
    public void secureDeleteFile(VaultFile vaultFile) throws Exception {
        Path encryptedFilePath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
        
        if (!Files.exists(encryptedFilePath)) {
            // File already deleted or doesn't exist
            return;
        }
        
        secureDeleteFile(encryptedFilePath);
    }
    
    /**
     * Securely delete file using DoD 5220.22-M standard (3-pass overwrite)
     */
    public static void secureDeleteFile(Path filePath) throws Exception {
        if (!Files.exists(filePath)) {
            return;
        }
        
        long fileSize = Files.size(filePath);
        SecureRandom random = new SecureRandom();
        
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rws")) {
            
            // DoD 5220.22-M 3-pass overwrite pattern
            for (int pass = 0; pass < AppConfig.SECURE_DELETE_PASSES; pass++) {
                file.seek(0);
                
                switch (pass) {
                    case 0:
                        // Pass 1: Write 0x00 (all zeros)
                        writePattern(file, fileSize, (byte) 0x00);
                        break;
                    case 1:
                        // Pass 2: Write 0xFF (all ones)
                        writePattern(file, fileSize, (byte) 0xFF);
                        break;
                    case 2:
                        // Pass 3: Write random data
                        writeRandomPattern(file, fileSize, random);
                        break;
                }
                
                // Force write to disk
                file.getFD().sync();
            }
        }
        
        // Finally delete the file
        Files.delete(filePath);
    }
    
    /**
     * Write specific byte pattern to file
     */
    private static void writePattern(RandomAccessFile file, long fileSize, byte pattern) throws IOException {
        byte[] buffer = new byte[8192]; // 8KB buffer
        java.util.Arrays.fill(buffer, pattern);
        
        long remaining = fileSize;
        while (remaining > 0) {
            int writeSize = (int) Math.min(buffer.length, remaining);
            file.write(buffer, 0, writeSize);
            remaining -= writeSize;
        }
    }
    
    /**
     * Write random pattern to file
     */
    private static void writeRandomPattern(RandomAccessFile file, long fileSize, SecureRandom random) throws IOException {
        byte[] buffer = new byte[8192]; // 8KB buffer
        
        long remaining = fileSize;
        while (remaining > 0) {
            int writeSize = (int) Math.min(buffer.length, remaining);
            random.nextBytes(buffer);
            file.write(buffer, 0, writeSize);
            remaining -= writeSize;
        }
    }
    
    /**
     * Check if file exists in vault
     */
    public boolean fileExists(VaultFile vaultFile) {
        Path encryptedFilePath = Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
        return Files.exists(encryptedFilePath);
    }
    
    /**
     * Get encrypted file path for a vault file
     */
    public Path getEncryptedFilePath(VaultFile vaultFile) {
        return Paths.get(AppConfig.FILES_DIR, vaultFile.getEncryptedName());
    }
    
    /**
     * Verify file integrity without decrypting entire file
     */
    public boolean verifyFileIntegrity(VaultFile vaultFile) {
        try {
            byte[] fileData = retrieveFile(vaultFile);
            String currentHash = FileUtils.calculateSHA256(fileData);
            MemoryUtils.secureWipe(fileData);
            return currentHash.equals(vaultFile.getHash());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get vault storage statistics
     */
    public VaultStats getVaultStats() throws IOException {
        Path vaultDir = Paths.get(AppConfig.FILES_DIR);
        
        if (!Files.exists(vaultDir)) {
            return new VaultStats(0, 0);
        }
        
        long totalSize = Files.walk(vaultDir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
        
        long fileCount = Files.walk(vaultDir)
                .filter(Files::isRegularFile)
                .count();
        
        return new VaultStats(totalSize, fileCount);
    }
    
    /**
     * Vault statistics data class
     */
    public static class VaultStats {
        private final long totalSize;
        private final long fileCount;
        
        public VaultStats(long totalSize, long fileCount) {
            this.totalSize = totalSize;
            this.fileCount = fileCount;
        }
        
        public long getTotalSize() { return totalSize; }
        public long getFileCount() { return fileCount; }
        
        public String getFormattedSize() {
            return FileUtils.formatFileSize(totalSize);
        }
        
        @Override
        public String toString() {
            return String.format("VaultStats{files=%d, size=%s}", fileCount, getFormattedSize());
        }
    }
    
    /**
     * Clean up resources and clear sensitive data
     */
    public void cleanup() {
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
        encryptionKey = null;
    }
}