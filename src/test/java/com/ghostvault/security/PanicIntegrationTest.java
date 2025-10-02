package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for panic mode with cryptographic erasure
 */
@DisplayName("Panic Mode Integration Tests")
class PanicIntegrationTest {
    
    @TempDir
    Path tempVaultRoot;
    
    private PanicModeExecutor panicExecutor;
    
    @BeforeEach
    void setUp() throws Exception {
        panicExecutor = new PanicModeExecutor();
        
        // Create mock vault structure
        createMockVaultStructure();
    }
    
    /**
     * Create a mock vault structure for testing
     */
    private void createMockVaultStructure() throws Exception {
        // Create directories
        Files.createDirectories(tempVaultRoot.resolve("files"));
        Files.createDirectories(tempVaultRoot.resolve("decoys"));
        Files.createDirectories(tempVaultRoot.resolve("logs"));
        
        // Create mock files
        Files.write(tempVaultRoot.resolve("config.enc"), "mock config data".getBytes());
        Files.write(tempVaultRoot.resolve(".salt"), "mock salt data".getBytes());
        Files.write(tempVaultRoot.resolve("metadata.enc"), "mock metadata".getBytes());
        Files.write(tempVaultRoot.resolve("audit.log.enc"), "mock audit log".getBytes());
        
        // Create mock encrypted files
        Files.write(tempVaultRoot.resolve("files/file1.enc"), "encrypted data 1".getBytes());
        Files.write(tempVaultRoot.resolve("files/file2.enc"), "encrypted data 2".getBytes());
        Files.write(tempVaultRoot.resolve("files/file3.enc"), "encrypted data 3".getBytes());
        
        // Create mock decoy files
        Files.write(tempVaultRoot.resolve("decoys/decoy1.txt"), "decoy content 1".getBytes());
        Files.write(tempVaultRoot.resolve("decoys/decoy2.txt"), "decoy content 2".getBytes());
    }
    
