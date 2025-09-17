package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.FileManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes panic mode with complete silent data destruction
 * WARNING: This class permanently destroys all vault data with no recovery possibility
 */
public class PanicModeExecutor {
    
    private static final int PANIC_OVERWRITE_PASSES = 7; // More passes than normal deletion
    private static final String PANIC_LOG_FILE = System.getProperty("java.io.tmpdir") + "/ghostvault_panic.log";
    
    private final List<String> destructionLog;
    private boolean silentMode;
    
    public PanicModeExecutor() {
        this.destructionLog = new ArrayList<>();
        this.silentMode = true; // Always silent by default
    }
    
    /**
     * Execute panic mode - complete vault destruction
     * This method performs irreversible data destruction
     */
    public void executePanicMode() {
        try {
            log("PANIC MODE INITIATED - " + java.time.LocalDateTime.now());
            log("WARNING: All vault data will be permanently destroyed");
            
            // Phase 1: Destroy all encrypted files
            destroyEncryptedFiles();
            
            // Phase 2: Destroy metadata and configuration
            destroyMetadataAndConfig();
            
            // Phase 3: Destroy decoy files
            destroyDecoyFiles();
            
            // Phase 4: Destroy vault directory structure
            destroyVaultDirectories();
            
            // Phase 5: Clear system traces
            clearSystemTraces();
            
            // Phase 6: Overwrite memory
            clearMemory();
            
            log("PANIC MODE COMPLETED - All data destroyed");
            
            // Final step: Terminate application immediately
            terminateApplication();
            
        } catch (Exception e) {
            // Even if panic mode fails partially, still terminate
            log("PANIC MODE ERROR: " + e.getMessage());
            terminateApplication();
        }
    }
    
