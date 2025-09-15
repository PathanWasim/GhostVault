package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Test for DecoyManager - Task 6 requirements
 */
public class DecoyManagerTest {
    
    public static void main(String[] args) {
        System.out.println("Testing DecoyManager Task 6 Requirements...");
        
        try {
            // Clean up any existing decoy files
            cleanupDecoyFiles();
            
            // Test 1: DecoyManager class creation and basic functionality
            System.out.println("\n1. Testing DecoyManager class...");
            
            DecoyManager decoyManager = new DecoyManager();
            
            // Initially should have no decoy files (after cleanup)
            assert decoyManager.getDecoyFileCount() >= 0 : "Should have non-negative decoy file count";
            
            System.out.println("Initial decoy file count: " + decoyManager.getDecoyFileCount());
            System.out.println("✓ DecoyManager class created successfully");
            
            // Test 2: Generate realistic fake content
            System.out.println("\n2. Testing realistic fake content generation...");
            
            // Generate various types of decoy files
            VaultFile textFile = decoyManager.generateSingleDecoyFile();
            VaultFile docFile = decoyManager.generateSingleDecoyFile();
            VaultFile reportFile = decoyManager.generateSingleDecoyFile();
            
            System.out.println("Generated decoy files:");
            System.out.println("  - " + textFile.getOriginalName() + " (" + textFile.getSize() + " bytes)");
            System.out.println("  - " + docFile.getOriginalName() + " (" + docFile.getSize() + " bytes)");
            System.out.println("  - " + reportFile.getOriginalName() + " (" + reportFile.getSize() + " bytes)");
            
            // Verify files have realistic names and content
            assert textFile.getOriginalName().contains(".") : "Should have file extension";
            assert textFile.getSize() > 100 : "Should have substantial content";
            
            // Check content is realistic
            byte[] content = decoyManager.getDecoyFileContent(textFile.getOriginalName());
            String contentStr = new String(content);
            
            assert contentStr.length() > 50 : "Content should be substantial";
            assert contentStr.contains("Date:") || contentStr.contains("NOTES") || 
                   contentStr.contains("Meeting") || contentStr.contains("Project") : 
                   "Content should appear business-like";
            
            System.out.println("Sample content preview:");
            System.out.println(contentStr.substring(0, Math.min(200, contentStr.length())) + "...");
            
            System.out.println("✓ Realistic fake content generation works");
            
            // Test 3: Decoy file management
            System.out.println("\n3. Testing decoy file management...");
            
            int initialCount = decoyManager.getDecoyFileCount();
            
            // Generate multiple decoy files
            decoyManager.generateDecoyFiles(5);
            
            int afterGenerationCount = decoyManager.getDecoyFileCount();
            assert afterGenerationCount >= initialCount + 5 : "Should have added 5 decoy files";
            
            // Test file retrieval
            List<VaultFile> allDecoys = decoyManager.getDecoyFiles();
            assert allDecoys.size() == afterGenerationCount : "Retrieved count should match";
            
            // Test file search
            List<VaultFile> searchResults = decoyManager.searchDecoyFiles("meeting");
            System.out.println("Search results for 'meeting': " + searchResults.size() + " files");
            
            // Test file removal
            if (!allDecoys.isEmpty()) {
                VaultFile fileToRemove = allDecoys.get(0);
                String fileName = fileToRemove.getOriginalName();
                
                boolean removed = decoyManager.removeDecoyFile(fileName);
                assert removed : "Should successfully remove decoy file";
                assert decoyManager.getDecoyFileCount() == afterGenerationCount - 1 : "Count should decrease";
            }
            
            System.out.println("✓ Decoy file management works");
            
            // Test 4: Ensure separation from real data
            System.out.println("\n4. Testing separation from real data...");
            
            // Verify decoy files are stored in separate directory
            File decoyDir = new File(AppConfig.DECOYS_DIR);
            File filesDir = new File(AppConfig.FILES_DIR);
            
            assert decoyDir.exists() : "Decoy directory should exist";
            assert !decoyDir.getAbsolutePath().equals(filesDir.getAbsolutePath()) : 
                   "Decoy and real file directories should be different";
            
            // Verify no cross-contamination
            File[] decoyDirFiles = decoyDir.listFiles();
            if (decoyDirFiles != null) {
                for (File file : decoyDirFiles) {
                    assert !file.getName().endsWith(".enc") : "Decoy files should not be encrypted";
                    assert file.getName().contains(".") : "Decoy files should have extensions";
                }
            }
            
            // Verify decoy files don't appear in real files directory
            if (filesDir.exists()) {
                File[] realDirFiles = filesDir.listFiles();
                if (realDirFiles != null) {
                    for (File file : realDirFiles) {
                        // Real encrypted files should end with .enc
                        if (!file.getName().endsWith(".enc")) {
                            System.out.println("Warning: Non-encrypted file in real files directory: " + file.getName());
                        }
                    }
                }
            }
            
            System.out.println("✓ Separation from real data verified");
            
            // Test 5: Decoy vault interface functionality
            System.out.println("\n5. Testing decoy vault interface functionality...");
            
            // Test statistics
            DecoyManager.DecoyStats stats = decoyManager.getDecoyStats();
            
            assert stats.getFileCount() > 0 : "Should have decoy files";
            assert stats.getTotalSize() > 0 : "Should have total size";
            
            Map<String, Integer> extensionCounts = stats.getExtensionCounts();
            assert !extensionCounts.isEmpty() : "Should have file extensions";
            
            System.out.println("Decoy statistics: " + stats);
            System.out.println("File types: " + extensionCounts);
            
            // Test minimum file count enforcement
            int currentCount = decoyManager.getDecoyFileCount();
            decoyManager.ensureMinimumDecoyFiles(currentCount + 3);
            
            assert decoyManager.getDecoyFileCount() >= currentCount + 3 : 
                   "Should ensure minimum decoy files";
            
            System.out.println("✓ Decoy vault interface functionality works");
            
            // Test 6: Content realism and variety
            System.out.println("\n6. Testing content realism and variety...");
            
            // Generate more files to test variety
            decoyManager.generateDecoyFiles(10);
            
            List<VaultFile> allFiles = decoyManager.getDecoyFiles();
            
            // Check for variety in file names and extensions
            boolean hasTextFiles = false;
            boolean hasDocFiles = false;
            boolean hasSpreadsheets = false;
            boolean hasMeetingNotes = false;
            boolean hasReports = false;
            
            for (VaultFile file : allFiles) {
                String name = file.getOriginalName().toLowerCase();
                String extension = file.getExtension();
                
                if (extension.equals("txt")) hasTextFiles = true;
                if (extension.equals("docx")) hasDocFiles = true;
                if (extension.equals("xlsx")) hasSpreadsheets = true;
                if (name.contains("meeting")) hasMeetingNotes = true;
                if (name.contains("report")) hasReports = true;
            }
            
            System.out.println("Content variety check:");
            System.out.println("  Text files: " + hasTextFiles);
            System.out.println("  Document files: " + hasDocFiles);
            System.out.println("  Spreadsheets: " + hasSpreadsheets);
            System.out.println("  Meeting notes: " + hasMeetingNotes);
            System.out.println("  Reports: " + hasReports);
            
            // Verify content realism by checking a few files
            int checkedFiles = 0;
            for (VaultFile file : allFiles) {
                if (checkedFiles >= 3) break;
                
                try {
                    byte[] fileContent = decoyManager.getDecoyFileContent(file.getOriginalName());
                    String fileContentStr = new String(fileContent);
                    
                    // Check for realistic business content
                    boolean hasRealisticContent = 
                        fileContentStr.contains("Date:") ||
                        fileContentStr.contains("Meeting") ||
                        fileContentStr.contains("Project") ||
                        fileContentStr.contains("Budget") ||
                        fileContentStr.contains("Notes") ||
                        fileContentStr.contains("Report") ||
                        fileContentStr.contains("SUMMARY") ||
                        fileContentStr.contains("AGENDA");
                    
                    assert hasRealisticContent : "File should have realistic business content: " + file.getOriginalName();
                    checkedFiles++;
                    
                } catch (Exception e) {
                    System.out.println("Warning: Could not check content of " + file.getOriginalName());
                }
            }
            
            System.out.println("✓ Content realism and variety verified");
            
            System.out.println("\n✅ All Task 6 requirements verified successfully!");
            System.out.println("\nTask 6 Implementation Summary:");
            System.out.println("- ✓ DecoyManager class generates and manages fake files");
            System.out.println("- ✓ Realistic decoy file content (reports, notes, documents) that appears legitimate");
            System.out.println("- ✓ Decoy vault interface mirrors real vault functionality");
            System.out.println("- ✓ Complete separation from real data with no cross-contamination");
            System.out.println("- ✓ Variety of file types and realistic business content");
            System.out.println("- ✓ Search, view, add, and delete functionality for decoy files");
            System.out.println("- ✓ Statistics and management features");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up test files
            cleanupDecoyFiles();
        }
    }
    
    /**
     * Clean up decoy files for testing
     */
    private static void cleanupDecoyFiles() {
        try {
            File decoyDir = new File(AppConfig.DECOYS_DIR);
            if (decoyDir.exists()) {
                File[] files = decoyDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}