    @Test
    @DisplayName("Should execute panic mode in dry-run without deleting files")
    void testDryRunMode() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, true);
        
        // Assert - Files should still exist in dry-run mode
        assertTrue(Files.exists(tempVaultRoot.resolve("config.enc")));
        assertTrue(Files.exists(tempVaultRoot.resolve(".salt")));
        assertTrue(Files.exists(tempVaultRoot.resolve("metadata.enc")));
        assertTrue(Files.exists(tempVaultRoot.resolve("files/file1.enc")));
        assertTrue(Files.exists(tempVaultRoot.resolve("decoys/decoy1.txt")));
        
        // Log should contain operations
        List<String> log = panicExecutor.getDestructionLog();
        assertFalse(log.isEmpty());
        assertTrue(log.stream().anyMatch(entry -> entry.contains("CRYPTOGRAPHIC ERASURE")));
    }
    
    @Test
    @DisplayName("Should destroy encryption keys in real mode")
    void testCryptographicErasure() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert - Critical files should be deleted
        assertFalse(Files.exists(tempVaultRoot.resolve("config.enc")),
            "Config file (wrapped keys) should be deleted");
        assertFalse(Files.exists(tempVaultRoot.resolve(".salt")),
            "Salt file should be deleted");
        
        // Log should show cryptographic erasure
        List<String> log = panicExecutor.getDestructionLog();
        assertTrue(log.stream().anyMatch(entry -> 
            entry.contains("CRYPTOGRAPHIC ERASURE COMPLETE")));
        assertTrue(log.stream().anyMatch(entry -> 
            entry.contains("PERMANENTLY UNRECOVERABLE")));
    }
    
    @Test
    @DisplayName("Should delete metadata and configuration files")
    void testMetadataDeletion() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert
        assertFalse(Files.exists(tempVaultRoot.resolve("metadata.enc")));
        assertFalse(Files.exists(tempVaultRoot.resolve("audit.log.enc")));
        
        // Log should show metadata deletion
        List<String> log = panicExecutor.getDestructionLog();
        assertTrue(log.stream().anyMatch(entry -> entry.contains("metadata")));
    }
    
    @Test
    @DisplayName("Should overwrite and delete encrypted files")
    void testFileOverwrite() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert - Encrypted files should be deleted
        assertFalse(Files.exists(tempVaultRoot.resolve("files/file1.enc")));
        assertFalse(Files.exists(tempVaultRoot.resolve("files/file2.enc")));
        assertFalse(Files.exists(tempVaultRoot.resolve("files/file3.enc")));
        
        // Log should show file operations
        List<String> log = panicExecutor.getDestructionLog();
        assertTrue(log.stream().anyMatch(entry -> entry.contains("Overwritten")));
    }
    
    @Test
    @DisplayName("Should delete decoy files")
    void testDecoyDeletion() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert
        assertFalse(Files.exists(tempVaultRoot.resolve("decoys/decoy1.txt")));
        assertFalse(Files.exists(tempVaultRoot.resolve("decoys/decoy2.txt")));
    }
    
    @Test
    @DisplayName("Should delete vault directories")
    void testDirectoryDeletion() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert - Directories should be deleted
        assertFalse(Files.exists(tempVaultRoot.resolve("files")));
        assertFalse(Files.exists(tempVaultRoot.resolve("decoys")));
        assertFalse(Files.exists(tempVaultRoot.resolve("logs")));
    }
    
    @Test
    @DisplayName("Should log all panic operations")
    void testLogging() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, true);
        
        // Assert
        List<String> log = panicExecutor.getDestructionLog();
        
        assertFalse(log.isEmpty());
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PANIC MODE INITIATED")));
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PHASE 1")));
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PHASE 2")));
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PHASE 3")));
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PHASE 4")));
        assertTrue(log.stream().anyMatch(entry -> entry.contains("PANIC MODE COMPLETED")));
        
        // Print log for verification
        System.out.println("\n=== Panic Mode Execution Log ===");
        log.forEach(System.out::println);
    }
    
    @Test
    @DisplayName("Should handle non-existent vault gracefully")
    void testNonExistentVault() throws Exception {
        // Arrange
        Path nonExistentPath = tempVaultRoot.resolve("nonexistent");
        
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            panicExecutor.executePanic(nonExistentPath, false);
        });
    }
    
    @Test
    @DisplayName("Should check if panic mode can be executed")
    void testCanExecutePanicMode() {
        // Assert
        assertTrue(panicExecutor.canExecutePanicMode(tempVaultRoot));
        assertFalse(panicExecutor.canExecutePanicMode(tempVaultRoot.resolve("nonexistent")));
    }
    
    @Test
    @DisplayName("Should estimate destruction time")
    void testEstimateDestructionTime() {
        // Act
        int estimatedSeconds = panicExecutor.getEstimatedDestructionTimeSeconds(tempVaultRoot);
        
        // Assert
        assertTrue(estimatedSeconds > 0);
        assertTrue(estimatedSeconds <= 30); // Should be capped at 30 seconds
        
        System.out.println("Estimated destruction time: " + estimatedSeconds + " seconds");
    }
    
    @Test
    @DisplayName("Should clear destruction log")
    void testClearLog() throws Exception {
        // Arrange
        panicExecutor.executePanic(tempVaultRoot, true);
        assertFalse(panicExecutor.getDestructionLog().isEmpty());
        
        // Act
        panicExecutor.clearLog();
        
        // Assert
        assertTrue(panicExecutor.getDestructionLog().isEmpty());
    }
    
    @Test
    @DisplayName("Should prioritize cryptographic erasure over physical overwrite")
    void testCryptoErasurePriority() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, false);
        
        // Assert - Check log order
        List<String> log = panicExecutor.getDestructionLog();
        
        int cryptoPhaseIndex = -1;
        int overwritePhaseIndex = -1;
        
        for (int i = 0; i < log.size(); i++) {
            if (log.get(i).contains("PHASE 1") && log.get(i).contains("CRYPTOGRAPHIC ERASURE")) {
                cryptoPhaseIndex = i;
            }
            if (log.get(i).contains("PHASE 3") && log.get(i).contains("physical overwrite")) {
                overwritePhaseIndex = i;
            }
        }
        
        assertTrue(cryptoPhaseIndex >= 0, "Cryptographic erasure phase should exist");
        assertTrue(overwritePhaseIndex >= 0, "Physical overwrite phase should exist");
        assertTrue(cryptoPhaseIndex < overwritePhaseIndex, 
            "Cryptographic erasure should occur before physical overwrite");
    }
    
    @Test
    @DisplayName("Should document SSD limitations in log")
    void testSSDLimitationsDocumented() throws Exception {
        // Act
        panicExecutor.executePanic(tempVaultRoot, true);
        
        // Assert
        List<String> log = panicExecutor.getDestructionLog();
        assertTrue(log.stream().anyMatch(entry -> 
            entry.toLowerCase().contains("ssd") || 
            entry.toLowerCase().contains("limited effectiveness")));
    }
}
