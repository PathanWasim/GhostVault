package com.ghostvault.core;

import com.ghostvault.model.VaultFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Comprehensive tests for file persistence across application restarts
 */
public class PersistenceSecurityTest {
    
    @TempDir
    Path tempDir;
    
    private String vaultPath;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    
    @BeforeEach
    void setUp() {
        vaultPath = tempDir.resolve("test-vault").toString();
        fileManager = new FileManager(vaultPath);
        metadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
    }
    
    @Test
    @DisplayName("Files should persist across FileManager restarts")
    void testFilePersistenceAcrossRestarts() throws Exception {
        // Create test file
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.write(testFile.toPath(), "Test content for persistence".getBytes());
        
        // Store file in first FileManager instance
        VaultFile vaultFile = fileManager.storeFile(testFile);
        assertNotNull(vaultFile, "File should be stored successfully");
        
        // Verify file can be retrieved
        byte[] retrievedContent = fileManager.retrieveFile(vaultFile);
        assertNotNull(retrievedContent, "File content should be retrievable");
        assertEquals("Test content for persistence", new String(retrievedContent), 
            "Retrieved content should match original");
        
        // Create new FileManager instance (simulating application restart)
        FileManager newFileManager = new FileManager(vaultPath);
        
        // Verify file is still accessible from new instance
        byte[] persistedContent = newFileManager.retrieveFile(vaultFile);
        assertNotNull(persistedContent, "File should persist across FileManager restarts");
        assertEquals("Test content for persistence", new String(persistedContent), 
            "Persisted content should match original");
    }
    
    @Test
    @DisplayName("Metadata should persist across MetadataManager restarts")
    void testMetadataPersistenceAcrossRestarts() throws Exception {
        // Create test file and add to metadata
        File testFile = tempDir.resolve("metadata-test.txt").toFile();
        Files.write(testFile.toPath(), "Metadata test content".getBytes());
        
        VaultFile vaultFile = fileManager.storeFile(testFile);
        metadataManager.addFile(vaultFile);
        
        // Verify metadata contains the file
        List<VaultFile> files = metadataManager.getAllFiles();
        assertEquals(1, files.size(), "Metadata should contain 1 file");
        assertEquals(vaultFile.getFileName(), files.get(0).getFileName(), 
            "Metadata should contain the correct file");
        
        // Create new MetadataManager instance (simulating application restart)
        MetadataManager newMetadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        newMetadataManager.loadMetadata();
        
        // Verify metadata persisted
        List<VaultFile> persistedFiles = newMetadataManager.getAllFiles();
        assertEquals(1, persistedFiles.size(), "Metadata should persist across restarts");
        assertEquals(vaultFile.getFileName(), persistedFiles.get(0).getFileName(), 
            "Persisted metadata should contain the correct file");
        assertEquals(vaultFile.getFileId(), persistedFiles.get(0).getFileId(), 
            "Persisted file ID should match original");
    }
    
    @Test
    @DisplayName("Multiple files should persist correctly")
    void testMultipleFilesPersistence() throws Exception {
        // Create multiple test files
        String[] fileNames = {"file1.txt", "file2.pdf", "file3.jpg"};
        String[] fileContents = {"Content 1", "PDF content", "Image data"};
        VaultFile[] vaultFiles = new VaultFile[3];
        
        // Store all files
        for (int i = 0; i < fileNames.length; i++) {
            File testFile = tempDir.resolve(fileNames[i]).toFile();
            Files.write(testFile.toPath(), fileContents[i].getBytes());
            
            vaultFiles[i] = fileManager.storeFile(testFile);
            metadataManager.addFile(vaultFiles[i]);
        }
        
        // Verify all files are stored
        List<VaultFile> storedFiles = metadataManager.getAllFiles();
        assertEquals(3, storedFiles.size(), "Should have 3 files stored");
        
        // Create new instances (simulating restart)
        FileManager newFileManager = new FileManager(vaultPath);
        MetadataManager newMetadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
        newMetadataManager.loadMetadata();
        
        // Verify all files persist
        List<VaultFile> persistedFiles = newMetadataManager.getAllFiles();
        assertEquals(3, persistedFiles.size(), "All files should persist");
        
        // Verify each file content
        for (int i = 0; i < fileNames.length; i++) {
            VaultFile persistedFile = persistedFiles.stream()
                .filter(f -> f.getFileName().equals(fileNames[i]))
                .findFirst()
                .orElse(null);
            
            assertNotNull(persistedFile, "File " + fileNames[i] + " should persist");
            
            byte[] content = newFileManager.retrieveFile(persistedFile);
            assertEquals(fileContents[i], new String(content), 
                "Content of " + fileNames[i] + " should match original");
        }
    }
    
