package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes panic mode with CRYPTOGRAPHIC ERASURE FIRST approach
 * 
 * SECURITY IMPROVEMENTS v2.0:
 * - PRIMARY: Destroy encryption keys (renders all data unrecoverable)
 * - SECONDARY: Best-effort physical overwrite (limited effectiveness on SSDs)
 * - Dry-run mode for safe testing
 * - Silent operation (no UI feedback)
 * 
 * CRITICAL: Cryptographic erasure is the ONLY reliable method on modern storage.
 * Physical overwrite is secondary and may not work on SSDs, journaling filesystems,
 * or copy-on-write filesystems.
 * 
 * @version 2.0.0 - Cryptographic Erasure First
 */
public class PanicModeExecutor {
    
    private final List<String> destructionLog;
    private boolean dryRun;
    
    public PanicModeExecutor() {
        this.destructionLog = new ArrayList<>();
        this.dryRun = false;
    }
    
    /**
     * Execute panic mode with cryptographic erasure
     * 
     * @param vaultRoot Path to vault root directory
     * @param dryRun If true, simulates destruction without actual file operations
     */
    public void executePanic(Path vaultRoot, boolean dryRun) {
        this.dryRun = dryRun;
        
        try {
            log("=== PANIC MODE INITIATED ===");
            log("Dry Run: " + dryRun);
            log("Vault Root: " + vaultRoot);
            log("Timestamp: " + java.time.LocalDateTime.now());
            
            // PHASE 1: CRYPTOGRAPHIC ERASURE (Primary defense)
            log("\n[PHASE 1] CRYPTOGRAPHIC ERASURE - Destroying encryption keys");
            destroyEncryptionKeys(vaultRoot);
            
            // PHASE 2: Delete metadata and configuration
            log("\n[PHASE 2] Deleting metadata and configuration files");
            deleteMetadataAndConfig(vaultRoot);
            
            // PHASE 3: Best-effort physical overwrite (Secondary, limited effectiveness)
            log("\n[PHASE 3] Best-effort physical overwrite (SSD-limited)");
            overwriteVaultFiles(vaultRoot);
            
            // PHASE 4: Delete vault directory structure
            log("\n[PHASE 4] Removing vault directory structure");
            deleteVaultDirectories(vaultRoot);
            
            log("\n=== PANIC MODE COMPLETED ===");
            log("All vault data is now UNRECOVERABLE");
            
        } catch (Exception e) {
            log("ERROR during panic mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * PHASE 1: Destroy encryption keys (CRYPTOGRAPHIC ERASURE)
     * 
     * This is the PRIMARY security mechanism. Once keys are destroyed,
     * all encrypted data becomes permanently unrecoverable, regardless
     * of whether physical files are overwritten.
     */
    private void destroyEncryptionKeys(Path vaultRoot) {
        try {
            // Destroy password configuration (contains wrapped keys)
            Path configFile = vaultRoot.resolve("config.enc");
            if (Files.exists(configFile)) {
                if (!dryRun) {
                    // Overwrite with random data before deletion
                    byte[] randomData = new byte[(int) Files.size(configFile)];
                    new java.security.SecureRandom().nextBytes(randomData);
                    Files.write(configFile, randomData);
                    Files.delete(configFile);
                }
                log("✓ Destroyed password configuration (wrapped keys)");
            }
            
            // Destroy salt file
            Path saltFile = vaultRoot.resolve(".salt");
            if (Files.exists(saltFile)) {
                if (!dryRun) {
                    byte[] randomData = new byte[(int) Files.size(saltFile)];
                    new java.security.SecureRandom().nextBytes(randomData);
                    Files.write(saltFile, randomData);
                    Files.delete(saltFile);
                }
                log("✓ Destroyed salt file");
            }
            
            log("✓ CRYPTOGRAPHIC ERASURE COMPLETE");
            log("  All vault data is now PERMANENTLY UNRECOVERABLE");
            
        } catch (Exception e) {
            log("ERROR in cryptographic erasure: " + e.getMessage());
        }
    }
    
    /**
     * PHASE 2: Delete metadata and configuration files
     */
    private void deleteMetadataAndConfig(Path vaultRoot) {
        try {
            String[] configFiles = {
                "metadata.enc",
                "audit.log.enc",
                "config.enc",
                ".salt"
            };
            
            for (String filename : configFiles) {
                Path filePath = vaultRoot.resolve(filename);
                if (Files.exists(filePath)) {
                    if (!dryRun) {
                        Files.delete(filePath);
                    }
                    log("✓ Deleted: " + filename);
                }
            }
            
        } catch (Exception e) {
            log("ERROR deleting metadata: " + e.getMessage());
        }
    }
    
    /**
     * PHASE 3: Best-effort physical overwrite
     * 
     * NOTE: This is SECONDARY defense and has LIMITED EFFECTIVENESS on:
     * - SSDs (wear leveling, spare blocks)
     * - Journaling filesystems (ext3/4, NTFS)
     * - Copy-on-write filesystems (Btrfs, ZFS)
     * - Network/cloud storage
     * 
     * Cryptographic erasure (Phase 1) is the ONLY reliable method.
     */
    private void overwriteVaultFiles(Path vaultRoot) {
        try {
            log("NOTE: Physical overwrite has limited effectiveness on modern storage");
            log("      Cryptographic erasure (Phase 1) is the primary defense");
            
            // Overwrite encrypted files
            Path filesDir = vaultRoot.resolve("files");
            if (Files.exists(filesDir) && Files.isDirectory(filesDir)) {
                Files.list(filesDir).forEach(file -> {
                    try {
                        if (Files.isRegularFile(file)) {
                            if (!dryRun) {
                                SecureDeletion.secureDelete(file.toFile());
                            }
                            log("✓ Overwritten: " + file.getFileName());
                        }
                    } catch (Exception e) {
                        log("  Warning: Could not overwrite " + file.getFileName());
                    }
                });
            }
            
            // Overwrite decoy files
            Path decoysDir = vaultRoot.resolve("decoys");
            if (Files.exists(decoysDir) && Files.isDirectory(decoysDir)) {
                Files.list(decoysDir).forEach(file -> {
                    try {
                        if (Files.isRegularFile(file)) {
                            if (!dryRun) {
                                SecureDeletion.secureDelete(file.toFile());
                            }
                            log("✓ Overwritten: decoys/" + file.getFileName());
                        }
                    } catch (Exception e) {
                        log("  Warning: Could not overwrite decoy " + file.getFileName());
                    }
                });
            }
            
        } catch (Exception e) {
            log("ERROR during physical overwrite: " + e.getMessage());
        }
    }
    
    /**
     * PHASE 4: Delete vault directory structure
     */
    private void deleteVaultDirectories(Path vaultRoot) {
        try {
            String[] directories = {
                "files",
                "decoys",
                "logs",
                "temp"
            };
            
            for (String dirName : directories) {
                Path dirPath = vaultRoot.resolve(dirName);
                if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                    if (!dryRun) {
                        deleteDirectoryRecursive(dirPath);
                    }
                    log("✓ Deleted directory: " + dirName);
                }
            }
            
            // Delete vault root if empty
            if (!dryRun && Files.exists(vaultRoot)) {
                try {
                    Files.delete(vaultRoot);
                    log("✓ Deleted vault root directory");
                } catch (Exception e) {
                    log("  Note: Vault root not empty or in use");
                }
            }
            
        } catch (Exception e) {
            log("ERROR deleting directories: " + e.getMessage());
        }
    }
    
    /**
     * Recursively delete directory
     */
    private void deleteDirectoryRecursive(Path directory) throws Exception {
        if (Files.isDirectory(directory)) {
            Files.list(directory).forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        deleteDirectoryRecursive(path);
                    } else {
                        Files.delete(path);
                    }
                } catch (Exception e) {
                    // Continue with other files
                }
            });
        }
        Files.delete(directory);
    }
    
    /**
     * Log panic mode operations
     */
    private void log(String message) {
        String logEntry = java.time.LocalDateTime.now() + ": " + message;
        destructionLog.add(logEntry);
        
        // Also print to console for debugging (in real deployment, this would be silent)
        if (!dryRun || System.getProperty("ghostvault.debug") != null) {
            System.out.println("[PANIC] " + message);
        }
    }
    
    /**
     * Get destruction log (for testing/verification)
     */
    public List<String> getDestructionLog() {
        return new ArrayList<>(destructionLog);
    }
    
    /**
     * Clear destruction log
     */
    public void clearLog() {
        destructionLog.clear();
    }
    
    /**
     * Check if panic mode can be executed
     */
    public boolean canExecutePanicMode(Path vaultRoot) {
        return Files.exists(vaultRoot) && Files.isDirectory(vaultRoot);
    }
    
    /**
     * Estimate destruction time (for UI feedback)
     */
    public int getEstimatedDestructionTimeSeconds(Path vaultRoot) {
        try {
            if (!Files.exists(vaultRoot)) {
                return 1;
            }
            
            // Cryptographic erasure is fast (< 1 second)
            // Physical overwrite depends on vault size
            long vaultSize = calculateDirectorySize(vaultRoot.toFile());
            int estimatedSeconds = (int) Math.max(2, vaultSize / (10 * 1024 * 1024)); // 10MB/sec
            
            return Math.min(estimatedSeconds, 30); // Cap at 30 seconds
            
        } catch (Exception e) {
            return 5; // Default estimate
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
