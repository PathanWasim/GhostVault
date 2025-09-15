package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Comprehensive test for FileManager and MetadataManager functionality
 */
public class FileManagerTest {
    
    private static final String TEST_VAULT_PATH = "test-vault";
    private static final String TEST_PASSWORD = "TestPassword123!";
    
    public static void main(String[] args) {
        FileManagerTest test = new FileManagerTest();
        
        try {
            test.setUp();
            test.testFileStorage();
            test.testFileRetrieval();
            test.testFileIntegrityVerification();
            test.testSecureFileDeletion();
            test.testMetadataManagement();
            test.testVaultStatistics();
            
            System.out.println("✅ All FileManager and MetadataManager tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            test.tearDown();
        }
    }
    
    private void setUp() throws Exception {
        // Clean up any existing test files
        tearDown();
        
        // Create test vault directory
        FileUtils.ensureDirectoryExists(TEST_VAULT_PATH);
        FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
    }
    
    private void tearDown() {
        try {
            // Clean up test files and directories
            deleteDirectory(new File(TEST_VAULT_PATH));
            deleteDirectory(new File(AppConfig.VAULT_DIR));
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    private void testFileStorage() throws Exception {
        System.out.println("Testing file storage with UUID-based naming...");
        
        // Create test file
        File testFile = createTestFile("test-document.txt", "This is a test document for vault storage.");
        
        // Initialize FileManager and set encryption key
        FileManager fileManager = new FileManager(TEST_VAULT_PATH);
        SecretKey encryptionKey = createTestEncryptionKey();
        fileManager.setEncryptionKey(encryptionKey);
        
        // Store file in vault
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Verify VaultFile metadata
        assert vaultFile != null : "VaultFile should not be null";
        assert "test-document.txt".equals(vaultFile.getOriginalName()) : "Original name should match";
        assert vaultFile.getFileId() != null : "File ID should be generated";
        assert vaultFile.getEncryptedName().endsWith(".enc") : "Encrypted name should end with .enc";
        assert vaultFile.getSize() == testFile.length() : "Size should match original file";
        assert vaultFile.getHash() != null : "Hash should be calculated";
        
        // Verify encrypted file exists
        Path encryptedPath = fileManager.getEncryptedFilePath(vaultFile);
        assert Files.exists(encryptedPath) : "Encrypted file should exist";
        
        // Clean up
        testFile.delete();
        fileManager.cleanup();
        
        System.out.println("✓ File storage test passed");
    }
    
    private void testFileRetrieval() throws Exception {
        System.out.println("Testing file retrieval and decryption...");
        
        // Create test file
        String originalContent = "This is test content for retrieval testing.";
        File testFile = createTestFile("retrieval-test.txt", originalContent);
        
        // Initialize FileManager
        FileManager fileManager = new FileManager(TEST_VAULT_PATH);
        SecretKey encryptionKey = createTestEncryptionKey();
        fileManager.setEncryptionKey(encryptionKey);
        
        // Store file
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Retrieve file data
        byte[] retrievedData = fileManager.retrieveFile(vaultFile);
        String retrievedContent = new String(retrievedData);
        
        // Verify content matches
        assert originalContent.equals(retrievedContent) : "Retrieved content should match original";
        
        // Test export functionality
        File exportFile = new File(TEST_VAULT_PATH, "exported-file.txt");
        fileManager.exportFile(vaultFile, exportFile);
        
        assert exportFile.exists() : "Exported file should exist";
        String exportedContent = new String(Files.readAllBytes(exportFile.toPath()));
        assert originalContent.equals(exportedContent) : "Exported content should match original";
        
        // Clean up
        testFile.delete();
        exportFile.delete();
        fileManager.cleanup();
        
        System.out.println("✓ File retrieval test passed");
    }
    
    private void testFileIntegrityVerification() throws Exception {
        System.out.println("Testing file integrity verification with SHA-256...");
        
        // Create test file
        File testFile = createTestFile("integrity-test.txt", "Content for integrity testing.");
        
        // Initialize FileManager
        FileManager fileManager = new FileManager(TEST_VAULT_PATH);
        SecretKey encryptionKey = createTestEncryptionKey();
        fileManager.setEncryptionKey(encryptionKey);
        
        // Store file
        VaultFile vaultFile = fileManager.storeFile(testFile);
        
        // Verify integrity
        assert fileManager.verifyFileIntegrity(vaultFile) : "File integrity should be valid";
        
        // Corrupt the encrypted file
        Path encryptedPath = fileManager.getEncryptedFilePath(vaultFile);
        byte[] corruptedData = Files.readAllBytes(encryptedPath);
        corruptedData[corruptedData.length - 1] = (byte) (corruptedData[corruptedData.length - 1] ^ 0xFF);
        Files.write(encryptedPath, corruptedData);
        
        // Verify integrity detection fails
        try {
            fileManager.retrieveFile(vaultFile);
            assert false : "Should detect corrupted file";
        } catch (SecurityException e) {
            assert e.getMessage().contains("integrity verification failed") : "Should report integrity failure";
        }
        
        // Clean up
        testFile.delete();
        fileManager.cleanup();
        
        System.out.println("✓ File integrity verification test passed");
    }
    
    private void testSecureFileDeletion() throws Exception {
        System.out.println("Testing secure file deletion with DoD 5220.22-M standard...");
        
        // Create test file
        File testFile = createTestFile("delete-test.txt", "This file will be securely deleted.");
        
        // Initialize FileManager
        FileManager fileManager = new FileManager(TEST_VAULT_PATH);
        SecretKey encryptionKey = createTestEncryptionKey();
        fileManager.setEncryptionKey(encryptionKey);
        
        // Store file
        VaultFile vaultFile = fileManager.storeFile(testFile);
        Path encryptedPath = fileManager.getEncryptedFilePath(vaultFile);
        
        // Verify file exists
        assert Files.exists(encryptedPath) : "Encrypted file should exist before deletion";
        
        // Perform secure deletion
        fileManager.secureDeleteFile(vaultFile);
        
        // Verify file is deleted
        assert !Files.exists(encryptedPath) : "Encrypted file should be deleted";
        
        // Test static secure delete method
        File tempFile = createTestFile("temp-delete.txt", "Temporary file for deletion test.");
        Path tempPath = tempFile.toPath();
        
        assert Files.exists(tempPath) : "Temp file should exist";
        FileManager.secureDeleteFile(tempPath);
        assert !Files.exists(tempPath) : "Temp file should be securely deleted";
        
        // Clean up
        testFile.delete();
        fileManager.cleanup();
        
        System.out.println("✓ Secure file deletion test passed");
    }
    
    private void testMetadataManagement() throws Exception {
        System.out.println("Testing metadata management with encrypted serialization...");
        
        // Initialize MetadataManager
        MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        SecretKey encryptionKey = createTestEncryptionKey();
        metadataManager.setEncryptionKey(encryptionKey);
        
        // Create test VaultFile
        VaultFile vaultFile = new VaultFile(
            "test-metadata.txt",
            "test-id-123",
            "test-id-123.enc",
            1024,
            "test-hash",
            System.currentTimeMillis()
        );
        
        // Add file to metadata
        metadataManager.addFile(vaultFile);
        
        // Verify file exists in metadata
        assert metadataManager.fileExists("test-id-123") : "File should exist in metadata";
        
        VaultFile retrievedFile = metadataManager.getFile("test-id-123");
        assert retrievedFile != null : "Retrieved file should not be null";
        assert "test-metadata.txt".equals(retrievedFile.getOriginalName()) : "File name should match";
        
        // Test FileMetadata compatibility
        MetadataManager.FileMetadata fileMetadata = metadataManager.getFileMetadata("test-id-123");
        assert fileMetadata != null : "FileMetadata should not be null";
        assert "test-metadata.txt".equals(fileMetadata.getFileName()) : "FileMetadata name should match";
        
        // Test search functionality
        vaultFile.setTags("document test important");
        metadataManager.addFile(vaultFile); // Update with tags
        
        assert metadataManager.searchFiles("document").size() > 0 : "Should find files by tag";
        assert metadataManager.searchFiles("test-metadata").size() > 0 : "Should find files by name";
        
        // Test persistence
        metadataManager.saveMetadata();
        
        // Create new instance and load
        MetadataManager metadataManager2 = new MetadataManager(AppConfig.METADATA_FILE);
        metadataManager2.setEncryptionKey(encryptionKey);
        metadataManager2.loadMetadata();
        
        assert metadataManager2.fileExists("test-id-123") : "File should exist after reload";
        
        // Test removal
        metadataManager2.removeFileMetadata("test-id-123");
        assert !metadataManager2.fileExists("test-id-123") : "File should be removed";
        
        // Clean up
        metadataManager.cleanup();
        metadataManager2.cleanup();
        
        System.out.println("✓ Metadata management test passed");
    }
    
    private void testVaultStatistics() throws Exception {
        System.out.println("Testing vault statistics...");
        
        // Initialize managers
        FileManager fileManager = new FileManager(TEST_VAULT_PATH);
        MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
        SecretKey encryptionKey = createTestEncryptionKey();
        
        fileManager.setEncryptionKey(encryptionKey);
        metadataManager.setEncryptionKey(encryptionKey);
        
        // Create and store multiple test files
        File testFile1 = createTestFile("stats-test1.txt", "Content for statistics test 1.");
        File testFile2 = createTestFile("stats-test2.pdf", "Content for statistics test 2.");
        
        VaultFile vaultFile1 = fileManager.storeFile(testFile1);
        VaultFile vaultFile2 = fileManager.storeFile(testFile2);
        
        metadataManager.addFile(vaultFile1);
        metadataManager.addFile(vaultFile2);
        
        // Test vault statistics
        FileManager.VaultStats vaultStats = fileManager.getVaultStats();
        assert vaultStats.getFileCount() >= 2 : "Should have at least 2 files";
        assert vaultStats.getTotalSize() > 0 : "Total size should be greater than 0";
        
        // Test metadata statistics
        MetadataManager.MetadataStats metadataStats = metadataManager.getMetadataStats();
        assert metadataStats.getFileCount() == 2 : "Should have exactly 2 files in metadata";
        assert metadataStats.getUniqueExtensions() == 2 : "Should have 2 unique extensions";
        
        System.out.println("Vault Stats: " + vaultStats);
        System.out.println("Metadata Stats: " + metadataStats);
        
        // Clean up
        testFile1.delete();
        testFile2.delete();
        fileManager.cleanup();
        metadataManager.cleanup();
        
        System.out.println("✓ Vault statistics test passed");
    }
    
    private File createTestFile(String name, String content) throws IOException {
        File file = new File(TEST_VAULT_PATH, name);
        Files.write(file.toPath(), content.getBytes());
        return file;
    }
    
    private SecretKey createTestEncryptionKey() throws Exception {
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        return passwordManager.deriveVaultKey(TEST_PASSWORD);
    }
}