    @Test
    @DisplayName("Vault structure should be created correctly")
    void testVaultStructureCreation() {
        PersistentStorageManager storageManager = new PersistentStorageManager(vaultPath);
        
        // Initialize vault structure
        boolean initialized = storageManager.initializeVaultStructure();
        assertTrue(initialized, "Vault structure should initialize successfully");
        
        // Verify directory structure
        assertTrue(Files.exists(Path.of(vaultPath)), "Main vault directory should exist");
        assertTrue(Files.exists(Path.of(vaultPath, "files")), "Files directory should exist");
        assertTrue(Files.exists(Path.of(vaultPath, "metadata")), "Metadata directory should exist");
        assertTrue(Files.exists(Path.of(vaultPath, "backups")), "Backups directory should exist");
        
        // Verify directories are writable
        assertTrue(Files.isWritable(Path.of(vaultPath)), "Vault directory should be writable");
        assertTrue(Files.isWritable(Path.of(vaultPath, "files")), "Files directory should be writable");
    }
    
    @Test
    @DisplayName("Storage verification should work correctly")
    void testStorageVerification() {
        PersistentStorageManager storageManager = new PersistentStorageManager(vaultPath);
        
        // Initialize vault structure
        storageManager.initializeVaultStructure();
        
        // Verify storage
        StorageVerification verification = storageManager.verifyStorageIntegrity();
        
        assertTrue(verification.isVaultDirectoryExists(), "Vault directory should exist");
        assertTrue(verification.isVaultDirectoryWritable(), "Vault directory should be writable");
        assertTrue(verification.isFilesDirectoryExists(), "Files directory should exist");
        assertTrue(verification.isStorageHealthy(), "Storage should be healthy");
        assertNull(verification.getVerificationError(), "Should have no verification errors");
    }
    
    @Test
    @DisplayName("File deletion should work correctly")
    void testFileDeletion() throws Exception {
        // Create and store test file
        File testFile = tempDir.resolve("delete-test.txt").toFile();
        Files.write(testFile.toPath(), "File to be deleted".getBytes());
        
        VaultFile vaultFile = fileManager.storeFile(testFile);
        metadataManager.addFile(vaultFile);
        
        // Verify file exists
        byte[] content = fileManager.retrieveFile(vaultFile);
        assertNotNull(content, "File should exist before deletion");
        
        // Delete file
        fileManager.deleteFile(vaultFile);
        metadataManager.removeFile(vaultFile);
        
        // Verify file is deleted
        assertThrows(Exception.class, () -> {
            fileManager.retrieveFile(vaultFile);
        }, "Deleted file should not be retrievable");
        
        // Verify metadata is updated
        List<VaultFile> files = metadataManager.getAllFiles();
        assertEquals(0, files.size(), "Metadata should not contain deleted file");
    }
    
    @Test
    @DisplayName("Storage recovery should work when directories are missing")
    void testStorageRecovery() throws Exception {
        PersistentStorageManager storageManager = new PersistentStorageManager(vaultPath);
        
        // Initialize vault structure
        storageManager.initializeVaultStructure();
        
        // Delete files directory to simulate corruption
        Files.deleteIfExists(Path.of(vaultPath, "files"));
        
        // Verify storage is unhealthy
        StorageVerification verification = storageManager.verifyStorageIntegrity();
        assertFalse(verification.isStorageHealthy(), "Storage should be unhealthy after directory deletion");
        
        // Attempt recovery
        boolean recovered = storageManager.recoverVaultStructure();
        assertTrue(recovered, "Storage recovery should succeed");
        
        // Verify storage is healthy again
        StorageVerification recoveredVerification = storageManager.verifyStorageIntegrity();
        assertTrue(recoveredVerification.isStorageHealthy(), "Storage should be healthy after recovery");
    }
}