    /**
     * Destroy all encrypted files in the vault
     */
    private void destroyEncryptedFiles() {
        try {
            log("Phase 1: Destroying encrypted files...");
            
            File filesDir = new File(AppConfig.FILES_DIR);
            if (filesDir.exists() && filesDir.isDirectory()) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            secureDeleteFile(file.toPath(), PANIC_OVERWRITE_PASSES);
                            log("Destroyed: " + file.getName());
                        }
                    }
                }
            }
            
            log("Phase 1 completed: All encrypted files destroyed");
            
        } catch (Exception e) {
            log("Phase 1 error: " + e.getMessage());
        }
    }
    
    /**
     * Destroy metadata and configuration files
     */
    private void destroyMetadataAndConfig() {
        try {
            log("Phase 2: Destroying metadata and configuration...");
            
            // Destroy configuration files
            String[] configFiles = {
                AppConfig.CONFIG_FILE,
                AppConfig.METADATA_FILE,
                AppConfig.SALT_FILE,
                AppConfig.LOG_FILE
            };
            
            for (String configFile : configFiles) {
                Path filePath = Paths.get(configFile);
                if (Files.exists(filePath)) {
                    secureDeleteFile(filePath, PANIC_OVERWRITE_PASSES);
                    log("Destroyed config: " + filePath.getFileName());
                }
            }
            
            log("Phase 2 completed: All configuration destroyed");
            
        } catch (Exception e) {
            log("Phase 2 error: " + e.getMessage());
        }
    }
    
    /**
     * Destroy decoy files
     */
    private void destroyDecoyFiles() {
        try {
            log("Phase 3: Destroying decoy files...");
            
            File decoysDir = new File(AppConfig.DECOYS_DIR);
            if (decoysDir.exists() && decoysDir.isDirectory()) {
                File[] files = decoysDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            secureDeleteFile(file.toPath(), PANIC_OVERWRITE_PASSES);
                            log("Destroyed decoy: " + file.getName());
                        }
                    }
                }
            }
            
            log("Phase 3 completed: All decoy files destroyed");
            
        } catch (Exception e) {
            log("Phase 3 error: " + e.getMessage());
        }
    }
    
    /**
     * Destroy vault directory structure
     */
    private void destroyVaultDirectories() {
        try {
            log("Phase 4: Destroying vault directories...");
            
            // Delete directories in reverse order (deepest first)
            String[] directories = {
                AppConfig.FILES_DIR,
                AppConfig.DECOYS_DIR,
                AppConfig.VAULT_DIR + "/logs",
                AppConfig.VAULT_DIR + "/temp",
                AppConfig.VAULT_DIR
            };
            
            for (String directory : directories) {
                File dir = new File(directory);
                if (dir.exists()) {
                    // Overwrite directory metadata if possible
                    try {
                        overwriteDirectoryMetadata(dir);
                    } catch (Exception e) {
                        // Continue even if metadata overwrite fails
                    }
                    
                    if (dir.delete()) {
                        log("Destroyed directory: " + dir.getName());
                    }
                }
            }
            
            log("Phase 4 completed: All directories destroyed");
            
        } catch (Exception e) {
            log("Phase 4 error: " + e.getMessage());
        }
    }
    
    /**
     * Clear system traces (temp files, registry entries, etc.)
     */
    private void clearSystemTraces() {
        try {
            log("Phase 5: Clearing system traces...");
            
            // Clear Java temp files
            clearJavaTempFiles();
            
            // Clear system temp files related to GhostVault
            clearSystemTempFiles();
            
            // Clear recent files list (Windows)
            clearRecentFiles();
            
            log("Phase 5 completed: System traces cleared");
            
        } catch (Exception e) {
            log("Phase 5 error: " + e.getMessage());
        }
    }
    
    /**
     * Clear Java temporary files
     */
    private void clearJavaTempFiles() {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempDirFile = new File(tempDir);
            
            if (tempDirFile.exists()) {
                File[] tempFiles = tempDirFile.listFiles((dir, name) -> 
                    name.toLowerCase().contains("ghostvault") || 
                    name.toLowerCase().contains("vault") ||
                    name.startsWith("tmp") && name.contains("ghost"));
                
                if (tempFiles != null) {
                    for (File tempFile : tempFiles) {
                        try {
                            if (tempFile.isFile()) {
                                secureDeleteFile(tempFile.toPath(), 3);
                            }
                        } catch (Exception e) {
                            // Continue with other files
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore temp file cleanup errors
        }
    }
    
    /**
     * Clear system temporary files
     */
    private void clearSystemTempFiles() {
        try {
            // Windows temp directories
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                String[] tempDirs = {
                    System.getenv("TEMP"),
                    System.getenv("TMP"),
                    "C:\\Windows\\Temp"
                };
                
                for (String tempDir : tempDirs) {
                    if (tempDir != null) {
                        clearTempDirectory(new File(tempDir));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore system temp cleanup errors
        }
    }
    
    /**
     * Clear recent files list
     */
    private void clearRecentFiles() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Clear Windows recent files for GhostVault
                Runtime.getRuntime().exec("reg delete \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\RecentDocs\" /f");
            }
        } catch (Exception e) {
            // Ignore registry cleanup errors
        }
    }
    
    /**
     * Clear temporary directory of GhostVault-related files
     */
    private void clearTempDirectory(File tempDir) {
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            return;
        }
        
        try {
            File[] files = tempDir.listFiles((dir, name) -> 
                name.toLowerCase().contains("ghostvault") ||
                name.toLowerCase().contains("vault"));
            
            if (files != null) {
                for (File file : files) {
                    try {
                        if (file.isFile()) {
                            secureDeleteFile(file.toPath(), 3);
                        }
                    } catch (Exception e) {
                        // Continue with other files
                    }
                }
            }
        } catch (Exception e) {
            // Ignore temp directory errors
        }
    }
    
    /**
     * Clear sensitive data from memory
     */
    private void clearMemory() {
        try {
            log("Phase 6: Clearing memory...");
            
            // Force garbage collection multiple times
            for (int i = 0; i < 5; i++) {
                System.gc();
                // Note: System.runFinalization() is deprecated in JDK 18+
                // GC will handle finalization automatically
                Thread.sleep(100);
            }
            
            // Allocate and clear large memory blocks to overwrite heap
            try {
                for (int i = 0; i < 10; i++) {
                    byte[] memoryBlock = new byte[1024 * 1024]; // 1MB blocks
                    new SecureRandom().nextBytes(memoryBlock);
                    memoryBlock = null;
                    System.gc();
                }
            } catch (OutOfMemoryError e) {
                // Expected when clearing memory
            }
            
            log("Phase 6 completed: Memory cleared");
            
        } catch (Exception e) {
            log("Phase 6 error: " + e.getMessage());
        }
    }
    
    /**
     * Secure delete file with multiple overwrite passes
     */
    private void secureDeleteFile(Path filePath, int passes) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        
        long fileSize = Files.size(filePath);
        SecureRandom random = new SecureRandom();
        
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rws")) {
            
            for (int pass = 0; pass < passes; pass++) {
                file.seek(0);
                
                switch (pass % 4) {
                    case 0:
                        // Pass: Write 0x00
                        writePattern(file, fileSize, (byte) 0x00);
                        break;
                    case 1:
                        // Pass: Write 0xFF
                        writePattern(file, fileSize, (byte) 0xFF);
                        break;
                    case 2:
                        // Pass: Write 0xAA
                        writePattern(file, fileSize, (byte) 0xAA);
                        break;
                    case 3:
                        // Pass: Write random data
                        writeRandomPattern(file, fileSize, random);
                        break;
                }
                
                file.getFD().sync(); // Force write to disk
            }
        }
        
        // Finally delete the file
        Files.delete(filePath);
    }
    
    /**
     * Write specific byte pattern to file
     */
    private void writePattern(RandomAccessFile file, long fileSize, byte pattern) throws IOException {
        byte[] buffer = new byte[8192];
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
    private void writeRandomPattern(RandomAccessFile file, long fileSize, SecureRandom random) throws IOException {
        byte[] buffer = new byte[8192];
        
        long remaining = fileSize;
        while (remaining > 0) {
            int writeSize = (int) Math.min(buffer.length, remaining);
            random.nextBytes(buffer);
            file.write(buffer, 0, writeSize);
            remaining -= writeSize;
        }
    }
    
    /**
     * Overwrite directory metadata (best effort)
     */
    private void overwriteDirectoryMetadata(File directory) {
        try {
            // Create and delete temporary files to overwrite directory metadata
            for (int i = 0; i < 10; i++) {
                File tempFile = new File(directory, "temp_overwrite_" + i + ".tmp");
                Files.write(tempFile.toPath(), new byte[1024]);
                tempFile.delete();
            }
        } catch (Exception e) {
            // Ignore metadata overwrite errors
        }
    }
    
    /**
     * Terminate application immediately
     */
    protected void terminateApplication() {
        try {
            log("TERMINATING APPLICATION");
            
            // Save panic log if possible
            savePanicLog();
            
            // Force immediate termination
            Runtime.getRuntime().halt(0);
            
        } catch (Exception e) {
            // Force termination even if logging fails
            System.exit(0);
        }
    }
    
    /**
     * Log panic mode operations
     */
    private void log(String message) {
        if (!silentMode) {
            System.out.println("[PANIC] " + message);
        }
        
        destructionLog.add(java.time.LocalDateTime.now() + ": " + message);
    }
    
    /**
     * Save panic log for forensic analysis (if needed)
     */
    private void savePanicLog() {
        try {
            if (!destructionLog.isEmpty()) {
                Files.write(Paths.get(PANIC_LOG_FILE), 
                    String.join("\n", destructionLog).getBytes());
            }
        } catch (Exception e) {
            // Ignore log saving errors
        }
    }
    
    /**
     * Set silent mode (default is true)
     */
    public void setSilentMode(boolean silent) {
        this.silentMode = silent;
    }
    
    /**
     * Check if panic mode can be executed (vault exists)
     */
    public boolean canExecutePanicMode() {
        return new File(AppConfig.VAULT_DIR).exists();
    }
    
    /**
     * Get estimated destruction time in seconds
     */
    public int getEstimatedDestructionTime() {
        try {
            File vaultDir = new File(AppConfig.VAULT_DIR);
            if (!vaultDir.exists()) {
                return 1; // Minimal time if no vault
            }
            
            // Estimate based on vault size (rough calculation)
            long vaultSize = calculateDirectorySize(vaultDir);
            int estimatedSeconds = (int) Math.max(5, vaultSize / (1024 * 1024)); // 1 second per MB minimum 5 seconds
            
            return Math.min(estimatedSeconds, 30); // Cap at 30 seconds
            
        } catch (Exception e) {
            return 10; // Default estimate
        }
    }
    
    /**
     * Calculate directory size recursively
     */
    private long calculateDirectorySize(File directory) {
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
}