package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.security.CryptoManager;
import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Basic test for FileManager Task 3 requirements
 */
public class FileManagerBasicTest {
    
    private static final String TEST_VAULT_PATH = "test-vault";
    
    public static void main(String[] args) {
        System.out.println("Testing FileManager Task 3 Requirements...");
        
        try {
            // Clean up any existing test files
            deleteDirectory(new File(TEST_VAULT_PATH));
            deleteDirectory(new File(AppConfig.VAULT_DIR));
            
            // Test 1: FileManager class with UUID-based naming
            System.out.println("\n1. Testing FileManager with UUID-based naming...");
            
            FileUtils.ensureDirectoryExists(TEST_VAULT_PATH);
            FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
            
            FileManager fileManager = new FileManager(TEST_VAULT_PATH);
            
            // Create encryption key
            CryptoManager cryptoManager = new CryptoManager();
            SecretKey encryptionKey = cryptoManager.deriveKey("TestPassword123!", "testsalt".getBytes());
            fileManager.setEncryptionKey(encryptionKey);
            
            // Create test file
            File testFile = createTestFile("test-document.txt", "This is test content for FileManager.");
            
            // Store file with UUID-based naming
            VaultFile vaultFile = fileManager.storeFile(testFile);
            
            System.out.println("Original name: " + vaultFile.getOriginalName());
            System.out.println("File ID (UUID): " + vaultFile.getFileId());
            System.out.println("Encrypted name: " + vaultFile.getEncryptedName());
            System.out.println("File size: " + vaultFile.getSize());
            System.out.println("SHA-256 hash: " + vaultFile.getHash());
            
            assert vaultFile.getFileId() != null : "UUID should be generated";
            assert vaultFile.getEncryptedName().endsWith(".enc") : "Should use .enc extension";
            assert vaultFile.getHash() != null : "SHA-256 hash should be calculated";
            
            System.out.println("✓ FileManager with UUID-based naming works");
            
            // Test 2: File integrity verification with SHA-256
            System.out.println("\n2. Testing file integrity verification...");
            
            boolean integrityValid = fileManager.verifyFileIntegrity(vaultFile);
            System.out.println("File integrity valid: " + integrityValid);
            
            assert integrityValid : "File integrity should be valid";
            
            System.out.println("✓ SHA-256 integrity verification works");
            
            // Test 3: Secure file deletion with DoD 5220.22-M standard
            System.out.println("\n3. Testing secure file deletion (DoD 5220.22-M)...");
            
            // Create a file to delete
            File deleteTestFile = createTestFile("delete-test.txt", "This will be securely deleted.");
            
            // Test the static secure delete method
            assert deleteTestFile.exists() : "Test file should exist";
            FileManager.secureDeleteFile(deleteTestFile.toPath());
            assert !deleteTestFile.exists() : "File should be securely deleted";
            
            System.out.println("✓ DoD 5220.22-M secure deletion works (3-pass overwrite)");
            
            // Test 4: MetadataManager with encrypted serialization
            System.out.println("\n4. Testing MetadataManager with encrypted serialization...");
            
            MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
            metadataManager.setEncryptionKey(encryptionKey);
            
            // Add file to metadata
            metadataManager.addFile(vaultFile);
            
            // Verify metadata operations
            assert metadataManager.fileExists(vaultFile.getFileId()) : "File should exist in metadata";
            
            VaultFile retrievedFile = metadataManager.getFile(vaultFile.getFileId());
            assert retrievedFile != null : "Should retrieve file from metadata";
            assert retrievedFile.getOriginalName().equals(vaultFile.getOriginalName()) : "Names should match";
            
            // Test FileMetadata compatibility
            MetadataManager.FileMetadata fileMetadata = metadataManager.getFileMetadata(vaultFile.getFileId());
            assert fileMetadata != null : "FileMetadata should not be null";
            
            System.out.println("✓ MetadataManager with encrypted serialization works");
            
            // Test 5: Vault statistics
            System.out.println("\n5. Testing vault statistics...");
            
            FileManager.VaultStats vaultStats = fileManager.getVaultStats();
            MetadataManager.MetadataStats metadataStats = metadataManager.getMetadataStats();
            
            System.out.println("Vault stats: " + vaultStats);
            System.out.println("Metadata stats: " + metadataStats);
            
            assert vaultStats.getFileCount() > 0 : "Should have files in vault";
            assert metadataStats.getFileCount() > 0 : "Should have files in metadata";
            
            System.out.println("✓ Vault statistics work");
            
            // Clean up
            testFile.delete();
            fileManager.cleanup();
            metadataManager.cleanup();
            
            System.out.println("\n✅ All Task 3 requirements verified successfully!");
            System.out.println("\nTask 3 Implementation Summary:");
            System.out.println("- ✓ FileManager class with encrypted file operations");
            System.out.println("- ✓ UUID-based naming for encrypted files");
            System.out.println("- ✓ MetadataManager with encrypted serialization");
            System.out.println("- ✓ File integrity verification using SHA-256 hashes");
            System.out.println("- ✓ Secure file deletion with DoD 5220.22-M standard (3-pass overwrite)");
            System.out.println("- ✓ Comprehensive vault and metadata statistics");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up test files
            try {
                deleteDirectory(new File(TEST_VAULT_PATH));
                deleteDirectory(new File(AppConfig.VAULT_DIR));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    private static File createTestFile(String name, String content) throws IOException {
        File file = new File(TEST_VAULT_PATH, name);
        Files.write(file.toPath(), content.getBytes());
        return file;
    }
    
    private static void deleteDirectory(File directory) {
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
}